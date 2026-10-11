# Naming & Dictionary Review

> **Status (2026-09-30): sections 2-6 are implemented** (`Dictionary.java`, `Naming.java`, `Settlements.java`, `Chronicle.java`, `TextTools.java`, `GuiMore.java`). Not done from section 6: JLine command history / tab completion (needs a library) and ambient sound.

Status: **proposal only. No dictionary was changed.** Every list below is already de-duplicated against `Dictionary.java`. Approve, strike or edit entries, then I will apply them (each new place noun also needs an `ArtGen` scene and each new adjective a mood in `PlaceImages`, which I will add at the same time).

## 1. What is in `Dictionary.java` today (and what uses it)

| Array | Size | Used by |
|---|---|---|
| biome noun arrays (10 biomes) | 14-29 each | `Area` place names |
| `adjectivesPlaces` | 123 (2 duplicates) | place names (**every** place gets one: `rand.nextInt(1) == 0` is always true, probably meant 1-in-2) |
| `adjectiveOf` | 53 | "of X" suffix, 1 place in 5 |
| `numbers` | 20 | number prefix, 1 place in 5 |
| `adjectivesEntities` | 1,320 | beast first names (a generic English adjective list) |
| `monsters` | 30 | beast "surname" (species) |
| `animals` | 10 | **not used anywhere** |
| `colors` | 56 | **not used anywhere** |
| `maleNames` / `femaleNames` / `lastNames` | 107 / 106 (3 duplicates) / 1,165 (46 duplicates) | every cultivator |
| `weaponAdjectives` | 32 | weapon name = adjective + weapon type |
| `clothingAdjectives` x `clothingNames` | 23 x 12 | clothing name |
| `arrayAdjectives` x `arrayTypes` | 20 x 10 | array name |
| `nouns`, `places` | empty | unused |

Not in the dictionary at all: **sect names** (always "<Founder Full Name>'s Sect"), **city names** (the tile name, e.g. "Smokey Pits"), **technique names** ("<maker>'s Martial Technique" for every technique), **dungeon names** (tile names), shops, libraries, herbs/metals, and reputation titles (5 fixed ones in `Character`).

## 2. Janky entries (suggested removals)

### Place nouns
- **Plural/singular mismatch:** `Beach` and `Point` are singular among plurals ("Eight Beach" reads wrong). Make them `Beaches` and `Points`.
- **Wrong biome:** `Rapids` in the ocean list, `Fjords` in the forest list, `Pits` in every biome including ocean/beach, `Volcanoes` in tundra/frozen lists (fine as a fantasy volcano, but they are far too common: 5 of 10 biomes).
- **Man-made in a natural list:** `Bridges` (grassland, forest). Suggest `Natural Bridges` / `Stone Bridges`.
- **Odd:** `Mounts` (suggest `Peaks`), `Giant Footprints` is a lovely quirk, keep it.

### Place adjectives
- Duplicates: `Burning`, `Scorching` (they are twice as likely as the rest).
- Nouns used as adjectives: `Chaos` (next to `Chaotic`), `Kin`.
- Triplet: `Dread`, `Dreaded`, `Dreadful`; spelling `Smokey` -> `Smoky`; `Wanderer's` is possessive (reads oddly after a number); `Parlous`, `Musky`.
- Biome clashes: `Landlocked` on oceans, `Flooded`/`Sandy`/`Snowy` anywhere. Suggest biome-aware pools (a generic pool plus a pool per biome family).

### Beast adjectives (`adjectivesEntities`, 1,320 words)
This is a generic English adjective list, so beasts come out as "Dimwitted Death Lord" or "Both Wild Boar". **222** entries should go (function words such as All/Another/Both/Which/This, ordinals and directions, modern or mundane words such as Digital/Medical/Legal/Biodegradable/Kosher/Spanish, food words such as Buttery/Minty/Tasty/Yummy, cutesy words such as Wiggly/Squiggly/Giddy, gender and age words such as Male/Female/Fatherly/Elderly). Also one entry has an encoding glitch (`Naive` with a diaeresis).

Remove list:

