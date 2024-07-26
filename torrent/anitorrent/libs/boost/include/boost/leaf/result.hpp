#ifndef BOOST_LEAF_RESULT_HPP_INCLUDED
#define BOOST_LEAF_RESULT_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/config.hpp>
#include <boost/leaf/detail/print.hpp>
#include <boost/leaf/detail/capture_list.hpp>
#include <boost/leaf/exception.hpp>

#include <climits>
#include <functional>

namespace boost { namespace leaf {

namespace leaf_detail { class dynamic_allocator; }

////////////////////////////////////////

class bad_result:
    public std::exception,
    public error_id
{
    char const * what() const noexcept final override
    {
        return "boost::leaf::bad_result";
    }

public:

    explicit bad_result( error_id id ) noexcept:
        error_id(id)
    {
        BOOST_LEAF_ASSERT(value());
    }
};

////////////////////////////////////////

namespace leaf_detail
{
    template <class T, bool Printable = is_printable<T>::value>
    struct result_value_printer;

    template <class T>
    struct result_value_printer<T, true>
    {
        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & s, T const & x )
        {
            (void) (s << x);
        }
    };

    template <class T>
    struct result_value_printer<T, false>
    {
        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & s, T const & )
        {
            (void) (s << "{not printable}");
        }
    };

    template <class CharT, class Traits, class T>
    void print_result_value( std::basic_ostream<CharT, Traits> & s, T const & x )
    {
        result_value_printer<T>::print(s, x);
    }
}

////////////////////////////////////////

namespace leaf_detail
{
    template <class T>
    struct stored
    {
        using type = T;
        using value_no_ref = T;
        using value_no_ref_const = T const;
        using value_cref = T const &;
        using value_ref = T &;
        using value_rv_cref = T const &&;
        using value_rv_ref = T &&;

        static value_no_ref_const * cptr( type const & v ) noexcept
        {
            return &v;
        }

        static value_no_ref * ptr( type & v ) noexcept
        {
            return &v;
        }
    };

    template <class T>
    struct stored<T &>
    {
        using type = std::reference_wrapper<T>;
        using value_no_ref = T;
        using value_no_ref_const = T;
        using value_ref = T &;
        using value_cref = T &;
        using value_rv_ref = T &;
        using value_rv_cref = T &;

        static value_no_ref_const * cptr( type const & v ) noexcept
        {
            return &v.get();
        }

        static value_no_ref * ptr( type const & v ) noexcept
        {
            return &v.get();
        }
    };

    class result_discriminant
    {
        int state_;

    public:

        enum kind_t
        {
            err_id_zero = 0,
            err_id = 1,
            err_id_capture_list = 2,
            val = 3
        };

        explicit result_discriminant( error_id id ) noexcept:
            state_(id.value())
        {
            BOOST_LEAF_ASSERT(state_ == 0 || (state_&3) == 1);
            BOOST_LEAF_ASSERT(kind()==err_id_zero || kind()==err_id);
        }

#if BOOST_LEAF_CFG_CAPTURE
        explicit result_discriminant( int err_id, leaf_detail::capture_list const & ) noexcept:
            state_((err_id&~3) | 2)
        {
            BOOST_LEAF_ASSERT((err_id&3) == 1);
            BOOST_LEAF_ASSERT(kind() == err_id_capture_list);
        }
#endif

        struct kind_val { };
        explicit result_discriminant( kind_val ) noexcept:
            state_(val)
        {
            BOOST_LEAF_ASSERT((state_&3) == 3);
            BOOST_LEAF_ASSERT(kind()==val);
        }

        kind_t kind() const noexcept
        {
            return kind_t(state_&3);
        }

        error_id get_error_id() const noexcept
        {
            BOOST_LEAF_ASSERT(kind()==err_id_zero || kind()==err_id || kind()==err_id_capture_list);
            return make_error_id(int((state_&~3)|1));
        }
    };
}

////////////////////////////////////////

template <class T>
class BOOST_LEAF_SYMBOL_VISIBLE result
{
    template <class U>
    friend class result;

    friend class leaf_detail::dynamic_allocator;

#if BOOST_LEAF_CFG_CAPTURE
    using capture_list = leaf_detail::capture_list;
#endif

