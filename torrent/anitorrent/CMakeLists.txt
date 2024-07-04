cmake_minimum_required(VERSION 3.25)
project(anitorrent)

set(CMAKE_CXX_STANDARD 23)


include(FetchContent)

# Libtorrent
FetchContent_Declare(
        libtorrent
        GIT_REPOSITORY https://github.com/arvidn/libtorrent.git
        GIT_TAG v2.0.10 # You can specify a particular branch, tag, or commit here
)
FetchContent_MakeAvailable(libtorrent)

# JNI
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})


add_library(anitorrent
        SHARED
        include/anitorrent.h
        src/anitorrent.cpp
        gen/cpp/anitorrent_wrap.cpp
        src/events.cpp
        include/events.h
        src/torrent_info_t.cpp
        include/torrent_info_t.h
        src/torrent_add_info_t.cpp
        include/torrent_add_info_t.h
        src/session_t.cpp
        include/session_t.h
        src/torrent_handle_t.cpp
        include/torrent_handle_t.h
)
target_include_directories(anitorrent PRIVATE include)

add_executable(anitorrent_test
        include/anitorrent.h
        src/anitorrent.cpp
        src/main.cpp
        src/events.cpp
        include/events.h
        src/torrent_info_t.cpp
        include/torrent_info_t.h
        src/torrent_add_info_t.cpp
        include/torrent_add_info_t.h
        src/session_t.cpp
        include/session_t.h
        src/torrent_handle_t.cpp
        include/torrent_handle_t.h
)
target_include_directories(anitorrent_test PRIVATE include)


target_link_libraries(anitorrent PRIVATE torrent-rasterbar ${JNI_LIBRARIES})
target_link_libraries(anitorrent_test PRIVATE torrent-rasterbar ${JNI_LIBRARIES})
