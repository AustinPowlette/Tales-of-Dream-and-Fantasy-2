# Development Log — Tales of Dream and Fantasy 2

## 2026-10-07 (the endings, seekers, Wandering Immortals, Giant Footprints)
- Added: six endings at the open Seam (`LoreEndings`): stay as the Good Steward (master of the Palace: gifts, a cull of the cruel), stay as the Evil Usurper (take / raze the Palace, own sect, alone; a hidden ending at potential 200 via `lore usurp`), stay as the Neutral Idler (the Garden goes out after 150 years), become a soldier (the true ending), leave alone (8 random Outside branches), try to kill ??? (50% struck down, 50% spared and sent to the Outside). Each: a story stub, funny credits (a scrolling window in the GUI, text in the terminal), a Hall of Fame line with a score, a Roll of the Crossed line, and the alignment of the choice.
- Added: soldiers come back as Wandering Immortals (`Character.wanderingImmortal`: cannot die, wander, give a pill and a hint every turn); their descendants are blessed (+5 strength in levels and +5 potential per soldier in the line, stacking: `Character.blessing`); the player is released to the succession dialog (`LoreEndings.releases`).
- Added: NPC seekers (the Convocation's admitted NPCs read the nine places in order in the background; at Major 10 / level 101 they take a random ending); Giant Footprints (six places near the Thin Sky and the Garrison: a short clue and a rare relic each, `LoreSites.placeFeet`).
- Added: `lore roll`, `lore hall`, `lore usurp` (hidden); the Journal shows the ending, the score and the footprints read; epoch purge spares characters a human has taken over.
- Fixed: a Wandering Immortal's title was reset by the yearly title refresh.
- Changed (2026-10-07, later): the blessing is +5 to `tempLvl` per soldier in the line (it had been +5 levels before the division); the Usurper's take-over kills the Palace's Good members; each year Usurpers (dark path) and Stewards post bounties on Good / Evil cultivators on the city boards for any sect member to take (`LoreEndings.huntYearly`); ??? no longer dies after 150 years: the Idler waits for the end of the world, and when the week counter nears its limit (`LoreEndings.worldEndWeek`) ??? goes out, Idlers choose, and the game saves and ends instead of overflowing; the hidden ending is rewritten (break the ceiling above True Immortal, kill the Gardener Outside, nothing changes there, a very big deal to the player).
- Tests: `LoreProbe` 106 checks (all endings, blessing stacking, seekers, footprints, wanderers, save), `EndFlowProbe` (the soldier ending through the real game loop and the succession dialog), `EndShots` (ending scene and credits).


## 2026-10-06 (fight reports, sect villainy, spoilage, logo, window overhaul, Help, terminal, lore, performance)
- Added: fight reports (`FightReport`): every fight a human or a followed character is in prints one block (odds, flight, forbidden technique, winner, kill / spare / injury, loot, array attempts, tag-team), posts a `fight` notice and a Chronicle line; routine beast hunts are one `[Fight]` line; `fights [n]` re-reads them (`Character.battle` is now `battleCore` inside `FightReport.begin/end`).
- Added: a non-lethal loser with bad blood (negative relationship, bounty, revenge, war) pays the winner 10% of their wealth, exact across all four currencies (`FightReport.moveWealth`); sparring and strangers' duels never cost anything.
- Added: sect villainy (`SectPolitics`): inner disciples bully outer ones (15% of stones or a spare weapon), Evil elders murder weaker disciples for gear (some crimes come to light and the elder is cast out with a bounty), steal from the treasury, poison and frame rivals, and Evil / Neutral elders plot coups against the master (`Organization.plot`, saved); Good members never take part; human targets are told and asked first; the old treachery event moved here.
- Changed: more spoilage (`Life.spoil`): food 55%, poison 20%, wine 15% a year at grade 1, shelves, pantries and bags included, at least 8%, big stacks rot faster, a character keeps at most 200 portions.
- Added: the game's emblem (`ArtMore.logo`, `assets/logo/*`, `tools/MakeLogo.java`) as window and taskbar icon; `tools/make-exe.bat` builds `dist\TalesOfDreamAndFantasy2\TalesOfDreamAndFantasy2.exe` with jpackage (tested: starts and runs).
- Changed: the dock (`GuiDock`): Move tab gets clickable buttons for everything the command bar does (8-way walk, Move to Selected Tile, Go to top quest, Route, Watch; Meditate / Hunt / Forage / Mine / Craft / Rest / Sell / Buy gear / Pills / Eat / Drink / Equip, greyed out with the reason when they cannot work here; Do top quest, Go to next alert; Talk, Spar, People here), the Help button moved to the Move tab, the Auto tab shows what the character is doing with a one-click "Take control back", the dock gets 60% of the right column (the d-pad arrows and the "?" buttons were being cut to "...").
- Changed: ONE quest window, the Quest Log (`GuiQuests`; `Quests` in Me and `Quests+` in World were two different windows), with a card for the selected quest, Go there / Do Quest / Open target / pin / mute / waypoints; `Duties & Policies` is a real themed window (`GuiDuties`; the Duties button used to print to the terminal); the Keys sheet is a key-cap layout (`GuiCards.keysWindow`); People here, Here, Journal are themed cards; every window has a purpose line and a "?" that opens its Help page (`GuiHub.header`, `GuiHelp.helpButton`).
- Added: the illustrated interactive Help (`GuiHelp`, `HelpContent`, `HelpPages`, `HelpPagesMore`, ~50 pages, tree + search + links + glossary + the generated command reference, `assets/help/*.png` screenshots made by `tools/HelpShots.java`), F1 / H / the Help button / `guide <page>`; the welcome tour lights up parts of the window and links into it.
- Added: terminal help and feel (`TermHelp`, `TermHud`, `TermInput`, `TermFormat`, `TermView`): a status block before each prompt (`settings hud off`), categorised `help`, did-you-mean for mistyped commands, unambiguous prefixes, aligned tables with thousands separators, safe number parsing (`list people 99999999999` used to throw).
- Added: lore (`Lore`, `LoreSites`, `LoreText`, `GuiLore`, `Lore Design.md`): the Palace of the Oldest Order stands at Originis in every new world (a sect with a virtual master "???"), the Convocation of the First Gate every New Year (entrance tournament), seven acts of objectives for every human, nine lore places scattered by the world seed, the Nine Lamps, ??? speaking in pop-up scenes (terminal: framed text) and whispers, omens, and the crossing at level 101; `settings lore on|off`, `lore ...` commands, Journal window (Me tab, Oldest Order).
- Added: `Organization.virtual` (a sect with no master character: `promote()` never crowns anyone, no wars, `whyNot` asks the lore).
- Changed: performance (`Performance Report.md`): the Heaven Treasure scan only runs while a treasure exists (`Map.heavenExists`), Seek Potential looks one turn in six, `tryRallyParty` walks the friend list instead of copying the whole tile; `Perf` (`-Dtdf.perf=true`), `-Dtdf.seed`, `-Dtdf.threads`, `-Dtdf.spread` for measurement.
- Fixed: a lore objective that was a quest at the target tile was re-created every time the player crossed that tile: alert spam and every `goto` interrupted. Only walks are quests now.
- Fixed: a human who resigned from the Palace was re-admitted on the next turn (`Lore.onResign`).
- Fixed: a beast kill printed the full fight report instead of one line (`FightReport.compact`).
- Changed: a new world of 20,000+ people starts spread over 15 x 15 tiles (`Settings.startSpread`, `settings spread <odd n>`, 0 = the old 3 x 3 zone): the first ticks at 100,000 take 1.0 s instead of 10.7 s; the world's health in a 3,000 test world is the same (see `Performance Report.md`).
- Changed: sects rest seven years (was four) after a war: 77% of 75 sects were at war at once in the long run.
- Changed: the Convocation of the First Gate is answered by unaffiliated cultivators from every land (up to 1,500), not only those standing in Originis: 1,531 competed and 250 were admitted in a 100,000 world (37 and 13 before); the Palace's member cap is a share of the population (`Organization.memberCap`), no master needed.
- Changed: an Evil elder without a vial buys one from a street poisoner 30% of the time they scheme (`SectPolitics.poisonRival`): 0 poisonings in 65 years before; the Palace also has villainy (murder, theft, framing; no coups: no master to overthrow); `Master: ???` is shown for the Palace everywhere; the Palace cannot be razed.
- Fixed: the Quest Log and People here windows fell into an endless refresh (StackOverflowError) when a row was selected: refreshing re-selected the row, which fired the selection listener (`GuiQuests`, `GuiCards`: re-entrancy guard).
- Fixed: the diagonal arrow glyphs and the flag glyph are not in the UI font (shown as boxes): the d-pad says NW NE SW SE, the quest state says NEW; the Auto tab card was cut off; the Keys window was too narrow; `Settings` has check boxes for the story and the scene pause.
- Tests: probes were running without FlatLaf (MSYS mangled the class path); fixed, GUI probes updated to the new buttons (QolGui 53, PcProbeGui 130, MoreGui 35), `HelpCheck` (58 pages, 583 links, 51 images), `QolSeed`.
- Tests: new `SimProbeX` (73 checks: reports, penalty, villainy, coups, spoilage), new `LoreProbe` (68 checks: Palace, Convocation, the whole road, places, scenes, crossing, save, old worlds, master-less sect safety); regression PcProbe 112/112, PcProbe2, QolProbe, RulesProbe, StartsProbe, TextProbe, LifeProbe 85/85, SpecProbe, Chaos green; see the `Playtest Plan.md` results block for the long runs.


## 2026-10-05 (music polish, GUI fixes, movement, sects and wars, more art and names, playtest plan)
- Changed: music notes are shorter and pleasanter (`Ambient`): strings ring about 2 s (Karplus-Strong decay now set by a 60 dB time), pad/erhu/flute/bell/drum lengths cut, level raised; sound source default `auto` (a SoundFont from `soundfonts/` when present, else the built-in synth); the 32 MB GeneralUser GS font is in `soundfonts/` (git-ignored).
- Fixed: "Mode" and "Modify character?" start-up prompts appeared in the terminal with the map viewer on; they are dialogs now (`StartMenu.askMode/askPlayerCount/askYesNo`).
- Fixed: the map froze during hybrid scripts, `goto` and Move Here: the repaint was scheduled after the game took the world lock back; the map picture is now rendered inside the end-of-tick hand-off (`MapPanel.renderBuffer`, `MapViewer.refreshNow`). Tiles renamed by a founded city show their land name too ("was ...") and their region.
- Added: diagonal moves for players (`nw ne sw se`, d-pad buttons, numpad / Home End PgUp PgDn keys; one turn, same as the pathfinder; `Character.movement('Q' 'E' 'Z' 'C')`); `goto` and Move Here take the shortest way across the wrapping world and cut corners (`PlayerCommands.stepAuto`); `goquest [n|#uid]` and `chase <#id>` (a chase re-aims every turn, gives up after 150), a "Go there" button in the quest panel.
- Added: `quit [nosave]` and a Quit button (the window's X asks too): the game saves at the end of the turn and exits (`GameDriver.quitRequested`).
- Added: mature start — a new human (not a Story start) begins with the ten years of cultivation everybody else had, stopping short of Major Lvl 3 (`Starts.prehistoryPlayer`, `Settings.matureStart`, `settings maturestart on|off`).
- Added: sect member cap 20 + 8 per sect level + 15 per master realm (`Organization.memberCap`, checked in `whyNot`); yearly treachery (an ambitious disciple becomes the master's arch-enemy, `Seasonal.diplomacy`); wars are dated (`warSince`), never end in the first two years, then a growing yearly chance of a truce, and after five years renewal (40%) or an end; warring sects' disciples fight whenever they meet (`Character.quest`).
- Added: squads are capped (12 for a Leader who is not a sect master or city lord, 4 for everybody else; companion beasts do not count; `Character.squadSize/partyCap/followerIds`) and such Leaders rally followers more often.
- Changed: a character holds at most two Hunt Beast bounties at a time (the board used to empty into one queue); `Hunt` says why when it cannot fight.
- Added: `ArtMore` — portraits (character window), sect seals (tile info), item icons (backpack), a painted world map with named regions (`Painted map`, Save as PNG), event illustrations in the Chronicle window, weather (clear / rain / fog per tile and month, in pictures and the tile info). Pill brands (`Item.brand`, `Naming.pillBrand`) and region-flavoured person names (`Naming.person(female, area)`).
- Added: `tools/PlaytestProbe.java` and `Playtest Plan.md` (long run, 100k stability, save compatibility, first hour).
- Fixed: yearly `Seasonal.diplomacy` skipped wars whose other side was not in the sect list.
- Fixed: spectator mode (no human) shifted every character id by one: the start-zone spread code created a stand-in `Character` that took id 0 and was never listed (`GameDriver`); the stand-in now leaves no trace. Player-mode games were not affected. `PlaytestProbe` records `idMismatch` per year, `SpecProbe` guards it.
- Fixed: Battle Rival / Arch-Enemy / Revenge quests had been merged into the Poison Contract handler (`Character.targetQuests`): rivals never fought; restored (found by PcProbe S5).
- Added: status-bar "▶ Go" (goquest) button; sects rest four years after a war (`Organization.peaceUntil`, NPC masters cannot start a new war before it).
- Tests: NewProbe (25 checks), SpecProbe, CompatProbe (old 100k save.dat loads, runs, re-saves and reloads: 17/17; the old save carries 7,891 id mismatches from older code, they do not grow), OnboardProbe; full suite green apart from timing flakes (PcProbeGui "Resume continues it" / "script running puts the character in HYBRID" / Move Here, LifeProbe L16 buy count: NPCs also buy during the turn).
- Playtest results (see `Playtest Plan.md`, "Results"): 100k world stable over 2 years (population 100.0k -> 100.9k -> 100.3k, 0 exceptions, 0 id mismatches, ~3 s per tick); 3,000- and 20,000-start worlds keep growing (+3 to 6% a year) because they start far below the carrying capacity.

## 2026-10-04 (challenge limit, start-zone spread, readable Me-tab windows, goods in shops/auctions/sects, Originis art, music engine, name engines, regions)
- Changed: strangers and dislikers may challenge a human only `Settings.maxRandomChallenges` (default 2) times per week; rivals, arch-enemies, party/sect-war fights always get through (`Character.acceptChallenge`, `mustFace`, counters in `PlayerControl`); `settings challenges <n>`. NPCs are not limited (the cap exists to keep pop-ups bearable).
- Changed: the first generation spreads over the 3x3 tiles around Originis (`GameDriver` start zone, `Area.isStartZone()`, `-Dtdf.nospread=true` restores one tile); measured 12 ticks in 218 s vs 2 ticks in 260 s with everybody on one tile; the first-year truce covers the whole start zone.
- Added: `GuiSheets` — themed, non-modal HTML windows for Stats, Locations, Relations, Squad & Sect, Cheats and every `showGuiInfo` text; the Me tab is grouped (Character / People / Places and work / System) and scrollable; Companions opens the Companions tab; `cheat <self|#id> ss|level|potential|kills|weapon|heal|reset|track` for the GUI cheat panel.
- Changed: everyone may tame one beast and use poison/wine/food; only the traits make goods or play (Tamer: Major Lvl + 1 companions).
- Added: goods reach the economy: shop shelves (`Shop.goodsShelf`), auction lots (`Life.auctionLots`), sect delivery missions and sect feasts, Poisoner contracts, festival trade, restocking merchants (`Life`, `Organization.postGoodsMissions`, `Seasonal`).
- Added: `ArtCity` — a painted Originis (walled capital, nine-tier pagoda, great hall, river and bridge), skylines for towns and cities, temple halls for sects, rivers across river tiles, spring blossoms / autumn leaves / winter snow; `ArtGen.Opts` gained `river/season/weather/settlement/sect`, `PlaceImages.painted` fills them (weather exists but is not driven yet).
- Changed: `Ambient` rewritten: a `Composer` writes pieces (title, key and mode, tempo, a motif that returns in four sections over a chord progression; guzheng/pipa lead, dizi answers, erhu glides, pad, bells, chimes, taiko and wood block by mood), played by the built-in 44.1 kHz stereo synth (two decorrelated reverbs, per-voice pan) or, with a `.sf2`/`.dls` in `soundfonts/`, by the Java MIDI synthesiser (`MidiOut`, `soundfonts/programs.txt` maps instruments to presets). Moods 0-6 (open, city, danger, night/winter, war, sect, Originis). `music` command, Settings "Sound source", song title in the status-bar tooltip.
- Added: name engines in `Naming`: dishes with a method and the cook's signature, vintage wines with their year and poetic names, named poisons and instruments, pet names for tamed beasts, clan names and land-named sects, per-city festival names, tournament names, war names (same whichever side tells it), beast-tide and trial names, century eras, song titles (shared with `Ambient`), culture styles for city names.
- Fixed: a patch collision had merged "Battle Rival" / "Battle Arch-Enemy" / "Revenge" into the "Poison Contract" handler (`Character.targetQuests`), so rivals and avengers never fought (caught by PcProbe S5); they call `partyBattleCheck` again and Poison Contract has its own case.
- Tests: probes updated for the start-zone spread (truce, crowded tiles, NPC arrays, goods lots), the trait rules and the non-modal windows (the GUI probe now records and closes the themed sheets); full suite green, 100k smoke test clean. Known flake: PcProbeGui "Move Here walks back" (timing).
- Added: `Regions` — seas, ranges, forests, plains, deserts and rivers get names from the terrain fields (deterministic, nothing saved); shown in `here`, city and sect details, tide announcements.

## 2026-10-03 (traits own their arts, same-character reincarnation, goods economy, city births at 18)
- Changed: the five life-art traits only work for those who have them: only a Chef cooks, a Drunken brews, a Poisoner mixes and uses poison (`poison`, `coat`), a Musician makes instruments and plays, a Beast Tamer tames (`Life.craft` / `play`, `Taming.tame`, command messages); anyone can still eat, drink, be poisoned or inspired; NPCs no longer tame by chance
- Changed: reincarnation keeps the same character (`Reincarnation.perform`): age back to 18, level 1, Major 0, cultivation 0, lifespan of the realms and failed-breakthrough injuries reset, potential +5; name, family, sect, relationships, wealth, items and techniques stay; no mother, no new body; companions beyond the new cap (1) wait for the soul and a "Find Lost Beast" quest appears when there is room; `RunStats.onReincarnation(c)` opens a new life record for the same body
- Added: goods economy — a goods market in every city (`Area.goodsBag`): `market`, `buy market <#> [qty]`, `sell goods|food|wine|poison [qty]`, a Goods tab in the Market window, goods sold by professions and by the "Go Sell" quest go on the shelves, NPCs buy meals, wine, poison (Poisoners) and instruments (Musicians), yearly spoilage, "Festival Trade" quests at festival time, poison sold to Poisoners only
- Changed: city-born cultivators (`Area.age`) arrive at 18 with the cultivation of the years since 8 (`Starts.prehistoryOne`) instead of at 8 with nothing; a Ghost's city rebirth is at 18
- Fixed: `Character.professionPicker` (Friendly) on a tile with an empty character list
- Tested: LifeProbe 85/85

## 2026-10-02 (rules, bug fixes, run stats, coherent biomes, sect rules, festivals and the rest of the list)
- Changed: cities are founded by Major Lvl 2 (was 3): `Character.cityLeadership`, `foundCityNow`, `Settlements`, hints and docs
- Fixed: the status-bar quest-alert and notification buttons (and every other window button) stopped working after the window was closed with its X — a `JDialog` hides on close by default and the singleton helper only raised the hidden window; windows now dispose on close and a hidden one is shown again (`GuiWindows.window`, browser, script editor, character and tile dialogs, recap); unread yearly recaps no longer pile up (one at a time)
- Fixed: clicking a map tile no longer walks the player there; it selects the tile and shows its information (the "Move Here" button in the tile panel walks); the tile-info / tracker / dock dividers get their proportions once the window has a size (tile info was squeezed to a strip)
- Fixed: a loaded (or new) game fills the GUI — map, tile info, status bar, log — before the first prompt (`GameDriver.guiStale`, the player's tile is selected); a loaded game opens a "What happened lately" digest (setting)
- Fixed: a Ghost's true death now ends the life like any other (Ghost trait, ghost form and city rebirth are unchanged and work in Brutal); a reincarnating Ghost stays a Ghost; `Character.event` no longer crashes on a tile with an empty character list
- Changed: Reincarnation needs Major Lvl 3 (Soul Wandering) AND the end of the rope (`lvl >= cap(potential)`); below that `reincarnate` explains why and points at Potential Pills / insights; potential still rises by 5; the soul's companions now WAIT for it (`Taming.waitFor`, `lostBeastIds`, `soulId`) and the reborn gets a "Find Lost Beast" quest (higher priority for a Tamer); they rejoin when found, as many as the cap allows
- Added: sect recruitment rules (`Organization`): craft (profession), potential floor, level floor, alignment, gender, family name; NPC-founded sects roll up to two; masters set them with `sect rules ...` or the Manage window; `join`/`recruit` say why someone is refused; shown in sect details
- Added: run statistics (`RunStats.java`, saved with the world): every life of the human (peaks, kills, how it ended), totals, `runstats`, `halloffame`, `retire`, `export run` (CSV), a Run stats window, the hall of fame file `exports/hall_of_fame.txt` written when a run ends (Brutal death, retire)
- Added: coherent world generation (`BiomeGen.java`): seeded, seamlessly tiling noise fields (elevation, moisture, temperature bands, ridged rivers) decide every tile's biome from its position (order of exploration no longer matters); land next to the sea is always a beach, cold never touches hot, Originis stays a temperate forest; place names follow the terrain (peaks on high ground, streams on rivers), resources follow it (herbs where wet, metal in highlands, more spirit veins on peaks); map colours, ASCII map letters and the world overview use the new biomes; `Map.worldSeed` is saved
- Added: terminal `map level|beasts|people|sect`, `watch ... for <categories>` and `until year <n>`, `digest`; GUI relationship web of anyone, Rivalries tab (sects: members, top level, power, wars, allies, rules; cities), Digest window, a thumbnail picture next to every save
- Added: seasonal festivals in every city of 500+ (`Seasonal.festivals`: Blossom Festival, Martial Fair, Harvest Fair, Lantern Night), yearly sect diplomacy (`Seasonal.diplomacy`: truces, joint gatherings, border quarrels), bounties on recognised poisoners (`Life.bounty`)
- Tested: LifeProbe 77/77 (new rules, waiting beasts, sect rules, biomes, festivals, diplomacy, bounties, map/watch/digest), plus the regression suites below

## 2026-10-01 (Java 8 build fix, Reincarnation, Beast Taming, food / wine / poison / music, ambient music, art, details windows)
- Fixed: build error in IntelliJ — the project SDK is JDK 1.8 and the code used `String.repeat`, `stripTrailing` and `JTextArea.viewToModel2D` (Java 9+/11+) (`PlayerScript.spaces`, `PlayerLists`, `MapViewer`); everything now compiles with `javac --release 8`
- Added: Reincarnation (`Reincarnation.java`) — the "Reincarnate" spot quest is posted when a cultivator reaches the last level their potential allows (the rope: 9 / 19 / 29 ... by the realm gates at potential 20, 37, 52, 65, 76, 85, 92, 97, 100); doing it (quest, `do Reincarnate`, `reincarnate`) ends the body without a death and the soul is born into its own family (a descendant couple, else a sibling's, else a nearby or any fertile couple) at age 8 with +5 potential, half the wealth, the items, half of the best techniques, the same trait and, for a player, control and all settings; past lives are kept (`pastLives`, `lineage`, Family window "Past lives" tab, "reborn xN" in the tree). Brutal does not change it
- Added: Beast Taming (`Taming.java`) — `tame`, `companions`, `release`, `feed`; at most Major Lvl + 1 companions; tamed beasts are real characters that cultivate, stand in their master's tile every week, are listed in the master's party and add a tenth of their level to the master's fighting level; they go back to the wild when the master dies or reincarnates; Tamer trait (+30% taming chance)
- Added: food, wine, poison and music (`Life.java`) — goods (`Character.goods`) made from herbs (`make food|wine|poison|instrument`, `cook`, `brew`), `eat`/`drink` (cultivation bonus; wine makes non-Drunken fighters clumsy and the Drunken fierce), `poison <person>` / `coat` (poisoned blade): victims cultivate slower, fight weaker and may die when the poison outranks them (food of equal level cures it), `play` (an instrument inspires listeners, earns tips in cities); five new traits Tamer, Poisoner, Drunken, Chef, Musician (1.6% each) with NPC professions; goods are sold in cities
- Changed: in MANUAL mode the game no longer swaps a human's gear for them (`Settings.autoEquipManual`, off by default; empty slots are still filled); new `unequip`; humans in MANUAL mode do not auto-eat or drink
- Added: ambient music (`Ambient.java`) — endless generated music (pentatonic drone, plucked strings, flute, gongs, chimes, reverb), never the same twice, follows the player's tile (city, danger, winter); Settings: Music on/off, volume, "New piece", status-bar toggle; quietly disabled without an audio device
- Changed: `ArtGen` — new scenes for whirlpools, hoodoos / stone forests / kopjes, standing stones, terraces and termite mounds, mirage shimmer; every one of the 139 place nouns has a scene
- Added: `city <name>` / `sect <name>` details (lord, shops, libraries, members, war, how to join, history from the chronicle) and a GUI details window (tile info, world overview); Backpack window tabs for food/wine/poison and companions; Settings for gear swapping and music
- Tested: LifeProbe 50/50 (rope table, reincarnation, taming cap and following, goods, poison, music, traits, NPC reincarnation in a running world, id == index preserved)

## 2026-09-30 (Starts and difficulty, naming overhaul, settlements, cave interiors, chronicle + terminal/GUI tools)
- Added: `Settlements.yearly()` (`Settlements.java`) — the number of cities/sects now follows the number of cultivators (1 city per 1,000, 1 sect per 300; founders drawn from M3+/M2+ weighted Leader 8, Merchant 3, Friendly 2; at most 6 cities / 15 sects a year) through `foundCityNow` / `foundSectNow`; hooked before `yearlyDone = true`
- Changed: prehistory — the first generation (NPCs) starts at age 18 with the cultivation of ages 8-18 already done (`Starts.prehistory`, parallel `cultivate()`); the player starts at level 1 (median NPC about level 13)
- Added: character starts (`Starts.java`, `StartMenu.java`) — Protagonist (potential 100, Alchemist), Standard (rolled from the NPC curve, one re-roll below potential 20, potential shown as a word until the first breakthrough), Standard custom (everything chosen, nothing hidden) and 10 Stories (heir, scion, orphan, disgraced, smith, tamer, ghost, lucky, clan, disciple) with a chain of objectives (`Starts.State`, steps REALM/LEVEL/KILLS/WEALTH/CRAFT/FRIENDS/MEET/DEFEAT/BEFRIEND/SECT/FOUND_SECT/FOUND_CITY/DUNGEON/AGE) and rewards; `story` command and Story window
- Added: difficulty tiers (`GameDriver.difficulty`, saved in `SaveData`): Story / Hard (potential x0.6, hidden, half money, no gear) / Brutal (potential 1-20, hidden, no money or gear, hooked stories keep their chain, pure stories are plain survival); hostile world (evil odds `max(30, 100-35*d)`, deathCheck +12*d for fights with the player); Brutal death ends the life (no rebirth, no body takeover)
- Added: `Naming` engine and a regenerated `Dictionary` (biome nouns/adjectives, tiered beast species and fantasy adjectives, names, colours, grade-based item names, array names); unique names for cities, sects, dungeons, shops, libraries, techniques and titles; `Area.geoName` keeps the geographic name for pictures when a tile is renamed; name collisions get a numbered successor (`Naming.unique`)
- Changed: `ArtGen` — pine-bough frames removed; caves are painted from the inside (mouths = count in the name, variants pit/sinkhole/burrow/grotto/cavern/underwater, crystals, pools); all new nouns mapped to scenes
- Added: `Chronicle` (`Chronicle.java`, saved in `SaveData`) — structured world history (deaths of Major 3+/players/sect masters, breakthroughs to Major 3+, cities and sects founded, wars and peace, legends and calamities), death causes, yearly life samples of players and followed cultivators, bookmarks/notes
- Added: terminal commands `map [radius]`, `route <x> <y>`, `time`, `watch [weeks]`, `journal`, `history [text|#id] [kind=...]`, `note`/`notes`/`unnote`, `dash`, `equip <n>`, `compare <n>`, `techniques`, `help <topic>` (`TextTools.java`, wired in `PlayerCommands`); shop and library names shown in lists
- Added: GUI windows (`GuiMore.java`) — Backpack & equipment (gear table with fighting-level comparison, techniques with mastery bars, stores), Chronicle (search, kind filter, "only me", hall of legends), Biography (level / wealth / friends+kills charts + life events; also a Biography button in the cultivator dialog), Notes & bookmarks, World overview (whole map, labelled cities/sects, click for details, Go to / Route), Dashboard tab in Rankings (realms, wealth spread, deaths by cause); hover cards for names in the event log; shortcuts V C J X Y `.` F1; auto-pause rules and a high-contrast theme in Settings (`Settings.pauseOn`, `Settings.highContrast`, `Theme`)
- Changed: `watch` ends on the next notice for the character (`Notifications.wake`), an attack, or a quest alert; hints and recaps do not end it
- Tested (3,000-person scale model): console probe 111/112 (the failing check was a test assumption: `join` now succeeds in Originis because NPC prehistory founds a sect there, so the probe no longer lists it), multiplayer 6/6, QoL console 66/66, rules 22/22, starts 29/29, terminal tools 27/27, chaos 9/9, QoL GUI 53/53 (against a save written by this build), GUI probe 127/128 (known flaky quest-pin colour check), new GUI windows 21/21; 100k smoke test clean (year 1 reached)
- Note: probe scale copies must set `Map.startingPopulation = 3000` after syncing sources (the 100k world makes the probes time out)

## 2026-09-30 (Balance step 1: survival, luck; painted art reads the name)
- Changed: the first generation (players and NPCs) starts at age 18 instead of 8 (`GameDriver.START_AGE`) — they can marry and have children from the first year; cultivators born later still start at 8
- Added: first-year truce — nobody may start a non-sparring fight with another cultivator in Originis until `GameDriver.TRUCE_WEEKS` (52) has passed (`Character.battleCheck`); a human player is told why nothing happens
- Fixed: lost fights between cultivators were ALWAYS fatal — the relationship-based death roll (`deathCheck`: 1% strangers .. 75% arch-enemies) only affected flee/forbidden-technique logic. `winCheck(loser, lethal)` now spares the loser (injured: cultivation x0.7 until the next level-up) when the roll says non-lethal; a human winner may still choose to kill. Effect in the 3,000-person scale model: population at year 30 = 73% of the start (was 24%), minimum 71% at year 20, then recovery; strangers' fights 0 of 120 fatal, arch-enemies 15 of 60
- Added: Luck is real — Lucky cultivators: +10% cultivation on average (1 tick in 10 doubled), +25% loot per kill, a fortune event about once a decade (+1 potential, gear one Major above, a windfall of 10-30 weeks of cultivation, or a technique breakthrough; "Good fortune" notification), and they are liked more easily; Friendly cultivators now double good feeling and halve bad feeling on either side, Lucky x1.5 (`socialBonus`, `fortuneEvent`)
- Added: `ArtGen.Opts` — painted pictures now read the place name: a number word paints that many (Eight Giant Footprints = 8 prints; also peaks, islands, springs, ponds, groves, arches, bridge spans, falls, cave mouths), mood words add motifs (dark/night: moon + dead trees, blood: red moon, fire: embers and glow, ice: snow, holy: light rays, toxic: spores, lush: flowers, haze: mist), level 40+ adds spirit rays and level 55+ floating islets; props (pagodas/pavilions on ridges, cranes, birds, boats, hermits, pine-bough frame); rim light on ridges; 2x supersampling with a Kuwahara oil-paint filter (`ArtGen.kuwahara`, summed-area tables)
- Added: `Naming Review.md` (dictionary audit and proposed additions/removals; nothing applied yet), `Character Starts.md` (proposed story starts and difficulty tiers), balance decisions recorded in `Balance Plan.md`
- Tested: rules probe 22/22 (age 18, truce, lethality, luck, art options), console probe 112/112, multiplayer 6/6, QoL console 66/66, QoL GUI 53/53, GUI probe 127/128 (known flaky pin check)

## 2026-09-30 (FlatLaf + ink-and-jade theme, layout redesign, painted place art, balance plan)
- Added: `Theme` — FlatLaf dark look with an "ink & jade" xianxia palette (jade accent, gold rules, cinnabar seal, serif title, 仙 seal banner). FlatLaf (`lib/flatlaf-3.7.2.jar`, Apache 2.0, from Maven Central, SHA-1 checked) is found at run time through reflection (`./lib`, the folder next to `src`), so `javac -cp src` still compiles with no dependency; without the jar the game falls back to a themed Nimbus. Colours/fonts in `MapViewer`, `GuiWindows`, `GuiExtras` now come from `Theme`; `.iml` lists the jar as a library for IntelliJ
- Changed: main-window layout — title banner + speed controls in one header with the current speed highlighted; the wall of buttons is an action dock of four tabs (Move d-pad + command box + move-to-tile, Me, Auto, World); Tile Info over NPC Tracker on the right with flexible rows; map is centred in its panel (`offX/offY`), always fills the viewport (no scrollbars), has a gold frame, ink-wash tile colours (`Theme.tile`), rounded-chip layer toggles, a Legend popup and a hover tooltip (name, coordinates, level, cultivators, beasts); larger status strip (serif name, bell, save indicator); thumbnails from tile size 12 (was 18)
- Added: `ArtGen` — painterly place pictures painted on demand: watercolour washes (recursively deformed polygons stacked at low opacity), dry-brush strokes, atmospheric perspective, paper grain, red seal; 16 scenes cover the 76 place nouns, biome picks the palette, the seed picks the composition. `PlaceImages.painted()` uses it whenever there is no hand-made file (files in `images/places` still win); big views are unique per tile (seed = `artSeed` + tile index), thumbnails share one picture per noun+biome; `Settings`: `paintedArt`, `artFresh` (new pictures every launch), `artSeed`; Settings window + `settings art|artfresh|reroll`
- Added: `tools/BalProbe.java` (per-year balance CSV + realm/lifespan summary) and `Balance Plan.md` (findings and phased plan; nothing in the game was rebalanced)
- Tested: console probe, multiplayer probe, QoL console probe 66/66 (incl. painted art), QoL GUI probe 53/53 (FlatLaf active, map centred, hover tooltip, dock tabs), chaos stress (200 s, 356 saves / 120 loads under load: no deadlock, no exceptions), GUI probe 127/128 (orange quest-pin pixel check is flaky), no-FlatLaf fallback builds windows

## 2026-09-30 (Race-free GUI, save slots, settings, notifications, QoL windows)
- Added: `World` — one fair `ReentrantLock` owned by the game thread; it is released during EVERY wait (input, pause/sleep, GUI dialogs via `GameDriver.edtWait`, save-thread join) and at the end of each tick (`World.handoff` posts a refresh and waits <=250 ms), so GUI readers (`World.read/readWait`, `tryLock` with a timeout, never an unbounded wait) see a stable world and never block the game; map painting is buffered with last-frame fallback. Writers never run on the EDT: GUI buttons only queue commands. `GameDriver.frozenNpcs` (test hook) freezes NPC/monster turns for deterministic probes
- Added: `SaveManager` — named slots in `saves/` (`.dat` + `.meta` sidecar + `.bak` of the previous good copy), temp-write + CRC + atomic move, rotating autosaves (`auto-1` newest .. `auto-N`, N = Settings), load falls back to `.bak` when the checksum/read fails, crash handler writes `crash.log` + a `crash-<time>` slot, legacy `save.dat` is still offered; start-up menu (console or GUI) lists slots; commands `save [name]`, `saves`, `load`, `delsave`; in-game load happens at the top of the next tick
- Added: `Settings` (`settings.properties`, git-ignored): autosave years/keep, text size, colour-blind map, pictures, tooltips, hints, confirmations, toasts, sound, muted notification categories, recap interval; `settings` command and GUI Settings window
- Added: `Notifications` (breakthrough, level, death of people you know, war, family, quest alert, danger, auction, hint, recap) -> `[Notice]` log line + history + GUI toast + bell badge (+ optional beep); `Hints` (one-time contextual tips + 8-page tutorial, first-run welcome window); `Recap` (per-human baseline, popup every N weeks: level/wealth/kills/notices/world stats)
- Added: combat preview (`Character.previewFight`, `preview <#id>`; shown inside the accept-duel and flee prompts); confirmations (`PlayerCommands.confirm`: leave sect, disband, declare war, delete pinned/own quest, mute, attack with bad odds, clear a long script, control, save overwrite/load/delete; `!cmd` = already confirmed; commands arriving from a script/AUTO count as confirmed)
- Added: `find`, `follow`/`unfollow`, `control <id>` (keeps duties/policies via `PlayerControl.inheritFrom`), `family`, `relations`, `notices`, `recap`, `tutorial`, `export` (CSV of any list / history / rankings) commands; `Family` (tree + relationship ties data), `Exporter` (CSV + PNG into `exports/`)
- Added: GUI — Saves window, Settings window, Family tree + relationship graph windows, Notifications window + toasts, tutorial/recap windows, save indicator and bell in the status bar, Browse "Search anyone" tab (name or #id; Follow/Control/Seek/Preview/Go there/Export), Export menu (map PNG at 3 sizes, window screenshot, event log, history/rankings CSV), Rankings window chart PNG/CSV buttons, button tooltips (`Ui`), live text-size scaling, colour-blind palette, `F`/`N` shortcuts; GUI confirmations use `GuiExtras.confirm` then send `!cmd`
- Changed: the GUI Control button and Search Control go through the `control` command (game thread does the switch) instead of writing `player` from the EDT; right-hand button column scrolls inside a divider so Tile Info / Tracker keep room; PlaceImages README gains a `_missing.txt` section (rewritten when outdated) and logs biome variants not yet drawn
- Tested: console probe 112/112, multiplayer probe 6/6, QoL console probe 58/58 (settings, slots, corruption fallback, crash slot, autosave rotation, confirmations, find/follow/control/preview, notices, recap, export), QoL GUI probe 49/49 (start-up menu, tutorial, saves, settings, family, notifications, search, exports), lock stress (75 s at 0 ms with 8 windows open: 1822 ticks, EDT p99 97 ms, no deadlock), GUI probe 127/128 (the orange quest-pin pixel check is flaky), 100k smoke test clean

## 2026-09-30 (Place pictures, GUI windows, layers, shortcuts, random targets)
- Added: commands with no person (`talk spar attack invite recruit`) pick one suitable person in the tile at random and say who (`PlayerCommands.pick`); never a child for spar/attack, never a friend/spouse for attack
- Added: place pictures (`PlaceImages`; art is user-made and git-ignored: `.gitignore` -> `images/`). A place name is `[number] [adjective] NOUN [of X]`; the noun (76 of them) and the biome pick `images/places/<noun>_<biome>.png` then `<noun>.png` (512x288 recommended); missing/corrupt files just draw nothing. Adjectives and "of X" map to colour moods (dark fire ice holy arid lush haze blood toxic night vast; editable in `images/moods.txt`); season, area level, level-hotspot and crowd-hotspot tint the picture; cities, sects, dungeons and Originis are drawn on top from `images/overlays/` (`city sect originis dungeon_<type>`); each tile gets a seeded mirror/crop/brightness variation; results are cached. `images/places/README.txt` (auto-written) lists every file name; `_missing.txt` lists nouns seen without art
- Added: GUI — Tile Info picture banner (click = large view; double-click a map tile too), status bar (name, realm, cultivation bar, age, wealth, mode/duties, alert badge, place, active quest), map layer bar (colour modes Terrain/Beasts/Population/Level; toggles Pictures, Tile pics, Crowd, Fog of war, Quest pins, Path), Quests+ panel (table, pin/unpin/delete/mute/unmute/Do Quest/new waypoint, filters), Duties+ panel (ten duty checkboxes, master buttons, policy controls, timeout, list rows), Manage window (shop rate sliders + open/restock/sell, city dashboard + actions, sect dashboard + member table + promote/war/peace/alliance), Rankings & Charts (strongest/richest/most kills/oldest with double-click to open; population, cities/sects/shops and realm charts from `GameDriver.statHistory`), Market (auction lots + library learn/sell), event-log filters (Fights / Economy / World / Me / Other + search) with click-a-line-to-open-the-named-cultivator (the event log now keeps 400 lines), keyboard shortcuts (WASD/arrows, Space, I L R H P, Q U B M K G T O, Ctrl+S; the Keys button lists them)
- Added: quests have stable `uid`s; `qp`, `qu`, `qd`, `qdo` accept `#uid` so GUI actions never hit the wrong row (`Quest.uid`, `PlayerControl.rowRef`, counter restored on load)
- Tested: image unit checks (34), GUI probe (128 checks incl. pixel checks of layers/pins/fog/crowd/path, picture banner and tile pictures), console + multiplayer probes

## 2026-09-30 (Selection lists, id targeting, shop buying, map flip)
- Added: `list people|monsters|shop|bag|places [limit] [filters] [sort=key desc]` (also `people`, `players`, `monsters`). One filter/sort/limit language (`ListQuery`): a bare number is the limit, `key=text` matches, `key>=n` `key<=n` `key>n` `key<n` compare numbers (`5k`/`2m` suffixes), bare words filter by name. `limit <n>` (1-500, default 20) sets the per-character default; a number on a command overrides it once (`PlayerLists`, `PlayerControl.listLimit`)
- Added: pick anyone by id — every person argument (`talk spar attack invite recruit promote gift do`) accepts a list number or `#<id>` (same tile); an id elsewhere explains where they are and offers `seek <id> friend|fight|spar [priority]`, which pins a quest (Befriend / Duel / Spar) that AUTO, scripts (`quest Duel`) or Do Quest carry out
- Added: `buy <#>` / `buy <shop>.<item> <name>` (name checked so a changed shelf is never bought by mistake) and `sell <backpack#>`; `list bag` shows backpack slot numbers
- Added: GUI "Browse" window over the same lists (tabs Cultivators / Monsters / Shop items / Backpack / Places; filter widgets, sort box, clickable headers, limit spinner; actions Talk, Spar, Attack, Invite, Seek x3, Copy ID, Buy, Sell, Go there, Add waypoint)
- Changed: the map is drawn north-up (+y at the top; the North button / `w` moves the marker up), clicks map back, "N ↑" compass (`MapViewer.MapPanel`)

## 2026-09-30 (Crowd threshold scaling, Originis exemption, auction bidding, decision timeout, script editor)
- Changed: crowd-hotspot capacity now derives from the STARTING population and the map area instead of the fixed 100/400: wild tile = `startingPopulation * CROWD_PERCENT (2%) * (31*31 / range^2)` (2000 at 100k on the 31x31 map; 60 in the 3,000-person test world), floor 20; cities are 4x. Never uses the current population (`GameDriver.CROWD_*`, `Area.crowdCapacity`)
- Changed: Originis can never be a crowd hotspot; `Area.crowdCheck` also lifts a hotspot an old save carried (`Area.isOriginis`, `endCrowdHotspot`). Tile info shows "Cultivators: n / cap" and an Overcrowded warning
- Added: hand-bidding at the yearly auction — extracted to `GameDriver.holdAuction`; a MANUAL human standing in the city who can afford the reserve is asked for a maximum bid per lot (0 passes; winner pays the runner-up's bid, capped by wealth). NPCs, tracked and AUTO characters bid their whole wealth; a HYBRID character never bids by itself. `auction` command lists lots and time to the next auction; a notice prints 3 weeks ahead (`DecisionBroker.askNumber`)
- Added: decision timeout — `policy timeout <seconds|off>` (per character, default off): an unanswered prompt (console or GUI dialog, including bids) falls back to the policy answer (`GameDriver.readInputTimed`, `DecisionBroker`, `PlayerControl.decisionTimeout`)
- Added: GUI script editor window (Scripts button): live step list, templates/free text, Add step, Delete selected, Clear, Run, Resume, Stop; every edit is a `script ...` command (`MapViewer.showScriptEditor`)

## 2026-09-29 (Player-control system: manual / auto / hybrid modes, quest tools, decisions, duties, scripts)
Implements `planning.md` (all six phases). New files: `PlayerControl.java`, `PlayerCommands.java`, `DecisionBroker.java`, `PlayerScript.java`, `QuestQueue.java`.
- Added: per-character `PlayerControl` (`Character.pc()`, created lazily for `player == 1`): mode MANUAL/AUTO/HYBRID, ten managed-duty toggles (city, sect, shop, craft, upkeep, bounty, tournament, social, family, loot) + `duties all on|off`, decision policies, muted quests, quest-alert inbox, script. `player == 2` (tracked NPC) now runs the plain NPC pipeline (`GameDriver.npcTurn`) instead of a random walk; `Character.player` still marks human/tracked
- Changed: `GameDriver` Phase 2A/2B rewritten around `npcTurn`, `playerTurn`, `manualTurn`, `runOnItsOwn`, `syncMotherChildren`. MANUAL prompts for a command line (`readLine()`); info/setup commands cost no time (used to end the turn); AUTO/HYBRID poll for `stop`/setup commands (`pollInput()`); an attack cancels an autopilot walk/wait
- Added: MANUAL/HYBRID suppress the NPC routine. `quest()` only runs quests whose duty is on (`pollAllowed`, `PlayerControl.allowedToRun`); quests classified M (routine) are ignored, A (alert) and D-with-toggle-off are announced once with target, coordinates, distance and reason (`scanAlerts`, max 3 printed per turn, `inbox` lists all; unanswered one-time spot alerts lapse after 26 weeks). Classification table = planning.md 15b (`PlayerControl.classify`); "Get Pregnant" displays as "Have Child" (`Quest.displayName`). Immediate NPC-style side effects (pill use, gear buying, selling surplus, library study, promotions, crafters' gathering, leader founding) are gated by their duty (`Character.autoActs`)
- Added: quest tools — numbered `q` view, `qp <n> <pri>` (pinned: `Quest.pin`, never ages, is never clamped/expired/shed — `bump`, `maintainQuests`), `qu`, `qd`, `qdo <n>` (Do Quest), `mute/unmute <name>` (`QuestQueue` refuses muted names so a deleted quest stays deleted), `qnew <label> <x> <y> <pri> [repeat]` (player waypoint quests, "Go: label"). Do Quest (`Character.doQuestNow`) checks viability and that the player is at the target, explains why not, then runs the same handler and result bookkeeping as `quest()` — the bookkeeping was extracted into `finishTarget` / `finishSpot`
- Added: `DecisionBroker` — one prompt at a time (console numbered/first-letter, GUI option dialog); a character not in MANUAL gets the answer from its policy (kill: always spare / evil only / if wronged / always kill; flee; forbidden technique; duel accept/evade). Hooked into `winCheck` (kill or spare — spare simply means "don't kill"), `fleeCheck` (`fleeRoll` holds the old formula; fleeing can fail), `forbiddenCheck`, and `battleCheck` (a challenged human may try to slip away; `acceptChallenge`)
- Added: `PlayerCommands` — the manual verb set: movement (`goto`, `wait`, `walk`, legacy `o`), info (`here`, `people [name]`, `bag`, `shops`, `city`, `sect`, `party`, `library`, `inbox`), social (`talk spar attack invite leave disband`), work (`forage mine craft meditate hunt sell buygear consume install learn`), city/sect (`found city|sect`, `join`, `resign`, `recruit`, `promote`, `war`, `peace`, `alliance`), `do <quest>` for on-demand duty actions, `price <shop#> buy|sell|city <rate>` (player-set shop rates with safety bounds), policy/duty/script commands. Old keys `w a s d i l r q e z f p o` still work
- Added: hybrid scripts (`PlayerScript`): steps goto/wait/forage/mine/craft/meditate/hunt/sell/quest/repeat/if/end/mode with `until` conditions; interrupts on attack, breakthrough, tile depleted, missing target, optional new alert; control returns to MANUAL (or AUTO with `policy script auto`)
- Added: NPC diplomacy aimed at a human character asks first (tribute demand — refusing means war, alliance proposal, sect recruitment, defector offer) through the same broker; script `hunt` stops after 5 turns with nothing suitable to fight; `equip` and `gift <person#> <item#>` commands, numbered `bag`
- Added: GUI buttons Auto / Manual / Stop / Alerts / Do Quest / Duties / All on / All off / Here / People / Help plus a command box (`MapViewer`)
- Changed: reincarnation and Ghost rebirth/possession carry duty toggles, policies and muted quests to the new body (`PlayerControl.inheritFrom`, `Character.takeControlFrom`); scripts and alerts are dropped. `Character.sell()` skips a shop paying under the city pool rate (players can now set low rates). `pathfinder` split into `pathfindXY`; a manual PC's duty quest only walks on ticks the player stood still. Disperse now also posts for human characters
- Tested with headless probes (scratch copy, 3000 population, driving the real loop through a controllable stdin; one single-player probe with ~70 checks and one two-player probe): info commands free, mundane suppression over 300 idle turns, duty toggles, pin/delete/mute/waypoint, alerts, Do Quest with the real prompt, kill/spare, AUTO run and `stop`, script run/interrupt/unbalanced, shop rates, policies, save file written, reincarnation carry-over, 600-turn all-duties soak; `id == index` held throughout

## 2026-09-29 (Sect raid cooldown, city economy caps, crowd hotspots, full-codebase bug review)
- Changed: sects launch at most one "Sect Raid" per 13 weeks (`Organization.nextRaidWeek`, `RAID_COOLDOWN_WEEKS`); it was issued every tick by every elder during a war (37% of all target-quest runs) (`Character.java`, `Organization.java`)
- Changed: city population is now bounded — 0.1%/tick growth toward `populationCap()` = 100k × cityLevel² (was 1%/tick with no ceiling, saturating int and founded at up to 1.4 billion); capital starts at 100k–1M, new cities 20k–200k; yearly tax is 1% of population (half to owner, half to treasury). Chose fixing the population variable over only lowering the tax because population feeds tax, auctions, blood, plunder and casualties (`Area.java`, `Character.java`, `GameDriver.java`)
- Added: overflow stoppers — monsters/herbs/metal growth capped (`MAX_MONSTERS` 50M, `MAX_MATERIALS` 1B) with a +1 floor for small counts, population clamps, `convertCurrency()` uses division (a huge balance used to loop billions of times), `Character.addToBag()` clamps stack quantities (`Area.java`, `Character.java`)
- Changed: overcrowding no longer culls cultivators yearly. A tile over capacity (100, cities 400; after year 10) becomes a hotspot (+15 level surge) and beasts ambush its cultivators every tick — level-blind, lethal if they win, 2% of the excess per tick (max 40), until the crowd drops under 80% of capacity. Array-protected tiles are exempt. Ambush beasts never awaken, destroy cities or stay on as roaming predators (`Area.crowdCheck/crowdAmbush`, `Character.ambusher`, `winCheck`)
- Added: "Disperse" quest — cultivators in a crowd hotspot head for a nearby tile under half capacity (`Character.java`)
- Review (5 parallel read-only reviewers over the whole codebase, every finding verified before fixing). Fixed:
  - `id != index` corruption: awakened Monsters were appended to `characters` while births were pending (`awakenMonster` now queues via `tempCharacters`); newborns built parent/sibling relationships before their id was assigned, so everyone got a friendship with character #0 (`Character.java`)
  - Quest engine: target result no longer overwritten by the spot result; failed one-time spot quests are dropped instead of freezing at priority 0 and blocking their own trigger; "Return to Sect" and "Find Parent's Body" one-time; "Possess Womb"/"Ghost City Rebirth"/"Restock Trip" viability checks; human ghosts carry over to the possessed child
  - Sect missions paid the reward without delivering goods; mission levels above 10 (unfillable) were posted; missions survived leaving a sect; Hunt missions counted non-fights; new stacks lost a unit (`donate - 1`)
  - Combat: forbidden-technique deaths gave no kill credit (`forbiddenCheck` returns a flag); a stuck `sparring` flag; spars broke arrays and escalated sect wars; siblings were inferred from two null parents; `interact` kept going after a death; failed tribulation set the wrong level and kept the lifespan bonus; True Immortal rebirth applied to beasts; unborn children weren't killed with their mother; dead Monsters lingered in tiles
  - Economy: Blood pills cost the wrong amount (stack went negative); pill scale ×10 made everyone sell/donate the pill they needed next; merchant gear swap duplicated items; stackable merging (no `break`, non-stackables merging, `destroyCity` unmerged stacks); shops kept by merchants after their city was destroyed; loot transfer dropped sc/dc/ij; buyGear's `ss <= price*4` blocked wealthy buyers; library re-bought known techniques and paid sellers out of thin air; "Seek Potential" bought a whole stack for one unit's price
  - Sects: founder/master left pointing at a destroyed sect; succession crisis lasted 52 years (timer is yearly); `promote()` could crown a corpse and wipe a newer sect on the tile; one-sided wars and stale alliances
  - World: yearly hotspot reset / storm / crowd surge corrupted `lvl`/`baseLvl`; small monster/herb counts never grew; CELESTIAL_TRIAL could spawn as a permanent dungeon; auction charged the winner their entire fortune (now runner-up price, money to owner/treasury); `deadCharacters` leaked; dead NPCs kept moving/questing (re-adding themselves to tiles); beasts killed mid-tick kept fighting; reloaded saves re-ran the year's events; saves could race the running game (now requested via `saveRequested` and written between ticks to a temp file then moved); GUI iteration races; player click-move used inverted y; empty-world loop never advanced time
  - Follow-ups fixed: payments to a city now go through `Area.receivePayment()` (living lord, else `cityTreasury` — a dead lord used to swallow pill/cargo/library/restock/auction money); "Sell Stock" viability uses the same `sellableCargo()` as its handler (spare gear was counted as cargo); array defender boost no longer applies to beasts (a hunter in an array tile strengthened its prey); the NPC-chooser dialog is created on the Swing event thread

## 2026-09-29 (Quest queue overhaul, city levels, population control)
- Fixed: a repeating target quest was re-queued but never released, so `currentQuest` locked a character onto its first repeating quest (chasing a friend, fighting a rival) until the target died and queue priorities were never consulted again; repeating quests now release after each success and the queue's top priority picks the next trip (`Character.quest()`)
- Fixed: quest priorities were edited in place inside a `PriorityQueue` (heap silently mis-ordered; ~16% of polls returned a non-top quest) and `maxPriority` was never enforced (priorities reached ~3M). New `Character.bump()` ages quests by `priorityDiff`, clamps to `maxPriority`, and rebuilds the heap (`Character.java`)
- Added: relationship quests are tiered and in the lowest band, but climb to a cap so they still run eventually: Like 10/+1/34 and Dislike 10/+1/34 (one-time, capped at 4 each and replaced with a *different* cultivator of the same relationship value on completion or when the relationship dissolves); Friend 14/+2/40, Close Friend 18/+2/46, Rival 18/+2/42, Arch-Enemy 24/+3/48 (persistent). One social quest per person, resynced when the relationship tier changes (`syncSocialQuest`, `refillWeakSocialQuests`)
- Added: yearly per-character `maintainQuests()` — clamps old runaway priorities, expires one-time quests older than 10 years (`Quest.createdAt`), hard-caps the target queue at 50 shedding the lowest priority first (`Character.java`)
- Changed: placeholder spot quest "Test" (27% of spot turns, did nothing) replaced by "Meditate" (15/+2/40) which triggers an extra cultivation tick; "Test" kept as an alias for old saves. "Find Parent's Body" is now one-time (`Character.java`)
- Added: city level 1–10 from `cityTreasury` scaled like item prices (`Area.cityLevel()`, ~18k for L2, 49k L3, 177k L5, 1M L10); a city hosts 3 shops per level; a shop's level = min(city level, owner major realm) and it only stocks items up to level×10 (higher items go to the city pool / auction); shops of dead merchants are removed yearly (`Area.java`, `Shop.java`, `Character.java`, `GameDriver.java`)
- Fixed: `IndexOutOfBoundsException` in `questViable` when a quest targeted a newborn still in `tempCharacters` (quests are polled far more often now) — out-of-range targets are treated as not viable (`Character.java`)
- Fixed: city `population +=` in Go Sell / Sell Stock overflowed int negative, making the yearly tax negative and driving `cityTreasury` to -280M; increments are clamped to `Integer.MAX_VALUE` and tax uses `max(0, population)` (`Character.java`, `GameDriver.java`)
- Fixed: NPE in "Sell Stock" (year ~74) merging into a city pool holding a broken item; `Item.deconstructor()` now leaves the name as "Shattered relic" instead of null, and the yearly cleanup purges broken items from city pools and shop shelves (`Item.java`, `GameDriver.java`)
- Added: crowding deaths — after year 10, tiles above 100 residents (cities 400) lose 30% of the excess per year, the weaker of two random residents dying; births untouched (`GameDriver.java`)
- Added: epoch purge — at 200k living cultivators, 10% of each major level dies in batches of 3000/tick; levels with fewer than 10 cultivators are spared (`GameDriver.java`)

## 2026-09-28 (Economy flow, exploration fix, hunting parties)
- Changed: sect founding needs Major Lvl 2 (city stays Major Lvl 3); a Leader can found a sect without owning a city (`Character.java:sectLeadership/quest`)
- Added: crafters (Blacksmith/Alchemist/Scribe/Tailor/Innovative/Merchant, non-player) run `professionPicker()` 1/3 of ticks from `quest()` so goods, techniques and shop upkeep actually happen (`Character.java`)
- Added: characters in a city with >=5 backpack items sell surplus (20%/tick); "Go Sell" travel trigger lowered from >20 to >8 items (`Character.java`)
- Changed: removed per-tick `population` ss payout to city owners (`Area.age()`); yearly tax is now population/500, half to owner, half to `cityTreasury` (`Area.java`, `GameDriver.java`)
- Changed: beast loot `ss = lvl^2.5 * 10 * (1 + potential/100)` instead of `100^((lvl+potential)/20)` — tracks item prices so hunting funds gear at every tier (`Character.java` beast constructor)
- Fixed: characters stopped exploring because empty shops still pulled every gearless NPC to the shop city via "Buy Gear"; the quest now only fires for a city whose shops stock an affordable upgrade (`hasBuyableGear`) (`Character.java`)
- Changed: "Hunt Beast" spot trigger targets areas from own major realm down to 2 realms below; same-realm targets rally a party (`tryRallyParty(true)` also invites unrelated non-hostile adventurers within 1 major realm)
- Fixed: repeating (non-oneTime) "Hunt Beast" target quests monopolised every character — spot quests (Profession, Mine, Forage) never ran and everyone farmed lvl-1 beasts at Originis (3.4k chars, 5M kills, 98% never left). Spot-trigger hunts are now oneTime and pick a random eligible known area (`Character.java`)
- Changed: `forage()`/`mine()` yield 10–100 per action (was 1–10) since crafts cost 1000 materials; crafters gather directly each tick when short instead of queueing starved Forage/Mine spot quests (`Character.java`)
- Added: wanderlust exploration — each non-party NPC starts a 6–25 tile trek in a random direction ~every 60 ticks (`exploreTicks`/`exploreDir`); target-quest travel pauses while exploring (`Character.java`, `GameDriver.java` Phase 2B)
- Fixed: broken gear stayed in the backpack with a null name after `deconstructor()` and crashed `sell()` (NPE) once trading became common; broken items are now removed from the backpack and `sell()` purges stale ones (`Character.java:durabilityCheck`, `sell`)
- Fixed: autosave threw `StackOverflowError` (year 10) once the world was explored — serialization recurses through knownLocations <-> Area.characters; `save()`/`load()` now run on a 1 GB-stack thread and catch `Throwable` so a failed save can't kill the sim (`GameDriver.java`)
- Fixed: `ConcurrentModificationException` (year ~16) — an Evil bully's "Battle Dislike" could target its own child; `battleCheck` redirects child fights to the mother (= the attacker), so it duelled itself and `transferAllItems` looted itself mid-iteration. Parents no longer duel their children and `transferAllItems` ignores self-transfer (`Character.java`)
- Fixed: NPE in "Sect Raid" (year ~54) when the raider died in the fight and lost its sect before the war-status check (`Character.java`)
- Fixed: NPE in "Donate to Sect" (year ~49) — deconstructed gear left in a backpack was donated into a sect treasury with a null name; breakage now removes every reference from the backpack and donation purges null-name items (`Character.java`)
- Added: `partyBeastHunt()` — leader + co-located followers fight the beast in turn (beast tires each round); hunting parties persist across repeated hunts (5%/round chance to disband)

## 2026-09-28 (Economy bootstrap: ownerless cities trade, founding by Major Lvl)
- Changed: `cityLeadership()` / `sectLeadership()` gated on `majorLvl >= 3` instead of ss (`Character.java`) — 1M ss was unreachable
- Added: Leaders with `majorLvl >= 3` attempt `cityLeadership()`/`sectLeadership()` every tick from `quest()` (`Character.java`) — the Profession spot quest was picked in ~0.06% of leader-ticks, so founding effectively never rolled
- Test (3000-pop scratch copy, forced 30 Leaders to Major 3): Originis got an owner and a sect; shops formed (0 -> 19 in 105 yrs unforced). `sell()` was never called and no gear/technique was ever bought or sold: shop stock stays empty because Go Sell needs >20 backpack items and crafting/technique creation run through the same rarely-picked Profession quest
- Found: an owned Originis (population up to 1e8) pays its owner `population` ss every tick in `Area.age()` -> owner ss reached ~1e12 within ~30 years; needs rebalancing
- Changed: removed `cityOwner != null` requirement from Go Sell, Restock Trip, Sell Stock, merchant shop setup, and sell/restock city finders so Originis and other ownerless cities function as markets (`Character.java`)

## 2026-09-28 (Bounty realm range, sect founding cost)
- Changed: Beast bounty pickup accepts areas from 2 major realms below to 1 above the cultivator's (`areaMajorLvl` in [majorLvl-2, majorLvl+1]) (`Character.java`)
- Changed: `sectLeadership()` threshold 100M ss -> 1M ss, matching city founding (`Character.java`)
- Found (headless probe, 3000-pop copy, 53 sim-years): max ss ~8k, zero characters near 1M, no city owners/sects/shops ever formed; only Originis' library got 1 technique. Economy is gated on a Leader reaching 1M ss (Go Sell / Sell Stock / merchant shops all require `cityOwner != null`) — bootstrap deadlock, not yet fixed

## 2026-09-28 (Party system refinements + bounty/array polish)
- Changed: Party leave condition — follower abandons party only when their top quest's priority strictly exceeds the leader's current quest priority (not a fixed threshold); keeps followers committed when the party's mission is equally or more urgent (`GameDriver.java`)
- Changed: Monster bounty generation moved from yearly to per-tick; `alreadyPosted` guard prevents board flooding; all nearby areas within 2-tile radius are listed, not just one (`GameDriver.java`)
- Changed: Beast bounty pickup now filters by major realm (±1 major level of the area's danger); same-major-level pickups immediately call `tryRallyParty()` so the character recruits allies before departing (`Character.java`)
- Changed: "Donate Array" execution awards 10 sect points per donated array (Scribe contribution recognition) (`Character.java`)

## 2026-09-28 (Party system, array quests, monster bounties)
- Added: `partyLeaderId` field on `Character` — id of leader being followed; -1 = not in party (`Character.java`)
- Added: `joinParty`, `leaveParty`, `tryRallyParty`, `dissolveParty` methods on `Character` — party formation and cleanup (`Character.java`)
- Changed: Phase 2B NPC loop — followers use `pathfinder(leader.location)` instead of random walk; quest runs only when co-located with leader; urgent personal quests (priority ≥ 60) break party (`GameDriver.java`)
- Changed: Leaders call `tryRallyParty()` (25% chance/tick while pathing) and `dissolveParty()` on quest completion (`Character.java:pathfinder section`)
- Added: `leaveParty` + `dissolveParty` called in `dead()` so death cleans up party state (`Character.java`)
- Added: "Buy Sect Array" spot quest — sect masters buy arrays from own pocket or `sectCash` treasury (`Character.java`)
- Changed: "Buy City Array" now also draws from `cityTreasury` if master lacks personal funds (`Character.java`)
- Added: `cityTreasury` on `Area`, `sectCash` on `Organization` — funded by yearly tax/population splits (`Area.java`, `Organization.java`, `GameDriver.java`)
- Changed: Scribe `case` — holds arrays in `arrayBag` when sect member; queues "Donate Array" target quest to deliver to sect location; donates all at once on arrival (`Character.java`)
- Added: "Donate Array" target quest validity check and execution handler (`Character.java`)
- Added: Yearly monster bounty generation — cities post "Hunt Beast" target quests for nearby areas (2-tile radius) with monsters (`GameDriver.java`)
- Changed: Beast bounty pickup — NPCs at cities pick up "Hunt Beast" bounties within 15 levels of area level (`Character.java`)
- Changed: "Hunt Beast" target quest — rewards clearing an area (ss bonus when monsters = 0) (`Character.java`)

## 2026-09-28 (Dungeon depletion — BEAST_LAIR and LOST_RUINS now clear)
- Added: `dungeonCharges` field on `Area` — tracks remaining exploration visits for LOST_RUINS (`Area.java`)
- Changed: LOST_RUINS initializes `dungeonCharges = max(3, lvl/2 + rand(lvl/2+1))` on spawn; decrements each visit; clears (`dungeonType = null`) when exhausted (`Area.java:284`, `Character.java:Explore Dungeon`)
- Changed: BEAST_LAIR clears when `monsters <= 0` after a hunt visit — hunted-out lairs disappear from the map (`Character.java:Explore Dungeon`)
- MARTIAL_TRIAL and HAUNTED_GROUNDS remain permanent (training grounds; no depletion mechanic)
- CELESTIAL_TRIAL and Heaven-Sent Treasure already temporary (unchanged)

## 2026-09-28 (Find Person quest duplication — fix 2500+ quest accumulation)
- Fixed: `changeRelationship` had three independent add-points for "Find Person" quests; under parallel or same-tick processing two characters could both observe "no quest exists" and each add one, accumulating thousands over time (`Character.java:changeRelationship`)
- Fixed: removed unconditional adds at new-relationship creation (lines 2843/2847) and the entire first dedup block (lines 2850–2865) from `changeRelationship`; quest management is now consolidated into `checkRelationship` only
- Fixed: `checkRelationship` else branch now deduplicates down to exactly one "Find Person" per target — any accumulated duplicates are collapsed to one on the next `changeRelationship` call, fixing existing save files (`Character.java:checkRelationship`)

## 2026-09-20 (Beast fight lethality — fix geometric population crash)
- Fixed: beast fights no longer unconditionally lethal — a deathmatch only occurs when the beast outlevels the cultivator by 6+ (`deathCheck Character.java:3939`)
- Fixed: `winCheck` — beast winner at equal/near level injures the cultivator (–30% cultivation) instead of killing them; only a lethal kill when the level gap is ≥6 (`Character.java:winCheck`)
- Root cause: all 100k NPCs fired Hunt every ~5–8 ticks from tick 1; 50% died per cycle → geometric decay to ~1k in <1 year

## 2026-09-20 (Area levels, beast/monster ages, technique print cleanup)
- Changed: new area level uses quadrant-based directional gradient capped at 100 — NW corner ceiling 20, NE 40, SW 60, SE 80; scales smoothly from ~1 at center to corner ceiling at the edge; levels cannot exceed 100 (`Area.java:272`)
- Added: Beasts spawn with randomized age `rand.nextInt(maxAge * 3/4)` instead of always 0 (`Character.java:beast constructor`)
- Added: Awakened Monsters also get randomized age `rand.nextInt(max(1, maxAge * 3/4))` instead of always 8 (`Character.java:awakenMonster`)
- Fixed: Technique upgrade message now only prints for the player or tracked characters — was printing for every NPC upgrade, creating enormous log spam (`Technique.java:62`)

## 2026-09-20 (Death cause displayed on reincarnation screen)
- Added: `Character.deathCause` field — set to "old age", "lifespan burned out by Forbidden Technique", "eliminated by an Array or Trap", or defaults to "natural causes" (`Character.java:67,780,845,4116`)
- Added: `handleReincarnation()` now shows cause — combat deaths name the killer with level and trait; other deaths show the cause string (`GameDriver.java:314,364`)

## 2026-09-20 (World simulation fixes — cities, sects, Beast Tides, combat output)
- Fixed: "won the fight" now names the opponent — `this + " won the fight against " + opponent` (`Character.java:3919,3923`)
- Fixed: Monster/Beast list removed from yearly Population line — was noisy (`GameDriver.java:583`)
- Fixed: Beast Tide now has 1/5 probability per eligible tick — was firing every tick any Beast Lair overflowed, resulting in many tides per year (`GameDriver.java:663`)
- Fixed: Leader NPCs no longer skip city creation (removed 80% chance to jump to professionLvl=50) — all Leaders now try `cityLeadership()` every tick, enabling far more cities to form (`Character.java:2533`)
- Fixed: Sect threshold lowered from 1 billion → 100 million spirit stones — 1B was never reached by any NPC (`Character.java:3471`)
- Added: `cityLeadership()` can now claim a leaderless city (2% chance/tick with 1M+ ss) — fixes Originis losing its owner permanently when the founding lord dies (`Character.java:3457`)
- Added: Tile Sect line appended to terminal/GUI Stats ('i') popup when the player's current tile has a sect (`GameDriver.java:921`)
- Added: `cityLeadership()` and `sectLeadership()` print a player-visible announcement when founding a city or sect (`Character.java`)

## 2026-09-20 (Technique system fixes; Ghost rebirth improvements)
- Fixed: Default Technique potential raised from 1→3 so upgrade() can improve it; NPCs no longer permanently stuck at lvl 1 (`Technique.java:43`)
- Fixed: practice() now also upgrades martialTechnique2 and movementTechnique2 backup slots — techniques found via dungeons/libraries/events were accumulating in backup but never leveling up (`Character.java:practice`)
- Fixed: Martial Trial dungeon also upgrades martialTechnique2 backup slot (`Character.java`)
- Added: NPCs in cities now attempt to buy the best technique from the city library (~2% chance per tick) — closes the Sell Technique → Library → NPC learning loop (`Character.java:quest`)
- Added: Ghost City Rebirth copies techniques at half lvl to the new body instead of resetting to defaults (`Character.java:Ghost City Rebirth`)
- Added: Ghost rebirth increments professionLvl by 1 each time — Ghosts grow wiser across lifetimes (`Character.java:Ghost City Rebirth`)

## 2026-09-20 (GUI — Info buttons show popup dialogs; running indicator)
- Fixed: Stats (i), Locations (l), Relationships (r), and Quests (q) now open a scrollable JOptionPane dialog in GUI mode instead of printing to the terminal — at 0ms tick speed the terminal output was buried under simulation spam, making these buttons appear non-functional (`GameDriver.java`)
- Added: `showGuiInfo(title, content)` helper wraps a JTextArea + JScrollPane in a modal JOptionPane; in terminal mode it falls back to stdout (`GameDriver.java`)

## 2026-09-20 (Performance — O(1) swap-remove for area.characters; GUI running indicator)
- Added: `Area.addChar(c)` / `Area.removeChar(c)` helpers using swap-remove — `c.areaIdx` stores each character's index in `area.characters`, making removal O(1) instead of O(n); eliminates the O(n²) bottleneck when 100k age-8 NPCs all leave the starting tile on tick 1 (`Area.java`, `Character.java`, `GameDriver.java`)
- Added: `areaIdx` rebuild step after save load — old saves don't have the field; `load()` iterates `createdAreas` and sets `c.areaIdx` from list position (`GameDriver.java:load`)
- Changed: `waitingForInput` volatile flag set true inside `readInput()` while blocked on `guiInputQueue.take()` — MapViewer status label now shows "⌨ Waiting for input — press a button" (blue) vs "▶ Simulation running..." (green) so the player knows when buttons take effect (`GameDriver.java`, `MapViewer.java`)

## 2026-09-20 (Performance — Age-8 Start, Filtered tickSnap, ensureTile Fix)
- Changed: `Character(Area area)` constructor now sets `age = 8`; all city-spawned cultivators (including initial 100k) start at age 8 and can move/quest immediately (`Character.java`)
- Changed: Phase 1a uses `fullSnap = new ArrayList<>(characters)` for time(); `tickSnap` is now a fresh filtered list (non-null, alive, born) built after time() — eliminates null/dead/born guard checks in Phase 1b and Phase 2B (`GameDriver.java`)
- Fixed: `ensureTile()` now sets `currentCharacter = this` inside the tileLock before creating `new Area()`, ensuring level seeding uses the correct character when future parallel movement creates tiles (`Character.java:ensureTile`)

## 2026-09-20 (GUI — Larger Fonts, Edit Self Fix, Button Debug Prints)
- Changed: All MapViewer fonts 14pt→16pt, 15pt→17pt for readability (`MapViewer.java`)
- Fixed: Edit Self ('e' button) now opens JOptionPane dialogs in GUI mode — previously `editCharacterInteractive` called `readInput()` which blocks on `guiInputQueue` (GUI button presses), so terminal keypresses (N/T/P etc.) were silently ignored; GUI branch now uses `showOptionDialog`/`showInputDialog` matching the reincarnation fix pattern (`GameDriver.java:editCharacterInteractive`)
- Added: `queueInput()` now prints `[GUI] queued: <cmd>` to terminal on every button press; click-to-move prints `[GUI] click-move → x,y` (`MapViewer.java`)

## 2026-09-20 (Threading Fix — Revert CopyOnWriteArrayList, Serial Phase 2B)
- Reverted: `Area.characters` back to `ArrayList<Character>` — `CopyOnWriteArrayList` caused O(n) copy overhead on every `add`/`remove` during NPC movement, catastrophically slowing Phase 2B (`Area.java`)
- Changed: Phase 2B (NPC quest/movement) reverted from parallel `byArea.entrySet().parallelStream()` to simple serial loop — quest targeting reads `location.characters` directly (not a snapshot), making safe parallelism complex without a deeper refactor; Phase 1b parallel `cultivate()` kept (`GameDriver.java`)
- Changed: `tickDelayMs` default set to `0` for maximum simulation speed; adjustable in-game via speed buttons (`GameDriver.java`)
- Tested: 354-year stability run with 500 NPCs, 0 exceptions, 0 errors; population dynamics, beast tides, monster ecosystem all functioning correctly

## 2026-09-20 (Multithreading — Parallel Cultivate + Spatial NPC Batching)
- Added: `Map.tickPool` (`ForkJoinPool`, all cores), `Map.tileLock`, `volatile` hero flags, synchronized `addTemp/addRem/addMonster/addCreatedArea/addLegacy` helpers (`Map.java`)
- Changed: `Area.characters` is now `List<Character>` backed by `CopyOnWriteArrayList` for thread-safe parallel iteration; `readObject()` migrates old saves; both `createdAreas.add(this)` calls replaced with `addCreatedArea(this)` (`Area.java`)
- Changed: All `remCharacters.add/tempCharacters.add/monstersList.add/legacyTitles.add` calls in dead paths replaced with synchronized Map helpers; hero flag announcements use double-checked locking on `Map.class`; `ensureTile()` helper with double-checked locking on `Map.tileLock` replaces 8 inline tile-creation sites in `movement(char)` (`Character.java`)
- Changed: Pass 1 split into serial `time()` loop (using `tickSnap`) then parallel `cultivate()` via `tickPool.submit(() -> tickSnap.parallelStream()...)` (`GameDriver.java`)
- Changed: Pass 2 split into serial player-I/O phase (Phase 2A) and parallel NPC-by-area phase (Phase 2B) — NPCs grouped by tile into `byArea` map then processed via `parallelStream()` (`GameDriver.java`)

## 2026-09-20 (GUI Polish — Larger Fonts, Resizable Panels, 0ms Speed, Running Indicator)
- Increased: All GUI fonts bumped again — 14pt body, 15pt labels/dialogs (was 12–13pt)
- Changed: Layout restructured with JSplitPane — vertical split between main content and event log (draggable divider, default 72% to log); horizontal split between map and right column; vertical split between Tile Info and NPC Tracker panels
- Added: "0ms" tick speed button (maximum simulation speed)
- Added: Status label in speed bar shows "▶ Simulation running — no input needed" (green) or "⏸ PAUSED" (orange), updating every 300ms

## 2026-09-20 (GUI Fixes — Scalability, Movement, Reincarnation, Calamity Grace)
- Fixed: GUI window opens at 85% of screen size instead of fixed 800×650; map tiles scale dynamically with panel size; ComponentListener triggers repaint on resize (`MapViewer.java`)
- Fixed: `pathfinder()` now returns early when `player >= 1 && skipping <= 0`, preventing quest auto-navigation from overwriting human player's manual movement (`Character.java:pathfinder`)
- Fixed: Reincarnation dialog uses `JOptionPane.showOptionDialog` via `invokeAndWait` when `guiEnabled`; `guiInputQueue.clear()` flushes stale movement button presses before prompting; NPC-by-ID sub-option uses `showInputDialog` (`GameDriver.java:handleReincarnation`)
- Fixed: Calamities (heaven fire, plague) gated behind `_year > 18` to give players an 18-year grace period at game start (`GameDriver.java`)
- Increased: All GUI font sizes from 10–11pt to 12–13pt throughout `MapViewer.java` for readability

## 2026-09-19 (Simulation Audit — 233-year run)
- Fixed: Beast Tide message now always prints when a lair overflows, not only when it strikes a populated city (`GameDriver.java:504`) — tides were silently firing 2–5 times/year with no output; city-strike follow-up line preserved

## 2026-09-19 (Save / Load System)
- Added: `SaveData.java` — serializable wrapper capturing all static state from `Map` and `GameDriver` (`SaveData.java`)
- Added: `save()` and `load()` in GameDriver — serialize/deserialize full world state to `save.dat` (`GameDriver.java:save`, `GameDriver.java:load`)
- Added: Load check at startup — detects existing `save.dat` and prompts Y/n to resume (`GameDriver.java:main`)
- Added: Auto-save every 10 in-game years (`GameDriver.java`) and manual 'f' key save
- Added: Save button in MapViewer GUI — runs save on background thread to avoid EDT block (`MapViewer.java`)
- Added: `implements Serializable + serialVersionUID` on all 10 game data classes: `Character`, `Area`, `Organization`, `Quest`, `Technique`, `Item`, `Library`, `Shop`, `Relationship`, `RelationshipList`
- Added: `Area.deadCharacters` marked `transient` + custom `readObject()` to reinitialize after load (`Area.java`)
- Tested: Auto-save fires at year 10; load path restores year/population and resumes without error

## 2026-09-19 (Feature Batch: Startup Flow, GUI Enhancements, Cheat Menu, Reincarnation)
- Added: Startup mode select — Single Player / Multiplayer (2-4 players) / Watch (spectate); each player gets an editable generated character (`GameDriver.java:main`)
- Added: Full character editor at startup and in-game ('e' key) — name, trait (all 17 variants), potential, affiliation, gender via `editCharacterInteractive()` (`GameDriver.java`)
- Added: Pause + tick-speed controls — `tickDelayMs` and `paused` volatile statics; GUI buttons (⏸/0.1s/0.5s/2s/10s) in speed panel; 'p' key in terminal; live status label (`GameDriver.java`, `MapViewer.java`)
- Added: World History tab in event log — `historyLog` list (capped 1000) accumulates `[HERO]`, `[SUCCESSION]`, `[WORLD]`, Beast Tide, Celestial Trial, plague, heaven fire, tournament wins; shown in GUI second tab (`GameDriver.java`, `MapViewer.java`)
- Added: NPC tracker panel in MapViewer — `Map.trackedCharacters` list; panel shows name/level/age/coords with remove button; tracked NPCs shown as cyan dots on map (`Map.java`, `MapViewer.java`)
- Added: Clickable cultivator names in tile info panel — JButton for each top-5 cultivator opens a detailed stats dialog with Track and Control buttons (`MapViewer.java:showCharacterDialog`)
- Added: Control button in character dialog — switches player control to that NPC (demotes previous player to follow mode); works from tracker panel too (`MapViewer.java`)
- Added: Quest display — 'q' key shows active quest, target queue, spot queue for the controlled character (`GameDriver.java`)
- Added: Cheat menu ('z' key) — select self/by ID/nearby; options: give SS, set level, set potential, heal, create weapon, clear quests, set kills, track (`GameDriver.java:handleCheatMenu`)
- Added: Death/reincarnation — `needsReincarnation` flag set in `dead()` and `dead(killer)` for non-Ghost player characters; `handleReincarnation()` offers to take over killer/child/any NPC/spectate after death (`Character.java`, `GameDriver.java:handleReincarnation`)
- Changed: `Item` weapon removed one-arg constructor usage in cheat menu; uses correct `Item(Character maker, WeaponType, lvl)` constructor

## 2026-09-19 (Features: Hero Threshold, Event Observation, Plague, Beast Tide Defense, City Succession)
- Fixed: Monster mortality restored — `dead()` no-arg now adds monsters to `remCharacters` (nulls `characters` slot); GameDriver already skips `population--` for them (`Character.java:788`)
- Added: Hero threshold — first cultivator to reach Ensoulment/Ascension/Pseudo Immortal prints a `[HERO]` world event; flags on `Map.java` (`Character.java:765`, `Map.java`)
- Added: `[WORLD]` summary line each year — areas, sects, cities, legacy count for system verification (`GameDriver.java`)
- Added: City succession tracking — snapshot owners at year start, print `[SUCCESSION]` on change, clean up dead owner refs (`GameDriver.java`)
- Fixed: Plague calamity now kills ~30% of cultivators in a random populated area instead of requiring a rare city; fires reliably every time it rolls (`GameDriver.java`)
- Added: Beast tide defense — cultivators present in a struck city reduce casualties proportional to their combined majorLvl power; prints saved count (`GameDriver.java`)
- Verified: 1000-year run clean; plague fires ~10 times, two hero events (Ensoulment + Ascension) confirmed; no crashes

## 2026-09-19 (1000-Year Run / Bug Fix Session)
- Fixed: `Area(Area, char)` constructor never initialized `backpack`, `materialBag`, `pillBag`, `arrayBag` — all null for every area except Originis; added 4 initializations (`Area.java:~263`)
- Fixed: `dead(Character killer)` NPE when `killer` is null — `arrayBreakAttempt` calls `dead(null)` for trap deaths; added null-guard redirecting to no-arg `dead()` (`Character.java:839`)
- Verified: 1000-year autonomous run completes clean (500 starting pop → 6,395 at year 1000; beast tide collapses pop to 140 at year 10 then steady recovery; no crashes)

## 2026-09-19 (NPC Audit / Bug Fix Session)
- Fixed: NPE in `quest()` after mid-quest death — added `if (dead) return;` guard and `currentQuest != null` checks before resolution block (`Character.java:~880`)
- Fixed: NPE/CME in GameDriver main loops — both character loops now iterate snapshot (`new ArrayList<>(characters)`) with null+dead guard (`GameDriver.java:~350`)
- Fixed: Heaven-Sent Treasure NPE — replaced `characters.get(rand.nextInt(...))` with first-non-null-non-dead scan (`GameDriver.java:~255`)
- Fixed: NPE in beast constructor — added all missing field initializations (`relationshipList`, collections, etc.) to `Character(Area, Character)` (`Character.java`)
- Fixed: NPE in birth constructor — added `affiliationNum = 0` before sibling `changeRelationship` call (`Character.java`)
- Fixed: IndexOutOfBoundsException in `dead()` relationship cleanup — added bounds check before `characters.get(friendID)` in both `dead()` overloads (`Character.java`)
- Fixed: Duplicate `dead()` calls — added `if (dead) return;` idempotency guard to both overloads (`Character.java`)
- Fixed: Monster trait mishandling in `dead(killer)` — added `trait != Traits.Monster` to all trait checks so monsters don't go through NPC death path (`Character.java`)
- Fixed: **Negative population bug** (root cause) — no-arg `dead()` called `remCharacters.add(this)` unconditionally; added early return for Beast/Monster to skip remCharacters (`Character.java:786`); also added trait guard in GameDriver remCharacters processing loop so Beast/Monster entries never decrement population or null `characters` slots (`GameDriver.java:591`)

## 2026-09-19 (Batch 19)
- Added: Seasons — `currentSeason` static int (0=Spring…3=Winter), updated every 13 weeks; `Area.age()` applies seasonal herb/monster growth multipliers; season change printed (`GameDriver.java`, `Area.java`)
- Added: Spiritual Veins — `Area.spiritualVein` bool (1% spawn chance); occupants get +1 cultivate/tick; teal tile color in MapViewer (`Area.java`, `GameDriver.java`, `MapViewer.java`)
- Added: Bloodline Bonus — `Character.bloodlineBonus` (birth constructor; +1 per parent at majorLvl≥3); extra cultivate/tick per point (`Character.java`, `GameDriver.java`)
- Added: Heaven Calamities — 10% per year: Spiritual Storm (area level surge), Plague (city -30% pop), Heaven Fire (clear arrays or cull beasts) (`GameDriver.java`)
- Added: Reputation Titles — `Character.repTitle` field; `updateRepTitle()` sets titles based on kills/majorLvl/professionLvl; shown in `toString()` as `'Title'`; updated yearly (`Character.java`)
- Added: Auction House — cities with population≥500 auction top 3 items yearly to richest known bidder; proceeds to city owner (`GameDriver.java`)
- Added: Sect Territory Bonus — yearly: if sect members occupy 3+ distinct tiles within 5 of sect, `territoryBonus=1`; added to cultivation loop bonus (`Organization.java`, `GameDriver.java`)
- Added: Interactive GUI — `LinkedBlockingQueue<String> guiInputQueue`; `readInput()`/`readInt()` helpers replace `scan.next()`/`scan.nextInt()` in player loop; `TeeStream` captures System.out to `eventLog` (`GameDriver.java`)
- Added: MapViewer interactive controls — movement pad (N/S/E/W/Wait), action buttons (Stats/Locs/Rels/Options), Move-to-Tile buttons (click map or info panel), event log panel; player dot on map; dynamic tile scaling (`Map.range`-aware); Celestial Trial cyan + Spiritual Vein teal colors; season/title/territory shown in info (`MapViewer.java`)
- Changed: Thread.sleep 10s→500ms when guiEnabled (`GameDriver.java`)

## 2026-09-19 (Batch 18)
- Confirmed: Forbidden Technique (feature 9) already implemented — `forbiddenCheck()` boosts tempLvl by √potential, costs maxAge, was already wired into winCheck(); no change needed
- Added: Breakthrough Tribulation — fires at majorLvl 3/5/7/9 in `cultivate()`; pass chance = potential - (majorLvl-3)*5, clamped 10-90%; fail pushes back one realm + inhibitor; exceptional pass has 10% chance +1 potential (`Character.java:cultivate()`)
- Added: Dual Cultivation — 5%/turn: spouse in same tile → 3 cultivate() bonus each; best friend (value≥3) → 1 cultivate() each (`Character.java:quest()`)
- Added: Legacy Titles — characters who die at majorLvl≥7 recorded in `Map.legacyTitles + Map.legacyLvls`; living characters have 2%/turn to meditate on a legend for cultivation bonus (`Character.java:dead()`, `quest()`, `Map.java`)
- Added: Celestial Trials — once per year a random wilderness area (lvl≥20) becomes CELESTIAL_TRIAL dungeon for 10 ticks; notifies characters within 15 tiles; on completion: 3 beast fights, 20 cultivate, 25% potential, 10% technique (`Area.java` new DungeonType, `GameDriver.java`, `Character.java:targetQuests()`)
- Added: Beast Tide — yearly check: Beast Lair with monsters > lvl*2000 spreads 1/4 of overflow to 4 cardinal neighbors; city neighbors lose up to 10% population (`GameDriver.java`)
- Added: Heaven-Sent Treasure — every 3 years a high-potential technique descends in a random wilderness area; broadcasts location to characters within 20 tiles; first arrival claims it via "Heaven Treasure" target quest (priority 70) (`Area.java heavenlyTechnique`, `GameDriver.java`, `Character.java`)
- Added: Weapon Enchantments — Innovative characters have 5%/turn chance to enchant equipped weapon (Sharp/Spiritual/Swift/Heavy/Fierce/Wrathful/Sundering, +1–5 bonus); bonus adds to tempLvl in `tempLvlCheck()`; shown in Item.toString() (`Item.java`, `Character.java`)
- Added: Trade Caravan growth — after Merchant sells at city (Go Sell or Sell Stock), city.population grows by earnings/1M; Evil characters may queue "Rob Caravan" target quest against nearby wealthy Merchants (5%/turn) (`Character.java`)
- Changed: Hotspot faction wars — hotspot-area squabbles now add 2 per year (vs 1 for dungeons); war threshold lowered from 5 to 4 (`GameDriver.java`)

## 2026-09-19 (Batch 17)
- Added: `MapViewer.java` — optional read-only Swing map viewer (31×31 tile grid, biome/feature color coding, cultivator density brightness, click-to-inspect info panel with tile details + top cultivators); interactive action hooks stubbed as comments for future expansion
- Changed: `GameDriver.java` — "Enable map viewer? Y/n" prompt at startup; `guiEnabled` + `mapViewer` static fields; `mapViewer.refresh()` call at end of each tick (after cleanup, before sleep)

## 2026-09-19 (Batch 16)
- Added: "Hunt Cultivator" spot quest (priority 55) to `monsterQuestInitializer()` — Monsters now actively target cultivators in their tile via `battleCheck()` (`Character.java:monsterQuestInitializer()`)
- Added: "Seek Territory" target quest trigger in `quest()` — 20% chance/tick, Monster migrates to highest-lvl known area with lvl > current+10; "Seek Territory" case in `targetQuests()` calls `beastBattleCheck()` on arrival (`Character.java:quest()`, `targetQuests()`)
- Added: Cultivation XP for Monster kills — when a Monster kills a cultivator in `dead()`, calls `cultivate()` up to `Math.min(victim.lvl, 20)` times; Monsters now grow from PvP kills like they do from beast kills (`Character.java:dead()`)
- Changed: Legendary milestone in `dead()` — now also grants 10 bonus `cultivate()` calls, 5% chance of +1 potential, and posts a high-priority "Bounty Hunt" quest (priority 65) at nearest city via `cityAreas` (`Character.java:dead()`)
- Added: "Hunt Beast" mission to `Organization.postMissionsYearly()` — posted when `sl >= 5`; format `Hunt|Beast|lvl|qty|bonus`; tracked in "Hunt" spot quest case (decrement remaining kills per `beastBattleCheck()`; complete when qty reaches 0) (`Organization.java:postMissionsYearly()`, `Character.java:spotQuests()`)
- Confirmed: Ghost rebirth ("Possess Womb" + "Ghost City Rebirth") fully implemented — no code change needed; item 6 was documentation-only

## 2026-09-19 (Batch 15)
- Added: Monster NPC awakening — `awakenMonster()` in `Character.java`; triggered from `winCheck()` at 10% when a Beast kills a cultivator; flips `trait=Monster`, `affiliation=Evil`, assigns legendary title prefix, sets `id=UID++`, adds to `characters` and `awakenedMonsters` (`Character.java`)
- Added: `monsterQuestInitializer()` — Monster spot quests: Hunt (priority 60) + Random (priority 25); no Profession/Test quests; battle quests accumulate via relationship system (`Character.java`)
- Added: Legendary milestone titles in `dead()` — Monster killing cultivators earns suffix at every 10 kills: Legend@10, Tyrant@20, Dread Lord@30, Immortal Terror@50; replaces existing suffix (`Character.java:dead()`)
- Added: `Map.awakenedMonsters` list — holds freshly-awakened Monsters for end-of-tick removal from `monstersList` without incorrectly decrementing population (`Map.java`, `GameDriver.java`)
- Changed: `quest()` — Monsters skip `gearNeeded` (buy-gear), `Seek Potential`, and `Buy Cultivation Pills` triggers; Monsters are predators and use no economy features (`Character.java`)
- Updated: `Game Info.md` — Monster trait entry updated; Monster quest initializer table added; cleaned Unimplemented Feature Note; Architecture section updated
- Updated: `Game Outline.md` — Monster NPC awakening flipped ❌→✅

## 2026-09-19 (6)
- Fixed: `Item` herb/metal constructor off-by-one — `this.quantity += quantity` → `this.quantity = quantity` (`Item.java:50`)
- Fixed: `toString()` True Immortal dead-flag side effect — moved rebirth logic to `dead(Character killer)` as an early-return guard; `toString()` no longer mutates state (`Character.java`)
- Added: City/sect destruction — `destroyCity(Area area)`: plunders all bags, Blood ∝ population/100, 10 cultivate() calls, 10% potential gain, coins from population, 90% pop wipeout, dissolves sect, clears city; triggered from `winCheck()` at 20% chance when Evil/Beast kills at an array-free city/sect (Originis protected) (`Character.java`)
- Added: Hunt for Money fallback — after `buyGear()` finds items but NPC still lacks a weapon or armor slot (can't afford), queues "Hunt Beast" with priority 40 toward nearest monster area (`Character.java:quest()`)
- Added: Lucky one-time corpse loot gate — when a corpse within 30 levels appears at Lucky's tile, queues one-time `"Loot Corpse"` spot quest at priority 75; handled by new `spotQuests()` case that calls `eventLootDead()` (`Character.java`)

## 2026-09-19 (5)
- Updated: `CLAUDE.md` — rewrote Current State (2026-05-28→2026-09-19), replaced 8-item stale bug list with 5 real active bugs, replaced all-done Priorities section with actual remaining work (3 unimplemented temp-doc items + 4 bug fixes); corrected `player` guard note from `if (player && ...)` to `if (player > 0 && ...)`

## 2026-09-19 (4)
- Updated: `Game Info.md` — comprehensive rewrite: fixed 10 stale bug entries (all resolved in prior sessions), added Library.java/Shop.java to file map, corrected Character.java line count (~3,900), added Ghost/IronBody/SpiritVein/HeavenlyRoot trait descriptions, corrected Merchant/Ghost traits from "stub/not implemented" to fully implemented, added Dungeon table, Sect Mission System, Library section, Shop pricing, itemPrice formula, potential gain events; reduced active bug list to 5 real remaining issues
- Updated: `Game Outline.md` — fixed "cityOwner is never set" stale bug note; fixed tax timing description from "per tick" to "per year"

## 2026-09-19 (3)
- Added: Sect mission delivery — Forage/Mine completion now checks location; if at sect, transfers materials to treasury immediately; if away, queues "Deliver Materials" target quest to return and deliver; materials actually leave character's bag on delivery (`Character.java:spotQuests()`, `targetQuests()`, `questViable()`)
- Added: City taxation — cityOwner collects `population / 100` ss per year in GameDriver's yearly tick (`GameDriver.java`)
- Changed: `eventChoosing()` — removed dead cases 0/3/4 (PvP/Forage/Mine); redistributed weights: beast 7–15%, interact 30–40%, profession 35–40%, loot 13–20%; cleaned up default-case debug print (`Character.java`)
- Changed: `event()` switch — removed empty cases 0, 3, 4; only active cases 1/2/5/6 remain (`Character.java`)
- Design: Technique rarity tiers noted as implicit in `CultivationGame2Recent.txt` — potential value (1–100) encodes rarity; sect library gating provides the access barrier

## 2026-09-19 (2)
- Added: "Return to Sect" quest — sect non-masters drift back to sect location (5%/turn when away); queued in dynamic triggers; awards 2 sectPoints on arrival; viability check cancels if sect dissolved (`Character.java:quest()`, `questViable()`, `targetQuests()`)
- Added: NPC buy cultivation pills — when at a city with pills for the next major realm, NPCs purchase one (10% chance/turn); price paid to cityOwner; economy circulation (`Character.java:quest()`)
- Fixed: `cultivate()` cases 3–10 breakthrough prints unguarded — now wrapped with `if (printBreakthroughs || (player > 0 && skipping <= 0))` to match cases 1–2 (`Character.java`)
- Fixed: `dead()` no-killer overload "has died" print unguarded — now wrapped (`Character.java:765`)
- Fixed: `battle()` debug win-chance prints (two instances) removed entirely — were always firing for every NPC fight (`Character.java`)
- Fixed: `battle()` win/lose prints — guarded with `if (player > 0 || opponent.player > 0)` (`Character.java`)
- Fixed: `deathCheck()` beast/no-relation/DEATH MATCH prints — removed (debug noise; no player-relevant info lost) (`Character.java`)
- Fixed: Marriage print, birth print, blacksmith max-level print — all now guarded with player/printBreakthroughs check (`Character.java`)
- Fixed: "Fleed Fight" debug print removed from `battle()` (`Character.java`)

## 2026-09-19
- Added: Potential gain on major realm breakthrough (`Character.java:cultivate()`) — 1% chance when `majorLvl` advances; capped at 101; prints message to player
- Added: Potential gain on surviving forbidden technique (`Character.java:forbiddenCheck()`) — 2% chance in surviving `else` branch; caps at 101
- Added: Sect rank promotion (`Character.java:quest()`) — sect masters check members each turn (5% chance); OUTER→INNER at `majorLvl >= master.majorLvl-4`; INNER→ELDER at `majorLvl >= master.majorLvl-2`
- Changed: Item pricing formula (`Character.java:itemPrice()`) — `lvl^2.5 * 10` replaces old exponential; lvl 1=10ss, lvl 10=3162ss, lvl 100=10M ss; Potential pills 5× base
- Changed: Technique mastery on library learn (`Library.java`) — city library copies start at lvl/5; sect library copies start at lvl/3
- Changed: Technique mastery on birth (`Character.java` birth constructor) — child inherits best parent technique at lvl/4 starting mastery
- Changed: Technique mastery on Gift Technique (`Character.java:targetQuests()`) — copy given at lvl/2 starting mastery

## 2026-09-18 (12)
- Added: Ghost physique trait — characters spawning with Ghost trait enter ghost form on death (not old age); can "Possess Womb" (take over unborn child stronger than mother) or "Ghost City Rebirth" (spawn as new NPC in a city); inherit Ghost trait in next body; `originalName` tracks name chain; permanent death if killed while already in ghost form
- Added: IronBody, SpiritVein, HeavenlyRoot special physique traits — IronBody adds majorLvl/2+1 to tempLvl; SpiritVein gives 1.5× cultivation speed; HeavenlyRoot doubles technique upgrade chance in winCheck(); all three added to NPC trait pickers at rare physique rarity (~1% each)
- Added: `practice()` method on Character — calls `cultivate()` + 10% chance technique upgrade in one helper; used by sparring resolution
- Added: Sparring event — 10% chance per tick when two friends (rel >= 1) meet in `interact()`; both parties call `practice()` via winCheck(); sparring flag cleared after resolution
- Added: City improvement quests — "Buy City Array" (3%/turn for city owners: purchase and install array); "Entice Merchant" (5%/turn: pay incentive to recruit a merchant to open a local shop)
- Added: `sectPopulation` on Organization (starts 1000) — grows 1%/year via `growPopulation()` called in `postMissionsYearly()`; taxes sectPopulation/100 SS to sect master yearly
- Changed: `player` field on Character — was boolean, now int (0=NPC, 1=human, 2=tracked NPC/follow mode); all `if (player && ...)` → `if (player > 0 && ...)`; `followMode` field removed, replaced by `player = 2`; fixed in Character.java, GameDriver.java, GameDriver2.java, Area.java

## 2026-09-18 (11)
- Added: Sect quest board — `ArrayList<String> questBoard` on Organization; `postMissionsYearly()` posts up to 5 herb/metal shortage missions ("Forage|Herb|lvl|qty|bonus" format); "Accept Sect Mission" spot quest (15%/turn at sect); Forage/Mine cases check active mission and award bonus sectPoints on delivery; called yearly from GameDriver
- Added: Tournament advance registration — "Hold Tournament" revamped to set 8-week `tournamentTimer` + store prize/organizer/registrants on Area; "Register Tournament" spot quest (5%/turn for eligible realm members); "Spectate Tournament" spot quest (10%/turn for non-registrants); `Area.runTournament()` runs bracket when timer hits 0; GameDriver ticks timer every week
- Added: MC creation options — on game start, player now picks (C)ustomize (trait, potential 1-100, affiliation), (F)ollow mode (auto-plays with prints, no scanner), or Enter (default Alchemist 100 potential)
- Added: Follow mode game loop — `followMode` field on Character; when set, player character auto-moves and quests without waiting for input; prints `[Follow] <char><loc>` each turn
- Added: Merchant equips from own stock — after `restockFromCity()`, merchant scans own shop inventory and equips better weapons/clothing into their own gear slots, returning old gear to shelf
- Added: Technique gifting — parent characters gift their best technique to children (5%/turn); "Gift Technique" targetQuests case delivers a copy to child at their location; `Technique(Technique)` copy constructor used
- Added: Get Pregnant quest — married female characters generate "Get Pregnant" targetQuest (2%/turn); handled in targetQuests(); added 10% probability gate to `checkBirth()` so NPC births aren't instant every encounter
- Added: Find Parent's Body quest — both `dead()` overloads now notify all born living children; "Find Parent's Body" targetQuests case awards +1 potential on 20% chance
- Changed: "Hold Tournament" case — now sets 8-week registration window instead of running immediately
- Changed: winCheck() — sparring win now gives +1 relationship to both parties; non-sparring win now has 20% chance to upgrade winner's martial technique
- Fixed: `sell()` cultivation pill level check — `item.lvl <= majorLvl` → `item.lvl <= majorLvl * 10` (item levels are 1-100, not 0-10)

## 2026-09-18 (10)
- Added: Sect treasury system — `ArrayList<Item> treasury` on Organization stores donated weapons/clothing/herbs/metals/pills; `sectLvl()` computes sect advancement level as `sum(herb+metal qty×lvl) / 10000` capped at 100; `donationPoints()`/`buyingCost()` use proportional formula (herbs/metals: qty×lvl/1000, pills: qty×lvl×2, gear: lvl)
- Added: "Donate to Sect" spot quest — triggers at sect home location when member has spare backpack items or surplus materials/pills (10%/turn); donates all non-equipped backpack items + herb/metal surplus above 2000 + pill surplus above 5 reserve; awards proportional sectPoints
- Added: "Buy from Sect" spot quest — triggers when member has sectPoints, treasury has items, and member needs materials (by profession) or better gear (10%/turn); deducts sectPoints equal to donation value (fair exchange)
- Changed: Sect cultivation bonus in GameDriver — now `Math.max(founder.majorLvl, sect.sectLvl() / 10)` so treasury accumulation also boosts members' cultivation speed
- Changed: 'i' stat screen — sect line now shows sect level and treasury item count

## 2026-09-18 (9)
- Added: Tournament quest — "Hold Tournament" spot quest for city owners and sect masters (2%/turn); single-elimination bracket with probability-based outcomes (no death risk); major-realm band ±1 around organizer's level; organizer funds prize at `(maxBand+1)*10 + rand(11)` level; winner receives weapon or clothing prize
- Added: Sect contribution points earning — Hunt Beast (+5), Explore Dungeon (+10), Sect Raid (+20), Forage/Mine while in sect (+2), donating unique technique to sect library (+10 in `technique()`)
- Added: Innovative technique selling — "Sell Technique" spot/area quest (10% chance/turn); travels to nearest city library and sells best unique technique; `Library.sellTechnique()` pays seller; `technique()` also auto-donates a copy to sect library with 20% chance
- Added: City library initialization — `cityLeadership()` and Originis constructor now add a `Library` to `area.libraries`
- Added: Relationship recycling on death — `dead()` and `dead(Character killer)` now remove the deceased from all living friends' RelationshipLists; prevents stale relationship accumulation
- Changed: 'i' stat screen — now shows `kills`, `sectPoints`, and sect rank alongside sect name
- Changed: 'o' auto-move menu — expanded to: `x` (wait/quest-auto), `w/a/s/d` (directional N turns), `c` (coordinate pathfind: enter tile X Y then N turns), `m` (random N turns); added `autoX`/`autoY` fields to Character for coordinate tracking
- Fixed: Sect Raid `targetQuests()` — broadened condition to allow crisis raids (any enemy sect at location, not just warTarget); sectPoints awarded on success

## 2026-09-17 (8)
- Added: Sequential tag-team party battle — `partyBattleCheck(Character opponent)` in Character.java; positive-relationship allies at same location join in sequence; enemy accumulates `exhaustionPenalty` (+2 per fighter) applied at end of `tempLvlCheck()`; "Battle Rival", "Battle Arch-Enemy", "Revenge", "Bounty Hunt" in `targetQuests()` now call `partyBattleCheck()` instead of `battleCheck()`
- Added: `int exhaustionPenalty` field on Character; applied in `tempLvlCheck()` after base calculation
- Removed: 3 residual debug prints from `battle()` — TempLvl and win-chance lines
- Added: Alliance system — `isAlly()`, `proposeAlliance()`, `dissolveAlliance()` on Organization; `declareWar()` now auto-notifies allies of the defender and dissolves alliances between combatants; "Propose Alliance" quest: sect masters seek non-enemy sect masters (5% chance/turn)
- Added: Resource disputes — yearly check in GameDriver.java; pairs of sects whose masters share a dungeon/hotspot `knownLocation` earn +1 squabble; at 5 squabbles war is declared
- Added: Tribute system — "Demand Tribute" Character quest (2%/turn) for sect masters with 20+ level lead over a rival master; target pays 10M ss or war is declared
- Added: Succession crisis — `crisisTimer` field on Organization; `promote()` sets crisisTimer=52 when no Elder exists; crisis sects can be opportunistically raided by any sect master/elder without requiring war; GameDriver ticks crisis timer down each year
- Added: Defection — "Recruit Defector" Character quest (1%/turn) for sect masters at war; 40% chance a target Elder defects, joining the recruiting sect and triggering war with their original sect
- Added: `inCrisis()`, `isAlly()`, `proposeAlliance()`, `dissolveAlliance()` methods to Organization.java

## 2026-09-17 (7)
- Fixed: Crafting level cap formula — `Math.min((majorLvl + 1) * 10, 100)` hard-caps at 100; majorLvl 10 crafters still cap at lvl 100 items
- Removed: Debug print noise — removed "One Time Target/Spot Quest finished", "spotQuests queue empty", pathfinder reach-target, "has found", "Weird Case?" prints from Character.java
- Fixed: Join Sect quest now gates through `canJoin()` so sect criteria (potential, level, gender, affiliation, trait) are respected
- Added: Kill counter — `int kills` field on Character; incremented for beast-kills-cultivator, cultivator-kills-beast, and cultivator-kills-cultivator in `dead()`
- Added: `int sectPoints` field on Character for future sect contribution point use
- Added: Sect squabble tracking in `battleCheck()` — cross-sect fights increment `sect.squabbles`; at 5 squabbles, `declareWar()` is called and both sects enter war state
- Added: `DungeonType` enum to Area — LOST_RUINS, BEAST_LAIR, MARTIAL_TRIAL, HAUNTED_GROUNDS; 5% chance per newly-created area
- Added: `baseLvl` and `hotspot` fields to Area for hotspot spike/reset
- Added: `ArrayList<Library> libraries` field to Area
- Added: Yearly hotspot generation in GameDriver.java — at year start, reset previous hotspots (restore baseLvl) then spike 3-5 random non-city areas by +10 to +30 levels
- Added: `hotspotAreas` tracking list to Map.java
- Added: `SectRank` enum to Organization (OUTER_DISCIPLE, INNER_DISCIPLE, ELDER, MASTER, FOUNDER)
- Added: `memberRanks` HashMap, `squabbles`, `warTarget`, `allies` fields to Organization
- Added: `canJoin()`, `getRank()`, `promoteRank()`, `declareWar()`, `endWar()`, `isAtWar()` methods to Organization
- Changed: `join()` now assigns OUTER_DISCIPLE rank; `resign()` removes rank; `promote()` dissolves sect and ends war if last member leaves
- Added: `sectLibrary` field to Organization (initialized in constructor)
- Added: "Explore Dungeon" Area quest — 16% chance/turn for adult non-beasts to seek a level-appropriate dungeon; LOST_RUINS (treasure + technique find), BEAST_LAIR (3× beastBattleCheck), MARTIAL_TRIAL (tempLvl vs trial power, technique upgrade on win), HAUNTED_GROUNDS (cultivation + rare potential gain)
- Added: "Sect Raid" Area quest — sect masters and elders travel to enemy sect location and attack; war ends when enemy sect master dies or sect dissolves
- Created: `src/Game/Library.java` — technique library class; city libraries sell technique copies for ss; sect libraries require sectPoints; Innovative sellers can contribute techniques; includes TODO note to flesh out family techniques, mastery levelling, and rarity tiers

## 2026-09-17 (6)
- Fixed: `canAfford(long)` — now checks all currency tiers (ss + sc×1M + dc×1T + ij×1E); previously only checked `ss`
- Fixed: `deductSs(long)` — cascades draw-down from sc→dc→ij before depleting ss; previously capped at ss only
- Fixed: `itemPrice()` overflow — caps `(long)Math.pow(100, lvl/10)` at `Long.MAX_VALUE` for lvl > ~92; Potential Pills now safely multiply without overflow
- Fixed: Quest queue cleanup on death — `dead()` and `dead(Character killer)` now clear `targetQuests`, `spotQuests`, and `currentQuest` to prevent dangling references after death
- Added: Buy Gear travel quest — when gear is outdated/missing but not in a city with shops, queues "Buy Gear" Area quest to nearest known city with shops; viability check + `targetQuests()` case added
- Added: "Seek Potential" Area quest — triggers when `potential < 101 - (9 - majorLvl)^2` (insufficient for next breakthrough); travels to city with Potential Pills in `pillBag`; buys and consumes pill on arrival
- Added: Bully trigger in `quest()` — Evil characters with 15+ level advantage over a co-located character queue "Battle Dislike" targeting them (10% chance/turn)
- Changed: `battleCheck()` — added affiliation rules: Good characters won't attack non-Evil unless arch-enemy (value ≤ -3); Neutral won't attack non-Evil unless rival or worse (value ≤ -2)
- Changed: `smithing()`, `tailoring()`, `array()` — crafted item level now capped at `majorLvl * 10 + 50` (5 major realms above crafter's cultivation)
- Added: `weaponAdjectives`, `clothingNames`, `clothingAdjectives`, `arrayTypes`, `arrayAdjectives` word lists to `Dictionary.java`
- Changed: `Item` constructors — Weapon names now use random adjective + weapon type; Clothing uses random adjective + clothing name; Array uses random adjective + array type
- Added: Player stat screen (`i` key) — shows level, cultivation, potential, age, affiliation, trait, profession, wealth, equipment, techniques, sect
- Added: Known locations display (`l` key) — lists all known areas with coordinates, city/sect flags
- Added: Relationship list (`r` key) — shows all living relationships with label and value; spouse highlighted separately
- Added: `canJoin(Character)` method to `Organization.java` — checks `minPotential`, `minLevel`, `genderPref`, `affiliationMatch`, `traitRequired` criteria; `join()` now gates through `canJoin()`
- Added: Recruitment criteria fields to `Organization.java` — `minPotential`, `minLevel`, `genderPref`, `affiliationMatch`, `traitRequired`
- Added: "Recruit Member" Character quest trigger in `quest()` — sect masters (10% chance/turn) seek eligible candidates in current location using `canJoin()`; `targetQuests()` case calls `interact()` then `sect.join()`

## 2026-09-17 (5)
- Added: `arrayBreakAttempt(Area area)` — attacker fights strongest array first; removes it on break (win OR lose); 5% death chance on loss; win chance uses `50 - 15*(arrayPower - tempLvl)` where `arrayPower = array.lvl / 5`
- Added: `installArray(Area area)` — moves first item from `arrayBag` to `area.arrayBag`
- Changed: Scribe `professionPicker()` — auto-installs crafted arrays at current location via `installArray(location)`
- Changed: `battleCheck()` — beasts are blocked entirely by area arrays; human attackers must call `arrayBreakAttempt()` before engaging defender; if array is stronger than attacker (`arrayPower > this.tempLvl`), defender won't interfere and attacker only fights array
- Changed: `battle()` — defender gets `bestArrayLvl / 10 + 1` bonus to `tempLvl` from arrays in their area (after both `tempLvlCheck()` calls, before win chance)
- Added: `findBestRestockCity()` — scores known cities by pool size ×10 (Option A), own-shop presence +50 (Option B), proximity bonus up to +30 (Option C); returns highest-scoring city with a non-empty backpack
- Added: Restock Trip Area quest trigger in `quest()` — Merchant with ownedShops has 10% chance/turn to queue travel to best restock city
- Added: Restock Trip viability in `questViable()` Area branch — invalidates if city lost owner or pool is empty
- Added: Restock Trip case in `targetQuests()` — opens shop at new city if under cap, restocks all owned shops there

## 2026-09-17 (4)
- Fixed: Buy Gear trigger and `buyGear()` item level comparison — now uses `character.lvl` (1-100) vs `item.lvl` (1-100) instead of `majorLvl` (0-10); one major realm = 10 item levels; trigger fires when `equipped.lvl + 10 <= lvl`; `buyGear()` buys items at `item.lvl >= lvl - 9`
- Changed: Leader 20/80 split in `professionPicker()` — at professionLvl 1, 80% skip straight to sect track (`professionLvl = 50`), 20% try city first; both then attempt `sectLeadership()` at professionLvl 50
- Added: Hunt Beast Area quest — 20% chance per turn to queue travel to a known area with monsters within ±10/+15 levels; `beastBattleCheck()` called on arrival; viability check invalidates if area runs dry
- Added: Join Sect Area quest — 5% chance per turn for non-Leader adults with no sect to seek a known sect location; calls `Organization.join()` on arrival; low priority (20, max 40); viability check invalidates if sect dissolves or character already joined
- Added: Bounty Hunt system — when a close friend is too weak for direct revenge, they post a `Quest("Bounty Hunt")` to the nearest known city's `area.bounties`; characters within 10 levels of the target automatically pick up posted bounties at cities and add to their `targetQuests`; calls `battleCheck()` on target arrival; added `ArrayList<Quest> bounties` to `Area.java`
- Changed: `targetQuests()` "Battle Dislike" — no longer falls through to battleCheck(); now calls `bullyCheck()` which steals 25% ss when 15+ levels stronger, otherwise escalates to battle
- Added: `bullyCheck(Character target)` — intimidation mechanic for Dislike quest; significantly weaker targets lose 25% ss and relationship decreases; comparable/stronger targets fight back
- Changed: `itemPrice()` — Potential Pills priced at 5× base (require deaths/blood, so more valuable); all other items unchanged
- Changed: `sell()` — pill selling implemented; sells cultivation pills at or below current majorLvl (past-realm, useless); sells Potential Pills only if potential >= 100; pills go to city.pillBag; print now includes pill count and pill earnings

## 2026-09-17 (3)
- Fixed: Buy Gear trigger in `quest()` — now also fires when equipped weapon or clothing is a full major level below character's `majorLvl` (previously only fired when slot was empty)
- Fixed: `buyGear()` purchase threshold — replaced `item.lvl > equipped.lvl + 3` with `item.lvl >= majorLvl - 1 && item.lvl > currentLvl`; character now buys gear that brings them up to their current realm rather than waiting for a 3-level gap

## 2026-09-17 (2)
- Added: `Design Notes` section to `CLAUDE.md` — documents Array defense design (arrays protect area features, give defenders temp level boost, multiple arrays don't buff each other, must break array AND beat defenders)
- Added: `Design Doc Update Rule` to `CLAUDE.md` — rule to append design decisions to relevant design docs each session
- Created: `src/Game/Shop.java` — per-merchant shop class with `buyRate=0.55`, `sellRate=1.50`, `cityBuyRate=0.30`; `buyFromSeller()`, `sellToBuyer()`, `restockFromCity()` methods; Non-Merchants cannot access city pool directly
- Added: `ArrayList<Shop> shops` field to `Area.java` — initialized empty on all areas
- Added: `ArrayList<Shop> ownedShops` field to `Character.java` — initialized in both constructors
- Added: `maxShops()` — returns `Math.max(1, majorLvl)` as shop cap
- Added: `canAfford(long)` and `deductSs(long)` helpers to `Character.java`
- Added: `buyGear()` to `Character.java` — checks shops in current city for weapon/clothing upgrades (lvl+3 threshold, 25% wealth cap)
- Updated: `sell(Area city)` — prefers merchant shops at buyRate=0.55 over city pool at 50%; only city-pool items use backpack.removeAll; merchant sales handled by `buyFromSeller()`
- Added: Merchant case to `professionPicker()` — sets up shop in city when under maxShops cap; restocks from city pool when at own shop
- Added: Buy Gear trigger in `quest()` dynamic triggers — calls `buyGear()` immediately when in a city with shops and missing equipped weapon or clothing

## 2026-09-16
- Setup: Added `## Development Workflow` section to `CLAUDE.md` — establishes dev branch strategy, compile/smoke-test steps, and doc-update rules
- Setup: Created `DEVLOG.md`
- Fixed: `changeRelationship()` NPE (`Character.java:1366,1369`) — null-checked `currentQuest` before access and corrected boolean OR logic (stream result was being overwritten instead of combined)
- Cleaned: Removed 5 unguarded `System.out.println` debug calls from `changeRelationship()` (`Character.java`)
- Fixed: `spotQuests()` always returned 0 (`Character.java:944`) — changed `return 0` to `return questSuccess`; spot quests now correctly re-enter the priority queue on success (non-oneTime) or get removed (oneTime)
- Fixed: `event()` case 1 fall-through (`Character.java:1024`) — added `break` so beast battle no longer also triggers interact
- Fixed: `professionPicker()` fall-throughs (`Character.java:1073,1075`) — added `break` after Friendly and Tailor so each trait runs only its own profession
- Fixed: `forbiddenCheck()` zombie characters (`Character.java:2000,2011,2034,2042`) — added dead-checks in `battle()` after each forbiddenCheck call; if overextension killed the user, call `dead(opponent)` and return immediately rather than leaving the character as a zombie
- Fixed: `checkRelationship()` string comparison (`Character.java:1473,1474`) — `q.name == "Find Person"` → `"Find Person".equals(q.name)`; removeIf was never firing
- Fixed: `checkRelationship()` NPE duplicate (`Character.java:1487,1490`) — same currentQuest null-check pattern as changeRelationship(); merged into stream OR with null guard
- Cleaned: Removed 10 unguarded `System.out.println` debug calls from `checkRelationship()`
- Fixed: Technique slot duplication in `equip()` (`Character.java:1789,1792`) — added `!techniqueList.contains()` guard before adding technique2 slots; repeated equip() calls were duplicating techniques into the list
- Fixed: Stale quest target area (`Character.java:832`) — refresh `currentQuest.targetArea` from live character location before each pathfinder() call for Character-category quests
- Confirmed: `knownLocations.add(location)` already present in `movement()` (`Character.java:507`) — Beast-guarded with contains check; no change needed
- Added: "Forage" and "Mine" spot quest cases to `spotQuests()` — call forage()/mine() in current tile, return success 2
- Added: Material pre-req quest queuing in `professionPicker()` — Alchemist keeps inline forage() and now also queues "Forage" spot quest on failure; Blacksmith queues "Mine" if no Metal qty>=1000; Tailor queues "Forage" if no materialBag item qty>=1000; Scribe queues "Forage" if no materialBag item qty>=10000
- Added: Battle quest creation in `changeRelationship()` — value -1 queues "Battle Dislike" (priority 20), -2 queues "Battle Rival" (priority 35), -3 queues "Battle Arch-Enemy" (priority 50); deduped per target per name; both sides receive the quest
- Added: "Battle Dislike"/"Battle Rival"/"Battle Arch-Enemy" cases in `targetQuests()` — all call battleCheck(); Dislike has TODO comment for bullyCheck() when bully system is implemented
- Fixed: Tailor material check in `professionPicker()` — now checks Herb and Metal separately; queues Forage if area has herbs and herbs are low, Mine if area has metals and metals are low; may queue both
- Fixed: NPE in `dead()` (`Character.java:773`) — null check added for `friend` before accessing `.dead`; previously crashed if a friend had been nulled out in Map.characters
- Added: Revenge quest in `dead()` — close friends (value>=2) get "Revenge" Character quest (oneTime, priority 60) targeting the killer when `friend.majorLvl >= killer.majorLvl - 1`; weaker friends skip (bounty/squad revenge deferred)
- Added: "Revenge" case to `targetQuests()` — calls battleCheck() on the killer
- Added: Currency tier fields to Character — `sc` (Spirit Crystals), `dc` (Divine Crystals), `ij` (Immortal Jades); conversion rate 1,000,000:1 per tier
- Added: `convertCurrency()` — rolls ss→sc→dc→ij when any tier exceeds 1,000,000; called after sell()
- Added: `itemPrice(Item)` static helper — placeholder formula `100^(lvl/10)`; prices to be confirmed in economy pass
- Added: `sell(Area city)` — sells all backpack items except equipped weapon/clothing and one spare of each; transfers items to city.backpack; pays ss at itemPrice(); calls convertCurrency(); pill selling deferred with TODO
- Fixed: `cityLeadership()` — added `location.cityOwner = this`; unblocks tax collection and Go Sell quest validation
- Added: Go Sell quest trigger in `quest()` — when backpack.size() > 20, finds first known city with an owner, queues "Go Sell" Area quest (oneTime, priority 50)
- Added: "Go Sell" case to `targetQuests()` — calls sell() on arrival at city
- Added: "Go Sell" viability check in `questViable()` Area branch — invalidates quest if city was destroyed or owner is gone

## 2026-10-02
- Added: `Story Script.md` (every story line, menus, credits tails and a proposed three-beat soldier ending) generated by `tools/StoryDump.java` + `tools/story_header.md` / `story_footer.md` — for the story review before the text rewrite; no game text changed yet

- Changed: the endings follow the new lore (`LoreEndings.java`, `Lore.java`, `LoreText.java`): ??? is only the Gardener's image and stays in the Garden; the Old Man (the Gardener, Outside, array master of nine great lamps) explains after the Seam (`end_oldman`); Stay (Good/Evil/Neutral, undoable with `lore leave`) or Leave (lamp-bearer / strike at the Gardener / walk out alone); the Outside army are Half-Immortal+ veterans; old `ending_go`/`ending_carry` scenes removed — the user's lore
- Added: nine-lamp cap (`LAMP_MAX`; soldier ending closed, strike always fatal, hidden ending locked once full), blood bonus (+5 potential to everyone alive and born later, stacking, `Lore.State.blood`, `Character` birth hook), 4 new Outside branches (Quiet Farm, Long Climb, Cold Pass, Dust of the Road), `end_dark` poem, `gardenEnds` (hidden ending or last week: the Garden goes out, those who can cross choose, the game saves and ends), NPC seekers choose stay/lamp-bearer/strike/walk out (`npcCross`, `stayersYearly`, stayers feed `huntYearly`) — NPCs make the same choices as humans
- Added: `Story Script.md` (all story text, generated by `tools/StoryDump.java` + `tools/story_header.md` / `story_footer.md`) for the story review; `LoreProbe` 121 checks and `EndFlowProbe` updated for the new menus
- Changed: all other story text rewritten at a plain 7th-grade level in a serious voice (`LoreText.java` scenes, clues, whispers, omens; `LoreSites.FOOT_CLUES`; `Lore.CHAIN` objective texts), the Empty Seat scene now connects the nine facts in six numbered steps; `tools/ReadingLevel.java` added (sentences over 25 words: 27 before, 0 after; words per sentence 9.2 → 8.1)
- Fixed: after the Garden ends the game waits for the ending and credits windows before it quits (`GameDriver.java`, `GuiLore.java`: the credits count as an open scene); refreshed `win-lore-scene.png` / `win-oldest-order.png` — so the player can read the last scene
- Tested: LoreProbe 121, EndFlowProbe 8, PcProbe 112, PcProbe2 6, QolProbe 66, RulesProbe 22, StartsProbe 29, TextProbe 27, LifeProbe 85, NewProbe 25, SpecProbe 3, Chaos 9, SimProbeX 73, TermFuzz 8, CompatProbe 17 (old save), HelpCheck PASS, QolGui 53, PcProbeGui 130, MoreGui 35; smoke test clean

## 2026-10-03
- Changed: Part 2 review pass of the story text — plainer place descriptions (`LoreSites.java`), clearer Seam and Door messages and the `lore` command list (`Lore.java`), removed a lore leak (a wanderer hint said the Hollow Court notices cultivators in the Garden; `LoreEndings.HINTS`), glossary in `Lore Design.md` now names the Old Man = the Gardener, the array and lamp-bearers — keep the story consistent with the settled lore

## 2026-10-03 (economy)
- Fixed: a MANUAL player was walked away by duty quests after `learn`, `do Buy from Sect`, `stay`, `wait` and on arrival of a `goto` — duty walking is now allowed only on `watch` turns (`GameDriver.manualTurn`); `MoveProbe` reproduces and guards it
- Changed: "Buy from Sect" says what it bought, or why nothing (no sect, not at its home, empty treasury, nothing suits you, too few points) instead of "done" (`Character.java`)
- Changed: shops have no stock limit (`Shop.canStock`); the city pool is the shops' only supply now (auctions no longer take from it)
- Changed: Library — one book per (type, potential), sale = 20% fee and the seller loses the technique, potential < 4 and equipped techniques are not sold, yearly `compact()`; GUI library table sorts on every column and filters; `library` filters in the terminal (`Library.java`, `GuiWindows.java`, `TermView.java`)
- Added: `Treasure.java` — one Heaven-Sent treasure every year in eight kinds (technique, instrument, poison, Potential Pill, regular pill, clothes, weapon, array); `Item.heavenSent`, `Area.heavenItem`
- Added: `Auctions.java` — consignment (`Character.sell`), auctions at 5 lots or 6 weeks, wanted-lot bidding, 90% to the consignor; only Heaven-Sent items, Potential Pills and vintage goods; Potential Pills are never put on a shelf; the old yearly auction in `GameDriver` is gone
- Added: "Refine Blood" quest — a non-alchemist takes Blood to an alchemist for Potential Pills (`Character.refineBloodWith`, UPKEEP duty)
- Added: `tools/probes/EconProbe` (30 checks), `MoveProbe`, `EconSim` (long-run economy counters)
- Tested (economy): EconProbe 30, MoveProbe 5, PcProbe 112, PcProbe2 6, QolProbe 66, RulesProbe 22, StartsProbe 29, TextProbe 27, LifeProbe 85, NewProbe 25, SpecProbe 3, Chaos 9, SimProbeX 73, LoreProbe 121, EndFlowProbe 8, TermFuzz 8, CompatProbe 17 (old save), HelpCheck PASS, QolGui 53, PcProbeGui 130, MoreGui 35 (PcProbe/NewProbe/LifeProbe flake once under load: timing). 60-year EconSim vs before: library books 1,568 -> 140, city pool items 21,000 -> 4, average potential unchanged (~39), 61 treasures / 60 claimed, ~740 auctions, 0 exceptions
- Changed: `win-market-auction.png` and `win-market-library.png` regenerated (a stocked block and library); the Auction tab wraps its lines
