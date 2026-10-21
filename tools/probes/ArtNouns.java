package Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

/** 2026-10-21: renders the painted pictures of chosen place nouns (ArtGen) side by side, three seeds each: `java Game.ArtNouns <dir> noun[:biome] ...` (no world needed). */
public class ArtNouns {
    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]); dir.mkdirs();
        int n = a.length - 1, tw = 384, th = 216;
        BufferedImage sheet = new BufferedImage(3 * tw, n * (th + 16), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics(); g.setColor(new Color(30, 30, 34)); g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        for (int i = 0; i < n; i++) {
            String[] nb = a[i + 1].split(":");
            for (int s = 0; s < 3; s++) {
                BufferedImage im = ArtGen.render(nb[0], nb.length > 1 ? nb[1] : "", 11L + s * 17, tw, th, 1.0);
                g.drawImage(im, s * tw, i * (th + 16) + 16, null);
            }
            g.setColor(Color.WHITE); g.drawString(a[i + 1], 4, i * (th + 16) + 12);
        }
        g.dispose();
        javax.imageio.ImageIO.write(sheet, "png", new File(dir, "nouns.png"));
        System.out.println("ok");
    }
}
