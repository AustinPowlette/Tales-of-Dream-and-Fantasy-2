# Tales of Dream and Fantasy 2 — Game Info

## Overview
A Java-based text-driven xianxia/cultivation simulation. A large NPC world (default 100,000 characters) runs in real-time turns (one turn ≈ one week). Characters cultivate, fight, forge items, form relationships, marry, have children, build cities/sects, and die of old age—all autonomously. The player participates in the same simulation.

Language: Java. Entry point: `src/Game/GameDriver.java`.

---

## File Map

| File | Purpose |
|---|---|
| `GameDriver.java` | Main loop, player I/O, tick ordering |
| `GameDriver2.java` | Simplified test harness |
| `Character.java` | Core class — all character logic (~3,900 lines) |
| `Area.java` | World tile — biome, resources, spawning |
| `Map.java` | Static world container (tile grid + global lists) |
| `Item.java` | All item types (Weapon, Clothing, Herb, Metal, Pill, Array, Blood) |
| `Technique.java` | Movement and Martial techniques |
| `Quest.java` | Quest data + priority queue ordering (+ `pinned`/`userCreated`/`bounty` flags, `displayName()`) |
| `QuestQueue.java` | `PriorityQueue<Quest>` subclass that refuses muted quest names (player characters only) |
| `PlayerControl.java` | Per-character player-control state: mode, duty toggles, policies, alerts, quest classification |
| `PlayerCommands.java` | Text-command dispatcher for human characters (movement, info, quests, work, city/sect, shops, `do`, scripts) |
| `DecisionBroker.java` | Sequential prompts (kill/spare, flee, forbidden technique, duel) with policy fallback |
| `PlayerScript.java` | Hybrid-mode scripts (steps, loops, conditions, interrupts) |
| `ListQuery.java` / `PlayerLists.java` | Shared filter/sort/limit language and the list views (cultivators, monsters, shop stock, backpack, places) |
| `PlaceImages.java` | Place pictures: name parsing, moods, tints, overlays, cache (art lives in git-ignored `images/`) |
| `GuiWindows.java` | Quest panel, duties & policies, manage (shops/city/sect), rankings & charts, market windows |
| `Theme.java` | Look and feel: FlatLaf loader (run-time, from `lib/`), ink & jade palette, fonts, title banner, tile colour filter |
| `ArtGen.java` | Painterly place pictures from a noun, biome and seed (watercolour washes) |
| `GuiExtras.java` | Saves, settings, family tree / relationship graph, notifications + toasts, tutorial, recap, start-up save menu, global cultivator search |
| `Ui.java` | Button tooltip table, live text-size scaling, tooltip preference |
| `World.java` | The lock that lets the GUI read the world without racing (or blocking) the game thread |
| `SaveManager.java` / `SaveData.java` | Save slots, backups, rotating autosaves, crash slot, start-up menu |
| `Settings.java` | User preferences (`settings.properties`) |
| `Notifications.java` / `Hints.java` / `Recap.java` | Notices, one-time hints + tutorial text, periodic recap |
| `Family.java` / `Exporter.java` | Family tree and relationship ties data; CSV / PNG export |
| `Organization.java` | Sect/city faction object |
| `Library.java` | City and sect technique libraries |
| `Shop.java` | Merchant-owned shop for buying/selling items |
| `Relationship.java` | Single relationship record |
| `RelationshipList.java` | ArrayList wrapper for relationships |
| `Dictionary.java` | Static name/biome/creature data arrays (regenerated: tiered beasts, grades, 1,300+ place/beast words) |
| `Naming.java` | Name engine: people, beasts (by tier), places, cities, sects, dungeons, shops, libraries, techniques, titles, item grades; unique-name registries |
| `Starts.java` / `StartMenu.java` | Character starts (protagonist / standard / custom / ten stories), difficulty, NPC prehistory, story objective engine; the start dialog / prompts |
| `Settlements.java` | Yearly founding of cities and sects in proportion to the number of cultivators |
| `Reincarnation.java` | The rope (level cap of a potential), the "Reincarnate" quest, rebirth into the family, lineage |
| `Taming.java` | Beast taming: cap, enlist, follow, release, bonus |
| `Life.java` | Food, wine, poison, music, the five life-arts traits, effects (`Fx`) |
| `Ambient.java` | Generated ambient music |
| `Chronicle.java` | World chronicle, death causes, life samples, notes/bookmarks (saved) |
| `TextTools.java` | Terminal tools shared with the GUI: map, route, time, watch, history/journal, notes, compare/equip/techniques, dashboard, help topics |
| `GuiMore.java` | Backpack, Chronicle, Biography, Notes, World overview windows and the Dashboard tab |
| `CultivationGame2.txt` | Original design document |
| `CultivationGame2Recent.txt` | Recent todo/in-progress notes |
| `Cultivation Game 2 Temp.txt` | Scratch notes and quest logic plans |

---

## Architecture

```
GameDriver.main()
  │
  ├─ Map() ──► 31×31 tile grid, origin Area "Originis" at center
  ├─ 100,000 Character NPCs spawned at origin
  │
  └─ Infinite Loop (each iteration = 1 week):
       1. TIME/CULTIVATE PHASE
          └─ character.time()         → age, births
          └─ character.cultivate()    → accumulate exp, level up, breakthroughs
          └─ sect bonus cultivation   → founder.majorLvl extra cultivate() calls
       2. ACTION PHASE
          ├─ Phase 2A, human/tracked characters (serial):
          │    MANUAL → prompt for commands (info/setup are free) → quest() runs only duty-enabled quests + announces alerts
          │    AUTO   → npcTurn() (same as an NPC; decisions answered by policy)
          │    HYBRID → PlayerScript.tick() → quest() (duties + alerts)
          │    player == 2 (tracked NPC) → npcTurn()
          ├─ Phase 2B, NPCs (serial): npcTurn() = party logic / explore / movement() → quest()
          └─ Child follow-mother logic (age < 8)
       3. AREA AGING PHASE
          └─ area.age() → +1% resources/monsters/population per tick
          └─ City taxation: cityOwner.ss += population/100 once per year
          └─ Monsters roam and battleCheck() against characters in their tile
       4. CLEANUP PHASE
          └─ Add tempCharacters to characters list
          └─ Null out remCharacters slots (dead/removed)
          └─ Move awakenedMonsters → characters (direct); remove from monstersList
          └─ Clear temp lists
```

---

## Character System

### Attributes
- `id` — unique index into `Map.characters` ArrayList (used for all cross-references)
- `lvl` / `majorLvl` / `minorLvl` — total level, realm tier (0–10), sub-level (1–10)
- `tempLvl` — combat power = `(lvl + weapon.lvl + armor.lvl + martial.lvl + movement.lvl) / 5`; IronBody adds `majorLvl/2+1`; Cheater adds `5`
- `potential` — 1–100, generated via `101 - sqrt(rand.nextInt(10000)+1)`; limits breakthrough success
- `cultivation` / `cultivationBase` / `cultivationBaseMax` — accumulated xp toward next level
- `inhibitor` — grows on each failed breakthrough, slows cultivation speed
- `caution` — 20/35/50/65/80; threshold below which a character prefers to flee
- `ss` — Spirit Stones (currency)
- `affiliation` / `affiliationNum` — "Good"(+1) / "Neutral"(0) / "Evil"(-1)
- `professionLvl` / `professionExp` / `professionExpMax` — profession skill (1–100)
- `maxAge` / `totalAgeGained` — lifespan; each breakthrough adds age
- `sectPoints` — contribution points for a sect; earned via missions and donations
- `sectMission` — String encoding active mission: `"Type|SubType|lvl|qty|bonus"`
- `isGhostForm` / `originalName` — Ghost physique rebirth state

### Constructors
| Constructor | Use |
|---|---|
| `Character(Area)` | Initial NPC or town spawn |
| `Character(Character mother)` | Birth from married couple |
| `Character(Area, Character cultivator)` | Beast monster |

