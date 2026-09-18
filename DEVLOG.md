# Development Log — Tales of Dream and Fantasy 2

## 2026-09-17 (8)
- Added: Sequential tag-team party battle — `partyBattleCheck(Character opponent)` in Character.java; positive-relationship allies at same location join in sequence; enemy accumulates `exhaustionPenalty` (+2 per fighter) applied at end of `tempLvlCheck()`; "Battle Rival", "Battle Arch-Enemy", "Revenge", "Bounty Hunt" in `targetQuests()` now call `partyBattleCheck()` instead of `battleCheck()`
- Added: `int exhaustionPenalty` field on Character; applied in `tempLvlCheck()` after base calculation
- Removed: 3 residual debug prints from `battle()` — TempLvl and win-chance lines
- Added: Alliance system — `isAlly()`, `proposeAlliance()`, `dissolveAlliance()` on Organization; `declareWar()` now auto-notifies allies of the defender and dissolves alliances between combatants; "Propose Alliance" quest: sect masters seek non-enemy sect masters (5% chance/turn)
- Added: Resource disputes — yearly check in GameDriver.java; pairs of sects whose masters share a dungeon/hotspot `knownLocation` earn +1 squabble; at 5 squabbles war is declared
- Added: Tribute system — "Demand Tribute" Character quest (2%/turn) for sect masters with 20+ level lead over a rival master; target pays 10M ss or war is declared
- Added: Succession crisis — `crisisTimer` field on Organization; `promote()` sets crisisTimer=52 when no Elder exists; crisis sects can be opportunistically raided by any sect master/elder without requiring war; GameDriver ticks crisis timer down each year
- Added: Defection — "Recruit Defector" Character quest (1%/turn) for sect masters at war; 40% chance a target Elder defects, joining the recruiting sect and triggering war with their original sect
- Added: `inCrisis()`, `isAlly()`, `proposeAlliance()`, `dissolveAlliance()` methods to Organization.java

## 2026-09-17 (7)
- Fixed: Crafting level cap formula — `Math.min((majorLvl + 1) * 10, 100)` hard-caps at 100; majorLvl 10 crafters still cap at lvl 100 items
- Removed: Debug print noise — removed "One Time Target/Spot Quest finished", "spotQuests queue empty", pathfinder reach-target, "has found", "Weird Case?" prints from Character.java
- Fixed: Join Sect quest now gates through `canJoin()` so sect criteria (potential, level, gender, affiliation, trait) are respected
- Added: Kill counter — `int kills` field on Character; incremented for beast-kills-cultivator, cultivator-kills-beast, and cultivator-kills-cultivator in `dead()`
- Added: `int sectPoints` field on Character for future sect contribution point use
- Added: Sect squabble tracking in `battleCheck()` — cross-sect fights increment `sect.squabbles`; at 5 squabbles, `declareWar()` is called and both sects enter war state
- Added: `DungeonType` enum to Area — LOST_RUINS, BEAST_LAIR, MARTIAL_TRIAL, HAUNTED_GROUNDS; 5% chance per newly-created area
- Added: `baseLvl` and `hotspot` fields to Area for hotspot spike/reset
- Added: `ArrayList<Library> libraries` field to Area
- Added: Yearly hotspot generation in GameDriver.java — at year start, reset previous hotspots (restore baseLvl) then spike 3-5 random non-city areas by +10 to +30 levels
- Added: `hotspotAreas` tracking list to Map.java
- Added: `SectRank` enum to Organization (OUTER_DISCIPLE, INNER_DISCIPLE, ELDER, MASTER, FOUNDER)
- Added: `memberRanks` HashMap, `squabbles`, `warTarget`, `allies` fields to Organization
- Added: `canJoin()`, `getRank()`, `promoteRank()`, `declareWar()`, `endWar()`, `isAtWar()` methods to Organization
- Changed: `join()` now assigns OUTER_DISCIPLE rank; `resign()` removes rank; `promote()` dissolves sect and ends war if last member leaves
- Added: `sectLibrary` field to Organization (initialized in constructor)
- Added: "Explore Dungeon" Area quest — 16% chance/turn for adult non-beasts to seek a level-appropriate dungeon; LOST_RUINS (treasure + technique find), BEAST_LAIR (3× beastBattleCheck), MARTIAL_TRIAL (tempLvl vs trial power, technique upgrade on win), HAUNTED_GROUNDS (cultivation + rare potential gain)
- Added: "Sect Raid" Area quest — sect masters and elders travel to enemy sect location and attack; war ends when enemy sect master dies or sect dissolves
- Created: `src/Game/Library.java` — technique library class; city libraries sell technique copies for ss; sect libraries require sectPoints; Innovative sellers can contribute techniques; includes TODO note to flesh out family techniques, mastery levelling, and rarity tiers

