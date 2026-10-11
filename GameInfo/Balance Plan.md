# Balance Plan — Tales of Dream and Fantasy 2

## Owner decisions so far (2026-09-30)
* **Leveling stays brutal and slow.** Even 2,600 years to Major 10 for a perfect cultivator is acceptable. Cultivators should congeal at the bottom; only a few reach the top through high potential, long life, luck or being a rich scion. **Do not speed up the XP curve.**
* **The early massacre is too brutal.** Target roughly a one-third population drop at most (about 67% survive the first decades).
* **NPCs and monsters must be able to reach the top realms.**
* **The breakthrough slowdown (`inhibitor`) is intentional:** it models the injury of a failed breakthrough, and a failure caused by low potential should soft-cap that cultivator's growth from then on.
* **Money is both scarce and plentiful.** Gear one Major above you must cost a relatively significant sum, yet money must flow: earned by killing beasts, spent and exchanged often. Prices and income follow the same polynomial curve as leveling.
* Open questions for these are in the reply of 2026-09-30 and will be folded into sections 4-7.

### Owner answers to the question list (2026-09-30)
* **Start age 18** (try it first). **Do not protect children by rule** (they may need to kill later); instead a **first-year truce in Originis**. Everyone still starts in Originis. Spread out only if still needed (e.g. the centre 9 tiles).
* **Soft stop for high vs low level fights** is believed to exist already (it is the flee/caution logic); keep.
* **Beasts**: hunting should be a bit risky, about 5-10% of deaths. (Finding: beasts only kill when they out-level the fighter by 6+ or ambush; hunters mostly fight below their level.)
* **Luck matters**: more gain in general, own events (potential, items), better social standing but less than Friendly. (Done 2026-09-30.)
* **Inhibitor stays permanent and uncapped**; frozen cultivators are fine, **reincarnation** should later reset gates and raise potential.
* Pills / bloodline / inheritance of gear and sect benefits are the rich-scion channels. Prices default as proposed; **anchor prices to time-to-level**; fix the loot vs price unit mismatch.
* Cities generate passive income from their `population` (cultivators pay no tax); city population stays a constant abstract number. The main purpose of beasts is the initial generation of spirit stones; later non-fighters must be able to sell goods and earn from cities.
* Settlements should scale with cultivator count: the open question is how many cities and sects per 10,000 cultivators (the 1-in-33,000 Leader trait rarity limits founding).
* Monsters awaken at the level of the beast they were; their growth should be similar to cultivators'.
* Start spreading: make it more likely to spread out, or use the centre 9 tiles. Presets: see `Character Starts.md`.
* Pacing: run the simulation and report rough dates per Major for approval (see the dates table below).

### Measured after step 1 (3,000-person scale model)
Population (two runs): year 10 = 82-92%, trough 71% (year 20) and 77% (year 30), then growth (year 60 = 128-140%, year 100 = 388%: births ~4% a year with no brake before the first cohort reaches old age at ~year 82-100 — watch this boom). Deaths: cultivator kills ~2,950 vs beast ambushes ~480 in 60 years (14%, all from crowd hotspots). First person at Major 3: year 37; median age at M1 10, M2 15, M3 49; nobody at M4 by year 109 (lvl 40 is the potential-65 gate).

Status: **measurement done, nothing changed yet.** This document records what a 250-game-year spectator run and a code read found, ranks the problems,
and proposes how to fix them in order, with a number to hit for each step. Decisions that need the owner are in section 7.

## 1. How it was measured

| Tool | What it gives | Where |
|---|---|---|
| `BalProbe` | one CSV row per game year (population, deaths, realm counts, settlements, wealth) and a summary of the age each realm was first reached and age at death | `tools/BalProbe.java` (usage in its header) |
| death telemetry (scratch patch, **not** in the repo) | who killed whom: killer/victim alignment, age, level gap, tile | to become permanent "Phase 0" below |
| arithmetic on the XP formulas | years needed to reach each realm for a given potential | section 3.B |

