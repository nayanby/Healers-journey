package com.healersjourney.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface GameDao {

    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM actions ORDER BY position ASC")
    suspend fun getActions(): List<ActionEntity>

    @Query("SELECT * FROM logs ORDER BY position ASC")
    suspend fun getLogs(): List<LogEntity>

    @Query("SELECT * FROM study_hours ORDER BY date ASC")
    suspend fun getStudyHours(): List<StudyHoursEntity>

    @Query("SELECT * FROM date_flags ORDER BY listName ASC, date ASC")
    suspend fun getDateFlags(): List<DateFlagEntity>

    @Query("SELECT * FROM weekly_xp ORDER BY week ASC, skill ASC")
    suspend fun getWeeklyXp(): List<WeeklyXpEntity>

    @Query("SELECT * FROM activities ORDER BY skill ASC, list ASC, position ASC")
    suspend fun getActivities(): List<ActivityEntity>

    @Query("SELECT * FROM custom_achievements ORDER BY position ASC")
    suspend fun getCustomAchievements(): List<CustomAchievementEntity>

    @Query("SELECT * FROM milestones ORDER BY position ASC")
    suspend fun getMilestones(): List<MilestoneEntity>

    @Query("SELECT * FROM personal_care ORDER BY careKey ASC")
    suspend fun getCare(): List<CareEntity>

    @Query("SELECT * FROM todos ORDER BY bucket ASC, position ASC")
    suspend fun getTodos(): List<TodoEntity>

    @Query("DELETE FROM actions")
    suspend fun clearActions()

    @Query("DELETE FROM logs")
    suspend fun clearLogs()

    @Query("DELETE FROM study_hours")
    suspend fun clearStudyHours()

    @Query("DELETE FROM date_flags")
    suspend fun clearDateFlags()

    @Query("DELETE FROM weekly_xp")
    suspend fun clearWeeklyXp()

    @Query("DELETE FROM activities")
    suspend fun clearActivities()

    @Query("DELETE FROM custom_achievements")
    suspend fun clearCustomAchievements()

    @Query("DELETE FROM milestones")
    suspend fun clearMilestones()

    @Query("DELETE FROM personal_care")
    suspend fun clearCare()

    @Query("DELETE FROM todos")
    suspend fun clearTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(items: List<ActionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(items: List<LogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyHours(items: List<StudyHoursEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateFlags(items: List<DateFlagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyXp(items: List<WeeklyXpEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(items: List<ActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAchievements(items: List<CustomAchievementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(items: List<MilestoneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCare(items: List<CareEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(items: List<TodoEntity>)

    /** Replaces the entire saved game atomically. */
    @Transaction
    suspend fun saveAll(
        profile: ProfileEntity,
        actions: List<ActionEntity>,
        logs: List<LogEntity>,
        studyHours: List<StudyHoursEntity>,
        dateFlags: List<DateFlagEntity>,
        weeklyXp: List<WeeklyXpEntity>,
        activities: List<ActivityEntity>,
        customAchievements: List<CustomAchievementEntity>,
        milestones: List<MilestoneEntity>,
        care: List<CareEntity>,
        todos: List<TodoEntity>
    ) {
        clearActions()
        clearLogs()
        clearStudyHours()
        clearDateFlags()
        clearWeeklyXp()
        clearActivities()
        clearCustomAchievements()
        clearMilestones()
        clearCare()
        clearTodos()
        upsertProfile(profile)
        insertActions(actions)
        insertLogs(logs)
        insertStudyHours(studyHours)
        insertDateFlags(dateFlags)
        insertWeeklyXp(weeklyXp)
        insertActivities(activities)
        insertCustomAchievements(customAchievements)
        insertMilestones(milestones)
        insertCare(care)
        insertTodos(todos)
    }
}
