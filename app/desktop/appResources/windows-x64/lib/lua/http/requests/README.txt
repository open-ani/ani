
This file describes commands available through the requests/ file:

Lines starting with < describe what the page sends back
Lines starting with > describe what you can send to the page

All parameters need to be URL encoded.
Examples:
 # -> %23
 % -> %25
 + -> %2B
 space -> +
 ...


Deprecation Notice:
---
The entire interface is moving to using <MRL> for input and output parameters and attributes
pl_play and in_enqueue previously accepted paths. This is still supported, but from 1.3 <MRL> will be required
where path attributes are provided in output, these should be ignored in favour of uri attributes
path support is scheduled to be removed entirely from 1.3
---

<root> (/)
===========


> Get album art for current input:
  /art  (NB: not /requests/art)

> Get album art for any playlist input (available from API version 3):
  /art?item=123  (NB: not /requests/art)


status.xml or status.json
===========


< Get VLC status information, current item info and meta.
< Get VLC version, and http api version

> add <uri> to playlist and start playback:
  ?command=in_play&input=<uri>&option=<option>
  the option field is optional, and can have the values:
    noaudio
    novideo

> add <uri> to playlist:
  ?command=in_enqueue&input=<uri>

> add subtitle to currently playing file
  ?command=addsubtitle&val=<uri>

> play playlist item <id>. If <id> is omitted, play last active item:
  ?command=pl_play&id=<id>

> toggle pause. If current state was 'stop', play item <id>, if no <id> specified, play current item. If no current item, play 1st item in the playlist:
  ?command=pl_pause&id=<id>

> resume playback if paused, else do nothing
  ?command=pl_forceresume

> pause playback, do nothing if already paused
  ?command=pl_forcepause

> stop playback:
  ?command=pl_stop

> jump to next item:
  ?command=pl_next

> jump to previous item:
  ?command=pl_previous

> delete item <id> from playlist:
  ?command=pl_delete&id=<id>
  NOTA BENE: pl_delete is completly UNSUPPORTED

> empty playlist:
  ?command=pl_empty

> set audio delay
  ?command=audiodelay&val=<delayinseconds>

> set subtitle delay
  ?command=subdelay&val=<delayinseconds>

> set playback rate. must be > 0
  ?command=rate&val=<newplaybackrate>

> set aspect ratio. Must be one of the following values. Any other value will reset aspect ratio to default
  ?command=aspectratio&val=<newratio>
  Valid aspect ratio values: 1:1 , 4:3 , 5:4 , 16:9 , 16:10 , 221:100 , 235:100 , 239:100

> sort playlist using sort mode <val> and order <id>:
  ?command=pl_sort&id=<id>&val=<val>
  If id=0 then items will be sorted in normal order, if id=1 they will be
  sorted in reverse order
  A non exhaustive list of sort modes:
    0 Id
    1 Name
    3 Author
    5 Random
    7 Track number

> toggle random playback:
  ?command=pl_random

> toggle loop:
  ?command=pl_loop

> toggle repeat:
  ?command=pl_repeat

> toggle enable service discovery module <val>:
  ?command=pl_sd&val=<val>
  Typical values are:
    sap
    shoutcast
    podcast
    hal

> toggle fullscreen:
  ?command=fullscreen

> set volume level to <val> (can be absolute integer, percent or +/- relative value):
  ?command=volume&val=<val>
  Allowed values are of the form:
    +<int>, -<int>, <int> or <int>%

> seek to <val>:
  ?command=seek&val=<val>
  Allowed values are of the form:
    [+ or -][<int><H or h>:][<int><M or m or '>:][<int><nothing or S or s or ">]
    or [+ or -]<int>%
    (value between [ ] are optional, value between < > are mandatory)
  examples:
    1000 -> seek to the 1000th second
    +1H:2M -> seek 1 hour and 2 minutes forward
    -10% -> seek 10% back

>command=preamp&val=<val in dB>
 sets the preamp value, must be >=-20 and <=20

>command=equalizer&band=<band>&val=<gain in dB, must be >=-20 and <=20)
 set the gain for a specific band

>command=enableeq&val=<0 or 1>
 0 --  disables the equalizer
 1 --  enables the equalizer

>command=setpreset&val=<presetid>
 set the equalizer preset as per the id specified

<Displays the equalizer band gains.
Band 0: 60 Hz, 1: 170 Hz, 2: 310 Hz, 3: 600 Hz, 4: 1 kHz,
5: 3 kHz, 6: 6 kHz, 7: 12 kHz , 8: 14 kHz , 9: 16 kHz

<Display the list of presets available for the equalizer

---
Commands available from API version 2
---

> select the title
  ?command=title&val=<val>

> select the chapter
  ?command=title&val=<val>

> select the audio track (use the number from the stream)
  ?command=audio_track&val=<val>

> select the video track (use the number from the stream)
  ?command=video_track&val=<val>

> select the sibtitle track (use the number from the stream)
  ?command=subtitle_track&val=<val>

playlist.xml or playlist.json:
=============
< get the full playlist tree

NB: playlist_jstree.xml is used for the internal web client. It should not be relied upon by external remotes.
It may be removed without notice.

browse.xml or browse.json:
===========

< ?dir=<uri>
> get file list from uri. At the moment, only local file uris are supported

NB: uri is the preferred parameter. Dir is deprecated and may be removed in a future release.
< ?dir=<dir>
> get <dir>'s filelist

vlm.xml:
========
< get the full list of VLM elements

vlm_cmd.xml:
============
< execute VLM command <cmd>
  ?command=<cmd>
> get the error message from <cmd>

