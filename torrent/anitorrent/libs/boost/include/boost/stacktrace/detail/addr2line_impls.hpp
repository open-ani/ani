// Copyright Antony Polukhin, 2016-2024.
//
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_STACKTRACE_DETAIL_ADDR2LINE_IMPLS_HPP
#define BOOST_STACKTRACE_DETAIL_ADDR2LINE_IMPLS_HPP

#include <boost/config.hpp>
#ifdef BOOST_HAS_PRAGMA_ONCE
#   pragma once
#endif

#include <boost/stacktrace/detail/addr_base.hpp>
#include <boost/stacktrace/detail/to_hex_array.hpp>
#include <boost/stacktrace/detail/to_dec_array.hpp>
#include <boost/stacktrace/detail/try_dec_convert.hpp>
#include <boost/core/demangle.hpp>
#include <cstdio>

#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>


namespace boost { namespace stacktrace { namespace detail {


#if defined(BOOST_STACKTRACE_ADDR2LINE_LOCATION) && !defined(BOOST_NO_CXX11_CONSTEXPR)

constexpr bool is_abs_path(const char* path) noexcept {
    return *path != '\0' && (
        *path == ':' || *path == '/' || is_abs_path(path + 1)
    );
}

#endif

class addr2line_pipe {
    ::FILE* p;
    ::pid_t pid;

public:
    explicit addr2line_pipe(const char *flag, const char* exec_path, const char* addr) noexcept
        : p(0)
        , pid(0)
    {
        int pdes[2];
        #ifdef BOOST_STACKTRACE_ADDR2LINE_LOCATION
        char prog_name[] = BOOST_STRINGIZE( BOOST_STACKTRACE_ADDR2LINE_LOCATION );
        #if !defined(BOOST_NO_CXX11_CONSTEXPR) && !defined(BOOST_NO_CXX11_STATIC_ASSERT)
        static_assert(
            boost::stacktrace::detail::is_abs_path( BOOST_STRINGIZE( BOOST_STACKTRACE_ADDR2LINE_LOCATION ) ),
            "BOOST_STACKTRACE_ADDR2LINE_LOCATION must be an absolute path"
        );
        #endif

        #else
        char prog_name[] = "/usr/bin/addr2line";
        #endif

        char* argp[] = {
            prog_name,
            const_cast<char*>(flag),
            const_cast<char*>(exec_path),
            const_cast<char*>(addr),
            0
        };

        if (::pipe(pdes) < 0) {
            return;
        }

        pid = ::fork();
        switch (pid) {
        case -1:
            // Failed...
            ::close(pdes[0]);
            ::close(pdes[1]);
            return;

        case 0:
            // We are the child.
            ::close(STDERR_FILENO);
            ::close(pdes[0]);
            if (pdes[1] != STDOUT_FILENO) {
                ::dup2(pdes[1], STDOUT_FILENO);
            }

            // Do not use `execlp()`, `execvp()`, and `execvpe()` here!
            // `exec*p*` functions are vulnerable to PATH variable evaluation attacks.
            ::execv(prog_name, argp);
            ::_exit(127);
        }

        p = ::fdopen(pdes[0], "r");
        ::close(pdes[1]);
    }

    operator ::FILE*() const noexcept {
        return p;
    }

    ~addr2line_pipe() noexcept {
        if (p) {
            ::fclose(p);
            int pstat = 0;
            ::kill(pid, SIGKILL);
            ::waitpid(pid, &pstat, 0);
        }
    }
};

inline std::string addr2line(const char* flag, const void* addr) {
    std::string res;

    boost::stacktrace::detail::location_from_symbol loc(addr);
    if (!loc.empty()) {
        res = loc.name();
    } else {
        res.resize(16);
        int rlin_size = ::readlink("/proc/self/exe", &res[0], res.size() - 1);
        while (rlin_size == static_cast<int>(res.size() - 1)) {
            res.resize(res.size() * 4);
            rlin_size = ::readlink("/proc/self/exe", &res[0], res.size() - 1);
        }
        if (rlin_size == -1) {
            res.clear();
            return res;
        }
        res.resize(rlin_size);
    }

    addr2line_pipe p(flag, res.c_str(), to_hex_array(addr).data());
    res.clear();

    if (!p) {
        return res;
    }

    char data[32];
    while (!::feof(p)) {
        if (::fgets(data, sizeof(data), p)) {
            res += data;
        } else {
            break;
        }
    }

    // Trimming
    while (!res.empty() && (res[res.size() - 1] == '\n' || res[res.size() - 1] == '\r')) {
        res.erase(res.size() - 1);
    }

    return res;
}

inline std::string source_location(const void* addr, bool position_independent) {
    uintptr_t addr_base = 0;
    if (position_independent) {
        addr_base = boost::stacktrace::detail::get_own_proc_addr_base(addr);
    }
    const void* offset = reinterpret_cast<void*>(reinterpret_cast<uintptr_t>(addr) - addr_base);
    std::string source_line = boost::stacktrace::detail::addr2line("-Cpe", reinterpret_cast<const void*>(offset));
    if (source_line.empty() || source_line[0] == '?') {
        return "";
    }

    return source_line;
}

struct to_string_using_addr2line {
    std::string res;
    void prepare_function_name(const void* addr) {
        res = boost::stacktrace::frame(addr).name();
    }

