// Copyright 2008 Christophe Henry
// henry UNDERSCORE christophe AT hotmail DOT com
// This is an extended version of the state machine available in the boost::mpl library
// Distributed under the same license as the original.
// Copyright for the original version:
// Copyright 2005 David Abrahams and Aleksey Gurtovoy. Distributed
// under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_MSM_BACK11_DISPATCH_TABLE_H
#define BOOST_MSM_BACK11_DISPATCH_TABLE_H

#include <cstdint>

#include <utility>

#include <boost/mpl/reverse_fold.hpp>
#include <boost/mpl/greater.hpp>
#include <boost/mpl/filter_view.hpp>
#include <boost/mpl/pop_front.hpp>
#include <boost/mpl/for_each.hpp>
#include <boost/mpl/advance.hpp>
#include <boost/fusion/container/vector.hpp>

#include <boost/type_traits/is_base_of.hpp>
#include <boost/type_traits/is_same.hpp>

#include <boost/msm/event_traits.hpp>
#include <boost/msm/back11/metafunctions.hpp>
#include <boost/msm/back/common_types.hpp>

BOOST_MPL_HAS_XXX_TRAIT_DEF(is_frow)

namespace boost { namespace msm { namespace back11
{

// Generates a singleton runtime lookup table that maps current state
// to a function that makes the SM take its transition on the given
// Event type.
template <class Fsm,class Stt, class Event,class CompilePolicy>
struct dispatch_table
{
 private:
    // This is a table of these function pointers.
    typedef boost::msm::back::HandledEnum (*cell)(Fsm&, int,int,Event&);
    typedef bool (*guard)(Fsm&, Event&);

    // class used to build a chain (or sequence) of transitions for a given event and start state
    // (like an UML diamond). Allows transition conflicts.
    template< typename Seq,typename AnEvent,typename State >
    struct chain_row
    {
        typedef State   current_state_type;
        typedef AnEvent transition_event;

        // helper for building a disable/enable_if-controlled execute function
        struct execute_helper
        {
            template <class Sequence>
            static
            boost::msm::back::HandledEnum
            execute(Fsm& , int, int, Event& , ::boost::mpl::true_ const & )
            {
                // if at least one guard rejected, this will be ignored, otherwise will generate an error
                return ::boost::msm::back::HANDLED_FALSE;
            }

            template <class Sequence>
            static
            boost::msm::back::HandledEnum
            execute(Fsm& fsm, int region_index , int state, Event& evt,
                    ::boost::mpl::false_ const & )
            {
                 // try the first guard
                 typedef typename ::boost::mpl::front<Sequence>::type first_row;
                 boost::msm::back::HandledEnum res = first_row::execute(fsm,region_index,state,evt);
                 if (::boost::msm::back::HANDLED_TRUE!=res && ::boost::msm::back::HANDLED_DEFERRED!=res)
                 {
                    // if the first rejected, move on to the next one
                    boost::msm::back::HandledEnum sub_res =
                         execute<typename ::boost::mpl::pop_front<Sequence>::type>(fsm,region_index,state,evt,
                            ::boost::mpl::bool_<
                                ::boost::mpl::empty<typename ::boost::mpl::pop_front<Sequence>::type>::type::value>());
                    // if at least one guards rejects, the event will not generate a call to no_transition
                    if ((::boost::msm::back::HANDLED_FALSE==sub_res) && (::boost::msm::back::HANDLED_GUARD_REJECT==res) )
                        return ::boost::msm::back::HANDLED_GUARD_REJECT;
                    else
                        return sub_res;
                 }
                 return res;
            }
        };
        // Take the transition action and return the next state.
        static boost::msm::back::HandledEnum execute(Fsm& fsm, int region_index, int state, Event& evt)
        {
            // forward to helper
            return execute_helper::template execute<Seq>(fsm,region_index,state,evt,
                ::boost::mpl::bool_< ::boost::mpl::empty<Seq>::type::value>());
        }
    };
    // nullary metafunction whose only job is to prevent early evaluation of _1
    template< typename Entry > 
    struct make_chain_row_from_map_entry
    { 
        // if we have more than one frow with the same state as source, remove the ones extra
        // note: we know the frow's are located at the beginning so we remove at the beginning (number of frows - 1) elements
        enum {number_frows = ::boost::mpl::count_if< typename Entry::second,has_is_frow< ::boost::mpl::placeholders::_1> >::value};

