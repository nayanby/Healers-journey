package com.healersjourney.app.domain

/** Starter activities for each category. */
object Defaults {

    data class Quest(val name: String, val detail: String, val skill: String, val xp: Int)

    val QUESTS = listOf(
        Quest("Read a book chapter", "Read one chapter of a book", "knowledgeXp", 50),
        Quest("Finish a work task", "Finish one work task", "medicineXp", 50),
        Quest("Walk for 30 minutes", "Take a 30-minute walk", "strengthXp", 40),
        Quest("Work on a hobby", "Spend time on a creative project", "hobbiesXp", 25),
        Quest("Call a family member", "Catch up with family", "socialXp", 25)
    )

    /** Dashboard quest order puts Knowledge last, matching the web layout. */
    val QUEST_SKILL_ORDER = listOf("medicineXp", "strengthXp", "socialXp", "hobbiesXp", "knowledgeXp")

    val STARTER_HOBBIES = listOf("Music", "Chess")

    fun activityDefs(): Map<String, ActivityList> = mapOf(
        "knowledgeXp" to ActivityList(
            major = mutableListOf(def("Read a book chapter", 40), def("Review today's notes", 30)),
            other = mutableListOf(def("Write study notes", 30))
        ),
        "medicineXp" to ActivityList(
            major = mutableListOf(def("Finish a work task", 50), def("Plan tomorrow's work", 50)),
            other = mutableListOf(def("Reply to work emails", 30), def("Learn a work skill", 40))
        ),
        "strengthXp" to ActivityList(
            major = mutableListOf(def("Complete a workout", 40), def("Walk for 30 minutes", 30)),
            other = mutableListOf(def("Stretch for ten minutes", 20), def("Do a yoga session", 25))
        ),
        "socialXp" to ActivityList(
            major = mutableListOf(def("Call a family member", 25), def("Meet up with a friend", 20)),
            other = mutableListOf(def("Celebrate someone's birthday", 30), def("Help a friend", 25), def("Send a thoughtful message", 10))
        ),
        "hobbiesXp" to ActivityList(
            major = mutableListOf(def("Practice a musical instrument", 25), def("Play a chess game", 25)),
            other = mutableListOf(def("Solve a puzzle", 20), def("Work on a hobby", 20))
        )
    )

    private fun def(name: String, xp: Int) = ActivityDef("", name, xp)
}
