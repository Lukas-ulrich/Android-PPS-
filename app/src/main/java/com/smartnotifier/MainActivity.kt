package com.smartnotifier

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    // UI Views
    private lateinit var statusCard: View
    private lateinit var statusIcon: ImageView
    private lateinit var statusText: TextView
    private lateinit var statusSubtext: TextView
    private lateinit var btnPermission: Button

    private lateinit var switchEnabled: SwitchMaterial
    private lateinit var etSenderName: EditText
    private lateinit var switchVibration: SwitchMaterial
    private lateinit var switchSound: SwitchMaterial
    private lateinit var seekVolume: SeekBar
    private lateinit var tvVolumeValue: TextView
    private lateinit var btnSave: Button
    private lateinit var btnTest: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = Prefs(this)
        bindViews()
        setupListeners()
        loadSettings()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun bindViews() {
        statusCard     = findViewById(R.id.statusCard)
        statusIcon     = findViewById(R.id.statusIcon)
        statusText     = findViewById(R.id.statusText)
        statusSubtext  = findViewById(R.id.statusSubtext)
        btnPermission  = findViewById(R.id.btnPermission)

        switchEnabled  = findViewById(R.id.switchEnabled)
        etSenderName   = findViewById(R.id.etSenderName)
        switchVibration= findViewById(R.id.switchVibration)
        switchSound    = findViewById(R.id.switchSound)
        seekVolume     = findViewById(R.id.seekVolume)
        tvVolumeValue  = findViewById(R.id.tvVolumeValue)
        btnSave        = findViewById(R.id.btnSave)
        btnTest        = findViewById(R.id.btnTest)
    }

    private fun setupListeners() {
        btnPermission.setOnClickListener {
            openNotificationListenerSettings()
        }

        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvVolumeValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        switchSound.setOnCheckedChangeListener { _, checked ->
            seekVolume.isEnabled = checked
        }

        btnSave.setOnClickListener {
            saveSettings()
        }

        btnTest.setOnClickListener {
            testAlert()
        }
    }

    private fun loadSettings() {
        switchEnabled.isChecked   = prefs.serviceEnabled
        etSenderName.setText(prefs.senderName)
        switchVibration.isChecked = prefs.vibrationEnabled
        switchSound.isChecked     = prefs.soundEnabled
        seekVolume.progress       = prefs.volumePercent
        tvVolumeValue.text        = "${prefs.volumePercent}%"
        seekVolume.isEnabled      = prefs.soundEnabled
    }

    private fun saveSettings() {
        val name = etSenderName.text.toString().trim()
        if (name.isEmpty()) {
            etSenderName.error = "Kontaktname eingeben"
            etSenderName.requestFocus()
            return
        }

        prefs.senderName       = name
        prefs.serviceEnabled   = switchEnabled.isChecked
        prefs.vibrationEnabled = switchVibration.isChecked
        prefs.soundEnabled     = switchSound.isChecked
        prefs.volumePercent    = seekVolume.progress

        Snackbar.make(btnSave, "✓ Einstellungen gespeichert", Snackbar.LENGTH_SHORT).show()
    }

    private fun testAlert() {
        if (!isNotificationListenerEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Berechtigung fehlt")
                .setMessage("Bitte erteile zuerst die Benachrichtigungs-Berechtigung.")
                .setPositiveButton("Einstellungen öffnen") { _, _ -> openNotificationListenerSettings() }
                .setNegativeButton("Abbrechen", null)
                .show()
            return
        }

        saveSettings()

        // Direkt Alert triggern über den Service
        val testListener = NotificationListener()
        // Einfacher Test-Aufruf via Reflection-freie Methode: eigene Klasse instanziieren klappt
        // nicht im Service-Kontext, deshalb senden wir einen Broadcast
        sendBroadcast(Intent(ACTION_TEST_ALERT).setPackage(packageName))

        Snackbar.make(btnTest, "Test-Alarm wird ausgelöst…", Snackbar.LENGTH_SHORT).show()
    }

    private fun updatePermissionStatus() {
        if (isNotificationListenerEnabled()) {
            statusCard.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.status_active_bg)
            statusIcon.setImageResource(R.drawable.ic_check)
            statusText.text     = "Aktiv"
            statusSubtext.text  = "Benachrichtigungs-Zugriff erteilt"
            statusText.setTextColor(ContextCompat.getColor(this, R.color.status_active_text))
            btnPermission.visibility = View.GONE
        } else {
            statusCard.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.status_inactive_bg)
            statusIcon.setImageResource(R.drawable.ic_warning)
            statusText.text     = "Berechtigung fehlt"
            statusSubtext.text  = "Tippe auf 'Berechtigung erteilen'"
            statusText.setTextColor(ContextCompat.getColor(this, R.color.status_inactive_text))
            btnPermission.visibility = View.VISIBLE
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val cn = ComponentName(this, NotificationListener::class.java)
        return flat.contains(cn.flattenToString())
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    companion object {
        const val ACTION_TEST_ALERT = "com.smartnotifier.TEST_ALERT"
    }
}