        //erases the first NumberToDelete rows
        template<class Sequence, int NumberToDelete>
        struct erase_first_rows
        {
            typedef typename ::boost::mpl::erase<
                typename Entry::second,
                typename ::boost::mpl::begin<Sequence>::type,
                typename ::boost::mpl::advance<
                        typename ::boost::mpl::begin<Sequence>::type, 
                        ::boost::mpl::int_<NumberToDelete> >::type
            >::type type;
        };
        // if we have more than 1 frow with this event (not allowed), delete the spare
        typedef typename ::boost::mpl::eval_if<
            typename ::boost::mpl::bool_< number_frows >= 2 >::type,
            erase_first_rows<typename Entry::second,number_frows-1>,
            ::boost::mpl::identity<typename Entry::second>
        >::type filtered_stt;

        typedef chain_row<filtered_stt,Event,
            typename Entry::first > type;
    }; 
    template< typename Entry >
    struct make_chain_row_from_map_entry2
    {
        // if we have more than one frow with the same state as source, remove the ones extra
        // note: we know the frow's are located at the beginning so we remove at the beginning (number of frows - 1) elements
        typedef typename boost::fusion::result_of::first<Entry>::type entry_vec_first;
        typedef typename boost::fusion::result_of::second<Entry>::type entry_vec_second;
        //typedef typename Entry::second_type entry_vec_second;

        enum {number_frows = ::boost::mpl::count_if<entry_vec_second,has_is_frow< ::boost::mpl::placeholders::_1> >::value};

        //erases the first NumberToDelete rows
        template<class Sequence, int NumberToDelete>
        struct erase_first_rows
        {
            typedef typename ::boost::mpl::erase<
                typename Entry::second,
                typename ::boost::mpl::begin<Sequence>::type,
                typename ::boost::mpl::advance<
                        typename ::boost::mpl::begin<Sequence>::type,
                        ::boost::mpl::int_<NumberToDelete> >::type
            >::type type;
        };
        // if we have more than 1 frow with this event (not allowed), delete the spare
        typedef typename ::boost::mpl::eval_if<
            typename ::boost::mpl::bool_< number_frows >= 2 >::type,
            erase_first_rows<entry_vec_second,number_frows-1>,
            ::boost::mpl::identity<entry_vec_second>
        >::type filtered_stt;

        typedef chain_row<filtered_stt,Event,entry_vec_first > type;
    };

    // helper for lazy evaluation in eval_if of change_frow_event
    template <class Transition,class NewEvent>
    struct replace_event
    {
        typedef typename Transition::template replace_event<NewEvent>::type type;
    };
    // changes the event type for a frow to the event we are dispatching
    // this helps ensure that an event does not get processed more than once because of frows and base events.
    template <class FrowTransition>
    struct change_frow_event
    {
        typedef typename ::boost::mpl::eval_if<
            typename has_is_frow<FrowTransition>::type,
            replace_event<FrowTransition,Event>,
            boost::mpl::identity<FrowTransition>
        >::type type;
    };
    // Compute the maximum state value in the sm so we know how big
    // to make the table
    typedef typename generate_state_set<Stt>::type state_list;
    BOOST_STATIC_CONSTANT(int, max_state = ( ::boost::mpl::size<state_list>::value));

    template <class Transition>
    struct convert_event_and_forward
    {
        static boost::msm::back::HandledEnum execute(Fsm& fsm, int region_index, int state, Event& evt)
        {
            typename Transition::transition_event forwarded(evt);
            return Transition::execute(fsm,region_index,state,forwarded);
        }
    };

