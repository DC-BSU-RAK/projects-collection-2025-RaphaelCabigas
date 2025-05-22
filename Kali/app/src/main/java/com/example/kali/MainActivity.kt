package com.example.kali

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.constraintlayout.helper.widget.Flow
import androidx.core.view.isVisible
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.content.edit
import android.media.SoundPool
import android.os.Build
import androidx.annotation.RequiresApi

class MainActivity : AppCompatActivity() {

    private lateinit var pinBoxes: List<ImageView>
    private lateinit var pinContainer: Flow
    private lateinit var playContainer: Flow
    private lateinit var modeBtn: ImageButton
    private lateinit var instructionsBtn: ImageButton
    private lateinit var holeImg: ImageView
    private lateinit var leftBtn: ImageButton
    private lateinit var rightBtn: ImageButton

    // Pin input position and maximum
    private var pinPosition = 0
    private val pinMaximum = 4
    // Customize hole position
    private var holePosition = 0
    // Tracks what the user has unlocked
    private val unlockedImgs = mutableMapOf<Int, Int>()
    // Stores pets to show in Play mode
    private val playImgs = mutableListOf<Int>()
    // Entered pin combinantion storing image PINs
    private val enteredPin = mutableListOf<Int>()

    private val noteIds = listOf(
        R.id.note_1, R.id.note_2, R.id.note_3,
        R.id.note_4, R.id.note_5, R.id.note_6,
        R.id.note_7, R.id.note_8, R.id.note_9
    )

    private val noteSounds = listOf(
        R.raw.note_1_c4, R.raw.note_2_a3, R.raw.note_3_f3,
        R.raw.note_4_d3, R.raw.note_5_c3, R.raw.note_6_e3,
        R.raw.note_7_g3, R.raw.note_8_b3, R.raw.note_9_d4
    )

    private val pinA = R.drawable.pin_a
    private val pinB = R.drawable.pin_b
    private val pinC = R.drawable.pin_c
    private val pinD = R.drawable.pin_d
    private val pinE = R.drawable.pin_e
    private val pinF = R.drawable.pin_f
    private val pinG = R.drawable.pin_g
    private val pinH = R.drawable.pin_h
    private val pinI = R.drawable.pin_i


    private val pinLetters = listOf(
        pinA,pinB,pinC,pinD,pinE,pinF,pinG,pinH,pinI
    )

    private val validPinPets = listOf(
        Pair(listOf(pinF, pinF, pinF, pinF), R.drawable.pet_1), // FFFF
        Pair(listOf(pinA, pinB, pinC, pinD), R.drawable.pet_2), // ABCD
        Pair(listOf(pinH, pinI, pinH, pinI), R.drawable.pet_3), // HIHI
        Pair(listOf(pinG, pinD, pinE, pinG), R.drawable.pet_4), // GDEG
        Pair(listOf(pinA, pinH, pinG, pinA), R.drawable.pet_5), // AHGA
        Pair(listOf(pinA, pinG, pinF, pinE), R.drawable.pet_6), // AGFE
        Pair(listOf(pinB, pinG, pinD, pinH), R.drawable.pet_7), // BGDH
        Pair(listOf(pinI, pinA, pinH, pinB), R.drawable.pet_8), // IAHB
        Pair(listOf(pinE, pinE, pinA, pinG), R.drawable.pet_9), // EEAG
    )

    private val rewardPopup = listOf(
        R.drawable.reward_pet_1,R.drawable.reward_pet_2,R.drawable.reward_pet_3,
        R.drawable.reward_pet_4,R.drawable.reward_pet_5,R.drawable.reward_pet_6,
        R.drawable.reward_pet_7,R.drawable.reward_pet_8,R.drawable.reward_pet_9
    )

    private val pet0 = R.drawable.pet_0

    // Initialize the user prefs so that we won't need to call it again
    private val prefs by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    private lateinit var soundPool: SoundPool
    private lateinit var noteBtns: List<Button>
    // Loads and plays the mapped sounds
    private val soundMap = mutableMapOf<Int, Int>()
    // Manages multi-touch input
    private val pointerToNoteMap = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // For 0.9s display the splash screen
        Thread.sleep(900)
        installSplashScreen()

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            // Hide the system bars e.g. status and navigation making the app fill the whole screen
            hide(WindowInsetsCompat.Type.systemBars())
            // and when the user swipes subtly within that area of bars it will display transparently
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContentView(R.layout.activity_main)

        // Set the maximum sounds for the sound pool
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .build()

        // Initialize all of the note and ui sounds
        noteSounds.forEach { note ->
            val sound = soundPool.load(this, note, 1)
            soundMap[note] = sound
        }
        soundMap[R.raw.button_click] = soundPool.load(this, R.raw.button_click, 1)
        soundMap[R.raw.button_close] = soundPool.load(this, R.raw.button_close, 1)

