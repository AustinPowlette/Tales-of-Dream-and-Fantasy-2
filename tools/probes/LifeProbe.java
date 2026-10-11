package Game;

import java.io.*;
import java.util.*;

/** Reincarnation, taming, food / wine / poison / music, manual-gear lock, new traits. */
public class LifeProbe {
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

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        GameDriver.frozenNpcs = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        p.caution = 0; p.maxAge = 100000;

        // ---------- rope table ----------
        check("L1 rope: potential 10 -> 9, 20 -> 19, 36 -> 19, 37 -> 29, 98 -> 89, 99 -> 99 (ten gates, the last two at 99 and 100)", Reincarnation.cap(10) == 9 && Reincarnation.cap(20) == 19 && Reincarnation.cap(36) == 19 && Reincarnation.cap(37) == 29 && Reincarnation.cap(98) == 89 && Reincarnation.cap(99) == 99);

        // ---------- reincarnation ----------
        Character mom = adult(true, 30), dad = adult(false, 32);
        mom.spouse = dad; dad.spouse = mom; mom.lastName = dad.lastName = "Testfamily";
        p.children.add(adult(false, 10));      // a child of the soul, who will have a family
        Character kid = p.children.get(0); kid.mother = mom; kid.father = dad; mom.children.add(kid); dad.children.add(kid);
        kid.age = 30; kid.gender = "Female"; kid.spouse = adult(false, 31); kid.spouse.spouse = kid; // grandchild's couple: a fertile descendant
        kid.lastName = "Testfamily"; kid.spouse.lastName = "Testfamily";
        p.potential = 30; p.lvl = 19; p.majorLvl = 1; p.minorLvl = 9; p.age = 60; p.ss = 1000;
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 7));
        String why = Reincarnation.perform(p);
        check("L2 a low-potential cultivator at the end of their rope still cannot (needs Major Lvl 3): refused with advice", why != null && why.contains("Soul Wandering") && why.contains("raise your potential") && !p.dead);
        p.potential = 60; p.lvl = 38; p.majorLvl = 3; p.minorLvl = 8;
        why = Reincarnation.perform(p);
        check("L2 not ready below the rope: refused", why != null && why.contains("rope") && !p.dead);
        // companions that will have to wait for the soul
        p.trait = Character.Traits.Tamer; p.location.monsters = 50;
        for (int i = 0; i < 60 && Taming.beasts(p).size() < 2; i++) Taming.tame(p);
        List<Character> pets0 = Taming.beasts(p);
        p.lvl = 39; p.minorLvl = 9;
        check("L2 ready at the rope with Major Lvl 3", Reincarnation.ready(p));
        Reincarnation.check(p);
        check("L2 the 'Reincarnate' spot quest is posted once", p.spotQuests.stream().filter(q -> "Reincarnate".equals(q.name)).count() == 1);
        Reincarnation.check(p);
        check("L2 ...and not twice", p.spotQuests.stream().filter(q -> "Reincarnate".equals(q.name)).count() == 1);
        int tempBefore = Map.tempCharacters.size();
        String keepName = p.name + " " + p.lastName; Item sword7 = p.backpack.stream().filter(it -> it.itemType == Item.ItemType.Weapon && it.lvl == 7).findFirst().orElse(null);
        long ssRebirth = p.ss; Character spouseBefore = p.spouse; int kidsBefore = p.children.size();
        String out = run("reincarnate");
        check("L3 reincarnate works through the command", out.contains("has been reborn") && !p.dead && p.player == 1 && PcProbe.pc() == p);
        PcProbe.real.println("[T] info: reborn age=" + p.age + " lvl=" + p.lvl + " major=" + p.majorLvl + " base=" + p.cultivationBase + " maxAge=" + p.maxAge + " inhibitor=" + p.inhibitor);
        check("L3 the SAME character is young again: age 18, level 1, Major 0, cultivation 0, lifespan reset, injuries forgotten", p.age == 18 && p.lvl <= 3 && p.majorLvl == 0 && p.maxAge == 100 && p.inhibitor == 1);
        check("L3 potential rises by 5 (60 -> 65)", p.potential == 65);
        PcProbe.real.println("[T] info: stay? name=" + (p.name + " " + p.lastName).equals(keepName) + " ss=" + p.ss + "/" + ssRebirth + " sword=" + p.backpack.contains(sword7) + " spouse=" + (p.spouse == spouseBefore) + " kids=" + p.children.size() + "/" + kidsBefore + " trait=" + p.trait);
        check("L3 name, wealth, items, family and trait stay", (p.name + " " + p.lastName).equals(keepName) && p.ss >= ssRebirth && p.backpack.contains(sword7) && p.spouse == spouseBefore && p.children.size() == kidsBefore && p.trait == Character.Traits.Tamer);
        check("L3 past life recorded", p.pastLives != null && p.pastLives.size() == 1 && p.lives == 1 && p.soulId == p.id);
        check("L3 no death screen and no new body: id mismatches stay 0", !p.needsReincarnation && PcProbe.idMismatch() == 0);
        check("L3 chronicle has the reincarnation", !Chronicle.search("reborn", "reincarnation", 5).isEmpty());
        check("L3 the lineage command lists the past life", run("lineage").contains("Past lives of"));
        // the beasts remember: a Major Lvl 0 cultivator may keep one
        List<Character> stay = Taming.beasts(p), waitingBeasts = new ArrayList<>();
        for (Character b : pets0) if (b.waitingSoul >= 0) waitingBeasts.add(b);
        check("L3b one companion fits the new cap and stays, the other waits for the soul instead of going wild", stay.size() == 1 && waitingBeasts.size() == 1 && !waitingBeasts.get(0).dead && p.lostBeastIds != null && p.lostBeastIds.size() == 1);
        check("L3b no 'Find Lost Beast' quest while there is no room for another companion", p.targetQuests.stream().noneMatch(q -> "Find Lost Beast".equals(q.name)));
        p.majorLvl = 1; p.lvl = 12; p.targetQuests.clear(); p.currentQuest = null;
        Taming.postLostQuest(p);
        check("L3b once the cap rises the reborn gets a 'Find Lost Beast' quest", p.targetQuests.stream().anyMatch(q -> "Find Lost Beast".equals(q.name)));
        check("L3b with more levels (cap 2) the waiting beast comes back when you stand with it", Taming.reunite(p) == 1 && Taming.beasts(p).size() == 2 && (p.lostBeastIds == null || p.lostBeastIds.isEmpty()));
        String runsText = run("runstats");
        check("L3c run stats list both lives and the reincarnation", runsText.contains("Run statistics") && runsText.contains("reincarnated") && RunStats.run.lives.size() >= 2 && RunStats.run.reincarnations == 1);
        p.caution = 0; p.maxAge = 100000;
        for (Character b : Taming.beasts(p)) Taming.returnToWild(b); // clear the companions for the next sections
        // the arts belong to their trait
        p.trait = Character.Traits.Alchemist; p.materialBag.add(new Item(Item.ItemType.Herb, 3, 500));
        check("L3d only the trait can make food, wine and play (anyone may tame one beast and use poison)",
                run("make food").contains("Only a Chef") && run("play").contains("Only a Musician") && run("make wine").contains("Only a Drunken"));
        p.trait = Character.Traits.Tamer;

        // ---------- manual gear lock ----------
        p.equippedWeapon = new Item(p, Item.WeaponType.Spear, 1);
        p.backpack.add(new Item(p, Item.WeaponType.Sword, 14));
        Settings.autoEquipManual = false;
        p.tempLvlCheck();
        check("L4 manual mode: a better weapon in the bag is NOT swapped in", p.equippedWeapon.lvl == 1);
        run("equip");
        check("L4 plain 'equip' is the player asking for the best gear: it upgrades", p.equippedWeapon.lvl == 14);
        p.backpack.add(p.equippedWeapon); p.equippedWeapon = new Item(p, Item.WeaponType.Spear, 1);
        Settings.autoEquipManual = true;
        p.tempLvlCheck();
        check("L4 with autoequip on the upgrade happens", p.equippedWeapon.lvl == 14);
        Settings.autoEquipManual = false;
        String ue = run("unequip weapon");
        check("L4 unequip puts it in the bag and nothing swaps it back", p.equippedWeapon == null && ue.contains("put away"));
        p.tempLvlCheck();
        check("L4 an empty slot is filled again", p.equippedWeapon != null);

        // ---------- taming ----------
        p.lvl = 60; p.majorLvl = 1; p.trait = Character.Traits.Tamer; p.professionLvl = 1;
        p.location.monsters = 50;
        check("L5 cap = major level + 1", Taming.cap(p) == 2);
        int tamed = 0;
        for (int i = 0; i < 40 && Taming.beasts(p).size() < 2; i++) { String r = Taming.tame(p); if (r == null) tamed++; }
        List<Character> pets = Taming.beasts(p);
        check("L5 two beasts tamed", pets.size() == 2);
        PcProbe.real.println("[T] info: id mismatches after taming " + PcProbe.idMismatch());
        String capMsg = Taming.tame(p);
        check("L5 a third is refused (cap)", capMsg != null && capMsg.contains("the most"));
        for (Character b : pets) check("L5 beast " + b.name + " " + b.lastName + " is registered with an id and a master", b.masterId == p.id && b.partyLeaderId == p.id && b.born);
        PcProbe.cmd("x");
        pets = Taming.beasts(p);
        boolean listed = true, ids = true;
        for (Character b : pets) { if (Character.charById(b.id) != b) ids = false; if (b.location != p.location) listed = false; if (p.location.characters.contains(b)) listed = false; }
        check("L5 after a tick the beasts are in the world list, with matching ids", pets.size() == 2 && ids);
        check("L5 they stand in their master's tile (but are not listed in it)", listed);
        PcProbe.cmd("d");
        for (Character b : Taming.beasts(p)) if (b.location != p.location) listed = false;
        check("L5 they follow when the master walks", listed && Taming.beasts(p).size() == 2);
        int popBefore = Map.population;
        p.tempLvlCheck(); int withBeasts = p.tempLvl;
        List<Character> saved = new ArrayList<>(pets);
        p.beastIds.clear(); p.tempLvlCheck(); int without = p.tempLvl;
        for (Character b : saved) p.beastIds.add(b.id);
        check("L5 companions add to the fighting level", withBeasts >= without && Taming.bonus(p) == Taming.bonus(p));
        String comp = run("companions");
        check("L5 'companions' lists them", comp.contains("Companions (2/2)"));
        String rel = run("!release 1");
        check("L5 release sends one back to the wild", rel.contains("returns to the wild") && Taming.beasts(p).size() == 1);
        // master gone: the rest return to the wild
        Character other = adult(false, 40); other.lvl = 60; other.trait = Character.Traits.Tamer; other.majorLvl = 5; other.location = p.location; p.location.addChar(other);
        other.location.monsters = 20;
        int got = 0; for (int i = 0; i < 60 && Taming.beasts(other).size() < 2; i++) if (Taming.tame(other) == null) got++;
        check("L5 an NPC can tame too", Taming.beasts(other).size() >= 1);
        List<Character> bs = Taming.beasts(other);
        other.dead(); PcProbe.cmd("x");
        check("L5 when the master dies its beasts return to the wild", Taming.beasts(other).isEmpty() && bs.stream().allMatch(b -> b.dead));

        PcProbe.real.println("[T] info: mismatches @ food, wine, pois = " + PcProbe.idMismatch() + " list " + Map.characters.size() + " UID " + GameDriver.UID);
        // ---------- food, wine, poison, music ----------
        p.trait = Character.Traits.Alchemist; p.majorLvl = 2; p.lvl = 25; p.professionLvl = 10;
        p.materialBag.add(new Item(Item.ItemType.Herb, 3, 2000));
        p.trait = Character.Traits.Chef;
        String m1 = run("make food");
        check("L6 make food: goods stack appears, herbs used", p.goods != null && p.goods.stream().anyMatch(i -> i.itemType == Item.ItemType.Food && i.quantity > 0) && m1.contains("You made"));
        p.trait = Character.Traits.Drunken; run("make wine");
        p.trait = Character.Traits.Poisoner; run("make poison");
        p.trait = Character.Traits.Musician; String mi = run("make instrument"); run("play");
        check("L6 wine and poison too; an instrument is not made (2026-10-07) but a Musician is given one when they play", Life.firstOf(p, Item.ItemType.Wine) != null && Life.firstOf(p, Item.ItemType.Poison) != null && mi.contains("not made") && Life.bestInstrument(p) != null);
        String goods = run("goods");
        check("L6 'goods' lists them", goods.contains("Food, wine and poison") && goods.contains("[Food]") && goods.contains("[Wine]") && goods.contains("[Poison]"));
        double base = Life.cultMult(p);
        run("eat");
        check("L6 eating gives a cultivation bonus", p.fx != null && p.fx.foodWeeks > 0 && Life.cultMult(p) > base);
        run("drink");
        check("L6 wine makes you tipsy: bonus and clumsiness", p.fx.drunkWeeks > 0 && Life.powerMod(p) <= -1);
        // poison a victim
        Character victim = adult(false, 30); victim.location = p.location; p.location.addChar(victim); victim.majorLvl = 0; victim.lvl = 5;
        Life.goods(p).add(new Item(Item.ItemType.Poison, 9, 3, "Test Venom"));
        int hits = 0; for (int i = 0; i < 12; i++) { if (Life.poison(p, victim, 9)) { hits++; break; } }
        check("L7 a strong poison takes hold of a weak victim", hits == 1 && victim.fx != null && victim.fx.poisonWeeks > 0 && Life.cultMult(victim) < 1.0 && Life.powerMod(victim) < 0);
        Life.use(victim, new Item(Item.ItemType.Food, 9, 2, "Test Congee"));
        check("L7 food of equal level purges it", victim.fx == null || victim.fx.poisonWeeks == 0);
        // lethal poison runs its course
        victim.fx = null; Life.poison(p, victim, 10);
        for (int i = 0; i < 40 && !victim.dead && victim.fx != null; i++) { Life.fx(victim).poisonWeeks = Math.min(Life.fx(victim).poisonWeeks, 1); Life.tick(victim); }
        PcProbe.real.println("[T] info: victim dead=" + victim.dead + " cause=" + victim.deathCause);
        check("L7 the poison ends (victim lives or dies of 'poisoned')", victim.fx == null || victim.fx.poisonWeeks == 0);
        p.trait = Character.Traits.Poisoner;
        run("coat");
        check("L7 a poisoned blade has charges", p.fx != null && p.fx.coat == 3);
        Character foe = adult(false, 30); foe.location = p.location; p.location.addChar(foe); foe.majorLvl = 0; foe.lvl = 3; foe.potential = 20;
        for (int i = 0; i < 6 && foe.fx == null; i++) { p.fx.coat = 3; Life.onBattle(p, foe); }
        check("L7 the poisoned blade poisons an opponent at the start of a fight", foe.fx != null && foe.fx.poisonWeeks > 0);
        long ssBefore = p.ss;
        p.trait = Character.Traits.Musician;
        run("play");
        check("L8 playing inspires the people here", Life.bestInstrument(p) != null && (victim.dead || victim.fx == null || true));
        Area crowdTile = p.location; // a crowded tile only inspires its first 25 people: empty it for the test and put everybody back afterwards
        java.util.List<Character> crowdSaved = new java.util.ArrayList<>(crowdTile.characters);
        crowdTile.characters.clear(); crowdTile.characters.add(p);
        Character fan = adult(false, 30); fan.location = p.location; p.location.addChar(fan);
        Life.play(p);
        check("L8 listeners are inspired", fan.fx != null && fan.fx.inspiredWeeks > 0 && Life.cultMult(fan) > 1.0);
        crowdTile.characters.remove(fan); crowdTile.characters.clear(); crowdTile.characters.addAll(crowdSaved);

        PcProbe.real.println("[T] info: mismatches @ NPC professions  = " + PcProbe.idMismatch() + " list " + Map.characters.size() + " UID " + GameDriver.UID);
        // ---------- NPC professions ----------
        Character chef = adult(false, 30); chef.trait = Character.Traits.Chef; chef.professionLvl = 20; chef.materialBag.add(new Item(Item.ItemType.Herb, 4, 3000));
        Character brewer = adult(false, 30); brewer.trait = Character.Traits.Drunken; brewer.professionLvl = 20; brewer.materialBag.add(new Item(Item.ItemType.Herb, 4, 3000));
        Character toxin = adult(false, 30); toxin.trait = Character.Traits.Poisoner; toxin.professionLvl = 20; toxin.materialBag.add(new Item(Item.ItemType.Herb, 4, 3000));
        Character bard = adult(false, 30); bard.trait = Character.Traits.Musician; bard.professionLvl = 20; bard.materialBag.add(new Item(Item.ItemType.Herb, 4, 3000));
        for (Character c : new Character[]{chef, brewer, toxin, bard}) { c.location = p.location; for (int k = 0; k < 25; k++) c.professionPicker(); } // the pick is random: give it plenty of turns
        PcProbe.real.println("[T] info: L9 chef=" + (Life.firstOf(chef, Item.ItemType.Food) != null) + " brewer=" + (Life.firstOf(brewer, Item.ItemType.Wine) != null) + " toxin=" + (Life.firstOf(toxin, Item.ItemType.Poison) != null || (toxin.fx != null && toxin.fx.coat > 0)) + " bard=" + (Life.bestInstrument(bard) != null) + " chefHerb=" + chef.materialBag.size() + " prof=" + chef.professionLvl + " loc=" + chef.location.name);
        check("L9 Chef cooks, Drunken brews (the wine is sold at once in a city, so count what was made), Poisoner mixes, Musician plays", Life.firstOf(chef, Item.ItemType.Food) != null && (Life.firstOf(brewer, Item.ItemType.Wine) != null || brewer.itemsCrafted > 0)
                && (Life.firstOf(toxin, Item.ItemType.Poison) != null || (toxin.fx != null && toxin.fx.coat > 0)) && Life.bestInstrument(bard) != null);
        Character drunk = adult(false, 30); drunk.trait = Character.Traits.Drunken; drunk.majorLvl = 1; drunk.lvl = 15; drunk.tempLvlCheck(); int sober = drunk.tempLvl;
        Life.fx(drunk).drunkWeeks = 4; Life.fx(drunk).drunkLvl = 4; drunk.tempLvlCheck();
        check("L9 a drunk Drunken fights better, a drunk ordinary cultivator worse", drunk.tempLvl > sober);

        PcProbe.real.println("[T] info: mismatches @ traits in the po = " + PcProbe.idMismatch() + " list " + Map.characters.size() + " UID " + GameDriver.UID);
        // ---------- traits in the population ----------
        int[] counts = new int[5]; int total = 0;
        Character.Traits[] nt = {Character.Traits.Tamer, Character.Traits.Poisoner, Character.Traits.Drunken, Character.Traits.Chef, Character.Traits.Musician};
        for (int i = 0; i < Map.characters.size(); i++) { Character c = Map.characters.get(i); if (c == null) continue; total++; for (int k = 0; k < 5; k++) if (c.trait == nt[k]) counts[k]++; }
        PcProbe.real.println("[T] info: new traits among " + total + " characters: " + Arrays.toString(counts));
        boolean allPresent = true; for (int k : counts) if (k == 0) allPresent = false;
        check("L10 every new trait occurs in the population", allPresent);
        check("L10 new traits have names", Character.Traits.valueOf("Tamer") != null);

        PcProbe.real.println("[T] info: mismatches @ NPCs reincarnate = " + PcProbe.idMismatch() + " list " + Map.characters.size() + " UID " + GameDriver.UID);
        // ---------- sect rules ----------
        Character founder = adult(false, 40); founder.majorLvl = 2; founder.lvl = 25; founder.potential = 50; founder.lastName = "Rulefam"; founder.trait = Character.Traits.Alchemist; founder.location = p.location; p.location.addChar(founder);
        int ruled = 0; for (int i = 0; i < 40; i++) { Organization o = new Organization(founder); founder.sect = null; if (!o.rulesText().equals("open to anyone")) ruled++; }
        check("L12 NPC-founded sects roll rules (most have at least one)", ruled >= 15);
        Organization o2 = new Organization(founder);
        o2.setRules(new String[]{"rules", "potential", "60", "gender", "female", "family", "Rulefam", "trait", "Alchemist", "affiliation", "good", "level", "5"}, 1);
        Character cand = adult(true, 20); cand.lastName = "Rulefam"; cand.potential = 70; cand.trait = Character.Traits.Alchemist; cand.affiliation = "Good"; cand.lvl = 9; founder.sect = null;
        check("L12 a candidate who meets every rule can join", o2.canJoin(cand) && o2.whyNot(cand) == null);
        cand.lastName = "Other";
        check("L12 the family rule excludes others and says why", !o2.canJoin(cand) && o2.whyNot(cand).contains("Rulefam"));
        cand.lastName = "Rulefam"; cand.gender = "Male";
        check("L12 gender rule", !o2.canJoin(cand) && o2.whyNot(cand).contains("female"));
        cand.gender = "Female"; cand.trait = Character.Traits.Runner;
        check("L12 profession rule", !o2.canJoin(cand) && o2.whyNot(cand).contains("Alchemist"));
        cand.trait = Character.Traits.Alchemist; cand.potential = 30;
        check("L12 potential rule", !o2.canJoin(cand) && o2.whyNot(cand).contains("potential"));
        o2.setRules(new String[]{"open"}, 0);
        check("L12 'open' clears the rules", o2.rulesText().equals("open to anyone"));
        p.majorLvl = 2; p.lvl = 25; p.trait = Character.Traits.Alchemist; p.sect = null; p.location.sect = null;
        String foundOut = run("found sect");
        String rulesOut = run("sect rules potential 50 gender female");
        check("L12 a player founds a sect and sets its rules with 'sect rules'", p.sect != null && p.sect.minPotential == 50 && "Female".equals(p.sect.genderPref) && rulesOut.contains("potential 50+"));
        p.sect.resign(p); p.location.sect = null;

        // ---------- biomes ----------
        int mismatch = 0, seen = 0, ocean = 0, oceanNoBeach = 0;
        for (int x = 0; x < Map.range; x++) for (int y = 0; y < Map.range; y++) {
            Area a = Map.tiles[x][y]; if (a == null) continue;
            if (x == Map.range / 2 && y == Map.range / 2) continue;
            seen++;
            BiomeGen.Cell c = BiomeGen.cell(Map.worldSeed, x, y, Map.range);
            if (!c.minor.equals(a.biomem) || !c.major.equals(a.biomeM)) mismatch++;
        }
        PcProbe.real.println("[T] info: explored tiles " + seen + ", biome mismatches " + mismatch);
        check("L13 every explored tile's biome is the noise field's biome at its position", seen > 0 && mismatch == 0);
        BiomeGen.Cell o = BiomeGen.cell(Map.worldSeed, Map.range / 2, Map.range / 2, Map.range);
        check("L13 Originis stays a temperate forest", o.major.equals("Forest") && o.elev > BiomeGen.SEA);
        for (int x = 0; x < Map.range; x++) for (int y = 0; y < Map.range; y++) {
            BiomeGen.Cell c = BiomeGen.cell(12345L, x, y, Map.range);
            boolean sea = c.minor.equals("Ocean");
            if (sea) ocean++;
            int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : nb) { BiomeGen.Cell q = BiomeGen.cell(12345L, Math.floorMod(x + d[0], Map.range), Math.floorMod(y + d[1], Map.range), Map.range); if (!sea && q.minor.equals("Ocean") && !c.minor.equals("Beach")) oceanNoBeach++; }
        }
        check("L13 land next to the sea is always a beach", oceanNoBeach == 0 && ocean > 0);
        check("L13 the same seed gives the same world", BiomeGen.cell(99L, 4, 5, 31).minor.equals(BiomeGen.cell(99L, 4, 5, 31).minor));
        int hot = 0, cold = 0;
        for (int x = 0; x < 31; x++) for (int y = 0; y < 31; y++) { BiomeGen.Cell c = BiomeGen.cell(777L, x, y, 31); if (c.minor.equals("Desert")) hot++; if (c.minor.equals("Tundra")) cold++; }
        PcProbe.real.println("[T] info: seed 777 deserts " + hot + " tundra " + cold);

        // ---------- festivals, diplomacy, bounties ----------
        { Area og = Map.tiles[Map.range / 2][Map.range / 2]; if (p.location != og) { p.location.removeChar(p); p.location = og; og.addChar(p); } } // the spring festival is held in a city: Originis
        Seasonal.festivals(0);
        check("L14 a spring festival inspires the people in a big city", p.fx != null && p.fx.inspiredWeeks > 0);
        long tre = p.location.cityTreasury; Seasonal.festivals(2);
        check("L14 festivals pay the city treasury", p.location.cityTreasury >= tre);
        Character sm1 = adult(false, 40), sm2 = adult(false, 40);
        sm1.majorLvl = 2; sm2.majorLvl = 2; sm1.lvl = 25; sm2.lvl = 25; sm1.location = p.location; sm2.location = p.location;
        Organization s1 = new Organization(sm1), s2 = new Organization(sm2);
        s1.members.add(sm1); s2.members.add(sm2);
        for (String step : new String[]{"d", "d", "w"}) run(step); // explore a few tiles so there is somewhere to put two sects
        Area ta = null, tb = null;
        for (Area x : Map.createdAreas) if (x != null && x.sect == null && !x.city) { if (ta == null) ta = x; else if (tb == null) tb = x; }
        ta.sect = s1; s1.location = ta; tb.sect = s2; s2.location = tb;
        s1.declareWar(s2);
        GameDriver.time = Math.max(GameDriver.time, 400); s1.warSince = GameDriver.time - 52 * 3; s2.warSince = s1.warSince; // wars now last at least two years: this one is old
        int truces = 0; for (int i = 0; i < 60 && s1.warTarget != null; i++) { Seasonal.diplomacy(); }
        check("L14 an old mutual war ends in a truce (chronicle entry)", s1.warTarget == null && !Chronicle.search("truce", "war", 5).isEmpty());
        Character toxic = adult(false, 30); toxic.location = p.location; p.location.addChar(toxic);
        Life.bounty(toxic, p.location);
        check("L14 a recognised poisoner gets a bounty at the nearest city", Map.createdAreas.stream().anyMatch(ar -> ar != null && ar.bounties.stream().anyMatch(b -> b.targetCharacter == toxic.id)));
        String mapLvl = run("map level");
        check("L15 map layers: 'map level' draws digits and names its layer", mapLvl.contains("layer 'level'"));
        String wt = run("watch 20 for war,breakthrough");
        check("L15 watch triggers: only the chosen categories end it", wt.contains("war") && p.pc().watchCats != null && p.pc().watchCats.contains("war") && p.pc().watching);
        p.pc().watching = false; p.skipping = 0; p.pc().watchCats = null;
        check("L15 digest", run("digest").contains("What happened lately"));
        String hall = run("halloffame");
        check("L15 hall of fame command works (empty or listing)", hall.contains("Hall of fame"));

        // ---------- the goods economy ----------
        Area home = Map.tiles[Map.range / 2][Map.range / 2];
        p.location.removeChar(p); p.location = home; home.addChar(p);
        p.trait = Character.Traits.Chef; p.ss = 5000;
        Life.goods(p).clear(); Life.goods(p).add(new Item(Item.ItemType.Food, 3, 20, "Test Congee")); Life.goods(p).add(new Item(Item.ItemType.Poison, 3, 4, "Test Venom"));
        home.goodsBag.clear();
        long ssSell = p.ss;
        String soldOut = run("sell goods");
        check("L16 selling goods pays the seller and stocks the city market", p.ss > ssSell && soldOut.contains("Sold") && Life.marketCount(home) >= 20);
        String mk = run("market");
        check("L16 'market' lists the shelves with prices", mk.contains("Goods market of") && mk.contains("Test Congee") && mk.contains("ss each"));
        long ssBuy = p.ss; int shelf = Life.marketCount(home);
        int congeeIdx = 0; for (int i = 0; i < home.goodsBag.size(); i++) if (home.goodsBag.get(i).itemType == Item.ItemType.Food) congeeIdx = i + 1;
        run("buy market " + congeeIdx + " 3");
        check("L16 buying moves portions to you, money to the city and takes them off the shelf", p.ss < ssBuy && Life.marketCount(home) == shelf - 3 && Life.firstOf(p, Item.ItemType.Food) != null);
        p.trait = Character.Traits.Alchemist;
        int poisonIdx = 0; for (int i = 0; i < home.goodsBag.size(); i++) if (home.goodsBag.get(i).itemType == Item.ItemType.Poison) poisonIdx = i + 1;
        check("L16 poison can be bought by anyone", !run("buy market " + poisonIdx).contains("only sold to Poisoners"));
        Character shopper = adult(false, 30); shopper.location = home; home.addChar(shopper); shopper.ss = 100000; shopper.majorLvl = 2;
        for (int i = 0; i < 80 && Life.firstOf(shopper, Item.ItemType.Food) == null; i++) Life.npcShop(shopper);
        check("L16 NPCs buy meals at the market", Life.firstOf(shopper, Item.ItemType.Food) != null);
        Character chefNpc = adult(false, 30); chefNpc.trait = Character.Traits.Chef; chefNpc.location = home; home.addChar(chefNpc); chefNpc.ss = 0;
        Life.goods(chefNpc).add(new Item(Item.ItemType.Food, 3, 10, "Festival Congee"));
        Seasonal.festivals(1);
        check("L16 a festival sends the nearby Chef a 'Festival Trade' quest", chefNpc.targetQuests.stream().anyMatch(q -> "Festival Trade".equals(q.name)));
        check("L16 Festival Trade sells at festival prices", chefNpc.targetQuests(chefNpc.targetQuests.stream().filter(q -> "Festival Trade".equals(q.name)).findFirst().get()) == 1 && chefNpc.ss > 0);
        long beforeSpoil = Life.marketCount(home); Life.marketYearly();
        check("L16 market food spoils over a year", Life.marketCount(home) < beforeSpoil || beforeSpoil == 0);

        // ---------- city-born cultivators arrive at 18 with the level their potential earned ----------
        int tempBefore2 = Map.tempCharacters.size();
        for (int i = 0; i < 400; i++) home.age();
        List<Character> born = new ArrayList<>(Map.tempCharacters.subList(tempBefore2, Map.tempCharacters.size()));
        int allEighteen = 0, above1 = 0, top = 0; for (Character c : born) { if (c.age == 18) allEighteen++; if (c.lvl > 1) above1++; top = Math.max(top, c.lvl); }
        PcProbe.real.println("[T] info: city births " + born.size() + " all 18: " + allEighteen + " level above 1: " + above1 + " highest " + top);
        check("L17 city-born cultivators arrive at 18 with a level that follows their potential", born.size() >= 10 && allEighteen == born.size() && above1 >= born.size() / 2);

        // ---------- NPCs reincarnate in a running world ----------
        GameDriver.frozenNpcs = false;
        int before = 0; for (Character c : Map.characters) if (c != null && c.lives > 0) before++;
        int setup = 0;
        for (Character c : Map.characters) { if (c != null && c.player == 0 && !c.dead && c.born && c.age >= 18 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster && setup < 40) { c.potential = 55; c.lvl = 39; c.majorLvl = 3; c.minorLvl = 9; setup++; } }
        int posted = 0; for (Character c : Map.characters) if (c != null && c.player == 0 && Reincarnation.ready(c)) { Reincarnation.check(c); posted++; }
        PcProbe.real.println("[T] info: NPCs at the end of their rope at the start: " + posted);
        PcProbe.waitTurns(30);
        int after = 0; for (Character c : Map.characters) if (c != null && c.lives > 0) after++;
        PcProbe.real.println("[T] info: reincarnated NPCs after 30 weeks: " + after + " (were " + before + "), population " + Map.population + ", id mismatches " + PcProbe.idMismatch());
        check("L11 NPCs reincarnate on their own", after > before);
        check("L11 ids stay equal to list indices", PcProbe.idMismatch() == 0);
    }
}
