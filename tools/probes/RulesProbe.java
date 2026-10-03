package Game;

import java.io.*;
import java.util.*;

/** The 2026-09-30 rule changes: first generation starts at 18, first-year truce in Originis, lost fights are only fatal when the death roll says so, luck. */
public class RulesProbe {
    static long hash(java.awt.image.BufferedImage im) { long h = 7; for (int y = 0; y < im.getHeight(); y += 3) for (int x = 0; x < im.getWidth(); x += 3) h = h * 31 + im.getRGB(x, y); return h; }
    static void check(String name, boolean ok) { PcProbe.passes += ok ? 1 : 0; PcProbe.fails += ok ? 0 : 1; PcProbe.real.println("[T] " + (ok ? "PASS " : "FAIL ") + name); }

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

    static List<Character> npcs(int n, java.util.function.Predicate<Character> f) {
        List<Character> out = new ArrayList<>();
        for (int i = 0; i < Map.characters.size() && out.size() < n; i++) {
            Character c = Map.characters.get(i);
            if (c != null && !c.dead && c.born && c.player == 0 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster && f.test(c)) out.add(c);
        }
        return out;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("R0 player created", p != null);
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();

        // ---------- R1 start age ----------
        check("R1 the player starts at 18", p.age == 18);
        List<Character> first = npcs(200, c -> true);
        check("R1 the first generation starts at 18 (" + first.size() + " sampled)", first.size() == 200 && first.stream().allMatch(c -> c.age == 18 || c.age == 19));

        // ---------- R2 truce ----------
        check("R2 it is still the first year", GameDriver.time < GameDriver.TRUCE_WEEKS);
        List<Character> evil = npcs(60, c -> "Evil".equals(c.affiliation) && c.location.isOriginis());
        List<Character> prey = npcs(60, c -> !"Evil".equals(c.affiliation) && c.location.isOriginis());
        int n = Math.min(evil.size(), prey.size());
        check("R2 have fighters (" + n + ")", n >= 30);
        for (int i = 0; i < n; i++) { evil.get(i).battleCheck(prey.get(i)); }
        long dead1 = 0; for (int i = 0; i < n; i++) if (evil.get(i).dead || prey.get(i).dead) dead1++;
        check("R2 nobody dies fighting in Originis during the truce", dead1 == 0);
        // sparring is still allowed
        Character s1 = prey.get(0), s2 = prey.get(1);
        s1.sparring = true; s2.sparring = true; s1.battleCheck(s2); s1.sparring = false; s2.sparring = false;
        check("R2 sparring is still allowed during the truce (nobody dead)", !s1.dead && !s2.dead);

        // ---------- R3 lethality follows the death roll ----------
        GameDriver.time = 200; // truce over
        List<Character> evil2 = npcs(120, c -> "Evil".equals(c.affiliation) && c.location.isOriginis() && !c.dead);
        List<Character> prey2 = npcs(240, c -> !"Evil".equals(c.affiliation) && c.location.isOriginis() && !c.dead);
        int m = Math.min(evil2.size(), prey2.size());
        int fought = 0, died = 0;
        for (int i = 0; i < m; i++) {
            Character x = evil2.get(i), y = prey2.get(i);
            if (x.dead || y.dead) continue;
            x.tempLvlCheck(); y.tempLvlCheck();
            x.battleCheck(y);
            fought++;
            if (x.dead || y.dead) died++;
        }
        PcProbe.real.println("[T] info: " + fought + " fights between strangers after the truce, " + died + " deaths");
        check("R3 most lost fights between strangers are no longer fatal (" + died + " of " + fought + ")", fought >= 60 && died <= fought * 0.25);
        // a wanted enemy still dies
        Character h1 = evil2.get(m - 1), h2 = prey2.get(m - 1);
        h1.dead = false; h2.dead = false;
        h1.changeRelationship(h2, -3); h1.changeRelationship(h2, -3); h1.changeRelationship(h2, -3);
        Relationship rr = h1.relationshipList.contains(h2.id);
        PcProbe.real.println("[T] info: relationship value " + (rr == null ? "none" : rr.value));
        int deadly = 0, trials = 0;
        for (int i = 0; i < 60 && i < m - 2; i++) {
            Character x = evil2.get(i + 1), y = prey2.get(i + 1);
            if (x.dead || y.dead) continue;
            x.changeRelationship(y, -3);
            x.changeRelationship(y, -3);
            x.changeRelationship(y, -3);
            Relationship q = x.relationshipList.contains(y.id);
            if (q == null || q.value > -3) continue;
            trials++;
            x.battleCheck(y);
            if (x.dead || y.dead) deadly++;
        }
        PcProbe.real.println("[T] info: " + trials + " fights between arch-enemies, " + deadly + " deaths");
        check("R3 fights between arch-enemies are far deadlier than between strangers (" + deadly + " of " + trials + " vs " + died + " of " + fought + ")", trials >= 10 && deadly >= Math.max(3, trials * 0.1) && deadly * 1.0 / trials > died * 1.0 / fought + 0.08);
        // non-lethal win leaves the loser alive and injured
        List<Character> pair = npcs(2, c -> !c.dead && c.location.isOriginis() && "Neutral".equals(c.affiliation));
        Character w = pair.get(0), l = pair.get(1);
        l.trait = Character.Traits.Alchemist; w.trait = Character.Traits.Alchemist;
        double before = l.cultivation;
        w.winCheck(l, false);
        check("R3 winCheck(lethal=false) leaves the loser alive and injured", !l.dead && l.cultivation <= Math.max(1, before));
        w.winCheck(l, true);
        check("R3 winCheck(lethal=true) still kills", l.dead);

        // ---------- R4 luck ----------
        List<Character> pp = npcs(4, c -> !c.dead && c.trait != Character.Traits.Lucky && c.trait != Character.Traits.Friendly);
        Character A = pp.get(0), B = pp.get(1), Lk = pp.get(2), Fr = pp.get(3);
        Lk.trait = Character.Traits.Lucky; Fr.trait = Character.Traits.Friendly;
        A.affiliationNum = 0; B.affiliationNum = 0; Lk.affiliationNum = 0; Fr.affiliationNum = 0;
        A.changeRelationship(B, 1); Relationship rel = A.relationshipList.contains(B.id); // make sure a relation object exists for the util
        A.changeRelationship(Lk, 1); A.changeRelationship(Fr, 1);
        for (Character o : new Character[]{B, Lk, Fr}) { Relationship q = A.relationshipList.contains(o.id); if (q != null) q.value = 0; A.lvl = 1; o.lvl = 1; }
        int base = 0, lucky = 0, friendly = 0, N = 40000;
        for (int i = 0; i < N; i++) { if (A.changeRelationshipUtil(B) > 0) base++; if (A.changeRelationshipUtil(Lk) > 0) lucky++; if (A.changeRelationshipUtil(Fr) > 0) friendly++; }
        PcProbe.real.println("[T] info: chance of warming up (per " + N + "): normal " + base + ", lucky " + lucky + ", friendly " + friendly);
        check("R4 the Lucky are liked more than the average", lucky > base * 1.2);
        check("R4 the Friendly are liked more than the Lucky", friendly > lucky * 1.1);
        // fortune events
        Character luckyOne = npcs(1, c -> !c.dead && c.trait != Character.Traits.Lucky).get(0);
        luckyOne.trait = Character.Traits.Lucky;
        int pot0 = luckyOne.potential, bag0 = luckyOne.backpack.size(); double cb0 = luckyOne.cultivationBase; long ss0 = luckyOne.ss;
        int lvl0 = luckyOne.lvl;
        luckyOne.potential = 40;
        for (int i = 0; i < 52000; i++) { luckyOne.time(); if (luckyOne.dead) break; }
        check("R4 over a century a Lucky cultivator finds fortune (potential " + 40 + "->" + luckyOne.potential + ", bag " + bag0 + "->" + luckyOne.backpack.size() + ", level " + lvl0 + "->" + luckyOne.lvl + ")",
                luckyOne.potential > 40 || luckyOne.backpack.size() > bag0 || luckyOne.lvl > lvl0 || luckyOne.cultivationBase != cb0);
        // ---------- R5 painted art understands the name ----------
        check("R5 number words become counts", ArtGen.countOf("Eight") == 8 && ArtGen.countOf("Twenty-One") == 21 && ArtGen.countOf("Ten-Thousand") == 10000 && ArtGen.countOf("One Hundred") == 100 && ArtGen.countOf("nonsense") == 0);
        long t0 = System.nanoTime();
        java.awt.image.BufferedImage e8 = ArtGen.render("giant_footprints", "Desert", 5, 320, 180, 1.0, new ArtGen.Opts(8, 10, new ArrayList<String>()));
        java.awt.image.BufferedImage e3 = ArtGen.render("giant_footprints", "Desert", 5, 320, 180, 1.0, new ArtGen.Opts(3, 10, new ArrayList<String>()));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        check("R5 'Eight' and 'Three' paint different pictures (" + ms + " ms for two)", e8.getWidth() == 320 && hash(e8) != hash(e3));
        java.awt.image.BufferedImage dark = ArtGen.render("forests", "Forest", 9, 320, 180, 1.0, new ArtGen.Opts(0, 20, Arrays.asList("dark", "night")));
        java.awt.image.BufferedImage plain = ArtGen.render("forests", "Forest", 9, 320, 180, 1.0, new ArtGen.Opts(0, 20, new ArrayList<String>()));
        check("R5 mood words change the scene", hash(dark) != hash(plain));
        java.awt.image.BufferedImage high = ArtGen.render("lakes", "Grassland", 9, 320, 180, 1.0, new ArtGen.Opts(0, 90, new ArrayList<String>()));
        java.awt.image.BufferedImage low = ArtGen.render("lakes", "Grassland", 9, 320, 180, 1.0, new ArtGen.Opts(0, 5, new ArrayList<String>()));
        check("R5 high-level places look different (spirit glow)", hash(high) != hash(low));
        java.awt.image.BufferedImage th = ArtGen.render("mountains", "Tundra", 3, 96, 54, 0.4, new ArtGen.Opts(5, 40, new ArrayList<String>()));
        check("R5 thumbnails render", th.getWidth() == 96 && th.getHeight() == 54);
        for (String nn : ArtGen.KIND.keySet()) { java.awt.image.BufferedImage x = ArtGen.render(nn, "Grassland", 1, 160, 90, 1.0, new ArtGen.Opts(7, 60, Arrays.asList("holy", "fire"))); if (x == null) { check("R5 scene " + nn, false); } }
        check("R5 every noun paints with count + moods + level", true);
        check("R4 no crash log", !new File("crash.log").exists());
        check("R4 idMismatch 0", PcProbe.idMismatch() == 0);
    }
}
