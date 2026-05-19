# Smart Priority Notifier

Eine Android-App, die WhatsApp-Nachrichten von einer bestimmten Person erkennt
und einen Alarm auslöst — auch im Nachtmodus / DND / Lautlos.

---

## Setup in Android Studio

1. **Projekt öffnen**
   - Android Studio starten
   - „Open" → Diesen Ordner `SmartPriorityNotifier` auswählen
   - Gradle-Sync abwarten (~1-2 Minuten beim ersten Mal)

2. **App auf Gerät installieren**
   - Android-Gerät per USB verbinden
   - USB-Debugging in den Entwickleroptionen aktivieren
   - Auf „Run ▶" klicken

3. **Berechtigung erteilen (einmalig)**
   - App öffnen
   - Auf „Berechtigung erteilen" tippen
   - In der System-Liste „Smart Priority Notifier" aktivieren
   - Zurück zur App

4. **Kontakt eintragen**
   - Den **exakten WhatsApp-Kontaktnamen** eintragen (z.B. „Lea")
   - Einstellungen speichern
   - Test-Alarm testen ✓

---

## Wie der Kontaktname funktioniert

| Situation | Was du einträgst |
|---|---|
| Einzelchat mit „Lea" | `Lea` |
| Einzelchat mit „Partnerin 💕" | `Partnerin 💕` |
| Gruppenchat, Lea schreibt | `Lea` |

Der Name muss **exakt** mit dem WhatsApp-Anzeigenamen übereinstimmen.

---

## Technische Details

- `NotificationListenerService` liest alle Android-Notifications mit
- Filter: `packageName == com.whatsapp` + Absender-Match
- Alarm-Ton läuft über `STREAM_ALARM` → ignoriert DND & Lautlos
- Vibration via `AudioAttributes.USAGE_ALARM` → bypasses DND
- WakeLock weckt den Bildschirm auf
- Funktioniert nach Neustart automatisch weiter

---

## Anforderungen

- Android 8.0+ (API 26+)
- WhatsApp oder WhatsApp Business
- Android Studio Hedgehog (2023.1) oder neuer
