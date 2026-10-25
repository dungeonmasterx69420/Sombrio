package wtf.sombra.sombrio.music

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.jdtech.jellyfin.api.JellyfinApi
import dev.jdtech.jellyfin.database.ServerDatabaseDao
import dev.jdtech.jellyfin.settings.domain.AppPreferences
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * What sombrAMP asks of Sombrio (see sombrAMP's lib/sombra/sombrio_host.dart): the account Sombrio
 * is signed in to, and authorizing sombrAMP's Quick Connect request with it, so the music player
 * signs in to the same account without asking for the password.
 */
class SombrioHostChannel(context: Context, private val scope: CoroutineScope) {
    companion object {
        const val NAME = "wtf.sombra.sombrio/host"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun appPreferences(): AppPreferences

        fun database(): ServerDatabaseDao

        fun jellyfinApi(): JellyfinApi
    }

    private val dependencies =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            Dependencies::class.java,
        )

    fun register(messenger: BinaryMessenger) {
        MethodChannel(messenger, NAME).setMethodCallHandler { call, result ->
            when (call.method) {
                "getAccount" ->
                    scope.launch {
                        try {
                            result.success(account())
                        } catch (e: Exception) {
                            Timber.e(e, "Couldn't read the current account")
                            result.success(null)
                        }
                    }
                "authorizeQuickConnect" -> {
                    val code = call.argument<String>("code")
                    if (code == null) {
                        result.error("INVALID_ARGS", "code is required", null)
                        return@setMethodCallHandler
                    }
                    scope.launch {
                        try {
                            authorizeQuickConnect(code)
                            result.success(null)
                        } catch (e: Exception) {
                            Timber.e(e, "Couldn't authorize Quick Connect")
                            result.error("QUICK_CONNECT_ERROR", e.message, null)
                        }
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    /** The server address, user name and server name of the account Sombrio is signed in to. */
    private suspend fun account(): Map<String, String>? {
        val appPreferences = dependencies.appPreferences()
        if (appPreferences.getValue(appPreferences.offlineMode)) return null
        val serverId = appPreferences.getValue(appPreferences.currentServer) ?: return null
        val current = dependencies.database().getServerWithAddressAndUser(serverId) ?: return null
        val address = current.address ?: return null
        val user = current.user ?: return null
        if (user.accessToken == null) return null
        return mapOf(
            "baseUrl" to address.address,
            "userName" to user.name,
            "serverName" to current.server.name,
        )
    }

    private suspend fun authorizeQuickConnect(code: String) {
        val jellyfinApi = dependencies.jellyfinApi()
        val authorized =
            jellyfinApi.authenticationApi
                .authorizeQuickConnect(code = code, userId = jellyfinApi.userId)
                .content
        check(authorized) { "The server didn't authorize the Quick Connect request" }
    }
}
