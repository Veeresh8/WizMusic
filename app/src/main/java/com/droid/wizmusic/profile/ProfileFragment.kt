package com.droid.wizmusic.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.droid.wizmusic.R
import com.droid.wizmusic.getPlaystoreIntent
import com.droid.wizmusic.openGithub
import com.droid.wizmusic.openLinkedIn

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance(): ProfileFragment =
            ProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        initUI(view)
        return view
    }

    private fun initUI(view: View?) {
        val playStore = view?.findViewById<TextView>(R.id.tvPlayStore)
        playStore?.setOnClickListener {
            startActivity(getPlaystoreIntent())
        }
        val github = view?.findViewById<TextView>(R.id.tvGithub)
        github?.setOnClickListener {
            activity?.let { it1 -> openGithub(it1) }
        }
        val linkdein = view?.findViewById<TextView>(R.id.tvLinkedin)
        linkdein?.setOnClickListener {
            activity?.let { it1 -> openLinkedIn(it1) }
        }

    }
}