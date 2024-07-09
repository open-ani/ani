#pragma once
#include <utility>

#include "libtorrent/torrent_info.hpp"

namespace anilt {
extern "C" {

struct torrent_file_t final {
    unsigned int index{};
    std::string name;
    std::string path;
    size_t offset{};
    size_t size{};

    torrent_file_t() = default;
};

// 一个种子的信息
class torrent_info_t {
  public:
    torrent_info_t() = default;

    int64_t total_size{};

    int num_pieces{};
    int piece_length{};
    int last_piece_size{};


    std::vector<torrent_file_t> files{};

    [[nodiscard]] unsigned long file_count() const { return files.size(); }
    [[nodiscard]] torrent_file_t *file_at(const int index) {
        if (index <= -1 || index >= files.size())
            return nullptr;
        return &files.at(index);
    }


  private:
    friend class torrent_handle_t;
    void parse(const libtorrent::torrent_info &torrent_info);
};
}
} // namespace anilt
