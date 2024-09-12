#pragma once

#include <string>

namespace anilt {
extern "C" {

typedef unsigned int handle_id_t;

struct torrent_add_info_t final {
    std::string magnet_uri{};
    std::string torrent_file_path{};

    std::string resume_data_path{};

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
