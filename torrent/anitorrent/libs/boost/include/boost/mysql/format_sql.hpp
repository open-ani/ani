//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_FORMAT_SQL_HPP
#define BOOST_MYSQL_FORMAT_SQL_HPP

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/constant_string_view.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/format_sql.hpp>
#include <boost/mysql/detail/output_string.hpp>

#include <boost/config.hpp>
#include <boost/core/span.hpp>
#include <boost/system/result.hpp>

#include <cstddef>
#include <initializer_list>
#include <string>
#include <type_traits>

namespace boost {
namespace mysql {

/**
 * \brief (EXPERIMENTAL) A named format argument, to be used in initializer lists.
 * \details
 * Represents a name, value pair to be passed to a formatting function.
 * This type should only be used in initializer lists, as a function argument.
 *
 * \par Object lifetimes
 * This is a non-owning type. Both the argument name and value are stored
 * as views.
 */
class format_arg
{
#ifndef BOOST_MYSQL_DOXYGEN
    struct
    {
        string_view name;
        detail::format_arg_value value;
    } impl_;

    friend struct detail::access;
#endif

public:
    /**
     * \brief Constructor.
     * \details
     * Constructs an argument from a name and a value.
     * value must satisfy the `Formattable` concept.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Both `name` and `value` are stored as views.
     */
    template <BOOST_MYSQL_FORMATTABLE Formattable>
    constexpr format_arg(string_view name, const Formattable& value) noexcept
        : impl_{name, detail::make_format_value(value)}
    {
    }
};

/**
 * \brief (EXPERIMENTAL) A SQL identifier to use for client-side SQL formatting.
 * \details
 * Represents a possibly-qualified SQL identifier.
 *
 * \par Object lifetimes
 * This type is non-owning, and should only be used as an argument to SQL formatting
 * functions.
 */
class identifier
{
    struct impl_t
    {
        std::size_t qual_level;
        string_view ids[3];

        constexpr impl_t(std::size_t qual_level, string_view id1, string_view id2, string_view id3) noexcept
            : qual_level(qual_level), ids{id1, id2, id3}
        {
        }
    } impl_;

#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
#endif

public:
    /**
     * \brief Constructs an unqualified identifier.
     * \details
     * Unqualified identifiers are usually field, table or database names,
     * and get formatted as: \code "`column_name`" \endcode
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    constexpr explicit identifier(string_view id) noexcept : impl_(1u, id, {}, {}) {}

    /**
     * \brief Constructs an identifier with a single qualifier.
     * \details
     * Identifiers with one qualifier are used for field, table and view names.
     * The qualifier identifies the parent object. For instance,
     * `identifier("table_name", "field_name")` maps to: \code "`table_name`.`field_name`" \endcode
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    constexpr identifier(string_view qualifier, string_view id) noexcept : impl_(2u, qualifier, id, {}) {}

    /**
     * \brief Constructs an identifier with two qualifiers.
     * \details
     * Identifiers with two qualifier are used for field names.
     * The first qualifier identifies the database, the second, the table name.
     * For instance, `identifier("db", "table_name", "field_name")` maps to:
     * \code "`db`.`table_name`.`field_name`" \endcode
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    constexpr identifier(string_view qual1, string_view qual2, string_view id) noexcept
        : impl_(3u, qual1, qual2, id)
    {
    }
};

/**
 * \brief (EXPERIMENTAL) An extension point to customize SQL formatting.
 * \details
 * This type can be specialized for custom types to make them formattable.
 * This makes them satisfy the `Formattable` concept, and usable in
 * \ref format_sql and \ref format_context_base::append_value.
 * \n
 * A `formatter` specialization for a type `T` should have the following form:
 * ```
 * template <> struct formatter<MyType> {
 *     static void format(const MyType&, format_context_base&);
 * }
 * ```
 * \n
 * Don't specialize `formatter` for built-in types, like `int`, `std::string` or
 * optionals (formally, any type satisfying `WritableField`). This is not supported
 * and will result in a compile-time error.
 */
template <class T>
struct formatter
#ifndef BOOST_MYSQL_DOXYGEN
    : detail::formatter_is_unspecialized
{
}
#endif
;

/**
 * \brief (EXPERIMENTAL) Base class for concrete format contexts.
 * \details
 * Conceptually, a format context contains: \n
 *   \li The result string. Output operations append characters to this output string.
 *       `format_context_base` is agnostic to the output string type.
 *   \li \ref format_options required to format values.
 *   \li An error state (\ref error_state) that is set by output operations when they fail.
 *       The error state is propagated to \ref basic_format_context::get.
 * \n
 * References to this class are useful when you need to manipulate
 * a format context without knowing the type of the actual context that will be used,
 * like when specializing \ref formatter.
 * \n
 * This class can't be
 * instantiated directly - use \ref basic_format_context, instead.
 * Do not subclass it, either.
 */
class format_context_base
{
#ifndef BOOST_MYSQL_DOXYGEN
    struct
    {
        detail::output_string_ref output;
        format_options opts;
        error_code ec;
    } impl_;

