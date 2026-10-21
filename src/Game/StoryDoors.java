package Game;

/** The last season of every Story start (2026-10-20): "The Door". Three short chapters, each opened only when the human has reached Ascension (Major 7, `Chapter.hardMajor`, which never lapses), where the Early Door of the Palace
 *  opens (`lore door`). Each ties the story's last open thread to the Door: a word comes from the Outside, the people of the story stand in the line at the Garrison of the Crossed, and the human comes to the Empty Seat and the latch.
 *  The choice `latch` (1 stay, 2 go when the turn comes, 3 hold the latch for the next) only colours the epilogue: the Palace's own endings (`lore door`, `lore cross`) are unchanged. A chapter is left out when the human has already
 *  gone through. The season number is the one after the story's last (3 for most; 4 where a full extra season comes first). Registered last, in `Chapters`' static block. */
final class StoryDoors {
    private StoryDoors() { }

    /** The Palace's story is over for this human: the Door chapters have nothing left to say. */
    static final Chapters.Cond GONE = (c, st) -> { Lore.PState p = Lore.pstate(c); return p != null && p.ended; };

    static Chapters.Chapter door(Chapters.Def d, String id, String title) { return d.chapter(id, title).hardMajor(7).skipIf(GONE); }

    /** Adds lines to the epilogue just before its last line (the last line is the story's closing image). */
    static void epi(Chapters.Def d, String... lines) {
        String[] old = d.epilogue == null ? new String[0] : d.epilogue;
        if (old.length == 0) { d.epilogue(lines); return; }
        String[] out = new String[old.length + lines.length];
        System.arraycopy(old, 0, out, 0, old.length - 1);
        System.arraycopy(lines, 0, out, old.length - 1, lines.length);
        out[out.length - 1] = old[old.length - 1];
        d.epilogue(out);
    }

    /** The three answers every Door shares, written once; each story words its own after-lines. */
    static Chapters.Choice latch(String prompt, String stay, String go, String hold, String[] a1, String[] a2, String[] a3) {
        return new Chapters.Choice("latch", prompt, stay, go, hold).after(a1, a2, a3);
    }

    static void add(Chapters.Def d) {
        int n = d.seasons.size() + 1;
        switch (d.id) {
            case "heir": heir(d, n); break;
            case "scion": scion(d, n); break;
            case "orphan": orphan(d, n); break;
            case "disgraced": disgraced(d, n); break;
            case "smith": smith(d, n); break;
            case "tamer": tamer(d, n); break;
            default: StoryDoorsB.add(d, n); break;
        }
    }

    // ------------------------------------------------------------------ the Last Heir of the Azure Cloud Sect
    private static void heir(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* Azure Cloud has seven hundred beds now and a roof that leaks in no corner at all. You have stopped counting the years by the harvest. You count them by the number of times the cauldron by the door has hummed.",
                "?seam=1|* It hums most in the evenings, with the murmur of a crowd leaving a theatre. Ambrose has filled four notebooks.",
                "* One morning you notice that you have been tireless for a month. You go out to the herb garden and the blue flower on Master Hui's stone is open, in the snow.");
        d.season(n, "The Door");

