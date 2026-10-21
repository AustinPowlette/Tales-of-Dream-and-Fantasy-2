package Game;

/** The Beast-Tamer's Daughter, season 4 (2026-10-20): "The Long Sleep" (an elegy in the plain pastoral voice). The four are old. Moss the lake tortoise goes under for a hundred years, the fox has a last litter, Quill flies into one more
 *  storm, Ember has a final autumn of apples, and the fox lies down. Choices: kits (what is done with the litter), slept (where the fox spends her last winter), farewell (what is said to the lake). Flags: kits, slept, farewell.
 *  The Door season (StoryDoors) comes after. */
final class TaleTamerMore {
    private TaleTamerMore() { }

    static void add(Chapters.Def d) {
        d.flags("kits", "slept", "farewell");
        d.place("oak", "wild");

        d.interlude(3, "Grey at the Whiskers",
                "* The sanctuary is a green place with a fence and a gate and a great many animals. Nobody counts them any more. The four walk its paths in the morning, slowly, one behind another, and you have begun to notice how often they stop.",
                "* The fox has a white muzzle. Ember has a limp. Quill's crest has gone the colour of ash, and the lake, in the evenings, is very still.");
        d.season(4, "The Long Sleep");

        d.chapter("grey4", "Grey at the Whiskers").years(3)
                .major(5)
                .intro(
                        "* You wake one morning and the fox is not on the bed. She is on the sill, in the sun, with her paws tucked under her. She does not look round when you speak. She has been sitting there since before light. She has the particular stillness of an animal who has just noticed something about her own body and is giving it her full attention.")
                .wait(4)
                .outro(
                        "* It takes the day to see it. Ember takes the orchard path at half his old pace, and sits down twice. Quill lands on the roof with a clatter and stays there, panting, with her wings half open. Even the tortoise, who is three hundred and has never been in a hurry, does not answer when you hum the low note from the shore. The water goes on being water.",
                        "* You do not need a vet to tell you what it is. Your mother used to say that beasts do not grow old the way a person does, by degrees, with a great deal of complaining. They go in a night. They simply turn grey, and slow, and one evening they are not at the door. She said it was the wild's way of being tactful.",
                        "@Fox| (in your head, from the sill, with her eyes shut) I am not ill. I am only tired. Don't make a face. I am telling you now so that you won't be surprised.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("under", "Moss Goes Under").gap(8)
                .intro(
                        "* The lake gives you the news by the way it looks. It has gone clear. Every leaf and stone on the bottom stands out in the light as if cut from paper, and a tall slow shape is moving in the middle, round and green and enormous. Moss rises beside the reeds with water pouring off his shell like a hill after rain. He says that he has come to say it properly.")
                .wait(2)
                .outro(
                        "@Moss| It is time. I have been awake three hundred and eleven years. It is customary at that age to sleep. A hundred years, give or take. I shall go to the bottom, where it is dark, and be still. I shall not know you. I shall not hear the low note. When I wake, you will not be here, and I ask you to not mind.",
                        "* You sit on the bank with your boots off. The water is cold and tastes of iron and reeds. You tell him that you do mind. You tell him very clearly. He listens with his old face tilted a little to one side. When you have done he says that it is the nicest thing anyone has said to him in a century, and the most unreasonable, and that he will remember it, in the dark, as a sort of lamp.",
                        "* He goes under at dusk. It takes a long time. He lowers himself into the water by inches, and the water closes over the dome of his shell with a small soft sound, like a book shut in the next room. A ring spreads, and spreads, and is gone. On the surface there is a leaf. It is yours. You do not remember dropping it.")
                .pay(200, 0, 0, 1, 0, 1);

        d.chapter("litter", "A Litter Under the Barn").gap(8)
                .intro(
                        "* They are found by the dog from the next farm, who raises such an uproar that the whole sanctuary turns out. Under the floor of the old barn, in a nest of straw and feathers, there are five fox kits. They are no larger than a sock. They are grey with a silver stripe along the back, and they smell of milk and dust and apples. Their mother lies beside them with her muzzle on her paws and the look of a person who has been proved right.")
                .wait(2)
                .outro(
                        "@Fox| (a long way down the bond, very dry) I did say I was tired. I did not say I had nothing left to do.",
                        "* The kits cannot see. They crawl over each other in a heap, mewing, with their eyes screwed shut, and fall asleep in the middle of climbing. Ember comes and stands over the barn door with enormous care, like a hill that has remembered how to tiptoe. Quill sits on the rafter and lowers her head to look, and a kit, without opening its eyes, sneezes at her.",
                        "* You sit up with them all night. At dawn the vixen stirs and licks the nearest. The nearest, a small bold female with a white tip to her tail, lifts her head and says in a squeak, to nobody, in the plainest words, the first thing she has ever said. It is: 'Where is the noise?'")
                .choice(new Chapters.Choice("kits", "Five kits, and a mother who has not long. The sanctuary has room, but the wild has more.",
                        "Keep all five: the barn becomes a den, and they grow up where they were born.", "Let them go to the wild at the proper age, and see them off at the fence.", "Ask them: sit with the kits, and the tune, and let each decide.")
                        .after(
                                new String[]{"* You keep them. It is the most chaos the sanctuary has ever seen. They tear up the vegetable patch, and the washing, and a Wick's best boots. By the summer they are the fastest creatures within ten miles, and have learned to open the gate. Ember, put in charge of their education, teaches them to roll apples downhill. It is an unholy success.", "@Prewitt| (the Magistrate, reading a petition about boots) I will not have it said that the law is without mercy."},
                                new String[]{"* You let them go. When the leaves turn, you open the gate on a dim blue dawn, and they trot out through the dew in a line, grey and silver, with their tails up. They do not look back. One by one they slip into the long grass. The vixen stands at the fence with her head up, the old bold light in her eye, and says nothing, and watches until the last of the grey has gone into the wood.", "@Brodin| (at the fence, with his hat off) They've good manners."},
                                new String[]{"* You sit with them at the edge of the field, in the evening, and hum the six notes. The kits tilt their heads. They hum back, in high thin squeaks, the six, and then, together, the seventh. Four of them go off into the grass, in the dusk, one after another, each as if called. The fifth, the bold one with the white tip, stays. She sits in your lap and looks up with her small clear eyes. She says: 'You can name me.'", "@Fox| (dryly) That one has taken after me. I'm so sorry."}))
                .done((c, st) -> { if (Chapters.flag(st, "kits") != 2) Chapters.companion(c, st, "kit", "Sprig", "Grey Fox", 6, true); })
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("prewitt", "The Magistrate's Cat").gap(8)
                .intro(
                        "* Magistrate Prewitt comes to the sanctuary on foot with a basket. She is seventy, with a firm grey bun and an expression like a closed ledger, and she has not been seen in anything but a black gown since the hearing. The basket is covered with a cloth. She sets it on the gatepost. A thin, yellow, one-eyed cat looks out of it with an air of intense disapproval.")
                .friends(4, "Keep the sanctuary a community: make four friends.")
                .outro(
                        "@Prewitt| This is Mister Gavel. He is nineteen. He has been in my chambers since the case of the Rooks. He has presided over four hundred trials, and I have it on good authority that his verdicts were always sounder than mine. I am retiring. I have been told he is too old to be moved. I have decided to ask if he might be allowed to be old with others.",
                        "* The cat steps out of the basket with great deliberation. He walks the length of the gatepost, down the fence, along the path to the barn. He stops in front of the fox. They regard each other with perfect composure, two creatures who have run a very long way and know it. He sits. She sits. After a minute he lies down beside her in the sun, and begins, harshly, to purr.",
                        "@Prewitt| (stiffly, blowing her nose) I find I have nothing further to say.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("storm", "Quill's Last Storm").gap(8)
                .intro(
                        "* The great autumn storm comes in over the sea on the first of October, a wall of purple cloud with a belly like a bruise. Quill has been waiting for it all week. She stands on the ridge of the barn roof with her wings half open and her neck stretched to the wind. When the first rain falls she looks down, once, at you, and you know exactly what she is going to do.")
                .at("oak", "Follow Quill to the oak on the high ground, {@oak}.")
                .outro(
                        "* She goes up on the first gust. You see her against the cloud, a pale cross, and then she is lost in the grey. The storm closes. The lightning begins, great forked white crowns over the sea, and in the flashes you see her, tiny and bright, wheeling and rising through the black, with the thunder rolling about her like drums. She is singing. You can hear it between the peals: the old storm-crane's cry, hoarse and high and glad.",
                        "* You wait under the oak for three hours with your coat over your head. The rain comes down in bars. The wind pulls the branches. At last, near dawn, there is a feather on the grass at your feet. It is a long grey pinion with a white tip, singed at the edge, and it is warm. You pick it up. The storm is blowing itself out in the east, and in the thin new light a speck is coming down the sky in long loops.",
                        "@Quill| (in your head, as she lands, wet to the bone, with her crest in rags) I have never been so cold. It was the best night of my life. Do you think they would let me do it again?")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("tune4", "The Seventh Note").gap(8)
                .intro(
                        "* The kits learn the tune faster than you did. By midsummer they hum it in their sleep. They sing it on the barn roof at dawn, in a high rough chorus, and the whole of the sanctuary, even the sheep, turn their heads to the east. They sing the six notes, and the seventh, which is not yours. Nobody has taught them the seventh. They found it themselves.")
                .wait(2)
                .outro(
                        "* You find Ember at the foot of the orchard on the longest evening of the year, listening, with his old head on one side. He says nothing. His small bright eyes are wet. When the kits come to the end of the tune and leave the seventh note hanging, he opens his throat and, in a rumble that shakes the apples loose from the branch, sings it. It is not in tune. It is the most generous noise you have ever heard from a pig.",
                        "@Ember| (afterwards, scratching an ear with a hoof, embarrassed) I thought somebody had better.",
                        "* On the hill, beyond the fence, there is a grey shape in the long grass. It is a wolf, an old one, with a torn ear. He has come to the edge of the sanctuary every month for years, and sat, and left a feather on the stone by the lake. He lifts his head and, quite softly, joins in. The seventh note goes up from three throats in the dusk, a boar, a wolf and a fox, and the lake, a mile off, answers it from the deep.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("apples", "Ember's Last Apples").gap(8)
                .intro(
                        "* The orchard has one tree. It is the tree Ember planted, from a stone, years ago, in the cleared ground where the fire had been. It is crooked, and hardly taller than a man, and it gives about forty apples. Every autumn he eats them in a single sitting. This year he does not. This year he stands under the tree, with his chin on a low branch, and counts.")
                .wait(2)
                .outro(
                        "* He gathers them in his mouth. One by one, he carries them to the others, and sets them down. One for the vixen, on her blanket. One for the cat, on the gatepost. One for Quill on the roof, which he cannot reach and she must come down for. One for each of the kits. One for the dog. One for you. One, the biggest, red and gold, for the lake, which he puts on the stone by the reeds and pushes into the water with his snout.",
                        "@Ember| I thought I should give them away. I am told it is what one does. It is not as hard as I thought. They taste better, I find, when somebody else has them.",
                        "* He keeps the last for himself. It is small and green and bitter. He eats it standing under the tree, slowly, with his eyes shut. When it is gone he lies down in the long grass with a groan of enormous contentment, and says that he thinks he will have a nap.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("winter", "Her Last Winter").gap(8)
                .intro(
                        "* The first frost comes early. On the morning after it, the fox does not come in for breakfast. You find her at the edge of the meadow, in the white grass, sitting very upright with her tail curled round her feet, looking at the lake. She has been there since the middle of the night. She turns her head as you come. Her eyes are clear and level and amused.")
                .wait(2)
                .choice(new Chapters.Choice("slept", "{fox.first} has asked, without a word, where she is to spend the winter.",
                        "Let her lie down by the lake, beside Moss, and sleep as long as she likes.", "Take her home, to the stove, and sit with her through the cold.", "Carry her up the hill to the oak where your mother's tune began.")
                        .after(
                                new String[]{"* You carry her down to the shore. She is no weight at all. You lay her on the flat stone by the reeds in the last of the sun, with a blanket round her, and a feather beside her, and an apple. The lake is still. She puts her chin on her paws. She looks at the water and says, very gently, that it is a good place to be warm. She closes her eyes.", "@Moss| (a long way down, in a voice like a bell under ice) Welcome. I have kept you a corner."},
                                new String[]{"* You carry her home. She sleeps by the stove in a basket, with the old cat on one side and a kit on the other. You sit with her in the evenings and read aloud, from your mother's book, in the lamplight. She listens with one ear. On the fortieth night she opens her eyes and looks at you for ages, and then, with enormous care, licks your hand.", "@Fox| (barely audible) You always did read too fast."},
                                new String[]{"* You carry her to the oak on the hill. It is a hard climb, in the frost, with a wolf walking beside you and a crane overhead. At the top you set her down in the roots, under the huge bare branches, where your mother once sat to hear the wild. She sniffs the bark. She turns round three times. She lies down, curled in the hollow between two roots, with her nose under her tail, and looks out over the valley as one looks from a window.", "@Quill| (from the branch, softly) It's the best seat in the country."}))
                .outro(
                        "* The snow comes at dusk. It falls without wind, large and soft, on the lake and the orchard and the barn, and the sanctuary goes white and silent and blue. In the barn the kits sleep in a heap with their noses together. On the gatepost the old cat is a loaf of yellow fur with one eye shut. And in the dark, somewhere, very quietly, the six notes are being hummed. You cannot tell by whom.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("grow", "The Kits Grow Up").gap(12)
                .intro(
                        "* They grow all spring. They grow long in the leg and sharp in the muzzle, and by midsummer they are the colour of ash with a stripe like a line of silver paint. They have learned every trick in the sanctuary, and invented several. They have learned to open the pantry. They have learned to steal the washing. They have learned, from Ember, to roll an apple uphill, which he says is a feat of the highest art.")
                .wait(3)
                .outro(
                        "?kits=2|* The empty barn has been left clean, and the straw changed, and a bowl of water set at the door, in case a grey vixen, one day, should come back to have her own. You have not been told of any who have. But in the dawn, sometimes, in the long wet grass of the meadow, there are small prints, going in, and not coming out.",
                        "?!kits=2|* They are going, by degrees. One goes to the wood on a night of rain. One to the hills. One to a farm across the valley, where a child has put out a saucer. Each time, you stand at the fence in the dawn, and each time the kit turns at the edge, and looks back, and barks once, small and sharp and quite without sorrow. It means: I know the way.",
                        "* You find, by autumn, that the sanctuary is quiet. It is a good quiet. It is the quiet of a house after a long visit, with the beds stripped and the cups in the rack. You walk the paths in the evening with your hands in your pockets and your companions at your heels. You hear for the first time what your mother meant, years ago, when she said that a tamer is a person who has learned how to be left.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("return", "Quill Comes Home").gap(8)
                .intro(
                        "* She has been away for three weeks. She flew out in the grey of a Tuesday, on a south wind, and did not say where, or why, and you did not ask. The sky has been empty. The roof of the barn has been empty. In the evenings you have looked up at the first stars with a feeling you did not care to examine.")
                .wait(3)
                .outro(
                        "* She comes down out of the north at sunset on a long, slow, tired glide. She is thin as a rake. Her feathers are ragged, and her crest is gone to a pale tuft, and there is a long white scar down her neck. She lands on the barn roof with a sound like a dropped umbrella, and folds her wings, and does not say anything for a good while.",
                        "@Quill| I have been to the edge of the sky. I thought you should know that it is a very long way, and that it is perfectly all right. There is a place at the top where the blue stops and a sort of grey begins. It has a seam. It hums. I sat on it for a day and listened. I am told by the wind that it is the oldest thing there is.",
                        "* She stretches out one wing. Caught in the pinions there is a single spangle of pale gold, no bigger than a coin. It is a seed, or a scale, or a splinter of the sky itself. She says she brought it for the lake. She says: 'It's a bit late. I'm sorry. It was a long way.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("voice4", "What the Wild Calls You").gap(8)
                .intro(
                        "* The wild has no word for what you are. It never did. Your mother was its Voice, and her mother before her, and the name went down the line like a coat. You are not that. You are, you were told in the autumn that you walked the roads with the four, something else. The wild has been thinking about it for years. At last, on an evening in October, it speaks.")
                .wait(2)
                .outro(
                        "* It does not come in words. It comes up from the ground, through your boots and your spine. It is a low long hum, and every living thing in the valley takes it up at once. The owl in the beech joins it, and the badger in the sett, and the mice in the barley, and the deer in the long grass. It is the six notes, and the seventh, and an eighth, which is new. The eighth is yours.",
                        "* It is a plain simple sound, a falling third, like a mother calling a child in at dusk. You know what it means. It means: here is one of ours. It means: she may come in. It is the sound the wild makes for a friend.",
                        "@Brodin| (at the fence, with the whole of the valley humming round him, in a shaken voice) I think you've been given a name.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("sleep", "A Hundred Years of Sleep").gap(8)
                .intro(
                        "* You go to the lake in the first week of spring. The ice has gone. The reeds are green at the roots, and a heron stands in the shallows on one leg, like a question. The water is flat and clear. At the bottom, a long way down, there is a round dark shape the size of a house, quite motionless, with a faint green light in it. Moss is sleeping.")
                .wait(2)
                .choice(new Chapters.Choice("farewell", "Moss will not wake for a hundred years. You will not be there.",
                        "Hum him the low note, and leave the lake to its keeping.", "Leave him the apple stone, the feather and the whistle, and promise to come.", "Say nothing. Sit with him until the light goes.")
                        .after(
                                new String[]{"* You hum the low note. It goes out across the water in a long warm line. For a moment the green light brightens, and a thin stream of bubbles rises, one at a time, from the deep, and breaks at the surface. It is, you decide, the closest a tortoise can come to a wave.", "@Quill| (from the reeds, dry) He heard."},
                                new String[]{"* You lay them on the stone by the reeds: the apple stone, the grey feather, the bone whistle. You say that you will come. You say it aloud, and clearly, to the water, as one says a thing to a friend who has dropped off in a chair. The surface ripples. A single large leaf turns over in the wind and lies, belly up, pale gold. It is the first leaf of the spring. It looks like a hand.", "@Brodin| That's a promise. I'll write it down."},
                                new String[]{"* You sit on the flat stone with your knees drawn up. You say nothing. The sun goes down behind the hill and the lake turns from blue to rose to grey. A heron flies over. An owl starts. You sit for ages in the dusk with the cold seeping up through your coat, and you are not sad. You are listening. After a long while you hear it, quite faint, from far down: a bell, very deep, very sweet, very slow."}))
                .outro(
                        "* The sanctuary is quiet on the walk home. The old cat sits on the gatepost with one eye open. The kit with the white-tipped tail comes down the lane to meet you, with a feather in her mouth. She puts it in your hand, and sits, and looks up. She says: 'Whose is this?' And you say that you think it is yours.")
                .pay(2000, 3, 0, 4, 2, 2);
    }
}
