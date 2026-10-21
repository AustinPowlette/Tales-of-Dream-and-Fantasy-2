package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Game.DialogueText.Voice;

/** What people say to a human character, in three depths:
 *  1. FLAVOR: a line when someone speaks to you, offers friendship, asks to join your squad or challenges you (log and terminal; `Settings.dialogueFlavor`).
 *  2. SCENES: when someone stops to talk, a small scene with replies to choose from and consequences (a rumor, a request, an offer, a spar, a meal, a threat...), in a window with the speaker's
 *     portrait (`Settings.dialoguePopups`, GUI only). An AUTO character has them only with `Settings.dialogueAuto`: it answers by its own temper and the talk goes to the log.
 *  3. CONVERSATION: the Talk button (`talk <n>`) opens a conversation to steer: how they are, the news, themselves, a third person, a lesson, trade, a spar, a gift, a walk together.
 *  The words are data (DialogueText); a speaker's voice follows their trade, age, alignment and how they stand with you. Nothing here runs for beasts. */
final class Dialogue {
    private Dialogue() { }

    // temper of a reply (an AUTO character picks the reply that suits its own)
    static final int KIND = 0, BOLD = 1, GREEDY = 2, COLD = 3, CURIOUS = 4, CAREFUL = 5;

    /** Where the talk happens: a window (GuiDialogue), or an AUTO character's log. The last reply offered is always the way out. */
    interface Io {
        void open(Character npc, Character me);
        void npc(String text);
        void narrate(String text);
        int pick(String[] labels, int[] temper);
        void close();
    }

    static final Random RND = GameDriver.rand;
    /** The week each human last had a scene: a crowded tile does not stack one window on another in a single turn (the rest only speak). */
    private static final java.util.HashMap<Integer, Integer> LAST_SCENE = new java.util.HashMap<>();

    // ------------------------------------------------------------------ who speaks, and how
    static Voice voiceOf(Character npc, Character me) {
        Relationship r = npc.relationshipList.contains(me.id);
        int rel = r == null ? 0 : r.value;
        if (rel <= -2) return Voice.HOSTILE;
        if (npc.affiliationNum == -1 && rel <= 0) return Voice.SLY;
        if (npc.maxAge > 0 && npc.age > 60 && npc.age >= 0.7 * npc.maxAge) return Voice.WEARY;
        if (npc.sect != null && npc.sect.isMaster(npc)) return Voice.FORMAL;
        switch (npc.trait) {
            case Merchant: case Poisoner: return Voice.SLY;
            case Friendly: case Lucky: case Musician: case Drunken: case Runner: return Voice.CHEERFUL;
            case Chef: return Voice.WARM;
            case Scribe: case Alchemist: case Innovative: return Voice.SCHOLAR;
            case Leader: return Voice.FORMAL;
            case Blacksmith: case Miner: case Gatherer: case Tamer: return Voice.BLUNT;
            default: break;
        }
        return npc.affiliationNum > 0 ? Voice.WARM : npc.affiliationNum < 0 ? Voice.SLY : Voice.BLUNT;
    }

    /** How the speaker names the listener. */
    static String address(Character npc, Character me) {
        Relationship r = npc.relationshipList.contains(me.id);
        int rel = r == null ? 0 : r.value;
        if (rel >= 3) return me.name;
        if (npc.majorLvl >= me.majorLvl + 2) return rel >= 1 ? me.name : "young one";
        if (npc.majorLvl + 2 <= me.majorLvl) return "Senior";
        if (rel >= 1) return "friend";
        if (rel <= -1) return "you";
        return r == null ? "stranger" : me.name;
    }

    static String season() { return new String[]{"spring", "summer", "autumn", "winter"}[(GameDriver.time % 52) / 13]; }

