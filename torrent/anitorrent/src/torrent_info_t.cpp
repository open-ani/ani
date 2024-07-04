
#include "torrent_info_t.h"
#include "libtorrent/file_storage.hpp"

namespace anilt {
void torrent_info_t::parse(const libtorrent::torrent_info &torrent_info) {
    total_size = torrent_info.total_size();
    num_pieces = torrent_info.num_pieces();
    piece_length = torrent_info.piece_length();
    this->files.clear();
    for (auto &file: torrent_info.files()) {
        auto f = this->files.emplace_back();
        f.index = file.path_index;
        // f.name = std::string(file.filename());
        f.root = std::string(file.root);
        f.offset = file.offset;
        f.size = file.size;
    }
}
} // namespace anilt
