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
| `Character.java` | Core class — all character logic (2,333 lines) |
| `Area.java` | World tile — biome, resources, spawning |
| `Map.java` | Static world container (tile grid + global lists) |
| `Item.java` | All item types (Weapon, Clothing, Herb, Metal, Pill, Array, Blood) |
| `Technique.java` | Movement and Martial techniques |
| `Quest.java` | Quest data + priority queue ordering |
| `Organization.java` | Sect/city faction object |
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
          └─ area.age() → +1% resources/monsters/population, NPC spawns, tax
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
- `tempLvl` — combat power = `(lvl + weapon.lvl + armor.lvl + martial.lvl + movement.lvl) / 5`
- `potential` — 1–100, generated via `101 - sqrt(rand.nextInt(10000)+1)`; limits breakthrough success
- `cultivation` / `cultivationBase` / `cultivationBaseMax` — accumulated xp toward next level
- `inhibitor` — grows on each failed breakthrough, slows cultivation speed
- `caution` — 20/35/50/65/80; threshold below which a character prefers to flee
- `ss` — Spirit Stones (currency)
- `affiliation` / `affiliationNum` — "Good"(+1) / "Neutral"(0) / "Evil"(-1)
- `professionLvl` / `professionExp` / `professionExpMax` — profession skill (1–100)
- `maxAge` / `totalAgeGained` — lifespan; each breakthrough adds age

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
| Ghost | ~1 in 33,000 | Defined but **not implemented** |
| Leader | ~1 in 33,000 | Creates cities (profLvl=1) and sects (profLvl=50+) |
| Innovative | ~1 in 33,000 | Creates techniques; sells techniques |
| Blacksmith | 1 in 100,000 | Smithing profession |
| Alchemist | 1 in 100,000 | Alchemy profession; forages if missing materials |
| Scribe | 1 in 100,000 | Array crafting |
| Merchant | 1 in 100,000 | **Decision engine not implemented** |
| Gatherer | 1 in 100,000 | Foraging profession |
| Miner | 1 in 100,000 | Mining profession |
| Friendly | 1 in 100,000 | Social interaction profession |
| Tailor | 1 in 100,000 | Clothing crafting |
| Runner | 1 in 100,000 | Guaranteed flee if opp is ≤10 levels higher |
| Monster | Enum entry only | Not assigned in constructors |
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

### Sect Bonus
- If a character belongs to a sect, the game loop grants them `founder.majorLvl` extra `cultivate()` calls per tick.

---

## Combat System

### Battle Flow (`battle()`)
1. `tempLvlCheck()` — runs `equip()`, then computes tempLvl for both fighters.
2. `deathCheck()` — determines if the fight is a deathmatch based on relationship value.
3. Win chance: `50 - (15 * (opponent.tempLvl - this.tempLvl))` ± Lucky trait (±45).
4. If win chance < caution → attempt `fleeCheck()`.
5. If can't flee AND deathmatch → `forbiddenCheck()` (boosts tempLvl by sqrt(potential), costs lifespan).
6. Same check for opponent.
7. `durabilityCheck()` — equipment may break before final roll.
8. Final roll: `rand.nextInt(100)+1 <= winChance` → winner calls `winCheck(loser)`.

### Flee Logic (`fleeCheck()`)
- Runner trait: flee if opp is ≤10 levels higher → always true.
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
| Innovative | 1 in 100 chance → `technique()` |
| Merchant | **Empty stub** |
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

### Target Quest: Find Person
- Created whenever `changeRelationship()` adds a new relationship.
- Priority scales with relationship value: `priorityDiff = abs(value)`, `maxPriority = 20 * abs(value)`.
- On arrival: positive relationship → `interact()`; negative → `battleCheck()`.

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
- Generated by: killing beasts, city taxes (population per tick to `cityOwner.ss`).
- Required by: `cityLeadership()` (1,000,000 ss), `sectLeadership()` (1,000,000,000 ss).

### City / Sect Creation
- City: Leader at profLvl=1, ss ≥ 1M, 1 in 100 chance → sets `area.city = true`, initializes area inventory bags.
- Sect: Leader at profLvl=50+, ss ≥ 1B, area has city OR 1 in 100 chance → creates `Organization`.
- **No Shop class exists yet.** Area inventory bags (`backpack`, `materialBag`, `pillBag`, `arrayBag`) are initialized but never populated.

