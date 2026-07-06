package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NumberRepository(
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentDao,
    private val statsDao: StatsDao
) {
    val favorites: Flow<List<FavoriteNumber>> = favoriteDao.getAllFavorites()
    val recents: Flow<List<RecentNumber>> = recentDao.getRecentNumbers()
    val stats: Flow<UserStats?> = statsDao.getStats()

    suspend fun toggleFavorite(number: Int, language: String): Boolean {
        val isFav = favoriteDao.isFavorite(number, language)
        if (isFav) {
            favoriteDao.removeFavorite(number, language)
            return false
        } else {
            favoriteDao.insertFavorite(FavoriteNumber(number = number, language = language))
            return true
        }
    }

    suspend fun isFavorite(number: Int, language: String): Boolean {
        return favoriteDao.isFavorite(number, language)
    }

    suspend fun addRecent(number: Int, language: String) {
        // First delete to avoid duplicate key issues in unique index and force it to top
        recentDao.deleteRecent(number, language)
        recentDao.insertRecent(RecentNumber(number = number, language = language))
        
        // Also update total learned stats
        markNumberAsLearned()
    }

    suspend fun clearRecents() {
        recentDao.clearAll()
    }

    private suspend fun markNumberAsLearned() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentStats = statsDao.getStatsDirect() ?: UserStats()

        val lastLearned = currentStats.lastLearnedDate
        var todayCount = currentStats.todayLearnedCount
        var total = currentStats.totalLearned

        if (lastLearned == currentDate) {
            todayCount++
        } else {
            todayCount = 1
        }
        total++

        // Check and update streak
        var streak = currentStats.streakDays
        val lastActive = currentStats.lastActiveDate
        if (lastActive.isNotEmpty()) {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val d1 = format.parse(lastActive)
                val d2 = format.parse(currentDate)
                if (d1 != null && d2 != null) {
                    val diff = d2.time - d1.time
                    val diffDays = diff / (24 * 60 * 60 * 1000)
                    if (diffDays == 1L) {
                        streak++
                    } else if (diffDays > 1L) {
                        streak = 1
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        } else {
            streak = 1
        }

        // Check for badges
        var bitmask = currentStats.badgeUnlockBitmask
        if (total >= 1) bitmask = bitmask or 1 // "First Step"
        if (total >= 20) bitmask = bitmask or 2 // "Counting Master"

        val updated = currentStats.copy(
            totalLearned = total,
            todayLearnedCount = todayCount,
            lastLearnedDate = currentDate,
            lastActiveDate = currentDate,
            streakDays = streak,
            badgeUnlockBitmask = bitmask
        )
        statsDao.insertOrUpdateStats(updated)
    }

    suspend fun updateQuizScore(score: Int) {
        val currentStats = statsDao.getStatsDirect() ?: UserStats()
        val high = if (score > currentStats.quizHighScore) score else currentStats.quizHighScore
        
        var bitmask = currentStats.badgeUnlockBitmask
        if (score >= 8) bitmask = bitmask or 4 // "Quiz Champion" badge
        
        val updated = currentStats.copy(
            quizHighScore = high,
            badgeUnlockBitmask = bitmask,
            lastActiveDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
        statsDao.insertOrUpdateStats(updated)
    }

    suspend fun unlockGameBadge() {
        val currentStats = statsDao.getStatsDirect() ?: UserStats()
        val bitmask = currentStats.badgeUnlockBitmask or 8 // "Game King" badge
        val updated = currentStats.copy(
            badgeUnlockBitmask = bitmask
        )
        statsDao.insertOrUpdateStats(updated)
    }

    suspend fun resetProgress() {
        statsDao.insertOrUpdateStats(UserStats())
        recentDao.clearAll()
    }
}
