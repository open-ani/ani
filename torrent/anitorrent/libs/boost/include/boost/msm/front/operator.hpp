// Copyright 2008 Christophe Henry
// henry UNDERSCORE christophe AT hotmail DOT com
// This is an extended version of the state machine available in the boost::mpl library
// Distributed under the same license as the original.
// Copyright for the original version:
// Copyright 2005 David Abrahams and Aleksey Gurtovoy. Distributed
// under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_MSM_FRONT_OPERATOR_H
#define BOOST_MSM_FRONT_OPERATOR_H



namespace boost { namespace msm { namespace front
{

template <class T1,class T2>
struct Or_
{
    template <class EVT,class FSM,class SourceState,class TargetState>
    bool operator()(EVT const& evt, FSM& fsm,SourceState& src,TargetState& tgt)
    {
        return (T1()(evt,fsm,src,tgt) || T2()(evt,fsm,src,tgt));
    }
    template <class Event,class FSM,class STATE>
    bool operator()(Event const& evt,FSM& fsm,STATE& state)
    {
        return (T1()(evt,fsm,state) || T2()(evt,fsm,state));
    }
};
template <class T1,class T2>
struct And_
{
    template <class EVT,class FSM,class SourceState,class TargetState>
    bool operator()(EVT const& evt, FSM& fsm,SourceState& src,TargetState& tgt)
    {
        return (T1()(evt,fsm,src,tgt) && T2()(evt,fsm,src,tgt));
    }
    template <class Event,class FSM,class STATE>
    bool operator()(Event const& evt,FSM& fsm,STATE& state)
    {
        return (T1()(evt,fsm,state) && T2()(evt,fsm,state));
    }
};
template <class T1>
struct Not_
{
    template <class EVT,class FSM,class SourceState,class TargetState>
    bool operator()(EVT const& evt, FSM& fsm,SourceState& src,TargetState& tgt)
    {
        return !(T1()(evt,fsm,src,tgt));
    }
    template <class Event,class FSM,class STATE>
    bool operator()(Event const& evt,FSM& fsm,STATE& state)
    {
        return !(T1()(evt,fsm,state));
    }
};


}}}

#endif // BOOST_MSM_FRONT_OPERATOR_H
