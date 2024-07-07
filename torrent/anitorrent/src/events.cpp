
#include "events.hpp"

#include <fstream>

#include "global_lock.h"
#include "libtorrent/alert_types.hpp"
#include "libtorrent/bencode.hpp"
#include "libtorrent/write_resume_data.hpp"

namespace anilt {


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
        listener.on_torrent_added(handle.id());
        return;
    }
    if (lt::alert_cast<lt::torrent_checked_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:torrent_checked_event_t");
        listener.on_checked(handle.id());
        return;
    }
    if (const auto a = lt::alert_cast<lt::save_resume_data_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:torrent_save_resume_data_event_t");
        const auto params = a->params;
        torrent_resume_data_t data;
        data.data_ = write_resume_data_buf(params);
        listener.on_save_resume_data(handle.id(), data);
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
    if (const auto a = lt::alert_cast<lt::file_completed_alert>(torrent_alert)) {
        function_printer_t _fp("call_listener:file_completed_alert");
        listener.on_file_completed(a->handle.id(), static_cast<int32_t>(a->index));
    }
}

static void writeVectorToFile(const std::vector<char> &data, const std::string &filePath) {
    // Open the file in binary mode
    std::ofstream outFile(filePath, std::ios::binary);

    // Check if the file was opened successfully
    if (!outFile) {
        std::cerr << "Error opening file for writing: " << filePath << std::endl;
        return;
    }

    // Write the contents of the vector to the file
    outFile.write(data.data(), static_cast<long>(data.size()));

    // Close the file
    outFile.close();
}

void torrent_resume_data_t::save_to_file(const std::string &path) const { writeVectorToFile(data_, path); }
} // namespace anilt
