//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_FIELD_VIEW_HPP
#define BOOST_MYSQL_FIELD_VIEW_HPP

#include <boost/mysql/blob_view.hpp>
#include <boost/mysql/date.hpp>
#include <boost/mysql/datetime.hpp>
#include <boost/mysql/field_kind.hpp>
#include <boost/mysql/string_view.hpp>
#include <boost/mysql/time.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/field_impl.hpp>
#include <boost/mysql/detail/string_view_offset.hpp>

#include <boost/config.hpp>

#include <cstddef>
#include <cstdint>
#include <iosfwd>

namespace boost {
namespace mysql {

/**
 * \brief Non-owning variant-like class that can represent of any of the allowed database types.
 * \details
 * This is a variant-like class, similar to \ref field, but semi-owning and read-only. Values
 * of this type are usually created by the library, not directly by the user. It's cheap to
 * construct and copy, and it's the main library interface when reading values from MySQL.
 * \n
 * Like a variant, at any point, a `field_view` always points to a value of
 * certain type. You can query the type using \ref field_view::kind and the `is_xxx` functions
 * like \ref field_view::is_int64. Use `as_xxx` and `get_xxx` for checked and unchecked value
 * access, respectively. As opposed to \ref field, these functions return values instead of
 * references.
 *
 * \par Object lifetimes
 * Depending on how it was constructed, `field_view` can have value or reference semantics:
 * \n
 * \li If it was created by the library, the `field_view` will have an associated \ref row,
 *     \ref rows or \ref results object holding memory to which the `field_view` points. It will be valid as
 *     long as the memory allocated by that object is valid.
 * \li If it was created from a \ref field (by calling `operator field_view`), the
 *     `field_view` acts as a reference to that `field` object, and will be valid as long as the
 *     `field` is.
 * \li If it was created from a scalar (null, integral, floating point, date, datetime or time), the
 *     `field_view` has value semnatics and will always be valid.
 * \li If it was created from a string or blob type, the `field_view` acts as a `string_view` or `blob_view`,
 *     and will be valid as long as the original string/blob is.
 * \n
 * Calling any member function on a `field_view` that has been invalidated results in undefined
 * behavior.
 */
class field_view
{
public:
    /**
     * \brief Constructs a `field_view` holding NULL.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR field_view() = default;

    /**
     * \brief Constructs a `field_view` holding NULL.
     * \details
     * Caution: `field_view(NULL)` will <b>not</b> match this overload. It will try to construct
     * a `string_view` from a NULL C string, causing undefined behavior.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(std::nullptr_t) noexcept {}

    /**
     * \brief Constructs a `field_view` holding an `int64`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(signed char v) noexcept : impl_{std::int64_t(v)} {}

    /// \copydoc field_view(signed char)
    BOOST_CXX14_CONSTEXPR explicit field_view(short v) noexcept : impl_{std::int64_t(v)} {}

    /// \copydoc field_view(signed char)
    BOOST_CXX14_CONSTEXPR explicit field_view(int v) noexcept : impl_{std::int64_t(v)} {}

    /// \copydoc field_view(signed char)
    BOOST_CXX14_CONSTEXPR explicit field_view(long v) noexcept : impl_{std::int64_t(v)} {}

    /// \copydoc field_view(signed char)
    BOOST_CXX14_CONSTEXPR explicit field_view(long long v) noexcept : impl_{std::int64_t(v)} {}

    /**
     * \brief Constructs a `field_view` holding a `uint64`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(unsigned char v) noexcept : impl_{std::uint64_t(v)} {}

    /// \copydoc field_view(unsigned char)
    BOOST_CXX14_CONSTEXPR explicit field_view(unsigned short v) noexcept : impl_{std::uint64_t(v)} {}

    /// \copydoc field_view(unsigned char)
    BOOST_CXX14_CONSTEXPR explicit field_view(unsigned int v) noexcept : impl_{std::uint64_t(v)} {}

    /// \copydoc field_view(unsigned char)
    BOOST_CXX14_CONSTEXPR explicit field_view(unsigned long v) noexcept : impl_{std::uint64_t(v)} {}

    /// \copydoc field_view(unsigned char)
    BOOST_CXX14_CONSTEXPR explicit field_view(unsigned long long v) noexcept : impl_{std::uint64_t(v)} {}

    /**
     * \brief Constructors from character types would incorrectly construct a `field_view` holding an integer,
     * so they are not allowed.
     */
    explicit field_view(char) = delete;

