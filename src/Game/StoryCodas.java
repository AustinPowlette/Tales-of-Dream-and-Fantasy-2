package Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** The last scene of a Story start: a few lines after the ending of the Palace's story (see LoreEndings.finish), written for each of the ten starts and each of five ways the Garden can end
 *  for the player. Families: stay (the Steward, the Usurper, the Idler), soldier (a lamp-bearer of the Outside), leave (walked out alone or spared by the Gardener), fall (struck down by the array)
 *  and world (the Garden itself ends). Lines are narration only ("* "), so the terminal's frame and the window show them alike; they may test the flags of the story (see ChapText). */
final class StoryCodas {
    private StoryCodas() { }

    static final HashMap<String, String[]> TABLE = new HashMap<>();
    private static void c(String story, String family, String... lines) { TABLE.put(story + ":" + family, lines); }

    /** The family of an ending id (see LoreEndings.finish), or null for one with no coda. */
    static String family(String id) {
        if (id == null) return null;
        if (id.equals("steward") || id.startsWith("usurper") || id.startsWith("idler")) return "stay";
        if (id.equals("soldier")) return "soldier";
        if (id.startsWith("leave_evil_dead")) return "fall";
        if (id.startsWith("leave_")) return "leave";
        if (id.startsWith("ascendant_") || id.equals("last_lamp")) return "world";
        return null;
    }

    /** The coda for this human's story and this ending (empty when the life has no chapter story). */
    static List<String> lines(Character c, String endingId) {
        List<String> out = new ArrayList<>();
        Starts.State st = c == null ? null : c.story;
        String fam = family(endingId);
        if (st == null || !st.v2 || fam == null) return out;
        String[] raw = TABLE.get(st.story + ":" + fam);
        if (raw == null) return out;
        Starts.Story s = Starts.STORIES.get(st.story);
        out.add("* ~ " + (s == null ? st.story : s.title) + " ~");
        if (!st.done) out.add("* Your own story was not quite finished. They never are. This is a good place to stop reading it.");
        out.addAll(ChapText.prepare(raw, c, st));
        return out;
    }

    /** One line for the credits: how the personal story stands. */
    static String credit(Character c) {
        Starts.State st = c == null ? null : c.story;
        if (st == null || !st.v2) return null;
        Starts.Story s = Starts.STORIES.get(st.story);
        return "Your story, " + (s == null ? st.story : s.title) + ": " + (st.done ? "told to the end." : "left open, for whoever picks it up.");
    }

