package Game;

import java.util.List;
import java.util.function.Predicate;

import static Game.Vignettes.L;
import static Game.Vignettes.calm;
import static Game.Vignettes.earn;
import static Game.Vignettes.give;
import static Game.Vignettes.pay;
import static Game.Vignettes.town;
import static Game.Vignettes.unknownPlace;
import static Game.Vignettes.wild;

/** Little stories for the higher realms: five for each of Foundation, Core Formation, Soul Wandering, Nascent Soul, Ensoulment, Nihility, Ascension, Half-Immortal, Pseudo Immortal and True Immortal. The first ten
 *  stories (in `Vignettes`) cost and give a few stones; these cost and give what a person of the realm can feel. The sums are written {s} (small), {m} (middling) and {b} (big): 1%, 4% and 12% of what a cultivator
 *  of the realm usually owns (`unit`), so a story has the same weight in every realm. The other gifts grow with the realm too: a fraction of the cultivation bar, herbs and metal of the realm's quality, a pill for
 *  the next gate, a piece of gear of the realm's level, a fraction of a Potential Pill (rarely), a technique that sharpens, a place on the map. Each is told once per person, in its own realm only. */
final class VignetteRealms {
    private VignetteRealms() { }

    // ------------------------------------------------------------------ the scale
    /** What a cultivator of the person's realm usually owns (the price of gear of that level). */
    static long unit(Character me) { return Math.round(Math.pow(10.0 * Math.max(0, me.majorLvl) + 5, 2.5) * 10); }

    /** Two significant figures, rounded down. */
    static long nice(long v) {
        if (v < 100) return Math.max(1, v);
        long p = 1;
        while (v / p >= 100) p *= 10;
        return (v / p) * p;
    }

    static long sm(Character me) { return nice(unit(me) / 100); }
    static long md(Character me) { return nice(unit(me) / 25); }
    static long bg(Character me) { return nice(unit(me) / 8); }

    static boolean can(Character me, long n) { return Vignettes.wealth(me) >= n; }

    private static int rnd(int n) { return GameDriver.rand.nextInt(n); }
    private static int quality(Character me, int plus) { return Math.max(1, Math.min(10, me.majorLvl + plus)); }

    /** A share of the whole cultivation bar, never so much that it breaks through by itself. */
    static void boost(Character me, double frac) {
        me.cultivationBase = Math.min(me.cultivationBaseMax * 0.95, me.cultivationBase + me.cultivationBaseMax * frac);
    }

    static void drain(Character me, double frac) {
        me.cultivationBase = Math.max(0, me.cultivationBase - me.cultivationBaseMax * frac);
    }

    private static void pill(Character me, int n) {
        Character.addToBag(me.pillBag, new Item(Item.ItemType.Pill, Item.PillType.Cultivation, Math.min(10, me.majorLvl + 1), n));
    }

    private static int gearLevel(Character me, int plus) { return Math.min(100, (me.majorLvl + 1) * 10 + plus + rnd(4)); }

    private static Item weapon(Character me, int plus) {
        Item w = new Item(me, Item.WeaponType.values()[rnd(Item.WeaponType.values().length)], gearLevel(me, plus));
        me.backpack.add(w);
        return w;
    }

    private static Item robe(Character me, int plus) {
        Item w = new Item(me, gearLevel(me, plus), Item.ItemType.Clothing);
        me.backpack.add(w);
        return w;
    }

    private static void sharpen(Character me) {
        if (me.martialTechnique != null) me.martialTechnique.upgrade(me);
        if (me.martialTechnique2 != null) me.martialTechnique2.upgrade(me);
    }

    private static final Predicate<Character> ANY = me -> true;
    private static Predicate<Character> townWith(final int pct) { return me -> town(me) && can(me, pct == 1 ? sm(me) : pct == 4 ? md(me) : bg(me)); }
    private static Predicate<Character> wildWith(final int pct) { return me -> wild(me) && can(me, pct == 1 ? sm(me) : pct == 4 ? md(me) : bg(me)); }

    private static void add(List<Vignettes.Story> all, int realm, String id, String title, Predicate<Character> when, String[] text, String prompt, String[] options, int dflt, Vignettes.Resolve r) {
        all.add(new Vignettes.Story(id, title, when, text, prompt, options, dflt, r, realm, realm));
    }

    static void addTo(List<Vignettes.Story> all) {
        foundation(all); core(all); soul(all); nascent(all); ensoul(all); nihility(all); ascension(all); half(all); pseudo(all); immortal(all);
    }

