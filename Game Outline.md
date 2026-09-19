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
- ✅ Hotspot generation — yearly: 3-5 random non-city areas get +10 to +30 level spike; reset at next year start
- ✅ Difficulty scaling — new areas spawn at `currentCharacter.lvl`; explorers naturally push into harder territory as they grow; Originis fixed at level 1
- ✅ Dungeon types — LOST_RUINS (treasure+technique), BEAST_LAIR (3× beasts), MARTIAL_TRIAL (technique upgrade), HAUNTED_GROUNDS (potential gain); 5% spawn chance per area

### Character Creation
- ✅ NPC constructor (`Character(Area)`)
- ✅ Birth constructor (`Character(Character mother)`)
- ✅ Beast constructor (`Character(Area, Character cultivator)`)
- ✅ Name/gender/lastName generation
- ✅ Potential generation (`101 - sqrt(random 0â€“10000)`)
- ✅ Caution generation (5-tier: 20/35/50/65/80)
- ✅ Affiliation generation (Good/Neutral/Evil)
- ✅ Trait assignment (weighted random)
- ✅ Technique inheritance (children inherit best technique of same-gender parent)
- ✅ Potential floor inheritance (child's potential â‰¥ average of parents')
- ✅ Affiliation inheritance (combined parent affiliations)
- ✅ Gestation period (time = -40, born = false)
- ✅ Player character creation — C=Customize (trait/potential/affiliation), F=Follow mode (auto-play), Enter=default Alchemist
- ✅ Ghost rebirth on death — Ghost physique trait; enters ghost form on combat death; seeks "Possess Womb" (take over unborn child) or "Ghost City Rebirth" (spawn in city); inherits Ghost trait; permanent death if killed in ghost form; `originalName` tracks prior name chain

