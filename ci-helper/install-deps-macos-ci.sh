#!/bin/zsh

# 这个文件会在 GitHub Actions 的 macOS runner 上执行

# C++
brew install cmake
brew install ninja

# libtorrent
brew install swig
brew install boost-variant boost-system boost-range boost-crc boost-logic boost-parameter boost-asio boost-variant2 boost-multi-index boost-multiprecision
