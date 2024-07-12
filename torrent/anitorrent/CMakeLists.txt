cmake_minimum_required(VERSION 3.25)
project(anitorrent)

set(CMAKE_CXX_STANDARD 17)


include(FetchContent)

# mmap 会导致它安装 signal handler, 而 JVM 依赖 signal handler 才能正常工作.
# 如果 JVM signal handler 被覆盖, JVM 就会因为 SIGBUS/SIGSEGV crash.
add_compile_definitions(TORRENT_HAVE_MMAP=0)

add_compile_definitions(TORRENT_ABI_VERSION=3)

# https://github.com/arvidn/libtorrent/blob/RC_2_0/docs/building.rst#building-with-vcpkg
if (WIN32)
    add_compile_definitions(TORRENT_DISABLE_ENCRYPTION=true)
    add_compile_definitions(TORRENT_USE_OPENSSL)
endif ()

#"D:\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.37.32822\bin\Hostx86\x64\cl.exe"  /nologo /TP -DBOOST_ALL_NO_LIB -DBOOST_ASIO_ENABLE_CANCELIO -DBOOST_ASIO_NO_DEPRECATED -DTORRENT_HAVE_MMAP=0 -DTORRENT_LINKING_SHARED -DTORRENT_USE_ASSERTS -DWIN32_LEAN_AND_MEAN -D_CRT_SECURE_NO_DEPRECATE -D_SCL_SECURE_NO_DEPRECATE -Danitorrent_EXPORTS -ID:\Java\jdk-18.0.1.102-openj9\include -ID:\Java\jdk-18.0.1.102-openj9\include\win32 -ID:\Projects\animation-garden\torrent\anitorrent\include -ID:\Projects\animation-garden\torrent\anitorrent\cmake-build-debug\_deps\libtorrent-src\include -external:ID:\vcpkg\installed\x64-windows\include -external:W0 /DWIN32 /D_WINDOWS /EHsc /Ob0 /Od /RTC1 -std:c++latest -MDd -Zi /EHsc /showIncludes /FoCMakeFiles\anitorrent.dir\gen\cpp\anitorrent_wrap.cpp.obj /FdCMakeFiles\anitorrent.dir\ /FS -c D:\Projects\animation-garden\torrent\anitorrent\gen\cpp\anitorrent_wrap.cpp
#Please define _WIN32_WINNT or _WIN32_WINDOWS appropriately. For example:
#- add -D_WIN32_WINNT=0x0601 to the compiler command line; or
#- add _WIN32_WINNT=0x0601 to your project's Preprocessor Definitions.
#Assuming _WIN32_WINNT=0x0601 (i.e. Windows 7 target).
if (WIN32)
    add_compile_definitions(_WIN32_WINNT=0x0A00)
endif ()

if (MSVC)
    message(STATUS "MSVC detected")
    add_compile_options(/Zc:__cplusplus)
endif ()

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
        gen/cpp/anitorrent_wrap.cpp # 如果你找不到这个文件, 在项目根目录跑一下 ./gradlew build
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

#add_executable(anitorrent_test
#        include/anitorrent.hpp
#        src/anitorrent.cpp
#        src/main.cpp
#        src/events.cpp
#        include/events.hpp
#        src/torrent_info_t.cpp
#        include/torrent_info_t.hpp
#        src/torrent_add_info_t.cpp
#        include/torrent_add_info_t.hpp
#        src/session_t.cpp
#        include/session_t.hpp
#        src/torrent_handle_t.cpp
#        include/torrent_handle_t.hpp
#        src/global_lock.cpp
#        include/global_lock.h
#)
#target_include_directories(anitorrent_test PRIVATE include)


target_link_libraries(anitorrent PRIVATE torrent-rasterbar ${JNI_LIBRARIES})
#target_link_libraries(anitorrent_test PRIVATE torrent-rasterbar ${JNI_LIBRARIES})
