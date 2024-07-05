
#include "torrent_info_t.hpp"
#include "libtorrent/file_storage.hpp"

namespace anilt {
void torrent_info_t::parse(const libtorrent::torrent_info &torrent_info) {
    const auto &fs = torrent_info.files();

    total_size = fs.total_size();
    num_pieces = fs.num_pieces();
    piece_length = fs.piece_length();
    if (num_pieces > 0) {
        last_piece_size = fs.piece_size(fs.num_pieces() - 1);
    }

    this->files.clear();
    for (int i = 0; i < fs.num_files(); ++i) {
        auto &f = this->files.emplace_back();
        f.index = i;
        f.name = fs.file_name(i);
        f.path = fs.file_path(i);
        f.offset = fs.file_offset(i);
        f.size = fs.file_size(i);
    }
}
} // namespace anilt
