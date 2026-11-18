package com.mauricioliveira.awesomenotes.core.database.di

import androidx.room.Room
import com.mauricioliveira.awesomenotes.core.database.AwesomeNotesDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AwesomeNotesDatabase::class.java,
            name = AwesomeNotesDatabase.NAME,
        ).build()
    }
    single { get<AwesomeNotesDatabase>().noteDao() }
}