    using result_discriminant = leaf_detail::result_discriminant;
    using stored_type = typename leaf_detail::stored<T>::type;
    using value_no_ref = typename leaf_detail::stored<T>::value_no_ref;
    using value_no_ref_const = typename leaf_detail::stored<T>::value_no_ref_const;
    using value_ref = typename leaf_detail::stored<T>::value_ref;
    using value_cref = typename leaf_detail::stored<T>::value_cref;
    using value_rv_ref = typename leaf_detail::stored<T>::value_rv_ref;
    using value_rv_cref = typename leaf_detail::stored<T>::value_rv_cref;

    union
    {
        stored_type stored_;
#if BOOST_LEAF_CFG_CAPTURE
        mutable capture_list cap_;
#endif
    };

    result_discriminant what_;

    struct error_result
    {
        error_result( error_result && ) = default;
        error_result( error_result const & ) = delete;
        error_result & operator=( error_result const & ) = delete;

        result & r_;

        error_result( result & r ) noexcept:
            r_(r)
        {
        }

        template <class U>
        operator result<U>() noexcept
        {
            result_discriminant const what = r_.what_;
            switch( what.kind() )
            {
                case result_discriminant::val:
                    return result<U>(error_id());
                case result_discriminant::err_id_capture_list:
#if BOOST_LEAF_CFG_CAPTURE
                    return result<U>(what.get_error_id().value(), std::move(r_.cap_));
#else
                    BOOST_LEAF_ASSERT(0); // Possible ODR violation.
#endif
                case result_discriminant::err_id_zero:
                case result_discriminant::err_id:
                    return result<U>(what.get_error_id());
            }
        }

        operator error_id() noexcept
        {
            result_discriminant const what = r_.what_;
            return what.kind() == result_discriminant::val? error_id() : what.get_error_id();
        }
    };

    void destroy() const noexcept
    {
        switch(this->what_.kind())
        {
        case result_discriminant::err_id_zero:
        case result_discriminant::err_id:
            break;
        case result_discriminant::err_id_capture_list:
#if BOOST_LEAF_CFG_CAPTURE
            cap_.~capture_list();
#else
            BOOST_LEAF_ASSERT(0); // Possible ODR violation.
#endif
            break;
        case result_discriminant::val:
            stored_.~stored_type();
        }
    }

    template <class U>
    result_discriminant move_from( result<U> && x ) noexcept
    {
        auto x_what = x.what_;
        switch(x_what.kind())
        {
        case result_discriminant::err_id_zero:
        case result_discriminant::err_id:
            break;
        case result_discriminant::err_id_capture_list:
#if BOOST_LEAF_CFG_CAPTURE
            (void) new(&cap_) capture_list(std::move(x.cap_));
#else
            BOOST_LEAF_ASSERT(0); // Possible ODR violation.
#endif
            break;
        case result_discriminant::val:
            (void) new(&stored_) stored_type(std::move(x.stored_));
        }
        return x_what;
    }

    error_id get_error_id() const noexcept
    {
        BOOST_LEAF_ASSERT(what_.kind() != result_discriminant::val);
        return what_.get_error_id();
    }

    stored_type const * get() const noexcept
    {
        return has_value() ? &stored_ : nullptr;
    }

    stored_type * get() noexcept
    {
        return has_value() ? &stored_ : nullptr;
    }

protected:

#if BOOST_LEAF_CFG_CAPTURE
    result( int err_id, leaf_detail::capture_list && cap ) noexcept:
        cap_(std::move(cap)),
        what_(err_id, cap)
    {
    }
#endif

    void enforce_value_state() const
    {
        switch( what_.kind() )
        {
        case result_discriminant::err_id_capture_list:
#if BOOST_LEAF_CFG_CAPTURE
            cap_.unload(what_.get_error_id().value());
#else
            BOOST_LEAF_ASSERT(0); // Possible ODR violation.
#endif
        case result_discriminant::err_id_zero:
        case result_discriminant::err_id:
            ::boost::leaf::leaf_detail::throw_exception_impl(bad_result(get_error_id()));
        case result_discriminant::val:
            break;
        }
    }

    template <class U>
    void move_assign( result<U> && x ) noexcept
    {
        destroy();
        what_ = move_from(std::move(x));
    }

public:

    using value_type = T;

    // NOTE: Copy constructor implicitly deleted.

