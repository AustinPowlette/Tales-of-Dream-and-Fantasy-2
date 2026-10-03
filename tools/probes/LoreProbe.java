package Game;

import java.io.*;
import java.util.*;

/** The lore: the Palace at Originis, the Convocation, the chain of objectives, the places, ??? speaks, the crossing, saves, safety of the virtual sect. */
public class LoreProbe {
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }
    static void info(String s) { PcProbe.real.println("[T] info: " + s); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static String text(Runnable r) { int m = PcProbe.mark(); r.run(); return PcProbe.since(m); }
    static void script(Integer... answers) { DecisionBroker.scripted.clear(); DecisionBroker.scripted.addAll(Arrays.asList(answers)); }

    static Character adult(int lvl, int major) {
        Character c = new Character(home());
        c.age = 30; c.gender = "Male"; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000; c.caution = 50; c.sect = null;
        if (c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) c.trait = Character.Traits.Friendly;
        Map.characters.add(c); Map.population++;
        return c;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;
        GameDriver.time = Math.max(GameDriver.time, 60);

        // ---- L1: the Palace stands at Originis
        Lore.State s = Lore.state;
        check("L1 the lore state exists in a new world", s != null && Settings.lore);
        Organization pal = Lore.palace();
        check("L1b the Palace is the sect of Originis, virtual, with no master, named the same every time", pal != null && home().sect == pal && pal.isVirtual() && pal.master == null && pal.name.equals(LoreText.PALACE));
        Set<String> spots = new HashSet<>(); boolean inRange = true;
        for (int i = 0; i < LoreSites.ALL.length; i++) { spots.add(s.siteX[i] + "," + s.siteY[i]); if (s.siteX[i] < 0 || s.siteX[i] >= Map.range || s.siteY[i] < 0 || s.siteY[i] >= Map.range) inRange = false; }
        check("L1c nine distinct places on the map", spots.size() == LoreSites.ALL.length && inRange && !spots.contains((Map.range / 2) + "," + (Map.range / 2)));
        int[][] again = LoreSites.place(s.seed);
        boolean same = true; for (int i = 0; i < again.length; i++) if (again[i][0] != s.siteX[i] || again[i][1] != s.siteY[i]) same = false;
        check("L1d the places are a pure function of the world seed", same);
        pal.promote();
        check("L1e promote() never crowns a master of the Palace", pal.master == null && home().sect == pal);
        Organization other = new Organization(adult(50, 3));
        int wd = Organization.warsDeclared; pal.declareWar(other); other.declareWar(pal);
        check("L1f the Palace is never at war", !pal.isAtWar() && other.warTarget != pal && Organization.warsDeclared == wd);
        if (other.master != null) { other.master.sect = null; }
        Character outsider = adult(10, 1);
        check("L1g an outsider cannot just join the Palace", pal.whyNot(outsider) != null && !pal.canJoin(outsider));

        // ---- L2: the Convocation opens, the human registers, the Gate is judged
        GameDriver.time = 52 * 3;
        s.convocationOpen = false; s.pending = false; home().tournamentTimer = 0; home().tournamentRegistrants.clear(); // the game proclaimed the first one at the start: reset it
        Lore.yearly();
        Lore.worldTick();
        check("L2 the Convocation of the First Gate opens at Originis", s.convocationOpen && home().tournamentTimer > 0 && home().tournamentName != null && home().tournamentName.contains("Convocation"));
        p.lvl = 10; p.majorLvl = 1;
        Area east = Map.tiles[Map.range / 2 + 1][Map.range / 2];
        p.location.removeChar(p); p.location = east; east.addChar(p);
        int mk = PcProbe.mark();
        Lore.tick(p);
        Lore.PState ps = Lore.pstate(p);
        check("L2b away from Originis the human gets a lore state and ONE walk quest to the Palace", ps != null && ps.questUid >= 0 && ps.questLabel.contains("Register") && p.targetQuests.stream().filter(q -> q.name.startsWith("Palace:")).count() == 1);
        int qUid = ps.questUid; Lore.tick(p); Lore.tick(p);
        check("L2b2 the walk quest is stable (not re-created every turn)", ps.questUid == qUid);
        p.location.removeChar(p); p.location = home(); home().addChar(p);
        Lore.tick(p);
        check("L2b3 standing at Originis there is no Palace walk quest left", p.targetQuests.stream().noneMatch(q -> q.name.startsWith("Palace:")) && ps.questUid < 0);
        check("L2c the bell scene is shown at Originis the first time", ps.scenes.contains("bell"));
        home().tournamentRegistrants.add(p);
        Lore.tick(p);
        check("L2d registering is noticed (the register scene)", ps.registeredYear == s.convocationYear && ps.scenes.contains("register"));
        for (int i = 0; i < 15; i++) { Character c = adult(8 + i, 1); home().tournamentRegistrants.add(c); }
        boolean ran = Lore.runConvocation(home());
        check("L2e the Convocation is judged by the Palace", ran && !s.convocationOpen && s.held == 1 && !s.lastResult.isEmpty());
        check("L2f the human got a result and the pass list matches it", ps.result != null && (ps.result.equals("PASS") == s.passes.contains(p.id)));
        if (ps.result.equals("FAIL")) { // make the story go on: the Palace notices a stronger human
            p.majorLvl = 2; Lore.tick(p);
            check("L2g a human who crosses the second gate is invited", s.passes.contains(p.id));
        }
        s.passes.add(p.id); ps.result = "PASS"; ps.declined = false;
        script(); Lore.tick(p);
        check("L2h a passed human joins the Palace as an outer disciple (a Wick)", p.sect == pal && pal.getRank(p) == Organization.SectRank.OUTER_DISCIPLE && pal.members.contains(p));
        check("L2i every Palace member is in the member list and the sect stays valid", valid(pal) == null);

        // ---- L3: the chain
        Lore.tick(p);
        check("L3 the first objective after joining asks for the Wayhouse", Lore.CHAIN[ps.step].kind.equals("SITE") || ps.step >= 2);
        int w = ps.step;
        Lore.PState before = ps;
        // the Wayhouse tile exists once revealed and the human knows it
        int wi = LoreSites.index("wayhouse");
        Area way = Map.tiles[s.siteX[wi]][s.siteY[wi]];
        check("L3b the Wayhouse is revealed as a real tile the human knows", way != null && way.loreSite != null && way.loreSite.equals("wayhouse") && p.knownLocations.contains(way));
        p.location.removeChar(p); p.location = way; way.addChar(p);
        Lore.tick(p);
        check("L3c standing on the Wayhouse reads it (clue + reward)", ps.found.contains("wayhouse"));
        for (int i = 0; i < 12; i++) Lore.tick(p);
        check("L3d the scene queue empties one scene per turn", ps.queue.isEmpty() && ps.scenes.contains("site_wayhouse"));
        p.sectPoints = 40;
        Lore.tick(p); Lore.tick(p);
        check("L3e merit counts toward the next step", ps.peakPoints >= 40 && ps.step >= 4);
        p.majorLvl = 2; Lore.tick(p); Lore.tick(p);
        check("L3f Major 2 makes a Flame of the Inner Court (rank promoted, gear given)", pal.getRank(p) == Organization.SectRank.INNER_DISCIPLE && ps.scenes.contains("inner"));
        // the next places are revealed when their step starts; reading one of them with too low a realm is refused politely
        int wv = LoreSites.index("weaver");
        Area weave = Map.tiles[s.siteX[wv]][s.siteY[wv]];
        check("L3g the next clue places are revealed", weave != null && ps.revealed.contains("weaver"));
        p.location.removeChar(p); p.location = weave; weave.addChar(p); p.majorLvl = 0;
        String low = text(() -> Lore.tick(p));
        check("L3h a realm too low only glimpses the place", !ps.found.contains("weaver") && low.contains("Something is here"));
        p.majorLvl = 2;
        Lore.tick(p);
        check("L3i with the realm and the previous place read, the place is read", ps.found.contains("weaver"));

        // ---- L4: the rest of the road, all the way to the Door
        p.location.removeChar(p); p.location = home(); home().addChar(p);
        p.majorLvl = 5; p.sectPoints = 300; p.lvl = 60;
        for (String id : new String[]{"cistern", "assayer", "shed", "thinsky", "lanternroad", "menagerie", "garrison"}) {
            int i = LoreSites.index(id);
            Character keep = Map.currentCharacter; Map.currentCharacter = p; Area ar = LoreSites.ensureTile(s, i); Map.currentCharacter = keep;
            check("L4 place " + id + " can be created on demand", ar != null && ar.loreSite != null);
            ps.revealed.add(id);
        }
        for (int i = 0; i < 20; i++) Lore.tick(p);

        for (String id : new String[]{"cistern", "assayer", "shed", "thinsky", "lanternroad", "menagerie", "garrison"}) {
            int i = LoreSites.index(id);
            Area ar = Map.tiles[s.siteX[i]][s.siteY[i]];
            p.majorLvl = 9; p.location.removeChar(p); p.location = ar; ar.addChar(p);
            for (int k = 0; k < 4; k++) Lore.tick(p);
        }
        check("L4c all nine places can be read in order", ps.found.size() >= LoreSites.ALL.length);
        p.location.removeChar(p); p.location = home(); home().addChar(p); p.majorLvl = 9;
        for (int k = 0; k < 6; k++) Lore.tick(p);
        check("L4b a human with the realm and merit takes a lamp of the Nine (Elder)", pal.getRank(p) == Organization.SectRank.ELDER && ps.lampIdx >= 0 && s.lamps[ps.lampIdx] == p.id);
        p.location.removeChar(p); p.location = home(); home().addChar(p);
        for (int k = 0; k < 20; k++) Lore.tick(p);
        check("L4d the Empty Seat is read by an Elder who has read the nine", ps.found.contains(LoreSites.SEAT) && ps.scenes.contains("seat"));
        String why = Lore.whyNotCross(p, ps);
        check("L4e the Seam stays shut below level 101", why != null && why.contains("101"));
        p.majorLvl = 10; p.lvl = 101;
        for (int k = 0; k < 20; k++) Lore.tick(p);
        check("L4f every realm scene was told in time", ps.scenes.contains("m4") && ps.scenes.contains("m8") && ps.scenes.contains("door"));
        check("L4g the Seam is open at Originis at level 101 with everything read", Lore.whyNotCross(p, ps) == null);
        script(1); // "Not yet" at the Seam
        boolean no = Lore.cross(p);
        check("L4h answering 'not yet' keeps the story open", !no && !ps.ended);
        Character kid = adult(30, 1); kid.mother = p; p.children.add(kid); kid.potential = 50;
        Character grandkid = adult(10, 0); grandkid.mother = kid; kid.children.add(grandkid); grandkid.potential = 40;
        script(0, 1, 0, 0); // step through, Leave, lamp-bearer, at the front
        String endTxt = text(() -> { Lore.cross(p); });
        check("L4i the true ending: a soldier, the story told, the credits, the roll", ps.ended && endTxt.contains("CREDITS") && endTxt.contains("THE END"));
        check("L4i2 the ending is recorded: roll, score, hall of fame line", s.crossed == 1 && s.roll.size() == 1 && ps.score > 3000 && ps.endingId.equals("soldier") && RunStats.hallOfFame().contains("ENDING") && RunStats.hallOfFame().contains("SCORE"));
        check("L4j a human who crossed gets no more quest, only whispers", Lore.pstate(p).questUid < 0);
        check("L4k the soldier comes back as an unkillable Wandering Immortal and the player is released to choose another character", p.wanderingImmortal && s.wanderers.contains(p.id) && LoreEndings.releases.contains(p) && p.deathCause.startsWith("became a lamp-bearer"));
        p.dead(); p.dead(p);
        check("L4l a Wandering Immortal cannot die", !p.dead);
        check("L4m the soldier's descendants are blessed: +5 per soldier in the line (potential and strength)", kid.blessing == 5 && grandkid.blessing == 5 && kid.potential == 55 && grandkid.potential == 45);
        kid.wanderingImmortal = true; LoreEndings.bless(kid);
        check("L4n a descendant who also becomes a soldier adds another +5 for the next generation", grandkid.blessing == 10 && grandkid.potential == 50);
        kid.wanderingImmortal = false;
        kid.tempLvlCheck(); int t5 = kid.tempLvl; kid.blessing = 0; kid.tempLvlCheck();
        check("L4o the blessing adds exactly +5 to the fighting level (tempLvl) per soldier in the line", t5 - kid.tempLvl == 5);
        LoreEndings.releases.remove(p);

        // ---- L5: NPC members and the yearly Palace
        Character np = adult(70, 4); np.sect = null;
        s.passes.add(np.id); pal.join(np);
        check("L5 an NPC with a pass joins the Palace", np.sect == pal);
        Character npc101 = adult(101, 10); s.passes.add(npc101.id); pal.join(npc101);
        if (s.seekers == null) s.seekers = new HashMap<>();
        s.seekers.put(npc101.id, 511); // a seeker who has read all nine
        long ss0 = np.ss;
        int cr0 = s.crossed;
        Lore.yearly();
        check("L5b a seeker at level 101 who read the nine places opens the Door (soldier, left or struck down) and is on the Roll", s.crossed == cr0 + 1 && !s.seekers.containsKey(npc101.id) && (npc101.dead || npc101.wanderingImmortal || (s.stayers != null && s.stayers.containsKey(npc101.id))));
        check("L5c members are paid a stipend and an inner disciple of Major 4 can be seated at a lamp", np.ss > ss0);
        check("L5d the Palace stays valid after the yearly", valid(pal) == null);
        int om0 = s.omens;
        GameDriver.time = 52 * 400;
        for (int i = 0; i < 80; i++) Lore.omens(400);
        check("L5e omens come now and then (and never break anything)", s.omens > om0);

        // ---- L6: commands
        Character q = adult(20, 1); q.player = 1; q.pc().mode = PlayerControl.Mode.MANUAL;
        String outc = "";
        for (String cmd : new String[]{"lore", "lore clues", "lore where", "lore palace", "lore scenes", "lore replay bell", "lore replay nonsense", "lore register", "lore join", "lore cross", "lore bogus", "lore journal"}) {
            final String cc = cmd; Character qq = q;
            String o = text(() -> PlayerCommands.handle(qq, cc));
            outc += o;
            check("L6 '" + cmd + "' answers without an exception", !o.contains("Exception") && !o.contains("at Game."));
        }
        check("L6b 'lore' mentions the Order", outc.contains("Order") || outc.contains("Palace"));
        q.player = 0; q.pc = null;

        // ---- L7: saves
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(bo)) { oo.writeObject(s); }
        Lore.State back = (Lore.State) new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray())).readObject();
        check("L7 the lore state round-trips through serialization", back.seed == s.seed && back.crossed == s.crossed && back.humans.size() == s.humans.size() && Arrays.equals(back.siteX, s.siteX) && back.humans.get(p.id).ended);
        SaveData d = new SaveData(); Lore.snapshotInto(d);
        Lore.State keep = Lore.state; Lore.state = null; Lore.restoreFrom(d);
        check("L7b snapshot/restore through SaveData", Lore.state == keep);

        // ---- L8: off switch and old worlds
        Settings.lore = false;
        int sc0 = Lore.shown.size();
        Character z = adult(10, 1); z.player = 1; z.pc().mode = PlayerControl.Mode.MANUAL;
        Lore.tick(z);
        check("L8 with the lore off nothing runs", Lore.shown.size() == sc0 && Lore.pstate(z) == null && !Lore.active());
        Settings.lore = true; z.player = 0; z.pc = null;
        Lore.State saved = Lore.state; Lore.state = null;
        Character y = adult(10, 1); y.player = 1; y.pc().mode = PlayerControl.Mode.MANUAL;
        Lore.tick(y); Lore.worldTick(); Lore.yearly();
        String eo = text(() -> PlayerCommands.handle(y, "lore"));
        check("L8b an old world (no lore state) is untouched and 'lore' explains how to start it", eo.contains("lore enable") && Lore.pstate(y) == null);
        Lore.state = saved; y.player = 0; y.pc = null;

        // ---- L9: the sect machinery with a master-less sect
        boolean ok9 = true; String err9 = "";
        try {
            Seasonal.diplomacy(); SectPolitics.yearly(Collections.singletonList(pal)); pal.postMissionsYearly(); pal.growPopulation();
            for (int i = 0; i < 10; i++) SectPolitics.seasonal(i % 4);
        } catch (RuntimeException e) { ok9 = false; err9 = e.toString(); }
        check("L9 diplomacy, sect politics and missions run on the master-less Palace without errors " + err9, ok9);
        check("L9b the Palace is still valid and still has no master", valid(pal) == null && pal.master == null);
        int cap = pal.memberCap();
        check("L9c its member cap does not need a master", cap >= 20);

        // ================================================================== E: the endings
        Settings.lore = true;
        endings(s);
    }

    static final HashMap<Integer, Lore.PState> keep = new HashMap<>();
    static Character aligned(int lvl, int a) { Character c = adult(lvl, 1); LoreEndings.setAlign(c, a); return c; }
    static Character ready(Lore.State s) {
        Organization pal = Lore.palace();
        for (java.util.Iterator<java.util.Map.Entry<Integer, Lore.PState>> it = s.humans.entrySet().iterator(); it.hasNext(); ) { java.util.Map.Entry<Integer, Lore.PState> e = it.next(); Character b = Character.charById(e.getKey()); if (b == null || b.dead || b.player != 1 || e.getValue().ended) { keep.put(e.getKey(), e.getValue()); it.remove(); } } // a finished story is not handed to the next test human
        Character h = adult(101, 10); h.player = 1; h.pc().mode = PlayerControl.Mode.MANUAL; h.potential = 100; h.sect = null;
        s.passes.add(h.id); pal.join(h);
        if (h.sect != pal) { pal.members.add(h); h.sect = pal; pal.memberRanks.put(h.id, Organization.SectRank.ELDER); }
        Lore.tick(h);
        Lore.PState q = Lore.pstate(h);
        for (LoreSites.Site si : LoreSites.ALL) { q.found.add(si.id); q.revealed.add(si.id); }
        q.found.add(LoreSites.SEAT); q.everMember = true; q.queue.clear();
        return h;
    }
    static void freshPalace(Lore.State s) { Organization pal = Lore.palace(); if (pal != null) { pal.master = null; pal.virtual = true; for (java.util.Map.Entry<Integer, Organization.SectRank> e : pal.memberRanks.entrySet()) if (e.getValue() == Organization.SectRank.MASTER) e.setValue(Organization.SectRank.ELDER); } s.silenced = false; s.gardenDestroyed = false; LoreEndings.worldEnded = false; }
    static void unhumanize(Character h) { if (h.sect != null && !h.dead) h.sect.resign(h); h.player = 0; h.pc = null; }

    static void endings(Lore.State s) throws Exception {
        Organization pal = Lore.palace();
        Area home = home();
        PcProbe.real.println("[T] ---- E: the endings ----");
        boolean allScenes = true; String missing = "";
        for (String id : new String[]{"end_threshold", "end_oldman", "end_oldman_full", "end_oldman_stay", "end_steward", "end_usurper", "end_ascendant", "end_idler", "end_fade", "end_fade_slain", "end_fade_door", "end_dark", "end_soldier", "end_soldier_front", "end_soldier_guard", "end_soldier_keep", "end_soldier_war", "end_attack", "end_attack_whole", "end_attack_dead", "end_attack_spared", "end_leave"}) if (LoreText.SCENES.get(id) == null || LoreText.lines(id, Lore.vars(adult(5, 0), null)).isEmpty()) { allScenes = false; missing += id + " "; }
        for (String id : LoreEndings.OUT_IDS) if (LoreText.SCENES.get(id) == null) { allScenes = false; missing += id + " "; }
        check("E0 every ending scene and every Outside branch has a story stub " + missing, allScenes && LoreEndings.OUT_IDS.length >= 12);

        // 1: the Steward
        Character h = ready(s); script(0, 0, 0);
        String t1 = text(() -> Lore.cross(h));
        Lore.PState q = Lore.pstate(h);
        check("E1 Stay (Good): the Steward becomes master of the Palace, Good, with credits and a score", pal.master == h && !pal.virtual && h.affiliation.equals("Good") && q.stewardActive && q.endingId.equals("steward") && q.ended && t1.contains("CREDITS") && t1.contains("OLD MAN") && q.score > 1500);
        Character poor = adult(20, 0); poor.majorLvl = 2; Character cruel = aligned(30, -1); cruel.kills = 9;
        GameDriver.rand.setSeed(7);
        LoreEndings.stewardYearly(s, pal);
        check("E1b the Steward's year runs (gifts and a cull) and the Palace stays valid", valid(pal) == null);
        freshPalace(s); unhumanize(h);

        // 2: the Usurper, three ways here (the fourth, razing, is the last test)
        Character gm = aligned(30, 1); s.passes.add(gm.id); pal.join(gm);
        Character h2 = ready(s); script(0, 0, 1, 0); Lore.cross(h2);
        PcProbe.real.println("[T] info: E2 master=" + (pal.master == null ? "null" : pal.master.name) + " h2=" + h2.name + " aff=" + h2.affiliation + " dark=" + Lore.pstate(h2).darkPath + " gmSect=" + (gm.sect == null ? "null" : gm.sect.name) + " ending=" + Lore.pstate(h2).endingId + " gmAff=" + gm.affiliationNum + " gmPlayer=" + gm.player + " inMembers=" + pal.members.contains(gm) + " gmDead=" + gm.dead);
        check("E2 Stay (Evil), take the Palace: master, Evil, the Good members are killed, the dark path opens", pal.master == h2 && h2.affiliation.equals("Evil") && Lore.pstate(h2).darkPath && gm.dead);
        freshPalace(s); unhumanize(h2);
        Character h3 = ready(s); script(0, 0, 1, 2); Lore.cross(h3);
        check("E2b Stay (Evil), your own sect: leaves the Palace, Evil", h3.sect == null && h3.affiliation.equals("Evil") && Lore.pstate(h3).darkPath && Lore.pstate(h3).endingId.equals("usurper2"));
        unhumanize(h3);
        Character h4 = ready(s); script(0, 0, 2, 1); Lore.cross(h4);
        check("E2c Stay (Neutral), your own way: leaves the Palace, Neutral", h4.sect == null && h4.affiliation.equals("Neutral") && Lore.pstate(h4).endingId.equals("idler1"));
        unhumanize(h4);

        // 3: the Idler: the Garden goes out after 150 years
        Character h5 = ready(s); script(0, 0, 2, 0); Lore.cross(h5);
        final Lore.PState q5 = Lore.pstate(h5);
        check("E3 Stay (Neutral), take the Palace lazily: master, Neutral, the chores are on, waiting for the end of the world (??? lives for ever until then)", pal.master == h5 && h5.affiliation.equals("Neutral") && h5.pc().dutiesOn() == 10 && q5.idleUntil == Integer.MAX_VALUE);
        LoreEndings.worldEnded = false;
        script(1);
        String t5 = text(() -> LoreEndings.worldEnd());
        check("E3a the end of the world saves and ends the game (no overflow)", GameDriver.quitRequested && LoreEndings.worldEnded);
        GameDriver.quitRequested = false; GameDriver.saveRequested = false; LoreEndings.worldEnded = false;
        check("E3b ??? dies, the lamps go out: the Last Lamp ending, the story falls silent", q5.endingId.equals("last_lamp") && s.silenced && t5.contains("LAST LAMP") && t5.contains("LIGHT FADES"));
        Lore.worldTick(); Lore.yearly();
        check("E3c a silenced story runs no more Convocations or omens (and breaks nothing)", s.silenced && valid(pal) == null);
        freshPalace(s); unhumanize(h5);
        Character h6 = ready(s); script(0, 0, 2, 1); Lore.cross(h6);
        script(0);
        LoreEndings.worldEnd();
        GameDriver.quitRequested = false; GameDriver.saveRequested = false; LoreEndings.worldEnded = false;
        check("E3d anyone who stayed may step through when the Garden goes out: a random Outside branch (the world is ending, so no succession dialog)", !h6.dead && Lore.pstate(h6).endingId.startsWith("last_out_") && h6.deathCause.startsWith("left the Garden"));
        freshPalace(s); unhumanize(h6);
        Character hq = ready(s); hq.lvl = 50; hq.majorLvl = 4; script();
        String tq = text(() -> LoreEndings.worldEnd());
        GameDriver.quitRequested = false; GameDriver.saveRequested = false; LoreEndings.worldEnded = false;
        check("E3e a human who could not cross just goes out with the world: no choice, the light fades", Lore.pstate(hq).endingId.equals("last_lamp") && tq.contains("LIGHT FADES") && !tq.contains("SEAM IS OPEN"));
        freshPalace(s); unhumanize(hq);

        // 4: leaving alone, and trying to kill ???
        Set<String> outs = new HashSet<>(); boolean allLeftClean = true;
        for (int i = 0; i < 40; i++) {
            Character hx = ready(s); script(0, 1, 2); Lore.cross(hx);
            Lore.PState qx = Lore.pstate(hx);
            outs.add(qx.endingId);
            allLeftClean &= hx.dead && hx.needsReincarnation && qx.endingId.startsWith("leave_neutral_") && qx.ended;
            unhumanize(hx);
        }
        check("E4 Leave alone (Neutral): the body leaves for a random branch of the Outside, every time (" + outs.size() + " branches in 40 tries)", allLeftClean && outs.size() >= 4);
        int dead = 0, spared = 0;
        for (int i = 0; i < 60; i++) {
            Character hx = ready(s); script(0, 1, 1); Lore.cross(hx);
            Lore.PState qx = Lore.pstate(hx);
            if (qx.endingId.equals("leave_evil_dead")) { dead++; if (!hx.dead || !hx.deathCause.startsWith("struck down")) dead += 1000; }
            else if (qx.endingId.startsWith("leave_evil_spared_")) { spared++; if (!hx.dead || !hx.needsReincarnation) spared += 1000; }
            unhumanize(hx);
        }
        check("E5 Leave (Evil): the attempt on ??? fails; about half are struck down, the rest spared and sent to the Outside (" + dead + " / " + spared + ")", dead > 10 && dead < 1000 && spared > 10 && spared < 1000);

        // 5: the hidden ending
        Character h7 = ready(s); script(0, 0, 1, 2); Lore.cross(h7);
        final Lore.PState q7 = Lore.pstate(h7);
        Character goodVic = aligned(20, 1); Character evilVic = aligned(20, -1); int pot0 = h7.potential;
        LoreEndings.onKill(h7, evilVic);
        PcProbe.real.println("[T] info: E6 pot0=" + pot0 + " now=" + h7.potential + " evilAff=" + evilVic.affiliationNum + " goodAff=" + goodVic.affiliationNum + " dark=" + q7.darkPath + " secret=" + q7.secretDone + " player=" + h7.player + " ending=" + q7.endingId);
        check("E6 the dark path: killing the cruel gives nothing", h7.potential == pot0);
        LoreEndings.onKill(h7, goodVic);
        check("E6b killing a Good cultivator on the dark path lifts the potential past 100", h7.potential == pot0 + 1);
        h7.potential = 199;
        String refused = text(() -> LoreEndings.usurp(h7));
        check("E6c below potential 200 the Seat is not warm enough", !q7.secretDone && refused.contains("not warm enough"));
        h7.potential = 200; h7.location.removeChar(h7); h7.location = home; home.addChar(h7);
        String t7 = text(() -> LoreEndings.usurp(h7));
        check("E6d potential 200 at the Empty Seat: the hidden ending (the long story, the credits, a big score), ??? is gone", q7.secretDone && s.silenced && q7.endingId.startsWith("ascendant_") && q7.score > 6000 && s.gardenDestroyed && GameDriver.quitRequested && t7.contains("PAST THE CEILING") && t7.contains("CREDITS"));
        check("E6e the hidden ending is in the hall of fame", RunStats.hallOfFame().contains("potential 200"));
        GameDriver.quitRequested = false; GameDriver.saveRequested = false; LoreEndings.worldEnded = false;
        s.silenced = false; freshPalace(s); unhumanize(h7);
        Character other = ready(s);
        check("E6f an ordinary human cannot use the hidden ending", !LoreEndings.usurp(other));
        unhumanize(other);

        // 6: seekers (NPCs the Convocation lets in walk the road in the background)
        s.convocationOpen = true; home.tournamentRegistrants.clear();
        for (int i = 0; i < 24; i++) { Character c = adult(10 + i, 0); c.majorLvl = 1; c.sect = null; c.age = 20; home.tournamentRegistrants.add(c); }
        Lore.runConvocation(home);
        check("E7 NPCs who win a place at the Convocation become seekers", s.seekers != null && !s.seekers.isEmpty());
        Character seeker = null; for (Integer id : s.seekers.keySet()) { Character c = Character.charById(id); if (c != null && !c.dead && c.sect == pal) { seeker = c; break; } }
        if (seeker != null) {
            seeker.majorLvl = 10; seeker.lvl = 90; final Integer sid = seeker.id;
            for (int y = 0; y < 150 && s.seekers.containsKey(sid) && Integer.bitCount(s.seekers.get(sid)) < LoreSites.ALL.length; y++) LoreEndings.seekersYearly(s, pal);
            check("E7b a seeker reads the places in order", !s.seekers.containsKey(sid) || Integer.bitCount(s.seekers.get(sid)) >= LoreSites.ALL.length);
            check("E7c the first reader of a place is in the Chronicle", s.firstRead != null && !s.firstRead.isEmpty() && Chronicle.search("first to read", "world", 20).size() > 0);
        } else check("E7b a seeker exists", false);

        // 7: the Giant Footprints
        LoreEndings.ensureFeet(s);
        boolean feetOk = s.footX != null && s.footX.length == 6; Set<String> fs = new HashSet<>();
        int ti = LoreSites.index("thinsky"), gi = LoreSites.index("garrison");
        for (int i = 0; feetOk && i < 6; i++) {
            fs.add(s.footX[i] + "," + s.footY[i]);
            int bi = i < 3 ? ti : gi;
            int dx = Math.min(Math.abs(s.footX[i] - s.siteX[bi]), Map.range - Math.abs(s.footX[i] - s.siteX[bi])), dy = Math.min(Math.abs(s.footY[i] - s.siteY[bi]), Map.range - Math.abs(s.footY[i] - s.siteY[bi]));
            if (Math.max(dx, dy) < 2 || Math.max(dx, dy) > 5) feetOk = false;
            if (s.footX[i] == Map.range / 2 && s.footY[i] == Map.range / 2) feetOk = false;
        }
        check("E8 six Giant Footprints, distinct, three near the Thin Sky and three near the Garrison", feetOk && fs.size() == 6);
        final Character hf = ready(s); final Lore.PState qf = Lore.pstate(hf);
        Area fa = Map.tiles[s.footX[0]][s.footY[0]];
        if (fa == null) { Character keep = Map.currentCharacter; Map.currentCharacter = hf; int fx = s.footX[0], fy = s.footY[0]; Map.tiles[fx][fy] = new Area(Area.ghost(fx == 0 ? Map.range - 1 : fx - 1, fy), 'e'); Map.currentCharacter = keep; fa = Map.tiles[fx][fy]; }
        LoreSites.applyFoot(s, fa);
        check("E8b the tile takes the footprint's name", fa.loreSite != null && fa.loreSite.equals("foot0") && fa.name.equals(LoreSites.FOOT_NAMES[0]));
        hf.location.removeChar(hf); hf.location = fa; fa.addChar(hf); hf.majorLvl = 2;
        LoreEndings.tick(hf, qf);
        check("E8c a Major 2 reader only glimpses it", qf.prints == null || qf.prints.isEmpty());
        hf.majorLvl = 4; int bag0 = hf.backpack.size();
        String tf = text(() -> LoreEndings.tick(hf, qf));
        boolean relic = false; for (Item it : hf.backpack) if (it.name != null && it.name.startsWith("Relic of the Footprint")) relic = true;
        check("E8d standing on it at Major 3+ reads the clue and yields a rare relic", qf.prints.contains("foot0") && relic && tf.contains("Heel") && hf.backpack.size() == bag0 + 1);
        LoreEndings.tick(hf, qf);
        check("E8e a print gives its relic only once", hf.backpack.size() == bag0 + 1);
        hf.location.removeChar(hf); hf.location = home; home.addChar(hf); unhumanize(hf);

        // 8: Wandering Immortals give hints and gifts every turn
        final Character wi = adult(101, 10); wi.wanderingImmortal = true; if (s.wanderers == null) s.wanderers = new ArrayList<>(); s.wanderers.add(wi.id);
        final Character met = ready(s); met.location.removeChar(met); wi.location.addChar(met); met.location = wi.location; met.skipping = 0;
        String tw = text(() -> LoreEndings.wanderTurn(wi));
        check("E9 a Wandering Immortal gives a human a pill and a hint every turn", tw.contains("Wandering Immortal") && !met.pillBag.isEmpty());
        wi.dead(); check("E9b a Wandering Immortal does not die", !wi.dead);
        unhumanize(met);

        // 9: credits
        Character hc = ready(s);
        List<String> cr = LoreEndings.credits(hc, Lore.pstate(hc), "Test Ending", 1234, Arrays.asList("TAIL LINE"));
        check("E10 the credits are funny, name the player, carry the score and the tail, and end with THE END", cr.size() > 25 && cr.contains("Score: 1234") && cr.contains("TAIL LINE") && cr.get(cr.size() - 1).startsWith("THE END") && String.join("\n", cr).contains(hc.name));
        unhumanize(hc);

        // 10: save/load of everything new
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(bo)) { oo.writeObject(new Object[]{s, q7, qf}); }
        Object[] back = (Object[]) new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray())).readObject();
        Lore.State bs = (Lore.State) back[0]; Lore.PState b7 = (Lore.PState) back[1], bf = (Lore.PState) back[2];
        check("E11 the new state saves and loads (seekers, wanderers, footprints, silence, prints, scores)", bs.seekers.size() == s.seekers.size() && bs.wanderers.size() == s.wanderers.size() && Arrays.equals(bs.footX, s.footX) && b7.score == q7.score && b7.darkPath && bf.prints.contains("foot0"));

        // 10b: bounties on the enemies of the Usurper and the Steward
        Character hd = ready(s); script(0, 0, 1, 2); Lore.cross(hd);
        for (int i = 0; i < 12; i++) { Character gv = aligned(20 + i, 1); gv.location = home; }
        int bounties0 = 0; for (Area ar : Map.cityAreas) bounties0 += ar.bounties.size();
        LoreEndings.huntYearly(s);
        int bounties1 = 0; for (Area ar : Map.cityAreas) bounties1 += ar.bounties.size();
        check("E11b a Usurper puts bounties on Good cultivators on the city boards (any sect member may take them)", Map.cityAreas.isEmpty() || bounties1 > bounties0);
        unhumanize(hd);

        // 10c: the Old Man, a Stay undone, the nine lamps, the blood, NPCs who make the same choices
        Character hs = ready(s); script(0, 0, 0);
        String ts = text(() -> Lore.cross(hs));
        final Lore.PState qs = Lore.pstate(hs);
        check("E13 the Old Man explains (the Gardener, the array, the Hollow Court) before the choice, and the Steward stays", ts.contains("OLD MAN") && ts.contains("array") && qs.metOldMan && qs.endingId.equals("steward") && LoreEndings.stayedOf(qs));
        check("E13b a Stay can be undone: the objective says how", Lore.objective(hs, qs).contains("lore leave"));
        script(2);
        boolean left = Lore.command(hs, new String[]{"lore", "leave"});
        check("E13c 'lore leave' after a Stay opens the Seam from anywhere: the Palace goes back to its image, the chores stop, and leaving is final", left && qs.endingId.startsWith("leave_neutral_") && !qs.stayed && !qs.stewardActive && pal.master == null && pal.virtual && hs.dead);
        script(0, 1, 2);
        check("E13d once left, the choice is final", !Lore.cross(hs));
        freshPalace(s); unhumanize(hs);
        List<Integer> backup = new ArrayList<>(); if (s.wanderers == null) s.wanderers = new ArrayList<>(); backup.addAll(s.wanderers);
        while (s.wanderers.size() < LoreEndings.LAMP_MAX) s.wanderers.add(-1000 - s.wanderers.size());
        check("E14 nine lamps carried: the array is whole", LoreEndings.lampsFull(s));
        Character hl = ready(s); script(0, 1, 0);
        String tl = text(() -> Lore.cross(hl));
        check("E14b with every lamp carried the soldier ending is closed", !Lore.pstate(hl).ended && tl.contains("no lamp left") && !hl.wanderingImmortal);
        unhumanize(hl);
        int fullDead = 0, blood0 = s.blood;
        for (int i = 0; i < 25; i++) { Character hx = ready(s); script(0, 1, 1); Lore.cross(hx); if (Lore.pstate(hx).endingId.equals("leave_evil_dead")) fullDead++; unhumanize(hx); }
        check("E14c once every lamp is carried an attack on the Gardener always ends in death (" + fullDead + " / 25)", fullDead == 25 && s.blood == blood0 + 125);
        Character cit = adult(10, 0); cit.potential = 40; int b0 = s.blood;
        LoreEndings.bloodSacrifice("Test"); LoreEndings.bloodSacrifice("Test");
        check("E14d blood: +5 potential to everyone alive per sacrifice, stacking, never past 100; later births get the bonus too", cit.potential == 50 && s.blood == b0 + 10 && LoreEndings.bloodBonus() == s.blood);
        Character hz = ready(s); script(0, 0, 1, 2); Lore.cross(hz); hz.potential = 200; hz.location.removeChar(hz); hz.location = home; home.addChar(hz);
        String tz = text(() -> LoreEndings.usurp(hz));
        check("E14e once every lamp is carried the hidden ending is locked: the Gardener is too strong", !Lore.pstate(hz).secretDone && tz.contains("array is whole"));
        unhumanize(hz);
        s.wanderers.clear(); s.wanderers.addAll(backup); s.blood = 0;
        int stayN = 0, soldN = 0, outN = 0, attN = 0; int blood1 = s.blood; List<Character> npcs = new ArrayList<>();
        if (s.seekers == null) s.seekers = new HashMap<>();
        for (int i = 0; i < 90; i++) {
            Character nx = adult(101, 10); nx.sect = null; nx.affiliationNum = (i % 3) - 1; npcs.add(nx);
            s.seekers.put(nx.id, 1023);
            LoreEndings.npcCross(s, nx);
            if (nx.wanderingImmortal) soldN++; else if (s.stayers != null && s.stayers.containsKey(nx.id)) stayN++; else if (nx.deathCause != null && nx.deathCause.startsWith("struck down")) attN++; else outN++;
        }
        check("E15 NPC seekers make every choice (" + stayN + " stay, " + soldN + " lamp-bearers, " + attN + " struck down, " + outN + " walked out) and never fill more than nine lamps", stayN > 0 && soldN > 0 && attN > 0 && outN > 0 && LoreEndings.lampsCarried(s) <= LoreEndings.LAMP_MAX && soldN <= LoreEndings.LAMP_MAX - backup.size());
        check("E15b an NPC who died striking the Gardener sent their blood back", s.blood >= blood1 + 5 && s.blood % 5 == 0);
        LoreEndings.stayersYearly(s); LoreEndings.huntYearly(s);
        check("E15c the stayers' year runs without errors", valid(pal) == null);
        for (Character nx : npcs) nx.wanderingImmortal = false;
        s.wanderers.clear(); s.wanderers.addAll(backup); s.blood = 0; if (s.stayers != null) s.stayers.clear();

        // 11: the Palace razed (last: it is gone)
        Character h8 = ready(s); script(0, 0, 1, 1); Lore.cross(h8);
        check("E12 Stay (Evil), destroy the Palace: the sect is gone from Originis", Lore.palace() == null && home.sect == null && s.palaceGone && h8.sect == null);
        Lore.worldTick(); Lore.yearly(); Lore.tick(h8);
        check("E12b with the Palace gone the story machinery runs on without errors", true);
    }

    static String valid(Organization s) {
        for (Character c : s.members) if (c != null && (c.dead || c.sect != s)) return "stale member " + (c == null ? "null" : c.name);
        for (java.util.Map.Entry<Integer, Organization.SectRank> e : s.memberRanks.entrySet()) {
            Character c = Character.charById(e.getKey());
            if (c == null || c.dead) return "dangling rank id " + e.getKey();
            if (c.sect != s) return "rank entry for a non-member " + c.name;
        }
        return null;
    }
}