    template <class T, class Map>
    struct push_to_map_of_vec
    {
        typedef typename
        boost::fusion::result_of::as_map<
                typename boost::fusion::result_of::insert<
                    Map,
                    typename ::boost::fusion::result_of::end<Map>::type,
                    typename ::boost::fusion::result_of::make_pair<
                        typename transition_source_type<T>::type,
                        typename ::boost::mpl::push_back<
                            typename ::boost::fusion::result_of::value_at_key<
                                Map,
                                typename transition_source_type<T>::type
                            >::type,
                            typename change_frow_event<T>::type
                        >::type
                     >::type
                 >::type
        >::type type;
    };

    // A function object for use with mpl::for_each that stuffs
    // transitions into cells.
    struct init_cell
    {
        init_cell(dispatch_table* self_)
          : self(self_)
        {}
        // version for transition event not base of our event
        // first for all transitions, then for internal ones of a fsm
        template <class Transition>
        typename ::boost::disable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::true_ const &, ::boost::mpl::false_ const &) const
        {
            typedef typename create_stt<Fsm>::type stt; 
            BOOST_STATIC_CONSTANT(int, state_id = 
                (get_state_id<stt,typename Transition::current_state_type>::value));
            // reinterpret_cast to uintptr_t to suppress gcc-11 warning
            self->entries[state_id+1] = reinterpret_cast<cell>(
                reinterpret_cast<std::uintptr_t>(&Transition::execute));
        }
        template <class Transition>
        typename ::boost::enable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::true_ const &, ::boost::mpl::false_ const &) const
        {
            self->entries[0] = reinterpret_cast<cell>(&Transition::execute);
        }

