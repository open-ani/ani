// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2017-2023 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2013-2022.
// Modifications copyright (c) 2013-2022 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_RESULT_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_RESULT_HPP

#include <algorithm>
#include <cstddef>
#include <cstring>
#include <string>
#include <tuple>
#include <type_traits>

#include <boost/throw_exception.hpp>

#include <boost/geometry/core/assert.hpp>
#include <boost/geometry/core/coordinate_dimension.hpp>
#include <boost/geometry/core/exception.hpp>
#include <boost/geometry/core/static_assert.hpp>
#include <boost/geometry/util/constexpr.hpp>
#include <boost/geometry/util/sequence.hpp>

namespace boost { namespace geometry {

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace relate {

enum field { interior = 0, boundary = 1, exterior = 2 };

// TODO: IF THE RESULT IS UPDATED WITH THE MAX POSSIBLE VALUE FOR SOME PAIR OF GEOEMTRIES
// THE VALUE ALREADY STORED MUSN'T BE CHECKED
// update() calls chould be replaced with set() in those cases
// but for safety reasons (STATIC_ASSERT) we should check if parameter D is valid and set() doesn't do that
// so some additional function could be added, e.g. set_dim()


template <typename MatrixOrMask, field F1, field F2>
using fields_in_bounds = util::bool_constant
    <
        (F1 < MatrixOrMask::static_height && F2 < MatrixOrMask::static_width)
    >;

// --------------- MATRIX ----------------

// matrix

template <std::size_t Height, std::size_t Width = Height>
class matrix
{
public:
    typedef char value_type;
    typedef std::size_t size_type;
    typedef const char * const_iterator;
    typedef const_iterator iterator;

    static const std::size_t static_width = Width;
    static const std::size_t static_height = Height;
    static const std::size_t static_size = Width * Height;

    inline matrix()
    {
        std::fill_n(m_array, static_size, 'F');
    }

    template
    <
        field F1, field F2,
        std::enable_if_t<fields_in_bounds<matrix, F1, F2>::value, int> = 0
    >
    inline char get() const
    {
        static const std::size_t index = F1 * Width + F2;
        BOOST_STATIC_ASSERT(index < static_size);
        return m_array[index];
    }

    template
    <
        field F1, field F2, char V,
        std::enable_if_t<fields_in_bounds<matrix, F1, F2>::value, int> = 0
    >
    inline void set()
    {
        static const std::size_t index = F1 * Width + F2;
        BOOST_STATIC_ASSERT(index < static_size);
        m_array[index] = V;
    }

    inline char operator[](std::size_t index) const
    {
        BOOST_GEOMETRY_ASSERT(index < static_size);
        return m_array[index];
    }

    inline const_iterator begin() const
    {
        return m_array;
    }

    inline const_iterator end() const
    {
        return m_array + static_size;
    }

    inline static std::size_t size()
    {
        return static_size;
    }

    inline const char * data() const
    {
        return m_array;
    }

    inline std::string str() const
    {
        return std::string(m_array, static_size);
    }

private:
    char m_array[static_size];
};

// matrix_handler

template <typename Matrix>
class matrix_handler
{
public:
    typedef Matrix result_type;

    static const bool interrupt = false;

    matrix_handler()
    {}

    result_type const& result() const
    {
        return m_matrix;
    }

    result_type const& matrix() const
    {
        return m_matrix;
    }

    result_type & matrix()
    {
        return m_matrix;
    }

    template <field F1, field F2, char D>
    inline bool may_update() const
    {
        BOOST_STATIC_ASSERT('0' <= D && D <= '9');
        char const c = m_matrix.template get<F1, F2>();
        return D > c || c > '9';
    }

    template <field F1, field F2, char V>
    inline void update()
    {
        BOOST_STATIC_ASSERT(('0' <= V && V <= '9') || V == 'T');
        char const c = m_matrix.template get<F1, F2>();
        // If c == T and V == T it will be set anyway but that's fine
        if (V > c || c > '9')
        {
            m_matrix.template set<F1, F2, V>();
        }
    }

    template <field F1, field F2, char V>
    inline void set()
    {
        BOOST_STATIC_ASSERT(('0' <= V && V <= '9') || V == 'T');
        m_matrix.template set<F1, F2, V>();
    }

    template <field F1, field F2>
    inline char get() const
    {
        return m_matrix.template get<F1, F2>();
    }

private:
    Matrix m_matrix;
};

// --------------- RUN-TIME MASK ----------------

// run-time mask

template <std::size_t Height, std::size_t Width = Height>
class mask
{
public:
    static const std::size_t static_width = Width;
    static const std::size_t static_height = Height;
    static const std::size_t static_size = Width * Height;

    inline mask(const char * s)
    {
        char * it = m_array;
        char * const last = m_array + static_size;
        for ( ; it != last && *s != '\0' ; ++it, ++s )
        {
            char c = *s;
            check_char(c);
            *it = c;
        }
        if ( it != last )
        {
            std::fill(it, last, '*');
        }
    }

    inline mask(const char * s, std::size_t count)
    {
        if ( count > static_size )
        {
            count = static_size;
        }
        if ( count > 0 )
        {
            std::for_each(s, s + count, check_char);
            std::copy_n(s, count, m_array);
        }
        if ( count < static_size )
        {
            std::fill_n(m_array + count, static_size - count, '*');
        }
    }

    template
    <
        field F1, field F2,
        std::enable_if_t<fields_in_bounds<mask, F1, F2>::value, int> = 0
    >
    inline char get() const
    {
        static const std::size_t index = F1 * Width + F2;
        BOOST_STATIC_ASSERT(index < static_size);
        return m_array[index];
    }

private:
    static inline void check_char(char c)
    {
        bool const is_valid = c == '*' || c == 'T' || c == 'F'
                         || ( c >= '0' && c <= '9' );
        if ( !is_valid )
        {
            BOOST_THROW_EXCEPTION(geometry::invalid_input_exception());
        }
    }

    char m_array[static_size];
};

// interrupt()

template <typename Mask, bool InterruptEnabled>
struct interrupt_dispatch
{
    template <field F1, field F2, char V>
    static inline bool apply(Mask const&)
    {
        return false;
    }
};

template <typename Mask>
struct interrupt_dispatch<Mask, true>
{
    template <field F1, field F2, char V>
    static inline bool apply(Mask const& mask)
    {
        char m = mask.template get<F1, F2>();
        return check_element<V>(m);
    }

    template <char V>
    static inline bool check_element(char m)
    {
        if BOOST_GEOMETRY_CONSTEXPR (V >= '0' && V <= '9')
        {
            return m == 'F' || ( m < V && m >= '0' && m <= '9' );
        }
        else if BOOST_GEOMETRY_CONSTEXPR (V == 'T')
        {
            return m == 'F';
        }
        else
        {
            return false;
        }
    }
};

template <typename Masks, int I = 0, int N = std::tuple_size<Masks>::value>
struct interrupt_dispatch_tuple
{
    template <field F1, field F2, char V>
    static inline bool apply(Masks const& masks)
    {
        typedef typename std::tuple_element<I, Masks>::type mask_type;
        mask_type const& mask = std::get<I>(masks);
        return interrupt_dispatch<mask_type, true>::template apply<F1, F2, V>(mask)
            && interrupt_dispatch_tuple<Masks, I+1>::template apply<F1, F2, V>(masks);
    }
};

template <typename Masks, int N>
struct interrupt_dispatch_tuple<Masks, N, N>
{
    template <field F1, field F2, char V>
    static inline bool apply(Masks const& )
    {
        return true;
    }
};

template <typename ...Masks>
struct interrupt_dispatch<std::tuple<Masks...>, true>
{
    typedef std::tuple<Masks...> mask_type;

    template <field F1, field F2, char V>
    static inline bool apply(mask_type const& mask)
    {
        return interrupt_dispatch_tuple<mask_type>::template apply<F1, F2, V>(mask);
    }
};

template <field F1, field F2, char V, bool InterruptEnabled, typename Mask>
inline bool interrupt(Mask const& mask)
{
    return interrupt_dispatch<Mask, InterruptEnabled>
                ::template apply<F1, F2, V>(mask);
}

// may_update()

template <typename Mask>
struct may_update_dispatch
{
    template <field F1, field F2, char D, typename Matrix>
    static inline bool apply(Mask const& mask, Matrix const& matrix)
    {
        BOOST_STATIC_ASSERT('0' <= D && D <= '9');

        char const m = mask.template get<F1, F2>();

        if ( m == 'F' )
        {
            return true;
        }
        else if ( m == 'T' )
        {
            char const c = matrix.template get<F1, F2>();
            return c == 'F'; // if it's T or between 0 and 9, the result will be the same
        }
        else if ( m >= '0' && m <= '9' )
        {
            char const c = matrix.template get<F1, F2>();
            return D > c || c > '9';
        }

        return false;
    }
};

template <typename Masks, int I = 0, int N = std::tuple_size<Masks>::value>
struct may_update_dispatch_tuple
{
    template <field F1, field F2, char D, typename Matrix>
    static inline bool apply(Masks const& masks, Matrix const& matrix)
    {
        typedef typename std::tuple_element<I, Masks>::type mask_type;
        mask_type const& mask = std::get<I>(masks);
        return may_update_dispatch<mask_type>::template apply<F1, F2, D>(mask, matrix)
            || may_update_dispatch_tuple<Masks, I+1>::template apply<F1, F2, D>(masks, matrix);
    }
};

template <typename Masks, int N>
struct may_update_dispatch_tuple<Masks, N, N>
{
    template <field F1, field F2, char D, typename Matrix>
    static inline bool apply(Masks const& , Matrix const& )
    {
        return false;
    }
};

template <typename ...Masks>
struct may_update_dispatch<std::tuple<Masks...>>
{
    typedef std::tuple<Masks...> mask_type;

    template <field F1, field F2, char D, typename Matrix>
    static inline bool apply(mask_type const& mask, Matrix const& matrix)
    {
        return may_update_dispatch_tuple<mask_type>::template apply<F1, F2, D>(mask, matrix);
    }
};

template <field F1, field F2, char D, typename Mask, typename Matrix>
inline bool may_update(Mask const& mask, Matrix const& matrix)
{
    return may_update_dispatch<Mask>
                ::template apply<F1, F2, D>(mask, matrix);
}

// check_matrix()

template <typename Mask>
struct check_dispatch
{
    template <typename Matrix>
    static inline bool apply(Mask const& mask, Matrix const& matrix)
    {
        return per_one<interior, interior>(mask, matrix)
            && per_one<interior, boundary>(mask, matrix)
            && per_one<interior, exterior>(mask, matrix)
            && per_one<boundary, interior>(mask, matrix)
            && per_one<boundary, boundary>(mask, matrix)
            && per_one<boundary, exterior>(mask, matrix)
            && per_one<exterior, interior>(mask, matrix)
            && per_one<exterior, boundary>(mask, matrix)
            && per_one<exterior, exterior>(mask, matrix);
    }

    template <field F1, field F2, typename Matrix>
    static inline bool per_one(Mask const& mask, Matrix const& matrix)
    {
        const char mask_el = mask.template get<F1, F2>();
        const char el = matrix.template get<F1, F2>();

        if ( mask_el == 'F' )
        {
            return el == 'F';
        }
        else if ( mask_el == 'T' )
        {
            return el == 'T' || ( el >= '0' && el <= '9' );
        }
        else if ( mask_el >= '0' && mask_el <= '9' )
        {
            return el == mask_el;
        }

        return true;
    }
};

template <typename Masks, int I = 0, int N = std::tuple_size<Masks>::value>
struct check_dispatch_tuple
{
    template <typename Matrix>
    static inline bool apply(Masks const& masks, Matrix const& matrix)
    {
        typedef typename std::tuple_element<I, Masks>::type mask_type;
        mask_type const& mask = std::get<I>(masks);
        return check_dispatch<mask_type>::apply(mask, matrix)
            || check_dispatch_tuple<Masks, I+1>::apply(masks, matrix);
    }
};

template <typename Masks, int N>
struct check_dispatch_tuple<Masks, N, N>
{
    template <typename Matrix>
    static inline bool apply(Masks const&, Matrix const&)
    {
        return false;
    }
};

template <typename ...Masks>
struct check_dispatch<std::tuple<Masks...>>
{
    typedef std::tuple<Masks...> mask_type;

    template <typename Matrix>
    static inline bool apply(mask_type const& mask, Matrix const& matrix)
    {
        return check_dispatch_tuple<mask_type>::apply(mask, matrix);
    }
};

template <typename Mask, typename Matrix>
inline bool check_matrix(Mask const& mask, Matrix const& matrix)
{
    return check_dispatch<Mask>::apply(mask, matrix);
}

// matrix_width

template <typename MatrixOrMask>
struct matrix_width
{
    static const std::size_t value = MatrixOrMask::static_width;
};

template <typename Tuple,
          int I = 0,
          int N = std::tuple_size<Tuple>::value>
struct matrix_width_tuple
{
    static const std::size_t
        current = matrix_width<typename std::tuple_element<I, Tuple>::type>::value;
    static const std::size_t
        next = matrix_width_tuple<Tuple, I+1>::value;

    static const std::size_t
        value = current > next ? current : next;
};

template <typename Tuple, int N>
struct matrix_width_tuple<Tuple, N, N>
{
    static const std::size_t value = 0;
};

template <typename ...Masks>
struct matrix_width<std::tuple<Masks...>>
{
    static const std::size_t
        value = matrix_width_tuple<std::tuple<Masks...>>::value;
};

// mask_handler

template <typename Mask, bool Interrupt>
class mask_handler
    : private matrix_handler
        <
            relate::matrix<matrix_width<Mask>::value>
        >
{
    typedef matrix_handler
        <
            relate::matrix<matrix_width<Mask>::value>
        > base_t;

public:
    typedef bool result_type;

    bool interrupt;

    inline explicit mask_handler(Mask const& m)
        : interrupt(false)
        , m_mask(m)
    {}

    result_type result() const
    {
        return !interrupt
            && check_matrix(m_mask, base_t::matrix());
    }

    template <field F1, field F2, char D>
    inline bool may_update() const
    {
        return detail::relate::may_update<F1, F2, D>(m_mask, base_t::matrix());
    }

    template <field F1, field F2, char V>
    inline void update()
    {
        if (relate::interrupt<F1, F2, V, Interrupt>(m_mask))
        {
            interrupt = true;
        }
        else
        {
            base_t::template update<F1, F2, V>();
        }
    }

    template <field F1, field F2, char V>
    inline void set()
    {
        if (relate::interrupt<F1, F2, V, Interrupt>(m_mask))
        {
            interrupt = true;
        }
        else
        {
            base_t::template set<F1, F2, V>();
        }
    }

    template <field F1, field F2>
    inline char get() const
    {
        return base_t::template get<F1, F2>();
    }

private:
    Mask const& m_mask;
};

// --------------- FALSE MASK ----------------

struct false_mask {};

// --------------- COMPILE-TIME MASK ----------------

// static_check_characters
template <typename Seq>
struct static_check_characters {};

template <char C, char ...Cs>
struct static_check_characters<std::integer_sequence<char, C, Cs...>>
    : static_check_characters<std::integer_sequence<char, Cs...>>
{
    typedef std::integer_sequence<char, C, Cs...> type;
    static const bool is_valid = (C >= '0' && C <= '9')
                               || C == 'T' || C == 'F' || C == '*';
    BOOST_GEOMETRY_STATIC_ASSERT((is_valid),
                                 "Invalid static mask character",
                                 type);
};

template <char ...Cs>
struct static_check_characters<std::integral_constant<char, Cs...>>
{};

// static_mask

template <typename Seq, std::size_t Height, std::size_t Width = Height>
struct static_mask
{
    static const std::size_t static_width = Width;
    static const std::size_t static_height = Height;
    static const std::size_t static_size = Width * Height;

    BOOST_STATIC_ASSERT(
        std::size_t(util::sequence_size<Seq>::value) == static_size);

    template <detail::relate::field F1, detail::relate::field F2>
    struct static_get
    {
        BOOST_STATIC_ASSERT(std::size_t(F1) < static_height);
        BOOST_STATIC_ASSERT(std::size_t(F2) < static_width);

        static const char value
            = util::sequence_element<F1 * static_width + F2, Seq>::value;
    };

private:
    // check static_mask characters
    enum { mask_check = sizeof(static_check_characters<Seq>) };
};

// static_should_handle_element

template
<
    typename StaticMask, field F1, field F2,
    bool IsSequence = util::is_sequence<StaticMask>::value
>
struct static_should_handle_element_dispatch
{
    static const char mask_el = StaticMask::template static_get<F1, F2>::value;
    static const bool value = mask_el == 'F'
                           || mask_el == 'T'
                           || ( mask_el >= '0' && mask_el <= '9' );
};

template
<
    typename Seq, field F1, field F2,
    std::size_t I = 0,
    std::size_t N = util::sequence_size<Seq>::value
>
struct static_should_handle_element_sequence
{
    typedef typename util::sequence_element<I, Seq>::type StaticMask;

    static const bool value
        = static_should_handle_element_dispatch
            <
                StaticMask, F1, F2
            >::value
       || static_should_handle_element_sequence
            <
                Seq, F1, F2, I + 1
            >::value;
};

template <typename Seq, field F1, field F2, std::size_t N>
struct static_should_handle_element_sequence<Seq, F1, F2, N, N>
{
    static const bool value = false;
};

template <typename StaticMask, field F1, field F2>
struct static_should_handle_element_dispatch<StaticMask, F1, F2, true>
{
    static const bool value
        = static_should_handle_element_sequence
            <
                StaticMask, F1, F2
            >::value;
};

template <typename StaticMask, field F1, field F2>
struct static_should_handle_element
{
    static const bool value
        = static_should_handle_element_dispatch
            <
                StaticMask, F1, F2
            >::value;
};

// static_interrupt

template
<
    typename StaticMask, char V, field F1, field F2,
    bool InterruptEnabled,
    bool IsSequence = util::is_sequence<StaticMask>::value
>
struct static_interrupt_dispatch
{
    static const bool value = false;
};

template <typename StaticMask, char V, field F1, field F2, bool IsSequence>
struct static_interrupt_dispatch<StaticMask, V, F1, F2, true, IsSequence>
{
    static const char mask_el = StaticMask::template static_get<F1, F2>::value;

    static const bool value
        = ( V >= '0' && V <= '9' ) ?
          ( mask_el == 'F' || ( mask_el < V && mask_el >= '0' && mask_el <= '9' ) ) :
          ( ( V == 'T' ) ? mask_el == 'F' : false );
};

template
<
    typename Seq, char V, field F1, field F2,
    std::size_t I = 0,
    std::size_t N = util::sequence_size<Seq>::value
>
struct static_interrupt_sequence
{
    typedef typename util::sequence_element<I, Seq>::type StaticMask;

    static const bool value
        = static_interrupt_dispatch
            <
                StaticMask, V, F1, F2, true
            >::value
       && static_interrupt_sequence
            <
                Seq, V, F1, F2, I + 1
            >::value;
};

template <typename Seq, char V, field F1, field F2, std::size_t N>
struct static_interrupt_sequence<Seq, V, F1, F2, N, N>
{
    static const bool value = true;
};

template <typename StaticMask, char V, field F1, field F2>
struct static_interrupt_dispatch<StaticMask, V, F1, F2, true, true>
{
    static const bool value
        = static_interrupt_sequence
            <
                StaticMask, V, F1, F2
            >::value;
};

template <typename StaticMask, char V, field F1, field F2, bool EnableInterrupt>
struct static_interrupt
{
    static const bool value
        = static_interrupt_dispatch
            <
                StaticMask, V, F1, F2, EnableInterrupt
            >::value;
};

// static_may_update

template
<
    typename StaticMask, char D, field F1, field F2,
    bool IsSequence = util::is_sequence<StaticMask>::value
>
struct static_may_update_dispatch
{
    static const char mask_el = StaticMask::template static_get<F1, F2>::value;
    static const int version
                        = mask_el == 'F' ? 0
                        : mask_el == 'T' ? 1
                        : mask_el >= '0' && mask_el <= '9' ? 2
                        : 3;

    // TODO: use std::enable_if_t instead of std::integral_constant

    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return apply_dispatch(matrix, std::integral_constant<int, version>());
    }

    // mask_el == 'F'
    template <typename Matrix>
    static inline bool apply_dispatch(Matrix const& , std::integral_constant<int, 0>)
    {
        return true;
    }
    // mask_el == 'T'
    template <typename Matrix>
    static inline bool apply_dispatch(Matrix const& matrix, std::integral_constant<int, 1>)
    {
        char const c = matrix.template get<F1, F2>();
        return c == 'F'; // if it's T or between 0 and 9, the result will be the same
    }
    // mask_el >= '0' && mask_el <= '9'
    template <typename Matrix>
    static inline bool apply_dispatch(Matrix const& matrix, std::integral_constant<int, 2>)
    {
        char const c = matrix.template get<F1, F2>();
        return D > c || c > '9';
    }
    // else
    template <typename Matrix>
    static inline bool apply_dispatch(Matrix const&, std::integral_constant<int, 3>)
    {
        return false;
    }
};

template
<
    typename Seq, char D, field F1, field F2,
    std::size_t I = 0,
    std::size_t N = util::sequence_size<Seq>::value
>
struct static_may_update_sequence
{
    typedef typename util::sequence_element<I, Seq>::type StaticMask;

    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return static_may_update_dispatch
                <
                    StaticMask, D, F1, F2
                >::apply(matrix)
            || static_may_update_sequence
                <
                    Seq, D, F1, F2, I + 1
                >::apply(matrix);
    }
};

template <typename Seq, char D, field F1, field F2, std::size_t N>
struct static_may_update_sequence<Seq, D, F1, F2, N, N>
{
    template <typename Matrix>
    static inline bool apply(Matrix const& /*matrix*/)
    {
        return false;
    }
};

template <typename StaticMask, char D, field F1, field F2>
struct static_may_update_dispatch<StaticMask, D, F1, F2, true>
{
    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return static_may_update_sequence
                <
                    StaticMask, D, F1, F2
                >::apply(matrix);
    }
};

