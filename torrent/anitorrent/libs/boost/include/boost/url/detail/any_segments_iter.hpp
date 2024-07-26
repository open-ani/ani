//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_ANY_SEGMENTS_ITER_HPP
#define BOOST_URL_DETAIL_ANY_SEGMENTS_ITER_HPP

#include <boost/url/pct_string_view.hpp>
#include <boost/static_assert.hpp>
#include <cstddef>
#include <iterator>
#include <type_traits>

namespace boost {
namespace urls {
namespace detail {

struct BOOST_SYMBOL_VISIBLE
    any_segments_iter
{
protected:
    explicit
    any_segments_iter(
        core::string_view s_ = {}) noexcept
        : s(s_)
    {
    }

    virtual ~any_segments_iter() = default;

public:
    // this is adjusted
    // when self-intersecting
    core::string_view s;

    // the first segment,
    // to handle special cases
    core::string_view front;

    // quick number of segments
    // 0 = zero
    // 1 = one
    // 2 = two, or more
    int fast_nseg = 0;

    // whether the segments should encode colons
    // when we measure and copy. the calling
    // function uses this for the first
    // segment in some cases, such as:
    // "x:y:z" -> remove_scheme -> "y%3Az"
    // as "y:z" would no longer represent a path
    bool encode_colons = false;

    // Rewind the iterator to the beginning
    virtual void rewind() noexcept = 0;

    // Measure and increment the current
    // element. n is increased by the
    // encoded size. Returns false on
    // end of range. 
    virtual bool measure(std::size_t& n) = 0;

    // Copy and increment the current
    // element, encoding as needed.
    virtual void copy(char*& dest,
        char const* end) noexcept = 0;
};

//------------------------------------------------
//
// segment_iter
//
//------------------------------------------------

// A 1-segment range
// allowing self-intersection
struct BOOST_SYMBOL_VISIBLE
    segment_iter
    : any_segments_iter
{
    virtual ~segment_iter() = default;

    explicit
    segment_iter(
        core::string_view s) noexcept;

private:
    bool at_end_ = false;
    void rewind() noexcept override;
    bool measure(std::size_t&) noexcept override;
    void copy(char*&, char const*) noexcept override;
};

//------------------------------------------------
//
// segments_iter
//
//------------------------------------------------

struct segments_iter_base
{
protected:
    BOOST_URL_DECL static void
    measure_impl(std::size_t&,
        core::string_view, bool) noexcept;
    BOOST_URL_DECL static void
    copy_impl(char*&, char const*,
        core::string_view, bool) noexcept;
};

// iterates segments in a
// plain segment range
template<class FwdIt>
struct segments_iter
    : any_segments_iter
    , segments_iter_base
{
    BOOST_STATIC_ASSERT(
        std::is_convertible<
            typename std::iterator_traits<
                FwdIt>::reference,
            core::string_view>::value);

    segments_iter(
        FwdIt first,
        FwdIt last) noexcept
        : it_(first)
        , it0_(first)
        , end_(last)
    {
        if(first != last)
        {
            front = *first;
            auto it = first;
            if(++it == last)
                fast_nseg = 1;
            else
                fast_nseg = 2;
        }
        else
        {
            fast_nseg = 0;
        }
    }

private:
    FwdIt it_;
    FwdIt it0_;
    FwdIt end_;

    void
    rewind() noexcept override
    {
        it_ = it0_;
    }

    bool
    measure(
        std::size_t& n) noexcept override
    {
        if(it_ == end_)
            return false;
        measure_impl(n,
            detail::to_sv(*it_),
            encode_colons);
        ++it_;
        return true;
    }

    void
    copy(
        char*& dest,
        char const* end) noexcept override
    {
        copy_impl(dest, end,
            detail::to_sv(*it_++),
            encode_colons);
    }
};

//------------------------------------------------
//
// segment_encoded_iter
//
//------------------------------------------------

// A 1-segment range
// allowing self-intersection
struct BOOST_SYMBOL_VISIBLE
    segment_encoded_iter
    : any_segments_iter
{
    virtual ~segment_encoded_iter() = default;

    explicit
    segment_encoded_iter(
        pct_string_view const& s) noexcept;

private:
    bool at_end_ = false;
    void rewind() noexcept override;
    bool measure(std::size_t&) noexcept override;
    void copy(char*&, char const*) noexcept override;
};

//------------------------------------------------
//
// segments_encoded_iter
//
//------------------------------------------------

// Validating and copying from
// a string of encoded segments
struct segments_encoded_iter_base
{
protected:
    BOOST_URL_DECL static void
    measure_impl(std::size_t&,
        core::string_view, bool) noexcept;
    BOOST_URL_DECL static void
    copy_impl(char*&, char const*,
        core::string_view, bool) noexcept;
};

// iterates segments in an
// encoded segment range
template<class FwdIt>
struct segments_encoded_iter
    : public any_segments_iter
    , public segments_encoded_iter_base
{
    BOOST_STATIC_ASSERT(
        std::is_convertible<
            typename std::iterator_traits<
                FwdIt>::reference,
            core::string_view>::value);

    segments_encoded_iter(
        FwdIt first,
        FwdIt last)
        : it_(first)
        , it0_(first)
        , end_(last)
    {
        if(it_ != end_)
        {
            // throw on invalid input
            front = pct_string_view(
                detail::to_sv(*first));
            auto it = first;
            if(++it == last)
                fast_nseg = 1;
            else
                fast_nseg = 2;
        }
        else
        {
            fast_nseg = 0;
        }
    }

private:
    FwdIt it_;
    FwdIt it0_;
    FwdIt end_;

    void
    rewind() noexcept override
    {
        it_ = it0_;
    }

    bool
    measure(
        std::size_t& n) override
    {
        if(it_ == end_)
            return false;
        // throw on invalid input
        measure_impl(n,
            pct_string_view(
                detail::to_sv(*it_++)),
            encode_colons);
        return true;
    }

    void
    copy(
        char*& dest,
        char const* end) noexcept override
    {
        copy_impl(dest, end,
            detail::to_sv(*it_++),
            encode_colons);
    }
};

//------------------------------------------------

template<class FwdIt>
segments_iter<FwdIt>
make_segments_iter(
    FwdIt first, FwdIt last)
{
    return segments_iter<
        FwdIt>(first, last);
}

template<class FwdIt>
segments_encoded_iter<FwdIt>
make_segments_encoded_iter(
    FwdIt first, FwdIt last)
{
    return segments_encoded_iter<
        FwdIt>(first, last);
}

} // detail
} // urls
} // boost

#endif