    /// \copydoc field_view(char)
    explicit field_view(wchar_t) = delete;

    /// \copydoc field_view(char)
    explicit field_view(char16_t) = delete;

    /// \copydoc field_view(char)
    explicit field_view(char32_t) = delete;

#ifdef __cpp_char8_t
    /// \copydoc field_view(char)
    explicit field_view(char8_t) = delete;
#endif

    /**
     * \brief Constructs a `field_view` holding a string.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with reference semantics. It will
     * be valid as long as the character buffer the `string_view` points to is valid.
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(string_view v) noexcept : impl_{v} {}

    /**
     * \brief Constructs a `field_view` holding a blob.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with reference semantics. It will
     * be valid as long as the character buffer the `blob_view` points to is valid.
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(blob_view v) noexcept : impl_{v} {}

    /**
     * \brief Constructs a `field_view` holding a `float`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(float v) noexcept : impl_{v} {}

    /**
     * \brief Constructs a `field_view` holding a `double`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(double v) noexcept : impl_{v} {}

    /**
     * \brief Constructs a `field_view` holding a `date`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(const date& v) noexcept : impl_{v} {}

    /**
     * \brief Constructs a `field_view` holding a `datetime`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(const datetime& v) noexcept : impl_{v} {}

    /**
     * \brief Constructs a `field_view` holding a `time`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Results in a `field_view` with value semantics (always valid).
     */
    BOOST_CXX14_CONSTEXPR explicit field_view(const time& v) noexcept : impl_{v} {}

    /**
     * \brief Returns the type of the value this `field_view` is pointing to.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline field_kind kind() const noexcept;

    /**
     * \brief Returns whether this `field_view` points to a `NULL` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_null() const noexcept { return kind() == field_kind::null; }

    /**
     * \brief Returns whether this `field_view` points to a `int64` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_int64() const noexcept { return kind() == field_kind::int64; }

    /**
     * \brief Returns whether this `field_view` points to a `uint64` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_uint64() const noexcept { return kind() == field_kind::uint64; }

    /**
     * \brief Returns whether this `field_view` points to a string value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_string() const noexcept { return kind() == field_kind::string; }

    /**
     * \brief Returns whether this `field_view` points to a binary blob.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_blob() const noexcept { return kind() == field_kind::blob; }

    /**
     * \brief Returns whether this `field_view` points to a `float` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_float() const noexcept { return kind() == field_kind::float_; }

    /**
     * \brief Returns whether this `field_view` points to a `double` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_double() const noexcept { return kind() == field_kind::double_; }

    /**
     * \brief Returns whether this `field_view` points to a `date` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_date() const noexcept { return kind() == field_kind::date; }

    /**
     * \brief Returns whether this `field_view` points to a `datetime` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_datetime() const noexcept { return kind() == field_kind::datetime; }

    /**
     * \brief Returns whether this `field_view` points to a `time` value.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool is_time() const noexcept { return kind() == field_kind::time; }

    /**
     * \brief Retrieves the underlying value as an `int64` or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_int64()`
     */
    BOOST_CXX14_CONSTEXPR inline std::int64_t as_int64() const;

    /**
     * \brief Retrieves the underlying value as an `uint64` or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_uint64()`
     */
    BOOST_CXX14_CONSTEXPR inline std::uint64_t as_uint64() const;

    /**
     * \brief Retrieves the underlying value as a string or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_string()`
     * \par Object lifetimes
     * The returned view has the same lifetime rules as `*this` (it's valid as long as `*this` is valid).
     */
    BOOST_CXX14_CONSTEXPR inline string_view as_string() const;

    /**
     * \brief Retrieves the underlying value as a blob or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_blob()`
     * \par Object lifetimes
     * The returned view has the same lifetime rules as `*this` (it's valid as long as `*this` is valid).
     */
    BOOST_CXX14_CONSTEXPR inline blob_view as_blob() const;

    /**
     * \brief Retrieves the underlying value as a `float` or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_float()`
     */
    BOOST_CXX14_CONSTEXPR inline float as_float() const;

