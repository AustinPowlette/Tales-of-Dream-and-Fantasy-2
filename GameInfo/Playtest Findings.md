# Playtest findings, 2026-10-09 (live play, speed and memory)

How this was done: the game was played through the terminal at the real size (100,000 people) in scratch copies, never in the repo and never with the user's saves. Commands were typed one at a time and each answer read before the next. A small wrapper (not part of the game) logged the tick phases and the heap every 10 seconds, and the JDK's own tools (`jcmd GC.class_histogram`, Java Flight Recorder) were attached to the running game. Characters played or inspected: Merchant, Blacksmith, Innovator, Gatherer (AUTO), Leader (reached Core Formation, joined the Palace, founded a city), Chef, Musician (taken over with `control`). The longest single run was 30 game years; across all runs (about 17 worlds) there was no exception and no `crash.log`.

**Status (same day, after approval):** the three crowd loops (Bully, Rob Caravan, `dissolveParty` / `partyBeastHunt`), the relationship cap (100) and interned item names were applied, and bugs 1-28 below were fixed (see DEVLOG 2026-10-09 "live playtest"), except: 8 (a note, not a bug), 20 (`leave`: the controlled character really was in a party; only `wait 0` was a bug), 27 (`spar` with no number still picks someone, by design; `attack` now asks for a number), and the balance notes of section 4. The other measured patches (cached quest-name bit, `learnPlace` shortcut, bounded Bully window) are not applied; they remain in [tools/perf-proposal.diff](tools/perf-proposal.diff), which was written against the code before the fixes.

**Second batch (same day, balance notes):** auction bids fixed (4 below: the cause was NPCs bidding their whole purse), shops get starter stock, the start spread is 21, titles go to the ten best of each kind, the Epoch population target is 200,000, tournament prizes are left as designed, and every speed idea of the "other patches" list was applied (cached quest-name bits, `learnPlace`, bounded Bully window, family cache, cheaper prune and meditate, bounty stream, and the trigger throttle). Only running NPC turns in parallel was not done. Measured on a quiet machine: years 0-2 / 2-4 / 4-6 take 746 / 750 / 759 ms per tick against 1,619 / 1,296 / 1,195 originally.

Original note: no `.java` file in the repo was changed during the playtest itself.

## 1. Speed

Where the time goes (100,000 people, one tick = one game week):

| Phase | ms per tick |
|---|---|
| NPC actions (`phase2B`, serial) | 1,100 to 1,600 (95% of the tick) |
| everything else together (time, cultivate, taming, areas, cleanup) | 25 to 40 |

Inside the NPC phase (Java Flight Recorder, 4,000 to 5,000 samples per profile):

| Where | Share of the tick |
|---|---|
| `Character.quest()` itself | 28 to 31% |
| "Bully" loop over the whole tile (`Character.java:1498-1509`) | up to 25% in years 0-2 (450 people per tile), about 5% later |
| "Rob Caravan" loop, which first copies the tile (`:1606`) | about 7% early |
| `dissolveParty()` copies the tile on every quest success (`:5049`) | 3 to 7% |
| `Character.Q("...")`: 35 hash lookups per NPC per turn | 5% |
| `QuestLife.family(name)` hash lookups | 3.5% |
| `tryRallyParty` friend walk (grows with the relationship list) | 4 to 5% |
| `pruneKnownLocations` | 3 to 5% |
| `learnPlace` (`contains` on a 150-180 element list per move) | 2% |

**Correction (after the fixes were applied):** the table below was measured while three game worlds shared the machine. Re-measured with nothing else running, same seed, the applied fixes (three loops + relationship cap + interned names; not the cached quest-name bit or the `learnPlace` shortcut) take the NPC phase from 1,619 / 1,296 / 1,195 ms per tick (years 0-2 / 2-4 / 4-6) to 1,522 / 1,075 / 982 ms: 6%, 17% and 18% less. The large early gains in the table (2.4x) appear when the machine is busy, because the crowd loops are memory-bound.

Measured with the patches in `tools/perf-proposal.diff` (same seed, same machine load, epoch purge disabled for the comparison; ms per tick of the NPC phase):

