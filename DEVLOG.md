# Development Log — Tales of Dream and Fantasy 2

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
