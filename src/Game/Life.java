package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Everyday arts of the cultivation world: food, wine, poison and music. Goods (food, wine, poison) live in `Character.goods`, instruments in the
 *  backpack. Short-lived conditions (nourished, drunk, inspired, poisoned, a poisoned blade) live in one small `Fx` record that is null for almost
 *  everybody. Everything here runs on the game thread or inside a character's own cultivate() call, so it only touches the character it is given
 *  (and, for poison and music, the people standing in the same tile). */
final class Life {
    private Life() { }

    private static final Random rand = Long.getLong("tdf.seed") != null ? new Random(Long.getLong("tdf.seed") + 1) : new Random();

    /** Temporary conditions (all counted in weeks). */
    static class Fx implements Serializable {
        static final long serialVersionUID = 1L;
        int foodWeeks;  double foodPow;   // nourished: cultivation bonus
        int drunkWeeks; int drunkLvl;     // tipsy: cultivation bonus, clumsy (or, for the Drunken, fierce)
        int inspiredWeeks; double inspiredPow; // heard music
        int poisonWeeks; int poisonLvl; int poisoner = -1; // poisoned: slower cultivation, weaker, maybe fatal
        int coat; int coatLvl;            // poisoned-blade charges
        boolean idle() { return foodWeeks <= 0 && drunkWeeks <= 0 && inspiredWeeks <= 0 && poisonWeeks <= 0 && coat <= 0; }
    }

    static Fx fx(Character c) { if (c.fx == null) c.fx = new Fx(); return c.fx; }
    static int goodsCount(Character c) { int n = 0; if (c.goods != null) for (Item it : c.goods) n += it.quantity; return n; }

    static ArrayList<Item> goods(Character c) { if (c.goods == null) c.goods = new ArrayList<>(); return c.goods; }

    static boolean isGood(Item.ItemType t) { return t == Item.ItemType.Food || t == Item.ItemType.Wine || t == Item.ItemType.Poison; }

    // ------------------------------------------------------------------ traits
    /** The new life-arts traits replace a common trait now and then (about 1.6% each). */
    static Character.Traits rollTrait(Character.Traits t) {
        switch (t) {
            case Blacksmith: case Alchemist: case Scribe: case Merchant: case Gatherer: case Miner: case Friendly: case Tailor: case Runner: break;
            default: return t;
        }
        int r = rand.nextInt(1000);
        if (r < 16) return Character.Traits.Tamer;
        if (r < 32) return Character.Traits.Poisoner;
        if (r < 48) return Character.Traits.Drunken;
        if (r < 64) return Character.Traits.Chef;
        if (r < 80) return Character.Traits.Musician;
        return t;
    }

    // ------------------------------------------------------------------ effects (called by Character)
    static int resist(Character c) {
        return c.majorLvl + 1 + (c.trait == Character.Traits.Poisoner ? 2 : 0) + (c.trait == Character.Traits.IronBody ? 2 : 0)
                + (c.fx != null && c.fx.foodWeeks > 0 ? 1 : 0);
    }

    /** Multiplier on a character's cultivation this week. */
    static double cultMult(Character c) {
        Fx f = c.fx;
        if (f == null) return 1.0;
        double m = 1.0;
        if (f.foodWeeks > 0) m += f.foodPow;
        if (f.drunkWeeks > 0) m += 0.25 * Math.min(1.0, f.drunkLvl / (c.majorLvl + 1.0)) * (c.trait == Character.Traits.Drunken ? 1.5 : 1.0);
        if (f.inspiredWeeks > 0) m += f.inspiredPow;
        if (f.poisonWeeks > 0) m *= 0.6;
        return m;
    }

    /** Added to the fighting level (after the /5): drunk fighters stumble, the Drunken fight like a storm; poison saps strength. */
    static int powerMod(Character c) {
        Fx f = c.fx;
        if (f == null) return 0;
        int p = 0;
        if (f.drunkWeeks > 0) p += c.trait == Character.Traits.Drunken ? 2 + f.drunkLvl / 4 : -1;
        if (f.poisonWeeks > 0) p -= 1 + f.poisonLvl / 3;
        return p;
    }

    /** Once a week from Character.time(). */
    static void tick(Character c) {
        Fx f = c.fx;
        if (f == null) return;
        if (f.foodWeeks > 0) f.foodWeeks--;
        if (f.inspiredWeeks > 0) f.inspiredWeeks--;
        if (f.drunkWeeks > 0) {
            f.drunkWeeks--;
            if (c.trait == Character.Traits.Drunken && rand.nextInt(8) == 0) c.cultivate(); // a drunken master's flash of insight
        }
        if (f.poisonWeeks > 0) {
            f.poisonWeeks--;
            if (f.poisonWeeks == 0) endPoison(c, f);
        }
        if (f.idle()) c.fx = null;
    }

