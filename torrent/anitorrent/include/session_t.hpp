
#ifndef SESSION_T_H
#define SESSION_T_H
#include <string>

#include "events.hpp"
#include "torrent_add_info_t.hpp"
#include "torrent_handle_t.hpp"
#include "torrent_info_t.hpp"

namespace anilt {
extern "C" {

struct session_settings_t final {
    /// libtorrent::settings_pack::download_rate_limit
    int download_rate_limit = 0;

    /// libtorrent::settings_pack::upload_rate_limit
    int upload_rate_limit = 1024 * 1024;

    /// libtorrent::settings_pack::active_downloads
    int active_seeds = 4;

    /// libtorrent::settings_pack::active_seeds
    int active_downloads = 4;

    /// libtorrent::settings_pack::user_agent
    std::string user_agent = "anilt/3.0.0";

    /// libtorrent::settings_pack::peer_fingerprint
    std::string peer_fingerprint = "anilt/3.0.0";

    /// Comma separated list of DHT bootstrap nodes
    /// libtorrent::settings_pack::dht_bootstrap_nodes
    std::set<std::string> dht_bootstrap_nodes_extra{};

    void dht_bootstrap_nodes_extra_add(const std::string &node) { dht_bootstrap_nodes_extra.insert(node); }

    /// Comma separated list of trackers
    std::string trackers_extra{};

    /// libtorrent::settings_pack::connections_limit
    int connections_limit = 500;

    /// libtorrent::settings_pack::max_peerlist_size
    int max_peerlist_size = 500;

    /// libtorrent::settings_pack::handshake_client_version
    std::string handshake_client_version{};

    session_settings_t() = default;
};

class session_t final {
  public:
    // session_settings_t is owned by Java and will be destroyed after this call
    void start(const session_settings_t &settings);

    void resume() const;

    [[deprecated]] [[nodiscard]] std::string fetch_magnet(const std::string &uri, int timeout_seconds,
                                                          const std::string &save_path) const;

    /**
     * @param handle [out]
     * @param info torrent to downlad
     * @param save_path
     */
    bool start_download(torrent_handle_t &handle, const torrent_add_info_t &info, std::string save_path) const;

    void release_handle(const torrent_handle_t &handle) const;

    [[deprecated]] // 在非 JVM 线程调用回调可能导致 VM crash. Use [process_events] instead
    bool
    set_listener(event_listener_t *listener) const;

    void process_events(event_listener_t *listener) const;

    void remove_listener() const;

  private:
    std::shared_ptr<libtorrent::session> session_;
};
}
} // namespace anilt

#endif // SESSION_T_H
