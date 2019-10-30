package com.droid.wizmusic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import coil.Coil
import coil.api.load
import com.droid.wizmusic.utils.MediaStyleHelper
import com.droid.wizmusic.utils.MediaStyleHelper.addNext
import com.droid.wizmusic.utils.MediaStyleHelper.addPause
import com.droid.wizmusic.utils.MediaStyleHelper.addPlay
import com.droid.wizmusic.utils.MediaStyleHelper.addPrev
import com.droid.wizmusic.utils.MediaStyleHelper.createNotification
import com.droid.wizmusic.dashboard.Track
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

/*
* Base media player class. Using MediaBrowserServiceCompat for media playback
* which is a service bounded to the UI.
*
* Client-Server implementation.
* */

abstract class BaseMusicPlayer : MediaBrowserServiceCompat(),
    AudioManager.OnAudioFocusChangeListener,
    AnkoLogger {

    lateinit var mediaPlayer: MediaPlayer
    lateinit var nextTrack: Track

    private var mediaSession: MediaSessionCompat? = null
    protected lateinit var mediaSessionCallback: MediaSessionCompat.Callback
    private lateinit var metadataBuilder: MediaMetadataCompat.Builder

    companion object {
        const val WIZ_MEDIA_ROOT_ID = "wiz_media_root"
        const val TAG = "WizMusicService"
        const val NOTIFICATION_ID = 1337
        var currentPosition: Int = 0
        var currentState: Int = 0
        var isRepeat = false
    }

    override fun onCreate() {
        info { "onCreate" }
        super.onCreate()
        initMediaPlayer()
        initMediaSession()
        initNoisyReceiver()
    }

    private fun initNoisyReceiver() {
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mNoisyReceiver, filter)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setVolume(1.0f, 1.0f)
    }

    private fun initMediaSession() {
        info { "initMediaSession" }
        mediaSession = MediaSessionCompat(
            this,
            TAG
        )
        mediaSession?.apply {
            setCallback(mediaSessionCallback)
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
        }
        sessionToken = mediaSession?.sessionToken
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(ArrayList())
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(WIZ_MEDIA_ROOT_ID, null)
    }

    override fun onDestroy() {
        warn { "onDestroy" }
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
        unregisterReceiver(mNoisyReceiver)
        mediaSession?.release()
        mediaPlayer.release()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        info { "Change in AudioFocus: $focusChange" }
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
                showPauseNotification()
                initPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer.pause()
                showPauseNotification()
                initPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer.setVolume(0.3f, 0.3f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
                mediaPlayer.setVolume(1.0f, 1.0f)
                showPlayingNotification()
                initPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }
        }
    }

    internal fun successfullyRetrievedAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = audioManager.requestAudioFocus(
            this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        )

        return result == AudioManager.AUDIOFOCUS_GAIN
    }

    internal fun initPlaybackState(state: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        playbackStateBuilder.setActions(
            PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackStateCompat.ACTION_STOP
        )
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        mediaSession?.setPlaybackState(playbackStateBuilder.build())

        if (state == PlaybackStateCompat.STATE_PAUSED) {
            currentPosition = mediaPlayer.currentPosition
        }

        currentState = state
    }

    private fun showPlayingNotification() {
        val builder = MediaStyleHelper.from(this, mediaSession)
        addPrev(builder, this)
        addPause(builder, this)
        addNext(builder, this)
        createNotification(this, mediaSession, builder)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    internal fun showPauseNotification() {
        val builder = MediaStyleHelper.from(this, mediaSession)
        addPrev(builder, this)
        addPlay(builder, this)
        addNext(builder, this)
        createNotification(this, mediaSession, builder)
    }

    private fun initMediaSessionMetadata(track: Track) {
        metadataBuilder = MediaMetadataCompat.Builder()

        Coil.load(this, track.cover_image) {
            target { drawable ->
                val resource = drawable.toBitmap()
                info { "Decoded album art" }
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, resource)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource)
                mediaSession?.setMetadata(metadataBuilder.build())
            }
        }

        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
            track.song
        )
        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
            track.artists
        )

        mediaSession?.setMetadata(metadataBuilder.build())

    }

    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }

    internal fun playTrack() {

        if (currentPosition > 0 && !mediaPlayer.isPlaying) {
            info { "Resuming at: $currentPosition" }
            mediaPlayer.seekTo(currentPosition)
            mediaPlayer.start()
            showPlayingNotification()
            initPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            return
        }

        initMediaSessionMetadata(WizMusicService.currentTrack)

        mediaSession?.isActive = true
        showPlayingNotification()

        mediaPlayer.reset()

        initPlaybackState(PlaybackStateCompat.STATE_CONNECTING)

        try {
            mediaPlayer.setDataSource(this, WizMusicService.currentTrack.url.toUri())
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.setOnBufferingUpdateListener { mediaPlayer, progress ->
                    info { "Buffering: $progress" }

                    if (progress != 100)
                        Toast.makeText(this, "Buffering: $progress", Toast.LENGTH_SHORT).show()

                    if (progress == 100 && currentState != PlaybackStateCompat.STATE_PAUSED) {
                        initPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                        mediaPlayer.start()
                    }
                }
                showPlayingNotification()
                currentPosition = 0

                mediaPlayer.setOnCompletionListener {
                    info { "Track completed: $isRepeat" }

                    if (isRepeat)
                        mediaPlayer.seekTo(0)
                    else
                        playNextTrack()
                }

                mediaPlayer.setOnErrorListener { mediaPlayer, what, extras ->
                    error { "MediaPlayer error: $what || $extras" }
                    true
                }
            }
        } catch (exception: Exception) {
            error { "Exception playing: ${WizMusicService.currentTrack.song} || ${exception.message}" }
        }

    }

    internal fun playNextTrack() {
        val index = WizMusicService.tracks.indexOf(WizMusicService.currentTrack)
        nextTrack = if (index == WizMusicService.tracks.size - 1) {
            WizMusicService.tracks.first()
        } else {
            WizMusicService.tracks[index + 1]
        }
        WizMusicService.currentTrack = nextTrack
        playTrack()
    }

    internal fun playPrevious() {
        val index = WizMusicService.tracks.indexOf(WizMusicService.currentTrack)
        if (index <= 0) {
            WizMusicService.currentTrack = WizMusicService.tracks.last()
            playTrack()
        } else {
            val nextTrack = WizMusicService.tracks[index - 1]
            WizMusicService.currentTrack = nextTrack
            playTrack()
        }
    }
}