package Game;

import java.awt.Color;
import java.util.Random;

import static Game.ArtGen.DH;
import static Game.ArtGen.DW;
import static Game.ArtGen.alpha;
import static Game.ArtGen.c;
import static Game.ArtGen.mix;
import static Game.ArtGen.poly;

/** Painted places made by people and by the weather: the capital Originis, the skyline that every other city gets on top of its terrain, a sect's
 *  temple hall, and the finishing touches every picture gets from its terrain and the calendar (a river across the land, snow in winter, falling leaves
 *  in autumn, blossoms in spring, rain and fog). All of it uses the watercolour primitives of ArtGen.Painter. */
final class ArtCity {
    private ArtCity() { }

    // ------------------------------------------------------------------ Originis
    /** The grand walled capital at dusk: mountains behind, a river and a stone bridge in front, towers, a nine-tier pagoda, the great hall, lanterns. */
    static void capital(ArtGen.Painter p) {
        Random r = p.r;
        ArtGen.Pal pal = p.pal;
        p.sky(150);
        p.sun(256 + r.nextInt(40) - 20, 112, 34);
        p.mountainLayers(152, 3, 96, r.nextInt(3) == 0);
        // rolling land behind the walls
        int[] back = p.ridge(160, 8, 0.9, 3);
        p.land(back, DH + 10, mix(pal.far, pal.ground, 0.55), 0.92, false, pal.far);
        p.mist(158, 20, 0.28);

        // the old rooftops, three rows climbing toward the great hall in the middle
        Color[] roofs = {c(62, 66, 64), c(86, 70, 62), c(70, 82, 76), c(104, 78, 62)};
        for (int row = 0; row < 3; row++) {
            double y = 196 - row * 15, s = 1.0 - row * 0.12;
            for (double x = 22 + r.nextInt(14); x < DW - 18; x += (12 + r.nextInt(8)) * s) {
                double w = (9 + r.nextInt(8)) * s, h = (6 + r.nextInt(5)) * s;
                if (Math.abs(x - 256) < 52 && row > 0) continue; // the great hall's square
                p.washRect(x - w / 2, y - h, w, h, c(228, 214, 190), 2, 0.7, 0.05);
                p.wash(poly(x - w * 0.72, y - h + 1, x - w * 0.3, y - h - 5 * s, x + w * 0.3, y - h - 5 * s, x + w * 0.72, y - h + 1), roofs[r.nextInt(roofs.length)], 3, 0.95, 0.05);
            }
        }
        // the great hall under the sun, with a flight of steps
        p.washRect(196, 168, 120, 6, c(214, 204, 186), 3, 0.9, 0.04);
        for (int i = 0; i < 6; i++) p.stroke(220 + i * 5, 174 + i * 2.2, 292 - i * 5, 174 + i * 2.2, 1.6, c(190, 180, 164), 0.5);
        p.pavilion(256, 168, 3.6);
        p.pavilion(212, 170, 1.7);
        p.pavilion(300, 170, 1.7);
        // the nine-tier pagoda on the left and a seven-tier one on the right
        tower(p, 128, 198, 9, 1.35);
        tower(p, 392, 198, 7, 1.2);

        // the city wall with crenellations, two gate towers and the great gate
        Color wall = c(222, 208, 182), shade = c(160, 144, 124);
        p.washRect(28, 202, DW - 56, 32, wall, 5, 0.97, 0.03);
        p.washRect(28, 226, DW - 56, 8, shade, 3, 0.45, 0.05);
        for (double x = 30; x < DW - 34; x += 9) p.washRect(x, 197, 5.5, 6, wall, 2, 0.95, 0.04);
        for (int i = 0; i < 34; i++) p.stroke(32 + r.nextInt(DW - 64), 206 + r.nextInt(24), 40 + r.nextInt(DW - 70), 206 + r.nextInt(24), 1.1, shade, 0.25);
        for (double gx : new double[]{150, 362}) p.pavilion(gx, 204, 2.0);
        p.pavilion(256, 204, 2.7);
        p.wash(poly(236, 234, 236, 218, 256, 208, 276, 218, 276, 234), c(44, 36, 34), 4, 0.95, 0.04); // the great gate
        p.stroke(256, 211, 256, 234, 1.2, c(180, 130, 70), 0.6);
        // lanterns along the wall, banners on the towers
        for (double x = 48; x < DW - 40; x += 34) { p.washCircle(x, 214, 2.6, 3.2, c(212, 58, 44), 3, 0.95, 0.1); p.stroke(x, 208, x, 211, 0.8, c(70, 52, 40), 0.8); }
        for (double bx : new double[]{150, 256, 362}) { p.stroke(bx + 6, 194, bx + 6, 168, 1.1, c(70, 52, 40), 0.9); p.wash(poly(bx + 6, 170, bx + 22, 173, bx + 6, 178), c(190, 52, 44), 3, 0.95, 0.06); }

        // the river with its stone bridge, boats and the evening haze
        p.waterBody(236, DH + 4);
        for (int i = 0; i < 40; i++) { double a = Math.PI * i / 39; p.stroke(190 + Math.cos(a + Math.PI) * 66, 250 - Math.sin(a) * 18, 190 + Math.cos(a + Math.PI + 0.08) * 66, 250 - Math.sin(a + 0.08) * 18, 9, c(120, 94, 82), 0.9); }
        p.stroke(110, 234, 300, 234, 8, c(98, 74, 64), 0.9);
        for (int i = 0; i < 7; i++) p.stroke(118 + i * 28, 226, 118 + i * 28, 234, 2, c(96, 70, 60), 0.85);
        p.boat(360, 262, 1.2); p.boat(436, 272, 0.9); p.boat(70, 270, 1.0);
        p.birds(300 + r.nextInt(100), 70 + r.nextInt(30), 4 + r.nextInt(3), 1.0);
        // chimney smoke
        for (int i = 0; i < 6; i++) { double x = 60 + r.nextInt(390); for (int k = 0; k < 5; k++) p.stroke(x + Math.sin(k + i) * 3, 176 - k * 7, x + Math.sin(k + 1 + i) * 3, 176 - (k + 1) * 7, 2.0, c(230, 226, 220), 0.35 - k * 0.05); }
        p.mist(238, 18, 0.35);
    }

