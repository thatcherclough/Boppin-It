package com.cs407.boppinit

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters


// Scores
object ScoreCalculator {
    private const val BASE_POINTS = 100
    private val DIFFICULTY_MULTIPLIER = mapOf(
        Difficulty.EASY to 1.0,
        Difficulty.MEDIUM to 1.5,
        Difficulty.HARD to 2.0
    )

    fun calculatePoints(timeRemaining: Long, totalTime: Long, difficulty: Difficulty): Int {
        val timeRatio = timeRemaining.toDouble() / totalTime.toDouble()
        val baseScore = (BASE_POINTS * timeRatio).toInt()
        return (baseScore * (DIFFICULTY_MULTIPLIER[difficulty] ?: 1.0)).toInt()
    }
}

// Game history
@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    var numPlayers: Int,
    val difficulty: Difficulty,
    val gameMode: GameMode,
    val finalScore: Int,
    val activitiesCompleted: Int
)

@Dao
interface GameHistoryDao {
    @Query("SELECT * FROM game_history ORDER BY date DESC")
    fun getAllGames(): List<GameHistory>

    @Insert
    suspend fun insert(gameHistory: GameHistory)

    // Get high score for specific game mode
    @Query("SELECT * FROM game_history WHERE gameMode = :mode ORDER BY finalScore DESC LIMIT 1")
    suspend fun getHighScore(mode: GameMode): GameHistory?
}

class Converters {
    @TypeConverter
    fun difficultyToString(difficulty: Difficulty): String = difficulty.name

    @TypeConverter
    fun stringToDifficulty(value: String): Difficulty = Difficulty.valueOf(value)

    @TypeConverter
    fun gameModeToString(gameMode: GameMode): String = gameMode.name

    @TypeConverter
    fun stringToGameMode(value: String): GameMode = GameMode.valueOf(value)
}

@Database(entities = [GameHistory::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameHistoryDao(): GameHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bop_it_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
