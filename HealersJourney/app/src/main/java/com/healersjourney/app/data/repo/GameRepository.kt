package com.healersjourney.app.data.repo

import com.healersjourney.app.data.db.ActionEntity
import com.healersjourney.app.data.db.ActivityEntity
import com.healersjourney.app.data.db.CareEntity
import com.healersjourney.app.data.db.CustomAchievementEntity
import com.healersjourney.app.data.db.DateFlagEntity
import com.healersjourney.app.data.db.GameDao
import com.healersjourney.app.data.db.LogEntity
import com.healersjourney.app.data.db.MilestoneEntity
import com.healersjourney.app.data.db.ProfileEntity
import com.healersjourney.app.data.db.StudyHoursEntity
import com.healersjourney.app.data.db.TodoEntity
import com.healersjourney.app.data.db.WeeklyXpEntity
import com.healersjourney.app.domain.ActionEntry
import com.healersjourney.app.domain.ActivityDef
import com.healersjourney.app.domain.ActivityList
import com.healersjourney.app.domain.CustomAchievement
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.LogEntry
import com.healersjourney.app.domain.Milestone
import com.healersjourney.app.domain.RemovedActivity
import com.healersjourney.app.domain.TodoTask
import com.healersjourney.app.data.backup.StateJson
import org.json.JSONArray
import org.json.JSONObject

object Json {

    fun stringListToJson(list: List<String>): String = JSONArray(list).toString()

    fun stringListFromJson(text: String?): List<String> {
        if (text.isNullOrEmpty()) return emptyList()
        val arr = JSONArray(text)
        return (0 until arr.length()).map { arr.optString(it, "") }.filter { it.isNotEmpty() }
    }

    fun removedActivitiesToJson(list: List<RemovedActivity>): String {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("skill", r.skill)
                    .put("id", r.id ?: JSONObject.NULL)
                    .put("name", r.name)
                    .put("xp", r.xp)
                    .put("removedAt", r.removedAt)
            )
        }
        return arr.toString()
    }

    fun removedActivitiesFromJson(text: String?): List<RemovedActivity> {
        if (text.isNullOrEmpty()) return emptyList()
        val arr = JSONArray(text)
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            RemovedActivity(
                skill = o.optString("skill"),
                id = o.optString("id").ifEmpty { null },
                name = o.optString("name"),
                xp = o.optInt("xp"),
                removedAt = o.optString("removedAt")
            )
        }
    }
}

/** Maps the in-memory [GameState] aggregate onto the Room tables and back. */
class GameRepository(private val dao: GameDao) {

    suspend fun load(): GameState {
        val profile = dao.getProfile() ?: return GameState()
        return fromEntities(
            profile,
            dao.getActions(),
            dao.getLogs(),
            dao.getStudyHours(),
            dao.getDateFlags(),
            dao.getWeeklyXp(),
            dao.getActivities(),
            dao.getCustomAchievements(),
            dao.getMilestones(),
            dao.getCare(),
            dao.getTodos()
        )
    }

    suspend fun save(state: GameState) {
        val saved = toEntities(state)
        dao.saveAll(
            saved.profile, saved.actions, saved.logs, saved.study, saved.flags,
            saved.weekly, saved.activities, saved.customs, saved.milestones,
            saved.care, saved.todos
        )
    }

