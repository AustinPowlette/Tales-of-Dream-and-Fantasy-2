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

# Part 2: Everything the story says

Placeholders in curly braces are filled in by the game: {name} full name, {first} first name, {rank} the player's Palace rank title, {year}, {round}, {lamp}, {convo}, {how}. Lines in italics with a `>` are narration; lines starting with **???:** are spoken by ???; **OLD MAN:** is the Gardener, seen only Outside.

## Names of ranks, lamps and acts

Palace name: **Palace of the Oldest Order**. Voice: **???**.

Acts: I. The Gate and the Bell / II. The Outer Court / III. The Unlit Roads / IV. The Inner Court / V. The Nine Lamps / VI. The Empty Seat / VII. The Door

Rank titles: Outer Court = **Wick of the Outer Court**, Inner Court = **Flame of the Inner Court**, Elder = **Lamp of the Nine**

The Nine Lamps: Dawn, Patience, Salt, Iron, Rain, Ash, Glass, Hunger, Winter

## The road: the 15 objectives every human follows

| # | Act | Kind | What the game tells the player |
|---|---|---|---|
| 1 | I. The Gate and the Bell | REGISTER | Present yourself at Originis and register for the Convocation of the First Gate ('do Register Tournament' there). |
| 2 | I. The Gate and the Bell | ADMIT | Win a place in the Palace of the Oldest Order. |
| 3 | II. The Outer Court | SITE | Find the Unlit Wayhouse the Order spoke of. |
| 4 | II. The Outer Court | POINTS | Earn 30 merit for the Order: sect missions, donations, delving ruins. |
| 5 | II. The Outer Court | MAJOR | Cross the second gate: reach Major Lvl 2 (Core Formation). The Order will take you into the Inner Court. |
| 6 | III. The Unlit Roads | SITES | Read two of these three places: the Weaver's Wall, the Cistern of Returning, the Assayer's Hall. |
| 7 | III. The Unlit Roads | MAJOR | Reach Major Lvl 4 (Nascent Soul). |
| 8 | IV. The Inner Court | SITES | Read two of these three places: the Gardener's Shed, the Thin Sky, the Road of Unreturned Lanterns. |
| 9 | IV. The Inner Court | ELDER | Take a lamp among the Nine: reach Major Lvl 5 (Ensoulment) with 200 merit. |
| 10 | V. The Nine Lamps | SITES | Read both of the far places: the Menagerie of Practice and the Garrison of the Crossed. |
| 11 | V. The Nine Lamps | MAJOR | Reach Major Lvl 8 (Half-Immortal). |
| 12 | VI. The Empty Seat | SEAT | Go to the Empty Seat at Originis (an Elder who has read the nine places). |
| 13 | VI. The Empty Seat | MAJOR | Reach Major Lvl 9 (Pseudo Immortal). |
| 14 | VII. The Door | MAJOR | Open the Door: reach Major Lvl 10, level 101 (True Immortal). |
| 15 | VII. The Door | CROSS | Go through the Seam under the Empty Seat ('lore cross' at Originis) and meet the Gardener. |

## The nine places and the Empty Seat

| Place | Looks like | Where | Needs Major Lvl | Reward when read |
|---|---|---|---|---|
| The Unlit Wayhouse | a roadside inn nobody lights | on the low ground by a river, a short walk from Originis | 0 | 2 Qi Cultivation Pills and 150 spirit stones from the keeper's strongbox |
| The Weaver's Wall | a wall that wraps around the land | at the edge of the map, where it wraps back on itself | 1 | the footprints teach your stride: movement technique improved |
| The Cistern of Returning | a flooded vault of stone basins | in low, wet ground | 2 | a Potential Pill, from the largest basin |
| The Assayer's Hall | a hall with a giant balance and nine locks | on high ground | 2 | the balance re-weighs you (your potential is shown, failed breakthroughs weigh less) |
| The Gardener's Shed | a shed of strange tools | deep in a forest | 3 | a seed of understanding: martial technique improved |
| The Thin Sky | a mountain peak under a cracked sky | on the highest ground of the far country | 4 | ten weeks of insight under the crack |
| The Road of Unreturned Lanterns | a road the dead still walk | in the barren wastes | 4 | a Potential Pill, pressed into your hand by a ghost |
| The Menagerie of Practice | a round hall of numbered cells | in the far country | 5 | gear from an unnumbered cell |
| The Garrison of the Crossed | an empty fortress, as if an army just left | at the edge of the world | 6 | the Crossed leave you their lamp-oil: potential +1 |
| The Empty Seat | the seat at the end of the hall in Originis | Originis (an Elder who has read the nine) | 9+ | potential +1 |

## What each place teaches (the clue list in the Journal)

- **The Ledger of Arrivals**: None of us was born. Everyone in the first generation appeared on the same morning, aged eighteen, with no past.
- **The Weaver's Wall**: The world has no edge. Walk far enough in one direction and you come back. It is small, and it was built.
- **The Cistern of Returning**: The dead are poured back into the world. Blood pills are made from them. Nothing leaves.
- **The Assayer's Balance**: Potential is a number someone measured. The Rope gives a second try to those who fall short.
- **The Gardener's Tools**: The Heavens have hands. Heaven-Sent treasures are seeds, and the great purge is pruning. The world has a gardener.
- **The Hairline in the Sky**: Something outside keeps striking the sky. Calamities are those blows, reaching us as weather.
- **The Road of Unreturned Lanterns**: Ghosts remember an Outside, and they name an enemy: the Hollow Court, which does not kill but puts out.
- **The Stray's Note**: Beasts are practice targets. Some wake up on their own. Nobody planned that.
- **The Roll of the Crossed**: Those who open the Door are called away to carry a lamp in the Gardener's array. It is not heaven. It is a muster.
- **The Empty Seat**: This is a Garden inside one mind. ??? is the Gardener's image and cannot leave. The Seam leads Outside, to the Gardener.

## Entrance and the Convocation

