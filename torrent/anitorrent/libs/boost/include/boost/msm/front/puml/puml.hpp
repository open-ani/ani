// Copyright 2024 Christophe Henry
// henry UNDERSCORE christophe AT hotmail DOT com
// This is an extended version of the state machine available in the boost::mpl library
// Distributed under the same license as the original.
// Copyright for the original version:
// Copyright 2005 David Abrahams and Aleksey Gurtovoy. Distributed
// under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_MSM_FRONT_PUML_COMMON_H
#define BOOST_MSM_FRONT_PUML_COMMON_H

#include <cstdint>
#include <string>
#include <string_view>
#include <vector>
#include <boost/any.hpp>
#include <boost/fusion/mpl.hpp>
#include <boost/fusion/include/as_vector.hpp>
#include <boost/fusion/container/vector.hpp>
#include <boost/fusion/include/insert_range.hpp>
#include <boost/fusion/include/at_c.hpp>
#include <boost/fusion/include/for_each.hpp>
#include <boost/fusion/include/make_vector.hpp>

#include <boost/mpl/size.hpp>

#include <boost/msm/front/states.hpp>
// functors
#include <boost/msm/front/functor_row.hpp>
#include <boost/msm/front/operator.hpp>

namespace boost::msm::front::puml
{
namespace detail {
    template <class T1, class T2>
    struct pair_type
    {
        using first = T1;
        using second = T2;
    };
}

    template<typename T>
    struct convert_to_msm_names
    {
        using type = T;
    };


    template <std::uint32_t hash, 
              class Flags = boost::fusion::vector0<>,
              class Entries = boost::fusion::vector0<>,
              class Exits = boost::fusion::vector0<>>
    struct State 
    {
        using generated_type = State<hash>;
        using flag_list = boost::fusion::vector0<>;
        using entries = Entries;
        using exits = Exits;
        using internal_flag_list = Flags;

        template <class Event, class FSM>
        void on_entry(Event& evt, FSM& fsm) 
        {
            boost::fusion::for_each(Entries{}, 
                [&](auto action_guard_pair) 
                {
                    if constexpr (
                        std::is_same_v<typename decltype(action_guard_pair)::second,boost::msm::front::none>)
                    {
                        typename decltype(action_guard_pair)::first{}(evt, fsm, *this, *this);
                    }
                    else
                    {
                        if (typename decltype(action_guard_pair)::second{}(evt, fsm, *this, *this))
                        {
                            typename decltype(action_guard_pair)::first{}(evt, fsm, *this, *this);
                        }
                    }
                });
        }
        template <class Event, class FSM>
        void on_exit(Event& evt, FSM& fsm) 
        {
            boost::fusion::for_each(Exits{},
                [&](auto action_guard_pair)
                {
                    if constexpr (
                        std::is_same_v<typename decltype(action_guard_pair)::second, boost::msm::front::none>)
                    {
                        typename decltype(action_guard_pair)::first{}(evt, fsm, *this, *this);
                    }
                    else
                    {
                        if (typename decltype(action_guard_pair)::second{}(evt, fsm, *this, *this))
                        {
                            typename decltype(action_guard_pair)::first{}(evt, fsm, *this, *this);
                        }
                    }
                });
        }
        // typedefs added for front::state compatibility
        typedef ::boost::mpl::vector<>  internal_transition_table;
        typedef ::boost::fusion::vector<>  internal_transition_table11;
        typedef ::boost::fusion::vector<>  transition_table;
        typedef ::boost::fusion::vector0<>       deferred_events;

    };
    template <std::uint32_t hash>
    struct Event
    {

    };
    template <std::uint32_t hash>
    struct Action
    {

    };
    template <std::uint32_t hash>
    struct Guard
    {

    };
    template <std::uint32_t hash>
    struct Flag
    {

    };


