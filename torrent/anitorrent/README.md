# Anitorrent

Libtorrent 的 wrapper. C++ 原生调用 libtorrent 实现 bt 功能, 通过 SWIG 生成 JNI 接口供 Kotlin 对接.

让与 libtorrent 的交互工作都在 C++ 实现, 避免 JNI 交互可能导致的内存所有权等问题. 直接交互也拥有最高的自由度.

## 安装依赖

Anitorrent 使用 CMake 构建. 推荐配合 Ninja.

### macOS

1. 安装 Homebrew, 即 `brew`. 官方安装命令:
   ```shell
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
2. 执行安装脚本 [`/ci-helper/install-deps-macos.sh`](/ci-helper/install-deps-macos.sh).
   这将会调用 brew 安装 CMake, Ninja, SWIG, Boost, OpenSSL. libtorrent 将会在构建 anitorrent 时现场构建.
3. 如果你系统没有安装任意种类的大于 17 版本的 JDK, 可以通过 brew 安装:
   ```shell
   brew install openjdk@17
   ```

### Windows

1. 安装 Visual Studio (不是 Visual Studio Code)
2. 安装 [Vcpkg](https://github.com/microsoft/vcpkg):
    ```shell
   git clone https://github.com/microsoft/vcpkg.git
   cd vcpkg && bootstrap-vcpkg.bat
    ```
3. 安装 [Chocolatey](https://chocolatey.org/install), 以下为示例 CMD 命令, 不一定可用:
   ```shell
   @powershell -NoProfile -ExecutionPolicy Bypass -Command "[System.Net.WebRequest]::DefaultWebProxy.Credentials = [System.Net.CredentialCache]::DefaultCredentials; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))" && SET PATH="%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
   ```
4. 确保 `vcpkg` 和 `choco` 可以直接在命令行使用, 否则尝试重启系统
5.
执行项目根目录的依赖安装脚本 [`/ci-helper/install-deps-windows.cmd`](/ci-helper/install-deps-windows.cmd)
6. 如果你系统没有安装任意种类的大于 17 版本的 JDK (且在 PATH 可见), 可以通过 choco 安装:
   ```shell
   choco install openjdk
   ```

Windows 下可能无法自动寻找编译器. 可通过 `-DCMAKE_C_COMPILER`, `-DCMAKE_CXX_COMPILER` 指定编译器路径.

#### Windows 使用提示

- 使用 x64/amd64 编译器.
  例如: `D:\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.37.32822\bin\Hostx64\x64`. 如果是使用
  CLion, 可在设置中 Toolchain 将 Architecture 设置为 `amd64`.
- 如果提示找不到 `Boost_INCLUDE_DIR`, 可在 CMake 命令行中指定你的 vcpkg 的 include 路径,
  例如: `-DBoost_INCLUDE_DIR=D:\vcpkg\installed\x64-windows\include\`

#### 测试过的工具链

Anitorrent 日常使用如下工具链构建测试:

- macOS AppleClang 14 (Xcode)
- macOS LLVM Clang 18
- Windows MSVC 14 (Visual Studio)

GCC 与 Linux 未经测试.

macOS 支持 aarch64 和 x86_64. Windows 仅支持 x86_64.

## 构建

所有构建工作在 Gradle 自动完成. 有以下几个 task:

- `generateSwig`: 生成 SWIG JNI 接口
- `configureAnitorrent`: 生成 CMake 构建配置
- `buildAnitorrent`: 构建 `libanitorrent.dylib` / `anitorrent.dll`

以上工作均不需要手动执行. 在运行 desktop 时将会自动构建并复制到 `appResources` 目录, 详见
task `:app:desktop:copyAnitorrentDylibToResources`.

也就是说, 你正常运行 IDE `Run Desktop` 或 Kotlin main 函数就行.

### 构建产物

- macOS: `build-ci/libanitorrent.dylib`
- Windows: `build-ci/anitorrent.dll`
