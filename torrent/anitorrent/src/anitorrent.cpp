#include "anitorrent.hpp"

#include "libtorrent/version.hpp"


// #include "libtorrent/session.hpp"

namespace anilt {
std::string lt_version() { return libtorrent::version(); }
} // namespace anilt
