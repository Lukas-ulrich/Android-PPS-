package com.smartnotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("SmartNotifier", "Boot completed - NotificationListenerService wird automatisch vom System gestartet")
            // NotificationListenerService wird vom Android-System automatisch
            // neu gestartet, wenn die Berechtigung erteilt wurde.
            // Dieser Receiver stellt sicher, dass wir nach dem Neustart bereit sind.
        }
    }
}