    private static void endPoison(Character c, Fx f) {
        int res = resist(c);
        int lvl = f.poisonLvl;
        Character by = f.poisoner >= 0 ? Character.charById(f.poisoner) : null;
        f.poisonLvl = 0; f.poisoner = -1;
        if (lvl <= res || c.dead) { if (c.player > 0 && c.skipping <= 0) System.out.println("The poison has run its course."); return; }
        int deadly = Math.min(60, 20 * (lvl - res));
        if (rand.nextInt(100) < deadly) {
            c.deathCause = "poisoned" + (by != null ? " by " + by.name + " " + by.lastName : "");
            if (by != null && !by.dead && by != c) { bounty(by, c.location); c.dead(by); } else c.dead();
        } else if (c.player > 0 && c.skipping <= 0) System.out.println("You survive the poison, barely.");
    }

    /** `source` poisons `target` with a poison of level `lvl`. True if it took hold. */
    static boolean poison(Character source, Character target, int lvl) {
        if (target == null || target.dead || target == source) return false;
        int res = resist(target);
        int chance = Math.max(5, Math.min(95, 55 + 15 * (lvl - res) + (source != null && source.trait == Character.Traits.Poisoner ? 15 : 0)));
        if (rand.nextInt(100) >= chance) return false;
        Fx f = fx(target);
        f.poisonLvl = Math.max(f.poisonLvl, lvl);
        f.poisonWeeks = 13;
        f.poisoner = source == null ? -1 : source.id;
        if (target.player > 0 && target.skipping <= 0) System.out.println("You feel sick: you have been poisoned (level " + lvl + ")" + (lvl > resist(target) ? " — it may be deadly. Eat food of level " + lvl + "+ to purge it." : ".")); // `resist` of the victim, not the source
        if (source != null && source.player > 0 && source.skipping <= 0) System.out.println(target.name + " " + target.lastName + " has been poisoned.");
        if (source != null && rand.nextInt(4) == 0) { // somebody saw it
            Relationship rel = target.relationshipList.contains(source.id);
            if (rel == null || rel.value > -2) { bounty(source, target.location); if (source.player > 0) System.out.println("Someone saw you: a bounty has been posted on you."); }
        }
        return true;
    }

    /** Start of a fight: a poisoned blade lets some of its venom into the opponent. */
    static void onBattle(Character attacker, Character opponent) {
        Fx f = attacker.fx;
        if (f == null || f.coat <= 0 || opponent == null) return;
        f.coat--;
        if (opponent.trait != Character.Traits.Beast && poison(attacker, opponent, f.coatLvl)) opponent.tempLvl = Math.max(1, opponent.tempLvl - (1 + f.coatLvl / 3)); // already-computed fighting level of the victim
    }

