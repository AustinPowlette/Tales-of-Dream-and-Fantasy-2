package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The concert pieces of the Musicians: twelve short pieces (a minute or so each, `kind: concert` scores) that are NOT part of the music the places choose from. When a Musician plays a concert (the Perform quest, or a person
 *  who plays plays himself) one of the pieces is heard (`Ambient.concert`): its lead voice is the musician's own instrument (a Guqin, a Pipa, an Erhu, a Dizi ...), its tempo follows his skill (a grade 1 instrument plays
 *  it at 90% of the written tempo, a grade 10 one at 108%), and its ornamentation does too: from grade 5 on the tune carries grace notes (`leadf`), from grade 8 on trills on its long notes (`leadt`). The key and the
 *  small changes of every performance (`Score.perform`) vary it. Every musician has a repertoire: the piece follows his id and the season.
 *
 *  The pieces are written as a tune in scale degrees (`3:1 5:.5 4,:2`, a mark `'` lifts a note an octave, `,` lowers it, `r` rests, durations in beats, `1/3` for the eighths of a 6/8 bar) over a chord for every bar; the
 *  accompaniment (a harp arpeggio, a pad, a bass) and the two ornamented versions of the tune are made from that. Three sections of eight bars: A, B and A again with another ending. */
final class Concerts {
    private Concerts() { }

    /** One piece: title, tempo, beats per bar, key (the MIDI note of degree 1), scale, flats?, the three chord lists, the tunes, and the centres of the accompaniment. It also knows the occasion it suits
     *  (market, road, water, season, tavern, lament, festival, night, study, wedding, temple, war, or "master" for a masterpiece), for a season piece which season (0 spring .. 3 winter), and, for a
     *  masterpiece, a fourth part (C, between B and the return of A) and a fuller accompaniment. */
    static final class Piece {
        String title, style, harpCtr, padCtr, bassCtr;
        int bpm, tonic; double beats; int[] scale; boolean flats;
        String chordsA, chordsB, chordsA2, melA, melB, endA2;
        String occasion = "any"; int season = -1; boolean master = false;
        String chordsC, melC;
        Piece tag(String occasion) { this.occasion = occasion; return this; }
        Piece season(int s) { this.occasion = "season"; this.season = s; return this; }
        Piece master() { this.occasion = "master"; this.master = true; return this; }
        Piece third(String chordsC, String melC) { this.chordsC = chordsC; this.melC = melC; return this; }
        Piece(String title, int bpm, double beats, int tonic, int[] scale, boolean flats, String style, String harpCtr, String padCtr, String bassCtr, String chordsA, String chordsB, String chordsA2, String melA, String melB, String endA2) {
            this.title = title; this.bpm = bpm; this.beats = beats; this.tonic = tonic; this.scale = scale; this.flats = flats; this.style = style; this.harpCtr = harpCtr; this.padCtr = padCtr; this.bassCtr = bassCtr;
            this.chordsA = chordsA; this.chordsB = chordsB; this.chordsA2 = chordsA2; this.melA = melA; this.melB = melB; this.endA2 = endA2;
        }
    }

    static final int[] MAJOR = {0, 2, 4, 5, 7, 9, 11}, MAJOR_PENT = {0, 2, 4, 7, 9}, MINOR = {0, 2, 3, 5, 7, 8, 10}, MINOR_PENT = {0, 3, 5, 7, 10}, DORIAN = {0, 2, 3, 5, 7, 9, 10}, MIXO = {0, 2, 4, 5, 7, 9, 10},
        LYDIAN = {0, 2, 4, 6, 7, 9, 11}, HARM_MINOR = {0, 2, 3, 5, 7, 8, 11};

