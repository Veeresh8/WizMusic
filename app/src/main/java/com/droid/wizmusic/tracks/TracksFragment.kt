package com.droid.wizmusic.tracks

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.airbnb.lottie.LottieAnimationView
import com.droid.wizmusic.*
import com.droid.wizmusic.dashboard.AddTrackResult
import com.droid.wizmusic.dashboard.Dashboard
import com.droid.wizmusic.dashboard.Track
import com.droid.wizmusic.dashboard.TrackResult
import com.droid.wizmusic.db.WizMusicDatabase
import com.droid.wizmusic.viewModels.MusicViewModel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class TracksFragment : Fragment(), TracksAdapter.ClickInterface, AnkoLogger {

    private var recyclerView: RecyclerView? = null
    private var tracksAdapter: TracksAdapter? = null
    private var lottieAnimation: LottieAnimationView? = null
    private var musicViewModel: MusicViewModel? = null

    companion object {
        fun newInstance(): TracksFragment =
            TracksFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tracks, container, false)
        initUI(view)
        return view
    }

    private fun initUI(view: View) {
        recyclerView = view.findViewById(R.id.rvTracks)
        lottieAnimation = view.findViewById(R.id.lottieAnimation)
        recyclerView?.layoutManager = withLinearLayout(activity)
        tracksAdapter = TracksAdapter(this)
        recyclerView?.adapter = tracksAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTracks()
    }

    private fun initTracks() {
        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)
        musicViewModel?.getTracks()?.observe(this, Observer { result ->
            when (result) {
                is TrackResult.Success -> {
                    info { "Tracks: ${result.tracks?.size} || ${result.tracks}" }
                    bindTracks(result.tracks)
                }
                is TrackResult.Error -> {
                    toast("Error fetching tracks: ${result.trackException.message} || ${result.trackException.code}")
                    info { "Error fetching tracks: ${result.trackException.message} || ${result.trackException.code}" }
                }
                is TrackResult.InProgress -> {
                    info { "Fetching Tracks" }
                    lottieAnimation?.playAnimation()
                    recyclerView?.gone()
                }
            }
        })
    }

    private fun bindTracks(tracks: List<Track>?) {
        tracksAdapter?.submitList(tracks?.toMutableList())
        checkPurchasedTracks()
        Handler().postDelayed({
            lottieAnimation?.gone()
            recyclerView?.visible()
        }, 1500)

    }

    override fun onClick(track: Track) {
        info { "Clicked: $track" }
        val dashboard = activity as Dashboard
        tracksAdapter?.currentList?.toMutableList()?.let { dashboard.playSong(track, it) }
    }

    override fun onBuy(track: Track) {
        info { "Clicked Buy: $track" }
        activity?.run {
            MaterialDialog(this).show {
                title(R.string.purchase_title)
                cancelable(true)
                message(text = track.song)
                positiveButton(text = "Confirm") {
                    musicViewModel?.saveTrack(track)
                        ?.observe(this@TracksFragment, Observer { result ->
                            when (result) {
                                is AddTrackResult.Success -> {
                                    info { "Saved track: ${track.song}" }
                                    val dashboard = this@run as Dashboard
                                    dashboard.performPurchaseAnimation()
                                    checkPurchasedTracks()
                                }
                                is AddTrackResult.Error -> {
                                    toast("Failed to add ${track.song}, ${result.trackException.message}")
                                    info { "Failed to add ${track.song} || ${result.trackException.message} || ${result.trackException.code}" }
                                }
                            }
                        })
                }
            }
        }
    }

    private fun checkPurchasedTracks() {
        WizMusicDatabase.instance?.run {
            this.tracksDAO().getPurchasedTracks()
                .observe(this@TracksFragment, Observer { purchasedTracks ->
                    val currentTracks = tracksAdapter?.currentList
                    val filteredTracks = currentTracks?.filterNot { track -> inPurchases(track, purchasedTracks) }
                    tracksAdapter?.submitList(filteredTracks)
                })
        }
    }

    private fun inPurchases(track: Track, purchasedTracks: List<Track>): Boolean {
        purchasedTracks.forEach {
            if (it.url == track.url)
                return true
        }
        return false
    }
}