| Game years | original | 3 loop fixes | + Q cache, learnPlace, bounded Bully | + relationship cap 48, interned item names |
|---|---|---|---|---|
| 0-2 | 2,792 | 1,524 | 1,187 | 983 |
| 2-4 | 1,497 | 1,320 | 1,171 | 946 |
| 4-6 | 1,195 | | 1,115 | 951 |
| 6-8 | 1,193 | | 1,044 | 1,011 |

A second pair of runs at the shipped `-Xmx4g`, original against the full patch set: years 0-2 1,576 vs 959; years 2-4 1,408 vs 942; years 4-6 1,326 vs 1,052; years 8-10 1,176 vs 1,059. So: about 2.4x faster in the crowded first years, about 10% in the steady state. The per-NPC cost is now about 10 microseconds, so a bigger gain would need NPC turns to run in parallel by tile (not tried).

Why this matters for the world, not just the clock: the Epoch purge (`GameDriver.java:721-724`) fires when a typical tick takes longer than 2,000 ms and kills 20% of every realm. In runs that were slowed by other processes it fired in year 0 to 1 (19,000 to 24,000 dead). In the quiet long run it fired at year 20 ("a turn now takes 2178 ms", 24,737 marked). The speed of the machine therefore decides when the population is purged.

Regression check of the patched source (3,000-person copies, project probes): PcProbe 112, LoreProbe 121, LifeProbe 85, SimProbeX 73, QuestLifeProbe 57, WorldProbe 41, PolishProbe 39, EconProbe 33, StartsProbe 29, NewProbe 25: identical to the counts before. (EconProbe flaked once inside a chain and passed 33/33 twice alone, on the original source too.)

## 2. Memory

Live heap after garbage collection (minimum per two years, original code, one world, MB): year 2-4 648, 4-6 739, 6-8 971, 8-10 1,091, 10-12 1,218, 12-14 1,313, 14-16 1,390, 16-18 1,464, 18-20 1,551, 20-22 1,677. After the purge at year 20: 1,093, then back to 1,510 by year 24-26. That is about 50 to 100 MB a year with no plateau.

What the 1.68 GB at year 21 is made of (class histogram): `Relationship` 6.2 M objects, 250 MB; `Object[]` 221 MB; `Quest` 2.5 M, 159 MB; `Item` 1.9 M, 135 MB; `String` + `byte[]` 193 MB; `Character` 168,000, 83 MB; `ArrayList` 46 MB.

The one that never stops growing is the relationship list. Per person: 18 at year 4, 54 at year 20 (some have 1,355). 99% have a value of +1 or -1 ("met once"), none are 0, only 0.6% point at dead people. Nothing removes them (`RelationshipList.add`), and every meeting does 2 to 4 linear `contains()` scans of the list, so the tick also slows as the lists grow (1.04 s at year 6 to 1.6 s at year 21 in a run with no purge).

Other findings:
* Item names are never shared: 1.9 M items, but only 13,300 distinct names (about 100 MB of strings).
* Tamed beasts settle at about one per adult (55,000 to 74,000), they are not a leak.
* Quests are bounded (cap 50, expiry 10 years); retained corpses: none; `Chronicle` is capped at 6,000.
* The list `Map.characters` keeps null holes (67,000 of 223,000 at year 21); harmless (4 bytes each).
* The process held 3.0 GB of RAM at year 12 and 4.2 GB committed at year 22 with 1.8 GB live: the JVM grows its heap to absorb about 125 MB/s of garbage. The shipped exe uses `-Xmx4g` (`tools/make-exe.bat`), so at about 55 MB/year the live data alone would reach the cap around year 45 to 60 unless a purge intervenes; run from IntelliJ without a cap the heap just keeps expanding (the "5 GB after 300 years").
* Saves are big: 222 MB at year 2 (79,000 people).

Measured fix (variant with relationship cap 48 and interned item names, same seed): post-GC heap year 4-6 795 vs 918 MB, year 8-10 1,012 vs 1,164, year 12-14 1,143 vs 1,371 (-17%, widening every period). The cap value (48 here) is a tuning choice; 100 to 150 would cost less of the "friend" memory and still stop the growth.

