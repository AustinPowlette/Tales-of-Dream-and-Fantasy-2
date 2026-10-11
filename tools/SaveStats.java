package Game;

import java.io.*;
import java.util.*;

/** Reads a save file WITHOUT applying it and prints what fills it: how many characters of each kind, the average size of each list a character carries (relationships, quests,
 *  items, techniques, known places, family), the most common quest and item names, how many characters are reachable only through family links (the dead), and what the
 *  relationship values look like. Used to see where the memory and the file size of an old world go (2026-10-10).
 *    java -Xmx14g -Xss512m -cp out Game.SaveStats saves/auto-1.dat */
public class SaveStats {
    static class Acc {
        String name; int n;
        long rel, relWeak, tq, sq, bp, mat, pill, arr, tech, known, sib, chi, goods, past, beastIds, hist, ownedShops;
        Acc(String n) { name = n; }
        void add(Character c) {
            n++;
            if (c.relationshipList != null && c.relationshipList.relationships != null) { rel += c.relationshipList.relationships.size(); for (Relationship r : c.relationshipList.relationships) if (Math.abs(r.value) <= 1) relWeak++; }
            tq += c.targetQuests == null ? 0 : c.targetQuests.size(); sq += c.spotQuests == null ? 0 : c.spotQuests.size();
            bp += c.backpack == null ? 0 : c.backpack.size(); mat += c.materialBag == null ? 0 : c.materialBag.size(); pill += c.pillBag == null ? 0 : c.pillBag.size(); arr += c.arrayBag == null ? 0 : c.arrayBag.size();
            tech += c.techniqueList == null ? 0 : c.techniqueList.size(); known += c.knownLocations == null ? 0 : c.knownLocations.size();
            sib += c.siblings == null ? 0 : c.siblings.size(); chi += c.children == null ? 0 : c.children.size();
            goods += c.goods == null ? 0 : c.goods.size(); past += c.pastLives == null ? 0 : c.pastLives.size(); ownedShops += c.ownedShops == null ? 0 : c.ownedShops.size();
        }
        String line() {
            if (n == 0) return String.format("%-22s %9d", name, 0);
            return String.format("%-22s %9d  rel %5.1f (weak %4.1f)  targetQ %4.1f spotQ %4.1f  bag %4.1f mat %4.1f pill %4.1f arr %3.1f  tech %4.1f  known %5.1f  sib %3.1f chi %3.1f goods %3.1f",
                    name, n, rel / (double) n, relWeak / (double) n, tq / (double) n, sq / (double) n, bp / (double) n, mat / (double) n, pill / (double) n, arr / (double) n, tech / (double) n, known / (double) n, sib / (double) n, chi / (double) n, goods / (double) n);
        }
    }

