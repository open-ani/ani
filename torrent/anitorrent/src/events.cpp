
#include "events.hpp"
#include "libtorrent/alert_types.hpp"

namespace anilt {


static void init_event(event_t &event, const lt::torrent_handle &handle) { event.handle_id = handle.id(); }

template<class Tp, class... Args>
static std::shared_ptr<Tp> make_event_shared(const lt::torrent_handle &handle, Args &&..._args) {
    const auto ptr = std::allocate_shared<Tp>(std::allocator<Tp>(), std::forward<Args>(_args)...);
    init_event(*ptr, handle);
    return ptr;
}

void call_listener(lt::alert *alert, libtorrent::session &session, event_listener_t &listener) {
    auto *torrent_alert = dynamic_cast<lt::torrent_alert *>(alert);
    if (!torrent_alert) {
        return;
    }
    const auto handle = torrent_alert->handle;
    if (lt::alert_cast<lt::add_torrent_alert>(torrent_alert)) {
        torrent_added_event_t event;
        init_event(event, handle);
        listener.on_torrent_added(event);
        return;
    }
    if (lt::alert_cast<lt::metadata_received_alert>(torrent_alert)) {
        metadata_received_event_t event;
        init_event(event, handle);
        listener.on_metadata_received(event);
        return;
    }
    if (lt::alert_cast<lt::torrent_checked_alert>(torrent_alert)) {
        listener.on_checked(handle.id());
        return;
    }
    if (lt::alert_cast<lt::save_resume_data_alert>(torrent_alert)) {
        torrent_save_resume_data_event_t event;
        init_event(event, handle);
        listener.on_save_resume_data(event);
        return;
    }
    if (const auto a = lt::alert_cast<lt::piece_finished_alert>(alert)) {
        listener.on_piece_finished(a->handle.id(), a->piece_index);
        return;
    }
    if (const auto a = lt::alert_cast<lt::block_downloading_alert>(alert)) {
        listener.on_block_downloading(a->handle.id(), a->piece_index, a->block_index);
        return;
    }
    if (const auto a = lt::alert_cast<lt::state_changed_alert>(alert)) {
        const auto state = static_cast<torrent_state_t>(a->state);
        listener.on_torrent_state_changed(a->handle.id(), state);
        return;
    }
    if (const auto a = lt::alert_cast<lt::state_update_alert>(alert)) {
        for (auto &torrent: a->status) {
            torrent_stats_t stats;
            stats.download_payload_rate = torrent.download_payload_rate;
            listener.on_status_update(torrent.handle.id(), stats);
        }
        return;
    }
}
} // namespace anilt
