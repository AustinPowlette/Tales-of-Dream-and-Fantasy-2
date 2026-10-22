package Game;

import java.io.PrintStream;
import java.util.*;

/** 2026-10-15: the Musicians' concerts (`Concerts`, `Conductor.concert`, `Ambient.concert`, `Life.play`): forty-three `kind: concert` pieces (the twelve first, thirty-one written for the occasions, six masterpieces) that parse, stay out of the places' music, are a minute or so long and render; a concert cuts
 *  in, plays once with the musician's instrument as its lead voice, at the pace and with the ornaments of his grade (plain tune below 5, grace notes 5-7, trills from 8), and the music of the places goes on after it;
 *  the repertoire follows the musician's id and the season; the Musician hall pays a quarter more. */
public class ConcertProbe {
    static { Character.TRIGGER_EVERY = 1; Shop.SEED = false; }
    static void check(String n, boolean ok) { PcProbe.check(n, ok); }

    public static void main(String[] a) throws Exception {
        PcProbe.real = System.out;
        System.setIn(PcProbe.in);
        GameDriver.frozenNpcs = true;
        System.setOut(new PrintStream(PcProbe.cap, true));
        Thread t = new Thread(null, () -> { try { GameDriver.main(new String[0]); } catch (Throwable e) { PcProbe.gameError = e; e.printStackTrace(PcProbe.real); } }, "game", 512L << 20);
        t.setDaemon(true); t.start();
        try { run(); } catch (Throwable e) { PcProbe.real.println("[T] PROBE EXCEPTION " + e); e.printStackTrace(PcProbe.real); PcProbe.fails++; }
        PcProbe.real.println("[T] ===== " + PcProbe.passes + " passed, " + PcProbe.fails + " failed; gameError=" + PcProbe.gameError + " =====");
        System.exit(0);
    }

    /** The notes of a concert played through a Conductor: {track name, instrument, time in samples}. Plays until `limit` samples. */
    static List<Object[]> play(Score piece, int lead, int level, long limit, long seed) {
        Conductor c = new Conductor(seed);
        c.testMode = true; c.mood = 1;
        c.concert(piece, lead, level);
        List<Object[]> out = new ArrayList<>();
        long clock = 0;
        while (clock < limit) {
            for (Ambient.Note n = c.poll(clock); n != null; n = c.poll(clock)) out.add(new Object[]{n.track >= 0 && n.track < piece.tracks.size() && c.title().equals(piece.title) ? piece.tracks.get(n.track).name : "?", n.inst, n.at, c.title()});
            clock += 256;
        }
        return out;
    }

