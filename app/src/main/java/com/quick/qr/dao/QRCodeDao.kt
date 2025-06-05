package com.quick.qr.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.quick.qr.model.QRCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QRCodeDao {

    @Insert
    suspend fun insertQRCode(qrCodeEntity: QRCodeEntity)

    @Query("SELECT * FROM qr_codes ORDER BY timestamp DESC")
    fun getAllQRCodes(): Flow<List<QRCodeEntity>>


    @Delete
    suspend fun deleteQRCode(qrCode: QRCodeEntity)

}