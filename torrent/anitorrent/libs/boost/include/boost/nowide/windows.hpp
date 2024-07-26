//
// Copyright (c) 2012 Artyom Beilis (Tonkikh)
// Copyright (c) 2022 Alexander Grund
//
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_NOWIDE_WINDOWS_HPP_INCLUDED
#define BOOST_NOWIDE_WINDOWS_HPP_INCLUDED

#ifdef BOOST_USE_WINDOWS_H
#include <windows.h>
// (Usually) included by windows.h
#include <shellapi.h>
#else

// When BOOST_USE_WINDOWS_H is not defined we declare the function prototypes to avoid including windows.h

extern "C" {

// From windows.h

__declspec(dllimport) wchar_t* __stdcall GetEnvironmentStringsW(void);
__declspec(dllimport) int __stdcall FreeEnvironmentStringsW(wchar_t*);
__declspec(dllimport) wchar_t* __stdcall GetCommandLineW(void);
__declspec(dllimport) unsigned long __stdcall GetLastError();
__declspec(dllimport) void* __stdcall LocalFree(void*);
__declspec(dllimport) int __stdcall SetEnvironmentVariableW(const wchar_t*, const wchar_t*);
__declspec(dllimport) unsigned long __stdcall GetEnvironmentVariableW(const wchar_t*, wchar_t*, unsigned long);

// From shellapi.h

__declspec(dllimport) wchar_t** __stdcall CommandLineToArgvW(const wchar_t*, int*);
}

#endif

#endif
