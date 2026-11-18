package com.mauricioliveira.awesomenotes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mauricioliveira.awesomenotes.core.database.dao.NoteDao
import com.mauricioliveira.awesomenotes.core.database.entity.NoteEntity

@Database(entities = [NoteEntity::class], version = 1)
abstract class AwesomeNotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        const val NAME = "awesome-notes.db"
    }
}