    /**
     * \brief Retrieves the underlying value as a `double` or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_double()`
     */
    BOOST_CXX14_CONSTEXPR inline double as_double() const;

    /**
     * \brief Retrieves the underlying value as a `date` or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_date()`
     */
    BOOST_CXX14_CONSTEXPR inline date as_date() const;

    /**
     * \brief Retrieves the underlying value as a `datetime` or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_datetime()`
     */
    BOOST_CXX14_CONSTEXPR inline datetime as_datetime() const;

    /**
     * \brief Retrieves the underlying value as a `time` or throws an exception.
     * \par Exception safety
     * Strong guarantee. Throws on type mismatch.
     * \throws bad_field_access If `!this->is_time()`
     */
    BOOST_CXX14_CONSTEXPR inline time as_time() const;

    /**
     * \brief Retrieves the underlying value as an `int64` (unchecked access).
     * \par Preconditions
     * `this->is_int64() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline std::int64_t get_int64() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<std::int64_t>() : impl_.repr.int64;
    }

    /**
     * \brief Retrieves the underlying value as an `uint64` (unchecked access).
     * \par Preconditions
     * `this->is_uint64() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline std::uint64_t get_uint64() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<std::uint64_t>() : impl_.repr.uint64;
    }

    /**
     * \brief Retrieves the underlying value as a string (unchecked access).
     * \par Preconditions
     * `this->is_string() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned view has the same lifetime rules as `*this` (it's valid as long as `*this` is valid).
     */
    BOOST_CXX14_CONSTEXPR inline string_view get_string() const noexcept
    {
        return is_field_ptr() ? string_view(impl_.repr.field_ptr->get<std::string>()) : impl_.repr.string;
    }

    /**
     * \brief Retrieves the underlying value as a blob (unchecked access).
     * \par Preconditions
     * `this->is_blob() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned view has the same lifetime rules as `*this` (it's valid as long as `*this` is valid).
     */
    BOOST_CXX14_CONSTEXPR inline blob_view get_blob() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<blob>() : impl_.repr.blob;
    }

    /**
     * \brief Retrieves the underlying value as a `float` (unchecked access).
     * \par Preconditions
     * `this->is_float() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline float get_float() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<float>() : impl_.repr.float_;
    }

    /**
     * \brief Retrieves the underlying value as a `double` (unchecked access).
     * \par Preconditions
     * `this->is_double() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline double get_double() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<double>() : impl_.repr.double_;
    }

    /**
     * \brief Retrieves the underlying value as a `date` (unchecked access).
     * \par Preconditions
     * `this->is_date() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline date get_date() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<date>() : impl_.repr.date_;
    }

    /**
     * \brief Retrieves the underlying value as a `datetime` (unchecked access).
     * \par Preconditions
     * `this->is_datetime() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline datetime get_datetime() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<datetime>() : impl_.repr.datetime_;
    }

    /**
     * \brief Retrieves the underlying value as a `time` (unchecked access).
     * \par Preconditions
     * `this->is_time() == true` (if violated, results in undefined behavior).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline time get_time() const noexcept
    {
        return is_field_ptr() ? impl_.repr.field_ptr->get<time>() : impl_.repr.time_;
    }

    /**
     * \brief Tests for equality.
     * \details
     * If one of the operands is a `uint64` and the other a
     * `int64`, and the values are equal, returns `true`. Otherwise, if the types are
     * different, returns always `false` (`float` and `double` values are considered to be
     * different between them). `NULL` values are equal to other `NULL` values.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR inline bool operator==(const field_view& rhs) const noexcept;

    /**
     * \brief Tests for inequality.
     * \par Exception safety
     * No-throw guarantee.
     */
    BOOST_CXX14_CONSTEXPR bool operator!=(const field_view& rhs) const noexcept { return !(*this == rhs); }

private:
    BOOST_CXX14_CONSTEXPR explicit field_view(detail::string_view_offset v, bool is_blob) noexcept
        : impl_{v, is_blob}
    {
    }

    BOOST_CXX14_CONSTEXPR explicit field_view(const detail::field_impl* v) noexcept : impl_{v} {}