All, Another, Any, Both, Each, Every, Few, Which, Whole, That, This, Those, Our, Some, Several, Same, Only, Next, Previous, Present, Past, Back, Front, Left, Right, Last, First, Second, Third, Early, Late, Total, Entire, Half, Double, Dual, Single, Twin, Joint, Equal, Even, Acceptable, Appropriate, Annual, Monthly, Quarterly, Biodegradable, Decimal, Digital, Dental, Medical, Political, Legal, Illegal, Lawful, Unlawful, Kosher, Spanish, French, Equatorial, Suburban, Urban, Rural, Edible, Potable, Nutritious, Cooked, Overcooked, Roasted, Buttery, Creamy, Minty, Yummy, Delicious, Tasty, Icky, Cuddly, Babyish, Married, Handmade, Virtual, Plastic, Bite-Sized, Jam-Packed, Pricey, Cheap, Costly, Expensive, Profitable, Official, Authorized, Academic, Scientific, Educated, Descriptive, Instructive, Informal, Formal, Conventional, Standard, Regular, Average, Medium, Ordinary, Practical, Functional, Automatic, Useful, Usable, Used, Utilized, Valid, Verifiable, Variable, Required, Necessary, Inconsequential, Insignificant, Trivial, Trifling, Negligible, Immaterial, Pertinent, Pointless, Mundane, Basic, Simple, Simplistic, Fixed, Finished, Unfinished, Unused, Untried, Uniform, Linear, Parallel, Circular, Cylindrical, Elliptical, Oblong, Oval, Rectangular, Round, Spherical, Square, Triangular, Fitting, Periodic, Occasional, Likely, Probable, Possible, Impossible, Improbable, Impractical, Unrealistic, Realistic, Reasonable, Proper, Genuine, Authentic, Original, Unwritten, Whirlwind, Gripping, Husky, Whopping, Dopey, Klutzy, Dimwitted, Wee, Jumbo, Itchy, Sniveling, Snoopy, Fluffy, Fuzzy, Baggy, Bubbly, Bouncy, Perky, Giddy, Silly, Wiggly, Squiggly, Zany, Kooky, Corny, Nutty, Nifty, Spiffy, Snappy, Peppery, Ornery, Pesky, Gummy, Mushy, Soupy, Slushy, Soggy, Runny, Mealy, Starchy, Sugary, Meaty, Juicy, Salty, Tart, Male, Female, Masculine, Feminine, Fatherly, Motherly, Adolescent, Elderly, Aged, Juvenile, Junior, Infantile, Youthful, Young

### Beast species (`monsters`, 30)
Several entries read as **named individuals**, not species: `Monkey King`, `Progenitor God`, `Combat God`, `Sea King Shi`, `King Ku`, `Mysterious Minstrel`, `Vampire Queen`, `Evil Dominator`, `Death Lord`, `Witch`, `Enchantress`, `Mother Bug`, `Mother Bug Servant`. Better use for them: a short **legendary named monsters** list used for awakened Monsters at high level. The species list should be **tiered by level** (see 3), because today a Lv 3 beast can be a "Progenitor God". The `animals` array (10 farm animals) is unused; it can seed the lowest tier.

### Colours (`colors`, 56, unused)
Currently unused. If they become place/item adjectives, drop the food and slang entries: Raspberry, Mango, Bubblegum, Salmon, Peach, Cocoa, Mahogany, Hazel, Taupe, Tan, Warm Gray, Coal Gray, Bronze Yellow, Metallic Blue, True Blue, Light Orange, Yellow Orange, Red Orange, Aqua Green, Green Blue, Lemon Yellow, Pale Rose, Light Blue, Light Brown, Dark Brown, Ruby Red, Navy Blue, Golden Yellow, Lime Green, Yellow Green. Add xianxia colours (3).

### Items
- Weapon adjective `True` ("True Sword") reads as unfinished: remove.
- **Names do not scale with level:** a Lv 1 and a Lv 10 sword draw from the same 32 adjectives, so a name says nothing about quality (see 4).

### Names
- Duplicate female names: Isabella, Lily, Piper. 46 duplicate surnames (Anderson, Bowers, Boyd, Cannon, Hancock, Hoffman, Miller, Myers, Ortiz, Quinn, Shaffer, Sweeney ...).
- **Pool too small for the population:** 107 male + 106 female first names x 1,119 surnames is about 238,000 full names. With 100,000 people plus every newborn over centuries, roughly **20,000 pairs of namesakes** are expected in the first generation alone (birthday maths), which makes `find`, logs and relationships ambiguous. The additions below roughly **triple** the first-name pools and add 183 surnames; a uniqueness check or a short epithet for homonyms would remove the problem entirely.

## 3. Proposed additions

