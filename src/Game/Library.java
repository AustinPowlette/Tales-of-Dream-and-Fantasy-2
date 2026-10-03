package Game;

import java.util.ArrayList;

// TODO: Flesh out further — Family technique inheritance (at birth / major realm milestone),
//       Sect Contribution Point earning (missions, gifts from superiors), mastery levelling during
//       cultivation/events, technique rarity tiers, and per-technique access restrictions.
public class Library implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    Character owner;        // null for sect/city libraries
    Area location;
    Organization sect;      // null for city/personal libraries
    ArrayList<Technique> techniques = new ArrayList<>();
    boolean isSectLibrary;
    String name = "";

    // City or public library
    public Library(Area location) {
        this.location = location;
        this.isSectLibrary = false;
        this.name = Naming.library(null);
    }

    // Sect library (techniques gated behind sect contribution points)
    public Library(Organization sect) {
        this.sect = sect;
        this.location = sect.location;
        this.isSectLibrary = true;
        this.name = Naming.library(sect.name);
    }

    // Price to buy a copy of a technique at a city library
    public long buyPrice(Technique t) {
        return Math.max(1000L, (long) t.potential * t.potential * 1000L);
    }

    /** Everyone starts with a potential-3 default technique, so nothing below this potential is worth a library's shelf. */
    public static final int MIN_SELL_POTENTIAL = 4;

    /** The fee for selling a technique: 20% of the old price (a library keeps one book per type and potential, so a sale is mostly a swap). */
    public static long sellPrice(Technique t) { return Math.max(100L, (long) t.potential * t.potential * 100L); }

    public static boolean sellable(Technique t) { return t != null && t.potential >= MIN_SELL_POTENTIAL; }

    /** Sells a technique to this library: the seller is paid the 20% fee and LOSES the technique (so the world does not fill with copies); the library keeps one book for each type and potential, and a new one replaces the old,
     *  so fresh names keep arriving and not everybody ends up with the same technique. Paid by the library's owner, else the city lord, else the city treasury; if none can afford it nothing happens. */
    public boolean sellTechnique(Character seller, Technique technique) {
        if (!sellable(technique)) return false;
        long price = sellPrice(technique);
        if (owner != null && owner.canAfford(price)) owner.deductSs(price);
        else if (owner == null && location != null && location.cityOwner != null && location.cityOwner.canAfford(price)) location.cityOwner.deductSs(price);
        else if (owner == null && location != null && location.cityTreasury >= price) location.cityTreasury -= price;
        else return false;
        seller.ss += price;
        seller.convertCurrency();
        seller.techniqueList.remove(technique);
        shelve(new Technique(technique));
        seller.equip();
        return true;
    }

    /** One book for each (type, potential): a new one takes the old one's place. */
    public void shelve(Technique copy) {
        for (int i = 0; i < techniques.size(); i++) {
            Technique t = techniques.get(i);
            if (t != null && t.type == copy.type && t.potential == copy.potential) { techniques.set(i, copy); return; }
        }
        techniques.add(copy);
    }

    /** Old shelves (before the one-book rule): keeps the newest book of each type and potential, and drops books nobody should buy. */
    public void compact() {
        ArrayList<Technique> kept = new ArrayList<>();
        for (int i = techniques.size() - 1; i >= 0; i--) {
            Technique t = techniques.get(i);
            if (t == null || !sellable(t)) continue;
            boolean dup = false;
            for (Technique k : kept) if (k.type == t.type && k.potential == t.potential) { dup = true; break; }
            if (!dup) kept.add(t);
        }
        java.util.Collections.reverse(kept);
        techniques = kept;
    }

    // Learn a technique from a city library (pays ss, gets a copy)
    public boolean learnTechnique(Character buyer, Technique technique) {
        if (!techniques.contains(technique)) return false;
        long price = buyPrice(technique);
        if (!buyer.canAfford(price)) return false;
        buyer.deductSs(price);
        if (location != null) location.receivePayment(price); // living lord, else the city treasury: the money is never destroyed
        Technique learned = new Technique(technique);
        learned.lvl = Math.max(1, technique.lvl / 5); // transcribed texts preserve some mastery
        buyer.techniqueList.add(learned);
        buyer.equip();
        return true;
    }

    // Learn a sect-exclusive technique (spends sectPoints)
    public boolean learnSectTechnique(Character member, Technique technique, int pointCost) {
        if (!isSectLibrary || sect == null) return false;
        if (member.sect != sect) return false;
        if (!techniques.contains(technique)) return false;
        if (member.sectPoints < pointCost) return false;
        member.sectPoints -= pointCost;
        Technique learned = new Technique(technique);
        learned.lvl = Math.max(1, technique.lvl / 3); // sect masters teach more directly
        member.techniqueList.add(learned);
        member.equip();
        return true;
    }
}
