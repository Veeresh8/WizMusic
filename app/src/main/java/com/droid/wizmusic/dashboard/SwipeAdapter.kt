package com.droid.wizmusic.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import coil.api.load
import com.airbnb.lottie.LottieAnimationView
import com.droid.wizmusic.R
import java.util.*

class SwipeAdapter internal constructor(context: Context, trackList: List<Track>) : PagerAdapter() {

    private val mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var trackList = ArrayList<Track>()

    init {
        this.trackList = trackList as ArrayList<Track>
    }

    override fun getCount(): Int {
        return trackList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = mLayoutInflater.inflate(R.layout.swipe_item, container, false)

        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        imageView.load(trackList[position].cover_image)
        val rightAnimation = itemView.findViewById<LottieAnimationView>(R.id.lottieAnimationSwipeRight)
        val leftAnimation = itemView.findViewById<LottieAnimationView>(R.id.lottieAnimationSwipeLeft)
        rightAnimation.playAnimation()
        rightAnimation.loop(true)
        leftAnimation.playAnimation()
        leftAnimation.loop(true)
        container.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}
