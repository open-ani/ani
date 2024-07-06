
#include "torrent_handle_t.hpp"

#include "global_lock.h"

namespace anilt {
torrent_handle_t::reload_file_result_t torrent_handle_t::reload_file() {
    function_printer_t _fp("torrent_handle_t::reload_file");
    const auto handle = delegate;
    if (!handle || !handle->is_valid()) {
        return kReloadFileNullHandle;
    }

    const auto lt_info = handle->torrent_file();
    if (!lt_info || !lt_info->is_valid()) {
        return kReloadFileNullFile;
    }
    this->info = std::make_shared<torrent_info_t>();
    this->info->parse(*lt_info);

    //
    // info.files.clear();
    // for (auto &file: lt_info->files()) {
    //     info.files.push_back({.index = file.path_index, .name = file.filename(), .path = file.path});
    // }
    return kReloadFileSuccess;
}

bool torrent_handle_t::post_status_updates() const {
    function_printer_t _fp("torrent_handle_t::post_status_updates");
    guard_global_lock;
    if (const auto handle = delegate; handle && handle->is_valid()) {
        handle->post_status(libtorrent::torrent_handle::query_pieces);
        return true;
    }
    return false;
}
void torrent_handle_t::reset_piece_deadline(const int index) const {
    function_printer_t _fp("torrent_handle_t::reset_piece_deadline");
    guard_global_lock;
    if (const auto handle = delegate; handle && handle->is_valid()) {
        handle->reset_piece_deadline(index);
    }
}
void torrent_handle_t::clear_piece_deadlines() const {
    function_printer_t _fp("torrent_handle_t::clear_piece_deadlines");
    guard_global_lock;
    if (const auto handle = delegate; handle && handle->is_valid()) {
        handle->clear_piece_deadlines();
    }
}

bool torrent_handle_t::set_piece_deadline(const int index, const int deadline) const {
    function_printer_t _fp("torrent_handle_t::set_piece_deadline");
    guard_global_lock;
    if (const auto handle = delegate; handle && handle->is_valid()) {
        handle->set_piece_deadline(index, deadline);
        return true;
    }
    return false;
}

void torrent_handle_t::request_piece_now(const int index) const {
    function_printer_t _fp("torrent_handle_t::request_piece_now");
    guard_global_lock;
    if (const auto handle = delegate; handle && handle->is_valid()) {
        std::vector<lt::peer_info> peers;
        handle->get_peer_info(peers);
        for (auto peer: peers) {
            libtorrent::peer_request req{};
            req.piece = index;
        }
    }
}
} // namespace anilt
