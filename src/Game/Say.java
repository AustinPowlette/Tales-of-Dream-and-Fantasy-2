package Game;

/** One door for the console lines that are news about the world, not about what the player did or what happens to the player. `settings worldnews off` keeps them out of the log; they still
 *  reach the history and the Chronicle (`GameDriver.logEvent`). The other kinds of line are listed in GameInfo/Print Review.md: results (`Character.tell`), the prompt line, notices
 *  (`Notifications`), quest alerts, story scenes, system lines (save, load). */
final class Say {
    private Say() { }

    /** News of the wider world (a tournament won far away, a city founded, a trial closed). */
    static void world(String text) {
        if (Settings.worldNews) System.out.println(text);
        else GameDriver.logEvent(text);
    }

    /** News that is about one person as well as the world (an auction won, a title earned, a bounty posted): a person who plays is told whatever the switch says (silent during an AUTO run); everybody else reads it as world news. */
    static void about(Character who, String text) {
        if (who != null && who.player > 0) who.tell(text);
        else world(text);
    }

    /** The status line of a character that is followed or runs on its own (the only sign that the run is alive): always shown. */
    static void status(String text) { System.out.println(text); }

    /** Developer lines (warnings, internal counts): shown only with -Dtdf.debug, never to a player. */
    static void debug(String text) { if (System.getProperty("tdf.debug") != null) System.out.println(text); }
}
