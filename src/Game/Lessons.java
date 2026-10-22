package Game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import Game.DialogueText.Voice;

/** Lessons: the way the game teaches. There is no tutorial lecture. Now and then, when someone stops to talk to a person who plays (or the person who plays talks to someone), the talk turns to something the person
 *  needs to know right now: how the bar fills, what a gate asks for, why potential matters, what a sect is for. Each lesson is a short casual talk (two voices: a warm one and a drier one), told once per person,
 *  and the next waits at least `COOLDOWN` weeks. They need a person to meet: nobody has to hunt for them in a menu. `settings lessons off` stops them; they do not depend on `settings dialogue`.
 *  The words are plain (about a 7th grade level), short and medium sentences, and may be funny, serious, strange or sad. Every fact in them is true of this game. */
final class Lessons {
    private Lessons() { }

    static final int COOLDOWN = 6; // weeks between two lessons

    /** The potential the next realm asks for, by the realm the person is in now (Foundation, Core Formation, ... Pseudo Immortal). */
    static final int[] GATE = {20, 37, 52, 65, 76, 85, 92, 97, 100};

    static final class L {
        final String id, title, gist;
        final Predicate<Character> when;
        final String[] warm, dry, more;
        L(String id, String title, String gist, Predicate<Character> when, String[] warm, String[] dry, String[] more) {
            this.id = id; this.title = title; this.gist = gist; this.when = when; this.warm = warm; this.dry = dry; this.more = more;
        }
    }

    private static int friends(Character me, int atLeast) {
        int n = 0;
        try { for (Relationship r : new ArrayList<>(me.relationshipList.relationships)) if (r != null && r.value >= atLeast) n++; } catch (RuntimeException e) { }
        return n;
    }

    private static boolean knowsDungeon(Character me) {
        try { for (Area a : new ArrayList<>(me.knownLocations)) if (a != null && a.dungeonType != null) return true; } catch (RuntimeException e) { }
        return false;
    }

