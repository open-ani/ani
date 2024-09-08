#include "peer_filter.hpp"


#include "libtorrent/extensions.hpp"
#include "libtorrent/peer_connection_handle.hpp"
#include "libtorrent/peer_connection_interface.hpp"

namespace anilt {
    void drop_connection(lt::peer_connection_handle handle) {
        handle.disconnect(boost::asio::error::connection_refused, lt::operation_t::bittorrent,
                          lt::disconnect_severity_t{0});
    }

    std::shared_ptr<peer_info_t> parse_peer_info(const lt::peer_info &info) {
        const std::shared_ptr<peer_info_t> info_t = std::make_shared<peer_info_t>();

        info_t->peer_id = info.pid.data();
        info_t->client = info.client;
        info_t->ip_addr = info.ip.address().to_string();
        info_t->ip_port = info.ip.port();

        return info_t;
    }

    std::shared_ptr<lt::torrent_plugin> create_peer_filter(const lt::torrent_handle &th,
                                                           const std::function<bool(
                                                                   peer_info_t *)> &filter) {
        // ignore private torrents
        if (th.torrent_file() && th.torrent_file()->priv())
            return nullptr;

        plugin::filter_function raw_filter = [filter](const lt::peer_info &info,
                                                      const bool handshake, bool *stop_filtering) {
            const auto peer_info = parse_peer_info(info);

            bool matched = filter(peer_info.get());
            *stop_filtering = !handshake && !matched;

            return matched;
        };

        return std::make_shared<plugin::peer_action_plugin>(std::move(raw_filter), drop_connection);
    }
} // namespace anilt