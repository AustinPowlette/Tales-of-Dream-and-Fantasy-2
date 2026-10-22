package Game;

import java.util.ArrayList;

import static Game.GameDriver.rand;
import static Game.GameDriver.time;

/** The auction houses of the cities (population 500 or more). Only luxury goods are auctioned: Heaven-Sent treasures, Potential Pills and vintage food, wine and poison.
 *  Cultivators consign lots when they sell in a city (`Character.sell`); the city's own vintage goods join now and then. A city holds an auction when {@link #LOTS_TO_HOLD}
 *  lots wait, or when {@link #MAX_WAIT} weeks have passed since the first of them arrived (never earlier than the week after). The winner pays the runner-up's bid (or the reserve
 *  price); the consignor receives 90%, the city lord (or the treasury) the rest, and the whole payment if there is no consignor. */
final class Auctions {
    private Auctions() { }

    static final int LOTS_TO_HOLD = 5, MAX_WAIT = 6, MAX_LOTS_PER_AUCTION = 5;
    /** Counters for the playtest probes (not saved): auctions held, lots sold, lots that found no buyer, and the Potential Pills among the sold. */
    static volatile int stHeld, stSold, stUnsold, stPotentialPills;

    static boolean hasHouse(Area a) { return a != null && a.city && a.population >= 500; }

    /** What belongs on the auction block instead of a shop: Heaven-Sent items and Potential Pills. */
    static boolean auctionable(Item it) {
        return it != null && it.itemType != null && (it.heavenSent || (it.itemType == Item.ItemType.Pill && it.pillType == Item.PillType.Potential));
    }

    /** Puts a lot on the block (stackable lots merge). `who` is paid when it sells. */
    static void consign(Area city, Item it, Character who) {
        ArrayList<Item> bag = city.auction();
        it.consignorId = who == null ? -1 : who.id;
        if (it.stackable) {
            for (Item o : bag) if (o.name != null && o.name.equals(it.name) && o.lvl == it.lvl && o.consignorId == it.consignorId) { o.quantity = (int) Math.min(Integer.MAX_VALUE, (long) o.quantity + it.quantity); return; }
        }
        bag.add(it);
    }

    /** The lowest price a lot may sell for. */
    static long reserve(Item it) {
        if (Life.isGood(it.itemType) && !it.heavenSent) return (long) (Math.pow(Math.max(1, it.lvl), 2.5) * 1000);
        return Math.max(1000L, Character.itemPrice(it) * Math.max(1, it.quantity) * (it.heavenSent ? 20L : 5L));
    }

    /** Weeks until the next auction here, or -1 when there is nothing on the block. */
    static int weeksToNext(Area a) {
        if (a == null || a.auctionBag == null || a.auctionBag.isEmpty()) return -1;
        if (a.auctionBag.size() >= LOTS_TO_HOLD) return 1;
        int since = a.auctionSince > 0 ? a.auctionSince : time;
        return Math.max(1, MAX_WAIT - (time - since));
    }

    /** Called every week for every city: vintage goods join the block now and then, and the auction is held when it is due. */
    static void tick(Area a) {
        if (!hasHouse(a)) return;
        if ((a.auctionBag == null || a.auctionBag.size() < 3) && rand.nextInt(26) == 0) Life.auctionLots(a);
        ArrayList<Item> bag = a.auctionBag;
        if (bag == null || bag.isEmpty()) { a.auctionSince = 0; return; }
        if (a.auctionSince <= 0) a.auctionSince = Math.max(1, time);
        int waited = time - a.auctionSince;
        if (waited >= 1 && (bag.size() >= LOTS_TO_HOLD || waited >= MAX_WAIT)) hold(a);
    }

    /** Would this character bid for the lot? Characters bid for what they need (the Potential Pill, the next-realm pill, better gear) and the very rich for anything. */
    static boolean wants(Character b, Item lot, long wealth) { return want(b, lot, wealth) > 0; }

