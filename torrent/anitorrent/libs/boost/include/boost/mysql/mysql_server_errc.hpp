//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_MYSQL_SERVER_ERRC_HPP
#define BOOST_MYSQL_MYSQL_SERVER_ERRC_HPP

namespace boost {
namespace mysql {

namespace mysql_server_errc {

/// Server error specific to mysql. Error number: 1076, symbol: ER_READY.
constexpr int er_ready = 1076;

/// Server error specific to mysql. Error number: 1101, symbol: ER_BLOB_CANT_HAVE_DEFAULT.
constexpr int er_blob_cant_have_default = 1101;

/// Server error specific to mysql. Error number: 1120, symbol: ER_WRONG_OUTER_JOIN_UNUSED.
constexpr int er_wrong_outer_join_unused = 1120;

/// Server error specific to mysql. Error number: 1176, symbol: ER_KEY_DOES_NOT_EXITS.
constexpr int er_key_does_not_exits = 1176;

/// Server error specific to mysql. Error number: 1185, symbol: ER_DUMP_NOT_IMPLEMENTED.
constexpr int er_dump_not_implemented = 1185;

/// Server error specific to mysql. Error number: 1349, symbol: ER_VIEW_SELECT_DERIVED_UNUSED.
constexpr int er_view_select_derived_unused = 1349;

/// Server error specific to mysql. Error number: 1487, symbol: ER_NO_CONST_EXPR_IN_RANGE_OR_LIST_ERROR.
constexpr int er_no_const_expr_in_range_or_list_error = 1487;

/// Server error specific to mysql. Error number: 1506, symbol: ER_FOREIGN_KEY_ON_PARTITIONED.
constexpr int er_foreign_key_on_partitioned = 1506;

/// Server error specific to mysql. Error number: 1547, symbol: ER_OBSOLETE_COL_COUNT_DOESNT_MATCH_CORRUPTED.
constexpr int er_obsolete_col_count_doesnt_match_corrupted = 1547;

/// Server error specific to mysql. Error number: 1548, symbol: ER_OBSOLETE_CANNOT_LOAD_FROM_TABLE.
constexpr int er_obsolete_cannot_load_from_table = 1548;

/// Server error specific to mysql. Error number: 1557, symbol: ER_FOREIGN_DUPLICATE_KEY_OLD_UNUSED.
constexpr int er_foreign_duplicate_key_old_unused = 1557;

/// Server error specific to mysql. Error number: 1561, symbol: ER_NDB_CANT_SWITCH_BINLOG_FORMAT.
constexpr int er_ndb_cant_switch_binlog_format = 1561;

/// Server error specific to mysql. Error number: 1593, symbol: ER_BINLOG_FATAL_ERROR.
constexpr int er_binlog_fatal_error = 1593;

/// Server error specific to mysql. Error number: 1608, symbol: ER_NEVER_USED.
constexpr int er_never_used = 1608;

/// Server error specific to mysql. Error number: 1611, symbol: ER_LOAD_DATA_INVALID_COLUMN_UNUSED.
constexpr int er_load_data_invalid_column_unused = 1611;

/// Server error specific to mysql. Error number: 1625, symbol: ER_NDB_REPLICATION_SCHEMA_ERROR.
constexpr int er_ndb_replication_schema_error = 1625;

/// Server error specific to mysql. Error number: 1720, symbol: ER_PLUGIN_NO_UNINSTALL.
constexpr int er_plugin_no_uninstall = 1720;

/// Server error specific to mysql. Error number: 1721, symbol: ER_PLUGIN_NO_INSTALL.
constexpr int er_plugin_no_install = 1721;

/// Server error specific to mysql. Error number: 1725, symbol: ER_TABLE_IN_FK_CHECK.
constexpr int er_table_in_fk_check = 1725;

/// Server error specific to mysql. Error number: 1726, symbol: ER_UNSUPPORTED_ENGINE.
constexpr int er_unsupported_engine = 1726;

/// Server error specific to mysql. Error number: 1742, symbol: ER_RPL_INFO_DATA_TOO_LONG.
constexpr int er_rpl_info_data_too_long = 1742;

/// Server error specific to mysql. Error number: 1768, symbol: ER_CANT_CHANGE_GTID_NEXT_IN_TRANSACTION.
constexpr int er_cant_change_gtid_next_in_transaction = 1768;

/// Server error specific to mysql. Error number: 1777, symbol: ER_AUTO_POSITION_REQUIRES_GTID_MODE_NOT_OFF.
constexpr int er_auto_position_requires_gtid_mode_not_off = 1777;

/// Server error specific to mysql. Error number: 1779, symbol: ER_GTID_MODE_ON_REQUIRES_ENFORCE_GTID_CONSISTENCY_ON.
constexpr int er_gtid_mode_on_requires_enforce_gtid_consistency_on = 1779;

/// Server error specific to mysql. Error number: 1807, symbol: ER_DISCARD_FK_CHECKS_RUNNING.
constexpr int er_discard_fk_checks_running = 1807;

/// Server error specific to mysql. Error number: 1826, symbol: ER_FK_DUP_NAME.
constexpr int er_fk_dup_name = 1826;

/// Server error specific to mysql. Error number: 1837, symbol: ER_GTID_NEXT_TYPE_UNDEFINED_GTID.
constexpr int er_gtid_next_type_undefined_gtid = 1837;

/// Server error specific to mysql. Error number: 1880, symbol: ER_OLD_TEMPORALS_UPGRADED.
constexpr int er_old_temporals_upgraded = 1880;

/// Server error specific to mysql. Error number: 1881, symbol: ER_INNODB_FORCED_RECOVERY.
constexpr int er_innodb_forced_recovery = 1881;

/// Server error specific to mysql. Error number: 1882, symbol: ER_AES_INVALID_IV.
constexpr int er_aes_invalid_iv = 1882;

/// Server error specific to mysql. Error number: 1883, symbol: ER_PLUGIN_CANNOT_BE_UNINSTALLED.
constexpr int er_plugin_cannot_be_uninstalled = 1883;

/// Server error specific to mysql. Error number: 1884, symbol: ER_GTID_UNSAFE_BINLOG_SPLITTABLE_STATEMENT_AND_ASSIGNED_GTID.
constexpr int er_gtid_unsafe_binlog_splittable_statement_and_assigned_gtid = 1884;

/// Server error specific to mysql. Error number: 1885, symbol: ER_SLAVE_HAS_MORE_GTIDS_THAN_MASTER.
constexpr int er_slave_has_more_gtids_than_master = 1885;

/// Server error specific to mysql. Error number: 1886, symbol: ER_MISSING_KEY.
constexpr int er_missing_key = 1886;

/// Server error specific to mysql. Error number: 1887, symbol: WARN_NAMED_PIPE_ACCESS_EVERYONE.
constexpr int warn_named_pipe_access_everyone = 1887;

/// Server error specific to mysql. Error number: 1888, symbol: ER_FOUND_MISSING_GTIDS.
constexpr int er_found_missing_gtids = 1888;

/// Server error specific to mysql. Error number: 3000, symbol: ER_FILE_CORRUPT.
constexpr int er_file_corrupt = 3000;

/// Server error specific to mysql. Error number: 3001, symbol: ER_ERROR_ON_MASTER.
constexpr int er_error_on_master = 3001;

/// Server error specific to mysql. Error number: 3002, symbol: ER_INCONSISTENT_ERROR.
constexpr int er_inconsistent_error = 3002;

/// Server error specific to mysql. Error number: 3003, symbol: ER_STORAGE_ENGINE_NOT_LOADED.
constexpr int er_storage_engine_not_loaded = 3003;

/// Server error specific to mysql. Error number: 3004, symbol: ER_GET_STACKED_DA_WITHOUT_ACTIVE_HANDLER.
constexpr int er_get_stacked_da_without_active_handler = 3004;

/// Server error specific to mysql. Error number: 3005, symbol: ER_WARN_LEGACY_SYNTAX_CONVERTED.
constexpr int er_warn_legacy_syntax_converted = 3005;

/// Server error specific to mysql. Error number: 3006, symbol: ER_BINLOG_UNSAFE_FULLTEXT_PLUGIN.
constexpr int er_binlog_unsafe_fulltext_plugin = 3006;

/// Server error specific to mysql. Error number: 3007, symbol: ER_CANNOT_DISCARD_TEMPORARY_TABLE.
constexpr int er_cannot_discard_temporary_table = 3007;

/// Server error specific to mysql. Error number: 3008, symbol: ER_FK_DEPTH_EXCEEDED.
constexpr int er_fk_depth_exceeded = 3008;

/// Server error specific to mysql. Error number: 3009, symbol: ER_COL_COUNT_DOESNT_MATCH_PLEASE_UPDATE_V2.
constexpr int er_col_count_doesnt_match_please_update_v2 = 3009;

/// Server error specific to mysql. Error number: 3010, symbol: ER_WARN_TRIGGER_DOESNT_HAVE_CREATED.
constexpr int er_warn_trigger_doesnt_have_created = 3010;

/// Server error specific to mysql. Error number: 3011, symbol: ER_REFERENCED_TRG_DOES_NOT_EXIST.
constexpr int er_referenced_trg_does_not_exist = 3011;

/// Server error specific to mysql. Error number: 3012, symbol: ER_EXPLAIN_NOT_SUPPORTED.
constexpr int er_explain_not_supported = 3012;

/// Server error specific to mysql. Error number: 3013, symbol: ER_INVALID_FIELD_SIZE.
constexpr int er_invalid_field_size = 3013;

/// Server error specific to mysql. Error number: 3014, symbol: ER_MISSING_HA_CREATE_OPTION.
constexpr int er_missing_ha_create_option = 3014;

/// Server error specific to mysql. Error number: 3015, symbol: ER_ENGINE_OUT_OF_MEMORY.
constexpr int er_engine_out_of_memory = 3015;

/// Server error specific to mysql. Error number: 3016, symbol: ER_PASSWORD_EXPIRE_ANONYMOUS_USER.
constexpr int er_password_expire_anonymous_user = 3016;

/// Server error specific to mysql. Error number: 3017, symbol: ER_SLAVE_SQL_THREAD_MUST_STOP.
constexpr int er_slave_sql_thread_must_stop = 3017;

/// Server error specific to mysql. Error number: 3018, symbol: ER_NO_FT_MATERIALIZED_SUBQUERY.
constexpr int er_no_ft_materialized_subquery = 3018;

/// Server error specific to mysql. Error number: 3019, symbol: ER_INNODB_UNDO_LOG_FULL.
constexpr int er_innodb_undo_log_full = 3019;

/// Server error specific to mysql. Error number: 3020, symbol: ER_INVALID_ARGUMENT_FOR_LOGARITHM.
constexpr int er_invalid_argument_for_logarithm = 3020;

/// Server error specific to mysql. Error number: 3021, symbol: ER_SLAVE_CHANNEL_IO_THREAD_MUST_STOP.
constexpr int er_slave_channel_io_thread_must_stop = 3021;

/// Server error specific to mysql. Error number: 3022, symbol: ER_WARN_OPEN_TEMP_TABLES_MUST_BE_ZERO.
constexpr int er_warn_open_temp_tables_must_be_zero = 3022;

/// Server error specific to mysql. Error number: 3023, symbol: ER_WARN_ONLY_MASTER_LOG_FILE_NO_POS.
constexpr int er_warn_only_master_log_file_no_pos = 3023;

/// Server error specific to mysql. Error number: 3024, symbol: ER_QUERY_TIMEOUT.
constexpr int er_query_timeout = 3024;

/// Server error specific to mysql. Error number: 3025, symbol: ER_NON_RO_SELECT_DISABLE_TIMER.
constexpr int er_non_ro_select_disable_timer = 3025;

/// Server error specific to mysql. Error number: 3026, symbol: ER_DUP_LIST_ENTRY.
constexpr int er_dup_list_entry = 3026;

/// Server error specific to mysql. Error number: 3027, symbol: ER_SQL_MODE_NO_EFFECT.
constexpr int er_sql_mode_no_effect = 3027;

/// Server error specific to mysql. Error number: 3028, symbol: ER_AGGREGATE_ORDER_FOR_UNION.
constexpr int er_aggregate_order_for_union = 3028;

/// Server error specific to mysql. Error number: 3029, symbol: ER_AGGREGATE_ORDER_NON_AGG_QUERY.
constexpr int er_aggregate_order_non_agg_query = 3029;

/// Server error specific to mysql. Error number: 3030, symbol: ER_SLAVE_WORKER_STOPPED_PREVIOUS_THD_ERROR.
constexpr int er_slave_worker_stopped_previous_thd_error = 3030;

/// Server error specific to mysql. Error number: 3031, symbol: ER_DONT_SUPPORT_SLAVE_PRESERVE_COMMIT_ORDER.
constexpr int er_dont_support_slave_preserve_commit_order = 3031;

/// Server error specific to mysql. Error number: 3032, symbol: ER_SERVER_OFFLINE_MODE.
constexpr int er_server_offline_mode = 3032;

/// Server error specific to mysql. Error number: 3033, symbol: ER_GIS_DIFFERENT_SRIDS.
constexpr int er_gis_different_srids = 3033;

/// Server error specific to mysql. Error number: 3034, symbol: ER_GIS_UNSUPPORTED_ARGUMENT.
constexpr int er_gis_unsupported_argument = 3034;

/// Server error specific to mysql. Error number: 3035, symbol: ER_GIS_UNKNOWN_ERROR.
constexpr int er_gis_unknown_error = 3035;

/// Server error specific to mysql. Error number: 3036, symbol: ER_GIS_UNKNOWN_EXCEPTION.
constexpr int er_gis_unknown_exception = 3036;

/// Server error specific to mysql. Error number: 3037, symbol: ER_GIS_INVALID_DATA.
constexpr int er_gis_invalid_data = 3037;

/// Server error specific to mysql. Error number: 3038, symbol: ER_BOOST_GEOMETRY_EMPTY_INPUT_EXCEPTION.
constexpr int er_boost_geometry_empty_input_exception = 3038;

/// Server error specific to mysql. Error number: 3039, symbol: ER_BOOST_GEOMETRY_CENTROID_EXCEPTION.
constexpr int er_boost_geometry_centroid_exception = 3039;

/// Server error specific to mysql. Error number: 3040, symbol: ER_BOOST_GEOMETRY_OVERLAY_INVALID_INPUT_EXCEPTION.
constexpr int er_boost_geometry_overlay_invalid_input_exception = 3040;

/// Server error specific to mysql. Error number: 3041, symbol: ER_BOOST_GEOMETRY_TURN_INFO_EXCEPTION.
constexpr int er_boost_geometry_turn_info_exception = 3041;

/// Server error specific to mysql. Error number: 3042, symbol: ER_BOOST_GEOMETRY_SELF_INTERSECTION_POINT_EXCEPTION.
constexpr int er_boost_geometry_self_intersection_point_exception = 3042;

/// Server error specific to mysql. Error number: 3043, symbol: ER_BOOST_GEOMETRY_UNKNOWN_EXCEPTION.
constexpr int er_boost_geometry_unknown_exception = 3043;

/// Server error specific to mysql. Error number: 3044, symbol: ER_STD_BAD_ALLOC_ERROR.
constexpr int er_std_bad_alloc_error = 3044;

/// Server error specific to mysql. Error number: 3045, symbol: ER_STD_DOMAIN_ERROR.
constexpr int er_std_domain_error = 3045;

/// Server error specific to mysql. Error number: 3046, symbol: ER_STD_LENGTH_ERROR.
constexpr int er_std_length_error = 3046;

/// Server error specific to mysql. Error number: 3047, symbol: ER_STD_INVALID_ARGUMENT.
constexpr int er_std_invalid_argument = 3047;

/// Server error specific to mysql. Error number: 3048, symbol: ER_STD_OUT_OF_RANGE_ERROR.
constexpr int er_std_out_of_range_error = 3048;

/// Server error specific to mysql. Error number: 3049, symbol: ER_STD_OVERFLOW_ERROR.
constexpr int er_std_overflow_error = 3049;

/// Server error specific to mysql. Error number: 3050, symbol: ER_STD_RANGE_ERROR.
constexpr int er_std_range_error = 3050;

/// Server error specific to mysql. Error number: 3051, symbol: ER_STD_UNDERFLOW_ERROR.
constexpr int er_std_underflow_error = 3051;

/// Server error specific to mysql. Error number: 3052, symbol: ER_STD_LOGIC_ERROR.
constexpr int er_std_logic_error = 3052;

/// Server error specific to mysql. Error number: 3053, symbol: ER_STD_RUNTIME_ERROR.
constexpr int er_std_runtime_error = 3053;

/// Server error specific to mysql. Error number: 3054, symbol: ER_STD_UNKNOWN_EXCEPTION.
constexpr int er_std_unknown_exception = 3054;

/// Server error specific to mysql. Error number: 3055, symbol: ER_GIS_DATA_WRONG_ENDIANESS.
constexpr int er_gis_data_wrong_endianess = 3055;

/// Server error specific to mysql. Error number: 3056, symbol: ER_CHANGE_MASTER_PASSWORD_LENGTH.
constexpr int er_change_master_password_length = 3056;

/// Server error specific to mysql. Error number: 3057, symbol: ER_USER_LOCK_WRONG_NAME.
constexpr int er_user_lock_wrong_name = 3057;

/// Server error specific to mysql. Error number: 3058, symbol: ER_USER_LOCK_DEADLOCK.
constexpr int er_user_lock_deadlock = 3058;

/// Server error specific to mysql. Error number: 3059, symbol: ER_REPLACE_INACCESSIBLE_ROWS.
constexpr int er_replace_inaccessible_rows = 3059;

/// Server error specific to mysql. Error number: 3060, symbol: ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_GIS.
constexpr int er_alter_operation_not_supported_reason_gis = 3060;

/// Server error specific to mysql. Error number: 3061, symbol: ER_ILLEGAL_USER_VAR.
constexpr int er_illegal_user_var = 3061;

/// Server error specific to mysql. Error number: 3062, symbol: ER_GTID_MODE_OFF.
constexpr int er_gtid_mode_off = 3062;

/// Server error specific to mysql. Error number: 3063, symbol: ER_UNSUPPORTED_BY_REPLICATION_THREAD.
constexpr int er_unsupported_by_replication_thread = 3063;

/// Server error specific to mysql. Error number: 3064, symbol: ER_INCORRECT_TYPE.
constexpr int er_incorrect_type = 3064;

/// Server error specific to mysql. Error number: 3065, symbol: ER_FIELD_IN_ORDER_NOT_SELECT.
constexpr int er_field_in_order_not_select = 3065;

/// Server error specific to mysql. Error number: 3066, symbol: ER_AGGREGATE_IN_ORDER_NOT_SELECT.
constexpr int er_aggregate_in_order_not_select = 3066;

/// Server error specific to mysql. Error number: 3067, symbol: ER_INVALID_RPL_WILD_TABLE_FILTER_PATTERN.
constexpr int er_invalid_rpl_wild_table_filter_pattern = 3067;

/// Server error specific to mysql. Error number: 3068, symbol: ER_NET_OK_PACKET_TOO_LARGE.
constexpr int er_net_ok_packet_too_large = 3068;

/// Server error specific to mysql. Error number: 3069, symbol: ER_INVALID_JSON_DATA.
constexpr int er_invalid_json_data = 3069;

/// Server error specific to mysql. Error number: 3070, symbol: ER_INVALID_GEOJSON_MISSING_MEMBER.
constexpr int er_invalid_geojson_missing_member = 3070;

/// Server error specific to mysql. Error number: 3071, symbol: ER_INVALID_GEOJSON_WRONG_TYPE.
constexpr int er_invalid_geojson_wrong_type = 3071;

/// Server error specific to mysql. Error number: 3072, symbol: ER_INVALID_GEOJSON_UNSPECIFIED.
constexpr int er_invalid_geojson_unspecified = 3072;

/// Server error specific to mysql. Error number: 3073, symbol: ER_DIMENSION_UNSUPPORTED.
constexpr int er_dimension_unsupported = 3073;

/// Server error specific to mysql. Error number: 3074, symbol: ER_SLAVE_CHANNEL_DOES_NOT_EXIST.
constexpr int er_slave_channel_does_not_exist = 3074;

/// Server error specific to mysql. Error number: 3075, symbol: ER_SLAVE_MULTIPLE_CHANNELS_HOST_PORT.
constexpr int er_slave_multiple_channels_host_port = 3075;

/// Server error specific to mysql. Error number: 3076, symbol: ER_SLAVE_CHANNEL_NAME_INVALID_OR_TOO_LONG.
constexpr int er_slave_channel_name_invalid_or_too_long = 3076;

/// Server error specific to mysql. Error number: 3077, symbol: ER_SLAVE_NEW_CHANNEL_WRONG_REPOSITORY.
constexpr int er_slave_new_channel_wrong_repository = 3077;

/// Server error specific to mysql. Error number: 3078, symbol: ER_SLAVE_CHANNEL_DELETE.
constexpr int er_slave_channel_delete = 3078;

/// Server error specific to mysql. Error number: 3079, symbol: ER_SLAVE_MULTIPLE_CHANNELS_CMD.
constexpr int er_slave_multiple_channels_cmd = 3079;

/// Server error specific to mysql. Error number: 3080, symbol: ER_SLAVE_MAX_CHANNELS_EXCEEDED.
constexpr int er_slave_max_channels_exceeded = 3080;

/// Server error specific to mysql. Error number: 3081, symbol: ER_SLAVE_CHANNEL_MUST_STOP.
constexpr int er_slave_channel_must_stop = 3081;

/// Server error specific to mysql. Error number: 3082, symbol: ER_SLAVE_CHANNEL_NOT_RUNNING.
constexpr int er_slave_channel_not_running = 3082;

/// Server error specific to mysql. Error number: 3083, symbol: ER_SLAVE_CHANNEL_WAS_RUNNING.
constexpr int er_slave_channel_was_running = 3083;

/// Server error specific to mysql. Error number: 3084, symbol: ER_SLAVE_CHANNEL_WAS_NOT_RUNNING.
constexpr int er_slave_channel_was_not_running = 3084;

/// Server error specific to mysql. Error number: 3085, symbol: ER_SLAVE_CHANNEL_SQL_THREAD_MUST_STOP.
constexpr int er_slave_channel_sql_thread_must_stop = 3085;

/// Server error specific to mysql. Error number: 3086, symbol: ER_SLAVE_CHANNEL_SQL_SKIP_COUNTER.
constexpr int er_slave_channel_sql_skip_counter = 3086;

/// Server error specific to mysql. Error number: 3087, symbol: ER_WRONG_FIELD_WITH_GROUP_V2.
constexpr int er_wrong_field_with_group_v2 = 3087;

/// Server error specific to mysql. Error number: 3088, symbol: ER_MIX_OF_GROUP_FUNC_AND_FIELDS_V2.
constexpr int er_mix_of_group_func_and_fields_v2 = 3088;

/// Server error specific to mysql. Error number: 3089, symbol: ER_WARN_DEPRECATED_SYSVAR_UPDATE.
constexpr int er_warn_deprecated_sysvar_update = 3089;

/// Server error specific to mysql. Error number: 3090, symbol: ER_WARN_DEPRECATED_SQLMODE.
constexpr int er_warn_deprecated_sqlmode = 3090;

/// Server error specific to mysql. Error number: 3091, symbol: ER_CANNOT_LOG_PARTIAL_DROP_DATABASE_WITH_GTID.
constexpr int er_cannot_log_partial_drop_database_with_gtid = 3091;

/// Server error specific to mysql. Error number: 3092, symbol: ER_GROUP_REPLICATION_CONFIGURATION.
constexpr int er_group_replication_configuration = 3092;

/// Server error specific to mysql. Error number: 3093, symbol: ER_GROUP_REPLICATION_RUNNING.
constexpr int er_group_replication_running = 3093;

/// Server error specific to mysql. Error number: 3094, symbol: ER_GROUP_REPLICATION_APPLIER_INIT_ERROR.
constexpr int er_group_replication_applier_init_error = 3094;

/// Server error specific to mysql. Error number: 3095, symbol: ER_GROUP_REPLICATION_STOP_APPLIER_THREAD_TIMEOUT.
constexpr int er_group_replication_stop_applier_thread_timeout = 3095;

/// Server error specific to mysql. Error number: 3096, symbol: ER_GROUP_REPLICATION_COMMUNICATION_LAYER_SESSION_ERROR.
constexpr int er_group_replication_communication_layer_session_error = 3096;

/// Server error specific to mysql. Error number: 3097, symbol: ER_GROUP_REPLICATION_COMMUNICATION_LAYER_JOIN_ERROR.
constexpr int er_group_replication_communication_layer_join_error = 3097;

/// Server error specific to mysql. Error number: 3098, symbol: ER_BEFORE_DML_VALIDATION_ERROR.
constexpr int er_before_dml_validation_error = 3098;

/// Server error specific to mysql. Error number: 3099, symbol: ER_PREVENTS_VARIABLE_WITHOUT_RBR.
constexpr int er_prevents_variable_without_rbr = 3099;

/// Server error specific to mysql. Error number: 3100, symbol: ER_RUN_HOOK_ERROR.
constexpr int er_run_hook_error = 3100;

/// Server error specific to mysql. Error number: 3101, symbol: ER_TRANSACTION_ROLLBACK_DURING_COMMIT.
constexpr int er_transaction_rollback_during_commit = 3101;

/// Server error specific to mysql. Error number: 3102, symbol: ER_GENERATED_COLUMN_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_generated_column_function_is_not_allowed = 3102;

/// Server error specific to mysql. Error number: 3103, symbol: ER_UNSUPPORTED_ALTER_INPLACE_ON_VIRTUAL_COLUMN.
constexpr int er_unsupported_alter_inplace_on_virtual_column = 3103;

/// Server error specific to mysql. Error number: 3104, symbol: ER_WRONG_FK_OPTION_FOR_GENERATED_COLUMN.
constexpr int er_wrong_fk_option_for_generated_column = 3104;

/// Server error specific to mysql. Error number: 3105, symbol: ER_NON_DEFAULT_VALUE_FOR_GENERATED_COLUMN.
constexpr int er_non_default_value_for_generated_column = 3105;

/// Server error specific to mysql. Error number: 3106, symbol: ER_UNSUPPORTED_ACTION_ON_GENERATED_COLUMN.
constexpr int er_unsupported_action_on_generated_column = 3106;

/// Server error specific to mysql. Error number: 3107, symbol: ER_GENERATED_COLUMN_NON_PRIOR.
constexpr int er_generated_column_non_prior = 3107;

/// Server error specific to mysql. Error number: 3108, symbol: ER_DEPENDENT_BY_GENERATED_COLUMN.
constexpr int er_dependent_by_generated_column = 3108;

/// Server error specific to mysql. Error number: 3109, symbol: ER_GENERATED_COLUMN_REF_AUTO_INC.
constexpr int er_generated_column_ref_auto_inc = 3109;

/// Server error specific to mysql. Error number: 3110, symbol: ER_FEATURE_NOT_AVAILABLE.
constexpr int er_feature_not_available = 3110;

/// Server error specific to mysql. Error number: 3111, symbol: ER_CANT_SET_GTID_MODE.
constexpr int er_cant_set_gtid_mode = 3111;

/// Server error specific to mysql. Error number: 3112, symbol: ER_CANT_USE_AUTO_POSITION_WITH_GTID_MODE_OFF.
constexpr int er_cant_use_auto_position_with_gtid_mode_off = 3112;

/// Server error specific to mysql. Error number: 3113, symbol: ER_CANT_REPLICATE_ANONYMOUS_WITH_AUTO_POSITION.
constexpr int er_cant_replicate_anonymous_with_auto_position = 3113;

/// Server error specific to mysql. Error number: 3114, symbol: ER_CANT_REPLICATE_ANONYMOUS_WITH_GTID_MODE_ON.
constexpr int er_cant_replicate_anonymous_with_gtid_mode_on = 3114;

/// Server error specific to mysql. Error number: 3115, symbol: ER_CANT_REPLICATE_GTID_WITH_GTID_MODE_OFF.
constexpr int er_cant_replicate_gtid_with_gtid_mode_off = 3115;

/// Server error specific to mysql. Error number: 3116, symbol: ER_CANT_ENFORCE_GTID_CONSISTENCY_WITH_ONGOING_GTID_VIOLATING_TX.
constexpr int er_cant_enforce_gtid_consistency_with_ongoing_gtid_violating_tx = 3116;

/// Server error specific to mysql. Error number: 3117, symbol: ER_ENFORCE_GTID_CONSISTENCY_WARN_WITH_ONGOING_GTID_VIOLATING_TX.
constexpr int er_enforce_gtid_consistency_warn_with_ongoing_gtid_violating_tx = 3117;

/// Server error specific to mysql. Error number: 3118, symbol: ER_ACCOUNT_HAS_BEEN_LOCKED.
constexpr int er_account_has_been_locked = 3118;

/// Server error specific to mysql. Error number: 3119, symbol: ER_WRONG_TABLESPACE_NAME.
constexpr int er_wrong_tablespace_name = 3119;

/// Server error specific to mysql. Error number: 3120, symbol: ER_TABLESPACE_IS_NOT_EMPTY.
constexpr int er_tablespace_is_not_empty = 3120;

/// Server error specific to mysql. Error number: 3121, symbol: ER_WRONG_FILE_NAME.
constexpr int er_wrong_file_name = 3121;

/// Server error specific to mysql. Error number: 3122, symbol: ER_BOOST_GEOMETRY_INCONSISTENT_TURNS_EXCEPTION.
constexpr int er_boost_geometry_inconsistent_turns_exception = 3122;

/// Server error specific to mysql. Error number: 3123, symbol: ER_WARN_OPTIMIZER_HINT_SYNTAX_ERROR.
constexpr int er_warn_optimizer_hint_syntax_error = 3123;

/// Server error specific to mysql. Error number: 3124, symbol: ER_WARN_BAD_MAX_EXECUTION_TIME.
constexpr int er_warn_bad_max_execution_time = 3124;

/// Server error specific to mysql. Error number: 3125, symbol: ER_WARN_UNSUPPORTED_MAX_EXECUTION_TIME.
constexpr int er_warn_unsupported_max_execution_time = 3125;

/// Server error specific to mysql. Error number: 3126, symbol: ER_WARN_CONFLICTING_HINT.
constexpr int er_warn_conflicting_hint = 3126;

/// Server error specific to mysql. Error number: 3127, symbol: ER_WARN_UNKNOWN_QB_NAME.
constexpr int er_warn_unknown_qb_name = 3127;

/// Server error specific to mysql. Error number: 3128, symbol: ER_UNRESOLVED_HINT_NAME.
constexpr int er_unresolved_hint_name = 3128;

/// Server error specific to mysql. Error number: 3129, symbol: ER_WARN_ON_MODIFYING_GTID_EXECUTED_TABLE.
constexpr int er_warn_on_modifying_gtid_executed_table = 3129;

/// Server error specific to mysql. Error number: 3130, symbol: ER_PLUGGABLE_PROTOCOL_COMMAND_NOT_SUPPORTED.
constexpr int er_pluggable_protocol_command_not_supported = 3130;

/// Server error specific to mysql. Error number: 3131, symbol: ER_LOCKING_SERVICE_WRONG_NAME.
constexpr int er_locking_service_wrong_name = 3131;

/// Server error specific to mysql. Error number: 3132, symbol: ER_LOCKING_SERVICE_DEADLOCK.
constexpr int er_locking_service_deadlock = 3132;

/// Server error specific to mysql. Error number: 3133, symbol: ER_LOCKING_SERVICE_TIMEOUT.
constexpr int er_locking_service_timeout = 3133;

/// Server error specific to mysql. Error number: 3134, symbol: ER_GIS_MAX_POINTS_IN_GEOMETRY_OVERFLOWED.
constexpr int er_gis_max_points_in_geometry_overflowed = 3134;

/// Server error specific to mysql. Error number: 3135, symbol: ER_SQL_MODE_MERGED.
constexpr int er_sql_mode_merged = 3135;

/// Server error specific to mysql. Error number: 3136, symbol: ER_VTOKEN_PLUGIN_TOKEN_MISMATCH.
constexpr int er_vtoken_plugin_token_mismatch = 3136;

/// Server error specific to mysql. Error number: 3137, symbol: ER_VTOKEN_PLUGIN_TOKEN_NOT_FOUND.
constexpr int er_vtoken_plugin_token_not_found = 3137;

/// Server error specific to mysql. Error number: 3138, symbol: ER_CANT_SET_VARIABLE_WHEN_OWNING_GTID.
constexpr int er_cant_set_variable_when_owning_gtid = 3138;

/// Server error specific to mysql. Error number: 3139, symbol: ER_SLAVE_CHANNEL_OPERATION_NOT_ALLOWED.
constexpr int er_slave_channel_operation_not_allowed = 3139;

/// Server error specific to mysql. Error number: 3140, symbol: ER_INVALID_JSON_TEXT.
constexpr int er_invalid_json_text = 3140;

/// Server error specific to mysql. Error number: 3141, symbol: ER_INVALID_JSON_TEXT_IN_PARAM.
constexpr int er_invalid_json_text_in_param = 3141;

/// Server error specific to mysql. Error number: 3142, symbol: ER_INVALID_JSON_BINARY_DATA.
constexpr int er_invalid_json_binary_data = 3142;

/// Server error specific to mysql. Error number: 3143, symbol: ER_INVALID_JSON_PATH.
constexpr int er_invalid_json_path = 3143;

/// Server error specific to mysql. Error number: 3144, symbol: ER_INVALID_JSON_CHARSET.
constexpr int er_invalid_json_charset = 3144;

/// Server error specific to mysql. Error number: 3145, symbol: ER_INVALID_JSON_CHARSET_IN_FUNCTION.
constexpr int er_invalid_json_charset_in_function = 3145;

/// Server error specific to mysql. Error number: 3146, symbol: ER_INVALID_TYPE_FOR_JSON.
constexpr int er_invalid_type_for_json = 3146;

/// Server error specific to mysql. Error number: 3147, symbol: ER_INVALID_CAST_TO_JSON.
constexpr int er_invalid_cast_to_json = 3147;

/// Server error specific to mysql. Error number: 3148, symbol: ER_INVALID_JSON_PATH_CHARSET.
constexpr int er_invalid_json_path_charset = 3148;

/// Server error specific to mysql. Error number: 3149, symbol: ER_INVALID_JSON_PATH_WILDCARD.
constexpr int er_invalid_json_path_wildcard = 3149;

/// Server error specific to mysql. Error number: 3150, symbol: ER_JSON_VALUE_TOO_BIG.
constexpr int er_json_value_too_big = 3150;

/// Server error specific to mysql. Error number: 3151, symbol: ER_JSON_KEY_TOO_BIG.
constexpr int er_json_key_too_big = 3151;

/// Server error specific to mysql. Error number: 3152, symbol: ER_JSON_USED_AS_KEY.
constexpr int er_json_used_as_key = 3152;

/// Server error specific to mysql. Error number: 3153, symbol: ER_JSON_VACUOUS_PATH.
constexpr int er_json_vacuous_path = 3153;

/// Server error specific to mysql. Error number: 3154, symbol: ER_JSON_BAD_ONE_OR_ALL_ARG.
constexpr int er_json_bad_one_or_all_arg = 3154;

/// Server error specific to mysql. Error number: 3155, symbol: ER_NUMERIC_JSON_VALUE_OUT_OF_RANGE.
constexpr int er_numeric_json_value_out_of_range = 3155;

/// Server error specific to mysql. Error number: 3156, symbol: ER_INVALID_JSON_VALUE_FOR_CAST.
constexpr int er_invalid_json_value_for_cast = 3156;

/// Server error specific to mysql. Error number: 3157, symbol: ER_JSON_DOCUMENT_TOO_DEEP.
constexpr int er_json_document_too_deep = 3157;

/// Server error specific to mysql. Error number: 3158, symbol: ER_JSON_DOCUMENT_NULL_KEY.
constexpr int er_json_document_null_key = 3158;

/// Server error specific to mysql. Error number: 3159, symbol: ER_SECURE_TRANSPORT_REQUIRED.
constexpr int er_secure_transport_required = 3159;

/// Server error specific to mysql. Error number: 3160, symbol: ER_NO_SECURE_TRANSPORTS_CONFIGURED.
constexpr int er_no_secure_transports_configured = 3160;

/// Server error specific to mysql. Error number: 3161, symbol: ER_DISABLED_STORAGE_ENGINE.
constexpr int er_disabled_storage_engine = 3161;

/// Server error specific to mysql. Error number: 3162, symbol: ER_USER_DOES_NOT_EXIST.
constexpr int er_user_does_not_exist = 3162;

/// Server error specific to mysql. Error number: 3163, symbol: ER_USER_ALREADY_EXISTS.
constexpr int er_user_already_exists = 3163;

/// Server error specific to mysql. Error number: 3164, symbol: ER_AUDIT_API_ABORT.
constexpr int er_audit_api_abort = 3164;

/// Server error specific to mysql. Error number: 3165, symbol: ER_INVALID_JSON_PATH_ARRAY_CELL.
constexpr int er_invalid_json_path_array_cell = 3165;

/// Server error specific to mysql. Error number: 3166, symbol: ER_BUFPOOL_RESIZE_INPROGRESS.
constexpr int er_bufpool_resize_inprogress = 3166;

/// Server error specific to mysql. Error number: 3167, symbol: ER_FEATURE_DISABLED_SEE_DOC.
constexpr int er_feature_disabled_see_doc = 3167;

/// Server error specific to mysql. Error number: 3168, symbol: ER_SERVER_ISNT_AVAILABLE.
constexpr int er_server_isnt_available = 3168;

/// Server error specific to mysql. Error number: 3169, symbol: ER_SESSION_WAS_KILLED.
constexpr int er_session_was_killed = 3169;

/// Server error specific to mysql. Error number: 3170, symbol: ER_CAPACITY_EXCEEDED.
constexpr int er_capacity_exceeded = 3170;

/// Server error specific to mysql. Error number: 3171, symbol: ER_CAPACITY_EXCEEDED_IN_RANGE_OPTIMIZER.
constexpr int er_capacity_exceeded_in_range_optimizer = 3171;

/// Server error specific to mysql. Error number: 3172, symbol: ER_TABLE_NEEDS_UPG_PART.
constexpr int er_table_needs_upg_part = 3172;

/// Server error specific to mysql. Error number: 3173, symbol: ER_CANT_WAIT_FOR_EXECUTED_GTID_SET_WHILE_OWNING_A_GTID.
constexpr int er_cant_wait_for_executed_gtid_set_while_owning_a_gtid = 3173;

/// Server error specific to mysql. Error number: 3174, symbol: ER_CANNOT_ADD_FOREIGN_BASE_COL_VIRTUAL.
constexpr int er_cannot_add_foreign_base_col_virtual = 3174;

/// Server error specific to mysql. Error number: 3175, symbol: ER_CANNOT_CREATE_VIRTUAL_INDEX_CONSTRAINT.
constexpr int er_cannot_create_virtual_index_constraint = 3175;

/// Server error specific to mysql. Error number: 3176, symbol: ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.
constexpr int er_error_on_modifying_gtid_executed_table = 3176;

/// Server error specific to mysql. Error number: 3177, symbol: ER_LOCK_REFUSED_BY_ENGINE.
constexpr int er_lock_refused_by_engine = 3177;

/// Server error specific to mysql. Error number: 3178, symbol: ER_UNSUPPORTED_ALTER_ONLINE_ON_VIRTUAL_COLUMN.
constexpr int er_unsupported_alter_online_on_virtual_column = 3178;

/// Server error specific to mysql. Error number: 3179, symbol: ER_MASTER_KEY_ROTATION_NOT_SUPPORTED_BY_SE.
constexpr int er_master_key_rotation_not_supported_by_se = 3179;

/// Server error specific to mysql. Error number: 3180, symbol: ER_MASTER_KEY_ROTATION_ERROR_BY_SE.
constexpr int er_master_key_rotation_error_by_se = 3180;

/// Server error specific to mysql. Error number: 3181, symbol: ER_MASTER_KEY_ROTATION_BINLOG_FAILED.
constexpr int er_master_key_rotation_binlog_failed = 3181;

/// Server error specific to mysql. Error number: 3182, symbol: ER_MASTER_KEY_ROTATION_SE_UNAVAILABLE.
constexpr int er_master_key_rotation_se_unavailable = 3182;

/// Server error specific to mysql. Error number: 3183, symbol: ER_TABLESPACE_CANNOT_ENCRYPT.
constexpr int er_tablespace_cannot_encrypt = 3183;

/// Server error specific to mysql. Error number: 3184, symbol: ER_INVALID_ENCRYPTION_OPTION.
constexpr int er_invalid_encryption_option = 3184;

/// Server error specific to mysql. Error number: 3185, symbol: ER_CANNOT_FIND_KEY_IN_KEYRING.
constexpr int er_cannot_find_key_in_keyring = 3185;

/// Server error specific to mysql. Error number: 3186, symbol: ER_CAPACITY_EXCEEDED_IN_PARSER.
constexpr int er_capacity_exceeded_in_parser = 3186;

/// Server error specific to mysql. Error number: 3187, symbol: ER_UNSUPPORTED_ALTER_ENCRYPTION_INPLACE.
constexpr int er_unsupported_alter_encryption_inplace = 3187;

/// Server error specific to mysql. Error number: 3188, symbol: ER_KEYRING_UDF_KEYRING_SERVICE_ERROR.
constexpr int er_keyring_udf_keyring_service_error = 3188;

/// Server error specific to mysql. Error number: 3189, symbol: ER_USER_COLUMN_OLD_LENGTH.
constexpr int er_user_column_old_length = 3189;

/// Server error specific to mysql. Error number: 3190, symbol: ER_CANT_RESET_MASTER.
constexpr int er_cant_reset_master = 3190;

/// Server error specific to mysql. Error number: 3191, symbol: ER_GROUP_REPLICATION_MAX_GROUP_SIZE.
constexpr int er_group_replication_max_group_size = 3191;

/// Server error specific to mysql. Error number: 3192, symbol: ER_CANNOT_ADD_FOREIGN_BASE_COL_STORED.
constexpr int er_cannot_add_foreign_base_col_stored = 3192;

/// Server error specific to mysql. Error number: 3193, symbol: ER_TABLE_REFERENCED.
constexpr int er_table_referenced = 3193;

/// Server error specific to mysql. Error number: 3194, symbol: ER_PARTITION_ENGINE_DEPRECATED_FOR_TABLE.
constexpr int er_partition_engine_deprecated_for_table = 3194;

/// Server error specific to mysql. Error number: 3195, symbol: ER_WARN_USING_GEOMFROMWKB_TO_SET_SRID_ZERO.
constexpr int er_warn_using_geomfromwkb_to_set_srid_zero = 3195;

/// Server error specific to mysql. Error number: 3196, symbol: ER_WARN_USING_GEOMFROMWKB_TO_SET_SRID.
constexpr int er_warn_using_geomfromwkb_to_set_srid = 3196;

/// Server error specific to mysql. Error number: 3197, symbol: ER_XA_RETRY.
constexpr int er_xa_retry = 3197;

/// Server error specific to mysql. Error number: 3198, symbol: ER_KEYRING_AWS_UDF_AWS_KMS_ERROR.
constexpr int er_keyring_aws_udf_aws_kms_error = 3198;

/// Server error specific to mysql. Error number: 3199, symbol: ER_BINLOG_UNSAFE_XA.
constexpr int er_binlog_unsafe_xa = 3199;

/// Server error specific to mysql. Error number: 3200, symbol: ER_UDF_ERROR.
constexpr int er_udf_error = 3200;

/// Server error specific to mysql. Error number: 3201, symbol: ER_KEYRING_MIGRATION_FAILURE.
constexpr int er_keyring_migration_failure = 3201;

/// Server error specific to mysql. Error number: 3202, symbol: ER_KEYRING_ACCESS_DENIED_ERROR.
constexpr int er_keyring_access_denied_error = 3202;

/// Server error specific to mysql. Error number: 3203, symbol: ER_KEYRING_MIGRATION_STATUS.
constexpr int er_keyring_migration_status = 3203;

/// Server error specific to mysql. Error number: 3204, symbol: ER_PLUGIN_FAILED_TO_OPEN_TABLES.
constexpr int er_plugin_failed_to_open_tables = 3204;

/// Server error specific to mysql. Error number: 3205, symbol: ER_PLUGIN_FAILED_TO_OPEN_TABLE.
constexpr int er_plugin_failed_to_open_table = 3205;

/// Server error specific to mysql. Error number: 3206, symbol: ER_AUDIT_LOG_NO_KEYRING_PLUGIN_INSTALLED.
constexpr int er_audit_log_no_keyring_plugin_installed = 3206;

/// Server error specific to mysql. Error number: 3207, symbol: ER_AUDIT_LOG_ENCRYPTION_PASSWORD_HAS_NOT_BEEN_SET.
constexpr int er_audit_log_encryption_password_has_not_been_set = 3207;

/// Server error specific to mysql. Error number: 3208, symbol: ER_AUDIT_LOG_COULD_NOT_CREATE_AES_KEY.
constexpr int er_audit_log_could_not_create_aes_key = 3208;

/// Server error specific to mysql. Error number: 3209, symbol: ER_AUDIT_LOG_ENCRYPTION_PASSWORD_CANNOT_BE_FETCHED.
constexpr int er_audit_log_encryption_password_cannot_be_fetched = 3209;

/// Server error specific to mysql. Error number: 3210, symbol: ER_AUDIT_LOG_JSON_FILTERING_NOT_ENABLED.
constexpr int er_audit_log_json_filtering_not_enabled = 3210;

/// Server error specific to mysql. Error number: 3211, symbol: ER_AUDIT_LOG_UDF_INSUFFICIENT_PRIVILEGE.
constexpr int er_audit_log_udf_insufficient_privilege = 3211;

/// Server error specific to mysql. Error number: 3212, symbol: ER_AUDIT_LOG_SUPER_PRIVILEGE_REQUIRED.
constexpr int er_audit_log_super_privilege_required = 3212;

/// Server error specific to mysql. Error number: 3213, symbol: ER_COULD_NOT_REINITIALIZE_AUDIT_LOG_FILTERS.
constexpr int er_could_not_reinitialize_audit_log_filters = 3213;

/// Server error specific to mysql. Error number: 3214, symbol: ER_AUDIT_LOG_UDF_INVALID_ARGUMENT_TYPE.
constexpr int er_audit_log_udf_invalid_argument_type = 3214;

/// Server error specific to mysql. Error number: 3215, symbol: ER_AUDIT_LOG_UDF_INVALID_ARGUMENT_COUNT.
constexpr int er_audit_log_udf_invalid_argument_count = 3215;

/// Server error specific to mysql. Error number: 3216, symbol: ER_AUDIT_LOG_HAS_NOT_BEEN_INSTALLED.
constexpr int er_audit_log_has_not_been_installed = 3216;

/// Server error specific to mysql. Error number: 3217, symbol: ER_AUDIT_LOG_UDF_READ_INVALID_MAX_ARRAY_LENGTH_ARG_TYPE.
constexpr int er_audit_log_udf_read_invalid_max_array_length_arg_type = 3217;

/// Server error specific to mysql. Error number: 3218, symbol: ER_AUDIT_LOG_UDF_READ_INVALID_MAX_ARRAY_LENGTH_ARG_VALUE.
constexpr int er_audit_log_udf_read_invalid_max_array_length_arg_value = 3218;

/// Server error specific to mysql. Error number: 3219, symbol: ER_AUDIT_LOG_JSON_FILTER_PARSING_ERROR.
constexpr int er_audit_log_json_filter_parsing_error = 3219;

/// Server error specific to mysql. Error number: 3220, symbol: ER_AUDIT_LOG_JSON_FILTER_NAME_CANNOT_BE_EMPTY.
constexpr int er_audit_log_json_filter_name_cannot_be_empty = 3220;

/// Server error specific to mysql. Error number: 3221, symbol: ER_AUDIT_LOG_JSON_USER_NAME_CANNOT_BE_EMPTY.
constexpr int er_audit_log_json_user_name_cannot_be_empty = 3221;

/// Server error specific to mysql. Error number: 3222, symbol: ER_AUDIT_LOG_JSON_FILTER_DOES_NOT_EXISTS.
constexpr int er_audit_log_json_filter_does_not_exists = 3222;

/// Server error specific to mysql. Error number: 3223, symbol: ER_AUDIT_LOG_USER_FIRST_CHARACTER_MUST_BE_ALPHANUMERIC.
constexpr int er_audit_log_user_first_character_must_be_alphanumeric = 3223;

/// Server error specific to mysql. Error number: 3224, symbol: ER_AUDIT_LOG_USER_NAME_INVALID_CHARACTER.
constexpr int er_audit_log_user_name_invalid_character = 3224;

/// Server error specific to mysql. Error number: 3225, symbol: ER_AUDIT_LOG_HOST_NAME_INVALID_CHARACTER.
constexpr int er_audit_log_host_name_invalid_character = 3225;

/// Server error specific to mysql. Error number: 3226, symbol: OBSOLETE_WARN_DEPRECATED_MAXDB_SQL_MODE_FOR_TIMESTAMP.
constexpr int obsolete_warn_deprecated_maxdb_sql_mode_for_timestamp = 3226;

/// Server error specific to mysql. Error number: 3228, symbol: ER_CANT_OPEN_ERROR_LOG.
constexpr int er_cant_open_error_log = 3228;

/// Server error specific to mysql. Error number: 3230, symbol: ER_CANT_START_SERVER_NAMED_PIPE.
constexpr int er_cant_start_server_named_pipe = 3230;

/// Server error specific to mysql. Error number: 3231, symbol: ER_WRITE_SET_EXCEEDS_LIMIT.
constexpr int er_write_set_exceeds_limit = 3231;

/// Server error specific to mysql. Error number: 3232, symbol: ER_DEPRECATED_TLS_VERSION_SESSION.
constexpr int er_deprecated_tls_version_session = 3232;

/// Server error specific to mysql. Error number: 3233, symbol: ER_WARN_DEPRECATED_TLS_VERSION.
constexpr int er_warn_deprecated_tls_version = 3233;

/// Server error specific to mysql. Error number: 3234, symbol: ER_WARN_WRONG_NATIVE_TABLE_STRUCTURE.
constexpr int er_warn_wrong_native_table_structure = 3234;

/// Server error specific to mysql. Error number: 3235, symbol: ER_AES_INVALID_KDF_NAME.
constexpr int er_aes_invalid_kdf_name = 3235;

/// Server error specific to mysql. Error number: 3236, symbol: ER_AES_INVALID_KDF_ITERATIONS.
constexpr int er_aes_invalid_kdf_iterations = 3236;

/// Server error specific to mysql. Error number: 3237, symbol: WARN_AES_KEY_SIZE.
constexpr int warn_aes_key_size = 3237;

/// Server error specific to mysql. Error number: 3238, symbol: ER_AES_INVALID_KDF_OPTION_SIZE.
constexpr int er_aes_invalid_kdf_option_size = 3238;

/// Server error specific to mysql. Error number: 3500, symbol: ER_UNSUPPORT_COMPRESSED_TEMPORARY_TABLE.
constexpr int er_unsupport_compressed_temporary_table = 3500;

/// Server error specific to mysql. Error number: 3501, symbol: ER_ACL_OPERATION_FAILED.
constexpr int er_acl_operation_failed = 3501;

/// Server error specific to mysql. Error number: 3502, symbol: ER_UNSUPPORTED_INDEX_ALGORITHM.
constexpr int er_unsupported_index_algorithm = 3502;

/// Server error specific to mysql. Error number: 3503, symbol: ER_NO_SUCH_DB.
constexpr int er_no_such_db = 3503;

/// Server error specific to mysql. Error number: 3504, symbol: ER_TOO_BIG_ENUM.
constexpr int er_too_big_enum = 3504;

/// Server error specific to mysql. Error number: 3505, symbol: ER_TOO_LONG_SET_ENUM_VALUE.
constexpr int er_too_long_set_enum_value = 3505;

/// Server error specific to mysql. Error number: 3506, symbol: ER_INVALID_DD_OBJECT.
constexpr int er_invalid_dd_object = 3506;

/// Server error specific to mysql. Error number: 3507, symbol: ER_UPDATING_DD_TABLE.
constexpr int er_updating_dd_table = 3507;

/// Server error specific to mysql. Error number: 3508, symbol: ER_INVALID_DD_OBJECT_ID.
constexpr int er_invalid_dd_object_id = 3508;

/// Server error specific to mysql. Error number: 3509, symbol: ER_INVALID_DD_OBJECT_NAME.
constexpr int er_invalid_dd_object_name = 3509;

/// Server error specific to mysql. Error number: 3510, symbol: ER_TABLESPACE_MISSING_WITH_NAME.
constexpr int er_tablespace_missing_with_name = 3510;

/// Server error specific to mysql. Error number: 3511, symbol: ER_TOO_LONG_ROUTINE_COMMENT.
constexpr int er_too_long_routine_comment = 3511;

/// Server error specific to mysql. Error number: 3512, symbol: ER_SP_LOAD_FAILED.
constexpr int er_sp_load_failed = 3512;

/// Server error specific to mysql. Error number: 3513, symbol: ER_INVALID_BITWISE_OPERANDS_SIZE.
constexpr int er_invalid_bitwise_operands_size = 3513;

/// Server error specific to mysql. Error number: 3514, symbol: ER_INVALID_BITWISE_AGGREGATE_OPERANDS_SIZE.
constexpr int er_invalid_bitwise_aggregate_operands_size = 3514;

/// Server error specific to mysql. Error number: 3515, symbol: ER_WARN_UNSUPPORTED_HINT.
constexpr int er_warn_unsupported_hint = 3515;

/// Server error specific to mysql. Error number: 3516, symbol: ER_UNEXPECTED_GEOMETRY_TYPE.
constexpr int er_unexpected_geometry_type = 3516;

/// Server error specific to mysql. Error number: 3517, symbol: ER_SRS_PARSE_ERROR.
constexpr int er_srs_parse_error = 3517;

/// Server error specific to mysql. Error number: 3518, symbol: ER_SRS_PROJ_PARAMETER_MISSING.
constexpr int er_srs_proj_parameter_missing = 3518;

/// Server error specific to mysql. Error number: 3519, symbol: ER_WARN_SRS_NOT_FOUND.
constexpr int er_warn_srs_not_found = 3519;

/// Server error specific to mysql. Error number: 3520, symbol: ER_SRS_NOT_CARTESIAN.
constexpr int er_srs_not_cartesian = 3520;

/// Server error specific to mysql. Error number: 3521, symbol: ER_SRS_NOT_CARTESIAN_UNDEFINED.
constexpr int er_srs_not_cartesian_undefined = 3521;

/// Server error specific to mysql. Error number: 3522, symbol: ER_PK_INDEX_CANT_BE_INVISIBLE.
constexpr int er_pk_index_cant_be_invisible = 3522;

/// Server error specific to mysql. Error number: 3523, symbol: ER_UNKNOWN_AUTHID.
constexpr int er_unknown_authid = 3523;

/// Server error specific to mysql. Error number: 3524, symbol: ER_FAILED_ROLE_GRANT.
constexpr int er_failed_role_grant = 3524;

/// Server error specific to mysql. Error number: 3525, symbol: ER_OPEN_ROLE_TABLES.
constexpr int er_open_role_tables = 3525;

/// Server error specific to mysql. Error number: 3526, symbol: ER_FAILED_DEFAULT_ROLES.
constexpr int er_failed_default_roles = 3526;

/// Server error specific to mysql. Error number: 3527, symbol: ER_COMPONENTS_NO_SCHEME.
constexpr int er_components_no_scheme = 3527;

/// Server error specific to mysql. Error number: 3528, symbol: ER_COMPONENTS_NO_SCHEME_SERVICE.
constexpr int er_components_no_scheme_service = 3528;

/// Server error specific to mysql. Error number: 3529, symbol: ER_COMPONENTS_CANT_LOAD.
constexpr int er_components_cant_load = 3529;

/// Server error specific to mysql. Error number: 3530, symbol: ER_ROLE_NOT_GRANTED.
constexpr int er_role_not_granted = 3530;

/// Server error specific to mysql. Error number: 3531, symbol: ER_FAILED_REVOKE_ROLE.
constexpr int er_failed_revoke_role = 3531;

/// Server error specific to mysql. Error number: 3532, symbol: ER_RENAME_ROLE.
constexpr int er_rename_role = 3532;

/// Server error specific to mysql. Error number: 3533, symbol: ER_COMPONENTS_CANT_ACQUIRE_SERVICE_IMPLEMENTATION.
constexpr int er_components_cant_acquire_service_implementation = 3533;

/// Server error specific to mysql. Error number: 3534, symbol: ER_COMPONENTS_CANT_SATISFY_DEPENDENCY.
constexpr int er_components_cant_satisfy_dependency = 3534;

/// Server error specific to mysql. Error number: 3535, symbol: ER_COMPONENTS_LOAD_CANT_REGISTER_SERVICE_IMPLEMENTATION.
constexpr int er_components_load_cant_register_service_implementation = 3535;

/// Server error specific to mysql. Error number: 3536, symbol: ER_COMPONENTS_LOAD_CANT_INITIALIZE.
constexpr int er_components_load_cant_initialize = 3536;

/// Server error specific to mysql. Error number: 3537, symbol: ER_COMPONENTS_UNLOAD_NOT_LOADED.
constexpr int er_components_unload_not_loaded = 3537;

/// Server error specific to mysql. Error number: 3538, symbol: ER_COMPONENTS_UNLOAD_CANT_DEINITIALIZE.
constexpr int er_components_unload_cant_deinitialize = 3538;

/// Server error specific to mysql. Error number: 3539, symbol: ER_COMPONENTS_CANT_RELEASE_SERVICE.
constexpr int er_components_cant_release_service = 3539;

/// Server error specific to mysql. Error number: 3540, symbol: ER_COMPONENTS_UNLOAD_CANT_UNREGISTER_SERVICE.
constexpr int er_components_unload_cant_unregister_service = 3540;

/// Server error specific to mysql. Error number: 3541, symbol: ER_COMPONENTS_CANT_UNLOAD.
constexpr int er_components_cant_unload = 3541;

/// Server error specific to mysql. Error number: 3542, symbol: ER_WARN_UNLOAD_THE_NOT_PERSISTED.
constexpr int er_warn_unload_the_not_persisted = 3542;

/// Server error specific to mysql. Error number: 3543, symbol: ER_COMPONENT_TABLE_INCORRECT.
constexpr int er_component_table_incorrect = 3543;

/// Server error specific to mysql. Error number: 3544, symbol: ER_COMPONENT_MANIPULATE_ROW_FAILED.
constexpr int er_component_manipulate_row_failed = 3544;

/// Server error specific to mysql. Error number: 3545, symbol: ER_COMPONENTS_UNLOAD_DUPLICATE_IN_GROUP.
constexpr int er_components_unload_duplicate_in_group = 3545;

/// Server error specific to mysql. Error number: 3546, symbol: ER_CANT_SET_GTID_PURGED_DUE_SETS_CONSTRAINTS.
constexpr int er_cant_set_gtid_purged_due_sets_constraints = 3546;

/// Server error specific to mysql. Error number: 3547, symbol: ER_CANNOT_LOCK_USER_MANAGEMENT_CACHES.
constexpr int er_cannot_lock_user_management_caches = 3547;

/// Server error specific to mysql. Error number: 3548, symbol: ER_SRS_NOT_FOUND.
constexpr int er_srs_not_found = 3548;

/// Server error specific to mysql. Error number: 3549, symbol: ER_VARIABLE_NOT_PERSISTED.
constexpr int er_variable_not_persisted = 3549;

/// Server error specific to mysql. Error number: 3550, symbol: ER_IS_QUERY_INVALID_CLAUSE.
constexpr int er_is_query_invalid_clause = 3550;

/// Server error specific to mysql. Error number: 3551, symbol: ER_UNABLE_TO_STORE_STATISTICS.
constexpr int er_unable_to_store_statistics = 3551;

/// Server error specific to mysql. Error number: 3552, symbol: ER_NO_SYSTEM_SCHEMA_ACCESS.
constexpr int er_no_system_schema_access = 3552;

/// Server error specific to mysql. Error number: 3553, symbol: ER_NO_SYSTEM_TABLESPACE_ACCESS.
constexpr int er_no_system_tablespace_access = 3553;

/// Server error specific to mysql. Error number: 3554, symbol: ER_NO_SYSTEM_TABLE_ACCESS.
constexpr int er_no_system_table_access = 3554;

/// Server error specific to mysql. Error number: 3555, symbol: ER_NO_SYSTEM_TABLE_ACCESS_FOR_DICTIONARY_TABLE.
constexpr int er_no_system_table_access_for_dictionary_table = 3555;

/// Server error specific to mysql. Error number: 3556, symbol: ER_NO_SYSTEM_TABLE_ACCESS_FOR_SYSTEM_TABLE.
constexpr int er_no_system_table_access_for_system_table = 3556;

/// Server error specific to mysql. Error number: 3557, symbol: ER_NO_SYSTEM_TABLE_ACCESS_FOR_TABLE.
constexpr int er_no_system_table_access_for_table = 3557;

/// Server error specific to mysql. Error number: 3558, symbol: ER_INVALID_OPTION_KEY.
constexpr int er_invalid_option_key = 3558;

/// Server error specific to mysql. Error number: 3559, symbol: ER_INVALID_OPTION_VALUE.
constexpr int er_invalid_option_value = 3559;

/// Server error specific to mysql. Error number: 3560, symbol: ER_INVALID_OPTION_KEY_VALUE_PAIR.
constexpr int er_invalid_option_key_value_pair = 3560;

/// Server error specific to mysql. Error number: 3561, symbol: ER_INVALID_OPTION_START_CHARACTER.
constexpr int er_invalid_option_start_character = 3561;

/// Server error specific to mysql. Error number: 3562, symbol: ER_INVALID_OPTION_END_CHARACTER.
constexpr int er_invalid_option_end_character = 3562;

/// Server error specific to mysql. Error number: 3563, symbol: ER_INVALID_OPTION_CHARACTERS.
constexpr int er_invalid_option_characters = 3563;

/// Server error specific to mysql. Error number: 3564, symbol: ER_DUPLICATE_OPTION_KEY.
constexpr int er_duplicate_option_key = 3564;

/// Server error specific to mysql. Error number: 3565, symbol: ER_WARN_SRS_NOT_FOUND_AXIS_ORDER.
constexpr int er_warn_srs_not_found_axis_order = 3565;

/// Server error specific to mysql. Error number: 3566, symbol: ER_NO_ACCESS_TO_NATIVE_FCT.
constexpr int er_no_access_to_native_fct = 3566;

/// Server error specific to mysql. Error number: 3567, symbol: ER_RESET_MASTER_TO_VALUE_OUT_OF_RANGE.
constexpr int er_reset_master_to_value_out_of_range = 3567;

/// Server error specific to mysql. Error number: 3568, symbol: ER_UNRESOLVED_TABLE_LOCK.
constexpr int er_unresolved_table_lock = 3568;

/// Server error specific to mysql. Error number: 3569, symbol: ER_DUPLICATE_TABLE_LOCK.
constexpr int er_duplicate_table_lock = 3569;

/// Server error specific to mysql. Error number: 3570, symbol: ER_BINLOG_UNSAFE_SKIP_LOCKED.
constexpr int er_binlog_unsafe_skip_locked = 3570;

/// Server error specific to mysql. Error number: 3571, symbol: ER_BINLOG_UNSAFE_NOWAIT.
constexpr int er_binlog_unsafe_nowait = 3571;

/// Server error specific to mysql. Error number: 3572, symbol: ER_LOCK_NOWAIT.
constexpr int er_lock_nowait = 3572;

/// Server error specific to mysql. Error number: 3573, symbol: ER_CTE_RECURSIVE_REQUIRES_UNION.
constexpr int er_cte_recursive_requires_union = 3573;

/// Server error specific to mysql. Error number: 3574, symbol: ER_CTE_RECURSIVE_REQUIRES_NONRECURSIVE_FIRST.
constexpr int er_cte_recursive_requires_nonrecursive_first = 3574;

/// Server error specific to mysql. Error number: 3575, symbol: ER_CTE_RECURSIVE_FORBIDS_AGGREGATION.
constexpr int er_cte_recursive_forbids_aggregation = 3575;

/// Server error specific to mysql. Error number: 3576, symbol: ER_CTE_RECURSIVE_FORBIDDEN_JOIN_ORDER.
constexpr int er_cte_recursive_forbidden_join_order = 3576;

/// Server error specific to mysql. Error number: 3577, symbol: ER_CTE_RECURSIVE_REQUIRES_SINGLE_REFERENCE.
constexpr int er_cte_recursive_requires_single_reference = 3577;

/// Server error specific to mysql. Error number: 3578, symbol: ER_SWITCH_TMP_ENGINE.
constexpr int er_switch_tmp_engine = 3578;

/// Server error specific to mysql. Error number: 3579, symbol: ER_WINDOW_NO_SUCH_WINDOW.
constexpr int er_window_no_such_window = 3579;

/// Server error specific to mysql. Error number: 3580, symbol: ER_WINDOW_CIRCULARITY_IN_WINDOW_GRAPH.
constexpr int er_window_circularity_in_window_graph = 3580;

/// Server error specific to mysql. Error number: 3581, symbol: ER_WINDOW_NO_CHILD_PARTITIONING.
constexpr int er_window_no_child_partitioning = 3581;

/// Server error specific to mysql. Error number: 3582, symbol: ER_WINDOW_NO_INHERIT_FRAME.
constexpr int er_window_no_inherit_frame = 3582;

/// Server error specific to mysql. Error number: 3583, symbol: ER_WINDOW_NO_REDEFINE_ORDER_BY.
constexpr int er_window_no_redefine_order_by = 3583;

/// Server error specific to mysql. Error number: 3584, symbol: ER_WINDOW_FRAME_START_ILLEGAL.
constexpr int er_window_frame_start_illegal = 3584;

/// Server error specific to mysql. Error number: 3585, symbol: ER_WINDOW_FRAME_END_ILLEGAL.
constexpr int er_window_frame_end_illegal = 3585;

/// Server error specific to mysql. Error number: 3586, symbol: ER_WINDOW_FRAME_ILLEGAL.
constexpr int er_window_frame_illegal = 3586;

/// Server error specific to mysql. Error number: 3587, symbol: ER_WINDOW_RANGE_FRAME_ORDER_TYPE.
constexpr int er_window_range_frame_order_type = 3587;

/// Server error specific to mysql. Error number: 3588, symbol: ER_WINDOW_RANGE_FRAME_TEMPORAL_TYPE.
constexpr int er_window_range_frame_temporal_type = 3588;

/// Server error specific to mysql. Error number: 3589, symbol: ER_WINDOW_RANGE_FRAME_NUMERIC_TYPE.
constexpr int er_window_range_frame_numeric_type = 3589;

/// Server error specific to mysql. Error number: 3590, symbol: ER_WINDOW_RANGE_BOUND_NOT_CONSTANT.
constexpr int er_window_range_bound_not_constant = 3590;

/// Server error specific to mysql. Error number: 3591, symbol: ER_WINDOW_DUPLICATE_NAME.
constexpr int er_window_duplicate_name = 3591;

/// Server error specific to mysql. Error number: 3592, symbol: ER_WINDOW_ILLEGAL_ORDER_BY.
constexpr int er_window_illegal_order_by = 3592;

/// Server error specific to mysql. Error number: 3593, symbol: ER_WINDOW_INVALID_WINDOW_FUNC_USE.
constexpr int er_window_invalid_window_func_use = 3593;

/// Server error specific to mysql. Error number: 3594, symbol: ER_WINDOW_INVALID_WINDOW_FUNC_ALIAS_USE.
constexpr int er_window_invalid_window_func_alias_use = 3594;

/// Server error specific to mysql. Error number: 3595, symbol: ER_WINDOW_NESTED_WINDOW_FUNC_USE_IN_WINDOW_SPEC.
constexpr int er_window_nested_window_func_use_in_window_spec = 3595;

/// Server error specific to mysql. Error number: 3596, symbol: ER_WINDOW_ROWS_INTERVAL_USE.
constexpr int er_window_rows_interval_use = 3596;

/// Server error specific to mysql. Error number: 3597, symbol: ER_WINDOW_NO_GROUP_ORDER_UNUSED.
constexpr int er_window_no_group_order_unused = 3597;

/// Server error specific to mysql. Error number: 3598, symbol: ER_WINDOW_EXPLAIN_JSON.
constexpr int er_window_explain_json = 3598;

/// Server error specific to mysql. Error number: 3599, symbol: ER_WINDOW_FUNCTION_IGNORES_FRAME.
constexpr int er_window_function_ignores_frame = 3599;

/// Server error specific to mysql. Error number: 3600, symbol: ER_WL9236_NOW_UNUSED.
constexpr int er_wl9236_now_unused = 3600;

/// Server error specific to mysql. Error number: 3601, symbol: ER_INVALID_NO_OF_ARGS.
constexpr int er_invalid_no_of_args = 3601;

/// Server error specific to mysql. Error number: 3602, symbol: ER_FIELD_IN_GROUPING_NOT_GROUP_BY.
constexpr int er_field_in_grouping_not_group_by = 3602;

/// Server error specific to mysql. Error number: 3603, symbol: ER_TOO_LONG_TABLESPACE_COMMENT.
constexpr int er_too_long_tablespace_comment = 3603;

/// Server error specific to mysql. Error number: 3604, symbol: ER_ENGINE_CANT_DROP_TABLE.
constexpr int er_engine_cant_drop_table = 3604;

/// Server error specific to mysql. Error number: 3605, symbol: ER_ENGINE_CANT_DROP_MISSING_TABLE.
constexpr int er_engine_cant_drop_missing_table = 3605;

/// Server error specific to mysql. Error number: 3606, symbol: ER_TABLESPACE_DUP_FILENAME.
constexpr int er_tablespace_dup_filename = 3606;

/// Server error specific to mysql. Error number: 3607, symbol: ER_DB_DROP_RMDIR2.
constexpr int er_db_drop_rmdir2 = 3607;

/// Server error specific to mysql. Error number: 3608, symbol: ER_IMP_NO_FILES_MATCHED.
constexpr int er_imp_no_files_matched = 3608;

/// Server error specific to mysql. Error number: 3609, symbol: ER_IMP_SCHEMA_DOES_NOT_EXIST.
constexpr int er_imp_schema_does_not_exist = 3609;

/// Server error specific to mysql. Error number: 3610, symbol: ER_IMP_TABLE_ALREADY_EXISTS.
constexpr int er_imp_table_already_exists = 3610;

/// Server error specific to mysql. Error number: 3611, symbol: ER_IMP_INCOMPATIBLE_MYSQLD_VERSION.
constexpr int er_imp_incompatible_mysqld_version = 3611;

/// Server error specific to mysql. Error number: 3612, symbol: ER_IMP_INCOMPATIBLE_DD_VERSION.
constexpr int er_imp_incompatible_dd_version = 3612;

/// Server error specific to mysql. Error number: 3613, symbol: ER_IMP_INCOMPATIBLE_SDI_VERSION.
constexpr int er_imp_incompatible_sdi_version = 3613;

/// Server error specific to mysql. Error number: 3614, symbol: ER_WARN_INVALID_HINT.
constexpr int er_warn_invalid_hint = 3614;

/// Server error specific to mysql. Error number: 3615, symbol: ER_VAR_DOES_NOT_EXIST.
constexpr int er_var_does_not_exist = 3615;

/// Server error specific to mysql. Error number: 3616, symbol: ER_LONGITUDE_OUT_OF_RANGE.
constexpr int er_longitude_out_of_range = 3616;

/// Server error specific to mysql. Error number: 3617, symbol: ER_LATITUDE_OUT_OF_RANGE.
constexpr int er_latitude_out_of_range = 3617;

/// Server error specific to mysql. Error number: 3618, symbol: ER_NOT_IMPLEMENTED_FOR_GEOGRAPHIC_SRS.
constexpr int er_not_implemented_for_geographic_srs = 3618;

/// Server error specific to mysql. Error number: 3619, symbol: ER_ILLEGAL_PRIVILEGE_LEVEL.
constexpr int er_illegal_privilege_level = 3619;

/// Server error specific to mysql. Error number: 3620, symbol: ER_NO_SYSTEM_VIEW_ACCESS.
constexpr int er_no_system_view_access = 3620;

/// Server error specific to mysql. Error number: 3621, symbol: ER_COMPONENT_FILTER_FLABBERGASTED.
constexpr int er_component_filter_flabbergasted = 3621;

/// Server error specific to mysql. Error number: 3622, symbol: ER_PART_EXPR_TOO_LONG.
constexpr int er_part_expr_too_long = 3622;

/// Server error specific to mysql. Error number: 3623, symbol: ER_UDF_DROP_DYNAMICALLY_REGISTERED.
constexpr int er_udf_drop_dynamically_registered = 3623;

/// Server error specific to mysql. Error number: 3624, symbol: ER_UNABLE_TO_STORE_COLUMN_STATISTICS.
constexpr int er_unable_to_store_column_statistics = 3624;

/// Server error specific to mysql. Error number: 3625, symbol: ER_UNABLE_TO_UPDATE_COLUMN_STATISTICS.
constexpr int er_unable_to_update_column_statistics = 3625;

/// Server error specific to mysql. Error number: 3626, symbol: ER_UNABLE_TO_DROP_COLUMN_STATISTICS.
constexpr int er_unable_to_drop_column_statistics = 3626;

/// Server error specific to mysql. Error number: 3627, symbol: ER_UNABLE_TO_BUILD_HISTOGRAM.
constexpr int er_unable_to_build_histogram = 3627;

/// Server error specific to mysql. Error number: 3628, symbol: ER_MANDATORY_ROLE.
constexpr int er_mandatory_role = 3628;

/// Server error specific to mysql. Error number: 3629, symbol: ER_MISSING_TABLESPACE_FILE.
constexpr int er_missing_tablespace_file = 3629;

/// Server error specific to mysql. Error number: 3630, symbol: ER_PERSIST_ONLY_ACCESS_DENIED_ERROR.
constexpr int er_persist_only_access_denied_error = 3630;

/// Server error specific to mysql. Error number: 3631, symbol: ER_CMD_NEED_SUPER.
constexpr int er_cmd_need_super = 3631;

/// Server error specific to mysql. Error number: 3632, symbol: ER_PATH_IN_DATADIR.
constexpr int er_path_in_datadir = 3632;

/// Server error specific to mysql. Error number: 3633, symbol: ER_CLONE_DDL_IN_PROGRESS.
constexpr int er_clone_ddl_in_progress = 3633;

/// Server error specific to mysql. Error number: 3634, symbol: ER_CLONE_TOO_MANY_CONCURRENT_CLONES.
constexpr int er_clone_too_many_concurrent_clones = 3634;

/// Server error specific to mysql. Error number: 3635, symbol: ER_APPLIER_LOG_EVENT_VALIDATION_ERROR.
constexpr int er_applier_log_event_validation_error = 3635;

/// Server error specific to mysql. Error number: 3636, symbol: ER_CTE_MAX_RECURSION_DEPTH.
constexpr int er_cte_max_recursion_depth = 3636;

/// Server error specific to mysql. Error number: 3637, symbol: ER_NOT_HINT_UPDATABLE_VARIABLE.
constexpr int er_not_hint_updatable_variable = 3637;

/// Server error specific to mysql. Error number: 3638, symbol: ER_CREDENTIALS_CONTRADICT_TO_HISTORY.
constexpr int er_credentials_contradict_to_history = 3638;

/// Server error specific to mysql. Error number: 3639, symbol: ER_WARNING_PASSWORD_HISTORY_CLAUSES_VOID.
constexpr int er_warning_password_history_clauses_void = 3639;

/// Server error specific to mysql. Error number: 3640, symbol: ER_CLIENT_DOES_NOT_SUPPORT.
constexpr int er_client_does_not_support = 3640;

/// Server error specific to mysql. Error number: 3641, symbol: ER_I_S_SKIPPED_TABLESPACE.
constexpr int er_i_s_skipped_tablespace = 3641;

/// Server error specific to mysql. Error number: 3642, symbol: ER_TABLESPACE_ENGINE_MISMATCH.
constexpr int er_tablespace_engine_mismatch = 3642;

/// Server error specific to mysql. Error number: 3643, symbol: ER_WRONG_SRID_FOR_COLUMN.
constexpr int er_wrong_srid_for_column = 3643;

/// Server error specific to mysql. Error number: 3644, symbol: ER_CANNOT_ALTER_SRID_DUE_TO_INDEX.
constexpr int er_cannot_alter_srid_due_to_index = 3644;

/// Server error specific to mysql. Error number: 3645, symbol: ER_WARN_BINLOG_PARTIAL_UPDATES_DISABLED.
constexpr int er_warn_binlog_partial_updates_disabled = 3645;

/// Server error specific to mysql. Error number: 3646, symbol: ER_WARN_BINLOG_V1_ROW_EVENTS_DISABLED.
constexpr int er_warn_binlog_v1_row_events_disabled = 3646;

/// Server error specific to mysql. Error number: 3647, symbol: ER_WARN_BINLOG_PARTIAL_UPDATES_SUGGESTS_PARTIAL_IMAGES.
constexpr int er_warn_binlog_partial_updates_suggests_partial_images = 3647;

/// Server error specific to mysql. Error number: 3648, symbol: ER_COULD_NOT_APPLY_JSON_DIFF.
constexpr int er_could_not_apply_json_diff = 3648;

/// Server error specific to mysql. Error number: 3649, symbol: ER_CORRUPTED_JSON_DIFF.
constexpr int er_corrupted_json_diff = 3649;

/// Server error specific to mysql. Error number: 3650, symbol: ER_RESOURCE_GROUP_EXISTS.
constexpr int er_resource_group_exists = 3650;

/// Server error specific to mysql. Error number: 3651, symbol: ER_RESOURCE_GROUP_NOT_EXISTS.
constexpr int er_resource_group_not_exists = 3651;

/// Server error specific to mysql. Error number: 3652, symbol: ER_INVALID_VCPU_ID.
constexpr int er_invalid_vcpu_id = 3652;

/// Server error specific to mysql. Error number: 3653, symbol: ER_INVALID_VCPU_RANGE.
constexpr int er_invalid_vcpu_range = 3653;

/// Server error specific to mysql. Error number: 3654, symbol: ER_INVALID_THREAD_PRIORITY.
constexpr int er_invalid_thread_priority = 3654;

/// Server error specific to mysql. Error number: 3655, symbol: ER_DISALLOWED_OPERATION.
constexpr int er_disallowed_operation = 3655;

/// Server error specific to mysql. Error number: 3656, symbol: ER_RESOURCE_GROUP_BUSY.
constexpr int er_resource_group_busy = 3656;

/// Server error specific to mysql. Error number: 3657, symbol: ER_RESOURCE_GROUP_DISABLED.
constexpr int er_resource_group_disabled = 3657;

/// Server error specific to mysql. Error number: 3658, symbol: ER_FEATURE_UNSUPPORTED.
constexpr int er_feature_unsupported = 3658;

/// Server error specific to mysql. Error number: 3659, symbol: ER_ATTRIBUTE_IGNORED.
constexpr int er_attribute_ignored = 3659;

/// Server error specific to mysql. Error number: 3660, symbol: ER_INVALID_THREAD_ID.
constexpr int er_invalid_thread_id = 3660;

/// Server error specific to mysql. Error number: 3661, symbol: ER_RESOURCE_GROUP_BIND_FAILED.
constexpr int er_resource_group_bind_failed = 3661;

/// Server error specific to mysql. Error number: 3662, symbol: ER_INVALID_USE_OF_FORCE_OPTION.
constexpr int er_invalid_use_of_force_option = 3662;

/// Server error specific to mysql. Error number: 3663, symbol: ER_GROUP_REPLICATION_COMMAND_FAILURE.
constexpr int er_group_replication_command_failure = 3663;

/// Server error specific to mysql. Error number: 3664, symbol: ER_SDI_OPERATION_FAILED.
constexpr int er_sdi_operation_failed = 3664;

/// Server error specific to mysql. Error number: 3665, symbol: ER_MISSING_JSON_TABLE_VALUE.
constexpr int er_missing_json_table_value = 3665;

/// Server error specific to mysql. Error number: 3666, symbol: ER_WRONG_JSON_TABLE_VALUE.
constexpr int er_wrong_json_table_value = 3666;

/// Server error specific to mysql. Error number: 3667, symbol: ER_TF_MUST_HAVE_ALIAS.
constexpr int er_tf_must_have_alias = 3667;

/// Server error specific to mysql. Error number: 3668, symbol: ER_TF_FORBIDDEN_JOIN_TYPE.
constexpr int er_tf_forbidden_join_type = 3668;

/// Server error specific to mysql. Error number: 3669, symbol: ER_JT_VALUE_OUT_OF_RANGE.
constexpr int er_jt_value_out_of_range = 3669;

/// Server error specific to mysql. Error number: 3670, symbol: ER_JT_MAX_NESTED_PATH.
constexpr int er_jt_max_nested_path = 3670;

/// Server error specific to mysql. Error number: 3671, symbol: ER_PASSWORD_EXPIRATION_NOT_SUPPORTED_BY_AUTH_METHOD.
constexpr int er_password_expiration_not_supported_by_auth_method = 3671;

/// Server error specific to mysql. Error number: 3672, symbol: ER_INVALID_GEOJSON_CRS_NOT_TOP_LEVEL.
constexpr int er_invalid_geojson_crs_not_top_level = 3672;

/// Server error specific to mysql. Error number: 3673, symbol: ER_BAD_NULL_ERROR_NOT_IGNORED.
constexpr int er_bad_null_error_not_ignored = 3673;

/// Server error specific to mysql. Error number: 3674, symbol: WARN_USELESS_SPATIAL_INDEX.
constexpr int warn_useless_spatial_index = 3674;

/// Server error specific to mysql. Error number: 3675, symbol: ER_DISK_FULL_NOWAIT.
constexpr int er_disk_full_nowait = 3675;

/// Server error specific to mysql. Error number: 3676, symbol: ER_PARSE_ERROR_IN_DIGEST_FN.
constexpr int er_parse_error_in_digest_fn = 3676;

/// Server error specific to mysql. Error number: 3677, symbol: ER_UNDISCLOSED_PARSE_ERROR_IN_DIGEST_FN.
constexpr int er_undisclosed_parse_error_in_digest_fn = 3677;

/// Server error specific to mysql. Error number: 3678, symbol: ER_SCHEMA_DIR_EXISTS.
constexpr int er_schema_dir_exists = 3678;

/// Server error specific to mysql. Error number: 3679, symbol: ER_SCHEMA_DIR_MISSING.
constexpr int er_schema_dir_missing = 3679;

/// Server error specific to mysql. Error number: 3680, symbol: ER_SCHEMA_DIR_CREATE_FAILED.
constexpr int er_schema_dir_create_failed = 3680;

/// Server error specific to mysql. Error number: 3681, symbol: ER_SCHEMA_DIR_UNKNOWN.
constexpr int er_schema_dir_unknown = 3681;

/// Server error specific to mysql. Error number: 3682, symbol: ER_ONLY_IMPLEMENTED_FOR_SRID_0_AND_4326.
constexpr int er_only_implemented_for_srid_0_and_4326 = 3682;

/// Server error specific to mysql. Error number: 3683, symbol: ER_BINLOG_EXPIRE_LOG_DAYS_AND_SECS_USED_TOGETHER.
constexpr int er_binlog_expire_log_days_and_secs_used_together = 3683;

/// Server error specific to mysql. Error number: 3684, symbol: ER_REGEXP_BUFFER_OVERFLOW.
constexpr int er_regexp_buffer_overflow = 3684;

/// Server error specific to mysql. Error number: 3685, symbol: ER_REGEXP_ILLEGAL_ARGUMENT.
constexpr int er_regexp_illegal_argument = 3685;

/// Server error specific to mysql. Error number: 3686, symbol: ER_REGEXP_INDEX_OUTOFBOUNDS_ERROR.
constexpr int er_regexp_index_outofbounds_error = 3686;

/// Server error specific to mysql. Error number: 3687, symbol: ER_REGEXP_INTERNAL_ERROR.
constexpr int er_regexp_internal_error = 3687;

/// Server error specific to mysql. Error number: 3688, symbol: ER_REGEXP_RULE_SYNTAX.
constexpr int er_regexp_rule_syntax = 3688;

/// Server error specific to mysql. Error number: 3689, symbol: ER_REGEXP_BAD_ESCAPE_SEQUENCE.
constexpr int er_regexp_bad_escape_sequence = 3689;

/// Server error specific to mysql. Error number: 3690, symbol: ER_REGEXP_UNIMPLEMENTED.
constexpr int er_regexp_unimplemented = 3690;

/// Server error specific to mysql. Error number: 3691, symbol: ER_REGEXP_MISMATCHED_PAREN.
constexpr int er_regexp_mismatched_paren = 3691;

/// Server error specific to mysql. Error number: 3692, symbol: ER_REGEXP_BAD_INTERVAL.
constexpr int er_regexp_bad_interval = 3692;

/// Server error specific to mysql. Error number: 3693, symbol: ER_REGEXP_MAX_LT_MIN.
constexpr int er_regexp_max_lt_min = 3693;

/// Server error specific to mysql. Error number: 3694, symbol: ER_REGEXP_INVALID_BACK_REF.
constexpr int er_regexp_invalid_back_ref = 3694;

/// Server error specific to mysql. Error number: 3695, symbol: ER_REGEXP_LOOK_BEHIND_LIMIT.
constexpr int er_regexp_look_behind_limit = 3695;

/// Server error specific to mysql. Error number: 3696, symbol: ER_REGEXP_MISSING_CLOSE_BRACKET.
constexpr int er_regexp_missing_close_bracket = 3696;

/// Server error specific to mysql. Error number: 3697, symbol: ER_REGEXP_INVALID_RANGE.
constexpr int er_regexp_invalid_range = 3697;

/// Server error specific to mysql. Error number: 3698, symbol: ER_REGEXP_STACK_OVERFLOW.
constexpr int er_regexp_stack_overflow = 3698;

/// Server error specific to mysql. Error number: 3699, symbol: ER_REGEXP_TIME_OUT.
constexpr int er_regexp_time_out = 3699;

/// Server error specific to mysql. Error number: 3700, symbol: ER_REGEXP_PATTERN_TOO_BIG.
constexpr int er_regexp_pattern_too_big = 3700;

/// Server error specific to mysql. Error number: 3701, symbol: ER_CANT_SET_ERROR_LOG_SERVICE.
constexpr int er_cant_set_error_log_service = 3701;

/// Server error specific to mysql. Error number: 3702, symbol: ER_EMPTY_PIPELINE_FOR_ERROR_LOG_SERVICE.
constexpr int er_empty_pipeline_for_error_log_service = 3702;

/// Server error specific to mysql. Error number: 3703, symbol: ER_COMPONENT_FILTER_DIAGNOSTICS.
constexpr int er_component_filter_diagnostics = 3703;

/// Server error specific to mysql. Error number: 3704, symbol: ER_NOT_IMPLEMENTED_FOR_CARTESIAN_SRS.
constexpr int er_not_implemented_for_cartesian_srs = 3704;

/// Server error specific to mysql. Error number: 3705, symbol: ER_NOT_IMPLEMENTED_FOR_PROJECTED_SRS.
constexpr int er_not_implemented_for_projected_srs = 3705;

/// Server error specific to mysql. Error number: 3706, symbol: ER_NONPOSITIVE_RADIUS.
constexpr int er_nonpositive_radius = 3706;

/// Server error specific to mysql. Error number: 3707, symbol: ER_RESTART_SERVER_FAILED.
constexpr int er_restart_server_failed = 3707;

/// Server error specific to mysql. Error number: 3708, symbol: ER_SRS_MISSING_MANDATORY_ATTRIBUTE.
constexpr int er_srs_missing_mandatory_attribute = 3708;

/// Server error specific to mysql. Error number: 3709, symbol: ER_SRS_MULTIPLE_ATTRIBUTE_DEFINITIONS.
constexpr int er_srs_multiple_attribute_definitions = 3709;

/// Server error specific to mysql. Error number: 3710, symbol: ER_SRS_NAME_CANT_BE_EMPTY_OR_WHITESPACE.
constexpr int er_srs_name_cant_be_empty_or_whitespace = 3710;

/// Server error specific to mysql. Error number: 3711, symbol: ER_SRS_ORGANIZATION_CANT_BE_EMPTY_OR_WHITESPACE.
constexpr int er_srs_organization_cant_be_empty_or_whitespace = 3711;

/// Server error specific to mysql. Error number: 3712, symbol: ER_SRS_ID_ALREADY_EXISTS.
constexpr int er_srs_id_already_exists = 3712;

/// Server error specific to mysql. Error number: 3713, symbol: ER_WARN_SRS_ID_ALREADY_EXISTS.
constexpr int er_warn_srs_id_already_exists = 3713;

/// Server error specific to mysql. Error number: 3714, symbol: ER_CANT_MODIFY_SRID_0.
constexpr int er_cant_modify_srid_0 = 3714;

/// Server error specific to mysql. Error number: 3715, symbol: ER_WARN_RESERVED_SRID_RANGE.
constexpr int er_warn_reserved_srid_range = 3715;

/// Server error specific to mysql. Error number: 3716, symbol: ER_CANT_MODIFY_SRS_USED_BY_COLUMN.
constexpr int er_cant_modify_srs_used_by_column = 3716;

/// Server error specific to mysql. Error number: 3717, symbol: ER_SRS_INVALID_CHARACTER_IN_ATTRIBUTE.
constexpr int er_srs_invalid_character_in_attribute = 3717;

/// Server error specific to mysql. Error number: 3718, symbol: ER_SRS_ATTRIBUTE_STRING_TOO_LONG.
constexpr int er_srs_attribute_string_too_long = 3718;

/// Server error specific to mysql. Error number: 3719, symbol: ER_DEPRECATED_UTF8_ALIAS.
constexpr int er_deprecated_utf8_alias = 3719;

/// Server error specific to mysql. Error number: 3720, symbol: ER_DEPRECATED_NATIONAL.
constexpr int er_deprecated_national = 3720;

/// Server error specific to mysql. Error number: 3721, symbol: ER_INVALID_DEFAULT_UTF8MB4_COLLATION.
constexpr int er_invalid_default_utf8mb4_collation = 3721;

/// Server error specific to mysql. Error number: 3722, symbol: ER_UNABLE_TO_COLLECT_LOG_STATUS.
constexpr int er_unable_to_collect_log_status = 3722;

/// Server error specific to mysql. Error number: 3723, symbol: ER_RESERVED_TABLESPACE_NAME.
constexpr int er_reserved_tablespace_name = 3723;

/// Server error specific to mysql. Error number: 3724, symbol: ER_UNABLE_TO_SET_OPTION.
constexpr int er_unable_to_set_option = 3724;

/// Server error specific to mysql. Error number: 3725, symbol: ER_SLAVE_POSSIBLY_DIVERGED_AFTER_DDL.
constexpr int er_slave_possibly_diverged_after_ddl = 3725;

/// Server error specific to mysql. Error number: 3726, symbol: ER_SRS_NOT_GEOGRAPHIC.
constexpr int er_srs_not_geographic = 3726;

/// Server error specific to mysql. Error number: 3727, symbol: ER_POLYGON_TOO_LARGE.
constexpr int er_polygon_too_large = 3727;

/// Server error specific to mysql. Error number: 3728, symbol: ER_SPATIAL_UNIQUE_INDEX.
constexpr int er_spatial_unique_index = 3728;

/// Server error specific to mysql. Error number: 3729, symbol: ER_INDEX_TYPE_NOT_SUPPORTED_FOR_SPATIAL_INDEX.
constexpr int er_index_type_not_supported_for_spatial_index = 3729;

/// Server error specific to mysql. Error number: 3730, symbol: ER_FK_CANNOT_DROP_PARENT.
constexpr int er_fk_cannot_drop_parent = 3730;

/// Server error specific to mysql. Error number: 3731, symbol: ER_GEOMETRY_PARAM_LONGITUDE_OUT_OF_RANGE.
constexpr int er_geometry_param_longitude_out_of_range = 3731;

/// Server error specific to mysql. Error number: 3732, symbol: ER_GEOMETRY_PARAM_LATITUDE_OUT_OF_RANGE.
constexpr int er_geometry_param_latitude_out_of_range = 3732;

/// Server error specific to mysql. Error number: 3733, symbol: ER_FK_CANNOT_USE_VIRTUAL_COLUMN.
constexpr int er_fk_cannot_use_virtual_column = 3733;

/// Server error specific to mysql. Error number: 3734, symbol: ER_FK_NO_COLUMN_PARENT.
constexpr int er_fk_no_column_parent = 3734;

/// Server error specific to mysql. Error number: 3735, symbol: ER_CANT_SET_ERROR_SUPPRESSION_LIST.
constexpr int er_cant_set_error_suppression_list = 3735;

/// Server error specific to mysql. Error number: 3736, symbol: ER_SRS_GEOGCS_INVALID_AXES.
constexpr int er_srs_geogcs_invalid_axes = 3736;

/// Server error specific to mysql. Error number: 3737, symbol: ER_SRS_INVALID_SEMI_MAJOR_AXIS.
constexpr int er_srs_invalid_semi_major_axis = 3737;

/// Server error specific to mysql. Error number: 3738, symbol: ER_SRS_INVALID_INVERSE_FLATTENING.
constexpr int er_srs_invalid_inverse_flattening = 3738;

/// Server error specific to mysql. Error number: 3739, symbol: ER_SRS_INVALID_ANGULAR_UNIT.
constexpr int er_srs_invalid_angular_unit = 3739;

/// Server error specific to mysql. Error number: 3740, symbol: ER_SRS_INVALID_PRIME_MERIDIAN.
constexpr int er_srs_invalid_prime_meridian = 3740;

/// Server error specific to mysql. Error number: 3741, symbol: ER_TRANSFORM_SOURCE_SRS_NOT_SUPPORTED.
constexpr int er_transform_source_srs_not_supported = 3741;

/// Server error specific to mysql. Error number: 3742, symbol: ER_TRANSFORM_TARGET_SRS_NOT_SUPPORTED.
constexpr int er_transform_target_srs_not_supported = 3742;

/// Server error specific to mysql. Error number: 3743, symbol: ER_TRANSFORM_SOURCE_SRS_MISSING_TOWGS84.
constexpr int er_transform_source_srs_missing_towgs84 = 3743;

/// Server error specific to mysql. Error number: 3744, symbol: ER_TRANSFORM_TARGET_SRS_MISSING_TOWGS84.
constexpr int er_transform_target_srs_missing_towgs84 = 3744;

/// Server error specific to mysql. Error number: 3745, symbol: ER_TEMP_TABLE_PREVENTS_SWITCH_SESSION_BINLOG_FORMAT.
constexpr int er_temp_table_prevents_switch_session_binlog_format = 3745;

/// Server error specific to mysql. Error number: 3746, symbol: ER_TEMP_TABLE_PREVENTS_SWITCH_GLOBAL_BINLOG_FORMAT.
constexpr int er_temp_table_prevents_switch_global_binlog_format = 3746;

/// Server error specific to mysql. Error number: 3747, symbol: ER_RUNNING_APPLIER_PREVENTS_SWITCH_GLOBAL_BINLOG_FORMAT.
constexpr int er_running_applier_prevents_switch_global_binlog_format = 3747;

/// Server error specific to mysql. Error number: 3748, symbol: ER_CLIENT_GTID_UNSAFE_CREATE_DROP_TEMP_TABLE_IN_TRX_IN_SBR.
constexpr int er_client_gtid_unsafe_create_drop_temp_table_in_trx_in_sbr = 3748;

/// Server error specific to mysql. Error number: 3750, symbol: ER_TABLE_WITHOUT_PK.
constexpr int er_table_without_pk = 3750;

/// Server error specific to mysql. Error number: 3751, symbol: ER_WARN_DATA_TRUNCATED_FUNCTIONAL_INDEX.
constexpr int er_warn_data_truncated_functional_index = 3751;

/// Server error specific to mysql. Error number: 3752, symbol: ER_WARN_DATA_OUT_OF_RANGE_FUNCTIONAL_INDEX.
constexpr int er_warn_data_out_of_range_functional_index = 3752;

/// Server error specific to mysql. Error number: 3753, symbol: ER_FUNCTIONAL_INDEX_ON_JSON_OR_GEOMETRY_FUNCTION.
constexpr int er_functional_index_on_json_or_geometry_function = 3753;

/// Server error specific to mysql. Error number: 3754, symbol: ER_FUNCTIONAL_INDEX_REF_AUTO_INCREMENT.
constexpr int er_functional_index_ref_auto_increment = 3754;

/// Server error specific to mysql. Error number: 3755, symbol: ER_CANNOT_DROP_COLUMN_FUNCTIONAL_INDEX.
constexpr int er_cannot_drop_column_functional_index = 3755;

/// Server error specific to mysql. Error number: 3756, symbol: ER_FUNCTIONAL_INDEX_PRIMARY_KEY.
constexpr int er_functional_index_primary_key = 3756;

/// Server error specific to mysql. Error number: 3757, symbol: ER_FUNCTIONAL_INDEX_ON_LOB.
constexpr int er_functional_index_on_lob = 3757;

/// Server error specific to mysql. Error number: 3758, symbol: ER_FUNCTIONAL_INDEX_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_functional_index_function_is_not_allowed = 3758;

/// Server error specific to mysql. Error number: 3759, symbol: ER_FULLTEXT_FUNCTIONAL_INDEX.
constexpr int er_fulltext_functional_index = 3759;

/// Server error specific to mysql. Error number: 3760, symbol: ER_SPATIAL_FUNCTIONAL_INDEX.
constexpr int er_spatial_functional_index = 3760;

/// Server error specific to mysql. Error number: 3761, symbol: ER_WRONG_KEY_COLUMN_FUNCTIONAL_INDEX.
constexpr int er_wrong_key_column_functional_index = 3761;

/// Server error specific to mysql. Error number: 3762, symbol: ER_FUNCTIONAL_INDEX_ON_FIELD.
constexpr int er_functional_index_on_field = 3762;

/// Server error specific to mysql. Error number: 3763, symbol: ER_GENERATED_COLUMN_NAMED_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_generated_column_named_function_is_not_allowed = 3763;

/// Server error specific to mysql. Error number: 3764, symbol: ER_GENERATED_COLUMN_ROW_VALUE.
constexpr int er_generated_column_row_value = 3764;

/// Server error specific to mysql. Error number: 3765, symbol: ER_GENERATED_COLUMN_VARIABLES.
constexpr int er_generated_column_variables = 3765;

/// Server error specific to mysql. Error number: 3766, symbol: ER_DEPENDENT_BY_DEFAULT_GENERATED_VALUE.
constexpr int er_dependent_by_default_generated_value = 3766;

/// Server error specific to mysql. Error number: 3767, symbol: ER_DEFAULT_VAL_GENERATED_NON_PRIOR.
constexpr int er_default_val_generated_non_prior = 3767;

/// Server error specific to mysql. Error number: 3768, symbol: ER_DEFAULT_VAL_GENERATED_REF_AUTO_INC.
constexpr int er_default_val_generated_ref_auto_inc = 3768;

/// Server error specific to mysql. Error number: 3769, symbol: ER_DEFAULT_VAL_GENERATED_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_default_val_generated_function_is_not_allowed = 3769;

/// Server error specific to mysql. Error number: 3770, symbol: ER_DEFAULT_VAL_GENERATED_NAMED_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_default_val_generated_named_function_is_not_allowed = 3770;

/// Server error specific to mysql. Error number: 3771, symbol: ER_DEFAULT_VAL_GENERATED_ROW_VALUE.
constexpr int er_default_val_generated_row_value = 3771;

/// Server error specific to mysql. Error number: 3772, symbol: ER_DEFAULT_VAL_GENERATED_VARIABLES.
constexpr int er_default_val_generated_variables = 3772;

/// Server error specific to mysql. Error number: 3773, symbol: ER_DEFAULT_AS_VAL_GENERATED.
constexpr int er_default_as_val_generated = 3773;

/// Server error specific to mysql. Error number: 3774, symbol: ER_UNSUPPORTED_ACTION_ON_DEFAULT_VAL_GENERATED.
constexpr int er_unsupported_action_on_default_val_generated = 3774;

/// Server error specific to mysql. Error number: 3775, symbol: ER_GTID_UNSAFE_ALTER_ADD_COL_WITH_DEFAULT_EXPRESSION.
constexpr int er_gtid_unsafe_alter_add_col_with_default_expression = 3775;

/// Server error specific to mysql. Error number: 3776, symbol: ER_FK_CANNOT_CHANGE_ENGINE.
constexpr int er_fk_cannot_change_engine = 3776;

/// Server error specific to mysql. Error number: 3777, symbol: ER_WARN_DEPRECATED_USER_SET_EXPR.
constexpr int er_warn_deprecated_user_set_expr = 3777;

/// Server error specific to mysql. Error number: 3778, symbol: ER_WARN_DEPRECATED_UTF8MB3_COLLATION.
constexpr int er_warn_deprecated_utf8mb3_collation = 3778;

/// Server error specific to mysql. Error number: 3779, symbol: ER_WARN_DEPRECATED_NESTED_COMMENT_SYNTAX.
constexpr int er_warn_deprecated_nested_comment_syntax = 3779;

/// Server error specific to mysql. Error number: 3780, symbol: ER_FK_INCOMPATIBLE_COLUMNS.
constexpr int er_fk_incompatible_columns = 3780;

/// Server error specific to mysql. Error number: 3781, symbol: ER_GR_HOLD_WAIT_TIMEOUT.
constexpr int er_gr_hold_wait_timeout = 3781;

/// Server error specific to mysql. Error number: 3782, symbol: ER_GR_HOLD_KILLED.
constexpr int er_gr_hold_killed = 3782;

/// Server error specific to mysql. Error number: 3783, symbol: ER_GR_HOLD_MEMBER_STATUS_ERROR.
constexpr int er_gr_hold_member_status_error = 3783;

/// Server error specific to mysql. Error number: 3784, symbol: ER_RPL_ENCRYPTION_FAILED_TO_FETCH_KEY.
constexpr int er_rpl_encryption_failed_to_fetch_key = 3784;

/// Server error specific to mysql. Error number: 3785, symbol: ER_RPL_ENCRYPTION_KEY_NOT_FOUND.
constexpr int er_rpl_encryption_key_not_found = 3785;

/// Server error specific to mysql. Error number: 3786, symbol: ER_RPL_ENCRYPTION_KEYRING_INVALID_KEY.
constexpr int er_rpl_encryption_keyring_invalid_key = 3786;

/// Server error specific to mysql. Error number: 3787, symbol: ER_RPL_ENCRYPTION_HEADER_ERROR.
constexpr int er_rpl_encryption_header_error = 3787;

/// Server error specific to mysql. Error number: 3788, symbol: ER_RPL_ENCRYPTION_FAILED_TO_ROTATE_LOGS.
constexpr int er_rpl_encryption_failed_to_rotate_logs = 3788;

/// Server error specific to mysql. Error number: 3789, symbol: ER_RPL_ENCRYPTION_KEY_EXISTS_UNEXPECTED.
constexpr int er_rpl_encryption_key_exists_unexpected = 3789;

/// Server error specific to mysql. Error number: 3790, symbol: ER_RPL_ENCRYPTION_FAILED_TO_GENERATE_KEY.
constexpr int er_rpl_encryption_failed_to_generate_key = 3790;

/// Server error specific to mysql. Error number: 3791, symbol: ER_RPL_ENCRYPTION_FAILED_TO_STORE_KEY.
constexpr int er_rpl_encryption_failed_to_store_key = 3791;

/// Server error specific to mysql. Error number: 3792, symbol: ER_RPL_ENCRYPTION_FAILED_TO_REMOVE_KEY.
constexpr int er_rpl_encryption_failed_to_remove_key = 3792;

/// Server error specific to mysql. Error number: 3793, symbol: ER_RPL_ENCRYPTION_UNABLE_TO_CHANGE_OPTION.
constexpr int er_rpl_encryption_unable_to_change_option = 3793;

/// Server error specific to mysql. Error number: 3794, symbol: ER_RPL_ENCRYPTION_MASTER_KEY_RECOVERY_FAILED.
constexpr int er_rpl_encryption_master_key_recovery_failed = 3794;

/// Server error specific to mysql. Error number: 3795, symbol: ER_SLOW_LOG_MODE_IGNORED_WHEN_NOT_LOGGING_TO_FILE.
constexpr int er_slow_log_mode_ignored_when_not_logging_to_file = 3795;

/// Server error specific to mysql. Error number: 3796, symbol: ER_GRP_TRX_CONSISTENCY_NOT_ALLOWED.
constexpr int er_grp_trx_consistency_not_allowed = 3796;

/// Server error specific to mysql. Error number: 3797, symbol: ER_GRP_TRX_CONSISTENCY_BEFORE.
constexpr int er_grp_trx_consistency_before = 3797;

/// Server error specific to mysql. Error number: 3798, symbol: ER_GRP_TRX_CONSISTENCY_AFTER_ON_TRX_BEGIN.
constexpr int er_grp_trx_consistency_after_on_trx_begin = 3798;

/// Server error specific to mysql. Error number: 3799, symbol: ER_GRP_TRX_CONSISTENCY_BEGIN_NOT_ALLOWED.
constexpr int er_grp_trx_consistency_begin_not_allowed = 3799;

/// Server error specific to mysql. Error number: 3800, symbol: ER_FUNCTIONAL_INDEX_ROW_VALUE_IS_NOT_ALLOWED.
constexpr int er_functional_index_row_value_is_not_allowed = 3800;

/// Server error specific to mysql. Error number: 3801, symbol: ER_RPL_ENCRYPTION_FAILED_TO_ENCRYPT.
constexpr int er_rpl_encryption_failed_to_encrypt = 3801;

/// Server error specific to mysql. Error number: 3802, symbol: ER_PAGE_TRACKING_NOT_STARTED.
constexpr int er_page_tracking_not_started = 3802;

/// Server error specific to mysql. Error number: 3803, symbol: ER_PAGE_TRACKING_RANGE_NOT_TRACKED.
constexpr int er_page_tracking_range_not_tracked = 3803;

/// Server error specific to mysql. Error number: 3804, symbol: ER_PAGE_TRACKING_CANNOT_PURGE.
constexpr int er_page_tracking_cannot_purge = 3804;

/// Server error specific to mysql. Error number: 3805, symbol: ER_RPL_ENCRYPTION_CANNOT_ROTATE_BINLOG_MASTER_KEY.
constexpr int er_rpl_encryption_cannot_rotate_binlog_master_key = 3805;

/// Server error specific to mysql. Error number: 3806, symbol: ER_BINLOG_MASTER_KEY_RECOVERY_OUT_OF_COMBINATION.
constexpr int er_binlog_master_key_recovery_out_of_combination = 3806;

/// Server error specific to mysql. Error number: 3807, symbol: ER_BINLOG_MASTER_KEY_ROTATION_FAIL_TO_OPERATE_KEY.
constexpr int er_binlog_master_key_rotation_fail_to_operate_key = 3807;

/// Server error specific to mysql. Error number: 3808, symbol: ER_BINLOG_MASTER_KEY_ROTATION_FAIL_TO_ROTATE_LOGS.
constexpr int er_binlog_master_key_rotation_fail_to_rotate_logs = 3808;

/// Server error specific to mysql. Error number: 3809, symbol: ER_BINLOG_MASTER_KEY_ROTATION_FAIL_TO_REENCRYPT_LOG.
constexpr int er_binlog_master_key_rotation_fail_to_reencrypt_log = 3809;

/// Server error specific to mysql. Error number: 3810, symbol: ER_BINLOG_MASTER_KEY_ROTATION_FAIL_TO_CLEANUP_UNUSED_KEYS.
constexpr int er_binlog_master_key_rotation_fail_to_cleanup_unused_keys = 3810;

/// Server error specific to mysql. Error number: 3811, symbol: ER_BINLOG_MASTER_KEY_ROTATION_FAIL_TO_CLEANUP_AUX_KEY.
constexpr int er_binlog_master_key_rotation_fail_to_cleanup_aux_key = 3811;

/// Server error specific to mysql. Error number: 3812, symbol: ER_NON_BOOLEAN_EXPR_FOR_CHECK_CONSTRAINT.
constexpr int er_non_boolean_expr_for_check_constraint = 3812;

/// Server error specific to mysql. Error number: 3813, symbol: ER_COLUMN_CHECK_CONSTRAINT_REFERENCES_OTHER_COLUMN.
constexpr int er_column_check_constraint_references_other_column = 3813;

/// Server error specific to mysql. Error number: 3814, symbol: ER_CHECK_CONSTRAINT_NAMED_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_check_constraint_named_function_is_not_allowed = 3814;

/// Server error specific to mysql. Error number: 3815, symbol: ER_CHECK_CONSTRAINT_FUNCTION_IS_NOT_ALLOWED.
constexpr int er_check_constraint_function_is_not_allowed = 3815;

/// Server error specific to mysql. Error number: 3816, symbol: ER_CHECK_CONSTRAINT_VARIABLES.
constexpr int er_check_constraint_variables = 3816;

/// Server error specific to mysql. Error number: 3817, symbol: ER_CHECK_CONSTRAINT_ROW_VALUE.
constexpr int er_check_constraint_row_value = 3817;

/// Server error specific to mysql. Error number: 3818, symbol: ER_CHECK_CONSTRAINT_REFERS_AUTO_INCREMENT_COLUMN.
constexpr int er_check_constraint_refers_auto_increment_column = 3818;

/// Server error specific to mysql. Error number: 3819, symbol: ER_CHECK_CONSTRAINT_VIOLATED.
constexpr int er_check_constraint_violated = 3819;

/// Server error specific to mysql. Error number: 3820, symbol: ER_CHECK_CONSTRAINT_REFERS_UNKNOWN_COLUMN.
constexpr int er_check_constraint_refers_unknown_column = 3820;

/// Server error specific to mysql. Error number: 3821, symbol: ER_CHECK_CONSTRAINT_NOT_FOUND.
constexpr int er_check_constraint_not_found = 3821;

/// Server error specific to mysql. Error number: 3822, symbol: ER_CHECK_CONSTRAINT_DUP_NAME.
constexpr int er_check_constraint_dup_name = 3822;

/// Server error specific to mysql. Error number: 3823, symbol: ER_CHECK_CONSTRAINT_CLAUSE_USING_FK_REFER_ACTION_COLUMN.
constexpr int er_check_constraint_clause_using_fk_refer_action_column = 3823;

/// Server error specific to mysql. Error number: 3824, symbol: WARN_UNENCRYPTED_TABLE_IN_ENCRYPTED_DB.
constexpr int warn_unencrypted_table_in_encrypted_db = 3824;

/// Server error specific to mysql. Error number: 3825, symbol: ER_INVALID_ENCRYPTION_REQUEST.
constexpr int er_invalid_encryption_request = 3825;

/// Server error specific to mysql. Error number: 3826, symbol: ER_CANNOT_SET_TABLE_ENCRYPTION.
constexpr int er_cannot_set_table_encryption = 3826;

/// Server error specific to mysql. Error number: 3827, symbol: ER_CANNOT_SET_DATABASE_ENCRYPTION.
constexpr int er_cannot_set_database_encryption = 3827;

/// Server error specific to mysql. Error number: 3828, symbol: ER_CANNOT_SET_TABLESPACE_ENCRYPTION.
constexpr int er_cannot_set_tablespace_encryption = 3828;

/// Server error specific to mysql. Error number: 3829, symbol: ER_TABLESPACE_CANNOT_BE_ENCRYPTED.
constexpr int er_tablespace_cannot_be_encrypted = 3829;

/// Server error specific to mysql. Error number: 3830, symbol: ER_TABLESPACE_CANNOT_BE_DECRYPTED.
constexpr int er_tablespace_cannot_be_decrypted = 3830;

/// Server error specific to mysql. Error number: 3831, symbol: ER_TABLESPACE_TYPE_UNKNOWN.
constexpr int er_tablespace_type_unknown = 3831;

/// Server error specific to mysql. Error number: 3832, symbol: ER_TARGET_TABLESPACE_UNENCRYPTED.
constexpr int er_target_tablespace_unencrypted = 3832;

/// Server error specific to mysql. Error number: 3833, symbol: ER_CANNOT_USE_ENCRYPTION_CLAUSE.
constexpr int er_cannot_use_encryption_clause = 3833;

/// Server error specific to mysql. Error number: 3834, symbol: ER_INVALID_MULTIPLE_CLAUSES.
constexpr int er_invalid_multiple_clauses = 3834;

/// Server error specific to mysql. Error number: 3835, symbol: ER_UNSUPPORTED_USE_OF_GRANT_AS.
constexpr int er_unsupported_use_of_grant_as = 3835;

/// Server error specific to mysql. Error number: 3836, symbol: ER_UKNOWN_AUTH_ID_OR_ACCESS_DENIED_FOR_GRANT_AS.
constexpr int er_uknown_auth_id_or_access_denied_for_grant_as = 3836;

/// Server error specific to mysql. Error number: 3837, symbol: ER_DEPENDENT_BY_FUNCTIONAL_INDEX.
constexpr int er_dependent_by_functional_index = 3837;

/// Server error specific to mysql. Error number: 3838, symbol: ER_PLUGIN_NOT_EARLY.
constexpr int er_plugin_not_early = 3838;

/// Server error specific to mysql. Error number: 3839, symbol: ER_INNODB_REDO_LOG_ARCHIVE_START_SUBDIR_PATH.
constexpr int er_innodb_redo_log_archive_start_subdir_path = 3839;

/// Server error specific to mysql. Error number: 3840, symbol: ER_INNODB_REDO_LOG_ARCHIVE_START_TIMEOUT.
constexpr int er_innodb_redo_log_archive_start_timeout = 3840;

/// Server error specific to mysql. Error number: 3841, symbol: ER_INNODB_REDO_LOG_ARCHIVE_DIRS_INVALID.
constexpr int er_innodb_redo_log_archive_dirs_invalid = 3841;

/// Server error specific to mysql. Error number: 3842, symbol: ER_INNODB_REDO_LOG_ARCHIVE_LABEL_NOT_FOUND.
constexpr int er_innodb_redo_log_archive_label_not_found = 3842;

/// Server error specific to mysql. Error number: 3843, symbol: ER_INNODB_REDO_LOG_ARCHIVE_DIR_EMPTY.
constexpr int er_innodb_redo_log_archive_dir_empty = 3843;

/// Server error specific to mysql. Error number: 3844, symbol: ER_INNODB_REDO_LOG_ARCHIVE_NO_SUCH_DIR.
constexpr int er_innodb_redo_log_archive_no_such_dir = 3844;

/// Server error specific to mysql. Error number: 3845, symbol: ER_INNODB_REDO_LOG_ARCHIVE_DIR_CLASH.
constexpr int er_innodb_redo_log_archive_dir_clash = 3845;

/// Server error specific to mysql. Error number: 3846, symbol: ER_INNODB_REDO_LOG_ARCHIVE_DIR_PERMISSIONS.
constexpr int er_innodb_redo_log_archive_dir_permissions = 3846;

/// Server error specific to mysql. Error number: 3847, symbol: ER_INNODB_REDO_LOG_ARCHIVE_FILE_CREATE.
constexpr int er_innodb_redo_log_archive_file_create = 3847;

/// Server error specific to mysql. Error number: 3848, symbol: ER_INNODB_REDO_LOG_ARCHIVE_ACTIVE.
constexpr int er_innodb_redo_log_archive_active = 3848;

/// Server error specific to mysql. Error number: 3849, symbol: ER_INNODB_REDO_LOG_ARCHIVE_INACTIVE.
constexpr int er_innodb_redo_log_archive_inactive = 3849;

/// Server error specific to mysql. Error number: 3850, symbol: ER_INNODB_REDO_LOG_ARCHIVE_FAILED.
constexpr int er_innodb_redo_log_archive_failed = 3850;

/// Server error specific to mysql. Error number: 3851, symbol: ER_INNODB_REDO_LOG_ARCHIVE_SESSION.
constexpr int er_innodb_redo_log_archive_session = 3851;

/// Server error specific to mysql. Error number: 3852, symbol: ER_STD_REGEX_ERROR.
constexpr int er_std_regex_error = 3852;

/// Server error specific to mysql. Error number: 3853, symbol: ER_INVALID_JSON_TYPE.
constexpr int er_invalid_json_type = 3853;

/// Server error specific to mysql. Error number: 3854, symbol: ER_CANNOT_CONVERT_STRING.
constexpr int er_cannot_convert_string = 3854;

/// Server error specific to mysql. Error number: 3855, symbol: ER_DEPENDENT_BY_PARTITION_FUNC.
constexpr int er_dependent_by_partition_func = 3855;

/// Server error specific to mysql. Error number: 3856, symbol: ER_WARN_DEPRECATED_FLOAT_AUTO_INCREMENT.
constexpr int er_warn_deprecated_float_auto_increment = 3856;

/// Server error specific to mysql. Error number: 3857, symbol: ER_RPL_CANT_STOP_SLAVE_WHILE_LOCKED_BACKUP.
constexpr int er_rpl_cant_stop_slave_while_locked_backup = 3857;

/// Server error specific to mysql. Error number: 3858, symbol: ER_WARN_DEPRECATED_FLOAT_DIGITS.
constexpr int er_warn_deprecated_float_digits = 3858;

/// Server error specific to mysql. Error number: 3859, symbol: ER_WARN_DEPRECATED_FLOAT_UNSIGNED.
constexpr int er_warn_deprecated_float_unsigned = 3859;

/// Server error specific to mysql. Error number: 3860, symbol: ER_WARN_DEPRECATED_INTEGER_DISPLAY_WIDTH.
constexpr int er_warn_deprecated_integer_display_width = 3860;

/// Server error specific to mysql. Error number: 3861, symbol: ER_WARN_DEPRECATED_ZEROFILL.
constexpr int er_warn_deprecated_zerofill = 3861;

/// Server error specific to mysql. Error number: 3862, symbol: ER_CLONE_DONOR.
constexpr int er_clone_donor = 3862;

/// Server error specific to mysql. Error number: 3863, symbol: ER_CLONE_PROTOCOL.
constexpr int er_clone_protocol = 3863;

/// Server error specific to mysql. Error number: 3864, symbol: ER_CLONE_DONOR_VERSION.
constexpr int er_clone_donor_version = 3864;

/// Server error specific to mysql. Error number: 3865, symbol: ER_CLONE_OS.
constexpr int er_clone_os = 3865;

/// Server error specific to mysql. Error number: 3866, symbol: ER_CLONE_PLATFORM.
constexpr int er_clone_platform = 3866;

/// Server error specific to mysql. Error number: 3867, symbol: ER_CLONE_CHARSET.
constexpr int er_clone_charset = 3867;

/// Server error specific to mysql. Error number: 3868, symbol: ER_CLONE_CONFIG.
constexpr int er_clone_config = 3868;

/// Server error specific to mysql. Error number: 3869, symbol: ER_CLONE_SYS_CONFIG.
constexpr int er_clone_sys_config = 3869;

/// Server error specific to mysql. Error number: 3870, symbol: ER_CLONE_PLUGIN_MATCH.
constexpr int er_clone_plugin_match = 3870;

/// Server error specific to mysql. Error number: 3871, symbol: ER_CLONE_LOOPBACK.
constexpr int er_clone_loopback = 3871;

/// Server error specific to mysql. Error number: 3872, symbol: ER_CLONE_ENCRYPTION.
constexpr int er_clone_encryption = 3872;

/// Server error specific to mysql. Error number: 3873, symbol: ER_CLONE_DISK_SPACE.
constexpr int er_clone_disk_space = 3873;

/// Server error specific to mysql. Error number: 3874, symbol: ER_CLONE_IN_PROGRESS.
constexpr int er_clone_in_progress = 3874;

/// Server error specific to mysql. Error number: 3875, symbol: ER_CLONE_DISALLOWED.
constexpr int er_clone_disallowed = 3875;

/// Server error specific to mysql. Error number: 3876, symbol: ER_CANNOT_GRANT_ROLES_TO_ANONYMOUS_USER.
constexpr int er_cannot_grant_roles_to_anonymous_user = 3876;

/// Server error specific to mysql. Error number: 3877, symbol: ER_SECONDARY_ENGINE_PLUGIN.
constexpr int er_secondary_engine_plugin = 3877;

/// Server error specific to mysql. Error number: 3878, symbol: ER_SECOND_PASSWORD_CANNOT_BE_EMPTY.
constexpr int er_second_password_cannot_be_empty = 3878;

/// Server error specific to mysql. Error number: 3879, symbol: ER_DB_ACCESS_DENIED.
constexpr int er_db_access_denied = 3879;

/// Server error specific to mysql. Error number: 3880, symbol: ER_DA_AUTH_ID_WITH_SYSTEM_USER_PRIV_IN_MANDATORY_ROLES.
constexpr int er_da_auth_id_with_system_user_priv_in_mandatory_roles = 3880;

/// Server error specific to mysql. Error number: 3881, symbol: ER_DA_RPL_GTID_TABLE_CANNOT_OPEN.
constexpr int er_da_rpl_gtid_table_cannot_open = 3881;

/// Server error specific to mysql. Error number: 3882, symbol: ER_GEOMETRY_IN_UNKNOWN_LENGTH_UNIT.
constexpr int er_geometry_in_unknown_length_unit = 3882;

/// Server error specific to mysql. Error number: 3883, symbol: ER_DA_PLUGIN_INSTALL_ERROR.
constexpr int er_da_plugin_install_error = 3883;

/// Server error specific to mysql. Error number: 3884, symbol: ER_NO_SESSION_TEMP.
constexpr int er_no_session_temp = 3884;

/// Server error specific to mysql. Error number: 3885, symbol: ER_DA_UNKNOWN_ERROR_NUMBER.
constexpr int er_da_unknown_error_number = 3885;

/// Server error specific to mysql. Error number: 3886, symbol: ER_COLUMN_CHANGE_SIZE.
constexpr int er_column_change_size = 3886;

/// Server error specific to mysql. Error number: 3887, symbol: ER_REGEXP_INVALID_CAPTURE_GROUP_NAME.
constexpr int er_regexp_invalid_capture_group_name = 3887;

/// Server error specific to mysql. Error number: 3888, symbol: ER_DA_SSL_LIBRARY_ERROR.
constexpr int er_da_ssl_library_error = 3888;

/// Server error specific to mysql. Error number: 3889, symbol: ER_SECONDARY_ENGINE.
constexpr int er_secondary_engine = 3889;

/// Server error specific to mysql. Error number: 3890, symbol: ER_SECONDARY_ENGINE_DDL.
constexpr int er_secondary_engine_ddl = 3890;

/// Server error specific to mysql. Error number: 3891, symbol: ER_INCORRECT_CURRENT_PASSWORD.
constexpr int er_incorrect_current_password = 3891;

/// Server error specific to mysql. Error number: 3892, symbol: ER_MISSING_CURRENT_PASSWORD.
constexpr int er_missing_current_password = 3892;

/// Server error specific to mysql. Error number: 3893, symbol: ER_CURRENT_PASSWORD_NOT_REQUIRED.
constexpr int er_current_password_not_required = 3893;

/// Server error specific to mysql. Error number: 3894, symbol: ER_PASSWORD_CANNOT_BE_RETAINED_ON_PLUGIN_CHANGE.
constexpr int er_password_cannot_be_retained_on_plugin_change = 3894;

/// Server error specific to mysql. Error number: 3895, symbol: ER_CURRENT_PASSWORD_CANNOT_BE_RETAINED.
constexpr int er_current_password_cannot_be_retained = 3895;

/// Server error specific to mysql. Error number: 3896, symbol: ER_PARTIAL_REVOKES_EXIST.
constexpr int er_partial_revokes_exist = 3896;

/// Server error specific to mysql. Error number: 3897, symbol: ER_CANNOT_GRANT_SYSTEM_PRIV_TO_MANDATORY_ROLE.
constexpr int er_cannot_grant_system_priv_to_mandatory_role = 3897;

/// Server error specific to mysql. Error number: 3898, symbol: ER_XA_REPLICATION_FILTERS.
constexpr int er_xa_replication_filters = 3898;

/// Server error specific to mysql. Error number: 3899, symbol: ER_UNSUPPORTED_SQL_MODE.
constexpr int er_unsupported_sql_mode = 3899;

/// Server error specific to mysql. Error number: 3900, symbol: ER_REGEXP_INVALID_FLAG.
constexpr int er_regexp_invalid_flag = 3900;

/// Server error specific to mysql. Error number: 3901, symbol: ER_PARTIAL_REVOKE_AND_DB_GRANT_BOTH_EXISTS.
constexpr int er_partial_revoke_and_db_grant_both_exists = 3901;

/// Server error specific to mysql. Error number: 3902, symbol: ER_UNIT_NOT_FOUND.
constexpr int er_unit_not_found = 3902;

/// Server error specific to mysql. Error number: 3903, symbol: ER_INVALID_JSON_VALUE_FOR_FUNC_INDEX.
constexpr int er_invalid_json_value_for_func_index = 3903;

/// Server error specific to mysql. Error number: 3904, symbol: ER_JSON_VALUE_OUT_OF_RANGE_FOR_FUNC_INDEX.
constexpr int er_json_value_out_of_range_for_func_index = 3904;

/// Server error specific to mysql. Error number: 3905, symbol: ER_EXCEEDED_MV_KEYS_NUM.
constexpr int er_exceeded_mv_keys_num = 3905;

/// Server error specific to mysql. Error number: 3906, symbol: ER_EXCEEDED_MV_KEYS_SPACE.
constexpr int er_exceeded_mv_keys_space = 3906;

/// Server error specific to mysql. Error number: 3907, symbol: ER_FUNCTIONAL_INDEX_DATA_IS_TOO_LONG.
constexpr int er_functional_index_data_is_too_long = 3907;

/// Server error specific to mysql. Error number: 3908, symbol: ER_WRONG_MVI_VALUE.
constexpr int er_wrong_mvi_value = 3908;

/// Server error specific to mysql. Error number: 3909, symbol: ER_WARN_FUNC_INDEX_NOT_APPLICABLE.
constexpr int er_warn_func_index_not_applicable = 3909;

/// Server error specific to mysql. Error number: 3910, symbol: ER_GRP_RPL_UDF_ERROR.
constexpr int er_grp_rpl_udf_error = 3910;

/// Server error specific to mysql. Error number: 3911, symbol: ER_UPDATE_GTID_PURGED_WITH_GR.
constexpr int er_update_gtid_purged_with_gr = 3911;

/// Server error specific to mysql. Error number: 3912, symbol: ER_GROUPING_ON_TIMESTAMP_IN_DST.
constexpr int er_grouping_on_timestamp_in_dst = 3912;

/// Server error specific to mysql. Error number: 3913, symbol: ER_TABLE_NAME_CAUSES_TOO_LONG_PATH.
constexpr int er_table_name_causes_too_long_path = 3913;

/// Server error specific to mysql. Error number: 3914, symbol: ER_AUDIT_LOG_INSUFFICIENT_PRIVILEGE.
constexpr int er_audit_log_insufficient_privilege = 3914;

/// Server error specific to mysql. Error number: 3916, symbol: ER_DA_GRP_RPL_STARTED_AUTO_REJOIN.
constexpr int er_da_grp_rpl_started_auto_rejoin = 3916;

/// Server error specific to mysql. Error number: 3917, symbol: ER_SYSVAR_CHANGE_DURING_QUERY.
constexpr int er_sysvar_change_during_query = 3917;

/// Server error specific to mysql. Error number: 3918, symbol: ER_GLOBSTAT_CHANGE_DURING_QUERY.
constexpr int er_globstat_change_during_query = 3918;

/// Server error specific to mysql. Error number: 3919, symbol: ER_GRP_RPL_MESSAGE_SERVICE_INIT_FAILURE.
constexpr int er_grp_rpl_message_service_init_failure = 3919;

/// Server error specific to mysql. Error number: 3920, symbol: ER_CHANGE_MASTER_WRONG_COMPRESSION_ALGORITHM_CLIENT.
constexpr int er_change_master_wrong_compression_algorithm_client = 3920;

/// Server error specific to mysql. Error number: 3921, symbol: ER_CHANGE_MASTER_WRONG_COMPRESSION_LEVEL_CLIENT.
constexpr int er_change_master_wrong_compression_level_client = 3921;

/// Server error specific to mysql. Error number: 3922, symbol: ER_WRONG_COMPRESSION_ALGORITHM_CLIENT.
constexpr int er_wrong_compression_algorithm_client = 3922;

/// Server error specific to mysql. Error number: 3923, symbol: ER_WRONG_COMPRESSION_LEVEL_CLIENT.
constexpr int er_wrong_compression_level_client = 3923;

/// Server error specific to mysql. Error number: 3924, symbol: ER_CHANGE_MASTER_WRONG_COMPRESSION_ALGORITHM_LIST_CLIENT.
constexpr int er_change_master_wrong_compression_algorithm_list_client = 3924;

/// Server error specific to mysql. Error number: 3925, symbol: ER_CLIENT_PRIVILEGE_CHECKS_USER_CANNOT_BE_ANONYMOUS.
constexpr int er_client_privilege_checks_user_cannot_be_anonymous = 3925;

/// Server error specific to mysql. Error number: 3926, symbol: ER_CLIENT_PRIVILEGE_CHECKS_USER_DOES_NOT_EXIST.
constexpr int er_client_privilege_checks_user_does_not_exist = 3926;

/// Server error specific to mysql. Error number: 3927, symbol: ER_CLIENT_PRIVILEGE_CHECKS_USER_CORRUPT.
constexpr int er_client_privilege_checks_user_corrupt = 3927;

/// Server error specific to mysql. Error number: 3928, symbol: ER_CLIENT_PRIVILEGE_CHECKS_USER_NEEDS_RPL_APPLIER_PRIV.
constexpr int er_client_privilege_checks_user_needs_rpl_applier_priv = 3928;

/// Server error specific to mysql. Error number: 3929, symbol: ER_WARN_DA_PRIVILEGE_NOT_REGISTERED.
constexpr int er_warn_da_privilege_not_registered = 3929;

/// Server error specific to mysql. Error number: 3930, symbol: ER_CLIENT_KEYRING_UDF_KEY_INVALID.
constexpr int er_client_keyring_udf_key_invalid = 3930;

/// Server error specific to mysql. Error number: 3931, symbol: ER_CLIENT_KEYRING_UDF_KEY_TYPE_INVALID.
constexpr int er_client_keyring_udf_key_type_invalid = 3931;

/// Server error specific to mysql. Error number: 3932, symbol: ER_CLIENT_KEYRING_UDF_KEY_TOO_LONG.
constexpr int er_client_keyring_udf_key_too_long = 3932;

/// Server error specific to mysql. Error number: 3933, symbol: ER_CLIENT_KEYRING_UDF_KEY_TYPE_TOO_LONG.
constexpr int er_client_keyring_udf_key_type_too_long = 3933;

/// Server error specific to mysql. Error number: 3934, symbol: ER_JSON_SCHEMA_VALIDATION_ERROR_WITH_DETAILED_REPORT.
constexpr int er_json_schema_validation_error_with_detailed_report = 3934;

/// Server error specific to mysql. Error number: 3935, symbol: ER_DA_UDF_INVALID_CHARSET_SPECIFIED.
constexpr int er_da_udf_invalid_charset_specified = 3935;

/// Server error specific to mysql. Error number: 3936, symbol: ER_DA_UDF_INVALID_CHARSET.
constexpr int er_da_udf_invalid_charset = 3936;

/// Server error specific to mysql. Error number: 3937, symbol: ER_DA_UDF_INVALID_COLLATION.
constexpr int er_da_udf_invalid_collation = 3937;

/// Server error specific to mysql. Error number: 3938, symbol: ER_DA_UDF_INVALID_EXTENSION_ARGUMENT_TYPE.
constexpr int er_da_udf_invalid_extension_argument_type = 3938;

/// Server error specific to mysql. Error number: 3939, symbol: ER_MULTIPLE_CONSTRAINTS_WITH_SAME_NAME.
constexpr int er_multiple_constraints_with_same_name = 3939;

/// Server error specific to mysql. Error number: 3940, symbol: ER_CONSTRAINT_NOT_FOUND.
constexpr int er_constraint_not_found = 3940;

/// Server error specific to mysql. Error number: 3941, symbol: ER_ALTER_CONSTRAINT_ENFORCEMENT_NOT_SUPPORTED.
constexpr int er_alter_constraint_enforcement_not_supported = 3941;

/// Server error specific to mysql. Error number: 3942, symbol: ER_TABLE_VALUE_CONSTRUCTOR_MUST_HAVE_COLUMNS.
constexpr int er_table_value_constructor_must_have_columns = 3942;

/// Server error specific to mysql. Error number: 3943, symbol: ER_TABLE_VALUE_CONSTRUCTOR_CANNOT_HAVE_DEFAULT.
constexpr int er_table_value_constructor_cannot_have_default = 3943;

/// Server error specific to mysql. Error number: 3944, symbol: ER_CLIENT_QUERY_FAILURE_INVALID_NON_ROW_FORMAT.
constexpr int er_client_query_failure_invalid_non_row_format = 3944;

/// Server error specific to mysql. Error number: 3945, symbol: ER_REQUIRE_ROW_FORMAT_INVALID_VALUE.
constexpr int er_require_row_format_invalid_value = 3945;

/// Server error specific to mysql. Error number: 3946, symbol: ER_FAILED_TO_DETERMINE_IF_ROLE_IS_MANDATORY.
constexpr int er_failed_to_determine_if_role_is_mandatory = 3946;

/// Server error specific to mysql. Error number: 3947, symbol: ER_FAILED_TO_FETCH_MANDATORY_ROLE_LIST.
constexpr int er_failed_to_fetch_mandatory_role_list = 3947;

/// Server error specific to mysql. Error number: 3948, symbol: ER_CLIENT_LOCAL_FILES_DISABLED.
constexpr int er_client_local_files_disabled = 3948;

/// Server error specific to mysql. Error number: 3949, symbol: ER_IMP_INCOMPATIBLE_CFG_VERSION.
constexpr int er_imp_incompatible_cfg_version = 3949;

/// Server error specific to mysql. Error number: 3950, symbol: ER_DA_OOM.
constexpr int er_da_oom = 3950;

/// Server error specific to mysql. Error number: 3951, symbol: ER_DA_UDF_INVALID_ARGUMENT_TO_SET_CHARSET.
constexpr int er_da_udf_invalid_argument_to_set_charset = 3951;

/// Server error specific to mysql. Error number: 3952, symbol: ER_DA_UDF_INVALID_RETURN_TYPE_TO_SET_CHARSET.
constexpr int er_da_udf_invalid_return_type_to_set_charset = 3952;

/// Server error specific to mysql. Error number: 3953, symbol: ER_MULTIPLE_INTO_CLAUSES.
constexpr int er_multiple_into_clauses = 3953;

/// Server error specific to mysql. Error number: 3954, symbol: ER_MISPLACED_INTO.
constexpr int er_misplaced_into = 3954;

/// Server error specific to mysql. Error number: 3955, symbol: ER_USER_ACCESS_DENIED_FOR_USER_ACCOUNT_BLOCKED_BY_PASSWORD_LOCK.
constexpr int er_user_access_denied_for_user_account_blocked_by_password_lock = 3955;

/// Server error specific to mysql. Error number: 3956, symbol: ER_WARN_DEPRECATED_YEAR_UNSIGNED.
constexpr int er_warn_deprecated_year_unsigned = 3956;

/// Server error specific to mysql. Error number: 3957, symbol: ER_CLONE_NETWORK_PACKET.
constexpr int er_clone_network_packet = 3957;

/// Server error specific to mysql. Error number: 3958, symbol: ER_SDI_OPERATION_FAILED_MISSING_RECORD.
constexpr int er_sdi_operation_failed_missing_record = 3958;

/// Server error specific to mysql. Error number: 3959, symbol: ER_DEPENDENT_BY_CHECK_CONSTRAINT.
constexpr int er_dependent_by_check_constraint = 3959;

/// Server error specific to mysql. Error number: 3960, symbol: ER_GRP_OPERATION_NOT_ALLOWED_GR_MUST_STOP.
constexpr int er_grp_operation_not_allowed_gr_must_stop = 3960;

/// Server error specific to mysql. Error number: 3961, symbol: ER_WARN_DEPRECATED_JSON_TABLE_ON_ERROR_ON_EMPTY.
constexpr int er_warn_deprecated_json_table_on_error_on_empty = 3961;

/// Server error specific to mysql. Error number: 3962, symbol: ER_WARN_DEPRECATED_INNER_INTO.
constexpr int er_warn_deprecated_inner_into = 3962;

/// Server error specific to mysql. Error number: 3963, symbol: ER_WARN_DEPRECATED_VALUES_FUNCTION_ALWAYS_NULL.
constexpr int er_warn_deprecated_values_function_always_null = 3963;

/// Server error specific to mysql. Error number: 3964, symbol: ER_WARN_DEPRECATED_SQL_CALC_FOUND_ROWS.
constexpr int er_warn_deprecated_sql_calc_found_rows = 3964;

/// Server error specific to mysql. Error number: 3965, symbol: ER_WARN_DEPRECATED_FOUND_ROWS.
constexpr int er_warn_deprecated_found_rows = 3965;

/// Server error specific to mysql. Error number: 3966, symbol: ER_MISSING_JSON_VALUE.
constexpr int er_missing_json_value = 3966;

/// Server error specific to mysql. Error number: 3967, symbol: ER_MULTIPLE_JSON_VALUES.
constexpr int er_multiple_json_values = 3967;

/// Server error specific to mysql. Error number: 3968, symbol: ER_HOSTNAME_TOO_LONG.
constexpr int er_hostname_too_long = 3968;

/// Server error specific to mysql. Error number: 3969, symbol: ER_WARN_CLIENT_DEPRECATED_PARTITION_PREFIX_KEY.
constexpr int er_warn_client_deprecated_partition_prefix_key = 3969;

/// Server error specific to mysql. Error number: 3970, symbol: ER_GROUP_REPLICATION_USER_EMPTY_MSG.
constexpr int er_group_replication_user_empty_msg = 3970;

/// Server error specific to mysql. Error number: 3971, symbol: ER_GROUP_REPLICATION_USER_MANDATORY_MSG.
constexpr int er_group_replication_user_mandatory_msg = 3971;

/// Server error specific to mysql. Error number: 3972, symbol: ER_GROUP_REPLICATION_PASSWORD_LENGTH.
constexpr int er_group_replication_password_length = 3972;

/// Server error specific to mysql. Error number: 3973, symbol: ER_SUBQUERY_TRANSFORM_REJECTED.
constexpr int er_subquery_transform_rejected = 3973;

/// Server error specific to mysql. Error number: 3974, symbol: ER_DA_GRP_RPL_RECOVERY_ENDPOINT_FORMAT.
constexpr int er_da_grp_rpl_recovery_endpoint_format = 3974;

/// Server error specific to mysql. Error number: 3975, symbol: ER_DA_GRP_RPL_RECOVERY_ENDPOINT_INVALID.
constexpr int er_da_grp_rpl_recovery_endpoint_invalid = 3975;

/// Server error specific to mysql. Error number: 3976, symbol: ER_WRONG_VALUE_FOR_VAR_PLUS_ACTIONABLE_PART.
constexpr int er_wrong_value_for_var_plus_actionable_part = 3976;

/// Server error specific to mysql. Error number: 3977, symbol: ER_STATEMENT_NOT_ALLOWED_AFTER_START_TRANSACTION.
constexpr int er_statement_not_allowed_after_start_transaction = 3977;

/// Server error specific to mysql. Error number: 3978, symbol: ER_FOREIGN_KEY_WITH_ATOMIC_CREATE_SELECT.
constexpr int er_foreign_key_with_atomic_create_select = 3978;

/// Server error specific to mysql. Error number: 3979, symbol: ER_NOT_ALLOWED_WITH_START_TRANSACTION.
constexpr int er_not_allowed_with_start_transaction = 3979;

/// Server error specific to mysql. Error number: 3980, symbol: ER_INVALID_JSON_ATTRIBUTE.
constexpr int er_invalid_json_attribute = 3980;

/// Server error specific to mysql. Error number: 3981, symbol: ER_ENGINE_ATTRIBUTE_NOT_SUPPORTED.
constexpr int er_engine_attribute_not_supported = 3981;

/// Server error specific to mysql. Error number: 3982, symbol: ER_INVALID_USER_ATTRIBUTE_JSON.
constexpr int er_invalid_user_attribute_json = 3982;

/// Server error specific to mysql. Error number: 3983, symbol: ER_INNODB_REDO_DISABLED.
constexpr int er_innodb_redo_disabled = 3983;

/// Server error specific to mysql. Error number: 3984, symbol: ER_INNODB_REDO_ARCHIVING_ENABLED.
constexpr int er_innodb_redo_archiving_enabled = 3984;

/// Server error specific to mysql. Error number: 3985, symbol: ER_MDL_OUT_OF_RESOURCES.
constexpr int er_mdl_out_of_resources = 3985;

/// Server error specific to mysql. Error number: 3986, symbol: ER_IMPLICIT_COMPARISON_FOR_JSON.
constexpr int er_implicit_comparison_for_json = 3986;

/// Server error specific to mysql. Error number: 3987, symbol: ER_FUNCTION_DOES_NOT_SUPPORT_CHARACTER_SET.
constexpr int er_function_does_not_support_character_set = 3987;

/// Server error specific to mysql. Error number: 3988, symbol: ER_IMPOSSIBLE_STRING_CONVERSION.
constexpr int er_impossible_string_conversion = 3988;

/// Server error specific to mysql. Error number: 3989, symbol: ER_SCHEMA_READ_ONLY.
constexpr int er_schema_read_only = 3989;

/// Server error specific to mysql. Error number: 3990, symbol: ER_RPL_ASYNC_RECONNECT_GTID_MODE_OFF.
constexpr int er_rpl_async_reconnect_gtid_mode_off = 3990;

/// Server error specific to mysql. Error number: 3991, symbol: ER_RPL_ASYNC_RECONNECT_AUTO_POSITION_OFF.
constexpr int er_rpl_async_reconnect_auto_position_off = 3991;

/// Server error specific to mysql. Error number: 3992, symbol: ER_DISABLE_GTID_MODE_REQUIRES_ASYNC_RECONNECT_OFF.
constexpr int er_disable_gtid_mode_requires_async_reconnect_off = 3992;

/// Server error specific to mysql. Error number: 3993, symbol: ER_DISABLE_AUTO_POSITION_REQUIRES_ASYNC_RECONNECT_OFF.
constexpr int er_disable_auto_position_requires_async_reconnect_off = 3993;

/// Server error specific to mysql. Error number: 3994, symbol: ER_INVALID_PARAMETER_USE.
constexpr int er_invalid_parameter_use = 3994;

/// Server error specific to mysql. Error number: 3995, symbol: ER_CHARACTER_SET_MISMATCH.
constexpr int er_character_set_mismatch = 3995;

/// Server error specific to mysql. Error number: 3996, symbol: ER_WARN_VAR_VALUE_CHANGE_NOT_SUPPORTED.
constexpr int er_warn_var_value_change_not_supported = 3996;

/// Server error specific to mysql. Error number: 3997, symbol: ER_INVALID_TIME_ZONE_INTERVAL.
constexpr int er_invalid_time_zone_interval = 3997;

/// Server error specific to mysql. Error number: 3998, symbol: ER_INVALID_CAST.
constexpr int er_invalid_cast = 3998;

/// Server error specific to mysql. Error number: 3999, symbol: ER_HYPERGRAPH_NOT_SUPPORTED_YET.
constexpr int er_hypergraph_not_supported_yet = 3999;

/// Server error specific to mysql. Error number: 4000, symbol: ER_WARN_HYPERGRAPH_EXPERIMENTAL.
constexpr int er_warn_hypergraph_experimental = 4000;

/// Server error specific to mysql. Error number: 4001, symbol: ER_DA_NO_ERROR_LOG_PARSER_CONFIGURED.
constexpr int er_da_no_error_log_parser_configured = 4001;

/// Server error specific to mysql. Error number: 4002, symbol: ER_DA_ERROR_LOG_TABLE_DISABLED.
constexpr int er_da_error_log_table_disabled = 4002;

/// Server error specific to mysql. Error number: 4003, symbol: ER_DA_ERROR_LOG_MULTIPLE_FILTERS.
constexpr int er_da_error_log_multiple_filters = 4003;

/// Server error specific to mysql. Error number: 4004, symbol: ER_DA_CANT_OPEN_ERROR_LOG.
constexpr int er_da_cant_open_error_log = 4004;

/// Server error specific to mysql. Error number: 4005, symbol: ER_USER_REFERENCED_AS_DEFINER.
constexpr int er_user_referenced_as_definer = 4005;

/// Server error specific to mysql. Error number: 4006, symbol: ER_CANNOT_USER_REFERENCED_AS_DEFINER.
constexpr int er_cannot_user_referenced_as_definer = 4006;

/// Server error specific to mysql. Error number: 4007, symbol: ER_REGEX_NUMBER_TOO_BIG.
constexpr int er_regex_number_too_big = 4007;

/// Server error specific to mysql. Error number: 4008, symbol: ER_SPVAR_NONINTEGER_TYPE.
constexpr int er_spvar_noninteger_type = 4008;

/// Server error specific to mysql. Error number: 4009, symbol: WARN_UNSUPPORTED_ACL_TABLES_READ.
constexpr int warn_unsupported_acl_tables_read = 4009;

/// Server error specific to mysql. Error number: 4010, symbol: ER_BINLOG_UNSAFE_ACL_TABLE_READ_IN_DML_DDL.
constexpr int er_binlog_unsafe_acl_table_read_in_dml_ddl = 4010;

/// Server error specific to mysql. Error number: 4011, symbol: ER_STOP_REPLICA_MONITOR_IO_THREAD_TIMEOUT.
constexpr int er_stop_replica_monitor_io_thread_timeout = 4011;

/// Server error specific to mysql. Error number: 4012, symbol: ER_STARTING_REPLICA_MONITOR_IO_THREAD.
constexpr int er_starting_replica_monitor_io_thread = 4012;

/// Server error specific to mysql. Error number: 4013, symbol: ER_CANT_USE_ANONYMOUS_TO_GTID_WITH_GTID_MODE_NOT_ON.
constexpr int er_cant_use_anonymous_to_gtid_with_gtid_mode_not_on = 4013;

/// Server error specific to mysql. Error number: 4014, symbol: ER_CANT_COMBINE_ANONYMOUS_TO_GTID_AND_AUTOPOSITION.
constexpr int er_cant_combine_anonymous_to_gtid_and_autoposition = 4014;

/// Server error specific to mysql. Error number: 4015, symbol: ER_ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS_REQUIRES_GTID_MODE_ON.
constexpr int er_assign_gtids_to_anonymous_transactions_requires_gtid_mode_on = 4015;

/// Server error specific to mysql. Error number: 4016, symbol: ER_SQL_SLAVE_SKIP_COUNTER_USED_WITH_GTID_MODE_ON.
constexpr int er_sql_slave_skip_counter_used_with_gtid_mode_on = 4016;

/// Server error specific to mysql. Error number: 4017, symbol: ER_USING_ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS_AS_LOCAL_OR_UUID.
constexpr int er_using_assign_gtids_to_anonymous_transactions_as_local_or_uuid = 4017;

/// Server error specific to mysql. Error number: 4018, symbol: ER_CANT_SET_ANONYMOUS_TO_GTID_AND_WAIT_UNTIL_SQL_THD_AFTER_GTIDS.
constexpr int er_cant_set_anonymous_to_gtid_and_wait_until_sql_thd_after_gtids = 4018;

/// Server error specific to mysql. Error number: 4019, symbol: ER_CANT_SET_SQL_AFTER_OR_BEFORE_GTIDS_WITH_ANONYMOUS_TO_GTID.
constexpr int er_cant_set_sql_after_or_before_gtids_with_anonymous_to_gtid = 4019;

/// Server error specific to mysql. Error number: 4020, symbol: ER_ANONYMOUS_TO_GTID_UUID_SAME_AS_GROUP_NAME.
constexpr int er_anonymous_to_gtid_uuid_same_as_group_name = 4020;

/// Server error specific to mysql. Error number: 4021, symbol: ER_CANT_USE_SAME_UUID_AS_GROUP_NAME.
constexpr int er_cant_use_same_uuid_as_group_name = 4021;

/// Server error specific to mysql. Error number: 4022, symbol: ER_GRP_RPL_RECOVERY_CHANNEL_STILL_RUNNING.
constexpr int er_grp_rpl_recovery_channel_still_running = 4022;

/// Server error specific to mysql. Error number: 4023, symbol: ER_INNODB_INVALID_AUTOEXTEND_SIZE_VALUE.
constexpr int er_innodb_invalid_autoextend_size_value = 4023;

/// Server error specific to mysql. Error number: 4024, symbol: ER_INNODB_INCOMPATIBLE_WITH_TABLESPACE.
constexpr int er_innodb_incompatible_with_tablespace = 4024;

/// Server error specific to mysql. Error number: 4025, symbol: ER_INNODB_AUTOEXTEND_SIZE_OUT_OF_RANGE.
constexpr int er_innodb_autoextend_size_out_of_range = 4025;

/// Server error specific to mysql. Error number: 4026, symbol: ER_CANNOT_USE_AUTOEXTEND_SIZE_CLAUSE.
constexpr int er_cannot_use_autoextend_size_clause = 4026;

/// Server error specific to mysql. Error number: 4027, symbol: ER_ROLE_GRANTED_TO_ITSELF.
constexpr int er_role_granted_to_itself = 4027;

/// Server error specific to mysql. Error number: 4028, symbol: ER_TABLE_MUST_HAVE_A_VISIBLE_COLUMN.
constexpr int er_table_must_have_a_visible_column = 4028;

/// Server error specific to mysql. Error number: 4029, symbol: ER_INNODB_COMPRESSION_FAILURE.
constexpr int er_innodb_compression_failure = 4029;

/// Server error specific to mysql. Error number: 4030, symbol: ER_WARN_ASYNC_CONN_FAILOVER_NETWORK_NAMESPACE.
constexpr int er_warn_async_conn_failover_network_namespace = 4030;

/// Server error specific to mysql. Error number: 4031, symbol: ER_CLIENT_INTERACTION_TIMEOUT.
constexpr int er_client_interaction_timeout = 4031;

/// Server error specific to mysql. Error number: 4032, symbol: ER_INVALID_CAST_TO_GEOMETRY.
constexpr int er_invalid_cast_to_geometry = 4032;

/// Server error specific to mysql. Error number: 4033, symbol: ER_INVALID_CAST_POLYGON_RING_DIRECTION.
constexpr int er_invalid_cast_polygon_ring_direction = 4033;

/// Server error specific to mysql. Error number: 4034, symbol: ER_GIS_DIFFERENT_SRIDS_AGGREGATION.
constexpr int er_gis_different_srids_aggregation = 4034;

/// Server error specific to mysql. Error number: 4035, symbol: ER_RELOAD_KEYRING_FAILURE.
constexpr int er_reload_keyring_failure = 4035;

/// Server error specific to mysql. Error number: 4036, symbol: ER_SDI_GET_KEYS_INVALID_TABLESPACE.
constexpr int er_sdi_get_keys_invalid_tablespace = 4036;

/// Server error specific to mysql. Error number: 4037, symbol: ER_CHANGE_RPL_SRC_WRONG_COMPRESSION_ALGORITHM_SIZE.
constexpr int er_change_rpl_src_wrong_compression_algorithm_size = 4037;

/// Server error specific to mysql. Error number: 4039, symbol: ER_CANT_USE_SAME_UUID_AS_VIEW_CHANGE_UUID.
constexpr int er_cant_use_same_uuid_as_view_change_uuid = 4039;

/// Server error specific to mysql. Error number: 4040, symbol: ER_ANONYMOUS_TO_GTID_UUID_SAME_AS_VIEW_CHANGE_UUID.
constexpr int er_anonymous_to_gtid_uuid_same_as_view_change_uuid = 4040;

/// Server error specific to mysql. Error number: 4041, symbol: ER_GRP_RPL_VIEW_CHANGE_UUID_FAIL_GET_VARIABLE.
constexpr int er_grp_rpl_view_change_uuid_fail_get_variable = 4041;

/// Server error specific to mysql. Error number: 4042, symbol: ER_WARN_ADUIT_LOG_MAX_SIZE_AND_PRUNE_SECONDS.
constexpr int er_warn_aduit_log_max_size_and_prune_seconds = 4042;

/// Server error specific to mysql. Error number: 4043, symbol: ER_WARN_ADUIT_LOG_MAX_SIZE_CLOSE_TO_ROTATE_ON_SIZE.
constexpr int er_warn_aduit_log_max_size_close_to_rotate_on_size = 4043;

/// Server error specific to mysql. Error number: 4044, symbol: ER_KERBEROS_CREATE_USER.
constexpr int er_kerberos_create_user = 4044;

/// Server error specific to mysql. Error number: 4045, symbol: ER_INSTALL_PLUGIN_CONFLICT_CLIENT.
constexpr int er_install_plugin_conflict_client = 4045;

/// Server error specific to mysql. Error number: 4046, symbol: ER_DA_ERROR_LOG_COMPONENT_FLUSH_FAILED.
constexpr int er_da_error_log_component_flush_failed = 4046;

/// Server error specific to mysql. Error number: 4047, symbol: ER_WARN_SQL_AFTER_MTS_GAPS_GAP_NOT_CALCULATED.
constexpr int er_warn_sql_after_mts_gaps_gap_not_calculated = 4047;

/// Server error specific to mysql. Error number: 4048, symbol: ER_INVALID_ASSIGNMENT_TARGET.
constexpr int er_invalid_assignment_target = 4048;

/// Server error specific to mysql. Error number: 4049, symbol: ER_OPERATION_NOT_ALLOWED_ON_GR_SECONDARY.
constexpr int er_operation_not_allowed_on_gr_secondary = 4049;

/// Server error specific to mysql. Error number: 4050, symbol: ER_GRP_RPL_FAILOVER_CHANNEL_STATUS_PROPAGATION.
constexpr int er_grp_rpl_failover_channel_status_propagation = 4050;

/// Server error specific to mysql. Error number: 4051, symbol: ER_WARN_AUDIT_LOG_FORMAT_UNIX_TIMESTAMP_ONLY_WHEN_JSON.
constexpr int er_warn_audit_log_format_unix_timestamp_only_when_json = 4051;

/// Server error specific to mysql. Error number: 4052, symbol: ER_INVALID_MFA_PLUGIN_SPECIFIED.
constexpr int er_invalid_mfa_plugin_specified = 4052;

/// Server error specific to mysql. Error number: 4053, symbol: ER_IDENTIFIED_BY_UNSUPPORTED.
constexpr int er_identified_by_unsupported = 4053;

/// Server error specific to mysql. Error number: 4054, symbol: ER_INVALID_PLUGIN_FOR_REGISTRATION.
constexpr int er_invalid_plugin_for_registration = 4054;

/// Server error specific to mysql. Error number: 4055, symbol: ER_PLUGIN_REQUIRES_REGISTRATION.
constexpr int er_plugin_requires_registration = 4055;

/// Server error specific to mysql. Error number: 4056, symbol: ER_MFA_METHOD_EXISTS.
constexpr int er_mfa_method_exists = 4056;

/// Server error specific to mysql. Error number: 4057, symbol: ER_MFA_METHOD_NOT_EXISTS.
constexpr int er_mfa_method_not_exists = 4057;

/// Server error specific to mysql. Error number: 4058, symbol: ER_AUTHENTICATION_POLICY_MISMATCH.
constexpr int er_authentication_policy_mismatch = 4058;

/// Server error specific to mysql. Error number: 4059, symbol: ER_PLUGIN_REGISTRATION_DONE.
constexpr int er_plugin_registration_done = 4059;

/// Server error specific to mysql. Error number: 4060, symbol: ER_INVALID_USER_FOR_REGISTRATION.
constexpr int er_invalid_user_for_registration = 4060;

/// Server error specific to mysql. Error number: 4061, symbol: ER_USER_REGISTRATION_FAILED.
constexpr int er_user_registration_failed = 4061;

/// Server error specific to mysql. Error number: 4062, symbol: ER_MFA_METHODS_INVALID_ORDER.
constexpr int er_mfa_methods_invalid_order = 4062;

/// Server error specific to mysql. Error number: 4063, symbol: ER_MFA_METHODS_IDENTICAL.
constexpr int er_mfa_methods_identical = 4063;

/// Server error specific to mysql. Error number: 4064, symbol: ER_INVALID_MFA_OPERATIONS_FOR_PASSWORDLESS_USER.
constexpr int er_invalid_mfa_operations_for_passwordless_user = 4064;

/// Server error specific to mysql. Error number: 4065, symbol: ER_CHANGE_REPLICATION_SOURCE_NO_OPTIONS_FOR_GTID_ONLY.
constexpr int er_change_replication_source_no_options_for_gtid_only = 4065;

/// Server error specific to mysql. Error number: 4066, symbol: ER_CHANGE_REP_SOURCE_CANT_DISABLE_REQ_ROW_FORMAT_WITH_GTID_ONLY.
constexpr int er_change_rep_source_cant_disable_req_row_format_with_gtid_only = 4066;

/// Server error specific to mysql. Error number: 4067, symbol: ER_CHANGE_REP_SOURCE_CANT_DISABLE_AUTO_POSITION_WITH_GTID_ONLY.
constexpr int er_change_rep_source_cant_disable_auto_position_with_gtid_only = 4067;

/// Server error specific to mysql. Error number: 4068, symbol: ER_CHANGE_REP_SOURCE_CANT_DISABLE_GTID_ONLY_WITHOUT_POSITIONS.
constexpr int er_change_rep_source_cant_disable_gtid_only_without_positions = 4068;

/// Server error specific to mysql. Error number: 4069, symbol: ER_CHANGE_REP_SOURCE_CANT_DISABLE_AUTO_POS_WITHOUT_POSITIONS.
constexpr int er_change_rep_source_cant_disable_auto_pos_without_positions = 4069;

/// Server error specific to mysql. Error number: 4070, symbol: ER_CHANGE_REP_SOURCE_GR_CHANNEL_WITH_GTID_MODE_NOT_ON.
constexpr int er_change_rep_source_gr_channel_with_gtid_mode_not_on = 4070;

/// Server error specific to mysql. Error number: 4071, symbol: ER_CANT_USE_GTID_ONLY_WITH_GTID_MODE_NOT_ON.
constexpr int er_cant_use_gtid_only_with_gtid_mode_not_on = 4071;

/// Server error specific to mysql. Error number: 4072, symbol: ER_WARN_C_DISABLE_GTID_ONLY_WITH_SOURCE_AUTO_POS_INVALID_POS.
constexpr int er_warn_c_disable_gtid_only_with_source_auto_pos_invalid_pos = 4072;

/// Server error specific to mysql. Error number: 4073, symbol: ER_DA_SSL_FIPS_MODE_ERROR.
constexpr int er_da_ssl_fips_mode_error = 4073;

/// Server error specific to mysql. Error number: 4074, symbol: ER_VALUE_OUT_OF_RANGE.
constexpr int er_value_out_of_range = 4074;

/// Server error specific to mysql. Error number: 4075, symbol: ER_FULLTEXT_WITH_ROLLUP.
constexpr int er_fulltext_with_rollup = 4075;

/// Server error specific to mysql. Error number: 4076, symbol: ER_REGEXP_MISSING_RESOURCE.
constexpr int er_regexp_missing_resource = 4076;

/// Server error specific to mysql. Error number: 4077, symbol: ER_WARN_REGEXP_USING_DEFAULT.
constexpr int er_warn_regexp_using_default = 4077;

/// Server error specific to mysql. Error number: 4078, symbol: ER_REGEXP_MISSING_FILE.
constexpr int er_regexp_missing_file = 4078;

/// Server error specific to mysql. Error number: 4079, symbol: ER_WARN_DEPRECATED_COLLATION.
constexpr int er_warn_deprecated_collation = 4079;

/// Server error specific to mysql. Error number: 4080, symbol: ER_CONCURRENT_PROCEDURE_USAGE.
constexpr int er_concurrent_procedure_usage = 4080;

/// Server error specific to mysql. Error number: 4081, symbol: ER_DA_GLOBAL_CONN_LIMIT.
constexpr int er_da_global_conn_limit = 4081;

/// Server error specific to mysql. Error number: 4082, symbol: ER_DA_CONN_LIMIT.
constexpr int er_da_conn_limit = 4082;

/// Server error specific to mysql. Error number: 4083, symbol: ER_ALTER_OPERATION_NOT_SUPPORTED_REASON_COLUMN_TYPE_INSTANT.
constexpr int er_alter_operation_not_supported_reason_column_type_instant = 4083;

/// Server error specific to mysql. Error number: 4084, symbol: ER_WARN_SF_UDF_NAME_COLLISION.
constexpr int er_warn_sf_udf_name_collision = 4084;

/// Server error specific to mysql. Error number: 4085, symbol: ER_CANNOT_PURGE_BINLOG_WITH_BACKUP_LOCK.
constexpr int er_cannot_purge_binlog_with_backup_lock = 4085;

/// Server error specific to mysql. Error number: 4086, symbol: ER_TOO_MANY_WINDOWS.
constexpr int er_too_many_windows = 4086;

/// Server error specific to mysql. Error number: 4087, symbol: ER_MYSQLBACKUP_CLIENT_MSG.
constexpr int er_mysqlbackup_client_msg = 4087;

/// Server error specific to mysql. Error number: 4088, symbol: ER_COMMENT_CONTAINS_INVALID_STRING.
constexpr int er_comment_contains_invalid_string = 4088;

/// Server error specific to mysql. Error number: 4089, symbol: ER_DEFINITION_CONTAINS_INVALID_STRING.
constexpr int er_definition_contains_invalid_string = 4089;

/// Server error specific to mysql. Error number: 4090, symbol: ER_CANT_EXECUTE_COMMAND_WITH_ASSIGNED_GTID_NEXT.
constexpr int er_cant_execute_command_with_assigned_gtid_next = 4090;

/// Server error specific to mysql. Error number: 4091, symbol: ER_XA_TEMP_TABLE.
constexpr int er_xa_temp_table = 4091;

/// Server error specific to mysql. Error number: 4092, symbol: ER_INNODB_MAX_ROW_VERSION.
constexpr int er_innodb_max_row_version = 4092;

/// Server error specific to mysql. Error number: 4094, symbol: ER_OPERATION_NOT_ALLOWED_WHILE_PRIMARY_CHANGE_IS_RUNNING.
constexpr int er_operation_not_allowed_while_primary_change_is_running = 4094;

/// Server error specific to mysql. Error number: 4095, symbol: ER_WARN_DEPRECATED_DATETIME_DELIMITER.
constexpr int er_warn_deprecated_datetime_delimiter = 4095;

/// Server error specific to mysql. Error number: 4096, symbol: ER_WARN_DEPRECATED_SUPERFLUOUS_DELIMITER.
constexpr int er_warn_deprecated_superfluous_delimiter = 4096;

/// Server error specific to mysql. Error number: 4097, symbol: ER_CANNOT_PERSIST_SENSITIVE_VARIABLES.
constexpr int er_cannot_persist_sensitive_variables = 4097;

/// Server error specific to mysql. Error number: 4098, symbol: ER_WARN_CANNOT_SECURELY_PERSIST_SENSITIVE_VARIABLES.
constexpr int er_warn_cannot_securely_persist_sensitive_variables = 4098;

/// Server error specific to mysql. Error number: 4099, symbol: ER_WARN_TRG_ALREADY_EXISTS.
constexpr int er_warn_trg_already_exists = 4099;

/// Server error specific to mysql. Error number: 4100, symbol: ER_IF_NOT_EXISTS_UNSUPPORTED_TRG_EXISTS_ON_DIFFERENT_TABLE.
constexpr int er_if_not_exists_unsupported_trg_exists_on_different_table = 4100;

/// Server error specific to mysql. Error number: 4101, symbol: ER_IF_NOT_EXISTS_UNSUPPORTED_UDF_NATIVE_FCT_NAME_COLLISION.
constexpr int er_if_not_exists_unsupported_udf_native_fct_name_collision = 4101;

/// Server error specific to mysql. Error number: 4102, symbol: ER_SET_PASSWORD_AUTH_PLUGIN_ERROR.
constexpr int er_set_password_auth_plugin_error = 4102;

/// Server error specific to mysql. Error number: 4105, symbol: ER_SRS_INVALID_LATITUDE_OF_ORIGIN.
constexpr int er_srs_invalid_latitude_of_origin = 4105;

/// Server error specific to mysql. Error number: 4106, symbol: ER_SRS_INVALID_LONGITUDE_OF_ORIGIN.
constexpr int er_srs_invalid_longitude_of_origin = 4106;

/// Server error specific to mysql. Error number: 4107, symbol: ER_SRS_UNUSED_PROJ_PARAMETER_PRESENT.
constexpr int er_srs_unused_proj_parameter_present = 4107;

/// Server error specific to mysql. Error number: 4108, symbol: ER_GIPK_COLUMN_EXISTS.
constexpr int er_gipk_column_exists = 4108;

/// Server error specific to mysql. Error number: 4109, symbol: ER_GIPK_FAILED_AUTOINC_COLUMN_EXISTS.
constexpr int er_gipk_failed_autoinc_column_exists = 4109;

/// Server error specific to mysql. Error number: 4110, symbol: ER_GIPK_COLUMN_ALTER_NOT_ALLOWED.
constexpr int er_gipk_column_alter_not_allowed = 4110;

/// Server error specific to mysql. Error number: 4111, symbol: ER_DROP_PK_COLUMN_TO_DROP_GIPK.
constexpr int er_drop_pk_column_to_drop_gipk = 4111;

/// Server error specific to mysql. Error number: 4112, symbol: ER_CREATE_SELECT_WITH_GIPK_DISALLOWED_IN_SBR.
constexpr int er_create_select_with_gipk_disallowed_in_sbr = 4112;

/// Server error specific to mysql. Error number: 4113, symbol: ER_DA_EXPIRE_LOGS_DAYS_IGNORED.
constexpr int er_da_expire_logs_days_ignored = 4113;

/// Server error specific to mysql. Error number: 4114, symbol: ER_CTE_RECURSIVE_NOT_UNION.
constexpr int er_cte_recursive_not_union = 4114;

/// Server error specific to mysql. Error number: 4115, symbol: ER_COMMAND_BACKEND_FAILED_TO_FETCH_SECURITY_CTX.
constexpr int er_command_backend_failed_to_fetch_security_ctx = 4115;

/// Server error specific to mysql. Error number: 4116, symbol: ER_COMMAND_SERVICE_BACKEND_FAILED.
constexpr int er_command_service_backend_failed = 4116;

/// Server error specific to mysql. Error number: 4117, symbol: ER_CLIENT_FILE_PRIVILEGE_FOR_REPLICATION_CHECKS.
constexpr int er_client_file_privilege_for_replication_checks = 4117;

/// Server error specific to mysql. Error number: 4118, symbol: ER_GROUP_REPLICATION_FORCE_MEMBERS_COMMAND_FAILURE.
constexpr int er_group_replication_force_members_command_failure = 4118;

/// Server error specific to mysql. Error number: 4119, symbol: ER_WARN_DEPRECATED_IDENT.
constexpr int er_warn_deprecated_ident = 4119;

/// Server error specific to mysql. Error number: 4120, symbol: ER_INTERSECT_ALL_MAX_DUPLICATES_EXCEEDED.
constexpr int er_intersect_all_max_duplicates_exceeded = 4120;

/// Server error specific to mysql. Error number: 4121, symbol: ER_TP_QUERY_THRS_PER_GRP_EXCEEDS_TXN_THR_LIMIT.
constexpr int er_tp_query_thrs_per_grp_exceeds_txn_thr_limit = 4121;

/// Server error specific to mysql. Error number: 4122, symbol: ER_BAD_TIMESTAMP_FORMAT.
constexpr int er_bad_timestamp_format = 4122;

/// Server error specific to mysql. Error number: 4123, symbol: ER_SHAPE_PRIDICTION_UDF.
constexpr int er_shape_pridiction_udf = 4123;

/// Server error specific to mysql. Error number: 4124, symbol: ER_SRS_INVALID_HEIGHT.
constexpr int er_srs_invalid_height = 4124;

/// Server error specific to mysql. Error number: 4125, symbol: ER_SRS_INVALID_SCALING.
constexpr int er_srs_invalid_scaling = 4125;

/// Server error specific to mysql. Error number: 4126, symbol: ER_SRS_INVALID_ZONE_WIDTH.
constexpr int er_srs_invalid_zone_width = 4126;

/// Server error specific to mysql. Error number: 4127, symbol: ER_SRS_INVALID_LATITUDE_POLAR_STERE_VAR_A.
constexpr int er_srs_invalid_latitude_polar_stere_var_a = 4127;

/// Server error specific to mysql. Error number: 4128, symbol: ER_WARN_DEPRECATED_CLIENT_NO_SCHEMA_OPTION.
constexpr int er_warn_deprecated_client_no_schema_option = 4128;

/// Server error specific to mysql. Error number: 4129, symbol: ER_TABLE_NOT_EMPTY.
constexpr int er_table_not_empty = 4129;

/// Server error specific to mysql. Error number: 4130, symbol: ER_TABLE_NO_PRIMARY_KEY.
constexpr int er_table_no_primary_key = 4130;

/// Server error specific to mysql. Error number: 4131, symbol: ER_TABLE_IN_SHARED_TABLESPACE.
constexpr int er_table_in_shared_tablespace = 4131;

/// Server error specific to mysql. Error number: 4132, symbol: ER_INDEX_OTHER_THAN_PK.
constexpr int er_index_other_than_pk = 4132;

/// Server error specific to mysql. Error number: 4133, symbol: ER_LOAD_BULK_DATA_UNSORTED.
constexpr int er_load_bulk_data_unsorted = 4133;

/// Server error specific to mysql. Error number: 4134, symbol: ER_BULK_EXECUTOR_ERROR.
constexpr int er_bulk_executor_error = 4134;

/// Server error specific to mysql. Error number: 4135, symbol: ER_BULK_READER_LIBCURL_INIT_FAILED.
constexpr int er_bulk_reader_libcurl_init_failed = 4135;

/// Server error specific to mysql. Error number: 4136, symbol: ER_BULK_READER_LIBCURL_ERROR.
constexpr int er_bulk_reader_libcurl_error = 4136;

/// Server error specific to mysql. Error number: 4137, symbol: ER_BULK_READER_SERVER_ERROR.
constexpr int er_bulk_reader_server_error = 4137;

/// Server error specific to mysql. Error number: 4138, symbol: ER_BULK_READER_COMMUNICATION_ERROR.
constexpr int er_bulk_reader_communication_error = 4138;

/// Server error specific to mysql. Error number: 4139, symbol: ER_BULK_LOAD_DATA_FAILED.
constexpr int er_bulk_load_data_failed = 4139;

/// Server error specific to mysql. Error number: 4140, symbol: ER_BULK_LOADER_COLUMN_TOO_BIG_FOR_LEFTOVER_BUFFER.
constexpr int er_bulk_loader_column_too_big_for_leftover_buffer = 4140;

/// Server error specific to mysql. Error number: 4141, symbol: ER_BULK_LOADER_COMPONENT_ERROR.
constexpr int er_bulk_loader_component_error = 4141;

/// Server error specific to mysql. Error number: 4142, symbol: ER_BULK_LOADER_FILE_CONTAINS_LESS_LINES_THAN_IGNORE_CLAUSE.
constexpr int er_bulk_loader_file_contains_less_lines_than_ignore_clause = 4142;

/// Server error specific to mysql. Error number: 4143, symbol: ER_BULK_PARSER_MISSING_ENCLOSED_BY.
constexpr int er_bulk_parser_missing_enclosed_by = 4143;

/// Server error specific to mysql. Error number: 4144, symbol: ER_BULK_PARSER_ROW_BUFFER_MAX_TOTAL_COLS_EXCEEDED.
constexpr int er_bulk_parser_row_buffer_max_total_cols_exceeded = 4144;

/// Server error specific to mysql. Error number: 4145, symbol: ER_BULK_PARSER_COPY_BUFFER_SIZE_EXCEEDED.
constexpr int er_bulk_parser_copy_buffer_size_exceeded = 4145;

/// Server error specific to mysql. Error number: 4146, symbol: ER_BULK_PARSER_UNEXPECTED_END_OF_INPUT.
constexpr int er_bulk_parser_unexpected_end_of_input = 4146;

/// Server error specific to mysql. Error number: 4147, symbol: ER_BULK_PARSER_UNEXPECTED_ROW_TERMINATOR.
constexpr int er_bulk_parser_unexpected_row_terminator = 4147;

/// Server error specific to mysql. Error number: 4148, symbol: ER_BULK_PARSER_UNEXPECTED_CHAR_AFTER_ENDING_ENCLOSED_BY.
constexpr int er_bulk_parser_unexpected_char_after_ending_enclosed_by = 4148;

/// Server error specific to mysql. Error number: 4149, symbol: ER_BULK_PARSER_UNEXPECTED_CHAR_AFTER_NULL_ESCAPE.
constexpr int er_bulk_parser_unexpected_char_after_null_escape = 4149;

/// Server error specific to mysql. Error number: 4150, symbol: ER_BULK_PARSER_UNEXPECTED_CHAR_AFTER_COLUMN_TERMINATOR.
constexpr int er_bulk_parser_unexpected_char_after_column_terminator = 4150;

/// Server error specific to mysql. Error number: 4151, symbol: ER_BULK_PARSER_INCOMPLETE_ESCAPE_SEQUENCE.
constexpr int er_bulk_parser_incomplete_escape_sequence = 4151;

/// Server error specific to mysql. Error number: 4152, symbol: ER_LOAD_BULK_DATA_FAILED.
constexpr int er_load_bulk_data_failed = 4152;

/// Server error specific to mysql. Error number: 4153, symbol: ER_LOAD_BULK_DATA_WRONG_VALUE_FOR_FIELD.
constexpr int er_load_bulk_data_wrong_value_for_field = 4153;

/// Server error specific to mysql. Error number: 4154, symbol: ER_LOAD_BULK_DATA_WARN_NULL_TO_NOTNULL.
constexpr int er_load_bulk_data_warn_null_to_notnull = 4154;

/// Server error specific to mysql. Error number: 4155, symbol: ER_REQUIRE_TABLE_PRIMARY_KEY_CHECK_GENERATE_WITH_GR.
constexpr int er_require_table_primary_key_check_generate_with_gr = 4155;

/// Server error specific to mysql. Error number: 4156, symbol: ER_CANT_CHANGE_SYS_VAR_IN_READ_ONLY_MODE.
constexpr int er_cant_change_sys_var_in_read_only_mode = 4156;

/// Server error specific to mysql. Error number: 4157, symbol: ER_INNODB_INSTANT_ADD_DROP_NOT_SUPPORTED_MAX_SIZE.
constexpr int er_innodb_instant_add_drop_not_supported_max_size = 4157;

/// Server error specific to mysql. Error number: 4158, symbol: ER_INNODB_INSTANT_ADD_NOT_SUPPORTED_MAX_FIELDS.
constexpr int er_innodb_instant_add_not_supported_max_fields = 4158;

/// Server error specific to mysql. Error number: 4159, symbol: ER_CANT_SET_PERSISTED.
constexpr int er_cant_set_persisted = 4159;

/// Server error specific to mysql. Error number: 4160, symbol: ER_INSTALL_COMPONENT_SET_NULL_VALUE.
constexpr int er_install_component_set_null_value = 4160;

/// Server error specific to mysql. Error number: 4161, symbol: ER_INSTALL_COMPONENT_SET_UNUSED_VALUE.
constexpr int er_install_component_set_unused_value = 4161;

/// Server error specific to mysql. Error number: 4162, symbol: ER_WARN_DEPRECATED_USER_DEFINED_COLLATIONS.
constexpr int er_warn_deprecated_user_defined_collations = 4162;

/// Server error specific to mysql. Error number: 4163, symbol: ER_USER_LOCK_OVERLONG_NAME.
constexpr int er_user_lock_overlong_name = 4163;

/// Server error specific to mysql. Error number: 4164, symbol: ER_WARN_NO_SPACE_VERSION_COMMENT.
constexpr int er_warn_no_space_version_comment = 4164;

/// Server error specific to mysql. Error number: 4165, symbol: ER_VALIDATE_PASSWORD_INSUFFICIENT_CHANGED_CHARACTERS.
constexpr int er_validate_password_insufficient_changed_characters = 4165;

}  // namespace mysql_server_errc

}  // namespace mysql
}  // namespace boost

#endif