### Trait System (1 per character)
| Trait | Rarity | Effect |
|---|---|---|
| Cheater | 1 in 1,000,000 | +5 tempLvl in combat |
| Lucky | 1 in 10,000 | +45 win chance; loots dead as profession |
| Ghost | ~1 in 33,000 | On death (non-Beast killer): enters ghost form, seeks a city, reborns as a new character; items dropped, sect resigned |
| IronBody | ~1 in 33,000 | `+majorLvl/2+1` to tempLvl in combat (natural armor) |
| SpiritVein | ~1 in 33,000 | 1.5× cultivation speed |
| HeavenlyRoot | ~1 in 33,000 | 2× technique upgrade chance on kill (1 in 2 vs 1 in 5) |
| Leader | ~1 in 33,000 | Creates cities (profLvl=1) and sects (profLvl=50+) |
| Innovative | ~1 in 33,000 | Creates techniques; sells techniques to city libraries |
| Blacksmith | 1 in 100,000 | Smithing profession |
| Alchemist | 1 in 100,000 | Alchemy profession; forages if missing materials |
| Scribe | 1 in 100,000 | Array crafting |
| Merchant | 1 in 100,000 | Runs a Shop; buys from sellers, sells to buyers, arbitrages between cities |
| Gatherer | 1 in 100,000 | Foraging profession |
| Miner | 1 in 100,000 | Mining profession |
| Friendly | 1 in 100,000 | Social interaction profession |
| Tailor | 1 in 100,000 | Clothing crafting |
| Runner | 1 in 100,000 | Guaranteed flee if opponent is ≤10 levels higher |
| Monster | Beast → kill → awaken | 10% chance when a Beast kills a cultivator in `winCheck()`: flips trait to Monster, gains a legendary title prefix (Ancient/Dread/Elder/etc.), enters the main NPC loop (movement + quests + relationships). Evil-aligned, hunts beasts ("Hunt" spot quest) and cultivators ("Hunt Cultivator" spot quest) in current tile, migrates to high-level areas ("Seek Territory" target quest). Gains `cultivate()` XP on every cultivator kill (up to 20 calls). Earns legendary title suffixes on kill milestones (Legend@10, Tyrant@20, Dread Lord@30, Immortal Terror@50); each milestone also grants +10 cultivate(), 5% chance +1 potential, and posts a high-priority bounty (priority 65) at nearest city. |
| Beast | Beast constructor | Combat-only NPC, no inventory/quests |

---

## Cultivation System

### Realms (majorLvl)
| majorLvl | Realm Name | Potential Required | Lifespan Bonus |
|---|---|---|---|
| 0 | Qi Cultivation | — | base 100y |
| 1 | Foundation Stage | ≥20 | +100y |
| 2 | Core Formation | ≥37 | +200y |
| 3 | Soul Wandering | ≥52 | +400y |
| 4 | Nascent Soul | ≥65 | +1,900y |
| 5 | Ensoulment | ≥76 | +3,700y |
| 6 | Nihility | ≥85 | +6,100y |
| 7 | Ascension | ≥92 | +9,100y |
| 8 | Half-Immortal | ≥97 | +13,700y |
| 9 | Pseudo Immortal | =100 | +16,900y |
| 10 | True Immortal | — (1 in 5 chance) | +2.14 billion y |

### Breakthrough Failure
- If potential is below the threshold AND a random check fails (1 in 15 or 1 in 10 at higher realms), the character drops back one level and `inhibitor++`.
- `inhibitor` penalizes future cultivation speed by a factor of `2^inhibitor`.

### Breakthrough Tribulation
- Fires when a character crosses majorLvl 3, 5, 7, or 9.
- Pass chance: `clamp(10, 90, potential - (majorLvl - 3) * 5)` percent.
- **Pass**: 10% chance of +1 potential; `cultivationBase += cultivationBaseMax * 0.5` bonus.
- **Fail**: majorLvl reverts to previous realm, `inhibitor++`, cultivation slowed.
- Printed via `printBreakthroughs` flag or player guard.

### Potential Gain Events
- 1 in 100 chance on any major realm breakthrough.
- 1 in 50 chance on surviving a `forbiddenCheck()` during combat.
- 4% chance on entering a HAUNTED_GROUNDS dungeon (plus 5 extra `cultivate()` calls).
- 25% chance on completing a CELESTIAL_TRIAL dungeon.
- Via Potential Pills (crafted by Alchemists, sold at city pill markets).

### Dual Cultivation
- 5%/turn: if spouse is in same tile → each partner gains 3 extra `cultivate()` calls.
- 5%/turn: if best friend (relationship value ≥ 3) is in same tile → each gains 1 extra `cultivate()` call.

### Legacy Titles
- When a character dies at majorLvl ≥ 7, their name and level are appended to `Map.legacyTitles` / `Map.legacyLvls` (no duplicates).
- Living characters have a 2%/turn chance to meditate on a random legacy: earn `min(legLvl/10, 10)` extra `cultivate()` calls.

### Sect Bonus
- If a character belongs to a sect, the game loop grants them `Math.max(founder.majorLvl, sect.sectLvl()/10)` extra `cultivate()` calls per tick.

---

## Combat System

### Battle Flow (`battle()`)
1. `tempLvlCheck()` — calls `equip()`, then computes tempLvl for both fighters.
2. `deathCheck()` — determines if the fight is a deathmatch based on relationship value.
3. Win chance: `50 - (15 * (opponent.tempLvl - this.tempLvl))` ± Lucky trait (±45).
4. If win chance < caution → attempt `fleeCheck()`.
5. If can't flee AND deathmatch → `forbiddenCheck()` (boosts tempLvl by sqrt(potential), costs lifespan).
6. Same check for opponent.
7. `durabilityCheck()` — equipment may break before final roll.
8. Final roll: `rand.nextInt(100)+1 <= winChance` → winner calls `winCheck(loser)`.

### Flee Logic (`fleeCheck()`)
- Runner trait: flee if opp is ≤10 levels higher → always flee.
- Both lvl and movementTechnique.lvl are higher → always flee.
- Partial advantage → 4 in 5 chance.
- Equal → 1 in 2 chance.
- Disadvantage → random proportional chance.

### Death Check (`deathCheck()`)
| Relationship Value | Death Chance |
|---|---|
| -4 (Wanted) | 100% |
| -3 (Arch Enemy) | 75% |
| -2 (Rival) | 25% |
| -1 (Dislike) | 5% |
| 0 (Neutral) | 1% |
| ≥1 (Friend) | 0% |
| Beast involved | Always deathmatch |

### City/Sect Destruction (`destroyCity(area)`)
- Triggered from `winCheck()` at 20% chance when an Evil character or Beast kills someone at an array-free city or sect area (Originis is protected).
- Plunders all area bags (backpack, materialBag, pillBag, arrayBag) to attacker's bags.
- Adds Blood to attacker's materialBag: `quantity = max(1, population/100)`.
- Gives attacker 10 extra `cultivate()` calls.
- 10% chance of +1 potential for the attacker.
- Transfers `area.population` ss to attacker; reduces population to 10%.
- Dissolves sect if present: all members lose their sect, treasury cleared.
- Destroys city: `city = false`, `cityOwner = null`, shops and libraries cleared.

### On Death (`dead(killer)`)
- True Immortal rebirth (majorLvl == 10): resets `dead=false`, `time=0`, `age=0`; killer still gets the kill credit; rebirth message printed.
- Ghost physique: enters ghost form instead of dying (once only; beast kills are final).
- Transfers all items to killer.
- Creates Blood item for killer's materialBag (quantity = potential + lvl).
- Friends (relationship ≥ 2) of the deceased lose relationship with killer by `-(value-1)`.
- If player dies and `changeMc = true`, killer becomes the new player character.
- Beast death: killer gains ss equal to beast's ss (`lvl^2.5 * 10 * (1 + potential/100)`, matching item price scale); beast gains XP from lvl kills.
- Legacy recording: if majorLvl ≥ 7, name appended to `Map.legacyTitles` (no duplicates).

### Weapon Enchantments
- Innovative characters have a 5%/turn chance to enchant their equipped weapon (in `professionPicker()`).
- Type chosen from: Sharp, Spiritual, Swift, Heavy, Fierce, Wrathful, Sundering.
- `Item.enchantBonus` = 1–5; `Item.enchantment` = type string.
- Applied in `tempLvlCheck()`: `tempLvl += equippedWeapon.lvl + equippedWeapon.enchantBonus`.
- Displayed in `Item.toString()` as `"Name lvl: N [EnchantType +B]"`.

---

## Profession System

### professionPicker() Dispatch
| Trait | Action |
|---|---|
| Alchemist | `alchemy()` → if failed (no materials), `forage()` |
| Blacksmith | `smithing()` |
| Friendly | `interact(random target)` |
| Tailor | `tailoring()` |
| Scribe | `array()` |
| Leader | Every tick from `quest()`: `cityLeadership()` at Major Lvl ≥ 3 (unless already city lord), `sectLeadership()` at Major Lvl ≥ 2 (if not in a sect) |
| Innovative | 1 in 100 chance → `technique()`; otherwise sells to city library |
| Merchant | Buys from city pool, sells to nearby characters via Shop |
| Lucky | `eventLootDead()` |

### Resource Requirements
| Profession | Material Needed | Quantity |
|---|---|---|
| Alchemy (herb) | Herb of appropriate level | ≥50 per pill batch |
| Alchemy (potential) | Blood | ≥1,000 per pill |
| Smithing | Metal | 1,000 per weapon |
| Tailoring | Any material | 1,000 per robe |
| Array | Any material | 10,000 per array |

