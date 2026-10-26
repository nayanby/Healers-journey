package com.healersjourney.app.domain

data class WorldItem(val id: String, val name: String, val slot: String, val xp: Int = 0, val adventures: Int = 0)
data class DailyQuest(val skill: String, val activity: ActivityDef, val completed: Boolean)
data class AdventureProgress(val actions: Int, val days: Int, val categories: Int) {
    val complete get() = actions >= 12 && days >= 3 && categories >= 3
    val fraction get() = (actions.coerceAtMost(12) + days.coerceAtMost(3) + categories.coerceAtMost(3)) / 18f
}

/** Cosmetics are bought with activity coins; owned items remain unlocked. */
object WorldRules {
    val items = listOf(
        WorldItem("traveler", "Traveler", "outfit"),
        WorldItem("forest_cloak", "Forest cloak", "outfit", 250),
        WorldItem("sun_cape", "Sun cape", "outfit", 1000),
        WorldItem("royal_cape", "Royal cape", "outfit", 3000),
        WorldItem("cozy_knit", "Cozy knit & beanie", "outfit", 450),
        WorldItem("ocean_explorer", "Ocean explorer", "outfit", 600),
        WorldItem("ranger", "Woodland ranger", "outfit", 1800),
        WorldItem("winter_coat", "Winter coat & scarf", "outfit", 2500),
        WorldItem("starlight_robes", "Starlight robes", "outfit", 4500),
        WorldItem("golden_knight", "Golden knight", "outfit", 6500),
        WorldItem("plain", "Cozy room", "decor"),
        WorldItem("flowers", "Wildflower garden", "decor", 150),
        WorldItem("lanterns", "Starlight lanterns", "decor", 900),
        WorldItem("bookshelf", "Reading nook", "decor", 350),
        WorldItem("rug", "Woven rug & cushions", "decor", 650),
        WorldItem("plants", "Indoor jungle", "decor", 1100),
        WorldItem("aquarium", "Little aquarium", "decor", 1400),
        WorldItem("telescope", "Stargazer telescope", "decor", 2800),
        WorldItem("fireplace", "Warm hearth", "decor", 4000),
        WorldItem("trophy", "Champion display", "decor", 5000),
        WorldItem("none", "Solo journey", "companion"),
        WorldItem("firefly", "Little firefly", "companion", 500),
        WorldItem("sprite", "Forest sprite", "companion", 2000),
        WorldItem("star", "Star companion", "companion", 3500),
        WorldItem("fox", "Copper fox", "companion", 650),
        WorldItem("bunny", "Little bunny", "companion", 850),
        WorldItem("cat", "Silver cat", "companion", 1200),
        WorldItem("owl", "Wise owl", "companion", 1600),
        WorldItem("cloud", "Cloud friend", "companion", 2500),
        WorldItem("dragon", "Pocket dragon", "companion", 5000),
        WorldItem("starter_room", "Traveler's room", "backdrop"),
        WorldItem("garden_room", "Woodland garden", "backdrop", 250),
        WorldItem("reading_room", "Cozy reading room", "backdrop", 450),
        WorldItem("ocean_room", "Ocean cabin", "backdrop", 600),
        WorldItem("lantern_room", "Lantern room", "backdrop", 900),
        WorldItem("workshop_room", "Ranger workshop", "backdrop", 1800),
        WorldItem("winter_room", "Winter hearth", "backdrop", 2500),
        WorldItem("royal_room", "Royal chamber", "backdrop", 3000),
        WorldItem("celestial_hall", "Celestial hall", "backdrop", 4000),
        WorldItem("golden_hall", "Golden hall", "backdrop", 6500),
        WorldItem("meadow", "Meadow", "scenery"),
        WorldItem("sunset", "Sunset valley", "scenery", 750),
        WorldItem("moonlight", "Moonlit realm", "scenery", 1800),
        WorldItem("forest", "Whispering forest", "scenery", 1100),
        WorldItem("beach", "Coral coast", "scenery", 1700),
        WorldItem("snow", "Snowy retreat", "scenery", 2400),
        WorldItem("aurora", "Aurora night", "scenery", 4000),
        WorldItem("sky_islands", "Sky islands", "scenery", 6000)
    )
    val defaults = mapOf("outfit" to "traveler", "decor" to "plain", "companion" to "none", "scenery" to "meadow", "backdrop" to "starter_room")

    /** Both final visible items cost 3,500 coins, about 175 days at two activities a day. */
    private val visibleItemPrices = mapOf(
        "forest_cloak" to 50, "cozy_knit" to 80, "ocean_explorer" to 120,
        "sun_cape" to 180, "ranger" to 300, "winter_coat" to 450,
        "royal_cape" to 650, "starlight_robes" to 1000, "golden_knight" to 2000,
        "firefly" to 50, "fox" to 100, "bunny" to 150, "cat" to 220,
        "owl" to 350, "sprite" to 500, "cloud" to 700, "star" to 1000,
        "dragon" to 1500,
        "garden_room" to 50, "reading_room" to 80, "ocean_room" to 120,
        "lantern_room" to 180, "workshop_room" to 300, "winter_room" to 450,
        "royal_room" to 650, "celestial_hall" to 1000, "golden_hall" to 2000
    )

    fun coinPrice(item: WorldItem): Int = visibleItemPrices[item.id]
        ?: if (item.id in defaults.values) 0 else ((item.xp.coerceAtLeast(150) + 249) / 250) * 50