        door(d, "dr1", "The Cauldron Speaks").years(2)
                .intro(
                        "* The cauldron by the door stops humming on the first night of the frost. It does not go quiet. It clears its throat. Ambrose, who sleeps in the hall to be near it, runs for you in his nightshirt with his notebook in his hand. By the time you come, {wen.first} is already there with a lamp.",
                        "@Ambrose| It said a word. A real one. I wrote it down. I think it was please.")
                .wait(8)
                .outro(
                        "* You put your ear to the iron. It is warm, as it has never been. Far down inside it, in the seam where Master Hui's crack was mended, there is a voice. It is thin, and dry, and cross. It is a voice you last heard at the foot of a grave in the herb garden.",
                        "@Hui| (down the seam, very faint) I told you to stir clockwise. I am in a dim long room. They let me cook. There is a pot here that has never boiled over, and I find I don't like it. Bring the cauldron when you come. And bring the boy with the notebook. He has a clean hand.",
                        "* The voice stops. Ambrose is crying and writing at once, and cannot do both. {wen.first} takes the lamp from his hand and sets it on the floor and sits beside him. Nobody says anything. In the iron, a long way off, a ladle rings against a pot.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr2", "Ambrose in the Line").gap(26)
                .intro(
                        "* Ambrose asks to go before you do. He asks it on a Sunday, after dinner, standing in the kitchen with his notebooks tied in a bundle on his back and the look of a boy who has rehearsed a speech and forgotten it. He says he is a cook. He says Master Hui wants a cook. He says he is sorry to leave in the middle of the pickling.")
                .at("garrison", "Walk with Ambrose to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* The Garrison stands empty on its bare plain, with the gates open and the beds made. In front of it a line of men and women in good boots wait in the rain with their lamps. Ambrose takes his place at the end. He is the smallest of them, and the only one with a ladle. A tall woman with a grey plait walks down the file with her ledger and writes his name.",
                        "@Ambrose| I'll tell him you're coming. I'll tell him you're slow. He'll like that.",
                        "* You stand in the rain until the line has gone in. The last thing you see is a thin boy at the gate turning round to wave a ladle. It is, you think, the bravest thing you have seen anyone do with a spoon.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr3", "Hui's Pot").gap(26)
                .intro(
                        "* You carry the cauldron to the Palace yourself, on a cart, wrapped in a blanket. It is heavier than it has ever been. The Wicks stand aside in the great hall. The Master of the Palace is nowhere. You walk past the nine lamps to the Empty Seat with an iron pot in your arms, and set it down on the flags in front of the chair that nobody sits in. The crack in its side shines.")
                .at("palace", "Take the cauldron to the Empty Seat in the Palace of the Oldest Order, at Originis.")
                .choice(latch("The Door is under the Seat. The cauldron is warm. Master Hui is waiting on the other side of a latch.",
                        "Stay: let the pot go through with a message, and keep the sect.", "Go: when your turn comes, carry it yourself.", "Hold the latch: stand by the Door and let the next person through with their own pot.",
                        new String[]{"* You send it through. You tie a note to the handle in your best hand. It says: still clockwise. The cauldron hums once, deep in its iron, and a small hand reaches up out of the dark under the Seat, and takes it, and draws it down.", "@Wen| (a whisper) He'll like that."},
                        new String[]{"* You put your hand on the rim. The iron is warm as a hearth. You say you will come, when it is time, and not before, and not slowly. A voice under the flags says: very good. It is the voice of a man who has been waiting a good while and is pleased to have something to complain about."},
                        new String[]{"* You stand by the Seat all afternoon. People come by twos and threes, a Wick with a bundle, a cook with a spoon, a woman with a lamp. You hold the latch while each one goes through. By dusk you have a stiff arm, and a very clear idea of what Master Hui meant by clockwise."}))
                .outro(
                        "* The cauldron stays at the Seat all night. In the morning it is quiet, and a little cooler, and when you lift it the crack is shut. Where it was, in the iron, a thin white line shines like a scar that has healed. You carry the pot home. You hang it by the door of the hall. It does not hum again. It has said what it came to say.")
                .pay(1000, 2, 0, 2, 1, 2);

