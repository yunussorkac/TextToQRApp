package com.quick.qr.di

import android.app.Application
import androidx.room.Room
import com.quick.qr.db.QRCodeDatabase
import com.quick.qr.presentation.home.HomeScreenViewModel
import com.quick.qr.presentation.saved.SavedQRCodesScreenViewModel
import com.quick.qr.presentation.scanner.QRScannerScreenViewModel
import com.quick.qr.repo.HomeScreenRepo
import com.quick.qr.repo.QRScannerScreenRepo
import com.quick.qr.repo.SavedQRCodesScreenRepo
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

object Modules {

    val viewModelModule = module {
        viewModel { HomeScreenViewModel(get()) }
        viewModel { QRScannerScreenViewModel(get()) }
        viewModel { SavedQRCodesScreenViewModel(get()) }
    }

    val repositoryModule = module {
        single { HomeScreenRepo(get(), get()) }
        single { QRScannerScreenRepo() }
        single { SavedQRCodesScreenRepo(get(), get()) }
    }

    val roomModule = module {
        single {
            Room.databaseBuilder(
                get<Application>(),
                QRCodeDatabase::class.java,
                "qr_code_database"
            ).build()
        }

        single { get<QRCodeDatabase>().qrCodeDao() }
    }




}