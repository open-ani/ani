@REM 这个文件会在 GitHub Actions 的 Windows runner 上执行

vcpkg install boost-variant:x64-windows
vcpkg install boost-system:x64-windows
vcpkg install boost-range:x64-windows
vcpkg install boost-crc:x64-windows
vcpkg install boost-logic:x64-windows
vcpkg install boost-parameter:x64-windows
vcpkg install boost-asio:x64-windows
vcpkg install boost-variant2:x64-windows
vcpkg install boost-multi-index:x64-windows

choco install swig -y

vcpkg integrate install
