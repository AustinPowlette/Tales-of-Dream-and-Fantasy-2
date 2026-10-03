package Game;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import static Game.ArtGen.c;
import static Game.ArtGen.mix;
import static Game.ArtGen.poly;

/** More painted pictures in the same watercolour style as ArtGen: portraits of cultivators and beasts, sect seals, item icons, a painted map of the
 *  whole world, illustrations for the big events of the chronicle, and the weather of a tile. Everything is drawn from seeds, so the same person (or
 *  sect, or item) always looks the same, and nothing needs a file. */
final class ArtMore {
    private ArtMore() { }

    // ------------------------------------------------------------------ helpers
    private static Graphics2D gfx(BufferedImage im) {
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    private static BufferedImage finish(BufferedImage im, Random r, boolean paint) {
        if (paint && im.getWidth() >= 64) { im = ArtGen.kuwahara(im, 2); ArtGen.paper(im, r); }
        return im;
    }

    private static final java.util.Map<String, BufferedImage> portraitCache = new java.util.LinkedHashMap<String, BufferedImage>(64, 0.75f, true) {
        @Override protected boolean removeEldestEntry(java.util.Map.Entry<String, BufferedImage> e) { return size() > 48; }
    };

    /** A portrait that is painted once and kept while the person looks the same (the level band, the age decade and the sect matter to the picture). */
    static synchronized BufferedImage portraitCached(Character ch, int w, int h) {
        String key = ch.id + "|" + w + "x" + h + "|" + ch.lvl / 10 + "|" + ch.age / 10 + "|" + ch.kills / 50 + "|" + (ch.sect == null ? "" : ch.sect.name) + "|" + ch.trait;
        BufferedImage im = portraitCache.get(key);
        if (im == null) { im = portrait(ch, w, h); portraitCache.put(key, im); }
        return im;
    }

    // ------------------------------------------------------------------ the game's emblem
    /** The logo of the game: a jade moon-disc with a gold rim, a pale moon, three ranges of ink mountains with a pagoda on the nearest peak and a red seal with the character for dream.
     *  Drawn as vectors at any size (used for the window icon, the taskbar and `tools/MakeLogo.java` which writes the PNGs and the .ico for the .exe). */
    static BufferedImage logo(int size) {
        BufferedImage im = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gfx(im);
        g.scale(size / 256.0, size / 256.0);
        java.awt.geom.Ellipse2D.Double disc = new java.awt.geom.Ellipse2D.Double(8, 8, 240, 240);
        g.setPaint(new java.awt.GradientPaint(0, 8, c(34, 92, 88), 0, 248, c(10, 28, 36)));
        g.fill(disc);
        java.awt.Shape oldClip = g.getClip();
        g.setClip(disc);
        // the moon with a soft glow
        for (int i = 6; i >= 1; i--) { g.setColor(new Color(255, 244, 214, 14 + i * 3)); g.fill(new Ellipse2D.Double(150 - 44 - i * 7, 92 - 44 - i * 7, 88 + i * 14, 88 + i * 14)); }
        g.setPaint(new java.awt.GradientPaint(120, 50, c(255, 250, 232), 180, 140, c(236, 222, 186)));
        g.fill(new Ellipse2D.Double(150 - 42, 92 - 42, 84, 84));
        // three ranges, far to near
        g.setColor(c(74, 126, 124)); g.fill(poly2(0, 170, 40, 132, 70, 150, 112, 108, 150, 146, 190, 120, 256, 160, 256, 256, 0, 256));
        g.setColor(c(42, 94, 92)); g.fill(poly2(0, 196, 34, 166, 66, 180, 104, 140, 140, 176, 176, 150, 214, 178, 256, 168, 256, 256, 0, 256));
        g.setColor(c(14, 44, 50)); g.fill(poly2(0, 226, 40, 196, 76, 214, 118, 176, 160, 212, 204, 192, 256, 220, 256, 256, 0, 256));
        // a pagoda on the middle peak
        g.setColor(c(206, 70, 56));
        for (int t = 0; t < 4; t++) { double w = 18 - t * 3, y = 150 - t * 12; g.fill(new java.awt.geom.Rectangle2D.Double(112 - w / 2, y - 10, w, 10)); }
        g.setColor(c(238, 196, 96));
        for (int t = 0; t < 4; t++) { double w = 26 - t * 4, y = 150 - t * 12; g.fill(poly2(112 - w / 2 - 2, y - 9, 112, y - 15, 112 + w / 2 + 2, y - 9)); }
        g.setStroke(new BasicStroke(1.8f)); g.drawLine(112, 104, 112, 92);
        // mist
        for (int i = 0; i < 3; i++) { g.setColor(new Color(240, 244, 236, 40)); g.fill(new java.awt.geom.RoundRectangle2D.Double(-10 + i * 40, 168 + i * 18, 190, 8, 8, 8)); }
        g.setClip(oldClip);
        // the gold rim
        g.setColor(c(222, 178, 84)); g.setStroke(new BasicStroke(6f)); g.draw(new Ellipse2D.Double(11, 11, 234, 234));
        g.setColor(new Color(222, 178, 84, 120)); g.setStroke(new BasicStroke(2f)); g.draw(new Ellipse2D.Double(21, 21, 214, 214));
        // the red seal, lower right, with the character for dream
        g.setColor(c(192, 40, 36)); g.fill(new RoundRectangle2D.Double(168, 168, 70, 70, 12, 12));
        g.setColor(c(250, 236, 220)); g.setStroke(new BasicStroke(2.4f)); g.draw(new RoundRectangle2D.Double(174, 174, 58, 58, 8, 8));
        Font f = new Font(Theme.CJK, Font.BOLD, 48);
        g.setFont(f);
        String glyph = "夢"; // 夢
        if (!f.canDisplay(glyph.charAt(0))) glyph = "仙"; // 仙 as a fallback
        FontMetrics fm = g.getFontMetrics();
        if (f.canDisplay(glyph.charAt(0))) g.drawString(glyph, (float) (203 - fm.stringWidth(glyph) / 2.0), (float) (203 + fm.getAscent() * 0.34));
        g.dispose();
        return im;
    }

    /** The emblem at the usual window-icon sizes (the window manager picks one). */
    static java.util.List<java.awt.Image> logoImages() {
        java.util.List<java.awt.Image> l = new java.util.ArrayList<>();
        for (int s : new int[]{16, 24, 32, 48, 64, 128, 256}) l.add(logo(s));
        return l;
    }

    // ------------------------------------------------------------------ portraits
    /** A portrait of a cultivator (or a beast) at w x h. The face, hair, robe and aura follow who they are: gender, age, trait, alignment, level, kills. */
    static BufferedImage portrait(Character ch, int w, int h) {
        long seed = ch.id * 7919L + (ch.name == null ? 0 : ch.name.hashCode()) * 31L + (ch.lastName == null ? 0 : ch.lastName.hashCode());
        Random r = new Random(Naming.mix64(seed));
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = gfx(im);
        g.scale(w / 256.0, h / 320.0);
        ArtGen.Painter p = new ArtGen.Painter(g, r, 1.0, ArtGen.palette("", r), "", null);
        boolean beast = ch.trait == Character.Traits.Beast || ch.trait == Character.Traits.Monster;
        boolean female = "Female".equals(ch.gender);
        int aff = ch.affiliationNum;
        // paper, a pale mountain and a sun or moon behind
        Color bg = aff < 0 ? c(220, 204, 196) : aff > 0 ? c(226, 234, 220) : c(234, 226, 206);
        g.setColor(bg); g.fillRect(0, 0, 256, 320);
        p.washRect(0, 0, 256, 320, mix(bg, c(180, 196, 196), 0.5), 5, 0.4, 0.1);
        p.wash(poly(0, 250, 40, 190, 90, 230, 140, 160, 190, 215, 256, 180, 256, 320, 0, 320), mix(c(150, 176, 176), bg, 0.4), 4, 0.5, 0.08);
        p.mist(236, 40, 0.5);
        double ageT = Math.max(0, Math.min(1, ch.age / (double) Math.max(40, ch.maxAge)));
        if (ch.lvl >= 30) { // a spirit glow round the head grows with the level
            double rad = 70 + Math.min(60, ch.lvl);
            p.washCircle(128, 120, rad, rad, c(255, 236, 170), 5, 0.30 + Math.min(0.3, ch.lvl / 300.0), 0.1);
        }
        if (beast) { beastFace(p, g, ch, r); } else {
            // robe
            Color robe = robeColor(ch);
            p.wash(poly(30, 320, 48, 238, 98, 206, 158, 206, 208, 238, 226, 320), robe, 5, 0.95, 0.04);
            p.wash(poly(98, 206, 128, 262, 158, 206, 146, 204, 128, 232, 110, 204), mix(c(246, 240, 226), robe, 0.25), 3, 0.9, 0.03); // collar
            p.stroke(98, 206, 128, 262, 2.2, mix(robe, c(20, 20, 20), 0.45), 0.8); p.stroke(158, 206, 128, 262, 2.2, mix(robe, c(20, 20, 20), 0.45), 0.8);
            p.stroke(70, 300, 186, 296, 7, mix(robe, c(250, 250, 240), 0.35), 0.55); // sash
            // neck and face
            Color skin = skinTone(r, aff);
            p.washRect(112, 176, 32, 40, mix(skin, c(60, 40, 30), 0.12), 3, 0.95, 0.04);
            // hair behind
            Color hair = ageT > 0.62 ? mix(c(40, 36, 36), c(236, 236, 232), Math.min(1, (ageT - 0.5) * 2.2)) : c(26 + r.nextInt(18), 22 + r.nextInt(14), 24 + r.nextInt(14));
            int style = r.nextInt(3);
            if (female || style == 0) p.wash(poly(76, 120, 84, 70, 128, 52, 172, 70, 180, 120, 190, 250, 170, 262, 160, 160, 96, 160, 86, 262, 66, 250), hair, 5, 0.97, 0.05); // long
            else p.washCircle(128, 96, 56, 50, hair, 4, 0.95, 0.06);
            p.washCircle(128, 134, 46, 56, skin, 5, 0.96, 0.03);                                                                           // face
            p.washCircle(128, 90, 52, 26, hair, 4, 0.97, 0.05);                                                                            // fringe
            if (!female && style == 1) { p.washCircle(128, 46, 17, 17, hair, 4, 0.97, 0.05); p.stroke(110, 60, 146, 60, 3.5, c(190, 150, 70), 0.9); } // topknot and pin
            if (female && style == 2) p.washCircle(94, 74, 16, 16, hair, 3, 0.97, 0.05);                                                  // side bun
            if (female && r.nextBoolean()) { p.stroke(160, 70, 186, 52, 2.4, c(200, 160, 70), 0.9); p.washCircle(188, 50, 5, 5, c(210, 60, 60), 3, 0.9, 0.1); }
            // face
            Color ink = c(40, 30, 28);
            double eye = r.nextDouble() * 4 - 2;
            p.stroke(100, 128 + eye, 118, 126, 3.2, ink, 0.9); p.stroke(138, 126, 156, 128 + eye, 3.2, ink, 0.9);            // eyes
            double brow = aff < 0 ? -3 : aff > 0 ? 1.5 : 0;
            p.stroke(98, 114 - brow, 120, 112 + brow, 2.4, ink, 0.8); p.stroke(136, 112 + brow, 158, 114 - brow, 2.4, ink, 0.8);
            p.stroke(128, 136, 125, 152, 1.6, mix(skin, ink, 0.5), 0.6);                                                      // nose
            double smile = aff > 0 ? 3 : aff < 0 ? -2.5 : 0.5;
            p.stroke(114, 166, 128, 166 + smile, 2.2, c(150, 60, 56), 0.85); p.stroke(128, 166 + smile, 142, 166, 2.2, c(150, 60, 56), 0.85); // mouth
            p.washCircle(104, 148, 9, 6, c(240, 150, 140), 2, 0.28, 0.2); p.washCircle(152, 148, 9, 6, c(240, 150, 140), 2, 0.28, 0.2);   // cheeks
            if (ch.kills >= 100) p.stroke(104, 100, 122, 150, 2.2, c(150, 60, 56), 0.7);                                       // an old scar
            if (!female && ageT > 0.55) p.wash(poly(112, 176, 144, 176, 138, 232, 128, 246, 118, 232), mix(hair, c(250, 250, 250), 0.5), 4, 0.9, 0.05); // beard
            if (ch.trait == Character.Traits.Musician) p.stroke(40, 300, 210, 250, 4, c(120, 80, 50), 0.9);                      // a guqin laid across
            if (ch.trait == Character.Traits.Alchemist) p.washCircle(206, 262, 16, 16, c(110, 150, 110), 4, 0.95, 0.05);       // a small cauldron
            if (ch.trait == Character.Traits.Blacksmith) p.stroke(210, 300, 226, 236, 5, c(90, 90, 96), 0.95);
            if (ch.trait == Character.Traits.Scribe) { p.washRect(30, 262, 28, 40, c(236, 226, 196), 3, 0.95, 0.04); p.stroke(44, 268, 44, 296, 1.5, ink, 0.6); }
        }
        // a seal in the corner for sect members, a frame
        if (ch.sect != null && ch.sect.name != null) {
            BufferedImage seal = seal(ch.sect.name, 44);
            g.drawImage(seal, 6, 6, 44, 44, null);
        }
        g.setColor(c(110, 90, 70)); g.setStroke(new BasicStroke(3f)); g.drawRect(2, 2, 252, 316);
        g.dispose();
        return finish(im, r, true);
    }

    private static Color skinTone(Random r, int aff) {
        Color[] s = {c(246, 220, 196), c(240, 208, 180), c(228, 190, 156), c(206, 160, 124), c(168, 120, 90)};
        Color k = s[r.nextInt(s.length)];
        return aff < 0 ? mix(k, c(220, 214, 214), 0.25) : k;
    }

    private static Color robeColor(Character ch) {
        if (ch.trait != null) switch (ch.trait) {
            case Alchemist: return c(104, 150, 112);
            case Blacksmith: return c(160, 92, 66);
            case Scribe: return c(86, 114, 160);
            case Merchant: return c(196, 160, 70);
            case Leader: return c(132, 84, 150);
            case Musician: return c(70, 150, 150);
            case Chef: return c(214, 130, 60);
            case Poisoner: return c(110, 70, 140);
            case Tamer: return c(140, 110, 70);
            case Drunken: return c(160, 60, 70);
            default: break;
        }
        return ch.affiliationNum < 0 ? c(110, 56, 62) : ch.affiliationNum > 0 ? c(236, 232, 218) : c(120, 134, 150);
    }

    private static void beastFace(ArtGen.Painter p, Graphics2D g, Character ch, Random r) {
        Color fur = mix(c(120 + r.nextInt(60), 100 + r.nextInt(50), 80 + r.nextInt(40)), c(240, 240, 240), ch.lvl >= 60 ? 0.35 : 0);
        Color dark = mix(fur, c(20, 20, 20), 0.55);
        p.wash(poly(54, 90, 70, 20, 110, 80), fur, 4, 0.95, 0.04); p.wash(poly(202, 90, 186, 20, 146, 80), fur, 4, 0.95, 0.04); // ears
        p.washCircle(128, 150, 84, 76, fur, 6, 0.97, 0.04);
        p.wash(poly(96, 150, 128, 130, 160, 150, 150, 214, 128, 232, 106, 214), mix(fur, c(250, 244, 230), 0.5), 4, 0.95, 0.04); // muzzle
        p.washCircle(128, 176, 11, 8, dark, 3, 0.95, 0.05);                                                                          // nose
        Color eye = ch.trait == Character.Traits.Monster ? c(220, 50, 40) : c(236, 190, 50);
        p.washCircle(98, 124, 12, 8, eye, 3, 0.95, 0.04); p.washCircle(158, 124, 12, 8, eye, 3, 0.95, 0.04);
        p.stroke(98, 124, 98, 124, 4, dark, 0.95); p.stroke(158, 124, 158, 124, 4, dark, 0.95);
        p.stroke(84, 108, 110, 116, 3, dark, 0.8); p.stroke(172, 108, 146, 116, 3, dark, 0.8);                                      // angry brows
        p.stroke(128, 184, 128, 204, 2, dark, 0.7); p.stroke(110, 206, 128, 204, 2, dark, 0.7); p.stroke(146, 206, 128, 204, 2, dark, 0.7);
        for (int i = 0; i < 6; i++) p.stroke(70 + r.nextInt(120), 200 + r.nextInt(20), 74 + r.nextInt(120), 232, 1.4, dark, 0.4);       // fur strokes
        if (ch.lvl >= 40) p.stroke(128, 40, 128, 96, 4, c(220, 190, 80), 0.9);                                                      // a mark on the brow
    }

    // ------------------------------------------------------------------ sect seals
    /** A red seal for a sect: four glyphs picked from its name, in a rough-edged square. */
    static BufferedImage seal(String name, int size) {
        Random r = new Random(Naming.mix64(name.hashCode()));
        String pool = "天道玄剑宗云山月龙凤火雷水木金土明阴阳星河风霜梦影魂";
        BufferedImage im = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gfx(im);
        Color red = c(188, 40, 36);
        g.setColor(red);
        double pad = size * 0.04, rad = size * (0.10 + r.nextDouble() * 0.12);
        g.fill(new RoundRectangle2D.Double(pad, pad, size - 2 * pad, size - 2 * pad, rad, rad));
        Color paper = c(250, 236, 220);
        g.setColor(paper);
        g.setStroke(new BasicStroke((float) (size * 0.045)));
        g.draw(new RoundRectangle2D.Double(size * 0.12, size * 0.12, size * 0.76, size * 0.76, rad * 0.6, rad * 0.6));
        g.setFont(new Font(Theme.CJK, Font.BOLD, (int) (size * 0.34)));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < 4; i++) {
            String glyph = String.valueOf(pool.charAt(r.nextInt(pool.length())));
            int col = i / 2, row = i % 2; // read top to bottom, right column first
            double cx = size * (col == 0 ? 0.68 : 0.32), cy = size * (row == 0 ? 0.36 : 0.68);
            if (g.getFont().canDisplay(glyph.charAt(0))) g.drawString(glyph, (float) (cx - fm.stringWidth(glyph) / 2.0), (float) (cy + fm.getAscent() * 0.34));
            else g.fill(new Ellipse2D.Double(cx - size * 0.1, cy - size * 0.1, size * 0.2, size * 0.2)); // no CJK font: a dot per glyph
        }
        g.setComposite(AlphaComposite.Clear);
        for (int i = 0; i < size / 5; i++) { // worn ink: a few chips out of the edge
            double px = r.nextBoolean() ? r.nextDouble() * size * 0.1 : size * (0.9 + r.nextDouble() * 0.1), py = r.nextDouble() * size;
            g.fill(new Ellipse2D.Double(r.nextBoolean() ? px : py, r.nextBoolean() ? py : px, 1 + r.nextDouble() * size * 0.03, 1 + r.nextDouble() * size * 0.03));
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.dispose();
        return im;
    }

    // ------------------------------------------------------------------ item icons
    private static Color gradeColor(int lvl) {
        return lvl <= 2 ? c(150, 140, 126) : lvl <= 4 ? c(96, 160, 118) : lvl <= 6 ? c(196, 150, 70) : lvl <= 8 ? c(90, 140, 210) : c(236, 190, 70);
    }

    /** A small painted icon for an item (weapon type, robe, herb, ore, pill, array, food, wine, poison, instrument), tinted by its grade. */
    static BufferedImage itemIcon(Item it, int size) {
        BufferedImage im = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gfx(im);
        g.scale(size / 64.0, size / 64.0);
        Color col = gradeColor(it.lvl), dark = mix(col, c(30, 26, 24), 0.55), light = mix(col, c(255, 255, 255), 0.55);
        g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 40)); g.fill(new Ellipse2D.Double(2, 2, 60, 60)); // a soft disc
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (it.itemType == null) { g.dispose(); return im; }
        switch (it.itemType) {
            case Weapon: {
                String wt = it.weaponType == null ? "Sword" : it.weaponType.name();
                g.rotate(-Math.PI / 4, 32, 32);
                switch (wt) {
                    case "Spear": g.setColor(c(120, 84, 56)); g.drawLine(32, 6, 32, 58); g.setColor(col); g.fill(poly2(32, 2, 38, 18, 32, 22, 26, 18)); g.setColor(c(190, 60, 50)); g.fill(poly2(32, 22, 40, 30, 32, 28, 24, 30)); break;
                    case "Staff": g.setColor(c(120, 84, 56)); g.setStroke(new BasicStroke(3f)); g.drawLine(32, 14, 32, 60); g.setColor(col); g.setStroke(new BasicStroke(3f)); g.draw(new java.awt.geom.Arc2D.Double(22, 4, 20, 20, 210, 120, java.awt.geom.Arc2D.OPEN)); g.fill(new Ellipse2D.Double(29, 10, 6, 6)); break;
                    case "Saber": g.setColor(col); g.fill(poly2(30, 6, 36, 10, 38, 38, 32, 42, 30, 36)); g.setColor(dark); g.fillRect(26, 42, 12, 3); g.setColor(c(120, 84, 56)); g.fillRect(30, 45, 4, 14); break;
                    case "Fan": g.setColor(light); g.fill(new java.awt.geom.Arc2D.Double(8, 14, 48, 48, 30, 120, java.awt.geom.Arc2D.PIE)); g.setColor(dark); g.setStroke(new BasicStroke(1.3f)); for (int a = 0; a <= 4; a++) { double t = Math.toRadians(30 + a * 30); g.drawLine(32, 38, (int) (32 + 24 * Math.cos(t)), (int) (38 - 24 * Math.sin(t))); } break;
                    default: g.setColor(col); g.fill(poly2(32, 2, 36, 10, 36, 40, 28, 40, 28, 10)); g.setColor(light); g.drawLine(32, 6, 32, 38); g.setColor(dark); g.fillRect(22, 40, 20, 3); g.setColor(c(120, 84, 56)); g.fillRect(30, 43, 4, 14);
                }
                break;
            }
            case Clothing: g.setColor(col); g.fill(poly2(20, 8, 32, 14, 44, 8, 60, 22, 52, 34, 46, 28, 46, 58, 18, 58, 18, 28, 12, 34, 4, 22)); g.setColor(dark); g.drawLine(32, 14, 32, 58); g.setColor(light); g.drawLine(20, 8, 32, 30); g.drawLine(44, 8, 32, 30); break;
            case Herb: g.setColor(c(80, 130, 80)); g.drawLine(32, 58, 32, 20); g.setColor(col); g.fill(new Ellipse2D.Double(14, 22, 20, 10)); g.fill(new Ellipse2D.Double(32, 14, 20, 10)); g.fill(new Ellipse2D.Double(18, 38, 16, 8)); g.setColor(light); g.fill(new Ellipse2D.Double(28, 6, 8, 12)); break;
            case Metal: g.setColor(col); g.fill(poly2(10, 44, 20, 22, 38, 14, 54, 30, 50, 52, 24, 54)); g.setColor(light); g.fill(poly2(20, 22, 38, 14, 32, 30)); g.setColor(dark); g.drawLine(32, 30, 24, 54); g.drawLine(32, 30, 50, 52); break;
            case Pill: g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 90)); g.fill(new Ellipse2D.Double(8, 8, 48, 48)); g.setColor(col); g.fill(new Ellipse2D.Double(18, 18, 28, 28)); g.setColor(light); g.fill(new Ellipse2D.Double(24, 22, 8, 7)); break;
            case Array: g.setColor(col); g.draw(new Ellipse2D.Double(8, 8, 48, 48)); g.draw(new Ellipse2D.Double(20, 20, 24, 24)); g.setStroke(new BasicStroke(1.6f));
                for (int a = 0; a < 8; a++) { double t = Math.PI / 4 * a; for (int k = 0; k < 3; k++) { double rr = 26 + k * 2.2 - 1; g.drawLine((int) (32 + 22 * Math.cos(t)), (int) (32 + 22 * Math.sin(t)), (int) (32 + rr * Math.cos(t)), (int) (32 + rr * Math.sin(t))); } }
                g.setColor(dark); g.fill(new Ellipse2D.Double(28, 28, 8, 8)); break;
            case Blood: g.setColor(c(180, 40, 44)); g.fill(poly2(32, 6, 46, 34, 44, 48, 32, 58, 20, 48, 18, 34)); g.setColor(c(255, 190, 190)); g.fill(new Ellipse2D.Double(24, 34, 6, 10)); break;
            case Food: g.setColor(col); g.fill(new java.awt.geom.Arc2D.Double(10, 24, 44, 36, 180, 180, java.awt.geom.Arc2D.CHORD)); g.setColor(dark); g.drawLine(8, 42, 56, 42); g.setColor(new Color(255, 255, 255, 140)); g.setStroke(new BasicStroke(1.6f)); for (int k = 0; k < 3; k++) g.draw(new java.awt.geom.QuadCurve2D.Double(22 + k * 10, 36, 18 + k * 10, 24, 22 + k * 10, 12)); break;
            case Wine: g.setColor(c(150, 100, 60)); g.fill(new Ellipse2D.Double(14, 28, 36, 32)); g.fill(new Ellipse2D.Double(22, 14, 20, 22)); g.setColor(col); g.fillRect(28, 8, 8, 8); g.setColor(c(210, 60, 60)); g.drawLine(32, 16, 32, 26); g.setColor(light); g.fill(new Ellipse2D.Double(20, 36, 7, 10)); break;
            case Poison: g.setColor(new Color(90, 190, 90, 190)); g.fill(poly2(24, 28, 40, 28, 48, 56, 16, 56)); g.setColor(c(200, 200, 205)); g.fillRect(27, 10, 10, 20); g.setColor(c(120, 80, 60)); g.fillRect(25, 6, 14, 6); g.setColor(c(30, 70, 30)); g.fill(new Ellipse2D.Double(28, 40, 4, 4)); g.fill(new Ellipse2D.Double(34, 46, 3, 3)); break;
            case Instrument: g.setColor(c(120, 84, 56)); g.fill(poly2(6, 24, 58, 22, 58, 42, 6, 44)); g.setColor(col); g.setStroke(new BasicStroke(1.3f)); for (int k = 0; k < 5; k++) g.drawLine(8, 28 + k * 3, 56, 26 + k * 3); g.setColor(dark); g.fill(new Ellipse2D.Double(10, 48, 5, 5)); g.fill(new Ellipse2D.Double(44, 48, 5, 5)); break;
            default: g.setColor(col); g.fill(new Ellipse2D.Double(16, 16, 32, 32));
        }
        g.dispose();
        return im;
    }

    private static Path2D poly2(double... xy) {
        Path2D.Double s = new Path2D.Double();
        for (int i = 0; i + 1 < xy.length; i += 2) { if (i == 0) s.moveTo(xy[i], xy[i + 1]); else s.lineTo(xy[i], xy[i + 1]); }
        s.closePath();
        return s;
    }

    // ------------------------------------------------------------------ the painted world map
    /** An ink-and-wash map of the whole world from the terrain fields: seas, forests, mountains with ink peaks, rivers, the named regions and every known city. */
    static BufferedImage worldMap(int w, int h, java.util.Set<Area> known) {
        int n = Map.range;
        Random r = new Random(Naming.mix64(Map.worldSeed ^ 0x77AB));
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = gfx(im);
        g.setColor(c(238, 230, 210)); g.fillRect(0, 0, w, h);
        double ts = Math.min(w, h) / (double) n, ox = (w - ts * n) / 2, oy = (h - ts * n) / 2;
        g.translate(ox, oy);
        ArtGen.Painter p = new ArtGen.Painter(g, r, 1.0, ArtGen.palette("", r), "", null);
        BiomeGen.Cell[][] cell = new BiomeGen.Cell[n][n];
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) cell[x][y] = BiomeGen.cell(Map.worldSeed, x, y, n);
        // pass 1: broad washes, blobby so the coasts are not squares
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) {
            BiomeGen.Cell k = cell[x][y];
            double cx = (x + 0.5) * ts, cy = (n - 1 - y + 0.5) * ts;
            Color col = mapColor(k);
            p.washCircle(cx + r.nextGaussian() * ts * 0.06, cy + r.nextGaussian() * ts * 0.06, ts * 0.78, ts * 0.78, col, 3, 0.9, 0.12);
        }
        // pass 2: ink detail
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) {
            BiomeGen.Cell k = cell[x][y];
            double cx = (x + 0.5) * ts, cy = (n - 1 - y + 0.5) * ts;
            if (k.minor.equals("Ocean")) { if ((x * 7 + y * 3) % 5 == 0) { p.stroke(cx - ts * 0.25, cy, cx, cy - ts * 0.08, 1.0, c(70, 110, 140), 0.35); p.stroke(cx, cy - ts * 0.08, cx + ts * 0.25, cy, 1.0, c(70, 110, 140), 0.35); } continue; }
            if (k.elev >= 0.78) { for (int i = 0; i < 2; i++) { double px = cx + (i - 0.5) * ts * 0.4, ph = ts * (0.35 + 0.25 * r.nextDouble()); p.wash(poly(px - ts * 0.22, cy + ts * 0.2, px, cy + ts * 0.2 - ph * 1.4, px + ts * 0.22, cy + ts * 0.2), c(86, 96, 100), 3, 0.8, 0.04); p.stroke(px, cy + ts * 0.2 - ph * 1.4, px + ts * 0.1, cy + ts * 0.2, 1.1, c(30, 34, 36), 0.55); } }
            else if (k.minor.equals("Forest") || k.minor.equals("Taiga")) { for (int i = 0; i < 3; i++) { double px = cx + (r.nextDouble() - 0.5) * ts * 0.7, py = cy + (r.nextDouble() - 0.5) * ts * 0.6; p.washCircle(px, py, ts * 0.12, ts * 0.14, k.minor.equals("Taiga") ? c(40, 84, 76) : c(54, 100, 64), 2, 0.8, 0.1); } }
            else if (k.minor.equals("Desert")) { p.stroke(cx - ts * 0.3, cy + ts * 0.1, cx + ts * 0.3, cy + ts * 0.05, 1.0, c(190, 150, 90), 0.4); }
            if (k.river) { // a river winds from tile to tile
                for (int[] d : new int[][]{{1, 0}, {0, 1}, {1, 1}, {1, -1}}) {
                    int nx = x + d[0], ny = y + d[1];
                    if (nx < 0 || ny < 0 || nx >= n || ny >= n || !cell[nx][ny].river) continue;
                    double ex = (nx + 0.5) * ts, ey = (n - 1 - ny + 0.5) * ts;
                    p.stroke(cx, cy, (cx + ex) / 2 + r.nextGaussian() * ts * 0.1, (cy + ey) / 2 + r.nextGaussian() * ts * 0.1, ts * 0.12, c(90, 150, 190), 0.85);
                    p.stroke((cx + ex) / 2, (cy + ey) / 2, ex, ey, ts * 0.12, c(90, 150, 190), 0.85);
                }
            }
        }
        // names of the great regions, faint and large
        g.setFont(new Font(Theme.SERIF, Font.ITALIC, Math.max(10, (int) (ts * 0.62))));
        List<Object[]> labs = Regions.labels();
        labs.sort((x, y) -> Integer.compare((Integer) y[3], (Integer) x[3])); // the biggest regions get their names first
        List<java.awt.geom.Rectangle2D> taken = new java.util.ArrayList<>();
        for (Object[] lab : labs) {
            String name = (String) lab[0];
            double cx = ((Double) lab[1] + 0.5) * ts, cy = (n - 1 - (Double) lab[2] + 0.5) * ts;
            int size = (Integer) lab[3];
            if (size < 6) continue;
            FontMetrics fm = g.getFontMetrics();
            String s = name.startsWith("the ") ? name.substring(4) : name;
            java.awt.geom.Rectangle2D box = new java.awt.geom.Rectangle2D.Double(cx - fm.stringWidth(s) / 2.0 - 4, cy - fm.getAscent(), fm.stringWidth(s) + 8, fm.getHeight());
            boolean clash = false;
            for (java.awt.geom.Rectangle2D t : taken) if (t.intersects(box)) { clash = true; break; }
            if (clash) continue;
            taken.add(box);
            g.setColor(new Color(40, 40, 50, 140));
            g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) cy);
        }
        // cities and sects the player has seen
        Font small = new Font(Theme.SERIF, Font.BOLD, Math.max(9, (int) (ts * 0.42)));
        for (Area a : new java.util.ArrayList<>(Map.createdAreas)) {
            if (a == null || (!a.city && a.sect == null) || (known != null && !known.contains(a))) continue;
            double cx = (a.x + 0.5) * ts, cy = (n - 1 - a.y + 0.5) * ts;
            if (a.city) { p.pagoda(cx, cy + ts * 0.3, Math.max(0.3, ts / 40.0)); g.setFont(small); g.setColor(c(120, 30, 30)); g.drawString(a.name == null ? "" : a.name, (float) (cx + ts * 0.3), (float) (cy + ts * 0.5)); }
            else { g.setColor(c(190, 50, 44)); g.fill(new RoundRectangle2D.Double(cx - ts * 0.12, cy - ts * 0.12, ts * 0.24, ts * 0.24, 3, 3)); }
        }
        // a compass and a frame
        g.setFont(new Font(Theme.SERIF, Font.BOLD, Math.max(12, (int) (ts * 0.7))));
        g.setColor(c(60, 50, 40)); g.drawString("N", (float) (ts * 0.4), (float) (ts * 0.9));
        g.translate(-ox, -oy);
        g.setColor(c(110, 90, 70)); g.setStroke(new BasicStroke(4f)); g.drawRect(2, 2, w - 4, h - 4);
        g.dispose();
        return finish(im, r, false);
    }

    private static Color mapColor(BiomeGen.Cell k) {
        if (k.minor.equals("Ocean")) return mix(c(150, 190, 206), c(70, 120, 160), Math.max(0, Math.min(1, (BiomeGen.SEA - k.elev) / 0.22)));
        if (k.minor.equals("Beach")) return c(236, 222, 180);
        if (k.elev >= 0.78) return c(176, 182, 178);
        switch (k.minor) {
            case "Forest": return c(132, 168, 112);
            case "Taiga": return c(120, 154, 134);
            case "Grassland": return c(170, 196, 120);
            case "Frozen Grassland": return c(196, 214, 196);
            case "Savanna": return c(204, 190, 120);
            case "Desert": return c(228, 204, 148);
            case "Frozen Desert": return c(222, 226, 226);
            case "Tundra": return c(208, 216, 220);
            default: return c(170, 196, 120);
        }
    }

    // ------------------------------------------------------------------ illustrations of events
    /** An illustration for a chronicle entry: "tide", "war", "tournament", "festival", "trial", "city", "sect", "death", "legend", "reincarnation", ... */
    static BufferedImage eventArt(String kind, long seed, int w, int h) {
        Random r = new Random(Naming.mix64(seed + kind.hashCode()));
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = gfx(im);
        g.setColor(c(238, 231, 214)); g.fillRect(0, 0, w, h);
        g.scale(w / (double) ArtGen.DW, h / (double) ArtGen.DH);
        ArtGen.Painter p = new ArtGen.Painter(g, r, 1.0, ArtGen.palette("", r), "", null);
        p.sky(170);
        switch (kind) {
            case "tide": {
                p.washRect(-10, 0, ArtGen.DW + 20, 200, c(200, 70, 50), 6, 0.35, 0.1);
                p.moon(380, 60, 24, c(210, 60, 50));
                p.mountainLayers(150, 3, 90, false);
                for (int i = 0; i < 70; i++) beastShape(p, 20 + r.nextInt(480), 200 + r.nextInt(80), 0.7 + r.nextDouble() * 0.9, r);
                p.mist(220, 40, 0.3);
                break;
            }
            case "war": {
                p.mountainLayers(150, 2, 70, false);
                p.washRect(-10, 180, ArtGen.DW + 20, 120, mix(p.pal.ground, c(110, 90, 70), 0.5), 6, 0.9, 0.05);
                for (int side = 0; side < 2; side++) for (int i = 0; i < 9; i++) {
                    double x = side == 0 ? 40 + i * 22 + r.nextInt(8) : 300 + i * 22 + r.nextInt(8), y = 230 + r.nextInt(40);
                    p.stroke(x, y, x, y - 46, 1.6, c(70, 54, 40), 0.9);
                    p.wash(poly(x, y - 46, x + (side == 0 ? 20 : -20), y - 40, x, y - 32), side == 0 ? c(190, 50, 44) : c(54, 84, 150), 3, 0.95, 0.05);
                    if (i % 2 == 0) p.figure(x + 6, y + 6, 0.5);
                }
                for (int i = 0; i < 4; i++) { double x = 150 + r.nextInt(220); for (int k = 0; k < 6; k++) p.stroke(x + Math.sin(k) * 4, 190 - k * 12, x + Math.sin(k + 1) * 4, 190 - (k + 1) * 12, 4, c(70, 70, 76), 0.3 - k * 0.04); }
                p.embers(30);
                break;
            }
            case "tournament": {
                p.mountainLayers(140, 2, 60, false);
                p.washRect(-10, 190, ArtGen.DW + 20, 120, c(206, 190, 150), 6, 0.9, 0.05);
                g.setColor(new Color(200, 170, 120, 220)); g.fill(new Ellipse2D.Double(130, 214, 252, 60));
                g.setColor(new Color(110, 70, 50, 220)); g.setStroke(new BasicStroke(3f)); g.draw(new Ellipse2D.Double(130, 214, 252, 60));
                for (int i = 0; i < 120; i++) { double t = r.nextDouble() * Math.PI * 2, rr = 1.1 + r.nextDouble() * 0.5; p.washCircle(256 + Math.cos(t) * 126 * rr, 244 + Math.sin(t) * 30 * rr + 16, 4, 6, i % 3 == 0 ? c(190, 60, 50) : c(60 + r.nextInt(120), 80 + r.nextInt(80), 100 + r.nextInt(100)), 2, 0.9, 0.1); }
                p.figure(226, 250, 1.2); p.figure(286, 250, 1.2);
                for (double bx : new double[]{110, 410}) { p.stroke(bx, 260, bx, 170, 2.4, c(80, 60, 40), 0.9); p.wash(poly(bx, 172, bx + 36, 182, bx, 196), c(200, 60, 50), 3, 0.95, 0.05); }
                break;
            }
            case "festival": {
                p.mountainLayers(150, 2, 60, false);
                for (int i = 0; i < 9; i++) { double x = 20 + i * 56, hh = 8 + r.nextInt(10); p.washRect(x, 214 - hh, 40, hh + 40, c(236, 220, 196), 3, 0.9, 0.05); p.wash(poly(x - 8, 214 - hh + 2, x + 20, 214 - hh - 14, x + 48, 214 - hh + 2), c(70, 66, 64), 3, 0.95, 0.05); }
                p.pagoda(256, 214, 2.0);
                for (int row = 0; row < 3; row++) for (int i = 0; i < 18; i++) { double x = 10 + i * 29, y = 80 + row * 30 + Math.sin(i * 0.7 + row) * 10; p.washCircle(x, y, 6, 7, row == 1 ? c(250, 190, 70) : c(210, 56, 46), 3, 0.95, 0.08); p.stroke(x, y - 7, x, y - 12, 0.8, c(70, 50, 40), 0.8); }
                p.birds(300, 40, 4, 1.0);
                break;
            }
            case "trial": {
                p.mountainLayers(160, 3, 100, true);
                p.lightRays(256, -10, 5, 0.3);
                p.washRect(236, 0, 40, 260, c(255, 236, 170), 6, 0.35, 0.08);
                for (int k = 0; k < 4; k++) { double x = 190 + r.nextInt(140), y = 0; for (int s = 0; s < 8; s++) { double nx = x + r.nextGaussian() * 14; p.stroke(x, y, nx, y + 26, 2.2, c(250, 240, 150), 0.8); x = nx; y += 26; } }
                for (int i = 0; i < 3; i++) p.floatingIslet(60 + r.nextInt(380), 40 + r.nextInt(60), 36 + r.nextInt(40));
                break;
            }
            case "city": case "sect": {
                p.mountainLayers(150, 2, 70, false);
                if (kind.equals("city")) ArtCity.skyline(p, 2); else ArtCity.sectHall(p);
                break;
            }
            case "death": {
                p.moon(120, 60, 22, c(238, 238, 224));
                p.mountainLayers(150, 2, 60, false);
                p.washRect(-10, 200, ArtGen.DW + 20, 100, c(150, 150, 150), 5, 0.9, 0.05);
                for (int i = 0; i < 3; i++) p.deadTree(60 + r.nextInt(400), 250 + r.nextInt(20), 40 + r.nextInt(30));
                p.crane(300, 90, 1.2);
                break;
            }
            case "reincarnation": {
                p.lightRays(256, -10, 6, 0.3);
                p.washCircle(256, 160, 90, 90, c(255, 236, 190), 6, 0.45, 0.1);
                p.mountainLayers(190, 2, 60, false);
                for (int i = 0; i < 12; i++) { double t = i / 12.0 * Math.PI * 2; p.washCircle(256 + Math.cos(t) * 70, 150 + Math.sin(t) * 40, 6, 6, c(250, 200, 110), 3, 0.9, 0.1); }
                p.figure(256, 250, 1.3);
                break;
            }
            default: { // legend, breakthrough, world: a lone figure on a peak under a great moon
                p.moon(380, 70, 30, c(244, 238, 214));
                p.mountainLayers(160, 3, 110, r.nextBoolean());
                p.figure(140 + r.nextInt(120), 150, 0.9);
                p.crane(300 + r.nextInt(120), 80, 1.2); p.crane(330 + r.nextInt(120), 100, 0.9);
                p.mist(190, 40, 0.4);
            }
        }
        g.dispose();
        return finish(im, r, true);
    }

    private static void beastShape(ArtGen.Painter p, double x, double y, double s, Random r) {
        Color dark = c(34 + r.nextInt(24), 30, 34);
        p.wash(poly(x - 12 * s, y, x - 6 * s, y - 8 * s, x + 6 * s, y - 9 * s, x + 14 * s, y - 14 * s, x + 18 * s, y - 8 * s, x + 12 * s, y - 4 * s, x + 8 * s, y, x + 2 * s, y + 5 * s, x - 8 * s, y + 5 * s), dark, 2, 0.95, 0.05); // body and head
        p.stroke(x + 12 * s, y - 14 * s, x + 14 * s, y - 20 * s, 1.4 * s, dark, 0.9);                                                                                   // an ear
        p.stroke(x - 12 * s, y, x - 20 * s, y + 4 * s, 1.4 * s, dark, 0.9);                                                                                             // the tail
        p.stroke(x - 4 * s, y + 5 * s, x - 6 * s, y + 12 * s, 1.4 * s, dark, 0.9); p.stroke(x + 6 * s, y + 5 * s, x + 8 * s, y + 12 * s, 1.4 * s, dark, 0.9);
        p.washCircle(x + 14 * s, y - 10 * s, 1.5 * s, 1.5 * s, c(240, 190, 60), 1, 0.95, 0.02);
    }

    // ------------------------------------------------------------------ weather
    static final String[] WEATHER_NAMES = {"clear", "rain", "fog"};

    /** The weather of a tile this month: the same for a stretch of weeks and neighbouring tiles, never saved (a function of the world seed, the date and the place). */
    static int weatherAt(Area a) {
        if (a == null) return 0;
        long month = GameDriver.time / 4;
        long cell = Math.floorDiv(a.x, 3) * 1000L + Math.floorDiv(a.y, 3);
        int roll = (int) Math.floorMod(Naming.mix64(Map.worldSeed ^ (month * 7919 + cell * 104729)), 100L);
        double wet = a.moist;
        if ("Desert".equals(a.biomem) && roll < 90) return 0;
        if (roll < 8 + (int) (wet * 12)) return 1;       // rain, more often where it is wet
        if (roll >= 92 - (int) (a.elev * 8)) return 2;   // fog, more often on high ground
        return 0;
    }
}
