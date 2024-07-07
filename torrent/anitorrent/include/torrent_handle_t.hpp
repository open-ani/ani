
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


    [[nodiscard]] bool is_valid() const;

    // See event_listener_t::on_status_update
    void post_status_updates() const;


    void set_piece_deadline(int index, int deadline) const;

    void reset_piece_deadline(int32_t index) const;
    void clear_piece_deadlines() const;

    /// This function blocks.
    void set_peer_endgame(bool endgame) const;

    void add_tracker(const std::string &url, std::uint8_t tier = 0, std::uint8_t fail_limit = 0) const;

  private:
    friend class session_t;

    std::shared_ptr<libtorrent::torrent_handle> handle_;
    std::shared_ptr<torrent_info_t> info;
    // std::mutex lock;
};
}
} // namespace anilt

#endif // TORRENT_HANDLE_T_H
