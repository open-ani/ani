//
// Copyright (c) 2022 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_RECYCLED_HPP
#define BOOST_URL_GRAMMAR_RECYCLED_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/grammar/detail/recycled.hpp>
#include <atomic>
#include <cstddef>
#include <type_traits>
#include <stddef.h> // ::max_align_t

#if !defined(BOOST_URL_DISABLE_THREADS)
# include <mutex>
#endif

namespace boost {
namespace urls {
namespace grammar {

/** Provides an aligned storage buffer aligned for T
*/
#ifdef BOOST_URL_DOCS
template<class T>
struct aligned_storage
{
    /** Return a pointer to the aligned storage area
    */
    void* addr() noexcept;

    /** Return a pointer to the aligned storage area
    */
    void const* addr() const noexcept;
};
#else
template<class T>
using aligned_storage =
    detail::aligned_storage_impl<
        detail::nearest_pow2(sizeof(T), 64),
            (alignof(::max_align_t) > alignof(T)) ?
                alignof(::max_align_t) : alignof(T)>;
#endif

//------------------------------------------------

/** A thread-safe collection of instances of T

    Instances of this type may be used to control
    where recycled instances of T come from when
    used with @ref recycled_ptr.

    @par Example
    @code
    static recycled< std::string > bin;

    recycled_ptr< std::string > ps( bin );

    // Put the string into a known state
    ps->clear();
    @endcode

    @see
        @ref recycled_ptr.
*/
template<class T>
class recycled
{
public:
    /** Destructor

        All recycled instances of T are destroyed.
        Undefined behavior results if there are
        any @ref recycled_ptr which reference
        this recycle bin.
    */
    ~recycled();

    /** Constructor
    */
    constexpr recycled() = default;

private:
    template<class>
    friend class recycled_ptr;

    struct U
    {
        T t;
        U* next = nullptr;

#if !defined(BOOST_URL_DISABLE_THREADS)
        std::atomic<
            std::size_t> refs;
#else
        std::size_t refs;
#endif


        U()
            : refs{1}
        {
        }
    };

    struct report;

    U* acquire();
    void release(U* u) noexcept;

    U* head_ = nullptr;

#if !defined(BOOST_URL_DISABLE_THREADS)
    std::mutex m_;
#endif
};

//------------------------------------------------

/** A pointer to shared instance of T

    This is a smart pointer container which can
    acquire shared ownership of an instance of
    `T` upon or after construction. The instance
    is guaranteed to be in a valid, but unknown
    state. Every recycled pointer references
    a valid recycle bin.

    @par Example
    @code
    static recycled< std::string > bin;

    recycled_ptr< std::string > ps( bin );

    // Put the string into a known state
    ps->clear();
    @endcode

    @tparam T the type of object to
        acquire, which must be
        <em>DefaultConstructible</em>.
*/
template<class T>
class recycled_ptr
{
    // T must be default constructible!
    static_assert(
        std::is_default_constructible<T>::value,
        "T must be DefaultConstructible");

    friend class recycled<T>;

    using B = recycled<T>;
    using U = typename B::U;

    B* bin_ = nullptr;
    U* p_ = nullptr;

public:
    /** Destructor

        If this is not empty, shared ownership
        of the pointee is released. If this was
        the last reference, the object is
        returned to the original recycle bin.

        @par Effects
        @code
        this->release();
        @endcode
    */
    ~recycled_ptr();

    /** Constructor

        Upon construction, this acquires
        exclusive access to an object of type
        `T` which is either recycled from the
        specified bin, or newly allocated.
        The object is in an unknown but
        valid state.

        @par Example
        @code
        static recycled< std::string > bin;

        recycled_ptr< std::string > ps( bin );

        // Put the string into a known state
        ps->clear();
        @endcode

        @par Postconditions
        @code
        &this->bin() == &bin && ! this->empty()
        @endcode

        @param bin The recycle bin to use

        @see
            @ref recycled.
    */
    explicit
    recycled_ptr(recycled<T>& bin);

    /** Constructor

        After construction, this is empty and
        refers to the specified recycle bin.

        @par Example
        @code
        static recycled< std::string > bin;

        recycled_ptr< std::string > ps( bin, nullptr );

        // Acquire a string and put it into a known state
        ps->acquire();
        ps->clear();
        @endcode

        @par Postconditions
        @code
        &this->bin() == &bin && this->empty()
        @endcode

        @par Exception Safety
        Throws nothing.

        @param bin The recycle bin to use

        @see
            @ref acquire,
            @ref recycled,
            @ref release.
    */
    recycled_ptr(
        recycled<T>& bin,
        std::nullptr_t) noexcept;

