%module anitorrent
%{
#include "anitorrent.h"
#include "events.h"
#include "session_t.h"
#include "torrent_add_info_t.h"
#include "torrent_handle_t.h"
#include "torrent_info_t.h"

%}

%include <std_string.i>  // Include support for std::string
%include <std_vector.i>  // Include support for std::vector

%feature("director") event_listener_t;

%include stdint.i
%include "include/torrent_info_t.h"
%include "include/torrent_add_info_t.h"
%include "include/torrent_handle_t.h"
%include "include/events.h"
%include "include/session_t.h"
%include "include/anitorrent.h"