*Plays when the player first reaches Originis, registers for the Convocation of the First Gate, passes or fails, is invited, or becomes an Inner Court disciple.*

### `bell`: The Bell

> *Originis, the first morning. A hundred thousand people are awake who were not awake yesterday. None of them finds that strange.*

> *In the center of the city stands a palace. You have no memory of walking past it. Its doors are open, and a bell is ringing. It has been ringing for as long as you can remember.*

> *A voice answers the bell. It is quiet. It seems to come from an empty seat at the far end of the hall.*

**???:** Welcome to Originis. I am glad you came.

**???:** This is the Palace of the Oldest Order. We keep a lamp lit for anyone who wants to learn what lamps are for. Each New Year, the First Gate opens. Anyone who stands in the courtyard may try the Convocation.

**???:** I would tell you who I am. But I am not allowed to. You may call me what the others do: ???.

**???:** Ask me anything. I will answer what I can. Each time I cannot, I will tell you so. That is the one promise I am able to keep.

### `register`: The Ledger

**???:** Another name for the ledger: {name}. It is a good one.

**???:** Do not be afraid. The Convocation is not cruel. It is only honest, and that is hard enough.

**???:** Win, or lose slowly, or lose quickly. I will remember you either way. I remember everyone.

### `pass`: The Doors Swing Inward

> *The courtyard is quiet. There are no judges. You understand now that the result was simply known. The great doors of the Palace swing inward.*

**???:** {name}. The Outer Court. We call you a wick, because the first thing a lamp needs is something to burn.

**???:** Here is what I can tell you. The Order teaches cultivation. Cultivation makes you stronger than you are. Everything else is the long way around.

**???:** There is a Wayhouse on the road not far from here. Nobody lights it. Go and see why. Take your time, and bring your questions.

### `fail`: The Bell, Once

> *The bell rings one low note, for you and for everyone who fell in round {round}.*

**???:** You lost. That is all right. Now you know what losing feels like, and you will not mistake it for the end.

**???:** The Gate opens again every New Year. And here is something that is not a secret: the Palace notices anyone who crosses the second gate. A Core Formation cultivator is never turned away.

**???:** Go and grow. I will be here. I am always here.

### `invite`: A Lantern With No Flame

> *A figure in grey stands exactly where you stopped walking. They hold a paper lantern with nothing lit inside it.*

**???:** You crossed the second gate without us. The stubborn ones always do.

**???:** The Order does not need a tournament to know your strength. Come in, {first}. You will join the Outer Court. We will not make you do the entrance again.

**???:** There is a Wayhouse on the road not far from here. Nobody lights it. Go and see why.

### `inner`: The Hall of the Nine Lamps

> *The Hall is larger on the inside. Nine lamps hang unlit in a ring from a ceiling you cannot see. In the middle of the ring is a plain seat. Nobody sits in it.*

**???:** Core Formation. This is where a person stops being a pile of habits and becomes a shape. I am proud of you. I had nothing to do with it.

**???:** The Inner Court. We call you a flame. You may now read the Inner Archive. It has one rule: it never lies, and it never explains.

**???:** You have seen the Wayhouse ledger and the question it asks. Take that question to the Roads. When you have two answers, come back. I will tell you what I am allowed to say.


## Realm milestones

*Plays the first time the player reaches that realm (and has joined the Palace).*

### `m1`: The First Lock

> *Something small happens in your chest. A latch you never knew about lets go.*

**???:** Foundation. The first lock. You opened it yourself. I only watched, very quietly.

**???:** There are nine more. Not all of them are locks. I will tell you which are which when I can. For now: well done. Eat something.

### `m3`: A Road I Cannot Follow

> *For a moment you see yourself from across the room. The person you see looks tired, and pleased.*

**???:** Soul Wandering. Part of you now travels a road I cannot follow. I find that I mind. That is a strange thing for me to feel.

**???:** Send word if it finds anything. A postcard would do.

### `m4`: What I Am

**???:** Nascent Soul. You have asked me what I am. I told you I was not allowed to say. That was true then.

**???:** Now I can say this much. I am an image. I am like a reflection left standing in a house after its owner has gone out. I answer the door. I keep the lamps. I am not him.

**???:** I cannot tell you who he is. I wish I could.

### `elder`: A Lamp Kindles

> *In the Hall of the Nine Lamps, the Lamp of {lamp} has been dark since before the first morning. As you step under it, it lights. It makes no sound.*

**???:** {rank}. There are nine seats. None was lit before this world began. So the only ones who may be jealous of you are the other eight.

**???:** I have a confession. The Order does not choose its Elders. They are chosen by... I would say 'by him'. But you would ask who, and I would have to go quiet. I am tired of going quiet around you.

**???:** Walk the Roads. Find the places that are on no map. Come back when you have read nine of them. There is a seat I want you to see.

### `m6`: Nihility

**???:** Nihility. A realm named for the part of the world that is not there. You will find there are more such parts than anyone told you.

**???:** I do not say this to scare you. I say it because the first time I noticed one, I sat down.

### `m7`: The Faces at This Height

> *From up here the Garden looks small. It is not small in size. It is small the way a hand looks small when you notice it is holding something.*

**???:** Ascension. Look at how few of you there are at this height. Look at their faces. They are the ones who will be asked.

**???:** I cannot say asked what. Look at the faces anyway.

### `m8`: Half-Immortal

**???:** Half-Immortal. Which half, do you think? I have wondered. I think it is the half that remembers being asked.

**???:** The Seat is waiting. It has waited longer than the bell has rung. It can wait a little more, but not much.

### `m9`: The Last Lock

**???:** Pseudo Immortal. The last lock is only called a lock because somebody had to call it something.

**???:** It is a door. It opens one time in five. It does not care who you are or how you have lived. Nothing I know how to give you will help. I am telling you now so you do not hear it from the door.

**???:** If it refuses you, you fall back one step and grow slower. Get up. Please. I have watched you fall before, and I have started counting.

### `door`: The Door

> *The Door opens the way a held breath ends.*

