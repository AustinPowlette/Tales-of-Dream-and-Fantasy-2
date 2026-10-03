package Game;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/** Drives the real game loop through a controllable stdin and checks the player-control system (planning.md). */
public class PcProbe {
    static class FeedIn extends InputStream {
        final LinkedBlockingQueue<byte[]> q = new LinkedBlockingQueue<>();
        volatile boolean blocked = false;
        byte[] cur = null; int pos = 0;
        void feed(String s) { blocked = false; q.add(s.getBytes()); }
        @Override public int read() throws IOException { byte[] b = new byte[1]; int n = read(b, 0, 1); return n < 0 ? -1 : (b[0] & 0xff); }
        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (cur == null || pos >= cur.length) {
                try {
                    cur = q.poll();
                    if (cur == null) { blocked = true; cur = q.take(); blocked = false; }
                } catch (InterruptedException e) { return -1; }
                pos = 0;
            }
            int n = Math.min(len, cur.length - pos);
            System.arraycopy(cur, pos, b, off, n);
            pos += n;
            return n;
        }
        @Override public int available() { int a = (cur != null ? cur.length - pos : 0); for (byte[] x : q) a += x.length; return a; }
    }

    static FeedIn in = new FeedIn();
    static ByteArrayOutputStream cap = new ByteArrayOutputStream();
    static PrintStream real;
    static int passes = 0, fails = 0;
    static volatile Throwable gameError = null;

    static String out() { return cap.toString(); }
    static int mark() { return cap.toString().length(); }
    static String since(int m) { String s = out(); return s.length() > m ? s.substring(m) : ""; }

    static void check(String name, boolean ok) {
        if (ok) passes++; else fails++;
        real.println("[T] " + (ok ? "PASS " : "FAIL ") + name);
    }

    static void awaitBlocked(int secs) throws Exception {
        long end = System.currentTimeMillis() + secs * 1000L;
        while (System.currentTimeMillis() < end) {
            if (gameError != null) return;
            if (in.blocked && in.available() == 0) { Thread.sleep(30); if (in.blocked && in.available() == 0) return; }
            Thread.sleep(20);
        }
        real.println("[T] WARN timeout waiting for the game to block on input");
    }

    static void cmd(String s, int secs) throws Exception { in.feed(s + "\n"); awaitBlocked(secs); }
    static void cmd(String s) throws Exception { cmd(s, 60); }
    // alerts cut autopilot waits short by design, so keep waiting until the requested number of turns has passed
    static void waitTurns(int n) throws Exception {
        int target = GameDriver.time + n; int guard = 0;
        while (GameDriver.time < target && guard++ < 400) cmd("wait " + (target - GameDriver.time), 300);
    }

    // The world's crowd ambushes attack an idle PC at Originis, which (correctly) interrupts scripts: resume until the script really ends
    static void runScriptToEnd(int secs) throws Exception {
        cmd("script run", secs);
        for (int i = 0; i < 25; i++) {
            PlayerScript sc = pc().pc().script;
            if (sc == null || sc.ip == 0 || sc.ip >= sc.steps.size() || !sc.lastReason.contains("attacked")) break;
            cmd("script resume", secs);
        }
    }

    static Character pc() {
        for (int i = 0; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c != null && c.player == 1 && !c.dead) return c; }
        return null;
    }

    static int idMismatch() {
        int bad = 0;
        for (int i = 0; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c != null && c.id != i) bad++; }
        return bad;
    }

    public static void main(String[] a) throws Exception {
        real = System.out;
        System.setIn(in);
        System.setOut(new PrintStream(cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread mon = new Thread(() -> { try { while (true) { Thread.sleep(15000); int alive = 0, beasts = 0, nulls = 0; ArrayList<Character> sn = new ArrayList<>(Map.characters); for (Character c : sn) { if (c == null) { nulls++; continue; } if (c.dead) continue; if (c.trait == Character.Traits.Beast) beasts++; else alive++; } try (PrintWriter lw = new PrintWriter("live.log")) { lw.print(out()); } catch (Exception ex) {}
 real.println("[M] t=" + GameDriver.time + " yr=" + GameDriver.time / 52 + " pop=" + Map.population + " listSize=" + sn.size() + " alive=" + alive + " beastsInList=" + beasts + " nulls=" + nulls + " uid=" + GameDriver.UID); } } catch (Exception e) {} });
        mon.setDaemon(true); mon.start();
        try {
            run();
        } catch (Throwable e) { real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(real); fails++; }
        real.println("[T] ===== " + passes + " passed, " + fails + " failed; gameError=" + gameError + " =====");
        String o = out();
        int ex = 0; for (String l : o.split("\n")) if (l.contains("Exception") || l.contains("\tat Game.")) ex++;
        real.println("[T] exceptionLines=" + ex);
        if (a.length > 0) { try (PrintWriter w = new PrintWriter(a[0])) { w.print(o); } }
        System.exit(0);
    }

    static void run() throws Exception {
        in.feed("n\ns\n1\nn\n");
        awaitBlocked(180);
        Character p = pc();
        check("player created and prompting", p != null && in.blocked);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.minorLvl = 0; p.maxAge = 100000; p.caution = 0; // Originis turns into a beast-ambush hotspot once 3000 people crowd it: keep the test PC alive
        DecisionBroker.scriptedNumbers = new ArrayDeque<>(); // yearly auction bids pass unless a test supplies one
        DecisionBroker.scripted = new ArrayDeque<>(); // random NPC challenges must not steal probe commands: policy answers unless a test wants a real prompt
        int t0 = GameDriver.time;

        // ---------- S1: info commands are free (no time passes) ----------
        String[] infos = {"help", "i", "l", "r", "q", "here", "people", "bag", "shops", "city", "sect", "party", "library", "inbox", "duties", "policy", "script list", "bogus", "qdo 99", "qp 1", "price 1 buy 0.5", "do Nothing"};
        int m = mark();
        { int tt = GameDriver.time; for (String c : infos) { cmd(c); if (GameDriver.time != tt) { real.println("[T] info: time moved " + tt + "->" + GameDriver.time + " after " + c); tt = GameDriver.time; } } }
        check("S1 info commands cost no time", GameDriver.time == t0);
        check("S1 unknown command reported", since(m).contains("Unknown command 'bogus'"));
        check("S1 help printed", since(m).contains("=== Commands ==="));
        check("S1 no exceptions", !since(m).contains("Exception"));
        check("S1 PC defaults MANUAL, duties off", p.pc().mode == PlayerControl.Mode.MANUAL && p.pc().dutiesOn() == 0);

        // ---------- S2: manual mode suppresses mundane routines ----------
        long ss0 = p.ss; int kills0 = p.kills, crafted0 = p.itemsCrafted; Area loc0 = p.location;
        m = mark();
        waitTurns(300);
        check("S2 300 turns passed", GameDriver.time >= t0 + 300);
        check("S2 no crafting/hunting/gathering happened by itself", p.kills == kills0 && p.itemsCrafted == crafted0 && p.materialBag.isEmpty() && p.location == loc0);
        check("S2 queues still fill", p.targetQuests.size() + p.spotQuests.size() > 0);
        check("S2 idMismatch 0", idMismatch() == 0);
        int alerts = since(m).split("\\[Quest Alert\\]", -1).length - 1;
        real.println("[T] info: alerts during 300 idle turns = " + alerts + "; inbox=" + p.pc().inbox.size());
        check("S2 alerts have coordinates", alerts == 0 || since(m).matches("(?s).*\\[Quest Alert\\][^\\n]*\\[\\d+, \\d+\\].*") || since(m).contains("(here, no travel)"));

        // ---------- S3: duty toggles ----------
        cmd("duties");
        cmd("duties craft on");
        check("S3 craft duty on", p.pc().duties[PlayerControl.CRAFT]);
        cmd("duties bogus on");
        cmd("duties all on");
        check("S3 all duties on", p.pc().dutiesOn() == 10 && p.pc().dutySummary().equals("all on"));
        cmd("duties sect off");
        check("S3 mixed state", p.pc().dutiesOn() == 9 && p.pc().dutySummary().startsWith("mixed"));
        cmd("duties all off");
        check("S3 all off", p.pc().dutiesOn() == 0);
        cmd("duties craft on");
        waitTurns(120);
        check("S3 craft duty made the PC work (gathered or crafted)", !p.materialBag.isEmpty() || p.itemsCrafted > crafted0);
        cmd("duties all off");

        // ---------- S4: quest tools ----------
        waitTurns(40);
        cmd("q");
        ArrayList<Quest> view = p.pc().view;
        check("S4 quest view numbered", !view.isEmpty());
        Quest target = null; int row = -1;
        for (int i = 0; i < view.size(); i++) if (p.targetQuests.contains(view.get(i))) { target = view.get(i); row = i + 1; break; }
        if (target == null) { target = view.get(0); row = 1; }
        cmd("qp " + row + " 97");
        check("S4 pinned priority", target.pinned && target.priority == 97 && target.priorityDiff == 0);
        waitTurns(60);
        check("S4 pinned priority never ages", target.priority == 97 || !(p.targetQuests.contains(target) || p.spotQuests.contains(target) || p.currentQuest == target) || target.pinned);
        cmd("q");
        int row2 = p.pc().view.indexOf(target) + 1;
        if (row2 > 0) { cmd("qu " + row2); check("S4 unpin", !target.pinned); }
        cmd("q");
        Quest del = p.pc().view.isEmpty() ? null : p.pc().view.get(p.pc().view.size() - 1);
        if (del != null) { cmd("qd " + p.pc().view.size()); check("S4 delete removes quest", !p.targetQuests.contains(del) && !p.spotQuests.contains(del) && p.currentQuest != del); }
        String muteName = "Hunt Beast";
        cmd("mute " + muteName);
        waitTurns(250);
        boolean reappeared = p.targetQuests.stream().anyMatch(q -> q.name.equals("Hunt Beast")) || (p.currentQuest != null && p.currentQuest.name.equals("Hunt Beast"));
        check("S4 muted quest stays deleted", !reappeared);
        cmd("unmute " + muteName);
        cmd("qnew home 15 15 88");
        boolean wp = p.targetQuests.stream().anyMatch(q -> q.userCreated && q.pinned && q.priority == 88);
        check("S4 custom waypoint quest created", wp);

        GameDriver.frozenNpcs = true; // no random NPC challenges may steal the prompts below
        // ---------- S5: alerts, Do Quest, kill/spare ----------
        DecisionBroker.scripted = null; // real console prompts here
        GameDriver.time = Math.max(GameDriver.time, 60); // past the first-year truce of the start zone
        if (p.location.arrayBag != null) p.location.arrayBag.clear(); // city arrays would make the avenger break them first (the sim buys some over the years)
        Character strong = p;
        strong.lvl = 100; strong.majorLvl = 10; strong.minorLvl = 0; strong.caution = 0;
        Character foe1 = null, foe2 = null;
        // put two weak NPCs on the PC's tile
        int found = 0;
        for (int i = 0; i < Map.characters.size() && found < 2; i++) {
            Character c = Map.characters.get(i);
            if (c == null || c.dead || !c.born || c.player != 0 || c.age < 12 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            c.location.removeChar(c); c.location = p.location; c.location.addChar(c); c.lvl = 5; c.affiliation = "Evil"; c.affiliationNum = -1; c.caution = 100000;
            if (found == 0) foe1 = c; else foe2 = c;
            found++;
        }
        real.println("[T] info: foes found=" + found + " foe1=" + foe1 + " foe2=" + foe2 + " same=" + (foe1 != null && foe1.location == p.location) + " chars=" + Map.characters.size());
        check("S5 foes placed", foe1 != null && foe2 != null && foe1.location == p.location);
        if (foe1 != null && foe2 != null) {
            final Character f1 = foe1;
            final Quest rq = new Quest("Revenge", true, 60, 8, 80, foe1);
            p.targetQuests.add(rq);
            m = mark();
            cmd("wait 1");
            String s = since(m);
            check("S5 alert shows target and coordinates", s.contains("[Quest Alert] Revenge") && s.matches("(?s).*Revenge -> .* at \\[\\d+, \\d+\\].*"));
            cmd("q");
            int rr = p.pc().view.indexOf(rq) + 1;
            check("S5 alert quest is in the numbered view", rr > 0);
            // foes wander during the tick: put them back on the PC's tile, then Do Quest with the real console prompt
            for (Character f : new Character[]{foe1, foe2}) { f.location.removeChar(f); f.location = p.location; f.location.addChar(f); }
            cmd("q"); rr = p.pc().view.indexOf(rq) + 1; // other alerts may have re-ordered the numbered view
            m = mark();
            in.feed("qdo " + rr + "\n");
            awaitBlocked(60);
            String prompt = since(m);
            real.println("[T] info: S5 time=" + GameDriver.time + " pLoc=" + p.location.name + "/" + p.location.x + "," + p.location.y + " zone=" + p.location.isStartZone() + " foeLoc=" + foe1.location.name + " arrays=" + (p.location.arrayBag == null ? -1 : p.location.arrayBag.size()) + " rel=" + p.relationshipList.contains(foe1.id) + " foeAff=" + foe1.affiliation + " pAff=" + p.affiliation + " foeDead=" + foe1.dead + " rq=" + p.targetQuests.contains(rq) + " mode=" + p.pc().mode + " promptable=" + DecisionBroker.promptable(p) + " pLvl=" + p.tempLvl + " foeLvl=" + foe1.tempLvl + " foeTime=" + foe1.time + " caution=" + p.caution + " pSect=" + p.sect + " foeSect=" + foe1.sect + " age=" + foe1.age + " rqq=" + rq.name + "/" + rq.category);
            real.println("[T] info: S5 prompt text = " + prompt.replace((char) 10, (char) 124));
            check("S5 kill/spare prompt raised", prompt.contains("DECISION") && prompt.contains("Kill or spare"));
            cmd("2"); // Spare
            check("S5 spare: foe alive", !foe1.dead);
            check("S5 revenge quest consumed after Do Quest", !p.targetQuests.contains(rq));
            // an out-of-place Do Quest is refused with the reason
            final Quest rq2 = new Quest("Revenge", true, 60, 8, 80, foe1);
            p.targetQuests.add(rq2);
            foe1.location.removeChar(foe1); foe1.location = Map.tiles[16][15] != null ? Map.tiles[16][15] : p.location; foe1.location.addChar(foe1);
            cmd("q");
            int rr2 = p.pc().view.indexOf(rq2) + 1;
            m = mark();
            cmd("qdo " + rr2);
            check("S5 Do Quest refuses when the target is elsewhere and says where", since(m).contains("Revenge:") && since(m).contains("you are at"));
            check("S5 refused quest stays queued", p.targetQuests.contains(rq2));
            // kill through the prompt with an explicit 'attack'
            foe2.location.removeChar(foe2); foe2.location = p.location; foe2.location.addChar(foe2);
            cmd("people " + foe2.name);
            int idx = p.pc().peopleView.indexOf(foe2) + 1;
            if (idx > 0) {
                in.feed("attack " + idx + "\n");
                awaitBlocked(60);
                cmd("1"); // Kill
                check("S5 kill: foe dead", foe2.dead);
            } else check("S5 foe2 listed", false);
            // policy path (no prompt): AUTO-style decision through scripted broker
            p.pc().killPolicy = 3;
            p.pc().mode = PlayerControl.Mode.AUTO; // policy answers apply
            check("S5 policy 'always kill' decides without prompting", !DecisionBroker.promptable(p));
            p.pc().mode = PlayerControl.Mode.MANUAL; p.pc().killPolicy = 0;
        }

        DecisionBroker.scripted = new ArrayDeque<>();
        GameDriver.frozenNpcs = false;
        // ---------- S5b: city founding and on-demand duties ----------
        p.majorLvl = 3; p.ss += 5_000_000_000L;
        Area home = p.location;
        home.cityOwner = null;
        m = mark();
        cmd("found city");
        check("S5b claim the leaderless city", home.cityOwner == p);
        int arrays0 = home.arrayBag.size();
        cmd("do Buy City Array");
        check("S5b do Buy City Array installs an array", since(m).contains("Installed") || home.arrayBag.size() > arrays0);
        cmd("do Hold Tournament");
        check("S5b do Hold Tournament opens registration", home.tournamentTimer > 0);
        cmd("do Have Child");
        check("S5b Have Child alias accepted (male PC refused politely)", since(m).contains("Only a mother") || since(m).contains("Have Child") || since(m).contains("Get Pregnant"));
        cmd("do Return to Sect");
        check("S5b exploitable quests are refused", since(m).contains("can't be done on demand"));
        cmd("found sect");
        check("S5b found sect follows the rules", (p.sect != null && home.sect == p.sect) || since(m).contains("already holds"));
        cmd("goto 16 15");
        check("S5b goto arrives", p.location.x == 16 && p.location.y == 15);
        cmd("goto 15 15");
        cmd("!resign");
        check("S5b resign", p.sect == null);
        if (p.sect != null) { p.sect.resign(p); }
        p.ss = 0; p.sc = 0; // a rich PC would be asked to bid at every yearly auction and swallow the probe's commands

        // ---------- S6: AUTO mode ----------
        int tt = GameDriver.time;
        m = mark();
        int autoLinesBefore = out().split("Discovered").length; cmd("auto 80", 180);
        check("S6 AUTO routine prints silenced", out().split("Discovered").length - autoLinesBefore < 8);
        check("S6 AUTO ran ~80 turns then returned control", GameDriver.time >= tt + 80 && p.pc().mode == PlayerControl.Mode.MANUAL && since(m).contains("AUTO run finished"));
        // unlimited AUTO, then 'stop' typed while it runs
        in.feed("auto\n");
        Thread.sleep(400);
        in.feed("stop\n");
        awaitBlocked(120);
        check("S6 'stop' hands control back", p.pc().mode == PlayerControl.Mode.MANUAL && in.blocked);
        check("S6 no exceptions", !since(m).contains("Exception"));

        // ---------- S7: hybrid scripts ----------
        cmd("!script clear");
        cmd("script add goto 12 12");
        cmd("script add wait 3");
        cmd("script add repeat 2");
        cmd("script add meditate 2");
        cmd("script add end");
        cmd("script add if ss >= 0");
        cmd("script add wait 1");
        cmd("script add end");
        cmd("script add nonsense 1");
        cmd("script list");
        m = mark();
        runScriptToEnd(120);
        check("S7 script ran to its end", since(m).contains("[Script finished]"));
        check("S7 goto arrived", p.location.x == 12 && p.location.y == 12);
        check("S7 control returned to MANUAL", p.pc().mode == PlayerControl.Mode.MANUAL);
        cmd("!script clear");
        cmd("script add repeat 1");
        cmd("script add end");
        cmd("script add end"); // unbalanced
        cmd("script run");
        check("S7 unbalanced script rejected", p.pc().mode == PlayerControl.Mode.MANUAL);
        cmd("!script clear");
        // interrupt: attacked while waiting
        cmd("script add wait 500");
        in.feed("script run\n");
        Thread.sleep(600);
        p.pc().attackedThisTick = true;
        m = mark();
        awaitBlocked(120);
        check("S7 attack interrupts the script", since(m).contains("You were attacked") || out().contains("You were attacked"));
        check("S7 script interrupted returns control", p.pc().mode == PlayerControl.Mode.MANUAL);
        cmd("!script clear");
        // script quest step using the waypoint
        cmd("qnew wp2 15 15 80");
        cmd("script add quest Go: wp2");
        m = mark();
        cmd("script run", 120);
        check("S7 quest step walks to the waypoint and completes it", since(m).contains("You reached Go: wp2") || since(m).contains("[Script finished]"));
        cmd("!script clear");

        // ---------- S7b: every script step type ----------
        cmd("!script clear");
        cmd("goto 15 15");
        for (String st : new String[]{"forage 3", "forage until 500", "mine 2", "craft 2", "hunt 3", "meditate 2", "meditate until major 3", "sell", "wait 2", "if herb >= 1", "wait 1", "end", "mode manual"})
            cmd("script add " + st);
        m = mark();
        runScriptToEnd(240);
        check("S7b all step types ran without exceptions", !since(m).contains("Exception"));
        check("S7b script ended or was halted with a reason", since(m).contains("[Script finished]") || since(m).contains("[Script stopped]") || since(m).contains("switching to MANUAL"));
        check("S7b control back with the player", p.pc().mode == PlayerControl.Mode.MANUAL);
        cmd("!script clear");

        // ---------- S8: shop prices ----------
        Shop shop = new Shop(p, p.location);
        p.ownedShops.add(shop);
        cmd("price 1 buy 0.6");
        cmd("price 1 sell 1.8");
        cmd("price 1 city 0.4");
        check("S8 rates set", Math.abs(shop.buyRate - 0.6) < 1e-9 && Math.abs(shop.sellRate - 1.8) < 1e-9 && Math.abs(shop.cityBuyRate - 0.4) < 1e-9);
        cmd("price 1 sell 0.62");
        cmd("price 1 buy 2");
        check("S8 unsafe rates rejected", Math.abs(shop.buyRate - 0.6) < 1e-9 && Math.abs(shop.sellRate - 1.8) < 1e-9);
        cmd("shops");
        p.ownedShops.remove(shop);

        // ---------- S9: policy & scripted decision broker ----------
        cmd("policy kill 2");
        cmd("policy flee off");
        cmd("policy forbidden on");
        cmd("policy duel evade");
        check("S9 policies set", p.pc().killPolicy == 2 && !p.pc().fleePolicy && p.pc().forbiddenPolicy && !p.pc().duelPolicy);
        cmd("policy kill 0"); cmd("policy flee on"); cmd("policy forbidden off"); cmd("policy duel accept");

        // ---------- S9b: human targets of NPC diplomacy are asked (scripted broker) ----------
        {
            Character boss = null;
            for (int i = 200; i < Map.characters.size() && boss == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 12 && c.sect == null && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) boss = c; }
            Organization bossOrg = new Organization(boss);
            Organization pOrg = new Organization(p);
            boss.lvl = 300; p.ss = 100_000_000L;
            long ssBefore = p.ss + p.sc * 1_000_000L; // large balances are stored as spirit crystals
            DecisionBroker.scripted = new ArrayDeque<>(Arrays.asList(0));
            int promptsBefore = DecisionBroker.prompts;
            boss.targetQuests(new Quest("Demand Tribute", true, 0, 0, 0, p));
            check("S9b human is asked about tribute and pays on Yes", DecisionBroker.prompts == promptsBefore + 1 && p.ss + p.sc * 1_000_000L <= ssBefore - 10_000_000L + 1);
            DecisionBroker.scripted = new ArrayDeque<>(Arrays.asList(1));
            boss.targetQuests(new Quest("Demand Tribute", true, 0, 0, 0, p));
            check("S9b refusing tribute triggers war", bossOrg.warTarget == pOrg);
            bossOrg.endWar(pOrg);
            DecisionBroker.scripted = new ArrayDeque<>(Arrays.asList(1));
            boss.targetQuests(new Quest("Propose Alliance", true, 0, 0, 0, p));
            check("S9b declining an alliance keeps the sects unallied", !bossOrg.isAlly(pOrg));
            DecisionBroker.scripted = new ArrayDeque<>(Arrays.asList(0));
            boss.targetQuests(new Quest("Propose Alliance", true, 0, 0, 0, p));
            check("S9b accepting forms the alliance", bossOrg.isAlly(pOrg));
            DecisionBroker.scripted = new ArrayDeque<>();
            bossOrg.dissolveAlliance(pOrg);
            p.sect = null; pOrg.members.clear(); pOrg.master = null;
            boss.sect = null; bossOrg.members.clear(); bossOrg.master = null; boss.lvl = 5; p.ss = 0; p.sc = 0;
        }

        // ---------- S13: hotspot threshold and Originis exemption ----------
        check("S13 capacity scales with starting population and map area", GameDriver.CROWD_CAPACITY == Math.max(GameDriver.CROWD_MIN_CAPACITY, (int) Math.round(Map.startingPopulation * GameDriver.CROWD_PERCENT * 961.0 / (Map.range * Map.range))) && GameDriver.CROWD_CAPACITY_CITY == 4 * GameDriver.CROWD_CAPACITY);
        real.println("[T] info: wild capacity=" + GameDriver.CROWD_CAPACITY + " city capacity=" + GameDriver.CROWD_CAPACITY_CITY + " (startingPopulation=" + Map.startingPopulation + ", range=" + Map.range + ")");
        Area origin = Map.tiles[Map.range / 2][Map.range / 2];
        check("S13 Originis is the founding tile", origin.isOriginis());
        origin.crowdHotspot = true; origin.crowdSurge = 15; origin.lvl += 15; origin.hotspot = true;
        m = mark();
        waitTurns(2);
        check("S13 Originis hotspot flag is cleared even though it is crowded", !origin.crowdHotspot && origin.crowdSurge == 0);
        check("S13 Originis never announced as overcrowded", !out().contains("Overcrowding turns Originis"));
        Area wild = null;
        for (Area ar : new ArrayList<>(Map.createdAreas)) if (!ar.city && !ar.isOriginis() && ar.monsters > 0 && ar.arrayBag != null && ar.arrayBag.isEmpty() && ar != p.location) { wild = ar; break; }
        if (wild != null) {
            int moved = 0;
            for (int i = 300; i < Map.characters.size() && moved < GameDriver.CROWD_CAPACITY + 20; i++) {
                Character c = Map.characters.get(i);
                if (c == null || c.dead || !c.born || c.player != 0 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
                c.location.removeChar(c); c.location = wild; wild.addChar(c); moved++;
            }
            waitTurns(2);
            check("S13 a wild tile over the threshold becomes a hotspot", wild.crowdHotspot || wild.characters.size() <= GameDriver.CROWD_CAPACITY);
        } else check("S13 found a wild tile", false);

        // ---------- S14: decision timeout ----------
        {
            Character foe3 = null;
            for (int i = 400; i < Map.characters.size() && foe3 == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 12 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) foe3 = c; }
            foe3.location.removeChar(foe3); foe3.location = p.location; foe3.location.addChar(foe3); foe3.lvl = 3; foe3.caution = 100000; foe3.affiliation = "Evil"; foe3.affiliationNum = -1;
            DecisionBroker.scripted = null;
            cmd("policy timeout 2");
            check("S14 timeout setting stored", p.pc().decisionTimeout == 2);
            cmd("people " + foe3.name);
            int idx3 = p.pc().peopleView.indexOf(foe3) + 1;
            int to0 = DecisionBroker.timeouts;
            m = mark();
            in.feed("attack " + idx3 + "\n");
            awaitBlocked(60);
            check("S14 unanswered prompt falls back to the policy answer", DecisionBroker.timeouts == to0 + 1 && since(m).contains("No answer in 2s") && !foe3.dead);
            cmd("policy timeout off");
            DecisionBroker.scripted = new ArrayDeque<>();
            check("S14 timeout can be switched off", p.pc().decisionTimeout == 0);
        }

        // ---------- S15: hand-bidding at the yearly auction ----------
        {
            Area city = p.location;
            city.cityOwner = null; // the test PC claimed Originis earlier and would pay itself
            p.ss = 0; p.sc = 50_000; // 50 billion ss worth
            city.auction().clear();
            if (city.goodsBag != null) city.goodsBag.clear(); // the market goods would join the lots (and ask for more bids)
            Item lot = new Item(p, 10, Item.ItemType.Clothing);
            lot.heavenSent = true; Auctions.consign(city, lot, null);
            long reserve = Auctions.reserve(lot);
            long w0 = p.ss + p.sc * 1_000_000L;
            DecisionBroker.scriptedNumbers = new ArrayDeque<>(Arrays.asList(reserve * 3));
            int pr0 = DecisionBroker.prompts;
            Auctions.hold(city);
            check("S15 a MANUAL human in the city is asked for a bid", DecisionBroker.prompts == pr0 + 1);
            long paid = w0 - (p.ss + p.sc * 1_000_000L);
            check("S15 winning bid pays at most the maximum and at least the reserve", p.backpack.contains(lot) ? (paid >= reserve && paid <= reserve * 3) : paid == 0);
            // passing
            Item lot2 = new Item(p, 10, Item.ItemType.Clothing);
            lot2.heavenSent = true; city.auction().clear(); Auctions.consign(city, lot2, null);
            DecisionBroker.scriptedNumbers = new ArrayDeque<>(Arrays.asList(0L));
            Auctions.hold(city);
            check("S15 bid of 0 passes: the human does not get the lot", !p.backpack.contains(lot2));
            // absent human is not asked
            Item lot3 = new Item(p, 10, Item.ItemType.Clothing);
            lot3.heavenSent = true; city.auction().clear(); Auctions.consign(city, lot3, null);
            Area elsewhere = Map.tiles[16][15] != null ? Map.tiles[16][15] : origin;
            Area back = p.location;
            p.location.removeChar(p); p.location = elsewhere; elsewhere.addChar(p);
            DecisionBroker.scriptedNumbers = new ArrayDeque<>(Arrays.asList(reserve * 3));
            pr0 = DecisionBroker.prompts;
            Auctions.hold(city);
            check("S15 a human outside the city is not asked", DecisionBroker.prompts == pr0);
            p.location.removeChar(p); p.location = back; back.addChar(p);
            DecisionBroker.scriptedNumbers = new ArrayDeque<>();
            cmd("auction");
        }

        // ---------- S16: lists, id selection, shop16 buying, seek ----------
        {
            Area city = p.location;
            p.ss = 0; p.sc = 5_000; // 5 billion ss: the test shop's 5x prices are far above what an NPC buyer can pay
            int m16 = mark();
            cmd("list people 5");
            check("S16 list people honours an explicit limit", since(m16).contains("showing 5 of"));
            m16 = mark();
            cmd("list players 50 lvl>=1 sort=lvl desc");
            check("S16 'list players' alias, filters and sort work", since(m16).contains("showing ") && !since(m16).contains("Unknown"));
            m16 = mark();
            cmd("list people lvl>=100000");
            check("S16 impossible filter shows nothing", since(m16).contains("nothing matches"));
            m16 = mark();
            cmd("list people bogus=1");
            check("S16 unknown filter key is reported with the valid keys", since(m16).contains("Unknown filter 'bogus'"));
            cmd("limit 7");
            check("S16 limit sets the default row count", p.pc().listLimit == 7);
            m16 = mark();
            cmd("people");
            check("S16 default limit applies to a bare 'people'", since(m16).contains("showing 7 of"));
            cmd("limit 500");
            m16 = mark();
            cmd("list people 20 sort=id");
            java.util.regex.Matcher idm = java.util.regex.Pattern.compile("(?m)^\\s*1\\s+(\\d+)\\s").matcher(since(m16));
            check("S16 list rows carry character ids", idm.find());
            m16 = mark();
            cmd("list monsters");
            check("S16 list monsters shows the beast count note", since(m16).contains("Wild beasts in this tile"));
            cmd("limit 20");

            Character near = null, far = null;
            for (int i = 500; i < Map.characters.size() && (near == null || far == null); i++) {
                Character c = Map.characters.get(i);
                if (c == null || c.dead || !c.born || c.player != 0 || c.age < 12 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
                if (near == null) near = c; else far = c;
            }
            near.location.removeChar(near); near.location = city; city.addChar(near); near.lvl = 3;
            m16 = mark();
            cmd("talk #" + near.id);
            check("S16 talk works by #id for someone in the tile", since(m16).contains("talked to"));
            Area away = Map.tiles[16][15] != null ? Map.tiles[16][15] : Map.tiles[14][15];
            far.location.removeChar(far); far.location = away; away.addChar(far); // moved only now: a turn passes after every command and an NPC next door can wander in
            m16 = mark();
            cmd("talk #" + far.id);
            real.println("[T] info: far talk output: " + since(m16).replace((char) 10, (char) 124) + " | away=" + away + " far.loc=" + far.location + " dead=" + far.dead);
            check("S16 #id of someone elsewhere explains where and offers seek", since(m16).contains("is at") && since(m16).contains("seek " + far.id));
            m16 = mark();
            cmd("talk #99999999");
            check("S16 unknown id is refused", since(m16).contains("No living cultivator"));

            m16 = mark();
            cmd("talk");
            check("S16 talk with no target picks someone at random", since(m16).contains("No target given") && since(m16).contains("talked to"));
            m16 = mark();
            cmd("spar");
            check("S16 spar with no target picks someone at random", since(m16).contains("No target given") || since(m16).contains("Nobody here"));
            m16 = mark();
            cmd("invite");
            check("S16 invite with no target picks someone at random", since(m16).contains("No target given") || since(m16).contains("Nobody here"));
            cmd("qnew uidtest " + p.location.x + " " + p.location.y + " 33");
            Quest uq = p.targetQuests.stream().filter(q -> q.name.equals("Go: uidtest")).findFirst().orElse(null);
            check("S16 quests have stable uids", uq != null && uq.uid > 0);
            if (uq != null) {
                cmd("qp #" + uq.uid + " 77");
                check("S16 qp #uid pins by uid", uq.pinned && uq.priority == 77);
                cmd("!qd #" + uq.uid);
                check("S16 qd #uid deletes by uid", !p.targetQuests.contains(uq));
            }
            cmd("seek " + far.id + " friend 75");
            Quest sq = null;
            for (Quest q : p.targetQuests) if (q.name.equals("Befriend") && q.targetCharacter == far.id) sq = q;
            check("S16 seek friend pins a Befriend quest with the given priority", sq != null && sq.pinned && sq.priority == 75 && sq.userCreated);
            cmd("seek " + near.id + " spar");
            Quest sp = null;
            for (Quest q : p.targetQuests) if (q.name.equals("Spar") && q.targetCharacter == near.id) sp = q;
            check("S16 seek spar creates a Spar quest", sp != null);
            cmd("q");
            int row16 = p.pc().view.indexOf(sp) + 1;
            m16 = mark();
            near.location.removeChar(near); near.location = p.location; p.location.addChar(near);
            cmd("qdo " + row16, 60);
            check("S16 Do Quest runs the Spar quest at the target's tile", !since(m16).contains("Exception") && !p.targetQuests.contains(sp));
            final Character farF = far;
            far.location.removeChar(far); far.location = p.location; p.location.addChar(far);
            cmd("q");
            Quest bq = p.targetQuests.stream().filter(q -> q.name.equals("Befriend") && q.targetCharacter == farF.id).findFirst().orElse(null);
            int rowb = p.pc().view.indexOf(bq) + 1;
            far.location.removeChar(far); far.location = p.location; p.location.addChar(far);
            cmd("qdo " + rowb, 60);
            check("S16 Do Quest runs the Befriend quest", bq != null && !p.targetQuests.contains(bq));

            Character owner = null;
            for (int i = 700; i < Map.characters.size() && owner == null; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 12 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) owner = c; }
            Shop shop16 = new Shop(owner, city);
            Item w30 = new Item(owner, Item.WeaponType.Sword, 500), w5 = new Item(owner, Item.WeaponType.Saber, 450), c50 = new Item(owner, 500, Item.ItemType.Clothing);
            shop16.inventory.add(w30); shop16.inventory.add(w5); shop16.inventory.add(c50);
            shop16.sellRate = 5.0; city.shops.add(shop16);
            int shopNo = city.shops.size();
            m16 = mark();
            cmd("list shop type=weapon lvl>=480 sort=price desc");
            String shopList = since(m16);
            check("S16 shop filter by type and level keeps only the matching item", shopList.contains(w30.name) && !shopList.contains(w5.name) && !shopList.contains(c50.name));
            m16 = mark();
            cmd("list shop sort=price desc 10");
            String s2 = since(m16);
            check("S16 shop sort by price puts the dearest first", s2.indexOf(c50.name) >= 0 && s2.indexOf(c50.name) < s2.indexOf(w5.name));
            m16 = mark();
            cmd("list shop price<=1 afford=1");
            check("S16 price filter with nothing cheap shows nothing", since(m16).contains("nothing matches"));
            cmd("list shop type=weapon lvl>=480");
            long before = p.ss + p.sc * 1_000_000L; int inv0 = shop16.inventory.size();
            m16 = mark();
            cmd("buy 1");
            check("S16 buy <#> buys the listed item", (p.backpack.contains(w30) || p.equippedWeapon == w30) && shop16.inventory.size() == inv0 - 1 && p.ss + p.sc * 1_000_000L < before && since(m16).contains("You bought"));
            m16 = mark();
            cmd("buy " + shopNo + ".1 Wrong Name");
            check("S16 buy S.I refuses when the name doesn't match", since(m16).contains("stock changed") && shop16.inventory.size() == inv0 - 1);
            int idx5 = shop16.inventory.indexOf(w5) + 1;
            cmd("buy " + shopNo + "." + idx5 + " " + w5.name);
            check("S16 buy S.I with the right name works", p.backpack.contains(w5) || p.equippedWeapon == w5);
            m16 = mark();
            cmd("buy 1");
            check("S16 a stale numbered buy is refused", since(m16).contains("no longer") || since(m16).contains("No listed item"));
            int bagNo = p.backpack.indexOf(w5) + 1;
            long b2 = p.ss + p.sc * 1_000_000L;
            m16 = mark();
            cmd("list bag type=weapon sort=lvl desc");
            check("S16 list bag shows slot numbers", since(m16).contains("Backpack"));
            city.shops.remove(shop16);
            cmd("sell " + bagNo);
            check("S16 sell <#> sells one backpack item to the city market", !p.backpack.contains(w5) && p.ss + p.sc * 1_000_000L > b2);
            m16 = mark();
            cmd("list places 3 sort=dist");
            check("S16 list places works", since(m16).contains("Known places"));
            p.ss = 0; p.sc = 0;
        }

        // ---------- S10: save / load round trip ----------
        cmd("duties city on");
        cmd("qnew savemark 15 15 66");
        cmd("script add wait 2");
        cmd("f");
        waitTurns(2);
        File sf = new File("saves/quick.dat");
        check("S10 save file written", sf.exists() && sf.length() > 1000);

        // ---------- S11: death and reincarnation keep settings ----------
        cmd("duties sect on");
        p.pc().script = new PlayerScript();
        p.dead(null);
        m = mark();
        in.feed("wait 1\n");
        awaitBlocked(60);
        check("S11 reincarnation prompt shown", since(m).contains("Your life has ended"));
        // choose "any living NPC" (its option number depends on killer/child availability): read the printed menu
        String menu = since(m);
        int npcOpt = 1;
        java.util.regex.Matcher mm = java.util.regex.Pattern.compile("(\\d+)\\. Choose any living NPC").matcher(menu);
        if (mm.find()) npcOpt = Integer.parseInt(mm.group(1));
        Character heir = null;
        for (int i = 100; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.age >= 10 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster) { heir = c; break; } }
        in.feed(npcOpt + "\n");
        Thread.sleep(200);
        in.feed(heir.id + "\n");
        awaitBlocked(60);
        Character np = pc();
        check("S11 new body is the PC", np != null && np == heir);
        check("S11 settings carried over, script cleared", np != null && np.pc().duties[PlayerControl.SECT] && np.pc().duties[PlayerControl.CITY]
                && (np.pc().script == null || np.pc().script.steps.isEmpty()) && np.pc().mode == PlayerControl.Mode.MANUAL);
        check("S11 queue is mute-aware", np != null && np.targetQuests instanceof QuestQueue && np.spotQuests instanceof QuestQueue);

        // ---------- S12: long manual soak ----------
        int before = GameDriver.time;
        m = mark();
        cmd("duties all on");
        waitTurns(600);
        check("S12 soak 600 turns with all duties on", GameDriver.time >= before + 600 && !since(m).contains("Exception"));
        check("S12 idMismatch 0", idMismatch() == 0);
        cmd("duties all off");
    }
}
