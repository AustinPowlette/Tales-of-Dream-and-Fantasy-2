# Long runs and the full test sweep: results (2026-10-18)

What was run, in a 3,000-person copy of the sources (`many3.sh`-style, `javac --release 8`), with the author's saves, settings and `dist/` untouched:

- **T0, the regression sweep**: 61 headless probes and 20 GUI probes (details at the end).
- **T3, the quest matrix**: every quest type of the New quest window (`PlayerControl.QUEST_TYPES`, 72 types) plus the quests a person starts another way (`seek`, the five dungeon kinds): 82 rows, each through the MANUAL path (`qadd` -> `goquest #uid` -> `qdo #uid`). Probe: `tools/probes/QuestMatrixProbe.java`.
- **T4, the story journeys**: each of the ten Story starts played for up to 60 game years in MANUAL mode by a careful bot that only uses commands a person has (no cheats, no state edits). Probe: `tools/probes/StoryJourney.java`.

A word on what "75 quests" is: the type list has 72 names (`Sect Mission` is the game's own); the table below has 82 rows because the dungeon kinds and the `seek` quests are separate rows.

---

## T3: the quest matrix (82 rows)

Verdicts: **PASS** it starts, a person can finish it and the result is shown (a line, or the `Result:` line); **FAIL** it does something wrong (silent, nothing done, only a waypoint, refused); **STUCK** a person cannot finish it; **N/A** the game makes it itself. Cost = how many `qdo` and how many weeks (each `qdo` is one week); walking is extra.

Totals of the run **before the fix round**: PASS 65, FAIL 15, STUCK 1, N/A 1 (its causes and repro steps are kept below). Totals **after the fix round** (the table): **PASS 81, FAIL 0, STUCK 0, N/A 1.** (Attend Auction, Recruit Defector and Get Pregnant are chance-dependent: they now report what happened either way; Hunt Cultivator needs prey it can beat and says so when there is none.)

| # | Quest (path) | Verdict | Starts | Cost | What the person sees / why it failed |
|---|---|---|---|---|---|
| 1 | Accept Sect Mission | PASS | yes | 1 qdo, 1 wk | Accepted sect mission: Forage 50 herbs of level 2 or better, +10 sect points. |
| 2 | Attend Auction | PASS | yes | 9 qdo, 9 wk (+4 wk walking) | You wait in Probe City for the auction (about 2 weeks; you are asked for your bids when it is held). |
| 3 | Battle Arch-Enemy | PASS | yes | 1 qdo, 1 wk | [Fight] Tag-team assault: you, N12 Probe, N3 Probe, N9 Probe, N11 Probe and N13 Probe take on N6 Probe, one after the other (the target tires each round). |
| 4 | Battle Dislike | PASS | yes | 1 qdo, 1 wk | The world: population 3001 ? 3032,  cities 8 ? 9,  sects 8 ? 10 |
| 5 | Battle Rival | PASS | yes | 1 qdo, 1 wk | [Fight] Tag-team assault: you, N12 Probe, N3 Probe, N9 Probe, N11 Probe and N13 Probe take on N5 Probe, one after the other (the target tires each round). |
| 6 | Bounty Hunt | PASS | yes | 1 qdo, 1 wk | [Fight] Tag-team assault: you, N12 Probe, N3 Probe, N9 Probe, N11 Probe and N13 Probe take on N4 Probe, one after the other (the target tires each round). |
| 7 | Buy City Array | PASS | yes | 1 qdo, 1 wk | Installed Divine-Grade Nine-Palace Sigil lvl: 60 at Probe City |
| 8 | Buy Gear | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Result: -121,898 ss, +1 item(s) in the backpack |
| 9 | Buy Pills | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Result: -88 ss, +1 pill(s) |
| 10 | Buy Sect Array | PASS | yes | 1 qdo, 1 wk | Installed Divine-Grade Azure-Dragon Circle lvl: 60 at Probe Ancestral Hall! |
| 11 | Buy from Sect | PASS | yes | 1 qdo, 1 wk | Bought Divine-Grade Earth-Rending Sword lvl: 90 from sect treasury for 90 sect points. |
| 12 | Courier Run | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | You deliver your word to Probe City and are paid 3000 ss. |
| 13 | Courtship | PASS | yes | 3 qdo, 3 wk | Bo Hu 'Lord of Ten Thousand Vaults' and N3 Probe 'Pillar of Heaven' are now married! Congratulations! |
| 14 | Defend | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | ==== FIGHT REPORT ====  Year 8, wk 28, Probe Peak |
| 15 | Deliver Goods | PASS | yes | 1 qdo, 1 wk | Sect mission complete: 1 food delivered to Probe Ancestral Hall! +10 SP |
| 16 | Deliver Materials | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Sect mission complete! +10 SP |
| 17 | Demand Tribute | PASS | yes | 1 qdo, 1 wk | Bo Hu 'Lord of Ten Thousand Vaults' demands tribute from Heavenly Dao Alliance |
| 18 | Disperse | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | You reach Probe Quiet, quieter than the crowded places you left: the crowd thins out. |
| 19 | Donate Array | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Bo Hu 'Lord of Ten Thousand Vaults' donated 1 array(s) to Probe Ancestral Hall! (+10 sect points) |
| 20 | Donate to Sect | PASS | yes | 1 qdo, 1 wk | Donated to sect treasury � earned 156 sect points. |
| 21 | Entice Merchant | PASS | yes | 1 qdo, 1 wk | Enticed Yuna Tang to open a shop in Probe City |
| 22 | Explore Dungeon | PASS | yes | 1 qdo, 1 wk (+8 wk walking) | You sit among the restless dead of Probe Ruin and cultivate five times: cultivation 8% -> 8% of Lv.55. |
| 23 | Festival Trade | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Sold goods for 2 ss (festival prices). |
| 24 | Find Parent's Body | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | You found your parent's resting place. |
| 25 | Find Person | PASS | yes | 1 qdo, 1 wk | Bo Hu 'Lord of Ten Thousand Vaults' talked to N3 Hu 'Pillar of Heaven' |
| 26 | Forage | PASS | yes | 1 qdo, 1 wk | Discovered 30 herbs. Quality: 3 |
| 27 | Gathering Trip | PASS | yes | 8 qdo, 8 wk (+4 wk walking) | Discovered 30 herbs. Quality: 2 |
| 28 | Get Pregnant | PASS | yes | 7 qdo, 7 wk | You spend the week with N15; no child yet. The quest stays: try again. |
| 29 | Gift Technique | PASS | yes | 1 qdo, 1 wk | Gave technique Seven Sun Peaks Scripture (potential 60) to N3 Hu 'Pillar of Heaven' |
| 30 | Give Gift | PASS | yes | 1 qdo, 1 wk | Bo Hu 'Lord of Ten Thousand Vaults' and N3 Hu 'Pillar of Heaven' are now married! Congratulations! |
| 31 | Go Sell | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Sold 3 items for 30398 ss at Probe City |
| 32 | Heaven Treasure | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | You claimed the Heaven-Sent treasure: Divine-Grade Ethereal Garb! It is in your backpack. A strong one can be put up at a city auction ('sell' in a city). |
| 33 | Hold Tournament | PASS | yes | 1 qdo, 1 wk | === The Iron Challenge of Year 9 is announced at Originis! Realm 4-5, 8 weeks registration. Prize: Divine-Grade Nine-Dragon Fan lvl: 62 === |
| 34 | Hunt Beast | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | [Fight] You defeated the beast Stiff Badger (Lv.4), power 43 vs 1, 100% to win. |
| 35 | Hunt Cultivator | PASS | yes | 1 qdo, 1 wk | Hunt Cultivator: you will not start a fight with N8, who is not Evil ('attack N8' ignores that scruple). |
| 36 | Hunt | PASS | yes | 1 qdo, 1 wk | [Fight] You defeated the beast Opal-Eyed Grey Wolf (Lv.4), power 43 vs 2, 100% to win. |
| 37 | Join Sect | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Result: you joined a sect |
| 38 | Loot Corpse | PASS | yes | 1 qdo, 1 wk | You loot the remains of N19 Probe. |
| 39 | Market Trip | PASS | yes | 2 qdo, 2 wk (+4 wk walking) | Bought 1 x Probe Rice for 2 ss. |
| 40 | Meditate | PASS | yes | 1 qdo, 1 wk | You meditate: cultivation 11% -> 11% of Lv.55. Techniques sharpen: Default Technique 2/3. |
| 41 | Meditation Retreat | PASS | yes | 8 qdo, 8 wk (+4 wk walking) | You begin a meditation retreat at Probe Quiet. |
| 42 | Mine | PASS | yes | 1 qdo, 1 wk | Discovered 60 metal. Quality: 2 |
| 43 | Perform | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | You play 'Dance of the Paper Lanterns' on Divine-Grade Immortal Guqin 'Echo of Bells' for 14 listener(s) and earn 13790 ss in tips. |
| 44 | Pilgrimage | PASS | yes | 6 qdo, 6 wk (+4 wk walking) | You reach Probe Quiet on pilgrimage and stay to meditate. |
| 45 | Profession | PASS | yes | 1 qdo, 1 wk | You brew 3 pill(s) ('bag' shows them). |
| 46 | Propose Alliance | PASS | yes | 1 qdo, 1 wk | Probe Ancestral Hall and Heavenly Dao Alliance have formed an alliance! |
| 47 | Random | PASS | yes | 1 qdo, 1 wk | Bo Hu 'Lord of Ten Thousand Vaults' and N9 Probe 'Pillar of Heaven' are now married! Congratulations! |
| 48 | Recruit Defector | PASS | yes | 1 qdo, 1 wk | N17 Probe 'Lord of Ten Thousand Vaults' has defected from Heavenly Dao Alliance to Probe Ancestral Hall! |
| 49 | Recruit Member | PASS | yes | 1 qdo, 1 wk | Bo Hu 'Lord of Ten Thousand Vaults' talked to N18 Probe 'Pillar of Heaven' |
| 50 | Refine Blood | PASS | yes | 1 qdo, 1 wk | N10 Probe refined your Blood into 4 Potential Pills and kept 2 for the work: you take 2. |
| 51 | Register Tournament | PASS | yes | 1 qdo, 1 wk | Registered for tournament at Probe Square |
| 52 | Reincarnate | PASS | yes | 1 qdo, 1 wk | Bo Hu has been reborn: a second youth at age 18, level 1, potential 80 -> 85 (life 2). |
| 53 | Restock Trip | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Result: -1,823 ss |
| 54 | Return to Sect | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Result: +2 sect point(s) |
| 55 | Revenge | PASS | yes | 1 qdo, 1 wk | [Fight] Tag-team assault: you, N12 Probe, N3 Hu, N9 Hu, N11 Probe, N13 Probe, Samuel Probe and Rin Hu take on N6 Probe, one after the other (the target tires each round). |
| 56 | Rob Caravan | PASS | yes | 1 qdo, 1 wk | [Fight] Tag-team assault: you, N12 Probe, N3 Hu, N9 Hu, N13 Probe, Samuel Probe and Rin Hu take on N11 Probe, one after the other (the target tires each round). |
| 57 | Sect Mission | N/A | - | - | given by the game from the sect's board (Accept Sect Mission); 'qadd' refuses it by design |
| 58 | Sect Raid | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Result: +20 sect point(s) |
| 59 | Seek Potential | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | You swallow a Potential Pill. Potential +1 (now 81). |
| 60 | Seek Teacher | PASS | yes | 5 qdo, 5 wk | N12 Probe 'Sovereign of the Nine Heavens' teaches you the Seven Cinder Dragons Fist. |
| 61 | Seek Territory | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | [Fight] You defeated the beast Blood-Scented Giant Ant (Lv.1), power 43 vs 0, 100% to win. |
| 62 | Sell Stock | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Bo Hu 'Lord of Ten Thousand Vaults' sold cargo at Probe City for 60798 ss |
| 63 | Sell Technique | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Sold Blaze Tortoise Claw (potential 90) to the library for 810000 ss (a fee of 20%; the book is theirs now). |
| 64 | Sightseeing | PASS | yes | 3 qdo, 3 wk (+4 wk walking) | You look around Probe Quiet. |
| 65 | Spectate Tournament | PASS | yes | 1 qdo, 1 wk | You take a seat to watch the tournament at Probe Square � 0 signed up so far. Stay in this place until it is held: you will see the last rounds and may bet. |
| 66 | Study Trip | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Bo Hu 'Lord of Ten Thousand Vaults' studied Razor Heron-Subduing Kick [Metal] lvl: 99 Potential: 99 at the library for 9801000 ss! |
| 67 | Tame Beast | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Cricket Wild Boar (Lv.5) accepts you as its master! (1/6) |
| 68 | Teach Disciple | PASS | yes | 4 qdo, 4 wk | You teach N13 Probe the Three Granite Gates Sword Art. |
| 69 | Trade Run | PASS | yes | 2 qdo, 2 wk (+4 wk walking) | You sell your spare herbs and metal in Probe City for 11400 ss. |
| 70 | Visit Family | PASS | yes | 4 qdo, 4 wk | You spend a few days with N9 Hu 'Pillar of Heaven'. |
| 71 | seek talk (Talk To) | PASS | yes | 1 qdo, 1 wk | N3 Hu: "Can I ask you something odd? How far do you think you can go?" |
| 72 | seek friend (Befriend) | PASS | yes | 1 qdo, 1 wk | Bo Hu 'Lord of Ten Thousand Vaults' talked to N7 Probe 'One of the Divine Hundred' |
| 73 | seek fight (Duel) | PASS | yes | 1 qdo, 1 wk | [Fight] Tag-team assault: you, N12 Probe, N3 Hu, N13 Probe, N7 Probe, N9 Hu, N11 Probe, Samuel Probe and Rin Hu take on N5 Probe, one after the other (the target tires... |
| 74 | seek spar (Spar) | PASS | yes | 1 qdo, 1 wk | Bo Hu 'Lord of Ten Thousand Vaults' and N3 Hu 'Lady of Ten Thousand Vaults' are now married! Congratulations! |
| 75 | seek court (Courtship) | PASS | yes | 3 qdo, 3 wk | Bo Hu 'Lord of Ten Thousand Vaults' and N3 Hu 'Lady of Ten Thousand Vaults' are now married! Congratulations! |
| 76 | seek kill (Hunt Down) | PASS | yes | 1 qdo, 1 wk | Your squad (N12, N3, N9, N11) fights beside you. |
| 77 | explore Lost Ruins | PASS | yes | 1 qdo, 1 wk (+8 wk walking) | You search the Lost Ruins of Probe Ruin and carry out 14400 ss of old coin. |
| 78 | explore Beast Lair | PASS | yes | 1 qdo, 1 wk (+8 wk walking) | [Fight] You defeated the beast Twin-Headed Snake (Lv.5), power 43 vs 4, 100% to win. |
| 79 | explore Martial Trial | PASS | yes | 1 qdo, 1 wk (+8 wk walking) | Razor Heron-Subduing Kick [Metal] lvl: 20 Potential: 99 was upgraded! |
| 80 | explore Celestial Trial | PASS | yes | 1 qdo, 1 wk (+8 wk walking) | [Fight] You defeated the beast Polished Field Mouse (Lv.7), power 43 vs 6, 100% to win. |
| 81 | Ghost City Rebirth | PASS | yes | 1 qdo, 1 wk (+4 wk walking) | Bo Hu 'Lord of Ten Thousand Vaults' is reborn as Nuan Howard in Probe City! (was: Bo Hu) |
| 82 | Possess Womb | PASS | yes | 1 qdo, 456 wk | Kavya Probe has been born to N14 Probe 'One of the Divine Hundred' and N15 Probe 'One of the Divine Hundred' |

### T3 failures: repro and cause

**A. Eight quests made by hand do nothing but "You reached X." (the biggest finding)**
Buy City Array, Buy Sect Array, Buy from Sect, Donate to Sect, Entice Merchant, Hold Tournament, Register Tournament, Spectate Tournament.
- Repro: stand as the city's lord (or a sect's master / member, or anywhere for the tournaments) -> New quest window (or `qadd Hold Tournament here`) -> `goquest` -> `qdo`. The log says "You reached Hold Tournament." and the quest is gone. Nothing is bought, donated, held or registered. The same quests offered by the game itself (alerts) work, because the game makes them as spot quests.
- Cause: `PlayerControl.java:210` (`SPOT_TYPES`) lacks these eight names, so `aboutOf` calls them "place" quests and `createQuest` (`PlayerControl.java:220`) builds an Area quest; their code lives in the SPOT switch of `Character.spotQuests` (Hold Tournament `Character.java:2891`, Register 2918, Spectate 2928, Buy City Array 2959, Buy Sect Array 2975, Entice Merchant 2991, Donate to Sect 3009, Buy from Sect 3073), so the target switch ends in the waypoint fallback `Character.java:3659`. A by-hand *Spectate Tournament* also gives no seat (`Tournaments.takeSeat` is called from the spot case).
- Fix (proposed, not made): add the eight names to `SPOT_TYPES` (they are then done where you stand; each handler already checks the place), or let `targetQuests` hand them to `spotQuests`.

**B. Bounty Hunt**: `qadd Bounty Hunt` makes a place quest but the handler wants a person (`Character.java:3213`, `characters.get(quest.targetCharacter)` with no target): `qdo` removes it, prints nothing. Cause also `PlayerControl.java:208` (`PERSON_TYPES` lacks it). Fix: take the bounty from the city's board (`Area.bounties`) or put it among the person quests.

**C. Quests that end silently when their condition fails** (`qdo` prints nothing, the quest is removed as if it worked):
- Get Pregnant `Character.java:3566`: one try in ten each `qdo`, a miss says nothing and ends the quest (the *Try for a child* button of 2026-10-16 is the better way).
- Recruit Defector `Character.java:3255`: 40% chance, the 60% say nothing.
- Hunt Cultivator `Character.java:2815`: a Monster's spot quest that a person can also make: with nobody weaker in the tile, or (a Good-aligned person) when the only prey is not Evil (the alignment scruple of `Character.java:5549` stops the fight silently), nothing happens and nothing is said.
- Deliver Goods `Character.java:3209` / `Life.java:519` (`deliverMission` returns false without a word when you carry no goods).
- Festival Trade `Life.java:611`: no goods and not a Musician: nothing, no line.
- Disperse `Character.java:3334`: "arriving is the whole quest" and it prints nothing (walk and done, but no "you reach the quieter tile").
- Attend Auction `QuestLife.java:659`: waits up to 8 weeks printing nothing and ends without a line about the auction.

**D. Refine Blood is STUCK with a wrong message** `Character.java:4264-4266`: an alchemist can refine at most `professionLvl / 10 + 1` pills at once; with a new alchemist (skill 1) that is 1, and the refusal says "will not refine less than two pills' worth of Blood (2000); you carry 4000". The person has 4,000 and is told he needs 2,000: he cannot tell the alchemist is too unskilled. Fix: say "N is not skilled enough to refine more than K pills at a time" (the cap), and start the quest only for an alchemist whose skill allows two pills.

**E. Befriend (seek friend)**: passes, but its result is chance (a stranger may stay a stranger); no failure was seen.

### What the matrix found that was fine
Turn cost: every quest costs one week per `qdo`; Visit Family 4, Seek Teacher 5, Pilgrimage 6, Meditation Retreat 8, Gathering Trip 8, Teach Disciple 4, Courtship 3, Sightseeing 3 weeks (the quests that stretch over several weeks say what they do on the first week). All the other rows print their result (`Result:` lines: gold, items, pills, sect points, relationships).

---

## The fix round (same day, after the author's answers)

The author chose to fix findings 1, 2, 5 and 6 and to leave 3, 4 and 7 as they are. State of each (probe `LongRunFixProbe`, 28 checks, and the matrix above):

| # | Finding | Status | What was done |
|---|---|---|---|
| 1 | Eight by-hand quests were only waypoints (and the silent results of section C/D) | **FIXED** | the eight are spot quests (`PlayerControl.SPOT_TYPES`), Bounty Hunt asks for a person (`PERSON_TYPES`); Deliver Goods, Bounty Hunt, Disperse, Get Pregnant, Recruit Defector, Festival Trade, Attend Auction, Hunt Cultivator and the Potential Pill now say what happened; Refine Blood names the alchemist's skill |
| 2 | A friend's battle drafted a MANUAL human into a lethal Tag-team fight (9 of 18 bot deaths) | **FIXED** | `Character.partyBattleCheck` skips a person who plays by hand and says "you stay out of it ('attack' joins them)."; NPC friends still join |
| 3 | The Scion's Salt Road needs Major 2; the Orphan's clue episodes; the Smith's 215 items | left as it is | by the author's choice |
| 4 | Chapters gated by a Major level wait decades (Heir 25.6 y, Disgraced 38.7 y) | left as it is | by the author's choice |
| 5 | Silent results and the wrong Refine Blood message | **FIXED** | see 1 |
| 6 | Poison: one warning line, death in 13 weeks | **FIXED** | notice of kind "danger", a `next:` line in the status block, reminders, a walk stops |
| 7 | The Smith's pace | left as it is | by the author's choice |

Matrix after the fixes: PASS 81, FAIL 0, STUCK 0, N/A 1 (`Sect Mission`: the game gives it, `qadd` refuses it by design). Not rerun after the fixes: the T4 story journeys (the fixes touch the party battle that killed half of the bot's runs, so the numbers in section T4 are from before; a rerun would show fewer deaths).

---

## T4: the ten story journeys

**How the runs were played.** A bot that plays like a careful, passive person: it reads the pinned Story quest, walks and chases to the place or person, attacks the story's foe (and does not flee from that fight), gathers and crafts with the Profession command for counted tasks, hunts for loot, joins a sect when the story allows it, equips and buys gear, flees from or declines fights it is likely to lose, eats when poisoned, breaks through on a button, and otherwise meditates in a quiet place. It never uses a cheat or edits the world. Each start was run with seed 1, then 2 and 3 when the person died before the story ended; the table shows the best run. Two of my first findings turned out to be mistakes of the bot, not of the game (it fled from its own attack on a story foe; it ignored the "you are poisoned" line), and are not reported.

| # | Start | Runs (final bot) | Verdict | Where the time went / what is slow or stuck | Deaths |
|---|---|---|---|---|---|
| 1 | The Last Heir of the Azure Cloud Sect | 1 run to year 60 (+2 earlier runs; the little-stories run did not finish by 60) | **PASS** complete at year 48 (46-56 in other runs) | "Marrow Reach" waits 10.9 y for Major 3 and "What a Sect Is For" 25.6 y after "The Ledger" for Major 4 (the Quest Log says nothing of the wait); the Torch (Cinder, a Runner) took 5 weeks | none |
| 2 | The Rich Scion | 1 run (+ 3 + 3 earlier) | **PASS** complete at year 23 (18-26 elsewhere) | **"The Salt Road" (two shops) opens at Major 0-1 and cannot be done before Major 2**: 9.5 y here, 5-13 y in the other runs; "Ten Thousand Vaults" waits 5 y for Major 2 | year 52 (a Miner, Lv 33); the three earlier runs, before the bot learned to cure poison, died of a Poisoner's contract at 3, 9 and 12 |
| 3 | The Orphan of the Ash Road | 1 run to year 60 | **PASS** complete at year 14 | "The Seventh Star" needs three of five clue episodes that are drawn at random: it opened at year 14 here (6 y after the sword fight) but never in the 60 years of two earlier runs (only 2 clues drawn): the main line of this story can stall for life on dice (`TaleOrphan.java:65-67`) | none |
| 4 | The Disgraced Disciple | 1 run to year 60 | **PASS** complete at year 52 | **38.7 y wait after "Why Your Desk" for "Elder Zhao" (Major 3)**; the little-stories run lost the fight with Brother Tse 313 times at ~0% odds (a bot that does not get stronger) and ended at year 60 in that chapter | none |
| 5 | The Blacksmith's Child | 3 runs | **FAIL: not finished** (died at 16, 25, 34) | counted crafts: "Learn the trade" 5.2 y, "Two Letters" (50 items) 9.6 y, "Your father's notes" 5.7 y, "Evenfall" (100 items) open since year 30; 215 items in all at 4-6 a year, potential 30 gives Major 2 at year 27 | killed at 16 (a Scribe), 25 (a Miner), 34 (a Poisoner, Lv 30) |
| 6 | The Beast-Tamer's Daughter | 1 run (+ 3 + 3 earlier that died at 3-5) | **PASS** complete at year 28 | the last chapter waits for Major 3 (year 28); the first four chapters were done by year 8 | year 39 (a Runner, Lv 29) |
| 7 | The Ghost-Touched | 1 run to year 60 (+ little-stories runs: complete at year 39; one died at 23) | **PASS** complete at year 19 | "What Meng Never Said" waits 4.7 y for Major 3 | none |
| 8 | The Lucky Fool | 3 runs (+ 6 earlier) | **FAIL: not finished** (died at 5, 11, 21) | potential 15: Foundation at year 2-3, then "No Luck at All" (Major 2) never reached; a start that dies young | an Alchemist at 5, a Scribe at 11, a Ghost at 21 (Lv 24-29 arch-enemies) |
| 9 | The Last of the Silver Crane Clan | 1 run to year 60 (+ little-stories run complete at year 11) | **PASS** complete at year 20 | "The Baron's Answer" took 9.3 y (the fight with Baron Hexley) | none (a first-batch run was killed by the Baron at year 30, after the story) |
| 10 | The Wandering Immortal's Disciple | 10 runs | **PASS** complete at year 13-17 in 4 of 10 | "The Road That Is Not Finished" waits 9.9 y for Major 3; "A Taste of Somewhere" needs 20,000 stones (a bot that cannot earn stays at 830 for 35 years: not counted); 6 of 10 runs died at year 2-5 or 31-43 | killed at 2, 3, 3, 4, 5, 31, 36, 43 by Lv 14-28 cultivators |

**What the journeys found** (the file:line of each cause is in the list of 6.35 of `Control Audit and Test Plan.md`):
1. **Story pace is set by the Major level, and the Major level comes late.** A diligent person meditating reaches Major 2 in about 3 years (median of 22 runs; 2 to 27), Major 3 in about 16 (median of 13 runs; 12 to 51) and Major 4 in about 51 (median of 7; 36 to 58); it depends on potential. Chapters that wait for a Major level therefore wait for decades: the Disgraced Disciple waits 38.7 years for "Elder Zhao" (Major 3), the Heir waits 25.6 years for "What a Sect Is For" (Major 4) and 10.9 years for "Marrow Reach" (Major 3), the Ghost-Touched 4-8 years for "What Meng Never Said" (Major 3), the Tamer's last chapter for Major 3. The plan's budget was "a chapter opens within 12 years of the last"; five of the ten stories break it, and nothing in the Quest Log says what the story is waiting for (a "waits for Major 3" line would).
2. **The Rich Scion's "The Salt Road" cannot be done before Major 2**: it asks for two shops and a shop keeper may keep `max(1, Major)` shops (`Character.java:4859`), and the chapter opens at Major 0-1 (`TaleScion.java`). 9.5 to 12.8 years of "You already keep the most shops you can (1)".
3. **The Blacksmith's Child cannot be finished in 60 years at a person's pace**: 215 items in all (10 + 25 + 50 + 30 + 100) at 4-6 items a year (500 units of one stack per item, 10-100 per forage); "Two Letters" (50 items) took 9.6 years alone; potential 30 gives Major 2 at year 27. All three runs died (year 16, 25, 34) before the last chapter.
4. **The Lucky Fool is the hardest start**: potential 15, Foundation at year 2-3, then "Keep going without luck: reach Major Lvl 2" needs the potential gate of 37; the three runs died at years 5, 11 and 21.
5. **The world kills a passive cultivator, and half of the deaths are not the person's own doing.** Of the 29 runs of the final bot, 18 ended in death (11 before year 30, 6 before year 10, median year 22), 11 lived to year 60 and 14 finished their story. In 9 of the 18 deaths the last fight was one in which the person *attacked* while the bot only meditated: a friend's battle quest draws every friend in the tile into a "Tag-team assault", a MANUAL human included, with no prompt and no choice (`Character.partyBattleCheck`, `Character.java:5868`), and the opponent was most often the person's arch-enemy (lethal, ~0-50% odds). The other deaths were arch-enemies and rivals that cannot be refused (`mustFace`) or outrun, and, without the bot's poison cure, a poison contract (all three Scion runs of the first batch: "You feel sick ... eat food of level L+" is one log line and the cure needs food of the poison's level).
6. **No chapter, scene or decision is lost**: every scene appeared, every prompt could be answered (friend offers, challenges, flee, kill or spare, the Forbidden Technique, auction bids, the story's own choices), no exception, no crash.log and no gameError in about 80 runs. The only silent result is `consume` of a Potential Pill (the potential rises, nothing is printed: `Character.java` consumePills).
7. **Lessons and little stories do not get in the way; favors could not be tested.** The lessons are on in every run (15-18 told in the first 25 years); the little stories were switched on in 6 runs (up to 16 told in one); the chapters opened at the same pace as without them (the Major-level gates decide) and no chapter scene was replaced or lost (a story or a lesson waits while a scene window is busy: `GuiLore.busy`). Favors are only asked inside the portrait-window conversation of the GUI, so a headless run never sees one: they need a GUI journey.

**Prompts and windows that appeared** (counts over all the runs; every one could be answered; none was left without an answer):
friend offers 3,779 (the bot accepts, which is how it gains the friends that draw it into their fights), "X challenges you!" 2,397, "Try to flee?" 1,116, "Kill or spare?" after a won fight 596, a stranger asking to join the squad 147, "The fight is deadly. Use the Forbidden Technique?" 137, the breakthrough question (Core Formation, Soul Wandering ...) about 25, auction bids 55 (Heaven-Sent lots, Potential Pills), and the story's own questions (what to do with the cauldron, Orrin's three thousand stones, the goose on the bridge, Stillspring, House Kessel, the robbers' schedule ...) one to three times each.

---

## T0: the regression sweep

**Headless (61 probes):** all pass after reruns of the flaky ones.

| Probe | Passed | Failed | Note |
|---|---|---|---|
| EconProbe | 35 | 0 |  |
| MoveProbe | 5 | 0 |  |
| PolishProbe | 39 | 0 |  |
| WorldProbe | 42 | 0 |  |
| QuestLifeProbe | 57 | 0 |  |
| EndFlowProbe | 8 | 0 |  |
| PcProbe | 111 | 0 | rerun: 110/2 on the first run (S2, S5: the known flaky pair) |
| PcProbe2 | 6 | 0 |  |
| QolProbe | 66 | 0 |  |
| RulesProbe | 22 | 0 |  |
| StartsProbe | 30 | 0 |  |
| TextProbe | 27 | 0 |  |
| LifeProbe | 85 | 0 |  |
| NewProbe | 25 | 0 |  |
| SpecProbe | 3 | 0 |  |
| SimProbeX | 73 | 0 | third run; the first two failed one different random check each (C3f, C2b: known flaky) |
| LoreProbe | 121 | 0 |  |
| TermFuzz | 8 | 0 |  |
| PlanProbe | 22 | 0 |  |
| PotentialProbe | 24 | 0 |  |
| TalesProbe | 206 | 0 |  |
| ErasedProbe | 19 | 0 |  |
| ArcsProbe | 29 | 0 |  |
| SongProbe | 36 | 0 |  |
| SlimProbe | 27 | 0 |  |
| DefendProbe | 49 | 0 |  |
| FixesProbe | 44 | 0 |  |
| UpdateProbe | 26 | 0 |  |
| DialogueProbe | 26 | 0 |  |
| SilenceProbe | 4 | 0 |  |
| BalanceProbe | 11 | 0 |  |
| ArrivalProbe | 7 | 0 |  |
| DupQuestProbe | 10 | 0 |  |
| TurnCostProbe | 49 | 0 |  |
| QuestSilenceProbe | 2 | 0 |  |
| PrintBudgetProbe | 6 | 0 |  |
| PausedProbe | 15 | 0 |  |
| SquadProbe | 36 | 0 |  |
| HuntDownProbe | 18 | 0 |  |
| T12Probe | 12 | 0 |  |
| PrioProbe | 20 | 0 |  |
| BreakthroughProbe | 23 | 0 |  |
| CraftMenuProbe | 26 | 0 |  |
| RankFightProbe | 18 | 0 |  |
| SpotActionProbe | 21 | 0 |  |
| QuestAddProbe | 16 | 0 |  |
| GiftProbe | 29 | 0 |  |
| SectThemeProbe | 12 | 0 |  |
| StoryCountProbe | 10 | 0 |  |
| BatchAProbe | 22 | 0 |  |
| BatchBProbe | 37 | 0 |  |
| ElementsProbe | 27 | 0 |  |
| FightReplayProbe | 23 | 0 | after the probe was updated for the seat (R6a, R6a2) and a natural-flow check added (R7: the game offers the quest, doing it takes the seat); first run 16/3 |
| MonsterProbe | 21 | 0 |  |
| PlaylistProbe | 12 | 0 |  |
| CheatsProbe | 82 | 0 |  |
| ConcertProbe | 17 | 0 |  |
| SongGenProbe | 20 | 0 |  |
| Chaos | 9 | 0 | its summary line is not tagged [T] |
| LongRunFixProbe | 28 | 0 | new (the fix round); 4 of 4 reruns clean after two probe checks were made independent of random neighbours (G4) and of the walk state (B2) |
| HelpCheck | - | 0 | PASS: 60 pages, 605 links, 53 images, 0 problems |
| OnboardProbe | - | 0 | runs clean (a report, no checks) |

**GUI (20 probes, a display is needed):** all pass; QolGui lost 3 timing checks in the sweep and passed 53/0 on the rerun.

| GUI probe | Passed | Failed | Note |
|---|---|---|---|
| SceneWindowProbe | 21 | 0 |  |
| PersonListProbe | 27 | 0 |  |
| ItemListProbe | 19 | 0 |  |
| PlaceListProbe | 20 | 0 |  |
| WrapTableProbe | 19 | 0 |  |
| ChronicleMergeProbe | 16 | 0 |  |
| DockMergeProbe | 17 | 0 |  |
| EditRandomProbe | 13 | 0 |  |
| ControlGuiProbe | 18 | 0 |  |
| PausedSweepProbe | 6 | 0 |  |
| PausedGuiProbe | 9 | 0 |  |
| LogLiveProbe | 4 | 0 |  |
| VignetteGuiProbe | 11 | 0 |  |
| FightGuiProbe | 16 | 0 |  |
| ManagerGuiProbe | 22 | 0 | after the new Cheats tabs were added to the probe (19 before) |
| SquadGuiProbe | 12 | 0 |  |
| PcProbeGui | 131 | 0 |  |
| MoreGui | 36 | 0 |  |
| DialogueGui | 13 | 0 |  |
| QolGui | 53 | 0 | rerun; the sweep's run was 50/3 on timing checks: confirming deletes every file of the slot / Follow adds them to the tracker / Control (confirmed) switches (QolGui fails 0-3 of these about every other run) |

**Sweep after the fix round (2026-10-18):** every headless probe passes; the only failures were the known random ones, each clean on a rerun: WorldProbe E1 (tiles for the dungeon test), RulesProbe R3 (a statistical check), SimProbeX C3f / A5, PcProbe S5 / S14 (random fights, timing) and PcProbe S5b, which was a stale check (a man without a spouse is now told so by `Try for a child`; the probe expected the old wording, so it failed whenever the random PC was male) and is fixed.


---

# Round 2: the long-run bug check (2026-10-18 / 19)

Asked for as a **beta test of the whole game** ("act like a beta tester trying to break the game"): the full probe sweep, the quest matrix, the ten story journeys, and, new this round, **soaks of a whole world** with a typing "monkey", an audit of every person and tile, the author's own year-330 save, the real size (100,000) and abuse of settings and saves. Nothing in `src/` was changed this round; the findings below are proposals (the author's choice: test-only fixes done, the rest proposed).

## What was run

| Batch | What | Result |
|---|---|---|
| T0 headless | 58 headless probes (3,000-person copy) | every probe passes; the only failures were random ones, each clean on a rerun (below) |
| T0 GUI | 21 GUI probes (ItemList, PersonList, PlaceList, Dock, Control, Paused, Fight, Manager, Squad, PcProbeGui, MoreGui, Dialogue, DeathMenu, Chaos ...) | all pass; PcProbeGui "Run executes the script..." failed once (known timing), 131/131 twice on rerun |
| T3 | quest matrix, 82 rows, 4 runs | PASS 81 / FAIL 0 / N/A 1 in three runs; once "Recruit Defector" FAIL 1 (the person randomly says "will not leave"; the result is shown, so it is chance, not a bug) |
| T4 | the ten Story starts, 60 game years, 1-3 seeds each, careful bot | 17 runs, **0 exceptions, 0 crash logs**; same picture as the first round (below) |
| Soak, spectator | 20,000 people, 150 years, `-Dtdf.verify=true` | 0 exceptions, the game's own `Verify` "all well" for 5,500 ticks, heap 370 MB |
| Soak, Auto | a person who plays in AUTO, 150 years, 20,000 | same; the person lived the whole time |
| Soak, Hybrid | a person running a saved script, 100 years, 20,000 | same |
| Soak, monkey (MANUAL) | **three** typists, 150 / 134 / 85 years (the character died at 134 and 85), **13,320 + 6,696 + 289 commands**, 20,000 people | **0 exceptions, 0 "[command error]" lines**, every documented command was tried with plausible and with silly arguments (negative, huge, unknown ids), saves and loads in between |
| Real size | 100,000 people, 10 years, monkey (937 commands) | 0 exceptions, 0 violations, about 1 game year a minute, heap 831 MB of 3 GB |
| **The author's own save** | `saves/auto-1` (year 330, population 185,423, Mila Pinecrest Major 6), a copy, loaded into the new build, 1+ year played | **loads**; `GearNames.fixAll` renamed **1,503,047** weapons, garments and arrays; the world ran on with no exception and no `Verify` violation (the character, in AUTO, was killed by an attacker at 19% odds: chance) |
| Fuzz | `TermFuzz`, 6 seeds x 1,500 commands | 8/8 each, no command error, no crash log |
| Abuse | 4 corrupt settings files (empty, garbage, nonsense numbers, huge numbers) and 7 damaged saves | no exception, no crash log; the saves findings are F2 and F3 below |
| Look | Techniques tab and Edit Self rendered to PNG | Edit Self fine; the Techniques tab has F4 |

### The random failures of the sweep (each passes on a rerun)
`WorldProbe` E1 (tiles for the dungeon test), `SilenceProbe` P (Innovative inspiration), `DialogueProbe` E7 (a rumour), `SquadProbe` Q9 / Q5b (an NPC attack meets the member told to fight first; a hunt), `PcProbeGui` "Run executes the script", `QuestMatrix` Recruit Defector. **`FightReplayProbe` R7** failed 2 runs in 3: the probe sets `p.dead = false` after staged deadly fights while the game has already queued the person's removal, so the game carries on as a spectator world and "wait 1" is never answered (thread dump: the game thread was running ticks). That is a **test fault, not a game bug**; hardened (the probe now puts the person back) and it passes 7 of 8 runs now.

### T4 compared with the first round
Same pace and the same causes: the Major-level waits (Heir "Marrow Reach" at year 25, Disgraced "Elder Zhao" at year 20), the Smith not finished (3 runs, died at 8, 13, 17: Evenfall never opened), the Lucky Fool not finished (died at 15, 16, 18), the Orphan finished at 26, the Ghost at 23, the Clan at 13, the Disciple at 16, the Scion at 21, the Tamer at 53 (last chapter waits for Major 3). The only "problems" listed are free commands that take no time (`join`, `buy`) and the Scion's "You already keep the most shops you can (1)" (140 times in one run: the known finding 3 of the first round, left as it is by the author's choice). Deaths: 13 of 17 runs ended in death by an NPC, as before.

## Findings

| # | Severity | Finding | Evidence | Cause | Proposed fix |
|---|---|---|---|---|---|
| **F1** | **medium, existing bug** | **An Innovative (or Beast) person's known-techniques list grows by one copy at every sort, forever**, and a technique he wears can be in the list too | 1,600-10,800 audit hits per 100-150-year soak; in the **author's year-330 world 484 people have lists over 40 entries, up to 388**, 431 hold a technique twice, 2,360 wear one that is also "known" | `Character.sortTechniques` (was in `equip`): when two techniques compete for the second slot the loser was **added to the list again** although it had never left it (`tempAddMartial2` / `tempAddMovement2`), and the worn one is removed only once | `tools/known-techniques-proposal.diff` (tested in a scratch copy: `TechProbe` A5 / A6 pass, 40 of 40): never re-add the displaced candidate; `tidyKnown()` removes duplicates and worn entries at every sort, so existing worlds heal at the next sort. Costs: the list is walked at every `equip` (which fights and upgrades call), so long-lived Innovatives slow the tick and bloat the save a little more every year |
| **F2** | medium (Windows) | **A save can finish with a stale `.meta`, and the next load then calls the newest save "damaged" and loads the older backup** | `Chaos` logged `[SAVE ERROR] FileSystemException: saves\quick.meta: The process cannot access the file because it is being used by another process` after 141 saves under load | `SaveManager.writeSlot`: the `.dat` is swapped in first, then `Files.move(.meta.tmp -> .meta)`; if anything holds the old `.meta` open at that instant (the Saves window listing slots, antivirus, **OneDrive: the repo and its `saves/` live in OneDrive**) the move throws, the `.dat` is new but the `.meta` still carries the old CRC, and `load` sees a CRC mismatch (`checksumOk`) and falls back to the `.bak` | retry each move a few times with a short wait (5 x 150 ms); if the meta still cannot be replaced, delete it (no CRC means "no comparison") or write it in place; and let `load` try the `.dat` before calling it damaged when the CRC alone is wrong |
| **F3** | medium (data risk, UX) | **Choosing a damaged save at the start-up menu silently starts a brand-new world** | 5 of 7 damage kinds (truncated, empty, garbage, one flipped byte; the "[DAMAGED]" flag shows for 3 of them) print nothing after "Load which?" and go straight to "Mode: (S)ingle Player..." | `GameDriver.main`: `SaveManager.load(...)` returns false, `lastError` is set but never shown, and the new-game flow runs. The first autosave then rotates `auto-0` over `auto-1`, which can overwrite the very slot (and its `.bak`) the player wanted to repair | say why ("'x' could not be read; nothing on disk was changed"), return to the saved-games menu instead of starting a game, and never rotate an autosave over a slot flagged damaged |
| **F4** | cosmetic (new this session) | **Techniques tab: the Slot column is too narrow** ("Martial 2" wraps, "Movement" is cut to "Movem-ent") and **the hint after the buttons is hidden** (the button row wrapped and its second line is clipped); the Mastery bars draw as blobs (older) | the screenshot `lr/shots_out/backpack_techniques.png` | `GuiMore` (the new buttons made the row longer; the Slot column has no preferred width) | set the Slot column to about 80 px and no wrap; shorten the hint to a tooltip; give the Mastery renderer a thin bar height |
| F5 | low (test hygiene, done) | `TalesProbe` used the Java 15 escape `\s` (does not compile with `--release 8`); `GuiShots2` had no Techniques shot; `FightReplayProbe` R7 fragile | | | fixed in the probes (no `src` change); `MoreGui` and `PcProbeGui` still use `modelToView2D` (Java 9+; not part of the IntelliJ source root, so left) |

**Not bugs (checked):** "city whose owner is dead" in the audit (3-12 cities at any moment): the yearly pass clears a dead owner (`GameDriver.java:1372`), so it is a lag of at most a year; "a command that spent more than 60 weeks" (9 in one run: `meditate` 116, `limit abc` 79): a long walk or wait typed earlier that was still running when the next line was timed (measurement overlap); a character killed within a year of loading the author's save: an NPC attack at 19% odds.

**Also checked and clean:** every command the Help quotes exists (`help_cmds.py`: 131 words, the 12 that are not commands are script keywords and folder names); no negative money, level, potential or quantity anywhere in a 296,000-person world; gear grade always fits its level after `GearNames.fixAll`; no technique above its potential; no duplicate quest id; the world's own `Verify` found nothing in 5,500 ticks of three soaks.

## New tools (all in `tools/probes`)
- `SoakProbe` (`-Dsoak.mode=watch|auto|hybrid|monkey -Dsoak.years=N -Dsoak.seed=S -Dsoak.minutes=M`, `-Dsoak.load=true` continues the newest save in `saves/` of the run folder, `-Dsoak.pop=true` for the 100,000 build): the soak, the monkey and the audit described above; prints `[S]` progress lines, `[V]` violations with examples, `[U]` per-command use and errors.
- `longrun/setup.sh`, `sweep.sh`, `soak.sh`, `robust.sh` (set `WORK` to a scratch folder): build a 3,000 / 20,000 / 100,000-person copy, run a list of probes with a summary per probe, run a soak, and the corrupt-settings / damaged-save checks.
- `StoryDump`, `story_stats.py` (the story work), `TechProbe` now 40 checks (A5 / A6 fail until F1 is fixed).

**Status (2026-10-19): F1, F2, F3 and F4 are fixed** (approved by the author; see the DEVLOG). `tools/known-techniques-proposal.diff` was applied and removed. A damaged save now returns to the saved-games menu with a message.

## Round 3: the final bug check (2026-10-21)

Run after the Garden's Variety Tournament, Fame, the siege chain, the place pictures and the print pass. A 3,000-person copy built with `javac --release 8`; the author's saves, settings and `dist/` were not touched (the real save was a copy).

| Group | What | Result |
|---|---|---|
| Headless sweep | 74 probes in three parallel groups (list in `tools/probes/README.md`) | all pass. `EconProbe` (a null tile in a 3,000 world, the known flake) and `SimProbeX` (C6 / C2b / C3f, the known flakes) failed once and passed on rerun (35 of 35, 73 of 73) |
| GUI sweep | 22 probes on a display, one at a time | all pass. Two probes had stale expectations of the new sixth Rankings tab (`PcProbeGui` 5 boards -> 6, `DockMergeProbe` tab list) and were updated; `PcProbeGui` kill/spare and the decision timeout are timing flakes (131 of 131 on rerun) |
| New GUI probe | `GardenGuiProbe` (13): `cheat garden` through the real windows (scene with picture, fight window, result window), a Story scene and a Lore scene with their pictures | pass; the screenshots were read |
| Story journeys | all 18 starts (`clan` is the Crane story's id), up to 2 seeds, 60 years, MANUAL | 0 exceptions, 0 silent results. The bot dies young in half of the runs; the Door chapters wait for Ascension as designed (hard gate), the Tamer / Chef / Disciple / Musician / Orphan / Clan runs that lived reached it at Major 2-4 and waited. `disgraced` stayed 50 years in "The Monk Who Does Not Speak": the bot has no gear (power 5 against 16, chance ~0%), not a story fault |
| Soaks | `watch` 151 years, `auto`, `hybrid`, `monkey` 106 years (`-Dtdf.verify=true`) | 0 exceptions, 0 command errors, 0 thread deaths; every tick verified |
| Soak audit | "a city whose owner has been dead for two audits" (1-4 per run) | a false positive of the probe: it compared the city, not the lord (a lord who died, was replaced and died again within two years); `SoakProbe` now compares the lord |
| Robustness | corrupt settings (empty, garbage, bad numbers, huge) and damaged saves (7 kinds) | no crash for the settings; a damaged save returns to the menu with a message. The `NoSuchElementException` after it is the script running out of input at the next menu |
| Old saves | `CompatProbe` with the repo's `save.dat` (100,000 people, week 2342) | 17 of 17 |
| The author's own save | a copy of `saves/auto-1` (year 329, 185,423 people) | loads, runs two years with no exception; the tick is 2.1 s (the epoch's 2 s rule fires) and the live heap 1.8-2.1 GB |
| `-Xlint:all` | | nothing that is a bug (intentional fall-through in `event()`, int truncation in compound assignments, redundant casts) |

Findings: none that needed a code fix. Fixed on the way: Foundation and Core Formation breakthroughs never printed for the person who plays (now they do); the probes above.