    fun reconcile(state: GameState) {
        state.worldUnlocks.addAll(defaults.values)
        defaults.forEach { (slot, id) ->
            if (state.worldEquipment[slot] !in state.worldUnlocks || items.none { it.id == state.worldEquipment[slot] && it.slot == slot })
                state.worldEquipment[slot] = id
        }
    }

    fun equip(state: GameState, id: String): Outcome {
        reconcile(state)
        val item = items.find { it.id == id } ?: return Outcome.Failure("Item not found.")
        if (id !in state.worldUnlocks) return Outcome.Failure("Buy this item with coins first.")
        state.worldEquipment[item.slot] = id
        return Outcome.Applied
    }

    fun buyAndEquip(state: GameState, id: String): Outcome {
        reconcile(state)
        val item = items.find { it.id == id } ?: return Outcome.Failure("Item not found.")
        if (id in state.worldUnlocks) return equip(state, id)
        val price = coinPrice(item)
        if (state.coins < price) return Outcome.Failure("You need ${price - state.coins} more coins.")
        state.coins -= price
        state.worldUnlocks.add(id)
        state.worldEquipment[item.slot] = id
        return Outcome.Applied
    }

    private fun token(skill: String, id: String) = "$skill|$id"

    fun prepareQuests(state: GameState, day: String = Dates.gameDayKey()) {
        val candidates = Rules.SKILL_KEYS.flatMap { skill ->
            val defs = state.activityDefs[skill]
            (defs?.major.orEmpty() + defs?.other.orEmpty()).map { skill to it }
        }
        val available = candidates.map { token(it.first, it.second.id) }.toSet()
        if (state.dailyQuestDate != day) { state.dailyQuestDate = day; state.dailyQuestIds.clear() }
        state.dailyQuestIds.removeAll { it !in available }
        // Stable picks survive edits and process restarts; rotate categories each game day.
        val offset = Math.floorMod(day.hashCode(), Rules.SKILL_KEYS.size)
        val ordered = Rules.SKILL_KEYS.indices.map { Rules.SKILL_KEYS[(it + offset) % Rules.SKILL_KEYS.size] }
        ordered.forEach { skill ->
            if (state.dailyQuestIds.size < 3 && state.dailyQuestIds.none { it.startsWith("$skill|") }) {
                val choices = candidates.filter { it.first == skill }
                if (choices.isNotEmpty()) {
                    val choice = choices[Math.floorMod(day.hashCode(), choices.size)]
                    state.dailyQuestIds.add(token(skill, choice.second.id))
                }
            }
        }
    }

    fun quests(state: GameState): List<DailyQuest> = state.dailyQuestIds.mapNotNull { key ->
        val skill = key.substringBefore('|')
        val lists = state.activityDefs[skill] ?: return@mapNotNull null
        val def = (lists.major + lists.other).find { token(skill, it.id) == key } ?: return@mapNotNull null
        DailyQuest(skill, def, state.actionHistory.any { it.date == Dates.gameDayKey() && it.skill == skill &&
            (it.activityId == def.id || (it.activityId == null && it.name == def.name)) })
    }

    /** A chosen board stays stable for the day; earned quests cannot be swapped away. */
    fun chooseQuests(state: GameState, selected: List<String>): Outcome {
        reconcile(state)
        val available = Rules.SKILL_KEYS.flatMap { skill ->
            val defs = state.activityDefs[skill]
            (defs?.major.orEmpty() + defs?.other.orEmpty()).map { token(skill, it.id) }
        }.toSet()
        val required = minOf(3, available.size)
        if (selected.distinct().size != required || selected.size != required || selected.any { it !in available })
            return Outcome.Failure("Choose $required different activities for your daily quests.")
        val earned = quests(state).filter { it.completed }.map { token(it.skill, it.activity.id) }
        if (!selected.containsAll(earned)) return Outcome.Failure("Completed quests stay on today's board.")
        state.dailyQuestIds.clear()
        state.dailyQuestIds.addAll(selected)
        return Outcome.Applied
    }

    fun finishQuest(state: GameState, skill: String, id: String): Outcome {
        reconcile(state)
        val quest = quests(state).find { it.skill == skill && it.activity.id == id }
            ?: return Outcome.Failure("This quest is no longer on today's board.")
        if (quest.completed) return Outcome.Failure("Quest already completed today.")
        val lists = state.activityDefs[skill] ?: return Outcome.Failure("Activity not found.")
        val major = lists.major.indexOfFirst { it.id == id }
        val index = if (major >= 0) major else lists.other.indexOfFirst { it.id == id }
        return GameEngine.completeActivity(state, skill, if (major >= 0) "major" else "other", index)
    }

    fun adventure(state: GameState, week: String = Dates.weekKeyOfNow()): AdventureProgress {
        val actions = state.actionHistory.filter { it.week == week && it.skill in Rules.SKILL_KEYS && it.xp > 0 }
        return AdventureProgress(actions.size, actions.map { it.date }.distinct().size, actions.map { it.skill }.distinct().size)
    }

    fun claimAdventure(state: GameState): Outcome {
        val week = Dates.weekKeyOfNow()
        if (week in state.adventureClaims) return Outcome.Failure("This adventure reward is already yours.")
        if (!adventure(state).complete) return Outcome.Failure("Finish all three adventure goals first.")
        state.adventureClaims.add(week)
        reconcile(state)
        return Outcome.Applied
    }

    fun placeStage(xp: Int): Int = when { xp >= 1500 -> 3; xp >= 500 -> 2; xp >= 100 -> 1; else -> 0 }
}
