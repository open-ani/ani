# Ani

[dmhy]: http://www.dmhy.org/

[Bangumi]: http://bangumi.tv

[Compose Multiplatform]: https://www.jetbrains.com/lp/compose-mpp/

集找番、追番、看番的一站式追番平台。

使用 Bangumi 的番剧索引以及观看记录功能，支持 [动漫花园][dmhy] 等下载源，未来接入弹弹play 等平台实现在线播放。

开发重点在于追番和找番体验，以及实用性. 项目来源于我和朋友的真实追番需求: 记录追番进度, 下载字幕组资源.

> 我不是专业客户端开发人员, 开发纯属兴趣, 不过比较讲究代码质量, 欢迎各位指点.

## 3.0 开发进程

Ani 3.0 完全重写, 继续使用 [Compose Multiplatform] 实现多平台, 将私有追番进度服务器改为使用 Bangumi,
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

### 数据源列表

每个数据源的实现与客户端独立.

## 下载

Ani 支持 Android 和桌面端 (macOS、Linux、Windows)。

3.0 重构还在进行中, 2.0 正式版本可在 [releases](https://github.com/Him188/ani/releases/latest)
中的 "Assets" 下载最新正式版本。

2.x 测试版本可以在 [releases](https://github.com/Him188/ani/releases/) 找到。
使用测试版本可以体验最新特性，但可能不稳定。

3.0 开发测试版本可以在每个 commit 的自动构建中找到。

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