## 2026-09-17 (6)
- Fixed: `canAfford(long)` — now checks all currency tiers (ss + sc×1M + dc×1T + ij×1E); previously only checked `ss`
- Fixed: `deductSs(long)` — cascades draw-down from sc→dc→ij before depleting ss; previously capped at ss only
- Fixed: `itemPrice()` overflow — caps `(long)Math.pow(100, lvl/10)` at `Long.MAX_VALUE` for lvl > ~92; Potential Pills now safely multiply without overflow
- Fixed: Quest queue cleanup on death — `dead()` and `dead(Character killer)` now clear `targetQuests`, `spotQuests`, and `currentQuest` to prevent dangling references after death
- Added: Buy Gear travel quest — when gear is outdated/missing but not in a city with shops, queues "Buy Gear" Area quest to nearest known city with shops; viability check + `targetQuests()` case added
- Added: "Seek Potential" Area quest — triggers when `potential < 101 - (9 - majorLvl)^2` (insufficient for next breakthrough); travels to city with Potential Pills in `pillBag`; buys and consumes pill on arrival
- Added: Bully trigger in `quest()` — Evil characters with 15+ level advantage over a co-located character queue "Battle Dislike" targeting them (10% chance/turn)
- Changed: `battleCheck()` — added affiliation rules: Good characters won't attack non-Evil unless arch-enemy (value ≤ -3); Neutral won't attack non-Evil unless rival or worse (value ≤ -2)
- Changed: `smithing()`, `tailoring()`, `array()` — crafted item level now capped at `majorLvl * 10 + 50` (5 major realms above crafter's cultivation)
- Added: `weaponAdjectives`, `clothingNames`, `clothingAdjectives`, `arrayTypes`, `arrayAdjectives` word lists to `Dictionary.java`
- Changed: `Item` constructors — Weapon names now use random adjective + weapon type; Clothing uses random adjective + clothing name; Array uses random adjective + array type
- Added: Player stat screen (`i` key) — shows level, cultivation, potential, age, affiliation, trait, profession, wealth, equipment, techniques, sect
- Added: Known locations display (`l` key) — lists all known areas with coordinates, city/sect flags
- Added: Relationship list (`r` key) — shows all living relationships with label and value; spouse highlighted separately
- Added: `canJoin(Character)` method to `Organization.java` — checks `minPotential`, `minLevel`, `genderPref`, `affiliationMatch`, `traitRequired` criteria; `join()` now gates through `canJoin()`
- Added: Recruitment criteria fields to `Organization.java` — `minPotential`, `minLevel`, `genderPref`, `affiliationMatch`, `traitRequired`
- Added: "Recruit Member" Character quest trigger in `quest()` — sect masters (10% chance/turn) seek eligible candidates in current location using `canJoin()`; `targetQuests()` case calls `interact()` then `sect.join()`

## 2026-09-17 (5)
- Added: `arrayBreakAttempt(Area area)` — attacker fights strongest array first; removes it on break (win OR lose); 5% death chance on loss; win chance uses `50 - 15*(arrayPower - tempLvl)` where `arrayPower = array.lvl / 5`
- Added: `installArray(Area area)` — moves first item from `arrayBag` to `area.arrayBag`
- Changed: Scribe `professionPicker()` — auto-installs crafted arrays at current location via `installArray(location)`
- Changed: `battleCheck()` — beasts are blocked entirely by area arrays; human attackers must call `arrayBreakAttempt()` before engaging defender; if array is stronger than attacker (`arrayPower > this.tempLvl`), defender won't interfere and attacker only fights array
- Changed: `battle()` — defender gets `bestArrayLvl / 10 + 1` bonus to `tempLvl` from arrays in their area (after both `tempLvlCheck()` calls, before win chance)
- Added: `findBestRestockCity()` — scores known cities by pool size ×10 (Option A), own-shop presence +50 (Option B), proximity bonus up to +30 (Option C); returns highest-scoring city with a non-empty backpack
- Added: Restock Trip Area quest trigger in `quest()` — Merchant with ownedShops has 10% chance/turn to queue travel to best restock city
- Added: Restock Trip viability in `questViable()` Area branch — invalidates if city lost owner or pool is empty
- Added: Restock Trip case in `targetQuests()` — opens shop at new city if under cap, restocks all owned shops there

## 2026-09-17 (4)
- Fixed: Buy Gear trigger and `buyGear()` item level comparison — now uses `character.lvl` (1-100) vs `item.lvl` (1-100) instead of `majorLvl` (0-10); one major realm = 10 item levels; trigger fires when `equipped.lvl + 10 <= lvl`; `buyGear()` buys items at `item.lvl >= lvl - 9`
- Changed: Leader 20/80 split in `professionPicker()` — at professionLvl 1, 80% skip straight to sect track (`professionLvl = 50`), 20% try city first; both then attempt `sectLeadership()` at professionLvl 50
- Added: Hunt Beast Area quest — 20% chance per turn to queue travel to a known area with monsters within ±10/+15 levels; `beastBattleCheck()` called on arrival; viability check invalidates if area runs dry
- Added: Join Sect Area quest — 5% chance per turn for non-Leader adults with no sect to seek a known sect location; calls `Organization.join()` on arrival; low priority (20, max 40); viability check invalidates if sect dissolves or character already joined
- Added: Bounty Hunt system — when a close friend is too weak for direct revenge, they post a `Quest("Bounty Hunt")` to the nearest known city's `area.bounties`; characters within 10 levels of the target automatically pick up posted bounties at cities and add to their `targetQuests`; calls `battleCheck()` on target arrival; added `ArrayList<Quest> bounties` to `Area.java`
- Changed: `targetQuests()` "Battle Dislike" — no longer falls through to battleCheck(); now calls `bullyCheck()` which steals 25% ss when 15+ levels stronger, otherwise escalates to battle
- Added: `bullyCheck(Character target)` — intimidation mechanic for Dislike quest; significantly weaker targets lose 25% ss and relationship decreases; comparable/stronger targets fight back
- Changed: `itemPrice()` — Potential Pills priced at 5× base (require deaths/blood, so more valuable); all other items unchanged
- Changed: `sell()` — pill selling implemented; sells cultivation pills at or below current majorLvl (past-realm, useless); sells Potential Pills only if potential >= 100; pills go to city.pillBag; print now includes pill count and pill earnings

## 2026-09-17 (3)
- Fixed: Buy Gear trigger in `quest()` — now also fires when equipped weapon or clothing is a full major level below character's `majorLvl` (previously only fired when slot was empty)
- Fixed: `buyGear()` purchase threshold — replaced `item.lvl > equipped.lvl + 3` with `item.lvl >= majorLvl - 1 && item.lvl > currentLvl`; character now buys gear that brings them up to their current realm rather than waiting for a 3-level gap

## 2026-09-17 (2)
- Added: `Design Notes` section to `CLAUDE.md` — documents Array defense design (arrays protect area features, give defenders temp level boost, multiple arrays don't buff each other, must break array AND beat defenders)
- Added: `Design Doc Update Rule` to `CLAUDE.md` — rule to append design decisions to relevant design docs each session
- Created: `src/Game/Shop.java` — per-merchant shop class with `buyRate=0.55`, `sellRate=1.50`, `cityBuyRate=0.30`; `buyFromSeller()`, `sellToBuyer()`, `restockFromCity()` methods; Non-Merchants cannot access city pool directly
- Added: `ArrayList<Shop> shops` field to `Area.java` — initialized empty on all areas
- Added: `ArrayList<Shop> ownedShops` field to `Character.java` — initialized in both constructors
- Added: `maxShops()` — returns `Math.max(1, majorLvl)` as shop cap
- Added: `canAfford(long)` and `deductSs(long)` helpers to `Character.java`
- Added: `buyGear()` to `Character.java` — checks shops in current city for weapon/clothing upgrades (lvl+3 threshold, 25% wealth cap)
- Updated: `sell(Area city)` — prefers merchant shops at buyRate=0.55 over city pool at 50%; only city-pool items use backpack.removeAll; merchant sales handled by `buyFromSeller()`
- Added: Merchant case to `professionPicker()` — sets up shop in city when under maxShops cap; restocks from city pool when at own shop
- Added: Buy Gear trigger in `quest()` dynamic triggers — calls `buyGear()` immediately when in a city with shops and missing equipped weapon or clothing

## 2026-09-16
- Setup: Added `## Development Workflow` section to `CLAUDE.md` — establishes dev branch strategy, compile/smoke-test steps, and doc-update rules
- Setup: Created `DEVLOG.md`
- Fixed: `changeRelationship()` NPE (`Character.java:1366,1369`) — null-checked `currentQuest` before access and corrected boolean OR logic (stream result was being overwritten instead of combined)
- Cleaned: Removed 5 unguarded `System.out.println` debug calls from `changeRelationship()` (`Character.java`)
- Fixed: `spotQuests()` always returned 0 (`Character.java:944`) — changed `return 0` to `return questSuccess`; spot quests now correctly re-enter the priority queue on success (non-oneTime) or get removed (oneTime)
- Fixed: `event()` case 1 fall-through (`Character.java:1024`) — added `break` so beast battle no longer also triggers interact
- Fixed: `professionPicker()` fall-throughs (`Character.java:1073,1075`) — added `break` after Friendly and Tailor so each trait runs only its own profession
- Fixed: `forbiddenCheck()` zombie characters (`Character.java:2000,2011,2034,2042`) — added dead-checks in `battle()` after each forbiddenCheck call; if overextension killed the user, call `dead(opponent)` and return immediately rather than leaving the character as a zombie
- Fixed: `checkRelationship()` string comparison (`Character.java:1473,1474`) — `q.name == "Find Person"` → `"Find Person".equals(q.name)`; removeIf was never firing
- Fixed: `checkRelationship()` NPE duplicate (`Character.java:1487,1490`) — same currentQuest null-check pattern as changeRelationship(); merged into stream OR with null guard
- Cleaned: Removed 10 unguarded `System.out.println` debug calls from `checkRelationship()`
- Fixed: Technique slot duplication in `equip()` (`Character.java:1789,1792`) — added `!techniqueList.contains()` guard before adding technique2 slots; repeated equip() calls were duplicating techniques into the list
- Fixed: Stale quest target area (`Character.java:832`) — refresh `currentQuest.targetArea` from live character location before each pathfinder() call for Character-category quests
- Confirmed: `knownLocations.add(location)` already present in `movement()` (`Character.java:507`) — Beast-guarded with contains check; no change needed
- Added: "Forage" and "Mine" spot quest cases to `spotQuests()` — call forage()/mine() in current tile, return success 2
- Added: Material pre-req quest queuing in `professionPicker()` — Alchemist keeps inline forage() and now also queues "Forage" spot quest on failure; Blacksmith queues "Mine" if no Metal qty>=1000; Tailor queues "Forage" if no materialBag item qty>=1000; Scribe queues "Forage" if no materialBag item qty>=10000
- Added: Battle quest creation in `changeRelationship()` — value -1 queues "Battle Dislike" (priority 20), -2 queues "Battle Rival" (priority 35), -3 queues "Battle Arch-Enemy" (priority 50); deduped per target per name; both sides receive the quest
- Added: "Battle Dislike"/"Battle Rival"/"Battle Arch-Enemy" cases in `targetQuests()` — all call battleCheck(); Dislike has TODO comment for bullyCheck() when bully system is implemented
- Fixed: Tailor material check in `professionPicker()` — now checks Herb and Metal separately; queues Forage if area has herbs and herbs are low, Mine if area has metals and metals are low; may queue both
- Fixed: NPE in `dead()` (`Character.java:773`) — null check added for `friend` before accessing `.dead`; previously crashed if a friend had been nulled out in Map.characters
- Added: Revenge quest in `dead()` — close friends (value>=2) get "Revenge" Character quest (oneTime, priority 60) targeting the killer when `friend.majorLvl >= killer.majorLvl - 1`; weaker friends skip (bounty/squad revenge deferred)
- Added: "Revenge" case to `targetQuests()` — calls battleCheck() on the killer
- Added: Currency tier fields to Character — `sc` (Spirit Crystals), `dc` (Divine Crystals), `ij` (Immortal Jades); conversion rate 1,000,000:1 per tier
- Added: `convertCurrency()` — rolls ss→sc→dc→ij when any tier exceeds 1,000,000; called after sell()
- Added: `itemPrice(Item)` static helper — placeholder formula `100^(lvl/10)`; prices to be confirmed in economy pass
- Added: `sell(Area city)` — sells all backpack items except equipped weapon/clothing and one spare of each; transfers items to city.backpack; pays ss at itemPrice(); calls convertCurrency(); pill selling deferred with TODO
- Fixed: `cityLeadership()` — added `location.cityOwner = this`; unblocks tax collection and Go Sell quest validation
- Added: Go Sell quest trigger in `quest()` — when backpack.size() > 20, finds first known city with an owner, queues "Go Sell" Area quest (oneTime, priority 50)
- Added: "Go Sell" case to `targetQuests()` — calls sell() on arrival at city
- Added: "Go Sell" viability check in `questViable()` Area branch — invalidates quest if city was destroyed or owner is gone