    static {
        // ------------------------------------------------------------------ the Last Heir of the Azure Cloud Sect
        c("heir", "stay",
                "* You stay in the Garden, and Azure Cloud stays with you: a small blue banner above the Palace's biggest kitchen. The cracked cauldron sits by the stove. Every new wick is told whose it was.",
                "?!dead.wen|* Wen visits once a year and tells everyone the Palace's herb garden is all wrong. She is right.");
        c("heir", "soldier",
                "* The Gardener's army marches under many banners. Yours has a blue cloud on it. On the night before a battle you take out Master Hui's old cracked cauldron and make something small and honest.",
                "* The soldiers say it is the only tea in the whole army that hurts nobody.");
        c("heir", "leave",
                "* You leave the Garden with a cracked cauldron on your back. On the other side there is nothing you recognise and everything you need: a fire, a pot, a stranger who is hungry.",
                "* You begin, as Master Hui would have wished, with soup.");
        c("heir", "fall",
                "* You struck at the Gardener, and the array answered. Master Hui would have called you a fool.",
                "* He would have kept your tea warm anyway.");
        c("heir", "world",
                "* The Garden goes out. The herb garden at Azure Cloud goes with it, and the one tall chimney, and a grave under the chamomile.",
                "* You are glad, in that last moment, that somebody saved the cauldron.");

        // ------------------------------------------------------------------ the Rich Scion
        c("scion", "stay",
                "* You stay, and your house goes on without you. The vault has a new door and a new steward, and on the door a note in your handwriting: Numbers before people. People before gold.");
        c("scion", "soldier",
                "* You carry a lamp for the Gardener, and a ledger for the soldiers. Somebody has to see that the ration carts are honest.",
                "* You have never been trusted by so many people who are not allowed to say so.");
        c("scion", "leave",
                "* On the other side of the Seam you open a small shop. It has a bell above the door, and when it rings you feel it in your chest.",
                "* Your father would have said the location was terrible. He would have come to see it anyway.");
        c("scion", "fall",
                "* The array took a fortune in a single moment and left you with nothing at all.",
                "* Your accountants, wherever they are, would like it known that this was not a good investment.");
        c("scion", "world",
                "* The Garden ends, and so do the vault, the caravans and the long ledger. The one thing that goes on is the envelope you carried.",
                "* You read it one more time, by the last light. Be better than I was. You tried.");

        // ------------------------------------------------------------------ the Orphan of the Ash Road
        c("orphan", "stay",
                "* The Palace's long corridors are not your road, but you walk them as you walked the Ash Road, in no particular hurry, with a lantern.",
                "?stay=1|* Ilan lives a day's walk away. You see her every week, and the students think it very strange that you never stay anywhere long and always come back.");
        c("orphan", "soldier",
                "* You carry the lamp the way you carried a tinker's kettle: gently, with both hands.",
                "* In the evenings, around the army's fires, you tell of a road that ran past a thousand cooking fires. The soldiers listen. For a moment, they are home.");
        c("orphan", "leave",
                "* You step through the Seam the way you stepped through every town on the Ash Road, lightly and without looking back.",
                "* This time there is somewhere to come back to. The idea is so new that you stop, mid-step, to look at it.");
        c("orphan", "fall",
                "* You never stayed anywhere long enough to be hurt. The array fixed that.",
                "* Somewhere on the Ash Road, in a small shrine, a lantern is lit tonight, as it is every night.");
        c("orphan", "world",
                "* The Garden goes out. The Ash Road goes out. The last thing to go is the lantern in the shrine window.",
                "* You watch it for as long as you can.");

        // ------------------------------------------------------------------ the Disgraced Disciple
        c("disgraced", "stay",
                "* You stay, and the Pavilion's old halls fill with your students. The Closed Stacks are open. Over them is a sign: Ask Anything.",
                "* Nobody has got to the end of the list.");
        c("disgraced", "soldier",
                "* You teach the Scroll of Unbroken Breath to every soldier of the Gardener's army.",
                "* Elder Zhao would be horrified. That is very nearly the point.");
        c("disgraced", "leave",
                "* You leave with a scroll in your sleeve, copied out by hand, in case the Outside has no library.",
                "* Breathing in, you hold the world. Breathing out, you set it down.");
        c("disgraced", "fall",
                "* The array broke your breath in the end. The last page of your scroll, which nobody has read, says that a person who can stand alone has no need of a master.",
                "* You were never alone. You just never noticed.");
        c("disgraced", "world",
                "* As the Garden goes out, the whole scroll goes through your mind, line by line.",
                "* You breathe in. You hold the world, for the last time. You breathe out. You set it down gently.");

        // ------------------------------------------------------------------ the Blacksmith's Child
        c("smith", "stay",
                "* The Palace's forge is the best you have ever seen. You do not make a single weapon in it. You make hinges and kettles, and a sign for the kitchen door that says Welcome.",
                "* Pia would have approved.");
        c("smith", "soldier",
                "* The army needs spears, and you spent a lifetime deciding which ones you can carry. You make them honestly and well.",
                "* Every night you set your father's sword on the armoury table, to remind the others what a blade is for.");
        c("smith", "leave",
                "* You leave the Garden with a hammer on your shoulder. Evenfall is finished, but there is room in the Outside for a second blade, you decide.",
                "* You find a forge. It is cold. You light it.");
        c("smith", "fall",
                "* Evenfall does not cut anyone who has not raised a hand. You had, that day. It is the one time its maker would have been ashamed.",
                "* He would have forgiven you before the end of the sentence.");
        c("smith", "world",
                "* The Garden dims. The forge cools. The last thing to go is the sound of a small hammer on a small anvil, tick, tick, tick, like a clock in a very small house.");

        // ------------------------------------------------------------------ the Beast-Tamer's Daughter
        c("tamer", "stay",
                "* The Palace has a courtyard with no animals in it. You fix this at once.",
                "* A cinder boar sleeps by the Empty Seat. A tortoise holds court in the fountain. A crane has opinions about the roof.");
        c("tamer", "soldier",
                "* The Gardener's army has no beasts. You bring yours. Moss carries the supplies, Quill scouts the sky, and Ember walks at the front, huge and quiet, where fire has always been afraid of him.");
        c("tamer", "leave",
                "* You walk out through the Seam, and three great shapes walk out beside you, and the Outside gets a very strange first impression of you.",
                "* The first creature you meet there is small and grey with big ears. It sits down in front of you and hums back.");
        c("tamer", "fall",
                "* The array took you, and the ones you left behind did not forget.",
                "* On the evening of every storm, a crane circles the lake, a tortoise lifts its head, and a very large boar leans on the sanctuary gate and hums three slow notes.");
        c("tamer", "world",
                "* The Garden goes out, and the beasts go with it, side by side, a boar and a tortoise and a crane. You are glad they were not alone.",
                "* As the light fails, you hum: three slow notes, and the same three, higher.");

        // ------------------------------------------------------------------ the Ghost-Touched
        c("ghost", "stay",
                "* The Palace is full of people who never quite left it. You can hear most of them. They like you.",
                "* Some of them are very rude about the architecture.");
        c("ghost", "soldier",
                "* The Gardener's army has more ghosts than it admits. You are the only one who talks to them.",
                "* By the first battle you have a very good rota. You also have a goose. Nobody can explain it.");
        c("ghost", "leave",
                "* You walk out of the Garden, and the dead walk out with you, as the dead will. Nobody on the other side is ready for any of it.",
                "* You wear a scarf. It helps.");
        c("ghost", "fall",
                "* The array killed you. It is not, after all, your first death. You wait in the grey place, in your scarf, for someone to arrive.",
                "* A goose does.");
        c("ghost", "world",
                "* The Garden goes out. There are a great many of the dead, and only one door. You hold it open as long as you can.",
                "* They go through one by one, saying thank you.");

        // ------------------------------------------------------------------ the Lucky Fool
        c("lucky", "stay",
                "* The Palace has a notice board. You are the only person ever to win its raffle five years in a row. The Elders want to investigate.",
                "* Pip says it is skill.");
        c("lucky", "soldier",
                "* You are the least qualified soldier in the Gardener's army. You are also the only one who has never been wounded.",
                "* The generals have given up asking why. They put you in the middle, where everyone can see you.");
        c("lucky", "leave",
                "* You leave the Garden with a purse, a hat, and a friend. The Outside is huge and strange and you have no talent for it whatever.",
                "* Within a week you have found a free supper.");
        c("lucky", "fall",
                "* The luck ran out at the array. It had run out before, on a mountain, and your friends caught you.",
                "* They are standing on the far side of the Seam, in tall black hats, looking up.");
        c("lucky", "world",
                "* The Garden goes out. Somewhere in the dark a very small woman in a grey dress straightens her spectacles.",
                "* 'Well,' she says, with great dignity, 'that is the final form submitted.'");

        // ------------------------------------------------------------------ the Last of the Silver Crane Clan
        c("clan", "stay",
                "* The Palace has a hundred kitchens. You choose the smallest and put a kettle on.",
                "* Tamsin turns up the next day and rearranges all the jars.");
        c("clan", "soldier",
                "* The Silver Crane banner goes to the front. It is blue, with a crane on it, on one leg.",
                "* It is the one banner in the whole army that people can find in the dark.");
        c("clan", "leave",
                "* You leave through the Seam on one leg, so to speak. See far, the motto said.",
                "* You see a very long way.");
        c("clan", "fall",
                "* The array took you, and the clan stood on the roof of the new hall and watched the banner turn in the wind.",
                "* 'They would have been terrible at this,' says Joren, and puts the kettle on.");
        c("clan", "world",
                "* The Garden goes out. The hall, the well and the spring go out. The last thing to go is the banner, a silver crane on blue, standing on one leg, seeing far.");

        // ------------------------------------------------------------------ the Wandering Immortal's Disciple
        c("disciple", "stay",
                "* You sit in the Palace kitchen, at a corner table, and you do not put a Reserved plate on it. A boy comes by and asks you to teach him three moves.",
                "* You say: watch. Don't try. Just watch.");
        c("disciple", "soldier",
                "* Somewhere in the army of the Outside you hear a tune hummed in a tent, slow and a little sad. You follow it.",
                "* The tent is empty. The cup on the table is warm, and mended with gold.");
        c("disciple", "leave",
                "* You step through the Seam with a red gourd on your hip. On the far side there is a road, and not all of it is finished.",
                "* You walk until you find an old man in a ragged coat, feeling the air with one hand. He does not turn around. 'There you are,' he says.");
        c("disciple", "fall",
                "* The array took you. In the dark, a warm hand pours a little wine into your palm.",
                "* 'Ask the gourd,' says a voice. You laugh. It is the first time you have understood the joke.");
        c("disciple", "world",
                "* The Garden goes out. The corner seat in the old inn goes out.",
                "* The last thing to go is a tune, slow and a little sad, and for the first time you hum along.");
    }
}