Runs used a **3,000-person world** (`startingPopulation = 3000`, crowd limits scale with it), spectating, 250 game years, about 21 minutes of wall time.
The real default is 100,000 people; it is ~33x denser in the same start tile, so every density effect below is expected to be **worse**, not better, at 100k
(section 3.A, pacing). Figures are one run with one random seed: read them as orders of magnitude, then re-measure after every change.

Baseline numbers (3,000 start, year 250): population 6,750; realm shares M0 18% / M1 30% / M2 46% / M3 5.6% / M4 0.5% / M5 0%; highest level 52;
cities 1-3; sects 1-22; shops ~38; dungeons 37-43; median wealth ~0.5M ss, total wealth 19.7 billion ss, top 1% owns 44% (peak 58% at year 195).

## 2. Findings, ranked

### A. Survival — the world massacres itself in the first decades (CRITICAL)
* Population 3,000 -> 1,989 (year 4) -> 727 (year 30) before recovering. All ~2,400 human deaths in the first 30 years were **killed by another cultivator**;
  **zero** by beasts or monsters.
* Victims are children: of deaths in the first 10 years, age 6-11 = 67%, 12-15 = 23%, 16+ = 10%. **67% of the killers are under 12.** 88% of victims are Major 0.
  92% of the kills happen outside Originis.
* Who fights: Evil attacks everyone (Good 529, Neutral 282 victims per 10 years of one run); Good hunts Evil (513). Killer is stronger by >5 levels in 15% of cases, stronger 1-5 in 39%,
  weaker or equal in 46%: fights are coin-flips between equals, and the loser dies (no non-lethal outcome, no retreat for NPCs).
* Code: `Character.battleCheck` lets anyone aged 8+ initiate; only ages <8 are protected, and only by redirecting to the mother. The whole starting population is
  created as children (`avgAge` 11.9 at year 4) on one tile.
* Old-age deaths: 96 in 250 years (0.9% of all deaths). Lifespan (100 + 100/200/400/1900... per realm) never matters; violence decides everything.
* Pacing side effect: at 100,000 people the first tick took ~90 s and the second ~80 s (one measurement, with other test jobs running, so an upper bound), because everyone starts on one tile and fights. At that rate a game year is over an hour of wall time.

### B. Progression — a steep polynomial curve behind a potential lottery (CRITICAL)
* XP to level `L` = `L^3 * (70.3 + 1.83 L) / 25`; XP per week = `potential^1.5 * sqrt(L) * 10 / 52 / 2^inhibitor`. Time per level therefore grows about `L^2.5 * L`.
  Expected years to *reach* a realm with no failures (idle cultivator, cultivating every week):

| potential | M1 | M2 | M3 | M4 | M5 | M6 | M8 | M10 |
|---|---|---|---|---|---|---|---|---|
| 20 | 6 | 61 | 264 | 774 | 1,824 | 3,724 | 11,744 | 29,139 |
| 40 | 2 | 22 | 93 | 274 | 645 | 1,317 | 4,152 | 10,302 |
| 60 | 1.2 | 12 | 51 | 149 | 351 | 717 | 2,260 | 5,608 |
| 100 | 0.5 | 5.5 | 24 | 69 | 163 | 333 | 1,050 | 2,606 |

* Measured (250-year world): median age at first reaching M3 = 48, M4 = 104, M5 = 241 (one person). **Nobody reached M6-M10.** Realm shares above.
* `potential = 101 - sqrt(U)` with U uniform 1..10000: mean 34, median 30. Breakthrough gates are `potential >= 20/37/52/65/76/85/92/97/100` for M1..M9.
  Share of people who can pass each gate without luck: M1 66%, M2 41%, M3 24%, M4 13%, M5 6%, M6 2.6%, M7 0.8%, M8 0.16%, M9 0.01%.