    private fun toEntities(state: GameState): Saved {
        val profile = ProfileEntity(
            id = 0,
            playerName = state.playerName,
            totalXp = state.totalXp,
            streak = state.streak,
            lastSleepHours = state.lastSleepHours,
            knowledgeXp = state.knowledgeXp,
            medicineXp = state.medicineXp,
            strengthXp = state.strengthXp,
            socialXp = state.socialXp,
            hobbiesXp = state.hobbiesXp,
            bankBalance = state.bankBalance,
            savings = state.savings,
            emergencyFund = state.emergencyFund,
            investments = state.investments,
            savingsGoal = state.savingsGoal,
            bankGoal = state.bankGoal,
            emergencyGoal = state.emergencyGoal,
            investmentsGoal = state.investmentsGoal,
            decayStartDate = state.decayStartDate,
            personalCareResetBaseline = state.personalCareResetBaseline,
            todoDate = state.todoDate,
            todoViewOffset = state.todoViewOffset,
            todoViewMode = state.todoViewMode,
            hobbiesJson = Json.stringListToJson(state.hobbies),
            removedActivitiesJson = Json.removedActivitiesToJson(state.removedActivities),
            gameExtrasJson = StateJson.encodeLocalExtras(state).toString()
        )

        val actions = state.actionHistory.mapIndexed { i, a ->
            ActionEntity(
                id = a.id, position = i, ts = a.ts, date = a.date, week = a.week, month = a.month,
                skill = a.skill, xp = a.xp, baseXp = a.baseXp, decayPercent = a.decayPercent,
                type = a.type, name = a.name, originalName = a.originalName, hours = a.hours,
                hobby = a.hobby, monthlyInvestmentAmount = a.monthlyInvestmentAmount,
                careKey = a.careKey, previousCareLast = a.previousCareLast, newCareLast = a.newCareLast
            )
        }

        val logs = state.taskLog.mapIndexed { i, l -> logEntity(l, "task", i) } +
            state.financeLog.mapIndexed { i, l -> logEntity(l, "finance", i) }

        val study = state.studyHoursByDate.map { StudyHoursEntity(it.key, it.value) }

        val flags = state.studyDates.distinct().map { DateFlagEntity("studyDates", it) } +
            state.workoutDates.distinct().map { DateFlagEntity("workoutDates", it) }

        val weekly = state.weeklyAttrXp.flatMap { (week, skills) ->
            skills.map { (skill, xp) -> WeeklyXpEntity(week, skill, xp) }
        }

        val activities = state.activityDefs.flatMap { (skill, lists) ->
            lists.major.mapIndexed { i, d -> activityEntity(skill, "major", i, d) } +
                lists.other.mapIndexed { i, d -> activityEntity(skill, "other", i, d) }
        }

        val customs = state.customAchievements.mapIndexed { i, c ->
            CustomAchievementEntity(c.id, i, c.name, c.group, c.metric, c.target, c.createdAt)
        }

        val milestones = state.milestones.mapIndexed { i, m ->
            MilestoneEntity(m.id, i, m.title, m.desc, m.completed, m.completedDate, m.showOnMain, m.image)
        }

        val care = state.personalCareLast.map { CareEntity(it.key, it.value) }

        val todos = state.todoTasks.mapIndexed { i, t -> todoEntity(t, "current", i) } +
            state.todoHistory.flatMap { (date, list) ->
                list.mapIndexed { i, t -> todoEntity(t, date, i) }
            }

        return Saved(profile, actions, logs, study, flags, weekly, activities, customs, milestones, care, todos)
    }

    private fun logEntity(l: LogEntry, kind: String, position: Int) =
        LogEntity(id = l.id, position = position, kind = kind, text = l.text, actionId = l.actionId)

    private fun activityEntity(skill: String, list: String, position: Int, d: ActivityDef) =
        ActivityEntity(skill, list, position, d.id, d.name, d.xp)

    private fun todoEntity(t: TodoTask, bucket: String, position: Int) = TodoEntity(
        id = t.id, position = position, bucket = bucket, name = t.name, skill = t.skill,
        xp = t.xp, date = t.date, completed = t.completed, completedAt = t.completedAt
    )

