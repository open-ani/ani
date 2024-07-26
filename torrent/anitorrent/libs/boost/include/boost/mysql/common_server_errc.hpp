//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_COMMON_SERVER_ERRC_HPP
#define BOOST_MYSQL_COMMON_SERVER_ERRC_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/system/error_category.hpp>

namespace boost {
namespace mysql {

/**
 * \brief Server-defined error codes, shared between MySQL and MariaDB.
 * \details The numeric value and semantics match the ones described in the MySQL documentation.
 * For more info, consult the error reference for
 * <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html">MySQL 8.0</a>, 
 * <a href="https://dev.mysql.com/doc/mysql-errors/5.7/en/server-error-reference.html">MySQL 5.7</a>,
 * <a href="https://mariadb.com/kb/en/mariadb-error-codes/">MariaDB</a>.
 */
enum class common_server_errc : int
{

    /// Common server error. Error number: 1000, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_hashchk">ER_HASHCHK</a>.
    er_hashchk = 1000,

    /// Common server error. Error number: 1001, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_nisamchk">ER_NISAMCHK</a>.
    er_nisamchk = 1001,

    /// Common server error. Error number: 1002, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no">ER_NO</a>.
    er_no = 1002,

    /// Common server error. Error number: 1003, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_yes">ER_YES</a>.
    er_yes = 1003,

    /// Common server error. Error number: 1004, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_file">ER_CANT_CREATE_FILE</a>.
    er_cant_create_file = 1004,

    /// Common server error. Error number: 1005, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_table">ER_CANT_CREATE_TABLE</a>.
    er_cant_create_table = 1005,

    /// Common server error. Error number: 1006, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_db">ER_CANT_CREATE_DB</a>.
    er_cant_create_db = 1006,

    /// Common server error. Error number: 1007, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_db_create_exists">ER_DB_CREATE_EXISTS</a>.
    er_db_create_exists = 1007,

    /// Common server error. Error number: 1008, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_db_drop_exists">ER_DB_DROP_EXISTS</a>.
    er_db_drop_exists = 1008,

    /// Common server error. Error number: 1009, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_db_drop_delete">ER_DB_DROP_DELETE</a>.
    er_db_drop_delete = 1009,

    /// Common server error. Error number: 1010, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_db_drop_rmdir">ER_DB_DROP_RMDIR</a>.
    er_db_drop_rmdir = 1010,

    /// Common server error. Error number: 1011, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_delete_file">ER_CANT_DELETE_FILE</a>.
    er_cant_delete_file = 1011,

    /// Common server error. Error number: 1012, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_find_system_rec">ER_CANT_FIND_SYSTEM_REC</a>.
    er_cant_find_system_rec = 1012,

    /// Common server error. Error number: 1013, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_get_stat">ER_CANT_GET_STAT</a>.
    er_cant_get_stat = 1013,

    /// Common server error. Error number: 1014, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_get_wd">ER_CANT_GET_WD</a>.
    er_cant_get_wd = 1014,

    /// Common server error. Error number: 1015, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_lock">ER_CANT_LOCK</a>.
    er_cant_lock = 1015,

    /// Common server error. Error number: 1016, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_open_file">ER_CANT_OPEN_FILE</a>.
    er_cant_open_file = 1016,

    /// Common server error. Error number: 1017, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_file_not_found">ER_FILE_NOT_FOUND</a>.
    er_file_not_found = 1017,

    /// Common server error. Error number: 1018, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_read_dir">ER_CANT_READ_DIR</a>.
    er_cant_read_dir = 1018,

    /// Common server error. Error number: 1019, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_wd">ER_CANT_SET_WD</a>.
    er_cant_set_wd = 1019,

    /// Common server error. Error number: 1020, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_checkread">ER_CHECKREAD</a>.
    er_checkread = 1020,

    /// Common server error. Error number: 1021, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_disk_full">ER_DISK_FULL</a>.
    er_disk_full = 1021,

    /// Common server error. Error number: 1022, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_key">ER_DUP_KEY</a>.
    er_dup_key = 1022,

    /// Common server error. Error number: 1023, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_on_close">ER_ERROR_ON_CLOSE</a>.
    er_error_on_close = 1023,

    /// Common server error. Error number: 1024, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_on_read">ER_ERROR_ON_READ</a>.
    er_error_on_read = 1024,

    /// Common server error. Error number: 1025, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_on_rename">ER_ERROR_ON_RENAME</a>.
    er_error_on_rename = 1025,

    /// Common server error. Error number: 1026, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_on_write">ER_ERROR_ON_WRITE</a>.
    er_error_on_write = 1026,

    /// Common server error. Error number: 1027, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_file_used">ER_FILE_USED</a>.
    er_file_used = 1027,

    /// Common server error. Error number: 1028, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_filsort_abort">ER_FILSORT_ABORT</a>.
    er_filsort_abort = 1028,

    /// Common server error. Error number: 1029, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_form_not_found">ER_FORM_NOT_FOUND</a>.
    er_form_not_found = 1029,

    /// Common server error. Error number: 1030, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_get_errno">ER_GET_ERRNO</a>.
    er_get_errno = 1030,

    /// Common server error. Error number: 1031, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_illegal_ha">ER_ILLEGAL_HA</a>.
    er_illegal_ha = 1031,

    /// Common server error. Error number: 1032, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_key_not_found">ER_KEY_NOT_FOUND</a>.
    er_key_not_found = 1032,

    /// Common server error. Error number: 1033, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_not_form_file">ER_NOT_FORM_FILE</a>.
    er_not_form_file = 1033,

    /// Common server error. Error number: 1034, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_not_keyfile">ER_NOT_KEYFILE</a>.
    er_not_keyfile = 1034,

    /// Common server error. Error number: 1035, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_old_keyfile">ER_OLD_KEYFILE</a>.
    er_old_keyfile = 1035,

    /// Common server error. Error number: 1036, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_open_as_readonly">ER_OPEN_AS_READONLY</a>.
    er_open_as_readonly = 1036,

    /// Common server error. Error number: 1037, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_outofmemory">ER_OUTOFMEMORY</a>.
    er_outofmemory = 1037,

    /// Common server error. Error number: 1038, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_out_of_sortmemory">ER_OUT_OF_SORTMEMORY</a>.
    er_out_of_sortmemory = 1038,

    /// Common server error. Error number: 1039, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unexpected_eof">ER_UNEXPECTED_EOF</a>.
    er_unexpected_eof = 1039,

    /// Common server error. Error number: 1040, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_con_count_error">ER_CON_COUNT_ERROR</a>.
    er_con_count_error = 1040,

    /// Common server error. Error number: 1041, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_out_of_resources">ER_OUT_OF_RESOURCES</a>.
    er_out_of_resources = 1041,

    /// Common server error. Error number: 1042, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_host_error">ER_BAD_HOST_ERROR</a>.
    er_bad_host_error = 1042,

    /// Common server error. Error number: 1043, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_handshake_error">ER_HANDSHAKE_ERROR</a>.
    er_handshake_error = 1043,

    /// Common server error. Error number: 1044, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dbaccess_denied_error">ER_DBACCESS_DENIED_ERROR</a>.
    er_dbaccess_denied_error = 1044,

    /// Common server error. Error number: 1045, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_access_denied_error">ER_ACCESS_DENIED_ERROR</a>.
    er_access_denied_error = 1045,

    /// Common server error. Error number: 1046, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_db_error">ER_NO_DB_ERROR</a>.
    er_no_db_error = 1046,

    /// Common server error. Error number: 1047, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_com_error">ER_UNKNOWN_COM_ERROR</a>.
    er_unknown_com_error = 1047,

    /// Common server error. Error number: 1048, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_null_error">ER_BAD_NULL_ERROR</a>.
    er_bad_null_error = 1048,

    /// Common server error. Error number: 1049, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_db_error">ER_BAD_DB_ERROR</a>.
    er_bad_db_error = 1049,

    /// Common server error. Error number: 1050, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_exists_error">ER_TABLE_EXISTS_ERROR</a>.
    er_table_exists_error = 1050,

    /// Common server error. Error number: 1051, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_table_error">ER_BAD_TABLE_ERROR</a>.
    er_bad_table_error = 1051,

    /// Common server error. Error number: 1052, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_non_uniq_error">ER_NON_UNIQ_ERROR</a>.
    er_non_uniq_error = 1052,

    /// Common server error. Error number: 1053, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_server_shutdown">ER_SERVER_SHUTDOWN</a>.
    er_server_shutdown = 1053,

    /// Common server error. Error number: 1054, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_field_error">ER_BAD_FIELD_ERROR</a>.
    er_bad_field_error = 1054,

    /// Common server error. Error number: 1055, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_field_with_group">ER_WRONG_FIELD_WITH_GROUP</a>.
    er_wrong_field_with_group = 1055,

    /// Common server error. Error number: 1056, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_group_field">ER_WRONG_GROUP_FIELD</a>.
    er_wrong_group_field = 1056,

    /// Common server error. Error number: 1057, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_sum_select">ER_WRONG_SUM_SELECT</a>.
    er_wrong_sum_select = 1057,

    /// Common server error. Error number: 1058, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_value_count">ER_WRONG_VALUE_COUNT</a>.
    er_wrong_value_count = 1058,

    /// Common server error. Error number: 1059, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_ident">ER_TOO_LONG_IDENT</a>.
    er_too_long_ident = 1059,

    /// Common server error. Error number: 1060, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_fieldname">ER_DUP_FIELDNAME</a>.
    er_dup_fieldname = 1060,

    /// Common server error. Error number: 1061, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_keyname">ER_DUP_KEYNAME</a>.
    er_dup_keyname = 1061,

    /// Common server error. Error number: 1062, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_entry">ER_DUP_ENTRY</a>.
    er_dup_entry = 1062,

    /// Common server error. Error number: 1063, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_field_spec">ER_WRONG_FIELD_SPEC</a>.
    er_wrong_field_spec = 1063,

    /// Common server error. Error number: 1064, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_parse_error">ER_PARSE_ERROR</a>.
    er_parse_error = 1064,

    /// Common server error. Error number: 1065, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_empty_query">ER_EMPTY_QUERY</a>.
    er_empty_query = 1065,

    /// Common server error. Error number: 1066, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_nonuniq_table">ER_NONUNIQ_TABLE</a>.
    er_nonuniq_table = 1066,

    /// Common server error. Error number: 1067, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_invalid_default">ER_INVALID_DEFAULT</a>.
    er_invalid_default = 1067,

    /// Common server error. Error number: 1068, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_multiple_pri_key">ER_MULTIPLE_PRI_KEY</a>.
    er_multiple_pri_key = 1068,

    /// Common server error. Error number: 1069, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_keys">ER_TOO_MANY_KEYS</a>.
    er_too_many_keys = 1069,

    /// Common server error. Error number: 1070, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_key_parts">ER_TOO_MANY_KEY_PARTS</a>.
    er_too_many_key_parts = 1070,

    /// Common server error. Error number: 1071, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_key">ER_TOO_LONG_KEY</a>.
    er_too_long_key = 1071,

    /// Common server error. Error number: 1072, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_key_column_does_not_exits">ER_KEY_COLUMN_DOES_NOT_EXITS</a>.
    er_key_column_does_not_exits = 1072,

    /// Common server error. Error number: 1073, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_blob_used_as_key">ER_BLOB_USED_AS_KEY</a>.
    er_blob_used_as_key = 1073,

    /// Common server error. Error number: 1074, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_fieldlength">ER_TOO_BIG_FIELDLENGTH</a>.
    er_too_big_fieldlength = 1074,

    /// Common server error. Error number: 1075, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_auto_key">ER_WRONG_AUTO_KEY</a>.
    er_wrong_auto_key = 1075,

    /// Common server error. Error number: 1077, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_normal_shutdown">ER_NORMAL_SHUTDOWN</a>.
    er_normal_shutdown = 1077,

    /// Common server error. Error number: 1078, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_got_signal">ER_GOT_SIGNAL</a>.
    er_got_signal = 1078,

    /// Common server error. Error number: 1079, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_shutdown_complete">ER_SHUTDOWN_COMPLETE</a>.
    er_shutdown_complete = 1079,

    /// Common server error. Error number: 1080, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_forcing_close">ER_FORCING_CLOSE</a>.
    er_forcing_close = 1080,

    /// Common server error. Error number: 1081, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_ipsock_error">ER_IPSOCK_ERROR</a>.
    er_ipsock_error = 1081,

    /// Common server error. Error number: 1082, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_such_index">ER_NO_SUCH_INDEX</a>.
    er_no_such_index = 1082,

    /// Common server error. Error number: 1083, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_field_terminators">ER_WRONG_FIELD_TERMINATORS</a>.
    er_wrong_field_terminators = 1083,

    /// Common server error. Error number: 1084, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_blobs_and_no_terminated">ER_BLOBS_AND_NO_TERMINATED</a>.
    er_blobs_and_no_terminated = 1084,

    /// Common server error. Error number: 1085, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_textfile_not_readable">ER_TEXTFILE_NOT_READABLE</a>.
    er_textfile_not_readable = 1085,

    /// Common server error. Error number: 1086, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_file_exists_error">ER_FILE_EXISTS_ERROR</a>.
    er_file_exists_error = 1086,

    /// Common server error. Error number: 1087, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_load_info">ER_LOAD_INFO</a>.
    er_load_info = 1087,

    /// Common server error. Error number: 1088, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_info">ER_ALTER_INFO</a>.
    er_alter_info = 1088,

    /// Common server error. Error number: 1089, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_sub_key">ER_WRONG_SUB_KEY</a>.
    er_wrong_sub_key = 1089,

    /// Common server error. Error number: 1090, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_remove_all_fields">ER_CANT_REMOVE_ALL_FIELDS</a>.
    er_cant_remove_all_fields = 1090,

    /// Common server error. Error number: 1091, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_drop_field_or_key">ER_CANT_DROP_FIELD_OR_KEY</a>.
    er_cant_drop_field_or_key = 1091,

    /// Common server error. Error number: 1092, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_insert_info">ER_INSERT_INFO</a>.
    er_insert_info = 1092,

    /// Common server error. Error number: 1093, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_update_table_used">ER_UPDATE_TABLE_USED</a>.
    er_update_table_used = 1093,

    /// Common server error. Error number: 1094, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_such_thread">ER_NO_SUCH_THREAD</a>.
    er_no_such_thread = 1094,

    /// Common server error. Error number: 1095, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_kill_denied_error">ER_KILL_DENIED_ERROR</a>.
    er_kill_denied_error = 1095,

    /// Common server error. Error number: 1096, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_tables_used">ER_NO_TABLES_USED</a>.
    er_no_tables_used = 1096,

    /// Common server error. Error number: 1097, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_set">ER_TOO_BIG_SET</a>.
    er_too_big_set = 1097,

    /// Common server error. Error number: 1098, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_unique_logfile">ER_NO_UNIQUE_LOGFILE</a>.
    er_no_unique_logfile = 1098,

    /// Common server error. Error number: 1099, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_not_locked_for_write">ER_TABLE_NOT_LOCKED_FOR_WRITE</a>.
    er_table_not_locked_for_write = 1099,

    /// Common server error. Error number: 1100, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_not_locked">ER_TABLE_NOT_LOCKED</a>.
    er_table_not_locked = 1100,

    /// Common server error. Error number: 1102, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_db_name">ER_WRONG_DB_NAME</a>.
    er_wrong_db_name = 1102,

    /// Common server error. Error number: 1103, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_table_name">ER_WRONG_TABLE_NAME</a>.
    er_wrong_table_name = 1103,

    /// Common server error. Error number: 1104, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_select">ER_TOO_BIG_SELECT</a>.
    er_too_big_select = 1104,

    /// Common server error. Error number: 1105, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_error">ER_UNKNOWN_ERROR</a>.
    er_unknown_error = 1105,

    /// Common server error. Error number: 1106, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_procedure">ER_UNKNOWN_PROCEDURE</a>.
    er_unknown_procedure = 1106,

    /// Common server error. Error number: 1107, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_paramcount_to_procedure">ER_WRONG_PARAMCOUNT_TO_PROCEDURE</a>.
    er_wrong_paramcount_to_procedure = 1107,

    /// Common server error. Error number: 1108, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_parameters_to_procedure">ER_WRONG_PARAMETERS_TO_PROCEDURE</a>.
    er_wrong_parameters_to_procedure = 1108,

    /// Common server error. Error number: 1109, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_table">ER_UNKNOWN_TABLE</a>.
    er_unknown_table = 1109,

    /// Common server error. Error number: 1110, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_field_specified_twice">ER_FIELD_SPECIFIED_TWICE</a>.
    er_field_specified_twice = 1110,

    /// Common server error. Error number: 1111, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_invalid_group_func_use">ER_INVALID_GROUP_FUNC_USE</a>.
    er_invalid_group_func_use = 1111,

    /// Common server error. Error number: 1112, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unsupported_extension">ER_UNSUPPORTED_EXTENSION</a>.
    er_unsupported_extension = 1112,

    /// Common server error. Error number: 1113, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_must_have_columns">ER_TABLE_MUST_HAVE_COLUMNS</a>.
    er_table_must_have_columns = 1113,

    /// Common server error. Error number: 1114, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_record_file_full">ER_RECORD_FILE_FULL</a>.
    er_record_file_full = 1114,

    /// Common server error. Error number: 1115, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_character_set">ER_UNKNOWN_CHARACTER_SET</a>.
    er_unknown_character_set = 1115,

    /// Common server error. Error number: 1116, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_tables">ER_TOO_MANY_TABLES</a>.
    er_too_many_tables = 1116,

    /// Common server error. Error number: 1117, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_fields">ER_TOO_MANY_FIELDS</a>.
    er_too_many_fields = 1117,

    /// Common server error. Error number: 1118, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_rowsize">ER_TOO_BIG_ROWSIZE</a>.
    er_too_big_rowsize = 1118,

    /// Common server error. Error number: 1119, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stack_overrun">ER_STACK_OVERRUN</a>.
    er_stack_overrun = 1119,

    /// Common server error. Error number: 1121, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_null_column_in_index">ER_NULL_COLUMN_IN_INDEX</a>.
    er_null_column_in_index = 1121,

    /// Common server error. Error number: 1122, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_find_udf">ER_CANT_FIND_UDF</a>.
    er_cant_find_udf = 1122,

    /// Common server error. Error number: 1123, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_initialize_udf">ER_CANT_INITIALIZE_UDF</a>.
    er_cant_initialize_udf = 1123,