**???:** True Immortal. Level one hundred and one. I have practiced saying that for a very long time. It sounds smaller than I thought it would.

**???:** You may notice you cannot quite die now. You stand up again. That is not a gift. It is a symptom. You are no longer entirely here.

**???:** One thing is left: the Seam, under the Seat. When you are ready, and not before, type 'lore cross'. I will not push.


## Life events

*Rebirth, a new body, and the omens.*

### `rope`: The Rope

> *The world turns inside out and back again, and you are eighteen.*

**???:** The Rope pulled. I felt it go. I did not do it, and I could not stop it. I am sorry for both.

**???:** Nobody asked if you wanted another try. Nobody asks. But you carry more of him now than before. That was the point. The first time I saw it, I thought it was cruel. I have begun to think it was only impatient.

### `another_face`: A New Face

> *You wake in someone else's hands. They are not as steady as yours were.*

**???:** A new face. Do not worry. I know you by your walk.

**???:** The Order keeps your place. A lamp does not stop being a lamp because it was carried into another room.

### `omen`: The Lanterns

> *All at once, across the whole city, every lantern goes out. A breath later, every lantern is lit again. Nobody touched them.*

**???:** That was not the wind. Please do not look for what it was. I am asking as myself, not as the Order.


## The nine places (shown when the player reads a place)

*Plays once, on the turn after the player reads the place.*

### `site_wayhouse`: The Unlit Wayhouse

> *Past the last tree on the road stands the Wayhouse. Its shutters are closed and its lamp is unlit. The sign is down. The door is not locked. The only footprints in the dust are yours.*

> *A ledger lies open on the counter, bound in grey cloth. Every entry has the same date. Every guest arrived on the same morning. Every guest wrote the same age: eighteen.*

> *In the column headed 'Last Home', every line has the same mark: a dash. In the margin, the keeper wrote: 'I was told to keep the lamp unlit until someone asked why. Nobody has asked.'*

> *You count the lines. There is one for you.*

**???:** I wish you had found that in a hundred years instead. Please do not count the others.

**???:** (You are counting the others.)

### `site_weaver`: The Weaver's Wall

> *A wall of grey stone crosses the land from one horizon to the other. The land on both sides is exactly the same: the same hill, the same stream, the same tree leaning at the same angle.*

> *Footprints are cut into the stone at eye height. Hundreds of them walk into the wall from the west and out the other side to the east. Beside the last pair, in the hand from the Wayhouse ledger, is written: 'Went a long way west. Arrived. See above.'*

> *You follow the wall for an hour. It does not end. You turn around and follow it back. It does not begin. Whoever built this did not hide the stitching. They only trusted that nobody would walk far enough to look.*

**???:** Please do not walk far to the west. You will arrive. That would be a disappointment for both of us.

### `site_cistern`: The Cistern of Returning

> *Steps lead down into a vault that smells of iron and rain. A hundred basins stand in rows. Each is fed by a clay pipe from above, and each is labeled in a neat hand with a name and two dates.*

> *You know some of the names. Some are on the Palace rolls. Some are in the Chronicle. One is a woman who died last winter of a fever. You have met her son.*

> *In the largest basin, something dark and slow turns, sweet as honey. The plaque says: NOTHING LEAVES. Under it, scratched with a nail: 'They said the pills were made from blood. They did not say whose.'*

**???:** It is not as bad as it looks. No. It is worse, and I am sorry. But it is also kind. Nothing here is thrown away.

### `site_assayer`: The Assayer's Hall

> *A tall white hall. In the center stands a balance taller than a pagoda, with empty pans. A hundred brass weights hang on the walls, numbered one to one hundred, beside nine bronze locks that have been left open.*

> *A path is worn into the floor by something that walked round and round. At the middle of the path, a number is chalked. It is yours. You do not remember being weighed. The chalk is fresh.*

> *A sign by the balance reads: 'POTENTIAL IS NOT NATURE. IT IS A MEASURE, AND WHOEVER TOOK IT WAS NOT YOU. THE LOCKS ARE THE MEASURE'S TEETH. THE ROPE GIVES A SECOND TRY TO THOSE WHO FALL SHORT.'*

> *The balance dips toward you, politely, and settles. Something that was pressing on you lifts, and you can read your own number.*

**???:** They weigh everyone. It is not meant unkindly. A gardener counts his seedlings.

### `site_shed`: The Gardener's Shed

> *In the middle of a wood stands a long, low shed. Its door is on the latch. Inside, on pegs, hang tools that fit no hand you know. There are shears as long as a man, a sieve the size of a table, and a watering can with no bottom.*

> *On a shelf lie small paper packets stamped HEAVEN-SENT, with a year. Most are empty. One is full of seeds the color of the technique scrolls people find in the wilds.*

> *A ledger lists, in neat columns: 'Pruning: one in ten of each tier, when the crowd grows too thick. Rain: as needed. Fire: rarely. Seeds: every third year.' The shears are worn at the tips. Whoever used them did it for a very long time, and did not enjoy it.*

> *You take one seed. It is warm. In your palm it turns into something you almost understand.*

**???:** The Heavens have hands. I hoped you would never need to see them. I do not like the shears either.

### `site_thinsky`: The Thin Sky

> *The path climbs until the air is thin and the stars come out in daylight. At the top the sky has a crack in it. It is thin and white, like one hair on a dark coat, and it runs from one edge of the world to the other.*

> *It is not a flaw. It hums. Every few breaths, something strikes it from the far side: a slow, patient blow, heavy enough to feel in your teeth. Where each blow lands, the crack flares. Far below, a storm breaks over someone's fields, or a sickness walks into a town, or the sky catches fire. By the time it reaches the ground, it is only weather.*

> *Someone has cut a tally into the rock by the cairn. It runs past the foot of the cairn and down into the dirt.*

**???:** Do not stand under it. It would make no difference. I say it anyway.

**???:** This is what I am for. I keep the lamps lit while the wall holds. It is an old wall, and I am a much newer thing.

