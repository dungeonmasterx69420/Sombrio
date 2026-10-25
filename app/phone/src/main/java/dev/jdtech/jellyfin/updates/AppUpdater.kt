package dev.jdtech.jellyfin.updates

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import dev.jdtech.jellyfin.BuildConfig
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** A newer build of Sombrio, published as a release on GitHub. */
data class AppUpdate(
    val buildNumber: Int,
    /** What changed, as written in the release. */
    val notes: String,
    val apkUrl: String,
    val apkSize: Long?,
)

/**
 * Updates Sombrio from the releases the Sombrio builds workflow publishes for each build of main,
 * as sombrAMP does: their tags are `build-<versionCode>` and they hold one APK per ABI, all signed
 * with the same key, so Android installs them over the running app.
 */
object AppUpdater {
    private const val REPOSITORY = "dungeonmasterx69420/Sombrio"
    private const val LATEST_RELEASE = "https://api.github.com/repos/$REPOSITORY/releases/latest"
    private val releaseTag = Regex("^build-(\\d+)$")

    /** Only builds numbered by CI (see .github/workflows/sombrio.yaml) are released. */
    val isSupported: Boolean
        get() = !BuildConfig.DEBUG && BuildConfig.VERSION_CODE >= 100

    /** Returns the latest release, if it's newer than the running app and has an APK for it. */
    suspend fun findUpdate(): AppUpdate? =
        withContext(Dispatchers.IO) {
            val connection = URL(LATEST_RELEASE).openConnection() as HttpURLConnection
            try {
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                when (val status = connection.responseCode) {
                    // Nothing has been released yet
                    HttpURLConnection.HTTP_NOT_FOUND -> null
                    HttpURLConnection.HTTP_OK ->
                        updateFromRelease(
                            JSONObject(connection.inputStream.bufferedReader().readText()),
                            BuildConfig.VERSION_CODE,
                        )
                    else -> throw IOException("GitHub answered with status $status")
                }
            } finally {
                connection.disconnect()
            }
        }

    /** Reads a release from GitHub's API, if it's newer than [currentBuildNumber]. */
    fun updateFromRelease(
        release: JSONObject,
        currentBuildNumber: Int,
        abis: List<String> = Build.SUPPORTED_ABIS.toList(),
    ): AppUpdate? {
        val buildNumber =
            releaseTag.find(release.optString("tag_name"))?.groupValues?.get(1)?.toIntOrNull()
                ?: return null
        if (buildNumber <= currentBuildNumber) return null
        val assets = release.optJSONArray("assets") ?: return null
        val apks =
            (0 until assets.length())
                .map { assets.getJSONObject(it) }
                .filter { it.optString("name").endsWith(".apk") }
        // The APK for the first ABI the device supports
        val apk =
            abis.firstNotNullOfOrNull { abi ->
                apks.firstOrNull { it.optString("name").endsWith("-$abi.apk") }
            } ?: return null
        return AppUpdate(
            buildNumber = buildNumber,
            notes = release.optString("body").trim(),
            apkUrl = apk.optString("browser_download_url"),
            apkSize = apk.optLong("size").takeIf { it > 0 },
        )
    }

    /**
     * Downloads [update] and opens Android's installer for it, which asks to allow installing apps
     * from Sombrio first if that hasn't been allowed yet. [onProgress] receives the downloaded
     * fraction, or null while it isn't known.
     */
    suspend fun install(context: Context, update: AppUpdate, onProgress: (Float?) -> Unit) {
        val apk =
            withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, "updates").apply { mkdirs() }
                directory.listFiles()?.forEach { it.delete() }
                val file = File(directory, "sombrio-build-${update.buildNumber}.apk")
                val connection = URL(update.apkUrl).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 15_000
                    connection.readTimeout = 30_000
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw IOException("GitHub answered with status ${connection.responseCode}")
                    }
                    val total =
                        connection.contentLengthLong.takeIf { it > 0 } ?: update.apkSize ?: -1
                    connection.inputStream.use { input ->
                        file.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var downloaded = 0L
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                downloaded += read
                                onProgress(if (total > 0) downloaded.toFloat() / total else null)
                            }
                        }
                    }
                } finally {
                    connection.disconnect()
                }
                file
            }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }
}