    /// Common server error. Error number: 1124, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_udf_no_paths">ER_UDF_NO_PATHS</a>.
    er_udf_no_paths = 1124,

    /// Common server error. Error number: 1125, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_udf_exists">ER_UDF_EXISTS</a>.
    er_udf_exists = 1125,

    /// Common server error. Error number: 1126, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_open_library">ER_CANT_OPEN_LIBRARY</a>.
    er_cant_open_library = 1126,

    /// Common server error. Error number: 1127, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_find_dl_entry">ER_CANT_FIND_DL_ENTRY</a>.
    er_cant_find_dl_entry = 1127,

    /// Common server error. Error number: 1128, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_function_not_defined">ER_FUNCTION_NOT_DEFINED</a>.
    er_function_not_defined = 1128,

    /// Common server error. Error number: 1129, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_host_is_blocked">ER_HOST_IS_BLOCKED</a>.
    er_host_is_blocked = 1129,

    /// Common server error. Error number: 1130, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_host_not_privileged">ER_HOST_NOT_PRIVILEGED</a>.
    er_host_not_privileged = 1130,

    /// Common server error. Error number: 1131, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_password_anonymous_user">ER_PASSWORD_ANONYMOUS_USER</a>.
    er_password_anonymous_user = 1131,

    /// Common server error. Error number: 1132, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_password_not_allowed">ER_PASSWORD_NOT_ALLOWED</a>.
    er_password_not_allowed = 1132,

    /// Common server error. Error number: 1133, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_password_no_match">ER_PASSWORD_NO_MATCH</a>.
    er_password_no_match = 1133,

    /// Common server error. Error number: 1134, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_update_info">ER_UPDATE_INFO</a>.
    er_update_info = 1134,

    /// Common server error. Error number: 1135, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_thread">ER_CANT_CREATE_THREAD</a>.
    er_cant_create_thread = 1135,

    /// Common server error. Error number: 1136, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_value_count_on_row">ER_WRONG_VALUE_COUNT_ON_ROW</a>.
    er_wrong_value_count_on_row = 1136,

    /// Common server error. Error number: 1137, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_reopen_table">ER_CANT_REOPEN_TABLE</a>.
    er_cant_reopen_table = 1137,

    /// Common server error. Error number: 1138, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_invalid_use_of_null">ER_INVALID_USE_OF_NULL</a>.
    er_invalid_use_of_null = 1138,

    /// Common server error. Error number: 1139, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_regexp_error">ER_REGEXP_ERROR</a>.
    er_regexp_error = 1139,

    /// Common server error. Error number: 1140, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mix_of_group_func_and_fields">ER_MIX_OF_GROUP_FUNC_AND_FIELDS</a>.
    er_mix_of_group_func_and_fields = 1140,

    /// Common server error. Error number: 1141, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_nonexisting_grant">ER_NONEXISTING_GRANT</a>.
    er_nonexisting_grant = 1141,

    /// Common server error. Error number: 1142, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_tableaccess_denied_error">ER_TABLEACCESS_DENIED_ERROR</a>.
    er_tableaccess_denied_error = 1142,

    /// Common server error. Error number: 1143, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_columnaccess_denied_error">ER_COLUMNACCESS_DENIED_ERROR</a>.
    er_columnaccess_denied_error = 1143,

    /// Common server error. Error number: 1144, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_illegal_grant_for_table">ER_ILLEGAL_GRANT_FOR_TABLE</a>.
    er_illegal_grant_for_table = 1144,

    /// Common server error. Error number: 1145, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_grant_wrong_host_or_user">ER_GRANT_WRONG_HOST_OR_USER</a>.
    er_grant_wrong_host_or_user = 1145,

    /// Common server error. Error number: 1146, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_such_table">ER_NO_SUCH_TABLE</a>.
    er_no_such_table = 1146,

    /// Common server error. Error number: 1147, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_nonexisting_table_grant">ER_NONEXISTING_TABLE_GRANT</a>.
    er_nonexisting_table_grant = 1147,

    /// Common server error. Error number: 1148, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_not_allowed_command">ER_NOT_ALLOWED_COMMAND</a>.
    er_not_allowed_command = 1148,

    /// Common server error. Error number: 1149, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_syntax_error">ER_SYNTAX_ERROR</a>.
    er_syntax_error = 1149,

    /// Common server error. Error number: 1152, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_aborting_connection">ER_ABORTING_CONNECTION</a>.
    er_aborting_connection = 1152,

    /// Common server error. Error number: 1153, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_packet_too_large">ER_NET_PACKET_TOO_LARGE</a>.
    er_net_packet_too_large = 1153,

    /// Common server error. Error number: 1154, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_read_error_from_pipe">ER_NET_READ_ERROR_FROM_PIPE</a>.
    er_net_read_error_from_pipe = 1154,

    /// Common server error. Error number: 1155, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_fcntl_error">ER_NET_FCNTL_ERROR</a>.
    er_net_fcntl_error = 1155,

    /// Common server error. Error number: 1156, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_packets_out_of_order">ER_NET_PACKETS_OUT_OF_ORDER</a>.
    er_net_packets_out_of_order = 1156,

    /// Common server error. Error number: 1157, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_uncompress_error">ER_NET_UNCOMPRESS_ERROR</a>.
    er_net_uncompress_error = 1157,

    /// Common server error. Error number: 1158, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_read_error">ER_NET_READ_ERROR</a>.
    er_net_read_error = 1158,

    /// Common server error. Error number: 1159, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_read_interrupted">ER_NET_READ_INTERRUPTED</a>.
    er_net_read_interrupted = 1159,

    /// Common server error. Error number: 1160, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_error_on_write">ER_NET_ERROR_ON_WRITE</a>.
    er_net_error_on_write = 1160,

    /// Common server error. Error number: 1161, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_net_write_interrupted">ER_NET_WRITE_INTERRUPTED</a>.
    er_net_write_interrupted = 1161,

    /// Common server error. Error number: 1162, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_string">ER_TOO_LONG_STRING</a>.
    er_too_long_string = 1162,

    /// Common server error. Error number: 1163, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_cant_handle_blob">ER_TABLE_CANT_HANDLE_BLOB</a>.
    er_table_cant_handle_blob = 1163,

    /// Common server error. Error number: 1164, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_cant_handle_auto_increment">ER_TABLE_CANT_HANDLE_AUTO_INCREMENT</a>.
    er_table_cant_handle_auto_increment = 1164,

    /// Common server error. Error number: 1166, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_column_name">ER_WRONG_COLUMN_NAME</a>.
    er_wrong_column_name = 1166,

    /// Common server error. Error number: 1167, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_key_column">ER_WRONG_KEY_COLUMN</a>.
    er_wrong_key_column = 1167,

    /// Common server error. Error number: 1168, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_mrg_table">ER_WRONG_MRG_TABLE</a>.
    er_wrong_mrg_table = 1168,

    /// Common server error. Error number: 1169, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_unique">ER_DUP_UNIQUE</a>.
    er_dup_unique = 1169,

    /// Common server error. Error number: 1170, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_blob_key_without_length">ER_BLOB_KEY_WITHOUT_LENGTH</a>.
    er_blob_key_without_length = 1170,

    /// Common server error. Error number: 1171, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_primary_cant_have_null">ER_PRIMARY_CANT_HAVE_NULL</a>.
    er_primary_cant_have_null = 1171,

    /// Common server error. Error number: 1172, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_rows">ER_TOO_MANY_ROWS</a>.
    er_too_many_rows = 1172,

    /// Common server error. Error number: 1173, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_requires_primary_key">ER_REQUIRES_PRIMARY_KEY</a>.
    er_requires_primary_key = 1173,

    /// Common server error. Error number: 1174, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_raid_compiled">ER_NO_RAID_COMPILED</a>.
    er_no_raid_compiled = 1174,

    /// Common server error. Error number: 1175, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_update_without_key_in_safe_mode">ER_UPDATE_WITHOUT_KEY_IN_SAFE_MODE</a>.
    er_update_without_key_in_safe_mode = 1175,

    /// Common server error. Error number: 1177, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_check_no_such_table">ER_CHECK_NO_SUCH_TABLE</a>.
    er_check_no_such_table = 1177,

    /// Common server error. Error number: 1178, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_check_not_implemented">ER_CHECK_NOT_IMPLEMENTED</a>.
    er_check_not_implemented = 1178,

    /// Common server error. Error number: 1179, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_do_this_during_an_transaction">ER_CANT_DO_THIS_DURING_AN_TRANSACTION</a>.
    er_cant_do_this_during_an_transaction = 1179,

    /// Common server error. Error number: 1180, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_during_commit">ER_ERROR_DURING_COMMIT</a>.
    er_error_during_commit = 1180,

    /// Common server error. Error number: 1181, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_during_rollback">ER_ERROR_DURING_ROLLBACK</a>.
    er_error_during_rollback = 1181,

    /// Common server error. Error number: 1182, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_during_flush_logs">ER_ERROR_DURING_FLUSH_LOGS</a>.
    er_error_during_flush_logs = 1182,

    /// Common server error. Error number: 1183, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_during_checkpoint">ER_ERROR_DURING_CHECKPOINT</a>.
    er_error_during_checkpoint = 1183,

    /// Common server error. Error number: 1184, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_new_aborting_connection">ER_NEW_ABORTING_CONNECTION</a>.
    er_new_aborting_connection = 1184,

    /// Common server error. Error number: 1186, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_flush_master_binlog_closed">ER_FLUSH_MASTER_BINLOG_CLOSED</a>.
    er_flush_master_binlog_closed = 1186,

    /// Common server error. Error number: 1187, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_index_rebuild">ER_INDEX_REBUILD</a>.
    er_index_rebuild = 1187,

    /// Common server error. Error number: 1188, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_master">ER_MASTER</a>.
    er_master = 1188,

    /// Common server error. Error number: 1189, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_master_net_read">ER_MASTER_NET_READ</a>.
    er_master_net_read = 1189,

    /// Common server error. Error number: 1190, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_master_net_write">ER_MASTER_NET_WRITE</a>.
    er_master_net_write = 1190,

    /// Common server error. Error number: 1191, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_ft_matching_key_not_found">ER_FT_MATCHING_KEY_NOT_FOUND</a>.
    er_ft_matching_key_not_found = 1191,

    /// Common server error. Error number: 1192, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_lock_or_active_transaction">ER_LOCK_OR_ACTIVE_TRANSACTION</a>.
    er_lock_or_active_transaction = 1192,

    /// Common server error. Error number: 1193, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_system_variable">ER_UNKNOWN_SYSTEM_VARIABLE</a>.
    er_unknown_system_variable = 1193,

    /// Common server error. Error number: 1194, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_crashed_on_usage">ER_CRASHED_ON_USAGE</a>.
    er_crashed_on_usage = 1194,

    /// Common server error. Error number: 1195, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_crashed_on_repair">ER_CRASHED_ON_REPAIR</a>.
    er_crashed_on_repair = 1195,

    /// Common server error. Error number: 1196, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warning_not_complete_rollback">ER_WARNING_NOT_COMPLETE_ROLLBACK</a>.
    er_warning_not_complete_rollback = 1196,

    /// Common server error. Error number: 1197, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trans_cache_full">ER_TRANS_CACHE_FULL</a>.
    er_trans_cache_full = 1197,

    /// Common server error. Error number: 1198, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_must_stop">ER_SLAVE_MUST_STOP</a>.
    er_slave_must_stop = 1198,

    /// Common server error. Error number: 1199, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_not_running">ER_SLAVE_NOT_RUNNING</a>.
    er_slave_not_running = 1199,

    /// Common server error. Error number: 1200, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_slave">ER_BAD_SLAVE</a>.
    er_bad_slave = 1200,

    /// Common server error. Error number: 1201, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_master_info">ER_MASTER_INFO</a>.
    er_master_info = 1201,

    /// Common server error. Error number: 1202, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_thread">ER_SLAVE_THREAD</a>.
    er_slave_thread = 1202,

    /// Common server error. Error number: 1203, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_user_connections">ER_TOO_MANY_USER_CONNECTIONS</a>.
    er_too_many_user_connections = 1203,

    /// Common server error. Error number: 1204, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_set_constants_only">ER_SET_CONSTANTS_ONLY</a>.
    er_set_constants_only = 1204,

    /// Common server error. Error number: 1205, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_lock_wait_timeout">ER_LOCK_WAIT_TIMEOUT</a>.
    er_lock_wait_timeout = 1205,

    /// Common server error. Error number: 1206, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_lock_table_full">ER_LOCK_TABLE_FULL</a>.
    er_lock_table_full = 1206,

    /// Common server error. Error number: 1207, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_read_only_transaction">ER_READ_ONLY_TRANSACTION</a>.
    er_read_only_transaction = 1207,

    /// Common server error. Error number: 1208, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_drop_db_with_read_lock">ER_DROP_DB_WITH_READ_LOCK</a>.
    er_drop_db_with_read_lock = 1208,

    /// Common server error. Error number: 1209, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_create_db_with_read_lock">ER_CREATE_DB_WITH_READ_LOCK</a>.
    er_create_db_with_read_lock = 1209,

    /// Common server error. Error number: 1210, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_arguments">ER_WRONG_ARGUMENTS</a>.
    er_wrong_arguments = 1210,

    /// Common server error. Error number: 1211, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_permission_to_create_user">ER_NO_PERMISSION_TO_CREATE_USER</a>.
    er_no_permission_to_create_user = 1211,

    /// Common server error. Error number: 1212, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_union_tables_in_different_dir">ER_UNION_TABLES_IN_DIFFERENT_DIR</a>.
    er_union_tables_in_different_dir = 1212,

    /// Common server error. Error number: 1213, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_lock_deadlock">ER_LOCK_DEADLOCK</a>.
    er_lock_deadlock = 1213,

    /// Common server error. Error number: 1214, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_cant_handle_ft">ER_TABLE_CANT_HANDLE_FT</a>.
    er_table_cant_handle_ft = 1214,

    /// Common server error. Error number: 1215, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cannot_add_foreign">ER_CANNOT_ADD_FOREIGN</a>.
    er_cannot_add_foreign = 1215,

    /// Common server error. Error number: 1216, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_referenced_row">ER_NO_REFERENCED_ROW</a>.
    er_no_referenced_row = 1216,

    /// Common server error. Error number: 1217, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_row_is_referenced">ER_ROW_IS_REFERENCED</a>.
    er_row_is_referenced = 1217,

    /// Common server error. Error number: 1218, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_connect_to_master">ER_CONNECT_TO_MASTER</a>.
    er_connect_to_master = 1218,

    /// Common server error. Error number: 1219, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_query_on_master">ER_QUERY_ON_MASTER</a>.
    er_query_on_master = 1219,

    /// Common server error. Error number: 1220, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_when_executing_command">ER_ERROR_WHEN_EXECUTING_COMMAND</a>.
    er_error_when_executing_command = 1220,

    /// Common server error. Error number: 1221, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_usage">ER_WRONG_USAGE</a>.
    er_wrong_usage = 1221,

    /// Common server error. Error number: 1222, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_number_of_columns_in_select">ER_WRONG_NUMBER_OF_COLUMNS_IN_SELECT</a>.
    er_wrong_number_of_columns_in_select = 1222,

    /// Common server error. Error number: 1223, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_update_with_readlock">ER_CANT_UPDATE_WITH_READLOCK</a>.
    er_cant_update_with_readlock = 1223,

    /// Common server error. Error number: 1224, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mixing_not_allowed">ER_MIXING_NOT_ALLOWED</a>.
    er_mixing_not_allowed = 1224,

    /// Common server error. Error number: 1225, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_argument">ER_DUP_ARGUMENT</a>.
    er_dup_argument = 1225,

    /// Common server error. Error number: 1226, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_user_limit_reached">ER_USER_LIMIT_REACHED</a>.
    er_user_limit_reached = 1226,

    /// Common server error. Error number: 1227, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_specific_access_denied_error">ER_SPECIFIC_ACCESS_DENIED_ERROR</a>.
    er_specific_access_denied_error = 1227,

    /// Common server error. Error number: 1228, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_local_variable">ER_LOCAL_VARIABLE</a>.
    er_local_variable = 1228,

    /// Common server error. Error number: 1229, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_global_variable">ER_GLOBAL_VARIABLE</a>.
    er_global_variable = 1229,

    /// Common server error. Error number: 1230, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_default">ER_NO_DEFAULT</a>.
    er_no_default = 1230,

    /// Common server error. Error number: 1231, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_value_for_var">ER_WRONG_VALUE_FOR_VAR</a>.
    er_wrong_value_for_var = 1231,

    /// Common server error. Error number: 1232, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_type_for_var">ER_WRONG_TYPE_FOR_VAR</a>.
    er_wrong_type_for_var = 1232,

    /// Common server error. Error number: 1233, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_var_cant_be_read">ER_VAR_CANT_BE_READ</a>.
    er_var_cant_be_read = 1233,

    /// Common server error. Error number: 1234, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_use_option_here">ER_CANT_USE_OPTION_HERE</a>.
    er_cant_use_option_here = 1234,

    /// Common server error. Error number: 1235, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_not_supported_yet">ER_NOT_SUPPORTED_YET</a>.
    er_not_supported_yet = 1235,

    /// Common server error. Error number: 1236, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_master_fatal_error_reading_binlog">ER_MASTER_FATAL_ERROR_READING_BINLOG</a>.
    er_master_fatal_error_reading_binlog = 1236,

    /// Common server error. Error number: 1237, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_ignored_table">ER_SLAVE_IGNORED_TABLE</a>.
    er_slave_ignored_table = 1237,

    /// Common server error. Error number: 1238, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_incorrect_global_local_var">ER_INCORRECT_GLOBAL_LOCAL_VAR</a>.
    er_incorrect_global_local_var = 1238,

    /// Common server error. Error number: 1239, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_fk_def">ER_WRONG_FK_DEF</a>.
    er_wrong_fk_def = 1239,

    /// Common server error. Error number: 1240, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_key_ref_do_not_match_table_ref">ER_KEY_REF_DO_NOT_MATCH_TABLE_REF</a>.
    er_key_ref_do_not_match_table_ref = 1240,

    /// Common server error. Error number: 1241, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_operand_columns">ER_OPERAND_COLUMNS</a>.
    er_operand_columns = 1241,

    /// Common server error. Error number: 1242, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_subquery_no_1_row">ER_SUBQUERY_NO_1_ROW</a>.
    er_subquery_no_1_row = 1242,