    /** Constructor

        Upon construction, this acquires
        exclusive access to an object of type
        `T` which is either recycled from a
        global recycle bin, or newly allocated.
        The object is in an unknown but
        valid state.

        @par Example
        @code
        recycled_ptr< std::string > ps;

        // Put the string into a known state
        ps->clear();
        @endcode

        @par Postconditions
        @code
        &this->bin() != nullptr && ! this->empty()
        @endcode

        @see
            @ref recycled.
    */
    recycled_ptr();

    /** Constructor

        After construction, this is empty
        and refers to a global recycle bin.

        @par Example
        @code
        recycled_ptr< std::string > ps( nullptr );

        // Acquire a string and put it into a known state
        ps->acquire();
        ps->clear();
        @endcode

        @par Postconditions
        @code
        &this->bin() != nullptr && this->empty()
        @endcode

        @par Exception Safety
        Throws nothing.

        @see
            @ref acquire,
            @ref recycled,
            @ref release.
    */
    recycled_ptr(
        std::nullptr_t) noexcept;

    /** Constructor

        If `other` references an object, the
        newly constructed pointer acquires
        shared ownership. Otherwise this is
        empty. The new pointer references
        the same recycle bin as `other`.

        @par Postconditions
        @code
        &this->bin() == &other->bin() && this->get() == other.get()
        @endcode

        @par Exception Safety
        Throws nothing.

        @param other The pointer to copy
    */
    recycled_ptr(
        recycled_ptr const& other) noexcept;

    /** Constructor

        If `other` references an object,
        ownership is transferred including
        a reference to the recycle bin. After
        the move, the moved-from object is empty.

        @par Postconditions
        @code
        &this->bin() == &other->bin() && ! this->empty() && other.empty()
        @endcode

        @par Exception Safety
        Throws nothing.

        @param other The pointer to move from
    */
    recycled_ptr(
        recycled_ptr&& other) noexcept;

    /** Assignment

        If `other` references an object,
        ownership is transferred including
        a reference to the recycle bin. After
        the move, the moved-from object is empty.

        @par Effects
        @code
        this->release()
        @endcode

        @par Postconditions
        @code
        &this->bin() == &other->bin()
        @endcode

        @par Exception Safety
        Throws nothing.

        @param other The pointer to move from
    */
    recycled_ptr&
    operator=(
        recycled_ptr&& other) noexcept;

    /** Assignment

        If `other` references an object,
        this acquires shared ownership and
        references the same recycle bin as
        `other`. The previous object if any
        is released.

        @par Effects
        @code
        this->release()
        @endcode

        @par Postconditions
        @code
        &this->bin() == &other->bin() && this->get() == other.get()
        @endcode

        @par Exception Safety
        Throws nothing.

        @param other The pointer to copy from
    */
    recycled_ptr&
    operator=(
        recycled_ptr const& other) noexcept;

    /** Return true if this does not reference an object

        @par Exception Safety
        Throws nothing.
    */
    bool
    empty() const noexcept
    {
        return p_ == nullptr;
    }

    /** Return true if this references an object

        @par Effects
        @code
        return ! this->empty();
        @endcode

        @par Exception Safety
        Throws nothing.
    */
    explicit
    operator bool() const noexcept
    {
        return p_ != nullptr;
    }

    /** Return the referenced recycle bin

        @par Exception Safety
        Throws nothing.
    */
    recycled<T>&
    bin() const noexcept
    {
        return *bin_;
    }

    /** Return the referenced object

        If this is empty, `nullptr` is returned.

        @par Exception Safety
        Throws nothing.
    */
    T* get() const noexcept
    {
        return &p_->t;
    }

    /** Return the referenced object

        If this is empty, `nullptr` is returned.

        @par Exception Safety
        Throws nothing.
    */
    T* operator->() const noexcept
    {
        return get();
    }

    /** Return the referenced object

        @par Preconditions
        @code
        not this->empty()
        @endcode
    */
    T& operator*() const noexcept
    {
        return *get();
    }

    /** Return the referenced object

        If this references an object, it is
        returned. Otherwise, exclusive ownership
        of a new object of type `T` is acquired
        and returned.

        @par Postconditions
        @code
        not this->empty()
        @endcode
    */
    T& acquire();

    /** Release the referenced object

        If this references an object, it is
        released to the referenced recycle bin.
        The pointer continues to reference
        the same recycle bin.

        @par Postconditions
        @code
        this->empty()
        @endcode

        @par Exception Safety
        Throws nothing.
    */
    void release() noexcept;
};

} // grammar
} // urls
} // boost

#include <boost/url/grammar/impl/recycled.hpp>

#endif
