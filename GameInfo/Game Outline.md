# Tales of Dream and Fantasy 2 — Implementation Outline

Legend: ✅ Implemented | 🟡 Partial / Skeleton | ❌ Not Started

**Status at the final pass (2026-10-21):** every planned feature is built. The 🟡 and ❌ lines below are decisions, not gaps: the song generator (experimental, off by default), parallel NPC turns (experimental, off by default), moving beasts and the awakened-beast start (not planned), the performance and balance notes (measured, left as they are), and the Control Audit items from 6.18 on (not started by choice). Tests: `tools/probes/README.md`.

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
- ✅ Dungeon types — LOST_RUINS (treasure+technique), BEAST_LAIR (3× beasts), MARTIAL_TRIAL (technique upgrade), HAUNTED_GROUNDS (potential gain), CELESTIAL_TRIAL (temporary 10-tick event; 3 boss fights + 20 cultivate + 25% potential + 10% technique); 5% spawn chance per area
- ✅ Beast Tides — yearly: Beast Lairs with monsters > lvl*2000 overflow to 4 cardinal neighbors; neighboring cities lose up to 10% population
- ✅ Heaven-Sent Treasure — every year one treasure (technique, instrument, poison, Potential Pill, regular pill, clothes, weapon, array; `Treasure`) appears in a random wilderness area; claimed items can be auctioned; notifies characters within 20 tiles; "Heaven Treasure" priority-70 target quest; technique is first-come-first-served via `area.heavenlyTechnique`

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
- ✅ Breakthrough Tribulation — fires at majorLvl 3/5/7/9; chance = `clamp(10-90, potential - (majorLvl-3)*5)`; pass: 10% chance of a pill's worth of potential + cultivation boost; fail: realm regression + inhibitor
- ✅ Dual Cultivation — 5%/turn: spouse in same tile → 3 cultivate() bonus each; best friend (value≥3) in tile → 1 cultivate() each
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
- ✅ Monster NPC awakening — Beast that kills a cultivator has 10% chance to flip trait Beast→Monster; gains a legendary title prefix (Ancient/Dread/Elder/etc.), becomes Evil-aligned, enters main NPC loop (movement + quests + relationships) via `awakenMonster()`; earns kill-milestone title suffixes (Legend@10, Tyrant@20, Dread Lord@30, Immortal Terror@50); gains cultivate() XP from cultivator kills (up to 20 calls); milestone grants +10 cultivate + 5% potential + city bounty posting; "Hunt Cultivator" spot quest + "Seek Territory" target quest give full aggressive AI; sect Hunt Beast missions
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
- ✅ `cityLeadership()` — Leader creates city (Major Lvl ≥ 3)
- ✅ `sectLeadership()` — Leader creates sect (Major Lvl ≥ 2)
- ✅ Profession exp + level cap at 100
- ✅ `professionPicker()` dispatch — fall-throughs fixed (Friendly/Tailor breaks added)
- ✅ Merchant profession — Shop.java; sets up shops, restocks local pool, equips from own stock; city-to-city arbitrage: buys cargo at 30% → carries to demand city → sells at 100% via "Sell Stock" quest
- ✅ Library/Technique selling — city libraries sell technique copies for ss (lvl/5 starting mastery); sect libraries gate on sectPoints (lvl/3 starting mastery); Innovative can sell; birth inheritance lvl/4; Gift Technique lvl/2; rarity tiers implicit via technique potential value (1–100)
- ✅ Pre-req check — Forage/Mine queued from `professionPicker()` when materials missing (Alchemist, Blacksmith, Tailor, Scribe branches)
- ✅ Crafting level cap — items capped at `(majorLvl + 1) * 10` capped at 100 max; crafter can make 1 realm above their cultivation
- ✅ Selling excess items — Go Sell quest at backpack > 20; `sell()` transfers surplus to city pool
- ✅ Technique selling (Innovative only) — "Sell Technique" spot/area quest; travels to city library; `Library.sellTechnique()` pays seller; auto-donates copy to sect library 20% chance
- ✅ Library (2026-10-03) — one book for each (type, potential); a sale pays a 20% fee and the seller loses the technique (the new book replaces the old); potential < 4 and equipped techniques are never sold; GUI sort and filter, terminal `library` filters
- ✅ Auctions (2026-10-03, `Auctions`) — only luxuries (Heaven-Sent items, Potential Pills, vintage goods); consigned by `sell` (90% to the consignor); held when 5 lots wait or 6 weeks after the first; NPCs bid for what they want; Potential Pills are never sold in shops
- ✅ Refine Blood (2026-10-03) — a non-alchemist takes 1,000+ Blood to an alchemist for Potential Pills (fee = the pill's price)
- ✅ Shops without a stock cap (2026-10-03) — a shop buys anything its owner can afford
- ✅ Manual duties (2026-10-03) — duty quests walk a MANUAL player only on `watch` turns; actions, `stay`, `wait` and the arrival of a walk never move you
- ✅ Amateur craft (2026-10-06) — every character can craft eight kinds (menu on the Craft button, `craft <kind>`); cheaper trade crafting (500 units) and half the break chance; NPCs without gear make a plain set
- ✅ Quest profiles per trait, stage of life and alignment, with fatigue (2026-10-08) — `QuestLife`: eight families, target shares, interest bends how often a quest is thought of and which is taken
- ✅ Eleven routine quests (2026-10-08) — Visit Family, Teach Disciple, Courtship, Trade Run, Pilgrimage, Meditation Retreat, Tame Beast, Buy Pills, Attend Auction, Found City, Found Sect
- ✅ Spirit veins draw retreats, dual cultivation, cities and sects (2026-10-08)
- ✅ Five more routine quests: Market Trip, Gathering Trip, Seek Teacher, Sightseeing, Courier Run (2026-10-08)
- ✅ Gatherer and Miner traits gather; a materials market between them, the cities and the craftsmen (2026-10-08)
- ✅ Place names revert when their city or dungeon is gone (2026-10-07)
- ✅ Map zoom 1x-5x, Tile pics cached for the whole map (2026-10-07)
- ✅ At least 25 cities and 25 sects; Evil razes only above 25 of that kind (2026-10-07)
- ✅ Gear reaches buyers: crafters sell sooner, shops first then the city pool; nearby dungeon/hunt targets with rests, smiths and tailors can use richer material (2026-10-07)
- ✅ Instruments mature instead of being crafted; Perform quest and `perform` (2026-10-07)
- ✅ Study Trip (2026-10-06) — travel to a library with a better technique you can afford and buy it
- ✅ Squad and friendship policies (2026-10-06) — ask / auto / never, ask / accept / decline, `avoid`; MANUAL asks by default
- ✅ Sect mission in the Quest Log, choose from the board (2026-10-06) — `mission`, pinned `Sect Mission` quest
- ✅ Info pop-ups work while paused, windows fill quickly (2026-10-06)
- ✅ Cheats for Cheaters only, Edit Self potential locked, portrait follows gender, Wandering Immortal craft gifts (2026-10-06)

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
- ✅ Priority increment per tick (`priorityDiff`) via `Character.bump()` — clamped to `maxPriority`, heap rebuilt; repeating quests release after success
- ✅ Relationship quest tiers — weak ties capped at 4 and replaced with new cultivators; strong ties persist; target queue capped at 50 with 10-year expiry for one-time quests
- ✅ "Meditate" low-priority spot quest (replaced "Test")
- ✅ City levels from treasury (a tenth of every sale, 2026-10-07); shop slots (3/level) and shop level cap; epoch purge when a turn takes over 2 s (20% of Major Lvl 0-3, then half a Potential Pill of potential for the survivors of the culled realms)
- ✅ Crowd hotspots — tiles over 3.5 times their fair share of the starting population (`settings crowd`, map-area scaled, cities 4x) get beast ambushes until thinned; Originis exempt; "Disperse" quest
- ✅ Sect raid cooldown (13 weeks per sect); bounded city population / overflow stoppers
- ✅ Full-codebase review pass (2026-09-29) — 40+ defects fixed, see DEVLOG
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
- ✅ Rob Caravan quest — Evil characters, 5%/turn, target wealthy Merchants (ss ≥ 1000) in same tile via priority-50 target quest; calls `partyBattleCheck(merchant)` on arrival
- ✅ Heaven Treasure quest — priority-70 target quest fired when character knows an area with `heavenlyTechnique != null`; claims technique on arrival (first-come-first-served)
- ✅ Legacy Discovery — 2%/turn, meditate on a random entry from `Map.legacyTitles`; `min(legLvl/10, 10)` cultivate() calls

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
- ✅ Trade Caravan population growth — Merchant city sales (Go Sell / Sell Stock) increase city population by earnings/1,000,000 per sale
- ✅ **Item pricing** — formula: `lvl^2.5 * 10` (lvl 1=10ss, lvl 10=3162ss, lvl 100=10M ss); Potential pills 5× base; multi-currency canAfford/deductSs correct
- ✅ City tax routing — `cityOwner` set by `cityLeadership()`; GameDriver yearly loop pays `cityOwner.ss += population / 100`; sect population also taxed via `Organization.growPopulation()`
- ✅ Leader/city 20%/80% split — NPC Leaders: 80% skip to sect track, 20% try city first

---

## Relationship System

- ✅ `Relationship` / `RelationshipList` objects (capped at 100 per person since 2026-10-09)
- ✅ `changeRelationship()` — adds new relationships, updates values
- ✅ `changeRelationshipUtil()` — probability-based change calculation
- ✅ `checkRelationship()` — handles marriage trigger, Find Person quest updates
- ✅ Marriage logic (both adults, single, opposite gender, not related)
- ✅ Sibling detection
- ✅ `checkBirth()` — pregnancy trigger from spouse interaction
- ✅ Relationship removal string comparison fixed — `==` → `.equals()`; NPE guard added
- ✅ Relationship recycling on death — deceased removed from all living friends' RelationshipLists in `dead()`; Revenge quest queued for close friends (value ≥ 2) within 1 major realm of killer
- ✅ Kill counter — `kills` field on Character; incremented on combat death, beast kill, and Ghost form entry
- ✅ Legacy Titles — characters dying at majorLvl≥7 recorded in `Map.legacyTitles + Map.legacyLvls`; living characters have 2%/turn to meditate on a legend for cultivation XP (min(legLvl/10, 10) cultivate calls)
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
- ✅ Weapon Enchantments — Innovative characters have 5%/turn chance to enchant their equipped weapon (Sharp/Spiritual/Swift/Heavy/Fierce/Wrathful/Sundering, +1–5 bonus); bonus added to tempLvl in `tempLvlCheck()`; displayed as "[EnchantName +N]" in Item.toString()

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
- ✅ Player-control modes — MANUAL / AUTO / HYBRID per human character (`PlayerControl`); `auto [n]`, `manual`, `stop`; tracked NPCs (`player == 2`) run the NPC pipeline
- ✅ Manual-mode quest suppression + alerts — routine quests ignored, notable ones announced with target/coordinates/distance/reason (`inbox`); "Do Quest" (`qdo`) checks viability and location before running the real handler
- ✅ Quest tools — numbered view, pinned (non-aging) priorities, delete, mute, waypoint quests
- ✅ Managed duties — ten toggles + master `duties all on|off`; "Get Pregnant" displays as "Have Child"
- ✅ Decision prompts — kill/spare, flee, forbidden technique, accept/evade duel via `DecisionBroker`, with policies for AUTO/HYBRID
- ✅ Manual verb set — movement, info screens, social, work, city/sect management, `do <quest>`, parties, library, shop pricing (`PlayerCommands`)
- ✅ Hybrid scripts — goto/wait/forage/mine/craft/meditate/hunt/sell/quest/repeat/if/mode with interrupts (`PlayerScript`)
- ✅ Selection lists — filter/sort/limit lists of cultivators, monsters, shop stock, backpack and places (terminal `list`, GUI Browse window); pick by list number or `#id`; `seek` quests; `buy`/`sell` by item
- ✅ Map drawn north-up
- ✅ Place pictures with mood/season/level/hotspot tints and overlays (user-made art in git-ignored `images/`)
- ✅ GUI windows: quest panel, duties & policies, manage shops/city/sect, rankings & charts, market; status bar; map layers (fog, crowd, quest pins, path); log filters; keyboard shortcuts
- ✅ Player-control GUI — buttons for modes/alerts/Do Quest/duties/help, command box, and a script editor window
- ✅ Hand-bidding at yearly auctions, per-character decision timeout
- ✅ Race-free GUI — `World` lock released at every wait, buffered map painting, GUI only queues commands (stress-tested at 0 ms with every window open)
- ✅ Save slots — named saves, `.bak` of the previous copy, CRC check with backup fallback, rotating autosaves, crash slot + `crash.log`, start-up load menu, Saves window, save indicator
- ✅ Settings window/command — autosave, text size, colour-blind map, pictures, tooltips, hints, confirmations, notifications, recap
- ✅ Notifications (log, history, toasts, bell, sound), one-time hints, tutorial + first-run welcome, periodic recap popup
- ✅ Combat preview (`preview`, shown inside duel/flee prompts) and undo-safe confirmations (`!cmd` = confirmed)
- ✅ Family tree + relationship graph windows; `find`/`follow`/`control` and the Browse "Search anyone" tab
- ✅ FlatLaf + xianxia "ink & jade" theme (run-time loaded; Nimbus fallback)
- ✅ Main window redesign — header, action dock in tabs, centred map with hover tooltip and legend popup
- ✅ Painted place pictures for every noun/biome, unique per place, re-rollable (`ArtGen`)
- ✅ Balance step 1 — first generation starts at 18, first-year Originis truce, fight death roll actually applied (population at year 30: 73% of start in the scale model)
- ✅ Luck trait — cultivation, loot, fortune events, social favour
- ✅ Painted art reads number words, level and mood words from the place name
- ✅ Dictionary/name overhaul and name engines for people, beasts, places, cities, sects, dungeons, shops, libraries, techniques, titles, items (`Naming.java`, `Naming Review.md`)
- ✅ Character starts (protagonist, standard roll, custom, ten stories with objective chains), difficulty tiers (Story / Hard / Brutal with permadeath), hidden potential, NPC prehistory (`Starts.java`, `Character Starts.md`)
- ✅ Settlements scale with population (`Settlements.java`)
- ✅ Cave pictures painted from inside; pine-bough frames removed
- ✅ World chronicle, death causes, life samples, notes (`Chronicle.java`)
- ✅ Terminal tools: map, route, time, watch, journal, history, notes, dash, equip/compare/techniques, help topics
- ✅ GUI windows: Backpack, Chronicle + hall of legends, Biography charts, Notes, World overview, Dashboard tab, log hover cards, auto-pause rules, high-contrast theme
- ✅ Reincarnation — a spot quest at the end of your rope (Major Lvl 3+): the same character, a second youth at 18, level 1, potential +5, past lives kept
- ✅ Beast Taming (Tamer trait) — up to Major Lvl + 1 companions that follow, fight with you and level
- ✅ Goods economy — city goods markets, NPC buying and selling, festival trade quests, spoilage; the five life-art traits own their arts
- ✅ City-born cultivators arrive at 18 at the level their potential earned
- ✅ Food, Wine (Drunken), Poison and Music — goods, effects, five new traits, NPC professions
- ✅ Music — seventy songs, a drum kit and 24 instruments written note by note (main theme, road, market, night, danger, war, sect, and forty-two more, seven for every kind of place; eight of them with drum-kit grooves), fades between songs (danger and war at once), rests, a different performance each time, stereo synth or SoundFont (`soundfonts/`); `music list|play|next`
- 🟡 Composed songs (experimental, off by default; new songs are written by hand) — `music gen` composes a new song in the style of a written one (new key, chords, melody; checked by the same lint), `music keep` keeps it in `songs/`, `music share` lets unkept ones mix in
- 🟡 Song library — seventy written songs plus whatever is kept; every kept song comes from listening, the generator's quality is the player's ear (the arrangements repeat the written songs' textures)
- ✅ Painted Originis, town/city skylines, sect halls, rivers and seasons (`ArtCity`)
- ✅ Name engines: recipes and vintages, pet names, clans, festivals, tournaments, wars, tides, eras, regions and rivers (`Naming`, `Regions`)
- ✅ Challenge limit for strangers, start-zone spread, readable non-modal Me-tab windows (`GuiSheets`)
- ✅ Weather, portraits, sect seals, item icons, a painted world map, event illustrations (`ArtMore`)
- ✅ Pill brands (`Item.brand`) and region-flavoured person names
- ✅ Diagonal moves, go-to-quest, chase, quit, mature player start, sect member cap, war life cycle, treachery, squad caps
- ✅ Playtest pass: plan (`Playtest Plan.md`), long-run tool (`tools/PlaytestProbe.java`), results recorded (2026-10-05, 2026-10-06)
- ✅ Fight reports (terminal + notices + chronicle): odds, fled / forbidden technique / won / killed / spared / injured, loot, array attempts, tag-team; `fights [n]` (`FightReport`)
- ✅ Non-lethal losers with bad blood pay the winner 10% of their wealth (negative relationship, bounty or war; sparring never)
- ✅ Sect villainy (`SectPolitics`): inner disciples bully outer ones, Evil elders murder weaker disciples for gear, steal from the treasury, poison and frame rivals, plot coups against the master; Evil acts most, Neutral less, Good never; humans are warned and asked first
- ✅ Spoilage: food, wine and poison rot in markets, shelves, pantries and bags; a hoard is capped (`Life.spoil`, `HOLD_CAP`)
- ✅ Logo: emblem in the window / taskbar, `assets/logo/*.png|.ico`, `tools/MakeLogo.java`, `tools/make-exe.bat` (jpackage .exe)
- ✅ GUI overhaul: one Quest Log window, Duties & Policies window, themed key-cap Keys sheet, command buttons on the Move tab (walk, work, quests, people), Help on the Move tab, a "?" in every window opening its Help page (`GuiDock`, `GuiQuests`, `GuiDuties`, `GuiCards`, `GuiHub`)
- ✅ Interactive illustrated Help (F1, `?`): ~50 pages with screenshots, search, tree, links, tour (`GuiHelp`, `HelpContent`, `HelpPages*`, `assets/help`, `tools/HelpShots.java`)
- ✅ Terminal: status block before each prompt, categorised `help`, did-you-mean, unambiguous prefixes, tables, safe number parsing (`TermHelp`, `TermHud`, `TermInput`, `TermFormat`, `TermView`)
- ✅ Lore: the Palace of the Oldest Order at Originis (virtual master ???), the Convocation of the First Gate, seven acts, nine places, the Nine Lamps, ??? scenes and whispers, the crossing at level 101 (`Lore*`, `GuiLore`, `Lore Design.md`)
- 🟡 Performance: hotspots measured (`Performance Report.md`); the two biggest per-turn scans fixed; more is possible (see report)
- ✅ City and sect detail views (terminal and GUI)
- ✅ Manual mode no longer swaps your gear behind your back
- ✅ Coherent biome generation from seeded noise fields (continents, beaches, climate bands, rivers, terrain-aware names and resources)
- ✅ Sect recruitment rules (craft, potential, level, alignment, gender, family name)
- ✅ Run statistics, hall of fame, retire
- ✅ Beasts remember a reincarnated master and the reborn gets a quest to find them
- ✅ Seasonal festivals, sect diplomacy events, bounties on poisoners
- ✅ Terminal map layers, watch triggers, digest; GUI relationship web of anyone, Rivalries tab, Digest, save thumbnails
- ✅ Cities are founded at Major Lvl 2
- 🟡 Balance — measured and planned (`Balance Plan.md`); no rebalancing done yet
- ✅ Export: map PNG, window screenshot, chart PNG, CSV of any list / rankings / history, event log text
- ✅ MapViewer GUI — optional Swing window (prompt at startup "Enable map viewer? Y/n"); 31×31 color-coded tile grid; click to select tile with info panel (area stats, top characters, bounties); refreshes each game tick; biome-based palette + special colors for Cities/Sects/Dungeons; Monsters shown in red
- ✅ Endings of the story (rewritten 2026-10-02): the Old Man (the Gardener, Outside) explains, then Stay (Good Steward / Evil Usurper / Neutral Idler; undoable with `lore leave`) or Leave (lamp-bearer, strike at the Gardener with a stacking +5-potential blood bonus, walk out to one of 12 Outside branches); nine lamps cap the lamp-bearer ending; hidden ending kills the Gardener and ends the Garden; the world's end offers the Outside or the dark; NPC seekers make the same choices; funny credits, Hall of Fame with scores, Roll of the Crossed (`LoreEndings`)
- ✅ Wandering Immortals, blessed descendants (+5 strength and potential per soldier in the line), NPC seekers, Giant Footprints (`LoreEndings`, `LoreSites`)
- ✅ Quest Log drops quests whose target is gone (humans too); a closing Celestial Trial restores its name; meeting a walker (2026-10-09)
- ✅ Profession button for every trade (Gatherer and Miner included), Inspiration for Innovators, Meditate 1.5 ticks + four techniques, Hunt Monster, Monsters awaken from every wild beast kill (2026-10-09)
- ✅ Scripts run any command, saved scripts, edit/insert/move (2026-10-09)
- ✅ Results shown: Heaven-Sent treasure, gear bought, tournament and Convocation windows (2026-10-09)
- ✅ Dialogue: flavor lines, scenes with a portrait window, the Talk conversation, decisions in the speaker's words, AUTO toggle (2026-10-09)
- ✅ Live playtest fixes (2026-10-09, `Playtest Findings.md`): crowd loops no longer walk the tile, relationship cap (`tdf.relCap`, 100) and interned item names, terminal cultivation %, Convocation notice + `next:` hint, stale tournament quests dropped, destroyed cities reported to their lord / sect / shop owners, one squad question a turn, Chronicle keeps deaths / cities when full, clearer messages (reasons for failed quests, policy values checked, trait by name)
- ✅ Balance and speed batch (2026-10-09): auction bids are a multiple of the reserve, shops get starter stock, the start spread is 21 tiles, titles go to the ten best of each kind, the Epoch population target is 200,000, the quest triggers are looked at every second turn by NPCs, cached quest-name bits / family / learnPlace, cheaper prune and meditate
- ✅ Fixes and planned moves (2026-10-10): no Join Sect once you belong to a sect (the Palace's too); the tournament sign-up is offered to a person who plays the moment a tournament is open for them; `invite <id>` takes an id; epoch deaths read "Heaven has consumed ..."; the epoch's three limits (most cultivators, slowest typical turn in seconds, the cull as a percent or a number) are settings; NPCs act on their tile first and take one planned step last, all steps together, and the people who play act in between, so Go There reads the exact planned step of a walker and meets him (`leadAim`)
- 🟡 Parallel NPC turns (2026-10-10, EXPERIMENTAL, off by default; `settings parallel on`): the turns of NPCs on different tiles run together on all cores, the parts that reach beyond a tile are deferred (`Map.later`), shared tile lists are `SafeList`s, steps are taken on all cores; the NPC phase is 4-5 times faster and a whole tick about 3 times at 100,000 people; clean runs under `-Dtdf.verify=true`. Not yet proven over centuries; the tiles with a person who plays are still played one NPC after the other
- 🟡 The four default spot quests are measured (82% of NPC turns) and kept; steering them with the quest-life profiles is not done (2026-10-09)
- ✅ Sect mission counters and hunt missions that complete (every beast fought counts; "1/3 done", "carrying 40/100"); the Sect Mission quest walks to beast ground (2026-10-10)
- ✅ What the character does while travelling (Meditate / Hunt / Profession / Random; Quest Log box, `spotaction`) (2026-10-10)
- ✅ `seek <id> talk|court` and Go & talk / Go & court buttons for people in other tiles; `explore` and the Explore button; beasts left shown on Hunt Monster quests (2026-10-10)
- ✅ Spoiled and scrapped goods pay their owner (`settings spoilrefund`, 100%) (2026-10-10)
- ✅ The Convocation is not offered to sect members; prize tournaments stay open to all (2026-10-10)
- ✅ Dead entries can be removed from the NPC tracker (2026-10-10)
- ✅ Nobody under 18 can die (2026-10-10)
- ✅ Boards and titles: the hundred Strongest / Richest / Most Kills with three tiers of titles, recounted yearly; the epoch spares children, the ranked, the thousand strongest and the Palace's road (2026-10-10)
- 🟡 Beasts stay on their tile (a count per tile); only awakened Monsters move. Moving beasts are not planned
- ✅ Chapters for the ten Story starts: scenes with named speakers, one task each, choices kept as flags, people of the story created in the world and protected (only the human kills them), hidden truths rolled at the start, road tales, an epilogue (2026-10-10)
- ✅ Story seasons (2026-10-19): the season engine (`Def.season`, interludes, waiting words, patience rules), opposite-gender love interests, echoes, adopted friends/rivals/kin, seasons 2 and 3 for all ten starts (about 4,300 to 7,100 words (counted in the script file, scaffolding included) and 9 to 13 chapters a season) and revamps of the Smith, the Tamer, the Scion's mystery and the Orphan's build-up; 🟡 a little under the 5,000 words a season the author asked for; ❌ Season 4 of the Tamer; ❌ a screen test of the season windows (headless only)
- ✅ An ending written for each start and each of five ways the Garden ends (2026-10-10)
- ✅ `story replay` and the Story window's list of scenes (2026-10-10)
- 🟡 The chapter scenes were tested headless (text, flags, roads, protection); the pop-up windows with answer buttons were not seen on a screen in this session
- ✅ The Palace's text in a plain voice, the Gardener ambiguous (2026-10-10)
- ✅ The Erased: once a century the Hollow Court puts out a seeker; only the humans who knew them remember (2026-10-10)
- ✅ Seeker arcs: Rival, Doubter, Pair, Defector, Teacher (2026-10-10)
- ✅ The Hollow Court felt: later omens and the Dimming of a city (2026-10-10)
- ✅ Quiet links from four starts to the Palace's places (2026-10-10)
- ✅ GUI overhaul (2026-10-10): item cards in the Backpack, the Edit Character dialog, the Script Manager with saved scripts, and sheet pages for family list, relationship list, past lives, biography, run statistics and hall of fame, story, hall of legends, digest, auction, city, sect, place details and the dashboard; Rankings titles and medals
- ✅ The size of the world is chosen at the start of a new game (default 100,000; 10,000 to 150,000 recommended; saved with the world) (2026-10-10)
- ✅ Saves are compressed; an old world is kept small (husks, caps, interned strings); the epoch also comes when memory is nearly full (2026-10-10)
- ✅ Sects and cities defend themselves: the alarm, people on the spot fight, members are sent a Defend quest, the player is told (2026-10-10)
- ✅ Feed policy for the companion beasts of a person who is AUTO or running a script (no hunger) (2026-10-10)
- ✅ Skin tone of the portrait is chosen in Edit Character; children inherit it (2026-10-10)

- ✅ Potential curve (2026-10-11): `Character.givePotential` (5 to 30 Potential Pills per gate, 156 in all, ten gates ending 99 / 100, the last breakthrough needs 100 and a 1 in 5 roll, progress kept as a fraction); the epoch blesses only the culled realms (Major Lvl 0-3, `epochrealm`); tile limit 3.5x the fair share (`crowd`)

- ✅ Control change set 1 (2026-10-13): a walk (goto, goquest, chase) gives the prompt in the turn it ends, and a chase ends with the target still in your tile; one door for quests a human makes (`Character.addPlayerQuest`: no second copy of the same name and target, a higher priority raises the old one); trading, gifts, feeding (one portion per companion per 4 weeks), eating, coating, pills, arrays, sect decisions and `invite` (4-week wait per person, party cap) cost no turn; `equip` and a `hunt` that fights nobody are free; a `gift` warms a friendship once a week per person. Probes `ArrivalProbe`, `DupQuestProbe`, `TurnCostProbe`. The rest of `GameInfo/Control Audit and Test Plan.md` (sections 6.18 onward) is ❌ not started.
- 🟡 Control change set 2 (2026-10-13), silent results and the print plan: the prompt is one line instead of the bag dump; `Explore Dungeon` reports its five kinds; `do` / `qdo` end with a `Result:` line and say "under way" or why nothing came of it; `Character.tell`. Not done yet: the `Say` class with print categories, the classification of the ~350 `System.out.print*` calls, the World-news switch, and the wording audit of the quests a human meets only through the Quest Log. Probes `QuestSilenceProbe`, `PrintBudgetProbe`.
- ✅ Control change set 3 (2026-10-13): Save is written at once when the game waits for you or is paused; every command that takes no time (settings, Edit Self, the Titled hundred, trading, gifts, equip...) works while paused, a command that spends the week opens a "Paused" window and waits in the queue; `Say.world` + `settings worldnews`; no duplicate `[Notice]` for a quest alert; `Loot Corpse` reports; `unequip` is free. `GameInfo/Print Review.md` classifies all 336 console prints (🟡 the Debug lines are not moved behind `-Dtdf.debug` yet, 28 "Unguarded: review" rows wait for the author). Probes `PausedProbe`, `PausedGuiProbe`.
- ✅ Control change set 4 (2026-10-13): the music cannot stack engines any more (control thread + generations; `music status`), notes are sent with time stamps 250 ms ahead on the synthesiser's own clock, 256 voices and sinc where Java allows (`--add-exports` on Java 9+, set in make-exe.bat), a core is left free for the audio threads; mid-week saves are marked and the load screens say what loading repeats. Probe `MusicLifeProbe`. ❌ not done: a rebuilt `dist/` (run `tools\make-exe.bat` to get the flag), listening tests by the author.
- ✅ Control change set 5 (2026-10-13): one window for all story and Lore scenes (queue, pages by block of narration, Back / Continue, the answers on the last page, what follows the answer in the same window, the X takes the default), the after-death window cannot be closed with the X and offers Save and quit / Quit, Spectate sets the speed to 2 s. Probes `SceneWindowProbe`, `DeathMenuProbe`.
- ✅ Control change set 6 (2026-10-13): squads of a person who plays: members stay with errands waiting, ride along tile by tile and stand in the leader's tile at every prompt, orders Follow / Hold and fights first / strongest first / reserve, kick (plain removal), the squad joins `attack` and `hunt` (`alone` / `squad` / `policy squadfight`: join, ask, alone), the Squad window with member orders. Probes `SquadProbe`, `SquadGuiProbe`. A `Work` order lets a member go about its own errands inside the squad, and a following squad defends you when an NPC or a beast attacks (change set 7a).
- ✅ Control change set 7a (2026-10-13): squad `Work` order and defence; **one list of people** (`GuiPersonList`) for People here, Browser > Cultivators / Monsters and Search anyone: same filters, same button bar, one Follow / Unfollow toggle (also in the character sheet), a Seek menu (befriend, talk, court, spar, fight), double-click opens the sheet, People here links to the Browser. Probes `PersonListProbe`, `SquadProbe`. Item 7 continues: items window, then Places, then the wrapped tables and portraits (🟡 not done).
- ✅ Control change set 7b (2026-10-14): **Seek: kill** (`Hunt Down`: any target but another player or a story's fated foe, a child and a monster included; Kill / Spar / Spare / Not now when you stand with the target; the hunt's chase never gives up); **Known people / Family** modes of the shared person list (the Relations window and the family window tabs); **one Items window** (`GuiItemList`: Mine / For sale here / Sell here with an Only-good-prices toggle; Browser > Items and the Market's Shops tab). Probes `HuntDownProbe`, `ItemListProbe`, `PersonListProbe` 27. Next in `Control Audit and Test Plan.md`: 7c Places.
- ✅ Control change set 7c (2026-10-14): **one list of places** (`GuiPlaceList`) for Me > Locations and Browser > Places: type / distance / level / spirit-vein / noted filters, sort by header, Go there, Route, waypoint quest, Details, Note… / Remove note (`note tile x y text`), `list places noted=1`; a plain `attack` ignores the scruples of alignment (not the friend rule). Probe `PlaceListProbe`. Next: 7d wrapped tables and portrait / double-click fixes.
- ✅ Control change set 7d (2026-10-14): **wrapped tables** (`GuiWrapTable`: long text wraps and the row grows in every list window, column widths follow the contents), **one sheet per person and no click-through** after Close, **a portrait with every Kill-or-spare / flee / Hunt Down question** (`DecisionBroker.askAbout`), double-click in Notes, Rivalries, Manage > shops and sect members. Probe `WrapTableProbe`. Next: 7e (the what-happened views as one window, dock duplicates, rankings, settings).
- ✅ Control change set 7e-1 (2026-10-14): **what happened is one window**: the Chronicle window has World / Digest / Journal / Recap / Hall of legends tabs (the Digest and Journal windows are gone), the recap is a card with links and the Recap tab keeps the last one; Search anyone's CSV is `search*.csv` again. Probe `ChronicleMergeProbe`. Next in 7e: dock duplicates, one Rankings window (the Titled hundred button goes: the boards already show the titles), one Control window (replaces the Duties and Settings buttons).
- ✅ Control change set 7e-2..4 (2026-10-14): one home per window (People here, Quest Log, Alerts on the Move tab; links on Me and Auto), **one Rankings window** (the Hall of legends and Hall of fame are tabs; the Titled hundred is gone), **one Control window** (chores, answers, spot action; Priorities tab) replacing the Duties and Policies buttons. Probe `DockMergeProbe`.
- ✅ Control change set 8 (2026-10-14): **priority rules** (`qprio`, Control > Priorities, Quest Log Always ▾), **the breakthrough choice** (hold, prompt with the odds, `breakthrough`, policy ask / auto / button), **the crafting menu** (`craft list|do`, Craft window) and **Potential Pills make you Evil**, **Randomize** in Edit Character, **Highest Fighting Level** board, **Surprise me** + Talk / Spar / Forage / Mine / Explore spot actions, **New quest** by hand (`qadd`, New quest window, Seek: kill from 7b). Probes `PrioProbe`, `BreakthroughProbe`, `CraftMenuProbe`, `EditRandomProbe`, `RankFightProbe`, `SpotActionProbe`, `QuestAddProbe`.
- ✅ Control change sets 9 and 10 (2026-10-14): **gifts** from best friends and relatives (quest `Give Gift`, `askgift`), **trait sects** (a third of the slots for non-Leaders, craft guilds named after their craft with a cultivation bonus), **the story craft counter** ("made 12 of 25 since this chapter began"). Probes `GiftProbe`, `SectThemeProbe`, `StoryCountProbe`.
- ✅ Musicians' concerts (2026-10-15): twelve concert pieces (`Concerts`, `kind: concert`), the musician's instrument as the lead voice, pace and ornament by the instrument's grade, a repertoire by id and season; a Musician hall pays a quarter more. Also: gifts once a year per giver (and of food, wine, poison), every pill counts as a crafted item, Potential Pills make only their maker Evil. Probes `ConcertProbe`, `GiftProbe`, `CraftMenuProbe`.
- ✅ Listening to musicians and 43 concert pieces (2026-10-21): `concerts` / `listen` / the Concerts window, performances remembered by the tile, heard marks, 25 pieces for the occasions of the world and six masterpieces (`ConcertsMore`). Probes `ConcertProbe`, `ConcertGuiProbe`.
- ✅ Pictures redone (2026-10-21): waterfalls, rapids, gorges, volcanoes, arches, the sea, deserts (`ArtNature`).
- ✅ Test plan T12 (2026-10-15): `T12Probe` (independent priority rules of two people who play, the cost of the four boards, the turn cost of the new verbs, the menu's promised levels over 300 crafts, what is saved), `PausedSweepProbe` (every dock button pressed while the game is paused: none does nothing, no time passes), with the earlier probes (`TurnCostProbe`, `DupQuestProbe`, `PrintBudgetProbe`, `SceneWindowProbe`, ...) these cover the T12 list and contract tests 8-12.
- ✅ Beta tester's list, batch A (2026-10-16): Try for a child (command + Me button, either spouse), Squad and Sect tabs, the two walk settings (a fight / an alert stops a walk: both off), Refine Blood without a fee (the Evil alchemist keeps half; starts at 2,000 Blood), Gatherer / Miner multiplier with skill growth, the tile quality cap and a floor under the ground, the GUI log refreshing at once. Probes `BatchAProbe`, `LogLiveProbe`.
- ✅ Batch B (2026-10-16): Lessons (15 casual one-time talks), Favors (a talk that ends with a quest that pays), Little stories (10 short scenes), each with its own switch. Probes `BatchBProbe`, `VignetteGuiProbe`.
- ✅ Batch C (2026-10-16): the five elements: paths and technique elements, the two cycles, the odds table, shown in the sheets, the preview and the report, chosen in Edit Character. Probe `ElementsProbe`.
- ✅ Batch D (2026-10-16): the fight window with stick figures: live (`settings fightview`) or after (the Fights window, a Watch button for every tournament bout). Probes `FightReplayProbe`, `FightGuiProbe`.
- ✅ Batch E (2026-10-16): Monsters live: never above the 20th strongest cultivator, hurt rather than killed when beaten by an NPC, hunting grounds (beasts three times as often as cultivators), on the boards. Probe `MonsterProbe`, tool `MonsterSim`.
- ✅ Fight window everywhere + tournament betting, Music Manager with a playlist, scroll speed, thirteen more cheats (2026-10-17). Probes `FightReplayProbe`, `PlaylistProbe`, `CheatsProbe`, `ManagerGuiProbe`.
- ✅ A seat to spectate; the rest of the cheats (breakthrough, epoch, treasure, dungeon, tournament, jump, reveal ... 27 in all); five little stories for every realm; six more lessons (2026-10-18). Probes `CheatsProbe` (82), `BatchBProbe` (37).
- ✅ The fix round after the long runs: by-hand quests done, no forced party battles for a person who plays, quests report their results, Refine Blood and poison messages (2026-10-18)
- ✅ The Door at Ascension (2026-10-18): open from Major Lvl 7 for a person who plays (`lore door`), the Early Door with its own endings (Rear Guard, Early Strike, walk out alone), ordinary people leave on reaching Ascension / Half-Immortal / Pseudo Immortal (chances as settings), True Immortals who are no seekers may take a lamp, descendants of leavers +2/+2 (`Garden at Ascension Design.md`)
- ✅ Eight new Story starts (2026-10-20): Chef, Poisoner, Tailor, Innovator, Musician, Miner, Cheater, Gatherer, each with three seasons, an interlude between, five codas and an epilogue; lore scenes Under-Kitchen and the Hands of the Court. Awakened-beast start: ❌ (not playable by design).
- ✅ The Door season (2026-10-20): the last season of every start, three chapters gated at Ascension; eight extra seasons for seven starts (Tamer, Heir, Smith, Crane, Ghost, Chef S4; Disciple S4 and S5).
- ✅ The Garden's Variety Tournament (2026-10-21): year 10, 100 and every 100 years after, everyone fights, rounds survived on every profile, prizes by tier, own music and stage, `settings garden`, `cheat garden`
- ✅ Technique effects in the fight window by name and type; tournament staging; `EventMusic` (2026-10-21)
- ✅ Fame and the Most Famous board (2026-10-21)
- ✅ Destroying a city or a sect is a siege: arrays, the people of the place in a row, the standing guards (2026-10-21, `Sieges`)
- ✅ Pictures for every place of the lore and the stories, with portraits of the speakers (2026-10-21, `StoryArt`)
- ✅ Print statements classified and moved (`Say.world / about / status / debug`, `Character.tell`) (2026-10-21)