    private static final Piece[] PIECES = {
        new Piece("A Song for the Market Square", 100, 4, 72, MAJOR_PENT, false, "arp4", "C3", "G3", "C2",
            "C | Am | F | G | C | Am | F | G", "F | G | C | Am | F | G | C | C", "C | Am | F | G | C | Am | G | C",
            "3:1 3:.5 2:.5 1:1 2:1 | 3:1 5:1 4:2 | 5:1 4:.5 3:.5 2:1 1:1 | 2:2 4:1 2:1 | 3:1 3:.5 2:.5 1:1 3:1 | 5:1 5:.5 4:.5 3:2 | 4:1 3:1 2:1 3:1 | 2:4",
            "5:1 1':1 5:1 4:1 | 2':2 1':1 5:1 | 1':1 5:.5 4:.5 3:1 5:1 | 3:2 2:1 1:1 | 5:1 1':1 5:1 4:1 | 2':1 4':1 2':2 | 1':1 5:1 3:1 5:1 | 1:4",
            "4:1 2:1 4:1 2:1 | 1:4"),
        new Piece("The Ferryman's Tune", 84, 4, 62, DORIAN, false, "arp4", "D3", "A3", "D2",
            "Dm | C | G | Dm | Dm | C | G | Am", "F | C | Dm | Am | F | C | G | Dm", "Dm | C | G | Dm | Dm | C | G | Dm",
            "1:1 3:1 5:2 | 5:1 7:1 5:1 2:1 | 4:1 6:1 4:1 2:1 | 3:2 1:2 | 1:1 3:1 5:1 1':1 | 7:1 5:1 2:1 5:1 | 6:1 4:1 2:1 4:1 | 5:2 2:2",
            "5:2 3:1 5:1 | 7:2 5:2 | 1':2 7:1 5:1 | 5:1 2:1 5:2 | 3':1 1':1 7:1 5:1 | 5:1 7:1 1':2 | 6:2 4:1 2:1 | 1:4",
            "6:1 4:1 2:1 4:1 | 1:4"),
        new Piece("Autumn Moon Waltz", 84, 3, 69, MINOR, false, "arp3", "A2", "E3", "A1",
            "Am | Am | F | E | Am | Dm | E | Am", "F | G | Am | E | F | G | E | Am", "Am | Am | F | E | Am | Dm | Dm | Am",
            "5:1 3:.5 2:.5 1:1 | 3:1 5:1 1':1 | 6:1 5:.5 4:.5 3:1 | 2:2 5,:1 | 5:1 3:.5 2:.5 1:1 | 4:1 6:1 4:1 | 5:1 2:1 5,:1 | 1:3",
            "6:2 1':1 | 7:1 5:1 2:1 | 3':2 1':1 | 2':1 5:1 2:1 | 6:1 4:1 6:1 | 7:1 5:1 7:1 | 5:2 2:1 | 1:3",
            "4:1 6:1 4:1 | 1:3"),
        new Piece("Plum Blossom Snow", 60, 4, 76, MINOR_PENT, false, "arp4", "E3", "B3", "E2",
            "Em | Em | Am | Em | Em | C | D | Em", "G | D | Em | Em | C | D | Em | Em", "Em | Em | Am | Em | Em | C | D | Em",
            "4:2 3:1 2:1 | 1:3 r:1 | 3:1 2:1 1:2 | 2:2 4:2 | 4:2 5:1 4:1 | 3:1 2:1 1:2 | 5:2 4:1 3:1 | 1:4",
            "4:1 5:1 4':2 | 5:2 3:1 4:1 | 1':2 5:1 4:1 | 3:2 2:2 | 2':1 1':1 5:1 3:1 | 5:2 4:2 | 3:1 2:1 1:2 | 1:4",
            "5:2 3:2 | 1:4"),
        new Piece("Drinking Song of the Nine Cups", 116, 4, 67, MAJOR, false, "arp4", "G2", "D3", "G1",
            "G | G | C | D | G | Em | C | D", "C | G | Am | D | C | G | D | G", "G | G | C | D | G | Em | D | G",
            "1:.5 3:.5 5:1 3:.5 5:.5 1':1 | 5:1 3:1 1:2 | 4:.5 6:.5 1':1 6:.5 4:.5 6:1 | 5:2 2':2 | 1:.5 3:.5 5:1 3:.5 5:.5 1':1 | 3':1 1':1 6:2 | 4:.5 6:.5 1':1 6:.5 4:.5 6:1 | 5:2 7:1 5:1",
            "1':1 6:.5 4:.5 6:1 1':1 | 3':1 1':.5 5:.5 3:1 5:1 | 2':1 1':.5 6:.5 4:1 6:1 | 5:1 7:1 2':2 | 1':1 6:.5 4:.5 6:1 1':1 | 3':1 1':.5 5:.5 3:1 5:1 | 7:1 5:1 7:1 2':1 | 1':2 5:1 1:1",
            "5:1 7:1 5:1 2:1 | 1:4"),
        new Piece("The Widow's Lament", 56, 4, 62, MINOR, true, "arp4", "D3", "A3", "D2",
            "Dm | Bb | Gm | A | Dm | Bb | Gm | A", "F | C | Dm | A | F | C | Gm | A", "Dm | Bb | Gm | A | Dm | Bb | Gm | Dm",
            "5:2 3:1 1:1 | 1:2 3:1 1:1 | 4:2 6:1 1':1 | 5:3 r:1 | 5:2 3:1 5:1 | 4:2 3:1 1:1 | 4:1 6:1 1':2 | 5:2 2:1 5:1",
            "3':2 1':1 5:1 | 7:2 5:1 4:1 | 3:2 5:2 | 5:3 r:1 | 3':1 5:1 3:1 1:1 | 7:2 5:1 7:1 | 6:1 4:1 6:1 4:1 | 5:4",
            "4:2 6:1 4:1 | 1:4"),
        new Piece("Dance of the Paper Lanterns", 96, 2, 77, MAJOR_PENT, true, "arp68", "F3", "C4", "F2",
            "F | F | Bb | C | F | Dm | Bb | C", "Bb | C | F | Dm | Bb | C | F | F", "F | F | Bb | C | F | Dm | C | F",
            "1:1 3:1/3 5:1/3 3:1/3 | 5:1 3:2/3 1:1/3 | 5:1 4:1/3 3:1/3 2:1/3 | 4:2/3 2:1/3 4:1 | 1:1 3:1/3 5:1/3 3:1/3 | 3:1 5:1/3 3:1/3 1:1/3 | 2:1 3:1 | 4:2",
            "5:1 5:1/3 4:1/3 5:1/3 | 4':1 5:1/3 4:1/3 3:1/3 | 3:1 4:2/3 3:1/3 | 5:1 3:1 | 5:1 5:1/3 4:1/3 5:1/3 | 1':1 5:1/3 4:1/3 3:1/3 | 4:1 3:1/3 2:1/3 1:1/3 | 1:2",
            "2:1 4:1 | 1:2"),
        new Piece("River at Dawn", 66, 4, 70, MAJOR, true, "arp4", "Bb2", "F3", "Bb1",
            "Bb | Gm | Eb | F | Bb | Gm | Eb | F", "Eb | Bb | F | Gm | Eb | Bb | F | Bb", "Bb | Gm | Eb | F | Bb | Gm | F | Bb",
            "3:2 5:1 3:1 | 6:1 5:1 3:2 | 4:2 6:1 4:1 | 5:2 2:1 7,:1 | 3:1 5:1 1':2 | 6:1 5:1 4:1 3:1 | 4:1 3:1 2:1 4:1 | 5:4",
            "6:1 1':1 6:1 4:1 | 5:1 3:1 5:1 1':1 | 7:1 5:1 7:1 2':1 | 3':2 1':2 | 6:1 1':1 3':1 1':1 | 5':1 3':1 1':2 | 2':1 7:1 5:2 | 1:4",
            "5:1 7:1 2':1 7:1 | 1:4"),
        new Piece("The Wanderer's Return", 72, 3, 64, DORIAN, false, "arp3", "E3", "B3", "E2",
            "Em | D | A | Em | Em | D | A | Em", "G | A | Bm | Em | G | A | D | Em", "Em | D | A | Em | Em | D | A | Em",
            "1:1 3:1 5:1 | 7:1 5:1 7:1 | 6:2 4:1 | 5:2 3:1 | 3:1 5:1 1':1 | 7:1 2':1 7:1 | 6:1 4:1 6:1 | 1:3",
            "3':2 1':1 | 6':1 5:1 4:1 | 2':2 7:1 | 5:1 3:1 1:1 | 3:1 5:1 7:1 | 6:2 4:1 | 7:1 5:1 2:1 | 1:3",
            "4:1 6:1 4:1 | 1:3"),
        new Piece("Festival of Kites", 120, 4, 69, MAJOR, false, "arp4", "A2", "E3", "A1",
            "A | E | F#m | D | A | E | D | E", "D | A | E | F#m | D | A | E | A", "A | E | F#m | D | A | E | E | A",
            "1:.5 2:.5 3:.5 5:.5 3:1 1:1 | 5:.5 4:.5 3:.5 2:.5 7,:2 | 6:1 5:.5 3:.5 1:2 | 2:1 4:1 6:2 | 1:.5 2:.5 3:.5 5:.5 3:1 1:1 | 7:.5 6:.5 5:.5 4:.5 5:2 | 4:1 6:1 1':2 | 5:2 7:2",
            "4':1 3':.5 2':.5 1':1 6:1 | 3':1 5':1 3':1 1':1 | 5':2 3':1 2':1 | 1':1 6:1 3:2 | 4':1 2':1 4':2 | 3':1 1':1 5:2 | 2':1 7:1 5:2 | 1:4",
            "7:1 5:1 7:1 2':1 | 1:4"),
        new Piece("Night Watch Elegy", 52, 4, 60, MINOR, true, "arp4", "C3", "G3", "C2",
            "Cm | Ab | Eb | Bb | Cm | Ab | Fm | G", "Ab | Eb | Bb | Cm | Ab | Eb | G | Cm", "Cm | Ab | Eb | Bb | Cm | Ab | G | Cm",
            "5:2 3:1 1:1 | 3:2 1:1 6,:1 | 5:2 3:1 5:1 | 7:2 5:1 2:1 | 5:1 3:1 5:1 1':1 | 6:2 3:1 6:1 | 4:2 6:1 4:1 | 5:3 r:1",
            "3':2 1':1 6:1 | 5':1 3':1 5:2 | 2':2 7:1 5:1 | 3:1 5:1 1':2 | 3':1 1':1 6:2 | 5:1 3:1 5:2 | 2':2 5:1 2:1 | 1:4",
            "5:1 2:1 5:1 2':1 | 1:4"),
        new Piece("The Master's Last Lesson", 76, 4, 67, MIXO, false, "arp4", "G2", "D3", "G1",
            "G | F | C | G | G | F | C | Dm", "C | G | F | C | C | G | F | G", "G | F | C | G | G | F | C | G",
            "1:1 3:1 5:2 | 7:1 5:1 7:1 1':1 | 6:1 4:1 6:1 1':1 | 5:2 3:1 1:1 | 1:1 3:1 5:1 1':1 | 7:2 5:1 2:1 | 6:1 4:1 2:1 4:1 | 5:2 7:1 5:1",
            "1':1 6:1 4:2 | 5:1 3:1 5:1 1':1 | 2':2 1':1 7:1 | 6:2 4:2 | 1':1 3':1 1':1 6:1 | 5:1 3:1 5:2 | 7:1 2':1 4':2 | 1:4",
            "6:1 4:1 6:1 4:1 | 1:4"),
    };

