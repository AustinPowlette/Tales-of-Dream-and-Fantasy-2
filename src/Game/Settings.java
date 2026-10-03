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
    static volatile boolean hud = true;          // terminal only: a status block (name, realm, place, time, mode, alerts, next quest) before each prompt
    static volatile int startSpread = 15;        // a new big world (20,000+ people) starts with everybody spread over this many tiles across (odd; 0 = the old 3 x 3 zone around Originis): the first ticks are 10x faster
    static volatile boolean lore = true;         // the story of the Palace of the Oldest Order (see Lore): a new world gets the Palace at Originis; off = nothing of it runs or shows
    static volatile boolean lorePause = true;    // GUI: the game waits while a lore scene is open (until Continue)
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
    static volatile boolean music = false;          // endless generated ambient music (see Ambient)
    static volatile int musicVolume = 40;           // 0-100
    static volatile String musicEngine = "auto";    // "auto" (a SoundFont from soundfonts/ when there is one), "soundfont" or "builtin" (the built-in synthesiser)
    static volatile String soundFont = "";          // file name inside soundfonts/ ("" = the first one found)
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
        try { startSpread = Math.max(0, Math.min(31, Integer.parseInt(p.getProperty("startSpread", String.valueOf(startSpread)).trim()))); } catch (Exception e) { startSpread = 15; }
        lore = b(p, "lore", lore);
        lorePause = b(p, "lorePause", lorePause);
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
        p.setProperty("startSpread", String.valueOf(startSpread));
        p.setProperty("lore", String.valueOf(lore));
        p.setProperty("lorePause", String.valueOf(lorePause));
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