### `site_lanternroad`: The Road of Unreturned Lanterns

> *A road of grey earth, where nothing has ever grown, runs through the wasteland. Lanterns hang along it from poles older than the road. They are lit. They have been lit for far longer than there have been people to light them.*

> *Pale figures walk the road, slowly, one behind another, carrying nothing. One turns as you pass. It walks like your mother, or your teacher, or somebody. It speaks without a voice: 'We were not poured back. We remember.'*

> *Farther on, another whispers a word in no language you know, and you understand it anyway: 'Hollow. The Hollow Court. It does not kill. It puts out.'*

**???:** They say it like a name. I was never allowed to say it. Now you have heard it from the ones who remember, and I will not pretend I did not know.

### `site_menagerie`: The Menagerie of Practice

> *A great round hall of pale stone, like a drum, with a hundred cells under its dome. Each cell has a number, a trough, and a door that has been opened from the inside.*

> *The beasts of the wilds are made here, or something like made. The floor is stamped with their tiers, from one to a hundred. A tally over each door counts how many were hunted and how many remain. The counts are balanced. They have been balanced for a very long time.*

> *In the center is a small old door marked with one sigil and no number. It is open. Something walked out of it and did not come back. In the dust in front are a hoofprint, a clawprint and a human footprint, one on top of another. A note is pinned beside it: 'STRAY. Not planned. Not removed. Watch.'*

**???:** The beasts were never meant to think. Some do. I do not know if that means the Garden is waking up or growing ill. I would like it to be the first.

### `site_garrison`: The Garrison of the Crossed

> *A fortress stands on a bare plain. The gates are open, the walls are whole, and the banners have faded to no color at all. No dead lie here. The beds are made. The lamps are trimmed. The armories are stripped to the racks. A whole army stood up and left.*

> *In the muster hall, a long stone is carved with names in a hundred different hands. THE CROSSED, it says at the top. Under the oldest hand: 'Went through the Door. They called me. It is not heaven. It is a muster.' Under that, in a hand you do not know: 'They told us we would see him. We have not. We were each given a lamp to carry.'*

> *The last name on the stone is the nickname that stories tell of: 'The Wandering Immortal'. Beside it is a small drawing of a gourd. There is room below it for a great many more names.*

**???:** Do not read that last part twice. I did, once. I kept the lamps lit for a long time afterward.


## The Empty Seat (the truth)

*Plays when an Elder who has read the nine places sits beside the Empty Seat.*

### `seat`: The Empty Seat

> *The Empty Seat is not empty. It is only not occupied. You can feel the difference, like the pressure in a room someone has only just left.*

**???:** Sit beside it, not on it. Nobody sits there. It is being held.

**???:** You have read nine places. You know most of it already. Let me say it in order, plainly.

**???:** One. This is not the Outside. It is a Garden: a world kept inside one man's mind, the way a seed is kept in a fist.

**???:** Two. That man is the Gardener. He made everyone here in a single morning, aged eighteen, out of his own dao. That is why none of us was born, and why the world loops. It is only as big as he made it.

**???:** Three. He is very old, and he is fighting a war against the Hollow Court. They do not kill. They put out whole skies. The blows of that war reach us as weather, through the crack in the Thin Sky.

**???:** Four. His way of cultivation is the array. He builds patterns of lamps, and each lamp needs a strong person to carry it. He made the Garden to grow people strong enough to carry one.

**???:** Five. Everything here serves that. The Gates are his tests. The Rope is his second try. The beasts are his practice. The Heavens are his hands, and the epochs are his pruning. The dead are poured back, so nothing is wasted.

**???:** Six. I am his image. I am what he left behind to run the school and to care for you. I am not him. I cannot fight, and I cannot leave the Garden. I keep a lamp for each of you.

**???:** When you open the Door at True Immortal, you will meet him. You may carry a lamp for him. You may come back here and help grow more lamps. You may say no. I cannot go with you. I ask only this: be strong, and be kind on the way. He forgot to teach that.


## The Giant Footprints (six optional places)

Three near the Thin Sky, three near the Garrison. Standing on one at Major Lvl 3+ shows the clue once and gives a rare relic.

- **The Giant's Heel**: A footprint wider than a courtyard, pressed into stone that never wears away. Something taller than the sky stood here and was shoved backward. A trench shows where its heel dragged. At the bottom lies a flake of cold white metal.
- **The Glass Hand**: The next print is shallower. The giant was already tired. The ground around it has melted into glass, in the shape of a hand that came down from the sky.
- **The Toothed Print**: Something with too many teeth bit the rim of this print. The bite marks are shaped like lamps. The sky above is a little paler than the sky around it.
- **The Muster Print**: Thousands of soldiers' bootprints run toward a giant's footprint and stop at its edge. None of them goes around.
- **Where the Giant Knelt**: A kneeling print: two knees and a palm, pressed deep. A spear stands upright in the stone beside it, and the shaft is warm.
- **The Last Print**: The last footprint points away from the Garrison, toward the Door. It is the only one filled with ordinary rain.

## Whispers (one line, now and then, once the player has met the Palace)

The number is the earliest act in which the line can appear (99 = after the story ended; 98 = only on the Evil dark path).

