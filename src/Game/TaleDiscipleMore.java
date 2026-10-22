package Game;

/** The Wandering Immortal's Disciple, seasons 4 and 5 (2026-10-20), in the fable and pastoral voices.
 *  Season 4, "Pass It On": the human is the teacher now. Brook is a proper disciple, the Three Drunken Moves are taught badly in several villages, a boy wants to win, Tobiah Lantern is mean with a cup, and Brook invents a wrong fourth move
 *  that works. Choice: pass (the cup). Flags: pass, road.
 *  Season 5, "The Unfinished Road": the road that is not finished (from the first season) is built by volunteers across a province, stone by stone, to the plain where the called wait. Choice: paving (what the last mile is).
 *  The Door season (StoryDoors) comes after. */
final class TaleDiscipleMore {
    private TaleDiscipleMore() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "pass", "paving");
        d.place("hall", "wild").place("plain", "far");

        d.interlude(3, "The Gourd Is Heavier",
                "* The red gourd is heavier than it was. You noticed it first on the road, on a hill, in the rain. It is not that it is fuller. It is that more is hanging on it. Brook walks beside you with her hat down and her dog at heel, and for nine miles neither of you says anything.",
                "* At the top of the hill she stops. She says she would like to be taught properly. She says she has been following you for eleven years and she thinks it is time one of you admitted it.");
        d.season(4, "Pass It On");

        d.chapter("heavier", "The Gourd Is Heavier").years(3)
                .major(5)
                .intro(
                        "* There is a rule of the road that Brook has never learned. It is that a teacher does not ask to be asked. He leaves a crust on a stump and waits. She has not left a crust. She has stood in the rain in front of you, in her thin coat, with her arms folded and her chin up, and demanded it, in a high clear furious voice, in front of a dog.")
                .wait(4)
                .outro(
                        "@Brook| I can do the three moves. I've done them since I was ten. I want the fourth. I want to know what it is. I think you know. I think the old man told you, on the last night, and you've been sitting on it like a hen on a stone.",
                        "@You| There isn't a fourth.",
                        "@Brook| (with her jaw working) That's what you would say.",
                        "* You sit down on the stump. You take out the gourd and look at it. You say that there is no fourth move, and that she is quite right to want one, and that you will teach her the first three, again, properly, as they were never taught to you. You say it will take about a year. She says that she will give it six months. You say that is a very good start.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("badly", "Three Moves, Badly").gap(8)
                .intro(
                        "* The village of Upper Mumble has a hall with a stove and a floor, and it has a problem. It has forty children and a dry spell and nothing to do. The headman has heard that you are a master. He has offered a roof and a dinner if you will teach the young people something improving. You say that you will do your best. You say it with complete honesty. You have never taught anyone anything in your life.")
                .wait(4)
                .outro(
                        "* You stand in front of forty children with a red gourd and say that you will show them the first move. You lift your hand, as if to hold a cup, and tip your head back. They do the same. Forty small heads tip back at once. The headman's wife lets out a cry of alarm. A boy at the end falls over. A girl at the front, aged six, in an enormous cap, tips her head so far that she sits down on the floor and goes on looking at the ceiling with an expression of tremendous interest.",
                        "@Girl| It's a long way up.",
                        "@You| That's the first move.",
                        "@Girl| (in awe) I thought it was a dance.",
                        "* You teach the second in the same fashion, and the third. By evening the children have learned them all, entirely incorrectly. They have invented a game with them. It is played with a bucket and takes three teams. The headman declares it the most improving thing the village has seen. You collect your dinner. Brook, at the back, has her face in her hands, and is shaking from head to foot with laughter.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("winner", "The Boy Who Wanted to Win").gap(8)
                .intro(
                        "* He is called Garrick and he is seventeen. He has been following you down the road for three days, a lean dark boy with a sword he does not know how to hold and a look of desperate certainty. He catches you at a ford, in the morning, with his chin up. He says that he has heard of the Three Drunken Moves. He says that he would like to learn them, and use them, and win.")
                .wait(2)
                .outro(
                        "@You| Win what?",
                        "@Garrick| (blankly) Everything. Whatever's going.",
                        "* You tell him a tale. It is the tale of a man who went into a tournament and won it, in the first round, with the second move, by falling over. He fell so hard and so far that his opponent, in a fit of alarm, ran to help him up, and in doing so tripped over a root and was knocked senseless. The winner was crowned, and given a purse, and a sword, and a horse, and a fine large house, and was never again so happy as he had been on the ground.",
                        "@Garrick| That's not how you win.",
                        "@You| No. That's how you fall over.",
                        "* He is silent for minutes. He stands in the ford up to his knees, with his sword in his hand, and the water runs round his boots. At last he says: 'I didn't know there was another kind.' He puts the sword down on the bank. He asks, in a smaller voice, whether he may carry the gourd.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("pie", "Granny Ottoline's Pie").gap(8)
                .intro(
                        "* Granny Ottoline has taken to entering the pie contest at the harvest fair. She is a hundred and one. She sits at a trestle table at the end of the row in her lace cap, with a pie the size of a cartwheel and a face of absolute composure, and every year she is disqualified. The judges say it is an unfair advantage. Nobody can say what the advantage is.")
                .wait(2)
                .outro(
                        "* This year you sit beside her. The judges walk the row, tasting. They reach her table. They look at the pie. They look at one another. They taste, and their faces go oddly slack, and the senior judge, a stout man with a tape-measure moustache, sits down on the grass with his plate on his knee. He says nothing. He eats the lot. He says that it is a plum pie. He says that it is also, he does not know how to put it, a rather good afternoon.",
                        "@Ottoline| It's the fourth move. I've never told anyone. It's what you do with a cup when there's nothing in it. You pass it. I pass it in a pie.",
                        "@You| That's not the fourth move.",
                        "@Ottoline| (comfortably, with a spoon) It is if I say so. Have a bit. I've made enough for the parish.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("tobiah", "Tobiah Lantern Asks").gap(8)
                .intro(
                        "* Tobiah Lantern keeps the old inn at the crossroads. He is a long, lean, sour man with a drooping moustache, and he is the meanest with a cup in three counties. He waters the wine. He rations the candles. He counts the crumbs. He has been, as far as anyone knows, perfectly miserable for forty years. He catches you at the door of the inn one evening and asks in a rough voice whether he might have a word.")
                .wait(2)
                .outro(
                        "@Tobiah| I want what you have. I don't know what to call it. It's the thing that makes people stop and sit down. They sit in your corner and laugh. They sit in mine and drink up and leave. I've kept an inn for forty years. I've never had anyone stay.",
                        "@You| Have you ever given anything away?",
                        "@Tobiah| (appalled) What for?",
                        "* You tell him to try it. You tell him, as gently as you can, to pour one cup of wine tonight, the good wine from the cellar, for the first person who comes in the door, and not to charge. He stares at you as if you had asked him to cut off his hand. He goes away. The next morning there is a note on the table of the corner seat. It says, in a cramped furious hand: Nobody came. I gave the cup to the cat. Tobiah Lantern.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("student4", "A Student, Against Advice").gap(8)
                .intro(
                        "* The girl who follows you for a week is named Pim, and she is eleven. She says she has run away from a tannery. She smells of it. She sleeps in a ditch and says nothing at all. On the eighth day she catches you at a well, in the dusk, and stands in front of you with a rag round her hand and says in a flat voice that she has decided to be your disciple.")
                .wait(2)
                .outro(
                        "* You say that you have a disciple. You say that you already have one, and a dog, and a boy who carries the gourd, and that it is quite enough. She nods. She says that she understands. She sits down on the ground where she is and folds her hands in her lap and does not say another word. She stays there all night. In the morning, in the frost, she is still there, with her lips blue.",
                        "@Brook| (coming up the road with a blanket, grumbling, with her arms full) You can't leave her. You can't. It's the first rule. The old man took us all in.",
                        "@You| I know.",
                        "@Brook| Then why are you standing there like a post?",
                        "* You pick her up. She weighs nothing. She is asleep before you reach the road. You put her on the dog.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("fish", "The Fish That Would Not Be Caught").gap(8)
                .intro(
                        "* A fisherman on the River Tuft has a complaint. There is a pike in the pool below the weir that he has been trying to catch for nineteen years. He has used every bait. He has used every net. He has sat on the bank in all weathers with a rod, and the fish, an enormous grey one, a hundred years old, has watched him from the shallows with perfect indifference. He asks whether the master has a move for it.")
                .wait(2)
                .outro(
                        "* You sit on the bank beside him. You take the gourd and drink. You do not fish; you tip your head back, in the first move, and look at the sky. A cloud goes over. A heron lands. The pike rises in the pool, quite slowly, and breaks the surface, and lies there with one cold gold eye on you.",
                        "@Pike| (in a voice like a rusty hinge) He has never once looked at me.",
                        "@Fisherman| (startled, with his rod in his lap) What does it say?",
                        "@Pike| He sits there with his eyes on a float. He has never looked at me. I would have come out years ago. All I wanted was for him to look.",
                        "* The fisherman looks. It takes him a long time. He puts down the rod. He gets to his feet and goes to the edge of the water, and kneels, and the great fish comes up to meet him, and they regard one another for a minute in the evening light. Then the pike turns over, like a door, and sinks. The fisherman goes home. He never fishes again. He is seen on the bank every morning, with a flask, looking.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("cannot", "What You Cannot Teach").gap(8)
                .intro(
                        "* The first move will not come to Brook. She has made it ten thousand times. She makes it at dawn, and at dusk, and in the rain, and with the dog watching, and every time it is wrong. She tips her head back. She lifts her hand. And nothing happens. The hand does not hold a cup. It holds the air. It is a thing she does, not a thing she is.")
                .wait(2)
                .outro(
                        "* She comes to you at dusk on the fourteenth day with her face white and her jaw tight and says that she is going to leave. She says that it is not working, and that it will not, and that she is tired of being laughed at by a dog. She says it all in one breath, at the foot of the stump, in a rising voice, and the last words crack.",
                        "@You| Who is laughing?",
                        "@Brook| (savagely) I am.",
                        "* You do not say anything. There is nothing to say. You take the gourd off your hip and put it into her hands. She stares at it. She says it is heavy. You say that it is. You say that you have never in your life been able to teach anyone anything, and that you are very glad of the company. She holds the gourd against her chest and bursts into tears.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("beats", "Brook Beats You").gap(8)
                .intro(
                        "* She asks you to spar on the first of the month. It is a clear day in the meadow behind the inn, with a wind in the grass. The whole of the district has come to see: Tobiah in the doorway with his arms folded, Granny Ottoline in a chair with a hamper, the boy Garrick with the gourd, little Pim on the dog. A line is drawn in the dirt with a stick. You stand on one side with your hands in your sleeves. Brook stands on the other with her hands open.")
                .wait(2)
                .outro(
                        "* It is over in a moment. You make the second move. It is the move you have made ten thousand times, a lurch, a stagger, a roll. She does not meet it. She does not move at all. She stands where she is, in the grass, with her hands at her sides, and looks at you with a face of perfect calm. And as you come on, off balance, falling toward her, she steps one pace to the left, and lets you pass.",
                        "* You hit the ground. It is soft. You lie there with the sky over you and a grass-seed in your teeth, and the whole meadow is silent, and then Pim says in a high clear voice: 'She dropped the cup.' You turn your head. It is true. There is a clay cup lying in the grass by Brook's foot. She was holding it the whole time. She was holding it in the hand that was empty.",
                        "@Brook| (quietly, looking at her hand) That's the first move.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("wrong", "The Wrong Fourth Move").gap(8)
                .intro(
                        "* Brook invents it in the spring. It is entirely incorrect. It is a sort of sideways skip, with a hop at the end, and an arm thrown out as if to catch a falling bucket. She does it at the kitchen door of the inn. Tobiah, who is watching, with his sour long face, lets out a snort. The cat on the sill falls off. Garrick, behind her, says in a hushed voice: 'That's not in the book.'")
                .wait(2)
                .outro(
                        "* There is no book. That is the whole joke, and also the point. She has made a thing that is not one of the three, and not the fourth, and not any move that has ever been taught. It works. You learn this in the afternoon, on the road, when a bandit with a cudgel steps out from behind a tree. He swings. Brook skips sideways, and hops, and flings out her arm, and the cudgel goes through where she was and the bandit goes through where he was. The pair of them end up in the ditch, at the bottom, in a heap, in the mud.",
                        "@Brook| (climbing out, flushed, with a leaf in her hair) Did I do it right?",
                        "@You| No.",
                        "@Brook| (beaming) Good.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("inn4", "The Corner Seat").gap(8)
                .intro(
                        "* The inn at the crossroads has a corner seat. It is a plain oak settle with a high back, beside the hearth, and nobody has sat in it for a year. Tobiah keeps it clean. He dusts it every Monday. He has put out a cup, and a bottle, and a candle, and he will not say for whom. On the first night of autumn, as you come in at the door with a gourd on your hip and a dog at your heel, he stops with the cloth in his hand.")
                .wait(2)
                .outro(
                        "@Tobiah| It's yours. It was always going to be. He said so, you know. The old man. He came through here forty years ago, a ragged drunk, with a gourd, and sat in that seat, and left without paying. I ran after him. He turned round and said: I'll leave you the seat. I've been keeping it ever since.",
                        "* You sit. The oak is smooth and warm. The fire is low. The room, which was empty, fills as you watch: a drover, a pedlar, a girl with a harp, a very old man, a child. They come in out of the dark and take their places and say nothing. Tobiah pours. He pours the good wine from the cellar, a brimming cup, for each, and does not take a penny.",
                        "@Tobiah| (setting the last cup before you, in a husky voice, with his eyes on the table) I think I've worked it out. It isn't the giving. It's the sitting down after.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("pass4", "Cup Passed").gap(8)
                .intro(
                        "* The cup is a plain tin one, dented, with a green glaze inside. It has been in the family of the road since before anyone can remember. The old man gave it to you. You are to give it to someone. That is the whole of what you know. You have carried it, with the gourd, for most of your life, and you have never once dared to ask what is meant by the giving. Tonight, at the hearth of the inn, with the fire low and the dog asleep, you turn it over in your hands.")
                .wait(2)
                .choice(new Chapters.Choice("pass", "The cup has been yours for a lifetime. Brook is sitting by the fire.",
                        "Pass it to Brook: she has earned it, wrongly, in her own way.", "Pass it to the stranger by the door: whoever comes next, and not the one you expect.", "Keep it a little longer, and pass it when you are sure.")
                        .after(
                                new String[]{"* You hold it out. She looks at it as at a live thing. She does not take it at once. She puts out both hands, like a child, and receives it, and brings it to her chest, and looks up at you with a face from which all her anger has gone. She says nothing. There is nothing she could say. She drinks from it, once, and it is empty, and the whole room, to the last stranger, breathes out.", "@Granny Ottoline| (from her chair, comfortably) About time."},
                                new String[]{"* You pass it to the stranger by the door. He is a thin grey man in a thin grey coat, with a pack, and a face you have never seen. He takes it, astonished. He looks at Brook. He looks at you. He asks what it is for. You say that you pass it. He asks who to, and you say that he will know. He sits with it in his hands, turning it, for the rest of the evening, with a look of enormous private effort.", "@Brook| (to herself, delighted) That's exactly right."},
                                new String[]{"* You keep it. You say that you are not sure, and that a cup passed too soon is a cup dropped. Brook nods. She is quite calm. She says that she knew. She says that is why she is not angry. She leans against the settle with her knees drawn up and her chin on them, and the dog's head in her lap, and says that she would like to hear a tale.", "@Tobiah| (setting down the bottle) I'll fetch another candle."}))
                .outro(
                        "* The morning is clear and cold. The road runs away over the hill into the pale gold distance, and you stand at the door of the inn with the gourd on your hip. Behind you, in the kitchen, Brook and Tobiah are arguing about the price of the wine. Garrick is feeding the cat. Pim is asleep on the dog. Somewhere on the hill a man is whistling a tune, a little flat, that you have not heard since you were young.")
                .pay(2000, 3, 0, 4, 2, 2);

        // ================================================================== SEASON 5
        d.interlude(4, "The Road That Is Not Finished",
                "* There is a road that runs from the old inn at the crossroads to the edge of the plain. It is a road of beaten earth and flint, and it stops. It has stopped at the same milestone, nine miles short of the plain, for as long as any man living has known. Children are told that it is a road that is not finished.",
                "* Brook comes up the lane one morning with a spade over her shoulder and a face of settled purpose. She says she has an idea. She says that it is a bad one, and that she would like you to hear it before she is talked out of it.");
        d.season(5, "The Unfinished Road");

        d.chapter("survey", "Nine Miles").years(3)
                .major(6)
                .intro(
                        "* The surveyor is a woman of forty with a theodolite and a lunch. She walks the nine miles in a day, with Brook, and Garrick, and the dog, and a child with a chain. She stops every hundred yards to drive in a peg. She mutters to herself. At the end she sits on the last stone of the old road, at the milestone, with her notebook on her knee, and works a sum on the page with a stub of pencil.")
                .wait(4)
                .outro(
                        "@Surveyor| Nine miles, four furlongs, and thirty yards. About three hundred thousand stones. I'd say two thousand people, a summer. It's all downhill and none of it's hard. It's only that nobody's ever been asked.",
                        "@Brook| (flatly) I'm asking.",
                        "* She looks up at the plain. It is a long bare grey expanse, under a grey sky, with a faint low shape on the horizon that might be a wall, and a line of tiny dots beneath it that might be people. The surveyor shades her eyes. She says nothing for a time. She says: 'Who's it for?' And Brook says: 'The ones who walk it.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("stones", "A Stone Apiece").gap(8)
                .setup(TaleDiscipleMore::mark)
                .intro(
                        "* You put up a notice at the crossroads. It is a very short one. It says: A ROAD IS TO BE FINISHED. BRING A STONE. Nobody takes it for a joke. They come in ones and twos on the first morning, a baker with a flat stone from his oven-floor, a boy with a pebble, an old man with a doorstep. They lay them in the dust at the end of the old road, in a line.")
                .made(10, "Lay the first stones: make ten.")
                .outro(
                        "* By the end of the week there are four hundred. They come from the whole district, in barrows and sacks and aprons and a child's red cart. They are all sorts: river-rounded cobbles, slate, a millstone, a tombstone with the name worn off, a pig's trough, a piece of a bridge. They go down side by side in a long uneven strip, bedded in sand. It is the ugliest road in the country. It is the only one that anybody wants to walk.",
                        "@Garrick| (leaning on a shovel, red in the face, with a stone in each hand) There's no pattern.",
                        "@Brook| (kneeling at the edge, tamping) There's a pattern. It's everybody.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("toll", "The Toll-Keeper").gap(8)
                .intro(
                        "* A thin man in a good coat sets up a table on the road at the end of the second month. He has a cash-box and a ledger and a notice. The notice says: TOLL, ONE PENNY. He explains with great patience that all roads are the property of the Crown, and that a road is a facility, and that a facility must be charged for. He is a minor official of the Board of Highways. He is very sorry. He has the paper.")
                .wait(4)
                .outro(
                        "* You stand with the paper in your hand for minutes. It is genuine. It is in order. It says that any person who builds a road must pay a penny a yard for the privilege. You count the yards on your fingers. It comes to nine hundred pounds. You look at the toll-keeper. He looks at the sky.",
                        "@Brook| (at your elbow, in a whisper) I could hit him.",
                        "@You| You could.",
                        "* Instead you sit down on the edge of the road beside his table and open the gourd. You pour a cup. You offer it. He says that he is on duty. You say that you know. He takes it. He sips. He sits there on the road in his good coat with a tin cup of wine. He looks at the long line of ugly stones, and at the sun, and at the children laying them. At last, in a thick voice, he says that the paper may be out of date. He says he must send to the capital to check. He says it may take some years.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("ottoline5", "Granny Ottoline Walks").gap(8)
                .intro(
                        "* Granny Ottoline insists on laying a stone. She is a hundred and three. She is driven to the road in a pony-cart, in her lace cap, with a rug round her knees, and set down at the far end of the finished stretch, where the new surface meets the old dust. She has brought her own stone. It is a small smooth one, a grey river-pebble, the size of an egg, and she has carried it in her pocket for ninety years.")
                .wait(2)
                .outro(
                        "* She does not lay it. She holds it a long time, in both hands, and looks at the plain. She says that she had it from a young man who was in a hurry. She says that he was ragged, and that he laughed a great deal, and that he gave it to her at a ford and asked her to keep it until the road was done. She says that she was nine. She says that she has been waiting, and she is not at all cross.",
                        "@Ottoline| It's a good road. It's not what he meant. He meant a smooth one. But I think he'd have liked the pig's trough.",
                        "* She leans out of the cart. She lays the pebble in the dust, in the very last gap, where the road ends. It fits exactly. She sits back, and arranges her rug, and shuts her eyes. You think she has gone to sleep. Then she says, without opening them: 'Well, that's me done.' And she is quite right.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("rain5", "Rain Follows Laughter").gap(8)
                .intro(
                        "* The rains come in the autumn, a steady grey downpour that lasts nineteen days. The road turns to a strip of cold porridge. The stones sink, and the sand washes, and the ditches fill. The volunteers go home. The surveyor goes to the town. There are four people left on the road, with a dog, in a tarpaulin lean-to, with a fire that will not light.")
                .wait(4)
                .outro(
                        "* You pass the time with a game. It is the old game, the one the children of Upper Mumble invented with the three moves and a bucket. You play it under the tarpaulin, with a tin pail and a cup and a boot, in the smoke, and it is the most absurd thing in the world. You laugh until you cannot stand. Brook laughs so hard she falls off a barrel. The dog howls. Garrick has tears running down his face.",
                        "* When you look out, the rain has stopped. It stopped some time ago. The clouds have gone grey-gold at the edges, and behind them there is blue. The road, in the wet light, shines like a river. Somebody has laid a plank across the worst of the puddles. Somebody else has brought hot soup. Out of the mist, one by one, in ones and twos, they are coming back.",
                        "@Brook| (wiping her eyes, with a ragged grin) It always does. Rain follows laughter. That was his, you know. It was the only thing he ever said that was true.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("bridge5", "A Bridge for Two").gap(8)
                .intro(
                        "* The road comes to a stream. It is a small stream, a brook in a hollow, and it has no bridge. It has stepping-stones. You stand on the bank with a stone in your arms, and look at the water, and think that you could ford it, and so could anyone, and that a bridge would be a luxury. Brook says a bridge is not a luxury. She says it is a way of saying: I thought you might be coming.")
                .friends(5, "Build the bridge: make five friends.")
                .outro(
                        "* You build it with the whole district. It is a plain one, of two beams and some planks, wide enough for two to walk abreast, with a rail on each side. A carpenter makes the rail. A girl paints a small blue flower on every post. A boy of eight carves his name in the middle plank, and another boy carves his under it. Then, in a spirit of generosity, a third carves a third, and by the time it is done the whole bridge is covered in names.",
                        "@Garrick| (at the rail, looking down at the water, in a quiet voice) My father would have said it was a waste. He would have said, who walks here? Who walks to a place like this?",
                        "@Brook| (beside him, resting her chin on the rail) People who are called.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("soldier5", "The Soldier Who Walked Early").gap(8)
                .intro(
                        "* The first of them comes along the road in the spring. He has no lamp. He has no ledger. He is a young soldier in a plain coat, with a pack and a blister, and he walks as if he were late. He comes down from the north, and goes straight on, at a steady pace, along the new stones, to the end. There he stops. You have been watching from the bridge. You run after him.")
                .wait(2)
                .outro(
                        "@Soldier| I'm sorry. I didn't know it would be here. I came by the old way, across the heath. I'd been told it was twenty miles of bog. I'd been told the called had to walk it in the dark. I don't understand. It's lovely. Who made it?",
                        "@You| Everybody.",
                        "* He stares. He looks back at the road, a long strip of every stone in the district, bright in the evening sun. He looks at you. He says that nobody has ever done anything for him that he did not have to pay for. He says that he does not know how to take it. He says: 'May I walk it back? Only the once. Only to see how it feels?' And he does, to the crossroads and back, at a very slow pace, with his hands behind him, looking at every stone.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("lamps5", "Lamps for the Road").gap(8)
                .intro(
                        "* Brook says that it needs lamps. It is, she says, no good being a road if you are a dark one. The soldiers come in the dusk. She says that a lamp every thirty yards would be about right. That is nine thousand lamps. She says it as one would say a dozen eggs.")
                .wait(4)
                .outro(
                        "* The district makes them. They make them of tin, of horn, of glass jars, of paper, of old boots with a candle in the toe. Tobiah Lantern makes four hundred in his cellar with a face of unbearable pride. They are all different. The surveyor stands them on the verge at the correct intervals. Pim lights each, with a taper, at sunset, running up the road behind the dog.",
                        "* By the first frost the whole nine miles is a long line of small flames, going away into the dusk. It is the loveliest thing that most of the people there have seen in their lives. The old man who gave the doorstep stands at the first lamp with his hat in his hand, and does not say a word. When a boy asks him what it is for he says: 'So they'll know somebody was thinking.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("mile", "The Last Mile").gap(8)
                .intro(
                        "* The last mile is the hardest. It runs over the lip of the plain, across a stretch of loose grey gravel, in a bitter wind, to the foot of the wall. It is bare and cold. Nobody lives there. The stones are heavy, and the wind takes the dust. The district comes in a body, in coats, with barrows, to finish it. They work for nine days. On the ninth the last stone goes down at sunset.")
                .at("plain", "Lay the last stone on the plain, {@plain}.")
                .outro(
                        "* There is no ceremony. Brook kneels in the gravel with a mallet and taps it home. Tobiah pours a cup of wine on it. Garrick stands with the gourd. The surveyor, with her notebook, makes a small clean cross on the last page and writes beside it: Done. Behind you the lamps stretch away in a long gold line, nine miles back to the crossroads, and at the other end, on the horizon, in the thin light, a small grey fortress has its gates open.",
                        "* A tall woman with a grey plait is standing at the foot of the wall. She has been watching for minutes. She walks forward, slowly, and puts her foot on the new stone, and presses it. It does not move. She looks along the road to the lights. She looks at the crowd, in their aprons and boots and Sunday hats. She says: 'It is the first time anyone has come to meet them.'")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("paving", "What the Road Is For").gap(8)
                .intro(
                        "* The road is done. The district goes home. Brook sits on the last stone in the dusk with the dog's head on her knee, and the gourd beside her, and a look of enormous tiredness. She says that she has never before made anything that was finished. She says that she does not know what to do with her hands.")
                .wait(2)
                .choice(new Chapters.Choice("paving", "The road ends at the wall. The Garrison is a stone's throw away. The old man's tune is in the wind.",
                        "Keep it: a road kept swept, and lit, and never closed, for as long as the called go in.", "Let it fall: leave it to the weather, and let it become a path like any other.", "Walk it: take the road all the way, to the Garrison, and go in yourself, one day.")
                        .after(
                                new String[]{"* You keep it. A road-keeper's hut goes up at the foot of the wall, a small stone one with a stove, and a woman from Upper Mumble moves in. She sweeps the road at dawn and trims the lamps at dusk and has a pot on the fire for whoever comes. In ten years it is the best-kept stretch of highway in the Garden. People are said to come from three provinces to see it. Nobody ever walks it for pleasure. They do it for the company.", "@Brook| (lighting the first lamp, as the keeper's hut warms) Somebody will always be at the end."},
                                new String[]{"* You let it fall. You do it on purpose. The first winter takes the sand. The second takes the lamps. By the fifth, the stones are green, and the road is a ribbon of moss between two hedges, and a child can run along it in a minute with a hoop. It is a path like any other. It is a good one. The called go in along it all the same. It is simply that now nobody notices that they were ever expected.", "@Surveyor| (grinning, with a new notebook) I like a road that has made its point."},
                                new String[]{"* You walk it. You do it on a clear morning in the spring, alone, with the gourd on your hip. It takes the whole day. You pass every stone. You know them: the tombstone, the trough, the millstone, the little grey pebble in the last gap. You come at dusk to the foot of the wall, and the gate, and the woman with the plait. She looks at you for ages. She does not write your name. She says that she will wait. She says: 'It's a good road. Come back when you've finished walking it.'", "@Brook| (a mile behind, running, with the dog) You might have waited!"}))
                .outro(
                        "* The wind rises on the plain. In the failing light the road is a ribbon of gold running away behind you, nine miles long, lit lamp by lamp. In front of you the wall, and the gate, and the open dark. And in the dusk, on the stone beside you, a thin ragged man in a patched coat is sitting, with his legs crossed, and a gourd. He does not look up. He says, in a cracked amused voice, a little flat: 'It's not what I meant. But I like the trough.'")
                .pay(2000, 3, 0, 4, 2, 2);
    }
}