        pinContainer = findViewById(R.id.pinContainer)
        playContainer = findViewById(R.id.playContainer)
        modeBtn = findViewById(R.id.modeBtn)
        instructionsBtn = findViewById(R.id.instructionsBtn)
        // map the corresponding button view to the list
        noteBtns = noteIds.map { findViewById(it) }

        pinBoxes = listOf(
            findViewById(R.id.pin_1),
            findViewById(R.id.pin_2),
            findViewById(R.id.pin_3),
            findViewById(R.id.pin_4)
        )

        instructionsBtn.apply {
            setOnClickListener {
            secondaryBtnAnim(this)
            uiSound(R.raw.button_click)

            // Checks if the phone is landscape or portrait then store the approriate instruction image to it
            val instruction = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                R.drawable.landscape_instructions
            } else {
                R.drawable.portrait_instructions
            }
            dialogPopup(instruction)
            // And disable it making sure it doesn't get called twice
            instructionsBtn.isEnabled = false
        }}

        modeBtn.setOnClickListener {
            secondaryBtnAnim(modeBtn)
            uiSound(R.raw.button_click)
            showPinMode()
        }

        holeImg = findViewById(R.id.holeImg)
        leftBtn = findViewById(R.id.leftBtn)
        rightBtn = findViewById(R.id.rightBtn)

        // Initialize the default pet image
        playImgs.add(pet0)
        holeImg.setImageResource(pet0)
        unlockedImgs[-1] = pet0
        loadUnlockedPets()

        // If the play mode images is greater than 1 and it's visible
        leftBtn.setOnClickListener {
            if (playImgs.size > 1 && playContainer.isVisible) {
                secondaryBtnAnim(leftBtn)
                uiSound(R.raw.button_click)
                // moves to the previous pet
                holePosition = (holePosition - 1 + playImgs.size) % playImgs.size
                holeImg.setImageResource(playImgs[holePosition])
                prefs.edit {
                    putInt("current_pet", holePosition)
                }
            }
        }
        rightBtn.setOnClickListener {
            if (playImgs.size > 1 && playContainer.isVisible) {
                secondaryBtnAnim(rightBtn)
                uiSound(R.raw.button_click)
                // moves to the next pet
                holePosition = (holePosition + 1) % playImgs.size
                holeImg.setImageResource(playImgs[holePosition])
                prefs.edit {
                    putInt("current_pet", holePosition)
                }
            }
        }
    }

    // Load Unlocked Pets from prefs
    private fun loadUnlockedPets() {
        // Get the string set of unlocked_pets
        // If theres not unlocked_pets initialized then just set an empty set
        val unlockedPrefs = prefs.getStringSet("unlocked_pets", emptySet()) ?: emptySet()

        // For each pin from the unlocked_pets
        unlockedPrefs.forEach { string ->
            // Set each string to an integer if there's nothing make sure the app doesn't crash by making it null
            val pin = string.toIntOrNull()
            // If the pin is not empty and the pin is a valid pin found in validPinPets
            if (pin != null && pin in validPinPets.indices) {
                // Get the pet image
                val petImg = validPinPets[pin].second
                // Set the pet image to the corresponding unlocked images
                unlockedImgs[pin] = petImg
                // If the images from play mode doesn't contain the corresponding pet image when add it back
                if (!playImgs.contains(petImg)) {
                    playImgs.add(petImg)
                }
            }
        }
        if (playImgs.size <= 1) {
            leftBtn.visibility = View.GONE
            rightBtn.visibility = View.GONE
        } else {
            leftBtn.visibility = View.VISIBLE
            rightBtn.visibility = View.VISIBLE
        }
        // Get the saved current pet and displaye it
        val currentPetPrefs = prefs.getInt("current_pet", 0)
        if (currentPetPrefs in playImgs.indices) {
            holePosition = currentPetPrefs
            holeImg.setImageResource(playImgs[holePosition])
        }
    }

    // Button Sounds Utility Function
    private fun uiSound(soundFile: Int) {
        // For the corresponding sound file from the map plat it
        soundMap[soundFile]?.let { id ->
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }
    // Secondary Buttons Utility Animation Function
    private fun secondaryBtnAnim(container: View) {
        AnimationUtils.loadAnimation(this, R.anim.scale_down).apply {
            container.startAnimation(this)
        }
    }
    // Container In & Out Animation Function
    private fun containerEnter(container: Flow) {
        // Loop through each item inside the flow
        container.referencedIds.forEach { item ->
            val view = findViewById<View>(item)
            view.visibility = View.VISIBLE
            // If the available pet images for the play mode is less than 1
            // make sure the left and right button are not visible
            if (playImgs.size <= 1) {
                leftBtn.visibility = View.GONE
                rightBtn.visibility = View.GONE
            }
           AnimationUtils.loadAnimation(this, R.anim.scale_fade_in).apply {
               view.startAnimation(this)
           }
        }
    }
    private fun containerOut(container: Flow) {
        container.referencedIds.forEach { item ->
            val view = findViewById<View>(item)
            AnimationUtils.loadAnimation(this, R.anim.scale_fade_out).apply {
                // When the animation ends set the visibility to gone making sure the animation runs smoothly
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        view.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }.also{view.startAnimation(it)}
        }
    }
    // Dialog Popup Utility Function
    private fun dialogPopup(img: Int) {
        // create a dialog window
        Dialog(this).apply {
            // The dialog can be dismiss through the back button
            setCancelable(true)
            // Sets the background to an darken overlay
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setContentView(R.layout.activity_dialog)
            // Make the layout only fit the content
            window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            // Do a pop-up animation on the window
            window?.attributes?.windowAnimations = R.style.popUpAnimation

            // Change the dialog image with the corresponding img
            findViewById<ImageView>(R.id.dialogImg).setImageResource(img)
            // Add a listener to the while window that closes the dialog
            findViewById<ConstraintLayout>(R.id.instructionsRoot).setOnClickListener {
                dismiss()
            }

            // Make the instructions button clickable again and do the ui sound
            setOnDismissListener {
                instructionsBtn.isEnabled = true
                uiSound(R.raw.button_close)
            }

            // show the dialog
            show()
        }
    }

    // Trigger the ripple effect for the corresponding button when touch happens
    private fun triggerRipple(btn: View, finger: MotionEvent) {
        // Check the button's background can show ripple effect
        btn.background?.apply {
            // Get the (x, y) coordinates of corresponding button on the screen
            val location = IntArray(2)
            btn.getLocationOnScreen(location)
            // Then subtract it by the finger's position to get the exact finger position
            val x = finger.rawX - location[0]
            val y = finger.rawY - location[1]
            // Set the ripple's origin of the finger in the corresponding button
            setHotspot(x, y)
            // Activate the button's state is in pressed and enabled mode simulating click interaction
            state = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
            Handler(Looper.getMainLooper()).postDelayed({
                // Reset the state after 0.2s
                state = intArrayOf()}, 200)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun dispatchTouchEvent(finger: MotionEvent): Boolean {
        // Checks if the user's finger/s move, first finger touches, or another finger touches the screen
        when (finger.actionMasked)
        {MotionEvent.ACTION_MOVE,
        MotionEvent.ACTION_DOWN,
        MotionEvent.ACTION_POINTER_DOWN -> {

            // The number of fingers on screen
            val count = finger.pointerCount

            // Loops through all the fingers
            for (i in 0 until count) {
                // Get the corresponding x and y position of the user's finger/s
                val x = finger.getRawX(i).toInt()
                val y = finger.getRawY(i).toInt()


                // Get the corresponding id for the finger
                val id = finger.getPointerId(i)
                var fingerOnNote = false

                // For each note buttons view
                noteBtns.forEachIndexed { index, btn ->
                    // Get the (x, y) coordinates of corresponding button on the screen
                    val location = IntArray(2)
                    btn.getLocationOnScreen(location)
                    // Calculate the width and height of the button starting from top left
                    val left = location[0]
                    val top = location[1]
                    val right = left + btn.width
                    val bottom = top + btn.height

                    // If the finger is within the button area
                    if (x in left..right && y in top..bottom) {
                        fingerOnNote = true
                        val previousNote = pointerToNoteMap[id]
                        // Only play the sound if it's a different note than last time
                        if (previousNote != index) {
                            uiSound(noteSounds[index])
                            // Update the lastPlayedNote index
                            pointerToNoteMap[id] = index
                            // Trigger ripple animations
                            triggerRipple(btn, finger)
                            AnimationUtils.loadAnimation(this, R.anim.tine_bounce).apply {
                                btn.startAnimation(this)
                            }
                            // Check if the user is in PIN mode and there's still space for pin inputs then validate the pin
                            if (pinContainer.isVisible && pinPosition < pinMaximum) {
                                validatePin(pinLetters[index])
                            }
                        }
                    }
                }
                if (!fingerOnNote) {
                    pointerToNoteMap.remove(id)
                }
            }
        } // If the user lifts up the last finger, the additional finger, or no gesture was done
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                // Get that finger with the corresponding id through index
                val id = finger.getPointerId(finger.actionIndex)
                // Then remove that finger in the map preventing any note play again upon lifting
                pointerToNoteMap.remove(id)
            }
        }

        // Set the touch event normal again
        return super.dispatchTouchEvent(finger)
    }

    // PIN Validation
    private fun validatePin(letter: Int) {
            // Change the image of the corresponding pin position then add that image to the enteredPin input then go to the next available pin position
            pinBoxes[pinPosition].setImageResource(letter)

            // For each box that gets added with a letter do a subtle bounce
            AnimationUtils.loadAnimation(this, R.anim.pin_bounce).apply {
                pinBoxes[pinPosition].startAnimation(this)
            }
            // add the image source to the enteredPin then move to the next pin box
            enteredPin.add(letter)
            pinPosition++

            // If the PIN already reaches 4
            if (pinPosition == pinMaximum) {
                // Compares the list pair of the ValidPinPets with the enteredPin list order
                val matchPin = validPinPets.indexOfFirst { it.first == enteredPin }

                // If the comparison is correct
                if (matchPin != -1) {
                    // unlock and display the corresponding pet image with the matchPin
                    unlockPetImage(matchPin)
                    showUnlockPetDialog(rewardPopup[matchPin])
                } else {
                    // Make an error animation
                    val shake = AnimationUtils.loadAnimation(this, R.anim.error_shake)
                    // For each pin box start the animation
                    pinBoxes.forEach { it.startAnimation(shake) }
                    Toast.makeText(this, "Invalid PIN. Try Again!", Toast.LENGTH_SHORT).show()

                    // Reset the pin as wel
                    resetPin()
                }

                // Empty the enteredPin list combination
                enteredPin.clear()
            }
    }

    private fun resetPin() {
        // Reset the pinPosition and replace all of the pinBoxes with the placeholder
        pinPosition = 0
        pinBoxes.forEach {
            it.setImageResource(R.drawable.pin_placeholder)
        }
    }

    // Changes to Play Mode
    private fun showPlayMode() {
        // Hide the pin container and do the following animations
        pinContainer.visibility = Flow.GONE
        playContainer.visibility = Flow.VISIBLE
        containerEnter(playContainer)
        containerOut(pinContainer)

        // Get the corresponding note strings
        val noteStrings = listOf(
            R.string.note_1, R.string.note_2, R.string.note_3,
            R.string.note_4, R.string.note_5, R.string.note_6,
            R.string.note_7, R.string.note_8, R.string.note_9
        )

        // Change the note text with the corresponding id
        noteIds.forEachIndexed { i, btn ->
            findViewById<Button>(btn).setText(noteStrings[i])
        }

        // Change the play mode icon with the pin mode's image and description
        // As well as change display onclick listener
        modeBtn.setImageResource(R.drawable.pet)
        modeBtn.contentDescription = getString(R.string.mode_pin)
        modeBtn.setOnClickListener {
            secondaryBtnAnim(modeBtn)
            uiSound(R.raw.button_click)
            showPinMode()
        }
    }

    // Changes to PIN Mode
    private fun showPinMode() {
        // Hide the play container and do the following animations and also reset the pin
        pinContainer.visibility = Flow.VISIBLE
        playContainer.visibility = Flow.GONE
        containerEnter(pinContainer)
        containerOut(playContainer)
        resetPin()

        // Get the corresponding letter strings
        val pinStrings = listOf(
            R.string.pin_1, R.string.pin_2, R.string.pin_3,
            R.string.pin_4, R.string.pin_5, R.string.pin_6,
            R.string.pin_7, R.string.pin_8, R.string.pin_9
        )

        // Change the note text with the corresponding id
        noteIds.forEachIndexed { i, btn ->
            findViewById<Button>(btn).setText(pinStrings[i])
        }

        // Change the pin mode icon with the play mode's image and description
        // As well as change display onclick listener
        modeBtn.setImageResource(R.drawable.play)
        modeBtn.contentDescription = getString(R.string.mode_play)
        modeBtn.setOnClickListener {
            secondaryBtnAnim(modeBtn)
            uiSound(R.raw.button_close)
            showPlayMode()
        }
    }

    // Shows the corresponding unlocked pet img with the popup
    private fun showUnlockPetDialog(petImg: Int) {
        dialogPopup(petImg)
        resetPin()
    }

    // Unlocks the pet image
    private fun unlockPetImage(pin: Int) {
        // Get the img from the list with the corresponding pin
        val petImg = validPinPets[pin].second

        // If the pet is not unlocked yet then add that pet image to the display images
        if (!unlockedImgs.containsKey(pin)) {
            // store the unlocked pet img with the corresponding pin
            unlockedImgs[pin] = petImg
            playImgs.add(petImg)
            // set the user's preferences may be an empty set or not
            val unlockedPrefs = prefs.getStringSet("unlocked_pets", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            // Add the unlocked pin string to the user prefs
            unlockedPrefs.add(pin.toString())
            // Save the unlocked prefs back to the unlocked_pets key
            prefs.edit { putStringSet("unlocked_pets", unlockedPrefs) }
        }
    }
}