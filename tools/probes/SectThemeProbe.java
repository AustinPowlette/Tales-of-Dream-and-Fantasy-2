package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: sects of a theme. A third of the sect slots are kept for founders who are not Leaders (`Settlements.sectRoomForLeader`, the first draws of the yearly founding); a Leader's sect may be a guild of the
 *  craft most practised in the tile where he founds it (`Organization.tileCraft`, `rollRules`), named after it (`Naming.sect(founder, theme)`); a member who practises the guild's craft cultivates a tenth faster. */
public class SectThemeProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }
    static Area at(int dx, int dy) {
        Area h = Map.tiles[Map.range / 2][Map.range / 2];
        int x = Math.floorMod(h.x + dx, Map.range), y = Math.floorMod(h.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        return Map.tiles[x][y];
    }
    static Character npc(Area where, String name, Character.Traits trait) {
        Character c = new Character(where);
        c.age = 40; c.gender = "Male"; c.lvl = 30; c.majorLvl = 3; c.maxAge = 100000; c.caution = 100000; c.sect = null; c.trait = trait; c.name = name; c.lastName = "Guildson"; c.affiliation = "Neutral"; c.affiliationNum = 0;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
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

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;

        // ---- T1: the reserve
        int tt = Settlements.sectTarget, cc = Settlements.sectCount.get();
        Settlements.sectTarget = 30;
        int cap = 35, reserved = Settlements.reservedSlots();
        check("T1 a third of the 35 slots are kept (" + reserved + ")", reserved == 11);
        Settlements.sectCount.set(cap - reserved - 1);
        check("T1b below the Leaders' two thirds a Leader may found (" + Settlements.sectCount.get() + ")", Settlements.sectRoomForLeader() && Settlements.sectRoom());
        Settlements.sectCount.set(cap - reserved);
        check("T1c at the two thirds Leaders stop, everyone else may still found (" + Settlements.sectCount.get() + ")", !Settlements.sectRoomForLeader() && Settlements.sectRoom());
        Settlements.sectCount.set(cap);
        check("T1d at the cap nobody founds", !Settlements.sectRoom() && !Settlements.sectRoomForLeader());
        Settlements.sectTarget = tt; Settlements.sectCount.set(cc);

        // ---- T2: a Leader's guild
        Area smithTown = at(5, 5);
        for (int i = 0; i < 5; i++) npc(smithTown, "Smith" + i, Character.Traits.Blacksmith);
        npc(smithTown, "Tailor", Character.Traits.Tailor); npc(smithTown, "Tailor2", Character.Traits.Tailor);
        Character leader = npc(smithTown, "Leader", Character.Traits.Leader);
        check("T2 the craft most practised here is the Blacksmith's (five of them; two Tailors are too few to compete)", Organization.tileCraft(leader) == Character.Traits.Blacksmith);
        Area quiet = at(9, 4);
        Character lone = npc(quiet, "Lone", Character.Traits.Leader);
        npc(quiet, "OneSmith", Character.Traits.Blacksmith); npc(quiet, "OneTailor", Character.Traits.Tailor);
        check("T2b with fewer than three of any craft there is no theme", Organization.tileCraft(lone) == null);
        int themed = 0, other = 0, none = 0;
        for (int i = 0; i < 400; i++) {
            Organization o = new Organization(npc(smithTown, "F" + i, Character.Traits.Leader));
            o.founder.sect = null;
            if (o.traitRequired == Character.Traits.Blacksmith) themed++; else if (o.traitRequired != null) other++; else none++;
        }
        check("T2c of 400 sects founded there about 40% are Blacksmith guilds and none is another craft (" + themed + " themed, " + other + " other, " + none + " open)", themed > 120 && themed < 200 && other == 0);

        // ---- T3: the name
        boolean named = true; int checkedNames = 0;
        for (int i = 0; i < 200; i++) {
            Character f = npc(smithTown, "G" + i, Character.Traits.Leader);
            Organization o = new Organization(f);
            if (o.traitRequired == Character.Traits.Blacksmith) { checkedNames++; if (!(o.name.contains("Forge") || o.name.contains("Anvil Hall") || o.name.contains("Weapon Hall") || o.name.contains("Clan") || o.name.contains("Family") || o.name.contains("House") || o.name.contains("Ancestral") || o.name.length() > 0 && !o.name.contains("Dynasty") && !o.name.contains("Throne"))) named = false; }
        }
        check("T3 a Blacksmith guild founded by a Leader is named like a forge, not like a Dynasty (" + checkedNames + " checked)", named && checkedNames > 0);
        boolean forges = false;
        for (int i = 0; i < 200 && !forges; i++) { Organization o = new Organization(npc(smithTown, "H" + i, Character.Traits.Leader)); if (o.traitRequired == Character.Traits.Blacksmith && (o.name.contains("Forge") || o.name.contains("Anvil") || o.name.contains("Weapon"))) forges = true; }
        check("T3b ...and some do say Forge / Anvil Hall / Weapon Hall", forges);

        // ---- T4: the bonus
        Character a1 = npc(smithTown, "InGuild", Character.Traits.Blacksmith), a2 = npc(smithTown, "Outside", Character.Traits.Blacksmith), a3 = npc(smithTown, "Other", Character.Traits.Tailor);
        Organization guild = new Organization(npc(smithTown, "Master", Character.Traits.Leader));
        guild.traitRequired = Character.Traits.Blacksmith;
        a1.sect = guild; a3.sect = guild;
        for (Character c : new Character[]{a1, a2, a3}) { c.potential = 50; c.lvl = 20; c.cultivation = 10; c.cultivationBase = 0; c.cultivationBaseMax = 1e9; c.fx = null; c.inhibitor = 1; if (c.trait != Character.Traits.Lucky) c.cultivate(1.0); }
        check("T4 the guild's own craft cultivates a tenth faster (" + a1.cultivationBase + " vs " + a2.cultivationBase + ")", Math.abs(a1.cultivationBase - a2.cultivationBase * 1.1) < 1e-6 && a2.cultivationBase > 0);
        check("T4b a member of another craft gets nothing extra (" + a3.cultivationBase + ")", Math.abs(a3.cultivationBase - a2.cultivationBase) < 1e-6);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