    /// Common server error. Error number: 1243, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_stmt_handler">ER_UNKNOWN_STMT_HANDLER</a>.
    er_unknown_stmt_handler = 1243,

    /// Common server error. Error number: 1244, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_corrupt_help_db">ER_CORRUPT_HELP_DB</a>.
    er_corrupt_help_db = 1244,

    /// Common server error. Error number: 1245, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cyclic_reference">ER_CYCLIC_REFERENCE</a>.
    er_cyclic_reference = 1245,

    /// Common server error. Error number: 1246, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_auto_convert">ER_AUTO_CONVERT</a>.
    er_auto_convert = 1246,

    /// Common server error. Error number: 1247, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_illegal_reference">ER_ILLEGAL_REFERENCE</a>.
    er_illegal_reference = 1247,

    /// Common server error. Error number: 1248, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_derived_must_have_alias">ER_DERIVED_MUST_HAVE_ALIAS</a>.
    er_derived_must_have_alias = 1248,

    /// Common server error. Error number: 1249, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_select_reduced">ER_SELECT_REDUCED</a>.
    er_select_reduced = 1249,

    /// Common server error. Error number: 1250, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_tablename_not_allowed_here">ER_TABLENAME_NOT_ALLOWED_HERE</a>.
    er_tablename_not_allowed_here = 1250,

    /// Common server error. Error number: 1251, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_not_supported_auth_mode">ER_NOT_SUPPORTED_AUTH_MODE</a>.
    er_not_supported_auth_mode = 1251,

    /// Common server error. Error number: 1252, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_spatial_cant_have_null">ER_SPATIAL_CANT_HAVE_NULL</a>.
    er_spatial_cant_have_null = 1252,

    /// Common server error. Error number: 1253, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_collation_charset_mismatch">ER_COLLATION_CHARSET_MISMATCH</a>.
    er_collation_charset_mismatch = 1253,

    /// Common server error. Error number: 1254, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_was_running">ER_SLAVE_WAS_RUNNING</a>.
    er_slave_was_running = 1254,

    /// Common server error. Error number: 1255, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_was_not_running">ER_SLAVE_WAS_NOT_RUNNING</a>.
    er_slave_was_not_running = 1255,

    /// Common server error. Error number: 1256, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_for_uncompress">ER_TOO_BIG_FOR_UNCOMPRESS</a>.
    er_too_big_for_uncompress = 1256,

    /// Common server error. Error number: 1257, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_zlib_z_mem_error">ER_ZLIB_Z_MEM_ERROR</a>.
    er_zlib_z_mem_error = 1257,

    /// Common server error. Error number: 1258, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_zlib_z_buf_error">ER_ZLIB_Z_BUF_ERROR</a>.
    er_zlib_z_buf_error = 1258,

    /// Common server error. Error number: 1259, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_zlib_z_data_error">ER_ZLIB_Z_DATA_ERROR</a>.
    er_zlib_z_data_error = 1259,

    /// Common server error. Error number: 1260, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cut_value_group_concat">ER_CUT_VALUE_GROUP_CONCAT</a>.
    er_cut_value_group_concat = 1260,

    /// Common server error. Error number: 1261, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_too_few_records">ER_WARN_TOO_FEW_RECORDS</a>.
    er_warn_too_few_records = 1261,

    /// Common server error. Error number: 1262, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_too_many_records">ER_WARN_TOO_MANY_RECORDS</a>.
    er_warn_too_many_records = 1262,

    /// Common server error. Error number: 1263, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_null_to_notnull">ER_WARN_NULL_TO_NOTNULL</a>.
    er_warn_null_to_notnull = 1263,

    /// Common server error. Error number: 1264, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_data_out_of_range">ER_WARN_DATA_OUT_OF_RANGE</a>.
    er_warn_data_out_of_range = 1264,

    /// Common server error. Error number: 1265, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_data_truncated">WARN_DATA_TRUNCATED</a>.
    warn_data_truncated = 1265,

    /// Common server error. Error number: 1266, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_using_other_handler">ER_WARN_USING_OTHER_HANDLER</a>.
    er_warn_using_other_handler = 1266,

    /// Common server error. Error number: 1267, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_aggregate_2collations">ER_CANT_AGGREGATE_2COLLATIONS</a>.
    er_cant_aggregate_2collations = 1267,

    /// Common server error. Error number: 1268, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_drop_user">ER_DROP_USER</a>.
    er_drop_user = 1268,

    /// Common server error. Error number: 1269, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_revoke_grants">ER_REVOKE_GRANTS</a>.
    er_revoke_grants = 1269,

    /// Common server error. Error number: 1270, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_aggregate_3collations">ER_CANT_AGGREGATE_3COLLATIONS</a>.
    er_cant_aggregate_3collations = 1270,

    /// Common server error. Error number: 1271, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_aggregate_ncollations">ER_CANT_AGGREGATE_NCOLLATIONS</a>.
    er_cant_aggregate_ncollations = 1271,

    /// Common server error. Error number: 1272, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_variable_is_not_struct">ER_VARIABLE_IS_NOT_STRUCT</a>.
    er_variable_is_not_struct = 1272,

    /// Common server error. Error number: 1273, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_collation">ER_UNKNOWN_COLLATION</a>.
    er_unknown_collation = 1273,

    /// Common server error. Error number: 1274, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_ignored_ssl_params">ER_SLAVE_IGNORED_SSL_PARAMS</a>.
    er_slave_ignored_ssl_params = 1274,

    /// Common server error. Error number: 1275, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_server_is_in_secure_auth_mode">ER_SERVER_IS_IN_SECURE_AUTH_MODE</a>.
    er_server_is_in_secure_auth_mode = 1275,

    /// Common server error. Error number: 1276, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_field_resolved">ER_WARN_FIELD_RESOLVED</a>.
    er_warn_field_resolved = 1276,

    /// Common server error. Error number: 1277, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_slave_until_cond">ER_BAD_SLAVE_UNTIL_COND</a>.
    er_bad_slave_until_cond = 1277,

    /// Common server error. Error number: 1278, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_missing_skip_slave">ER_MISSING_SKIP_SLAVE</a>.
    er_missing_skip_slave = 1278,

    /// Common server error. Error number: 1279, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_until_cond_ignored">ER_UNTIL_COND_IGNORED</a>.
    er_until_cond_ignored = 1279,

    /// Common server error. Error number: 1280, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_name_for_index">ER_WRONG_NAME_FOR_INDEX</a>.
    er_wrong_name_for_index = 1280,

    /// Common server error. Error number: 1281, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_name_for_catalog">ER_WRONG_NAME_FOR_CATALOG</a>.
    er_wrong_name_for_catalog = 1281,

    /// Common server error. Error number: 1282, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_qc_resize">ER_WARN_QC_RESIZE</a>.
    er_warn_qc_resize = 1282,

    /// Common server error. Error number: 1283, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_ft_column">ER_BAD_FT_COLUMN</a>.
    er_bad_ft_column = 1283,

    /// Common server error. Error number: 1284, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_key_cache">ER_UNKNOWN_KEY_CACHE</a>.
    er_unknown_key_cache = 1284,

    /// Common server error. Error number: 1285, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_hostname_wont_work">ER_WARN_HOSTNAME_WONT_WORK</a>.
    er_warn_hostname_wont_work = 1285,

    /// Common server error. Error number: 1286, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_storage_engine">ER_UNKNOWN_STORAGE_ENGINE</a>.
    er_unknown_storage_engine = 1286,

    /// Common server error. Error number: 1287, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_deprecated_syntax">ER_WARN_DEPRECATED_SYNTAX</a>.
    er_warn_deprecated_syntax = 1287,

    /// Common server error. Error number: 1288, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_non_updatable_table">ER_NON_UPDATABLE_TABLE</a>.
    er_non_updatable_table = 1288,

    /// Common server error. Error number: 1289, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_feature_disabled">ER_FEATURE_DISABLED</a>.
    er_feature_disabled = 1289,

    /// Common server error. Error number: 1290, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_option_prevents_statement">ER_OPTION_PREVENTS_STATEMENT</a>.
    er_option_prevents_statement = 1290,

    /// Common server error. Error number: 1291, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_duplicated_value_in_type">ER_DUPLICATED_VALUE_IN_TYPE</a>.
    er_duplicated_value_in_type = 1291,

    /// Common server error. Error number: 1292, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_truncated_wrong_value">ER_TRUNCATED_WRONG_VALUE</a>.
    er_truncated_wrong_value = 1292,

    /// Common server error. Error number: 1293, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_much_auto_timestamp_cols">ER_TOO_MUCH_AUTO_TIMESTAMP_COLS</a>.
    er_too_much_auto_timestamp_cols = 1293,

    /// Common server error. Error number: 1294, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_invalid_on_update">ER_INVALID_ON_UPDATE</a>.
    er_invalid_on_update = 1294,

    /// Common server error. Error number: 1295, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unsupported_ps">ER_UNSUPPORTED_PS</a>.
    er_unsupported_ps = 1295,

    /// Common server error. Error number: 1296, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_get_errmsg">ER_GET_ERRMSG</a>.
    er_get_errmsg = 1296,

    /// Common server error. Error number: 1297, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_get_temporary_errmsg">ER_GET_TEMPORARY_ERRMSG</a>.
    er_get_temporary_errmsg = 1297,

    /// Common server error. Error number: 1298, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_time_zone">ER_UNKNOWN_TIME_ZONE</a>.
    er_unknown_time_zone = 1298,

    /// Common server error. Error number: 1299, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_invalid_timestamp">ER_WARN_INVALID_TIMESTAMP</a>.
    er_warn_invalid_timestamp = 1299,

    /// Common server error. Error number: 1300, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_invalid_character_string">ER_INVALID_CHARACTER_STRING</a>.
    er_invalid_character_string = 1300,

    /// Common server error. Error number: 1301, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_allowed_packet_overflowed">ER_WARN_ALLOWED_PACKET_OVERFLOWED</a>.
    er_warn_allowed_packet_overflowed = 1301,

    /// Common server error. Error number: 1302, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_conflicting_declarations">ER_CONFLICTING_DECLARATIONS</a>.
    er_conflicting_declarations = 1302,

    /// Common server error. Error number: 1303, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_no_recursive_create">ER_SP_NO_RECURSIVE_CREATE</a>.
    er_sp_no_recursive_create = 1303,

    /// Common server error. Error number: 1304, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_already_exists">ER_SP_ALREADY_EXISTS</a>.
    er_sp_already_exists = 1304,

    /// Common server error. Error number: 1305, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_does_not_exist">ER_SP_DOES_NOT_EXIST</a>.
    er_sp_does_not_exist = 1305,

    /// Common server error. Error number: 1306, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_drop_failed">ER_SP_DROP_FAILED</a>.
    er_sp_drop_failed = 1306,

    /// Common server error. Error number: 1307, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_store_failed">ER_SP_STORE_FAILED</a>.
    er_sp_store_failed = 1307,

    /// Common server error. Error number: 1308, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_lilabel_mismatch">ER_SP_LILABEL_MISMATCH</a>.
    er_sp_lilabel_mismatch = 1308,

    /// Common server error. Error number: 1309, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_label_redefine">ER_SP_LABEL_REDEFINE</a>.
    er_sp_label_redefine = 1309,

    /// Common server error. Error number: 1310, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_label_mismatch">ER_SP_LABEL_MISMATCH</a>.
    er_sp_label_mismatch = 1310,

    /// Common server error. Error number: 1311, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_uninit_var">ER_SP_UNINIT_VAR</a>.
    er_sp_uninit_var = 1311,

    /// Common server error. Error number: 1312, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_badselect">ER_SP_BADSELECT</a>.
    er_sp_badselect = 1312,

    /// Common server error. Error number: 1313, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_badreturn">ER_SP_BADRETURN</a>.
    er_sp_badreturn = 1313,

    /// Common server error. Error number: 1314, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_badstatement">ER_SP_BADSTATEMENT</a>.
    er_sp_badstatement = 1314,

    /// Common server error. Error number: 1315, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_update_log_deprecated_ignored">ER_UPDATE_LOG_DEPRECATED_IGNORED</a>.
    er_update_log_deprecated_ignored = 1315,

    /// Common server error. Error number: 1316, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_update_log_deprecated_translated">ER_UPDATE_LOG_DEPRECATED_TRANSLATED</a>.
    er_update_log_deprecated_translated = 1316,

    /// Common server error. Error number: 1317, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_query_interrupted">ER_QUERY_INTERRUPTED</a>.
    er_query_interrupted = 1317,

    /// Common server error. Error number: 1318, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_wrong_no_of_args">ER_SP_WRONG_NO_OF_ARGS</a>.
    er_sp_wrong_no_of_args = 1318,

    /// Common server error. Error number: 1319, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_cond_mismatch">ER_SP_COND_MISMATCH</a>.
    er_sp_cond_mismatch = 1319,

    /// Common server error. Error number: 1320, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_noreturn">ER_SP_NORETURN</a>.
    er_sp_noreturn = 1320,

    /// Common server error. Error number: 1321, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_noreturnend">ER_SP_NORETURNEND</a>.
    er_sp_noreturnend = 1321,

    /// Common server error. Error number: 1322, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_bad_cursor_query">ER_SP_BAD_CURSOR_QUERY</a>.
    er_sp_bad_cursor_query = 1322,

    /// Common server error. Error number: 1323, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_bad_cursor_select">ER_SP_BAD_CURSOR_SELECT</a>.
    er_sp_bad_cursor_select = 1323,

    /// Common server error. Error number: 1324, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_cursor_mismatch">ER_SP_CURSOR_MISMATCH</a>.
    er_sp_cursor_mismatch = 1324,

    /// Common server error. Error number: 1325, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_cursor_already_open">ER_SP_CURSOR_ALREADY_OPEN</a>.
    er_sp_cursor_already_open = 1325,

    /// Common server error. Error number: 1326, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_cursor_not_open">ER_SP_CURSOR_NOT_OPEN</a>.
    er_sp_cursor_not_open = 1326,

    /// Common server error. Error number: 1327, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_undeclared_var">ER_SP_UNDECLARED_VAR</a>.
    er_sp_undeclared_var = 1327,

    /// Common server error. Error number: 1328, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_wrong_no_of_fetch_args">ER_SP_WRONG_NO_OF_FETCH_ARGS</a>.
    er_sp_wrong_no_of_fetch_args = 1328,

    /// Common server error. Error number: 1329, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_fetch_no_data">ER_SP_FETCH_NO_DATA</a>.
    er_sp_fetch_no_data = 1329,

    /// Common server error. Error number: 1330, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_dup_param">ER_SP_DUP_PARAM</a>.
    er_sp_dup_param = 1330,

    /// Common server error. Error number: 1331, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_dup_var">ER_SP_DUP_VAR</a>.
    er_sp_dup_var = 1331,

    /// Common server error. Error number: 1332, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_dup_cond">ER_SP_DUP_COND</a>.
    er_sp_dup_cond = 1332,

    /// Common server error. Error number: 1333, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_dup_curs">ER_SP_DUP_CURS</a>.
    er_sp_dup_curs = 1333,

    /// Common server error. Error number: 1334, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_cant_alter">ER_SP_CANT_ALTER</a>.
    er_sp_cant_alter = 1334,

    /// Common server error. Error number: 1335, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_subselect_nyi">ER_SP_SUBSELECT_NYI</a>.
    er_sp_subselect_nyi = 1335,

    /// Common server error. Error number: 1336, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stmt_not_allowed_in_sf_or_trg">ER_STMT_NOT_ALLOWED_IN_SF_OR_TRG</a>.
    er_stmt_not_allowed_in_sf_or_trg = 1336,

    /// Common server error. Error number: 1337, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_varcond_after_curshndlr">ER_SP_VARCOND_AFTER_CURSHNDLR</a>.
    er_sp_varcond_after_curshndlr = 1337,

    /// Common server error. Error number: 1338, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_cursor_after_handler">ER_SP_CURSOR_AFTER_HANDLER</a>.
    er_sp_cursor_after_handler = 1338,

    /// Common server error. Error number: 1339, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_case_not_found">ER_SP_CASE_NOT_FOUND</a>.
    er_sp_case_not_found = 1339,

    /// Common server error. Error number: 1340, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fparser_too_big_file">ER_FPARSER_TOO_BIG_FILE</a>.
    er_fparser_too_big_file = 1340,

    /// Common server error. Error number: 1341, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fparser_bad_header">ER_FPARSER_BAD_HEADER</a>.
    er_fparser_bad_header = 1341,

    /// Common server error. Error number: 1342, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fparser_eof_in_comment">ER_FPARSER_EOF_IN_COMMENT</a>.
    er_fparser_eof_in_comment = 1342,

    /// Common server error. Error number: 1343, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fparser_error_in_parameter">ER_FPARSER_ERROR_IN_PARAMETER</a>.
    er_fparser_error_in_parameter = 1343,

    /// Common server error. Error number: 1344, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fparser_eof_in_unknown_parameter">ER_FPARSER_EOF_IN_UNKNOWN_PARAMETER</a>.
    er_fparser_eof_in_unknown_parameter = 1344,

    /// Common server error. Error number: 1345, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_no_explain">ER_VIEW_NO_EXPLAIN</a>.
    er_view_no_explain = 1345,

    /// Common server error. Error number: 1346, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_frm_unknown_type">ER_FRM_UNKNOWN_TYPE</a>.
    er_frm_unknown_type = 1346,

    /// Common server error. Error number: 1347, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_object">ER_WRONG_OBJECT</a>.
    er_wrong_object = 1347,

    /// Common server error. Error number: 1348, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_nonupdateable_column">ER_NONUPDATEABLE_COLUMN</a>.
    er_nonupdateable_column = 1348,

    /// Common server error. Error number: 1350, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_select_clause">ER_VIEW_SELECT_CLAUSE</a>.
    er_view_select_clause = 1350,

    /// Common server error. Error number: 1351, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_select_variable">ER_VIEW_SELECT_VARIABLE</a>.
    er_view_select_variable = 1351,

    /// Common server error. Error number: 1352, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_select_tmptable">ER_VIEW_SELECT_TMPTABLE</a>.
    er_view_select_tmptable = 1352,

    /// Common server error. Error number: 1353, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_wrong_list">ER_VIEW_WRONG_LIST</a>.
    er_view_wrong_list = 1353,

    /// Common server error. Error number: 1354, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_view_merge">ER_WARN_VIEW_MERGE</a>.
    er_warn_view_merge = 1354,

