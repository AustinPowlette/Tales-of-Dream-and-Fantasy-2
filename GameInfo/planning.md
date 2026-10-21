# Player Control System — Planning

Status: **implemented (2026-09-29)** — all six phases in section 13 are in the code. See DEVLOG.md for the file list and `Game Info.md` ("Player Control") for the reference. Deviations from the design as written are listed in section 16.
Purpose: capture the full design discussion so it survives a context compaction. Update this file as decisions change.

## 1. Goal

Human-controlled player characters (PCs, `player == 1`) currently run on the same framework as NPCs. Players should get three ways to play, selectable per character at any time:

| Mode | Meaning |
|---|---|
| **Manual** (granular) | The character does only what the player commands: wasd movement, kill/spare choices, professions, running sects/cities/shops, ghost abilities, parties, bounties, etc. |
| **Auto** (NPC framework) | The player switches the character onto the NPC pipeline; it acts like an NPC. |
| **Hybrid** | The player writes quests / a series of quests / scripts (go to a coordinate, do a profession N times, ...) and the character carries them out automatically, then hands control back. |

Everything below is in service of covering every base before implementation starts.

## 2. What exists today (facts from the code)

- `Character.player`: `0` NPC, `1` human, `2` "follow" (random-walk, prints like a player). Up to 4 humans (multiplayer).
- Player input phase = "Phase 2A" in `GameDriver.main` (serial, blocking): a `switch` on `readInput()` handles `w a s d`, `i` (stats), `l` (locations), `r` (relationships), `q` (quests, read-only), `e` (edit self), `z` (cheats), `f` (save request), `p` (pause), `o` (auto-move: wait N turns / walk a direction N turns / walk to a coordinate N turns / random N turns). `character.skipping` counts remaining auto turns.
- After the command, **`character.quest()` still runs every tick for PCs** (the NPC quest engine). Consequences:
  - `pathfinder()` returns early for a PC in manual control (`player >= 1 && skipping <= 0`), so target quests only fire when the player happens to stand on the target.
  - Spot quests still run on their own: `Profession` auto-crafts, `Random` triggers events (which can start fights), `Hunt` fights beasts, `Meditate` cultivates. So today's PCs are *neither* fully manual nor fully automatic.
- Combat decisions are automatic for PCs: `battle()` -> `fleeCheck()`, `forbiddenCheck()`, and `winCheck()` always kills (unless sparring). There is no spare/kill/flee prompt.
- Input plumbing: `readInput()` / `readInt()` block on either `scan` (console) or `guiInputQueue` (MapViewer buttons call `queueInput(...)`). The game thread is single-threaded for NPC/PC phases, so it can already block mid-tick.
- Death: `handleReincarnation()` runs after the tick for PCs with `needsReincarnation`. Ghost trait: `Possess Womb` / `Ghost City Rebirth` quests already transfer `player` to the new body.
- Quest priorities: `Character.bump()` ages every quest by `priorityDiff` (clamped to `maxPriority`) and rebuilds the heap. Repeating target quests are re-queued at priority 0 and the character is released so the queue picks the next trip. `maintainQuests()` (yearly) clamps priorities, expires one-time quests after 10 years, caps the target queue at 50, and refills weak social quests.

## 3. Decisions made so far (from the user)

