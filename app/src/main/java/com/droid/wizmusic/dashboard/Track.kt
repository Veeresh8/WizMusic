package com.droid.wizmusic.dashboard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Track(
    @PrimaryKey val url: String,
    val song: String,
    val artists: String,
    val cover_image: String,
    var isPurchased: Boolean
)

data class TrackException(
    val message: String?,
    val code: Int = 1337
)

sealed class TrackResult {
    class Success(val tracks: List<Track>?) : TrackResult()
    class Error(val trackException: TrackException) : TrackResult()
    object InProgress : TrackResult()
}

sealed class AddTrackResult {
    object Success : AddTrackResult()
    class Error(val trackException: TrackException) : AddTrackResult()
}