### Names
- **Male first names (+148):** Aldric, Anselm, Barnaby, Bram, Caspian, Cedric, Dorian, Elric, Emmerich, Evander, Fabian, Gideon, Griffin, Hadrian, Hollis, Ignatius, Jareth, Kellan, Leander, Lorcan, Magnus, Marek, Nikolai, Osric, Percival, Quentin, Rafferty, Remy, Rhys, Rowan, Silas, Soren, Tobias, Ulric, Valen, Wystan, Zephyr, Alaric, Bastian, Corbin, Darius, Desmond, Edmund, Emeric, Garrick, Gavin, Hector, Lazarus, Lucian, Matthias, Maximus, Nolan, Orion, Peregrine, Rafael, Roderick, Sterling, Thaddeus, Tristan, Victor, Warrick, Xander, Yorick, Zane, Anders, Bjorn, Dmitri, Emil, Gunnar, Henrik, Ivar, Jorah, Konrad, Leif, Mikhail, Oleg, Piotr, Ragnar, Stellan, Torvald, Viktor, Anton, Bruno, Carlo, Dante, Enzo, Fabrizio, Giovanni, Lorenzo, Marco, Matteo, Nico, Paolo, Rocco, Salvatore, Tomas, Vito, Akira, Daichi, Haruki, Hiro, Kaito, Kenji, Ren, Ryo, Satoshi, Takeshi, Yuki, Jin, Wei, Bo, Cheng, Feng, Guang, Hao, Jun, Kai, Lei, Ming, Qiang, Rui, Tao, Xin, Yong, Zhen, Ashok, Dev, Kiran, Rahul, Rohan, Vikram, Idris, Kamal, Omar, Rashid, Tariq, Yusuf, Kwame, Amadi, Jabari, Tendai, Zuberi, Mateo, Diego, Emilio, Joaquin, Rafa, Santiago

- **Female first names (+142):** Adela, Alina, Beatrix, Briar, Calla, Camille, Cordelia, Dahlia, Delphine, Elara, Eloise, Esme, Fiona, Genevieve, Hestia, Ilse, Isolde, Jocelyn, Juniper, Katarina, Lavinia, Linnea, Marigold, Maren, Nerys, Odette, Ophelia, Petra, Rosalind, Rowena, Selene, Seraphina, Sylvie, Thalia, Una, Vesper, Wilhelmina, Yvette, Zelda, Astrid, Brynn, Cassia, Clementine, Diana, Elena, Fenella, Gwendolyn, Helena, Imelda, Josephine, Kestrel, Lorelei, Mirabel, Nadia, Octavia, Pandora, Rhiannon, Saoirse, Tamsin, Ursula, Viola, Winifred, Xanthe, Zinnia, Anya, Dasha, Galina, Ingrid, Katya, Lena, Marta, Natasha, Olga, Sasha, Sigrid, Svetlana, Tatiana, Valentina, Yelena, Bianca, Chiara, Elisa, Francesca, Gianna, Lucia, Paola, Serena, Vittoria, Aiko, Chiyo, Emi, Hana, Kaede, Mei, Miyu, Nanami, Rin, Sakura, Yui, Yuna, Ai, Bao, Chun, Fang, Hua, Jia, Lan, Li, Ling, Mulan, Nuan, Qing, Shu, Xiu, Yue, Zhu, Anjali, Deepa, Divya, Kavya, Lakshmi, Priya, Riya, Sita, Amara, Fatima, Noor, Safiya, Yasmin, Zainab, Abena, Amina, Imani, Kesi, Nia, Zola, Camila, Inés, Lucía, Marisol, Rosario, Valeria

