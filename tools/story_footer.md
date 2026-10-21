
---

# Part 3: Menus, prompts and credits

## The choices the game asks

**At the Seam** (`lore cross`, at Originis, level 101, everything read):
1. *"The Seam is open. Step through and meet the one who made the Garden?"* Step through / Not yet
2. After the Old Man: *"The Gardener waits. What do you do?"* Stay in the Garden and grow its soldiers / Leave the Garden / Not yet

**Stay:** *"How will you grow the soldiers?"* As the Steward: make everyone stronger (Good) / By taking what the world has (Evil) / By living as you please (Neutral) / Back
- Evil: *"What becomes of the Palace?"* Take it and make it yours / Destroy it / Neither: go your own way (a sect of your own, or alone) / Back
- Neutral: *"How do you live?"* Take the Palace and nap on its throne / Leave the Palace be and go your own way / Back

**Leave:** *"How do you leave the Garden?"* Become a lamp-bearer in the Gardener's array (Good), or "(closed: all nine lamps are carried)" / Strike at the Gardener (Evil) / Walk out alone and find your own road (Neutral) / Back
- Lamp-bearer: *"How do you carry your lamp?"* At the front, where the fighting is worst / Beside the young ones, to keep them alive / At the back, to keep the light steady

**After a Stay:** `lore leave` (from anywhere, any time) opens the Leave menu again. Leaving is final.

**At the Garden's end** (the last week of the world, or the hidden ending), for those who can cross: *"The Seam stands open and the lamps are going out. Step through?"* Step through and see the Outside / Stay with the Garden until the light is gone

**Joining the Palace** (a human already in another sect): *"The Palace of the Oldest Order has let you in. Leave [sect name] and join it? (You keep your pass: 'lore join' later.)"* Yes / No

Messages printed at other moments:
- "The Seam waits. It has waited this long."
- "The Seam opens for you, as the Gardener promised." (`lore leave`)
- "You raise your hand against the Gardener..."
- "The Gardener shakes his head. All nine great lamps are carried; there is no lamp left for you."
- "The Seat is at Originis." / "Nothing answers." / "The Seat is warm. It is not warm enough. (Potential N, level N.)"
- "The Seat is cold. All nine great lamps are carried now, and the Gardener's array is whole. Nothing you are can break it."
- "[Lore] The hunger takes what the good leave behind: potential N." (every 25 kills on the dark path)
- "=== The world has reached its last week. The Gardener is gone, and the Garden goes with him. ===" / "=== The Gardener is dead. The Garden, which lived inside him, goes out. ==="
- Chronicle: "[name] fell on the Gardener's array. Their blood ran back into the Garden: every cultivator, now and to come, has +5 potential (N in all)."
- Chronicle: "A Usurper's bounties on the Good / a Steward's bounties on the cruel: N new prices were posted on the boards this year."

## The Usurper's "How" line (inserted as the second line of `end_usurper`)

- **Take the Palace:** You take the Palace the way a tide takes a beach. The Elders who object are removed. Those who stay learn to smile. The Nine Lamps burn red.
- **Destroy it:** You tear down the Palace. It takes eleven days, because the walls keep trying to be kind to you. At the end, the bell lies on its side in the rubble. It rings once when you kick it.
- **Neither:** You walk out of the Palace like a man leaving a coat behind. Your sect, or your solitude, will have your name on the door and nobody's lamp in it.

## Credits tails (the last lines before "No spirit stones were harmed...")

| Ending | Tail lines |
|---|---|
| Steward | The Palace is under new management. The management says the lamps are fine. / Epilogue: a grateful wick has named a noodle dish after you. It is mostly water. |
| Usurper | No good cultivators were harmed in the making of this credit. A great many were harmed afterwards. / Spoiler: the Seat is cold. |
| Idler | The protagonist is currently napping. Please leave a message. / Fun fact: this ending has the lowest body count and the highest tea count. |
| Lamp-bearer | TALES OF DREAM AND FANTASY 3: THE OUTSIDE / [name] and the Gardener's army fight on. Coming soon. / (The Gardener has said 'soon' for ten thousand years. Do not hold your breath. Do hold your lamp.) / Lamps carried: N of 9 (the array is whole). / N descendants were blessed: +5 strength and +5 potential for every soldier in the line. / A Wandering Immortal now walks the Garden. They cannot die. They leave gifts. They are not sorry about the gifts. |
| Struck down by the array | The attempt on the Gardener was rated 'brave'. The result was rated 'a short credit'. / Your blood went home: +5 potential for everyone in the Garden, for good (N so far). / Memorial lamp: lit. Memorial lamp: out. |
| Walk out (any Outside branch) | Branch of the Outside: [title] ([good/evil/neutral/mystery/bad]). / There are more branches. The Outside is large, and nobody mapped it. |
| The Garden's end, stepped through | Branch of the Outside (as above). |
| The Garden's end, stayed to the dark | The world did not end. It was simply finished. You are still in it. Please remain seated. / No lamps were harmed. All of them are out. (Those who could not cross: "The Garden went out with all its lamps. You were one of them.") |
| Hidden ending | SECRET ENDING UNLOCKED: you were warned. Several times. In several languages. One of them was a sneeze. / Outside: nobody noticed. The war continues. Please do not tell the Hollow Court; they would only be sympathetic. / Special thanks: ??? (lamp, formerly image, pending paperwork). |

Every credit scroll opens with *TALES OF DREAM AND FANTASY 2 / A Garden Production / Starring [name] as Themselves, Mostly / and ??? as ???*, then the ending and score, then 18 random funny lines from the list in Part 2, a stat line, the Roll of the Crossed (the last six), the tail above, and *No spirit stones were harmed in the making of this credit. All of them were harmed before. THE END (or is it?)*.

## Scores (bonus added to level, realm, kills, places, wealth and potential)

Hidden ending 6,000 (+ half the branch bonus) / lamp-bearer 3,000 / the dark at the Garden's end 2,500 (500 if you could not cross) / Steward 1,500 / Usurper 1,200 / Idler 800 / strike and killed 500 / Outside branches 400 to 2,100.

## How NPCs make the same choices

An NPC seeker who opens the Door chooses by alignment: Good mostly lamp-bearer (45%) or stays (30%); Evil mostly strikes (40%) or stays (25%); Neutral mostly walks out or stays. NPCs who stay give gifts (Good) or cull the Good (Evil) each year. If the lamps are full, no NPC becomes a lamp-bearer, and a strike is always fatal.

---

# End of the script

When you have decided what to change, give me a list (a scene id plus a note is enough) and I will apply it in one pass, then regenerate this file so it always matches the game.
