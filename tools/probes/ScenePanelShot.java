package Game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.swing.*;

/** 2026-10-21: paints the left panel of a scene window (the picture of the place and the portraits of the speakers) to a PNG, headless, no world needed. `java Game.ScenePanelShot <dir>`. */
public class ScenePanelShot {
    static void layout(Container c) { c.doLayout(); for (Component k : c.getComponents()) if (k instanceof Container) layout((Container) k); }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File dir = new File(a.length > 0 ? a[0] : "artshots"); dir.mkdirs();
        GuiScenes.Scene s = new GuiScenes.Scene("Story", "Seven Hems", Arrays.asList("* A shop.", "@Orla| Stand there."), null, "Continue", null);
        s.artKey = "quarter";
        s.faceNames = Arrays.asList("Orla", "Thimble");
        s.faceImages = new ArrayList<BufferedImage>();
        for (int i = 0; i < 2; i++) { BufferedImage f = new BufferedImage(84, 84, BufferedImage.TYPE_INT_RGB); Graphics2D g = f.createGraphics(); g.setColor(new Color(90 + i * 60, 120, 140)); g.fillRect(0, 0, 84, 84); g.dispose(); s.faceImages.add(f); }
        JPanel p = GuiScenes.artPanel(s);
        p.setSize(380, 330); p.doLayout();
        layout(p);
        BufferedImage im = new BufferedImage(380, 330, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics(); p.print(g); g.dispose();
        javax.imageio.ImageIO.write(im, "png", new File(dir, "scene_panel.png"));
        System.out.println("ok");
    }
}
