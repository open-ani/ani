package me.him188.ani.app.data.source

import java.net.URI

interface AssetLoader {
    fun loadAsUri(path: String): URI?
}