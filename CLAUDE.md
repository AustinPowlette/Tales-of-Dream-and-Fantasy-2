# CLAUDE.md — Tales of Dream and Fantasy 2

## Project Summary
Java xianxia cultivation simulation. 100K+ NPC world, turn-based (weekly ticks), procedurally generated. Entry point: `src/Game/GameDriver.java`. All game logic is in `src/Game/`. No build tool (IntelliJ project, `.iml` file present).

## Key Documentation
- [Game Info.md](Game Info.md) — Full technical deep-dive: systems, combat formulas, bugs, class reference
- [Game Outline.md](Game Outline.md) — ✅/🟡/❌ checklist of every feature (implemented / partial / not started)
- Design docs: `src/Game/CultivationGame2.txt`, `src/Game/CultivationGame2Recent.txt`, `src/Game/Cultivation Game 2 Temp.txt`

## Current State (as of 2026-05-28)
Solid foundation implemented: world generation, cultivation, combat, all crafting professions, relationship/marriage/birth, quest pathfinding infrastructure. Three major systems are blocked or unfinished:

1. **Quest system** — infrastructure exists (`Quest.java`, `quest()`, `pathfinder()`, `spotQuests()`, `targetQuests()`) but only "Find Person" and 4 generic spot quests work. All quest types from the design docs (Bounty, Revenge, Buy Gear, Sell, Forage/Mine prereq, Sect, Battle, etc.) are not implemented.
2. **Economy/Shops** — no Shop class. City area inventory bags exist but are empty and unused. Merchant trait is a stub. `cityOwner` is never set so no tax is collected.
3. **Sect/Organization AI** — Organization class exists and members are tracked, but sect members have no sect-specific behavior (no recruitment quests, no sect politics, no sect events).

## Known Bugs (Critical)
1. **`changeRelationship()` NPE** — Lines 1366/1369 access `this.currentQuest.targetCharacter` without null-checking `currentQuest`. Crashes when any character has no active quest.
2. **`spotQuests()` always returns 0** — Line 944: `return 0` is hardcoded; the `questSuccess` variable computed inside the switch is never returned.
3. **`event()` case 1 fall-through** — No `break` after beast battle case; always falls into interact case 2.
4. **Equipment duplication** — `equip()` can duplicate items when multiple upgrades exist in backpack simultaneously. Also: `transferAllItems()` runs after `dead()` moves equipped items into backpack — if `equip()` is called between these two (e.g., inside `tempLvlCheck()` during combat), items can be double-counted.
5. **`forbiddenCheck()` zombie characters** — Sets `dead = true` without calling `dead()` or `dead(killer)`, so characters are never removed from `Map.characters`.
6. **`professionPicker()` fall-through** — Missing `break` after Friendly case (falls into Tailor) and after Tailor (falls into Scribe).
7. **Relationship string comparison** — `q.name == "Find Person"` uses reference equality instead of `.equals()`, so the remove condition never fires.
8. **Quest target area is stale** — Character-category quests store `targetArea` at creation time; it is never updated when the target character moves.

## Design Notes
- **Arrays** protect area features (Shops, Dungeons/Tombs, Sects, Cities). Attackers must break the array AND defeat any defending cultivators to take the area. Arrays give defenders a temporary level boost during the fight. Multiple arrays can be installed in one area but they do not buff each other — only the array being broken at that moment matters.

## Architecture Notes
- `Map.characters` is an ArrayList where dead characters are nulled out by index (`characters.set(id, null)`). Always null-check before use: `Map.characters.get(someId)`.
- `Map.tempCharacters` accumulates new births; added to `characters` at end of tick.
- `Map.remCharacters` accumulates death removals; processed at end of tick.
- `currentCharacter` (static on Map) is set per-character during the action loop. Some methods rely on it (notably `Area` constructor uses it for level seeding).
- `character.id == index into Map.characters` — this identity is load-bearing for quests, relationships, and death removal. Do not change.

## Priorities for Next Development
In rough dependency order:

