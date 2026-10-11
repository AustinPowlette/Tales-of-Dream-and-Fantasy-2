package Game;

import java.io.*;
import java.util.*;

/** Section-6 terminal tools: map, route, time, help topics, notes, equip/compare, techniques, history/journal, dash, watch, chronicle persistence. */
public class TextProbe {
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

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();

        String s = run("map");
        PcProbe.real.println(s);
        check("T1 map draws you (@), a legend and 31 rows", s.contains("@") && s.contains("= World map") && s.contains("terrain:") && s.split("\n").length > 33);
        s = run("map 4");
        check("T1 map <radius> is smaller", s.split("\n").length < 20 && s.contains("@"));

        s = run("time");
        check("T2 time shows year, season, auction, birthday", s.contains("Year ") && s.contains("auction") && s.contains("birthday") && (s.contains("Spring") || s.contains("Summer") || s.contains("Autumn") || s.contains("Winter")));

        int px = p.location.x * 100, py = p.location.y * 100;
        s = run("route " + (px + 500) + " " + (py + 300));
        check("T3 route: 8 tiles / 8 weeks, arrival date", s.contains("Distance 8 tiles") && s.contains("Arrive in year"));
        s = run("route 99999 0");
        check("T3 route off the map is refused", s.contains("off the map"));

        s = run("help map");
        check("T4 help <topic>", s.contains("ASCII map"));
        s = run("help quests");
        check("T4 help <section>", s.contains("Quests:") && !s.contains("Move:"));
        s = run("help zzz");
        check("T4 help unknown lists topics", s.contains("No help topic"));

        run("note a quiet place to meditate");
        s = run("notes");
        check("T5 note + notes", s.contains("a quiet place to meditate") && s.contains("1."));
        s = run("map");
        check("T5 notes don't hide the @ marker", s.contains("@"));
        run("unnote 1");
        s = run("notes");
        check("T5 unnote", s.contains("(none)"));

        p.backpack.add(new Item(p, Item.WeaponType.Sword, 12)); p.backpack.add(new Item(p, 2, Item.ItemType.Clothing));
        int widx = -1;
        for (int i = 0; i < p.backpack.size(); i++) if (p.backpack.get(i).itemType == Item.ItemType.Weapon && p.backpack.get(i).lvl == 12) widx = i + 1;
        s = run("compare " + widx);
        check("T6 compare shows the fighting-level change", s.contains("fighting level") && s.contains("Candidate"));
        s = run("equip " + widx);
        check("T6 equip <n> puts it on and returns the old one", p.equippedWeapon != null && p.equippedWeapon.lvl == 12 && s.contains("You equip"));
        s = run("equip 99");
        check("T6 equip bad number refused", s.contains("No backpack item"));

        s = run("techniques");
        check("T7 techniques sheet", s.contains("=== Techniques") && s.contains("Martial:") && s.contains("Movement:"));

        Character big = new Character(Map.tiles[Map.range / 2][Map.range / 2]); big.age = 50; Map.characters.add(big); Map.population++;
        big.lvl = 31; big.majorLvl = 3; big.name = "Zhao"; big.lastName = "Mingzhe";
        Chronicle.breakthrough(big);
        Chronicle.add("city", "Test city founded by Zhao Mingzhe.", big.id);
        Character killer = new Character(Map.tiles[Map.range / 2][Map.range / 2]); killer.age = 30; Map.characters.add(killer); Map.population++; killer.name = "Lu"; killer.lastName = "Fang";
        big.dead(killer);
        s = run("history Zhao");
        check("T8 history finds the person's entries (breakthrough, city, slain)", s.contains("broke through to Major Lvl 3") && s.contains("Test city") && s.contains("was slain by"));
        s = run("history #" + big.id);
        check("T8 history #id", s.contains("Zhao Mingzhe"));
        s = run("history kind=death");
        check("T8 history kind filter", s.contains("[death]") && !s.contains("[breakthrough]"));
        s = run("journal");
        check("T8 journal", s.contains("=== Journal of") && s.contains("Recent notices"));
        check("T8 death counted by cause (battle)", Chronicle.deathCauses.getOrDefault("battle", 0) >= 1);

        s = run("dash");
        check("T9 dashboard: realms, wealth, deaths", s.contains("World dashboard") && s.contains("Cultivators by major realm") && s.contains("Wealth") && s.contains("Deaths of cultivators"));

        p.pc().watching = true; p.skipping = 20;
        Notifications.post("war", "a test war", -1, p.id);
        check("T10 a notice ends the watch", !p.pc().watching && p.skipping == 0);
        p.pc().watching = true; p.skipping = 20;
        Notifications.post("hint", "just a hint", -1, p.id);
        PcProbe.real.println("[T] info: hint wake -> watching=" + p.pc().watching);
        p.pc().watching = false; p.skipping = 0;

        SaveData d = new SaveData();
        Chronicle.notes.put("t:3,4", "persisted");
        Chronicle.bio.put(p.id, new ArrayList<>(Arrays.asList(new int[]{1, 2, 3, 4, 5})));
        Chronicle.snapshotInto(d);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(bo)) { oo.writeObject(d); }
        SaveData d2 = (SaveData) new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray())).readObject();
        int n0 = Chronicle.log.size();
        Chronicle.restoreFrom(d2);
        check("T11 chronicle, causes, bio and notes survive a save round-trip", Chronicle.log.size() == n0 && Chronicle.deathCauses.getOrDefault("battle", 0) >= 1 && "persisted".equals(Chronicle.notes.get("t:3,4")) && Chronicle.bioOf(p.id).size() == 1);
        Chronicle.restoreFrom(new SaveData());
        check("T11 an old save (nulls) restores to empty", Chronicle.log.isEmpty());

        s = run("watch 5");
        check("T12 watch command sets the watch", s.contains("Watching for up to 5 weeks"));
        PcProbe.waitTurns(6);
        check("T12 the watch runs out by itself", p.skipping <= 0);

        s = run("zzzz");
        check("T13 unknown command message intact", s.contains("Unknown command"));
    }
}