    static {   // the occasions of the first twelve (their order above)
        String[] occ = {"market", "water", "season", "season", "tavern", "lament", "festival", "water", "road", "festival", "night", "study"};
        int[] seasons = {-1, -1, 2, 3, -1, -1, -1, -1, -1, -1, -1, -1};
        for (int i = 0; i < PIECES.length; i++) { if (occ[i].equals("season")) PIECES[i].season(seasons[i]); else PIECES[i].tag(occ[i]); }
    }

    private static volatile List<Score> parsed;
    private static volatile List<Piece> pieceList;
    private static volatile java.util.Map<String, int[]> byOccasion;

    /** The pieces, parsed (a piece with a problem is left out and listed in `problems`). */
    static final List<String> problems = new ArrayList<String>();

    /** Every piece in order: the twelve first pieces, the twenty-four of the occasions, the six masterpieces (the order is the number of `music concert` and the bit of the heard mask). */
    static synchronized List<Score> all() {
        if (parsed == null) {
            List<Score> l = new ArrayList<Score>();
            List<Piece> ps = new ArrayList<Piece>();
            java.util.List<Piece> src = new ArrayList<Piece>(java.util.Arrays.asList(PIECES));
            src.addAll(java.util.Arrays.asList(ConcertsMore.MORE));
            java.util.Map<String, List<Integer>> occ = new java.util.LinkedHashMap<String, List<Integer>>();
            for (Piece p : src) {
                Score s = Score.parse(lines(p));
                for (String pr : s.problems) problems.add(p.title + ": " + pr);
                if (!s.problems.isEmpty()) continue;
                List<Integer> li = occ.get(p.occasion);
                if (li == null) { li = new ArrayList<Integer>(); occ.put(p.occasion, li); }
                li.add(l.size());
                l.add(s); ps.add(p);
            }
            java.util.Map<String, int[]> m = new java.util.HashMap<String, int[]>();
            for (java.util.Map.Entry<String, List<Integer>> e : occ.entrySet()) { int[] a = new int[e.getValue().size()]; for (int i = 0; i < a.length; i++) a[i] = e.getValue().get(i); m.put(e.getKey(), a); }
            byOccasion = m; pieceList = ps;
            parsed = l;
        }
        return parsed;
    }