- [1] Somebody lit the first lamp. I sometimes wonder if they minded.
- [1] The bell always rings the same note. I tune it every night. Nobody notices. I am happy anyway.
- [1] You walk fast for someone with such a long road.
- [1] I counted the people in Originis again. I stopped at a hundred thousand. I always stop there. I think I am afraid of the next number.
- [1] Eat something. Cultivators forget. It is the one thing I am sure I would do better.
- [2] If a place on the road feels unfinished, it may be. Do not tell the road.
- [2] You are doing well. I am not allowed to say at what.
- [2] Someone swept the Hall again. Nobody is paid to. I think it is the dust.
- [3] The Roads are longer than they look. That is not a metaphor. I measured.
- [3] I dreamed of a house with many rooms. In each room someone was waiting to be told they could leave. I do not dream. So.
- [3] Do not trust a sky that is too clean. It is a clean one tonight.
- [4] I remember the first morning. A hundred thousand first words. I do not know what else I was made to remember. I never forget those.
- [4] Some nights the lanterns all flicker at once. It is not the wind. Please do not blow on them.
- [4] Do you feel like someone is standing just behind you, making sure you are all right? That is probably me. Sorry.
- [5] The Nine are very quiet this season. They are always quiet. I only notice when I am lonely.
- [5] The ghosts say a word I am not allowed to say. I say it in my head. It does not help.
- [6] You are stronger than I was told you would be. I was told very little. It is still a comfort.
- [6] Something struck the Wall again. It took the long way around, to be gentle. I wish it would not come at all.
- [7] I give you the better chair now. It is not in the rules. I have decided to be a bad employee.
- [7] Do you want to know the strangest thing about being an image? I get tired for him.
- [7] There are fewer stars each year. I have started naming them, to keep them.
- [7] Nearly. Nearly. I am not allowed to hurry you. I am only allowed to say "nearly".
- [98] The Seat is warm. It is warm for the wrong reason.
- [98] I counted the ones who stopped hoping. It was a short count. Then it was not.
- [98] You are strong now. I would like it to be for something.
- [99] A new bell today. Someone else is ringing it.
- [99] Do you remember the Wayhouse? I think about it. Someone should light the lamp.
- [99] You left the door open. The Garden is quieter, and a little brighter. I do not know which of those I did.

## Omens (a strange line in the world chronicle, from year 20 on; {place} is a city or Originis)

- On Lantern Night every lantern in {place} went out in one breath and lit again with no hand to light them.
- A bell rang over {place}. Nobody there owned a bell.
- For the length of a sigh, rain fell upward over {place}.
- A thin white line crossed the afternoon sky above {place} for the count of a hundred. Those who looked straight at it forgot what they had been doing.
- A well at {place} showed the wrong faces for one night: faces older than the people looking in.
- Every clock in {place} stopped at the same moment. Then they all agreed on a new time that none of them could explain.
- At {place} the stars came out an hour early, and some of them did not come out at all.
- A child at {place} was heard counting aloud, in a voice that was not hers, from one to a hundred thousand and no farther.

## The endings

*In order: the Seam (end_threshold), the Old Man (end_oldman...), then the ending scenes, then the credits.*

### `end_threshold`: The Seam Asks

> *The Empty Seat is warm. Under it, the Seam stands open. It looks like a crack of pale light in the floor.*

**???:** I have told you everything I am allowed to tell. What you do now is your choice. I will not push you.

**???:** Past the Seam is the Outside. The one who made this Garden lives there. He is waiting for you.

**???:** I cannot go with you. I am only his image. I belong to the Garden, the way a painting belongs to a room.

**???:** Please be careful. And eat something first.

### `end_oldman`: The Outside

> *You step into the Seam. The light is cold. Then it is gone, and you are standing on a plain of grey ash, under a sky with no edge.*

> *A camp stretches across the plain. The soldiers in it are not young. Each one is at least a Half-Immortal, and many are True Immortals. They are hundreds of years old, or thousands. Their armor is dented, mended, and dented again.*

> *At the center of the camp, on a plain wooden seat, sits an old man. His robe is grey. His hands are scarred. You know his face. It is the face of ???, but older and far more tired.*

**OLD MAN:** Welcome. I am the Gardener. The Garden you came from lives inside my mind. I made it. ??? is my image there. He is not a person. But he is the part of me that cares about you, and he has been telling you the truth, a little at a time.

**OLD MAN:** There is a war. The enemy is the Hollow Court. They do not kill people. They put out whole skies, one after another, until nothing is left.

**OLD MAN:** My path is the array. I build great patterns of light. Each lamp in the pattern is carried by one person, and the more lamps burn, the stronger the pattern grows. With every lamp lit, my array can stand against an army of True Immortals.

**OLD MAN:** That is still not enough. There are levels above True Immortal in this world, and I have only just touched them. So I need more lamps, and I need strong people to carry them. That is why I made the Garden. It grows lamp-bearers.

**OLD MAN:** The array holds nine great lamps, and no more. {lamps} of the nine are carried now.

**OLD MAN:** Now you choose. You can stay in the Garden, and I will send you back to grow more soldiers, faster. Or you can leave the Garden and walk the Outside.

### `end_oldman_full`: The Array Is Whole

**OLD MAN:** I must tell you something unkind. All nine great lamps are carried. The array is whole, and I cannot give you a lamp.

**OLD MAN:** You can still stay. You can still go. But do not raise your hand against me. The array is set now, and nothing in this world can break it.

### `end_oldman_stay`: Back to the Garden

**OLD MAN:** Then go back, and take a task with you. Grow more soldiers, and grow them faster than I could. Everyone in the Garden is a lamp that has not been lit yet.

**OLD MAN:** How you do it is up to you. I will not watch over your shoulder. I have a war to fight.

**OLD MAN:** The Seam will open again whenever you ask. Remember that. Staying is never a lock. Leaving is.

> *He lifts one scarred hand. The light of the Seam wraps around you, and you are carried home.*

### `end_steward`: The Steward

> *You are back in the Garden. The Seam closes behind you, quietly, like a hand around a seed.*

**???:** You came back. I did not think you would. Thank you. Somebody has to look after them.

> *Within the year, the Palace has a master with a face. The Nine Lamps are lit together for the first time. The Elders find you fair.*

> *You hunt down the cruel so the weak can grow. You hold tournaments and give the prizes away. You learn the name of every Wick in the Outer Court.*

> *The Garden does not become a paradise. It becomes a place where growing stronger is worth it, because somebody is watching who will be proud.*

**???:** I keep a lamp for each of them. Now there is one more person keeping watch. I am glad it is you.

### `end_usurper`: The Usurper

> *You are back in the Garden. The Gardener told you to grow soldiers. You decide to grow them your way.*

