import com.healersjourney.app.domain.*;

/** Smoke checks against an isolated game state; never touches the installed user's data. */
public final class TodayAndCareChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        GameEngine engine = GameEngine.INSTANCE;
        GameState state = new GameState();
        engine.ensureDefaults(state);
        for (String skill : Rules.INSTANCE.getSKILL_KEYS()) {
            ActivityDef preset = engine.defsFor(state, skill, "major").get(0);
            int reward = engine.activityXpPreview(state, skill, preset.getXp()).getFirst();
            int before = state.getTotalXp();
            int beforeSkill = state.skillXp(skill);
            int count = state.getActionHistory().size();
            check(engine.completeActivity(state, skill, "major", 0) instanceof Outcome.Applied, "Preset rejected: " + skill);
            check(state.getTotalXp() == before + reward, "Total XP differs from preview: " + skill);
            check(state.skillXp(skill) == beforeSkill + reward, "Category XP not awarded: " + skill);
            check(state.getActionHistory().size() == count + 1, "Missing history: " + skill);
            check(state.getActionHistory().get(0).getDate().equals(Dates.INSTANCE.gameDayKey(java.util.Calendar.getInstance())), "Wrong game day");
        }
        ActivityDef editable = engine.defsFor(state, "knowledgeXp", "major").get(0);
        int earned = state.getTotalXp();
        check(engine.updateActivityById(state, "knowledgeXp", editable.getId(), "Study anything", 75) instanceof Outcome.Applied, "Edit failed");
        engine.ensureDefaults(state);
        check(engine.defsFor(state, "knowledgeXp", "major").stream().anyMatch(d -> d.getName().equals("Study anything") && d.getXp() == 75), "Edit was reset");
        check(engine.removeActivityById(state, "knowledgeXp", editable.getId()) instanceof Outcome.Applied, "Major removal failed");
        ActivityDef other = engine.defsFor(state, "socialXp", "other").get(0);
        check(engine.removeActivityById(state, "socialXp", other.getId()) instanceof Outcome.Applied, "Other removal failed");
        engine.ensureDefaults(state);
        check(engine.defsFor(state, "knowledgeXp", "major").stream().noneMatch(d -> d.getId().equals(editable.getId())), "Deleted major returned");
        check(engine.defsFor(state, "socialXp", "other").stream().noneMatch(d -> d.getId().equals(other.getId())), "Deleted other returned");
        check(state.getTotalXp() == earned, "Editing changed earned XP");
        String previous = state.getPersonalCareLast().get("shower");
        String today = Dates.INSTANCE.todayKey();
        String selected = Dates.INSTANCE.addDays(today, -3);
        int xpBeforeCare = state.getTotalXp();
        check(engine.completePersonalCare(state, "shower", selected) instanceof Outcome.Applied, "Backdated care rejected");
        check(selected.equals(state.getPersonalCareLast().get("shower")), "Selected completion date lost");
        check(state.getTotalXp() == xpBeforeCare, "Care unexpectedly changed XP");
        check(engine.personalCareStats(state).getItems().stream().filter(i -> i.getKey().equals("shower")).findFirst().get().getDays() == 3, "Care age ignores selected date");
        check(engine.undoActionById(state, state.getActionHistory().get(0).getId()), "Care undo failed");
        check(previous.equals(state.getPersonalCareLast().get("shower")), "Undo did not restore prior care date");
        System.out.println("PASS: five category presets award XP and history; selected care date, age, and undo remain correct.");
    }
}
