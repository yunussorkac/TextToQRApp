package com.quick.qr.repo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import com.quick.qr.dao.QRCodeDao
import com.quick.qr.model.QRCodeEntity
import kotlinx.coroutines.flow.Flow
import java.util.zip.GZIPInputStream

class SavedQRCodesScreenRepo (
    private val dao: QRCodeDao,
    private val appContext: Context
) {

    fun getAllQRCodes(): Flow<List<QRCodeEntity>> = dao.getAllQRCodes()

    suspend fun deleteQRCode(qrCode: QRCodeEntity) {
        dao.deleteQRCode(qrCode)
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

            val source = com.google.zxing.RGBLuminanceSource(softwareBitmap.width, softwareBitmap.height, intArray)
            val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))

            val result = com.google.zxing.MultiFormatReader().decode(binaryBitmap)
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

    fun saveBitmapToGallery(bitmap: Bitmap, fileName: String = "qr_code_${System.currentTimeMillis()}") {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QRApp")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = appContext.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri).use { out ->
                    if (out != null) {
                        val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        if (success) {
                            Log.d("SaveQR", "QR kod başarıyla galeriye kaydedildi: $fileName")
                        } else {
                            Log.e("SaveQR", "QR kod sıkıştırılamadı")
                        }
                    } else {
                        Log.e("SaveQR", "Çıktı akışı (OutputStream) null")
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            } else {
                Log.e("SaveQR", "URI oluşturulamadı, resim eklenemedi.")
            }
        } catch (e: Exception) {
            Log.e("SaveQR", "Hata oluştu: ${e.localizedMessage}")
        }
    }

}