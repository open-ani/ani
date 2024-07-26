//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_FORMAT_SQL_IPP
#define BOOST_MYSQL_IMPL_FORMAT_SQL_IPP

#include <boost/mysql/blob_view.hpp>
#include <boost/mysql/character_set.hpp>
#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/format_sql.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/escape_string.hpp>
#include <boost/mysql/detail/format_sql.hpp>
#include <boost/mysql/detail/output_string.hpp>

#include <boost/mysql/impl/internal/byte_to_hex.hpp>
#include <boost/mysql/impl/internal/call_next_char.hpp>
#include <boost/mysql/impl/internal/dt_to_string.hpp>

#include <boost/charconv/from_chars.hpp>
#include <boost/charconv/to_chars.hpp>
#include <boost/system/result.hpp>
#include <boost/system/system_error.hpp>
#include <boost/throw_exception.hpp>

#include <cmath>
#include <cstddef>
#include <limits>
#include <string>

namespace boost {
namespace mysql {
namespace detail {

// Helpers to format fundamental types
inline void append_identifier(string_view name, format_context_base& ctx)
{
    auto& impl = access::get_impl(ctx);
    auto ec = detail::escape_string(name, impl.opts, '`', impl.output);
    if (ec)
        ctx.add_error(ec);
}

template <class T>
void append_int(T integer, format_context_base& ctx)
{
    // Make sure our buffer is big enough. 2: sign + digits10 is only 1 below max
    constexpr std::size_t buffsize = 32;
    static_assert(2 + std::numeric_limits<double>::digits10 < buffsize, "");

    char buff[buffsize];

    auto res = charconv::to_chars(buff, buff + buffsize, integer);

    // Can only fail becuase of buffer being too small
    BOOST_ASSERT(res.ec == std::errc());

    // Copy
    access::get_impl(ctx).output.append(string_view(buff, res.ptr - buff));
}

inline void append_double(double number, format_context_base& ctx)
{
    // Make sure our buffer is big enough. 4: sign, radix point, e+
    // 3: max exponent digits
    constexpr std::size_t buffsize = 32;
    static_assert(4 + std::numeric_limits<double>::max_digits10 + 3 < buffsize, "");

    // inf and nan are not supported by MySQL
    if (std::isinf(number) || std::isnan(number))
    {
        ctx.add_error(client_errc::unformattable_value);
        return;
    }

    char buff[buffsize];

    // We format as scientific to make MySQL understand the number as a double.
    // Otherwise, it takes it as a DECIMAL.
    auto res = charconv::to_chars(buff, buff + buffsize, number, charconv::chars_format::scientific);

    // Can only fail because of buffer being too small
    BOOST_ASSERT(res.ec == std::errc());

    // Copy
    access::get_impl(ctx).output.append(string_view(buff, res.ptr - buff));
}

inline void append_quoted_string(string_view str, format_context_base& ctx)
{
    auto& impl = access::get_impl(ctx);
    impl.output.append("'");
    auto ec = detail::escape_string(str, impl.opts, '\'', impl.output);
    if (ec)
        ctx.add_error(ec);
    impl.output.append("'");
}

inline void append_blob(blob_view b, format_context_base& ctx)
{
    // Blobs have a binary character set, which may include characters
    // that are not valid in the current character set. However, escaping
    // is always performed using the character_set_connection.
    // mysql_real_escape_string escapes multibyte characters with a backslash,
    // but this behavior is not documented, so we don't want to rely on it.
    // The most reliable way to encode blobs is using hex strings.

    // Output string
    auto output = access::get_impl(ctx).output;

    // We output characters to a temporary buffer, batching append calls
    constexpr std::size_t buffer_size = 64;
    char buffer[buffer_size]{};
    char* it = buffer;
    char* const end = buffer + buffer_size;

    // Binary string introducer
    output.append("x'");

    // Serialize contents
    for (unsigned char byte : b)
    {
        // Serialize the byte
        it = byte_to_hex(byte, it);

        // If we filled the buffer, dump it
        if (it == end)
        {
            output.append({buffer, buffer_size});
            it = buffer;
        }
    }

    // Dump anything that didn't fill the buffer
    output.append({buffer, static_cast<std::size_t>(it - buffer)});

    // Closing quote
    ctx.append_raw("'");
}

inline void append_quoted_date(date d, format_context_base& ctx)
{
    char buffer[34];
    buffer[0] = '\'';
    std::size_t sz = detail::date_to_string(d.year(), d.month(), d.day(), span<char, 32>(buffer + 1, 32));
    buffer[sz + 1] = '\'';
    access::get_impl(ctx).output.append(string_view(buffer, sz + 2));
}

inline void append_quoted_datetime(datetime d, format_context_base& ctx)
{
    char buffer[66];
    buffer[0] = '\'';
    std::size_t sz = detail::datetime_to_string(
        d.year(),
        d.month(),
        d.day(),
        d.hour(),
        d.minute(),
        d.second(),
        d.microsecond(),
        span<char, 64>(buffer + 1, 64)
    );
    buffer[sz + 1] = '\'';
    access::get_impl(ctx).output.append(string_view(buffer, sz + 2));
}

inline void append_quoted_time(time t, format_context_base& ctx)
{
    char buffer[66];
    buffer[0] = '\'';
    std::size_t sz = time_to_string(t, span<char, 64>(buffer + 1, 64));
    buffer[sz + 1] = '\'';
    access::get_impl(ctx).output.append(string_view(buffer, sz + 2));
}

inline void append_field_view(field_view fv, format_context_base& ctx)
{
    switch (fv.kind())
    {
    case field_kind::null: ctx.append_raw("NULL"); return;
    case field_kind::int64: return append_int(fv.get_int64(), ctx);
    case field_kind::uint64: return append_int(fv.get_uint64(), ctx);
    case field_kind::float_:
        // float is formatted as double because it's parsed as such
        return append_double(fv.get_float(), ctx);
    case field_kind::double_: return append_double(fv.get_double(), ctx);
    case field_kind::string: return append_quoted_string(fv.get_string(), ctx);
    case field_kind::blob: return append_blob(fv.get_blob(), ctx);
    case field_kind::date: return append_quoted_date(fv.get_date(), ctx);
    case field_kind::datetime: return append_quoted_datetime(fv.get_datetime(), ctx);
    case field_kind::time: return append_quoted_time(fv.get_time(), ctx);
    default: BOOST_ASSERT(false); return;
    }
}

// Helpers for parsing format strings
inline bool is_number(char c) noexcept { return c >= '0' && c <= '9'; }

inline bool is_name_start(char c) noexcept
{
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
}

class format_state
{
    format_context_base& ctx_;
    span<const format_arg> args_;

