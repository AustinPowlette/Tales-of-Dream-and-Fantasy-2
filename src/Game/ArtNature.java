package Game;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static Game.ArtGen.DH;
import static Game.ArtGen.DW;
import static Game.ArtGen.alpha;
import static Game.ArtGen.c;
import static Game.ArtGen.mix;
import static Game.ArtGen.poly;

/** The five pictures that were the weakest of `ArtGen` (2026-10-21), painted with the same watercolour primitives but composed like a landscape painter would: waterfalls (tiered falls into a pool between two walls, a river
 *  that steps down in white ledges, a gorge that closes in with the strata running to a vanishing point), volcanoes (a cone with gullies and lava, an ash cloud lit from below, lightning), natural arches (a heavy span with
 *  strata and the landscape seen through it), the sea (wave rows that shrink towards the horizon with a glitter path under the sun) and deserts (dunes with a bright windward face and a dark lee face, ripples, a caravan,
 *  salt crust, half-buried columns). Nothing here changes the game: `ArtGen.scene` calls these for its "falls", "volcano", "arch", "sea" and "desert" scenes. */
final class ArtNature {
    private ArtNature() { }

    // ------------------------------------------------------------------ small tools
    private static double smooth(double t) { t = Math.max(0, Math.min(1, t)); return t * t * (3 - 2 * t); }

    private static void gradient(ArtGen.Painter p, Shape s, double y0, Color a, double y1, Color b) { p.g.setPaint(new GradientPaint(0, (float) y0, a, 0, (float) y1, b)); p.g.fill(s); }

    private static void sideGradient(ArtGen.Painter p, Shape s, double x0, Color a, double x1, Color b) { p.g.setPaint(new GradientPaint((float) x0, 0, a, (float) x1, 0, b)); p.g.fill(s); }