### Profession Level Cap
- All professions cap at 100.
- `professionExpMax = 10^(professionLvl/10)` — exponential curve.

---

## Quest System

### Architecture
- `spotQuests` — `PriorityQueue<Quest>` for immediate (no-travel) actions.
- `targetQuests` — `PriorityQueue<Quest>` for goals requiring pathfinding.
- `currentQuest` — active target quest in progress.

### Quest Priority
| Range | Label |
|---|---|
| 0–20 | None |
| 21–40 | Low |
| 41–60 | Medium |
| 61–80 | High |
| 81–100 | Critical |

### Quest Codes (return values)
| Code | Meaning |
|---|---|
| -3 | currentQuest is null / queue empty |
| -2 | Spot quest failed |
| -1 | Target quest failed |
| 0 | Progressed (pathfinding, not arrived) |
| 1 | Target quest success |
| 2 | Spot quest success |

### Initial Spot Quests (from `questInitializer()`)
| Name | Priority | PriorityDiff | MaxPriority |
|---|---|---|---|
| Random | 35 | 2 | 90 |
| Profession | 30 | 3 | 70 |
| Hunt | 30 | 5 | 50 |
| Test | 45 | 5 | 10 |

### Monster Spot Quests (from `monsterQuestInitializer()`)
Monsters get a separate initializer — no Profession/Test, high-priority Hunt:

| Name | Priority | PriorityDiff | MaxPriority |
|---|---|---|---|
| Hunt | 60 | 5 | 80 |
| Hunt Cultivator | 55 | 5 | 75 |
| Random | 25 | 2 | 50 |

**Hunt** — calls `beastBattleCheck()` in current tile; tracks sect Hunt mission kill count (decrement remaining qty per completion).
**Hunt Cultivator** — iterates `location.characters`; finds first non-Beast non-Monster alive character and calls `battleCheck()`. Monsters actively prey on cultivators.

### Target Quests (selected)
| Name | Trigger | On Success |
|---|---|---|
| Find Person / Battle Dislike / Battle Rival / Battle Arch-Enemy | Relationship tier (±1 weak, ±2, ±3 strong) — one per person, resynced by `syncSocialQuest()` | `interact()` / `battleCheck()`; weak ties are one-time, capped at 4 each and replaced by a different cultivator (`refillWeakSocialQuests()`); strong ties persist. Lowest priority band (10–48) |
| Meditate (spot) | Always queued (15/+2/40) | `cultivate()` — an extra cultivation tick (replaced the old "Test" no-op) |

**Queue mechanics:** target quests are polled by priority; a repeating quest is re-queued at priority 0 and *released* after each success, so the queue picks the next trip. `Character.bump()` ages all quests by `priorityDiff`, clamps to `maxPriority` and rebuilds the heap. `maintainQuests()` (yearly per character) clamps priorities, expires one-time quests older than 10 years (`Quest.createdAt`), caps the target queue at 50 (lowest priority shed first), and tops up weak social quests.
| Hunt Beast | Known area whose major realm is own realm to 2 below; same-realm rallies a party | `partyBeastHunt()` (solo falls back to `beastBattleCheck()`) |
| Seek Territory | Monster, 20%/turn, highest-lvl known area > current+10 | `beastBattleCheck()` on arrival |
| Battle Rival | Dislike/Rival/Arch-Enemy relationship exists | `battleCheck()` with target |
| Revenge | Close friend (value ≥ 2) dies | `battleCheck()` with killer |
| Bounty Hunt | Can't afford revenge target, or legendary Monster milestone | Post or accept bounty on Area.bounties |
| Return to Sect | Member is not at sect location (5%/turn) | Sect arrival grants +2 sectPoints |
| Deliver Materials | Sect mission complete but away from sect | Transfer materials to treasury, gain sectPoints |
| Ghost City Rebirth | Ghost physique: seeking a city | Rebirth as new character at city |
| Buy Gear | Equipment slots empty or gear outgrown | Purchase from city market |
| Sell Stock | Backpack over threshold (Merchant) | Travel to destination city, sell cargo |

### Sect Mission System
- Sect masters post missions to members via `postMissionsYearly()` (yearly).
- Mission formats: `"Forage|Herb|lvl|qty|bonus"`, `"Mine|Metal|lvl|qty|bonus"`, `"Hunt|Beast|lvl|qty|bonus"`.
- Hunt missions posted when `sectLvl() >= 5`; qty = kills remaining (decremented in "Hunt" spot quest case on each `beastBattleCheck()`; completes when qty reaches 0).
- Two-phase delivery for Forage/Mine: if mission completed away from sect, a Deliver Materials quest queues; if at sect, materials transfer to `sect.treasury` immediately. Hunt missions complete in-place (no delivery needed).

### Pathfinder (`pathfinder()`)
- Moves diagonally toward destination using wrap-around world logic (`pathfindXY(x, y)`; scripts call `stepToward`).
- Returns 1 when the destination tile is reached.
- A MANUAL/HYBRID human character only paths for a duty quest on ticks the player did not move (`PlayerControl.movedThisTick`).

---

## Player Control (`planning.md` has the full design)

Human characters (`player == 1`) carry a `PlayerControl` (`Character.pc()`); NPCs never do. Tracked NPCs (`player == 2`) run the plain NPC pipeline.

### Modes
| Mode | Behaviour |
|---|---|
| MANUAL (default) | Prompts for a command each turn. Info/setup commands are free; actions use the turn. `quest()` runs only duty-enabled quests and announces the rest. Combat decisions prompt through `DecisionBroker` |
| AUTO | `GameDriver.npcTurn()` — identical to an NPC; decisions come from policies. `auto [turns]`; `stop`/`manual` (typed while it runs) hands control back |
| HYBRID | A `PlayerScript` drives the character one turn at a time; `quest()` still handles duty-enabled quests and alerts. Ends → MANUAL (or AUTO with `policy script auto`) |

