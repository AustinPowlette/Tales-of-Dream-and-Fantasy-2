# Design: letting people out of the Garden at Ascension

Status: **BUILT 2026-10-18** as B2 with the author's decisions of sections 2b and 2c (sections 3-11 below are the first draft of the design and are kept for the reasoning; where they disagree with 2b/2c, 2b/2c won; see `Game Info.md` section H for what exists). Earlier status: **DESIGN ONLY, nothing is built** (asked for 2026-10-18; the author said "don't implement yet", then "make the change designed in detail"). Everything here is a
proposal until the author answers the questions at the end. Lines marked *(check)* are facts I read quickly in the code and would verify before building.

## 1. Why

The realms are called Qi Cultivation, Foundation, Core Formation, Soul Wandering, Nascent Soul, Ensoulment, Nihility, **Ascension**, Half-Immortal, Pseudo Immortal, True Immortal.
"Ascension" is the word for leaving. Today nobody can leave there: the Door (the Seam under the Empty Seat) opens only at Major 10, level 101, so three whole realms
(Half-Immortal, Pseudo Immortal, True Immortal) sit between "Ascension" and the Door. The names say one thing and the story says another.

Goal: the Door can be opened at Ascension, **leaving is a choice, staying stays possible**, and the three realms above Ascension get a meaning of their own.

## 2. The idea in one paragraph

The Garden has a ceiling: its sky is the Gardener's own mind, and Ascension is the top of what a person can grow *inside* it. At Ascension the Seam opens and the person may
go through. Most who go through meet the Old Man and make the choice that exists today (Stay or Leave, Good / Evil / Neutral). Those who **stay** are the only ones
who keep growing past the ceiling, because they now borrow strength from the Gardener: **Half-Immortal** is a person lent a lamp, **Pseudo Immortal** is a person who has become
an *image* like ??? (a reflection that can act in the Garden while the real self is in the array), **True Immortal** is a person who has gone the whole circle (the rebirth cycle the
game already has). So the three top realms become the realms of the Stayers, and "Leave" stays final and earlier.

## 2b. The author's answer (revision 1) -- this replaces the "leave" quota of Level B

The author likes Level B, with three changes:
1. **Seekers stay until True Immortal.** The Palace's seekers (`State.seekers`, humans and NPCs) do not cross before True Immortal, so that they can become lamp-bearers.
2. **Only True Immortals become lamp-bearers.** Below True Immortal nobody becomes a lamp-bearer (no Wandering Immortal, no place among the nine lamps). Their **descendants may still get a bonus** (the blessing).
3. **No yearly quota.** Instead, **a percentage of the people who reach a realm decide to leave** at that moment.

What that makes (call it **B2**):

