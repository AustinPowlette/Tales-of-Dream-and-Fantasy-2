# Tales of Dream and Fantasy 2 — Implementation Outline

Legend: ✅ Implemented | 🟡 Partial / Skeleton | ❌ Not Started

---

## Core Systems

### World Generation
- ✅ 31×31 tile grid (`Map.tiles`)
- ✅ Lazy area creation when characters move into null tiles
- ✅ 10-biome system (Forest, Grassland, Desert, Tundra, Ocean, Beach, Savanna, Taiga, Frozen Grassland, Frozen Desert)
- ✅ Biome transition logic (Desert→Savanna, Forest→Taiga, etc.)
- ✅ Procedural area naming from biome-specific word lists + adjectives
- ✅ Area resources (herbMaterials, metalMaterials, monsters, population)
- ✅ Area aging (+1%/tick on all resources)
- ✅ NPC auto-spawning from populated areas
- ❌ Hotspot generation (special high-difficulty areas)
- ❌ Difficulty scaling further from Originis (area level currently = explorer's level)
- ❌ Dungeon/structure types (Chambers, Catacombs, Labyrinths, etc. from design doc)

### Character Creation
- ✅ NPC constructor (`Character(Area)`)
- ✅ Birth constructor (`Character(Character mother)`)
- ✅ Beast constructor (`Character(Area, Character cultivator)`)
- ✅ Name/gender/lastName generation
- ✅ Potential generation (`101 - sqrt(random 0–10000)`)
- ✅ Caution generation (5-tier: 20/35/50/65/80)
- ✅ Affiliation generation (Good/Neutral/Evil)
- ✅ Trait assignment (weighted random)
- ✅ Technique inheritance (children inherit best technique of same-gender parent)
- ✅ Potential floor inheritance (child's potential ≥ average of parents')
- ✅ Affiliation inheritance (combined parent affiliations)
- ✅ Gestation period (time = -40, born = false)
- 🟡 Player character creation (hardcoded in GameDriver; no interactive creation screen)
- ❌ "Make MC" creation option (interactive player setup at start)
- ❌ Ghost rebirth on death (Ghost trait entirely unimplemented)

### Cultivation
- ✅ `cultivate()` — experience accumulation and level gain
- ✅ `cultivationBaseMax` formula: `lvl^3 * ((70.33 + 1.83*lvl) / 25)`
- ✅ 11 realm breakthroughs (Qi → True Immortal)
- ✅ Potential-gated breakthrough success checks
- ✅ `inhibitor` system (penalizes failed breakthroughs)
- ✅ Lifespan bonuses per breakthrough
- ✅ Sect cultivation bonus (sect founder's majorLvl extra ticks)
- ✅ Pill consumption (`consume()`) for both Cultivation and Potential pills
- ❌ Rebirth mechanic for killed original NPCs (new family lines)
- ❌ Potential increase/decrease events (pills only raise, no events yet)

### Combat
- ✅ `battle()` full resolution
- ✅ `tempLvlCheck()` — equipment + technique combat power
- ✅ `deathCheck()` — relationship-based deathmatch probability
- ✅ `winCheck()` / `deathCheck()` — win/loss resolution
- ✅ `fleeCheck()` — escape logic with Runner trait + movement technique
- ✅ `forbiddenCheck()` — forbidden technique (tempLvl boost at lifespan cost)
- ✅ `durabilityCheck()` — equipment breaking during combat
- ✅ Beast combat (`beastBattleCheck()`)
- ✅ Cheater trait (+5 tempLvl)
- ✅ Lucky trait (±45 win chance)
- 🟡 Sparring (`sparring` boolean exists, no dedicated sparring quest/event path)
- ❌ Group/party battle algorithm
- ❌ Bounty system (wanted flag, bounty board in cities)
- ❌ Arena/tournament combat
- ✅ Combat system complete — current `battle()` IS the revamped design; tile-based push mechanic was intentionally dropped as too complex
- ✅ Second technique slot (slot 2) is a training slot — holds highest-potential technique, does NOT contribute to combat power by design; children inherit it as their starting technique

---

## Professions

- ✅ `forage()` — gather herbs from current location
- ✅ `mine()` — gather metals from current location
- ✅ `alchemy()` — create Cultivation and Potential pills from herbs/blood
- ✅ `smithing()` — create weapons from metals
- ✅ `tailoring()` — create clothing/armor from materials
- ✅ `array()` — create magical arrays from materials
- ✅ `technique()` — Innovative trait creates new techniques
- ✅ `cityLeadership()` — Leader creates city (1M ss)
- ✅ `sectLeadership()` — Leader creates sect (1B ss)
- ✅ Profession exp + level cap at 100
- ✅ `professionPicker()` dispatch — fall-throughs fixed (Friendly/Tailor breaks added)
- ❌ Merchant profession (stub only — no buying/selling logic)
- ❌ Pre-req check: if profession fails due to missing materials, queue a Forage/Mine quest (design doc requirement)
- ❌ Crafting level cap on item quality (items should be capped at crafter's level + 5 majorLvl, per design doc)
- ❌ Selling excess items (pills, gear)
- ❌ Technique selling (Innovative only)

---

## Quest System

- ✅ `Quest` class with `Spot`, `Area`, `Character` categories
- ✅ Priority queue ordering with `compareTo()`
- ✅ `questInitializer()` — creates starting spot quests
- ✅ `quest()` main dispatcher — checks viable → pathfind → target action or spot fallback
- ✅ `questViable()` — validates target still exists/alive
- ✅ `pathfinder()` — diagonal movement toward destination with world-wrap
- ✅ `spotQuests()` dispatcher — Random, Profession, Hunt, Interact, Test
- ✅ `targetQuests()` dispatcher — Find Person
- ✅ "Find Person" quest created for every new relationship
- ✅ Priority increment per tick (`priorityDiff`)
- ✅ `spotQuests()` return value fixed — now returns `questSuccess`
- ✅ Quest target area refreshed from live character location before each pathfinder call
- ✅ `currentQuest` null-check added in `changeRelationship()`
- ✅ **Forage/Mine spot quests** — queued from `professionPicker()` when materials are missing; call forage()/mine() in current tile
- ✅ **Battle Dislike / Battle Rival / Battle Arch-Enemy quests** — queued from `changeRelationship()` when value reaches -1/-2/-3; Dislike is placeholder for bullyCheck()
- ❌ **Hunt Beast quest** (seek out a specific beast in known locations)
- ❌ **Buy Gear quest** (when slots are empty or gear is outgrown)
- ❌ **Go Sell quest** (when backpack reaches size threshold)
- ❌ **Bounty Hunt quest** (within 5–10 levels of target beast/player)
- ❌ **Revenge quest** (when close friend is killed)
- ❌ **Sect events** (politics, recruitment, tournaments)
- ❌ **Potential quest** (seek potential-raising pills or events)
- ❌ **Join Sect quest**
- ❌ **Find Dead Parent's Body quest**
- ❌ **Get Pregnant quest** (prerequisite check version)
- ❌ Quest priority deconstructor on death (dead characters leave dangling quests)
- ❌ One-time quest cleanup path is incomplete

---

## NPC Decision Engine (AI)

- ✅ Random movement (1 in 5 chance of staying)
- ✅ Quest-based pathfinding (`pathfinder()`)
- ✅ Event probability by affiliation (`eventChoosing()`)
- ✅ Beast autonomous movement + attacking characters in tile
- ✅ `event()` case 1 fall-through fixed — `break` added after beast battle
- 🟡 `event()` case 0 (PvP battle) is commented out (intentionally obsolete once quest system handles aggression)
- ❌ **Sect NPC decision engine** — sects exist but members have no sect-specific behavior
- ❌ **Economy/Merchant decision engine** — Merchant trait is a stub
- ❌ Higher-level characters avoiding weaker targets (design doc note)
- ❌ Affiliation-based aggression rules fully enforced (Good should never attack non-Evil; Neutral should rarely attack)
- ❌ Bully personality type

---

## Economy & Shops

- ✅ Spirit stones (ss) tracked on characters
- ✅ City initialization creates area inventory bags
- ✅ Tax collection stub (cityOwner.ss += population per tick — but `cityOwner` is never set)
- 🟡 Area inventory bags (`backpack`, `materialBag`, `pillBag`, `arrayBag`) initialized in cities but never used
- ❌ **Shop class** (no vendor/market object)
- ❌ **Market array** in city (city item pool with seller tracking)
- ❌ **Buy Gear AI logic** (NPCs checking if they need gear)
- ❌ **Sell AI logic** (NPCs selling surplus items to city market)
- ❌ Merchant trait buying at 1/100th price, selling at normal
- ❌ Potential pill selling restriction (only sell if potential = 100)
- ❌ 10% city tax routing to founder
- ❌ Leader/city 20%/80% split — NPC Leaders: 20% chance city, 80% chance sect; player character chooses freely

---

## Relationship System

- ✅ `Relationship` / `RelationshipList` objects
- ✅ `changeRelationship()` — adds new relationships, updates values
- ✅ `changeRelationshipUtil()` — probability-based change calculation
- ✅ `checkRelationship()` — handles marriage trigger, Find Person quest updates
- ✅ Marriage logic (both adults, single, opposite gender, not related)
- ✅ Sibling detection
- ✅ `checkBirth()` — pregnancy trigger from spouse interaction
- ✅ Relationship removal string comparison fixed — `==` → `.equals()`; NPE guard added
- ❌ Relationship recycling on death (design doc: don't hate killer, just give revenge quest)
- ❌ Kill counter
- ❌ Appropriate relationship gain/loss from fighting (design doc requirement)

---

## Items

- ✅ All item types: Weapon, Clothing, Herb, Metal, Pill, Array, Blood
- ✅ Weapon sub-types: Sword, Saber, Staff, Spear, Fan
- ✅ Pill sub-types: Potential, Cultivation (with realm name)
- ✅ `equip()` — auto-equip best weapon/armor + best technique slots
- ✅ `transferAllItems()` — bulk item transfer on death/looting
- ✅ `eventLootDead()` — loot corpses in area (within 2 majorLvl)
- ✅ `durabilityCheck()` / `durabilityCheckUtil()` — equipment breaking
- ✅ Item stackability (Herb, Metal, Pill, Blood stackable; Weapon, Clothing, Array not)
- ✅ `equip()` technique duplication fixed — `!techniqueList.contains()` guard prevents duplicate adds
- 🟡 `transferAllItems()` doesn't merge Weapons/Arrays by name (only merges Herbs, Metals, Pills by name)
- ❌ Random name generation for weapons, clothing, arrays, techniques (all use maker's name)
- ❌ Item counter / kill counter tracking
- ❌ Method for removing specific quantity of stackable items from inventory

---

## UI / Player Experience

- ✅ w/a/s/d movement input
- ✅ Skip turn options (stationary 'x', moving 'm') with count
- ✅ Inventory display on player turn (backpack, materialBag, pillBag)
- ✅ Spouse location display
- ✅ Breakthrough announcements (behind `printBreakthroughs` flag)
- ✅ Birth/death announcements
- 🟡 All NPC print statements fire (extremely noisy) — needs player-only filter
- ❌ Full options menu
- ❌ Player stat screen
- ❌ Known locations display
- ❌ Relationship list display
- ❌ Auto-movement direction for N turns