    enum class internal_kind
    {
        null = 0,
        int64,
        uint64,
        string,
        blob,
        float_,
        double_,
        date,
        datetime,
        time,
        sv_offset_string,
        sv_offset_blob,
        field_ptr
    };

    union repr_t
    {
        std::int64_t int64;
        std::uint64_t uint64;
        string_view string;
        blob_view blob;
        float float_;
        double double_;
        date date_;
        datetime datetime_;
        time time_;
        detail::string_view_offset sv_offset_;
        const detail::field_impl* field_ptr;

        BOOST_CXX14_CONSTEXPR repr_t() noexcept : int64{} {}
        BOOST_CXX14_CONSTEXPR repr_t(std::int64_t v) noexcept : int64(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(std::uint64_t v) noexcept : uint64(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(string_view v) noexcept : string{v} {}
        BOOST_CXX14_CONSTEXPR repr_t(blob_view v) noexcept : blob{v} {}
        BOOST_CXX14_CONSTEXPR repr_t(float v) noexcept : float_(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(double v) noexcept : double_(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(date v) noexcept : date_(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(datetime v) noexcept : datetime_(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(time v) noexcept : time_(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(detail::string_view_offset v) noexcept : sv_offset_(v) {}
        BOOST_CXX14_CONSTEXPR repr_t(const detail::field_impl* v) noexcept : field_ptr(v) {}
    };

    struct impl_t
    {
        internal_kind ikind{internal_kind::null};
        repr_t repr{};

        // Required by lib internal functions
        bool is_string_offset() const noexcept { return ikind == internal_kind::sv_offset_string; }
        bool is_blob_offset() const noexcept { return ikind == internal_kind::sv_offset_blob; }

        BOOST_CXX14_CONSTEXPR impl_t() = default;
        BOOST_CXX14_CONSTEXPR impl_t(std::int64_t v) noexcept : ikind(internal_kind::int64), repr(v) {}
        BOOST_CXX14_CONSTEXPR impl_t(std::uint64_t v) noexcept : ikind(internal_kind::uint64), repr(v) {}
        BOOST_CXX14_CONSTEXPR impl_t(string_view v) noexcept : ikind(internal_kind::string), repr{v} {}
        BOOST_CXX14_CONSTEXPR impl_t(blob_view v) noexcept : ikind(internal_kind::blob), repr{v} {}
        BOOST_CXX14_CONSTEXPR impl_t(float v) noexcept : ikind(internal_kind::float_), repr(v) {}
        BOOST_CXX14_CONSTEXPR impl_t(double v) noexcept : ikind(internal_kind::double_), repr(v) {}
        BOOST_CXX14_CONSTEXPR impl_t(date v) noexcept : ikind(internal_kind::date), repr(v) {}
        BOOST_CXX14_CONSTEXPR impl_t(datetime v) noexcept : ikind(internal_kind::datetime), repr(v) {}
        BOOST_CXX14_CONSTEXPR impl_t(time v) noexcept : ikind(internal_kind::time), repr(v) {}
        BOOST_CXX14_CONSTEXPR impl_t(detail::string_view_offset v, bool is_blob) noexcept
            : ikind(is_blob ? internal_kind::sv_offset_blob : internal_kind::sv_offset_string), repr{v}
        {
        }
        BOOST_CXX14_CONSTEXPR impl_t(const detail::field_impl* v) noexcept
            : ikind(internal_kind::field_ptr), repr(v)
        {
        }
    } impl_;

    BOOST_CXX14_CONSTEXPR bool is_field_ptr() const noexcept
    {
        return impl_.ikind == internal_kind::field_ptr;
    }
    BOOST_CXX14_CONSTEXPR inline void check_kind(internal_kind expected) const;

#ifndef BOOST_MYSQL_DOXYGEN
    friend class field;
    friend struct detail::access;
    BOOST_MYSQL_DECL
    friend std::ostream& operator<<(std::ostream& os, const field_view& v);
#endif
};

/**
 * \relates field_view
 * \brief Streams a `field_view`.
 */
BOOST_MYSQL_DECL
std::ostream& operator<<(std::ostream& os, const field_view& v);

}  // namespace mysql
}  // namespace boost

#include <boost/mysql/impl/field_view.hpp>
#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/field_view.ipp>
#endif

#endif
