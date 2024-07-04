
#include "session_t.h"

#include <iostream>

#include "libtorrent/alert_types.hpp"
#include "libtorrent/bencode.hpp"
#include "libtorrent/magnet_uri.hpp"
#include "torrent_handle_t.h"

namespace anilt {
static std::string compute_torrent_hash(const std::shared_ptr<const lt::torrent_info> &ti) {
    std::vector<char> torrent_data;
    bencode(std::back_inserter(torrent_data), ti->info_section());
    return std::string(torrent_data);
}

void session_t::start(const char *user_agent) {
    using libtorrent::settings_pack;

    settings_pack settings;
    settings.set_str(settings_pack::user_agent, user_agent);
    settings.set_int(settings_pack::alert_mask, libtorrent::alert_category::status | libtorrent::alert_category::error |
                                                    libtorrent::alert_category::piece_progress |
                                                    libtorrent::alert_category::upload |
                                                    libtorrent::alert_category::stats);

    session_ = std::make_shared<libtorrent::session>(settings);
}
void session_t::resume() const {
    if (const auto session = session_) {
        session->resume();
    }
}
std::string session_t::fetch_magnet(const std::string &uri, int timeout_seconds, const std::string &save_path) {
    const auto fn = "[anilt::fetch_magnet]: ";
    std::cerr << fn << uri << std::endl;

    const std::string &magnet_uri = uri;

    auto session_ptr = session_;
    if (!session_ptr) {
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
bool session_t::start_download(torrent_handle_t &handle, torrent_add_info_t &info, const std::string &save_path) const {
    lt::add_torrent_params params;
    const auto inf = info.info;
    const auto session = session_;
    if (!inf || !session) {
        return false;
    }
    params.ti = inf;
    params.save_path = save_path;

    auto torrent_handle = session->add_torrent(params);
    handle.id = torrent_handle.id();
    handle.delegate = std::make_shared<libtorrent::torrent_handle>(torrent_handle);
    return true;
}
void session_t::release_handle(torrent_handle_t &handle) const {
    const auto session = session_;
    if (const auto ref = handle.delegate; session && ref) {
        session->remove_torrent(*ref);
    }
}
void session_t::set_listener(event_listener_t &listener) {
    if (const auto session = session_) {
        session->set_alert_notify([=] {
            std::vector<lt::alert *> alerts;
            session->pop_alerts(&alerts);
            for (lt::alert *alert: alerts) {
                if (const auto event = convert_event(alert)) {
                    listener.on_event(*event);
                }

                if (const auto a = lt::alert_cast<lt::piece_finished_alert>(alert)) {
                    listener.on_piece_finished(a->handle.id(), a->piece_index);
                }
            }
        });
    }
}
} // namespace anilt
