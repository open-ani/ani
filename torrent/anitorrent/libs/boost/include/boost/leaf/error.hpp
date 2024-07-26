#ifndef BOOST_LEAF_ERROR_HPP_INCLUDED
#define BOOST_LEAF_ERROR_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/config.hpp>
#include <boost/leaf/detail/optional.hpp>
#include <boost/leaf/detail/function_traits.hpp>
#include <boost/leaf/detail/capture_list.hpp>
#include <boost/leaf/detail/print.hpp>

#if BOOST_LEAF_CFG_DIAGNOSTICS
#   include <ostream>
#endif

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR
#   include <system_error>
#endif

#if BOOST_LEAF_CFG_CAPTURE
#   include <memory>
#endif

#define BOOST_LEAF_TOKEN_PASTE(x, y) x ## y
#define BOOST_LEAF_TOKEN_PASTE2(x, y) BOOST_LEAF_TOKEN_PASTE(x, y)
#define BOOST_LEAF_TMP BOOST_LEAF_TOKEN_PASTE2(boost_leaf_tmp_, __LINE__)

#define BOOST_LEAF_ASSIGN(v,r)\
    auto && BOOST_LEAF_TMP = r;\
    static_assert(::boost::leaf::is_result_type<typename std::decay<decltype(BOOST_LEAF_TMP)>::type>::value,\
        "BOOST_LEAF_ASSIGN/BOOST_LEAF_AUTO requires a result object as the second argument (see is_result_type)");\
    if( !BOOST_LEAF_TMP )\
        return BOOST_LEAF_TMP.error();\
    v = std::forward<decltype(BOOST_LEAF_TMP)>(BOOST_LEAF_TMP).value()

#define BOOST_LEAF_AUTO(v, r)\
    BOOST_LEAF_ASSIGN(auto v, r)

#if BOOST_LEAF_CFG_GNUC_STMTEXPR

#define BOOST_LEAF_CHECK(r)\
    ({\
        auto && BOOST_LEAF_TMP = (r);\
        static_assert(::boost::leaf::is_result_type<typename std::decay<decltype(BOOST_LEAF_TMP)>::type>::value,\
            "BOOST_LEAF_CHECK requires a result object (see is_result_type)");\
        if( !BOOST_LEAF_TMP )\
            return BOOST_LEAF_TMP.error();\
        std::move(BOOST_LEAF_TMP);\
    }).value()

#else

#define BOOST_LEAF_CHECK(r)\
    {\
        auto && BOOST_LEAF_TMP = (r);\
        static_assert(::boost::leaf::is_result_type<typename std::decay<decltype(BOOST_LEAF_TMP)>::type>::value,\
            "BOOST_LEAF_CHECK requires a result object (see is_result_type)");\
        if( !BOOST_LEAF_TMP )\
            return BOOST_LEAF_TMP.error();\
    }

#endif

#define BOOST_LEAF_NEW_ERROR ::boost::leaf::leaf_detail::inject_loc{__FILE__,__LINE__,__FUNCTION__}+::boost::leaf::new_error

