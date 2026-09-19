# Development Log — Tales of Dream and Fantasy 2

## 2026-09-19 (Features: Hero Threshold, Event Observation, Plague, Beast Tide Defense, City Succession)
- Fixed: Monster mortality restored — `dead()` no-arg now adds monsters to `remCharacters` (nulls `characters` slot); GameDriver already skips `population--` for them (`Character.java:788`)
- Added: Hero threshold — first cultivator to reach Ensoulment/Ascension/Pseudo Immortal prints a `[HERO]` world event; flags on `Map.java` (`Character.java:765`, `Map.java`)
- Added: `[WORLD]` summary line each year — areas, sects, cities, legacy count for system verification (`GameDriver.java`)
- Added: City succession tracking — snapshot owners at year start, print `[SUCCESSION]` on change, clean up dead owner refs (`GameDriver.java`)
- Fixed: Plague calamity now kills ~30% of cultivators in a random populated area instead of requiring a rare city; fires reliably every time it rolls (`GameDriver.java`)
- Added: Beast tide defense — cultivators present in a struck city reduce casualties proportional to their combined majorLvl power; prints saved count (`GameDriver.java`)
- Verified: 1000-year run clean; plague fires ~10 times, two hero events (Ensoulment + Ascension) confirmed; no crashes

## 2026-09-19 (1000-Year Run / Bug Fix Session)
- Fixed: `Area(Area, char)` constructor never initialized `backpack`, `materialBag`, `pillBag`, `arrayBag` — all null for every area except Originis; added 4 initializations (`Area.java:~263`)
- Fixed: `dead(Character killer)` NPE when `killer` is null — `arrayBreakAttempt` calls `dead(null)` for trap deaths; added null-guard redirecting to no-arg `dead()` (`Character.java:839`)
- Verified: 1000-year autonomous run completes clean (500 starting pop → 6,395 at year 1000; beast tide collapses pop to 140 at year 10 then steady recovery; no crashes)

## 2026-09-19 (NPC Audit / Bug Fix Session)
- Fixed: NPE in `quest()` after mid-quest death — added `if (dead) return;` guard and `currentQuest != null` checks before resolution block (`Character.java:~880`)
- Fixed: NPE/CME in GameDriver main loops — both character loops now iterate snapshot (`new ArrayList<>(characters)`) with null+dead guard (`GameDriver.java:~350`)
- Fixed: Heaven-Sent Treasure NPE — replaced `characters.get(rand.nextInt(...))` with first-non-null-non-dead scan (`GameDriver.java:~255`)
- Fixed: NPE in beast constructor — added all missing field initializations (`relationshipList`, collections, etc.) to `Character(Area, Character)` (`Character.java`)
- Fixed: NPE in birth constructor — added `affiliationNum = 0` before sibling `changeRelationship` call (`Character.java`)
- Fixed: IndexOutOfBoundsException in `dead()` relationship cleanup — added bounds check before `characters.get(friendID)` in both `dead()` overloads (`Character.java`)
- Fixed: Duplicate `dead()` calls — added `if (dead) return;` idempotency guard to both overloads (`Character.java`)
- Fixed: Monster trait mishandling in `dead(killer)` — added `trait != Traits.Monster` to all trait checks so monsters don't go through NPC death path (`Character.java`)
- Fixed: **Negative population bug** (root cause) — no-arg `dead()` called `remCharacters.add(this)` unconditionally; added early return for Beast/Monster to skip remCharacters (`Character.java:786`); also added trait guard in GameDriver remCharacters processing loop so Beast/Monster entries never decrement population or null `characters` slots (`GameDriver.java:591`)