    static int count() { return all().size(); }

    /** The occasion (market, tavern, wedding, temple, war ...) of a piece by its number (0-based). */
    static String occasionOf(int index) { all(); return index >= 0 && index < pieceList.size() ? pieceList.get(index).occasion : "any"; }
    static boolean isMaster(int index) { all(); return index >= 0 && index < pieceList.size() && pieceList.get(index).master; }
    static int indexOf(Score s) { return all().indexOf(s); }
    static String[] OCCASIONS = {"market", "road", "water", "season", "tavern", "lament", "festival", "night", "study", "wedding", "temple", "war"};

    // ------------------------------------------------------------------ the pieces for a musician
    /** What a place calls for: a seat of a sect (temple, or war while it is at war), a city (festival, market, tavern, wedding, study or the season, by the quarter of the year), a spirit vein (temple),
     *  a haunted ground (a lament), a beast lair or a martial trial (war), a stretch of water (water) and the wilds (road, night in winter, the season). */
    static String occasionAt(Area a, Character c) {
        int q = Math.floorMod(c.id + GameDriver.time / 13, 6);
        if (a == null) return "road";
        if (a.sect != null && !a.sect.palace) return a.sect.warTarget != null ? "war" : "temple";
        if (a.city) return q == 0 ? "festival" : q == 1 ? "market" : q == 2 ? "tavern" : q == 3 ? "wedding" : q == 4 ? "study" : "season";
        if (a.spiritualVein) return "temple";
        if (a.dungeonType == Area.DungeonType.HAUNTED_GROUNDS) return "lament";
        if (a.dungeonType == Area.DungeonType.BEAST_LAIR || a.dungeonType == Area.DungeonType.MARTIAL_TRIAL) return "war";
        String g = (a.geoName != null ? a.geoName : a.name == null ? "" : a.name).toLowerCase();
        for (String w : new String[]{"lake", "river", "stream", "falls", "spring", "shore", "bay", "sea", "ocean", "marsh", "isle", "island", "tide"}) if (g.contains(w)) return "water";
        int season = (GameDriver.time % 52) / 13;
        return q < 2 ? "road" : q < 4 ? (season == 3 ? "night" : "season") : "lament";
    }

