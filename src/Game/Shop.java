package Game;

import java.util.ArrayList;

public class Shop implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    String name = "";
    Character owner;
    Area location;
    ArrayList<Item> inventory = new ArrayList<>();
    ArrayList<Item> goodsShelf = new ArrayList<>(); // food, wine and poison on sale (see Life); null in saves from before goods existed
    double buyRate    = 0.55; // fraction of itemPrice paid to sellers (beats city pool 0.50)
    double sellRate   = 1.50; // multiplier on itemPrice charged to buyers
    double cityBuyRate = 0.30; // fraction paid when restocking from city pool

    public Shop(Character owner, Area location) {
        this.owner = owner;
        this.location = location;
        this.name = Naming.shop(owner);
    }

    // A shop is only as good as its city and its owner: level = min(city level, owner's major realm), shown in the lists
    public int level() {
        int ownerLvl = (owner != null) ? Math.max(1, owner.majorLvl) : 1;
        return Math.max(1, Math.min(location != null ? location.cityLevel() : 1, ownerLvl));
    }
    /** A shop takes any item its owner can pay for (the old level * 10 cap left every strong item piling up in the city pool). */
    public boolean canStock(Item item) { return item != null; }

    public long buyPrice(Item item)  { return Math.max(1, (long)(Character.itemPrice(item) * buyRate));  }
    public long sellPrice(Item item) { return Math.max(1, (long)(Character.itemPrice(item) * sellRate)); }

    // Merchant buys an item from a seller; returns false if merchant can't afford it.
    public boolean buyFromSeller(Character seller, Item item) {
        if (!canStock(item)) return false;
        long price = buyPrice(item);
        if (!owner.canAfford(price)) return false;
        seller.backpack.remove(item);
        inventory.add(item);
        seller.ss += price;
        owner.deductSs(price);
        seller.convertCurrency();
        return true;
    }

    // Buyer purchases an item from this shop; returns false if buyer can't afford it.
    public boolean sellToBuyer(Character buyer, Item item) {
        long price = sellPrice(item);
        if (!buyer.canAfford(price)) return false;
        inventory.remove(item);
        buyer.backpack.add(item);
        buyer.deductSs(price);
        owner.ss += price;
        owner.convertCurrency();
        return true;
    }

    // Merchant restocks inventory from the city pool at cityBuyRate.
    public void restockFromCity(Area city) {
        if (city.backpack == null) return;
        ArrayList<Item> toBuy = new ArrayList<>();
        for (Item item : city.backpack) {
            if (!canStock(item)) continue;
            long cost = Math.max(1, (long)(Character.itemPrice(item) * cityBuyRate));
            if (owner.canAfford(cost)) {
                toBuy.add(item);
                owner.deductSs(cost);
                city.receivePayment(cost);
            }
        }
        city.backpack.removeAll(toBuy);
        inventory.addAll(toBuy);
    }
}
