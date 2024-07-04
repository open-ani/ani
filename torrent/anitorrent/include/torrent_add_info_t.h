#pragma once

#include <string>
#include "libtorrent/torrent_info.hpp"

namespace anilt {
extern "C" {

typedef unsigned int handle_id_t;

struct torrent_add_info_t final {
    std::string name{};
    std::string infohash_hex{};
    int file_count = 0;

    int error_code = 0;
    std::string error_message{};

    std::shared_ptr<libtorrent::torrent_info> info{};

    bool parse(const std::string &encoded);

    torrent_add_info_t() = default;
};

}
} // namespace anilt
