package com.healersjourney.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val playerName: String,
    val totalXp: Int,
    val streak: Int,
    val lastSleepHours: Double,
    val knowledgeXp: Int,
    val medicineXp: Int,
    val strengthXp: Int,
    val socialXp: Int,
    val hobbiesXp: Int,
    val bankBalance: Double,
    val savings: Double,
    val emergencyFund: Double,
    val investments: Double,
    val savingsGoal: Double,
    val bankGoal: Double,
    val emergencyGoal: Double,
    val investmentsGoal: Double,
    val decayStartDate: String,
    val personalCareResetBaseline: Boolean,
    val todoDate: String,
    val todoViewOffset: Int,
    val todoViewMode: String,
    val hobbiesJson: String,
    val removedActivitiesJson: String,
    @ColumnInfo(defaultValue = "'{}'") val gameExtrasJson: String = "{}"
)

@Entity(tableName = "actions")
data class ActionEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val ts: String,
    val date: String,
    val week: String,
    val month: String,
    val skill: String,
    val xp: Int,
    val baseXp: Int,
    val decayPercent: Int,
    val type: String,
    val name: String,
    val originalName: String?,
    val hours: Double,
    val hobby: String?,
    val monthlyInvestmentAmount: Double,
    val careKey: String?,
    val previousCareLast: String?,
    val newCareLast: String?
)

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val kind: String,
    val text: String,
    val actionId: String?
)

@Entity(tableName = "study_hours")
data class StudyHoursEntity(
    @PrimaryKey val date: String,
    val hours: Double
)

@Entity(tableName = "date_flags", primaryKeys = ["listName", "date"])
data class DateFlagEntity(
    val listName: String,
    val date: String
)

@Entity(tableName = "weekly_xp", primaryKeys = ["week", "skill"])
data class WeeklyXpEntity(
    val week: String,
    val skill: String,
    val xp: Int
)

@Entity(tableName = "activities", primaryKeys = ["skill", "list", "position"])
data class ActivityEntity(
    val skill: String,
    val list: String,
    val position: Int,
    val id: String,
    val name: String,
    val xp: Int
)

@Entity(tableName = "custom_achievements")
data class CustomAchievementEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val name: String,
    @ColumnInfo(name = "groupName") val groupName: String,
    val metric: String,
    val target: Int,
    val createdAt: String
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val title: String,
    val description: String,
    val completed: Boolean,
    val completedDate: String,
    val showOnMain: Boolean,
    val image: String
)

@Entity(tableName = "personal_care")
data class CareEntity(
    @PrimaryKey val careKey: String,
    val lastDate: String
)

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val bucket: String,
    val name: String,
    val skill: String,
    val xp: Int,
    val date: String,
    val completed: Boolean,
    val completedAt: String
)