namespace boost { namespace leaf {

struct BOOST_LEAF_SYMBOL_VISIBLE e_source_location
{
    char const * file;
    int line;
    char const * function;

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, e_source_location const & x )
    {
        return os << leaf::type<e_source_location>() << ": " << x.file << '(' << x.line << ") in function " << x.function;
    }
};

////////////////////////////////////////

class BOOST_LEAF_SYMBOL_VISIBLE error_id;

namespace leaf_detail
{
    template <class E>
    class BOOST_LEAF_SYMBOL_VISIBLE slot:
        optional<E>
    {
        slot( slot const & ) = delete;
        slot & operator=( slot const & ) = delete;

        using impl = optional<E>;
        slot<E> * prev_;

    public:

        BOOST_LEAF_CONSTEXPR slot() noexcept:
            prev_(nullptr)
        {
        }

        BOOST_LEAF_CONSTEXPR slot( slot && x ) noexcept:
            optional<E>(std::move(x)),
            prev_(nullptr)
        {
            BOOST_LEAF_ASSERT(x.prev_==nullptr);
        }

        void activate() noexcept
        {
            prev_ = tls::read_ptr<slot<E>>();
            tls::write_ptr<slot<E>>(this);
        }

        void deactivate() const noexcept
        {
            tls::write_ptr<slot<E>>(prev_);
        }

        void unload( int err_id ) noexcept(!BOOST_LEAF_CFG_CAPTURE);

        template <class CharT, class Traits>
        void print( std::basic_ostream<CharT, Traits> & os, int err_id_to_print ) const
        {
            if( !diagnostic<E>::is_invisible )
                if( int k = this->key() )
                {
                    if( err_id_to_print )
                    {
                        if( err_id_to_print!=k )
                            return;
                    }
                    else
                        os << '[' << k << "] ";
                    diagnostic<E>::print(os, value(k));
                    os << '\n';
                }
        }

        using impl::load;
        using impl::has_value;
        using impl::value;
    };
}

////////////////////////////////////////

#if BOOST_LEAF_CFG_CAPTURE

namespace leaf_detail
{
    class BOOST_LEAF_SYMBOL_VISIBLE dynamic_allocator:
        capture_list
    {
        dynamic_allocator( dynamic_allocator const & ) = delete;
        dynamic_allocator & operator=( dynamic_allocator const & ) = delete;

        class capturing_node:
            public capture_list::node
        {
        protected:
            BOOST_LEAF_CONSTEXPR explicit capturing_node( capture_list::node * * & last ) noexcept:
                node(last)
            {
                BOOST_LEAF_ASSERT(last == &next_);
                BOOST_LEAF_ASSERT(next_ == nullptr);
            }
        public:
            virtual void deactivate() const noexcept = 0;
        };

        template <class E>
        class capturing_slot_node:
            public capturing_node,
            public slot<E>
        {
            using impl = slot<E>;

            capturing_slot_node( capturing_slot_node const & ) = delete;
            capturing_slot_node & operator=( capturing_slot_node const & ) = delete;

            void deactivate() const noexcept final override
            {
                impl::deactivate();
            }

            void unload( int err_id ) final override
            {
                impl::unload(err_id);
            }

#if BOOST_LEAF_CFG_DIAGNOSTICS
            void print( std::ostream & os, int err_id_to_print ) const final override
            {
                impl::print(os, err_id_to_print);
            }
#endif

        public:

            template <class T>
            BOOST_LEAF_CONSTEXPR capturing_slot_node( capture_list::node * * & last, int err_id, T && e ):
                capturing_node(last)
            {
                BOOST_LEAF_ASSERT(last == &next_);
                BOOST_LEAF_ASSERT(next_ == nullptr);
                impl::load(err_id, std::forward<T>(e));
            }
        };

#ifndef BOOST_LEAF_NO_EXCEPTIONS
        class capturing_exception_node:
            public capturing_node
        {
            capturing_exception_node( capturing_exception_node const & ) = delete;
            capturing_exception_node & operator=( capturing_exception_node const & ) = delete;

            void deactivate() const noexcept final override
            {
                BOOST_LEAF_ASSERT(0);
            }

            void unload( int ) final override
            {
                std::rethrow_exception(ex_);
            }

#if BOOST_LEAF_CFG_DIAGNOSTICS
            void print( std::ostream &, int err_id_to_print ) const final override
            {
            }
#endif

            std::exception_ptr const ex_;

        public:

            explicit capturing_exception_node( capture_list::node * * & last, std::exception_ptr && ex ) noexcept:
                capturing_node(last),
                ex_(std::move(ex))
            {
                BOOST_LEAF_ASSERT(last == &next_);
                BOOST_LEAF_ASSERT(ex_);
            }
        };
#endif

        int err_id_;
        node * * last_;

    public:

        dynamic_allocator() noexcept:
            capture_list(nullptr),
            err_id_(0),
            last_(&first_)
        {
            BOOST_LEAF_ASSERT(first_ == nullptr);
        }

        dynamic_allocator( dynamic_allocator && other ) noexcept:
            capture_list(std::move(other)),
            err_id_(other.err_id_),
            last_(other.last_ == &other.first_? &first_ : other.last_)
        {
            BOOST_LEAF_ASSERT(last_ != nullptr);
            BOOST_LEAF_ASSERT(*last_ == nullptr);
            BOOST_LEAF_ASSERT(other.first_ == nullptr);
            other.last_ = &other.first_;
        }

        void append( dynamic_allocator && other ) noexcept
        {
            if( node * other_first = other.first_ )
            {
                *last_ = other_first;
                last_ = other.last_;
                other.first_ = nullptr;
                other.last_ = &other.first_;
            }
        }

        template <class E>
        typename std::decay<E>::type & dynamic_load(int err_id, E && e)
        {
            using T = typename std::decay<E>::type;
            BOOST_LEAF_ASSERT(last_ != nullptr);
            BOOST_LEAF_ASSERT(*last_ == nullptr);
            BOOST_LEAF_ASSERT(tls::read_ptr<slot<T>>() == nullptr);
            capturing_slot_node<T> * csn = new capturing_slot_node<T>(last_, err_id, std::forward<E>(e));
            csn->activate();
            err_id_ = err_id;
            return csn->value(err_id);
        }

        void deactivate() const noexcept
        {
            for_each(
                []( capture_list::node const & n )
                {
                    static_cast<capturing_node const &>(n).deactivate();
                } );
        }

        template <class LeafResult>
        LeafResult extract_capture_list() noexcept
        {
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            if( std::exception_ptr ex = std::current_exception() )
                (void) new capturing_exception_node(last_, std::move(ex));
#endif
            leaf_detail::capture_list::node * const f = first_;
            first_ = nullptr;
            last_ = &first_;
            return { err_id_, capture_list(f) };
        }

        using capture_list::print;
    };