    /// Common server error. Error number: 1355, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_view_without_key">ER_WARN_VIEW_WITHOUT_KEY</a>.
    er_warn_view_without_key = 1355,

    /// Common server error. Error number: 1356, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_invalid">ER_VIEW_INVALID</a>.
    er_view_invalid = 1356,

    /// Common server error. Error number: 1357, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_no_drop_sp">ER_SP_NO_DROP_SP</a>.
    er_sp_no_drop_sp = 1357,

    /// Common server error. Error number: 1358, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_goto_in_hndlr">ER_SP_GOTO_IN_HNDLR</a>.
    er_sp_goto_in_hndlr = 1358,

    /// Common server error. Error number: 1359, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_already_exists">ER_TRG_ALREADY_EXISTS</a>.
    er_trg_already_exists = 1359,

    /// Common server error. Error number: 1360, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_does_not_exist">ER_TRG_DOES_NOT_EXIST</a>.
    er_trg_does_not_exist = 1360,

    /// Common server error. Error number: 1361, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_on_view_or_temp_table">ER_TRG_ON_VIEW_OR_TEMP_TABLE</a>.
    er_trg_on_view_or_temp_table = 1361,

    /// Common server error. Error number: 1362, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_cant_change_row">ER_TRG_CANT_CHANGE_ROW</a>.
    er_trg_cant_change_row = 1362,

    /// Common server error. Error number: 1363, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_no_such_row_in_trg">ER_TRG_NO_SUCH_ROW_IN_TRG</a>.
    er_trg_no_such_row_in_trg = 1363,

    /// Common server error. Error number: 1364, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_default_for_field">ER_NO_DEFAULT_FOR_FIELD</a>.
    er_no_default_for_field = 1364,

    /// Common server error. Error number: 1365, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_division_by_zero">ER_DIVISION_BY_ZERO</a>.
    er_division_by_zero = 1365,

    /// Common server error. Error number: 1366, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_truncated_wrong_value_for_field">ER_TRUNCATED_WRONG_VALUE_FOR_FIELD</a>.
    er_truncated_wrong_value_for_field = 1366,

    /// Common server error. Error number: 1367, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_illegal_value_for_type">ER_ILLEGAL_VALUE_FOR_TYPE</a>.
    er_illegal_value_for_type = 1367,

    /// Common server error. Error number: 1368, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_nonupd_check">ER_VIEW_NONUPD_CHECK</a>.
    er_view_nonupd_check = 1368,

    /// Common server error. Error number: 1369, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_check_failed">ER_VIEW_CHECK_FAILED</a>.
    er_view_check_failed = 1369,

    /// Common server error. Error number: 1370, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_procaccess_denied_error">ER_PROCACCESS_DENIED_ERROR</a>.
    er_procaccess_denied_error = 1370,

    /// Common server error. Error number: 1371, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_relay_log_fail">ER_RELAY_LOG_FAIL</a>.
    er_relay_log_fail = 1371,

    /// Common server error. Error number: 1372, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_passwd_length">ER_PASSWD_LENGTH</a>.
    er_passwd_length = 1372,

    /// Common server error. Error number: 1373, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_target_binlog">ER_UNKNOWN_TARGET_BINLOG</a>.
    er_unknown_target_binlog = 1373,

    /// Common server error. Error number: 1374, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_io_err_log_index_read">ER_IO_ERR_LOG_INDEX_READ</a>.
    er_io_err_log_index_read = 1374,

    /// Common server error. Error number: 1375, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_purge_prohibited">ER_BINLOG_PURGE_PROHIBITED</a>.
    er_binlog_purge_prohibited = 1375,

    /// Common server error. Error number: 1376, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fseek_fail">ER_FSEEK_FAIL</a>.
    er_fseek_fail = 1376,

    /// Common server error. Error number: 1377, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_purge_fatal_err">ER_BINLOG_PURGE_FATAL_ERR</a>.
    er_binlog_purge_fatal_err = 1377,

    /// Common server error. Error number: 1378, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_log_in_use">ER_LOG_IN_USE</a>.
    er_log_in_use = 1378,

    /// Common server error. Error number: 1379, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_log_purge_unknown_err">ER_LOG_PURGE_UNKNOWN_ERR</a>.
    er_log_purge_unknown_err = 1379,

    /// Common server error. Error number: 1380, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_relay_log_init">ER_RELAY_LOG_INIT</a>.
    er_relay_log_init = 1380,

    /// Common server error. Error number: 1381, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_binary_logging">ER_NO_BINARY_LOGGING</a>.
    er_no_binary_logging = 1381,

    /// Common server error. Error number: 1382, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_reserved_syntax">ER_RESERVED_SYNTAX</a>.
    er_reserved_syntax = 1382,

    /// Common server error. Error number: 1383, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wsas_failed">ER_WSAS_FAILED</a>.
    er_wsas_failed = 1383,

    /// Common server error. Error number: 1384, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_diff_groups_proc">ER_DIFF_GROUPS_PROC</a>.
    er_diff_groups_proc = 1384,

    /// Common server error. Error number: 1385, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_group_for_proc">ER_NO_GROUP_FOR_PROC</a>.
    er_no_group_for_proc = 1385,

    /// Common server error. Error number: 1386, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_order_with_proc">ER_ORDER_WITH_PROC</a>.
    er_order_with_proc = 1386,

    /// Common server error. Error number: 1387, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_logging_prohibit_changing_of">ER_LOGGING_PROHIBIT_CHANGING_OF</a>.
    er_logging_prohibit_changing_of = 1387,

    /// Common server error. Error number: 1388, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_file_mapping">ER_NO_FILE_MAPPING</a>.
    er_no_file_mapping = 1388,

    /// Common server error. Error number: 1389, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_magic">ER_WRONG_MAGIC</a>.
    er_wrong_magic = 1389,

    /// Common server error. Error number: 1390, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_ps_many_param">ER_PS_MANY_PARAM</a>.
    er_ps_many_param = 1390,

    /// Common server error. Error number: 1391, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_key_part_0">ER_KEY_PART_0</a>.
    er_key_part_0 = 1391,

    /// Common server error. Error number: 1392, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_checksum">ER_VIEW_CHECKSUM</a>.
    er_view_checksum = 1392,

    /// Common server error. Error number: 1393, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_multiupdate">ER_VIEW_MULTIUPDATE</a>.
    er_view_multiupdate = 1393,

    /// Common server error. Error number: 1394, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_no_insert_field_list">ER_VIEW_NO_INSERT_FIELD_LIST</a>.
    er_view_no_insert_field_list = 1394,

    /// Common server error. Error number: 1395, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_delete_merge_view">ER_VIEW_DELETE_MERGE_VIEW</a>.
    er_view_delete_merge_view = 1395,

    /// Common server error. Error number: 1396, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cannot_user">ER_CANNOT_USER</a>.
    er_cannot_user = 1396,

    /// Common server error. Error number: 1397, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xaer_nota">ER_XAER_NOTA</a>.
    er_xaer_nota = 1397,

    /// Common server error. Error number: 1398, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xaer_inval">ER_XAER_INVAL</a>.
    er_xaer_inval = 1398,

    /// Common server error. Error number: 1399, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xaer_rmfail">ER_XAER_RMFAIL</a>.
    er_xaer_rmfail = 1399,

    /// Common server error. Error number: 1400, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xaer_outside">ER_XAER_OUTSIDE</a>.
    er_xaer_outside = 1400,

    /// Common server error. Error number: 1401, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xaer_rmerr">ER_XAER_RMERR</a>.
    er_xaer_rmerr = 1401,

    /// Common server error. Error number: 1402, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xa_rbrollback">ER_XA_RBROLLBACK</a>.
    er_xa_rbrollback = 1402,

    /// Common server error. Error number: 1403, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_nonexisting_proc_grant">ER_NONEXISTING_PROC_GRANT</a>.
    er_nonexisting_proc_grant = 1403,

    /// Common server error. Error number: 1404, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_proc_auto_grant_fail">ER_PROC_AUTO_GRANT_FAIL</a>.
    er_proc_auto_grant_fail = 1404,

    /// Common server error. Error number: 1405, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_proc_auto_revoke_fail">ER_PROC_AUTO_REVOKE_FAIL</a>.
    er_proc_auto_revoke_fail = 1405,

    /// Common server error. Error number: 1406, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_data_too_long">ER_DATA_TOO_LONG</a>.
    er_data_too_long = 1406,

    /// Common server error. Error number: 1407, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_bad_sqlstate">ER_SP_BAD_SQLSTATE</a>.
    er_sp_bad_sqlstate = 1407,

    /// Common server error. Error number: 1408, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_startup">ER_STARTUP</a>.
    er_startup = 1408,

    /// Common server error. Error number: 1409, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_load_from_fixed_size_rows_to_var">ER_LOAD_FROM_FIXED_SIZE_ROWS_TO_VAR</a>.
    er_load_from_fixed_size_rows_to_var = 1409,

    /// Common server error. Error number: 1410, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_user_with_grant">ER_CANT_CREATE_USER_WITH_GRANT</a>.
    er_cant_create_user_with_grant = 1410,

    /// Common server error. Error number: 1411, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_value_for_type">ER_WRONG_VALUE_FOR_TYPE</a>.
    er_wrong_value_for_type = 1411,

    /// Common server error. Error number: 1412, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_def_changed">ER_TABLE_DEF_CHANGED</a>.
    er_table_def_changed = 1412,

    /// Common server error. Error number: 1413, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_dup_handler">ER_SP_DUP_HANDLER</a>.
    er_sp_dup_handler = 1413,

    /// Common server error. Error number: 1414, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_not_var_arg">ER_SP_NOT_VAR_ARG</a>.
    er_sp_not_var_arg = 1414,

    /// Common server error. Error number: 1415, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_no_retset">ER_SP_NO_RETSET</a>.
    er_sp_no_retset = 1415,

    /// Common server error. Error number: 1416, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_geometry_object">ER_CANT_CREATE_GEOMETRY_OBJECT</a>.
    er_cant_create_geometry_object = 1416,

    /// Common server error. Error number: 1417, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_failed_routine_break_binlog">ER_FAILED_ROUTINE_BREAK_BINLOG</a>.
    er_failed_routine_break_binlog = 1417,

    /// Common server error. Error number: 1418, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_routine">ER_BINLOG_UNSAFE_ROUTINE</a>.
    er_binlog_unsafe_routine = 1418,

    /// Common server error. Error number: 1419, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_create_routine_need_super">ER_BINLOG_CREATE_ROUTINE_NEED_SUPER</a>.
    er_binlog_create_routine_need_super = 1419,

    /// Common server error. Error number: 1420, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_exec_stmt_with_open_cursor">ER_EXEC_STMT_WITH_OPEN_CURSOR</a>.
    er_exec_stmt_with_open_cursor = 1420,

    /// Common server error. Error number: 1421, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stmt_has_no_open_cursor">ER_STMT_HAS_NO_OPEN_CURSOR</a>.
    er_stmt_has_no_open_cursor = 1421,

    /// Common server error. Error number: 1422, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_commit_not_allowed_in_sf_or_trg">ER_COMMIT_NOT_ALLOWED_IN_SF_OR_TRG</a>.
    er_commit_not_allowed_in_sf_or_trg = 1422,

    /// Common server error. Error number: 1423, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_default_for_view_field">ER_NO_DEFAULT_FOR_VIEW_FIELD</a>.
    er_no_default_for_view_field = 1423,

    /// Common server error. Error number: 1424, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_no_recursion">ER_SP_NO_RECURSION</a>.
    er_sp_no_recursion = 1424,

    /// Common server error. Error number: 1425, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_scale">ER_TOO_BIG_SCALE</a>.
    er_too_big_scale = 1425,

    /// Common server error. Error number: 1426, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_precision">ER_TOO_BIG_PRECISION</a>.
    er_too_big_precision = 1426,

    /// Common server error. Error number: 1427, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_m_bigger_than_d">ER_M_BIGGER_THAN_D</a>.
    er_m_bigger_than_d = 1427,

    /// Common server error. Error number: 1428, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_lock_of_system_table">ER_WRONG_LOCK_OF_SYSTEM_TABLE</a>.
    er_wrong_lock_of_system_table = 1428,

    /// Common server error. Error number: 1429, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_connect_to_foreign_data_source">ER_CONNECT_TO_FOREIGN_DATA_SOURCE</a>.
    er_connect_to_foreign_data_source = 1429,

    /// Common server error. Error number: 1430, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_query_on_foreign_data_source">ER_QUERY_ON_FOREIGN_DATA_SOURCE</a>.
    er_query_on_foreign_data_source = 1430,

    /// Common server error. Error number: 1431, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_foreign_data_source_doesnt_exist">ER_FOREIGN_DATA_SOURCE_DOESNT_EXIST</a>.
    er_foreign_data_source_doesnt_exist = 1431,

    /// Common server error. Error number: 1432, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_foreign_data_string_invalid_cant_create">ER_FOREIGN_DATA_STRING_INVALID_CANT_CREATE</a>.
    er_foreign_data_string_invalid_cant_create = 1432,

    /// Common server error. Error number: 1433, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_foreign_data_string_invalid">ER_FOREIGN_DATA_STRING_INVALID</a>.
    er_foreign_data_string_invalid = 1433,

    /// Common server error. Error number: 1434, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_federated_table">ER_CANT_CREATE_FEDERATED_TABLE</a>.
    er_cant_create_federated_table = 1434,

    /// Common server error. Error number: 1435, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_in_wrong_schema">ER_TRG_IN_WRONG_SCHEMA</a>.
    er_trg_in_wrong_schema = 1435,

    /// Common server error. Error number: 1436, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stack_overrun_need_more">ER_STACK_OVERRUN_NEED_MORE</a>.
    er_stack_overrun_need_more = 1436,

    /// Common server error. Error number: 1437, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_body">ER_TOO_LONG_BODY</a>.
    er_too_long_body = 1437,

    /// Common server error. Error number: 1438, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_cant_drop_default_keycache">ER_WARN_CANT_DROP_DEFAULT_KEYCACHE</a>.
    er_warn_cant_drop_default_keycache = 1438,

    /// Common server error. Error number: 1439, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_big_displaywidth">ER_TOO_BIG_DISPLAYWIDTH</a>.
    er_too_big_displaywidth = 1439,

    /// Common server error. Error number: 1440, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xaer_dupid">ER_XAER_DUPID</a>.
    er_xaer_dupid = 1440,

    /// Common server error. Error number: 1441, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_datetime_function_overflow">ER_DATETIME_FUNCTION_OVERFLOW</a>.
    er_datetime_function_overflow = 1441,

    /// Common server error. Error number: 1442, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_update_used_table_in_sf_or_trg">ER_CANT_UPDATE_USED_TABLE_IN_SF_OR_TRG</a>.
    er_cant_update_used_table_in_sf_or_trg = 1442,

    /// Common server error. Error number: 1443, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_prevent_update">ER_VIEW_PREVENT_UPDATE</a>.
    er_view_prevent_update = 1443,

    /// Common server error. Error number: 1444, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_ps_no_recursion">ER_PS_NO_RECURSION</a>.
    er_ps_no_recursion = 1444,

    /// Common server error. Error number: 1445, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_cant_set_autocommit">ER_SP_CANT_SET_AUTOCOMMIT</a>.
    er_sp_cant_set_autocommit = 1445,

    /// Common server error. Error number: 1446, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_malformed_definer">ER_MALFORMED_DEFINER</a>.
    er_malformed_definer = 1446,

    /// Common server error. Error number: 1447, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_frm_no_user">ER_VIEW_FRM_NO_USER</a>.
    er_view_frm_no_user = 1447,

    /// Common server error. Error number: 1448, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_other_user">ER_VIEW_OTHER_USER</a>.
    er_view_other_user = 1448,

    /// Common server error. Error number: 1449, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_such_user">ER_NO_SUCH_USER</a>.
    er_no_such_user = 1449,

    /// Common server error. Error number: 1450, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_forbid_schema_change">ER_FORBID_SCHEMA_CHANGE</a>.
    er_forbid_schema_change = 1450,

    /// Common server error. Error number: 1451, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_row_is_referenced_2">ER_ROW_IS_REFERENCED_2</a>.
    er_row_is_referenced_2 = 1451,

    /// Common server error. Error number: 1452, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_referenced_row_2">ER_NO_REFERENCED_ROW_2</a>.
    er_no_referenced_row_2 = 1452,

    /// Common server error. Error number: 1453, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_bad_var_shadow">ER_SP_BAD_VAR_SHADOW</a>.
    er_sp_bad_var_shadow = 1453,

    /// Common server error. Error number: 1454, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_no_definer">ER_TRG_NO_DEFINER</a>.
    er_trg_no_definer = 1454,

    /// Common server error. Error number: 1455, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_old_file_format">ER_OLD_FILE_FORMAT</a>.
    er_old_file_format = 1455,

    /// Common server error. Error number: 1456, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_recursion_limit">ER_SP_RECURSION_LIMIT</a>.
    er_sp_recursion_limit = 1456,

    /// Common server error. Error number: 1457, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_proc_table_corrupt">ER_SP_PROC_TABLE_CORRUPT</a>.
    er_sp_proc_table_corrupt = 1457,

    /// Common server error. Error number: 1458, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_wrong_name">ER_SP_WRONG_NAME</a>.
    er_sp_wrong_name = 1458,

    /// Common server error. Error number: 1459, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_needs_upgrade">ER_TABLE_NEEDS_UPGRADE</a>.
    er_table_needs_upgrade = 1459,

    /// Common server error. Error number: 1460, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sp_no_aggregate">ER_SP_NO_AGGREGATE</a>.
    er_sp_no_aggregate = 1460,

    /// Common server error. Error number: 1461, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_max_prepared_stmt_count_reached">ER_MAX_PREPARED_STMT_COUNT_REACHED</a>.
    er_max_prepared_stmt_count_reached = 1461,

    /// Common server error. Error number: 1462, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_recursive">ER_VIEW_RECURSIVE</a>.
    er_view_recursive = 1462,

    /// Common server error. Error number: 1463, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_non_grouping_field_used">ER_NON_GROUPING_FIELD_USED</a>.
    er_non_grouping_field_used = 1463,

    /// Common server error. Error number: 1464, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_cant_handle_spkeys">ER_TABLE_CANT_HANDLE_SPKEYS</a>.
    er_table_cant_handle_spkeys = 1464,

    /// Common server error. Error number: 1465, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_triggers_on_system_schema">ER_NO_TRIGGERS_ON_SYSTEM_SCHEMA</a>.
    er_no_triggers_on_system_schema = 1465,

