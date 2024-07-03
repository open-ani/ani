#include "anitorrent.h"

#include <fstream>
#include <iostream>

#include "libtorrent/alert_types.hpp"
#include "libtorrent/bencode.hpp"
#include "libtorrent/magnet_uri.hpp"
#include "libtorrent/session.hpp"


// #include "libtorrent/session.hpp"

namespace anilt {
    std::string lt_version() { return libtorrent::version(); }

    libtorrent::session *new_session(const char *user_agent) {
        using libtorrent::settings_pack;

        settings_pack settings;
        settings.set_str(settings_pack::user_agent, user_agent);
        settings.set_int(settings_pack::alert_mask,
                         libtorrent::alert_category::status | libtorrent::alert_category::error |
                                 libtorrent::alert_category::piece_progress |
                                 libtorrent::alert_category::upload |
                                 libtorrent::alert_category::stats);

        return new libtorrent::session(settings);
    }

    std::string compute_torrent_hash(const std::shared_ptr<const lt::torrent_info> &ti) {
        std::vector<char> torrent_data;
        bencode(std::back_inserter(torrent_data), ti->info_section());
        return std::string(torrent_data);
    }

    std::string
    fetch_magnet(libtorrent::session *s, const std::string &uri, const int timeout_seconds,
                             const std::string &save_path) {
        const auto fn = "[anilt::fetch_magnet]: ";
        std::cerr << fn << uri << std::endl;

        const std::string &magnet_uri = uri;

        lt::session &session = *s;

        lt::add_torrent_params params = lt::parse_magnet_uri(magnet_uri);
        params.save_path = save_path; // specify the save path

        // Check if the torrent is already in the session
        lt::sha1_hash info_hash = params.info_hashes.v1; // Assuming v1, adjust if using v2
        lt::torrent_handle existing_handle = session.find_torrent(info_hash);

        if (existing_handle.is_valid()) {
            std::cerr << fn << "Torrent already added. Using exisiting file" << std::endl;
            if (const auto file = existing_handle.torrent_file()) {
                return compute_torrent_hash(file);
            }
            std::cerr << fn << "existing_handle.torrent_file() returned null" << std::endl;
            return "";
        }

        const auto start_time = std::chrono::high_resolution_clock::now();

        lt::torrent_handle handle = session.add_torrent(params);

        while (true) {
            std::vector<lt::alert *> alerts;
            session.pop_alerts(&alerts);

            for (lt::alert *alert: alerts) {
                if (lt::alert_cast<lt::add_torrent_alert>(alert)) {
                } else if (auto md = lt::alert_cast<lt::metadata_received_alert>(alert)) {
                    std::cerr << fn << "Metadata received, computing hash and return" << std::endl;
                    return compute_torrent_hash(md->handle.torrent_file());
                }
            }


            const auto current_time = std::chrono::high_resolution_clock::now();
            if (std::chrono::duration_cast<std::chrono::seconds>(
                    current_time - start_time).count() >=
                timeout_seconds) {
                std::cerr << "Timeout reached." << std::endl;
                return "";
            }

            std::this_thread::sleep_for(std::chrono::milliseconds(200));
        }
    }
} // namespace anilt
