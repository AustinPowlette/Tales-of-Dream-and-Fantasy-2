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
| `Quest.java` | Quest data + priority queue ordering |
| `Organization.java` | Sect/city faction object |
| `Library.java` | City and sect technique libraries |
| `Shop.java` | Merchant-owned shop for buying/selling items |
| `Relationship.java` | Single relationship record |
| `RelationshipList.java` | ArrayList wrapper for relationships |
| `Dictionary.java` | Static name/biome/creature data arrays |
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
          ├─ Player: read keyboard input → movement → quest()
          └─ NPC:    movement() → quest()
          └─ Child follow-mother logic (age < 8)
       3. AREA AGING PHASE
          └─ area.age() → +1% resources/monsters/population per tick
          └─ City taxation: cityOwner.ss += population/100 once per year
          └─ Monsters roam and battleCheck() against characters in their tile
       4. CLEANUP PHASE
          └─ Add tempCharacters to characters list
          └─ Null out remCharacters slots (dead/removed)
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
| Monster | Enum entry | Not assigned in any constructor |
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

### Potential Gain Events
- 1 in 100 chance on any major realm breakthrough.
- 1 in 50 chance on surviving a `forbiddenCheck()` during combat.
- 4% chance on entering a HAUNTED_GROUNDS dungeon (plus 5 extra `cultivate()` calls).
- Via Potential Pills (crafted by Alchemists, sold at city pill markets).

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

### On Death (`dead(killer)`)
- Ghost physique: enters ghost form instead of dying (once only; beast kills are final).
- Transfers all items to killer.
- Creates Blood item for killer's materialBag (quantity = potential + lvl).
- Friends (relationship ≥ 2) of the deceased lose relationship with killer by `-(value-1)`.
- If player dies and `changeMc = true`, killer becomes the new player character.
- Beast death: killer gains ss equal to beast's ss; beast gains XP from lvl kills.

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
| Leader | `cityLeadership()` at profLvl=1, `sectLeadership()` at profLvl=50 |
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

### Target Quests (selected)
| Name | Trigger | On Success |
|---|---|---|
| Find Person | New relationship formed | `interact()` or `battleCheck()` depending on relationship value |
| Hunt Beast | Area has no beasts at appropriate level | `beastBattleCheck()` |
| Battle Rival | Dislike/Rival/Arch-Enemy relationship exists | `battleCheck()` with target |
| Revenge | Close friend (value ≥ 2) dies | `battleCheck()` with killer |
| Bounty Hunt | Can't afford revenge target | Post or accept bounty on Area.bounties |
| Return to Sect | Member is not at sect location (5%/turn) | Sect arrival grants +2 sectPoints |
| Deliver Materials | Sect mission complete but away from sect | Transfer materials to treasury, gain sectPoints |
| Ghost City Rebirth | Ghost physique: seeking a city | Rebirth as new character at city |
| Buy Gear | Equipment slots empty or gear outgrown | Purchase from city market |
| Sell Stock | Backpack over threshold (Merchant) | Travel to destination city, sell cargo |

### Sect Mission System
- Sect masters post missions to members via `postMissionsYearly()` (yearly).
- Mission format: `"Forage|Herb|lvl|qty|bonus"` or `"Mine|Metal|lvl|qty|bonus"`.
- Two-phase delivery: if mission completed away from sect, a Deliver Materials quest queues; if at sect, materials transfer to `sect.treasury` immediately.

### Pathfinder (`pathfinder()`)
- Moves diagonally toward destination using wrap-around world logic.
- Returns 1 when `location == destination`.

---

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
- Required by: `cityLeadership()` (1,000,000 ss), `sectLeadership()` (1,000,000,000 ss).

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
- `buyFromSeller()` — buys items from sellers at 55% of `itemPrice`.
- `sellToBuyer()` — sells to buyers at 150% of `itemPrice`.
- `restockFromCity()` — buys from city pool at 30% of `itemPrice`.
- Merchant arbitrage: Buy Gear quest (travel to city A for cheap goods) → Sell Stock quest (travel to city B to sell at profit).

### Library
- `Library(Area)` — city library; buy techniques with ss (`potential^2 × 1000`); sell techniques for ss.
- `Library(Organization)` — sect library; access costs sectPoints.
- `learnTechnique()`: learned copy starts at `technique.lvl / 5` mastery.
- `learnSectTechnique()`: learned copy starts at `technique.lvl / 3` mastery.

### Organization
- Tracks founder, master, members list, location, treasury.
- `resign()` triggers `promote()` — next member in list becomes master.
- `ArrayList<Item> treasury` — stores donated items from all members.
- `sectLvl()` — `sum(herb+metal qty×lvl) / 10000`, capped at 100. Used as cultivation bonus multiplier.
- `donationPoints(Item, qty)` / `buyingCost(Item, qty)` — proportional: herbs/metals: `qty×lvl/1000`; pills: `qty×lvl×2`; gear: `item.lvl`.
- `postMissionsYearly()` — assigns Forage/Mine missions to members.
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

---

## Known Bugs & Issues

### Active
1. **`Item` herb/metal constructor off-by-one** — `quantity` field initializes to 1; constructor does `this.quantity += quantity` so every herb/metal stack has +1 extra item. Fix: change to `this.quantity = quantity`.

2. **`Quest.targetArea` not updated** — Character-category quests store `targetArea = targetCharacter.location` at creation time. Never updated as the target moves, so pathfinding chases a stale location.

3. **`Map.characters.get(id)` null slots** — `Quest.targetCharacter` stores an int id. `Map.characters` is an ArrayList where dead characters are nulled out by index. Callers of `characters.get(quest.targetCharacter)` can receive null and must null-check before use.

4. **`Character.toString()` True Immortal rebirth side effect** — If `dead == true && majorLvl == 10`, `toString()` silently sets `dead = false`, `time = 0`, `age = 0`. Called implicitly by any print that includes a True Immortal, this can clear the death flag outside of the normal death flow.

5. **`Monster` trait unassigned** — The `Monster` enum entry exists but is never set in any constructor. Only `Beast` is used for creature characters.

### Design Notes
- `movementTechnique2` / `martialTechnique2` secondary slots are temporary holders used during `equip()` to merge a newly-learned technique into the primary slot. They are nulled after processing; they do not need to be included in `tempLvlCheck()` separately since `equip()` is always called first.
- Technique rarity is implicit via `potential` value — high-potential techniques are rarer and stronger by construction. No separate rarity tier system is needed.

---

## Implemented Features Summary
See [Game Outline.md](Game Outline.md) for the full checklist.