    result( result && x ) noexcept:
        what_(move_from(std::move(x)))
    {
    }

    template <class U, class = typename std::enable_if<std::is_convertible<U, T>::value>::type>
    result( result<U> && x ) noexcept:
        what_(move_from(std::move(x)))
    {
    }

    result():
        stored_(stored_type()),
        what_(result_discriminant::kind_val{})
    {
    }

    result( value_no_ref && v ) noexcept:
        stored_(std::forward<value_no_ref>(v)),
        what_(result_discriminant::kind_val{})
    {
    }

    result( value_no_ref const & v ):
        stored_(v),
        what_(result_discriminant::kind_val{})
    {
    }

    template<class... A, class = typename std::enable_if<std::is_constructible<T, A...>::value && sizeof...(A) >= 2>::type>
    result( A && ... a ) noexcept:
        stored_(std::forward<A>(a)...),
        what_(result_discriminant::kind_val{})
    {
    }

    result( error_id err ) noexcept:
        what_(err)
    {
    }

#if defined(BOOST_STRICT_CONFIG) || !defined(__clang__)

    // This should be the default implementation, but std::is_convertible
    // breaks under COMPILER=/usr/bin/clang++ CXXSTD=11 clang 3.3.
    // On the other hand, the workaround exposes a rather severe bug in
    //__GNUC__ under 11: https://github.com/boostorg/leaf/issues/25.

    // SFINAE: T can be initialized with an A, e.g. result<std::string>("literal").
    template<class A, class = typename std::enable_if<std::is_constructible<T, A>::value && std::is_convertible<A, T>::value>::type>
    result( A && a ) noexcept:
        stored_(std::forward<A>(a)),
        what_(result_discriminant::kind_val{})
    {
    }

#else

private:
    static int init_T_with_A( T && );
public:

    // SFINAE: T can be initialized with an A, e.g. result<std::string>("literal").
    template <class A>
    result( A && a, decltype(init_T_with_A(std::forward<A>(a))) * = nullptr ):
        stored_(std::forward<A>(a)),
        what_(result_discriminant::kind_val{})
    {
    }

#endif

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR
    result( std::error_code const & ec ) noexcept:
        what_(error_id(ec))
    {
    }

    template <class Enum, class = typename std::enable_if<std::is_error_code_enum<Enum>::value, int>::type>
    result( Enum e ) noexcept:
        what_(error_id(e))
    {
    }
#endif

    ~result() noexcept
    {
        destroy();
    }

    // NOTE: Assignment operator implicitly deleted.

    result & operator=( result && x ) noexcept
    {
        move_assign(std::move(x));
        return *this;
    }

    template <class U, class = typename std::enable_if<std::is_convertible<U, T>::value>::type>
    result & operator=( result<U> && x ) noexcept
    {
        move_assign(std::move(x));
        return *this;
    }

    bool has_value() const noexcept
    {
        return what_.kind() == result_discriminant::val;
    }

    bool has_error() const noexcept
    {
        return !has_value();
    }

    explicit operator bool() const noexcept
    {
        return has_value();
    }

#ifdef BOOST_LEAF_NO_CXX11_REF_QUALIFIERS

    value_cref value() const
    {
        enforce_value_state();
        return stored_;
    }

    value_ref value()
    {
        enforce_value_state();
        return stored_;
    }

#else

    value_cref value() const &
    {
        enforce_value_state();
        return stored_;
    }

    value_ref value() &
    {
        enforce_value_state();
        return stored_;
    }

    value_rv_cref value() const &&
    {
        enforce_value_state();
        return std::move(stored_);
    }

    value_rv_ref value() &&
    {
        enforce_value_state();
        return std::move(stored_);
    }

#endif

    value_no_ref_const * operator->() const noexcept
    {
        return has_value() ? leaf_detail::stored<T>::cptr(stored_) : nullptr;
    }

    value_no_ref * operator->() noexcept
    {
        return has_value() ? leaf_detail::stored<T>::ptr(stored_) : nullptr;
    }

#ifdef BOOST_LEAF_NO_CXX11_REF_QUALIFIERS

    value_cref operator*() const noexcept
    {
        auto p = get();
        BOOST_LEAF_ASSERT(p != nullptr);
        return *p;
    }

