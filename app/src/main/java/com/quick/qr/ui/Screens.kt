package com.quick.qr.ui

import kotlinx.serialization.Serializable

sealed class Screens {

    @Serializable
    data object Home : Screens()

    @Serializable
    data class QRScanner(val encoded : String? = null) : Screens()

    @Serializable
    data object SavedQRCodes : Screens()


}