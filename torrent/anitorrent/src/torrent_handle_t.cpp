
#include "torrent_handle_t.hpp"

#include "global_lock.h"

namespace anilt {
torrent_handle_t::reload_file_result_t torrent_handle_t::reload_file() {
    function_printer_t _fp("torrent_handle_t::reload_file");
    const auto handle = handle_;
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

bool torrent_handle_t::is_valid() const {
    const auto handle = handle_;
    return handle && handle->is_valid();
}

void torrent_handle_t::post_status_updates() const {
    function_printer_t _fp("torrent_handle_t::post_status_updates");
    guard_global_lock;
        if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->post_status(libtorrent::torrent_handle::query_pieces);
    }
}

    void torrent_handle_t::reset_piece_deadline(const int32_t index) const {
    function_printer_t _fp("torrent_handle_t::reset_piece_deadline");
    guard_global_lock;
        if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->reset_piece_deadline(static_cast<libtorrent::piece_index_t>(index));
    }
    }
    void torrent_handle_t::clear_piece_deadlines() const {
        function_printer_t _fp("torrent_handle_t::clear_piece_deadlines");
    guard_global_lock;
    if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->clear_piece_deadlines();
    }
}

    void torrent_handle_t::set_piece_deadline(const int32_t index, const int deadline) const {
    function_printer_t _fp("torrent_handle_t::set_piece_deadline");
    guard_global_lock;
        if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->set_piece_deadline(libtorrent::piece_index_t(index), deadline);
        }
    }

    void torrent_handle_t::set_peer_endgame(const bool endgame) const {
        function_printer_t _fp("torrent_handle_t::set_peer_endgame");
        guard_global_lock;
        if (const auto handle = handle_; handle && handle->is_valid()) {
            std::vector<lt::peer_info> peers;
            handle->get_peer_info(peers);
            for (auto &peer: peers) {
                if (endgame) {
                    peer.flags |= libtorrent::peer_info::endgame_mode; // don't know if it works
                } else {
                    peer.flags &= ~libtorrent::peer_info::endgame_mode;
                }
            }
        }
    }

    void torrent_handle_t::add_tracker(const std::string &url, const std::uint8_t tier,
                                       const std::uint8_t fail_limit) const {
        function_printer_t _fp("torrent_handle_t::add_tracker");
        guard_global_lock;
        if (const auto handle = handle_; handle && handle->is_valid()) {
            libtorrent::announce_entry ae(url);
            ae.tier = tier;
            ae.fail_limit = fail_limit;
            ae.source = libtorrent::announce_entry::source_client;
            handle_->add_tracker(ae);
        }
    }
} // namespace anilt
