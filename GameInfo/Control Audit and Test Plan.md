# Control Audit and Test Plan (2026-10-13)

Nothing in the game code was changed for this document. Every number below was measured today with throwaway probes (kept in the session scratchpad, not in the repo); the recipes are at the end so they can be rerun. Where something is only read from the code, it says "read".

---

## 1. What the measurements showed

### 1.1 "Go There on NPC targets is broken on manual, works on auto" - found, and it is one cause

12 MANUAL `chase` trials against a walking NPC: the game printed "You caught up with X" **21 times, and 17 of those times X was already one tile away at the next prompt**. AUTO catches the same walker in 3.4 weeks on average, MANUAL needs 10.3, and about 1 chase in 4 never lands inside 40 weeks.

Why (read + traced week by week): a tick is NPC phase (NPCs only *plan* a step) -> people who play -> all steps applied. The MANUAL autopilot (`GameDriver.manualTurn`, `PlayerCommands.stepAuto`) notices it has arrived at the *start* of the following tick, stops the walk, and **that tick ends without a prompt** (the `prompted` flag was false when the tick began). The NPC then takes its planned step. At the next prompt you are one tile behind again. A target that walks every turn at your speed can never be talked to, attacked or `qdo`'d. AUTO does not seem to have the problem because its character acts like an NPC, in the same phase and tick it becomes co-located (measured: 3.4 weeks against 10.3).

Knock-on: every quest or story step that needs you *with a moving NPC at your prompt* (Find Person, Battle Rival/Dislike, `seek <id> talk|court`, story DEFEAT such as Voss, Zhao, Cinder, Brodin, Hexley) is much harder in MANUAL. MEET steps that only need co-location at the start of the tick (`Starts.tick`) do close.

Fix options (not applied): (a) when `stepAuto` finds you co-located, give the prompt in the same tick instead of ending it; (b) let the chase end one tile early and "ask" the target to stop; (c) fated story NPCs stand still while their chapter task is open. (a) is the real fix, (c) is a cheap safety net for stories.

### 1.2 Squads and tag-teaming on the human side - three separate problems

1. **Followers leave a human leader within weeks.** 4 NPC followers, leader MANUAL: with no errands 4 of 4 stay after 1 week, 2 of 4 after 4 weeks, 1 of 4 after 10; with one pending sell errand each, **0 of 4 stay after 1 week**. AUTO leader: same with errands, 4 of 4 after 4 weeks without. Cause (read, `GameDriver.npcTurn`): a follower quits when its top queued quest has a higher priority than the leader's *current quest*; a MANUAL leader has no current quest (priority 0), so any errand wins.
2. **The tag-team fight is not the squad.** `Character.partyBattleCheck` gathers *friends standing in the tile* (relationship >= 1), not `followerIds`; squad members only fight together in `partyBeastHunt` (the `hunt` command and hunt quests).
3. **The human's own fights never use it.** `attack` and `spar` call `battleCheck` (one-on-one); only quest-driven fights reach `partyBattleCheck`. A human cannot call for help or be backed up in a duel they start, and cannot choose who in the party fights first.
Smaller: `invite` ignores `partyCap()` (4, or 12 for a throneless Leader); it is a coin flip with no way to improve the odds; the Squad window cannot reorder, kick or set a role.

### 1.3 Blacksmith story (and every item-count objective) - pacing, not wording

All Blacksmith (and Heir/Alchemist) chapter checks are "make N items": 10, 25, 50, 30, 100 = 215 items. Counting works (every craft path increments `itemsCrafted`). The problem is the rate: an item needs `CRAFT_COST = 500` units **in one quality stack**, a mine/forage gives 10-100 units of a random quality. Measured:
- diligent MANUAL (a `profession` every single week): **10 items took 2.4 years, 40 items 8.3 years** (about 4-5 items per year);
- AUTO Smith: the 8 chapters took **52 years** (50 items = 10 years, 100 items = 16 years).
So "On the tenth night..." and "make ten items" read like a week's work but are 120 weeks of play. This matches "the quest does not trigger when it says it should". On top of that:
- Gating is silent: `years(n)` since the last chapter and `major(m)` hide the next chapter with no Quest Log entry or hint (Smith: Two Letters needs Major 1, The Quartermaster Major 2; Heir "What a Sect Is For" 8 years and Major 4; Disgraced "Elder Zhao" Major 3).
- "Study your father's notes: make thirty items to his designs" counts any thirty items.
- Voss: the text says "type 'attack' while you stand in the same place" - see 1.1.
Fix options: scale the counts down (3/6/10/8/15), or count crafting *actions/material worked* rather than finished items, or lower the stack rule for story-active crafters; show "waiting: 1 year or Major Lvl 2" in the Quest Log for a gated chapter.

### 1.4 Actions that do not report what they did

`explore` on all five dungeon kinds prints only "Explore Dungeon done." (6 tries each). Read, `Character.java` "Explore Dungeon": Lost Ruins pays level^2 x 100 ss **silently**; Martial Trial prints only on success; Haunted Grounds and Celestial Trial cultivate 5 and 20 times silently; +10 sect points silently. `SilenceProbe` only covers button *commands*, not quest *results*, which is why this was never listed. The same check should be run on every quest (section 3, T3).

### 1.5 "Why does saving require me to move?"

The save flag is read at the **top of the tick loop** (`GameDriver` ~line 1115). While you stand at a MANUAL prompt the game thread is inside the tick, so nothing happens until your next command ends the turn. The save is deliberately taken between ticks because the end-of-tick lists (`Map.remCharacters`, `tempCharacters`) are not part of a save. Also: **Save while the game is paused does nothing until you unpause** (the pause loop sits after the save check).
Options: (1) handle `saveRequested` inside the pause loop (trivial, safe); (2) at the prompt: flush the pending lists first, then save (planned steps are `transient`, so the only loss is that the week is replayed on load) - needs a test that a mid-turn save loads cleanly; (3) leave it and show "Saving when this turn ends" in the status bar.

### 1.6 Which menus work while the game is paused (read)

While paused only commands in `PlayerCommands.INFO_VERBS` run (the pop-up readers); everything else a button queues **waits until you unpause**, in order. Windows built directly from `World.read` open fine. Reading all `queueInput` verbs of the GUI:
- Work paused: Stats, Backpack, Techniques, Biography, Journal, Story, Oldest Order, Run stats, Relations, Family, Squad & Sect, Companions, People here, Lineage, Locations, Here, Quest Log, Browse, Market, Manage, Rankings, Overview, Painted map, Chronicle, Digest, Notes, Saves, Settings, Export, Keys, Tutorial, Help; `time`, `route`, `preview`, `library`, `auction`, `goods`, `shops`.
- **Wait until unpaused though they take no game time:** Edit Self (`e`), Titled hundred (`ranking`), Duties / Policies / All on / All off (`duties`, `policy`), `limit`, `spotaction`, Notes add/remove (`note`/`unnote`), Quest Log pin/mute/delete/new (`qp`, `qd`, `mute`, `unmute`, `qnew`), Follow/Unfollow, Script Manager buttons (`script ...`), Manual/Auto/Hybrid and Take control (`stop`, `manual`, `auto`), price changes (`price`), Save / Load / Delete save (`save`, `load`, `delsave`).
- Wait unpaused and *should* (they act): moves, Meditate, Hunt, Explore, Forage, Mine, Profession, Rest, Sell, Buy gear, Use pills, Eat, Drink, Equip, Do top quest, Go there, Talk, Spar, Chase, Recruit, Promote, Found, Join...
(See also 6.9: Titled hundred, Edit Self and the setup commands.) Fix: split INFO_VERBS into "looks" and "setup (no game time)": run both while paused, plus the save flag. Windows opened by the game thread (story scenes, decision prompts, Dialogue windows) cannot open while paused by construction - that is correct.

### 1.7 Scroll speed

73 `JScrollPane`s, only 7 set a unit increment. Text components scroll about 16 px per wheel click; plain panels without an increment scroll about 3 px. Yes it can be raised: one `Settings.scrollSpeed` (1-5) and one installer that walks every new window (the pop-ups already go through `GuiWindows.window`) and sets `unitIncrement` and block increment. About 40 lines. Settings and Tile Info are the two worst: see 6.9.

### 1.8 Sects for specific traits - why you never see them

10,000-person world, 25 years: **from year 5 on, 29-30 of 30 sects were founded by Leaders; trait-only sects: 0.** Reasons: (1) a throneless Leader tries `sectLeadership` every turn from Major Lvl 2, so the global sect cap fills with Leaders in 5 years; everyone else founds only through the rare Found Sect quest; (2) `Organization.rollRules` deliberately skips the craft rule for Leaders (the `case Leader:` list), and the craft rule is always the founder's *own* trait; (3) a rule only restricts, nothing attracts - NPCs do not look for a sect of their trait; (4) the name never says "Smiths' Guild". What is already there: potential floor (35%), level floor (30%), alignment (25%), gender (12%), family name (10%) - 12 of 30 sects had a potential floor, 11 an alignment.
Options: draw the trait rule from the *tile's/region's trait mix* for Leaders too (say 40%), reserve a share of sect slots for non-Leaders, name the sect after its theme, let Join Sect prefer a sect that wants your trait, and give themed sects a small bonus (craft sect: craft speed; Musician hall: concerts pay more).

### 1.9 In-game music against the WAVs

Three measured differences between the offline WAV render (`SongTool.renderFont`) and the game (`MidiOut`, `Ambient.midiLoop`):
1. **Polyphony: 17 of the 70 songs need more than 64 voices at some moment, and the game's synth keeps 64.** Over 64 for: Rice Terraces After Rain 90% of its length (peak 95), Thunder in the Ravine 55% (96), Ashes Before the Gate 45%, Caravan Bells 39%, The Red Tide Rises 32%, Lantern Festival Night 28%, Abacus 19%, Lion Dance 11%. Past the cap the synth has to steal voices that are still sounding (read: released tails first, then the oldest): pad and harp tails and bass notes can vanish, which would sound thin and choppy. (Not yet confirmed by ear: that is what the A/B/C clips are for.) (Mood 0, every layer on = worst case.)
2. **Interpolation:** the WAV uses `sinc`, the game Java's default `linear` (duller, more aliasing on bells, chimes, harp).
3. **Timing:** the game fires notes from an 8 ms `sleep` loop; on this machine the wake-ups are 8.9 ms median / 15.8 ms at the 90th percentile idle, and 46 ms (99th) with all cores busy, worst 237 ms. Notes arrive late and in clumps; the WAV is sample-exact. Default buffer 120 ms, so a heavy tick can underrun (crackle). GC pauses were under 1% of wall time in the 100k playtest.
Clips A (WAV settings), B (game synth), C (game synth + timer jitter) of Rice Terraces and Lion Dance were sent to the user to confirm by ear.
Fix (small, all in `MidiOut`): open the synth through `SoftSynthesizer` with `max polyphony 256`, `interpolation sinc` (or `cubic` if CPU), `latency 250 ms`; schedule each note a fixed look-ahead (say 80 ms) ahead with a timestamp on the `Receiver` instead of "now"; set volumes in finer steps (CC 7 plus CC 39) so fades do not zipper. Measure CPU before and after.

