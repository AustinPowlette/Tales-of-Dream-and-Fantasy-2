package Game;

import java.util.ArrayList;

// TODO: Flesh out further — Family technique inheritance (at birth / major realm milestone),
//       Sect Contribution Point earning (missions, gifts from superiors), mastery levelling during
//       cultivation/events, technique rarity tiers, and per-technique access restrictions.
public class Library {

    Character owner;        // null for sect/city libraries
    Area location;
    Organization sect;      // null for city/personal libraries
    ArrayList<Technique> techniques = new ArrayList<>();
    boolean isSectLibrary;

    // City or public library
    public Library(Area location) {
        this.location = location;
        this.isSectLibrary = false;
    }

    // Sect library (techniques gated behind sect contribution points)
    public Library(Organization sect) {
        this.sect = sect;
        this.location = sect.location;
        this.isSectLibrary = true;
    }

    // Price to buy a copy of a technique at a city library
    public long buyPrice(Technique t) {
        return Math.max(1000L, (long) t.potential * t.potential * 1000L);
    }

    // Sell/donate a technique to this library; buyer pays character for it
    public void sellTechnique(Character seller, Technique technique) {
        long price = Math.max(500L, (long) technique.potential * technique.potential * 500L);
        seller.ss += price;
        seller.convertCurrency();
        if (owner != null) owner.deductSs(price);
        else if (location != null && location.cityOwner != null) location.cityOwner.deductSs(price);
        techniques.add(technique);
    }

    // Learn a technique from a city library (pays ss, gets a copy)
    public boolean learnTechnique(Character buyer, Technique technique) {
        if (!techniques.contains(technique)) return false;
        long price = buyPrice(technique);
        if (!buyer.canAfford(price)) return false;
        buyer.deductSs(price);
        if (location != null && location.cityOwner != null) {
            location.cityOwner.ss += price;
            location.cityOwner.convertCurrency();
        }
        buyer.techniqueList.add(new Technique(technique)); // copy — original stays in library
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
        member.techniqueList.add(new Technique(technique));
        member.equip();
        return true;
    }
}