    bool prepare_source_location(const void* addr) {
        // general idea in all addr2line uses:
        // in each case:
        //  - try to resolve whole address as if it was a non-pie binary
        //  - if that didn't work, try to resolve just an offset from binary base address
        // this is needed because:
        //  - in pie binaries just passing an address to addr2line won't work (it needs an offset in this case)
        //  - in non-pie binaries whole address is needed (offset won't work)
        //  - there is no easy way to test if binary is position independent (that I know of)
        std::string source_line = boost::stacktrace::detail::source_location(addr, false);
        if(source_line.empty()) {
            source_line = boost::stacktrace::detail::source_location(addr, true);
        }

        if (!source_line.empty()) {
            res += " at ";
            res += source_line;
            return true;
        }

        return false;
    }
};

template <class Base> class to_string_impl_base;
typedef to_string_impl_base<to_string_using_addr2line> to_string_impl;

inline std::string name(const void* addr, bool position_independent) {
    uintptr_t addr_base = 0;
    if(position_independent){
        addr_base = boost::stacktrace::detail::get_own_proc_addr_base(addr);
    }
    const void* offset = reinterpret_cast<void*>(reinterpret_cast<uintptr_t>(addr) - addr_base);
    std::string res = boost::stacktrace::detail::addr2line("-fe", offset);
    res = res.substr(0, res.find_last_of('\n'));
    res = boost::core::demangle(res.c_str());

    if (res == "??") {
        res.clear();
    }

    return res;
}

inline std::string name_impl(const void* addr) {
    std::string res = boost::stacktrace::detail::name(addr, false);
    if (res.empty()) {
        res = boost::stacktrace::detail::name(addr, true);
    }

    return res;
}

inline std::string source_file(const void* addr, bool position_independent) {
    std::string res;
    uintptr_t addr_base = 0;
    if(position_independent){
        addr_base = boost::stacktrace::detail::get_own_proc_addr_base(addr);
    }
    const void* offset = reinterpret_cast<void*>(reinterpret_cast<uintptr_t>(addr) - addr_base);
    res = boost::stacktrace::detail::addr2line("-e", offset);
    res = res.substr(0, res.find_last_of(':'));
    if (res == "??") {
        res.clear();
    }

    return res;
}

inline std::size_t source_line(const void* addr, bool position_independent) {
    std::size_t line_num = 0;
    uintptr_t addr_base = 0;
    if(position_independent){
        addr_base = boost::stacktrace::detail::get_own_proc_addr_base(addr);
    }
    const void* offset = reinterpret_cast<void*>(reinterpret_cast<uintptr_t>(addr) - addr_base);
    std::string res = boost::stacktrace::detail::addr2line("-e", offset);
    const std::size_t last = res.find_last_of(':');
    if (last == std::string::npos) {
        return 0;
    }
    res = res.substr(last + 1);

    if (!boost::stacktrace::detail::try_dec_convert(res.c_str(), line_num)) {
        return 0;
    }

    return line_num;
}

} // namespace detail


std::string frame::source_file() const {
    std::string res = boost::stacktrace::detail::source_file(addr_, false);
    if (res.empty()) {
        res = boost::stacktrace::detail::source_file(addr_, true);
    }

    return res;
}

std::size_t frame::source_line() const {
    std::size_t line_num = boost::stacktrace::detail::source_line(addr_, false);
    if (line_num == 0) {
        line_num = boost::stacktrace::detail::source_line(addr_, true);
    }

    return line_num;
}


}} // namespace boost::stacktrace

#endif // BOOST_STACKTRACE_DETAIL_ADDR2LINE_IMPLS_HPP
