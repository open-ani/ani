//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_FIELD_HPP
#define BOOST_MYSQL_FIELD_HPP

#include <boost/mysql/blob.hpp>
#include <boost/mysql/field_kind.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/field_impl.hpp>

#include <boost/variant2/variant.hpp>

#include <cstddef>
#include <iosfwd>
#include <string>
#ifdef __cpp_lib_string_view
#include <string_view>
#endif

namespace boost {
namespace mysql {

/**
 * \brief Variant-like class that can represent of any of the allowed database types.
 * \details
 * This is a regular variant-like class that can represent any of the types that MySQL allows. It
 * has value semantics (as opposed to \ref field_view). Instances of this class are not created
 * by the library. They should be created by the user, when the reference semantics of
 * \ref field_view are not appropriate.
 * \n
 * Like a variant, at any point, a `field` always contains a value of
 * certain type. You can query the type using \ref kind and the `is_xxx` functions
 * like \ref is_int64. Use `as_xxx` and `get_xxx` for checked and unchecked value
 * access, respectively. You can mutate a `field` by calling the assignment operator,
 * or using the lvalue references returned by `as_xxx` and `get_xxx`.
 */
class field
{
public:
    /**
     * \brief Constructs a `field` holding NULL.
     * \par Exception safety
     * No-throw guarantee.
     */
    field() = default;

    /**
     * \brief Copy constructor.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     */
    field(const field&) = default;

    /**
     * \brief Move constructor.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * All references into `other` are invalidated, including the ones obtained by calling
     * get_xxx, as_xxx and \ref field::operator field_view().
     */
    field(field&& other) = default;

    /**
     * \brief Copy assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(const field&) = default;

    /**
     * \brief Move assignment.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references to `*this` obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view(). All references into `other`
     * are invalidated, including the ones obtained by calling get_xxx, as_xxx and
     * \ref field::operator field_view().
     */
    field& operator=(field&& other) = default;

    /// Destructor.
    ~field() = default;

    /**
     * \brief Constructs a `field` holding NULL.
     * \details
     * Caution: `field(NULL)` will __NOT__ match this overload. It will try to construct
     * a `string_view` from a NULL C string, causing undefined behavior.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(std::nullptr_t) noexcept {}

    /**
     * \brief Constructs a `field` holding an `int64`.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(signed char v) noexcept : repr_(std::int64_t(v)) {}

    /// \copydoc field(signed char)
    explicit field(short v) noexcept : repr_(std::int64_t(v)) {}

    /// \copydoc field(signed char)
    explicit field(int v) noexcept : repr_(std::int64_t(v)) {}

    /// \copydoc field(signed char)
    explicit field(long v) noexcept : repr_(std::int64_t(v)) {}

    /// \copydoc field(signed char)
    explicit field(long long v) noexcept : repr_(std::int64_t(v)) {}

    /**
     * \brief Constructs a `field` holding an `uint64`.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(unsigned char v) noexcept : repr_(std::uint64_t(v)) {}

    /// \copydoc field(unsigned char)
    explicit field(unsigned short v) noexcept : repr_(std::uint64_t(v)) {}

    /// \copydoc field(unsigned char)
    explicit field(unsigned int v) noexcept : repr_(std::uint64_t(v)) {}

    /// \copydoc field(unsigned char)
    explicit field(unsigned long v) noexcept : repr_(std::uint64_t(v)) {}

    /// \copydoc field(unsigned char)
    explicit field(unsigned long long v) noexcept : repr_(std::uint64_t(v)) {}

    /**
     * \brief Constructors from character types would incorrectly construct a `field` holding an integer,
     * so they are not allowed.
     */
    explicit field(char) = delete;

    /// \copydoc field(char)
    explicit field(wchar_t) = delete;

    /// \copydoc field(char)
    explicit field(char16_t) = delete;

    /// \copydoc field(char)
    explicit field(char32_t) = delete;

#ifdef __cpp_char8_t
    /// \copydoc field(char)
    explicit field(char8_t) = delete;
#endif

    /**
     * \brief Constructs a `field` holding a string.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     */
    explicit field(const std::string& v) : repr_(v) {}

