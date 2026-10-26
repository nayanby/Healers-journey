import com.healersjourney.app.domain.*;

public final class DailyBonusChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        String day = "2026-10-26";
        GameState state = new GameState();
        GameEngine engine = GameEngine.INSTANCE;
        engine.addXp(state, "knowledgeXp", 20, "Read", day);
        engine.addXp(state, "knowledgeXp", 20, "Practice", day);
        engine.addXp(state, "medicineXp", 20, "Work", day);
        check(state.getCoins() == 30, "repeat category does not earn category bonus");

        ActionEntry third = engine.addXp(state, "strengthXp", 20, "Workout", day);
        check(state.getCoins() == 70, "third category awards ten plus thirty");
        check(DailyCoinBonus.INSTANCE.earnedToday(state, day) == 70, "shelf shows first bonus");

        ActionEntry fourth = engine.addXp(state, "socialXp", 20, "Meet friend", day);
        check(state.getCoins() == 80, "fourth category awards only ten");
        ActionEntry fifth = engine.addXp(state, "hobbiesXp", 20, "Draw", day);
        check(state.getCoins() == 140, "fifth category awards ten plus fifty");
        check(DailyCoinBonus.INSTANCE.earnedToday(state, day) == 140, "shelf shows both bonuses");

        check(engine.undoActionById(state, fifth.getId()), "undo fifth category");
        check(state.getCoins() == 80, "fifty-coin bonus reverses");
        check(engine.undoActionById(state, third.getId()), "undo third completion");
        check(state.getCoins() == 70, "three-category bonus remains while three categories qualify");
        check(engine.undoActionById(state, fourth.getId()), "undo fourth completion");
        check(state.getCoins() == 30, "thirty-coin bonus reverses below three categories");
        check(DailyCoinBonus.INSTANCE.earnedToday(state, day) == 30, "shelf follows undo");
        System.out.println("DailyBonusChecks passed");
    }
}
