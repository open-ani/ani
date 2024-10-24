// IProxySettingsFlow.aidl
package me.him188.ani.app.domain.torrent;

import me.him188.ani.app.domain.torrent.parcel.PProxySettings;

// Declare any non-default types here with import statements

interface IProxySettingsFlow {
    void onEmit(in PProxySettings config);
}