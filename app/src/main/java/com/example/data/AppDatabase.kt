package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ------------------ ENTITIES ------------------

@Entity(tableName = "favorites", indices = [Index(value = ["number", "language"], unique = true)])
data class FavoriteNumber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val number: Int,
    val language: String
)

@Entity(tableName = "recently_viewed", indices = [Index(value = ["number", "language"], unique = true)])
data class RecentNumber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val number: Int,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Single-row config
    val totalLearned: Int = 0,
    val todayLearnedCount: Int = 0,
    val lastLearnedDate: String = "", // YYYY-MM-DD
    val quizHighScore: Int = 0,
    val streakDays: Int = 0,
    val lastActiveDate: String = "", // YYYY-MM-DD
    val completedSectionsCount: Int = 0,
    val badgeUnlockBitmask: Int = 0 // 1: First Step, 2: Counting Master, 4: Quiz Champion, 8: Game King
)

// ------------------ DAOs ------------------

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY id DESC")
    fun getAllFavorites(): Flow<List<FavoriteNumber>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteNumber)

    @Query("DELETE FROM favorites WHERE number = :number AND language = :language")
    suspend fun removeFavorite(number: Int, language: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE number = :number AND language = :language)")
    suspend fun isFavorite(number: Int, language: String): Boolean
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recently_viewed ORDER BY timestamp DESC LIMIT 30")
    fun getRecentNumbers(): Flow<List<RecentNumber>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentNumber)

    @Query("DELETE FROM recently_viewed WHERE number = :number AND language = :language")
    suspend fun deleteRecent(number: Int, language: String)

    @Query("DELETE FROM recently_viewed")
    suspend fun clearAll()
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getStats(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStatsDirect(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)
}

// ------------------ DATABASE ------------------

@Database(entities = [FavoriteNumber::class, RecentNumber::class, UserStats::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentDao(): RecentDao
    abstract fun statsDao(): StatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "infinity_number_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
