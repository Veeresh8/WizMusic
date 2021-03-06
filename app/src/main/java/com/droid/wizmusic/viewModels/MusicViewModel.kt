package com.droid.wizmusic.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.droid.wizmusic.dashboard.AddTrackResult
import com.droid.wizmusic.dashboard.Track
import com.droid.wizmusic.dashboard.TrackException
import com.droid.wizmusic.dashboard.TrackResult
import com.droid.wizmusic.db.WizMusicDatabase
import com.droid.wizmusic.network.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.lang.Exception

/*
* Using co-routines for I/O requests
* */


class MusicViewModel : ViewModel(), AnkoLogger {

    private lateinit var job: Job
    private var allTracks: MutableLiveData<TrackResult>? = null
    private var addTrackResult: MutableLiveData<AddTrackResult> = MutableLiveData()

    override fun onCleared() {
        super.onCleared()
        info { "Cancelling request" }
        job.cancel()
    }

    fun getTracks(): LiveData<TrackResult>? {
        if (allTracks == null) {
            allTracks = MutableLiveData()
            job = GlobalScope.launch(Dispatchers.IO) {
                getAllTracks()
            }
        }
        return allTracks
    }

    private suspend fun getAllTracks() {

        allTracks?.postValue(TrackResult.InProgress)

        try {
            val response =
                NetworkManager.getInstance()
                    ?.getAllTracksAsync("http://starlord.hackerearth.com/studio")
                    ?.await()

            response?.let { networkResponse ->
                if (networkResponse.isSuccessful) {
                    when (networkResponse.code()) {
                        200 -> {
                            allTracks?.postValue(
                                TrackResult.Success(
                                    networkResponse.body()
                                )
                            )
                        }
                        else -> {
                            allTracks?.postValue(
                                TrackResult.Error(
                                    TrackException(
                                        networkResponse.message(),
                                        networkResponse.code()
                                    )
                                )
                            )
                        }
                    }
                } else {
                    allTracks?.postValue(
                        TrackResult.Error(
                            TrackException(
                                networkResponse.message(),
                                networkResponse.code()
                            )
                        )
                    )
                }

            }
        } catch (exception: Exception) {
            allTracks?.postValue(
                TrackResult.Error(
                    TrackException(
                        exception.message
                    )
                )
            )
        }
    }

    fun saveTrack(track: Track): LiveData<AddTrackResult>? {
        info { "Saving track: $track" }
        track.apply {
            isPurchased = true
        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (WizMusicDatabase.instance?.tracksDAO()?.addTrack(track) != null) {
                    addTrackResult.postValue(AddTrackResult.Success)
                } else {
                    addTrackResult.postValue(
                        AddTrackResult.Error(
                            TrackException(
                                "Something went wrong adding ${track.song}"
                            )
                        )
                    )
                }
            } catch (exception: Exception) {
                addTrackResult.postValue(
                    AddTrackResult.Error(
                        TrackException(
                            exception.message
                        )
                    )
                )
            }
        }

        return addTrackResult
    }

}