package Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

/** 2026-10-20: renders every picture of `StoryArt` (the places of the lore and of the stories) to PNG and one contact sheet. `java Game.ArtShots <dir>` (default `artshots`). No world is needed. */
public class ArtShots {
    public static void main(String[] a) throws Exception {
        File dir = new File(a.length > 0 ? a[0] : "artshots");
        dir.mkdirs();
        String[] keys = StoryArt.KEYS;
        int cols = 5, tw = 320, th = 180, rows = (keys.length + cols - 1) / cols;
        BufferedImage sheet = new BufferedImage(cols * tw, rows * (th + 18), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(30, 30, 34)); g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < keys.length; i++) {
            BufferedImage im = StoryArt.picture(keys[i]);
            javax.imageio.ImageIO.write(im, "png", new File(dir, keys[i] + ".png"));
            int x = (i % cols) * tw, y = (i / cols) * (th + 18);
            g.drawImage(im, x, y + 18, tw, th, null);
            g.setColor(Color.WHITE); g.drawString(keys[i], x + 4, y + 13);
        }
        g.dispose();
        javax.imageio.ImageIO.write(sheet, "png", new File(dir, "sheet.png"));
        System.out.println("wrote " + keys.length + " pictures in " + (System.currentTimeMillis() - t0) + " ms to " + dir.getAbsolutePath());
    }
}
