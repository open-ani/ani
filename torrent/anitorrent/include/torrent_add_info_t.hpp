#pragma once

#include <string>
#include "libtorrent/torrent_info.hpp"

namespace anilt {
extern "C" {

typedef unsigned int handle_id_t;

struct torrent_add_info_t final {
    std::string magnetUri;
    std::string torrentFilePath;

    int kind = 0;

    enum {
        kKindUnset = 0,
        kKindMagnetUri = 1,
        kKindTorrentFile = 2,
    };

    torrent_add_info_t() = default;
};
}
} // namespace anilt
