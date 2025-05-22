package com.example.thebreak

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var instructionBtn: ImageButton
    private lateinit var breaksBtn: ImageButton
    private lateinit var timerTxt: TextView
    private lateinit var editBtn: ImageButton
    private lateinit var startBtn: ImageButton

    private var initialTime: Long = 0
    private var remainingTime: Long = initialTime
    private var countingDown = false
    private var paused = false

    private lateinit var breakTimer: CountDownTimer

    // Initialize the user prefs so that we won't need to call it again
    private val prefs by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    private val breakSuggestions = listOf(
        R.drawable.popup_silence,
        R.drawable.popup_eye,
        R.drawable.popup_game,
        R.drawable.popup_plant,
        R.drawable.popup_palette,
        R.drawable.popup_language,
        R.drawable.popup_journal,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // When the user presses the back button just move the task to the background instead of exiting making sure the app is still running
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timerTxt = findViewById(R.id.timerTxt)
        editBtn = findViewById(R.id.editBtn)
        startBtn = findViewById(R.id.startBtn)
        instructionBtn = findViewById(R.id.instructionBtn)
        breaksBtn = findViewById(R.id.breaksBtn)

        instructionBtn.setOnClickListener {
            instructionBtn.isEnabled = false
            btnAnim(this@MainActivity, instructionBtn)
            dialogPopUp(R.drawable.instructions_img)
        }

        breaksBtn.setOnClickListener {
            btnAnim(this@MainActivity, breaksBtn)
            startActivity(Intent(this, BreaksActivity::class.java))
        }

        initialTime = prefs.getLong("initial_time", 25 * 60 * 1000)
        updateTimer()

        // Timer start/resume and pause
        startBtn.setOnClickListener {
            btnAnim(this@MainActivity, startBtn)
            when {
                !countingDown -> startTimer()
                countingDown && !paused -> pauseTimer()
            }
        }

        // Timer edit or reset logic
        editBtn.setOnClickListener {
            btnAnim(this@MainActivity, editBtn)
            when {
                !countingDown && !paused -> editTimer()
                !countingDown && paused -> resetTimer()
            }
        }
    }

    // Update Timer Text Utility
    private fun updateTimer() {
        remainingTime = initialTime
        timerTxt.text = formatTime(initialTime)
    }

    // Animate Edit Button in & Out
    private fun animateEditBtn(animation: Int, visibility: Int) {
        val anim = AnimationUtils.loadAnimation(this, animation)
        editBtn.startAnimation(anim)
        editBtn.visibility = visibility
    }

    // Format time to hours, minutes, seconds
    private fun formatTime(remaining: Long): String {
        // 1000ms multiply by 60 secs and 60 mins, within 24 hours
        val hours = (remaining / (1000 * 60 * 60)) % 24
        // 1000ms multiply by 60 mins, within 1 hour
        val minutes = (remaining / (1000 * 60)) % 60
        // 1000ms multiply by 60 secs, within 1 minute
        val seconds = (remaining / 1000) % 60
        // Format the string like HH:MM:SS
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun dialogPopUp(img: Int) {
        // create a dialog window
        Dialog(this).apply {
            // The dialog can be dismiss through the back button
            setCancelable(true)
            // Sets the background to an darken overlay
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setContentView(R.layout.activity_instructions)
            // Make the layout only fit the content
            window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            // Do a pop-up animation on the window
            window?.attributes?.windowAnimations = R.style.popUpAnimation

            // Change the dialog image with the corresponding img
            findViewById<ImageView>(R.id.instructionsImg).setImageResource(img)
            // Add a listener to the while window that closes the dialog
            findViewById<ConstraintLayout>(R.id.instructionsRoot).setOnClickListener {
                dismiss()
            }

            // Make the instructions button clickable again and do the ui sound
            setOnDismissListener {
                instructionBtn.isEnabled = true
            }

            // show the dialog
            show()
        }
    }

    // Start countdown
    private fun startTimer() {
        // Makes sure to cancel any overlapping timers before this one
        if (::breakTimer.isInitialized) breakTimer.cancel()
        // Initialize the timer that goes down every second
        breakTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(remaining: Long) {
                // store the remaining time and update the timer text
                remainingTime = remaining
                timerTxt.text = formatTime(remainingTime)
            }

            override fun onFinish() {
                // Show random break image
                val breakRandom = breakSuggestions.random()
                dialogPopUp(breakRandom)
                revertTimer()
            }
        }.start()

        // Timer is counting down and not paused
        countingDown = true
        paused = false
        // Change the buttons with the appropriate images
        startBtn.setImageResource(R.drawable.timer_pause_btn)
        editBtn.setImageResource(R.drawable.timer_reset_btn)
        animateEditBtn(R.anim.scale_fade_out, ImageButton.GONE)
    }

    private fun revertTimer() {
        // Reset the countingDown and paused flags
        countingDown = false
        paused = false
        // Revert the remaining and timer text back to initial time
        updateTimer()
        // Revert start and edit button to original
        startBtn.setImageResource(R.drawable.timer_start_btn)
        editBtn.setImageResource(R.drawable.timer_edit_btn)
            animateEditBtn(R.anim.scale_fade_in, ImageButton.VISIBLE)
    }

    // Reset timer to default
    private fun resetTimer() {
        breakTimer.cancel()
        revertTimer()
    }

    // Pause the timer
    private fun pauseTimer() {
        breakTimer.cancel()
        countingDown = false
        paused = true
        startBtn.setImageResource(R.drawable.timer_continue_btn)
        animateEditBtn(R.anim.scale_fade_in, ImageButton.VISIBLE)
    }


    private fun editTimer() {
        val totalSecs = initialTime / 1000

        // Initialize the clock's values
        val clock = MaterialTimePicker.Builder()
            // Uses 24 hours format like 12:30
            .setTimeFormat(CLOCK_24H)
            // Set the timer editor's values as the initial time, hour and mins
            .setHour((totalSecs / 3600).toInt())
            .setMinute(((totalSecs % 3600) / 60).toInt())
            .setTitleText("Set Break Timer Duration")
            .setTheme(R.style.timerTheme)
            .build()

        // Display the timer picker fragment on screen
        clock.show(supportFragmentManager, "timer_picker")

        // Validates the user's preference for the hour and minutes selected for the timer
        clock.addOnPositiveButtonClickListener {
            initialTime = ((clock.hour * 3600) + (clock.minute * 60)) * 1000L
            updateTimer()
            // Store the preferred timer
            prefs.edit { putLong("initial_time", initialTime) }
        }
    }
}