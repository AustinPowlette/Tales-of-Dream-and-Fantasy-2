package Game;

import java.io.*;
import java.util.*;

/** Starts (protagonist / standard / story / difficulty), NPC prehistory, Brutal death, naming engine, settlement growth. */
public class StartsProbe {
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

    static Character fresh() { Character c = new Character(Map.tiles[Map.range / 2][Map.range / 2]); c.age = 18; Map.characters.add(c); Map.population++; return c; }
    static Starts.Choice story(String id, int diff) { Starts.Choice ch = new Starts.Choice(); ch.kind = Starts.Kind.STORY; ch.story = id; ch.difficulty = diff; return ch; }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        PcProbe.real.println("[T] info: p age=" + p.age + " lvl=" + p.lvl + " pot=" + p.potential + " trait=" + p.trait + " story=" + p.story + " hidden=" + p.potentialHidden);
        check("S0 protagonist: potential 100, Alchemist, age 18, ten years of cultivation (level up to 29), no story", p != null && p.potential == 100 && p.trait == Character.Traits.Alchemist && p.age <= 19 && p.lvl <= 29 && p.story == null && !p.potentialHidden);
        check("S0 difficulty 0 for a protagonist", GameDriver.difficulty == 0);

        // ---------- prehistory ----------
        List<Integer> lv = new ArrayList<>();
        for (int i = 0; i < Map.characters.size() && lv.size() < 400; i++) { Character c = Map.characters.get(i); if (c != null && c != p && c.player == 0 && c.age == 18) lv.add(c.lvl); }
        Collections.sort(lv);
        PcProbe.real.println("[T] info: first generation level median " + lv.get(lv.size() / 2) + ", max " + lv.get(lv.size() - 1) + " at age 18");
        check("S1 NPCs start at the level a cultivator of 18 would have (median > 8)", lv.get(lv.size() / 2) > 8);

        // ---------- standard roll ----------
        int low = 0, n = 400;
        for (int i = 0; i < n; i++) { Character c = fresh(); Starts.Choice ch = new Starts.Choice(); ch.kind = Starts.Kind.STANDARD_ROLL; Starts.apply(c, ch); if (c.potential < 20) low++; if (!c.potentialHidden) low += 1000; }
        PcProbe.real.println("[T] info: standard rolls with potential < 20 after the re-roll: " + low + " / " + n + " (34% before)");
        check("S2 standard roll re-rolls duds (well under the 34% of the raw curve) and hides potential", low < n * 0.22);

        // ---------- stories ----------
        Character h0 = fresh(); Starts.apply(h0, story("heir", 0));
        check("S3 Heir/Story: potential 100, Alchemist, Neutral, 500 ss, gear, pure story state", h0.potential == 100 && h0.trait == Character.Traits.Alchemist && h0.ss == 500 && h0.equippedWeapon != null && h0.story != null && h0.story.hooked && !h0.potentialHidden);
        Integer wid = h0.story.npcs.get("wraith"); Character wr = wid == null ? null : Character.charById(wid);
        check("S3 the enemy NPC exists, is strong and hates you", wr != null && wr.lvl >= 14 && h0.relationshipList.contains(wr.id) != null && h0.relationshipList.contains(wr.id).value <= -2);
        Character h1 = fresh(); Starts.apply(h1, story("heir", 1));
        check("S4 Hard: potential 60, hidden, half money, no gear", h1.potential == 60 && h1.potentialHidden && h1.ss == 250 && h1.equippedWeapon == null && h1.backpack.isEmpty());
        Character h2 = fresh(); Starts.apply(h2, story("heir", 2));
        check("S5 Brutal: potential 1-20, hidden, no money, world difficulty 2", h2.potential >= 1 && h2.potential <= 20 && h2.potentialHidden && h2.ss == 0 && GameDriver.difficulty == 2);
        check("S5 Brutal hooked story keeps its chain", h2.story.hooked && !h2.story.done);
        Character s2 = fresh(); Starts.apply(s2, story("smith", 2));
        check("S5 Brutal pure story has no hook and no chain", !s2.story.hooked && s2.story.done && s2.story.npcs.isEmpty());
        GameDriver.difficulty = 0;