**???:** {how}

**???:** I could not tell you no. I was not allowed. I want you to know that I mind.

> *The Garden changes, the way a field changes when a storm walks over it. People grow stronger because they are afraid, and they call the result peace.*

> *The Good go first, because they are the ones who would stop you.*

> *In the Palace, the Empty Seat, warm for ten thousand years, goes a little cold.*

### `end_idler`: The Idler

> *You are back in the Garden. You do not do anything for a very long time, and it is wonderful.*

**???:** You are lying down. In the Hall of the Nine Lamps. I would like it noted that this is not what the Hall is for.

> *The Elders learn to step around you. The Wicks tell the new ones: that is the person who met the Gardener, and came home to take a nap.*

> *You grow tea. You sleep through three Convocations. A beast curls up on your feet. When it is time, you politely wake it and ask it to leave.*

> *The weeks go by. Far away, the war goes on without you, and the lamps stay lit. The Gardener lives, and as long as he does, so does the Garden. Someday that will end. Not yet.*

**???:** I should disapprove. But I have watched many people try very hard for a very long time. You are the first who looks happy. I will keep the lamps lit as long as I can. Take your time.

### `end_fade`: The Last Lamp

> *The weeks have run out. There is no week after this one. Far away in the Outside, the Gardener's long fight is over, and the Garden, which lives only inside him, begins to go out.*

**???:** It is time. I am only the part of him that stayed behind, and when he goes, I go. I wanted to say thank you. I kept a lamp for each of you. It was never a duty. I know all your names, and I will say them until I cannot.

### `end_fade_slain`: The Last Lamp

> *A great shock runs through the sky. Far away in the Outside, someone has done what nobody thought could be done. The Gardener is dead, and the Garden, which lives only inside him, begins to go out.*

**???:** It is time. I am only the part of him that stayed behind, and when he goes, I go. I wanted to say thank you. I kept a lamp for each of you. It was never a duty. I know all your names, and I will say them until I cannot.

### `end_fade_door`: The Seam Is Open

> *The Seam stands open. Beyond it, a road of pale light runs out into the dark. Behind you, one by one, the lamps of the Garden are going out.*

**???:** I did not mind you. I want you to hear that. Of everyone, I did not mind you at all. Go, if you want to. Or stay with me until the end. Either way, thank you.

### `end_dark`: The Light Fades

> *It does not hurt. That is the first surprise. The light only grows thin, like the last of the tea in a cup.*

> *The rivers stop first. They do not dry up. They only grow still, so that you can see the sky in them one last time. The sky is full of the faces of everyone you have known.*

> *The mountains forget their names. The cities forget their streets. Somewhere a bell begins to ring, and it does not finish the note.*

> *You think of the first morning. You were eighteen. The road was long and the whole world was new. You think of the people you met, the ones you loved, and the ones you fought. You remember them better than you ever did, and you are not afraid.*

> *The lamps go out, one at a time. Dawn. Patience. Salt. Iron. Rain. Ash. Glass. Hunger. Winter. You say each name, and each one is a little quieter than the last.*

> *At the end there is only one light, small as a seed. You hold it in your cupped hands. It is warm. It is the only thing left in the world, and it is enough.*

> *Then it too goes gentle into the dark. The Garden is not destroyed. It is finished, like a story at the end of its book. Somewhere in the dark, a very long way off, someone closes it with great care.*

### `end_soldier`: The True Ending: The Lamp-Bearer

**OLD MAN:** Good. Take this.

> *He puts a small lamp in your hands. It is warm. It flickers once, like a glad eye. Something in your chest answers it, as if a lamp had been lit there years ago and you only now noticed.*

**OLD MAN:** Nine great lamps hold the array. Yours is number {lampNo}. Behind you are the small ones: every cultivator from the Garden who came before you. They have been waiting.

> *You turn. Down the long slope of the camp, soldiers from the Garden are lining up. There are hundreds of them. Next to the old veterans they are young, and they are frightened. They hold their lamps in both hands. When they see you, they stand a little straighter.*

### `end_soldier_front`: At the Front

> *You take the place at the front. The old veterans make room for you without a word. It is the most dangerous place on the plain, and they know it.*

### `end_soldier_guard`: Beside the Young

> *You stand among the young ones. You show them how to hold a lamp when their hands shake, and how not to run. You tell them: stay close, and you will see morning.*

### `end_soldier_keep`: At the Back

> *You take the place at the back, with the wounded. A lamp that goes out in a fight is a lamp that cannot be lit again, so someone must keep them steady. You keep them steady.*

### `end_soldier_war`: The First Dawn

> *The Hollow Court comes at dawn. It makes no sound. The sky goes dark, patch by patch, and the dark moves toward the camp.*

> *The Gardener lifts his hands. Nine great lamps flare, and thousands of small ones with them. The array rises over the camp like a second sun.*

> *It is a bitter fight. The dark does not die; it only goes quiet, and comes back. Many fall. People you knew in the Garden fall. Veterans who have fought for a thousand years fall beside them.*

> *Somehow, the line holds. When night comes, the dark pulls back. You are alive. Fewer of the others are than you hoped.*

**OLD MAN:** That is one fight.

> *He says it plainly. He is not celebrating. There will be another at dawn. This is not the end of the war. It is only another struggle in the Outside, and you are in it now, and so is everyone you brought.*

> *Years later, in the Garden, on a road nobody remembers building, a traveller with your walk goes by. They leave a gift on the doorstep of whoever needs it most. The Gardener marked your soul, so as long as he thinks well of you, you can always find your way home.*

### `end_attack`: The Blow

> *You do not take the lamp. You turn to the old man, and with everything you have, you strike at him.*

> *The camp goes quiet. The veterans do not even draw their weapons. They look at you the way you might look at a child that has picked up a sword.*

**OLD MAN:** I know why. I made you strong and I never asked whether you wanted it. That is my fault.

> *The old man raises one hand.*

### `end_attack_whole`: The Array Is Whole

