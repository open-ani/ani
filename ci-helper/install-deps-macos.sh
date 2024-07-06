#!/bin/zsh

# 这个文件会在 GitHub Actions 的 macOS runner 上执行

# C++
brew install cmake -y
brew install ninja -y
brew install llvm -y

# libtorrent
brew install swig -y
brew install openssl -y
brew install boost -y