* Failing a gate gives a 1/15 chance and `inhibitor++`, which **permanently halves** XP speed (`2^inhibitor`). After 4 failures a person is 16x slower; ~14 failures expected
  at a gate they cannot pass -> effectively frozen forever. 34% of the population (potential < 20) is capped at M0-M1 by this alone.
* The player is created with potential 100, so the top realm is theoretically open to them only: M3 in ~24 years (1,250 turns), M5 in ~160 (8,500 turns), M10 in ~2,600 (135,000 turns).
  XP bonuses exist but are small (sect: founder major + sect level/10 + territory extra `cultivate()` calls per tick; spirit vein +1; dungeons +3..+20 once).
* So the upper 5 realms are not reachable in any normal session. Either the game is meant to be a multi-thousand-year simulation (then the curve is fine but pacing tools are missing), or the curve is too steep.

### C. Economy — money only grows (HIGH)
* Wealth per capita 20k ss (year 5) -> 390k (30) -> 1.2M (100) -> 2.9M (245): roughly +6%/year with no matching sink. Prices scale with `lvl^2.5`, income (beast loot)
  is `lvl^2.5 * 10 * (1 + potential/100)` per kill and each person logs ~450-520 kills.
* Concentration: top 1% share 5% (year 50) -> 17% (150) -> 55% (200). Median stays flat (0.4-0.6M) while the total grows 35x: the average player-facing price is
  meaningless to most NPCs and a few holders (city/sect owners) accumulate everything.
* Shops stay at ~38 and dungeons at ~40 for 250 years: the economic map never grows.

### D. World growth — settlements barely form (HIGH)
* Cities: 1 until year 120, at most 3 by year 250 (and sometimes back to 1), although hundreds of Major 3 cultivators exist (`cityLeadership`: 1% chance per tick for Major 3+ *who reach that code path*).
  Sects: 0-4 for 120 years, 20 at year 240. The world stays a wilderness around one city; quests and shops depend on cities.
* Population recovers slowly: net growth ~1.2%/year from year 75 to 240 (933 -> 6,091).

### E. Combat and risk — unproven, suspected (MEDIUM, needs data)
* NPC fights have one outcome class (kill). There is no surrender, flee, or injury for NPCs (the player gets a preview and a flee prompt). Fights between equals are 50/50 to the death.
* Tribulations (M3/5/7/9) pass with chance `max(10, min(90, potential - 5*(realm-3)))` % and a failure sets back 2 levels + `inhibitor++`: a potential-52 to 65 cultivator fails the M3 tribulation 35-48% of the time, each failure is a permanent -50% speed.

### F. Not yet measured (put in Phase 0)
Item/price curves, technique upgrade rates, pill effects, sect mission income, auction/market clearing, beast spawn vs kill rates per level band (beasts are tile counters, not characters),
dungeon reward versus risk, tournament payouts, per-tick wall cost by system at 10k/30k/100k.

## 3. Root-cause summary (file:line anchors are in the knob table)

1. Children are valid fight targets and attackers from age 8; everyone starts as a child on one tile -> cohort annihilation.
2. The XP curve grows ~`L^3.5` but lifespan/realm rewards and potential gates grow far more slowly in usefulness; the inhibitor turns every failure into a permanent penalty.
3. No money sinks, loot scales with level, prices scale with level: inflation; ownership of cities/sects concentrates wealth.
4. City/sect founding is a 1%/tick roll behind several conditions and cities/sects are also destroyed; net creation is ~0.

## 4. Targets (proposed; confirm in section 7)

| Metric | Now | Target |
|---|---|---|
| Population at year 30 / start | 24% | 70-85% |
| Deaths by cultivators before age 16 | ~77% of deaths | < 10% of deaths |
| Old-age share of deaths at year 100 | < 1% | 15-30% |
| Median age at M1 / M2 / M3 (potential 30) | 10 / 12 / 48 | 12 / 35 / 120 (players with potential 100: ~1 / 3 / 8 years) |
| Fraction of living people at M4+ at year 250 | 0.5% | 3-6% |
| Someone at M6+ by year 250 | never | yes, rare (1-3 people); M10 by year ~800 |
| Cities / sects at year 100 (3,000 world) | 1 / 4 | 6-10 / 15-25 |
| Wealth per capita growth | +6%/yr | +1-2%/yr, flat in real terms; top 1% share < 25% |
| Wall time per game year at 100k | > 1 h (early) | < 60 s steady state after year 3 |

