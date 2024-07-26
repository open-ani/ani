// Boost.Geometry

// Copyright (c) 2021-2023, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_INDEX_DETAIL_MINMAX_HEAP_HPP
#define BOOST_GEOMETRY_INDEX_DETAIL_MINMAX_HEAP_HPP

#include <iterator>
#include <limits.h>
#include <type_traits>
#include <utility>

#ifdef _MSC_VER // msvc and clang-win
#include <intrin.h>
#endif

namespace boost { namespace geometry { namespace index { namespace detail
{

// Resources:
// https://en.wikipedia.org/wiki/Min-max_heap
// http://akira.ruc.dk/~keld/teaching/algoritmedesign_f03/Artikler/02/Atkinson86.pdf
// https://probablydance.com/2020/08/31/on-modern-hardware-the-min-max-heap-beats-a-binary-heap/
// https://stackoverflow.com/questions/6531543/efficient-implementation-of-binary-heaps
// https://stackoverflow.com/questions/994593/how-to-do-an-integer-log2-in-c

namespace minmax_heap_detail
{

template <typename T>
using bitsize = std::integral_constant<std::size_t, sizeof(T) * CHAR_BIT>;

template <typename It>
using diff_t = typename std::iterator_traits<It>::difference_type;
template <typename It>
using val_t = typename std::iterator_traits<It>::value_type;

// TODO: In C++20 use std::bit_width()

template <typename T, std::enable_if_t<!std::is_integral<T>::value || (bitsize<T>::value != 32 && bitsize<T>::value != 64), int> = 0>
inline int level(T i)
{
    ++i;
    int r = 0;
    while (i >>= 1) { ++r; }
    return r;
}

//template <typename T>
//inline int level(T i)
//{
//    using std::log2;
//    return int(log2(i + 1));
//}

#ifdef _MSC_VER // msvc and clang-win

template <typename T, std::enable_if_t<std::is_integral<T>::value && bitsize<T>::value == 32, int> = 0>
inline int level(T i)
{
    unsigned long r = 0;
    _BitScanReverse(&r, (unsigned long)(i + 1));
    return int(r);
}

template <typename T, std::enable_if_t<std::is_integral<T>::value && bitsize<T>::value == 64, int> = 0>
inline int level(T i)
{
    unsigned long r = 0;
#ifdef _WIN64
    _BitScanReverse64(&r, (unsigned long long)(i + 1));
#else
    if (_BitScanReverse(&r, (unsigned long)((i + 1) >> 32))) { r += 32; }
    else { _BitScanReverse(&r, (unsigned long)(i + 1)); }
#endif
    return int(r);
}

#elif defined(__clang__) || defined(__GNUC__)
// Only available in gcc-10 and clang-10
//#elif defined(__has_builtin) && __has_builtin(__builtin_clzl) && __has_builtin(__builtin_clzll)

template <typename T, std::enable_if_t<std::is_integral<T>::value && bitsize<T>::value == 32, int> = 0>
inline int level(T i)
{
    return 31 - __builtin_clzl((unsigned long)(i + 1));
}

template <typename T, std::enable_if_t<std::is_integral<T>::value && bitsize<T>::value == 64, int> = 0>
inline int level(T i)
{
    return 63 - __builtin_clzll((unsigned long long)(i + 1));
}

#else

template <typename T, std::enable_if_t<std::is_integral<T>::value && bitsize<T>::value == 32, int> = 0>
inline int level(T i)
{
    ++i;
    int r = 0;
    if (i >= 65536) { r += 16; i >>= 16; }
    if (i >= 256) { r += 8; i >>= 8; }
    if (i >= 16) { r += 4; i >>= 4; }
    if (i >= 4) { r += 2; i >>= 2; }
    if (i >= 2) { r += 1; i >>= 1; }
    return r;
}

template <typename T, std::enable_if_t<std::is_integral<T>::value && bitsize<T>::value == 64, int> = 0>
inline int level(T i)
{
    ++i;
    int r = 0;
    if (i >= 4294967296ll) { r += 32; i >>= 32; }
    if (i >= 65536ll) { r += 16; i >>= 16; }
    if (i >= 256ll) { r += 8; i >>= 8; }
    if (i >= 16ll) { r += 4; i >>= 4; }
    if (i >= 4ll) { r += 2; i >>= 2; }
    if (i >= 2ll) { r += 1; i >>= 1; }
    return r;
}

#endif


// min/max functions only differ in the order of arguments in comp
struct min_call
{
    template <typename Compare, typename T1, typename T2>
    bool operator()(Compare&& comp, T1 const& v1, T2 const& v2) const
    {
        return comp(v1, v2);
    }
};

struct max_call
{
    template <typename Compare, typename T1, typename T2>
    bool operator()(Compare&& comp, T1 const& v1, T2 const& v2) const
    {
        return comp(v2, v1);
    }
};


template <typename Call, typename It, typename Compare>
inline void push_heap2(It first, diff_t<It> c, val_t<It> val, Compare comp)
{
    while (c > 2)
    {
        diff_t<It> const g = (c - 3) >> 2; // grandparent index
        if (! Call()(comp, val, *(first + g)))
        {
            break;
        }
        *(first + c) = std::move(*(first + g));
        c = g;
    }

    *(first + c) = std::move(val);
}

template <typename MinCall, typename MaxCall, typename It, typename Compare>
inline void push_heap1(It first, diff_t<It> c, val_t<It> val, Compare comp)
{
    diff_t<It> const p = (c - 1) >> 1; // parent index
    if (MinCall()(comp, *(first + p), val))
    {
        *(first + c) = std::move(*(first + p));
        return push_heap2<MaxCall>(first, p, std::move(val), comp);
    }
    else
    {
        return push_heap2<MinCall>(first, c, std::move(val), comp);
    }
}

template <typename MinCall, typename MaxCall, typename It, typename Compare>
inline void push_heap(It first, It last, Compare comp)
{
    diff_t<It> const size = last - first;
    if (size < 2)
    {
        return;
    }

    diff_t<It> c = size - 1; // back index
    val_t<It> val = std::move(*(first + c));
    if (level(c) % 2 == 0) // is min level
    {
        push_heap1<MinCall, MaxCall>(first, c, std::move(val), comp);
    }
    else
    {
        push_heap1<MaxCall, MinCall>(first, c, std::move(val), comp);
    }
}


template <typename Call, typename It, typename Compare>
inline diff_t<It> pick_grandchild4(It first, diff_t<It> f, Compare comp)
{
    It it = first + f;
    diff_t<It> m1 = Call()(comp, *(it), *(it + 1)) ? f : f + 1;
    diff_t<It> m2 = Call()(comp, *(it + 2), *(it + 3)) ? f + 2 : f + 3;
    return Call()(comp, *(first + m1), *(first + m2)) ? m1 : m2;
}

//template <typename Call, typename It, typename Compare>
//inline diff_t<It> pick_descendant(It first, diff_t<It> f, diff_t<It> l, Compare comp)
//{
//    diff_t<It> m = f;
//    for (++f; f != l; ++f)
//    {
//        if (Call()(comp, *(first + f), *(first + m)))
//        {
//            m = f;
//        }
//    }
//    return m;
//}

template <typename Call, typename It, typename Compare>
inline void pop_heap1(It first, diff_t<It> p, diff_t<It> size, val_t<It> val, Compare comp)
{
    if (size >= 7) // grandparent of 4 grandchildren is possible
    {
        diff_t<It> const last_g = (size - 3) >> 2; // grandparent of the element behind back
        while (p < last_g) // p is grandparent of 4 grandchildren
        {
            diff_t<It> const ll = 4 * p + 3;
            diff_t<It> const m = pick_grandchild4<Call>(first, ll, comp);

            if (! Call()(comp, *(first + m), val))
            {
                break;
            }

            *(first + p) = std::move(*(first + m));

            diff_t<It> par = (m - 1) >> 1;
            if (Call()(comp, *(first + par), val))
            {
                using std::swap;
                swap(*(first + par), val);
            }

            p = m;
        }
    }

    if (size >= 2 && p <= ((size - 2) >> 1)) // at least one child
    {
        diff_t<It> const l = 2 * p + 1;
        diff_t<It> m = l; // left child
        if (size >= 3 && p <= ((size - 3) >> 1)) // at least two children
        {
            // m = left child
            diff_t<It> m2 = l + 1; // right child
            if (size >= 4 && p <= ((size - 4) >> 2)) // at least two children and one grandchild
            {
                diff_t<It> const ll = 2 * l + 1;
                m = ll; // left most grandchild
                // m2 = right child
                if (size >= 5 && p <= ((size - 5) >> 2)) // at least two children and two grandchildren
                {
                    m = Call()(comp, *(first + ll), *(first + ll + 1)) ? ll : (ll + 1); // greater of the left grandchildren
                    // m2 = right child
                    if (size >= 6 && p <= ((size - 6) >> 2)) // at least two children and three grandchildren
                    {
                        // m = greater of the left grandchildren
                        m2 = ll + 2; // third grandchild
                    }
                }
            }

            m = Call()(comp, *(first + m), *(first + m2)) ? m : m2;
        }

        if (Call()(comp, *(first + m), val))
        {
            *(first + p) = std::move(*(first + m));

            if (m >= 3 && p <= ((m - 3) >> 2)) // is p grandparent of m
            {
                diff_t<It> par = (m - 1) >> 1;
                if (Call()(comp, *(first + par), val))
                {
                    using std::swap;
                    swap(*(first + par), val);
                }
            }

            p = m;
        }
    }

    *(first + p) = std::move(val);
}

template <typename MinCall, typename MaxCall, typename It, typename Compare>
inline void pop_heap(It first, It el, It last, Compare comp)
{
    diff_t<It> size = last - first;
    if (size < 2)
    {
        return;
    }

    --last;
    val_t<It> val = std::move(*last);
    *last = std::move(*el);

    // Ignore the last element
    --size;

    diff_t<It> p = el - first;
    if (level(p) % 2 == 0) // is min level
    {
        pop_heap1<MinCall>(first, p, size, std::move(val), comp);
    }
    else
    {
        pop_heap1<MaxCall>(first, p, size, std::move(val), comp);
    }
}

template <typename MinCall, typename MaxCall, typename It, typename Compare>
inline void make_heap(It first, It last, Compare comp)
{
    diff_t<It> size = last - first;
    diff_t<It> p = size / 2;
    if (p <= 0)
    {
        return;
    }

    int level_p = level(p - 1);
    diff_t<It> level_f = (diff_t<It>(1) << level_p) - 1;
    while (p > 0)
    {
        --p;
        val_t<It> val = std::move(*(first + p));
        if (level_p % 2 == 0) // is min level
        {
            pop_heap1<MinCall>(first, p, size, std::move(val), comp);
        }
        else
        {
            pop_heap1<MaxCall>(first, p, size, std::move(val), comp);
        }

        if (p == level_f)
        {
            --level_p;
            level_f >>= 1;
        }
    }
}

template <typename Call, typename It, typename Compare>
inline bool is_heap(It first, It last, Compare comp)
{
    diff_t<It> const size = last - first;
    diff_t<It> pow2 = 4;
    bool is_min_level = false;
    for (diff_t<It> i = 1; i < size; ++i)
    {
        if (i == pow2 - 1)
        {
            pow2 *= 2;
            is_min_level = ! is_min_level;
        }

        diff_t<It> const p = (i - 1) >> 1;
        if (is_min_level)
        {
            if (Call()(comp, *(first + p), *(first + i)))
            {
                return false;
            }
        }
        else
        {
            if (Call()(comp, *(first + i), *(first + p)))
            {
                return false;
            }
        }

        if (i >= 3)
        {
            diff_t<It> const g = (p - 1) >> 1;
            if (is_min_level)
            {
                if (Call()(comp, *(first + i), *(first + g)))
                {
                    return false;
                }
            }
            else
            {
                if (Call()(comp, *(first + g), *(first + i)))
                {
                    return false;
                }
            }
        }
    }
    return true;
}

template <typename Call, typename It, typename Compare>
inline It bottom_heap(It first, It last, Compare comp)
{
    diff_t<It> const size = last - first;
    return size <= 1 ? first :
           size == 2 ? (first + 1) :
           Call()(comp, *(first + 1), *(first + 2)) ? (first + 2) : (first + 1);
}

} // namespace minmax_heap_detail


template <typename It, typename Compare>
inline void push_minmax_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    minmax_heap_detail::push_heap<min_call, max_call>(first, last, comp);
}

template <typename It>
inline void push_minmax_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    minmax_heap_detail::push_heap<min_call, max_call>(first, last, std::less<>());
}

template <typename It, typename Compare>
inline void pop_top_minmax_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    pop_heap<min_call, max_call>(first, first, last, comp);
}

template <typename It>
inline void pop_top_minmax_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    pop_heap<min_call, max_call>(first, first, last, std::less<>());
}

