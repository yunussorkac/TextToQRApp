package com.quick.qr.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_codes")
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val imageData: ByteArray,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QRCodeEntity

        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (title != other.title) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + imageData.contentHashCode()
        return result
    }
}
