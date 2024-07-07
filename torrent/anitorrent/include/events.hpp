
#ifndef EVENTS_H
#define EVENTS_H


#include "libtorrent/session.hpp"
#include "torrent_add_info_t.hpp"


namespace anilt {
extern "C" {
// copied from libtorrent
enum torrent_state_t {
#if TORRENT_ABI_VERSION == 1
    // The torrent is in the queue for being checked. But there
    // currently is another torrent that are being checked.
    // This torrent will wait for its turn.
    queued_for_checking TORRENT_DEPRECATED_ENUM,
#else
    // internal
    unused_enum_for_backwards_compatibility,
#endif

    // The torrent has not started its download yet, and is
    // currently checking existing files.
    checking_files,

    // The torrent is trying to download metadata from peers.
    // This implies the ut_metadata extension is in use.
    downloading_metadata,

    // The torrent is being downloaded. This is the state
    // most torrents will be in most of the time. The progress
    // meter will tell how much of the files that has been
    // downloaded.
    downloading,

    // In this state the torrent has finished downloading but
    // still doesn't have the entire torrent. i.e. some pieces
    // are filtered and won't get downloaded.
    finished,

    // In this state the torrent has finished downloading and
    // is a pure seeder.
    seeding,

// If the torrent was started in full allocation mode, this
// indicates that the (disk) storage for the torrent is
// allocated.
#if TORRENT_ABI_VERSION == 1
    allocating TORRENT_DEPRECATED_ENUM,
#else
    unused_enum_for_backwards_compatibility_allocating,
#endif

    // The torrent is currently checking the fast resume data and
    // comparing it to the files on disk. This is typically
    // completed in a fraction of a second, but if you add a
    // large number of torrents at once, they will queue up.
    checking_resume_data
};

struct torrent_stats_t {
    int download_payload_rate = 0;
    int upload_payload_rate = 0;
    float progress = 0;
};

class event_listener_t;

struct torrent_resume_data_t {
    void save_to_file(const std::string &path) const;

  private:
    friend void call_listener(lt::alert *alert, libtorrent::session &session, event_listener_t &listener);
    std::vector<char> data_;
};

class event_listener_t { // inherited from Kotlin
  public:
    virtual ~event_listener_t() = default;

    virtual void on_checked(handle_id_t handle_id) {}
    virtual void on_torrent_added(handle_id_t handle_id) {}
    virtual void on_save_resume_data(handle_id_t handle_id, torrent_resume_data_t &data) {}
    virtual void on_torrent_state_changed(handle_id_t handle_id, torrent_state_t state) {}

    virtual void on_block_downloading(handle_id_t handle_id, int32_t piece_index, int block_index) {}

    virtual void on_piece_finished(handle_id_t handle_id, int32_t piece_index) {}

    // See torrent_handle_t::post_status_updates
    virtual void on_status_update(handle_id_t handle_id, torrent_stats_t &stats) {}

  private:
    friend class session_t;
    std::mutex lock_;
};


void call_listener(lt::alert *alert, libtorrent::session &session, event_listener_t &listener);
}
} // namespace anilt


#endif // EVENTS_H
