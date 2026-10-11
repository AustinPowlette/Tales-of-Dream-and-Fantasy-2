# CLAUDE.md — Tales of Dream and Fantasy 2

## Project Summary
Java xianxia cultivation simulation. 100K+ NPC world, turn-based (weekly ticks), procedurally generated. Entry point: `src/Game/GameDriver.java`. All game logic is in `src/Game/`. No build tool (IntelliJ project, `.iml` file present).

## Key Documentation
All the design docs and notes live in the `GameInfo/` folder in the project root (every `.md` except this file, and the three original design `.txt` files). The links below point there.
- [Game Info.md](GameInfo/Game Info.md) — Full technical deep-dive: systems, combat formulas, bugs, class reference
- [Game Outline.md](GameInfo/Game Outline.md) — ✅/🟡/❌ checklist of every feature (implemented / partial / not started)
- [Naming Review.md](GameInfo/Naming Review.md) — dictionary audit, name engines, terminal/GUI ideas (all implemented)
- [Character Starts.md](GameInfo/Character Starts.md) — start kinds, stories, difficulty tiers (implemented)
- [Balance Plan.md](GameInfo/Balance Plan.md) — measured balance problems (massacre of children at start, steep progression, inflation, few settlements) and the phased plan; `tools/BalProbe.java` measures them
- [Lore Design.md](GameInfo/Lore Design.md) — the Palace of the Oldest Order story (implemented; all text is data in `LoreText.java`); open questions for the author at the end
- [Playtest Findings.md](GameInfo/Playtest Findings.md) — the 2026-10-09 live playtest at 100,000: where the tick time and memory go (measured), the 28 bugs with repro (fixed the same day, except the ones marked), balance notes; `tools/perf-proposal.diff` holds the speed/memory patches that were measured but not applied
- [Performance Report.md](GameInfo/Performance Report.md) — what takes the time and the memory after 10-50 years, what was fixed, what is left
- [planning.md](GameInfo/planning.md) — Player-control system design and implementation notes (manual / auto / hybrid modes, quest tools, decision broker, scripts); implemented
- Design docs: `GameInfo/CultivationGame2.txt`, `GameInfo/CultivationGame2Recent.txt`, `GameInfo/Cultivation Game 2 Temp.txt`

## Current State (as of 2026-09-19)
All systems are now implemented. The simulation runs a full autonomous loop with quests, economy, sect AI, world events, and depth mechanics.

