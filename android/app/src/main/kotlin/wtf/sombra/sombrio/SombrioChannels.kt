package wtf.sombra.sombrio

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import androidx.core.content.FileProvider
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.mediarouter.media.MediaRouter
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayInputStream
import java.io.File
import java.security.KeyStore
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import kotlin.concurrent.thread

/**
 * The platform channels Sombrio needs beyond Moonfin's: the ones sombrAMP's own Android app sets up in its
 * MainActivity (music/sombrAMP/android/app/src/main/kotlin/com/unicornsonlsd/finamp/MainActivity.kt), for the Music
 * tab, and installing Sombrio's updates (lib/sombrio/sombrio_updates.dart).
 *
 * sombrAMP's theme channel isn't here: inside Sombrio, sombrAMP doesn't set the app's night mode.
 */
object SombrioChannels {
    private const val TAG = "SombrioChannels"

    private const val CLIENT_CERT_CHANNEL = "com.unicornsonlsd.finamp/client_certificate"
    private const val DOWNLOADS_SERVICE_CHANNEL = "com.unicornsonlsd.finamp/downloads_service"
    private const val OUTPUT_SWITCHER_CHANNEL = "com.unicornsonlsd.finamp/output_switcher"
    private const val UPDATES_CHANNEL = "wtf.sombra.sombrio/updates"

    fun register(activity: Activity, messenger: BinaryMessenger) {
        val mainThread = Handler(Looper.getMainLooper())
        val mediaRouter = MediaRouter.getInstance(activity)

        MethodChannel(messenger, CLIENT_CERT_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "installClientCertificate" -> {
                    val bytes = call.argument<ByteArray>("bytes")
                    val password = call.argument<String>("password")
                    if (bytes == null || password == null) {
                        result.error("INVALID_ARGS", "bytes and password are required", null)
                        return@setMethodCallHandler
                    }
                    thread {
                        try {
                            installClientCertificate(bytes, password)
                            mainThread.post { result.success(null) }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to install client certificate", e)
                            mainThread.post { result.error("CERT_ERROR", e.message, null) }
                        }
                    }
                }
                "clearClientCertificate" -> {
                    thread {
                        try {
                            clearClientCertificate()
                            mainThread.post { result.success(null) }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to clear client certificate", e)
                            mainThread.post { result.error("CERT_ERROR", e.message, null) }
                        }
                    }
                }
                else -> result.notImplemented()
            }
        }

        MethodChannel(messenger, DOWNLOADS_SERVICE_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "fixDownloadsFileOwner" -> {
                    val downloadLocations = call.argument<List<String>?>("download_locations").orEmpty()
                    val appUid = activity.applicationInfo.uid
                    val cacheDir = activity.cacheDir
                    thread { fixDownloadsFileOwner(downloadLocations, appUid, cacheDir) }
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        MethodChannel(messenger, OUTPUT_SWITCHER_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "showOutputSwitcherDialog" -> {
                    SystemOutputSwitcherDialogController.showDialog(activity)
                    result.success(null)
                }
                "getRoutes" -> {
                    result.success(
                        mediaRouter.routes.map { route ->
                            mapOf(
                                "name" to route.name,
                                "connectionState" to route.connectionState,
                                "isSystemRoute" to route.isSystemRoute,
                                "isDefault" to route.isDefault,
                                "isDeviceSpeaker" to route.isDeviceSpeaker,
                                "isBluetooth" to route.isBluetooth,
                                "volume" to route.volume,
                                "providerPackageName" to route.provider.packageName,
                                "isSelected" to route.isSelected,
                                "deviceType" to route.deviceType,
                                "description" to route.description,
                                "iconUri" to route.iconUri?.toString(),
                            )
                        }
                    )
                }
                "setOutputToDeviceSpeaker" -> {
                    mediaRouter.routes.firstOrNull { it.isDeviceSpeaker }?.let { mediaRouter.selectRoute(it) }
                    result.success(null)
                }
                "setOutputToBluetoothDevice" -> {
                    mediaRouter.routes.firstOrNull { it.isBluetooth }?.let { mediaRouter.selectRoute(it) }
                    result.success(null)
                }
                "setOutputToRouteByName" -> {
                    val name = call.argument<String>("name")
                    mediaRouter.routes.firstOrNull { it.name == name }?.let { mediaRouter.selectRoute(it) }
                    result.success(null)
                }
                "openBluetoothSettings" -> {
                    activity.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        MethodChannel(messenger, UPDATES_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "installApk" -> {
                    val path = call.argument<String>("path")
                    if (path == null) {
                        result.error("INVALID_ARGS", "path is required", null)
                        return@setMethodCallHandler
                    }
                    try {
                        installApk(activity, File(path))
                        result.success(null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Couldn't open the installer", e)
                        result.error("INSTALL_ERROR", e.message, null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    /**
     * Opens Android's installer for a downloaded update. The APK is shared through [UpdateFileProvider], and Android
     * asks to allow installing apps from Sombrio first, if that hasn't been allowed yet.
     */
    private fun installApk(activity: Activity, apk: File) {
        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.updates", apk)
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        activity.startActivity(intent)
    }

    private fun installClientCertificate(bytes: ByteArray, password: String) {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(ByteArrayInputStream(bytes), password.toCharArray())

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, password.toCharArray())

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, null, null)

        SSLContext.setDefault(sslContext)
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
    }

    private fun clearClientCertificate() {
        val defaultContext = SSLContext.getInstance("TLS")
        defaultContext.init(null, null, null)
        SSLContext.setDefault(defaultContext)
        HttpsURLConnection.setDefaultSSLSocketFactory(defaultContext.socketFactory)
    }

    /**
     * Gives downloaded tracks to the app's own group. Older versions of Finamp left them in the cache group, which made
     * Android count them as cache.
     */
    private fun fixDownloadsFileOwner(downloadLocations: List<String>, appUid: Int, cacheDir: File) {
        val cacheGid =
            try {
                Os.stat(cacheDir.absolutePath).st_gid
            } catch (e: ErrnoException) {
                Log.e(TAG, "Failed to get cache directory GID", e)
                return
            }
        for (downloadLocation in downloadLocations) {
            val downloadDirectory = File(downloadLocation)
            if (!downloadDirectory.isDirectory) continue

            for (file in downloadDirectory.walkTopDown()) {
                try {
                    if (!file.isFile) continue
                    if (Os.stat(file.absolutePath).st_gid != cacheGid) continue
                    // A uid of -1 keeps the owner.
                    Os.chown(file.absolutePath, -1, appUid)
                } catch (e: ErrnoException) {
                    Log.e(TAG, "Failed to fix owner for: ${file.absolutePath}", e)
                }
            }
        }
    }
}
