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
        if (id.startsWith("leave_evil_dead") || id.startsWith("early_strike_dead")) return "fall";
        if (id.startsWith("early_")) return "leave"; // the Early Door: the rear guard, a walk out alone, a strike that was spared
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
                "?!dead.wen|* Wen visits once a year and tells everyone the Palace's herb garden is all wrong. {wen.He} is right.");
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
                "* You have never been trusted by so many people.");
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

        // ------------------------------------------------------------------ the Cook Who Fed the Mountain
        c("chef", "stay",
                "* You stay in the Garden and feed it. The range in the Under-Kitchen is never cold now, and a man in an apron has learned to add salt.",
                "* Over the door of the oldest kitchen in the Palace someone has chalked: NOBODY IS TURNED AWAY. Under it, smaller: (See the Cook.)");
        c("chef", "soldier",
                "* In the Gardener's army the cooks go in last and eat first. You carry a jar of pot-bottom on your belt, and on the night before a battle you stir a spoonful into the common pot.",
                "* The soldiers say it tastes of a room they used to know. Nobody asks where.");
        c("chef", "leave",
                "* You step through the Seam with a pot on your back. On the far side there is a road, and a crossing, and a stranger sitting by it with an empty bowl.",
                "* You put the pot on the fire. You say: eat first, then ask.");
        c("chef", "fall",
                "* The array took you. In the dark someone is stirring a pot, and you can taste it.",
                "* It tastes of the room.");
        c("chef", "world",
                "* The Garden goes out. The Long Table goes out, and the door, and the board, and the scarred table that was always a little too small.",
                "* The last thing to go is the bottom of the pot, ninety years deep.");

        // ------------------------------------------------------------------ the Apothecary's Second Cup
        c("poisoner", "stay",
                "* You stay in the Garden, and the Palace's pill kitchen gets a new rule over the stove: three questions before every batch. Who is it for. What do they think it does. What happens if they are wrong.",
                "?!dead.calla|* {calla.He} visits on Wednesdays and inspects the soup.");
        c("poisoner", "soldier",
                "* In the Gardener's army you are the one who is asked for the other thing, and the one who asks the three questions first. Nobody has ever been poisoned by a soldier of yours who did not deserve it.",
                "* On the long nights you knit.");
        c("poisoner", "leave",
                "* You step through the Seam with a shop's worth of jars in a bag. On the far side there is a harbour, and a net-mender, and a window with room for a sign.",
                "* You paint it yourself. CURES, it says. Below that, smaller: ask first.");
        c("poisoner", "fall",
                "* The array took you. In the dark someone is pouring tea, and for once you know exactly what is in the cup.",
                "* You drink it anyway. It is only fair.");
        c("poisoner", "world",
                "* The Garden goes out. Salt Lane goes out, and the shop, and the cat on the scales.",
                "* The last thing to go is a ball of green wool, rolling off a counter and down a very long hill.");

        // ------------------------------------------------------------------ the Tailor of Seven Hems
        c("tailor", "stay",
                "* You stay in the Garden. The Palace's Wardrobe has a new tenant in the cedar room under the stair, and every robe that comes out of it has a seventh hem.",
                "* The Master of the Palace wears a plain apron with one stitch in it that is not plain. He will not say what it promises.");
        c("tailor", "soldier",
                "* The army of the Outside marches in uniforms you did not make, and on the night before a battle you sew a hem into the nearest coat. You do not say what it promises. It always comes true a little.",
                "* The soldiers call the coats lucky. You let them.");
        c("tailor", "leave",
                "* You step through the Seam with a pincushion on your wrist. On the far side there is a street with a lamp-maker, a tea merchant who sells only one dull tea, and an empty shop with a window.",
                "* You hang the grey dummy in it. You say: Seven Hems.");
        c("tailor", "fall",
                "* The array took you. In the dark a hand turns up a hem, and then the next, and the next.",
                "* The seventh is silver, a hair's breadth wide, and it says your name.");
        c("tailor", "world",
                "* The Garden goes out. The shop, the lane and the long table go out, and the sign with the gold needle.",
                "* The last thing to go is a dog asleep on a doorstep, with a coat across its back.");

        // ------------------------------------------------------------------ the Crank of Gearwheel Lane
        c("inventor", "stay",
                "* You stay in the Garden. The Palace's forge has a bench in the corner, a brass duck on the shelf above it, and a man in an apron who is learning, very slowly, how to open a clock and shut it again.",
                "?!dead.mott|* {mott.He} visits on Thursdays and corrects the labels.");
        c("inventor", "soldier",
                "* In the Gardener's army you are the person who is handed the thing that does not work. You put it on a bench, look at it for an hour, and make it work in a way nobody meant.",
                "* The soldiers call it luck. The duck says nothing. The duck is, as always, right.");
        c("inventor", "leave",
                "* You step through the Seam with a toolbox and a brass duck on your shoulder. On the far side there is a lane, and a bakery, and a workshop with a crooked chimney and a door that opens outward.",
                "* You put a sign on it. BRACE, it says.");
        c("inventor", "fall",
                "* The array took you. In the dark something is ringing, very low, below hearing, and you know the note.",
                "* You hum along. It is the first thing you have ever built that nobody else could take apart.");
        c("inventor", "world",
                "* The Garden goes out. The lane goes out, and the bakery, and the forty-one machines on the benches.",
                "* The last thing to go is a small brass duck, on a windowsill, looking at the stars with one blue eye and one brown.");

        // ------------------------------------------------------------------ the Zither That Would Not Be Tuned
        c("musician", "stay",
                "* You stay in the Garden. The Choir of the Nine Bells has a new member in the back stalls with a pale-stringed zither, and at the Convocation it holds the quarter-tone under the organ without a rest.",
                "?!dead.ilse|* {ilse.He} listens to you with {ilse.his} eyes shut, still.");
        c("musician", "soldier",
                "* In the Gardener's army you are the one who plays at the muster. The hymn has three verses now. Nobody knows who taught the soldiers the third.",
                "* On the night before a battle you play it quietly, and the line sings.");
        c("musician", "leave",
                "* You step through the Seam with a zither on your back. On the far side there is a long dim room full of people humming one note, and one of them looks up.",
                "* You sit down beside her. You play the verse you have been keeping. She says: you took your time.");
        c("musician", "fall",
                "* The array took you. In the dark, very low, below hearing, a string is being played by someone who is not you.",
                "* It is perfectly in tune with nothing at all. You join in.");
        c("musician", "world",
                "* The Garden goes out. The barn goes out, and the Fiddler's Rest, and the green in Reedmarch with its bandstand.",
                "* The last thing to go is a note, a quarter-tone below the lowest pipe of the organ, still sounding.");

        // ------------------------------------------------------------------ the Lamplighter of Cinder Deep
        c("miner", "stay",
                "* You stay in the Garden. The Palace's lowest floor has a shaft in it, four hundred feet deep, with a cage, and a man in an apron rides it down on Tuesdays to listen to the root.",
                "* He says he has stopped being afraid of the dark. He says it has a sound, and the sound is a kind of company.");
        c("miner", "soldier",
                "* In the Gardener's army the sappers are the ones who go first and come back last. You dig the trench the line stands in. Nobody thanks you. You prop it anyway.",
                "* On the night before a battle you tap three and a pause on the wall of the tent. Somewhere, very far away, someone taps back.");
        c("miner", "leave",
                "* You step through the Seam with a lamp at your belt. On the far side there is a hill, and a sea, and a very long seam of something dark under the grass.",
                "* You light the lamp. You say: first shift.");
        c("miner", "fall",
                "* The array took you. In the dark someone is counting, patiently, a long way down, and you are on the list.",
                "* You say: here. It is the first time you have ever said it and meant it.");
        c("miner", "world",
                "* The Garden goes out. The town goes out, and the wheel, and the four hundred grey houses.",
                "* The last thing to go is a slow tapping in the rock, three and a pause, and then, for the first time, a fourth.");

        // ------------------------------------------------------------------ Status Window (Loading)
        c("cheater", "stay",
                "* You stay in the Garden. The Palace's Under-Kitchen has a second stool by the terminal, and a plain blue panel in the corner of everyone's sight that says, once a day, hello.",
                "?!dead.vesna|* {vesna.He} takes the stool. {vesna.He} says the font is better than it was.");
        c("cheater", "soldier",
                "* In the Gardener's army a small blue window sits in the corner of your eye. It does not rank anyone. It lists, in plain white text, the names of the people in your company.",
                "* On the night before a battle you press Wave. A hundred small hands wave back.");
        c("cheater", "leave",
                "* You step through the Seam. On the far side the panel in your eye flickers once, and a line of text appears. [Welcome, Host.]",
                "* It is, for the first time, not a greeting but a reply.");
        c("cheater", "fall",
                "* The array took you. In the dark a blue rectangle opens, the size of a postage stamp.",
                "* It says: Not yet. Take your time. You wait, and it is very kind about it.");
        c("cheater", "world",
                "* The Garden goes out. The village goes out, and the inn, and the Starting Village sign that the panel was so proud of.",
                "* The last thing to go is a bar at eighty-eight per cent, glowing in the dark, quite content.");

        // ------------------------------------------------------------------ the Forager's Almanac
        c("forager", "stay",
                "* You stay in the Garden. The Palace's Under-Kitchen window has fourteen flowerpots on the sill, all of them thriving, and a man in an apron waters them once a day and then, with great effort, leaves them alone.",
                "* On the first day of winter he sets them in the dark. He says it is the hardest thing he has ever done.");
        c("forager", "soldier",
                "* In the Gardener's army you are the one who knows which leaf stops a bleeding and which root makes a fever break. You carry a small brown book with a cord round it.",
                "* On the night before a battle you read the first page aloud to the line, in a low voice. Nobody asks what it means.");
        c("forager", "leave",
                "* You step through the Seam with a basket on your arm. On the far side there is a hill, and a wood on the hill, and a path up the middle that nobody cut.",
                "* You start up it. The first leaf you see is the wrong colour for the month. You smile. You take out the pencil.");
        c("forager", "fall",
                "* The array took you. In the dark something is growing, very slowly, a long way off, and it is not afraid.",
                "* You are a seed. It is not the worst thing to be. You wait for the winter to end.");
        c("forager", "world",
                "* The Garden goes out. The Greywood goes out, tree by tree, from the edges, and the hill, and the hut of green boughs.",
                "* The last thing to go is a single acorn, warm, in the heart of a stump, three thousand and eleven rings deep.");

        // @@CODAS
    }
}