template <typename StaticMask, char D, field F1, field F2>
struct static_may_update
{
    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return static_may_update_dispatch
                <
                    StaticMask, D, F1, F2
                >::apply(matrix);
    }
};

// static_check_matrix

template
<
    typename StaticMask,
    bool IsSequence = util::is_sequence<StaticMask>::value
>
struct static_check_dispatch
{
    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return per_one<interior, interior>::apply(matrix)
            && per_one<interior, boundary>::apply(matrix)
            && per_one<interior, exterior>::apply(matrix)
            && per_one<boundary, interior>::apply(matrix)
            && per_one<boundary, boundary>::apply(matrix)
            && per_one<boundary, exterior>::apply(matrix)
            && per_one<exterior, interior>::apply(matrix)
            && per_one<exterior, boundary>::apply(matrix)
            && per_one<exterior, exterior>::apply(matrix);
    }

    template <field F1, field F2>
    struct per_one
    {
        static const char mask_el = StaticMask::template static_get<F1, F2>::value;
        static const int version
                            = mask_el == 'F' ? 0
                            : mask_el == 'T' ? 1
                            : mask_el >= '0' && mask_el <= '9' ? 2
                            : 3;

        // TODO: use std::enable_if_t instead of std::integral_constant

        template <typename Matrix>
        static inline bool apply(Matrix const& matrix)
        {
            const char el = matrix.template get<F1, F2>();
            return apply_dispatch(el, std::integral_constant<int, version>());
        }

        // mask_el == 'F'
        static inline bool apply_dispatch(char el, std::integral_constant<int, 0>)
        {
            return el == 'F';
        }
        // mask_el == 'T'
        static inline bool apply_dispatch(char el, std::integral_constant<int, 1>)
        {
            return el == 'T' || ( el >= '0' && el <= '9' );
        }
        // mask_el >= '0' && mask_el <= '9'
        static inline bool apply_dispatch(char el, std::integral_constant<int, 2>)
        {
            return el == mask_el;
        }
        // else
        static inline bool apply_dispatch(char /*el*/, std::integral_constant<int, 3>)
        {
            return true;
        }
    };
};

