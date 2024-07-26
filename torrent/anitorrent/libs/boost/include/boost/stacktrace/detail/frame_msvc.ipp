// Copyright Antony Polukhin, 2016-2024.
//
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_STACKTRACE_DETAIL_FRAME_MSVC_IPP
#define BOOST_STACKTRACE_DETAIL_FRAME_MSVC_IPP

#include <boost/config.hpp>
#ifdef BOOST_HAS_PRAGMA_ONCE
#   pragma once
#endif

#include <boost/stacktrace/frame.hpp>

#include <boost/core/demangle.hpp>
#include <boost/core/noncopyable.hpp>
#include <boost/stacktrace/detail/to_dec_array.hpp>
#include <boost/stacktrace/detail/to_hex_array.hpp>
#include <windows.h>
#include "dbgeng.h"

#include <mutex>

#ifdef BOOST_MSVC
#   pragma comment(lib, "ole32.lib")
#   pragma comment(lib, "Dbgeng.lib")
#endif


#ifdef __CRT_UUID_DECL // for __MINGW32__
#if !defined(__MINGW32__) || \
    (!defined(__clang__) && __GNUC__ < 12) || \
    (defined(__clang__) && __clang_major__ < 16)
    __CRT_UUID_DECL(IDebugClient,0x27fe5639,0x8407,0x4f47,0x83,0x64,0xee,0x11,0x8f,0xb0,0x8a,0xc8)
    __CRT_UUID_DECL(IDebugControl,0x5182e668,0x105e,0x416e,0xad,0x92,0x24,0xef,0x80,0x04,0x24,0xba)
    __CRT_UUID_DECL(IDebugSymbols,0x8c31e98c,0x983a,0x48a5,0x90,0x16,0x6f,0xe5,0xd6,0x67,0xa9,0x50)
#endif
#elif defined(DEFINE_GUID) && !defined(BOOST_MSVC)
    DEFINE_GUID(IID_IDebugClient,0x27fe5639,0x8407,0x4f47,0x83,0x64,0xee,0x11,0x8f,0xb0,0x8a,0xc8);
    DEFINE_GUID(IID_IDebugControl,0x5182e668,0x105e,0x416e,0xad,0x92,0x24,0xef,0x80,0x04,0x24,0xba);
    DEFINE_GUID(IID_IDebugSymbols,0x8c31e98c,0x983a,0x48a5,0x90,0x16,0x6f,0xe5,0xd6,0x67,0xa9,0x50);
#endif



// Testing. Remove later
//#   define __uuidof(x) ::IID_ ## x

namespace boost { namespace stacktrace { namespace detail {

template <class T>
class com_holder: boost::noncopyable {
    T* holder_;

public:
    com_holder() noexcept
        : holder_(0)
    {}

    T* operator->() const noexcept {
        return holder_;
    }

    void** to_void_ptr_ptr() noexcept {
        return reinterpret_cast<void**>(&holder_);
    }

    bool is_inited() const noexcept {
        return !!holder_;
    }