## 2026-09-19 (Batch 19)
- Added: Seasons — `currentSeason` static int (0=Spring…3=Winter), updated every 13 weeks; `Area.age()` applies seasonal herb/monster growth multipliers; season change printed (`GameDriver.java`, `Area.java`)
- Added: Spiritual Veins — `Area.spiritualVein` bool (1% spawn chance); occupants get +1 cultivate/tick; teal tile color in MapViewer (`Area.java`, `GameDriver.java`, `MapViewer.java`)
- Added: Bloodline Bonus — `Character.bloodlineBonus` (birth constructor; +1 per parent at majorLvl≥3); extra cultivate/tick per point (`Character.java`, `GameDriver.java`)
- Added: Heaven Calamities — 10% per year: Spiritual Storm (area level surge), Plague (city -30% pop), Heaven Fire (clear arrays or cull beasts) (`GameDriver.java`)
- Added: Reputation Titles — `Character.repTitle` field; `updateRepTitle()` sets titles based on kills/majorLvl/professionLvl; shown in `toString()` as `'Title'`; updated yearly (`Character.java`)
- Added: Auction House — cities with population≥500 auction top 3 items yearly to richest known bidder; proceeds to city owner (`GameDriver.java`)
- Added: Sect Territory Bonus — yearly: if sect members occupy 3+ distinct tiles within 5 of sect, `territoryBonus=1`; added to cultivation loop bonus (`Organization.java`, `GameDriver.java`)
- Added: Interactive GUI — `LinkedBlockingQueue<String> guiInputQueue`; `readInput()`/`readInt()` helpers replace `scan.next()`/`scan.nextInt()` in player loop; `TeeStream` captures System.out to `eventLog` (`GameDriver.java`)
- Added: MapViewer interactive controls — movement pad (N/S/E/W/Wait), action buttons (Stats/Locs/Rels/Options), Move-to-Tile buttons (click map or info panel), event log panel; player dot on map; dynamic tile scaling (`Map.range`-aware); Celestial Trial cyan + Spiritual Vein teal colors; season/title/territory shown in info (`MapViewer.java`)
- Changed: Thread.sleep 10s→500ms when guiEnabled (`GameDriver.java`)

## 2026-09-19 (Batch 18)
- Confirmed: Forbidden Technique (feature 9) already implemented — `forbiddenCheck()` boosts tempLvl by √potential, costs maxAge, was already wired into winCheck(); no change needed
- Added: Breakthrough Tribulation — fires at majorLvl 3/5/7/9 in `cultivate()`; pass chance = potential - (majorLvl-3)*5, clamped 10-90%; fail pushes back one realm + inhibitor; exceptional pass has 10% chance +1 potential (`Character.java:cultivate()`)
- Added: Dual Cultivation — 5%/turn: spouse in same tile → 3 cultivate() bonus each; best friend (value≥3) → 1 cultivate() each (`Character.java:quest()`)
- Added: Legacy Titles — characters who die at majorLvl≥7 recorded in `Map.legacyTitles + Map.legacyLvls`; living characters have 2%/turn to meditate on a legend for cultivation bonus (`Character.java:dead()`, `quest()`, `Map.java`)
- Added: Celestial Trials — once per year a random wilderness area (lvl≥20) becomes CELESTIAL_TRIAL dungeon for 10 ticks; notifies characters within 15 tiles; on completion: 3 beast fights, 20 cultivate, 25% potential, 10% technique (`Area.java` new DungeonType, `GameDriver.java`, `Character.java:targetQuests()`)
- Added: Beast Tide — yearly check: Beast Lair with monsters > lvl*2000 spreads 1/4 of overflow to 4 cardinal neighbors; city neighbors lose up to 10% population (`GameDriver.java`)
- Added: Heaven-Sent Treasure — every 3 years a high-potential technique descends in a random wilderness area; broadcasts location to characters within 20 tiles; first arrival claims it via "Heaven Treasure" target quest (priority 70) (`Area.java heavenlyTechnique`, `GameDriver.java`, `Character.java`)
- Added: Weapon Enchantments — Innovative characters have 5%/turn chance to enchant equipped weapon (Sharp/Spiritual/Swift/Heavy/Fierce/Wrathful/Sundering, +1–5 bonus); bonus adds to tempLvl in `tempLvlCheck()`; shown in Item.toString() (`Item.java`, `Character.java`)
- Added: Trade Caravan growth — after Merchant sells at city (Go Sell or Sell Stock), city.population grows by earnings/1M; Evil characters may queue "Rob Caravan" target quest against nearby wealthy Merchants (5%/turn) (`Character.java`)
- Changed: Hotspot faction wars — hotspot-area squabbles now add 2 per year (vs 1 for dungeons); war threshold lowered from 5 to 4 (`GameDriver.java`)

