
#include "torrent_handle_t.hpp"

namespace anilt {
torrent_handle_t::reload_file_result_t torrent_handle_t::reload_file() {
    const auto handle = delegate;
    if (!handle) {
        return kReloadFileNullHandle;
    }

    const auto lt_info = handle->torrent_file();
    if (!lt_info) {
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

    bool torrent_handle_t::post_status_updates() {
        std::lock_guard guard(lock);
    const auto handle = delegate;
    if (!handle) {
        return false;
    }
    handle->post_status({});
    return true;
    }

    bool torrent_handle_t::set_piece_deadline(const int index, const int deadline) {
        std::lock_guard guard(lock);
        const auto handle = delegate;
        if (!handle) {
            return false;
        }
        handle->set_piece_deadline(index, deadline);
        return true;
}

    void torrent_handle_t::request_piece_now(int index) {
        std::lock_guard guard(lock);
        const auto handle = delegate;
        if (handle) {
            std::vector<lt::peer_info> peers;
            handle->get_peer_info(peers);
            for (auto peer: peers) {
                libtorrent::peer_request req{};
                req.piece = index;
            }
        }
    }
} // namespace anilt
