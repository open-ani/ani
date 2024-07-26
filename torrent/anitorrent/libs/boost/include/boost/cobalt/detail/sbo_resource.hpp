//
// Copyright (c) 2023 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_SBO_BUFFER_RESOURCE_HPP
#define BOOST_COBALT_DETAIL_SBO_BUFFER_RESOURCE_HPP

#include <boost/cobalt/config.hpp>

namespace boost::cobalt::detail
{

struct sbo_resource
#if !defined(BOOST_COBALT_NO_PMR)
  final : pmr::memory_resource
#endif
{
 private:
  struct block_
  {
    void* p{nullptr};
    std::size_t avail{0u};
    std::size_t size{0u};
    bool fragmented{false};
  };

  block_ buffer_;
#if !defined(BOOST_COBALT_NO_PMR)
  pmr::memory_resource * upstream_;
#endif
  constexpr std::size_t align_as_max_(std::size_t size)
  {
    auto diff = size % alignof(std::max_align_t );
    if (diff > 0)
      return size + alignof(std::max_align_t) - diff;
    else
      return size;
  }
  constexpr void align_as_max_()
  {
    const auto buffer = static_cast<char*>(buffer_.p) - static_cast<char*>(nullptr);
    const auto diff = buffer % alignof(std::max_align_t );
    if (diff > 0)
    {
      const auto padding = alignof(std::max_align_t) - diff;
      buffer_.p = static_cast<void*>(static_cast<char*>(nullptr) + buffer + padding);
      if (padding >= buffer_.size) [[unlikely]]
      {
        buffer_.size = 0;
        buffer_.avail = 0;
      }
      else
      {
        buffer_.size -= padding;
        buffer_.avail -= padding;
      }

    }
  }

 public:
  constexpr sbo_resource(void * buffer, std::size_t size
#if !defined(BOOST_COBALT_NO_PMR)
                        , pmr::memory_resource * upstream = pmr::get_default_resource()
#endif
      ) : buffer_{buffer, size, size, false}
#if !defined(BOOST_COBALT_NO_PMR)
      , upstream_(upstream)
#endif
  {
    align_as_max_();
  }

#if defined(BOOST_COBALT_NO_PMR)
  constexpr sbo_resource() : buffer_{nullptr, 0u, 0u, false} {}

#else
  constexpr sbo_resource(pmr::memory_resource * upstream = pmr::get_default_resource())
      : buffer_{nullptr, 0u, 0u, false}, upstream_(upstream) {}
#endif

  ~sbo_resource() = default;

  constexpr void * do_allocate(std::size_t size, std::size_t align)
#if !defined(BOOST_COBALT_NO_PMR)
  override
#endif
  {
    const auto sz = align_as_max_(size);
    if (sz <= buffer_.avail && !buffer_.fragmented) [[likely]]
    {
      auto p = static_cast<char*>(buffer_.p) + buffer_.size - buffer_.avail;
      buffer_.avail -= sz;
      return p;
    }
    else
#if !defined(BOOST_COBALT_NO_PMR)
      return upstream_->allocate(size, align);
#else
      return operator new(size, std::align_val_t(align));
#endif

  }

  constexpr void do_deallocate(void * p, std::size_t size, std::size_t align)
#if !defined(BOOST_COBALT_NO_PMR)
      override
#endif
  {
    auto begin = static_cast<char*>(static_cast<char*>(buffer_.p));
    auto end = begin + buffer_.size;
    auto itr = static_cast<char*>(p);

    if(begin <= itr && itr < end) [[likely]]
    {
      const auto sz = align_as_max_(size);
      const auto used_mem_end = end - buffer_.avail;
      const auto dealloc_end = itr + sz;
      if (used_mem_end != dealloc_end )
        buffer_.fragmented = true;
      buffer_.avail += sz;
      if (buffer_.avail == buffer_.size)
        buffer_.fragmented = false;
    }
    else
    {
#if !defined(BOOST_COBALT_NO_PMR)
      upstream_->deallocate(p, size, align);
#else
  #if defined(__cpp_sized_deallocation)
      operator delete(p, size, std::align_val_t(align));
  #else
      operator delete(p, std::align_val_t(align));
  #endif
#endif
    }
  }


#if !defined(BOOST_COBALT_NO_PMR)
  constexpr bool do_is_equal(memory_resource const& other) const noexcept override
  {
    return this == &other;
  }
#endif
};

inline sbo_resource * get_null_sbo_resource()
{
  static sbo_resource empty_resource;
  return &empty_resource;
}

template<typename T>
struct sbo_allocator
{
  template<typename U>
  sbo_allocator(sbo_allocator<U> alloc) : resource_(alloc.resource_)
  {

  }
  using value_type                             = T;
  using size_type                              = std::size_t;
  using difference_type                        = std::ptrdiff_t;
  using propagate_on_container_move_assignment = std::true_type;

  [[nodiscard]] constexpr T* allocate( std::size_t n )
  {
    BOOST_ASSERT(resource_);
    return static_cast<T*>(resource_->do_allocate(sizeof(T) * n, alignof(T)));
  }

  constexpr void deallocate( T* p, std::size_t n )
  {
    BOOST_ASSERT(resource_);
    resource_->do_deallocate(p, sizeof(T) * n, alignof(T));
  }
  sbo_allocator(sbo_resource * resource) : resource_(resource) {}
 private:
  template<typename>
  friend struct sbo_allocator;

  sbo_resource * resource_{nullptr};
};

}

#endif //BOOST_COBALT_DETAIL_SBO_BUFFER_RESOURCE_HPP
