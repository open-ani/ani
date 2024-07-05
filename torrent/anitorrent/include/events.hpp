
#ifndef EVENTS_H
#define EVENTS_H
#include "libtorrent/session.hpp"
#include "torrent_add_info_t.hpp"


namespace anilt {
extern "C" {

struct event_t {
    handle_id_t handle_id{};
};

struct torrent_add_event_t final : event_t {};
struct metadata_received_event_t final : event_t {};
struct torrent_save_resume_data_event_t final : event_t {};
struct torrent_finished_event_t final : event_t {};
}

std::shared_ptr<event_t> convert_event(lt::alert *alert);

extern "C" {
class event_listener_t { // inherited from Kotlin
  public:
    virtual ~event_listener_t() = default;
    virtual void on_event(event_t &event) const {}; // event is owned by the caller and is destroyed after this call
    virtual void on_piece_finished(handle_id_t handle_id, int piece_index) const {}
};
}
} // namespace anilt

#endif // EVENTS_H
