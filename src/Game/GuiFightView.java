package Game;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** The fight window: two stick figures, one in the colour of each fighter's element, play out a `FightReplay` with health bars, the techniques called out and a flash where an element gives one the upper hand.
 *  Two ways to see a fight: LIVE (`settings fightview on`: the window opens when a fight the person who plays was in is over, the game waits until it is closed or skipped) and REPLAY (always: the Fights window
 *  lists the last fights with a Watch button, and the tournament result has a Watch button for each bout). The fights are settled before the window opens; it shows them, it never decides them.
 *  Window code runs on the Swing thread; `live` is called from the game thread, which waits with the world released. */
final class GuiFightView {
    private GuiFightView() { }

    // ------------------------------------------------------------------ opening
    /** The game thread shows a fight and waits until the window is closed (or skipped to the end and closed). */
    static void live(final FightReplay f) {
        if (f == null || GameDriver.mapViewer == null) return;
        final CountDownLatch done = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> open(GameDriver.mapViewer, f, done));
        try { GameDriver.blocked(() -> { done.await(); return null; }); } catch (RuntimeException ignored) { }
    }

    /** A replay: the game goes on (call on the Swing thread, or any thread). */
    static void replay(final MapViewer mv, final FightReplay f) {
        if (f == null || mv == null) return;
        SwingUtilities.invokeLater(() -> open(mv, f, null));
    }

    // ------------------------------------------------------------------ the stage
    static final class Stage extends JPanel {
        final FightReplay f;
        final javax.swing.Timer timer;
        int beat = -1;                 // the beat being played (-1: before the first)
        long beatStart;
        double speed = 1.0;
        boolean paused = false, finished = false;
        double dispA = 100, dispB = 100;   // the health shown (moves toward the beat's)
        final List<float[]> floaters = new ArrayList<>();  // damage numbers: x, y, age
        final List<String> floatTexts = new ArrayList<>();
        Runnable onBeat, onEnd;
        long lastTick = System.nanoTime();
        BufferedImage bg;              // the picture of the place where it was fought (dimmed), or null
        long hitAt = 0;                // when the last hard blow landed (a tournament stage rings, shakes and flashes with it)
        final long stageStart = System.currentTimeMillis();
        final java.util.Map<String, FightFx.Look> looks = new java.util.HashMap<>();
        FightFx.Look look(String tech, boolean movement, int elem) {
            String k = (movement ? "m:" : "t:") + tech + "/" + elem;
            FightFx.Look l = looks.get(k);
            if (l == null) { l = FightFx.look(tech, movement, elem); looks.put(k, l); }
            return l;
        }

        Stage(FightReplay f) {
            this.f = f;
            setPreferredSize(new Dimension(760, 300));
            setBackground(new Color(20, 24, 30));
            timer = new javax.swing.Timer(30, e -> tick());
        }

        void restart() {
            beat = -1; dispA = 100; dispB = 100; finished = false; floaters.clear(); floatTexts.clear();
            beatStart = System.currentTimeMillis();
            lastTick = System.nanoTime();
            next();
            timer.start();
        }

        long beatMs(FightReplay.Beat b) { return (long) ((b.kind == FightReplay.TALK || b.kind == FightReplay.ARRAY ? 1600 : b.kind == FightReplay.FORBID ? 1700 : 1150) / speed); }

        void next() {
            beat++;
            if (beat >= f.beats.size()) { finished = true; beat = f.beats.size() - 1; timer.stop(); if (onEnd != null) onEnd.run(); repaint(); return; }
            beatStart = System.currentTimeMillis();
            FightReplay.Beat b = f.beats.get(beat);
            if (b.actor >= 0 && b.kind != FightReplay.FLEE && b.kind != FightReplay.TALK) {
                FightReplay.Beat prev = beat > 0 ? f.beats.get(beat - 1) : null;
                int before = b.actor == 0 ? (prev == null ? 100 : prev.hpB) : (prev == null ? 100 : prev.hpA);
                int after = b.actor == 0 ? b.hpB : b.hpA;
                if (before > after) { floaters.add(new float[]{b.actor == 0 ? 0.72f : 0.28f, 0.42f, 0}); floatTexts.add("-" + (before - after)); }
            }
            if (onBeat != null) onBeat.run();
        }

        void skipToEnd() {
            timer.stop();
            while (!finished) { beat = f.beats.size() - 1; finished = true; }
            FightReplay.Beat last = f.beats.get(f.beats.size() - 1);
            dispA = last.hpA; dispB = last.hpB; floaters.clear(); floatTexts.clear();
            if (onBeat != null) onBeat.run();
            if (onEnd != null) onEnd.run();
            repaint();
        }

        void tick() {
            long now = System.nanoTime();
            double dt = Math.min(0.1, (now - lastTick) / 1e9);
            lastTick = now;
            if (paused || finished || beat < 0) { repaint(); return; }
            FightReplay.Beat b = f.beats.get(beat);
            long age = System.currentTimeMillis() - beatStart;
            double p = Math.min(1.0, age / (double) beatMs(b));
            // health runs toward the beat's after the strike lands
            if (p >= 0.5) {
                double rate = 80 * dt * speed;
                dispA = approach(dispA, b.hpA, rate); dispB = approach(dispB, b.hpB, rate);
            }
            for (float[] fl : floaters) fl[2] += dt * speed;
            if (p >= 1.0) next();
            repaint();
        }

        private double approach(double v, double target, double step) { return v < target ? Math.min(target, v + step) : Math.max(target, v - step); }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(24, 30, 40), 0, h, new Color(12, 14, 18)));
            g.fillRect(0, 0, w, h);
            if (bg != null) { // the place, cut to fill the stage and dimmed so the fighters and the bars stand out
                double k = Math.max(w / (double) bg.getWidth(), h / (double) bg.getHeight());
                int iw = (int) (bg.getWidth() * k), ih = (int) (bg.getHeight() * k);
                g.drawImage(bg, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
                g.setPaint(new GradientPaint(0, 0, new Color(8, 10, 14, 120), 0, h, new Color(8, 10, 14, 185)));
                g.fillRect(0, 0, w, h);
            }
            int ground = (int) (h * 0.80);
            if (f.tourney == 0) { g.setColor(new Color(60, 66, 76)); g.setStroke(new BasicStroke(2f)); g.drawLine(24, ground, w - 24, ground); }
            Color ca = new Color(Elements.COLOR[f.elemA]), cb = new Color(Elements.COLOR[f.elemB]);
            FightReplay.Beat b = beat >= 0 ? f.beats.get(Math.min(beat, f.beats.size() - 1)) : null;
            long age = System.currentTimeMillis() - beatStart;
            double p = finished || b == null ? 1.0 : Math.min(1.0, age / (double) beatMs(b));
            // the stage of a tournament: a crowd and banners, or the Garden's night sky and its carved platform
            final long nowMs = System.currentTimeMillis();
            final double hitFx = Math.max(0, 1 - (nowMs - hitAt) / 650.0);
            double finishFx = 0;
            if (b != null && !finished && beat == f.beats.size() - 1 && b.kind == FightReplay.FINISH && p > 0.6) finishFx = 1 - (p - 0.6) / 0.4;
            if (f.tourney > 0) FightFx.stage(g, f.tourney, w, h, ground, nowMs - stageStart, hitFx, finishFx, f.aWon);
            final java.awt.geom.AffineTransform before = g.getTransform();
            if (f.tourney > 0 && hitFx > 0.45) g.translate(Math.sin(nowMs / 23.0) * 3 * hitFx, Math.cos(nowMs / 31.0) * 2 * hitFx);   // a hard blow shakes the stage
            // positions: A on the left, B on the right
            double ax = w * 0.28, bx = w * 0.72, lungeA = 0, lungeB = 0, flinchA = 0, flinchB = 0;
            boolean hitting = false;
            if (b != null && !finished && b.actor >= 0 && b.kind != FightReplay.TALK && b.kind != FightReplay.ARRAY && b.kind != FightReplay.FORBID) {
                double reach = p < 0.35 ? p / 0.35 : p < 0.55 ? 1.0 : Math.max(0, 1.0 - (p - 0.55) / 0.45);
                double flinch = p >= 0.40 && p < 0.80 ? Math.sin((p - 0.40) / 0.40 * Math.PI) : 0;
                if (b.kind == FightReplay.FLEE) {
                    if (b.actor == 0) lungeA = -reach * 0 - p * 160; else lungeB = p * 160;
                } else if (b.kind == FightReplay.BLOCK) {
                    if (b.actor == 0) { lungeA = reach * 70; flinchB = flinch * 22; } else { lungeB = -reach * 70; flinchA = -flinch * 22; }
                } else {
                    if (b.actor == 0) { lungeA = reach * 120; flinchB = flinch * 16; } else { lungeB = -reach * 120; flinchA = -flinch * 16; }
                    hitting = p >= 0.45 && p < 0.65;
                }
            }
            boolean aDown = finished && (f.killed && f.bWon || f.spared && f.bWon || f.injured && f.bWon) && f.endA() <= 20;
            boolean bDown = finished && (f.killed && f.aWon || f.spared && f.aWon || f.injured && f.aWon) && f.endB() <= 20;
            if (!finished && beat >= 0 && f.aWon && beat == f.beats.size() - 1 && p > 0.7 && f.endB() <= 20 && !f.fledB) bDown = true;
            if (!finished && beat >= 0 && f.bWon && beat == f.beats.size() - 1 && p > 0.7 && f.endA() <= 20 && !f.fledA) aDown = true;
            // auras
            drawAura(g, (int) (ax + lungeA + flinchA), ground, ca, b != null && b.boost && b.actor == 0 && hitting);
            drawAura(g, (int) (bx + lungeB + flinchB), ground, cb, b != null && b.boost && b.actor == 1 && hitting);
            // figures
            double stepA = Math.sin(age / 90.0) * (lungeA != 0 ? 1 : 0.15), stepB = Math.sin(age / 90.0 + 1) * (lungeB != 0 ? 1 : 0.15);
            if (hitting && nowMs - hitAt > 450) hitAt = nowMs;
            // a movement technique is the fighter's own motion: after-images, a leap, a fade (the one who slips a blow, or runs)
            int moverSide = -1; FightFx.Look mlk = null; double[] mo = null;
            if (b != null && !finished && b.tech != null && b.movement && b.actor >= 0) { moverSide = b.kind == FightReplay.FLEE ? b.actor : 1 - b.actor; mlk = look(b.tech, true, b.techElem); mo = FightFx.motion(mlk, p); }
            for (int side = 0; side < 2; side++) {
                int cx = (int) (side == 0 ? ax + lungeA + flinchA : bx + lungeB + flinchB), face = side == 0 ? 1 : -1;
                Color col = side == 0 ? ca : cb;
                int kind = side == 0 ? f.kindA : f.kindB;
                boolean acting = b != null && b.actor == side && !finished, down = side == 0 ? aDown : bDown, hurt = (side == 0 ? flinchA : flinchB) != 0;
                double st = side == 0 ? stepA : stepB;
                int lift = 0;
                java.awt.Composite old = g.getComposite();
                if (side == moverSide && mo != null) {
                    for (int k = 1; k <= (int) mo[0]; k++) {
                        g.setComposite(AlphaComposite.SrcOver.derive((float) Math.max(0.05, 0.40 - 0.06 * k)));
                        drawFighter(g, kind, cx + face * k * (int) mo[1], ground, face, col, acting, st, down, hurt);
                    }
                    lift = (int) mo[2];
                    g.setComposite(AlphaComposite.SrcOver.derive((float) Math.max(0.12, Math.min(1.0, mo[3]))));
                }
                drawFighter(g, kind, cx, ground + lift, face, col, acting, st, down, hurt);
                g.setComposite(old);
                if (side == moverSide && mlk != null) FightFx.movement(g, mlk, p, cx, ground, face, new Color(Elements.COLOR[Math.max(0, Math.min(4, b.techElem))]), b.tech.hashCode() * 17L + beat);
            }
            // the blow of a martial technique, drawn from the technique's name
            if (b != null && !finished && b.tech != null && !b.movement && b.actor >= 0) {
                FightFx.Look lk = look(b.tech, false, b.techElem);
                double xa = b.actor == 0 ? ax + lungeA : bx + lungeB, xb = b.actor == 0 ? bx + lungeB : ax + lungeA;
                FightFx.blow(g, lk, p, xa, ground - 70, xb, ground - 70, b.actor == 0 ? 1 : -1, new Color(Elements.COLOR[Math.max(0, Math.min(4, b.techElem))]), b.tech.hashCode() * 31L + beat);
            }
            // strike flash
            if (hitting && b != null) {
                int cx = (int) ((ax + lungeA + bx + lungeB) / 2), cy = ground - 78;
                g.setColor(b.boost ? new Color(Elements.COLOR[b.actor == 0 ? f.elemA : f.elemB]) : new Color(255, 245, 210));
                g.setStroke(new BasicStroke(3f));
                for (int i = 0; i < 7; i++) { double ang = i * Math.PI / 3.5 + age / 200.0; g.drawLine(cx, cy, cx + (int) (Math.cos(ang) * 26), cy + (int) (Math.sin(ang) * 26)); }
            }
            // floating damage numbers
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            for (int i = 0; i < floaters.size(); i++) {
                float[] fl = floaters.get(i);
                if (fl[2] > 1.4) continue;
                g.setColor(new Color(255, 90, 80, (int) Math.max(0, 255 * (1 - fl[2] / 1.4))));
                g.drawString(floatTexts.get(i), (int) (w * fl[0]) - 10, (int) (h * fl[1]) - (int) (fl[2] * 40));
            }
            g.setTransform(before);
            // health bars and names
            drawBar(g, 24, 16, (w - 72) / 2, f.nameA, f.elemA, dispA, f.lvlA, false);
            drawBar(g, w / 2 + 24, 16, (w - 72) / 2, f.nameB, f.elemB, dispB, f.lvlB, true);
            g.dispose();
        }

        private void drawBar(Graphics2D g, int x, int y, int w, String name, int elem, double hp, int lvl, boolean right) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.setColor(new Color(235, 235, 225));
            String label = name + "  ·  " + Elements.name(elem) + "  ·  Lv " + lvl;
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, right ? x + w - fm.stringWidth(label) : x, y + 12);
            int by = y + 20, bh = 14;
            g.setColor(new Color(50, 54, 62)); g.fillRoundRect(x, by, w, bh, 8, 8);
            Color c = hp > 55 ? new Color(90, 190, 110) : hp > 25 ? new Color(220, 180, 70) : new Color(215, 80, 70);
            int fillW = (int) Math.round(w * Math.max(0, Math.min(100, hp)) / 100.0);
            g.setColor(c);
            if (right) g.fillRoundRect(x + w - fillW, by, fillW, bh, 8, 8); else g.fillRoundRect(x, by, fillW, bh, 8, 8);
            g.setColor(new Color(120, 126, 136)); g.drawRoundRect(x, by, w, bh, 8, 8);
        }

        private void drawAura(Graphics2D g, int cx, int ground, Color c, boolean flare) {
            int r = flare ? 78 : 52;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), flare ? 95 : 42));
            g.fill(new Ellipse2D.Double(cx - r, ground - 70 - r, r * 2, r * 2));
        }

        private void drawFighter(Graphics2D g, int kind, int cx, int ground, int facing, Color c, boolean acting, double stride, boolean down, boolean hurt) {
            if (kind == 0) drawFigure(g, cx, ground, facing, c, acting, stride, down, hurt);
            else drawAnimal(g, cx, ground, facing, c, acting, stride, down, hurt, kind == 2);
        }

        /** A stick animal (a beast: a four-legged shape with a head, ears and a tail); a Monster is bigger, spiked and has glowing eyes. */
        private void drawAnimal(Graphics2D g, int cx, int ground, int facing, Color c, boolean acting, double stride, boolean down, boolean hurt, boolean monster) {
            double sc = monster ? 1.35 : 1.0;
            g.setStroke(new BasicStroke(monster ? 5f : 4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Color col = hurt ? new Color(255, 170, 160) : c;
            g.setColor(col);
            int len = (int) (56 * sc), r = (int) (9 * sc);
            if (down) { // lying on its side
                int by = ground - 12;
                g.drawLine(cx - facing * len / 2, by, cx + facing * len / 2, by);
                g.draw(new Ellipse2D.Double(cx + facing * (len / 2 + r) - r, by - r, 2 * r, 2 * r));
                g.drawLine(cx - facing * len / 4, by, cx - facing * len / 4 - 8, by + 10); g.drawLine(cx + facing * len / 4, by, cx + facing * len / 4 + 8, by + 10);
                g.drawLine(cx - facing * len / 2, by, cx - facing * (len / 2 + (int) (14 * sc)), by - 4);
                return;
            }
            int rx = cx - facing * len / 2, fx = cx + facing * len / 2, by = ground - (int) (36 * sc);
            int lean = acting ? facing * 10 : 0, sw = (int) (stride * 10);
            g.drawLine(rx, by, fx, by - (acting ? 6 : 0));                                       // the back
            g.drawLine(rx, by, rx - sw, ground); g.drawLine(rx + facing * 7, by, rx + facing * 7 + sw, ground);   // hind legs
            g.drawLine(fx, by, fx + sw, ground); g.drawLine(fx - facing * 7, by, fx - facing * 7 - sw, ground);   // forelegs
            int hx = fx + facing * (int) (16 * sc) + lean, hy = by - (int) (22 * sc);
            g.drawLine(fx, by - (acting ? 6 : 0), hx, hy);                                      // the neck
            g.draw(new Ellipse2D.Double(hx - r, hy - r, 2 * r, 2 * r));                          // the head
            g.drawLine(hx + facing * r, hy + 1, hx + facing * (r + (int) (11 * sc)), hy + (acting ? 6 : 3));   // the snout
            g.drawLine(hx - facing * 2, hy - r, hx - facing * 6, hy - r - (int) (9 * sc)); g.drawLine(hx + facing * 3, hy - r, hx + facing * 5, hy - r - (int) (9 * sc)); // ears
            g.drawLine(rx, by, rx - facing * (int) (20 * sc), by - (int) (18 * sc));             // the tail
            if (monster) {
                for (int i = 0; i < 5; i++) { int sx = rx + facing * i * len / 4; g.drawLine(sx, by, sx - facing * 3, by - 12); }
                g.setColor(new Color(255, 214, 80)); g.fill(new Ellipse2D.Double(hx + facing * 2 - 2.5, hy - 3, 5, 5));
            }
        }

        private void drawFigure(Graphics2D g, int cx, int ground, int facing, Color c, boolean acting, double stride, boolean down, boolean hurt) {
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(hurt ? new Color(255, 170, 160) : c);
            if (down) { // lying on the ground
                int hx = cx - facing * 46;
                g.draw(new Ellipse2D.Double(hx - 12, ground - 24, 24, 24));
                g.drawLine(hx + facing * 12, ground - 10, cx + facing * 40, ground - 8);
                g.drawLine(cx + facing * 40, ground - 8, cx + facing * 64, ground - 2);
                g.drawLine(cx + facing * 8, ground - 9, cx + facing * 24, ground - 24);
                return;
            }
            int hipY = ground - 42, shoulderY = ground - 82;
            int lean = acting ? facing * 8 : 0;
            int headX = cx + lean, headY = ground - 100;
            g.draw(new Ellipse2D.Double(headX - 12, headY - 12, 24, 24));
            g.drawLine(headX, headY + 12, cx, shoulderY); g.drawLine(cx, shoulderY, cx, hipY); // neck and body
            int legSpread = (int) (stride * 14);
            g.drawLine(cx, hipY, cx - 14 + legSpread, ground); g.drawLine(cx, hipY, cx + 14 - legSpread, ground);
            if (acting) { // one arm thrust forward, one back
                g.drawLine(cx, shoulderY + 6, cx + facing * 38, shoulderY + 2); g.drawLine(cx, shoulderY + 6, cx - facing * 18, shoulderY + 22);
            } else if (hurt) {
                g.drawLine(cx, shoulderY + 6, cx - facing * 22, shoulderY - 10); g.drawLine(cx, shoulderY + 6, cx - facing * 26, shoulderY + 14);
            } else {
                g.drawLine(cx, shoulderY + 6, cx + facing * 22, shoulderY + 24); g.drawLine(cx, shoulderY + 6, cx - facing * 14, shoulderY + 26);
            }
        }
    }

    // ------------------------------------------------------------------ the window
    private static void open(final MapViewer mv, final FightReplay f, final CountDownLatch done) {
        final JDialog d = new JDialog(mv, "Fight  ·  " + f.nameA + " against " + f.nameB, false);
        d.setName("fightview");
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.getContentPane().setBackground(Theme.BG);
        final Stage stage = new Stage(f);
        if (f.tourney > 0) {
            EventMusic.hold(f.tourney);
            d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosed(java.awt.event.WindowEvent e) { EventMusic.release(); } });
        }
        if (Settings.pictures && f.area != null && f.tourney != 2) { try { stage.bg = PlaceImages.render(f.area, 800, 450, false); } catch (RuntimeException ex) { stage.bg = null; } }
        final JLabel caption = new JLabel(" ");
        caption.setForeground(Theme.TEXT); caption.setFont(Theme.plain(16)); caption.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        final JTextArea log = new JTextArea(5, 40);
        log.setEditable(false); log.setBackground(Theme.FIELD); log.setForeground(Theme.DIM); log.setFont(Theme.plain(13)); log.setLineWrap(true); log.setWrapStyleWord(true);
        final JLabel result = new JLabel(" ");
        result.setForeground(Theme.GOLD); result.setFont(Theme.bold(15)); result.setBorder(BorderFactory.createEmptyBorder(2, 12, 6, 12));
        final JButton pause = mv.makeBtn("Pause", "Pause or go on.", () -> { });
        final JButton skip = mv.makeBtn("Skip to the end", "Show the end of the fight.", () -> { });
        final JButton again = mv.makeBtn("Watch again", "Play the fight from the start.", () -> { });
        final JButton report = mv.makeBtn("Fight report", "The written report of this fight.", () -> { });
        final JButton close = mv.makeBtn("Close", "Close the window.", () -> { });
        final JComboBox<String> speed = new JComboBox<>(new String[]{"Slow", "Normal", "Fast"});
        speed.setSelectedIndex(1);
        stage.onBeat = () -> {
            FightReplay.Beat b = f.beats.get(Math.max(0, stage.beat));
            caption.setText("<html><div style='width:700px'>" + GuiSheets.esc(b.text) + "</div></html>");
            if (log.getText().isEmpty() || !log.getText().endsWith(b.text + "\n")) log.append(b.text + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        };
        stage.onEnd = () -> {
            result.setText(f.resultLine);
            pause.setEnabled(false); skip.setEnabled(false);
            d.getRootPane().setDefaultButton(close);
        };
        pause.addActionListener(e -> { stage.paused = !stage.paused; pause.setText(stage.paused ? "Go on" : "Pause"); });
        skip.addActionListener(e -> stage.skipToEnd());
        again.addActionListener(e -> { log.setText(""); result.setText(" "); pause.setEnabled(true); skip.setEnabled(true); pause.setText("Pause"); stage.paused = false; stage.restart(); });
        report.addActionListener(e -> GuiSheets.infoWindow("Fight report", f.reportText == null ? f.resultLine : f.reportText));
        speed.addActionListener(e -> stage.speed = speed.getSelectedIndex() == 0 ? 0.6 : speed.getSelectedIndex() == 2 ? 2.2 : 1.0);
        close.addActionListener(e -> d.dispose());
        d.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { stage.timer.stop(); if (done != null) done.countDown(); }
        });
        final JCheckBox noAuto = new JCheckBox("Do not open this window by itself", !Settings.fightView);
        noAuto.setBackground(Theme.BG); noAuto.setForeground(Theme.TEXT);
        noAuto.setToolTipText("Checked: fights do not open this window any more (the Fights window can still show them). The same switch is in Settings.");
        noAuto.addActionListener(e -> { Settings.fightView = !noAuto.isSelected(); Settings.save(); GuiExtras.fightViewChanged(); });
        final Runnable syncBox = () -> noAuto.setSelected(!Settings.fightView);
        GuiExtras.fightViewListeners.add(syncBox);
        d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosed(java.awt.event.WindowEvent e) { GuiExtras.fightViewListeners.remove(syncBox); } });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        buttons.setBackground(Theme.BG);
        buttons.add(pause); buttons.add(skip); buttons.add(again); buttons.add(new JLabel("speed")); buttons.add(speed); buttons.add(report); buttons.add(noAuto); buttons.add(close);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.BG);
        JPanel texts = new JPanel(new BorderLayout());
        texts.setBackground(Theme.BG);
        texts.add(caption, BorderLayout.NORTH);
        texts.add(new JScrollPane(log), BorderLayout.CENTER);
        texts.add(result, BorderLayout.SOUTH);
        south.add(texts, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        JLabel head = new JLabel("  " + f.title);
        head.setFont(Theme.serif(18)); head.setForeground(Theme.GOLD); head.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));
        d.add(head, BorderLayout.NORTH);
        d.add(stage, BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.setSize(900, 660);
        d.setLocationRelativeTo(mv);
        Ui.prepare(d);
        d.setVisible(true);
        stage.restart();
    }

    // ------------------------------------------------------------------ the Fights window and the tournament result
    /** The last fights of the person who plays, each with a Watch button; a switch for the live window. */
    static void listWindow(final MapViewer mv) {
        GuiWindows.window(mv, "fights", "Fights", 760, 480, 2500, (d, refreshers) -> {
            final DefaultTableModel m = GuiWindows.model();
            final JTable t = GuiWindows.table(m);
            final List<FightReplay> shown = new ArrayList<>();
            final JCheckBox live = new JCheckBox("Show each fight live, as it ends");
            live.setBackground(GuiWindows.BG); live.setForeground(GuiWindows.FG);
            live.setToolTipText("On: the fight window opens when a fight you were in is over, and the game waits until you close it. Off: watch any fight from this list afterwards.");
            final boolean[] setting = {false};
            live.addActionListener(e -> { if (!setting[0]) mv.queueInput("settings fightview " + (live.isSelected() ? "on" : "off")); });
            refreshers.add(() -> {
                setting[0] = true; live.setSelected(Settings.fightView); setting[0] = false;
                List<FightReplay> list;
                synchronized (FightReport.replays) { list = new ArrayList<>(FightReport.replays); }
                if (list.size() == shown.size() && !list.isEmpty() && shown.get(shown.size() - 1) == list.get(list.size() - 1)) return;
                shown.clear(); shown.addAll(list);
                List<Object[]> rows = new ArrayList<>();
                for (int i = list.size() - 1; i >= 0; i--) {
                    FightReplay f = list.get(i);
                    rows.add(new Object[]{"Y" + (f.week / 52) + " wk " + (f.week % 52 + 1), f.nameA + " (" + Elements.name(f.elemA) + ")", f.nameB + " (" + Elements.name(f.elemB) + ")", f.winA + "%", f.resultLine});
                }
                Collections_reverse(shown);
                m.setDataVector(rows.toArray(new Object[0][]), new String[]{"When", "First fighter", "Second fighter", "Odds for the first", "Result"});
                t.getColumnModel().getColumn(0).setPreferredWidth(90); t.getColumnModel().getColumn(4).setPreferredWidth(260);
            });
            JPanel south = GuiWindows.flow(FlowLayout.LEFT);
            south.add(mv.makeBtn("Watch the fight", "Play the selected fight with the stick figures.", () -> { int r = t.getSelectedRow(); if (r >= 0 && r < shown.size()) replay(mv, shown.get(r)); }));
            south.add(mv.makeBtn("Fight report", "The written report of the selected fight.", () -> { int r = t.getSelectedRow(); if (r >= 0 && r < shown.size()) GuiSheets.infoWindow("Fight report", shown.get(r).reportText); }));
            south.add(live);
            t.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) { int r = t.getSelectedRow(); if (r >= 0 && r < shown.size()) replay(mv, shown.get(r)); } } });
            JPanel root = new JPanel(new BorderLayout(4, 4));
            root.setBackground(GuiWindows.BG);
            root.add(new JScrollPane(t), BorderLayout.CENTER);
            root.add(south, BorderLayout.SOUTH);
            return root;
        });
    }

    private static void Collections_reverse(List<FightReplay> l) { java.util.Collections.reverse(l); }

    /** The tournament result as a window: the text, and a Watch button for each of the person's bouts (the game goes on). */
    static void tournamentResult(final String text, final List<FightReplay> bouts) { tournamentResult("Tournament result", text, bouts); }

    /** The same with a title of its own (the Convocation, a spectated tournament). */
    static void tournamentResult(final String title, final String text, final List<FightReplay> bouts) { tournamentResult(title, text, bouts, 0); }

    /** The same, with the piece of music of the tournament (`EventMusic.ARENA` or `GARDEN`; 0: none) playing while it is open. */
    static void tournamentResult(final String title, final String text, final List<FightReplay> bouts, final int music) {
        final MapViewer mv = GameDriver.mapViewer;
        if (mv == null) return;
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog(mv, title, false);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            if (music > 0) { EventMusic.hold(music); d.addWindowListener(new java.awt.event.WindowAdapter() { @Override public void windowClosed(java.awt.event.WindowEvent e) { EventMusic.release(); } }); }
            d.getContentPane().setBackground(Theme.BG);
            JTextArea ta = new JTextArea(text);
            ta.setEditable(false); ta.setBackground(Theme.FIELD); ta.setForeground(Theme.TEXT); ta.setFont(Theme.plain(15)); ta.setLineWrap(true); ta.setWrapStyleWord(true);
            ta.setCaretPosition(0);
            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            south.setBackground(Theme.BG);
            south.add(new JLabel("Watch a bout:"));
            for (int i = 0; i < bouts.size(); i++) {
                final FightReplay f = bouts.get(i);
                south.add(mv.makeBtn("Bout " + (i + 1), "Play this bout with the stick figures: " + f.nameA + " against " + f.nameB + ".", () -> replay(mv, f)));
            }
            if (bouts.isEmpty()) south.add(new JLabel("(you did not fight a bout)"));
            JButton close = mv.makeBtn("Close", () -> d.dispose());
            south.add(close);
            d.add(new JScrollPane(ta), BorderLayout.CENTER);
            d.add(south, BorderLayout.SOUTH);
            d.setSize(640, 460);
            d.setLocationRelativeTo(mv);
            Ui.prepare(d);
            d.setVisible(true);
        });
    }
}
