package com.droid.wizmusic

import android.content.ComponentName
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.droid.wizmusic.service.WizMusicService
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

abstract class BaseMediaActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var mediaBrowser: MediaBrowserCompat

    abstract fun updateTrackName(trackName: String)

    abstract fun updatePlayPause(isPaused: Boolean)

    abstract fun updateArtistName(artistName: String)

    abstract fun updateCoverArt(url: String)

    abstract fun updateSeekbar(total: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        info { "onCreate" }
        super.onCreate(savedInstanceState)
        initMediaBrowser()
        mediaBrowser.connect()
    }

    private fun initMediaBrowser() {
        info { "initMediaBrowser" }
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, WizMusicService::class.java),
            connectionCallbacks,
            null
        )
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            info { "onConnected" }
            val mediaController =
                MediaControllerCompat(this@BaseMediaActivity, mediaBrowser.sessionToken)
            MediaControllerCompat.setMediaController(this@BaseMediaActivity, mediaController)
            mediaController.registerCallback(controllerCallback)
        }

        override fun onConnectionSuspended() {
            error { "The Service has crashed. Disable transport controls until it automatically reconnects" }
        }

        override fun onConnectionFailed() {
            error { "The Service has refused our connection" }
        }
    }

    public override fun onStart() {
        info { "onStart" }
        super.onStart()
    }

    public override fun onResume() {
        info { "onResume" }
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onStop() {
        info { "onStop" }
        super.onStop()
    }

    override fun onDestroy() {
        info { "onDestroy" }
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        super.onDestroy()
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            info { "onMetadataChanged: ${metadata.toString()}" }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (state == null) {
                return
            }
            info { "onPlaybackStateChanged: $state" }

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    updatePlayPause(false)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    updatePlayPause(true)
                }
            }
            updateTrackName(WizMusicService.currentTrack.song)
            updateArtistName(WizMusicService.currentTrack.artists)
            updateCoverArt(WizMusicService.currentTrack.cover_image)
            WizMusicService.instance?.mediaPlayer?.duration?.let { updateSeekbar(it) }
        }
    }
}
