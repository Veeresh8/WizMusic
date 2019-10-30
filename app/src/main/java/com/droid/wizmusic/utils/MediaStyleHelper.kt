package com.droid.wizmusic.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import android.content.Intent
import android.app.PendingIntent
import com.droid.wizmusic.R
import com.droid.wizmusic.dashboard.Dashboard
import com.droid.wizmusic.service.BaseMusicPlayer


/*
* Helper class for notification building
* */

object MediaStyleHelper {

    private const val NOTIFICATION_CHANNEL = "666"

    fun from(
        context: Context, mediaSession: MediaSessionCompat?
    ): NotificationCompat.Builder {
        val controller = mediaSession?.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description
        val title = description?.title
        val artist = description?.subtitle

        createNotificationChannel(context)

        val notificationIntent = Intent(context, Dashboard::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(
            context,
            NOTIFICATION_CHANNEL
        )
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(description?.iconBitmap)
            .setSmallIcon(R.drawable.ic_stat_music_note)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
            )
    }

    fun addNext(builder: NotificationCompat.Builder, context: Context) {
        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_fast_forward_black_48dp,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )
        )
    }

    fun addPause(builder: NotificationCompat.Builder, context: Context) {
        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_pause_black_48dp,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        )
    }

    fun addPlay(builder: NotificationCompat.Builder, context: Context) {
        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_play_arrow_black_48dp,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        )
    }

    fun addPrev(builder: NotificationCompat.Builder, context: Context) {
        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_fast_rewind_black_48dp,
                "Prev",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
        )
    }

    fun createNotification(
        context: Context,
        mediaSession: MediaSessionCompat?,
        builder: NotificationCompat.Builder
    ) {

        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(
                0,
                1,
                2
            ).setMediaSession(
                mediaSession?.sessionToken
            )
        )

        NotificationManagerCompat.from(context)
            .notify(BaseMusicPlayer.NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}