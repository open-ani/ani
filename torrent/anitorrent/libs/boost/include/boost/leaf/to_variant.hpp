#ifndef BOOST_LEAF_TO_VARIANT_HPP_INCLUDED
#define BOOST_LEAF_TO_VARIANT_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#if __cplusplus >= 201703L

#include <boost/leaf/config.hpp>
#include <boost/leaf/handle_errors.hpp>
#include <boost/leaf/result.hpp>
#include <variant>
#include <optional>
#include <tuple>

namespace boost { namespace leaf {

template <class... E, class TryBlock>
std::variant<typename std::decay<decltype(std::declval<TryBlock>()().value())>::type,std::tuple<std::optional<E>...>>
to_variant( TryBlock && try_block )
{
    static_assert(is_result_type<decltype(std::declval<TryBlock>()())>::value, "The return type of the try_block passed to a to_variant function must be registered with leaf::is_result_type");
    using T = typename std::decay<decltype(std::declval<TryBlock>()().value())>::type;
    using error_tuple_type = std::tuple<std::optional<E>...>;
    using variant_type = std::variant<T, error_tuple_type>;
    return try_handle_all(
        [&]() -> result<variant_type>
        {
            if( auto r = std::forward<TryBlock>(try_block)() )
                return *std::move(r);
            else
                return r.error();
        },
        []( E const * ... e ) -> variant_type
        {
            return error_tuple_type { e ? std::optional<E>(*e) : std::optional<E>{}... };
        },
        []() -> variant_type
        {
            return error_tuple_type { };
        } );
}

} }

#endif

#endif