    /** The piece a musician plays at a place: the occasion the place calls for, then a piece of that occasion by his id and the quarter of the year (a player who watches hears a different piece in the next season).
     *  A musician of grade 8 or more (or with a Heaven-Sent instrument) plays one of the masterpieces now and then (one concert in five). */
    static Score pieceAt(Character c, Area a, int week, int grade, boolean heavenSent) {
        List<Score> all = all();
        if (all.isEmpty()) return null;
        int turn = Math.floorMod(c.id * 7 + week / 13, 1 << 20);
        int[] masters = byOccasion.get("master");
        if (masters != null && masters.length > 0 && (grade >= 8 || heavenSent) && Math.floorMod(c.id + week / 13, 5) == 0) return all.get(masters[turn % masters.length]);
        String occ = occasionAt(a, c);
        int[] pool = byOccasion.get(occ);
        if (pool == null || pool.length == 0) return all.get(turn % all.size());
        if (occ.equals("season")) {   // the four season pieces: the one of the quarter of the year
            int season = Math.floorMod(week, 52) / 13;
            for (int i : pool) if (pieceList.get(i).season == season) return all.get(i);
        }
        return all.get(pool[turn % pool.length]);
    }

    /** The piece a musician plays now where he stands. */
    static Score pieceFor(Character c) {
        Item inst = Life.bestInstrument(c);
        return pieceAt(c, c.location, GameDriver.time, level(inst), inst != null && inst.heavenSent);
    }

    /** A piece by number (1-based) or part of its title, or null. */
    static Score find(String key) {
        List<Score> all = all();
        try { int n = Integer.parseInt(key.trim()); if (n >= 1 && n <= all.size()) return all.get(n - 1); } catch (NumberFormatException ignored) { }
        for (Score s : all) if (s.title.toLowerCase().contains(key.trim().toLowerCase())) return s;
        return null;
    }

    /** The voice of an instrument (by its name: "Mortal-Grade Sandalwood Guqin"): the synthesiser's instrument that plays the lead of a concert. */
    static int leadFor(Item inst) { return leadForName(inst == null ? null : inst.name); }

    static int leadForName(String instName) {
        String n = instName == null ? "" : instName.toLowerCase();
        if (n.contains("guqin")) return Ambient.instIndex("pluck");
        if (n.contains("guzheng")) return Ambient.instIndex("pluck");
        if (n.contains("pipa")) return Ambient.instIndex("pipa");
        if (n.contains("erhu")) return Ambient.instIndex("erhu");
        if (n.contains("dizi")) return Ambient.instIndex("flute");
        if (n.contains("xiao")) return Ambient.instIndex("xiao");
        if (n.contains("sheng")) return Ambient.instIndex("sheng");
        if (n.contains("konghou")) return Ambient.instIndex("harp");
        if (n.contains("chime")) return Ambient.instIndex("chime");
        if (n.contains("drum")) return Ambient.instIndex("timpani");
        return Ambient.instIndex("erhu");
    }

    /** Skill of the instrument 1-10: its grade. */
    static int level(Item inst) { return inst == null ? 1 : Math.max(1, Math.min(10, inst.lvl)); }

    /** The tempo of a performance for a skill: 90% of the written tempo at grade 1, 108% at grade 10. */
    static double tempoFor(int level) { return 0.88 + 0.02 * Math.max(1, Math.min(10, level)); }

    /** The piece of the musician's repertoire, his instrument as the lead. It is heard only when the musician is a person who plays (an NPC's concert in your tile stays silent); nothing sounds when the music is off. */
    static String perform(Character musician, Item inst) {
        Score piece = pieceFor(musician);
        if (piece == null) return null;
        markHeard(musician, piece);
        if (!Settings.music || !Ambient.isRunning()) return piece.title;
        if (musician.player == 1) Ambient.concert(piece, leadFor(inst), level(inst));
        return piece.title;
    }

