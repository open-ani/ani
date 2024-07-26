#ifndef BOOST_LEAF_DETAIL_CAPTURE_HPP_INCLUDED
#define BOOST_LEAF_DETAIL_CAPTURE_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/config.hpp>

#if BOOST_LEAF_CFG_CAPTURE

#include <iosfwd>

namespace boost { namespace leaf {

namespace leaf_detail
{
    struct BOOST_LEAF_SYMBOL_VISIBLE tls_tag_id_factory_current_id;

    class capture_list
    {
        capture_list( capture_list const & ) = delete;
        capture_list & operator=( capture_list const & ) = delete;

    protected:

        class node
        {
            friend class capture_list;

            virtual void unload( int err_id ) = 0;
#if BOOST_LEAF_CFG_DIAGNOSTICS
            virtual void print( std::ostream &, int err_id_to_print ) const = 0;
#endif

        protected:

            virtual ~node() noexcept
            {
            };

            node * next_;

            BOOST_LEAF_CONSTEXPR explicit node( node * * & last ) noexcept:
                next_(nullptr)
            {
                BOOST_LEAF_ASSERT(last != nullptr);
                *last = this;
                last = &next_;
            }
        } * first_;

        template <class F>
        BOOST_LEAF_CONSTEXPR void for_each( F f ) const
        {
            for( node * p=first_; p; p=p->next_ )
                f(*p);
        }

    public:

        BOOST_LEAF_CONSTEXPR explicit capture_list( node * first ) noexcept:
            first_(first)
        {
        }

        BOOST_LEAF_CONSTEXPR capture_list( capture_list && other ) noexcept:
            first_(other.first_)
        {
            other.first_ = nullptr;
        }

        ~capture_list() noexcept
        {
            for( node const * p = first_; p; )
            {
                node const * n = p -> next_;
                delete p;
                p = n;
            }
        }

        void unload( int const err_id )
        {
            capture_list moved(first_);
            first_ = nullptr;
            tls::write_uint<leaf_detail::tls_tag_id_factory_current_id>(unsigned(err_id));
            moved.for_each(
                [err_id]( node & n )
                {
                    n.unload(err_id); // last node may throw
                } );
        }

        template <class CharT, class Traits>
        void print( std::basic_ostream<CharT, Traits> & os, char const * title, int const err_id_to_print ) const
        {
            BOOST_LEAF_ASSERT(title != nullptr);
#if BOOST_LEAF_CFG_DIAGNOSTICS
            if( first_ )
            {
                os << title;
                for_each(
                    [&os, err_id_to_print]( node const & n )
                    {
                        n.print(os, err_id_to_print);
                    } );
            }
#else
            (void) os;
            (void) title;
            (void) err_id_to_print;
#endif
        }
    };

}

} }

#endif

#endif
