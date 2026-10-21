package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/** Little stories: small scenes that happen on the road, in a market or on a spirit vein. They are not the lore and not a talk with a portrait: a short story in the scene window ("A little story"), one or two
 *  choices, and a small consequence (a few spirit stones, some herbs, a glimpse of a faraway place, a calm week). Each is told once per person. They have their own switch, `settings vignettes`, and do not follow
 *  `settings lore` or `settings dialogue`. They come to a person who plays by hand, at the prompt, about once in 40 weeks, never within 20 weeks of the last one.
 *  In the lines, "* " is narration and "@Name| words" is somebody speaking (the same marks the Story chapters use). */
final class Vignettes {
    private Vignettes() { }

    static final int COOLDOWN = 20, ODDS = 20; // weeks between two stories; one turn in ODDS (when one fits) starts one

    /** What a choice does and what is told after it. */
    interface Resolve { List<String> go(Character me, int pick); }

    static final class Story {
        final String id, title; final Predicate<Character> when; final String[] text; final String prompt; final String[] options; final int dflt; final Resolve resolve;
        final int lo, hi; // the realms (Major Lvl) it is told in
        Story(String id, String title, Predicate<Character> when, String[] text, String prompt, String[] options, int dflt, Resolve resolve) { this(id, title, when, text, prompt, options, dflt, resolve, 0, 1); }
        Story(String id, String title, Predicate<Character> when, String[] text, String prompt, String[] options, int dflt, Resolve resolve, int lo, int hi) {
            this.id = id; this.title = title; this.when = when; this.text = text; this.prompt = prompt; this.options = options; this.dflt = dflt; this.resolve = resolve; this.lo = lo; this.hi = hi;
        }
    }

    // ------------------------------------------------------------------ helpers for the effects
    static boolean wild(Character me) { return me.location != null && !me.location.city; }
    static boolean town(Character me) { return me.location != null && me.location.city; }
    static long wealth(Character me) { return PlayerLists.wealth(me); }

    static boolean pay(Character me, long n) {
        if (!me.canAfford(n)) return false;
        me.deductSs(n);
        return true;
    }

    static void earn(Character me, long n) { me.ss += n; me.convertCurrency(); }

    static void give(Character me, Item.ItemType type, int lvl, int qty) {
        for (Item i : me.materialBag) if (i.itemType == type && i.lvl == lvl) { i.quantity += qty; return; }
        me.materialBag.add(new Item(type, lvl, qty));
    }

    static void calm(Character me, int weeks, double pow) {
        Life.Fx f = Life.fx(me);
        f.inspiredWeeks = Math.max(f.inspiredWeeks, weeks);
        f.inspiredPow = Math.max(f.inspiredPow, pow);
    }

    /** A place the person has not seen yet that is worth knowing (a dungeon, or a city), not too far away; null if none. */
    static Area unknownPlace(Character me, boolean dungeon) {
        List<Area> pool = new ArrayList<>();
        try {
            for (Area a : new ArrayList<>(Map.createdAreas)) {
                if (a == null || me.knownLocations.contains(a) || a == me.location) continue;
                if (dungeon ? a.dungeonType == null || a.dungeonType == Area.DungeonType.CELESTIAL_TRIAL : !a.city) continue;
                if (PlayerControl.distance(me.location, a) > 40) continue;
                pool.add(a);
            }
        } catch (RuntimeException e) { }
        return pool.isEmpty() ? null : pool.get(GameDriver.rand.nextInt(pool.size()));
    }

    static List<String> L(String... s) { return new ArrayList<>(Arrays.asList(s)); }

