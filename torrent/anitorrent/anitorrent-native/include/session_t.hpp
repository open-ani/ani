
#ifndef SESSION_T_H
#define SESSION_T_H
#include <string>

#include "events.hpp"
#include "torrent_add_info_t.hpp"
#include "torrent_handle_t.hpp"
#include "torrent_info_t.hpp"
#include "peer_filter.hpp"

namespace anilt {
extern "C" {

struct session_settings_t final {
    /// libtorrent::settings_pack::download_rate_limit
    int download_rate_limit = 20 * 1024 * 1024;

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
    int connections_limit = 1000;

    /// libtorrent::settings_pack::max_peerlist_size
    int max_peerlist_size = 1500;
    
    // libtorrent::settings_pack::share_ratio_limit
    int share_ratio_limit = 110;

    /// libtorrent::settings_pack::handshake_client_version
    std::string handshake_client_version{};

    session_settings_t() = default;
};

class new_event_listener_t {
  public:
    virtual ~new_event_listener_t() = default;

    virtual void on_new_events() {}
};

class session_t final {
  public:
    // session_settings_t is owned by Java and will be destroyed after this call
    void start(const session_settings_t &settings);
    void apply_settings(const session_settings_t &settings);

    void resume() const;

    /**
     * @param handle [out]
     * @param info torrent to downlad
     * @param save_path
     */
    bool start_download(torrent_handle_t &handle, const torrent_add_info_t &info, std::string save_path) const;

    void release_handle(const torrent_handle_t &handle) const;

    bool set_new_event_listener(new_event_listener_t *listener) const;

    void process_events(event_listener_t *listener) const;

    void remove_listener() const;

    void set_peer_filter(anilt::peer_filter_t *filter);

    /// blocks
    void wait_for_alert(int timeout_seconds) const;

    void post_session_stats() const;

  private:
    std::shared_ptr<libtorrent::session> session_;
    peer_filter_t * peer_filter_ = nullptr;
    static bool compute_add_torrent_params(const torrent_add_info_t &info, lt::add_torrent_params &params);
};
}
} // namespace anilt

#endif // SESSION_T_H
