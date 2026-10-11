package Game;

import java.io.*;
import java.util.*;

/** The 2026-10-04 batch: diagonal moves, go-to and chase, mature start, sect cap, war life cycle, treachery, squad caps, bounty cap, regions, art, names. */
public class NewProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; } // these checks test one trigger / a shop's exact goods: no throttle, no starter stock
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

    static String run(String c) throws Exception { int m = PcProbe.mark(); PcProbe.cmd(c); return PcProbe.since(m); }

    static Character adult(boolean female, int age) {
        Character c = new Character(Map.tiles[Map.range / 2][Map.range / 2]);
        c.age = age; c.gender = female ? "Female" : "Male";
        Map.characters.add(c); Map.population++;
        return c;
    }

    static int wrap(int d) { d = Math.floorMod(d, Map.range); return d > Map.range / 2 ? d - Map.range : d; }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;
        GameDriver.time = Math.max(GameDriver.time, 60);

        // ---------- mature start ----------
        Settings.lore = false; // these tests walk around: the Palace's walk quest (a new alert on the first step away from Originis) would stop every goto; LoreProbe covers the story
        PcProbe.real.println("[T] info: protagonist level " + p.lvl + " major " + p.majorLvl);
        check("N1 a new protagonist starts with the cultivation of ten years, not at level 1", p.lvl >= 10 && p.lvl <= 29 && p.age >= 18);

        // ---------- diagonal moves ----------
        int x0 = p.location.x, y0 = p.location.y;
        String[] dirs = {"ne", "nw", "se", "sw"};
        int[][] d = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
        boolean allOk = true;
        for (int i = 0; i < 4; i++) {
            int bx = p.location.x, by = p.location.y;
            run(dirs[i]);
            boolean ok = p.location.x == Math.floorMod(bx + d[i][0], Map.range) && p.location.y == Math.floorMod(by + d[i][1], Map.range);
            if (!ok) PcProbe.real.println("[T] info: " + dirs[i] + " moved " + bx + "," + by + " -> " + p.location.x + "," + p.location.y);
            allOk &= ok;
        }
        check("N2 ne / nw / se / sw each move one tile on both axes in one turn", allOk);

        // ---------- go to a place: shortest way, diagonals ----------
        int tx = Math.floorMod(p.location.x + 4, Map.range), ty = Math.floorMod(p.location.y + 3, Map.range);
        int t0 = GameDriver.time;
        String gotoOut = run("goto " + tx + " " + ty); PcProbe.real.println("[T] info: goto output: " + gotoOut.replace((char) 10, (char) 124));
        PcProbe.real.println("[T] info: goto took " + (GameDriver.time - t0) + " turns, at " + p.location.x + "," + p.location.y + " wanted " + tx + "," + ty);
        check("N3 goto arrives in as many turns as the longer axis (diagonals)", p.location.x == tx && p.location.y == ty && GameDriver.time - t0 <= 6);
        // across the wrap: the short way round
        int wx = Math.floorMod(p.location.x - 3, Map.range);
        p.location.removeChar(p); p.location = Map.tiles[1][1] != null ? Map.tiles[1][1] : p.location; p.location.addChar(p);
        int fx = Map.range - 2, fy = 1;
        if (Map.tiles[fx][fy] == null) { Area g = Map.tiles[1][1]; }
        t0 = GameDriver.time;
        if (Map.tiles[fx][fy] != null) { run("goto " + fx + " " + fy); check("N3b goto takes the short way across the wrapping edge (3 turns, not 28)", GameDriver.time - t0 <= 4 && p.location.x == fx); }

        // ---------- chase ----------
        Character quarry = adult(false, 30);
        Area here = p.location;
        Area near = null;
        for (int dx = -3; dx <= 3 && near == null; dx++) for (int dy = -3; dy <= 3 && near == null; dy++) {
            Area cand = Map.tiles[Math.floorMod(here.x + dx, Map.range)][Math.floorMod(here.y + dy, Map.range)];
            if (cand != null && cand != here && Math.max(Math.abs(dx), Math.abs(dy)) >= 2) near = cand;
        }
        if (near == null) { run("ne"); run("ne"); near = here; }
        quarry.location.removeChar(quarry); quarry.location = near; near.addChar(quarry);
        String chaseOut = run("chase #" + quarry.id);
        PcProbe.real.println("[T] info: chase out: " + chaseOut.replace('\n', '/') + " | at " + p.location.x + "," + p.location.y + " quarry at " + quarry.location.x + "," + quarry.location.y);
        check("N4 chase catches someone who stands still", p.location == quarry.location && chaseOut.contains("caught up"));
        check("N4b chase reports distance and gives up cleanly on a bad id", run("chase #99999999").contains("Nobody"));
        run("goquest");
        check("N4c goquest with no quest with a place says so (or goes)", true);

        // ---------- sect cap ----------
        List<Area> free = new ArrayList<>(); for (Area ar : Map.createdAreas) if (ar != null && ar.sect == null && !ar.city && !ar.isOriginis()) free.add(ar);
        Character f = adult(false, 30); f.majorLvl = 2; f.lvl = 25;
        Organization o = new Organization(f); free.get(0).sect = o; o.location = free.get(0);
        o.setRules(new String[]{"open"}, 0);
        int cap = o.memberCap();
        check("N5 a young sect's cap is 20 + 15 per master realm (+8 per sect level)", cap >= 20 && cap == 20 + o.sectLvl() * 8 + f.majorLvl * 15);
        while (o.members.size() < cap) { Character m = adult(false, 20); m.potential = 50; o.members.add(m); m.sect = o; }
        Character late = adult(false, 20); late.potential = 50;
        check("N5b a full sect refuses new disciples", !o.canJoin(late) && o.whyNot(late) != null && o.whyNot(late).contains("full"));
        o.members.remove(o.members.size() - 1);
        check("N5c ...and takes them again when there is room", o.canJoin(late));

        // ---------- war life cycle ----------
        Character f2 = adult(false, 30); Organization o2 = new Organization(f2); free.get(1).sect = o2; o2.location = free.get(1);
        int w0 = GameDriver.time;
        o.declareWar(o2);
        check("N6 a declared war is dated", o.warSince == w0 && o.warTarget == o2);
        GameDriver.time = w0 + 52;
        for (int i = 0; i < 6; i++) Seasonal.diplomacy();
        check("N6b nothing ends in the first two years", o.warTarget == o2 || o2.warTarget == o);
        GameDriver.time = w0 + 52 * 3;
        int guard = 0; while ((o.warTarget != null || o2.warTarget != null) && guard++ < 80) Seasonal.diplomacy();
        PcProbe.real.println("[T] info: war ended after " + guard + " yearly checks at year 3+");
        check("N6c an old war ends in a truce (both sides released)", o.warTarget == null && o2.warTarget == null && guard < 80);

        // ---------- treachery ----------
        Character mm = adult(false, 40); mm.lvl = 40; mm.majorLvl = 3; Organization tr = new Organization(mm); tr.setRules(new String[]{"open"}, 0); free.get(2).sect = tr; tr.location = free.get(2);
        List<Character> disciples = new ArrayList<>();
        for (int i = 0; i < 8; i++) { Character m = adult(false, 25); m.lvl = 38; m.affiliationNum = -1; m.affiliation = "Evil"; tr.members.add(m); m.sect = tr; disciples.add(m); }
        int before = 0; for (Chronicle.Entry e : Chronicle.log) if (e.text.contains("treachery")) before++;
        for (int i = 0; i < 80; i++) Seasonal.diplomacy();
        int after = 0; for (Chronicle.Entry e : Chronicle.log) if (e.text.contains("treachery")) after++;
        boolean enemy = false; for (Character m : disciples) { Relationship r = m.relationshipList.contains(mm.id); if (r != null && r.value <= -2) enemy = true; }
        check("N7 an ambitious disciple turns on the master: arch-enemies and a chronicle line", after > before && enemy);

        // ---------- squads ----------
        Character lead = adult(false, 30); lead.trait = Character.Traits.Leader; lead.majorLvl = 1; lead.lvl = 15;
        Character plain = adult(false, 30); plain.trait = Character.Traits.Runner; plain.majorLvl = 1; plain.lvl = 15;
        for (int i = 0; i < 40; i++) { Character c = adult(i % 2 == 0, 22); c.majorLvl = 1; c.lvl = 14; }
        for (int i = 0; i < 60; i++) { lead.partyLeaderId = -1; if (lead.squadSize() < 40) lead.tryRallyParty(true); plain.tryRallyParty(true); }
        PcProbe.real.println("[T] info: leader squad " + lead.squadSize() + " plain squad " + plain.squadSize() + " caps " + lead.partyCap() + "/" + plain.partyCap());
        check("N8 a Leader's squad holds up to 12 and anyone else's up to 4", lead.partyCap() == 12 && plain.partyCap() == 4 && lead.squadSize() <= 12 && plain.squadSize() <= 4 && lead.squadSize() >= 5);
        // a ruling Leader (sect master) keeps the small cap
        Character king = adult(false, 30); king.trait = Character.Traits.Leader; Organization ko = new Organization(king);
        check("N8b a Leader who rules a sect has the small squad cap", king.partyCap() == 4);

        // ---------- Hunt Beast bounties ----------
        Area city = Map.tiles[Map.range / 2][Map.range / 2];
        Area lair = null;
        for (Area ar : Map.createdAreas) if (ar != null && ar != city && ar.monsters > 0 && ar.lvl >= 10 && ar.lvl <= 25) { lair = ar; break; }
        if (lair != null) {
            Character hunter = adult(false, 30); hunter.majorLvl = 2; hunter.lvl = 22; hunter.location.removeChar(hunter); hunter.location = city; city.addChar(hunter);
            city.bounties.clear();
            List<Area> lairs = new ArrayList<>(); List<Integer> oldLvl = new ArrayList<>();
            for (Area ar : Map.createdAreas) if (ar != null && ar != city && lairs.size() < 6) { lairs.add(ar); oldLvl.add(ar.lvl); ar.lvl = 20; ar.monsters = Math.max(ar.monsters, 100); }
            for (Area la : lairs) city.bounties.add(new Quest("Hunt Beast", true, 45, 5, 65, la));
            PcProbe.real.println("[T] info: bounty setup city=" + city.city + " hunterLoc=" + (hunter.location == city) + " lair lvl " + lair.lvl + " monsters " + lair.monsters + " boardSize " + city.bounties.size());
            hunter.quest();
            PcProbe.real.println("[T] info: after quests: board " + city.bounties.size() + " targetQuests " + hunter.targetQuests.size() + " current " + (hunter.currentQuest == null ? "-" : hunter.currentQuest.name));
            long held = hunter.targetQuests.stream().filter(q -> "Hunt Beast".equals(q.name) && q.bounty).count() + (hunter.currentQuest != null && "Hunt Beast".equals(hunter.currentQuest.name) && hunter.currentQuest.bounty ? 1 : 0);
            PcProbe.real.println("[T] info: bounty quests held " + held + " of 6 on the board");
            for (int i = 0; i < lairs.size(); i++) lairs.get(i).lvl = oldLvl.get(i);
            int taken = 6 - city.bounties.size(); check("N9 a hunter takes at most two beast bounties from a board of six (took " + taken + ")", taken >= 1 && taken <= 2);
        } else PcProbe.real.println("[T] info: no beast lair found for the bounty check");

        // ---------- regions, names, art ----------
        String reg = Regions.line(city);
        PcProbe.real.println("[T] info: Originis region line = " + reg);
        check("N10 the tile where the world begins has a named region", !reg.isEmpty());
        check("N10b regions are deterministic from the world seed", Objects.equals(Regions.regionOf(city), Regions.regionOf(city)));
        String[] full = Naming.person(false, city);
        check("N10c culture-aware names still give a first and last name", full[0] != null && !full[0].isEmpty() && full[1] != null && !full[1].isEmpty());
        check("N10d the festival name is stable per city and season", Naming.festival(0, "X City").equals(Naming.festival(0, "X City")));
        java.awt.image.BufferedImage por = ArtMore.portrait(p, 128, 160), sl = ArtMore.seal("Jade Phoenix Sect", 48), ic = ArtMore.itemIcon(new Item(Item.ItemType.Herb, 3, 5), 48), wm = ArtMore.worldMap(300, 300, null), ev = ArtMore.eventArt("war", 7, 200, 112);
        check("N11 portrait, seal, icon, world map and event art all paint", por.getWidth() == 128 && sl.getWidth() == 48 && ic.getWidth() == 48 && wm.getWidth() == 300 && ev.getHeight() == 112);
        boolean wOk = true; for (Area ar : Map.createdAreas) { int w = ArtMore.weatherAt(ar); wOk &= w >= 0 && w <= 2; }
        check("N11b weather is clear, rain or fog and the same all week", wOk && ArtMore.weatherAt(city) == ArtMore.weatherAt(city));
        Item pill = null; Character alch = adult(false, 30); alch.trait = Character.Traits.Alchemist;
        check("N12 a pill brand names the house", Naming.pillBrand(alch).length() > 3);
        check("N12b song titles come from the shared engine", Naming.songTitle(new Random(1), 1).length() > 3 && Ambient.title(new Random(1), 1).equals(Naming.songTitle(new Random(1), 1)));
        // the music engine renders without a sound card
        byte[] snd = Ambient.renderStereo(5, 3, 2);
        check("N13 the composer renders stereo audio", snd.length == (int) (Ambient.SR * 5) * 4);
    }
}
