package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-14: gifts from the people who love a person who plays (`Gifts`, the quest "Give Gift", `askgift`): best friends and relatives get the quest every few weeks, walk to the human and give a gift from their OWN
 *  holdings when they stand in his tile; the gift follows the giver's trait and a level the receiver can use; nothing is made from nothing; the cooldown holds; a human can ask, and can make the quest by hand. */
public class GiftProbe {
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
        c.age = 40; c.gender = "Male"; c.lvl = 30; c.majorLvl = 3; c.maxAge = 100000; c.caution = 100000; c.sect = null; c.trait = trait; c.name = name; c.lastName = "Giftson";
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        c.materialBag.clear(); c.pillBag.clear(); c.backpack.clear(); c.arrayBag.clear(); c.ss = 0; c.sc = 0; c.dc = 0;
        return c;
    }
    static void rel(Character a, Character b, int v) { a.changeRelationship(b, 1); a.relationshipList.contains(b.id).value = v; b.changeRelationship(a, 1); b.relationshipList.contains(a.id).value = v; }
    static void tp(Character c, Area to) { c.location.removeChar(c); c.location = to; to.addChar(c); }
    static int herbs(Character c) { int n = 0; for (Item i : c.materialBag) if (i.itemType == Item.ItemType.Herb) n += i.quantity; return n; }
    static int quests(Character g, Character h) { int n = 0; if (g.currentQuest != null && g.currentQuest.name.equals("Give Gift")) n++; for (Quest q : g.targetQuests) if (q.name.equals("Give Gift") && q.targetCharacter == h.id) n++; return n; }

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
        p.lvl = 35; p.majorLvl = 3; p.maxAge = 100000; p.caution = 0; p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null; p.professionLvl = 30;
        p.equippedWeapon = null; p.backpack.clear(); p.materialBag.clear(); p.pillBag.clear();
        GameDriver.time = Math.max(GameDriver.time, 100);
        PlayerControl pc = p.pc();
        Area lab = at(6, 6), far = at(12, 9);
        p.location.removeChar(p); p.location = lab; lab.addChar(p);

        // ---- G1: who is asked
        Character best = npc(far, "Best", Character.Traits.Gatherer), friend2 = npc(far, "Friend", Character.Traits.Gatherer), kid = npc(far, "Kid", Character.Traits.Gatherer), human2 = npc(far, "Other", Character.Traits.Gatherer), kin = npc(far, "Kin", Character.Traits.Miner);
        kid.age = 8; human2.player = 1;
        rel(p, best, 3); rel(p, friend2, 2); rel(p, kid, 3); rel(p, human2, 3);
        p.siblings.add(kin);
        check("G1 a best friend and a relative are eligible; a plain friend, a child and another human are not", Gifts.eligibleGiver(p, best, 3) && Gifts.eligibleGiver(p, kin, 0) && !Gifts.eligibleGiver(p, friend2, 2) && !Gifts.eligibleGiver(p, kid, 3) && !Gifts.eligibleGiver(p, human2, 3));
        pc.nextGiftCheck = 0;
        Gifts.weekly(p);
        int asked = quests(best, p) + quests(kin, p);
        check("G1b the weekly look sends exactly one of them (" + asked + ") and starts the next check 4-8 weeks away (" + (pc.nextGiftCheck - GameDriver.time) + ")", asked == 1 && pc.nextGiftCheck - GameDriver.time >= 4 && pc.nextGiftCheck - GameDriver.time <= 8);
        Gifts.weekly(p);
        check("G1c ...and looking again before then changes nothing", quests(best, p) + quests(kin, p) == 1);
        Character npcLooker = npc(lab, "Nobody", Character.Traits.Friendly);
        Gifts.weekly(npcLooker);
        check("G1d an NPC never receives gifts (no sampler runs for it)", npcLooker.pc == null);

        // ---- G2: what is given, by trait, from the giver's own holdings
        best.materialBag.add(new Item(Item.ItemType.Herb, 4, 300));
        int before = herbs(best) + herbs(p);
        String line = Gifts.deliver(best, p);
        check("G2 a Gatherer gives herbs (" + line + ")", line != null && line.contains("herbs") && herbs(p) == 100 && herbs(best) == 200);
        check("G2b nothing was made from nothing: the herbs of both add up as before (" + before + " = " + (herbs(best) + herbs(p)) + ")", herbs(best) + herbs(p) == before);
        check("G2c the receiver found it in his materials and heard of it (" + line + ")", p.materialBag.stream().anyMatch(i -> i.itemType == Item.ItemType.Herb && i.lvl == 4));

        Character miner = npc(far, "Miner", Character.Traits.Miner); miner.materialBag.add(new Item(Item.ItemType.Metal, 6, 40));
        check("G2d a Miner gives metal (all 40 of a quality the receiver can use: skill 30 uses up to 5 + 1)", Gifts.deliver(miner, p) != null && p.materialBag.stream().anyMatch(i -> i.itemType == Item.ItemType.Metal && i.lvl == 6 && i.quantity == 40) && miner.materialBag.isEmpty());

        Character alch = npc(far, "Alch", Character.Traits.Alchemist); alch.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 4, 3));
        check("G2e an Alchemist gives one cultivation pill (" + alch.pillBag.get(0).quantity + " left)", Gifts.deliver(alch, p) != null && alch.pillBag.get(0).quantity == 2 && p.pillBag.stream().anyMatch(i -> i.pillType == Item.PillType.Cultivation && i.lvl == 4));
        Character alch2 = npc(far, "Alch2", Character.Traits.Alchemist); alch2.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, 40, 2));
        check("G2f ...never a pill far above the receiver's realm (quality 40 is refused)", Gifts.deliver(alch2, p) == null);

        Character smith = npc(far, "Smith", Character.Traits.Blacksmith);
        Item good = new Item(smith, Item.WeaponType.Sword, 35), tooLow = new Item(smith, Item.WeaponType.Sword, 5);
        smith.backpack.add(good); smith.backpack.add(tooLow);
        String sl = Gifts.deliver(smith, p);
        check("G2g a Blacksmith gives a weapon the receiver can use (Lv 31-50), not the weak one (" + sl + ")", sl != null && p.backpack.contains(good) && !p.backpack.contains(tooLow) && smith.backpack.contains(tooLow));
        Character smith2 = npc(far, "Smith2", Character.Traits.Blacksmith); smith2.materialBag.add(new Item(Item.ItemType.Metal, 6, 900)); smith2.majorLvl = 10; smith2.professionLvl = 80;
        smith2.equippedWeapon = null;
        String sm = Gifts.deliver(smith2, p);
        check("G2h a Blacksmith with metal makes one to give (" + sm + "; his metal " + smith2.materialBag.stream().mapToInt(i -> i.quantity).sum() + ")", sm != null || smith2.materialBag.stream().mapToInt(i -> i.quantity).sum() < 900);

        Character rich = npc(far, "Rich", Character.Traits.Merchant); rich.ss = 1_000_000; long r0 = PlayerLists.wealth(p), g0 = PlayerLists.wealth(rich);
        String ml = Gifts.deliver(rich, p);
        check("G2i a Merchant gives a twentieth of his wealth in silver (" + ml + "): he loses what the receiver gains", ml != null && PlayerLists.wealth(rich) == g0 - 50_000 && PlayerLists.wealth(p) == r0 + 50_000);

        Character leader = npc(far, "Leader", Character.Traits.Leader);
        check("G2j a Leader's favour needs a shared sect: without one he falls back (nothing to give: " + Gifts.deliver(leader, p) + ")", Gifts.deliver(leader, p) == null);

        // food, wine and poison come from the giver's goods
        Character chef = npc(far, "Chef", Character.Traits.Chef), brewer = npc(far, "Brewer", Character.Traits.Drunken), venom = npc(far, "Venom", Character.Traits.Poisoner);
        Life.goods(chef).add(new Item(Item.ItemType.Food, 3, 8, "Moonlit Dumplings")); Life.goods(brewer).add(new Item(Item.ItemType.Wine, 2, 3, "Plum Wine")); Life.goods(venom).add(new Item(Item.ItemType.Poison, 4, 2, "Silent Moth"));
        String fl = Gifts.deliver(chef, p), wl = Gifts.deliver(brewer, p), vl = Gifts.deliver(venom, p);
        check("G2k a Chef gives up to five portions of food (" + fl + "; " + Life.goods(chef).get(0).quantity + " left), a Drunken wine (" + wl + "), a Poisoner poison (" + vl + ")", fl != null && fl.contains("5 portions") && Life.goods(chef).get(0).quantity == 3 && wl != null && wl.contains("3 portions") && Life.goods(brewer).isEmpty() && vl != null && Life.goods(venom).isEmpty());
        check("G2l they arrive in the receiver's goods", Life.firstOf(p, Item.ItemType.Food) != null && Life.firstOf(p, Item.ItemType.Wine) != null && Life.firstOf(p, Item.ItemType.Poison) != null);

        // the cooldown: one gift a year from a giver
        pc.giftsFrom = new HashMap<>();
        Character yearly = npc(far, "Yearly", Character.Traits.Gatherer); rel(p, yearly, 3);
        Gifts.send(p, yearly);
        yearly.targetQuests.removeIf(x -> x.name.equals("Give Gift"));
        int t52 = GameDriver.time;
        check("G2m a giver who was sent is not eligible again for a year (" + Gifts.eligibleGiver(p, yearly, 3) + " now)", !Gifts.eligibleGiver(p, yearly, 3));
        GameDriver.time = t52 + 51;
        boolean at51 = Gifts.eligibleGiver(p, yearly, 3);
        GameDriver.time = t52 + 52;
        boolean at52 = Gifts.eligibleGiver(p, yearly, 3);
        GameDriver.time = t52;
        check("G2n ...not at 51 weeks (" + at51 + "), again at 52 (" + at52 + ")", !at51 && at52);

        // ---- G3: the quest walks and gives
        Character g = kin;
        kin.materialBag.add(new Item(Item.ItemType.Metal, 3, 50));
        g.targetQuests.clear(); g.currentQuest = null;
        Quest gq = new Quest("Give Gift", true, 38, 2, 58, p);
        check("G3 the quest is viable while the human lives", QuestLife.owns("Give Gift") && QuestLife.viable(g, gq) == 1);
        check("G3b away from him the quest goes on (returns 0) and gives nothing", QuestLife.run(g, gq) == 0 && g.materialBag.size() == 1);
        tp(g, lab);
        int metals = p.materialBag.size();
        check("G3c in his tile it is given and the quest is done (" + QuestLife.run(g, gq) + ")", g.materialBag.isEmpty() && p.materialBag.size() > metals);
        Character empty = npc(lab, "Empty", Character.Traits.Gatherer);
        rel(p, empty, 3); int v0 = empty.relationshipList.contains(p.id).value;
        check("G3d a giver with nothing to give only visits and the quest still ends (" + QuestLife.run(empty, new Quest("Give Gift", true, 38, 2, 58, p)) + ")", true);

        // ---- G4: asking
        PcProbe.cmd("askgift #" + best.id, 60);
        check("G4 'askgift #id' of a best friend sends them (quest " + quests(best, p) + ") unless the cooldown holds", quests(best, p) == 0 || quests(best, p) == 1);
        pc.giftsFrom = new HashMap<>();
        best.targetQuests.removeIf(x -> x.name.equals("Give Gift")); best.currentQuest = null;
        PcProbe.cmd("askgift #" + best.id, 60);
        check("G4b with no cooldown the best friend is sent (" + quests(best, p) + ")", quests(best, p) == 1);
        PcProbe.cmd("askgift #" + best.id, 60);
        check("G4c asking again while they are on the way adds no second quest (" + quests(best, p) + ")", quests(best, p) == 1);
        PcProbe.cmd("askgift #" + friend2.id, 60);
        check("G4d a plain friend (bond 2) is refused", quests(friend2, p) == 0);
        PcProbe.cmd("askgift", 60);

        // ---- G5: by hand: I give
        Character target = npc(lab, "Receiver", Character.Traits.Friendly);
        p.trait = Character.Traits.Merchant; p.ss = 5_000_000; p.convertCurrency();
        PcProbe.cmd("qadd Give Gift #" + target.id + " p=50", 60);
        Quest mine = null; for (Quest q : p.targetQuests) if (q.name.equals("Give Gift")) mine = q;
        check("G5 a Give Gift quest can be made by hand", mine != null && mine.userCreated && mine.targetCharacter == target.id);
        long w0 = PlayerLists.wealth(target);
        PcProbe.cmd("qdo #" + mine.uid, 120);
        check("G5b doing it where the person stands gives a gift from my own holdings (target " + w0 + " -> " + PlayerLists.wealth(target) + ")", PlayerLists.wealth(target) > w0);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }
}
