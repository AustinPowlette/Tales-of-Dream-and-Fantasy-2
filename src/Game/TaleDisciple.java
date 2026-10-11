package Game;

/** The Wandering Immortal's Disciple (see "Story Plan.md"). A drunk old man taught the player three moves and vanished. Each move comes back as a memory, and each memory holds one of the
 *  answers he gave when asked his name (the moon, the road, the gourd). At the end he comes back for one night, and gives the name he uses here. He does not belong to the Garden, and
 *  the story never says where he is from: only small things are wrong about him (he never ages, his wine tastes of a place, he thinks a year is short, he hums an old tune) and one
 *  thing is said that could be a message. Nothing here explains the Palace or the Outside. A few short tales (episodes) of other people who met him come up now and then. */
final class TaleDisciple {
    private TaleDisciple() { }

    /** Whether this human has read this place of the Palace's story (so a story may notice what they know). */
    static int read(Character c, String site) { Lore.PState p = Lore.pstate(c); return p != null && p.found.contains(site) ? 1 : 0; }

    static void register() {
        Chapters.Def d = Chapters.def("disciple");
        d.place("inn", "far").place("pass", "wild").flags("sawgourd");
        d.episodes(3, 3);

        d.chapter("moves", "Three Drunken Moves")
                .intro(
                        "* You remember it the way you remember a smell. Not clearly. All at once.",
                        "* You were ten. It was an inn courtyard, at night, with paper lanterns and a smell of spilled wine. An old man sat on a barrel, scratching his beard, with a red gourd on his lap that was never empty.",
                        "* You were watching him because everyone else had stopped. He was swaying, humming, and you were sure he was drunk. Then he stood up, and he was not drunk at all.",
                        "@Old man| Watch. Don't try. Just watch. Once. Twice. Three times.",
                        "* He showed you three moves, slow as honey, in the lantern light. When he finished, he sat down on his barrel and looked at you for a long time.",
                        "@Old man| That's all of it. It isn't much. Don't try to be clever with it. A clever person will improve the moves and lose what they are for.",
                        "* In the morning he was gone. All that was left on the barrel was a small clay cup, and a rice-paper note in his terrible handwriting: 'Be good. Don't drink on an empty stomach.'",
                        "* You have never forgotten the moves. You have never learned his name.",
                        "* Now you are eighteen, and you are ready to learn what they are for.")
                .realm(1, "Master the first of the three moves: reach Major Lvl 1.")
                .outro(
                        "* The first move is easy now. It was never hard. It was only quiet.",
                        "* It is called Cup Raised to the Moon. You hold up one hand, as if holding a cup, and you tilt your head back, and you let the whole sky tip in.",
                        "* As you finish it, another memory surfaces, small and sharp. You asked him his name that night. You were ten, and not shy.",
                        "@Old man| My name? Ask the moon, lad. She's known me longest.",
                        "* You look up. The moon looks back. She does not say anything. You think, though, that she has a good idea.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("inn", "The Corner Seat").years(1)
                .intro(
                        "* It takes a year of asking. Most people look at you as though you had asked for the loan of a unicorn.",
                        "* Then an innkeeper, in a far town, says something strange over a cup of cold tea.",
                        "@Innkeeper| An old man with a red gourd? Oh, aye. He had the corner seat for thirty years. My father's inn, then mine. Never aged a day. Nobody saw him arrive. Nobody saw him leave.",
                        "@Innkeeper| You'd better come and see. He left something.")
                .at("inn", "Visit the old inn at {@inn}, where an old man sat for thirty years.")
                .outro(
                        "* The inn is a long, low, warm building, full of people laughing. In the corner is a table with one chair. Nobody sits there. A little brass plate on the table says Reserved, in a hand very like the one on your rice-paper note.",
                        "@Innkeeper| I never took the plate off. He asked me not to. He said he'd be back, only he didn't say when. I don't like to disappoint a regular.",
                        "@Innkeeper| He paid in coins I've never seen before. Small, gold, with a picture on them of a bird I couldn't name. I kept them. Here.",
                        "* She puts one in your hand. It is warm. On one side is an animal like a crane. On the other, in small letters: the writing is not any alphabet in this country.",
                        "@Innkeeper| He used to hum, too. Always the same tune. Slow, a bit sad. I've heard it in my sleep.",
                        "* You sit in the corner seat for a quarter of an hour. Nobody stops you. The chair is exactly the right height. You feel as though you have been here before, and as though somebody has just left.")
                .pay(500, 0, 0, 0, 0, 0);

        d.chapter("pass", "The Second Move").years(1)
                .intro(
                        "* The second move is harder to remember. It is not hard to do. It is hard to want to do. It looks like a drunk man falling over.",
                        "* You know exactly what it is for, though, and you know where to try it. The Weeping Pass is an old trade road, and the bandits there have been waiting a long time for something to do.")
                .task("KILLS", 15, "", "", "Test the moves where they were meant for: fifteen kills.")
                .outro(
                        "* It takes a season. It is not what you expected. You do not strike. You stumble. You turn, you sway, you lunge toward the man you want, and fall into him, and he is on the floor and you are not.",
                        "* It is called The Stumble That Is Not a Fall. Never aim, the old man told you. Fall toward the one you want to stop.",
                        "* On the fifteenth night, in a roadside shrine, you remember the rest of the lesson.",
                        "@Old man| The road. You can ask the road, too, if you've a mind to. Everything I know I learned on foot.",
                        "* He was pouring wine into the shrine's little offering bowl while he said it. He did it every night. The god, he said, was a lonely type.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("wine", "A Taste of Somewhere").years(2).major(2)
                .intro(
                        "* A wine merchant in the city has been talking in the market. She says she bought a gourd of something extraordinary from a peddler who traded it for a song. She says it cannot be priced. She says that once you taste it, ordinary wine tastes like water.",
                        "* You know before she says it. She has a red gourd.")
                .wealth(20000, "Even immortals drink: hold 20,000 spirit stones to buy what the merchant keeps.")
                .outro(
                        "* She names a price. You have it in your hand. She pours you a thimbleful, as though handing over a baby.",
                        "* You taste it. It is not sweet. It is not strong. It tastes like a place. It tastes like a long, long way from here, in the evening, with the windows open.",
                        "* You cannot say what the place is. You can only say, with total certainty, that you have never been there, and that he has.",
                        "@Merchant| The peddler said something odd as he left. He said, 'A year isn't very long, where I come from.' I thought he meant he was old.",
                        "* You buy the gourd. It is heavy. It never empties.")
                .pay(0, 0, 0, 0, 3, 1);

        d.chapter("road", "The Road That Is Not Finished").years(2).major(2)
                .intro(
                        "* You dream. It is not a dream you have had before.",
                        "* You are on a road at the edge of the world. It runs on ahead into a mist. The paving stones are good at your feet and thin ahead, and then not stones at all, just a faint grey suggestion of the way.",
                        "* Far in front of you is an old man in a ragged coat, with a red gourd at his hip. He is walking slowly, and testing the air with his hand, like someone feeling for a draught under a door.",
                        "@Old man| (not turning) Is that you, lad? Don't come any closer. The road's a bit thin here. Wait till you're ready.",
                        "@Old man| I'm nearly there. I think I've found the crack. It's very small. I've been looking for years.",
                        "* You try to speak. The dream will not let you. He stops, and tilts his head, and you think he may be smiling.",
                        "@Old man| Third move. You know it. You've always known it. I only had to remind you.")
                .realm(3, "Rise far enough that he might return: reach Major Lvl 3.")
                .outro(
                        "* The third move is called Empty Gourd, Full Sky. You tip the gourd all the way over, and there is nothing in it, and you let your arms fall wide, and you are not afraid.",
                        "* It is not a movement to hit anybody. It is a movement to be hit by. You let the whole world come in, and you do not close.",
                        "* On the night you finish it, you remember the last answer. He gave it very late, on the night he left, when he thought you were asleep.",
                        "@Old man| If you want to know my name, ask the gourd. It's the only thing that never lied to me.",
                        "* You hold up the red gourd. You turn it over. There is a name scratched under the stopper in tiny letters, so worn you can hardly read it.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("cup", "The Last Cup").years(1)
                .setup((c, st) -> Chapters.set(st, "sawgourd", read(c, "garrison")))
                .intro(
                        "* You go back to the inn. You did not decide to. Your feet decided.",
                        "* The corner table has a lantern on it tonight, and the lantern is lit. The Reserved plate is gone.",
                        "* The innkeeper stands in the doorway, wiping her hands on her apron, and she looks at you as though she wants to say something and cannot find the first word.")
                .at("inn", "Go back to the corner seat at {@inn}.")
                .outro(
                        "* He is sitting in it. He is exactly the same. The beard, the coat, the red gourd at his hip, the look of a man who has just remembered a very good joke.",
                        "@Old man| Ah. There you are. You've gotten tall. Sit. Don't stand there like a stork.",
                        "* You sit. He pours into the little clay cup, and hands it to you. It is the cup he left behind, years ago. It has been mended with gold.",
                        "@Old man| You kept it. Good. Most don't.",
                        "@You| You never told me your name.",
                        "@Old man| Didn't I? How rude. It's Wen Jiu. Warm Wine. It isn't the first one. The first wouldn't fit in your mouth. Ask me again when you've seen the sky from the other side.",
                        "* You do not ask what he means. You have the feeling you are not meant to.",
                        "?sawgourd|* You think of a long stone in an empty fortress, and a small drawing of a gourd beside the words The Wandering Immortal. You don't ask. You have the feeling he knows what you saw.",
                        "@Old man| I've signed a few margins in my time. 'A guest.' People think it's modest. It's only accurate.",
                        "@Old man| I can't stay. I've left it too long. I only came to see if you'd got there. You have. Almost.",
                        "@Old man| There's no fourth move. I never found it. Perhaps you will. That's the thing about a teacher, you see. He's only the first pair of hands.",
                        "* He stands. The chair scrapes. He pats your head, absently, like a boy's. He is humming, softly, a slow tune you have heard all your life.",
                        "@Old man| If you ever meet a quiet man in a very big garden, tell him I found the crack. And that I'm sorry I'm late.",
                        "* The lantern flickers. When it steadies, the chair is empty. The cup in your hand is warm.")
                .outChoice(new Chapters.Choice("cup", "He is gone, and the cup is still warm.",
                        "Pour the first cup out onto the floor, for him.", "Drink it yourself, to the dregs.", "Put it back on the table where it was.")
                        .after(
                                new String[]{"* The wine soaks into the boards. For a moment the whole inn smells of somewhere else. Then it is only an inn again, and you pour another."},
                                new String[]{"* It tastes of a place you have never been. You swallow. For one long moment, you stand at an open window at evening, in a country with a different sky."},
                                new String[]{"* You set it on the corner table, on the brass plate. The innkeeper nods. She will not move it. Nobody will."}))
                .pay(5000, 0, 0, 2, 0, 2);

        d.epilogue(
                "* You do not become a great master. You become a person who falls toward the one they want to stop, and tips the whole sky into a cup.",
                "?cup=1|* On the night of every full moon you pour a cup out for him on the inn's boards. The innkeeper pretends not to see. Her daughter has started to do it too.",
                "?cup=2|* You drank that cup, and you have never again tasted anything quite like it. You have never stopped trying.",
                "?cup=3|* The cup is still on the corner table. People say it is warm. Some say they have seen it refill.",
                "* Sometimes, in the evening, you take out the red gourd and look at the name scratched under the stopper. It does not say Wen Jiu. It says a word in a country you do not know.",
                "* You hum a tune. Slow, a bit sad. You find you know it better than you thought.");

        // a few short tales of other people who met him
        d.episode("song", "A Song in the Tavern").odds(25)
                .when((c, st) -> st.chapter >= 1 && c.location != null && c.location.city)
                .intro(
                        "* The tavern is loud, but one voice carries. A drover at the bar is humming into his beer, a slow tune, a little sad, with the same four notes in a row.",
                        "* You walk over. You are not sure why.",
                        "@Drover| This? I got it from an old man on the Ash Road. Years ago. Wouldn't tell me where it was from. Said it was a lullaby for someone who couldn't sleep.",
                        "@Drover| He bought me a drink, then he walked off, and I never saw him again. I've had the tune in my head ever since. Funny, that.",
                        "* You buy him another beer. The song goes on in your head until morning.")
                .pay(0, 0, 0, 0, 0, 0);

        d.episode("joke", "The Beggar's Joke").odds(25)
                .when((c, st) -> st.chapter >= 1)
                .intro(
                        "* A beggar on a bridge holds out a bowl and tells you a joke. You are not in a laughing mood, and he can see it.",
                        "@Beggar| What did the monk say to the wine jar? 'You're half empty.' And the jar says, 'No, you're half full.' And the monk says, 'No, no, I'm a monk.'",
                        "* It is not a good joke. You laugh anyway, because it is exactly the kind of joke you have heard before, from the same person.",
                        "@Beggar| An old drunk gave it to me, a long time ago. Said it was the oldest one he knew.",
                        "* You give him a coin. He tells you the joke again, slower, in case you missed it.")
                .pay(0, 0, 0, 0, 0, 0);

        d.episode("game", "Children's Game").odds(25)
                .when((c, st) -> st.chapter >= 1 && c.location != null && c.location.city)
                .intro(
                        "* In a village square, a ring of children is playing a game. One stands in the middle and sways, with a stick for a gourd, and the others try to catch him.",
                        "* They never can. He always falls toward the one who is about to grab him. They shriek and scatter and do it all again.",
                        "@Child| It's called Drunken Master! You have to fall at people! My gran taught it to me, and her gran taught it to her!",
                        "* You stand there for a long time, watching. A game this old must have come from somewhere. You are not sure who is imitating whom.")
                .pay(0, 0, 0, 0, 0, 0);
    }
}