    // ------------------------------------------------------------------ writing a piece as a score
    private static final String[] SHARP_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"}, FLAT_NAMES = {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};

    private static final class Nt { int midi; double dur; String durText; boolean rest; }

    private static String name(int midi, boolean flats) { return (flats ? FLAT_NAMES : SHARP_NAMES)[Math.floorMod(midi, 12)] + (Math.floorDiv(midi, 12) - 1); }

    private static double parseDur(String s) {
        int slash = s.indexOf('/');
        return slash > 0 ? Double.parseDouble(s.substring(0, slash)) / Double.parseDouble(s.substring(slash + 1)) : Double.parseDouble(s);
    }

    /** The tune of one section as bars of notes. */
    private static List<List<Nt>> notes(String bars, Piece p) {
        List<List<Nt>> out = new ArrayList<List<Nt>>();
        for (String bar : bars.split("\\|")) {
            List<Nt> l = new ArrayList<Nt>();
            for (String tok : bar.trim().split("\\s+")) {
                int colon = tok.indexOf(':');
                Nt n = new Nt();
                n.durText = tok.substring(colon + 1); n.dur = parseDur(n.durText);
                String head = tok.substring(0, colon);
                if (head.equals("r")) n.rest = true;
                else {
                    int deg = head.charAt(0) - '0', oct = 0;
                    for (int i = 1; i < head.length(); i++) oct += head.charAt(i) == '\'' ? 1 : head.charAt(i) == ',' ? -1 : 0;
                    int idx = deg - 1, len = p.scale.length;
                    n.midi = p.tonic + p.scale[idx % len] + 12 * (idx / len + oct);
                }
                l.add(n);
            }
            out.add(l);
        }
        return out;
    }

    /** The neighbour of a note for a grace note or a trill: the next note of the scale above when it is no more than two semitones away, else the one below (the gaps of a pentatonic scale). */
    private static int above(int midi, Piece p) {
        int up = midi + 2;
        for (int m = midi + 1; m < midi + 6; m++) { int pc = Math.floorMod(m - p.tonic, 12); for (int s : p.scale) if (s == pc) { up = m; m = midi + 6; break; } }
        if (up - midi <= 2) return up;
        for (int m = midi - 1; m > midi - 6; m--) { int pc = Math.floorMod(m - p.tonic, 12); for (int s : p.scale) if (s == pc) return m; }
        return up;
    }

    private static String fmt(double d) { return String.format(Locale.ROOT, "%.2f", d); }

    /** mode 0: the tune as written; 1: with a grace note on every note of a beat or more; 2: and trills on the notes of two beats or more. */
    private static String line(List<List<Nt>> bars, Piece p, int mode) {
        StringBuilder sb = new StringBuilder();
        for (int b = 0; b < bars.size(); b++) {
            if (b > 0) sb.append(" | ");
            boolean first = true;
            for (Nt n : bars.get(b)) {
                if (!first) sb.append(' ');
                first = false;
                if (n.rest) { sb.append("r:").append(n.durText); continue; }
                String me = name(n.midi, p.flats);
                boolean plain = n.durText.indexOf('/') >= 0;
                if (mode == 0 || plain || n.dur < 1.0 - 1e-9) { sb.append(me).append(':').append(n.durText); continue; }
                String up = name(above(n.midi, p), p.flats);
                if (mode == 2 && n.dur >= 2.0 - 1e-9) {
                    int k = (int) Math.floor((n.dur - 1.0) / 0.5 + 1e-9);
                    for (int i = 0; i < k; i++) sb.append(me).append(":0.25 ").append(up).append(":0.25 ");
                    sb.append(me).append(':').append(fmt(n.dur - 0.5 * k));
                } else sb.append(up).append(":0.25 ").append(me).append(':').append(fmt(n.dur - 0.25));
            }
        }
        return sb.toString();
    }