    /// Common server error. Error number: 1466, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_removed_spaces">ER_REMOVED_SPACES</a>.
    er_removed_spaces = 1466,

    /// Common server error. Error number: 1467, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_autoinc_read_failed">ER_AUTOINC_READ_FAILED</a>.
    er_autoinc_read_failed = 1467,

    /// Common server error. Error number: 1468, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_username">ER_USERNAME</a>.
    er_username = 1468,

    /// Common server error. Error number: 1469, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_hostname">ER_HOSTNAME</a>.
    er_hostname = 1469,

    /// Common server error. Error number: 1470, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_string_length">ER_WRONG_STRING_LENGTH</a>.
    er_wrong_string_length = 1470,

    /// Common server error. Error number: 1471, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_non_insertable_table">ER_NON_INSERTABLE_TABLE</a>.
    er_non_insertable_table = 1471,

    /// Common server error. Error number: 1472, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_admin_wrong_mrg_table">ER_ADMIN_WRONG_MRG_TABLE</a>.
    er_admin_wrong_mrg_table = 1472,

    /// Common server error. Error number: 1473, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_high_level_of_nesting_for_select">ER_TOO_HIGH_LEVEL_OF_NESTING_FOR_SELECT</a>.
    er_too_high_level_of_nesting_for_select = 1473,

    /// Common server error. Error number: 1474, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_name_becomes_empty">ER_NAME_BECOMES_EMPTY</a>.
    er_name_becomes_empty = 1474,

    /// Common server error. Error number: 1475, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_ambiguous_field_term">ER_AMBIGUOUS_FIELD_TERM</a>.
    er_ambiguous_field_term = 1475,

    /// Common server error. Error number: 1476, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_foreign_server_exists">ER_FOREIGN_SERVER_EXISTS</a>.
    er_foreign_server_exists = 1476,

    /// Common server error. Error number: 1477, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_foreign_server_doesnt_exist">ER_FOREIGN_SERVER_DOESNT_EXIST</a>.
    er_foreign_server_doesnt_exist = 1477,

    /// Common server error. Error number: 1478, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_illegal_ha_create_option">ER_ILLEGAL_HA_CREATE_OPTION</a>.
    er_illegal_ha_create_option = 1478,

    /// Common server error. Error number: 1479, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_requires_values_error">ER_PARTITION_REQUIRES_VALUES_ERROR</a>.
    er_partition_requires_values_error = 1479,

    /// Common server error. Error number: 1480, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_wrong_values_error">ER_PARTITION_WRONG_VALUES_ERROR</a>.
    er_partition_wrong_values_error = 1480,

    /// Common server error. Error number: 1481, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_maxvalue_error">ER_PARTITION_MAXVALUE_ERROR</a>.
    er_partition_maxvalue_error = 1481,

    /// Common server error. Error number: 1482, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_subpartition_error">ER_PARTITION_SUBPARTITION_ERROR</a>.
    er_partition_subpartition_error = 1482,

    /// Common server error. Error number: 1483, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_subpart_mix_error">ER_PARTITION_SUBPART_MIX_ERROR</a>.
    er_partition_subpart_mix_error = 1483,

    /// Common server error. Error number: 1484, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_wrong_no_part_error">ER_PARTITION_WRONG_NO_PART_ERROR</a>.
    er_partition_wrong_no_part_error = 1484,

    /// Common server error. Error number: 1485, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_wrong_no_subpart_error">ER_PARTITION_WRONG_NO_SUBPART_ERROR</a>.
    er_partition_wrong_no_subpart_error = 1485,

    /// Common server error. Error number: 1486, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_expr_in_partition_func_error">ER_WRONG_EXPR_IN_PARTITION_FUNC_ERROR</a>.
    er_wrong_expr_in_partition_func_error = 1486,

    /// Common server error. Error number: 1488, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_field_not_found_part_error">ER_FIELD_NOT_FOUND_PART_ERROR</a>.
    er_field_not_found_part_error = 1488,

    /// Common server error. Error number: 1489, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_list_of_fields_only_in_hash_error">ER_LIST_OF_FIELDS_ONLY_IN_HASH_ERROR</a>.
    er_list_of_fields_only_in_hash_error = 1489,

    /// Common server error. Error number: 1490, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_inconsistent_partition_info_error">ER_INCONSISTENT_PARTITION_INFO_ERROR</a>.
    er_inconsistent_partition_info_error = 1490,

    /// Common server error. Error number: 1491, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_func_not_allowed_error">ER_PARTITION_FUNC_NOT_ALLOWED_ERROR</a>.
    er_partition_func_not_allowed_error = 1491,

    /// Common server error. Error number: 1492, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partitions_must_be_defined_error">ER_PARTITIONS_MUST_BE_DEFINED_ERROR</a>.
    er_partitions_must_be_defined_error = 1492,

    /// Common server error. Error number: 1493, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_range_not_increasing_error">ER_RANGE_NOT_INCREASING_ERROR</a>.
    er_range_not_increasing_error = 1493,

    /// Common server error. Error number: 1494, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_inconsistent_type_of_functions_error">ER_INCONSISTENT_TYPE_OF_FUNCTIONS_ERROR</a>.
    er_inconsistent_type_of_functions_error = 1494,

    /// Common server error. Error number: 1495, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_multiple_def_const_in_list_part_error">ER_MULTIPLE_DEF_CONST_IN_LIST_PART_ERROR</a>.
    er_multiple_def_const_in_list_part_error = 1495,

    /// Common server error. Error number: 1496, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_entry_error">ER_PARTITION_ENTRY_ERROR</a>.
    er_partition_entry_error = 1496,

    /// Common server error. Error number: 1497, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mix_handler_error">ER_MIX_HANDLER_ERROR</a>.
    er_mix_handler_error = 1497,

    /// Common server error. Error number: 1498, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_not_defined_error">ER_PARTITION_NOT_DEFINED_ERROR</a>.
    er_partition_not_defined_error = 1498,

    /// Common server error. Error number: 1499, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_partitions_error">ER_TOO_MANY_PARTITIONS_ERROR</a>.
    er_too_many_partitions_error = 1499,

    /// Common server error. Error number: 1500, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_subpartition_error">ER_SUBPARTITION_ERROR</a>.
    er_subpartition_error = 1500,

    /// Common server error. Error number: 1501, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_handler_file">ER_CANT_CREATE_HANDLER_FILE</a>.
    er_cant_create_handler_file = 1501,

    /// Common server error. Error number: 1502, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_blob_field_in_part_func_error">ER_BLOB_FIELD_IN_PART_FUNC_ERROR</a>.
    er_blob_field_in_part_func_error = 1502,

    /// Common server error. Error number: 1503, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unique_key_need_all_fields_in_pf">ER_UNIQUE_KEY_NEED_ALL_FIELDS_IN_PF</a>.
    er_unique_key_need_all_fields_in_pf = 1503,

    /// Common server error. Error number: 1504, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_parts_error">ER_NO_PARTS_ERROR</a>.
    er_no_parts_error = 1504,

    /// Common server error. Error number: 1505, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_mgmt_on_nonpartitioned">ER_PARTITION_MGMT_ON_NONPARTITIONED</a>.
    er_partition_mgmt_on_nonpartitioned = 1505,

    /// Common server error. Error number: 1507, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_drop_partition_non_existent">ER_DROP_PARTITION_NON_EXISTENT</a>.
    er_drop_partition_non_existent = 1507,

    /// Common server error. Error number: 1508, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_drop_last_partition">ER_DROP_LAST_PARTITION</a>.
    er_drop_last_partition = 1508,

    /// Common server error. Error number: 1509, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_coalesce_only_on_hash_partition">ER_COALESCE_ONLY_ON_HASH_PARTITION</a>.
    er_coalesce_only_on_hash_partition = 1509,

    /// Common server error. Error number: 1510, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_reorg_hash_only_on_same_no">ER_REORG_HASH_ONLY_ON_SAME_NO</a>.
    er_reorg_hash_only_on_same_no = 1510,

    /// Common server error. Error number: 1511, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_reorg_no_param_error">ER_REORG_NO_PARAM_ERROR</a>.
    er_reorg_no_param_error = 1511,

    /// Common server error. Error number: 1512, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_only_on_range_list_partition">ER_ONLY_ON_RANGE_LIST_PARTITION</a>.
    er_only_on_range_list_partition = 1512,

    /// Common server error. Error number: 1513, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_add_partition_subpart_error">ER_ADD_PARTITION_SUBPART_ERROR</a>.
    er_add_partition_subpart_error = 1513,

    /// Common server error. Error number: 1514, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_add_partition_no_new_partition">ER_ADD_PARTITION_NO_NEW_PARTITION</a>.
    er_add_partition_no_new_partition = 1514,

    /// Common server error. Error number: 1515, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_coalesce_partition_no_partition">ER_COALESCE_PARTITION_NO_PARTITION</a>.
    er_coalesce_partition_no_partition = 1515,

    /// Common server error. Error number: 1516, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_reorg_partition_not_exist">ER_REORG_PARTITION_NOT_EXIST</a>.
    er_reorg_partition_not_exist = 1516,

    /// Common server error. Error number: 1517, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_same_name_partition">ER_SAME_NAME_PARTITION</a>.
    er_same_name_partition = 1517,

    /// Common server error. Error number: 1518, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_binlog_error">ER_NO_BINLOG_ERROR</a>.
    er_no_binlog_error = 1518,

    /// Common server error. Error number: 1519, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_consecutive_reorg_partitions">ER_CONSECUTIVE_REORG_PARTITIONS</a>.
    er_consecutive_reorg_partitions = 1519,

    /// Common server error. Error number: 1520, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_reorg_outside_range">ER_REORG_OUTSIDE_RANGE</a>.
    er_reorg_outside_range = 1520,

    /// Common server error. Error number: 1521, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_function_failure">ER_PARTITION_FUNCTION_FAILURE</a>.
    er_partition_function_failure = 1521,

    /// Common server error. Error number: 1522, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_part_state_error">ER_PART_STATE_ERROR</a>.
    er_part_state_error = 1522,

    /// Common server error. Error number: 1523, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_limited_part_range">ER_LIMITED_PART_RANGE</a>.
    er_limited_part_range = 1523,

    /// Common server error. Error number: 1524, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_plugin_is_not_loaded">ER_PLUGIN_IS_NOT_LOADED</a>.
    er_plugin_is_not_loaded = 1524,

    /// Common server error. Error number: 1525, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_value">ER_WRONG_VALUE</a>.
    er_wrong_value = 1525,

    /// Common server error. Error number: 1526, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_partition_for_given_value">ER_NO_PARTITION_FOR_GIVEN_VALUE</a>.
    er_no_partition_for_given_value = 1526,

    /// Common server error. Error number: 1527, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_filegroup_option_only_once">ER_FILEGROUP_OPTION_ONLY_ONCE</a>.
    er_filegroup_option_only_once = 1527,

    /// Common server error. Error number: 1528, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_create_filegroup_failed">ER_CREATE_FILEGROUP_FAILED</a>.
    er_create_filegroup_failed = 1528,

    /// Common server error. Error number: 1529, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_drop_filegroup_failed">ER_DROP_FILEGROUP_FAILED</a>.
    er_drop_filegroup_failed = 1529,

    /// Common server error. Error number: 1530, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_tablespace_auto_extend_error">ER_TABLESPACE_AUTO_EXTEND_ERROR</a>.
    er_tablespace_auto_extend_error = 1530,

    /// Common server error. Error number: 1531, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_size_number">ER_WRONG_SIZE_NUMBER</a>.
    er_wrong_size_number = 1531,

    /// Common server error. Error number: 1532, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_size_overflow_error">ER_SIZE_OVERFLOW_ERROR</a>.
    er_size_overflow_error = 1532,

    /// Common server error. Error number: 1533, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_filegroup_failed">ER_ALTER_FILEGROUP_FAILED</a>.
    er_alter_filegroup_failed = 1533,

    /// Common server error. Error number: 1534, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_row_logging_failed">ER_BINLOG_ROW_LOGGING_FAILED</a>.
    er_binlog_row_logging_failed = 1534,

    /// Common server error. Error number: 1535, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_row_wrong_table_def">ER_BINLOG_ROW_WRONG_TABLE_DEF</a>.
    er_binlog_row_wrong_table_def = 1535,

    /// Common server error. Error number: 1536, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_row_rbr_to_sbr">ER_BINLOG_ROW_RBR_TO_SBR</a>.
    er_binlog_row_rbr_to_sbr = 1536,

    /// Common server error. Error number: 1537, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_already_exists">ER_EVENT_ALREADY_EXISTS</a>.
    er_event_already_exists = 1537,

    /// Common server error. Error number: 1538, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_store_failed">ER_EVENT_STORE_FAILED</a>.
    er_event_store_failed = 1538,

    /// Common server error. Error number: 1539, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_does_not_exist">ER_EVENT_DOES_NOT_EXIST</a>.
    er_event_does_not_exist = 1539,

    /// Common server error. Error number: 1540, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_cant_alter">ER_EVENT_CANT_ALTER</a>.
    er_event_cant_alter = 1540,

    /// Common server error. Error number: 1541, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_drop_failed">ER_EVENT_DROP_FAILED</a>.
    er_event_drop_failed = 1541,

    /// Common server error. Error number: 1542, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_interval_not_positive_or_too_big">ER_EVENT_INTERVAL_NOT_POSITIVE_OR_TOO_BIG</a>.
    er_event_interval_not_positive_or_too_big = 1542,

    /// Common server error. Error number: 1543, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_ends_before_starts">ER_EVENT_ENDS_BEFORE_STARTS</a>.
    er_event_ends_before_starts = 1543,

    /// Common server error. Error number: 1544, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_exec_time_in_the_past">ER_EVENT_EXEC_TIME_IN_THE_PAST</a>.
    er_event_exec_time_in_the_past = 1544,

    /// Common server error. Error number: 1545, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_open_table_failed">ER_EVENT_OPEN_TABLE_FAILED</a>.
    er_event_open_table_failed = 1545,

    /// Common server error. Error number: 1546, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_neither_m_expr_nor_m_at">ER_EVENT_NEITHER_M_EXPR_NOR_M_AT</a>.
    er_event_neither_m_expr_nor_m_at = 1546,

    /// Common server error. Error number: 1549, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_cannot_delete">ER_EVENT_CANNOT_DELETE</a>.
    er_event_cannot_delete = 1549,

    /// Common server error. Error number: 1550, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_compile_error">ER_EVENT_COMPILE_ERROR</a>.
    er_event_compile_error = 1550,

    /// Common server error. Error number: 1551, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_same_name">ER_EVENT_SAME_NAME</a>.
    er_event_same_name = 1551,

    /// Common server error. Error number: 1552, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_data_too_long">ER_EVENT_DATA_TOO_LONG</a>.
    er_event_data_too_long = 1552,

    /// Common server error. Error number: 1553, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_drop_index_fk">ER_DROP_INDEX_FK</a>.
    er_drop_index_fk = 1553,

    /// Common server error. Error number: 1554, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_deprecated_syntax_with_ver">ER_WARN_DEPRECATED_SYNTAX_WITH_VER</a>.
    er_warn_deprecated_syntax_with_ver = 1554,

    /// Common server error. Error number: 1555, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_write_lock_log_table">ER_CANT_WRITE_LOCK_LOG_TABLE</a>.
    er_cant_write_lock_log_table = 1555,

    /// Common server error. Error number: 1556, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_lock_log_table">ER_CANT_LOCK_LOG_TABLE</a>.
    er_cant_lock_log_table = 1556,

    /// Common server error. Error number: 1558, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_col_count_doesnt_match_please_update">ER_COL_COUNT_DOESNT_MATCH_PLEASE_UPDATE</a>.
    er_col_count_doesnt_match_please_update = 1558,

    /// Common server error. Error number: 1559, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_temp_table_prevents_switch_out_of_rbr">ER_TEMP_TABLE_PREVENTS_SWITCH_OUT_OF_RBR</a>.
    er_temp_table_prevents_switch_out_of_rbr = 1559,

    /// Common server error. Error number: 1560, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stored_function_prevents_switch_binlog_format">ER_STORED_FUNCTION_PREVENTS_SWITCH_BINLOG_FORMAT</a>.
    er_stored_function_prevents_switch_binlog_format = 1560,

    /// Common server error. Error number: 1562, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_no_temporary">ER_PARTITION_NO_TEMPORARY</a>.
    er_partition_no_temporary = 1562,

    /// Common server error. Error number: 1563, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_const_domain_error">ER_PARTITION_CONST_DOMAIN_ERROR</a>.
    er_partition_const_domain_error = 1563,

    /// Common server error. Error number: 1564, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_function_is_not_allowed">ER_PARTITION_FUNCTION_IS_NOT_ALLOWED</a>.
    er_partition_function_is_not_allowed = 1564,

    /// Common server error. Error number: 1565, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_ddl_log_error">ER_DDL_LOG_ERROR</a>.
    er_ddl_log_error = 1565,

    /// Common server error. Error number: 1566, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_null_in_values_less_than">ER_NULL_IN_VALUES_LESS_THAN</a>.
    er_null_in_values_less_than = 1566,

    /// Common server error. Error number: 1567, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_partition_name">ER_WRONG_PARTITION_NAME</a>.
    er_wrong_partition_name = 1567,

    /// Common server error. Error number: 1568, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_change_tx_characteristics">ER_CANT_CHANGE_TX_CHARACTERISTICS</a>.
    er_cant_change_tx_characteristics = 1568,

    /// Common server error. Error number: 1569, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_entry_autoincrement_case">ER_DUP_ENTRY_AUTOINCREMENT_CASE</a>.
    er_dup_entry_autoincrement_case = 1569,

    /// Common server error. Error number: 1570, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_modify_queue_error">ER_EVENT_MODIFY_QUEUE_ERROR</a>.
    er_event_modify_queue_error = 1570,

    /// Common server error. Error number: 1571, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_set_var_error">ER_EVENT_SET_VAR_ERROR</a>.
    er_event_set_var_error = 1571,

    /// Common server error. Error number: 1572, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_merge_error">ER_PARTITION_MERGE_ERROR</a>.
    er_partition_merge_error = 1572,

    /// Common server error. Error number: 1573, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_activate_log">ER_CANT_ACTIVATE_LOG</a>.
    er_cant_activate_log = 1573,

    /// Common server error. Error number: 1574, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_rbr_not_available">ER_RBR_NOT_AVAILABLE</a>.
    er_rbr_not_available = 1574,

