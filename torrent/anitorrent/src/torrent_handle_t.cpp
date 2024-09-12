
#include "torrent_handle_t.hpp"

#include "global_lock.h"
#include "libtorrent/magnet_uri.hpp"

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
    this->info_ = std::make_shared<torrent_info_t>();
    this->info_->parse(*lt_info);

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
        handle->post_status();
    }
}
void torrent_handle_t::post_save_resume() const {
    function_printer_t _fp("torrent_handle_t::post_save_resume");
    guard_global_lock;
    if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->save_resume_data(libtorrent::torrent_handle::save_info_dict |
                                 libtorrent::torrent_handle::only_if_modified);
    }
}
void torrent_handle_t::post_file_progress() const {
    function_printer_t _fp("torrent_handle_t::post_file_progress");
    guard_global_lock;
    if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->post_file_progress(libtorrent::torrent_handle::piece_granularity);
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
        handle->piece_priority(libtorrent::piece_index_t(index), libtorrent::default_priority);
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
        handle->add_tracker(ae);
    }
}
void torrent_handle_t::resume() const {
    function_printer_t _fp("torrent_handle_t::add_tracker");
    guard_global_lock;
    if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->resume();
    }
}
void torrent_handle_t::ignore_all_files() const {
    function_printer_t _fp("torrent_handle_t::add_tracker");
    guard_global_lock;
    if (const auto handle = handle_; handle && handle->is_valid()) {
        if (const auto info = handle->torrent_file()) {
            const size_t num_files = info->num_files();
            std::vector<libtorrent::download_priority_t> priorities{num_files};
            for (int i = 0; i < num_files; ++i) {
                priorities.push_back(libtorrent::dont_download);
            }
            handle->prioritize_files(priorities);
        }
    }
}
void torrent_handle_t::set_file_priority(const int index, const uint8_t priority) const {
    function_printer_t _fp("torrent_handle_t::set_file_priority");
    guard_global_lock;
    if (const auto handle = handle_; handle && handle->is_valid()) {
        handle->file_priority(static_cast<libtorrent::file_index_t>(index),
                              static_cast<libtorrent::download_priority_t>(priority));
    }
}

void torrent_handle_t::get_peers(std::vector<anilt::peer_info_t> &peers) const {
    function_printer_t _fp("torrent_handle_t::get_peers");
    guard_global_lock;
    if (const auto handle = handle_; handle && handle->is_valid()) {
        std::vector<libtorrent::peer_info> raw_peers;
        handle->get_peer_info(raw_peers);
        for (const auto& rp: raw_peers) {
            peers.push_back(parse_peer_info(*handle, rp));
        }
    }
}


    std::string torrent_handle_t::make_magnet_uri() {
        if (const auto handle = handle_; handle && handle->is_valid()) {
            return libtorrent::make_magnet_uri(*handle);
        }
        return "";
    }

    int torrent_handle_t::get_state() const {
        if (const auto handle = handle_; handle && handle->is_valid()) {
            return handle->status().state;
        }
        return -1;
    }
} // namespace anilt
