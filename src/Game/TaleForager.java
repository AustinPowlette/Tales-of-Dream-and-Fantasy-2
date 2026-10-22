package Game;

/** The Forager's Almanac (2026-10-20; new start, three seasons). The human's mother keeps an almanac that lists every plant of the Greywood by the month it should bloom. This spring half of them are wrong, and the wood is a little
 *  further away each morning. Season 1, "The Almanac" (plain pastoral with a strangeness, card C): the wrong spring, the morels, the cartographer Rowena Thistle (the other gender than the human's) whose map will not hold,
 *  the apprentice Tuck, the path that moves, the logging company of Reeve Stoat, the bitter honey, midsummer, the day Mother walks into the wood. Choices: company (the Reeve's offer), love. Flags: company, love.
 *  Seasons 2 and 3 are in TaleForagerSeasons. */
final class TaleForager {
    private TaleForager() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void register() {
        Chapters.Def d = Chapters.def("forager");
        d.season(1, "The Almanac");
        d.flags("craft0", "company", "love");
        d.place("wood", "wildnear").place("oak", "wild").place("glade", "wild");
        d.cast("bryn", "Bryn", "Alder").she().keep().trait(Character.Traits.Gatherer).side("Good").lvl(6).pot(6).age(58).at("place:wood").rel(3);
        d.cast("tuck", "Tuck", "Marlow").he().keep().trait(Character.Traits.Friendly).side("Good").lvl(2).age(20).at("place:wood").rel(3);
        d.cast("rowena", "Rowena", "Thistle").opposite("Rowan").keep().trait(Character.Traits.Scribe).side("Neutral").lvl(4).age(28).at("far").rel(1);
        d.cast("stoat", "Reeve", "Stoat").he().keep().trait(Character.Traits.Merchant).side("Evil").lvl(8).pot(6).age(49).at("far").rel(-2);

        d.chapter("spring", "A Wrong Spring")
                .setup((c, st) -> Chapters.spawn(c, st, "bryn"))
                .intro(
                        "* Your mother's almanac is a fat brown book with a cord round it and a pressed primrose on the cover. It lists every plant in the Greywood by the month it flowers, with a note of where it grows and what it is good for. It has been kept in the family for six generations. Every spring your mother takes it down from the shelf and reads the first page aloud, like a priest.",
                        "* This spring she reads it and stops. She puts her finger on the line. She says it again, in a different voice. Then she shuts the book and puts on her boots and walks out into the garden, where the snowdrops are in full bloom in the second week of May, and the bluebells have not yet come at all.")
                .at("wood", "Go to the edge of the Greywood, {@wood}, with your mother.")
                .outro(
                        "* The Greywood is a very old forest on the edge of the village, oak and ash and a great deal of holly, with paths that your grandmother cut. You have walked it every spring of your life. You know the way to the stream, the big beech, the hollow with the wild garlic. You walk it now with your mother and a basket, in a cold grey light, and you cannot find the stream.",
                        "@Bryn| It was there on Friday.",
                        "@You| Perhaps we turned off.",
                        "@Bryn| I have walked this path for fifty years. We did not turn off.",
                        "* The path ends in a bank of bracken. The air has the faint dry sweetness of a place that has just been left. Your mother stands there with the almanac under her arm, and for the first time in your life you see her look at the wood the way you would look at a person who has been rude.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("morels", "Morel Season").gap(8)
                .intro(
                        "* The morels come in the third week of May, which is two weeks late and one hour from the village. They grow under the old ash on the south side of the hill in a ring, as they have always done, and you and your mother pick them at dawn with a knife and a cloth. They are honey-coloured and wrinkled like old hands. Each is worth a silver penny at the market in the town.")
                .wealth(300, "Sell the first harvest: hold 300 spirit stones.")
                .outro(
                        "* It takes the whole season to make three hundred. You forage from first light to dark, with the basket on your back and a stick in your hand, and learn the wood's habits by sitting very still. The morels like a south slope after rain. The chanterelles like a mossy bank under beech. The wild garlic is gone, but the nettles are up, and make a very good soup. You carry it all to market on Saturdays in a cart that is mostly mud.",
                        "* The market is loud and bright. There are cheeses and candles and live hens. A fat woman buys the morels without a word and puts them in her apron. A thin man buys the nettles and asks, very civilly, if you have any more of that lichen. You say you do not know what lichen. He holds up a grey-green thread. He says it is from the east side of the hill. He says he has never seen it in the market before. He says it has started to grow on the north side of everything.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("cartographer", "The Cartographer").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "rowena"))
                .intro(
                        "* The surveyor comes up the lane on a wet morning in June, with a tripod over one shoulder and a long brass telescope under the other. They stop at your gate. They stand there for a moment, with a roll of paper in their teeth and the rain running off their hat. They look at the Greywood across the road with the expression of a person who has been given a sum that will not add up.")
                .meet("rowena", "Speak to the surveyor at your gate.")
                .outro(
                        "@Rowena| Thistle. Surveyor, Second Class, the Cartographers' Society. I have been sent to map the Greywood for the new Survey. It was last mapped sixty years ago. It was a very clear map. It had a river. I have been looking for the river for a fortnight.",
                        "* {rowena.He} is about twenty-eight, small and wiry and rather muddy, with a long nose and a cheerful, hunted look. {rowena.He} unrolls the map on your kitchen table and holds down the corners with the sugar bowl, the teapot, the almanac and a cat. It is a beautiful thing, in blue and brown ink, with every tree marked. The river runs down the middle.",
                        "@Rowena| I have walked the whole wood four times. I have never found it. Here is my own map, made by hand, from the same points. The river is gone. And look at the edge. On the old map the wood ends at the Eastern Road. On mine it ends eleven chains short.",
                        "@Bryn| (from the stove) It's moved.",
                        "@Rowena| I beg your pardon?",
                        "@Bryn| The wood. It isn't where it was. It was a hundred yards farther off in March. I thought I'd gone mad. I thought it was my eyes.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("tuck", "Tuck").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "tuck"))
                .intro(
                        "* The boy at the back door is twenty, as thin and brown as a hazel stick, with a basket on his arm and a squirrel on his shoulder. He says he was told you needed a hand. He says he can tell a morel from a false one with his eyes shut. He says it with such grave confidence that you ask him to prove it. He does, to your astonishment, with his eyes shut.")
                .friends(2, "Take on an apprentice and make a second friend.")
                .outro(
                        "* His name is Tuck. His father kept bees at the edge of the common until the winter. He has been sleeping in a hedge since the spring. He eats like a horse and talks to everything, and the squirrel, whom he calls Admiral Nutkin, comes and goes as it pleases. By the end of the week he has learned the Greywood better than you did at his age, and has found, without being asked, three plants your mother had marked as extinct.",
                        "@Tuck| I just listen, you know. I don't know how else to say it. They talk. A bit. If you stand still.",
                        "@Bryn| (from her chair, with a long look) Yes. I thought you might.",
                        "* She says nothing more that day. But that night she takes down the almanac and, with a stub of pencil, writes a new line on the flyleaf, in her small upright hand. You read it over her shoulder. It says: The wood is calling. Tuck can hear. Remember to ask about the other one.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("moved", "The Path That Moved").gap(16)
                .intro(
                        "* The path to the beech grows longer by a few yards every day. It is not much, at first. A quarter of an hour in the morning becomes twenty minutes, and then half an hour. Tuck measures it with a knotted string. By July it is a mile and a half, and by August it is three. The beech itself has not moved. It is simply farther from the village than it was.")
                .wait(8)
                .outro(
                        "* You put a stick in the ground at the gate and sight along it to the beech. The next morning the stick is leaning. Rowena takes bearings at noon and dusk and midnight, with her brass telescope and a candle, and sleeps in a hammock in your barn. At the end of the week she comes in at dawn with a face the colour of paper and lays a drawing on the table.",
                        "@Rowena| It isn't the path. It's the village. We are moving. Or the wood is moving, and we are the same size. Either way. It's about a hand's breadth a day. It is perfectly regular. It has been going on since March.",
                        "@Bryn| (very calm) In which direction?",
                        "@Rowena| North.",
                        "* Your mother puts down her cup. She goes to the window and looks out at the grey-green line of the Greywood in the distance, quiet and tall and mild. She says, in a voice with no expression: 'It's leaving.'")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("notes", "Mother's Marginalia").gap(16)
                .setup(TaleForager::mark)
                .intro(
                        "* The almanac is full of marginal notes. You have always known that. You have never read them. They are in your mother's hand, and her mother's, and further back in hands that have faded to the colour of tea. You take it down from the shelf on a wet afternoon, and untie the cord, and sit at the kitchen table with a mug of nettle tea and begin at the beginning.")
                .made(12, "Copy out the notes: make twelve entries.")
                .outro(
                        "* They are not about plants. Not wholly. Beside the entry for foxglove, in a thin crabbed hand: She came at Candlemas and was gone by Whitsun. Beside the entry for the holly: The tree remembers. Be civil. Beside the entry for the oak at the heart of the wood, in your grandmother's round hand, underlined three times: DO NOT CUT. IT ASKED.",
                        "* The oldest note is on the flyleaf, in brown ink that has gone almost to nothing. It is written in a language you cannot read. Under it, in a later hand, someone has added a translation, in pencil, with a question mark. It says: The wood is a guest. It has stayed a long time. It will leave when it is called.",
                        "@Rowena| (reading over your shoulder, in a low voice) Who calls it?",
                        "@You| I don't know.",
                        "@Tuck| (from the floor, where he is sitting with the squirrel) I think someone already has.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("company", "The Reeve's Offer").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "stoat"))
                .intro(
                        "* The Marsh and Stoat Timber Company comes to the village in the autumn. It arrives in four wagons with a banner and a brass band. Its Reeve, a stout man of fifty in a fur-lined coat, climbs on the pump in the square and announces to the assembled village that the Greywood is the last great stand of oak in the county. That it is in danger of being allowed to rot. He says the Company will save it. He says the Company will, in fact, cut it down.")
                .meet("stoat", "Hear Reeve Stoat out in the square.")
                .choice(new Chapters.Choice("company", "The Reeve has offered the village a good price for the Greywood, and a bonus to whoever delivers the Almanac family's consent.",
                        "Refuse flatly, and warn the village.", "Delay: ask for a survey, and let Rowena's map stall the Company.", "Tell him the truth: the wood is moving, and will not be there to cut.")
                        .after(
                                new String[]{"* You refuse. You say it from the steps of the church, in a loud clear voice, and you tell the village what is in the wood. The Reeve smiles. He says it is a very touching speech. He says that the Company has a charter, and a licence, and a good deal of money, and that the Greywood will be down by the spring. He lifts his hat to your mother. She does not return it.", "@Stoat| I admire your spirit. It will be a pity."},
                                new String[]{"* You ask for a survey. It is a perfectly reasonable request, under the Crown's forest laws, and the Reeve cannot refuse. Rowena measures for a month. At the end of it she submits a report of eighty pages that proves, with figures, that the Greywood does not have fixed boundaries. The Company's lawyers read it twice. They conclude that the licence is therefore void, until a boundary can be shown. It is a small, local, utterly respectable victory.", "@Rowena| I'm told I've committed an act of cartography. I hope you appreciate it."},
                                new String[]{"* You tell him. You tell him plainly, in the square, in front of everyone: the path, the river, the stick. The Reeve listens with his head on one side and his hands in his pockets. When you have finished he laughs, a big warm rich laugh, and asks the crowd if they have ever heard such a thing. They laugh, too. It is not unkind. But Tuck, at the back, with the squirrel, is not laughing at all. He is looking at the Reeve, and his eyes are very wide.", "@Tuck| (afterwards, in a whisper) He believes you. That's why he's in a hurry."}))
                .outro(
                        "* The wagons stay in the square for a week. The band plays every evening. The Reeve takes a room at the inn, and dines out, and is charming to everyone, and gives sweets to the children. Your mother stands at the window of the cottage in the dusk, watching his lamp, with the almanac in her hands. She does not speak until the band has finished. Then she says: 'He's not the worst of them. That is the difficulty.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("oak", "The Oak That Hummed").gap(16)
                .intro(
                        "* The oak at the heart of the Greywood is the oldest tree you have ever seen. It is wider than a house, and taller than the church, and its branches go out over the glade like the arms of a patient old woman. Nobody has cut a twig from it in a thousand years. Your grandmother used to say that you could hear it breathing. You find, on a still October evening, that she was right.")
                .at("oak", "Go to the great oak in the heart of the wood, {@oak}.")
                .outro(
                        "* You go alone, with a lantern. It takes three hours. The wood is farther and darker than you have ever known it. Halfway there the path simply stops, and you walk the rest by the sound. It is a low, steady hum, so deep that you feel it in your teeth. It comes from the oak. As you come into the glade, the lantern flame leans toward the trunk, as toward an open door.",
                        "* You lay your hand on the bark. It is warm. There is a pulse in it, slow and strong. The hum rises. A leaf falls on your sleeve, and another, and you look up and see that the whole tree is shedding, at the start of October, in a quiet golden rain. The leaves are not dying. They are the colour of honey and perfectly alive, and they drift down in a glittering silence all round you, and settle on the ground in the shape of a path.",
                        "@Tuck| (appearing at the edge of the glade, with the squirrel on his shoulder and his face quite calm) It's showing us the way. It wants us to follow.",
                        "@You| Where?",
                        "@Tuck| North. Everywhere is north now.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("honey", "Bitter Honey").gap(16)
                .intro(
                        "* Tuck's bees swarm in the second week of June, which is the wrong time. They come out of the hive in a brown cloud, with a noise like a distant train, and settle in a great hanging lump on the lowest branch of the garden apple tree. Tuck stands under them with a sheet and a straw skep and a face of utter bliss. He says it is the best swarm he has ever seen. He says they are in a hurry.")
                .wait(8)
                .outro(
                        "* He hives them in a week. They produce a dark honey, very bitter, that tastes of oak-leaf and rain. The grocer in the town will not buy it. The apothecary will. She says it is the finest medicine for a cough that she has ever had, and the only one she has known to work on a cough that has already stopped.",
                        "* The bees are restless. They leave the hive at dawn and return at dusk, and every evening, as they come in over the garden wall, they fly in a long curving line that points north. Tuck stands in the garden with his hands in his pockets watching them go. He says: 'They know the way.' He says it as one says the name of a friend. He says: 'They've been told.'",
                        "@Rowena| (taking a bearing) Told by whom?",
                        "@Tuck| I don't know. It's like a smell. It's very far off. It's like someone with the window open.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("midsummer", "Midsummer").gap(24)
                .intro(
                        "* The village keeps midsummer on the longest day with a fire on the green and a dance round the bonfire and a feast. It is the oldest custom in the district. The children make wreaths of vervain. The men build a great stack of wood, and the women bring pies. The Greywood, on the edge of the common, stands tall and dark and silent in the long gold evening, and for the first time in months it looks just where it should.")
                .wait(8)
                .outro(
                        "* They light the fire at dusk. The smoke goes up straight in the still air. The dancing begins. Rowena, who has never danced in her life, is persuaded by a fat farmer's wife to try a reel, and does it with appalling energy. Tuck plays the pipe. Your mother sits by the fire with the almanac on her knee and a cup of cider, watching the flames with a look you cannot read.",
                        "* At midnight, at the height of the dance, the deer come out of the wood. They come in ones and twos, at first, soft-footed, and stand at the edge of the green. Then in dozens. They are red deer and fallow deer and a few small roe, with their heads up and their eyes shining in the firelight. The music dies. The dancers stop. Nobody screams. Nobody moves. The children stand with their wreaths in their hands, wide-eyed and silent.",
                        "* The largest stag walks into the light. He is old and grey in the muzzle, with a crown of antlers like a winter tree. He walks straight to your mother. He bows his head. She puts out her hand. She lays it on his brow, as one lays a hand on the head of an old friend, and says, in a clear low voice, 'Yes. I know. I'll come.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("walks", "Mother Walks In").gap(24)
                .intro(
                        "* She goes at dawn. She has packed a small bag with a loaf, a flask, a pair of stout boots and the almanac. She has put on her gardening coat. She stands at the gate in the grey light, tiny and upright, with her hair under a scarf, and looks at the three of you: you, Tuck and Rowena, in a row on the step. She says that she is only going as far as the glade. She says she'll be back by supper.")
                .realm(1, "See her off properly: reach Major Lvl 1.")
                .outro(
                        "@Bryn| It's all right. I'm not afraid. I want you to know that. It's been asking for me since the primroses. I'm to go with it, a bit of the way. Somebody has to read the almanac aloud, and I'm the only one left who can. You'll keep the garden. You'll keep the house. Tuck, you'll keep the bees.",
                        "* She kisses you on the forehead, as she did when you were small. She puts the almanac into your hands, wrapped in its cord. It is warm. She says that she will not be needing it where she is going. She says there is only one page left, and it is the first. She says: 'You'll know what to write.'",
                        "* She walks down the lane in the grey light, a small figure with a bag, and does not look back. At the edge of the Greywood the stag is waiting. He bows his head. She puts a hand on his shoulder. They go into the trees together, slowly, the old woman and the old beast, and the wood closes behind them like a hand.",
                        "@Rowena| (very low, wiping her eyes with her sleeve) I haven't the faintest idea how to draw that.")
                .pay(500, 0, 0, 1, 1, 1);

        d.chapter("leaffall", "Leaf-Fall").gap(24)
                .intro(
                        "* The house is quiet. The garden is overgrown. You keep it as you were told, and the bees keep to the hive, and the squirrel sits on the sill. In the evenings you read the almanac aloud at the kitchen table, from the first page, as your mother did. It is a small, strange, comforting ritual. Tuck sits on the floor with his back against the stove. Rowena draws, at the other end of the table, with her tongue between her teeth.")
                .wait(8)
                .choice(new Chapters.Choice("love", "Rowena Thistle has been staying in the barn for a year, and the survey is nearly done.",
                        "Ask {rowena.him} to stay when the survey ends.", "Draw {rowena.him} a map of the garden, with the almanac's best path marked, and give it with no words.", "Say nothing. Let the winter decide.")
                        .after(
                                new String[]{"* You ask. {rowena.He} stares at you over the rim of the cup. {rowena.He} puts it down. {rowena.He} says, in a rather shaky voice, that {rowena.he} has been waiting for someone to say it since the day the river went missing. {rowena.He} says that a surveyor's life is a series of departures and {rowena.he} has never in {rowena.his} life found a place where the map said Stay. {rowena.He} writes to the Society that evening and resigns.", "@Rowena| I'll finish the survey. It's only fair. And then I'd like to be shown the bees."},
                                new String[]{"* You draw it on the back of a seed packet. It takes three nights. The garden is there, the pear tree, the gate, the compost heap and the path to the wood. The almanac's best path is marked in green, winding up the hill. You leave it on the barn table. In the morning {rowena.he} is at the kitchen door with the paper in {rowena.his} hand, without the faintest idea what to say, and says it anyway: 'It's very accurate. You've left out your own house.'", "@You| I know where that is."},
                                new String[]{"* You say nothing. The winter comes, early and hard, and the three of you keep the fire in. You read the almanac. Tuck tells stories. {rowena.He} draws the frost on the windows, and the birds at the table, and the face of the squirrel. Nothing is said. Everything is said. When the snow melts, {rowena.he} is still there.", "@Tuck| I think you're both very slow. I think it's rather nice."}))
                .outro(
                        "* The first frost comes on the last day of October. The leaves of the garden apple fall in a single night, gold and brown and red, like a coat shrugged off. You stand at the gate in the morning with a mug of tea and watch them go down the lane in the wind. Far to the north, at the very edge of seeing, the line of the Greywood is a grey mist on the hills. It is farther than it was. It is going slowly. It is not afraid.")
                .done((c, st) -> { int l = Chapters.flag(st, "love"); Chapters.bond(c, st, "rowena", l == 3 ? 1 : 3); if (l == 1 || l == 2) Chapters.wed(c, st, "rowena"); })
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "The First Page",
                "* The almanac is yours now. You keep it on the shelf by the stove with the cord round it and the primrose on the cover. The first page, which your mother read aloud every spring, says, in a thin upright hand: The wood will stay as long as it is wanted. When it is wanted elsewhere, it will go.",
                "?company=1|* The Marsh and Stoat Timber Company's wagons left the village before Christmas. Nobody knows why. There is a rumour that the Reeve was seen in the square, in the rain, staring at the sky.",
                "?company=2|* Rowena's report is framed over the fireplace in the inn. It is eighty pages long and cited, they say, in three universities.",
                "?company=3|* The Reeve has taken to walking in the wood at dusk, alone. He says nothing about it. He says he likes the quiet.",
                "* The village is smaller than it was. The fields are farther. Each morning the line of the Greywood is a little farther north. Each evening, at the edge of the common, Tuck stands with the squirrel on his shoulder and a pipe in his hand, and plays a slow tune, and the wood, a long way off, rustles.");
        TaleForagerSeasons.add(d);
    }
}
