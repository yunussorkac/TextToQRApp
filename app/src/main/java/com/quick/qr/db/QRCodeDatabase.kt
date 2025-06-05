package com.quick.qr.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quick.qr.dao.QRCodeDao
import com.quick.qr.model.QRCodeEntity

@Database(entities = [QRCodeEntity::class], version = 1)
abstract class QRCodeDatabase : RoomDatabase() {
    abstract fun qrCodeDao(): QRCodeDao
}