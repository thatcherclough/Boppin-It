package com.cs407.boppinit.activities.standard

import com.cs407.boppinit.Difficulty
import com.cs407.boppinit.GameMode
import com.cs407.boppinit.activities.FlipItActivityView
import com.cs407.boppinit.activities.ListenToItActivityView
import com.cs407.boppinit.activities.MashItActivityView
import com.cs407.boppinit.activities.MathItActivityView
import com.cs407.boppinit.activities.PickItActivityView
import com.cs407.boppinit.activities.ScreamItActivityView
import com.cs407.boppinit.activities.ShakeItActivityView
import com.cs407.boppinit.activities.SpinItActivityView

data class ActivityTimeLimits(
    val easy: Long,
    val medium: Long,
    val hard: Long
)

interface BopItActivityView {
    fun initializeView()
    fun startActivity()
    fun stopActivity() }

data class BopItActivity(
    val title: String,
    val subtitle: String,
    val timeLimits: ActivityTimeLimits?,
    val viewProvider: (onComplete: () -> Unit, difficulty: Difficulty) -> BopItActivityView
) {
    fun getTimeLimit(difficulty: Difficulty): Long? {
        if (timeLimits == null) {
            return null
        }

        return when (difficulty) {
            Difficulty.EASY -> { this.timeLimits.easy }
            Difficulty.MEDIUM -> { this.timeLimits.medium }
            else -> { this.timeLimits.hard }
        }
    }
}

val PassItActivity: BopItActivity = BopItActivity(
    title = "Pass it!",
    subtitle = "Pass it to the next player!",
    timeLimits = ActivityTimeLimits(
        easy = 3000L,
        medium = 3000L,
        hard = 3000L
    ),
    viewProvider = { done, _ -> PassItActivityView(done) }
)

val EliminatedActivity: BopItActivity = BopItActivity(
    title = "Eliminated!",
    subtitle = "You've been eliminated!\nPass the phone to the person to your right.",
    timeLimits = null,
    viewProvider = { done, _ -> EliminatedActivityView(done) },
)

val GameOverActivity: (score: Int, activitiesCompleted: Int, difficulty: Difficulty, gameMode: GameMode, newHighScore: Boolean) -> BopItActivity = { score, activitiesCompleted, difficulty, gameMode, newHighScore ->
    BopItActivity(
        title = "Game Over!",
        subtitle = "The game has ended.\nGreat job!",
        timeLimits = null,
        viewProvider = { done, _ ->
            GameOverActivityView(
                onComplete = done,
                score = score,
                activitiesCompleted = activitiesCompleted,
                difficulty = difficulty,
                gameMode = gameMode,
                isNewHighScore = newHighScore
            )
        }
    )
}

object BopItActivityRepository {
    val activities = listOf(
        BopItActivity(
            title = "Mash it!",
            subtitle = "Mash the button until you reach zero!",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds for easy
                medium = 8000L,   // 8 seconds for medium
                hard = 5000L      // 5 seconds for hard
            ),
            viewProvider = { onComplete, difficulty ->
                MashItActivityView(onComplete, difficulty)
            }
        ),
        BopItActivity(
            title = "Flip It!",
            subtitle = "Flip the phone over.",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete, difficulty -> FlipItActivityView(onComplete, difficulty) }
        ),
        BopItActivity(
            title = "Listen to it!",
            subtitle = "Select the button corresponding to the sound you hear.",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete, difficulty -> ListenToItActivityView(onComplete, difficulty) }
        ),
        BopItActivity(
            title = "Math It!",
            subtitle = "Solve this math question.",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete, difficulty -> MathItActivityView(onComplete, difficulty) }
        ),
        BopItActivity(
            title = "Pick It!",
            subtitle = "Pick one of the following options.",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete, difficulty -> PickItActivityView(onComplete, difficulty) }
        ),
        BopItActivity(
            title = "Scream It!",
            subtitle = "AHHHH!!!",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete, difficulty -> ScreamItActivityView(onComplete, difficulty) }
        ),
        BopItActivity(
            title = "Shake It!",
            subtitle = "Shake your phone!",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete, difficulty -> ShakeItActivityView(onComplete, difficulty) }
        ),
        BopItActivity(
            title = "Spin It Activity!",
            subtitle = "Spin this S**T!",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete, difficulty -> SpinItActivityView(onComplete, difficulty) }
        )
    )

    private var lastActivity: BopItActivity? = null

//    fun getRandomActivity(gameMode: GameMode): BopItActivity {
//        var newActivity: BopItActivity
//        if (gameMode == GameMode.SOLO) {
//            do {
//                newActivity = activities.random()
//            } while (newActivity == lastActivity)
//        } else {
//            newActivity = activities.random()
//        }
//        lastActivity = newActivity
//        return newActivity
//    }


    // Hardcode to scream it or whatever other activity you want to test
    fun getRandomActivity(gameMode: GameMode): BopItActivity {
        // Hardcode to return the "Scream It!" activity
//        val screamItActivity = activities.find { it.title == "Scream It!" }
//        if (screamItActivity != null) {
//            return screamItActivity
//        }

        val listenToItActivity = activities.find { it.title == "Listen to it!" }
        if (listenToItActivity != null) {
            return listenToItActivity
        }

        var newActivity: BopItActivity
        do {
            newActivity = activities.random()
        } while (newActivity == lastActivity)
        lastActivity = newActivity
        return newActivity
    }


}