    static final List<L> ALL = new ArrayList<>();
    static {
        ALL.add(new L("qi", "The slow fire", "You cultivate every week by yourself; meditating and spirit veins add more.",
                me -> me.majorLvl == 0 && me.minorLvl <= 9 && GameDriver.time >= 1,
                new String[]{
                        "{You}, sit a moment. You walk like someone who is late for something.",
                        "I was the same. An old woman cured me with one sentence: you don't chase qi, qi finds you.",
                        "Every week your body cultivates on its own. You don't have to do a thing for that.",
                        "Meditating adds a little extra. A spirit vein under your feet adds more, every single week.",
                        "So a quiet week is not a wasted week. Remember that when the road feels slow."},
                new String[]{
                        "You. Yes, you. Stop looking at the road like it owes you money.",
                        "Cultivation happens every week. Walking, trading, sleeping, it makes no difference. That part is free.",
                        "Meditating is the bonus. Spirit veins are the jackpot. Stand on one and you gain extra qi every week.",
                        "Veins are rare. Learn where they are. Or don't, and stay slow. Your choice."},
                new String[]{
                        "Your Stats sheet has a bar. That bar is your cultivation.",
                        "When it fills at the last level of a realm, you can try to break through.",
                        "Until then there is no shame in simply living."}));

        ALL.add(new L("gate", "The edge of a realm", "Before a breakthrough, check that your potential meets what the next realm asks.",
                me -> me.majorLvl < 9 && me.minorLvl >= 8,
                new String[]{
                        "You're close to a gate, aren't you? I can feel it on you.",
                        "When your bar is full at the last level of a realm, you may try to break through.",
                        "Here is the part nobody warns you about. Each realm asks for a certain potential. The first wants 20, the second 37, and it keeps climbing.",
                        "Meet it and you cross cleanly. Fall short and the try can fail. You slide back a level and your cultivation is slower for good.",
                        "So look at your potential before you press the button."},
                new String[]{
                        "Gate's near. Don't smile. It's not a party.",
                        "Full bar at the last level: you may break through. You choose the moment, so choose it well.",
                        "Each realm wants a minimum potential. 20, then 37, then 52. After that it gets steep.",
                        "Under the number, you can fail. A failure costs a level and slows you down forever.",
                        "Over the number, you're safe. Check first."},
                new String[]{
                        "If you're short, there are ways to raise your potential.",
                        "They are not pretty. But they exist, and people use them."}));

        ALL.add(new L("potential", "The ceiling", "Potential is the ceiling on how far you can go; pills, treasures and luck raise it.",
                me -> me.majorLvl < 9 && me.potential < GATE[Math.max(0, Math.min(8, me.majorLvl))] && GameDriver.time >= 8,
                new String[]{
                        "Can I ask you something odd? How far do you think you can go?",
                        "Everyone has a ceiling. We call it potential, a number from one to a hundred.",
                        "A low number stops at the early realms. Only a hundred walks the whole road.",
                        "It can grow, though. Potential Pills do it. So do some treasures, and sometimes a lucky breakthrough.",
                        "Nobody told me that for years. I'm telling you now."},
                new String[]{
                        "What's the most valuable thing you own? Wrong. It's a number you can't spend.",
                        "Potential. A ceiling from one to a hundred. It decides which realms will ever open for you.",
                        "You can lift it with Potential Pills, with certain treasures, or with plain luck at a breakthrough.",
                        "Nothing matters more. Not gold. Not swords."},
                new String[]{
                        "The pills are made from blood.",
                        "That's a story for another day."}));

        ALL.add(new L("travel", "Roads and patience", "Move to Selected Tile walks the whole way; the Control window decides what stops a walk.",
                me -> GameDriver.time >= 10,
                new String[]{
                        "Lost? No? Good. Most people are, the first season.",
                        "Click a tile on the map and press Move to Selected Tile. The game walks you there, week by week, and gives control back when you arrive.",
                        "The Locations list remembers every place you've seen. Route tells you how far each one is.",
                        "If something keeps stopping your walk, the Control window has two switches for it. One for fights, one for alerts."},
                new String[]{
                        "You walk one tile at a time? Painful to watch.",
                        "Click a tile. Press Move to Selected Tile. You walk until you arrive.",
                        "Locations lists the places you know. Route shows the distance.",
                        "Fights and alerts can stop a walk. The Control window has two switches that decide it. Both start off."},
                new String[]{
                        "Watch is the other trick. It lets the weeks run by until something happens.",
                        "Good for waiting. Bad for impatience."}));

        ALL.add(new L("quests", "Too many errands", "Quests go by priority; the Control window lets you rank kinds of quests yourself.",
                me -> me.targetQuests != null && me.targetQuests.size() >= 6,
                new String[]{
                        "Your face says you have too many errands.",
                        "The Quest Log is the list. Each quest has a priority, and the higher number goes first.",
                        "You don't have to take the game's word for it. In the Control window, the Priorities tab lets you rank whole kinds of quests.",
                        "Care about family more than money? Say so. It will listen."},
                new String[]{
                        "Count your quests. Go on. Too many?",
                        "A higher priority goes first. That's the whole system.",
                        "Control, then Priorities, lets you rank kinds of quests yourself. Family above gold, say. The game obeys.",
                        "Pinned quests never age. Use that for the ones that really matter."},
                new String[]{
                        "A quest you never want to see again can be deleted.",
                        "It stays gone."}));

        ALL.add(new L("sect", "A roof and a rulebook", "A sect gives extra cultivation every week, a mission board and a master.",
                me -> me.sect == null && me.majorLvl >= 1,
                new String[]{
                        "Still without a sect? I don't judge. I did it alone for ten years. Worst ten years of my life.",
                        "A sect gives you extra cultivation every week, missions from its board, and people who answer when you call.",
                        "Different sects want different things. Some take anyone. Some only take a certain kind of cultivator.",
                        "Stand in a sect's tile and press Join sect here. See what they say."},
                new String[]{
                        "Alone, are you? Brave. Also slow.",
                        "A sect cultivates for you. Extra qi every week, a mission board, a master to learn from.",
                        "Entry rules differ. Some sects want a certain trade. Some only want strong people.",
                        "Find a sect tile. Press Join sect here. That's all it takes to ask."},
                new String[]{
                        "And a sect has enemies, too.",
                        "Nothing in this world comes free."}));

        ALL.add(new L("gear", "The right tool", "Gear has a level from one to ten; equipping is free; smiths and tailors make better pieces.",
                me -> me.majorLvl >= 1 && GameDriver.time >= 20,
                new String[]{
                        "Nice robe. What's in your bag?",
                        "Gear has a level from one to ten, the same as realms. A level-five sword in the hand of a second-realm cultivator is a gift.",
                        "Putting something on costs no time. Open the Backpack, pick the best, and wear it.",
                        "Smiths and tailors make better pieces from good ore and cloth. Shops sell what they can't."},
                new String[]{
                        "Look at your weapon. Be honest.",
                        "Gear levels run from one to ten, like realms. Better gear wins fights. Not always. Mostly.",
                        "Equipping is free. Backpack, pick the best, done.",
                        "Smiths make the good stuff. Shops sell it. You pay."},
                new String[]{
                        "Don't forget your techniques.",
                        "A sword helps. A good technique helps more."}));

        ALL.add(new L("elements", "The five elements", "Your path is one of Wood, Fire, Earth, Metal or Water; the circle of the elements moves the odds of a fight.",
                me -> me.majorLvl >= 1 && GameDriver.time >= 15,
                new String[]{
                        "Have you looked at your own path yet? Open your Stats sheet. It names one of the five elements.",
                        "Wood, Fire, Earth, Metal and Water. They push on each other in a circle.",
                        "Fire melts Metal. Metal chops Wood. Wood parts Earth. Earth absorbs Water. Water puts out Fire.",
                        "Fight someone whose element beats yours and the odds turn against you. Fight one that yours beats and they turn for you.",
                        "Techniques have elements too. A technique that matches your own path works best."},
                new String[]{
                        "Five elements. Wood, Fire, Earth, Metal, Water. Your path is one of them.",
                        "Fire melts Metal. Metal chops Wood. Wood parts Earth. Earth absorbs Water. Water puts out Fire.",
                        "In a fight, the element that wins the circle gets an edge. The fight preview shows it.",
                        "Techniques have elements, too. Matching your path is good. Fighting against your own path is bad."},
                new String[]{
                        "There is a gentler circle as well. Wood feeds Fire, Fire makes Earth, Earth bears Metal, Metal holds Water, Water nourishes Wood.",
                        "A technique that feeds your own path helps you a little. One that holds it back hurts."}));

        ALL.add(new L("blood", "Dirty work", "1,000 Blood make a Potential Pill; an Evil alchemist refines two pills' worth or more and keeps half.",
                me -> me.bloodAmount() >= 1000 && me.potential < 100,
                new String[]{
                        "You're carrying Blood. Don't look so shocked. I can smell it.",
                        "A thousand Blood make one Potential Pill. Pills raise your potential, so they're worth a fortune.",
                        "Only an Evil alchemist will refine it. Bring them two pills' worth or more, and they keep half for the work.",
                        "Eating the pill doesn't change you. Making it does. That's why the good ones refuse."},
                new String[]{
                        "Blood in your bag. Mm.",
                        "A thousand Blood, one Potential Pill. And potential is everything.",
                        "An Evil alchemist will refine it. Bring two pills' worth or more. They keep half. No fee.",
                        "Making the pills makes the maker Evil. Taking them doesn't. Remember that."},
                new String[]{
                        "Where does Blood come from? The fallen.",
                        "I'll leave it at that."}));

        ALL.add(new L("squad", "Better together", "Friends can follow you as a squad; the Squad tab gives them orders.",
                me -> me.squadSize() == 0 && friends(me, 1) >= 1 && me.majorLvl >= 1,
                new String[]{
                        "You always travel alone? That's lonely, and dangerous.",
                        "Friends can follow you as a squad. They walk with you and fight beside you.",
                        "Use Invite on someone in your tile. If they like you, they come.",
                        "The Squad tab gives them orders: who fights first, who waits, who goes off to work."},
                new String[]{
                        "Alone again. Poor planning.",
                        "Invite someone in your tile. Friends say yes more often. Strangers roll the dice.",
                        "A squad follows you tile to tile and joins your fights.",
                        "Orders are in the Squad tab. Use them."},
                new String[]{
                        "A squad has a limit, though. Don't invite a crowd.",
                        "And somebody has to carry the food."}));

        ALL.add(new L("gifts", "A small gift", "A gift warms a friendship; good friends bring gifts about once a year; you can ask for one.",
                me -> friends(me, 2) >= 1,
                new String[]{
                        "Good friends are worth more than good swords.",
                        "Keep them close. A gift now and then warms a friendship, once a week at most.",
                        "Your best friends and your kin sometimes bring gifts of their own, about once a year each.",
                        "You can ask a friend for one, too. Look for Ask for a gift in the people list."},
                new String[]{
                        "Friends. Funny things. They stay if you feed them.",
                        "A gift warms a friendship. Once a week, at most.",
                        "Once a year, a good friend might bring you something. Be kind when they do.",
                        "Or ask. The people list has an Ask for a gift button. Don't be shy."},
                new String[]{
                        "A gift can be food or wine, too. Or poison, if your friend is the wrong kind of friend.",
                        "I won't say which kind I am."}));

        ALL.add(new L("tournament", "Where names are made", "Cities hold tournaments; the Rankings window keeps four boards.",
                me -> me.majorLvl >= 1 && me.location != null && me.location.city && GameDriver.time >= 30,
                new String[]{
                        "Come for the tournament? Cities hold them now and then.",
                        "Register, and you fight for the crowd. A winner earns a name that people repeat for years.",
                        "The Rankings window keeps score on four boards: highest level, richest, most kills and highest fighting level.",
                        "Look at who else registered first. A cultivator who knows the field wins more."},
                new String[]{
                        "Tournaments. Noise, sweat and glory.",
                        "Register in a city that is holding one. Win, and the world learns your name.",
                        "Rankings shows who is on top. Four boards. You might be on one of them.",
                        "Check your rivals first. Walking in blind is how heroes become stories."},
                new String[]{
                        "A name opens doors.",
                        "It also opens graves, if you're careless with it."}));

        ALL.add(new L("tribulation", "The lightning gates", "Four gates bring a tribulation; the odds follow your potential.",
                me -> (me.majorLvl == 2 || me.majorLvl == 4 || me.majorLvl == 6 || me.majorLvl == 8) && me.minorLvl >= 7,
                new String[]{
                        "Careful. You're near a gate the heavens guard.",
                        "Crossing into Soul Wandering, Ensoulment, Ascension and Pseudo Immortal brings a tribulation. Lightning, they say.",
                        "Your chance depends on your potential. The higher it is, the better your odds.",
                        "Pass, and the heavens reward you. Fail, and you fall back a realm and slow down."},
                new String[]{
                        "Four gates have a price. Soul Wandering, Ensoulment, Ascension and Pseudo Immortal.",
                        "Each one is a tribulation. A test from above.",
                        "Potential decides the odds. More potential, better odds. Pass and you gain. Fail and you drop a realm.",
                        "Be ready before you press the button."},
                new String[]{
                        "I watched a friend go through one.",
                        "I won't say how it ended. Press the button when you're ready."}));

        ALL.add(new L("gatherer", "Soft hands, full bags", "A Gatherer or Miner finds more of the materials below his skill; poor ground gives poor stuff.",
                me -> me.trait == Character.Traits.Gatherer || me.trait == Character.Traits.Miner,
                new String[]{
                        "You gather for a living? Then listen. Your skill is worth more than you think.",
                        "Once your skill passes the grade of what you find, you pull up more of it. One extra for every step.",
                        "A skill-fifty gatherer finds four times as many grade-two herbs. And every find teaches you more.",
                        "But poor ground gives poor stuff. A tile only yields materials a couple of grades above its own level."},
                new String[]{
                        "Herb picker. Or ore digger. Same thing, different dirt.",
                        "Skill matters. Find anything below your grade and you get a bonus, bigger with each step.",
                        "Every find trains you. Keep at it.",
                        "And rich ground gives rich stuff. Poor ground doesn't. Travel."},
                new String[]{
                        "The ground never runs out, by the way. It always grows back a little.",
                        "Take what you like."}));

        ALL.add(new L("family", "A house with a cradle", "Try for a child: both spouses in one tile; a week per try.",
                me -> me.spouse != null && !me.spouse.dead && me.children != null && me.children.isEmpty(),
                new String[]{
                        "Congratulations on your marriage! Truly.",
                        "If you're thinking of children, stand in the same tile as your spouse and press Try for a child in the Me tab.",
                        "Every try takes a week, and it won't always work. Be patient.",
                        "A child grows up in this world and may follow your road."},
                new String[]{
                        "Married. Good.",
                        "If you want a family, put you both in the same tile and press Try for a child.",
                        "A week per try. Luck decides the rest.",
                        "Don't ask me for more. I'm not that kind of expert."},
                new String[]{
                        "Either of you can ask. It does not matter who.",
                        "The Me tab has the button."}));

        ALL.add(new L("beasts", "Teeth and spoils", "Hunt fights the wild beasts of a tile and pays their spirit stones; a beast near your level can be tamed as a companion.",
                me -> me.location != null && !me.location.city && me.location.monsters > 0 && GameDriver.time >= 12,
                new String[]{
                        "Careful out there. There are beasts in this tile. I can hear them.",
                        "Press Hunt and you fight the wild beasts of the place. Win, and you take the spirit stones they carry. Every kill counts toward your name, too.",
                        "Monsters are beasts that woke up. They are sharper and they carry more, because they have taken it from the fallen.",
                        "There is another way. If a beast is not much stronger than you, you can try to tame it. Type tame, or press Tame a beast here in the Companions tab of your Backpack, and if it accepts, it follows you and fights at your side."},
                new String[]{
                        "Beasts here. Hear them? Hunt them or avoid them.",
                        "Hunt fights the wild beasts of your tile. Beat one and you keep what it carries. Spirit stones. Kills count for the rankings.",
                        "Monsters are beasts that woke. Smarter, richer, meaner. They keep what they took.",
                        "Tame (a command, and a button in the Backpack's Companions tab) tries to make a beast follow you. A beast more than five levels above you just shrugs. The closer to your level, the better your odds. A Beast Tamer keeps more than one."},
                new String[]{
                        "One more thing. A beast that beats you can finish you, now and then. That is how a Monster is born.",
                        "A tamed companion adds a little to your fighting level. It also has to eat."}));

        ALL.add(new L("ruins", "Places that remember", "Each kind of dungeon gives something different: coin and old techniques, beasts, sharper techniques, or a lot of cultivation.",
                me -> GameDriver.time >= 25 && (me.location != null && me.location.dungeonType != null || knowsDungeon(me)),
                new String[]{
                        "You have been looking at that old place on your map, haven't you? Dungeons are worth a visit.",
                        "There are different kinds, and each gives something different. Press Explore when you stand in one.",
                        "Lost Ruins hold old coin, and now and then an ancient technique. They run dry after a few visits, then they fade away.",
                        "A Beast Lair is a den. You fight the beasts, and when they are gone, so is the lair. Haunted Grounds let you cultivate five times in one visit.",
                        "A Martial Trial sharpens your fighting techniques, if you pass. A Celestial Trial is rare and only lasts a few weeks, but it gives twenty cultivations and sometimes a new technique. Go quickly."},
                new String[]{
                        "Dungeons. Look at your map. Different kinds, different prizes.",
                        "Lost Ruins: old coin, sometimes a technique. A few visits, then empty.",
                        "Beast Lair: you fight the beasts until there are none.",
                        "Martial Trial: pass it and your martial techniques sharpen. Haunted Grounds: five cultivations in one visit. Celestial Trial: twenty, but it lasts only a few weeks."},
                new String[]{
                        "If you belong to a sect, every visit also earns sect points.",
                        "A trial can be failed. Check your level against the place before you go."}));

        ALL.add(new L("spar", "Practice with no price", "Spar with someone in your tile: nobody dies, both of you cultivate, and you like each other better afterwards.",
                me -> GameDriver.time >= 14,
                new String[]{
                        "Want to spar? Nobody gets hurt, I promise.",
                        "Type spar, or press the Spar button, with someone in your tile. It takes a week.",
                        "Win or lose, both of you cultivate a little, and now and then a technique gets sharper.",
                        "And you will like each other better afterwards. Friendships grow in practice yards."},
                new String[]{
                        "Spar. Free practice. Nobody dies.",
                        "It costs a week. Both fighters cultivate once and may sharpen a technique.",
                        "Both walk away liking each other more. A little sad, really.",
                        "Use it on friends. And on strangers you would like to be friends with."},
                new String[]{
                        "You can spar anyone who is eight or older and in your tile. Even a rival.",
                        "The fight preview shows the odds, if you want to know how it will go."}));

        ALL.add(new L("modes", "Who holds the reins", "Manual, Auto and Hybrid: you decide each week, the game plays you like anyone else, or you write a script.",
                me -> GameDriver.time >= 30 && me.pc() != null && me.pc().mode == PlayerControl.Mode.MANUAL,
                new String[]{
                        "Do you ever want to let the world turn for a while without you?",
                        "There are three ways to play. Manual, where you decide every week. Auto, where the game plays you the way it plays everyone else, until you say stop. And Hybrid, where you write a script and your character follows it.",
                        "Even in Manual you can hand over chores. The Control window has duties you can switch on: shop, craft, sect, upkeep and more.",
                        "Auto is good for waiting out years. Hybrid is good for chores like forage five hundred herbs, and then come back to me."},
                new String[]{
                        "Manual, Auto, Hybrid. Know them.",
                        "Manual: you choose every week. Auto: the game plays you like a villager until you stop it. Hybrid: you write a script and the character runs it.",
                        "The Control window has duties. Switch one on, and the game does that work for you even in Manual.",
                        "The Script Manager keeps your scripts. Use it."},
                new String[]{
                        "In Auto, your policies answer the questions that come up: spare or kill, fight or flee.",
                        "Set them before you hand over the reins."}));

        ALL.add(new L("auction", "Going once", "Cities of 500 or more have an auction house for luxuries; be in the city when it is held to bid.",
                me -> me.location != null && Auctions.hasHouse(me.location) && GameDriver.time >= 20,
                new String[]{
                        "You have a good eye. Have you been to an auction yet? Cities with five hundred people or more have an auction house.",
                        "Only luxuries go on the block: Heaven-Sent treasures, Potential Pills, and rare food, wine and poison.",
                        "Sell one in a city and it goes on the block for you. You get ninety in a hundred of what it fetches.",
                        "To bid, be standing in the city when the auction is held. You name the most you would pay for each lot, and the winner pays what the runner-up offered."},
                new String[]{
                        "Auction houses. Cities of five hundred people or more.",
                        "Luxuries only. Heaven-Sent things, Potential Pills, vintage food, wine, poison.",
                        "One is held when five lots are waiting, or six weeks after the first one arrived.",
                        "Be in the city. Name your highest bid per lot. Zero passes. The winner pays the second-best bid."},
                new String[]{
                        "The city's lord takes the rest of the price.",
                        "If you are not there, you lose your chance. The Auction window lists what is on the block."}));

        ALL.add(new L("found", "A name over a door", "From Core Formation you can found a city or a sect; a Merchant can open a shop in any city.",
                me -> me.majorLvl >= 2,
                new String[]{
                        "Core Formation. Do you know what that lets you do?",
                        "You can found a city or a sect. Stand in a wild tile and type found city, or found sect.",
                        "A city is a place for people and shops. You are its lord, and its taxes and sales fill the treasury. A sect needs you to belong to none already. You become its master, and members get extra cultivation and a mission board.",
                        "A shop is simpler. A Merchant presses Profession in any city and opens one. One shop for each realm you have reached, at least one."},
                new String[]{
                        "Core Formation. Now you can found things.",
                        "found city, in a wild tile: you are the lord. found sect, if you belong to none: you are the master.",
                        "A sect gives its members extra cultivation and a mission board. A city gives people, shops, a library, and an auction house when it grows.",
                        "Shops are for Merchants. Profession opens one in a city with a free slot."},
                new String[]{
                        "Careful. Cities and sects draw raiders. Arrays protect them.",
                        "And a name over a door means somebody will want to take it down."}));
    }

