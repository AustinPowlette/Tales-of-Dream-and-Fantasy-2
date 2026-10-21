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
| `RelationshipList.java` | ArrayList wrapper for relationships; capped at `tdf.relCap` (default 100) per person: the oldest acquaintance with value -1/0/+1 (not refused, never talked to, not a human) is dropped first. Item names are interned (1.9 M items shared ~13 K strings) |
| `Dictionary.java` | Static name/biome/creature data arrays (regenerated: tiered beasts, grades, 1,300+ place/beast words) |
| `Naming.java` | Name engine: people, beasts (by tier), places, cities, sects, dungeons, shops, libraries, techniques, titles, item grades; unique-name registries |
| `Starts.java` / `StartMenu.java` | Character starts (protagonist / standard / custom / ten stories), difficulty, NPC prehistory, story objective engine; the start dialog / prompts |
| `Settlements.java` | Yearly founding of cities and sects in proportion to the number of cultivators |
| `Reincarnation.java` | The rope (level cap of a potential), the "Reincarnate" quest, rebirth into the family, lineage |
| `Taming.java` | Beast taming: cap, enlist, follow, release, bonus |
| `Life.java` | Food, wine, poison, music, the five life-arts traits, effects (`Fx`) |
| `Ambient.java` | Music engines (built-in synth, SoundFont), moods, instruments; the old random composer as fallback |
| `Conductor.java`, `Score.java`, `SongBook.java`, `SongTale/Road/Market.java` | The songs (text scores), the parser and the player (layers, fades, rests) |
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
          ├─ Phase 2B FIRST, NPCs: npcTurn() = party logic → quest() (act on the tile you stand in) → then, if no quest planned a step, one wander step.
          │    A step is only PLANNED (`Character.planning`, `pendTo`): nobody leaves or enters a tile while the turns run. One thread, or (Settings.parallelNpcs,
          │    experimental) the tiles shared out over the cores. The births of the phase are merged into the list (`Map.mergeBirths`) before the next phase.
          ├─ Phase 2A, human/tracked characters (serial), who now know where every NPC is going this week:
          │    MANUAL → prompt for commands (info/setup are free) → quest() runs only duty-enabled quests + announces alerts
          │    AUTO   → npcTurn() (same as an NPC; decisions answered by policy; its step is taken at once)
          │    HYBRID → PlayerScript.tick() → quest() (duties + alerts)
          │    player == 2 (tracked NPC) → npcTurn()
          ├─ The planned steps are taken together (`GameDriver.applyMoves`), then party riders, children follow their mothers, companions follow their masters
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
Rates are read from the code (`Character(Area)`, the trait picker at about line 262, for the starting population and city/town spawns; `Character(Character mother)`, about line 440, for births; `Life.rollTrait` afterwards). A die of 1,000,000 is rolled; the nine common traits (Blacksmith to Runner) are rolled at 10% each and then `Life.rollTrait` turns 8% of each of them into one of the five life-arts traits (1.6% apiece), so a common trait ends at 9.2% and a life-arts trait at 1.44% of everyone.

