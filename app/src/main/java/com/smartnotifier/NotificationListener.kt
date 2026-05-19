package com.smartnotifier

import android.app.Notification
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "SmartNotifier"
        const val WHATSAPP_PKG = "com.whatsapp"
        const val WHATSAPP_BUSINESS_PKG = "com.whatsapp.w4b"
        private const val WAKELOCK_TIMEOUT = 10_000L // 10 Sekunden
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val prefs = Prefs(this)

        // App prüfen: nur WhatsApp
        if (!isWhatsApp(sbn.packageName)) return

        // Extras auslesen
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text  = extras.getString(Notification.EXTRA_TEXT)  ?: ""
        val bigText = extras.getString(Notification.EXTRA_BIG_TEXT) ?: ""

        Log.d(TAG, "WhatsApp Notification | title='$title' text='$text'")

        // Absender prüfen
        if (!isSenderMatch(title, text, bigText, prefs)) return

        Log.d(TAG, "MATCH! Triggering alert for sender '${prefs.senderName}'")
        triggerAlert(prefs)
    }

    private fun isWhatsApp(pkg: String): Boolean {
        return pkg == WHATSAPP_PKG || pkg == WHATSAPP_BUSINESS_PKG
    }

    /**
     * Prüft ob der Absender übereinstimmt.
     * Funktioniert für:
     * - Einzel-Chat: title == "Partnerin"
     * - Gruppen-Chat: text enthält "Partnerin: Hey!"
     * - Business-WhatsApp: gleiche Logik
     */
    private fun isSenderMatch(
        title: String,
        text: String,
        bigText: String,
        prefs: Prefs
    ): Boolean {
        val senderName = prefs.senderName.trim()
        if (senderName.isEmpty()) return false

        // Einzelchat: title ist direkt der Kontaktname
        if (title.contains(senderName, ignoreCase = true)) return true

        // Gruppenchat: Text beginnt mit "Name: Nachricht"
        if (text.startsWith("$senderName:", ignoreCase = true)) return true
        if (bigText.contains("$senderName:", ignoreCase = true)) return true

        return false
    }

    private fun triggerAlert(prefs: Prefs) {
        // Bildschirm aufwecken
        wakeScreen()

        // Vibration (bypassed DND via USAGE_ALARM)
        if (prefs.vibrationEnabled) {
            triggerVibration()
        }

        // Sound (bypassed DND via STREAM_ALARM)
        if (prefs.soundEnabled) {
            triggerSound(prefs)
        }
    }

    private fun wakeScreen() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wl = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "SmartNotifier::WakeLock"
            )
            wl.acquire(WAKELOCK_TIMEOUT)
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock failed: ${e.message}")
        }
    }

    private fun triggerVibration() {
        try {
            // Starkes Vibrationsmuster: lang-kurz-lang
            val pattern = longArrayOf(0, 800, 200, 800, 200, 1200)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vm.defaultVibrator
                val effect = VibrationEffect.createWaveform(pattern, -1)
                val audioAttr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM) // Bypasses DND!
                    .build()
                vibrator.vibrate(effect, audioAttr)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator.vibrate(effect)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed: ${e.message}")
        }
    }

    private fun triggerSound(prefs: Prefs) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Auf STREAM_ALARM umstellen – ignoriert DND/Lautlos
            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

            // Lautstärke entsprechend Einstellung setzen
            val targetVolume = (maxVolume * prefs.volumePercent / 100f).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolume, 0)

            // Alarm-Ton oder Standard-Alarm
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }

            ringtone.play()

            // Nach 3 Sekunden stoppen
            android.os.Handler(mainLooper).postDelayed({
                try {
                    ringtone.stop()
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
                } catch (_: Exception) {}
            }, 3000)

        } catch (e: Exception) {
            Log.e(TAG, "Sound failed: ${e.message}")
        }
    }
}
