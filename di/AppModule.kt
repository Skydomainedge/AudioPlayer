package org.wit.audioplayer.di

import android.app.Application
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.wit.audioplayer.data.local.AppDatabase
import org.wit.audioplayer.data.local.AudioTrackDao
import org.wit.audioplayer.data.repository.AudioRepository
import org.wit.audioplayer.service.PlaybackServices
import org.wit.audioplayer.ui.library.SongLibraryViewModel
import org.wit.audioplayer.ui.player.PlayerViewModel

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "audio_player_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // Dao
    single<AudioTrackDao> { get<AppDatabase>().audioTrackDao() }

    single { AudioRepository(androidApplication(), get()) }

    // ViewModels
    viewModel { SongLibraryViewModel(get()) }
    viewModel { PlayerViewModel(get()) }
}
