package Game;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Supplier;

/** 2026-10-18 (test T3 of the control audit): every one of the 75 quest types (`PlayerControl.QUEST_TYPES`) once through the MANUAL path, in a state where it can start. Each is made with `qadd` (the New quest window's
 *  command), reached with `goquest` and done with `qdo`, exactly as a person does. For each: does it start, can a person finish it, is the result shown (a line printed, or the change visible), what does it cost in weeks.
 *  Verdicts: PASS, FAIL (refused in a valid state, exception, a result that changed something and said nothing, or "done" with nothing done and nothing said), STUCK (still in the log after 40 tries), N/A (the game
 *  makes it itself and a person cannot, or the state cannot be built here). The NPCs are frozen so the world stays as built. Run in a 3,000-person copy, from an empty folder; the table goes to `questmatrix.txt`. */
public class QuestMatrixProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static PrintStream table;
    static void say(String s) { PcProbe.real.println("[T] " + s); if (table != null) table.println(s); }

    // ------------------------------------------------------------------ the world
    static Character p;
    static Area H, C2, S, S2, W, B, T, TT, QT, D1;
    static Organization sect, sect2;
    static Character master, master2, friend, enemy, rival, arch, stranger, weak, kin, alch, merch, teacher, disciple, wife, husband, memberA, memberB, outsider;
    static int seq = 0;

    static Area at(int dx, int dy) {
        int x = Math.floorMod(H.x + dx, Map.range), y = Math.floorMod(H.y + dy, Map.range);
        if (Map.tiles[x][y] == null) Map.tiles[x][y] = new Area(Area.ghost(x == 0 ? Map.range - 1 : x - 1, y), 'e');
        Area a = Map.tiles[x][y];
        a.city = a == H || a.city; // (only what the caller sets)
        return a;
    }

    static Character npc(Area where, String gender, int lvl, Character.Traits trait) {
        Character c = new Character(where);
        c.age = 30; c.gender = gender; c.lvl = lvl; c.majorLvl = Math.max(0, (lvl - 1) / 10); c.minorLvl = Math.max(1, lvl - 10 * c.majorLvl); c.tempLvl = lvl; c.maxAge = 100000; c.born = true; c.caution = 0;
        c.trait = trait; c.affiliation = "Good"; c.affiliationNum = 1; c.name = "N" + (++seq); c.lastName = "Probe"; c.sect = null; c.potential = 60;
        c.targetQuests.clear(); c.spotQuests.clear(); c.currentQuest = null;
        while (Map.characters.size() < c.id) Map.characters.add(null);
        Map.characters.add(c); Map.population++;
        return c;
    }

    static void tp(Character c, Area to) { if (c.location != null) c.location.removeChar(c); c.location = to; to.addChar(c); c.learnPlace(to); }

    static void bond(Character a, Character b, int v) { Cheats.setBoth(a, b, v); }

    static void build() {
        H = Map.tiles[Map.range / 2][Map.range / 2];
        H.city = true; H.population = 50000; H.cityTreasury = 5_000_000; if (H.backpack == null) H.backpack = new SafeList<>(); if (H.pillBag == null) H.pillBag = new SafeList<>(); if (H.materialBag == null) H.materialBag = new SafeList<>();
        if (H.libraries.isEmpty()) H.libraries.add(new Library(H));
        C2 = at(4, 0); C2.city = true; C2.population = 40000; C2.cityTreasury = 3_000_000; C2.name = "Probe City"; C2.backpack = new SafeList<>(); C2.pillBag = new SafeList<>(); C2.materialBag = new SafeList<>(); C2.libraries.add(new Library(C2));
        S = at(0, 4); S.city = false; S.name = "Probe Peak";
        S2 = at(0, -4); S2.city = false; S2.name = "Rival Peak";
        W = at(-4, 0); W.city = false; W.monsters = 0; W.herbMaterials = 900000; W.metalMaterials = 900000; W.name = "Probe Meadow";
        B = at(-4, 4); B.city = false; B.monsters = 80; B.lvl = 5; B.highestLvl = 6; B.name = "Probe Den";
        T = at(4, 4); T.city = false; T.name = "Probe Hollow";
        TT = at(4, -4); TT.city = false; TT.name = "Probe Square";
        QT = at(-4, -4); QT.city = false; QT.monsters = 0; QT.name = "Probe Quiet";
        D1 = at(8, 8); D1.city = false; D1.name = "Probe Ruin"; D1.lvl = 12; D1.highestLvl = 14; D1.monsters = 10;
        master = npc(S, "Male", 80, Character.Traits.Leader); sect = new Organization(master); S.sect = sect; open(sect); // (a real sect: the constructor with a name is the Palace's)
        master2 = npc(S2, "Male", 85, Character.Traits.Leader); sect2 = new Organization(master2); S2.sect = sect2; open(sect2);
        friend = npc(H, "Female", 40, Character.Traits.Friendly); enemy = npc(H, "Male", 30, Character.Traits.Friendly); rival = npc(H, "Male", 40, Character.Traits.Friendly); arch = npc(H, "Male", 40, Character.Traits.Friendly);
        stranger = npc(H, "Male", 40, Character.Traits.Friendly); weak = npc(H, "Male", 12, Character.Traits.Friendly); kin = npc(H, "Female", 40, Character.Traits.Friendly);
        alch = npc(H, "Male", 40, Character.Traits.Alchemist); alch.affiliation = "Evil"; alch.affiliationNum = -1; merch = npc(H, "Male", 40, Character.Traits.Merchant);
        teacher = npc(H, "Male", 90, Character.Traits.Friendly); disciple = npc(H, "Male", 20, Character.Traits.Friendly);
        wife = npc(H, "Female", 40, Character.Traits.Friendly); husband = npc(H, "Male", 40, Character.Traits.Friendly);
        memberA = npc(S, "Male", 50, Character.Traits.Friendly); sect.forceJoin(memberA); memberB = npc(S2, "Male", 30, Character.Traits.Friendly); sect2.forceJoin(memberB);
        outsider = npc(S, "Male", 45, Character.Traits.Friendly);
    }

    /** A sect that takes anybody. */
    static void open(Organization o) { o.minPotential = 0; o.minLevel = 0; o.genderPref = null; o.affiliationMatch = null; o.traitRequired = null; o.familyName = null; }

    static void baseState() {
        p.lvl = 55; p.majorLvl = 5; p.minorLvl = 5; p.tempLvl = 55; p.potential = 80; p.maxAge = 100000; p.age = 30; p.caution = 0; p.gender = "Male"; p.trait = Character.Traits.Friendly; p.affiliation = "Good"; p.affiliationNum = 1;
        p.dead = false; p.skipping = 0; p.movement = 'x'; p.autoX = -1; p.autoY = -1; p.chaseId = -1; p.partyLeaderId = -1; p.spouse = null; p.pregnant = false;
        p.targetQuests.clear(); p.spotQuests.clear(); p.currentQuest = null;
        if (p.sect != null) { p.sect.resign(p); } p.sect = null; p.sectPoints = 0; p.sectMission = null; p.sectMissionBonus = 0;
        p.ss = 20_000_000L; p.sc = 0; p.dc = 0; p.convertCurrency();
        p.backpack.clear(); p.materialBag.clear(); p.pillBag.clear(); p.arrayBag.clear(); p.ownedShops.clear();
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 50)); p.backpack.add(new Item(p, 50, Item.ItemType.Clothing)); p.equip();
        p.materialBag.add(new Item(Item.ItemType.Herb, 3, 6000)); p.materialBag.add(new Item(Item.ItemType.Metal, 3, 6000));
        p.pc().mode = PlayerControl.Mode.MANUAL;
        p.nextExploreWeek = 0; p.nextHuntWeek = 0;
        p.techniqueList.clear(); p.techniqueList.add(new Technique(Technique.techniqueType.Martial, p, 60));
        for (Character c : new Character[]{friend, enemy, rival, arch, stranger, weak, kin, alch, merch, teacher, disciple, wife, husband}) if (c != null) { c.dead = false; c.spouse = null; c.children.clear(); c.pregnant = false; c.gender = c == friend || c == kin || c == wife ? "Female" : "Male"; if (c.location != H) tp(c, H); }
        p.children.clear();
        bond(p, friend, 2); bond(p, enemy, -1); bond(p, rival, -2); bond(p, arch, -3); bond(p, kin, 3); bond(p, teacher, 2); bond(p, disciple, 2); bond(p, alch, 0); bond(p, merch, 1);
        kin.mother = null;
        tp(p, H);
        for (Area a : new Area[]{H, C2, S, S2, W, B, T, TT, QT, D1}) p.learnPlace(a);
        H.tournamentTimer = 0; H.tournamentRegistrants.clear(); TT.tournamentTimer = 0;
        sect.questBoard.clear(); sect.treasury.clear(); C2.arrayBag.clear(); S.arrayBag.clear(); H.arrayBag.clear();
        sect.warTarget = null; sect2.warTarget = null; sect.allies.clear(); sect2.allies.clear(); sect.peaceUntil = 0; sect2.peaceUntil = 0; sect.master = master; sect2.master = master2;
        sect2.memberRanks.remove(memberB.id);
    }

    // ------------------------------------------------------------------ one trial
    static final class Result { String type, verdict, reason, started = "", cost = ""; String out = ""; }

    interface Setup { String go() throws Exception; }
    interface Effect { boolean happened(); }
    static final class Scn { Setup setup; Effect effect; String na, quest; Scn(Setup s, Effect e) { setup = s; effect = e; } Scn na(String r) { na = r; return this; } Scn quest(String q) { quest = q; return this; } }
    static final LinkedHashMap<String, Scn> SCN = new LinkedHashMap<>();
    static void scn(String type, Setup s, Effect e) { SCN.put(type, new Scn(s, e)); }
    /** A row for a quest that a person starts by another command than qadd (the setup returns that command after "!!"); `quest` is the name it makes. */
    static void scnX(String key, String quest, Setup s, Effect e) { SCN.put(key, new Scn(s, e).quest(quest)); }
    static void na(String type, String why) { SCN.put(type, new Scn(null, null).na(why)); }

    static String snap() {
        int pills = 0; for (Item i : p.pillBag) pills += i.quantity;
        int mats = 0; for (Item i : p.materialBag) mats += i.quantity;
        int rel = 0; for (Relationship r : p.relationshipList.copy()) rel += r.value;
        return "ss=" + PlayerLists.wealth(p) + " bag=" + p.backpack.size() + " pills=" + pills + " mats=" + mats + " lvl=" + p.lvl + " tech=" + p.techniqueList.size()
                + " sp=" + p.sectPoints + " pot=" + p.potential + " kills=" + p.kills + " rels=" + rel + " sect=" + (p.sect == null ? "-" : p.sect.name) + " arrays=" + p.arrayBag.size() + " shops=" + p.ownedShops.size() + " beasts=" + Taming.beasts(p).size() + " party=" + p.squadSize() + " spouse=" + (p.spouse != null) + " preg=" + p.pregnant + " mission=" + (p.sectMission != null) + " kids=" + p.children.size() + " dead=" + p.dead + " lives=" + p.lives;
    }

    static int mineUid = -1;
    static Quest find(String type) {
        for (Quest q : p.targetQuests) if (q.name.equals(type) && (mineUid < 0 || q.uid == mineUid)) return q;
        for (Quest q : p.spotQuests) if (q.name.equals(type) && (mineUid < 0 || q.uid == mineUid)) return q;
        if (p.currentQuest != null && p.currentQuest.name.equals(type) && (mineUid < 0 || p.currentQuest.uid == mineUid)) return p.currentQuest;
        return null;
    }

    static final String[] NOISE = {"| MANUAL", "next:", "try:", "[Quest Alert]", "[Notice]", "=== Spring", "=== Summer", "=== Autumn", "=== Winter"};

    static String exec(String cmd) throws Exception {
        PcProbe.cap.reset();
        PcProbe.cmd(cmd, 120);
        String raw = PcProbe.out();
        PcProbe.cap.reset();
        StringBuilder sb = new StringBuilder();
        Character me = PcProbe.pc();
        String nm = me == null ? "@@" : me.name + " " + me.lastName + " - ";
        outer:
        for (String line : raw.split("\n")) {
            String t = line.trim();
            if (t.isEmpty() || t.equals(">") || t.startsWith(nm)) continue;
            for (String n : NOISE) if (t.contains(n)) continue outer;
            if (t.matches("^\\[.*\\|.*\\|.*\\]\\s*$")) continue;
            if (t.contains("awakened as a Monster") || t.contains("more beasts awakened") || t.contains("recap") || t.startsWith("It's year") || t.contains("Celestial Trial") || t.contains("Heaven's Will") || t.contains("has founded")
                    || t.startsWith("[EPOCH]") || t.contains("Convocation") || t.startsWith("[WORLD]") || t.contains("holds its") || t.contains("Beast Tide") || t.contains("Heaven fire") || t.contains("plague")
                    || t.startsWith("You:  level") || t.startsWith("Wealth:") || t.startsWith("Family &") || t.startsWith("What happened") || t.startsWith("•") || t.startsWith("Quest dropped") || t.contains("Overcrowding turns") || t.matches(".* is in the .* at x: \\d+, y: \\d+.*") || t.startsWith("=== Year") || t.contains("has awakened") || t.startsWith("(")) continue; // (the world's own news, printed in the same week)
            sb.append(t).append("\n");
        }
        return sb.toString().trim();
    }

    static Result trial(String type) throws Exception {
        Result r = new Result(); r.type = type;
        Scn sc = SCN.get(type);
        if (sc == null) { r.verdict = "N/A"; r.reason = "no scenario written"; return r; }
        if (sc.na != null) { r.verdict = "N/A"; r.reason = sc.na; return r; }
        Character live = PcProbe.pc();
        if (live != null) p = live; // (a ghost's rebirth and a possessed womb hand the player to a new body)
        baseState();
        String args;
        try { args = sc.setup.go(); } catch (RuntimeException e) { r.verdict = "N/A"; r.reason = "setup failed: " + e; return r; }
        if (args == null) args = "";
        String before = snap();
        int t0 = GameDriver.time;
        StringBuilder all = new StringBuilder();
        final String qname = sc.quest != null ? sc.quest : type;
        String made = exec(args.startsWith("!!") ? args.substring(2) : "qadd " + type + (args.isEmpty() ? "" : " " + args) + " p=70");
        all.append("qadd: ").append(made.replace('\n', '|')).append('\n');
        mineUid = -1;
        Quest q = find(qname);
        if (q != null) mineUid = q.uid;
        r.started = q != null ? "yes" : "no";
        if (q == null) {
            r.verdict = "FAIL"; r.reason = "qadd did not make the quest: " + made.replace('\n', '|'); r.out = all.toString();
            return r;
        }
        boolean spot = q.category == Quest.QuestCategory.Spot;
        int qdoWeeks = 0, goWeeks = 0, qdos = 0, steps = 0;
        boolean finished = false;
        StringBuilder results = new StringBuilder();
        for (; steps < 40; steps++) {
            q = find(qname);
            if (q == null) { finished = true; break; }
            if (PcProbe.pc() == null || PcProbe.pc().dead) { results.append("(the character died)"); finished = true; break; }
            Area dest = null;
            if (!spot) {
                if (q.category == Quest.QuestCategory.Character) { Character tg = Character.charById(q.targetCharacter); dest = tg == null ? null : tg.location; }
                else dest = q.targetArea;
            }
            int w0 = GameDriver.time;
            String o;
            if (dest != null && dest != p.location) { o = exec("goquest #" + q.uid); goWeeks += GameDriver.time - w0; all.append("goquest: ").append(o.replace('\n', '|')).append('\n'); continue; }
            o = exec("qdo #" + q.uid);
            qdos++;
            qdoWeeks += GameDriver.time - w0;
            all.append("qdo: ").append(o.replace('\n', '|')).append(" [").append(GameDriver.time - w0).append("w]\n");
            if (o.length() > 0) results.append(o).append('\n');
            if (o.contains("couldn't be done") || o.contains("can't be done") || o.contains("No quest to do") || o.contains("is no longer in your queue") || o.contains("the target is gone")) {
                if (steps >= 3 && !o.contains("go to")) break; // refused again and again
            }
        }
        r.out = all.toString();
        String after = snap();
        boolean changed = !before.equals(after);
        boolean effect = sc.effect != null && sc.effect.happened();
        r.cost = qdos + " qdo, " + qdoWeeks + " wk" + (goWeeks > 0 ? " (+" + goWeeks + " wk walking)" : "");
        String shown = results.toString().trim();
        if (!finished) { r.verdict = "STUCK"; r.reason = "still in the Quest Log after " + steps + " tries; last: " + lastLine(all); return r; }
        if (all.toString().contains("Exception") || all.toString().contains("at Game.")) { r.verdict = "FAIL"; r.reason = "exception: " + firstLine(all, "Exception"); return r; }
        if (shown.contains("You reached " + qname + ".")) { r.verdict = "FAIL"; r.reason = "only a waypoint: 'You reached " + qname + ".' and the quest is removed; nothing of the quest was done (its handler is a spot quest, the New quest window made it a place quest)"; return r; }
        boolean did = effect || (sc.effect == null && changed);
        if (did && shown.isEmpty()) { r.verdict = "FAIL"; r.reason = "silent: it changed something (" + diff(before, after) + ") and printed nothing"; return r; }
        if (!did && !changed && shown.isEmpty()) { r.verdict = "FAIL"; r.reason = "done with nothing done and nothing said (the quest is removed as if it had worked)"; return r; }
        if (!did && sc.effect != null && !changed) { r.verdict = "FAIL"; r.reason = "removed as done but the expected effect did not happen; it said: " + shown.replace('\n', '|'); return r; }
        r.verdict = "PASS"; r.reason = shown.isEmpty() ? "changed: " + diff(before, after) : shown.split("\n")[0];
        return r;
    }

    static String lastLine(CharSequence s) { String[] l = s.toString().trim().split("\n"); return l[l.length - 1]; }
    static String firstLine(CharSequence s, String key) { for (String l : s.toString().split("\n")) if (l.contains(key)) return l; return ""; }
    static String diff(String a, String b) {
        String[] x = a.split(" "), y = b.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x.length && i < y.length; i++) if (!x[i].equals(y[i])) sb.append(x[i]).append("->").append(y[i]).append(' ');
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------ the scenarios
    static String id(Character c) { return "#" + c.id; }
    static String xy(Area a) { return "at " + a.x + " " + a.y; }

    static void scenarios() {
        // ---- spot quests
        scn("Meditate", () -> "", () -> p.cultivationBase > 0);
        scn("Forage", () -> { tp(p, W); return ""; }, () -> true);
        scn("Mine", () -> { tp(p, W); return ""; }, () -> true);
        scn("Profession", () -> { p.trait = Character.Traits.Alchemist; tp(p, W); return ""; }, null);
        scn("Hunt", () -> { tp(p, B); return ""; }, () -> B.monsters < 80);
        scn("Hunt Cultivator", () -> { tp(weak, W); tp(p, W); return ""; }, null);
        scn("Loot Corpse", () -> { Character v = npc(H, "Male", 30, Character.Traits.Friendly); v.ss = 5000; v.backpack.add(new Item(v, Item.WeaponType.Sword, 20)); v.deathCause = "probe"; v.dead(); return ""; }, null);
        scn("Random", () -> "", null);
        scn("Reincarnate", () -> { p.lvl = 101; p.majorLvl = 10; p.minorLvl = 1; return ""; }, () -> p.lives > 0);
        scn("Accept Sect Mission", () -> { sect.forceJoin(p); tp(p, S); sect.questBoard.add("Forage|Herb|2|50|10"); DecisionBroker.scripted.add(0); return ""; }, () -> p.sectMission != null);

        // ---- person quests
        scn("Find Person", () -> id(friend), null);
        scn("Battle Dislike", () -> id(enemy), null);
        scn("Battle Rival", () -> id(rival), null);
        scn("Battle Arch-Enemy", () -> id(arch), null);
        scn("Revenge", () -> { bond(p, arch, -4); return id(arch); }, null);
        scn("Courtship", () -> { bond(p, friend, 3); return id(friend); }, () -> p.relationshipList.contains(friend.id).value >= 3);
        scn("Gift Technique", () -> id(friend), () -> friend.techniqueList.size() > 0);
        scn("Visit Family", () -> { p.children.add(kin); kin.mother = p; return id(kin); }, null);
        scn("Teach Disciple", () -> id(disciple), null);
        scn("Give Gift", () -> { p.backpack.add(new Item(p, 20, Item.ItemType.Clothing)); return id(friend); }, null);
        scn("Propose Alliance", () -> { sect.forceJoin(p); sect.master = p; tp(p, S2); PcProbe.real.println("[T] info: alliance refusal=" + sect2.allianceRefusal(sect) + " atWar=" + sect.isAtWar() + " ally=" + sect.isAlly(sect2) + " master2isMaster=" + sect2.isMaster(master2) + " master2.sect==sect2 " + (master2.sect == sect2)); return id(master2); }, () -> sect.isAlly(sect2));
        scn("Recruit Defector", () -> { sect.forceJoin(p); sect.warTarget = sect2; sect2.warTarget = sect; sect2.promoteRank(memberB); sect2.promoteRank(memberB); tp(p, S2); return id(memberB); }, () -> memberB.sect == sect);
        scn("Recruit Member", () -> { sect.forceJoin(p); sect.master = p; tp(p, S); PcProbe.real.println("[T] info: canJoin=" + sect.canJoin(outsider) + " why=" + sect.whyNot(outsider) + " outsider.sect=" + outsider.sect); return id(outsider); }, () -> outsider.sect == sect);
        scn("Seek Teacher", () -> { teacher.techniqueList.add(new Technique(Technique.techniqueType.Martial, teacher, 90)); return id(teacher); }, null);
        scn("Get Pregnant", () -> { p.gender = "Female"; p.spouse = husband; husband.spouse = p; tp(husband, H); for (int i = 0; i < 1; i++) { } return id(husband); }, () -> p.pregnant);
        scn("Demand Tribute", () -> { sect.forceJoin(p); p.lvl = 100; p.majorLvl = 9; tp(p, S2); memberB.ss = 20_000_000L; return id(memberB); }, () -> memberB.ss < 20_000_000L);
        scn("Refine Blood", () -> { alch.professionLvl = 30; p.materialBag.add(new Item(Item.ItemType.Blood, 4000)); return id(alch); }, () -> alch.pillBag.size() > 0);
        scn("Rob Caravan", () -> id(merch), null);
        scn("Possess Womb", () -> { p.trait = Character.Traits.Ghost; wife.spouse = husband; husband.spouse = wife; Character unborn = new Character(wife); Map.addTemp(unborn); p.lvl = 60; return id(wife); }, null);

        // ---- place quests
        scn("Bounty Hunt", () -> { H.bounties.add(new Quest("Bounty Hunt", true, 40, 0, 40, enemy)); return id(enemy); }, null);
        scn("Explore Dungeon", () -> { D1.dungeonType = Area.DungeonType.HAUNTED_GROUNDS; return xy(D1); }, null);
        scn("Hunt Beast", () -> xy(B), () -> B.monsters < 80);
        scn("Heaven Treasure", () -> { T.heavenItem = new Item(p, 60, Item.ItemType.Clothing); T.heavenItem.heavenSent = true; return xy(T); }, () -> T.heavenItem == null);
        scn("Join Sect", () -> xy(S), () -> p.sect == sect);
        scn("Return to Sect", () -> { sect.forceJoin(p); return xy(S); }, () -> p.sectPoints > 0);
        scn("Donate to Sect", () -> { sect.forceJoin(p); p.backpack.add(new Item(p, Item.WeaponType.Sword, 20)); tp(p, S); return ""; }, () -> p.sectPoints > 0);
        scn("Donate Array", () -> { sect.forceJoin(p); p.arrayBag.add(new Item(p, 30, Item.ItemType.Array)); return xy(S); }, () -> p.arrayBag.isEmpty());
        scn("Buy from Sect", () -> { sect.forceJoin(p); p.sectPoints = 100000; Item w = new Item(master, Item.WeaponType.Sword, 90); sect.treasury.add(w); tp(p, S); return ""; }, () -> p.sectPoints < 100000);
        scn("Buy Sect Array", () -> { sect.forceJoin(p); sect.master = p; tp(p, S); return ""; }, () -> S.arrayBag.size() > 0);
        scn("Deliver Materials", () -> { sect.forceJoin(p); p.sectMission = "Forage|Herb|2|500|10"; p.sectMissionBonus = 10; return xy(S); }, () -> p.sectMission == null);
        scn("Deliver Goods", () -> { sect.forceJoin(p); p.sectMission = "Deliver|Food|1|1|10"; p.sectMissionBonus = 10; Life.goods(p).add(new Item(Item.ItemType.Food, 1, 5, "Probe Rice")); tp(p, S); return xy(S); }, () -> p.sectMission == null || sect.treasury.size() > 0);
        scn("Sect Raid", () -> { sect.forceJoin(p); sect.warTarget = sect2; sect2.warTarget = sect; return xy(S2); }, () -> p.sectPoints > 0);
        na("Sect Mission", "given by the game from the sect's board (Accept Sect Mission); 'qadd' refuses it by design");
        scn("Study Trip", () -> { C2.libraries.get(0).techniques.add(new Technique(Technique.techniqueType.Martial, master, 99)); return xy(C2); }, () -> p.techniqueList.size() > 1);
        scn("Buy City Array", () -> { C2.cityOwner = p; tp(p, C2); return ""; }, () -> C2.arrayBag.size() > 0);
        scn("Entice Merchant", () -> { C2.cityOwner = p; tp(merch, W); tp(p, C2); return ""; }, () -> p.ss < 20_000_000L);
        scn("Hold Tournament", () -> { tp(p, H); return ""; }, () -> H.tournamentTimer > 0);
        scn("Register Tournament", () -> { TT.tournamentTimer = 8; TT.tournamentMinMajor = 0; TT.tournamentMaxMajor = 10; TT.tournamentRegistrants.clear(); tp(p, TT); return ""; }, () -> TT.tournamentRegistrants.contains(p));
        scn("Spectate Tournament", () -> { TT.tournamentTimer = 8; TT.tournamentMinMajor = 0; TT.tournamentMaxMajor = 10; TT.tournamentRegistrants.clear(); Tournaments.clearSeats(TT); tp(p, TT); return ""; }, () -> Tournaments.spectators(TT, new ArrayList<Character>()).contains(p));
        scn("Buy Gear", () -> { C2.backpack.add(new Item(master, Item.WeaponType.Sword, 99)); return xy(C2); }, () -> p.equippedWeapon != null && p.equippedWeapon.lvl > 50);
        scn("Buy Pills", () -> { C2.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Cultivation, p.majorLvl + 1, 5)); return xy(C2); }, null);
        scn("Seek Potential", () -> { C2.pillBag.add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 3)); return xy(C2); }, () -> p.potential > 80 || p.potentialProgress > 0);
        scn("Go Sell", () -> { for (int i = 0; i < 3; i++) p.backpack.add(new Item(p, Item.WeaponType.Sword, 20 + i)); return xy(C2); }, () -> p.backpack.size() < 5);
        scn("Sell Stock", () -> { for (int i = 0; i < 3; i++) p.backpack.add(new Item(p, Item.WeaponType.Sword, 20 + i)); return xy(C2); }, () -> p.backpack.size() < 5);
        scn("Sell Technique", () -> { p.techniqueList.add(new Technique(Technique.techniqueType.Martial, p, 90)); return xy(C2); }, null);
        scn("Restock Trip", () -> { p.trait = Character.Traits.Merchant; return xy(C2); }, () -> p.ownedShops.size() > 0);
        scn("Perform", () -> { p.trait = Character.Traits.Musician; p.backpack.add(new Item(p, 30, Item.ItemType.Instrument)); return xy(C2); }, null);
        scn("Festival Trade", () -> xy(C2), null);
        scn("Attend Auction", () -> { C2.auction().add(new Item(Item.ItemType.Pill, Item.PillType.Potential, 11, 1)); C2.auctionSince = GameDriver.time; return xy(C2); }, null);
        scn("Market Trip", () -> { C2.goodsBag.add(new Item(Item.ItemType.Food, 1, 20, "Probe Rice")); return xy(C2); }, null);
        scn("Trade Run", () -> { C2.cityTreasury = 3_000_000; return xy(C2); }, null);
        scn("Courier Run", () -> xy(C2), null);
        scn("Meditation Retreat", () -> xy(QT), null);
        scn("Pilgrimage", () -> xy(QT), null);
        scn("Sightseeing", () -> xy(QT), null);
        scn("Gathering Trip", () -> { p.trait = Character.Traits.Gatherer; return xy(W); }, null);
        scn("Tame Beast", () -> { p.trait = Character.Traits.Tamer; return xy(B); }, () -> Taming.beasts(p).size() > 0);
        scn("Seek Territory", () -> xy(B), null);
        scn("Disperse", () -> xy(QT), null);
        scn("Defend", () -> { sect.forceJoin(p); S.siegeUntil = GameDriver.time + 20; S.siegeBy = outsider.id; return xy(S); }, null);
        scn("Find Parent's Body", () -> xy(T), null);
        scn("Ghost City Rebirth", () -> { p.trait = Character.Traits.Ghost; return xy(C2); }, () -> p.dead);
        scn("Sell Technique", () -> { p.techniqueList.add(new Technique(Technique.techniqueType.Martial, p, 90)); return xy(C2); }, null);

        // ---- quests a person starts another way (seek, the dungeon kinds, found)
        scnX("seek talk (Talk To)", "Talk To", () -> "!!seek " + friend.id + " talk", null);
        scnX("seek friend (Befriend)", "Befriend", () -> "!!seek " + stranger.id + " friend", null);
        scnX("seek fight (Duel)", "Duel", () -> "!!seek " + rival.id + " fight", null);
        scnX("seek spar (Spar)", "Spar", () -> "!!seek " + friend.id + " spar", null);
        scnX("seek court (Courtship)", "Courtship", () -> { p.gender = "Male"; friend.gender = "Female"; bond(p, friend, 3); return "!!seek " + friend.id + " court"; }, null);
        scnX("seek kill (Hunt Down)", "Hunt Down", () -> { DecisionBroker.scripted.add(0); return "!!seek " + enemy.id + " kill"; }, () -> enemy.dead);
        scnX("explore Lost Ruins", "Explore Dungeon", () -> { D1.dungeonType = Area.DungeonType.LOST_RUINS; D1.dungeonCharges = 5; return "!!qadd Explore Dungeon " + xy(D1) + " p=70"; }, () -> p.ss > 20_000_000L);
        scnX("explore Beast Lair", "Explore Dungeon", () -> { D1.dungeonType = Area.DungeonType.BEAST_LAIR; D1.monsters = 30; return "!!qadd Explore Dungeon " + xy(D1) + " p=70"; }, () -> D1.monsters < 30);
        scnX("explore Martial Trial", "Explore Dungeon", () -> { D1.dungeonType = Area.DungeonType.MARTIAL_TRIAL; return "!!qadd Explore Dungeon " + xy(D1) + " p=70"; }, null);
        scnX("explore Celestial Trial", "Explore Dungeon", () -> { D1.dungeonType = Area.DungeonType.CELESTIAL_TRIAL; D1.celestialTrialTimer = 10; return "!!qadd Explore Dungeon " + xy(D1) + " p=70"; }, null);
    }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(a.length > 0 ? String.join(" ", a) : null); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    static void run(String only) throws Exception {
        table = new PrintStream(new java.io.FileOutputStream("questmatrix.txt"), true, "UTF-8");
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        p = PcProbe.pc();
        if (p == null) { say("no character"); return; }
        Settings.lore = false; Settings.fightView = false;
        p.pc().breakthroughPolicy = PlayerControl.BT_AUTO;
        exec("duties all off");
        GameDriver.time = 400; // (the first year's truce in Originis is over)
        build();
        scenarios();
        int pass = 0, fail = 0, stuck = 0, na = 0;
        List<Result> results = new ArrayList<>();
        say("type | verdict | started | cost | reason");
        List<String[]> order = new ArrayList<>();
        for (String[] qt : PlayerControl.QUEST_TYPES) if (!qt[0].equals("Possess Womb") && !qt[0].equals("Ghost City Rebirth")) order.add(qt); // (these two hand the player to a new body: last)
        for (String k : new ArrayList<>(SCN.keySet())) { boolean listed = false; for (String[] qt : PlayerControl.QUEST_TYPES) if (qt[0].equals(k)) listed = true; if (!listed) order.add(new String[]{k, "0"}); }
        for (String[] qt : PlayerControl.QUEST_TYPES) if (qt[0].equals("Possess Womb") || qt[0].equals("Ghost City Rebirth")) order.add(qt);
        for (String[] qt : order) {
            if (only != null && !qt[0].equalsIgnoreCase(only)) continue;
            Result r;
            try { r = trial(qt[0]); } catch (Throwable e) { r = new Result(); r.type = qt[0]; r.verdict = "FAIL"; r.reason = "harness/game exception " + e; e.printStackTrace(PcProbe.real); }
            results.add(r);
            say(r.type + " | " + r.verdict + " | " + r.started + " | " + r.cost + " | " + r.reason);
            if (PcProbe.in.blocked == false && PcProbe.gameError != null) break;
            if (r.verdict.equals("PASS")) pass++; else if (r.verdict.equals("STUCK")) stuck++; else if (r.verdict.equals("N/A")) na++; else fail++;
            PcProbe.real.println("[T]    log: " + r.out.replace('\n', ' ').substring(0, Math.min(500, r.out.replace('\n', ' ').length())));
            table.println("    LOG: " + r.out.replace("\n", "\n         "));
        }
        say("TOTAL " + results.size() + " types: PASS " + pass + ", FAIL " + fail + ", STUCK " + stuck + ", N/A " + na);
    }
}
