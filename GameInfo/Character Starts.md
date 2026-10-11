# Character Starts and Difficulty (proposal)

> **Status (2026-09-30): implemented** (`Starts.java`, `StartMenu.java`). Rebirth (reset gates, raise potential) is still a future feature.

Status: **design only, nothing implemented.** Based on your notes: Protagonist = potential 100; Standard = random 1-100; Story = several story types with preset characters of varied potential; Brutal = a difficulty tier for the story types with potential rolled in 1-20.

## 1. Structure

| Mode | Character | Potential | Intended feel |
|---|---|---|---|
| **Protagonist** | the perfect cultivator (the current default) | 100 | the heroic long game: first realms fly by, the top realm is still ~2,600 years away |
| **Standard** | random body, trait, affiliation | 1-100 (see 3) | "you are one of the crowd"; most rolls are mediocre, a few are gifted |
| **Story** | choose one of the preset characters below (name, trait, backstory, starting gear, relationships) | as written | a defined challenge with a reason to play |
| **Brutal** | any Story character, difficulty applied | rolled in the range (default 1-20) | "most of you never rise": luck, reincarnation and pills are the way up |

Difficulty is a separate slider over Story: **Story** (as written) -> **Hard** (potential x0.6, no starting gear) -> **Brutal** (potential rolled 1-20, no gear, no starting money, hostile start). Standard keeps its own random roll.

## 2. What I think

* **The tiers are good and fit the game's identity.** A potential-15 start cannot pass the first gate (needs 20) except by the 1-in-15 roll, and each failed try is a permanent slowdown, so "Brutal" really is a life of stagnation unless the player finds another way up. That only works if those other ways exist and are visible: Potential Pills, luck events (Lucky trait), heaven treasures, a benefactor, a sect that adopts you, and **reincarnation that resets the gates and raises potential**. I would build rebirth before shipping Brutal, otherwise a bad roll is a dead run.
* **Roll in a range but guarantee a story hook**, not just a number: Brutal rolls potential 1-20, but a Brutal start also grants one *thread* (a lucky trait, a mysterious pill, a rival to avenge, a debt) so it feels like a challenge, not a punishment.
* **Standard should not be a flat 1-100 roll.** A flat roll gives 20% of players potential <20 (stuck at the very start) and is harsher than NPCs on average relative to expectation. I would roll it from the **NPC distribution** (`101 - sqrt(random)`, mean 34) plus one re-roll if the result is below 20 ("fate rerolls once"); that keeps the "crowd" feeling without a dead start. Offer "true random 1-100" as an option.
* **Potential should be partially hidden** in Standard/Brutal (shown as a vague word: "dull / ordinary / gifted / prodigy") so the first breakthroughs are a discovery.
* **Starting realm:** today the first generation starts at age 18 but at level 1 (no prior cultivation). A possible refinement is to give them the XP a normal 18-year-old would have earned (about Major 1), while the protagonist keeps starting at level 1.

## 3. Standard roll, in numbers

| Roll | Chance |
|---|---|
| potential < 20 (cannot pass M1 gate without luck) | 34% flat-NPC curve, 20% flat 1-100 |
| potential 20-36 | 25% / 17% |
| potential 37-51 | 17% / 15% |
| potential 52-64 | 11% / 13% |
| potential >= 65 | 13% / 35% |

(NPC curve = what everyone else rolls. The flat 1-100 roll makes the player far stronger than the average NPC and cannot be called "one of the crowd".)

## 4. Story starts (blurbs)

Each start fixes name seed, gender choice, trait, potential, affiliation, a handful of starting relationships and a first quest chain. Potential shown is the **Story** value; Hard and Brutal override it.

1. **The Last Heir of the Azure Cloud Sect** (potential 100, Alchemist, Neutral). *Your sect burned, your master's last gift was a cracked cauldron and a name to find. You are the best talent in a generation and everyone who ruined your home knows it.* Starts with a damaged cauldron, one pill, one enemy.
2. **The Rich Scion** (potential 45, Merchant, Neutral). *Born in a merchant house with more spirit stones than sense. The caravans, the bodyguards and the best gear money can buy are yours, and so are the rivals who want them.* Bloodline bonus, strong gear, 3 Potential Pills, a bodyguard NPC; low skill, high leverage.
3. **The Orphan of the Ash Road** (potential 62, Runner, Good). *Raised by travellers, taught to survive. You never stay anywhere long enough to be hurt.* Fast, no gear, no money, many acquaintances along the road.
4. **The Disgraced Disciple** (potential 78, Scribe, Evil). *Expelled for reading what you should not have. You still carry half a forbidden scripture and a grudge the size of a sect.* Starts hunted by a sect; knows a forbidden technique fragment.
5. **The Blacksmith's Child** (potential 30, Blacksmith, Good). *Your father forged weapons for heroes and died holding a hammer. You will never out-cultivate them, but you will arm the ones who do.* Slow growth, excellent crafting, starts with a forge.
6. **The Beast-Tamer's Daughter** (potential 55, Gatherer, Neutral). *The wild remembers your mother's voice. Beasts hesitate before they attack you.* Beast-hunting bonus, herbs and metals, a small home area.
7. **The Ghost-Touched** (potential 70, Ghost physique, Neutral). *You died once and did not stay dead. People shy away; the dead do not.* Rebirth in a city on death; frightens strangers (small social penalty).
8. **The Lucky Fool** (potential 15, Lucky trait, Good). *Everyone agrees you have no talent. Everyone also agrees that things keep going your way.* Brutal-friendly by design: fortune events, loot bonus, well liked, but a weak foundation that needs every bit of luck.
9. **The Last of the Silver Crane Clan** (potential 85, Leader, Good). *Your clan is gone; the survivors look to you.* Starts with 3 loyal followers and an old clan hall; clan enemies at the gate.
10. **The Wandering Immortal's Disciple** (potential 90, Innovative, Neutral). *A drunk old man taught you three moves and vanished.* Powerful hidden technique, no memory of the name of your master.

## 5. Open points

* How many stories at launch (I suggest 6) and whether stories carry a **chain of quests** or just a hook.
* Whether difficulty changes only potential (my suggestion plus no starting gear/money) or also world hostility.
* Whether a death in Brutal should offer rebirth with a potential bonus (my suggestion: +5 potential per realm reached, cap 100, gates reset).
* Whether the Protagonist keeps starting at level 1 while NPCs start at age 18 with prior cultivation (my suggestion: yes).