template
<
    typename Seq,
    std::size_t I = 0,
    std::size_t N = util::sequence_size<Seq>::value
>
struct static_check_sequence
{
    typedef typename util::sequence_element<I, Seq>::type StaticMask;

    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return static_check_dispatch
                <
                    StaticMask
                >::apply(matrix)
            || static_check_sequence
                <
                    Seq, I + 1
                >::apply(matrix);
    }
};

template <typename Seq, std::size_t N>
struct static_check_sequence<Seq, N, N>
{
    template <typename Matrix>
    static inline bool apply(Matrix const& /*matrix*/)
    {
        return false;
    }
};

template <typename StaticMask>
struct static_check_dispatch<StaticMask, true>
{
    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return static_check_sequence
                <
                    StaticMask
                >::apply(matrix);
    }
};

template <typename StaticMask>
struct static_check_matrix
{
    template <typename Matrix>
    static inline bool apply(Matrix const& matrix)
    {
        return static_check_dispatch
                <
                    StaticMask
                >::apply(matrix);
    }
};

// static_mask_handler

template <typename StaticMask, bool Interrupt>
class static_mask_handler
    : private matrix_handler< matrix<3> >
{
    typedef matrix_handler< relate::matrix<3> > base_type;

public:
    typedef bool result_type;

    bool interrupt;

    inline static_mask_handler()
        : interrupt(false)
    {}

    inline explicit static_mask_handler(StaticMask const& /*dummy*/)
        : interrupt(false)
    {}

    result_type result() const
    {
        return (!Interrupt || !interrupt)
            && static_check_matrix<StaticMask>::apply(base_type::matrix());
    }

    template <field F1, field F2, char D>
    inline bool may_update() const
    {
        return static_may_update<StaticMask, D, F1, F2>::
                    apply(base_type::matrix());
    }

    template <field F1, field F2, char V>
    inline void update()
    {
        static const bool interrupt_c = static_interrupt<StaticMask, V, F1, F2, Interrupt>::value;
        static const bool should_handle = static_should_handle_element<StaticMask, F1, F2>::value;
        static const int version = interrupt_c ? 0
                                 : should_handle ? 1
                                 : 2;

        update_dispatch<F1, F2, V>(integral_constant<int, version>());
    }

    template
    <
        field F1, field F2, char V,
        std::enable_if_t<static_interrupt<StaticMask, V, F1, F2, Interrupt>::value, int> = 0
    >
    inline void set()
    {
        interrupt = true;
    }

    template
    <
        field F1, field F2, char V,
        std::enable_if_t<! static_interrupt<StaticMask, V, F1, F2, Interrupt>::value, int> = 0
    >
    inline void set()
    {
        base_type::template set<F1, F2, V>();
    }

    template <field F1, field F2>
    inline char get() const
    {
        return base_type::template get<F1, F2>();
    }

private:
    // Interrupt && interrupt
    template <field F1, field F2, char V>
    inline void update_dispatch(integral_constant<int, 0>)
    {
        interrupt = true;
    }
    // else should_handle
    template <field F1, field F2, char V>
    inline void update_dispatch(integral_constant<int, 1>)
    {
        base_type::template update<F1, F2, V>();
    }
    // else
    template <field F1, field F2, char V>
    inline void update_dispatch(integral_constant<int, 2>)
    {}
};