    /** A soft patch of haze (an ellipse that fades to nothing at its edge). */
    static void haze(ArtGen.Painter p, double x, double y, double rx, double ry, Color col, double a) {
        java.awt.geom.AffineTransform old = p.g.getTransform();
        p.g.translate(x, y); p.g.scale(1, ry / rx);
        p.g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), (float) rx, new float[]{0f, 0.6f, 1f}, new Color[]{alpha(col, a), alpha(col, a * 0.45), alpha(col, 0)}));
        p.g.fill(new Ellipse2D.Double(-rx, -rx, 2 * rx, 2 * rx));
        p.g.setTransform(old);
    }

    /** Bands of rock across a shape (horizontal layers of two colours, each a little tilted). */
    static void strata(ArtGen.Painter p, Shape shape, Color a, Color b, double y0, double y1, double minH, double maxH, double al) {
        Shape old = p.g.getClip();
        p.g.setClip(shape);
        double y = y0;
        while (y < y1) {
            double h = minH + p.r.nextDouble() * (maxH - minH), tilt = (p.r.nextDouble() - 0.5) * 8;
            List<double[]> band = new ArrayList<>();
            for (int x = -10; x <= DW + 10; x += 32) band.add(new double[]{x, y + tilt * (x / (double) DW) + p.r.nextGaussian() * 0.8});
            for (int x = DW + 10; x >= -10; x -= 32) band.add(new double[]{x, y + h + tilt * (x / (double) DW) + p.r.nextGaussian() * 0.8});
            p.g.setColor(alpha(mix(a, b, p.r.nextDouble()), al * (0.5 + p.r.nextDouble() * 0.8)));
            p.g.fill(p.path(band));
            y += h;
        }
        p.g.setClip(old);
    }

    /** Foam and spray where water lands: a heap of white washes, a plume going up and a few drops. */
    static void splash(ArtGen.Painter p, double cx, double y, double w, double plume) {
        Random r = p.r;
        for (int i = 0; i < 9; i++) p.washCircle(cx + (r.nextDouble() - 0.5) * w * 1.1, y + r.nextInt(6) - 2, w * (0.18 + r.nextDouble() * 0.2), 5 + r.nextInt(6), c(255, 255, 255), 3, 0.75, 0.2);
        for (int i = 0; i < 8; i++) p.washCircle(cx + r.nextGaussian() * w * 0.2, y - 8 - i * plume / 8, w * (0.3 + i * 0.05), plume / 6, c(248, 250, 252), 3, 0.34 - i * 0.03, 0.2);
        for (int i = 0; i < 14; i++) { double dx = r.nextGaussian() * w * 0.4, dy = -r.nextDouble() * plume * 0.6; p.g.setColor(alpha(c(255, 255, 255), 0.5 + r.nextDouble() * 0.4)); p.g.fill(new Ellipse2D.Double(cx + dx, y + dy - 6, 1.6 + r.nextDouble() * 1.8, 1.6 + r.nextDouble() * 1.8)); }
    }

    /** One sheet of falling water: a curved lip, many strands, a fan at the foot. */
    static void fallSheet(ArtGen.Painter p, double cx, double w, double top, double bot, boolean ice) {
        Random r = p.r;
        Color a = ice ? c(236, 246, 252) : c(250, 253, 255), b = ice ? c(170, 206, 226) : c(196, 222, 232);
        Path2D.Double body = new Path2D.Double();
        body.moveTo(cx - w / 2, top);
        body.curveTo(cx - w / 2 - 3, top + 5, cx - w * 0.58, top + 12, cx - w * 0.56, top + 22);
        body.lineTo(cx - w * 0.68, bot); body.lineTo(cx + w * 0.68, bot); body.lineTo(cx + w * 0.56, top + 22);
        body.curveTo(cx + w * 0.58, top + 12, cx + w / 2 + 3, top + 5, cx + w / 2, top);
        body.closePath();
        gradient(p, body, top, a, bot, b);
        Shape old = p.g.getClip(); p.g.setClip(body);
        int strands = (int) (10 + w / 3.2 * (0.5 + p.q * 0.5));
        for (int i = 0; i < strands; i++) {
            double t = r.nextDouble(), x0 = cx - w * 0.48 + t * w * 0.96, x1 = cx + (x0 - cx) * 1.28 + r.nextGaussian() * 1.2;
            p.stroke(x0, top + 6 + r.nextInt(14), x1, bot - r.nextInt(10), 1.6 + r.nextDouble() * 2.2, i % 3 == 0 ? mix(p.pal.water, c(120, 170, 200), 0.5) : c(255, 255, 255), 0.45 + r.nextDouble() * 0.3);
        }
        for (int k = 0; k < 4; k++) { double x = cx - w * 0.5 + r.nextDouble() * w, y = top + 20 + r.nextDouble() * (bot - top - 30); p.stroke(x - 3, y, x + 3, y + 18, 2, c(255, 255, 255), 0.7); }   // breaks of white along the fall
        p.stroke(cx - w * 0.52, top + 4, cx - w * 0.66, bot, 4, alpha(mix(p.pal.waterDeep, c(40, 70, 90), 0.4), 1), 0.22);   // the shaded edges
        p.stroke(cx + w * 0.52, top + 4, cx + w * 0.66, bot, 4, alpha(mix(p.pal.waterDeep, c(40, 70, 90), 0.4), 1), 0.28);
        p.g.setClip(old);
        if (ice) for (int i = 0; i < 9; i++) { double x = cx - w * 0.5 + i * w / 8; p.stroke(x, top + 4, x + r.nextGaussian(), top + 16 + r.nextInt(14), 2.4, c(220, 240, 250), 0.9); }
    }

    // ------------------------------------------------------------------ falls
    static void falls(ArtGen.Painter p, String noun) {
        boolean ice = noun.startsWith("frozen") || noun.equals("ice_falls");
        if (noun.equals("rapids")) rapids(p);
        else if (noun.equals("waterfalls") || ice) tieredFall(p, ice);
        else gorge(p, noun);
    }

    private static void tieredFall(ArtGen.Painter p, boolean ice) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        double cx = 236 + r.nextInt(40), ledge = 150 + r.nextInt(20), top = 62 + r.nextInt(10);
        Color wallLo = ice ? c(158, 190, 206) : mix(pal.rock, pal.rockDark, 0.12), wallHi = ice ? c(206, 228, 238) : mix(pal.rock, c(214, 206, 188), 0.25);
        p.sky(top + 6);
        p.sun(60 + r.nextInt(80), 40, 18);
        p.mountainLayers(76, 2, 36, ice);
        // the back wall the water leaves: strata, a lip of green at the top
        List<double[]> back = poly(cx - 120, 300, cx - 122, top + 6, cx - 70, top - 2, cx - 24, top, cx + 24, top, cx + 80, top - 4, cx + 124, top + 8, cx + 122, 300);
        p.rock(back, mix(wallHi, wallLo, 0.35));
        strata(p, p.path(back), wallHi, wallLo, top, 290, 5, 16, 0.3);
        for (int i = 0; i < 7; i++) p.pine(cx - 112 + r.nextInt(224), top + 4 + r.nextInt(3), 14 + r.nextInt(8), ice ? c(120, 150, 160) : pal.leafDark);
        // the river above the lip: a pale band narrowing into the distance
        p.g.setPaint(new GradientPaint(0, (float) (top - 10), alpha(mix(pal.water, c(255, 255, 255), 0.4), 0), 0, (float) top, alpha(mix(pal.water, c(255, 255, 255), 0.4), 0.9)));
        p.g.fill(new Rectangle2D.Double(cx - 34, top - 10, 68, 12));
        // two tiers and the ledge between
        fallSheet(p, cx, 44, top, ledge, ice);
        List<double[]> ledgeRock = poly(cx - 96, ledge + 4, cx - 60, ledge - 6, cx - 30, ledge + 2, cx + 30, ledge + 3, cx + 62, ledge - 5, cx + 98, ledge + 6, cx + 80, ledge + 22, cx - 78, ledge + 22);
        p.rock(ledgeRock, mix(wallLo, pal.rockDark, 0.3));
        splash(p, cx, ledge + 2, 62, 22);
        fallSheet(p, cx, 74, ledge + 14, 250, ice);
        // the two near walls: dark, with strata, moss, trees along the top
        List<double[]> left = poly(-8, 300, -8, 18, 40, 10, 96, 30, 150, 62, 140, 120, 156, 176, 130, 230, 150, 300);
        List<double[]> right = poly(DW + 8, 300, DW + 8, 8, 470, 22, 416, 40, 366, 80, 380, 140, 360, 190, 388, 240, 372, 300);
        p.rock(left, wallLo); p.rock(right, mix(wallLo, pal.rockDark, 0.2));
        strata(p, p.path(left), wallHi, pal.rockDark, 10, 296, 6, 18, 0.28); strata(p, p.path(right), wallHi, pal.rockDark, 10, 296, 6, 18, 0.28);
        sideGradient(p, p.path(left), 0, alpha(c(20, 26, 30), 0.0), 150, alpha(c(20, 26, 30), 0.22)); sideGradient(p, p.path(right), 370, alpha(c(20, 26, 30), 0.18), DW, alpha(c(20, 26, 30), 0.0));
        if (!ice) for (int i = 0; i < 16; i++) { boolean lw = r.nextBoolean(); double x = lw ? 10 + r.nextInt(130) : 380 + r.nextInt(120), y = 50 + r.nextInt(220); p.stroke(x, y, x + r.nextGaussian() * 3, y + 14 + r.nextInt(30), 3, mix(pal.leafDark, pal.leafLight, r.nextDouble()), 0.34); }
        for (int i = 0; i < 6; i++) { p.pine(10 + r.nextInt(120), 24 + r.nextInt(30), 30 + r.nextInt(10), ice ? c(110, 140, 150) : pal.leafDark); p.pine(390 + r.nextInt(110), 24 + r.nextInt(26), 30 + r.nextInt(10), ice ? c(110, 140, 150) : pal.leafDark); }
        // the pool, the foam and the plume
        List<double[]> pool = poly(cx - 150, 252, cx - 60, 246, cx, 250, cx + 80, 246, cx + 160, 254, cx + 190, 300, cx - 190, 300);
        gradient(p, p.path(pool), 246, mix(pal.water, c(220, 238, 244), 0.5), 300, pal.waterDeep);
        for (int i = 0; i < 18; i++) { double y = 256 + i * 2.6, len = 14 + i * 3, x = cx - 150 + r.nextDouble() * 280; p.stroke(x, y, x + len, y + r.nextGaussian(), 1.6, c(255, 255, 255), 0.3); }
        splash(p, cx, 252, 110, 46);
        for (int i = 0; i < 6; i++) p.washCircle(cx - 140 + r.nextInt(280), 272 + r.nextInt(22), 14 + r.nextInt(18), 7 + r.nextInt(6), alpha(c(250, 252, 255), 1), 3, 0.28, 0.2);
        // rocks in the pool and at the feet of the walls
        for (int i = 0; i < 5; i++) { double x = 60 + r.nextInt(400), y = 268 + r.nextInt(24), w = 16 + r.nextInt(26); p.rock(poly(x - w, y + 4, x - w * 0.6, y - w * 0.5, x + w * 0.2, y - w * 0.7, x + w, y - 2, x + w * 0.6, y + 8), mix(pal.rockDark, c(70, 76, 76), 0.4)); }
        if (!ice && r.nextInt(3) == 0) { // a rainbow in the spray
            Color[] bow = {c(232, 100, 90), c(244, 176, 90), c(244, 232, 120), c(120, 200, 130), c(110, 160, 230), c(150, 120, 220)};
            for (int k = 0; k < bow.length; k++) { p.g.setStroke(new BasicStroke(2.6f)); p.g.setColor(alpha(bow[k], 0.22)); p.g.draw(new Arc2D.Double(cx - 70 - k * 2.6, 190 - 70 - k * 2.6 + 24, 140 + k * 5.2, 140 + k * 5.2, 20, 140, Arc2D.OPEN)); }
        }
        p.mist(238, 24, 0.2);
    }

    private static void rapids(ArtGen.Painter p) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        double hz = 128, vx = 240 + r.nextInt(40);
        p.sky(hz + 6);
        p.sun(300 + r.nextInt(120), 50, 22);
        p.mountainLayers(hz - 20, 3, 56, false);
        // the banks: slopes that fall towards the viewer; the river between them widens
        double[] leftBank = new double[DH + 1], rightBank = new double[DH + 1];
        for (int y = (int) hz; y <= DH; y++) { double s = (y - hz) / (DH - hz); double w = 22 + 300 * Math.pow(s, 1.25); leftBank[y] = vx - w + Math.sin(y * 0.07) * 6; rightBank[y] = vx + w + Math.cos(y * 0.06) * 6; }
        List<double[]> lb = new ArrayList<>(), rb = new ArrayList<>(), water = new ArrayList<>();
        lb.add(new double[]{-8, hz - 6}); for (int y = (int) hz; y <= DH; y += 6) lb.add(new double[]{leftBank[y], y}); lb.add(new double[]{-8, DH + 8});
        rb.add(new double[]{DW + 8, hz - 6}); for (int y = (int) hz; y <= DH; y += 6) rb.add(new double[]{rightBank[y], y}); rb.add(new double[]{DW + 8, DH + 8});
        for (int y = (int) hz; y <= DH; y += 6) water.add(new double[]{leftBank[y], y}); for (int y = DH; y >= (int) hz; y -= 6) water.add(new double[]{rightBank[Math.min(DH, y)], y});
        p.wash(lb, mix(pal.near, pal.ground, 0.4), 10, 0.95, 0.03); p.wash(rb, mix(pal.near, pal.ground, 0.4), 10, 0.95, 0.03);
        gradient(p, p.path(water), hz, mix(pal.water, pal.skyBot, 0.5), DH, mix(pal.waterDeep, pal.water, 0.3));
        // trees on both banks, small in the distance
        for (int i = 0; i < 26; i++) { double y = hz + 4 + Math.pow(r.nextDouble(), 1.4) * (DH - hz - 14), s = 0.35 + (y - hz) / (DH - hz) * 1.1; boolean lw = r.nextBoolean(); double x = lw ? leftBank[(int) y] - 10 - r.nextInt(80) * s : rightBank[(int) y] + 10 + r.nextInt(80) * s; if (x < -10 || x > DW + 10) continue; if (r.nextInt(3) == 0) p.broad(x, y, 34 * s, pal.leafDark); else p.pine(x, y, 36 * s, pal.leafDark); }
        // ledges: each a dark lip, a white fan of foam and streaks of water falling down it
        int steps = 5;
        for (int k = 0; k < steps; k++) {
            double s = Math.pow((k + 0.6) / steps, 1.6), y = hz + 8 + s * (DH - hz - 24), h = 4 + 16 * s;
            double xl = leftBank[(int) y] + 4, xr = rightBank[(int) y] - 4;
            List<double[]> lip = new ArrayList<>();
            lip.add(new double[]{xl, y}); for (double x = xl; x <= xr; x += 14) lip.add(new double[]{x, y + Math.sin(x * 0.05 + k) * 2 + r.nextGaussian()}); lip.add(new double[]{xr, y}); lip.add(new double[]{xr, y + h}); lip.add(new double[]{xl, y + h});
            p.wash(lip, mix(pal.waterDeep, c(24, 40, 56), 0.5), 6, 0.55, 0.04);
            for (int i = 0; i < (int) ((xr - xl) / 7); i++) { double x = xl + r.nextDouble() * (xr - xl); p.stroke(x, y + 1, x + r.nextGaussian() * 2, y + h * (0.35 + r.nextDouble() * 0.5), 1.8 + 2.4 * s, c(255, 255, 255), 0.45 + r.nextDouble() * 0.3); }
            for (int i = 0; i < 3 + (int) (7 * s); i++) p.washCircle(xl + r.nextDouble() * (xr - xl), y + h + 1, 12 + 30 * s * r.nextDouble() + 6, 3 + 6 * s, c(255, 255, 255), 3, 0.75, 0.2);
            for (int i = 0; i < 1 + (int) (3 * s); i++) { double x = xl + (xr - xl) * (0.1 + 0.8 * r.nextDouble()), w = (7 + 20 * s) * (0.6 + r.nextDouble() * 0.7); p.rock(poly(x - w, y + h + 4, x - w * 0.6, y + h - w * 0.6, x + w * 0.2, y + h - w * 0.75, x + w, y + h + 2, x + w * 0.6, y + h + 8), mix(pal.rockDark, c(84, 90, 90), 0.4)); p.washCircle(x, y + h + 6, w * 1.3, w * 0.3, c(255, 255, 255), 3, 0.7, 0.2); }
        }
        haze(p, vx, hz + 8, 200, 30, c(250, 246, 236), 0.5);
        for (int i = 0; i < 5; i++) { double x = r.nextInt(DW), y = 266 + r.nextInt(18), w = 14 + r.nextInt(22); p.rock(poly(x - w, y + 6, x - w * 0.6, y - w * 0.4, x + w * 0.2, y - w * 0.6, x + w, y, x + w * 0.5, y + 10), mix(pal.rockDark, c(70, 76, 76), 0.4)); }
    }

    private static void gorge(ArtGen.Painter p, String noun) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        boolean slot = noun.equals("slot_canyons"), warm = noun.equals("canyons") || noun.equals("gulches") || slot, icy = noun.equals("crevasses");
        double vx = 250 + r.nextInt(24), vy = 142 + r.nextInt(14), slotW = slot ? 40 : 86;
        Color wallA = icy ? c(176, 214, 232) : warm ? c(214, 128, 84) : mix(pal.rock, c(190, 168, 130), 0.45), wallB = icy ? c(96, 140, 180) : warm ? c(120, 60, 54) : mix(pal.rockDark, c(74, 62, 60), 0.5);
        Color sky0 = icy ? c(196, 224, 240) : pal.skyTop, sky1 = icy ? c(240, 248, 252) : pal.skyBot;
        gradient(p, new Rectangle2D.Double(-4, -4, DW + 8, DH + 8), 0, sky0, vy + 30, sky1);
        // the far end: a pale hill, haze, a spill of light
        p.mountainLayers(vy + 6, 2, 30, icy);
        p.washCircle(vx, vy + 4, 80, 26, alpha(c(255, 244, 214), 1), 6, 0.55, 0.15);
        // the two walls (trapezoids that meet at the vanishing point)
        List<double[]> lw = poly(-8, -8, vx - slotW, -8, vx - 22, vy - 8, vx - 20, vy + 18, -8, 296);
        List<double[]> rw = poly(DW + 8, -8, vx + slotW, -8, vx + 22, vy - 8, vx + 20, vy + 18, DW + 8, 296);
        p.wash(lw, mix(wallA, wallB, 0.45), 12, 0.97, 0.025); p.wash(rw, mix(wallA, wallB, 0.62), 12, 0.97, 0.025);
        for (int side = 0; side < 2; side++) {
            Shape wall = p.path(side == 0 ? lw : rw);
            Shape old = p.g.getClip(); p.g.setClip(wall);
            double ex = side == 0 ? -8 : DW + 8;
            for (int k = 0; k < 34; k++) { // strata as thin wedges to the vanishing point
                double y1 = -20 + k * 11 + r.nextDouble() * 8, th = 3 + r.nextDouble() * 9;
                List<double[]> wedge = poly(ex, y1, ex, y1 + th, vx, vy + 6 + r.nextGaussian() * 1.5);
                p.g.setColor(alpha(mix(wallA, wallB, r.nextDouble()), 0.32 + r.nextDouble() * 0.4)); p.g.fill(p.path(wedge));
            }
            for (int k = 0; k < 20; k++) { double y1 = r.nextInt(300); p.stroke(ex + (side == 0 ? 1 : -1) * r.nextInt(70), y1, vx + (side == 0 ? -1 : 1) * 30, vy + r.nextGaussian() * 8, 2.4, mix(wallB, c(20, 16, 20), 0.4), 0.16); } // cracks
            gradient(p, wall, 0, alpha(c(20, 16, 24), 0.3), 300, alpha(c(20, 16, 24), 0.0));
            p.g.setClip(old);
        }
        sideGradient(p, p.path(lw), 120, alpha(c(255, 238, 190), 0), vx - 20, alpha(c(255, 238, 190), 0.18)); // the light that comes down the canyon
        // the river
        List<double[]> river = poly(vx - 20, vy + 18, vx + 20, vy + 18, DW + 8, 296, DW + 8, DH + 8, -8, DH + 8, -8, 296);
        gradient(p, p.path(river), vy + 18, mix(pal.water, c(255, 246, 220), 0.55), DH, mix(pal.waterDeep, pal.water, 0.2));
        for (int i = 0; i < 46; i++) { double s = Math.pow(r.nextDouble(), 1.3), y = vy + 22 + s * (DH - vy - 26), spread = 20 + (y - vy) * 2.1, x = vx + (r.nextDouble() - 0.5) * spread * 1.8, len = 6 + s * 44; p.stroke(x, y, x + len, y + r.nextGaussian(), 1.3 + s * 2.2, r.nextInt(3) == 0 ? alpha(pal.waterDeep, 1) : c(255, 255, 255), 0.25 + s * 0.2); }
        // gravel banks and boulders
        for (int side = 0; side < 2; side++) for (int i = 0; i < 7; i++) {
            double s = Math.pow(r.nextDouble(), 1.2), y = vy + 24 + s * (DH - vy - 36), x = side == 0 ? vx - 20 - (y - vy) * 1.55 + r.nextInt(18) : vx + 20 + (y - vy) * 1.55 - r.nextInt(18), w = 8 + 28 * s;
            p.rock(poly(x - w, y + 5, x - w * 0.6, y - w * 0.5, x + w * 0.2, y - w * 0.7, x + w, y - 2, x + w * 0.6, y + 8), mix(wallB, c(70, 64, 62), 0.4));
        }
        // trees on the rims and a ribbon of water falling from a ledge
        if (!warm && !icy) for (int i = 0; i < 9; i++) { double y = 8 + r.nextInt(40); p.pine(vx - slotW - 20 - r.nextInt(90) + (y * 0.4), y, 26, pal.leafDark); p.pine(vx + slotW + 20 + r.nextInt(90) - (y * 0.4), y, 26, pal.leafDark); }
        // a small bridge or a hermit for scale
        if (r.nextInt(2) == 0) p.figure(vx + r.nextInt(40) - 20, DH - 20, 1.4);
        haze(p, vx, vy + 20, 150, 40, c(250, 244, 232), 0.5);
    }

    // ------------------------------------------------------------------ volcano
    static void volcano(ArtGen.Painter p, String noun) {
        Random r = p.r; Graphics2D g = p.g;
        boolean caldera = noun.equals("calderas");
        double cx = 214 + r.nextInt(84), top = caldera ? 150 : 98 + r.nextInt(18), baseY = 262, halfW = caldera ? 340 : 236, ch = caldera ? 70 : 17;
        Color glow = c(246, 118, 54), lavaHot = c(255, 206, 90);
        p.sky(176);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, top), 280f, new float[]{0f, 1f}, new Color[]{alpha(glow, 0.42), alpha(glow, 0)}));
        g.fill(new Rectangle2D.Double(0, 0, DW, DH));
        p.sun(70 + r.nextInt(50), 58, 18);
        p.mountainLayers(188, 2, 44, false);
        // the cone: steep at the top and flaring at the foot, notched at the crater
        double[] prof = new double[DW + 1];
        for (int x = 0; x <= DW; x++) {
            double dx = x - cx, d = Math.abs(dx) / halfW, y = d >= 1 ? baseY + 8 : baseY - (baseY - top) * Math.pow(1 - d, caldera ? 1.7 : 2.0);
            if (Math.abs(dx) < ch) y = top + (caldera ? 6 : 11) * (1 - Math.pow(Math.abs(dx) / ch, 2));
            prof[x] = y + Math.sin(x * 0.41) * 1.2 + Math.sin(x * 0.13 + 1) * 2.2;
        }
        List<double[]> cone = new ArrayList<>();
        cone.add(new double[]{-8, 300});
        for (int x = -8; x <= DW + 8; x += 4) cone.add(new double[]{x, prof[Math.max(0, Math.min(DW, x))]});
        cone.add(new double[]{DW + 8, 300});
        p.wash(cone, c(82, 70, 74), 14, 0.97, 0.02);
        Shape coneShape = p.path(cone);
        Shape old = g.getClip(); g.setClip(coneShape);
        sideGradient(p, coneShape, cx - halfW * 0.7, alpha(c(210, 190, 170), 0.22), cx + halfW * 0.7, alpha(c(20, 14, 20), 0.4)); // light from the left, the right flank in shade
        g.setPaint(new GradientPaint(0, (float) top, alpha(glow, 0.5), 0, (float) (top + 70), alpha(glow, 0))); g.fill(new Rectangle2D.Double(0, top, DW, 90)); // the crater lights the top
        for (int k = 0; k < 26; k++) { // gullies running down the flanks
            double sgn = r.nextBoolean() ? 1 : -1, x0 = cx + sgn * r.nextDouble() * ch, x1 = cx + sgn * (halfW * (0.1 + r.nextDouble() * 0.7)), y1 = baseY - r.nextInt(40);
            double px = x0, py = top + 12;
            for (int s = 1; s <= 8; s++) { double t = s / 8.0, nx = x0 + (x1 - x0) * Math.pow(t, 1.5) + r.nextGaussian() * 2, ny = top + 12 + (y1 - top - 12) * t; if (ny > prof[(int) Math.max(0, Math.min(DW, nx))] + 3) p.stroke(px, py, nx, ny, 1.6 + t * 2.4, c(26, 20, 26), 0.28); px = nx; py = ny; }
        }
        for (int k = 0; k < 3 + r.nextInt(2); k++) { // rivers of lava
            double sgn = k % 2 == 0 ? -1 : 1, x0 = cx + sgn * (2 + r.nextDouble() * ch * 0.6), len = baseY - top - 40 - r.nextInt(40), drift = sgn * (20 + r.nextInt(70));
            double px = x0, py = top + 8;
            for (int s = 1; s <= 14; s++) {
                double t = s / 14.0, nx = x0 + drift * t * t + Math.sin(t * 9 + k) * 3, ny = top + 8 + len * t;
                if (ny > prof[(int) Math.max(0, Math.min(DW, nx))] + 2) { p.stroke(px, py, nx, ny, 6 + 4 * t, glow, 0.2); p.stroke(px, py, nx, ny, 2.4 + 2 * t, lavaHot, 0.9 - t * 0.35); }
                px = nx; py = ny;
            }
        }
        g.setClip(old);
        p.washCircle(cx, top + 5, ch + 8, 6, glow, 6, 0.9, 0.1); p.washCircle(cx, top + 6, ch * 0.7, 3, lavaHot, 5, 0.95, 0.1);   // the glow in the crater
        // the cloud: a stalk of ash that opens into an anvil, lit orange from below
        double wind = r.nextBoolean() ? 1 : -1;
        int puffs = 9;
        for (int i = 0; i < puffs; i++) {
            double t = i / (double) (puffs - 1), y = top - 4 - t * (top - 22), x = cx + wind * t * t * 54 + r.nextGaussian() * 4, rad = (caldera ? 24 : 12) + t * 34;
            p.washCircle(x, y, rad, rad * 0.8, mix(c(66, 58, 66), c(176, 168, 172), t * 0.6), 4, 0.92, 0.18);
            if (i < 4) p.washCircle(x, y + rad * 0.3, rad * 0.8, rad * 0.5, glow, 3, 0.3 - i * 0.05, 0.2);
        }
        p.washCircle(cx + wind * 54, 20, 150, 22, c(88, 80, 90), 7, 0.9, 0.15);
        p.washCircle(cx + wind * 54, 30, 110, 12, glow, 3, 0.2, 0.2);
        for (int i = 0; i < 24; i++) { double x = cx + wind * 54 + r.nextGaussian() * 100, y = 34 + r.nextInt(40); p.stroke(x, y, x + wind * 12, y + 40 + r.nextInt(60), 2, c(70, 62, 70), 0.12); } // ash falling
        if (r.nextInt(4) != 0) for (int k = 0; k < 2; k++) { // lightning in the cloud
            double x = cx + wind * 40 + r.nextGaussian() * 40, y = 16 + r.nextInt(20);
            for (int s = 0; s < 5; s++) { double nx = x + r.nextGaussian() * 9, ny = y + 11 + r.nextInt(8); p.stroke(x, y, nx, ny, 1.8, c(255, 250, 200), 0.95); x = nx; y = ny; }
        }
        // the foreground: black ground cut by glowing cracks, boulders, dead trees, embers
        List<double[]> ground = new ArrayList<>();
        ground.add(new double[]{-8, 300}); for (int x = -8; x <= DW + 8; x += 22) ground.add(new double[]{x, 252 + Math.sin(x * 0.03 + 1) * 6 + r.nextInt(6)}); ground.add(new double[]{DW + 8, 300});
        p.wash(ground, c(38, 32, 36), 10, 0.97, 0.03);
        for (int k = 0; k < 7; k++) {
            double x = r.nextInt(DW), y = 262 + r.nextInt(30);
            for (int s = 0; s < 6; s++) { double nx = x + (r.nextDouble() - 0.4) * 40, ny = y + r.nextGaussian() * 6; p.stroke(x, y, nx, ny, 5, glow, 0.2); p.stroke(x, y, nx, ny, 1.8, lavaHot, 0.85); x = nx; y = ny; }
        }
        for (int i = 0; i < 6; i++) { double x = r.nextInt(DW), y = 266 + r.nextInt(26), w = 12 + r.nextInt(26); p.rock(poly(x - w, y + 6, x - w * 0.6, y - w * 0.5, x + w * 0.2, y - w * 0.7, x + w, y - 2, x + w * 0.6, y + 8), c(36, 30, 36)); p.stroke(x - w * 0.6, y - w * 0.4, x + w * 0.2, y - w * 0.65, 2, glow, 0.5); }
        for (int i = 0; i < 3; i++) p.deadTree(20 + r.nextInt(470), 262 + r.nextInt(20), 26 + r.nextInt(16));
        p.embers(60);
        g.setColor(alpha(c(120, 40, 20), 0.1)); g.fill(new Rectangle2D.Double(0, 0, DW, DH));
        p.mist(250, 24, 0.3);
    }

    // ------------------------------------------------------------------ arches
    /** A natural arch standing on `base`, `w` wide and `h` tall: a heavy span on two piers of layered rock, lit from the left. */
    private static void archOne(ArtGen.Painter p, double cx, double base, double w, double h, Color col, boolean ice) {
        Random r = p.r;
        double[][] pts = {
            {-0.50, 0}, {-0.47, -0.30}, {-0.45, -0.58}, {-0.38, -0.82}, {-0.20, -0.96}, {0.04, -1.0}, {0.26, -0.95}, {0.42, -0.78}, {0.47, -0.52}, {0.49, -0.26}, {0.52, 0},
            {0.24, 0}, {0.22, -0.34}, {0.18, -0.56}, {0.05, -0.70}, {-0.12, -0.67}, {-0.22, -0.52}, {-0.25, -0.30}, {-0.22, 0}};
        List<double[]> poly = new ArrayList<>();
        for (double[] q : pts) poly.add(new double[]{cx + q[0] * w * (1 + r.nextGaussian() * 0.015), base + q[1] * h * (1 + r.nextGaussian() * 0.02)});
        p.rock(poly, col);
        Shape shape = p.path(poly);
        Color lite = ice ? c(240, 250, 255) : mix(col, c(255, 232, 176), 0.5), dark = ice ? c(96, 140, 176) : mix(col, c(60, 30, 34), 0.55);
        strata(p, shape, lite, dark, base - h, base, 4, 13, 0.32);
        Shape old = p.g.getClip(); p.g.setClip(shape);
        sideGradient(p, shape, cx - w * 0.5, alpha(c(255, 232, 180), 0.2), cx + w * 0.5, alpha(c(30, 16, 28), 0.38));
        // the underside of the span and the inner faces of the piers are in shadow
        for (int k = 11; k + 1 < pts.length; k++) {
            double x1 = cx + pts[k][0] * w, y1 = base + pts[k][1] * h, x2 = cx + pts[k + 1][0] * w, y2 = base + pts[k + 1][1] * h;
            p.stroke(x1, y1, x2, y2, 13, c(26, 14, 22), 0.2); p.stroke(x1, y1, x2, y2, 6, c(26, 14, 22), 0.24);
        }
        for (int i = 0; i < 12; i++) { double x = cx + (r.nextDouble() - 0.5) * w, y = base - r.nextDouble() * h; p.stroke(x, y, x + r.nextGaussian() * 2, y + 8 + r.nextInt(26), 1.6, dark, 0.3); }   // cracks and weathering
        p.g.setClip(old);
        for (int i = 0; i < 14; i++) { double t = i / 13.0, a = Math.PI * (1.05 + t * 0.95); p.stroke(cx + Math.cos(a) * w * 0.48, base - h * 0.52 + Math.sin(a) * h * 0.48, cx + Math.cos(a + 0.12) * w * 0.48, base - h * 0.52 + Math.sin(a + 0.12) * h * 0.48, 2.2, lite, 0.5); } // sun on the outer rim
        p.washCircle(cx, base + 2, w * 0.7, 6, mix(p.pal.sand, c(120, 90, 70), 0.4), 4, 0.5, 0.15);
    }

    static void arch(ArtGen.Painter p, String noun) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        boolean ice = noun.equals("ice_arches"), sea = noun.equals("sea_arches"), bridge = noun.equals("natural_bridges");
        int na = p.o.count > 0 ? Math.min(p.o.count, 6) : 1;
        p.sky(176);
        p.sun(60 + r.nextInt(120), 62, 24);
        Color stone = ice ? c(170, 208, 226) : sea ? mix(pal.rock, c(150, 130, 112), 0.5) : mix(c(206, 128, 88), pal.rock, 0.25 + r.nextDouble() * 0.2);
        // distant buttes seen through the opening
        for (int i = 0; i < 5; i++) { double x = 20 + r.nextInt(480), w = 40 + r.nextInt(70), y = 168, h = 20 + r.nextInt(34); p.wash(poly(x - w * 0.6, y + 6, x - w * 0.5, y - h, x - w * 0.3, y - h - 3, x + w * 0.3, y - h - 2, x + w * 0.5, y - h * 0.9, x + w * 0.6, y + 6), mix(ice ? c(200, 224, 238) : mix(stone, pal.skyBot, 0.65), pal.skyBot, 0.3), 6, 0.95, 0.03); }
        if (sea) {
            ArtNature.waves(p, 176, 300);
            for (int i = 0; i < 3; i++) p.washRect(-10, 236 + i * 20, DW + 20, 4, c(255, 255, 255), 3, 0.5, 0.2);
        } else {
            Color ground = ice ? c(228, 240, 248) : mix(pal.sand, c(222, 170, 118), 0.4);
            List<double[]> gl = new ArrayList<>(); gl.add(new double[]{-8, 300}); for (int x = -8; x <= DW + 8; x += 24) gl.add(new double[]{x, 214 + Math.sin(x * 0.02) * 6 + r.nextInt(5)}); gl.add(new double[]{DW + 8, 300});
            p.wash(gl, ground, 10, 0.97, 0.03);
            for (int i = 0; i < 26; i++) { double x = r.nextInt(DW), y = 224 + r.nextInt(64); p.stroke(x, y, x + 16 + r.nextInt(30), y + r.nextGaussian() * 1.5, 1.8, ice ? c(150, 190, 214) : mix(ground, c(140, 96, 70), 0.6), 0.22); } // wind ripples
        }
        double span = 400.0 / na;
        for (int k = 0; k < na; k++) {
            double cx = 56 + (k + 0.5) * span + r.nextInt(14) - 7, scale = na == 1 ? 1.0 : na <= 3 ? 0.78 : 0.6, w = (bridge ? 390 : 340) * scale * (na == 1 ? 1.0 : 0.62), hgt = (bridge ? 150 : 210) * scale;
            archOne(p, cx, 272 - (k % 2) * 8, Math.min(w, span * 1.15), hgt, stone, ice);
        }
        // small things for scale: shrubs, boulders, a traveller
        if (!ice && !sea) for (int i = 0; i < 9; i++) { double x = r.nextInt(DW), y = 262 + r.nextInt(26); p.broad(x, y, 12 + r.nextInt(12), mix(pal.leafDark, c(120, 120, 70), 0.4)); }
        for (int i = 0; i < 6; i++) { double x = r.nextInt(DW), y = 270 + r.nextInt(22), w = 8 + r.nextInt(20); p.rock(poly(x - w, y + 5, x - w * 0.6, y - w * 0.5, x + w * 0.2, y - w * 0.7, x + w, y - 2, x + w * 0.6, y + 8), mix(stone, c(70, 50, 52), 0.4)); }
        if (r.nextInt(2) == 0) p.figure(246 + r.nextInt(60), 262, 1.3);
        p.mist(270, 22, 0.25);
        p.birds(300 + r.nextInt(120), 70, 3, 1.0);
    }

    // ------------------------------------------------------------------ the sea
    /** Wave rows from `horizon` to `bottom`: they grow and spread towards the viewer, each with a bright crest and a dark trough, the biggest with foam; a glitter path runs under the sun. */
    static void waves(ArtGen.Painter p, double horizon, double bottom) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        Color far = mix(pal.water, pal.skyBot, 0.45), deep = mix(pal.waterDeep, c(20, 50, 80), 0.35);
        p.g.setPaint(new LinearGradientPaint(0, (float) horizon, 0, (float) bottom, new float[]{0f, 0.35f, 1f}, new Color[]{far, mix(pal.water, deep, 0.35), deep}));
        p.g.fill(new Rectangle2D.Double(-4, horizon, DW + 8, bottom - horizon + 6));
        for (int i = 0; i < 6; i++) { double s = i / 5.0, y = horizon + 10 + Math.pow(s, 1.5) * (bottom - horizon - 20); p.washRect(-20, y, DW + 40, 8 + 20 * s, i % 2 == 0 ? alpha(deep, 1) : alpha(pal.skyBot, 1), 3, 0.16, 0.04); } // broad swells
        int rows = (int) (30 + 22 * p.q);
        for (int i = 0; i < rows; i++) {
            double t = (i + r.nextDouble() * 0.7) / rows, s = Math.pow(t, 1.9), y = horizon + 4 + s * (bottom - horizon - 8);
            double wl = 16 + 170 * s, amp = 1.2 + 14 * s, x = -r.nextDouble() * wl;
            while (x < DW) {
                double len = wl * (0.55 + r.nextDouble() * 0.8);
                double px = x, py = y;
                for (int j = 1; j <= 6; j++) {
                    double xx = x + len * j / 6.0, yy = y - Math.sin(Math.PI * j / 6.0) * amp * 0.5;
                    p.stroke(px, py + amp * 0.35, xx, yy + amp * 0.35, 2.2 + 7 * s, mix(deep, c(10, 30, 60), 0.45), 0.2 + 0.16 * s);              // the trough under the crest
                    p.stroke(px, py, xx, yy, 1.5 + 4.2 * s, mix(c(255, 255, 255), pal.skyBot, 0.25), 0.22 + 0.4 * s);                                // the crest
                    px = xx; py = yy;
                }
                if (s > 0.4 && r.nextInt(3) == 0) for (int f = 0; f < 3; f++) p.stroke(x + len * (0.2 + 0.2 * f), y - amp * 0.5 + 1, x + len * (0.28 + 0.2 * f), y - amp * 0.5 + 2, 2.2 + 2 * s, c(255, 255, 255), 0.75); // foam
                x += wl * (0.75 + r.nextDouble() * 0.9);
            }
        }
    }

    static void sea(ArtGen.Painter p, String noun) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        double hz = 150, sunX = 250 + r.nextInt(150);
        p.sky(hz); p.sun(sunX, 96, 28);
        for (int i = 0; i < 3; i++) p.washRect(-20, hz - 10 - i * 14, DW + 40, 6 + r.nextInt(8), alpha(pal.mist, 1), 3, 0.3 - i * 0.08, 0.05);   // a low bank of cloud at the horizon
        waves(p, hz, DH + 4);
        for (int i = 0; i < 70; i++) { // the glitter path of the sun
            double s = Math.pow(r.nextDouble(), 1.3), y = hz + 4 + s * (DH - hz - 8), half = 8 + 70 * s, x = sunX + r.nextGaussian() * half * 0.45, len = 4 + s * 26;
            p.stroke(x - len / 2, y, x + len / 2, y, 1.3 + 2.6 * s, c(255, 244, 208), 0.45 + 0.3 * s);
        }
        if (noun.equals("oceans") || noun.equals("cloud_seas")) { p.boat(60 + r.nextInt(380), hz + 6 + r.nextInt(10), 0.45); p.birds(80 + r.nextInt(300), 56 + r.nextInt(30), 3 + r.nextInt(3), 1.1); }
        if (noun.equals("fjords")) {
            p.mountainLayers(hz - 10, 2, 60, true);
            for (int side = 0; side < 2; side++) {   // two steep walls that close in towards the open sea
                double sg = side == 0 ? 1 : -1, ex = side == 0 ? -8 : DW + 8, ox = side == 0 ? 196 + r.nextInt(30) : DW - 196 - r.nextInt(30);
                List<double[]> wall = new ArrayList<>();
                wall.add(new double[]{ex, DH + 8}); wall.add(new double[]{ex, -8});
                for (int k = 1; k <= 7; k++) { double t = k / 7.0; wall.add(new double[]{ex + sg * (ox - ex) * sg * 0 + (ox - ex) * Math.pow(t, 1.3), -8 + (hz - 8 - 30 * (1 - t)) * t + r.nextGaussian() * 6}); }
                wall.add(new double[]{ox, hz + 4});
                p.wash(wall, mix(pal.near, pal.rockDark, 0.3 + side * 0.15), 12, 0.97, 0.03);
                for (int i = 0; i < 10; i++) { double t = r.nextDouble(); p.pine(ex + (ox - ex) * t * 0.9, hz - 20 + t * 40 - r.nextInt(40), 22, pal.leafDark); }
                for (int i = 0; i < 20; i++) { double x = ex + (ox - ex) * r.nextDouble() * 0.9, y = r.nextInt(100); p.stroke(x, y, x + (side == 0 ? 6 : -6), y + 4, 2.4, c(250, 250, 252), 0.45); }   // snow on the tops
            }
            haze(p, 256, hz + 4, 150, 24, c(250, 246, 236), 0.5);
        } else if (noun.equals("coasts") || noun.equals("shorelines") || noun.equals("tide_pools") || noun.equals("driftwood_shores") || noun.equals("tidal_flats")) {
            beach(p);
        }
        p.mist(hz + 3, 18, 0.3);
    }

    /** A strip of shore along the bottom: wet sand, a foam line that breathes in and out, rocks and a headland with a pine. */
    private static void beach(ArtGen.Painter p) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        double ph = r.nextDouble() * 6, yb = 238 + r.nextInt(14);
        List<double[]> sand = new ArrayList<>(), wet = new ArrayList<>();
        sand.add(new double[]{-8, 300}); wet.add(new double[]{-8, 300});
        for (int x = -8; x <= DW + 8; x += 14) { double y = yb + Math.sin(x * 0.022 + ph) * 9 + r.nextGaussian() * 1.2; sand.add(new double[]{x, y}); wet.add(new double[]{x, y - 8 - Math.sin(x * 0.05 + ph) * 3}); }
        sand.add(new double[]{DW + 8, 300}); wet.add(new double[]{DW + 8, 300});
        p.wash(wet, mix(pal.sand, pal.skyBot, 0.35), 8, 0.9, 0.03);
        p.wash(sand, pal.sand, 10, 0.97, 0.03);
        for (int row = 0; row < 3; row++) for (int x = -8; x < DW; x += 14) { double y = yb - 6 - row * 5 + Math.sin(x * 0.022 + ph + row) * 9, y2 = yb - 6 - row * 5 + Math.sin((x + 14) * 0.022 + ph + row) * 9; if (r.nextInt(5) != 0) p.stroke(x, y - 1, x + 14, y2 - 1, 2.4 - row * 0.5, c(255, 255, 255), 0.7 - row * 0.18); }
        for (int i = 0; i < 16; i++) { double x = r.nextInt(DW), y = yb + 8 + r.nextInt(40); p.stroke(x, y, x + 10 + r.nextInt(20), y + r.nextGaussian(), 1.6, mix(pal.sand, c(120, 100, 80), 0.5), 0.25); }
        for (int i = 0; i < 4; i++) { double x = r.nextInt(DW), y = yb + 14 + r.nextInt(36), w = 8 + r.nextInt(18); p.rock(poly(x - w, y + 5, x - w * 0.6, y - w * 0.5, x + w * 0.2, y - w * 0.7, x + w, y - 2, x + w * 0.6, y + 8), mix(pal.rockDark, c(90, 84, 76), 0.4)); }
        if (r.nextBoolean()) { // a headland running into the sea
            double hx = r.nextBoolean() ? 0 : DW;
            List<double[]> head = hx == 0 ? poly(-8, 120, 40, 118, 100, 134, 150, 166, 176, yb - 4, -8, yb + 6) : poly(DW + 8, 120, DW - 40, 118, DW - 100, 134, DW - 150, 166, DW - 176, yb - 4, DW + 8, yb + 6);
            p.rock(head, mix(pal.rock, pal.rockDark, 0.3)); p.pine(hx == 0 ? 30 : DW - 30, 130, 34, pal.leafDark); p.pine(hx == 0 ? 62 : DW - 62, 128, 28, pal.leafDark);
            p.washCircle(hx == 0 ? 164 : DW - 164, yb - 2, 34, 5, c(255, 255, 255), 4, 0.8, 0.2);
        }
    }

    // ------------------------------------------------------------------ deserts
    private static void duneLayer(ArtGen.Painter p, double base, double amp, double wl, Color lit, Color shade, double haze, boolean ripples) {
        Random r = p.r;
        List<double[]> bumps = new ArrayList<>(); // {centre, height, windward width, lee width}
        double c0 = -r.nextDouble() * wl * 0.6;
        while (c0 < DW + wl) { double w = wl * (0.8 + r.nextDouble() * 0.5); bumps.add(new double[]{c0 + w * 0.5, amp * (0.45 + r.nextDouble() * 0.55), w * 0.62, w * 0.38}); c0 += w; }
        double[] h = new double[DW + 1];
        for (int x = 0; x <= DW; x++) {
            double best = 0;
            for (double[] b : bumps) { double dx = x - b[0], v = dx <= 0 ? b[1] * (1 - smooth(-dx / b[2])) : b[1] * (1 - smooth(dx / b[3])); if (v > best) best = v; }
            h[x] = base - best;
        }
        Color l2 = mix(lit, c(252, 236, 206), haze), s2 = mix(shade, c(238, 214, 186), haze);
        List<double[]> body = new ArrayList<>(); body.add(new double[]{-8, DH + 10});
        for (int x = -8; x <= DW + 8; x += 4) body.add(new double[]{x, h[Math.max(0, Math.min(DW, x))]});
        body.add(new double[]{DW + 8, DH + 10});
        p.wash(body, l2, 10, 0.97, 0.012);
        for (double[] b : bumps) { // the lee face: dark, fading into the valley, with a hard crest line
            double cxb = b[0], lw = b[3];
            if (cxb < -10 || cxb - b[2] > DW + 10) continue;
            double yc = h[(int) Math.max(0, Math.min(DW, cxb))];
            Path2D.Double lee = new Path2D.Double();
            lee.moveTo(cxb, yc);
            for (double x = cxb + 3; x <= cxb + lw; x += 3) lee.lineTo(x, h[(int) Math.max(0, Math.min(DW, x))]);
            lee.lineTo(cxb + lw, base + 1); lee.lineTo(cxb - lw * 0.35, base + 1); lee.closePath();
            p.g.setPaint(new GradientPaint(0, (float) yc, alpha(s2, 0.92), 0, (float) (base + 1), alpha(s2, 0.04)));
            p.g.fill(lee);
            p.stroke(cxb - 8, h[(int) Math.max(0, Math.min(DW, cxb - 8))] + 1, cxb + 2, h[(int) Math.max(0, Math.min(DW, cxb + 2))], 1.8, mix(c(255, 250, 228), l2, haze), 0.6); // the crest catches the light
            if (ripples) for (int i = 0; i < 9; i++) { double x = cxb - b[2] * (0.1 + 0.8 * r.nextDouble()), y = h[(int) Math.max(0, Math.min(DW, x))] + 3 + r.nextDouble() * (b[1] * 0.6), len = 10 + r.nextInt(18); p.stroke(x, y, x + len, y - 0.8, 1.5, mix(shade, l2, 0.3), 0.26); }
        }
    }

    private static void palm(ArtGen.Painter p, double x, double y, double h, double lean) {
        double px = x, py = y;
        for (int s = 1; s <= 6; s++) { double t = s / 6.0, nx = x + lean * t * t, ny = y - h * t; p.stroke(px, py, nx, ny, 4.2 - 2 * t, p.pal.trunk, 0.92); px = nx; py = ny; }
        for (int f = 0; f < 9; f++) {
            double a = Math.toRadians(-172 + f * 20), L = h * (0.5 + p.r.nextDouble() * 0.15), dx = Math.cos(a), dy = Math.sin(a);
            double mx = px + dx * L * 0.6, my = py + dy * L * 0.45 - L * 0.12, ex = px + dx * L * 1.1, ey = py + dy * L * 0.1 + L * 0.5 * Math.abs(dx);
            Color leaf = p.r.nextBoolean() ? p.pal.leafDark : mix(p.pal.leafDark, p.pal.leafLight, 0.5);
            p.stroke(px, py, mx, my, 3.6, leaf, 0.9); p.stroke(mx, my, ex, ey, 2.6, leaf, 0.85);
        }
    }

    private static void camel(ArtGen.Painter p, double x, double y, double s) {
        Color body = c(96, 66, 50);
        p.wash(poly(x - 7 * s, y - 6 * s, x - 5 * s, y - 10 * s, x - 2 * s, y - 6 * s, x + 4 * s, y - 6.5 * s, x + 7 * s, y - 5 * s, x + 6 * s, y - 2 * s, x - 6 * s, y - 2 * s), body, 3, 0.95, 0.05);
        p.stroke(x + 6 * s, y - 5 * s, x + 9 * s, y - 11 * s, 1.5 * s, body, 0.95); p.stroke(x + 9 * s, y - 11 * s, x + 12 * s, y - 11 * s, 1.5 * s, body, 0.95);
        for (int i = 0; i < 4; i++) p.stroke(x - 5 * s + i * 3.4 * s, y - 2 * s, x - 5 * s + i * 3.4 * s, y + 4 * s, 1.1 * s, body, 0.95);
    }

    static void desert(ArtGen.Painter p, String noun) {
        Random r = p.r; ArtGen.Pal pal = p.pal;
        boolean salt = noun.startsWith("salt"), oasis = noun.equals("oases"), mirage = noun.equals("mirage_wastes"), ruins = noun.equals("buried_ruins"), dust = noun.equals("dust_bowls");
        double hz = 150;
        Color skyT = dust ? c(188, 164, 134) : c(204, 214, 216), skyB = dust ? c(236, 208, 168) : c(252, 236, 202);
        Color lit = salt ? c(238, 234, 222) : mix(c(242, 212, 152), pal.sand, 0.15), shade = salt ? c(150, 158, 176) : c(190, 128, 92);
        gradient(p, new Rectangle2D.Double(-4, -4, DW + 8, hz + 12), 0, skyT, hz, skyB);
        for (int i = 0; i < 4; i++) p.washCircle(r.nextDouble() * DW, 24 + r.nextDouble() * 70, 70 + r.nextDouble() * 90, 6 + r.nextDouble() * 6, alpha(c(255, 250, 238), 1), 4, 0.3, 0.2);
        p.sun(70 + r.nextInt(150), 74, 34);
        p.washRect(-10, hz - 14, DW + 20, 26, alpha(c(255, 244, 214), 1), 4, 0.45, 0.2);   // the heat haze on the horizon
        if (!noun.equals("sand_seas") && !noun.equals("dunes")) for (int i = 0; i < 4; i++) { // far mesas
            double x = 20 + r.nextInt(470), w = 40 + r.nextInt(80), h = 14 + r.nextInt(30);
            p.wash(poly(x - w * 0.6, hz + 6, x - w * 0.5, hz - h, x - w * 0.3, hz - h - 3, x + w * 0.3, hz - h - 2, x + w * 0.5, hz - h * 0.8, x + w * 0.6, hz + 6), mix(shade, skyB, 0.7), 6, 0.9, 0.03);
        }
        if (mirage) {
            p.washCircle(256, hz + 6, 170, 9, mix(pal.water, c(230, 240, 245), 0.4), 7, 0.55, 0.12);
            p.washCircle(256, hz - 10, 130, 8, mix(pal.water, c(255, 255, 255), 0.5), 4, 0.3, 0.15);
        }
        if (salt) { // a white crust cracked into plates, with puddles of sky
            List<double[]> flat = poly(-8, hz + 4, DW + 8, hz + 4, DW + 8, DH + 8, -8, DH + 8);
            gradient(p, p.path(flat), hz, mix(lit, skyB, 0.5), DH, lit);
            for (int row = 0; row < 14; row++) {
                double s = Math.pow(row / 13.0, 1.7), y = hz + 8 + s * (DH - hz - 12), pw = 14 + 90 * s;
                for (double x = -r.nextDouble() * pw; x < DW; x += pw * (0.8 + r.nextDouble() * 0.5)) p.stroke(x, y, x + pw * 0.5, y + r.nextGaussian() * (1 + 2 * s), 1.6 + 2.2 * s, shade, 0.5);
                for (double x = -r.nextDouble() * pw; x < DW; x += pw * (0.8 + r.nextDouble() * 0.5)) p.stroke(x, y, x + (r.nextDouble() - 0.5) * pw * 0.6, y + pw * 0.28, 1.5 + 2 * s, shade, 0.42);
            }
            int[] sr = p.peaks(hz + 4, 36, 6); List<double[]> sm = new ArrayList<>(); sm.add(new double[]{-8, hz + 8}); for (int x = -8; x <= DW + 8; x += 8) sm.add(new double[]{x, sr[Math.max(0, Math.min(DW, x))]}); sm.add(new double[]{DW + 8, hz + 8});
            p.wash(sm, mix(c(140, 154, 184), skyB, 0.4), 8, 0.9, 0.02);
            for (int i = 0; i < 4; i++) p.washCircle(40 + r.nextInt(430), hz + 40 + r.nextInt(110), 30 + r.nextInt(80), 4 + r.nextInt(8), mix(skyB, c(120, 170, 196), 0.6), 5, 0.75, 0.08);
        } else {
            double[] bases = {hz + 6, hz + 26, hz + 54, hz + 90, hz + 132};
            for (int l = 0; l < 5; l++) {
                if (ruins && l == 3) ruinsOnDune(p, shade);
                double t = l / 4.0;
                duneLayer(p, bases[l] + 14 + 10 * t, 12 + 48 * t, 90 + 58 * l + r.nextInt(30), mix(lit, c(228, 184, 128), t * 0.35), mix(shade, c(120, 74, 62), t * 0.5), 0.55 - t * 0.5, l >= 2);
                if (l == 1 && !oasis && r.nextInt(3) != 0) { double x0 = 90 + r.nextInt(240); for (int k = 0; k < 4; k++) camel(p, x0 + k * 17, bases[l] - 2 - k * 0.8 + 3, 0.9); }
            }
        }
        if (oasis) {
            p.washCircle(256, 246, 96, 17, mix(pal.water, c(200, 232, 236), 0.2), 6, 0.95, 0.08);
            p.washCircle(256, 238, 110, 22, alpha(pal.leafLight, 1), 4, 0.5, 0.1);
            for (int i = 0; i < 5; i++) palm(p, 176 + i * 40 + r.nextInt(14), 244 + r.nextInt(8), 52 + r.nextInt(26), (r.nextDouble() - 0.5) * 30);
            p.washCircle(256, 252, 70, 7, c(255, 255, 255), 3, 0.25, 0.2);
        } else {
            for (int i = 0; i < 8; i++) { double x = r.nextInt(DW), y = 262 + r.nextInt(28); p.stroke(x, y, x + 3, y - 10 - r.nextInt(8), 2, mix(pal.leafDark, c(150, 130, 80), 0.4), 0.7); p.stroke(x, y - 3, x - 5, y - 9, 1.6, mix(pal.leafDark, c(150, 130, 80), 0.4), 0.7); } // dry brush
            if (r.nextInt(3) == 0) p.deadTree(60 + r.nextInt(380), 276, 40 + r.nextInt(20));
            if (r.nextInt(2) == 0) p.figure(150 + r.nextInt(220), 268, 1.5);
        }
        if (dust) { p.g.setColor(alpha(c(176, 140, 100), 0.22)); p.g.fill(new Rectangle2D.Double(0, 0, DW, DH)); for (int i = 0; i < 40; i++) { double x = r.nextInt(DW), y = r.nextInt(DH); p.stroke(x, y, x + 40 + r.nextInt(60), y + 4, 1.6, c(220, 190, 150), 0.14); } }
        p.mist(hz + 22, 20, 0.2);
    }

    private static void ruinsOnDune(ArtGen.Painter p, Color shade) {
        Random r = p.r;
        double x0 = 120 + r.nextInt(220);
        for (int i = 0; i < 4; i++) {
            double x = x0 + i * 34 + r.nextInt(8), top = 160 + r.nextInt(18) + (i % 2) * 6, w = 8 + r.nextInt(3);
            Color col = mix(c(214, 190, 156), shade, 0.3);
            p.rock(poly(x - w, 240, x - w, top + 4, x - w * 0.5, top - 3, x + w * 0.4, top, x + w, top + 6, x + w, 240), col);
            for (int f = 0; f < 3; f++) p.stroke(x - w * 0.5 + f * w * 0.5, top + 4, x - w * 0.5 + f * w * 0.5, 232, 1.4, mix(col, shade, 0.7), 0.4);
        }
        p.rock(poly(x0 + 8, 160, x0 + 30, 150, x0 + 52, 150, x0 + 74, 164, x0 + 70, 170, x0 + 8, 168), mix(c(214, 190, 156), shade, 0.3));
    }
}
