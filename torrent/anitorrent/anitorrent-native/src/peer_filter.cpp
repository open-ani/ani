#include "peer_filter.hpp"


#include "libtorrent/extensions.hpp"
#include "libtorrent/peer_connection_handle.hpp"
#include "libtorrent/peer_connection_interface.hpp"

namespace anilt {
    void drop_connection(lt::peer_connection_handle handle) {
        handle.disconnect(boost::asio::error::connection_refused, lt::operation_t::bittorrent,
                          lt::disconnect_severity_t{0});
    }

    peer_info_t parse_peer_info(const lt::torrent_handle &th, const lt::peer_info &info) {
        // peer_id 一定是 160 位的, digest32<160>::data() 返回其 char pointer. 
        // 所以 vector 的大小为 20
        std::vector<char> peer_id(20);
        memcpy(&peer_id[0], info.pid.data(), 20);
        
        const peer_info_t info_t = {
            th.id(),
            peer_id,
            info.client,
            info.ip.address().to_string(),
            info.ip.port(),
            info.progress,
            info.total_download,
            info.total_upload,
            static_cast<uint32_t>(info.flags)
        };

        return info_t;
    }

    std::shared_ptr<lt::torrent_plugin> create_peer_filter(const lt::torrent_handle &th,
                                                           const std::function<bool(
                                                                   peer_info_t &)> &filter) {
        // ignore private torrents
        if (th.torrent_file() && th.torrent_file()->priv())
            return nullptr;

        plugin::filter_function raw_filter = [filter, th](const lt::peer_info &info,
                                                      const bool handshake, bool *stop_filtering) {
            peer_info_t peer_info = parse_peer_info(th, info);

            const bool matched = filter(peer_info);
            *stop_filtering = !handshake && !matched;

            return matched;
        };

        return std::make_shared<plugin::peer_action_plugin>(std::move(raw_filter), drop_connection);
    }
} // namespace anilt