    /** The text score of a piece. */
    private static String[] lines(Piece p) {
        List<String> l = new ArrayList<String>();
        l.add("title: " + p.title);
        l.add("kind: concert");
        l.add("layers: fixed");
        l.add("bpm: " + p.bpm);
        l.add("beats: " + (p.beats == Math.floor(p.beats) ? String.valueOf((int) p.beats) : String.valueOf(p.beats)));
        l.add("shift: -2 2");
        l.add(p.melC != null ? "form: A:mp B:mf C:f A2:mp" : "form: A:mp B:mf A2:mp");
        String arp = p.style.equals("arp4") ? "1:.5 5:.5 3':.5 5:.5 1':.5 5:.5 3':.5 5:.5" : p.style.equals("arp3") ? "1:.5 5:.5 3':.5 5:.5 3':.5 5:.5" : "1:1/3 5:1/3 3':1/3 1':1/3 3':1/3 5:1/3";
        String bass = p.style.equals("arp4") ? "b:2 5:2" : p.style.equals("arp3") ? "b:1 5:1 5:1" : "b:1 5:1";
        l.add("pattern arp: " + arp);
        l.add("pattern padN: =1+=3+=5:*");
        l.add("pattern bassP: " + bass);
        String lead = "inst=erhu vel=0.95 pan=0.00"; // (balanced with `SongTool balance`: the lead sits at its target level)
        l.add("track lead  role=lead " + lead);
        l.add("track leadf role=lead " + lead);
        l.add("track leadt role=lead " + lead);
        l.add("track harp  role=rhythm  inst=harp|pluck   vel=0.95 pan=-0.25 ctr=" + p.harpCtr + " pat=arp");
        l.add("track pad   role=harmony inst=pad|strings  vel=0.80 pan=0.15 ctr=" + p.padCtr + " pat=padN");
        l.add("track bass  role=bass    inst=cello|upright vel=1.40 pan=-0.10 ctr=" + p.bassCtr + " pat=bassP");
        if (p.master) l.add("track grand role=harmony inst=strings|choir vel=0.55 pan=0.25 ctr=" + p.padCtr + " pat=padN");   // a masterpiece has a fuller body under the tune
        List<List<Nt>> a = notes(p.melA, p), b = notes(p.melB, p);
        // A2: the first six bars of A, then the new ending
        List<List<Nt>> a2 = new ArrayList<List<Nt>>(a.subList(0, 6));
        a2.addAll(notes(p.endA2, p));
        List<String[]> sections = new ArrayList<String[]>();
        List<List<List<Nt>>> tunes = new ArrayList<List<List<Nt>>>();
        sections.add(new String[]{"A", p.chordsA}); tunes.add(a);
        sections.add(new String[]{"B", p.chordsB}); tunes.add(b);
        if (p.melC != null) { sections.add(new String[]{"C", p.chordsC}); tunes.add(notes(p.melC, p)); }
        sections.add(new String[]{"A2", p.chordsA2}); tunes.add(a2);
        for (int i = 0; i < sections.size(); i++) {
            boolean big = sections.get(i)[0].equals("B") || sections.get(i)[0].equals("C");
            l.add("[" + sections.get(i)[0] + "]");
            l.add("chords: " + sections.get(i)[1]);
            l.add("lead: " + line(tunes.get(i), p, 0));
            l.add("leadf: " + line(tunes.get(i), p, 1));
            l.add("leadt: " + line(tunes.get(i), p, 2));
            l.add("harp: arp x." + (big ? "9" : "7"));
            l.add("pad: padN x." + (big ? "8" : "6"));
            l.add("bass: bassP x.7");
            if (p.master) l.add("grand: padN x." + (sections.get(i)[0].equals("C") ? "7" : big ? "6" : "4"));
        }
        return l.toArray(new String[0]);
    }

    /** The text score of a piece by number (for the tools). */
    static String[] linesOf(int i) { all(); return lines(pieceList.get(i)); }

    // ------------------------------------------------------------------ performances going on, and listening to them
    /** A performance a person can hear: the musician, the place, the piece, the instrument and its grade, and the week it ends. */
    static final class Show {
        Character who; Area at; Score piece; int index, grade, until, distance; String instName;
        String describe() { return "\"" + piece.title + "\" played by " + who.name + " " + who.lastName + " on " + (instName == null ? "an old erhu" : instName) + " (grade " + grade + ")"; }
    }

    /** A musician plays here: the place remembers it for `weeks` weeks, and a person who plays and stands here is told (`settings concerts`). Four fields and a name: the piece is worked out only when somebody asks. */
    static void announce(Character c, Item inst, int weeks) {
        Area a = c.location;
        if (a == null || c.age < 8) return;
        boolean fresh = a.perfUntil <= GameDriver.time || a.perfBy != c.id;
        a.perfBy = c.id; a.perfStart = GameDriver.time; a.perfUntil = GameDriver.time + weeks;
        a.perfInst = inst == null ? null : inst.name; a.perfGrade = level(inst); a.perfHeaven = inst != null && inst.heavenSent;
        if (fresh && Settings.concertNotices && c.player != 1) tellListeners(c, a);
    }

    private static void tellListeners(Character m, Area a) {
        if (a.characters == null) return;
        for (Character h : new ArrayList<Character>(Notifications.humans())) {
            if (h == null || h.dead || h == m || h.location != a || h.age < 8) continue;
            Show s = showAt(a);
            if (s != null) Notifications.post("music", m.name + " " + m.lastName + " is playing here: " + s.describe() + ". 'listen' hears it.", m.id, h.id);
        }
    }

