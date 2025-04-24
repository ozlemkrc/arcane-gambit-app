package com.example.arcane_gambit.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.arcane_gambit.MainActivity
import java.nio.charset.Charset

/**
 * Helper class to simulate NFC tag scanning for development and testing purposes
 * This allows testing the NFC functionality without actual NFC hardware
 */
class NfcSimulator {
    companion object {
        private const val TAG = "NfcSimulator"

        /**
         * Simulate an NFC tag scanning event
         */
        fun simulateNfcScan(activity: ComponentActivity, tagId: String) {
            try {
                Log.d(TAG, "Simulating NFC tag scan with ID: $tagId")

                // Create an intent similar to what would be received from an NFC scan
                val intent = Intent(NfcAdapter.ACTION_TAG_DISCOVERED)

                // Add the tag ID as a string extra
                intent.putExtra("SIMULATED_TAG_ID", tagId)

                // Create fake tag ID as a byte array
                val tagIdBytes = createTagIdBytes(tagId)

                // Add tag ID bytes as a byte array extra
                intent.putExtra("SIMULATED_TAG_ID_BYTES", tagIdBytes)

                // Create a fake NDEF message
                val ndefMessage = createFakeNdefMessage()
                intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf<Parcelable>(ndefMessage))

                // Log the simulation
                Log.d(TAG, "Sending simulated NFC intent")

                // Notify the user
                Toast.makeText(
                    activity,
                    "Simulated NFC tag scan: $tagId",
                    Toast.LENGTH_SHORT
                ).show()

                // Process the intent directly through the ViewModel
                if (activity is MainActivity) {
                    activity.providNfcViewModel().processNfcIntent(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error simulating NFC scan", e)
                Toast.makeText(
                    activity,
                    "Error simulating NFC scan: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /**
         * Create a byte array to represent a tag ID from a string
         */
        private fun createTagIdBytes(tagId: String): ByteArray {
            // If the tagId is already a hex string, convert it to bytes
            if (tagId.matches(Regex("[0-9A-Fa-f]+"))) {
                return hexStringToByteArray(tagId)
            }

            // Otherwise, use the string's bytes directly (up to 8 bytes)
            val idBytes = tagId.toByteArray(Charset.forName("UTF-8"))
            val result = ByteArray(minOf(idBytes.size, 8))
            System.arraycopy(idBytes, 0, result, 0, result.size)
            return result
        }

        /**
         * Convert a hex string to byte array
         */
        private fun hexStringToByteArray(s: String): ByteArray {
            // Make sure length is even
            val sanitized = if (s.length % 2 == 0) s else "0$s"

            val len = sanitized.length
            val data = ByteArray(len / 2)

            for (i in 0 until len step 2) {
                data[i / 2] = ((Character.digit(sanitized[i], 16) shl 4) +
                        Character.digit(sanitized[i + 1], 16)).toByte()
            }

            return data
        }

        /**
         * Create a fake NDEF message
         */
        private fun createFakeNdefMessage(): NdefMessage {
            val text = "Arcane Gambit RFID"
            val textBytes = text.toByteArray(Charset.forName("UTF-8"))
            val textRecord = NdefRecord.createMime(
                "text/plain",
                textBytes
            )
            return NdefMessage(arrayOf(textRecord))
        }
    }
}