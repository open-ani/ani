
#ifndef GLOBAL_LOCK_H
#define GLOBAL_LOCK_H
#include <iomanip>
#include <iostream>
#include <mutex>
#include <sstream>

namespace anilt {
extern std::recursive_mutex global_lock;

// #define guard_global_lock std::lock_guard guard(global_lock)
#define guard_global_lock (void *) 0

#define ENABLE_TRACE_LOGGING false

struct function_printer_t {
#if ENABLE_TRACE_LOGGING

    static std::string getCurrentTimeFormatted() {
        // Get current time
        auto now = std::chrono::system_clock::now();
        auto in_time_t = std::chrono::system_clock::to_time_t(now);

        // Format the time to a struct tm
        std::tm buf{};
#if defined(_MSC_VER) || defined(__MINGW32__)
        localtime_s(&buf, &in_time_t);
#else
        localtime_r(&in_time_t, &buf);
#endif

        // Create a stringstream to format the output
        std::stringstream ss;
        ss << std::put_time(&buf, "%Y-%m-%d %H:%M:%S");
        ss << std::flush;

        return ss.str();
    }

    std::string name;
    explicit function_printer_t(const std::string &name) : name(name) {
        std::cout << getCurrentTimeFormatted << " Function " << name << " started" << std::endl;
    }
#else
    explicit function_printer_t(const std::string &_) {}
#endif

    ~function_printer_t() {
#if ENABLE_TRACE_LOGGING
        std::cout << getCurrentTimeFormatted << " Function " << name << " finished" << std::endl;
#endif
    }
};
} // namespace anilt

#endif // GLOBAL_LOCK_H
