package com.droid.wizmusic.service

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.droid.wizmusic.dashboard.Track
import org.jetbrains.anko.error
import org.jetbrains.anko.info

class WizMusicService : BaseMusicPlayer() {

    companion object {
        var tracks = mutableListOf<Track>()
        var tracksHolder = mutableListOf<Track>()
        lateinit var currentTrack: Track
        var instance: WizMusicService? = null
    }

    override fun onCreate() {
        info { "Created" }
        mediaSessionCallback = MediaSessionCallback()
        super.onCreate()
        instance = this
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            info { "onPlay" }
            if (!successfullyRetrievedAudioFocus()) {
                error { "Request AudioFocus failed" }
                return
            }
            playTrack()
        }

        override fun onSeekTo(position: Long) {
            info { "onSeekTo: $position" }
            mediaPlayer.seekTo(position.toInt())
        }

        override fun onPause() {
            info { "onPause" }
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                showPauseNotification()
                initPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }
        }

        override fun onStop() {
            info { "onStop" }
        }

        override fun onSkipToNext() {
            info { "onSkipToNext" }
            playNextTrack()
        }

        override fun onSkipToPrevious() {
            info { "onSkipToPrevious" }
            playPrevious()
        }
    }
}