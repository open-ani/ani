
#ifndef TORRENT_HANDLE_T_H
#define TORRENT_HANDLE_T_H

#include "libtorrent/torrent.hpp"
#include "torrent_info_t.hpp"

namespace anilt {
extern "C" {

class torrent_handle_t final {
  public:
    unsigned int id = 0;

    [[nodiscard]] torrent_info_t *get_info_view() const { return info.get(); }

    enum reload_file_result_t : unsigned int {
        kReloadFileSuccess = 0,
        kReloadFileNullHandle,
        kReloadFileNullFile,
    };

    reload_file_result_t reload_file();


    bool post_status_updates() const;


    bool set_piece_deadline(int index, int deadline) const;
    void reset_piece_deadline(int index) const;
    void clear_piece_deadlines() const;

    void request_piece_now(int index) const;

  private:
    friend class session_t;
    std::shared_ptr<libtorrent::torrent_handle> delegate;
    std::shared_ptr<torrent_info_t> info;
    // std::mutex lock;
};
}
} // namespace anilt

#endif // TORRENT_HANDLE_T_H