    namespace detail {
        // CRC32 Table (zlib polynomial)
        static constexpr std::uint32_t crc_table[256] =
        {
            0x00000000L, 0x77073096L, 0xee0e612cL, 0x990951baL, 0x076dc419L,
            0x706af48fL, 0xe963a535L, 0x9e6495a3L, 0x0edb8832L, 0x79dcb8a4L,
            0xe0d5e91eL, 0x97d2d988L, 0x09b64c2bL, 0x7eb17cbdL, 0xe7b82d07L,
            0x90bf1d91L, 0x1db71064L, 0x6ab020f2L, 0xf3b97148L, 0x84be41deL,
            0x1adad47dL, 0x6ddde4ebL, 0xf4d4b551L, 0x83d385c7L, 0x136c9856L,
            0x646ba8c0L, 0xfd62f97aL, 0x8a65c9ecL, 0x14015c4fL, 0x63066cd9L,
            0xfa0f3d63L, 0x8d080df5L, 0x3b6e20c8L, 0x4c69105eL, 0xd56041e4L,
            0xa2677172L, 0x3c03e4d1L, 0x4b04d447L, 0xd20d85fdL, 0xa50ab56bL,
            0x35b5a8faL, 0x42b2986cL, 0xdbbbc9d6L, 0xacbcf940L, 0x32d86ce3L,
            0x45df5c75L, 0xdcd60dcfL, 0xabd13d59L, 0x26d930acL, 0x51de003aL,
            0xc8d75180L, 0xbfd06116L, 0x21b4f4b5L, 0x56b3c423L, 0xcfba9599L,
            0xb8bda50fL, 0x2802b89eL, 0x5f058808L, 0xc60cd9b2L, 0xb10be924L,
            0x2f6f7c87L, 0x58684c11L, 0xc1611dabL, 0xb6662d3dL, 0x76dc4190L,
            0x01db7106L, 0x98d220bcL, 0xefd5102aL, 0x71b18589L, 0x06b6b51fL,
            0x9fbfe4a5L, 0xe8b8d433L, 0x7807c9a2L, 0x0f00f934L, 0x9609a88eL,
            0xe10e9818L, 0x7f6a0dbbL, 0x086d3d2dL, 0x91646c97L, 0xe6635c01L,
            0x6b6b51f4L, 0x1c6c6162L, 0x856530d8L, 0xf262004eL, 0x6c0695edL,
            0x1b01a57bL, 0x8208f4c1L, 0xf50fc457L, 0x65b0d9c6L, 0x12b7e950L,
            0x8bbeb8eaL, 0xfcb9887cL, 0x62dd1ddfL, 0x15da2d49L, 0x8cd37cf3L,
            0xfbd44c65L, 0x4db26158L, 0x3ab551ceL, 0xa3bc0074L, 0xd4bb30e2L,
            0x4adfa541L, 0x3dd895d7L, 0xa4d1c46dL, 0xd3d6f4fbL, 0x4369e96aL,
            0x346ed9fcL, 0xad678846L, 0xda60b8d0L, 0x44042d73L, 0x33031de5L,
            0xaa0a4c5fL, 0xdd0d7cc9L, 0x5005713cL, 0x270241aaL, 0xbe0b1010L,
            0xc90c2086L, 0x5768b525L, 0x206f85b3L, 0xb966d409L, 0xce61e49fL,
            0x5edef90eL, 0x29d9c998L, 0xb0d09822L, 0xc7d7a8b4L, 0x59b33d17L,
            0x2eb40d81L, 0xb7bd5c3bL, 0xc0ba6cadL, 0xedb88320L, 0x9abfb3b6L,
            0x03b6e20cL, 0x74b1d29aL, 0xead54739L, 0x9dd277afL, 0x04db2615L,
            0x73dc1683L, 0xe3630b12L, 0x94643b84L, 0x0d6d6a3eL, 0x7a6a5aa8L,
            0xe40ecf0bL, 0x9309ff9dL, 0x0a00ae27L, 0x7d079eb1L, 0xf00f9344L,
            0x8708a3d2L, 0x1e01f268L, 0x6906c2feL, 0xf762575dL, 0x806567cbL,
            0x196c3671L, 0x6e6b06e7L, 0xfed41b76L, 0x89d32be0L, 0x10da7a5aL,
            0x67dd4accL, 0xf9b9df6fL, 0x8ebeeff9L, 0x17b7be43L, 0x60b08ed5L,
            0xd6d6a3e8L, 0xa1d1937eL, 0x38d8c2c4L, 0x4fdff252L, 0xd1bb67f1L,
            0xa6bc5767L, 0x3fb506ddL, 0x48b2364bL, 0xd80d2bdaL, 0xaf0a1b4cL,
            0x36034af6L, 0x41047a60L, 0xdf60efc3L, 0xa867df55L, 0x316e8eefL,
            0x4669be79L, 0xcb61b38cL, 0xbc66831aL, 0x256fd2a0L, 0x5268e236L,
            0xcc0c7795L, 0xbb0b4703L, 0x220216b9L, 0x5505262fL, 0xc5ba3bbeL,
            0xb2bd0b28L, 0x2bb45a92L, 0x5cb36a04L, 0xc2d7ffa7L, 0xb5d0cf31L,
            0x2cd99e8bL, 0x5bdeae1dL, 0x9b64c2b0L, 0xec63f226L, 0x756aa39cL,
            0x026d930aL, 0x9c0906a9L, 0xeb0e363fL, 0x72076785L, 0x05005713L,
            0x95bf4a82L, 0xe2b87a14L, 0x7bb12baeL, 0x0cb61b38L, 0x92d28e9bL,
            0xe5d5be0dL, 0x7cdcefb7L, 0x0bdbdf21L, 0x86d3d2d4L, 0xf1d4e242L,
            0x68ddb3f8L, 0x1fda836eL, 0x81be16cdL, 0xf6b9265bL, 0x6fb077e1L,
            0x18b74777L, 0x88085ae6L, 0xff0f6a70L, 0x66063bcaL, 0x11010b5cL,
            0x8f659effL, 0xf862ae69L, 0x616bffd3L, 0x166ccf45L, 0xa00ae278L,
            0xd70dd2eeL, 0x4e048354L, 0x3903b3c2L, 0xa7672661L, 0xd06016f7L,
            0x4969474dL, 0x3e6e77dbL, 0xaed16a4aL, 0xd9d65adcL, 0x40df0b66L,
            0x37d83bf0L, 0xa9bcae53L, 0xdebb9ec5L, 0x47b2cf7fL, 0x30b5ffe9L,
            0xbdbdf21cL, 0xcabac28aL, 0x53b39330L, 0x24b4a3a6L, 0xbad03605L,
            0xcdd70693L, 0x54de5729L, 0x23d967bfL, 0xb3667a2eL, 0xc4614ab8L,
            0x5d681b02L, 0x2a6f2b94L, 0xb40bbe37L, 0xc30c8ea1L, 0x5a05df1bL,
            0x2d02ef8dL
        };
        constexpr std::uint32_t crc32(std::string_view str)
        {
            std::uint32_t crc = 0xffffffff;
            for (auto c : str)
                crc = (crc >> 8) ^ boost::msm::front::puml::detail::crc_table[(crc ^ c) & 0xff];
            return crc ^ 0xffffffff;
        }

