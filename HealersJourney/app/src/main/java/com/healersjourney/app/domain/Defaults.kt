package com.healersjourney.app.domain

/** Content the app ships with: the seeded activity lists and the fixed daily quests. */
object Defaults {

    data class Quest(val name: String, val detail: String, val skill: String, val xp: Int)

    val QUESTS = listOf(
        Quest("Study Quest", "Study for 60 minutes", "knowledgeXp", 50),
        Quest("Clinical Quest", "Learn one case or clinical sign", "medicineXp", 50),
        Quest("Fitness Quest", "Walk or exercise for 30 minutes", "strengthXp", 40),
        Quest("Hobby Quest", "Spend time on Music, Chess, or another hobby", "hobbiesXp", 25),
        Quest("Social Quest", "Call family or help someone", "socialXp", 25)
    )

    /** Dashboard quest order puts Knowledge last, matching the web layout. */
    val QUEST_SKILL_ORDER = listOf("medicineXp", "strengthXp", "socialXp", "hobbiesXp", "knowledgeXp")

    val STARTER_HOBBIES = listOf("Music", "Chess")

    fun activityDefs(): Map<String, ActivityList> = mapOf(
        "knowledgeXp" to ActivityList(
            major = mutableListOf(def("MCQ practice", 40), def("Revision", 30)),
            other = mutableListOf(def("Make notes", 30))
        ),
        "medicineXp" to ActivityList(
            major = mutableListOf(def("Case learned", 50), def("Ward round", 50)),
            other = mutableListOf(def("Read patient diagnosis", 30), def("Procedure learning", 40))
        ),
        "strengthXp" to ActivityList(
            major = mutableListOf(def("Workout", 40), def("Walk 30 minutes", 30)),
            other = mutableListOf(def("Stretching", 20), def("Yoga", 25))
        ),
        "socialXp" to ActivityList(
            major = mutableListOf(def("Call family", 25), def("Meet friend", 20)),
            other = mutableListOf(def("Celebrate birthday", 30), def("Help someone", 25), def("Important message", 10))
        ),
        "hobbiesXp" to ActivityList(
            major = mutableListOf(def("Music practice", 25), def("Chess practice", 25)),
            other = mutableListOf(def("Puzzle training", 20), def("Hobby session", 20))
        )
    )

    private fun def(name: String, xp: Int) = ActivityDef("", name, xp)
}