## 3. Bugs and rough edges (with how to see each one)

Cultivation progress is wrong in the terminal.
1. `i` shows "Cultivation: 238 / 24,105 (1% of the way to the next level)" and the status line shows "(cult 1%)" forever, while `meditate` says "cultivation 86% -> 88% of Lv.18". `PlayerCommands.java:1615` and `TermHud.java:42` divide `cultivation` (the gain per tick) by the level cost; `Character.meditate` and `MapViewer.java:601` correctly use `cultivationBase`.

The Convocation is easy to miss.
2. Start any game and stay in Originis. `q` lists only "Spectate Tournament"; the register step exists in `lore` and as the spot quest "Register Tournament" (a duty that is off), and `Lore.ensureQuest` only makes a quest when the target is somewhere else. One run got the "Palace: Register" alert in the last week of the eight, after walking away. The `next:` line says "nothing urgent".
3. After the Convocation ends: `q` still lists "Spectate Tournament" (`qdo` answers "couldn't be done right now") and the `next:` line keeps saying "Register Tournament -> qdo #...".
4. These stale low-priority alerts also cut `wait 30` to 5 weeks (7 weeks in another run). `settings notify alert off` does stop it (`wait 30` then ran the full 30 weeks), but nothing in `wait`'s help says so.

A city you rule can be razed without a word.
4b. A Core Formation Leader did `found city` (year 7, week 6). Her city got a Chronicle line, a fair (wk 377) and bounties (wk 16 and 22), then about 20 weeks later `here` showed the bare tile again (the city name reverted to "Leaning Hills", no City line) with no notice, no alert and no Chronicle entry. Cause: `Character.java:5334-5339` -> `destroyCity()` (an Evil character or a Beast that wins a fight in an undefended city has a 1-in-`RAZE_CITY_ODDS` chance), which prints only when the destroyer is a human. The ruler, the shop owners and the sect members get nothing. Repro: found a city and `wait` a few hundred weeks with `history <city name>` open.

Prompts and noise.
5. One `hunt` in a crowded tile produced about 30 "X wants to join your squad" prompts in the first tick (`Character.tryRallyParty` asks for every stranger it rolls, up to 48 candidates at 1 in 4). `policy squad never` fixes it for the rest of the game; a limit of one prompt per turn would be gentler.
6. The Chronicle: about 6,000 "X tamed Y" lines in 10 weeks fill its 6,000-entry cap, so deaths, cities and sects are evicted within weeks, and the `digest` ("What happened lately") shows 30 tamed lines. Repro: 10 weeks of play, then `dash` ("5988 world" entries) and `digest`.
7. About 12 "has awakened as a Monster!" lines a week (up to 70 in the first weeks at Originis) within 20 tiles of a human.
8. The same prompt eats the next typed command; fine, but `mute X`, `release 1`, `control N` ask for confirmation (use `!` to skip).

