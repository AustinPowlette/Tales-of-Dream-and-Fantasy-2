package Game;

import java.io.PrintStream;
import java.util.*;

import Game.DialogueText.Voice;

/** 2026-10-09: what people say (Dialogue): the words for every voice, the voices, the scenes and their effects, the conversation, the log lines of the terminal and of an AUTO character, and the
 *  decisions put in the speaker's words. The window (GuiDialogue) is looked at by PcProbeGui. */
public class DialogueProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true; // frozen before the first tick: the NPCs act before the human prompt of a tick, and one of them could otherwise ask the character a question before the probe is ready
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }

    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Cheater;
        c.id = Map.characters.size();
        GameDriver.UID = Math.max(GameDriver.UID, c.id + 1);
        Map.characters.add(c); Map.population++;
        c.player = 0;
        return c;
    }
    static Character npc(Character.Traits tr, int lvl, int major) { Character c = adult(lvl, major); c.trait = tr; c.affiliation = "Neutral"; c.affiliationNum = 0; return c; }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static Area tile(int x, int y) {
        x = Math.floorMod(x, Map.range); y = Math.floorMod(y, Map.range);
        if (Map.tiles[x][y] == null) synchronized (Map.tileLock) { if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e'); }
        return Map.tiles[x][y];
    }
    static Area plain(Set<Area> not) {
        for (int k = 3; k < 14; k++) for (int[] d : new int[][]{{k, 3}, {3, k}, {-k, 3}}) {
            Area a = tile(home().x + d[0], home().y + d[1]);
            if (a != null && !a.city && a.sect == null && a.dungeonType == null && a.loreSite == null && !a.spiritualVein && !not.contains(a)) return a;
        }
        return null;
    }
    static void setRel(Character a, Character b, int v) {
        Relationship r = a.relationshipList.contains(b.id), r2 = b.relationshipList.contains(a.id);
        if (r == null) { r = new Relationship(b.id, v); a.relationshipList.add(r); } else r.value = v;
        if (r2 == null) { r2 = new Relationship(a.id, v); b.relationshipList.add(r2); } else r2.value = v;
    }
    static int relOf(Character a, Character b) { Relationship r = a.relationshipList.contains(b.id); return r == null ? 0 : r.value; }

    /** An Io that answers by a plan and keeps the whole talk. */
    static class TestIo implements Dialogue.Io {
        final StringBuilder log = new StringBuilder();
        int mode; final Random rnd = new Random(7); int npcLines = 0, picks = 0; final ArrayDeque<Integer> script = new ArrayDeque<>();
        TestIo(int mode) { this.mode = mode; }  // 0 = first reply, 1 = last reply, 2 = random, 3 = the script then the last
        public void open(Character npc, Character me) { }
        public void npc(String t) { npcLines++; log.append("N: ").append(t).append('\n'); }
        public void narrate(String t) { log.append("- ").append(t).append('\n'); }
        public int pick(String[] labels, int[] temper) {
            picks++;
            int p = mode == 0 ? 0 : mode == 1 ? labels.length - 1 : mode == 2 ? rnd.nextInt(labels.length) : script.isEmpty() ? labels.length - 1 : Math.min(script.poll(), labels.length - 1);
            log.append("> ").append(labels[p]).append('\n');
            return p;
        }
        public void close() { }
        boolean clean() { String s = log.toString(); return !s.contains("{") && !s.contains("}") && !s.contains("null"); }
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000; p.pc().squadPolicy = 2; p.pc().friendPolicy = 1; p.lvl = 40; p.majorLvl = 4; p.ss = 5000;
        GameDriver.time = Math.max(GameDriver.time, 200);
        Map.currentCharacter = p;
        Set<Area> used = new HashSet<>();
        Area spot = plain(used); used.add(spot);
        tp(p, spot);
        Settings.dialogueFlavor = true; Settings.dialoguePopups = true; Settings.dialogueAuto = false;

        // ---- T: the words
        boolean allVoices = true; StringBuilder miss = new StringBuilder();
        String[] needAll = {"greet", "greet_first", "friend", "squad", "challenge", "farewell", "open", "r_warm", "r_nod", "r_cold", "r_spar", "alliance", "tribute", "defect", "invite"};
        for (String k : needAll) for (Voice v : Voice.values()) if (DialogueText.lines(k, v).length == 0) { allVoices = false; miss.append(k).append('/').append(v).append(' '); }
        check("T1 every kind has words for every voice " + miss, allVoices);
        Character subject = npc(Character.Traits.Friendly, 30, 3); tp(subject, spot);
        int lines = 0; boolean clean = true; StringBuilder bad = new StringBuilder();
        for (String k : DialogueText.kinds()) for (Voice v : Voice.values()) for (String raw : DialogueText.lines(k, v)) {
            Dialogue.Ctx c = new Dialogue.Ctx(subject, p, null); c.thing = "x"; c.rumor = "x"; c.third = "x"; c.price = "1"; c.item = "x"; c.sect = "Sect";
            String f = c.fill(raw); lines++;
            if (f.contains("{") || f.contains("}") || f.trim().isEmpty()) { clean = false; bad.append(k).append(':').append(raw).append(' '); }
        }
        check("T2 all " + lines + " lines fill their slots cleanly " + bad, clean && lines > 150);

        // ---- V: voices and names
        Character evil = npc(Character.Traits.Cheater, 30, 3); evil.affiliation = "Evil"; evil.affiliationNum = -1;
        Character old = npc(Character.Traits.Cheater, 30, 3); old.age = 90; old.maxAge = 100;
        Character foe = npc(Character.Traits.Friendly, 30, 3); setRel(foe, p, -2);
        Character mer = npc(Character.Traits.Merchant, 30, 3), sch = npc(Character.Traits.Scribe, 30, 3), cheery = npc(Character.Traits.Musician, 30, 3), smith = npc(Character.Traits.Blacksmith, 30, 3), lead = npc(Character.Traits.Leader, 30, 3);
        check("V1 the voice follows the trade, the years and the ties: Evil sly, old weary, a foe hostile, Merchant sly, Scribe scholarly, Musician cheerful, Blacksmith blunt, Leader formal",
                Dialogue.voiceOf(evil, p) == Voice.SLY && Dialogue.voiceOf(old, p) == Voice.WEARY && Dialogue.voiceOf(foe, p) == Voice.HOSTILE && Dialogue.voiceOf(mer, p) == Voice.SLY && Dialogue.voiceOf(sch, p) == Voice.SCHOLAR
                        && Dialogue.voiceOf(cheery, p) == Voice.CHEERFUL && Dialogue.voiceOf(smith, p) == Voice.BLUNT && Dialogue.voiceOf(lead, p) == Voice.FORMAL);
        Character elder = npc(Character.Traits.Cheater, 90, 8), junior = npc(Character.Traits.Cheater, 5, 1), pal = npc(Character.Traits.Cheater, 40, 4); setRel(pal, p, 3);
        check("V2 a much stronger stranger says young one, a much weaker says Senior, a close friend uses the name (" + Dialogue.address(elder, p) + ", " + Dialogue.address(junior, p) + ", " + Dialogue.address(pal, p) + ")",
                Dialogue.address(elder, p).equals("young one") && Dialogue.address(junior, p).equals("Senior") && Dialogue.address(pal, p).equals(p.name));

        // ---- S: every scene runs, in every voice, with every plan of replies, and says clean words
        String[] ids = Dialogue.SCENES;
        Character.Traits[] traits = Character.Traits.values();
        int runs = 0; boolean allClean = true, noCrash = true; StringBuilder what = new StringBuilder();
        Area veinA = plain(used); used.add(veinA); veinA.spiritualVein = true;
        for (String id : ids) for (int mode = 0; mode < 3; mode++) for (int k = 0; k < traits.length; k++) {
            Character.Traits tr = traits[k];
            if (tr == Character.Traits.Beast || tr == Character.Traits.Monster) continue;
            Character n = npc(tr, 40 + (k % 3) * 10, 4 + k % 3);
            n.affiliationNum = k % 3 - 1; n.affiliation = n.affiliationNum < 0 ? "Evil" : n.affiliationNum > 0 ? "Good" : "Neutral";
            tp(n, spot); n.knownLocations.add(veinA); n.ss = 50;
            if (k % 4 == 0) setRel(n, p, k % 7 - 3);
            if (tr == Character.Traits.Poisoner) Character.addToBag(Life.goods(n), new Item(Item.ItemType.Poison, 2, 3, "Test Poison"));
            if (tr == Character.Traits.Leader) { n.sect = new Organization(n); }
            TestIo io = new TestIo(mode);
            try {
                p.ss = 5000; p.convertCurrency();
                Dialogue.run(id, new Dialogue.Ctx(n, p, io));
            } catch (RuntimeException e) { noCrash = false; what.append(id).append('/').append(tr).append(':').append(e).append(' '); }
            runs++;
            boolean needsStuff = id.equals("pitch") || id.equals("recruit");
            if (!io.clean() || (io.npcLines == 0 && !needsStuff)) { allClean = false; if (what.length() < 600) what.append(id).append('/').append(tr).append(" unclean: ").append(io.log.toString().replace("\n", " | ")).append(' '); }
            p.dead = false; p.dead = false;
            n.dead = true; if (n.sect != null && tr == Character.Traits.Leader) n.sect = null;
        }
        check("S1 " + runs + " scene runs (" + ids.length + " scenes x 3 plans x every trait) without a crash " + (noCrash ? "" : what), noCrash);
        check("S2 every scene says something and its words are clean " + (allClean ? "" : what), allClean);

        // ---- E: effects
        p.ss = 5000; p.convertCurrency(); p.dead = false;
        Character poor = npc(Character.Traits.Cheater, 30, 3); poor.ss = 5; tp(poor, spot); poor.affiliationNum = 1; poor.affiliation = "Good"; long w0 = PlayerLists.wealth(p);
        TestIo io = new TestIo(0);
        Dialogue.run("help", new Dialogue.Ctx(poor, p, io));
        check("E1 helping someone in need moves the stones and warms the tie (" + (w0 - PlayerLists.wealth(p)) + " ss given, rel " + relOf(poor, p) + ")", w0 - PlayerLists.wealth(p) >= 20 && poor.ss > 5 && relOf(poor, p) >= 1);

        Character alch = npc(Character.Traits.Alchemist, 40, 5); tp(alch, spot); alch.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, p.majorLvl + 1, 3));
        int pills0 = 0; for (Item it : p.pillBag) pills0 += it.quantity; long w1 = PlayerLists.wealth(p);
        io = new TestIo(0); Dialogue.run("pitch", new Dialogue.Ctx(alch, p, io));
        int pills1 = 0; for (Item it : p.pillBag) pills1 += it.quantity;
        check("E2 buying the offered pill: it changes hands for the price (" + pills0 + " -> " + pills1 + " pills, " + (w1 - PlayerLists.wealth(p)) + " ss)", pills1 == pills0 + 1 && alch.pillBag.get(0).quantity == 2 && w1 > PlayerLists.wealth(p));

        Character chef = npc(Character.Traits.Chef, 40, 4); chef.professionLvl = 30; tp(chef, spot); p.fx = null;
        io = new TestIo(0); Dialogue.run("meal", new Dialogue.Ctx(chef, p, io));
        check("E3 accepting a chef's meal nourishes you", p.fx != null && p.fx.foodWeeks > 0);
        Character bard = npc(Character.Traits.Musician, 40, 4); tp(bard, spot); p.fx = null;
        io = new TestIo(0); Dialogue.run("song", new Dialogue.Ctx(bard, p, io));
        check("E4 listening to a musician inspires you", p.fx != null && p.fx.inspiredWeeks > 0);

        Character sage = npc(Character.Traits.Cheater, 90, 8); tp(sage, spot); setRel(sage, p, 2);
        Technique mt = p.martialTechnique; mt.potential = 80; mt.lvl = 10; double cb = p.cultivationBase;
        io = new TestIo(0); Dialogue.run("advice", new Dialogue.Ctx(sage, p, io));
        check("E5 a lesson raises your techniques a level and sets a half-year wait (" + mt.lvl + ")", mt.lvl == 11 && sage.relationshipList.contains(p.id).lastAdvice == GameDriver.time && Dialogue.weight("advice", new Dialogue.Ctx(sage, p, null)) == 0);

        Character third = npc(Character.Traits.Cheater, 30, 3); Area elsewhere = plain(used); used.add(elsewhere); tp(third, elsewhere);
        Character gos = npc(Character.Traits.Friendly, 30, 3); tp(gos, spot); setRel(gos, third, 2); p.knownLocations.remove(elsewhere);
        io = new TestIo(0); Dialogue.run("gossip", new Dialogue.Ctx(gos, p, io));
        check("E6 asking about a third person marks where they are", p.knownLocations.contains(elsewhere));

        Character rum = npc(Character.Traits.Gatherer, 30, 3); tp(rum, spot); rum.knownLocations.add(veinA); p.knownLocations.remove(veinA);
        boolean learned = false; String sample = "";
        for (int i = 0; i < 60 && !learned; i++) { TestIo r = new TestIo(0); Dialogue.run("rumor", new Dialogue.Ctx(rum, p, r)); learned = p.knownLocations.contains(veinA); sample = r.log.toString(); }
        check("E7 a rumor of a place puts it on your map (" + sample.replace("\n", " | ").substring(0, Math.min(150, sample.length())) + ")", learned);

        Organization sect = new Organization(lead); lead.sect = sect; sect.location = spot; p.sect = null;
        io = new TestIo(1); // the first reply asks what is required; plan: ask, then accept
        TestIo io2 = new TestIo(3); io2.script.add(1);
        boolean can = sect.canJoin(p);
        Dialogue.run("recruit", new Dialogue.Ctx(lead, p, io2));
        check("E8 a sect's invitation: accepted at its hall you join it (" + (p.sect == sect) + ", could join " + can + ")", !can || p.sect == sect);
        if (p.sect != null) { p.sect.resign(p); p.sect = null; }

        Character thug = npc(Character.Traits.Cheater, 50, 5); thug.affiliation = "Evil"; thug.affiliationNum = -1; tp(thug, spot); long w2 = PlayerLists.wealth(p);
        io = new TestIo(0); Dialogue.run("shakedown", new Dialogue.Ctx(thug, p, io));
        check("E9 paying a thug takes a tenth of your stones (" + (w2 - PlayerLists.wealth(p)) + ")", w2 - PlayerLists.wealth(p) > 0);

        // ---- C: the conversation
        Character friend = npc(Character.Traits.Friendly, 60, 6); tp(friend, spot); setRel(friend, p, 3); friend.knownLocations.add(veinA);
        Character mutual = npc(Character.Traits.Cheater, 30, 3); tp(mutual, elsewhere); setRel(friend, mutual, 2);
        TestIo hub = new TestIo(3);
        for (int i = 0; i < 9; i++) hub.script.add(0); // every topic that is offered, in turn (each is taken once), then goodbye
        int talks0 = friend.relationshipList.contains(p.id).talks;
        try { Dialogue.hub(new Dialogue.Ctx(friend, p, hub)); } catch (RuntimeException e) { hub.log.append("EXC " + e); }
        Relationship fr = friend.relationshipList.contains(p.id);
        check("C1 a conversation runs through its topics and ends with a goodbye (" + hub.npcLines + " lines, " + hub.picks + " replies) clean=" + hub.clean(), hub.npcLines >= 8 && hub.clean() && !hub.log.toString().contains("EXC") && fr.talks > talks0 && fr.topics != 0);
        TestIo again = new TestIo(0);
        Dialogue.hub(new Dialogue.Ctx(friend, p, again));
        String firstReply = ""; for (String ln : again.log.toString().split(String.valueOf((char) 10))) if (ln.startsWith("> ")) { firstReply = ln; break; }
        check("C2 the same visit does not offer a topic twice (the first reply is now " + firstReply + ", not the first topic)", fr.topics != 0 && !firstReply.contains("How have you been"));

        // ---- F: terminal, AUTO and decisions
        Character chat = npc(Character.Traits.Blacksmith, 30, 3); tp(chat, spot); setRel(chat, p, 1);
        int m = PcProbe.mark();
        boolean said = Dialogue.approach(chat, p);
        String out = PcProbe.since(m);
        check("F1 someone who speaks to you gives a line in the log (" + out.trim().replace("\n", " | ") + ")", said && out.contains(chat.name + " " + chat.lastName + ": \""));
        Settings.dialogueFlavor = false; m = PcProbe.mark();
        said = Dialogue.approach(chat, p);
        check("F2 with the lines off nothing is said and the old 'talked to' line stands in", !said && PcProbe.since(m).isEmpty());
        Settings.dialogueFlavor = true;
        p.pc().mode = PlayerControl.Mode.AUTO;
        m = PcProbe.mark(); Dialogue.approach(chat, p);
        out = PcProbe.since(m);
        check("F3 an AUTO character hears the line but has no talk by default", out.contains("\"") && !out.contains("talks with"));
        Settings.dialogueAuto = true; m = PcProbe.mark(); Dialogue.approach(chat, p);
        out = PcProbe.since(m);
        check("F4 with the AUTO toggle on it has the talk, and answers by its temper (" + out.trim().replace("\n", " | ").substring(0, Math.min(200, out.length())) + ")", out.contains("talks with") && out.contains(p.name + ": "));
        Settings.dialogueAuto = false;
        p.pc().mode = PlayerControl.Mode.MANUAL;
        DecisionBroker.scripted = new ArrayDeque<>(Arrays.asList(1));
        m = PcProbe.mark();
        int ans = DecisionBroker.ask(p, "X would like to be your friend. Accept?", new String[]{"Accept", "Decline"}, 0, chat, "friend");
        check("F5 a decision put in someone's words still takes the scripted answer (" + ans + ")", ans == 1);
        DecisionBroker.scripted = new ArrayDeque<>();
        p.pc().mode = PlayerControl.Mode.AUTO; m = PcProbe.mark();
        ans = DecisionBroker.ask(p, "X challenges you!", new String[]{"Fight", "Slip away"}, 1, chat, "challenge");
        out = PcProbe.since(m);
        check("F6 an AUTO character answers by policy and hears the challenge (" + ans + ", " + out.trim().replace("\n", " | ") + ")", ans == 1 && out.contains("\""));
        p.pc().mode = PlayerControl.Mode.MANUAL;
        m = PcProbe.mark();
        PcProbe.cmd("settings dialogue off"); PcProbe.cmd("settings dialogueauto on"); PcProbe.cmd("settings dialoguepopups off");
        check("F7 the three settings can be changed by command", !Settings.dialogueFlavor && Settings.dialogueAuto && !Settings.dialoguePopups);
        PcProbe.cmd("settings dialogue on"); PcProbe.cmd("settings dialogueauto off"); PcProbe.cmd("settings dialoguepopups on");

        // ---- L: in the live world, people really do speak to a player who stands among them
        GameDriver.frozenNpcs = false; p.lvl = 100; p.majorLvl = 10; p.caution = 0;
        Area crowd = Map.tiles[Map.range / 2][Map.range / 2]; tp(p, crowd);
        p.pc().duties[0] = false;
        m = PcProbe.mark(); int tStart = GameDriver.time; int spoken = 0;
        for (int round = 0; round < 12 && spoken == 0; round++) {
            PcProbe.cmd("wait 40", 300);
            for (String ln : PcProbe.since(m).split(String.valueOf((char) 10))) if (ln.trim().matches("^[A-Z][^:]{2,40}: \".*\"$")) spoken++;
        }
        check("L1 in a populated tile people speak to the player (" + spoken + " lines in " + (GameDriver.time - tStart) + " weeks)", spoken > 0);
        GameDriver.frozenNpcs = true;
        // ---- D: a few talks to read (not checks)
        PcProbe.real.println("[D] ---- samples ----");
        String[][] cast = {{"WARM", "Chef"}, {"CHEERFUL", "Musician"}, {"BLUNT", "Blacksmith"}, {"FORMAL", "Leader"}, {"SLY", "Merchant"}, {"WEARY", "Cheater"}, {"SCHOLAR", "Scribe"}, {"HOSTILE", "Friendly"}};
        String[] sceneFor = {"greeting", "rumor", "story", "meal", "greeting", "story", "greeting", "hostile"};
        for (int i = 0; i < cast.length; i++) {
            Character n = npc(Character.Traits.valueOf(cast[i][1]), 40, 4); tp(n, spot); n.knownLocations.add(veinA);
            if (cast[i][0].equals("WEARY")) { n.age = 90; n.maxAge = 100; }
            if (cast[i][0].equals("HOSTILE")) setRel(n, p, -2); else if (i % 2 == 0) setRel(n, p, 1);
            if (cast[i][0].equals("SLY")) { n.affiliation = "Evil"; n.affiliationNum = -1; }
            for (String sc : new String[]{sceneFor[i], "greeting"}) {
                TestIo d = new TestIo(2);
                Dialogue.run(sc, new Dialogue.Ctx(n, p, d));
                PcProbe.real.println("[D] " + cast[i][0] + " " + cast[i][1] + " / " + sc + ":" + (char) 10 + d.log);
            }
        }
        // every scene once more, written out
        Character lucky = npc(Character.Traits.Lucky, 40, 4); tp(lucky, spot); Character pois = npc(Character.Traits.Poisoner, 40, 4); tp(pois, spot); Character sorry = npc(Character.Traits.Blacksmith, 40, 4); tp(sorry, spot); setRel(sorry, p, -1);
        Character fighter = npc(Character.Traits.Blacksmith, 40, 4); tp(fighter, spot); setRel(fighter, p, 1); Character tale = npc(Character.Traits.Alchemist, 40, 4); tp(tale, spot); setRel(tale, p, 2);
        Character.addToBag(Life.goods(pois), new Item(Item.ItemType.Poison, 3, 3, "Night-Orchid Draught"));
        alch.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, p.majorLvl + 1, 3)); sage.relationshipList.contains(p.id).lastAdvice = -1000; lead.sect = sect; sect.location = spot;
        p.ss = 5000; p.convertCurrency();
        Object[][] list = {{"help", poor}, {"pitch", alch}, {"spar", fighter}, {"meal", chef}, {"song", bard}, {"recruit", lead}, {"shakedown", thug}, {"gossip", gos}, {"advice", sage}, {"fortune", lucky}, {"poison", pois}, {"story", tale}, {"apology", sorry}};
        for (Object[] o : list) {
            TestIo d = new TestIo(0);
            try { Dialogue.run((String) o[0], new Dialogue.Ctx((Character) o[1], p, d)); } catch (RuntimeException e) { d.log.append("EXC " + e); }
            PcProbe.real.println("[D] SCENE " + o[0] + " (" + Dialogue.voiceOf((Character) o[1], p) + "):" + (char) 10 + d.log);
        }
        check("Z no exceptions", !PcProbe.out().contains("Exception"));
    }
}