    // Borrowed from fmt
    // 0: we haven't used any args yet
    // -1: we're doing explicit indexing
    // >0: we're doing auto indexing
    int next_arg_id_{0};

    BOOST_ATTRIBUTE_NODISCARD
    bool advance(const char*& it, const char* end)
    {
        std::size_t size = detail::call_next_char(ctx_.impl_.opts.charset, it, end);
        if (size == 0)
        {
            ctx_.add_error(client_errc::format_string_invalid_encoding);
            return false;
        }
        it += size;
        return true;
    }

    bool uses_auto_ids() const noexcept { return next_arg_id_ > 0; }
    bool uses_explicit_ids() const noexcept { return next_arg_id_ == -1; }

    void do_field(format_arg arg) { ctx_.format_arg(access::get_impl(arg).value); }

    BOOST_ATTRIBUTE_NODISCARD
    bool do_indexed_field(int arg_id)
    {
        BOOST_ASSERT(arg_id >= 0);
        if (static_cast<std::size_t>(arg_id) >= args_.size())
        {
            ctx_.add_error(client_errc::format_arg_not_found);
            return false;
        }
        do_field(args_[arg_id]);
        return true;
    }

    BOOST_ATTRIBUTE_NODISCARD
    bool parse_field(const char*& it, const char* format_end)
    {
        // {{ : escape for brace
        // {}:  auto field
        // {integer}: index
        // {identifier}: named field
        // All characters until the closing } must be ASCII, otherwise the format string is not valid

        if (it == format_end)
        {
            ctx_.add_error(client_errc::format_string_invalid_syntax);
            return false;
        }
        else if (*it == '{')
        {
            ctx_.append_raw("{");
            ++it;
            return true;
        }
        else if (*it == '}')
        {
            ++it;
            return append_auto_field();
        }
        else if (is_number(*it))
        {
            unsigned short field_index = 0;
            auto res = charconv::from_chars(it, format_end, field_index);
            if (res.ec != std::errc{})
            {
                ctx_.add_error(client_errc::format_string_invalid_syntax);
                return false;
            }
            it = res.ptr;
            if (it == format_end || *it++ != '}')
            {
                ctx_.add_error(client_errc::format_string_invalid_syntax);
                return false;
            }
            return append_indexed_field(static_cast<int>(field_index));
        }
        else
        {
            const char* name_begin = it;
            if (!is_name_start(*it++))
            {
                ctx_.add_error(client_errc::format_string_invalid_syntax);
                return false;
            }
            while (it != format_end && (is_name_start(*it) || is_number(*it)))
                ++it;
            string_view field_name(name_begin, it);
            if (it == format_end || *it++ != '}')
            {
                ctx_.add_error(client_errc::format_string_invalid_syntax);
                return false;
            }
            return append_named_field(field_name);
        }
    }

