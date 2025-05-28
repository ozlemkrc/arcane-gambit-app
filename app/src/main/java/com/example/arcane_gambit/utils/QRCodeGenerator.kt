package com.example.arcane_gambit.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.*

object QRCodeGenerator {
    
    /**
     * Generate a QR code bitmap from the given data
     * @param data The data to encode in the QR code
     * @param width The width of the generated bitmap
     * @param height The height of the generated bitmap
     * @return Bitmap containing the QR code
     */
    fun generateQRCode(data: String, width: Int, height: Int): Bitmap {
        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints)
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            return bitmap
        } catch (e: WriterException) {
            throw RuntimeException("Error generating QR code", e)
        }
    }
}
