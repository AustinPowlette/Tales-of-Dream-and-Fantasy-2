package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: finds quest results that are silent. Runs `do <quest>` for every quest a human can do on demand (and `explore` on all five dungeon kinds) in a prepared state,
 *  snapshots what the character owns before and after (coin, items, pills, level and cultivation, techniques, sect points, potential, kills, relationships, spouse, party, place),
 *  and lists the ones that changed something but printed no more than the bare "<quest> done." line. Since the "Result:" line (Character.ledgerNote) every quest done by hand that changes the snapshot says so. Run with -Dlist=1 to print every command's output. */
public class QuestSilenceProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Character adult(Area where, String gender) {
        Character c = new Character(where);
        c.age = 30; c.gender = gender; c.lvl = 30; c.majorLvl = 3; c.maxAge = 100000; c.caution = 0; c.sect = null; c.trait = Character.Traits.Friendly;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static Area at(Area h, int dx, int dy) {
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }
    static String snap(Character p) {
        int pills = 0; for (Item i : p.pillBag) pills += i.quantity;
        int goods = 0; if (p.goods != null) for (Item i : p.goods) goods += i.quantity;
        int mats = 0; for (Item i : p.materialBag) mats += i.quantity;
        return "ss=" + p.ss + "/" + p.sc + "/" + p.dc + " bag=" + p.backpack.size() + " pills=" + pills + " goods=" + goods + " mats=" + mats + " lvl=" + p.lvl + " bar=" + (int) (p.cultivationBase / Math.max(1, p.cultivationBaseMax) * 100)
                + " tech=" + p.techniqueList.size() + " sp=" + p.sectPoints + " pot=" + p.potential + " kills=" + p.kills + " rels=" + p.relationshipList.copy().size() + " sect=" + (p.sect == null ? "-" : p.sect.name)
                + " party=" + p.squadSize() + " arrays=" + p.arrayBag.size() + " loc=" + p.location.x + "," + p.location.y;
    }
    static String run1(String c) throws Exception {
        int m = PcProbe.mark();
        PcProbe.cmd(c, 120);
        StringBuilder sb = new StringBuilder();
        Character pl = PcProbe.pc();
        String nm = pl == null ? "@@" : pl.name + " " + pl.lastName + " - ";
        for (String line : PcProbe.since(m).split("\n")) {
            String t = line.trim();
            if (t.isEmpty() || t.equals(">") || t.contains("| MANUAL") || t.startsWith("next:") || t.startsWith("try:") || t.startsWith(nm) || t.startsWith("[Quest Alert]") || t.startsWith("[Notice]") || t.startsWith("=== Spring") || t.startsWith("=== Summer") || t.startsWith("=== Autumn") || t.startsWith("=== Winter")) continue;
            sb.append(t).append("\n");
        }
        return sb.toString().trim();
    }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static final ArrayList<String> silent = new ArrayList<>();
    static final boolean LIST = System.getProperty("list") != null;

    /** Runs one command; a state change with at most one line of output is "silent". */
    static void probe(Character p, String label, String cmd) throws Exception {
        String before = snap(p);
        String o = run1(cmd);
        String after = snap(p);
        int lines = o.isEmpty() ? 0 : o.split("\n").length;
        boolean changed = !before.equals(after);
        if (LIST) say(label + " [" + (changed ? "changed" : "same") + ", " + lines + " line(s)]: " + o.replace('\n', '|'));
        if (changed && lines <= 1) { silent.add(label + "  (" + o.replace('\n', '|') + ")  " + before + " -> " + after); }
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        PcProbe.check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.trait = Character.Traits.Friendly;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        Settings.lore = false;
        p.ss = 50_000_000L; p.convertCurrency();
        p.materialBag.add(new Item(Item.ItemType.Herb, 3, 6000)); p.materialBag.add(new Item(Item.ItemType.Metal, 3, 6000));
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 30));

        // ---- the five dungeon kinds, each explored a few times
        Area h = home();
        for (Area.DungeonType dt : Area.DungeonType.values()) {
            Area d = at(h, 6, 6 + dt.ordinal());
            d.dungeonType = dt; d.lvl = 12; d.highestLvl = 14; d.monsters = 20; d.dungeonCharges = 0;
            tp(p, d);
            for (int i = 0; i < 4; i++) probe(p, "explore " + dt + " #" + (i + 1), "explore");
        }
        // ---- every quest done on demand in a city with another person in the tile
        tp(p, h);
        Character m1 = adult(h, "Male"), f1 = adult(h, "Female");
        p.gender = "Male";
        ArrayList<String> names = new ArrayList<>();
        names.addAll(new TreeSet<>(PlayerCommands.DO_SPOT)); names.addAll(new TreeSet<>(PlayerCommands.DO_AREA)); names.addAll(new TreeSet<>(PlayerCommands.DO_PERSON));
        for (String q : names) {
            if (q.equals("Reincarnate") || q.equals("Found City") || q.equals("Found Sect")) continue; // end a life / restructure the world: covered by their own probes
            tp(p, h);
            String arg = PlayerCommands.DO_PERSON.contains(q) ? " #" + (q.equals("Courtship") ? f1.id : m1.id) : "";
            for (int i = 0; i < 2; i++) probe(p, "do " + q + arg + " #" + (i + 1), "do " + q + arg);
        }
        say("SILENT quest results (state changed, at most one line printed): " + silent.size());
        for (String s : silent) say("  " + s);
        PcProbe.check("no quest changes your state while printing at most one line", silent.isEmpty());
        say("DONE");
    }
}
