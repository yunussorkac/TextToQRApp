package com.quick.qr.repo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.zip.GZIPInputStream

class QRScannerScreenRepo {

    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        val cleanedBase64 = base64Str.replace("%0A", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "")
        return try {
            val decodedBytes = Base64.decode(cleanedBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeQRCodeFromBitmap(bitmap: Bitmap): String? {
        return try {
            val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }

            val intArray = IntArray(softwareBitmap.width * softwareBitmap.height)
            softwareBitmap.getPixels(intArray, 0, softwareBitmap.width, 0, 0, softwareBitmap.width, softwareBitmap.height)
            val source = RGBLuminanceSource(softwareBitmap.width, softwareBitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val result = MultiFormatReader().decode(binaryBitmap)
            val rawText = result.text

            if (rawText.startsWith("COMPRESSED:")) {
                val base64Data = rawText.removePrefix("COMPRESSED:")
                val compressedBytes = Base64.decode(base64Data, Base64.NO_WRAP)

                val inputStream = GZIPInputStream(compressedBytes.inputStream())
                val decompressedText = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

                decompressedText
            } else {
                rawText
            }
        } catch (e: Exception) {
            Log.e("QRDecoder", "QR kod çözme hatası: ${e.localizedMessage}")
            null
        }
    }
}