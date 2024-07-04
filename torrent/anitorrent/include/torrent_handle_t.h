
#ifndef TORRENT_HANDLE_T_H
#define TORRENT_HANDLE_T_H

#include "libtorrent/torrent.hpp"
#include "torrent_info_t.h"

namespace anilt {
extern "C" {

class torrent_handle_t final {
  public:
    std::shared_ptr<libtorrent::torrent_handle> delegate;

    std::shared_ptr<torrent_info_t> info;
    unsigned int id = 0;

    [[nodiscard]] torrent_info_t *get_info_view() const { return info.get(); }

    bool reload_file();
};
}
} // namespace anilt

#endif // TORRENT_HANDLE_T_H
