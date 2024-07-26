/* Pointer to a SG14 status_code
(C) 2018 - 2023 Niall Douglas <http://www.nedproductions.biz/> (5 commits)
File Created: Sep 2018


Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License in the accompanying file
Licence.txt or at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


Distributed under the Boost Software License, Version 1.0.
(See accompanying file Licence.txt or copy at
http://www.boost.org/LICENSE_1_0.txt)
*/

#ifndef BOOST_OUTCOME_SYSTEM_ERROR2_NESTED_STATUS_CODE_HPP
#define BOOST_OUTCOME_SYSTEM_ERROR2_NESTED_STATUS_CODE_HPP

#include "quick_status_code_from_enum.hpp"

#include <memory>  // for allocator

BOOST_OUTCOME_SYSTEM_ERROR2_NAMESPACE_BEGIN

namespace detail
{
  template <class StatusCode, class Allocator> class indirecting_domain : public status_code_domain
  {
    template <class DomainType> friend class status_code;
    using _base = status_code_domain;

  public:
    struct payload_type
    {
      using allocator_traits = std::allocator_traits<Allocator>;
      union
      {
        char _uninit[sizeof(StatusCode)];
        StatusCode sc;
      };
      Allocator alloc;

      payload_type(StatusCode _sc, Allocator _alloc)
          : alloc(static_cast<Allocator &&>(_alloc))
      {
        allocator_traits::construct(alloc, &sc, static_cast<StatusCode &&>(_sc));
      }
      payload_type(const payload_type &) = delete;
      payload_type(payload_type &&) = delete;
      ~payload_type() { allocator_traits::destroy(alloc, &sc); }
    };
    using value_type = payload_type *;
    using payload_allocator_traits = typename payload_type::allocator_traits::template rebind_traits<payload_type>;
    using _base::string_ref;

    constexpr indirecting_domain() noexcept
        : _base(0xc44f7bdeb2cc50e9 ^ typename StatusCode::domain_type().id() /* unique-ish based on domain's unique id */)
    {
    }
    indirecting_domain(const indirecting_domain &) = default;
    indirecting_domain(indirecting_domain &&) = default;             // NOLINT
    indirecting_domain &operator=(const indirecting_domain &) = default;
    indirecting_domain &operator=(indirecting_domain &&) = default;  // NOLINT
    ~indirecting_domain() = default;

#if __cplusplus < 201402L && !defined(_MSC_VER)
    static inline const indirecting_domain &get()
    {
      static indirecting_domain v;
      return v;
    }
#else
    static inline constexpr const indirecting_domain &get();
#endif

    virtual string_ref name() const noexcept override { return typename StatusCode::domain_type().name(); }  // NOLINT

    virtual payload_info_t payload_info() const noexcept override
    {
      return {sizeof(value_type), sizeof(status_code_domain *) + sizeof(value_type),
              (alignof(value_type) > alignof(status_code_domain *)) ? alignof(value_type) : alignof(status_code_domain *)};
    }

  protected:
    using _mycode = status_code<indirecting_domain>;
    virtual bool _do_failure(const status_code<void> &code) const noexcept override  // NOLINT
    {
      assert(code.domain() == *this);
      const auto &c = static_cast<const _mycode &>(code);  // NOLINT
      return static_cast<status_code_domain &&>(typename StatusCode::domain_type())._do_failure(c.value()->sc);
    }
    virtual bool _do_equivalent(const status_code<void> &code1, const status_code<void> &code2) const noexcept override  // NOLINT
    {
      assert(code1.domain() == *this);
      const auto &c1 = static_cast<const _mycode &>(code1);  // NOLINT
      return static_cast<status_code_domain &&>(typename StatusCode::domain_type())._do_equivalent(c1.value()->sc, code2);
    }
    virtual generic_code _generic_code(const status_code<void> &code) const noexcept override  // NOLINT
    {
      assert(code.domain() == *this);
      const auto &c = static_cast<const _mycode &>(code);  // NOLINT
      return static_cast<status_code_domain &&>(typename StatusCode::domain_type())._generic_code(c.value()->sc);
    }
    virtual string_ref _do_message(const status_code<void> &code) const noexcept override  // NOLINT
    {
      assert(code.domain() == *this);
      const auto &c = static_cast<const _mycode &>(code);  // NOLINT
      return static_cast<status_code_domain &&>(typename StatusCode::domain_type())._do_message(c.value()->sc);
    }
#if defined(_CPPUNWIND) || defined(__EXCEPTIONS) || defined(BOOST_OUTCOME_STANDARDESE_IS_IN_THE_HOUSE)
    BOOST_OUTCOME_SYSTEM_ERROR2_NORETURN virtual void _do_throw_exception(const status_code<void> &code) const override  // NOLINT
    {
      assert(code.domain() == *this);
      const auto &c = static_cast<const _mycode &>(code);  // NOLINT
      static_cast<status_code_domain &&>(typename StatusCode::domain_type())._do_throw_exception(c.value()->sc);
      abort();                                             // suppress buggy GCC warning
    }
#endif
    virtual bool _do_erased_copy(status_code<void> &dst, const status_code<void> &src, payload_info_t dstinfo) const override  // NOLINT
    {
      // Note that dst may not have its domain set
      const auto srcinfo = payload_info();
      assert(src.domain() == *this);
      if(dstinfo.total_size < srcinfo.total_size)
      {
        return false;
      }
      auto &d = static_cast<_mycode &>(dst);               // NOLINT
      const auto &_s = static_cast<const _mycode &>(src);  // NOLINT
      const payload_type &sp = *_s.value();
      typename payload_allocator_traits::template rebind_alloc<payload_type> payload_alloc(sp.alloc);
      auto *dp = payload_allocator_traits::allocate(payload_alloc, 1);
#if defined(_CPPUNWIND) || defined(__EXCEPTIONS) || defined(BOOST_OUTCOME_STANDARDESE_IS_IN_THE_HOUSE)
      try
#endif
      {
        payload_allocator_traits::construct(payload_alloc, dp, sp.sc, sp.alloc);
        new(BOOST_OUTCOME_SYSTEM_ERROR2_ADDRESS_OF(d)) _mycode(in_place, dp);
      }
#if defined(_CPPUNWIND) || defined(__EXCEPTIONS) || defined(BOOST_OUTCOME_STANDARDESE_IS_IN_THE_HOUSE)
      catch(...)
      {
        payload_allocator_traits::deallocate(payload_alloc, dp, 1);
        throw;
      }
#endif
      return true;
    }
    virtual void _do_erased_destroy(status_code<void> &code, payload_info_t /*unused*/) const noexcept override  // NOLINT
    {
      assert(code.domain() == *this);
      auto &c = static_cast<_mycode &>(code);  // NOLINT
      payload_type *p = c.value();
      typename payload_allocator_traits::template rebind_alloc<payload_type> payload_alloc(p->alloc);
      payload_allocator_traits::destroy(payload_alloc, p);
      payload_allocator_traits::deallocate(payload_alloc, p, 1);
    }
  };
#if __cplusplus >= 201402L || defined(_MSC_VER)
  template <class StatusCode, class Allocator> constexpr indirecting_domain<StatusCode, Allocator> _indirecting_domain{};
  template <class StatusCode, class Allocator>
  inline constexpr const indirecting_domain<StatusCode, Allocator> &indirecting_domain<StatusCode, Allocator>::get()
  {
    return _indirecting_domain<StatusCode, Allocator>;
  }
#endif
}  // namespace detail