    /** The performance going on in a tile, or null. */
    static Show showAt(Area a) {
        if (a == null || a.perfUntil <= 0 || a.perfUntil < GameDriver.time) return null;
        Character m = Character.charById(a.perfBy);
        if (m == null || m.dead || m.location != a) return null;
        Score p = pieceAt(m, a, a.perfStart, a.perfGrade, a.perfHeaven);
        if (p == null) return null;
        Show s = new Show();
        s.who = m; s.at = a; s.piece = p; s.index = all().indexOf(p); s.instName = a.perfInst; s.grade = Math.max(1, a.perfGrade); s.until = a.perfUntil;
        return s;
    }

    /** The performances within `radius` tiles of the person, the nearest first. */
    static List<Show> near(Character c, int radius) {
        List<Show> out = new ArrayList<Show>();
        if (c == null || c.location == null) return out;
        int range = Map.range;
        for (int dx = -radius; dx <= radius; dx++) for (int dy = -radius; dy <= radius; dy++) {
            if (Math.abs(dx) + Math.abs(dy) > radius) continue;
            Area a = Map.tiles[Math.floorMod(c.location.x + dx, range)][Math.floorMod(c.location.y + dy, range)];
            Show s = showAt(a);
            if (s == null || s.who == c) continue;
            s.distance = Math.abs(dx) + Math.abs(dy);
            out.add(s);
        }
        out.sort((x, y) -> Integer.compare(x.distance, y.distance));
        return out;
    }

    static void markHeard(Character c, Score piece) { int i = all().indexOf(piece); if (c != null && i >= 0 && i < 63) c.heardConcerts |= 1L << i; }
    static boolean heardBy(Character c, int index) { return c != null && index >= 0 && index < 63 && (c.heardConcerts & (1L << index)) != 0; }
    static int heardCount(Character c) { return c == null ? 0 : Long.bitCount(c.heardConcerts); }

    /** The text of `concerts`: what is playing within 25 tiles, and how many pieces the person has heard. */
    static String listText(Character c) {
        List<Show> shows = near(c, 25);
        StringBuilder sb = new StringBuilder("Performances going on within 25 tiles");
        sb.append(shows.isEmpty() ? ": none just now (a Musician plays in cities, at the seats of sects and on the road).\n" : ":\n");
        int n = 1;
        for (Show s : shows) {
            sb.append("  ").append(n++).append(". ").append(s.describe()).append(" at ").append(s.at.name).append(" ").append(PlayerControl.coord(s.at))
              .append(s.distance == 0 ? "  (here: 'listen')" : "  " + s.distance + " tiles away ('goto' it, then 'listen')").append(heardBy(c, s.index) ? "  [heard]" : "  [new to you]").append('\n');
        }
        sb.append("You have heard ").append(heardCount(c)).append(" of the ").append(count()).append(" pieces ('music concerts' lists them all; 'music concert <n>' plays one again).");
        return sb.toString();
    }

    /** Listens to the performance in the person's tile (`n` = 0), or the n-th of the list when it is in the same tile. Returns the text; `spent[0]` is set when the week passes. */
    static String listen(Character c, int n, boolean[] spent) {
        Show s;
        if (n <= 0) s = showAt(c.location);
        else { List<Show> l = near(c, 25); s = n <= l.size() ? l.get(n - 1) : null; if (s != null && s.at != c.location) return "That one is " + s.distance + " tiles away: go there first ('goto " + s.at.x + " " + s.at.y + "'), then 'listen'."; }
        if (s == null) return "Nobody is playing here. 'concerts' lists the performances within 25 tiles.";
        if (s.who == c) return "That is you: 'play' or 'perform' gives your own concert.";
        boolean fresh = !heardBy(c, s.index);
        markHeard(c, s.piece);
        if (c.isManualPc() && Settings.music && Ambient.isRunning()) Ambient.concert(s.piece, leadForName(s.instName), s.grade);
        Life.Fx f = Life.fx(c);
        f.inspiredWeeks = Math.max(f.inspiredWeeks, 4); f.inspiredPow = Math.max(f.inspiredPow, 0.05);
        if (c.lastListened != s.who.id + 1) { c.lastListened = s.who.id + 1; c.changeRelationship(s.who, 1); }
        if (spent != null) spent[0] = true;
        String hear = c.isManualPc() && Settings.music && Ambient.isRunning() ? "" : " (the music is off: 'music on' to hear it)";
        return "You sit and listen to " + s.describe() + ". A glow of calm stays with you for a few weeks" + (fresh ? "; it is a piece you had not heard before (" + heardCount(c) + " of " + count() + ")." : ".") + hear;
    }
}
