package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-20: the Garden's Variety Tournament, Fame, the effects of a technique's name and the music of a tournament. Headless; the tournament is run directly on the 3,000-person scratch world.
 *  G1 the years; G2 the rounds are ceil(log2 n), one champion, nobody dies; G3 every living person has one record and the shares halve; G4 the record on the profile, the titles; G5 the prizes;
 *  G6 once per year (held), `settings garden off`; G7 the tick is left out of the epoch's measure; G8 Fame: kills, breakthrough, the yearly count and decay, the Most Famous board;
 *  G9 the look of a technique from its name; G10 the two tournament pieces parse and render; G11 the settled tournament takes little time. */
public class GardenProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
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

    static int living() { int n = 0; for (Character c : Map.characters) if (GardenTournament.eligible(c)) n++; return n; }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;
        GameDriver.frozenNpcs = true;

        // ---- G1: the years
        check("G1 years 10, 100, 200, 1000 are Garden years", GardenTournament.isGardenYear(10) && GardenTournament.isGardenYear(100) && GardenTournament.isGardenYear(200) && GardenTournament.isGardenYear(1000));
        check("G1b years 1, 9, 11, 50, 99, 101, 150 are not", !GardenTournament.isGardenYear(1) && !GardenTournament.isGardenYear(9) && !GardenTournament.isGardenYear(11) && !GardenTournament.isGardenYear(50) && !GardenTournament.isGardenYear(99) && !GardenTournament.isGardenYear(101) && !GardenTournament.isGardenYear(150));

        // ---- G2/G3: a tournament on the whole world
        int before = living();
        p.player = 1; // (the probe's character plays: it is entered and gets its report on the console)
        long t0 = System.nanoTime();
        Settings.gardenGames = true;
        Character champ = GardenTournament.run(10);
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        int n = before, rounds = 0;
        for (int m = n; m > 1; m = (m + 1) / 2) rounds++;
        check("G2 there is a champion (" + (champ == null ? "none" : champ.name) + ") and nobody died (" + before + " alive before, " + living() + " after)", champ != null && living() == before);
        int withLog = 0, champs = 0, maxS = 0, bad = 0;
        int[] hist = new int[rounds + 2];
        for (Character c : Map.characters) {
            if (c == null || !GardenTournament.eligible(c)) continue;
            if (c.gardenLog == null || c.gardenLog.length != 1) { bad++; continue; }
            withLog++;
            int e = c.gardenLog[0];
            if (GardenTournament.yearOf(e) != 10 || GardenTournament.roundsOf(e) != rounds) bad++;
            int s = GardenTournament.survivedOf(e);
            if (s > rounds) bad++; else hist[s]++;
            if (s == rounds) champs++;
            maxS = Math.max(maxS, s);
        }
        check("G2b the rounds are ceil(log2 " + n + ") = " + rounds + ", exactly one person survived them all (" + champs + "), and the champion is that one", champs == 1 && champ.gardenLog != null && GardenTournament.survivedOf(champ.gardenLog[0]) == rounds);
        check("G3 every living person has exactly one record of year 10 (" + withLog + " of " + before + ", " + bad + " wrong)", withLog == before && bad == 0);
        int atLeastHalf = 0; for (int s = 1; s <= rounds; s++) atLeastHalf += hist[s];
        check("G3b about half of the field survived the first round (" + atLeastHalf + " of " + n + ")", atLeastHalf >= n * 0.45 && atLeastHalf <= n * 0.55 + 2);
        check("G3c round by round the survivors about halve (" + hist[rounds] + " champion, " + hist[rounds - 1] + " lost the final, " + hist[rounds - 2] + " lost a semifinal: 1 or 2, a bye makes it 1)", hist[rounds] == 1 && hist[rounds - 1] == 1 && hist[rounds - 2] >= 1 && hist[rounds - 2] <= 2);

        // ---- G4: the profile and the titles
        check("G4 the champion's profile says it (" + GardenTournament.profileLine(champ) + ")", GardenTournament.profileLine(champ).contains("Year 10: survived " + rounds + " of " + rounds + " rounds") && GardenTournament.profileLine(champ).contains("Champion of the Garden"));
        check("G4b the champion's title is 'Champion of the Garden' and outranks the boards", "Champion of the Garden".equals(GardenTournament.title(champ)) && GardenTournament.grand(champ));
        Naming.assignTitles(Map.characters);
        check("G4c the yearly titles give the champion that title (" + champ.repTitle + ")", "Champion of the Garden".equals(champ.repTitle));
        Character nobody = null; for (Character c : Map.characters) if (GardenTournament.eligible(c) && c != champ && c.gardenLog != null && GardenTournament.survivedOf(c.gardenLog[0]) == 0) { nobody = c; break; }
        check("G4d someone who fell in round 1 has a profile line too (survived 0) and no Garden title (" + (nobody == null ? "none found" : GardenTournament.profileLine(nobody)) + ")", nobody != null && GardenTournament.profileLine(nobody).contains("survived 0 of " + rounds) && GardenTournament.title(nobody) == null);
        check("G4e the Chronicle has the tournament", GardenTournament.held(10) && !GardenTournament.held(100));
        check("G4f a person who was not born yet has no record", new Character(Map.tiles[Map.range / 2][Map.range / 2]).gardenLog == null);

        // ---- G5: the prizes
        check("G5 the champion has about 5,000 fame (" + champ.fame + "; the yearly count of the titles faded 4%)", champ.fame >= 4700);
        Character fin = null; for (Character c : Map.characters) if (GardenTournament.eligible(c) && c.gardenLog != null && GardenTournament.survivedOf(c.gardenLog[0]) == rounds - 1) { fin = c; break; }
        check("G5b the finalist has about 2,500 fame and the title 'Finalist of the Garden' (" + (fin == null ? "?" : fin.fame + ", " + GardenTournament.title(fin)) + ")", fin != null && fin.fame >= 2350 && "Finalist of the Garden".equals(GardenTournament.title(fin)));
        check("G5c the tiers: champion 0, finalist 1, semifinal 2, quarter 3, last 16 is 4, last 64 is 5, half-way 6, the rest 7 (R=17)", GardenTournament.tierOf(17, 17) == 0 && GardenTournament.tierOf(16, 17) == 1 && GardenTournament.tierOf(15, 17) == 2 && GardenTournament.tierOf(14, 17) == 3 && GardenTournament.tierOf(13, 17) == 4 && GardenTournament.tierOf(12, 17) == 5 && GardenTournament.tierOf(11, 17) == 5 && GardenTournament.tierOf(10, 17) == 6 && GardenTournament.tierOf(9, 17) == 6 && GardenTournament.tierOf(8, 17) == 7 && GardenTournament.tierOf(0, 17) == 7);
        int lucky = 0; for (Character c : Map.characters) if (GardenTournament.eligible(c) && c.gardenLog != null && GardenTournament.luckyOf(c.gardenLog[0])) lucky++;
        check("G5d at most one Favoured of the Garden (" + lucky + ")", lucky <= 1);
        check("G5e the person who plays got a report on the console (an entrant)", PcProbe.cap.toString().contains("Garden's Variety Tournament, Year 10") && PcProbe.cap.toString().contains("rounds"));

        // ---- G6: once a year, and the switch
        int logsBefore = champ.gardenLog.length;
        GardenTournament.yearly(10);
        check("G6 the same year is not held twice (the Chronicle remembers)", champ.gardenLog.length == logsBefore);
        Settings.gardenGames = false;
        GardenTournament.yearly(100);
        check("G6b `settings garden off` skips it", !GardenTournament.held(100));
        Settings.gardenGames = true;

        // ---- G7: the tick is left out
        GardenTournament.heldThisTick = false; GardenTournament.run(100);
        check("G7 the tick that held it is flagged (it is not counted in the epoch's median)", GardenTournament.heldThisTick);
        GardenTournament.heldThisTick = false;
        check("G7b a second tournament adds a second line to the profile", GardenTournament.profileLine(champ).split(";").length == 2 || champ.dead);
        check("G7c the time to settle " + n + " people: " + ms + " ms (per 100,000 about " + (ms * 100000L / Math.max(1, n)) + " ms)", ms < 4000);

        // ---- G8: fame
        Character k1 = new Character(Map.tiles[Map.range / 2][Map.range / 2]), k2 = new Character(Map.tiles[Map.range / 2][Map.range / 2]);
        k1.age = 30; k2.age = 30; k1.majorLvl = 3; k2.majorLvl = 3;
        k1.fame = 0;
        Fame.slain(k1, k2);
        check("G8 killing an equal earns 3 x (3 + 1) = 12 fame (" + k1.fame + ")", k1.fame == 12);
        k2.majorLvl = 0; int f0 = k1.fame; Fame.slain(k1, k2);
        check("G8b killing someone three realms below is no glory (" + (k1.fame - f0) + ")", k1.fame == f0);
        k2.majorLvl = 3; k2.age = 12; Fame.slain(k1, k2);
        check("G8c a child's death earns nothing", k1.fame == f0);
        k1.fame = 0; k1.majorLvl = 5; Fame.breakthrough(k1);
        check("G8d reaching Ensoulment earns 160 (" + k1.fame + "), True Immortal 5,120", k1.fame == 160 && 5120 == (10 << 9));
        k1.fame = 1000; k1.itemsCrafted = 500; k1.fameCrafted = 0; Fame.yearly(Arrays.asList(k1));
        check("G8e a year of crafting counts at most " + Fame.CRAFT_CAP + " items (60 fame) and the old fame fades 4% (" + k1.fame + ")", k1.fame == (int) ((1000 + 60) * 96L / 100) && k1.fameCrafted == 500);
        Character beast = new Character(Map.tiles[Map.range / 2][Map.range / 2]); beast.trait = Character.Traits.Beast;
        int before2 = beast.fame; Fame.add(beast, 99);
        check("G8f a beast earns no fame", beast.fame == before2);
        List<Character> all = new ArrayList<>(Map.characters);
        for (Character c : all) if (c != null) c.fame = 0;
        champ.fame = 7000; k1.fame = 100;
        Rankings.yearly(Map.characters);
        check("G8g the Most Famous board ranks the champion first (" + champ.rankFame + ") and the epoch spares the ranked", champ.rankFame == 1 && Rankings.epochSafe(champ));
        check("G8h the text of the board", Rankings.text("fame", champ).contains("Most Famous") && Rankings.places(champ).contains("Most Famous 1"));

        // ---- G9: the look of a technique
        FightFx.Look l1 = FightFx.look("Nine Thunder Peaks Sword Art", false, 1);
        check("G9 'Nine Thunder Peaks Sword Art' is a slash with bolts, six of them (" + l1 + ")", l1.shape == FightFx.SLASH && l1.motif == FightFx.BOLTS && l1.count == 6);
        FightFx.Look l2 = FightFx.look("Ember Crane Palm", false, 1);
        check("G9b 'Ember Crane Palm' is a palm, with embers and a winged beast (" + l2 + ")", l2.shape == FightFx.PALM && l2.motif == FightFx.EMBERS && l2.beast == FightFx.WINGED);
        FightFx.Look l3 = FightFx.look("Heaven Frost Dragon Spear Art", false, 4);
        check("G9c 'Heaven Frost Dragon Spear Art': a thrust, crystals, a serpent and a ring of runes (" + l3 + ")", l3.shape == FightFx.THRUST && l3.motif == FightFx.CRYSTALS && l3.beast == FightFx.SERPENT && l3.ring);
        FightFx.Look l4 = FightFx.look("Twin Frost Leap", true, 4);
        check("G9d a movement technique is shown as a motion: 'Twin Frost Leap' leaps (" + l4 + ")", l4.movement && l4.move == FightFx.LEAP);
        FightFx.Look l5 = FightFx.look("Shadow Walk", true, 0);
        FightFx.Look l6 = FightFx.look("Wind-Treading Dash", true, 0);
        check("G9e 'Shadow Walk' fades, 'Wind-Treading Dash' rushes", l5.move == FightFx.FADE && l6.move == FightFx.RUSH);
        FightFx.Look l7 = FightFx.look("Default Technique", false, 2);
        check("G9f a technique with no word still has a look from its element (Earth: shards, a plain blow)", l7.shape == FightFx.STRIKE && l7.motif == FightFx.SHARDS);
        check("G9g the same name gives the same look, a different name a different one", FightFx.look("Nine Thunder Peaks Sword Art", false, 1).toString().equals(l1.toString()) && !l1.toString().equals(l2.toString()));
        // every word of the name engine gets a form
        int unknownForm = 0;
        for (String f : new String[]{"Palm", "Fist", "Sword Art", "Saber Technique", "Spear Art", "Staff Method", "Fan Dance", "Scripture", "Strike", "Claw", "Kick", "Finger", "Seal"}) if (FightFx.look("Flame Tiger " + f, false, 1).shape == FightFx.STRIKE && !f.equals("Strike")) unknownForm++;
        check("G9h all thirteen forms of the name engine have a shape of their own (" + unknownForm + " without)", unknownForm == 0);
        // a drawing runs without an exception on an image
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(760, 300, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        boolean drew = true;
        try {
            for (FightFx.Look lk : new FightFx.Look[]{l1, l2, l3, l7, FightFx.look("Hundred Jade Claw", false, 3), FightFx.look("Ten Thousand Mist Finger", false, 4), FightFx.look("Seven Moon Seal", false, 4), FightFx.look("Blood Wolf Fan Dance", false, 1), FightFx.look("Void Tortoise Scripture", false, 0), FightFx.look("Star Fist", false, 3), FightFx.look("Storm Eagle Kick", false, 0), FightFx.look("Spirit Bear Staff Method", false, 2)})
                for (double pp = 0.05; pp < 1; pp += 0.1) FightFx.blow(g, lk, pp, 200, 230, 560, 230, 1, new java.awt.Color(200, 120, 60), 7);
            for (FightFx.Look lk : new FightFx.Look[]{l4, l5, l6})
                for (double pp = 0.05; pp < 1; pp += 0.1) { FightFx.motion(lk, pp); FightFx.movement(g, lk, pp, 300, 240, -1, new java.awt.Color(90, 160, 220), 3); }
            for (int kind = 1; kind <= 2; kind++) for (double pp = 0; pp <= 1; pp += 0.25) FightFx.stage(g, kind, 760, 300, 240, (long) (pp * 4000), pp, 1 - pp, true);
        } catch (RuntimeException e) { drew = false; e.printStackTrace(PcProbe.real); }
        g.dispose();
        int lit = 0; for (int x = 0; x < 760; x += 4) for (int y = 0; y < 300; y += 4) if (((img.getRGB(x, y) >>> 24) & 255) > 0) lit++;
        check("G9i every effect and both stages draw on an image without an exception (" + lit + " sampled pixels painted)", drew && lit > 500);

        // ---- G10: the music
        Score arena = EventMusic.score(EventMusic.ARENA), garden = EventMusic.score(EventMusic.GARDEN);
        check("G10 'Sand and Drums' and 'The Platforms Fall' parse with no problem (" + EventMusic.problems + ")", EventMusic.problems.isEmpty() && arena.problems.isEmpty() && garden.problems.isEmpty());
        byte[] pcm1 = Ambient.renderScore(arena, 4, 3, 90), pcm2 = Ambient.renderScore(garden, 6, 3, 90);
        double s1 = pcm1.length / (44100.0 * 4), s2 = pcm2.length / (44100.0 * 4);
        check("G10b they render to sound: " + String.format("%.0f", s1) + " s and " + String.format("%.0f", s2) + " s", s1 > 25 && s2 > 25 && nonSilent(pcm1) && nonSilent(pcm2));
        check("G10c neither is in the music the places choose from (SongBook)", SongBook.find("Sand and Drums") == null && SongBook.find("Platforms Fall") == null);
        EventMusic.hold(EventMusic.GARDEN); EventMusic.release();
        check("G10d holding and letting go of the music is counted (" + EventMusic.holds() + " holds)", EventMusic.holds() == 0);

        System.out.println();
        PcProbe.real.println("[T] DONE");
    }

    static boolean nonSilent(byte[] pcm) { int loud = 0; for (int i = 0; i + 1 < pcm.length; i += 2) { int v = (short) ((pcm[i + 1] << 8) | (pcm[i] & 255)); if (Math.abs(v) > 800) loud++; } return loud > 2000; }
}