- **Quest system** — complete. All quest types: Forage/Mine prereq, Hunt Beast, Buy Gear, Go Sell, Battle Rival/Revenge/Bounty, Join Sect, Seek Potential, Return to Sect, Deliver Materials, sect missions, tournaments, arena registration, Ghost City Rebirth, Get Pregnant, Find Parent's Body, Sell Technique, Rob Caravan, Heaven Treasure, Legacy Discovery, and more.
- **Economy/Shops** — complete. `Shop.java` exists. `cityOwner` set by `cityLeadership()`. Tax collected yearly. Merchant arbitrage. NPC pill buying. Library buy/sell. Trade Caravan population growth (earnings/1M → city pop).
- **Sect/Organization AI** — complete. Sect missions (Forage/Mine/Hunt Beast). Promotions. Tournaments. Politics. Return to Sect drift. Sect cultivation bonus.
- **Monster AI** — complete. Hunt Cultivator spot quest, Seek Territory target quest, cultivation XP from kills, legendary milestone rewards (cultivate + potential + bounties).
- **Cultivation depth** — complete. Breakthrough Tribulations (majorLvl 3/5/7/9), Dual Cultivation (spouse/friend bonus), Legacy Titles (meditation on fallen legends), Forbidden Technique (existing).
- **World events** — complete. Beast Tides (yearly overflow from Beast Lairs), Celestial Trials (temporary lvl≥20 dungeon, 10-tick timer), Heaven-Sent Treasure (every 3 years, claimed via target quest), Hotspot faction wars (threshold 4).
- **Items** — complete. Weapon Enchantments (Innovative 5%/turn, +1–5 bonus to tempLvl).
- **Player control** — complete. Human characters run in MANUAL / AUTO / HYBRID (`PlayerControl`, `PlayerCommands`, `DecisionBroker`, `PlayerScript`, `QuestQueue`); type `help` in-game for the command list.
- **Names, art, music (2026-10-04)** — `Naming` (recipes, vintages, pet names, clans, festivals, tournaments, wars, eras), `Regions` (named seas/ranges/rivers from terrain), `ArtCity` (painted Originis, skylines, sect halls, seasons), `Ambient`+`MidiOut` (composed music, built-in stereo synth or a SoundFont from `soundfonts/`), `GuiSheets` (readable non-modal windows, grouped Me tab).
- **Play helpers (2026-10-05)** — diagonal moves, `goquest`/`chase`, `quit`, mature start, sect cap, war life cycle, treachery, squad caps; `ArtMore` (portraits, seals, icons, painted map, event art, weather). `Playtest Plan.md` + `tools/PlaytestProbe.java` describe the next bug and balance pass.
- **Story, reports, windows (2026-10-06)** — `Lore*` (Palace of the Oldest Order at Originis, master "???", Convocation, nine places, crossing at level 101; `settings lore`), `FightReport` (+10% feud penalty), `SectPolitics` (bullying, murder, theft, coups), spoilage, logo + `tools/make-exe.bat`, the dock rebuilt (`GuiDock`: Move buttons, one Quest Log, Duties window, Keys sheet, "?" on every window), the illustrated Help (`GuiHelp`, `HelpPages*`, `assets/help`, `tools/HelpShots.java`), terminal status block / help categories / did-you-mean (`Term*`), `Perf` timing. Probes: `SimProbeX`, `LoreProbe` (scratchpad `ag/probes`).
- **Economy (2026-10-03)** — `Treasure` (a Heaven-Sent treasure every year, 8 kinds), `Auctions` (consigned luxuries only: Heaven-Sent items, Potential Pills, vintage goods; 5 lots or 6 weeks), Refine Blood quest, library one-book rule (20% fee, seller loses it), shops without a stock cap, manual duties walk only on `watch` turns. Probes `EconProbe`, `MoveProbe`, `EconSim`.
- **Polish (2026-10-06)** — `AmateurCraft` (everyone crafts; Craft button is a menu), Study Trip, squad/friend policies + `avoid`, sect mission as a Quest Log entry (`mission`), info pop-ups while paused (`servePausedInfo`), cheats for Cheaters only, NPC `knownLocations` cap and the quest-name bit mask (`Performance Report.md`). Probes `PolishProbe`.
- **World (2026-10-07)** — place names revert, map zoom + cached Tile pics, 25+ cities and sects (`Settlements`, Evil razes only above 25), a tenth of every sale to the city treasury, the epoch by tick time (a blessing of potential), gear flows to buyers (shops, then the pool), instruments mature + the Perform quest. Probes `WorldProbe`, `CitySim`, `QuestSim`.
- **Quest life (2026-10-08)** — `QuestLife`: quest families with target shares per trait / age / alignment and a fatigue rule steering what NPCs and AUTO characters do; eleven routine quests (visits, teaching, courtship, trade runs, pilgrimage, meditation retreats, taming, pills, auctions, founding); spirit veins draw retreats, dual cultivation, cities and sects; Gatherers and Miners gather. Probe `QuestLifeProbe`; `QuestSim` shows shares against targets.
- **Fixes, Profession, Dialogue (2026-10-09)** — the Quest Log drops quests with a gone target, meeting a walker, the Profession button (`Profession`) for every trade, Inspiration for Innovators, Meditate 1.5 ticks + the four techniques, Hunt Monster, Monsters awaken from every wild beast kill, scripts run any command + saved scripts (`ScriptStore`), results shown (treasure, gear, tournaments), and **Dialogue** (`Dialogue`, `DialogueText`, `GuiDialogue`: lines, scenes with a portrait window, the Talk conversation; `settings dialogue|dialoguepopups|dialogueauto`). Probes `UpdateProbe`, `SilenceProbe`, `DialogueProbe`, `DialogueGui`, `SpotSim`.
- **Balance and speed (2026-10-09)** — auction bids are a few reserves (not the purse), shops get starter stock, the start spread is 21, titles go to the ten best of each kind, the Epoch population target is 200,000, NPCs look at the dynamic quest triggers every second turn (`Character.TRIGGER_EVERY`, dice scaled), relationship cap (`tdf.relCap`), cached quest-name bits. Probe `BalanceProbe`. Tick time at 100,000 (quiet machine): 746 ms (years 0-2) against 1,619 before.
- **Planned moves, fixes, parallel turns (2026-10-10)** — an NPC acts on its tile and only PLANS one step (`Character.planning`, `pendTo`); the steps are taken together after the people who play have acted (`GameDriver.applyMoves`), so Go There reads exact planned steps (`interceptAim`, `leadAim`); the order of a tick is NPC phase, births merged (`Map.mergeBirths`), people who play, steps. Epoch limits are settings (`epochpop`, `epochseconds`, `epochcull`). `Settings.parallelNpcs` (`settings parallel on`, EXPERIMENTAL, default off) plays the NPCs of different tiles together on all cores: reaches beyond a tile go through `Map.later`, tile lists are `SafeList`, `Verify` (`-Dtdf.verify=true`) checks the world each tick. Probes freeze NPCs before the first tick. Probe `PlanProbe`.
- **Music (2026-10-10)** — seven written songs (`SongTale` main theme, `SongRoad`, `SongMarket`, `SongNight`, `SongDanger`, `SongWar`, `SongSect`; text scores parsed by `Score`, played by `Conductor` with layers by mood, fades, rests, varied performances), 15 instruments, SoundFont by default with measured loudness trims (`MidiOut.TRIM_DB`); `music list|play|next`; new songs are written by hand; `SongGen` (EXPERIMENTAL, off by default, no further work planned) composes extras (`music gen`, `music keep` into `songs/`, `music share`); `tools/SongTool.java` (lint, dump, roll, midi, wav, balance, trim, gen), probes `SongProbe`, `SongGenProbe`.
- **Mission counters, boards, children (2026-10-10, second batch)** — a hunt mission counts every beast (`Character.beastFought`, `missionLine` counters), `spotaction` (what the spare moment of a turn does), `seek <id> talk|court`, `explore`, spoiled goods pay their owner (`Life.refundShare`, `settings spoilrefund`), the Convocation is not for sect members (`Lore.isEntryTournament`), nobody under 18 dies (`Character.childSafe`), `Rankings` (the hundred Strongest / Richest / Most Kills with three tiers of titles, yearly; the epoch spares them, the thousand strongest, the Palace's road and children). Probe `FixesProbe`.
- **Story chapters (2026-10-10, third batch)** — the ten Story starts are written as chapters (`Chapters` engine, `ChapText`, `GuiChapter`, one `Tale*` file per story, `StoryCodas` for the endings; `Story Plan.md`): scenes with named speakers and choices kept as flags, a task each, people of the story created in the world and fated to the human (`Character.fatedTo`: only that human kills them), hidden truths rolled at the start, road tales, an epilogue and five endings per start; `story replay`. Old saves keep the flat objectives (`Starts.State.v2`). Probe `TalesProbe`.
- **The Palace text and the Erased (2026-10-10, fourth batch)** — all the Palace's words rewritten in a plain voice with an ambiguous Gardener (`LoreText`, `LoreEndings`, Help glossary); `Lore.erasedYearly`: once in a century the Hollow Court puts out a seeker, remembered only by the humans who knew them. Probe `ErasedProbe`. Then `SeekerArcs` (Rival, Doubter, Pair, Defector, Teacher), `HollowCourt` (later omens, the Dimming of a city) and quiet links from four starts; probe `ArcsProbe`.
- **GUI overhaul (2026-10-10, fifth batch)** — item cards (`GuiItems`) in the Backpack, the Edit Character dialog (`GuiEditSelf`), the Script Manager (`GuiScripts`, saved scripts), and `GuiPages` sheets for family list, relationship list, past lives, biography, run statistics + hall of fame, story, hall of legends, digest, auction, city, sect, place details and the dashboard; Manage / Squad & Sect banners and grouped buttons; Rankings titles and medals. Probe `GuiShots2` renders them.
- **Size, memory, defence, feeding, skin (2026-10-10, sixth batch)** — the world's size is asked at a new game (`StartMenu.askPopulation`, `Settings.startPopulation`, 10,000-150,000 recommended, default 100,000; the smoke test's `N` answers it with the default), saves are gzip, `Slim` keeps an old world small (husks, relationship cap 40, errand caps, 12% of people keep a beast), `MemWatch` brings the epoch at 70% of `-Xmx` alive (run with `-Xmx3g`; RAM in the task manager is the JVM's heap, not the need), `Defense` (alarm + Defend quest in sects and cities), feed policy (`policy feed`), portrait skin tone; an array costs 600 of one stack; an NPC sect master can refuse an alliance (`Organization.allianceRefusal`); `tools/make-exe.bat` caps the heap at 3 GB; 70 hand-written songs with a drum kit (`kit`, named pieces, accents, swing) and 24 instruments (`Ambient.INST`, `MidiOut` shares its 16 channels, `Score.DRUMS`), `SongTool similar`. Probes `DefendProbe`, `SlimProbe`; tools `MemProbe`, `SaveStats`.
- **Potential curve, epoch realms, tile limit (2026-10-11)** — `Character.givePotential(pills)` (5, 7, 9, 11, 13, 15, 18, 22, 26, 30 Potential Pills from gate to gate, ten gates 20/37/52/65/76/85/92/97/99/100, `potentialProgress` keeps the fraction; every chance gain is one pill's worth, exact rewards still set the number), True Immortal needs 100 potential and the 1 in 5 roll, the epoch culls only Major Lvl 0-`epochrealm` (3) and blesses the survivors of the culled realms with half a pill, a tile is overcrowded above `crowd` (3.5) times its fair share of the starting population (364 at 100,000). Probes `PotentialProbe`, `TileProbe`.
- **Look** — FlatLaf (`lib/`) + ink & jade theme (`Theme`), dock layout, painted place art (`ArtGen`). The jar is optional at compile time (reflection); run from the repo root (or keep `lib/` next to `src`).
- **Quality of life** — complete. Save slots + backups + crash slot (`SaveManager`), Settings, Notifications/Hints/Recap, combat preview, confirmations, family/relationship windows, search/follow/control, exports, tutorial. GUI/game race-freedom via `World` (see Architecture Notes).
- **Starts / difficulty** — complete. Protagonist, Standard (NPC curve, one re-roll, hidden potential), custom, ten Stories with objective chains; Story / Hard / Brutal (permadeath). NPC first generation has prehistory.
- **Naming** — complete. `Naming` engine + regenerated `Dictionary`; unique names; `Area.geoName` for pictures.
- **Settlements** — cities/sects scale with population (`Settlements.yearly`).
- **Reincarnation / Taming / Life arts / Music** — complete. `Reincarnation` (rope = level cap of the potential, "Reincarnate" quest, +5 potential, the same character reborn at 18), `Taming` (Major Lvl + 1 companions), `Life` (food, wine, poison, instruments, five new traits), `Ambient` (generated music).
- **Terrain / sects / run** — `BiomeGen` (coherent noise terrain), sect recruitment rules, `RunStats` (+ hall of fame), `Seasonal` (festivals, sect diplomacy), cities founded at Major Lvl 2.
- **Chronicle + tools** — `Chronicle`, `TextTools` (map, route, time, watch, journal, history, notes, dash, equip/compare/techniques), `GuiMore` (Backpack, Chronicle, Biography, Notes, World overview, Dashboard), auto-pause rules, high-contrast theme.
- **MapViewer GUI** — complete. Optional Swing window (Y/n prompt at startup); 31×31 color-coded map; click-to-inspect tile; refreshes each tick.

## Known Bugs (Active)
None as of 2026-09-19. (Items 1–4 — herb/metal off-by-one, Quest.targetArea stale, null-slot NPE, toString True Immortal — all resolved.)

## Design Notes
- **Arrays** protect area features (Shops, Dungeons/Tombs, Sects, Cities). Attackers must break the array AND defeat any defending cultivators to take the area. Arrays give defenders a temporary level boost during the fight. Multiple arrays can be installed in one area but they do not buff each other — only the array being broken at that moment matters.

## Architecture Notes
- `Map.characters` is an ArrayList where dead characters are nulled out by index (`characters.set(id, null)`). Always null-check before use: `Map.characters.get(someId)`.
- `Map.tempCharacters` accumulates new births; added to `characters` at end of tick.
- `Map.remCharacters` accumulates death removals; processed at end of tick.
- `Map.awakenedMonsters` accumulates Beasts that awakened to Monster NPCs this tick; removed from `monstersList` (but NOT nulled from `characters`) at end of tick. Direct `characters.add()` is used instead of tempCharacters to avoid inflating population count.
- `currentCharacter` (static on Map) is set per-character during the action loop. Some methods rely on it (notably `Area` constructor uses it for level seeding).
- `character.id == index into Map.characters` — this identity is load-bearing for quests, relationships, and death removal. Do not change.

- **Threading**: the game thread owns `World.lock` and releases it at every wait and at the end of each tick; GUI code must read state only inside `World.read/readWait`, never write game state from the EDT (queue a command with `MapViewer.queueInput`), and never block on the game thread while holding the lock. `GameDriver.frozenNpcs` is a test-only hook. Commands prefixed with `!` skip confirmations.
- Tests are probes in `tools/probes/` (see its README: headless and GUI regression, long run `tools/PlaytestProbe.java`, screenshots `tools/HelpShots.java`); run them from an empty scratch dir with a 3,000-person copy of the sources (a leftover save adds a start-up menu).

## Priorities for Next Development

All planned features from the design documents are now implemented. The simulation is functionally complete. Potential next improvements are polish/depth items (see "what could be better fleshed out" in session history).

## Coding Conventions (this codebase)
- Java, package `Game`. Static imports from `GameDriver`, `Map`, `Dictionary`.
- No build tool. Compile/run through IntelliJ.
- Quest success codes: -3 (null), -2 (spot fail), -1 (target fail), 0 (progressed), 1 (target success), 2 (spot success).
- Item level is major cultivation level (1–10 = Qi to Pseudo Immortal realms).
- All cross-character references use integer id (index into `Map.characters`).
- Print statements should be wrapped in `if (player > 0 && skipping <= 0)` — `player` is an `int` (0=NPC, 1=human, 2=follow), not boolean. Most prints are now guarded; check new code.

## Development Workflow

### Branch Strategy
- All development happens on `dev` branch. Push to `main` only when the version is stable and tested.
- Create `dev` branch from `main` if it does not exist: `git checkout -b dev`

### Before Any Code Change
- **Propose the change to the user first** — describe what you plan to change, what files/lines are affected, why, and what edge cases you considered. Wait for explicit approval before editing any `.java` file.

### After Any Code Change
1. **Compile**: Run `javac --release 8 -encoding UTF-8 -cp "src;lib/flatlaf-3.7.2.jar" -d /tmp/r8 src/Game/*.java` from the repo root using Bash. **The IntelliJ project uses JDK 1.8** (`.idea/misc.xml`): no `String.repeat`, `strip*`, `isBlank`, `List.of`, `var`, text blocks, `viewToModel2D` etc. Fix all errors before proceeding.
2. **Smoke test**: Run `(printf "n\nN\nw\n"; sleep 40) | timeout 40 java -cp "src;lib/flatlaf-3.7.2.jar" -Djava.awt.headless=true Game.GameDriver 2>&1` — 'n' = no map window, 'N' = new game (only asked when a save exists), 'w' = watch mode (no player); scan output for stack traces and check no `crash.log` appears; delete `saves/`, `settings.properties` and `crash.log` afterwards. A timeout exit with no crash output is a pass.
3. **Update `GameInfo/DEVLOG.md`** — append a dated entry (see format below).
4. **Update `GameInfo/Game Outline.md`** — flip ❌→🟡→✅ for any feature whose status changed.
5. **Update `GameInfo/Game Info.md`** — update any class reference, formula, or system description affected by the change.

### DEVLOG Entry Format
```
## YYYY-MM-DD
- Fixed/Added/Changed: <what changed> (`File.java:line`) — <one line why>
```

### Design Doc Reference
Before implementing any new feature, read the relevant section of:
- `GameInfo/CultivationGame2.txt` — original design vision
- `GameInfo/CultivationGame2Recent.txt` — revised notes, combat revamp, quest logic
- `GameInfo/Cultivation Game 2 Temp.txt` — working scratchpad, quest categories, priorities

### Design Doc Update Rule
**Any time a design decision is made during development** (in conversation, during debugging, or through implementation discovery), append it to the most relevant design doc before the session ends. New mechanics → `CultivationGame2.txt`. Quest/combat revisions → `CultivationGame2Recent.txt`. Working notes / priorities → `Cultivation Game 2 Temp.txt`. Keep entries terse (1–2 lines each). This prevents design drift from context window loss.
