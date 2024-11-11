package com.cs407.boppinit.activities

import com.cs407.boppinit.Difficulty

data class ActivityTimeLimits(
    val easy: Long,
    val medium: Long,
    val hard: Long
)

interface BopItActivityView {
    fun initializeView()
    fun startActivity()
    fun stopActivity()
}

data class BopItActivity(
    val title: String,
    val subtitle: String,
    val timeLimits: ActivityTimeLimits?,
    val viewProvider: (onComplete: () -> Unit) -> BopItActivityView
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
    viewProvider = { done -> PassItActivityView(done) },
)

val EliminatedActivity: BopItActivity = BopItActivity(
    title = "Eliminated!",
    subtitle = "You've been eliminated!\nPass the phone to the person to your left.",
    timeLimits = null,
    viewProvider = { done -> EliminatedActivityView(done) },
)

object BopItActivityRepository {
    val activities = listOf(
        BopItActivity(
            title = "Test it!",
            subtitle = "This is test 1",
            timeLimits = ActivityTimeLimits(
                easy = 3000L,    // 3 seconds
                medium = 2000L,  // 2 seconds
                hard = 1000L     // 1 second
            ),
            viewProvider = { onComplete -> TestItActivityView(onComplete) }
        ),
        BopItActivity(
            title = "Test it (2)!",
            subtitle = "This is test 2",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete -> TestItActivityView(onComplete) }
        ),
        BopItActivity(
            title = "Test it (3)!",
            subtitle = "This is test 3",
            timeLimits = ActivityTimeLimits(
                easy = 10000L,    // 10 seconds
                medium = 7000L,  // 7 seconds
                hard = 5000L     // 5 second
            ),
            viewProvider = { onComplete -> TestItActivityView(onComplete) }
        ),
    )

    fun getRandomActivity(): BopItActivity = activities.random()
}