    ~com_holder() noexcept {
        if (holder_) {
            holder_->Release();
        }
    }
};


inline std::string mingw_demangling_workaround(const std::string& s) {
#ifdef BOOST_GCC
    if (s.empty()) {
        return s;
    }

    if (s[0] != '_') {
        return boost::core::demangle(('_' + s).c_str());
    }

    return boost::core::demangle(s.c_str());
#else
    return s;
#endif
}

inline void trim_right_zeroes(std::string& s) {
    // MSVC-9 does not have back() and pop_back() functions in std::string
    while (!s.empty()) {
        const std::size_t last = static_cast<std::size_t>(s.size() - 1);
        if (s[last] != '\0') {
            break;
        }
        s.resize(last);
    }
}

class debugging_symbols: boost::noncopyable {
    static void try_init_com(com_holder< ::IDebugSymbols>& idebug) noexcept {
        com_holder< ::IDebugClient> iclient;
        if (S_OK != ::DebugCreate(__uuidof(IDebugClient), iclient.to_void_ptr_ptr())) {
            return;
        }

        com_holder< ::IDebugControl> icontrol;
        const bool res0 = (S_OK == iclient->QueryInterface(
            __uuidof(IDebugControl),
            icontrol.to_void_ptr_ptr()
        ));
        if (!res0) {
            return;
        }

        const bool res1 = (S_OK == iclient->AttachProcess(
            0,
            ::GetCurrentProcessId(),
            DEBUG_ATTACH_NONINVASIVE | DEBUG_ATTACH_NONINVASIVE_NO_SUSPEND
        ));
        if (!res1) {
            return;
        }

        if (S_OK != icontrol->WaitForEvent(DEBUG_WAIT_DEFAULT, INFINITE)) {
            return;
        }

        // No checking: QueryInterface sets the output parameter to NULL in case of error.
        iclient->QueryInterface(__uuidof(IDebugSymbols), idebug.to_void_ptr_ptr());
    }

#ifndef BOOST_STACKTRACE_USE_WINDBG_CACHED
    static std::mutex& get_mutex_inst() noexcept {
        static std::mutex m;
        return m;
    }

    static com_holder< ::IDebugSymbols>& get_static_debug_inst() noexcept {
        // [class.mfct]: A static local variable or local type in a member function always refers to the same entity, whether
        // or not the member function is inline.
        static com_holder< ::IDebugSymbols> idebug;

        if (!idebug.is_inited()) {
            try_init_com(idebug);
        }

        return idebug;
    }

    std::lock_guard<std::mutex> guard_;
    com_holder< ::IDebugSymbols>& idebug_;
public:
    debugging_symbols() noexcept
        : guard_( get_mutex_inst() )
        , idebug_( get_static_debug_inst() )
    {}
#else

#ifdef BOOST_NO_CXX11_THREAD_LOCAL
#   error Your compiler does not support C++11 thread_local storage. It`s impossible to build with BOOST_STACKTRACE_USE_WINDBG_CACHED.
#endif

    static com_holder< ::IDebugSymbols>& get_thread_local_debug_inst() noexcept {
        // [class.mfct]: A static local variable or local type in a member function always refers to the same entity, whether
        // or not the member function is inline.
        static thread_local com_holder< ::IDebugSymbols> idebug;

        if (!idebug.is_inited()) {
            try_init_com(idebug);
        }

        return idebug;
    }

    com_holder< ::IDebugSymbols>& idebug_;
public:
    debugging_symbols() noexcept
        : idebug_( get_thread_local_debug_inst() )
    {}

#endif // #ifndef BOOST_STACKTRACE_USE_WINDBG_CACHED

    bool is_inited() const noexcept {
        return idebug_.is_inited();
    }

    std::string get_name_impl(const void* addr, std::string* module_name = 0) const {
        std::string result;
        if (!is_inited()) {
            return result;
        }
        const ULONG64 offset = reinterpret_cast<ULONG64>(addr);

        char name[256];
        name[0] = '\0';
        ULONG size = 0;
        bool res = (S_OK == idebug_->GetNameByOffset(
            offset,
            name,
            sizeof(name),
            &size,
            0
        ));

        if (!res && size != 0) {
            result.resize(size);
            res = (S_OK == idebug_->GetNameByOffset(
                offset,
                &result[0],
                static_cast<ULONG>(result.size()),
                &size,
                0
            ));

            // According to https://docs.microsoft.com/en-us/windows-hardware/drivers/ddi/dbgeng/nf-dbgeng-idebugsymbols-getnamebyoffset
            // "This size includes the space for the '\0' terminating character."
            result.resize(size - 1);
        } else if (res) {
            result.assign(name, size - 1);
        }

        if (!res) {
            result.clear();
            return result;
        }

        const std::size_t delimiter = result.find_first_of('!');
        if (module_name) {
            *module_name = result.substr(0, delimiter);
        }

        if (delimiter == std::string::npos) {
            // If 'delimiter' is equal to 'std::string::npos' then we have only module name.
            result.clear();
            return result;
        }

        result = mingw_demangling_workaround(
            result.substr(delimiter + 1)
        );

        return result;
    }

