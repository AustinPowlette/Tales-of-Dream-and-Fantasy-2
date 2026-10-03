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
1. **Cap `knownLocations`** (it only ever grows) at about 150 entries, dropping the least interesting (far, plain wild tiles) first, or keep a second small list of "interesting" places (cities, sects, dungeons, treasures) that the quest scans use. The remaining scans (Hunt Beast pick, Buy Gear, Go Sell, Join Sect) are all gated by dice, but each still walks the whole list.
2. Replace the `targetQuests.stream().noneMatch(q -> "X".equals(q.name))` guards (about 25 per quest() call) with one pass that fills a small bit set of the names present.
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
