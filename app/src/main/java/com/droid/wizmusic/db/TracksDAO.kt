package com.droid.wizmusic.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.droid.wizmusic.Track

@Dao
interface TracksDAO {

    @Query("SELECT * FROM track WHERE isPurchased = 1")
    fun getPurchasedTracks(): LiveData<List<Track>>

    @Insert(onConflict = REPLACE)
    fun addTrack(track: Track): Long
}