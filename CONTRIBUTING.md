[Compose for Desktop]: https://www.jetbrains.com/lp/compose-desktop/

[动漫花园]: https://www.dmhy.org/

[context receivers]: https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md

# 参与开发

欢迎你提交 PR 参与开发。本文将介绍项目结构等可能帮助你的内容。

## 目录

1. 开发工具: IDE, JDK, 推荐插件
2. 代码风格: 格式化, 规范
3. 模块结构: 整个项目范围的模块划分, 模块间依赖
4. 依赖管理: 依赖版本管理
5. 构建打包: 如何编译, 如何打包 APK, 如何调试
6. App 架构: 最主要的客户端模块的层级划分, 以及各层的职责
7. 开发与调试: 源集结构, 预览 Compose UI, Navigation, 以及一些坑

## 1. 开发工具

因为项目使用 Kotlin MPP (多平台), 建议使用 Android Studio 的最新 Canary 测试版本以获得最佳体验.

使用 IntelliJ IDEA 则需要最低版本至少为 `2023.3`.

- 需要 JDK 版本至少为 11.
- 需要 Android SDK 版本至少为 API 34.

安装如下 IDE 插件:

- Android
- Jetpack Compose
- Compose Multiplatform IDE Support
- Compose colors preview (可选安装, 用于预览颜色)

> 如果不按照上述要求, 你可能会遇到奇怪的难以解决的问题.

## 2. 代码风格

### 格式化

项目根目录有 `.editorconfig` 文件, IDE 会自动读取该文件以确保代码风格一致.
请使用 IDE 提供的自动代码格式化功能即可.

### 代码规范

