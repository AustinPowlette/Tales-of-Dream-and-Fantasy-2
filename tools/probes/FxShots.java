package Game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

/** 2026-10-20: renders frames of the fight window (`GuiFightView.Stage`) to PNG, off screen: a technique's blow from its name, a movement technique, an ordinary tournament, the Garden's Variety Tournament.
 *  `-Dshots=<dir>` (default `fxshots`). Needs the 3,000-person scratch world like the other probes (the fighters are real characters); no display is needed. */
public class FxShots {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); }
        System.exit(0);
    }

    static Character fighter(String name, int lvl, int major, String martial, String move, int elem) {
        Character c = new Character(Map.tiles[Map.range / 2][Map.range / 2]);
        c.name = name; c.lastName = "Test"; c.age = 30; c.lvl = lvl; c.majorLvl = major; c.maxAge = 100000;
        Technique m = new Technique(Technique.techniqueType.Martial, c, 20), v = new Technique(Technique.techniqueType.Movement, c, 20);
        m.name = martial; m.elemPlus1 = (byte) (elem + 1); v.name = move; v.elemPlus1 = (byte) (elem + 1);
        c.martialTechnique = m; c.movementTechnique = v;
        return c;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        File dir = new File(System.getProperty("shots", "fxshots"));
        dir.mkdirs();
        Object[][] set = {
            {"slash", "Nine Thunder Peaks Sword Art", "Wind-Treading Dash", 1, 0},
            {"palm", "Ember Crane Palm", "Twin Frost Leap", 1, 4},
            {"spear", "Heaven Frost Dragon Spear Art", "Shadow Walk", 4, 0},
            {"claw", "Blood Tiger Claw", "Verdant Wolf Glide", 1, 0},
            {"seal", "Seven Moon Seal", "Iron Footwork", 4, 3},
            {"scripture", "Void Tortoise Scripture", "Mist Vanishing", 0, 4},
            {"fan", "Bamboo Willow Fan Dance", "Wind Stride", 0, 2},
        };
        for (Object[] s : set) {
            Character x = fighter("Lian", 60, 5, (String) s[1], (String) s[2], (Integer) s[3]), y = fighter("Borin", 55, 4, "Granite Bear Fist", "Stone Leap", (Integer) s[4]);
            for (int mode = 0; mode < 4; mode++) {
                FightReplay f = FightReplay.bout(x, y, 60, x, "Test", "Test place");
                f.tourney = mode == 0 ? 0 : mode;           // 0 ordinary fight, 1 tournament, 2 Garden
                if (mode == 3) continue;
                shot(f, new File(dir, s[0] + "_t" + f.tourney + ".png"), false);
                if (mode == 0) shot(f, new File(dir, s[0] + "_move.png"), true);
            }
        }
        PcProbe.real.println("[T] wrote shots to " + dir.getAbsolutePath());
    }

    static void shot(FightReplay f, File out, boolean movement) throws Exception {
        GuiFightView.Stage st = new GuiFightView.Stage(f);
        st.setSize(760, 300);
        int idx = -1;
        for (int i = 0; i < f.beats.size(); i++) { FightReplay.Beat b = f.beats.get(i); if (b.tech != null && b.movement == movement) { idx = i; break; } }
        if (idx < 0) for (int i = 0; i < f.beats.size(); i++) if (f.beats.get(i).tech != null) { idx = i; break; }
        if (idx < 0) idx = f.beats.size() - 1;
        st.beat = idx;
        FightReplay.Beat b = f.beats.get(idx);
        st.beatStart = System.currentTimeMillis() - (long) (0.55 * st.beatMs(b));
        st.dispA = b.hpA; st.dispB = b.hpB;
        st.hitAt = System.currentTimeMillis() - 100;
        BufferedImage img = new BufferedImage(760, 300, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        st.paint(g);
        g.dispose();
        javax.imageio.ImageIO.write(img, "png", out);
    }
}