template <typename It, typename Compare>
inline void pop_bottom_minmax_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    It bottom = minmax_heap_detail::bottom_heap<min_call>(first, last, comp);
    pop_heap<min_call, max_call>(first, bottom, last, comp);
}

template <typename It>
inline void pop_bottom_minmax_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    auto&& comp = std::less<>();
    It bottom = minmax_heap_detail::bottom_heap<min_call>(first, last, comp);
    pop_heap<min_call, max_call>(first, bottom, last, comp);
}

template <typename It, typename Compare>
inline void make_minmax_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::make_heap<min_call, max_call>(first, last, comp);
}

template <typename It>
inline void make_minmax_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::make_heap<min_call, max_call>(first, last, std::less<>());
}

template <typename It, typename Compare>
inline bool is_minmax_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::is_heap<min_call>(first, last, comp);
}

template <typename It>
inline bool is_minmax_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::is_heap<min_call>(first, last, std::less<>());
}

template <typename It, typename Compare>
inline decltype(auto) bottom_minmax_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    return *minmax_heap_detail::bottom_heap<min_call>(first, last, comp);
}

template <typename It>
inline decltype(auto) bottom_minmax_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    return *minmax_heap_detail::bottom_heap<min_call>(first, last, std::less<>());
}


}}}} // namespace boost::geometry::index::detail

#endif // BOOST_GEOMETRY_INDEX_DETAIL_MINMAX_HEAP_HPP