1. **Quest priority is player-controlled.** Players can set the priority of their quests, and player-set priorities **do not age/increase**. Players can also **delete quests** from the queue.
2. **Manual mode partially suppresses NPC quests.** Mundane ones are ignored. If something significant / one-time appears (e.g. Revenge), the player is **notified with the target and coordinates** so they can head there themselves. At the location, a **"Do Quest" command** checks the quest can be done there and then runs the quest logic.
3. **Decisions arrive sequentially at the terminal** through a lock so the player answers them in order. If that proves too complicated, fall back to **policies**. (Assessment in section 7: feasible; policies remain the fallback for Auto/Hybrid/unattended play.)
4. **Managed duties get an auto toggle** (on/off), and the player can also manage duties manually whenever they want.
5a. **"Spare" just means "don't kill".** No extra relationship/reputation/loot rules for now: on a win the player chooses kill or spare; spare = the loser lives and nothing else changes.
5b. **Players set their shops' prices** (fixed rates, replacing the constant `buyRate`/`sellRate`/`cityBuyRate` for a player-owned `Shop`). Per-shop settings: rate paid to sellers, markup charged to buyers, and (optionally) the rate used when restocking from the city pool.
5c. **Mundane quests = spot quests and Find Person** (little danger, little consequence) as the starting rule. The user will decide the exact classification quest-by-quest from the list in section 15.
5. **Multiplayer stays sequential** (one player's prompt at a time, in turn order). There is no realistic alternative for a blocking single-threaded turn loop, other than real-time/async, which is out of scope.

## 4. Control modes in detail

One `controlMode` per PC (enum: `MANUAL`, `AUTO`, `HYBRID`). All three feed the **same action functions** so behaviour is identical no matter who triggers it.

- **MANUAL:** no autonomous actions. Movement only from commands. Spot quests (Random, Profession, Hunt, Meditate, Forage, Mine) are not run. Significant quests are *announced* (section 5) but never auto-executed.
- **AUTO:** the existing NPC pipeline (`movement()`/`pathfinder()` + `quest()`), plus the player's **decision policy** (section 6) for anything that would otherwise prompt. Replaces today's `player == 2` follow mode.
- **HYBRID:** a **script** (section 8) drives the character. When the script ends, is stopped, or is interrupted, the character drops to MANUAL (default) or back to AUTO (setting).
- Mode switch commands available any time: e.g. `auto`, `manual`, `stop`.
- Per-mode toggles that persist: `autoDuties` flags (section 9), decision policy (section 6), muted quest names (section 5).

## 5. Quest management for players

### 5.1 Queue editing
- **Set priority:** player chooses a number for any quest; sets `priority = N`, `priorityDiff = 0`, `maxPriority = N` (a **pinned** quest never ages). `Quest` gets a `pinned` (or `userSet`) flag so `bump()`, `maintainQuests()` clamping, and TTL expiry all skip it.
- **Delete:** remove from `targetQuests` / `spotQuests` (and clear `currentQuest` if it is that quest). Because many NPC quests are *re-created by triggers*, deleting alone would not stick: add a per-character `mutedQuests` set (by quest name and optionally by target) that the trigger code checks before re-adding. Mute/unmute commands.
- **Create custom quest:** player-authored quest (name, target area/coordinate or character, one-time or repeating, priority). Stored as a normal `Quest` flagged `userCreated` + pinned.
- **View:** `q` lists both queues with **stable numbering** so `qp <n> <priority>`, `qd <n>`, `qdo <n>` can refer to a row. Show priority, pinned marker, target name and coordinates, distance.

### 5.2 Manual-mode notifications ("quest alerts")
- Classify quests into **mundane** (silently ignored in MANUAL) and **notable** (alerted):
  - Mundane: Random, Profession, Hunt, Meditate, Forage, Mine, weak-tie Find Person / Battle Dislike, routine Buy Gear / Go Sell / Restock, Return to Sect drift.
  - Notable (one-time / story): Revenge, Bounty Hunt, Battle Rival / Arch-Enemy, Sect Raid, Heaven Treasure, Explore Dungeon, Ghost City Rebirth, Possess Womb, Find Parent's Body, Join Sect, Recruit Member / Defector, Deliver Materials, Accept Sect Mission, Hold / Register / Spectate Tournament, Rob Caravan, beast bounties, Disperse, Get Pregnant, Seek Potential, Propose Alliance / Demand Tribute.
  - Classification stored as data (a `Set<String>` or a field on `Quest`) so it is easy to tune.
- **Alert content:** quest name, target (character name / area name), **coordinates** (same `x*100, y*100` convention the game already prints), distance from the player, priority, and why it appeared.
- Alerts go into a **quest inbox** (viewable any time, cleared when done/deleted) and print immediately. Unanswered alerts do not block the turn.
- The quest is *held in the queue but not auto-executed* in MANUAL; the player walks there themselves.

### 5.3 "Do Quest" command
- `qdo <n>` (GUI button "Do Quest"):
  1. Look up the quest in the queue.
  2. Check `questViable(quest) == 1`.
  3. Check the player is where the quest requires (in the target area; for character-targeted quests, in the same tile as the target; spot quests have no location).
  4. If any check fails, print *why* (wrong place, target dead, cannot afford, conditions no longer met).
  5. If OK, run the same `targetQuests(quest)` / `spotQuests(quest)` logic once and apply the same result handling as `quest()` (drop one-time, re-queue and release repeating, refill weak social, etc.).
- Refactor needed: pull the post-execution bookkeeping out of `Character.quest()` into a reusable method so the NPC path and the Do Quest path share it.

## 6. Decision points (prompt or policy)

Places where NPC code currently decides silently and a PC should be asked:

| Decision | Where in code | Notes |
|---|---|---|
| Kill or spare after winning | `winCheck()` (always kills unless sparring) | Most important. Options: kill, spare (loser survives, maybe relationship/reputation effect), loot only |
| Flee | `fleeCheck()` inside `battle()` | Prompt when the PC is the one fleeing/being pursued |
| Forbidden technique | `forbiddenCheck()` in `battle()` | Player opts in |
| Accept/decline a fight | `battleCheck()` when someone challenges the PC | Includes bullying, bounty hunters, rivals |
| Party invite / sect invite | `tryRallyParty`, sect recruit quests | |
| Tribute demand, alliance proposal | Demand Tribute / Propose Alliance handlers | |
| Treasure / dungeon / tournament entry | Heaven Treasure, Explore Dungeon, Register Tournament | |
| Reincarnation / possession | `handleReincarnation`, Possess Womb, Ghost City Rebirth | Already partly interactive |
| Duty decisions | city/sect/shop actions (section 9) | Only when duties are manual |

**Policies** (used in AUTO, HYBRID, and as the unattended fallback): small per-character settings such as `killPolicy` (always spare / kill evil only / kill if wronged / always kill), `fleePolicy`, `forbiddenPolicy`, `partyInvitePolicy`, `duelPolicy`. Defaults should be conservative (spare, flee when outmatched).

## 7. Sequential decision lock (answers question 2)

**Feasible.** The simulation phases are serial on one game thread, and `readInput()` already blocks that thread, so a prompt can be issued from anywhere in tick processing (including deep inside an NPC-initiated fight against a PC).

Design:
- A `DecisionBroker` (static, single lock) exposes `ask(pc, DecisionRequest) -> answer`. Only one request is on the terminal/GUI at a time; requests are answered in the order they are raised. Because everything is on the game thread this is *naturally* sequential; the lock guards the GUI/input side and any future async callers.
- Each request carries: who it is for, a prompt, numbered options, a default, and context (opponent, coordinates, stakes).
- If the PC is not in MANUAL (or no human is attached: headless/spectate/console closed/GUI closed), the broker answers immediately from the **policy** instead of prompting.
- Optional per-decision timeout with default answer, so a stuck GUI never freezes the world.

Known complications to handle:
- **Re-entrancy:** a prompt inside a battle inside an NPC's quest inside Phase 2B; state must be consistent when the answer returns (the opponent may have died meanwhile; re-check after returning).
- **Pause / autosave:** `saveRequested` is honoured only at the top of a tick, so prompts do not interfere; `paused` must not deadlock a pending prompt.
- **Parallel phase:** `cultivate()` runs in a parallel stream; no prompts may originate there (they don't).
- **GUI:** answers arrive through `guiInputQueue`; the GUI needs a visible "decision pending" panel so the player knows the game is waiting.
- **Multiplayer:** prompts are shown to the owning player, in order; other humans wait. Keep it simple.

Fallback (if the broker proves too invasive): ship policies first, add prompts later.

## 8. Hybrid mode: scripts and player-made quests

A **script** is an ordered list of **steps**, run one step at a time by the tick loop while the character is in HYBRID.

Step types (initial set):
- `GoTo(coordinate | area | character | named place)` — uses `pathfinder`; completes on arrival.
- `Wait(n turns)`.
- `Profession(kind, count | until condition)` — e.g. forage/mine/craft N times, until materials >= X, until level Y.
- `Hunt(area, kills | until major level | until turns)` — beast hunting with a party option.
- `Sell(city)` / `Buy(item filter, city)`.
- `DoQuest(quest)` — go to a queue quest's target and execute it.
- `Follow(character | party)`.
- `Repeat(steps, n | forever | until condition)` and `If(condition, steps)`.
- `SetMode(MANUAL | AUTO)`.

Conditions: reached location, count done, item/material threshold, ss threshold, level/major-level reached, age, time.

**Interrupts (stop or pause the script and notify):**
- attacked / hostile arrives in the tile (then broker decision or policy)
- large danger gap (no HP in this game, so level gap of the opponent)
- level-up or breakthrough
- materials/money exhausted for the current step
- quest completed / target dead
- death (script cleared)
- notable quest alert (optionally)

Script authoring: console text commands first (e.g. `script goto 15 15`, `script forage 500`, `script loop ... end`), GUI script editor later. Custom quests (section 5.1) are the simplest hybrid tool: pin a priority, turn on pathfinding for the PC (AUTO/HYBRID), and the engine walks there and executes.

Scripts must persist in saves and be cleared on death/reincarnation.

## 9. Managed duties (answers question 3)

**Duties** = the recurring management work a role brings. Each category has an auto toggle; when OFF the player does it manually (or gets a reminder), when ON the existing NPC triggers run for the PC.

| Category | Auto examples (existing NPC behaviour) | Manual verbs needed |
|---|---|---|
| City lord | Buy City Array, Entice Merchant, Hold Tournament, claim/found city | found/claim city, buy array, recruit merchant, host tournament, view treasury/level/shops |
| Sect master / elder | Buy Sect Array, Hold Tournament, Sect Raid (cooldown), Propose Alliance, Demand Tribute, Recruit Member/Defector, missions, promotions | found sect, recruit, promote, set recruitment criteria, declare war/peace/alliance, demand tribute, post/accept missions, donate |
| Shop owner / merchant | Restock Trip, Sell Stock, shop upkeep | open shop, stock, set prices (new), sell/buy, arbitrage trips |
| Crafter | Profession loop (gather + craft), Scribe array donation | craft with choices, choose materials/level, install arrays, donate |
| Personal upkeep | Buy Gear, Go Sell, Seek Potential, meditate | equip, buy, sell, consume pills |

Toggles: `autoDuties = {city, sect, shop, craft, upkeep}` on the character. A PC can flip any of them at any time, independently of the control mode (e.g. MANUAL movement but sect duties on auto).

## 10. Verb catalogue for MANUAL mode (coverage checklist)

- **Movement:** wasd; travel to coordinate / area / character; follow party.
- **Combat & social:** attack, spar, flee, spare/kill, challenge, interact/befriend, gift item/technique, marry, have children, take bounty, post bounty.
- **Cultivation:** meditate (extra tick), consume pills, forbidden technique toggle, choose to attempt breakthrough tribulation where applicable.
- **Professions:** forage, mine, alchemy, smithing, tailoring, array crafting, technique creation, weapon enchanting (Innovative).
- **Inventory:** equip, unequip, use, drop, trade, view.
- **Economy:** sell to pool/shop, buy from shop, auction bid, run shop (stock, price), library buy/sell technique.
- **Cities:** found, claim, arrays, tournaments, library, taxes overview.
- **Sects:** found, join, resign, recruit, missions, donate, promote, war/alliance/tribute.
- **Parties:** invite, accept/decline, kick, disband, set leader; party hunt.
- **Ghost trait:** possess womb, rebirth in a city.
- **Quests:** view, set priority, delete, mute, create, Do Quest.
- **Info:** stats, relationships, known locations/map, event log/alerts.

## 11. Implementation touchpoints (where the code changes)

- `Character`: `controlMode`, `autoDuties`, `killPolicy` etc., `mutedQuests`, `script`, `questAlerts`; guard `quest()` so MANUAL skips mundane spot/target quests and only alerts on notable ones; enable `pathfinder` for AUTO/HYBRID PCs; extract quest-result bookkeeping for reuse by Do Quest.
- `Quest`: `pinned`, `userCreated`, `notable` (or classification table).
- `Character.bump()` / `maintainQuests()`: skip pinned quests.
- `winCheck()`, `fleeCheck()` / `battle()`, `forbiddenCheck()`, `battleCheck()`: call the `DecisionBroker` when the acting/affected character is a MANUAL PC.
- `GameDriver` Phase 2A: replace the flat `switch` with a command dispatcher supporting the full verb set, mode switches, script commands, and queue commands; keep `w a s d i l r q e z f p o` working.
- `MapViewer`: buttons for mode toggle, Do Quest, quest editor, decision panel, script editor.
- `SaveData`: persist new fields (scripts, policies, mutedQuests, alerts).
- New classes: `DecisionBroker`, `DecisionRequest`, `PlayerScript` + `Step` types, `DecisionPolicy`.
- `handleReincarnation`, Possess Womb, Ghost City Rebirth: clear scripts, keep mode/policy settings.

## 12. Testing plan

- Headless harness (the scratch-probe approach in the `headless-sim-probe` memory): inject a PC, set a mode/script, run N ticks, assert results (arrived, count done, no unprompted actions in MANUAL, alerts raised, priorities pinned, deleted quests stay deleted).
- Decision tests: simulate an attack on a MANUAL PC and check the broker orders prompts correctly; check policy fallback when no input is attached.
- Regression: make sure NPCs are unaffected (control mode only applies to `player >= 1`).
- Save/load round-trip with scripts and pinned quests.

## 13. Suggested phasing

1. **Foundation:** `controlMode` enum; `player==2` folded into AUTO; MANUAL suppresses mundane quests; pathfinding for AUTO.
2. **Quest tools:** numbered queue view, set priority (pinned), delete, mute, custom quests, quest alerts inbox, Do Quest.
3. **Decisions:** `DecisionBroker`, policies, kill/spare and flee/forbidden/duel prompts.
4. **Verbs:** professions, economy, city/sect/shop management, parties, ghost, plus duty toggles.
5. **Hybrid scripts:** step types, interrupts, persistence.
6. **GUI:** mode toggle, decision panel, quest editor, script editor.

## 14. Open questions / risks

- Exact mundane vs notable classification (section 15) — **waiting on the user's per-quest decisions**.
- ~~What "spare" does~~ — decided: spare = don't kill, nothing else.
- ~~Shop pricing~~ — decided: player-set fixed rates on player-owned shops (NPC shops keep the defaults).
- Multiplayer prompt ordering with the GUI open (kept sequential; revisit only if it becomes annoying).
- Blocking prompts during NPC-initiated events with several humans present: default to policy for non-active players if this feels bad in practice.
- Age < 8 PCs (children) — likely stay auto until 8.

## 15. Quest classification worksheet (user to decide)

Rule of thumb from the user: **mundane = little danger, little consequence** (starting assumption: spot quests and Find Person). Each row below has a suggested default; the user overrides per quest. **Final decisions are in section 15b (they supersede the suggestions here).**
Legend: **M** = mundane (silently ignored in MANUAL) · **A** = alert (notify with target + coordinates; player goes there and uses Do Quest) · **D** = duty (controlled by the auto-duties toggle; alert or reminder when the toggle is off).

### Spot quests (no location; run wherever the character is)

| Quest | What it does | Suggested |
|---|---|---|
| Random | Random event (can start a fight, interaction, loot, profession) | M |
| Profession | Runs the trait's craft/gather routine | M (manual verbs replace it) |
| Hunt | Fights a beast in the current tile if level-appropriate | M |
| Meditate | Extra cultivation tick | M |
| Forage / Mine | Gather herbs / metal at the current tile | M |
| Loot Corpse | Lucky trait: loot a fresh corpse in the tile | M |
| Sell Technique (spot) | Innovative: sell a technique to a city library | M |
| Accept Sect Mission | Take a mission from the sect board | D (sect) |
| Donate to Sect | Donate surplus items/materials/pills for sect points | D (sect) |
| Buy from Sect | Spend sect points on materials/gear | D (sect) |
| Buy City Array / Buy Sect Array | Master buys a defensive array | D (city/sect) |
| Entice Merchant | City lord pays a merchant to open a shop | D (city) |
| Hold Tournament | Lord/master hosts a tournament | D (city/sect) |
| Register Tournament | Sign up for a local tournament | A or M? (fights, prizes) |
| Spectate Tournament | Watch a tournament | M |

### Target quests (have a place or a person)

| Quest | What it does | Danger / consequence | Suggested |
|---|---|---|---|
| Find Person (Like/Friend/Close Friend) | Go and interact with someone you have a tie with | low | M |
| Battle Dislike | Go and fight someone you mildly dislike (bully check) | some (fights) | A? (fights can kill) |
| Battle Rival / Battle Arch-Enemy | Fight a rival / arch-enemy | high | A |
| Revenge | Avenge a killed friend | high | A |
| Bounty Hunt | Hunt a bounty target | high | A |
| Hunt Beast (incl. city beast bounties) | Hunt beasts in an area (party option) | medium | M or A (bounty ones = A?) |
| Explore Dungeon | Explore a dungeon (fights, treasure, depletion) | medium-high | A |
| Heaven Treasure | Race to claim a heavenly technique | medium (competition) | A |
| Rob Caravan | Evil: rob a wealthy merchant | high | A |
| Buy Gear | Travel to a shop city with an upgrade you can afford | low | M |
| Go Sell | Travel to a city to sell surplus | low | M |
| Restock Trip / Sell Stock | Merchant trade trips | low | D (shop) |
| Seek Potential | Buy a Potential pill in a city | low, useful | A or M? |
| Sell Technique (target) | Travel to a library city to sell a technique | low | M |
| Join Sect | Travel to a sect and join | medium consequence | A |
| Return to Sect | Drift back to the sect home | low | M |
| Recruit Member | Sect: recruit someone | low | D (sect) |
| Sect Raid | Master/elder raids an enemy sect | very high | A / D (sect) |
| Propose Alliance / Demand Tribute | Sect diplomacy | medium (can trigger war) | D (sect) |
| Recruit Defector | Poach an enemy elder | medium | D (sect) |
| Deliver Materials / Donate Array | Deliver goods/arrays to the sect | low | D (sect) |
| Gift Technique | Parent teaches a child | low | M or A? |
| Find Parent's Body | Visit a dead parent's resting place | low | M or A? |
| Get Pregnant | Spouse conception attempt | consequence: a child | A? |
| Ghost City Rebirth / Possess Womb | Ghost rebirth mechanics | very high (new body) | A |
| Disperse | Leave an overcrowded hotspot | medium (beast ambushes) | A |
| Seek Territory / Hunt Cultivator | Monster-NPC only | n/a for PCs | M (never occur) |

Notes for deciding:
- A "duty" (D) quest is only alerted when the matching auto-duty toggle is OFF; when ON it runs like an NPC would.
- Anything left **M** is skipped entirely in MANUAL and can be muted/created/deleted from the quest tools if the player wants to override.
- In AUTO and HYBRID modes classification is irrelevant: everything runs (subject to decision policies and script interrupts).

## 15b. Classification — decided by the user (2026-09-29)

Overrides of the suggested defaults in section 15 (anything not listed keeps its suggested default):

| Quest | Decision | Notes |
|---|---|---|
| Loot Corpse | **D** | Lucky-trait duty |
| Spectate Tournament | **A** | Alert, player goes there |
| Register Tournament | **D** | Tournament participation is a duty |
| Battle Dislike | **D** | Handled by a duty toggle, not silently ignored |
| Hunt Beast | **D for bounties** (city beast bounties); ordinary Hunt Beast stays **M** | |
| Seek Potential | **D** | Upkeep duty |
| Gift Technique | **A** | |
| Find Parent's Body | **A** | |
| Get Pregnant | **D** | **Display name in the terminal/GUI must be "Have Child"** (appropriateness). Keep the internal quest name stable or add a display-name mapping used by `Quest.toString()`, alerts, and the quest list |

Resulting final table (defaults + overrides):
- **M (ignored in MANUAL):** Random, Profession, Hunt, Meditate, Forage, Mine, Sell Technique (spot + target), Find Person (all tiers), plain Hunt Beast, Buy Gear, Go Sell, Return to Sect, Seek Territory / Hunt Cultivator (monster-only).
- **A (alert with target + coordinates, then Do Quest):** Spectate Tournament, Battle Rival, Battle Arch-Enemy, Revenge, Bounty Hunt, Explore Dungeon, Heaven Treasure, Rob Caravan, Join Sect, Gift Technique, Find Parent's Body, Ghost City Rebirth, Possess Womb, Disperse, Sect Raid (also a sect duty).
- **D (controlled by an auto-duty toggle; alert/reminder when OFF):** Loot Corpse, Register Tournament, Battle Dislike, beast bounties (Hunt Beast from a city board), Seek Potential, Have Child (Get Pregnant), Accept Sect Mission, Donate to Sect, Buy from Sect, Buy City Array, Buy Sect Array, Entice Merchant, Hold Tournament, Restock Trip, Sell Stock, Recruit Member, Recruit Defector, Propose Alliance, Demand Tribute, Deliver Materials, Donate Array, Sect Raid.

### Duty toggle categories (expanded to cover the D list)

Section 9 listed `city, sect, shop, craft, upkeep`. The D decisions need a few more groupings (proposed; confirm or merge):

| Toggle | Quests it controls |
|---|---|
| `city` | Buy City Array, Entice Merchant, Hold Tournament (as city lord) |
| `sect` | Accept Sect Mission, Donate to Sect, Buy from Sect, Buy Sect Array, Hold Tournament (as sect master), Recruit Member/Defector, Propose Alliance, Demand Tribute, Deliver Materials, Donate Array, Sect Raid |
| `shop` | Restock Trip, Sell Stock, shop upkeep |
| `craft` | Profession routine, Scribe array donation |
| `upkeep` | Seek Potential, (Buy Gear / Go Sell run as mundane) |
| `bounty` | City beast bounties (Hunt Beast from a board), Bounty Hunt if the player wants it automated |
| `tournament` | Register Tournament |
| `social` | Battle Dislike |
| `family` | Have Child (Get Pregnant) |
| `loot` | Loot Corpse |

**Decided (user):** keep all **ten** separate toggles for customization, plus a **master toggle** that sets all ten on or all ten off in one action.

Master toggle semantics:
- `duties all on` / `duties all off` set every individual toggle at once (GUI: one button).
- Individual toggles can still be flipped afterwards; the master then shows a **mixed** state (e.g. `duties: 6/10 on`) rather than on/off.
- Stored as the ten booleans only (the master is derived/UI-level), so saves stay simple.
- Commands: `duties` (show), `duties all on|off`, `duties <name> on|off` (e.g. `duties sect off`).
- Quick presets are an easy later addition (e.g. "leader" = city+sect+shop on), but are not planned now.
- Design property: any target set of k duties can be reached with one master press plus min(k, 10-k) individual flips, i.e. at most 5 individual flips (flip all on and untoggle the rest, or all off and toggle the wanted ones).

## 16. Implementation notes (deviations and decisions made while building it)

- **Info commands are free.** Old behaviour: every keypress (even `i`) ended the turn. Now `i l r q e z f here people ...` re-prompt without time passing; only actions/movement/`p` use the turn.
- **Alerts interrupt autopilot.** A new alert or an attack cancels a `wait`/`walk`/`goto` so the player is not carried past something important; at most 3 new alerts print per turn (`...and N more`), all stay in `inbox`.
- **Sect Raid and Bounty Hunt** are classified as duties (sect / bounty) rather than plain alerts: they alert when the toggle is off (same as A) and run by themselves when it is on.
- **`player == 2`** (tracked NPC / spectate) now runs the NPC pipeline exactly like AUTO instead of random-walking. `player` still distinguishes human (1) from tracked (2).
- **Decisions:** kill/spare only applies to cultivators (beasts are always killed, otherwise auto-hunting would stall). Default policies are conservative: always spare, flee when outmatched, no forbidden technique, accept duels.
- **Do Quest** also has an ad-hoc form `do <name> [person#]` for spot/area/person quests that validate themselves (Donate to Sect, Buy City Array, Restock Trip, Have Child, ...). Quests that would be exploitable on demand (Go Sell, Return to Sect, Find Parent's Body, Ghost City Rebirth, Possess Womb) are queue-only.
- **Waypoint quests** ("Go: label") need an existing tile; they are pinned, never muted or expired.
- **Shop prices:** buy rate 0.05–1.0, sell rate at least buy rate + 0.05 and at most 5.0 (prevents NPC buy/sell arbitrage loops); NPC sellers ignore a shop paying under the city pool rate.
- **Added afterwards:** auction bidding by hand (max bid per lot when standing in the city and able to meet the reserve), GUI script editor window, per-character decision timeout (`policy timeout`). Not built: a quest-editor window (the numbered `q` list and commands cover it).
- **NPC diplomacy aimed at a human** also asks (yes/no through the broker; default accepts, except defection which defaults to no): tribute demand (refusing = the demanding sect declares war, as when an NPC can't pay), alliance proposal, sect recruitment, defector offer.
