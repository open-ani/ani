//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IMPL_PARAMS_ENCODED_REF_HPP
#define BOOST_URL_IMPL_PARAMS_ENCODED_REF_HPP

#include <boost/url/detail/except.hpp>
#include <boost/assert.hpp>

namespace boost {
namespace urls {

//------------------------------------------------
//
// Modifiers
//
//------------------------------------------------

inline
void
params_encoded_ref::
clear() noexcept
{
    u_->remove_query();
}

template<class FwdIt>
void
params_encoded_ref::
assign(FwdIt first, FwdIt last)
{
/*  If you get a compile error here, it
    means that the iterators you passed
    do not meet the requirements stated
    in the documentation.
*/
    static_assert(
        std::is_convertible<
            typename std::iterator_traits<
                FwdIt>::reference,
            param_view>::value,
        "Type requirements not met");

    assign(first, last,
        typename std::iterator_traits<
            FwdIt>::iterator_category{});
}

inline
auto
params_encoded_ref::
append(
    param_pct_view const& p) ->
        iterator
{
    return insert(end(), p);
}

inline
auto
params_encoded_ref::
append(
    std::initializer_list<
        param_pct_view> init) ->
    iterator
{
    return insert(end(), init);
}

template<class FwdIt>
auto
params_encoded_ref::
append(
    FwdIt first, FwdIt last) ->
    iterator
{
/*  If you get a compile error here, it
    means that the iterators you passed
    do not meet the requirements stated
    in the documentation.
*/
    static_assert(
        std::is_convertible<
            typename std::iterator_traits<
                FwdIt>::reference,
            param_view>::value,
        "Type requirements not met");

    return insert(
        end(), first, last);
}

template<class FwdIt>
auto
params_encoded_ref::
insert(
    iterator before,
    FwdIt first,
    FwdIt last) ->
        iterator
{
/*  If you get a compile error here, it
    means that the iterators you passed
    do not meet the requirements stated
    in the documentation.
*/
    static_assert(
        std::is_convertible<
            typename std::iterator_traits<
                FwdIt>::reference,
            param_view>::value,
        "Type requirements not met");

    return insert(
        before,
        first,
        last,
        typename std::iterator_traits<
            FwdIt>::iterator_category{});
}

template<class FwdIt>
auto
params_encoded_ref::
replace(
    iterator from,
    iterator to,
    FwdIt first,
    FwdIt last) ->
        iterator
{
/*  If you get a compile error here, it
    means that the iterators you passed
    do not meet the requirements stated
    in the documentation.
*/
    static_assert(
        std::is_convertible<
            typename std::iterator_traits<
                FwdIt>::reference,
            param_view>::value,
        "Type requirements not met");

    return u_->edit_params(
        from.it_, to.it_,
        detail::make_params_encoded_iter(
            first, last));
}

//------------------------------------------------
//
// implementation
//
//------------------------------------------------

template<class FwdIt>
void
params_encoded_ref::
assign(FwdIt first, FwdIt last,
    std::forward_iterator_tag)
{
    u_->edit_params(
        begin().it_,
        end().it_,
        detail::make_params_encoded_iter(
            first, last));
}

template<class FwdIt>
auto
params_encoded_ref::
insert(
    iterator before,
    FwdIt first,
    FwdIt last,
    std::forward_iterator_tag) ->
        iterator
{
    return u_->edit_params(
        before.it_,
        before.it_,
        detail::make_params_encoded_iter(
            first, last));
}

} // urls
} // boost

#endif