        // ---------- engine ----------
        h0.player = 1; h0.pc(); // a human for the engine's notifications
        Starts.tick(h0);
        boolean hasQuest = h0.spotQuests.stream().anyMatch(q -> q.name.startsWith("Story:")) || h0.targetQuests.stream().anyMatch(q -> q.name.startsWith("Story:"));
        check("S6 the current objective is in the quest list", hasQuest);
        h0.majorLvl = 1;
        int pills0 = h0.pillBag.size();
        Starts.tick(h0);
        check("S6 reaching the realm completes step 1 and pays the reward (three pills)", h0.story.step == 1 && h0.pillBag.stream().anyMatch(i -> i.quantity >= 3));
        h0.itemsCrafted = 12; Starts.tick(h0);
        check("S6 crafting ten items completes step 2 (gear reward)", h0.story.step == 2);
        wr.dead = true;
        h0.story.step = 3; Starts.tick(h0);
        check("S6 defeating the enemy completes the story", h0.story.done);
        // story NPCs stay put
        Character ghostly = fresh(); Starts.apply(ghostly, story("orphan", 0));
        Character elder = Character.charById(ghostly.story.npcs.get("elder"));
        elder.location.removeChar(elder); Area other = null;
        for (Area ar : Map.createdAreas) if (ar != elder.location && !ar.isOriginis()) { other = ar; break; }
        if (other != null) { elder.location = other; other.addChar(elder); }
        Starts.tick(ghostly);
        check("S7 a story character that wandered off is put back in Originis", elder.location.isOriginis());

        // ---------- hidden potential reveal ----------
        Character rv = fresh(); Starts.apply(rv, story("heir", 1)); rv.player = 1; rv.pc();
        check("S8 hidden potential reads as a word", rv.potentialLabel().equals(Starts.potentialWord(rv.potential)) && !rv.potentialLabel().matches("\\d+"));
        rv.majorLvl = 1; Notifications.observe(rv);
        check("S8 the first breakthrough reveals the number", !rv.potentialHidden && rv.potentialLabel().equals(String.valueOf(rv.potential)));

        // ---------- hostility ----------
        GameDriver.difficulty = 2;
        Character x = fresh(), y = fresh(); y.player = 1; y.pc();
        x.changeRelationship(y, 0); // a relation object
        int lethal0 = 0, lethal2 = 0, N = 3000;
        for (int i = 0; i < N; i++) { GameDriver.difficulty = 0; if (x.deathCheck(y)) lethal0++; GameDriver.difficulty = 2; if (x.deathCheck(y)) lethal2++; }
        check("S9 Brutal makes lost fights with the player deadlier (" + lethal0 + " -> " + lethal2 + " of " + N + ")", lethal2 > lethal0 * 5);
        GameDriver.difficulty = 0;

