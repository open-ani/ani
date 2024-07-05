
#include "torrent_add_info_t.hpp"

#include "libtorrent/hex.hpp"
#include "libtorrent/torrent_info.hpp"

namespace anilt {
// bool torrent_add_info_t::parse(const std::string &encoded) {
//     libtorrent::error_code ec;
//     libtorrent::torrent_info info(encoded, ec);
//
//     auto &ti = *this;
//     if (ec) {
//         ti.error_code = ec.value();
//         ti.error_message = ec.message();
//         return false;
//     }
//     ti.name = info.name();
//     ti.infohash_hex = libtorrent::aux::to_hex(info.info_hash().to_string());
//     ti.file_count = info.num_files();
//     ti.info = std::make_shared<libtorrent::torrent_info>(info);
//     return true;
// }
} // namespace anilt
