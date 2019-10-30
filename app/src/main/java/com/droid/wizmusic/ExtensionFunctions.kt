package com.droid.wizmusic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun withLinearLayout(context: Context?): RecyclerView.LayoutManager? {
    return LinearLayoutManager(context)
}

fun toast(message: String, isShort: Boolean = true) {
    if (isShort)
        Toast.makeText(Application.instance, message, Toast.LENGTH_SHORT).show()
    else
        Toast.makeText(Application.instance, message, Toast.LENGTH_LONG).show()
}

fun updateFragment(activity: AppCompatActivity, toHide: Fragment, toShow: Fragment) {
    val transaction = activity.supportFragmentManager.beginTransaction()
    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
    transaction.hide(toHide).show(toShow)
    transaction.commit()
}

fun addFragment(activity: AppCompatActivity, toAdd: Fragment, toHide: Fragment, tag: String) {
    activity.supportFragmentManager.beginTransaction().add(R.id.container, toAdd, tag)
        .hide(toHide).commit()
}

fun getPlaystoreIntent(): Intent {
    return try {
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.droid.netflixIMDB"))
    } catch (exception: Exception) {
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=com.droid.netflixIMDB")
        )
    }
}

fun openGithub(context: Context) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/Veeresh8")
            )
        )
    } catch (exception: Exception) {
        Log.e("Github Launch", "Exception launching - ${exception.message}")
    }
}

fun openLinkedIn(context: Context) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.linkedin.com/in/veeresh-charantimath-056176b6/")
            )
        )
    } catch (exception: Exception) {
        Log.e("LinkedIn Launch", "Exception launching - ${exception.message}")
    }
}

fun getReadableTime(milliseconds: Long): String {
    var finalTimerString = ""
    val secondsString: String

    val hours = (milliseconds / (1000 * 60 * 60)).toInt()
    val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
    val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
    if (hours > 0) {
        finalTimerString = "$hours:"
    }

    secondsString = if (seconds < 10) {
        "0$seconds"
    } else {
        "" + seconds
    }

    finalTimerString = "$finalTimerString$minutes:$secondsString"

    return finalTimerString
}