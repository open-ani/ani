
#ifndef GLOBAL_LOCK_H
#define GLOBAL_LOCK_H
#include <iostream>
#include <mutex>

namespace anilt {
extern std::recursive_mutex global_lock;

// #define guard_global_lock std::lock_guard guard(global_lock)
#define guard_global_lock (void *) 0

#define ENABLE_TRACE_LOGGING true

struct function_printer_t {
#if ENABLE_TRACE_LOGGING
    std::string name;
    explicit function_printer_t(const std::string &name) : name(name) {
        std::cerr << "Function " << name << " started" << std::endl;
    }
#else
    explicit function_printer_t(const std::string &_) {}
#endif

    ~function_printer_t() {
#if ENABLE_TRACE_LOGGING
        std::cerr << "Function " << name << " finished" << std::endl;
#endif
    }
};
} // namespace anilt

#endif // GLOBAL_LOCK_H
