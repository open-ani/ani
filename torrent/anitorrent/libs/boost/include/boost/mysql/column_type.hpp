//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_COLUMN_TYPE_HPP
#define BOOST_MYSQL_COLUMN_TYPE_HPP

#include <boost/mysql/detail/config.hpp>

#include <iosfwd>

namespace boost {
namespace mysql {

/**
 * \brief Represents the database type of a MySQL column.
 * \details This represents a database type, as opposed to \ref field_kind, which represents a
 * C++ type.
 *\n
 * Unless otherwise noted, the names in this enumeration
 * directly correspond to the names of the types you would use in
 * a `CREATE TABLE` statement to create a column of this type
 * (e.g. `VARCHAR` corresponds to \ref column_type::varchar).
 */
enum class column_type
{
    tinyint,    ///< `TINYINT` (signed and unsigned).
    smallint,   ///< `SMALLINT` (signed and unsigned).
    mediumint,  ///< `MEDIUMINT` (signed and unsigned).
    int_,       ///< `INT` (signed and unsigned).
    bigint,     ///< `BIGINT` (signed and unsigned).
    float_,     ///< `FLOAT` (warning: FLOAT(p) where p >= 24 creates a DOUBLE column).
    double_,    ///< `DOUBLE`
    decimal,    ///< `DECIMAL`
    bit,        ///< `BIT`
    year,       ///< `YEAR`
    time,       ///< `TIME`
    date,       ///< `DATE`
    datetime,   ///< `DATETIME`
    timestamp,  ///< `TIMESTAMP`
    char_,      ///< `CHAR` (any length)
    varchar,    ///< `VARCHAR` (any length)
    binary,     ///< `BINARY` (any length)
    varbinary,  ///< `VARBINARY` (any length)
    text,       ///< `TEXT` types (`TINYTEXT`, `MEDIUMTEXT`, `TEXT` and `LONGTEXT`)
    blob,       ///< `BLOB` types (`TINYBLOB`, `MEDIUMBLOB`, `BLOB` and `LONGBLOB`)
    enum_,      ///< `ENUM`
    set,        ///< `SET`
    json,       ///< `JSON`
    geometry,   ///< `GEOMETRY`
    unknown,    ///< None of the known types; maybe a new MySQL type we have no knowledge of.
};

/**
 * \brief Streams a `column_type`.
 */
BOOST_MYSQL_DECL
std::ostream& operator<<(std::ostream& os, column_type t);

}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/column_type.ipp>
#endif

#endif
