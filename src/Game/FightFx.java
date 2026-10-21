package Game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** The effects of the fight window. Two kinds:
 *  1. What a TECHNIQUE looks like, read from its name and its type: the word of its form (Palm, Fist, Sword Art, Saber Technique, Spear Art, Staff Method, Fan Dance, Scripture, Strike, Claw, Kick, Finger, Seal) gives the
 *     shape of the blow, the word of its element (Thunder, Frost, Flame, Wind, Stone, River, Shadow, Moon, Sun, Star, Iron, Jade, Void, Storm, Mist, Lightning, Blood, Spirit, or the words of the five paths: Bamboo, Ember,
 *     Granite, Steel, Tide ...) the sparks around it, the creature (Dragon, Phoenix, Tiger, Crane, Serpent, Tortoise, Wolf, Lion, Eagle, Mantis, Bear, Monkey, Qilin, Heron) a beast that rides the blow, the number (Nine, Thousand,
 *     Twin ...) how many of them there are, and Heaven / Earth / Five Elements ... a ring of runes. A MOVEMENT technique (Step, Leap, Flight, Shadow Walk, Dash, Vanishing ...) is shown as the fighter's own motion: after-images,
 *     a jump, a fade. The colour is the element's. Everything is drawn from the name, so every technique looks the same each time it is used and a different name looks different.
 *  2. The STAGE of a tournament: a crowd, banners and lanterns for an ordinary tournament, and for the Garden's Variety Tournament a night sky with floating platforms, falling light and a carved stone platform that rings with every blow.
 *  Pure drawing: no game state is read or changed. */
final class FightFx {
    private FightFx() { }

    // shapes of a martial blow
    static final int STRIKE = 0, SLASH = 1, THRUST = 2, STAFF = 3, PALM = 4, FIST = 5, KICK = 6, CLAW = 7, FINGER = 8, FAN = 9, GLYPHS = 10, SEAL = 11;
    // how a movement technique moves
    static final int RUSH = 0, LEAP = 1, FADE = 2;
    // the sparks around a blow
    static final int NONE = 0, LEAVES = 1, EMBERS = 2, RAYS = 3, SHARDS = 4, GLINTS = 5, DROPS = 6, CRYSTALS = 7, MIST = 8, WAVES = 9, BOLTS = 10, SPIRAL = 11, DARK = 12, CRESCENT = 13, STARS = 14, BLOOD = 15, ORB = 16;
    // the beast that rides a blow
    static final int NO_BEAST = 0, SERPENT = 1, WINGED = 2, CLAWED = 3, SHELL = 4;

    static final String[] SHAPE_NAMES = {"strike", "slash", "thrust", "staff swing", "palm", "fist", "kick", "claws", "finger beam", "fan", "glyphs", "seal"};
    static final String[] MOTIF_NAMES = {"none", "leaves", "embers", "rays", "shards", "glints", "drops", "crystals", "mist", "waves", "bolts", "spiral", "darkness", "crescent", "stars", "blood", "orb"};
    static final String[] BEAST_NAMES = {"none", "serpent", "winged", "clawed", "shell"};
    static final String[] MOVE_NAMES = {"rush", "leap", "fade"};

    /** What a technique looks like. */
    static final class Look {
        int shape = STRIKE, motif = NONE, beast = NO_BEAST, count = 1, move = RUSH;
        boolean movement, ring;
        int element;
        String tech = "";
        public String toString() { return tech + " -> " + (movement ? "movement " + MOVE_NAMES[move] : SHAPE_NAMES[shape]) + ", " + MOTIF_NAMES[motif] + ", beast " + BEAST_NAMES[beast] + ", x" + count + (ring ? ", rune ring" : ""); }
    }