    value_ref operator*() noexcept
    {
        auto p = get();
        BOOST_LEAF_ASSERT(p != nullptr);
        return *p;
    }

#else

    value_cref operator*() const & noexcept
    {
        auto p = get();
        BOOST_LEAF_ASSERT(p != nullptr);
        return *p;
    }

    value_ref operator*() & noexcept
    {
        auto p = get();
        BOOST_LEAF_ASSERT(p != nullptr);
        return *p;
    }

    value_rv_cref operator*() const && noexcept
    {
        auto p = get();
        BOOST_LEAF_ASSERT(p != nullptr);
        return std::move(*p);
    }

    value_rv_ref operator*() && noexcept
    {
        auto p = get();
        BOOST_LEAF_ASSERT(p != nullptr);
        return std::move(*p);
    }

#endif

    error_result error() noexcept
    {
        return error_result{*this};
    }

    template <class... Item>
    error_id load( Item && ... item ) noexcept
    {
        return error_id(error()).load(std::forward<Item>(item)...);
    }

    void unload()
    {
#if BOOST_LEAF_CFG_CAPTURE
        if( what_.kind() == result_discriminant::err_id_capture_list )
            cap_.unload(what_.get_error_id().value());
#endif
    }

    template <class CharT, class Traits>
    void print( std::basic_ostream<CharT, Traits> & os ) const
    {
        result_discriminant const what = what_;
        if( what.kind() == result_discriminant::val )
            leaf_detail::print_result_value(os, value());
        else
        {
            error_id const err_id = what.get_error_id();
            os << "Error ID " << err_id;
            if( what.kind() == result_discriminant::err_id_capture_list )
            {
#if BOOST_LEAF_CFG_CAPTURE
#   if BOOST_LEAF_CFG_DIAGNOSTICS
                cap_.print(os, ". Captured error objects:\n", err_id.value());
#   endif
#else
                BOOST_LEAF_ASSERT(0); // Possible ODR violation.
#endif
            }
        }
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, result const & r )
    {
        r.print(os);
        return os;
    }
};

////////////////////////////////////////

namespace leaf_detail
{
    struct void_ { };
}

template <>
class BOOST_LEAF_SYMBOL_VISIBLE result<void>:
    result<leaf_detail::void_>
{
    template <class U>
    friend class result;

    friend class leaf_detail::dynamic_allocator;

    using result_discriminant = leaf_detail::result_discriminant;
    using void_ = leaf_detail::void_;
    using base = result<void_>;

#if BOOST_LEAF_CFG_CAPTURE
    result( int err_id, leaf_detail::capture_list && cap ) noexcept:
        base(err_id, std::move(cap))
    {
    }
#endif

public:

    using value_type = void;

    // NOTE: Copy constructor implicitly deleted.
    result( result && x ) noexcept:
        base(std::move(x))
    {
    }

    result() noexcept
    {
    }

    result( error_id err ) noexcept:
        base(err)
    {
    }

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR
    result( std::error_code const & ec ) noexcept:
        base(ec)
    {
    }

    template <class Enum, class = typename std::enable_if<std::is_error_code_enum<Enum>::value, int>::type>
    result( Enum e ) noexcept:
        base(e)
    {
    }
#endif

    ~result() noexcept
    {
    }

    // NOTE: Assignment operator implicitly deleted.
    result & operator=( result && x ) noexcept
    {
        base::move_assign(std::move(x));
        return *this;
    }

    void value() const
    {
        base::enforce_value_state();
    }

    void const * operator->() const noexcept
    {
        return base::operator->();
    }

    void * operator->() noexcept
    {
        return base::operator->();
    }

    void operator*() const noexcept
    {
        BOOST_LEAF_ASSERT(has_value());
    }

    template <class CharT, class Traits>
    void print( std::basic_ostream<CharT, Traits> & os ) const
    {
        if( what_.kind() == result_discriminant::val )
            os << "No error";
        else
            os << *static_cast<base const *>(this);
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, result const & r )
    {
        r.print(os);
        return os;
    }

    using base::operator=;
    using base::operator bool;
    using base::get_error_id;
    using base::error;
    using base::load;
    using base::unload;
};

////////////////////////////////////////

template <class R>
struct is_result_type;

template <class T>
struct is_result_type<result<T>>: std::true_type
{
};

} }

#endif
