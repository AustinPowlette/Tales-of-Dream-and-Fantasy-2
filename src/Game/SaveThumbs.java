package Game;

import javax.imageio.ImageIO;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

/** A small picture of the game window next to each save (`saves/<slot>.png`), shown in the Saves window. GUI mode only; never fails a save. */
final class SaveThumbs {
    private SaveThumbs() { }

    static void capture(String slot) {
        if (!GameDriver.guiEnabled || GameDriver.mapViewer == null || slot == null) return;
        try {
            final BufferedImage[] small = {null};
            GameDriver.edtWait(() -> {
                Container c = GameDriver.mapViewer.getContentPane();
                if (c.getWidth() < 50 || c.getHeight() < 50) return;
                BufferedImage full = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = full.createGraphics();
                c.paint(g);
                g.dispose();
                int w = 360, h = Math.max(1, w * full.getHeight() / full.getWidth());
                BufferedImage s = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = s.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(full, 0, 0, w, h, null);
                g2.dispose();
                small[0] = s;
            });
            if (small[0] != null) { SaveManager.dir.mkdirs(); ImageIO.write(small[0], "png", new File(SaveManager.dir, slot + ".png")); }
        } catch (Throwable ignored) { /* a picture is a bonus */ }
    }
}
