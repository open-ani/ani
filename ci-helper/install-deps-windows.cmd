@REM 这个文件会在 GitHub Actions 的 Windows runner 上执行

vcpkg install boost:x64-windows

choco install swig -y

vcpkg integrate install