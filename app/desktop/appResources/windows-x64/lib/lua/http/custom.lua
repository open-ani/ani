-- make xgettext fetch strings from html code
function gettext(text) print(vlc.gettext._(text)) end

local _G = _G

local dialogs = setmetatable({}, {
__index = function(self, name)
    -- Cache the dialogs
    return rawget(self, name) or
           rawget(rawset(self, name, process(http_dir.."/dialogs/"..name)), name)
end})

_G.dialogs = function(...)
    for i=1, select("#",...) do
        dialogs[(select(i,...))]()
    end
end

_G.vlm = vlc.vlm()
