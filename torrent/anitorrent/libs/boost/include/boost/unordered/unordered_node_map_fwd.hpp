
// Copyright (C) 2022 Christian Mazakas
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_UNORDERED_NODE_MAP_FWD_HPP_INCLUDED
#define BOOST_UNORDERED_NODE_MAP_FWD_HPP_INCLUDED

#include <boost/config.hpp>
#if defined(BOOST_HAS_PRAGMA_ONCE)
#pragma once
#endif

#include <boost/container_hash/hash_fwd.hpp>
#include <functional>
#include <memory>

namespace boost {
  namespace unordered {
    template <class Key, class T, class Hash = boost::hash<Key>,
      class KeyEqual = std::equal_to<Key>,
      class Allocator = std::allocator<std::pair<const Key, T> > >
    class unordered_node_map;

    template <class Key, class T, class Hash, class KeyEqual, class Allocator>
    bool operator==(
      unordered_node_map<Key, T, Hash, KeyEqual, Allocator> const& lhs,
      unordered_node_map<Key, T, Hash, KeyEqual, Allocator> const& rhs);

    template <class Key, class T, class Hash, class KeyEqual, class Allocator>
    bool operator!=(
      unordered_node_map<Key, T, Hash, KeyEqual, Allocator> const& lhs,
      unordered_node_map<Key, T, Hash, KeyEqual, Allocator> const& rhs);

    template <class Key, class T, class Hash, class KeyEqual, class Allocator>
    void swap(unordered_node_map<Key, T, Hash, KeyEqual, Allocator>& lhs,
      unordered_node_map<Key, T, Hash, KeyEqual, Allocator>& rhs)
      noexcept(noexcept(lhs.swap(rhs)));
  } // namespace unordered

  using boost::unordered::unordered_node_map;
} // namespace boost

#endif