    friend struct detail::access;
    friend class detail::format_state;
#endif

    BOOST_MYSQL_DECL void format_arg(detail::format_arg_value arg);

protected:
    format_context_base(detail::output_string_ref out, format_options opts, error_code ec = {}) noexcept
        : impl_{out, opts, ec}
    {
    }

    format_context_base(detail::output_string_ref out, const format_context_base& rhs) noexcept
        : impl_{out, rhs.impl_.opts, rhs.impl_.ec}
    {
    }

    void assign(const format_context_base& rhs) noexcept
    {
        // output never changes, it always points to the derived object's storage
        impl_.opts = rhs.impl_.opts;
        impl_.ec = rhs.impl_.ec;
    }

public:
    /**
     * \brief Adds raw SQL to the output string.
     * \details
     * Adds raw, unescaped SQL to the output string. Doesn't alter the error state.
     * \n
     * By default, the passed SQL should be available at compile-time.
     * Use \ref runtime if you need to use runtime values.
     *
     * \par Exception safety
     * Basic guarantee. Memory allocations may throw.
     *
     * \par Object lifetimes
     * The passed string is copied as required and doesn't need to be kept alive.
     */
    format_context_base& append_raw(constant_string_view sql)
    {
        impl_.output.append(sql.get());
        return *this;
    }

    /**
     * \brief Formats a value and adds it to the output string.
     * \details
     * value is formatted according to its type. If formatting
     * generates an error (for instance, a string with invalid encoding is passed),
     * the error state may be set.
     * \n
     * The supplied type must satisfy the `Formattable` concept.
     *
     * \par Exception safety
     * Basic guarantee. Memory allocations may throw.
     *
     * \par Errors
     * The error state may be updated with the following errors: \n
     * \li \ref client_errc::invalid_encoding if a string with byte sequences that can't be decoded
     *          with the current character set is passed.
     * \li \ref client_errc::unformattable_value if a NaN or infinity `float` or `double` is passed.
     * \li Any other error code that user-supplied formatter specializations may add using \ref add_error.
     */
    template <BOOST_MYSQL_FORMATTABLE Formattable>
    format_context_base& append_value(const Formattable& v)
    {
        format_arg(detail::make_format_value(v));
        return *this;
    }

    /**
     * \brief Adds an error to the current error state.
     * \details
     * This function can be used by custom formatters to report that they
     * received a value that can't be formatted. For instance, it's used by
     * the built-in string formatter when a string with an invalid encoding is supplied.
     * \n
     * If the error state is not set before calling this function, the error
     * state is updated to `ec`. Otherwise, the error is ignored.
     * This implies that once the error state is set, it can't be reset.
     * \n
     * \par Exception safety
     * No-throw guarantee.
     */
    void add_error(error_code ec) noexcept
    {
        if (!impl_.ec)
            impl_.ec = ec;
    }

    /**
     * \brief Retrieves the current error state.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    error_code error_state() const noexcept { return impl_.ec; }

    /**
     * \brief Retrieves the format options.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    format_options format_opts() const noexcept { return impl_.opts; }
};

/**
 * \brief (EXPERIMENTAL) Format context for incremental SQL formatting.
 * \details
 * The primary interface for incremental SQL formatting. Contrary to \ref format_context_base,
 * this type is aware of the output string's actual type. `basic_format_context` owns
 * an instance of `OutputString`. Format operations will append characters to such string.
 * \n
 * Objects of this type are single-use: once the result has been retrieved using \ref get,
 * they cannot be re-used. This is a move-only type.
 */
template <BOOST_MYSQL_OUTPUT_STRING OutputString>
class basic_format_context : public format_context_base
{
    OutputString output_{};