    /** A tiered pagoda (the Painter's own has 3-4 tiers). */
    static void tower(ArtGen.Painter p, double x, double y, int tiers, double s) {
        Color wall = c(176, 78, 60), roof = c(56, 60, 60);
        double w = 14 * s, h = 8 * s;
        for (int i = 0; i < tiers; i++) {
            double ww = w * (1 - i * 0.075), by = y - i * h * 1.3;
            p.washRect(x - ww * 0.6, by - h, ww * 1.2, h, wall, 3, 0.9, 0.05);
            p.wash(poly(x - ww * 1.3, by - h + 1 * s, x - ww * 0.7, by - h - 3.2 * s, x + ww * 0.7, by - h - 3.2 * s, x + ww * 1.3, by - h + 1 * s, x, by - h - 1.2 * s), roof, 3, 0.95, 0.04);
        }
        double top = y - tiers * h * 1.3;
        p.stroke(x, top - 2 * s, x, top - 12 * s, 1.5 * s, roof, 0.9);
        p.washCircle(x, top - 12 * s, 1.8 * s, 2.2 * s, c(214, 170, 70), 3, 0.95, 0.1);
    }

    // ------------------------------------------------------------------ other cities and sects, on top of their terrain
    /** size 1 = a town (a few roofs and one pagoda), 2 = a city (rows of roofs, two pagodas, a wall). Stands low in the picture, left or right of centre. */
    static void skyline(ArtGen.Painter p, int size) {
        Random r = p.r;
        double cx = r.nextBoolean() ? 150 + r.nextInt(60) : 300 + r.nextInt(60);
        double base = 236 + r.nextInt(14);
        int houses = size == 1 ? 7 : 16;
        Color[] roofs = {c(62, 66, 64), c(86, 70, 62), c(70, 82, 76), c(104, 78, 62)};
        if (size >= 2) { p.washRect(cx - 100, base - 8, 200, 14, c(222, 208, 182), 4, 0.9, 0.04); for (double x = cx - 98; x < cx + 96; x += 9) p.washRect(x, base - 12, 5, 5, c(222, 208, 182), 2, 0.9, 0.04); }
        for (int i = 0; i < houses; i++) {
            double x = cx + r.nextGaussian() * (size == 1 ? 24 : 52), y = base - 8 - r.nextInt(size == 1 ? 8 : 26), s = 0.7 + r.nextDouble() * 0.5;
            double w = 11 * s, h = 7 * s;
            p.washRect(x - w / 2, y - h, w, h, c(230, 216, 192), 2, 0.8, 0.05);
            p.wash(poly(x - w * 0.75, y - h + 1, x - w * 0.3, y - h - 5 * s, x + w * 0.3, y - h - 5 * s, x + w * 0.75, y - h + 1), roofs[r.nextInt(roofs.length)], 3, 0.95, 0.05);
        }
        p.pagoda(cx + (size == 1 ? 0 : -34), base - 14, size == 1 ? 1.0 : 1.3);
        if (size >= 2) { p.pagoda(cx + 40, base - 16, 1.0); p.pavilion(cx, base - 22, 1.6); }
        for (int i = 0; i < 3 + size * 2; i++) { double x = cx + r.nextGaussian() * 40; for (int k = 0; k < 4; k++) p.stroke(x + Math.sin(k) * 2, base - 30 - k * 6, x + Math.sin(k + 1) * 2, base - 30 - (k + 1) * 6, 1.6, c(232, 228, 222), 0.3 - k * 0.06); }
    }