---

## 2. Granular-control audit

Legend: **ok** the human decides; **auto** decided for them; **gap** not possible at all; **MANUAL-only gap** works for NPC/AUTO but a MANUAL human cannot do it.

### 2.1 Who decides what (by area)

| Area | The human decides | Decided for the human / missing | Suggestion |
|---|---|---|---|
| Weekly cultivation | extra `meditate` | **auto** base cultivation every week, sect/vein/bloodline bonuses | design choice; maybe a "rest/work/cultivate" week focus |
| Breakthrough | nothing | **auto** the week the bar fills; tribulation (Major 3/5/7/9) happens at once, failure regresses | "Ready to break through: now / after I prepare" prompt; show gate potential needed |
| Potential Pills | `consume` | **auto** swallows every matching pill; cannot hold Potential Pills for a later gate (now matters: 5 to 30 pills per gate) | `consume <n>`, `consume potential/cultivation`, a "keep for gate N" setting |
| Techniques | learn from a library | **auto** which 4 are equipped (best by potential); cannot choose, swap or train one | Techniques window with equip/train |
| Combat | preview, kill/spare, flee, forbidden technique, accept duel, sparring | **auto** the whole fight; no technique choice, no item/pill use, no retreat mid-fight, no "to submission" | at least: use a pill / quit at X% HP; stance (careful/all-out) |
| Beast hunts | `hunt`, party hunt | **auto** order and who fights | - |
| Squad | invite (50%), accept/decline joiners, leave, disband | **auto** members quit (1.2); no kick, role, order; squad not used in own duels | see 1.2 |
| Friends/marriage | accept/decline friend, `seek court`, `avoid` | **auto marriage at relationship 3, opposite gender only**; no proposal, no refusal, no divorce; **auto** pregnancy/children; no adoption, no names for children | `propose`, `decline`, `divorce`; same-gender marriage; name your child |
| Teaching / learning | `gift` a technique to a child | **MANUAL-only gap:** Teach Disciple, Seek Teacher (learn from a stronger person), Visit Family are ignored as "routine" | `teach <id>`, `study <id>` commands |
| Journeys | `goto`, `goquest`, `chase`, `explore` | **MANUAL-only gap:** Pilgrimage (chance of potential), Sightseeing (learn the map around), Courier Run (income), Meditation Retreat (vein x2 + dual cultivation), Trade Run, Gathering Trip, Market Trip: classified MUNDANE, silently ignored; their rewards exist only for NPC/AUTO | `pilgrimage`, `retreat`, `courier` commands or a "Journeys" tab; or let them appear as optional Quest Log entries |
| Dungeons | `explore` (one click) | **auto** outcome, nothing reported (1.4) | result lines; choose depth/retreat |
| Trade | sell all, buy gear, library, auction bids, `price`, market goods | **auto** `sell` picks the shop and what is "unused"; cannot sell one item to a chosen buyer or to another player | item-level sell/offer |
| Crafting | `craft <kind>`, profession | fine; but item cost/rate (1.3) | - |
| Shops | prices | restock / sell stock only as SHOP duty or quests; no staff, no hours | - |
| City ownership | found, arrays, price | **auto** tax is fixed (a tenth of sales), city growth, who may enter, naming; no laws | tax rate, name, open/closed gates |
| Sect master | rules, recruit, promote, war, peace, alliance | **no expel/kick**, no dues, no assigning missions, no choosing an heir (crisis picks), tournaments only via a quest | `expel`, `heir`, `dues` |
| Sect member | join, resign, mission choice (asked), donate/buy via quests | **auto** promotion, mission pay; cannot challenge for rank | - |
| Wars/alliances | war, peace, alliance | **auto** battles between sects; human cannot send people | - |
| Tournaments | register (duty/quest) | **auto** bracket and fights | - |
| Story | choices in scenes | **auto** gating and pacing (1.3); NPC targets walk away (1.1) | hints for gated chapters; stand-still rule |
| Death | reincarnate, retire | **auto** body, age at rebirth; Reincarnate quest | - |
| Lore/Palace | scene choices, `lore` | **auto** Convocation result, Erased, Hollow Court events | - |
| Pets | tame, feed, release | **auto** their fights, loyalty | - |
| Time | wait, watch, pause | **auto** `watch` stop rules (can be configured: `watch ... for`) | ok |