    template <>
    struct diagnostic<dynamic_allocator, false, false, false>
    {
        static constexpr bool is_invisible = true;

        template <class CharT, class Traits>
        BOOST_LEAF_CONSTEXPR static void print( std::basic_ostream<CharT, Traits> &, dynamic_allocator const & )
        {
        }
    };

    template <>
    inline void slot<dynamic_allocator>::deactivate() const noexcept
    {
        if( int const err_id = this->key() )
            if( dynamic_allocator const * c = this->has_value(err_id) )
                c->deactivate();
        tls::write_ptr<slot<dynamic_allocator>>(prev_);
    }

    template <>
    inline void slot<dynamic_allocator>::unload( int err_id ) noexcept(false)
    {
        BOOST_LEAF_ASSERT(err_id);
        if( dynamic_allocator * da1 = this->has_value(err_id) )
            if( impl * p = tls::read_ptr<slot<dynamic_allocator>>() )
                if( dynamic_allocator * da2 = p->has_value(err_id) )
                    da2->append(std::move(*da1));
                else
                    *p = std::move(*this);
    }

    template <class E>
    inline void dynamic_load_( int err_id, E && e )
    {
        if( slot<dynamic_allocator> * sl = tls::read_ptr<slot<dynamic_allocator>>() )
        {
            if( dynamic_allocator * c = sl->has_value(err_id) )
                c->dynamic_load(err_id, std::forward<E>(e));
            else
                sl->load(err_id).dynamic_load(err_id, std::forward<E>(e));
        }
    }

    template <class E, class F>
    inline void dynamic_accumulate_( int err_id, F && f )
    {
        if( slot<dynamic_allocator> * sl = tls::read_ptr<slot<dynamic_allocator>>() )
        {
            if( dynamic_allocator * c = sl->has_value(err_id) )
                (void) std::forward<F>(f)(c->dynamic_load(err_id, E{}));
            else
                (void) std::forward<F>(f)(sl->load(err_id).dynamic_load(err_id, E{}));
        }
    }

    template <bool OnError, class E>
    inline void dynamic_load( int err_id, E && e  ) noexcept(OnError)
    {
        if( OnError )
        {
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            try
            {
#endif
                dynamic_load_(err_id, std::forward<E>(e));
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            }
            catch(...)
            {
            }
#endif
        }
        else
            dynamic_load_(err_id, std::forward<E>(e));
    }

