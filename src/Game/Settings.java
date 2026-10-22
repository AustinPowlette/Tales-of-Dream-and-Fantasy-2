package Game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/** User preferences, kept in settings.properties next to the game (git-ignored). Static on purpose: the GUI, the save system and the
 *  notification code all read them. Every setter goes through save() so a crash never loses a change. */
public class Settings {
    static File file = new File("settings.properties");

    // saving
    static volatile int autosaveYears = 10;      // autosave every N game years (0 = never)
    static volatile int autosaveKeep = 3;        // rotating autosave slots kept
    // display
    static volatile double fontScale = 1.0;      // GUI text size multiplier
    static volatile boolean colorblindMap = false; // colour-blind-safe map palette
    static volatile boolean pictures = true;     // place pictures
    static volatile boolean paintedArt = true;   // paint a picture for places without hand-made art (ArtGen)
    static volatile boolean artFresh = false;    // new random pictures every launch (otherwise the saved seed is reused)
    static volatile long artSeed = 0;            // 0 = pick one at first launch
    static volatile boolean tooltips = true;
    static volatile boolean hints = true;        // one-time contextual hints (and the rotating 'try:' tip of the terminal status block)
    static volatile int scrollSpeed = 5;           // the mouse-wheel step of every scroll pane, 1-10 (Ui.scrollUnit)
    static volatile boolean fightView = true;     // the fight window opens by itself when a fight you were in is over (the game waits for it; on by default); off: watch a fight afterwards from the Fights window
    static volatile boolean vignettes = true;     // the little stories (Vignettes): short scenes on the road and in the markets; not tied to `lore` or `dialogue`
    static volatile boolean lessons = true;       // the casual talks that teach the game (Lessons); not tied to `dialogue`
    static volatile boolean tileQualityCap = true; // gathering: the quality of what a tile yields is capped at its level / 10 + 2 (`settings tilecap off` lifts it)
    static volatile boolean worldNews = true;    // console lines about the wider world (a tournament won, a city founded, a trial closed): `settings worldnews off` hides them (the Chronicle keeps them)
    static volatile boolean concertNotices = true;  // a notice when a musician starts playing in the tile of a person who plays ('listen' hears it): `settings concerts off` stops them
    static volatile boolean gardenGames = true;  // the Garden's Variety Tournament at year 10 and every 100 years after (GardenTournament): `settings garden off` skips it (it takes a few seconds in a big world)
    static volatile boolean hud = true;          // terminal only: a status block (name, realm, place, time, mode, alerts, next quest) before each prompt
    static volatile int startSpread = 21;        // a new big world (20,000+ people) starts with everybody spread over this many tiles across (odd; 0 = the old 3 x 3 zone around Originis): the first ticks are 10x faster. 21 puts about 230 people on a tile (15, the old default, put 450)
    // the epoch (a purge of the crowded world; see GameDriver): it comes when this many cultivators live, or when a typical turn takes longer than the limit below (a slower computer is purged sooner)
    static volatile int startPopulation = 100000;   // the number of people a NEW world begins with (asked at the start; recommended 10,000 to 150,000)
    static volatile int epochPopulation = 200000;   // the most cultivators the world holds before the epoch (used when epochCustom is on)
    static volatile int epochMemoryPercent = 70;    // the epoch also comes when more than this percent of the memory the game may use is alive (0 = never for that reason)
    static volatile boolean epochCustom = false;    // off: the epoch comes at twice the starting population (200,000 in the usual world); on: at epochPopulation
    /** The number of cultivators at which the epoch comes. */
    static int epochLimit() { return epochCustom ? epochPopulation : Math.max(1000, 2 * Map.startingPopulation); }
    static volatile double epochTickSeconds = Integer.getInteger("tdf.epochMs", 2000) / 1000.0; // seconds a typical turn may take (in a world of 10,000+) before the epoch
    static volatile int epochCullPercent = 20;      // percent of each realm's cultivators the epoch takes (realms with fewer than 10 are spared)
    static volatile int epochCullNumber = 0;        // above 0: the epoch takes this many cultivators in all (shared out over the realms by size) and the percent is not used
    static volatile int epochMaxRealm = 3;          // the epoch culls (and then blesses) only the realms from 0 up to this Major Lvl; the higher realms are left alone
    static volatile double crowdFactor = 3.5;       // a tile is overcrowded above this many times its fair share of the STARTING population (people / tiles); a city holds four times as much (see GameDriver.crowdFor)
    static volatile int[] leavePct = {20, 15, 10, 25};   // percent of the non-seekers who leave the Garden when they first reach Ascension, Half-Immortal, Pseudo Immortal (they just go) and True Immortal (they carry a lamp)
    static volatile int leaveBless = 2;             // fighting level and potential a descendant of one who left below True Immortal carries (a lamp-bearer gives 5)
    static volatile int spoilRefundPercent = 100;   // goods that spoil or are scrapped pay their owner (shop owner, city lord / treasury, carrier) this percent of their price back in spirit stones
    static volatile boolean parallelNpcs = Boolean.getBoolean("tdf.parallel"); // EXPERIMENTAL: the turns of the NPCs on different tiles run together on several cores (see GameDriver.parallelNpcPhase); off by default
    static volatile boolean lore = true;         // the story of the Palace of the Oldest Order (see Lore): a new world gets the Palace at Originis; off = nothing of it runs or shows
    static volatile boolean lorePause = true;    // GUI: the game waits while a lore scene is open (until Continue)
    static volatile boolean dialogueFlavor = true;  // what people say to you shows as a line in the log and the terminal (see Dialogue)
    static volatile boolean dialoguePopups = true;  // GUI: a window with the speaker's portrait; replies to choose from, and the Talk conversation
    static volatile boolean dialogueAuto = false;   // an AUTO or script-run character also has the branching talks and conversations (it answers by its own temper), logged; off = only the flavor line
    static volatile boolean firstRunDone = false;
    // safety
    static volatile boolean confirmRisky = true; // ask before irreversible actions
    // notifications
    static volatile boolean toasts = true;
    static volatile boolean sound = false;
    static Set<String> mutedNotifications = set("level"); // categories switched off (see Notifications.CATEGORIES); level-ups start muted
    static volatile int maxRandomChallenges = 2;     // challenges per turn that strangers and dislikers may make against a human (rivals, arch-enemies and war fights always count)
    static volatile boolean matureStart = true;     // a new human (not a Story start) begins with the cultivation of their years from 8 to 18 instead of at level 1
    static volatile boolean digestOnLoad = true;    // a "what happened lately" window when a saved game opens
    static volatile boolean music = false;          // the game's songs (see Ambient, Conductor, SongBook)
    static volatile int musicVolume = 40;           // 0-100
    static volatile String musicEngine = "auto";    // "auto" (a SoundFont from soundfonts/ when there is one), "soundfont" or "builtin" (the built-in synthesiser)
    static volatile String soundFont = "";          // file name inside soundfonts/ ("" = the first one found)
    static volatile Set<String> musicPlaylist = new java.util.LinkedHashSet<String>(); // the titles on the Music Manager's playlist (empty: every song is played; see Playlist)
    static volatile boolean musicFilter = false;     // a playlist is in use (musicPlaylist may then even be empty: the conductor falls back to every song so the music never goes silent)
    static volatile int musicGenerated = 0;         // 0-100: how often a song newly composed by SongGen (not yet kept) takes a slot; the written and the kept songs fill the rest
    static volatile String musicMix = "";           // per-instrument loudness, e.g. "pluck=80,taiko=0" (see Ambient.INST)
    static volatile boolean autoEquipManual = false; // false: in MANUAL mode the game never swaps your gear for you (empty slots are still filled)
    static volatile boolean highContrast = false; // black/white/yellow palette (restart to apply)
    static volatile boolean rememberWindows = true; // pop-up windows reopen where and how big you left them (GuiHub.restoreBounds)
    static final java.util.Map<String, String> windowBounds = new java.util.concurrent.ConcurrentHashMap<>(); // window key -> "x,y,w,h"
    static Set<String> pauseOn = set("");           // notice categories (or "attack") that pause the game in the GUI
    // recap
    static volatile int recapWeeks = 52;         // summary popup every N weeks (0 = off; 13 = each season; 52 = each year)
    // misc
    static Set<String> shownHints = set("");
    static volatile String exportDir = "exports";