    static final int NO_INTEREST = 0, COLLECTOR = 1, NEED = 2;
    /** How much the bidder wants the lot: a need (the Potential Pill, the next-realm pill, better gear, an array for a ruler), a collector's fancy (a purse of twenty reserves), or nothing. */
    static int want(Character b, Item lot, long wealth) {
        switch (lot.itemType) {
            case Pill:
                if (lot.pillType == Item.PillType.Potential) { if (b.potential < 100) return NEED; break; }
                if (lot.lvl > b.majorLvl && lot.lvl <= b.majorLvl + 2) return NEED;
                break;
            case Weapon: if (b.equippedWeapon == null || lot.lvl > b.equippedWeapon.lvl) return NEED; break;
            case Clothing: if (b.equippedClothing == null || lot.lvl > b.equippedClothing.lvl) return NEED; break;
            case Array: if (b.sect != null && b.sect.isMaster(b)) return NEED; if (b.location != null && b.location.cityOwner == b) return NEED; break;
            default: break;
        }
        return wealth >= reserve(lot) * 20 ? COLLECTOR : NO_INTEREST;
    }

    /** What a computer-played bidder offers: someone in need up to three reserves, a collector up to one and a half, and never more than half (a need) or a tenth (a collector) of the purse. It used to be the whole purse, so the winner paid the runner-up's entire fortune (billions for a plate of dumplings). */
    static long npcBid(int want, long wealth, long base) {
        double f = want == NEED ? 1.0 + 2.0 * rand.nextDouble() : 1.0 + 0.5 * rand.nextDouble();
        long bid = (long) Math.ceil(base * f);
        long cap = want == NEED ? wealth / 2 : wealth / 10;
        if (bid > cap) bid = Math.max(base, cap);
        return Math.min(bid, wealth);
    }

    private static void give(Character buyer, Item lot) {
        switch (lot.itemType) {
            case Pill: Character.addToBag(buyer.pillBag, lot); break;
            case Array: if (buyer.arrayBag == null) buyer.arrayBag = new ArrayList<>(); buyer.arrayBag.add(lot); break;
            case Food: case Wine: case Poison: Character.addToBag(Life.goods(buyer), lot); break;
            default: buyer.backpack.add(lot); buyer.equip(); break;
        }
    }

