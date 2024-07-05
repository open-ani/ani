%module(directors="1") anitorrent
%{
#include "anitorrent.hpp"
#include "events.hpp"
#include "session_t.hpp"
#include "torrent_add_info_t.hpp"
#include "torrent_handle_t.hpp"
#include "torrent_info_t.hpp"

%}

%include <std_string.i>  // Include support for std::string
%include <std_vector.i>  // Include support for std::vector

%feature("director") event_listener_t;

%include stdint.i
%include "include/torrent_info_t.hpp"
%include "include/torrent_add_info_t.hpp"
%include "include/torrent_handle_t.hpp"
%include "include/events.hpp"
%include "include/session_t.hpp"
%include "include/anitorrent.hpp"

