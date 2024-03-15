package me.him188.ani.app.data

/**
 * 每个 Repository 封装对相应数据的访问逻辑, 提供简单的接口, 让调用方无需关心数据的来源是网络还是本地存储.
 *
 * 实现约束:
 * - 所有访问数据的接口都只会抛出 [RepositoryException], 用于向调用方传递已知的情况, 例如网络连接失败.
 *   其他异常属于 bug.
 */
interface Repository