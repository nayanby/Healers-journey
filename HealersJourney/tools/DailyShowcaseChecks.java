import com.healersjourney.app.domain.*;
import com.healersjourney.app.data.backup.StateJson;
import java.util.*;

public final class DailyShowcaseChecks {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        GameEngine e=GameEngine.INSTANCE;
        GameState s=new GameState();e.ensureDefaults(s);
        String today=Dates.INSTANCE.gameDayKey(Calendar.getInstance());
        String yesterday=Dates.INSTANCE.addDays(today,-1);
        check(DailyShowcase.INSTANCE.entries(s,today).isEmpty(),"Empty day got objects");
        e.addTodoTask(s,"Plan only","knowledgeXp",20);
        check(DailyShowcase.INSTANCE.entries(s,today).isEmpty(),"Planning gave object");
        for(String skill:Rules.INSTANCE.getSKILL_KEYS()) e.addXp(s,skill,20,"Completed "+skill,today);
        check(DailyShowcase.INSTANCE.entries(s,today).size()==5,"Missing category completions");
        check(DailyShowcase.INSTANCE.getCollections().stream().map(DailyShowcase.Collection::getTitle).distinct().count()==5,"Main categories lack distinct badges");
        check(DailyCoinBonus.INSTANCE.earnedToday(s,today)==130,"Five activities and both bonuses should total 130 coins");
        e.addXp(s,"knowledgeXp",20,"Yesterday",yesterday);
        check(DailyShowcase.INSTANCE.entries(s,today).size()==5 && DailyShowcase.INSTANCE.entries(s,yesterday).size()==1,"Dates mixed");
        e.undoActionById(s,s.getActionHistory().get(0).getId());
        check(DailyShowcase.INSTANCE.entries(s,yesterday).isEmpty(),"Undo left badge");
        e.completePersonalCare(s,"shower",yesterday);
        check(DailyShowcase.INSTANCE.entries(s,yesterday).isEmpty() && DailyShowcase.INSTANCE.entries(s,today).size()==5,"Care appeared on badge shelf");
        GameState restored=StateJson.INSTANCE.decode(StateJson.INSTANCE.backupEnvelope(s));e.ensureDefaults(restored);
        check(DailyShowcase.INSTANCE.entries(restored,today).equals(DailyShowcase.INSTANCE.entries(s,today)),"Backup changed display order or objects");
        Achievement ten=Achievements.INSTANCE.all(s).stream().filter(a->a.getName().equals("Ten Actions Logged")).findFirst().get();
        check(ten.getCurrent()==6 && ten.getTarget()==10 && !ten.getUnlocked(),"Achievement numeric progress wrong");
        System.out.println("PASS: five category badges, bonuses, planning/completion, date isolation, undo, care exclusion, backup order and numeric achievements.");
    }
}
