@REM 这个文件会在 GitHub Actions 的 Windows runner 上执行

@REM C++
vcpkg install openssl:x64-windows
vcpkg install boost:x64-windows

@REM libtorrent
choco install swig -y
choco install cmake --installargs 'ADD_CMAKE_TO_PATH=System' -y
choco install ninja -y
