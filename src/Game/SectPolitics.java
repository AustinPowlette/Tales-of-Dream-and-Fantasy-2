package Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/** The politics inside a sect: ambition, greed and violence between its own members. Every sect member has an alignment (Evil, Neutral, Good) and a
 *  caution; the chance of each villainy scales with both: Evil members act at full rate, Neutral ones at 30%, Good ones never.
 *    - bullying (each season): Evil/Neutral inner disciples shake down weaker outer disciples (spirit stones or a spare item); victims may complain
 *      (a Good master punishes the bully), leave, or fight back (a human victim is always asked first);
 *    - murder for loot (yearly): an Evil elder kills a weaker disciple who owns a better item than the elder's own; the crime is found out or not;
 *    - theft from the treasury (yearly): an Evil elder pockets cash, a weapon, an array, pills or a cut of the herbs and metals;
 *    - poisoning a rival (yearly): an Evil elder doses the strongest rival with a vial from their bag or the sect pantry;
 *    - framing (yearly): a schemer blames a rival to the master (the rival is thrown out, or the lie is exposed);
 *    - treachery (yearly): the old "ambitious disciple turns on the master", now also against a fellow disciple;
 *    - coup (yearly state machine, `Organization.plot`): Evil and Neutral elders conspire for a year or more, may be exposed, and strike the master in a
 *      tag-team duel: the strongest surviving plotter takes the throne (the loyal elders may be purged), otherwise the survivors are cast out.
 *  A discovered crime ends with the culprit expelled and a bounty on their head; the chronicle and the humans of the sect hear of the notable ones.
 *  Humans never act on their own here, and a human target is told and asked (`Character.acceptChallenge`) before anything turns into a fight.
 *  Everything runs once a year or a season on the game thread (never per character per tick). */
final class SectPolitics {
    private SectPolitics() { }

    /** A conspiracy against the master, kept on the Organization (null = none). Serialised with the world. */
    static class Plot implements Serializable {
        static final long serialVersionUID = 1L;
        ArrayList<Integer> plotters = new ArrayList<>(); // ids of the conspiring elders
        int startWeek;                                   // game week the plot was hatched
        int leaderId = -1;
    }

    // counters (not saved): the playtest tool and the probes read them
    static int bullied, murders, murdersExposed, thefts, poisonings, framings, plotsHatched, coupsWon, coupsFailed, plotsExposed, treacheries, errors;
    static String lastError = "";
    static void resetCounters() { bullied = murders = murdersExposed = thefts = poisonings = framings = plotsHatched = coupsWon = coupsFailed = plotsExposed = treacheries = errors = 0; lastError = ""; }
    static String summary() {
        return "bullied " + bullied + ", murders " + murders + " (exposed " + murdersExposed + "), thefts " + thefts + ", poisonings " + poisonings + ", framings " + framings
                + ", treachery " + treacheries + ", plots " + plotsHatched + " (coups won " + coupsWon + ", failed " + coupsFailed + ", exposed " + plotsExposed + "), errors " + errors;
    }

    /** Multiplies every chance (tests raise it to force events; the game leaves it at 1). */
    static double scale = 1.0;
    static final double NEUTRAL_RATE = 0.3;
    // base chances, in percent per Evil member of the right rank per year (per season for bullying), before the caution factor
    static final int BASE_BULLY = 25, BASE_MURDER = 7, BASE_THEFT = 8, BASE_POISON = 6, BASE_FRAME = 8, BASE_PLOT = 5, BASE_TREACHERY = 12;

    // ------------------------------------------------------------------ helpers
    private static String nm(Character c) { return c.name + " " + c.lastName; }

    /** 1.0 Evil, 0.3 Neutral, 0 Good. */
    static double evil(Character c) {
        int a = c.affiliationNum == null ? 0 : c.affiliationNum;
        return a < 0 ? 1.0 : a == 0 ? NEUTRAL_RATE : 0.0;
    }