- **Surnames (+183):** Ashford, Blackwood, Brightwater, Calloway, Dunmore, Eastwick, Fairchild, Greystone, Hawthorne, Ironside, Kingsley, Lockhart, Marlowe, Northcott, Oakenshield, Pemberton, Ravenscroft, Stormcrow, Thornfield, Underhill, Valmont, Whitlock, Winterbourne, Yarrow, Ashdown, Briarwood, Cinderfell, Dawnbringer, Evermoor, Frostmere, Goldleaf, Hollowell, Ivybridge, Jadewater, Kestrelhurst, Lightfoot, Moonbrook, Nightingale, Oakheart, Pinecrest, Quillfeather, Redmayne, Silverbrook, Tidewater, Umberfield, Wildwood, Wolfsbane, Zephyrine, Abernathy, Beaumont, Carrington, Grantham, Huntington, Kensington, Nottingham, Ockham, Prescott, Quimby, Rutherford, Sinclair, Thackeray, Vandermeer, Westbrook, Yardley, Aldridge, Barrow, Cromwell, Draycott, Ellwood, Foxworth, Gladstone, Harcourt, Jameson, Kilbride, Langdon, Mortimer, Norwood, Ormsby, Pritchard, Radcliffe, Stanhope, Trevelyan, Ullman, Vickers, Wetherby, Bianchi, Conti, Esposito, Ferrari, Greco, Lombardi, Marino, Neri, Orsini, Pellegrini, Santoro, Vitale, Bergstrom, Dahl, Eriksson, Fjeld, Gustafsson, Halvorsen, Iversen, Johansen, Karlsen, Lindqvist, Magnusson, Nilsen, Sorensen, Thorsen, Dietrich, Engel, Graf, Hartmann, Neumann, Richter, Vogel, Zimmermann, Zhao, Zhou, Xu, Sun, Ma, Zhu, Hu, Guo, He, Gao, Luo, Zheng, Liang, Xie, Song, Tang, Han, Feng, Deng, Cao, Peng, Zeng, Xiao, Tian, Dong, Pan, Yuan, Cai, Jiang, Du, Ye, Cheng, Wei, Su, Ding, Ren, Tanaka, Suzuki, Takahashi, Watanabe, Ito, Yamamoto, Nakamura, Kobayashi, Kato, Yoshida, Sato, Jung, Kang, Cho, Yoon, Jang, Sharma, Gupta, Kumar, Reddy, Nair, Mehta, Joshi, Hussain, Rahman, Haddad, Mansour, Nasser

The additions mix Western, Nordic, Romance, Chinese, Japanese, Indian, Arabic and African names because the current list is mostly Anglo. If you would rather keep one flavour, tell me which and I will filter.

### Place nouns (plural so numbers read well), with the scene `ArtGen` will paint

- **all:** Waterfalls (falls), Gorges (falls), Valleys (river), Ridges (mountains), Peaks (mountains), Summits (mountains), Passes (mountains), Highlands (hills), Sinkholes (cave), Hot Springs (spring)
- **forest:** Bamboo Forests (forest), Pine Forests (forest), Cherry Blossom Groves (forest), Willow Groves (forest), Lotus Ponds (lake), Stone Forests (mountains), Orchards (forest), Terraces (hills), Wetlands (marsh), Deltas (river)
- **grassland:** Prairies (meadow), Steppes (meadow), Moors (marsh), Orchards (forest), Terraces (hills), Wetlands (marsh), Lotus Ponds (lake), Oxbow Lakes (lake), Natural Bridges (arch), Standing Stones (arch)
- **desert:** Dunes (desert), Salt Flats (desert), Sand Seas (desert), Hoodoos (mountains), Slot Canyons (falls), Mirage Wastes (desert), Buried Ruins (desert), Rock Gardens (hills)
- **savanna:** Acacia Groves (forest), Termite Mounds (hills), Watering Holes (lake), Kopjes (mountains), Dry Riverbeds (river), Salt Pans (desert)
- **tundra:** Snowfields (ice), Ice Fields (ice), Frozen Lakes (lake), Frozen Waterfalls (falls), Ice Caves (cave), Fjords (sea), Permafrost Plains (ice), Aurora Plains (ice), Geysers (spring)
- **taiga:** Pine Forests (forest), Frozen Lakes (lake), Snow Groves (forest), Bogs (marsh), Ice Falls (falls), Muskegs (marsh)
- **frozen:** Snowfields (ice), Ice Fields (ice), Frozen Lakes (lake), Ice Caves (cave), Glaciers (ice), Frost Hollows (cave), Rime Forests (forest)
- **ocean:** Kelp Forests (sea), Sea Stacks (islands), Whirlpools (sea), Hydrothermal Vents (spring), Sunken Gardens (sea), Trenches (cave), Cloud Seas (sea), Shoals (islands)
- **beach:** Tidal Flats (sea), Salt Marshes (marsh), Dunes (desert), Sea Stacks (islands), Beaches (meadow), Headlands (hills), Sea Caves (cave), Driftwood Shores (sea)

`all` = add to every biome where it makes sense. Biome-specific lists go only to that biome (frozen = frozen grassland + frozen desert).

