
#include "session_t.hpp"

#include <iostream>
#include <utility>

#include "global_lock.h"
#include "libtorrent/alert_types.hpp"
#include "libtorrent/bencode.hpp"
#include "libtorrent/magnet_uri.hpp"
#include "torrent_handle_t.hpp"

namespace anilt {
static std::string compute_torrent_hash(const std::shared_ptr<const lt::torrent_info> &ti) {
    std::vector<char> torrent_data;
    bencode(std::back_inserter(torrent_data), ti->info_section());
    return {torrent_data.begin(), torrent_data.end()};
}

void session_t::start(std::string user_agent) {
    function_printer_t _fp("session_t::start");
    guard_global_lock;
    using libtorrent::settings_pack;

    settings_pack settings;
    settings.set_int(settings_pack::piece_timeout, 1);
    settings.set_int(settings_pack::request_timeout, 1);
    settings.set_int(settings_pack::upload_rate_limit, 1024 * 1024); // 1MB/s
    settings.set_str(settings_pack::user_agent, std::move(user_agent));
    settings.set_int(settings_pack::alert_mask, libtorrent::alert_category::status |
                                                    libtorrent::alert_category::piece_progress |
                                                    libtorrent::alert_category::upload);

    session_ = std::make_shared<libtorrent::session>(std::move(settings));
}
void session_t::resume() const {
    function_printer_t _fp("session_t::resume");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid()) {
        session->resume();
    }
}
std::string session_t::fetch_magnet(const std::string uri, int timeout_seconds,
                                    const std::string save_path) { // NOLINT(*-unnecessary-value-param)
    function_printer_t _fp("session_t::fetch_magnet");
    guard_global_lock;
    const auto fn = "[anilt::fetch_magnet]: ";
    std::cerr << fn << uri << std::endl;

    const std::string &magnet_uri = uri;

    auto session_ptr = session_;
    if (!session_ptr || !session_ptr->is_valid()) {
        std::cerr << fn << "session_ is nullptr!" << std::endl;
        return "";
    }
    lt::session &session = *session_ptr;

    lt::add_torrent_params params = lt::parse_magnet_uri(magnet_uri);
    params.save_path = save_path; // specify the save path

    // Check if the torrent is already in the session
    lt::sha1_hash info_hash = params.info_hashes.v1; // Assuming v1, adjust if using v2
    lt::torrent_handle existing_handle = session.find_torrent(info_hash);

    if (existing_handle.is_valid()) {
        std::cerr << fn << "Torrent already added. Using exisiting file" << std::endl;
        if (const auto file = existing_handle.torrent_file()) {
            return compute_torrent_hash(file);
        }
        std::cerr << fn << "existing_handle.torrent_file() returned null" << std::endl;
        return "";
    }

    const auto start_time = std::chrono::high_resolution_clock::now();

    lt::torrent_handle handle = session.add_torrent(params);

    while (true) {
        std::vector<lt::alert *> alerts;
        session.pop_alerts(&alerts);

        for (lt::alert *alert: alerts) {
            if (lt::alert_cast<lt::add_torrent_alert>(alert)) {
            } else if (auto md = lt::alert_cast<lt::metadata_received_alert>(alert)) {
                std::cerr << fn << "Metadata received, computing hash and return" << std::endl;
                return compute_torrent_hash(md->handle.torrent_file());
            }
        }


        const auto current_time = std::chrono::high_resolution_clock::now();
        if (std::chrono::duration_cast<std::chrono::seconds>(current_time - start_time).count() >= timeout_seconds) {
            std::cerr << "Timeout reached." << std::endl;
            return "";
        }

        std::this_thread::sleep_for(std::chrono::milliseconds(200));
    }
}

// info is hold by Java and will be destroyed after this call
bool session_t::start_download(torrent_handle_t &handle, torrent_add_info_t &info, std::string save_path) const {
    function_printer_t _fp("session_t::start_download");
    guard_global_lock;
    if (info.kind == torrent_add_info_t::kKindUnset) {
        return false;
    }
    const auto session = session_;
    if (!session || !session->is_valid()) {
        return false;
    }

    lt::add_torrent_params params;

    if (info.kind == torrent_add_info_t::kKindMagnetUri) {
        params = lt::parse_magnet_uri(info.magnetUri);
    } else if (info.kind == torrent_add_info_t::kKindTorrentFile) {
        libtorrent::error_code ec;
        libtorrent::torrent_info ti(info.torrentFilePath, ec);
        params.ti = std::make_shared<lt::torrent_info>(std::move(ti));
    }

    params.save_path = std::move(save_path);
    // params.flags = libtorrent::torrent_flags::need_save_resume;

    // Check if the torrent is already in the session
    libtorrent::torrent_handle torrent_handle{};
    if (params.info_hashes.has_v1()) {
        const lt::sha1_hash info_hash = params.info_hashes.v1; // Assuming v1, adjust if using v2
        torrent_handle = session->find_torrent(info_hash);
    }
    if (!torrent_handle.is_valid() && params.ti) {
        torrent_handle = session->find_torrent(params.ti->info_hashes().v1);
    }

    if (torrent_handle.is_valid()) {
        std::cerr << "Torrent already added. " << std::endl;
    } else {
        libtorrent::error_code ec;
        torrent_handle = session->add_torrent(params, ec);
        if (ec || !torrent_handle.is_valid()) {
            std::cerr << "Failed to add torrent: " << ec.message() << std::endl;
            return false;
        }
    }

    handle.id = torrent_handle.id();
    handle.delegate = std::make_shared<libtorrent::torrent_handle>(torrent_handle);
    return true;
}
void session_t::release_handle(torrent_handle_t &handle) const {
    function_printer_t _fp("session_t::release_handle");
    guard_global_lock;
    const auto session = session_;
    if (const auto ref = handle.delegate; session && ref) {
        session->remove_torrent(*ref);
    }
}
bool session_t::set_listener(event_listener_t *listener) {
    function_printer_t _fp("session_t::set_listener");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid() && listener) {
        session->set_alert_notify([this, listener] { process_events(listener); });
        return true;
    }
    return false;
}

#if ENABLE_TRACE_LOGGING
#define ALERTS_LOG(log) std::cout << log
#else
#define ALERTS_LOG(log) (void *) 0
#endif

void session_t::process_events(event_listener_t *listener) {
    function_printer_t _fp("session_t::process_events");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid() && listener) {
        ALERTS_LOG("Alerts processing... " << std::flush);
        std::vector<lt::alert *> alerts;
        if (session->is_valid()) {
            session->pop_alerts(&alerts);
        } else {
            ALERTS_LOG("session invalid" << std::flush);
            return;
        }
        std::lock_guard _(listener->lock_);
        ALERTS_LOG("Poped " << std::flush);
        for (lt::alert *alert: alerts) {
            if (alert) {
                ALERTS_LOG("call " << alert->what() << ".." << std::flush);
                call_listener(alert, *session, *listener);
                ALERTS_LOG("ok " << std::flush);
            }
        }
        ALERTS_LOG("done" << std::endl << std::flush);
    }
}

bool session_t::remove_listener() {
    function_printer_t _fp("session_t::remove_listener");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid()) {
        session->set_alert_notify({});
        return true;
    }
    return false;
}

} // namespace anilt
