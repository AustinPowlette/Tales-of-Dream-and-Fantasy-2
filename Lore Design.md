# Lore design: the Palace of the Oldest Order

Status: implemented (2026-10-06) in `Lore.java` (engine), `LoreSites.java` (the nine places), `LoreText.java` (every word, as data), `GuiLore.java` (scene
pop-ups, Journal). Switch: `settings lore on|off` (default on; a world that began without it: `lore enable`). The story is **open for discussion**: all text
lives in `LoreText.java` and the objectives in `Lore.CHAIN`, so changing the story means editing data, not code.

## The truth (what the player slowly learns)
- The world is a **Garden**: a pocket of one far stronger cultivator's mind, kept inside him like a seed in a fist. Everyone in it was grown out of his dao in
  one morning, aged 18 (the first generation has no past: the Wayhouse ledger).
- Potential is a measure someone took of each of us; the major-realm "locks" are its teeth; the Rope (reincarnation) hands the unfinished another try.
  Heaven-Sent treasures are seeds, the great purge (epoch) is pruning, beasts are whetstones, calamities are blows from beyond the sky that arrive as weather.
- The owner is **losing a war** against the **Hollow Court** ("it does not kill; it puts out"). He wants his Garden's people strong enough to leave and fight.
  The Order is a school that never says what it trains you for. Those who reach the Door (level 101, True Immortal) are *called*, as soldiers.
- **???** is the virtual image of the owner: a reflection left in the Garden to run the school, fond of the people, forbidden to say who he is or why.
  He tells you each time he is not allowed to answer (the one promise he can keep). Endings: cross alone, or carry the Image with you.

## The Palace (a sect with a virtual master)
- `Organization.virtual`: the Palace of the Oldest Order stands at Originis in every new world, with the same name every time. No Character is its master
  (`master == null` for ever, `promote()` never crowns anyone, never at war); everything the master would do is done by `Lore` (yearly ranks and stipends,
  the Convocation, the Nine Lamps). The master line in the sect sheet is "???".
- **Entrance: the Convocation of the First Gate** (the entrance tournament), proclaimed at Originis every New Year, 8 weeks, Major Lvl 0-4 may register
  (`do Register Tournament`). Single elimination by `tempLvl` (Lucky favoured). A third of the field is let in (at most pop/400); the champion gets a weapon and pills.
  Unaffiliated cultivators from every land (up to 1,500) answer the call, not only those standing in Originis. Anyone who crosses Major Lvl 2 after the first Convocation is simply sent for (an invitation), so the way in never closes. NPC winners join; a human may decline. The Palace is never at war, cannot be razed, shows "Master: ???", and its Evil elders still bully, murder, steal and scheme (no coups: there is no master).
- Ranks: Wick of the Outer Court (outer) -> Flame of the Inner Court (inner, Major 2) -> Lamp of the Nine (elder, Major 5 + 200 merit). Nine lamps (Dawn, Patience,
  Salt, Iron, Rain, Ash, Glass, Hunger, Winter); NPC Elders yield a lamp to a human who qualifies.

## The road: seven acts, the same for every human (story or not)
I The Gate and the Bell (register, win a place) / II The Outer Court (find the Unlit Wayhouse, 30 merit, Major 2) / III The Unlit Roads (two of Weaver's Wall,
Cistern of Returning, Assayer's Hall; Major 4) / IV The Inner Court (two of the Gardener's Shed, Thin Sky, Road of Unreturned Lanterns; Major 5 + lamp) /
V The Nine Lamps (Menagerie of Practice and Garrison of the Crossed; Major 8) / VI The Empty Seat (an Elder who has read the nine sits beside the Seat; Major 9) /
VII The Door (Major 10, level 101, then `lore cross` beneath the Empty Seat). Each step is a pinned quest in the human's list, with rewards (pills, gear, potential).
Reincarnation keeps the progress (the story is about the soul, not the body).

## The places (LoreSites)
Nine tiles placed from the world seed (same nine in the same spots every time, chosen by terrain: river, low ground, high ground, the seam where the map wraps, forest,
barren, far edge) plus the Empty Seat at Originis. A tile takes its lore name when created; a clue forces the next tile into being. A human who stands there with the
right realm and having read the places before it learns what it holds (a scene, a clue, a reward). NPCs walk past them.

