
#ifndef TORRENT_HANDLE_T_H
#define TORRENT_HANDLE_T_H

#include "libtorrent/torrent.hpp"
#include "torrent_info_t.hpp"

namespace anilt {
extern "C" {

class torrent_handle_t final {
  public:
    unsigned int id = 0;

    [[nodiscard]] torrent_info_t *get_info_view() const { return info_.get(); }

    enum reload_file_result_t : unsigned int {
        kReloadFileSuccess = 0,
        kReloadFileNullHandle,
        kReloadFileNullFile,
    };

    reload_file_result_t reload_file();


    [[nodiscard]] bool is_valid() const;

    int get_state() const;

    // See event_listener_t::on_status_update
    void post_status_updates() const;
    void post_save_resume() const;
    void post_file_progress() const;

    void set_piece_deadline(int index, int deadline) const;

    void reset_piece_deadline(int32_t index) const;
    void clear_piece_deadlines() const;

    /// This function blocks.
    void set_peer_endgame(bool endgame) const;

    void add_tracker(const std::string &url, std::uint8_t tier = 0, std::uint8_t fail_limit = 0) const;

    void resume() const;

    // supports only 0, 1, 4, 7. See libtorrent::download_priority_t
    void ignore_all_files() const;

    void set_file_priority(int index, uint8_t priority) const;

    // Return empty string if handle is not valid
    std::string make_magnet_uri();

  private:
    friend class session_t;

    std::shared_ptr<libtorrent::torrent_handle> handle_;
    std::shared_ptr<torrent_info_t> info_;
    // std::mutex lock;
};
}
} // namespace anilt

#endif // TORRENT_HANDLE_T_H