    private static final String[][] MOTIFS = {
        {"thunder", "" + BOLTS}, {"lightning", "" + BOLTS}, {"storm", "" + SPIRAL}, {"wind", "" + SPIRAL}, {"shadow", "" + DARK}, {"void", "" + DARK}, {"moon", "" + CRESCENT}, {"star", "" + STARS},
        {"blood", "" + BLOOD}, {"spirit", "" + ORB}, {"frost", "" + CRYSTALS}, {"mist", "" + MIST}, {"river", "" + WAVES}, {"tide", "" + WAVES}, {"ripple", "" + WAVES}, {"rain", "" + DROPS}, {"lake", "" + WAVES},
        {"sun", "" + RAYS}, {"jade", "" + GLINTS}, {"verdant", "" + LEAVES}, {"bamboo", "" + LEAVES}, {"willow", "" + LEAVES}, {"pine", "" + LEAVES}, {"thorn", "" + LEAVES}, {"vine", "" + LEAVES}, {"orchid", "" + LEAVES}, {"cedar", "" + LEAVES},
        {"flame", "" + EMBERS}, {"ember", "" + EMBERS}, {"crimson", "" + EMBERS}, {"blaze", "" + EMBERS}, {"cinder", "" + EMBERS}, {"scarlet", "" + EMBERS}, {"ash", "" + EMBERS},
        {"stone", "" + SHARDS}, {"mountain", "" + SHARDS}, {"clay", "" + SHARDS}, {"granite", "" + SHARDS}, {"dust", "" + SHARDS}, {"boulder", "" + SHARDS}, {"ochre", "" + SHARDS}, {"cavern", "" + SHARDS},
        {"iron", "" + GLINTS}, {"steel", "" + GLINTS}, {"silver", "" + GLINTS}, {"bronze", "" + GLINTS}, {"golden", "" + GLINTS}, {"razor", "" + GLINTS}, {"copper", "" + GLINTS}, {"bell", "" + GLINTS}};
    private static final int[] ELEMENT_MOTIF = {LEAVES, EMBERS, SHARDS, GLINTS, DROPS};      // Wood, Fire, Earth, Metal, Water
    private static final String[][] FORMS = {
        {"sword", "" + SLASH}, {"saber", "" + SLASH}, {"spear", "" + THRUST}, {"staff", "" + STAFF}, {"palm", "" + PALM}, {"fist", "" + FIST}, {"kick", "" + KICK}, {"claw", "" + CLAW}, {"finger", "" + FINGER},
        {"fan", "" + FAN}, {"scripture", "" + GLYPHS}, {"seal", "" + SEAL}, {"strike", "" + STRIKE}};
    private static final String[][] BEASTS = {
        {"dragon", "" + SERPENT}, {"serpent", "" + SERPENT}, {"phoenix", "" + WINGED}, {"crane", "" + WINGED}, {"eagle", "" + WINGED}, {"heron", "" + WINGED}, {"tiger", "" + CLAWED}, {"wolf", "" + CLAWED}, {"lion", "" + CLAWED},
        {"bear", "" + CLAWED}, {"mantis", "" + CLAWED}, {"monkey", "" + CLAWED}, {"tortoise", "" + SHELL}, {"qilin", "" + SHELL}};
    private static final String[][] NUMBERS = {
        {"ten thousand", "13"}, {"thousand", "11"}, {"hundred", "9"}, {"twelve", "6"}, {"single", "1"}, {"twin", "2"}, {"three", "3"}, {"seven", "5"}, {"eight", "5"}, {"nine", "6"}, {"ten", "7"}};
    private static final String[] RING_WORDS = {"heaven", "earth", "five elements", "trigrams", "two principles", "ten directions"};

    /** Reads a technique's name (and type, and element) into a look. Deterministic. */
    static Look look(String tech, boolean movement, int element) {
        Look l = new Look();
        l.tech = tech == null ? "" : tech;
        l.movement = movement;
        l.element = Math.max(0, Math.min(4, element));
        String n = l.tech.toLowerCase();
        for (String[] m : MOTIFS) if (n.contains(m[0])) { l.motif = Integer.parseInt(m[1]); break; }
        if (l.motif == NONE) l.motif = ELEMENT_MOTIF[l.element];
        for (String[] m : NUMBERS) if (n.contains(m[0])) { l.count = Integer.parseInt(m[1]); break; }
        for (String w : RING_WORDS) if (n.contains(w)) { l.ring = true; break; }
        for (String[] m : BEASTS) if (n.contains(m[0])) { l.beast = Integer.parseInt(m[1]); break; }
        if (movement) {
            if (n.contains("leap") || n.contains("flight") || n.contains("ascent") || n.contains("glide")) l.move = LEAP;
            else if (n.contains("shadow walk") || n.contains("vanishing") || n.contains("mist") || n.contains("shadow")) l.move = FADE;
            else l.move = RUSH;
        } else {
            for (String[] m : FORMS) if (n.contains(m[0])) { l.shape = Integer.parseInt(m[1]); break; }
        }
        return l;
    }

