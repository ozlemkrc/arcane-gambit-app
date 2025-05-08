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
 * Utility class for handling NFC operations
 */
object NFCUtil {

    /**
     * Checks if NFC is available and enabled
     */
    fun isNfcAvailable(activity: ComponentActivity): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        return nfcAdapter != null && nfcAdapter.isEnabled
    }

    /**
     * Creates an NDEF message with character data
     */
    fun createCharacterNdefMessage(character: Character): NdefMessage {
        // Serialize character to JSON
        val json = JSONObject().apply {
            put("id", character.id)
            put("name", character.name)
            put("level", character.level)
            put("strength", character.strength)
            put("agility", character.agility)
            // Derived stats
            put("attack", character.strength * 2)
            put("defence", (character.strength / 2) + (character.agility / 2) + 5)
            put("luck", character.agility * 3)
            put("vitality", character.level * 5)
        }

        val jsonString = json.toString()

        // Create MIME record with character data
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
     * Extracts a Character object from an NDEF message
     */
    fun extractCharacterFromNdefMessage(message: NdefMessage): Character? {
        for (record in message.records) {
            if (String(record.type) == "application/com.example.arcane_gambit") {
                val payload = record.payload
                val characterJson = String(payload, Charset.forName("UTF-8"))

                try {
                    val json = JSONObject(characterJson)
                    return Character(
                        id = json.getString("id"),
                        name = json.getString("name"),
                        level = json.getInt("level"),
                        strength = json.getInt("strength"),
                        agility = json.getInt("agility")
                    )
                } catch (e: Exception) {
                    return null
                }
            }
        }
        return null
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
            }
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Writes character data to an NFC tag
     * Returns true if successful, false otherwise
     */
    fun writeCharacterToTag(tag: Tag, character: Character): Boolean {
        val ndefMessage = createCharacterNdefMessage(character)
        return writeNdefMessage(tag, ndefMessage)
    }
}