这是一个"玩具项目"
，能跑的代码就是好代码，不过也请至少遵守遵循 [Kotlin 官方代码风格指南](https://kotlinlang.org/docs/coding-conventions.html).

## 3. 模块结构

```
ani
├── buildSrc (Gradle 构建)
├── ci-helper (GitHub release 用)
├── data-sources (数据源)
│   ├── api (数据源抽象)
│   ├── bangumi (Bangumi API)
│   └── dmhy (动漫花园 API)
├── app (客户端)
│   ├── shared (桌面端与客户端共享代码)
│   ├── desktop (桌面端独享代码)
│   ├── torrent (BitTorrent 下载支持库)
│   ├── video-player (视频播放器支持库)
│   └── android (Android 客户端独享代码)
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

## 4. 依赖管理

Ani 使用 Gradle Version Catalogs. 依赖位于 `gradle/libs.versions.toml`.

提示: 更新依赖后, 请先让 IDE sync 一下新的配置, 然后才能在 `build.gradle.kts` 中有 `libs.xxx` 的自动补全.

## 5. 构建打包

要构建项目, 你需要首先进行几个简单配置:

### 配置 Bangumi OAuth

1. 前往 <https://bangumi.tv/dev/app>
2. 创建一个新应用
    - 应用名任意
    - 主页地址请**不要**填写本项目地址, 请填写你的私人 fork 地址或个人主页地址.
3. 编辑你刚刚创建的新应用
    - 回调地址设置为 `ani://bangumi-oauth-callback`
    - 记录 App ID 和 App Secret
4. 在项目根目录的 `local.properties` 或 Gradle Home 的 `gradle.properties` 中添加如下内容 (
   替换相应内容为你应用的):
    - `bangumi.oauth.client.id=bgmXXXXXXXX`
    - `bangumi.oauth.client.secret=XXXXXXXX`
5. 在 IDE 同步项目, 执行一次构建 (`./gradlew build`). 待构建完成后 (双击 shift)
   查看生成的 `me.him188.ani.android.BuildConfig`, 你应当能看到你的应用 ID 和
   Secret 已经更新到该文件中.
   > 桌面端也会自动有类似的 BuildConfig 文件生成. 它与 Android 端的是同步的.

### 配置 Android 签名

在构建安卓目标时会自动弹出配置. 跟随 IDE 的指引即可.

### 执行构建

执行 `./gradlew build` 即可编译并运行测试。需要正确的 Android SDK 配置才能完成编译。在没有配置时，编译将会出错并提示如何配置。

要构建桌面应用，请参考 [Compose for Desktop] 官方文档，或简单执行 `./gradlew package`
。这将进行 `desktop` 模块的所有编译打包操作，结果保存在 `desktop/build/compose/binaries` 中。

一个操作系统只能构建对应的桌面应用，例如 Windows 只能构建 Windows 应用，而不能构建 macOS 应用。

要构建 Android 应用，请执行 `./gradlew assembleRelease` 或 `./gradlew assembleDebug`
，分别编译发布版或测试版。使用 `./gradlew installRelease` 或 `./gradlew installDebug` 还可以构建应用并安装到模拟器。

## 6. App 项目架构

本节将介绍客户端模块 (`:app`) 的架构设计. 所提及目录均位于 `app/shared/commonMain` 下.

与通常的 Android 应用开发一样, ani 从外到内有: UI 层, 数据层, 网络层/持久层.

> 理论上使用数据源提供的数据还要设计一个兼容层, 但应该很长时间内都只会使用 Bangumi 源,
> 就没有为条目等数据增加兼容层.

### UI 层

主要采用 MVVM 模式. MVVM 设计模式中的 Model 部分在 ani 中为各数据源提供的数据类.
View 与 ViewModel 部分在 `ui` 目录中, 相关联的 View 和 ViewModel 放置在同一目录下.

#### UI 层组织方式

- 一个 Activity 的内容为一个 Screen
- 一个 Screen 为 navigation controller 容器, 支持在多个 scene (场景) 之间跳转.
- 一个 Scene 的内容是 Page. Scene 将与 navigation 无关的 Page 对接到 navigation 系统上.
    - 通常来说, 一个 Page 只会被一个 Scene 使用. 这样设计只是为了在预览 UI 的时候无需考虑 navigation
      相关兼容问题.
- Page 为实际包含 UI 控件 (`Column`, `Button` 等) 的容器.

在 Android, ani 主要有一个 `MainActivity`.
`MainActivity` 使用 `main/MainScreen`.
`MainScreen` 包含一个 navigation controller, 有如下几个 scene:

- `ui/home/HomeScene.kt`: 打开应用会进入的首页. 包含如下几个 page, 通过底部导航栏按钮切换 page.
    - `HomePage`: 包含一个搜索条, 未来可能会做一些推荐之类的.
    - `CollectionPage`: 自己的追番列表, 可以跳转详情或者直接播放.
    - `ProfilePage`: 个人主页, 未来的设置也会加载这里面.
- `ui/auth/AuthRequestScene.kt`: 请求登录场景. 当请求 Bangumi 登录时会跳转.
- `ui/collection/CollectionPage`: 位于 `collection` 目录. 用于展示收藏的番剧.
- `ui/subject/details/SubjectDetailsScene.kt`: 番剧详情场景. 当点击搜索结果或点击收藏的番剧时会跳转.
  展示番剧的角色与声优, 制作人员等信息. 也可以跳转到对应剧集的播放.
- `ui/subject/episode/EpisodeScene.kt`: 剧集详情场景. 有一个视频播放器, 可以剧集的评论等.

#### ViewModel

所有 ViewModel 继承 `AbstractViewModel`. 在构造示例后必须通过 Compose 的 `remember {}` 绑定生命周期到当前
composable.

### 数据层

数据层主要包含两个部分: 数据源 (data source) 与数据仓库 (repository).

各数据源位于 app 模块之外的项目根目录的 `data-sources` 目录下. 数据源列表:

- `data-sources/bangumi`: Bangumi 索引数据源, 提供番剧索引, 观看记录等.
- `data-sources/api`: 下载数据源的抽象, 定义了数据源的接口以供接入多个下载数据源.
- `data-sources/dmhy`: 动漫花园下载数据源, 只提供番剧下载链接.
- ... 未来接入更多的下载数据源

数据仓库位于 `app/shared/commonMain/data` 目录下. 数据仓库的职责是将数据源提供的数据进行整合, 为 UI
层提供统一的数据接口, 并且让 UI 层无需关心数据源的具体实现. 仓库不仅对接上述数据源,
还封装对本地数据库的操作 (例如保存的 token 和用户的字幕组偏好).

可阅读 `app/shared/commonMain/data/Repository.kt` 了解实现细节.

### 网络层/持久层

网络请求在数据源中实现. 各个数据源会封装一套操作该数据源的 API 接口, 然后由各 Repository 调用.

- `data-sources/bangumi` 的 API 客户端是根据其官方 OpenAPI 文档自动生成的.
- `data-sources/dmhy` 的 API 客户端是手写的.

对于本地保存的内容, 例如 token, 用户偏好等, ani 使用 Google DataStore 进行保存. 各个 store
的定义在 `persistent` 目录下.

你若只是写 ani 的 app 部分, 应该不需要太关心这些细节. 相关用法示例可以在对应的 Repository 找到.

### app 其他内容

- `platform`: 进行平台适配的专用内容. 例如 `CommonKoinModule`, `Context`, `currentPlatform`.
- `navigation`: Navigation controller 封装.
  > 因为使用的 PreCompose 库并不靠谱. 封装一下日后若有问题可以简单切换.
- `session`: 管理 Bangumi OAuth 会话.
- `ui/foundation`: 可复用的基本组件, 例如图像控件 `AniKamelImage`, `Tabs`, `AniTopAppBar` 等.
- `ui/theme`: 自定义主题.
- `ui/external`: 一些外部库的 fork, 例如 Google Accompanist 的 Placeholders.
  > 它应当放在单独的模块, 但由于 Kotlin 的 bug, 目前只能先这样.
- `videoplayer`: 视频播放器
  > 它同样应当在单独的模块.

## 7. 开发与调试

### 源集 (source set) 结构

为了减少文件树层级, 项目使用了一些特殊的源集结构.

在 `commonMain`, `androidMain` 等目录直接存放源码, 而不需要 `src/kotlin` 目录.
资源放置于 `commonResources`, `androidResources` 等目录下.

若你不是很清楚 Gradle 这方面的功能, 只需模仿已有的源代码存放新文件.

### 预览 Compose UI

因为项目支持 Android 和桌面两个平台, 预览也就分两个平台.
Compose 多平台项目的预览支持目前不是特别好. 不支持在 common 平台选择预览目标平台.

绝大部分 UI 代码写在 commonMain 中 (这样才能在两个平台共享代码). 在 commonMain 中, 使用 `@Preview`
只能预览桌面端的 UI. 这通常来说与手机上的是一样的.
但是桌面预览不支持可交互式预览 (Interactive Mode).

若要预览 Android 端的 UI, 需要在 `androidMain` 添加函数使用 `@Preview` 注解, 就像 Jetpack Compose
那样. 你可以在 commonMain 中添加一个 `@Composable expect fun PreviewXXX`, 根据 IDE 的错误提示自动修复,
为两个平台都添加对应的 `actual fun`. 然后为其添加 `@Preview` 注解.  
这样就可以使用如下 IDE 分屏功能进行 Android 平台预览.

项目中的一个这样的示例为 `PreviewPlayerControllerOverlay`.

<img width="600" src=".readme/images/contributing/previewing-compose-ui.png" alt="previewing-compose-ui"/>

### Navigation

Navigation 使用了 [PreCompose](https://github.com/Tlaster/PreCompose). 它与 Jetpack NavHost 用法类似.
*也有很多坑, 但我已经踩完了.*

封装为了 `AniNavigator`, 在 composable 中可以使用 `LocalAniNavigator.current` 获取当前 navigator,
然后使用
`navigateXxx` 进行跳转.
