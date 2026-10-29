# Life RPG Android

An Android life RPG where real-life activities grow your character, earn coins, and unlock outfits, companions and room backdrops. Package: `com.life.rpg`.

The Kotlin / Jetpack Compose project lives in `HealersJourney/`. Its four main tabs are Home, Today, Journal and Store. Artwork uses individual pixel PNGs, while text stays clear and readable.

## Current experience

- Home centers your character and companion against the equipped backdrop. Swipe the five-category XP graph through Day, Week, Month, Year and Lifetime. Tap your character to open Care.
- Today lets you plan tasks from saved activities or create new ones. Completed tasks remain visible with a strike-through. Undoing or removing a completed task reverses its linked XP and coin rewards. Long-press saved activities to edit or delete them.
- A dated pixel shelf displays five category-specific trophy PNGs, today's earned coins and category bonus goals. Tap a trophy for its activity, date, XP and coins, or browse the full collection. Date navigation slides the shelf between days.
- Today supports optional focus sessions with pause, resume and cancellation. Timers persist across navigation and restarts; once time is up, finishing the session completes its linked task with the usual rewards exactly once. No background alarm is scheduled.
- Recurring tasks appear on selected weekdays, beginning when their schedule is created. Open task copies are editable; removed copies stay removed, and schedule changes preserve completed history. The game day still rolls over at 5 AM.
- Personal goals in Today and Journal → Goals & milestones count completions for a calendar month, either across one category or for an exact activity name. Choose this or next month with a readable month selector. Done today records one 0-XP, 10-coin completion if no matching activity has already counted today; its check-in can be undone. Reaching a target earns a commemorative trophy. Undo and deletion update goal progress and its trophy.
- Store offers outfits, companions and backdrops for coins. Male and female characters have matching outfit tiers, with care states that change their appearance, expression and pose.
- Care starts with Shower and Nails. Choose other common routines or add a custom routine, set its frequency, and long-press to edit or remove it.
- Journal has Your journey and My thoughts tabs. My thoughts is plain text, with one automatically saved entry per calendar date and no custom title field. Entries stay on-device and are included in local saves and backup exports. Your journey contains history, Goals & milestones and finance; the Achievements page has been removed. Goals and milestones have separate tabs, with active/achieved goal filters and upcoming/remembered milestones. Milestones can be edited and pinned to Home. Journal's Material settings icon opens character preferences, backup, sync information, feedback settings and data reset. First launch asks for a username and character.
- Tasks, saved activities and recurring tasks can use 0 XP while retaining normal completion records, coins, trophies and goal progress. The activity picker carries typed search text into new-task and new-preset forms; Save as a new activity stores the typed task for reuse.

## Rewards

Each completed activity in Learning, Work, Fitness, Social or Creativity earns 10 coins. Completing activities in any three categories earns an additional 30 coins; all five categories earns another 50 coins. Undo reverses linked rewards. If coins were already spent, later earnings repay the difference.

Each category's first 200 base XP per game day earns 100%, the next 200 earns 50%, and the rest earns 25%. Cumulative rounding prevents extra rewards from splitting an activity. The game day resets at 5 AM.

## Build

Use Android SDK 36 and JDK 17 or a compatible Android Studio runtime. Configure `ANDROID_HOME` or an untracked `HealersJourney/local.properties`.

```powershell
cd HealersJourney
.\gradlew.bat assembleDebug
```

Release signing uses `LIFERPG_UPLOAD_STORE_FILE` and `LIFERPG_UPLOAD_PASSWORD` environment variables. Signing keys, passwords and machine-specific configuration are excluded from Git.

The isolated regression checks in `HealersJourney/tools/` cover task undo, coins, daily category bonuses, care routines, planning and the daily shelf. `PlanningChecks.java` checks recurring copies, focus recovery, goal progress, undo, backup round-trips and older saves. They do not modify installed user data.

## Google Play Games and privacy

Google Play Games Services v2 (`22.1.0`) is initialized at app startup. Settings displays the connected player name and offers sign-in. The app does not upload activity logs, care records, finances or game saves to Play Games. Google Drive sync is unavailable until its separate OAuth configuration is supplied.

Public integration identifiers are recorded in `HealersJourney/store-listing/play-games-setup.json`. Debug and Play-distributed builds require matching registered signing certificates and an eligible Play Games tester account while the service is in testing.

See [PRIVACY.md](PRIVACY.md) for local storage, SDK collection and deletion instructions. Google Play's updated Data safety declaration was published on 27 October 2026.

## Artwork and store listing

Runtime artwork is in `HealersJourney/app/src/main/res/`. Design sources and generation prompts are under `HealersJourney/design/`. Store descriptions, graphics, screenshots and their tooling are under `HealersJourney/store-listing/`; generated application bundles and private publishing files are excluded.

The five trophies use transparent PNG artwork with dimensional pixel-art shading. Original generated images and prompts are in `HealersJourney/design/trophies/`; `tools/export_trophy_pngs.py` creates optimized 512-pixel runtime PNGs. Shelf, collection and personal goals use the same cached images. Character artwork and level badges retain their existing style.