    /// Common server error. Error number: 1575, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_base64_decode_error">ER_BASE64_DECODE_ERROR</a>.
    er_base64_decode_error = 1575,

    /// Common server error. Error number: 1576, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_recursion_forbidden">ER_EVENT_RECURSION_FORBIDDEN</a>.
    er_event_recursion_forbidden = 1576,

    /// Common server error. Error number: 1577, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_events_db_error">ER_EVENTS_DB_ERROR</a>.
    er_events_db_error = 1577,

    /// Common server error. Error number: 1578, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_only_integers_allowed">ER_ONLY_INTEGERS_ALLOWED</a>.
    er_only_integers_allowed = 1578,

    /// Common server error. Error number: 1579, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unsuported_log_engine">ER_UNSUPORTED_LOG_ENGINE</a>.
    er_unsuported_log_engine = 1579,

    /// Common server error. Error number: 1580, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_log_statement">ER_BAD_LOG_STATEMENT</a>.
    er_bad_log_statement = 1580,

    /// Common server error. Error number: 1581, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_rename_log_table">ER_CANT_RENAME_LOG_TABLE</a>.
    er_cant_rename_log_table = 1581,

    /// Common server error. Error number: 1582, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_paramcount_to_native_fct">ER_WRONG_PARAMCOUNT_TO_NATIVE_FCT</a>.
    er_wrong_paramcount_to_native_fct = 1582,

    /// Common server error. Error number: 1583, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_parameters_to_native_fct">ER_WRONG_PARAMETERS_TO_NATIVE_FCT</a>.
    er_wrong_parameters_to_native_fct = 1583,

    /// Common server error. Error number: 1584, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_parameters_to_stored_fct">ER_WRONG_PARAMETERS_TO_STORED_FCT</a>.
    er_wrong_parameters_to_stored_fct = 1584,

    /// Common server error. Error number: 1585, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_native_fct_name_collision">ER_NATIVE_FCT_NAME_COLLISION</a>.
    er_native_fct_name_collision = 1585,

    /// Common server error. Error number: 1586, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_entry_with_key_name">ER_DUP_ENTRY_WITH_KEY_NAME</a>.
    er_dup_entry_with_key_name = 1586,

    /// Common server error. Error number: 1587, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_purge_emfile">ER_BINLOG_PURGE_EMFILE</a>.
    er_binlog_purge_emfile = 1587,

    /// Common server error. Error number: 1588, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_cannot_create_in_the_past">ER_EVENT_CANNOT_CREATE_IN_THE_PAST</a>.
    er_event_cannot_create_in_the_past = 1588,

    /// Common server error. Error number: 1589, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_cannot_alter_in_the_past">ER_EVENT_CANNOT_ALTER_IN_THE_PAST</a>.
    er_event_cannot_alter_in_the_past = 1589,

    /// Common server error. Error number: 1590, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_incident">ER_SLAVE_INCIDENT</a>.
    er_slave_incident = 1590,

    /// Common server error. Error number: 1591, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_partition_for_given_value_silent">ER_NO_PARTITION_FOR_GIVEN_VALUE_SILENT</a>.
    er_no_partition_for_given_value_silent = 1591,

    /// Common server error. Error number: 1592, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_statement">ER_BINLOG_UNSAFE_STATEMENT</a>.
    er_binlog_unsafe_statement = 1592,

    /// Common server error. Error number: 1594, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_relay_log_read_failure">ER_SLAVE_RELAY_LOG_READ_FAILURE</a>.
    er_slave_relay_log_read_failure = 1594,

    /// Common server error. Error number: 1595, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_relay_log_write_failure">ER_SLAVE_RELAY_LOG_WRITE_FAILURE</a>.
    er_slave_relay_log_write_failure = 1595,

    /// Common server error. Error number: 1596, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_create_event_failure">ER_SLAVE_CREATE_EVENT_FAILURE</a>.
    er_slave_create_event_failure = 1596,

    /// Common server error. Error number: 1597, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_master_com_failure">ER_SLAVE_MASTER_COM_FAILURE</a>.
    er_slave_master_com_failure = 1597,

    /// Common server error. Error number: 1598, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_logging_impossible">ER_BINLOG_LOGGING_IMPOSSIBLE</a>.
    er_binlog_logging_impossible = 1598,

    /// Common server error. Error number: 1599, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_no_creation_ctx">ER_VIEW_NO_CREATION_CTX</a>.
    er_view_no_creation_ctx = 1599,

    /// Common server error. Error number: 1600, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_view_invalid_creation_ctx">ER_VIEW_INVALID_CREATION_CTX</a>.
    er_view_invalid_creation_ctx = 1600,

    /// Common server error. Error number: 1601, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sr_invalid_creation_ctx">ER_SR_INVALID_CREATION_CTX</a>.
    er_sr_invalid_creation_ctx = 1601,

    /// Common server error. Error number: 1602, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_corrupted_file">ER_TRG_CORRUPTED_FILE</a>.
    er_trg_corrupted_file = 1602,

    /// Common server error. Error number: 1603, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_no_creation_ctx">ER_TRG_NO_CREATION_CTX</a>.
    er_trg_no_creation_ctx = 1603,

    /// Common server error. Error number: 1604, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_invalid_creation_ctx">ER_TRG_INVALID_CREATION_CTX</a>.
    er_trg_invalid_creation_ctx = 1604,

    /// Common server error. Error number: 1605, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_event_invalid_creation_ctx">ER_EVENT_INVALID_CREATION_CTX</a>.
    er_event_invalid_creation_ctx = 1605,

    /// Common server error. Error number: 1606, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_trg_cant_open_table">ER_TRG_CANT_OPEN_TABLE</a>.
    er_trg_cant_open_table = 1606,

    /// Common server error. Error number: 1607, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_create_sroutine">ER_CANT_CREATE_SROUTINE</a>.
    er_cant_create_sroutine = 1607,

    /// Common server error. Error number: 1609, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_format_description_event_before_binlog_statement">ER_NO_FORMAT_DESCRIPTION_EVENT_BEFORE_BINLOG_STATEMENT</a>.
    er_no_format_description_event_before_binlog_statement = 1609,

    /// Common server error. Error number: 1610, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_corrupt_event">ER_SLAVE_CORRUPT_EVENT</a>.
    er_slave_corrupt_event = 1610,

    /// Common server error. Error number: 1612, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_log_purge_no_file">ER_LOG_PURGE_NO_FILE</a>.
    er_log_purge_no_file = 1612,

    /// Common server error. Error number: 1613, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xa_rbtimeout">ER_XA_RBTIMEOUT</a>.
    er_xa_rbtimeout = 1613,

    /// Common server error. Error number: 1614, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_xa_rbdeadlock">ER_XA_RBDEADLOCK</a>.
    er_xa_rbdeadlock = 1614,

    /// Common server error. Error number: 1615, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_need_reprepare">ER_NEED_REPREPARE</a>.
    er_need_reprepare = 1615,

    /// Common server error. Error number: 1616, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_delayed_not_supported">ER_DELAYED_NOT_SUPPORTED</a>.
    er_delayed_not_supported = 1616,

    /// Common server error. Error number: 1617, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_no_master_info">WARN_NO_MASTER_INFO</a>.
    warn_no_master_info = 1617,

    /// Common server error. Error number: 1618, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_option_ignored">WARN_OPTION_IGNORED</a>.
    warn_option_ignored = 1618,

    /// Common server error. Error number: 1619, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_plugin_delete_builtin">ER_PLUGIN_DELETE_BUILTIN</a>.
    er_plugin_delete_builtin = 1619,

    /// Common server error. Error number: 1620, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_plugin_busy">WARN_PLUGIN_BUSY</a>.
    warn_plugin_busy = 1620,

    /// Common server error. Error number: 1621, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_variable_is_readonly">ER_VARIABLE_IS_READONLY</a>.
    er_variable_is_readonly = 1621,

    /// Common server error. Error number: 1622, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_engine_transaction_rollback">ER_WARN_ENGINE_TRANSACTION_ROLLBACK</a>.
    er_warn_engine_transaction_rollback = 1622,

    /// Common server error. Error number: 1623, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_heartbeat_failure">ER_SLAVE_HEARTBEAT_FAILURE</a>.
    er_slave_heartbeat_failure = 1623,

    /// Common server error. Error number: 1624, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_heartbeat_value_out_of_range">ER_SLAVE_HEARTBEAT_VALUE_OUT_OF_RANGE</a>.
    er_slave_heartbeat_value_out_of_range = 1624,

    /// Common server error. Error number: 1626, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_conflict_fn_parse_error">ER_CONFLICT_FN_PARSE_ERROR</a>.
    er_conflict_fn_parse_error = 1626,

    /// Common server error. Error number: 1627, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_exceptions_write_error">ER_EXCEPTIONS_WRITE_ERROR</a>.
    er_exceptions_write_error = 1627,

    /// Common server error. Error number: 1628, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_table_comment">ER_TOO_LONG_TABLE_COMMENT</a>.
    er_too_long_table_comment = 1628,

    /// Common server error. Error number: 1629, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_field_comment">ER_TOO_LONG_FIELD_COMMENT</a>.
    er_too_long_field_comment = 1629,

    /// Common server error. Error number: 1630, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_func_inexistent_name_collision">ER_FUNC_INEXISTENT_NAME_COLLISION</a>.
    er_func_inexistent_name_collision = 1630,

    /// Common server error. Error number: 1631, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_database_name">ER_DATABASE_NAME</a>.
    er_database_name = 1631,

    /// Common server error. Error number: 1632, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_name">ER_TABLE_NAME</a>.
    er_table_name = 1632,

    /// Common server error. Error number: 1633, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_name">ER_PARTITION_NAME</a>.
    er_partition_name = 1633,

    /// Common server error. Error number: 1634, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_subpartition_name">ER_SUBPARTITION_NAME</a>.
    er_subpartition_name = 1634,

    /// Common server error. Error number: 1635, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_temporary_name">ER_TEMPORARY_NAME</a>.
    er_temporary_name = 1635,

    /// Common server error. Error number: 1636, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_renamed_name">ER_RENAMED_NAME</a>.
    er_renamed_name = 1636,

    /// Common server error. Error number: 1637, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_concurrent_trxs">ER_TOO_MANY_CONCURRENT_TRXS</a>.
    er_too_many_concurrent_trxs = 1637,

    /// Common server error. Error number: 1638, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_non_ascii_separator_not_implemented">WARN_NON_ASCII_SEPARATOR_NOT_IMPLEMENTED</a>.
    warn_non_ascii_separator_not_implemented = 1638,

    /// Common server error. Error number: 1639, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_debug_sync_timeout">ER_DEBUG_SYNC_TIMEOUT</a>.
    er_debug_sync_timeout = 1639,

    /// Common server error. Error number: 1640, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_debug_sync_hit_limit">ER_DEBUG_SYNC_HIT_LIMIT</a>.
    er_debug_sync_hit_limit = 1640,

    /// Common server error. Error number: 1641, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_signal_set">ER_DUP_SIGNAL_SET</a>.
    er_dup_signal_set = 1641,

    /// Common server error. Error number: 1642, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_signal_warn">ER_SIGNAL_WARN</a>.
    er_signal_warn = 1642,

    /// Common server error. Error number: 1643, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_signal_not_found">ER_SIGNAL_NOT_FOUND</a>.
    er_signal_not_found = 1643,

    /// Common server error. Error number: 1644, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_signal_exception">ER_SIGNAL_EXCEPTION</a>.
    er_signal_exception = 1644,

    /// Common server error. Error number: 1645, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_resignal_without_active_handler">ER_RESIGNAL_WITHOUT_ACTIVE_HANDLER</a>.
    er_resignal_without_active_handler = 1645,

    /// Common server error. Error number: 1646, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_signal_bad_condition_type">ER_SIGNAL_BAD_CONDITION_TYPE</a>.
    er_signal_bad_condition_type = 1646,

    /// Common server error. Error number: 1647, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_cond_item_truncated">WARN_COND_ITEM_TRUNCATED</a>.
    warn_cond_item_truncated = 1647,

    /// Common server error. Error number: 1648, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cond_item_too_long">ER_COND_ITEM_TOO_LONG</a>.
    er_cond_item_too_long = 1648,

    /// Common server error. Error number: 1649, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_locale">ER_UNKNOWN_LOCALE</a>.
    er_unknown_locale = 1649,

    /// Common server error. Error number: 1650, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_ignore_server_ids">ER_SLAVE_IGNORE_SERVER_IDS</a>.
    er_slave_ignore_server_ids = 1650,

    /// Common server error. Error number: 1651, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_query_cache_disabled">ER_QUERY_CACHE_DISABLED</a>.
    er_query_cache_disabled = 1651,

    /// Common server error. Error number: 1652, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_same_name_partition_field">ER_SAME_NAME_PARTITION_FIELD</a>.
    er_same_name_partition_field = 1652,

    /// Common server error. Error number: 1653, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_column_list_error">ER_PARTITION_COLUMN_LIST_ERROR</a>.
    er_partition_column_list_error = 1653,

    /// Common server error. Error number: 1654, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_type_column_value_error">ER_WRONG_TYPE_COLUMN_VALUE_ERROR</a>.
    er_wrong_type_column_value_error = 1654,

    /// Common server error. Error number: 1655, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_partition_func_fields_error">ER_TOO_MANY_PARTITION_FUNC_FIELDS_ERROR</a>.
    er_too_many_partition_func_fields_error = 1655,

    /// Common server error. Error number: 1656, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_maxvalue_in_values_in">ER_MAXVALUE_IN_VALUES_IN</a>.
    er_maxvalue_in_values_in = 1656,

    /// Common server error. Error number: 1657, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_many_values_error">ER_TOO_MANY_VALUES_ERROR</a>.
    er_too_many_values_error = 1657,

    /// Common server error. Error number: 1658, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_row_single_partition_field_error">ER_ROW_SINGLE_PARTITION_FIELD_ERROR</a>.
    er_row_single_partition_field_error = 1658,

    /// Common server error. Error number: 1659, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_field_type_not_allowed_as_partition_field">ER_FIELD_TYPE_NOT_ALLOWED_AS_PARTITION_FIELD</a>.
    er_field_type_not_allowed_as_partition_field = 1659,

    /// Common server error. Error number: 1660, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_fields_too_long">ER_PARTITION_FIELDS_TOO_LONG</a>.
    er_partition_fields_too_long = 1660,

    /// Common server error. Error number: 1661, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_row_engine_and_stmt_engine">ER_BINLOG_ROW_ENGINE_AND_STMT_ENGINE</a>.
    er_binlog_row_engine_and_stmt_engine = 1661,

    /// Common server error. Error number: 1662, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_row_mode_and_stmt_engine">ER_BINLOG_ROW_MODE_AND_STMT_ENGINE</a>.
    er_binlog_row_mode_and_stmt_engine = 1662,

    /// Common server error. Error number: 1663, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_and_stmt_engine">ER_BINLOG_UNSAFE_AND_STMT_ENGINE</a>.
    er_binlog_unsafe_and_stmt_engine = 1663,

    /// Common server error. Error number: 1664, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_row_injection_and_stmt_engine">ER_BINLOG_ROW_INJECTION_AND_STMT_ENGINE</a>.
    er_binlog_row_injection_and_stmt_engine = 1664,

    /// Common server error. Error number: 1665, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_stmt_mode_and_row_engine">ER_BINLOG_STMT_MODE_AND_ROW_ENGINE</a>.
    er_binlog_stmt_mode_and_row_engine = 1665,

    /// Common server error. Error number: 1666, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_row_injection_and_stmt_mode">ER_BINLOG_ROW_INJECTION_AND_STMT_MODE</a>.
    er_binlog_row_injection_and_stmt_mode = 1666,

    /// Common server error. Error number: 1667, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_multiple_engines_and_self_logging_engine">ER_BINLOG_MULTIPLE_ENGINES_AND_SELF_LOGGING_ENGINE</a>.
    er_binlog_multiple_engines_and_self_logging_engine = 1667,

    /// Common server error. Error number: 1668, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_limit">ER_BINLOG_UNSAFE_LIMIT</a>.
    er_binlog_unsafe_limit = 1668,

    /// Common server error. Error number: 1670, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_system_table">ER_BINLOG_UNSAFE_SYSTEM_TABLE</a>.
    er_binlog_unsafe_system_table = 1670,

    /// Common server error. Error number: 1671, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_autoinc_columns">ER_BINLOG_UNSAFE_AUTOINC_COLUMNS</a>.
    er_binlog_unsafe_autoinc_columns = 1671,

    /// Common server error. Error number: 1672, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_udf">ER_BINLOG_UNSAFE_UDF</a>.
    er_binlog_unsafe_udf = 1672,

    /// Common server error. Error number: 1673, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_system_variable">ER_BINLOG_UNSAFE_SYSTEM_VARIABLE</a>.
    er_binlog_unsafe_system_variable = 1673,

    /// Common server error. Error number: 1674, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_system_function">ER_BINLOG_UNSAFE_SYSTEM_FUNCTION</a>.
    er_binlog_unsafe_system_function = 1674,

    /// Common server error. Error number: 1675, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_nontrans_after_trans">ER_BINLOG_UNSAFE_NONTRANS_AFTER_TRANS</a>.
    er_binlog_unsafe_nontrans_after_trans = 1675,

    /// Common server error. Error number: 1676, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_message_and_statement">ER_MESSAGE_AND_STATEMENT</a>.
    er_message_and_statement = 1676,

    /// Common server error. Error number: 1677, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_conversion_failed">ER_SLAVE_CONVERSION_FAILED</a>.
    er_slave_conversion_failed = 1677,

    /// Common server error. Error number: 1678, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_cant_create_conversion">ER_SLAVE_CANT_CREATE_CONVERSION</a>.
    er_slave_cant_create_conversion = 1678,

    /// Common server error. Error number: 1679, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_inside_transaction_prevents_switch_binlog_format">ER_INSIDE_TRANSACTION_PREVENTS_SWITCH_BINLOG_FORMAT</a>.
    er_inside_transaction_prevents_switch_binlog_format = 1679,

    /// Common server error. Error number: 1680, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_path_length">ER_PATH_LENGTH</a>.
    er_path_length = 1680,

    /// Common server error. Error number: 1681, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_deprecated_syntax_no_replacement">ER_WARN_DEPRECATED_SYNTAX_NO_REPLACEMENT</a>.
    er_warn_deprecated_syntax_no_replacement = 1681,

