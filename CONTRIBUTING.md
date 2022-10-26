[Compose for Desktop]: https://www.jetbrains.com/lp/compose-desktop/

[动漫花园]: https://www.dmhy.org/

[context receivers]: https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md

# 参与开发

欢迎你提交 PR 参与开发。本文将介绍项目结构等可能帮助你的内容。

## 项目结构

本项目使用 Gradle 构建，有以下几个模块：

- `api`
- `common`
- `desktop`
- `server`
- `android`

> 可在 [settings.gradle.kts](settings.gradle.kts) 详细查看

`api` 实现从[动漫花园]官网获取资源并解析的 API，供其他模块使用。

`common` 为 UI 通用代码，是 Kotlin MPP（Multiplatform Project，多平台项目），供桌面应用模块 `desktop` 和 Android 应用模块 `android` 使用，实现最大化的组件共享。

## 编译项目

执行 `./gradlew build` 即可编译并运行测试。需要正确的 Android SDK 配置才能完成编译。在没有配置时，编译将会出错并提示如何配置。

要构建桌面应用，请参考 [Compose for Desktop] 官方文档，或简单执行 `./gradlew package`。这将进行 `desktop` 模块的编译打包操作，结果保存在 `desktop/build/compose/binaries` 中。

要构建 Android 应用，请执行 `./gradlew assembleRelease` 或 `./gradlew assembleDebug`，分别编译发布版或测试版。使用 `./gradlew installRelease` 或 `./gradlew installDebug` 还可以构建应用并安装到模拟器。

## 开发提示

### 代码规范

这是一个"玩具项目"，能跑的代码就是好代码，不过也请遵守最基本的规范（例如命名规范、避免大量代码重复）。

欢迎你尝试各种新特性或设计想法，比如体验 Kotlin 预览版的 [context receivers] （使用它的项目甚至不能简单地由其他项目依赖，但在这个玩具项目中使用无妨）。

### 获取帮助

由于我时间有限，项目里只写了必要的注释。如果你对项目代码有疑惑，幻影在 issues 提交。