        // ---------- naming ----------
        Set<String> names = new HashSet<>(); int dupes = 0;
        for (int i = 0; i < 6000; i++) { String[] pn = Naming.person(i % 2 == 0); if (!names.add(pn[0] + " " + pn[1])) dupes++; }
        check("S11 names: 6,000 new cultivators share few full names (" + dupes + " namesakes)", dupes < 6);
        Set<String> cities = new HashSet<>(), sects = new HashSet<>();
        Character f0 = PcProbe.pc();
        for (int i = 0; i < 400; i++) { if (!cities.add(Naming.city(f0, 50000 + i * 300))) cities.add("DUP" + i); if (!sects.add(Naming.sect(f0))) sects.add("DUP" + i); }
        check("S11 city and sect names are unique (400 each)", cities.size() == 400 && sects.size() == 400 && cities.stream().noneMatch(s -> s.startsWith("DUP")) && sects.stream().noneMatch(s -> s.startsWith("DUP")));
        int bad = 0, unmapped = 0;
        String[] bs = {"Ocean", "Grassland", "Forest", "Desert", "Tundra", "Beach", "Frozen Grassland", "Frozen Desert", "Taiga", "Savanna"};
        for (int i = 0; i < 5000; i++) { PlaceImages.Parsed pp = PlaceImages.parse(Naming.place(bs[i % bs.length])); if (pp.noun.isEmpty()) bad++; }
        for (String noun : PlaceImages.allNouns()) if (!ArtGen.KIND.containsKey(ArtGen.slug(noun))) unmapped++;
        check("S12 every generated place name parses to a noun and every noun has a scene (" + bad + " unparsed, " + unmapped + " unmapped)", bad == 0 && unmapped == 0);
        boolean dictOk = true;
        for (String[] arr : new String[][]{Dictionary.ocean, Dictionary.beach, Dictionary.desert, Dictionary.savanna, Dictionary.grassland, Dictionary.forest, Dictionary.tundra, Dictionary.taiga, Dictionary.frozenGrassland, Dictionary.frozenDesert, Dictionary.adjectivesPlaces, Dictionary.adjectivesEntities, Dictionary.beastAdjectives, Dictionary.maleNames, Dictionary.femaleNames, Dictionary.lastNames, Dictionary.colors, Dictionary.numbers}) {
            Set<String> s = new HashSet<>(); for (String w : arr) { if (w == null || w.trim().isEmpty() || !s.add(w.toLowerCase())) dictOk = false; }
        }
        check("S12 the dictionary has no empty or duplicate entries", dictOk);
        Item w = new Item(f0, Item.WeaponType.Sword, 7);
        check("S13 item names carry their grade (" + w.name + ")", w.name.startsWith("Heaven-Grade"));
        Technique tq = new Technique(Technique.techniqueType.Martial, f0, 30);
        check("S13 technique names are generated (" + tq.name + ")", !tq.name.contains("Technique") || tq.name.length() > 12);

        // ---------- settlements ----------
        PcProbe.in.feed("duties all off\n"); // keep the player passive
        GameDriver.frozenNpcs = false;
        PcProbe.waitTurns(120);
        GameDriver.frozenNpcs = true;
        int cityNow = 0, sectNow = 0; for (Area ar : Map.createdAreas) { if (ar.city) cityNow++; if (ar.sect != null) sectNow++; }
        int placed = 0;
        for (int i = 0; i < Map.characters.size() && placed < 12; i++) { Character c = Map.characters.get(i); if (c != null && !c.dead && c.born && c.player == 0 && c.location != null && !c.location.city && c.location.sect == null && !"Ocean".equals(c.location.biomem) && c.trait != Character.Traits.Beast) { c.majorLvl = 3; c.age = 30; placed++; } }
        int[] founded = Settlements.yearly();
        int cityAfter = 0, sectAfter = 0; for (Area ar : Map.createdAreas) { if (ar.city) cityAfter++; if (ar.sect != null) sectAfter++; }
        PcProbe.real.println("[T] info: cities " + cityNow + " -> " + cityAfter + ", sects " + sectNow + " -> " + sectAfter + " (founded " + founded[0] + " / " + founded[1] + ") with " + placed + " candidates");
        check("S14 the world founds the settlements its population supports", placed >= 6 && cityAfter >= cityNow && cityAfter >= Map.population / Settlements.CITY_PER && sectAfter > sectNow && cityAfter <= Math.max(1, Map.population / Settlements.CITY_PER) + 1);
        Area founded1 = null; for (Area ar : Map.createdAreas) if (ar.city && ar.geoName != null) founded1 = ar;
        check("S14 a founded city has its own name and keeps its geographic name for pictures", founded1 != null && !founded1.name.equals(founded1.geoName) && !PlaceImages.parse(PlaceImages.geo(founded1)).noun.isEmpty());

        // ---------- Brutal death ----------
        Character doomed = PcProbe.pc();
        GameDriver.difficulty = 2;
        doomed.dead(null);
        doomed.needsReincarnation = true;
        int before = PcProbe.mark();
        GameDriver.handleReincarnation(doomed);
        check("S10 Brutal death offers no rebirth and no prompt", !PcProbe.since(before).contains("Reincarnation options") && doomed.player == 0 && !doomed.needsReincarnation);
        GameDriver.difficulty = 0;

        check("S15 no crash log", !new File("crash.log").exists());
        check("S15 idMismatch 0", PcProbe.idMismatch() == 0);
    }
}
