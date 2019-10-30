package com.droid.wizmusic.dashboard

import android.animation.Animator
import android.os.Bundle
import android.view.MenuItem
import android.widget.SeekBar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import coil.api.load
import com.droid.wizmusic.*
import com.droid.wizmusic.profile.ProfileFragment
import com.droid.wizmusic.purchases.PurchasesFragment
import com.droid.wizmusic.service.BaseMusicPlayer
import com.droid.wizmusic.service.WizMusicService
import com.droid.wizmusic.tracks.TracksFragment
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.player.*
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.util.*

/*
* Contains UI, listening events from it's parent
* */
class Dashboard : BaseMediaActivity() {

    private var tracksFragment = TracksFragment.newInstance()
    private var purchasesFragment = PurchasesFragment.newInstance()
    private var profileFragment = ProfileFragment.newInstance()
    private lateinit var active: Fragment
    private var currentState: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_dashboard)
        super.onCreate(savedInstanceState)
        initBottomView()
        initSwipeView()
        initClicks()
    }

    override fun onBackPressed() {
        if (currentState == motionLayout?.endState) {
            motionLayout?.transitionToStart()
        } else {
            super.onBackPressed()
        }
    }

    private fun initSwipeView() {
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

            override fun allowsTransition(p0: MotionScene.Transition?): Boolean {
                return true
            }

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                currentState = motionLayout?.currentState ?: 0
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                info { "Transition Changed" }
            }
        })

        swipeContainer.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                info { "Swiped to: $position" }
                if (WizMusicService.tracks.indexOf(WizMusicService.currentTrack) == position) {
                    return
                }
                WizMusicService.currentTrack = WizMusicService.tracks[position]
                BaseMusicPlayer.currentPosition = 0
                mediaController.transportControls.play()
            }
        })
    }

    private fun initBottomView() {

        addFragment(this, profileFragment, profileFragment, "3")
        addFragment(this, purchasesFragment, purchasesFragment, "2")
        supportFragmentManager.beginTransaction().add(R.id.container, tracksFragment, "1").commit()

        active = tracksFragment

        bottomView.setOnNavigationItemSelectedListener { item: MenuItem ->
            return@setOnNavigationItemSelectedListener when (item.itemId) {
                R.id.navigation_tracks -> {
                    updateFragment(this, active, tracksFragment)
                    active = tracksFragment
                    true
                }
                R.id.navigation_purchases -> {
                    updateFragment(this, active, purchasesFragment)
                    active = purchasesFragment
                    true
                }
                R.id.navigation_profile -> {
                    updateFragment(this, active, profileFragment)
                    active = profileFragment
                    true
                }
                else -> false
            }
        }

        currentState = motionLayout.currentState

        lottieAnimationSwipe.playAnimation()
        lottieAnimationSwipe.loop(true)

    }

    fun performPurchaseAnimation() {
        supportFragmentManager.beginTransaction().hide(active).commit()
        lottieAnimationMain.visible()
        animationLayout.visible()
        toast("Purchase Successful")
        lottieAnimationMain.playAnimation()
        lottieAnimationMain.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                lottieAnimationMain.gone()
                animationLayout.gone()
                supportFragmentManager.beginTransaction().hide(active).show(purchasesFragment)
                    .commit()
                active = purchasesFragment
                bottomView.selectedItemId = R.id.navigation_purchases
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        })
    }

    fun playSong(track: Track, currentList: List<Track>) {
        WizMusicService.tracks = currentList.toMutableList()
        WizMusicService.currentTrack = track
        mediaController.transportControls.play()
        if (currentState == motionLayout?.startState || currentState == 0) {
            motionLayout?.transitionToEnd()
        }
    }

    override fun updateTrackName(trackName: String) {
        tvTrackNamePlayer.text = trackName
        tvTrackNameFull.text = trackName
    }

    override fun updatePlayPause(isPaused: Boolean) {
        if (isPaused) {
            ivPlayFull.setImageResource(R.drawable.play_arrow_24px)
            ivPlaySub.setImageResource(R.drawable.play_arrow_24px)

            ivPlaySub.tag = "paused"
            ivPlayFull.tag = "paused"
        } else {
            ivPlayFull.setImageResource(R.drawable.pause_24px)
            ivPlaySub.setImageResource(R.drawable.pause_24px)

            ivPlaySub.tag = "playing"
            ivPlayFull.tag = "playing"
        }

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isFinishing)
                    return

                runOnUiThread {
                    val currentProgress =
                        WizMusicService.instance?.mediaPlayer?.currentPosition ?: 0
                    seekBar.progress = currentProgress

                    tvEndTime.text =
                        WizMusicService.instance?.mediaPlayer?.duration?.toLong()?.let {
                            getReadableTime(
                                it
                            )
                        }

                    tvStartTime.text =
                        WizMusicService.instance?.mediaPlayer?.currentPosition?.toLong()?.let {
                            getReadableTime(
                                it
                            )
                        }
                }
            }
        }, 0, 1000)
    }

    override fun updateCoverArt(url: String) {
        ivTrackCover.load(url)
        updateSwipeView()
    }

    private fun updateSwipeView() {
        info { "Update swipe view" }

        val adapter = SwipeAdapter(this, WizMusicService.tracks)
        swipeContainer.adapter = adapter
        swipeContainer.currentItem = WizMusicService.tracks.indexOf(WizMusicService.currentTrack)
    }

    override fun updateArtistName(artistName: String) {
        tvArtistNameFull.text = artistName
    }


    override fun updateSeekbar(total: Int) {
        info { "Total duration: $total" }
        seekBar.max = total
    }

    private fun initClicks() {
        ivPlayFull.setOnClickListener {
            if (WizMusicService.tracks.isEmpty())
                return@setOnClickListener

            val tag = it.tag
            if (tag == "paused")
                mediaController.transportControls.play()
            else
                mediaController.transportControls.pause()
        }

        ivPlaySub.setOnClickListener {
            val tag = it.tag
            if (tag == "paused")
                mediaController.transportControls.play()
            else
                mediaController.transportControls.pause()
        }

        ivNextSub.setOnClickListener {
            if (WizMusicService.tracks.isEmpty())
                return@setOnClickListener
            mediaController.transportControls.skipToNext()
        }

        ivPrev.setOnClickListener {
            if (WizMusicService.tracks.isEmpty())
                return@setOnClickListener
            mediaController.transportControls.skipToPrevious()
        }

        ivNextFull.setOnClickListener {
            if (WizMusicService.tracks.isEmpty())
                return@setOnClickListener
            mediaController.transportControls.skipToNext()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, position: Int, fromUser: Boolean) {
                if (fromUser) mediaController.transportControls.seekTo(position.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        ivRepeat.setOnClickListener {
            if (WizMusicService.tracks.isEmpty())
                return@setOnClickListener

            val tag = it.tag
            if (tag == "on") {
                it.tag = "off"
                it.alpha = 0.5F
                BaseMusicPlayer.isRepeat = false
                toast("Repeat OFF")
            } else {
                it.tag = "on"
                it.alpha = 1F
                BaseMusicPlayer.isRepeat = true
                toast("Repeat ON")
            }
        }

        ivShuffle.setOnClickListener {
            if (WizMusicService.tracks.isEmpty())
                return@setOnClickListener

            val tag = it.tag
            if (tag == "on") {
                it.tag = "off"
                it.alpha = 0.5F
                WizMusicService.tracks.clear()
                WizMusicService.tracks.addAll(WizMusicService.tracksHolder)
                toast("Shuffle OFF")
            } else {
                it.tag = "on"
                it.alpha = 1F
                WizMusicService.tracksHolder.clear()
                WizMusicService.tracksHolder.addAll(WizMusicService.tracks)
                WizMusicService.tracks.shuffle()
                toast("Shuffle ON")
            }

            updateSwipeView()
        }
    }
}
