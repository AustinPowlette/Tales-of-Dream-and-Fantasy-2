# CLAUDE.md — Tales of Dream and Fantasy 2

## Project Summary
Java xianxia cultivation simulation. 100K+ NPC world, turn-based (weekly ticks), procedurally generated. Entry point: `src/Game/GameDriver.java`. All game logic is in `src/Game/`. No build tool (IntelliJ project, `.iml` file present).

## Key Documentation
- [Game Info.md](Game Info.md) — Full technical deep-dive: systems, combat formulas, bugs, class reference
- [Game Outline.md](Game Outline.md) — ✅/🟡/❌ checklist of every feature (implemented / partial / not started)
- Design docs: `src/Game/CultivationGame2.txt`, `src/Game/CultivationGame2Recent.txt`, `src/Game/Cultivation Game 2 Temp.txt`

## Current State (as of 2026-09-19)
All three previously-blocked systems are now implemented. The simulation runs a full autonomous loop with quests, economy, and sect AI. Remaining work is polish and minor bugs.

- **Quest system** — complete. All quest types from design docs implemented: Forage/Mine prereq, Hunt Beast, Buy Gear, Go Sell, Battle Rival/Revenge/Bounty, Join Sect, Seek Potential, Return to Sect, Deliver Materials, sect missions, tournaments, arena registration, Ghost City Rebirth, Get Pregnant, Find Parent's Body, Sell Technique, and more.
- **Economy/Shops** — complete. `Shop.java` exists. `cityOwner` set by `cityLeadership()`. Tax collected yearly (`population/100` to cityOwner). Merchant arbitrage (buy cheap → carry → sell dear). NPC pill buying. Library buy/sell. Item pricing formula: `lvl^2.5 × 10`.
- **Sect/Organization AI** — complete. Sect missions (Forage/Mine + Deliver Materials two-phase). Promotions (Outer→Inner→Elder). Tournaments. Politics (war/alliance/tribute/defection/succession). Return to Sect drift. Sect cultivation bonus.

## Known Bugs (Active)
None as of 2026-09-19. (Items 1–4 — herb/metal off-by-one, Quest.targetArea stale, null-slot NPE, toString True Immortal — all resolved.)

## Design Notes
- **Arrays** protect area features (Shops, Dungeons/Tombs, Sects, Cities). Attackers must break the array AND defeat any defending cultivators to take the area. Arrays give defenders a temporary level boost during the fight. Multiple arrays can be installed in one area but they do not buff each other — only the array being broken at that moment matters.

## Architecture Notes
- `Map.characters` is an ArrayList where dead characters are nulled out by index (`characters.set(id, null)`). Always null-check before use: `Map.characters.get(someId)`.
- `Map.tempCharacters` accumulates new births; added to `characters` at end of tick.
- `Map.remCharacters` accumulates death removals; processed at end of tick.
- `currentCharacter` (static on Map) is set per-character during the action loop. Some methods rely on it (notably `Area` constructor uses it for level seeding).
- `character.id == index into Map.characters` — this identity is load-bearing for quests, relationships, and death removal. Do not change.

## Priorities for Next Development

### Unimplemented features
- **Monster NPC awakening** — `Traits.Monster` is the planned upgrade for a Beast that kills a cultivator: it gains a proper name, trait flips from Beast → Monster, and begins behaving like an NPC (movement, quests, relationships). Not yet implemented; the `Monster` enum entry is reserved for this path.

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
