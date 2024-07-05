
#ifndef SESSION_T_H
#define SESSION_T_H
#include <string>

#include "events.hpp"
#include "torrent_add_info_t.hpp"
#include "torrent_handle_t.hpp"
#include "torrent_info_t.hpp"

namespace anilt {
extern "C" {

class session_t final {
  public:
    void start(const char *user_agent);

    void resume() const;

    std::string fetch_magnet(std::string uri, int timeout_seconds, std::string save_path);

    /**
     * @param handle [out]
     * @param info torrent to downlad
     * @param save_path
     */
    bool start_download(torrent_handle_t &handle, torrent_add_info_t &info, const std::string &save_path) const;

    void release_handle(torrent_handle_t &handle) const;

    bool set_listener(event_listener_t *listener);

    bool remove_listener();

  private:
    std::shared_ptr<libtorrent::session> session_;
};
}
} // namespace anilt

#endif // SESSION_T_H