    private fun fromEntities(
        profile: ProfileEntity,
        actions: List<ActionEntity>,
        logs: List<LogEntity>,
        study: List<StudyHoursEntity>,
        flags: List<DateFlagEntity>,
        weekly: List<WeeklyXpEntity>,
        activities: List<ActivityEntity>,
        customs: List<CustomAchievementEntity>,
        milestones: List<MilestoneEntity>,
        care: List<CareEntity>,
        todos: List<TodoEntity>
    ): GameState {
        val state = GameState()
        state.playerName = profile.playerName
        state.totalXp = profile.totalXp
        state.streak = profile.streak
        state.lastSleepHours = profile.lastSleepHours
        state.knowledgeXp = profile.knowledgeXp
        state.medicineXp = profile.medicineXp
        state.strengthXp = profile.strengthXp
        state.socialXp = profile.socialXp
        state.hobbiesXp = profile.hobbiesXp
        state.bankBalance = profile.bankBalance
        state.savings = profile.savings
        state.emergencyFund = profile.emergencyFund
        state.investments = profile.investments
        state.savingsGoal = profile.savingsGoal
        state.bankGoal = profile.bankGoal
        state.emergencyGoal = profile.emergencyGoal
        state.investmentsGoal = profile.investmentsGoal
        state.decayStartDate = profile.decayStartDate
        state.personalCareResetBaseline = profile.personalCareResetBaseline
        state.todoDate = profile.todoDate
        state.todoViewOffset = profile.todoViewOffset
        state.todoViewMode = profile.todoViewMode

        state.hobbies.clear()
        state.hobbies.addAll(Json.stringListFromJson(profile.hobbiesJson))

        state.removedActivities.clear()
        state.removedActivities.addAll(Json.removedActivitiesFromJson(profile.removedActivitiesJson))

        state.actionHistory.addAll(actions.map { a ->
            ActionEntry(
                id = a.id, ts = a.ts, date = a.date, week = a.week, month = a.month, skill = a.skill,
                xp = a.xp, baseXp = a.baseXp, decayPercent = a.decayPercent, type = a.type, name = a.name,
                originalName = a.originalName, hours = a.hours, hobby = a.hobby,
                monthlyInvestmentAmount = a.monthlyInvestmentAmount, careKey = a.careKey,
                previousCareLast = a.previousCareLast, newCareLast = a.newCareLast
            )
        })

        state.taskLog.addAll(logs.filter { it.kind == "task" }.map { LogEntry(it.id, it.text, it.actionId) })
        state.financeLog.addAll(logs.filter { it.kind == "finance" }.map { LogEntry(it.id, it.text, it.actionId) })

        state.studyHoursByDate.putAll(study.associate { it.date to it.hours })

        state.studyDates.addAll(flags.filter { it.listName == "studyDates" }.map { it.date })
        state.workoutDates.addAll(flags.filter { it.listName == "workoutDates" }.map { it.date })

        weekly.groupBy({ it.week }, { it.skill to it.xp }).forEach { (weekKey, pairs) ->
            state.weeklyAttrXp[weekKey] = pairs.toMap().toMutableMap()
        }

        activities.groupBy { it.skill }.forEach { (skill, rows) ->
            state.activityDefs[skill] = ActivityList(
                major = rows.filter { it.list == "major" }.sortedBy { it.position }
                    .map { ActivityDef(it.id, it.name, it.xp) }.toMutableList(),
                other = rows.filter { it.list == "other" }.sortedBy { it.position }
                    .map { ActivityDef(it.id, it.name, it.xp) }.toMutableList()
            )
        }

        state.customAchievements.addAll(customs.map {
            CustomAchievement(it.id, it.name, it.groupName, it.metric, it.target, it.createdAt)
        })

        state.milestones.addAll(milestones.map {
            Milestone(it.id, it.title, it.description, it.completed, it.completedDate, it.showOnMain, it.image)
        })

        state.personalCareLast.putAll(care.associate { it.careKey to it.lastDate })

        state.todoTasks.addAll(
            todos.filter { it.bucket == "current" }.sortedBy { it.position }.map { todoFromEntity(it) }
        )
        todos.filter { it.bucket != "current" }.groupBy { it.bucket }.forEach { (date, rows) ->
            state.todoHistory[date] = rows.sortedBy { it.position }.map { todoFromEntity(it) }.toMutableList()
        }

        StateJson.restoreLocalExtras(state, profile.gameExtrasJson)
        return state
    }

    private fun todoFromEntity(e: TodoEntity) = TodoTask(
        id = e.id, name = e.name, skill = e.skill, xp = e.xp, date = e.date,
        completed = e.completed, completedAt = e.completedAt
    )

    private data class Saved(
        val profile: ProfileEntity,
        val actions: List<ActionEntity>,
        val logs: List<LogEntity>,
        val study: List<StudyHoursEntity>,
        val flags: List<DateFlagEntity>,
        val weekly: List<WeeklyXpEntity>,
        val activities: List<ActivityEntity>,
        val customs: List<CustomAchievementEntity>,
        val milestones: List<MilestoneEntity>,
        val care: List<CareEntity>,
        val todos: List<TodoEntity>
    )
}