    /** A sect: a stepped temple complex with a gate, a hall and a pagoda, high on the side of the land. */
    static void sectHall(ArtGen.Painter p) {
        Random r = p.r;
        double x0 = r.nextBoolean() ? 80 + r.nextInt(40) : 330 + r.nextInt(50);
        for (int t = 0; t < 3; t++) { // terraces climbing up
            double y = 232 - t * 24;
            p.washRect(x0 - 50 + t * 6, y, 100 - t * 12, 8, c(206, 196, 176), 3, 0.85, 0.05);
        }
        for (int i = 0; i < 18; i++) p.stroke(x0 - 4, 238 - i * 2.6, x0 + 4, 238 - i * 2.6, 1.2, c(180, 170, 152), 0.45); // the long stair
        p.pavilion(x0, 186, 2.3);
        p.pavilion(x0 - 34, 212, 1.4); p.pavilion(x0 + 34, 212, 1.4);
        p.pagoda(x0 + 52, 214, 1.2);
        // the mountain gate
        p.stroke(x0 - 14, 246, x0 - 14, 226, 2.4, c(168, 62, 50), 0.95); p.stroke(x0 + 14, 246, x0 + 14, 226, 2.4, c(168, 62, 50), 0.95);
        p.stroke(x0 - 20, 226, x0 + 20, 226, 3, c(168, 62, 50), 0.95); p.stroke(x0 - 16, 232, x0 + 16, 232, 1.8, c(60, 56, 54), 0.9);
        p.washCircle(x0 - 24, 238, 2.4, 3.0, c(212, 58, 44), 3, 0.95, 0.1); p.washCircle(x0 + 24, 238, 2.4, 3.0, c(212, 58, 44), 3, 0.95, 0.1);
        p.mist(236, 14, 0.3);
    }

    // ------------------------------------------------------------------ terrain and calendar
    private static boolean landKind(String kind) {
        switch (kind) {
            case "hills": case "meadow": case "forest": case "desert": case "mountains": case "marsh": case "arch": case "pillars": case "menhirs": case "terraces": case "mounds": return true;
            default: return false;
        }
    }

    /** A river across the land (tiles that have one), the season and the weather. Called after the scene is painted. */
    static void finish(ArtGen.Painter p, String kind) {
        Random r = p.r;
        ArtGen.Opts o = p.o;
        if (o.river && landKind(kind)) river(p);
        // season
        boolean cold = p.pal.ground.getBlue() > 200;
        if (o.season == 3 && !cold && !kind.equals("sea") && !kind.equals("cave")) { p.washRect(-10, 150, DW + 20, DH - 140, c(246, 250, 255), 6, 0.42, 0.1); p.flakes(70); }
        else if (o.season == 2 && (kind.equals("forest") || kind.equals("hills") || kind.equals("meadow") || kind.equals("mountains"))) {
            p.washRect(-10, 150, DW + 20, 130, c(222, 126, 52), 5, 0.22, 0.2);
            for (int i = 0; i < 26; i++) { double x = r.nextInt(DW), y = 60 + r.nextInt(200); p.stroke(x, y, x + 5 + r.nextInt(5), y + 3, 2.2, r.nextBoolean() ? c(214, 110, 40) : c(196, 70, 36), 0.7); }
        } else if (o.season == 0 && (kind.equals("meadow") || kind.equals("hills") || kind.equals("forest"))) p.flowers(50);
        // weather
        if (o.weather == 1) {
            p.washRect(0, 0, DW, 170, c(88, 98, 112), 4, 0.18, 0.1);
            for (int i = 0; i < 150; i++) { double x = r.nextInt(DW + 40), y = r.nextInt(DH); p.stroke(x, y, x - 6, y + 16, 1.0, c(210, 222, 232), 0.4); }
            p.mist(210, 50, 0.3);
        } else if (o.weather == 2) {
            p.mist(120, 70, 0.3); p.mist(200, 70, 0.3);
            p.washRect(-10, 0, DW + 20, DH, c(240, 240, 238), 5, 0.12, 0.1);
        }
    }

    /** A winding river from the horizon to the front of the picture. */
    static void river(ArtGen.Painter p) {
        Random r = p.r;
        double x0 = 120 + r.nextInt(280), x1 = 60 + r.nextInt(400), phase = r.nextDouble() * 6.28;
        double px = x0, py = 160;
        for (int s = 1; s <= 28; s++) {
            double t = s / 28.0;
            double x = x0 + (x1 - x0) * t + Math.sin(t * 5 + phase) * (18 + 40 * t), y = 160 + t * 130;
            double w = 3 + t * t * 26;
            p.stroke(px, py, x, y, w, mix(p.pal.water, p.pal.skyBot, 0.25), 0.9);
            p.stroke(px + 1, py, x + 1, y, Math.max(1, w * 0.3), c(252, 252, 250), 0.45);
            px = x; py = y;
        }
        p.mist(168, 12, 0.25);
    }
}