    // ================================================================== 1 Foundation
    private static void foundation(List<Vignettes.Story> all) {
        add(all, 1, "boy_at_gate", "The boy who wants to learn", Vignettes::town,
                new String[]{
                        "* A boy of about twelve has followed you through the market for an hour. He thinks you cannot see him.",
                        "@Boy| Please. Teach me one thing. I will carry your bags and I will not talk.",
                        "* He has talked the entire time."},
                "What do you do?", new String[]{"Teach him one thing today.", "Pay {s} spirit stones so he can study at a sect.", "Send him home."}, 2,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.06); return L("* You show him how to sit and how to breathe. Saying it aloud, you find two holes in your own habits and mend them.", "* (A little more cultivation: six in a hundred of the bar.)"); }
                    if (p == 1 && pay(me, sm(me))) { calm(me, 6, 0.06); return L("@Boy| A real sect? With a roof?", "* He bows so deeply that he falls over. You feel light for weeks. (-{s} ss, a calmer mind for six weeks)"); }
                    return L("* He goes, slowly, looking back twice.", "* You will think of him at odd moments, for a while.");
                });

        add(all, 1, "cold_forge", "The cold forge", townWith(4),
                new String[]{
                        "* The smith's forge on the corner has been cold since autumn. His widow sits in the doorway with one half-finished blade across her knees.",
                        "@Widow| He never finished it. I cannot. And I cannot sell it to someone who will hang it on a wall.",
                        "@Widow| {m} stones, and you carry it. Use it."},
                "What do you do?", new String[]{"Pay {m} spirit stones for the blade.", "Offer {s} stones and a promise to use it.", "Walk on."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { Item w = weapon(me, 0); return L("* She wraps it in cloth, and her hands do not shake.", "* A good blade, finished by an honest hand. (-{m} ss; " + w + " is in your backpack)"); }
                    if (p == 1 && pay(me, sm(me))) {
                        if (rnd(2) == 0) { Item w = weapon(me, -4); return L("@Widow| A promise. Hm. He would have liked that. Take it.", "* (-{s} ss; " + w + " is in your backpack)"); }
                        return L("@Widow| A promise is not money. I am sorry.", "* She keeps the blade. You keep your stones, and a small ache.");
                    }
                    return L("* The forge stays cold behind you.");
                });

        add(all, 1, "toll_pass", "A toll in the pass", Vignettes::wild,
                new String[]{
                        "* The road narrows between two cliffs. Three men sit on a fallen log, eating, with a rope across the path.",
                        "@Bandit| The toll is {s} stones. Cheaper than a funeral.",
                        "* They try to look fierce. One of them has jam on his chin."},
                "What do you do?", new String[]{"Pay the toll ({s} ss).", "Fight them.", "Find another way."}, 0,
                (me, p) -> {
                    if (p == 0) {
                        if (pay(me, sm(me))) return L("@Bandit| Pleasure doing business.", "* He counts the stones twice. (-{s} ss)");
                        return L("@Bandit| No stones? Go on, then. I cannot rob a pauper, it ruins my reputation.");
                    }
                    if (p == 1) {
                        if (rnd(100) < 55 + 6 * me.majorLvl) { earn(me, md(me)); return L("* It takes four moves. They run, and leave a purse and half a loaf of bread.", "* (+{m} ss)"); }
                        long lost = Math.min(sm(me), Vignettes.wealth(me)); if (lost > 0) pay(me, lost); drain(me, 0.04);
                        return L("* There were more of them in the rocks than you thought. You come away bruised, a little poorer, and short of breath.", "* (-{s} ss, a little cultivation lost)");
                    }
                    Area a = unknownPlace(me, true); if (a == null) a = unknownPlace(me, false);
                    if (a != null) { me.learnPlace(a); return L("* The goat track is steep, and it takes the whole day. At the top you can see for miles.", "* A place is on your map now: " + a.name + " at " + PlayerControl.coord(a) + "."); }
                    return L("* The goat track is steep, and it takes the whole day. The view is very good.");
                });

        add(all, 1, "warm_spring", "The warm spring", Vignettes::wild,
                new String[]{
                        "* Steam rises between two rocks. Someone has set a flat stone beside the water, as if for a guest.",
                        "* The water is the colour of weak tea and smells faintly of iron and mint."},
                "What do you do?", new String[]{"Soak for a while.", "Bottle some water to sell.", "Do not trust it."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.07); return L("* Muscles you did not know you had unclench one by one. You stay until the sun goes.", "* (More cultivation: seven in a hundred of the bar.)"); }
                    if (p == 1) { earn(me, sm(me)); return L("* A healer in the next town pays well for the flask and asks where you found it. You say you cannot remember.", "* (+{s} ss)"); }
                    return L("* A bird lands in the water, stays, and looks at you. You take it as a yes, and keep walking anyway.");
                });

        add(all, 1, "nameless_letter", "The letter with no name", Vignettes::town,
                new String[]{
                        "* A letter lies in the road, sealed, with no name on it. The wax is still soft.",
                        "* A boy runs by, shouting that someone dropped something. He does not wait to see who."},
                "What do you do?", new String[]{"Open it.", "Give it to the guards.", "Leave it."}, 1,
                (me, p) -> {
                    if (p == 0) {
                        if (rnd(2) == 0) { calm(me, 6, 0.06); return L("* It is a love letter, two pages, with a crossed-out line in the middle. Someone was afraid to say it.", "* You fold it and put it back where you found it. (A calmer mind for six weeks)"); }
                        earn(me, md(me)); return L("* It is a note about a hiding place: the third stone of the well in the yard behind the dyer's. You find a small pouch.", "* (+{m} ss)");
                    }
                    if (p == 1) { calm(me, 4, 0.05); return L("* A guard thanks you without looking up. Three days later he tells you the owner cried when it came back.", "* (A calmer mind for four weeks)"); }
                    return L("* It is still in the road when you look back. Someone steps over it.");
                });
    }

    // ================================================================== 2 Core Formation
    private static void core(List<Vignettes.Story> all) {
        add(all, 2, "inn_stone", "The stone under the inn", Vignettes::town,
                new String[]{
                        "* The innkeeper pulls you aside. Workers mending the floor lifted a stone, and under it lay a smaller one cut with fine lines.",
                        "@Innkeeper| It hums when I touch it. My cook says it is haunted. My wife says it is valuable. I say I would like it gone.",
                        "@Innkeeper| {b} stones, and it is yours."},
                "What do you do?", new String[]{"Buy it ({b} ss).", "Tell him to show it to the city's lord.", "Tell him to put the floor back."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, bg(me))) {
                        Item a = new Item(me, gearLevel(me, 0), Item.ItemType.Array); Character.addToBag(me.arrayBag, a);
                        return L("* It is a real array stone, the old kind. It will guard a place for as long as someone keeps it.", "* (-{b} ss; " + a + " is in your array bag)");
                    }
                    if (p == 1) { earn(me, sm(me)); calm(me, 6, 0.05); return L("* The lord sends a purse and a note thanking you for your honesty. The note is two lines long. You read it three times.", "* (+{s} ss, a calmer mind for six weeks)"); }
                    return L("* He puts the floor back. Behind you the stone hums, very quietly, then stops.");
                });

        add(all, 2, "broken_wheel", "The wagon with the broken wheel", Vignettes::wild,
                new String[]{
                        "* A merchant's wagon sits crooked on the road with a wheel in two pieces. The merchant and his two sons look up the hill and then at each other.",
                        "@Merchant| Wolves, up there. Three nights I have seen their eyes. If you could stay until we are mended...",
                        "@Merchant| Or buy the cargo from me, and I will walk home light. {m} stones."},
                "What do you do?", new String[]{"Guard them while they mend the wheel.", "Buy the cargo ({m} ss).", "Wish them luck."}, 0,
                (me, p) -> {
                    if (p == 0) {
                        if (rnd(100) < 60 + 6 * me.majorLvl) { earn(me, md(me)); give(me, Item.ItemType.Herb, quality(me, 1), 120); return L("* The wolves come at midnight and leave at one. Nobody tells the sons how it ended, but they look at you like a legend.", "@Merchant| Take it. Coin and herbs. And my thanks.", "* (+{m} ss, +120 herbs of quality " + quality(me, 1) + ")"); }
                        earn(me, sm(me)); drain(me, 0.04); return L("* The wolves were more than three. The wagon is safe, and you have a sore shoulder and a lighter qi for a day.", "* (+{s} ss, a little cultivation lost)");
                    }
                    if (p == 1 && pay(me, md(me))) { give(me, Item.ItemType.Metal, quality(me, 1), 400); return L("* A heavy crate of ore, and the merchant walks away whistling.", "* (-{m} ss, +400 metal of quality " + quality(me, 1) + ")"); }
                    return L("* From the next hill you see a small fire. You hope the wheel holds.");
                });

        add(all, 2, "honest_thief", "The honest thief", Vignettes::town,
                new String[]{
                        "* A thin man taps your elbow and holds out your own purse. He does not meet your eyes.",
                        "@Thief| I took it. I do not know why. It looked so... easy to take.",
                        "@Thief| I would like to stop. Could you tell someone I am not so bad? I want a job at the docks."},
                "What do you do?", new String[]{"Write him a good word.", "Call the guard.", "Tell him to keep a share and go to the docks."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.04); return L("* You write three honest lines. He reads them twice, moving his lips.", "* Doing someone good is its own kind of practice. (A little more cultivation.)"); }
                    if (p == 1) return L("* The guard takes him away, and he does not fight. You feel nothing about it for a long moment, and then something.");
                    pay(me, Math.min(sm(me), Vignettes.wealth(me))); calm(me, 6, 0.06);
                    return L("* He weeps, bows, and keeps the stones. A letter comes a month later: he is hauling crates, and he is tired, and he is glad. (-{s} ss, a calmer mind for six weeks)");
                });

        add(all, 2, "storm_cave", "Rain on the mountain", Vignettes::wild,
                new String[]{
                        "* The storm hits all at once, like a thrown bucket. You find a cave, and in it a very old man tending a very small fire.",
                        "@Hermit| Sit. Dry yourself. The rain does not take guests. It will be gone by morning.",
                        "@Hermit| While we wait, I could show you how I breathe. Or we could just sit. Both are fine."},
                "What do you do?", new String[]{"Learn how he breathes.", "Share your food and just sit.", "Brave the storm and leave."}, 1,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.08); sharpen(me); return L("* It is a slow breath with a long stop at the top, as if you were holding a very full cup.", "* By dawn it comes without thinking. (More cultivation, and a technique sharpens)"); }
                    if (p == 1) { calm(me, 8, 0.07); give(me, Item.ItemType.Herb, quality(me, 2), 30); return L("* You talk about nothing until the fire is a red coal. In the morning he gives you a small bundle without a word.", "* (A calmer mind for eight weeks, +30 herbs of quality " + quality(me, 2) + ")"); }
                    return L("* You are soaked by the second bend, and it is the best story you tell for a year.");
                });

        add(all, 2, "giant_bone", "A bone the size of a house", Vignettes::wild,
                new String[]{
                        "* A farmer's plough has turned up a rib as long as a cart. The whole village stands around it, arguing.",
                        "@Farmer| It is a dragon! It is a cow! It is the Heavens' joke! I want {m} stones for it, and I want it off my field."},
                "What do you do?", new String[]{"Pay {m} spirit stones for the bone.", "Offer to haul it away for nothing.", "Look at it and go."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { give(me, Item.ItemType.Metal, quality(me, 2), 300); return L("* A smith in the next town grinds a sliver of it and goes very quiet. It works like metal from deep under a mountain.", "* (-{m} ss, +300 metal of quality " + quality(me, 2) + ")"); }
                    if (p == 1) { boost(me, 0.05); earn(me, sm(me)); return L("* It takes eleven villagers, four ropes and a lot of swearing. They feed you for three days and press a gift into your hands.", "* (+{s} ss, a little more cultivation)"); }
                    return L("* Behind you the argument starts again, louder.");
                });
    }

    // ================================================================== 3 Soul Wandering
    private static void soul(List<Vignettes.Story> all) {
        add(all, 3, "own_face", "Your own sleeping face", ANY,
                new String[]{
                        "* In the night your soul steps out and stands at the foot of your own bed. It is not as frightening as the books said.",
                        "* Your body lies asleep, a little crooked, with one hand open on the blanket.",
                        "* The window is open. The sky is very large."},
                "What do you do?", new String[]{"Fly over the roofs.", "Look at your own face.", "Go back at once."}, 1,
                (me, p) -> {
                    if (p == 0) {
                        Area a = unknownPlace(me, true); if (a == null) a = unknownPlace(me, false);
                        if (a != null) { me.learnPlace(a); return L("* Over the roofs, the fields, the dark hills. Far away a place glows faintly: " + a.name + ".", "* In the morning you remember where it was: " + PlayerControl.coord(a) + ". (It is on your map.)"); }
                        boost(me, 0.05); return L("* You fly until the light changes. You wake with your legs aching, as if you had walked.", "* (More cultivation.)");
                    }
                    if (p == 1) { calm(me, 10, 0.08); return L("* You look tired. You look kind. You look like someone you would be glad to meet.", "* You make a note to be gentler with that person. (A calmer mind for ten weeks)"); }
                    return L("* Back in the body, quickly, like a hand into a glove. It is warm. It is yours.");
                });

        add(all, 3, "ghost_debt", "The ghost who owed money", Vignettes::wild,
                new String[]{
                        "* A faint figure sits on a milestone and does not look up. You realise you can see through his hat.",
                        "@Ghost| Forty years ago I borrowed {m} stones from a grocer in the next town. I never paid. I cannot rest until it is paid.",
                        "@Ghost| You are the first person who has listened in a very long time."},
                "What do you do?", new String[]{"Pay the grocer {m} ss for him.", "Argue the debt down to {s} ss.", "Say you cannot hear ghosts."}, 0,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.12); calm(me, 8, 0.07); return L("* The grocer is astonished and gives you a receipt. When you come back to the milestone, it is just a milestone.", "* A warm breath goes through you, like a thank you. (-{m} ss, more cultivation, a calmer mind)"); }
                    if (p == 1) {
                        if (rnd(100) < 60 && pay(me, sm(me))) { boost(me, 0.08); return L("@Ghost| Forty years of interest, and you wipe it with a smile. I like you.", "* (-{s} ss, more cultivation)"); }
                        drain(me, 0.03); return L("@Ghost| Argue? ARGUE?", "* The wailing goes on for an hour. You leave with a headache and a slightly thinner qi.");
                    }
                    return L("@Ghost| Yes. Yes, of course. Sorry to have bothered you.", "* You walk for a long time with your back very straight.");
                });

        add(all, 3, "memory_stall", "A stall of small bottles", townWith(4),
                new String[]{
                        "* In a back lane a stall sells small glass bottles, each stoppered with wax. Each has a thin thread of light inside.",
                        "@Seller| Memories. Bought and sold. A summer evening. A first kiss. A good meal.",
                        "@Seller| {m} stones for one, or I buy one of yours for {s}. You will not miss it, I promise. Most people do not."},
                "What do you do?", new String[]{"Buy a bottle ({m} ss).", "Sell one of your own memories ({s} ss).", "Just look."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.1); me.givePotential(0.1, "In the bottle was someone's perfect morning of cultivation, still warm."); return L("* You uncork it on a quiet roof. For a few breaths you know how it feels to be someone who is exactly where they should be.", "* (-{m} ss, more cultivation, and a stir in your potential)"); }
                    if (p == 1) { earn(me, sm(me)); drain(me, 0.03); return L("* He takes it with tongs. You cannot say what it was. That is the odd part: you know that you had it.", "* (+{s} ss, a little cultivation lost)"); }
                    return L("* Every bottle glows a little brighter when you pass. Maybe it is the lamp.");
                });

        add(all, 3, "answering_cave", "The cave that answers", Vignettes::wild,
                new String[]{
                        "* You shout your name into a cave, the way a child would. The cave says it back in a different voice.",
                        "* Then, after a moment, it says something else."},
                "What do you do?", new String[]{"Ask it a question.", "Sing to it.", "Leave quietly."}, 2,
                (me, p) -> {
                    if (p == 0) {
                        if (rnd(2) == 0) { boost(me, 0.08); return L("@Cave| You already know. You have always known. You are afraid of how simple it is.", "* You sit at the mouth for an hour and find that it is true. (More cultivation.)"); }
                        return L("@Cave| Ask again tomorrow.", "* It is not clear whether the cave is wise or only rude.");
                    }
                    if (p == 1) { calm(me, 8, 0.08); return L("* It sings back a half beat late, and then it harmonises, which no cave should be able to do.", "* (A calmer mind for eight weeks)"); }
                    return L("* The echo says goodbye in your own voice. You do not turn around.");
                });

        add(all, 3, "fox_wedding", "A wedding in the rain", Vignettes::wild,
                new String[]{
                        "* The sun is out and it is raining at the same time. Down the road, a small procession with red lanterns winds between the trees.",
                        "* The bride is a fox in a bridal veil. So is the groom. So are the guests."},
                "What do you do?", new String[]{"Follow at a distance.", "Bow as they pass.", "Hide behind a tree."}, 1,
                (me, p) -> {
                    if (p == 0) {
                        if (rnd(100) < 50) { Area a = unknownPlace(me, true); if (a == null) a = unknownPlace(me, false); if (a != null) { me.learnPlace(a); return L("* They lead you to a quiet hollow with a long table, and then, politely, the lanterns go out.", "* In the morning you remember the way: " + a.name + " at " + PlayerControl.coord(a) + ". (It is on your map.)"); } }
                        drain(me, 0.02); return L("* They notice you. The lanterns go out at once. You walk home in the dark with leaves in your hair.");
                    }
                    if (p == 1) { give(me, Item.ItemType.Herb, quality(me, 1), 80); return L("* The bride nods, very slightly. As the last guest goes by, something drops at your feet.", "* (+80 herbs of quality " + quality(me, 1) + ")"); }
                    return L("* You hold your breath until the lanterns are gone. The rain stops at once.");
                });
    }

    // ================================================================== 4 Nascent Soul
    private static void nascent(List<Vignettes.Story> all) {
        add(all, 4, "quiet_room", "Ten years in a quiet room", townWith(4),
                new String[]{
                        "* A patron of the city keeps a sealed room for guests who need to be left alone. Stone walls, a mat, one lamp, no windows.",
                        "@Steward| A month costs {m} stones. A week costs {s}. Food is passed through the slot.",
                        "@Steward| Most people come out looking five years older, and ten years wiser."},
                "What do you do?", new String[]{"Take the room for a month ({m} ss).", "Take it for a week ({s} ss).", "Decline."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.22); calm(me, 12, 0.08); return L("* The first week is boredom. The second is fear. The third is a long quiet where the Nascent Soul in you stretches like a cat.", "* (-{m} ss, much more cultivation, a calmer mind for twelve weeks)"); }
                    if (p == 1 && pay(me, sm(me))) { boost(me, 0.08); return L("* Seven days. You mostly sleep. But on the last evening you hear your own breath for the first time.", "* (-{s} ss, more cultivation)"); }
                    return L("* The door closes on someone else, and the lamp goes out in the slot.");
                });

        add(all, 4, "weighing_woman", "The woman with the scales", Vignettes::town,
                new String[]{
                        "* On a bridge an old woman sits with a tiny brass scale on her knee. A line of people waits, and nobody is in a hurry.",
                        "@Weigher| Step up. I weigh what you carry. Not your bag. The other thing.",
                        "@Weigher| {m} stones, or you can tell me what the weight is for, and I will think about it."},
                "What do you do?", new String[]{"Pay {m} ss to be weighed.", "Tell her what you carry.", "Step out of the line."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.1); calm(me, 8, 0.07); return L("* The scale tips, then rocks. 'Put one thing down,' she says. 'You know which.' You do.", "* You feel lighter for weeks. (-{m} ss, more cultivation, a calmer mind)"); }
                    if (p == 1) { boost(me, 0.06); return L("* You talk. She listens like a lamp. When you finish, the scale is level.", "@Weigher| There. That was the price.", "* (A little more cultivation.)"); }
                    return L("* The next in line steps up, and the scale tips, and the old woman laughs.");
                });

        add(all, 4, "proud_boy", "The boy who wants to beat you", Vignettes::town,
                new String[]{
                        "* A fifteen-year-old planted himself in front of you in the square. He has a wooden sword and a very serious face.",
                        "@Boy| They say you are strong. I am going to be stronger. I would like to start now."},
                "What do you do?", new String[]{"Spar gently.", "Win quickly.", "Refuse."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.05); calm(me, 6, 0.06); return L("* You give him ten minutes of the best swordplay he has ever had. He loses, and shines with it.", "* (A little more cultivation, a calmer mind)"); }
                    if (p == 1) { earn(me, sm(me)); return L("* One move. The wooden sword lands in the fountain. His father comes out of a shop and pays you, a little stiffly, for a lesson.", "* (+{s} ss)"); }
                    return L("* He bows, very formally, and goes off to challenge a mule.");
                });

        add(all, 4, "pawned_sword", "The sword in the pawnshop", townWith(4),
                new String[]{
                        "* In a pawnshop window hangs a sword with a worn grip and a chip near the guard. It looks like it has been used for something.",
                        "@Pawnbroker| The owner never came back for it. A good sword. A sad one.",
                        "@Pawnbroker| {b} stones. Or I can tell you who owned it, for free, if you stay for tea."},
                "What do you do?", new String[]{"Buy it ({b} ss).", "Stay for tea.", "Walk on."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, bg(me))) { Item w = weapon(me, 4); return L("* The grip fits as if it had waited for your hand.", "* (-{b} ss; " + w + " is in your backpack)"); }
                    if (p == 1) { calm(me, 8, 0.07); boost(me, 0.04); return L("@Pawnbroker| A courier. He sang while he fought, badly, so everyone knew where he was. He was never late.", "* You drink three cups. The sword hangs in the window, listening. (A calmer mind, a little more cultivation)"); }
                    return L("* In the glass your reflection and the sword cross for a moment.");
                });

        add(all, 4, "summit_oath", "The oath at the summit", Vignettes::wild,
                new String[]{
                        "* At the top of a hard climb, a cloaked traveller sits on a flat rock and looks at the valley.",
                        "@Traveller| I carry a secret I cannot say and cannot throw away. I need someone to hold it for ten years. I will pay {m} stones.",
                        "@Traveller| You will not be asked to do anything. Only not to tell."},
                "What do you do?", new String[]{"Swear.", "Refuse.", "Ask what it is about."}, 1,
                (me, p) -> {
                    if (p == 0) { earn(me, md(me)); calm(me, 6, 0.05); return L("* He puts his hand on yours, and the oath settles in you like a small, warm stone.", "* (+{m} ss, a calmer mind for six weeks. The secret is yours now.)"); }
                    if (p == 2) { boost(me, 0.04); return L("@Traveller| It is about a door. That is all I may say.", "* You think about doors for the rest of the walk. (A little more cultivation)"); }
                    return L("@Traveller| Wise. I would have refused myself.", "* He smiles and looks at the valley again. You leave him to it.");
                });
    }

    // ================================================================== 5 Ensoulment
    private static void ensoul(List<Vignettes.Story> all) {
        add(all, 5, "name_stone", "The stone that remembers names", Vignettes::wild,
                new String[]{
                        "* A standing stone, tall as three men, covered with names. Some are cut deep. Some are scratched and weathered. Some are new.",
                        "* There is a small blank space near the base, at the height of your hand."},
                "What do you do?", new String[]{"Carve your name.", "Read the names.", "Leave it alone."}, 1,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.08); calm(me, 12, 0.08); return L("* It takes an hour, and your hand shakes at the last letter. The stone is cool, and then, very faintly, warm.", "* (More cultivation, a calmer mind for twelve weeks)"); }
                    if (p == 1) {
                        Area a = unknownPlace(me, true); if (a == null) a = unknownPlace(me, false);
                        if (a != null) { me.learnPlace(a); return L("* Names of heroes and fools. Beside one, small, someone has scratched a direction: " + a.name + ".", "* It is on your map now: " + PlayerControl.coord(a) + "."); }
                        boost(me, 0.04); return L("* You find one you know, and say it aloud.", "* (A little more cultivation)");
                    }
                    return L("* The blank space waits. It has waited a long time.");
                });

        add(all, 5, "open_tomb", "A tomb with the door open", Vignettes::wild,
                new String[]{
                        "* A fresh-dug tomb, the earth still dark. Three men argue at the door about how to split what they carried out.",
                        "* One looks at you and says, too quickly, that the tomb was empty."},
                "What do you do?", new String[]{"Ask for a share.", "Drive them off and seal the door.", "Walk away."}, 1,
                (me, p) -> {
                    if (p == 0) { earn(me, md(me)); if (rnd(100) < 35) pill(me, 1); drain(me, 0.03); return L("* They hand over a share without looking at you. It is warm in your hand, and you do not like that.", "* (+{m} ss, and maybe a pill; you feel unclean and lose a little cultivation)"); }
                    if (p == 1) { boost(me, 0.1); earn(me, sm(me)); return L("* They run. You seal the door with the stone it came with, and say a few words you hardly knew you remembered.", "* A tomb keeper hears of it and sends a gift. (+{s} ss, more cultivation)"); }
                    return L("* Their voices fade behind you. It is a very quiet evening.");
                });

        add(all, 5, "dry_ferry", "The ferry over the dry river", Vignettes::wild,
                new String[]{
                        "* A ferryman stands by a boat in the sand, beside a wide river bed with not one drop of water in it.",
                        "@Ferryman| {m} stones to cross. I get a lot of people who think it is a joke.",
                        "@Ferryman| It is not a joke. It is a river."},
                "What do you do?", new String[]{"Pay {m} ss and get in the boat.", "Ask why.", "Walk across."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.1); return L("* The boat slides over dry sand like a boat on water, and for a moment, between the banks, the water does come, high and dark and quiet.", "* On the far bank he hands you a coin. 'For luck.' (-{m} ss, more cultivation)"); }
                    if (p == 1) { boost(me, 0.04); return L("@Ferryman| It comes back in spring, and some years in autumn. In between, people forget it ever did.", "@Ferryman| I do not forget. That is my whole job.", "* (A little more cultivation)"); }
                    drain(me, 0.02); pay(me, Math.min(sm(me), Vignettes.wealth(me)));
                    return L("* It is not dry. It is a thin film of water over mud, ankle deep, ice cold. The ferryman is laughing so hard he has to sit down.", "* (-{s} ss for the boots you ruin, a little cultivation lost)");
                });

        add(all, 5, "old_student", "Someone you taught", Vignettes::town,
                new String[]{
                        "* A grey-haired stranger stops you in the street and bows very low.",
                        "@Stranger| You taught me when I was twelve. A week, nothing more. I became a magistrate because of you.",
                        "@Stranger| I do not expect you to remember."},
                "What do you do?", new String[]{"Say you remember.", "Ask what he became.", "Say you do not remember, but you are glad."}, 2,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.06); calm(me, 8, 0.06); return L("* He looks at you as if you had given him the sun. You find that, a little, you do remember.", "* (More cultivation, a calmer mind)"); }
                    if (p == 1) { earn(me, md(me)); return L("@Magistrate| A fair one, I hope. Please, take this. It is not payment. It is a thank you with a number on it.", "* (+{m} ss)"); }
                    earn(me, sm(me)); return L("@Magistrate| That is the best answer. I would have been suspicious of the other.", "* He insists on buying you tea. (+{s} ss in a gift)");
                });

        add(all, 5, "sealed_jar", "The jar that should not be opened", townWith(4),
                new String[]{
                        "* A merchant has a stall with only one item on it: a sealed jar, black clay, red wax, a string of paper charms.",
                        "@Merchant| Do not open it. That is the price. {m} stones, and you never open it.",
                        "@Merchant| People who open it do not come back to ask for their stones."},
                "What do you do?", new String[]{"Buy it and open it.", "Buy it and keep it sealed.", "No."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) {
                        if (rnd(2) == 0) { pill(me, 1); return L("* Inside is a single pill, wrapped in silk, and a note: 'You were not supposed to open it, but here we are.'", "* (-{m} ss, +1 pill for the next gate)"); }
                        drain(me, 0.05); return L("* Dust, and a cold wind that goes through you and out the door. For a minute you cannot remember your own name.", "* (-{m} ss, a little cultivation lost)");
                    }
                    if (p == 1 && pay(me, md(me))) { calm(me, 12, 0.07); return L("* You put it on a shelf. Some nights you feel it there, patient, and it is oddly comforting.", "* (-{m} ss, a calmer mind for twelve weeks)"); }
                    return L("* The merchant nods as if you had passed something.");
                });
    }

    // ================================================================== 6 Nihility
    private static void nihility(List<Vignettes.Story> all) {
        add(all, 6, "silent_village", "A village with no sound", Vignettes::wild,
                new String[]{
                        "* You walk into a village where nobody speaks. Children play without noise. A dog barks, and its mouth moves, and the sound never arrives.",
                        "* An old woman touches her ear, then her heart, then points at a chair beside her door."},
                "What do you do?", new String[]{"Sit with them in the silence.", "Shout, to see what happens.", "Trade in gestures."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.12); calm(me, 10, 0.08); return L("* The silence has weight and no edges. By dusk you have not thought a word for an hour.", "* (Much more cultivation, a calmer mind for ten weeks)"); }
                    if (p == 1) { drain(me, 0.02); return L("* Nothing comes out. Everyone looks at you with kind amusement. You feel like a very loud child.", "* (A little cultivation lost)"); }
                    earn(me, md(me)); return L("* A great deal of pointing, counting on fingers, and one comedy of errors with a cabbage. You leave with more than you came with.", "* (+{m} ss)");
                });

        add(all, 6, "grey_mirror", "The mirror that shows nothing", Vignettes::town,
                new String[]{
                        "* A shop sells a single mirror in a plain frame. The glass shows a soft grey, like fog.",
                        "@Seller| It reflects what you are not. Most people find it unbearable.",
                        "@Seller| Look as long as you like. If you want to take it home, {b} stones."},
                "What do you do?", new String[]{"Look into it for a while.", "Buy it ({b} ss).", "Turn away."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.1); return L("* In the grey you see everything you are not: not hungry, not afraid, not a name. What is left is a lot of room.", "* (More cultivation.)"); }
                    if (p == 1 && pay(me, bg(me))) { calm(me, 20, 0.1); boost(me, 0.08); return L("* You hang it over your bed. On bad nights you look into it, and the night gets smaller.", "* (-{b} ss, a calmer mind for twenty weeks, more cultivation)"); }
                    return L("* The seller watches you go with something like respect.");
                });

        add(all, 6, "forgotten_road", "The road nobody remembers", Vignettes::wild,
                new String[]{
                        "* A paved road runs off the main way, overgrown, with milestones whose numbers have worn off. It is on no map you have seen.",
                        "* Nobody you ask in the next village has heard of it."},
                "What do you do?", new String[]{"Follow it.", "Mark it with a pile of stones and go on.", "Turn back."}, 0,
                (me, p) -> {
                    if (p == 0) {
                        Area a = unknownPlace(me, true); if (a == null) a = unknownPlace(me, false);
                        boost(me, 0.05);
                        if (a != null) { me.learnPlace(a); return L("* It goes a long way, and at its end is " + a.name + ", at " + PlayerControl.coord(a) + ". The road is gone when you look back.", "* (It is on your map. A little more cultivation.)"); }
                        return L("* It goes a long way and ends at a wall. The road is gone when you look back.", "* (A little more cultivation.)");
                    }
                    if (p == 1) { calm(me, 6, 0.05); return L("* Three stones, one on another. In a week it will be gone, but it is nice to leave a mark.", "* (A calmer mind for six weeks)"); }
                    return L("* The road waits for someone else.");
                });

        add(all, 6, "giving_monk", "The monk who owns nothing", Vignettes::town,
                new String[]{
                        "* A monk sits in the shade of a tree with a bowl in his lap. He owns the bowl, he says, and tomorrow he will not.",
                        "@Monk| I ask each guest one thing: give something away today, freely, without keeping score.",
                        "@Monk| It does not have to be much. It just has to be true."},
                "What do you do?", new String[]{"Give {m} spirit stones to the first person you meet.", "Give {s} spirit stones.", "Keep everything and thank him."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.14); calm(me, 8, 0.07); return L("* A porter, tired and surprised. His laugh is so loud that three people turn to look.", "* The coin leaves your hand, and so does something else. (-{m} ss, much more cultivation, a calmer mind)"); }
                    if (p == 1 && pay(me, sm(me))) { boost(me, 0.07); return L("* A child with a sore foot. You do not watch where it goes.", "* (-{s} ss, more cultivation)"); }
                    return L("@Monk| That is honest too. Come back when you are ready.", "* He smiles and closes his eyes.");
                });

        add(all, 6, "bottomless_pond", "The pond with no bottom", Vignettes::wild,
                new String[]{
                        "* A round black pond lies in a clearing. You drop a pebble in, and listen for the splash. It does not come.",
                        "* The water is perfectly still. Your reflection is a little too sharp."},
                "What do you do?", new String[]{"Drop a coin ({s} ss).", "Put your hand in the water.", "Step back."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, sm(me))) { boost(me, 0.06); return L("* A faint ding from very deep, like a bell in another house.", "* (-{s} ss, a little more cultivation)"); }
                    if (p == 1) {
                        if (rnd(100) < 30) { me.givePotential(0.15, "The cold goes up your arm, and something in it teaches."); return L("* For a second your hand is not there at all, and you know exactly what it is made of.", "* (A stir in your potential)"); }
                        drain(me, 0.03); return L("* The cold goes up to your shoulder and stays all day. Your hand is numb until the evening.", "* (A little cultivation lost)");
                    }
                    return L("* You take one step back. The pond does not mind.");
                });
    }

    // ================================================================== 7 Ascension
    private static void ascension(List<Vignettes.Story> all) {
        add(all, 7, "noon_lightning", "The field the lightning loves", Vignettes::wild,
                new String[]{
                        "* A farmer stands at the edge of a perfectly ordinary field and watches the sky with his hat in his hands.",
                        "@Farmer| Every day at noon, in the same spot. Nothing else is hit. Not the barn, not the cow. Just that.",
                        "@Farmer| Pay me nothing. If you can stand in it, I would like to know what it wants."},
                "What do you do?", new String[]{"Stand in the spot at noon.", "Watch from a distance.", "Tell him to move the field."}, 1,
                (me, p) -> {
                    if (p == 0) {
                        if (rnd(100) < 60 + 3 * me.majorLvl) { boost(me, 0.16); sharpen(me); return L("* The bolt comes down like a hand on your shoulder, and it is not angry. It stays a moment and leaves everything a little brighter.", "* (Much more cultivation, and a technique sharpens)"); }
                        drain(me, 0.08); return L("* It is angry, and it is very fast. You wake in a ditch with smoke coming from your sleeves.", "* (A good deal of cultivation lost)");
                    }
                    if (p == 1) { boost(me, 0.05); return L("* It strikes. You see the shape of it in the air for a long moment, a tree of light, upside down.", "* (A little more cultivation)"); }
                    return L("@Farmer| It is a good field. I will not.", "* He puts his hat back on and goes to feed the cow.");
                });

        add(all, 7, "endless_stair", "A staircase up the cliff", Vignettes::wild,
                new String[]{
                        "* A staircase has been cut into a sheer cliff, wide enough for one. It goes up into cloud.",
                        "* A small sign at the bottom, in careful brush strokes: 'A thousand steps. The top is not worth it. The climb is.'"},
                "What do you do?", new String[]{"Climb a hundred steps and look.", "Climb all thousand.", "Sit at the bottom and watch the clouds."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.08); calm(me, 6, 0.06); return L("* At the hundredth the village is the size of a coin. You sit there a long time, in a wind like a cool hand.", "* (More cultivation, a calmer mind)"); }
                    if (p == 1) {
                        if (rnd(100) < 55 + 4 * me.majorLvl) { boost(me, 0.22); me.givePotential(0.1, "At the top there is nothing, just a flat stone and a view."); return L("* Your legs shake and you do not care. The air is thin and very clear, and for a breath everything that you have ever done fits into your chest.", "* (Much more cultivation, a stir in your potential)"); }
                        drain(me, 0.05); return L("* At six hundred your knees give out. You come down on your hands, laughing at yourself, with half the day gone.", "* (A little cultivation lost)");
                    }
                    return L("* The clouds shift into a fish, then a face, then a fish again. It is a good afternoon.");
                });

        add(all, 7, "heavens_clerk", "The clerk of the Heavens", townWith(4),
                new String[]{
                        "* A small man in a grey robe, with a ledger as big as a door, stops you outside a teahouse.",
                        "@Clerk| Your name is in the book twice. Two entries. I cannot decide which is correct. One of them is better for you.",
                        "@Clerk| For {m} stones I will choose the better one. It is not exactly legal. It is not exactly illegal either."},
                "What do you do?", new String[]{"Pay {m} ss to have it fixed.", "Report him to the city's lord.", "Ignore him."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.14); me.givePotential(0.1, "He crosses out one line and underlines the other."); return L("* The ink is a color you do not have a name for. 'Thank you for your cooperation,' he says, and vanishes in a way that is mostly polite.", "* (-{m} ss, more cultivation, a stir in your potential)"); }
                    if (p == 1) { earn(me, sm(me)); return L("* The lord's guards find no clerk, no ledger and a very empty alley. A reward comes anyway, for your trouble.", "* (+{s} ss)"); }
                    return L("* You go into the teahouse. When you come out, the alley has a single grey button in it, and nothing else.");
                });

        add(all, 7, "friends_pyre", "A funeral for someone you knew", Vignettes::town,
                new String[]{
                        "* Smoke rises from a pyre outside the city wall. You recognise the name on the banner. You were not close, but you knew them.",
                        "* The family stands in a line, stiff and tired, and nobody knows what to say."},
                "What do you do?", new String[]{"Speak a few words.", "Give {m} spirit stones to the family.", "Stand at the back and say nothing."}, 0,
                (me, p) -> {
                    if (p == 0) { calm(me, 12, 0.08); boost(me, 0.06); return L("* You tell one true story. Someone laughs, then everyone laughs, then everyone cries a little. It was the right story.", "* (A calmer mind for twelve weeks, more cultivation)"); }
                    if (p == 1 && pay(me, md(me))) { boost(me, 0.1); return L("* The widow takes the purse in both hands and does not say a word. It is enough.", "* (-{m} ss, more cultivation)"); }
                    return L("* The smoke goes straight up for a while and then leans. You stay until it is gone.");
                });

        add(all, 7, "cloud_market", "The market above the clouds", Vignettes::wild,
                new String[]{
                        "* On a high ridge, above the cloud line, a few stalls huddle under bright awnings. The merchants wear furs in summer.",
                        "@Merchant| Cloud-silk! Spun from the morning mist. {b} stones a robe, light as a thought.",
                        "@Merchant| Or sell us herbs. Lowland herbs are rare up here."},
                "What do you do?", new String[]{"Buy a robe ({b} ss).", "Sell some herbs.", "Just browse."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, bg(me))) { Item r = robe(me, 3); return L("* It weighs almost nothing, and shines like water in the dusk.", "* (-{b} ss; " + r + " is in your backpack)"); }
                    if (p == 1) { earn(me, md(me)); return L("* You trade a bag of the commonest herbs for a purse of stones. They say it is a fair price, and they seem happy.", "* (+{m} ss)"); }
                    boost(me, 0.04); return L("* The mist rolls over the awnings, and the stalls glow. You buy a cup of hot barley tea.", "* (A little more cultivation)");
                });
    }

    // ================================================================== 8 Half-Immortal
    private static void half(List<Vignettes.Story> all) {
        add(all, 8, "old_cup", "A cup you drank from", Vignettes::town,
                new String[]{
                        "* You walk into a teahouse you have been to before. You know it by the crack in the third step. It has been eighty years.",
                        "* The same cup sits on the shelf, chipped on the rim. The owner's grandson does not know why you are staring at it."},
                "What do you do?", new String[]{"Ask for tea in that cup.", "Ask the owner what he remembers of the old days.", "Leave quietly."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.1); calm(me, 16, 0.09); return L("* The tea tastes of the day you first drank it: of the person across the table, and the rain.", "* (More cultivation, a calmer mind for sixteen weeks)"); }
                    if (p == 1) { earn(me, md(me)); return L("@Owner| My grandfather left a note: 'If an old guest comes back, the deposit is theirs.' I thought it was a joke.", "* An envelope, yellow with age. (+{m} ss)"); }
                    return L("* Some things are better kept in the past. You tell yourself that.");
                });

        add(all, 8, "slow_river", "The river that runs slow", Vignettes::wild,
                new String[]{
                        "* A river bends through a wood, and the leaves on it move like honey. Birds fly past in slow motion.",
                        "* On the bank a stone bench is worn smooth. Time is different here, and everyone who sits down knows it."},
                "What do you do?", new String[]{"Sit for an hour.", "Fill a flask from the river.", "Do not stay."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.2); return L("* An hour passes. When you stand, the sun is exactly where it was, and your qi is as full as after a month of retreat.", "* (Much more cultivation)"); }
                    if (p == 1) { pill(me, 1); return L("* The water in the flask turns thick, then solid, then a little bead of pill in your palm. It does not look edible, but it is.", "* (+1 pill for the next gate)"); }
                    return L("* You walk away. Behind you, the leaves are still falling.");
                });

        add(all, 8, "young_hero", "The young hero", Vignettes::town,
                new String[]{
                        "* A young man with a sword, a scar and a hopeful face is waiting for you at the inn door. Everyone knows who he is.",
                        "@Hero| They call me the Sword of the East. They say nobody has beaten me. I want to know if it is true. May I try?"},
                "What do you do?", new String[]{"Lose on purpose.", "Win honestly.", "Decline kindly."}, 1,
                (me, p) -> {
                    if (p == 0) { earn(me, md(me)); calm(me, 8, 0.07); return L("* You fall back at the right moment, and the crowd roars. His eyes are bright. He bows to you, and he means it.", "* The inn's owner, delighted, covers your night. (+{m} ss, a calmer mind)"); }
                    if (p == 1) { boost(me, 0.08); sharpen(me); return L("* You win, politely. He sits on the ground afterward, laughing, and thanks you.", "* (More cultivation, a technique sharpens)"); }
                    return L("@Hero| Another time, then. I will practice.", "* He bows and goes off to practice on a tree.");
                });

        add(all, 8, "forgotten_sect", "The sect that forgot itself", Vignettes::wild,
                new String[]{
                        "* A ruined hall stands in a mountain meadow, its roof gone. Inside, a shelf holds a few books and a bowl of old rice.",
                        "* On the wall, a faded banner names a sect that you have never heard of."},
                "What do you do?", new String[]{"Take the manuscript.", "Bury the old rice in the meadow and say a few words.", "Leave it as you found it."}, 1,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.1); sharpen(me); sharpen(me); return L("* The pages are brittle, the script old. But two ideas in them are very good, and they were waiting for someone.", "* (More cultivation, your techniques sharpen)"); }
                    if (p == 1) { calm(me, 12, 0.08); if (rnd(100) < 30) me.givePotential(0.15, "You feel an odd, deep approval from the empty hall."); return L("* The grass takes the rice gently. The hall seems less ruined when you look back.", "* (A calmer mind for twelve weeks)"); }
                    return L("* The banner moves in the breeze as if it were waving.");
                });

        add(all, 8, "old_debt", "A debt that outlived its debtor", Vignettes::town,
                new String[]{
                        "* A clerk in a counting-house unrolls a yellow note. Your name is on it, in your own hand, for {m} stones. It is ninety years old.",
                        "@Clerk| The lender is long dead. The house kept the note. Interest at three in the hundred.",
                        "@Clerk| You do not have to pay. But the house would remember."},
                "What do you do?", new String[]{"Pay it ({m} ss).", "Argue that the interest is unfair.", "Say it is not your hand."}, 0,
                (me, p) -> {
                    if (p == 0 && pay(me, md(me))) { boost(me, 0.1); calm(me, 10, 0.07); return L("* He stamps it, folds it, and holds it out to you. You keep it. It is odd to hold a promise you made so long ago.", "* (-{m} ss, more cultivation, a calmer mind)"); }
                    if (p == 1) { if (rnd(2) == 0 && pay(me, sm(me))) { boost(me, 0.05); return L("@Clerk| Hm. The house will settle for {s}.", "* (-{s} ss, a little more cultivation)"); } return L("@Clerk| The house is firm on the rate.", "* You leave with your stones and a feeling that you have lost something larger."); }
                    return L("@Clerk| It is your hand. I have your signature on a hundred other notes.", "* You laugh in spite of yourself and change the subject.");
                });
    }

    // ================================================================== 9 Pseudo Immortal
    private static void pseudo(List<Vignettes.Story> all) {
        add(all, 9, "own_shrine", "A shrine with your face", Vignettes::town,
                new String[]{
                        "* On a side street, between a noodle stand and a barber, someone has built a tiny shrine. The statue is a little too young and a lot too tall, but it is you.",
                        "* There are fresh fruit and a few coins. An old woman lights incense and talks to the statue about her knees."},
                "What do you do?", new String[]{"Take the offerings: they are meant for you.", "Ask them kindly to take it down.", "Leave a blessing and say nothing."}, 1,
                (me, p) -> {
                    if (p == 0) { earn(me, bg(me)); drain(me, 0.06); return L("* The coins are warm and a little heavy. Somewhere, something in you notes that it is easy to be worshipped, and not good for you.", "* (+{b} ss, some cultivation lost)"); }
                    if (p == 1) { boost(me, 0.12); calm(me, 10, 0.08); return L("* The old woman cries a little. The statue goes, and in its place a small bench appears, free for anybody.", "* (More cultivation, a calmer mind)"); }
                    calm(me, 6, 0.06); return L("* You press your palm to the base, and the fruit go a little brighter. The woman never learns who it was.", "* (A calmer mind for six weeks)");
                });

        add(all, 9, "stone_door", "The door in the rock", Vignettes::wild,
                new String[]{
                        "* In a bare cliff a door stands, small, round and plain, with no handle. It is set a hand's breadth into the stone.",
                        "* It was not there yesterday. Or you did not see it. Or it was not there for you."},
                "What do you do?", new String[]{"Knock.", "Push.", "Walk away."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.14); me.givePotential(0.15, "Three knocks answer you from inside, slow, as if from far below."); return L("* After a while, the door sighs open a finger's width and a cool breath comes out. It smells of your own childhood.", "* (Much more cultivation, a stir in your potential)"); }
                    if (p == 1) { drain(me, 0.04); return L("* It does not move. The cliff is cold. Your shoulder is sore, and you feel foolish.", "* (A little cultivation lost)"); }
                    return L("* When you come back the next day, there is no door. You are not sure whether that is a relief.");
                });

        add(all, 9, "copycat", "A man who copies you", Vignettes::town,
                new String[]{
                        "* For three days a stranger has walked behind you at a polite distance, copying everything: your walk, your sleeves, the way you hold a cup.",
                        "@Stranger| I am not mocking you. I am learning how to be someone. You seemed like a good person to start with."},
                "What do you do?", new String[]{"Teach him properly.", "Tell him to find his own way.", "Pretend not to notice."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.1); earn(me, md(me)); return L("* A fortnight of lessons later, he does a thing that you do not do: the left-handed version of your best move, and it is better.", "* He is so grateful his family sends a gift. (More cultivation, +{m} ss)"); }
                    if (p == 1) { calm(me, 8, 0.07); return L("@Stranger| Of course. I knew that. I only needed someone to say it.", "* He walks off, a little crooked, entirely himself. (A calmer mind)"); }
                    return L("* On the fourth day he copies your cough. You leave town that night.");
                });

        add(all, 9, "thirsty_dragon", "A dragon asks for water", Vignettes::wild,
                new String[]{
                        "* A young dragon, as big as a barn and as thin as a rail, lies on the dry bed of a spring. Its scales are dull. It lifts its head when you come close.",
                        "@Dragon| I have not drunk in a month. The spring died. Please. I cannot carry myself to the next one."},
                "What do you do?", new String[]{"Give it all your water.", "Leave it.", "Take its scales: they are worth a fortune."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.12); give(me, Item.ItemType.Metal, quality(me, 0), 200); me.givePotential(0.1, "The dragon drinks and breathes out slowly, and warms your hands."); return L("* It drinks and drinks, and the scales come back one by one, like lamps. It sheds three of them at your feet before it flies.", "* (More cultivation, +200 metal of quality " + quality(me, 0) + ", a stir in your potential)"); }
                    if (p == 1) return L("* You do not look back. You tell yourself it was not your business, and for the next day or two that is true.");
                    earn(me, bg(me)); drain(me, 0.1); return L("* The scales come away easily, and it does not even struggle. That is the worst part.", "* (+{b} ss, a lot of cultivation lost)");
                });

        add(all, 9, "wall_of_years", "The man who wants to die well", Vignettes::town,
                new String[]{
                        "* A very old man in a courtyard asks for a moment. He is not afraid, he says. He only wants advice from someone who has lived long.",
                        "@Old man| How do I die well? I do not mean easily. I mean well."},
                "What do you do?", new String[]{"Tell him what you truly think.", "Tell him to ask a doctor.", "Sit with him in silence."}, 2,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.1); calm(me, 10, 0.07); return L("* You say a few honest things, and one of them surprises you. He nods slowly. 'Yes. That is what I thought.'", "* (More cultivation, a calmer mind)"); }
                    if (p == 1) return L("@Old man| I did. He sent me to you.", "* You both smile. It is not much of a joke.");
                    boost(me, 0.14); calm(me, 14, 0.08); return L("* An hour. Two. A bird in the tree. When he finally speaks, it is only to say thank you.", "* (More cultivation, a calmer mind for fourteen weeks)");
                });
    }

    // ================================================================== 10 True Immortal
    private static void immortal(List<Vignettes.Story> all) {
        add(all, 10, "done_before", "You have done this before", ANY,
                new String[]{
                        "* You sit down in a teahouse you have never visited, and the tea is exactly what you expected. The cup is cool. The owner has his back to you.",
                        "* He says, without turning, 'The usual?' You have never been here. Or you have, in another turn of the wheel."},
                "What do you do?", new String[]{"Say yes.", "Ask what 'the usual' is.", "Leave."}, 1,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.1); calm(me, 12, 0.08); return L("* A plain bowl of rice and pickles. You eat it slowly. It is the best thing you have eaten in a hundred years.", "* (More cultivation, a calmer mind for twelve weeks)"); }
                    if (p == 1) { Area a = unknownPlace(me, true); if (a == null) a = unknownPlace(me, false); if (a != null) { me.learnPlace(a); return L("@Owner| You always have the red bean cake, and you always ask where the others went. Go to " + a.name + ". They were there last time.", "* (It is on your map: " + PlayerControl.coord(a) + ".)"); } return L("@Owner| You always have the red bean cake. And you always ask what it means. I have never known."); }
                    return L("* He turns around as you reach the door. He has your face, but very old. You are gone before he can say what he meant to.");
                });

        add(all, 10, "late_student", "The student of a student of a student", Vignettes::town,
                new String[]{
                        "* A girl, perhaps fourteen, holds out a battered book. 'My teacher's teacher's teacher was taught by someone with your name,' she says. 'Is it you?'",
                        "* The book has your handwriting in the margins, from a century nobody remembers."},
                "What do you do?", new String[]{"Admit it.", "Say you have no idea.", "Teach her one thing."}, 2,
                (me, p) -> {
                    if (p == 0) { calm(me, 12, 0.08); boost(me, 0.08); return L("* She stares at you for a long while, and then she cries a little and laughs, and says she knew.", "* (A calmer mind, more cultivation)"); }
                    if (p == 1) return L("@Girl| Oh. That is what the others said you would say.", "* She smiles as if she had won a bet.");
                    boost(me, 0.14); me.givePotential(0.1, "You give her the oldest lesson you have."); return L("* You show her how to hold her breath for one beat longer. She does it, once, and glows. You glow a little too.", "* (More cultivation, a stir in your potential)");
                });

        add(all, 10, "pruned_tree", "The tree that is trimmed by nobody", Vignettes::wild,
                new String[]{
                        "* A tall plum tree stands alone on a hill, and every one of its branches has been cut at exactly the same height.",
                        "* The cuts are clean. The grass around the trunk shows no footprints.",
                        "* The cuts look fresh. Somebody tends it every spring, and nobody has ever seen who."},
                "What do you do?", new String[]{"Wait to see who comes.", "Pick one of the plums.", "Leave it alone."}, 2,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.12); return L("* You wait through the night. At dawn the tree is whole again and the cuts are older than they were. You did not see a hand.", "* But you felt a patience that was not yours. (More cultivation)"); }
                    if (p == 1) { pill(me, 1); return L("* It tastes of a summer you do not remember, sweet, with an edge of iron. The stone in the middle is a small, hard pill.", "* (+1 pill for the next gate)"); }
                    return L("* The tree is in the corner of your eye for a long time after you have gone. Something tends it, you decide, and it does not mind being watched.");
                });

        add(all, 10, "empty_chair", "An empty chair in a tavern", Vignettes::town,
                new String[]{
                        "* In a busy tavern one chair stands at the end of a long table. The tavern is full. The chair is empty.",
                        "@Barkeep| Nobody sits there. Do not ask me why. Everybody has their reason, and I would rather not hear them."},
                "What do you do?", new String[]{"Sit in it.", "Ask for the reasons.", "Leave it be."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.18); return L("* The room goes quiet for a moment, then loud again. The chair is as comfortable as a lap. Whatever it was waiting for, it seems satisfied.", "* (Much more cultivation)"); }
                    if (p == 1) { calm(me, 10, 0.07); return L("@Barkeep| One said a ghost. One said a god. One said his father. Not one of them is wrong.", "* You drink to all three. (A calmer mind)"); }
                    return L("* The chair stays empty. The table goes on talking around it.");
                });

        add(all, 10, "empty_boat", "The boat with no passengers", Vignettes::wild,
                new String[]{
                        "* At the edge of a great lake, a small boat is tied to a post, with its oars crossed on the bench. A lantern is lit on the bow.",
                        "* On the far shore, a second lantern burns. Between them, the water is smooth as glass."},
                "What do you do?", new String[]{"Row across.", "Light the other lantern and wait.", "Leave it."}, 0,
                (me, p) -> {
                    if (p == 0) { boost(me, 0.16); calm(me, 14, 0.09); return L("* The oars hardly touch the water. Halfway over you stop, and the stars are below you as well as above, and you hang between like a seed.", "* (Much more cultivation, a calmer mind for fourteen weeks)"); }
                    if (p == 1) { me.givePotential(0.15, "A third lantern lights itself, on the water, as if to answer."); boost(me, 0.08); return L("* You wait until the three lamps burn in a line. It feels like a conversation, and it feels like you won it.", "* (More cultivation, a stir in your potential)"); }
                    return L("* The lanterns burn all night. You notice that you look back.");
                });
    }
}
