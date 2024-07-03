#ifndef ANILT_H
#define ANILT_H

#include "libtorrent/session.hpp"

namespace anilt {
extern "C" {
std::string lt_version();

struct stats_t {};

// allocate from Java
struct torrent_info_t {
        std::string name;
        std::string infohash_hex;
        int file_count = 0;

        int error_code = 0;
        std::string error_message;

        std::shared_ptr<libtorrent::torrent_info> info;
};

bool torrent_info_parse(torrent_info_t &ti, const std::string &encoded);

void torrent_info_release(torrent_info_t &ti);


// allocate from Java
struct torrent_handle_t {
        std::shared_ptr<libtorrent::torrent_handle> delegate;
};

libtorrent::session *new_session(const char *user_agent);

// stats get_stats(libtorrent::session &s) {}
std::string fetch_magnet(libtorrent::session *s, const std::string &uri, int timeout_seconds,
                         const std::string &save_path);

bool start_download(libtorrent::session *s, torrent_handle_t &handle, const torrent_info_t &info,
                    const std::string &save_path);

} // namespace anilt
} // namespace anilt

#endif // ANILT_H
