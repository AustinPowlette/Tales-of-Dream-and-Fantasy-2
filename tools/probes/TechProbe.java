package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-18 (headless): the technique slots (a tie goes to the higher potential; in Manual mode the game leaves the slots alone and swaptech / usetech / forget work; the first movement technique levels in practice),
 *  the Craft chore of a person who plays (it follows the spot action) and the little stories (Manual only). */
public class TechProbe {
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }

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

    static Technique tech(Technique.techniqueType k, String name, int lvl, int pot) {
        Technique q = new Technique(k); q.name = name; q.lvl = lvl; q.potential = pot; return q;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        PlayerControl pc = p.pc();
        Technique.techniqueType M = Technique.techniqueType.Martial, V = Technique.techniqueType.Movement;

        // ---- A: a tie in level goes to the higher potential (not in Manual: there the slots are left alone)
        pc.mode = PlayerControl.Mode.AUTO;
        Technique m1 = tech(M, "Old Fist", 10, 30), m2 = tech(M, "New Fist", 10, 40), v1 = tech(V, "Old Step", 12, 30), v2 = tech(V, "New Step", 12, 45);
        p.martialTechnique = m1; p.martialTechnique2 = m2; p.movementTechnique = v1; p.movementTechnique2 = v2; p.techniqueList.clear();
        p.equip();
        check("A1 Auto: martial 2 with the same level and more potential takes the first place (" + p.martialTechnique.name + ")", p.martialTechnique == m2);
        check("A2 ...and movement 2 too (" + p.movementTechnique.name + ")", p.movementTechnique == v2);
        Technique k1 = tech(M, "Steady Fist", 11, 30), k2 = tech(M, "Rising Fist", 10, 40);
        p.martialTechnique = k1; p.martialTechnique2 = k2; p.techniqueList.clear(); p.equip();
        check("A3 a higher level in the first slot stays (" + p.martialTechnique.name + ") and the second keeps its place (" + (p.martialTechnique2 == null ? "none" : p.martialTechnique2.name) + ")", p.martialTechnique == k1 && p.martialTechnique2 == k2);
        p.equip(); p.equip();
        check("A4 sorting again changes nothing (no churn)", p.martialTechnique == k1 && p.martialTechnique2 == k2);

        // ---- A5: the known list never holds a worn technique, nor one twice (an Innovative keeps its known techniques; found by the 2026-10-18 soak: two techniques competing for the second slot left the loser in the list twice)
        p.trait = Character.Traits.Innovative;
        Technique s1 = tech(M, "Base Fist", 10, 30), c1 = tech(M, "Cand One", 5, 40), c2 = tech(M, "Cand Two", 5, 50);
        p.martialTechnique = s1; p.martialTechnique2 = null; p.techniqueList.clear(); p.techniqueList.add(c1); p.techniqueList.add(c2);
        p.equip();
        int dups = 0; for (int i = 0; i < p.techniqueList.size(); i++) for (int j = i + 1; j < p.techniqueList.size(); j++) if (p.techniqueList.get(i) == p.techniqueList.get(j)) dups++;
        boolean wornInList = p.techniqueList.contains(p.martialTechnique) || (p.martialTechnique2 != null && p.techniqueList.contains(p.martialTechnique2));
        check("A5 two techniques compete for the second slot: the best is worn (" + (p.martialTechnique2 == null ? "none" : p.martialTechnique2.name) + "), the other is in the list once (" + p.techniqueList.size() + " entries, " + dups + " duplicates)", p.martialTechnique2 == c2 && dups == 0 && !wornInList && p.techniqueList.size() == 1);
        for (int k = 0; k < 5; k++) p.equip();
        check("A6 sorting again and again does not grow the list (" + p.techniqueList.size() + ")", p.techniqueList.size() == 1);
        p.trait = Character.Traits.Alchemist; p.techniqueList.clear();
        // ---- A7: an NPC's known list is kept to the best 30; a person who plays keeps every technique
        p.trait = Character.Traits.Innovative; p.martialTechnique = s1; p.martialTechnique2 = null; p.techniqueList.clear();
        for (int i = 1; i <= 45; i++) p.techniqueList.add(tech(V, "Idea " + i, 1, i + 3));
        pc.mode = PlayerControl.Mode.AUTO; int pl = p.player; p.player = 0; p.equipNow = true; p.equip(); p.equipNow = false; p.player = pl;
        boolean bestKept = p.movementTechnique2 != null && p.movementTechnique2.potential == 48; for (Technique q : p.techniqueList) if (q.potential == 48) bestKept = true; // (the best one may be worn as the second movement technique)
        boolean worstGone = true; for (Technique q : p.techniqueList) if (q.potential < 19) worstGone = false;
        check("A7 an NPC keeps its best " + Character.TECH_CAP + " known techniques (" + p.techniqueList.size() + ", best kept " + bestKept + ")", p.techniqueList.size() <= Character.TECH_CAP + 1 && bestKept && worstGone);
        pc.mode = PlayerControl.Mode.MANUAL; p.techniqueList.clear();
        for (int i = 1; i <= 45; i++) p.techniqueList.add(tech(V, "Idea " + i, 1, i + 3));
        p.equipNow = true; p.equip(); p.equipNow = false;
        check("A8 a person who plays keeps all of them (" + p.techniqueList.size() + ")", p.techniqueList.size() >= 44);
        p.trait = Character.Traits.Alchemist; p.techniqueList.clear(); pc.mode = PlayerControl.Mode.MANUAL;

        // ---- B: practice levels the first movement technique
        pc.mode = PlayerControl.Mode.MANUAL;
        Technique mv = tech(V, "Sprint", 1, 90); p.movementTechnique = mv; p.movementTechnique2 = null; p.lvl = Math.max(p.lvl, 50); p.potential = 100;
        int before = mv.lvl;
        for (int i = 0; i < 4000 && mv.lvl == before; i++) p.practice();
        check("B1 practice() raises the level of the first movement technique (" + before + " -> " + mv.lvl + ")", mv.lvl > before);

        // ---- C: Manual mode leaves the slots alone
        Technique a1 = tech(M, "Slow Palm", 5, 30), a2 = tech(M, "Fast Palm", 20, 40), kn = tech(M, "Spare Palm", 30, 50);
        p.martialTechnique = a1; p.martialTechnique2 = a2; p.techniqueList.clear(); p.techniqueList.add(kn);
        p.equip(); p.tempLvlCheck();
        check("C1 Manual: equip() and tempLvlCheck() leave the slots as put (" + p.martialTechnique.name + " / " + p.martialTechnique2.name + ")", p.martialTechnique == a1 && p.martialTechnique2 == a2);
        check("C2 ...and keep the known list, even for a trait that forgets it (" + p.techniqueList.size() + ", trait " + p.trait + ")", p.techniqueList.contains(kn));
        p.equipNow = true; try { p.equip(); } finally { p.equipNow = false; }
        check("C3 the equip command (equipNow) sorts them: the better one is first now (" + p.martialTechnique.name + ")", p.martialTechnique != a1);
        p.martialTechnique = a1; p.martialTechnique2 = a2; p.techniqueList.clear(); p.techniqueList.add(kn);
        Settings.autoEquipManual = true; p.equip(); Settings.autoEquipManual = false;
        check("C4 'settings autoequip on' brings the sorting back in Manual", p.martialTechnique != a1);

        // ---- D: swaptech
        p.martialTechnique = a1; p.martialTechnique2 = a2; p.movementTechnique2 = null; p.techniqueList.clear();
        String s = TechniqueSlots.swap(p, "martial");
        check("D1 swaptech martial: the two change places (" + s + ")", p.martialTechnique == a2 && p.martialTechnique2 == a1);
        p.tempLvlCheck();
        check("D2 the swap survives tempLvlCheck()", p.martialTechnique == a2 && p.martialTechnique2 == a1);
        s = TechniqueSlots.swap(p, "movement");
        check("D3 no second movement technique: a sentence, nothing changes (" + s + ")", p.movementTechnique2 == null && s.contains("no second"));
        check("D4 a bad kind gives the usage", TechniqueSlots.swap(p, "flying").startsWith("Usage"));

        // ---- E: usetech
        p.martialTechnique = a1; p.martialTechnique2 = a2; p.techniqueList.clear(); p.techniqueList.add(kn);
        s = TechniqueSlots.use(p, 1, 2);
        check("E1 usetech 1 wears the known one as the second and the old second goes back (" + s + ")", p.martialTechnique2 == kn && p.techniqueList.size() == 1 && p.techniqueList.get(0) == a2 && p.martialTechnique == a1);
        s = TechniqueSlots.use(p, 1, 1);
        check("E2 usetech 1 1 wears it as the first (" + p.martialTechnique.name + ")", p.martialTechnique == a2 && p.techniqueList.contains(a1));
        check("E3 a wrong number gives the usage", TechniqueSlots.use(p, 9, 2).startsWith("Usage"));

        // ---- F: forget
        p.martialTechnique = a1; p.martialTechnique2 = a2; p.techniqueList.clear(); p.techniqueList.add(kn);
        s = TechniqueSlots.forget(p, "martial2", -1);
        check("F1 forget martial2 empties the second slot (" + s + ")", p.martialTechnique2 == null && p.martialTechnique == a1);
        p.martialTechnique2 = a2;
        s = TechniqueSlots.forget(p, "martial", -1);
        check("F2 forget martial: the second takes the first place (" + p.martialTechnique.name + ")", p.martialTechnique == a2 && p.martialTechnique2 == null);
        s = TechniqueSlots.forget(p, "martial", -1);
        check("F3 forgetting again: the best known one steps in and leaves the list (" + p.martialTechnique.name + ")", p.martialTechnique == kn && p.techniqueList.isEmpty());
        s = TechniqueSlots.forget(p, "martial", -1);
        check("F4 with nothing else, the Default Technique (" + p.martialTechnique.name + ")", "Default Technique".equals(p.martialTechnique.name));
        s = TechniqueSlots.forget(p, "martial", -1);
        check("F5 the Default Technique alone cannot be forgotten (" + s + ")", s.contains("nothing to forget") && p.martialTechnique != null);
        p.techniqueList.add(a1); p.techniqueList.add(a2);
        s = TechniqueSlots.forget(p, "known", 1);
        check("F6 forget known 1 removes it (" + s + ")", p.techniqueList.size() == 1 && p.techniqueList.get(0) == a2);
        check("F7 a wrong number says so", TechniqueSlots.forget(p, "known", 5).startsWith("No such"));

        // ---- G: through the command line
        p.martialTechnique = a1; p.martialTechnique2 = a2; p.techniqueList.clear(); p.techniqueList.add(kn);
        int mk = PcProbe.mark();
        PcProbe.cmd("swaptech martial");
        check("G1 the command swaptech works and takes no turn (" + p.martialTechnique.name + ")", p.martialTechnique == a2 && PcProbe.since(mk).contains("now"));
        PcProbe.cmd("techniques");
        check("G2 'techniques' numbers the known list", PcProbe.since(mk).contains("1) Martial"));
        PcProbe.cmd("usetech 1");
        check("G3 the command usetech wears it", p.martialTechnique2 == kn);
        PcProbe.cmd("!forget martial2");
        check("G4 !forget martial2 skips the question", p.martialTechnique2 == null);
        check("G5 no exceptions so far", !PcProbe.out().contains("Exception"));

        // ---- H: the little stories are for Manual mode only
        Settings.lore = false; Settings.vignettes = true; Vignettes.testMode = true;
        DecisionBroker.scripted = new ArrayDeque<>(); DecisionBroker.scriptedNumbers = new ArrayDeque<>();
        for (int i = 0; i < 300; i++) DecisionBroker.scripted.add(0);
        int savedTime = GameDriver.time;
        GameDriver.time = 200;
        pc.vignettesSeen = new HashSet<>(); pc.nextVignetteWeek = 0;
        pc.mode = PlayerControl.Mode.HYBRID;
        for (int i = 0; i < 3000; i++) Vignettes.tick(p);
        check("H1 Hybrid: no little story in 3,000 turns", pc.vignettesSeen.isEmpty());
        pc.mode = PlayerControl.Mode.AUTO;
        for (int i = 0; i < 3000; i++) Vignettes.tick(p);
        check("H2 Auto: none either", pc.vignettesSeen.isEmpty());
        pc.mode = PlayerControl.Mode.MANUAL;
        int ticks = 0;
        while (pc.vignettesSeen.isEmpty() && ticks < 6000) { Vignettes.tick(p); ticks++; }
        check("H3 Manual: one does come (" + ticks + " turns)", !pc.vignettesSeen.isEmpty());
        DecisionBroker.scripted = null; DecisionBroker.scriptedNumbers = null; Vignettes.testMode = false; GameDriver.time = savedTime;

        // ---- I: the Craft chore follows the spot action
        pc.mode = PlayerControl.Mode.MANUAL;
        p.trait = Character.Traits.Blacksmith;
        pc.spotAction = 1; // Hunt
        check("I1 spot action Hunt: the craft chore does not run", !p.craftChoreTurn());
        pc.spotAction = 0;
        check("I2 Meditate: no", !p.craftChoreTurn());
        pc.spotAction = 2;
        check("I3 Profession: yes", p.craftChoreTurn());
        pc.spotAction = 3;
        check("I4 Surprise me: yes", p.craftChoreTurn());
        pc.mode = PlayerControl.Mode.AUTO; pc.spotAction = 1;
        check("I5 Auto works its trade whatever the spot action", p.craftChoreTurn());
        pc.mode = PlayerControl.Mode.HYBRID;
        check("I6 Hybrid follows the spot action like Manual", !p.craftChoreTurn());
        pc.mode = PlayerControl.Mode.MANUAL;
        PcProbe.real.println("[T] DONE");
    }
}
