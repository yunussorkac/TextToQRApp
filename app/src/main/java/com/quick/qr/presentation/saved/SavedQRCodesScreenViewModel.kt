package com.quick.qr.presentation.saved

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quick.qr.model.QRCodeEntity
import com.quick.qr.repo.SavedQRCodesScreenRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedQRCodesScreenViewModel  (
    private val repository: SavedQRCodesScreenRepo
) : ViewModel() {

    val qrCodes: StateFlow<List<QRCodeEntity>> = repository.getAllQRCodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteQRCode(qrCode: QRCodeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQRCode(qrCode)
        }
    }

    fun decodeQRCodeFromBitmap(bitmap: Bitmap): String? {
        return repository.decodeQRCodeFromBitmap(bitmap)
    }

    fun saveBitmapToGallery(bitmap: Bitmap, fileName: String = "qr_code_${System.currentTimeMillis()}") {
        repository.saveBitmapToGallery(bitmap, fileName)
    }

}