        // version for transition event is boost::any
        // first for all transitions, then for internal ones of a fsm
        template <class Transition>
        typename ::boost::disable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::false_ const &, ::boost::mpl::true_ const &) const
        {
            typedef typename create_stt<Fsm>::type stt; 
            BOOST_STATIC_CONSTANT(int, state_id = 
                (get_state_id<stt,typename Transition::current_state_type>::value));
            self->entries[state_id+1] = &convert_event_and_forward<Transition>::execute;
        }
        template <class Transition>
        typename ::boost::enable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::false_ const &, ::boost::mpl::true_ const &) const
        {
            self->entries[0] = &convert_event_and_forward<Transition>::execute;
        }
        template <class Transition>
        typename ::boost::disable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::true_ const &, ::boost::mpl::true_ const &) const
        {
            typedef typename create_stt<Fsm>::type stt;
            BOOST_STATIC_CONSTANT(int, state_id =
                (get_state_id<stt,typename Transition::current_state_type>::value));
            self->entries[state_id+1] = &convert_event_and_forward<Transition>::execute;
        }
        template <class Transition>
        typename ::boost::enable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::true_ const &, ::boost::mpl::true_ const &) const
        {
            self->entries[0] = &convert_event_and_forward<Transition>::execute;
        }
        // end version for kleene

        // version for transition event base of our event
        // first for all transitions, then for internal ones of a fsm
        template <class Transition>
        typename ::boost::disable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::false_ const &, ::boost::mpl::false_ const &) const
        {
            typedef typename create_stt<Fsm>::type stt; 
            BOOST_STATIC_CONSTANT(int, state_id = 
                (get_state_id<stt,typename Transition::current_state_type>::value));
            self->entries[state_id+1] = &Transition::execute;
        }
        template <class Transition>
        typename ::boost::enable_if<
            typename ::boost::is_same<typename Transition::current_state_type,Fsm>::type
        ,void>::type
        init_event_base_case(Transition const&, ::boost::mpl::false_ const &, ::boost::mpl::false_ const &) const
        {
            self->entries[0] = &Transition::execute;
        }
        // Cell initializer function object, used with mpl::for_each
        template <class Transition>
        typename ::boost::enable_if<typename has_not_real_row_tag<Transition>::type,void >::type
            operator()(Transition const&,boost::msm::back::dummy<0> = 0) const
        {
            // version for not real rows. No problem because irrelevant for process_event
        }
        template <class Transition>
        typename ::boost::disable_if<typename has_not_real_row_tag<Transition>::type,void >::type
        operator()(Transition const& tr,boost::msm::back::dummy<1> = 0) const
        {
            //only if the transition event is a base of our event is the reinterpret_case safe
            init_event_base_case(tr,
                ::boost::mpl::bool_< 
                    ::boost::is_base_of<typename Transition::transition_event,Event>::type::value>(),
                ::boost::mpl::bool_< 
                    ::boost::msm::is_kleene_event<typename Transition::transition_event>::type::value>());
        }
    
        dispatch_table* self;
    };

    // Cell default-initializer function object, used with mpl::for_each
    // initializes with call_no_transition, defer_transition or default_eventless_transition
    // variant for non-anonymous transitions
    template <class EventType,class Enable=void>
    struct default_init_cell
    {
        default_init_cell(dispatch_table* self_,cell* tofill_entries_)
            : self(self_),tofill_entries(tofill_entries_)
        {}
        template <class State>
        typename ::boost::enable_if<typename has_state_delayed_event<State,Event>::type,void>::type
        operator()(boost::msm::wrap<State> const&,boost::msm::back::dummy<0> = 0)
        {
            typedef typename create_stt<Fsm>::type stt; 
            BOOST_STATIC_CONSTANT(int, state_id = (get_state_id<stt,State>::value));
            cell call_no_transition = &Fsm::defer_transition;
            tofill_entries[state_id+1] = call_no_transition;
        }
        template <class State>
        typename ::boost::disable_if<
            typename ::boost::mpl::or_<
                typename has_state_delayed_event<State,Event>::type,
                typename ::boost::is_same<State,Fsm>::type
            >::type
        ,void >::type
        operator()(boost::msm::wrap<State> const&,boost::msm::back::dummy<1> = 0)
        {
            typedef typename create_stt<Fsm>::type stt; 
            BOOST_STATIC_CONSTANT(int, state_id = (get_state_id<stt,State>::value));
            cell call_no_transition = &Fsm::call_no_transition;
            tofill_entries[state_id+1] = call_no_transition;
        }
        // case for internal transitions of this fsm
        template <class State>
        typename ::boost::enable_if<
            typename ::boost::mpl::and_<
                typename ::boost::mpl::not_<typename has_state_delayed_event<State,Event>::type>::type,
                typename ::boost::is_same<State,Fsm>::type
            >::type
        ,void>::type
        operator()(boost::msm::wrap<State> const&,boost::msm::back::dummy<2> = 0)
        {
            cell call_no_transition = &Fsm::call_no_transition_internal;
            tofill_entries[0] = call_no_transition;
        }
        dispatch_table* self;
        cell* tofill_entries;
    };

    // variant for anonymous transitions
    template <class EventType>
    struct default_init_cell<EventType,
                             typename ::boost::enable_if<
                                typename is_completion_event<EventType>::type>::type>
    {
        default_init_cell(dispatch_table* self_,cell* tofill_entries_)
            : self(self_),tofill_entries(tofill_entries_)
        {}

        // this event is a compound one (not a real one, just one for use in event-less transitions)
        // Note this event cannot be used as deferred!
        // case for internal transitions of this fsm 
        template <class State>
        typename ::boost::disable_if<
            typename ::boost::is_same<State,Fsm>::type
        ,void>::type
        operator()(boost::msm::wrap<State> const&,boost::msm::back::dummy<0> = 0)
        {
            typedef typename create_stt<Fsm>::type stt; 
            BOOST_STATIC_CONSTANT(int, state_id = (get_state_id<stt,State>::value));
            cell call_no_transition = &Fsm::default_eventless_transition;
            tofill_entries[state_id+1] = call_no_transition;
        }

        template <class State>
        typename ::boost::enable_if<
            typename ::boost::is_same<State,Fsm>::type
        ,void>::type
        operator()(boost::msm::wrap<State> const&,boost::msm::back::dummy<1> = 0)
        {
            cell call_no_transition = &Fsm::default_eventless_transition;
            tofill_entries[0] = call_no_transition;
        }
        dispatch_table* self;
        cell* tofill_entries;
    };

 public:
    // initialize the dispatch table for a given Event and Fsm
    dispatch_table()
    {
        // Initialize cells for no transition
        ::boost::mpl::for_each<typename generate_state_set<Stt>::type, 
                               boost::msm::wrap< ::boost::mpl::placeholders::_1> >
                        (default_init_cell<Event>(this,entries));

        typedef typename ::boost::mpl::reverse_fold<
                        // filter on event
                        ::boost::mpl::filter_view
                            <Stt, boost::mpl::or_<
                                    ::boost::is_base_of<transition_event< ::boost::mpl::placeholders::_>, Event>,
                                    ::boost::msm::is_kleene_event<transition_event< ::boost::mpl::placeholders::_> >
                                    >
                            >,
                        // build a map
                        ::boost::fusion::map<>,
                        ::boost::mpl::if_<
                            // if we already have a row on this source state
                            ::boost::mpl::has_key< ::boost::mpl::placeholders::_1,
                                                   transition_source_type< ::boost::mpl::placeholders::_2> >,
                            // insert a new element in the value type
//                            push_to_map_of_vec<::boost::mpl::placeholders::_2, ::boost::mpl::placeholders::_1>,
                            boost::fusion::result_of::as_map<boost::fusion::result_of::insert<
                                ::boost::mpl::placeholders::_1,
                                ::boost::fusion::result_of::end<mpl::placeholders::_1 >,
                                ::boost::fusion::result_of::make_pair<transition_source_type< ::boost::mpl::placeholders::_2>,
                                                   ::boost::mpl::push_back<
                                                        ::boost::fusion::result_of::value_at_key< ::boost::mpl::placeholders::_1,
                                                        transition_source_type< ::boost::mpl::placeholders::_2> >,
                                                        change_frow_event< ::boost::mpl::placeholders::_2 > >
                                                   > > >,
                            // first row on this source state, make a vector with 1 element
                            boost::fusion::result_of::as_map<boost::fusion::result_of::insert<
                                        ::boost::mpl::placeholders::_1,
                                        ::boost::fusion::result_of::end<mpl::placeholders::_1 >,
                                        ::boost::fusion::result_of::make_pair<transition_source_type< ::boost::mpl::placeholders::_2>,
                                        make_vector< change_frow_event< ::boost::mpl::placeholders::_2> > > > >
                               >
                       >::type map_of_row_seq;

        // and then build chaining rows for all source states having more than 1 row
        typedef typename ::boost::mpl::fold<
            map_of_row_seq,::boost::fusion::vector<>,
            ::boost::mpl::if_<
                     ::boost::mpl::greater< ::boost::mpl::size<
                                                    ::boost::fusion::result_of::second< ::boost::mpl::placeholders::_2> >,
                                            ::boost::mpl::int_<1> >,
                     // we need row chaining
                     ::boost::mpl::push_back< ::boost::mpl::placeholders::_1,
                                    make_chain_row_from_map_entry2< ::boost::mpl::placeholders::_2> >,
                     // just one row, no chaining, we rebuild the row like it was before
                     ::boost::mpl::push_back< ::boost::mpl::placeholders::_1,
                                              get_first_element_pair_second2< ::boost::mpl::placeholders::_2> >
             > >::type chained_rows;

        // Go back and fill in cells for matching transitions.
        ::boost::mpl::for_each<chained_rows>(init_cell(this));

    }

    // The singleton instance.
    static const dispatch_table& instance() {
        static dispatch_table table;
        return table;
    }
 public: // data members
     // +1 => 0 is reserved for this fsm (internal transitions)
    cell entries[max_state+1];
};

}}} // boost::msm::back11


#endif //BOOST_MSM_BACK11_DISPATCH_TABLE_H