    detail::output_string_ref ref() noexcept { return detail::output_string_ref::create(output_); }

public:
    /**
     * \brief Constructor.
     * \details
     * Uses a default-constructed `OutputString` as output string, and an empty
     * error code as error state. This constructor can only be called if `OutputString`
     * is default-constructible.
     *
     * \par Exception safety
     * Strong guarantee: exceptions thrown by default-constructing `OutputString` are propagated.
     */
    explicit basic_format_context(format_options opts
    ) noexcept(std::is_nothrow_default_constructible<OutputString>::value)
        : format_context_base(ref(), opts)
    {
    }

    /**
     * \brief Constructor.
     * \details
     * Move constructs an `OutputString` using `storage`. After construction,
     * the output string is cleared. Uses an empty
     * error code as error state. This constructor allows re-using existing
     * memory for the output string.
     * \n
     *
     * \par Exception safety
     * Basic guarantee: exceptions thrown by move-constructing `OutputString` are propagated.
     */
    basic_format_context(format_options opts, OutputString&& storage) noexcept(
        std::is_nothrow_move_constructible<OutputString>::value
    )
        : format_context_base(ref(), opts), output_(std::move(storage))
    {
        output_.clear();
    }

#ifndef BOOST_MYSQL_DOXYGEN
    basic_format_context(const basic_format_context&) = delete;
    basic_format_context& operator=(const basic_format_context&) = delete;
#endif

    /**
     * \brief Move constructor.
     * \details
     * Move constructs an `OutputString` using `rhs`'s output string.
     * `*this` will have the same format options and error state than `rhs`.
     * `rhs` is left in a valid but unspecified state.
     *
     * \par Exception safety
     * Basic guarantee: exceptions thrown by move-constructing `OutputString` are propagated.
     */
    basic_format_context(basic_format_context&& rhs
    ) noexcept(std::is_nothrow_move_constructible<OutputString>::value)
        : format_context_base(ref(), rhs), output_(std::move(rhs.output_))
    {
    }

    /**
     * \brief Move assignment.
     * \details
     * Move assigns `rhs`'s output string to `*this` output string.
     * `*this` will have the same format options and error state than `rhs`.
     * `rhs` is left in a valid but unspecified state.
     *
     * \par Exception safety
     * Basic guarantee: exceptions thrown by move-constructing `OutputString` are propagated.
     */
    basic_format_context& operator=(basic_format_context&& rhs
    ) noexcept(std::is_nothrow_move_assignable<OutputString>::value)
    {
        output_ = std::move(rhs.output_);
        assign(rhs);
        return *this;
    }

