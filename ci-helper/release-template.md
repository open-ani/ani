Automatically created from tag ${{ steps.tag.outputs.tag }}. Do not change anything until assets are
uploaded.

----

### 下载

[//]: # (注意, `checkLatestVersion` 有字符串处理, 修改标题和分隔符前务必查询)

[github-win-x64]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-windows-x86_64.zip

[github-mac-x64]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-x86_64.dmg

[github-mac-aarch64]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-aarch64.dmg

[github-android]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}.apk

[cf-win-x64]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-windows-x86_64.zip

[cf-mac-x64]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-x86_64.dmg

[cf-mac-aarch64]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-aarch64.dmg

[cf-android]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}.apk

[qb-enhanced]: https://github.com/c0re100/qBittorrent-Enhanced-Edition/releases/latest

PC 目前默认只使用在线数据源。
若本机装有 qBittorrent 或 [qBittorrent Enhanced][qb-enhanced] (建议), 则可额外支持 BT 数据源。
可在设置中配置 qBittorrent 连接方式, 随后可在数据源设置中启用 BT 数据源。

PC 首次播放在线数据源时, 可能需要加载 10-30 秒。

| 操作系统                    | 全球                           | 中国大陆                            |
|-------------------------|------------------------------|---------------------------------|
| Windows x86_64          | [GitHub][github-win-x64]     | [Cloudflare 分流][cf-win-x64]     |
| macOS x86_64 (Intel 芯片) | [GitHub][github-mac-x64]     | [Cloudflare 分流][cf-mac-x64]     |
| macOS aarch64 (M 系列芯片)  | [GitHub][github-mac-aarch64] | [Cloudflare 分流][cf-mac-aarch64] |  
| Android APK aarch64     | [GitHub][github-android]     | [Cloudflare 分流][cf-android]     |

扫描二维码下载 Android 版本：

[github-android-qr]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}.apk.github.qrcode.png

[cf-android-qr]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}.apk.cloudflare.qrcode.png

| 全球                           | 中国大陆                            |
|------------------------------|---------------------------------|
| ![GitHub][github-android-qr] | ![Cloudflare 分流][cf-android-qr] |
