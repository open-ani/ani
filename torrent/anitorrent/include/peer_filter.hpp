#ifndef PEER_FILTER_H
#define PEER_FILTER_H

#include <iostream>
#include "plugin/peer_filter_plugin.h"

namespace anilt {
    extern "C" {

    struct peer_info_t final {
        uint32_t torrent_handle_id = 0;

        std::string peer_id{};
        std::string client{};
        std::string ip_addr{};
        unsigned short ip_port = 0;

        float progress = 0.0;
        int64_t total_download = 0;
        int64_t total_upload = 0;

        uint32_t flags = 0;
    };

    class peer_filter_t {
    public:
        virtual ~peer_filter_t() = default;

        virtual bool on_filter(const peer_info_t &) = 0;

    private:
        friend class session_t;
    };
    }

    peer_info_t parse_peer_info(const lt::torrent_handle &th, const lt::peer_info &info);

    std::shared_ptr<lt::torrent_plugin> create_peer_filter(const lt::torrent_handle &th,
                                                           const std::function<bool(
                                                                   anilt::peer_info_t *)> &filter);


} // namespace anilt

#endif // PEER_FILTER_H
