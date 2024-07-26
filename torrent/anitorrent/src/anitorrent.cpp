#include "anitorrent.hpp"

#include <iostream>

// used by boost stacktrace

// macos or linux
#if (defined(__APPLE__) && defined(__MACH__)) || defined(__linux__)
#define ANI_USE_BACKTRACE
#endif

#ifdef ANI_USE_BACKTRACE
#define _GNU_SOURCE // NOLINT(*-reserved-identifier)
#include <execinfo.h> // This is not available on windows
#include "boost/stacktrace/stacktrace.hpp"
#endif

#include "libtorrent/version.hpp"


// #include "libtorrent/session.hpp"

namespace anilt {
std::string lt_version() { return libtorrent::version(); }

static void signal_handler([[maybe_unused]] int sig) {
#ifdef ANI_USE_BACKTRACE
    void *buffer[100];

    const int nptrs = backtrace(buffer, 100);

    std::cerr << "Error: signal " << sig << std::endl << std::flush;

    /* The call backtrace_symbols_fd(buffer, nptrs, STDOUT_FILENO)
       would produce similar output to the following: */

    char **strings = backtrace_symbols(buffer, nptrs);
    if (strings == nullptr) {
        perror("backtrace_symbols");
        exit(EXIT_FAILURE);
    }

    for (int j = 0; j < nptrs; j++) {
        std::cerr << strings[j] << std::endl << std::flush;
    }
    free(strings);
    exit(sig);
#endif
}
void install_signal_handlers() {
#ifdef ANI_USE_BACKTRACE
    signal(SIGSEGV, signal_handler);
    signal(SIGBUS, signal_handler);
#endif
}
} // namespace anilt