/*! Make an erased status code which indirects to a dynamically allocated status code,
using the allocator `alloc`.

This is useful for shoehorning a rich status code with large value type into a small
erased status code like `system_code`, with which the status code generated by this
function is compatible. Note that this function can throw if the allocator throws.
*/
BOOST_OUTCOME_SYSTEM_ERROR2_TEMPLATE(class T, class Alloc = std::allocator<typename std::decay<T>::type>)
BOOST_OUTCOME_SYSTEM_ERROR2_TREQUIRES(BOOST_OUTCOME_SYSTEM_ERROR2_TPRED(is_status_code<T>::value))  //
inline status_code<detail::erased<typename std::add_pointer<typename std::decay<T>::type>::type>> make_nested_status_code(T &&v, Alloc alloc = {})
{
  using status_code_type = typename std::decay<T>::type;
  using domain_type = detail::indirecting_domain<status_code_type, typename std::decay<Alloc>::type>;
  using payload_allocator_traits = typename domain_type::payload_allocator_traits;
  typename payload_allocator_traits::template rebind_alloc<typename domain_type::payload_type> payload_alloc(alloc);
  auto *p = payload_allocator_traits::allocate(payload_alloc, 1);
#if defined(_CPPUNWIND) || defined(__EXCEPTIONS) || defined(BOOST_OUTCOME_STANDARDESE_IS_IN_THE_HOUSE)
  try
#endif
  {
    payload_allocator_traits::construct(payload_alloc, p, static_cast<T &&>(v), static_cast<Alloc &&>(alloc));
    return status_code<domain_type>(in_place, p);
  }
#if defined(_CPPUNWIND) || defined(__EXCEPTIONS) || defined(BOOST_OUTCOME_STANDARDESE_IS_IN_THE_HOUSE)
  catch(...)
  {
    payload_allocator_traits::deallocate(payload_alloc, p, 1);
    throw;
  }
#endif
}

/*! If a status code refers to a `nested_status_code` which indirects to a status
code of type `StatusCode`, return a pointer to that `StatusCode`. Otherwise return null.
*/
BOOST_OUTCOME_SYSTEM_ERROR2_TEMPLATE(class StatusCode, class U)
BOOST_OUTCOME_SYSTEM_ERROR2_TREQUIRES(BOOST_OUTCOME_SYSTEM_ERROR2_TPRED(is_status_code<StatusCode>::value)) inline StatusCode *get_if(status_code<detail::erased<U>> *v) noexcept
{
  if((0xc44f7bdeb2cc50e9 ^ typename StatusCode::domain_type().id()) != v->domain().id())
  {
    return nullptr;
  }
  union
  {
    U value;
    StatusCode *ret;
  };
  value = v->value();
  return ret;
}
//! \overload Const overload
BOOST_OUTCOME_SYSTEM_ERROR2_TEMPLATE(class StatusCode, class U)
BOOST_OUTCOME_SYSTEM_ERROR2_TREQUIRES(BOOST_OUTCOME_SYSTEM_ERROR2_TPRED(is_status_code<StatusCode>::value))
inline const StatusCode *get_if(const status_code<detail::erased<U>> *v) noexcept
{
  if((0xc44f7bdeb2cc50e9 ^ typename StatusCode::domain_type().id()) != v->domain().id())
  {
    return nullptr;
  }
  union
  {
    U value;
    const StatusCode *ret;
  };
  value = v->value();
  return ret;
}

/*! If a status code refers to a `nested_status_code`, return the id of the erased
status code's domain. Otherwise return a meaningless number.
*/
template <class U> inline typename status_code_domain::unique_id_type get_id(const status_code<detail::erased<U>> &v) noexcept
{
  return 0xc44f7bdeb2cc50e9 ^ v.domain().id();
}

BOOST_OUTCOME_SYSTEM_ERROR2_NAMESPACE_END

#endif