## ??? speaks
Scenes (pop-up in the GUI, framed block in the terminal; `lore replay <id>` reads one again): the bell, the Convocation, pass/fail/invite, the Inner Hall, one for
each realm milestone (M1, 3, 4, 6, 7, 8, 9, the Door), the Rope, a new face, omens, the nine places, the Empty Seat (the whole truth) and two endings.
Whispers (rare one-liners, at most one a year, tied to the act). Omens: from year 20 the Hollow brushes the Garden now and then (a strange chronicle line in some city).
The game waits while a scene is open (`settings lorepause off` to stop that).

## Open questions for the author
- Should NPCs have the story too (they already join the Palace and may cross at level 101: `Lore.crossNpc`, remembered in the Roll of the Crossed)?
- After the crossing: a new-game-plus (a Crossed character returns as ???'s soldier in a later epoch)? Currently the story ends and the world goes on.
- Hollow Court as an actual world threat (a calamity type that follows the omens) in a later act?
- More tiers of places for realms above 8, side stories per Lamp, a second Palace in other worlds.

## The endings (rewritten 2026-10-02; `LoreEndings.java`)
**Who is who:** the **Gardener** (the Old Man) is the real cultivator of the Outside; the Garden is the inner world inside his mind. **???** is only his *image*, the part of him left to guide the Garden's cultivators; he does not fight and **cannot leave** the Garden. The **Outside army** are cultivators of at least Half-Immortal level, hundreds to thousands of years old (not the Garden's 18-year-olds). The Gardener's path is the **array**: each of its **nine great lamps** needs a strong carrier, and with every lamp lit it can face an army of True Immortals, but there are levels above True Immortal and he has barely scratched them. The Garden exists to grow lamp-bearers. Wandering Immortals have their souls imprinted by the Gardener, so while they are on good terms with him they can return to the Garden. The Hollow Court is never a threat inside the Garden.

**Three beats** (`lore cross` at the open Seam: Major Lvl 10, level 101, the nine places and the Empty Seat read):
1. **The Seam and the Old Man** (`end_threshold`, `end_oldman`): ??? says goodbye (he cannot come), the player steps through and the Old Man explains the war, the array and the nine lamps. Then **Stay** or **Leave**.
2. **The choice.** Stay: the Old Man sends you back to grow soldiers faster; then Good / Evil / Neutral (Evil: take the Palace, destroy it, or neither; Neutral: take it or neither; founding a sect or going solo are just gameplay afterwards). Leave (final): lamp-bearer (Good), strike at the Gardener (Evil) or walk out alone (Neutral).
3. **The aftermath** (a story stub per path, then the funny credits, a Hall of Fame line with a score and a line in the Roll of the Crossed).