    private static Color a(Color c, double alpha) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.max(0, Math.min(255, alpha * 255))); }

    /** A small deterministic stream of numbers 0..1 from a seed. */
    private static final class Rnd {
        long s;
        Rnd(long seed) { s = seed * 6364136223846793005L + 1442695040888963407L; }
        double next() { s = s * 6364136223846793005L + 1442695040888963407L; return ((s >>> 33) % 100000) / 100000.0; }
    }

    // ================================================================== the blow
    /** Draws the blow of a martial technique. `p` is the progress of the beat (0..1); the blow lives in its middle. (x0, y0) is the attacker's chest, (x1, y1) the target's; `dir` is +1 when the attacker is on the left. */
    static void blow(Graphics2D g0, Look l, double p, double x0, double y0, double x1, double y1, int dir, Color c, long seed) {
        double q = (p - 0.28) / 0.5;
        if (q <= 0 || q >= 1) return;
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        double fade = q < 0.7 ? 1 : (1 - q) / 0.3;
        double dx = x1 - x0, dy = y1 - y0;
        double len = Math.max(30, Math.hypot(dx, dy));
        double ang = Math.atan2(dy, dx);
        double reach = Math.min(1.0, q / 0.6);                   // the blow flies out in the first 60%
        double hx = x0 + dx * reach, hy = y0 + dy * reach;       // the head of the blow
        Color core = new Color(255, 250, 230);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int copies = Math.max(1, Math.min(5, (l.count + 1) / 2));
        switch (l.shape) {
            case SLASH: {                                         // crescents sweeping across the target
                for (int i = 0; i < copies; i++) {
                    double off = (i - (copies - 1) / 2.0) * 0.5;
                    double r = 34 + 12 * i;
                    double cx = hx - dir * 6, cy = hy + off * 18;
                    g.setColor(a(c, fade * (0.9 - i * 0.12)));
                    g.setStroke(new BasicStroke(5f - i * 0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    double start = dir > 0 ? 100 - 120 * reach : 80 + 120 * reach - 180;
                    g.draw(new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, start + off * 30, 120, Arc2D.OPEN));
                }
                g.setColor(a(core, fade)); g.setStroke(new BasicStroke(2f));
                g.draw(new Arc2D.Double(hx - 30, hy - 30, 60, 60, dir > 0 ? 100 - 120 * reach : 80 + 120 * reach - 180, 120, Arc2D.OPEN));
                break;
            }
            case THRUST: {                                        // a long bolt of light with a head
                for (int i = 0; i < copies; i++) {
                    double oy = (i - (copies - 1) / 2.0) * 9;
                    g.setColor(a(c, fade * 0.8));
                    g.setStroke(new BasicStroke(3f));
                    g.drawLine((int) (x0 + dir * 10), (int) (y0 + oy), (int) hx, (int) (hy + oy));
                    g.setColor(a(core, fade));
                    int[] px = {(int) hx, (int) (hx - dir * 16), (int) (hx - dir * 16)}, py = {(int) (hy + oy), (int) (hy + oy - 6), (int) (hy + oy + 6)};
                    g.fillPolygon(px, py, 3);
                }
                break;
            }
            case STAFF: {                                         // a bar swung over the head
                double sw = -80 + 160 * q;
                double r = 60;
                double ex = x0 + dir * 12 + Math.cos(Math.toRadians(sw)) * r * dir, ey = y0 - 30 + Math.sin(Math.toRadians(sw)) * r;
                g.setColor(a(c, fade)); g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (x0 + dir * 12), (int) (y0 - 30), (int) ex, (int) ey);
                g.setColor(a(core, fade * 0.7)); g.setStroke(new BasicStroke(2f));
                g.draw(new Arc2D.Double(x0 + dir * 12 - r, y0 - 30 - r, 2 * r, 2 * r, dir > 0 ? 80 - sw : 100 + sw, dir > 0 ? sw + 80 : -(sw + 80), Arc2D.OPEN));
                break;
            }
            case PALM: {                                          // a hand-shaped shock ring that grows as it flies
                double r = 14 + 40 * reach;
                g.setColor(a(c, fade * 0.7)); g.setStroke(new BasicStroke(5f));
                g.draw(new Ellipse2D.Double(hx - r * 0.6, hy - r, r * 1.2, r * 2));
                g.setColor(a(core, fade)); g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Double(hx - r * 0.35, hy - r * 0.6, r * 0.7, r * 1.2));
                for (int f = -2; f <= 2; f++) g.drawLine((int) (hx + dir * r * 0.3), (int) (hy + f * 6), (int) (hx + dir * (r * 0.3 + 14)), (int) (hy + f * 9));
                break;
            }
            case FIST: {                                          // a hard round burst at the target
                double r = 10 + 30 * Math.sin(Math.min(1, q * 1.4) * Math.PI / 2);
                g.setColor(a(c, fade * 0.55)); g.fill(new Ellipse2D.Double(x1 - r, y1 - r, 2 * r, 2 * r));
                g.setColor(a(core, fade)); g.setStroke(new BasicStroke(3f)); g.draw(new Ellipse2D.Double(x1 - r, y1 - r, 2 * r, 2 * r));
                for (int i = 0; i < 8; i++) { double an = i * Math.PI / 4; g.drawLine((int) (x1 + Math.cos(an) * r), (int) (y1 + Math.sin(an) * r), (int) (x1 + Math.cos(an) * (r + 14)), (int) (y1 + Math.sin(an) * (r + 14))); }
                g.setColor(a(c, fade * 0.8)); g.setStroke(new BasicStroke(6f)); g.drawLine((int) (x0 + dir * 10), (int) y0, (int) hx, (int) hy);
                break;
            }
            case KICK: {                                          // a low sweeping arc
                g.setColor(a(c, fade)); g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                double r = 52;
                g.draw(new Arc2D.Double(x0 + dir * 10 - r, y0 + 20 - r, 2 * r, 2 * r, dir > 0 ? -70 : 250, dir > 0 ? 140 * reach : -140 * reach, Arc2D.OPEN));
                g.setColor(a(core, fade * 0.8)); g.setStroke(new BasicStroke(2f));
                g.draw(new Arc2D.Double(x0 + dir * 10 - r + 8, y0 + 20 - r + 8, 2 * r - 16, 2 * r - 16, dir > 0 ? -70 : 250, dir > 0 ? 140 * reach : -140 * reach, Arc2D.OPEN));
                break;
            }
            case CLAW: {                                          // three parallel rakes at the target
                g.setColor(a(c, fade)); g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                double grow = Math.min(1, q * 2);
                for (int i = -1; i <= 1; i++) g.drawLine((int) (x1 - 26 + i * 12), (int) (y1 - 38), (int) (x1 - 26 + i * 12 + 52 * grow), (int) (y1 - 38 + 70 * grow));
                g.setColor(a(core, fade)); g.setStroke(new BasicStroke(1.5f));
                for (int i = -1; i <= 1; i++) g.drawLine((int) (x1 - 26 + i * 12), (int) (y1 - 38), (int) (x1 - 26 + i * 12 + 52 * grow), (int) (y1 - 38 + 70 * grow));
                break;
            }
            case FINGER: {                                        // a thin straight beam
                g.setColor(a(c, fade * 0.5)); g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (x0 + dir * 10), (int) (y0 - 4), (int) hx, (int) (hy - 4));
                g.setColor(a(core, fade)); g.setStroke(new BasicStroke(2.5f));
                g.drawLine((int) (x0 + dir * 10), (int) (y0 - 4), (int) hx, (int) (hy - 4));
                g.fill(new Ellipse2D.Double(hx - 5, hy - 9, 10, 10));
                break;
            }
            case FAN: {                                           // a spread of crescents opening like a fan
                int blades = 3 + Math.min(4, copies);
                for (int i = 0; i < blades; i++) {
                    double spread = (i - (blades - 1) / 2.0) * 22 * (0.4 + reach * 0.6);
                    double bx = x0 + dir * (24 + 70 * reach), by = y0 + spread * 1.2;
                    g.setColor(a(c, fade * 0.85)); g.setStroke(new BasicStroke(3f));
                    g.draw(new Arc2D.Double(bx - 14, by - 14, 28, 28, dir > 0 ? -60 : 120, 120, Arc2D.OPEN));
                }
                break;
            }
            case GLYPHS: {                                        // squares of writing that rise and fly
                Rnd r = new Rnd(seed);
                int n = 4 + copies * 2;
                g.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < n; i++) {
                    double t = Math.min(1, reach * (0.6 + 0.4 * r.next()));
                    double gx = x0 + dx * t + (r.next() - 0.5) * 24, gy = y0 + dy * t - 30 * Math.sin(t * Math.PI) + (r.next() - 0.5) * 30;
                    g.setColor(a(i % 2 == 0 ? c : core, fade * 0.9));
                    g.draw(new java.awt.geom.Rectangle2D.Double(gx - 6, gy - 7, 12, 14));
                    g.drawLine((int) gx - 4, (int) gy - 3, (int) gx + 4, (int) gy - 3); g.drawLine((int) gx - 4, (int) gy + 2, (int) gx + 4, (int) gy + 2); g.drawLine((int) gx, (int) gy - 7, (int) gx, (int) gy + 7);
                }
                break;
            }
            case SEAL: {                                          // a circle with a star in it that drops on the target
                double r = 34 + 6 * copies;
                double sy = y1 - 70 + 60 * Math.min(1, q * 1.6);
                g.setColor(a(c, fade)); g.setStroke(new BasicStroke(3f));
                g.draw(new Ellipse2D.Double(x1 - r, sy - r * 0.45, 2 * r, r * 0.9));
                g.setStroke(new BasicStroke(1.8f));
                g.draw(new Ellipse2D.Double(x1 - r * 0.7, sy - r * 0.32, 1.4 * r, 0.64 * r));
                double rot = q * 3;
                for (int i = 0; i < 5; i++) {
                    double a1 = rot + i * 2 * Math.PI / 5 * 2, a2 = rot + (i + 1) * 2 * Math.PI / 5 * 2;
                    g.drawLine((int) (x1 + Math.cos(a1) * r * 0.7), (int) (sy + Math.sin(a1) * r * 0.32), (int) (x1 + Math.cos(a2) * r * 0.7), (int) (sy + Math.sin(a2) * r * 0.32));
                }
                break;
            }
            default: {                                            // a plain blow
                g.setColor(a(c, fade * 0.8)); g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (x0 + dir * 10), (int) y0, (int) hx, (int) hy);
                double r = 8 + 14 * q;
                g.setColor(a(core, fade)); g.draw(new Ellipse2D.Double(x1 - r, y1 - r, 2 * r, 2 * r));
            }
        }
        if (l.beast != NO_BEAST) beast(g, l, q, fade, x0, y0, dx, dy, ang, dir, c, core);
        motif(g, l, q, fade, x0, y0, dx, dy, x1, y1, c, core, seed);
        if (l.ring) runeRing(g, q, fade, x0 + dir * 4, y0 + 10, 44 + 10 * Math.min(2, l.count / 4), c);
        g.dispose();
    }

    private static void beast(Graphics2D g, Look l, double q, double fade, double x0, double y0, double dx, double dy, double ang, int dir, Color c, Color core) {
        double t = Math.min(1, q / 0.65);
        double bx = x0 + dx * t, by = y0 + dy * t - 18;
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(a(c, fade * 0.8));
        switch (l.beast) {
            case SERPENT: {                                       // a body of waves with a head
                Path2D.Double body = new Path2D.Double();
                int segs = 18;
                for (int i = 0; i <= segs; i++) {
                    double u = i / (double) segs;
                    double px = x0 + dx * t * (1 - u * 0.7) - dir * 0;
                    double wx = x0 + (bx - x0) * (1 - u * 0.75);
                    double wy = y0 - 18 + dy * t * (1 - u * 0.75) + Math.sin(u * 9 + q * 12) * (10 + 6 * u);
                    if (i == 0) body.moveTo(wx, wy); else body.lineTo(wx, wy);
                }
                g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(body);
                g.setColor(a(core, fade)); g.fill(new Ellipse2D.Double(bx - 8, by + 18 - 8 + Math.sin(q * 12) * 0, 16, 14));
                break;
            }
            case WINGED: {                                        // a bird with two wings that beat
                double flap = Math.sin(q * 18) * 14;
                g.draw(new java.awt.geom.QuadCurve2D.Double(bx, by, bx - dir * 18, by - 24 - flap, bx - dir * 40, by - 6 - flap));
                g.draw(new java.awt.geom.QuadCurve2D.Double(bx, by, bx - dir * 18, by + 14 + flap, bx - dir * 36, by + 24 + flap));
                g.setColor(a(core, fade)); g.fill(new Ellipse2D.Double(bx - 6, by - 6, 14, 12));
                g.setColor(a(c, fade)); g.drawLine((int) (bx + dir * 6), (int) by - 2, (int) (bx + dir * 16), (int) by);
                break;
            }
            case CLAWED: {                                        // a head with ears running ahead of the blow
                g.setColor(a(c, fade * 0.9)); g.setStroke(new BasicStroke(4f));
                g.draw(new Ellipse2D.Double(bx - 12, by - 10, 24, 22));
                g.drawLine((int) bx - 9, (int) by - 8, (int) bx - 12, (int) by - 20); g.drawLine((int) bx + 9, (int) by - 8, (int) bx + 12, (int) by - 20);
                g.setColor(a(core, fade)); g.fill(new Ellipse2D.Double(bx - 6, by - 3, 4, 4)); g.fill(new Ellipse2D.Double(bx + 3, by - 3, 4, 4));
                for (int i = -1; i <= 1; i++) g.drawLine((int) (bx - dir * 20), (int) (by + i * 9), (int) (bx - dir * 54), (int) (by + i * 9));
                break;
            }
            default: {                                            // SHELL: a dome of light at the attacker's feet that shoves forward
                double r = 30 + 18 * q;
                g.setColor(a(c, fade * 0.7)); g.setStroke(new BasicStroke(4f));
                g.draw(new Arc2D.Double(x0 + dir * 30 * q - r, y0 + 30 - r, 2 * r, 2 * r, 0, 180, Arc2D.OPEN));
                g.setColor(a(core, fade * 0.8)); g.setStroke(new BasicStroke(1.5f));
                for (int i = 1; i < 4; i++) g.draw(new Arc2D.Double(x0 + dir * 30 * q - r * i / 4.0, y0 + 30 - r * i / 4.0, 2 * r * i / 4.0, 2 * r * i / 4.0, 0, 180, Arc2D.OPEN));
            }
        }
    }

    /** The sparks of the element's word: they fly along the blow and burst at the target. */
    private static void motif(Graphics2D g, Look l, double q, double fade, double x0, double y0, double dx, double dy, double x1, double y1, Color c, Color core, long seed) {
        Rnd r = new Rnd(seed ^ 0x9E3779B97F4A7C15L);
        int n = Math.min(22, 6 + l.count * 2);
        double t = Math.min(1, q / 0.6);
        for (int i = 0; i < n; i++) {
            double u = r.next(), v = r.next(), w = r.next();
            double px = x0 + dx * t * u + (v - 0.5) * 26, py = y0 + dy * t * u + (w - 0.5) * 50;
            double drift = q * 30;
            Color cc = a(i % 3 == 0 ? core : c, fade * (0.5 + 0.5 * w));
            g.setColor(cc);
            switch (l.motif) {
                case LEAVES: { double lx = px + Math.sin(q * 9 + i) * 10, ly = py + drift * 0.6; g.fill(new Ellipse2D.Double(lx - 4, ly - 2, 9, 4)); break; }
                case EMBERS: { double ly = py - drift; g.fill(new Ellipse2D.Double(px - 2, ly - 2, 4 + w * 3, 4 + w * 3)); break; }
                case RAYS: { double an = i * 2 * Math.PI / n; g.setStroke(new BasicStroke(2f)); g.drawLine((int) x1, (int) y1 - 20, (int) (x1 + Math.cos(an) * (20 + 50 * q)), (int) (y1 - 20 + Math.sin(an) * (20 + 50 * q))); break; }
                case SHARDS: { double ly = py + drift * 1.4; int[] sx = {(int) px, (int) px + 6, (int) px - 3}, sy = {(int) ly - 6, (int) ly + 3, (int) ly + 5}; g.fillPolygon(sx, sy, 3); break; }
                case GLINTS: { g.setStroke(new BasicStroke(1.5f)); int s = (int) (3 + 4 * Math.abs(Math.sin(q * 10 + i))); g.drawLine((int) px - s, (int) py, (int) px + s, (int) py); g.drawLine((int) px, (int) py - s, (int) px, (int) py + s); break; }
                case DROPS: { double ly = py + drift * 1.2; g.fill(new Ellipse2D.Double(px - 2, ly - 3, 4, 7)); break; }
                case CRYSTALS: { g.setStroke(new BasicStroke(1.5f)); int s = 4 + (int) (w * 4); for (int k = 0; k < 3; k++) { double an = k * Math.PI / 3 + q; g.drawLine((int) (px - Math.cos(an) * s), (int) (py - Math.sin(an) * s), (int) (px + Math.cos(an) * s), (int) (py + Math.sin(an) * s)); } break; }
                case MIST: { g.setColor(a(c, fade * 0.18)); g.fill(new Ellipse2D.Double(px - 14, py - 8 - drift * 0.2, 30 + w * 14, 16)); break; }
                case WAVES: { g.setStroke(new BasicStroke(2f)); Path2D.Double pw = new Path2D.Double(); double wy = y1 + 26 - i * 5 % 40; pw.moveTo(x1 - 36, wy); for (int k = 1; k <= 8; k++) pw.lineTo(x1 - 36 + k * 9, wy + Math.sin(k + q * 9 + i) * 4); if (i % 3 == 0) g.draw(pw); break; }
                case BOLTS: { if (i % 3 != 0) break; g.setStroke(new BasicStroke(2.5f)); double bx = x1 + (v - 0.5) * 50, by = y1 - 90; int[] zx = new int[6], zy = new int[6]; for (int k = 0; k < 6; k++) { zx[k] = (int) (bx + (k % 2 == 0 ? -7 : 7) * (1 - k / 8.0) + (w - 0.5) * 6); zy[k] = (int) (by + k * 22); } g.drawPolyline(zx, zy, 6); break; }
                case SPIRAL: { g.setStroke(new BasicStroke(2f)); double an = q * 10 + i * 0.7, rad = 8 + i * 2.2; g.drawArc((int) (x0 + dx * t - rad), (int) (y0 + dy * t - rad * 0.6), (int) (rad * 2), (int) (rad * 1.2), (int) Math.toDegrees(an) % 360, 110); break; }
                case DARK: { g.setColor(new Color(20, 14, 30, (int) (fade * 140 * (0.4 + 0.6 * w)))); g.fill(new Ellipse2D.Double(px - 12, py - 12, 26 + 12 * w, 26 + 12 * w)); break; }
                case CRESCENT: { if (i % 4 != 0) break; g.setStroke(new BasicStroke(3f)); g.draw(new Arc2D.Double(px - 12, py - 12, 24, 24, 40 + q * 100, 200, Arc2D.OPEN)); break; }
                case STARS: { if (i % 2 != 0) break; g.setStroke(new BasicStroke(1.5f)); int s = 5; for (int k = 0; k < 5; k++) { double a1 = -Math.PI / 2 + k * 4 * Math.PI / 5, a2 = -Math.PI / 2 + (k + 1) * 4 * Math.PI / 5; g.drawLine((int) (px + Math.cos(a1) * s), (int) (py + Math.sin(a1) * s), (int) (px + Math.cos(a2) * s), (int) (py + Math.sin(a2) * s)); } break; }
                case BLOOD: { g.setColor(a(new Color(190, 30, 40), fade * (0.5 + 0.5 * w))); double ly = py + drift; g.fill(new Ellipse2D.Double(px - 2, ly - 3, 4 + w * 2, 6 + w * 3)); break; }
                case ORB: { g.setColor(a(core, fade * 0.35)); double rr = 5 + 5 * Math.abs(Math.sin(q * 6 + i)); g.fill(new Ellipse2D.Double(px - rr, py - rr - drift * 0.4, 2 * rr, 2 * rr)); break; }
                default: break;
            }
        }
    }

    private static void runeRing(Graphics2D g, double q, double fade, double cx, double cy, double r, Color c) {
        g.setColor(a(c, fade * 0.8)); g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(cx - r, cy - r * 0.42, 2 * r, r * 0.84));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Double(cx - r * 0.8, cy - r * 0.34, 1.6 * r, 0.68 * r));
        for (int i = 0; i < 12; i++) {
            double an = q * 2.4 + i * Math.PI / 6;
            double px = cx + Math.cos(an) * r * 0.9, py = cy + Math.sin(an) * r * 0.38;
            g.drawLine((int) px - 2, (int) py - 3, (int) px + 2, (int) py + 3); g.drawLine((int) px - 2, (int) py + 1, (int) px + 3, (int) py - 1);
        }
    }

    // ================================================================== the movement
    /** How a movement technique shows on its fighter: the sideways offset of the after-images (the caller draws the fighter again at each), the height of a leap, and the opacity of the fighter. `p` is the beat's progress.
     *  Returns {ghostCount, ghostStep, lift, opacity}. */
    static double[] motion(Look l, double p) {
        double act = p < 0.2 ? p / 0.2 : p < 0.7 ? 1 : Math.max(0, 1 - (p - 0.7) / 0.3);
        int ghosts = Math.min(6, 2 + l.count / 2);
        switch (l.move) {
            case LEAP: return new double[]{ghosts - 1, 10, -70 * Math.sin(Math.min(1, p) * Math.PI), 1};
            case FADE: return new double[]{ghosts, 6, 0, 1 - 0.75 * Math.sin(Math.min(1, p) * Math.PI)};
            default: return new double[]{ghosts, 16 * act, 0, 1};
        }
    }

    /** Streaks and dust at a mover's feet, in the element's colour. */
    static void movement(Graphics2D g0, Look l, double p, double cx, double ground, int dir, Color c, long seed) {
        if (p <= 0.05 || p >= 0.95) return;
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        Rnd r = new Rnd(seed);
        double fade = 1 - Math.abs(p - 0.5) * 2 * 0.7;
        int n = 5 + l.count;
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n; i++) {
            double y = ground - 12 - r.next() * 80, len = 18 + r.next() * 40, off = r.next() * 30;
            g.setColor(a(c, fade * 0.55));
            g.drawLine((int) (cx - dir * (20 + off)), (int) y, (int) (cx - dir * (20 + off + len)), (int) y);
        }
        if (l.motif == LEAVES || l.motif == SHARDS || l.motif == EMBERS || l.motif == DROPS) motif(g, l, p, fade, cx - dir * 40, ground - 30, dir * 40, 0, cx, ground - 30, c, new Color(255, 250, 230), seed);
        g.dispose();
    }

    // ================================================================== the stage of a tournament
    /** Draws the setting behind the fighters. kind 1: an ordinary tournament (a crowd, banners, lanterns); kind 2: the Garden's Variety Tournament (night sky, floating platforms, falling light, a carved stone platform). `hit` is 0..1, how
     *  recently a blow landed (a ring on the platform and a shake); `finish` is 0..1, how near the last blow is (a flash). */
    static void stage(Graphics2D g0, int kind, int w, int h, int ground, long ms, double hit, double finish, boolean wonA) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        if (kind == 2) garden(g, w, h, ground, ms, hit, finish);
        else arena(g, w, h, ground, ms, hit, finish);
        g.dispose();
    }

    private static void arena(Graphics2D g, int w, int h, int ground, long ms, double hit, double finish) {
        // the sand
        g.setPaint(new java.awt.GradientPaint(0, ground - 6, new Color(120, 98, 62, 160), 0, h, new Color(70, 56, 36, 200)));
        g.fillRect(0, ground - 6, w, h - ground + 6);
        // the crowd: rows of heads that bob, more of them in the back
        Rnd r = new Rnd(7);
        for (int row = 0; row < 3; row++) {
            int y = (int) (h * (0.16 + row * 0.07));
            int n = 26 - row * 2;
            for (int i = 0; i < n; i++) {
                double x = (i + 0.5 + (row % 2) * 0.5) * (w / (double) n);
                double bob = Math.sin(ms / (260.0 + row * 40) + i * 1.7) * (2 + hit * 6);
                g.setColor(new Color(24 + row * 10, 26 + row * 8, 34 + row * 8, 220));
                g.fill(new Ellipse2D.Double(x - 7, y + bob - 7, 14, 14));
                g.fillRoundRect((int) x - 9, (int) (y + bob + 6), 18, 20, 8, 8);
                if (r.next() < 0.12) { g.setColor(a(new Color(220, 80, 60), 0.9)); g.fillRect((int) x + 8, (int) (y + bob - 12), 3, 18); g.fillRect((int) x + 11, (int) (y + bob - 12), 8, 6); } // a small flag
            }
        }
        // banners hanging from the top
        for (int i = 0; i < 6; i++) {
            double x = (i + 0.5) * (w / 6.0);
            double sway = Math.sin(ms / 700.0 + i) * 4;
            Color bc = i % 2 == 0 ? new Color(160, 40, 40) : new Color(190, 150, 50);
            g.setColor(a(bc, 0.85));
            int[] px = {(int) (x - 16 + sway), (int) (x + 16 + sway), (int) (x + sway)}, py = {0, 0, 66};
            g.fillPolygon(px, py, 3);
            g.setColor(a(new Color(255, 240, 200), 0.6)); g.setStroke(new BasicStroke(1.5f)); g.drawLine((int) (x + sway), 8, (int) (x + sway), 44);
        }
        // lanterns glowing on both sides
        for (int side = 0; side < 2; side++) {
            double x = side == 0 ? 44 : w - 44;
            for (int k = 0; k < 2; k++) {
                double y = ground - 120 - k * 40;
                double glow = 0.5 + 0.2 * Math.sin(ms / 300.0 + k + side);
                g.setColor(a(new Color(255, 190, 90), 0.12 * glow)); g.fill(new Ellipse2D.Double(x - 42, y - 42, 84, 84));
                g.setColor(a(new Color(230, 70, 50), 0.95)); g.fill(new Ellipse2D.Double(x - 9, y - 12, 18, 24));
                g.setColor(a(new Color(255, 220, 120), 0.9)); g.fill(new Ellipse2D.Double(x - 4, y - 6, 8, 12));
            }
        }
        if (hit > 0.05) { g.setColor(a(new Color(255, 245, 210), hit * 0.2)); g.fillRect(0, 0, w, h); } // a flash at every hard blow
        if (finish > 0) { g.setColor(a(new Color(255, 230, 150), finish * 0.25)); g.fillRect(0, 0, w, h); }
    }

    private static void garden(Graphics2D g, int w, int h, int ground, long ms, double hit, double finish) {
        // a night sky
        g.setPaint(new java.awt.GradientPaint(0, 0, new Color(8, 10, 34), 0, h, new Color(40, 24, 62)));
        g.fillRect(0, 0, w, h);
        Rnd r = new Rnd(31);
        for (int i = 0; i < 60; i++) { double x = r.next() * w, y = r.next() * h * 0.7; double tw = 0.4 + 0.6 * Math.abs(Math.sin(ms / 900.0 + i)); g.setColor(a(new Color(255, 250, 230), 0.5 * tw)); g.fill(new Ellipse2D.Double(x, y, 2, 2)); }
        g.setColor(a(new Color(250, 240, 210), 0.9)); g.fill(new Ellipse2D.Double(w * 0.80, h * 0.10, 54, 54));
        g.setColor(a(new Color(250, 240, 210), 0.12)); g.fill(new Ellipse2D.Double(w * 0.80 - 30, h * 0.10 - 30, 114, 114));
        // platforms drifting in the dark, the farther ones smaller and paler
        for (int i = 0; i < 9; i++) {
            double depth = 0.35 + (i % 3) * 0.3;
            double x = ((i * 137 + ms / (40.0 + i * 8)) % (w + 120)) - 60, y = h * (0.12 + ((i * 53) % 40) / 100.0) + Math.sin(ms / 1400.0 + i) * 6;
            double pw = 70 * depth, ph = 12 * depth;
            g.setColor(a(new Color(120, 124, 160), 0.55 * depth)); g.fill(new Ellipse2D.Double(x - pw / 2, y - ph / 2, pw, ph));
            g.setColor(a(new Color(70, 70, 100), 0.55 * depth)); g.fill(new Ellipse2D.Double(x - pw / 2 + 4, y, pw - 8, ph * 0.9));
        }
        // falling light
        Rnd p = new Rnd(5);
        for (int i = 0; i < 38; i++) {
            double x = p.next() * w, speed = 18 + p.next() * 30, ph = p.next() * 1000;
            double y = ((ph + ms / 1000.0 * speed) % (h + 20)) - 10;
            double sway = Math.sin(ms / 800.0 + i) * 12;
            g.setColor(a(i % 4 == 0 ? new Color(255, 214, 120) : new Color(230, 220, 255), 0.55));
            g.fill(new Ellipse2D.Double(x + sway, y, 3 + p.next() * 2, 3 + p.next() * 2));
        }
        // the stone platform under the fighters
        double cx = w / 2.0, rx = w * 0.40, ry = 28;
        g.setColor(new Color(0, 0, 0, 90)); g.fill(new Ellipse2D.Double(cx - rx - 10, ground - ry + 18, 2 * rx + 20, 2 * ry));
        g.setPaint(new java.awt.GradientPaint(0f, (float) (ground - ry), new Color(176, 176, 196), 0f, (float) (ground + ry), new Color(96, 96, 124)));
        g.fill(new Ellipse2D.Double(cx - rx, ground - ry, 2 * rx, 2 * ry));
        g.setColor(new Color(72, 70, 96)); g.fill(new java.awt.geom.Rectangle2D.Double(cx - rx, ground, 2 * rx, 12)); // the thickness of the slab
        g.setColor(new Color(214, 214, 230)); g.setStroke(new BasicStroke(2f)); g.draw(new Ellipse2D.Double(cx - rx, ground - ry, 2 * rx, 2 * ry));
        // the carved runes, turning slowly
        g.setColor(a(new Color(255, 214, 120), 0.45 + hit * 0.4)); g.setStroke(new BasicStroke(1.4f));
        for (int i = 0; i < 28; i++) {
            double an = ms / 3000.0 + i * 2 * Math.PI / 28;
            double px = cx + Math.cos(an) * rx * 0.86, py = ground + Math.sin(an) * ry * 0.82 - 2;
            g.drawLine((int) px - 2, (int) py - 3, (int) px + 3, (int) py + 1); g.drawLine((int) px + 2, (int) py - 3, (int) px - 3, (int) py + 1);
        }
        g.draw(new Ellipse2D.Double(cx - rx * 0.7, ground - ry * 0.7, rx * 1.4, ry * 1.4));
        // a ring ripples out from every blow
        if (hit > 0.02) {
            double k = 1 - hit;
            g.setColor(a(new Color(255, 224, 140), hit * 0.8)); g.setStroke(new BasicStroke(3f));
            g.draw(new Ellipse2D.Double(cx - rx * (0.2 + 0.8 * k), ground - ry * (0.2 + 0.8 * k), 2 * rx * (0.2 + 0.8 * k), 2 * ry * (0.2 + 0.8 * k)));
        }
        if (finish > 0) { g.setColor(a(new Color(255, 244, 200), finish * 0.55)); g.fillRect(0, 0, w, h); }
    }
}
