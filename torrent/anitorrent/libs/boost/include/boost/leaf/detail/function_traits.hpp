#ifndef BOOST_LEAF_DETAIL_FUNCTION_TRAITS_HPP_INCLUDED
#define BOOST_LEAF_DETAIL_FUNCTION_TRAITS_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/detail/mp11.hpp>

#include <tuple>

namespace boost { namespace leaf {

namespace leaf_detail
{
    template <class T> struct remove_noexcept { using type = T; };
    template <class R, class... A>  struct remove_noexcept<R(*)(A...) noexcept> { using type = R(*)(A...); };
    template <class C, class R, class... A>  struct remove_noexcept<R(C::*)(A...) noexcept> { using type = R(C::*)(A...); };
    template <class C, class R, class... A>  struct remove_noexcept<R(C::*)(A...) const noexcept> { using type = R(C::*)(A...) const; };

    template<class...>
    struct gcc49_workaround //Thanks Glen Fernandes
    {
        using type = void;
    };

    template<class... T>
    using void_t = typename gcc49_workaround<T...>::type;

    template<class F,class V=void>
    struct function_traits_impl
    {
        constexpr static int arity = -1;
    };

    template<class F>
    struct function_traits_impl<F, void_t<decltype(&F::operator())>>
    {
    private:

        using tr = function_traits_impl<typename remove_noexcept<decltype(&F::operator())>::type>;

    public:

        using return_type = typename tr::return_type;
        static constexpr int arity = tr::arity - 1;

        using mp_args = typename leaf_detail_mp11::mp_rest<typename tr::mp_args>;

        template <int I>
        struct arg:
            tr::template arg<I+1>
        {
        };
    };

    template<class R, class... A>
    struct function_traits_impl<R(A...)>
    {
        using return_type = R;
        static constexpr int arity = sizeof...(A);

        using mp_args = leaf_detail_mp11::mp_list<A...>;

        template <int I>
        struct arg
        {
            static_assert(I < arity, "I out of range");
            using type = typename std::tuple_element<I,std::tuple<A...>>::type;
        };
    };

    template<class F> struct function_traits_impl<F&> : function_traits_impl<F> { };
    template<class F> struct function_traits_impl<F&&> : function_traits_impl<F> { };
    template<class R, class... A> struct function_traits_impl<R(*)(A...)> : function_traits_impl<R(A...)> { };
    template<class R, class... A> struct function_traits_impl<R(* &)(A...)> : function_traits_impl<R(A...)> { };
    template<class R, class... A> struct function_traits_impl<R(* const &)(A...)> : function_traits_impl<R(A...)> { };
    template<class C, class R, class... A> struct function_traits_impl<R(C::*)(A...)> : function_traits_impl<R(C&,A...)> { };
    template<class C, class R, class... A> struct function_traits_impl<R(C::*)(A...) const> : function_traits_impl<R(C const &,A...)> { };
    template<class C, class R> struct function_traits_impl<R(C::*)> : function_traits_impl<R(C&)> { };

    template <class F>
    struct function_traits: function_traits_impl<typename remove_noexcept<F>::type>
    {
    };

    template <class F>
    using fn_return_type = typename function_traits<F>::return_type;

    template <class F, int I>
    using fn_arg_type = typename function_traits<F>::template arg<I>::type;

    template <class F>
    using fn_mp_args = typename function_traits<F>::mp_args;
}

} }

#endif