        // removes training spaces or -
        constexpr std::string_view cleanup_token(const std::string_view& str)
        {
            auto first_not_whitespace = str.find_first_not_of("- \t");
            auto last_not_whitespace = str.find_last_not_of("- \t");
            if (first_not_whitespace != std::string::npos && last_not_whitespace != std::string::npos)
            {
                //return std::string(str, first_not_whitespace, last_not_whitespace - first_not_whitespace + 1);
                return str.substr(first_not_whitespace, last_not_whitespace - first_not_whitespace + 1);
            }
            else
            {
                return std::string_view{};
            }
        }

        struct Transition
        {
            std::string_view source;
            std::string_view target;
            std::string_view event;
            std::string_view guard;
            std::string_view action;
        };

        // finds guards [Guard]
        // requires all blanks to have been removed
        constexpr boost::msm::front::puml::detail::Transition
            parse_guards(std::string_view part)
        {
            boost::msm::front::puml::detail::Transition res;
            auto start_pos = part.find("[");
            auto end_pos = part.find("]");

            if (start_pos != std::string::npos && end_pos != std::string::npos)
            {
                ++start_pos;
                res.guard = boost::msm::front::puml::detail::cleanup_token(part.substr(start_pos, end_pos - start_pos));
            }
            return res;
        }


        // requires all blanks to have been removed
        constexpr boost::msm::front::puml::detail::Transition parse_row_right(std::string_view part)
        {
            auto action_pos = part.find("/");
            auto guard_pos = part.find("[");
            auto evt_pos = part.find(":");
            auto internal_pos = part.find("-");
            bool is_internal_transition =
                internal_pos != std::string::npos && internal_pos > evt_pos && internal_pos <= action_pos && internal_pos <= guard_pos;
            auto start_event_name_pos = (internal_pos == std::string::npos) ? evt_pos : internal_pos;

            boost::msm::front::puml::detail::Transition res;

            // target is until : or end of string if not provided
            if (evt_pos != std::string::npos && !is_internal_transition)
            {
                res.target = boost::msm::front::puml::detail::cleanup_token(part.substr(0, evt_pos));
            }
            else if (!is_internal_transition)
            {
                res.target = boost::msm::front::puml::detail::cleanup_token(part);
            }
            // event is between : and / or [, whatever comes first
            if (action_pos == std::string::npos && guard_pos == std::string::npos)
            {
                res.event = boost::msm::front::puml::detail::cleanup_token(part.substr(start_event_name_pos + 1));
            }
            else
            {
                auto length = std::min(action_pos, guard_pos) > 1 + start_event_name_pos ?
                    std::min(action_pos, guard_pos) - 1 - start_event_name_pos
                    : 0;
                res.event = boost::msm::front::puml::detail::cleanup_token(part.substr(start_event_name_pos + 1, length));
            }

            // handle different guard / action cases
            if (action_pos != std::string::npos && guard_pos != std::string::npos)
            {
                res.action = boost::msm::front::puml::detail::cleanup_token(part.substr(action_pos + 1, guard_pos - 1 - action_pos));
            }
            else if (action_pos != std::string::npos)
            {
                // we have an action => target until /
                res.action = boost::msm::front::puml::detail::cleanup_token(part.substr(action_pos + 1));
            }
            // handle guards
            res.guard = boost::msm::front::puml::detail::parse_guards(
                boost::msm::front::puml::detail::cleanup_token(part)).guard;
            return res;
        }

        constexpr boost::msm::front::puml::detail::Transition parse_row(std::string_view row)
        {
            auto arrow_pos = row.find("->");
            auto puml_event_pos = row.find(":");
            auto left = row.substr(0, arrow_pos);
            auto right = row.substr(arrow_pos + 2);

            if (puml_event_pos != std::string::npos)
            {
                return boost::msm::front::puml::detail::Transition{
                            boost::msm::front::puml::detail::cleanup_token(left),
                            boost::msm::front::puml::detail::parse_row_right(right).target,
                            boost::msm::front::puml::detail::parse_row_right(right).event,
                            boost::msm::front::puml::detail::parse_row_right(right).guard,
                            boost::msm::front::puml::detail::parse_row_right(right).action };
            }
            else if (arrow_pos != std::string::npos)
            {
                // simple source -> target form
                return boost::msm::front::puml::detail::Transition{
                    boost::msm::front::puml::detail::cleanup_token(left),
                    boost::msm::front::puml::detail::cleanup_token(right),
                    std::string_view{},
                    std::string_view{},
                    std::string_view{}
                };
            }
            return boost::msm::front::puml::detail::Transition{};
        }


