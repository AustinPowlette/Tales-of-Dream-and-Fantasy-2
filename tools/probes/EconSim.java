package Game;

import java.io.*;
import java.util.*;

/** Long-run measurement of the 2026-10-03 economy (Heaven-Sent treasures, auctions, Potential Pills, Blood, libraries, shop and pool sizes). Spectates a shrunken world for N game years
 *  and prints one CSV row per year (also written to econ_years.csv). Same set-up as PlaytestProbe:  java -Xss512m -cp <out>;lib/flatlaf-3.7.2.jar Game.EconSim 60 */
public class EconSim {
    static class Feed extends InputStream {
        private final byte[] data = "n\nw\n".getBytes();
        private int pos = 0;
        @Override public int read() throws IOException { byte[] b = new byte[1]; int n = read(b, 0, 1); return n < 0 ? -1 : (b[0] & 0xff); }
        @Override public int read(byte[] b, int off, int len) throws IOException {
            if (pos >= data.length) { try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException e) { return -1; } }
            int n = Math.min(len, data.length - pos);
            System.arraycopy(data, pos, b, off, n);
            pos += n;
            return n;
        }
    }

    static volatile Throwable gameError = null;

    public static void main(String[] a) throws Exception {
        final int years = a.length > 0 ? Integer.parseInt(a[0]) : 60;
        PrintStream real = System.out;
        System.setIn(new Feed());
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        System.setOut(new PrintStream(sink, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { gameError = e; e.printStackTrace(real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        Thread.sleep(3000);
        String head = "year,pop,cultivators,avgPotential,pctPotential100,potPillsHeld,potPillsOnBlock,lotsWaiting,auctionsHeld,lotsSold,lotsUnsold,potPillsSold,treasuresSpawned,treasuresClaimed,treasuresUnclaimed,bloodStock,pillsRefined,libraryBooks,avgTechniques,shopItems,poolItems,sectCount";
        PrintWriter csv = new PrintWriter(new FileWriter("econ_years.csv"));
        csv.println(head); real.println(head);
        int lastYear = -1, exceptions = 0;
        while (true) {
            int yr = GameDriver.time / 52;
            if (gameError != null) { real.println("GAME ERROR " + gameError); break; }
            if (yr == lastYear) { Thread.sleep(200); continue; }
            lastYear = yr;
            if (yr > years) break;
            int n = Map.characters.size(), cult = 0, p100 = 0, techs = 0; long potSum = 0, potPills = 0, blood = 0;
            for (int i = 0; i < n; i++) {
                Character c = Map.characters.get(i);
                if (c == null || c.dead || !c.born || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster || c.age < 8) continue;
                try {
                    cult++; potSum += c.potential; if (c.potential >= 100) p100++;
                    techs += c.techniqueList == null ? 0 : c.techniqueList.size();
                    for (Item it : c.pillBag) if (it.pillType == Item.PillType.Potential) potPills += it.quantity;
                    for (Item it : c.materialBag) if (it.itemType == Item.ItemType.Blood) blood += it.quantity;
                } catch (RuntimeException e) { exceptions++; }
            }
            long onBlock = 0, lots = 0, books = 0, shopItems = 0, pool = 0; int unclaimed = 0, sects = 0;
            for (Area ar : new ArrayList<>(Map.createdAreas)) {
                if (ar == null) continue;
                try {
                    if (ar.hasHeaven()) unclaimed++;
                    if (ar.sect != null) sects++;
                    if (ar.auctionBag != null) for (Item it : ar.auctionBag) { lots++; if (it.pillType == Item.PillType.Potential) onBlock += it.quantity; }
                    if (ar.pillBag != null) for (Item it : ar.pillBag) if (it.pillType == Item.PillType.Potential) onBlock += it.quantity;
                    for (Library l : ar.libraries) books += l.techniques.size();
                    for (Shop s : ar.shops) shopItems += s.inventory.size();
                    if (ar.city && ar.backpack != null) pool += ar.backpack.size();
                } catch (RuntimeException e) { exceptions++; }
            }
            String row = String.join(",", String.valueOf(yr), String.valueOf(Map.population), String.valueOf(cult), String.format("%.1f", cult == 0 ? 0 : (double) potSum / cult), String.format("%.1f", cult == 0 ? 0 : 100.0 * p100 / cult),
                    String.valueOf(potPills), String.valueOf(onBlock), String.valueOf(lots), String.valueOf(Auctions.stHeld), String.valueOf(Auctions.stSold), String.valueOf(Auctions.stUnsold), String.valueOf(Auctions.stPotentialPills),
                    String.valueOf(Treasure.stSpawned), String.valueOf(Treasure.stClaimed), String.valueOf(unclaimed), String.valueOf(blood), String.valueOf(Character.stRefined), String.valueOf(books),
                    String.format("%.2f", cult == 0 ? 0 : (double) techs / cult), String.valueOf(shopItems), String.valueOf(pool), String.valueOf(sects));
            csv.println(row); csv.flush(); real.println(row);
        }
        csv.close();
        String out = sink.toString();
        int ex = 0; for (String line : out.split("\n")) if (line.contains("Exception") || line.contains("\tat Game.")) ex++;
        real.println("[E] exceptionLines=" + ex + " sampleExceptions=" + exceptions + " gameError=" + gameError);
        System.exit(0);
    }
}