    /** The lesson that fits this person now and this speaker, or null. */
    static L next(Character npc, Character me) {
        if (!Settings.lessons || me == null || npc == null || me.player != 1 || me.dead || npc.dead) return null;
        PlayerControl pc = me.pc();
        if (pc == null || GameDriver.time < pc.nextLessonWeek) return null;
        if (npc.age < 14 || npc.trait == Character.Traits.Beast || npc.trait == Character.Traits.Monster || !npc.born) return null;
        if (Dialogue.voiceOf(npc, me) == Voice.HOSTILE) return null;
        if (pc.lessonsSeen == null) pc.lessonsSeen = new HashSet<>();
        for (L l : ALL) {
            if (pc.lessonsSeen.contains(l.id)) continue;
            boolean ok;
            try { ok = l.when.test(me); } catch (RuntimeException e) { ok = false; }
            if (ok) return l;
        }
        return null;
    }

    /** True when the talk should turn to a lesson now (a person who plays is at the prompt, not walking on). */
    static boolean due(Character npc, Character me) {
        return me != null && me.skipping <= 0 && me.isManualPc() && next(npc, me) != null;
    }

    /** Tells the lesson through the talk's own way of speaking (the portrait window, or the log). Marks it as told and starts the cooldown. */
    static void give(Dialogue.Ctx c) {
        L l = next(c.npc, c.me);
        if (l == null) return;
        PlayerControl pc = c.me.pc();
        pc.lessonsSeen.add(l.id);
        pc.nextLessonWeek = GameDriver.time + COOLDOWN;
        Voice v = c.voice;
        boolean warm = v == Voice.WARM || v == Voice.CHEERFUL || v == Voice.SCHOLAR;
        String[] lines = warm ? l.warm : l.dry;
        for (String s : lines) c.say(s);
        String[] o = {"Thank you. That helps.", "Tell me more."};
        int p = c.pick(o, new int[]{Dialogue.KIND, Dialogue.CURIOUS});
        if (p == 1) for (String s : l.more) c.say(s);
        c.rel(1);
        if (c.me.player > 0) System.out.println("[Lesson] " + l.title + ": " + l.gist + "  ('lessons' lists the ones you have heard)");
    }

    /** The `lessons` command: what the person has been told and how many are still to come. */
    static String list(Character me) {
        PlayerControl pc = me.pc();
        StringBuilder sb = new StringBuilder("=== Lessons you have heard ===\n");
        int n = 0;
        if (pc != null && pc.lessonsSeen != null) for (L l : ALL) if (pc.lessonsSeen.contains(l.id)) { sb.append("  ").append(l.title).append(": ").append(l.gist).append('\n'); n++; }
        if (n == 0) sb.append("  None yet. People you meet will tell you things when the time is right.\n");
        sb.append("(Lessons are casual talks with the people you meet; `settings lessons off` stops them. ").append(ALL.size() - n).append(" more to come.)");
        return sb.toString();
    }
}