## 2026-09-19 (Batch 17)
- Added: `MapViewer.java` — optional read-only Swing map viewer (31×31 tile grid, biome/feature color coding, cultivator density brightness, click-to-inspect info panel with tile details + top cultivators); interactive action hooks stubbed as comments for future expansion
- Changed: `GameDriver.java` — "Enable map viewer? Y/n" prompt at startup; `guiEnabled` + `mapViewer` static fields; `mapViewer.refresh()` call at end of each tick (after cleanup, before sleep)

## 2026-09-19 (Batch 16)
- Added: "Hunt Cultivator" spot quest (priority 55) to `monsterQuestInitializer()` — Monsters now actively target cultivators in their tile via `battleCheck()` (`Character.java:monsterQuestInitializer()`)
- Added: "Seek Territory" target quest trigger in `quest()` — 20% chance/tick, Monster migrates to highest-lvl known area with lvl > current+10; "Seek Territory" case in `targetQuests()` calls `beastBattleCheck()` on arrival (`Character.java:quest()`, `targetQuests()`)
- Added: Cultivation XP for Monster kills — when a Monster kills a cultivator in `dead()`, calls `cultivate()` up to `Math.min(victim.lvl, 20)` times; Monsters now grow from PvP kills like they do from beast kills (`Character.java:dead()`)
- Changed: Legendary milestone in `dead()` — now also grants 10 bonus `cultivate()` calls, 5% chance of +1 potential, and posts a high-priority "Bounty Hunt" quest (priority 65) at nearest city via `cityAreas` (`Character.java:dead()`)
- Added: "Hunt Beast" mission to `Organization.postMissionsYearly()` — posted when `sl >= 5`; format `Hunt|Beast|lvl|qty|bonus`; tracked in "Hunt" spot quest case (decrement remaining kills per `beastBattleCheck()`; complete when qty reaches 0) (`Organization.java:postMissionsYearly()`, `Character.java:spotQuests()`)
- Confirmed: Ghost rebirth ("Possess Womb" + "Ghost City Rebirth") fully implemented — no code change needed; item 6 was documentation-only

## 2026-09-19 (Batch 15)
- Added: Monster NPC awakening — `awakenMonster()` in `Character.java`; triggered from `winCheck()` at 10% when a Beast kills a cultivator; flips `trait=Monster`, `affiliation=Evil`, assigns legendary title prefix, sets `id=UID++`, adds to `characters` and `awakenedMonsters` (`Character.java`)
- Added: `monsterQuestInitializer()` — Monster spot quests: Hunt (priority 60) + Random (priority 25); no Profession/Test quests; battle quests accumulate via relationship system (`Character.java`)
- Added: Legendary milestone titles in `dead()` — Monster killing cultivators earns suffix at every 10 kills: Legend@10, Tyrant@20, Dread Lord@30, Immortal Terror@50; replaces existing suffix (`Character.java:dead()`)
- Added: `Map.awakenedMonsters` list — holds freshly-awakened Monsters for end-of-tick removal from `monstersList` without incorrectly decrementing population (`Map.java`, `GameDriver.java`)
- Changed: `quest()` — Monsters skip `gearNeeded` (buy-gear), `Seek Potential`, and `Buy Cultivation Pills` triggers; Monsters are predators and use no economy features (`Character.java`)
- Updated: `Game Info.md` — Monster trait entry updated; Monster quest initializer table added; cleaned Unimplemented Feature Note; Architecture section updated
- Updated: `Game Outline.md` — Monster NPC awakening flipped ❌→✅