    /** Does this member act on a base chance (percent)? The cautious hesitate, the impulsive do not. */
    static boolean roll(Character c, double basePercent) {
        double p = evil(c) * basePercent * (1.3 - c.caution / 100.0) * scale;
        return p > 0 && GameDriver.rand.nextDouble() * 100 < p;
    }

    /** A character the sect's NPC intrigue may use as an actor: alive, grown, a cultivator, not a human (humans decide for themselves). */
    static boolean actor(Character c) {
        return c != null && !c.dead && c.born && c.age >= 14 && c.player != 1 && c.trait != Character.Traits.Beast && c.trait != Character.Traits.Monster;
    }

    static class Roster {
        final ArrayList<Character> elders = new ArrayList<>(), inners = new ArrayList<>(), outers = new ArrayList<>();
        int n;
    }

    static Roster roster(Organization s) {
        Roster ro = new Roster();
        for (Character c : s.members) {
            if (c == null || c.dead || c.sect != s || c == s.master) continue;
            ro.n++;
            switch (s.getRank(c)) {
                case ELDER: case FOUNDER: ro.elders.add(c); break;
                case INNER_DISCIPLE: ro.inners.add(c); break;
                case MASTER: break;
                default: ro.outers.add(c);
            }
        }
        return ro;
    }

    static List<Organization> allSects() {
        List<Organization> out = new ArrayList<>();
        for (Area a : new ArrayList<>(Map.createdAreas)) if (a != null && a.sect != null && a.sect.members != null && (a.sect.master != null || !a.sect.members.isEmpty())) out.add(a.sect);
        return out;
    }

    static int relValue(Character a, Character b) { Relationship r = a.relationshipList.contains(b.id); return r == null ? 0 : r.value; }

    /** Sets the relationship between two characters directly (both directions, -4..3). */
    static void setRel(Character a, Character b, int v) {
        Relationship r1 = a.relationshipList.contains(b.id), r2 = b.relationshipList.contains(a.id);
        if (r1 == null) a.relationshipList.add(new Relationship(b.id, v)); else r1.changeValue(v);
        if (r2 == null) b.relationshipList.add(new Relationship(a.id, v)); else r2.changeValue(v);
    }

    /** Chronicle line (kind "sect") and a notice to every human member of the sect. */
    static void announce(Organization s, String text, int... ids) {
        Chronicle.add("sect", text, ids);
        for (Character h : Notifications.humans()) {
            if (h == null || h.dead || h.player != 1) continue;
            boolean named = false; // a human named in the news hears it even when it has just lost the sect (a master who was driven out by a coup was never told)
            for (int id : ids) if (id == h.id) named = true;
            if (h.sect == s || named) Notifications.post("sect", text, ids.length > 0 ? ids[0] : -1, h.id);
        }
    }

    private static void tell(Character h, String text, int charId) { if (h != null && h.player == 1 && !h.dead) Notifications.post("sect", text, charId, h.id); }

    /** Throws a member out of the sect (a master's throne passes on); `hunted` also puts a price on their head. Humans are never expelled by intrigue. */
    static void expel(Organization s, Character c, boolean hunted) {
        if (c == null || c.dead || c.player == 1) return;
        Character m = s.master;
        s.resign(c);
        if (m != null && m != c && !m.dead) setRel(c, m, Math.min(relValue(c, m), -2));
        if (hunted) Life.bounty(c, s.location);
    }

    static long gearPrice(Character c) {
        long best = 0;
        if (c.equippedWeapon != null) best = Math.max(best, Character.itemPrice(c.equippedWeapon));
        if (c.equippedClothing != null) best = Math.max(best, Character.itemPrice(c.equippedClothing));
        for (Item it : c.backpack) if (it != null && (it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing)) best = Math.max(best, Character.itemPrice(it));
        for (Item it : c.arrayBag) if (it != null) best = Math.max(best, Character.itemPrice(it));
        return best;
    }

    private static void fail(Organization s, RuntimeException e) {
        errors++; lastError = (s == null ? "?" : s.name) + ": " + e;
        System.err.println("[SectPolitics] " + lastError);
        e.printStackTrace();
    }