        epi(d,
                "?latch=1|* The cauldron went through with a note tied to its handle. Ambrose copies the answer into his notebook every year on the same day, in his best hand, and has never once shown it to anyone.",
                "?latch=2|* You have settled that you will go through the Door, when it is time. The pot stands by the hall door, with a ladle in it, ready.",
                "?latch=3|* You hold the latch at the Seat for an afternoon each autumn. The Wicks have stopped asking. They bring you tea.");
    }

    // ------------------------------------------------------------------ the Rich Scion
    private static void scion(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The city has grown past its second wall. The vault is full, and the books are in order, and the sums are lovely every year. You have been the richest person in the province for so long that you have stopped noticing.",
                "?love=1|* {linden.first} keeps the key, and the footnotes, and a bowl of oranges on the corner of the desk that is never empty.",
                "* On the first of the month there is still a note on the gate, in a neat sharp hand. This month it says: 'You will want to come and see me.'");
        d.season(n, "The Door");

        door(d, "dr1", "The Note at the Gate").years(2)
                .intro(
                        "* The note is on the gate on the first of the month, as it has been for thirty years. It is on good plain paper, folded in three, with a stone on it against the wind. You have never, in all that time, stood at the gate before dawn to see who puts it there. You do so now, in a thick coat, with a lantern, and wait.")
                .wait(8)
                .outro(
                        "?fall=3|* She comes at five, up the street, in a black coat, with a stick and a small barrow. It is a very old barrow with a wobbly wheel. She is ninety-one. She leans the barrow on the gatepost and takes the stone off the old note and puts it on the new one, and looks up, and sees you, and says: 'You're late. I wrote it a week ago. I've been standing here since.'",
                        "?!fall=3|* A clerk comes at five, up the street, in a grey coat, with a satchel. He does not see you. He folds the note in three and puts a stone on it and goes away. You follow him to a door in a back street, and the door opens on a room full of notes, in a neat sharp hand, weeks and years of them, tied in bundles. The hand is Odile Kessel's. She has been writing ahead.",
                        "@Odile| I'm going through the Door at the end of the season. I thought I might see the vault before I go. I didn't like to come in. I've been told I'm no longer a person who is received.",
                        "* You take her arm. It weighs almost nothing. You walk her in through the gate you built, past the school and the bell and the market. She says the drains could have been deeper. She says it is a very fine wall.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr2", "Salt and String").gap(26)
                .intro(
                        "* Odile asks to be taken to the Garrison on the day of the muster. She says she would like to walk the last of it herself, with the barrow, and be seen by the people she wronged. She says she has a list. It is short. It has two names on it, and a boy's, and a dog's.")
                .at("garrison", "Take Odile Kessel to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* The line is long in the rain. She joins it at the end, a very small woman with a barrow, and nobody looks at her twice. The woman with the grey plait marks her name. Odile takes a long look at the open gate. She says it is a great deal neater than the Guild.",
                        "@Odile| Your father would have built it twice as big and half as sound. I am glad you didn't. Tell Tobias I took the wheel. Tell him I did the talking.",
                        "* She goes through with her head up. At the gate she stops and takes something out of the barrow, a small brass plate no bigger than a hand, and sets it on the sill. It says SALT AND STRING. Then she is gone, and the barrow is left leaning on the wall, with its wobbly wheel in the air.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr3", "The Sums").gap(26)
                .intro(
                        "* The vault of the city has a round room in the centre, under the dome, with a brass plate on the floor. You stand on it at the end of the autumn with the account books in a case, and look at the arithmetic of your life. It is a good sum. It comes out. It has come out every year, and you have never once asked what it was for.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(latch("You have the books, and the key, and an open Door under the Seat.",
                        "Stay: leave the books to the city, and the key to someone honest.", "Go: when the sums are done, close the ledger and walk through.", "Hold the latch: be the one who stands at the Door with a lantern and checks that nobody is charged.",
                        new String[]{"* You leave the books to the council, with a letter. It says: do not make it bigger than it needs to be. It says: keep the donkey. You put the key in the hand of a girl of nineteen who has never been bribed, and she holds it as if it were warm.", "@Linden| (reading the letter) You've put a footnote."},
                        new String[]{"* You close the ledger. It makes a soft final sound, like a hand on a table. You lay the pen across it, and put on your coat. The Seat is empty, and the flags beneath it are very cold, and under them, somewhere, a latch."},
                        new String[]{"* You stand at the Seat with a lantern. People go through by ones and twos, and each time you ask the same question: has anyone charged you? Nobody has. It is the cheapest and best bit of accounting you have ever done."}))
                .outro(
                        "* The city goes on without a bell for a week. Then the children ring it, for no reason, on a Tuesday, and the whole high street comes out to listen. On the gate, on the first of the month, there is a new note in a hand you do not know. It says only: the sums are lovely.")
                .pay(1000, 2, 0, 2, 1, 2);

        epi(d,
                "?latch=1|* The key is in good hands. The council have kept the donkey. They have kept the footnote, too, and read it aloud on feast days.",
                "?latch=2|* The ledger is closed on your desk with the pen across it. The city leaves it as it is.",
                "?latch=3|* On the last day of the month a man with a lantern stands at the Empty Seat and asks every traveller the same question. Nobody has ever lied to him.");
    }

    // ------------------------------------------------------------------ the Orphan of the Ash Road
    private static void orphan(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The Ash Road is a road with a shrine on it, and a house, and a lantern in the window that nobody has let go out. You have walked it more often than any other person living. You know the stones.",
                "* One evening, at the shrine, a lantern comes down the road by itself. It is carried by a boy of nine in a man's coat. He says he was sent. He says the old woman at the end of the road wants her tea.");
        d.season(n, "The Door");

        door(d, "dr1", "The Last Lantern").years(2)
                .intro(
                        "* The boy sets the lantern on the shrine step and waits. It is a plain tin one, with a candle stub in it, and a piece of paper tied to the handle with a thread. The paper says, in a hand you know from the flyleaf of a book of tales: Come and see the end of the road. There is a kettle.")
                .wait(8)
                .outro(
                        "* The road runs past the last village, and the last field, and a long grey stretch of heath, to a house with no door. The house is just a roof on four posts, with a stove and a table and two chairs. A woman sits in one. She is old and small and her eyes are white. She is pouring tea into two cups before you reach the step.",
                        "@Hesper| I knew your step. I'd know it in my sleep. I haven't been able to sleep, you know. Not since the road ended. There is a place just there where it goes into the ground. I thought you'd like to see it.",
                        "* She is dead, of course, and has been for years. It makes no difference. You sit down and drink the tea. It is hot. It tastes of rain and smoke and sugar. She tells you a tale about a girl who was given to a tinker, and what the tinker's wagon was for.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr2", "Seven on the Road").gap(26)
                .intro(
                        "* The people you collected on the road come to the Garrison together, in a wagon with seven wheels that Corvo built out of two. Ilan sits on the box. Perpetua has baked. The wagon rocks on the plain like a ship, and everybody on it is talking at once, and the horse looks entirely resigned.")
                .at("garrison", "Take the wagon to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* They go in the order they joined. Ilan first, with a kettle and a letter. Perpetua with a cake and a small bridge of sugar. Corvo with a ring on a string. They stop at the gate one by one, and turn, and each of them says the same thing. They say it in a different voice. They say: it was a good road.",
                        "?lovet=1|@Lamplighter| (at the end of the line, with a lamp) I'll keep the last light, if you'd rather. I'd like to go on keeping it.",
                        "* The gate is a plain arch, with no door. A grey rider waits there to count the lamps. When the last of your seven has gone in, she writes a number in her ledger, and it is not the right number. She looks at you. She crosses it out. She writes eight.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr3", "The End of the Road").gap(26)
                .intro(
                        "* You walk to the Empty Seat on foot, as you walked everywhere. It takes four days. You come into the great hall at dusk with the dust of the Ash Road on your boots and a lantern in your hand. The Wicks, who have never seen anyone arrive on foot, make a way for you without a word.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(latch("The Door is under the Seat. The road ends there, or goes on.",
                        "Stay: the road ends here, and you are its last light.", "Go: walk it to the end when the lantern burns down.", "Hold the latch: be the one at the Door with a lamp for those who come after.",
                        new String[]{"* You light the lantern from the one in your hand and set it on the flags at the foot of the Seat. It burns without oil. The Wicks stand round it, silent, and then one by one they light their own from it, and carry them out into the hall. By midnight the whole Palace is lit by small flames that came from a road.", "@Hush| (at the edge of the light, very softly, with a cup) May I?"},
                        new String[]{"* You say that you will walk it to the end. You say it quietly. A draught comes up under the Seat that smells of rain and smoke and sugar, and the lantern leans toward it like a plant. You are not sorry. You are a person who has never stayed anywhere long enough to be hurt, and you find you are not afraid of being on the road for good."},
                        new String[]{"* You sit down by the Seat with your lantern. All night the people come through, a few at a time. You hold up the light and say good evening, and they say it back. At dawn you realise that you have not once thought of yourself, and that it is the first time since you were small."}))
                .outro(
                        "* The Palace gives you a bed. You do not sleep in it; you sit in the window and watch the sun come up over Originis, and the lamps going out one by one in the streets, and the smoke rising, and the first carts. Somewhere below, in the dark, a latch clicks, softly, and does not click again.")
                .pay(1000, 2, 0, 2, 1, 2);

        epi(d,
                "?latch=1|* The lantern at the foot of the Empty Seat has never gone out. The Wicks light their own from it on their first night.",
                "?latch=2|* You have left the door of the house on the Ash Road unlatched. There is a kettle on the stove.",
                "?latch=3|* A lamp is lit at the Seat every evening, at dusk. A Wick has been given the job of keeping it. He says he is told it was a person once.");
    }

    // ------------------------------------------------------------------ the Disgraced Disciple
    private static void disgraced(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The Pavilion is a school with a very loud library. The cat has become a legend. The breathing is taught in a thousand villages, in a dozen versions, none of them quite the one you wrote.",
                "?love2=1|* Mei keeps the accounts and the cat, and has taken to leaving a note on your pillow: Breathe.",
                "* A letter comes from the old inn at the foot of the hill. It is from Elder Zhao. It is four lines long, in a firm narrow hand, and it says that he is going on a journey.");
        d.season(n, "The Door");

        door(d, "dr1", "A Letter from Zhao").years(2)
                .intro(
                        "* Elder Zhao is ninety-three and lives in a hut at the edge of the Pavilion's grounds. He has not been to the library in a decade. He sends a boy to ask whether you will come and carry a book. It is a very small book. It is the only one he has not given away.")
                .wait(8)
                .outro(
                        "* The hut is bare and clean. There is a bed, a table, a window onto the hill. He sits in a straight chair with a blanket on his knees and his hands folded, a thin old man with a face like a closed fan. He tells you that he has been called. He says he has been called for ages, and has put off answering. He says he does not like to go without paying his debts.",
                        "@Zhao| I expelled you for reading half a scroll. I want you to know I read the other half. I read it forty years before you did. I put it away because I was afraid. I should like you to have the cover.",
                        "* He puts the small book in your hand. It is bound in plain grey cloth, and it is empty. On the inside of the cover, in a hand that has not changed in sixty years, he has written: For the one who did not turn.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr2", "Zhao's Bag").gap(26)
                .intro(
                        "* You carry his bag to the Garrison. It is a small cloth bag, with a flask in it, and a change of linen, and a cheese. He walks with a stick, slowly, and does not take your arm. He says he would like to arrive on his own feet. He says it is a thing a person ought to do once.")
                .at("garrison", "Walk with Elder Zhao to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* The line is long and wet. He stands at the end of it with his stick. The people in front of him do not know who he is. He does not tell them. He asks a young soldier how long the wait is, and the soldier says an hour, and Zhao says that is perfectly reasonable, and begins to teach him a way of breathing.",
                        "?guest=1|@Zhao| (aside, to you) The Guest who taught you the last line. She was here before me. She is somewhere in the Garrison, I am told, drinking tea. I should like to say I forgive her. I find I don't know what for.",
                        "* He takes his bag. He shakes your hand, once, firmly, like a man closing a contract. He goes in at the gate between a woman with a lamp and a boy with a spade. He does not look back. At the threshold his shoulders drop, a very little, the way a man's do when he sets down a weight he did not know he carried.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr3", "The Empty Page").gap(26)
                .intro(
                        "* You take the grey book to the Empty Seat. You have not written in it; you have carried it for a year in your sleeve, and opened it perhaps a hundred times, and each time you have shut it again. Now you sit on the flags with your back to the chair that nobody sits in, and a pen in your hand, and think about the first line.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(latch("The book is empty. The Door is under the Seat.",
                        "Stay: write the first page, and leave it with the school.", "Go: leave the book blank, and walk through with it.", "Hold the latch: sit by the Door and write down the name of each person who goes in.",
                        new String[]{"* You write: Breathing in, you hold the world. Breathing out, you set it down. Then you stop, because you have written it before, and it was only half true. You cross it out. You write underneath: You were allowed to pick it up again. You shut the book and send it to the library by a boy.", "@Mei| (reading it, later, with the cat on her lap) That's the whole of the thing."},
                        new String[]{"* You leave it blank. You tuck it in your sleeve, and rise. The Seat above you is empty, and the Door below it is a long way down. You think of a boy in a corridor with half a scroll, and what he would have said if someone had told him he would be allowed to keep it. You are not afraid."},
                        new String[]{"* You write names. Nobody asks you to. You sit on the flags and write each one down as it passes, in your neat small hand, with the date, and a word about them if you can. By morning the book is half full. It is the first thing you have ever written that nobody else will be able to say you were wrong to read."}))
                .outro(
                        "* A cat is sitting on the Seat when you leave. It is a large grey cat with a white bib and an expression of calm authority. It watches you go. It does not follow. It lifts one paw, politely, as one lifts a hand at a window, and puts it down again.")
                .pay(1000, 2, 0, 2, 1, 2);

        epi(d,
                "?latch=1|* The first page of a grey book hangs in the library at the Pavilion, in a frame. Students are not allowed to touch the glass.",
                "?latch=2|* The grey book is empty. You keep it in your sleeve. It has never felt lighter.",
                "?latch=3|* The grey book in the Palace is nearly full. A Wick has been given the job of bringing it a fresh pen each season.");
    }

    // ------------------------------------------------------------------ the Blacksmith's Child
    private static void smith(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The forge has three names on the sign now, and a fourth, in a child's hand, that nobody will paint over. The blades go out, and the stories come back. You keep the hammer by the door.",
                "?tamgo=1|* Tam keeps the bellows. He says he is only keeping them warm.",
                "* A blade arrives by the Seam in a plain sack. It is notched. It has a drawing of a bird on the tang.");
        d.season(n, "The Door");

        door(d, "dr1", "A Blade Comes Home").years(2)
                .intro(
                        "* It comes in a sack, left on the step in the night, with no note. It is a plain short sword, dull-edged and heavy in the hand, notched in eleven places. You turn it over in the light of the forge. You know the work. It is yours. You made it, a long time ago, for a boy who asked for a thing he could carry.")
                .wait(8)
                .outro(
                        "* The tang has a small drawing scratched into it, a bird, in a nervous careful hand. Under it, in the same hand, a line: I kept it sharp. I'm sorry about the notches. They were all for something. The grip is wrapped in a strip of green cloth that you do not remember. It smells of apples.",
                        "?tamgo=2|@Pia| (looking over your shoulder, with soot on her nose) He's alive. It's from him. It's his handwriting.",
                        "?!tamgo=2|@Pia| (looking over your shoulder, with soot on her nose) That's a blade from the Outside. I'd know the quench anywhere. Somebody has been looking after it.",
                        "* You hang it on the wall above the anvil. It does not look out of place. After a time you notice the other blades on the wall have turned a very little, all of them, toward it, as plants lean toward a window.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr2", "The Thousand and One").gap(26)
                .intro(
                        "* Elder Iron sends for you. She is at the gate of the Garrison with a lamp, in her leather apron, standing in the rain with her hands behind her back. She asks whether you would like to see where the blades go. She says she has never asked anyone to watch twice.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}, to meet Elder Iron.")
                .outro(
                        "* The line is long. Each of them has a blade at their side. You know most of them by the grip. There is the long one with the walnut handle, for the sergeant. There is the little curved knife for the girl from the salt flats. There is the plain blade you made for a man who would not lift it, and he is lifting it, and he has a smile on his face you have never seen on a soldier.",
                        "@Iron| I forged the first nine. I forged them for the nine of us. I gave each one a weight to carry. I have never been sure that was right. I am watching your hundred and first walk in at the gate, and I think it was.",
                        "* She puts a hand on your arm. It is hard and warm, and has a long white scar. She says nothing more. You stand beside her until the last of them has gone, and then she hands you the lamp, and walks into the dusk, and you do not follow.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr3", "Evenfall").gap(26)
                .intro(
                        "* Evenfall hangs in a sheath that thinks twice. You take it down on the first morning of winter and carry it, as you would carry a child that was a bit too old, to the Palace. The Wicks make way. The Seat is cold. The nine lamps burn without a flicker. Under your arm the blade makes a sound like a held breath.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(latch("Evenfall is in your arms. The Door is under the Seat.",
                        "Stay: hang Evenfall on the wall of the Palace forge, for Elder Iron's successors.", "Go: carry it through, and leave it where it can be borrowed.", "Hold the latch: stand by the Door and hand a blade to each one who has none.",
                        new String[]{"* You hang it in the forge under the Palace, on a hook that Elder Iron's hands have worn. It settles. The whole room seems to breathe out. A young smith with a burned apron stands by the door, very pale, and asks if she may touch it. You say she may. She does. Nothing happens, and she bursts into tears.", "@Iron| (from the doorway, gruff) That is exactly what is supposed to happen."},
                        new String[]{"* You stand with it in the doorway of the Seat. You stay on the threshold and say that you will go in when the forge is quiet. You find that you have said it in the voice you use for customers, and that it is a promise, and for once you do not mind that it is a long one."},
                        new String[]{"* You spend the winter at the Seat with a bag of knives and a whetstone. You hand a blade to each one who comes without. They look at it, and at you, and go through. It is the humblest work you have ever done. Elder Iron stands at your back some nights with her arms folded, and does not say a word."}))
                .outro(
                        "* The forge at home has a new sign. It is hand-lettered, in black paint, in a hand that is yours and Pia's and Tam's and a child's. It says: WE MAKE THINGS. THEY GO WHERE THEY ARE NEEDED. It hangs under the old sign. The wind does not swing it. Nobody has asked who hung it.")
                .pay(1000, 2, 0, 2, 1, 2);

        epi(d,
                "?latch=1|* Evenfall hangs in the Palace forge, on a worn hook. The young smiths touch it before their first quench. Nothing happens. They say it helps.",
                "?latch=2|* There is a blade by the door of the forge, with no scabbard, and a promise attached to it that is not yet due.",
                "?latch=3|* A man with a bag of knives stands at the Empty Seat on winter evenings. The Wicks say he gives them away.");
    }

    // ------------------------------------------------------------------ the Beast-Tamer's Daughter
    private static void tamer(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The four walk the roads slowly now. The cinder boar sleeps through whole weeks. The crane has gone grey at the crest. The lake is where it was. The whistle has not been blown in a year.",
                "* One evening the stag from the Palace's gardens comes down the road and stands at the gate. He is not a stag. He is a messenger, and he has the manners of one.");
        d.season(n, "The Door");

        door(d, "dr1", "The Messenger at the Gate").years(2)
                .intro(
                        "* The stag at the gate is white as a bone and old as the road. He does not graze. He stands in the yard with his head up, looking at nothing, until you come out with the bone whistle in your hand. Then he lowers his antlers and kneels. A strip of bark is tied to the left tine with a thread of moss. It has writing on it.")
                .wait(8)
                .outro(
                        "* The writing is in no hand you know. It is slanting and clean. It says: Bring them to the plain. There is a gate. It cannot take them through, because it was made for people. It would like to say goodbye. It says the beasts are owed a word, and that the person who is asked to give it is the one they listen to.",
                        "?!dead.fox|?!slept=1|* The fox, who is very old, comes out of the house and sniffs the bark. She sneezes. She walks twice round the stag, sniffing, and sits down on his foot. It is the clearest approval you have ever seen from her.",
                        "* The stag rises. He walks out of the yard at a slow dignified pace. At the bend of the road he stops and looks back. You get your coat.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr2", "The Gate Was Made for People").gap(26)
                .intro(
                        "* The four go with you to the Garrison, one behind the other, on the long road. The cinder boar leads. The crane rides on his back. The tortoise is carried in a cart, and the fox walks beside it, with her tail up, as if she had arranged the whole matter.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}, with the four.")
                .outro(
                        "* The line stops at the gate. A woman with a grey plait and a ledger comes down it. She looks at the four. She looks at you. She says that the gate is a muster, and that a muster is for people, and that she has never been asked for a rule about beasts. She says there is no rule. She says it in the voice of someone who has just realised how odd that is.",
                        "* The boar walks to the gate and puts his nose to the sill. The crane preens. The tortoise withdraws her head and thinks about it. The fox sits down in the dust and begins, very calmly, to wash. They do not go in. They do not need to. One by one they turn, and look at the line, and the line looks back, a hundred soldiers in the rain, and not one of them is afraid.",
                        "@Woman| I will write it down. I will write that they came. It is the first time anyone has asked.")
                .pay(300, 0, 0, 1, 0, 1);

        door(d, "dr3", "Six Notes and a Seventh").gap(26)
                .intro(
                        "* The Empty Seat is in the great hall of the Palace, under the dome. You have not been in it for years. The Wicks bring a mat and a bowl of water for the beasts, and then, awkwardly, leave you alone. The four settle at the foot of the chair. The whistle is warm in your hand.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(latch("The beasts are at your feet. The Door is under the Seat. Your mother's tune is in your hand.",
                        "Stay: blow the whistle once, and let the tune go through alone.", "Go: sing the six notes, and the seventh, and walk with the tune as far as it goes.", "Hold the latch: stay by the Door and see the next of the wild safely to the gate.",
                        new String[]{"* You blow the whistle. The six notes go out into the hall and down under the flags, and then the seventh, which is not yours. It goes through the Door. You hear it a long way off, small and clear, and then you hear it answered. The beasts lift their heads in a body. The fox, in the dust, begins to sing.", "@Fox| (in your head, with immense dignity) That is the right one."},
                        new String[]{"* You hum the six notes. The seventh comes up out of the floor to meet you. It is a deep slow warmth, like a coat put around the shoulders. You are not afraid. You find that you have never been afraid of this. It is your mother's voice, and the wild's, and it has been calling the whole of your life."},
                        new String[]{"* You sit at the Seat for a year. Whenever a beast comes in at the great doors, a stray, a hunted thing, an old bear, you take it by the scruff and say good evening. You show it the water. You show it the Door. You tell it that nobody will mind. By the end of the year you have a very large number of friends and a stiff back."}))
                .outro(
                        "* The beasts go home. The fox leads. She does not look back; she is a fox, and has appointments. At the gate of the house she stops and sits and waits for you, and when you come up she butts your knee with her head, hard, once, as she did on the first night. It means: well done. It means: I was never worried.")
                .pay(1000, 2, 0, 2, 1, 2);

        epi(d,
                "?latch=1|* On certain evenings the seventh note comes up out of the ground by the lake, small and clear. Nobody has ever been able to say who is playing.",
                "?latch=2|* You hum the six notes in the garden. The seventh is already there, waiting, and has been all your life.",
                "?latch=3|* A stray fox, an old bear, a wounded hawk are all, sooner or later, brought to the Seat. A tall tired person gives them water and tells them they are welcome.");
    }
}
