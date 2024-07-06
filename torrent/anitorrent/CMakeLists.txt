cmake_minimum_required(VERSION 3.25)
project(anitorrent)

set(CMAKE_CXX_STANDARD 23)


include(FetchContent)

# mmap 会导致它安装 signal handler, 而 JVM 依赖 signal handler 才能正常工作.
# 如果 JVM signal handler 被覆盖, JVM 就会因为 SIGBUS/SIGSEGV crash.
add_compile_definitions(TORRENT_HAVE_MMAP=0)

# Libtorrent
FetchContent_Declare(
        libtorrent
        GIT_REPOSITORY https://github.com/arvidn/libtorrent.git
        GIT_TAG v2.0.10
)
FetchContent_MakeAvailable(libtorrent)

# JNI
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})


add_library(anitorrent
        SHARED
        include/anitorrent.hpp
        src/anitorrent.cpp
        gen/cpp/anitorrent_wrap.cpp
        src/events.cpp
        include/events.hpp
        src/torrent_info_t.cpp
        include/torrent_info_t.hpp
        src/torrent_add_info_t.cpp
        include/torrent_add_info_t.hpp
        src/session_t.cpp
        include/session_t.hpp
        src/torrent_handle_t.cpp
        include/torrent_handle_t.hpp
        src/global_lock.cpp
        include/global_lock.h
)
target_include_directories(anitorrent PRIVATE include)

add_executable(anitorrent_test
        include/anitorrent.hpp
        src/anitorrent.cpp
        src/main.cpp
        src/events.cpp
        include/events.hpp
        src/torrent_info_t.cpp
        include/torrent_info_t.hpp
        src/torrent_add_info_t.cpp
        include/torrent_add_info_t.hpp
        src/session_t.cpp
        include/session_t.hpp
        src/torrent_handle_t.cpp
        include/torrent_handle_t.hpp
        src/global_lock.cpp
        include/global_lock.h
)
target_include_directories(anitorrent_test PRIVATE include)


target_link_libraries(anitorrent PRIVATE torrent-rasterbar ${JNI_LIBRARIES})
target_link_libraries(anitorrent_test PRIVATE torrent-rasterbar ${JNI_LIBRARIES})