    public static void main(String[] a) throws Exception {
        File f = new File(a.length > 0 ? a[0] : "saves/auto-1.dat");
        System.out.println("file: " + f + "  " + (f.length() >> 20) + " MB");
        long t0 = System.currentTimeMillis();
        SaveData d;
        try (ObjectInputStream in = new ObjectInputStream(SaveManager.openSave(f))) { d = (SaveData) in.readObject(); }
        System.out.println("read in " + (System.currentTimeMillis() - t0) / 1000 + " s; year " + d.time / 52 + ", population " + d.population + ", list " + d.characters.size());
        System.out.println("live heap after loading: " + used() + " MB");
        report(d);
        if (a.length > 1 && a[1].equals("slim")) {
            Map.characters = d.characters; Map.population = d.population; Map.createdAreas = d.createdAreas;
            long t1 = System.currentTimeMillis();
            Slim.yearly();
            int n = 0;
            for (Character c : d.characters) if (c != null && !c.dead) { Slim.trim(c); n++; }
            System.out.println("\n=== after Slim.yearly + Slim.trim for " + n + " characters (" + (System.currentTimeMillis() - t1) + " ms) ===");
            System.out.println("live heap after slimming: " + used() + " MB");
            report(d);
            long t2 = System.currentTimeMillis();
            File out = new File(a.length > 2 ? a[2] : "slim_test.dat");
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new java.util.zip.GZIPOutputStream(new FileOutputStream(out), 1 << 16) { { def.setLevel(java.util.zip.Deflater.BEST_SPEED); } }, 1 << 16))) { oos.writeObject(d); }
            System.out.println("saved again, slimmed and compressed: " + (out.length() >> 20) + " MB (was " + (f.length() >> 20) + " MB) in " + (System.currentTimeMillis() - t2) / 1000 + " s");
            out.delete();
        }
    }

    static void sv(java.util.Map<String, IdentityHashMap<String, Boolean>> m, String k, String v) { if (v == null) return; m.computeIfAbsent(k, x -> new IdentityHashMap<>()).put(v, Boolean.TRUE); }

    static long bagItems(Character c) { long n = 0; for (List<Item> b : Arrays.asList(c.backpack, c.materialBag, c.pillBag, c.arrayBag, c.goods)) if (b != null) n += b.size(); return n; }

    static long used() { for (int i = 0; i < 3; i++) System.gc(); Runtime rt = Runtime.getRuntime(); return (rt.totalMemory() - rt.freeMemory()) >> 20; }

    static void report(SaveData d) {
        Acc cult = new Acc("cultivators"), beastW = new Acc("wild beasts/monsters"), beastT = new Acc("tamed beasts"), human = new Acc("people who play");
        IdentityHashMap<Character, Boolean> seen = new IdentityHashMap<>();
        ArrayDeque<Character> q = new ArrayDeque<>();
        int nulls = 0;
        TreeMap<String, Integer> questNames = new TreeMap<>(), itemNames = new TreeMap<>();
        long[] relHist = new long[13];
        long items = 0;
        for (Character c : d.characters) {
            if (c == null) { nulls++; continue; }
            seen.put(c, Boolean.TRUE); q.add(c);
            boolean beast = c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster;
            (c.player > 0 ? human : beast ? (c.wasTamed ? beastT : beastW) : cult).add(c);
            for (Quest qu : c.targetQuests) questNames.merge("T:" + qu.name, 1, Integer::sum);
            for (Quest qu : c.spotQuests) questNames.merge("S:" + qu.name, 1, Integer::sum);
            for (List<Item> bag : Arrays.asList(c.backpack, c.materialBag, c.pillBag, c.arrayBag, c.goods)) if (bag != null) for (Item it : bag) { items++; itemNames.merge(it.itemType + ":" + it.name, 1, Integer::sum); }
            if (c.relationshipList != null) for (Relationship r : c.relationshipList.relationships) relHist[Math.max(0, Math.min(12, r.value + 6))]++;
        }
        int dead = 0, husks = 0; long deadItems = 0, equipped = 0;
        for (Character c : d.characters) if (c != null && (c.equippedWeapon != null || c.equippedClothing != null)) equipped += (c.equippedWeapon != null ? 1 : 0) + (c.equippedClothing != null ? 1 : 0);
        while (!q.isEmpty()) {
            Character c = q.poll();
            List<Character> next = new ArrayList<>();
            if (c.father != null) next.add(c.father); if (c.mother != null) next.add(c.mother); if (c.spouse != null) next.add(c.spouse); if (c.reincarnationKiller != null) next.add(c.reincarnationKiller);
            if (c.siblings != null) next.addAll(c.siblings); if (c.children != null) next.addAll(c.children);
            for (Character n : next) if (n != null && seen.put(n, Boolean.TRUE) == null) { dead++; if (n.husked) husks++; deadItems += bagItems(n); q.add(n); }
        }
        System.out.println("items held by the dead (reachable only through kin): " + deadItems + ", husks: " + husks + ", equipped items of the living: " + equipped);
        System.out.println("slots in the list: " + d.characters.size() + " (" + nulls + " empty), characters reachable only through family links: " + dead);
        for (Acc x : new Acc[]{cult, human, beastW, beastT}) System.out.println(x.line());
        System.out.println("run statistics: " + (d.runStats == null ? "none" : d.runStats.lives.size() + " life records, " + d.runStats.reincarnations + " reincarnations counted") + "; ");
        System.out.println("items in bags: " + items + ", chronicle entries: " + (d.chronicle == null ? 0 : d.chronicle.size()) + ", areas: " + d.createdAreas.size() + ", bio samples: " + (d.bio == null ? 0 : d.bio.size()));
        StringBuilder sb = new StringBuilder("relationship values (<=-6 ... >=6): ");
        for (int i = 0; i < relHist.length; i++) sb.append(relHist[i]).append(i < relHist.length - 1 ? " " : "");
        System.out.println(sb);
        {   // which strings are separate objects (identity), by where they hang
            java.util.Map<String, IdentityHashMap<String, Boolean>> byField = new TreeMap<>();
            for (Character c : d.characters) {
                if (c == null) continue;
                sv(byField, "char.name", c.name); sv(byField, "char.lastName", c.lastName); sv(byField, "char.gender", c.gender); sv(byField, "char.affiliation", c.affiliation);
                sv(byField, "char.repTitle", c.repTitle); sv(byField, "char.deathCause", c.deathCause); sv(byField, "char.sectMission", c.sectMission); sv(byField, "char.originalName", c.originalName);
                if (c.martialTechnique != null) { sv(byField, "tech.name", c.martialTechnique.name); sv(byField, "tech.maker", c.martialTechnique.maker); }
                if (c.movementTechnique != null) { sv(byField, "tech.name", c.movementTechnique.name); sv(byField, "tech.maker", c.movementTechnique.maker); }
                for (Quest qu : c.targetQuests) { sv(byField, "quest.name", qu.name); sv(byField, "quest.label", qu.label); }
                for (Quest qu : c.spotQuests) { sv(byField, "quest.name", qu.name); sv(byField, "quest.label", qu.label); }
                for (List<Item> bag : Arrays.asList(c.backpack, c.materialBag, c.pillBag, c.arrayBag, c.goods)) if (bag != null) for (Item it : bag) { sv(byField, "item.name", it.name); sv(byField, "item.maker", it.maker); sv(byField, "item.brand", it.brand); sv(byField, "item.enchantment", it.enchantment); }
            }
            StringBuilder sb2 = new StringBuilder("distinct String objects by field: ");
            for (java.util.Map.Entry<String, IdentityHashMap<String, Boolean>> e : byField.entrySet()) sb2.append(e.getKey()).append('=').append(e.getValue().size()).append("  ");
            System.out.println(sb2);
        }
        System.out.println("-- quest names (count, top 25) --");
        top(questNames, 25);
        System.out.println("-- item names (count, top 15), distinct " + itemNames.size() + " --");
        top(itemNames, 15);
        System.out.println("-- area stock --");
        long ab = 0, am = 0, ap = 0, sh = 0, shi = 0, lib = 0, ch = 0;
        for (Area ar : d.createdAreas) { ab += ar.backpack == null ? 0 : ar.backpack.size(); am += ar.materialBag == null ? 0 : ar.materialBag.size(); ap += ar.pillBag == null ? 0 : ar.pillBag.size(); sh += ar.shops == null ? 0 : ar.shops.size();
            if (ar.shops != null) for (Shop s : ar.shops) shi += s.inventory == null ? 0 : s.inventory.size(); lib += ar.libraries == null ? 0 : ar.libraries.size(); ch += ar.characters == null ? 0 : ar.characters.size(); }
        System.out.println("area bags " + ab + ", materials " + am + ", pills " + ap + ", shops " + sh + " (shelf items " + shi + "), libraries " + lib + ", characters listed in tiles " + ch);
    }

    static void top(TreeMap<String, Integer> m, int n) {
        List<java.util.Map.Entry<String, Integer>> l = new ArrayList<>(m.entrySet());
        l.sort((x, y) -> y.getValue() - x.getValue());
        for (int i = 0; i < Math.min(n, l.size()); i++) System.out.println(String.format("  %9d  %s", l.get(i).getValue(), l.get(i).getKey()));
    }
}
