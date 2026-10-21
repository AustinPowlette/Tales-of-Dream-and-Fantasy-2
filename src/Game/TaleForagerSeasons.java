package Game;

/** The Forager's Almanac, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Wood Walks" (a slow road story, ecological and strange, card C): the Greywood is leaving, north, a hand's breadth a day, and the human, Tuck and Rowena follow it in a wagon. The Reeve's company cuts at its
 *  edges, there is a fire, a village in the way, a seed in every garden, and a flock of plants with no roots. The wood stops at the gates of Originis. Choices: axe (what is done with the Reeve), rootless.
 *  Flags: axe, rootless.
 *  Season 3, "The Last Almanac" (cosmic and gentle): the Greywood plants itself round the Palace. Madam Dusk, a Guest of the Hollow Court, offers an autumn that never ends. Winter is what the almanac is for.
 *  Flags: autumn, almanac, dusk. */
final class TaleForagerSeasons {
    private TaleForagerSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "axe", "rootless", "autumn", "almanac", "dusk");
        d.place("palace", "start").place("north", "far").place("village", "wild");
        d.cast("dusk", "Madam", "Dusk").she().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(15).pot(8).age(60).at("far").rel(0);

        // ================================================================== SEASON 2
        d.season(2, "The Wood Walks");

        d.chapter("lie", "The Map Is a Lie").years(2)
                .setup((c, st) -> { Chapters.spawn(c, st, "tuck"); Chapters.spawn(c, st, "rowena"); Chapters.spawn(c, st, "stoat"); })
                .intro(
                        "* Rowena's new map is three yards long and the best thing she has ever drawn. It lies unrolled on the kitchen table with the almanac, the teapot, a candle and a very patient cat on the corners. It shows the Greywood as it was in March, and as it was in June, and as it is now. The three outlines are drawn in three colours. They run away from each other northward like three men going up a hill.",
                        "@Rowena| It has gone forty miles. It is going at the speed of a snail. It is perfectly steady. At this rate it will be in the mountains by the autumn. I have worked the sums nine times.")
                .wait(12)
                .outro(
                        "* The almanac is open at the first page. It has a new line, under your mother's, in a hand you know. It is not ink. It is green, and faintly raised, like a vein. It has grown up out of the paper in the night. It says: Follow. Under it is a smaller line, in the same green: Bring the bees.",
                        "@Tuck| (with the squirrel on his shoulder, reading upside down) She wants us to come.",
                        "@You| She says to follow.",
                        "@Rowena| (rolling the map, with her jaw set) Then I shall need a bigger tripod.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("wagon", "A Wagon for the Road").gap(16)
                .intro(
                        "* The wagon is bought at the horse fair in the town. It is a high covered cart with four wheels and a canvas hood, painted green, with a stove and two bunks and a shelf for the almanac. It costs more than you have. Tuck carries the beehive in a padded crate. The squirrel gets the roof. Rowena brings the tripod. You bring a trowel, a basket, a spade, twenty jars and a small ancient ball of string.")
                .wealth(1500, "Fit out the wagon: hold 1,500 spirit stones.")
                .outro(
                        "* It takes the harvest of a season to afford, and a loan from the grocer, and the sale of the second-best morel knife. When the wagon is loaded it stands at the gate in the autumn light, creaking, absurd, with its green hood and its tilting pile of jars. The village comes out to see you go. The baker brings a loaf. The priest brings a blessing. A very small girl brings a jar of pickled walnuts, which she says her mother wants returned.",
                        "@Rowena| It looks like a caravan of the dispossessed.",
                        "@Tuck| It looks like a house that's got up and gone.",
                        "* You shut the gate behind you. The cottage stands in its overgrown garden with its doors open, and the pear tree dropping fruit on the path. You do not look back; you tell yourself that it will be there when you return. The almanac, on the shelf above the stove, has not said whether this is true.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("night", "The Wood Walks at Night").gap(16)
                .intro(
                        "* You catch up to it on the ninth night, on a long dark hillside under a sky of stars. The wagon stops. Tuck puts his finger to his lips. The squirrel has gone to ground in his collar. In front of you, across a mile of rough grass, a line of black shapes is moving against the sky.")
                .wait(8)
                .outro(
                        "* They are trees. They are walking. They do not walk as a man walks, in strides, but as a tide comes in, an inch at a time, with a long slow swaying. The oaks go first, big and stately, with their roots drawn up like skirts. The hollies come after. The birches skip. Behind them, in a procession a mile long, come the brambles and the bracken, the toadstools and the ferns, a vast dim river of green moving north under the stars, with a sound like the sea.",
                        "* You sit on the box of the wagon with a blanket round your shoulders and a cup of tea and watch it go by. Nobody speaks. A moth lands on your sleeve. A fox goes past at a trot with her cubs in tow, and an owl swings overhead, and a hedgehog trundles by in the grass, on the most important errand of its life.",
                        "@Tuck| (hoarse, with tears running down his nose) I can hear them. They're all talking. They're saying: nearly, nearly, nearly.",
                        "@Rowena| (very quietly, with her notebook open on her knee and no pencil) I've been a surveyor for nine years. I've never seen a thing I couldn't draw.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("hart", "The Hollow Hart").gap(16)
                .intro(
                        "* The stag comes back alone on the twelfth day. He walks out of the dawn mist toward the wagon, slowly, with his head lowered, and stops ten yards away. He is thinner than before. His coat is dull. His antlers, which were a crown, are bare of velvet and hung with a few strings of moss. He does not bow. He stands, and looks at you, and waits.")
                .wait(4)
                .outro(
                        "* Tuck goes to him. He stands with his hand on the stag's neck for ages. When he turns back his face is quite serious. He says that the Hart has come from the front. He says that it hurt him to come so far from the wood, and that it will take him a week to get back. He says he has a message. It is not in words.",
                        "@Tuck| She's all right. She's walking in the middle. She's carrying the book. She says: don't hurry, but don't stop. She says there's a bad man behind us with an axe. She says you'll know what to do.",
                        "@Rowena| (taking off her hat) I don't see how we're to know what to do.",
                        "@Tuck| (gently) She says you won't. She says you'll do it anyway.",
                        "* The stag bows his head. He lowers his muzzle to the earth by the wagon wheel and, with one slow breath, blows out a cloud of pale gold spores. They settle on the grass in a ring. By evening there is a circle of tiny white mushrooms round the wagon, ten feet across, glowing faintly in the dusk. They do not go out. They light the camp every night for the rest of the road.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("axe", "The Reeve's Axe").gap(16)
                .setup(TaleForagerSeasons::mark)
                .intro(
                        "* The Marsh and Stoat Timber Company has followed the wood for a hundred miles. They keep to the edge, in four wagons, with a brass band and a hundred axemen, felling the stragglers. They take the slow trees, the old and the lame, the oaks that cannot keep up. Every day there are more stumps along the road. Every evening the smoke of their fires goes up, a mile behind you, into the pale evening sky.")
                .made(15, "Mend the Wood's fences: make fifteen markers.")
                .outro(
                        "* On the nineteenth night you come on the Reeve in the open. He is sitting by his own fire at the edge of a clearing, alone, in his fur-lined coat. There is a bottle of brandy at his elbow. He looks old and tired. He looks up as you come, and does not seem surprised. He says that he has been waiting. He pours a second glass.",
                        "@Stoat| I should like to explain, if you will allow it. I am not a wicked man. I have a charter. I have seven hundred men who are paid by the week. If the wood goes, they have nothing. If the wood stays, they have nothing. I am only trying to keep my people fed.",
                        "@You| You're cutting a living thing.",
                        "@Stoat| I am cutting wood. I have always cut wood. My father cut wood. I do not know how else to do it.",
                        "* The firelight shows his face, red and square and unexpectedly lost. He puts down the brandy. He says: 'I was in the Greywood as a boy. There was an oak. I lay under it, in a summer. I have not thought of it for forty years.' He is silent for so long that the fire sinks. He says: 'What do you want me to do?'")
                .outChoice(new Chapters.Choice("axe", "The Reeve has asked, for the first time, what he should do.",
                        "Tell him to stop: take his axes home.", "Offer him other work: the Company can plant what it has felled.", "Say nothing, and leave him to the fire.")
                        .after(
                                new String[]{"* You tell him. You do it kindly, and without heat. He listens with his head down. When you have finished he nods. He does not argue. In the morning the band has gone, and the wagons with it, and the clearing is empty but for the ashes of a fire and a single bottle. On the bottle, on a slip of paper, in a heavy hand, he has written: Understood.", "@Rowena| That is the first time I have seen a man give up an argument in the night."},
                                new String[]{"* You offer him a better trade. He stares. He says it is an extraordinary idea. He sits with his chin in his hand for an hour. At dawn he stands, and kicks out the fire, and orders his men to put down their axes and pick up spades. They plant. It takes them the whole of the winter. Behind the wood, the length of the road, a faint green haze of seedlings rises out of the churned ground.", "@Tuck| (watching, wondering) They're following it. They're planting after it. It's like a very slow funeral."},
                                new String[]{"* You say nothing. You leave him by the fire, in the dark, with the brandy and the stumps. You are not sure why. He sits on there alone, and the next day he is still there, and the day after that. The band plays, thinly, in the distance. The axes lie in a pile. He does not give an order. By the end of the week the Company has dissolved, and gone home by threes and fours. The Reeve walks into the wood alone, one evening, bareheaded, and does not come out.", "@Tuck| (very low) He's all right. He's there. The trees have got him. They're being gentle."}))
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("fire", "Fire Season").gap(16)
                .setup(TaleForagerSeasons::mark)
                .intro(
                        "* The drought comes in July, as dry and white as a bone. The streams fail. The grass is straw. The Greywood, going north through a long yellow summer, cracks and drops its leaves in the middle of June. The air tastes of ash. At night a little wind comes down from the hills with a smell of burning, and the horses stamp and snort. On the twenty-fourth day Tuck stands up in the wagon and says one word, very low: 'Smoke.'")
                .made(15, "Cut a firebreak: make fifteen things.")
                .outro(
                        "* It starts behind you, in the dry grass, a quarter of a mile to the south. A line of red runs along the ground like a spilled drink, and a column of smoke goes up and leans, and then the whole slope is alight. The wood is in front of it. The wood is slow. The trees, for the first time since you have known them, are afraid. You can feel it in the ground, a dull hammering, like a heart at a run.",
                        "* You fight it all night. You fight it with spades and wet sacks and buckets passed hand to hand along a line of every person for ten miles around. Villagers come out of their houses. Farmers come in from the fields. The sergeant of a passing cavalry troop dismounts with his men and digs. Rowena runs the line, a smoking figure in a ruined coat, shouting bearings and directions in the voice of a general.",
                        "* The wind turns at dawn. A cold drizzle starts, from nowhere, and falls on the burning grass with a hiss. By noon the fire is out. The Greywood stands behind a charred black strip a hundred yards wide, scorched on its southern face, steaming and shaking and alive. A thousand people lean on their spades in the ash and stare at it, black-faced and exhausted. Nobody says a word.",
                        "@Tuck| (in a cracked whisper) It says thank you.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("village", "A Village in the Way").gap(16)
                .intro(
                        "* The wood has to cross the village of Hollowell. There is no way round. It lies in the neck of a valley, in the throat of the road, a hundred houses and a church and a mill. The wood cannot be turned. It cannot be stopped. It will pass through the village, over the village, and out of the other side, like a tide through a drain, and when it has gone the houses will be under green.")
                .at("village", "Go ahead of the wood to Hollowell, {@village}, and speak to the villagers.")
                .outro(
                        "* They receive you in the church, on a Sunday, all hundred households, on hard benches, in their best. The parson is a thin tall man with a voice like a bad hinge. He asks, with a certain relish, whether you propose to explain why a wood is coming to sit on his parish. The miller is angry. The blacksmith is frightened. A very old woman at the back says she has been waiting for this since she was a girl.",
                        "* You tell them what you know, in the order you learned it, without adornment. The path. The path moving. The oak. The deer on the green. Your mother. Tuck stands beside you with the squirrel and says nothing, and Rowena lays the map on the lectern and weights it with a hymn-book. There is a silence. The old woman says, clearly: 'I know a song.'",
                        "* She sings it in a cracked high voice, from the back bench, with her eyes shut. It is an old harvest song, with nine verses. At the second verse the blacksmith joins in. At the fourth the miller. At the sixth the whole church is singing, hundred upon hundred, a great slow rolling tune. At the end the parson takes off his spectacles and wipes them. He says: 'Well. I suppose we had better put the kettle on.'")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("survey", "Rowena's Survey").gap(16)
                .intro(
                        "* Rowena receives a letter in the middle of August. It is on thick cream paper with the Society's seal, and arrives by a rider on a lathered horse. She reads it standing, in the road, with the map-case under her arm. She reads it again. Then she folds it very small and puts it in her boot. She says that the Society has been looking at her reports. She says that it has decided the Greywood is of national interest. She says that it would like it surveyed. In full. At once.")
                .friends(6, "Keep your friends together: make six friends.")
                .outro(
                        "@Rowena| They have sent a team. They are to take bearings, and fix its boundaries, and file them. They are going to claim the wood as Crown property and register it as a feature of the county. They are going to draw a line around it. I will be the one who draws it, if I choose to. I have been invited to resign myself to the facts.",
                        "@You| What will you do?",
                        "* {rowena.He} is quiet for ages. {rowena.He} takes the letter out of {rowena.his} boot and tears it into very small pieces and lets them go on the wind. They fly away over the stubble like snow.",
                        "@Rowena| I have been mapping things all my life. I have never once asked what the things wanted. I should like to find out. I should like to see it from the inside.",
                        "* {rowena.He} puts down the tripod. {rowena.He} puts down the telescope. {rowena.He} tucks the map-case under one arm and walks into the wood on foot, with only a notebook and a pencil, to see what is there.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("seeds", "Seed-Bank").gap(16)
                .intro(
                        "* The seeds are in your pockets before you notice. They are in the folds of your coat, and the pleats of your skirt, and the rim of your hat. Tuck finds a dozen in his shoe. Rowena finds a handful in her notebook. They are small, brown and winged, and they smell of rain. Nobody has put them there. You find them at the end of every day, and you do not know how to say what you feel.")
                .wait(8)
                .outro(
                        "* You plant them as you go. You drop one in the garden of the inn at Lower Marl and one in the yard of the smithy. You press one into the crack in a church wall, and another into a flower-pot on a cottage sill, and a third in the verge of the high road. They sprout within the hour. By the next morning each is a green shoot three inches high, with a tiny crown of leaves. They are oaks, and ash, and holly, and hazel.",
                        "@Tuck| It's leaving seeds. It's leaving a bit of itself in every place it passes. It isn't leaving. It's spreading out. It's like a person scattering their ashes.",
                        "@You| Or like a person scattering their children.",
                        "* On the third day a woman in a village runs after you down the road with a pot in her hands. She says it is the finest seedling she has ever seen. She says it grew from nothing, in a night, on the sill. She asks what it is for. You tell her you do not know. She says that is all right. She says she will find out.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("rootless", "The Rootless Ones").gap(16)
                .intro(
                        "* You meet them in the autumn, on a long stony hill in the north. They are walking down the road, a dozen of them, in single file: small grey-green plants with no roots, trundling along on their stems like bundles of sticks. They have leaves and flowers and a faint, wandering smell of earth. There are daisies and docks and a whole tribe of dandelions. They stop when they see the wagon, and wait, with their heads up.")
                .wait(4)
                .choice(new Chapters.Choice("rootless", "A dozen plants with no roots have caught up with the wood and want to come with it.",
                        "Let them walk with the wagon, in the hold, and carry them to the end.", "Plant them: give each a patch of ground by the road and wish them well.", "Send them back to wherever they came from.")
                        .after(
                                new String[]{"* You let them in. They climb into the back of the wagon on their own stems and sit among the jars, quiet as sparrows. Tuck lays out a blanket for them and puts the beehive near. At night they sing, very faintly, a sort of high hum, like grass in a wind. The squirrel is deeply offended. By the end of the week it has become their chief.", "@Rowena| I am drawing them. I am drawing them in the margin of the map. It's the first time I have felt that a map might have company."},
                                new String[]{"* You plant them. You take each in turn, in your hands, and kneel in the ditch and scoop out a hole and settle it in. They sigh as the earth closes over their stems. In a week the roadside is a long garden of daisies and docks, and the dandelions have gone to seed. A passer-by on a donkey stops to stare, and then takes off his hat, as at a graveyard.", "@Tuck| I think they're glad. I think they were only tired."},
                                new String[]{"* You send them back. You do not like it. They stand in the road with their leaves drooping while you explain, as gently as you can, that there is no room. They say nothing. They turn and walk away down the hill, in single file, slowly, and the dandelions are the last. You watch until they are out of sight. That night you cannot sleep.", "@Tuck| (in the dark, in his bunk) I wish we hadn't."}))
                .outro(
                        "* There is frost on the wagon in the morning. The first snow has fallen on the high ground, a thin white powder, and the whole hillside glitters. The wood is going over the ridge ahead of you, a long dark file of trees against a sky the colour of a pearl. You can hear them, a low, dignified murmur. They are not afraid. They have been walking a long time. They have almost arrived.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("still", "Standing Still").gap(16)
                .intro(
                        "* You come to it at the end of November, from the top of a long grey hill. The road runs down into a plain, wide and pale and treeless, under a low sky. In the middle of the plain there is a city. It is enormous. It has towers and domes and a palace at its heart, grey and gold and lit from within, and round it, for miles and miles, is the smoke of a hundred thousand chimneys. It is Originis. The wood has stopped.")
                .wait(8)
                .outro(
                        "* It has gone as far as it can. It stands in a long line along the crest of the hill, facing the city. The oaks are in the front. They have put down their roots. The ground is frozen, but they have pressed them deep, one after another, with a long slow groan, like a congregation kneeling. The hollies have settled. The birches have straightened. They look at the city, in the cold, with an air of enormous patience.",
                        "* You find her on the third morning. She is sitting under the big oak at the front, with her back to the trunk and the almanac on her knee, wrapped in her gardening coat, with a stag at her feet. She looks up. She is very much older. There is moss in her hair. There is a small new leaf at her wrist. She smiles, quite slowly, with the same crooked smile you remember.",
                        "@Bryn| You took your time. I have been sitting here a week. Did you bring the bees?")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("home", "Home Is Where the Wood Is").gap(24)
                .intro(
                        "* The reunion takes the day. There is tea, from the wagon's stove. There is a stew. Tuck cries and tells her everything at once. Rowena shows her the map. The squirrel gets a nut. Your mother listens to it all, in her oak-leaf coat, with the almanac on her knee, nodding at intervals, as an old woman nods at the news of a distant family. At dusk she opens the book to the first page, and reads it aloud.")
                .realm(2, "Reach the end of the road: reach Major Lvl 2.")
                .outro(
                        "@Bryn| The wood will stay as long as it is wanted. When it is wanted elsewhere, it will go. It is wanted here. It has been called by the oldest voice there is. I am only the one who reads the page. I should like you to understand that I'm not gone. I'm only a little further in.",
                        "* The first snow falls on the hill that night. It comes down in great soft flakes, silently, in the light of the wagon's lamp. The oaks stand in a row with the white settling on their shoulders. Somewhere in the Palace, in the vast gold city below, a single bell begins to ring. It is slow, and clear, and welcoming, and the wood, on the hill, answers it with a long low sound like a sigh.",
                        "@Tuck| (very softly, with his hand on the bark) They're home.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "The Hill",
                "* There is a new hill outside Originis. It was not there in the summer. It is long and green and rather steep, covered with trees, and it has a path up the middle that nobody can say who cut. The city has stopped asking.",
                "?axe=1|* The Marsh and Stoat Timber Company has been wound up. Its axes hang in the church porch at Hollowell, with a plaque.",
                "?axe=2|* The Company's gardeners plant the road behind the wood every autumn. They call themselves the Followers. They are very good.",
                "?axe=3|* There is an old oak on the south side of the hill with a face in the bark. Nobody has asked whose.",
                "?rootless=1|* A dozen small plants live in a crate on the shelf of the wagon. On warm evenings they sing.",
                "?rootless=2|* The roadside from the mountains to Originis is a long unbroken garden of daisies and docks.",
                "?rootless=3|* You think of them sometimes. You think of them walking away down the hill in single file, in the grey.",
                "* Tuck lives in the wagon on the hill now, with the bees and the squirrel and the stag. He plays the pipe at dusk, and the trees listen. If you stand at the foot of the hill and lay a hand on the first oak you can feel it humming. It is a very low steady note, like a breath that has been held for a thousand years and is, at last, being let out.");

        // ================================================================== SEASON 3
        d.season(3, "The Last Almanac");



        d.chapter("planted", "The Wood Plants Itself").years(2)
                .intro(
                        "* The wood does not move again. It has been standing on the hill for two years, in a long dark line facing the city, and it has begun to spread. The oaks have dropped their acorns. The hollies have set berries. A fringe of saplings, thin and bright and hopeful, has come up along the foot of the slope. By the second spring you can walk a path of fifty yards through a young copse where the plain used to be bare. The Palace's gardeners come to look and go away troubled.",
                        "* Rowena has stayed. She has built a hut of green boughs at the edge of the trees, with a stove, a table, a brass telescope on a tripod, and a window that looks on the city. She works there all day. She has put up a sign: THE SURVEY OF THE GREYWOOD (UNOFFICIAL). Underneath, smaller: Visitors welcome. Do not feed the squirrel.")
                .wait(8)
                .outro(
                        "* Bryn is changing. It is not unkind. She grows quieter. She spends the days under the big oak with the almanac on her knee, reading it aloud in a low voice, to nobody, to the trees. Her hair has gone the colour of birch bark. Her fingers are long and slim and slightly green at the tips, and she has taken to resting her hand on a trunk as she walks, as an old person leans on a stick.",
                        "@Bryn| Don't look at me like that. I'm not ill. I am only becoming a little more of the wood. It happens to the ones who read the almanac for long enough. It happened to your grandmother. I can hear the whole hill now. They're all saying the same thing. They're saying: soon.",
                        "@You| Soon what?",
                        "@Bryn| (smiling, with her head against the bark) Soon, soon, soon. They don't say what. I don't think they know. I think it's a word they like.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("changed", "Mother, Changed").gap(16)
                .intro(
                        "* She takes you into the heart of the copse on the first day of spring. It is a still bright morning. The new leaves are the green of a freshly washed glass. She walks ahead of you, slowly, on your arm, with the almanac in the crook of her other. The path winds between the saplings. At the end of it there is a small clearing with a stone bench, and on the bench a thing you have never seen. It is a page.")
                .wait(4)
                .outro(
                        "* It is a single leaf of the almanac, loose, in a dry thin brown. It lies on the bench with a pebble to hold it. It is in your mother's hand, and in her mother's, and in hands before. It is the last page. It is written all over with notes, the months and the days, and each day has a mark beside it: a flower, a frost, a bud. It runs up to the bottom of the leaf, and stops. The last mark is a small circle. It is empty.",
                        "@Bryn| That's the winter. I never could fill it in. Every almanac has a winter, but it's the one thing the wood doesn't do. It doesn't die. It sleeps in the cold, and wakes again, and I've always thought of that as a sort of cheating.",
                        "@You| What do you want me to write?",
                        "@Bryn| I don't want you to write anything. I want you to learn why it's there.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("palace3", "The Palace Has a Window").gap(16)
                .intro(
                        "* The brown cart comes in the second week of April. The Wick on the box is a thin pale young man in a thin coat, with a flower pot on his knee. He holds it out as you come to the cart. In the pot is a single sad green stalk with two leaves, leaning toward the light. There is a note tied round it with string, in a round careful hand: I have been trying to grow this for a year. I think it is unwell. Can you come?")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and down to the Under-Kitchen.")
                .outro(
                        "* The Under-Kitchen has a window. You have never been in a kitchen under the ground with a window. It is a small high thing, at the far end of the long table, with a deep sill. On the sill are fourteen flower pots. They hold a basil, a mint, a geranium, a thing that was once a cabbage. A man in a plain apron stands over them with a watering can, in a state of tragic anxiety. He says his name is ???, and that he is sorry about it. He says he has watered them every day.",
                        "@You| Every day?",
                        "@???| Three times. I read it in a book. I thought that more was better. They are all dying.",
                        "* You put your finger in a pot. The soil is a sodden black mud. You tell him that a plant needs water and then a rest. You tell him it needs the dark as much as the light. You tell him that nothing grows without a winter in it. He listens with his mouth open. He sits down on a stool. He says: 'That is the single most extraordinary thing I have ever been told.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("seed", "What the Wood Is").gap(8)
                .intro(
                        "* He tells it to you at the long table, with his hands wrapped round a cup. He says that the wood is a seed. It is a seed the Gardener left behind when he went to the war, in the care of the Garden itself, in the dark, in the earth. It is the whole of the Garden's greenery in a single bud. Every tree and herb and moss that was ever planted here came, in the beginning, from that one place. It has been waiting a very long time to be needed.")
                .wait(4)
                .outro(
                        "@???| Every year a treasure falls. You know that. A seed of heaven, they call it. That is a leaf off the wood. Every great pruning, the great purge that comes when the world is crowded, is a thing the wood does to itself, to keep from being too full. The Garden is not a place that has a forest. It is a forest that has put on a village for a spell.",
                        "@You| And now it's come to you.",
                        "@???| It has come to the Palace. It has come to the Seat. I do not know why. I am not allowed to say what the Seat is for. I am allowed to say it has been empty a very long time, and that the first thing to grow in an empty room is a seed.",
                        "* He rises. He goes to the window. He puts out a finger, and touches the leaf of a geranium. A drop of water falls. He says, in a small clear voice, without turning: 'I should like to learn to be a gardener. I did not know I could want something so much.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("dusk", "Madam Dusk").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "dusk"))
                .intro(
                        "* She arrives at the edge of the wood on the evening of the vernal equinox. She is a slim grey woman in a long soft coat the colour of ash, with a basket on her arm and a pale gold light behind her, like a lamp seen through a curtain. She walks very quietly. The birds fall silent as she passes. A fox lifts its head and lowers it. She stops at the foot of the hill, and takes off her hat, and looks up at the trees as at a vast and beloved cathedral.")
                .meet("dusk", "Greet the visitor at the foot of the hill.")
                .outro(
                        "@Dusk| Madam Dusk. A Guest of the Court. I have come to pay my respects to the wood. I do not wish to interrupt. I only wanted to say how very beautiful it is. I have been walking for ages, and it is the first green thing I have seen that I did not want to put out.",
                        "* She sits on a fallen log and takes a flask from her basket and a pair of small cups. She pours. The tea is the colour of dried apples. It smells of woodsmoke and leaves. She holds out a cup. You do not take it, and she does not mind. She holds it a moment, and sets it on the log, and the steam rises and curls in the air with a kind of leisure.",
                        "@Dusk| I have an offer. It is a small one. I think it will please you. The Court is tired of seasons. We find them a cruelty. The spring that comes, and the autumn that goes, and the winter in the middle that takes the old and the weak. We would like to make one of them last. We would choose the autumn. It is the kindest. Gold and brown and still. It need never end.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("autumn", "Autumn Without End").gap(8)
                .setup(TaleForagerSeasons::mark)
                .intro(
                        "* It starts on the first of October, and by the middle of the month it has taken the whole plain. The leaves turn. They turn gold and copper and a deep warm red, and then they stay. They do not fall. They hang on the branches like lamps, motionless, in a still amber light, week after week, while the days go by. The birds stay. The bees do not go into the hive. The hedgehogs wander up and down the lane at noon, bewildered, and will not lie down.")
                .made(12, "Keep the almanac: make twelve entries.")
                .outro(
                        "* It is beautiful. That is the dreadful part. There is a golden hush over everything. Children play in the amber leaves that never rot. Old people sit on benches in the mild bright afternoons, and do not die. Nobody is cold. Nobody is hungry. A woman in the lane says it is the happiest autumn she has ever had, and that she has had it nine times. Her husband, beside her, says: 'Has it been nine?'",
                        "* The sap does not stop. The seeds do not fall. The ground does not rest. The wood, on the hill, stands rigid in its flame-coloured coat, holding its breath. You can hear it in the dark, a thin high tension in the air, like a string pulled too tight. The squirrel will not sleep. The stag stands day and night in the glade, with his head hanging, trembling.",
                        "@Bryn| (under the oak, very quietly, with her hand on the bark) It can't go on. It can't. A thing that doesn't rest breaks.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("winter", "The Winter Almanac").gap(8)
                .intro(
                        "* You take the last page of the almanac to the bench in the clearing and sit with it for three nights. Rowena brings a lantern. Tuck brings a flask. Your mother sits at your elbow with her hand on your sleeve, and does not help. You read down the column of days. A flower. A frost. A bud. A thaw. You read up it again. At the end of the third night you take the pencil from behind your ear and put it to the paper.")
                .wait(4)
                .outro(
                        "* You write: Winter. You write it at the bottom of the page, in the empty circle, in small clear letters. You write beneath it what you know. That it is the time when the roots rest. That the dead are laid by and the seeds are kept. That the cold is not a cruelty but a pause. That a thing which does not end cannot begin. You write it in the plainest words you can, as you would write a recipe.",
                        "@Bryn| (reading over your shoulder, very low) It's right. It's exactly right. I never could say it.",
                        "* Tuck has come to stand behind you. He has the squirrel on his shoulder and his hands in his pockets. He says that the wood has been trying to tell him for a week. He says it was only waiting for someone to put it in words. He says, in a rather wondering tone: 'It says thank you. It says it's been so tired.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("listen", "Tuck Translates").gap(16)
                .intro(
                        "* Tuck walks into the Court's camp on the plain at dawn with a sprig of holly in his cap and a pipe in his hand. He walks as an acolyte walks, steadily, looking at nothing. He has not slept for three days. He has not said what he is going to say. The Guests and the Snuffers and the Captains turn in their camp-chairs as he passes. He stops in front of Madam Dusk, who is sitting at a little table under a grey awning with a pot of tea.")
                .friends(8, "Stand behind Tuck: make eight friends.")
                .outro(
                        "@Tuck| I'd like to read you something. It isn't from me. It's from the wood. I listened to it for a very long time. I'd like to say it exactly. It would be rude not to.",
                        "* He puts the pipe to his lips. He plays. It is a long, slow, aching tune, with nine notes in it, over and over, and under the tune, in the deep part of the sound, a voice. The voice is not Tuck's. It is the wood's. It speaks in a vast slow murmur that comes out of the pipe like smoke, and every word of it, the Guests and the Captains hear, in their own tongues.",
                        "@Wood| (through the pipe, very distant) I have stood a thousand years. I have grown and dropped and slept and grown again. I have loved every one of my leaves. I love them best when they fall. A thing that is held for ever is not held. It is kept. I will not be kept. I wish to be a tree.",
                        "* The tune ends. There is a silence. The tea has gone cold on the table. Madam Dusk sits looking at her cup with her hands in her lap, as a woman looks at a letter she has been dreading to read. After a long time she says, almost inaudibly: 'I see.'")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("rings", "Rowena Counts Rings").gap(16)
                .intro(
                        "* The old oak falls in the night. It is nobody's fault. It has stood on the hill through the autumn that would not end, and it has had enough. At dawn Rowena finds it lying across the path with its roots in the air, enormous, pale, and quite peaceful, with a faint breath of resin on the cold. She stands for ages looking at the stump. Then she goes for her measuring tape.")
                .wait(4)
                .outro(
                        "* It takes her a day to count the rings. She does it on her knees, with a magnifying glass and a pin and a notebook, working her way from the heart outward, one ring at a time. She does not speak. She marks each century with a dot of ink. When she has done she sits back on her heels, with her hair in her eyes, and says, in a small flat voice, that there are three thousand and eleven.",
                        "@Rowena| And every one of them is different. Look. This one is thick, and this one is thin. This is a drought. This is a flood. This is a year when the whole world was warm. And this is a year with nothing in it at all. It's not a gap. It's a rest. There is a narrow pale line, here, and here, and here. The tree has been counting its winters.",
                        "* She holds the glass over the heart of the stump. At the very centre, tiny and pale, there is a ring that is not a ring. It is a seed. It is a tiny brown acorn, as old as the oak, perfectly preserved. She picks it up between finger and thumb. It is warm. She says: 'It's been in there all along.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("tea", "A Cup with Madam Dusk").gap(16)
                .intro(
                        "* You take the acorn to the foot of the hill, where the Guest sits at her table under the grey awning in the golden light. You put it down by her cup. She looks at it for ages. She does not pick it up. She is silent. The still air stirs. A single leaf, red as a coal, comes loose from the branch above her and drifts down onto the table, and lies there, turning slowly.")
                .wait(4)
                .choice(new Chapters.Choice("autumn", "Madam Dusk has seen the acorn and heard the wood, and has not yet put down her teacup.",
                        "Refuse her: the autumn must end, and winter must come.", "Bargain: ask for one more day, to say goodbye to the gold.", "Invite her to stay: not as a Guest of the Court, but through the winter, in the hut by the trees.")
                        .after(
                                new String[]{"* You refuse. You say it gently, as one tells a friend that she has made a mistake. You say that the wood does not wish to be kept. You say that winter is a mercy and not a cruelty. Madam Dusk looks into her cup. She says, in a very small voice, that she knows. She says that she has always known. She says she was only so tired of watching things end.", "@Dusk| I should like it noted that I did try."},
                                new String[]{"* You ask for a day. She stares. Then, with a little breath of laughter, she says she supposes it is not much. You spend it with her in the golden wood. You walk in the amber light among the unfallen leaves, and she names every tree, and every tree answers. At dusk she lifts her hand, and the leaves begin to fall, not all at once, but one by one, like a very slow applause.", "@Dusk| It was a lovely day. I shall remember it."},
                                new String[]{"* You invite her. She looks up with a startled expression. She says that nobody has ever asked. She says that she does not know how to stay. You say it is simple: you sit by a fire, and the snow comes, and you wait. She thinks about it for ages. Then she folds her hands in her lap and says: 'I should like to see one. Just once. I have never seen a winter all the way through.'", "@Tuck| (softly, from the log, with the pipe on his knee) She'll be all right. The wood likes her."}))
                .outro(
                        "* She stands. She takes up her basket and her flask and her little cups, and folds the awning, and bows. The leaves fall about her in a slow soft rain, red and gold and brown, one after another, onto the grass. She holds out her hand, palm up, and one settles on it. She looks at it. She says: 'Oh, it's cold.' And she smiles, a wondering, tired, lovely smile, as a child smiles at the first snow.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("snow", "First Snow").gap(8)
                .intro(
                        "* It comes in the night, on the twentieth of November, without a sound. You wake at dawn to a white light on the ceiling and a hush in the world. You go to the door of the wagon. The plain is white. The hill is white. The trees on it are white to the tips of their branches, with the snow laid along each twig like a line of chalk. Every sound is muffled. Every colour is gone. The sky is a pale pearl grey, and from it, quite slowly, quite steadily, the flakes are drifting down.")
                .wait(4)
                .outro(
                        "* There is a man in the snow. He is standing in the middle of the plain in a plain apron, with a spoon in his hand, bareheaded, with the flakes settling on his hair. He has come out of the Palace. He has come out of the gates, and across the white, in his house-shoes, alone. He is looking up. His face is wet. He is laughing, a high delighted astonished laugh, like a child who has seen the sea.",
                        "@???| It's falling! It's all falling! It doesn't stop! Does it stop? I do not know how to feel. I thought it would be cold. It is cold. I did not know it would be so, so kind.",
                        "* You walk out to him across the field. You stand together in the white. A snowflake lands on your sleeve, a perfect star, and lies there for a moment before it melts. He bends over it, with his hands behind his back, and looks, and says nothing at all for ages. Then he says: 'Is it always like this?' And you say: 'Every year.'")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("last", "The Last Almanac").gap(24)
                .intro(
                        "* They bury your mother in the spring. It is not a burial. She goes on the first warm day of March, in the clearing, under the young oak that has grown from the acorn. She sits on the stone bench with her hand on the sapling's trunk and the almanac on her knee, and reads the first page aloud. Her voice is very thin. She reads it to the end. She closes the book. She says: 'There.' And her hand, on the bark, goes still.")
                .wait(8)
                .choice(new Chapters.Choice("almanac", "The almanac has one empty line under your mother's last, and you have the pencil.",
                        "Write the first page again: the wood will stay as long as it is wanted.", "Write the winter: let it come, and let it go, and let the book end with it.", "Give the pencil to Tuck, and let the next hand write what it hears.")
                        .after(
                                new String[]{"* You write it. You copy it out in your best hand, word for word, under your mother's. The wood will stay as long as it is wanted. When it is wanted elsewhere, it will go. You put the pencil down. The sapling stirs in the wind, and a single new leaf, bright as a flame, unfolds from a bud on its crown.", "@Rowena| (very low) It's the oldest sentence in the book. I think that's why it works."},
                                new String[]{"* You write: Winter came, and was welcome. Then, in a smaller hand, underneath: and went. It is the plainest entry in the almanac. You close the book. You tie the cord. You set it on the bench, in the sun, and leave it there, and when you come back in the evening a small green shoot has pushed up through the cover, where the primrose was.", "@Tuck| That's new. It's never done that."},
                                new String[]{"* You give him the pencil. He looks at it for a minute. He sits down on the bench with the almanac on his knee, and the squirrel on his shoulder, and listens. The wood is silent. It is the silence of someone deciding. Then Tuck puts the pencil to the paper and writes, slowly, in a round clumsy childish hand: I can hear it. It says: soon. He looks up with his whole face open. 'It's not a word they like,' he says. 'It's a promise.'", "@Bryn's voice| (from the oak, very faint, amused) Quite right."}))
                .outro(
                        "* You walk down the hill at sunset with the almanac under your arm. The city is below, grey and gold. The wood is behind, dark and tall. Between them the plain is green with the first thin blades of grass. A thaw has come, and a smell of mud and blossom and rain. Somewhere in the Palace a bell is ringing. Somewhere on the hill a stag calls, long and clear. And in a hut of green boughs at the edge of the trees a light comes on, and a woman with a brass telescope puts the kettle on.")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* The Greywood stands on a hill outside Originis. It has a path up the middle that nobody cut. Every spring the almanac is read aloud under the great oak, and every autumn the leaves fall.",
                "?autumn=1|* A grey woman visits at midwinter and leaves a small cup of tea on the stump of the old oak. By morning it is always empty.",
                "?autumn=2|* There is a day in October when the leaves fall one at a time, slowly, in the amber light. The village calls it Dusk's Day.",
                "?autumn=3|* A thin grey lady lives in a hut at the edge of the trees. She keeps a calendar. She marks the first snow.",
                "?almanac=1|* The first page of the almanac says: The wood will stay as long as it is wanted. It is the oldest sentence in the book.",
                "?almanac=2|* The almanac has a shoot growing from its cover. It has been transplanted to the clearing. It is a tiny oak.",
                "?almanac=3|* Tuck keeps the almanac. Its pages are filled in a round clumsy hand. They record the dates of the first frost, the first swallow, and the first time anyone laughed.",
                "?axe=1|* The Reeve's bottle stands on the shelf in the wagon, with its slip of paper. It says: Understood.",
                "?axe=2|* There is a young forest along the road from the mountains to the city. It is planted in neat rows. It is slowly forgetting that it was.",
                "?axe=3|* On the south side of the hill there is an oak with a face in the bark. It is a plain, square, tired face. Children leave it a toffee.",
                "?love=1|* Rowena Thistle surveys the Greywood on Mondays. On Tuesdays she draws the bees.",
                "?love=2|* Rowena has a key to the cottage and a hammock in the barn, and has been seen to use both.",
                "?love=3|* Rowena Thistle keeps a calendar of the winter. She writes in it, every night, the name of someone she is waiting for.",
                "* On the shelf by the stove in the cottage, between the jar of morels and the cord of onions, the almanac sits, fat and brown, with a small green shoot at the corner of its cover. It has no title. If you open it at the first page you will see, in your mother's thin upright hand, the sentence she read aloud every spring. And under it, in pencil, a new line, in a hand that is not quite anyone's: Thank you for the winter.");

        // @@END
    }
}
