package Game;

/** The Ghost-Touched, season 4 (2026-10-20): "The Usher's Apprenticeship" (the comic voice with a gentle centre). Mister Tally of the Office of Returning wants a successor. The human learns the Usher's Manual (nine hundred pages),
 *  sees the Office from the inside, deals with the dead who will not leave, the goose, a child, a friend, and a ghost of a ghost. Choices: usher (what the human takes on), friend (how a friend is seen off).
 *  Flags: usher, friendgo. The Door season (StoryDoors) comes after. */
final class TaleGhostMore {
    private TaleGhostMore() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "usher", "friendgo");
        d.place("corridor", "wild");

        d.interlude(3, "A Course of Instruction",
                "* The Office of Returning has a new wing, a pigeonhole for every dead person in the province, and a queue that has never grown shorter. Mister Tally has stopped walking the pipes. He sits by the stove with his pen, and watches you work with an expression like a man trying to hide a good idea.",
                "* A parcel arrives in brown paper tied with string. It is the size of a paving stone. It is labelled in a hand like a row of fence posts: USHER'S MANUAL. VOL. I OF IX.");
        d.season(4, "The Usher's Apprenticeship");

        d.chapter("manual", "A Course of Instruction").years(3)
                .major(5)
                .intro(
                        "* The Usher's Manual is nine hundred pages. It is bound in grey cloth, in nine volumes, with a ribbon in each. You read the first volume in a week with your feet on the stove and a plate of dumplings. It begins, as all great works do, with a definition. An Usher is a person who sees the dead to the door. It goes on, for eight hundred and ninety-eight pages, to explain what a door is.")
                .wait(4)
                .outro(
                        "@Tally| (stiffly, on the stool, with his pen) I have been an Usher for a hundred and nine years. I was an Usher before the Office had a building. I have never had a successor. I have had a number of very capable clerks. I find I do not want a clerk. I want someone who will sit with them while they are dead.",
                        "@You| I already do.",
                        "@Tally| Quite. You do it badly. You do it with enormous warmth and no procedure. I should like to give you the procedure. You are not obliged to take it.",
                        "* He opens the second volume at the ribbon. The page is headed: Of the Dead Who Will Not Leave. Underneath it there is a single sentence, in his own tiny script. It says: Do not argue. Ask what they are waiting for.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("stay", "The Dead Who Will Not Leave").gap(8)
                .intro(
                        "* Mrs. Halloran has been dead for eleven years. She sits in the kitchen of her old house in Tanner's Row, in her chair by the range, in her apron, with her hands in her lap. She has not moved. She has been sitting there since the Tuesday she died, and the family who live in the house now, a very nervous young couple with a baby, have taken to going out the back way.")
                .wait(4)
                .outro(
                        "* You ask what she is waiting for. It takes an hour. She is not rude. She is merely preoccupied. At last she says that she is waiting for the kettle. She says she put it on, and then she had a turn, and she has been waiting ever since for it to boil. She says she is a woman who does not leave a kettle.",
                        "* You go to the range. The kettle is there. It is black with age and bone-dry, and there is no fire in the stove. You fill it from the pump. You light the fire. You sit with her for forty minutes in the quiet kitchen, with the baby crying upstairs, and the young couple on the stairs in their nightclothes, and the kettle begins to sing.",
                        "@Mrs. Halloran| (rising, with great dignity, as the steam goes up) There. I knew it would.",
                        "* She takes off her apron and folds it. She puts it on the chair. She goes out of the back door without a word and does not come back. On the step there is a single dry leaf. The kettle is still singing. The young woman pours a cup of tea from it and sits down at the table and weeps.")
                .pay(200, 0, 0, 1, 0, 1);

        d.chapter("appeals", "The Appeals Desk").gap(8)
                .intro(
                        "* The Office has an Appeals Desk. It is at the end of a long grey corridor, behind a pillar, and it is staffed by a grey woman called Miss Parr who has not been seen to blink. Anyone who is dead and displeased with the arrangements may lodge an appeal. They do so in great numbers. The queue goes down the corridor and out of the door and round the pump.")
                .friends(5, "Learn the Office: make five friends.")
                .outro(
                        "* You sit with Miss Parr for a week. She hands you the day's cases on a clip. A man who was promised a funeral with a band and got a cart. A woman who wishes it recorded that she was right about the bridge. A boy of nine who would like to know why he was not allowed a dog. They stand in front of the desk, in their last clothes, translucent and firm, and state their cases in turn.",
                        "* You hear them out. It takes two hundred and eleven days. at last you settle them all, by one method or another: a band, a letter, a plot of ground, a puppy that appears from nowhere on the steps of the Office, spotted and ridiculous. Miss Parr says that it is the first time in sixty years the queue has gone down. She says that she would like it noted.",
                        "@Miss Parr| (with the faintest of smiles) Your handwriting is atrocious. It is a pleasure to see it.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("pipes4", "The Names on the Pipes").gap(8)
                .intro(
                        "* Tally takes you down into the Cistern's long gallery on a night of rain. It is a place you have been before. It is a hall of pipes, grey and tall, in rows, each with a brass tag. You have never until now been shown the tags. He lifts the lamp. He reads one aloud. It is a name, and a date, and a word. The word is: Returning.")
                .wait(2)
                .outro(
                        "@Tally| Every pipe is a person. Every person is a pipe. When they are done, they go in. They go round. They come out as water, and the water feeds the roots, and the roots feed the herbs, and the herbs feed the pills, and the pills feed the living, who go on and make more water. I used to find it a very cold sort of arrangement. I do not any longer. I find it extremely civil.",
                        "* You walk the gallery with him. The names are in eleven languages. There are three that no one can read. At the far end, in a niche, there is a pipe with a clean brass tag and nothing written on it. Tally stops. He says that it is his own. He says that he has had it polished every ten years since the Office was founded, and has never once been tempted to fill it in.",
                        "@Tally| I am, you see, a little reluctant. It is a ridiculous thing in a man of a hundred and nine. I think you may be the first person to whom I have been able to say it.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("peter", "A Boy at the Gate").gap(8)
                .intro(
                        "* He is nine and wears a nightshirt. He comes into the Office at midnight through the wall, quite calmly, carrying a wooden horse. He sits on the bench in the hall with his bare feet dangling and looks about with interest. He has a round face and a missing tooth. He asks, in a small clear voice, whether this is where one gets sent. You say it is. He nods.")
                .wait(2)
                .outro(
                        "@Peter| I'm not frightened. I just wondered. I was in my bed, and then I wasn't, and there was a lady with a lamp who said I should come here. I think she was nice. She didn't say much. Is my mother coming?",
                        "* You tell him the truth, in the plainest words you have. You do not make it sound better than it is; you tell him that she will come, in time, a long time, when she is old. That he may wait for her, or he may go on ahead, and that either is allowed. He thinks about it. He holds the horse by the neck. He says that he would like to wait, if it is all the same, but that he would like it to be somewhere with a window.",
                        "* You give him the room at the top of the Office, with the window on the street. He sits in it for six years. He grows no older. He counts the carts. He has a view of the Mill Bridge, and the goose. The river, and in the evenings, when the lamps are lit, a woman in black comes slowly down the street and stops under the window and looks up. She cannot see him. She stays a minute. He waves.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("goose4", "The Goose Inspects").gap(8)
                .intro(
                        "* The goose has been appointed. Nobody has appointed it. It appears at the Office on a Monday morning, white and large and in a dreadful temper, and waddles down the corridor in front of Miss Parr, who says nothing. It goes into every room. It examines every pigeonhole. It nips a clerk. It sits in the Usher's chair with an air of proprietary calm and looks at you with one cold eye.")
                .wait(2)
                .outro(
                        "* It conducts an inspection of the Office that lasts nine days. It bites a number of ledgers. It rejects the stove. It pronounces on the tea, by overturning it. It examines the Appeals Desk, the Manual, the pipes. At the end it sits, in the middle of the great hall, under the lamp, and makes a long, low, sonorous noise like a small trombone. It is, Tally says, with a face of awe, the official finding.",
                        "@Tally| It is satisfied. It is the first time in a hundred and nine years. I should like it recorded that I did not think it could be done.",
                        "* The goose waddles across the hall to the door. It stops on the threshold. It looks back, at you, once, with an expression you have never seen on a bird: a sort of weary approval. Then it goes out into the rain, with its head up, and across the square, and is gone.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("chair", "Meng's Chair").gap(8)
                .intro(
                        "* Old Meng's chair stands in the corner of the Office, where it has stood for three years, beside the stove. It is a high-backed rocking chair with a patched cushion. It has not been sat in. It is not locked, or roped, or labelled. It is simply left alone, by common agreement, as one leaves a coat on a peg. This evening, as you pass, it is rocking.")
                .wait(2)
                .outro(
                        "* You stand and watch. It rocks four times, slowly, and stops. A cat comes out from under it, a small grey cat with a white bib, and jumps up onto the cushion and settles. It looks at you. It begins to purr. In the sound, if you listen, there is another sound, much fainter, like a woman humming over her knitting. You know the tune. It is the one she hummed when she was thinking.",
                        "@Meng's voice| (very faint, from the cushion, amused) You've taken your time. I wondered. I've been watching you with the dead. You are not a bad Usher. You are a sentimental one. It's the better kind. The procedure is only a handrail.",
                        "* The cat stretches. The chair rocks once more. The humming stops. On the cushion there is a small round dent, as if from a person who has just got up. You do not sit in it; you put a hand on the back and stand there a minute. Then you put a cup of tea on the arm, as one does for a guest, and go to bed.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("ghostghost", "The Ghost of a Ghost").gap(8)
                .intro(
                        "* The Manual has a chapter on it, in the ninth volume, in a footnote: Of the Dead Who Have Died Again. It is a very small footnote. It says that this is rare, and that the Usher is advised not to be alarmed. It says that the Usher is advised to be kind. It does not say what a person who has died twice looks like. You find out on a Thursday, at the foot of the stairs, in a dim hall, in the shape of a man who is a good deal less than there.")
                .wait(2)
                .outro(
                        "* He is thin and pale and nearly transparent, in a grey coat that is itself a ghost of a coat. He has a face like a pencil sketch of a face. He is very polite. He says that he was a clerk, once, in a counting-house, and that he died of a chill in the winter of the great fog. That he stayed for fifty years in the same office, copying a ledger. He says that last winter he died again, in a quiet sort of way, and found he was still there.",
                        "@Ghost| I wondered if you could tell me what one does. I have the oddest feeling of being not quite the thing. I keep forgetting my own name. I think it was Edwin. I am fairly sure there was something about a hat.",
                        "* You sit with him all night. You do not find his name; you find his hat, at last, in a cupboard at the back of the Office, a small black bowler with a worn band. You put it on his head. He looks at himself in the glass of the window, a vague thin shape under a hat, and says, in a voice growing steadier: 'That's me.' He is a good deal clearer by morning. He thanks you. He goes, still faintly, but with real purpose.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("marguerite", "Marguerite").gap(8)
                .setup((c, st) -> Chapters.adopt(c, st, "friend", "friend"))
                .intro(
                        "* The letter is from Marguerite Dale, who was sorry for thirty years and is now eighty-one. She writes in a hand that has begun to shake. She says that she is not ill. She says she is merely tired. She says that she has been reading about the Office, and its procedure, and that she would like, if it were not an imposition, to be seen out by someone who has done it before.")
                .wait(2)
                .choice(new Chapters.Choice("friendgo", "An old friend has asked to be seen to the door by you.",
                        "Do it by the book: every form, every procedure, so that nothing is left out.", "Do it as a friend: no book, no forms, a pot of tea and a chair.", "Do it both: the procedure at the Office, the friend at her own fire.")
                        .after(
                                new String[]{"* You do it by the book. It takes four days, and fills nine forms in triplicate, and Miss Parr stamps them with her own hand. The ritual of the Office is long and solemn and strange. Marguerite sits through all of it with her hands folded, listening intently, like a woman at a play. At the end she says that it was beautiful. She says she had not known a form could be.", "@Tally| (very low, in the corner) That is the first time in a hundred years that anyone has said that."},
                                new String[]{"* You do it as a friend. You sit with her in her little house on the edge of the town, with a pot of tea and the cat, and talk. You talk about the old days, and the old mistakes, and a great many things that were funny at the time. She falls asleep in the afternoon. She wakes at dusk, and smiles, and says: 'Oh, it's you.' It is a thing she says to the door, rather than to you, and you stay until it is open.", "@Marguerite| (a whisper) You were never any good at procedure."},
                                new String[]{"* You do it both. You walk her to the Office on the last day, arm in arm, in a thin spring rain. You do every form. You say every word. And then, at the door of the long room, you put away the book, and sit down on the bench beside her, and hold her hand. She tells you a joke. It is not a good one. You laugh until you cry. She laughs until she is not there, and her hand, in yours, is warm for ages afterwards.", "@Miss Parr| (at the desk, not looking up) I have stamped it. It was an irregular procedure. It was entirely correct."}))
                .outro(
                        "* The house on the edge of town is shut up in the autumn. The cat comes to live at the Office. It sits on Meng's chair with the other. Two grey cats, on one cushion, purring in turn.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("corridor", "The Long Corridor").gap(8)
                .intro(
                        "* The last volume of the Manual is not a book. It is a walk. It is the last chapter of the training, and it cannot be learned from a page. Tally takes you to the old well behind the Office, at dusk, and lowers a lantern on a rope, and down in the black a door is lit, small and round and brown. He says that this is the way the dead go. He says it runs under the whole of the Garden, to a plain at the edge of the world.")
                .at("corridor", "Walk the Long Corridor with Mister Tally, {@corridor}.")
                .outro(
                        "* It takes three days. It is a plain stone passage, lit by a lamp every thirty yards, wide enough for four to walk abreast. The air is cool and still and smells of old rain. There are doors in the walls, here and there, small and low, some shut and some ajar. Behind one, you hear children's voices. Behind another, a harp. Behind a third, an argument, in a language you have never heard, which is, you gather, over a game of cards.",
                        "@Tally| They are all in different rooms, you see. The dead. Each in the room they made. It is one of the things I have never quite explained.",
                        "* The corridor ends in a plain wooden door, with a latch of iron. It is closed. You lay your hand on it. It is warm. A faint grey light shows in the crack, and you can hear, on the other side, very far off, a hundred voices singing a hymn.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("lamp", "Lamp and Whistle").gap(8)
                .intro(
                        "* The Usher's lamp is a battered tin one, with a cracked glass and a dent in the cap. It has been in Tally's hands for a hundred and nine years. He gives it to you on the steps of the Office in the first light of a grey morning, with the same formal care with which a man hands over a baby he is not quite sure of. There is a whistle on a cord. He says it is for the geese.")
                .wait(2)
                .choice(new Chapters.Choice("usher", "Tally holds out the lamp. He is a hundred and nine, and has never had a successor.",
                        "Take it: you are the Usher now, and he may rest.", "Share it: take the lamp and keep him as Usher Emeritus, with a chair by the stove.", "Refuse: the Office needs a clerk and a kind person, and you will be the second.")
                        .after(
                                new String[]{"* You take it. It is lighter than you expected. The tin is warm, as from a hand. Tally lets go of it with a long thin sigh, like a boy letting go of a balloon. He stands on the step, with his hands empty, in the thin sun, looking, for the first time, genuinely old. Then he straightens. He says: 'Well. I shall see what a pipe is like from the inside.'", "@Miss Parr| (at the door, with her ledger, softly) Welcome, Usher."},
                                new String[]{"* You take it, and you keep him. He is installed in the corner chair by the stove, in a shawl, with a rug and a pen, and told to give opinions. He does so, relentlessly. For years you are the only Usher in the history of the Garden with a living predecessor who sends back your forms with red corrections. It is the happiest arrangement the Office has known.", "@Tally| (from the stove, with a pen) Your commas are an affront."},
                                new String[]{"* You refuse the lamp. You say that you will be the clerk. You say that you will be the kind person. You say that the lamp is for someone else. Tally looks at you for ages. He puts the lamp back in his pocket. He says that he rather thought you would. He says that he has long suspected the Usher is the person who does not want to be one. He says that he will keep looking.", "@Goose| (from the roof of the Office, one flat note, a kind of honk)"}))
                .outro(
                        "* You walk out into the square in the thin sun. The Mill Bridge is bright over the river. Under the bridge, in the clear water, a long line of small gold lights are drifting by. They are lanterns, set afloat the night before for the Festival of Hungry Ghosts. The last of them goes under the arch and out of sight. You stand and watch it go. Behind you, in the Office, a stove is lit, and a kettle begins to sing.")
                .pay(2000, 3, 0, 4, 2, 2);
    }
}