    /// Common server error. Error number: 1682, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_native_table_structure">ER_WRONG_NATIVE_TABLE_STRUCTURE</a>.
    er_wrong_native_table_structure = 1682,

    /// Common server error. Error number: 1683, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_perfschema_usage">ER_WRONG_PERFSCHEMA_USAGE</a>.
    er_wrong_perfschema_usage = 1683,

    /// Common server error. Error number: 1684, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_i_s_skipped_table">ER_WARN_I_S_SKIPPED_TABLE</a>.
    er_warn_i_s_skipped_table = 1684,

    /// Common server error. Error number: 1685, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_inside_transaction_prevents_switch_binlog_direct">ER_INSIDE_TRANSACTION_PREVENTS_SWITCH_BINLOG_DIRECT</a>.
    er_inside_transaction_prevents_switch_binlog_direct = 1685,

    /// Common server error. Error number: 1686, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stored_function_prevents_switch_binlog_direct">ER_STORED_FUNCTION_PREVENTS_SWITCH_BINLOG_DIRECT</a>.
    er_stored_function_prevents_switch_binlog_direct = 1686,

    /// Common server error. Error number: 1687, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_spatial_must_have_geom_col">ER_SPATIAL_MUST_HAVE_GEOM_COL</a>.
    er_spatial_must_have_geom_col = 1687,

    /// Common server error. Error number: 1688, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_index_comment">ER_TOO_LONG_INDEX_COMMENT</a>.
    er_too_long_index_comment = 1688,

    /// Common server error. Error number: 1689, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_lock_aborted">ER_LOCK_ABORTED</a>.
    er_lock_aborted = 1689,

    /// Common server error. Error number: 1690, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_data_out_of_range">ER_DATA_OUT_OF_RANGE</a>.
    er_data_out_of_range = 1690,

    /// Common server error. Error number: 1691, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_wrong_spvar_type_in_limit">ER_WRONG_SPVAR_TYPE_IN_LIMIT</a>.
    er_wrong_spvar_type_in_limit = 1691,

    /// Common server error. Error number: 1692, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_multiple_engines_and_self_logging_engine">ER_BINLOG_UNSAFE_MULTIPLE_ENGINES_AND_SELF_LOGGING_ENGINE</a>.
    er_binlog_unsafe_multiple_engines_and_self_logging_engine = 1692,

    /// Common server error. Error number: 1693, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_mixed_statement">ER_BINLOG_UNSAFE_MIXED_STATEMENT</a>.
    er_binlog_unsafe_mixed_statement = 1693,

    /// Common server error. Error number: 1694, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_inside_transaction_prevents_switch_sql_log_bin">ER_INSIDE_TRANSACTION_PREVENTS_SWITCH_SQL_LOG_BIN</a>.
    er_inside_transaction_prevents_switch_sql_log_bin = 1694,

    /// Common server error. Error number: 1695, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stored_function_prevents_switch_sql_log_bin">ER_STORED_FUNCTION_PREVENTS_SWITCH_SQL_LOG_BIN</a>.
    er_stored_function_prevents_switch_sql_log_bin = 1695,

    /// Common server error. Error number: 1696, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_failed_read_from_par_file">ER_FAILED_READ_FROM_PAR_FILE</a>.
    er_failed_read_from_par_file = 1696,

    /// Common server error. Error number: 1697, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_values_is_not_int_type_error">ER_VALUES_IS_NOT_INT_TYPE_ERROR</a>.
    er_values_is_not_int_type_error = 1697,

    /// Common server error. Error number: 1698, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_access_denied_no_password_error">ER_ACCESS_DENIED_NO_PASSWORD_ERROR</a>.
    er_access_denied_no_password_error = 1698,

    /// Common server error. Error number: 1699, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_set_password_auth_plugin">ER_SET_PASSWORD_AUTH_PLUGIN</a>.
    er_set_password_auth_plugin = 1699,

    /// Common server error. Error number: 1700, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_grant_plugin_user_exists">ER_GRANT_PLUGIN_USER_EXISTS</a>.
    er_grant_plugin_user_exists = 1700,

    /// Common server error. Error number: 1701, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_truncate_illegal_fk">ER_TRUNCATE_ILLEGAL_FK</a>.
    er_truncate_illegal_fk = 1701,

    /// Common server error. Error number: 1702, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_plugin_is_permanent">ER_PLUGIN_IS_PERMANENT</a>.
    er_plugin_is_permanent = 1702,

    /// Common server error. Error number: 1703, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_heartbeat_value_out_of_range_min">ER_SLAVE_HEARTBEAT_VALUE_OUT_OF_RANGE_MIN</a>.
    er_slave_heartbeat_value_out_of_range_min = 1703,

    /// Common server error. Error number: 1704, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_heartbeat_value_out_of_range_max">ER_SLAVE_HEARTBEAT_VALUE_OUT_OF_RANGE_MAX</a>.
    er_slave_heartbeat_value_out_of_range_max = 1704,

    /// Common server error. Error number: 1705, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stmt_cache_full">ER_STMT_CACHE_FULL</a>.
    er_stmt_cache_full = 1705,

    /// Common server error. Error number: 1706, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_multi_update_key_conflict">ER_MULTI_UPDATE_KEY_CONFLICT</a>.
    er_multi_update_key_conflict = 1706,

    /// Common server error. Error number: 1707, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_needs_rebuild">ER_TABLE_NEEDS_REBUILD</a>.
    er_table_needs_rebuild = 1707,

    /// Common server error. Error number: 1708, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_option_below_limit">WARN_OPTION_BELOW_LIMIT</a>.
    warn_option_below_limit = 1708,

    /// Common server error. Error number: 1709, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_index_column_too_long">ER_INDEX_COLUMN_TOO_LONG</a>.
    er_index_column_too_long = 1709,

    /// Common server error. Error number: 1710, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_in_trigger_body">ER_ERROR_IN_TRIGGER_BODY</a>.
    er_error_in_trigger_body = 1710,

    /// Common server error. Error number: 1711, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_error_in_unknown_trigger_body">ER_ERROR_IN_UNKNOWN_TRIGGER_BODY</a>.
    er_error_in_unknown_trigger_body = 1711,

    /// Common server error. Error number: 1712, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_index_corrupt">ER_INDEX_CORRUPT</a>.
    er_index_corrupt = 1712,

    /// Common server error. Error number: 1713, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_undo_record_too_big">ER_UNDO_RECORD_TOO_BIG</a>.
    er_undo_record_too_big = 1713,

    /// Common server error. Error number: 1714, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_insert_ignore_select">ER_BINLOG_UNSAFE_INSERT_IGNORE_SELECT</a>.
    er_binlog_unsafe_insert_ignore_select = 1714,

    /// Common server error. Error number: 1715, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_insert_select_update">ER_BINLOG_UNSAFE_INSERT_SELECT_UPDATE</a>.
    er_binlog_unsafe_insert_select_update = 1715,

    /// Common server error. Error number: 1716, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_replace_select">ER_BINLOG_UNSAFE_REPLACE_SELECT</a>.
    er_binlog_unsafe_replace_select = 1716,

    /// Common server error. Error number: 1717, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_create_ignore_select">ER_BINLOG_UNSAFE_CREATE_IGNORE_SELECT</a>.
    er_binlog_unsafe_create_ignore_select = 1717,

    /// Common server error. Error number: 1718, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_create_replace_select">ER_BINLOG_UNSAFE_CREATE_REPLACE_SELECT</a>.
    er_binlog_unsafe_create_replace_select = 1718,

    /// Common server error. Error number: 1719, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_update_ignore">ER_BINLOG_UNSAFE_UPDATE_IGNORE</a>.
    er_binlog_unsafe_update_ignore = 1719,

    /// Common server error. Error number: 1722, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_write_autoinc_select">ER_BINLOG_UNSAFE_WRITE_AUTOINC_SELECT</a>.
    er_binlog_unsafe_write_autoinc_select = 1722,

    /// Common server error. Error number: 1723, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_create_select_autoinc">ER_BINLOG_UNSAFE_CREATE_SELECT_AUTOINC</a>.
    er_binlog_unsafe_create_select_autoinc = 1723,

    /// Common server error. Error number: 1724, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_insert_two_keys">ER_BINLOG_UNSAFE_INSERT_TWO_KEYS</a>.
    er_binlog_unsafe_insert_two_keys = 1724,

    /// Common server error. Error number: 1727, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_unsafe_autoinc_not_first">ER_BINLOG_UNSAFE_AUTOINC_NOT_FIRST</a>.
    er_binlog_unsafe_autoinc_not_first = 1727,

    /// Common server error. Error number: 1728, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cannot_load_from_table_v2">ER_CANNOT_LOAD_FROM_TABLE_V2</a>.
    er_cannot_load_from_table_v2 = 1728,

    /// Common server error. Error number: 1729, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_master_delay_value_out_of_range">ER_MASTER_DELAY_VALUE_OUT_OF_RANGE</a>.
    er_master_delay_value_out_of_range = 1729,

    /// Common server error. Error number: 1730, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_only_fd_and_rbr_events_allowed_in_binlog_statement">ER_ONLY_FD_AND_RBR_EVENTS_ALLOWED_IN_BINLOG_STATEMENT</a>.
    er_only_fd_and_rbr_events_allowed_in_binlog_statement = 1730,

    /// Common server error. Error number: 1731, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_exchange_different_option">ER_PARTITION_EXCHANGE_DIFFERENT_OPTION</a>.
    er_partition_exchange_different_option = 1731,

    /// Common server error. Error number: 1732, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_exchange_part_table">ER_PARTITION_EXCHANGE_PART_TABLE</a>.
    er_partition_exchange_part_table = 1732,

    /// Common server error. Error number: 1733, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_exchange_temp_table">ER_PARTITION_EXCHANGE_TEMP_TABLE</a>.
    er_partition_exchange_temp_table = 1733,

    /// Common server error. Error number: 1734, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_instead_of_subpartition">ER_PARTITION_INSTEAD_OF_SUBPARTITION</a>.
    er_partition_instead_of_subpartition = 1734,

    /// Common server error. Error number: 1735, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_partition">ER_UNKNOWN_PARTITION</a>.
    er_unknown_partition = 1735,

    /// Common server error. Error number: 1736, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_tables_different_metadata">ER_TABLES_DIFFERENT_METADATA</a>.
    er_tables_different_metadata = 1736,

    /// Common server error. Error number: 1737, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_row_does_not_match_partition">ER_ROW_DOES_NOT_MATCH_PARTITION</a>.
    er_row_does_not_match_partition = 1737,

    /// Common server error. Error number: 1738, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_cache_size_greater_than_max">ER_BINLOG_CACHE_SIZE_GREATER_THAN_MAX</a>.
    er_binlog_cache_size_greater_than_max = 1738,

    /// Common server error. Error number: 1739, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_index_not_applicable">ER_WARN_INDEX_NOT_APPLICABLE</a>.
    er_warn_index_not_applicable = 1739,

    /// Common server error. Error number: 1740, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_exchange_foreign_key">ER_PARTITION_EXCHANGE_FOREIGN_KEY</a>.
    er_partition_exchange_foreign_key = 1740,

    /// Common server error. Error number: 1741, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_no_such_key_value">ER_NO_SUCH_KEY_VALUE</a>.
    er_no_such_key_value = 1741,

    /// Common server error. Error number: 1743, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_network_read_event_checksum_failure">ER_NETWORK_READ_EVENT_CHECKSUM_FAILURE</a>.
    er_network_read_event_checksum_failure = 1743,

    /// Common server error. Error number: 1744, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_read_event_checksum_failure">ER_BINLOG_READ_EVENT_CHECKSUM_FAILURE</a>.
    er_binlog_read_event_checksum_failure = 1744,

    /// Common server error. Error number: 1745, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_stmt_cache_size_greater_than_max">ER_BINLOG_STMT_CACHE_SIZE_GREATER_THAN_MAX</a>.
    er_binlog_stmt_cache_size_greater_than_max = 1745,

    /// Common server error. Error number: 1746, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_update_table_in_create_table_select">ER_CANT_UPDATE_TABLE_IN_CREATE_TABLE_SELECT</a>.
    er_cant_update_table_in_create_table_select = 1746,

    /// Common server error. Error number: 1747, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_partition_clause_on_nonpartitioned">ER_PARTITION_CLAUSE_ON_NONPARTITIONED</a>.
    er_partition_clause_on_nonpartitioned = 1747,

    /// Common server error. Error number: 1748, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_row_does_not_match_given_partition_set">ER_ROW_DOES_NOT_MATCH_GIVEN_PARTITION_SET</a>.
    er_row_does_not_match_given_partition_set = 1748,

    /// Common server error. Error number: 1750, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_change_rpl_info_repository_failure">ER_CHANGE_RPL_INFO_REPOSITORY_FAILURE</a>.
    er_change_rpl_info_repository_failure = 1750,

    /// Common server error. Error number: 1751, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warning_not_complete_rollback_with_created_temp_table">ER_WARNING_NOT_COMPLETE_ROLLBACK_WITH_CREATED_TEMP_TABLE</a>.
    er_warning_not_complete_rollback_with_created_temp_table = 1751,

    /// Common server error. Error number: 1752, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warning_not_complete_rollback_with_dropped_temp_table">ER_WARNING_NOT_COMPLETE_ROLLBACK_WITH_DROPPED_TEMP_TABLE</a>.
    er_warning_not_complete_rollback_with_dropped_temp_table = 1752,

    /// Common server error. Error number: 1753, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_feature_is_not_supported">ER_MTS_FEATURE_IS_NOT_SUPPORTED</a>.
    er_mts_feature_is_not_supported = 1753,

    /// Common server error. Error number: 1754, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_updated_dbs_greater_max">ER_MTS_UPDATED_DBS_GREATER_MAX</a>.
    er_mts_updated_dbs_greater_max = 1754,

    /// Common server error. Error number: 1755, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_cant_parallel">ER_MTS_CANT_PARALLEL</a>.
    er_mts_cant_parallel = 1755,

    /// Common server error. Error number: 1756, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_inconsistent_data">ER_MTS_INCONSISTENT_DATA</a>.
    er_mts_inconsistent_data = 1756,

    /// Common server error. Error number: 1757, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fulltext_not_supported_with_partitioning">ER_FULLTEXT_NOT_SUPPORTED_WITH_PARTITIONING</a>.
    er_fulltext_not_supported_with_partitioning = 1757,

    /// Common server error. Error number: 1758, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_da_invalid_condition_number">ER_DA_INVALID_CONDITION_NUMBER</a>.
    er_da_invalid_condition_number = 1758,

    /// Common server error. Error number: 1759, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_insecure_plain_text">ER_INSECURE_PLAIN_TEXT</a>.
    er_insecure_plain_text = 1759,

    /// Common server error. Error number: 1760, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_insecure_change_master">ER_INSECURE_CHANGE_MASTER</a>.
    er_insecure_change_master = 1760,

    /// Common server error. Error number: 1761, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_foreign_duplicate_key_with_child_info">ER_FOREIGN_DUPLICATE_KEY_WITH_CHILD_INFO</a>.
    er_foreign_duplicate_key_with_child_info = 1761,

    /// Common server error. Error number: 1762, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_foreign_duplicate_key_without_child_info">ER_FOREIGN_DUPLICATE_KEY_WITHOUT_CHILD_INFO</a>.
    er_foreign_duplicate_key_without_child_info = 1762,

    /// Common server error. Error number: 1763, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sqlthread_with_secure_slave">ER_SQLTHREAD_WITH_SECURE_SLAVE</a>.
    er_sqlthread_with_secure_slave = 1763,

    /// Common server error. Error number: 1764, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_has_no_ft">ER_TABLE_HAS_NO_FT</a>.
    er_table_has_no_ft = 1764,

    /// Common server error. Error number: 1765, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_variable_not_settable_in_sf_or_trigger">ER_VARIABLE_NOT_SETTABLE_IN_SF_OR_TRIGGER</a>.
    er_variable_not_settable_in_sf_or_trigger = 1765,

    /// Common server error. Error number: 1766, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_variable_not_settable_in_transaction">ER_VARIABLE_NOT_SETTABLE_IN_TRANSACTION</a>.
    er_variable_not_settable_in_transaction = 1766,

    /// Common server error. Error number: 1767, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_next_is_not_in_gtid_next_list">ER_GTID_NEXT_IS_NOT_IN_GTID_NEXT_LIST</a>.
    er_gtid_next_is_not_in_gtid_next_list = 1767,

    /// Common server error. Error number: 1769, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_set_statement_cannot_invoke_function">ER_SET_STATEMENT_CANNOT_INVOKE_FUNCTION</a>.
    er_set_statement_cannot_invoke_function = 1769,

    /// Common server error. Error number: 1770, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_next_cant_be_automatic_if_gtid_next_list_is_non_null">ER_GTID_NEXT_CANT_BE_AUTOMATIC_IF_GTID_NEXT_LIST_IS_NON_NULL</a>.
    er_gtid_next_cant_be_automatic_if_gtid_next_list_is_non_null = 1770,

    /// Common server error. Error number: 1771, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_skipping_logged_transaction">ER_SKIPPING_LOGGED_TRANSACTION</a>.
    er_skipping_logged_transaction = 1771,

    /// Common server error. Error number: 1772, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_malformed_gtid_set_specification">ER_MALFORMED_GTID_SET_SPECIFICATION</a>.
    er_malformed_gtid_set_specification = 1772,

    /// Common server error. Error number: 1773, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_malformed_gtid_set_encoding">ER_MALFORMED_GTID_SET_ENCODING</a>.
    er_malformed_gtid_set_encoding = 1773,

    /// Common server error. Error number: 1774, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_malformed_gtid_specification">ER_MALFORMED_GTID_SPECIFICATION</a>.
    er_malformed_gtid_specification = 1774,

    /// Common server error. Error number: 1775, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gno_exhausted">ER_GNO_EXHAUSTED</a>.
    er_gno_exhausted = 1775,

    /// Common server error. Error number: 1776, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_bad_slave_auto_position">ER_BAD_SLAVE_AUTO_POSITION</a>.
    er_bad_slave_auto_position = 1776,

    /// Common server error. Error number: 1778, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_do_implicit_commit_in_trx_when_gtid_next_is_set">ER_CANT_DO_IMPLICIT_COMMIT_IN_TRX_WHEN_GTID_NEXT_IS_SET</a>.
    er_cant_do_implicit_commit_in_trx_when_gtid_next_is_set = 1778,

