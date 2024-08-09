package me.him188.ani.app.tools

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import me.him188.ani.utils.logging.logger

/**
 * DocumentContract from [android.provider.DocumentsContract] since [KitKat][android.os.Build.VERSION_CODES.KITKAT].
 *
 * Only keeps document contract uri to local storage parser.
 */
object DocumentsContractApi19 {
    private val logger = logger<DocumentsContract>()
    private const val AUTHORITY_DOCUMENT_EXTERNAL_STORAGE: String = "com.android.externalstorage.documents"
    private const val AUTHORITY_DOCUMENT_DOWNLOAD: String = "com.android.providers.downloads.documents"
    private const val AUTHORITY_DOCUMENT_MEDIA: String = "com.android.providers.media.documents"
    private const val PROVIDER_INTERFACE: String = "android.content.action.DOCUMENTS_PROVIDER"

    fun parseUriToStorage(context: Context, uri: Uri): String? {
        val documentId = try {
            DocumentsContract.getDocumentId(uri)
        } catch (_: Exception) {
            DocumentsContract.getTreeDocumentId(uri)
        }
        val self = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)

        val authority = self.authority
        if (AUTHORITY_DOCUMENT_EXTERNAL_STORAGE == authority) {
            // Get type and path
            val docId = DocumentsContract.getDocumentId(self)
            val split = docId.split(':')
            val type = split[0]
            val path = split[1]

            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + path
            } else {
                // Get the storage path
                var storageDir: String? = null
                for (cacheDir in context.externalCacheDirs) {
                    val cachePath = cacheDir.path
                    val index = cachePath.indexOf(type)
                    if (index >= 0) {
                        storageDir = cachePath.substring(0, index + type.length)
                    }
                }

                return if (storageDir != null) {
                    "$storageDir/$path"
                } else {
                    null
                }
            }
        } else if (AUTHORITY_DOCUMENT_DOWNLOAD == authority) {
            val id = DocumentsContract.getDocumentId(self)

            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), id.toLong(),
            )

            return queryForString(context, contentUri, MediaStore.MediaColumns.DATA)
        } else if (AUTHORITY_DOCUMENT_MEDIA == authority) {
            // Get type and id
            val docId = DocumentsContract.getDocumentId(self)
            val split = docId.split(':')
            val type = split[0]
            val id = split[1]
            val baseUri = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> {
                    logger.debug("Unknown type in $AUTHORITY_DOCUMENT_MEDIA: $type")
                    return null
                }
            }

            val contentUri = ContentUris.withAppendedId(baseUri, id.toLong())

            // Requires android.permission.READ_EXTERNAL_STORAGE or return null
            return queryForString(context, contentUri, MediaStore.MediaColumns.DATA)
        } else {
            return null
        }
    }

    private fun queryForString(context: Context, self: Uri, column: String, defaultValue: String? = null): String? {
        val resolver = context.contentResolver

        var c: Cursor? = null
        try {
            c = resolver.query(self, arrayOf(column), null, null, null)
            return if (c != null && c.moveToFirst() && !c.isNull(0)) {
                c.getString(0)
            } else {
                defaultValue
            }
        } catch (e: java.lang.Exception) {
            return defaultValue
        } finally {
            try {
                c?.close()
            } catch (_: Exception) {
            }
        }
    }
}