    // ------------------------------------------------------------------ seasonal: bullying
    static void seasonal(int season) {
        Random r = GameDriver.rand;
        for (Organization s : allSects()) {
            try { bullyRound(s, r); } catch (RuntimeException e) { fail(s, e); }
        }
    }

    static void bullyRound(Organization s, Random r) {
        if (s.members == null || s.members.size() < 3) return;
        Roster ro = roster(s);
        if (ro.inners.isEmpty() || ro.outers.isEmpty()) return;
        int done = 0;
        for (Character b : ro.inners) {
            if (done >= 3) break;
            if (!actor(b) || !roll(b, BASE_BULLY)) continue;
            Character v = null;
            for (int t = 0; t < 6 && v == null; t++) {
                Character c = ro.outers.get(r.nextInt(ro.outers.size()));
                if (c != null && !c.dead && c.sect == s && c.age >= 8 && c.lvl <= b.lvl + 2 && c.trait != Character.Traits.Beast) v = c;
            }
            if (v == null) continue;
            bully(s, b, v, r);
            done++;
        }
    }

    /** One shakedown. Returns true when the bully got something. */
    static boolean bully(Organization s, Character b, Character v, Random r) {
        bullied++;
        if (relValue(b, v) > -1) setRel(b, v, -1); // so a fight between them counts as a feud (loser pays 10%)
        if (v.player == 1) {
            tell(v, nm(b) + ", an inner disciple of " + s.name + ", comes to take what is yours.", b.id);
            if (!v.acceptChallenge(b)) { tell(v, "You slipped away from " + nm(b) + ".", b.id); return false; }
            b.battle(v);
            return true;
        }
        b.tempLvlCheck(); v.tempLvlCheck();
        int win = 50 - 15 * (v.tempLvl - b.tempLvl);
        if (r.nextInt(100) + 1 > win) { v.changeRelationship(b, -1); return false; } // the victim stood up to them
        long took = FightReport.moveWealth(v, b, 15);
        String what = took > 0 ? FightReport.moneyOf(took) : null;
        if (took < 20) { // nothing in the purse: a spare piece of gear instead
            Item pick = null;
            for (Item it : v.backpack) if (it != null && (it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing) && it != v.equippedWeapon && it != v.equippedClothing
                    && (pick == null || Character.itemPrice(it) < Character.itemPrice(pick))) pick = it;
            if (pick != null) { v.backpack.remove(pick); b.backpack.add(pick); what = pick.name; }
        }
        v.changeRelationship(b, -1);
        boolean notable = v.player > 0 || b.majorLvl >= 3 || Map.trackedCharacters.contains(b.id) || Map.trackedCharacters.contains(v.id);
        if (notable && what != null) announce(s, nm(b) + " of " + s.name + " bullied the outer disciple " + nm(v) + " out of " + what + ".", b.id, v.id);
        // the victim may complain; a Good master punishes the bully, an Evil one shrugs and the victim may quit
        Character m = s.master;
        int goodness = m == null || m.affiliationNum == null ? 0 : m.affiliationNum;
        int complain = goodness > 0 ? 50 : goodness == 0 ? 25 : 8;
        if (r.nextInt(100) < complain) {
            if (goodness >= 0 && m != null && !m.dead) { s.memberRanks.put(b.id, Organization.SectRank.OUTER_DISCIPLE); setRel(m, b, Math.min(relValue(m, b), -1)); }
        } else if (goodness < 0 && r.nextInt(100) < 20 && v.player == 0) expel(s, v, false);
        return took > 0 || what != null;
    }

    // ------------------------------------------------------------------ yearly
    /** Called from Seasonal.diplomacy once a year with every sect that has members. */
    static void yearly(List<Organization> all) {
        Random r = GameDriver.rand;
        for (Organization s : all) {
            try { yearlySect(s, r); } catch (RuntimeException e) { fail(s, e); }
        }
    }