### Quest classification (`PlayerControl.classify`)
- **Routine (ignored in MANUAL/HYBRID):** Random, Profession, Hunt, Meditate, Forage, Mine, Interact, Sell Technique, Find Person, plain Hunt Beast, Buy Gear, Go Sell, Return to Sect, Seek Territory, Hunt Cultivator.
- **Alert (announced with target, coordinates, distance, reason; travel there and use Do Quest):** everything else not listed as a duty — Spectate Tournament, Battle Rival/Arch-Enemy, Revenge, Explore Dungeon, Heaven Treasure, Rob Caravan, Join Sect, Gift Technique, Find Parent's Body, Ghost City Rebirth, Possess Womb, Disperse, player waypoints ("Go: label").
- **Duty (auto-run when the toggle is on, otherwise announced):** loot (Loot Corpse), tournament (Register Tournament), social (Battle Dislike), bounty (city beast bounties, Bounty Hunt), upkeep (Seek Potential + pill use, gear buying, selling surplus, library study), family (Get Pregnant — shown as "Have Child"), city (Buy City Array, Entice Merchant, Hold Tournament as lord, founding), sect (missions, donations, Buy Sect Array, recruiting, diplomacy, Sect Raid, promotions), shop (Restock Trip, Sell Stock), craft (crafters' gathering/crafting routine).
- Ten duty toggles + `duties all on|off`: any target set is one master press plus at most five single flips.

### Quest tools
`q` (numbered view: active, target, spot), `qp <n> <pri>` pin a priority (never ages, is never clamped, expired or shed), `qu` unpin, `qd` delete, `mute/unmute <name>` (`QuestQueue` refuses muted names so a deleted quest stays deleted), `qnew <label> <x> <y> <pri> [repeat]` (waypoint quest), `qdo <n>` Do Quest (`Character.doQuestNow`: viability + "is the player at the target tile" checks with a printed reason, then the same handler and `finishTarget`/`finishSpot` bookkeeping as `quest()`), `inbox` (alerts).

### Decisions (`DecisionBroker`)
Kill or spare (`winCheck`; spare = don't kill, nothing else changes; only cultivators — beasts are always killed), flee (`fleeCheck`; fleeing can fail — `fleeRoll` holds the original formula), forbidden technique (`forbiddenCheck`), accept/evade a challenge (`battleCheck` → `acceptChallenge`), auction bids (`Auctions.hold`: a MANUAL human in the city who can afford the reserve gives a max bid per lot; the winner pays the runner-up's bid), and yes/no offers from NPC sects aimed at a human (tribute demand — refusing means war, alliance, recruitment, defection). `policy timeout <s|off>` makes an unanswered prompt (console or GUI) fall back to the policy answer. Non-MANUAL characters answer from policies: kill 0 always spare / 1 evil only / 2 if wronged / 3 always kill (default 0), flee on, forbidden off, duel accept.

### Scripts (`PlayerScript`)
Steps: `goto x y`, `wait n`, `forage n|until q`, `mine n|until q`, `craft n`, `meditate n|until major L`, `hunt n`, `sell`, `quest <name>`, `repeat n|forever … end`, `if <ss|major|level|age|herb|metal|items|potential> >=|<= n … end`, `mode auto|manual`. Interrupts: attacked, breakthrough, tile depleted, missing quest/city/beasts, optionally a new alert. Coordinates accept displayed values (x*100) or tile numbers.

### Shops
`price <shop#> buy|sell|city <rate>` sets `Shop.buyRate` (0.05–1.0), `sellRate` (≥ buyRate+0.05, ≤ 5.0), `cityBuyRate` (0.05–1.0). NPC shops keep 0.55/1.5/0.30; NPC sellers skip a shop paying under the city pool's 0.5.

---

## Concurrency (`World`)
The game thread takes `World.lock` (fair `ReentrantLock`) at start-up and gives it up whenever it waits: `GameDriver.readInput/readInt/readLine`, the pause/tick-delay sleep, `edtWait` (any Swing dialog it raises), the save-thread join, and the end-of-tick `World.handoff(refreshNow, 250)`. Readers (`World.read(ms, r)`, `World.readWait(ms, r)`) `tryLock` with a timeout and skip the refresh when the game is mid-tick, so the EDT never blocks indefinitely and the game never waits for the GUI (a waiting reader only delays the game by the time it holds the lock). `MapPanel` paints into an off-screen image under `World.read(30ms)` and falls back to the last finished frame. All writes to game state happen on the game thread: GUI buttons only enqueue commands (`MapViewer.queueInput`). `GameDriver.frozenNpcs` freezes NPC/monster turns for deterministic tests.

## Saves (`SaveManager`)
`saves/<name>.dat` (serialized `SaveData` incl. `version`, `savedAt`, `playerSummary`), `<name>.meta` (kind, savedAt, year, population, players, bytes, CRC) and `<name>.bak` (previous good copy). Writing = snapshot on the game thread, write `.tmp` on a big-stack thread, CRC, move into place, keep the old file as `.bak`. Autosave (every `Settings.autosaveYears`) writes `auto-0` then rotates to `auto-1` (newest) .. `auto-N` (`Settings.autosaveKeep`). `load` verifies the CRC and falls back to `.bak`. An uncaught exception writes `crash.log` and a `crash-<time>` slot. Quick save (Ctrl+S / Save button / `f`) = slot `quick`. `state/lastName/lastAt/lastYear` drive the GUI indicator. The legacy `save.dat` is listed as a slot. Save/load requests are volatile flags (`saveRequested`, `saveRequestName`, `loadRequest`) honoured at the top of the next tick, when the world is consistent.

## Settings, Notifications, Hints, Recap
`Settings` (volatile fields, saved on every change): autosave, font scale, colour-blind map, pictures, tooltips, hints, confirmations, toasts, sound, muted notification categories, recap weeks. `Notifications.post(cat, text, charId, forId)` records history (500), prints `[Notice] ...`, calls the GUI listener (toast) and optionally beeps; categories: breakthrough level death war family alert danger auction hint recap (level starts muted). Hooks: `Character.dead/birth/marriage`, `Organization.declareWar`, `PlayerControl.scanAlerts`, `Notifications.observe` (levels, crowd danger, lifespan) each tick per human. `Hints.trigger(h, id, text)` fires once ever per id. `Recap.check()` (top of tick) builds a period summary per human from a baseline snapshot and shows it in the GUI (`GuiExtras.recapWindow`).

## Confirmations and previews
`PlayerCommands.confirm(c, confirmed, prompt, line)`: asks via `DecisionBroker.askYesNo` (default No) when `Settings.confirmRisky`; a leading `!` on a command, or a non-MANUAL character (script/AUTO), counts as confirmed. Guarded: leave sect, disband, declare war, delete a pinned/own quest, mute, attack when win chance < 35% or death chance >= 50%, clear a script of >3 steps, control another cultivator, overwrite/load/delete a save. `Character.previewFight(other, ...)` returns `FightPreview` (win/death/escape chances, `oneLine()`, `detail()`), used by the `preview` command and inside the duel and flee prompts.

## Player tools added in the QoL pass
`find <name|#id>` searches everyone (`PlayerLists.world`), `follow/unfollow`, `control <id>` (target becomes the human; the previous character becomes tracked `player == 2`; duties/policies copied), `family`, `relations`, `notices`, `recap`, `tutorial`, `export history|rankings|<list kind>` (CSV in `exports/`), `settings ...`, `save/saves/load/delsave`. GUI: Saves, Settings, Family (tree + graph), Notifications windows; toasts; Browse "Search anyone"; Export menu (map PNG, screenshot, log, CSV); Rankings chart PNG.

## Look, layout and painted art
`Theme.install()` (called on the EDT before the window is built) loads FlatLaf from `lib/flatlaf-*.jar` by reflection and applies the palette (`@background`, `@accentColor`... plus `UIManager` overrides for arcs, scrollbars, tabs, tables); missing jar -> Nimbus with the same colours. Window layout: header (banner + speed controls), status strip, then a split of [layer toolbar + map | right column] over the event log. The right column is a split of Tile Info over NPC Tracker above the action dock (tabs Move / Me / Auto / World). The map panel implements `Scrollable` (tracks the viewport), draws a square world centred with `offX()/offY()`, and answers `getToolTipText` for the hovered tile. `PlaceImages.render` picks: hand-made file (`<noun>_<biome>`, `<noun>`) -> else `ArtGen.render(noun, biome, seed, w, h, quality)` when `Settings.paintedArt`; tints (mood/season/level/hotspot) and overlays apply on top either way.

## Early-game rules (2026-09-30)
First generation: age 18 (`GameDriver.START_AGE`). First-year truce in Originis (`TRUCE_WEEKS`, checked in `Character.battleCheck`). Fight lethality: `battle()` rolls `deathCheck` once (relationship value: >=1 friendly 0%, 0 strangers 1%, -1 5%, -2 25%, -3 75%; beasts: only when they out-level the fighter by more than 5, or ambush) and passes it to `winCheck(loser, lethal)`; a non-lethal loss leaves the cultivator injured (cultivation x0.7 until the next level-up). Beasts never kill a cultivator at or near their level unless they ambush (crowd hotspots) or out-level by 6+. Luck: see `Character.fortuneEvent`, `socialBonus`, loot and cultivation in `cultivate()` / `dead()`.

## Starts, difficulty and prehistory (`Starts`, `StartMenu`)
`StartMenu.choose` (dialog in the GUI, numbered prompts in the console) returns a `Starts.Choice`; `Starts.apply` builds the character: PROTAGONIST (potential 100, Alchemist), STANDARD_ROLL (the NPC curve; one re-roll when potential < 20; `potentialHidden` shows a word - dull/ordinary/talented/gifted/prodigious/heaven-chosen - until Major >= 1), STANDARD_CUSTOM (the character editor, nothing hidden), STORY (one of ten `Story` records: potential, trait, affiliation, money, gear major, potential pills, NPC specs, objective steps with rewards). `GameDriver.difficulty` 0/1/2 (Story/Hard/Brutal) is saved. Hard: potential x0.6, hidden, half money, no gear. Brutal: potential 1-20, hidden, nothing, hooked stories (heir, orphan, ghost, lucky) keep their chain, the rest are pure survival; a lost life is final (`handleReincarnation` returns early). Hostility: evil odds `max(30, 100-35*d)`, `deathCheck` +12*d for fights with the player. `Starts.prehistory` runs `(START_AGE-8)*52` `cultivate()` calls for the NPC first generation. `Starts.tick(c)` advances story objectives each turn (`story` command / Story window).

## Sect rules (`Organization`)
`traitRequired` (a craft), `minPotential`, `minLevel`, `affiliationMatch`, `genderPref`, `familyName`; `whyNot(c)` / `canJoin(c)`. `rollRules` (NPC founders; a human's sect starts open): at most two of: craft 45% (not for Leader/Ghost/rare physiques), potential floor 35% (40-70% of the founder's), level floor 30% (about a quarter to a third of the founder's), alignment 25%, gender 12%, family 10%. The master changes them with `sect rules potential <n> level <n> gender male|female|any affiliation good|neutral|evil|any trait <name|any> family <name|any>` (or `open`), or the Manage window.

## Run statistics (`RunStats`)
`Data` (saved in `SaveData.runStats`): start (kind, difficulty, first name), a `Life` per body the human has lived in (peak level and realm, kills, potential at start and end, how it ended), totals (peak wealth, deepest dungeon level, companions tamed, sects/cities founded, children, loved ones outlived). Hooks: `begin` (new game), `yearly`, `endLife` (death, `handleReincarnation`), `onReincarnation`, `finish` (Brutal death, `retire`) which appends a line to `exports/hall_of_fame.txt`. Commands `runstats`, `halloffame`, `retire`, `export run`; GUI Run statistics window.

## Terrain (`BiomeGen`)
`Map.worldSeed` (saved) seeds tileable value-noise fields: elevation (three octaves, stretched; sea below 0.22, highland above 0.84), moisture, temperature (a warm and a cold band around the torus + noise, colder with height) and a ridged river field. `BiomeGen.cell(seed, x, y, range)` returns the biome pair (`biomeM`, `biomem` as before: Ocean/Beach/Desert/Frozen Desert/Tundra/Frozen Grassland/Taiga/Grassland/Savanna/Forest), elevation, moisture, temperature and `river`; land next to the sea is a Beach; the middle of the map (Originis) is pulled toward a temperate forest. `Area` stores `elev`, `moist`, `temp`, `river`; names use `BiomeGen.nounFor` (highland / river / wet / lowland nouns); herbs scale with moisture, metal with elevation, spirit veins are likelier on peaks. Old saves keep their tiles; unexplored tiles of an old save use a fresh seed.

## Seasons and politics (`Seasonal`)
On every new season each city of 500+ people holds a festival: spring Blossom Festival (inspired attendees, new friendships), summer Martial Fair (the lord is nudged to hold a tournament), autumn Harvest Fair (shops restock, cooks and brewers sell twice), winter Lantern Night (warm meals, musicians play); the city treasury gains population / 200. Once a year `diplomacy`: mutual wars end in a truce (25%), allied sects hold a joint gathering (20%), neighbouring sects at peace quarrel (4% per sect, +2 squabbles each). Recorded in the chronicle and sent to humans of the sects. `Life.bounty` posts a "Bounty Hunt" on a recognised poisoner (killed by poison, or seen: 1 in 4).

## Naming (`Naming`, `Dictionary`)
All names come from `Naming`: `person`, `beastSpecies(level)` + `beastAdjective`, `place(biome)`, `city`, `sect`, `dungeon`, `shop`, `library`, `technique`, `title`, item grades. Unique names are tracked in registries (`rebuild()` after a load, `release` on death); a name that is taken gets a numbered successor (`II`, `III`...). `Area.geoName` is the geographic name used for pictures when a tile is renamed (city, dungeon).

## Settlements (`Settlements.yearly`)
Each year: cities wanted = cultivators / 1,000, sects wanted = cultivators / 300; missing ones are founded by living candidates (M2+ for both; weights Leader 8, Merchant 3, Friendly 2) via `foundCityNow` / `foundSectNow`, at most 6 cities and 15 sects a year.

## Reincarnation (`Reincarnation`)
Potential decides how far a cultivator climbs: each major-realm breakthrough gate needs potential 20 / 37 / 52 / 65 / 76 / 85 / 92 / 97 / 100 or it almost always fails (see `Character.cultivate`). `Reincarnation.cap(p)` = 10 * (gates passed) + 9 = the last level reachable (9, 19, 29, ... 89; 99 at potential 100). `Life.upkeep` (from `quest()`) posts the spot quest "Reincarnate" (priority 95, an alert for humans, in `DO_SPOT`) once `majorLvl >= 3` (Soul Wandering) and `lvl >= cap(potential)` and potential < 100. `perform` keeps the SAME character and gives a second youth: age 18 (`GameDriver.START_AGE`), level 1, Major 0, cultivation 0, `maxAge` 100 and `totalAgeGained` 100 (the realms' lifespan is earned again), `inhibitor` back to 1 (failed breakthroughs forgotten), conditions cleared, potential +5; name, family, sect, relationships, wealth, items and techniques stay; `pastLives` / `lives` / `soulId` record the earlier lives (`lineage`, Family window "Past lives"; the tree marks "reborn xN"); `RunStats` closes the life and opens a new one. Companions beyond what a Major Lvl 0 cultivator may keep (1) wait for the soul (see below). Brutal changes nothing. Measured: from potential 52 the whole climb to 100 takes about 10 lives and about 3,900 years (a perfect potential-100 cultivator needs about 2,600). Death-time body takeover (`handleReincarnation`) is unchanged and titled "After death"; Brutal still ends the life on death.

## Companions that remember
When a character is reborn (`Reincarnation.perform`) the companions beyond the new cap (Major Lvl 0 + 1) are not freed: `Taming.waitFor` sets `waitingSoul` (they keep their tile and have no turn, like tamed ones) and the character gets `lostBeastIds` and `soulId`; a "Find Lost Beast" Area quest (priority 80 for a Tamer, 50 otherwise) is posted whenever there is room for another companion (checked every 26 weeks and at once if there is room). `Taming.reunite` (run each turn from `Life.upkeep` and by the quest) brings back the beasts standing in the child's tile, up to the cap (Major Lvl + 1), and says so; beasts that die while waiting are forgotten.

## Beast taming (`Taming`)
Only a Beast Tamer (trait) can tame. Cap = Major Lvl + 1 companions. `tame` uses one beast of the tile (`monsters--`, a new beast `new Character(area, master)`); too strong (level more than 5 above you) = refused; chance 35 + 3 * (your level - its level) (+30 Tamer, +10 instrument, +10 Friendly), 5-95; failure: half the time it attacks. A tamed beast gets an id (`GameDriver.UID++`), is added through `Map.addTemp` (population is pre-decremented like awakened monsters), has `masterId`/`partyLeaderId`, is never listed in a tile, has no turn (`GameDriver` skips `masterId >= 0` in the NPC pass) and is moved to its master's tile after all turns (`Taming.follow`). It cultivates like every character (so it levels and breaks through), adds `min(its lvl, master lvl) / 10` to the master's fighting level (`tempLvlCheck`), is freed when the master dies/reincarnates (`onMasterGone`) and cleared from the list when it dies (`onBeastDeath`; a beast freed in the tick it was tamed stays in its list slot until the end-of-tick merge so id == index holds). NPCs try now and then (`Life.upkeep`, 1 in 600 per turn per eligible NPC on a tile with beasts; Tamers on every Profession quest).

## Food, wine, poison, music (`Life`)
Each art belongs to its trait: only a Chef cooks, a Drunken brews, a Poisoner mixes poison and uses it (`poison`, `coat`), a Musician makes instruments and plays, a Tamer tames (anyone can eat, drink and be poisoned or inspired). Goods (`Character.goods`; `Item.ItemType` Food / Wine / Poison, stackable) are made from herbs, 50 per unit (`make ...`; `professionLvl / 10 + 1` units, herbs up to `professionLvl / 10 + 2`). Level = herb level 1-10 (like pills). Economy: a city has a goods market (`Area.goodsBag`, up to 600 portions): `sell goods|food|wine|poison [qty]` (and the automatic sale of a profession or the "Go Sell" quest, now also triggered by 20+ portions) pays half the unit price (unit price = a quarter of the pill price; poison costs three times as much and is sold to Poisoners only) and puts the portions on the shelves; `market` lists them and `buy market <#> [qty]` buys (the city lord gets the money); NPCs buy meals when unfed, wine now and then (the Drunken often), poison when a Poisoner has none, and a better instrument for a Musician from the city stock (`Life.npcShop`); food spoils 50% a year, wine 10%, poison 5% (`Life.marketYearly`); each festival season Chefs, brewers and Musicians within 8 tiles get a "Festival Trade" quest (sells at double, the Musician plays). Instruments are never sold by their Musician (`Character.sell` keeps the best). Conditions live in `Character.fx` (`Life.Fx`, null for most): Food = +15% cultivation x level ratio for 13 weeks (26 and x1.5 for a Chef), cures poison of equal or lower level; Wine = drunk 4 weeks: +25% cultivation, -1 fighting level (Drunken: +2 + level/4, x1.5 bonus, 6 weeks, a free cultivation tick one week in eight); Music = an Instrument in the backpack; `play` = extra cultivation ticks (2 for a Musician), everyone present gets inspired (+10% cultivation, 4 weeks, 8 for a Musician) and may like you more, a city pays tips; Poison = `poison <person>` or a coated blade (`coat`: 3 fights): chance 55 + 15 * (poison level - victim's resistance), resistance = major level + 1 (+2 Poisoner, +2 Iron Body, +1 fed); poisoned: cultivation x0.6, fighting level - (1 + level/3) for 13 weeks, then a lethal roll (20% per level above resistance, max 60%) if the poison outranks the victim; the killer is credited. The new traits replace a common trait 1.6% each (`Life.rollTrait`). City-born cultivators (`Area.age`) now arrive at 18 with the level their potential earned since 8 (`Starts.prehistoryOne`, like the first generation); a Ghost's city rebirth is also at 18. NPCs eat when not fed and drink now and then; a human in MANUAL mode does neither on their own. `Settings.autoEquipManual` (default off) stops the automatic gear swap for a human in MANUAL mode.

## Ambient music (`Ambient`, `MidiOut`)
A `Composer` writes whole pieces as note events (`Note`: time, instrument, pitch, velocity, duration, pan, glide): a title (`Naming.songTitle`), a tonic and pentatonic mode, a tempo of 56-80 bpm, four sections of eight bars (statement, answer, contrast, return) built on one two-bar motif that is transposed, inverted and re-ended, a chord progression for the pad, a guzheng (pipa in cities and war) lead with grace notes and runs, a dizi answering, an erhu singing long glides (contrast section, danger, Originis, autumn), a pad, gongs on section starts and chimes on section ends, then a short rest and a new piece in the same key. `setMood` (set from the player's tile by `followWorld`): 0 open, 1 city (faster, pipa, hand drum), 2 danger (low, sparse, taiko), 3 night/winter (slow, thin), 4 war (fast, taiko and wood block), 5 sect (temple block, bells), 6 Originis (gongs, erhu, slow taiko); a change between strong cues (2, 4, 6) starts a new piece in the same key. Engines: the built-in 44.1 kHz stereo synth (Karplus-Strong strings, bowed and blown harmonic tones, drums, bells, two decorrelated reverbs, per-voice pan) or the Java software MIDI synthesiser with a SoundFont from `soundfonts/` (`MidiOut`; General MIDI programs koto, flute, fiddle, warm pad, tubular bells, crystal, taiko, woodblock, shamisen, remapped in `soundfonts/programs.txt`; a font that fails to load falls back to the built-in synth and says why in `Ambient.engine`). Settings: `music`, `musicVolume`, `musicEngine`, `soundFont`, `musicMix` (per-instrument percent). Command `music` (on/off/new/song/volume/fonts/font/presets/engine/mix). No audio device = silently off. `Ambient.renderStereo/renderForTest` render samples without a sound card (90 s in 2-6 s).

## Movement, going places, quitting
Players move in eight directions (`w a s d` plus `nw ne sw se`; internally `movement('Q' 'E' 'Z' 'C')` = two single-axis moves in one turn, which is what `pathfinder` always did for NPCs). `goto x y`, the Move Here buttons and `goquest` set `autoX/autoY` and `movement = 'c'`; `PlayerCommands.stepAuto` takes the shortest signed distance on the wrapping world and steps diagonally. `chase <#id>` / `goquest` on a person-quest sets `chaseId`, re-aims at the target's tile each turn, stops when on the same tile, when the target is gone, or after 150 turns. `quit [nosave]` / the Quit button / the window X set `GameDriver.quitRequested`; the loop saves (QUICK slot) at the end of the turn and exits.

## Sects, wars, squads (changes of 2026-10-05)
Member cap `20 + sectLvl*8 + master.majorLvl*15` (`Organization.memberCap`, checked by `whyNot`, so every join path respects it). Wars: `warSince` is set by `declareWar`; `Seasonal.diplomacy` (yearly) ends a war with chance min(90, 15 x (years-1)) from year 2, and at years 5, 10, ... renews it 40% of the time, otherwise ends it; each war is judged once. Disciples of warring sects fight when they meet on a tile (`Character.quest`, rivals: death chance 25%). Treachery: each year 12% of sects with 6+ members have an ambitious (Evil, bold, or 1 in 4) disciple strong enough turn on the master: both get relationship -3, so Battle Arch-Enemy quests follow. Squads: `partyCap()` 12 for a Leader who is neither sect master nor city lord, 4 otherwise; `squadSize()` counts `followerIds` (pruned), companion beasts are never listed; `tryRallyParty` stops at the cap and Leaders rally more widely (2 realms, 1 in 2). Hunt Beast bounties: at most two held per character.

## Names beyond the basics (`Naming`, `Regions`)
Goods: `dish/wine/poison/instrument(level, maker)` (method follows the grade, a cook's signature 22%, vintage wines carry their year, poetic names 30-35%). `petName` renames a beast when it is tamed (cute, mighty by level; the species stays the surname). Sects: 18% `<Surname> Clan/House/Hall`, 18% named after the founding tile (`geoWord`). Events: `festival(season, city)` (stable per city), `tournament(place, year)` (stored in `Area.tournamentName`), `war(a, b)` (symmetric), `event("tide"|"trial", place, year)`, `era(century)` (a chronicle entry every 100 years), `songTitle`. Random streams use `mix64` seeds so near-equal seeds are unrelated. `Regions` floods the torus world's terrain (`BiomeGen.cell`) into regions (ocean, mountain by elevation >= 0.78, one per biome) and 8-connected river chains, names them deterministically from the world seed (`regionOf/riverOf/line/culture`), and gives each region a culture (1 pinyin-like, 2 western-fantasy) used for city names. Regions under 4-8 tiles and beaches have no name.

## Portraits, seals, icons, maps, events, weather (`ArtMore`)
`portrait(Character, w, h)` paints a face from the person (gender, age, trait robe colour, alignment, level aura, kills scar, sect seal; beasts get an animal face), cached by id and look; `seal(sectName, size)` four CJK glyphs from the name hash in a red square (the font is `Theme.CJK`); `itemIcon(Item, size)` a vector icon by item type and weapon kind tinted by grade; `worldMap(w, h, known)` an ink-and-wash map from `BiomeGen` cells with mountains, forests, rivers, region names (`Regions.labels()`, collision-free) and the cities and sects the player knows; `eventArt(kind, seed, w, h)` tide / war / tournament / festival / trial / city / sect / death / reincarnation / legend; `weatherAt(Area)` 0 clear, 1 rain, 2 fog from world seed, month and a 3x3-tile cell (never saved). Used by the character window, tile info, backpack, `Painted map`, the Chronicle window and `PlaceImages` (weather in the picture cache keys).

## Painted places made by people (`ArtCity`)
`ArtGen.Opts` carries `river`, `season`, `weather`, `settlement` (1 town, 2 city, 3 Originis) and `sect`. `ArtCity.capital` paints Originis (distant ranges, rooftops climbing to the great hall, nine- and seven-tier pagodas, crenellated wall with gate towers, lanterns, banners, river with bridge and boats), `skyline` adds houses, pagodas and a wall to a town or city, `sectHall` a stepped temple complex with a mountain gate, `finish` the river, spring flowers, autumn leaves, winter snow and (unused so far) rain and fog. The picture cache key includes all of them.

## Chronicle and player tools (`Chronicle`, `TextTools`, `GuiMore`)
`Chronicle.add(kind, text, ids...)` (thread-safe) records world history; hooks: `Character.dead`, breakthrough, `establishCity/Sect`, `Organization` war/peace, tagged world events (`GameDriver.logEvent`). `Chronicle.deathCauses` counts deaths of cultivators (old age, battle, beasts, arrays/traps, forbidden technique); `Chronicle.yearly()` samples level, wealth (log), friends and kills of players and followed cultivators; `Chronicle.notes` holds bookmarks. All four are in `SaveData` (null in old saves). Terminal commands: `map`, `route`, `time`, `watch`, `journal`, `history`, `note`/`notes`/`unnote`, `dash`, `equip <n>`, `compare <n>`, `techniques`, `help <topic>`. `watch` sets `PlayerControl.watching`; `Notifications.wake` ends it (and pauses the GUI when the category is in `Settings.pauseOn`). GUI: `GuiMore` windows (Backpack, Chronicle, Biography, Notes, World overview, Dashboard tab), hover cards in the log, shortcuts V C J X Y `.` F1.

## Fight reports and the feud penalty (`FightReport`, 2026-10-06)
`Character.battle` wraps `battleCore` in `FightReport.begin/end`; a report exists only when a human (player 1) or a followed NPC (2) is in the fight. The hooks (`odds`, `fleeAttempt`, `forbidden`, `won`, `spared`, `injured`, `loot`, `arrayAttempt`, `partyStart`) fill it while the fight is fought; `finish()` prints one block (header with place and date, who attacked whom, power and win chance, lethal or not and why, what happened step by step, RESULT: YOU WON / YOU LOST / no winner, Fate: slain / spared / injured / escaped, loot and kills) as terminal text, posts a `fight` notice for the human and adds a `fight` line to the Chronicle. A routine beast hunt is one `[Fight] ...` line. `fights [n]` re-prints the last reports. Penalty: a non-lethal loser pays the winner 10% of their total wealth (exact in BigInteger across ss/sc/dc/ij, `FightReport.moveWealth`) when there is bad blood: a negative relationship in either direction, a bounty or revenge quest, or sects at war; strangers' duels and sparring never cost anything; a lethal fight is still all-loot.

## Sect villainy (`SectPolitics`, 2026-10-06)
Called from `Seasonal.seasonal` (bullying, every season) and `Seasonal.diplomacy` (yearly: treachery, murder, theft, poison, framing, plots). Odds scale with alignment (`evil()`: Evil full, Neutral about a third, Good never) and caution. Inner disciples (Evil, less so Neutral) shake down outer disciples for 15% of their stones or a spare piece of gear; Evil elders murder a weaker disciple (3+ levels below) whose gear is worth at least 10% more than theirs (a lethal fight; 40% of murders come to light and the elder is expelled with a bounty, an Evil master may look away from a strong elder); they steal gear from the treasury, poison and frame equals; Evil and Neutral elders hatch a `Plot` (saved in `Organization.plot`) against the master, the plotters strike one after another, the strongest survivor inherits the sect (`installMaster`), the master may expose it first. A human target is told and asked (fight or slip away) before it turns into a fight; a human master is told of whispers and ambushes. `SectPolitics.scale` multiplies every chance (tests), `summary()` returns the counters.

## Spoilage (`Life.spoil`, 2026-10-06)
Once a year every stack of food, wine and poison in the city markets, shop shelves, sect pantries and bags loses a share: food 55%, poison 20%, wine 15% for a plain grade, 12% less per grade above 1, at least 8%, up to twice that for a stack of 150+ portions, at least one portion; a character keeps at most `Life.HOLD_CAP` (200) portions (worst first rot). Measured: market stock under a constant inflow levels off.

## The Palace of the Oldest Order (`Lore`, `LoreSites`, `LoreText`, `GuiLore`, 2026-10-06)
See `Lore Design.md`. `Organization.virtual` sects have `master == null` for ever (`promote()` is a no-op, no wars, `whyNot` asks `Lore.whyNotEnter`); `Lore.newWorld()` (GameDriver, after the start zone exists) founds the Palace at Originis when `Settings.lore`, `Lore.state` (saved in `SaveData.lore`) holds the Convocation, the passes, the Nine Lamps and one `PState` per human body. Hooks: `Area` constructor (`Lore.onTileCreated`) and `runTournament` (`Lore.runConvocation`), the tick (`Lore.worldTick`), the yearly block (`Lore.yearly`), `GameDriver.playerTurn` (`Lore.tick`), `Organization.resign` (`Lore.onResign`), the `lore` command. Quests: only walks to places become quests (waypoints that complete on arrival); the other objectives are in the Journal.


## The endings (`LoreEndings`, 2026-10-07)
See `Lore Design.md`. `Lore.cross` -> `LoreEndings.choose` (the Seam, the Old Man scene `end_oldman`, then `decide`: Stay or Leave; DecisionBroker menus, scripted answers in tests: step through / Stay-Leave / alignment / sub-choice) -> `stayGood/stayEvil/stayNeutral` (a Stay can be undone by `lore leave` -> `LoreEndings.leave` -> `undoStay`) or `soldier` (a lamp-bearer, `LAMP_MAX` = 9 great lamps = `State.wanderers`)/`attack` (`bloodSacrifice`: +5 potential for everyone, stacking in `State.blood`, also applied at birth)/`leaveAlone` (`outside`: 12 random branches) -> `finish` (score, Roll, Hall of Fame line via `RunStats.hallEnding`, `present`: scene + credits). The hidden ending (`usurp`) and the world's last week (`worldEnd`) both end in `gardenEnds`: the Garden goes out, every human who can cross chooses the Outside or the dark (`lastLamp`), then the game saves and quits. NPC seekers choose by alignment (`npcCross`; stayers in `State.stayers`). A soldier is released to the succession dialog through `LoreEndings.releases` / `drainReleases` (called each tick before the reincarnation loop); a leaving body dies with `reincarnating = true` (no death notice) and `needsReincarnation`. `GameDriver.npcTurn` hands a `wanderingImmortal` to `LoreEndings.wanderTurn`; `Character.dead()` ignores them. `Character.blessing` is inherited at birth (`LoreEndings.inheritedBlessing`) and spread to existing descendants (`bless`). `Character.dead(killer)` calls `LoreEndings.onKill` (the dark path's potential). New saved fields are default-safe: `Lore.State.{silenced,palaceGone,seekers,wanderers,footX,footY,firstRead}`, `Lore.PState.{endingId,score,darkPath,secretDone,stewardActive,idleUntil,prints}`, `Character.{blessing,wanderingImmortal}`, `Organization.palace`.

## Terminal (`TermHelp`, `TermHud`, `TermInput`, `TermFormat`, `TermView`, 2026-10-06)
`TermHelp` lists every command by category (`help`, `help <category>`, `help <command>`, `help all`), `TermInput` resolves unambiguous prefixes, aliases and did-you-mean, `TermHud` prints a status block before each prompt (`settings hud off`), `TermFormat.Table` aligns tables, `TermFormat.parseInt` makes number arguments safe. The `switch` in `PlayerCommands.handle` stays the single source of what a command does.

## The window system (`GuiDock`, `GuiQuests`, `GuiDuties`, `GuiCards`, `GuiHub`, `GuiHelp`, 2026-10-06)
Move tab: walk (8 directions, Move to Selected Tile, Go to top quest, Route, Watch), Do here (meditate, hunt, forage, mine, craft, rest, sell, buy gear, pills, eat, drink, equip), Quests (Do top quest, Go to next alert, Quest Log), People (Talk, Spar, People here), command box and Help. Me tab: Character / People / Places and work / System groups. Auto tab: what the character is doing + Take control back, mode buttons, chores (Duties, Policies), scripts and quests. World tab: lists, the world, the game. There is ONE quest window (`GuiQuests`, the "Quest Log"); `GuiHub.header` puts a one-line purpose and a "?" (`GuiHelp.helpButton`) on every window, opening its Help page. The Help (`GuiHelp`, text in `HelpPages`/`HelpPagesMore` compiled by `HelpContent`, screenshots in `assets/help` made by `tools/HelpShots.java`) has a tree, search, links and the guided tour.

## Performance notes (2026-10-06)
See `Performance Report.md`. `Map.heavenExists` (refreshed once a tick) gates the Heaven Treasure scan; Seek Potential looks one turn in six; `tryRallyParty` walks the friend list instead of copying the tile; `Perf` (`-Dtdf.perf=true`) times the tick phases, `-Dtdf.seed`, `-Dtdf.threads` and `-Dtdf.spread` make runs comparable.


## Relationship System

### Values
| Value | Label |
|---|---|
| 3 | Close Friend / Spouse candidate |
| 2 | Good Friend |
| 1 | Friend |
| 0 | Neutral (relationship removed) |
| -1 | Dislike |
| -2 | Rival |
| -3 | Arch Enemy |

### Change Utility (`changeRelationshipUtil()`)
- Positive/negative thresholds scale based on affiliation match, family relation, level gap, and current relationship score.
- Same affiliation: 250K positive / 50K negative (out of 1,000,000).
- Opposite (Good/Evil): 50K positive / 250K negative.
- Siblings share the same mother or father: double positive, half negative.

### Marriage (`checkRelationship()`)
- Triggers when relationship reaches 3 and both are age ≥18, single, opposite gender, not related.
- Female takes male's last name.

### Births (`checkBirth()`)
- Triggers every interaction between spouses.
- One child created per event (40-week gestation via `time = -40`).
- Child inherits: techniques from same-gender parent, potential ≥ average of parents', affiliation from combined parents, sect membership.

---

## Economy & Organization

### Spirit Stones (ss)
- Generated by: killing beasts, city population tax (cityOwner.ss += population/100 once per year), Merchant shop profit, technique sales to libraries.
- Required by: `cityLeadership()` (Major Lvl ≥ 3), `sectLeadership()` (Major Lvl ≥ 2); city commerce (Go Sell, Restock, Sell Stock, merchant shops) works in ownerless cities.

### Item Pricing (`itemPrice()`)
- Herb/Metal: `lvl^2.5 × 10`
- Pills: `lvl^2.5 × 100`
- Weapons/Clothing: `lvl^2.5 × 1000`
- Arrays: `lvl^2.5 × 500`

### City / Sect Creation
- City: Leader at profLvl=1, ss ≥ 1M, 1 in 100 chance → sets `area.city = true`, initializes area inventory bags, sets `area.cityOwner`.
- Sect: Leader at profLvl=50+, ss ≥ 1B, area has city OR 1 in 100 chance → creates `Organization`.

### Shop (Merchant trait)
- `Shop` class with `buyRate=0.55`, `sellRate=1.50`, `cityBuyRate=0.30`.
- City level (`Area.cityLevel()`, 1–10) scales with `cityTreasury` like item prices: level = floor((treasury/10)^0.4 / 10). A city hosts `3 × level` shops (`maxShopSlots()`). A shop's level = min(city level, owner's major realm), shown in the lists; a shop takes **any** item its owner can pay for (`Shop.canStock()` has no level cap since 2026-10-03). Auctions no longer draw on the city pool. Shops whose merchant died are removed yearly.
- Population control: after year 10, a tile over capacity (`GameDriver.CROWD_CAPACITY` = 2% of the *starting* population × 961/range², floor 20 — 2000 at 100k on the 31×31 map, 60 in the 3,000-person test world; cities 4×; never based on the current population; Originis is exempt) becomes a **crowd hotspot** (`Area.crowdCheck`, +15 level surge, `crowdHotspot`/`hotspot` flags). Every tick beasts ambush 2% of the excess (max 40) — level-blind, `ambusher` beasts kill any cultivator they defeat; array-protected tiles are exempt. The hotspot ends when the tile is under 80% of capacity. Cultivators there get a "Disperse" quest (10%/turn) to a nearby tile under half capacity. Births are never reduced. At 200,000 living cultivators (`GameDriver` yearly) an epoch purge marks 10% of each major level (levels with < 10 cultivators spared); victims die 3000 per tick.
- City population (`Area.age()`): +0.1%/tick (min +1) toward `populationCap()` = 100k × cityLevel² (max 100M); yearly tax = population / 100 (half owner, half `cityTreasury`). Counters (monsters 50M, herbs/metal 1B, population) are capped so they can never wrap; `convertCurrency()` converts by division.
- Sect raids: at most one "Sect Raid" per sect per 13 weeks (`Organization.nextRaidWeek`).
- Saving: see "Saves (`SaveManager`)" above; `SaveData.yearlyDone` stops a loaded save from repeating its year's events.
- `buyFromSeller()` — buys items from sellers at 55% of `itemPrice`.
- `sellToBuyer()` — sells to buyers at 150% of `itemPrice`.
- `restockFromCity()` — buys from city pool at 30% of `itemPrice`.
- Merchant arbitrage: Buy Gear quest (travel to city A for cheap goods) → Sell Stock quest (travel to city B to sell at profit).

### Library
- `Library(Area)` — city library; buy techniques with ss (`potential^2 × 1000`). Sells (`Library.sellTechnique`, `Character.sellBestTechnique`): the seller is paid **20%** (`100 × potential²`), **loses** the technique (so copies do not multiply), and the book replaces the shelf's book of the same (type, potential) (`shelve`). Techniques of potential < 4 (`MIN_SELL_POTENTIAL`) and the four equipped ones are never sold; `compact()` (yearly) cleans old shelves. The GUI library table sorts on every column and filters (name, type, potential range, max price); `library [limit] [words] type= potential>= price<= sort=` in the terminal (`ListQuery`).
- `Library(Organization)` — sect library; access costs sectPoints.
- `learnTechnique()`: learned copy starts at `technique.lvl / 5` mastery.
- `learnSectTechnique()`: learned copy starts at `technique.lvl / 3` mastery.

### Organization
- Tracks founder, master, members list, location, treasury.
- `resign()` triggers `promote()` — next member in list becomes master.
- `ArrayList<Item> treasury` — stores donated items from all members.
- `sectLvl()` — `sum(herb+metal qty×lvl) / 10000`, capped at 100. Used as cultivation bonus multiplier.
- `donationPoints(Item, qty)` / `buyingCost(Item, qty)` — proportional: herbs/metals: `qty×lvl/1000`; pills: `qty×lvl×2`; gear: `item.lvl`.
- `postMissionsYearly()` — assigns Forage/Mine/Hunt missions to members. Hunt missions require `sectLvl() >= 5`.
- Master promotes Outer Disciples to Inner Disciples (5%/turn, level threshold check) and Inner Disciples to Elders.

---

## World Generation

### Map
- `Map.tiles[31][31]` — grid initialized with `Originis` at center `[15][15]`.
- New tiles generated lazily as characters move into null slots.
- `createdAreas` list tracks all instantiated areas.

### Biome Transitions
- 1 in 10 chance of biome change on area creation.
- Transition rules: Desert↔Savanna, Forest↔Taiga, Tundra→Frozen variants, Ocean→Beach at land edges.
- Area level = level of the character who first explored it (`currentCharacter.lvl`).

### Area Aging (`area.age()`)
- All resources/monsters/population multiply by 1.01 per tick.
- 1 in 10 population tick → spawn new Character (possibly joining sect).
- `Area.bounties` — `ArrayList<Quest>` for posted bounties on this area.

### Dungeons
| Type | Effect |
|---|---|
| LOST_RUINS | Treasure chest + random technique |
| BEAST_LAIR | 3 forced beast battles |
| MARTIAL_TRIAL | tempLvlCheck + technique upgrade if won |
| HAUNTED_GROUNDS | 4% potential gain; 5 extra cultivate() calls |
| CELESTIAL_TRIAL | Temporary 10-tick event; 3 boss beast battles, 20 cultivate() calls, 25% +1 potential, 10% technique grant |

### Beast Tides
- Checked once per year in GameDriver yearly loop.
- Condition: `area.dungeonType == BEAST_LAIR && area.monsters > area.lvl * 2000`.
- Overflow (`area.monsters - area.lvl * 2000`) spread evenly to 4 cardinal-neighbor tiles.
- If a neighbor is a city, population decreases by up to 10%.

### Heaven-Sent Treasure (`Treasure.java`)
- Fires **every year** (`Treasure.yearly`, at most 12 unclaimed): one treasure of a random kind (technique, instrument, poison, Potential Pill, regular pill, clothes, weapon, array) on a random wilderness tile (`area.heavenlyTechnique` or `area.heavenItem`). Level scales with the strongest living realm; items carry `Item.heavenSent` and the Heavens' Blessing (+5).
- All characters within 20 tiles have the area added to `knownLocations` and receive a priority-70 "Heaven Treasure" target quest.
- First character to arrive claims it (`Treasure.claim`): techniques are learned, pills/arrays/gear go to the right bag. Heaven-Sent items are **never sold to a shop**: selling in a city with an auction house consigns them (`Auctions.consign`).

### Auctions (`Auctions.java`)
- Cities of 500+ have an auction block (`Area.auctionBag`). Only luxuries are put up: Heaven-Sent items, **Potential Pills** (never on shelves any more; `Character.sell` consigns them once the seller's potential is 100) and the city's vintage food/wine/poison (`Life.auctionLots`, now and then).
- `Auctions.tick(area)` every week: an auction is held when 5 lots wait, or 6 weeks after the first arrived (never the same week). Up to 5 lots, best first. Second-price: the winner pays the runner-up's bid or the reserve (`reserve`: heaven 20× / other 5× `itemPrice` × quantity; vintage goods the old curve); a MANUAL human in the city is asked (`DecisionBroker.askNumber`), NPCs and AUTO characters bid only for what they `wants` (the Potential Pill below potential 100, the next-realm pill, better gear, arrays for masters/lords, anything for the very rich). The consignor gets 90%, the city lord/treasury the rest. Unsold lots wait.
- Blood: a non-alchemist with 1,000+ Blood gets a "Refine Blood" quest (UPKEEP duty) to an alchemist, who makes one Potential Pill per 1,000 Blood (up to `professionLvl/10 + 1`) for a fee equal to the pill's price (`Character.refineBloodWith`).

### Celestial Trials (Spawn)
- Fires once per year.
- Requires a wilderness area with `lvl >= 20` and no existing dungeon.
- Sets `area.dungeonType = CELESTIAL_TRIAL`, `area.celestialTrialTimer = 10`.
- Broadcasts the area to characters within 15 tiles (adds to `knownLocations`).
- Timer counts down each tick; at 0, `dungeonType` cleared and closure message printed.

---

## Known Bugs & Issues

### Active
None as of 2026-09-19.

### Design Notes
- `movementTechnique2` / `martialTechnique2` secondary slots are temporary holders used during `equip()` to merge a newly-learned technique into the primary slot. They are nulled after processing; they do not need to be included in `tempLvlCheck()` separately since `equip()` is always called first.
- Technique rarity is implicit via `potential` value — high-potential techniques are rarer and stronger by construction. No separate rarity tier system is needed.

---

## Implemented Features Summary
See [Game Outline.md](Game Outline.md) for the full checklist.
