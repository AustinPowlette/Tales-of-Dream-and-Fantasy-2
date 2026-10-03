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
- ✅ Breakthrough Tribulation — fires at majorLvl 3/5/7/9; chance = `clamp(10-90, potential - (majorLvl-3)*5)`; pass: 10% chance +1 potential + cultivation boost; fail: realm regression + inhibitor
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
- ✅ City levels from treasury; shop slots (3/level) and shop level cap; epoch purge at 200k
- ✅ Crowd hotspots — tiles over 2% of the starting population (map-area scaled, cities 4x) get beast ambushes until thinned; Originis exempt; "Disperse" quest
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
- ✅ Generated ambient music — composed pieces with titles, motifs and sections, seven moods, stereo synth or SoundFont (`soundfonts/`)
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
