# Seasons work log (2026-10-19, written while the author slept)

The author asked for all six steps of "Suggested order of work" in `Start Stories Seasons Plan.md`, using their `Start Story Opinions.txt`, with these decisions:
pacing is mine to choose; 6-10 chapters and 5,000+ words per season; existing saves do not matter; seasons may change genre; a final season touches the Palace/Door lore only where it comes easily (Season 4/5 are fine where a story wants them); the dead may be ghost clients; **all** pilots (every story); romanceable characters are always the opposite gender of the player.

## What exists (engine, `Chapters.java`)
- `Def.season(n, title)` starts a season; the chapters declared after it belong to it. `Def.interlude(n, title, lines...)` is the scene when season n ends and another follows (the old epilogue of season 1 became the interlude). `Def.epilogue(...)` plays only after the last season.
- Pacing rules (the author left them to me): `Chapters.PATIENCE` = 6 years: a Major-level gate (`major(m)`) or a `soft(...)` wish lapses once the gap is over and 6 more years have passed; `REALM_WAIVER` = 8 years: a "reach Major N" task (N >= 2) is waived after 8 years. `Chapter.needs(text)` / `soft(cond, text)` give the Story window words for what a chapter waits for (`Chapters.waiting`). `story` and the Story window show season headings and the wait.
- `Cast.opposite(maleFirstName)`: a person to love takes the other gender than the human's (`Chapters.isFemale/firstName`); text tokens `{key.he} {key.He} {key.him} {key.his} {key.hers} {key.man} {key.boy} {key.lad} {key.sib}`; `Cast.keep()` = +60 years of life for companions. `Chapters.echo` keeps flags `e.female e.male e.spouse e.kids e.sect e.master e.city e.shops e.beasts e.kills` fresh before every scene (text can test them: `?e.spouse|`). `Chapters.wed/bond/sectSize/ownsCity` helpers.
- Tools: `tools/story_lint.py` (hard bans fail, watchlist per 1,000 words; `tools/story_lint_allow.txt` lets through lines the author asked for), `TalesProbe` (lint, Heir on four roads then seasons 2-3, every story played to its epilogue on 12 roads).

## Done (each: text in `Tale<Name>Seasons.java`, probe green)
All ten stories are in (S1 repaired, S2 and S3 written, then expanded twice with extra chapters): 26 to 39 chapters and 10,500 to 14,900 words a story, about 4,300 to 7,100 words (counted in the script file, scaffolding included) and 9 to 13 chapters a season. The probe is green (214 checks).
- [x] Heir: S1 edits as the author corrected them; Wen's gender follows the player (Wen can no longer die in S1); S2 "The Silent Tonic" (8), S3 "What the Cauldron Is For" (8).
- [x] Smith: S1 revamped (commissions of 3-6 named pieces, Pia was Number Three in Voss's forge, Tam, Marl, the clock), S2 "Evenfall's Edge" (9: the Salt Oath ward that only a blade made for nothing passes), S3 "The War Forge" (8: a thousand blades for people met by name).
- [x] Tamer: S1 "The Fox" (the fox is named by the player and is a real companion kept among the story's people; Moss at the end), S2 "The Storm Crane" (Quill, a trial), S3 "The Burned Wood" (Ember, the orchard, the seventh note). Season 4 "The Long Sleep" is only planned.
- [x] Ghost: S2 "Dead Letters" (real people of the world are the clients: `Chapters.adopt`, `Chapters.deadKin`), S3 "The Door" (the Cistern, the Long Night, Meng).
- [x] Lucky Fool: S2 "The Department of Misfortune" (intern Wilbur Hex, the team), S3 "The Clerical Error" (the luck was a gap in the books; the hearing before ???).
- [x] Scion: S1 reworked (more mystery: the wax; the sealed envelope is opened when the traitor is known and holds the father's real secret; Linden Kessel to love), S2 "The Fall of House Kessel", S3 "Ten Thousand Vaults" (the city).
- [x] Orphan: S1 gets "One More Town" before the death, a build-up for "The Empty Wagon" and the last kettle; Sol is of the other gender; S2 "The Six Other Stars" (six tales in six modes, Ilan in every one), S3 "The Road Home".
- [x] Disgraced: S1 gets "A Cat on the Wrapper" (Mei, of the other gender, afraid of the dark), S2 "The Pillar Fund" (a whodunit about the Concord of Healers), S3 "The Last Page" (Madam Gloam, the guest, harmonizes the three ways the scroll came).
- [x] Crane: S1 dialogue rewritten, the clan is people (Poll, Marta, Pilcher and Verdict), the hum is a motif; S2 "Stillspring" (labour dread), S3 "The Crane's Flight" (the First Crane, the tea as a lullaby).
- [x] Disciple: S1 untouched but for the gaps and the interlude; S2 "The Fourth Move" (Nettle/Brook, five fables, Cup Passed), S3 "Tell the Quiet Man" (the Master was the boy who could not sleep).

## To do
- Season 4 of the Tamer ("The Long Sleep"). Seasons 2 and 3 now reach about 5,000 words each (sixth round done 2026-10-20); Season 1 of Ghost, Lucky and the Disciple stays short on purpose.
- A real playthrough in the GUI (the season windows, the Story window's season headings and wait text, and the first scene of a later season) and a long `StoryJourney` run over three seasons: only the headless probes were run.
- Reading pass by the author: mark what to cut, what to rewrite (a reject log would make the lint better).

## Done after the drafts
- `StoryDump` reads the seasons (`Start Stories Script.md` is 830 KB), docs updated (Game Info, Outline, DEVLOG, README, CLAUDE.md, Seasons Plan section 8), `Start Stories Beat Sheets.md`, `Story Voice Cards.md`, `StoryCountProbe` C4b changed for the Smith's new small counts, the regression probes `StartsProbe` 30, `StoryCountProbe` 10, `EndFlowProbe` 8, `LoreProbe` 121, `ArcsProbe` 29, `TechProbe` 42, `HelpCheck` all green; the smoke test (`n x w`) is clean.

## Conventions used in the new text
Scene lines are paragraphs (2-5 sentences), mixed sentence length; hard bans at zero; `@Speaker|` lines; flags declared with `d.flags(...)`; every chapter ends differently; romance is a thread, never a gate.


## 2026-10-20: eight new starts and the lore audit
- Lore audit against the ten Season 3s: four seams fixed (Cistern under the Palace, the Seam at a field, "born on the blanks", the Master's plain speech), two scenes added (Under-Kitchen, the Hands), `Lore Design.md` records what the stories add.
- New starts, three seasons each, all linted and run through `TalesProbe` (254/254): chef, poisoner, tailor, inventor, musician, miner, cheater, forager. Beat table in `Start Stories Beat Sheets.md`.
- Tool: `tools/story_autofix.py`. Names changed to avoid cross-story repeats (Musician's Clarice Quill, Poisoner's Lucien Fell).
- Still open: the author's reading pass over the eight; a GUI playthrough of the season windows; Season 4+ (see the recommendations in the 2026-10-20 reply: a short shared "Season 4: The Door" for every start from Major 7, plus full extra seasons for a few).


## 2026-10-20: the Door and the extra seasons
- Built the shared Door season (all 18) and eight extra seasons (see the Beat Sheets). `TalesProbe` 254/254, `SeasonsProbe` 23/23 after raising the generic player to Major 8.
- Still open: a GUI playthrough of a Door season, a long journey run past Ascension (the careful bot dies young), and the author's reading pass over everything written since 2026-10-19.