| Trait | Starting population | Births | Effect |
|---|---|---|---|
| Cheater | 0.0001% (1 in 1,000,000) | 0.0001% | +5 tempLvl in combat; the only trait that may use cheats in a running game |
| Lucky | 1.0% | 0.5% | +45 win chance; loots dead as profession |
| Ghost | 3.0% | 1.0% | On death (non-Beast killer): enters ghost form, seeks a city, reborns as a new character; items dropped, sect resigned |
| IronBody | never | 1.0% | `+majorLvl/2+1` to tempLvl in combat (natural armor) |
| SpiritVein | never | 1.0% | 1.5x cultivation speed |
| HeavenlyRoot | never | 0.5% | 2x technique upgrade chance on kill (1 in 2 vs 1 in 5) |
| Leader | 3.0% | 3.0% | Creates cities (profLvl=1) and sects (profLvl=50+) |
| Innovative | 3.0% | 3.0% | Fights give Inspiration; a hundred make a new technique; sells techniques to city libraries (measured: about 100 of 3,000 people, but libraries hold their first books only in game year 4-5) |
| Blacksmith | 9.2% | 9.2% | Smithing profession |
| Alchemist | 9.2% | 9.2% | Alchemy profession; forages if missing materials |
| Scribe | 9.2% | 9.2% | Array crafting |
| Merchant | 9.2% | 9.2% | Runs a Shop; buys from sellers, sells to buyers, arbitrages between cities |
| Gatherer | 9.2% | 9.2% | Foraging profession |
| Miner | 9.2% | 9.2% | Mining profession |
| Friendly | 9.2% | 9.2% | Social interaction profession |
| Tailor | 9.2% | 9.2% | Clothing crafting |
| Runner | 9.2% | 9.2% | Guaranteed flee if opponent is <=10 levels higher |
| Tamer | 1.44% | 1.44% | Beast taming (`Taming`) |
| Poisoner | 1.44% | 1.44% | Brews and uses poison (`Life`) |
| Drunken | 1.44% | 1.44% | Wine (`Life`) |
| Chef | 1.44% | 1.44% | Food (`Life`) |
| Musician | 1.44% | 1.44% | Instruments, concerts (`Life`, `Concerts`) |
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
- Triggered from `winCheck()` when an Evil character or Beast kills someone at an array-free city or sect area (Originis is protected): 1 in 250 for a city, 1 in 100 for a sect (`Settlements.RAZE_CITY_ODDS/RAZE_SECT_ODDS`), and only while `Settlements.mayDestroy` allows it (the world has more than 25 cities / more than 25 sects; 2026-10-07, it was 1 in 5).
- The tile's old name comes back (`Area.restoreName`), as it does when a dungeon is cleared.
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
- Returns 1 when the destination tile is reached. For a character whose steps are planned (an NPC, or AUTO) it returns 1 only when it already stands there at the start of the turn: the arrival is noticed the turn after the last step, and the quest's work is done then (no turn continues after a step).
- Walkers are met, not chased to where they were: `interceptAim` reads the exact planned step of an NPC when a person who plays acts (`GameDriver.movesPlanned`, `Character.turnT`); a walker too far ahead to be caught from behind is met on his way (`leadAim`: the first tile on his way the chaser reaches no later than he does) or at the place he is heading for.
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
The game thread takes `World.lock` (fair `ReentrantLock`) at start-up and gives it up whenever it waits: `GameDriver.readInput/readInt/readLine`, the pause/tick-delay sleep, `edtWait` (any Swing dialog it raises), the save-thread join, and the end-of-tick `World.handoff(refreshNow, 250)`. Readers (`World.read(ms, r)`, `World.readWait(ms, r)`) `tryLock` with a timeout and skip the refresh when the game is mid-tick, so the EDT never blocks indefinitely and the game never waits for the GUI (a waiting reader only delays the game by the time it holds the lock). `MapPanel` paints into an off-screen image under `World.read(30ms)` and falls back to the last finished frame. All writes to game state happen on the game thread: GUI buttons only enqueue commands (`MapViewer.queueInput`). `GameDriver.frozenNpcs` freezes NPC/monster turns for deterministic tests. Since the NPCs act before the people who play in a tick, a probe that freezes them sets it before the first tick (a free NPC could ask the character a question before the probe is ready). With `Settings.parallelNpcs` the NPC phase uses the `Map.tickPool` threads: see "Planned moves, Go There and parallel NPC turns".

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
Potential decides how far a cultivator climbs: each major-realm breakthrough gate (`Character.GATE_POTENTIAL`) needs potential 20 / 37 / 52 / 65 / 76 / 85 / 92 / 97 / 99 / 100 or it almost always fails (see `Character.cultivate`; the tenth gate, True Immortal, needs 100 AND a 1 in 5 roll). **Gaining potential** (`Character.givePotential(pills)`): the band below each gate costs `GATE_PILLS` = 5, 7, 9, 11, 13, 15, 18, 22, 26, 30 Potential Pills (156 from 0 to 100), so one pill is worth width / cost points: 4.0, 2.4, 1.7, 1.2, 0.85, 0.60, 0.39, 0.23, 0.08, 0.03; the fraction is kept in `Character.potentialProgress`. Every chance gain (pill, 1% breakthrough insight, tribulation pass, fortune, Haunted Grounds, Celestial Trial, Find Parent's Body, carnage, Forbidden Technique, pilgrimage, Monster milestone) gives one pill's worth and the epoch half a pill; exact rewards (start, chapters, Lore sites, reincarnation +5, soldier blessing, Usurper, edits) set the number directly. `Reincarnation.cap(p)` = 10 * (gates passed) + 9 = the last level reachable (9, 19, 29, ... 89; 99 at potential 99 and 100). `Life.upkeep` (from `quest()`) posts the spot quest "Reincarnate" (priority 95, an alert for humans, in `DO_SPOT`) once `majorLvl >= 3` (Soul Wandering) and `lvl >= cap(potential)` and potential < 100. `perform` keeps the SAME character and gives a second youth: age 18 (`GameDriver.START_AGE`), level 1, Major 0, cultivation 0, `maxAge` 100 and `totalAgeGained` 100 (the realms' lifespan is earned again), `inhibitor` back to 1 (failed breakthroughs forgotten), conditions cleared, potential +5; name, family, sect, relationships, wealth, items and techniques stay; `pastLives` / `lives` / `soulId` record the earlier lives (`lineage`, Family window "Past lives"; the tree marks "reborn xN"); `RunStats` closes the life and opens a new one. Companions beyond what a Major Lvl 0 cultivator may keep (1) wait for the soul (see below). Brutal changes nothing. Measured: from potential 52 the whole climb to 100 takes about 10 lives and about 3,900 years (a perfect potential-100 cultivator needs about 2,600). Death-time body takeover (`handleReincarnation`) is unchanged and titled "After death"; Brutal still ends the life on death.

## Companions that remember
When a character is reborn (`Reincarnation.perform`) the companions beyond the new cap (Major Lvl 0 + 1) are not freed: `Taming.waitFor` sets `waitingSoul` (they keep their tile and have no turn, like tamed ones) and the character gets `lostBeastIds` and `soulId`; a "Find Lost Beast" Area quest (priority 80 for a Tamer, 50 otherwise) is posted whenever there is room for another companion (checked every 26 weeks and at once if there is room). `Taming.reunite` (run each turn from `Life.upkeep` and by the quest) brings back the beasts standing in the child's tile, up to the cap (Major Lvl + 1), and says so; beasts that die while waiting are forgotten.

## Beast taming (`Taming`)
Only a Beast Tamer (trait) can tame. Cap = Major Lvl + 1 companions. `tame` uses one beast of the tile (`monsters--`, a new beast `new Character(area, master)`); too strong (level more than 5 above you) = refused; chance 35 + 3 * (your level - its level) (+30 Tamer, +10 instrument, +10 Friendly), 5-95; failure: half the time it attacks. A tamed beast gets an id (`GameDriver.UID++`), is added through `Map.addTemp` (population is pre-decremented like awakened monsters), has `masterId`/`partyLeaderId`, is never listed in a tile, has no turn (`GameDriver` skips `masterId >= 0` in the NPC pass) and is moved to its master's tile after all turns (`Taming.follow`). It cultivates like every character (so it levels and breaks through), adds `min(its lvl, master lvl) / 10` to the master's fighting level (`tempLvlCheck`), is freed when the master dies/reincarnates (`onMasterGone`) and cleared from the list when it dies (`onBeastDeath`; a beast freed in the tick it was tamed stays in its list slot until the end-of-tick merge so id == index holds). NPCs try now and then (`Life.upkeep`, 1 in 600 per turn per eligible NPC on a tile with beasts; Tamers on every Profession quest).

## Food, wine, poison, music (`Life`)
Each art belongs to its trait: only a Chef cooks, a Drunken brews, a Poisoner mixes poison and uses it (`poison`, `coat`), a Musician makes instruments and plays, a Tamer tames (anyone can eat, drink and be poisoned or inspired). Goods (`Character.goods`; `Item.ItemType` Food / Wine / Poison, stackable) are made from herbs, 50 per unit (`make ...`; `professionLvl / 10 + 1` units, herbs up to `professionLvl / 10 + 2`). Level = herb level 1-10 (like pills). Economy: a city has a goods market (`Area.goodsBag`, up to 600 portions): `sell goods|food|wine|poison [qty]` (and the automatic sale of a profession or the "Go Sell" quest, now also triggered by 20+ portions) pays half the unit price (unit price = a quarter of the pill price; poison costs three times as much and is sold to Poisoners only) and puts the portions on the shelves; `market` lists them and `buy market <#> [qty]` buys (the city lord gets the money); NPCs buy meals when unfed, wine now and then (the Drunken often), poison when a Poisoner has none, and a better instrument for a Musician from the city stock (`Life.npcShop`); food spoils 50% a year, wine 10%, poison 5% (`Life.marketYearly`); each festival season Chefs, brewers and Musicians within 8 tiles get a "Festival Trade" quest (sells at double, the Musician plays). Instruments are never sold by their Musician (`Character.sell` keeps the best). Conditions live in `Character.fx` (`Life.Fx`, null for most): Food = +15% cultivation x level ratio for 13 weeks (26 and x1.5 for a Chef), cures poison of equal or lower level; Wine = drunk 4 weeks: +25% cultivation, -1 fighting level (Drunken: +2 + level/4, x1.5 bonus, 6 weeks, a free cultivation tick one week in eight); Music = an Instrument in the backpack; `play` = extra cultivation ticks (2 for a Musician), everyone present gets inspired (+10% cultivation, 4 weeks, 8 for a Musician) and may like you more, a city pays tips; Poison = `poison <person>` or a coated blade (`coat`: 3 fights): chance 55 + 15 * (poison level - victim's resistance), resistance = major level + 1 (+2 Poisoner, +2 Iron Body, +1 fed); poisoned: cultivation x0.6, fighting level - (1 + level/3) for 13 weeks, then a lethal roll (20% per level above resistance, max 60%) if the poison outranks the victim; the killer is credited. The new traits replace a common trait 1.6% each (`Life.rollTrait`). City-born cultivators (`Area.age`) now arrive at 18 with the level their potential earned since 8 (`Starts.prehistoryOne`, like the first generation); a Ghost's city rebirth is also at 18. NPCs eat when not fed and drink now and then; a human in MANUAL mode does neither on their own. `Settings.autoEquipManual` (default off) stops the automatic gear swap for a human in MANUAL mode.

## Music: songs, conductor, engines (`Score`, `SongBook`, `Song*`, `Conductor`, `Ambient`, `MidiOut`)
Since 2026-10-10 the game plays songs written note by note (seven, forty-two more on 2026-10-11 and twenty-one more the same day: 70 in all, open 15, city 13, danger 9, night 9, war 7, sect 9, capital 8; eight of the older ones were given a drum-kit groove afterwards) instead of random pieces (the random composer, `Ambient.Composer`, is kept only as a fallback when no song parses).

**Songs** (`SongBook.all()`; each is a `String[]` text score in its own class, parsed by `Score.parse`; a song with a problem is left out and listed in `SongBook.problems`):
| Song | Class | Key, tempo | Plays for (mood:weight) | Character |
|---|---|---|---|---|
| A Tale Begins | `SongTale` | D major, 4/4, 72 | capital 6:5, sect 5:1, open 0:1, night 3:1 | the main theme: one hook (A B D), a piano statement, an answer on erhu/flute/cello, a yearning B section, a climax with the melody in octaves and strings, a quiet close |
| The Long Road | `SongRoad` | E minor, 6/8, 52 | open 0:4, night 3:1, city 1:1 | koto rolls, a dizi hook, an erhu answer leaning on B7, a cello line, a soft heartbeat; its `drive` layer (taiko + low cello) is used only when a song is pinned in danger or war (`music play`) |
| Market of a Thousand Lamps | `SongMarket` | G major, 4/4, 108 | city 1:5, capital 6:1 | harp bass bounce, off-beat pipa chords, a short singable dizi hook, an erhu answer, a wood block |
| Snow on the Lantern Road | `SongNight` | A minor, 3/4, 56 | night and winter 3:5 | a slow waltz: rocking harp, a soft falling piano line, flute later, a warm pad, chimes, a turn to E major at each phrase end |
| Where the Beasts Wait | `SongDanger` | C minor, 4/4, 76 | danger 2:5 | galloping low cello, taiko, trembling strings, a horn that cannot finish its phrase, a high erhu, a choir at the climax |
| March of the Broken Banners | `SongWar` | G minor, 4/4, 108 | war 4:5 | horn fanfare (short-short-long), chugging cello, marching taiko, string stabs, an erhu lament, full band last |
| Morning Bell, Mountain Hall | `SongSect` | F major, 4/4, 58 | sect 5:5 | tolling bell, a dizi on long notes, slow guqin arpeggios, a cello drone, a hushed choir, a wood block keeping the hour |
| Lanterns on the Reed River | `SongReeds` | D major, 4/4, 64 | open 0:4, night 3:1, city 1:1 | a second open-country song: a flowing pluck in eighths, a dizi hook (F# E D, A), an erhu answer climbing to the dominant, a bright turn through G and E minor; the same `drive` layer as the Road |
| Tea at Dusk | `SongTeahouse` | C major, 4/4, 92 | city 1:5, capital 6:1, open 0:1 | a second city song, warmer and slower than the market: a pipa plays the tune, a dizi answers, a walking harp bass, a light wood block, the middle leans into A minor |
| Moon on the Frozen River | `SongMoon` | D minor, 4/4, 46 | night 3:5 (`layers: fixed`) | a second night song: a rocking harp, a cello pedal, long falling erhu phrases, a sparse high piano answer, a turn to F major, one bell |
| The Nine Lamps | `SongLamps` | B-flat major, 3/4, 66 | capital 6:5, sect 5:1 | a stately procession: horn and cello carry the tune, an erhu answers above, strings and a hushed choir, a stepping taiko, a bell; the last time the tune is doubled an octave up |
| What the Lantern Does Not Reach | `SongDeep` | F-sharp minor, 5/4 (3+2), 84 | danger 2:5 (`layers: fixed`) | a second danger song: a stumbling low cello figure, a taiko heartbeat with a hole in it, trembling strings, a horn leaning on the flat second, an erhu climbing in the middle, a choir under the last statement |
| Ashes Before the Gate | `SongGate` | D minor, 6/8, 84 | war 4:5 (`layers: fixed`) | a second war song in two long beats a bar: a long-short-long horn call, a relentless cello chug, string stabs, a pipa, an erhu answer, B and A repeated and the last A doubled an octave up |
| Incense at the Ninth Hour | `SongIncense` | C major, 4/4, 52 | sect 5:5 (`layers: fixed`) | a second sect song: a dizi on long pentatonic notes, a rising guqin figure, a cello drone, an erhu on the relative minor in the middle, a hushed choir, a wood block keeping the hour |
| Wind Across the Barley | `SongBarley` | A major (flat seventh), 4/4, 112 | open 0:4, city 1:1 | a brisk walking song: a dizi hook that skips to the tonic, a pipa chopping off-beats, a bouncing harp bass, an erhu answer, wooden boots |
| Cranes Over the Marsh | `SongCranes` | E Dorian, 7/8 (2+2+3), 120 | open 0:4, night 3:1 | a limping pluck that lands on the long beat, a flute in short rising phrases, an erhu a sixth above, a low heartbeat |
| Beyond the Last Pass | `SongPass` | G Mixolydian, 4/4, 58 | open 0:3, night 3:1, sect 5:1 | vast and alone: a horn calls G D F, a flute answers an octave up, a harp drone, a wide choir |
| Lantern Festival Night | `SongFestival` | E-flat major, 4/4, 120 | city 1:4, capital 6:1 | a holiday: a pipa running the chord in eighths, a dizi on long notes in the middle, bells everywhere, taiko like feet on cobbles |
| The Moneychangers' Waltz | `SongMoneychangers` | B minor to D major, 3/4, 100 | city 1:4, capital 6:1 | an oom-pah waltz: erhu tune up the triad, a pipa on two and three, a sudden turn to D, a wood tick for coins |
| Rain on the Tile Roofs | `SongRain` | A minor pentatonic, 4/4, 72 | city 1:3, night 3:2, open 0:1 | a piano dripping a pentatonic tune, a harp in eighths like a gutter, random chimes, no drums |
| Eyes in the Cave | `SongCave` | B-flat minor, 4/4, 60 | danger 2:5 (fixed) | danger that waits: a creeping cello with a gap, a double-hit heartbeat that stops, one bell dripping, a horn never above a fifth |
| Thunder in the Ravine | `SongRavine` | C-sharp minor, 3/4, 130 | danger 2:5 (fixed) | a chase: a horn hammering the triad, taiko on every beat, a pounding cello, a screaming erhu, the i VI VII V turn |
| The Sleeper Stirs | `SongSleeper` | A Phrygian, 4/4, 44 | danger 2:5 (fixed) | something enormous turning over: a cello drone, a breathing choir, one held erhu note, a horn walking down in fifths, one far taiko hit |
| Fireflies by the Stone Bridge | `SongFireflies` | E-flat major, 12/8, 46 | night 3:5 (fixed) | a lullaby: a piano rocking up the triad, a harp rolling in sixes, a flute on long notes in the middle |
| The Watchman's Lamp | `SongWatchman` | G Dorian, 4/4, 54 | night 3:5 (fixed) | a vigil: one flute, a sparse guqin, a cello drone, a wood block striking the hour each bar, an erhu low then climbing |
| Stars Over the Salt Flats | `SongSalt` | E Lydian, 3/4, 50 | night 3:5 (fixed) | open space: a few high piano notes, held pad and choir, sparkling chimes, a harp in threes |
| The Red Tide Rises | `SongTide` | E minor, 4/4, 124 | war 4:5 (fixed) | a rush: a repeating three-note horn riff that leaps, a chugging cello, taiko double hit, a raking pipa, a shrieking erhu |
| Drums of the Eastern Gate | `SongEastGate` | A minor, 12/8, 80 | war 4:5 (fixed) | a ritual: a taiko triplet, a low horn rising through the triad, a chanting choir, a pipa in triplets |
| The Last Charge | `SongCharge` | F minor, 4/4, 88 | war 4:5 (fixed) | a sacrifice in half-time: a horn singing above a bowed cello, a taiko only on beat three, long choir chords, the tune doubled an octave up at the end |
| Bamboo and Temple Rain | `SongBamboo` | A major pentatonic, 3/4, 56 | sect 5:5 (fixed) | a slow waltz: a flute on long notes, a guqin rocking in threes, a temple bell, an erhu rising in the middle |
| Disciples at Dawn | `SongDawn` | G major, 6/8, 54 | sect 5:5 (fixed) | the first light: a rolling harp, a flute climbing the triad, a cello walking down the scale, an erhu answer |
| The Forbidden Scripture | `SongScripture` | C Dorian, 4/4, 50 | sect 5:5 (fixed) | secret reading: a lone pipa, a whispering choir, a cello drone, an erhu as a second reader |
| Gates of Originis | `SongGates` | E major, 4/4, 84 | capital 6:5, sect 5:1 | a fanfare: horns on E B E, strings answering, a swelling choir, a gate-closing taiko, the erhu an octave above at the end |
| The Voice in the Empty Seat | `SongSeat` | F minor / A-flat major, 4/4, 60 | capital 6:4, sect 5:1, night 3:1 | a question: a piano saying a few notes and waiting, a hushed choir, chords that never decide major or minor, no drums |
| Noon at the Palace of a Thousand Windows | `SongNoon` | A major, 6/8, 70 | capital 6:4, city 1:1 | full sun: a flute tune climbing the chord, harps cascading in sixes, an erhu answer, a pipa chop |
| Caravan Bells at Noon | `SongCaravan` | C Mixolydian, 4/4, 100, swing 0.12 | open 0:4, city 1:1 | a marimba tune that skips up the chord, a walking upright bass, a yangqin chopping off-beats, congas, a shaker, a kick and a rim click |
| Kites over the Plateau | `SongKites` | F Lydian, 9/8, 66 | open 0:4, night 3:1 | an xiao tune, a yangqin rolling nines, a sheng pad, a frame drum on one and three, a shaker in threes |
| Night Market Drums | `SongNightMarket` | E minor pentatonic, 4/4, 116, swing 0.14 | city 1:4, capital 6:1 | the full kit: a syncopated kick, a ghost-note backbeat, sixteenth hats, congas, shaker, tresillo clave; a suona on the tune, a yangqin answering |
| The Scholar's Garden | `SongGarden` | A-flat major, 6/8, 54 | city 1:3, capital 6:1, sect 5:1 | a marimba tune, a rolling harp, an xiao answer, a soft shaker in triplets, a conga on the beat, one block tap |
| Bones Underfoot | `SongBones` | E Phrygian, 4/4, 72 | danger 2:5 (fixed) | an xun wailing long notes, sheng and choir chords, a cello drone, a stumbling timpani, floor toms, a rim tick, a gong at each phrase, a suona in the middle |
| The Hollow Court Drums | `SongHollowDrums` | D Phrygian, 6/4, 84 | danger 2:5 (fixed) | a horn walking the flat second, a lopsided timpani, toms, a muffled snare, a gong each bar of the middle, a suona shriek |
| Lamplight on the Water | `SongLamplight` | D-flat major, 4/4, 58 | night 3:5 (fixed) | a yangqin singing slow phrases, a rocking harp, an upright bass, a barely played heartbeat kick, a brushed shaker, one block tap, an xun in the middle |
| The Moon Does Not Hurry | `SongSlowMoon` | B Dorian, 3/4, 46 | night 3:5 (fixed) | an xiao on long phrases, sheng and choir, a swaying harp, a marimba middle, a frame drum once a bar, a shaker whisper |
| Iron Rain | `SongIronRain` | G-sharp minor, 4/4, 140 | war 4:5 (fixed) | a suona riff, a horn answer, a cello pounding eighths, a kick in eighths, a backbeat snare, sixteenth hats, a timpani, crashes, a snare roll into the last statement |
| The Banner Bearers' March | `SongBanners` | E-flat minor, 2/4, 104 | war 4:5 (fixed) | a horn tune of short-short-long figures, an upright bass stepping like a tuba, kick and snare march, timpani, strings stabs, crashes, a suona over a choir in the middle |
| Cloud Terrace Sutra | `SongSutra` | B major, 4/4, 54 | sect 5:5 (fixed) | an xun on long phrases, sheng and choir, a cello drone, a wooden fish (a block on every beat), a floor-tom thud, a yangqin arpeggio, a gong at each phrase |
| Sword Practice at Dawn | `SongSwordDawn` | A Dorian, 4/4, 96, swing 0.05 | sect 5:5 (fixed) | a brisk flute tune, a yangqin arpeggio, an upright bass, frame drums, a tresillo clave and a steady block like blades on a post, a suona in the middle |
| Banquet of the Nine Lamps | `SongBanquet` | D-flat major, 4/4, 96, swing 0.1 | capital 6:4, city 1:1 | a suona tune that leaps, a yangqin answer, a sheng pad, an upright walking bass, kick on every beat, handclaps, hats, tambourine, cowbell, a harp running in the middle |
| Procession of the Silent Court | `SongSilentCourt` | F-sharp major, 4/4, 60 | capital 6:4, sect 5:1 | a horn in long notes, choir and strings, a swaying harp, a timpani on one and three, a floor-tom thud, a block tap, a gong at each phrase, an erhu above in the last statement |
| Dust on the Silk Road | `SongSilkRoad` | D Phrygian dominant, 10/8 (3+3+2+2), 96 | open 0:5, city 1:1 | a suona tune that falls A F-sharp E-flat D, an erhu answering every other bar, a pipa on the group starts, a sheng drone, an upright bass on the dum-tek, congas, a shaker and a zil; a break where the drums play alone |
| Riders of the Grey Steppe | `SongSteppe` | G Dorian, 6/8, 66 | open 0:5, city 1:1 | a gallop in the toms (X-xX-x), an erhu tune, a pipa strumming the gallop, a horn on open fifths, a cello skipping root and fifth, a flute descant, an erhu solo over a heartbeat |
| Rice Terraces After Rain | `SongTerraces` | B-flat Lydian, 4/4, 96 | open 0:5, city 1:1 | a marimba and a yangqin interlocking one stream of sixteenths (gamelan style), a flute tune, a deep xiao answer, a kendang-like conga pattern, a ketuk on the cowbell, a gong at each phrase |
| Haul Away on the Tow Path | `SongTowPath` | E-flat Mixolydian, 12/8, 58 | open 0:5, city 1:1 | a work song: the erhu calls, a crew of voices and horn answers, a kick on one and three, a clap on two and four, a stomping tom, a pluck on the last eighth of every beat |
| Seven Roads to Market Town | `SongSevenRoads` | F Mixolydian, 7/4 (4+3), 112, a little swing | open 0:4, city 1:2 | a flute tune that skips up the chord, a pipa striking five times a bar, a harp running eighths, a low erhu, an upright bass on root-fifth-root, a kick on one and five, rim on three and six, hats, congas and a tom fill into every return |
| Where the Wind Sleeps | `SongWindSleeps` | D-flat Lydian, 4/4, 46 | open 0:5, night 3:1 | an xiao on very long notes, a harp playing notes three beats long across the four-beat bars (3 against 4), chords that change every three bars, a barely audible heartbeat, a gong at each change |
| Lone Rider at the Border | `SongLoneRider` | C-sharp Dorian, 4/4, 84, swung | open 0:5, night 3:1 | a whistle-like xiao over a picked pipa, hoofbeats on two wood blocks, a heartbeat tom, brushed snare; the bridge adds a kick and a tambourine, an erhu solo takes the middle |
| Reel of the Harvest Fair | `SongReel` | E major, 6/8 jig, 66 | open 0:4, city 1:2 | an erhu fiddling a jig, a flute doubling it, a pipa boom-chick, a bodhran-like tom, a clap and a tambourine; the middle is an octave down with the drums nearly gone, the last two rounds lift a whole tone to F-sharp |
| Rooftop Courier | `SongCourier` | B-flat Dorian, 5/4 (3+2), 132, a little swing | city 1:5, capital 6:1 | a funk groove: a syncopated bass riff of sixteenths, kick and snare playing the five beats unevenly, sixteenth hats, cowbell, a pipa striking off-beats, a suona tune of short phrases, an erhu in the gaps, a break with only bass and drums |
| The Abacus Song | `SongAbacus` | A-flat Mixolydian, 4/4, 112 | city 1:5, capital 6:1 | a marimba ostinato grouping its eighths 3+3+2 against the kick's 4+4, built up bead by bead in the intro; a pipa tune, claves on the 3+3+2 and a wood block on the straight eighths |
| The Lanternmaker's Lullaby | `SongLullaby` | A Lydian, 5/4 (3+2), 56 | city 1:4, night 3:2 | a soft flute tune, a harp rocking A E C-sharp E C-sharp, a low xun in the second round, a cello holding three beats then two, a shaker on the 3+2, a tom heartbeat |
| Guild Hall Fugue | `SongFugue` | C Dorian, 4/4, 92 | city 1:5, capital 6:1 | a two-bar subject entering in a flute, an erhu (a fifth higher, with a countersubject), a pipa and a cello; a falling sequence; the exposition again; a stretto with all four voices a bar apart |
| Lion Dance in the Square | `SongLion` | F major pentatonic, 4/4, 140 | city 1:5, capital 6:1 | a big drum that rolls to wake the lion then plays X-xx-X-xx, a gong, crash cymbals and an agogo, a suona tune of leaps, a pipa strumming eighths, a quiet middle over claves |
| Last Round at the Crooked Crane | `SongCrane` | F major, swung 4/4 with seventh chords, 126 | city 1:5, capital 6:1 | a tavern jazz trio: a flute head of sixteen bars, piano comping, an upright walking the chord tones, a swing ride on the hat, a foot on two and four; an erhu solo of chord-tone runs; the head again |
| The Floor Is Alive | `SongFloor` | G Phrygian, 7/8 (3+2+2), 128 (`layers: fixed`) | danger 2:5 | a cello riff that changes a note a bar, timpani on the group starts, a suona shrieking short phrases, a bridge that stops almost everything, a scream climbing a note a bar |
| Three Footsteps and a Heartbeat | `SongFootsteps` | B Phrygian, 5/4, 58 (fixed) | danger 2:5 | a tom that steps three times, unevenly, then a kick heartbeat; an ocarina-like xun, a cello holding the root, an erhu keening above a choir, a break where the steps stop and the heart speeds up |
| Last Lantern Out | `SongLanternOut` | D Lydian, 12/8, 40 (fixed) | night 3:5 | a piano rocking twelve eighths a bar, a xun on slow falling phrases, strings and a low cello, a faint heartbeat and brushed snare, a bell and a chime drop, a minor-side middle |
| The Dream Weaver's Loom | `SongLoom` | D then G major pentatonic, 4/4, 52 (fixed) | night 3:5 | layers of 3, 5, 10 and 12 beats drifting against the four-beat bar (a harp, a marimba, an xiao, a chime, a wood tick) that meet again after fifteen bars |
| The Hall of Nine Gongs | `SongNineGongs` | E minor on its pentatonic notes, 4/4, 48 (fixed) | sect 5:5 | a sixteen-beat cycle of gongs, a low drum, an agogo and a wood tick under a slow flute, a sheng and a choir, a cello drone |
| The Elders Disagree | `SongElders` | E-flat Dorian, 5/4 (3+2), 76 (fixed) | sect 5:5 | a horn and an erhu argue in turn, a timpani calls order, claves and a floor tom keep the 3+2; later both play the same tune in two octaves, the cello and choir settle it |
| Ambassadors in the Hall of Windows | `SongAmbassadors` | D in four colours, 4/4, 88 | capital 6:5, city 1:1 | three envoys, each with a scale (D Phrygian dominant on a suona, D major pentatonic on an erhu, D Dorian on a horn), then one tune in plain D major together |

**Score text** (`Score`): header `title`, `bpm`, `beats` (per bar; 6/8 is `beats: 2` with eighths written `1/3`), `shift lo hi` (key shifts a performance may use), `moods mood:weight ...`, `layers: fixed` (the song plays every track whatever the mood: the four songs written for one mood use it), `form section[?][:dynamic] ...` (`?` = optional, dynamics pp p mp mf f ff); `pattern NAME: tone:beats ...` (tones 1 3 5 7 9 = chord tones, `b` bass, `'` octave up, `,` down, `a+b` together, `=` nearest to the track's centre (smooth pads), `@D2` a fixed pitch (drums), `r` rest, duration `*` = the rest of the chord); `track NAME role= inst=a|b vel= pan= ctr=D3 pat= p=percent all`; then `[Section]` / `[A2 < A]` (copies A) with a `chords:` line (`D | Bm7 | G/B | Asus4 A`, a bar may hold several chords) and one line per track: explicit notes (`A4:1.5 B4:.5 D5:2 | ...`, `r`, `[C3 G3]` chords, `d=.5` default length, dynamics, `>` accent, `'` short, `_` legato, `~` tie, `s` strum, `%` repeat the bar, `(..)*n`) or, for a chord-driven track, a pattern name / `on` / `-` with `@5-8` bars, `x.8` level, `o+1` octave. A line may name an instrument for that section: `lead(erhu): ...`. The parser checks that every bar adds up and every chord, pattern, track and instrument exists.

**Layers**: each track has a role (lead, counter, harmony, bass, rhythm, color, perc, drive). `Conductor.MASK` gives each mood a level per role (open: all but drive; city: same; danger: no lead or counter, rhythm half, drive full; night: lead 0.7, harmony 0.9, bass 0.6, rhythm 0.45, color full, no counter or perc; war: lead 0.6, no counter, drive full; sect: lead 0.85, counter 0.6, bass 0.5, rhythm 0.6; capital: all). Levels move a third of the way per bar, so layers come in and out on bar lines.

**Conductor**: picks a song by mood weight (never the song just played when the mood has another choice), `Score.perform` plays it a little differently (key shift, tempo +-4%, one instrument of each track's `inst=a|b|c`, `p=` chance of a track, optional sections), fades in over 4 s, slows 5-10% over the last two bars, and when it ends the music rests (20-50 s in the open, 15-40 in a city, 35-80 at night, 6-20 in danger or war). A mood change to one the song does not suit (`weight*2 < best weight`) starts a 2-bar fade-out at the next section start (at most 8 bars later, never in the first 6 bars or before the mood has held 6 s) and the new song begins after 1.2 s; danger and war do not wait: after the mood has held 3 s (and the song has played 8 s) the song fades out over one bar at the next bar line. The same song stays when only the layers change (the mood the song began in never makes it leave). Notes are written a bar at a time into a queue (`Ambient.Note`: time, instrument, pitch, velocity, length, pan, track); plucked and struck instruments ring on after their written length (`Conductor.RING`).

**Engines**: the built-in 44.1 kHz stereo synthesiser (Karplus-Strong plucks, bowed and blown harmonic tones, struck bars, drums, bells, two decorrelated reverbs; 24 instruments: pluck flute erhu pad bell chime taiko wood pipa piano harp strings cello horn choir, then kit suona yangqin sheng xun xiao marimba timpani upright) or the Java software MIDI synthesiser with a SoundFont from `soundfonts/` (`MidiOut`; the default engine `auto` uses the first font found; General MIDI programs 107 73 110 89 14 98 116 115 106 0 46 48 42 60 53 and 0 111 15 20 79 77 12 47 32, remappable in `soundfonts/programs.txt`; the master fade is controller 7, `MidiOut.cc7`). `MidiOut.TRIM_DB` equalises the instruments of the bundled GeneralUser GS (A-weighted loudness of the same test phrase; measure again with `SongTool trim` for another font) so a song's `vel=` means the same for every instrument.

**The drum kit and the new instruments (2026-10-11)**: `kit` is a whole drum kit: in the MIDI engine it is the General MIDI drum channel (9) and the pitch of a note is the drum (35..81), in the built-in synthesiser `Ambient.Kit` makes each of the 41 named pieces from sine sweeps, filtered noise and ringing partials (levelled one by one, `Kit.GAIN_DB`). Scores name the pieces: `kick kick2 rim snare snare2 clap tom1..tom5 hat pedal ohat crash splash crash2 ride ride2 rbell gong tamb cow bongo bongo2 mconga conga conga2 timbale timbale2 agogo agogo2 cabasa shaker guiro guiro2 clave block block2 mtri tri` (`Score.DRUMS`): `@snare:1` in a pattern, `snare:1` or `[kick hat]` in a line of notes; a trailing `>` is an accent (x1.3 in a pattern, x1.18 in a line) and `^` a ghost note (x0.5); kit, taiko and wood notes are never transposed by a key shift; no note-off is sent to the drum channel (a drum is struck, and an off could cut the next hit of the same drum short). `swing: 0.15` in the header delays every note on the second half of a beat by that fraction of a beat (`Conductor`). A synthesiser has sixteen channels, so `MidiOut` gives an instrument a channel when its first note comes and hands the channel that has been quiet the longest to the next new instrument (a song uses ten or twelve; the note-offs remember the channel the note was played on). The SoundFont's drum pieces differ a lot in loudness and in sample layers by velocity (the ride and the triangles are 15 to 20 dB under the snare, tom2 is silent at full velocity): balance each kit track on its own with `SongTool balance` and avoid `mtri`, `tri` and `tom2` with this font. The new melodic instruments: suona (GM shehnai, a piercing reed for festivals, war and danger), yangqin (dulcimer, bright hammered strings), sheng (reed organ, held chords), xun (ocarina, a breathy wail), xiao (shakuhachi, an end-blown flute), marimba (a struck wooden bar), timpani (tuned kettle drum, follows the key shift) and upright (an acoustic bass for walking lines).

**Checking how alike songs are**: `SongTool similar [limit]` lists every song with its detected key, metre and tempo and the pairs that sound most alike: the mean of four cosines (the instruments by number of notes, where the pitched notes fall inside a beat, the steps of the lead lines, the pitch classes of the lead lines turned to the key), x0.9 when the metre differs, x0.95 when the tempo differs by more than 15%. Of the 49 songs the closest pair scores 0.92 and the mean is 0.64; `SongProbe` S1 fails above 0.985.

**Settings and commands**: `Settings.music` (off by default), `musicVolume` (40), `musicEngine`, `soundFont`, `musicMix`. `music on|off|next|list|play <number or name>|play auto|volume|fonts|font|presets|engine|mix`; the Settings window has Music, volume, Next song, Sound source; `Ambient.pin(song)` plays only that song until `pin(null)`. `Ambient.followWorld` sets the mood from the player's tile (crowd hotspot war, hotspot / dungeon / 3,000+ monsters danger, Originis capital, sect, city, winter night).

**Tools and tests**: `tools/SongTool.java` (`info`, `lint` melody against chords + semitone clashes + leaps, `dump` a song bar by bar, `roll` a piano-roll PNG, `midi`, `wav` with the SoundFont or built-in, `balance` each track's A-weighted loudness against its role's target and the velocity that reaches it, `trim`, `all <folder>`); run with `--add-exports java.desktop/com.sun.media.sound=ALL-UNNAMED` for WAV files. `tools/probes/SongProbe.java` (28 checks, and `SongGenProbe` 20 for the generator and the kept songs: parsing, the score format, lint, a song for every mood, drive layer in danger and none in the open, rests, fade to the market song, no song twice in a row, both engines render). The preview files go to `music-preview/` (not in git).
A song with `layers: fixed` ignores `Conductor.MASK`; the others get the layers of the mood (see above).
**Composed songs and kept songs (EXPERIMENTAL; the game's songs are written by hand and new songs will be too, the generator is frozen as an extra for players who want endless variety)** (`SongGen`, `SongCheck`, `SongBook`): `SongGen.generate(seed, arrangement)` writes a new song in the style of the written ones. The arrangement is one of the seven written songs (all its patterns, tracks, instruments, levels, sections and layers are kept, so it is balanced); the key, mode (major, or natural minor / dorian), chords and melody are new. Chords come from banks of eight-bar progressions as scale degrees (major A and B, minor A and B), coloured with maj7 / m7 / 7 / add9 and a major V in minor keys. The melody is built like the written ones: a one-bar hook (a rhythm cell of the meter and a contour of steps along the scale), the hook again on the next chord (a sequence, never the same bar twice), a contrast bar, a half cadence, the hook returning, a climbing bar, a cadence on the third or fifth of the last chord; the answer keeps bars 1, 2, 5, 6 and rewrites the rest; the middle is a long-note line along a rising contour; the last statement climbs in arpeggios; intro, outro, counter-melody, bells and chimes follow the chords. Notes on long strong beats are chord tones, passing notes keep a semitone away from the chord, cadences and arpeggios are chosen so the leap before and after is small. Each attempt goes through `SongCheck` (parse, no stray notes, no clashes, no leap over a sixth, at least half the moves steps, range at most 28 semitones) and is thrown away if it fails (about six attempts a song, about 30 ms); the same seed and arrangement always give the same song. Titles come from `SongGen.title` (mood word lists, hundreds of names). A composed song says `source: gen <arrangement> <seed>` and its moods are the template's weights divided by four (so it is rare).
**Curating**: `music gen [open|city|danger|night|war|sect|capital] [arrangement] [seed]` composes and plays one (pinned, looping with short rests; the seed is printed); `music keep` writes the song that is playing to `songs/<title>.txt` (moods weighted 3, so it plays as often as a written song) and it joins the rotation at once; `music drop <song>` deletes a kept song; `music list` marks kept songs `[kept]`. `songs/*.txt` is read at start-up (and by `SongBook.reloadKept`); a file that does not parse is skipped and noted in `SongBook.keptProblems`. A kept file is an ordinary score, so it can be edited by hand. `Settings.musicGenerated` (`music share <0-100>`, a slider in Settings, default 0) lets newly composed songs that are not kept take that share of the song slots (`Conductor.pick`).
**Adding a song**: copy a `Song*.java`, write the text, add it to `SongBook.sources`, run `SongTool lint` and `balance`, then `music play <name>` in the game.

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

## Planned moves, Go There and parallel NPC turns (2026-10-10)
- **A turn acts first and steps last.** `GameDriver.npcTurn` -> `actThenWander`: the quest routine (`quest()`: spot quests, the target quest's work, the step toward its place) runs on the tile the character stands in; a character whose quest planned no step wanders one step (a quest in progress no longer adds a random step on top: an NPC takes one step a turn, a diagonal counts as one). Arriving is noticed at the start of the next turn (`pathfindXY` returns 1 when already there), and the work at the place is done then.
- **Steps are planned, then taken together.** While `Character.planning` is true `movement(dir)` only sets `pendTo` / `pendFrom` (making the tiles on the way, like a real step; one planned step a turn); `GameDriver.applyMoves` takes them all after the people who play have acted (`Character.applyMove`; a step is dropped if the character died or was moved some other way meanwhile). So nobody leaves or enters a tile while the NPC turns run: a mover can still be met, fought or robbed where he stood. A party member who stood with his leader rides with him (`Character.ride`, `applyRide`, taken after the leaders' steps); children follow their mothers and companions their masters after the steps (`followAllCompanions`, on all cores).
- **Order of a tick**: time/aging and cultivation as before, then the NPC phase (decide and act, plan the step), the births of the phase are merged (`Map.mergeBirths`, in id order, so ids stay equal to list positions), then the people who play act (MANUAL prompt, AUTO, HYBRID, followed NPCs), then the steps are taken. Before, the people who play went first.
- **Go There** (`PlayerCommands.stepAuto`, `chase`, `goquest`): the chaser acts after the NPCs have decided, so `Character.interceptAim` reads the exact step an NPC has planned (`GameDriver.movesPlanned`, `Character.turnT`, `pendTo`) instead of guessing it from his quest; a walker too far ahead to be caught from behind is met on his way (`leadAim`) or at the place he heads for. `UpdateProbe` (the old guess) and `PlanProbe` P7-P11 (the exact aim and a live chase) test it.
- **Parallel NPC turns (EXPERIMENTAL, off by default)**: `Settings.parallelNpcs` (`settings parallel on|off`, the Settings window, `-Dtdf.parallel=true`). `GameDriver.parallelNpcPhase` groups the NPCs by tile and shares the groups over `Map.tickPool` (biggest first, a core takes the next group when it is free); the tiles where a person who plays stands are played one NPC after the other on the game thread afterwards (a turn there may have to ask the person a question and wait for the console). A turn that reaches beyond its tile hands that part to `Map.later(Runnable)`, which runs it at once on one thread and, in a parallel phase, on the game thread when the phase is over: everything of a death that touches other people (party, master, spouse, unborn children, sect, friends' lists, children's "Find Parent's Body", revenge and bounties, the tile's list of people, legacy), the awakening of a Monster (its place in the tile's list), a payment to a shop owner / merchant / city owner far away (`Character.credit`), and the invitation of a friend elsewhere (`QuestLife.invitePartner`). Shared lists of a tile are `SafeList`s (every call synchronized, iteration over a snapshot: `Area.characters`, pool, shelves, boards, `Shop.inventory`, `Library.techniques`; converted when an old save is loaded), the purses are synchronized (`canAfford`, `deductSs`, `convertCurrency`, `credit`), `Organization` changes of people, ranks and alliances share one static lock, ids come from `GameDriver.nextUid()`, `Map.addPopulation`, `Map.addAwakened`, `Map.addMonsterOnce`, `ParallelRandom` gives every thread its own random numbers, and the steps themselves are taken on all cores (`applyStepsInParallel`: everybody leaves, then everybody arrives, one core per tile). A turn that still fails (an exception) is counted and printed (`[PARALLEL]`, the sites summarised every 1,000) and loses the rest of that one turn.
- **Checking it**: `-Dtdf.verify=true` runs `Verify.check()` at the end of every tick (ids = positions, every living person listed once at his own index in his tile, dead not listed, sect lists agree) and prints the violations by kind. `PlanProbe` tests the planned steps, riders, the aim, Join Sect, `invite <id>`, the tournament sign-up and the epoch settings.
- **Two old bugs the verifier found** (serial too): a Monster killed by a beast or by another Monster stayed in the tile's list as a corpse, and a Monster that lost the fight it started in the roaming loop then walked into a new tile and listed itself there; slain Monsters now also leave their sect (a Monster can join one).

## Performance notes (2026-10-06)
See `Performance Report.md`. `Map.heavenExists` (refreshed once a tick) gates the Heaven Treasure scan; an NPC's `knownLocations` is capped at about 150 (`Character.learnPlace`: past 180 plain far tiles are forgotten first; humans keep all); `quest()` tests quest-name guards against a bit mask (`qMaskT`/`qMaskS`, rebuilt at its start, updated by `addT`/`addS`); Seek Potential looks one turn in six; `tryRallyParty` walks the friend list instead of copying the tile; `Perf` (`-Dtdf.perf=true`) times the tick phases, `-Dtdf.seed`, `-Dtdf.threads` and `-Dtdf.spread` make runs comparable.


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

### Item levels (the scale note, from the code, 2026-10-18)
- **Herbs, metals, pills, food, wine, poison**: level 1-10 = the realm number (1 = Qi Cultivation ... 10 = True Immortal; `Item.getPillName`).
- **Gear (weapons, clothing, arrays)**: level **1-100**, the same scale as a character's level (`tempLvl = (lvl + armor + weapon + both techniques) / 5`, `Character.tempLvlCheck`). A smith's weapon: `ml = min(material level, skill cap)`, `raw = rand(ml * 5) + 1 + ml * 5`, capped at `(majorLvl + 1) * 10` (and 100). `Treasure`, tournament prizes and `AmateurCraft` also use 1-100.
- **Fixed 2026-10-18**: (1) `Naming.weapon / clothing / array` now take the gear LEVEL and name it by its realm (`Naming.gearRealm(L) = (L - 1) / 10 + 1`), and the Items window / icons grade by `GuiItems.realmOf(item)` (gear by realm, everything else by its level); (2) gear that was handed out on the 1-10 scale now lands inside its realm through `Item.levelInRealm(realm)` (levels `r*10-9 .. r*10`): the Lucky fortune event (one realm above), the Lore tournament prize, the Giant Footprint relic, `Starts.giveGear` (story starts, Palace gear), and the `weapon` setting / `cheat give weapon|armor|array` / the old `GameDriver` debug menu take 1-100; (3) a Heaven-Sent instrument is level 1-10 (instruments use the realm number; it used to be 11-100); (4) `GearNames.fixAll()` runs at every load (`GameDriver.applySnapshot`): a weapon, garment or array whose grade or adjective band does not match its level is renamed by the current namer (type word and a "Heaven-Sent " / "Relic of the Footprint: " prefix kept; names that are right or not of the old shape are left alone; idempotent). The scrap gear a smith makes without material (1 in 300, levels 1-10) stays as it is. Gear that old worlds already hold keeps its level (it was weak because it was made on the 1-10 scale); only its name changes. Probe `GearProbe` (20 checks).

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
- Population control: after year 10, a tile over capacity (`GameDriver.CROWD_CAPACITY` = `Settings.crowdFactor` (3.5, `settings crowd`) × the *starting* population / range², i.e. 3.5 times a tile's fair share, floor 20 — 364 at 100k on the 31×31 map, 20 in the 3,000-person test world; cities 4×; never based on the current population; Originis is exempt) becomes a **crowd hotspot** (`Area.crowdCheck`, +15 level surge, `crowdHotspot`/`hotspot` flags). Every tick beasts ambush 2% of the excess (max 40) — level-blind, `ambusher` beasts kill any cultivator they defeat; array-protected tiles are exempt. The hotspot ends when the tile is under 80% of capacity. Cultivators there get a "Disperse" quest (10%/turn) to a nearby tile under half capacity. Births are never reduced. At 200,000 living cultivators (`GameDriver` yearly) an epoch purge marks 20% of each major level from 0 to `Settings.epochMaxRealm` (3; levels with < 10 cultivators spared); victims die 3000 per tick.
- City population (`Area.age()`): +0.1%/tick (min +1) toward `populationCap()` = 100k × cityLevel² (max 100M); yearly tax = population / 100 (half owner, half `cityTreasury`). Counters (monsters 50M, herbs/metal 1B, population) are capped so they can never wrap; `convertCurrency()` converts by division.
- Sect raids: at most one "Sect Raid" per sect per 13 weeks (`Organization.nextRaidWeek`).
- Saving: see "Saves (`SaveManager`)" above; `SaveData.yearlyDone` stops a loaded save from repeating its year's events.
- `buyFromSeller()` — buys items from sellers at 55% of `itemPrice`.
- `sellToBuyer()` — sells to buyers at 150% of `itemPrice`.
- `restockFromCity()` — buys from city pool at 30% of `itemPrice`.
- Merchant arbitrage: Buy Gear quest (travel to city A for cheap goods) → Sell Stock quest (travel to city B to sell at profit).

### Library
- `Library(Area)` — city library; buy techniques with ss (`potential^2 × 1000`). Sells (`Library.sellTechnique`, `Character.sellBestTechnique`): the seller is paid **20%** (`100 × potential²`), **loses** the technique (so copies do not multiply), and the book replaces the shelf's book of the same (type, potential) (`shelve`). Techniques of potential < 4 (`MIN_SELL_POTENTIAL`) and the four equipped ones are never sold; `compact()` (yearly) cleans old shelves. The GUI library table sorts on every column and filters (name, type, potential range, max price); `library [limit] [words] type= potential>= price<= sort=` in the terminal (`ListQuery`).
- **Technique slots** (`Character.equip` -> `sortTechniques`, `TechniqueSlots`): four slots, Martial / Martial 2 / Movement / Movement 2. The sort gives slot 1 to the higher level (at the same level, the higher potential) and slot 2 to the highest potential above slot 1's; everybody but an Innovative or a Beast forgets the techniques that fit no slot. **In Manual mode the sort does not run** (same rule as gear: `Settings.autoEquipManual` turns it back on; `equip` / Equip best sort once) and the Known list is kept; the hand's tools are `swaptech martial|movement`, `usetech <n> [1|2]`, `forget martial|martial2|movement|movement2|known <n>` (Techniques tab buttons: Swap 1 / 2, Wear selected, Forget selected, Sort for me). `practice()` levels Martial 1 and 2 and Movement 1 and 2. Probe `TechProbe`. The known list (kept by Innovative and Beast people; Manual players keep theirs) holds each technique once and never a worn one: `tidyKnown()` at every sort, and an NPC's list is kept to its best 30 (`TECH_CAP`). Saves: every file move is retried (`SaveManager.moveRetry`), a gzip save is read first and its checksum only matters for an old uncompressed one, a failed load at the start-up menu says so and shows the menu again, and a damaged autosave is set aside as `damaged-auto-<n>-<time>` before the autosaves rotate.
- **Craft chore and the spot action:** for a person who plays (Manual or Hybrid) the Craft duty's trade work and gathering (`Character.quest`, `craftChoreTurn`) runs only when the spot action is Profession or Surprise me. The little stories (`Vignettes.tick`) come only in Manual mode.
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

### Speed, memory and balance changes (2026-10-09, see `Playtest Findings.md`)
- `Character.quest()`: the Bully, Rob Caravan and party loops no longer walk the whole tile (the Bully check looks at a random window of 48 people; `dissolveParty` / `partyBeastHunt` read `followerIds`); the guard bits of the quest names are constants (`QB_*`) and cached on the `Quest` (`bitName`/`bitVal`); `learnPlace` remembers the last place learned; `QuestLife.family` has a direct-mapped cache; `pruneKnownLocations` selects with a primitive sort; `meditate` builds its text only for a human; the bounty board is read only in a city.
- **Trigger throttle**: an NPC looks at the dynamic quest triggers (the long block of `quest()` up to `QuestLife.consider`) every `TRIGGER_EVERY`-th turn (2; `-Dtdf.triggerEvery=1` switches it off); on that turn every dice roll in the block (`dice(n)`, `QuestLife.roll`) is scaled by `trigK`, so the chance per week stays the same. A human is never throttled.
- `RelationshipList` keeps at most `tdf.relCap` (100) entries per person; `Item` names are interned.
- Reputation titles (`Naming.assignTitles`, yearly): each kind of title (`Naming.titleFamily`: both genders are one kind; a sect master's title is per sect) is held by its ten best only (`TITLE_HOLDERS`, ranked by `Naming.titleMetric`: kills, items crafted, wealth, ...); the others fall to the next title they qualify for, or none.
- Shops: `Shop.seedStock()` puts 4-8 plain weapons / coats on the shelves when a shop opens and each year it holds fewer than 5 items. The start spread is 21 tiles (about 230 people per tile; `settingsRev` 2 moves a saved 15 to 21).

### Auctions (`Auctions.java`)
- Cities of 500+ have an auction block (`Area.auctionBag`). Only luxuries are put up: Heaven-Sent items, **Potential Pills** (never on shelves any more; `Character.sell` consigns them once the seller's potential is 100) and the city's vintage food/wine/poison (`Life.auctionLots`, now and then).
- `Auctions.tick(area)` every week: an auction is held when 5 lots wait, or 6 weeks after the first arrived (never the same week). Up to 5 lots, best first. Second-price: the winner pays the runner-up's bid or the reserve (`reserve`: heaven 20× / other 5× `itemPrice` × quantity; vintage goods the old curve); a MANUAL human in the city is asked (`DecisionBroker.askNumber`), NPCs and AUTO characters bid only for what they `want` (`NEED` or `COLLECTOR`) and offer `npcBid`: a need 1-3 reserves, a collector 1-1.5, at most half / a tenth of the purse (they used to bid the whole purse, so the winner paid the runner-up's entire fortune),  (the Potential Pill below potential 100, the next-realm pill, better gear, arrays for masters/lords, anything for the very rich). The consignor gets 90%, the city lord/treasury the rest. Unsold lots wait.
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


## 2026-10-06: amateur crafting, Study Trip, policies, sect missions, paused info, cheats
- **Amateur craft** (`AmateurCraft.java`): everyone can `craft weapon|clothing|pill|array|food|wine|poison|instrument` (the GUI Craft button is a menu). Twice a trade's material (weapon/clothing 1,000, array 4,000 in one stack, pill 100 herbs, goods 50 herbs a portion, at most two portions), plain quality: item level = min(10 + `amateurLvl` (0-50), (majorLvl+1)*10) from a roll like a trade's, materials above the usable level just work at that level; `amateurLvl` rises with `AmateurCraft.gain`. A trade (`Character.hasTrade()`) keeps its own craft (`craft` with no argument). Without a trade `craft` shows the menu (no turn). An NPC or AUTO character with no weapon or clothes calls `AmateurCraft.gearUp` (1 turn in 8: craft from what it carries, or mine/forage on its tile).
- **Cheaper gear**: a trade uses `Character.CRAFT_COST` = 500 units (it was 1,000), an array needs `ARRAY_MIN` 600 in the bag (was 10,000, then 2,000); `durabilityCheckUtil` breaks gear half as often (5,000 base and 450 per item realm instead of 10,000 and 900).
- **Study Trip** (UPKEEP duty): `Character.bestStudy/studyTarget/studyHere`: an NPC or AUTO character (1 turn in 30) heads for the nearest known city whose library has a technique better than the one it wears, unknown to it and affordable; arriving it buys (`Library.learnTechnique`). Humans get it as an alert. The old 1-in-52 buy while standing in a library city uses the same `studyHere`.
- **Policies** (`PlayerControl.squadPolicy/friendPolicy`, `policy squad ask|auto|never`, `policy friends ask|accept|decline`): a human leader's squad recruits go through `recruitAccepted` (a MANUAL player is asked; AUTO and scripts accept; the declined are remembered in `squadDeclined`); a friendship that would form with a human (value reaching 1, not family, not a child) goes through `friendshipAccepted`; a refusal is `Relationship.refused` (kept, the value never grows past it; `avoid <n>` / `unavoid <n>` set and lift it; nothing is lost).
- **Sect missions**: `mission` lists the board, `mission <n>` takes one at the sect's home (a MANUAL human is also asked which one when the spot quest runs), `mission abandon`. A human's mission is a pinned `Sect Mission` target quest (`Character.syncMissionQuest`, shown in the Quest Log with `Quest.label`; `qdo` at the sect's home hands in what it asks; removed when done, abandoned or the character goes AUTO, where the old text-field flow runs by itself). `Character.missionText` writes a board entry in words.
- **Paused game**: `GameDriver.servePausedInfo` answers the commands in `PlayerCommands.INFO_VERBS` (no turn, no change) from the pause loop, so the info buttons still open their pop-ups; commands that act wait in order. Windows retry every 100 ms until they can read the world (`World.readSoon`, `GuiWindows.window`).
- **Cheats** only for the Cheater trait (`cheat`, `z`, the dock button); Edit Self keeps working but the potential is locked unless you are a Cheater (the editor at character creation is unlimited). The portrait cache key includes the gender. Wandering Immortals add a gift from their old trade (`LoreEndings.professionGift`).
- **Shelf limits** (after the cheaper crafting): `Shop.capacity()` = 40 + 20 x level (a full shop refuses `buyFromSeller` and stops restocking), `Character.POOL_CAP` 400 items per city pool (overflow scrapped), an NPC crafter with a pack of 16+ does not smith or tailor until it has sold (`smithing`, `tailoring`).


## 2026-10-07: settlements, epoch by tick time, sales tax, zoom, gear flow, instruments
- **Settlements** (`Settlements`): targets `cityTarget = max(min(25, cult/150), cult/2000)`, `sectTarget = max(min(25, cult/150), cult/2500)`, up to 10 of each founded a year; `cityCount/sectCount` (atomic, recounted yearly) feed `cityRoom/sectRoom` (a Leader's own founding stops at target + 5) and `mayDestroy` (razing needs more than `PROTECT` = 25 of that kind).
- **Sales tax**: `Area.salesTax(amount)` puts a tenth in `cityTreasury`; `receivePayment` and `Shop.sellToBuyer` use it. City level = f(treasury) as before, shown beside the tile level in the map UI.
- **Epoch** (`GameDriver`): `tickNs[26]` ring of each tick's own time (`World.blockedNs` subtracts input / dialog / GUI waits); checked once a year: `living >= Settings.epochPopulation (default 200,000, the population target) || (living >= EPOCH_MIN_LIVING (10,000) && medianTickMs() > Settings.epochTickSeconds * 1000 (default 2 s))`; marks `Settings.epochCullPercent` (20%) of every realm of 10 or more (humans spared) or, when `Settings.epochCullNumber` > 0, that many in all, shared out over the realms by size; kills 3,000 a tick (death cause `GameDriver.PURGE_CAUSE`: the Chronicle says "consumed by Heaven in the epoch", a friend's notice "Heaven has consumed your ...", a followed cultivator prints "Heaven has consumed ..."), then `epochBlessing()` (the potential of half a Potential Pill, `givePotential(0.5)`, to the survivors of the realms that were culled: `GameDriver.epochTierCulled`, `Settings.epochMaxRealm`; `settings epochrealm 3`). The three limits are settings (`settings epochpop 200000`, `settings epochseconds 2`, `settings epochcull 20%` or `epochcull 5000`; the Settings window; saved in settings.properties). A slower computer reaches the tick-time limit sooner. Tests set `Settings.epochTickSeconds` / `GameDriver.EPOCH_MIN_LIVING` (`-Dtdf.epochMs`, `-Dtdf.epochMin` give the defaults).
- **Map**: `MapViewer.zoom` 1-5, `viewX/viewY`, `followMe`; `vis()` tiles a side from `originX/originY` (the world wraps), `colOf/rowOf` (screen) and `tileX/tileY` (click) are the only conversions. `PlaceImages.thumb(area, cell)` is the map's picture cache (1,600 entries, 40 new pictures per paint, 96/160/224 px by cell size).
- **Gear flow**: `Character.canShopHere`, `buyFromPool` (1.25x `poolPrice`, after the shops), `shopRoom`; crafters' `crafterStock` (2+ cargo pieces) sells in a city and starts Go Sell to the nearest city whose shops have room; `nextExploreWeek/nextHuntWeek` rests.
- **Music**: `Life.craft` refuses instruments (-4); `Life.play` gives a Musician a Lv.1 instrument if they have none; `Life.practise` matures it (chance 1 in 10 + 6 x level per play, cap min(10, realm + 1)); `Life.concert` / `Life.stage` and the `Perform` quest (`Character.nextPerformWeek`, 1 turn in 20, rest 26 weeks).
- **Craft material**: `smithing/tailoring` take the stack that gives the best quality for the skill and work richer material at the level `professionLvl/10 + 2` allows (before, a stack above that was never used but still counted as "enough", so the craftsman stopped gathering).
- **Gear index**: `Area.gearIndex()` counts the weapons and clothes on the shelves and in the pool by item level (rebuilt when the turn or the number of items changes); `Character.cheapestGear/hasBuyableGear/buyGear` use it so that thousands of shelf items are not read by every character every turn.


## 2026-10-08: quest profiles, fatigue and the routine quests (`QuestLife.java`)
- **Families**: `ADV` (Hunt Beast, Explore Dungeon, Heaven Treasure, Bounty Hunt, Seek Territory, Hunt Cultivator), `FIGHT` (Battle *, Revenge, Rob Caravan, Sect Raid, Demand Tribute, Recruit Defector), `GROW` (Buy Gear, Study Trip, Seek Potential, Refine Blood, Buy Pills, Sell Technique, Attend Auction), `TRADE` (Go Sell, Restock Trip, Sell Stock, Festival Trade, Trade Run, Deliver *), `SOC` (Find Person, Visit Family, Courtship, Teach Disciple, Gift Technique, Get Pregnant, Find Parent's Body), `SECT` (Join Sect, Return to Sect, Recruit Member, Propose Alliance, Donate Array, Sect Mission, Found City / Sect), `CULT` (Meditation Retreat), `JOURN` (Pilgrimage, Perform, Tame Beast, Find Lost Beast). `QuestLife.family(name)` is -1 for the rest.
- **Target shares** (`QuestLife.target`, once a year): `BASE` {adventure .22, conflict .09, growth .13, trade .06, company .20, sect .09, meditation .12, journeys .07} times the trait's factors (Leader company 1.3 and adventure 0.7, Merchant trade 3, Friendly company 2.2, Runner journeys 2, Tamer 1.8, Musician 2.5, Spirit Vein meditation 2.2, Innovative growth 1.5...), times the stage (under 30: adventure 1.4, growth 1.5; elder: meditation 1.8, company 1.5, adventure 0.5, conflict 0.4), times the alignment (Evil conflict 1.6, Good company and sect 1.2), normalised.
- **Fatigue** (`QuestLife.tick`, `qEma`): every turn the family of the current target quest gets 1.5% added to a running average that forgets at 1.5% a turn (about 66 weeks); `interest(f)` = (target / actual share)^1.5 between 0.1 and 3.5. `roll(c, quest, odds)` divides the odds by the interest (squared when below 1); `pick` takes the quest with the best `priority + 30 ln(interest)` and leaves out any quest whose interest is below 0.8 unless its maximum priority is 80 or more. The arrays are transient (a loaded game starts at its targets).
- **New quests**: `QuestLife.consider` (once a turn, not for a MANUAL human; one new routine quest at most and `nextRoutineWeek` rests 3-4 weeks) adds them; `viable` and `run` are called from `Character.questViable` and `targetQuests`; `Quest.progress` counts the turns of a retreat (8), a pilgrimage (6) or an auction wait (up to 8) while the quest stays current (it returns 0).
- **Spirit veins**: `Area.spiritualVein` (1% of tiles and 1 in 30 of high ones) already adds one `cultivate()` a turn to everyone standing there; now also Meditation Retreat (twice the cultivation, partner dual cultivation), `Character` dual cultivation 1 in 5 per turn on a vein (was 1 in 20), `Settlements` and the natural `cityLeadership/sectLeadership` rolls (1 in 20 on a vein, 1 in 100 elsewhere), `QuestLife.foundingSite` (+100 for a vein).
- **Materials market**: Gatherers and Miners gather every turn; `Trade Run` sells everything above 300 (a crafter keeps 2,000 of what it uses) to a city that can pay (`unit price = lvl^2.5 / 10`, at most a fifth of the treasury per run); `QuestLife.buyMaterials` lets a craftsman short of herbs or metal buy up to 1,500 from the city's `materialBag` at 1.5x.
- **More routine quests** (same day): `Market Trip` (no meal: `Life.marketTrip`), `Gathering Trip` (`herbWorker/metalWorker` go where `herbMaterials`/`metalMaterials` are 3x richer and at least 20,000), `Seek Teacher` (master or friend 10+ levels up; 5 days), `Sightseeing` (3 days; `learnPlace` on the 5x5 tiles round it), `Courier Run` (Runner; pays min(treasury/50, 3000)); `QuestLife.consider` walks `ROUTINE`/`ODDS` from a random start each turn. Durations: visit 4, lessons 4, courtship 3, trade run 2, market trip 2, teacher 5, retreat 8, pilgrimage 6, gathering 8.

- **Profession (2026-10-09)**: `Profession.run(c)` is the Profession button and the `profession` / `craft` command with no kind (`hasTrade`: Alchemist, Blacksmith, Tailor, Scribe, Leader, Innovative, Merchant, Friendly, Lucky, Tamer, Poisoner, Drunken, Chef, Musician, Gatherer, Miner). A trade short of material gathers it (herb or metal tile) or buys it at a city market (`QuestLife.buyMaterials`) and says so; with neither it says why. NPCs still use `Character.professionPicker`. Innovative: `inspiration` (`gainInspiration` in `winCheck`, +5 won / +2 lost, x0.5-2 by the foe's `tempLvl`), `INSPIRATION_NEEDED` 100, `inventTechnique` (potential `(major+1)*8 + rand(10) + professionLvl/10`, always kept, one in five copied to the sect library), `enchantWeapon`.
- **Meditate**: `Character.meditate()` = `cultivate(1.5)` and +1 level on each distinct equipped technique below its potential; `practice()` (sparring) is unchanged.
- **Quest Log pruning**: `Character.pruneGoneQuests` / `targetGone` every turn for a MANUAL human (target person dead; Explore Dungeon without a dungeon, Hunt Beast without monsters, Heaven Treasure without the treasure, city quests without a city, sect quests without a sect). `Quest.displayName` shows "Hunt Beast" as "Hunt Monster"; `Quest.canon` maps the typed name back.
- **Meeting a walker**: `Character.interceptAim(target)` (within two tiles, Chebyshev) = `nextStepOf(target)`: their `currentQuest`'s way (`stepTile` mirrors `pathfindXY`), or a player's `autoX/autoY`; `pathfinderFor(quest)` and `PlayerCommands.stepAuto` use it.
- **Monsters**: `Character.dead(killer)` awakens a wild (`masterId < 0`) Beast that killed a Beast-less victim (`awakenMonster`); `winCheck` lets a beast's win be fatal 1 in `BEAST_KILL_ODDS` (100) besides the old level-gap rule. Tamed beasts never awaken.
- **Scripts**: any word that is not a native step (`goto wait forage mine craft meditate hunt sell quest repeat if end mode`) is a `cmd` step run through `PlayerCommands.handle` with `!`; `NOT_IN_SCRIPT` refuses the commands that would end or take over the script; a multi-turn command (walk, wait) is driven by the step until `skipping` is 0. `ScriptStore` keeps named scripts in `scripts/<name>.txt` (one step a line in the words `script list` shows). Commands: `script add|list|del|edit|insert|move|clear|run|resume|stop|save|load|saved|delete`.
- **Results**: `Treasure.announce`, `PlayerCommands` `buygear` / `stay` / `wait` messages, `confirm` prints "Cancelled", `Area.reportToHuman` (tournament window), `Lore.runConvocation` result window; `SilenceProbe`.
- **Dialogue**: `Dialogue` (voices, scenes, hub, `AutoIo`), `DialogueText` (data: kinds x voices; slots `{you} {You} {Name} {me} {place} {season} {sect} {thing} {rumor} {Rumor} {third} {price} {item}`), `GuiDialogue` (portrait window, `decide` for decisions). `Dialogue.approach(npc, human)` is called from `Character.interact` when an NPC speaks to a human; `Dialogue.converse` from the `talk` command; `DecisionBroker.ask(c, prompt, options, policy, speaker, kind)` for the friend / squad / challenge / alliance / tribute / defect / invite questions. Settings `dialogueFlavor` (on), `dialoguePopups` (on), `dialogueAuto` (off). `Relationship.talks / lastTalk / topics / lastAdvice`. A crowded tile gives one scene a turn per human (`LAST_SCENE`); a human on autopilot gets the line only.
- **The four spot quests** (`questInitializer`): Random (the event: beast battle, talk, profession, loot), Profession, Hunt, Meditate. Measured 2026-10-09: 82% of adult NPC turns (Hunt 31%, Profession 20%, Random 15.5%, Meditate 15.3%); `tools/probes/SpotSim`.
## 2026-10-10 (second batch): missions, spot action, seek, spoilage, children, boards
- **Sect mission counters**: `Character.sectMission` is still the board string (`Hunt|Beast|<lvl>|<left>|<bonus>`); `sectMissionTotal` is how many it asked for. `beastFought()` (called by `beastBattleCheck` and for each fighter of `partyBeastHunt`) counts one beast whatever started the fight and pays the bonus at zero. `missionLine()` adds "(n/total done)" or "(carrying n/need)" and is what the Quest Log label, the sect window and `mission` show. `syncMissionQuest` points a Hunt mission at `huntGround()` (nearest known tile with beasts the character can fight) and a hand-in mission at the sect's home; `qdo` there hunts one beast (`targetQuests` case "Sect Mission").
- **Spot action** (`PlayerControl.spotAction`, `SPOT_ACTIONS`): `Character.quest()` runs `chosenSpot(picked)` for a person who plays: if the routine picked Random, Profession, Hunt or Meditate the chosen one runs instead (Hunt with no beasts meditates); the picked quest is still spent or put back. Set with the Quest Log box or `spotaction`. NPCs are unchanged.
- **Seek**: `seek <id> talk` -> "Talk To" (`targetQuests`: the Talk conversation for a human), `court` -> "Courtship" (needs `QuestLife.suitor`), friend / fight / spar as before. Character quests follow a moving target (`Character.quest` re-aims every turn).
- **Explore**: `explore` = `do Explore Dungeon` at the tile's ruins / lair / trial; the effects per kind are in `targetQuests` case "Explore Dungeon". Beasts are a count per tile (`Area.monsters`), not creatures: only awakened Monsters (characters) move (`Seek Territory`).
- **Spoilage refund**: `Life.spoilList(list, shelfOf)` returns the value lost (a shelf: `shopBuyPrice`; else `sellPrice`) times `Settings.spoilRefundPercent`/100. `marketYearly` pays: city market -> `Area.receivePayment` (lord or treasury, a tenth tax), shelf -> the owner (or the city if the owner is gone), hoards -> the carrier (also the surplus above `HOLD_CAP`); `Life.sell` pays the city for the oldest portions over `MARKET_CAP`; `Character.sell` pays the city for an item scrapped over `POOL_CAP`. A sect pantry spoils without payment (nobody paid for it). This mints spirit stones; the setting scales it (0 = off).
- **The Convocation**: `Lore.isEntryTournament(area)` is true for the open Convocation. A character with any sect gets no "Register Tournament" there (`Character.quest`), cannot register (`spotQuests`, `lore register`), has no register quest or 'next:' line (`Lore.convocation`, `questFor`, `registerHint`). Other tournaments (`Hold Tournament`, prize) are open to the realm band whatever the sect.
- **Children**: `Character.ADULT_AGE` 18; `childSafe()` = age under 18, not a Beast / Monster, not reincarnating. `dead()` and `dead(killer)` return at once for such a character; `winCheck` leaves a beaten child injured (cultivation x0.7) with no kill credit; the plague skips them; the epoch does not pick them.
- **Boards** (`Rankings`): once a year `Naming.assignTitles` calls `Rankings.yearly`: `top(all, board, n)` keeps the `n` best in a heap (Strongest by `majorLvl*100000 + lvl*1000 + kills`, Richest by `PlayerLists.wealth`, Most Kills by `kills`; ties to the lower id; beasts, Monsters and Wandering Immortals do not rank; zero kills / no wealth do not rank). `Character.rankStrong` (to 1000), `rankRich`, `rankKills` (to 100). `Rankings.title(c)`: the best tier across boards (1-3, 4-10, 11-100), ties to Strongest, Richest, Most Kills; `Naming.assignTitles` gives it before the older title families (the kill and wealth families are removed). `Rankings.epochSafe(c)`: a child, anyone with a rank, `Lore.onStoryPath` (a Palace member or an NPC seeker / stayer). The epoch skips them when it picks and when it kills. Command `ranking [strongest|richest|kills]` (`Rankings.text`), button "Titled hundred" in the World tab.
- **Tracker**: `MapViewer.updateTracker` shows dead entries with a x and a "Clear deceased" button.
## 2026-10-10 (third batch): chapters for the Story starts
- **Engine** (`Chapters`): `Def` (cast, places, chapters, episodes, epilogue, `begin` / `finish` hooks), `Chapter` (id, title, gap in weeks, minimum age and Major, `when`, `skipIf`, `setup` / `done` actions, intro and outro lines, `Choice` after the intro and after the outro, the task `kind`, `who`, `where`, `n`, `text`, the pay: spirit stones, potential, pills, Potential Pills, gear Majors above, technique insights), `Cast` (key, name, gender, trait, side, `offset` levels above the human, `potBonus`, `rel`, age, `home`, `fated`, `atStart`). `Starts.tick` hands a `v2` life to `Chapters.tick` (every turn of a human): skip the chapters that were left out, open the next if its gap, age, Major and `when` hold (a scene, its quest), close it when the task is done (the second scene, the pay, `done`), and when none is due draw an episode. Scenes: GUI = the Lore window headed "Story" (the game waits per `settings lorepause`) or, for a MANUAL player with a question, a modal window with a button for each answer (`GuiChapter.decide`, honours the decision timeout); terminal = a framed block and `DecisionBroker.ask`; AUTO / HYBRID get the first option.
- **State** (`Starts.State`): `v2`, `open`, `cur` (id of the open chapter or episode), `chapter` (index), `openedWeek`, `lastEnd`, `lastEpisode`, `flags` (choices and results), `seen` (scenes, for `story replay`), `places` (key -> {x, y}), `npcs` (cast key -> character id). `ChapText.refresh` keeps `dead.<key>` current; `Chapters.beaten` (called from `winCheck` when the human wins against a fated foe) sets `beaten.<key>`.
- **Fated people** (`Character.fatedTo` = human id + 1): `dead()` returns for them; `dead(killer)` returns unless the killer is that human; `winCheck` leaves a fated person who lost to anyone else injured; a foe fated to a Story-difficulty hero who beats that hero leaves them alive; `Rankings.epochSafe` includes them; `Chapters.watch` lets one go within two years of `maxAge`; `Chapters.finish` lets all go.
- **Creating a person** (`Chapters.spawn`): `new Character(home)`, the story's name (claimed in `Naming`), gender, trait, side, potential = the human's + bonus + 4 per difficulty (30 to 98), `raise` cultivates to the human's level + offset + 3 per difficulty, age, gear, `storyNpc`, added to `Map.characters` / `Map.population`. `Chapters.pick` finds the tile (`ensureWild` creates a few empty-land tiles in a young world).
- **Text** (`ChapText`): a line is `* narration` or `@Name| speech`; `?flag|`, `?!flag|`, `?flag=N|` prefixes (any number); `{name} {first} {last} {year} {age} {key} {key.first} {key.last} {@place} {#flag}`. `StoryCodas`: `family(endingId)`, `lines(c, endingId)` (added by `LoreEndings.finish` to the ending scene), `credit(c)`.
- **Seasons** (2026-10-19, `Chapters`): `Def.season(n, title)` makes the chapters declared after it a season (`Chapter.season`); `Def.interlude(n, title, lines)` is the scene when season n ends and another follows (`seen` keeps it as `interlude.n`, `story replay interlude.n` reads it); the first chapter of a later season opens with `* Season n: title`; `Def.epilogue` is for the last season. Waiting: `Chapters.PATIENCE` (6 years) lets a `major(m)` gate or a `soft(cond, text)` wish lapse once the gap and 6 more years have passed; `REALM_WAIVER` (8 years) waives a "reach Major N" task (N >= 2); `Chapters.waiting` words the wait (`story`, the Story window). A season is 8-11 chapters in one flat list. Road tales (`episode`) belong to a season; `Def.episodes` counts per season (`episodes.n` flags).
- **People who may be loved** (`Cast.opposite(maleFirst)`): the gender is always the other one than the human's (`Chapters.isFemale/firstName`); text uses `{key.he}` `{key.He}` `{key.him}` `{key.his}` `{key.hers}` `{key.man}` `{key.boy}` `{key.lad}` `{key.sib}`; `Cast.keep()` adds 60 years of life; `Chapters.bond(c, st, key, n)` and `Chapters.wed(c, st, key)` (the same rule as the world's marriages). **Echoes**: `Chapters.echo` refreshes `e.female e.male e.spouse e.kids e.sect e.master e.city e.shops e.beasts e.kills` before each scene (`?e.spouse|`). **Taking from the world**: `Chapters.adopt(c, st, key, "friend|rival|any|kin")` makes a real person tied to the human a person of the story (not protected), `Chapters.deadKin` writes a dead relative's name to `{=kin}` / `{=kinrel}` (`Starts.State.words`), `Chapters.companion` keeps a tamed legend (`Taming.legend`) among the story's people so `{fox.first}` and `?dead.fox|` work. Choice options are filled like prompts. Files: `Tale<Name>.java` (season 1) and `Tale<Name>Seasons.java` (seasons 2 and 3). Tools: `tools/story_lint.py`; docs: `Start Stories Beat Sheets.md`, `Story Voice Cards.md`, `Seasons Work Log.md`.
- **The stories**: see "Story Plan.md" for the truth, cast, flags and choices of each. `Taming.legend` gives the Tamer a named companion whatever the cap.
- **Tests**: `TalesProbe`.
## 2026-10-10 (fourth batch): the Palace text and the Erased
- **Text**: the Palace's words are in `LoreText` (scenes, clues, whispers, omens) and `LoreEndings.registerScenes`; plain voice, see "Lore Design.md". `Lore.CHAIN` has the objective texts.
- **The Erased** (`Lore.State.erasedNext` / `erasedWarn` / `erased`, `PState.remembered` / `lastErased`): `Lore.yearly` calls `erasedYearly(year)` after `omens`. Year >= 30; the first year is 50 + random(40); a warning picks a seeker (`State.seekers`, alive, an NPC; one that a living human has a relationship with, if any) and posts a world line and notices; the next year `erase` removes them silently: for each human who knew them a notice, `PState.remembered` and the scene "erased" (`{erased}` is their name); their lamp, seeker, stayer and pass records are cleared; `Chronicle.log` entries containing their name are removed; `m.reincarnating = true; m.dead()` (no chronicle death line, no death notice, no corpse). The next is 95-105 years later. A seeker who died before their year: the next try is the following year.
## 2026-10-10 (fourth batch, continued): seeker arcs, the Dimming, links
- **Seeker arcs** (`SeekerArcs`, saved in `Lore.State.arcs`, null in older saves): `yearly(s, year)` from year 12 advances the arcs whose `nextYear` has come (`beat` returns ADVANCE / FINISH / HOLD) and starts a new one with 35% a year while fewer than `MAX_ACTIVE` (6) run. `start` fails (returns null) when its people are not there; kinds are tried in a random order. A person can be in only one arc (`busy`). `tell` writes a chronicle line and notices the humans who are Palace members or have a relationship with anyone in it. Kinds and effects: see DEVLOG; `defectorAllies(s, c)` is read by `LoreEndings.npcCross` (two or more living allies: the seeker strikes at the Gardener, death chance 20% instead of 50%).
- **The Dimming** (`HollowCourt`, `Lore.State.dimNext` / `dimmings`): from year 150; `dim` picks the city with most people (not Originis), removes one to three relationships from every NPC there, posts a world line and notices, and queues the scene "dimming" for a human standing there. `LoreText.OMENS_LATE` is used for half of the omens from year 100 (`Lore.omens`).
- **Start links**: flag `sawgourd` (Disciple), `lanterns` (Ghost), `sawshed` (Lucky), `sawlanterns` (Orphan) are set at a chapter's opening from `Lore.pstate(c).found`, and lines tagged with them appear only then.
## 2026-10-10 (fifth batch): GUI overhaul
- **Sheets** (`GuiPages`): HTML pages for `GuiSheets.view` (the Stats / Relations / Journal style). Building blocks: `tiles(perRow, {label, value[, colour]}...)` (Swing HTML tables with `bgcolor` cells; whitespace after a closing inline tag is dropped, so use `&nbsp;`), `tag`, `dim`, `link`, `placeLink`, `kindColor`; `pane(mv, refreshers, supplier)` is a scroll pane that re-reads its page on each refresh and keeps the scroll position. Pages: `familyListHtml`, `pastLivesHtml`, `bioHtml`, `runHtml` / `hallHtml`, `storyHtml`, `legendsHtml`, `digestHtml(weeks)`, `auctionHtml`, `sectHtml(org, viewer, detail)` (compact banner or full), `cityHtml(viewer, area, detail)`, `placeHtml(area, viewer)`, `dashHtml`. They only read the world (they run in `GuiWindows.window`'s `World.read`). The terminal keeps its text builders (`TextTools`, `Family`, `RunStats`, `Auctions.text`).
- **Item cards** (`GuiItems`): `Entry` (an item with its bag place, or a section header), `Renderer` (JList cells; BorderLayout, because a BoxLayout clipped bold titles at the first measured width), `WornCard`, `fill` keeps selection and scroll across refreshes, `selected(list)` gives the 0-based bag place for the commands that take a number. `grade(lvl)` = realm of the item level (`REALMS[lvl - 1]`); food, wine, poison and blood show "level n" instead.
- **Edit Character** (`GuiEditSelf.show(owner, c, creation)`, modal, on the Swing thread via `edtWait`): changes are written on Done; Cancel restores the character (the trait and gender are applied live for the portrait). `INFO` holds {trait, group, text}.
- **Script Manager** (`GuiScripts.open`): the script is still only changed by `script ...` commands; the saved files are `ScriptStore` (plain text in `scripts/`), `read` previews and `rename` renames a file. "Unsaved changes" compares the steps with their state when last saved or loaded.
- Probe tooling: `tools/probes/GuiShots2.java` (`-Dshots=<dir> -Donly=backpack,editself,scripts,family,bio,run,story,chronicle,digest,market,manage,squad,place,rankings`) renders the windows to PNG with test data; GUI probes send commands through `mv.queueInput` (stdin is not read in GUI mode).
## 2026-10-10 (sixth batch): size, memory, saves, defence, feeding, skin
- **Size of the world**: `Map.startingPopulation` is now a variable (default `Map.DEFAULT_START` = 100,000), set by `StartMenu.askPopulation()` before the first tile is made (console: a number, anything else = the default; GUI: a dialog with a spinner and a live warning outside 10,000 - 150,000) and by `settings startpop`; `Map.setStartingPopulation` recomputes `GameDriver.CROWD_CAPACITY` (2% of the start x 961 / tiles) and `CROWD_CAPACITY_CITY`. Used by: the tile capacity, the monsters / herbs / metal of a new tile (`Area`), the Palace's quota (`Lore`), the start spread (>= 20,000). `SaveData.startPop` keeps it (0 in old saves = 100,000). The epoch limit is `Settings.epochLimit()`: twice the start, or `epochPopulation` once `epochCustom`.
- **Saves**: `SaveManager.writeSlot` writes gzip level 1 under the checksum; `openSave` reads gzip or the old plain stream. A year-560 world: 1,377 MB -> 270 MB.
- **Memory** (`Slim`, `MemWatch`): see the DEVLOG entry; numbers: the year-560 world (182k cultivators, 108k beasts, 58k dead kept by kin) held 2,493 MB alive, 1,434 MB after one year of Slim; a new 100,000 world holds 794 / 1,040 / 1,341 MB at years 15 / 30 / 45 (1,437 MB at year 30 before); `MemWatch.full()` (once a year in the epoch check) reads the live heap after a full collection when the heap looks over half full. `Character.husked`, `Character.trimKnown`, `RelationshipList.trim` (faint ties to the dead, the cap), `Slim.trim` (copies of an errand, stacks at 0, materials, interned makers), `Slim.yearly` (husks for the dead still held by kin, counts the beasts kept: `Taming.keptCount`). Constants: `RelationshipList.CAP` 40 (`-Dtdf.relCap`), `Character.KNOWN_CAP` 80 + 16, `TARGET_QUEUE_CAP` 24, `WEAK_SOCIAL_CAP` 2, `Slim.SAME_ERRAND` 3, `Taming.NPC_SHARE` 12 (%), corpses per tile 12. City-born cultivators (`Area.age`, 10% a tick for every tile with population > 0) are the main inflow of people: ruined cities keep their 10% and keep producing them.
- **Defence** (`Defense`): `onAttack(attacker, victim)` is called from `Character.battleCheck` when a real fight starts; `defended(area)` = a sect's seat (not the Palace) or a city with a living lord; `alarm` sets `Area.siegeUntil` (13 weeks, no second alarm), notifies, posts Defend errands (`circle`: members, or lord + shop owners + lord's sect + friends with value >= 3, within 8 tiles; a person who plays only if master / lord or near), then `fightOff` (<= `MAX_FIGHT` 3 fighters, the strongest first, attacker tired by `exhaustionPenalty`, a non-Evil attacker makes the Good hate it first). The "Defend" quest: `questViable` / `targetGone` while `Defense.stillOn(area)`, `targetQuests` calls `Defense.arrive`, classified SECT or CITY (`PlayerControl.classify`). Tuning: `-Dtdf.defSiege/defCalled/defFight/defKills/defPower`. Counters `Defense.counts()`.
- **Feeding**: `PlayerControl.FEED_POLICIES`, `Taming.autoFeed` (every `FEED_EVERY` 4 weeks per beast; leftovers keeps `KEEP_FOR_SELF` 3 portions; the cheapest food first for leftovers, the best for always).
- **Portrait skin**: `Character.skinTone` 0 = from the portrait's seed (the five old tones), 1-9 = a chosen tone; `ArtMore.portrait` always draws the seed's tone so the rest of the face stays the same; the portrait cache key includes it.

## Walks, quest creation and the turn cost of commands (2026-10-13)
- **A walk ends with a prompt in the same turn.** `PlayerCommands.stepAuto(c)` returns true when it ended the walk in that call (arrived, caught up, target gone, gave up after 150 turns). `GameDriver.manualTurn` then falls through into `promptLoop` in the same turn. In a chase the target is checked first: if it is already in the player's tile the chase ends there, before `interceptAim` aims at the tile it will step to. A chased NPC has only *planned* its step (`pendTo`; steps are taken after the people who play act), so the player acts while it is still there. Before, the walk was noticed at the start of the next turn, that turn passed with no prompt, and the NPC stepped away (17 of 21 "You caught up" lines left the target a tile off; now 7 of 7 in `ArrivalProbe`). A plain `goto` of N tiles now costs N weeks, not N+1. Walks started by scripts (`PlayerScript` "cmd") keep their own flow.
- **One door for a human's quests.** `Character.addPlayerQuest(q)`: looks in the current quest, `targetQuests` and `spotQuests` for the same name (case-insensitive), category and target (person id or tile); if found it adds nothing, raises the old quest to the higher priority (`pin`) and sets `questAddWasDuplicate`; callers (`seek`, `qnew` via `PlayerControl.createWaypoint`) say "Already in your Quest Log". The queues are `PriorityQueue`s, so a priority change takes the quest out and puts it back. `dedupePlayerQueue()` runs once per load for a human (`PlayerControl.dedupedAtLoad`, transient) and keeps the highest priority of identical copies. `do <quest>` never queues a quest (it runs a throwaway one now) so it has no dedupe.
- **Turn cost of commands** (`PlayerCommands.dispatch` returns true = the week passes). **Free:** every info command, `seek`, `qnew`, quest-log commands, `buy`, `sell`, `buygear`, `learn`, `gift`, `feed` (once per companion per `Taming.FEED_EVERY` 4 weeks: `Character.fedAt`, `Taming.feedWait`; with no number it picks a hungry one), `eat`, `drink`, `coat`, `consume`, `install`, `equip` (bare or `equip <n>`), `join`, `resign`, `recruit`, `war`, `peace`, `alliance`, `invite` (see below) and a `hunt` that fights nobody. **A week:** moves, `x`, `wait`, `watch`, `goto`/`goquest`/`chase`, `meditate`, `forage`, `mine`, `hunt` (only when a fight happened, won or lost), `explore`/`do` when done, `craft`/`profession`, `make`, `poison`, `perform`, `play` (a Musician's extra cultivation), `tame`, `talk`, `spar`, `attack`, `found`, `reincarnate`, `control`, `qdo`. `invite` costs nothing; a person who declined is not asked again for `PlayerControl.INVITE_WAIT` (4) weeks (`PlayerControl.inviteRefused`, person id -> week; null in old saves); a party at `partyCap()` (4, 12 for a Leader without a throne) takes no one more.
- `gift` hands the item over every time, but warms the friendship (+1) only once per person per week (`PlayerControl.giftWeek`, person id -> week; null in old saves).
- **Results are never silent (2026-10-13).** `Character.tell(text)` says a line to the person who plays (nothing for an NPC or while an AUTO run is silenced). `Explore Dungeon` reports each of the five kinds (Lost Ruins: the coin carried out; Beast Lair: beasts fought and left; Martial Trial: the chance and pass/fail; Haunted Grounds and Celestial Trial: the cultivation bar before and after; +10 sect points). `Character.ledger()` / `ledgerNote(before)` snapshot what a quest can change (coin, backpack, pills, goods, herbs and metal, level and bar, techniques gained, sect points, potential, kills, arrays, relationships, sect), and `do <quest>` and `qdo` / Do Quest add a `Result: +14,400 ss, cultivation 73% -> 76%` line when something changed. `do` says "under way" for a quest that takes more than a week (result 0) and "nothing came of it this time (reason)" for a failure; `Loot Corpse` says whether there was a corpse. `QuestSilenceProbe` finds quests that change the snapshot while printing at most one line (0 now).
- **The prompt.** `GameDriver.promptLoop` prints one line (name, realm and place) when `TermHud` is not showing its block (the GUI, `settings hud off`); the backpack, herb and metal bag, pill bag and spouse lines that used to follow were 35% of a manual week's console text. `bag` and the Backpack window show them. `PrintBudgetProbe`: a manual meditating week prints 3-4 lines (was 7), an AUTO year under 1 a week.
- Probes: `ArrivalProbe`, `DupQuestProbe`, `TurnCostProbe`, `QuestSilenceProbe`, `PrintBudgetProbe` (`tools/probes/`).

## Saving at the prompt, paused commands, world news (2026-10-13, change set 3)
- **Saving.** `GameDriver.servePendingSaves()` writes a requested Save (`saveRequested` for the quick slot, `saveRequestName` for a named one) between steps: at the top of a tick (as before), in the pause loop, in `GameDriver.promptLoop` right after each command, and in the GUI `readLine` (it now polls the input queue every 150 ms instead of blocking, so a Save button clicked while the game waits for your command is written at once, with no move). `flushPendingLists()` first does the end-of-tick bookkeeping that a save in the middle of a week would otherwise lose: `mergeBirths()`, the dead (`remCharacters`) leave `Map.characters` (population -1), awakened monsters leave `monstersList`. Loading a save made while the game waited for a command **repeats that week** (the tick starts again at the same week: the NPCs act once more, `yearlyDone` is saved so the yearly events do not run twice); a save made while paused is exact.
- **Paused.** `PlayerCommands.runsWhilePaused(line)`: `INFO_VERBS` plus `FREE_VERBS` (seek, qnew, quest-log commands, settings, duties, policy, music, save, notes, follow, ranking/board, price, release, leave, disband, promote, invite, buy, sell, buygear, learn, gift, feed, eat, drink, coat, consume, install, equip, unequip, join, resign, recruit, war, peace, alliance, `e` (Edit Self), export, tutorial, guide, `z`, `f`, the reading subcommands of `lore`, the editing subcommands of `script`) run at once in `GameDriver.servePaused(who, queue)` while the game stands in the pause loop (`GameDriver.pauseWaiting`); every other command (moves, work, fights, `do`, `qdo`, `make`, `script run`...) stays in the queue in order and a non-modal "Paused" window says so once per pause (`showPausedNotice`); they run when the game resumes. This is what lets Edit Self, the Titled hundred (`ranking`) and the settings work in the middle of a `watch`/`wait`/`auto` run.
- **World news.** `Say.world(text)` for news of the wider world (tournament winners, cities founded by NPCs, a closed Celestial Trial, a Beast Tide, a spiritual storm, Heaven fire); `settings worldnews on|off` (default on, `Settings.worldNews`); off sends the line to `GameDriver.logEvent` only (history and Chronicle keep it). The `[Notice] New quest alert` echo is gone (`Notifications.postQuiet`): the `[Quest Alert]` line says it.
- `GameInfo/Print Review.md` lists every `System.out.print*` call of `src/Game` with its kind and a proposed action (generated; 336 calls).
- Probes: `PausedProbe` (headless: the free/queued table, `servePaused`, save at the prompt, the pending death flushed, load back) and `PausedGuiProbe` (GUI: Save at the prompt, the Titled hundred, Edit Self and Save while paused in the middle of a wait, the Paused window for Meditate, the queued command running after resume).

## Music engine: life cycle, time stamps, tuning (2026-10-13, change set 4)
- **One engine at a time.** `Ambient.apply()` only queues work on a single control thread (`music-control`); `applyNow` / `startNow` / `stopNow` run in order and a stop *joins* the old `ambient-music` thread before a new one starts. Each music thread carries a `generation` number and ends (and never writes `running` or `status` again) when its number is stale. Before, `start()` ran while the old thread was still closing, the old thread's last act (`running = false`) switched the new one off, and quick Off/On clicks or Settings changes left several engines playing (measured: six threads, then silence). This is the best explanation of the tester's "Next plays two songs" and of music that sounds muddy until the application is restarted (a fresh process starts with one engine). `Ambient.threadsAlive()` counts them; `music status` prints it.
- **Notes carry time stamps.** `Ambient.midiLoop` uses the synthesiser's own audio clock (`MidiOut.micros()`), polls the Conductor `LOOK_MS` = 250 ms ahead, and hands each note on and off to the synthesiser's `Receiver` with the time stamp of its start and its end (`MidiOut.on(inst, midi, vel, at)`, `off(channel, midi, at)`; channel set-up messages carry the same stamp). Gervill plays them sample-exactly, so a late wake-up of the loop moves no note. The loop sleeps 15 ms (was 8) and runs at normal priority. Measured with 40 busy threads on 20 cores: 0 notes sent late, the longest pause of the loop 78 ms. A cut (danger music taking over) cannot recall notes already handed over: up to 250 ms of them still sound.
- **Tuned synthesiser.** `MidiOut.openTuned()` opens Gervill with 256 voices, sinc interpolation and a 250 ms buffer, the way the WAV render does (17 of the 70 songs need more than the default 64 voices). Those are options of `SoftSynthesizer.open(line, info)`, reached by reflection; Java 9 and later hide that package, so there the plain `open()` is used (64 voices, linear) and `music status` says so. `tools/make-exe.bat` now passes `--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED`; Java 8 (the IDE) needs nothing; an IDE run on a newer Java needs that VM option.
- **Cores.** `Map.tickPool` has one worker fewer than the cores (when there are more than two) so a core stays free for the audio threads; `-Dtdf.threads=<n>` overrides.
- `music status` shows: on/off, status, engine and tuning, music threads alive, notes sent, notes sent too late, the longest pause of the note loop, the most voices used at once.
- Probe: `MusicLifeProbe` (needs an audio device and `soundfonts/` in the run folder; run once with and once without `--add-exports java.desktop/com.sun.media.sound=ALL-UNNAMED`).

## Saves made in the middle of a week (2026-10-13)
A save made while the game waits for the player's command is marked `midWeek` in the slot's `.meta` (`SaveManager.savingMidWeek`, `Slot.midWeek`); lists show `[mid-week]`, the start-up menu, the Saves window and the `load` command carry `SaveManager.MIDWEEK_NOTE`. **Side effects of playing that week over:** every person's own week counter and age advance once more (`Character.time()`), everyone cultivates and acts once more, the world's dice are rolled again; the calendar (`GameDriver.time`) does not move, so after one such load everybody is a week older than the calendar says. Saves made while paused or at the top of a tick are exact and are not marked.

## One scene window and the after-death window (2026-10-13, change set 5)
- **`GuiScenes`** is the only place a story or Lore scene opens. `GuiLore.scene/story/ending` and `GuiChapter.decide` enqueue a `GuiScenes.Scene`; one non-modal window shows them one after the other (`GuiScenes.busy()` is true from the moment of the call, not when the window appears: the old counter was raised on the Swing thread, so a burst of scenes in one tick opened several windows). A scene is cut into **pages** (`GuiScenes.paginate`: a new page starts at narration that follows spoken lines, at most 8 lines a page); the window shows one page with "Back", "Continue ▸" and "page n of m", the last page shows the closing button (Continue / Close / Credits ▸) or, for a scene with a question, the prompt and one button per answer. The X takes the default answer (or just closes a plain scene); the decision timeout does the same.
- **A question** (`GuiScenes.ask`, called by `GuiChapter.decide` from `Chapters.play`): the game thread waits (world unlocked) on a latch; the window stays open after the answer and `Chapters.play` hands it the text that follows that answer (`GuiChapter.afterChoice` -> `GuiScenes.supplyAfter`), which appears as more pages of the same window (before: a second window). Not modal any more.
- **Pause and credits:** `settings lorepause` is held from the first queued scene until the last is closed; the credits window (`GuiLore.credits`) calls `GuiScenes.hold()` / `release()` so the world does not quit under them.
- **After death** (`GameDriver.deathDialog`): a modal window that the X and Escape cannot close, with the choices (take over the killer / a child / any NPC by id, Spectate), **Save and quit the game** and **Quit the game (without saving)**; the terminal menu has the same two extra numbers. `GameDriver.slowForSpectating()`: choosing to spectate (or a Brutal death) sets the speed to 2 s a week in the windowed game (`tickDelayMs`), unless it is slower already. A Brutal death now shows the same window (Spectate / Save and quit / Quit) instead of a message box.
- Probes: `SceneWindowProbe` (GUI, 21 checks) and `DeathMenuProbe` (GUI, 9 checks; `-Ddeath.choice=Save|Quit` presses those buttons and the program must end). `PcProbeGui.IGNORE` is a set of window titles the dialog watcher leaves alone.

## The packaged app's folders (2026-10-13)
`tools/make-exe.bat` builds `dist/TalesOfDreamAndFantasy2/`. The game reads `soundfonts/`, `assets/`, `images/` and `songs/` and writes `settings.properties`, `saves/` and `exports/` relative to its **working folder**, which for a double-click is the folder of the `.exe`; the launcher does not change it. So the four read-only folders are added with `jpackage --app-content` (JDK 18+) beside the `.exe`, not inside `app/` (where `tdf.jar`, the FlatLaf jar and the .cfg live). If music in the packaged game is the built-in synthesiser, `music status` says `engine: built-in` and the `soundfonts` folder is missing or the game was started from another folder.

## Squads of a person who plays (2026-10-13, change set 6)
- **Members stay.** In `GameDriver.npcTurn` a member whose leader is a person who plays and is not on AUTO (`leader.player == 1 && pc.mode != AUTO`) no longer compares its errands with the leader's `currentQuest` (a MANUAL leader has none, so every member with an errand left within a week: measured 0 of 4 after a week). It leaves only when dismissed, when the leader dies, or by the leader's `leave`/`disband`. Its turn is `quest()` with `Character.squadBound` set (`planMove` then plans no step, so the errand cannot walk it away; spot work on the tile still happens), then `ride(leader)` when it stands in the leader's tile (`applyRide` moves it wherever the leader's step took him: members stand in the leader's tile at every prompt, diagonals included), or a step toward the leader when it is elsewhere. Their errands wait, they are not thrown away. AUTO leaders keep the old NPC rule.
- **Orders** (set by the leader, saved on the member): `Character.squadOrder` Follow (0) / Hold (1) and `squadRole` strongest first (0) / fights first (1) / reserve (2). Hold: no ride, no step, sits fights out, stays in the squad (Follow calls it back: it walks to the leader when he stands still). `cancelRide()` makes an order given at the prompt take effect for the step the leader is about to take (the NPCs plan their rides before the leader acts). `kickFromSquad` is plain removal (orders reset, no relationship change).
- **Fights.** `Character.squadHere()` = members in the leader's tile that follow orders. `partyBeastHunt(withSquad)`: leader + members, ordered by role then strength (`sortForFight`), each fighter has its own credit, the party needs a member who could take the beasts alone. `squadAttack(foe)` (the `attack` command): the members in the tile fight the foe in turn with the leader (fights-first, then by strength, the reserve last), `opponent.exhaustionPenalty = 2 per earlier fight`; a member will not fight a friend (relationship >= 1); a truce, or a leader's fight that does not take place (`foughtAt/foughtWith`), stops the squad too; the cascade stops as soon as the foe is dead or beaten (`lostAt/lostToId`, set in `battleCore`), and goes on when the foe fled or the fighter lost. `spar` stays one-on-one.
- **Choice.** `PlayerCommands.bringSquad`: `alone` / `squad` on the line, else `PlayerControl.squadFight` (`policy squadfight` / `squad fight join|ask|alone`; ask = "Call the squad" / "Go alone" through `DecisionBroker`). Commands: `squad` (the list with numbers, orders, where each is), `squad kick|hold|follow|front|normal|reserve <n|#id|all>`. The Squad & Sect window shows every member (also those elsewhere) with Order, In a fight and Where, buttons Follow / Hold here / Fights first / Strongest first / Reserve / Kick for the selected member and a combo for the fight policy.
- Probes: `SquadProbe` (headless, 28 checks) and `SquadGuiProbe` (GUI, 12).

## Squads, second part: Work order and defence (2026-10-13, change set 7a)
- **Work** (`Character.ORDER_WORK`, `squad work <n|all>`, the "Let them work" button): the member goes about its own errands like any NPC (`actThenWander`, no `squadBound`) but stays in the squad; it takes no part in fights while away; Follow calls it back. The follower-quits rule is not applied to members of a person who plays.
- **Defence** (`Character.squadDefend(attacker)`): when an NPC or a beast attacks a person who plays (`battleCheck`, just before the fight with him; not when he declines the duel), the members beside him (Follow) fight the attacker first, in turn (fights first, by strength, reserve last; the attacker tires 2 per earlier fight; no member fights its own friend); once one beats it, the attack ends and he is not fought; if none does, it goes on against him. He is told (`tell`) who stepped in and who won. Hold and Work members do not defend.
- Probe: `SquadProbe` now 36 checks (Work, defence).
- Squad orders are now Follow / Hold / Work (`Character.ORDER_NAMES`).

## The one list of people (2026-10-13, change set 7a)
`GuiPersonList.create(mv, Kind.HERE | MONSTERS | WORLD, refreshers, inBrowser)` replaces three lists that had grown apart: `GuiCards.peopleTab` (People here), the people branches of `MapViewer.buildBrowserTab` (Browser > Cultivators / Monsters) and `GuiExtras.searchTab` (Browser > Search anyone). It has the filters of all three (name, level from / to, alignment, extra list-language filters, sort and desc, limit — the spinner starts at the player's `listLimit`), sorting by clicking a header, colours by column name (name by alignment, quest targets gold, bond green / red), a double-click or Enter opens the sheet, the card and portrait of the selected row for the people in the tile, and **one button bar**: Open, Talk, Spar, Attack, Invite to party, Preview fight, Chase, **Follow / Unfollow as one toggle** (the label follows the selected person; the card dialog has the same toggle, and both send `follow` / `unfollow` commands instead of writing the tracker from the Swing thread), Control, **Seek ▾** (a menu: Befriend, Talk, Court (enabled for a suitor), Spar, Fight), Go there, Copy ID; and Refresh / Search, Set as default limit, Export CSV, Open in Browser (People here only). Talk, Spar, Attack and Invite are same-tile actions: for someone elsewhere the status line says so and suggests Seek or Chase. Monsters get Open, Attack, Hunt here, Preview, Chase, Follow, Go there, Copy ID. WORLD searches only on demand. Probe: `PersonListProbe` (GUI, 21 checks).

## Seek: kill, Known people, the Items window (2026-10-13/14, change set 7b)
- **Seek: kill (the quest `Hunt Down`).** `seek <id> kill` (alias `hunt`), the Seek menu of every person list ("Kill (hunt down)") and "Seek: kill" in the Monsters bar pin a user quest through `addPlayerQuest` (no second copy). Anyone can be the target - a child, a monster, a friend - except another player and a person fated to somebody else's story (`fatedTo`). With the target in your tile, `qdo` asks Kill / Spar / Spare / Not now: Kill is a fight the hunter has chosen (`Character.killIntentId`: no alignment scruples and no friend rule in `battleCheck`, `dead(killer)` bypasses `childSafe`, `winCheck` asks no second "kill or spare?"), Spar and Not now keep the hunt, Spare ends it. The chase of a hunt (`chaseHunt`) has no give-up after 150 turns; a plain `chase` keeps it. Without the hunt a child is still protected. Probe `HuntDownProbe` (16 checks).
- **Known people.** `PlayerLists.known(...)` lists everyone alive with whom the player has a bond (family included, bond or not) with a Relation column (spouse, mother, father, child, sibling, or the bond word); default order family first, then the strongest bonds, then level. `GuiPersonList.Kind.KNOWN` (a "show" combo: everyone / friends / rivals and enemies) and `Kind.FAMILY` (kin only) are the tabs of the Relations window (`GuiSheets.relationsWindow`) and the Family list / Relationship list tabs of the family window (`GuiExtras`); same button bar as the other lists. Both can be far away: Talk, Spar, Attack, Invite say they need the same tile; Seek, Chase and Go there work.
- **The Items window** (`GuiItemList.create(mv, refreshers)`): three sub-tabs on the item cards of `GuiItems` - *Mine* (the backpack with the change in fighting level; Equip, Sell, Gift, Consume), *For sale here* (the shops of this city; Buy; "affordable"), *Sell here* (the backpack priced by the shops against what the pool pays; the **Only good prices** toggle keeps items a shop pays more for than the pool: `Shop.buyPrice` of the best shop with `buyRate >= 0.5` against `itemPrice/2`). Same filters and sort as the terminal `list` (name, type, level, price, sort, desc, limit); every action is a command (`equip n`, `sell n`, `gift <p> n`, `buy shop.item name`, `buygear`, `consume`). It is Browser > Items (replacing the Backpack and Shop items text tables) and the Market's new "Shops" tab (after Goods). The Browser now has Cultivators, Monsters, Items, Places, Search anyone. Probe `ItemListProbe` (19 checks).

## The Places window and the attack rule (2026-10-14, change set 7c)
- **`attack` and alignment.** A plain `attack` typed by a person who plays no longer stops at the scruples of alignment (a Good hunter would not strike a non-Evil stranger): `Character.attackChosen` is set around the command (`PlayerCommands`, case `attack`) and `battleCheck` skips only that check. The friend rule (relationship >= 1), the first-year truce, array protection and `childSafe` stay. A Hunt Down fight (`killIntentId`) additionally ignores the friend rule. Probe `HuntDownProbe` H7.
- **The one list of places** (`GuiPlaceList.create(mv, refreshers, inBrowser)`): Me > Locations (`GuiSheets.locationsWindow`, a normal window, key `locations`, press L) and Browser > Places are the same table. `PlayerLists.places` gives it the columns # / Name / Coords / Dist / Lv / Type / What / Beasts / Note (`What` = city and population, sect, dungeon kind, spirit vein, or the biome and river of a wild tile: `PlayerLists.placeWhat`) and the new keys `noted` (1 when the tile has a note) and `vein` (1 for a spirit vein) next to name, dist, lvl, type, pop, beasts, x, y; so `list places noted=1 type=city sort=dist` works in the terminal too. Filters: name, type, dist, lvl from / to, spirit veins, noted only, extra list-language filters, sort and desc, limit (starts at the player's `listLimit`); a click on a header sorts. Buttons: Go there (`goto`), Route, Add waypoint quest, Details (a city or sect sheet, otherwise selects the tile on the map; a double-click or Enter does the same), **Note…** (asks for the text) and **Remove note**, Refresh, Set as default limit, Export CSV, Open in Browser (not in the Browser). A noted place has its name and note in gold; cities are green, dungeons red. Notes are the bookmarks of `Chronicle.notes`: new commands `note tile <x> <y> <text>` and `unnote tile <x> <y>` (tile numbers) mark and clear a place anywhere (`TextTools.note/unnote`). The Browser's old table builder (`MapViewer.buildBrowserTab`) and the HTML Locations page are gone. The two maps (Overview, Painted map) stay separate windows. Probe `PlaceListProbe` (20 checks).

## Wrapped tables, double-clicks and portraits (2026-10-14, change set 7d)
- **One wrapping table** (`GuiWrapTable`, made by `GuiWindows.table()` so every list window has it): a String cell too wide for its column is drawn as wrapped text (a `JTextArea` with the colours, font and background the table's own renderer gave the cell; a cell that fits keeps its renderer) and the row grows to the wrapped height (`wrapLines` counts the lines with the font metrics; rows are refitted when rows, sort or column widths change; short rows stay at the normal 20 px). The first time rows arrive after a structure change the column widths follow the contents (short columns stay short, the long text columns share the rest, a column a window capped keeps its cap), so only truly long text wraps. Not applied to HTML cells, tables over 1,500 rows, or numbers. The recap is still the plain text area (it moves into the what-happened merge, 7e).
- **One sheet per person, no click-through.** `MapViewer.buildCharacterDialog`: asking for the sheet of a person who already has one open raises it; a request within 600 ms of any sheet closing opens nothing (a double-click on Close or a chattering button put its second click on the list below, which opened a new sheet, and again).
- **A portrait with every decision about a person.** `DecisionBroker.askAbout(c, prompt, options, default, person)`: the plain GUI window (the one used when Dialogue pop-ups are off or there is no spoken line) carries the person's portrait; the Kill-or-spare after a win, the flee question, and the Hunt Down question use it; `ask(..., speaker, kind)` falls back to the same window with the speaker's portrait. Squad, friend, duel, alliance and tribute offers already had the speaker.
- **Double-click everywhere it means something:** Chronicle (the first living person in the event), Notes (the noted person or tile: the Go there action), Rivalries (the city or sect sheet: `GuiMore.rivalriesPanel(mv, ...)`), Manage > My shops (the city sheet) and Manage > Sect members (the person's sheet) (`GuiWindows.onDoubleClick`); the lists, Quest Log, Squad, Rankings, Notifications and the list windows already had it. Saves and the item tables of the Market keep none on purpose (loading and buying are not a double-click).
- Probe `WrapTableProbe` (GUI, 19 checks): the same 300-character event at three widths, the colour of a wrapped cell, the Chronicle, a sweep of every window's tables (no clipped cell), the double-clicks, one sheet per person and the click-through guard, the decision portrait.

## One window for what happened (2026-10-14, change set 7e-1)
- **The Chronicle window** (`GuiMore.chronicleWindow(mv, tab)`, key `chronicle`, shortcut C) has the tabs **World** (the searchable record of the world, was "Chronicle"), **Digest** (a season, a year or five years; was its own window "What happened lately"), **Journal** (your own story: your chronicle entries, notices, life so far; was its own window), **Recap** and **Hall of legends**. `digestWindow(mv, weeks)` and `GuiCards.journalWindow(mv)` only open the window on their tab (the dock's Digest and Journal buttons and the Help links still work); the window is a singleton, so asking for another tab selects it. The notices (what was reported to you) stay their own window; Notes (bookmarks) too.
- **The recap is a card.** `Recap.html(h, base)` lays out the same facts as the console text (`Recap.build`): you, wealth and kills, family and friends, the world, what happened to you (links to the people), notable events. `Recap.cardHtml(h)` is the Recap tab: the period so far, then the last delivered recap (`Recap.lastBody`; `Recap.deliver` keeps it). The pop-up (`GuiExtras.recapWindow(mv, title, html)`) shows the same card in a scrolling page with the stop check box and two buttons: Chronicle (opens the Recap tab) and Notices. The terminal `recap` / `journal` / `digest` commands are unchanged.
- Also fixed: Export CSV of Search anyone wrote `world*.csv`; it is `search*.csv` again (`GuiPersonList`).
- Probe `ChronicleMergeProbe` (GUI, 16 checks). `MoreGui`, `QolGui` and `GuiShots2` were updated to the new titles; `QolGui` accepts a game paused by a notice as alive (a week of the seeded world can end in an auto-pause).

## The rest of the windows, and the control features (2026-10-14, change sets 7e-2..4, 8, 9, 10)
- **One home for each window (7e-2).** The dock's Move tab holds People here, the Quest Log and Alerts; the Me and Auto tabs only link to them (`GuiDock.linkRow`). The Digest, Journal and Titled hundred buttons are gone (Digest and Journal are tabs of the Chronicle window, the titles are in Rankings).
- **One Rankings window (7e-3).** Tabs: Highest level, Fighting level, Richest, Most kills, Oldest, **Hall of legends**, **Hall of fame**, Charts, Dashboard, Rivalries (the Chronicle window and Run statistics lost those two tabs). The `ranking` command opens it in the GUI (text with an argument).
- **The Control window (7e-4)** (`GuiDuties`, key `control`, the U key, `Control…` in the Auto tab; replaces the Duties and Policies buttons). Tab 1 *Chores and answers*: the ten duties, the policies, and the **spot action**. Tab 2 *Priorities* (`GuiPriorities`): the breakthrough policy and the priority rules.
- **Priority rules.** `PlayerControl.prioRules` (type | scope -> 0-100; `*` = any type; scope = nothing, `kind:<family|friend|enemy|sect|stranger|city|sect|dungeon|wild>`, `id:<person>`, `tile:x,y`); the most specific wins: this type and this target, any type and this target, this type and this kind, any type and this kind, this type. `Character.applyPriorityRules` (game thread, start of every quest turn and when a rule changes) gives the matching quests the rule's number with no ageing (`Quest.ruled`, original numbers kept in `origPriority/origMax/origDiff` and put back when the rule goes); pinned and own quests are never touched; NPCs and AUTO-less characters are unaffected; a new body inherits the rules (`inheritFrom`). Command `qprio <type> [@kind | #id | at:x,y] <0-100|off>`, `qprio`, `qprio reset`; the Quest Log's **Always ▾** menu and **New quest…** button. Probe `PrioProbe`.
- **Breakthrough choice.** A person who plays in MANUAL does not break through by himself: the bar fills and stays full at its cap (`Character.heldBreakthrough`, `breakthroughReady`); at the next prompt `offerBreakthrough` asks once per full bar (Break through now / Not yet / Not yet and do not ask again) with `breakthroughNote`: the potential against the gate, the odds (1 in 15, 1 in 10 from Major 6, 1 in 5 for the last realm), the inhibitor a failure costs and the tribulation's odds. `breakthrough` command and the Me tab's Breakthrough button (`breakthroughNow`; no week is spent). `policy breakthrough ask|auto|button` (default ask; Control > Priorities). AUTO, HYBRID, NPCs and policy auto break through as before. Probe `BreakthroughProbe`.
- **Crafting menu and Potential Pills.** `CraftMenu` (+ `GuiCraft`, Profession ▾ > Choose what to make…, `craft list`, `craft do <n> [kind | count]`): for a Blacksmith, Tailor, Scribe or Alchemist one row per usable stack with what it makes at his skill and realm (an item is made in the upper half of the quality's realm, capped by the maker's realm; a pill has the herb's quality); a weapon may be a chosen kind; one craft is one turn (a turn's skill allows several pills). `Character.smithing(type, stack)`, `tailoring(stack)`, `array(stack)`, `alchemyOf(stack, count)`. **Making Potential Pills makes the maker Evil** (`stainPotentialMaker`: the menu, the game's own `alchemy()` and refining someone's Blood; only the production does it, never eating the pill or gaining Blood; nothing turns it back; applies to NPC alchemists too). Probe `CraftMenuProbe`.
- **Randomize** in Edit Character (`EditRandom`, ticked fields: Name, Skin, Trait, Potential (only when it may change), Alignment; Gender off by default) and `(R)andomize` in the terminal editor. Probe `EditRandomProbe`.
- **Rankings: four boards.** Highest Level (was Strongest), Highest Fighting Level (new, `Character.rankFight`, saved; 0 in old saves until the next count), Richest, Most Kills. `Rankings.fightRating(c)` = level + worn gear + enchantment + the two chosen techniques + 5 per point of lasting bonus (blessing, Cheater, Iron Body, companion beasts), read without equipping anything; the thousand best of each level board are spared by the epoch, the first hundred of every board get a title. `ranking [level|fighting|richest|kills]`. Probe `RankFightProbe`.
- **Spot actions.** `PlayerControl.SPOT_ACTIONS`: Meditate, Hunt, Profession, **Surprise me** (the old Random, same index: a trade turn, a talk or a meditation for a person who plays; never a fight, never looting the dead), **Talk** (`Character.safeTalk`: the friendliest person here, never a fight), **Spar** (`safeSpar`, nobody dies), **Forage**, **Mine**, **Explore** (each falls back to meditating when the tile has nothing). `spotaction` takes the names (the first that begins so; `random` still works). Probe `SpotActionProbe`.
- **New quest by hand** (`PlayerControl.createQuest`, `qadd`, `GuiNewQuest`): every type in `QUEST_TYPES` except the game's own (Sect Mission), about a person, a place or nothing (`PERSON_TYPES`, `SPOT_TYPES`), pinned and its own, through `addPlayerQuest` / `addPlayerSpotQuest` (no second copy). `qadd <type> [#id | name | at x y | here] [p=n] [repeat] [go]`. Probe `QuestAddProbe`.
- **Gifts** (`Gifts`, quest `Give Gift`, `askgift`). Every 4-8 weeks a best friend (bond 3) or a relative of a person who plays may get the quest with the human as target; they walk to his tile and give a gift **from their own holdings** by trait (Gatherer herbs, Miner metal, Alchemist a pill, Blacksmith / Tailor gear, Scribe an array, Merchant a twentieth of his wealth, Leader sect points, Runner news of places, Innovative an enchantment; the rest silver / a pill) at a level the receiver can use; one gift per giver to the same person per year (52 weeks); food, wine and poison come from a Chef's, a Drunken's and a Poisoner's goods (up to five portions); humans only receive, NPCs never give each other gifts. Doing the quest by hand gives a gift from your own holdings. Probe `GiftProbe`.
- **Trait sects.** A third of the sect slots is kept for founders who are not Leaders (`Settlements.sectRoomForLeader`, the first draws of the yearly founding); a Leader's sect may be a craft guild of the craft most practised in his tile (`Organization.tileCraft`, 40%, at least three practitioners), named after the craft (`Naming.sect(founder, theme)`); a member who practises the guild's craft cultivates a tenth faster (`Organization.guildBonus`), and a Musician of a hall of musicians is paid a quarter more at concerts and in the street (`Life.hallBonus`). Probe `SectThemeProbe`.
- **Story craft counter.** `Chapters.Chapter.made(n, text)` counts items made **since the chapter began**; the Story window, the Quest Log label and the objective message say "made 12 of 25 since this chapter began" (`Chapters.taskText`); the Smith's five and the Heir's one craft tasks use it. Every pill counts as one item (`itemsCrafted` rises by the number of pills in a batch). Probe `StoryCountProbe`.
- Probes `DockMergeProbe` (the dock, Rankings, Control). `PcProbeGui`, `PausedGuiProbe`, `FixesProbe` were updated.
## The Musicians' concerts (2026-10-15)
- **Twelve concert pieces** (`Concerts`; `kind: concert` scores: `Score.concert`), a minute or so each (30-111 s), not part of the music the places choose from (`SongBook.all()` does not list them): A Song for the Market Square, The Ferryman's Tune, Autumn Moon Waltz, Plum Blossom Snow, Drinking Song of the Nine Cups, The Widow's Lament, Dance of the Paper Lanterns, River at Dawn, The Wanderer's Return, Festival of Kites, Night Watch Elegy, The Master's Last Lesson (major, minor, Dorian, Mixolydian and pentatonic keys; 4/4, 3/4 and 6/8). A piece is written as a tune in scale degrees over a chord for every bar (three sections of eight bars: A, B, A with another ending); the harp arpeggio, the pad, the bass and the ornamented versions of the tune are made from it. `SongTool concerts` lints them (no clashes between the tune and the chords), `SongTool balance <name>` balances them (the lead at its role's level).
- **Listening and the repertoire of 43 (2026-10-21).** `ConcertsMore.MORE` adds 31 pieces to the twelve (generated from note-name sources: a tune in note names over chords is checked bar by bar, then written as degrees): 25 for the occasions market, road, water, season (spring and summer join autumn and winter), tavern, lament, festival, night, study, wedding, temple, war, and six masterpieces (`Piece.master()`: a fourth section C between B and the return, 80-130 s, a `grand` strings and choir track). `Concerts.occasionAt(area, musician)` picks the occasion from the place (sect seat: temple, or war in wartime; city: festival / market / tavern / wedding / study / season by the quarter of the year; spirit vein: temple; haunted ground: lament; lair or trial: war; water names: water; the wilds: road, night in winter, season, lament); `pieceAt` picks a piece of that occasion by the musician's id and the quarter, and a masterpiece one concert in five for grade 8+ or a Heaven-Sent instrument. A musician who plays (`Life.play` 2 weeks, `Life.concert` 4) calls `Concerts.announce`: the tile keeps `perfBy / perfStart / perfUntil / perfGrade / perfInst / perfHeaven` and the piece is worked out only when asked (`showAt`, `near(c, 25)`). `listen` (`Concerts.listen`) plays it with `Ambient.concert(piece, leadForName(instrument), grade)`, marks `Character.heardConcerts` (bit i = i-th piece of `all()`), costs the week, gives 4 weeks inspired (+5%) and +1 friendship once per musician (`lastListened`). `GuiConcerts` is the window; `settings concerts` the notice; notifications category "music".
- **Pictures (2026-10-21).** `ArtNature` paints the falls / rapids / gorge scenes, the volcano, the natural arches, the sea (`waves`, also under islands) and the desert (`duneLayer`, `palm`, salt crust, ruins) for `ArtGen.scene`; `tools/probes/ArtNouns.java` renders three seeds of chosen nouns side by side.
- **The instrument leads.** `Concerts.leadFor(instrument)` maps the instrument's name to a voice (Guqin and Guzheng pluck, Pipa pipa, Erhu erhu, Dizi flute, Xiao xiao, Sheng sheng, Konghou harp, Bell Chime chime, War Drum timpani). `Conductor.concert(piece, lead, level)` cuts the music in at once, plays the piece once with the lead tracks on the musician's instrument, and the places' music goes on after it (6 s of rest).
- **Grade sets pace and ornament.** Tempo = written tempo x (0.88 + 0.02 x grade) (grade 1 plays at 90%, grade 10 at 108%); grade 1-4 plays the plain tune (`lead`), 5-7 the tune with a grace note on every note of a beat or more (`leadf`), 8-10 also trills on the notes of two beats or more (`leadt`). The usual variation of a performance (key shift, small tempo drift) applies too.
- **Repertoire and when it plays.** The piece follows the musician's id and the season (`Concerts.pieceFor`); it is heard (`Ambient.concert`) when the music is on and the musician is a person who plays (an NPC musician's concert in your tile stays silent, by the author's choice), at the Perform quest's concert and at every turn of a Musician's trade (`Life.play`, `Life.concert`); a concert in progress is not interrupted by another. `music concerts` lists the pieces, `music concert <n|name>` plays one with your own instrument as the lead. The message of a Musician's turn names the piece ("You play 'River at Dawn' on ...").
- Probe `ConcertProbe` (17 checks: the pieces parse and stay out of the rotation, lengths, rendering, the lead instrument and the three ornament levels, tempo by grade, played once and the music goes on, repertoire, the hall's bonus).

## The beta tester's list (2026-10-16)
Five batches, all measured by probes (`BatchAProbe`, `LogLiveProbe`, `BatchBProbe`, `VignetteGuiProbe`, `ElementsProbe`, `FightReplayProbe`, `FightGuiProbe`, `MonsterProbe`).

### A. Small things
- **Try for a child**: `child` / `conceive` (also `do Get Pregnant`) and the Me tab's "Try for a child" button (`GuiDock.canTryForChild`). Either spouse can ask; a father who plays asks his wife (`PlayerCommands.tryForChildAsFather`: she must stand in his tile and not be expecting; her own `Get Pregnant` step runs; a week passes; about 1 in 10 works). NPCs keep their quest.
- **Squad & Sect** is one window with two tabs (`GuiSheets.squadWindow(mv, tab)`); the dock's People group has a Squad and a Sect button.
- **Walks**: `policy walkfight on|off`, `policy walknotice on|off` (Control > Priorities has the boxes; both default OFF). A fight that finds a walking person is fought and the walk goes on; a new quest alert waits for the next prompt (`PlayerControl.walkStopFight / walkStopNotice / attackedBy`, `GameDriver.manualTurn`, `Character.quest`). On: the walk stops and says why.
- **Refine Blood**: only Evil alchemists are sought (`Character.isRefiner`); starts at 2,000 Blood (`REFINE_MIN_BLOOD`); no fee; of n pills (1,000 Blood each, at most skill / 10 + 1) the alchemist keeps n / 2 (rounded down) and the cultivator gets the rest.
- **Gatherers and Miners** (`Character.gather`): a Gatherer (herbs) or Miner (ore) finds x (1 + skill tier - material level) of materials below his tier (profession level / 10); the ground loses only the base amount; every find teaches the trade (3 x level x units); a find's quality is capped by the tile (level / 10 + 2; `settings tilecap off`); a tile under 5,000 gets 50 a week back (`Area.MATERIAL_FLOOR / MATERIAL_TRICKLE`).
- **The GUI log refreshes at once** (`GameDriver.logVersion`, `MapViewer.refreshLogIfNew`): a command that costs no turn showed its answer only after the next week; this was the "Invite does not print" report.

### B. Teaching and little stories
- **Lessons** (`Lessons`): 15 casual one-time talks (qi, the edge of a realm, potential, travel, quests, sects, gear, the five elements, Blood, squads, gifts, tournaments, tribulations, gatherers, children), told when someone stops to talk (`Dialogue.approach`) or you talk first (`Dialogue.converse`), in the portrait window or the log (`Dialogue.LogIo`), at least 6 weeks apart, two voices each (warm / dry) and "Tell me more". `settings lessons`, `lessons`. `PlayerControl.lessonsSeen / nextLessonWeek`.
- **Favors** (`Favors`): a dialogue scene whose ask becomes a Hunt Beast quest with `Quest.giver / reward`; `Character.finishTarget` calls `Favors.thank`: pays what the asker can, +2 bond. At most 2 open, 8 weeks apart.
- **Little stories** (`Vignettes`): 10 short scenes (scene window headed "A little story", `GuiLore.vignette`, `GuiScenes.ask`), one turn in 20 when one fits, 20 weeks apart, never for AUTO; own switch `settings vignettes`, `stories`. Small consequences only.

### C. The five elements (`Elements`)
- Every creature has a path of one element (Wood, Fire, Earth, Metal, Water) and a name ("Way of the Silent Mountain"); `Character.path` is one short; it comes from the id when first asked (old saves too); a child follows a parent's element half the time. Every technique has an element (`Technique.elemPlus1`, a word of it in its name; the Default Technique follows its user).
- Cycles: Wood parts Earth, Earth absorbs Water, Water quenches Fire, Fire melts Metal, Metal chops Wood; Wood feeds Fire, Fire makes Earth, Earth bears Metal, Metal holds Water, Water nourishes Wood.
- Odds: `winChance += Elements.swing(a, b)` (three places in `battleCore`, `previewFight`, tournament bouts); edge = two thirds martial + one third movement technique, each = against (overcomes +8, overcome -8, drains +3, feeds -3) + suits (attuned +5, feeds own path +3, own path feeds it +2, holds own path back -6, own path holds it back -4); swing = a's edge - b's edge. Two defaults of Fire and Metal: +/-16. A beast fights with its own element. A fighter not steered by a hand (`Elements.chooseTechniques`, called in `battleCore` and `previewFight`) fights with whichever of his two martial (and two movement) techniques is worth more against this opponent: 3 points of the chance per technique level plus the element part; he swaps the slots when the other is worth more than 3 points better, for that fight only, and the fighting level follows.
- Shown in Stats, the people card, the character card, the fight preview and report, the techniques list; chosen (or rolled) in Edit Character at creation (PATH box), fixed for life. Help page "The five elements", a lesson.

### D. The fight window (`FightReplay`, `GuiFightView`)
- A fight is settled by one roll; its picture is a dramatisation built to agree with the result (winner, loser's end health: killed 0, spared / injured low, fled = a flight beat; health only falls; elements, techniques, a Forbidden Technique and arrays are beats). Built in `FightReport.finish` for full reports (`FightReport.replays`, the last 40) and for tournament bouts (`FightReplay.bout`).
- `settings fightview on|off` (default ON; also a Settings box and a switch in the Fights window): the window opens when a fight a manual person was in is over and the game waits. Always: the **Fights** window (Me tab, `fights`) with Watch / Fight report, and every tournament result is a window with a Bout button for each of the person's bouts (`GuiFightView.tournamentResult`). Pause / Skip / Watch again / speed.

### E. Monsters (`Monsters`)
- Measured before (3,000 people, 40 years, `MonsterSim`): a Monster beaten by an NPC cultivator ALWAYS died (`winCheck` had no death roll for it), about as many were killed each year as woke (50-75), and a dozen or more stood above the 20th strongest cultivator (level 50-80 against 33).
- Now: a Monster is never stronger than the 20th strongest cultivator (`Monsters.cap`, recounted at the New Year in `Rankings.yearly`; clamped when it wakes and yearly; it stops levelling at the cap in `cultivate`); a Monster beaten by an NPC cultivator is hurt and lives on, dying on 18 in 100 (`Monsters.dies`; the fight's own death roll, which follows enmity, is not used); a person who plays still kills; a Monster is wary (`caution` 55); "Hunt Cultivator" picks a cultivator it can beat (`Monsters.weakerCultivator`); "Seek Territory" now walks to a hunting ground within 6 tiles (a wild tile with beasts, weight 3, or with cultivators it can beat, weight 1; never a city, a sect or an array; `Monsters.huntGround`) and fights there (`Monsters.huntAt`: beasts 70% of the time when there are any). Monsters stand on the boards of Highest Level, Most Kills and Highest Fighting Level (never Richest; "Sovereign / Lord / Legend of the Wilds"), are marked "(Monster)" and are spared by the epoch.

### F. After the first round of play (2026-10-17)
- **Every fight through the window** (`FightReport.finish`): hunts, lairs, attacks, spars, challenges, tournaments, the Convocation; beasts and Monsters are stick animals (`FightReplay.kindA/B`, `GuiFightView.drawAnimal`); the place picture is the background; the window's check box "Do not open this window by itself" and the Settings box are one switch (`GuiExtras.fightViewListeners`).
- **Tournaments for the people who play** (`Tournaments`): the entrant's bouts and a result window with a Bout button each (also `Lore.runConvocation`); a **spectator** (anyone who plays in the tile, entered or not) sees the last two rounds (all if 8 or fewer) and can bet: side, then stake 2% / 10% / 25% of the wealth; payout 100 / chance x 0.95, x1.05 to x15.
- **Music Manager** (`GuiMusic`, `Playlist`, `Settings.musicFilter / musicPlaylist`): see the DEVLOG of 2026-10-17; the options left Settings; `music playlist ...` in the terminal.
- **Scroll speed** (`Settings.scrollSpeed`, `Ui.installScrollSpeed`): 8 px x speed per wheel unit, 3 units a notch.
- **More cheats** (`Cheats`, `GuiCheats`, `cheat help`): kill (divine lightning), relation, unbreakable, technique, teleport, give, tame / companion, evade (policy), nodeath (mark), resurrect (graveyard of 500), win, noescape, create, city, sect. Engine hooks: `Character.smitten / noDeath / cheatWin / cheatNoEscape / unbreakable`, `PlayerControl.evadeDeath`, `Cheats.bias / evades / bury`.

### G. Seats, all the cheats, stories for every realm (2026-10-18)
- **A seat to spectate** (`Tournaments.takeSeat / spectators / clearSeats`): the bouts and bets of a tournament are for a person who plays, stands in the tile, did not enter and took a seat with the *Spectate Tournament* quest (always offered to a person who plays while a tournament is open). Seats live in memory (a weak map) and are freed when the tournament is held or called off.
- **The third set of cheats** (`CheatsMore`, `cheat help`; tabs Growth, This tile, Time of the Cheats window): breakthrough, pass (`Character.cheatBreak` + `gateShort`), age, lifespan, inhibitor, mastery, element, marry, pregnant, epoch (`GameDriver.epochNow`, `epochCheck()`), treasure, vein, dungeon, array, wipe, awaken, tournament open / hold / close, jump, season, freecraft (`Character.cheatFreeCraft`, refilled every turn by `CheatsMore.refill`), wealth, reveal, teleport with the squad. A cheat that waits (`jump`, `season`) spends the week (`CheatsMore.takeTurnFlag`).
- **Little stories by realm** (`VignetteRealms`, `Vignettes.Story.lo / hi`): 60 stories: ten for Qi Cultivation and Foundation (three of them for every realm) and five for each realm from Foundation to True Immortal. A story is told only in its realm. `{s}`, `{m}`, `{b}` in the text and the options are 1%, 4% and 12% of `VignetteRealms.unit` = the price of gear of the realm's level (10 x (10 x major + 5)^2.5), rounded down to two figures (`Vignettes.fmt`). Rewards: `boost` (a share of the cultivation bar, never past 95%), herbs and metal of the realm's quality, a pill for the next gate, gear of the realm's level, `sharpen` (techniques), a place on the map, `calm`, and now and then `givePotential(0.1 .. 0.2)` (a tenth or fifth of a Potential Pill).
- **Lessons**: 21 (six new: `beasts`, `ruins`, `spar`, `modes`, `auction`, `found`).
- **The fix round after the long runs** (`LongRunFixProbe`, 28 checks): `PlayerControl.SPOT_TYPES` now holds the eight quests that are done where you stand (Buy City Array, Buy Sect Array, Buy from Sect, Donate to Sect, Entice Merchant, Hold / Register / Spectate Tournament) and `PERSON_TYPES` holds Bounty Hunt; `Character.partyBattleCheck` skips a person who plays by hand (`isManualPc`) and tells him once a week that he stays out of it (an NPC friend still joins); `Character.fightRefusal` gives the reason a Good or Neutral person will not strike a non-Evil one; Deliver Goods, Bounty Hunt, Disperse, Get Pregnant (a person who plays: the quest stays until it works), Recruit Defector, Potential Pills, Festival Trade and Attend Auction report what happened; `refineBloodWith` names the alchemist's skill when that is the limit; poison posts a 'danger' notice, adds a `next:` line to the status block (`Life.poisonHint`) and reminds every third week (`Life.poison / tick`). Left as they are: Major-level chapter gates (the Scion's Salt Road, the Heir / Disgraced / Disciple waits) and the Smith's pace. A design for letting people out at Ascension is in `GameInfo/Garden at Ascension Design.md` (not built).

### H. The Door at Ascension (2026-10-18; design: `Garden at Ascension Design.md`)
- **The Door is open from Ascension** (Major Lvl 7, level 71) for a person who plays: `lore door` (alias of `lore cross`) from anywhere and at any time, the Door button of the Me tab, a link in the Oldest Order journal. On reaching Ascension: a notice and the scene `door7` (`Lore.PState.doorTold`). `LoreEndings.earlyDoor(c, p)` = Major >= 7 and not `readyToCross` (True Immortal, level 101, the nine places and the Seat read). The Full Door (at Originis) is unchanged.
- **The Early Door** (`chooseEarly`): the Old Man (`early_door`, `early_oldman`), then Go back / Stay (the existing Stay menu) / Leave without a lamp (final). Leave: **Rear Guard** (Good, `early_rearguard`, +500), **Early Strike** (Evil; the array kills with `earlyStrikeDeath` 85 / 75 / 65 / 55 % by Major 7 / 8 / 9 / 10, always with nine lamps; a death sends the blood home, +200), **walk out alone** (Neutral; `pickOutsideFor` weights the bad Outside branches 4 / 3 / 2 / 1 by realm; the branch bonus is divided by 4). The descendants carry +`Settings.leaveBless` (2). `lore leave` after an early Stay opens the Early leave menu while the person is not a ready True Immortal. Endings are `early_rearguard`, `early_strike_dead`, `early_evil_spared_*`, `early_alone_*` (StoryCodas: leave / fall / leave).
- **Ordinary people leave**: `LoreEndings.realmReached(c)` (called from the breakthrough code in `Character` when Major Lvl >= 7 is reached; once per realm: `Character.leaveRolled`) rolls `Settings.leavePct[realm - 7]` (20 / 15 / 10 / 25 %); a winner is queued and `drainLeavers()` (end of the turn, `GameDriver`) lets them go through the Seam: below True Immortal they are gone (`deathCause` "left the Garden", `leaverGift` = `Settings.leaveBless`, `bless(c)`), at True Immortal a free lamp makes them a Wandering Immortal (no lamp reserve; the nine-lamp cap holds). Never rolled: seekers and stayers (`State.seekers`, `stayers`), Wandering Immortals, `fatedTo` people, Beasts / Monsters, people who play. `State.leftAt[4]` counts them (shown in the Roll of the Crossed). `childBlessing` = `blessing` + 5 for a lamp-bearer + `leaverGift`. Settings: `settings leavepct a h p t`, `settings leavebless n` (saved as `leavePct`, `leaveBless`).
- Tests: `DoorProbe` (NPC rolls, descendants, exemptions, lamps, the human flows; one ending per run), `LeaveSim` (realm counts per decade, with and without the chance).
- Measured (`LeaveSim`): a 20,000-person world for 150 years, with and without the chance, **never had anybody at Major 7 or above** (about 4,000 at Major 6 at most; Ascension is out of reach for NPCs in that time), so the leave chance does not touch the ordinary world at all. With 400 NPCs started at the top of Nihility with 100 potential (3,000-person world, 100 years): the group reached Ascension from about year 45, and with the chance on 38 of the 188 who got there had gone through the Seam by year 100 (20% as set; 150 stayed, 199 without the chance), nobody reached Half-Immortal in 100 years. The realm counts above Ascension are therefore unchanged in a normal world, and the chances (settings) can be raised without harming it.

## The eight new starts and the Palace below (2026-10-20)
- `Starts.STORIES` now holds eighteen starts (menu order: the first ten, then `chef`, `poisoner`, `tailor`, `inventor`, `musician`, `miner`, `cheater`, `forager`). Each has a `Tale<Name>.register()` (Season 1 and the interlude) calling `Tale<Name>Seasons.add(d)` (Seasons 2 and 3 and the epilogue); `Chapters`' static block registers them; `StoryCodas` has the five endings for each.
- Rolled mysteries: Chef S2 (`culprit`), Poisoner S2 (`killer`, bent by the `ledger` choice), Tailor S2 (`culprit`). Each culprit leaves two fair clues, gated with `?culprit=n|`.
- Choices come in two places: `.choice(...)` is made after the intro (the outro reads the answer); `.outChoice(...)` is made after the outro (the outro leads to the question).
- New lore data: scenes `undercroft` and `court_hands` in `LoreText`, queued in `Lore.complete` (STEP_INNER) and `LoreSites.discover` (lanternroad). The Hands of the Court: Guest, Snuffer, Captain, Clerk.
- A counted task (`made(n)`) lapses after `Chapters.MADE_WAIVER` (4 years) so a start whose trade makes nothing is never stuck; Major-level and wish gates lapse after `PATIENCE` as before.

### The Door season and extra seasons (2026-10-20)
- `Chapter.hardMajor(m)`: a chapter that waits for Major `m` and never lapses (`major(m)` lapses after `PATIENCE`). `StoryDoors.add` (called for every Def at the end of `Chapters`' static block) numbers its season after the story's last, so extra seasons must be registered first, from `Tale<Name>Seasons.add`.
- `StoryDoors.door(...)` builds a chapter with `hardMajor(7)` and `skipIf(GONE)`; `StoryDoors.epi` adds epilogue lines before the last line; `StoryDoors.latch` is the shared three-way choice.

## The Garden's Variety Tournament, Fame, sieges, pictures (2026-10-21)

### The Garden's Variety Tournament (`GardenTournament`)
- When: year 10, year 100 and every 100 years after (`isGardenYear`); the year change calls `GardenTournament.yearly(year)` before `epochCheck()`, so everyone alive is in it. The Chronicle remembers that it was held (`held(year)`), a reload at the same week does not hold it twice. `settings garden on|off` (`Settings.gardenGames`, default on), `cheat garden` holds one now.
- Who: every living, born person who is not a beast (a Monster of the wilds is; a tamed one is not). Strength is `Rankings.fightLevel` (rating / 5, read from what is worn: nothing is equipped), the chance of a bout is the one of every duel (`50 - 15 * gap + Elements.swing`, +/-45 for the Lucky, 5..95), so luck matters at any gap.
- How: single elimination with a new random pairing every round (a person with no opponent goes on); the rounds are ceil(log2 n) (17 for 100,000); nobody dies. 999 platforms are the setting only (bouts are numbered on them in the report). The score is the number of rounds survived out of the rounds there were, kept as a packed int on `Character.gardenLog` (flags | year | survived | rounds), shown as "Garden's Variety Tournament: Year 10: survived 7 of 17 rounds (Last Sixteen)" on the person card, the Stats sheet, the character dialog and the terminal stats.
- Prizes (`TIER`, `PILLS`, `FAME`, `STONES`): champion 3 pills' worth of potential + a Heaven-Sent treasure + 5,000 fame + the title Champion of the Garden; finalist 2 + treasure + 2,500 + title; semifinalists 1 / 1,500; quarterfinalists 1 / 800; last sixteen 0.5 / 400; last sixty-four (two rounds) spirit stones / 150; half the rounds or more spirit stones / 40; the Favoured of the Garden (the weakest person who got through half the rounds): 1 pill + 150 fame + a title. Spirit stones are base x (Major + 1)^2. The two grand titles outrank the board titles (`Naming.assignTitles`).
- Cost: one pass of the year change, 47 ms for 3,000 people (about 1.5 s for 100,000); the tick is not counted in `tickNs` (the epoch's median of slow ticks) because `heldThisTick` is set.
- A person who plays: a scene with the picture of the platforms and the question "Step onto the platform / Let the bouts pass" (`GuiScenes.ask`), then their first and last three bouts in the fight window (stage 2, music "The Platforms Fall"), then a result window (rounds survived, the share of the field they beat, the standing, the prize, the field's best sixteen) with a Watch button per bout. An AUTO or headless character gets the notice and the text.

### Fight window effects (`FightFx`, `FightReplay`, `GuiFightView.Stage`)
- A technique is read from its name (`FightFx.look`): form word (Palm, Fist, Sword Art, Saber Technique, Spear Art, Staff Method, Fan Dance, Scripture, Strike, Claw, Kick, Finger, Seal) -> the shape of the blow; element word (the five paths' words and Thunder, Frost, Flame, Wind, Stone, River, Shadow, Moon, Sun, Star, Iron, Jade, Void, Storm, Mist, Lightning, Blood, Spirit) -> the sparks (leaves, embers, shards, glints, drops, crystals, mist, waves, bolts, spiral, darkness, crescent, stars, blood, orb); creature word -> a beast that rides the blow (serpent, winged, clawed, shell); number word -> how many; Heaven / Earth / Five Elements ... -> a ring of runes; the colour is the technique's element. A movement technique (Step, Leap, Flight, Shadow Walk, Dash, Vanishing) is shown as the mover's own motion (after-images, a leap, a fade). `FightReplay.Beat` carries the technique (`tech`, `movement`, `techElem`); a dodge shows the defender's movement technique.
- `FightReplay.tourney`: 0 an ordinary fight, 1 a tournament or Convocation bout (crowd, banners, lanterns, flashes), 2 a bout of the Garden's (night sky, floating platforms, falling light, a carved stone slab that rings with every blow, a white flash on the last blow). A hard blow shakes a tournament stage.
- Music (`EventMusic`, `SongArena` "Sand and Drums", `SongPlatforms` "The Platforms Fall"): pinned with `Ambient.pin` while a tournament fight or result window is open (a count of holds, the Garden's wins), the previous pin (or the places' music) returns 2 s after the last window closes; nothing when the music is off; the two pieces are not in `SongBook`.

### Fame (`Fame`)
- `Character.fame` (int), `fameCrafted` (items crafted when last counted), `rankFame`. Sources: a beast hunted (1 + Major / 2, only if the beast is within a realm below the hunter), a Monster (10 x (Major + 1)), a cultivator slain (3 x (Major + 1), less 3 for every realm the killer is above, nothing for a gap of 2 or more or for a victim under 18), a breakthrough (10 << (Major - 1)), a title (30), a tournament won (20 x rounds x (max Major + 1)), the Garden's prizes, crafting (yearly, at most 300 items = 60 fame). It fades 4% each New Year (`Fame.yearly`, called by `Rankings.yearly`). A Beast earns none.
- The board "Most Famous" (`Rankings` board 4: `rankFame`, top 100, titles Name Known in All the Realms / Household Name of the Garden / One of the Hundred Names, spared by the epoch, `ranking fame`, Rankings window tab with a Fame column).

### How a city or a sect is destroyed (`Sieges`)
The chain: (1) an Evil cultivator or a wild beast (not an ambusher) kills someone in a city or sect seat while the world holds more than 25 of that kind (`Settlements.mayDestroy`); (2) the place has no array left (an array comes down only when the attacker beats it: `arrayBreakAttempt`, 50 - 15 x (array level / 5 - attacker's power)%, a 5% backlash death on failure; a failed attempt leaves it standing); (3) a roll of 1 in 250 (city) / 1 in 100 (sect); (4) the storm: everyone who stands for the place and is in it (sect members in the seat; a city's lord, the lord's sect, the shop owners, the lord's close friends; at most 12, none far weaker than the attacker) fights the attacker one after the other, strongest first, as real fights, the attacker tiring 2 fighting levels per bout; then the standing guards (2 + 1 per 6 members for a sect, 2 + 1 per 20,000 people for a city, at most 10, as strong as the best of the place) are rolled with the odds of a fight; one defeat ends it, the attacker is hurt and the place is "under attack" for a season; only a clean sweep calls `destroyCity`. A 3,000-person world over 50 years: 199 places razed before, 69 razed and 108 turned back after (one run each). `Sieges.counts()` and `CitySim` report them.

### Pictures of places (`StoryArt`)
- 41 watercolour pictures from the `ArtGen.Painter`, painted from a seed (never stored): the nine sites of the Palace's story, the Empty Seat, the Door, the Under-Kitchen, the Hollow Court, the Giant Footprints, the Garden, the platforms, and 25 kinds of story place. `forPlace(story, placeName)` gives each story place name ("harbour", "quarter", "kessel", "fordcamp" ...) its picture; a chapter's picture is the place of its task, or of the person it meets, or its story's home (`Chapters.artKey`); `forLore(sceneId)` does the lore scenes and endings. The scene window (`GuiScenes`) shows the picture and a portrait (`ArtMore.portraitCached`) of each person of the story who speaks in it on its left; `Settings.pictures` off hides them. The hand-over from the game thread is `StoryArt.next(key, names, faces)` and `Scene`'s constructor takes it.

### Print statements (`Say`, `Print Review.md`)
`Say.world` (world news: the switch `settings worldnews`), `Say.about(person, text)` (a person who plays is told, everybody else reads world news), `Say.status` (the followed or automatic character's status line), `Say.debug` (only with `-Dtdf.debug`), `Character.tell` (a result of the person's own action). The debug lines of the review are behind `Say.debug` except `GameDriver2` (a scratch `main`), `Verify` (behind `-Dtdf.verify`) and the `[PARALLEL]` error lines (errors of an experimental mode go to stderr).
