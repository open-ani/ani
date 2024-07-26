#ifndef BOOST_LEAF_CAPTURE_HPP_INCLUDED
#define BOOST_LEAF_CAPTURE_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/config.hpp>
#include <boost/leaf/handle_errors.hpp>

#if BOOST_LEAF_CFG_CAPTURE

namespace boost { namespace leaf {

namespace leaf_detail
{
    template <class R, bool IsResult = is_result_type<R>::value>
    struct is_result_tag;

    template <class R>
    struct is_result_tag<R, false>
    {
    };

    template <class R>
    struct is_result_tag<R, true>
    {
    };
}

#ifdef BOOST_LEAF_NO_EXCEPTIONS

namespace leaf_detail
{
    template <class R, class F, class... A>
    inline
    decltype(std::declval<F>()(std::forward<A>(std::declval<A>())...))
    capture_impl(is_result_tag<R, false>, F && f, A... a) noexcept
    {
        return std::forward<F>(f)(std::forward<A>(a)...);
    }

    template <class R, class Future>
    inline
    decltype(std::declval<Future>().get())
    future_get_impl(is_result_tag<R, false>, Future & fut) noexcept
    {
        return fut.get();
    }
}

#else

namespace leaf_detail
{
    // Not defined, no longer supported. Please use try_capture_all instead of make_shared_context/capture.
    template <class R, class F, class... A>
    decltype(std::declval<F>()(std::forward<A>(std::declval<A>())...))
    capture_impl(is_result_tag<R, false>, F && f, A... a);

    // Not defined, no longer supported. Please use try_capture_all instead of make_shared_context/capture.
    template <class R, class Future>
    decltype(std::declval<Future>().get())
    future_get_impl(is_result_tag<R, false>, Future & fut );
}

#endif

namespace leaf_detail
{
    template <class R, class F, class... A>
    inline
    decltype(std::declval<F>()(std::forward<A>(std::declval<A>())...))
    capture_impl(is_result_tag<R, true>, F && f, A... a) noexcept
    {
        return try_capture_all(
            [&]
            {
                return std::forward<F>(f)(std::forward<A>(a)...);
            } );
    }

    template <class R, class Future>
    inline
    decltype(std::declval<Future>().get())
    future_get_impl(is_result_tag<R, true>, Future & fut) noexcept
    {
        if( auto r = fut.get() )
            return r;
        else
        {
            r.unload();
            return r;
        }
    }
}

template <class F, class... A>
inline
decltype(std::declval<F>()(std::forward<A>(std::declval<A>())...))
capture(context_ptr &&, F && f, A... a)
{
    using namespace leaf_detail;
    return capture_impl(is_result_tag<decltype(std::declval<F>()(std::forward<A>(std::declval<A>())...))>(), std::forward<F>(f), std::forward<A>(a)...);
}

template <class Future>
inline
decltype(std::declval<Future>().get())
future_get( Future & fut )
{
    using namespace leaf_detail;
    return future_get_impl(is_result_tag<decltype(std::declval<Future>().get())>(), fut);
}

} }

#endif

#endif