    template <bool OnError, class E, class F>
    inline void dynamic_load_accumulate( int err_id, F && f  ) noexcept(OnError)
    {
        if( OnError )
        {
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            try
            {
#endif
                dynamic_accumulate_<E>(err_id, std::forward<F>(f));
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            }
            catch(...)
            {
            }
#endif
        }
        else
            dynamic_accumulate_<E>(err_id, std::forward<F>(f));
    }
}

#endif

////////////////////////////////////////

namespace leaf_detail
{
    template <class E>
    inline void slot<E>::unload( int err_id ) noexcept(!BOOST_LEAF_CFG_CAPTURE)
    {
        BOOST_LEAF_ASSERT(err_id);
        if( this->key()!=err_id )
            return;
        if( impl * p = tls::read_ptr<slot<E>>() )
            *p = std::move(*this);
#if BOOST_LEAF_CFG_CAPTURE
        else
            dynamic_load<false>(err_id, std::move(*this).value(err_id));
#endif
    }

    template <bool OnError, class E>
    BOOST_LEAF_CONSTEXPR inline int load_slot( int err_id, E && e ) noexcept(OnError)
    {
        using T = typename std::decay<E>::type;
        static_assert(!std::is_pointer<E>::value, "Error objects of pointer types are not allowed");
        static_assert(!std::is_same<T, error_id>::value, "Error objects of type error_id are not allowed");
        BOOST_LEAF_ASSERT((err_id&3)==1);
        if( slot<T> * p = tls::read_ptr<slot<T>>() )
        {
            if( !OnError || !p->has_value(err_id) )
                (void) p->load(err_id, std::forward<E>(e));
        }
#if BOOST_LEAF_CFG_CAPTURE
        else
            dynamic_load<OnError>(err_id, std::forward<E>(e));
#endif        
        return 0;
    }

    template <bool OnError, class F>
    BOOST_LEAF_CONSTEXPR inline int load_slot_deferred( int err_id, F && f ) noexcept(OnError)
    {
        using E = typename function_traits<F>::return_type;
        using T = typename std::decay<E>::type;
        static_assert(!std::is_pointer<E>::value, "Error objects of pointer types are not allowed");
        static_assert(!std::is_same<T, error_id>::value, "Error objects of type error_id are not allowed");
        BOOST_LEAF_ASSERT((err_id&3)==1);
        if( slot<T> * p = tls::read_ptr<slot<T>>() )
        {
            if( !OnError || !p->has_value(err_id) )
                (void) p->load(err_id, std::forward<F>(f)());
        }
#if BOOST_LEAF_CFG_CAPTURE
        else
            dynamic_load<OnError>(err_id, std::forward<F>(f)());
#endif        
        return 0;
    }

    template <bool OnError, class F>
    BOOST_LEAF_CONSTEXPR inline int load_slot_accumulate( int err_id, F && f ) noexcept(OnError)
    {
        static_assert(function_traits<F>::arity==1, "Lambdas passed to accumulate must take a single e-type argument by reference");
        using E = typename std::decay<fn_arg_type<F,0>>::type;
        static_assert(!std::is_pointer<E>::value, "Error objects of pointer types are not allowed");
        BOOST_LEAF_ASSERT((err_id&3)==1);
        if( auto sl = tls::read_ptr<slot<E>>() )
        {
            if( auto v = sl->has_value(err_id) )
                (void) std::forward<F>(f)(*v);
            else
                (void) std::forward<F>(f)(sl->load(err_id,E()));
        }
#if BOOST_LEAF_CFG_CAPTURE
        else
            dynamic_load_accumulate<OnError, E>(err_id, std::forward<F>(f));
#endif
        return 0;
    }
}

////////////////////////////////////////

namespace leaf_detail
{
    template <class T, int Arity = function_traits<T>::arity>
    struct load_item
    {
        static_assert(Arity==0 || Arity==1, "If a functions is passed to new_error or load, it must take zero or one argument");
    };

    template <class E>
    struct load_item<E, -1>
    {
        BOOST_LEAF_CONSTEXPR static int load_( int err_id, E && e ) noexcept
        {
            return load_slot<false>(err_id, std::forward<E>(e));
        }
    };

    template <class F>
    struct load_item<F, 0>
    {
        BOOST_LEAF_CONSTEXPR static int load_( int err_id, F && f ) noexcept
        {
            return load_slot_deferred<false>(err_id, std::forward<F>(f));
        }
    };

