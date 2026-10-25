package wtf.sombra.sombrio.music

import android.app.UiModeManager
import android.content.Intent
import android.content.Intent.CATEGORY_APP_MUSIC
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.INTENT_ACTION_MUSIC_PLAYER
import android.provider.Settings
import android.system.ErrnoException
import android.system.Os
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.mediarouter.media.MediaRouter
import com.ryanheise.audioservice.AudioServiceActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayInputStream
import java.io.File
import java.security.KeyStore
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * The Music tab: sombrAMP, Sombra's music player, running on the Flutter engine audio_service keeps
 * alive while music plays.
 *
 * The channels are the ones sombrAMP's own Android app sets up in its MainActivity, plus
 * [SombrioHostChannel], through which sombrAMP signs in to the account Sombrio is signed in to.
 */
class MusicActivity : AudioServiceActivity() {
    companion object {
        private const val CLIENT_CERT_CHANNEL = "com.unicornsonlsd.finamp/client_certificate"
        private const val DOWNLOADS_SERVICE_CHANNEL = "com.unicornsonlsd.finamp/downloads_service"
        private const val OUTPUT_SWITCHER_CHANNEL = "com.unicornsonlsd.finamp/output_switcher"
        private const val SET_NATIVE_THEME_CHANNEL = "com.unicornsonlsd.finamp/set_native_theme"
    }

    private lateinit var mediaRouter: MediaRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        updateIntent(intent)
        super.onCreate(savedInstanceState)

        mediaRouter = MediaRouter.getInstance(this)
    }

    override fun onNewIntent(intent: Intent) {
        updateIntent(intent)
        super.onNewIntent(intent)
    }

    /** Asked to play music without saying what, sombrAMP plays a surprise mix. */
    private fun updateIntent(intent: Intent) {
        if (
            (intent.action == INTENT_ACTION_MUSIC_PLAYER || intent.action == CATEGORY_APP_MUSIC) &&
                intent.data == null
        ) {
            intent.data = "finamp://play/surprisemix".toUri()
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val messenger = flutterEngine.dartExecutor.binaryMessenger

        SombrioHostChannel(applicationContext, lifecycleScope).register(messenger)

        MethodChannel(messenger, CLIENT_CERT_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "installClientCertificate" -> {
                    val bytes = call.argument<ByteArray>("bytes")
                    val password = call.argument<String>("password")
                    if (bytes == null || password == null) {
                        result.error("INVALID_ARGS", "bytes and password are required", null)
                        return@setMethodCallHandler
                    }
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                installClientCertificate(bytes, password)
                            }
                            result.success(null)
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to install client certificate")
                            result.error("CERT_ERROR", e.message, null)
                        }
                    }
                }
                "clearClientCertificate" -> {
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) { clearClientCertificate() }
                            result.success(null)
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to clear client certificate")
                            result.error("CERT_ERROR", e.message, null)
                        }
                    }
                }
                else -> result.notImplemented()
            }
        }

        MethodChannel(messenger, DOWNLOADS_SERVICE_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "fixDownloadsFileOwner" -> {
                    val downloadLocations =
                        call.argument<List<String>?>("download_locations").orEmpty()
                    lifecycleScope.launch(Dispatchers.IO) {
                        fixDownloadsFileOwner(downloadLocations)
                    }
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        MethodChannel(messenger, SET_NATIVE_THEME_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "setNativeThemeMode" -> {
                    // Only API 31 and up can set the night mode of one app.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        result.success(null)
                        return@setMethodCallHandler
                    }
                    val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
                    val mode =
                        when (call.argument<Int?>("targetMode")) {
                            0 -> UiModeManager.MODE_NIGHT_AUTO
                            1 -> UiModeManager.MODE_NIGHT_NO
                            2 -> UiModeManager.MODE_NIGHT_YES
                            else -> null
                        }
                    if (mode == null) {
                        result.notImplemented()
                    } else {
                        uiModeManager.setApplicationNightMode(mode)
                        result.success(null)
                    }
                }
                else -> result.notImplemented()
            }
        }

        MethodChannel(messenger, OUTPUT_SWITCHER_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "showOutputSwitcherDialog" -> {
                    SystemOutputSwitcherDialogController.showDialog(this)
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
                                "extras" to route.extras,
                                "iconUri" to route.iconUri,
                            )
                        }
                    )
                }
                "setOutputToDeviceSpeaker" -> {
                    mediaRouter.selectRoute(mediaRouter.routes.first { it.isDeviceSpeaker })
                    result.success(null)
                }
                "setOutputToBluetoothDevice" -> {
                    mediaRouter.selectRoute(mediaRouter.routes.first { it.isBluetooth })
                    result.success(null)
                }
                "setOutputToRouteByName" -> {
                    val name = call.argument<String>("name")
                    mediaRouter.selectRoute(mediaRouter.routes.first { it.name == name })
                    result.success(null)
                }
                "openBluetoothSettings" -> {
                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun installClientCertificate(bytes: ByteArray, password: String) {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(ByteArrayInputStream(bytes), password.toCharArray())

        val keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
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
     * Gives downloaded tracks to the app's own group. Older versions of Finamp left them in the
     * cache group, which made Android count them as cache.
     */
    @WorkerThread
    private fun fixDownloadsFileOwner(downloadLocations: List<String>) {
        val appUid = applicationInfo.uid
        val cacheGid =
            try {
                Os.stat(cacheDir.absolutePath).st_gid
            } catch (e: ErrnoException) {
                Timber.e(e, "Failed to get cache directory GID")
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
                    Timber.e(e, "Failed to fix owner for: ${file.absolutePath}")
                }
            }
        }
    }
}
