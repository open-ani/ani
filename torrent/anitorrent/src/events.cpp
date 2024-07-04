
#include "events.h"
#include "libtorrent/alert_types.hpp"

namespace anilt {


static void init_event(event_t &event, const lt::torrent_handle &handle) { event.handle_id = handle.id(); }

template<class Tp, class... Args>
static std::shared_ptr<Tp> make_event_shared(const lt::torrent_handle &handle, Args &&..._args) {
    const auto ptr = std::allocate_shared<Tp>(std::allocator<Tp>(), std::forward<Args>(_args)...);
    init_event(*ptr, handle);
    return ptr;
}


std::shared_ptr<event_t> convert_event(lt::alert *alert) {
    auto *torrent_alert = lt::alert_cast<lt::torrent_alert>(alert);
    if (!torrent_alert) {
        return nullptr;
    }
    const auto handle = torrent_alert->handle;
    const auto name = handle.status().name;
    if (lt::alert_cast<lt::add_torrent_alert>(torrent_alert)) {
        return make_event_shared<torrent_add_event_t>(handle);
    }
    if (lt::alert_cast<lt::metadata_received_alert>(torrent_alert)) {
        return make_event_shared<metadata_received_event_t>(handle);
    }
    if (lt::alert_cast<lt::save_resume_data_alert>(torrent_alert)) {
        return make_event_shared<torrent_save_resume_data_event_t>(handle);
    }
    if (lt::alert_cast<lt::torrent_finished_alert>(torrent_alert)) {
        return make_event_shared<torrent_finished_event_t>(handle);
    }
    return nullptr;
}
} // namespace anilt