    std::size_t get_line_impl(const void* addr) const noexcept {
        ULONG result = 0;
        if (!is_inited()) {
            return result;
        }

        const bool is_ok = (S_OK == idebug_->GetLineByOffset(
            reinterpret_cast<ULONG64>(addr),
            &result,
            0,
            0,
            0,
            0
        ));

        return (is_ok ? result : 0);
    }

    std::pair<std::string, std::size_t> get_source_file_line_impl(const void* addr) const {
        std::pair<std::string, std::size_t> result;
        if (!is_inited()) {
            return result;
        }
        const ULONG64 offset = reinterpret_cast<ULONG64>(addr);

        char name[256];
        name[0] = 0;
        ULONG size = 0;
        ULONG line_num = 0;
        bool res = (S_OK == idebug_->GetLineByOffset(
            offset,
            &line_num,
            name,
            sizeof(name),
            &size,
            0
        ));

        if (res) {
            result.first = name;
            result.second = line_num;
            return result;
        }

        if (!res && size == 0) {
            return result;
        }

        result.first.resize(size);
        res = (S_OK == idebug_->GetLineByOffset(
            offset,
            &line_num,
            &result.first[0],
            static_cast<ULONG>(result.first.size()),
            &size,
            0
        ));
        trim_right_zeroes(result.first);
        result.second = line_num;

        if (!res) {
            result.first.clear();
            result.second = 0;
        }

        return result;
    }

    void to_string_impl(const void* addr, std::string& res) const {
        if (!is_inited()) {
            return;
        }

        std::string module_name;
        std::string name = this->get_name_impl(addr, &module_name);
        if (!name.empty()) {
            res += name;
        } else {
            res += to_hex_array(addr).data();
        }

        std::pair<std::string, std::size_t> source_line = this->get_source_file_line_impl(addr);
        if (!source_line.first.empty() && source_line.second) {
            res += " at ";
            res += source_line.first;
            res += ':';
            res += boost::stacktrace::detail::to_dec_array(source_line.second).data();
        } else if (!module_name.empty()) {
            res += " in ";
            res += module_name;
        }
    }
};

std::string to_string(const frame* frames, std::size_t size) {
    boost::stacktrace::detail::debugging_symbols idebug;
    if (!idebug.is_inited()) {
        return std::string();
    }

    std::string res;
    res.reserve(64 * size);
    for (std::size_t i = 0; i < size; ++i) {
        if (i < 10) {
            res += ' ';
        }
        res += boost::stacktrace::detail::to_dec_array(i).data();
        res += '#';
        res += ' ';
        idebug.to_string_impl(frames[i].address(), res);
        res += '\n';
    }

    return res;
}

} // namespace detail

std::string frame::name() const {
    boost::stacktrace::detail::debugging_symbols idebug;
    return idebug.get_name_impl(addr_);
}


std::string frame::source_file() const {
    boost::stacktrace::detail::debugging_symbols idebug;
    return idebug.get_source_file_line_impl(addr_).first;
}

std::size_t frame::source_line() const {
    boost::stacktrace::detail::debugging_symbols idebug;
    return idebug.get_line_impl(addr_);
}

std::string to_string(const frame& f) {
    std::string res;

    boost::stacktrace::detail::debugging_symbols idebug;
    idebug.to_string_impl(f.address(), res);
    return res;
}

}} // namespace boost::stacktrace

#endif // BOOST_STACKTRACE_DETAIL_FRAME_MSVC_IPP