### Place adjectives (+124)
Verdant, Misty, Silent, Whispering, Howling, Weeping, Mourning, Gilded, Azure, Ivory, Ebon, Moonlit, Starlit, Sunlit, Thundering, Roaring, Singing, Hollow, Floating, Hanging, Sunken, Buried, Fallen, Frostbitten, Glacial, Boiling, Molten, Ashen, Withered, Blooming, Celestial, Divine, Immortal, Demonic, Sealed, Veiled, Shrouded, Drowned, Silvered, Iron, Bone, Serene, Tranquil, Solitary, Windward, Crystalline, Prismatic, Obsidian, Thunderous, Emerald, Sapphire, Amber, Scarlet, Vermilion, Cinnabar, Jade, Golden, Silver, Crimson, Bamboo, Lotus, Willow, Plum, Cherry, Pearl, Coral, Cloudy, Frozen, Mirror, Gleaming, Gentle, Wild, Savage, Hallowed, Profane, Blighted, Sacrosanct, Ghostly, Spectral, Nameless, Forgotten, Secret, Veiling, Wandering, Stormy, Breezy, Sultry, Parched, Barren, Lush, Overgrown, Terraced, Cliffside, Lakeside, Riverside, Highland, Lowland, Northern, Southern, Eastern, Western, Central, Outer, Inner, Upper, Lower, Twin, Triple, Nine-Fold, Thousand-Mile, Heaven-Touched, Spirit-Rich, Qi-Dense, Moon-Washed, Sun-Scorched, Dragon's, Phoenix's, Tiger's, Crane's, Hermit's, Immortal's, Sage's, Elder's, Ancestor's

### Place numbers (+11)
Eighteen, Twenty-Four, Thirty-Three, Forty-Nine, Seventy-Two, Eighty-One, Ninety-Nine, One Hundred, One Thousand, Three Thousand, Ten Thousand. (`Hundred` and `Thousand` should read `One Hundred` and `One Thousand`; `Sixty-Nine` is yours to keep or cut.)

### Beast adjectives, fantasy flavoured (+96)
Ferocious, Savage, Feral, Rabid, Ravenous, Cunning, Primal, Hulking, Scarred, One-Eyed, Three-Tailed, Nine-Tailed, Six-Winged, Horned, Fanged, Armored, Flame-Maned, Frost-Furred, Storm-Winged, Thunder-Scaled, Venomous, Shadow-Cloaked, Iron-Hided, Jade-Scaled, Bone-Plated, Blood-Eyed, Moon-Marked, Sun-Touched, Star-Eyed, Void-Born, Spirit-Touched, Wind-Swift, Stone-Skinned, Ember-Eyed, Ash-Furred, Silver-Maned, Golden-Crested, Crimson-Clawed, Azure-Scaled, Obsidian-Horned, Lightning-Fast, Soul-Eating, Mind-Bending, Night-Prowling, Dawn-Hunting, Tide-Calling, Mist-Walking, Cave-Dwelling, Sky-Splitting, Earth-Shaking, Ground-Shaking, Plague-Ridden, Bloodthirsty, Relentless, Territorial, Elder, Primeval, Mythic, Legendary, Cursed, Blighted, Corrupted, Awakened, Frenzied, Stalking, Lurking, Prowling, Soaring, Burrowing, Skulking, Gnashing, Howling, Shrieking, Hissing, Roaring, Spiked, Thorned, Tusked, Many-Eyed, Many-Legged, Twin-Headed, Three-Headed, Cloud-Riding, Mountain-Sized, Tower-Tall, Spectral, Rotting, Gilded, Glass-Winged, Sand-Colored, Snow-White, Midnight, Ember-Backed, Gem-Studded, Sharp-Eyed, Patient

