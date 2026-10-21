# Story Script: Tales of Dream and Fantasy 2

This file collects **every piece of story text in the game**, in the order it is told, so you can read it and mark what to change. It was generated from the source on the date of the last DEVLOG entry that mentions it (`tools/StoryDump.java` rebuilds Part 2 from the code; Parts 1 and 3 are hand-written in `tools/story_header.md` and `tools/story_footer.md`).

1. **Part 1:** who is who, the lore as settled, and the flow of the endings.
2. **Part 2:** the text of everything (scenes, clues, whispers, omens, places, footprints, endings, the Outside branches, hints, credits jokes).
3. **Part 3:** the menus and prompts, the credit tails, and the rules that change the endings.

**Status of the text:** all story text now follows the new lore and a plain 7th-grade voice (2026-10-02). `tools/ReadingLevel.java` checks it: no sentence is over 25 words (it was 27 before the rewrite). Tell me what to keep, cut or change.

---

# Part 1: Who is who, and how it ends

| Who | What they are | Where |
|---|---|---|
| **The Gardener** (the "Old Man") | The real cultivator of the Outside. The Garden is the inner world inside his mind. His path is the **array**: nine great lamps, each carried by a strong cultivator, and with all of them lit it can face an army of True Immortals. There are levels above True Immortal, and he has barely scratched them. He is leading a war against the Hollow Court. | The Outside. Players meet him only after the Seam. |
| **???** | An **image** of the Gardener: the part of him that stayed, fond of the people he made. He runs the Palace of the Oldest Order and guides the Garden's cultivators. He does **not** fight, and he **cannot leave** the Garden. | The Garden only. |
| **The Hollow Court** | The enemy: they do not kill, they put out whole skies. Never a threat inside the Garden. The Giant Footprints are old scars of battles that spilled into it. | The Outside, beyond the dark. |
| **The Outside army** | Cultivators of at least Half-Immortal level, hundreds to thousands of years old. | The Outside. |
| **Lamp-bearers** | Cultivators of the Garden who cross and carry one of the nine great lamps (humans and NPCs). Only nine exist. They come back to the Garden as **Wandering Immortals**: their souls are imprinted by the Gardener, so while they are on good terms with him they can return home. | The Outside, and the Garden as wanderers. |

## The ending flow (three beats, the same for humans and NPCs)

1. **The Seam and the Old Man.** ??? says goodbye (he cannot come). The player steps through and the Old Man explains the war, the array and the nine lamps. Then: **Stay** or **Leave**.
2. **The choice.**
   - **Stay** (the Old Man sends you back to grow soldiers faster; it can be undone any time with `lore leave`): **Good** (the Steward), **Evil** (the Usurper: take the Palace, destroy it, or neither), **Neutral** (the Idler: take the Palace or neither).
   - **Leave** (final): **Good** = become a lamp-bearer (a role: front, beside the young, at the back); **Evil** = strike at the Gardener; **Neutral** = walk out alone.
3. **The aftermath.**
   - Lamp-bearer: the Old Man's first fight with the array. Bitter, many die, only another struggle in the Outside.
   - Strike: 50/50 the array kills you or spares you. Killed: your blood goes back to the Garden, **+5 potential for everyone in it, now and for every birth after, stacking**. Spared: you walk the Outside like the Neutral leavers.
   - Walk out: one of twelve random lives (strong, dead, ordinary, weak; you may hear of the Old Man or not).
   - Stay: a short stub, then you play on as the player. When the Gardener dies at the world's last week the Garden goes with him: if you could cross you choose the Outside (a Neutral branch) or fading with the world (a poem); if not, you fade.
   - Hidden ending (Evil path, potential 200): you kill the Gardener. The Garden lives inside him, so it goes out. You end in a Neutral Outside life.

**The nine-lamp rule:** once all nine great lamps are carried, the lamp-bearer ending is closed, the hidden ending is locked (the array is too strong), and a strike at the Gardener always ends in death. NPCs choose too, so it pays to stop other seekers from reaching the Seam first.

---

