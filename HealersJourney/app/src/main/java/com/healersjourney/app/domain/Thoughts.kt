package com.healersjourney.app.domain

data class ThoughtEntry(val id: String, val date: String, val text: String)

object Thoughts {
    fun save(state: GameState, date: String, text: String): Outcome {
        if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) || Dates.keyToCal(date)?.let { Dates.dateKeyOf(it) } != date)
            return Outcome.Failure("This entry needs a valid date.")
        val index = state.thoughts.indexOfFirst { it.date == date }
        if (index < 0 && text.isBlank()) return Outcome.Applied
        val entry = ThoughtEntry(state.thoughts.getOrNull(index)?.id ?: Ids.make("thought"), date, text.take(50_000))
        if (index >= 0) state.thoughts[index] = entry else state.thoughts.add(entry)
        return Outcome.Applied
    }
}
