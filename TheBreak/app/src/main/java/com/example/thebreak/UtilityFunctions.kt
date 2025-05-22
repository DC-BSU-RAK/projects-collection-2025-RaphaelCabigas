package com.example.thebreak

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils

fun btnAnim(context: Context, container: View) {
    AnimationUtils.loadAnimation(context, R.anim.scale_down).apply {
        container.startAnimation(this)
    }
}