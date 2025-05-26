package com.example.arcane_gambit.utils

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.activity.ComponentActivity
import com.example.arcane_gambit.ui.screens.Character
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Data class for character data read from NFC
 */
data class NfcCharacterData(
    val characterId: String,
    val userToken: String
)

/**
 * Utility class for handling NFC operations
 */
object NFCUtil {    /**
     * Checks if NFC is available and enabled
     */
    fun isNfcAvailable(activity: ComponentActivity): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        return nfcAdapter != null && nfcAdapter.isEnabled
    }

    /**
     * Creates an NDEF message with character ID and user token
     */
    fun createCharacterNdefMessage(character: Character, userToken: String): NdefMessage {
        // Serialize only character ID and user token to JSON
        val json = JSONObject().apply {
            put("characterId", character.id)
            put("userToken", userToken)
        }

        val jsonString = json.toString()

        // Create MIME record with minimal character data
        val mimeRecord = NdefRecord.createMime(
            "application/com.example.arcane_gambit",
            jsonString.toByteArray(Charset.forName("UTF-8"))
        )

        // Create application record to launch our app on the receiving end if installed
        val appRecord = NdefRecord.createApplicationRecord("com.example.arcane_gambit")

        // Return the NDEF message with both records
        return NdefMessage(arrayOf(mimeRecord, appRecord))
    }

    /**
     * Writes an NDEF message to an NFC tag
     * Returns true if successful, false otherwise
     */
    fun writeNdefMessage(tag: Tag, ndefMessage: NdefMessage): Boolean {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                // Tag is already NDEF formatted
                ndef.connect()

                if (ndef.isWritable) {
                    // Check if there's enough space on the tag
                    val messageSize = ndefMessage.toByteArray().size
                    if (messageSize > ndef.maxSize) {
                        return false
                    }

                    try {
                        ndef.writeNdefMessage(ndefMessage)
                        return true
                    } catch (e: Exception) {
                        return false
                    } finally {
                        ndef.close()
                    }
                } else {
                    // Tag is read-only
                    return false
                }
            } else {
                // Try to format the tag
                val ndefFormat = NdefFormatable.get(tag)
                if (ndefFormat != null) {
                    try {
                        ndefFormat.connect()
                        ndefFormat.format(ndefMessage)
                        return true
                    } catch (e: Exception) {
                        return false
                    } finally {
                        ndefFormat.close()
                    }
                } else {
                    // Tag doesn't support NDEF
                    return false
                }
            }        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Writes character data to an NFC tag
     * Returns true if successful, false otherwise
     */
    fun writeCharacterToTag(tag: Tag, character: Character, userToken: String): Boolean {
        val ndefMessage = createCharacterNdefMessage(character, userToken)
        return writeNdefMessage(tag, ndefMessage)
    }    /**
     * Writes character data to an NFC tag using SessionManager to get user token
     * Returns true if successful, false otherwise
     */
    fun writeCharacterToTag(tag: Tag, character: Character, activity: ComponentActivity): Boolean {
        val sessionManager = SessionManager(activity)
        val userToken = sessionManager.getToken()
        
        if (userToken == null) {
            return false // No user token available
        }
        
        return writeCharacterToTag(tag, character, userToken)
    }

    /**
     * Reads character data from an NFC tag
     * Returns NfcCharacterData if successful, null otherwise
     */
    fun readCharacterFromTag(tag: Tag): NfcCharacterData? {
        try {
            val ndef = Ndef.get(tag) ?: return null
            ndef.connect()
            
            val ndefMessage = ndef.ndefMessage
            if (ndefMessage != null && ndefMessage.records.isNotEmpty()) {
                // Look for our MIME record
                for (record in ndefMessage.records) {
                    val mimeType = String(record.type, Charset.forName("UTF-8"))
                    if (mimeType == "application/com.example.arcane_gambit") {
                        val payload = String(record.payload, Charset.forName("UTF-8"))
                        
                        try {
                            val json = JSONObject(payload)
                            val characterId = json.getString("characterId")
                            val userToken = json.getString("userToken")
                            
                            return NfcCharacterData(characterId, userToken)
                        } catch (e: Exception) {
                            // Failed to parse JSON
                            return null
                        }
                    }
                }
            }
            
            ndef.close()
        } catch (e: Exception) {
            // Error reading tag
        }
        
        return null
    }
}