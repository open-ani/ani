# Ani

[dmhy]: http://www.dmhy.org/

[Bangumi]: http://bangumi.tv

[Compose Multiplatform]: https://www.jetbrains.com/lp/compose-mpp/

集找番、追番、看番的一站式追番平台。

使用 Bangumi 的番剧索引以及观看记录功能，支持 [动漫花园][dmhy] 等下载源，未来接入弹弹play 等平台实现在线播放。

开发重点在于追番和找番体验，以及实用性. 项目来源于我和朋友的真实追番需求: 记录追番进度, 下载字幕组资源.

> 我不是专业客户端开发人员, 开发纯属兴趣, 不过比较讲究代码质量, 欢迎各位指点.

## 3.0 开发进程

Ani 3.0 正在开发中. 继续使用 [Compose Multiplatform] 实现多平台, 将私有追番进度服务器改为使用
Bangumi,
并从 Bangumi 获取番剧信息以及相关评论等.

### 模块列表

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

## 下载

Ani 支持 Android 和桌面端 (macOS、Linux、Windows)。

3.0 重构还在进行中, 2.0 正式版本可在 [releases](https://github.com/Him188/ani/releases/latest)
中的 "Assets" 下载最新正式版本。

2.x 测试版本可以在 [releases](https://github.com/Him188/ani/releases/) 找到。
使用测试版本可以体验最新特性，但可能不稳定。

3.0 开发测试版本可以在每个 commit 的自动构建中找到。

## 功能截图

快速开发中, 我每隔几天会更一下截图. 实际样式请以最新版本为准.

采用 Material 3 风格 (略有根据个人审美调整), 支持同步系统的浅色和深色主题。

### 登录

使用 Bangumi OAuth 登录. 登录状态可以保持较久.

### 个人收藏页面

- 同步 Bangumi 收藏
- 支持修改收藏状态
- "已完结" / "连载至" 标签展示连载情况
- 自动滚动到上次观看的剧集, 已观看过的剧集展示颜色更浅

<img width="300" src=".readme/images/collection/collection-light.jpeg" alt="collection-light"/> <img width="300" src=".readme/images/collection/collection-dark.jpg" alt="collection-dark"/>

### 番剧详情页面

根据一般人找番时会关注的如下几点设计:

- 开播时间
- 声优
- 制作公司, 监督
- 海报第一印象
- 角色第一印象
- 追番人数

待 3.0 正式版发布后的未来会增加其他更多补充信息.

<img width="300" src=".readme/images/subject/subject-light.jpeg" alt="subject-light"/> <img width="300" src=".readme/images/subject/subject-dark.jpg" alt="subject-dark"/>

### 剧集详情与播放页面

- 后台自动从[动漫花园][dmhy]拉取字幕组资源并解析标题
- 支持选择一般人最关心的参数: 清晰度, 字幕语言, 字幕组
- 默认选择第一个字幕组, 无需配置即可自动播放
- 手动修改字幕组后会为此番剧记住字幕组选择, 下次自动选择

<img width="300" src=".readme/images/episode/episode-dark.jpg" alt="episode-dark"/> <img width="300" src=".readme/images/episode/episode-playsource-dark.jpg" alt="episode-light"/>

备注: 播放器正在开发中, 目前仅支持选择播放源并记录上次的选择然后复制链接.

### 标题 / 标签搜索页面

~重构掉了, 下次补上~

## 参与开发

欢迎你提交 [PR](https://github.com/Him188/ani/pulls) [参与开发](CONTRIBUTING.md)。

## 提示

#### 访问动漫花园

动漫花园在中国大陆无法通过 IPv4 访问。你可能需要一些技术手段，或者在一个有 IPv6 的环境 (例如数据网络)
，才能正常使用。

在桌面端，可以在设置（Windows 在标题栏，macOS 在屏幕左上角点击"动漫花园"）中设置使用代理。代理是默认禁用的。初始的
HTTP
代理设置为连接本地 Clash 并使用 Clash 的默认端口。

#### 额外设置

部分桌面端会支持额外设置，这些设置都可以由上述方法看到，如果看不到就是没有。例如 macOS
端支持窗口沉浸（将背景颜色绘制到标题栏内，默认启用）。
