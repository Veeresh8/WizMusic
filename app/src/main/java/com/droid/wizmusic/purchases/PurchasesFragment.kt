package com.droid.wizmusic.purchases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.droid.wizmusic.*
import com.droid.wizmusic.dashboard.Dashboard
import com.droid.wizmusic.dashboard.Track
import com.droid.wizmusic.db.WizMusicDatabase
import com.droid.wizmusic.tracks.TracksAdapter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class PurchasesFragment : Fragment(), TracksAdapter.ClickInterface, AnkoLogger {

    private var recyclerView: RecyclerView? = null
    private var tracksAdapter: TracksAdapter? = null
    private var tvPurchaseHint: TextView? = null
    private var tvHint: TextView? = null
    private var lottieAnimation: LottieAnimationView? = null

    companion object {
        fun newInstance(): PurchasesFragment =
            PurchasesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_purchases, container, false)
        initUI(view)
        return view
    }

    private fun initUI(view: View) {
        recyclerView = view.findViewById(R.id.rvPurchasedTracks)
        tvPurchaseHint = view.findViewById(R.id.tvPurchaseHint)
        tvHint = view.findViewById(R.id.tvHint)
        lottieAnimation = view.findViewById(R.id.lottieAnimation)
        recyclerView?.layoutManager = withLinearLayout(activity)
        tracksAdapter = TracksAdapter(this)
        recyclerView?.adapter = tracksAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPurchases()
    }

    private fun checkPurchases() {
        WizMusicDatabase.instance?.run {
            this.tracksDAO().getPurchasedTracks()
                .observe(this@PurchasesFragment, Observer { purchasedTracks ->
                    info { "Purchases Tracks: ${purchasedTracks.size} || $purchasedTracks" }
                    purchasedTracks?.size?.run {
                        if (this > 0) {
                            tvPurchaseHint?.gone()
                            lottieAnimation?.gone()
                            tvHint?.visible()
                        } else {
                            tvPurchaseHint?.visible()
                            lottieAnimation?.visible()
                            tvHint?.gone()
                        }
                    }
                    tracksAdapter?.submitList(purchasedTracks)
                })
        }
    }

    override fun onClick(track: Track) {
        info { "Clicked: $track" }
        val dashboard = activity as Dashboard
        tracksAdapter?.currentList?.toMutableList()?.let { dashboard.playSong(track, it) }
    }

    override fun onBuy(track: Track) {

    }
}