## 5. Plan, in order (each phase ends with a BalProbe run that must hit its number)

### Phase 0 — measure properly (no gameplay change)
* Add permanent, cheap telemetry (`Stats` counters: deaths by cause/age/alignment/level gap, kills by target type, ss earned/spent by source, XP by source, breakthrough attempts/failures, per-tick ms by phase) and a `balance` console command / CSV dump.
* Move the BalProbe harness to run with a chosen `startingPopulation` and seed from the command line (seeded `Random` so runs are comparable), keep `frozenNpcs` for unit checks.
* Baseline at 3k, 10k and 30k; record in `DEVLOG.md` before touching anything. Extend to items, techniques, pills, dungeons (section 2.F).

### Phase 1 — survival and start-up (fixes A and the 100k wall time)
1. Protect children: no NPC-initiated lethal fight against anyone under ~16 (and none by attackers under ~16); children spar, flee, or are escorted by the mother.
2. Spread the start: spawn the initial population across many tiles/cities instead of one; or age the starting cohort (most adults, a minority of children).
3. Give NPC fights a non-lethal tail: loser flees/yields with a chance that rises with the level gap and relationships (kill only when evil or hostile); add a cool-down after a fight.
4. Re-check Evil aggression: Evil attack rate per tick vs. density; cap fights per character per year.
* Acceptance: population at year 30 >= 70% of start (3k world); deaths before 16 < 10% of deaths; first 10 ticks at 100k under 10 s each.

