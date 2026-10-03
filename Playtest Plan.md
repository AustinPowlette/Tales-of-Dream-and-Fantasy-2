# Playtest and balance plan (after the 2026-10-04 batch)

Goal: find out whether the world stays healthy over a long life, whether the new systems (reincarnation, the goods economy, 18-year-old city births, sect
caps, wars, squads, tamed beasts, the mature player start) behave, whether old saves still load, and whether the first hour is playable. Everything below
has a tool, a pass condition, and a place the numbers go. Run order: A, then B/C in parallel, then D.

## A. Long run, shrunken world (`tools/PlaytestProbe.java`)
- Set-up: scratch copy of `src/Game`, `startingPopulation = 3000`, `javac` it with the probe, run from an empty `run/` folder:
  `java -Xss512m -cp ../out Game.PlaytestProbe 150` (about 1-2 s per game year at 3,000; the run saves a slot `playtest` at the end).
- Output: `play_years.csv` (one row per year) and `play_summary.txt`. Columns: population, cultivators, beasts, tamed, reincarnations logged, cities, sects,
  largest/average sect, sects over their cap, wars active/started, truces, treachery, largest squad, squad members, goods on the market and in bags,
  total/median wealth and the top-1% share, highest realm, how many are Major 3+/5+, the 18-year-old inflow, exceptions.
- What to read, and what counts as healthy:
  | Question | Metric | Healthy | Act if |
  |---|---|---|---|
  | Does the population stay bounded? | `cultivators` vs year | flat or slow growth after year ~30, no doubling | grows every year (city births + births outrun deaths): cap the city-birth rate by the city's capacity |
  | Is every cultivator shadowed by a beast? | `tamed` / `cultivators` | well under 1 | one tamed beast each doubles the characters the game moves: limit NPC taming (rate, or only Tamers / the strong) |
  | Do sects stay inside their cap? | `sectsOverCap`, `largestSect` | 0 over, largest below ~500 at 3,000 pop | any over: a join path bypasses `whyNot` |
  | Do wars end? | `warsActive`, `truces`, `warsStartedLogged` | active stays small, truces keep pace with declarations | active grows without bound |
  | Treachery | `treachery` | a few per decade | none (too rare) or dozens (too many) |
  | Squads | `largestSquad` (cap 12), `leaderSquadMembers` | at most 12; Leaders hold most big squads | above 12: a join path bypasses the cap |
  | Reincarnation pace | `reincarnationsLogged`, `major5plus` | a trickle (some per century), top realms still rare | most of the world reincarnating, or nobody ever |
  | Goods money | `marketGoods`, `goodsHeld`, `totalWealthSS`, `top1pctShare` | goods stay a small share of wealth; market stock does not grow without bound (spoilage works) | wealth inflation (total x10 per 25 years), market stock only rises |
  | Stability | `exceptions`, `heapMB`, wall time per year | 0 exceptions, heap flat, time per year does not climb | any |
- Repeat once with a different `startingPopulation` (10,000) for 40 years to see whether per-capita numbers scale.

## B. Full-size stability (100,000)
- Not 100 years (a tick is ~20 s at 100k: 100 years would take about 30 hours). Instead: world creation time, first-tick time (spread over the 9 start
  tiles), 2 simulated years (104 ticks, ~35 min) watching heap, tick time and exceptions; a save, a quit, a load, and 10 more ticks.
- The long-run behaviour at 100k is inferred from A (per-capita rates) plus B's tick timings; the risks that only show at 100k are time and memory.

## C. Save compatibility
1. The legacy `save.dat` in the repo (an old world): start the game, load it, run 52 ticks in spectate mode, no exceptions; check the new fields default sensibly
   (`Area.goodsBag`, `Area.tournamentName`, `Organization.warSince`, `Character.followerIds/chaseId`, `Item.brand`, `Settings.*`).
2. Round trip on a long-lived world: the `playtest` slot written by A: load it in a fresh JVM, run 10 ticks, save again, load again, compare a few counts
   (population, sects, wars, wealth) before and after.
3. A save made mid-war and mid-chase and with goods in bags and a reincarnated human: load, `q`, `market`, `companions`, `squad`, `lineage` all work.
4. Old settings: delete `settings.properties` keys added this batch; the game starts with defaults.

