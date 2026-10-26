import com.healersjourney.app.domain.*;
import com.healersjourney.app.data.backup.StateJson;
import java.util.*;

public final class RewardsAndRoutinesChecks {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static GameState fresh() { GameState s = new GameState(); GameEngine.INSTANCE.ensureDefaults(s); return s; }
    public static void main(String[] args) {
        GameEngine e = GameEngine.INSTANCE;
        String today = Dates.INSTANCE.gameDayKey(Calendar.getInstance());
        GameState s = fresh();
        e.addXp(s, "knowledgeXp", 200, "First", today);
        check(s.getTotalXp() == 200, "Full tier wrong");
        check(e.activityXpPreview(s, "knowledgeXp", 200).getFirst() == 100, "Half tier preview wrong");
        e.addStudySession(s, 4);
        check(s.getTotalXp() == 300, "Study bypassed shared calculation");
        e.addXp(s, "knowledgeXp", 100, "More", today);
        check(s.getTotalXp() == 325, "Quarter tier wrong");
        check(e.activityXpPreview(s, "socialXp", 20).getFirst() == 20, "Other category penalized");
        check(e.activityXpPreviewForDate(s, "knowledgeXp", 20, Dates.INSTANCE.addDays(today,-1)).getFirst() == 20, "Date budgets not isolated");
        GameState a=fresh(), b=fresh();
        e.addXp(a,"hobbiesXp",450,"Single",today);
        for(int i=0;i<450;i++) e.completeHobbyActivity(b,"Art","",1);
        check(a.getTotalXp() == b.getTotalXp(), "Splitting reward exploit");
        int earned=s.getTotalXp();
        e.saveCareRoutine(s,"shower","Quick shower",3);
        check(e.personalCareStats(s).getItems().stream().anyMatch(it -> it.getLabel().equals("Quick shower") && it.getCycleDays()==3), "Care edit lost");
        check(e.saveCareRoutine(s,null,"Floss",1) instanceof Outcome.Applied,"Add routine failed");
        check(e.saveCareRoutine(s,null,"Floss",1) instanceof Outcome.Failure,"Duplicate routine allowed");
        check(e.saveCareRoutine(s,null,"Bad",0) instanceof Outcome.Failure,"Invalid frequency allowed");
        e.removeCareRoutine(s,"haircut");
        s.getWorldUnlocks().add("royal_cape");
        s.getWorldEquipment().put("outfit","royal_cape");
        GameState local = new GameState();
        StateJson.INSTANCE.restoreLocalExtras(local, StateJson.INSTANCE.encodeLocalExtras(s).toString());
        check(local.getCareRoutines().size()==s.getCareRoutines().size() && local.getCareRoutinesConfigured(),"Local database lost care settings");
        check("royal_cape".equals(local.getWorldEquipment().get("outfit")) && local.getWorldUnlocks().contains("royal_cape"),"Local database lost equipment");
        GameState restored=StateJson.INSTANCE.decode(StateJson.INSTANCE.backupEnvelope(s));e.ensureDefaults(restored);
        check(restored.getCareRoutines().size()==s.getCareRoutines().size(),"Care backup lost changes");
        check(restored.getCareRoutines().stream().noneMatch(it->it.getKey().equals("haircut")),"Deleted routine resurrected");
        for(Rules.CareRule r:new ArrayList<>(restored.getCareRoutines()))e.removeCareRoutine(restored,r.getKey());
        restored=StateJson.INSTANCE.decode(StateJson.INSTANCE.backupEnvelope(restored));e.ensureDefaults(restored);
        check(restored.getCareRoutines().isEmpty(),"Empty routines resurrected defaults");
        check(s.getTotalXp()==earned,"Care settings changed XP");
        e.addTodoTask(s,"Planned","socialXp",25);
        check(e.addTodoTask(s,"Planned","socialXp",25) instanceof Outcome.Failure,"Duplicate planned task");
        GameState rollover=fresh();
        rollover.setTodoDate(Dates.INSTANCE.addDays(today,-1));
        rollover.getTodoHistory().put(today,new ArrayList<>(s.getTodoTasks()));
        e.ensureTodoTasks(rollover);
        check(rollover.getTodoTasks().size()==1,"Scheduled task lost at rollover");
        GameState legacy=StateJson.INSTANCE.decode("{\"totalXp\":123}");e.ensureDefaults(legacy);
        check(legacy.getCareRoutines().size()==5 && legacy.getTotalXp()==123,"Legacy migration changed XP or lost routines");
        System.out.println("PASS: reward tiers, shared session rewards, split invariance, date budgets, care CRUD, validation, persistence and migration.");
    }
}
