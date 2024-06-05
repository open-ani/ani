package me.him188.ani.app.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import me.him188.ani.BuildConfig
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import java.io.File


class AndroidUpdateInstaller : UpdateInstaller {
    private companion object {
        private val logger = logger<AndroidUpdateInstaller>()
    }

    override fun install(file: File, context: ContextMP): InstallationResult {
        logger.info { "Requesting install APK" }
        if (!context.packageManager.canRequestPackageInstalls()) {
            // Request permission from the user
            kotlin.runCatching {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setData(Uri.parse(String.format("package:%s", context.packageName)))
                startActivity(context, intent, null)
            }.onFailure {
                logger.warn(it) { "Failed to request permission to install APK" }
            }
        } else {
            kotlin.runCatching {
                installApk(context, file)
            }.onFailure {
                logger.warn(it) { "Failed to install update APK using installApkLegacy" }
            }
        }
        return InstallationResult.Succeed
    }


    // Function to install APK
    private fun installApk(
        context: Context,
        file: File,
    ) {
        val intent = Intent(Intent.ACTION_VIEW)
//        val externalFile = Environment.getExternalStorageDirectory().resolve("Download/api-update.apk")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            MediaStore.createWriteRequest(
//                context.contentResolver,
//                listOf(Uri.fromFile(externalFile)),
//            ).apply {
//                startActivity(context, intent, null)
//            }
//        }
//        file.copyTo(externalFile)
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
        val apkUri = FileProvider.getUriForFile(context, "${BuildConfig.APP_APPLICATION_ID}.fileprovider", file)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resInfoList: List<ResolveInfo> =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                apkUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        startActivity(context, intent, null)
    }
}