    /** One auction: up to five lots, best first. A human standing in the city is asked for a maximum bid per lot (0 passes); NPCs and AUTO characters bid what they want. */
    static void hold(Area a) {
        ArrayList<Item> bag = a.auction();
        bag.removeIf(i -> i == null || i.itemType == null || i.quantity <= 0);
        if (bag.isEmpty()) { a.auctionSince = 0; return; }
        bag.sort((x, y) -> Long.compare(reserve(y), reserve(x)));
        a.auctionSince = 0;
        long minReserve = Long.MAX_VALUE;
        ArrayList<Item> lots = new ArrayList<>(bag.subList(0, Math.min(MAX_LOTS_PER_AUCTION, bag.size())));
        for (Item l : lots) minReserve = Math.min(minReserve, reserve(l));
        ArrayList<Character> bidders = new ArrayList<>(), humans = new ArrayList<>();
        for (Character c : Map.characters) {
            if (c == null || c.dead || !c.born || c.age < 8 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) continue;
            long wealth = c.ss + c.sc * 1_000_000L + c.dc * 1_000_000_000_000L;
            if (wealth < minReserve) continue;
            if (c.player == 1 && DecisionBroker.promptable(c)) {
                if (c.location == a) humans.add(c); // a human must be in the city to bid
            } else if ((c.player != 1 || c.pc().mode == PlayerControl.Mode.AUTO) && c.knownLocations.contains(a) && c.location != null && PlayerControl.distance(c.location, a) <= 8) { // bidders have come (the Attend Auction quest) or live within eight tiles // a HYBRID character never bids by itself
                bidders.add(c);
            }
        }
        stHeld++;
        for (Item lot : lots) {
            long base = reserve(lot);
            Character buyer = null;
            long best = base - 1, second = base;
            for (Character c : bidders) {
                if (c.dead) continue;
                long wealth = c.ss + c.sc * 1_000_000L + c.dc * 1_000_000_000_000L;
                if (wealth < base) continue;
                int want = want(c, lot, wealth);
                if (want == NO_INTEREST) continue;
                long bid = npcBid(want, wealth, base);
                if (bid > best) { second = Math.max(base, best); best = bid; buyer = c; }
                else if (bid > second) second = bid;
            }
            for (Character h : humans) {
                if (h.dead) continue;
                long wealth = h.ss + h.sc * 1_000_000L + h.dc * 1_000_000_000_000L;
                if (wealth < base) continue;
                long max = DecisionBroker.askNumber(h, "Auction at " + a.name + ": " + describe(lot) + (lot.heavenSent ? " [Heaven-Sent]" : "") + ". Reserve price " + base
                        + " ss. Your wealth: " + wealth + " ss. The winner pays the runner-up's bid. Your maximum bid (0 = pass)?", 0);
                long bid = Math.min(max, wealth);
                if (bid < base) continue;
                if (bid > best) { second = Math.max(base, best); best = bid; buyer = h; }
                else if (bid > second) second = bid;
            }
            if (buyer == null) { stUnsold++; continue; } // nobody met the reserve: the lot waits for the next auction
            long payment = Math.min(best, second);
            stSold++; if (lot.itemType == Item.ItemType.Pill && lot.pillType == Item.PillType.Potential) stPotentialPills += lot.quantity;
            buyer.deductSs(payment);
            bag.remove(lot);
            give(buyer, lot);
            Character consignor = lot.consignorId >= 0 ? Character.charById(lot.consignorId) : null;
            if (consignor != null && !consignor.dead && consignor != buyer) {
                long fee = payment / 10;
                consignor.ss += payment - fee; consignor.convertCurrency();
                a.receivePayment(fee);
            } else a.receivePayment(payment);
            lot.consignorId = -1;
            Chronicle.add("world", "Auction at " + a.name + ": " + buyer.name + " " + buyer.lastName + " won " + describe(lot) + " for " + payment + " ss.", buyer.id);
            Say.about(buyer, "Auction at " + a.name + ": " + buyer.name + " " + buyer.lastName + " wins " + describe(lot) + " for " + payment + " SS!");
        }
        Life.returnUnsoldLots(a); // unsold vintage goods go back on the shelves; the rest wait for next time
    }

    static String describe(Item it) { return it.name + (it.quantity > 1 ? " x" + it.quantity : "") + " (Lv." + it.lvl + ")"; }

    /** The lots on the block and when the next auction is, for the terminal and the GUI. */
    static String text(Area a, Character c) {
        StringBuilder sb = new StringBuilder("Auction at " + a.name + "\n\n");
        if (!a.city) sb.append("You are not in a city.\n");
        else if (a.population < 500) sb.append("This city is too small for an auction house (needs 500 residents).\n");
        else {
            ArrayList<Item> lots = new ArrayList<>();
            if (a.auctionBag != null) for (Item i : new ArrayList<>(a.auctionBag)) if (i != null && i.itemType != null) lots.add(i);
            lots.sort((x, y) -> Long.compare(reserve(y), reserve(x)));
            if (lots.isEmpty()) sb.append("No lots on the block right now.\n");
            for (int i = 0; i < lots.size(); i++) sb.append(String.format("  Lot %d: %s%s   reserve %d ss%n", i + 1, describe(lots.get(i)), lots.get(i).heavenSent ? " [Heaven-Sent]" : "", reserve(lots.get(i))));
            int w = weeksToNext(a);
            sb.append("\nOnly luxuries are auctioned: Heaven-Sent treasures, Potential Pills and vintage food, wine and poison. Sell them in a city ('sell') to put them on the block.\n");
            sb.append(w < 0 ? "An auction is held when " + LOTS_TO_HOLD + " lots wait, or " + MAX_WAIT + " weeks after the first arrives.\n" : "Next auction: in about " + w + " week(s) (sooner if " + LOTS_TO_HOLD + " lots wait).\n");
            sb.append("Be standing in the city then: you are asked for a maximum bid per lot (0 passes; the winner pays the runner-up's bid). The consignor gets 90%.\n");
        }
        if (c != null) sb.append("Your wealth: ").append(PlayerLists.wealth(c)).append(" ss\n");
        return sb.toString();
    }
}
