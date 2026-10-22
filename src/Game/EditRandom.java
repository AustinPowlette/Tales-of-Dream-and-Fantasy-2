package Game;

import static Game.GameDriver.rand;

/** The dice of the Randomize button of Edit Character (`GuiEditSelf`, and `(R)andomize` in the terminal editor): a name from the NPC name engine, a skin tone, a trait, a potential from the NPC curve,
 *  an alignment and a gender. Each draw is independent; nothing here touches a character (the editor fills its fields and Done / Cancel work as before). */
final class EditRandom {
    private EditRandom() { }

    /** A first and a last name that fit the gender and the region (not kept as taken: the name is applied the way a typed one is). */
    static String[] name(boolean female, Area where) {
        String[] n;
        try { n = where == null ? Naming.person(female) : Naming.person(female, where); } catch (RuntimeException e) { n = Naming.person(female); }
        Naming.releaseName(n[0], n[1]);
        return n;
    }

    /** One of the nine skin tones (1-9; 0 would leave it to the portrait). */
    static int skin() { return 1 + rand.nextInt(ArtMore.SKINS.length); }

    /** A trait a player may have: any but the two beast traits and the Cheater (which changes the rules of potential). */
    static Character.Traits trait() {
        java.util.List<Character.Traits> all = new java.util.ArrayList<>();
        for (Character.Traits t : Character.Traits.values()) if (t != Character.Traits.Monster && t != Character.Traits.Beast && t != Character.Traits.Cheater) all.add(t);
        return all.get(rand.nextInt(all.size()));
    }

    /** Potential from the NPC curve (1-100: most people are low, a few are high). */
    static int potential() { return Math.max(1, Math.min(100, (int) (101 - Math.sqrt(rand.nextInt(10000) + 1)))); }

    /** Good, Neutral or Evil, a third each (the world's own mix). */
    static String alignment() { switch (rand.nextInt(3)) { case 0: return "Good"; case 1: return "Neutral"; default: return "Evil"; } }

    static boolean female() { return rand.nextBoolean(); }
}