## D. First hour (what a new player meets)
- Scripted GUI pass (`PcProbeGui` style) and a hand checklist. Start a Standard character with the mature start (level around 15-35 instead of 1):
  1. Start-up: the map-viewer question (terminal), then only GUI dialogs: mode, how this life begins, modify character. No terminal prompt after the GUI opens.
  2. First 60 turns on MANUAL: count quest alerts per turn, pop-ups, challenges (limit 2 per turn from strangers), fights, deaths. Targets: fewer than
     2 alerts per turn on average, no more than 2 pop-ups per turn, death in the first hour under 5% on Story, under 15% on Hard.
  3. Can a new player reach every basic action with the mouse: move (8 directions), go to a quest place (`Go there`), do a quest, hunt, sell, join a sect, quit.
  4. Does the tutorial appear once, and do the hints explain the Me tab groups, the Painted map and the new Quit button?
  5. Time to first reward: first breakthrough or first sale within about 20 turns.
- Also run the three difficulty tiers for 52 ticks each in the probe and compare level, wealth and deaths with a Standard start at level 1 (Settings: `maturestart off`)
  to judge the mature start's effect on difficulty.

## E. Bug sweep prompts (anything the numbers above do not catch)
- Every new command with bad input (`goquest`, `chase`, `music ...`, `cheat ...`, `quit nosave`, `nw/ne/sw/se` at the world edge: the world wraps).
- The map after: hybrid scripts, `goto`, `chase`, loads, window resize (the map repaints every tick).
- Windows: open every Me-tab window twice, close with X, reopen; Painted map while the game runs at speed 0 ms.
- Tile names: a founded city keeps its land name in `geoName` (shown as "was ...").

## Reporting
Each run appends a short result block to `DEVLOG.md` (date, tool, numbers, verdict) and any tuning to `Balance Plan.md`. Fixes found by the runs are
made one at a time and the run that exposed them is repeated.

## Results, 2026-10-05 (first pass)
- **A, long run (3,000 start, ids fixed, 40 years):** cultivators 3.2k -> 18k; wars: with the 4-year rest after a truce, about 45% of sects at war at any time (69 declarations in 39 years, 19 truces; before the rest 79 declarations); treachery about one a year; no sect above its cap (largest 149 with a high-level master); squads never above 13 (cap 12 + the leader); goods in markets rising per head (0.8 -> 2.7 over 20 years in the 20k run), median wealth x3.5 in 23 years, top-1% share 6% -> 15%; no reincarnations yet (first Major 3 appear around year 25-30; potential 37-51 can never reach Major 3 and so never reincarnate); 0 exceptions.
- **A, 20,000 start (25 years):** +3% a year, 25 s per year at 40k: no ceiling yet. Open decision: a population ceiling (births scaled down near a cap) or none.
- **B, 100,000 start:** stable for 2 years: 100.0k -> 100.9k -> 100.3k, heap 256 -> 640 -> 488 MB, ~3 s per tick when the machine is quiet, 0 exceptions. A 105-year run was started in the background (`pc6/r_long`, about 4 hours).
- **C, save compatibility:** the old repo `save.dat` (100k, week 2342) loads, runs 128 ticks, re-saves and reloads with all new fields defaulted (17/17). It contains 7,891 id mismatches from older code that do not grow. A spectator world made by the buggy 2026-10-03/04 build would have all ids shifted by one: start such worlds fresh.
- **D, first hour (100k, passive waiting player, 40 turns):** mature start (potential 55, level 17): 3 alerts (0.08/turn), no fights; level-1 start (potential 28, level 2 -> 6): 16 alerts (0.40/turn), no fights. Mature start roughly halves early alerts of the "weak character" kind and lets a Standard character begin near its peers. Not yet measured: the dense first hour of an active player (fights/deaths), difficulty tiers.
- **E, bug sweep:** found and fixed: ids shifted in spectator mode, rival quests running the poison handler. Timing-flaky GUI probes are listed in the DEVLOG.