    private static boolean loaded = false;

    static synchronized void load() {
        loaded = true;
        if (!file.isFile()) { applyArt(); return; }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(file)) { p.load(in); } catch (Exception e) { return; }
        autosaveYears = i(p, "autosaveYears", autosaveYears, 0, 1000);
        autosaveKeep = i(p, "autosaveKeep", autosaveKeep, 1, 20);
        fontScale = d(p, "fontScale", fontScale, 0.7, 2.5);
        colorblindMap = b(p, "colorblindMap", colorblindMap);
        pictures = b(p, "pictures", pictures);
        paintedArt = b(p, "paintedArt", paintedArt);
        artFresh = b(p, "artFresh", artFresh);
        try { artSeed = Long.parseLong(p.getProperty("artSeed", "0").trim()); } catch (Exception e) { artSeed = 0; }
        tooltips = b(p, "tooltips", tooltips);
        hints = b(p, "hints", hints);
        hud = b(p, "hud", hud);
        worldNews = b(p, "worldNews", worldNews);
        gardenGames = b(p, "gardenGames", gardenGames);
        concertNotices = b(p, "concertNotices", concertNotices);
        tileQualityCap = b(p, "tileQualityCap", tileQualityCap);
        lessons = b(p, "lessons", lessons);
        vignettes = b(p, "vignettes", vignettes);
        fightView = b(p, "fightView", fightView);
        try { scrollSpeed = Math.max(1, Math.min(10, Integer.parseInt(p.getProperty("scrollSpeed", String.valueOf(scrollSpeed))))); } catch (NumberFormatException e) { }
        try { startSpread = Math.max(0, Math.min(31, Integer.parseInt(p.getProperty("startSpread", String.valueOf(startSpread)).trim()))); } catch (Exception e) { startSpread = 21; }
        if (!"2".equals(p.getProperty("settingsRev")) && startSpread == 15) startSpread = 21; // settings saved before revision 2 hold the old default (15): move them to the new one
        epochPopulation = i(p, "epochPopulation", epochPopulation, 1000, 10000000);
        epochCustom = p.containsKey("epochCustom") ? b(p, "epochCustom", false) : epochPopulation != 200000; // (a file from before the switch: only a value other than the old default was a choice)
        startPopulation = i(p, "startPopulation", startPopulation, 1000, 1000000);
        epochMemoryPercent = i(p, "epochMemoryPercent", epochMemoryPercent, 0, 95);
        epochTickSeconds = d(p, "epochTickSeconds", epochTickSeconds, 0.1, 600);
        epochCullPercent = i(p, "epochCullPercent", epochCullPercent, 1, 95);
        epochCullNumber = i(p, "epochCullNumber", epochCullNumber, 0, 10000000);
        epochMaxRealm = i(p, "epochMaxRealm", epochMaxRealm, 0, 10);
        crowdFactor = d(p, "crowdFactor", crowdFactor, 0.5, 100);
        spoilRefundPercent = i(p, "spoilRefundPercent", spoilRefundPercent, 0, 100);
        leaveBless = i(p, "leaveBless", leaveBless, 0, 10);
        try { String[] lp = p.getProperty("leavePct", "").split(","); if (lp.length == 4) { int[] v = new int[4]; for (int k = 0; k < 4; k++) v[k] = Math.max(0, Math.min(100, Integer.parseInt(lp[k].trim()))); leavePct = v; } } catch (RuntimeException ignored) { }
        parallelNpcs = b(p, "parallelNpcs", parallelNpcs);
        lore = b(p, "lore", lore);
        lorePause = b(p, "lorePause", lorePause);
        dialogueFlavor = b(p, "dialogueFlavor", dialogueFlavor);
        dialoguePopups = b(p, "dialoguePopups", dialoguePopups);
        dialogueAuto = b(p, "dialogueAuto", dialogueAuto);
        firstRunDone = b(p, "firstRunDone", firstRunDone);
        confirmRisky = b(p, "confirmRisky", confirmRisky);
        toasts = b(p, "toasts", toasts);
        sound = b(p, "sound", sound);
        highContrast = b(p, "highContrast", highContrast);
        autoEquipManual = b(p, "autoEquipManual", autoEquipManual);
        rememberWindows = b(p, "rememberWindows", rememberWindows);
        for (String kv : p.getProperty("windowBounds", "").split(";")) { int eq = kv.indexOf('='); if (eq > 0) windowBounds.put(kv.substring(0, eq).trim(), kv.substring(eq + 1).trim()); }
        music = b(p, "music", music);
        digestOnLoad = b(p, "digestOnLoad", digestOnLoad);
        matureStart = b(p, "matureStart", matureStart);
        maxRandomChallenges = i(p, "maxRandomChallenges", maxRandomChallenges, 0, 50);
        musicVolume = i(p, "musicVolume", musicVolume, 0, 100);
        musicEngine = p.getProperty("musicEngine", musicEngine);
        soundFont = p.getProperty("soundFont", soundFont);
        musicMix = p.getProperty("musicMix", musicMix);
        musicGenerated = i(p, "musicGenerated", musicGenerated, 0, 100);
        { java.util.Set<String> pl = new java.util.LinkedHashSet<String>(); String raw = p.getProperty("musicPlaylist", ""); if (!raw.isEmpty()) for (String t : raw.split("\\|")) if (!t.trim().isEmpty()) pl.add(t.trim()); musicPlaylist = pl; musicFilter = b(p, "musicFilter", !pl.isEmpty()); }
        pauseOn = set(p.getProperty("pauseOn", ""));
        recapWeeks = i(p, "recapWeeks", recapWeeks, 0, 520);
        mutedNotifications = set(p.getProperty("mutedNotifications", "level"));
        shownHints = set(p.getProperty("shownHints", ""));
        exportDir = p.getProperty("exportDir", exportDir);
        applyArt();
    }

    static synchronized void save() {
        Properties p = new Properties();
        p.setProperty("autosaveYears", String.valueOf(autosaveYears));
        p.setProperty("autosaveKeep", String.valueOf(autosaveKeep));
        p.setProperty("fontScale", String.valueOf(fontScale));
        p.setProperty("colorblindMap", String.valueOf(colorblindMap));
        p.setProperty("pictures", String.valueOf(pictures));
        p.setProperty("paintedArt", String.valueOf(paintedArt));
        p.setProperty("artFresh", String.valueOf(artFresh));
        p.setProperty("artSeed", String.valueOf(artSeed));
        p.setProperty("tooltips", String.valueOf(tooltips));
        p.setProperty("hints", String.valueOf(hints));
        p.setProperty("hud", String.valueOf(hud));
        p.setProperty("worldNews", String.valueOf(worldNews));
        p.setProperty("gardenGames", String.valueOf(gardenGames));
        p.setProperty("concertNotices", String.valueOf(concertNotices));
        p.setProperty("tileQualityCap", String.valueOf(tileQualityCap));
        p.setProperty("lessons", String.valueOf(lessons));
        p.setProperty("vignettes", String.valueOf(vignettes));
        p.setProperty("fightView", String.valueOf(fightView));
        p.setProperty("scrollSpeed", String.valueOf(scrollSpeed));
        p.setProperty("startSpread", String.valueOf(startSpread));
        p.setProperty("settingsRev", "2");
        p.setProperty("parallelNpcs", String.valueOf(parallelNpcs));
        p.setProperty("epochPopulation", String.valueOf(epochPopulation));
        p.setProperty("epochCustom", String.valueOf(epochCustom));
        p.setProperty("startPopulation", String.valueOf(startPopulation));
        p.setProperty("epochMemoryPercent", String.valueOf(epochMemoryPercent));
        p.setProperty("epochTickSeconds", String.valueOf(epochTickSeconds));
        p.setProperty("epochCullPercent", String.valueOf(epochCullPercent));
        p.setProperty("epochCullNumber", String.valueOf(epochCullNumber));
        p.setProperty("epochMaxRealm", String.valueOf(epochMaxRealm));
        p.setProperty("crowdFactor", String.valueOf(crowdFactor));
        p.setProperty("spoilRefundPercent", String.valueOf(spoilRefundPercent));
        p.setProperty("leaveBless", String.valueOf(leaveBless));
        p.setProperty("leavePct", leavePct[0] + "," + leavePct[1] + "," + leavePct[2] + "," + leavePct[3]);
        p.setProperty("lore", String.valueOf(lore));
        p.setProperty("lorePause", String.valueOf(lorePause));
        p.setProperty("dialogueFlavor", String.valueOf(dialogueFlavor));
        p.setProperty("dialoguePopups", String.valueOf(dialoguePopups));
        p.setProperty("dialogueAuto", String.valueOf(dialogueAuto));
        p.setProperty("firstRunDone", String.valueOf(firstRunDone));
        p.setProperty("confirmRisky", String.valueOf(confirmRisky));
        p.setProperty("toasts", String.valueOf(toasts));
        p.setProperty("sound", String.valueOf(sound));
        p.setProperty("highContrast", String.valueOf(highContrast));
        p.setProperty("autoEquipManual", String.valueOf(autoEquipManual));
        p.setProperty("rememberWindows", String.valueOf(rememberWindows));
        StringBuilder wb = new StringBuilder();
        for (java.util.Map.Entry<String, String> e : new java.util.TreeMap<>(windowBounds).entrySet()) wb.append(wb.length() > 0 ? ";" : "").append(e.getKey()).append('=').append(e.getValue());
        p.setProperty("windowBounds", wb.toString());
        p.setProperty("music", String.valueOf(music));
        p.setProperty("digestOnLoad", String.valueOf(digestOnLoad));
        p.setProperty("matureStart", String.valueOf(matureStart));
        p.setProperty("maxRandomChallenges", String.valueOf(maxRandomChallenges));
        p.setProperty("musicVolume", String.valueOf(musicVolume));
        p.setProperty("musicEngine", musicEngine);
        p.setProperty("soundFont", soundFont);
        p.setProperty("musicMix", musicMix);
        p.setProperty("musicGenerated", String.valueOf(musicGenerated));
        p.setProperty("musicPlaylist", String.join("|", musicPlaylist));
        p.setProperty("musicFilter", String.valueOf(musicFilter));
        p.setProperty("pauseOn", String.join(",", pauseOn));
        p.setProperty("recapWeeks", String.valueOf(recapWeeks));
        p.setProperty("mutedNotifications", String.join(",", mutedNotifications));
        p.setProperty("shownHints", String.join(",", shownHints));
        p.setProperty("exportDir", exportDir);
        File tmp = new File(file.getPath() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            p.store(out, "Tales of Dream and Fantasy 2 settings");
        } catch (Exception e) { return; }
        try { java.nio.file.Files.move(tmp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING); } catch (Exception ignored) { }
    }

    /** One-time hint bookkeeping: true the first time `id` is asked for (and hints are on). */
    static synchronized boolean firstTime(String id) {
        if (!hints || shownHints.contains(id)) return false;
        shownHints.add(id);
        save();
        return true;
    }

    static void ensureLoaded() { if (!loaded) load(); }

    /** Pushes the picture settings into PlaceImages (and makes the seed on first use). */
    static synchronized void applyArt() {
        if (artSeed == 0) { artSeed = new java.util.Random().nextLong() | 1; }
        PlaceImages.enabled = pictures;
        PlaceImages.generate = paintedArt;
        PlaceImages.artSeed = artFresh ? System.nanoTime() : artSeed;
    }

    /** Paint every place again with a new seed. */
    static synchronized void rerollArt() {
        artSeed = new java.util.Random().nextLong() | 1;
        applyArt();
        PlaceImages.artSeed = artSeed;
        PlaceImages.reload();
        save();
    }

    private static int i(Properties p, String k, int def, int lo, int hi) {
        try { return Math.max(lo, Math.min(hi, Integer.parseInt(p.getProperty(k, String.valueOf(def)).trim()))); } catch (Exception e) { return def; }
    }
    private static double d(Properties p, String k, double def, double lo, double hi) {
        try { return Math.max(lo, Math.min(hi, Double.parseDouble(p.getProperty(k, String.valueOf(def)).trim()))); } catch (Exception e) { return def; }
    }
    private static boolean b(Properties p, String k, boolean def) {
        String v = p.getProperty(k);
        return v == null ? def : v.trim().equalsIgnoreCase("true");
    }
    private static Set<String> set(String csv) {
        Set<String> s = java.util.concurrent.ConcurrentHashMap.newKeySet();
        for (String x : csv.split(",")) if (!x.trim().isEmpty()) s.add(x.trim());
        return s;
    }
}
