package com.quick.qr.presentation.home

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.quick.qr.dao.QRCodeDao
import com.quick.qr.model.QRCodeEntity
import com.quick.qr.repo.HomeScreenRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeScreenViewModel(
    private val repository : HomeScreenRepo
) : ViewModel() {

    fun saveBitmapToRoom(title: String, bitmap: Bitmap) {
        viewModelScope.launch {
            repository.saveQRCodeToRoom(title, bitmap)
        }
    }

    fun generateQRCode(text: String, errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.L): Bitmap? {
        return repository.generateQRCode(text, errorCorrectionLevel)
    }

    fun generateMultipleQRCodes(text: String, maxChunkSize: Int = 1800) =
        repository.generateMultipleQRCodes(text, maxChunkSize)

    fun generateCompressedQRCode(text: String) =
        repository.generateCompressedQRCode(text)

    fun saveBitmapToGallery(bitmap: Bitmap, fileName: String = "qr_code_${System.currentTimeMillis()}") {
        viewModelScope.launch {
            repository.saveBitmapToGallery(bitmap, fileName)
        }
    }

    fun getCurrentDateTimeString() = repository.getCurrentDateTimeString()

    fun smartGenerateQRCode(text: String): QRResult {
        return when {
            text.length <= 100 -> {
                val bitmap = generateQRCode(text)
                QRResult.Single(bitmap)
            }
            text.length <= repository.getMaxQRCapacity() -> {
                val bitmap = generateQRCode(text)
                if (bitmap != null) {
                    QRResult.Single(bitmap)
                } else {
                    val compressedBitmap = generateCompressedQRCode(text)
                    if (compressedBitmap != null) {
                        QRResult.Compressed(compressedBitmap)
                    } else {
                        val bitmaps = generateMultipleQRCodes(text)
                        QRResult.Multiple(bitmaps)
                    }
                }
            }
            text.length <= 8000 -> {
                val compressedBitmap = generateCompressedQRCode(text)
                if (compressedBitmap != null) {
                    QRResult.Compressed(compressedBitmap)
                } else {
                    val bitmaps = generateMultipleQRCodes(text)
                    QRResult.Multiple(bitmaps)
                }
            }
            else -> {
                val bitmaps = generateMultipleQRCodes(text)
                QRResult.Multiple(bitmaps)
            }
        }
    }

}

sealed class QRResult {
    data class Single(val bitmap: Bitmap?) : QRResult()
    data class Multiple(val bitmaps: List<Bitmap>) : QRResult()
    data class Compressed(val bitmap: Bitmap?) : QRResult()
}