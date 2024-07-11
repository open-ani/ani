
#include "session_t.hpp"

#include <fstream>
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


static std::vector<std::string> splitString(const std::string &str, const std::string& delimiter) {
    std::vector<std::string> tokens;
    std::string::size_type start = 0;
    std::string::size_type end = str.find(delimiter);

    while (end != std::string::npos) {
        tokens.push_back(str.substr(start, end - start));
        start = end + delimiter.length();
        end = str.find(delimiter, start);
    }

    tokens.push_back(str.substr(start));

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

static std::vector<char> load_file_to_vector(const std::string &filePath) {
    // Open the file in binary mode
    std::ifstream inFile(filePath, std::ios::binary);

    // Check if the file was opened successfully
    if (!inFile) {
        return {};
    }

    // Seek to the end of the file to determine its size
    inFile.seekg(0, std::ios::end);
    const std::streamsize size = inFile.tellg();
    inFile.seekg(0, std::ios::beg);

    // Create a vector to hold the file contents
    std::vector<char> buffer(size);

    // Read the contents of the file into the vector
    if (!inFile.read(buffer.data(), size)) {
        return {};
    }

    // Close the file
    inFile.close();

    return std::move(buffer);
}

#if ENABLE_TRACE_LOGGING
#define START_LOG(log) std::cout << log
#else
#define START_LOG(log) (void *) 0
#endif

void session_t::start(const session_settings_t &settings) {
    function_printer_t _fp("session_t::start");
    guard_global_lock;
    using libtorrent::settings_pack;

    START_LOG("Starting session..." << std::flush);
    settings_pack s{};
    START_LOG("Pack initialied" << std::flush);

    s.set_bool(settings_pack::enable_dht,
               true); // this will start dht immediately when the setting is started
    s.set_bool(settings_pack::enable_lsd, true);

    s.set_int(settings_pack::download_rate_limit, settings.download_rate_limit);
    s.set_int(settings_pack::upload_rate_limit, settings.upload_rate_limit); // 1MB/s

    s.set_int(settings_pack::active_downloads, 8);
    s.set_int(settings_pack::active_seeds, settings.active_seeds);
    s.set_int(settings_pack::active_limit, 2000);

    s.set_int(settings_pack::piece_timeout, 5);
    s.set_int(settings_pack::request_timeout, 1);
    s.set_int(settings_pack::max_out_request_queue, 2000);
    s.set_int(settings_pack::torrent_connect_boost, 200);

    // peers
    s.set_int(settings_pack::connections_limit, settings.connections_limit);
    s.set_int(settings_pack::max_peerlist_size, settings.max_peerlist_size);
    s.set_int(settings_pack::peer_connect_timeout, 5);
    s.set_int(settings_pack::max_failcount, 2);
    s.set_int(settings_pack::max_peer_recv_buffer_size, 5 * 1024 * 1024);

    s.set_int(settings_pack::aio_threads, 8);
    s.set_int(settings_pack::checking_mem_usage, 2048);

    START_LOG("Set int values ok" << std::flush);

    s.set_str(settings_pack::user_agent, settings.user_agent);
    s.set_str(settings_pack::peer_fingerprint, settings.peer_fingerprint);
    s.set_str(settings_pack::handshake_client_version, settings.handshake_client_version);
    s.set_int(settings_pack::alert_queue_size, 10000);

    // seeding
    s.set_int(settings_pack::max_allowed_in_request_queue, 100);
    s.set_int(settings_pack::suggest_mode, settings_pack::suggest_read_cache);
    // s.set_bool(settings_pack::close_redundant_connections, true);
    START_LOG("Start set dht_bootstrap_nodes_extra" << std::flush);

    if (!settings.dht_bootstrap_nodes_extra.empty()) {
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
    START_LOG("set dht_bootstrap_nodes_extra ok" << std::flush);

    s.set_int(settings_pack::alert_mask,
              libtorrent::alert_category::status | libtorrent::alert_category::piece_progress |
                  libtorrent::alert_category::file_progress | libtorrent::alert_category::upload);

    START_LOG("create session" << std::flush);

    session_ = std::make_shared<libtorrent::session>(s);
    START_LOG("session created" << std::flush);
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
    std::vector<char> buf = load_file_to_vector(path);
    if (buf.empty())
        return false;

    libtorrent::error_code ec;
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
        std::cerr << "Had resume_data_path but failed to load resume data, ignoring" << std::endl;
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
    params.flags |= libtorrent::torrent_flags::need_save_resume // 初始化好后立即 save 一个信息, 因为我们不会保存
                                                                // .torrent 文件, 这样下次启动时无需从磁力链请求信息
                    | libtorrent::torrent_flags::default_dont_download; // 所有文件默认不下载, 初始化完毕后会立即暂停

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
void session_t::post_session_stats() const {
    function_printer_t _fp("session_t::post_session_stats");
    guard_global_lock;
    if (const auto session = session_; session && session->is_valid()) {
        session->post_session_stats();
    }
}

} // namespace anilt
