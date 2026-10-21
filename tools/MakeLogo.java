package Game;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/** Writes the game's emblem (ArtMore.logo) to assets/logo/: logo-16..512.png and logo.ico (PNG-compressed icon images, 16-256 px) for the .exe.
 *  Run from the repo root after compiling the sources:  java -cp <classes> Game.MakeLogo */
public class MakeLogo {
    public static void main(String[] a) throws Exception {
        File dir = new File(a.length > 0 ? a[0] : "assets/logo");
        dir.mkdirs();
        int[] pngSizes = {16, 24, 32, 48, 64, 128, 256, 512};
        for (int s : pngSizes) ImageIO.write(ArtMore.logo(s), "png", new File(dir, "logo-" + s + ".png"));
        int[] icoSizes = {16, 24, 32, 48, 64, 128, 256};
        byte[][] data = new byte[icoSizes.length][];
        for (int i = 0; i < icoSizes.length; i++) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ImageIO.write(ArtMore.logo(icoSizes[i]), "png", bo);
            data[i] = bo.toByteArray();
        }
        try (DataOutputStream o = new DataOutputStream(new FileOutputStream(new File(dir, "logo.ico")))) {
            o.write(le16(0)); o.write(le16(1)); o.write(le16(icoSizes.length));
            int offset = 6 + 16 * icoSizes.length;
            for (int i = 0; i < icoSizes.length; i++) {
                int s = icoSizes[i];
                o.writeByte(s >= 256 ? 0 : s); o.writeByte(s >= 256 ? 0 : s); o.writeByte(0); o.writeByte(0);
                o.write(le16(1)); o.write(le16(32)); o.write(le32(data[i].length)); o.write(le32(offset));
                offset += data[i].length;
            }
            for (byte[] d : data) o.write(d);
        }
        System.out.println("wrote " + dir.getPath() + "/logo-*.png and logo.ico");
    }

    private static byte[] le16(int v) { return new byte[]{(byte) v, (byte) (v >> 8)}; }
    private static byte[] le32(int v) { return new byte[]{(byte) v, (byte) (v >> 8), (byte) (v >> 16), (byte) (v >> 24)}; }
}
