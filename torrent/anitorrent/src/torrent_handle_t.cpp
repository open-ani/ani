
#include "torrent_handle_t.h"

namespace anilt {
bool torrent_handle_t::reload_file() {
    const auto handle = delegate;
    if (!handle) {
        return false;
    }

    const auto lt_info = handle->torrent_file();
    if (!lt_info) {
        return false;
    }
    this->info = std::make_shared<torrent_info_t>();
    auto &info = *this->info;
    info.parse(*lt_info);
    
    //
    // info.files.clear();
    // for (auto &file: lt_info->files()) {
    //     info.files.push_back({.index = file.path_index, .name = file.filename(), .path = file.path});
    // }
    return true;
}
} // namespace anilt