Wrong or missing text.
9. `help found` says a city needs Major Lvl 3 and the hint shown at a breakthrough says "Major 2 can found a sect, Major 3 a city"; the code (and `profession`'s own message) says a city needs Major Lvl 2 (`TermHelp.java:163`, `Character.java:4564`). Both texts are wrong.
10. `mute Hunt Monster` asks "Mute 'Hunt Beast'?".
11. `do Restock Trip` -> "can't be done right now (requirements not met)" with no reason; the forge message "short of metal (your biggest stack holds 100)" never says how much is needed.
12. `sell materials` / `sell metal` -> "No backpack item #-1". Herbs and metal are only sold through the `Trade Run` quest, which nothing tells a Gatherer.
13. `Only a Drunken can do that` (trait name used as a noun) for `make wine`, `cook`, `brew`.
14. `goods` suggests `make food|wine|poison` to a trait that cannot make it.
15. `sell food` prints "Sold goods for 38 ss." and "Sold for 38 ss.".
16. `people limit 3` -> "(nothing matches)" with no hint that the command is `limit 3`; `people trait=Musician` works.
17. `settings` lists `notify <category> on|off`, but `notify ...` alone is "Unknown command" (`settings notify alert off` works).
18. `policy flee maybe` silently sets flee to off (any non-"on" value); `policy kill 9` is rejected correctly.
19. Custom character: typing the trait as a word ("Innovative") is silently ignored; only the code (`I`) works.
20. `wait 0` waits a week; `leave` says "You left your party." with no party.
21. An unknown command echoes its whole text back (a 3,000-character line is printed in full).
22. A friendly spar you lose prints "Fate: You survived (a rebirth or ghost form saved you)" although it was never lethal.
23. Dialogue: a close friend (+2) greets with "Haven't seen you around. Ella."; an introduction line ("Oh, hello! I'm Briar.") comes after her friendship request in the same meeting.
24. After founding a city a Leader's sheet shows "Profession Lvl: 50" (the founding flag, `Character.java:4551`).
25. A reply to a yes/no prompt that has several words ("no thanks 5 here") answers with the first word and then runs the rest as a command ("Unknown command 'thanks'").
26. After `control <id>` the character you left keeps a player-style output: its `[Follow] ... is in ...` lines, its fight reports and "You meditate: ... of Lv.17" lines (wrong "You") scroll through the new character's console; a different level than the status line shows.
27. `spar` with no number picks a stranger at random and starts the fight prompt ("No target given - picked Levi Bullock #3510 at random").
28. Innovator: gaining Inspiration is silent until the 100 mark ("Your head is full of ideas..."), and the technique only appears when you type `profession` (works: "you set down Qilin Footwork (potential 21, Movement)"). A hunt shows no "+3 Inspiration" line.

## 4. Balance observations (not bugs, worth a look)

* Tournament prizes are Divine-Grade items of level 40 to 50 from year 2, when the strongest people are about level 20 to 27; at year 20 every winner has the title "Demon/Demoness Butcher".
* Auctions: vintage dumplings or wine sold for 3 to 4.8 billion spirit stones at year 0 to 20, and two different lots in one city closed at the identical price. The median cultivator owns 0.
* The three shops of Originis were empty at week 10 and the goods market held 4 food items, so a new player has nothing to buy.
* The population grows past 100,000 (124,000 at year 20) until the Epoch purge.
* The 100,000 start puts about 450 people on every tile (225 areas), which is why the first years cost 2 to 3 times more per tick.

## 5. Long runs at a fixed heap cap (spectator worlds of 100,000, default epoch rule)

| Run | Result |
|---|---|
| original, `-Xmx4g` (the exe's setting) | Epoch purge at about year 19 (a turn took 2,021 ms, 23,809 marked). Live heap after GC: 1.46 GB (year 16-20), 1.37 GB right after the purge, 1.47 GB by year 24-26. No OutOfMemoryError in 26 years. Ticks 1.2 s at year 8-10, 1.9 s at year 12-18. |
| patched, `-Xmx4g` | No purge until about year 26 (ticks stayed under 2 s, so the world grew to 138,000 people), then 20% fewer. Ticks 1.06 s at year 8-10, 1.86 s at year 22-24. Live heap 1.3 to 1.5 GB for years 16-30. No OutOfMemoryError in 30 years. |
| original and patched, `-Xmx2g` | No OutOfMemoryError in 19 years (live heap 1.0 and 1.2 GB; the original had been purged in year 1 by a loaded machine, so the two worlds differ in size). |

GC pauses were under 1% of wall time in every run, so the growth in tick time is real work (more people, longer lists), not collection.

Two things to know when reading these numbers:
* The measured "post-GC" heap is an upper bound. With a large heap G1 leaves old garbage uncollected for a while: the same kind of world measured 1.0 GB live under a 2 GB cap and 1.5 GB under a 4 GB cap. The class histogram (which forces a full collection) agrees with the higher number only for the world that had never been purged (1.6 GB at year 21, 124,000 people).
* Speeding the tick up delays the Epoch purge, so the world gets bigger (138,000 people) and uses more memory. If the purge is meant to keep the world near 100,000, its trigger should be a population target, not a tick time (`GameDriver.java:721-724`).

Not shown: whether the exe's 4 GB cap is ever reached. At about 50 to 100 MB a year with a purge every 20 years that is a matter of 50 to 80 game years in the original; I did not run that long.

## 6. Caveats of the proposed patches (`tools/perf-proposal.diff`)

* Bully check: the tile is now looked at through a random window of 48 people (as `tryRallyParty` already does) and the cap test is made once; the behaviour is the same on average, not identical per roll.
* `dissolveParty` / `partyBeastHunt` read the leader's `followerIds` instead of the tile. A save from before `followerIds` existed would not dissolve parties it already holds (they are short-lived).
* Relationship cap (48 in the tested variant; 100 to 150 is probably kinder): when a list passes the cap, the oldest entry with |value| <= 1 that is not refused and never talked to is dropped. Bonds of 2 or 3 stay. The other person keeps their half of the pair until they meet again; `changeRelationship` rebuilds a missing half at 0 and the two sides re-agree on the next change. Friends of value +1 are the ones that age out.
* Interned item names and the cached quest-name bit change no behaviour.

## 7. What was not tested

The GUI (Swing) was not driven; sect war / peace / alliance as a sect master; reincarnation; Poisoner, Alchemist, Scribe, Tailor, Miner and Runner as played characters (Beast Tamer, Musician, Chef, Innovator, Leader, Merchant, Blacksmith and Gatherer were); multiplayer; a run beyond 30 years.
# Second playtest, 2026-10-10 (memory in an old world, live play at 100,000, the GUI past year 30)

How it was done: the same scratch-copy rules as above (nothing in the repo or in the user's saves was touched; the user's year-560 `saves/auto-1.dat` was only read, through hard links). Three kinds of runs: (1) **measurement**: `tools/probes/MemProbe` (a spectator run that logs the live heap after a full collection every few years, with class histograms) and `tools/SaveStats.java` (reads a save without applying it and lists what fills it), on a new 100,000 world and on the user's year-560 world; (2) **live play** through the terminal at 100,000 (a Poisoner, then Alchemist / Scribe / Tailor / Miner / Runner by changing the trait mid-run, a Cheater to set scenes up, a sect master at war and allied, a reincarnation, 400 turns of AUTO, save and load); (3) **the GUI against the old world** (`MoreGui`, `QolGui`, `PcProbeGui`, `DialogueGui` against a 3,000 world and `MoreGui` against the year-560 save).

## 1. Where the memory went (the year-560 world, 182,153 cultivators)

| What | Before | Why it was big |
|---|---|---|
| live heap after loading | 2,493 MB (1,377 MB file) | |
| relationships | 10.2 million, 56 per cultivator, 93% of them +1 / -1 | the cap was 100 and no tie to a dead person was ever dropped |
| queued errands | 3.9 million, 20 per cultivator | Find Person 1.15M, Rob Caravan 0.66M, Battle Dislike 0.64M: copies, one per tie or per merchant ever seen |
| dead characters held by living kin | 58,017 (22,000 items in their bags) | `father`, `mother`, `siblings`, `children` keep a dead person alive for ever, with everything they carried |
| tamed beasts | 84,270 (46% as many as cultivators) | every NPC could tame one (1 in 1,200 a turn) and nothing limited it |
| known places | 155 per cultivator | the cap was 150 + 30 |
| raw materials | 11.6 stacks per cultivator, many at quantity 0 | crafting pills, weapons, robes and arrays left an empty stack behind |
| makers on items | 652,801 separate strings | the same name written out again for every item |

After `Slim` (one year of it): 1,434 MB alive (-42%), and the file written again, compressed: 270 MB (a fifth). A new 100,000 world (year: before -> after, MB alive): 15: 994 -> 794, 30: 1,437 -> 1,040, 45: about 1,900 -> 1,341; at the 200,000-cultivator epoch limit it holds about 1.3 to 1.5 GB instead of 1.9 to 2.0 GB.

**Why the task manager shows 7 to 8.5 GB:** the JVM grows its heap to a quarter of the machine's memory (8 GB on 32 GB) because the game allocates a lot of short-lived garbage every tick; the live data was about 2 GB. With `-Xmx3g` the process sits at about 3.4 GB whatever the world needs. `MemWatch` now brings the epoch when 70% of `-Xmx` is alive, so a smaller `-Xmx` makes the world thin itself instead of running out.

## 2. The defence feature and what it taught (population)

The first version of "places defend themselves" blocked the razing of a place whose people matched the attacker. At 20,000 (seed 777) it cut the growth of the world from +34% in 20 years to about -5%: city-born births were 3,335 against 1,535 in ten years. Cause (not a bug of the defence, a property of the world): `Area.age()` makes a city-born cultivator 10% of ticks for EVERY tile with population > 0, and a razed city keeps 10% of its people, so razing multiplies the sources of people. The razing is no longer blocked. The defence itself is cheap: in 15 years at 20,000 it raised 1,333 alarms, sent 6,941 errands, fought 3,651 bouts and killed 33 attackers and 6 defenders. A 20,000 world is chaotic (deaths of identical settings differed by 40%), so each comparison was run several times.

## 3. Bugs found by playing (all fixed unless noted)

1. **NullPointerException out of the whole NPC turn** (`Character`, "Recruit Member"): after talking to the recruit the recruiter could be out of its sect (`sect` null) and `sect.join(recruit)` threw. Found by a variant run, which died at year 5. Now the sect that invites is kept in a local and the errand ends if the recruiter left or died.
2. `settings notify alert off` did not let `wait 52` run through new quest alerts (a new alert always woke a waiting character).
3. Crafting left **stacks at quantity 0** in the Materials / Pills lists (alchemy, weapon, robe, array crafting and `consume`), and they were saved for ever.
4. `make poison` with herbs too fine for the profession said "of a level you can work with" and nothing else; `poison` with no target printed the usage of `attack`; `coat` worked with no weapon ("Your weapon is coated"); a poisoning was announced twice; `sell` with only herbs and metals said "Nothing to sell" without saying they are crafting material and are not sold raw.
5. `cheat level` left the cultivation bar and weekly gain of the old level; a reincarnated character kept the reputation title of the old life (for example "Sovereign of the Nine Heavens" on a level-2 body) until the next New Year (now cleared at rebirth).
6. **Not changed, for you to decide:** the Scribe's array costs 2,000 herbs of one stack while a forage gives 10 to 100, so a Scribe cannot make one for a long time; an alliance with an NPC sect is made at once, without the other sect agreeing; the terminal prints accented names as `?` when its output is redirected on Windows (the console itself is fine); `WorldProbe` E1 and `NewProbe` N3 / N4 fail about one run in three on the old code as well (world-dependent), `PcProbe` S14 and `LifeProbe` L16 about one in ten.

## 4. What was exercised and found working

- **Sects**: founding at Major 3 (89 members after one year), alliance, war on an ally (the alliance breaks), a sect raid (+20 sect points), peace, a coup by an elder that drove the player out of the sect they founded (year 6), the Palace's seat under attack, the new Defend notices (the master hears wherever they are; the notice shows where).
- **Reincarnation**: `reincarnate` at the end of the rope (potential 25 -> level 19 reached): reborn at 18, level 1, +5 potential, the sect and the wealth kept, the run statistics list both lives. A voluntary one only; a death-driven one is covered by `PcProbe`.
- **Poisoner, Alchemist, Scribe, Tailor, Miner, Runner as played characters**: each trait's craft works (a Tailor made a Divine-Grade robe at level 15); Scribe, see the cost above.
- **400 turns of AUTO** at 100,000 (a Runner, all duties on): no exception, the character reached Foundation 15, tamed a companion, joined the Palace, was driven out of its own sect, got a title and 32 friends.
- **The GUI at year 560** (`MoreGui` against the user's save, 182,153 cultivators): 35 of 36 checks, 0 exceptions (the one failure waits 20 s for a thumbnail of a 1.4 GB save). `QolGui` 53, `PcProbeGui` 136, `MoreGui` 36, `DialogueGui` 13 against 3,000 worlds.

## 5. The open findings of section 3, decided (2026-10-11)

- **Scribe's array**: `Character.ARRAY_MIN` 2,000 -> 600 units of one stack (a forage gives 10 to 100, so 6 to 60 forages; an amateur needs 1,200).
- **Alliance without consent**: `Organization.allianceRefusal` - the master of an NPC sect refuses when the masters are Good and Evil, when they dislike each other, or when the two sects (or an ally of the proposer) are at war; the `alliance` command and the "Propose Alliance" errand obey it, a human master is asked.
- **Accented names printed as `?`**: not a game bug. A Windows console code page cannot show some letters when stdout is redirected; start a redirected run with `-Dstdout.encoding=UTF-8` (the GUI and the .exe console are fine).
- **Reputation title after rebirth**: already fixed (`Reincarnation` clears `repTitle`).

## 6. Third pass: live play and the 2 GB heap (2026-10-11)

Played at 100,000 through the terminal (a Scribe, then a Cheater to reach Major 10 and found a sect, then a Blacksmith's Child in AUTO to year 33; every trait through the editor; 26 odd or empty commands; save and load at year 34 = 203 MB compressed). **One bug found and fixed:** a human master driven out of the sect by a coup (a Lv.1 master after a rebirth, AUTO) was never told, because `SectPolitics.announce` told only the humans still in the sect. Nothing else crashed (0 exceptions in about 8,000 turns).

Things seen that are NOT bugs but you may want to decide:
- A potential under 52 can never reincarnate (the rope ends below Major 3; the sheet says so). A Blacksmith's Child at potential 33 reached Lv.20 at age 51 and gains +83 a week there.
- "Make ten items" (chapter 1 of the Blacksmith) takes about 110 mining turns (500 metal an item, a mine gives 10 to 100).
- Friend offers at Originis come almost only from Musicians (they go to the biggest city to perform).
- A human's Quest Log keeps up to 9 "Find Person" errands (the caps in `Slim.trim` are for NPCs only).
- The Chronicle gets about ten "bounty posted on the poisoner" lines a week.

**Memory with `-Xmx2g`** (`MemProbe`, a new 100,000 world, spectator, 4 threads; the machine was busy with other runs, which also made the tick-time epoch come earlier than it would): live heap after a full collection 853 MB at year 20, 998 at 30, 1,175 at 40, 1,169 at 50, 1,007 at 60, 1,047 at 70 (world 136,000 to 162,000 cultivators, the epoch thinning it every ten to twenty years); the committed heap stays at the 2 GB cap. The memory epoch (70% of 2 GB = 1,434 MB) never had to act. This is 70 years, not 1,000: the live heap is flat from year 40 to 70, and the epoch keeps the number of cultivators near 140,000 to 200,000, so it should stay there.

## 7. The tile limit at 3.5 times the fair share, and the new potential curve (2026-10-11, `TileProbe`, a spectator run of the 100,000 world to year 70)
Limits: 364 on a wild tile, 1,456 in a city (31 x 31 map). One line every five years (`tiles.csv`):
- **Wild tiles** (about 900 of the 961 exist as places): the median tile holds 79-106 people; tiles over the limit were 17 at year 5 (before the 10-year grace), then 6-16 for the rest of the run (0.7-1.8%); the fullest tile 575-1,347 (857 at year 70). Hotspots: none until year 10, then 13, 16, 19, 22 (the peak, year 25) and 7-12 from year 40 on (1-1.3% of the tiles). So with 3.5 the limit bites in a few crowded places and never on the whole map.
- **Cities** (49-68 city tiles): the fullest city holds 325-924 of 1,456, never over the limit; Originis 370-715.
- **Population** swung between 92,000 and 125,000: the epoch by tick time (typical turn 1.5-1.9 s on this machine) came about every 10-15 years.
- **Potential**: the mean rose from 36 at the start to 41 at year 70; people with 52+ 24,000-35,000; 85+ about 3,500-3,900; 99+ 51-67; exactly 100: 17-25. At year 70 the realms held 18,955 / 37,765 / 32,342 / 8,446 / 29 people in Major Lvl 0 / 1 / 2 / 3 / 4 and nobody above, so an epoch that culls Major Lvl 0-3 leaves out only a few dozen.