        constexpr int count_transitions(std::string_view s)
        {
            //s = reduce(s, "");
            int occurrences = 0;
            std::string::size_type pos = 0;
            auto target = "->";
            while ((pos = s.find(target, pos)) != std::string::npos)
            {
                ++occurrences;
                pos += 2;
            }
            return occurrences;
        };
        constexpr int count_inits(std::string_view s, std::size_t occurrences=0)
        {
            auto target = "[*]";
            auto star_pos = s.find(target);
            auto endl_after_pos = s.find("\n", star_pos);
            auto arrow_after_pos = s.find("->", star_pos);
            if (star_pos != std::string::npos && 
                star_pos < arrow_after_pos && 
                arrow_after_pos < endl_after_pos)
            {
                return count_inits(s.substr(endl_after_pos), occurrences + 1);
            }
            return occurrences;
        };
        constexpr int count_actions(std::string_view s)
        {
            int occurrences = 0;
            if (!s.empty())
            {
                occurrences = 1;
            }
            std::string::size_type pos = 0;
            auto target = ",";
            while ((pos = s.find(target, pos)) != std::string::npos)
            {
                ++occurrences;
                pos += 1;
            }
            return occurrences;
        };

        template <int t>
        constexpr
            auto parse_stt(std::string_view stt)
        {
            auto prev_pos = std::string::size_type(0);
            auto pos = std::string::size_type(0);
            auto trans_cpt = 0;
            do
            {
                pos = stt.find("\n", prev_pos);
                auto transition_symbol = stt.find("->", prev_pos);
                auto init_symbol = stt.find("[*]", prev_pos);
                if (init_symbol < pos || transition_symbol >= pos)
                {
                    prev_pos = pos + 1;
                }
                else
                {
                    if (trans_cpt == t)
                    {
                        return boost::msm::front::puml::detail::parse_row(stt.substr(prev_pos, pos - prev_pos));
                    }
                    prev_pos = pos + 1;
                    ++trans_cpt;
                }
            } while (pos != std::string::npos);
            // should not happen
            return boost::msm::front::puml::detail::Transition{};
        }

        template <int a>
        constexpr auto parse_action(std::string_view actions)
        {
            //actions = reduce(actions, "");
            auto prev_pos = std::string::size_type(0);
            auto pos = std::string::size_type(0);
            auto action_cpt = 0;
            do
            {
                pos = actions.find(",", prev_pos);
                if (action_cpt == a)
                {
                    return boost::msm::front::puml::detail::cleanup_token(actions.substr(prev_pos, pos - prev_pos));
                }
                ++action_cpt;
                prev_pos = pos + 1;
            } while (pos != std::string::npos);
            // should not happen
            return std::string_view{};
        }


        template <int t>
        constexpr
            auto parse_inits(std::string_view stt)
        {
            //stt = reduce(stt, "");

            auto prev_pos = std::string::size_type(0);
            auto pos = std::string::size_type(0);
            auto trans_cpt = 0;
            do
            {
                pos = stt.find("\n", prev_pos);
                auto init_symbol = stt.find("[*]", prev_pos);
                if (pos > init_symbol)
                {
                    auto init_symbol2 = stt.find("->", init_symbol);
                    if (pos > init_symbol2)
                    {
                        if (trans_cpt == t)
                        {
                            return cleanup_token(stt.substr(init_symbol2 + 2, pos - init_symbol2 - 2));
                        }
                        ++trans_cpt;
                    }
                }
                prev_pos = pos + 1;
            } while (pos != std::string::npos);
            // should not happen
            return std::string_view{};
        }        
    } //namespace detail

    constexpr std::uint32_t by_name(std::string_view str)
    {
        return boost::msm::front::puml::detail::crc32(str) ^ 0xFFFFFFFF;
    }

    // specializations
    template<>
    struct convert_to_msm_names<State<by_name("")>>
    {
        using type = boost::msm::front::none;
    };
    template<>
    struct convert_to_msm_names<Event< by_name("")>>
    {
        using type = boost::msm::front::none;
    };
    template<>
    struct convert_to_msm_names<Action< by_name("")>>
    {
        using type = boost::msm::front::none;
    };
    template<>
    struct convert_to_msm_names<Guard< by_name("")>>
    {
        using type = boost::msm::front::none;
    };
    template<>
    struct convert_to_msm_names<Action< by_name("defer")>>
    {
        using type = boost::msm::front::Defer;
    };
    template<>
    struct convert_to_msm_names<Event< by_name("*")>>
    {
        using type = boost::any;
    };

