package Game;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Writes every line of the story (scenes, clues, whispers, omens, objectives, the places, the endings, the credits jokes) from the real sources into one
 *  Markdown file for reading and editing:  java -cp <classes> Game.StoryDump "Story Script.md" (the hand-written parts come from tools/story_header.md and
 *  tools/story_footer.md, concatenated by the caller). */
public class StoryDump {
    static PrintWriter w;

    static void scenes(String heading, String note, String... ids) {
        w.println("\n## " + heading + "\n");
        if (!note.isEmpty()) w.println("*" + note + "*\n");
        for (String id : ids) {
            LoreText.Scene sc = LoreText.SCENES.get(id);
            if (sc == null) { w.println("### " + id + " (missing)\n"); continue; }
            w.println("### `" + id + "`: " + sc.title + "\n");
            for (String l : sc.lines) {
                if (LoreText.narration(l)) w.println("> *" + l.substring(2) + "*\n");
                else if (LoreText.other(l)) w.println("**OLD MAN:** " + l.substring(9) + "\n");
                else w.println("**???:** " + l + "\n");
            }
        }
    }

    public static void main(String[] a) throws Exception {
        w = new PrintWriter(a.length > 0 ? a[0] : "story_body.md", "UTF-8");
        w.println("# Part 2: Everything the story says\n");
        w.println("Placeholders in curly braces are filled in by the game: {name} full name, {first} first name, {rank} the player's Palace rank title, {year}, {round}, {lamp}, {convo}, {how}. Lines in italics with a `>` are narration; lines starting with **???:** are spoken by ???; **OLD MAN:** is the Gardener, seen only Outside.\n");

        w.println("## Names of ranks, lamps and acts\n");
        w.println("Palace name: **" + LoreText.PALACE + "**. Voice: **" + LoreText.VOICE + "**.\n");
        w.println("Acts: " + String.join(" / ", Arrays.copyOfRange(LoreText.ACT_NAMES, 1, LoreText.ACT_NAMES.length)) + "\n");
        w.println("Rank titles: Outer Court = **" + LoreText.RANK_TITLE[0] + "**, Inner Court = **" + LoreText.RANK_TITLE[1] + "**, Elder = **" + LoreText.RANK_TITLE[2] + "**\n");
        w.println("The Nine Lamps: " + String.join(", ", LoreText.LAMPS) + "\n");

        w.println("## The road: the 15 objectives every human follows\n");
        w.println("| # | Act | Kind | What the game tells the player |\n|---|---|---|---|");
        for (int i = 0; i < Lore.CHAIN.length; i++) { Lore.Step st = Lore.CHAIN[i]; w.println("| " + (i + 1) + " | " + LoreText.ACT_NAMES[st.act] + " | " + st.kind + " | " + st.text + " |"); }

        w.println("\n## The nine places and the Empty Seat\n");
        w.println("| Place | Looks like | Where | Needs Major Lvl | Reward when read |\n|---|---|---|---|---|");
        String[] rewards = {"2 Qi Cultivation Pills and 150 spirit stones from the keeper's strongbox", "the footprints teach your stride: movement technique improved", "a Potential Pill, from the largest basin",
                "the balance re-weighs you (your potential is shown, failed breakthroughs weigh less)", "a seed of understanding: martial technique improved", "ten weeks of insight under the crack",
                "a Potential Pill, pressed into your hand by a ghost", "gear from an unnumbered cell", "the Crossed leave you their lamp-oil: potential +1"};
        for (int i = 0; i < LoreSites.ALL.length; i++) { LoreSites.Site s = LoreSites.ALL[i]; w.println("| " + s.title + " | " + s.kind + " | " + s.where + " | " + s.minMajor + " | " + rewards[i] + " |"); }
        w.println("| The Empty Seat | the seat at the end of the hall in Originis | Originis (an Elder who has read the nine) | 9+ | potential +1 |");

        w.println("\n## What each place teaches (the clue list in the Journal)\n");
        for (LoreText.Clue c : LoreText.CLUES.values()) w.println("- **" + c.title + "**: " + c.truth);

        scenes("Entrance and the Convocation", "Plays when the player first reaches Originis, registers for the Convocation of the First Gate, passes or fails, is invited, or becomes an Inner Court disciple.",
                "bell", "register", "pass", "fail", "invite", "inner");
        scenes("Realm milestones", "Plays the first time the player reaches that realm (and has joined the Palace).", "m1", "m3", "m4", "elder", "m6", "m7", "m8", "m9", "door");
        scenes("Life events", "Rebirth, a new body, and the omens.", "rope", "another_face", "omen");
        List<String> sites = new ArrayList<>();
        for (String id : LoreText.SCENES.keySet()) if (id.startsWith("site_")) sites.add(id);
        scenes("The nine places (shown when the player reads a place)", "Plays once, on the turn after the player reads the place.", sites.toArray(new String[0]));
        scenes("The Empty Seat (the truth)", "Plays when an Elder who has read the nine places sits beside the Empty Seat.", "seat");

        w.println("\n## The Giant Footprints (six optional places)\n");
        w.println("Three near the Thin Sky, three near the Garrison. Standing on one at Major Lvl 3+ shows the clue once and gives a rare relic.\n");
        for (int i = 0; i < LoreSites.FOOT_NAMES.length; i++) w.println("- **" + LoreSites.FOOT_NAMES[i] + "**: " + LoreSites.FOOT_CLUES[i]);

        w.println("\n## Whispers (one line, now and then, once the player has met the Palace)\n");
        w.println("The number is the earliest act in which the line can appear (99 = after the story ended; 98 = only on the Evil dark path).\n");
        for (String s : LoreText.WHISPERS) { int bar = s.indexOf('|'); w.println("- [" + s.substring(0, bar) + "] " + s.substring(bar + 1)); }
        w.println("\n## Omens (a strange line in the world chronicle, from year 20 on; {place} is a city or Originis)\n");
        for (String s : LoreText.OMENS) w.println("- " + s);

        Set<String> done = new HashSet<>(Arrays.asList("bell", "register", "pass", "fail", "invite", "inner", "m1", "m3", "m4", "elder", "m6", "m7", "m8", "m9", "door", "rope", "another_face", "omen", "seat"));
        List<String> old = new ArrayList<>();
        for (String id : LoreText.SCENES.keySet()) if (!id.startsWith("site_") && !done.contains(id) && !id.startsWith("end_") && !id.startsWith("out_")) old.add(id);
        if (!old.isEmpty()) scenes("Other scenes", "", old.toArray(new String[0]));

        List<String> ends = new ArrayList<>();
        for (String id : LoreText.SCENES.keySet()) if (id.startsWith("end_")) ends.add(id);
        scenes("The endings", "In order: the Seam (end_threshold), the Old Man (end_oldman...), then the ending scenes, then the credits.", ends.toArray(new String[0]));
        List<String> outs = new ArrayList<>();
        for (String id : LoreEndings.OUT_IDS) outs.add(id);
        scenes("The eight branches of the Outside (random: after walking out alone, being spared by the array, the hidden ending, or stepping through at the Garden's end)", "Tags: " + String.join(", ", LoreEndings.OUT_TAG) + " (in this order).", outs.toArray(new String[0]));

        w.println("\n## Wandering Immortal hints (given to a human on the same tile, every turn)\n");
        for (String s : LoreEndings.HINTS) w.println("- " + s);
        w.println("\n## Funny credit lines (a random 18 are used per ending)\n");
        for (String s : LoreEndings.FUNNY) w.println("- " + s);
        w.close();
        System.out.println("wrote " + (a.length > 0 ? a[0] : "story_body.md"));
    }
}