    /**
     * \brief Constructs a `field` holding a string.
     * \details v is moved into an internal `std::string` object.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(std::string&& v) noexcept : repr_(std::move(v)) {}

    /// \copydoc field(const std::string&)
    explicit field(const char* v) : repr_(boost::variant2::in_place_type_t<std::string>(), v) {}

    /// \copydoc field(const std::string&)
    explicit field(string_view v) : repr_(boost::variant2::in_place_type_t<std::string>(), v) {}

#if defined(__cpp_lib_string_view)
    /// \copydoc field(const std::string&)
    explicit field(std::string_view v) noexcept : repr_(boost::variant2::in_place_type_t<std::string>(), v) {}
#endif

    /**
     * \brief Constructs a `field` holding a `blob`.
     * \details v is moved into an internal `blob` object.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(blob v) noexcept : repr_(std::move(v)) {}

    /**
     * \brief Constructs a `field` holding a `float`.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(float v) noexcept : repr_(v) {}

    /**
     * \brief Constructs a `field` holding a `double`.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(double v) noexcept : repr_(v) {}

    /**
     * \brief Constructs a `field` holding a `date`.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(const date& v) noexcept : repr_(v) {}

    /**
     * \brief Constructs a `field` holding a `datetime`.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(const datetime& v) noexcept : repr_(v) {}

    /**
     * \brief Constructs a `field` holding a `time`.
     * \par Exception safety
     * No-throw guarantee.
     */
    explicit field(const time& v) noexcept : repr_(v) {}

    /**
     * \brief Constructs a `field` from a \ref field_view.
     * \details The resulting `field` has the same kind and value as the original `field_view`.
     *
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * The resulting `field` is guaranteed to be valid even after `v` becomes invalid.
     */
    field(const field_view& v) { from_view(v); }

    /**
     * \brief Replaces `*this` with a `NULL`, changing the kind to `null` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(std::nullptr_t) noexcept
    {
        repr_.data.emplace<detail::field_impl::null_t>();
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `int64` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(signed char v) noexcept
    {
        repr_.data.emplace<std::int64_t>(v);
        return *this;
    }

    /// \copydoc operator=(signed char)
    field& operator=(short v) noexcept
    {
        repr_.data.emplace<std::int64_t>(v);
        return *this;
    }

    /// \copydoc operator=(signed char)
    field& operator=(int v) noexcept
    {
        repr_.data.emplace<std::int64_t>(v);
        return *this;
    }

    /// \copydoc operator=(signed char)
    field& operator=(long v) noexcept
    {
        repr_.data.emplace<std::int64_t>(v);
        return *this;
    }

    /// \copydoc operator=(signed char)
    field& operator=(long long v) noexcept
    {
        repr_.data.emplace<std::int64_t>(v);
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `uint64` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(unsigned char v) noexcept
    {
        repr_.data.emplace<std::uint64_t>(v);
        return *this;
    }

    /// \copydoc operator=(unsigned char)
    field& operator=(unsigned short v) noexcept
    {
        repr_.data.emplace<std::uint64_t>(v);
        return *this;
    }

    /// \copydoc operator=(unsigned char)
    field& operator=(unsigned int v) noexcept
    {
        repr_.data.emplace<std::uint64_t>(v);
        return *this;
    }

    /// \copydoc operator=(unsigned char)
    field& operator=(unsigned long v) noexcept
    {
        repr_.data.emplace<std::uint64_t>(v);
        return *this;
    }

    /// \copydoc operator=(unsigned char)
    field& operator=(unsigned long long v) noexcept
    {
        repr_.data.emplace<std::uint64_t>(v);
        return *this;
    }

    /**
     * \brief Assignments from character types would incorrectly assign an integer,
     * so they are not allowed.
     */
    field& operator=(char) = delete;

    /// \copydoc operator=(char)
    field& operator=(wchar_t) = delete;

    /// \copydoc operator=(char)
    field& operator=(char16_t) = delete;

    /// \copydoc operator=(char)
    field& operator=(char32_t) = delete;

#ifdef __cpp_char8_t
    /// \copydoc operator=(char)
    field& operator=(char8_t) = delete;
#endif

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `string` and destroying any previous
     * contents.
     *
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(const std::string& v)
    {
        repr_.data.emplace<std::string>(v);
        return *this;
    }

    /// \copydoc operator=(const std::string&)
    field& operator=(std::string&& v)
    {
        repr_.data.emplace<std::string>(std::move(v));
        return *this;
    }

    /// \copydoc operator=(const std::string&)
    field& operator=(const char* v)
    {
        repr_.data.emplace<std::string>(v);
        return *this;
    }