### Beast species by tier (replaces the flat list; the tier follows the beast's level)
- **Lv 1-20:** Field Mouse, Hare, Wild Boar, Grey Wolf, Wildcat, Badger, Stag, Black Bear, Viper, Giant Toad, Crow, Hawk, Jackal, Lizard, Marsh Frog, Otter, Weasel, Mountain Goat, Giant Ant
- **Lv 21-50:** Spirit Wolf, Iron-Fanged Boar, Thunder Hawk, Flame Salamander, Frost Lynx, Jade Serpent, Moonlit Stag, Shadow Panther, Stone Tortoise, Wind Leopard, Cave Bat Swarm, Poison Centipede, Horned Bear, Mirage Fox, Sand Scorpion, Tide Crab, Silver-Winged Moth, Bone Hound, Lotus Toad, Iron-Shell Beetle
- **Lv 51-80:** Nine-Tailed Fox, Azure Crane, Thunderbird, Pixiu, Qilin, Bai Ze, Xiezhi, Jiaolong, Golden Roc, Venom Basilisk, Frost Wyrm, Magma Drake, Spirit Tiger, Storm Eagle, Blood Bat King, Void Spider, Mountain Giant, Ghost Lion, Skyhorn Ram, Abyssal Eel
- **Lv 81+:** Azure Dragon, Vermilion Bird, White Tiger, Black Tortoise, Yellow Dragon, Kunpeng, Fenghuang, Taotie, Hundun, Qiongqi, Taowu, Nine-Headed Serpent, Heavenly Hound, World Tortoise, Thousand-Eyed Watcher, Chaos Beast, Primordial Phoenix, Star-Devouring Toad

Awakened Monsters keep their beast's species and level, so a dead Major 10 killed by a beast wakes as a mythic-tier Monster, as you described.

### Colours (+39)
Vermilion, Cinnabar, Azure, Crimson, Scarlet, Gold, Silver, Ivory, Ebony, Indigo, Amethyst, Jade, Celadon, Ochre, Umber, Sienna, Verdigris, Saffron, Obsidian, Pearl, Ashen, Rosewood, Lapis, Emerald, Sapphire, Ruby, Onyx, Copper, Bronze, Rust, Moss, Fern, Sage, Charcoal, Midnight, Dusk, Dawn, Sunset, Twilight

## 4. Items

**Weapon adjectives (+51):** Azure, Vermilion, Cinnabar, Verdant, Frostbitten, Ember, Sunforged, Moonforged, Starfall, Rending, Whispering, Howling, Soulbound, Bloodthirsty, Celestial, Abyssal, Primordial, Sanguine, Hallowed, Jade-Veined, Cloud-Splitting, Thunderclap, Frostfire, Emerald, Sapphire, Amethyst, Onyx, Bronze, Steel, Meteoric, Cold-Forged, Ashen, Mournful, Venomous, Radiant, Hungering, Silent, Nine-Dragon, Five-Element, Heaven-Cleaving, Earth-Rending, Sky-Piercing, Ghostly, Gilded, Tempered, Ancestral, Cursed, Blessed, Wyrmbone, Phoenix, Qilin

**Clothing adjectives (+34):** Azure, Vermilion, Emerald, Sapphire, Verdant, Amber, Ivory, Ebon, Pearl, Mist, Dawn, Dusk, Twilight, Thunder, Flame, Ember, Tempest, Starlit, Sunlit, Lunar, Solar, Lotus-Embroidered, Dragon-Scale, Phoenix-Feather, Crane-Feather, Tiger-Stripe, Iron-Thread, Jade-Thread, Gossamer, Mournful, Hallowed, Ancestral, Hermit's, Sage's

**Clothing names (+14):** Gown, Jerkin, Mail, Plate, Hauberk, Surcoat, Shawl, Wrap, Tabard, Habit, Hanfu, Sash-Robe, Coat of Scales, Brigandine

**Array adjectives (+27):** Azure-Dragon, Vermilion-Bird, White-Tiger, Black-Tortoise, Nine-Palace, Eight-Trigram, Two-Principle, Three-Talent, Seven-Star, Heaven-Earth, Yin-Yang, Cloud-Veiling, Soul-Binding, Heaven-Shaking, Earth-Rending, Thunder-Calling, Frost-Binding, Flame-Ringed, Wind-Gathering, Illusory, Bewildering, Devouring, Reflecting, Phantom, Guardian, Warding, Slaughtering

**Array types (+8):** Pavilion Seal, Net, Cage, Gate, Circle, Pillar Array, Labyrinth, Sigil

**Level-aware item names (recommended).** Name = `[Grade] [Adjective] [Type]` where the grade follows the item's Major level and the adjective pool follows the tier: Majors 1-2 *Mortal* (iron, bronze, plain), 3-4 *Spirit*, 5-6 *Earth*, 7-8 *Heaven*, 9-10 *Divine* (the classic Yellow/Mystic/Earth/Heaven grading), with materials rising from Iron through Spirit Iron to Star Metal. Herbs, metals and pills should get real names too (they are all "Herb Lvl 4" today): Ginseng, Lingzhi, Frostgrass, Moonlotus ...; Black Iron, Cold Steel, Meteoric Iron ...