### Fix existing bugs first
- Fix `changeRelationship()` NPE (null-check `currentQuest`)
- Fix `spotQuests()` return value
- Fix `event()` case 1 fall-through
- Fix `professionPicker()` fall-through
- Fix `forbiddenCheck()` zombie (call `dead(killer)` properly)
- Fix quest string comparison (`==` → `.equals()`)

### Quest system completion
Add the following quest types to `spotQuests()` and `targetQuests()`:
- Forage/Mine (spot) — triggered when profession fails due to missing materials
- Hunt Beast (area) — find an area with monsters at appropriate level
- Buy Gear (area) — travel to city market when slots empty or gear outgrown
- Go Sell (area) — travel to city when backpack over threshold
- Battle Rival / Dislike / Arch-Enemy (character target)
- Revenge (character target) — triggered when close friend (value ≥ 2) dies
- Bounty Hunt (character/area) — if too weak for revenge, post bounty; others pick it up
- Join Sect (area) — Leader characters recruit

### Economy / Shop system
- Create `Shop` class (or use city's existing area bags as the market pool)
- Set `area.cityOwner` when `cityLeadership()` succeeds
- Implement Merchant decision engine: buy at 1/100 price, sell at normal price
- Wire Buy Gear and Sell quests to city locations
- Implement `vendorID` tracking on items

### Sect & Organization AI
- Give Leader-trait characters sect recruitment quests
- Implement sect events (tournaments, political events)
- Sect members should prefer staying near sect location

## Coding Conventions (this codebase)
- Java, package `Game`. Static imports from `GameDriver`, `Map`, `Dictionary`.
- No build tool. Compile/run through IntelliJ.
- Quest success codes: -3 (null), -2 (spot fail), -1 (target fail), 0 (progressed), 1 (target success), 2 (spot success).
- Item level is major cultivation level (1–10 = Qi to Pseudo Immortal realms).
- All cross-character references use integer id (index into `Map.characters`).
- Print statements should be wrapped in `if (player && skipping <= 0 && age >= 8)` — many are not yet.

## Development Workflow

### Branch Strategy
- All development happens on `dev` branch. Push to `main` only when the version is stable and tested.
- Create `dev` branch from `main` if it does not exist: `git checkout -b dev`

### Before Any Code Change
- **Propose the change to the user first** — describe what you plan to change, what files/lines are affected, why, and what edge cases you considered. Wait for explicit approval before editing any `.java` file.

### After Any Code Change
1. **Compile**: Run `javac -cp src src/Game/*.java` from the repo root using Bash. Fix all errors before proceeding.
2. **Smoke test**: Run `echo "n" | timeout 15 java -cp src Game.GameDriver 2>&1` — answer 'n' to skip player creation, let it run 15 seconds, scan output for stack traces. A timeout exit (no crash output) is a pass.
3. **Update `DEVLOG.md`** — append a dated entry (see format below).
4. **Update `Game Outline.md`** — flip ❌→🟡→✅ for any feature whose status changed.
5. **Update `Game Info.md`** — update any class reference, formula, or system description affected by the change.

### DEVLOG Entry Format
```
## YYYY-MM-DD
- Fixed/Added/Changed: <what changed> (`File.java:line`) — <one line why>
```

### Design Doc Reference
Before implementing any new feature, read the relevant section of:
- `src/Game/CultivationGame2.txt` — original design vision
- `src/Game/CultivationGame2Recent.txt` — revised notes, combat revamp, quest logic
- `src/Game/Cultivation Game 2 Temp.txt` — working scratchpad, quest categories, priorities

### Design Doc Update Rule
**Any time a design decision is made during development** (in conversation, during debugging, or through implementation discovery), append it to the most relevant design doc before the session ends. New mechanics → `CultivationGame2.txt`. Quest/combat revisions → `CultivationGame2Recent.txt`. Working notes / priorities → `Cultivation Game 2 Temp.txt`. Keep entries terse (1–2 lines each). This prevents design drift from context window loss.