    // ------------------------------------------------------------------ the stories
    static final List<Story> ALL = new ArrayList<>();
    static {
        ALL.add(new Story("bridge", "The riddle at the bridge", Vignettes::wild,
                new String[]{
                        "* A man sits on the rail of a small stone bridge, swinging his boots over the water.",
                        "@Bridge keeper| The toll today is a riddle. Coins are boring.",
                        "@Bridge keeper| I have cities, but no houses. Mountains, but no trees. Water, but no fish. What am I?"},
                "What do you say?", new String[]{"\"A map.\"", "\"A dream.\"", "Pay two spirit stones and skip it."}, 2,
                (me, p) -> {
                    if (p == 0) { earn(me, 40); return L("@Bridge keeper| A map! Nobody gets it. Not once in three years.", "* He tosses you a small purse. \"Forty stones. Don't spend them all in one place.\" (+40 ss)"); }
                    if (p == 1) return L("@Bridge keeper| A dream. Wrong, but pretty. Go on, then.", "* He waves you across without a toll. You decide he likes you a little.");
                    if (pay(me, 2)) return L("@Bridge keeper| Cheap or clever? I can never tell.", "* You cross with a lighter purse and a heavier mind. (-2 ss)");
                    return L("@Bridge keeper| No stones, no riddle? Fine. Cross. I'm too tired to argue.");
                }));

        ALL.add(new Story("cranes", "Paper cranes", Vignettes::town,
                new String[]{
                        "* The rain starts just as you cross the market square. Under a shop awning, a girl of about nine is folding paper.",
                        "* A tray of tiny paper cranes sits beside her. None have sold.",
                        "@Girl| They bring luck. My mother said so. She said it before she left for the mountains.",
                        "@Girl| Five stones for one. Or nothing, if you don't want one. I'll still be here."},
                "What do you do?", new String[]{"Buy one crane (5 ss).", "Buy every crane (30 ss).", "Smile, and walk on."}, 2,
                (me, p) -> {
                    if (p == 0 && pay(me, 5)) { calm(me, 4, 0.05); return L("* She folds the last corner with great care and puts the crane in your hand.", "* You tuck it into your sleeve. For a few weeks you feel a little lighter. (-5 ss, a calmer mind for four weeks)"); }
                    if (p == 1 && pay(me, 30)) { calm(me, 8, 0.07); return L("@Girl| All of them? Even the crooked one?", "* She runs off through the rain, shouting a name. Somewhere a door opens. You feel lighter for weeks. (-30 ss, a calmer mind for eight weeks)"); }
                    return L("* The rain follows you out of the square.", "* You think about her for a long time. The crane is a very small thing to want.");
                }));

        ALL.add(new Story("cart", "The cart in the mud", Vignettes::wild,
                new String[]{
                        "* A merchant's cart has sunk to its axles in the road. The merchant is shouting at a very patient donkey.",
                        "@Merchant| Don't just stand there! Push, or pray, or both!"},
                "What do you do?", new String[]{"Push the cart out.", "Offer to buy his herbs as they are (25 ss).", "Walk past."}, 2,
                (me, p) -> {
                    if (p == 0) { give(me, Item.ItemType.Herb, 2, 60); return L("* It takes the whole afternoon, and by the end you are as muddy as the donkey.", "@Merchant| You saved my week. Take these, and don't argue.", "* He presses a bundle of herbs into your hands. (+60 herbs, quality 2)"); }
                    if (p == 1 && pay(me, 25)) { give(me, Item.ItemType.Herb, 1, 100); return L("@Merchant| Buy my goods in the mud? You're either a saint or a thief. Take them, they're yours.", "* A fair price for a bad day. (-25 ss, +100 herbs, quality 1)"); }
                    return L("* The donkey gives you a look you will remember for the rest of your life.");
                }));

        ALL.add(new Story("vein", "Thunder under the ground", me -> me.location != null && me.location.spiritualVein,
                new String[]{
                        "* You stop without knowing why. The air tastes like metal and rain.",
                        "* Beneath your feet something slow and enormous turns over, like a sleeper rolling in bed.",
                        "* This is a spirit vein. The qi here is thick enough to chew."},
                "What do you do?", new String[]{"Sit down and cultivate.", "Remember the place and walk on."}, 0,
                (me, p) -> {
                    if (p == 0) { me.cultivate(); me.cultivate(); me.cultivate(); return L("* You lose track of time. When you stand again your knees crack and your qi hums like a bowl struck once.", "* (Three extra weeks of cultivation.)"); }
                    return L("* You fix the shape of the hills in your mind. Places like this are rare.", "* Come back to it when you need a lot of qi in a short time.");
                }, 0, 10));

        ALL.add(new Story("stranger", "The hooded stranger", me -> wild(me) && unknownPlace(me, false) != null || wild(me) && unknownPlace(me, true) != null,
                new String[]{
                        "* A hooded stranger falls into step beside you on the road. You never hear them arrive.",
                        "@Stranger| You walk like someone who has lost something.",
                        "@Stranger| I won't ask what. But I can tell you where other people lost theirs."},
                "What do you say?", new String[]{"\"I'm listening.\"", "\"Go away.\"", "\"Who are you?\""}, 1,
                (me, p) -> {
                    if (p == 0) {
                        Area a = unknownPlace(me, true);
                        if (a == null) a = unknownPlace(me, false);
                        if (a == null) return L("@Stranger| Hm. You already know everything I know. Walk well.");
                        me.learnPlace(a);
                        return L("@Stranger| Go " + PlayerControl.coord(a) + ". There is " + (a.city ? "a city" : "an old place") + " there called " + a.name + ".",
                                "* When you look up, the stranger is gone. The place is on your map now (Me > Locations).");
                    }
                    if (p == 1) return L("@Stranger| Fair enough. The road is wide.", "* The stranger turns off at the next bend. You never see them again.");
                    return L("@Stranger| Someone who is also lost.", "* They smile. It is the saddest thing you have seen all year.");
                }, 0, 10));

        ALL.add(new Story("beggar", "The beggar's bowl", me -> town(me) && wealth(me) >= 20,
                new String[]{
                        "* A beggar sits against a wall with an empty bowl between his knees. His hands are scarred like a cultivator's.",
                        "@Beggar| Spare a few stones, young one? I used to be somebody."},
                "What do you do?", new String[]{"Give 10 spirit stones.", "Give him nothing.", "Ask what happened to him."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, 10)) { me.cultivate(); return L("@Beggar| Kind. Here is one thing for free: before you break through, check your potential. I did not. Look at me.", "* He laughs without any bitterness at all. (-10 ss, and a little wisdom: one extra week of cultivation)"); }
                    if (p == 2) return L("@Beggar| A breakthrough I tried too soon. Dropped a level, and then another. After that the sect had no use for me.", "@Beggar| Don't pity me. Just don't do what I did.", "* You sit with him a while. It costs nothing, and it is worth the time.");
                    return L("* You walk on. His bowl stays empty behind you. You remember that, later.");
                }));

        ALL.add(new Story("mushrooms", "Glowing mushrooms", me -> wild(me) && me.location.herbMaterials > 0,
                new String[]{
                        "* Under an old tree, a ring of pale mushrooms glows softly. They look edible. They also look like a bad idea."},
                "What do you do?", new String[]{"Eat one.", "Pick them to sell.", "Leave them alone."}, 2,
                (me, p) -> {
                    if (p == 0) {
                        if (GameDriver.rand.nextBoolean()) { me.cultivate(); return L("* It tastes like pepper and thunder. Your whole body hums for an hour.", "* (One extra week of cultivation.)"); }
                        return L("* Your hair stands on end, then falls out in one neat circle. It will grow back. Probably.", "* You keep very still, and a squirrel laughs at you.");
                    }
                    if (p == 1) { give(me, Item.ItemType.Herb, 2, 40); return L("* You pick them with a cloth over your hands, like a thief at a feast.", "* (+40 herbs, quality 2)"); }
                    return L("* Wisdom is boring, but alive.");
                }));

        ALL.add(new Story("soldier", "The old soldier's map", me -> wild(me) && wealth(me) >= 100 && unknownPlace(me, true) != null,
                new String[]{
                        "* An old soldier sits by a dead fire, polishing a sword he no longer swings.",
                        "@Old soldier| I drew a map once, of a place nobody comes back from. Never went in. I have too many ghosts already.",
                        "@Old soldier| A hundred stones and it's yours."},
                "What do you do?", new String[]{"Pay 100 spirit stones.", "Haggle.", "Say no."}, 2,
                (me, p) -> {
                    Area a = unknownPlace(me, true);
                    if (a == null) return L("@Old soldier| Hm. I seem to have lost it. Old age.");
                    if (p == 0 && pay(me, 100)) { me.learnPlace(a); return L("* The map is stained and half burned. Still, the place is on it: " + a.name + " at " + PlayerControl.coord(a) + ".", "* (-100 ss; the place is now in Me > Locations.)"); }
                    if (p == 1) {
                        if (GameDriver.rand.nextBoolean() && pay(me, 60)) { me.learnPlace(a); return L("@Old soldier| Sixty. And you are a thief. Take it.", "* The place is " + a.name + " at " + PlayerControl.coord(a) + ". (-60 ss)"); }
                        return L("@Old soldier| A hundred. Take it or leave it.", "* He turns back to his sword. The map goes back in his coat.");
                    }
                    return L("* You leave him with his sword and his ghosts.");
                }));

        ALL.add(new Story("lanterns", "Lanterns on the river", Vignettes::town,
                new String[]{
                        "* At dusk, people gather at the river and set small paper lanterns afloat. Each one carries a name.",
                        "* A woman beside you holds an unlit lantern and a brush. She is not writing anything.",
                        "@Woman| I don't know who to write. Too many. Do you ever feel that?"},
                "What do you do?", new String[]{"Light one for someone you lost.", "Light one for yourself.", "Watch from the bridge."}, 2,
                (me, p) -> {
                    if (p == 0) { calm(me, 8, 0.06); return L("* You write a name you have not written in a long time. The lantern wobbles, then holds.", "* The woman writes one too. Neither of you says anything. (A calmer mind for eight weeks)"); }
                    if (p == 1) { calm(me, 4, 0.05); return L("* You write your own name, and then, smaller, what you hope for.", "* It is the first time you have said it, even to paper. (A calmer mind for four weeks)"); }
                    return L("* From the bridge the river is a road of small stars, going somewhere slowly.", "* You stay until the last one is out of sight.");
                }, 0, 10));

        ALL.add(new Story("wager", "A friendly wager", me -> town(me) && wealth(me) >= 30,
                new String[]{
                        "* In a tavern yard, a big man with a bigger laugh slams his elbow on a barrel.",
                        "@Big man| Arm-wrestle! Twenty stones a go. Winner buys the next round!"},
                "What do you do?", new String[]{"Take the bet (20 ss).", "Decline politely."}, 1,
                (me, p) -> {
                    if (p == 0 && pay(me, 20)) {
                        boolean win = GameDriver.rand.nextInt(100) < 40 + Math.min(50, me.majorLvl * 6);
                        if (win) { earn(me, 40); return L("* Your arm shakes. His shakes more. The barrel creaks, and then his hand goes down with a thump.", "@Big man| Ha! Best fight I've had all week. Here's your stones.", "* (+20 ss net)"); }
                        return L("* It is over faster than it has any right to be. Your hand hits the barrel and your pride follows.", "@Big man| Better luck next round. Which you're buying.", "* (-20 ss)");
                    }
                    return L("@Big man| Another time, then!", "* You leave with your stones, your dignity, and a vague feeling of having missed something.");
                }));
    }

    static { VignetteRealms.addTo(ALL); } // Foundation and above (the stories above are for the first realms)

    // ------------------------------------------------------------------ choosing and telling
    /** The story that fits where the person stands, not yet told; null when none. */
    static Story pick(Character me) {
        PlayerControl pc = me.pc();
        if (pc == null) return null;
        if (pc.vignettesSeen == null) pc.vignettesSeen = new HashSet<>();
        List<Story> fit = new ArrayList<>();
        for (Story s : ALL) {
            if (pc.vignettesSeen.contains(s.id) || me.majorLvl < s.lo || me.majorLvl > s.hi) continue;
            boolean ok;
            try { ok = s.when.test(me); } catch (RuntimeException e) { ok = false; }
            if (ok) fit.add(s);
        }
        return fit.isEmpty() ? null : fit.get(GameDriver.rand.nextInt(fit.size()));
    }

    /** Called once for each turn of a person who plays: sometimes a little story begins. */
    static void tick(Character me) {
        if (!Settings.vignettes || me == null || me.player != 1 || me.dead || me.age < 8 || me.pc() == null || me.pc().mode != PlayerControl.Mode.MANUAL || me.skipping > 0) return; // (a story is a choice of yours: Auto and Hybrid skip it, nothing is queued)
        PlayerControl pc = me.pc();
        if (pc == null || GameDriver.time < 12 || GameDriver.time < pc.nextVignetteWeek) return;
        if ((DecisionBroker.scripted != null && !testMode) || GuiLore.busy()) return; // (a test that scripts the answers is not interrupted by a story, unless the test is about them)
        if (GameDriver.rand.nextInt(ODDS) != 0) return;
        Story s = pick(me);
        if (s != null) tell(me, s);
    }

    /** The amounts in a story follow the realm: {s} a small sum, {m} a middling one, {b} a big one (1%, 4% and 12% of what a person of the realm usually owns), {realm} the realm's name. */
    static String fmt(Character me, String s) {
        if (s == null || s.indexOf('{') < 0) return s;
        return s.replace("{s}", String.format("%,d", VignetteRealms.sm(me))).replace("{m}", String.format("%,d", VignetteRealms.md(me))).replace("{b}", String.format("%,d", VignetteRealms.bg(me)))
                .replace("{realm}", TermFormat.realm(me.majorLvl));
    }

    static List<String> fmt(Character me, List<String> in) {
        List<String> out = new ArrayList<>();
        for (String s : in) out.add(fmt(me, s));
        return out;
    }

    /** Tells a story and resolves the choice: a scene window in the GUI (the game waits for the answer), a framed block and a question in the terminal. */
    static void tell(Character me, Story s) {
        PlayerControl pc = me.pc();
        if (pc.vignettesSeen == null) pc.vignettesSeen = new HashSet<>();
        pc.vignettesSeen.add(s.id);
        pc.nextVignetteWeek = GameDriver.time + COOLDOWN;
        List<String> lines = fmt(me, Arrays.asList(s.text));
        final String prompt = fmt(me, s.prompt);
        final String[] options = new String[s.options.length];
        for (int i = 0; i < options.length; i++) options[i] = fmt(me, s.options[i]);
        boolean gui = GameDriver.guiEnabled && GameDriver.mapViewer != null && DecisionBroker.scripted == null;
        boolean asked = gui && DecisionBroker.promptable(me);
        int pick;
        if (me.player > 0 && me.skipping <= 0) System.out.println("[A little story] " + s.title);
        if (asked) pick = GuiScenes.ask(me, "A little story", s.title, lines, prompt, options, s.dflt);
        else {
            if (gui) GuiLore.vignette(s.title, lines);
            else if (DecisionBroker.scripted == null) System.out.println(ChapText.frame(s.title, lines));
            pick = DecisionBroker.ask(me, prompt, options, s.dflt);
        }
        if (pick < 0 || pick >= options.length) pick = s.dflt;
        List<String> after = fmt(me, s.resolve.go(me, pick));
        if (asked) GuiScenes.supplyAfter(after);
        else if (gui) GuiLore.vignette(s.title, after);
        else if (DecisionBroker.scripted == null) System.out.println(ChapText.frame(s.title, after));
        if (DecisionBroker.scripted != null) transcript.addAll(lines);
        if (DecisionBroker.scripted != null) transcript.addAll(after);
    }

    static boolean testMode = false;

    /** What was told (kept only while tests script the answers). */
    static final List<String> transcript = new ArrayList<>();

    /** The `stories` command: which little stories the person has lived. */
    static String list(Character me) {
        PlayerControl pc = me.pc();
        StringBuilder sb = new StringBuilder("=== Little stories you have lived ===\n");
        int n = 0;
        if (pc != null && pc.vignettesSeen != null) for (Story s : ALL) if (pc.vignettesSeen.contains(s.id)) { sb.append("  ").append(s.title).append('\n'); n++; }
        if (n == 0) sb.append("  None yet. They find you on the road, in the markets and on spirit veins.\n");
        int waiting = 0;
        for (Story s : ALL) if (me.majorLvl >= s.lo && me.majorLvl <= s.hi && !(pc != null && pc.vignettesSeen != null && pc.vignettesSeen.contains(s.id))) waiting++;
        sb.append("(`settings vignettes off` stops them. ").append(waiting).append(" more are waiting in your realm, ").append(TermFormat.realm(me.majorLvl)).append(".)");
        return sb.toString();
    }
}
