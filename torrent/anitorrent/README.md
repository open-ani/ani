# Anitorrent

Libtorrent 的 wrapper. C++ 原生调用 libtorrent 实现 bt 功能, 通过 SWIG 生成 JNI 接口供 Kotlin 对接.

让与 libtorrent 的交互工作都在 C++ 实现, 避免 JNI 交互可能导致的内存所有权等问题. 直接交互也拥有最高的自由度.

## 安装依赖

Anitorrent 使用 CMake 构建. 推荐配合 Ninja.

### macOS

1. 安装 Homebrew
2. 执行项目根目录 `/ci-helper/install-deps-macos.sh`.
   这将会调用 brew 安装 CMake, Ninja, SWIG, Boost, OpenSSL. libtorrent 将会在构建 anitorrent 时现场构建.

### Windows

1. 安装 Visual Studio
2. 安装 Chocolatey
3. 执行项目根目录 `/ci-helper/install-deps-windows.cmd`

Windows 下可能无法自动寻找编译器. 可通过 `-DCMAKE_C_COMPILER`, `-DCMAKE_CXX_COMPILER` 指定编译器路径.

### 准备编译器

Anitorrent 日常使用如下工具链构建:

- macOS AppleClang 14
- macOS Clang 18
- Windows MSVC

GCC 没有测试过.

macOS 支持 aarch64 和 x86_64. Windows 仅测试过 x86_64.

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
