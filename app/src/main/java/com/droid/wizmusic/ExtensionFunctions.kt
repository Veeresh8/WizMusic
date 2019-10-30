package com.droid.wizmusic

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


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

