# Life RPG Android

An Android life RPG where real-life activities grow your character, earn coins, and unlock outfits, companions and room backdrops. Package: `com.life.rpg`.

The Kotlin / Jetpack Compose project lives in `HealersJourney/`. Its four main tabs are Home, Today, Store and Journal. Artwork uses individual pixel PNGs, while text stays clear and readable.

## Current experience

- Home centers your character and companion against the equipped backdrop. Swipe the five-category XP graph through Day, Week, Month, Year and Lifetime. Tap your character to open Care.
- Today lets you plan tasks from saved activities or create new ones. Completed tasks remain visible with a strike-through. Undoing or removing a completed task reverses its linked XP and coin rewards. Long-press saved activities to edit or delete them.
- A dated pixel shelf displays round badges using the five category activity icons, today's earned coins and category bonus goals. Date navigation slides the shelf between days.
- Store offers outfits, companions and backdrops for coins. Male and female characters have matching outfit tiers, with care states that change their appearance, expression and pose.
- Care starts with Shower and Nails. Choose other common routines or add a custom routine, set its frequency, and long-press to edit or remove it.
- Journal contains history, achievements, milestones and finance tools. Its Material settings icon opens character preferences, backup, sync information, feedback settings and data reset. First launch asks for a username and character.

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

The isolated regression checks in `HealersJourney/tools/` cover task undo, coins, daily category bonuses, care routines and the daily shelf. They do not modify installed user data.

## Google Play Games and privacy

Google Play Games Services v2 (`22.1.0`) is initialized at app startup. Settings displays the connected player name and offers sign-in. The app does not upload activity logs, care records, finances or game saves to Play Games. Google Drive sync is unavailable until its separate OAuth configuration is supplied.

Public integration identifiers are recorded in `HealersJourney/store-listing/play-games-setup.json`. Debug and Play-distributed builds require matching registered signing certificates and an eligible Play Games tester account while the service is in testing.

See [PRIVACY.md](PRIVACY.md) for local storage, SDK collection and deletion instructions. Google Play's updated Data safety declaration was published on 27 October 2026.

## Artwork and store listing

Runtime artwork is in `HealersJourney/app/src/main/res/`. Design sources and generation prompts are under `HealersJourney/design/`. Store descriptions, graphics, screenshots and their tooling are under `HealersJourney/store-listing/`; generated application bundles and private publishing files are excluded.
