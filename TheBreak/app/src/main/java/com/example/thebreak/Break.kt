package com.example.thebreak
import androidx.annotation.DrawableRes

data class Break(
    val name: String,
    val description: String,
    @DrawableRes val image: Int
)