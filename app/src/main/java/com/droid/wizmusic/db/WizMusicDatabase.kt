package com.droid.wizmusic.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.droid.wizmusic.Application
import com.droid.wizmusic.Track

@Database(entities = [Track::class], version = 1)
abstract class WizMusicDatabase : RoomDatabase() {
    abstract fun tracksDAO(): TracksDAO

    companion object {
        val instance = Application.instance?.applicationContext?.let {
            Room.databaseBuilder(
                it,
                WizMusicDatabase::class.java, "wiz_music_database"
            ).build()
        }
    }
}