package com.sususgh.gamermobile

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

/**
 * Minimal, opt-in integration point.
 * This prototype intentionally does not inspect window contents or dispatch gestures.
 */
class GameAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Future implementation must be transparent, user-controlled, and game-policy compliant.
    }

    override fun onInterrupt() {
        Log.i("GamerMobile", "Accessibility service interrupted")
    }
}