    namespace detail
    {
        template <class Func>
        constexpr auto parse_guard_simple(Func guard_func)
        {
            constexpr auto and_pos = guard_func().find("&&");
            constexpr auto or_pos = guard_func().find("||");
            constexpr auto not_pos = guard_func().find("!");
            constexpr auto parens_begin_pos = guard_func().find("(");
            constexpr auto parens_end_pos = guard_func().find(")");
            constexpr auto last_and_pos = guard_func().find("&&", parens_end_pos);
            constexpr auto last_or_pos = guard_func().find("||", parens_end_pos);

            // check for operator of the lesser precedence after end parens
            if constexpr (parens_begin_pos != std::string::npos && parens_end_pos != std::string::npos &&
                last_or_pos != std::string::npos && parens_end_pos < last_or_pos)
            {
                return boost::msm::front::Or_<
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(0, last_or_pos)); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(last_or_pos + 2)); })) > {};
            }
            else if constexpr (parens_begin_pos != std::string::npos && parens_end_pos != std::string::npos &&
                last_and_pos != std::string::npos && parens_end_pos < last_and_pos)
            {
                return boost::msm::front::And_<
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(0, last_and_pos)); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(last_and_pos + 2)); })) > {};
            }
            else if  constexpr (parens_begin_pos != std::string::npos && parens_end_pos != std::string::npos &&
                or_pos != std::string::npos && or_pos < and_pos && or_pos < parens_begin_pos)
            {
                return boost::msm::front::Or_<
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(0, or_pos)); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(or_pos + 2)); })) > {};
            }
            else if  constexpr (parens_begin_pos != std::string::npos && parens_end_pos != std::string::npos &&
                and_pos != std::string::npos && and_pos < or_pos && and_pos < parens_begin_pos)
            {
                return boost::msm::front::And_<
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(0, and_pos)); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(and_pos + 2)); })) > {};
            }
            else if  constexpr (parens_begin_pos != std::string::npos && parens_end_pos != std::string::npos &&
                not_pos != std::string::npos && not_pos < parens_begin_pos)
            {
                return boost::msm::front::Not_<decltype(boost::msm::front::puml::detail::parse_guard_simple(
                    [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(not_pos + 1)); })) > {};
            }
            else if constexpr (parens_begin_pos != std::string::npos && parens_end_pos != std::string::npos)
            {
                return boost::msm::front::puml::detail::parse_guard_simple(
                    [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(parens_begin_pos + 1, parens_end_pos - (parens_begin_pos + 1))); });
            }
            else if constexpr (and_pos == std::string::npos && or_pos == std::string::npos && not_pos == std::string::npos)
            {
                return typename boost::msm::front::puml::convert_to_msm_names < Guard <by_name(guard_func())> >::type{};
            }
            // at least one operator, break at pos of the lesser precedence
            else if constexpr (or_pos != std::string::npos)
            {
                return boost::msm::front::Or_<
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(0, or_pos)); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(or_pos + 2)); })) > {};
            }
            else if constexpr (and_pos != std::string::npos)
            {
                return boost::msm::front::And_<
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(0, and_pos)); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_simple(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(and_pos + 2)); })) > {};
            }
            else
            {
                return boost::msm::front::Not_<decltype(boost::msm::front::puml::detail::parse_guard_simple(
                    [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(not_pos + 1)); })) > {};
            }
        }
        template <class Func>
        constexpr auto parse_guard_advanced(Func guard_func)
        {
            constexpr auto and_pos = guard_func().find("And(");
            constexpr auto or_pos = guard_func().find("Or(");
            constexpr auto not_pos = guard_func().find("Not(");

            if constexpr (and_pos == std::string::npos && or_pos == std::string::npos && not_pos == std::string::npos)
            {
                return typename boost::msm::front::puml::convert_to_msm_names < Guard <by_name(guard_func())> >::type{};
            }
            // at least one operator, break at pos of the lesser precedence
            else if constexpr (or_pos != std::string::npos)
            {
                constexpr auto comma_pos = guard_func().find(",", or_pos);
                constexpr auto endparens_pos = guard_func().find(")", or_pos);
                return boost::msm::front::Or_<
                    decltype(boost::msm::front::puml::detail::parse_guard_advanced(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(or_pos + 3, comma_pos - (or_pos + 3))); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_advanced(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(comma_pos + 1, endparens_pos - (comma_pos + 1))); }))
                > {};
            }
            else if constexpr (and_pos != std::string::npos)
            {
                constexpr auto comma_pos = guard_func().find(",", and_pos);
                constexpr auto endparens_pos = guard_func().find(")", and_pos);
                return boost::msm::front::And_<
                    decltype(boost::msm::front::puml::detail::parse_guard_advanced(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(and_pos + 4, comma_pos - (and_pos + 4))); })),
                    decltype(boost::msm::front::puml::detail::parse_guard_advanced(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(comma_pos + 1, endparens_pos - (comma_pos + 1))); }))
                > {};
            }
            else
            {
                constexpr auto endparens_pos = guard_func().find(")", not_pos);
                return boost::msm::front::Not_<
                    decltype(boost::msm::front::puml::detail::parse_guard_advanced(
                        [=]() {return boost::msm::front::puml::detail::cleanup_token(guard_func().substr(not_pos + 4, endparens_pos - (not_pos + 4))); }))
                > {};
            }
        }

        template <class Func>
        constexpr auto parse_guard(Func guard_func)
        {
            if constexpr (guard_func().find("And(") != std::string::npos ||
                guard_func().find("Or(") != std::string::npos ||
                guard_func().find("Not(") != std::string::npos)
            {
                return boost::msm::front::puml::detail::parse_guard_advanced(guard_func);
            }
            else
            {
                return boost::msm::front::puml::detail::parse_guard_simple(guard_func);
            }
        }

        constexpr int count_terminates(std::string_view s, std::size_t occurrences = 0)
        {
            if (s.empty())
                return occurrences;
            auto star_pos = s.find("[*]");
            auto arrow_pos = s.rfind("->", star_pos);
            auto endl_before_pos = s.rfind("\n", star_pos);
            if (star_pos != std::string::npos &&
                arrow_pos != std::string::npos &&
                arrow_pos > endl_before_pos )
            {
                return count_terminates(s.substr(star_pos + 3), occurrences + 1);
            }
            else if(star_pos != std::string::npos)
            {
                return count_terminates(s.substr(star_pos + 3), occurrences);
            }
            return occurrences;
        };

        template <class Func, class T>
        constexpr auto parse_terminate(Func stt, auto state_name, T vec = T{})
        {
            if constexpr (stt().empty())
            {
                return vec;
            }
            else
            {
                constexpr auto star_pos = stt().find("[*]");
                constexpr auto arrow_pos = stt().rfind("->", star_pos);
                constexpr auto endl_before_pos = stt().rfind("\n", star_pos);
                constexpr auto state_pos = stt().rfind(state_name(), arrow_pos);

                if constexpr (
                    star_pos != std::string::npos &&
                    arrow_pos != std::string::npos &&
                    arrow_pos > endl_before_pos &&
                    state_pos != std::string::npos &&
                    state_pos > endl_before_pos &&
                    cleanup_token(stt().substr(state_pos, arrow_pos - state_pos)) == state_name())
                {
                    return
                        typename ::boost::mpl::push_back<
                        T,
                        boost::msm::TerminateFlag
                        >::type{};
                }
                else if constexpr (star_pos != std::string::npos)
                {
                    return parse_terminate(
                        [=]() {return stt().substr(star_pos + 3); },
                        state_name,
                        vec);
                }
                else
                {
                    return vec;
                }
            }

        };

        template <class Func, class T = boost::fusion::vector0<>>
        constexpr auto parse_flags(Func stt, auto state_name, T vec = T{})
        {
            constexpr auto flag_pos = stt().find("flag");

            if constexpr (flag_pos != std::string::npos)
            {          
                // we need to handle a flag
                constexpr auto endl_after_flag_pos = stt().find("\n", flag_pos);
                constexpr auto col_pos = stt().rfind(":",flag_pos);
                constexpr auto endl_before_flag_pos = stt().rfind("\n", flag_pos);

                if constexpr (endl_after_flag_pos != std::string::npos)
                {
                    // the name start from end of prev line+1 to :
                    if constexpr (cleanup_token(stt().substr(endl_before_flag_pos+1,col_pos- (endl_before_flag_pos+1))) == state_name())
                    {
                        // flag name is from flag tag until endline
                        return parse_flags(
                            [=]() {return stt().substr(endl_after_flag_pos + 1); },
                            state_name,
                            typename ::boost::mpl::push_back<
                            T,
                            typename boost::msm::front::puml::convert_to_msm_names<
                            boost::msm::front::puml::Flag< by_name(cleanup_token(stt().substr(flag_pos + 4, endl_after_flag_pos - (flag_pos + 4))))>>::type
                            >::type{});
                    }
                    else
                    {
                        // check starting from next line
                        return parse_flags(
                            [=]() {return stt().substr(endl_after_flag_pos + 1); },
                            state_name,
                            vec);
                    }
                }
                else
                {
                    // the flag line is the last line of the string so we will end recursion wether we found a flag or not
                    if constexpr (cleanup_token(stt().substr(endl_before_flag_pos + 1, col_pos - (endl_before_flag_pos + 1))) == state_name())
                    {
                        return typename ::boost::mpl::push_back<
                            T,
                            typename boost::msm::front::puml::convert_to_msm_names<
                            boost::msm::front::puml::Flag< by_name(cleanup_token(stt().substr(flag_pos + 4, endl_after_flag_pos - (flag_pos + 4))))>>::type
                            >::type{};
                    }
                    else
                    {
                        return vec;
                    }
                }                
            }
            else 
            {
                // no flag left, end recursion
                return vec;
            }            
        }

        template <class Func, int actions_count, int anum, class T = boost::fusion::vector<>>
        constexpr auto create_action_sequence_helper(Func action_func, T vec = T{})
        {
            // stop condition
            if constexpr (anum >= actions_count)
            {
                return vec;
            }
            else
            {
                return boost::msm::front::puml::detail::create_action_sequence_helper<Func, actions_count, anum + 1>(
                    action_func,
                    typename ::boost::mpl::push_back<
                    T,
                    typename boost::msm::front::puml::convert_to_msm_names<
                    Action<by_name(boost::msm::front::puml::detail::parse_action<anum>(action_func()))>>::type >::type{});
            }
        }
        template <class Func>
        constexpr auto create_action_sequence(Func action_func)
        {
            return boost::msm::front::puml::detail::create_action_sequence_helper<
                Func, boost::msm::front::puml::detail::count_actions(action_func()), 0>(action_func);
        }


        template <class Func, class T = boost::fusion::vector0<>>
        constexpr auto parse_state_actions(Func stt, auto state_name, auto tag_text, T vec = T{})
        {
            constexpr auto entry_pos = stt().find(std::string_view(tag_text()));
            constexpr auto tag_size = std::string_view(tag_text()).length();

            if constexpr (entry_pos != std::string::npos)
            {
                // we need to handle an entry
                constexpr auto endl_after_entry_pos = stt().find("\n", entry_pos);
                constexpr auto bracket_beg_after_entry_pos = stt().find("[", entry_pos);

                constexpr auto col_pos = stt().rfind(":", entry_pos);
                constexpr auto endl_before_entry_pos = stt().rfind("\n", entry_pos);
                auto make_action_sequence = [](auto actions)
                    {
                        if constexpr (boost::mpl::size<decltype(actions)>::value == 1)
                            return boost::fusion::at_c<0>(actions);
                        else if constexpr (boost::mpl::size<decltype(actions)>::value == 0)
                            return boost::msm::front::none{};
                        else
                            return boost::msm::front::ActionSequence_<decltype(actions)>{};
                    };

                if constexpr (endl_after_entry_pos != std::string::npos)
                {
                    // the name start from end of prev line+1 to :
                    if constexpr (by_name(cleanup_token(stt().substr(endl_before_entry_pos + 1, col_pos - (endl_before_entry_pos + 1)))) == by_name(state_name()))
                    {
                        if constexpr (bracket_beg_after_entry_pos != std::string::npos && bracket_beg_after_entry_pos < endl_after_entry_pos)
                        {
                            constexpr auto bracket_end_after_entry_pos = stt().find("]", entry_pos);
                            auto guard_l = [=]() {return stt().substr(bracket_beg_after_entry_pos +1, bracket_end_after_entry_pos - (bracket_beg_after_entry_pos +1)) ; };
                            auto action_l = [=]() {return stt().substr(entry_pos + tag_size, bracket_beg_after_entry_pos - (entry_pos + tag_size)); };

                            // action name is from entry tag until [
                            return parse_state_actions(
                                [=]() {return stt().substr(endl_after_entry_pos + 1); },
                                state_name,
                                tag_text,
                                typename ::boost::mpl::push_back<
                                    T,
                                    boost::msm::front::puml::detail::pair_type<
                                        decltype(make_action_sequence(boost::msm::front::puml::detail::create_action_sequence(action_l))),
                                        typename boost::msm::front::puml::convert_to_msm_names<
                                            decltype(boost::msm::front::puml::detail::parse_guard(guard_l))>::type
                                    >
                                >::type{});
                        }
                        else
                        {
                            // action name is from entry tag until endline
                            auto action_l = [=]() {return stt().substr(entry_pos + tag_size, endl_after_entry_pos - (entry_pos + tag_size)); };

                            return parse_state_actions(
                                [=]() {return stt().substr(endl_after_entry_pos + 1); },
                                state_name,
                                tag_text,
                                typename ::boost::mpl::push_back<
                                    T,
                                    boost::msm::front::puml::detail::pair_type<
                                        decltype(make_action_sequence(boost::msm::front::puml::detail::create_action_sequence(action_l))),
                                        typename boost::msm::front::puml::convert_to_msm_names<
                                            boost::msm::front::puml::Guard<by_name("")>>::type
                                    >
                                >::type{});
                        }

                    }
                    else
                    {
                        // check starting from next line
                        return parse_state_actions(
                            [=]() {return stt().substr(endl_after_entry_pos + 1); },
                            state_name,
                            tag_text,
                            vec);
                    }
                }
                // last line of string
                else
                {
                    // the entry line is the last line of the string so we will end recursion wether we found an entry or not
                    if constexpr (by_name(cleanup_token(stt().substr(endl_before_entry_pos + 1, col_pos - (endl_before_entry_pos + 1)))) == by_name(state_name()))
                    {
                        if constexpr (bracket_beg_after_entry_pos != std::string::npos && bracket_beg_after_entry_pos < endl_after_entry_pos)
                        {
                            constexpr auto bracket_end_after_entry_pos = stt().find("]", entry_pos);
                            auto guard_l = [stt]() {return stt().substr(bracket_beg_after_entry_pos +1, bracket_end_after_entry_pos - (bracket_beg_after_entry_pos +1)) ; };


                            // action name is from entry tag until [
                            auto action_l = [stt]() {return stt().substr(entry_pos + tag_size, bracket_beg_after_entry_pos - (entry_pos + tag_size)); };

                            return 
                                typename ::boost::mpl::push_back<
                                    T,
                                    boost::msm::front::puml::detail::pair_type<
                                        decltype(make_action_sequence(boost::msm::front::puml::detail::create_action_sequence(action_l))),
                                        decltype(boost::msm::front::puml::detail::parse_guard(guard_l))
                                    >
                                >::type{};
                        }
                        else
                        {
                            auto action_l = [stt]() {return stt().substr(entry_pos + tag_size, endl_after_entry_pos - (entry_pos + tag_size)); };

                            return 
                            typename ::boost::mpl::push_back<
                                T,
                                boost::msm::front::puml::detail::pair_type <
                                    decltype(make_action_sequence(boost::msm::front::puml::detail::create_action_sequence(action_l))),
                                    boost::msm::front::puml::Guard<by_name("")>
                                >
                            >::type{};
                        }

                    }
                    else
                    {
                        return vec;
                    }
                }
            }
            else
            {
                // no entry left, end recursion
                return vec;
            }
        }

        // recursively fills fusion vector with transition (making a tansition_table)
        template <class Func, int transitions, int tnum, class T = boost::fusion::vector<>>
        constexpr auto create_transition_table_helper(Func stt, T vec = T{})
        {
            // stop condition
            if constexpr (tnum >= transitions)
            {
                return vec;
            }
            else
            {
                auto guard_l = [stt]() {return boost::msm::front::puml::detail::parse_stt<tnum>(stt()).guard; };
                auto action_l = [stt]() {return boost::msm::front::puml::detail::parse_stt<tnum>(stt()).action; };
                auto source_l = [stt]() {return boost::msm::front::puml::detail::parse_stt<tnum>(stt()).source; };
                auto target_l = [stt]() {return boost::msm::front::puml::detail::parse_stt<tnum>(stt()).target; };
                auto stt_l = [stt]() {return std::string_view(stt()); };
                auto entry_l = []() {return "entry"; };
                auto exit_l = []() {return "exit"; };

                auto make_action_sequence = [](auto actions)
                    {
                        if constexpr (boost::mpl::size<decltype(actions)>::value == 1)
                            return boost::fusion::at_c<0>(actions);
                        else if constexpr (boost::mpl::size<decltype(actions)>::value == 0)
                            return boost::msm::front::none{};
                        else
                            return boost::msm::front::ActionSequence_<decltype(actions)>{};
                    };


                using one_row =
                    boost::msm::front::Row <
                    State < by_name(boost::msm::front::puml::detail::parse_stt<tnum>(stt()).source),
                            decltype(boost::msm::front::puml::detail::parse_terminate(
                                stt_l, source_l, boost::msm::front::puml::detail::parse_flags(stt_l,source_l))),
                            decltype(parse_state_actions(stt_l,source_l, entry_l)),
                            decltype(parse_state_actions(stt_l,source_l, exit_l))
                    >,
                    typename boost::msm::front::puml::convert_to_msm_names<
                        Event< by_name(boost::msm::front::puml::detail::parse_stt<tnum>(stt()).event)>>::type,
                    typename boost::msm::front::puml::convert_to_msm_names<
                        State< by_name(boost::msm::front::puml::detail::parse_stt<tnum>(stt()).target),
                               decltype(boost::msm::front::puml::detail::parse_terminate(
                                   stt_l, target_l, boost::msm::front::puml::detail::parse_flags(stt_l,target_l))),
                               decltype(parse_state_actions(stt_l, target_l, entry_l)),
                               decltype(parse_state_actions(stt_l, target_l, exit_l))
                    >>::type,
                    decltype(make_action_sequence(boost::msm::front::puml::detail::create_action_sequence(action_l))),
                    decltype(boost::msm::front::puml::detail::parse_guard(guard_l))
                    >;

                return boost::msm::front::puml::detail::create_transition_table_helper<Func, transitions, tnum + 1>(
                    stt, typename ::boost::mpl::push_back< T, one_row>::type{});
            }
        }


        template <class Func, int regions, int rnum, class T = boost::fusion::vector<>>
        constexpr auto create_inits_helper(Func stt, T vec = T{})
        {
            // stop condition
            if constexpr (rnum >= regions)
            {
                return vec;
            }
            else
            {
                return boost::msm::front::puml::detail::create_inits_helper<Func, regions, rnum + 1>(
                    stt, typename ::boost::mpl::push_back< T, State<by_name(boost::msm::front::puml::detail::parse_inits<rnum>(stt()))> >::type{});
            }
        }


    }//namespace detail



    template <class Func>
    constexpr auto create_transition_table(Func stt_func)
    {
        return boost::msm::front::puml::detail::create_transition_table_helper<
            Func,
            boost::msm::front::puml::detail::count_transitions(
                stt_func()) - 
                boost::msm::front::puml::detail::count_inits(stt_func()) - 
                boost::msm::front::puml::detail::count_terminates(stt_func())
            , 0>(stt_func);
    }

    template <class Func>
    constexpr auto create_initial_states(Func stt_func)
    {
        return boost::msm::front::puml::detail::create_inits_helper<
            Func,
            boost::msm::front::puml::detail::count_inits(stt_func()), 0>(stt_func);
    }

    template <class Func>
    constexpr auto create_fsm_table(Func stt_func)
    {
        return boost::msm::front::puml::detail::pair_type <
           decltype(
           boost::msm::front::puml::detail::create_transition_table_helper<
                Func,
                boost::msm::front::puml::detail::count_transitions(
                    stt_func()) -
                    boost::msm::front::puml::detail::count_inits(stt_func()) - 
                    boost::msm::front::puml::detail::count_terminates(stt_func())
               , 0>(stt_func)),
           decltype(
           boost::msm::front::puml::detail::create_inits_helper<
                Func,
                boost::msm::front::puml::detail::count_inits(stt_func()), 0>(stt_func))>{};
    }

}//boost::msm::front::puml

// helper macro to hide declarations
#define BOOST_MSM_PUML_DECLARE_TABLE(stt)                       \
using Stt = decltype(create_fsm_table([]() {return stt;}));     \
using transition_table = typename Stt::first;                   \
using initial_state = typename Stt::second;


#endif // BOOST_MSM_FRONT_PUML_COMMON_H