### 2.2 Per-feature MANUAL-human checklist (to confirm one by one)
For each quest family below: can a MANUAL human (a) learn it exists, (b) start it, (c) finish it, (d) read the result? Known answers: ALERT quests yes via the Quest Log; MUNDANE quests (a) no. List to walk: Forage, Mine, Hunt, Hunt Beast, Hunt Cultivator, Seek Territory, Interact, Sell Technique, Find Person, Buy Gear, Go Sell, Return to Sect, Perform, Visit Family, Courtship, Trade Run, Pilgrimage, Meditation Retreat, Tame Beast, Buy Pills, Attend Auction, Market Trip, Gathering Trip, Seek Teacher, Sightseeing, Courier Run (all MUNDANE); Bounty Hunt, Loot Corpse, Register Tournament, Battle Dislike, Seek Potential, Refine Blood, Study Trip, Get Pregnant, Accept Sect Mission, Donate to Sect, Buy from Sect, Buy Sect Array, Recruit Member/Defector, Propose Alliance, Demand Tribute, Deliver Materials, Donate Array, Sect Raid, Teach Disciple, Found Sect/City, Buy City Array, Entice Merchant, Defend, Hold Tournament, Restock Trip, Sell Stock (duties), and the unclassified ALERT ones (Revenge, Battle Rival/Arch-Enemy, Explore Dungeon, Heaven Treasure, Rob Caravan, Join Sect, Gift Technique, Find Parent's Body, Ghost City Rebirth, Possess Womb, Disperse, Spectate Tournament, Reincarnate, Find Lost Beast, Festival Trade, Deliver Goods).

---

## 3. The full test plan

Why earlier bug passes missed things (observed): probes drive hand-built states; quests were tested on the NPC path; buttons were not enumerated; stories were tested with forced flags and time jumps, never at the pace a person plays; the arrival/ordering problems (1.1) only show in a live multi-tick run. So this plan adds **journey tests** and **contract tests** on top of the unit probes.

### T0 Build gate (every change)
Compile with `--release 8`; smoke test; run the existing probes (EconProbe MoveProbe PolishProbe WorldProbe QuestLifeProbe EndFlowProbe PcProbe PcProbe2 QolProbe RulesProbe StartsProbe TextProbe LifeProbe NewProbe SpecProbe Chaos SimProbeX LoreProbe HelpCheck CompatProbe TermFuzz OnboardProbe PlanProbe PotentialProbe TileProbe TalesProbe ErasedProbe ArcsProbe SongProbe SlimProbe DefendProbe FixesProbe UpdateProbe DialogueProbe SilenceProbe) and the GUI ones with a display (QolGui PcProbeGui MoreGui GuiShots2 DialogueGui).

### T1 Command matrix (about 170 verbs; 131 documented in TermHelp)
For each verb: no argument, valid argument, bad argument, wrong place (no city / no beasts / nobody here), as child, as ghost, dead, each of the 24 traits where it differs, in MANUAL, in AUTO (rejected cleanly?), in HYBRID, while paused, from the GUI button and from the typed command (same output?). Assert: at least one result line; no exception; no state change on refusal; the state change the Help text promises; the turn is used only when it should be (info commands cost none).
Verbs by group: move (w a s d nw ne sw se x stay wait walk goto goquest/gq chase o), quests (q inbox qp qu qd qdo qnew mute unmute mission spotaction idle), look (i stats l places r relationships here people players monsters list/ls limit bag shops city sect party library auction help map companions/beasts techniques/tech compare journal history/chronicle fights/fightlog route digest time/date notes/note/unnote dash/dashboard ranking runstats/run halloffame/fame lineage find story tutorial lore/guide recap family relations preview), people (talk spar attack invite recruit leave disband seek follow/track unfollow/untrack avoid/unavoid gift control), work (forage mine craft/profession/prof meditate explore hunt sell set buygear consume equip unequip install learn make cook brew eat drink poison coat perform/play), money and places (buy price market goods found join resign promote war peace alliance tame release feed retire reincarnate), modes (auto manual/stop script duties policy pause on/off new/next/skip), system (save saves load delsave export settings notices cheat music quit/exit e z f p), and every `settings` key (about 60), `policy` key (kill flee forbidden duel feed squad friends script alerts timeout), `script` sub-command (add list saved save load delete/rm edit/insert move del clear run/start resume stop), `music` sub-command (list play next gen keep drop share volume fonts font engine presets mix song).

### T2 GUI matrix
- 45 windows (alerts auction backpack browse character cheats chronicle city digest duties family goods help here journal keys library locations lorejournal main manage market monsters notes notices paintedmap pastlives people policies preview quests rankings relations run saves scripts sect settings shops squad stats story tutorial world): open running, open **paused**, open with an empty character (child, ghost, new), with a rich character, window resized small, font scale 100/150/200%, high contrast, colorblind map, remembered position; every tab; every button inside (about 120 across Dock, Manage, Squad & Sect, Backpack, Quest Log, People here, Browse, Market, Script Manager, Edit Self, Settings).
- Dock: 4 tabs, 9 move buttons, 13 work buttons with their grey-out conditions and tooltips (Forage without herbs, Sell outside a city, Eat with no food...), Quests (3), People (3), Me (9+6+3), Auto (modes, turns spinner, duties, policies, scripts), World (lists, game, export menu), command box with history.
- Paused sweep (new): with the game paused press every button; classify "acts now / waits / does nothing" and compare with section 1.6. Required: no button silently does nothing.
- Keyboard: every shortcut in the Keys sheet (Ctrl+S, F1, I, R, F, Q, B, K, M, G, X, C, J, N, U, T, V, L, Y, arrows, PgUp/PgDn/Home/End, Space, `.`).
- Map: click select, right-click menu (Go here, Chase, Seek talk/court, Open), zoom/pan, painted map, overview.
- Scroll: wheel on every window type at 3 settings once the speed option exists.

### T3 Quest matrix (about 75 quests)
For each quest x path {NPC, AUTO, MANUAL via `qdo`, MANUAL via `goquest` then `qdo`, HYBRID script} x outcome {success, failure, target gone, interrupted by a fight, load/save in the middle}: assert the **result line is printed** (what changed: ss, cultivation, potential, items, relationships), reward correct, queue pruned, Quest Log entry removed, no duplicate, class (routine/alert/duty) correct. This is where "Explore Dungeon prints nothing" is found; run it as one generic harness over `Quest.name`.

### T4 Story matrix (10 stories x every chapter)
- **Journey test per story:** play at the real pace (MANUAL, duties off, diligent; and AUTO) for 60 years in a 3,000 world; log the year/Major/level each chapter opens and closes; assert every chapter opens within a stated budget (say 12 years between chapters), the Quest Log always shows what the story is waiting for, no chapter blocks on an NPC that walked away, the epilogue is reached.
- Per chapter: gating message, task text matches the real trigger (words vs `check`), reward paid, choice kept in flags, text variants (`?flag=` lines) all shown on some road, replay (`story replay`), death or reincarnation mid-chapter, load of an old save (`v2` flag), Lore on/off.
- Specific: Smith (item counts, Voss attack), Heir (brew counts, Cinder, Orrin roads, Pell), Crane (found sect, Hexley), Disciple (15 kills), Disgraced (age 21, seals), Ghost (witnesses, tea), Lucky (kills, WAIT), Orphan (episodes by place/age), Scion (two shops, city), Tamer (tame, Brodin).

### T5 Lore and endings
Nine places and clues, Convocation (entry tournament vs sect members), every ending and Outside branch, credits, Erased once a century, five seeker arcs, Hollow Court events, wandering immortal, soldier release to a new character, world end week.

### T6 Systems
Potential and pills (done: PotentialProbe), epoch (population/memory/time triggers, realm limit, blessing), tile limit, economy (shops, stock, pools, auctions, library, spoilage and refund), pills (cultivation/potential), sects (rules, mission, politics, coups), cities (levels, owners, treasury), wars/defence, tournaments, rankings and titles, family (marriage, birth, death, inheritance, lineage), reincarnation, taming, life arts (food, wine, poison, instruments), arrays, weather/seasons/festivals, naming, hotspots/disperse, treasure.

### T7 Persistence
Save/load every kind (quick, named, autosave, crash, backup), save mid-prompt (after the fix), save while paused, quit-with-save, old-save compatibility (CompatProbe), version flags, export files, gzip, settings file.

### T8 Performance and memory
100k tick time by phase, heap at years 10/30/70 with `-Xmx3g`, epoch at 70% heap, parallel NPC flag with `Verify`, GUI refresh cost, a 70-year spectator run for exceptions (`PlaytestProbe`, `TileProbe`).

### T9 Audio
SongProbe (lint, 70 songs, similarity); **new:** voice count per song (<= 64 or raise the cap), game-synth vs WAV render, timing jitter, fade zipper, mood switch, engine fallback without a SoundFont, volume settings, music off.

### T10 Text and help
HelpCheck (help pages vs behaviour), every Help screenshot current, tutorial pages, terminal HUD, did-you-mean, hints, recap, notices categories and filters.

### T11 Contract tests (cheap, run always)
1. **No silent action:** every command and every quest completion prints >= 1 line.
2. **No silent tick:** a MANUAL turn that ends without a prompt must say why.
3. **Co-location at the prompt:** after "You caught up with X", X is in your tile at the next prompt.
4. **Paused sweep** (T2).
5. **Squad persistence:** followers of a MANUAL leader are still there after 10 weeks unless they had a reason (message printed).
6. **Story budget:** each chapter opens within its budget at natural pace.
7. **Voice cap:** no song above 64 simultaneous voices (or the synth opened with a higher cap).

### Order of work I would suggest (superseded by 6.18, which adds the second batch)
1. 1.1 co-location fix + contract test (also unblocks story DEFEAT steps and 1.2).
2. Silent results (1.4) with the generic quest harness.
3. Paused-game split and save fixes (1.5, 1.6).
4. Story pacing (1.3) and gating hints.
5. Music cap/interpolation/look-ahead (1.9).
6. Squad rules (1.2).
7. Then the control gaps in 2.1, starting with the MANUAL-only journeys and pills.

---

## 4. Other questions

**Do musicians make songs we can listen to? Yes since 2026-10-15** (option B with option A's idea of the instrument as the lead): twelve concert-only pieces (`Concerts`, `kind: concert`), the musician's instrument leads, pace and ornament by its grade; see `Game Info.md` ("The Musicians' concerts"). Option C (musicians composing from `SongGen`) is not planned.

**Trait ideas:** none. The author does not want new traits; none will be designed or built.

**Which menus need the game unpaused:** section 1.6.

**Scroll sensitivity:** section 1.7.

**Printing results to the Console:** section 1.4 and contract test 1.

---

## 5. How to rerun the measurements

Scratch folder copies of `src/Game` + `tools/probes` with `startingPopulation = 3000` (or 10,000 for the sect run), compiled `--release 8`, run from an empty directory. Probes used today (not in the repo; second batch: `DupProbe`, `TurnProbe`, `MusicRace`, `PrintCensus`): `Cmp` (voices per song, game-vs-WAV clips), `Jit` (timer jitter), `ChaseProbe` (weekly trace and "caught up" count), `SquadProbe` (followers staying), `SectProbe` (sects by rule and founder trait), `DungeonSilence` (what `explore` says), `StoryPace` (a story at natural pace). They can be moved into `tools/probes/` as T11 contract tests when the fixes are approved.

---

## 6. Second batch of requests (2026-10-13, later) - findings, options, tests

Everything below was read from the code or measured; nothing is changed yet. "Measured" items used throwaway probes (section 5 plus `DupProbe`, `TurnProbe`, `MusicRace`, `PrintCensus`).

### 6.1 Per-player default quest priorities (by type, maybe by target)
- Today a quest's priority is fixed where it is created (`new Quest("Go Sell", true, 50, 0, 70, city)`: start priority, rise per week, ceiling) and the queue sorts on it. Pinning (`qp`) overrides one quest, not a type.
- Design: `PlayerControl.prioByType` (name -> base) and `prioByTarget` (name + target id / tile -> base), one map per controlled character, copied in `PlayerControl.inheritFrom` so a reincarnated body keeps it. Nothing is global, so Player 1 never changes Player 2. Apply it at one choke point: `Character.addT/addS/addTarget` when `player == 1` (set `priority` and `maxPriority`, and mark the quest so `maintainQuests` does not clamp it back). NPCs never read the maps.
- UI: a "Priorities" tab in the Quest Log (every quest type, its default, a spinner, a reset), `qprio <type> [@target] <n>` and `qprio reset`, and "Always this priority for this person/place" in the quest row menu.
- Edge cases: QuestLife's interest bending (`QuestLife.pick`) scales priorities for AUTO characters (decide whether an override bypasses it); old saves have no map (null = defaults); a type with several creators ("Find Person" from relationships and from `seek`) must all go through the choke point.
- Tests: override set -> new quest has it; two humans independent; survives reincarnation and save/load; AUTO ordering follows it; reset restores.

### 6.2 After death: speed and the decision menu
- The after-death menu is a `JOptionPane.showOptionDialog` (`GameDriver` ~line 740). Closing it with the X returns -1, which is treated as Spectate; Spectate leaves `tickDelayMs` as it was (0 = as fast as the CPU goes), so the world races.
- Plan: a custom modal `JDialog` with `DO_NOTHING_ON_CLOSE`, the options plus "Quit the game" (offer save-and-quit). On Spectate (or any path that ends with no human) set `tickDelayMs = max(tickDelayMs, 2000)` and say so ("Speed set to 2 s; change it with the Speed buttons"). Terminal path: same default. If the human takes over a body, leave the speed alone.
- Tests: X does nothing; each option works; speed is 2000 after Spectate and unchanged after Take over; the quit path saves or not as chosen.

### 6.3 Rankings: Highest Level and Highest Fighting Level
- Today three boards (`Rankings.BOARDS`): Strongest (`majorLvl*100000 + lvl*1000 + kills`), Richest, Most Kills; only Strongest's thousand are epoch-safe (`epochSafe`).
- Plan: rename Strongest to **Highest Level** (realm, level, kills as the tie-break) and add **Highest Fighting Level** = the fight rating `tempLvl` (level + armour + weapon + enchant + the two techniques, then /5, + blessing). Do NOT call `tempLvlCheck()` for 100,000 people: it calls `equip()` (a side effect, and for a MANUAL player it re-equips). Write a pure `fightRating(c)` that reads the equipped items without equipping, and rank on the value before the `/5` (the divided value has only ~30 distinct numbers, so ties everywhere).
- Needs: titles for the fourth board (three tiers, man/woman) in `TITLES`; a new saved field `rankFight` (old saves read 0); `Rankings.yearly` fills it; `epochSafe` keeps the first 1,000 of both level boards; `places()`, `title()` (best tier across four boards), the `ranking` command, `GuiWindows.rankingsPanel`, the help page and the Titled hundred window show the fourth board. `top()` uses a heap, so the cost stays O(n log 100).
- Tests: `FixesProbe` ranking checks extended; epoch exemption for both; old-save load; title tier ordering; cost of the yearly pass at 100,000.

### 6.4 Music thread (and "two songs after Next")
- Music already has its own thread (`ambient-music`, priority 3). What strains it is the CPU: `Map.tickPool` is a `ForkJoinPool` with one worker per core (20 here) and the cultivate pass uses it **every tick even with `parallel` off**, so for those milliseconds every core is busy and a low-priority 8 ms note loop is not scheduled (measured: 99th percentile wake-up 46 ms, worst 237 ms under load).
- Fix options (combine): raise the thread to `NORM_PRIORITY + 1`; build the pool with `cores - 1` workers; move the composing (`Conductor.advance/bar`: song picking and score expansion arrive in bursts) to a second thread that fills a queue 2-4 s ahead, with the dispatcher only sending timestamped notes (look-ahead); a bigger synth buffer. See also 1.9.
- **Two songs after Next: a cause reproduced, not yet Next itself.** `Ambient.start/stop/apply` is racy. 40 Next clicks never made a second thread, but switching the music off and on six times quickly gave **6 music threads at once and then silence** (the old thread's `finally { running = false }` also kills the new one; an interrupt can be swallowed inside `MidiSystem` calls). Any Settings change that restarts the music (engine, font, on/off, apply) while a thread is still shutting down can leave two engines playing two songs. Fix: a `generation` counter (a thread exits when its number is stale and never writes `running`), `stop()` joins the old thread before `start()` returns, `newPiece()` only sets the flag. Ask the beta tester what they clicked just before (Music on/off, a Settings change, loading a save?). Test: the `MusicRace` probe becomes a regression test (max 1 thread, settled 1).

### 6.5 One window per scene
- Cause (read): `GuiLore.show` opens a **non-modal** window for every call, and the story code calls it separately for the intro, the choice (`GuiChapter.decide`, a different window), the "after" text of a choice (`GuiChapter.scene` again) and the outro. The Palace code (`Lore`, `LoreEndings`) does the same, and a Lore scene and a story scene in the same tick both open. Not yet counted live (needs a GUI run).
- Plan: a `SceneQueue`: scenes are enqueued as **pages**; one window shows them, **Continue** goes to the next page, the last page shows the choice buttons (if any) or Close. A scene queued while a window is open appends to it or waits behind it, so there is never more than one scene window. The pause logic (`openScenes`, `weHoldThePause`) becomes "queue not empty". The terminal path is unchanged; `story replay` uses the same pages.
- Tests: a GUI probe counts open scene windows (always <= 1) for every story's chapters, every Lore scene and the endings; paging; the choice on the last page; pause released at the end; closing with X advances or ends cleanly.

### 6.6 Duplicate quests
- Measured: `seek <id> talk` ten times = **10 identical "Talk To" quests**; `seek ... friend` ten times = 10; `qnew Home 15 15 50` ten times = 10 waypoints. The built-in Find Person paths are already guarded (30 syncs = 1). The beta tester's ten "Find Person" were most likely ten clicks on a Seek button.
- Plan: one `Character.addPlayerQuest(q)` used by `seek`, `qnew`/`createWaypoint`, `do`, the Quest Creation Menu, scripts and every Seek button: if a quest with the same name and target (person id or tile) is in the queue or current, do not add a second; refresh its priority and say "already in your Quest Log - priority raised to N". Also dedupe a human's queue once at load for old saves (`Slim` trims copies for NPCs).
- Tests: the `DupProbe` becomes a contract test; same quest from the GUI and the command; a completed quest can be created again.

### 6.7 Invite to party, and every action that ends your turn
- Today `invite` returns `true` (a turn) on success and on refusal (a coin flip: friends always, others 50% if within one realm).
- If it becomes free: (a) you could retry the coin flip until it succeeds - add a per-person per-week cooldown (like `squadDeclined`) or make acceptance deterministic with a stated reason; (b) `invite` has no cap check (`partyCap()`: 4, or 12 for a throneless Leader) - add one ("your party is full"); (c) scripts are safe (`PlayerScript.MAX_FREE_STEPS`); (d) the NPC is added mid-tick, followers ride from the next NPC phase, nothing breaks; (e) probes that assert `invite` ends the turn (PcProbe / PlanProbe) need updating; (f) AUTO is unaffected. Recommended: free, with the cooldown and the cap.
- **Complete list**, measured with `TurnProbe` and read from the return statements of `PlayerCommands.handle` (`true` = the week passes, `false` = free):
  - **Ends the turn:** every move (`w a s d nw ne sw se`), `x`/`stay`, `wait <n>` (n weeks), `watch`, `goto`, `goquest`, `chase`, `auto`, a running script (many weeks); `meditate`, `forage`, `mine`, `hunt` (**even when it only says the beasts are too strong**), `explore` and `do <quest>` when they work, `profession` and `craft <kind>` (**even when it only says your herbs are too rich for your skill**), `make food|wine|poison`, `cook`, `brew`, `eat`, `drink`, `poison`, `coat`, `perform`, `play`, `tame` (also a failed attempt), `feed` (when the beast eats), `talk`, `spar`, `attack`, `invite`, `sell`, `buygear`, `buy`, `learn`, `gift`, `install`, `consume`, `equip` (**even when nothing changes**), `unequip`, `found`, `join`, `resign`, `recruit`, `war`, `peace`, `alliance`, `reincarnate`, `control`, `qdo`.
  - **Free:** every info command (`i l r here people list ls monsters bag shops city sect party squad library auction market goods help map companions techniques compare journal history fights route digest time dash runstats fame lineage family relations story lore recap notices find preview ranking`), `seek`, `qnew`, `q`/`qp`/`qd`/`mute`/`unmute`, `qdo`/`do` when nothing could be done, `leave`, `disband`, `promote`, `price`, `release`, `avoid`, `follow`/`unfollow`, `note`/`unnote`, `limit`, `set`, `settings`, `duties`, `policy`, `spotaction`, `script` editing, `music`, `save`, `saves`, `e` (edit), `manual`/`stop`, `mission` (the list), `export`, and a refused action in general.
  - **Anomalies worth fixing:** a *refused* `hunt`, `profession`, `equip` still cost a week; `found city/sect` and taking a mission are decisions that cost a week; `disband` is free while `invite` costs.
  - Not commands: a decision prompt does not use a turn; an attack on you, an alert during `watch`/`goto`, a story or Palace scene stops the walk (`stopWalking`).
- Test: a contract table (command -> expected turn cost on success and on refusal) generated from `TurnProbe`.

### 6.8 How squads work now (for the Help text)
1. **Joining.** `invite <n>` (or the People here / Browser button): same tile only; an adult, not a player character, not already in a party; friends (relationship >= 1) always accept, others accept 50% of the time if their relationship is 0 or better and they are within one Major Realm of you. A Leader-trait human also recruits automatically now and then (`tryRallyParty`, asking each time unless `policy squad auto`). `policy squad ask|auto|never` and `policy friends` decide how joiners and friend requests are answered when you are not at the keys.
2. **Size.** 4 members (12 for a Leader without a throne) - enforced for automatic recruiting only.
3. **Living with a squad.** Members on your tile ride with you when you move (`Character.ride/applyRide`). A member NOT on your tile walks toward you. **Each week a member leaves if its top queued quest has a higher priority than your current quest** - a MANUAL player has no current quest, so members with any errand leave within a week (measured: 0 of 4 stayed).
4. **Fighting beasts together.** Only `hunt` and the hunt quests (`partyBeastHunt`): the members on your tile attack ONE beast one after another, strongest first; the beast tires (-2 per fighter); each fighter gets its own kill/cultivation credit. The party only hunts beasts its strongest member could take alone. This is the one way to get people to fight with you.
5. **Fighting people together.** No command does it. `attack` and `spar` are one-on-one. The tag-team (`partyBattleCheck`) is used by quest fights and gathers **friends standing in the tile**, not your squad.
6. **Seeing it.** `party`/`squad`, the Squad & Sect window, `leave`, `disband`.
- Fix plan: members stay while you are MANUAL (compare against your stated intent, not your current quest; leave only after N weeks away or when you dismiss them); `attack` (and duels you accept) call the squad (`followerIds` first, friends second); a Squad window with kick, order and role; cap on invite. Tests: `SquadProbe` as a contract test (members still there after 10 weeks), a squad attack, a squad duel, beasts.

### 6.9 Smaller bugs and clean-ups
- **Scroll speed in Settings and Tile Info:** both wrap a plain `JPanel` (`GuiExtras` ~line 301, `MapViewer.infoScroll`), so the wheel moves about 3 pixels a click. A `Scrollable` panel or `setUnitIncrement(24)` fixes them; the global setting (1.7) fixes all 73.
- **Text not wrapped (Chronicle, Notices, Recaps):** every table goes through `GuiWindows.table()` with a fixed 20 px row and no wrapping renderer, so long events are cut off; the recap is a plain ASCII text area (`Recap.java` builds columns by hand). Fix: one wrapping renderer in `GuiWindows.table()` (a `JTextArea`-based cell renderer, row height from the wrapped text, recomputed on resize), and the recap as an HTML card (`GuiSheets` style) with sections.
- **Titled Hundred only opens unpaused:** the button sends `ranking`, which is not in `INFO_VERBS`; add it (and `note`, `unnote`, `limit`, `spotaction`, `duties`, `policy`, `qprio`, `e`... see 1.6). **Edit Self for the beta tester:** `e` is not an info command either, so it only runs when the game thread is waiting at your prompt. Paused at your prompt it opens for you; paused with the Pause button during a `watch`, `auto` or a running week, it waits until unpaused for the tester.
- **Browser > Cultivators double-click** does nothing (the Browser's own tab, `MapViewer.buildBrowserTab`, has no row-click handler); People here (`GuiCards`) and Search anyone do open the card. **Digest** is an HTML page (`GuiPages`), so "double-click" becomes links on the names (`character:ID` hyperlinks with one `HyperlinkListener`).
- **Chronicle portrait loop (a diagnosis, not reproduced):** the Chronicle opens a portrait on any double-click (`clickCount == 2`) of a row. The portrait is a **non-modal** dialog centred on the main window with its Close button over where the Chronicle table usually is. A user who **double-clicks Close** closes it with the first click, and the second click lands on the table row underneath with a click count of 2 -> a new portrait; double-click Close again -> again. A mouse with a chattering button does the same. Cheap checks: a `java.awt.Robot` test that double-clicks Close; fixes: ignore a row double-click within ~600 ms of a dialog closing, reuse one portrait window per character (bring it to front), open portraits on Enter or a button as well.
- **Friend-request portrait:** it already appears when "Dialogue pop-ups" is on and the request has a spoken line (`GuiDialogue.decide` shows the portrait). It does NOT appear when the setting is off, or on the plain `JOptionPane` fallback (`DecisionBroker.ask` without a speaker line) - squad requests, duel challenges, alliance and tribute offers use that fallback too. Fix: any prompt that names a person gets a portrait card (a small shared `PersonPrompt`), whatever the dialogue setting.
- **Random spot action for humans:** `Random` runs `event()`: one of fight a beast, **interact with a random person in the tile** (can start a fight with an Evil one), profession, or **loot a corpse**. It never walks (walking is the NPC `movement()` wander, which a MANUAL human does not get). So it cannot make you walk, but it can start a fight or loot. Options: rename it "Surprise me" and drop interact/loot, or offer only Meditate, Hunt, Profession. Cheap extra spot actions: Forage, Mine, Rest (just the week's base cultivation), Practise techniques (techniques sharpen, no meditation bonus), Socialise (talk to the friendliest person, never fights), Cook/Brew, Study (library), Tend shop, Guard (join a Defend).

### 6.10 Lists and windows: what exists, what is redundant, how to merge
Buttons by window, from the code as it is now (if your build is older, the exe in `dist/` may differ):
| Window | Buttons |
|---|---|
| Browser > Cultivators / Monsters (`MapViewer.buildBrowserTab`) | Refresh, Set as default limit, Talk, Spar, Attack, Invite, Seek: befriend/talk/court/fight/spar, Copy ID. **No Open or double-click, no Follow, Control, Chase, Preview** |
| People here > Cultivators / Monsters (`GuiCards.peopleTab`) | Open (+ double-click), Talk, Spar, Attack, Invite, Preview fight, Seek: befriend/talk/court/fight/spar, Chase, Follow, Copy ID, Refresh. **No Control, no Set as default limit, Follow only (no Unfollow)** |
| Browser > Search anyone (`GuiExtras.searchTab`) | Open, Follow, Unfollow, Control, Preview, Seek: befriend/fight/spar, Go there, Copy ID, Export CSV. **No Seek: talk/court, no Chase** |
| Browser > Shop items / Backpack | filters + Buy selected / Sell selected (text rows) |
| Browser > Places | filters, Go there, Add waypoint quest |
| Me > Backpack (`GuiMore.backpackWindow`, item cards `GuiItems`) | tabs Gear / Techniques / Companions / Food; Equip, Sell, Consume... on the cards |
| Me > Locations (`GuiSheets.locationsWindow`) | go / route, nicer layout, no search or sort |
| World > Market (`GuiWindows.marketPanel`) | auction, goods, library |

Redundancies and how to merge them, in order of payoff:
1. **Three person lists** (Browser Cultivators/Monsters, People here, Search anyone) plus Relations and Family: one `PersonList` component (columns, filters, sort, double-click -> card, one button bar) used four ways: *Here* (the tile), *Known* (people you have met / relationships), *World* (search anyone), *Family*. One button bar: Open, Talk, Spar, Attack, Invite, Preview, Chase, **Follow/Unfollow as ONE toggle** (in every list and in the card), Control, Seek (a drop-down: talk, befriend, court, spar, fight, **kill**), Copy ID, Export CSV, Set as default limit. Same-tile actions (Talk, Spar, Attack, Invite, courtship) are instant `do`-style actions; the Seek menu is the "wherever they are" version. Browser's Cultivators and Monsters tabs become a thin wrapper around it and People here gets an "Open in Browser" button.
2. **Backpack twice** (Browser text list, Me item cards) and **Shop items** (Browser text list): one *Items* component on the item cards (`GuiItems`) with the Browser's filters and sort in two modes: *Mine* (equip, sell, consume, gift, compare) and *For sale* (a shop or market: buy, with a "Sell to this shop" tab in the same window). `Market` (auctions, goods, library) gets the same Shops tab; the Browser tabs for them go.
3. **Locations twice** (Me > Locations looks better, Browser > Places has search/sort/filters) plus Overview, Painted map and Notes: keep the nice Locations layout, add the Browser's type / distance / text filters and column sort, make it THE Places window (Browser > Places opens it), keep Overview and Painted map as pure maps, merge Notes (bookmarks) in as a "Noted" filter.
4. **"What happened" views** (Chronicle, Digest, Notices, Journal, Recap, the main window log, the History pane): give each a role - *Notices* = things that concern you (with filters); *Chronicle* = the world's record; *Digest* = a summary of a period (links to people and places); *Journal* = your own story; *Recap* = the periodic digest of you. One wrapping list renderer and one "double-click a name -> card" behaviour for all.
5. **Dock duplicates**: "People here" and "Quest Log" appear on several tabs (Move, Me, Auto); keep one home each and make the others links.
6. **Stats** (own sheet) vs the character card (others) vs Edit Self vs Biography: the card and the sheet share a header; Edit Self stays a dialog.
7. **Rankings** window / Titled hundred (text) / Hall of legends (Chronicle tab) / Hall of fame (Run stats): one Rankings window, boards as tabs.
8. **Settings / Duties / Policies**: the control settings (duties, policies, spot action, priorities, breakthrough prompt) in one "Control" window; "Settings" keeps the game and display options.
Tests: a button matrix per merged component (every button, every list kind, same-tile and far target), the paused sweep, screenshots before and after.

### 6.11 Randomize Character
- Edit Self (`GuiEditSelf`, a modal dialog) is also the creation screen for the custom start (`editCharacterInteractive(bete, true)`). Add one **Randomize** button with checkboxes: Name, Skin, Trait, Potential (enabled only when potential can be changed: at creation, or with the Cheater trait), Gender (default off, as asked), Alignment. Names from `Naming` (the NPC engine, matching the gender), skin from the existing tones, trait from the dialog's list (no Beast/Monster), potential from the NPC curve (`Starts` STANDARD_ROLL), alignment weighted like the world. Randomize only fills the fields; Done/Cancel as now (Cancel restores). Terminal: `e` gets an `(R)andomize` line.
- Tests: each checkbox changes only its field; gender stays when off; potential untouched when locked; the portrait repaints; Cancel restores.

### 6.12 How item creation and profession leveling work (read from `Character.alchemy/smithing`, `AmateurCraft`, `Profession`)
- **Skill cap.** `professionLvl` runs 0-100. The best material quality a crafter can use is `skillCap = professionLvl / 10 + 2` (Profession 0-9 -> quality 2; 50 -> 7; 80+ -> 10). Herbs and metals have quality 1-10 (`11 - sqrt(sqrt(rand 1..10000))`: mostly low).
- **Weapons (Blacksmith; clothing/Tailor and arrays/Scribe follow the same pattern).** One item costs `CRAFT_COST = 500` units **of one quality stack**. The smith picks the stack with the highest `min(quality, skillCap)`. The weapon type (sword, saber, staff, spear, fan) is random. Item level = `rand(ml*5) + 1 + ml*5` with `ml = min(quality, skillCap)`: **a random level in the upper half of that quality's realm** (`5*ml+1 ... 10*ml`), capped by `(majorLvl + 1) * 10` and 100. With quality-7 metal at Profession 50 the weapon is level 36-70, never 1. With no 500-stack at all there is a 1-in-300 chance of a random level 1-10 item. So the answer to your question is the second case: **the level is always inside the realm of the material (its upper half)**, limited by skill and by your own realm.
- **Pills (Alchemist).** One action makes up to `professionLvl/10 + 1` pills. Each costs **50 herbs** of one quality and the pill's level **equals the herb's quality exactly** (no randomness). The herb used is the highest quality you can use; with qualities 3 and 5 in the bag it always spends the 5s first. A Potential Pill costs **1,000 Blood** and is made **first, automatically, whenever you hold a Blood stack of 1,000** - there is no alignment check in the code. Blood comes from kills (`potential + lvl` units from each fallen cultivator) and from destroying a city (population/100), which is the real limit; the Refine Blood quest lets a non-alchemist pay an alchemist.
- **Profession leveling.** Each craft gives `professionExp`: weapon `level * 3`, pills `pill level * 10 * pills`. The next level needs `10^(professionLvl/10)` exp (10 at Lv 10, 100 at 20, 100,000 at 50, ten billion at 100). Skill grows fast at first and then crawls; better material gives more exp per item but the cost per item is fixed, so the real brake is gathering (a gather action yields 10-100 units; 500 in one stack was about 12 weeks of play as measured). Amateurs (`AmateurCraft`) pay twice the material for plain quality.
- **A crafting menu is worth it**, since the level follows the material and the only way to choose is to choose the stack and the kind. Menu: the **kind** (weapon type / armour / array / pill type / technique for Innovative), the **material stack** (shown with its quality and what it will make at your skill, e.g. "Lv 36-70"), the **number** (pills: 1..max), the cost, the profession exp, "Craft" and "Craft all of this stack". Alchemists get a Potential Pill row whenever Blood >= 1000. Making something for someone else (a gift, a child) can use a lower stack on purpose.
- **Potential Pills and alignment.** As coded there is no restriction to remove. To add the cost you suggest: each batch is a "Blood deed" that moves alignment toward Evil, with a visible warning in the menu (e.g. a Good crafter becomes Neutral after N pills, Neutral -> Evil after more). Numbers are your call (question 2 below).
- Tests: per trait, the item level range against the formula; the menu preview equals the result; Blood pills first only when chosen in the menu and still first for NPC/AUTO; exp and profession level; alignment drift.

### 6.13 Console prints: what exists and a plan
Measured with `PrintCensus` on a 3,000-person world, with what the GUI prints at every prompt (`settings hud off`): **52 manual weeks = 364 lines, 28,790 characters (7 lines and 550 characters a week)**; one AUTO year = 55 lines.
- About **35% of the manual lines are the prompt dump** (`GameDriver.manualTurn`; the plain branch is used by the GUI because `TermHud.enabled()` is `!guiEnabled && hud`): the whole backpack list, **the whole material (herb/metal) bag**, the whole pill bag, the spouse line and "Name 'Title' is in the Originis at x, y". The terminal status block replaced it only in terminal mode; this is the leftover you remember.
- 14 of 52 weeks print "X has awakened as a Monster! The wilds tremble." (world-wide, not about you); "You meditate: ... Your techniques are at their potential." on every meditate; "[Quest Alert]" and "[Notice]" for the same event (a line and a toast); the Auto status line every 13 weeks; tournaments and cities founded far away.
- Source: 349 `System.out.print*` calls in 25 files (Character 127, GameDriver 80, GameDriver2 18, Life 17, Lore 13, StartMenu 11, SaveManager 10, DecisionBroker 10, PlayerControl 8, Chapters 7...). Leftovers seen: "Error in questViable Spot", `[WARN]` parallel messages, `[GUI] Player now controls`, `[WORLD] Areas | Sects | Cities` banners.
- **Plan.** (1) A census script: run N years in each mode and list every distinct message with a count and the code line that prints it (`PrintCensus` plus a static list of every `println`). (2) Classify every print: *Result* (what your action did - always shown), *Prompt/status* (compact, not a dump per turn), *Notice* (concerns you), *World news* (far away; off by default or a digest), *Story* (always), *System* (save, load, errors), *Debug* (delete or `-Dtdf.debug`). (3) One `Say` class: `Say.result(c, text)`, `Say.world(...)`, `Say.debug(...)` with the existing `player > 0 && skipping <= 0` guard inside, a per-category switch (`settings print world off`) and a preset (Quiet / Normal / Detailed). (4) The GUI prompt shows the one-line status, bags only on `bag`. (5) Every action prints its result (contract test 1). (6) A contract test counts lines per week per mode (budget: a manual week <= 3 lines unless something happened).
- The per-print table (keep / merge / toggle / delete for each of ~350) is the first step of the clean-up; we fill it in together.

### 6.14 Gifts from best friends and family
- Hooks that exist: `Relationship.value` (3 = best friend), `Character.isKin`, `Gift Technique` (parents -> children), `Visit Family`, `Find Person`, the item constructors used by crafting and `Starts.giveGear/givePills`, `Chronicle`/`Notifications`.
- Design: a sampler for each **human** (`player >= 1`): every 4-8 weeks pick one person with value >= 3 or kin (spouse, parent, child, sibling); if the giver is alive and **can afford it from what they hold** (taken from the giver's own bag, or crafted with their own profession, so no items from nothing and the economy stays closed), roll by trait: Gatherer -> herbs (a stack of a quality the receiver can use), Miner -> metal, Alchemist -> a cultivation pill of the receiver's next realm (rarely a Potential Pill if they hold one), Blacksmith -> a weapon, Tailor -> clothing, Scribe -> an array or a scroll, Merchant -> silver, Chef -> food, Drunken -> wine, Poisoner -> poison, Musician -> a performance (a short boost), Innovative -> an enchant, Runner -> news of places, Tamer -> pet food, Leader -> a favour (sect points), Lucky -> a small treasure; everyone else -> silver or a pill. Item level: the receiver's realm (never below it, so it is useable) up to `majorLvl + 1`, never above what the giver can make. It arrives as an event ("Mei sends you a bag of herbs") when the giver is in your tile, or by messenger within N tiles after a delay, and raises the relationship a little.
- NPC-to-NPC gifts are not needed (they would change the economy unseen). Cost at 100,000: only the few humans, nothing for the crowd.
- Guards: a yearly cap per giver, a cooldown, no gift while the receiver is hostile, value limited to a share of the giver's wealth. Tests: item in realm range; the giver loses what the receiver gains; the trait table; cooldown; no gifts from the dead.

### 6.15 Choosing to break through, and a Breakthrough button
- Where it happens: inside `Character.cultivate(mult)`. When the bar fills at the last minor level (`minorLvl >= 11`) the realm check runs **at once**, in the middle of the cultivation pass (which runs in parallel for everyone). If potential is under the gate, 14 times in 15 the attempt fails, `lvl--` and **`inhibitor++` (every failed attempt permanently slows cultivation: divisor `2^inhibitor`)**. Tribulations (Major 3/5/7/9) run in the same call; a failure regresses cultivation.
- Plan: for a person who plays, when the bar fills at the last minor level, **hold** (the bar stays full, `breakthroughReady = true`) instead of resolving, and show the prompt (default "ask and pause") with what matters: potential now / needed, the chance (1 in 15 below the gate, certain otherwise), what a failure costs (inhibitor +1), the risk for a tribulation. Options: **Break through now / Not yet**; on the first prompt a "Don't ask again" button (with a note that it can be turned on again in Settings); the policy then applies (`breakthrough always | ready | never`). The Me tab gets a **Breakthrough** button, dimmed until `breakthroughReady`, and `breakthrough` as a command (a turn). NPCs, AUTO and HYBRID keep today's behaviour unless the policy says otherwise.
- Consequences to decide: a held bar wastes cultivation (store the overflow, or cap at 100%?); a human who never presses stays at the gate (that is the choice; a reminder in the Quest Log); the cultivation pass runs on worker threads, so it only raises the flag and the human phase opens the prompt; save/load of the flag; the Reincarnation rope uses the same gates; True Immortal's 1-in-5 roll happens on the press.
- Tests: hold and ready flags; button states; each policy; a failure costs inhibitor; tribulation still works; AUTO unchanged; save/load while held.

### 6.16 Quest Creation Menu (and Seek: kill)
- Today a human creates quests with `qnew` (waypoints), `seek` (five person kinds), `do <quest>` (immediate), scripts, and buttons scattered over windows.
- Plan: a "New quest" window (a button in the Quest Log, Browser rows, the character card, the map right-click): left a searchable list of **every quest type a human may create**, grouped by what it needs (a person: Find, Talk, Befriend, Court, Spar, Fight, Kill, Gift technique, Propose alliance...; a place: Go, Hunt Beast, Explore Dungeon, Heaven Treasure, Sell, Buy Gear, Join Sect, Found City/Sect, Deliver Materials...; nothing: Meditate, Profession, Forage...), a **target** chooser by ID, name (search), coordinates, the tile selected on the map, a dropdown of known places or relationships, a **priority** (default from the type, or your per-type override, 6.1), pin, repeat, and Create / Create and go. Duplicates are handled by 6.6. Types the game manages itself (story, mission) are listed read-only. It writes through `addPlayerQuest`, so the command line and the menu stay one path.
- **Seek: kill** (new): a pinned hunt-down quest. Pursue until you stand with them and then offer Kill / Spare / Spar (a DecisionBroker prompt, so the fight is your choice); if they flee or leave, re-target their current tile (anyone's tile is visible in Search anyone; for fog of war use the last known tile plus a search radius) with no 150-turn give-up (the Quest Log shows the distance); it ends when they are dead, you cancel, or you spare them. A hunted NPC may flee or hide (a `hunted` flag makes it avoid you, so a chase is a real chase). Settle: does Kill respect the first-year truce, sect halls and defenders, and children (never: `childSafe`)?
- Tests: every type creatable from the menu has a viable target kind; invalid targets are refused with a reason; the same quest through menu and command gives one quest; Seek: kill against a fleeing NPC, a dying target, a target in a sect hall.

### 6.17 Additions to the test plan (T12) and to the contract tests (T11)
**T12 New-request tests**: per-player priorities (6.1), death menu and speed (6.2), four rankings and epoch exemption (6.3), music thread lifecycle and starvation (6.4: thread count, wake-up jitter under load, look-ahead dispatch), one scene window (6.5), no duplicate quests (6.6), the turn-cost table (6.7), squads (6.8), wrapped tables (6.9: a 300-character event at three widths), double-click-through (6.9: Robot), randomize (6.11), the crafting menu equals the formulas (6.12), the print budget (6.13), gifts (6.14), breakthrough hold (6.15), the Quest Creation Menu (6.16).
**Contract tests to add to T11**: 8. turn cost of every command equals the table in 6.7; 9. at most one scene window at a time; 10. no duplicate quest from any creator; 11. print budget per week; 12. every window action works while paused.

### 6.18 Revised order of work (replaces the list at the end of section 3)
1. **DONE 2026-10-13 (see 6.22).** Chase/arrival fix (1.1) + the turn table (6.7) + `addPlayerQuest` dedupe (6.6): one small change set that makes MANUAL play behave, with contract tests for all three.
2. **DONE with defaults 2026-10-13 (see 6.23, 6.24; the author reviews GameInfo/Print Review.md).** Silent results and the print plan (1.4, 6.13): the `Say` class, the prompt dump removed, every action reports.
3. **DONE 2026-10-13 (see 6.24).** Paused / save / info-verb split, Titled hundred, Edit Self (1.5, 1.6, 6.9).
4. **DONE 2026-10-13 (see 6.25).** Music: lifecycle race, priority and pool size, MidiOut settings, look-ahead (1.9, 6.4).
5. **DONE 2026-10-13 (see 6.26).** One scene window, death menu and speed (6.2, 6.5).
6. **DONE 2026-10-13 (see 6.27).** Squads and the invite rule (1.2, 6.7, 6.8).
7. Lists and windows merge (6.9, 6.10), one piece at a time as the author asked: **a. PersonList DONE 2026-10-13 (6.28)**; b. Items (Backpack twice, Shop items) - next; c. Places (Locations twice); d. wrapping tables and the portrait/double-click fixes; e. the remaining merges of 6.10 (what-happened views, dock duplicates, rankings, settings).
8. Control features: priorities, Quest Creation Menu, Seek: kill, breakthrough choice, crafting menu, randomize, rankings (6.1, 6.16, 6.15, 6.12, 6.11, 6.3).
9. Gifts, trait sects, concert music (6.14, 1.8, section 4). DONE 2026-10-14/15 (6.33, 6.34).
10. Story pacing and gating hints (1.3), then the full journey-test run.

### 6.19 Questions for you (each changes what I build)
1. **Fighting level:** the fight rating (level + gear + techniques, the number combat uses)? Should it include companions or squad help?
2. **Potential Pills and alignment:** how strong should the shift be (one batch -> Neutral? ten pills -> Evil?), and does Blood from a self-defence kill count?
3. **Breakthrough prompt:** at every Major realm, or only when your potential is below the gate (the 1-in-15 case)? Should the held bar keep storing cultivation or stop?
4. **Gifts:** delivered only when the giver is in your tile, or by messenger from afar? Only to humans (my suggestion) or between NPCs too?
5. **Squad fights:** when you `attack`, does the squad join automatically (with a "go alone" option), or only when you choose "Call the squad"?
6. **Scene pages:** one page per block of narration, or one per speaker line?
7. **Priority by target:** key it by person id / tile (breaks when the person dies) or by kind of target (any family member, any sect)?
8. **Quest Creation Menu:** every type, or only those a human could do without the game's own conditions (some need a sect, shop or city)?
9. **Seek: kill:** do hunted NPCs know and flee, and can you hunt anyone (sect masters in their hall; children excluded)?
10. **Random spot action:** rename to "Surprise me" without fights and loot, or remove? Which extra spot actions first?
11. **Trait sects:** reserve slots for non-Leader founders (a third?), and should a themed sect give a bonus?
12. **Story pacing:** shrink the item counts, or change what counts as progress (actions rather than finished items)?

### 6.20 The user's answers to 6.19 (2026-10-13) and what each one decides
1. **Fighting level** = (level + gear + techniques) / 5 + companion boost; "basically the number combat uses". The Highest Fighting Level board ranks the pure `fightRating(c)` (6.3) with the companion boost added; no `equip()` side effect.
2. **Potential Pills**: making them turns the maker **Evil**. Blood from self-defence kills is still gained but does **not** shift affiliation. (Today there is no alignment check at all; build the shift where the pill is made, and the Blood-gain code must tell a self-defence kill from a hunt. Open detail for item 8: one pill or one batch, and can an Evil maker ever turn back.)
3. **Breakthrough prompt** at **every** realm, with a warning note when you are below the gate (the 1-in-15 case). The held bar's behaviour was not answered: default = it keeps storing cultivation up to the cap (6.15).
4. **Gifts**: humans only; the giver must be in your tile; **plus a "Give Gift" quest** so an NPC can be asked to bring one (the quest, not a messenger).
5. **Squad**: it joins when you `attack` and `hunt` automatically, with **"Call the squad"** and **"Go alone"** choices.
6. **Scenes**: one page per block of narration.
7. **Priorities**: both keys, by person id / tile and by kind of target (family member, any sect, ...). The kind rule is the default, the id rule overrides it.
8. **Quest Creation Menu**: every type (a human may switch to auto, which will then do the quest).
9. **Seek: kill**: players can hunt **everything**, children and monsters included. NPCs do not know and do not flee. (This conflicts with `Character.childSafe`, the rule that nobody under 18 dies: it must be relaxed for a human-chosen kill only, never for NPCs. Confirm when item 8 is proposed.)
10. **Random spot action** becomes **"Surprise me"**, without fights and loot. New spot quests: talk, spar, forage, mine, explore.
11. **Trait sects**: themed sects may give bonuses (my choice, proposal first); reserve founding slots for non-Leaders.
12. **Story pacing**: the item counts stay. The real complaint: the quest said "Craft 25 items" and fired at Profession Lvl 26 instead. Read in `TaleSmith`: `made(c, st, n)` counts `itemsCrafted - craft0`, where `craft0` is stored when the chapter's `setup` runs, so it is **25 items since the chapter started**, not 25 in total, and the Quest Log shows **no counter** (the user's own guess). Fix: show "made 12 of 25" in the objective line, and say "since this chapter began" in the text. Still to check: that `setup` runs when the chapter is announced and not earlier or later, and whether a stack made by `Life.profession` counts the same as a smithing.

### 6.21 Proposal for change set 1 (written 2026-10-13, awaiting approval; no `.java` edited)
Corrections to what 6.7 and section 7 said, found while reading the code:
- `do <quest>` does **not** add anything to the Quest Log (it builds a throwaway quest with priority 0 and runs it now), so it needs no dedupe. The only two creators of a human's queued quests are `PlayerCommands.seekCommand` and `PlayerControl.createWaypoint` (`qnew`); every Seek button in the windows goes through the `seek` command.
- `profession` is not a refused-turn bug: `Profession.alchemist` says "your herbs are too rich" and then *gathers* (a real action) or returns false when it cannot. Only `hunt` and bare `equip` are false turns.

**(a) The prompt in the tick the walk ends** (`PlayerCommands.stepAuto`, `GameDriver.manualTurn`, `PlayerScript` line 316)
- `stepAuto` returns `boolean`: true when it ended the walk this call (arrived, caught up, target gone, gave up after 150).
- In the chase branch, check **first** whether the target is already in my tile; if so, end the walk at once ("You caught up with X") instead of aiming at where the target steps next. This is the moment the NPC has only *planned* its step (`pendTo`), so it is still here.
- `manualTurn`: the prompt block (HUD, auction notice, command loop) moves into a private method. In the autopilot branch, when `stepAuto` returns true, fall through into that method in the same tick; `prompted` becomes true for the `movedThisTick` rule.
- Effect: a chase ends with you at a prompt **with the target in your tile**, and the NPC steps away only after your action. A plain `goto`/`goquest` no longer wastes the empty week after arriving (one week fewer than today; the timing assertions in PcProbe/PlanProbe move by one).
- `PlayerScript` line 316 ignores the result (a script keeps its own flow); `legacyAutoMove` and `goQuestCommand` call sites unchanged.

**(b) `Character.addPlayerQuest(Quest q)`** (new, in `Character.java`; used by `seekCommand` and `createWaypoint`)
- Looks in `targetQuests`, `spotQuests` and `currentQuest` for the same name (case-insensitive) and the same target (person id or tile). Found: do not add; raise its priority to the larger of the two with `pin`; say "already in your Quest Log (priority N)". Not found: add, as today.
- Once per load, a human's queue loses its exact copies (old saves; `transient boolean dedupedAtLoad` in `PlayerControl` run from `playerTurn`).
- NPC paths (`addT/addS/addTarget`, `syncSocialQuest`) are not touched.

**(c) Turn cost** (`PlayerCommands.handle`)
- `hunt`: returns the result of `partyBeastHunt()` (a refusal is free, the message stays).
- bare `equip`: returns true only when the weapon or the armour changed; otherwise "You already wear your best gear" and no turn. `equip <n>` unchanged.
- `invite`: always free. New: `PlayerControl.inviteRefused` (person id -> week) and a 4-week wait before the same person can be asked again ("X turned you down recently"); a cap check with `squadSize() >= partyCap()` ("your party is full, 4 of 4"). Success and refusal text unchanged. Help text and the People here button tooltip updated.

**Tests** (new probes in `tools/probes/`, run from a scratch copy per its README)
- `ArrivalProbe`: 12 chases against a walking NPC; pass = at least 95% of "You caught up" lines have the target in the player's tile at the next prompt (before: 4 of 21), and a `goto` of 5 tiles gives a prompt after 5 weeks, not 6.
- `DupQuestProbe`: 10x `seek ... talk/friend/fight`, 10x `qnew Home 15 15 50` = one quest each; priority raised; a completed quest can be made again; two humans independent; an old save with copies loads deduplicated.
- `TurnCostProbe`: the 6.7 table as data (command, state, expected cost): refused `hunt`, bare `equip` with nothing to change, refused and successful `invite`, the cooldown and the cap cost 0 weeks; every command that must cost a week still does.
- Docs after the change: DEVLOG, Game Outline, Game Info (chase flow, turn table), CLAUDE.md state line.

### 6.22 Change set 1 as built (2026-10-13)
Approved by the user with these additions: the 4-week wait for `invite` is right; **also free**: `buy`, `learn` (a technique from a library), `gift`, `buygear`, `sell`, `feed` (limited), `coat`, `eat`, `drink`, `consume`, `install`, `join`, `resign`, `recruit`, `war`, `peace`, `alliance`; `play` was left to me. Decisions: **`play` still takes the week** (a Musician's extra cultivation ticks, the audience and tips: free play would be unlimited cultivation), as do `perform`, `meditate`, `craft`; **`feed` is limited** to one portion per companion per 4 weeks (the same rhythm as the AUTO policy; `feed` without a number picks a hungry companion); **a refused `hunt` is free** except for a lone hunter a little under the tile's level, who loses the week "not daring" (the 3-in-4 roll is kept); **bare `equip` costs the week only when the gear changes**, `equip <n>` still does (the user did not list it; ask below).
Built: `PlayerCommands.stepAuto` (returns true when the walk ended; target already in the tile ends a chase first), `GameDriver.manualTurn` + `promptLoop`, `Character.addPlayerQuest` / `dedupePlayerQueue`, `PlayerControl.inviteRefused` / `INVITE_WAIT` / `dedupedAtLoad`, `Character.fedAt` + `Taming.feedWait`, help text (`TermHelp`, `HelpPagesMore`). Measured: chases 7 of 7 end with the target in the tile (was 4 of 21 correct), goto N tiles = N weeks (was N+1), ten identical seeks/qnews = one quest, the 6.7 table as `TurnCostProbe` (48 checks). Regression probes unchanged or better (PcProbe 112/0 where the baseline had one failure).
Left for later (open questions for the user): free `gift` can raise a friendship quickly (each gift is +1; items are used up); `equip <n>` still costs a week; `hunt` at a level a little under the tile's still loses the week on a failed roll; the squad does not yet join `attack`/`hunt` (item 6); the six sect commands have only been run in their refusal paths in `TurnCostProbe` (no sect set-up in the probe).

### 6.23 Answers to the change-set-1 questions, and change set 2 as built (2026-10-13)
User answers: `equip` costs no week (done, bare and `equip <n>`); a gift warms a friendship once per person per week, extra gifts are still handed over (done); a `hunt` where nobody is challenged costs nothing, a fight won or lost costs the week (done: the 3-in-4 "does not dare" roll is now free too); the craft counter ("made 12 of 25 since this chapter began") is wanted (kept for item 10, with the story pacing work).
Built (item 2, first part): the prompt is one line (no bag dump), `Explore Dungeon` reports all five kinds, `do` / `qdo` end with a `Result:` line, wording fixes ("under way", "nothing came of it this time (reason)"), `Loot Corpse` reports. Measured: the same 52 manual weeks went from 365 lines / 26,706 characters to 189 / 14,695; `QuestSilenceProbe` finds 0 quests that change the snapshot in silence (its first run found only the dungeon kinds and a few misleading "nothing happened" lines, fewer than 1.4 feared: the quest handlers of the `do` path were mostly fine; the AUTO / NPC path does not print by design).
**Left of item 2:** (a) the `Say` class with categories and `settings print world|results|debug`, (b) the classification of the ~350 `System.out.print*` calls into Result / Status / Notice / World news / Story / System / Debug (the "we fill it in together" table of 6.13), (c) a toggle for world news (awakenings are already limited to within 3 tiles of a human; city foundings, tournaments and arrays broken far away still print), (d) the duplicate "[Quest Alert]" and "[Notice] New quest alert" lines for one event, (e) the Auto status line every 13 weeks, (f) a wording audit of the quests a human only meets through the Quest Log (`quest()` path) and the story objectives, (g) `Loot Corpse` still answers "done" when there was no corpse.

### 6.24 Change set 3 as built (2026-10-13): saving, paused menus, the print write-up
User answers: finish the print cleanup with the defaults and write it up for review (`GameInfo/Print Review.md`); save at once (yes); every command that takes no time works while paused, and a command that does take time shows a "you are paused" window and still waits in the queue (yes).
Built: see `Game Info.md` ("Saving at the prompt, paused commands, world news"). The save repeats the week when it was made while the game waited for a command (a save made while paused is exact); `PausedProbe` loads such a save and checks the ids, the living count and four more weeks. Item 2's leftovers are done as defaults: `Say.world` and `settings worldnews`, the duplicate alert notice removed, `Loot Corpse` reports. **Not done (waits for the author's review of Print Review.md):** moving the 25 Debug lines behind `-Dtdf.debug`, the 28 "Unguarded: review" rows, and replacing the individual `System.out.println` result lines by `Character.tell` everywhere.

### 6.25 Change set 4 as built (2026-10-13): music and mid-week saves
User answers: the three A/B/C clips sounded the same; the in-game audio is normal again after the application is restarted ("something between the first and the second launch fixes it"); a pool one core smaller is fine; add a note to the save/load screens (repeating a week is **not** free of side effects: everyone ages and cultivates one more week than the calendar, so the note says so).
Reading of the clue: a fresh process starts with exactly one engine, and the old start/stop code could leave several (or none) after Off/On clicks, a Settings change or Next. That is the one thing a restart removes, and it is what the tester's "two songs" looks like. The clips cannot show it (they are offline renders). The fix removes the stacking, makes timing independent of the load (time stamps on the audio clock), and adds `music status` so the next occurrence can be diagnosed in seconds: ask for its output. Built: see `Game Info.md` ("Music engine: life cycle, time stamps, tuning"). Measured: `MusicLifeProbe` 10/10 plain and tuned; 40 busy threads: 0 of 172 notes late, longest loop pause 78 ms.
**Done afterwards (see 6.26):** `dist/` was rebuilt with the flag. If the game is run from the IDE on Java 9+, add the VM option `--add-exports java.desktop/com.sun.media.sound=ALL-UNNAMED`.

### 6.26 Change set 5 as built (2026-10-13): one scene window, the after-death window
User answers: closing a scene with the X takes the default option; "Save and quit" and "Quit" are offered in the after-death window; the author runs the game from IntelliJ and from `dist/` and deletes saves on every update (so the exe may be rebuilt freely).
Built: see `Game Info.md` ("One scene window and the after-death window"). Not covered: the Palace's `Lore` scenes in a live Palace run (the unit tests use the same enqueue path; `LoreProbe` is headless), the DecisionBroker's own decision windows (friend requests, duels, auctions) which stay separate windows because the game waits on them.

A packaging bug found while rebuilding: the packaged exe never found `soundfonts/`, `assets/`, `images/` or `songs/` (they were in `app/`, the game looks next to the exe), so the packaged game played the built-in synthesiser. Fixed in `tools/make-exe.bat` with `--app-content`; verified with `music status` in the rebuilt exe.

### 6.27 Change set 6 as built (2026-10-13): squads
User answers: members follow tile by tile so that everyone is in the leader's tile at the leader's turn; kicking is plain removal; orders as suggested (Follow, Hold here, Fights first / Guard). Guard became **Reserve** (fights last, only if the ones before did not win): a defender role (a squad that fights when an NPC attacks you) was not asked for and is **not built**.
Built: see `Game Info.md` ("Squads of a person who plays"). Not covered: an NPC attacking a person who plays does not call the squad; spars stay one-on-one; a squad member's own errands wait until it leaves (the author may want a 'let them work' order); the AUTO leader keeps the old NPC rule (members with a more pressing errand leave).

### 6.28 Change set 7a as built (2026-10-13): squad follow-ups and the one list of people
User answers: yes to a "let them work" order and to a defending squad; the list merge one piece at a time (the list of people first).
Built: see `Game Info.md` ("Squads, second part", "The one list of people"). Decisions: the Seek menu has Befriend, Talk, Court, Spar, Fight (**Kill comes with the Quest Creation Menu, item 8**); the card and portrait show only for the people in the tile (a distant stranger's equipment is not shown in the Search list); Talk / Spar / Attack / Invite stay same-tile actions and say so for someone elsewhere; the list default limit starts at your `listLimit`. Left from 6.10 point 1: Relations and Family lists are still their own windows (they can use `GuiPersonList` later), and the "Known people" mode.

### 6.29 Change set 7b as built (2026-10-14): Seek: kill, Known people, the Items window
User answers: Seek: kill goes in the Seek menu; Relations and Family move onto the shared list; the "sell only at good prices" idea becomes a toggle inside the Items window; one piece at a time.
Built: see `Game Info.md` ("Seek: kill, Known people, the Items window"). Decisions: Kill is allowed on children and monsters (the plan's "hunt everything"), but never on another player or a person fated to somebody else's story; a Hunt Down fight ignores alignment scruples and the friend rule because the hunter chose it (a plain `attack` still has both). The Items window has no separate Pills / Weapons tabs: the type filter does that.

### 6.30 Change set 7c as built (2026-10-14): the Places window, `attack` and alignment
User answers: a plain `attack` ignores the scruples of alignment but not the friend rule; the Overview and Painted map stay separate windows; noted places get a Noted filter (and gold); no further proposal round for 7c.
Built: see `Game Info.md` ("The Places window and the attack rule"). Decisions: notes are the existing bookmarks (`Chronicle.notes`, key `t:x,y`), so the Notes window and `notes` still list them; the Note button asks for text in a small dialog; Details on a place that is neither city nor sect selects its tile on the map.

### 6.31 Change set 7d as built (2026-10-14): wrapped tables, double-clicks, portraits
User answers: check every window (done by the `WrapTableProbe` sweep); wrap the text; the what-happened views become one window (that is 7e).
Built: see `Game Info.md` ("Wrapped tables, double-clicks and portraits"). Decisions: wrapping is in the one table class so every window gets it; a long cell is drawn as wrapped text only when it does not fit (a fitting cell keeps its renderer); the click-through is stopped at the source (one sheet per person, 600 ms guard) instead of in each window; Saves and the Market's item tables get no double-click on purpose.

### 6.32 Change set 7e-1 as built (2026-10-14): what happened is one window
User answers: the 7e merges one at a time (what-happened first); the Control window replaces the separate Duties and Settings buttons; the Titled hundred should not exist apart from Rankings (the Rankings boards already show the title of each of the hundred, so the button and the text list go).
Built: see `Game Info.md` ("One window for what happened"). Decisions: Notices and Notes stay their own windows; the Hall of legends stays a tab of the Chronicle window until the Rankings merge moves it.

### 6.33 Change sets 7e-2..4, 8, 9 and 10 as built (2026-10-14)
User answers: the dock duplicates one at a time (People here and the Quest Log on the Move tab, links elsewhere); the Hall of legends and the Hall of fame are two tabs of Rankings and the Titled hundred goes (the boards already show the titles); the Control window replaces the Duties and Policies buttons, with the chores, answers and spot action on the first tab and the priorities and the breakthrough prompt on a second; then "do everything you can without asking".
Built: see `Game Info.md` ("The rest of the windows, and the control features"). Decisions I made (the author reviews them): Potential Pills make **everyone** who makes them Evil (the menu, an NPC alchemist's own brewing, refining another's Blood) and nothing turns it back; a held breakthrough keeps the bar at its cap and is free of a week (`breakthrough`); Randomize leaves Gender off; the fourth board ranks `fightRating` (no companions beyond the lasting bonus, no wine or poison); a themed Leader sect is a guild of the craft most practised in its tile (40%, three or more), its members of that craft cultivate 10% faster; a giver's gift comes out of their own holdings, one per 26 weeks; "Surprise me" for a person who plays is a trade turn, a talk or a meditation; the New quest window can make every type except the game's own.
Not done from 6.18: nothing left except what the author finds playing the build. (The new traits were dropped by the author's choice; the concert music was done on 2026-10-15, see 6.34.)

### 6.34 The author's answers to the 6.33 questions, and what they decided (2026-10-15)
- Potential Pills: only the **making** makes someone Evil (not eating the pill, not gaining Blood): as built. Gifts: once per year per giver to the same person (52 weeks); food, wine and poison may be given. Guild bonus +10% cultivation and a Musician hall pays 25% more at concerts. Each pill counts as a crafted item. Concerts: option B with the musician's instrument as the lead (option A's idea). New traits: dropped. T12: done.
- Built: see `Game Info.md` ("The Musicians' concerts"). Test plan T12 (6.17): `PrioProbe` + `T12Probe` (per-player priorities), `DeathMenuProbe`, `RankFightProbe` + `FixesProbe` + `T12Probe` (the boards, the epoch, the cost), `MusicLifeProbe`, `SceneWindowProbe`, `DupQuestProbe` + `QuestAddProbe`, `TurnCostProbe` + `T12Probe` (the turn table), `SquadProbe`, `WrapTableProbe`, `EditRandomProbe`, `CraftMenuProbe` + `T12Probe` (the formulas), `PrintBudgetProbe`, `GiftProbe`, `BreakthroughProbe` + `T12Probe` (what is saved), `ControlGuiProbe`; contract tests 8-12: `TurnCostProbe`, `SceneWindowProbe`, `DupQuestProbe`, `PrintBudgetProbe`, `PausedSweepProbe`. Not run as journeys: T4's 60-year story journeys and T3's 75-quest matrix are still the older probes' (`TalesProbe`, `QuestLifeProbe`, `QuestSim`, `PlaytestProbe` by hand).

---

## 7. How to resume (paste this after compacting)

Read `GameInfo/Control Audit and Test Plan.md` first: the measured findings are in sections 1 and 6, the control audit in 2, the test plan in 3 and 6.17, the order of work in 6.18, the user's answers in 6.20 and 6.23-6.34, and what is already built in 6.22-6.34. Change sets 1-10 and the concerts (all of 6.18) are implemented, tested and documented but **not committed**. The throwaway probes are in the session scratchpad (`many3.sh` builds a 3,000-person copy in `pc2` and runs probes by name; **its compile errors are easy to miss: check that `pc2/out` has the new classes**; GUI probes are run by hand with `java -cp ../out;lib/flatlaf-3.7.2.jar Game.<Probe>` from `pc2/run`). `PcProbe`, `PcProbeGui`, `PlanProbe` and `NewProbe` are flaky on the committed code too: rerun before suspecting a change. Follow CLAUDE.md: propose before editing any `.java`, Java 8 only, no sub-agents, short frequent status updates. The author runs the game from IntelliJ and from `dist/` (rebuild with `tools\make-exe.bat`, saves may be deleted). All of 6.18 is done except what the author wants after playing the build. Ask him what he found; the author said the new traits are not wanted, so do not propose them. Write the proposal first and wait for approval unless the user says to go ahead.

### 6.35 The long runs T3 and T4 and the full sweep (2026-10-18)
Done: **T3** (82 rows through the MANUAL path, `QuestMatrixProbe`), **T4** (the ten Story starts for up to 60 game years, `StoryJourney`, careful bot, 1-3 seeds each) and the whole regression sweep (61 headless + 20 GUI probes). Everything is in `GameInfo/Long Run Results.md` (the tables, the repro steps and the file:line of each cause). Short list of what the runs found (status after the fix round of the same day: 1, 2, 4, 5 and 6 FIXED, 3 left as it is by the author's choice; see `Long Run Results.md`, section "Fix round"):
1. Eight quests made by hand (Buy City Array, Buy Sect Array, Buy from Sect, Donate to Sect, Entice Merchant, Hold / Register / Spectate Tournament) are only waypoints: `PlayerControl.java:210` (`SPOT_TYPES`).
2. A friend's battle quest draws a MANUAL human into the fight with no prompt (`Character.partyBattleCheck`, `Character.java:5868`): several runs died in "Tag-team assault" rounds against an arch-enemy at ~0%.
3. Chapter gating: the Scion's "The Salt Road" (two shops) cannot be done before Major 2 (`maxShops = max(1, major)`, `Character.java:4859`; `TaleScion.java`); the Orphan's "The Seventh Star" needs three of five random clue episodes (`TaleOrphan.java:65`) and did not open in 60 years in 3 of 3 runs; the Heir's last chapter and the Disciple's / Ghost's middle chapters wait for a Major level that took 15-37 years.
4. Silent results: Bounty Hunt by hand, Deliver Goods, Festival Trade, Disperse, Attend Auction, Get Pregnant, Recruit Defector, Hunt Cultivator, and `consume` of a Potential Pill (`Character.java` consumePills).
5. Refine Blood: the refusal quotes the wrong rule (the alchemist's skill caps the pills, not the 2,000 Blood).
6. Poison: a rich character is poisoned and dies in 13 weeks unless he eats food of the poison's level; the only warning is one log line.