    template <class F>
    struct load_item<F, 1>
    {
        BOOST_LEAF_CONSTEXPR static int load_( int err_id, F && f ) noexcept
        {
            return load_slot_accumulate<false>(err_id, std::forward<F>(f));
        }
    };
}

////////////////////////////////////////

namespace leaf_detail
{
    struct BOOST_LEAF_SYMBOL_VISIBLE tls_tag_id_factory_current_id;

    template <class=void>
    struct BOOST_LEAF_SYMBOL_VISIBLE id_factory
    {
        static atomic_unsigned_int counter;

        BOOST_LEAF_CONSTEXPR static unsigned generate_next_id() noexcept
        {
            auto id = (counter+=4);
            BOOST_LEAF_ASSERT((id&3)==1);
            return id;
        }
    };

    template <class T>
    atomic_unsigned_int id_factory<T>::counter(unsigned(-3));

    inline int current_id() noexcept
    {
        unsigned id = tls::read_uint<tls_tag_id_factory_current_id>();
        BOOST_LEAF_ASSERT(id==0 || (id&3)==1);
        return int(id);
    }

    inline int new_id() noexcept
    {
        unsigned id = id_factory<>::generate_next_id();
        tls::write_uint<tls_tag_id_factory_current_id>(id);
        return int(id);
    }

    struct inject_loc
    {
        char const * const file;
        int const line;
        char const * const fn;

        template <class T>
        friend T operator+( inject_loc loc, T && x ) noexcept
        {
            x.load_source_location_(loc.file, loc.line, loc.fn);
            return std::move(x);
        }
    };
}

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR

namespace leaf_detail
{
    class leaf_category final: public std::error_category
    {
        bool equivalent( int,  std::error_condition const & ) const noexcept final override { return false; }
        bool equivalent( std::error_code const &, int ) const noexcept final override { return false; }
        char const * name() const noexcept final override { return "LEAF error"; }
        std::string message( int ) const final override { return name(); }
    public:
        ~leaf_category() noexcept final override { }
    };

    template <class=void>
    struct get_error_category
    {
        static leaf_category cat;
    };

    template <class T>
    leaf_category get_error_category<T>::cat;

    inline int import_error_code( std::error_code const & ec ) noexcept
    {
        if( int err_id = ec.value() )
        {
            std::error_category const & cat = get_error_category<>::cat;
            if( &ec.category() == &cat )
            {
                BOOST_LEAF_ASSERT((err_id&3)==1);
                return (err_id&~3)|1;
            }
            else
            {
                err_id = new_id();
                (void) load_slot<false>(err_id, ec);
                return (err_id&~3)|1;
            }
        }
        else
            return 0;
    }
}

inline bool is_error_id( std::error_code const & ec ) noexcept
{
    bool res = (&ec.category() == &leaf_detail::get_error_category<>::cat);
    BOOST_LEAF_ASSERT(!res || !ec.value() || ((ec.value()&3)==1));
    return res;
}

#endif

////////////////////////////////////////

namespace leaf_detail
{
    BOOST_LEAF_CONSTEXPR error_id make_error_id(int) noexcept;
}

class BOOST_LEAF_SYMBOL_VISIBLE error_id
{
    friend error_id BOOST_LEAF_CONSTEXPR leaf_detail::make_error_id(int) noexcept;

    int value_;

    BOOST_LEAF_CONSTEXPR explicit error_id( int value ) noexcept:
        value_(value)
    {
        BOOST_LEAF_ASSERT(value_==0 || ((value_&3)==1));
    }

public:

    BOOST_LEAF_CONSTEXPR error_id() noexcept:
        value_(0)
    {
    }

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR
    error_id( std::error_code const & ec ) noexcept:
        value_(leaf_detail::import_error_code(ec))
    {
        BOOST_LEAF_ASSERT(!value_ || ((value_&3)==1));
    }

    template <class Enum>
    error_id( Enum e, typename std::enable_if<std::is_error_code_enum<Enum>::value, Enum>::type * = 0 ) noexcept:
        value_(leaf_detail::import_error_code(e))
    {
    }