### Cultivation
- ✅ `cultivate()` — experience accumulation and level gain
- ✅ `cultivationBaseMax` formula: `lvl^3 * ((70.33 + 1.83*lvl) / 25)`
- ✅ 11 realm breakthroughs (Qi → True Immortal)
- ✅ Potential-gated breakthrough success checks
- ✅ `inhibitor` system (penalizes failed breakthroughs)
- ✅ Lifespan bonuses per breakthrough
- ✅ Sect cultivation bonus (sect founder's majorLvl extra ticks)
- ✅ Pill consumption (`consume()`) for both Cultivation and Potential pills
- ✅ New NPC family lines — `Area.age()` spawns new characters from city populations each tick; world naturally repopulates without a separate rebirth system
- ✅ Potential increase events — HAUNTED_GROUNDS dungeon; pills via Seek Potential quest; 1% on major realm breakthrough; 2% on surviving forbidden technique; decrease events intentionally deferred

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
- ✅ Lucky trait (Â±45 win chance)
- ✅ Sparring — 10% chance/tick when rel ≥ 1; both call `practice()` (cultivate + technique upgrade) via `winCheck()` sparring branch; +1 relationship to both parties
- ✅ Group/party battle — sequential tag-team; `partyBattleCheck()` collects positive-relationship allies, strongest leads, enemy accumulates +2 exhaustionPenalty per successive fight
- ✅ Bounty system (wanted flag, bounty board in cities)
- ✅ Arena/tournament combat — "Hold Tournament" spot quest; 8-week advance registration window; single-elimination bracket (probability-based, no death); major-realm band prizes; Register/Spectate spot quests; `Area.runTournament()` runs bracket at timer end
- ✅ Combat system complete — current `battle()` IS the revamped design; tile-based push mechanic was intentionally dropped as too complex
- ✅ City/sect destruction — Evil or Beast destroys array-free city/sect after defeating defender (20% chance); plunders bags, Blood from population, cultivation boost, potential gain, dissolves sect; Originis protected
- ✅ Second technique slot (slot 2) is a training slot — holds highest-potential technique, does NOT contribute to combat power by design; children inherit it as their starting technique

---

## Professions

- ✅ `forage()` — gather herbs from current location
- ✅ `mine()` — gather metals from current location
- ✅ `alchemy()` — create Cultivation and Potential pills from herbs/blood
- ✅ `smithing()` — create weapons from metals
- ✅ `tailoring()` — create clothing/armor from materials
- ✅ `array()` — create magical arrays from materials
- ✅ `installArray(Area)` — any character can install an array at a location; Scribes auto-install on craft
- ✅ Array combat — `arrayBreakAttempt()`: attacker fights strongest array first; array removed on break; 5% death on loss; defender gets `bestArrayLvl/10+1` tempLvl boost; beasts blocked entirely; array stronger than attacker → defender won't interfere
- ✅ `technique()` — Innovative trait creates new techniques
- ✅ `cityLeadership()` — Leader creates city (1M ss)
- ✅ `sectLeadership()` — Leader creates sect (1B ss)
- ✅ Profession exp + level cap at 100
- ✅ `professionPicker()` dispatch — fall-throughs fixed (Friendly/Tailor breaks added)
- ✅ Merchant profession — Shop.java; sets up shops, restocks local pool, equips from own stock; city-to-city arbitrage: buys cargo at 30% → carries to demand city → sells at 100% via "Sell Stock" quest
- ✅ Library/Technique selling — city libraries sell technique copies for ss (lvl/5 starting mastery); sect libraries gate on sectPoints (lvl/3 starting mastery); Innovative can sell; birth inheritance lvl/4; Gift Technique lvl/2; rarity tiers implicit via technique potential value (1–100)
- ✅ Pre-req check — Forage/Mine queued from `professionPicker()` when materials missing (Alchemist, Blacksmith, Tailor, Scribe branches)
- ✅ Crafting level cap — items capped at `(majorLvl + 1) * 10` capped at 100 max; crafter can make 1 realm above their cultivation
- ✅ Selling excess items — Go Sell quest at backpack > 20; `sell()` transfers surplus to city pool
- ✅ Technique selling (Innovative only) — "Sell Technique" spot/area quest; travels to city library; `Library.sellTechnique()` pays seller; auto-donates copy to sect library 20% chance

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
- ✅ **Hunt Beast quest** — 20% chance/turn to travel to level-appropriate known area; `beastBattleCheck()` on arrival; also queued with priority 40 as "Hunt for Money" fallback when NPC visits a city shop but still can't afford missing gear
- ✅ **Buy Gear quest** — fires when gear is outdated/missing; calls buyGear() inline if in shop city; otherwise queues "Buy Gear" Area quest to nearest known shop city
- ✅ **Go Sell quest** — triggers at backpack.size() > 20; Area quest to nearest known city; calls sell() on arrival
- ✅ **Bounty Hunt quest** — posted by close friends too weak for Revenge; characters within 10 levels pick up from city bounty board; calls battleCheck on target
- ✅ **Revenge quest** — queued in `dead()` when close friend (value>=2) is within 1 major realm of killer; weaker friends defer to bounty/squad (not yet implemented)
- ✅ **Sect events** — tournaments (advance registration, brackets, prizes), politics (war/alliance/tribute/defection/resource disputes/succession crisis), recruitment quests, sect missions/quest board
- ✅ **Seek Potential quest** — triggers when `potential < 101 - (9 - majorLvl)^2`; travels to city with Potential Pills; buys and consumes on arrival
- ✅ **Join Sect quest** — non-Leader adults with no sect seek known sect locations; calls `Organization.join()` on arrival
- ✅ **Find Parent's Body quest** — queued in both `dead()` overloads; awards +1 potential on 20% chance on arrival
- ✅ **Get Pregnant quest** — queued 2%/turn for married women; handled in targetQuests()
- ✅ Quest cleanup on death — `dead()` and `dead(Character killer)` clear targetQuests/spotQuests/currentQuest; questViable() handles stale target references lazily

---

## NPC Decision Engine (AI)

- ✅ Random movement (1 in 5 chance of staying)
- ✅ Quest-based pathfinding (`pathfinder()`)
- ✅ Event probability by affiliation (`eventChoosing()`)
- ✅ Beast autonomous movement + attacking characters in tile
- ✅ `event()` case 1 fall-through fixed — `break` added after beast battle
- ✅ `event()` dead cases removed — PvP (case 0) removed (aggression is quest-driven); Forage/Mine (cases 3/4) removed (now profession spot quests); event weights redistributed
- ✅ **Sect NPC decision engine** — Join Sect, Recruit Member, Sect Raid, Propose Alliance, Demand Tribute, Recruit Defector, Donate to Sect, Buy from Sect, Promote Disciples, Return to Sect, Deliver Materials (mission delivery) all implemented; treasury system with sectLvl advancement; squabble→war escalation; sect ranks; succession crisis; resource disputes
- ✅ **Economy/Merchant decision engine** — Merchant sets up shops, restocks from city pool; all NPCs buy gear via buyGear() and cultivation pills inline; arbitrage via Restock Trip + Sell Stock; cityOwner receives shop revenue + 1%/yr city population tax
- ✅ Higher-level characters avoiding weaker targets — affiliation guards in `battleCheck()` prevent unprovoked attacks (Good won't attack non-Evil unless arch-enemy; Neutral unless rival); `bullyCheck()` replaces combat with intimidation/theft when 15+ level advantage; arch-enemy fights regardless of level (correct behavior)
- ✅ Affiliation-based aggression rules — Good won't attack non-Evil unless arch-enemy; Neutral won't attack non-Evil unless rival or worse
- ✅ Bully personality type — Evil characters with 15+ level advantage queue "Battle Dislike" vs weaker co-located characters (10%/turn)

---

## Economy & Shops

- ✅ Spirit stones (ss) tracked on characters
- ✅ City initialization creates area inventory bags
- ✅ Tax collection — `cityOwner.ss += population / 100` once per year (GameDriver yearly tick)
- ✅ Area inventory bags — city `backpack`/`pillBag`/`arrayBag` used as market pools; sell() fills them; buyGear()/restockFromCity()/sellStock() drain them; Merchants do arbitrage between pools
- ✅ `cityLeadership()` now sets `cityOwner` — unblocks tax collection
- ✅ `sell(Area city)` — transfers non-spare backpack items to city pool; pays `100^(lvl/10)` ss (placeholder pricing); pill selling deferred
- ✅ Go Sell quest — triggers at backpack.size() > 20; Area quest to nearest known city; calls sell() on arrival
- ✅ Currency tiers added — `sc`, `dc`, `ij` fields; `convertCurrency()` rolls up at 1M:1 per tier
- ✅ **Shop class** — `Shop.java`; per-merchant; buyRate=0.55, sellRate=1.50, cityBuyRate=0.30; `buyFromSeller()`, `sellToBuyer()`, `restockFromCity()`
- ✅ NPC buy decision — all characters check gear-missing/outdated in `quest()` each tick; `buyGear()` fires immediately in city with shops; otherwise queues "Buy Gear" travel quest
- ✅ **Merchant trait** — sets up shops, restocks from city pool, self-equips from stock, city-to-city arbitrage via Sell Stock quest
- ✅ Potential pill selling restriction — sell() only adds Potential pills to sell list when potential >= 100
- ✅ **Item pricing** — formula: `lvl^2.5 * 10` (lvl 1=10ss, lvl 10=3162ss, lvl 100=10M ss); Potential pills 5× base; multi-currency canAfford/deductSs correct
- ✅ City tax routing — `cityOwner` set by `cityLeadership()`; GameDriver yearly loop pays `cityOwner.ss += population / 100`; sect population also taxed via `Organization.growPopulation()`
- ✅ Leader/city 20%/80% split — NPC Leaders: 80% skip to sect track, 20% try city first

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
- ✅ Relationship recycling on death — deceased removed from all living friends' RelationshipLists in `dead()`; Revenge quest queued for close friends (value ≥ 2) within 1 major realm of killer
- ✅ Kill counter — `kills` field on Character; incremented on combat death, beast kill, and Ghost form entry
- ✅ Relationship change from fighting — `changeRelationship()` fires in `battleCheck()` before combat; same thematic effect as post-fight

---

## Items

- ✅ All item types: Weapon, Clothing, Herb, Metal, Pill, Array, Blood
- ✅ Weapon sub-types: Sword, Saber, Staff, Spear, Fan
- ✅ Pill sub-types: Potential, Cultivation (with realm name)
- ✅ `equip()` — auto-equip best weapon/armor + best technique slots
- ✅ `transferAllItems()` — bulk item transfer on death/looting
- ✅ `eventLootDead()` — loot corpses in area (within 2 majorLvl); Lucky also gets a one-time priority-75 "Loot Corpse" spot quest triggered whenever a corpse within 30 levels appears in their tile
- ✅ `durabilityCheck()` / `durabilityCheckUtil()` — equipment breaking
- ✅ Item stackability (Herb, Metal, Pill, Blood stackable; Weapon, Clothing, Array not)
- ✅ `equip()` technique duplication fixed — `!techniqueList.contains()` guard prevents duplicate adds
- ✅ `transferAllItems()` — Weapons/Arrays are unique non-stackable items; they transfer individually by design; Herbs/Metals/Pills stack by name as expected
- ✅ Random name generation for weapons, clothing, arrays — word lists in Dictionary.java; Item constructors use random adjective+type combos
- ✅ Kill counter — `kills` field; shown on 'i' stat screen
- ✅ Item counter — `itemsCrafted` field; incremented on each successful craft (alchemy, smithing, tailoring, array, technique); shown on 'i' stat screen
- ✅ `removeItems(bag, name, qty)` — static helper; finds item by name in any bag, deducts quantity, removes entry if quantity hits zero; returns amount actually removed

---

## UI / Player Experience

- ✅ w/a/s/d movement input
- ✅ Skip turn options (stationary 'x', moving 'm') with count
- ✅ Inventory display on player turn (backpack, materialBag, pillBag)
- ✅ Spouse location display
- ✅ Breakthrough announcements (behind `printBreakthroughs` flag)
- ✅ Birth/death announcements
- ✅ NPC print noise fixed — `player` changed to int (0=NPC, 1=human, 2=follow); all print guards use `player > 0`; NPCs are silent
- ✅ Options menu — 'o' key opens auto-move submenu (wait/directional/coordinate/random N turns); 'i' stat screen; 'l' locations; 'r' relationships
- ✅ Player stat screen ('i' key) — level, cultivation, potential, age, affiliation, trait, profession, wealth, equipment, techniques, sect
- ✅ Known locations display ('l' key) — all known areas with coordinates, city/sect flags
- ✅ Relationship list ('r' key) — all living relationships with label and value
- ✅ Auto-movement — 'o' menu: wait N turns, directional N turns, coordinate pathfind (X Y then N turns), random N turns; `autoX`/`autoY` fields track coordinate target
