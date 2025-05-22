package com.example.thebreak


// Credits to: https://www.youtube.com/watch?v=nuLyv70ViLc&t=628s

object Breaks {
    val breakName = mapOf(
        BreakId.BOOK to "Read A Book",
        BreakId.WALK to "Take A Walk",
        BreakId.WATER to "Drink Water or Tea",
        BreakId.SKETCH to "Doodle or Sketch",
        BreakId.PLANT to "Water Your Plants",
        BreakId.GAME to "Play a Short Game",
        BreakId.JOURNAL to "Journal for a Few Minutes",
        BreakId.CLEAN to "Do some cleaning",
        BreakId.EYE to "Do an Eye Exercise",
        BreakId.SIT to "Sit in Silence",
        BreakId.LANGUAGE to "Learn a Phrase in a Different Language"
    )

    val breakDescription = mapOf(
        BreakId.BOOK to "Learn something new by reading one chapter of a book.",
        BreakId.WALK to "Stretch your legs and get fresh air. A short stroll can boost creativity and reduce stress.",
        BreakId.WATER to "Hydrate with a glass of water or enjoy a calming cup of tea.",
        BreakId.SKETCH to "Grab a pen and paper and let your hand wander. It’s relaxing and stimulates creativity.",
        BreakId.PLANT to "Caring for plants can be soothing and gives your eyes a break from screens.",
        BreakId.GAME to "Play a short mobile or desktop game to reset your brain and have a little fun.",
        BreakId.JOURNAL to "Write down your thoughts, ideas, or what you're grateful for.",
        BreakId.CLEAN to "Wipe your screen, organize your to-do list, or take out the trash.",
        BreakId.EYE to "Practice the 20-20-20 rule: every 20 minutes, look at something 20 feet away for 20 seconds.",
        BreakId.SIT to "Just sit quietly and let your mind settle.",
        BreakId.LANGUAGE to "Learn how to say “Hello” or “Thank you” in a language you don’t speak yet."
    )

    val breakImage = mapOf(
        BreakId.BOOK to R.drawable.break_book,
        BreakId.WALK to R.drawable.break_walk,
        BreakId.WATER to R.drawable.break_water,
        BreakId.SKETCH to R.drawable.break_sketch,
        BreakId.PLANT to R.drawable.break_plant,
        BreakId.GAME to R.drawable.break_game,
        BreakId.JOURNAL to R.drawable.break_journal,
        BreakId.CLEAN to R.drawable.break_clean,
        BreakId.EYE to R.drawable.break_eye,
        BreakId.SIT to R.drawable.break_sit,
        BreakId.LANGUAGE to R.drawable.break_language
    )
}