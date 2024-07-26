// Copyright Antony Polukhin, 2016-2024.
//
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_STACKTRACE_DETAIL_ADDR_BASE_HPP
#define BOOST_STACKTRACE_DETAIL_ADDR_BASE_HPP

#include <boost/config.hpp>
#ifdef BOOST_HAS_PRAGMA_ONCE
#   pragma once
#endif

#include <fstream>
#include <sstream>
#include <cstdint>
#include <cstdlib>

namespace boost { namespace stacktrace { namespace detail {

struct mapping_entry_t {
    uintptr_t start = 0;
    uintptr_t end = 0;
    uintptr_t offset_from_base = 0;

    inline bool contains_addr(const void* addr) const {
        uintptr_t addr_uint = reinterpret_cast<uintptr_t>(addr);
        return addr_uint >= start && addr_uint < end;
    }
};

inline uintptr_t hex_str_to_int(const std::string& str) {
    uintptr_t out;
    std::stringstream ss;
    ss << std::hex << str;
    ss >> out;
    if(ss.eof() && !ss.fail()) { // whole stream read, with no errors
        return out;
    } else {
        throw std::invalid_argument(std::string("can't convert '") + str + "' to hex");
    }
}

// parse line from /proc/<id>/maps
// format:
// 7fb60d1ea000-7fb60d20c000 r--p 00000000 103:02 120327460                 /usr/lib/libc.so.6
// only parts 0 and 2 are interesting, these are:
//  0. mapping address range
//  2. mapping offset from base
inline mapping_entry_t parse_proc_maps_line(const std::string& line) {
    std::string mapping_range_str, permissions_str, offset_from_base_str;
    std::istringstream line_stream(line);
    if(!std::getline(line_stream, mapping_range_str, ' ') ||
        !std::getline(line_stream, permissions_str, ' ') ||
        !std::getline(line_stream, offset_from_base_str, ' ')) {
        return mapping_entry_t{};
    }
    std::string mapping_start_str, mapping_end_str;
    std::istringstream mapping_range_stream(mapping_range_str);
    if(!std::getline(mapping_range_stream, mapping_start_str, '-') ||
        !std::getline(mapping_range_stream, mapping_end_str)) {
        return mapping_entry_t{};
    }
    mapping_entry_t mapping{};
    try {
        mapping.start = hex_str_to_int(mapping_start_str);
        mapping.end = hex_str_to_int(mapping_end_str);
        mapping.offset_from_base = hex_str_to_int(offset_from_base_str);
        return mapping;
    } catch(std::invalid_argument& e) {
        return mapping_entry_t{};
    }
}

inline uintptr_t get_own_proc_addr_base(const void* addr) {
    std::ifstream maps_file("/proc/self/maps");
    for (std::string line; std::getline(maps_file, line); ) {
        const mapping_entry_t mapping = parse_proc_maps_line(line);
        if (mapping.contains_addr(addr)) {
            return mapping.start - mapping.offset_from_base;
        }
    }
    return 0;
}

}}} // namespace boost::stacktrace::detail

#endif // BOOST_STACKTRACE_DETAIL_ADDR_BASE_HPP
