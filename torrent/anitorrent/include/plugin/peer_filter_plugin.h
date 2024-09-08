#include <libtorrent/extensions.hpp>
#include <libtorrent/peer_connection_handle.hpp>

#ifndef ANI_PEER_FILTER_PLUGIN_H
#define ANI_PEER_FILTER_PLUGIN_H

namespace anilt::plugin {
    using filter_function = std::function<bool(const lt::peer_info&, bool, bool*)>;
    using action_function = std::function<void(lt::peer_connection_handle)>;

    class peer_filter_plugin final : public lt::peer_plugin {
    public:
        peer_filter_plugin(lt::peer_connection_handle p, filter_function filter, action_function action)
                : m_peer_connection(p)
                , m_filter(std::move(filter))
                , m_action(std::move(action))
        {}

        bool on_handshake(lt::span<char const> d) override;

        bool on_extension_handshake(lt::bdecode_node const& d) override;

        bool on_interested() override;

        bool on_not_interested() override;

        bool on_have(lt::piece_index_t p) override;

        bool on_dont_have(lt::piece_index_t p) override;

        bool on_bitfield(lt::bitfield const& bitfield) override;

        bool on_have_all() override;

        bool on_have_none() override;

        bool on_request(lt::peer_request const& r) override;

    protected:
        void handle_peer(bool handshake = false);

    private:
        lt::peer_connection_handle m_peer_connection;

        filter_function m_filter;
        action_function m_action;

        bool m_stop_filtering = false;
    };


    class peer_action_plugin : public lt::torrent_plugin {
    public:
        peer_action_plugin(filter_function filter, action_function action)
                : m_filter(std::move(filter))
                , m_action(std::move(action))
        {}

        std::shared_ptr<lt::peer_plugin> new_connection(lt::peer_connection_handle const& handle);

    private:
        filter_function m_filter;
        action_function m_action;
    };
} // namespace anilt::plugin


#endif //ANI_PEER_FILTER_PLUGIN_H