    // ------------------------------------------------------------------ bounties on poisoners
    static Area nearestCity(Area from) {
        Area best = null; int bd = Integer.MAX_VALUE;
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null || !a.city) continue;
            int d = from == null ? 0 : PlayerControl.distance(from, a);
            if (d < bd) { bd = d; best = a; }
        }
        return best;
    }

    /** A poisoner has been recognised: the nearest city posts a bounty on them. */
    static void bounty(Character culprit, Area near) {
        if (culprit == null || culprit.dead) return;
        Area city = near != null && near.city ? near : nearestCity(near);
        if (city == null) return;
        final int id = culprit.id;
        if (city.bounties.stream().anyMatch(b -> b.targetCharacter == id)) return;
        city.bounties.add(new Quest("Bounty Hunt", true, 55, 4, 80, culprit));
        Chronicle.add("world", "A bounty was posted at " + city.name + " on the poisoner " + culprit.name + " " + culprit.lastName + " (#" + culprit.id + ").", culprit.id);
    }

    // ------------------------------------------------------------------ goods
    private static Item bestHerb(Character c, int maxLvl, int need) {
        Item best = null;
        for (Item it : c.materialBag) {
            if (it.itemType == Item.ItemType.Herb && it.quantity >= need && it.lvl <= maxLvl && (best == null || it.lvl > best.lvl)) best = it;
        }
        return best;
    }

    private static boolean matches(Character c, Item.ItemType t) {
        switch (t) {
            case Food: return c.trait == Character.Traits.Chef;
            case Wine: return c.trait == Character.Traits.Drunken;
            case Poison: return c.trait == Character.Traits.Poisoner;
            case Instrument: return c.trait == Character.Traits.Musician;
            default: return false;
        }
    }

    /** The trait that owns each art, for messages. */
    static String artist(Item.ItemType t) {
        switch (t) { case Food: return "Chef"; case Wine: return "Drunken"; case Poison: return "Poisoner"; case Instrument: return "Musician"; default: return "specialist"; }
    }

    /** Cooks, brews or mixes from herbs (50 per unit) and files the result. Only the matching trait can (Chef, Drunken, Poisoner, Musician).
     *  2 = made something, -2 = no herbs, -3 = not your art. */
    static int craft(Character c, Item.ItemType type) {
        if (!matches(c, type)) return -3;
        boolean pro = true;
        int skill = Math.max(10, c.professionLvl);
        Item herb = bestHerb(c, skill / 10 + 2, 50);
        if (herb == null) return -2;
        int units = Math.min(herb.quantity / 50, pro ? skill / 10 + 1 : 2);
        if (units <= 0) return -2;
        herb.quantity -= 50 * units;
        if (herb.quantity <= 0) c.materialBag.remove(herb);
        Item made;
        if (type == Item.ItemType.Instrument) {
            made = new Item(c, Math.min(10, herb.lvl + (pro ? 1 : 0)), Item.ItemType.Instrument);
            c.backpack.add(made);
        } else {
            int qty = units * (type == Item.ItemType.Food ? 3 : type == Item.ItemType.Wine ? 2 : 1);
            String name = type == Item.ItemType.Food ? Naming.dish(herb.lvl, c) : type == Item.ItemType.Wine ? Naming.wine(herb.lvl, c) : Naming.poison(herb.lvl, c);
            made = new Item(type, herb.lvl, qty, name);
            made.maker = c.name + " " + c.lastName; made.makerID = c.id;
            Character.addToBag(goods(c), made);
        }
        c.itemsCrafted++;
        if (pro && c.professionLvl < 100) {
            c.professionExp += herb.lvl * 10 * units;
            while (c.professionExp > c.professionExpMax && c.professionLvl < 100) { c.professionLvl++; c.professionExp -= c.professionExpMax; c.professionExpMax = Math.pow(10, c.professionLvl / 10.); }
        }
        if (c.player > 0 && c.skipping <= 0 && c.age >= 8) System.out.println("You made " + made + ".");
        return 2;
    }

    /** Eats or drinks one portion of the item (Food or Wine). */
    static boolean use(Character c, Item it) {
        if (it == null || it.quantity <= 0) return false;
        Fx f = fx(c);
        double eff = Math.min(1.0, it.lvl / (c.majorLvl + 1.0));
        if (it.itemType == Item.ItemType.Food) {
            boolean chef = c.trait == Character.Traits.Chef;
            f.foodWeeks = Math.max(f.foodWeeks, chef ? 26 : 13);
            f.foodPow = Math.max(f.foodPow, 0.15 * eff * (chef ? 1.5 : 1.0));
            if (f.poisonWeeks > 0 && it.lvl >= f.poisonLvl) { f.poisonWeeks = 0; f.poisonLvl = 0; f.poisoner = -1; if (c.player > 0 && c.skipping <= 0) System.out.println("The meal purges the poison."); }
        } else if (it.itemType == Item.ItemType.Wine) {
            f.drunkWeeks = Math.max(f.drunkWeeks, c.trait == Character.Traits.Drunken ? 6 : 4);
            f.drunkLvl = Math.max(f.drunkLvl, it.lvl);
        } else return false;
        it.quantity--;
        if (it.quantity <= 0) goods(c).remove(it);
        if (c.player > 0 && c.skipping <= 0 && c.age >= 8) System.out.println("You " + (it.itemType == Item.ItemType.Wine ? "drink " : "eat ") + it.name + ".");
        return true;
    }

    static Item firstOf(Character c, Item.ItemType t) {
        if (c.goods == null) return null;
        Item best = null;
        for (Item it : c.goods) if (it.itemType == t && it.quantity > 0 && (best == null || it.lvl > best.lvl)) best = it;
        return best;
    }

    static long unitPrice(Item it) { return Math.max(1, Character.itemPrice(it) / 4); }

    static final int MARKET_CAP = 600; // portions a city market holds before the oldest spoil

    static ArrayList<Item> market(Area a) { if (a.goodsBag == null) a.goodsBag = new ArrayList<>(); return a.goodsBag; }

    /** What a buyer pays for one portion at the market (poison costs three times as much). */
    static long buyPrice(Item it) { return unitPrice(it) * (it.itemType == Item.ItemType.Poison ? 3 : 1); }

    /** What the market pays a seller for one portion. */
    static long sellPrice(Item it) { return Math.max(1, buyPrice(it) / 2); }

    // ------------------------------------------------------------------ shops and the market together
    static ArrayList<Item> shelf(Shop s) { if (s.goodsShelf == null) s.goodsShelf = new ArrayList<>(); return s.goodsShelf; }

    /** What a shop charges for a portion (the market price at the default markup; a player raises it with `price <shop> sell <rate>`). */
    static long shopSellPrice(Shop s, Item it) { return Math.max(1, Math.round(buyPrice(it) * s.sellRate / 1.5)); }

    /** What a shop pays a seller for a portion (a little above what the city market bids). */
    static long shopBuyPrice(Shop s, Item it) { return Math.max(1, Math.round(sellPrice(it) * s.buyRate / 0.5)); }

    /** One thing for sale: a stack on the city market (shop == null) or on a shop's shelf. */
    static class Offer { final Item stack; final Shop shop; final long price; Offer(Item st, Shop sh, long p) { stack = st; shop = sh; price = p; } }

    static List<Offer> offers(Area a) {
        List<Offer> out = new ArrayList<>();
        for (Item it : new ArrayList<>(market(a))) out.add(new Offer(it, null, buyPrice(it)));
        for (Shop sh : new ArrayList<>(a.shops)) for (Item it : new ArrayList<>(shelf(sh))) out.add(new Offer(it, sh, shopSellPrice(sh, it)));
        return out;
    }

    static int marketCount(Area a) { int n = 0; for (Item it : market(a)) n += it.quantity; return n; }

    static int shelfCount(Shop s) { int n = 0; for (Item it : shelf(s)) n += it.quantity; return n; }

    /** Sells goods to the city market (everything but what a trade keeps for itself, or only `only` kinds, or at festival prices): the portions
     *  go on the market's shelves, the seller is paid. Returns what was earned. */
    static long sellGoods(Character c) { return sellGoods(c, false, null, -1); }

    static long sellGoods(Character c, boolean festival, Item.ItemType only, int maxQty) {
        if (c.goods == null || c.goods.isEmpty() || c.location == null || !c.location.city) return 0;
        long earned = 0;
        int left = maxQty < 0 ? Integer.MAX_VALUE : maxQty;
        for (Item it : new ArrayList<>(c.goods)) {
            if (only != null && it.itemType != only) continue;
            int keep = only != null ? 0 : it.itemType == Item.ItemType.Poison && c.trait == Character.Traits.Poisoner ? 2 : it.itemType == Item.ItemType.Food ? 1 : 0;
            int sell = Math.min(left, it.quantity - keep);
            if (sell <= 0) continue;
            Shop taker = null;
            for (Shop sh : c.location.shops) { // the best-paying shop whose owner can afford the stock takes it first
                if (sh.owner == null || sh.owner == c || sh.owner.dead || sh.buyRate < 0.5) continue;
                if (shelfCount(sh) + sell > 150 || !sh.owner.canAfford(shopBuyPrice(sh, it) * sell)) continue;
                if (taker == null || sh.buyRate > taker.buyRate) taker = sh;
            }
            Item onShelf = new Item(it.itemType, it.lvl, sell, it.name);
            onShelf.maker = it.maker; onShelf.makerID = it.makerID;
            if (taker != null) {
                long pay = shopBuyPrice(taker, it) * sell;
                taker.owner.deductSs(pay);
                earned += pay * (festival ? 2 : 1) / (festival ? 1 : 1);
                Character.addToBag(shelf(taker), onShelf);
            } else {
                earned += sellPrice(it) * sell * (festival ? 2 : 1);
                Character.addToBag(market(c.location), onShelf);
            }
            left -= sell;
            it.quantity -= sell;
            if (it.quantity <= 0) c.goods.remove(it);
        }
        ArrayList<Item> m = market(c.location);
        while (marketCount(c.location) > MARKET_CAP && !m.isEmpty()) m.remove(0); // the oldest portions spoil
        if (earned > 0) { c.ss += earned; c.convertCurrency(); }
        if (earned > 0 && c.player > 0 && c.skipping <= 0 && c.age >= 8) System.out.println("Sold goods for " + earned + " ss" + (festival ? " (festival prices)" : "") + ".");
        return earned;
    }

    /** Buys `qty` portions of a market stack (compat: the city market only). */
    static String buy(Character c, Item stack, int qty) {
        for (Offer o : offers(c.location)) if (o.stack == stack) return buy(c, o, qty);
        return "That is no longer on the market.";
    }

    /** Buys `qty` portions of an offer (city market or a shop's shelf). Returns null on success, else why not. */
    static String buy(Character c, Offer offer, int qty) {
        Area a = c.location;
        Item stack = offer.stack;
        if (a == null || !a.city) return "You need to be in a city.";
        if (stack == null || stack.quantity <= 0 || !(offer.shop == null ? market(a).contains(stack) : shelf(offer.shop).contains(stack))) return "That is no longer on sale.";
        if (offer.shop != null && offer.shop.owner == c) return "It is your own shelf: set prices with 'price'.";
        qty = Math.max(1, Math.min(qty, stack.quantity));
        long price = offer.price * qty;
        if (!c.canAfford(price)) return stack.name + " costs " + price + " ss for " + qty + "; you have " + PlayerLists.wealth(c) + " ss.";
        c.deductSs(price);
        if (offer.shop != null && offer.shop.owner != null) { offer.shop.owner.ss += price; offer.shop.owner.convertCurrency(); } else a.receivePayment(price);
        Item mine = new Item(stack.itemType, stack.lvl, qty, stack.name);
        mine.maker = stack.maker; mine.makerID = stack.makerID;
        Character.addToBag(goods(c), mine);
        stack.quantity -= qty;
        if (stack.quantity <= 0) (offer.shop == null ? market(a) : shelf(offer.shop)).remove(stack);
        if (c.player > 0 && c.skipping <= 0) System.out.println("Bought " + qty + " x " + stack.name + " for " + price + " ss" + (offer.shop != null ? " from " + offer.shop.name : "") + ".");
        return null;
    }

    static final int HOLD_CAP = 200; // portions of goods one character keeps at most; the surplus rots in the bag

    /** What is left of a stack after one year. The loss is a share of the stack: food goes off fastest (55% for a plain grade), wine keeps (15%), poison
     *  in between (20%); better grades last longer (about 12% less loss per grade above 1); every stack loses at least 8% a year; a big stack spoils
     *  faster (up to twice the share at 150+ portions, so a hoard cannot grow without bound); at least one portion is always lost. */
    static int spoil(Item it) {
        if (it == null || it.quantity <= 0) return 0;
        double base = it.itemType == Item.ItemType.Food ? 0.55 : it.itemType == Item.ItemType.Wine ? 0.15 : 0.20;
        double grade = 1.0 / (1.0 + 0.12 * Math.max(0, it.lvl - 1));
        double size = 1.0 + Math.min(1.0, it.quantity / 150.0);
        double loss = Math.min(0.9, Math.max(0.08, base * grade) * size);
        int lost = Math.max(1, (int) Math.round(it.quantity * loss));
        return Math.max(0, it.quantity - lost);
    }

    /** Spoils every food/wine/poison stack in the list (other items are left alone). */
    static void spoilList(List<Item> list) {
        if (list == null) return;
        for (int i = list.size() - 1; i >= 0; i--) {
            Item it = list.get(i);
            if (it == null || it.itemType == null || !isGood(it.itemType)) continue;
            it.quantity = spoil(it);
            if (it.quantity <= 0) list.remove(i);
        }
    }

    /** The yearly spoilage of every pile of goods in the world: city markets, shop shelves, sect pantries and what characters carry (their hoards are
     *  also capped at HOLD_CAP portions, the worst and oldest rotting first). It is the sink that keeps stock per head flat while production grows. */
    static void marketYearly() {
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null) continue;
            if (a.goodsBag != null && !a.goodsBag.isEmpty()) spoilList(a.goodsBag);
            if (a.shops != null) for (Shop s : a.shops) if (s != null && s.goodsShelf != null && !s.goodsShelf.isEmpty()) spoilList(s.goodsShelf);
            if (a.sect != null && a.sect.treasury != null && !a.sect.treasury.isEmpty()) spoilList(a.sect.treasury);
        }
        for (int i = 0, n = Map.characters.size(); i < n; i++) {
            Character c = Map.characters.get(i);
            if (c == null || c.goods == null || c.goods.isEmpty()) continue;
            spoilList(c.goods);
            int total = goodsCount(c);
            while (total > HOLD_CAP && !c.goods.isEmpty()) { // the surplus rots: lowest grade first
                Item worst = c.goods.get(0);
                for (Item it : c.goods) if (it.lvl < worst.lvl || (it.lvl == worst.lvl && it.itemType == Item.ItemType.Food && worst.itemType != Item.ItemType.Food)) worst = it;
                int cut = Math.min(worst.quantity, total - HOLD_CAP);
                worst.quantity -= cut; total -= cut;
                if (worst.quantity <= 0) c.goods.remove(worst);
            }
        }
    }

    /** NPCs shop at the market now and then: a meal when not fed, wine for the thirsty (the Drunken above all), poison for a Poisoner out of it, a better
     *  instrument for a Musician. Cheap when there is nothing for sale. */
    static void npcShop(Character c) {
        Area a = c.location;
        if (c.age < 8 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster) return;
        if (a.goodsBag != null && !a.goodsBag.isEmpty() && rand.nextInt(6) == 0) {
            Fx f = c.fx;
            if ((f == null || f.foodWeeks <= 0) && firstOf(c, Item.ItemType.Food) == null) buyBest(c, Item.ItemType.Food, c.majorLvl + 1);
            else if (c.trait == Character.Traits.Drunken ? rand.nextInt(4) == 0 : rand.nextInt(40) == 0) { if (firstOf(c, Item.ItemType.Wine) == null) buyBest(c, Item.ItemType.Wine, c.majorLvl + 1); }
            if (c.trait == Character.Traits.Poisoner && firstOf(c, Item.ItemType.Poison) == null) buyBest(c, Item.ItemType.Poison, c.majorLvl + 2);
        }
        if (c.trait == Character.Traits.Musician && a.backpack != null && rand.nextInt(20) == 0) {
            Item mine = bestInstrument(c), best = null;
            for (Item it : new ArrayList<>(a.backpack)) if (it != null && it.itemType == Item.ItemType.Instrument && (mine == null || it.lvl > mine.lvl) && (best == null || it.lvl > best.lvl)) best = it;
            if (best != null) { long price = Character.itemPrice(best); if (c.canAfford(price)) { c.deductSs(price); a.receivePayment(price); a.backpack.remove(best); c.backpack.add(best); } }
        }
    }

    private static void buyBest(Character c, Item.ItemType type, int maxLvl) {
        Offer best = null;
        for (Offer o : offers(c.location)) {
            if (o.stack.itemType != type || o.stack.quantity <= 0 || o.stack.lvl > Math.max(1, maxLvl) || !c.canAfford(o.price)) continue;
            if (best == null || o.stack.lvl > best.stack.lvl || (o.stack.lvl == best.stack.lvl && o.price < best.price)) best = o;
        }
        if (best != null) buy(c, best, 1);
    }

    /** A Merchant stocks the shop's goods shelf from the city market (a few portions a visit, at a wholesale price). */
    static void restockShopGoods(Shop shop) {
        if (shop.owner == null || shop.location == null) return;
        Area city = shop.location;
        if (shelfCount(shop) >= 100) return;
        int bought = 0;
        for (Item it : new ArrayList<>(market(city))) {
            if (bought >= 8) break;
            int take = Math.min(8 - bought, it.quantity);
            long cost = Math.max(1, buyPrice(it) * 2 / 5) * take;
            if (take <= 0 || !shop.owner.canAfford(cost)) continue;
            shop.owner.deductSs(cost);
            city.receivePayment(cost);
            Item moved = new Item(it.itemType, it.lvl, take, it.name);
            moved.maker = it.maker; moved.makerID = it.makerID;
            Character.addToBag(shelf(shop), moved);
            it.quantity -= take;
            if (it.quantity <= 0) market(city).remove(it);
            bought += take;
        }
    }

    // ------------------------------------------------------------------ the yearly auction
    /** Puts the best stacks of vintage goods on the auction block (up to two lots of up to five portions; the best of the city's goods). */
    static void auctionLots(Area a) {
        ArrayList<Item> m = market(a);
        for (int lot = 0; lot < 2; lot++) {
            Item best = null;
            for (Item it : m) if (it.quantity > 0 && it.lvl >= 3 && (best == null || it.lvl > best.lvl)) best = it;
            if (best == null) return;
            int qty = Math.min(5, best.quantity);
            Item l = new Item(best.itemType, best.lvl, qty, "Vintage " + best.name);
            l.maker = best.maker; l.makerID = best.makerID;
            Auctions.consign(a, l, null);
            best.quantity -= qty;
            if (best.quantity <= 0) m.remove(best);
        }
    }

    /** Lots nobody bought go back on the shelves. */
    static void returnUnsoldLots(Area a) {
        if (a.auctionBag == null) return;
        for (Item it : new ArrayList<>(a.auctionBag)) {
            if (it != null && it.itemType != null && isGood(it.itemType) && !it.heavenSent && it.consignorId < 0) {
                a.auctionBag.remove(it);
                String base = it.name.startsWith("Vintage ") ? it.name.substring(8) : it.name;
                Item back = new Item(it.itemType, it.lvl, it.quantity, base);
                back.maker = it.maker; back.makerID = it.makerID;
                Character.addToBag(market(a), back);
            }
        }
    }

    // ------------------------------------------------------------------ sect missions: "Deliver|Food|lvl|qty|bonus"
    static Item.ItemType missionType(String kind) { return "Wine".equals(kind) ? Item.ItemType.Wine : "Poison".equals(kind) ? Item.ItemType.Poison : Item.ItemType.Food; }

    /** Can this character reasonably fill the mission? (A human always may: they can buy the goods.) */
    static boolean canMake(Character c, String mission) {
        String[] mp = mission.split("\\|");
        if (mp.length < 4) return false;
        return c.player == 1 || matches(c, missionType(mp[1]));
    }

    static int countIn(java.util.List<Item> bag, Item.ItemType type, int minLvl) { int n = 0; for (Item it : bag) if (it.itemType == type && it.lvl >= minLvl) n += it.quantity; return n; }

    /** Hands the mission's goods to the sect pantry (its treasury) and collects the points; returns true when it was delivered. */
    static boolean deliverMission(Character c) {
        if (c.sect == null || c.sectMission == null || !c.sectMission.startsWith("Deliver|") || c.location != c.sect.location) return false;
        String[] mp = c.sectMission.split("\\|");
        if (mp.length < 4) return false;
        Item.ItemType type = missionType(mp[1]);
        int minLvl = Integer.parseInt(mp[2]), qty = Integer.parseInt(mp[3]);
        if (c.goods == null || countIn(c.goods, type, minLvl) < qty) return false;
        int need = qty;
        for (Item it : new ArrayList<>(c.goods)) {
            if (it.itemType != type || it.lvl < minLvl || need <= 0) continue;
            int take = Math.min(need, it.quantity);
            Item given = new Item(type, it.lvl, take, it.name);
            given.maker = it.maker; given.makerID = it.makerID;
            Character.addToBag(c.sect.treasury, given);
            it.quantity -= take; need -= take;
            if (it.quantity <= 0) c.goods.remove(it);
        }
        c.sectPoints += c.sectMissionBonus;
        if (c.player > 0 && c.skipping <= 0) System.out.println("Sect mission complete: " + qty + " " + mp[1].toLowerCase() + " delivered to " + c.sect.name + "! +" + c.sectMissionBonus + " SP");
        c.sectMission = null; c.sectMissionBonus = 0;
        return true;
    }

    /** Called each turn: a character holding a delivery mission delivers, heads for the sect, or (in a city) buys what is missing. */
    static void pursueDelivery(Character c) {
        if (c.sect == null || c.sectMission == null || !c.sectMission.startsWith("Deliver|")) return;
        String[] mp = c.sectMission.split("\\|");
        if (mp.length < 4) return;
        Item.ItemType type = missionType(mp[1]);
        int minLvl = Integer.parseInt(mp[2]), qty = Integer.parseInt(mp[3]);
        int have = c.goods == null ? 0 : countIn(c.goods, type, minLvl);
        if (have >= qty) {
            if (c.location == c.sect.location) deliverMission(c);
            else if (c.targetQuests.stream().noneMatch(q -> "Deliver Goods".equals(q.name)) && (c.currentQuest == null || !"Deliver Goods".equals(c.currentQuest.name)))
                c.targetQuests.add(new Quest("Deliver Goods", true, 65, 3, 80, c.sect.location));
        } else if (c.location != null && c.location.city && !manualMode(c) && rand.nextInt(4) == 0) {
            for (Offer o : offers(c.location)) {
                if (o.stack.itemType != type || o.stack.lvl < minLvl || o.stack.quantity <= 0) continue;
                int want = Math.min(qty - have, o.stack.quantity);
                if (want > 0 && c.canAfford(o.price * want)) { buy(c, o, want); break; }
            }
        }
    }

    /** The sect kitchen: each season the pantry feeds the members; spring and winter feasts add wine. */
    static void sectFeast(int season) {
        for (Area a : new ArrayList<>(Map.createdAreas)) {
            if (a == null || a.sect == null || a.sect.members == null || a.sect.members.isEmpty()) continue;
            Organization s = a.sect;
            java.util.List<Character> alive = new ArrayList<>();
            for (Character m : new ArrayList<>(s.members)) if (m != null && !m.dead) alive.add(m);
            if (alive.isEmpty()) continue;
            Item food = null;
            for (Item it : s.treasury) if (it.itemType == Item.ItemType.Food && it.quantity > 0 && (food == null || it.lvl > food.lvl)) food = it;
            if (food != null) {
                int fed = Math.min(food.quantity, alive.size());
                for (int i = 0; i < fed; i++) { Fx f = fx(alive.get(i)); f.foodWeeks = Math.max(f.foodWeeks, 13); f.foodPow = Math.max(f.foodPow, 0.10 * Math.min(1.0, food.lvl / (alive.get(i).majorLvl + 1.0))); }
                food.quantity -= fed;
                if (food.quantity <= 0) s.treasury.remove(food);
            }
            Item wine = null;
            for (Item it : s.treasury) if (it.itemType == Item.ItemType.Wine && it.quantity > 0 && (wine == null || it.lvl > wine.lvl)) wine = it;
            if (wine != null && (season == 0 || season == 3)) {
                int poured = Math.min(wine.quantity, alive.size());
                for (int i = 0; i < poured; i++) { Fx f = fx(alive.get(i)); f.drunkWeeks = Math.max(f.drunkWeeks, 2); f.drunkLvl = Math.max(f.drunkLvl, wine.lvl); }
                wine.quantity -= poured;
                if (wine.quantity <= 0) s.treasury.remove(wine);
            }
        }
    }

    // ------------------------------------------------------------------ poison contracts
    /** A Poisoner takes a contract (a bounty that is not a straight fight): poisons the target and is paid at once. 1 = done. */
    static int contract(Character c, Character target) {
        if (target == null || target.dead) return 1;
        Item vial = firstOf(c, Item.ItemType.Poison);
        if (vial == null) return -1;
        vial.quantity--;
        if (vial.quantity <= 0) c.goods.remove(vial);
        boolean took = poison(c, target, vial.lvl);
        if (took) {
            long reward = Math.max(100L, (long) (Math.pow(Math.max(1, target.lvl), 2.5) * 3));
            c.ss += reward; c.convertCurrency();
            if (c.player > 0) System.out.println("Contract fulfilled: " + target.name + " " + target.lastName + " has been poisoned. Paid " + reward + " ss.");
            for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && a.bounties != null) { final int id = target.id; a.bounties.removeIf(b -> b.targetCharacter == id); }
        } else if (c.player > 0) System.out.println(target.name + " shrugged the poison off.");
        return 1;
    }

    static int marketCount(Area a, boolean unused) { return marketCount(a); }

    /** "Festival Trade": a Chef, brewer or musician comes to the festival city: goods sell at double and the musician plays. */
    static int festivalTrade(Character c) {
        if (c.location == null || !c.location.city) return -1;
        sellGoods(c, true, null, -1);
        if (c.trait == Character.Traits.Musician && bestInstrument(c) != null) play(c);
        return 1;
    }

    // ------------------------------------------------------------------ music
    static Item bestInstrument(Character c) {
        Item best = null;
        for (Item it : c.backpack) if (it.itemType == Item.ItemType.Instrument && (best == null || it.lvl > best.lvl)) best = it;
        return best;
    }

    /** Plays for the people here: the player meditates (an extra cultivation tick, two for a Musician), listeners are inspired, a city pays tips. 2 = played. */
    static int play(Character c) {
        if (c.trait != Character.Traits.Musician) return -3; // music is the Musician's art
        Item inst = bestInstrument(c);
        if (inst == null) return -2;
        boolean pro = c.trait == Character.Traits.Musician;
        int ticks = pro ? 2 : 1;
        for (int i = 0; i < ticks; i++) c.cultivate();
        double eff = Math.min(1.0, inst.lvl / (c.majorLvl + 1.0));
        int listeners = 0;
        if (c.location != null) {
            for (Character p : new ArrayList<>(c.location.characters)) {
                if (p == null || p.dead || p == c || p.age < 8 || p.trait == Character.Traits.Beast || p.trait == Character.Traits.Monster) continue;
                Relationship r = c.relationshipList.contains(p.id);
                if (r != null && r.value <= -2) continue; // enemies cover their ears
                Fx f = fx(p);
                f.inspiredWeeks = Math.max(f.inspiredWeeks, pro ? 8 : 4);
                f.inspiredPow = Math.max(f.inspiredPow, 0.10 * eff);
                if (rand.nextInt(5) == 0) p.changeRelationship(c, 1);
                if (++listeners >= 25) break;
            }
        }
        long tips = 0;
        if (c.location != null && c.location.city && listeners > 0) { tips = Math.max(1, Character.itemPrice(inst) / 50) * listeners; c.ss += tips; c.convertCurrency(); }
        if (c.player > 0 && c.skipping <= 0 && c.age >= 8)
            System.out.println("You play '" + Naming.songTitle(rand, 0) + "' on " + inst.name + (listeners > 0 ? " for " + listeners + " listener(s)" : " alone") + (tips > 0 ? " and earn " + tips + " ss in tips." : "."));
        return 2;
    }

    // ------------------------------------------------------------------ NPC behaviour
    /** The "Profession" quest for the life-arts traits. */
    static int profession(Character c) {
        switch (c.trait) {
            case Chef: {
                int r = craft(c, Item.ItemType.Food);
                if (r < 0) { needHerbs(c); return -2; }
                Item food = firstOf(c, Item.ItemType.Food);
                if (food != null && c.location != null) { // a meal for a friend
                    for (Character p : new ArrayList<>(c.location.characters)) {
                        if (p == null || p.dead || p == c || p.age < 8 || p.trait == Character.Traits.Beast) continue;
                        Relationship rel = c.relationshipList.contains(p.id);
                        if (rel != null && rel.value >= 1 && food.quantity > 1 && rand.nextInt(4) == 0) { food.quantity--; use(p, new Item(Item.ItemType.Food, food.lvl, 1, food.name)); p.changeRelationship(c, 1); break; }
                    }
                }
                sellGoods(c);
                return 2;
            }
            case Drunken: {
                int r = craft(c, Item.ItemType.Wine);
                if (r < 0) { needHerbs(c); return -2; }
                sellGoods(c);
                return 2;
            }
            case Poisoner: {
                int r = craft(c, Item.ItemType.Poison);
                if (r < 0) { needHerbs(c); return -2; }
                Item vial = firstOf(c, Item.ItemType.Poison);
                if (vial != null && c.location != null) {
                    for (Character p : new ArrayList<>(c.location.characters)) { // an old grudge in the same tile
                        if (p == null || p.dead || p == c || p.age < 8 || p.trait == Character.Traits.Beast || p.trait == Character.Traits.Monster) continue;
                        Relationship rel = c.relationshipList.contains(p.id);
                        if (rel != null && rel.value <= -2 && rand.nextInt(3) == 0) { vial.quantity--; if (vial.quantity <= 0) c.goods.remove(vial); poison(c, p, vial.lvl); p.changeRelationship(c, -3); break; }
                    }
                }
                if (vial != null && vial.quantity > 0 && fx(c).coat <= 0) { Fx f = fx(c); f.coat = 3; f.coatLvl = vial.lvl; vial.quantity--; if (vial.quantity <= 0) c.goods.remove(vial); }
                sellGoods(c);
                return 2;
            }
            case Musician: {
                if (bestInstrument(c) == null) {
                    int r = craft(c, Item.ItemType.Instrument);
                    if (r < 0) { needHerbs(c); return -2; }
                }
                return play(c);
            }
            case Tamer:
                return Taming.attempt(c) ? 2 : -2;
            default:
                return -2;
        }
    }

    private static void needHerbs(Character c) {
        if (c.location != null && c.location.herbMaterials > 0) c.forage();
        if (c.spotQuests.stream().noneMatch(q -> "Forage".equals(q.name))) c.spotQuests.add(new Quest("Forage", false, 40, 3, 70));
    }

    /** Called once a turn for every character from quest(): eating, drinking, the reincarnation call and an occasional taming attempt. Cheap when there is nothing to do. */
    static boolean manualMode(Character c) { return c.player == 1 && c.pc != null && c.pc.mode == PlayerControl.Mode.MANUAL; }

    static void upkeep(Character c) {
        boolean manual = manualMode(c); // a human in MANUAL mode eats and drinks when they choose to
        if (!manual && c.goods != null && !c.goods.isEmpty()) {
            Fx f = c.fx;
            if ((f == null || f.foodWeeks <= 0) && rand.nextInt(4) == 0) { Item food = firstOf(c, Item.ItemType.Food); if (food != null) use(c, food); }
            if ((f == null || f.drunkWeeks <= 0) && rand.nextInt(c.trait == Character.Traits.Drunken ? 6 : 80) == 0) { Item wine = firstOf(c, Item.ItemType.Wine); if (wine != null) use(c, wine); }
        }
        Reincarnation.check(c);
        if (c.lostBeastIds != null && !c.lostBeastIds.isEmpty()) { // beasts that remember this soul
            Taming.reunite(c);
            if (GameDriver.time % 26 == c.id % 26) Taming.postLostQuest(c);
        }
        if (!manualMode(c) && c.location != null && c.location.city) npcShop(c);
        if (c.player == 0 && c.age >= 8 && c.majorLvl >= 1 && c.location != null && c.location.monsters > 0 && (c.beastIds == null || c.beastIds.isEmpty()) && rand.nextInt(c.trait == Character.Traits.Tamer ? 100 : 1200) == 0) Taming.attempt(c); // everybody may win one beast over; Tamers try more often
    }
}