## 2026-09-19 (6)
- Fixed: `Item` herb/metal constructor off-by-one — `this.quantity += quantity` → `this.quantity = quantity` (`Item.java:50`)
- Fixed: `toString()` True Immortal dead-flag side effect — moved rebirth logic to `dead(Character killer)` as an early-return guard; `toString()` no longer mutates state (`Character.java`)
- Added: City/sect destruction — `destroyCity(Area area)`: plunders all bags, Blood ∝ population/100, 10 cultivate() calls, 10% potential gain, coins from population, 90% pop wipeout, dissolves sect, clears city; triggered from `winCheck()` at 20% chance when Evil/Beast kills at an array-free city/sect (Originis protected) (`Character.java`)
- Added: Hunt for Money fallback — after `buyGear()` finds items but NPC still lacks a weapon or armor slot (can't afford), queues "Hunt Beast" with priority 40 toward nearest monster area (`Character.java:quest()`)
- Added: Lucky one-time corpse loot gate — when a corpse within 30 levels appears at Lucky's tile, queues one-time `"Loot Corpse"` spot quest at priority 75; handled by new `spotQuests()` case that calls `eventLootDead()` (`Character.java`)

## 2026-09-19 (5)
- Updated: `CLAUDE.md` — rewrote Current State (2026-05-28→2026-09-19), replaced 8-item stale bug list with 5 real active bugs, replaced all-done Priorities section with actual remaining work (3 unimplemented temp-doc items + 4 bug fixes); corrected `player` guard note from `if (player && ...)` to `if (player > 0 && ...)`

## 2026-09-19 (4)
- Updated: `Game Info.md` — comprehensive rewrite: fixed 10 stale bug entries (all resolved in prior sessions), added Library.java/Shop.java to file map, corrected Character.java line count (~3,900), added Ghost/IronBody/SpiritVein/HeavenlyRoot trait descriptions, corrected Merchant/Ghost traits from "stub/not implemented" to fully implemented, added Dungeon table, Sect Mission System, Library section, Shop pricing, itemPrice formula, potential gain events; reduced active bug list to 5 real remaining issues
- Updated: `Game Outline.md` — fixed "cityOwner is never set" stale bug note; fixed tax timing description from "per tick" to "per year"

## 2026-09-19 (3)
- Added: Sect mission delivery — Forage/Mine completion now checks location; if at sect, transfers materials to treasury immediately; if away, queues "Deliver Materials" target quest to return and deliver; materials actually leave character's bag on delivery (`Character.java:spotQuests()`, `targetQuests()`, `questViable()`)
- Added: City taxation — cityOwner collects `population / 100` ss per year in GameDriver's yearly tick (`GameDriver.java`)
- Changed: `eventChoosing()` — removed dead cases 0/3/4 (PvP/Forage/Mine); redistributed weights: beast 7–15%, interact 30–40%, profession 35–40%, loot 13–20%; cleaned up default-case debug print (`Character.java`)
- Changed: `event()` switch — removed empty cases 0, 3, 4; only active cases 1/2/5/6 remain (`Character.java`)
- Design: Technique rarity tiers noted as implicit in `CultivationGame2Recent.txt` — potential value (1–100) encodes rarity; sect library gating provides the access barrier

## 2026-09-19 (2)
- Added: "Return to Sect" quest — sect non-masters drift back to sect location (5%/turn when away); queued in dynamic triggers; awards 2 sectPoints on arrival; viability check cancels if sect dissolved (`Character.java:quest()`, `questViable()`, `targetQuests()`)
- Added: NPC buy cultivation pills — when at a city with pills for the next major realm, NPCs purchase one (10% chance/turn); price paid to cityOwner; economy circulation (`Character.java:quest()`)
- Fixed: `cultivate()` cases 3–10 breakthrough prints unguarded — now wrapped with `if (printBreakthroughs || (player > 0 && skipping <= 0))` to match cases 1–2 (`Character.java`)
- Fixed: `dead()` no-killer overload "has died" print unguarded — now wrapped (`Character.java:765`)
- Fixed: `battle()` debug win-chance prints (two instances) removed entirely — were always firing for every NPC fight (`Character.java`)
- Fixed: `battle()` win/lose prints — guarded with `if (player > 0 || opponent.player > 0)` (`Character.java`)
- Fixed: `deathCheck()` beast/no-relation/DEATH MATCH prints — removed (debug noise; no player-relevant info lost) (`Character.java`)
- Fixed: Marriage print, birth print, blacksmith max-level print — all now guarded with player/printBreakthroughs check (`Character.java`)
- Fixed: "Fleed Fight" debug print removed from `battle()` (`Character.java`)

## 2026-09-19
- Added: Potential gain on major realm breakthrough (`Character.java:cultivate()`) — 1% chance when `majorLvl` advances; capped at 101; prints message to player
- Added: Potential gain on surviving forbidden technique (`Character.java:forbiddenCheck()`) — 2% chance in surviving `else` branch; caps at 101
- Added: Sect rank promotion (`Character.java:quest()`) — sect masters check members each turn (5% chance); OUTER→INNER at `majorLvl >= master.majorLvl-4`; INNER→ELDER at `majorLvl >= master.majorLvl-2`
- Changed: Item pricing formula (`Character.java:itemPrice()`) — `lvl^2.5 * 10` replaces old exponential; lvl 1=10ss, lvl 10=3162ss, lvl 100=10M ss; Potential pills 5× base
- Changed: Technique mastery on library learn (`Library.java`) — city library copies start at lvl/5; sect library copies start at lvl/3
- Changed: Technique mastery on birth (`Character.java` birth constructor) — child inherits best parent technique at lvl/4 starting mastery
- Changed: Technique mastery on Gift Technique (`Character.java:targetQuests()`) — copy given at lvl/2 starting mastery

## 2026-09-18 (12)
- Added: Ghost physique trait — characters spawning with Ghost trait enter ghost form on death (not old age); can "Possess Womb" (take over unborn child stronger than mother) or "Ghost City Rebirth" (spawn as new NPC in a city); inherit Ghost trait in next body; `originalName` tracks name chain; permanent death if killed while already in ghost form
- Added: IronBody, SpiritVein, HeavenlyRoot special physique traits — IronBody adds majorLvl/2+1 to tempLvl; SpiritVein gives 1.5× cultivation speed; HeavenlyRoot doubles technique upgrade chance in winCheck(); all three added to NPC trait pickers at rare physique rarity (~1% each)
- Added: `practice()` method on Character — calls `cultivate()` + 10% chance technique upgrade in one helper; used by sparring resolution
- Added: Sparring event — 10% chance per tick when two friends (rel >= 1) meet in `interact()`; both parties call `practice()` via winCheck(); sparring flag cleared after resolution
- Added: City improvement quests — "Buy City Array" (3%/turn for city owners: purchase and install array); "Entice Merchant" (5%/turn: pay incentive to recruit a merchant to open a local shop)
- Added: `sectPopulation` on Organization (starts 1000) — grows 1%/year via `growPopulation()` called in `postMissionsYearly()`; taxes sectPopulation/100 SS to sect master yearly
- Changed: `player` field on Character — was boolean, now int (0=NPC, 1=human, 2=tracked NPC/follow mode); all `if (player && ...)` → `if (player > 0 && ...)`; `followMode` field removed, replaced by `player = 2`; fixed in Character.java, GameDriver.java, GameDriver2.java, Area.java

## 2026-09-18 (11)
- Added: Sect quest board — `ArrayList<String> questBoard` on Organization; `postMissionsYearly()` posts up to 5 herb/metal shortage missions ("Forage|Herb|lvl|qty|bonus" format); "Accept Sect Mission" spot quest (15%/turn at sect); Forage/Mine cases check active mission and award bonus sectPoints on delivery; called yearly from GameDriver
- Added: Tournament advance registration — "Hold Tournament" revamped to set 8-week `tournamentTimer` + store prize/organizer/registrants on Area; "Register Tournament" spot quest (5%/turn for eligible realm members); "Spectate Tournament" spot quest (10%/turn for non-registrants); `Area.runTournament()` runs bracket when timer hits 0; GameDriver ticks timer every week
- Added: MC creation options — on game start, player now picks (C)ustomize (trait, potential 1-100, affiliation), (F)ollow mode (auto-plays with prints, no scanner), or Enter (default Alchemist 100 potential)
- Added: Follow mode game loop — `followMode` field on Character; when set, player character auto-moves and quests without waiting for input; prints `[Follow] <char><loc>` each turn
- Added: Merchant equips from own stock — after `restockFromCity()`, merchant scans own shop inventory and equips better weapons/clothing into their own gear slots, returning old gear to shelf
- Added: Technique gifting — parent characters gift their best technique to children (5%/turn); "Gift Technique" targetQuests case delivers a copy to child at their location; `Technique(Technique)` copy constructor used
- Added: Get Pregnant quest — married female characters generate "Get Pregnant" targetQuest (2%/turn); handled in targetQuests(); added 10% probability gate to `checkBirth()` so NPC births aren't instant every encounter
- Added: Find Parent's Body quest — both `dead()` overloads now notify all born living children; "Find Parent's Body" targetQuests case awards +1 potential on 20% chance
- Changed: "Hold Tournament" case — now sets 8-week registration window instead of running immediately
- Changed: winCheck() — sparring win now gives +1 relationship to both parties; non-sparring win now has 20% chance to upgrade winner's martial technique
- Fixed: `sell()` cultivation pill level check — `item.lvl <= majorLvl` → `item.lvl <= majorLvl * 10` (item levels are 1-100, not 0-10)

## 2026-09-18 (10)
- Added: Sect treasury system — `ArrayList<Item> treasury` on Organization stores donated weapons/clothing/herbs/metals/pills; `sectLvl()` computes sect advancement level as `sum(herb+metal qty×lvl) / 10000` capped at 100; `donationPoints()`/`buyingCost()` use proportional formula (herbs/metals: qty×lvl/1000, pills: qty×lvl×2, gear: lvl)
- Added: "Donate to Sect" spot quest — triggers at sect home location when member has spare backpack items or surplus materials/pills (10%/turn); donates all non-equipped backpack items + herb/metal surplus above 2000 + pill surplus above 5 reserve; awards proportional sectPoints
- Added: "Buy from Sect" spot quest — triggers when member has sectPoints, treasury has items, and member needs materials (by profession) or better gear (10%/turn); deducts sectPoints equal to donation value (fair exchange)
- Changed: Sect cultivation bonus in GameDriver — now `Math.max(founder.majorLvl, sect.sectLvl() / 10)` so treasury accumulation also boosts members' cultivation speed
- Changed: 'i' stat screen — sect line now shows sect level and treasury item count

## 2026-09-18 (9)
- Added: Tournament quest — "Hold Tournament" spot quest for city owners and sect masters (2%/turn); single-elimination bracket with probability-based outcomes (no death risk); major-realm band ±1 around organizer's level; organizer funds prize at `(maxBand+1)*10 + rand(11)` level; winner receives weapon or clothing prize
- Added: Sect contribution points earning — Hunt Beast (+5), Explore Dungeon (+10), Sect Raid (+20), Forage/Mine while in sect (+2), donating unique technique to sect library (+10 in `technique()`)
- Added: Innovative technique selling — "Sell Technique" spot/area quest (10% chance/turn); travels to nearest city library and sells best unique technique; `Library.sellTechnique()` pays seller; `technique()` also auto-donates a copy to sect library with 20% chance
- Added: City library initialization — `cityLeadership()` and Originis constructor now add a `Library` to `area.libraries`
- Added: Relationship recycling on death — `dead()` and `dead(Character killer)` now remove the deceased from all living friends' RelationshipLists; prevents stale relationship accumulation
- Changed: 'i' stat screen — now shows `kills`, `sectPoints`, and sect rank alongside sect name
- Changed: 'o' auto-move menu — expanded to: `x` (wait/quest-auto), `w/a/s/d` (directional N turns), `c` (coordinate pathfind: enter tile X Y then N turns), `m` (random N turns); added `autoX`/`autoY` fields to Character for coordinate tracking
- Fixed: Sect Raid `targetQuests()` — broadened condition to allow crisis raids (any enemy sect at location, not just warTarget); sectPoints awarded on success

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
