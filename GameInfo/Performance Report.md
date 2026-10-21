# Performance report (2026-10-06)

Question: what takes the time and the memory in a long run (10 to 50 years), and what can be done? Tools: `Perf` (per-phase tick timer, `-Dtdf.perf=true`), Java Flight Recorder, heap histograms, `PerfProbe` (a spectator run that writes a CSV per year and heap snapshots at chosen years). Worlds: 3,000 start (50 years), 20,000 start (21 years), 100,000 start (6 years). Machine: 20 hardware threads.

## 1. Time: where a tick goes
- **Phase 2B (the NPC turns: quests, movement, fights) is 85 to 97% of every tick** and grows with the population: 3k world, tick 25 ms in year 1, 580 ms in year 49 (pop 25k); 20k world, 340 ms in year 5, 860 ms in year 20 (pop 31k); 100k world, 11.9 s for the first tick (everybody on nine tiles), 3.5 s in year 5. Everything else (yearly block, tile scan, epoch, aging, monsters) together is under 5%, except one yearly spike of 20 to 60 ms.
- **JFR, 3k world year 35 and 20k world year 9** (12,000 samples): `Character.quest` 59 to 67% of all CPU, all of it in two loops that scanned `knownLocations` (every tile a character ever saw: 300 to 900 entries after 30 years) **every turn for every character**: the Heaven Treasure check (27 to 33%) and the Seek Potential check (18 to 21%), both of which almost never find anything. `tryRallyParty` 4 to 8% (it copied the whole tile's character list, up to thousands, on every call), `ArrayList.removeIf` 3.6%, `String.equals` 3% (the `targetQuests.stream().noneMatch(q -> "Name".equals(q.name))` guards), `RelationshipList.contains` 3%, `ArrayList.indexOfRange` 1 to 3% (`knownLocations.contains`).
- GC is not a problem: 3,444 collections and 3.4 s in 553 s (0.6%) in the 3k/50-year run; 5,855 and 2.8 s in the 100k run.

## 2. Memory
- Live heap after GC: about **6.6 KB per living character (beasts included), 7.8 KB per cultivator**: 200 MB at 27k cultivators (3k world, year 50); 500 to 750 MB at 100k (year 0 to 5). Peak (before GC) is about twice that. Run with `-Xmx4g` or more at 100k (the `.exe` sets `-Xmx4g`).
- What holds it (3k world, year 50, 27k cultivators + 4.7k beasts): `Object[]` backing the ArrayLists 71 MB (217k arrays), `Relationship` 1.1M objects 27 MB, `Quest` 442k objects 25 MB (13 per character), `byte[]`/`String` 35 MB (names, quest names, item names: 510k strings), `Item` 283k objects 18 MB, `Character` 33k objects 13 MB, `ArrayList` 9 MB, `Technique` 2 MB. `Map.characters` is 46% nulls (59k entries for 27k alive: dead characters are nulled but never compacted, because id == index): 4 bytes per entry, about 0.25 MB: irrelevant.
- Conclusion: memory is not the problem at any population this game reaches. The per-character cost is dominated by quest lists (13 quests each, most of them the same few routine kinds) and relationship lists (34 each); both could be pooled or capped (see below) but would save at most 20 to 30% of the heap.

## 3. What was done (2026-10-06)
1. `Map.heavenExists` (refreshed once per tick): the Heaven Treasure scan runs only while a treasure lies unclaimed. It used to scan every known tile of every character, every turn.
2. Seek Potential looks one turn in six (always for a human): same behaviour, a sixth of the scans.
3. `tryRallyParty` walks the (short) friend list and a bounded 48-tile sample instead of copying a tile that may hold thousands.
4. Instrumentation kept in the code, switched off unless asked for: `Perf` (-Dtdf.perf=true), `-Dtdf.seed=N` (reproducible world), `-Dtdf.threads=N`, `-Dtdf.spread=N` (spread the first generation over an N x N block of tiles; now also `settings spread`, default 15 in worlds of 20,000+).
5. A new world of 20,000+ people now starts spread over 15 x 15 tiles (`Settings.startSpread`): first ticks 10.7 s -> 1.0 s at 100,000 (section 5).

## Results (3,000 start, same seed, 40 years; `Game.PerfProbe`)
| year | before: wall / tick / pop | after: wall / tick / pop |
|---|---|---|
| 10 | 24 s / 97 ms / 4.2k | 7 s / 22 ms / 4.5k |
| 20 | 89 s / 170 ms / 7.1k | 31 s / 68 ms / 8.0k |
| 30 | 207 s / 284 ms / 12.2k | 76 s / 127 ms / 14.2k |
| 40 | 405 s / 501 ms / 18.6k | 158 s / 227 ms / 21.6k |

Per character per tick: 26.9 microseconds before, 10.5 after: **about 2.5 times faster** after the first three changes, with the same behaviour (heap unchanged: 448 MB at year 40 with 2.9k more cultivators). The curve is still slightly super-linear in the population (the remaining quest guards and list scans), so the candidates in section 4 are worth doing before the world gets much older.

## 4. What is left (candidates, in order of value for effort)
1. ✅ DONE 2026-10-06 (`Character.learnPlace`, `KNOWN_CAP` 150 + slack 30): **Cap `knownLocations`** (it only ever grows) at about 150 entries, dropping the least interesting (far, plain wild tiles) first, or keep a second small list of "interesting" places (cities, sects, dungeons, treasures) that the quest scans use. The remaining scans (Hunt Beast pick, Buy Gear, Go Sell, Join Sect) are all gated by dice, but each still walks the whole list.
2. ✅ DONE 2026-10-06 (`Character.qMaskT/qMaskS`, `Q(name)`, `addT/addS`): Replace the `targetQuests.stream().noneMatch(q -> "X".equals(q.name))` guards (about 25 per quest() call) with one pass that fills a small bit set of the names present.
3. Quest object pooling / a smaller per-character quest budget (13 now).
4. `Relationship` lists: cap or prune neutral zero-value entries (34 per character on average).
5. (done, see section 5) Spread the first generation: the biggest single lever at 100,000.

## 5. Spreading the NPCs
Where the first generation lives decides the first years: all 100,000 start on 9 tiles (11,000 per tile), and several parts of a turn look at everybody who shares your tile (squads, challenges, tournaments, crowd checks). Measured at 100,000 (first 8 ticks):

| start | tick time |
|---|---|
| old 3 x 3 zone around Originis | **10.7 s** per tick (about 9 minutes per game year) |
| spread over 15 x 15 tiles | **0.98 s** per tick |
| spread over the whole 31 x 31 world | **0.52 s** per tick |

In a 3,000 world the spread costs nothing in health (same sects, wars, cities, population curve and Major 3 arrivals after 38 years in all three variants; population within 10% of each other; a few percent more early deaths because the outer tiles are more dangerous). So **a new world of 20,000+ people now starts spread over 15 x 15 tiles by default** (`Settings.startSpread`, `settings spread <odd n>`; 0 restores the old 3 x 3 zone; a smaller world never spreads: crowding is not its problem). Originis is no longer a city of 11,000 strangers at the start; it has about 450 people, like every other tile in the block. Everything else (tile levels, beasts, the capital's art) is unchanged.


## 2026-10-06: the first two items, measured
3,000-person world, 60 spectated years (`tools/probes/EconSim`, columns avgKnown / maxKnown / wallSec). Before: an NPC knew 253 places at year 10, 364 at year 40 (largest 874); the 40-year run took 148 s. After: 146 to 150 on average (largest 180) for the whole run; the 40-year run took 128 s (about 14% less wall time; the economy changes of the same day are in both numbers). A human or followed character keeps every place it has seen (the fog of war never shrinks). The quest-name guards read one 64-bit mask built from the two queues at the start of `quest()` instead of 31 stream scans, and every `add` inside `quest()` updates the mask (`addT`, `addS`).
Still open: quest object pooling (3) and pruning neutral relationships (4).

Note 2026-10-07: cheaper crafting made shops and pools grow without bound (55,000 shop items at year 60, 732 s for the 60-year run). Bounded by `Shop.capacity()`, `POOL_CAP` and a pack limit for NPC crafters: 354 s.

Note 2026-10-07: with the smiths and tailors working again (richer material usable) the 3,000-person world holds ~26,000 items in shops and pools at year 25 (bounded by `Shop.capacity()` and `POOL_CAP`); the "is there anything to buy?" question is answered by `Area.gearIndex()` instead of reading the shelves. The map's pictures are built on a background thread (`PlaceImages.thumb`), so Tile pics no longer cost the tick anything. Tick time is now also what triggers the epoch (`GameDriver.medianTickMs`).

## 2026-10-09 update
Quiet machine, 100,000 world, ms per tick of the NPC phase, years 0-2 / 2-4 / 4-6: 1,619 / 1,296 / 1,195 before the playtest fixes, 746 / 750 / 759 after (tile loops, relationship cap, cached quest-name bits and families, cheaper prune / meditate, and NPCs looking at the dynamic quest triggers every second turn). See `Playtest Findings.md` and the DEVLOG entries of 2026-10-09.

## 2026-10-10 update: planned steps and parallel NPC turns
Quiet machine (14 cores / 20 threads), 100,000 world, one tick, years 0-9 (`-Dtdf.perf=true`, ms per phase):

| | NPC phase | steps | aging `time()` | whole tick |
|---|---|---|---|---|
| one thread (planned steps, `settings parallel off`) | 560-720 | 60-85 (one thread) | 31-37 | about 700-850 |
| parallel (`settings parallel on`) | 105-170 | 35-40 (all cores) | 17-45 | about 230-330 |

The NPC phase is 4-5 times faster and the whole tick about 3 times (more cores help the phase, not the aging, the yearly block or the 9-second autosave of year 10). What is left in a parallel tick: the NPC phase itself (150), taking the steps (35: leaving then arriving, one core per tile), `time()` (aging, serial: 17-45), the people on a tile with a person who plays (serial, by design), the tile grouping (about 15), and the yearly block.
Companions follow their masters on all cores (22 ms to 2) and the reincarnation list is a queue instead of a scan of every character (13 ms to 0). Scanning every `Character` costs 10-20 ms per pass at 100,000 (cache misses): avoid whole-population loops in a tick.

The planned-step model changed the pace of the world a little: an NPC on a quest used to take a random step AND the quest's step in a turn, now it takes one step (and acts before it). Measured on 3,000-person worlds at year 50, five seeds each: population 20.7K before (18.6-22.3K) against 18.4K now (15.9-20.4K); parallel (three seeds) 18.0K, the same as one thread. Wealth and cities are unchanged.

## Final pass (2026-10-21): measured at 100,000 and on the author's save

100,000 people, a new world, `MemProbe 6 2 2,6` with `-Xmx6g -Dtdf.perf=true` and a Flight Recorder profile (11,600 samples):

| Year | tick (median) | population | beasts | live heap after GC | committed |
|---|---|---|---|---|---|
| 2 | 624 ms | 98,955 | 21,951 | 451 MB | 1,112 MB |
| 4 | 864 ms | 98,834 | 39,345 | 577 MB | 1,268 MB |
| 6 | 881 ms | 99,102 | 50,437 | 648 MB | 1,392 MB |

Live heap at year 6 (150,000 characters, 4.3 KB each): `Quest` 1.88 M objects 196 MB (12.5 per character, 104 bytes each), `Object[]` 106 MB, `Character` 97 MB (648 bytes), `Item` 1.16 M 83 MB (72 bytes), `Relationship` 1.96 M 79 MB (40 bytes), `ArrayList` 40 MB, `double[]` 16 MB (`qTarget` / `qEma`), `Technique` 12 MB (two default ones per character), `PriorityQueue` 10 MB.
The author's year-329 world (185,423 cultivators, 173,000 beasts, 474,000 characters): tick 2.1 s, live heap 2.1 GB: `Quest` 4.5 M 469 MB, `Character` 307 MB, `Relationship` 7.0 M 280 MB, `Object[]` 260 MB, `Item` 3.6 M 257 MB, `ArrayList` 125 MB.

Where the NPC phase (77% of every tick) spends its time, by top of stack: `Character.quest` itself 26.5% (a flat spread over its ~1,100 lines, no line above 4%), `java.util.Random.next` 5.3% (`QuestLife.roll` 2.2%, `tryRallyParty` 1.3%), `HashMap.get` 4.7% (`Character.Q(String)` 3.5%: the quest-name bit of every new `Quest`), the gear chain `buyGear` 4.2% + `Area.gearIndex` 4.1% + `Area.gearSignature` 2.4% (7.4-7.8% inclusive: a copy of every shelf and a `sellPrice` for every item, per shop, per call), the Rob Caravan scan of the whole tile (`quest:1986`, 3.9%), `ArrayList.indexOfRange` 3.4%, `Area.removeChar` 1.9%, `applyMoves` 1.7%.

**Applied (author's decision, 2026-10-21):** two changes in `Character.java`, measured in a scratch copy first (100,000 people, the same seed, run side by side, `MemProbe 6 2`):
1. `buyGear`: the price of a shelf item (`Shop.sellPrice`, a `Math.pow`) is worked out only for an item that passes the type and level tests (it was worked out for every item of every shelf, twice).
2. Rob Caravan: an Evil person looks for merchants in a random stretch of 48 people of the tile (as the Bully loop does) instead of the whole tile.
Tick (median) at years 2 / 4 / 6: 742 / 968 / 1,028 ms before, 585 / 846 / 915 ms after (-21%, -13%, -11%); six game years of wall time 285 s -> 244 s; live heap unchanged (649 vs 637 MB). A table of prices by level instead of `Math.pow` made no difference (580 / 840 / 885 ms) and was not kept.
**Not done, with the reason:** shrinking `Quest` (104 bytes, 12.5 per character), `Relationship` (40 bytes) or `Item` (72 bytes) would save about 10% of the heap at the price of changing serialised fields (an old save would stop loading unless every change is a removal): live heap is 650 MB at 100,000 people against a 3 GB cap, so it was left. A faster `Random` (the same generator without the atomic) would give about 4%, but the cultivation pass runs on pool threads and shares `GameDriver.rand`: left as it is. Other candidates, in order of value: `Area.gearSignature` per call (2.4%), a cheap "I cannot afford any gear" test before the scan of known cities (`quest()` buy-gear and `hasBuyableGear`, about 4%), the quest-name bit lookup of every new `Quest` (3.5%).