    operator std::error_code() const noexcept
    {
        return std::error_code(value_, leaf_detail::get_error_category<>::cat);
    }
#endif

    BOOST_LEAF_CONSTEXPR error_id load() const noexcept
    {
        return *this;
    }

    template <class Item>
    BOOST_LEAF_CONSTEXPR error_id load(Item && item) const noexcept
    {
        if (int err_id = value())
        {
            int const unused[] = { 42, leaf_detail::load_item<Item>::load_(err_id, std::forward<Item>(item)) };
            (void)unused;
        }
        return *this;
    }

    template <class... Item>
    BOOST_LEAF_CONSTEXPR error_id load( Item && ... item ) const noexcept
    {
        if( int err_id = value() )
        {
            int const unused[] = { 42, leaf_detail::load_item<Item>::load_(err_id, std::forward<Item>(item))... };
            (void) unused;
        }
        return *this;
    }

    BOOST_LEAF_CONSTEXPR int value() const noexcept
    {
        BOOST_LEAF_ASSERT(value_==0 || ((value_&3)==1));
        return value_;
    }

    BOOST_LEAF_CONSTEXPR explicit operator bool() const noexcept
    {
        return value_ != 0;
    }

    BOOST_LEAF_CONSTEXPR friend bool operator==( error_id a, error_id b ) noexcept
    {
        return a.value_ == b.value_;
    }

    BOOST_LEAF_CONSTEXPR friend bool operator!=( error_id a, error_id b ) noexcept
    {
        return !(a == b);
    }

    BOOST_LEAF_CONSTEXPR friend bool operator<( error_id a, error_id b ) noexcept
    {
        return a.value_ < b.value_;
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, error_id x )
    {
        return os << x.value_;
    }

    BOOST_LEAF_CONSTEXPR void load_source_location_( char const * file, int line, char const * function ) const noexcept
    {
        BOOST_LEAF_ASSERT(file&&*file);
        BOOST_LEAF_ASSERT(line>0);
        BOOST_LEAF_ASSERT(function&&*function);
        BOOST_LEAF_ASSERT(value_);
        (void) load(e_source_location {file,line,function});
    }
};

namespace leaf_detail
{
    BOOST_LEAF_CONSTEXPR inline error_id make_error_id( int err_id ) noexcept
    {
        BOOST_LEAF_ASSERT(err_id==0 || (err_id&3)==1);
        return error_id((err_id&~3)|1);
    }
}

inline error_id new_error() noexcept
{
    return leaf_detail::make_error_id(leaf_detail::new_id());
}

template <class... Item>
inline error_id new_error( Item && ... item ) noexcept
{
    return leaf_detail::make_error_id(leaf_detail::new_id()).load(std::forward<Item>(item)...);
}

inline error_id current_error() noexcept
{
    return leaf_detail::make_error_id(leaf_detail::current_id());
}

////////////////////////////////////////////

class polymorphic_context
{
};

#if BOOST_LEAF_CFG_CAPTURE
using context_ptr = std::shared_ptr<polymorphic_context>;
#endif

////////////////////////////////////////////

template <class Ctx>
class context_activator
{
    context_activator( context_activator const & ) = delete;
    context_activator & operator=( context_activator const & ) = delete;

    Ctx * ctx_;

public:

    explicit BOOST_LEAF_CONSTEXPR BOOST_LEAF_ALWAYS_INLINE context_activator(Ctx & ctx) noexcept:
        ctx_(ctx.is_active() ? nullptr : &ctx)
    {
        if( ctx_ )
            ctx_->activate();
    }

    BOOST_LEAF_CONSTEXPR BOOST_LEAF_ALWAYS_INLINE context_activator( context_activator && x ) noexcept:
        ctx_(x.ctx_)
    {
        x.ctx_ = nullptr;
    }

    BOOST_LEAF_ALWAYS_INLINE ~context_activator() noexcept
    {
        if( ctx_ && ctx_->is_active() )
            ctx_->deactivate();
    }
};

////////////////////////////////////////////

template <class R>
struct is_result_type: std::false_type
{
};

template <class R>
struct is_result_type<R const>: is_result_type<R>
{
};

} }

#endif