    /// \copydoc operator=(const std::string&)
    field& operator=(string_view v)
    {
        repr_.data.emplace<std::string>(v);
        return *this;
    }

#if defined(__cpp_lib_string_view)
    /// \copydoc operator=(const std::string&)
    field& operator=(std::string_view v)
    {
        repr_.data.emplace<std::string>(v);
        return *this;
    }
#endif

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `blob` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(blob v)
    {
        repr_.data.emplace<blob>(std::move(v));
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `float_` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(float v) noexcept
    {
        repr_.data.emplace<float>(v);
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `double` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(double v) noexcept
    {
        repr_.data.emplace<double>(v);
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `date` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(const date& v) noexcept
    {
        repr_.data.emplace<date>(v);
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `datetime` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions,
     * but not the ones obtained by \ref field::operator field_view().
     */
    field& operator=(const datetime& v) noexcept
    {
        repr_.data.emplace<datetime>(v);
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `time` and destroying any
     * previous contents.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Invalidates references obtained by as_xxx and get_xxx functions, but not
     */
    field& operator=(const time& v) noexcept
    {
        repr_.data.emplace<time>(v);
        return *this;
    }

    /**
     * \brief Replaces `*this` with `v`, changing the kind to `v.kind()` and destroying any previous
     * contents.
     *
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * Invalidates references to `*this` obtained by as_xxx and get_xxx functions, but not
     * the ones obtained by \ref field::operator field_view().
     *\n
     * `*this` is guaranteed to be valid even after `v` becomes invalid.
     */
    field& operator=(const field_view& v)
    {
        from_view(v);
        return *this;
    }

    /**
     * \brief Returns the type of the value this `field` is holding.
     * \par Exception safety
     * No-throw guarantee.
     */
    field_kind kind() const noexcept { return repr_.kind(); }

    /**
     * \brief Returns whether this `field` is holding a `NULL` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_null() const noexcept { return kind() == field_kind::null; }

    /**
     * \brief Returns whether this `field` is holding a `int64` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_int64() const noexcept { return kind() == field_kind::int64; }

    /**
     * \brief Returns whether this `field` is holding a `uint64` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_uint64() const noexcept { return kind() == field_kind::uint64; }

    /**
     * \brief Returns whether this `field` is holding a string value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_string() const noexcept { return kind() == field_kind::string; }

    /**
     * \brief Returns whether this `field` is holding a blob value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_blob() const noexcept { return kind() == field_kind::blob; }

    /**
     * \brief Returns whether this `field` is holding a `float` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_float() const noexcept { return kind() == field_kind::float_; }

    /**
     * \brief Returns whether this `field` is holding a `double` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_double() const noexcept { return kind() == field_kind::double_; }

    /**
     * \brief Returns whether this `field` is holding a `date` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_date() const noexcept { return kind() == field_kind::date; }

    /**
     * \brief Returns whether this `field` is holding a `datetime` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_datetime() const noexcept { return kind() == field_kind::datetime; }

    /**
     * \brief Returns whether this `field` is holding a `time` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool is_time() const noexcept { return kind() == field_kind::time; }

    /**
     * \brief Retrieves a reference to the underlying `std::int64_t` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_int64()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const std::int64_t& as_int64() const { return repr_.as<std::int64_t>(); }

    /**
     * \brief Retrieves a reference to the underlying `std::uint64_t` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_uint64()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const std::uint64_t& as_uint64() const { return repr_.as<std::uint64_t>(); }

    /**
     * \brief Retrieves a reference to the underlying `std::string` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_string()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const std::string& as_string() const { return repr_.as<std::string>(); }

    /**
     * \brief Retrieves a reference to the underlying `blob` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_blob()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const blob& as_blob() const { return repr_.as<blob>(); }

    /**
     * \brief Retrieves a reference to the underlying `float` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_float()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const float& as_float() const { return repr_.as<float>(); }

    /**
     * \brief Retrieves a reference to the underlying `double` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_double()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const double& as_double() const { return repr_.as<double>(); }

    /**
     * \brief Retrieves a reference to the underlying `date` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_date()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const date& as_date() const { return repr_.as<date>(); }

    /**
     * \brief Retrieves a reference to the underlying `datetime` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_datetime()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const datetime& as_datetime() const { return repr_.as<datetime>(); }

    /**
     * \brief Retrieves a reference to the underlying `time` value or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_time()`
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const time& as_time() const { return repr_.as<time>(); }

    /// \copydoc as_int64
    std::int64_t& as_int64() { return repr_.as<std::int64_t>(); }

    /// \copydoc as_uint64
    std::uint64_t& as_uint64() { return repr_.as<std::uint64_t>(); }

    /// \copydoc as_string
    std::string& as_string() { return repr_.as<std::string>(); }

    /// \copydoc as_blob
    blob& as_blob() { return repr_.as<blob>(); }

    /// \copydoc as_float
    float& as_float() { return repr_.as<float>(); }

    /// \copydoc as_double
    double& as_double() { return repr_.as<double>(); }

    /// \copydoc as_date
    date& as_date() { return repr_.as<date>(); }

    /// \copydoc as_datetime
    datetime& as_datetime() { return repr_.as<datetime>(); }

    /// \copydoc as_time
    time& as_time() { return repr_.as<time>(); }

    /**
     * \brief Retrieves a reference to the underlying `std::int64_t` value (unchecked access).
     * \par Preconditions
     * `this->is_int64() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const std::int64_t& get_int64() const noexcept { return repr_.get<std::int64_t>(); }

    /**
     * \brief Retrieves a reference to the underlying `std::uint64_t` value (unchecked access).
     * \par Preconditions
     * `this->is_uint64() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const std::uint64_t& get_uint64() const noexcept { return repr_.get<std::uint64_t>(); }

    /**
     * \brief Retrieves a reference to the underlying `std::string` value (unchecked access).
     * \par Preconditions
     * `this->is_string() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const std::string& get_string() const noexcept { return repr_.get<std::string>(); }

    /**
     * \brief Retrieves a reference to the underlying `blob` value (unchecked access).
     * \par Preconditions
     * `this->is_blob() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const blob& get_blob() const noexcept { return repr_.get<blob>(); }

    /**
     * \brief Retrieves a reference to the underlying `float` value (unchecked access).
     * \par Preconditions
     * `this->is_float() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const float& get_float() const noexcept { return repr_.get<float>(); }

    /**
     * \brief Retrieves a reference to the underlying `double` value (unchecked access).
     * \par Preconditions
     * `this->is_double() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const double& get_double() const noexcept { return repr_.get<double>(); }

    /**
     * \brief Retrieves a reference to the underlying `date` value (unchecked access).
     * \par Preconditions
     * `this->is_date() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const date& get_date() const noexcept { return repr_.get<date>(); }

    /**
     * \brief Retrieves a reference to the underlying `datetime` value (unchecked access).
     * \par Preconditions
     * `this->is_datetime() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const datetime& get_datetime() const noexcept { return repr_.get<datetime>(); }

    /**
     * \brief Retrieves a reference to the underlying `time` value (unchecked access).
     * \par Preconditions
     * `this->is_time() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive and no function that invalidates
     * references is called on `*this`.
     */
    const time& get_time() const noexcept { return repr_.get<time>(); }

    /// \copydoc get_int64
    std::int64_t& get_int64() noexcept { return repr_.get<std::int64_t>(); }

    /// \copydoc get_uint64
    std::uint64_t& get_uint64() noexcept { return repr_.get<std::uint64_t>(); }

    /// \copydoc get_string
    std::string& get_string() noexcept { return repr_.get<std::string>(); }

    /// \copydoc get_blob
    blob& get_blob() noexcept { return repr_.get<blob>(); }

    /// \copydoc get_float
    float& get_float() noexcept { return repr_.get<float>(); }

    /// \copydoc get_double
    double& get_double() noexcept { return repr_.get<double>(); }

    /// \copydoc get_date
    date& get_date() noexcept { return repr_.get<date>(); }

    /// \copydoc get_datetime
    datetime& get_datetime() noexcept { return repr_.get<datetime>(); }

    /// \copydoc get_time
    time& get_time() noexcept { return repr_.get<time>(); }

    /**
     * \brief Constructs a \ref field_view pointing to `*this`.
     * \details The resulting `field_view` has the same kind and value as `*this`.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned object acts as a
     * reference to `*this`, and will be valid as long as `*this` is alive.
     */
    inline operator field_view() const noexcept { return field_view(&repr_); }

private:
    detail::field_impl repr_;

    BOOST_MYSQL_DECL
    void from_view(const field_view& v);
};

/**
 * \relates field
 * \brief Tests for equality.
 * \details The same considerations as \ref field_view::operator== apply.
 *
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator==(const field& lhs, const field& rhs) noexcept
{
    return field_view(lhs) == field_view(rhs);
}

/**
 * \relates field
 * \brief Tests for inequality.
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator!=(const field& lhs, const field& rhs) noexcept { return !(lhs == rhs); }

/**
 * \relates field
 * \brief Tests for equality.
 * \details The same considerations as \ref field_view::operator== apply.
 *
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator==(const field_view& lhs, const field& rhs) noexcept { return lhs == field_view(rhs); }

/**
 * \relates field
 * \brief Tests for inequality.
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator!=(const field_view& lhs, const field& rhs) noexcept { return !(lhs == rhs); }

/**
 * \relates field
 * \brief Tests for equality.
 * \details The same considerations as \ref field_view::operator== apply.
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator==(const field& lhs, const field_view& rhs) noexcept { return field_view(lhs) == rhs; }

/**
 * \relates field
 * \brief Tests for inequality.
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator!=(const field& lhs, const field_view& rhs) noexcept { return !(lhs == rhs); }

/**
 * \relates field
 * \brief Streams a `field`.
 */
BOOST_MYSQL_DECL
std::ostream& operator<<(std::ostream& os, const field& v);

}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/field.ipp>
#endif

#endif