### Phase 2 — progression curve (fixes B) — REVISED: the curve stays; work on the rules around it
0. (Owner decision: leave the XP curve and the potential gates alone. Items 1-5 below are the original proposals; only the parts that keep the brutal shape apply.)
1. ~~Decide the pacing target, then reshape XP:~~ e.g. replace `L^3 * (70.3 + 1.83 L)/25` by a realm-stepped cost, and/or raise XP with realm (`cultivation` multiplier per major realm), so a potential-60 cultivator reaches M3 in ~50 years and M6 in ~500.
2. Replace the permanent `inhibitor` with a temporary one (recovers over N years / with pills / after a breakthrough insight) and cap it (e.g. at 3), so failure costs time, not the whole future.
3. Soften the potential lottery: distribution with a fatter upper tail (mean ~45), or let potential grow slowly with age and deeds (already +1 events exist), and lower the gate for M1-M3 so 60%+ can reach M3.
4. Make lifespan matter again after Phase 1 (old age should bound a mortal's run at ~100-200 years unless they break through).
5. Pace tools for the player: a difficulty/pacing preset (potential floor, XP multiplier) rather than hard-coding potential 100.
* Acceptance: table in section 4; BalProbe realm counts at years 100/250/500.

### Phase 3 — economy (fixes C)
1. Sinks: upkeep for shops/cities/sects, repair/decay of items, pill/array consumption, taxes that are spent (city treasury funds defence and growth), auction fees, luxury goods.
2. Loot: flatten per-kill `ss` growth (or scale by a world-wide damping factor based on money per capita).
3. Anti-concentration: progressive tax; owner death/succession splits wealth; cap treasury or convert excess to population/garrison.
* Acceptance: per-capita growth +1-2%/yr for 200 years, top 1% < 25%, NPC median can afford a level-appropriate weapon.

### Phase 4 — world growth (fixes D)
* Trace the city/sect founding path with telemetry (how many Major-3 cultivators reach `cityLeadership`, how many cities are destroyed and why); raise the rate, add a minimum-distance rule and a cap; let sects form from friend groups; make settlements attract NPC migration.
* Acceptance: cities/sects targets in section 4 at years 100 and 250.

### Phase 5 — player experience and regression net
* Pacing presets (Story / Standard / Long-lived), a "world age" fast-forward with summary, and the balance dashboard inside Rankings & Charts (population, deaths by cause, wealth, realm histogram).
* Add a nightly-style script that runs BalProbe at 3k for 100 years and compares every column to the targets, failing on drift.

## 6. Knob table (where to change things)

| Knob | Where |
|---|---|
| Fight eligibility by age, evil aggression, affiliation rules | `Character.battleCheck` (`age < 8`, affiliation block) |
| Lethality of a lost fight | `Character.battle` / `deathCheck` |
| Start cohort (all children, one tile) | `GameDriver.main` world creation (`startingPopulation`, age init in `Character(Area)`) |
| XP per level / per week | `Character` constructor and `cultivate()`: `cultivationBaseMax`, `cultivation`, `inhibitor` |
| Breakthrough gates and lifespan bonus per realm | `Character.cultivate()` `switch (majorLvl)` (`potential < 20/37/52/65/76/85/92/97/100`, `maxAge += 100/200/400/1900/3700/...`) |
| Tribulation odds | `Character.cultivate()` (`tribChance`) |
| Potential distribution | `Character(Area)` (`potential = 101 - sqrt(...)`) |
| Sect / vein / dungeon XP bonuses | `GameDriver` Pass 1b, `Character` dungeon switch |
| Beast loot and price curves | `Character` (beast constructor `ss`), `Character.itemPrice` |
| City / sect founding | `Character.cityLeadership`, `sectLeadership`, `establishCity`, `establishSect` |
| Crowd limits | `Area.crowdCapacity`, `GameDriver.CROWD_*` |

## 7. Decisions needed from the owner

1. **Pacing.** What should a typical session feel like? Proposals: (a) *short* — a new player reaches Major 3 within ~1 real hour of normal play and Major 6 in a long campaign; (b) *epic* — keep the current multi-thousand-year curve and add fast-forward, summaries and generational play.
2. **NPCs vs player.** Should NPCs be able to reach the top realms (living legends) or should those stay player-only / event-only? (Today potential gates make M8-M9 almost impossible for NPCs.)
3. **Violence.** Is the brutal start intended ("a dangerous world") or a bug? Proposal: dangerous but survivable (children protected, non-lethal losses, kills when hostile).
4. **Scale.** Is 100,000 the supported default, or should the game auto-scale to the machine (e.g. 10-30k) with density-aware rules?
5. **Permadeath of progression.** Is `inhibitor` meant to be permanent?
6. **Economy flavour.** Should money be scarce (hard sinks, barter) or plentiful (status/luxury goods)?

## 8. Risks
* Fixing survival first changes every other curve (more people alive -> more competition, more wealth); re-baseline after each phase.
* One-seed measurements are noisy: run 3 seeds per change once the seed option exists (Phase 0).
* The 100k world behaves differently from the 3k scale model (density); confirm each phase at 10k and 30k, and time the first 10 ticks at 100k.

## 9. Settlements and prehistory (2026-09-30)
* Settlement scale test (3k scale model): sects were founded steadily; cities appeared once M3 cultivators existed (about year 40, 7-8 cities, later reduced by wars to 3). Population grew quickly (30k by year 70); the owner decided the epoch and hotspot density should cull it, so no cap was added. Watch performance at 100k.
* Settlement engine: `Settlements.yearly()` - 1 city per 1,000 cultivators and 1 sect per 300, at most 6 / 15 a year (see `Game Info.md`).
* NPC prehistory: the first generation is 18 and has cultivated for the 10 years since age 8 (median level 13 at 18 in the scale model); the player starts at level 1.
* Standard roll: NPC curve with one re-roll below potential 20 (dud rate 45/400 after the re-roll, versus 34% raw).
* Hostility by difficulty: lethal rolls in 3,000 fights went from 144 (Story) to 874 (Brutal).
