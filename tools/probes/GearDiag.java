package Game;

import java.io.*;
import java.util.*;

/** One snapshot of the gear shortage after N game years: how many adults have no weapon or clothes, whether they know a shop city and can pay, what the shops and city pools hold,
 *  and how many pieces the Blacksmiths and Tailors carry unsold.  java -Xss512m -cp <out>;lib/flatlaf-3.7.2.jar Game.GearDiag 25 */
public class GearDiag {
    static class Feed extends InputStream {
        private final byte[] data = "n\nw\n".getBytes();
        private int pos = 0;
        @Override public int read() throws IOException { byte[] b = new byte[1]; int n = read(b, 0, 1); return n < 0 ? -1 : (b[0] & 0xff); }
        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (pos >= data.length) { try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException e) { return -1; } }
            int n = Math.min(len, data.length - pos); System.arraycopy(data, pos, b, off, n); pos += n; return n;
        }
    }

    public static void main(String[] a) throws Exception {
        int years = Integer.parseInt(a[0]);
        PrintStream real = System.out;
        System.setIn(new Feed());
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        while (GameDriver.time / 52 < years) Thread.sleep(200);
        int[] adults = new int[11], unarmed = new int[11], unclothed = new int[11], knowsShopCity = new int[11], poor = new int[11];
        long[] wealth = new long[11];
        for (Character c : new ArrayList<>(Map.characters)) {
            if (c == null || c.dead || !c.born || c.player != 0 || c.age < 16 || c.majorLvl < 1 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            int m = Math.min(10, c.majorLvl);
            adults[m]++;
            if (c.equippedClothing == null) unclothed[m]++;
            if (c.equippedWeapon != null) continue;
            unarmed[m]++;
            boolean shopCity = false;
            for (Area ar : c.knownLocations) if (ar != null && ar.city && (!ar.shops.isEmpty() || (ar.backpack != null && !ar.backpack.isEmpty()))) { shopCity = true; break; }
            if (shopCity) knowsShopCity[m]++;
            long money = c.ss + c.sc * 1_000_000L;
            if (money < 1000) poor[m]++;
            wealth[m] += money;
        }
        long shopW = 0, shopC = 0, poolW = 0, poolC = 0; int shops = 0, shopsNoWeapon = 0, cities = 0;
        for (Area ar : new ArrayList<>(Map.createdAreas)) {
            if (ar == null || !ar.city) continue;
            cities++;
            for (Shop s : ar.shops) { shops++; int w = 0; for (Item it : s.inventory) { if (it.itemType == Item.ItemType.Weapon) { shopW++; w++; } if (it.itemType == Item.ItemType.Clothing) shopC++; } if (w == 0) shopsNoWeapon++; }
            if (ar.backpack != null) for (Item it : ar.backpack) { if (it.itemType == Item.ItemType.Weapon) poolW++; if (it.itemType == Item.ItemType.Clothing) poolC++; }
        }
        real.println("year " + years + "  cities=" + cities + " shops=" + shops + " weaponsOnShelves=" + shopW + " clothingOnShelves=" + shopC + " shopsWithNoWeapon=" + shopsNoWeapon + "  city pools: weapons=" + poolW + " clothing=" + poolC);
        real.println("majorLvl: adults / unarmed / unclothed / unarmedKnowShopCity / unarmedPoor(<1000ss) / avgMoneyUnarmed");
        int tA = 0, tU = 0, tC = 0;
        for (int m = 1; m <= 10; m++) if (adults[m] > 0) { tA += adults[m]; tU += unarmed[m]; tC += unclothed[m]; real.println("  " + m + ": " + adults[m] + " / " + unarmed[m] + " / " + unclothed[m] + " / " + knowsShopCity[m] + " / " + poor[m] + " / " + (unarmed[m] == 0 ? 0 : wealth[m] / unarmed[m])); }
        real.println("adults without a weapon " + (tA == 0 ? 0 : 100 * tU / tA) + "%, without clothes " + (tA == 0 ? 0 : 100 * tC / tA) + "%");
        int smiths = 0, tailors = 0, oldSmiths = 0, smithsWithMetal = 0; long smithW = 0, tailorC = 0, oldCrafted = 0, metalHeld = 0;
        for (Character c : new ArrayList<>(Map.characters)) {
            if (c == null || c.dead || !c.born) continue;
            int w = 0, cl = 0; for (Item it : c.backpack) { if (it.itemType == Item.ItemType.Weapon && it != c.equippedWeapon) w++; if (it.itemType == Item.ItemType.Clothing && it != c.equippedClothing) cl++; }
            if (c.trait == Character.Traits.Blacksmith) {
                smiths++; smithW += w;
                long metal = 0; for (Item it : c.materialBag) if (it.itemType == Item.ItemType.Metal) metal += it.quantity;
                metalHeld += metal; if (metal >= Character.CRAFT_COST) smithsWithMetal++;
                if (c.age >= 30) { oldSmiths++; oldCrafted += c.itemsCrafted; }
            }
            if (c.trait == Character.Traits.Tailor) { tailors++; tailorC += cl; }
        }
        real.println("blacksmiths=" + smiths + " carry " + smithW + " spare weapons (" + (smiths == 0 ? 0 : smithW / smiths) + " each); tailors=" + tailors + " carry " + tailorC + " spare pieces");
        real.println("blacksmiths aged 30+: " + oldSmiths + ", each made " + (oldSmiths == 0 ? 0 : oldCrafted / oldSmiths) + " pieces so far; " + smithsWithMetal + " of " + smiths + " hold enough metal for one (avg " + (smiths == 0 ? 0 : metalHeld / smiths) + ")");
        System.exit(0);
    }
}
