package com.quick.qr.repo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.quick.qr.dao.QRCodeDao
import com.quick.qr.model.QRCodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPOutputStream

class HomeScreenRepo (
    private val context: Context,
    private val dao: QRCodeDao
) {

    suspend fun saveQRCodeToRoom(title: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val byteArray = bitmapToByteArray(bitmap)
        val qrCode = QRCodeEntity(title = title, imageData = byteArray)
        dao.insertQRCode(qrCode)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun generateQRCode(text: String, errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.L): Bitmap? {
        return try {
            val size = 512
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
            }

            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap[x, y] = if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }
            bitmap
        } catch (e: WriterException) {
            Log.e("QRCodeRepository", "QR kod oluşturma hatası: ${e.localizedMessage}", e)
            null
        }
    }

     fun getMaxQRCapacity(errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.L): Int {
        return when (errorCorrectionLevel) {
            ErrorCorrectionLevel.L -> 2953
            ErrorCorrectionLevel.M -> 2331
            ErrorCorrectionLevel.Q -> 1663
            ErrorCorrectionLevel.H -> 1273
        }
    }

    fun generateMultipleQRCodes(text: String, maxChunkSize: Int = 1800): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val chunks = text.chunked(maxChunkSize)

        chunks.forEachIndexed { index, chunk ->
            val chunkText = if (chunks.size > 1) {
                "[${index + 1}/${chunks.size}] $chunk"
            } else {
                chunk
            }

            if (chunkText.length > getMaxQRCapacity()) {
                val smallerChunks = chunkText.chunked(getMaxQRCapacity() - 50)
                smallerChunks.forEach { smallChunk ->
                    generateQRCode(smallChunk)?.let { bitmaps.add(it) }
                }
            } else {
                generateQRCode(chunkText)?.let { bitmaps.add(it) }
            }
        }

        return bitmaps
    }

    fun generateCompressedQRCode(text: String): Bitmap? {
        return try {
            val compressedBytes = compressText(text)
            val base64Data = android.util.Base64.encodeToString(compressedBytes, android.util.Base64.NO_WRAP)
            val qrData = "COMPRESSED:$base64Data"
            if (qrData.length <= getMaxQRCapacity()) {
                generateQRCode(qrData)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("QRCodeRepository", "Sıkıştırma veya QR oluşturma hatası: ${e.localizedMessage}", e)
            null
        }
    }

    private fun compressText(text: String): ByteArray {
        return try {
            val baos = ByteArrayOutputStream()
            val gzos = GZIPOutputStream(baos)
            gzos.write(text.toByteArray(Charsets.UTF_8))
            gzos.close()
            baos.toByteArray()
        } catch (e: Exception) {
            Log.e("QRCodeRepository", "Sıkıştırma hatası: ${e.localizedMessage}", e)
            text.toByteArray(Charsets.UTF_8)
        }
    }

    fun getCurrentDateTimeString(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        return current.format(formatter)
    }

    suspend fun saveBitmapToGallery(bitmap: Bitmap, fileName: String = "qr_code_${System.currentTimeMillis()}") = withContext(Dispatchers.IO) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QRApp")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri).use { out ->
                    if (out != null) {
                        val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        if (!success) {
                            Log.e("QRCodeRepository", "Bitmap sıkıştırılamadı")
                        }
                    } else {
                        Log.e("QRCodeRepository", "OutputStream null")
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            } else {
                Log.e("QRCodeRepository", "URI oluşturulamadı")
            }
        } catch (e: Exception) {
            Log.e("QRCodeRepository", "Galeriye kaydetme hatası: ${e.localizedMessage}", e)
        }
    }
}