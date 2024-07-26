//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//
// This is a derivative work based on Zlib, copyright below:
/*
    Copyright (C) 1995-2022 Jean-loup Gailly and Mark Adler

    This software is provided 'as-is', without any express or implied
    warranty.  In no event will the authors be held liable for any damages
    arising from the use of this software.

    Permission is granted to anyone to use this software for any purpose,
    including commercial applications, and to alter it and redistribute it
    freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not
       claim that you wrote the original software. If you use this software
       in a product, an acknowledgment in the product documentation would be
       appreciated but is not required.
    2. Altered source versions must be plainly marked as such, and must not be
       misrepresented as being the original software.
    3. This notice may not be removed or altered from any source distribution.

    Jean-loup Gailly        Mark Adler
    jloup@gzip.org          madler@alumni.caltech.edu

    The data format used by the zlib library is described by RFCs (Request for
    Comments) 1950 to 1952 in the files http://tools.ietf.org/html/rfc1950
    (zlib format), rfc1951 (deflate format) and rfc1952 (gzip format).
*/

#ifndef BOOST_BEAST_ZLIB_ZLIB_HPP
#define BOOST_BEAST_ZLIB_ZLIB_HPP

#include <boost/beast/core/detail/config.hpp>
#include <cstdint>
#include <cstdlib>

namespace boost {
namespace beast {
namespace zlib {

#if !defined(__MACTYPES__)
using Byte = unsigned char; // 8 bits
#endif
using uInt = unsigned int;  // 16 bits or more

/* Possible values of the data_type field (though see inflate()) */
enum kind
{
    binary    = 0,
    text      = 1,
    unknown   = 2
};

/** Deflate codec parameters.

    Objects of this type are filled in by callers and provided to the
    deflate codec to define the input and output areas for the next
    compress or decompress operation.

    The application must update next_in and avail_in when avail_in has dropped
    to zero.  It must update next_out and avail_out when avail_out has dropped
    to zero.  The application must initialize zalloc, zfree and opaque before
    calling the init function.  All other fields are set by the compression
    library and must not be updated by the application.

    The fields total_in and total_out can be used for statistics or progress
    reports.  After compression, total_in holds the total size of the
    uncompressed data and may be saved for use in the decompressor (particularly
    if the decompressor wants to decompress everything in a single step).
*/
struct z_params
{
    /** A pointer to the next input byte.

        If there is no more input, this may be set to `nullptr`.

        The application must update `next_in` and `avail_in` when
        `avail_in` has dropped to zero.
    */
    void const* next_in;

    /** The number of bytes of input available at `next_in`.

        If there is no more input, this should be set to zero.

        The application must update `next_in` and `avail_in` when
        `avail_in` has dropped to zero.
    */
    std::size_t avail_in;

    /** The total number of input bytes read so far.

        This field is set by the compression library and must
        not be updated by the application.

        This field can also be used for statistics or progress
        reports.

        After compression, total_in holds the total size of the
        uncompressed data and may be saved for use by the
        decompressor (particularly if the decompressor wants
        to decompress everything in a single step).

    */
    std::size_t total_in = 0;

    /** A pointer to the next output byte.

        The application must update `next_out` and `avail_out`
        when avail_out has dropped to zero.
    */
    void* next_out;

    /** The remaining bytes of space at `next_out`.

        The application must update `next_out` and `avail_out`
        when avail_out has dropped to zero.
    */
    std::size_t avail_out;

    /** The total number of bytes output so far.

        This field is set by the compression library and must
        not be updated by the application.

        This field can also be used for statistics or progress
        reports.
    */
    std::size_t total_out = 0;

    /** Best guess about the data type: binary or text

        This represents binary or text for deflate, or
        the decoding state for inflate.
     */
    int data_type = unknown;
};

/** Flush option.

    The allowed flush values for the @ref deflate_stream::write
    and @ref inflate_stream::write functions.

    Please refer to @ref deflate_stream::write and
    @ref inflate_stream::write for details.

    @see
        deflate_stream::write,
        inflate_stream::write

*/
enum class Flush
{
    // order matters

    /// No policy
    none,

    /// Flush all pending output on a bit boundary and hold up to seven bits
    block,

    /// Flush all pending output on a bit boundary
    partial,

    /// Flush all pending output on a byte boundary
    sync,

    /// Flush all pending output on a byte boundary and reset state
    full,

    /// Compress the input left in a single step
    finish,

    /// Flush output as in Flush::block or at the end of each deflate block header
    trees
};

/** Compression levels.

    The compression levels go from 0 and 9: 1 gives best speed, 9 gives
    best compression.

    Compression level 0 gives no compression at all. The input data is
    simply copied a block at a time.

    A compression level 6 is usually a default compromise between
    speed and compression.

*/
enum compression
{
    none        =  0,
    best_speed            =  1,
    best_size      =  9,
    default_size   = -1
};

/** Compression strategy.

    These are used when compressing streams.
*/
enum class Strategy
{
    /** Default strategy.

        This is suitable for general purpose compression, and works
        well in the majority of cases.
    */
    normal,

    /** Filtered strategy.

        This strategy should be used when the data be compressed
        is produced by a filter or predictor.
    */
    filtered,

    /** Huffman-only strategy.

        This strategy only performs Huffman encoding, without doing
        any string matching.
    */
    huffman,

    /** Run Length Encoding strategy.

        This strategy limits match distances to one, making it
        equivalent to run length encoding. This can give better
        performance for things like PNG image data.
    */
    rle,

    /** Fixed table strategy.

        This strategy prevents the use of dynamic Huffman codes,
        allowing for a simpler decoder for special applications.
    */
    fixed
};

} // zlib
} // beast
} // boost

#endif

