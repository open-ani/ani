//
// based on boost.json
//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
// Copyright (c) 2023 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_MONOTONIC_BUFFER_RESOURCE_HPP
#define BOOST_COBALT_DETAIL_MONOTONIC_BUFFER_RESOURCE_HPP

#include <boost/cobalt/config.hpp>
#include <new>

namespace boost::cobalt::detail
{

struct monotonic_resource
{
 private:
  struct block_
  {
    void* p;
    std::size_t avail;
    std::size_t size;
    std::align_val_t aligned;
    block_* next;

  };

  block_ buffer_;
  block_* head_ = &buffer_;
  std::size_t chunk_size_{buffer_.size};
 public:
  constexpr monotonic_resource(void * buffer, std::size_t size)
      : buffer_{buffer, size, size, std::align_val_t{0u}, nullptr} {}

  constexpr monotonic_resource(std::size_t chunk_size = 1024)
      : buffer_{nullptr, 0u, 0u, std::align_val_t{0u}, nullptr}, chunk_size_(chunk_size) {}


  monotonic_resource(monotonic_resource && lhs) noexcept = delete;
  constexpr ~monotonic_resource()
  {
    if (head_ != &buffer_)
      release();
  }
  constexpr void release()
  {
    head_ = &buffer_;
    auto nx = buffer_.next;
    head_->next = nullptr;
    head_->avail = head_->size;
    while (nx != nullptr)
    {
      auto p = nx;
      nx = nx->next;
#if defined(__cpp_sized_deallocation)
      const auto size = sizeof(block_) + p->size;
      operator delete(p->p, size, p->aligned);
#else
      operator delete(p->p, p->aligned);
#endif
    }
  }

  constexpr void * allocate(std::size_t size, std::align_val_t align_ = std::align_val_t(alignof(std::max_align_t)))
  {
    const auto align = (std::max)(static_cast<std::size_t>(align_), alignof(block_));
    // let's say size = 11, and align is 8, that leaves us with 3
    {
      const auto align_offset = size % align ;
      // padding is 5
      const auto padding = align  - align_offset;
      const auto needed_size = size + padding;
      if (needed_size <= head_->avail) // fits, but we need to check alignment too
      {
        const auto offset = head_->size - head_->avail;
        auto pp = static_cast<char*>(head_->p) + offset + padding;
        head_->avail -= needed_size;
        return pp; // done
      }
    }

    // alright, we need to alloc something.
    const auto mem_size = (std::max)(chunk_size_, size);
    // add padding at the end
    const auto offset = (mem_size % alignof(block_));
    const auto padding = offset == 0 ? 0u : (alignof(block_) - offset);
    // size to allocate
    const auto raw_size = mem_size + padding;
    const auto alloc_size = raw_size + sizeof(block_);

    const auto aligned = std::align_val_t(align);
    const auto mem = ::operator new(alloc_size, aligned);
    const auto block_location = static_cast<char*>(mem) +  mem_size + offset;
    head_ = head_->next = new (block_location) block_{mem, raw_size - size, raw_size, aligned, nullptr};

    return mem;
  }

};

template<typename T>
struct monotonic_allocator
{
  template<typename U>
  monotonic_allocator(monotonic_allocator<U> alloc) : resource_(alloc.resource_)
  {

  }
  using value_type                             = T;
  using size_type                              = std::size_t;
  using difference_type                        = std::ptrdiff_t;
  using propagate_on_container_move_assignment = std::true_type;

  [[nodiscard]] constexpr T* allocate( std::size_t n )
  {
    if (resource_)
      return static_cast<T*>(
          resource_->allocate(
              sizeof(T) * n,
              std::align_val_t(alignof(T))));
    else
      return std::allocator<T>().allocate(n);
  }

  constexpr void deallocate( T* p, std::size_t n )
  {
    if (!resource_)
      std::allocator<T>().deallocate(p, n);
  }
  monotonic_allocator(monotonic_resource * resource = nullptr) : resource_(resource) {}
 private:
  template<typename>
  friend struct monotonic_allocator;

  monotonic_resource * resource_{nullptr};
};

}

#endif //BOOST_COBALT_DETAIL_MONOTONIC_BUFFER_RESOURCE_HPP