    static void yearlySect(Organization s, Random r) {
        if (s.members == null) return;
        boolean virtual = s.isVirtual(); // the Palace of the Oldest Order has no master to turn on or overthrow, but its Evil elders still murder, steal and scheme
        if (!virtual && (s.master == null || s.master.dead)) { s.plot = null; return; }
        Roster ro = roster(s);
        if (!virtual) {
            treachery(s, ro, r);
            if (s.master == null || s.master.dead) return;
            plotYearly(s, ro, r);
            if (s.master == null || s.master.dead || s.members.isEmpty()) return;
            ro = roster(s); // the coup (or an exposure) may have changed who is who
        } else s.plot = null;
        boolean murdered = false, stole = false, poisoned = false, framed = false;
        for (Character e : new ArrayList<>(ro.elders)) {
            if (!actor(e) || e.dead || e.sect != s) continue;
            if (!murdered && roll(e, BASE_MURDER)) murdered = murder(s, e, ro, r);
            if (!stole && !e.dead && roll(e, BASE_THEFT)) stole = theft(s, e, r);
            if (!poisoned && !e.dead && roll(e, BASE_POISON)) poisoned = poisonRival(s, e, ro, r);
        }
        for (Character c : new ArrayList<>(ro.inners)) { // schemers: inner disciples and elders blame their rivals
            if (framed) break;
            if (actor(c) && !c.dead && c.sect == s && roll(c, BASE_FRAME)) framed = frame(s, c, ro, r);
        }
        if (!framed) for (Character c : new ArrayList<>(ro.elders)) if (actor(c) && !c.dead && c.sect == s && roll(c, BASE_FRAME)) { framed = frame(s, c, ro, r); break; }
    }

    // ---- treachery: an ambitious member turns on the master or a fellow disciple (the original yearly event)
    private static void treachery(Organization s, Roster ro, Random r) {
        if (s.members.size() < 6 || r.nextDouble() * 100 >= BASE_TREACHERY * scale) return;
        Character plotter = null, target = null;
        boolean againstMaster = r.nextInt(100) < 60;
        for (int tries = 0; tries < 15 && plotter == null; tries++) {
            Character m = s.members.get(r.nextInt(s.members.size()));
            if (!actor(m) || m == s.master || m.age < 18 || m.sect != s) continue;
            int al = m.affiliationNum == null ? 0 : m.affiliationNum;
            boolean ambitious = al < 0 || (m.caution < 25 && al == 0) || (al == 0 && r.nextInt(4) == 0) || (al > 0 && r.nextInt(12) == 0);
            if (!ambitious) continue;
            if (againstMaster) { if (m.lvl + 8 >= s.master.lvl * 0.75) { plotter = m; target = s.master; } }
            else {
                Character rival = s.members.get(r.nextInt(s.members.size()));
                if (rival != null && rival != m && !rival.dead && rival.sect == s && rival.age >= 14 && Math.abs(rival.lvl - m.lvl) <= 10) { plotter = m; target = rival; }
            }
        }
        if (plotter == null || target == null) return;
        setRel(plotter, target, Math.min(relValue(plotter, target), -3));
        treacheries++;
        announce(s, nm(plotter) + " of " + s.name + " turned against " + (target == s.master ? "the master " : "the fellow disciple ") + nm(target) + ": treachery within the sect.", plotter.id, target.id);
    }

