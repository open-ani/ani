
#include "torrent_info_t.hpp"

#include "global_lock.h"
#include "libtorrent/file_storage.hpp"

namespace anilt {
void torrent_info_t::parse(const libtorrent::torrent_info &torrent_info) {
    function_printer_t _fp("torrent_info_t::parse");
    guard_global_lock;
    name = torrent_info.name();
    
    const auto &fs = torrent_info.files();

    total_size = fs.total_size();
    num_pieces = fs.num_pieces();
    piece_length = fs.piece_length();
    if (num_pieces > 0) {
        last_piece_size = fs.piece_size(static_cast<libtorrent::piece_index_t>(fs.num_pieces() - 1));
    }

    this->files.clear();
    for (int i = 0; i < fs.num_files(); ++i) {
        auto &f = this->files.emplace_back();
        const auto index = static_cast<libtorrent::file_index_t>(i);
        f.index = i;
        f.name = std::string(fs.file_name(index));
        f.path = fs.file_path(index);
        f.offset = fs.file_offset(index);
        f.size = fs.file_size(index);
    }
}
} // namespace anilt