// --------------- UTIL FUNCTIONS ----------------

// update

template <field F1, field F2, char D, typename Result>
inline void update(Result & res)
{
    res.template update<F1, F2, D>();
}

template
<
    field F1, field F2, char D, bool Transpose, typename Result,
    std::enable_if_t<! Transpose, int> = 0
>
inline void update(Result & res)
{
    res.template update<F1, F2, D>();
}

template
<
    field F1, field F2, char D, bool Transpose, typename Result,
    std::enable_if_t<Transpose, int> = 0
>
inline void update(Result & res)
{
    res.template update<F2, F1, D>();
}

// may_update

template <field F1, field F2, char D, typename Result>
inline bool may_update(Result const& res)
{
    return res.template may_update<F1, F2, D>();
}

template
<
    field F1, field F2, char D, bool Transpose, typename Result,
    std::enable_if_t<! Transpose, int> = 0
>
inline bool may_update(Result const& res)
{
    return res.template may_update<F1, F2, D>();
}

template
<
    field F1, field F2, char D, bool Transpose, typename Result,
    std::enable_if_t<Transpose, int> = 0
>
inline bool may_update(Result const& res)
{
    return res.template may_update<F2, F1, D>();
}

// result_dimension

template <typename Geometry>
struct result_dimension
{
    static const std::size_t dim = geometry::dimension<Geometry>::value;
    BOOST_STATIC_ASSERT(dim >= 0);
    static const char value = (dim <= 9) ? ('0' + dim) : 'T';
};

}} // namespace detail::relate
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_RESULT_HPP
