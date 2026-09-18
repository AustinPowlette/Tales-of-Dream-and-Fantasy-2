# Tales of Dream and Fantasy 2 â€” Implementation Outline

Legend: âœ… Implemented | ðŸŸ¡ Partial / Skeleton | âŒ Not Started

---

## Core Systems

### World Generation
- âœ… 31Ã—31 tile grid (`Map.tiles`)
- âœ… Lazy area creation when characters move into null tiles
- âœ… 10-biome system (Forest, Grassland, Desert, Tundra, Ocean, Beach, Savanna, Taiga, Frozen Grassland, Frozen Desert)
- âœ… Biome transition logic (Desertâ†’Savanna, Forestâ†’Taiga, etc.)
- âœ… Procedural area naming from biome-specific word lists + adjectives
- âœ… Area resources (herbMaterials, metalMaterials, monsters, population)
- âœ… Area aging (+1%/tick on all resources)
- âœ… NPC auto-spawning from populated areas
- âœ… Hotspot generation â€” yearly: 3-5 random non-city areas get +10 to +30 level spike; reset at next year start
- âŒ Difficulty scaling further from Originis (area level currently = explorer's level)
- âœ… Dungeon types â€” LOST_RUINS (treasure+technique), BEAST_LAIR (3Ã— beasts), MARTIAL_TRIAL (technique upgrade), HAUNTED_GROUNDS (potential gain); 5% spawn chance per area

### Character Creation
- âœ… NPC constructor (`Character(Area)`)
- âœ… Birth constructor (`Character(Character mother)`)
- âœ… Beast constructor (`Character(Area, Character cultivator)`)
- âœ… Name/gender/lastName generation
- âœ… Potential generation (`101 - sqrt(random 0â€“10000)`)
- âœ… Caution generation (5-tier: 20/35/50/65/80)
- âœ… Affiliation generation (Good/Neutral/Evil)
- âœ… Trait assignment (weighted random)
- âœ… Technique inheritance (children inherit best technique of same-gender parent)
- âœ… Potential floor inheritance (child's potential â‰¥ average of parents')
- âœ… Affiliation inheritance (combined parent affiliations)
- âœ… Gestation period (time = -40, born = false)
- ðŸŸ¡ Player character creation (hardcoded in GameDriver; no interactive creation screen)
- âŒ "Make MC" creation option (interactive player setup at start)
- âŒ Ghost rebirth on death (Ghost trait entirely unimplemented)

### Cultivation
- âœ… `cultivate()` â€” experience accumulation and level gain
- âœ… `cultivationBaseMax` formula: `lvl^3 * ((70.33 + 1.83*lvl) / 25)`
- âœ… 11 realm breakthroughs (Qi â†’ True Immortal)
- âœ… Potential-gated breakthrough success checks
- âœ… `inhibitor` system (penalizes failed breakthroughs)
- âœ… Lifespan bonuses per breakthrough
- âœ… Sect cultivation bonus (sect founder's majorLvl extra ticks)
- âœ… Pill consumption (`consume()`) for both Cultivation and Potential pills
- âŒ Rebirth mechanic for killed original NPCs (new family lines)
- âŒ Potential increase/decrease events (pills only raise, no events yet)

### Combat
- âœ… `battle()` full resolution
- âœ… `tempLvlCheck()` â€” equipment + technique combat power
- âœ… `deathCheck()` â€” relationship-based deathmatch probability
- âœ… `winCheck()` / `deathCheck()` â€” win/loss resolution
- âœ… `fleeCheck()` â€” escape logic with Runner trait + movement technique
- âœ… `forbiddenCheck()` â€” forbidden technique (tempLvl boost at lifespan cost)
- âœ… `durabilityCheck()` â€” equipment breaking during combat
- âœ… Beast combat (`beastBattleCheck()`)
- âœ… Cheater trait (+5 tempLvl)
- âœ… Lucky trait (Â±45 win chance)
- ðŸŸ¡ Sparring (`sparring` boolean exists, no dedicated sparring quest/event path)
- âœ… Group/party battle â€” sequential tag-team; `partyBattleCheck()` collects positive-relationship allies, strongest leads, enemy accumulates +2 exhaustionPenalty per successive fight
- âœ… Bounty system (wanted flag, bounty board in cities)
- ðŸŸ¡ Arena/tournament combat â€” "Hold Tournament" spot quest for city owners/sect masters; single-elimination bracket (probability-based, no death); major-realm band prizes; advance registration and spectator systems deferred
- âœ… Combat system complete â€” current `battle()` IS the revamped design; tile-based push mechanic was intentionally dropped as too complex
- âœ… Second technique slot (slot 2) is a training slot â€” holds highest-potential technique, does NOT contribute to combat power by design; children inherit it as their starting technique

---

## Professions

- âœ… `forage()` â€” gather herbs from current location
- âœ… `mine()` â€” gather metals from current location
- âœ… `alchemy()` â€” create Cultivation and Potential pills from herbs/blood
- âœ… `smithing()` â€” create weapons from metals
- âœ… `tailoring()` â€” create clothing/armor from materials
- âœ… `array()` â€” create magical arrays from materials
- âœ… `installArray(Area)` â€” any character can install an array at a location; Scribes auto-install on craft
- âœ… Array combat â€” `arrayBreakAttempt()`: attacker fights strongest array first; array removed on break; 5% death on loss; defender gets `bestArrayLvl/10+1` tempLvl boost; beasts blocked entirely; array stronger than attacker â†’ defender won't interfere
- âœ… `technique()` â€” Innovative trait creates new techniques
- âœ… `cityLeadership()` â€” Leader creates city (1M ss)
- âœ… `sectLeadership()` â€” Leader creates sect (1B ss)
- âœ… Profession exp + level cap at 100
- âœ… `professionPicker()` dispatch â€” fall-throughs fixed (Friendly/Tailor breaks added)
- âŒ Merchant profession (stub only â€” no buying/selling logic)
- ðŸŸ¡ Library/Technique selling â€” `Library.java` skeleton; city libraries sell technique copies for ss; sect libraries gate on sectPoints; Innovative can sell; family inheritance, mastery levelling, rarity tiers deferred (TODO in Library.java)
- âŒ Pre-req check: if profession fails due to missing materials, queue a Forage/Mine quest (design doc requirement)
- âœ… Crafting level cap â€” items capped at `(majorLvl + 1) * 10` capped at 100 max; crafter can make 1 realm above their cultivation
- âŒ Selling excess items (pills, gear)
- âŒ Technique selling (Innovative only)

---

## Quest System

- âœ… `Quest` class with `Spot`, `Area`, `Character` categories
- âœ… Priority queue ordering with `compareTo()`
- âœ… `questInitializer()` â€” creates starting spot quests
- âœ… `quest()` main dispatcher â€” checks viable â†’ pathfind â†’ target action or spot fallback
- âœ… `questViable()` â€” validates target still exists/alive
- âœ… `pathfinder()` â€” diagonal movement toward destination with world-wrap
- âœ… `spotQuests()` dispatcher â€” Random, Profession, Hunt, Interact, Test
- âœ… `targetQuests()` dispatcher â€” Find Person
- âœ… "Find Person" quest created for every new relationship
- âœ… Priority increment per tick (`priorityDiff`)
- âœ… `spotQuests()` return value fixed â€” now returns `questSuccess`
- âœ… Quest target area refreshed from live character location before each pathfinder call
- âœ… `currentQuest` null-check added in `changeRelationship()`
- âœ… **Forage/Mine spot quests** â€” queued from `professionPicker()` when materials are missing; call forage()/mine() in current tile
- âœ… **Battle Dislike / Battle Rival / Battle Arch-Enemy quests** â€” queued from `changeRelationship()` when value reaches -1/-2/-3; Dislike is placeholder for bullyCheck()
- âœ… **Hunt Beast quest** â€” 20% chance/turn to travel to level-appropriate known area; `beastBattleCheck()` on arrival
- âœ… **Buy Gear quest** â€” fires when gear is outdated/missing; calls buyGear() inline if in shop city; otherwise queues "Buy Gear" Area quest to nearest known shop city
- âŒ **Go Sell quest** (when backpack reaches size threshold)
- âœ… **Bounty Hunt quest** â€” posted by close friends too weak for Revenge; characters within 10 levels pick up from city bounty board; calls battleCheck on target
- âœ… **Revenge quest** â€” queued in `dead()` when close friend (value>=2) is within 1 major realm of killer; weaker friends defer to bounty/squad (not yet implemented)
- âŒ **Sect events** (politics, recruitment, tournaments)
- âœ… **Seek Potential quest** â€” triggers when `potential < 101 - (9 - majorLvl)^2`; travels to city with Potential Pills; buys and consumes on arrival
- âœ… **Join Sect quest** â€” non-Leader adults with no sect seek known sect locations; calls `Organization.join()` on arrival
- âŒ **Find Dead Parent's Body quest**
- âŒ **Get Pregnant quest** (prerequisite check version)
- âœ… Quest cleanup on death â€” `dead()` and `dead(Character killer)` clear targetQuests/spotQuests/currentQuest; questViable() handles stale target references lazily

---

## NPC Decision Engine (AI)

- âœ… Random movement (1 in 5 chance of staying)
- âœ… Quest-based pathfinding (`pathfinder()`)
- âœ… Event probability by affiliation (`eventChoosing()`)
- âœ… Beast autonomous movement + attacking characters in tile
- âœ… `event()` case 1 fall-through fixed â€” `break` added after beast battle
- ðŸŸ¡ `event()` case 0 (PvP battle) is commented out (intentionally obsolete once quest system handles aggression)
- ðŸŸ¡ **Sect NPC decision engine** â€” Join Sect (canJoin() criteria), Recruit Member, Sect Raid, Propose Alliance, Demand Tribute, Recruit Defector Donate to Sect, Buy from Sect implemented; treasury system with sectLvl advancement; squabbleâ†’war escalation; sect ranks; succession crisis; resource disputes; internal rank promotion, assigned mission quests deferred
- ðŸŸ¡ **Economy/Merchant decision engine** â€” Merchant sets up shops, restocks from city pool; NPC buy decision (buyGear) for unequipped characters; arbitrage travel via Restock Trip quest (combines pool-size scan, own-shop bonus, circuit distance); full NPC buy/sell trees deferred
- âŒ Higher-level characters avoiding weaker targets (design doc note)
- âœ… Affiliation-based aggression rules â€” Good won't attack non-Evil unless arch-enemy; Neutral won't attack non-Evil unless rival or worse
- âœ… Bully personality type â€” Evil characters with 15+ level advantage queue "Battle Dislike" vs weaker co-located characters (10%/turn)

---

## Economy & Shops

- âœ… Spirit stones (ss) tracked on characters
- âœ… City initialization creates area inventory bags
- âœ… Tax collection stub (cityOwner.ss += population per tick â€” but `cityOwner` is never set)
- ðŸŸ¡ Area inventory bags (`backpack`, `materialBag`, `pillBag`, `arrayBag`) initialized in cities but never used
- âœ… `cityLeadership()` now sets `cityOwner` â€” unblocks tax collection
- âœ… `sell(Area city)` â€” transfers non-spare backpack items to city pool; pays `100^(lvl/10)` ss (placeholder pricing); pill selling deferred
- âœ… Go Sell quest â€” triggers at backpack.size() > 20; Area quest to nearest known city; calls sell() on arrival
- âœ… Currency tiers added â€” `sc`, `dc`, `ij` fields; `convertCurrency()` rolls up at 1M:1 per tier
- âœ… **Shop class** â€” `Shop.java`; per-merchant; buyRate=0.55, sellRate=1.50, cityBuyRate=0.30; `buyFromSeller()`, `sellToBuyer()`, `restockFromCity()`
- ðŸŸ¡ **Merchant trait** â€” sets up shops in cities, restocks from city pool; arbitrage travel and NPC buy decision tree deferred
- âŒ Potential pill selling restriction (only sell if potential = 100)
- ðŸŸ¡ **Item pricing** â€” overflow fixed (capped at Long.MAX_VALUE for lvl > 92); multi-currency canAfford/deductSs fixed; formula still placeholder pending economy pass
- âŒ 10% city tax routing (tax already collected; distribution logic not implemented)
- âœ… Leader/city 20%/80% split â€” NPC Leaders: 80% skip to sect track, 20% try city first

---

## Relationship System

- âœ… `Relationship` / `RelationshipList` objects
- âœ… `changeRelationship()` â€” adds new relationships, updates values
- âœ… `changeRelationshipUtil()` â€” probability-based change calculation
- âœ… `checkRelationship()` â€” handles marriage trigger, Find Person quest updates
- âœ… Marriage logic (both adults, single, opposite gender, not related)
- âœ… Sibling detection
- âœ… `checkBirth()` â€” pregnancy trigger from spouse interaction
- âœ… Relationship removal string comparison fixed â€” `==` â†’ `.equals()`; NPE guard added
- âŒ Relationship recycling on death (design doc: don't hate killer, just give revenge quest)
- âŒ Kill counter
- âŒ Appropriate relationship gain/loss from fighting (design doc requirement)

---

## Items

- âœ… All item types: Weapon, Clothing, Herb, Metal, Pill, Array, Blood
- âœ… Weapon sub-types: Sword, Saber, Staff, Spear, Fan
- âœ… Pill sub-types: Potential, Cultivation (with realm name)
- âœ… `equip()` â€” auto-equip best weapon/armor + best technique slots
- âœ… `transferAllItems()` â€” bulk item transfer on death/looting
- âœ… `eventLootDead()` â€” loot corpses in area (within 2 majorLvl)
- âœ… `durabilityCheck()` / `durabilityCheckUtil()` â€” equipment breaking
- âœ… Item stackability (Herb, Metal, Pill, Blood stackable; Weapon, Clothing, Array not)
- âœ… `equip()` technique duplication fixed â€” `!techniqueList.contains()` guard prevents duplicate adds
- ðŸŸ¡ `transferAllItems()` doesn't merge Weapons/Arrays by name (only merges Herbs, Metals, Pills by name)
- âœ… Random name generation for weapons, clothing, arrays â€” word lists in Dictionary.java; Item constructors use random adjective+type combos
- âŒ Item counter / kill counter tracking
- âŒ Method for removing specific quantity of stackable items from inventory

---

## UI / Player Experience

- âœ… w/a/s/d movement input
- âœ… Skip turn options (stationary 'x', moving 'm') with count
- âœ… Inventory display on player turn (backpack, materialBag, pillBag)
- âœ… Spouse location display
- âœ… Breakthrough announcements (behind `printBreakthroughs` flag)
- âœ… Birth/death announcements
- ðŸŸ¡ All NPC print statements fire (extremely noisy) â€” needs player-only filter
- âŒ Full options menu
- âœ… Player stat screen ('i' key) â€” level, cultivation, potential, age, affiliation, trait, profession, wealth, equipment, techniques, sect
- âœ… Known locations display ('l' key) â€” all known areas with coordinates, city/sect flags
- âœ… Relationship list ('r' key) â€” all living relationships with label and value
- âŒ Auto-movement direction for N turns
