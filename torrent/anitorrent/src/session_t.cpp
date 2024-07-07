
#include "session_t.hpp"

#include <iostream>
#include <libtorrent/aux_/file_pointer.hpp>
#include <utility>

#include "global_lock.h"
#include "libtorrent/alert_types.hpp"
#include "libtorrent/bencode.hpp"
#include "libtorrent/magnet_uri.hpp"
#include "libtorrent/read_resume_data.hpp"
#include "torrent_handle_t.hpp"

namespace anilt {
static std::string compute_torrent_hash(const std::shared_ptr<const lt::torrent_info> &ti) {
    std::vector<char> torrent_data;
    bencode(std::back_inserter(torrent_data), ti->info_section());
    return {torrent_data.begin(), torrent_data.end()};
}

static std::vector<std::string> splitString(const std::string &str, const char *delimiter) {
    if (str.empty())
        return {};
    std::vector<std::string> tokens;
    const auto cstr = new char[str.length() + 1];
    std::strcpy(cstr, str.c_str());

    char *token = std::strtok(cstr, delimiter);
    while (token != nullptr) {
        tokens.emplace_back(token);
        token = std::strtok(nullptr, delimiter);
    }

    delete[] cstr;
    return tokens;
}

template<typename Container>
static std::string join_to_string(const Container &container, const std::string &delimiter) {
    std::ostringstream result;
    auto it = container.begin();

    if (it != container.end()) {
        result << *it;
        ++it;
    }

    while (it != container.end()) {
        result << delimiter << *it;
        ++it;
    }

    return result.str();
}

static int load_file(std::string const &filename, std::vector<char> &v, libtorrent::error_code &ec,
                     int const max_buffer_size = 80000000) {
    ec.clear();
#ifdef TORRENT_WINDOWS
    libtorrent::aux::file_pointer f(::_wfopen(convert_to_native_path_string(filename).c_str(), L"rb"));
#else
    libtorrent::aux::file_pointer f(std::fopen(filename.c_str(), "rb"));
#endif
    if (f.file() == nullptr) {
        ec.assign(errno, boost::system::generic_category());
        return -1;
    }

    if (std::fseek(f.file(), 0, SEEK_END) < 0) {
        ec.assign(errno, boost::system::generic_category());
        return -1;
    }
    std::int64_t const s = std::ftell(f.file());
    if (s < 0) {
        ec.assign(errno, boost::system::generic_category());
        return -1;
    }
    if (s > max_buffer_size) {
        ec = libtorrent::errors::metadata_too_large;
        return -1;
    }
    if (std::fseek(f.file(), 0, SEEK_SET) < 0) {
        ec.assign(errno, boost::system::generic_category());
        return -1;
    }
    v.resize(std::size_t(s));
    if (s == 0)
        return 0;
    std::size_t const read = std::fread(v.data(), 1, v.size(), f.file());
    if (read != std::size_t(s)) {
        if (std::feof(f.file())) {
            v.resize(read);
            return 0;
        }
        ec.assign(errno, boost::system::generic_category());
        return -1;
    }
    return 0;
}

void session_t::start(const session_settings_t &settings) {
    function_printer_t _fp("session_t::start");
    guard_global_lock;
    using libtorrent::settings_pack;

    settings_pack s;

    s.set_bool(settings_pack::enable_dht,
               true); // this will start dht immediately when the setting is started
    s.set_bool(settings_pack::enable_lsd, true);

    s.set_int(settings_pack::active_downloads, settings.active_downloads);
    s.set_int(settings_pack::active_seeds, settings.active_seeds);
    s.set_int(settings_pack::connections_limit, settings.connections_limit);
    s.set_int(settings_pack::max_peerlist_size, settings.max_peerlist_size);

    s.set_int(settings_pack::piece_timeout, 1);
    s.set_int(settings_pack::request_timeout, 1);
    s.set_int(settings_pack::peer_connect_timeout, 2);

    s.set_str(settings_pack::user_agent, settings.user_agent);
    s.set_str(settings_pack::peer_fingerprint, settings.peer_fingerprint);
    s.set_int(settings_pack::alert_queue_size, 10000);
    s.set_bool(settings_pack::close_redundant_connections, true);

    {
        // 在原有的基础上添加额外的 DHT bootstrap 节点
        const auto existing = s.get_str(settings_pack::dht_bootstrap_nodes);
        std::set<std::string> nodes{};
        for (const auto &part: splitString(existing, ",")) {
            nodes.insert(part);
        }
        for (const auto &dht_bootstrap_nodes_extra: settings.dht_bootstrap_nodes_extra) {
            nodes.insert(dht_bootstrap_nodes_extra);
        }
        const auto res = join_to_string(nodes, ",");
        s.set_str(settings_pack::dht_bootstrap_nodes, res);
    }

    s.set_str(settings_pack::handshake_client_version, settings.handshake_client_version);

    s.set_int(settings_pack::download_rate_limit, settings.download_rate_limit);
    s.set_int(settings_pack::upload_rate_limit, settings.upload_rate_limit); // 1MB/s
    s.set_int(settings_pack::alert_mask,
              libtorrent::alert_category::status | libtorrent::alert_category::piece_progress |
                  libtorrent::alert_category::file_progress | libtorrent::alert_category::upload);

    session_ = std::make_shared<libtorrent::session>(std::move(s));
}

void session_t::resume() const {
    function_printer_t _fp("session_t::resume");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid()) {
        session->resume();
    }
}