## 5. Name generation engine (cities, sects, dungeons, features, techniques, titles)

Today every place is `[Number] [Adjective] [Noun] [of X]`, so a city is just "Smokey Pits" and a sect is "Harry Hood's Sect". Proposal: one `Naming` class with seeded randomness, a **uniqueness registry** (no two places or sects share a name) and **templates per kind**:

| Kind | Pattern | Examples |
|---|---|---|
| City | `[Syllable name] [Rank]` or `[Founder]'s [Landmark]` or `[Geo adjective] [Noun]`; rank by size | Qinghe City, Hogan's Rest, Three Pines, Azure Cloud Capital |
| Sect | `[Element/Heaven/Beast] [Noun] [Sect/Pavilion/Hall/Temple/Valley/Palace/Gate/Alliance]`; **alignment** picks the vocabulary (Evil: Blood Moon, Shadow, Ninefold Hell; Good: Radiant, Heavenly, Compassion; Neutral: Wandering, Hidden); **founder trait** picks the type (Alchemist: Pill Pavilion / Cauldron Hall, Blacksmith: Forge, Scribe: Scroll Hall, Merchant: Trading Guild, Leader: Dynasty / Alliance, Runner: Swift Wind) | Azure Cloud Sect, Nine Thunder Pavilion, Blood Moon Hall |
| Dungeon | by type: Lost Ruins -> "Ruins of the X King"; Beast Lair -> "X's Den / Hollow"; Martial Trial -> "Gauntlet of X"; Haunted Grounds -> "X Graveyard / Weeping Marsh"; Celestial Trial -> "Heavenly Trial Grounds" | Tomb of the Jade King, Ashfang Den |
| Spirit vein / features | `[Adjective] Spirit Vein`, `[Noun] of [Element]` | Moonlit Spirit Vein |
| Shop / library | `[Owner]'s [Emporium/Pavilion/Exchange]`, `Hall of [Adjective] Scrolls` | Thousand Treasures Pavilion |
| Technique | `[Number/Element] [Creature/Nature] [Palm/Fist/Sword Art/Step/Scripture]`; movement techniques: `[Element] [Step/Leap/Flight]` | Nine-Heavens Thunder Palm, Cloud-Stepping |
| Reputation titles | many, from kills, realm, trade and deeds (today 5) | Sword Saint, Thousand-Mile Wanderer, Pill Sage |

Syllable names use two phoneme sets (pinyin-like: qing, yun, lu, hai, feng ...; and a Western-fantasy set) chosen by a region or founder culture, so a world can mix cultures.

Engine fixes worth making regardless: make the adjective chance explicit (the `nextInt(1)` bug), merge duplicates in `adjectiveOf` ("Ancestors" and "The Ancestors", `10,000 Corpses` as digits while numbers are spelled out), seed names from the world seed so a saved world regenerates the same names, and add a uniqueness/epithet pass for homonymous cultivators.

## 6. GUI and terminal ideas, in the order I would do them

**Terminal parity (terminal users get far less than GUI users today):**
1. `map` command: ASCII world map with coordinates, biome letters, cities/sects/dungeons and your position (ANSI colour optional).
2. `bag` improvements: `equip <item>`, `compare <item>`, `techniques`.
3. `journal` / `history <name>`: biography and world chronicle, searchable.
4. `watch`: fast-forward until an event (breakthrough, alert, attack, auction, birthday of a child).
5. `route <x> <y>`: path preview with distance and ETA; `time`: year, season, ticks to next auction.
6. Command history and tab completion (JLine) and `help <topic>`.

**GUI:**
1. **Backpack / equipment window:** equip, sell, compare with tooltips, technique manager.
2. **Chronicle window:** world history, hall of legends, reincarnation lineage, filters by sect / city / person.
3. **Auto-pause rules:** pause on attack, breakthrough, low health, alert, auction; fast-forward until the next event.
4. **Biography tab** in the character dialog: life timeline chart (level, wealth, relationships over time).
5. **Bookmarks and notes** on tiles and cultivators; hover cards on names in the log.
6. **Balance dashboard** in Rankings (population, deaths by cause, wealth distribution, realm histogram), plus a hotkey cheat sheet overlay.
7. A **region/world overview** zoom and a minimap in the map's side gutters.
8. Optional ambient sound and a high-contrast theme.
