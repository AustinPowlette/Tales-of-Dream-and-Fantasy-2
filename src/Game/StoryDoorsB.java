package Game;

/** The Door season (see StoryDoors) for the Ghost-Touched, the Lucky Fool, the Last of the Silver Crane Clan, the Wandering Immortal's Disciple, the Cook and the Apothecary. */
final class StoryDoorsB {
    private StoryDoorsB() { }

    static void add(Chapters.Def d, int n) {
        switch (d.id) {
            case "ghost": ghost(d, n); break;
            case "lucky": lucky(d, n); break;
            case "clan": clan(d, n); break;
            case "disciple": disciple(d, n); break;
            case "chef": chef(d, n); break;
            case "poisoner": poisoner(d, n); break;
            default: StoryDoorsC.add(d, n); break;
        }
    }

    // ------------------------------------------------------------------ the Ghost-Touched
    private static void ghost(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The Office of Returning has a new wing and a pigeonhole for every dead person in the province. The queue at the shop on the bridge is shorter on Tuesdays. The goose has been seen to nod at strangers.",
                "* One night a grey cat sits down on the step and does not move. It faces the pipes. It has the look of a creature that has been appointed.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "The Last Clients").years(2)
                .intro(
                        "* They come on the night of the Festival of Hungry Ghosts, in a long file along the river, each with a lantern. They are the dead you helped. There are hundreds. The baker with the bridge. The captain with the ring. The poet with her muse. The cheese. They stop on the Mill Bridge and look at you, politely, with the shy bright look of people who have been told a thing is allowed.")
                .wait(8)
                .outro(
                        "@Dumpling Lu| We wanted to say we're going. All of us. There is a Door, and a muster, and it is allowed for the dead, if they are asked. We have been asked. By a lady with a ledger. She says it is the only place left where we are wanted.",
                        "* They set their lanterns on the water. It is a very long line. They float away down the river in the dark, one after another, small and gold, and the whole city is on the banks to see them. The goose stands on the rail with its beak shut. For once it does not bite anyone.",
                        "@Lu| Will you come? You don't have to. You could stay and run the shop. But there's a place in the line, if you'd like it. You'd only have to say.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "The Dead in the Line").gap(26)
                .intro(
                        "* You go to see them off at the Garrison. It is the strangest muster the plain has seen: a column of lanterns going in at the gate, and among them a hundred men and women who are not breathing, in the clothes they died in. The woman with the grey plait has put a second ledger on the table. It has two columns. It says Lent and Returned.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* They go in quietly. The captain with the ring bows at the gate. The poet is composing something under her breath. The baker, who was one of your first, stops at the sill and turns, and holds up a small loaf. It is warm. It is a thing she baked, in the afterlife, out of nothing, to give you. You take it. It tastes of every morning you ever had.",
                        "?!dead.tally|@Tally| (a grey cat on the sill, in your head, formally) The Office has no objection to the departure of the clientele. I should like it noted.",
                        "* When the last has gone in, the gate closes, softly, though there is no door. The plain is empty. You stand there in the dusk with a loaf in your hand. It is still warm.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "Lost Property").gap(26)
                .intro(
                        "* The Empty Seat has a drawer. You find it on your way in, behind the chair, a long flat brass drawer, at knee height, with a label in a hand like a row of fence posts: LOST PROPERTY. It is unlocked. You open it. It is full of small things. A shoe. A button. A tooth. A folded paper with a name. They are, you realise, what each of the dead left behind at the Door.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The drawer is full. The Door is under the Seat. You have been the one who returns things all your life.",
                        "Stay: take the drawer to the shop on the bridge, and give each thing back to whoever comes for it.", "Go: put the drawer back, and carry your own thing through.", "Hold the latch: sit by the drawer and hand each thing to its owner as they pass.",
                        new String[]{"* You carry the drawer home on a cart, with a goose in attendance. Within a week the shop on the bridge has a new queue, of living people who have lost a button, a tooth, a name. You find the owner of every item. It takes eleven years. It is the best work you ever did.", "@Tally| (on the step) The Office wishes to commend this."},
                        new String[]{"* You close the drawer. You stand for a moment with your hand on it. Then you take off your scarf, the one you have worn since the first day, and fold it, and put it in with the rest. It is your own lost property. It has been waiting for you to notice."},
                        new String[]{"* You sit on the flags with the drawer open and a candle. They come, one at a time, out of the dark, and each of them pauses, and you find the right thing and put it in their hand. A shoe. A letter. A song. They go through. It is quiet. It is the only job in the world where you are never once told you are too late."}))
                .outro(
                        "* The goose is waiting at the gate of the Palace when you come out. It looks at you for a long moment with one cold eye. Then it bows. It is the first and only time. It turns and waddles away across the square with the dignity of something that has just won a very long argument.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* The shop on the bridge has eleven thousand things in it, each with a name on a tag. It is the biggest lost property office in the country. It is open every day.",
                "?latch=2|* Your scarf is in a drawer under the Empty Seat, with a tag in a hand like a row of fence posts. It says: Returned.",
                "?latch=3|* A brass drawer stands under the Seat, and a candle burns beside it. The Wicks say a quiet person sits there some nights and always knows the right thing to give.");
    }

    // ------------------------------------------------------------------ the Lucky Fool
    private static void lucky(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The Society of the Fortunate Hat has a hall now, and a brass band, and a newsletter that is read aloud at funerals. You have never once been to a meeting without a hat.",
                "* A postcard comes on a Tuesday. It shows a small cloud wearing sunglasses. On the back it says: Final audit. No alarm. Mrs. F.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "Final Audit").years(2)
                .intro(
                        "* Mrs. Fortune calls in person this time. She comes up the path in a flowered hat and a mackintosh, with a briefcase and an umbrella, and rings the bell. She is very old. She is also, you gather, a good deal older than that. She sits down at your kitchen table and takes out a ledger the size of a paving stone.")
                .wait(8)
                .outro(
                        "@Mrs. Fortune| It's only the final tally, dear. I like to do it in person. Let me see. Luck, ninety-eight, which was never yours. Friends, a great many. Hats, nine hundred and four. I find the sums come out very well. There is a small balance in your favour of one life, which you may spend as you please.",
                        "@You| What happens if I don't?",
                        "@Mrs. Fortune| It goes back into the books. Somebody else gets it. A person who needs a little luck will find a purse on the road one day and not know why.",
                        "* She shuts the ledger. She sips her tea. She says, rather shyly, that she has put in for retirement, and that she would like it very much if you would come to the party. It is to be held at the Garrison, on the plain. There will be cake.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "A Party on the Plain").gap(26)
                .intro(
                        "* The Society comes in procession. Two thousand people in tall black hats, with a brass band at the head and Pip at the front with a banner that says HAT'S OFF. They walk the whole way. They play all the way. By the time they reach the plain, half of them have blisters and all of them are happy.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}, for Mrs. Fortune's party.")
                .outro(
                        "* The line at the gate is silent and wet. The Society sets up its trestles in the rain, a hundred yards off, and unpacks the cake. It is the size of a cartwheel. Wilbur Hex arrives on the back of a snail, an hour late, with a card. The soldiers in the line look at the hats. One by one, they take them. They put them on. By the time they go in at the gate, every one of them is wearing a tall black hat with a silver cloud on the brim.",
                        "@Pip| It's the nicest thing I've ever seen. Is it all right? Is it allowed?",
                        "@Mrs. Fortune| (with a plate of cake, smiling) My dear, it is the only allowed thing there is.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "The Hat and the Seat").gap(26)
                .intro(
                        "* The Empty Seat has a hat on it. You find it there when you come in, in the cool grey light of the great hall, set a little crooked on the cushion of the chair that nobody sits in. It is a tall black hat with a silver cloud on the brim, and a tag in the lining in a hand like a spider's: For the Host. Return when wanted.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The hat is on the Seat. The Door is under it. You have a life to spend as you please.",
                        "Stay: put the hat on, and spend the life on being ordinary.", "Go: put the hat on and walk through the Door, and see whether it is lucky.", "Hold the latch: stay by the Door with the hat, and tip it to everyone who goes in.",
                        new String[]{"* You put it on. It fits. You go home and drop a cup, and step in a puddle, and miss a boat, and it sinks. It is a magnificent week. Pip says you look ten years younger. Wilbur sleeps in the teapot and tells people you are a Hidden Master.", "@Pip| Hat's off."},
                        new String[]{"* You put it on and stand a moment in the dark under the Seat. The flags are cold. The latch is a small brass thing, shaped like a bird. You say that you will go through, when you are ready, with a hat on. A small voice from under the stone says: that's the spirit. It is the voice of someone who has just filed a very satisfactory form."},
                        new String[]{"* You sit by the Door with the hat on and tip it to each one who goes in. Some of them laugh. Some cry. A few take off their own and tip it back. It is the least useful job in the Garden, and you do it for a hundred years, in a manner of speaking, and not once do you feel the least bit lost."}))
                .outro(
                        "* The goose has followed you from the Palace gates, or a goose very like it. It sits on the steps in the sun. It has the expression of a being that has seen everything. As you pass it says, in a flat polite human voice, as it did once before: 'Hat's off.'")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* You are, by any measure, an ordinary person. You wear a hat. It is the luckiest thing you own.",
                "?latch=2|* A tall black hat with a silver cloud hangs on a peg by the door. It is waiting for you to go through. It has been very patient.",
                "?latch=3|* A person in a tall black hat stands at the Empty Seat. They tip it to everyone who goes by. The Wicks have never seen them leave.");
    }

    // ------------------------------------------------------------------ the Last of the Silver Crane Clan
    private static void clan(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The hall is a house of two hundred and a very large well. The vein hums in the stones, the banner turns in the wind, and the tea is poured at midnight in the cellar by the head of the clan. It is you, still. You have not been replaced.",
                "* The hum changes key on a Thursday. Joren is the first to notice, from the bottom of the well. He says it is going somewhere.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "The Hum Changes Key").years(2)
                .intro(
                        "* It goes up a step. It goes up another. By the end of the week the vein under the hall is singing, not humming, a long clear bright note, and the banners in the hall ring with it like glasses at a feast. Pilcher's school sits in the yard with its fingers in its ears and its faces bright. Isla has stopped sewing. She sits on the steps with her needle in her lap and her head back.")
                .wait(8)
                .outro(
                        "* On the seventh day the note stops. In the silence you can hear every sound in the valley: the wind, the well-rope, a cat on a roof. The three cousins stand in the yard with their faces turned to the north, where the sky is the colour of a pearl. Tamsin says, quietly, that it is not a note. It is a name.",
                        "@Joren| It says: come. It says it nicely.",
                        "@Isla| (with her needle held up like a candle) It's the First Crane. She's calling the clan. She's asking if we'd like to see the roof.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "Stand on One Leg").gap(26)
                .intro(
                        "* The clan goes to the Garrison. It is a small procession, by the standards of the road: forty-one people, a goat, a pan the size of a cartwheel, and a banner on a pole. Marta walks in front with the dumplings. Poll brings up the rear, complaining of the weather. Pilcher carries the school's cat.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* They stand in the rain and watch the line go in. Tamsin has her arms folded. Joren has his hat in his hands. Isla holds the banner up, and the silver crane on it, standing on one leg, catches a gleam of light from the open gate. A tall woman with a grey plait stops in front of the banner and reads the motto. She reads it twice.",
                        "@Woman| Stand on one leg, see far. I've never had that from a clan before. I shall put it in the ledger.",
                        "* One by one the clan of the Silver Crane goes in at the gate. They do not go all at once. They go as people go to dinner, in twos and threes, talking, with their plates and their jugs. Marta is the last of the first batch. She puts a dumpling on the sill, as she always did, for the road.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "The Roof of the World").gap(26)
                .intro(
                        "* You go to the Empty Seat last. You go alone, on a clear cold evening, in the dark blue coat the clan wears at funerals. The hall is empty. The nine lamps burn without wavering. You sit on the flags with your back against the cold stone of the Seat, and look up into the dome. The dome, as you look, goes thin as glass, and you see a sky with no roof.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The First Crane is above you. The Door is under the Seat. The clan is, in part, already through.",
                        "Stay: keep the hall, and the vein, and the tea.", "Go: ride the First Crane up through the glass, and see how far one can see.", "Hold the latch: stay by the Door and help the clan's last stragglers on with their luggage.",
                        new String[]{"* You keep the hall. The hum sinks back to its old steady note. The tea is poured at midnight by the head of the clan, who is you, in a tin cup, with one sugar. Joren, who stayed, digs a hole in the north meadow and plants an apple tree in it. It is the happiest day of his life.", "@Joren| It's a good hole."},
                        new String[]{"* A long white shape comes down from the glass. It is as big as a hill and as soft as a cloud, and it has an eye like a pond. It lowers a wing. You climb on. The whole of the valley falls away beneath you. The last thing you see, as the dome closes behind, is a banner on a pole, standing on one leg, seeing far."},
                        new String[]{"* You sit by the Door for a season. Every few days a straggler arrives, a cousin, a goat, a schoolmaster's cat, and you help them with their bags. Tamsin comes through from the other side now and then with a message and a pie, and goes back again. You have the oddest sense, as you sit there, of being exactly where you were put."}))
                .outro(
                        "* On the roof of the hall, the next morning, there is a single silver feather as long as a spear. Someone has stuck it upright in the thatch like a flag. The children say it grew. Nobody asks them to prove it.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* The apple tree in the north meadow gives sour apples. They make the best tea you have ever had.",
                "?latch=2|* The hall is empty, and swept, and warm. A banner hangs in the window. The tea is on the stove.",
                "?latch=3|* Tamsin comes back through the Door on Thursdays with a pie, and has never once been late.");
    }

    // ------------------------------------------------------------------ the Wandering Immortal's Disciple
    private static void disciple(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* You are old, or what passes for it in your trade. The red gourd has a name on it again, in a hand you do not recognise, and nobody can tell you when it was added.",
                "* Brook comes up the road in a hat that has seen weather. She has a small pack, a smaller dog, and a message. She says it is from the quiet man in the very small garden.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "The Gourd Gets a Name").years(2)
                .intro(
                        "* You turn the gourd over on the step of the inn. The scratch under the stopper has changed. It says, in tiny neat strokes, a name. It is not the one it used to be, and not the one you feared, and not yours. It is a word you have heard in a song. It is a word for somewhere you have been, and are going.")
                .wait(8)
                .outro(
                        "@Brook| It's the Door. It's the name of the Door. He wrote it. He says it's time. He says he's not in any hurry, but the cup is going round, and it'd be rude to be the last one to pass it.",
                        "* She sits down on the step beside you. She takes the gourd and looks at the name for ages. She says: 'I've been carrying a message for you for six years. I've never known how to say it.' She says: 'He says, thank you for taking so long. He says it was the right amount of time.'",
                        "* The old inn is quiet. A sparrow lands on the rail, and looks at the gourd, and flies away. The wine in it moves a little, though nobody has touched it.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "The Cup Goes Round").gap(26)
                .intro(
                        "* The line at the Garrison is the longest you have seen it, a quiet river of people in boots and lamps. Halfway down the file, on a camp stool, with a bottle between his feet, sits an old man in a ragged coat, feeling the air with one hand. He does not turn his head. He says, without any preamble: 'There you are.'")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* He passes you a cup. It is a tin one. The wine in it is the colour of tea. You drink. It tastes of somewhere that has not been built yet, and is nearly finished. He says he has been waiting at the gate for ages, but that it has not been boring, because there is always someone to pass a cup to. He says that is the whole of what he knows.",
                        "@Master| I taught you three moves. I never taught you the fourth. It isn't a move. It's a place to put your feet.",
                        "* He gets up, a little stiffly, and puts the bottle under his arm. He shakes out the stool. He goes in at the gate without looking back, whistling, a little flat. At the sill he turns and lifts the cup, once, at you, in a gesture that has no name. Then he is just another back in the line.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "The Empty Cup").gap(26)
                .intro(
                        "* The Master of the Palace is sitting on the flags beside the Seat, in his plain apron, with a bowl of soup. He is not nodding off. He is awake in a way you have not seen him. He looks up as you come in, and smiles, a wide open smile, and holds out a hand. In it there is a cup.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The Master has offered you a cup. The Door is under the Seat. The cup is empty.",
                        "Stay: take the cup, and keep it for the next one who comes by.", "Go: drink what is not in it, and walk through.", "Hold the latch: sit with him at the Seat, and pass the cup to everyone who goes in.",
                        new String[]{"* You take it. It is a plain clay cup with a crack in it, mended with gold. You hold it in both hands. You tell him you will keep it in the kitchen, on the shelf, with the others. He says that is all he ever wanted: a shelf, and a place where it would be used.", "@???| (very quietly) I think I will take a nap."},
                        new String[]{"* You raise it to your lips. It is empty. You drink what is in it. It is the whole of the road, the inn and the ferry and the hat, and a winter in the north. A girl of ten with a leaf in her hand, and it goes down like warm rain. You hand it back. He is crying and laughing. He says: 'That's the fourth.'"},
                        new String[]{"* You sit with him all night and pass the cup. The people come in ones and twos. Each takes it, and looks into it, and drinks nothing, and hands it back with a nod. By morning you have taught the Master of the Palace to sit still. It is the one thing you were ever good at."}))
                .outro(
                        "* The cup is on the flags in front of the Seat in the morning. The gourd is beside it. A thin line of wine runs from one to the other, across the stone. Nobody mops it. It smells of somewhere else, and it smells like a place you will go. The cook leaves a blanket on the Master's knees, and goes quietly away.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* A clay cup with a crack mended in gold stands on the kitchen shelf, between the soup and the spoons. It is the only cup in the Palace that is ever used.",
                "?latch=2|* The cup is empty. The road goes on. It is still not finished.",
                "?latch=3|* Two figures sit by the Empty Seat at night, one in an apron, one in a ragged coat. They are said to be passing a cup. They are said to be very good company.");
    }

    // ------------------------------------------------------------------ the Cook Who Fed the Mountain
    private static void chef(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The Long Table has a door and a board and a very large dog. The bottom of the pot is older than anyone who eats from it. Ox has gone grey. Cassia's chair has a cushion on it that nobody mentions.",
                "* Rue writes from the coast, in a hand like a row of knives. She says she is a hundred and one, that the sea is overrated, and that she has been asked to a dinner.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "Rue's Invitation").years(2)
                .intro(
                        "* The invitation is on a card the size of a plate. It has a gold edge and a blot of soup in one corner. It is in Rue's writing. It says: Dinner at the Garrison, last night of the harvest. Bring the pot. Bring the bottom of it. There will be a number of guests, none of whom will be able to pay, and all of whom must be fed. Nobody means nobody. See me.")
                .wait(8)
                .outro(
                        "* You pack the pot on the cart with Ox, who is sixty and as broad as a door. You put the cookbook in the apron pocket, with the pencil. You put in a ham, a sack of onions, a crock of dumplings and a very small barrel of something yellow. Cassia comes out to the cart with a basket. {cassia.He} says nothing at all. {cassia.He} stows the basket under the seat and climbs up, and takes the reins.",
                        "@Ox| I'll miss the door. I've hung it eleven times. I think it likes me.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "The Last Dinner").gap(26)
                .intro(
                        "* Rue is waiting at the Garrison in a wheeled chair, a hundred and one years old, as tall as a stove, in an apron. Her hands are folded on the head of a stick. Her eyes are as bright as a jay's. She is sitting at the end of a very long trestle table set up in the open on the plain, in the rain, laid for two thousand.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}, with the pot.")
                .outro(
                        "* You cook for the line. It takes all night. Ox ladles. Cassia carries. Brannock's grandson, a large silent boy, washes up. The soup is the old brown soup with the single dumpling in it, and Rue watches every bowl go out with a critical eye. The soldiers eat standing, in the rain, and each of them says the same thing, one after another, as though giving evidence: I can taste it.",
                        "@Rue| That's it. That's the whole secret. It's only what's in the room.",
                        "* When the last bowl has gone she asks for the bottom of the pot. You scrape it into a cup. She drinks. She sits for a time with her eyes shut. She says: 'It's got me in it now. Good.' She says she is going in. She says she has been meaning to for a decade. She says: 'Wash up. They'll want us to have done it properly.'")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "A Table at the Seat").gap(26)
                .intro(
                        "* The Palace gives you the Under-Kitchen for the night. The man in the apron is there, with a spoon, and his terrible anxious smile. He asks if you would be so kind as to cook for the Seat. He says it has never had a meal. He says he has put out a chair.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The chair is laid with a bowl and a spoon, in front of the Empty Seat. The Door is under it.",
                        "Stay: cook the Seat's supper every night, and keep the Under-Kitchen's pot going.", "Go: cook one last meal, sit down in the chair, and eat it.", "Hold the latch: set the table by the Door, and feed everyone who goes in.",
                        new String[]{"* You cook for the Seat. It takes you a week to decide what a chair might like. You settle on a clear broth, a hard roll and a dish of plums. You set it out at dusk. In the morning the bowl is empty, and clean, and on the rim, in what looks like a drop of gravy, there is a mark that might be a thumbprint.", "@???| It likes plums. I did not know it was allowed."},
                        new String[]{"* You cook it with care. It is the best meal you have ever made. You sit in the chair, in the dusk, in front of the Seat, and eat it slowly, to the last crumb. It tastes of everyone you ever fed. When you have finished, you wipe your mouth, and fold your napkin, and stand. It is time. It has been a very good dinner."},
                        new String[]{"* You set the table by the Door, a long trestle with a cloth on it, and a pot, and a ladle. You feed everyone who comes. You do it for a long season. They sit on the flags and eat their soup and say nothing. When they are finished they stand, and thank you, and go down the stair. You say: nobody leaves hungry. Nobody means nobody."}))
                .outro(
                        "* The man in the apron cleans the bowl himself. He dries it on a cloth, and puts it on the shelf with the others, and stands looking at it for a spell. He says: 'I think I have learned something. I think it is that a meal is not what is in it. It is who is at the table.' He says this with the air of a man announcing a law of nature.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* There is a chair in front of the Empty Seat with a bowl set before it every evening. It is always empty by morning. The Wicks have stopped being afraid of it.",
                "?latch=2|* The pot hangs on its hook at the Long Table, scoured. The bottom of it is a hundred and eleven years old. Ox feeds it every Tuesday.",
                "?latch=3|* A trestle stands by the Door, and nobody who has passed it has gone hungry. Over it hangs a board, in black, in a hand you know. NOBODY IS TURNED AWAY.");
    }

    // ------------------------------------------------------------------ the Apothecary's Second Cup
    private static void poisoner(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The shop in Salt Lane has three signs and a cat on the scales. The harbour has two apothecaries and one inspector, and the inspector eats her lunch in your front room. The card in the window is faded at the edges. You have not changed a word of it.",
                "* The foxglove by the back door is in flower in January. It is a bad sign, or a good one. Nobody is sure which.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "The Foxglove Blooms").years(2)
                .intro(
                        "* Madam Verrel's foxglove is a legend in Brinewick. It grows in a half pot by the back door and blooms once in a lifetime. The net-mender says it flowered the week she was born. It flowers now, in the snow, with a tall spike of purple bells, in a pot that is full of cold black earth. You find it on a Tuesday morning when you go out to empty the tea.")
                .wait(8)
                .outro(
                        "* The petals are warm. A small card is tucked into the pot, on the stake, in a hand like a row of knitting. It says: It was a very good shop. Under that, smaller: The third question is for the one who asks. Under that: I had to go. Look after the cat.",
                        "?!dead.calla|@Calla| (reading over your shoulder, with a cup of coffee and her badge in her pocket) She died in her sleep. At the Tower, on Sunday. She had a ball of wool. The warder says the last row was perfectly plain.",
                        "* The cat comes out of the shop and sits in front of the foxglove with its tail wrapped round its feet. It looks at the flower for ages. Then it turns and walks back in, and sits on the scales.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "Three Questions at the Gate").gap(26)
                .intro(
                        "* You go to the Garrison with a bag of jars. You have no idea why. You have said you are going to look at the line, and to see whether anyone needs a cure, and you believe none of it. You walk the whole file, in the rain, from end to end, with the bag clinking. Every few yards you stop, and ask.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* They are all afraid, a little. It is under the lamp-light and the steadiness, the soldiers' faces, a small bright animal fear. You ask the three questions. Who is it for. What do they think it does. What happens if they are wrong. You do not sell them anything; you give them a dose of something plain, for the nerves: a tisane of chamomile and honey. It is, you reflect, the best work you have ever done.",
                        "@Soldier| (a young one, taking a cup) What's in it?",
                        "@You| Nothing that will hurt.",
                        "@Soldier| That's the nicest thing anyone's said to me all week.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "The Honest Dose").gap(26)
                .intro(
                        "* You carry the last of the foxglove to the Empty Seat in a jar. It is not a poison. It is not a cure. It is both. It is exactly as much of each as a person needs, which is the thing Madam Verrel spent her life trying to say, and which, you now understand, could not be said, only measured. You put the jar on the flags at the foot of the chair.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The jar is on the flags. The Door is under the Seat. The third question is yours.",
                        "Stay: keep the shop and the cat, and ask the questions every day.", "Go: take the dose yourself, in the exact amount, and walk through.", "Hold the latch: sit by the Door with a tray of doses, and measure out peace for each.",
                        new String[]{"* You go home. The shop is warm. You hang up your coat and put the foxglove in the window, and the cat comes down off the scales to look. On Monday morning the bell rings, and a man comes in with a cough, and you ask him who it is for. He looks puzzled. He says it is for him. You tell him that is the best answer you have had all year.", "@Calla| (from the doorway) I'll put the kettle on. I'll put it on early. It's going to be a long one."},
                        new String[]{"* You open the jar. You measure the dose by eye, as Verrel taught you, without a scale, and you drink it. It is cold and bitter, and sweet at the end. The flags beneath you grow gently warm. You are not afraid. You have never been afraid of this. It is the one thing in the world that you know exactly how much of."},
                        new String[]{"* You sit at the Seat with a tray and a lamp. All night they come. You ask them each the third question. Most don't know. You give them something plain. It is not a cure. It is not a poison. It is a pause, a warm cup, a few quiet minutes in a stone hall. You do it for a hundred nights, in a manner of speaking, and the cat comes to sit on your feet."}))
                .outro(
                        "* The foxglove in the window of the shop blooms every year now, in the month it ought. The net-mender says it is the only thing in the lane that has never once told a lie. The card beside it has a fourth line, in your hand, in pencil: And who will carry the cup? Beneath it, in a new ink, in a smaller hand, someone has written: I will.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* The shop in Salt Lane is open from eight to six. The Chief Inspector has a stool by the stove. The third question is asked, daily, of everyone.",
                "?latch=2|* A jar stands at the foot of the Empty Seat with a dose measured out in it. It has been there for years. It has never been touched.",
                "?latch=3|* A tray of warm cups is laid by the Door each night, and a lamp, and a stool. The Wicks say it is the least frightening place in the Palace.");
    }
}
