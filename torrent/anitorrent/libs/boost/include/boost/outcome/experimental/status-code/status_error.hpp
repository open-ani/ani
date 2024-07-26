/* Proposed SG14 status_code
(C) 2018 - 2022 Niall Douglas <http://www.nedproductions.biz/> (5 commits)
File Created: Feb 2018


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

#ifndef BOOST_OUTCOME_SYSTEM_ERROR2_STATUS_ERROR_HPP
#define BOOST_OUTCOME_SYSTEM_ERROR2_STATUS_ERROR_HPP

#include "status_code.hpp"

#include <exception>  // for std::exception

BOOST_OUTCOME_SYSTEM_ERROR2_NAMESPACE_BEGIN

/*! Exception type representing a thrown status_code
 */
template <class DomainType> class status_error;

/*! The erased type edition of status_error.
 */
template <> class status_error<void> : public std::exception
{
protected:
  //! Constructs an instance. Not publicly available.
  status_error() = default;
  //! Copy constructor. Not publicly available
  status_error(const status_error &) = default;
  //! Move constructor. Not publicly available
  status_error(status_error &&) = default;
  //! Copy assignment. Not publicly available
  status_error &operator=(const status_error &) = default;
  //! Move assignment. Not publicly available
  status_error &operator=(status_error &&) = default;
  //! Destructor. Not publicly available.
  ~status_error() override = default;

  virtual const status_code<void> &_do_code() const noexcept = 0;

public:
  //! The type of the status domain
  using domain_type = void;
  //! The type of the status code
  using status_code_type = status_code<void>;

public:
  //! The erased status code which generated this exception instance.
  const status_code<void> &code() const noexcept { return _do_code(); }
};

/*! Exception type representing a thrown status_code
 */
template <class DomainType> class status_error : public status_error<void>
{
  status_code<DomainType> _code;
  typename DomainType::string_ref _msgref;

  virtual const status_code<void> &_do_code() const noexcept override final { return _code; }

public:
  //! The type of the status domain
  using domain_type = DomainType;
  //! The type of the status code
  using status_code_type = status_code<DomainType>;

  //! Constructs an instance
  explicit status_error(status_code<DomainType> code)
      : _code(static_cast<status_code<DomainType> &&>(code))
      , _msgref(_code.message())
  {
  }

  //! Return an explanatory string
  virtual const char *what() const noexcept override { return _msgref.c_str(); }  // NOLINT

  //! Returns a reference to the code
  const status_code_type &code() const & { return _code; }
  //! Returns a reference to the code
  status_code_type &code() & { return _code; }
  //! Returns a reference to the code
  const status_code_type &&code() const && { return _code; }
  //! Returns a reference to the code
  status_code_type &&code() && { return _code; }
};

/*! Exception type representing a thrown erased status_code
 */
template <class ErasedType> class status_error<detail::erased<ErasedType>> : public status_error<void>
{
  status_code<detail::erased<ErasedType>> _code;
  typename status_code_domain::string_ref _msgref;

  virtual const status_code<detail::erased<ErasedType>> &_do_code() const noexcept override final { return _code; }

public:
  //! The type of the status domain
  using domain_type = void;
  //! The type of the status code
  using status_code_type = status_code<detail::erased<ErasedType>>;

  //! Constructs an instance
  explicit status_error(status_code<detail::erased<ErasedType>> code)
      : _code(static_cast<status_code<detail::erased<ErasedType>> &&>(code))
      , _msgref(_code.message())
  {
  }

  //! Return an explanatory string
  virtual const char *what() const noexcept override { return _msgref.c_str(); }  // NOLINT

  //! Returns a reference to the code
  const status_code_type &code() const & { return _code; }
  //! Returns a reference to the code
  status_code_type &code() & { return _code; }
  //! Returns a reference to the code
  const status_code_type &&code() const && { return _code; }
  //! Returns a reference to the code
  status_code_type &&code() && { return _code; }
};

BOOST_OUTCOME_SYSTEM_ERROR2_NAMESPACE_END

#endif
