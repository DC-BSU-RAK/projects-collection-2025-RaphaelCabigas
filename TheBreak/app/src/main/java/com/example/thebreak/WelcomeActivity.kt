package com.example.thebreak

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Does an animation on click and set change prefs to this is not the user's first time and start new activity
        findViewById<ImageButton>(R.id.welcomeBtn).setOnClickListener { btn ->
                btnAnim(this@WelcomeActivity, btn)
                val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                prefs.edit { putBoolean("not_first_time", true) }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }