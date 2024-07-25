cmake_minimum_required(VERSION 3.21) # Android recommends at least 3.21
project(anitorrent)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_STANDARD 17)

include(FetchContent)

# mmap 会导致它安装 signal handler, 而 JVM 依赖 signal handler 才能正常工作.
# 如果 JVM signal handler 被覆盖, JVM 就会因为 SIGBUS/SIGSEGV crash.
add_compile_definitions(TORRENT_HAVE_MMAP=0)
add_compile_definitions(TORRENT_ABI_VERSION=3) # Hide all deprecated APIs

# Android-specific settings
if (ANDROID)
    # https://developer.android.com/studio/projects/configure-cmake#call-cmake-cli
    set(ANDROID_STL c++_shared)
    set(CMAKE_SYSTEM_NAME Android)
    set(CMAKE_SYSTEM_VERSION 21) # Adjust as needed
    set(CMAKE_ANDROID_ARCH_ABI arm64-v8a)

    # Set Android NDK paths
    set(ANDROID_NDK ${ANDROID_NDK_HOME})
    set(CMAKE_ANDROID_NDK ${ANDROID_NDK})
    set(CMAKE_TOOLCHAIN_FILE ${ANDROID_NDK}/build/cmake/android.toolchain.cmake)
else ()
    if (WIN32)
        add_compile_definitions(TORRENT_DISABLE_ENCRYPTION=true)
        add_compile_definitions(TORRENT_USE_OPENSSL)
    endif ()

    if (WIN32)
        add_compile_definitions(_WIN32_WINNT=0x0A00)
    endif ()

    if (MSVC)
        message(STATUS "MSVC detected")
        add_compile_options(/Zc:__cplusplus)
        set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>DLL")
    endif ()
endif ()

# Fetch and build Boost for Android using CMake
if (ANDROID)
    FetchContent_Declare(
            boost
            GIT_REPOSITORY https://github.com/boostorg/boost.git
            GIT_TAG boost-1.85.0
            UPDATE_DISCONNECTED 1
    )
    FetchContent_MakeAvailable(boost)

    # List of Boost libraries to build
    set(BOOST_LIBS variant system range crc logic parameter asio variant2 multi-index multiprecision)

    foreach (lib ${BOOST_LIBS})
        include_directories(${boost_SOURCE_DIR}/libs/${lib}/include)
    endforeach ()

    set(Boost_INCLUDE_DIR ${boost_SOURCE_DIR}) #TODO

    set(BOOST_LIBRARIES "")
    foreach (lib ${BOOST_LIBS})
        list(APPEND BOOST_LIBRARIES boost_${lib})
    endforeach ()
endif ()

# Fetch and build OpenSSL for Android using CMake
if (ANDROID)
    FetchContent_Declare(
            openssl
            URL https://www.openssl.org/source/openssl-3.3.1.tar.gz
            DOWNLOAD_DIR ${CMAKE_SOURCE_DIR}/openssl_android
            SOURCE_DIR ${CMAKE_SOURCE_DIR}/openssl_android/src
            BINARY_DIR ${CMAKE_SOURCE_DIR}/openssl_android/build
    )
    FetchContent_MakeAvailable(openssl)

    # Manually configure and build OpenSSL
    ExternalProject_Add(openssl_project
            SOURCE_DIR ${openssl_SOURCE_DIR}
            CONFIGURE_COMMAND ${CMAKE_COMMAND} -DANDROID=1 -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} -DCMAKE_INSTALL_PREFIX=${OPENSSL_ROOT_DIR} -DCMAKE_POSITION_INDEPENDENT_CODE=ON ${openssl_SOURCE_DIR}
            BUILD_COMMAND ${CMAKE_COMMAND} --build ${openssl_BINARY_DIR}
            INSTALL_COMMAND ${CMAKE_COMMAND} --install ${openssl_BINARY_DIR}
    )
endif ()

# Fetch libtorrent
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

# Link libraries
if (ANDROID)
    add_dependencies(anitorrent openssl_project)
    target_link_libraries(anitorrent PRIVATE torrent-rasterbar ${JNI_LIBRARIES} ${BOOST_LIBRARIES} ${OPENSSL_CRYPTO_LIBRARY} ${OPENSSL_SSL_LIBRARY})
else ()
    target_link_libraries(anitorrent PRIVATE torrent-rasterbar ${JNI_LIBRARIES})
endif ()
