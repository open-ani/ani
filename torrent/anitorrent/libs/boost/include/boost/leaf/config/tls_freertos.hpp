#ifndef BOOST_LEAF_CONFIG_TLS_FREERTOS_HPP_INCLUDED
#define BOOST_LEAF_CONFIG_TLS_FREERTOS_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.
// Copyright (c) 2022 Khalil Estell

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

// LEAF requires thread local storage support for pointers and for uin32_t values.

// This header implements "thread local" storage via FreeTOS functions
// pvTaskGetThreadLocalStoragePointer / pvTaskSetThreadLocalStoragePointer

#include <task.h>

#ifndef BOOST_LEAF_USE_TLS_ARRAY
#   define BOOST_LEAF_USE_TLS_ARRAY
#endif

#ifndef BOOST_LEAF_CFG_TLS_ARRAY_SIZE
#   define BOOST_LEAF_CFG_TLS_ARRAY_SIZE configNUM_THREAD_LOCAL_STORAGE_POINTERS
#endif

static_assert((BOOST_LEAF_CFG_TLS_ARRAY_SIZE) <= configNUM_THREAD_LOCAL_STORAGE_POINTERS,
    "Bad BOOST_LEAF_CFG_TLS_ARRAY_SIZE");

namespace boost { namespace leaf {

namespace tls
{
    // See https://www.freertos.org/thread-local-storage-pointers.html.

    inline void * read_void_ptr( int tls_index ) noexcept
    {
        return pvTaskGetThreadLocalStoragePointer(0, tls_index);
    }

    inline void write_void_ptr( int tls_index, void * p ) noexcept
    {
        vTaskSetThreadLocalStoragePointer(0, tls_index, p);
    }
}

} }

#endif
