package com.example.arcane_gambit.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.util.Log
import android.widget.Toast

/**
 * Manages NFC operations for RFID tag reading in the Arcane Gambit app
 */
class NfcManager(private val activity: androidx.activity.ComponentActivity) {
    private val tag = "NfcManager" // Lowercase variable name as per convention
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFilters: Array<IntentFilter>? = null

    init {
        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

        // Create a PendingIntent that will be used to read NFC tags
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                activity,
                0,
                Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                activity,
                0,
                Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // Set up intent filters for NFC discovery
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        intentFilters = arrayOf(tagDetected, ndefDetected, techDetected)
    }

    /**
     * Checks if NFC is available on the device
     */
    fun isNfcAvailable(): Boolean {
        return nfcAdapter != null
    }

    /**
     * Checks if NFC is enabled on the device
     */
    fun isNfcEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    /**
     * Enables foreground dispatch for NFC tag detection
     * Call this in onResume()
     */
    fun enableForegroundDispatch() {
        if (nfcAdapter != null && nfcAdapter!!.isEnabled) {
            try {
                nfcAdapter!!.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
                Log.d(tag, "NFC foreground dispatch enabled")
            } catch (e: Exception) {
                Log.e(tag, "Error enabling NFC foreground dispatch", e)
                Toast.makeText(activity, "Failed to enable NFC scanning", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(tag, "NFC not available or disabled")
        }
    }

    /**
     * Disables foreground dispatch for NFC
     * Call this in onPause()
     */
    fun disableForegroundDispatch() {
        if (nfcAdapter != null) {
            try {
                nfcAdapter!!.disableForegroundDispatch(activity)
                Log.d(tag, "NFC foreground dispatch disabled")
            } catch (e: Exception) {
                Log.e(tag, "Error disabling NFC foreground dispatch", e)
            }
        }
    }

    /**
     * Process an NFC intent and extract RFID tag ID
     * Call this in onNewIntent()
     */
    // In NfcManager.kt, update the processIntent method:
    fun processIntent(intent: Intent): String? {
        val action = intent.action

        if (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            // First check if this is a simulated tag
            val simulatedTagId = intent.getStringExtra("SIMULATED_TAG_ID")
            if (simulatedTagId != null) {
                Log.d(tag, "Simulated RFID Tag detected: $simulatedTagId")
                return simulatedTagId
            }

            // If not simulated, process as a real tag
            val tagObject: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            if (tagObject != null) {
                // Convert tag ID to hex string
                val tagId = bytesToHexString(tagObject.id)
                Log.d(tag, "RFID Tag detected: $tagId")
                return tagId
            }
        }
        return null
    }

    /**
     * Convert byte array to hexadecimal string
     */
    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            val hex = Integer.toHexString(0xFF and b.toInt())
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }
}