    // ---- murder for loot
    /** An Evil elder kills a weaker disciple who holds a better item. Returns true when the murder was attempted. */
    static boolean murder(Organization s, Character e, Roster ro, Random r) {
        Character victim = null; long best = 0;
        long mine = gearPrice(e);
        List<Character> pool = new ArrayList<>(ro.inners); pool.addAll(ro.outers);
        for (Character v : pool) {
            if (v == null || v.dead || v.sect != s || v.age < 8 || v == e || e.lvl < v.lvl + 3) continue;
            long theirs = gearPrice(v);
            if (theirs >= 150 && theirs > mine * 1.1 && theirs > best) { best = theirs; victim = v; }
        }
        if (victim == null) return false;
        murders++;
        setRel(e, victim, -4); // a deadly feud: the fight is lethal
        if (victim.player == 1) {
            tell(victim, "Elder " + nm(e) + " of " + s.name + " covets your gear and comes for you.", e.id);
            if (!victim.acceptChallenge(e)) { setRel(e, victim, -3); tell(victim, "You slipped away from " + nm(e) + ". They are your sworn enemy now.", e.id); return true; }
        }
        FightReport.lastFleer = null;
        e.battle(victim);
        if (!victim.dead && !e.dead) setRel(e, victim, -3); // the victim got away, and knows
        if (e.dead) {
            announce(s, "Elder " + nm(e) + " of " + s.name + " was slain by the disciple " + nm(victim) + " whom he tried to murder for a treasure.", victim.id, e.id);
            return true;
        }
        if (!victim.dead) return true;
        // the body is found, or not
        boolean caught = r.nextInt(100) < 40;
        Character m = s.master;
        boolean mercy = m != null && m.affiliationNum != null && m.affiliationNum < 0 && e.lvl * 10 >= m.lvl * 6 && r.nextInt(100) < 50; // an Evil master may look away from a strong elder
        if (caught && !mercy) {
            murdersExposed++;
            announce(s, "Elder " + nm(e) + " murdered the disciple " + nm(victim) + " of " + s.name + " for a treasure; the crime came to light and the elder was cast out.", e.id, victim.id);
            expel(s, e, true);
        } else {
            for (Character h : Notifications.humans()) if (h != null && !h.dead && h.player == 1 && h.sect == s && h != victim)
                Notifications.post("sect", "The disciple " + nm(victim) + " of " + s.name + " was found dead. Nobody knows who did it.", victim.id, h.id);
        }
        return true;
    }

    // ---- theft from the treasury
    static boolean theft(Organization s, Character e, Random r) {
        Item bestItem = null; long bestV = 0;
        for (Item it : s.treasury) {
            if (it == null || it.itemType == null || it.quantity <= 0) continue;
            if (it.itemType == Item.ItemType.Weapon || it.itemType == Item.ItemType.Clothing || it.itemType == Item.ItemType.Array) {
                long v = Character.itemPrice(it);
                if (v > bestV) { bestV = v; bestItem = it; }
            }
        }
        long cash = s.sectCash;
        String what;
        if (bestItem != null && (cash < 1000 || r.nextBoolean())) {
            s.treasury.remove(bestItem);
            if (bestItem.itemType == Item.ItemType.Array) e.arrayBag.add(bestItem); else e.backpack.add(bestItem);
            what = bestItem.name;
        } else if (cash >= 1000) {
            long take = Math.max(1, cash * (15 + r.nextInt(21)) / 100);
            s.sectCash -= take; e.ss += take; e.convertCurrency();
            what = take + " ss from the sect's coffers";
        } else { // herbs and metals: a cut of the biggest stack, fenced for a few stones
            Item stack = null;
            for (Item it : s.treasury) if (it != null && it.quantity > 1 && (it.itemType == Item.ItemType.Herb || it.itemType == Item.ItemType.Metal)
                    && (stack == null || (long) it.quantity * it.lvl > (long) stack.quantity * stack.lvl)) stack = it;
            if (stack == null) return false;
            int cut = Math.max(1, stack.quantity * (15 + r.nextInt(16)) / 100);
            stack.quantity -= cut;
            long pay = Math.max(1, (long) cut * stack.lvl / 10);
            e.ss += pay; e.convertCurrency();
            what = cut + " " + stack.name;
        }
        thefts++;
        Character m = s.master;
        boolean caught = r.nextInt(100) < 30 || (m != null && m.player == 1 && r.nextInt(100) < 20);
        if (caught) {
            announce(s, "Elder " + nm(e) + " was caught stealing " + what + " from the treasury of " + s.name + " and was cast out.", e.id);
            expel(s, e, true);
        } else if (m != null && m.player == 1 && r.nextInt(100) < 35) {
            tell(m, "Your treasury is lighter than it should be: someone in " + s.name + " has been stealing.", -1);
        }
        return true;
    }