| Who | At Ascension / Half-Immortal / Pseudo Immortal | At True Immortal |
|---|---|---|
| **Seekers** (Palace road, humans and NPCs) | never leave; the Door is read, not crossed. A human seeker may open the Seam to *talk* (the Old Man's scene) but the Leave menu is closed until True Immortal ("not yet: you are not strong enough to carry a lamp") | cross as today (`npcCross`: soldier / stay / outside / attack by alignment), lamp cap 9 |
| **Everybody else** (the ordinary cultivators) | when they *first* reach the realm, a **percentage** (setting, default 20% / 15% / 10%) decide to leave the Garden: they are gone with a chronicle line ("X reached Ascension and went through the Seam"), not lamp-bearers. The rest stay and go on | the same roll, default 25%: these become **lamp-bearers** while lamps are free (`LAMP_MAX` 9), else they just leave (Outside) or stay |
| **A human who is not a seeker** | gets the same roll *as a choice* (a window: "The Seam is open to you. Go through, or stay?"), never forced | choice, as the endings today |

- **No ceiling and no "leave" flag.** The gate of Level B is gone: everybody can keep rising, so the top of the world thins out by itself instead of by decree. With 20/15/10% leaving at the three realms, about 0.8 x 0.85 x 0.9 = 61% of the people who reach Ascension are still there at True Immortal (before deaths); the percentages are settings (`settings leavepct a h p t`) and a probe can tune them against today's realm counts.
- **Descendants.** A leaver below True Immortal gives a **smaller blessing**: default +2 fighting level and +2 potential for each such ancestor in the line, against +5 for a lamp-bearer (`LoreEndings.bless` takes an amount; `Character.blessing` is already a per-character number that adds up along the line). Both kinds stack, capped as now (potential <= 100). *(Question 8)*
- **Why a percentage and not a quota:** it needs no ranking list and no yearly pass, only one die roll per person per realm at the moment of the breakthrough (`Character`'s breakthrough code, the three places that print "has broken through to ..."), and it reads as a story ("some of them leave when they see the sky"), not as the Gardener picking favourites.
- **Who is never rolled:** seekers, humans (they get the window instead), Wandering Immortals, people of a Story fated to a human (`fatedTo`), characters who hold a Rankings title that year (so the boards do not lose their champions in one tick; they are rolled next year) *(optional)*, children (cannot be that realm anyway).
- **The Palace's Elders:** an Elder who is not a seeker gets the percentage roll like anyone else (the Palace elects another Elder as today).
- **Hidden ending and the Steward/Usurper path** are unchanged: they need level 101, which a human only reaches by staying.

What this changes in the tables below: Section 4's "leave" flag, `State.leave`, the breakthrough gate (`Lore.mayRise`) and the yearly lift are **dropped**; the Door text/steps of section 3 stay, with the Leave menu closed to seekers below True Immortal. Section 5 (what the upper realms mean) gets a simpler reading: Half-Immortal and Pseudo Immortal are the realms of *those who were free to leave and stayed*; True Immortal is the realm of the lamp-bearers and of the rebirth cycle.

## 2c. The author's decisions (revision 2) -- these are final unless the author says otherwise

| # | Question | Decision |
|---|---|---|
| 1 | Blessing for a leaver below True Immortal | **+2 fighting level and +2 potential** per such ancestor in the line (lamp-bearers stay at +5 / +5) |
| 2 | Percentages (20 / 15 / 10 at Ascension / Half / Pseudo, 25 at True Immortal for non-seekers) | **accepted** as the starting values (settings) |
| 3 | A human seeker below True Immortal | **may open the Door**, but is not a lamp-bearer and is not forced into the army: they need **their own set of endings** (below) |
| 4 | A human who is not a seeker | the Door is a **choice available at all times**, not only at a breakthrough |
| 5 | Non-seeker True Immortals | **may become lamp-bearers** (while lamps are free) |
| 6 | Where the Door opens | **at the start of Ascension (level 71)** |

### What follows from them

**The Door for a human, at any time.** From Major 7, level 71, `lore door` (alias of `lore cross`, works from anywhere, like `lore leave` does now), a Me > Door button and a standing line in the Quest Log ("The Door is open to you") open the Seam. A scene `m7` (at level 71) tells every human the sky has become thin. No travel to Originis and no "nine places read" test is needed for the *early* Door; those tests stay for the lamp-bearer endings and the hidden ending (the Old Man's talk changes with how much was read). The window closes when the human closes it, nothing is forced, and a human who goes back changes nothing.

**Two Doors, by realm.**
- *The Early Door* (Major 7, 8, 9; seeker or not): the new endings below.
- *The Full Door* (Major 10, level 101, nine places and the Empty Seat read, as today): the existing endings, lamp-bearer included. A **seeker** is told at the Early Door "when you can carry a lamp, come back"; a seeker who goes back and rises to True Immortal gets the Full Door unchanged. A human who is not a seeker and reaches True Immortal also gets the Full Door, with the same lamp rules as the NPCs (a free lamp, else only the other choices).

**The Early Door scene** (`early_door`, text in `LoreText`/`LoreEndings`, plain voice): ??? cannot come; the Old Man (the Gardener) meets the human at the Seam in a short scene: *you are strong, but a lamp needs a True Immortal; I cannot put you in the array yet; you may go home, stay and grow, or go your own way, and nobody here will force you.* Then:

| Choice | What happens |
|---|---|
| **Go back** | nothing; the Door stays open |
| **Stay** (Good / Evil / Neutral) | exactly the existing Stay paths (Steward, Usurper, Idler; `lore leave` undoes it; the hidden ending still needs level 101, so the Usurper must rise past Ascension as a Stayer) |
| **Leave, Good: the Rear Guard** (new, final) | not a soldier and not a Wandering Immortal: the human goes Outside to carry water, bandages and messages behind the line, safe and useful, never in the array. Ending stub `end_rearguard` (plain, short, bittersweet). The body does not come back. Descendants get **+2 / +2** (a `bless(c, 2)`). Score: level x realm, no lamp bonus. |
| **Leave, Evil: the Early Strike** (new, final) | as "strike at the Gardener" but the human is much weaker: the array kills them with **85% (Ascension), 75% (Half-Immortal), 65% (Pseudo Immortal)**; always death once nine lamps are carried; a death sends the blood back as today (+5 potential for the living and the unborn, cap 100, stacking); spared: walk the Outside as a wanderer (one of the Outside branches, weighted to the bad ones). |
| **Leave, Neutral: walk out alone** | one of the 12 Outside branches, weighted by realm (more bad branches the lower the realm; the same list, only the odds change); you may hear of the Old Man or not |
| **The hidden ending** | unchanged and still locked by level 101 / potential 200 / dark path |

Hall of Fame and the Roll of the Crossed show "left early" with the realm, and the score has no lamp bonus. `lore leave` for a Stayer picks the Early or the Full menu by the realm they have **at that moment**.

**NPC leavers.** As in section 2b, with the numbers above. Non-seeker True Immortals may take a lamp (decision 5): to keep the lamps from being filled by strangers before the seekers arrive, I propose that non-seekers may take a lamp only while **fewer than 5 of the 9 are carried** (four stay free for seekers and humans). *(new question A)*

## 3. What changes for the player (the story)

| Moment | Today | Proposed |
|---|---|---|
| Act V (far places) | Major 8 | Major 7 (Ascension) |
| Act VI (Empty Seat) | Major 9 | Major 7 |
| Act VII (the Door) | Major 10, level 101 | Major 7, **level 71 (the start of Ascension)**: decided by the author |
| The Door scene | `door` at Major 10 | `door` at Major 7; text says "this is the top of the Garden" instead of "True Immortal" |
| `m8`, `m9` scenes | on reaching Major 8, 9 | they become **scenes of a Stayer** (shown when a person who stayed breaks through to 8 / 9); `m7` gets the new weight |
| Stay | back to the Garden, same realm rules | back to the Garden **with the Gardener's leave**: may pass Ascension (Half -> Pseudo -> True) |
| Leave | final, at True Immortal | final, at Ascension (earlier); the arrival text is a **recruit**, not a veteran |
| Hidden ending (`lore usurp`) | Dark path, potential 200, level 101 | unchanged, but only a Stayer can reach level 101 now, so it is **the Evil Stayer's goal** (this keeps it meaningful) |
| Wandering Immortal (Leave, Good) | returns at True Immortal | returns at Ascension; keep "cannot die, a pill and a hint every turn"; bless values per soldier unchanged |

Reading-order / the nine places: unchanged. The Empty Seat still needs the nine places read and an Elder.

## 4. The ceiling (the part that can break the game)

If the ceiling applies to *everyone*, Half-Immortals, Pseudo Immortals and True Immortals vanish from the world, and everything that watches them (Rankings, the `[HERO]` lines,
True Immortal rebirth, tribulations at Major 9, Monsters capped at the 20th strongest cultivator, the late game) loses its top. So three levels of change, in order of risk:

- **Level A, doors only (safest).** The Door opens at Ascension; the Stay/Leave endings are reachable earlier; the realms above stay open to everyone as now. The story gets earlier
  and the names make more sense, but the three top realms keep no meaning. Cost: small (Lore + LoreEndings + text).
- **Level B, the Gardener's leave (recommended; SUPERSEDED by B2 in section 2b: no flag, no gate, no quota, a percentage leaves instead).** Level A plus: passing Major 7 -> 8 needs the **leave**. The leave is held by (1) any person who crossed and stayed, (2) anyone at Major 8+
  when the update arrives (grandfathered), and (3) **a yearly quota of "lifted" cultivators**: the Gardener lifts up to N Ascension cultivators who rank highest (Rankings board), N tuned
  so that the number at Major 8-10 stays near today's (measured by a probe, see 7). So the world keeps its immortals but they are scarce and chosen, which is also a story ("the Heavens
  have hands", see the Gardener's Shed). Cost: medium; the quota is the risk.
- **Level C, hard ceiling.** Only Stayers pass Ascension. NPC Half-Immortals die out, the top of every board is Ascension. Cost: large rebalancing; not recommended unless the author wants the world to feel capped.

I recommend **B**. It needs one new flag (`Character.leave`, or a set in `Lore.State`) *(check which: Character fields are saved by serialization, a set in State is simpler for old saves)* and one gate in the breakthrough code *(check: Character.cultivate / the realm switch around lines 962-988 that prints "has broken through to Half-Immortal / Pseudo Immortal / True Immortal")*.

## 5. Meaning of the three upper realms (flavour + one mechanic each)

- **Half-Immortal (8): lent a lamp.** A Stayer who now carries part of one of the nine great lamps. Mechanic: none new needed (the existing Stay gifts/culls continue); text only. Optional: counts toward the lamp total (`LAMP_MAX` 9) so Stayers share the cap with soldiers *(Q4)*.
- **Pseudo Immortal (9): an image.** Like ???: the person's reflection can act in the Garden while the person is "in the array". Optional mechanic: a Pseudo Immortal who dies leaves an image (a ghost-like NPC the human can talk to; reuse `isGhostForm`), instead of a true death. Fits "Pseudo". *(Q3 whether this is wanted)*
- **True Immortal (10): the circle.** Keeps today's rebirth cycle (`Character.java` ~1184, "completed a cycle and has rebirthed"), the 1-in-5 roll and potential 100. It remains the gate to the hidden ending.

## 6. Code and text touch list

Gates (Door / step thresholds):
- `Lore.CHAIN` (Lore.java ~99): thresholds 8 -> 7, 9 -> 7, 10 -> 7 and the level (101 -> 80); step *indexes stay the same*, so old saves keep valid `PState.step`.
- `Lore.STEP_SEAT`, `LEVEL_SCENE_MAJOR` / `LEVEL_SCENE_ID` (Lore.java ~116-117, 546, 777): `door` moves to 7; `m8`/`m9` become Stayer scenes (new trigger in the breakthrough code).
- `Lore.whyNotCross` (Lore.java ~859: "Major Lvl 10 ... level 101").
- `LoreEndings.readyToCross` (~757) and the NPC door test `seekersYearly` (~873: `majorLvl >= 10 && lvl >= 101`), `npcCross` (~878: odds per alignment stay as they are).
- `LoreEndings` line ~555 (`lore usurp`: potential 200, level 101) stays as is.
- `Lore.State`: new set `leave` (ids with the Gardener's leave) and `yearlyLift` bookkeeping; `LoreEndings.yearly` gets `liftYearly(s)`.

Breakthrough gate (Level B):
- Character.java: the branch that lets Major 7 become 8 (and 8 -> 9, 9 -> 10) asks `Lore.mayRise(this)`: true when lore is off (old worlds keep old rules), or the person is in `leave`, or Major >= 8 already.
- `Character.hero*` messages ("first to reach Pseudo Immortal") keep working; Rankings and epoch are untouched (epoch culls Major 0-3 only).

Text:
- `LoreText`: scenes `m7` (new weight), `m8`, `m9`, `door`, `seat` (the "level 101" and "True Immortal" lines), `end_threshold`; `LoreEndings`: threshold text, Wandering Immortal arrival text, hints.
- Help: `HelpPagesMore` lore page + Realms page; glossary line "Ascension = the top of the Garden, where the Door opens".
- `Lore Design.md`, `Game Info.md` (lore section and the realm table), `Game Outline.md`, DEVLOG.
- The Story start "The Wandering Immortal's Disciple" (`Tale*` chapters) mentions realms; check wording when building.
- Dialogue/Lessons that name True Immortal as the Door (grep `101`, `True Immortal`, `Seam`).

Cheats: `CheatsMore` "breakthrough/pass" must respect or bypass the leave (decide: cheats bypass, as now).

### 6b. Touch list for B2 (replaces the Level B items above: `State.leave`, `Lore.mayRise`, `liftYearly`)
- `Character` breakthrough code (the three places that print "has broken through to Half-Immortal / Pseudo Immortal / True Immortal", and the Ascension break at Major 7): one call `LoreEndings.realmLeaveRoll(this, newMajor)` after the breakthrough, only when `Lore.active()`.
- `LoreEndings.realmLeaveRoll`: skips seekers (`State.seekers`), Wandering Immortals, `fatedTo` people, humans (they get the window); rolls `Settings.leavePct[realm]`; a leaver below True Immortal: `leavePalace(c)`, chronicle line, `bless(c, smallAmount)`, `c.deathCause = "left the Garden"`, `c.reincarnating = true; c.dead()` as `npcCross` does for "outside"; a True Immortal non-seeker with a free lamp becomes a lamp-bearer (`npcCross`'s soldier branch, factored out).
- `LoreEndings.seekersYearly` / `npcCross`: the Door test stays at True Immortal for seekers (`majorLvl >= 10 && lvl >= 101`), so **seekers' code is not changed**; only the Door for humans moves (section 3).
- `LoreEndings.bless(Character, int amount)`: the amount becomes a parameter (5 for lamp-bearers, `Settings.leaveBless` for others); `Character.blessing` and `inheritedBlessing` already add along the line.
- `Settings`: `leavepct` (four numbers), `leavebless`; `settings` help and the Settings window.
- Human window: `GuiScenes`/`DecisionBroker.ask` at the first breakthrough into Ascension / Half / Pseudo for a human who is not a seeker (Stay / Go through); a seeker is told the Seam is open but "not yet" (text in `LoreText`).
- Probe `CeilingProbe` (renamed `LeaveProbe`): seeker never leaves below 10; non-seeker rolls once per realm (seeded); leavers are not lamp-bearers and `wanderers` is unchanged; descendants of a leaver carry the small blessing; True Immortal non-seeker takes a free lamp; lamps full -> no lamp; human gets the window; settings 0 turns it off. Realm counts after 150 years (BalProbe/SimProbeX) compared with today's.

## 7. Tests and measurements

Existing probes that will change expectations: `LoreProbe` (108 checks: Door thresholds, scene order), `EndFlowProbe` (cross -> Stay/Leave), `TalesProbe` and `StoryJourney` start 10 (Disciple), `ErasedProbe`/`ArcsProbe` (seeker arcs end at the Door), `PotentialProbe` (True Immortal roll), `CheatsProbe`, `HelpCheck`, `FixesProbe` (Rankings).
New probe `CeilingProbe`: (1) a human at Major 7 level 71 can open the Door at any time, level 70 cannot; (2) without leave a Major 7 person fails to break through, with leave passes; (3) Stay grants leave, Leave does not matter; (4) lore off -> no gate; (5) grandfathering (a Major 9 person in an old save keeps rising); (6) the yearly lift picks only Ascension cultivators and never exceeds N; (7) the hidden ending still reachable from a Stayer at 101.
Measurement (Level B): run `tools/BalProbe.java` / `SimProbeX` for 150 years at 100,000 before and after; compare the count at Major 7, 8, 9, 10 per decade; tune N so the 8-10 count is within +-25% of today's.

## 8. Balance risks and mitigations

- *Players leave early and miss the late game.* Leaving is final, so make Stay attractive: the Stayer is the only way to Half-Immortal and above, to the hidden ending, to the Steward powers. Show the choice plainly in the Old Man's scene ("if you go now you go as a recruit").
- *The lamp-bearer ending (3,000 points) becomes cheap.* Scale the bonus by realm at crossing (e.g. 3,000 x level / 101, min 1,500) or add a late bonus for Stay-then-Leave from True Immortal (keep `lore leave` for Stayers, it exists).
- *All nine lamps are filled by early leavers.* They fill faster, which locks the hidden ending sooner. Mitigate: Stayers do not count toward `LAMP_MAX` (Q4) and the lamp cap stays 9.
- *NPC seekers cross at 7 and thin out the Palace.* Their odds already include "stay" (25-45%); keep, and let the Stayer count feed the quota.
- *Quest/story pacing.* Stories gated by Major 8-10 (check Tale* chapters) get easier; fine.

## 9. Migration of old saves (B2: nothing to migrate)

B2 adds no new saved set: the roll happens at the moment of a breakthrough, so characters who are already Half-Immortal or higher in an old save are never rolled (they are not at a new realm). Only `Settings` gains numbers. The older text below describes Level B and is kept for reference.

- `PState.step` indexes stay valid (CHAIN length and order unchanged); the advance loop (`guard < 8`) walks anyone who now qualifies forward on the next turn, queueing the new scenes in order (`p.scenes` already replays missed milestone scenes, Lore.java ~546).
- `State.leave` is created lazily; at first load of a Level B world, every living character at Major >= 8 is added (grandfather).
- A human already past the Door (crossed) is untouched. A human at Major 8-9 not yet crossed keeps rising (grandfathered) and may cross as soon as the Seat is read.
- Settings: new `settings ceiling off|lift|hard` (A / B / C) so the author can change level without a new game; default per the author's answer.

## 10. Build order (B2 with the author's decisions; nothing is built yet)

1. **Settings and the roll**: `Settings.leavePct` (20/15/10/25), `leaveBless` (2), `LoreEndings.realmLeaveRoll`, `bless(c, amount)`; lamp reserve rule; chronicle lines. Probe `LeaveProbe` parts for NPCs.
2. **The Early Door**: `m7` at level 71, `lore door`, button and Quest Log line, `early_door` scene, Stay (reuse), Rear Guard / Early Strike / walk-out with realm weights; Hall of Fame and Roll of the Crossed lines; `lore leave` by realm. Probe parts for humans (EndFlowProbe additions).
3. **Text** (`LoreText`, `LoreEndings`), Help lore page and glossary, Realms page; `Lore Design.md`.
4. **Tuning run**: 150 years at 100,000 (BalProbe / SimProbeX) comparing the number at Major 7, 8, 9, 10 per decade with today's; adjust the percentages; a 60-year MANUAL Palace journey (StoryJourney, start 10) that opens the Early Door and takes each ending.
5. Docs, README, DEVLOG, Outline.

Rough size: 1 and 2 about two days of careful edits with probes, 3 the text (about 1,500 words), 4 a day of runs.

## 11. Questions

Settled by the author: see section 2c (blessing +2/+2, percentages, human seekers, the choice at all times, non-seeker True Immortals, level 71).
Still open (my defaults in brackets, I will use them unless told otherwise):
- **A. Lamp reserve:** non-seekers take a lamp only while fewer than 5 of 9 are carried [yes].
- **B. The Early Door from anywhere** (`lore door`), not only at Originis [yes].
- **C. Rear Guard** for the Good early leaver, and the odds of the Early Strike 85 / 75 / 65% [as written].
- **D. Pseudo Immortal as an "image"** (a reflection that stays behind when such a person dies) [name only, no mechanic].
- **E. Old saves:** nobody is rolled retroactively [yes]; humans already at Major 7+ get the Door at once.
- **F. Lamp-bearer score:** keep 3,000 at the Full Door [yes]; early leavers get a smaller ending bonus (500 Rear Guard, 200 Early Strike, 300 walk-out) [yes].
