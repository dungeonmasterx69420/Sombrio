package dev.jdtech.jellyfin.api

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Lets the Jellyfin SDK read responses from Jellyfin 10.11 servers.
 *
 * The SDK is generated from the Jellyfin 12 API, in which every media stream has an `IsOriginal`
 * field. 10.11 doesn't send it, so the SDK can't read any item, or playback info, with media
 * streams in it. This adds `"IsOriginal":false` to each media stream of a response that has none.
 */
class LegacyServerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body
        val contentType = body.contentType()
        if (contentType?.subtype != "json") return response

        val json = body.string()
        val patched =
            if (IS_ORIGINAL in json) json
            else json.replace(IS_HEARING_IMPAIRED) { "\"IsOriginal\":false,${it.value}" }

        return response.newBuilder().body(patched.toResponseBody(contentType)).build()
    }

    private companion object {
        // Quotes inside JSON strings are escaped, so these only match keys
        const val IS_ORIGINAL = "\"IsOriginal\""
        val IS_HEARING_IMPAIRED = Regex("\"IsHearingImpaired\"\\s*:")
    }
}