    static String cap(String s) { return s == null || s.isEmpty() ? s : java.lang.Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    /** One side of a talk: the two people, the voice, and the way the words go out. */
    static final class Ctx {
        final Character npc, me; final Voice voice; final Io io; final Relationship rel;
        final String you;
        String thing = "", place = "", rumor = "", third = "", price = "", item = "", sect = "";
        Ctx(Character npc, Character me, Io io) {
            this.npc = npc; this.me = me; this.io = io;
            this.voice = voiceOf(npc, me);
            this.rel = npc.relationshipList.contains(me.id);
            this.you = address(npc, me);
            this.place = npc.location == null ? "" : npc.location.name;
            this.sect = npc.sect == null ? "" : npc.sect.name;
        }
        int relValue() { Relationship r = npc.relationshipList.contains(me.id); return r == null ? 0 : r.value; }
        String fill(String s) {
            return s.replace("{You}", cap(you)).replace("{you}", you).replace("{Name}", me.name).replace("{me}", npc.name).replace("{place}", place).replace("{season}", season())
                    .replace("{sect}", sect).replace("{thing}", thing).replace("{Rumor}", cap(rumor)).replace("{rumor}", rumor).replace("{third}", third).replace("{price}", price).replace("{item}", item);
        }
        /** A random line of this kind in the speaker's voice. */
        String line(String kind) {
            String[] l = DialogueText.lines(kind, voice);
            return l.length == 0 ? "" : fill(l[RND.nextInt(l.length)]);
        }
        /** The voice's way into a scene, sometimes. */
        String open() { return RND.nextInt(3) == 0 ? "" : line("open") + " "; }
        void say(String text) { io.npc(fill(text)); }
        /** One of several ways to say the same thing (so the same refusal is not word for word every time). */
        String any(String... v) { return v[RND.nextInt(v.length)]; }
        void say(String voiceOpener, String text) { io.npc(fill(voiceOpener + text)); }
        void note(String text) { io.narrate(fill(text)); }
        int pick(String[] labels, int[] temper) { return io.pick(labels, temper); }
        /** The relationship moves (both ways at once). */
        void rel(int d) { if (d != 0) npc.changeRelationship(me, d); }
        boolean pay(long amount) {
            if (amount <= 0) return true;
            if (!me.canAfford(amount)) return false;
            me.deductSs(amount); npc.ss += amount; npc.convertCurrency();
            return true;
        }
        long wealth() { return PlayerLists.wealth(me); }
        /** The odds (percent) that this person can be talked into something: how they feel about you, how you compare, who they are. */
        int charm() {
            int v = 40 + 12 * relValue() + (me.lvl - npc.lvl) / 2 + (npc.trait == Character.Traits.Friendly ? 10 : 0) - (npc.affiliationNum == -1 ? 8 : 0) - (voice == Voice.HOSTILE ? 40 : 0);
            return Math.max(5, Math.min(95, v));
        }
    }

    // ------------------------------------------------------------------ flavor (depth 1)
    /** The words for a kind of approach, or null when the speaker does not talk. */
    static String line(String kind, Character npc, Character me) {
        if (npc == null || me == null || npc.trait == Character.Traits.Beast || npc.trait == Character.Traits.Monster) return null;
        String l = new Ctx(npc, me, null).line(kind);
        return l.isEmpty() ? null : l;
    }

    /** Prints what a speaker says, when the listener is a human and flavor is on. */
    static void flavor(Character npc, Character me, String text) {
        if (text == null || !Settings.dialogueFlavor || me == null || me.player < 1) return;
        System.out.println(npc.name + " " + npc.lastName + ": \"" + text + "\"");
    }

    static void flavor(String kind, Character npc, Character me) { flavor(npc, me, line(kind, npc, me)); }

    // ------------------------------------------------------------------ when someone speaks to a human (depth 2)
    /** `npc` stopped to speak to `me` (an NPC's turn, aimed at a human). Returns true when the words were given (so the plain "X talked to Y" line is not needed). */
    static boolean approach(Character npc, Character me) {
        if (npc == null || me == null || npc.player != 0 || me.player != 1 || npc.dead || me.dead || !npc.born || !me.born) return false;
        if (npc.age < 8 || npc.trait == Character.Traits.Beast || npc.trait == Character.Traits.Monster) return false;
        boolean manual = me.isManualPc();
        boolean busy = me.skipping > 0 || LAST_SCENE.getOrDefault(me.id, -1) == GameDriver.time; // on autopilot, or already talking this turn: the words, not another window
        if (manual && !busy && Lessons.due(npc, me)) { // the talk turns to something the person needs to know (a lesson): in the portrait window, or told in the log
            Io lio = GameDriver.guiEnabled && GameDriver.mapViewer != null && Settings.dialoguePopups ? new GuiDialogue(me) : new LogIo();
            LAST_SCENE.put(me.id, GameDriver.time);
            Ctx lc = new Ctx(npc, me, lio);
            lio.open(npc, me);
            try { Lessons.give(lc); } finally { lio.close(); }
            remember(npc, me);
            return true;
        }
        Io io = null;
        if (manual && !busy && GameDriver.guiEnabled && GameDriver.mapViewer != null && Settings.dialoguePopups) io = new GuiDialogue(me);
        else if (!manual && Settings.dialogueAuto) io = new AutoIo();
        if (io == null) {
            Ctx c = new Ctx(npc, me, null);
            Relationship r = c.rel;
            String kind = firstMeeting(r) ? "greet_first" : "greet";
            String l = c.line(kind);
            if (l.isEmpty()) l = c.line("greet");
            flavor(npc, me, l);
            remember(npc, me);
            return Settings.dialogueFlavor && !l.isEmpty();
        }
        LAST_SCENE.put(me.id, GameDriver.time);
        scene(new Ctx(npc, me, io));
        remember(npc, me);
        return true;
    }

    /** The memory of a talk: how many, and when. */
    static void remember(Character npc, Character me) {
        Relationship r = npc.relationshipList.contains(me.id);
        if (r == null) { npc.changeRelationship(me, 0); r = npc.relationshipList.contains(me.id); }
        if (r == null) return;
        r.talks++;
        if (GameDriver.time - r.lastTalk > 4) r.topics = 0; // a new visit
        r.lastTalk = GameDriver.time;
        Relationship r2 = me.relationshipList.contains(npc.id);
        if (r2 != null && r2 != r) { r2.talks = r.talks; r2.lastTalk = r.lastTalk; r2.topics = r.topics; r2.lastAdvice = r.lastAdvice; }
    }

    static final String[] SCENES = {"greeting", "rumor", "help", "pitch", "spar", "meal", "song", "recruit", "shakedown", "gossip", "advice", "fortune", "poison", "story", "apology", "hostile", "favor"};

    /** Runs the first scene that fits, drawn by weight. */
    static void scene(Ctx c) {
        String id = choose(c);
        c.io.open(c.npc, c.me);
        try { run(id, c); } finally { c.io.close(); }
    }

    static double weight(String id, Ctx c) {
        int rel = c.relValue();
        boolean hostile = c.voice == Voice.HOSTILE;
        boolean evil = c.npc.affiliationNum == -1;
        switch (id) {
            case "greeting": return hostile ? 0 : 4;
            case "hostile": return hostile ? 8 : 0;
            case "rumor": return hostile ? 0 : 3;
            case "help": return !hostile && !evil && c.npc.ss < 200 && c.wealth() >= 60 && c.npc.trait != Character.Traits.Merchant ? 1.5 : 0;
            case "pitch": return !hostile && pitchItem(c.npc, c.me) != null ? 2.5 : 0;
            case "spar": return rel >= 0 && !hostile && c.voice != Voice.SLY && c.voice != Voice.WEARY && c.npc.age >= 14 && Math.abs(c.npc.lvl - c.me.lvl) <= 15 ? 1.2 : 0;
            case "meal": return !hostile && (c.npc.trait == Character.Traits.Chef || c.npc.trait == Character.Traits.Drunken) ? 4 : 0;
            case "song": return !hostile && c.npc.trait == Character.Traits.Musician ? 4 : 0;
            case "recruit": return !hostile && c.npc.sect != null && c.me.sect == null && (c.npc.trait == Character.Traits.Leader || rel >= 1) ? 2.5 : 0;
            case "shakedown": return evil && rel <= 0 && c.npc.trait != Character.Traits.Merchant && c.npc.tempLvl + 3 >= c.me.tempLvl && c.wealth() >= 100 ? 1.5 : 0;
            case "gossip": return !hostile && thirdPerson(c.npc, c.me) != null ? 2 : 0;
            case "advice": return !hostile && rel >= 1 && c.npc.lvl >= c.me.lvl + 5 && GameDriver.time - adviceWeek(c) >= 26 ? 3 : 0;
            case "fortune": return !hostile && c.npc.trait == Character.Traits.Lucky ? 4 : 0;
            case "poison": return !hostile && c.npc.trait == Character.Traits.Poisoner && Life.firstOf(c.npc, Item.ItemType.Poison) != null ? 4 : 0;
            case "story": return !hostile && rel >= 1 && rel <= 3 ? 2 : 0;
            case "apology": return rel == -1 && !hostile && !evil ? 3 : 0;
            case "favor": return hostile ? 0 : Favors.weight(c);
            default: return 0;
        }
    }

    static String choose(Ctx c) {
        double total = 0; double[] w = new double[SCENES.length];
        for (int i = 0; i < SCENES.length; i++) { w[i] = weight(SCENES[i], c); total += w[i]; }
        if (total <= 0) return "greeting";
        double r = RND.nextDouble() * total;
        for (int i = 0; i < SCENES.length; i++) { r -= w[i]; if (r <= 0 && w[i] > 0) return SCENES[i]; }
        return "greeting";
    }

    static void run(String id, Ctx c) {
        switch (id) {
            case "rumor": if (!rumorScene(c, false)) greetingScene(c); break;
            case "help": helpScene(c); break;
            case "pitch": if (!pitchScene(c, false)) greetingScene(c); break;
            case "spar": sparScene(c); break;
            case "meal": mealScene(c); break;
            case "song": songScene(c); break;
            case "recruit": if (c.npc.sect == null) greetingScene(c); else recruitScene(c); break;
            case "shakedown": shakedownScene(c); break;
            case "gossip": if (!gossipScene(c, false)) greetingScene(c); break;
            case "advice": if (!adviceScene(c, false)) greetingScene(c); break;
            case "fortune": fortuneScene(c); break;
            case "poison": poisonScene(c); break;
            case "story": storyScene(c); break;
            case "apology": apologyScene(c); break;
            case "hostile": hostileScene(c); break;
            case "favor": Favors.scene(c); break;
            default: greetingScene(c); break;
        }
    }

    static int adviceWeek(Ctx c) { return c.rel == null ? -1000 : c.rel.lastAdvice; }

    /** Something a person notices about the other or the place, so that two people who meet do not only trade greetings. "" when nothing comes to mind. */
    static String remark(Ctx c) {
        ArrayList<String> r = new ArrayList<>();
        Character me = c.me, n = c.npc;
        Area here = n.location;
        long wealth = c.wealth();
        if (wealth >= 5_000_000L) r.add("That's a heavy purse you carry, {you}. Mind the roads.");
        if (me.kills >= 50) r.add(n.affiliationNum < 0 ? "Your name travels ahead of you, {you}. Impressive." : "People talk about you, {you}. Some of it is even kind.");
        if (here != null && here.spiritualVein) r.add("Feel that? The qi is thick here. An hour on this ground is worth a week elsewhere.");
        if (here != null && here.city) r.add("The market's loud today. I came out for the quiet, which is a joke.");
        if (here != null && here.monsters > 100) r.add("The beasts are thick in this tile. Watch yourself out here.");
        switch ((GameDriver.time % 52) / 13) {
            case 0: r.add("The blossoms are early this year."); break;
            case 1: r.add("Hot enough to boil the ink."); break;
            case 2: r.add("Harvest is in the air. Everyone's in a better mood."); break;
            default: r.add("Cold enough to freeze the ink."); break;
        }
        if (n.majorLvl >= me.majorLvl + 2) r.add("You've got promise, {you}. Don't waste it.");
        if (n.majorLvl + 2 <= me.majorLvl) r.add("I hear you're a long way ahead of me now. Humbling, honestly.");
        if (me.spouse != null && !me.spouse.dead) r.add("How is " + me.spouse.name + "? Give them my regards.");
        if (me.beastIds != null && !me.beastIds.isEmpty()) r.add("That's a fine companion you keep.");
        if (me.sect != null && me.sect == n.sect) r.add("Good to see a fellow of " + me.sect.name + " out here.");
        if (me.age < 20) r.add("So young to be out on these roads.");
        if (me.age > 80) r.add("Still walking at your age. I respect that.");
        if (r.isEmpty()) return "";
        return c.fill(r.get(RND.nextInt(r.size())));
    }

    /** A stranger: never talked to and not yet a friend (someone who is a friend does not introduce themselves again). */
    private static boolean firstMeeting(Relationship r) { return r == null || (r.talks == 0 && r.value < 1); }

    // ------------------------------------------------------------------ the scenes
    private static void greetingScene(Ctx c) {
        boolean first = firstMeeting(c.rel);
        c.say(c.line(first ? "greet_first" : "greet"));
        if (!first && c.voice != Voice.HOSTILE && RND.nextInt(5) < 2) { String rm = remark(c); if (!rm.isEmpty()) c.say(rm); }
        String[] o = {"Greet them warmly.", "Ask what is new.", "Nod and move on."};
        int[] t = {KIND, CURIOUS, CAREFUL};
        int p = c.pick(o, t);
        if (p == 0) { c.say(c.line("r_warm")); c.rel(1); }
        else if (p == 1) { if (!rumorScene(c, true)) c.say("Nothing worth repeating, I'm afraid. Quiet times."); }
        else c.say(c.line("r_nod"));
    }

    private static void hostileScene(Ctx c) {
        c.say(c.line("greet"));
        String[] o = {"Offer to settle things between you.", "Hold your ground.", "Say nothing and walk away."};
        int[] t = {KIND, BOLD, CAREFUL};
        int p = c.pick(o, t);
        if (p == 0) {
            if (RND.nextInt(100) < 25 + 5 * Math.max(0, c.me.lvl - c.npc.lvl) / 5) { c.say("...Perhaps. I am tired of this too. We will see."); c.rel(1); }
            else c.say("Settle? You think words undo what's done? Get out of my sight.");
        } else if (p == 1) {
            c.say("Careful. I am in no mood to be tested.");
            if (RND.nextInt(5) == 0) { c.note("It comes to blows."); c.npc.battleCheck(c.me); }
        } else c.say(c.line("r_cold"));
    }

    // ---- rumors
    static final class Rumor { String text; Area area; Rumor(String t, Area a) { text = t; area = a; } }

    static Rumor pickRumor(Character npc, Character me) {
        ArrayList<Rumor> pool = new ArrayList<>();
        Character.Traits tr = npc.trait;
        int nearMax = 25;
        List<Area> known = npc.knownLocations == null ? new ArrayList<Area>() : new ArrayList<>(npc.knownLocations);
        for (Area a : known) {
            if (a == null || a == me.location) continue;
            int d = PlayerControl.distance(me.location, a);
            if (d > nearMax || d < 2) continue;
            String where = a.name + " at " + PlayerControl.coord(a);
            if (a.dungeonType != null) {
                switch (a.dungeonType) {
                    case LOST_RUINS: pool.add(new Rumor("an old ruin near " + where + " still holds treasure nobody has carried off", a)); break;
                    case BEAST_LAIR: pool.add(new Rumor("there is a beast lair near " + where + ", plenty of monsters and plenty of loot for anyone who can take them", a)); break;
                    case MARTIAL_TRIAL: pool.add(new Rumor("a martial trial hall stands near " + where + " and tests anyone bold enough to walk in", a)); break;
                    case HAUNTED_GROUNDS: pool.add(new Rumor("the dead keep cultivating on the haunted grounds near " + where + ", and some say the living can learn from them", a)); break;
                    default: break;
                }
            }
            if (a.spiritualVein) pool.add(new Rumor("the qi under " + where + " runs thick, a spirit vein, they say; an hour there is worth a week elsewhere", a));
            if (a.hasHeaven()) pool.add(new Rumor("something fell from the sky near " + where + ", a gift of the Heavens that nobody has claimed yet", a));
            if (a.city && a.tournamentTimer > 0) pool.add(new Rumor(a.name + " is holding a tournament soon (" + PlayerControl.coord(a) + "); the winner takes a real prize", a));
            if (Auctions.hasHouse(a) && Auctions.weeksToNext(a) > 0 && Auctions.weeksToNext(a) <= 10) pool.add(new Rumor(a.name + " holds an auction in " + Auctions.weeksToNext(a) + " weeks (" + PlayerControl.coord(a) + "): luxuries and old treasures", a));
            if ((tr == Character.Traits.Gatherer || tr == Character.Traits.Miner || tr == Character.Traits.Alchemist || tr == Character.Traits.Blacksmith)
                    && (tr == Character.Traits.Gatherer || tr == Character.Traits.Alchemist ? a.herbMaterials : a.metalMaterials) > 400_000)
                pool.add(new Rumor("the ground near " + where + " is rich in " + (tr == Character.Traits.Gatherer || tr == Character.Traits.Alchemist ? "herbs" : "ore") + ", more than you'd carry out in a season", a));
            if (tr == Character.Traits.Tamer && a.monsters > 50) pool.add(new Rumor("the beasts near " + where + " are thick on the ground this year", a));
            if ((tr == Character.Traits.Merchant || tr == Character.Traits.Runner) && a.city && a.cityLevel() >= 3) pool.add(new Rumor(a.name + " (" + PlayerControl.coord(a) + ") is doing well, a big market with money in it", a));
        }
        // the world's own news
        synchronized (Chronicle.log) {
            for (int i = Chronicle.log.size() - 1, n = 0; i >= 0 && n < 40; i--, n++) {
                Chronicle.Entry e = Chronicle.log.get(i);
                if (GameDriver.time - e.week > 80) break;
                if (e.kind.equals("world") || e.kind.equals("war") || e.kind.equals("city") || e.kind.equals("sect") || e.kind.equals("legend")) { pool.add(new Rumor("the word going round is: " + e.text, null)); if (pool.size() > 60) break; }
            }
        }
        if (pool.isEmpty()) return null;
        // a trade favors its own kind of news
        ArrayList<Rumor> preferred = new ArrayList<>();
        for (Rumor r : pool) {
            String t = r.text;
            boolean pref = ((tr == Character.Traits.Gatherer || tr == Character.Traits.Miner) && t.contains("rich in")) || (tr == Character.Traits.Tamer && (t.contains("beast") || t.contains("monsters")))
                    || ((tr == Character.Traits.Merchant || tr == Character.Traits.Runner) && (t.contains("market") || t.contains("auction") || t.contains("tournament"))) || (tr == Character.Traits.Scribe && t.contains("word going round"));
            if (pref) preferred.add(r);
        }
        ArrayList<Rumor> from = preferred.isEmpty() || RND.nextInt(3) == 0 ? pool : preferred;
        return from.get(RND.nextInt(from.size()));
    }

    /** True when something was told. `inline`: the scene is part of another (the opening line is shorter). */
    private static boolean rumorScene(Ctx c, boolean inline) {
        Rumor r = pickRumor(c.npc, c.me);
        if (r == null) return false;
        c.rumor = r.text;
        String[] intro = {"Heard something worth passing on: {rumor}.", "Between us: {rumor}.", "You didn't hear this from me, but {rumor}.", "Thought you'd want to hear this. {Rumor}."};
        c.say(c.open(), intro[RND.nextInt(intro.length)]);
        String[] o = {"Thank them and ask exactly where.", "Ask what they want for the tip.", "Say you are not interested."};
        int[] t = {CURIOUS, GREEDY, CAREFUL};
        int p = c.pick(o, t);
        if (p == 0) {
            c.say(c.voice == Voice.SLY ? "Glad to help. I'll remember you were glad." : c.any("Glad to share. Mind how you go.", "Happy to. Keep your eyes open out there.", "Don't mention it. Well, do mention it if it pays off."));
            if (r.area != null) { c.me.learnPlace(r.area); c.note("You mark " + r.area.name + " " + PlayerControl.coord(r.area) + " on your map."); }
            c.rel(1);
        } else if (p == 1) {
            if (c.voice == Voice.SLY || c.npc.trait == Character.Traits.Merchant) { c.say("A favor, someday. I won't say what. I'll know when."); if (r.area != null) c.me.learnPlace(r.area); }
            else { c.say("Nothing. A good tip is its own reward."); if (r.area != null) c.me.learnPlace(r.area); c.rel(1); }
            if (r.area != null) c.note("You mark " + r.area.name + " " + PlayerControl.coord(r.area) + " on your map.");
        } else c.say(c.any("Suit yourself.", "Your loss.", "Fair enough. Plenty of others will want to know.", "Ah well. It was a good one."));
        return true;
    }

    // ---- a request
    private static void helpScene(Ctx c) {
        long big = Math.max(20, Math.min(100, c.wealth() / 10)), small = Math.max(5, big / 3);
        c.say(c.open(), "I hate to ask. I'm short this week and the food stalls don't take promises. Could you spare a few spirit stones?");
        String[] o = {"Give " + big + " ss.", "Give " + small + " ss.", "Say you can't spare any."};
        int[] t = {KIND, CAREFUL, COLD};
        int p = c.pick(o, t);
        if (p == 0 && c.pay(big)) { c.say(c.any("You won't regret this. I mean it.", "I'll pay you back, one way or another.", "That's more than I'd hoped. Thank you.")); c.note("You give " + big + " ss."); c.rel(2); }
        else if (p == 1 && c.pay(small)) { c.say("Every bit counts. Thank you."); c.note("You give " + small + " ss."); c.rel(1); }
        else if (p <= 1) c.say("Ah. Never mind, then.");
        else c.say(c.any("I understand. Forget I asked.", "No, no, of course. It's not your problem.", "Of course. Thank you for hearing me out."));
    }

    // ---- an offer
    static Item pitchItem(Character npc, Character me) {
        Item best = null;
        for (Item it : npc.pillBag) if (it != null && it.itemType == Item.ItemType.Pill && it.quantity > 0 && it.pillType == Item.PillType.Cultivation && it.lvl <= 10 && it.lvl >= me.majorLvl && it.lvl <= me.majorLvl + 2) { best = it; break; }
        if (best != null) return best;
        for (Item it : npc.backpack) {
            if (it == null || it == npc.equippedWeapon || it == npc.equippedClothing || it.itemType == null) continue;
            if ((it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing) && it.lvl > 0 && it.lvl >= me.lvl - 5 && it.lvl <= me.lvl + 20) {
                Item mine = it.itemType == Item.ItemType.Weapon ? me.equippedWeapon : me.equippedClothing;
                if (mine == null || it.lvl > mine.lvl) { best = it; break; }
            }
        }
        return best;
    }

    private static long priceOf(Item it) {
        Item one = it.itemType == Item.ItemType.Pill ? new Item(Item.ItemType.Pill, it.pillType, it.lvl, 1) : it;
        return Math.max(1, Math.round(Character.itemPrice(one) * 1.25));
    }

    private static void sellTo(Ctx c, Item it, long price) {
        if (!c.pay(price)) { c.say("Come back when you can pay."); return; }
        if (it.itemType == Item.ItemType.Pill) {
            it.quantity--;
            if (it.quantity <= 0) c.npc.pillBag.remove(it);
            Character.addToBag(c.me.pillBag, new Item(Item.ItemType.Pill, it.pillType, it.lvl, 1));
        } else {
            c.npc.backpack.remove(it);
            c.me.backpack.add(it);
        }
        c.note("You pay " + price + " ss and take the " + it.name + (it.itemType == Item.ItemType.Pill ? " (see 'bag')" : "") + ".");
    }

    private static boolean pitchScene(Ctx c, boolean asked) {
        Item it = pitchItem(c.npc, c.me);
        if (it == null) { if (asked) c.say("Nothing I'd part with just now. Ask again some other day."); return false; }
        long price = priceOf(it);
        c.item = it.name + (it.itemType == Item.ItemType.Pill ? " (Lv." + it.lvl + ")" : " (Lv." + it.lvl + ")"); c.price = String.valueOf(price);
        c.say(c.open(), asked ? "As it happens, yes: {item}, {price} ss. Honest price." : "I'm clearing stock. {item}, {price} ss. You won't find it cheaper in {place}.");
        boolean can = c.me.canAfford(price);
        ArrayList<String> o = new ArrayList<>(); ArrayList<Integer> t = new ArrayList<>();
        if (can) { o.add("Buy it."); t.add(CURIOUS); }
        o.add("Haggle."); t.add(BOLD);
        o.add("No, thank you."); t.add(CAREFUL);
        int p = pickList(c, o, t);
        String chosen = o.get(p);
        if (chosen.equals("Buy it.")) { c.say("A pleasure."); sellTo(c, it, price); c.rel(0); }
        else if (chosen.equals("Haggle.")) {
            int odds = 45 + (c.relValue() >= 2 ? 10 : 0) - (c.voice == Voice.SLY ? 15 : 0);
            if (RND.nextInt(100) < odds) {
                long cheaper = Math.max(1, Math.round(price * 0.8));
                c.say("All right, all right. " + cheaper + ". But you're robbing me.");
                if (c.me.canAfford(cheaper)) { String[] o2 = {"Take the deal.", "Walk away."}; int[] t2 = {GREEDY, CAREFUL}; if (c.pick(o2, t2) == 0) { c.say("Done."); sellTo(c, it, cheaper); } else c.say("Your loss."); }
                else c.say("Sadly you can't even afford that. Come back with more stones.");
            } else {
                c.say("Not a chance. Full price or nothing.");
                if (can) { String[] o2 = {"Pay " + price + " ss.", "Walk away."}; int[] t2 = {GREEDY, CAREFUL}; if (c.pick(o2, t2) == 0) { c.say("Pleasure."); sellTo(c, it, price); } else c.say("Suit yourself."); }
            }
        } else c.say(c.any("Another time, then.", "Suit yourself. It won't last.", "Think it over. I'm not going anywhere."));
        return true;
    }

    private static int pickList(Ctx c, ArrayList<String> o, ArrayList<Integer> t) {
        int[] tt = new int[t.size()];
        for (int i = 0; i < tt.length; i++) tt[i] = t.get(i);
        int p = c.pick(o.toArray(new String[0]), tt);
        return Math.max(0, Math.min(o.size() - 1, p));
    }

    // ---- a spar
    private static void sparScene(Ctx c) {
        String[] intro = {"You carry yourself like someone who fights. A friendly bout? No grudges, no killing.", "I could use the practice. Spar with me? Easy hands.", "Bored of hitting trees. Care to trade a few blows?"};
        c.say(c.open(), intro[RND.nextInt(intro.length)]);
        String[] o = {"Accept.", "Another time."};
        int[] t = {BOLD, CAREFUL};
        if (c.pick(o, t) == 0) {
            c.say(c.voice == Voice.FORMAL ? "In good form, then." : "Good. Show me.");
            c.note("You trade blows.");
            c.me.sparring = true; c.npc.sparring = true;
            try { c.me.battleCheck(c.npc); } finally { c.me.sparring = false; c.npc.sparring = false; }
            if (!c.me.dead && !c.npc.dead) c.say(c.line("r_spar"));
        } else c.say(c.any("Fair enough. Another time.", "Your loss. I'm good.", "Next time, then."));
    }

    // ---- food and drink
    private static void mealScene(Ctx c) {
        boolean chef = c.npc.trait == Character.Traits.Chef;
        int lvl = Math.max(1, Math.min(10, c.npc.professionLvl / 10 + 2));
        String name = chef ? Naming.dish(lvl, c.npc) : Naming.wine(lvl, c.npc);
        c.thing = name;
        c.say(c.open(), chef ? "I cooked too much again. {thing}, still warm. Have some?" : "Care for a cup? {thing}. Best thing you'll drink this season.");
        String[] o = {"Accept gladly.", "Buy a portion to take along (20 ss).", "Decline politely."};
        int[] t = {KIND, GREEDY, CAREFUL};
        int p = c.pick(o, t);
        if (p == 0) {
            Item food = new Item(chef ? Item.ItemType.Food : Item.ItemType.Wine, lvl, 1, name);
            Life.use(c.me, food);
            c.say(chef ? "Eat, eat. You're too thin." : "To the road, then.");
            c.note(chef ? "It is good. For some weeks your cultivation comes a little easier." : "The wine is warm and a little dizzy. For some weeks your cultivation hums.");
            c.rel(1);
        } else if (p == 1) {
            if (c.pay(20)) {
                Item food = new Item(chef ? Item.ItemType.Food : Item.ItemType.Wine, lvl, 2, name);
                food.maker = c.npc.name + " " + c.npc.lastName; food.makerID = c.npc.id;
                Character.addToBag(Life.goods(c.me), food);
                c.say("Wrapped and ready. Two portions.");
                c.note("You pay 20 ss and take two portions of " + name + " ('eat' or 'drink' later).");
                c.rel(1);
            } else c.say("Come back when you've a few stones to spare.");
        } else c.say(c.line("r_nod"));
    }

    // ---- music
    private static void songScene(Ctx c) {
        c.say(c.open(), "May I play for you? A tune costs nothing.");
        String[] o = {"Listen.", "Listen, and tip them 20 ss.", "Walk on."};
        int[] t = {KIND, KIND, CAREFUL};
        int p = c.pick(o, t);
        if (p <= 1) {
            boolean tip = p == 1 && c.pay(20);
            Life.Fx f = Life.fx(c.me);
            f.inspiredWeeks = Math.max(f.inspiredWeeks, tip ? 8 : 4);
            f.inspiredPow = Math.max(f.inspiredPow, tip ? 0.12 : 0.08);
            c.note("They play '" + Naming.songTitle(RND, 0) + "'. " + (tip ? "You leave 20 ss in the bowl. " : "") + "It settles in your chest; for some weeks your cultivation comes easier.");
            c.say(tip ? "You're kind. I'll play you something better next time." : "Thank you for listening. Few do.");
            c.rel(tip ? 2 : 1);
        } else c.say(c.any("Another time, then.", "As you like. It's a short tune, though."));
    }

    // ---- a sect asks
    private static void recruitScene(Ctx c) {
        Organization s = c.npc.sect;
        c.sect = s.name;
        c.say(c.line("invite"));
        boolean can = s.canJoin(c.me);
        String[] o = {"Ask what they require.", "Accept the invitation.", "Decline."};
        int[] t = {CURIOUS, BOLD, CAREFUL};
        int p = c.pick(o, t);
        if (p == 0) {
            c.say(can ? "Nothing you lack. Come to our hall at " + s.location.name + " " + PlayerControl.coord(s.location) + " and take the oath." : "Not yet: " + s.whyNot(c.me) + ".");
            if (s.location != null) c.me.learnPlace(s.location);
        } else if (p == 1) {
            if (!can) c.say("I wish I could. " + s.whyNot(c.me) + ".");
            else if (c.me.location == s.location) { s.join(c.me); c.say("Welcome to " + s.name + "."); c.note("You join " + s.name + "."); c.rel(1); }
            else { c.say("Come to our hall at " + s.location.name + " " + PlayerControl.coord(s.location) + " and ask for me."); c.me.learnPlace(s.location); c.note("You mark " + s.location.name + " on your map ('join' there when you are ready)."); }
        } else c.say(c.any("The door stays open.", "Think about it. We keep our word.", "No pressure. Come and see us if you change your mind."));
    }

    // ---- a threat
    private static void shakedownScene(Ctx c) {
        long amt = Math.max(10, Math.min(500, c.wealth() / 10));
        c.say(c.open(), "Nice robes. Nicer pouch. Hand over " + amt + " ss and we part friends.");
        String[] o = {"Pay " + amt + " ss.", "Say you are not alone.", "Refuse."};
        int[] t = {CAREFUL, BOLD, BOLD};
        int p = c.pick(o, t);
        if (p == 0 && c.pay(amt)) { c.say("Pleasure doing business."); c.note("You hand over " + amt + " ss."); }
        else if (p == 1 && RND.nextInt(100) < 30 + 3 * (c.me.lvl - c.npc.lvl)) { c.say("...Hm. Another time, then."); c.note("They look over your shoulder, and decide against it."); }
        else { c.say(p == 2 ? "Wrong answer." : "Don't lie to me."); c.note("They draw."); c.npc.battleCheck(c.me); }
    }

    // ---- gossip
    static Character thirdPerson(Character npc, Character me) {
        ArrayList<Character> pool = new ArrayList<>();
        for (Relationship r : new ArrayList<>(npc.relationshipList.relationships)) {
            if (r.friendID == me.id || r.value == 0) continue;
            Character o = Character.charById(r.friendID);
            if (o != null && !o.dead && o.born && o.location != null && o.age >= 8 && o.trait != Character.Traits.Beast && o.trait != Character.Traits.Monster && o != npc) pool.add(o);
        }
        return pool.isEmpty() ? null : pool.get(RND.nextInt(pool.size()));
    }

    private static boolean gossipScene(Ctx c, boolean asked) {
        Character t = thirdPerson(c.npc, c.me);
        if (t == null) { if (asked) c.say("I don't hear much about anyone these days."); return false; }
        Relationship r = c.npc.relationshipList.contains(t.id);
        int v = r == null ? 0 : r.value;
        c.third = t.name + " " + t.lastName;
        String how = v >= 2 ? new String[]{"Good soul. Spoke well of everyone.", "In fine spirits, and generous with it.", "Doing well. They asked about the old days."}[RND.nextInt(3)]
                : v == 1 ? new String[]{"Fine, I suppose. Hard to read.", "Busy. Said little.", "Same as ever."}[RND.nextInt(3)]
                : new String[]{"In a foul mood. Don't expect a warm welcome.", "Asking too many questions about people. Be careful.", "Spoiling for a fight, if you ask me."}[RND.nextInt(3)];
        c.say(c.open(), "I ran into {third} the other day. " + how);
        String[] o = {"Ask where they are now.", "Tell them to mind their own business.", "Change the subject."};
        int[] tt = {CURIOUS, COLD, CAREFUL};
        int p = c.pick(o, tt);
        if (p == 0) {
            c.say("Last I saw, near " + t.location.name + " " + PlayerControl.coord(t.location) + ". They may have moved on.");
            c.me.learnPlace(t.location);
            c.note("You mark " + t.location.name + " " + PlayerControl.coord(t.location) + " on your map.");
            c.rel(1);
        } else if (p == 1) { c.say("Touchy. All right, all right."); c.rel(-1); }
        else c.say("Of course. Where was I?");
        return true;
    }

    // ---- a lesson
    private static boolean adviceScene(Ctx c, boolean asked) {
        if (c.npc.lvl < c.me.lvl + 5 && !asked) return false;
        if (c.npc.lvl < c.me.lvl) { if (asked) c.say("You know more than I could teach, I think. Share what you know instead."); return false; }
        if (GameDriver.time - adviceWeek(c) < 26) { if (asked) c.say("I've shown you what I can for now. Practice it, then come back."); return false; }
        c.say(c.open(), asked ? "A lesson? Gladly. Watch my feet, and breathe the way I do." : "You've come a long way. Your footing is loose on the " + (c.me.martialTechnique != null ? c.me.martialTechnique.name : "stance") + ". Let me show you something.");
        boolean copy = c.relValue() >= 3 && betterTechnique(c.npc, c.me) != null;
        ArrayList<String> o = new ArrayList<>(); ArrayList<Integer> t = new ArrayList<>();
        o.add("Gladly. Show me."); t.add(CURIOUS);
        if (copy) { o.add("Ask for your best technique."); t.add(BOLD); }
        o.add("Another time."); t.add(CAREFUL);
        int p = pickList(c, o, t);
        if (o.get(p).startsWith("Gladly")) {
            c.say("Again. Slower. Good.");
            c.me.meditate(false);
            c.note("A few hours of pointers: your cultivation moves, and every technique you carry gains a level.");
            setAdvice(c);
            c.rel(1);
        } else if (o.get(p).startsWith("Ask")) {
            Technique best = betterTechnique(c.npc, c.me);
            Technique cp = new Technique(best);
            cp.lvl = Math.max(1, best.lvl / 2);
            c.me.techniqueList.add(cp);
            c.me.equip();
            c.say("This was my life's work. Don't waste it.");
            c.note("You learn " + best.name + " (potential " + best.potential + ", at half its level).");
            setAdvice(c);
            c.rel(1);
        } else c.say("Whenever you like.");
        return true;
    }

    private static void setAdvice(Ctx c) {
        Relationship r = c.npc.relationshipList.contains(c.me.id), r2 = c.me.relationshipList.contains(c.npc.id);
        if (r != null) r.lastAdvice = GameDriver.time;
        if (r2 != null) r2.lastAdvice = GameDriver.time;
    }

    static Technique betterTechnique(Character npc, Character me) {
        Technique best = null, mine = null;
        for (Technique t : npc.techniqueList) if (best == null || t.potential > best.potential) best = t;
        for (Technique t : me.techniqueList) if (mine == null || t.potential > mine.potential) mine = t;
        if (best == null || (mine != null && best.potential <= mine.potential)) return null;
        for (Technique t : me.techniqueList) if (t.name.equals(best.name)) return null;
        return best;
    }

    // ---- luck
    private static void fortuneScene(Ctx c) {
        c.say(c.open(), "Give me your hand. Luck leaves marks, and yours is interesting.");
        String[] o = {"Let them read it.", "Joke about it.", "Pull your hand away."};
        int[] t = {CURIOUS, KIND, CAREFUL};
        int p = c.pick(o, t);
        if (p == 0) {
            int r = RND.nextInt(100);
            if (r < 20) {
                Life.Fx f = Life.fx(c.me); f.inspiredWeeks = Math.max(f.inspiredWeeks, 8); f.inspiredPow = Math.max(f.inspiredPow, 0.12);
                c.say("Oh. Oh, that's a good one. A big turn is coming for you, and soon.");
                c.note("You feel lighter. For some weeks your cultivation comes easier.");
                c.rel(1);
            } else if (r < 80) { c.say("Steady. Nothing you won't handle. A little patience will pay."); c.rel(1); }
            else c.say("Hm. Watch your back this season. That's all I'll say.");
        } else if (p == 1) { c.say("Ha! Keep laughing. Luck likes people who laugh."); c.rel(1); }
        else c.say("Not a believer. Fine.");
    }

    // ---- a shady offer
    private static void poisonScene(Ctx c) {
        Item vial = Life.firstOf(c.npc, Item.ItemType.Poison);
        if (vial == null) { greetingScene(c); return; }
        long price = Math.max(10, Math.round(Character.itemPrice(new Item(Item.ItemType.Poison, vial.lvl, 1, vial.name)) * 1.3));
        c.item = vial.name; c.price = String.valueOf(price);
        c.say(c.open(), "Quietly now. I make things that solve problems. {item}: {price} ss.");
        boolean can = c.me.canAfford(price);
        ArrayList<String> o = new ArrayList<>(); ArrayList<Integer> t = new ArrayList<>();
        if (can) { o.add("Buy a vial."); t.add(GREEDY); }
        o.add("Tell them to stay away from you."); t.add(COLD);
        o.add("Walk on."); t.add(CAREFUL);
        int p = pickList(c, o, t);
        if (o.get(p).startsWith("Buy")) {
            c.pay(price);
            vial.quantity--; if (vial.quantity <= 0) c.npc.goods.remove(vial);
            Character.addToBag(Life.goods(c.me), new Item(Item.ItemType.Poison, vial.lvl, 1, vial.name));
            c.say("Use it wisely. Or don't. I won't ask.");
            c.note("You pay " + price + " ss and pocket the vial.");
        } else if (o.get(p).startsWith("Tell")) { c.say("Everyone says that. Until they need me."); c.rel(-1); }
        else c.say("You know where to find me.");
    }

    // ---- a story
    private static void storyScene(Ctx c) {
        Character n = c.npc;
        ArrayList<String> facts = new ArrayList<>();
        facts.add("I'm " + n.age + " this year. Feels like both more and less.");
        if (n.spouse != null && !n.spouse.dead) facts.add("My " + ("Male".equals(n.spouse.gender) ? "husband" : "wife") + " would laugh to see me standing here talking.");
        if (n.children != null && !n.children.isEmpty()) facts.add("I have " + n.children.size() + " child" + (n.children.size() == 1 ? "" : "ren") + " now. They have my stubbornness. I'm sorry for them.");
        if (n.sect != null) facts.add("I belong to " + n.sect.name + ". They took me in when I had nothing. I don't forget that.");
        if (n.mother != null && n.mother.dead) facts.add("I lost my mother some years back. You never stop turning around to tell them things.");
        if (n.father != null && n.father.dead) facts.add("My father is gone now. I still hear his voice when I'm unsure.");
        facts.add(tradeLine(n));
        c.say(c.open(), facts.get(RND.nextInt(facts.size())));
        String[] o = {"Listen, and say something kind.", "Share something of your own.", "Cut it short."};
        int[] t = {KIND, KIND, COLD};
        int p = c.pick(o, t);
        if (p == 0) { c.say("Thank you. Not many stop to hear it."); c.rel(1); }
        else if (p == 1) {
            c.say("Is that so? Then we're not so different, you and I. " + tradeEcho(c.me));
            c.rel(2);
        } else c.say(c.any("Right. Of course. Forgive me.", "You're busy. I'm rambling. Go on.", "Ah. Yes. Sorry."));
    }

    static String tradeLine(Character n) {
        switch (n.trait) {
            case Alchemist: return "I make pills for a living. People think it's all fire and herbs. It's mostly waiting.";
            case Blacksmith: return "I work metal. The sword you'll swing next year might be one I've not yet made.";
            case Tailor: return "I sew. Everyone laughs, until their robe saves their life.";
            case Scribe: return "I draw arrays. One wrong line and the whole thing hums out of tune.";
            case Merchant: return "I buy low and sell high. It's less glamorous than it sounds, and more tiring.";
            case Musician: return "I play for whoever will listen. Most days that's nobody. Some days that's enough.";
            case Chef: return "Food is how I say what I can't say any other way.";
            case Drunken: return "They call me the Drunken. I prefer \"well preserved\".";
            case Poisoner: return "Most of what I mix does no harm. Most.";
            case Tamer: return "Beasts are honest. They bite when they mean to, and they stay when they mean to.";
            case Innovative: return "I make up techniques. Half of them are rubbish. The other half keep me alive.";
            case Leader: return "People follow me, and I never quite understand why.";
            case Lucky: return "Everyone assumes I've never worked a day. Luck is a full-time occupation.";
            case Gatherer: return "I gather herbs. You learn the hills like faces.";
            case Miner: return "I dig for ore. The mountain keeps its secrets, but it pays in rock.";
            case Runner: return "I carry word between places. I've worn out more boots than I can count.";
            case Friendly: return "I just like people. It's that simple, and it gets me in trouble.";
            default: return "I cultivate and I keep walking. It's not much of a story, but it's mine.";
        }
    }

    static String tradeEcho(Character me) {
        switch (me.trait) {
            case Alchemist: return "Another one who knows the waiting.";
            case Blacksmith: return "Another one with burnt fingers, I see.";
            case Merchant: return "Another one who counts everything.";
            case Musician: return "Another one who hears things.";
            case Friendly: return "Another one who can't walk past anyone.";
            default: return "I'm glad you told me.";
        }
    }

    // ---- an apology
    private static void apologyScene(Ctx c) {
        c.say(c.open(), "I owe you an apology. I was out of line the last time. I've thought about it more than I'd like to admit.");
        String[] o = {"Forgive them.", "Say words are cheap.", "Say you will not forget."};
        int[] t = {KIND, COLD, COLD};
        int p = c.pick(o, t);
        if (p == 0) { c.say("Thank you. Truly."); c.rel(2); }
        else if (p == 1) c.say("Fair. Let my actions prove it, then.");
        else { c.say("Then I'll have to earn it."); }
    }

    // ------------------------------------------------------------------ conversation (depth 3): the Talk button
    static final String[] TOPICS = {"How have you been?", "Any news?", "Tell me about yourself.", "What do you know of someone?", "Could you teach me something?", "Anything for sale?", "Care for a friendly spar?", "Walk with me.", "Give a gift.", "Goodbye."};
    static final int[] TOPIC_TEMPER = {KIND, CURIOUS, CURIOUS, CURIOUS, CURIOUS, GREEDY, BOLD, KIND, KIND, CAREFUL};

    /** The Talk button / `talk <n>` for a human who plays: a conversation to steer (a window with a portrait), else the speaker's words in the log. */
    static boolean converse(Character me, Character npc) {
        if (npc == null || me == null || npc.dead || npc.trait == Character.Traits.Beast || npc.trait == Character.Traits.Monster) return false;
        boolean gui = GameDriver.guiEnabled && GameDriver.mapViewer != null && Settings.dialoguePopups && me.isManualPc();
        boolean auto = !me.isManualPc() && Settings.dialogueAuto;
        if (!gui && !auto) {
            if (me.player == 1 && Lessons.due(npc, me)) { // the talk turns to a lesson first (told in the log), then the usual words
                Io lio = new LogIo();
                Ctx lc = new Ctx(npc, me, lio);
                lio.open(npc, me);
                Lessons.give(lc);
            }
            Ctx c = new Ctx(npc, me, null);
            Relationship r = c.rel;
            flavor(npc, me, c.line(firstMeeting(r) ? "greet_first" : "greet"));
            remember(npc, me);
            return Settings.dialogueFlavor;
        }
        Io io = gui ? new GuiDialogue(me) : new AutoIo();
        Ctx c = new Ctx(npc, me, io);
        io.open(npc, me);
        try {
            if (me.player == 1 && Lessons.due(npc, me)) Lessons.give(c); // the person asked for a talk and the talk turns to a lesson first, then goes on as a conversation
            hub(c);
        } finally { io.close(); }
        return true;
    }

    /** The conversation itself: greeting, topics (each once a visit), goodbye. */
    static void hub(Ctx c) {
        Character npc = c.npc, me = c.me;
        boolean first = firstMeeting(c.rel);
        c.say(c.line(first ? "greet_first" : "greet"));
        if (!first && c.voice != Voice.HOSTILE && RND.nextInt(5) < 3) { String rm = remark(c); if (!rm.isEmpty()) c.say(rm); }
        remember(npc, me);
        Relationship r = npc.relationshipList.contains(me.id);
        if (r == null) r = me.relationshipList.contains(npc.id); // the NPC's list can be full (no record of you): your own side then keeps the topics of this visit
        int asked = 0;
        for (int guard = 0; guard < 20; guard++) {
            // the topics offered: those with something to say; each can be raised once a visit
            ArrayList<String> o = new ArrayList<>(); ArrayList<Integer> t = new ArrayList<>(); ArrayList<Integer> idx = new ArrayList<>();
            for (int i = 0; i < TOPICS.length - 1; i++) {
                if (r != null && (r.topics & (1 << i)) != 0) continue;
                if (i == 3 && thirdPerson(npc, me) == null) continue;
                o.add(TOPICS[i]); t.add(TOPIC_TEMPER[i]); idx.add(i);
            }
            o.add(TOPICS[TOPICS.length - 1]); t.add(CAREFUL); idx.add(TOPICS.length - 1);
            int p = pickList(c, o, t);
            int topic = idx.get(p);
            if (topic == TOPICS.length - 1) break;
            markTopic(r, me.relationshipList.contains(npc.id), topic);
            asked++;
            if (c.voice == Voice.HOSTILE && topic != 8) { c.say("I have nothing to say to you."); break; }
            topic(c, topic);
            if (me.dead || npc.dead) break;
            if (asked >= 6) { c.say("Well. I should be getting on."); break; }
        }
        c.say(c.line("farewell"));
    }

    /** The topic was raised this visit: kept on the speaker's record of the person and copied to the person's record of the speaker (either may be missing: a list that was trimmed). */
    static void markTopic(Relationship r, Relationship r2, int topic) {
        if (r != null) r.topics |= 1 << topic;
        if (r != null && r2 != null && r2 != r) r2.topics = r.topics;
    }

    private static void topic(Ctx c, int topic) {
        switch (topic) {
            case 0: statusTopic(c); break;
            case 1: if (!rumorScene(c, true)) c.say("Nothing worth repeating just now. Quiet times."); break;
            case 2: storyScene(c); break;
            case 3: gossipScene(c, true); break;
            case 4: adviceScene(c, true); break;
            case 5: pitchScene(c, true); break;
            case 6: sparScene(c); break;
            case 7: walkTopic(c); break;
            case 8: giftTopic(c); break;
            default: break;
        }
    }

    /** What the person is doing, in words, from what they really are doing. */
    static String activity(Character n) {
        Quest q = n.currentQuest;
        if (q != null) {
            String where = q.targetArea != null ? q.targetArea.name : "";
            String who = "";
            if (q.category == Quest.QuestCategory.Character) { Character t = Character.charById(q.targetCharacter); if (t != null) who = t.name + " " + t.lastName; }
            switch (q.name) {
                case "Hunt Beast": return "hunting monsters around " + where;
                case "Explore Dungeon": return "heading for " + where + ", a dungeon I mean to explore";
                case "Heaven Treasure": return "chasing a rumor of a treasure fallen at " + where;
                case "Go Sell": case "Sell Stock": case "Trade Run": case "Restock Trip": return "taking goods to " + where;
                case "Buy Gear": return "off to " + where + " to buy better gear";
                case "Join Sect": return "on my way to " + where + " to join the sect there";
                case "Meditation Retreat": case "Pilgrimage": return "walking to " + where + " to meditate";
                case "Sect Mission": case "Deliver Materials": case "Return to Sect": return "on an errand for my sect";
                case "Find Person": case "Visit Family": return who.isEmpty() ? "looking for someone" : "looking for " + who;
                case "Battle Rival": case "Battle Dislike": case "Revenge": case "Battle Arch-Enemy": return who.isEmpty() ? "settling an old score" : "settling an old score with " + who;
                case "Courtship": return "courting " + (who.isEmpty() ? "someone special" : who);
                case "Teach Disciple": return "teaching " + (who.isEmpty() ? "a student" : who);
                case "Found City": case "Found Sect": return "looking for a place to found something of my own";
                case "Perform": case "Festival Trade": return "heading for " + where + " for the festival";
                default: break;
            }
        }
        switch (n.trait) {
            case Alchemist: return "brewing pills, mostly";
            case Blacksmith: return "working metal, mostly";
            case Tailor: return "sewing, mostly";
            case Scribe: return "drawing arrays, mostly";
            case Merchant: return "buying and selling, mostly";
            case Musician: return "playing wherever people will listen";
            case Chef: return "cooking for anyone hungry";
            case Gatherer: return "gathering herbs";
            case Miner: return "digging for ore";
            case Tamer: return "keeping an eye on the beasts";
            case Innovative: return "working out a technique";
            default: return "cultivating, mostly";
        }
    }

    private static void statusTopic(Ctx c) {
        String a = activity(c.npc);
        String s;
        switch (c.voice) {
            case WARM: s = "Can't complain. I've been " + a + "."; break;
            case CHEERFUL: s = "Oh, you know. " + cap(a) + "! Can't stop moving."; break;
            case BLUNT: s = cap(a) + "."; break;
            case FORMAL: s = "I have been occupied: " + a + "."; break;
            case SLY: s = "Busy. " + cap(a) + ". Profitable, mostly."; break;
            case WEARY: s = "Slower than I used to be. " + cap(a) + ", when my back allows."; break;
            case SCHOLAR: s = "Fascinating, actually. I've been " + a + "."; break;
            default: s = "None of your concern."; break;
        }
        c.say(s);
        if (c.npc.ss + c.npc.sc * 1_000_000L < 50) c.say("Money is thin, if I'm honest.");
        else if (c.npc.maxAge > 0 && c.npc.age >= 0.9 * c.npc.maxAge) c.say("And I feel the end coming closer. Not that I mind, much.");
        if (c.relValue() < 3) c.rel(0);
    }

    private static void walkTopic(Ctx c) {
        if (c.npc.partyLeaderId == c.me.id) { c.say("I'm already with you, aren't I?"); return; }
        if (c.me.followerIds != null && c.me.followerIds.size() >= c.me.partyCap()) { c.say("Your party is full already. I'd only be in the way."); return; }
        if (c.npc.partyLeaderId >= 0) { c.say("I'm following someone already."); return; }
        c.say(c.open(), "Walk with you? Let me think.");
        if (RND.nextInt(100) < c.charm()) {
            c.say(c.line("squad"));
            c.npc.joinParty(c.me);
            c.note(c.npc.name + " joins your party.");
            c.rel(1);
        } else c.say(c.relValue() >= 1 ? "Not now. I've my own road to walk. Another time." : "I don't know you well enough for that.");
    }

    private static void giftTopic(Ctx c) {
        ArrayList<Item> gifts = new ArrayList<>();
        for (Item it : c.me.backpack) if (it != null && it != c.me.equippedWeapon && it != c.me.equippedClothing && it.itemType != null && it.lvl > 0) { gifts.add(it); if (gifts.size() >= 5) break; }
        for (Item it : c.me.goods == null ? new ArrayList<Item>() : c.me.goods) if (it != null && it.quantity > 0 && (it.itemType == Item.ItemType.Food || it.itemType == Item.ItemType.Wine) && gifts.size() < 7) gifts.add(it);
        if (gifts.isEmpty()) { c.say("You have nothing to spare, and I wouldn't take it if you did. Your company is gift enough."); return; }
        c.say("A gift? You needn't. ...What did you have in mind?");
        ArrayList<String> o = new ArrayList<>(); ArrayList<Integer> t = new ArrayList<>();
        for (Item it : gifts) { o.add("Give " + it.name + (it.quantity > 1 ? " (one of " + it.quantity + ")" : "")); t.add(KIND); }
        o.add("Never mind."); t.add(CAREFUL);
        int p = pickList(c, o, t);
        if (p >= gifts.size()) { c.say("Another time."); return; }
        Item it = gifts.get(p);
        long worth = Math.max(1, Character.itemPrice(it.quantity > 1 ? new Item(it.itemType, it.lvl, 1, it.name) : it));
        if (it.itemType == Item.ItemType.Food || it.itemType == Item.ItemType.Wine) {
            it.quantity--; if (it.quantity <= 0) c.me.goods.remove(it);
            Character.addToBag(Life.goods(c.npc), new Item(it.itemType, it.lvl, 1, it.name));
        } else { c.me.backpack.remove(it); c.npc.backpack.add(it); }
        c.say(worth >= 1000 ? "This is too much. Thank you. I mean it." : "That's kind. I'll keep it.");
        c.note("You give " + it.name + ".");
        c.rel(worth >= 1000 ? 3 : 1);
    }

    // ------------------------------------------------------------------ an AUTO character's talks
    /** Answers by temper and writes the talk to the log. */
    /** A talk told in the log (a lesson when the portrait window is off, or in the terminal): the speaker's words are printed, a question takes its first answer. */
    static final class LogIo implements Io {
        Character npc;
        public void open(Character npc, Character me) { this.npc = npc; }
        public void npc(String text) { System.out.println(npc.name + " " + npc.lastName + ": \"" + text + "\""); }
        public void narrate(String text) { System.out.println("  (" + text + ")"); }
        public int pick(String[] labels, int[] temper) { return 0; }
        public void close() { }
    }

    static final class AutoIo implements Io {
        Character npc, me;
        public void open(Character npc, Character me) { this.npc = npc; this.me = me; if (Settings.dialogueFlavor) System.out.println("-- " + me.name + " talks with " + npc.name + " " + npc.lastName + " --"); }
        public void npc(String text) { if (Settings.dialogueFlavor) System.out.println(npc.name + " " + npc.lastName + ": \"" + text + "\""); }
        public void narrate(String text) { if (Settings.dialogueFlavor) System.out.println("  (" + text + ")"); }
        public int pick(String[] labels, int[] temper) {
            int best = labels.length - 1; double bestW = -1;
            for (int i = 0; i < labels.length; i++) {
                double w = 1 + RND.nextDouble();
                int t = temper[i];
                if (me.affiliationNum > 0 && t == KIND) w += 2.5;
                if (me.affiliationNum < 0 && (t == GREEDY || t == COLD)) w += 2;
                if (me.affiliationNum < 0 && t == BOLD) w += 1;
                if (me.affiliationNum == 0 && (t == CURIOUS || t == CAREFUL)) w += 1.5;
                if (me.trait == Character.Traits.Merchant && t == GREEDY) w += 2;
                if (me.trait == Character.Traits.Friendly && t == KIND) w += 2.5;
                if ((me.trait == Character.Traits.Scribe || me.trait == Character.Traits.Alchemist || me.trait == Character.Traits.Innovative) && t == CURIOUS) w += 2.5;
                if ((me.trait == Character.Traits.Leader || me.trait == Character.Traits.Blacksmith) && t == BOLD) w += 1.5;
                if (me.caution > 60 && t == CAREFUL) w += 2; else if (me.caution < 30 && t == BOLD) w += 1;
                if (i == labels.length - 1 && labels.length > 2) w -= 0.8; // the way out is the last resort
                if (w > bestW) { bestW = w; best = i; }
            }
            if (Settings.dialogueFlavor) System.out.println("  " + me.name + ": " + labels[best]);
            return best;
        }
        public void close() { }
    }
}