    /// Common server error. Error number: 1780, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_mode_requires_binlog">ER_GTID_MODE_REQUIRES_BINLOG</a>.
    er_gtid_mode_requires_binlog = 1780,

    /// Common server error. Error number: 1781, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_gtid_next_to_gtid_when_gtid_mode_is_off">ER_CANT_SET_GTID_NEXT_TO_GTID_WHEN_GTID_MODE_IS_OFF</a>.
    er_cant_set_gtid_next_to_gtid_when_gtid_mode_is_off = 1781,

    /// Common server error. Error number: 1782, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_gtid_next_to_anonymous_when_gtid_mode_is_on">ER_CANT_SET_GTID_NEXT_TO_ANONYMOUS_WHEN_GTID_MODE_IS_ON</a>.
    er_cant_set_gtid_next_to_anonymous_when_gtid_mode_is_on = 1782,

    /// Common server error. Error number: 1783, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_gtid_next_list_to_non_null_when_gtid_mode_is_off">ER_CANT_SET_GTID_NEXT_LIST_TO_NON_NULL_WHEN_GTID_MODE_IS_OFF</a>.
    er_cant_set_gtid_next_list_to_non_null_when_gtid_mode_is_off = 1783,

    /// Common server error. Error number: 1785, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_unsafe_non_transactional_table">ER_GTID_UNSAFE_NON_TRANSACTIONAL_TABLE</a>.
    er_gtid_unsafe_non_transactional_table = 1785,

    /// Common server error. Error number: 1786, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_unsafe_create_select">ER_GTID_UNSAFE_CREATE_SELECT</a>.
    er_gtid_unsafe_create_select = 1786,

    /// Common server error. Error number: 1787, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_unsafe_create_drop_temporary_table_in_transaction">ER_GTID_UNSAFE_CREATE_DROP_TEMPORARY_TABLE_IN_TRANSACTION</a>.
    er_gtid_unsafe_create_drop_temporary_table_in_transaction = 1787,

    /// Common server error. Error number: 1788, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_mode_can_only_change_one_step_at_a_time">ER_GTID_MODE_CAN_ONLY_CHANGE_ONE_STEP_AT_A_TIME</a>.
    er_gtid_mode_can_only_change_one_step_at_a_time = 1788,

    /// Common server error. Error number: 1789, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_master_has_purged_required_gtids">ER_MASTER_HAS_PURGED_REQUIRED_GTIDS</a>.
    er_master_has_purged_required_gtids = 1789,

    /// Common server error. Error number: 1790, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_gtid_next_when_owning_gtid">ER_CANT_SET_GTID_NEXT_WHEN_OWNING_GTID</a>.
    er_cant_set_gtid_next_when_owning_gtid = 1790,

    /// Common server error. Error number: 1791, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_explain_format">ER_UNKNOWN_EXPLAIN_FORMAT</a>.
    er_unknown_explain_format = 1791,

    /// Common server error. Error number: 1792, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_execute_in_read_only_transaction">ER_CANT_EXECUTE_IN_READ_ONLY_TRANSACTION</a>.
    er_cant_execute_in_read_only_transaction = 1792,

    /// Common server error. Error number: 1793, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_too_long_table_partition_comment">ER_TOO_LONG_TABLE_PARTITION_COMMENT</a>.
    er_too_long_table_partition_comment = 1793,

    /// Common server error. Error number: 1794, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_configuration">ER_SLAVE_CONFIGURATION</a>.
    er_slave_configuration = 1794,

    /// Common server error. Error number: 1795, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_ft_limit">ER_INNODB_FT_LIMIT</a>.
    er_innodb_ft_limit = 1795,

    /// Common server error. Error number: 1796, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_no_ft_temp_table">ER_INNODB_NO_FT_TEMP_TABLE</a>.
    er_innodb_no_ft_temp_table = 1796,

    /// Common server error. Error number: 1797, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_ft_wrong_docid_column">ER_INNODB_FT_WRONG_DOCID_COLUMN</a>.
    er_innodb_ft_wrong_docid_column = 1797,

    /// Common server error. Error number: 1798, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_ft_wrong_docid_index">ER_INNODB_FT_WRONG_DOCID_INDEX</a>.
    er_innodb_ft_wrong_docid_index = 1798,

    /// Common server error. Error number: 1799, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_online_log_too_big">ER_INNODB_ONLINE_LOG_TOO_BIG</a>.
    er_innodb_online_log_too_big = 1799,

    /// Common server error. Error number: 1800, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_alter_algorithm">ER_UNKNOWN_ALTER_ALGORITHM</a>.
    er_unknown_alter_algorithm = 1800,

    /// Common server error. Error number: 1801, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_unknown_alter_lock">ER_UNKNOWN_ALTER_LOCK</a>.
    er_unknown_alter_lock = 1801,

    /// Common server error. Error number: 1802, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_change_master_cant_run_with_gaps">ER_MTS_CHANGE_MASTER_CANT_RUN_WITH_GAPS</a>.
    er_mts_change_master_cant_run_with_gaps = 1802,

    /// Common server error. Error number: 1803, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_recovery_failure">ER_MTS_RECOVERY_FAILURE</a>.
    er_mts_recovery_failure = 1803,

    /// Common server error. Error number: 1804, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_reset_workers">ER_MTS_RESET_WORKERS</a>.
    er_mts_reset_workers = 1804,

    /// Common server error. Error number: 1805, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_col_count_doesnt_match_corrupted_v2">ER_COL_COUNT_DOESNT_MATCH_CORRUPTED_V2</a>.
    er_col_count_doesnt_match_corrupted_v2 = 1805,

    /// Common server error. Error number: 1806, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_silent_retry_transaction">ER_SLAVE_SILENT_RETRY_TRANSACTION</a>.
    er_slave_silent_retry_transaction = 1806,

    /// Common server error. Error number: 1808, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_schema_mismatch">ER_TABLE_SCHEMA_MISMATCH</a>.
    er_table_schema_mismatch = 1808,

    /// Common server error. Error number: 1809, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_in_system_tablespace">ER_TABLE_IN_SYSTEM_TABLESPACE</a>.
    er_table_in_system_tablespace = 1809,

    /// Common server error. Error number: 1810, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_io_read_error">ER_IO_READ_ERROR</a>.
    er_io_read_error = 1810,

    /// Common server error. Error number: 1811, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_io_write_error">ER_IO_WRITE_ERROR</a>.
    er_io_write_error = 1811,

    /// Common server error. Error number: 1812, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_tablespace_missing">ER_TABLESPACE_MISSING</a>.
    er_tablespace_missing = 1812,

    /// Common server error. Error number: 1813, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_tablespace_exists">ER_TABLESPACE_EXISTS</a>.
    er_tablespace_exists = 1813,

    /// Common server error. Error number: 1814, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_tablespace_discarded">ER_TABLESPACE_DISCARDED</a>.
    er_tablespace_discarded = 1814,

    /// Common server error. Error number: 1815, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_internal_error">ER_INTERNAL_ERROR</a>.
    er_internal_error = 1815,

    /// Common server error. Error number: 1816, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_import_error">ER_INNODB_IMPORT_ERROR</a>.
    er_innodb_import_error = 1816,

    /// Common server error. Error number: 1817, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_index_corrupt">ER_INNODB_INDEX_CORRUPT</a>.
    er_innodb_index_corrupt = 1817,

    /// Common server error. Error number: 1818, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_invalid_year_column_length">ER_INVALID_YEAR_COLUMN_LENGTH</a>.
    er_invalid_year_column_length = 1818,

    /// Common server error. Error number: 1819, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_not_valid_password">ER_NOT_VALID_PASSWORD</a>.
    er_not_valid_password = 1819,

    /// Common server error. Error number: 1820, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_must_change_password">ER_MUST_CHANGE_PASSWORD</a>.
    er_must_change_password = 1820,

    /// Common server error. Error number: 1821, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_no_index_child">ER_FK_NO_INDEX_CHILD</a>.
    er_fk_no_index_child = 1821,

    /// Common server error. Error number: 1822, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_no_index_parent">ER_FK_NO_INDEX_PARENT</a>.
    er_fk_no_index_parent = 1822,

    /// Common server error. Error number: 1823, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_fail_add_system">ER_FK_FAIL_ADD_SYSTEM</a>.
    er_fk_fail_add_system = 1823,

    /// Common server error. Error number: 1824, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_cannot_open_parent">ER_FK_CANNOT_OPEN_PARENT</a>.
    er_fk_cannot_open_parent = 1824,

    /// Common server error. Error number: 1825, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_incorrect_option">ER_FK_INCORRECT_OPTION</a>.
    er_fk_incorrect_option = 1825,

    /// Common server error. Error number: 1827, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_password_format">ER_PASSWORD_FORMAT</a>.
    er_password_format = 1827,

    /// Common server error. Error number: 1828, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_column_cannot_drop">ER_FK_COLUMN_CANNOT_DROP</a>.
    er_fk_column_cannot_drop = 1828,

    /// Common server error. Error number: 1829, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_column_cannot_drop_child">ER_FK_COLUMN_CANNOT_DROP_CHILD</a>.
    er_fk_column_cannot_drop_child = 1829,

    /// Common server error. Error number: 1830, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_column_not_null">ER_FK_COLUMN_NOT_NULL</a>.
    er_fk_column_not_null = 1830,

    /// Common server error. Error number: 1831, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_index">ER_DUP_INDEX</a>.
    er_dup_index = 1831,

    /// Common server error. Error number: 1832, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_column_cannot_change">ER_FK_COLUMN_CANNOT_CHANGE</a>.
    er_fk_column_cannot_change = 1832,

    /// Common server error. Error number: 1833, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_fk_column_cannot_change_child">ER_FK_COLUMN_CANNOT_CHANGE_CHILD</a>.
    er_fk_column_cannot_change_child = 1833,

    /// Common server error. Error number: 1835, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_malformed_packet">ER_MALFORMED_PACKET</a>.
    er_malformed_packet = 1835,

    /// Common server error. Error number: 1836, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_read_only_mode">ER_READ_ONLY_MODE</a>.
    er_read_only_mode = 1836,

    /// Common server error. Error number: 1838, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_variable_not_settable_in_sp">ER_VARIABLE_NOT_SETTABLE_IN_SP</a>.
    er_variable_not_settable_in_sp = 1838,

    /// Common server error. Error number: 1839, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_gtid_purged_when_gtid_mode_is_off">ER_CANT_SET_GTID_PURGED_WHEN_GTID_MODE_IS_OFF</a>.
    er_cant_set_gtid_purged_when_gtid_mode_is_off = 1839,

    /// Common server error. Error number: 1840, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_gtid_purged_when_gtid_executed_is_not_empty">ER_CANT_SET_GTID_PURGED_WHEN_GTID_EXECUTED_IS_NOT_EMPTY</a>.
    er_cant_set_gtid_purged_when_gtid_executed_is_not_empty = 1840,

    /// Common server error. Error number: 1841, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_cant_set_gtid_purged_when_owned_gtids_is_not_empty">ER_CANT_SET_GTID_PURGED_WHEN_OWNED_GTIDS_IS_NOT_EMPTY</a>.
    er_cant_set_gtid_purged_when_owned_gtids_is_not_empty = 1841,

    /// Common server error. Error number: 1842, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_purged_was_changed">ER_GTID_PURGED_WAS_CHANGED</a>.
    er_gtid_purged_was_changed = 1842,

    /// Common server error. Error number: 1843, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_gtid_executed_was_changed">ER_GTID_EXECUTED_WAS_CHANGED</a>.
    er_gtid_executed_was_changed = 1843,

    /// Common server error. Error number: 1844, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_stmt_mode_and_no_repl_tables">ER_BINLOG_STMT_MODE_AND_NO_REPL_TABLES</a>.
    er_binlog_stmt_mode_and_no_repl_tables = 1844,

    /// Common server error. Error number: 1845, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported">ER_ALTER_OPERATION_NOT_SUPPORTED</a>.
    er_alter_operation_not_supported = 1845,

    /// Common server error. Error number: 1846, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON</a>.
    er_alter_operation_not_supported_reason = 1846,

    /// Common server error. Error number: 1847, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_copy">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_COPY</a>.
    er_alter_operation_not_supported_reason_copy = 1847,

    /// Common server error. Error number: 1848, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_partition">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_PARTITION</a>.
    er_alter_operation_not_supported_reason_partition = 1848,

    /// Common server error. Error number: 1849, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_fk_rename">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_FK_RENAME</a>.
    er_alter_operation_not_supported_reason_fk_rename = 1849,

    /// Common server error. Error number: 1850, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_column_type">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_COLUMN_TYPE</a>.
    er_alter_operation_not_supported_reason_column_type = 1850,

    /// Common server error. Error number: 1851, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_fk_check">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_FK_CHECK</a>.
    er_alter_operation_not_supported_reason_fk_check = 1851,

    /// Common server error. Error number: 1853, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_nopk">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_NOPK</a>.
    er_alter_operation_not_supported_reason_nopk = 1853,

    /// Common server error. Error number: 1854, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_autoinc">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_AUTOINC</a>.
    er_alter_operation_not_supported_reason_autoinc = 1854,

    /// Common server error. Error number: 1855, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_hidden_fts">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_HIDDEN_FTS</a>.
    er_alter_operation_not_supported_reason_hidden_fts = 1855,

    /// Common server error. Error number: 1856, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_change_fts">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_CHANGE_FTS</a>.
    er_alter_operation_not_supported_reason_change_fts = 1856,

    /// Common server error. Error number: 1857, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_fts">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_FTS</a>.
    er_alter_operation_not_supported_reason_fts = 1857,

    /// Common server error. Error number: 1858, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_sql_slave_skip_counter_not_settable_in_gtid_mode">ER_SQL_SLAVE_SKIP_COUNTER_NOT_SETTABLE_IN_GTID_MODE</a>.
    er_sql_slave_skip_counter_not_settable_in_gtid_mode = 1858,

    /// Common server error. Error number: 1859, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_dup_unknown_in_index">ER_DUP_UNKNOWN_IN_INDEX</a>.
    er_dup_unknown_in_index = 1859,

    /// Common server error. Error number: 1860, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_ident_causes_too_long_path">ER_IDENT_CAUSES_TOO_LONG_PATH</a>.
    er_ident_causes_too_long_path = 1860,

    /// Common server error. Error number: 1861, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_alter_operation_not_supported_reason_not_null">ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_NOT_NULL</a>.
    er_alter_operation_not_supported_reason_not_null = 1861,

    /// Common server error. Error number: 1862, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_must_change_password_login">ER_MUST_CHANGE_PASSWORD_LOGIN</a>.
    er_must_change_password_login = 1862,

    /// Common server error. Error number: 1863, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_row_in_wrong_partition">ER_ROW_IN_WRONG_PARTITION</a>.
    er_row_in_wrong_partition = 1863,

    /// Common server error. Error number: 1864, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_mts_event_bigger_pending_jobs_size_max">ER_MTS_EVENT_BIGGER_PENDING_JOBS_SIZE_MAX</a>.
    er_mts_event_bigger_pending_jobs_size_max = 1864,

    /// Common server error. Error number: 1865, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_no_ft_uses_parser">ER_INNODB_NO_FT_USES_PARSER</a>.
    er_innodb_no_ft_uses_parser = 1865,

    /// Common server error. Error number: 1866, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_binlog_logical_corruption">ER_BINLOG_LOGICAL_CORRUPTION</a>.
    er_binlog_logical_corruption = 1866,

    /// Common server error. Error number: 1867, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_purge_log_in_use">ER_WARN_PURGE_LOG_IN_USE</a>.
    er_warn_purge_log_in_use = 1867,

    /// Common server error. Error number: 1868, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_warn_purge_log_is_active">ER_WARN_PURGE_LOG_IS_ACTIVE</a>.
    er_warn_purge_log_is_active = 1868,

    /// Common server error. Error number: 1869, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_auto_increment_conflict">ER_AUTO_INCREMENT_CONFLICT</a>.
    er_auto_increment_conflict = 1869,

    /// Common server error. Error number: 1870, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_warn_on_blockhole_in_rbr">WARN_ON_BLOCKHOLE_IN_RBR</a>.
    warn_on_blockhole_in_rbr = 1870,

    /// Common server error. Error number: 1871, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_mi_init_repository">ER_SLAVE_MI_INIT_REPOSITORY</a>.
    er_slave_mi_init_repository = 1871,

    /// Common server error. Error number: 1872, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_slave_rli_init_repository">ER_SLAVE_RLI_INIT_REPOSITORY</a>.
    er_slave_rli_init_repository = 1872,

    /// Common server error. Error number: 1873, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_access_denied_change_user_error">ER_ACCESS_DENIED_CHANGE_USER_ERROR</a>.
    er_access_denied_change_user_error = 1873,

    /// Common server error. Error number: 1874, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_read_only">ER_INNODB_READ_ONLY</a>.
    er_innodb_read_only = 1874,

    /// Common server error. Error number: 1875, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stop_slave_sql_thread_timeout">ER_STOP_SLAVE_SQL_THREAD_TIMEOUT</a>.
    er_stop_slave_sql_thread_timeout = 1875,

    /// Common server error. Error number: 1876, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_stop_slave_io_thread_timeout">ER_STOP_SLAVE_IO_THREAD_TIMEOUT</a>.
    er_stop_slave_io_thread_timeout = 1876,

    /// Common server error. Error number: 1877, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_table_corrupt">ER_TABLE_CORRUPT</a>.
    er_table_corrupt = 1877,

    /// Common server error. Error number: 1878, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_temp_file_write_failure">ER_TEMP_FILE_WRITE_FAILURE</a>.
    er_temp_file_write_failure = 1878,

    /// Common server error. Error number: 1879, symbol:
    /// <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html#error_er_innodb_ft_aux_not_hex_id">ER_INNODB_FT_AUX_NOT_HEX_ID</a>.
    er_innodb_ft_aux_not_hex_id = 1879,

};

BOOST_MYSQL_DECL
const boost::system::error_category& get_common_server_category() noexcept;

/// Creates an \ref error_code from a \ref common_server_errc.
inline error_code make_error_code(common_server_errc error)
{
    return error_code(static_cast<int>(error), get_common_server_category());
}

}  // namespace mysql

#ifndef BOOST_MYSQL_DOXYGEN
namespace system {

template <>
struct is_error_code_enum<::boost::mysql::common_server_errc>
{
    static constexpr bool value = true;
};

}  // namespace system
#endif

}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/error_categories.ipp>
#endif

#endif