    // ---- poison for a rival
    static boolean poisonRival(Organization s, Character e, Roster ro, Random r) {
        Item vial = Life.firstOf(e, Item.ItemType.Poison);
        boolean fromPantry = false;
        if (vial == null) {
            for (Item it : s.treasury) if (it != null && it.itemType == Item.ItemType.Poison && it.quantity > 0 && (vial == null || it.lvl > vial.lvl)) vial = it;
            fromPantry = vial != null;
        }
        if (vial == null && e.affiliationNum != null && e.affiliationNum < 0 && r.nextInt(100) < 30) { // an Evil elder buys a vial from a street poisoner (nobody carried one: 0 poisonings in 65 simulated years)
            vial = new Item(Item.ItemType.Poison, Math.max(1, Math.min(10, e.majorLvl + 1)), 1, "Street Venom");
            if (e.goods == null) e.goods = new ArrayList<>();
            e.goods.add(vial);
        }
        if (vial == null) return false;
        Character rival = null;
        List<Character> pool = new ArrayList<>(ro.elders); pool.addAll(ro.inners);
        for (Character c : pool) {
            if (c == e || c == null || c.dead || c.sect != s || c.player == 1 || c.lvl < e.lvl * 0.7 || c.lvl > e.lvl * 1.4) continue;
            if (rival == null || c.lvl > rival.lvl) rival = c;
        }
        if (rival == null) return false;
        int lvl = vial.lvl;
        vial.quantity--;
        if (vial.quantity <= 0) { if (fromPantry) s.treasury.remove(vial); else e.goods.remove(vial); }
        boolean took = Life.poison(e, rival, lvl);
        poisonings++;
        if (took) announce(s, nm(rival) + " of " + s.name + " fell gravely ill after a cup shared in the hall; whispers point at Elder " + nm(e) + ".", rival.id, e.id);
        return true;
    }

    // ---- framing a rival to the master
    static boolean frame(Organization s, Character schemer, Roster ro, Random r) {
        Character m = s.master;
        if (m == null || m.dead) return false;
        Organization.SectRank mine = s.getRank(schemer);
        Character rival = null;
        List<Character> pool = new ArrayList<>(ro.elders); pool.addAll(ro.inners);
        for (Character c : pool) {
            if (c == schemer || c == null || c.dead || c.sect != s || c.player == 1 || c.lvl < schemer.lvl - 5) continue;
            Organization.SectRank cr = s.getRank(c);
            if (cr.ordinal() < mine.ordinal()) continue; // only a rival of equal or higher standing is worth the lie
            if (rival == null || c.lvl > rival.lvl) rival = c;
        }
        if (rival == null) return false;
        framings++;
        int al = m.affiliationNum == null ? 0 : m.affiliationNum;
        int believes = al > 0 ? 60 : al == 0 ? 50 : 40;
        if (r.nextInt(100) < believes) {
            setRel(m, rival, Math.min(relValue(m, rival), -2));
            announce(s, nm(rival) + " was cast out of " + s.name + " on the strength of lies spread by " + nm(schemer) + ".", rival.id, schemer.id);
            Organization.SectRank was = s.getRank(rival);
            expel(s, rival, false);
            if (was == Organization.SectRank.ELDER && s.getRank(schemer) == Organization.SectRank.INNER_DISCIPLE) s.promoteRank(schemer);
        } else if (r.nextInt(100) < 35) {
            s.memberRanks.put(schemer.id, Organization.SectRank.OUTER_DISCIPLE);
            setRel(schemer, rival, Math.min(relValue(schemer, rival), -3));
            announce(s, nm(schemer) + " of " + s.name + " was caught slandering " + nm(rival) + " and was demoted.", schemer.id, rival.id);
        }
        return true;
    }

