#include <libtorrent/extensions.hpp>
#include <libtorrent/peer_connection_handle.hpp>
#include "plugin/peer_filter_plugin.h"

namespace anilt::plugin {
    bool peer_filter_plugin::on_handshake(lt::span<char const> d) {
        handle_peer(true);
        return lt::peer_plugin::on_handshake(d);
    }

    bool peer_filter_plugin::on_extension_handshake(lt::bdecode_node const &d) {
        handle_peer(true);
        return lt::peer_plugin::on_extension_handshake(d);
    }

    bool peer_filter_plugin::on_interested() {
        handle_peer();
        return lt::peer_plugin::on_interested();
    }

    bool peer_filter_plugin::on_not_interested() {
        handle_peer();
        return lt::peer_plugin::on_not_interested();
    }

    bool peer_filter_plugin::on_have(lt::piece_index_t p) {
        handle_peer();
        return lt::peer_plugin::on_have(p);
    }

    bool peer_filter_plugin::on_dont_have(lt::piece_index_t p) {
        handle_peer();
        return lt::peer_plugin::on_dont_have(p);
    }

    bool peer_filter_plugin::on_bitfield(lt::bitfield const &bitfield) {
        handle_peer();
        return lt::peer_plugin::on_bitfield(bitfield);
    }

    bool peer_filter_plugin::on_have_all() {
        handle_peer();
        return lt::peer_plugin::on_have_all();
    }

    bool peer_filter_plugin::on_have_none() {
        handle_peer();
        return lt::peer_plugin::on_have_none();
    }

    bool peer_filter_plugin::on_request(lt::peer_request const &r) {
        handle_peer();
        return lt::peer_plugin::on_request(r);
    }

    void peer_filter_plugin::handle_peer(bool handshake) {
        if (stop_filtering)
            return;

        lt::peer_info info;
        peer_connection_.get_peer_info(info);

        if (filter_(info, handshake, &stop_filtering))
            action_(peer_connection_);
    }

    std::shared_ptr<lt::peer_plugin> peer_action_plugin::new_connection(lt::peer_connection_handle const& handle) {
        return std::make_shared<peer_filter_plugin>(handle, filter_, action_);
    }
}