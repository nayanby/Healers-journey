import com.healersjourney.app.domain.*;
import com.healersjourney.app.data.backup.StateJson;
import java.util.*;

/** Isolated progression and save-compatibility checks. Never reads installed app data. */
public final class WorldChecks {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static GameState fresh() {
        GameState state = new GameState();
        GameEngine.INSTANCE.ensureDefaults(state);
        WorldRules.INSTANCE.reconcile(state);
        return state;
    }
    public static void main(String[] args) {
        GameEngine engine = GameEngine.INSTANCE;
        WorldRules world = WorldRules.INSTANCE;
        GameState state = fresh();
        check(state.getDailyQuestIds().isEmpty(), "Automatic three-task board still generated");
        check(engine.addTodoTask(state, "My chosen task", "knowledgeXp", 30) instanceof Outcome.Applied, "Task add failed");
        String taskId = state.getTodoTasks().get(0).getId();
        check(engine.completeTodoTask(state, taskId) instanceof Outcome.Applied, "Task completion failed");
        check(state.getTotalXp() == 30, "Wrong task reward");
        engine.completeTodoTask(state, taskId);
        check(state.getTotalXp() == 30, "Repeated completion awarded XP");
        check(engine.undoActionById(state, state.getActionHistory().get(0).getId()), "Task undo failed");
        check(!state.getTodoTasks().get(0).getCompleted() && state.getTotalXp() == 0, "Undo did not reopen planned task");
        check(world.equip(state, "royal_cape") instanceof Outcome.Failure, "Locked outfit equipped");
        state.setTotalXp(1000); world.reconcile(state);
        check(state.getWorldUnlocks().containsAll(Arrays.asList("flowers", "forest_cloak", "firefly", "sunset", "sun_cape")), "XP unlocks missing");
        check(world.equip(state, "sun_cape") instanceof Outcome.Applied, "Owned outfit rejected");
        state.setTotalXp(0); world.reconcile(state);
        check(state.getWorldUnlocks().contains("sun_cape"), "Earned cosmetic was revoked");
        check("sun_cape".equals(state.getWorldEquipment().get("outfit")), "Equipped outfit lost");

        GameState adventure = fresh();
        check(world.claimAdventure(adventure) instanceof Outcome.Failure, "Incomplete adventure claimed");
        List<String> days = Dates.INSTANCE.currentWeekKeys();
        List<String> skills = Rules.INSTANCE.getSKILL_KEYS();
        for (int d = 0; d < 3; d++) for (int a = 0; a < 4; a++) engine.addXp(adventure, skills.get(d), 20, "Adventure step", days.get(d));
        check(world.adventure(adventure, Dates.INSTANCE.weekKeyOfNow()).getComplete(), "Balanced week should finish adventure");
        int adventureXp = adventure.getTotalXp();
        check(world.claimAdventure(adventure) instanceof Outcome.Applied, "Finished adventure claim rejected");
        check(adventure.getWorldUnlocks().contains("lanterns"), "First adventure reward missing");
        check(world.claimAdventure(adventure) instanceof Outcome.Failure, "Weekly reward claimed twice");
        check(adventure.getTotalXp() == adventureXp, "Cosmetic reward altered XP");
        check(world.placeStage(0) == 0 && world.placeStage(100) == 1 && world.placeStage(500) == 2 && world.placeStage(1500) == 3, "Place upgrade thresholds wrong");

        adventure.setSoundEnabled(false); adventure.setHapticsEnabled(false);
        world.equip(adventure, "lanterns");
        String encoded = StateJson.INSTANCE.backupEnvelope(adventure);
        GameState restored = StateJson.INSTANCE.decode(encoded);
        engine.ensureDefaults(restored); world.reconcile(restored);
        check(restored.getTotalXp() == adventure.getTotalXp(), "Backup changed XP");
        check(restored.getWorldUnlocks().equals(adventure.getWorldUnlocks()), "Backup lost unlocks");
        check(restored.getWorldEquipment().equals(adventure.getWorldEquipment()), "Backup lost equipment");
        check(restored.getAdventureClaims().equals(adventure.getAdventureClaims()), "Backup lost adventure seals");
        check(restored.getDailyQuestIds().equals(adventure.getDailyQuestIds()), "Backup changed daily quests");
        check(!restored.getSoundEnabled() && !restored.getHapticsEnabled(), "Feedback preferences not restored");
        GameState legacy = StateJson.INSTANCE.decode("{\"playerName\":\"Legacy\",\"totalXp\":750}");
        engine.ensureDefaults(legacy); world.reconcile(legacy);
        check("Legacy".equals(legacy.getPlayerName()) && legacy.getTotalXp() == 750, "Legacy save changed");
        check(legacy.getWorldUnlocks().contains("sunset") && world.quests(legacy).isEmpty(), "Legacy save not initialized for world");
        GameState planRestore = StateJson.INSTANCE.decode(StateJson.INSTANCE.backupEnvelope(state));
        engine.ensureDefaults(planRestore);
        check(planRestore.getTodoTasks().size() == 1 && !planRestore.getTodoTasks().get(0).getCompleted(), "Backup lost personal plan");
        System.out.println("PASS: personal planner, completion/undo, unlocks, equipment, adventure, backup and legacy compatibility.");
    }
}
