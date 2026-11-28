package com.sususgh.gamermobile

import android.content.Context
import android.os.Build
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.UUID

object PairingClient {
    data class Pairing(
        val host: String,
        val port: Int,
        val protocol: String,
        val instanceId: String,
        val token: String,
        val baseUrl: String
    )

    data class Result(
        val success: Boolean,
        val message: String,
        val sessionId: String?,
        val sessionToken: String? = null
    )

    fun parse(raw: String): Pairing? {
        return try {
            val uri = URI(raw.trim())
            if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank() ||
                uri.rawUserInfo != null || uri.path != "/pair") return null
            val params = uri.rawQuery.orEmpty().split("&").mapNotNull { item ->
                val parts = item.split("=", limit = 2)
                if (parts.size == 2) {
                    java.net.URLDecoder.decode(parts[0], "UTF-8") to
                        java.net.URLDecoder.decode(parts[1], "UTF-8")
                } else null
            }.toMap()
            val protocol = params["protocol"] ?: return null
            val instanceId = params["instance_id"] ?: return null
            val token = params["token"] ?: return null
            if (protocol != "gamer-runtime/1" || token.length !in 32..128 ||
                instanceId.length !in 16..64) return null
            val port = if (uri.port == -1) if (uri.scheme == "https") 443 else 80 else uri.port
            if (port !in 1..65535) return null
            Pairing(uri.host, port, protocol, instanceId, token,
                "${uri.scheme}://${uri.host}:$port")
        } catch (_: Exception) { null }
    }

    fun claim(context: Context, pairing: Pairing): Result {
        val prefs = context.getSharedPreferences("gamer_mobile", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("device_id", it).apply()
        }
        val payload = JSONObject()
            .put("protocol", pairing.protocol)
            .put("instance_id", pairing.instanceId)
            .put("token", pairing.token)
            .put("device_id", deviceId)
            .put("device_name", Build.MANUFACTURER + " " + Build.MODEL)
            .put("android_version", Build.VERSION.RELEASE)
            .put("app_id", context.packageName)
        val connection = (URL(pairing.baseUrl + "/api/pairing/claim").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 7000
            connection.readTimeout = 7000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = JSONObject(response.ifBlank { "{}" })
            if (code !in 200..299 || json.optBoolean("paired") != true) {
                val reason = json.optString("error", "HTTP_$code")
                return Result(false, reason, null)
            }
            val sessionId = json.optString("sessionId").takeIf { it.isNotBlank() }
                ?: return Result(false, "INVALID_SESSION_RESPONSE", null)
            val sessionToken = json.optString("sessionToken").takeIf { it.isNotBlank() }
                ?: return Result(false, "MISSING_SESSION_CREDENTIAL", null)
            Result(true, "配对认证成功", sessionId, sessionToken)
        } finally {
            connection.disconnect()
        }
    }
}
