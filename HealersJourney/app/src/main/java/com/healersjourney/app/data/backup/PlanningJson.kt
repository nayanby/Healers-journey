package com.healersjourney.app.data.backup

import com.healersjourney.app.domain.*
import org.json.JSONArray
import org.json.JSONObject

object PlanningJson {
    fun encode(state: GameState): JSONObject = JSONObject()
        .put("goals", JSONArray().apply { state.personalGoals.forEach { g -> put(JSONObject()
            .put("id", g.id).put("title", g.title).put("skill", g.skill).put("activity", g.activity)
            .put("target", g.target).put("start", g.start).put("end", g.end)) } })
        .put("recurring", JSONArray().apply { state.recurringTasks.forEach { r -> put(JSONObject()
            .put("id", r.id).put("name", r.name).put("skill", r.skill).put("xp", r.xp)
            .put("weekdays", JSONArray(r.weekdays.toList())).put("start", r.start)) } })
        .put("occurrences", JSONArray(state.scheduledOccurrences.toList()))
        .put("focus", state.focusSession?.let { f -> JSONObject().put("taskId", f.taskId).put("day", f.day)
            .put("duration", f.durationMs).put("remaining", f.remainingMs).put("endsAt", f.endsAt).put("running", f.running) } ?: JSONObject.NULL)

    fun restore(state: GameState, json: JSONObject?) {
        if (json == null) return
        json.optJSONArray("goals")?.let { arr -> (0 until arr.length()).forEach { i ->
            val g = arr.optJSONObject(i) ?: return@forEach
            if (g.optString("skill") in Rules.SKILL_KEYS && g.optInt("target") in 1..1000 && g.optString("id").isNotBlank())
                state.personalGoals.add(PersonalGoal(g.getString("id"), g.optString("title"), g.getString("skill"),
                    g.optString("activity"), g.getInt("target"), g.optString("start"), g.optString("end")))
        } }
        json.optJSONArray("recurring")?.let { arr -> (0 until arr.length()).forEach { i ->
            val r = arr.optJSONObject(i) ?: return@forEach
            val days = r.optJSONArray("weekdays")?.let { d -> (0 until d.length()).map { d.optInt(it) }.filter { it in 1..7 }.toSet() }.orEmpty()
            if (r.optString("skill") in Rules.SKILL_KEYS && days.isNotEmpty() && r.optString("id").isNotBlank())
                state.recurringTasks.add(RecurringTask(r.getString("id"), r.optString("name"), r.getString("skill"),
                    r.optInt("xp", 20).coerceIn(1, 10000), days, r.optString("start")))
        } }
        json.optJSONArray("occurrences")?.let { arr -> (0 until arr.length()).forEach { state.scheduledOccurrences.add(arr.getString(it)) } }
        json.optJSONObject("focus")?.let { f ->
            val duration = f.optLong("duration").coerceIn(60_000L, 180 * 60_000L)
            if (f.optString("taskId").isNotBlank()) state.focusSession = FocusSession(f.getString("taskId"), f.optString("day"), duration,
                f.optLong("remaining").coerceIn(0, duration), f.optLong("endsAt"), f.optBoolean("running"))
        }
    }
}