    BOOST_ATTRIBUTE_NODISCARD
    bool append_named_field(string_view field_name)
    {
        // Find the argument
        for (const auto& arg : args_)
        {
            if (access::get_impl(arg).name == field_name)
            {
                do_field(arg);
                return true;
            }
        }

        // Not found
        ctx_.add_error(client_errc::format_arg_not_found);
        return false;
    }

    BOOST_ATTRIBUTE_NODISCARD
    bool append_indexed_field(int index)
    {
        if (uses_auto_ids())
        {
            ctx_.add_error(client_errc::format_string_manual_auto_mix);
            return false;
        }
        next_arg_id_ = -1;
        return do_indexed_field(index);
    }

    BOOST_ATTRIBUTE_NODISCARD
    bool append_auto_field()
    {
        if (uses_explicit_ids())
        {
            ctx_.add_error(client_errc::format_string_manual_auto_mix);
            return false;
        }
        return do_indexed_field(next_arg_id_++);
    }

public:
    format_state(format_context_base& ctx, span<const format_arg> args) noexcept : ctx_(ctx), args_(args) {}

    void format(string_view format_str)
    {
        // We can use operator++ when we know a character is ASCII. Some charsets
        // allow ASCII continuation bytes, so we need to skip the entire character othwerwise
        auto cur_begin = format_str.data();
        auto it = format_str.data();
        auto end = format_str.data() + format_str.size();
        while (it != end)
        {
            if (*it == '{')
            {
                // Found a replacement field. Dump the SQL we've been skipping and parse it
                ctx_.impl_.output.append({cur_begin, it});
                ++it;
                if (!parse_field(it, end))
                    return;
                cur_begin = it;
            }
            else if (*it == '}')
            {
                // A lonely } is only legal as a escape curly brace (i.e. }})
                ctx_.impl_.output.append({cur_begin, it});
                ++it;
                if (it == end || *it != '}')
                {
                    ctx_.add_error(client_errc::format_string_invalid_syntax);
                    return;
                }
                ctx_.impl_.output.append("}");
                ++it;
                cur_begin = it;
            }
            else
            {
                if (!advance(it, end))
                    return;
            }
        }

        // Dump any remaining SQL
        ctx_.impl_.output.append({cur_begin, end});
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

void boost::mysql::formatter<boost::mysql::identifier>::format(
    const identifier& value,
    format_context_base& ctx
)
{
    const auto& impl = detail::access::get_impl(value);

    ctx.append_raw("`");
    detail::append_identifier(impl.ids[0], ctx);
    if (impl.qual_level >= 2u)
    {
        ctx.append_raw("`.`");
        detail::append_identifier(impl.ids[1], ctx);
        if (impl.qual_level == 3u)
        {
            ctx.append_raw("`.`");
            detail::append_identifier(impl.ids[2], ctx);
        }
    }
    ctx.append_raw("`");
}

void boost::mysql::format_context_base::format_arg(detail::format_arg_value arg)
{
    if (arg.is_custom)
    {
        arg.data.custom.format_fn(arg.data.custom.obj, *this);
    }
    else
    {
        detail::append_field_view(arg.data.fv, *this);
    }
}

void boost::mysql::detail::vformat_sql_to(
    format_context_base& ctx,
    string_view format_str,
    std::initializer_list<format_arg> args
)
{
    detail::format_state(ctx, {args.begin(), args.end()}).format(format_str);
}

std::string boost::mysql::detail::vformat_sql(
    const format_options& opts,
    string_view format_str,
    std::initializer_list<format_arg> args
)
{
    format_context ctx(opts);
    detail::vformat_sql_to(ctx, format_str, args);
    return std::move(ctx).get().value();
}

#endif