    // ------------------------------------------------------------------ the coup
    static List<Character> livePlotters(Organization s, Plot p) {
        List<Character> out = new ArrayList<>();
        for (int i = p.plotters.size() - 1; i >= 0; i--) {
            Character c = Character.charById(p.plotters.get(i));
            Organization.SectRank rk = c == null ? null : s.getRank(c);
            if (c == null || c.dead || c.sect != s || rk == Organization.SectRank.MASTER || rk == Organization.SectRank.OUTER_DISCIPLE) p.plotters.remove(i);
            else out.add(c);
        }
        return out;
    }

    static void plotYearly(Organization s, Roster ro, Random r) {
        Character m = s.master;
        Plot p = s.plot;
        if (p != null && livePlotters(s, p).isEmpty()) { s.plot = null; p = null; }
        if (p == null) { hatch(s, ro, r); return; }
        int years = (GameDriver.time - p.startWeek) / 52;
        // more elders are drawn in
        for (Character e : ro.elders) {
            if (!actor(e) || p.plotters.contains(e.id) || e.affiliationNum == null || e.affiliationNum > 0) continue;
            if (r.nextDouble() * 100 < (e.affiliationNum < 0 ? 30 : 10) * scale) p.plotters.add(e.id);
        }
        // the master (or a loyal elder) may smell it out
        boolean loyal = false;
        for (Character e : ro.elders) if (e != null && !p.plotters.contains(e.id) && relValue(e, m) >= 1) loyal = true;
        if (r.nextInt(100) < 12 + (loyal ? 10 : 0) + m.caution / 10) { expose(s, p, r); return; }
        if (m.player == 1 && r.nextInt(100) < 40) { // a human master hears rumours
            List<Character> pl = livePlotters(s, p);
            if (!pl.isEmpty()) tell(m, "Rumours in " + s.name + ": Elder " + nm(pl.get(0)) + " has been meeting others in secret. You may want to act (they are " + pl.size() + (pl.size() == 1 ? " person" : " people") + ").", pl.get(0).id);
        }
        if (years >= 1 && (years >= 3 || r.nextInt(100) < 45)) strike(s, p, years, r);
    }

    private static void hatch(Organization s, Roster ro, Random r) {
        Character m = s.master, lead = null;
        for (Character e : ro.elders) {
            if (!actor(e) || !roll(e, BASE_PLOT) || e.lvl < m.lvl * 0.7) continue; // the weak do not dare
            if (lead == null || e.lvl > lead.lvl) lead = e;
        }
        if (lead == null) return;
        Plot p = new Plot();
        p.startWeek = GameDriver.time; p.leaderId = lead.id; p.plotters.add(lead.id);
        for (Character e : ro.elders) { // the first conspirators
            if (e == lead || !actor(e) || e.affiliationNum == null || e.affiliationNum > 0) continue;
            if (r.nextInt(100) < (e.affiliationNum < 0 ? 55 : 20)) p.plotters.add(e.id);
        }
        s.plot = p;
        plotsHatched++;
        if (m.player == 1) tell(m, "You hear whispers that someone in " + s.name + " wants your seat.", lead.id);
    }

    /** The master learns of the conspiracy: the plotters are cast out (an Evil master executes the weaker ones). A human master is only told. */
    static void expose(Organization s, Plot p, Random r) {
        Character m = s.master;
        List<Character> pl = livePlotters(s, p);
        boolean ruthless = m.player == 0 && m.affiliationNum != null && m.affiliationNum < 0;
        int executed = 0, expelled = 0;
        StringBuilder names = new StringBuilder();
        for (Character c : pl) {
            names.append(names.length() > 0 ? ", " : "").append(nm(c));
            if (ruthless && c.lvl <= m.lvl * 0.9) { c.deathCause = "executed for plotting against the master"; c.dead(m); if (c.dead) { executed++; continue; } }
            expel(s, c, true); expelled++;
        }
        plotsExposed++;
        s.plot = null;
        announce(s, "A conspiracy against the master " + nm(m) + " of " + s.name + " was exposed: " + names + (executed > 0 ? " (" + executed + " executed)" : "") + (expelled > 0 ? " (" + expelled + " cast out)" : "") + ".", m.id);
    }

