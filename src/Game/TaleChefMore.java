package Game;

/** The Cook Who Fed the Mountain, season 4 (2026-10-20): "The Nine Courses" (a warm voice with a quiet awe). The man in the apron asks the cook to make the Convocation feast: nine courses, one from each of the nine places of the
 *  Palace's story. The cook goes to each with a pot and a question and comes home with a dish. The feast is laid for the Nine Lamps and one empty chair; Captain Verge asks for a place. Choices: beast (the Menagerie's course),
 *  seat (what is done about the Court's request). Flags: beast, seat. The Door season (StoryDoors) comes after. */
final class TaleChefMore {
    private TaleChefMore() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "beast", "seat");
        d.place("wayhouse", "wild").place("menagerie", "far");

        d.interlude(3, "A Long Table",
                "* The Long Table seats forty and feeds two hundred. The bottom of the pot is older than anyone who eats from it. Rue writes from the coast to say that she is ninety-four, that the sea is overrated, and that she has been thinking about a menu.",
                "* A plain brown cart stops at the crossing at noon. The Wick on the box says that he has a message from the Under-Kitchen. He says it is a short one. It says: Would you be so kind as to cook a dinner for the Nine?");
        d.season(4, "The Nine Courses");

        d.chapter("ask", "A Dinner for the Nine").years(3)
                .major(5)
                .setup((c, st) -> Chapters.spawn(c, st, "rue"))
                .intro(
                        "* The man in the apron is waiting in the Under-Kitchen with his hands behind his back. He has laid a long table in the middle of the floor, with a cloth, and nine plates, and a tenth set apart at the head. He has put a sprig of holly in a jar. He has the look of a man who has been rehearsing a speech in front of a ladle.")
                .wait(4)
                .outro(
                        "@???| It is for the Convocation. I am allowed to say that. Once in a generation the Nine Lamps sit down together and eat a meal that was not cooked in the Palace. It is a custom older than the Order. I did not know. I read it in a book. It says: nine courses, and one for each of the places. It says that each must be made from what the place gives.",
                        "@You| And the tenth plate?",
                        "@???| (very softly, looking at the head of the table) That is for the Seat. It has never been used. It has been set at every one of these dinners, for a thousand years, and has always been taken away clean. I do not know why. I only know that I would like to give it something.",
                        "* He takes a sheet of paper from his apron. It is a list in his round careful hand. It has nine names on it, in a column. Beside each is a blank. He says that he has not the least idea what a place might give. He says that he supposes that is what a cook is for.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("bread", "Bread from the Wayhouse").gap(8)
                .intro(
                        "* The first place is the Unlit Wayhouse, on a road not far from Originis. It is a long low inn of grey stone with a lamp in every window and not one of them lit. There is a ledger in the hall, and nobody in it. The beds are made. The hearth is laid. A bag of flour stands on the shelf in the kitchen, and a crock of yeast, and a loaf pan with a faint rim of crumbs.")
                .wait(4)
                .outro(
                        "* You bake it in the Wayhouse oven, which has not been lit in a thousand years, with wood from the yard. The fire catches at the first match. The flour is fine and white and smells of nothing you can name. The loaf rises in an hour, golden, and tall as a hat. It tastes of a morning you do not remember. It tastes of the day you were made.",
                        "@???| (when you bring it, very quietly, to the Under-Kitchen) Everyone in the Garden began on one morning, aged eighteen. Nobody has ever been able to say what they had for breakfast. I think it was this.",
                        "* He takes a slice. He chews. He sits down at the long table with the bread in his hand. He says that it is the first thing he has ever eaten that made him homesick, and that he does not know for what.")
                .pay(200, 0, 0, 1, 0, 1);

        d.chapter("wall", "A Cake That Loops").gap(8)
                .intro(
                        "* The second place is the Weaver's Wall. It is the edge of the world, and it is woven, and a cook cannot well bake there. But she can cook from it. You stand at the foot of the Wall with a bowl and a spoon and a sack of eggs, and look up at a hundred miles of grey thread, and wonder what to do.")
                .wait(4)
                .outro(
                        "* The answer is in the weave. It comes loose in your fingers, a single long grey thread, as fine as spun sugar. You pull it. It does not break. It keeps coming, yard on yard, out of the cloth, without a seam, until you have a ball the size of a fist and the wall has not so much as shivered. You beat it into the eggs. You pour the batter into a ring.",
                        "* The cake bakes in an hour. It has no end. If you cut a slice, the cut closes behind the knife, and the slice is whole, and the cake is whole. Every bite is the first and the last. You carry it back on a plate with a cloth over it, and nobody on the road is able to say where it begins.",
                        "@Ox| (staring, with a fork) It's like eating a circle.",
                        "@Rue| (from the bench, tasting, with a nod) It's a pudding that knows its own business. I approve.")
                .pay(200, 0, 0, 1, 0, 1);

        d.chapter("broth", "Clear Broth").gap(8)
                .setup(TaleChefMore::mark)
                .intro(
                        "* The third place is the Cistern. You have been before. You were not there to cook then. You go down the long stair with a ladle and a copper pot and a heart that is not at all steady. The basins are black and still. The pale shapes turn slowly in the deep. You stand at the edge of the first basin and wonder, in the plainest words, how one makes a broth from the dead.")
                .made(6, "Make the broth: cook six dishes.")
                .outro(
                        "* You do not take from them. That is the answer. You take from the water, which has been theirs. You dip the ladle in the clearest basin and draw up a cupful, and it is cold, and clean, and tastes of nothing at all. You simmer it with a bay leaf and a clove of garlic, and a sprig of thyme from your own apron, and you say, softly, over the pot, the names of everyone you have fed.",
                        "* The broth comes out the colour of a pale morning. It is thin. It is very hot. It is the lightest soup you have ever made, and it has in it, you could swear, an echo of every bowl that has ever been put before a hungry person. You pour it into a flask and carry it up.",
                        "@???| (tasting, after a long pause, with his eyes shut) This is what they would have wanted. I do not know how I know. I think it is that there is nothing in it but kindness and water.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("weigh", "The Weighed Portion").gap(8)
                .intro(
                        "* The fourth place is the Assayer's Hall. It is a stone room with a balance as large as a church. Every portion must be exact. That is the rule, and the rule is the dish. You are given a bag of barley and a pair of silver scoops and a card that says: One measure for each. You are not told of what.")
                .wait(4)
                .outro(
                        "* You weigh the barley grain by grain. It takes a day. When you have done, you have nine heaps, each exactly equal, on nine plates, and you know what is wanted. It is a dish of absolute fairness. It is a bowl of plain boiled barley, with one pat of butter and a grain of salt, and not a thing more, for each of nine. It is the dullest food in the world. It is also the only dish you have ever cooked that nobody could say was unfair.",
                        "@Rue| (at the end of the table, tasting, with a cough) It's the sort of food I used to make for the pig. I mean that as the highest praise. It's honest.",
                        "* The man in the apron eats his without a word, to the last grain. He pushes the plate away. He says that it is the first time in his life he has felt entirely equal to somebody else's portion.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("spice", "Spice from the Shed").gap(8)
                .intro(
                        "* The fifth place is the Gardener's Shed, in the deep wood. It has no kitchen. It has racks of tools, and a ledger, and labels on strings. You come in with a basket on your arm and stand in the dust, and look along the pegs for something to season with.")
                .wait(4)
                .outro(
                        "* There is a tool for it. There is a tool for everything. It hangs near the door: a small grey grater, no longer than a hand, with a label that says ON LOAN TO: THE COOK. It does not say when. You take it down. It is warm. You grate a lump of nothing over a dish of mushrooms. The dish takes on, as you watch, a flavour that has never been tasted: salt and sky and a faint far smoke, and the smell of rain on hot stone.",
                        "@You| What is it?",
                        "@Rue| (sniffing, delighted) It's the thing that makes the other thing taste of itself. I've been trying to say it for seventy years.",
                        "* You put the grater back on the peg. You do not take it. It is not yours. But the dish is made, and it is the best you have ever cooked, and you can taste it for a week afterwards at the back of your tongue.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("salt", "Salt from the Thin Sky").gap(8)
                .intro(
                        "* The sixth place is the highest. You climb to it for four days with a bag of oatcakes and a flask. The air thins and chills. The path is a goat track, and then a ledge, and then a rope. At the top there is a pale bare peak under a dome of blue that has a crack in it, a long dark line from one horizon to the other, and through it a thin bright wind.")
                .wait(4)
                .outro(
                        "* The wind leaves salt. You find it in the morning on the rocks, in a white crust as fine as frost, on every ledge facing the crack. You scrape it into a jar with a feather. It tastes of the sea at midnight and of iron and of something else, thin and sweet and far away, like the memory of a voice. It is the saltiest salt you have ever known and it has no bitterness in it at all.",
                        "* You take it down the mountain wrapped in a cloth. You sprinkle it over a dish of plain roasted roots. They taste of the earth they grew in, only more so. They taste of a field at dawn in the first week of spring. They taste, absurdly, of being understood.",
                        "@Ox| (with his mouth full, in awe) I think I could eat a whole mountain with this.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("tea", "Tea for the Dead Road").gap(8)
                .intro(
                        "* The seventh place is the Road of Unreturned Lanterns. It runs through a barren waste of grey stone, dry as a bone, and along it, at dusk, the dead walk. You have been here before. You do not need a guide; you set up a trestle at the side of the road, with a kettle and a rack of cups, and wait.")
                .wait(4)
                .outro(
                        "* They come in ones and twos, in the failing light, each with a small lantern, going the same way. They do not speak. They do not look. But as they pass your trestle, one by one, they slow, and stop, and take a cup, and drink. It is a plain tea with a spoon of honey. They hold it a moment in their pale hands, and drink it to the bottom, and hand it back, and go on.",
                        "* You pour four hundred cups. At the last a child, a girl of seven in a torn dress, takes hers in both hands and looks at you over the rim. She says that it is the first thing she has tasted since she died. She says it tastes of her grandmother's kitchen. She says: 'Is that you?' And you say no, but that it is perhaps where she is going.",
                        "@???| (when you tell him, at the table, with his eyes full) I have been trying to give them something for such a long time.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("wild", "The Menagerie's Roast").gap(8)
                .intro(
                        "* The eighth place is the Menagerie of Practice. It is a round stone hall of numbered cells, empty, with a tally over each door. In the middle of the floor there is a small old door with no number. In the dust in front of it lie a hoofprint, a clawprint and a human footprint, one on top of another. The hall is silent, and cold, and smells of straw.")
                .wait(2)
                .choice(new Chapters.Choice("beast", "The Menagerie gives game: a creature made for practice, and a creature that woke up.",
                        "Cook what the Menagerie gives: a roast of the practice-beast, as the book says.", "Refuse the meat: make a nut roast of the hall's own barley, and say why.", "Ask the creature that woke up what it would like to be served.")
                        .after(
                                new String[]{"* You roast it. It is a small stag, tier two, with a number on its flank. It was made to be hunted. It gives its meat without protest, as a loaf gives its crust. You cook it with juniper and wine, slowly, over a fire of oak, and the smell goes up to the dome and out through the roof. It is very good. It is also, you feel, rather a heavy dish to put before a table of Lamps.", "@Ox| (low, at your elbow) It's what the book said. Doesn't mean you have to like it."},
                                new String[]{"* You refuse the meat. You make a nut roast, from hazel and barley and a handful of the hall's own dry herbs, and you stand it on the plate in the shape of a stag, as a joke the cells will understand. You write a note: Practice is for the one practising. Nobody asked the stag. The man in the apron reads it twice. He says: 'I did not know that one could.'", "@Rue| (grinning) Take it from a cook: it's always allowed."},
                                new String[]{"* You ask. You go to the small old door with no number and knock. It opens a crack. A pair of enormous soft eyes look out of the dark. It is the creature that woke. It is a stag, you think, or something like one. It listens to your question. It says, in a very small voice, that it would like to be a mushroom. You make a mushroom dish, in a stag-shaped dish, with a sprig of thyme for antlers. It is the only course of the nine that anybody weeps over.", "@???| It likes thyme. I did not know."}))
                .outro(
                        "* The dish is carried up the road in a cloth. It smells of smoke and something older. The hall behind you is dark. As you go out you pass a cell with its door ajar, and in it, on a heap of straw, a small thing is sleeping, curled round a wooden spoon.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("garrison4", "The Muster's Soup").gap(8)
                .intro(
                        "* The ninth place is the Garrison of the Crossed. It stands on a bare plain at the edge of the world, with its gates open and its beds made. In the muster hall there is a great black pot, hung from a hook, with a ladle on a chain. The ladle is worn, as if by a million hands. There is a fire laid under the pot. There is no wood.")
                .wait(4)
                .outro(
                        "* The soup is what it has always been. It is the soup they eat, on the last night, before they go in: barley, bone, an onion, a bay leaf. You do not improve it; you light the fire with a match from your apron, and stir, and add a pinch of the salt from the Thin Sky. It comes to the boil. It fills the muster hall with a smell like a mother's house.",
                        "* A woman with a grey plait is watching from the door. She has her ledger under her arm. She says nothing for a time. Then she says that she has served that soup for nine hundred years and not once tasted it. She takes the ladle. She tastes. She shuts her eyes. She says: 'Oh. It has onions in it.'",
                        "@Plait| I thought it was only for the going.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("feast", "The Feast").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "verge"))
                .intro(
                        "* The Convocation feast is laid in the Great Hall of the Palace at sunset on the first night of winter. The long table is set with a white cloth, and nine chairs, and one, at the head, empty. The Nine Lamps sit in their places in their robes, grave and pale and ancient, like portraits that have come down off a wall. The Wicks bring in the courses on silver. The man in the apron stands at the kitchen door with his hands wrung together.")
                .wait(2)
                .outro(
                        "* They eat in silence. At the first course, the bread, the Lamp of Dawn closes her eyes. At the second, the cake that loops, the Lamp of Patience laughs aloud. At the third there is a sound, soft and general, as of nine people drawing a breath. They eat the barley. They eat the mushrooms. They drink the dead road's tea. At the roast, the Lamp of Iron puts down her fork and says nothing for a minute. At the soup she weeps without sound.",
                        "* When the last bowl is cleared the hall is silent. The tenth plate at the head of the table is still empty. Nobody has touched it. Nobody has looked at it. The man in the apron comes forward from the kitchen, slowly, and sets down in front of the empty chair a single bowl of barley soup, with an onion in it. He steps back. And in the great door at the end of the hall a thin grey figure in a plain coat is standing, with a pack, and a short sword, and his hat in his hand.")
                .choice(new Chapters.Choice("seat", "Captain Verge of the Hollow Court stands in the doorway of the feast, and asks for a place at it.",
                        "Set him a chair: nobody means nobody, and the Court is somebody.", "Refuse: the Court has come to put the Garden out, and this is not its table.", "Ask the Nine: put it to the Lamps, and let the Seat decide.")
                        .after(
                                new String[]{"* You set a chair. It is the plainest chair in the hall, a kitchen one, with a rush seat, and you place it at the foot of the table. Verge crosses the floor. He sits. He takes up the spoon. The Lamps do not move. For a moment the whole hall is a frieze. Then the Lamp of Salt, with a trembling hand, passes him the bread.", "@Verge| (hoarse) I came to say thank you. I did not expect to be fed."},
                                new String[]{"* You refuse. You say it in the doorway, quietly, with your hand on the jamb. You say that the Court has a quarrel with the Garden, and that a quarrel is not a meal. Verge bows. He says that he understands. He says that it was a fair answer. He steps back into the dusk, and the door closes, and the Lamp of Winter says, in a dry voice, that it was well done, and that she wishes she felt better about it.", "@Rue| (low) It's a hard thing to turn a hungry man from the door. I'd have done it too."},
                                new String[]{"* You turn to the Nine. You put it to them in two sentences. The Lamps look at one another along the table. Nobody speaks. Then, very slowly, the tenth plate at the head of the table moves. It slides an inch along the cloth, as if pushed by a patient hand, toward the foot. The Lamp of Hunger says, in an awed whisper: 'The Seat has voted.' Verge takes the chair.", "@???| (in the kitchen door, with his hands over his mouth) It has never done that before."}))
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("wash", "Washing Up").gap(8)
                .intro(
                        "* The Under-Kitchen after a feast is a ruin. There are pots on every surface, plates in every sink, a pyramid of dishes on the floor. The man in the apron sits on a stool in the middle of it with a cloth in his hand and a look of perfect contentment. Rue has been persuaded to come, and sits at the table in her shawl with a cup of tea, looking about with a grim satisfaction. Ox washes. Cassia dries.")
                .wait(2)
                .outro(
                        "@???| I have learned what a meal is for. I think. It is not the food. It is the table. It is a place where you may sit with anybody and not be asked what you are. The Lamps have not spoken to one another for three hundred years. They are in the great hall now, with their sleeves rolled, arguing about the washing up.",
                        "@Rue| (without turning her head, with a sniff) That's the best sound in the world.",
                        "* You hear it from the kitchen door: a rumble of old voices, rising and falling, a laugh, a clatter of plates. The tenth plate is on the table at the head, where it was left. It is empty. It is clean. It has, in the middle, in the glaze, a small bright mark, a thumb's width, as if someone had pressed a finger to it. The man in the apron picks it up. He dries it. He puts it, very carefully, on the shelf with the others, and says: 'Thank you.'")
                .pay(2000, 3, 0, 4, 2, 2);
    }
}
