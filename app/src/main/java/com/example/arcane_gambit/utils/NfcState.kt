package com.example.arcane_gambit.utils

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import com.example.arcane_gambit.ui.screens.Character
import com.google.gson.Gson
import java.nio.charset.Charset

/**
 * Helper class to handle NFC operations for character data transfer
 */
class NFCHelper(private val activity: Activity) {

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(activity)
    }

    /**
     * Check if the device has NFC capabilities
     */
    fun hasNFCSupport(): Boolean {
        return nfcAdapter != null
    }

    /**
     * Check if NFC is enabled on the device
     */
    fun isNFCEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    /**
     * Enable NFC foreground dispatch to handle NFC intents
     */
    fun enableNFCForegroundDispatch() {
        try {
            val intent = Intent(activity, activity.javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                activity, 0, intent, android.app.PendingIntent.FLAG_MUTABLE
            )

            nfcAdapter?.enableForegroundDispatch(
                activity,
                pendingIntent,
                null,
                null
            )
        } catch (e: Exception) {
            Log.e("NFCHelper", "Error enabling NFC foreground dispatch: ${e.message}")
        }
    }

    /**
     * Disable NFC foreground dispatch
     */
    fun disableNFCForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(activity)
        } catch (e: Exception) {
            Log.e("NFCHelper", "Error disabling NFC foreground dispatch: ${e.message}")
        }
    }

    /**
     * Convert a Character object to NdefMessage for NFC transfer
     */
    fun createCharacterNdefMessage(character: Character): NdefMessage? {
        try {
            // Convert character to JSON string
            val characterJson = Gson().toJson(character)

            // Create MIME record with the character data
            val mimeRecord = NdefRecord.createMime(
                "application/com.arcane_gambit.character",
                characterJson.toByteArray(Charset.forName("UTF-8"))
            )

            // Create NdefMessage with the MIME record
            return NdefMessage(arrayOf(mimeRecord))
        } catch (e: Exception) {
            Log.e("NFCHelper", "Error creating character NDEF message: ${e.message}")
            return null
        }
    }

    /**
     * Write character data to an NFC tag
     */
    fun writeCharacterToTag(tag: Tag, character: Character): Boolean {
        try {
            val ndef = Ndef.get(tag) ?: return false

            // Check if tag is writable
            if (!ndef.isWritable) {
                Log.e("NFCHelper", "NFC tag is read-only")
                return false
            }

            // Create the NDEF message
            val ndefMessage = createCharacterNdefMessage(character) ?: return false

            // Check if the tag has enough space
            val messageSize = ndefMessage.toByteArray().size
            if (ndef.maxSize < messageSize) {
                Log.e("NFCHelper", "NFC tag capacity is too small")
                return false
            }

            // Connect and write to tag
            ndef.connect()
            ndef.writeNdefMessage(ndefMessage)
            ndef.close()

            return true
        } catch (e: Exception) {
            Log.e("NFCHelper", "Error writing to NFC tag: ${e.message}")
            return false
        }
    }
}