> *The array rises before you can move. All nine great lamps burn, and it does not hesitate. It does not need to.*

### `end_attack_dead`: Struck Down by the Array

> *The array answers. It does not feel like pain. It feels like being counted, and found to be too much.*

**OLD MAN:** I am sorry. I did not want this.

> *Your body comes apart into light. The light does not go out. It runs back down the Seam like water down a drain, all the way back to the Garden.*

> *That night, in the Garden, a rain falls that tastes of iron. A great many people wake up a little stronger than they were. They do not know why. Nobody tells them it was you. Your blood has lifted the potential of the whole world, for ever.*

### `end_attack_spared`: Spared

> *The light of the array stops a finger's width from your face. It hovers. It trembles. Then it goes out.*

**OLD MAN:** No. I need every lamp I can find, even a dark one. Go. Do not come back until you want to carry something.

> *The Seam closes behind you. Ahead, the sky has no walls.*

### `end_leave`: Alone

> *You turn away from the lamp and from the army. The old man watches you go. He does not stop you.*

**OLD MAN:** Walk well. If you ever want a lamp, you know where to find me.

> *The Seam folds shut behind you, small as a lit window. Ahead, the sky has no walls.*

### `end_ascendant`: The Hidden Ending: Past the Ceiling

> *Two hundred. The number is no longer a measure. It is a statement.*

> *The Garden has a ceiling: level 101, True Immortal. It was the last rung of the ladder the Gardener built. You put your hand against it. It is thin as paper, and you push.*

**???:** Stop. Please. I am not allowed to ask that, so I am asking as myself. There is no level after the last one. I would know. I helped write the tables.

> *There is one. The tables have no name for it. The Garden was built to hold a hundred thousand people, none stronger than the man who dreamed them. It makes a sound like a lamp being lifted off its hook.*

> *You walk into the Seam the way a flood walks into a field. You are not called. You are not carried. You are coming.*

> *In the Outside, the camp goes quiet. The veterans stand. At the center, on a plain wooden seat, the Gardener looks up.*

**OLD MAN:** You are not on the roll of lamps.

> *He lifts his hands. The array rises: nine great lamps and a thousand small ones, bright enough to hold back the Hollow Court. It meets you, and you walk through it, because no lamp in it is brighter than what you have become.*

> *You kill him. It is not hard. That is the part nobody warned you about. He is a very old, very tired man. The array that could hold a thousand True Immortals does not know what to do with someone who has gone past them.*

> *Far behind you, the Garden begins to go dark. It lived inside him. Now there is no inside. The bell stops. The lamps lean toward you, and one by one they go out.*

> *The war goes on. The army looks at you for a long moment, and then turns back to the dark. For the Hollow Court, it is one lamp fewer. For you, it is the biggest thing you have ever done, and you are the only person who knows how small it was.*


## The eight branches of the Outside (random: after walking out alone, being spared by the array, the hidden ending, or stepping through at the Garden's end)

*Tags: good, evil, neutral, mystery, bad, neutral, mystery, mystery, neutral, neutral, bad, neutral (in this order).*

### `out_road`: The Free Road

> *You walk out of the Seam into rain, and the rain is real, and it smells of an ordinary hill.*

> *There is a village below. It is on the wrong side of a war it has never heard of. You carry a woman's grain cart over a ford because it is on your way, and then because it is not.*

> *Within the year you have taught forty farmers to hold a spear, and forty more to not need one.*

> *They call you the Free Road, because wherever you stood the soldiers of every side found they could walk through without being asked for anything.*

### `out_throne`: The Small Throne

> *The Outside is poor in people who can do what you can do, and rich in people who will pay to be protected from them.*

> *You take a river valley, then the next, then a mountain pass with a good view. Nobody tells you no, and you find you have stopped asking whether anybody should.*

> *You are not a monster. You are a very good lord, the way a storm is good for rivers. The valley prospers. The valley is afraid. The valley prospers.*

> *Sometimes, very late, you hear a bell that is not there. You ring for more wine, and the bell goes away.*

### `out_teahouse`: The Teahouse at the Edge of the War

> *The war is a line on the horizon that moves a little every season. You build a teahouse on the side of it that is not on fire.*

> *Soldiers of both armies come in, and they are polite. The tea is very good. The owner is rumored to have come from nowhere, and is clearly not to be argued with.*

> *Nobody wins the war. It simply stops one spring, because there is nobody left who remembers what they were fighting about, and they are all in your teahouse.*

### `out_mirror`: The City of Mirrors

> *Past the third ridge there is a city built entirely of glass. In every pane there is a person who is you, walking a different road.*

> *In one pane you are the Steward. In another you have a crown. In a third you are small and nobody is looking at you, and you are happy.*

> *A woman made of reflections tells you that the city is a place where a Garden goes to be remembered. 'There are others,' she says. 'Have you not wondered why the Seam was already open?'*

> *You step through a pane. It is cold. It is not your Garden. There is a bell, and it is ringing.*

### `out_hollow`: The Hollow Edge

> *The sky beyond the third ridge is not dark. It is absent. A place where the lamps have been put out, and the putting-out is still going on, quietly, like frost.*

> *You raise your hands. It does not matter. It does not kill you; it only goes on subtracting. Your name, first. Then your face. Then the reason you came.*

> *You are pulled out by a hand you do not see, and set down in a field with no memory of your own name. A tired soldier wipes your face with a cloth, and says: 'You were gone for a while. Welcome back. What do you remember?'*

> *You remember a bell. It is the only thing.*

### `out_deserter`: The Deserters' Camp

> *They are all veterans of the war, hundreds of years old, in the dented armor of people who stood a long time in the line. They have put their spears down.*

> *'We are not afraid,' says their sergeant, a Half-Immortal with three scars. 'We are only tired. He never taught us to rest. Will you teach us?'*

> *You do not know how to teach rest. You sit with them, and show them, and by the end of the season nobody has deserted any further, and nobody has gone back either.*