    /** The conspirators strike the master one after another (the master tires each round). The strongest plotter left standing inherits the sect. */
    static void strike(Organization s, Plot p, int years, Random r) {
        Character m = s.master;
        List<Character> pl = livePlotters(s, p);
        if (m == null || m.dead || pl.isEmpty()) { s.plot = null; return; }
        m.tempLvlCheck();
        int sum = 0;
        for (Character c : pl) { c.tempLvlCheck(); sum += c.tempLvl; }
        pl.sort((x, y) -> Integer.compare(y.tempLvl, x.tempLvl));
        if (sum < m.tempLvl) { if (years >= 3) s.plot = null; return; } // too weak to dare: wait for allies, or give up after three years
        if (m.player == 1) {
            tell(m, "Elder " + nm(pl.get(0)) + " and " + (pl.size() - 1) + " others ambush you in " + s.name + "!", pl.get(0).id);
            if (!m.acceptChallenge(pl.get(0))) {
                for (Character c : pl) setRel(c, m, -3);
                s.plot = null; coupsFailed++;
                tell(m, "You slipped away from the plotters of " + s.name + ". They are your sworn enemies now.", pl.get(0).id);
                return;
            }
        }
        HashMap<Integer, Organization.SectRank> ranks = new HashMap<>(s.memberRanks);
        boolean masterFled = false;
        for (int i = 0; i < pl.size() && !m.dead && !masterFled; i++) {
            Character c = pl.get(i);
            if (c.dead || c.sect != s) continue;
            m.exhaustionPenalty = i * 2;
            setRel(c, m, -4);
            FightReport.lastFleer = null;
            c.battle(m);
            if (FightReport.lastFleer == m) masterFled = true;
        }
        m.exhaustionPenalty = 0;
        List<Character> alive = new ArrayList<>();
        for (Character c : pl) if (!c.dead && c.sect == s) alive.add(c);
        s.plot = null;
        if (m.dead || masterFled) {
            if (!m.dead) s.resign(m);          // the master abandons the sect to its fate
            if (alive.isEmpty()) { announce(s, "The coup in " + s.name + " drowned in blood: the master and the plotters all fell.", m.id); coupsFailed++; return; }
            Character winner = alive.get(0);
            installMaster(s, winner, ranks);
            if (!m.dead) setRel(m, winner, -3);
            int purged = 0;
            for (Character e : new ArrayList<>(s.members)) {
                if (e == null || e.dead || e == winner || alive.contains(e) || e.player == 1 || s.getRank(e) != Organization.SectRank.ELDER) continue;
                if (r.nextInt(100) < 35) { setRel(e, winner, -3); expel(s, e, false); purged++; }
            }
            coupsWon++;
            announce(s, "COUP in " + s.name + ": " + nm(winner) + " seized the sect after " + (m.dead ? "killing" : "driving out") + " the master " + nm(m) + (purged > 0 ? "; " + purged + " loyal elders were purged" : "") + ".", winner.id, m.id);
        } else {
            coupsFailed++;
            for (Character c : alive) expel(s, c, true);
            announce(s, "The coup against the master " + nm(m) + " of " + s.name + " failed: " + (alive.isEmpty() ? "the plotters died" : alive.size() + " survivors were cast out") + ".", m.id);
        }
    }

    /** Makes `winner` the master (after the old one died or left, `promote()` may have crowned somebody else), keeping every rank consistent. */
    static void installMaster(Organization s, Character winner, HashMap<Integer, Organization.SectRank> ranksBefore) {
        Character cur = s.master;
        if (cur != winner) {
            if (cur != null && !cur.dead && cur.sect == s) {
                Organization.SectRank old = ranksBefore.get(cur.id);
                s.memberRanks.put(cur.id, old == null || old == Organization.SectRank.MASTER ? Organization.SectRank.ELDER : old);
            }
            s.master = winner;
        }
        s.memberRanks.put(winner.id, Organization.SectRank.MASTER);
        s.crisisTimer = 0;
    }
}
