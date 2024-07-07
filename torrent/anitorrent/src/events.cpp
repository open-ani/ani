
#include "events.hpp"

#include "global_lock.h"
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
    // Non-torrent alerts

    if (const auto a = lt::alert_cast<lt::state_update_alert>(alert)) {
        function_printer_t _fp("call_listener:torrent_state_update_event_t");
        for (auto &torrent: a->status) {
            torrent_stats_t stats;
            stats.download_payload_rate = torrent.download_payload_rate;
            stats.upload_payload_rate = torrent.upload_payload_rate;
            stats.progress = torrent.progress;
            listener.on_status_update(torrent.handle.id(), stats);
        }
        return;
    }

    auto *torrent_alert = dynamic_cast<lt::torrent_alert *>(alert);
    if (!torrent_alert) {
        return;
    }
    const auto handle = torrent_alert->handle;
    if (!handle.is_valid())
        return;

    if (lt::alert_cast<lt::add_torrent_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:torrent_added_event_t");
        torrent_added_event_t event;
        init_event(event, handle);
        listener.on_torrent_added(event);
        return;
    }
    if (lt::alert_cast<lt::metadata_received_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:metadata_received_event_t");
        metadata_received_event_t event;
        init_event(event, handle);
        listener.on_metadata_received(event);
        return;
    }
    if (lt::alert_cast<lt::torrent_checked_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:torrent_checked_event_t");
        listener.on_checked(handle.id());
        return;
    }
    if (lt::alert_cast<lt::save_resume_data_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:torrent_save_resume_data_event_t");
        torrent_save_resume_data_event_t event;
        init_event(event, handle);
        listener.on_save_resume_data(event);
        return;
    }
    if (const auto a = lt::alert_cast<lt::piece_finished_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:piece_finished_event_t");
        listener.on_piece_finished(a->handle.id(), static_cast<int32_t>(a->piece_index));
        return;
    }
    if (const auto a = lt::alert_cast<lt::block_downloading_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:block_downloading_event_t");
        listener.on_block_downloading(a->handle.id(), static_cast<int32_t>(a->piece_index), a->block_index);
        return;
    }
    if (const auto a = lt::alert_cast<lt::state_changed_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:torrent_state_changed_event_t");
        const auto state = static_cast<torrent_state_t>(a->state);
        listener.on_torrent_state_changed(a->handle.id(), state);
        return;
    }
}
} // namespace anilt
