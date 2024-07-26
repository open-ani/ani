#ifndef BOOST_LEAF_CONFIG_TLS_ARRAY_HPP_INCLUDED
#define BOOST_LEAF_CONFIG_TLS_ARRAY_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.
// Copyright (c) 2022 Khalil Estell

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

// LEAF requires thread local storage support for pointers and for uin32_t values.

// This header implements thread local storage for pointers and for unsigned int
// values for platforms that support thread local pointers by index.

namespace boost { namespace leaf {

namespace tls
{
    // The TLS support defined in this header requires the following two
    // functions to be defined elsewhere:
    void * read_void_ptr( int tls_index ) noexcept;
    void write_void_ptr( int tls_index, void * ) noexcept;
}

} }

////////////////////////////////////////

#include <limits>
#include <atomic>
#include <cstdint>
#include <type_traits>

#ifndef BOOST_LEAF_CFG_TLS_INDEX_TYPE
#   define BOOST_LEAF_CFG_TLS_INDEX_TYPE unsigned char
#endif

#ifndef BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX
#   define BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX 0
#endif

static_assert((BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX) >= 0,
    "Bad BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX");

#ifdef BOOST_LEAF_CFG_TLS_ARRAY_SIZE
    static_assert((BOOST_LEAF_CFG_TLS_ARRAY_SIZE) > (BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX),
        "Bad BOOST_LEAF_CFG_TLS_ARRAY_SIZE");
    static_assert((BOOST_LEAF_CFG_TLS_ARRAY_SIZE) - 1 <= std::numeric_limits<BOOST_LEAF_CFG_TLS_INDEX_TYPE>::max(),
        "Bad BOOST_LEAF_CFG_TLS_ARRAY_SIZE");
#endif

////////////////////////////////////////

namespace boost { namespace leaf {

namespace leaf_detail
{
    using atomic_unsigned_int = std::atomic<unsigned int>;
}

namespace tls
{
    template <class=void>
    class BOOST_LEAF_SYMBOL_VISIBLE index_counter
    {
        static int c_;

        static BOOST_LEAF_CFG_TLS_INDEX_TYPE next_() noexcept
        {
            int idx = ++c_;
            BOOST_LEAF_ASSERT(idx > (BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX));
            BOOST_LEAF_ASSERT(idx < (BOOST_LEAF_CFG_TLS_ARRAY_SIZE));
            return idx;
        }

    public:

        template <class T>
        static BOOST_LEAF_CFG_TLS_INDEX_TYPE next() noexcept
        {
            return next_(); // Set breakpoint here to monitor TLS index allocation for T.
        }
    };

    template <class T>
    struct BOOST_LEAF_SYMBOL_VISIBLE tls_index
    {
        static BOOST_LEAF_CFG_TLS_INDEX_TYPE idx;
    };

    template <class T>
    struct BOOST_LEAF_SYMBOL_VISIBLE alloc_tls_index
    {
        static BOOST_LEAF_CFG_TLS_INDEX_TYPE const idx;
    };

    template <class T>
    int index_counter<T>::c_ = BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX;

    template <class T>
    BOOST_LEAF_CFG_TLS_INDEX_TYPE tls_index<T>::idx = BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX;

    template <class T>
    BOOST_LEAF_CFG_TLS_INDEX_TYPE const alloc_tls_index<T>::idx = tls_index<T>::idx = index_counter<>::next<T>();

    ////////////////////////////////////////

    template <class T>
    T * read_ptr() noexcept
    {
        int tls_idx = tls_index<T>::idx;
        if( tls_idx == (BOOST_LEAF_CFG_TLS_ARRAY_START_INDEX) )
            return nullptr;
        --tls_idx;
        return reinterpret_cast<T *>(read_void_ptr(tls_idx));
    }

    template <class T>
    void write_ptr( T * p ) noexcept
    {
        int tls_idx = alloc_tls_index<T>::idx;
        --tls_idx;
        write_void_ptr(tls_idx, p);
        BOOST_LEAF_ASSERT(read_void_ptr(tls_idx) == p);
    }

    ////////////////////////////////////////

    template <class Tag>
    unsigned read_uint() noexcept
    {
        static_assert(sizeof(std::intptr_t) >= sizeof(unsigned), "Incompatible tls_array implementation");
        return (unsigned) (std::intptr_t) (void *) read_ptr<Tag>();
    }

    template <class Tag>
    void write_uint( unsigned x ) noexcept
    {
        static_assert(sizeof(std::intptr_t) >= sizeof(unsigned), "Incompatible tls_array implementation");
        write_ptr<Tag>((Tag *) (void *) (std::intptr_t) x);
    }

    template <class Tag>
    void uint_increment() noexcept
    {
        write_uint<Tag>(read_uint<Tag>() + 1);
    }

    template <class Tag>
    void uint_decrement() noexcept
    {
        write_uint<Tag>(read_uint<Tag>() - 1);
    }
}

} }

#endif
