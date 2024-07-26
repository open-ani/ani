//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_TEST_HANDLER_HPP
#define BOOST_BEAST_TEST_HANDLER_HPP

#include <boost/beast/_experimental/unit_test/suite.hpp>
#include <boost/beast/core/error.hpp>
#include <boost/asio/io_context.hpp>
#include <boost/core/exchange.hpp>
#include <boost/optional.hpp>

namespace boost {
namespace beast {
namespace test {

/** A CompletionHandler used for testing.

    This completion handler is used by tests to ensure correctness
    of behavior. It is designed as a single type to reduce template
    instantiations, with configurable settings through constructor
    arguments. Typically this type will be used in type lists and
    not instantiated directly; instances of this class are returned
    by the helper functions listed below.

    @see success_handler, @ref fail_handler, @ref any_handler
*/
class handler
{
    boost::optional<error_code> ec_;
    bool pass_ = false;
    boost::source_location loc_{BOOST_CURRENT_LOCATION};
public:
    handler(boost::source_location loc = BOOST_CURRENT_LOCATION) : loc_(loc) {}

    explicit
    handler(error_code ec, boost::source_location loc = BOOST_CURRENT_LOCATION)
        : ec_(ec), loc_(loc)
    {
    }

    explicit
    handler(boost::none_t, boost::source_location loc = BOOST_CURRENT_LOCATION) : loc_(loc)
    {
    }

    handler(handler&& other)
        : ec_(other.ec_)
        , pass_(boost::exchange(other.pass_, true))
        , loc_(other.loc_)

    {
    }

    ~handler()
    {
        ::boost::beast::unit_test::suite::this_suite()->expect(pass_, loc_.file_name(), loc_.line());
    }

    template<class... Args>
    void
    operator()(error_code ec, Args&&...)
    {
        ::boost::beast::unit_test::suite::this_suite()->expect(!pass_, loc_.file_name(), loc_.line());
        if (ec_ && ec != *ec_)
            ::boost::beast::unit_test::suite::this_suite()->fail(ec.message(), loc_.file_name(), loc_.line());
        else
            ::boost::beast::unit_test::suite::this_suite()->pass();
        pass_ = true;
    }

    void
    operator()()
    {
        ::boost::beast::unit_test::suite::this_suite()->expect(!pass_, loc_.file_name(), loc_.line());
        if (ec_ && ec_->failed())
            ::boost::beast::unit_test::suite::this_suite()->fail(ec_->message(), loc_.file_name(), loc_.line());
        else
            ::boost::beast::unit_test::suite::this_suite()->pass();

        pass_ = true;
    }

    template<class Arg0, class... Args,
        class = typename std::enable_if<
            ! std::is_convertible<Arg0, error_code>::value>::type>
    void
    operator()(Arg0&&, Args&&...)
    {
        ::boost::beast::unit_test::suite::this_suite()->expect(!pass_, loc_.file_name(), loc_.line());
        if (ec_ && ec_->failed())
            ::boost::beast::unit_test::suite::this_suite()->fail(ec_->message(), loc_.file_name(), loc_.line());
        else
            ::boost::beast::unit_test::suite::this_suite()->pass();
        pass_ = true;
    }
};

/** Return a test CompletionHandler which requires success.
    
    The returned handler can be invoked with any signature whose
    first parameter is an `error_code`. The handler fails the test
    if:

    @li The handler is destroyed without being invoked, or

    @li The handler is invoked with a non-successful error code.
*/
inline
handler
success_handler(boost::source_location loc = BOOST_CURRENT_LOCATION) noexcept
{
    return handler(error_code{}, loc);
}

/** Return a test CompletionHandler which requires invocation.

    The returned handler can be invoked with any signature.
    The handler fails the test if:

    @li The handler is destroyed without being invoked.
*/
inline
handler
any_handler(boost::source_location loc = BOOST_CURRENT_LOCATION) noexcept
{
    return handler(boost::none, loc);
}

/** Return a test CompletionHandler which requires a specific error code.

    This handler can be invoked with any signature whose first
    parameter is an `error_code`. The handler fails the test if:

    @li The handler is destroyed without being invoked.

    @li The handler is invoked with an error code different from
    what is specified.

    @param ec The error code to specify.
*/
inline
handler
fail_handler(error_code ec,boost::source_location loc = BOOST_CURRENT_LOCATION) noexcept
{
    return handler(ec, loc);
}

/** Run an I/O context.
    
    This function runs and dispatches handlers on the specified
    I/O context, until one of the following conditions is true:
        
    @li The I/O context runs out of work.

    @param ioc The I/O context to run
*/
inline
void
run(net::io_context& ioc)
{
    ioc.run();
    ioc.restart();
}

/** Run an I/O context for a certain amount of time.
    
    This function runs and dispatches handlers on the specified
    I/O context, until one of the following conditions is true:
        
    @li The I/O context runs out of work.

    @li No completions occur and the specified amount of time has elapsed.

    @param ioc The I/O context to run

    @param elapsed The maximum amount of time to run for.
*/
template<class Rep, class Period>
void
run_for(
    net::io_context& ioc,
    std::chrono::duration<Rep, Period> elapsed)
{
    ioc.run_for(elapsed);
    ioc.restart();
}

} // test
} // beast
} // boost

#endif