[[nodiscard]] std::string
session_t::fetch_magnet(const std::string &uri, const int timeout_seconds,
                        const std::string &save_path) const { // NOLINT(*-unnecessary-value-param)
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

[[nodiscard]] static bool load_resume_data(const std::string &path, libtorrent::add_torrent_params &params) {
    std::vector<char> buf;
    libtorrent::error_code ec;
    if (const int ret = load_file(path, buf, ec); ret < 0)
        return false;

    const libtorrent::bdecode_node e = libtorrent::bdecode(buf, ec);
    if (ec)
        return false;

    params = read_resume_data(e, ec);
    if (ec)
        return false;
    return true;
}

[[nodiscard]] bool session_t::compute_add_torrent_params(const torrent_add_info_t &info,
                                                         lt::add_torrent_params &params) {
    if (!info.resume_data_path.empty()) {
        if (load_resume_data(info.resume_data_path, params)) {
            return true;
        }
    }
    if (info.kind == torrent_add_info_t::kKindMagnetUri) {
        params = lt::parse_magnet_uri(info.magnet_uri);
        return true;
    }
    if (info.kind == torrent_add_info_t::kKindTorrentFile) {
        libtorrent::error_code ec;
        libtorrent::torrent_info ti(info.torrent_file_path, ec);
        params.ti = std::make_shared<lt::torrent_info>(std::move(ti));
        return true;
    }
    return false;
}
// info is hold by Java and will be destroyed after this call
bool session_t::start_download(torrent_handle_t &handle, const torrent_add_info_t &info, std::string save_path) const {
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

    if (!compute_add_torrent_params(info, params)) {
        return false;
    }

    params.save_path = std::move(save_path);
    params.flags |= libtorrent::torrent_flags::need_save_resume | libtorrent::torrent_flags::default_dont_download;
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
    handle.handle_ = std::make_shared<libtorrent::torrent_handle>(torrent_handle);
    return true;
}

void session_t::release_handle(const torrent_handle_t &handle) const {
    function_printer_t _fp("session_t::release_handle");
    guard_global_lock;
    const auto session = session_;
    if (const auto ref = handle.handle_; session && ref) {
        session->remove_torrent(*ref);
    }
}

bool session_t::set_new_event_listener(new_event_listener_t *listener) const {
    function_printer_t _fp("session_t::set_new_event_listener");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid() && listener) {
        session->set_alert_notify([this, listener] { listener->on_new_events(); });
        return true;
    }
    return false;
}

#if ENABLE_TRACE_LOGGING
#define ALERTS_LOG(log) std::cout << log
#else
#define ALERTS_LOG(log) (void *) 0
#endif

void session_t::process_events(event_listener_t *listener) const {
    function_printer_t _fp("session_t::process_events");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid() && listener) {
        ALERTS_LOG("Alerts processing... " << std::flush);
        std::lock_guard _(listener->lock_);
        std::vector<lt::alert *> alerts;
        if (session->is_valid()) {
            session->pop_alerts(&alerts);
        } else {
            ALERTS_LOG("session invalid" << std::flush);
            return;
        }
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

void session_t::remove_listener() const {
    function_printer_t _fp("session_t::remove_listener");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid()) {
        session->set_alert_notify({});
    }
}
void session_t::wait_for_alert(const int timeout_seconds) const {
    function_printer_t _fp("session_t::wait_for_alert");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid()) {
        session->wait_for_alert(std::chrono::seconds(timeout_seconds));
    }
}

} // namespace anilt