    /**
     * \brief Retrieves the result of the formatting operation.
     * \details
     * After running the relevant formatting operations (using \ref append_raw,
     * \ref append_value or \ref format_sql_to), call this function to retrieve the
     * overall result of the operation.
     * \n
     * If \ref error_state is a non-empty error code, returns it as an error.
     * Otherwise, returns the output string, move-constructing it into the `system::result` object.
     * \n
     * This function is move-only: once called, `*this` is left in a valid but unspecified state.
     *
     * \par Exception safety
     * Basic guarantee: exceptions thrown by move-constructing `OutputString` are propagated.
     */
    system::result<OutputString> get() && noexcept(std::is_nothrow_move_constructible<OutputString>::value)
    {
        auto ec = error_state();
        if (ec)
            return ec;
        return std::move(output_);
    }
};

/**
 * \brief (EXPERIMENTAL) Format context for incremental SQL formatting.
 * \details
 * Convenience type alias for `basic_format_context`'s most common case.
 */
using format_context = basic_format_context<std::string>;

template <>
struct formatter<identifier>
{
    BOOST_MYSQL_DECL
    static void format(const identifier& value, format_context_base& ctx);
};

/**
 * \brief (EXPERIMENTAL) Composes a SQL query client-side appending it to a format context.
 * \details
 * Parses `format_str` as a format string, substituting replacement fields (like `{}`, `{1}` or `{name}`)
 * by formatted arguments, extracted from `args`.
 * \n
 * Formatting is performed as if \ref format_context_base::append_raw and
 * \ref format_context_base::append_value were called on `ctx`, effectively appending
 * characters to its output string.
 * \n
 * Compared to \ref format_sql, this function is more flexible, allowing the following use cases: \n
 *   \li Appending characters to an existing context. Can be used to concatenate the output of successive
 *       format operations efficiently.
 *   \li Using string types different to `std::string` (works with any \ref basic_format_context).
 *   \li Avoiding exceptions (see \ref basic_format_context::get).
 *
 *
 * \par Exception safety
 * Basic guarantee. Memory allocations may throw.
 *
 * \par Errors
 * \li \ref client_errc::invalid_encoding if `args` contains a string with byte sequences
 *     that can't be decoded with the current character set.
 * \li \ref client_errc::unformattable_value if `args` contains a floating-point value
 *     that is NaN or infinity.
 * \li Any other error generated by user-defined \ref formatter specializations.
 * \li \ref client_errc::format_string_invalid_syntax if `format_str` can't be parsed as
 *     a format string.
 * \li \ref client_errc::format_string_invalid_encoding if `format_str` contains byte byte sequences
 *     that can't be decoded with the current character set.
 * \li \ref client_errc::format_string_manual_auto_mix if `format_str` contains a mix of automatic
 *     (`{}`) and manual indexed (`{1}`) replacement fields.
 * \li \ref client_errc::format_arg_not_found if an argument referenced by `format_str` isn't present
 *     in `args` (there aren't enough arguments or a named argument is not found).
 */
template <BOOST_MYSQL_FORMATTABLE... Formattable>
void format_sql_to(format_context_base& ctx, constant_string_view format_str, const Formattable&... args)
{
    std::initializer_list<format_arg> args_il{
        {string_view(), args}
        ...
    };
    detail::vformat_sql_to(ctx, format_str.get(), args_il);
}

/**
 * \copydoc format_sql_to
 * \details
 * \n
 * This overload allows using named arguments.
 */
inline void format_sql_to(
    format_context_base& ctx,
    constant_string_view format_str,
    std::initializer_list<format_arg> args
)
{
    detail::vformat_sql_to(ctx, format_str.get(), args);
}

/**
 * \brief (EXPERIMENTAL) Composes a SQL query client-side.
 * \details
 * Parses `format_str` as a format string, substituting replacement fields (like `{}`, `{1}` or `{name}`)
 * by formatted arguments, extracted from `args`. `opts` is using to parse the string and format string
 * arguments.
 * \n
 * Formatting is performed as if \ref format_context::append_raw and \ref format_context::append_value
 * were called on a context created by this function.
 * \n
 *
 * \par Exception safety
 * Strong guarantee. Memory allocations may throw. `boost::system::system_error` is thrown if an error
 * is found while formatting. See below for more info.
 *
 * \par Errors
 * \li \ref client_errc::invalid_encoding if `args` contains a string with byte sequences
 *     that can't be decoded with the current character set.
 * \li \ref client_errc::unformattable_value if `args` contains a floating-point value
 *     that is NaN or infinity.
 * \li Any other error generated by user-defined \ref formatter specializations.
 * \li \ref client_errc::format_string_invalid_syntax if `format_str` can't be parsed as
 *     a format string.
 * \li \ref client_errc::format_string_invalid_encoding if `format_str` contains byte byte sequences
 *     that can't be decoded with the current character set.
 * \li \ref client_errc::format_string_manual_auto_mix if `format_str` contains a mix of automatic
 *     (`{}`) and manual indexed (`{1}`) replacement fields.
 * \li \ref client_errc::format_arg_not_found if an argument referenced by `format_str` isn't present
 *     in `args` (there aren't enough arguments or a named argument is not found).
 */
template <BOOST_MYSQL_FORMATTABLE... Formattable>
std::string format_sql(
    const format_options& opts,
    constant_string_view format_str,
    const Formattable&... args
)
{
    std::initializer_list<format_arg> args_il{
        {string_view(), args}
        ...
    };
    return detail::vformat_sql(opts, format_str.get(), args_il);
}

/**
 * \copydoc format_sql
 * \details
 * \n
 * This overload allows using named arguments.
 */
inline std::string format_sql(
    const format_options& opts,
    constant_string_view format_str,
    std::initializer_list<format_arg> args
)
{
    return detail::vformat_sql(opts, format_str.get(), args);
}

}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/format_sql.ipp>
#endif

#endif