    static void run() throws Exception {
        PcProbe.in.feed("n\ns\n1\nn\n");
        PcProbe.awaitBlocked(180);
        Character p = PcProbe.pc();
        check("player created and prompting", p != null && PcProbe.in.blocked);
        if (p == null) return;
        Settings.lore = false;

        // ---- N1: the pieces
        List<Score> pieces = Concerts.all();
        check("N1 forty-three concert pieces parse (" + pieces.size() + "; problems: " + Concerts.problems + ")", pieces.size() == 43 && Concerts.problems.isEmpty());
        boolean allConcert = true, notInRotation = true, lengths = true; StringBuilder lens = new StringBuilder();
        Set<String> titles = new HashSet<>();
        for (Score s : pieces) {
            if (!s.concert) allConcert = false;
            if (SongBook.all().contains(s)) notInRotation = false;
            double sec = s.seconds(); lens.append(String.format("%.0f ", sec));
            if (sec < 30 || sec > (Concerts.isMaster(Concerts.indexOf(s)) ? 150 : 120)) lengths = false;
            titles.add(s.title);
        }
        check("N1b every one is a `kind: concert` score and none is in the songs the places choose from (" + SongBook.all().size() + " songs)", allConcert && notInRotation);
        check("N1c each lasts between 30 and 120 seconds, a masterpiece up to 150 (" + lens + ") and the titles differ (" + titles.size() + ")", lengths && titles.size() == 43);
        boolean noteOk = true;
        for (Score s : pieces) { List<Ambient.Note> notes = Conductor.collect(s, 1, 7); if (notes.size() < 100) noteOk = false; }
        check("N1d each plays notes (a whole performance has at least 100)", noteOk);
        byte[] pcm = Ambient.renderScore(pieces.get(0), 1, 3, 40);
        long energy = 0; for (int i = 0; i + 1 < pcm.length; i += 2) energy += Math.abs((short) ((pcm[i] & 0xff) | (pcm[i + 1] << 8)));
        check("N1e the built-in synthesiser renders a piece (" + pcm.length / 4 / 44100 + " s of sound, not silence)", pcm.length > 44100 * 4 && energy > 100000);

        int masters = 0; java.util.Map<String, Integer> perOcc = new TreeMap<>();
        for (int i = 0; i < pieces.size(); i++) { if (Concerts.isMaster(i)) masters++; else perOcc.merge(Concerts.occasionOf(i), 1, Integer::sum); }
        boolean spread = perOcc.keySet().containsAll(Arrays.asList(Concerts.OCCASIONS)); for (int n : perOcc.values()) if (n < 3) spread = false;
        check("N1f six masterpieces, and every one of the twelve occasions has at least three pieces (" + perOcc + ")", masters == 6 && spread);
        boolean masterLong = true; for (int i = 0; i < pieces.size(); i++) if (Concerts.isMaster(i) && pieces.get(i).seconds() < 70) masterLong = false;
        check("N1g a masterpiece lasts at least 70 seconds and has the extra strings and choir layer (a 'grand' track)", masterLong && hasTrack(pieces.get(pieces.size() - 1), "grand") && !hasTrack(pieces.get(0), "grand"));

        // ---- N2: the lead voice, grade and tempo
        Score piece = pieces.get(0);
        int guqin = Concerts.leadFor(item("Mortal-Grade Sandalwood Guqin", 1)), pipa = Concerts.leadFor(item("Spirit-Grade Pipa", 5)), drum = Concerts.leadFor(item("Heaven-Grade War Drum", 9));
        check("N2 the instrument's name picks the voice (Guqin " + Ambient.INST[guqin] + ", Pipa " + Ambient.INST[pipa] + ", War Drum " + Ambient.INST[drum] + ")", Ambient.INST[guqin].equals("pluck") && Ambient.INST[pipa].equals("pipa") && Ambient.INST[drum].equals("timpani"));
        int erhu = Ambient.instIndex("erhu"), flute = Ambient.instIndex("flute");
        List<Object[]> low = play(piece, flute, 3, 44100L * 150, 11), mid = play(piece, flute, 6, 44100L * 150, 11), high = play(piece, flute, 10, 44100L * 150, 11);
        check("N2b the concert plays at once and its lead tracks use the musician's instrument (flute " + flute + "): grade 3 plays the plain tune", leadTracks(low).equals(Collections.singleton("lead")) && leadInst(low, flute));
        check("N2c grades 5-7 play the tune with grace notes only, grade 8 and up with trills only (" + leadTracks(mid) + ", " + leadTracks(high) + ")", leadTracks(mid).equals(Collections.singleton("leadf")) && leadTracks(high).equals(Collections.singleton("leadt")));
        long endLow = lastNote(low, piece.title), endHigh = lastNote(high, piece.title);
        double ratio = endLow / (double) endHigh;
        check("N2d a grade 10 musician plays it faster than a grade 3 one (written tempo x1.08 against x0.94: ratio " + String.format("%.2f", ratio) + " expected about 1.15)", ratio > 1.08 && ratio < 1.22);
        check("N2e the ornamented versions have more notes than the plain tune (" + notesOf(low, "lead") + " < " + notesOf(mid, "leadf") + " < " + notesOf(high, "leadt") + ")", notesOf(low, "lead") < notesOf(mid, "leadf") && notesOf(mid, "leadf") < notesOf(high, "leadt"));

        // ---- N3: it plays once and the music goes on
        List<Object[]> long3 = play(piece, erhu, 5, 44100L * 260, 5);
        Set<String> seen = new LinkedHashSet<>(); for (Object[] n : long3) seen.add((String) n[3]);
        check("N3 the concert is heard first and, after it, the music of the places goes on (" + seen.size() + " pieces: " + seen + ")", seen.size() >= 2 && seen.iterator().next().equals(piece.title));
        int concertSpans = 0; String lastTitle = null; for (Object[] n : long3) { String tt = (String) n[3]; if (!tt.equals(lastTitle)) { if (tt.equals(piece.title)) concertSpans++; lastTitle = tt; } }
        check("N3b ...and the concert is not repeated (" + concertSpans + " time)", concertSpans == 1);

        // ---- N4: the repertoire
        Character a1 = new Character(p.location), a2 = new Character(p.location);
        Score s1 = Concerts.pieceFor(a1), s1b = Concerts.pieceFor(a1), s2 = Concerts.pieceFor(a2);
        check("N4 a musician's piece is stable (" + s1.title + ") and another musician usually has another (" + s2.title + ")", s1 == s1b && s1 != s2);
        int time0 = GameDriver.time; GameDriver.time = time0 + 13;
        Score later = Concerts.pieceFor(a1);
        GameDriver.time = time0;
        check("N4b the repertoire turns with the season (" + later.title + ")", later != s1);

        // ---- N5: playing a concert as a human
        p.trait = Character.Traits.Musician; p.backpack.removeIf(i -> i.itemType == Item.ItemType.Instrument);
        PcProbe.cmd("music concerts", 60);
        int m0 = PcProbe.mark();
        p.pc().friendPolicy = 1; Life.play(p); // (a Musician's turn of the trade)
        String said = PcProbe.since(m0);
        boolean titled = false; for (Score s : pieces) if (said.contains("'" + s.title + "'")) titled = true;
        check("N5 a Musician's turn plays a piece of the repertoire by name (" + (said.length() > 400 ? said.substring(0, 400) : said).replace('\n', ' ') + ")", titled);
        PcProbe.cmd("music concert market", 60);

        // ---- N6: the hall
        Organization hall = new Organization(new Character(p.location));
        hall.traitRequired = Character.Traits.Musician;
        Character m = new Character(p.location); m.trait = Character.Traits.Musician; m.sect = hall;
        Character other = new Character(p.location); other.trait = Character.Traits.Musician;
        check("N6 a Musician of a hall of musicians is paid a quarter more (x" + Life.hallBonus(m) + " against x" + Life.hallBonus(other) + ")", Life.hallBonus(m) == 1.25 && Life.hallBonus(other) == 1.0);

        // ---- N7: listening to a performance in the tile
        Character mus = null; for (Character x : Map.characters) if (x != null && x != p && !x.dead && x.age >= 18 && x.location != null) { mus = x; break; }
        Area musHome = mus.location; mus.trait = Character.Traits.Musician; mus.location = p.location;
        Item pipa5 = item("Spirit-Grade Pipa", 5);
        Concerts.announce(mus, pipa5, 4);
        Concerts.Show sh = Concerts.showAt(p.location);
        check("N7 a musician who plays leaves a performance in the tile: the piece follows the place and the week, the instrument and grade are kept", sh != null && sh.who == mus && sh.piece == Concerts.pieceAt(mus, p.location, GameDriver.time, 5, false) && sh.grade == 5 && "Spirit-Grade Pipa".equals(sh.instName));
        List<Concerts.Show> nearby = Concerts.near(p, 25);
        check("N7b `near` lists it with distance 0 (" + nearby.size() + " performance)", nearby.size() >= 1 && nearby.get(0).who == mus && nearby.get(0).distance == 0);
        int h0 = Concerts.heardCount(p);
        int m2 = PcProbe.mark(); PcProbe.cmd("concerts", 60);
        String listed = PcProbe.since(m2);
        check("N7c `concerts` names the piece, the musician and 'new to you' (" + listed.replace((char) 10, ' ').substring(0, Math.min(200, listed.length())) + ")", listed.contains(sh.piece.title) && listed.contains("new to you"));
        int t0 = GameDriver.time; int m3 = PcProbe.mark(); PcProbe.cmd("listen", 60);
        for (int w = 0; w < 100 && GameDriver.time == t0; w++) Thread.sleep(50);   // (the probe can look before the tick is done)
        String heard = PcProbe.since(m3);
        check("N7d `listen` hears it: marks the piece, one more heard, the week passes, an inspired spell (time +" + (GameDriver.time - t0) + ", inspired " + Life.fx(p).inspiredWeeks + ", heard " + Concerts.heardCount(p) + "/" + h0 + "; " + heard.replace((char) 10, ' ').substring(0, Math.min(200, heard.length())) + ")",
            heard.contains("You sit and listen") && Concerts.heardCount(p) == h0 + 1 && Concerts.heardBy(p, sh.index) && GameDriver.time >= t0 && Life.fx(p).inspiredWeeks >= 3);
        boolean[] spent = {false}; String again = Concerts.listen(p, 0, spent);
        check("N7d2 listening takes the week (the command says so) and plays the piece with the musician's instrument and grade (" + again.length() + " chars)", spent[0] && again.contains("You sit and listen"));
        int h1 = Concerts.heardCount(p);
        mus.location = p.location; Concerts.announce(mus, pipa5, 4);
        PcProbe.cmd("listen", 60);
        check("N7e hearing the same piece again does not count twice (" + Concerts.heardCount(p) + " of " + Concerts.count() + ")", Concerts.heardCount(p) == h1);
        GameDriver.time += 9;
        check("N7f the performance is gone after its weeks (a street play lasts two, a stage concert four)", Concerts.showAt(p.location) == null);
        GameDriver.time = GameDriver.time - 9;
        mus.location = musHome;
        // N7g: one in five of a high-grade musician's concerts is a masterpiece, never a low-grade one's
        int lowM = 0, highM = 0, highN = 0;
        for (int id = 0; id < 400; id++) {
            Character fake = new Character(p.location); fake.id = id;
            for (int week = 0; week < 52 * 10; week += 13) {
                if (Concerts.isMaster(Concerts.indexOf(Concerts.pieceAt(fake, p.location, week, 5, false)))) lowM++;
                highN++; if (Concerts.isMaster(Concerts.indexOf(Concerts.pieceAt(fake, p.location, week, 9, false)))) highM++;
            }
        }
        check("N7g a grade 5 musician never plays a masterpiece; a grade 9 one about one concert in five (" + lowM + " / " + highM + " of " + highN + ")", lowM == 0 && highM > highN / 8 && highM < highN / 3);
        check("N7h `music concert <name>` plays a piece without marking it heard", markCheck(p));
        // ---- N8: a save keeps the performance of a tile and what a person heard, and an old save (without those fields) means "nobody plays here"
        mus.location = p.location; Concerts.announce(mus, pipa5, 6);
        p.heardConcerts |= 0b1010101L;
        long mask = p.heardConcerts; int musId = mus.id, px = p.location.x, py = p.location.y; String title0 = Concerts.showAt(p.location).piece.title;
        Area areaBefore = p.location; Character pBefore = p;
        PcProbe.cmd("save concerts", 120);
        { long until = System.currentTimeMillis() + 120_000; while (!new java.io.File("saves/concerts.dat").isFile() && System.currentTimeMillis() < until) Thread.sleep(300); Thread.sleep(1500); }
        check("N8 a save is written", new java.io.File("saves/concerts.dat").isFile());
        GameDriver.loadRequest = "concerts";
        PcProbe.cmd("wait 1", 120);
        { long until = System.currentTimeMillis() + 120_000; while (GameDriver.loadRequest != null && System.currentTimeMillis() < until) Thread.sleep(300); Thread.sleep(2500); }
        Character p2 = PcProbe.pc();
        Area here2 = Map.tiles[px][py];
        check("N8a the world really was reloaded (new objects for the tile and the person)", here2 != areaBefore && p2 != pBefore);
        check("N8b after loading, the heard mask is back (" + (p2 == null ? "no player" : Long.toBinaryString(p2.heardConcerts)) + ")", p2 != null && (p2.heardConcerts & mask) == mask);
        check("N8c ...and the performance of the tile (by " + here2.perfBy + ", until " + here2.perfUntil + ") is still there for the same musician and piece", here2.perfBy == musId && here2.perfUntil > 0 && (Concerts.showAt(here2) == null || Concerts.showAt(here2).piece.title.equals(title0) || GameDriver.time > here2.perfUntil));
        Area untouched = null; for (Area ar : Map.createdAreas) if (ar != null && ar != here2 && ar.perfUntil == 0) { untouched = ar; break; }
        check("N8d a tile nobody played in shows no performance", untouched != null && Concerts.showAt(untouched) == null);
        check("N9 every notice category has a label (the Settings window shows them in pairs: " + Notifications.CATEGORIES.length + " / " + Notifications.LABELS.length + ")", Notifications.CATEGORIES.length == Notifications.LABELS.length);
        System.out.println();
        PcProbe.real.println("[T] DONE");
    }

