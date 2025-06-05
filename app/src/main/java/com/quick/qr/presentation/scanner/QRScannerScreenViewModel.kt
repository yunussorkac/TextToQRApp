package com.quick.qr.presentation.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.quick.qr.repo.QRScannerScreenRepo
import java.util.zip.GZIPInputStream

class QRScannerScreenViewModel (
    private val repository: QRScannerScreenRepo
) : ViewModel() {

    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return repository.decodeBase64ToBitmap(base64Str)
    }

    fun decodeQRCodeFromBitmap(bitmap: Bitmap): String? {
        return repository.decodeQRCodeFromBitmap(bitmap)
    }

}