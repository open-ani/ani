# 这个文件会在 GitHub Actions 的 Windows runner 上执行

# C++
vcpkg install openssl:x64-windows
vcpkg install boost
vcpkg install cmake
vcpkg install ninja
# libtorrent
choco install swig