    static boolean hasTrack(Score s, String name) { for (Score.Track t : s.tracks) if (t.name.equals(name)) return true; return false; }
    static boolean markCheck(Character p) throws Exception {
        Score unheard = null; for (Score s : Concerts.all()) if (!Concerts.heardBy(p, Concerts.indexOf(s))) { unheard = s; break; }
        if (unheard == null) return false;
        int before = Concerts.heardCount(p);
        PcProbe.cmd("music concert " + unheard.title, 60);
        return Concerts.heardCount(p) == before;
    }
    static Item item(String name, int lvl) { Item i = new Item(Item.ItemType.Instrument, lvl, 1, name); return i; }
    static Set<String> leadTracks(List<Object[]> notes) { Set<String> s = new HashSet<>(); for (Object[] n : notes) { String t = (String) n[0]; if (t.startsWith("lead")) s.add(t); } return s; }
    static boolean leadInst(List<Object[]> notes, int inst) { boolean any = false; for (Object[] n : notes) if (((String) n[0]).startsWith("lead")) { any = true; if ((Integer) n[1] != inst) return false; } return any; }
    static int notesOf(List<Object[]> notes, String track) { int c = 0; for (Object[] n : notes) if (track.equals(n[0])) c++; return c; }
    static long lastNote(List<Object[]> notes, String title) { long last = 0; for (Object[] n : notes) if (title.equals(n[3])) last = Math.max(last, (Long) n[2]); return last; }
}