| Choice | What happens |
|---|---|
| **Stay, Good: the Steward** | The human becomes the Palace's master (`Organization.virtual` ends). Every year: gifts for up to 200 cultivators below Major 7, up to 40 cruel Evil cultivators culled, bounties on Evil cultivators, Palace members train harder. |
| **Stay, Evil: the Usurper** | Take the Palace (its Good members are killed), raze it (`State.palaceGone`), or go your own way. Opens the *dark path*: each Good cultivator killed adds +1 potential past 100 up to 200; yearly bounties on Good cultivators. |
| **Stay, Neutral: the Idler** | Take the Palace and nap, or go your own way. Lives out their days as the player. |
| **A Stay can be undone** | `lore leave` (or `lore cross`), from anywhere, any time: the Seam opens again and the Leave menu follows; a master gives the Palace back to its image (`undoStay`). Leaving is final. |
| **Leave, Good: lamp-bearer (the true ending)** | Pick a role (front line, beside the young, at the back); the stub is the Old Man's first fight with the array (a bitter fight, many die, it is only another struggle in the Outside). The human is released to the succession dialog and the body returns as a **Wandering Immortal** (cannot die, a pill and a hint every turn). Descendants are blessed: +5 to `tempLvl` and +5 potential per soldier in the line, stacking. Costs one of the nine lamps. |
| **Leave, Evil: strike at the Gardener** | 50/50: the array kills you or spares you (spared: walk the Outside as an Evil wanderer, one of the random branches). Always death once all nine lamps are carried. A death sends the blood back: **every cultivator alive and every one born later gets +5 potential** (`State.blood`, stacking, cap 100). |
| **Leave, Neutral: walk out alone** | One of 12 random Outside branches (good, evil, neutral, mystery, bad: the Free Road, the Small Throne, the Teahouse at the Edge of the War, the City of Mirrors, the Hollow Edge, the Deserters' Camp, the Other Garden, the Ferryman, the Quiet Farm, the Long Climb, the Cold Pass, the Dust of the Road). You may hear of the Old Man or not; you may rise, die, or stay ordinary. |
| **The hidden ending** | Dark path, potential 200, level 101, `lore usurp` at the Empty Seat: you break through the ceiling above True Immortal, go through the Seam and kill the Gardener. The Garden lives inside him, so it goes out: the game saves and ends (`gardenEnds`); the killer gets a random Outside branch; other humans who can cross may step through, the rest fade with the world. Locked once all nine lamps are carried (the array is whole). |
| **The world's end** | When the week counter nears its limit (`LoreEndings.worldEndWeek`) the Gardener dies and the Garden goes with him. Every human who can cross (or already stayed) chooses the Outside (a neutral branch) or to fade with the world (`end_dark`, a poem); the rest simply fade. Then the game saves and ends. |

**The nine lamps** (`LAMP_MAX`): every soldier, human or NPC, carries one (`State.wanderers`). When all nine are carried: the lamp-bearer ending is closed, the hidden ending is locked, and an attack on the Gardener always ends in death. So it pays to stop other seekers from reaching the Seam first.

Score = level, realm, kills, places read, footprints read, years played, wealth, potential, lives lived, plus a bonus per ending (hidden 6,000; lamp-bearer 3,000; Last Lamp 2,500; Steward 1,500; Evil kill attempt 500 ...).

**Seekers** (NPCs the Convocation admits; `State.seekers` = a bit mask of the places read): they read the places in order (35% a year), and at Major 10, level 101 with all nine read they open the Door and choose by alignment (`npcCross`): Good mostly lamp-bearer or stay, Evil mostly strike or stay, Neutral mostly walk out or stay. NPCs who stay (`State.stayers`) give gifts (Good) or cull the Good (Evil) every year and put prices on their enemies (`huntYearly`). The lamp cap and the blood bonus apply to them too.

**The Giant Footprints** (`LoreSites.placeFeet`, six tiles, pure in the world seed): the remnants of battles Hollow Court members and others fought on the inner world of ???: three near the Thin Sky, three near the Garrison. A human at Major 3+ standing on one gets a short clue and a rare relic (a weapon two realms above them with an enchantment), once each. The Hollow Court stays lore: it is never a threat in the world.

The player is never destroyed by the epoch (`GameDriver`: only characters with `player < 1` are marked, and a marked character a human has since taken over is spared).

## Story outline and writing guide (2026-10-07, for the text rewrite)
**Who is who (use these words the same way everywhere):** the **Garden** is this world, kept inside the mind of the **Gardener**, a very strong cultivator. **???** is the Gardener's image: a reflection he left behind to run the **Palace of the Oldest Order** and watch over the people. The **Outside** is the real world, where the Gardener fights a war against the **Hollow Court**, an enemy that "puts out" whole skies. The **Seam** (also called the Door) is the way out, under the **Empty Seat**. **Wicks** are Outer Court disciples; the **Nine Lamps** are the Elders. The **Old Man** met after the Seam is the Gardener himself (never ???, who cannot leave the Garden). His path is the **array**: nine **great lamps**, each carried by a strong **lamp-bearer** (the soldiers); the Outside army are Half-Immortal-or-higher veterans, hundreds to thousands of years old.

**What the player slowly learns, one idea per place:** the Unlit Wayhouse (nobody here was born; we all began on one morning, aged 18) / the Weaver's Wall (the world has no edge: it loops, and it was built) / the Cistern of Returning (the dead are recycled; blood pills are made from them) / the Assayer's Hall (potential is a number someone measured; the Rope is a second try) / the Gardener's Shed (the Heavens have hands: treasures are seeds, the epoch is pruning) / the Thin Sky (something outside keeps hitting the sky; disasters are those hits) / the Road of Unreturned Lanterns (ghosts remember an Outside and name the Hollow Court) / the Menagerie of Practice (beasts are training targets; some wake up on their own) / the Garrison of the Crossed (people who open the Door are called away as soldiers) / the Empty Seat (puts the nine facts together; ??? says what he is and what he wants). The Giant Footprints say: giants fought here; the Hollow Court and others raged war on the Garden.

**Voice:** mostly serious and mysterious, 7th-grade words, sentences under about 15 words, one idea per line, one clear picture instead of stacked metaphors. ??? is gentle, a little lonely and honest; he says "I am not allowed to say" at most once or twice a scene and always gives something useful next. Jokes live in the credits.
