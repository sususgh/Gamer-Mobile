package com.sususgh.gamermobile

import android.accessibilityservice.AccessibilityService
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Opt-in foreground game discovery.
 * This service records only the foreground package/game profile; it does not capture screen contents.
 */
class GameAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName ||
            packageName.startsWith("com.android.") ||
            packageName.startsWith("com.google.android.")) return

        val (gameName, gameKind) = identifyGame(packageName)
        if (gameKind == "unknown") return

        getSharedPreferences("gamer_mobile", MODE_PRIVATE).edit()
            .putString("detected_game_name", gameName)
            .putString("detected_game_package", packageName)
            .putString("detected_game_kind", gameKind)
            .putLong("detected_game_at", System.currentTimeMillis())
            .apply()
        Log.i("GamerMobile", "Foreground game detected: $gameName ($packageName), profile=$gameKind")
    }

    private fun identifyGame(pkg: String): Pair<String, String> {
        return when (pkg) {
            "com.mojang.minecraftpe" -> "Minecraft" to "minecraft"
            "com.miHoYo.Yuanshen",
            "com.miHoYo.GenshinImpact",
            "com.miHoYo.ys.bilibili" -> "原神" to "genshin"
            else -> {
                val appInfo = try {
                    if (Build.VERSION.SDK_INT >= 33) {
                        packageManager.getApplicationInfo(pkg, android.content.pm.PackageManager.ApplicationInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getApplicationInfo(pkg, 0)
                    }
                } catch (_: Exception) { null }
                val isGame = appInfo != null && Build.VERSION.SDK_INT >= 26 &&
                    appInfo.category == ApplicationInfo.CATEGORY_GAME
                if (isGame) {
                    val label = packageManager.getApplicationLabel(appInfo!!).toString()
                    label to "game"
                } else {
                    "未知游戏" to "unknown"
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.i("GamerMobile", "Accessibility service interrupted")
    }
}
