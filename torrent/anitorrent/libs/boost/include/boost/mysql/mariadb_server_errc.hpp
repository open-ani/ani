//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_MARIADB_SERVER_ERRC_HPP
#define BOOST_MYSQL_MARIADB_SERVER_ERRC_HPP

namespace boost {
namespace mysql {

namespace mariadb_server_errc {

/// Server error specific to mariadb. Error number: 1076, symbol: ER_BINLOG_CANT_DELETE_GTID_DOMAIN.
constexpr int er_binlog_cant_delete_gtid_domain = 1076;

/// Server error specific to mariadb. Error number: 1120, symbol: ER_WRONG_OUTER_JOIN.
constexpr int er_wrong_outer_join = 1120;

/// Server error specific to mariadb. Error number: 1150, symbol: ER_DELAYED_CANT_CHANGE_LOCK.
constexpr int er_delayed_cant_change_lock = 1150;

/// Server error specific to mariadb. Error number: 1151, symbol: ER_TOO_MANY_DELAYED_THREADS.
constexpr int er_too_many_delayed_threads = 1151;

/// Server error specific to mariadb. Error number: 1165, symbol: ER_DELAYED_INSERT_TABLE_LOCKED.
constexpr int er_delayed_insert_table_locked = 1165;

/// Server error specific to mariadb. Error number: 1176, symbol: ER_KEY_DOES_NOT_EXISTS.
constexpr int er_key_does_not_exists = 1176;

/// Server error specific to mariadb. Error number: 1349, symbol: ER_VIEW_SELECT_DERIVED.
constexpr int er_view_select_derived = 1349;

/// Server error specific to mariadb. Error number: 1487, symbol: ER_NOT_CONSTANT_EXPRESSION.
constexpr int er_not_constant_expression = 1487;

/// Server error specific to mariadb. Error number: 1506, symbol: ER_FEATURE_NOT_SUPPORTED_WITH_PARTITIONING.
constexpr int er_feature_not_supported_with_partitioning = 1506;

/// Server error specific to mariadb. Error number: 1593, symbol: ER_SLAVE_FATAL_ERROR.
constexpr int er_slave_fatal_error = 1593;

/// Server error specific to mariadb. Error number: 1611, symbol: ER_LOAD_DATA_INVALID_COLUMN.
constexpr int er_load_data_invalid_column = 1611;

/// Server error specific to mariadb. Error number: 1669, symbol: ER_BINLOG_UNSAFE_INSERT_DELAYED.
constexpr int er_binlog_unsafe_insert_delayed = 1669;

/// Server error specific to mariadb. Error number: 1726, symbol: ER_VERS_NOT_ALLOWED.
constexpr int er_vers_not_allowed = 1726;

/// Server error specific to mariadb. Error number: 1742, symbol: ER_VALUE_TOO_LONG.
constexpr int er_value_too_long = 1742;

/// Server error specific to mariadb. Error number: 1768, symbol: ER_CANT_CHANGE_GTID_NEXT_IN_TRANSACTION_WHEN_GTID_NEXT_LIST_IS_NULL.
constexpr int er_cant_change_gtid_next_in_transaction_when_gtid_next_list_is_null = 1768;

/// Server error specific to mariadb. Error number: 1777, symbol: ER_AUTO_POSITION_REQUIRES_GTID_MODE_ON.
constexpr int er_auto_position_requires_gtid_mode_on = 1777;

/// Server error specific to mariadb. Error number: 1779, symbol: ER_GTID_MODE_2_OR_3_REQUIRES_ENFORCE_GTID_CONSISTENCY_ON.
constexpr int er_gtid_mode_2_or_3_requires_enforce_gtid_consistency_on = 1779;

/// Server error specific to mariadb. Error number: 1784, symbol: ER_FOUND_GTID_EVENT_WHEN_GTID_MODE_IS_OFF.
constexpr int er_found_gtid_event_when_gtid_mode_is_off = 1784;

/// Server error specific to mariadb. Error number: 1826, symbol: ER_DUP_CONSTRAINT_NAME.
constexpr int er_dup_constraint_name = 1826;

/// Server error specific to mariadb. Error number: 1834, symbol: ER_FK_CANNOT_DELETE_PARENT.
constexpr int er_fk_cannot_delete_parent = 1834;

/// Server error specific to mariadb. Error number: 1837, symbol: ER_GTID_NEXT_TYPE_UNDEFINED_GROUP.
constexpr int er_gtid_next_type_undefined_group = 1837;

/// Server error specific to mariadb. Error number: 1852, symbol: ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_IGNORE.
constexpr int er_alter_operation_not_supported_reason_ignore = 1852;

/// Server error specific to mariadb. Error number: 1901, symbol: ER_GENERATED_COLUMN_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_generated_column_function_is_not_allowed = 1901;

/// Server error specific to mariadb. Error number: 1903, symbol: ER_PRIMARY_KEY_BASED_ON_GENERATED_COLUMN.
constexpr int er_primary_key_based_on_generated_column = 1903;

/// Server error specific to mariadb. Error number: 1904, symbol: ER_KEY_BASED_ON_GENERATED_VIRTUAL_COLUMN.
constexpr int er_key_based_on_generated_virtual_column = 1904;

/// Server error specific to mariadb. Error number: 1905, symbol: ER_WRONG_FK_OPTION_FOR_GENERATED_COLUMN.
constexpr int er_wrong_fk_option_for_generated_column = 1905;

/// Server error specific to mariadb. Error number: 1906, symbol: ER_WARNING_NON_DEFAULT_VALUE_FOR_GENERATED_COLUMN.
constexpr int er_warning_non_default_value_for_generated_column = 1906;

/// Server error specific to mariadb. Error number: 1907, symbol: ER_UNSUPPORTED_ACTION_ON_GENERATED_COLUMN.
constexpr int er_unsupported_action_on_generated_column = 1907;

/// Server error specific to mariadb. Error number: 1910, symbol: ER_UNSUPPORTED_ENGINE_FOR_GENERATED_COLUMNS.
constexpr int er_unsupported_engine_for_generated_columns = 1910;

/// Server error specific to mariadb. Error number: 1911, symbol: ER_UNKNOWN_OPTION.
constexpr int er_unknown_option = 1911;

/// Server error specific to mariadb. Error number: 1912, symbol: ER_BAD_OPTION_VALUE.
constexpr int er_bad_option_value = 1912;

/// Server error specific to mariadb. Error number: 1916, symbol: ER_DATA_OVERFLOW.
constexpr int er_data_overflow = 1916;

/// Server error specific to mariadb. Error number: 1917, symbol: ER_DATA_TRUNCATED.
constexpr int er_data_truncated = 1917;

/// Server error specific to mariadb. Error number: 1918, symbol: ER_BAD_DATA.
constexpr int er_bad_data = 1918;

/// Server error specific to mariadb. Error number: 1919, symbol: ER_DYN_COL_WRONG_FORMAT.
constexpr int er_dyn_col_wrong_format = 1919;

/// Server error specific to mariadb. Error number: 1920, symbol: ER_DYN_COL_IMPLEMENTATION_LIMIT.
constexpr int er_dyn_col_implementation_limit = 1920;

/// Server error specific to mariadb. Error number: 1921, symbol: ER_DYN_COL_DATA.
constexpr int er_dyn_col_data = 1921;

/// Server error specific to mariadb. Error number: 1922, symbol: ER_DYN_COL_WRONG_CHARSET.
constexpr int er_dyn_col_wrong_charset = 1922;

/// Server error specific to mariadb. Error number: 1923, symbol: ER_ILLEGAL_SUBQUERY_OPTIMIZER_SWITCHES.
constexpr int er_illegal_subquery_optimizer_switches = 1923;

/// Server error specific to mariadb. Error number: 1924, symbol: ER_QUERY_CACHE_IS_DISABLED.
constexpr int er_query_cache_is_disabled = 1924;

/// Server error specific to mariadb. Error number: 1925, symbol: ER_QUERY_CACHE_IS_GLOBALY_DISABLED.
constexpr int er_query_cache_is_globaly_disabled = 1925;

/// Server error specific to mariadb. Error number: 1926, symbol: ER_VIEW_ORDERBY_IGNORED.
constexpr int er_view_orderby_ignored = 1926;

/// Server error specific to mariadb. Error number: 1927, symbol: ER_CONNECTION_KILLED.
constexpr int er_connection_killed = 1927;

/// Server error specific to mariadb. Error number: 1929, symbol: ER_INSIDE_TRANSACTION_PREVENTS_SWITCH_SKIP_REPLICATION.
constexpr int er_inside_transaction_prevents_switch_skip_replication = 1929;

/// Server error specific to mariadb. Error number: 1930, symbol: ER_STORED_FUNCTION_PREVENTS_SWITCH_SKIP_REPLICATION.
constexpr int er_stored_function_prevents_switch_skip_replication = 1930;

/// Server error specific to mariadb. Error number: 1931, symbol: ER_QUERY_EXCEEDED_ROWS_EXAMINED_LIMIT.
constexpr int er_query_exceeded_rows_examined_limit = 1931;

/// Server error specific to mariadb. Error number: 1932, symbol: ER_NO_SUCH_TABLE_IN_ENGINE.
constexpr int er_no_such_table_in_engine = 1932;

/// Server error specific to mariadb. Error number: 1933, symbol: ER_TARGET_NOT_EXPLAINABLE.
constexpr int er_target_not_explainable = 1933;

/// Server error specific to mariadb. Error number: 1934, symbol: ER_CONNECTION_ALREADY_EXISTS.
constexpr int er_connection_already_exists = 1934;

/// Server error specific to mariadb. Error number: 1935, symbol: ER_MASTER_LOG_PREFIX.
constexpr int er_master_log_prefix = 1935;

/// Server error specific to mariadb. Error number: 1936, symbol: ER_CANT_START_STOP_SLAVE.
constexpr int er_cant_start_stop_slave = 1936;

/// Server error specific to mariadb. Error number: 1937, symbol: ER_SLAVE_STARTED.
constexpr int er_slave_started = 1937;

/// Server error specific to mariadb. Error number: 1938, symbol: ER_SLAVE_STOPPED.
constexpr int er_slave_stopped = 1938;

/// Server error specific to mariadb. Error number: 1939, symbol: ER_SQL_DISCOVER_ERROR.
constexpr int er_sql_discover_error = 1939;

/// Server error specific to mariadb. Error number: 1940, symbol: ER_FAILED_GTID_STATE_INIT.
constexpr int er_failed_gtid_state_init = 1940;

/// Server error specific to mariadb. Error number: 1941, symbol: ER_INCORRECT_GTID_STATE.
constexpr int er_incorrect_gtid_state = 1941;

/// Server error specific to mariadb. Error number: 1942, symbol: ER_CANNOT_UPDATE_GTID_STATE.
constexpr int er_cannot_update_gtid_state = 1942;

/// Server error specific to mariadb. Error number: 1943, symbol: ER_DUPLICATE_GTID_DOMAIN.
constexpr int er_duplicate_gtid_domain = 1943;

/// Server error specific to mariadb. Error number: 1944, symbol: ER_GTID_OPEN_TABLE_FAILED.
constexpr int er_gtid_open_table_failed = 1944;

/// Server error specific to mariadb. Error number: 1945, symbol: ER_GTID_POSITION_NOT_FOUND_IN_BINLOG.
constexpr int er_gtid_position_not_found_in_binlog = 1945;

/// Server error specific to mariadb. Error number: 1946, symbol: ER_CANNOT_LOAD_SLAVE_GTID_STATE.
constexpr int er_cannot_load_slave_gtid_state = 1946;

/// Server error specific to mariadb. Error number: 1947, symbol: ER_MASTER_GTID_POS_CONFLICTS_WITH_BINLOG.
constexpr int er_master_gtid_pos_conflicts_with_binlog = 1947;

/// Server error specific to mariadb. Error number: 1948, symbol: ER_MASTER_GTID_POS_MISSING_DOMAIN.
constexpr int er_master_gtid_pos_missing_domain = 1948;

/// Server error specific to mariadb. Error number: 1949, symbol: ER_UNTIL_REQUIRES_USING_GTID.
constexpr int er_until_requires_using_gtid = 1949;

/// Server error specific to mariadb. Error number: 1950, symbol: ER_GTID_STRICT_OUT_OF_ORDER.
constexpr int er_gtid_strict_out_of_order = 1950;

/// Server error specific to mariadb. Error number: 1951, symbol: ER_GTID_START_FROM_BINLOG_HOLE.
constexpr int er_gtid_start_from_binlog_hole = 1951;

/// Server error specific to mariadb. Error number: 1952, symbol: ER_SLAVE_UNEXPECTED_MASTER_SWITCH.
constexpr int er_slave_unexpected_master_switch = 1952;

/// Server error specific to mariadb. Error number: 1953, symbol: ER_INSIDE_TRANSACTION_PREVENTS_SWITCH_GTID_DOMAIN_ID_SEQ_NO.
constexpr int er_inside_transaction_prevents_switch_gtid_domain_id_seq_no = 1953;

/// Server error specific to mariadb. Error number: 1954, symbol: ER_STORED_FUNCTION_PREVENTS_SWITCH_GTID_DOMAIN_ID_SEQ_NO.
constexpr int er_stored_function_prevents_switch_gtid_domain_id_seq_no = 1954;

/// Server error specific to mariadb. Error number: 1955, symbol: ER_GTID_POSITION_NOT_FOUND_IN_BINLOG2.
constexpr int er_gtid_position_not_found_in_binlog2 = 1955;

/// Server error specific to mariadb. Error number: 1956, symbol: ER_BINLOG_MUST_BE_EMPTY.
constexpr int er_binlog_must_be_empty = 1956;

/// Server error specific to mariadb. Error number: 1957, symbol: ER_NO_SUCH_QUERY.
constexpr int er_no_such_query = 1957;

/// Server error specific to mariadb. Error number: 1958, symbol: ER_BAD_BASE64_DATA.
constexpr int er_bad_base64_data = 1958;

/// Server error specific to mariadb. Error number: 1959, symbol: ER_INVALID_ROLE.
constexpr int er_invalid_role = 1959;

/// Server error specific to mariadb. Error number: 1960, symbol: ER_INVALID_CURRENT_USER.
constexpr int er_invalid_current_user = 1960;

/// Server error specific to mariadb. Error number: 1961, symbol: ER_CANNOT_GRANT_ROLE.
constexpr int er_cannot_grant_role = 1961;

/// Server error specific to mariadb. Error number: 1962, symbol: ER_CANNOT_REVOKE_ROLE.
constexpr int er_cannot_revoke_role = 1962;

/// Server error specific to mariadb. Error number: 1963, symbol: ER_CHANGE_SLAVE_PARALLEL_THREADS_ACTIVE.
constexpr int er_change_slave_parallel_threads_active = 1963;

/// Server error specific to mariadb. Error number: 1964, symbol: ER_PRIOR_COMMIT_FAILED.
constexpr int er_prior_commit_failed = 1964;

/// Server error specific to mariadb. Error number: 1965, symbol: ER_IT_IS_A_VIEW.
constexpr int er_it_is_a_view = 1965;

/// Server error specific to mariadb. Error number: 1966, symbol: ER_SLAVE_SKIP_NOT_IN_GTID.
constexpr int er_slave_skip_not_in_gtid = 1966;

/// Server error specific to mariadb. Error number: 1967, symbol: ER_TABLE_DEFINITION_TOO_BIG.
constexpr int er_table_definition_too_big = 1967;

/// Server error specific to mariadb. Error number: 1968, symbol: ER_PLUGIN_INSTALLED.
constexpr int er_plugin_installed = 1968;

/// Server error specific to mariadb. Error number: 1969, symbol: ER_STATEMENT_TIMEOUT.
constexpr int er_statement_timeout = 1969;

/// Server error specific to mariadb. Error number: 1970, symbol: ER_SUBQUERIES_NOT_SUPPORTED.
constexpr int er_subqueries_not_supported = 1970;

/// Server error specific to mariadb. Error number: 1971, symbol: ER_SET_STATEMENT_NOT_SUPPORTED.
constexpr int er_set_statement_not_supported = 1971;

/// Server error specific to mariadb. Error number: 1973, symbol: ER_USER_CREATE_EXISTS.
constexpr int er_user_create_exists = 1973;

/// Server error specific to mariadb. Error number: 1974, symbol: ER_USER_DROP_EXISTS.
constexpr int er_user_drop_exists = 1974;

/// Server error specific to mariadb. Error number: 1975, symbol: ER_ROLE_CREATE_EXISTS.
constexpr int er_role_create_exists = 1975;

/// Server error specific to mariadb. Error number: 1976, symbol: ER_ROLE_DROP_EXISTS.
constexpr int er_role_drop_exists = 1976;

/// Server error specific to mariadb. Error number: 1977, symbol: ER_CANNOT_CONVERT_CHARACTER.
constexpr int er_cannot_convert_character = 1977;

/// Server error specific to mariadb. Error number: 1978, symbol: ER_INVALID_DEFAULT_VALUE_FOR_FIELD.
constexpr int er_invalid_default_value_for_field = 1978;

/// Server error specific to mariadb. Error number: 1979, symbol: ER_KILL_QUERY_DENIED_ERROR.
constexpr int er_kill_query_denied_error = 1979;

/// Server error specific to mariadb. Error number: 1980, symbol: ER_NO_EIS_FOR_FIELD.
constexpr int er_no_eis_for_field = 1980;

/// Server error specific to mariadb. Error number: 1981, symbol: ER_WARN_AGGFUNC_DEPENDENCE.
constexpr int er_warn_aggfunc_dependence = 1981;

/// Server error specific to mariadb. Error number: 1982, symbol: WARN_INNODB_PARTITION_OPTION_IGNORED.
constexpr int warn_innodb_partition_option_ignored = 1982;

/// Server error specific to mariadb. Error number: 3000, symbol: ER_FILE_CORRUPT.
constexpr int er_file_corrupt = 3000;

/// Server error specific to mariadb. Error number: 3001, symbol: ER_ERROR_ON_MASTER.
constexpr int er_error_on_master = 3001;

/// Server error specific to mariadb. Error number: 3002, symbol: ER_INCONSISTENT_ERROR.
constexpr int er_inconsistent_error = 3002;

/// Server error specific to mariadb. Error number: 3003, symbol: ER_STORAGE_ENGINE_NOT_LOADED.
constexpr int er_storage_engine_not_loaded = 3003;

/// Server error specific to mariadb. Error number: 3004, symbol: ER_GET_STACKED_DA_WITHOUT_ACTIVE_HANDLER.
constexpr int er_get_stacked_da_without_active_handler = 3004;

/// Server error specific to mariadb. Error number: 3005, symbol: ER_WARN_LEGACY_SYNTAX_CONVERTED.
constexpr int er_warn_legacy_syntax_converted = 3005;

/// Server error specific to mariadb. Error number: 3006, symbol: ER_BINLOG_UNSAFE_FULLTEXT_PLUGIN.
constexpr int er_binlog_unsafe_fulltext_plugin = 3006;

/// Server error specific to mariadb. Error number: 3007, symbol: ER_CANNOT_DISCARD_TEMPORARY_TABLE.
constexpr int er_cannot_discard_temporary_table = 3007;

/// Server error specific to mariadb. Error number: 3008, symbol: ER_FK_DEPTH_EXCEEDED.
constexpr int er_fk_depth_exceeded = 3008;

/// Server error specific to mariadb. Error number: 3009, symbol: ER_COL_COUNT_DOESNT_MATCH_PLEASE_UPDATE_V2.
constexpr int er_col_count_doesnt_match_please_update_v2 = 3009;

/// Server error specific to mariadb. Error number: 3010, symbol: ER_WARN_TRIGGER_DOESNT_HAVE_CREATED.
constexpr int er_warn_trigger_doesnt_have_created = 3010;

/// Server error specific to mariadb. Error number: 3011, symbol: ER_REFERENCED_TRG_DOES_NOT_EXIST_MYSQL.
constexpr int er_referenced_trg_does_not_exist_mysql = 3011;

/// Server error specific to mariadb. Error number: 3012, symbol: ER_EXPLAIN_NOT_SUPPORTED.
constexpr int er_explain_not_supported = 3012;

/// Server error specific to mariadb. Error number: 3013, symbol: ER_INVALID_FIELD_SIZE.
constexpr int er_invalid_field_size = 3013;

/// Server error specific to mariadb. Error number: 3014, symbol: ER_MISSING_HA_CREATE_OPTION.
constexpr int er_missing_ha_create_option = 3014;

/// Server error specific to mariadb. Error number: 3015, symbol: ER_ENGINE_OUT_OF_MEMORY.
constexpr int er_engine_out_of_memory = 3015;

/// Server error specific to mariadb. Error number: 3016, symbol: ER_PASSWORD_EXPIRE_ANONYMOUS_USER.
constexpr int er_password_expire_anonymous_user = 3016;

/// Server error specific to mariadb. Error number: 3017, symbol: ER_SLAVE_SQL_THREAD_MUST_STOP.
constexpr int er_slave_sql_thread_must_stop = 3017;

/// Server error specific to mariadb. Error number: 3018, symbol: ER_NO_FT_MATERIALIZED_SUBQUERY.
constexpr int er_no_ft_materialized_subquery = 3018;

/// Server error specific to mariadb. Error number: 3019, symbol: ER_INNODB_UNDO_LOG_FULL.
constexpr int er_innodb_undo_log_full = 3019;

/// Server error specific to mariadb. Error number: 3020, symbol: ER_INVALID_ARGUMENT_FOR_LOGARITHM.
constexpr int er_invalid_argument_for_logarithm = 3020;

/// Server error specific to mariadb. Error number: 3021, symbol: ER_SLAVE_CHANNEL_IO_THREAD_MUST_STOP.
constexpr int er_slave_channel_io_thread_must_stop = 3021;

/// Server error specific to mariadb. Error number: 3022, symbol: ER_WARN_OPEN_TEMP_TABLES_MUST_BE_ZERO.
constexpr int er_warn_open_temp_tables_must_be_zero = 3022;

/// Server error specific to mariadb. Error number: 3023, symbol: ER_WARN_ONLY_MASTER_LOG_FILE_NO_POS.
constexpr int er_warn_only_master_log_file_no_pos = 3023;

/// Server error specific to mariadb. Error number: 3024, symbol: ER_QUERY_TIMEOUT.
constexpr int er_query_timeout = 3024;

/// Server error specific to mariadb. Error number: 3025, symbol: ER_NON_RO_SELECT_DISABLE_TIMER.
constexpr int er_non_ro_select_disable_timer = 3025;

/// Server error specific to mariadb. Error number: 3026, symbol: ER_DUP_LIST_ENTRY.
constexpr int er_dup_list_entry = 3026;

/// Server error specific to mariadb. Error number: 3027, symbol: ER_SQL_MODE_NO_EFFECT.
constexpr int er_sql_mode_no_effect = 3027;

/// Server error specific to mariadb. Error number: 3028, symbol: ER_AGGREGATE_ORDER_FOR_UNION.
constexpr int er_aggregate_order_for_union = 3028;

/// Server error specific to mariadb. Error number: 3029, symbol: ER_AGGREGATE_ORDER_NON_AGG_QUERY.
constexpr int er_aggregate_order_non_agg_query = 3029;

/// Server error specific to mariadb. Error number: 3030, symbol: ER_SLAVE_WORKER_STOPPED_PREVIOUS_THD_ERROR.
constexpr int er_slave_worker_stopped_previous_thd_error = 3030;

/// Server error specific to mariadb. Error number: 3031, symbol: ER_DONT_SUPPORT_SLAVE_PRESERVE_COMMIT_ORDER.
constexpr int er_dont_support_slave_preserve_commit_order = 3031;

/// Server error specific to mariadb. Error number: 3032, symbol: ER_SERVER_OFFLINE_MODE.
constexpr int er_server_offline_mode = 3032;

/// Server error specific to mariadb. Error number: 3033, symbol: ER_GIS_DIFFERENT_SRIDS.
constexpr int er_gis_different_srids = 3033;

/// Server error specific to mariadb. Error number: 3034, symbol: ER_GIS_UNSUPPORTED_ARGUMENT.
constexpr int er_gis_unsupported_argument = 3034;

/// Server error specific to mariadb. Error number: 3035, symbol: ER_GIS_UNKNOWN_ERROR.
constexpr int er_gis_unknown_error = 3035;

/// Server error specific to mariadb. Error number: 3036, symbol: ER_GIS_UNKNOWN_EXCEPTION.
constexpr int er_gis_unknown_exception = 3036;

/// Server error specific to mariadb. Error number: 3037, symbol: ER_GIS_INVALID_DATA.
constexpr int er_gis_invalid_data = 3037;

/// Server error specific to mariadb. Error number: 3038, symbol: ER_BOOST_GEOMETRY_EMPTY_INPUT_EXCEPTION.
constexpr int er_boost_geometry_empty_input_exception = 3038;

/// Server error specific to mariadb. Error number: 3039, symbol: ER_BOOST_GEOMETRY_CENTROID_EXCEPTION.
constexpr int er_boost_geometry_centroid_exception = 3039;

/// Server error specific to mariadb. Error number: 3040, symbol: ER_BOOST_GEOMETRY_OVERLAY_INVALID_INPUT_EXCEPTION.
constexpr int er_boost_geometry_overlay_invalid_input_exception = 3040;

/// Server error specific to mariadb. Error number: 3041, symbol: ER_BOOST_GEOMETRY_TURN_INFO_EXCEPTION.
constexpr int er_boost_geometry_turn_info_exception = 3041;

/// Server error specific to mariadb. Error number: 3042, symbol: ER_BOOST_GEOMETRY_SELF_INTERSECTION_POINT_EXCEPTION.
constexpr int er_boost_geometry_self_intersection_point_exception = 3042;

/// Server error specific to mariadb. Error number: 3043, symbol: ER_BOOST_GEOMETRY_UNKNOWN_EXCEPTION.
constexpr int er_boost_geometry_unknown_exception = 3043;

/// Server error specific to mariadb. Error number: 3044, symbol: ER_STD_BAD_ALLOC_ERROR.
constexpr int er_std_bad_alloc_error = 3044;

/// Server error specific to mariadb. Error number: 3045, symbol: ER_STD_DOMAIN_ERROR.
constexpr int er_std_domain_error = 3045;

/// Server error specific to mariadb. Error number: 3046, symbol: ER_STD_LENGTH_ERROR.
constexpr int er_std_length_error = 3046;

/// Server error specific to mariadb. Error number: 3047, symbol: ER_STD_INVALID_ARGUMENT.
constexpr int er_std_invalid_argument = 3047;

/// Server error specific to mariadb. Error number: 3048, symbol: ER_STD_OUT_OF_RANGE_ERROR.
constexpr int er_std_out_of_range_error = 3048;

/// Server error specific to mariadb. Error number: 3049, symbol: ER_STD_OVERFLOW_ERROR.
constexpr int er_std_overflow_error = 3049;

/// Server error specific to mariadb. Error number: 3050, symbol: ER_STD_RANGE_ERROR.
constexpr int er_std_range_error = 3050;

/// Server error specific to mariadb. Error number: 3051, symbol: ER_STD_UNDERFLOW_ERROR.
constexpr int er_std_underflow_error = 3051;

/// Server error specific to mariadb. Error number: 3052, symbol: ER_STD_LOGIC_ERROR.
constexpr int er_std_logic_error = 3052;

/// Server error specific to mariadb. Error number: 3053, symbol: ER_STD_RUNTIME_ERROR.
constexpr int er_std_runtime_error = 3053;

/// Server error specific to mariadb. Error number: 3054, symbol: ER_STD_UNKNOWN_EXCEPTION.
constexpr int er_std_unknown_exception = 3054;

/// Server error specific to mariadb. Error number: 3055, symbol: ER_GIS_DATA_WRONG_ENDIANESS.
constexpr int er_gis_data_wrong_endianess = 3055;

/// Server error specific to mariadb. Error number: 3056, symbol: ER_CHANGE_MASTER_PASSWORD_LENGTH.
constexpr int er_change_master_password_length = 3056;

/// Server error specific to mariadb. Error number: 3057, symbol: ER_USER_LOCK_WRONG_NAME.
constexpr int er_user_lock_wrong_name = 3057;

/// Server error specific to mariadb. Error number: 3058, symbol: ER_USER_LOCK_DEADLOCK.
constexpr int er_user_lock_deadlock = 3058;

/// Server error specific to mariadb. Error number: 3059, symbol: ER_REPLACE_INACCESSIBLE_ROWS.
constexpr int er_replace_inaccessible_rows = 3059;

/// Server error specific to mariadb. Error number: 3060, symbol: ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_GIS.
constexpr int er_alter_operation_not_supported_reason_gis = 3060;

/// Server error specific to mariadb. Error number: 4002, symbol: ER_WITH_COL_WRONG_LIST.
constexpr int er_with_col_wrong_list = 4002;

/// Server error specific to mariadb. Error number: 4003, symbol: ER_TOO_MANY_DEFINITIONS_IN_WITH_CLAUSE.
constexpr int er_too_many_definitions_in_with_clause = 4003;

/// Server error specific to mariadb. Error number: 4004, symbol: ER_DUP_QUERY_NAME.
constexpr int er_dup_query_name = 4004;

/// Server error specific to mariadb. Error number: 4005, symbol: ER_RECURSIVE_WITHOUT_ANCHORS.
constexpr int er_recursive_without_anchors = 4005;

/// Server error specific to mariadb. Error number: 4006, symbol: ER_UNACCEPTABLE_MUTUAL_RECURSION.
constexpr int er_unacceptable_mutual_recursion = 4006;

/// Server error specific to mariadb. Error number: 4007, symbol: ER_REF_TO_RECURSIVE_WITH_TABLE_IN_DERIVED.
constexpr int er_ref_to_recursive_with_table_in_derived = 4007;

/// Server error specific to mariadb. Error number: 4008, symbol: ER_NOT_STANDARD_COMPLIANT_RECURSIVE.
constexpr int er_not_standard_compliant_recursive = 4008;

/// Server error specific to mariadb. Error number: 4009, symbol: ER_WRONG_WINDOW_SPEC_NAME.
constexpr int er_wrong_window_spec_name = 4009;

/// Server error specific to mariadb. Error number: 4010, symbol: ER_DUP_WINDOW_NAME.
constexpr int er_dup_window_name = 4010;

/// Server error specific to mariadb. Error number: 4011, symbol: ER_PARTITION_LIST_IN_REFERENCING_WINDOW_SPEC.
constexpr int er_partition_list_in_referencing_window_spec = 4011;

/// Server error specific to mariadb. Error number: 4012, symbol: ER_ORDER_LIST_IN_REFERENCING_WINDOW_SPEC.
constexpr int er_order_list_in_referencing_window_spec = 4012;

/// Server error specific to mariadb. Error number: 4013, symbol: ER_WINDOW_FRAME_IN_REFERENCED_WINDOW_SPEC.
constexpr int er_window_frame_in_referenced_window_spec = 4013;

/// Server error specific to mariadb. Error number: 4014, symbol: ER_BAD_COMBINATION_OF_WINDOW_FRAME_BOUND_SPECS.
constexpr int er_bad_combination_of_window_frame_bound_specs = 4014;

/// Server error specific to mariadb. Error number: 4015, symbol: ER_WRONG_PLACEMENT_OF_WINDOW_FUNCTION.
constexpr int er_wrong_placement_of_window_function = 4015;

/// Server error specific to mariadb. Error number: 4016, symbol: ER_WINDOW_FUNCTION_IN_WINDOW_SPEC.
constexpr int er_window_function_in_window_spec = 4016;

/// Server error specific to mariadb. Error number: 4017, symbol: ER_NOT_ALLOWED_WINDOW_FRAME.
constexpr int er_not_allowed_window_frame = 4017;

/// Server error specific to mariadb. Error number: 4018, symbol: ER_NO_ORDER_LIST_IN_WINDOW_SPEC.
constexpr int er_no_order_list_in_window_spec = 4018;

/// Server error specific to mariadb. Error number: 4019, symbol: ER_RANGE_FRAME_NEEDS_SIMPLE_ORDERBY.
constexpr int er_range_frame_needs_simple_orderby = 4019;

/// Server error specific to mariadb. Error number: 4020, symbol: ER_WRONG_TYPE_FOR_ROWS_FRAME.
constexpr int er_wrong_type_for_rows_frame = 4020;

/// Server error specific to mariadb. Error number: 4021, symbol: ER_WRONG_TYPE_FOR_RANGE_FRAME.
constexpr int er_wrong_type_for_range_frame = 4021;

/// Server error specific to mariadb. Error number: 4022, symbol: ER_FRAME_EXCLUSION_NOT_SUPPORTED.
constexpr int er_frame_exclusion_not_supported = 4022;

/// Server error specific to mariadb. Error number: 4023, symbol: ER_WINDOW_FUNCTION_DONT_HAVE_FRAME.
constexpr int er_window_function_dont_have_frame = 4023;

/// Server error specific to mariadb. Error number: 4024, symbol: ER_INVALID_NTILE_ARGUMENT.
constexpr int er_invalid_ntile_argument = 4024;

/// Server error specific to mariadb. Error number: 4025, symbol: ER_CONSTRAINT_FAILED.
constexpr int er_constraint_failed = 4025;

/// Server error specific to mariadb. Error number: 4026, symbol: ER_EXPRESSION_IS_TOO_BIG.
constexpr int er_expression_is_too_big = 4026;

/// Server error specific to mariadb. Error number: 4027, symbol: ER_ERROR_EVALUATING_EXPRESSION.
constexpr int er_error_evaluating_expression = 4027;

/// Server error specific to mariadb. Error number: 4028, symbol: ER_CALCULATING_DEFAULT_VALUE.
constexpr int er_calculating_default_value = 4028;

/// Server error specific to mariadb. Error number: 4029, symbol: ER_EXPRESSION_REFERS_TO_UNINIT_FIELD.
constexpr int er_expression_refers_to_uninit_field = 4029;

/// Server error specific to mariadb. Error number: 4030, symbol: ER_PARTITION_DEFAULT_ERROR.
constexpr int er_partition_default_error = 4030;

/// Server error specific to mariadb. Error number: 4031, symbol: ER_REFERENCED_TRG_DOES_NOT_EXIST.
constexpr int er_referenced_trg_does_not_exist = 4031;

/// Server error specific to mariadb. Error number: 4032, symbol: ER_INVALID_DEFAULT_PARAM.
constexpr int er_invalid_default_param = 4032;

/// Server error specific to mariadb. Error number: 4033, symbol: ER_BINLOG_NON_SUPPORTED_BULK.
constexpr int er_binlog_non_supported_bulk = 4033;

/// Server error specific to mariadb. Error number: 4034, symbol: ER_BINLOG_UNCOMPRESS_ERROR.
constexpr int er_binlog_uncompress_error = 4034;

/// Server error specific to mariadb. Error number: 4035, symbol: ER_JSON_BAD_CHR.
constexpr int er_json_bad_chr = 4035;

/// Server error specific to mariadb. Error number: 4036, symbol: ER_JSON_NOT_JSON_CHR.
constexpr int er_json_not_json_chr = 4036;

/// Server error specific to mariadb. Error number: 4037, symbol: ER_JSON_EOS.
constexpr int er_json_eos = 4037;

/// Server error specific to mariadb. Error number: 4038, symbol: ER_JSON_SYNTAX.
constexpr int er_json_syntax = 4038;

/// Server error specific to mariadb. Error number: 4039, symbol: ER_JSON_ESCAPING.
constexpr int er_json_escaping = 4039;

/// Server error specific to mariadb. Error number: 4040, symbol: ER_JSON_DEPTH.
constexpr int er_json_depth = 4040;

/// Server error specific to mariadb. Error number: 4041, symbol: ER_JSON_PATH_EOS.
constexpr int er_json_path_eos = 4041;

/// Server error specific to mariadb. Error number: 4042, symbol: ER_JSON_PATH_SYNTAX.
constexpr int er_json_path_syntax = 4042;

/// Server error specific to mariadb. Error number: 4043, symbol: ER_JSON_PATH_DEPTH.
constexpr int er_json_path_depth = 4043;

/// Server error specific to mariadb. Error number: 4044, symbol: ER_JSON_PATH_NO_WILDCARD.
constexpr int er_json_path_no_wildcard = 4044;

/// Server error specific to mariadb. Error number: 4045, symbol: ER_JSON_PATH_ARRAY.
constexpr int er_json_path_array = 4045;

/// Server error specific to mariadb. Error number: 4046, symbol: ER_JSON_ONE_OR_ALL.
constexpr int er_json_one_or_all = 4046;

/// Server error specific to mariadb. Error number: 4047, symbol: ER_UNSUPPORTED_COMPRESSED_TABLE.
constexpr int er_unsupported_compressed_table = 4047;

/// Server error specific to mariadb. Error number: 4048, symbol: ER_GEOJSON_INCORRECT.
constexpr int er_geojson_incorrect = 4048;

/// Server error specific to mariadb. Error number: 4049, symbol: ER_GEOJSON_TOO_FEW_POINTS.
constexpr int er_geojson_too_few_points = 4049;

/// Server error specific to mariadb. Error number: 4050, symbol: ER_GEOJSON_NOT_CLOSED.
constexpr int er_geojson_not_closed = 4050;

/// Server error specific to mariadb. Error number: 4051, symbol: ER_JSON_PATH_EMPTY.
constexpr int er_json_path_empty = 4051;

/// Server error specific to mariadb. Error number: 4052, symbol: ER_SLAVE_SAME_ID.
constexpr int er_slave_same_id = 4052;

/// Server error specific to mariadb. Error number: 4053, symbol: ER_FLASHBACK_NOT_SUPPORTED.
constexpr int er_flashback_not_supported = 4053;

/// Server error specific to mariadb. Error number: 4054, symbol: ER_KEYS_OUT_OF_ORDER.
constexpr int er_keys_out_of_order = 4054;

/// Server error specific to mariadb. Error number: 4055, symbol: ER_OVERLAPPING_KEYS.
constexpr int er_overlapping_keys = 4055;

/// Server error specific to mariadb. Error number: 4056, symbol: ER_REQUIRE_ROW_BINLOG_FORMAT.
constexpr int er_require_row_binlog_format = 4056;

/// Server error specific to mariadb. Error number: 4057, symbol: ER_ISOLATION_MODE_NOT_SUPPORTED.
constexpr int er_isolation_mode_not_supported = 4057;

/// Server error specific to mariadb. Error number: 4058, symbol: ER_ON_DUPLICATE_DISABLED.
constexpr int er_on_duplicate_disabled = 4058;

/// Server error specific to mariadb. Error number: 4059, symbol: ER_UPDATES_WITH_CONSISTENT_SNAPSHOT.
constexpr int er_updates_with_consistent_snapshot = 4059;

/// Server error specific to mariadb. Error number: 4060, symbol: ER_ROLLBACK_ONLY.
constexpr int er_rollback_only = 4060;

/// Server error specific to mariadb. Error number: 4061, symbol: ER_ROLLBACK_TO_SAVEPOINT.
constexpr int er_rollback_to_savepoint = 4061;

/// Server error specific to mariadb. Error number: 4062, symbol: ER_ISOLATION_LEVEL_WITH_CONSISTENT_SNAPSHOT.
constexpr int er_isolation_level_with_consistent_snapshot = 4062;

/// Server error specific to mariadb. Error number: 4063, symbol: ER_UNSUPPORTED_COLLATION.
constexpr int er_unsupported_collation = 4063;

/// Server error specific to mariadb. Error number: 4064, symbol: ER_METADATA_INCONSISTENCY.
constexpr int er_metadata_inconsistency = 4064;

/// Server error specific to mariadb. Error number: 4065, symbol: ER_CF_DIFFERENT.
constexpr int er_cf_different = 4065;

/// Server error specific to mariadb. Error number: 4066, symbol: ER_RDB_TTL_DURATION_FORMAT.
constexpr int er_rdb_ttl_duration_format = 4066;

/// Server error specific to mariadb. Error number: 4067, symbol: ER_RDB_STATUS_GENERAL.
constexpr int er_rdb_status_general = 4067;

/// Server error specific to mariadb. Error number: 4068, symbol: ER_RDB_STATUS_MSG.
constexpr int er_rdb_status_msg = 4068;

/// Server error specific to mariadb. Error number: 4069, symbol: ER_RDB_TTL_UNSUPPORTED.
constexpr int er_rdb_ttl_unsupported = 4069;

/// Server error specific to mariadb. Error number: 4070, symbol: ER_RDB_TTL_COL_FORMAT.
constexpr int er_rdb_ttl_col_format = 4070;

/// Server error specific to mariadb. Error number: 4071, symbol: ER_PER_INDEX_CF_DEPRECATED.
constexpr int er_per_index_cf_deprecated = 4071;

/// Server error specific to mariadb. Error number: 4072, symbol: ER_KEY_CREATE_DURING_ALTER.
constexpr int er_key_create_during_alter = 4072;

/// Server error specific to mariadb. Error number: 4073, symbol: ER_SK_POPULATE_DURING_ALTER.
constexpr int er_sk_populate_during_alter = 4073;

/// Server error specific to mariadb. Error number: 4074, symbol: ER_SUM_FUNC_WITH_WINDOW_FUNC_AS_ARG.
constexpr int er_sum_func_with_window_func_as_arg = 4074;

/// Server error specific to mariadb. Error number: 4075, symbol: ER_NET_OK_PACKET_TOO_LARGE.
constexpr int er_net_ok_packet_too_large = 4075;

/// Server error specific to mariadb. Error number: 4076, symbol: ER_GEOJSON_EMPTY_COORDINATES.
constexpr int er_geojson_empty_coordinates = 4076;

/// Server error specific to mariadb. Error number: 4077, symbol: ER_MYROCKS_CANT_NOPAD_COLLATION.
constexpr int er_myrocks_cant_nopad_collation = 4077;

/// Server error specific to mariadb. Error number: 4078, symbol: ER_ILLEGAL_PARAMETER_DATA_TYPES2_FOR_OPERATION.
constexpr int er_illegal_parameter_data_types2_for_operation = 4078;

/// Server error specific to mariadb. Error number: 4079, symbol: ER_ILLEGAL_PARAMETER_DATA_TYPE_FOR_OPERATION.
constexpr int er_illegal_parameter_data_type_for_operation = 4079;

/// Server error specific to mariadb. Error number: 4080, symbol: ER_WRONG_PARAMCOUNT_TO_CURSOR.
constexpr int er_wrong_paramcount_to_cursor = 4080;

/// Server error specific to mariadb. Error number: 4081, symbol: ER_UNKNOWN_STRUCTURED_VARIABLE.
constexpr int er_unknown_structured_variable = 4081;

/// Server error specific to mariadb. Error number: 4082, symbol: ER_ROW_VARIABLE_DOES_NOT_HAVE_FIELD.
constexpr int er_row_variable_does_not_have_field = 4082;

/// Server error specific to mariadb. Error number: 4083, symbol: ER_END_IDENTIFIER_DOES_NOT_MATCH.
constexpr int er_end_identifier_does_not_match = 4083;

/// Server error specific to mariadb. Error number: 4084, symbol: ER_SEQUENCE_RUN_OUT.
constexpr int er_sequence_run_out = 4084;

/// Server error specific to mariadb. Error number: 4085, symbol: ER_SEQUENCE_INVALID_DATA.
constexpr int er_sequence_invalid_data = 4085;

/// Server error specific to mariadb. Error number: 4086, symbol: ER_SEQUENCE_INVALID_TABLE_STRUCTURE.
constexpr int er_sequence_invalid_table_structure = 4086;

/// Server error specific to mariadb. Error number: 4087, symbol: ER_SEQUENCE_ACCESS_ERROR.
constexpr int er_sequence_access_error = 4087;

/// Server error specific to mariadb. Error number: 4088, symbol: ER_SEQUENCE_BINLOG_FORMAT.
constexpr int er_sequence_binlog_format = 4088;

/// Server error specific to mariadb. Error number: 4089, symbol: ER_NOT_SEQUENCE.
constexpr int er_not_sequence = 4089;

/// Server error specific to mariadb. Error number: 4090, symbol: ER_NOT_SEQUENCE2.
constexpr int er_not_sequence2 = 4090;

/// Server error specific to mariadb. Error number: 4091, symbol: ER_UNKNOWN_SEQUENCES.
constexpr int er_unknown_sequences = 4091;

/// Server error specific to mariadb. Error number: 4092, symbol: ER_UNKNOWN_VIEW.
constexpr int er_unknown_view = 4092;

/// Server error specific to mariadb. Error number: 4093, symbol: ER_WRONG_INSERT_INTO_SEQUENCE.
constexpr int er_wrong_insert_into_sequence = 4093;

/// Server error specific to mariadb. Error number: 4094, symbol: ER_SP_STACK_TRACE.
constexpr int er_sp_stack_trace = 4094;

/// Server error specific to mariadb. Error number: 4095, symbol: ER_PACKAGE_ROUTINE_IN_SPEC_NOT_DEFINED_IN_BODY.
constexpr int er_package_routine_in_spec_not_defined_in_body = 4095;

/// Server error specific to mariadb. Error number: 4096, symbol: ER_PACKAGE_ROUTINE_FORWARD_DECLARATION_NOT_DEFINED.
constexpr int er_package_routine_forward_declaration_not_defined = 4096;

/// Server error specific to mariadb. Error number: 4097, symbol: ER_COMPRESSED_COLUMN_USED_AS_KEY.
constexpr int er_compressed_column_used_as_key = 4097;

/// Server error specific to mariadb. Error number: 4098, symbol: ER_UNKNOWN_COMPRESSION_METHOD.
constexpr int er_unknown_compression_method = 4098;

/// Server error specific to mariadb. Error number: 4099, symbol: ER_WRONG_NUMBER_OF_VALUES_IN_TVC.
constexpr int er_wrong_number_of_values_in_tvc = 4099;

/// Server error specific to mariadb. Error number: 4100, symbol: ER_FIELD_REFERENCE_IN_TVC.
constexpr int er_field_reference_in_tvc = 4100;

/// Server error specific to mariadb. Error number: 4101, symbol: ER_WRONG_TYPE_FOR_PERCENTILE_FUNC.
constexpr int er_wrong_type_for_percentile_func = 4101;

/// Server error specific to mariadb. Error number: 4102, symbol: ER_ARGUMENT_NOT_CONSTANT.
constexpr int er_argument_not_constant = 4102;

/// Server error specific to mariadb. Error number: 4103, symbol: ER_ARGUMENT_OUT_OF_RANGE.
constexpr int er_argument_out_of_range = 4103;

/// Server error specific to mariadb. Error number: 4104, symbol: ER_WRONG_TYPE_OF_ARGUMENT.
constexpr int er_wrong_type_of_argument = 4104;

/// Server error specific to mariadb. Error number: 4105, symbol: ER_NOT_AGGREGATE_FUNCTION.
constexpr int er_not_aggregate_function = 4105;

/// Server error specific to mariadb. Error number: 4106, symbol: ER_INVALID_AGGREGATE_FUNCTION.
constexpr int er_invalid_aggregate_function = 4106;

/// Server error specific to mariadb. Error number: 4107, symbol: ER_INVALID_VALUE_TO_LIMIT.
constexpr int er_invalid_value_to_limit = 4107;

/// Server error specific to mariadb. Error number: 4108, symbol: ER_INVISIBLE_NOT_NULL_WITHOUT_DEFAULT.
constexpr int er_invisible_not_null_without_default = 4108;

/// Server error specific to mariadb. Error number: 4109, symbol: ER_UPDATE_INFO_WITH_SYSTEM_VERSIONING.
constexpr int er_update_info_with_system_versioning = 4109;

/// Server error specific to mariadb. Error number: 4110, symbol: ER_VERS_FIELD_WRONG_TYPE.
constexpr int er_vers_field_wrong_type = 4110;

/// Server error specific to mariadb. Error number: 4111, symbol: ER_VERS_ENGINE_UNSUPPORTED.
constexpr int er_vers_engine_unsupported = 4111;

/// Server error specific to mariadb. Error number: 4113, symbol: ER_PARTITION_WRONG_TYPE.
constexpr int er_partition_wrong_type = 4113;

/// Server error specific to mariadb. Error number: 4114, symbol: WARN_VERS_PART_FULL.
constexpr int warn_vers_part_full = 4114;

/// Server error specific to mariadb. Error number: 4115, symbol: WARN_VERS_PARAMETERS.
constexpr int warn_vers_parameters = 4115;

/// Server error specific to mariadb. Error number: 4116, symbol: ER_VERS_DROP_PARTITION_INTERVAL.
constexpr int er_vers_drop_partition_interval = 4116;

/// Server error specific to mariadb. Error number: 4118, symbol: WARN_VERS_PART_NON_HISTORICAL.
constexpr int warn_vers_part_non_historical = 4118;

/// Server error specific to mariadb. Error number: 4119, symbol: ER_VERS_ALTER_NOT_ALLOWED.
constexpr int er_vers_alter_not_allowed = 4119;

/// Server error specific to mariadb. Error number: 4120, symbol: ER_VERS_ALTER_ENGINE_PROHIBITED.
constexpr int er_vers_alter_engine_prohibited = 4120;

/// Server error specific to mariadb. Error number: 4121, symbol: ER_VERS_RANGE_PROHIBITED.
constexpr int er_vers_range_prohibited = 4121;

/// Server error specific to mariadb. Error number: 4122, symbol: ER_CONFLICTING_FOR_SYSTEM_TIME.
constexpr int er_conflicting_for_system_time = 4122;

/// Server error specific to mariadb. Error number: 4123, symbol: ER_VERS_TABLE_MUST_HAVE_COLUMNS.
constexpr int er_vers_table_must_have_columns = 4123;

/// Server error specific to mariadb. Error number: 4124, symbol: ER_VERS_NOT_VERSIONED.
constexpr int er_vers_not_versioned = 4124;

/// Server error specific to mariadb. Error number: 4125, symbol: ER_MISSING.
constexpr int er_missing = 4125;

/// Server error specific to mariadb. Error number: 4126, symbol: ER_VERS_PERIOD_COLUMNS.
constexpr int er_vers_period_columns = 4126;

/// Server error specific to mariadb. Error number: 4127, symbol: ER_PART_WRONG_VALUE.
constexpr int er_part_wrong_value = 4127;

/// Server error specific to mariadb. Error number: 4128, symbol: ER_VERS_WRONG_PARTS.
constexpr int er_vers_wrong_parts = 4128;

/// Server error specific to mariadb. Error number: 4129, symbol: ER_VERS_NO_TRX_ID.
constexpr int er_vers_no_trx_id = 4129;

/// Server error specific to mariadb. Error number: 4130, symbol: ER_VERS_ALTER_SYSTEM_FIELD.
constexpr int er_vers_alter_system_field = 4130;

/// Server error specific to mariadb. Error number: 4131, symbol: ER_DROP_VERSIONING_SYSTEM_TIME_PARTITION.
constexpr int er_drop_versioning_system_time_partition = 4131;

/// Server error specific to mariadb. Error number: 4132, symbol: ER_VERS_DB_NOT_SUPPORTED.
constexpr int er_vers_db_not_supported = 4132;

/// Server error specific to mariadb. Error number: 4133, symbol: ER_VERS_TRT_IS_DISABLED.
constexpr int er_vers_trt_is_disabled = 4133;

/// Server error specific to mariadb. Error number: 4134, symbol: ER_VERS_DUPLICATE_ROW_START_END.
constexpr int er_vers_duplicate_row_start_end = 4134;

/// Server error specific to mariadb. Error number: 4135, symbol: ER_VERS_ALREADY_VERSIONED.
constexpr int er_vers_already_versioned = 4135;

/// Server error specific to mariadb. Error number: 4137, symbol: ER_VERS_NOT_SUPPORTED.
constexpr int er_vers_not_supported = 4137;

/// Server error specific to mariadb. Error number: 4138, symbol: ER_VERS_TRX_PART_HISTORIC_ROW_NOT_SUPPORTED.
constexpr int er_vers_trx_part_historic_row_not_supported = 4138;

/// Server error specific to mariadb. Error number: 4139, symbol: ER_INDEX_FILE_FULL.
constexpr int er_index_file_full = 4139;

/// Server error specific to mariadb. Error number: 4140, symbol: ER_UPDATED_COLUMN_ONLY_ONCE.
constexpr int er_updated_column_only_once = 4140;

/// Server error specific to mariadb. Error number: 4141, symbol: ER_EMPTY_ROW_IN_TVC.
constexpr int er_empty_row_in_tvc = 4141;

/// Server error specific to mariadb. Error number: 4142, symbol: ER_VERS_QUERY_IN_PARTITION.
constexpr int er_vers_query_in_partition = 4142;

/// Server error specific to mariadb. Error number: 4143, symbol: ER_KEY_DOESNT_SUPPORT.
constexpr int er_key_doesnt_support = 4143;

/// Server error specific to mariadb. Error number: 4144, symbol: ER_ALTER_OPERATION_TABLE_OPTIONS_NEED_REBUILD.
constexpr int er_alter_operation_table_options_need_rebuild = 4144;

/// Server error specific to mariadb. Error number: 4145, symbol: ER_BACKUP_LOCK_IS_ACTIVE.
constexpr int er_backup_lock_is_active = 4145;

/// Server error specific to mariadb. Error number: 4146, symbol: ER_BACKUP_NOT_RUNNING.
constexpr int er_backup_not_running = 4146;

/// Server error specific to mariadb. Error number: 4147, symbol: ER_BACKUP_WRONG_STAGE.
constexpr int er_backup_wrong_stage = 4147;

/// Server error specific to mariadb. Error number: 4148, symbol: ER_BACKUP_STAGE_FAILED.
constexpr int er_backup_stage_failed = 4148;

/// Server error specific to mariadb. Error number: 4149, symbol: ER_BACKUP_UNKNOWN_STAGE.
constexpr int er_backup_unknown_stage = 4149;

/// Server error specific to mariadb. Error number: 4150, symbol: ER_USER_IS_BLOCKED.
constexpr int er_user_is_blocked = 4150;

/// Server error specific to mariadb. Error number: 4151, symbol: ER_ACCOUNT_HAS_BEEN_LOCKED.
constexpr int er_account_has_been_locked = 4151;

/// Server error specific to mariadb. Error number: 4152, symbol: ER_PERIOD_TEMPORARY_NOT_ALLOWED.
constexpr int er_period_temporary_not_allowed = 4152;

/// Server error specific to mariadb. Error number: 4153, symbol: ER_PERIOD_TYPES_MISMATCH.
constexpr int er_period_types_mismatch = 4153;

/// Server error specific to mariadb. Error number: 4154, symbol: ER_MORE_THAN_ONE_PERIOD.
constexpr int er_more_than_one_period = 4154;

/// Server error specific to mariadb. Error number: 4155, symbol: ER_PERIOD_FIELD_WRONG_ATTRIBUTES.
constexpr int er_period_field_wrong_attributes = 4155;

/// Server error specific to mariadb. Error number: 4156, symbol: ER_PERIOD_NOT_FOUND.
constexpr int er_period_not_found = 4156;

/// Server error specific to mariadb. Error number: 4157, symbol: ER_PERIOD_COLUMNS_UPDATED.
constexpr int er_period_columns_updated = 4157;

/// Server error specific to mariadb. Error number: 4158, symbol: ER_PERIOD_CONSTRAINT_DROP.
constexpr int er_period_constraint_drop = 4158;

/// Server error specific to mariadb. Error number: 4159, symbol: ER_TOO_LONG_KEYPART.
constexpr int er_too_long_keypart = 4159;

/// Server error specific to mariadb. Error number: 4160, symbol: ER_TOO_LONG_DATABASE_COMMENT.
constexpr int er_too_long_database_comment = 4160;

/// Server error specific to mariadb. Error number: 4161, symbol: ER_UNKNOWN_DATA_TYPE.
constexpr int er_unknown_data_type = 4161;

/// Server error specific to mariadb. Error number: 4162, symbol: ER_UNKNOWN_OPERATOR.
constexpr int er_unknown_operator = 4162;

/// Server error specific to mariadb. Error number: 4163, symbol: ER_WARN_HISTORY_ROW_START_TIME.
constexpr int er_warn_history_row_start_time = 4163;

/// Server error specific to mariadb. Error number: 4164, symbol: ER_PART_STARTS_BEYOND_INTERVAL.
constexpr int er_part_starts_beyond_interval = 4164;

/// Server error specific to mariadb. Error number: 4165, symbol: ER_GALERA_REPLICATION_NOT_SUPPORTED.
constexpr int er_galera_replication_not_supported = 4165;

/// Server error specific to mariadb. Error number: 4166, symbol: ER_LOAD_INFILE_CAPABILITY_DISABLED.
constexpr int er_load_infile_capability_disabled = 4166;

/// Server error specific to mariadb. Error number: 4167, symbol: ER_NO_SECURE_TRANSPORTS_CONFIGURED.
constexpr int er_no_secure_transports_configured = 4167;

/// Server error specific to mariadb. Error number: 4168, symbol: ER_SLAVE_IGNORED_SHARED_TABLE.
constexpr int er_slave_ignored_shared_table = 4168;

/// Server error specific to mariadb. Error number: 4169, symbol: ER_NO_AUTOINCREMENT_WITH_UNIQUE.
constexpr int er_no_autoincrement_with_unique = 4169;

/// Server error specific to mariadb. Error number: 4170, symbol: ER_KEY_CONTAINS_PERIOD_FIELDS.
constexpr int er_key_contains_period_fields = 4170;

/// Server error specific to mariadb. Error number: 4171, symbol: ER_KEY_CANT_HAVE_WITHOUT_OVERLAPS.
constexpr int er_key_cant_have_without_overlaps = 4171;

/// Server error specific to mariadb. Error number: 4172, symbol: ER_NOT_ALLOWED_IN_THIS_CONTEXT.
constexpr int er_not_allowed_in_this_context = 4172;

/// Server error specific to mariadb. Error number: 4173, symbol: ER_DATA_WAS_COMMITED_UNDER_ROLLBACK.
constexpr int er_data_was_commited_under_rollback = 4173;

/// Server error specific to mariadb. Error number: 4174, symbol: ER_PK_INDEX_CANT_BE_IGNORED.
constexpr int er_pk_index_cant_be_ignored = 4174;

/// Server error specific to mariadb. Error number: 4175, symbol: ER_BINLOG_UNSAFE_SKIP_LOCKED.
constexpr int er_binlog_unsafe_skip_locked = 4175;

/// Server error specific to mariadb. Error number: 4176, symbol: ER_JSON_TABLE_ERROR_ON_FIELD.
constexpr int er_json_table_error_on_field = 4176;

/// Server error specific to mariadb. Error number: 4177, symbol: ER_JSON_TABLE_ALIAS_REQUIRED.
constexpr int er_json_table_alias_required = 4177;

/// Server error specific to mariadb. Error number: 4178, symbol: ER_JSON_TABLE_SCALAR_EXPECTED.
constexpr int er_json_table_scalar_expected = 4178;

/// Server error specific to mariadb. Error number: 4179, symbol: ER_JSON_TABLE_MULTIPLE_MATCHES.
constexpr int er_json_table_multiple_matches = 4179;

/// Server error specific to mariadb. Error number: 4180, symbol: ER_WITH_TIES_NEEDS_ORDER.
constexpr int er_with_ties_needs_order = 4180;

/// Server error specific to mariadb. Error number: 4181, symbol: ER_REMOVED_ORPHAN_TRIGGER.
constexpr int er_removed_orphan_trigger = 4181;

/// Server error specific to mariadb. Error number: 4182, symbol: ER_STORAGE_ENGINE_DISABLED.
constexpr int er_storage_engine_disabled = 4182;

/// Server error specific to mariadb. Error number: 4183, symbol: WARN_SFORMAT_ERROR.
constexpr int warn_sformat_error = 4183;

/// Server error specific to mariadb. Error number: 4184, symbol: ER_PARTITION_CONVERT_SUBPARTITIONED.
constexpr int er_partition_convert_subpartitioned = 4184;

/// Server error specific to mariadb. Error number: 4185, symbol: ER_PROVIDER_NOT_LOADED.
constexpr int er_provider_not_loaded = 4185;

/// Server error specific to mariadb. Error number: 4186, symbol: ER_JSON_HISTOGRAM_PARSE_FAILED.
constexpr int er_json_histogram_parse_failed = 4186;

/// Server error specific to mariadb. Error number: 4187, symbol: ER_SF_OUT_INOUT_ARG_NOT_ALLOWED.
constexpr int er_sf_out_inout_arg_not_allowed = 4187;

/// Server error specific to mariadb. Error number: 4188, symbol: ER_INCONSISTENT_SLAVE_TEMP_TABLE.
constexpr int er_inconsistent_slave_temp_table = 4188;

/// Server error specific to mariadb. Error number: 4189, symbol: ER_VERS_HIST_PART_FAILED.
constexpr int er_vers_hist_part_failed = 4189;

/// Server error specific to mariadb. Error number: 4190, symbol: WARN_OPTION_CHANGING.
constexpr int warn_option_changing = 4190;

/// Server error specific to mariadb. Error number: 4191, symbol: ER_CM_OPTION_MISSING_REQUIREMENT.
constexpr int er_cm_option_missing_requirement = 4191;

/// Server error specific to mariadb. Error number: 4192, symbol: ER_SLAVE_STATEMENT_TIMEOUT.
constexpr int er_slave_statement_timeout = 4192;

/// Server error specific to mariadb. Error number: 4193, symbol: ER_JSON_INVALID_VALUE_FOR_KEYWORD.
constexpr int er_json_invalid_value_for_keyword = 4193;

/// Server error specific to mariadb. Error number: 4194, symbol: ER_JSON_SCHEMA_KEYWORD_UNSUPPORTED.
constexpr int er_json_schema_keyword_unsupported = 4194;

}  // namespace mariadb_server_errc

}  // namespace mysql
}  // namespace boost

#endif