> *Somewhere over the hill, an old man with scarred hands stops giving orders for a moment, and listens.*

### `out_garden`: The Other Garden

> *The Outside is not empty. It is full of gardens: pockets in other minds, each with its own bell, each with its own image keeping its lamps.*

> *You find one with a small dry orchard and an image that wears the face of a girl. She does not know she is an image. She is trying very hard to run a school.*

> *You do not tell her. You become her first student, quietly, and stay the winter.*

> *In spring you ask who her Gardener is. She says: 'I am not permitted to say.' And you realise how very many of these there must be.*

### `out_ferry`: The Ferryman

> *At the edge of the Outside there is a river the colour of nothing, and a boat, and a ferryman who knows you.*

> *'You are early,' he says. 'Or late. I never get it right. Pay the fare.' You hold out your hand. In it is a very small lamp. You do not remember being given it.*

> *He rows. The far shore is a bell, and a hall, and an empty seat, very like the one you left.*

> *'That is where it all began,' he says. 'Did he never tell you? He built the Garden to have somewhere to come back to.'*

### `out_farm`: The Quiet Farm

> *You walk until the road gets quiet. Then you walk a little more. There is a farm with a broken fence.*

> *You mend the fence. You learn to plow. You are not the strongest person for a hundred miles, or the weakest. You are just the neighbor.*

> *Once, a soldier in dented armor stops for water. He mentions an old man with a great array of lamps, far to the north. You nod and pass the bucket. It is a good bucket.*

> *At night you light one lamp on the porch, for no reason you can name, and you sleep well.*

### `out_peak`: The Long Climb

> *You do not stop at the edge of the Garden. You keep climbing.*

> *The Outside has levels the Garden never named. You find a teacher in a cave who hardly speaks. You find a mountain that tries to crush you, and you do not let it.*

> *Eighty years later, you have gone past anything you thought possible. On a clear night you can see a great light, far away: the Gardener's array, shining against the dark.*

> *You could go to it. You could even join. You wave. Then you turn back to the mountain. There is still more to climb.*

### `out_fall`: The Cold Pass

> *You cross the Outside with no map. For a while it goes well.*

> *Then, at a mountain pass, you meet a stranger who wants what you carry. He is stronger than you. You fight him anyway.*

> *It is a quick death and a plain one. Nobody in the Outside knows your name. Snow covers you.*

> *The last thing you see is a very small star, shaped like a lamp.*

### `out_dust`: The Dust of the Road

> *The Outside is larger than the Garden, and you are not the strongest in it. You are not even close.*

> *You carry loads for a trader. Then you keep his books. Then you keep his daughter's books. You never rise past the first realm again.*

> *It turns out to be a long, small, happy life. At the end, a young cultivator asks whether you ever wanted more. You think for a long time.*

> *'Once,' you say. 'It was enough to have wanted it.'*


## Wandering Immortal hints (given to a human on the same tile, every turn)

- The Cistern does not like being asked. Ask anyway.
- A weapon that meets a stronger fighter breaks. Carry two.
- The Thin Sky is cracked. Do not stand under it. (Do stand near it.)
- Spoiled food is not a tragedy. Spoiled food is a market signal.
- The Convocation notices anyone who crosses the second gate. It sends a lantern to find them.
- Evil elders murder for gear. Wear the plain one.
- A road with lit lanterns and no travellers is not a road. It is a memory.
- Every sect master is replaceable. Every Wick is somebody's child.
- Potential is a measure someone took of you. It is not a measure of you.
- There is an Outside. It is large. Nobody mapped it. Bring tea.
- The Giant Footprints are not natural. Something stood there and was pushed back.
- The bell is tuned every night by somebody who is not permitted to say so.
- Not every beast is a whetstone. Some are strays. Be kind to the strays.
- If a stranger leaves a gift on your doorstep, do not look for them. They are shy.
- The Door opens one time in five. Fall back one step, get up, try again.

## Funny credit lines (a random 18 are used per ending)

- Directed by: ??? (not permitted to say)
- Executive Producer: The Gardener (shears not included)
- Written by: A hundred thousand people, all aged eighteen
- Stunt Double: the Rope
- Cinematography: the Thin Sky, which has a crack in it and is not afraid to use it
- Catering: the Unlit Wayhouse (the lamp remained unlit)
- Music: the Bell (same note, tuned nightly, nobody noticed)
- Casting: the Assayer's Balance
- Dialect Coach: none of the beasts were consulted
- Beast Wrangler: every beast who was tamed was asked first. The answer was always 'raaar'
- Props: the Gardener's Tools, one (1) bottomless watering can
- Costumes: Wicks of the Outer Court, one grey robe, many sizes
- Key Grip: the Weaver's Wall, which is still arriving
- Gaffer: whoever lit the lanterns on Lantern Night. Nobody. Nobody lit them.
- Legal: the Cistern of Returning (nothing leaves)
- Spirit stones harmed in the making of this game: all of them
- No Hollow Court members were hired. They were uncredited because they put out the credits.
- Continuity: the Menagerie of Practice (cells 1 to 100, all of them open)
- Medical Advisor: a Potential Pill of dubious origin
- Weather: the Garden's, as rendered by someone's very large mind
- Pets: three thousand and seven, all of them somebody's, none of them dogs
- Understudy for the Hero: the Hero's reincarnation
- The Elders were played by the Elders. They did not enjoy it.
- Safety Officer: the Door ('it opens one time in five and does not care who you are')
- Translations: ???
- Chief Gardener: the Gardener, regrettably absent
- Sound Mixing: wind, fire, and a Garrison's worth of unreturned lanterns
- Quality Assurance: nine places, one Seat, zero complaints
- Sword Fights: choreographed by the Menagerie's strays, with their consent
- Special Effects: the Heavens (hands)
- Accounting: the market, whose goods spoil at a very fair rate
- The Epoch was not available for comment.
- Marketing: a very persuasive bell
- Human Resources: the Convocation of the First Gate (the bell is not a person, but it is also in HR)

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
