package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-13: the turn-cost table of the commands (plan 6.7): what costs a week and what is free, on success and on refusal.
 *  Free: trading (buy, sell, buygear, learn), gift, feed (once per companion per four weeks), eat, drink, coat, consume, install, join, resign, recruit, war, peace, alliance,
 *  invite (4-week wait per person, the party cap), a refused hunt and an equip that changes nothing. A week: moves, wait, meditate, forage, mine, hunt (a fight), play, perform,
 *  an equip that changes the gear. */
public class TurnCostProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void say(String s) { PcProbe.real.println("[T] " + s); }
    static Area home() { return Map.tiles[Map.range / 2][Map.range / 2]; }
    static Character adult(Area where) {
        Character c = new Character(where);
        c.age = 30; c.gender = "Male"; c.lvl = 30; c.majorLvl = 3; c.maxAge = 100000; c.caution = 0; c.sect = null; c.trait = Character.Traits.Friendly;
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
    static void rel(Character a, Character b, int v) {
        Relationship r = a.relationshipList.contains(b.id);
        if (r == null) a.relationshipList.add(new Relationship(b.id, v)); else r.value = v;
    }
    /** Runs a command and returns {weeks it cost, printed text}. */
    static Object[] run(String cmd) throws Exception {
        int t0 = GameDriver.time, m = PcProbe.mark();
        PcProbe.cmd(cmd, 120);
        return new Object[]{GameDriver.time - t0, PcProbe.since(m)};
    }
    static void cost(String label, String cmd, int weeks, String mustSay) throws Exception {
        Object[] r = run(cmd);
        boolean ok = (Integer) r[0] == weeks && (mustSay == null || ((String) r[1]).contains(mustSay));
        PcProbe.check(label + ": '" + cmd + "' costs " + weeks + " week(s) (took " + r[0] + ")" + (mustSay != null && !((String) r[1]).contains(mustSay) ? " [missing text '" + mustSay + "']" : ""), ok);
        if (!ok) say("   output was: " + ((String) r[1]).trim().replace('\n', '|'));
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
        PcProbe.check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        p.lvl = 100; p.majorLvl = 10; p.maxAge = 100000; p.caution = 0; p.trait = Character.Traits.Friendly;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.pc().squadPolicy = 2; p.pc().friendPolicy = 1;
        Settings.lore = false;
        GameDriver.time = Math.max(GameDriver.time, 100);
        Area lab = at(home(), 4, 4);
        lab.monsters = 0; lab.lvl = 1; lab.highestLvl = 1; lab.city = false; lab.herbMaterials = 50;
        tp(p, lab);

        // ---- controls: what must still cost a week
        cost("C1", "x", 1, null);
        cost("C2", "meditate", 1, null);
        cost("C3", "forage", 1, null);

        // ---- hunt
        cost("H1 no beasts", "hunt", 0, "No beasts");
        lab.monsters = 2; lab.lvl = 5000; lab.highestLvl = 5000;
        cost("H2 too strong for you", "hunt", 0, "too strong");
        lab.lvl = 1; lab.highestLvl = 1; lab.monsters = 3;
        cost("H3 a beast you can fight", "hunt", 1, null);
        lab.monsters = 0;

        // ---- equip
        p.equip();
        cost("E1 nothing to change", "equip", 0, "nothing changed");
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 60));
        cost("E2 a better weapon in the bag", "equip", 0, null);
        PcProbe.check("E2b the better weapon is on", p.equippedWeapon != null && p.equippedWeapon.lvl >= 60);

        // ---- invite
        Character friend = adult(lab), stranger = adult(lab), other = adult(lab);
        rel(p, friend, 3); rel(friend, p, 3); rel(p, stranger, -1); rel(stranger, p, -1);
        cost("I1 a friend accepts", "invite #" + friend.id, 0, "joins your party");
        PcProbe.check("I1b the friend follows me", friend.partyLeaderId == p.id);
        cost("I2 already in a party", "invite #" + friend.id, 0, "already in a party");
        cost("I3 a stranger declines", "invite #" + stranger.id, 0, "declines");
        cost("I4 asking again at once", "invite #" + stranger.id, 0, "turned you down");
        { int t0 = GameDriver.time; PcProbe.waitTurns(4); PcProbe.check("I5 four weeks pass", GameDriver.time - t0 >= 4); } // (an alert cuts one wait short: waitTurns repeats it)
        cost("I6 asked again after the wait", "invite #" + stranger.id, 0, "declines");
        { int guard = 0; while (p.squadSize() < p.partyCap() && guard++ < 10) adult(lab).joinParty(p); }
        rel(p, other, 3); rel(other, p, 3);
        cost("I7 a full party", "invite #" + other.id, 0, "party is full");
        PcProbe.check("I7b the one invited did not join", other.partyLeaderId < 0);

        // ---- gift, eat, drink, coat, feed
        Item gift = new Item(p, Item.WeaponType.Sword, 3); p.backpack.add(gift);
        int idx = p.backpack.indexOf(gift) + 1;
        cost("G1 gift", "gift #" + stranger.id + " " + idx, 0, "You gave");
        PcProbe.check("G1b the gift changed hands", !p.backpack.contains(gift) && stranger.backpack.contains(gift));
        { Relationship gr = p.relationshipList.contains(stranger.id); int v0 = gr == null ? 0 : gr.value;
          Item g2 = new Item(p, Item.WeaponType.Sword, 3); p.backpack.add(g2);
          Object[] r2 = run("gift #" + stranger.id + " " + (p.backpack.indexOf(g2) + 1));
          gr = p.relationshipList.contains(stranger.id);
          say("   G2 output: " + ((String) r2[1]).trim().replace('\n', '|') + " bagHas=" + stranger.backpack.contains(g2));
          PcProbe.check("G2 a second gift the same week is handed over but adds no friendship (" + v0 + " -> " + (gr == null ? 0 : gr.value) + ")", (Integer) r2[0] == 0 && stranger.backpack.contains(g2) && (gr == null ? 0 : gr.value) == v0 && ((String) r2[1]).contains("once a week")); }
        Item food = new Item(Item.ItemType.Food, 2, 6, "Probe Rice"); Life.goods(p).add(food);
        Item wine = new Item(Item.ItemType.Wine, 2, 2, "Probe Wine"); Life.goods(p).add(wine);
        Item poison = new Item(Item.ItemType.Poison, 2, 2, "Probe Poison"); Life.goods(p).add(poison);
        cost("F1 eat", "eat", 0, "You eat");
        PcProbe.check("F1b a portion of food was used", food.quantity == 5);
        cost("F2 drink", "drink", 0, "You drink");
        PcProbe.check("F2b a cup of wine was used", wine.quantity == 1);
        cost("F3 coat the blade", "coat", 0, "coated");
        PcProbe.check("F3b the blade is coated", Life.fx(p).coat == 3);
        { // a companion beast
            Character b = Taming.legend(p, "Probe", "Cub", 5); // a companion, enlisted like a tamed beast
            int before = food.quantity;
            cost("F4 feed a hungry companion", "feed 1", 0, "eats");
            PcProbe.check("F4b a portion was used", food.quantity == before - 1);
            cost("F5 feed again at once", "feed 1", 0, "fed lately");
            PcProbe.check("F5b no portion was used", food.quantity == before - 1);
            cost("F6 'feed' with no number picks the hungry one (none is hungry)", "feed", 0, "fed lately");
            { int t0 = GameDriver.time; PcProbe.waitTurns(4); PcProbe.check("F7 four weeks pass", GameDriver.time - t0 >= 4); }
            cost("F8 feed after the wait", "feed", 0, "eats");
            Taming.returnToWild(b);
        }

        // ---- trade (needs a city with a shop and a library: the capital)
        Area city = home();
        tp(p, city); p.ss = 1_000_000_000L; p.convertCurrency();
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 5));
        int bag0 = p.backpack.size();
        cost("T1 sell an item", "sell " + bag0, 0, null);
        PcProbe.check("T1b the item is gone", p.backpack.size() == bag0 - 1 || !p.location.city);
        PcProbe.cmd("list shop", 60);
        Object[] r = run("buy 1");
        say("info: buy 1 -> " + ((String) r[1]).trim().replace('\n', '|'));
        PcProbe.check("T2 buy costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        r = run("buygear");
        PcProbe.check("T3 buygear costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        r = run("learn 1");
        PcProbe.check("T4 learn costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        { int pot0 = p.potential = 40; Item pill = new Item(Item.ItemType.Pill, Item.PillType.Potential, 1, 2); p.pillBag.add(pill);
          r = run("consume");
          PcProbe.check("T5 consume costs no week and the pills work (took " + r[0] + ", potential " + pot0 + " -> " + p.potential + ")", (Integer) r[0] == 0 && p.potential > pot0); }
        // sect commands and install: refusals and the happy path where the capital allows
        r = run("join");
        PcProbe.check("T6 join costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        r = run("!resign");
        PcProbe.check("T7 resign costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        r = run("recruit #" + friend.id);
        PcProbe.check("T8 recruit costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        r = run("!war");
        PcProbe.check("T9 war costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        r = run("!peace");
        PcProbe.check("T10 peace costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        r = run("!alliance");
        PcProbe.check("T11 alliance costs no week (took " + r[0] + ")", (Integer) r[0] == 0);
        { Item ar = new Item(p, 1, Item.ItemType.Array); p.arrayBag.add(ar);
          r = run("install");
          PcProbe.check("T12 install costs no week and sets the array (took " + r[0] + ")", (Integer) r[0] == 0 && !p.arrayBag.contains(ar)); }
        // play is a Musician's work (extra cultivation): it still takes the week; for anyone else it is refused and free
        r = run("play");
        PcProbe.check("T13 play by a non-Musician is refused and free (took " + r[0] + ")", (Integer) r[0] == 0 && ((String) r[1]).contains("Only a Musician"));
        say("DONE");
    }
}
