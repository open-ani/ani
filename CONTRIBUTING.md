[Compose for Desktop]: https://www.jetbrains.com/lp/compose-desktop/

[动漫花园]: https://www.dmhy.org/

[context receivers]: https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md

# 参与开发

欢迎你提交 PR 参与开发。本文将介绍项目结构等可能帮助你的内容。

## 项目结构

```
ani
├── buildSrc (Gradle 构建)
├── ci-helper (GitHub release 用)
├── data-sources (数据源)
│   ├── api (数据源抽象)
│   ├── bangumi (Bangumi API)
│   └── dmhy (动漫花园 API)
├── app (客户端)
│   ├── shared (通用)
│   ├── desktop (桌面端)
│   └── android (Android 客户端)
└── utils (模块共享工具库)
```

- 有关客户端模块化: 客户端绝大部分 UI, 数据层, 网络层以及多平台相关代码等全都位于 `app/shared` 中.
  最佳实践是将功能拆分模块, 但由于 Kotlin 的 bug, 拆分模块会导致 Android Studio 无法正确解决多平台依赖的目标源集,
  因此暂时不拆分.
  Kotlin 2.0 正式版不久后就会发布, 这个问题会被修复, 届时可能再会考虑拆分模块.

- 有关数据源独立: 数据源的实现独立于客户端逻辑. 它其实可以单独拆分作为一个库发布, 不过我比较懒.

> 可在 [settings.gradle.kts](settings.gradle.kts) 详细查看

`common` 为 UI 通用代码，是 Kotlin MPP（Multiplatform Project，多平台项目），供桌面应用模块 `desktop` 和
Android 应用模块 `android` 使用，实现最大化的组件共享。

## 依赖管理

Ani 使用 Gradle Version Catalogs. 依赖位于 `gradle/libs.versions.toml`.

## 编译项目

执行 `./gradlew build` 即可编译并运行测试。需要正确的 Android SDK 配置才能完成编译。在没有配置时，编译将会出错并提示如何配置。

要构建桌面应用，请参考 [Compose for Desktop] 官方文档，或简单执行 `./gradlew package`
。这将进行 `desktop` 模块的编译打包操作，结果保存在 `desktop/build/compose/binaries` 中。

要构建 Android 应用，请执行 `./gradlew assembleRelease` 或 `./gradlew assembleDebug`
，分别编译发布版或测试版。使用 `./gradlew installRelease` 或 `./gradlew installDebug` 还可以构建应用并安装到模拟器。

## 开发提示

### 代码规范

这是一个"玩具项目"，能跑的代码就是好代码，不过也请遵守最基本的规范（例如命名规范、避免大量代码重复）。

欢迎你尝试各种新特性或设计想法，比如体验 Kotlin 预览版的 [context receivers]
（使用它的项目甚至不能简单地由其他项目依赖，但在这个玩具项目中使用无妨）。

### 获取帮助

由于我时间有限，项目里只写了必要的注释。如果你对项目代码有疑惑，幻影在 issues 提交。