- **Update (war counters, 3,000 start, 46 years):** 96 wars declared and 93 ended by year 41: wars end almost as fast as they start (about 2.3 declarations a year among 25-40 sects; the chronicle ring had been hiding old war lines at 100k, so use `Organization.warsDeclared/warsEnded`). Whether 2.3 a year is too warlike is a taste call: raise the squabble threshold (5) or the rest after a truce (4 years) to calm it.
- **100,000 long run:** stopped at year 17 so it would not eat the machine: cultivators 99.7k -> 104.5k (+0.3% a year), 0 exceptions, 0 id mismatches, heap about 1 GB, about 160 s per simulated year (a 105-year run is about 4.7 hours: `java -Xss512m -Xmx12g -cp out Game.PlaytestProbe 105` from an empty folder with the unshrunk source).

## Results, 2026-10-06 (second pass: the new systems)
- **A, long run (3,000 start, `Game.PlaytestProbe 80`, about 65 simulated years, 990 s):** 0 exceptions, 0 id mismatches, heap 0.4 GB at year 43 and 0.8 GB at 58k cultivators; cultivators 3.2k -> 59k (+4% a year, no ceiling: the epoch is meant to handle that); no sect over its cap (largest 86 at 75 sects); median wealth 22k -> 170k (x7.5 in 77 years; the earlier run was x3.5 in 23 years), top-1% share steady at 15%; goods held 0.9 per head (was 2.7 and rising): spoilage works; no NPC above Major 3 (721 at Major 3 after 65 years).
  - Sect villainy over 65 years among up to 75 sects: bullying 3,687 (about 57 a year), framings 392, murders 89 (2 came to light), thefts 25, plots 90 (33 coups won, 10 failed, the rest exposed), treachery 146, poisonings 0 (nobody carried a vial: Evil elders now buy a street vial 30% of the time they scheme). Coups are about one every two years in the whole world: noticeable, not constant.
  - Wars: 520 declared and 501 ended in 80 years, but with 75 sects 58 of them (77%) were at war at the end: the rest after a truce was four years, now seven (`Organization.endWar`).
  - The Palace admitted 86 members through 80 Convocations: only the few who stood in Originis competed (37 of 100,000 in the first one), so the call now reaches unaffiliated cultivators everywhere (1,531 competed and 250 were admitted in a 100,000 world).
  - Nobody crossed the Seam: no NPC gets near level 101 in 65 years (as designed: the last quest is meant to take thousands of years for the strongest).
- **B, full-size (100,000):** with the start spread over 15 x 15 tiles the first ticks take 1.0 s (they took 10.7 s with the 3 x 3 zone); a 30-tick run with the Convocation (1,531 competitors) averaged 1.1 s per tick, peak 2.4 s, heap 0.3 to 0.8 GB, 0 exceptions.
- **C, save compatibility:** the old repo `save.dat` (100k, week 2342) loads, runs, saves in the new format, reloads and runs again: 17/17 (the story is simply absent in old worlds: `lore enable` adds it). Old `settings.properties` without the new keys start with defaults.
- **E, bug sweep (this pass):** found and fixed by the new GUI probes: selecting a row in the Quest Log or People here put the window into an endless refresh (StackOverflowError: refreshing re-selected the row, which fired the selection listener); lore objectives re-created as quests every time the player crossed the target tile (alert spam, `goto` interrupted); a human who resigned from the Palace was re-admitted at once; a beast kill printed a full report; the diagonal arrow glyphs and the flag glyph are missing from the UI font (the d-pad says NW NE SW SE); the Auto tab card was cut off; the probe harness ran without FlatLaf (a path mangled by MSYS) and so never saw the real look.
- Probes now: PcProbe 112, PcProbe2 6, QolProbe 66, RulesProbe 22, StartsProbe 29, TextProbe 27, LifeProbe 85, NewProbe 25, SpecProbe 3, Chaos 9, SimProbeX 73, LoreProbe 67, QolGui 53, PcProbeGui 130, MoreGui 35, CompatProbe 17, HelpCheck (58 pages, 583 links, 51 images). Timing-dependent: NewProbe N3/N4 (a seasonal notice can interrupt a goto), LifeProbe L9, SimProbeX A1 / C6, PcProbeGui "Resume continues it".