### Organization
- Tracks founder, master, members list, location.
- `resign()` triggers `promote()` — next member in list becomes master.
- `ArrayList<Item> treasury` — stores donated items from all members.
- `sectLvl()` — `sum(herb+metal qty×lvl) / 10000`, capped at 100. Used as cultivation bonus multiplier.
- `donationPoints(Item, qty)` / `buyingCost(Item, qty)` — proportional: herbs/metals: `qty×lvl/1000`; pills: `qty×lvl×2`; gear: `item.lvl`. Buying cost equals donation value (fair exchange).
- Cultivation bonus in GameDriver: `Math.max(founder.majorLvl, sect.sectLvl() / 10)` extra cultivate() calls per tick for members below founder's level.

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
- City owner receives population-amount ss per tick.

---

## Known Bugs & Issues

### Critical
1. **`changeRelationship()` NullPointerException risk** — Lines 1366 and 1369 directly access `this.currentQuest.targetCharacter` and `player.currentQuest.targetCharacter` without null-checking `currentQuest`. If `currentQuest` is null at that point, the game crashes.

2. **`spotQuests()` always returns 0** — Line 944 hard-codes `return 0` regardless of `questSuccess`. No spot quest ever correctly reports success or failure, breaking priority accumulation.

3. **`event()` case 1 missing `break`** — The beast battle case (case 1) falls through into the interact case (case 2) every time. Beast battles also trigger an interaction with a random character.

4. **Equipment duplication in `equip()`** — When a better weapon is found in `backpack`, the code sets `equippedWeapon` to the new item and puts the old one in `tempAddWeapon`, then calls `backpack.remove(equippedWeapon)` — removing the *new* item from the backpack (correct), but if multiple better items exist in backpack, the loop continues replacing `equippedWeapon` without adding intermediate items back, potentially losing items. Additionally, `transferAllItems()` blindly copies `equippedWeapon`/`equippedClothing` — but `dead()` moves them to `backpack` before transfer — if the timing is off (e.g., `equip()` is called during combat before death), equipment could be duplicated.

5. **`forbiddenCheck()` sets `dead = true` without full death handling** — Line 2190 sets `dead = true` directly but does not call `dead()` or `dead(killer)`, so the character is never removed from `characters`, never transferred items, and never added to `remCharacters`. This creates a zombie character.

6. **Relationship removal at value 0** — `changeRelationship()` removes the relationship object when value == 0, but `checkRelationship()` still tries to read it via `relationshipList.contains()`, and the quest search uses `q.name == "Find Person"` (reference equality, not `.equals()`), which will never match.

7. **`Quest.targetArea` not updated** — A `Character`-category quest stores `targetArea = targetCharacter.location` at creation time. It is never updated as the target character moves, so pathfinding chases a stale location.

8. **`Area.age()` NPC spawning ignores `cityOwner`** — `cityOwner` field is never set anywhere in the codebase; it is always null, so no city tax is ever collected.

9. **`professionPicker()` missing `break` statements** — Friendly case falls through into Tailor, Tailor falls through into Scribe.

10. **`Item` herb/metal constructor off-by-one** — `this.quantity += quantity` starts at `quantity = 1` (field default) then adds the passed quantity, so every herb/metal item has +1 extra.

### Minor / Design Debt
- All `System.out.println` calls run for every NPC, not just the player. Extremely noisy output.
- `Monster` trait exists in enum but is never assigned in any constructor.
- `movementTechnique2` / `martialTechnique2` secondary slots exist but `tempLvlCheck()` does not include them in combat power.
- `Character.toString()` has True Immortal rebirth logic that resets `dead` and `age` to zero — this means a True Immortal character's `dead` flag is cleared silently during printing, which could interfere with the removal loop.
- The `int skipping` decrement in the skip-turn branch only decrements by 1 per turn but the comment implies it should skip exactly N turns.
- `Quest.targetCharacter` stores an `int` (the id), but `Map.characters` is an ArrayList that can have null slots after death removal. `characters.get(quest.targetCharacter)` can return null and cause NPE.

---

## Implemented Features Summary
See [Game Outline.md](Game Outline.md) for the full checklist.
