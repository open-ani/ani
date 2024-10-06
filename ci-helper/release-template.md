Automatically created from tag ${{ steps.tag.outputs.tag }}. Do not change anything until assets are
uploaded.

----

### 下载

<details>
<summary>>> <h3>点我展开下载列表</h3> <<</summary>

[//]: # (注意, `checkLatestVersion` 有字符串处理, 修改标题和分隔符前务必查询)

[github-win-x64]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-windows-x86_64.zip

[github-mac-x64]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-x86_64.dmg

[github-mac-aarch64]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-aarch64.dmg

[github-android]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-universal.apk

[github-android-arm64-v8a]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-arm64-v8a.apk

[github-android-armeabi-v7a]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-armeabi-v7a.apk

[github-android-x86_64]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-x86_64.apk

[cf-win-x64]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-windows-x86_64.zip

[cf-mac-x64]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-x86_64.dmg

[cf-mac-aarch64]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-macos-aarch64.dmg

[cf-android]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-universal.apk

[cf-android-arm64-v8a]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-arm64-v8a.apk

[cf-android-armeabi-v7a]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-armeabi-v7a.apk

[cf-android-x86_64]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-x86_64.apk

[ghproxy-win-x64]: https://mirror.ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fopen-ani%2Fani%2Freleases%2Fdownload%2F${{steps.tag.outputs.tag}}%2Fani-${{steps.tag-version.outputs.substring}}-windows-x86_64.zip

[ghproxy-mac-x64]: https://mirror.ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fopen-ani%2Fani%2Freleases%2Fdownload%2F${{steps.tag.outputs.tag}}%2Fani-${{steps.tag-version.outputs.substring}}-macos-x86_64.dmg

[ghproxy-mac-aarch64]: https://mirror.ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fopen-ani%2Fani%2Freleases%2Fdownload%2F${{steps.tag.outputs.tag}}%2Fani-${{steps.tag-version.outputs.substring}}-macos-aarch64.dmg

[ghproxy-android]: https://mirror.ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fopen-ani%2Fani%2Freleases%2Fdownload%2F${{steps.tag.outputs.tag}}%2Fani-${{steps.tag-version.outputs.substring}}-universal.apk

[ghproxy-android-arm64-v8a]: https://mirror.ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fopen-ani%2Fani%2Freleases%2Fdownload%2F${{steps.tag.outputs.tag}}%2Fani-${{steps.tag-version.outputs.substring}}-arm64-v8a.apk

[ghproxy-android-armeabi-v7a]: https://mirror.ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fopen-ani%2Fani%2Freleases%2Fdownload%2F${{steps.tag.outputs.tag}}%2Fani-${{steps.tag-version.outputs.substring}}-armeabi-v7a.apk

[ghproxy-android-x86_64]: https://mirror.ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fopen-ani%2Fani%2Freleases%2Fdownload%2F${{steps.tag.outputs.tag}}%2Fani-${{steps.tag-version.outputs.substring}}-x86_64.apk

[qb-enhanced]: https://github.com/c0re100/qBittorrent-Enhanced-Edition/releases/latest

- macOS
  安装后如果无法打开: [解决方案](https://github.com/open-ani/ani/wiki/macOS-%E6%97%A0%E6%B3%95%E6%89%93%E5%BC%80%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88)
- Windows
  打开时如果显示异常: [解决方案](https://github.com/open-ani/ani/wiki/Windows-%E4%B8%8B%E5%AD%97%E4%BD%93%E4%B8%8E%E8%83%8C%E6%99%AF%E9%A2%9C%E8%89%B2%E5%BC%82%E5%B8%B8)

| 操作系统                               | 全球                                   | 中国大陆                                                             | 
|------------------------------------|--------------------------------------|------------------------------------------------------------------|
| Windows x86_64                     | [GitHub][github-win-x64]             | [主线][cf-win-x64] / [备线][ghproxy-win-x64]                         |
| macOS x86_64 (Intel 芯片)            | [GitHub][github-mac-x64]             | [主线][cf-mac-x64] / [备线][ghproxy-mac-x64]                         |
| macOS aarch64 (M 系列芯片)             | [GitHub][github-mac-aarch64]         | [主线][cf-mac-aarch64] / [备线][ghproxy-mac-aarch64]                 |
| Android universal (适用于所有设备)        | [GitHub][github-android]             | [主线][cf-android] / [备线][ghproxy-android]                         |
| Android arm64-v8a (仅新手机)           | [GitHub][github-android-arm64-v8a]   | [主线][cf-android-arm64-v8a] / [备线][ghproxy-android-arm64-v8a]     |
| Android armeabi-v7a (仅 32 位旧手机和电视) | [GitHub][github-android-armeabi-v7a] | [主线][cf-android-armeabi-v7a] / [备线][ghproxy-android-armeabi-v7a] |
| Android x86_64 (Chromebook 及模拟器)   | [GitHub][github-android-x86_64]      | [主线][cf-android-x86_64] / [备线][ghproxy-android-x86_64]           |

也可以扫描二维码下载 Android 版本 (适用于所有设备)：

[github-android-qr]: https://github.com/open-ani/ani/releases/download/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-universal.apk.github.qrcode.png

[cf-android-qr]: https://d.myani.org/${{steps.tag.outputs.tag}}/ani-${{steps.tag-version.outputs.substring}}-universal.apk.cloudflare.qrcode.png

| 全球                           | 中国大陆                         |
|------------------------------|------------------------------|
| ![GitHub][github-android-qr] | ![Cloudflare][cf-android-qr] |

</details>
