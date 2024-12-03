package com.cs407.boppinit

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class LeaderboardEntry(
    val userId: String = "",  // Firebase Auth UID
    val displayName: String = "",
    val score: Int = 0
)

class LeaderboardRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val leaderboardCollection = db.collection("leaderboard")

    suspend fun updateUserScore(newScore: Int): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not signed in")

            val leaderboardEntry = LeaderboardEntry(
                userId = user.uid,
                displayName = user.displayName ?: "Anonymous",
                score = newScore
            )

            leaderboardCollection
                .document(user.uid)
                .set(leaderboardEntry)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllLeaderboardEntries(): Result<List<LeaderboardEntry>> {
        return try {
            val snapshot = leaderboardCollection
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val entries = snapshot.toObjects(LeaderboardEntry::class.java)
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
