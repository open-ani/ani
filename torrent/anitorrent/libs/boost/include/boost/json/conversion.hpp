//
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_CONVERSION_HPP
#define BOOST_JSON_CONVERSION_HPP

#include <boost/json/detail/config.hpp>
#include <boost/json/fwd.hpp>

#include <type_traits>

namespace boost {
namespace json {

namespace detail {

template< class Ctx, class T, class Dir >
struct supported_context;

} // namespace detail

/** Customization point tag.

    This tag type is used by the function
    @ref value_from to select overloads
    of `tag_invoke`.

    @note This type is empty; it has no members.

    @see @ref value_from, @ref value_to, @ref value_to_tag,
    <a href="http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2019/p1895r0.pdf">
        tag_invoke: A general pattern for supporting customisable functions</a>
*/
struct value_from_tag { };

/** Customization point tag type.

    This tag type is used by the function
    @ref value_to to select overloads
    of `tag_invoke`.

    @note This type is empty; it has no members.

    @see @ref value_from, @ref value_from_tag, @ref value_to,
    <a href="http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2019/p1895r0.pdf">
        tag_invoke: A general pattern for supporting customisable functions</a>
*/
template<class T>
struct value_to_tag { };

/** Customization point tag type.

    This tag type is used by the function
    @ref try_value_to to select overloads
    of `tag_invoke`.

    @note This type is empty; it has no members.

    @see @ref value_to, @ref value_to_tag
    <a href="http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2019/p1895r0.pdf">
        tag_invoke: A general pattern for supporting customisable functions</a>
*/
template<class T>
struct try_value_to_tag { };

/** Determine if `T` can be treated like a string during conversions.

    Provides the member constant `value` that is equal to `true`, if `T` is
    convertible to @ref string_view. Otherwise, `value` is equal to `false`.
    <br>

    Users can specialize the trait for their own types if they don't want them
    to be treated like strings. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_string_like<your::string> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode

    @par Types satisfying the trait

    @ref string,
    @ref string_view,
    <a href="https://en.cppreference.com/w/cpp/string/basic_string"><tt>std::string</tt></a>,
    <a href="https://en.cppreference.com/w/cpp/string/basic_string_view"><tt>std::string_view</tt></a>.

    @see @ref value_from, @ref value_to
*/
template<class T>
struct is_string_like;

/** Determine if `T` can be treated like `std::filesystem::path` during conversions.

    Given `t`, a glvalue of type `T`, if

    @li given `It`, the type denoted by `decltype(std::begin(t))`,
        <tt>std::iterator_traits<It>::iterator_category</tt> is well-formed and
        denotes a type; and

    @li <tt>std::iterator_traits<It>::value_type</tt> is `T`; and

    @li `T::value_type` is well-formed and denotes a type; and

    @li `T::string_type` is well-formed, denotes a type, and is an alias for
    `std::basic_string< T::value_type >`;

    then the trait provides the member constant `value` that is equal to
    `true`. Otherwise, `value` is equal to `false`.<br>

    Users can specialize the trait for their own types if they don't want them
    to be treated like filesystem paths. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_path_like<your::path> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode


    @par Types satisfying the trait

    `std::filesystem::path`, `boost::filesystem::path`.

    @see @ref value_from, @ref value_to
*/
template<class T>
struct is_path_like;

/** Determine if `T` can be treated like a sequence during conversions.

    Given `t`, a glvalue of type `T`, if

    @li given `It`, the type denoted by `decltype(std::begin(t))`,
        <tt>std::iterator_traits<It>::iterator_category</tt> is well-formed and
        denotes a type; and

    @li `decltype(std::end(t))` also denotes the type `It`; and

    @li <tt>std::iterator_traits<It>::value_type</tt> is not `T`; and

    then the trait provides the member constant `value` that is equal to
    `true`. Otherwise, `value` is equal to `false`.<br>

    Users can specialize the trait for their own types if they don't want them
    to be treated like sequences. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_sequence_like<your::container> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode


    @par Types satisfying the trait

    Any <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    array types.

    @see @ref value_from, @ref value_to
*/
template<class T>
struct is_sequence_like;

/** Determine if `T` can be treated like a 1-to-1 mapping during
    conversions.

    Given `t`, a glvalue of type `T`, if

    @li <tt>is_sequence_like<T>::value</tt> is `true`; and

    @li given type `It` denoting `decltype(std::begin(t))`, and types `K`
        and `M`,  <tt>std::iterator_traits<It>::value_type</tt> denotes
        `std::pair<K, M>`; and

    @li <tt>std::is_string_like<K>::value</tt> is `true`; and

    @li given `v`, a glvalue of type `V`, and `E`, the type denoted by
        `decltype(t.emplace(v))`,
        <tt>std::is_tuple_like<E>::value</tt> is `true`;

    then the trait provides the member constant `value`
    that is equal to `true`. Otherwise, `value` is equal to `false`.<br>

    Users can specialize the trait for their own types if they don't want them
    to be treated like mappings. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_map_like<your::map> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode


    @note

    The restriction for `t.emplace()` return type ensures that the container
    does not accept duplicate keys.

    @par Types satisfying the trait

    <a href="https://en.cppreference.com/w/cpp/container/map"><tt>std::map</tt></a>,
    <a href="https://en.cppreference.com/w/cpp/container/unordered_map"><tt>std::unordered_map</tt></a>.

    @see @ref value_from, @ref value_to
*/
template<class T>
struct is_map_like;

/** Determine if `T` can be treated like a tuple during conversions.

    Provides the member constant `value` that is equal to `true`, if
    <tt>std::tuple_size<T>::value</tt> is a positive number. Otherwise, `value`
    is equal to `false`.<br>

    Users can specialize the trait for their own types if they don't want them
    to be treated like tuples. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_tuple_like<your::tuple> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode


    @par Types satisfying the trait

    <a href="https://en.cppreference.com/w/cpp/utility/tuple"><tt>std::tuple</tt></a>,
    <a href="https://en.cppreference.com/w/cpp/utility/pair"><tt>std::pair</tt></a>.

    @see @ref value_from, @ref value_to
*/
template<class T>
struct is_tuple_like;

/** Determine if `T` can be treated like null during conversions.

    Primary template instantiations provide the member constant `value` that is
    equal to `false`. Users can specialize the trait for their own types if
    they **do** want them to be treated as nulls. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_null_like<your::null_type> : std::true_type
    { };

    } // namespace boost
    } // namespace json
    @endcode


    @par Types satisfying the trait

    <a href="https://en.cppreference.com/w/cpp/types/nullptr_t"><tt>std::nullptr_t</tt></a>.

    @see @ref value_from, @ref value_to
*/
template<class T>
struct is_null_like
    : std::false_type
{ };

/** Determine if `T` should be treated as a described class

    Described classes are serialised as objects with an element for each
    described data member. A described class should not have described
    bases or non-public members.<br>

    Or more formally, given `L`, a class template
    of the form `template<class...> struct L {};`, if

    @li <tt>boost::describe::has_members<T, boost::describe::mod_public>::value</tt> is `true`; and

    @li `boost::describe::describe_members<T, boost::describe::mod_private | boost::describe::mod_protected>` denotes `L<>`; and

    @li `boost::describe::describe_bases<T, boost::describe::mod_any_access>` denotes `L<>`; and

    @li <tt>std::is_union<T>::value</tt> is `false`;

    then the trait provides the member constant `value`
    that is equal to `true`. Otherwise, `value` is equal to `false`.<br>

    Users can specialize the trait for their own types if they don't want them
    to be treated as described classes. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_described_class<your::described_class> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode

    Users can also specialize the trait for their own types _with_ described
    bases or described non-public data members to enable this conversion
    implementation. In this case the class will be serialized in a flattened
    way, that is members of bases will be serialized as direct elements of the
    object, and no nested objects will be created for bases.

    @see <a href="https://www.boost.org/doc/libs/develop/libs/describe/doc/html/describe.html">Boost.Describe</a>.
*/
template<class T>
struct is_described_class;

/** Determine if `T` should be treated as a described enum

    Described enums are serialised as strings when their value equals to a
    described enumerator, and as integers otherwise. The reverse operation
    does not convert numbers to enums values, though, and instead produces
    an error.<br>

    If <tt>boost::describe::has_describe_enumerators<T>::value</tt> is `true`,
    then the trait provides the member constant `value`
    that is equal to `true`. Otherwise, `value` is equal to `false`.<br>

    Users can specialize the trait for their own enums if they don't want them
    to be treated as described enums. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_described_enum<your::described_enum> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode

    @see <a href="https://www.boost.org/doc/libs/develop/libs/describe/doc/html/describe.html">Boost.Describe</a>.
*/
template<class T>
struct is_described_enum;

/** Determine if `T` should be treated as a variant

    Variants are serialised the same way their active alternative is
    serialised. The opposite conversion selects the first alternative for which
    conversion succeeds.<br>

    Given `t`, a glvalue of type ` const T`, if
    <tt>t.valueless_by_exception()</tt> is well-formed, then the trait provides
    the member constant `value` that is equal to `true`. Otherwise, `value` is
    equal to `false`.<br>

    Users can specialize the trait for their own types if they don't want them
    to be treated as variants. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_variant_like<your::variant> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode
*/
template<class T>
struct is_variant_like;

/** Determine if `T` should be treated as an optional

    Optionals are serialised as `null` if empty, or as the stored type
    otherwise.<br>

    Given `t`, a glvalue of type `T`, if

    @li <tt>decltype( t.value() )</tt> is well-formed and isn't a void type; and

    @li <tt>t.reset()</tt> is well-formed;

    then the trait provides the member constant `value`
    that is equal to `true`. Otherwise, `value` is equal to `false`.<br>

    Users can specialize the trait for their own types if they don't want them
    to be treated as optionals. For example:

    @code
    namespace boost {
    namespace json {

    template <>
    struct is_optional_like<your::optional> : std::false_type
    { };

    } // namespace boost
    } // namespace json
    @endcode
*/
template<class T>
struct is_optional_like;

} // namespace json
} // namespace boost

#include <boost/json/impl/conversion.hpp>

#endif // BOOST_JSON_CONVERSION_HPP
