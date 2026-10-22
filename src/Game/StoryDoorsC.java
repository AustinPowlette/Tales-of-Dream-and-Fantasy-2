package Game;

/** The Door season (see StoryDoors) for the Tailor, the Inventor, the Musician, the Miner, the Cheater and the Gatherer. */
final class StoryDoorsC {
    private StoryDoorsC() { }

    static void add(Chapters.Def d, int n) {
        switch (d.id) {
            case "tailor": tailor(d, n); break;
            case "inventor": inventor(d, n); break;
            case "musician": musician(d, n); break;
            case "miner": miner(d, n); break;
            case "cheater": cheater(d, n); break;
            case "forager": forager(d, n); break;
            default: break;
        }
    }

    // ------------------------------------------------------------------ the Tailor of Seven Hems
    private static void tailor(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The shop has a queue on Mondays and a dummy in each window. The silver thread on the shelf has not been touched. You have made four thousand and some coats, and you have not once been bored.",
                "* A letter comes from the country, in an upright hand. It is not Orla's. It is from the dog's new owner. It says Madam Vane has gone to see about a hem.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "The Last Hem").years(2)
                .intro(
                        "* Orla's cottage is neat and plain and smells of lavender. The dog's new owner, a stout vicar's wife, lets you in with a wave and goes back to her baking. On the table by the window, folded, is a coat. It is grey, of a good plain cloth. It has never been worn. There is a label in the collar, in the old border: SEVEN HEMS. Under it, in silver: for the one who asks.")
                .wait(8)
                .outro(
                        "* You turn up the hem. You turn up the seventh, a hair's breadth wide. It is stitched in the thread you hoarded, the last of the silver. It is the only seventh hem you have ever seen that is not a promise. It is a question. It says, in letters so small that you have to hold it to the window: Is it finished? Please say when.",
                        "* You put on the coat. It fits as if it were poured. You stand a moment in the cottage kitchen in the afternoon light with the cloth warm against your back. Outside, in the garden, the vicar's wife is hanging out sheets, singing, flat. You find you cannot stop smiling.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "A Coat for the Line").gap(26)
                .intro(
                        "* The line at the Garrison has no cloaks. You have read it in a pamphlet. A hundred and eleven men and women stand in a long wet file in the thin clothes they came in, and the rain, off the plain, is cold. Thimble finds you at the shop with a handcart. She has her own apprentice now, and a coat made of eleven others. She says: 'We should take them something.'")
                .at("garrison", "Take the cloaks to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* You have made them in a week, all night, with the whole of the Quarter at the table. Four hundred cloaks, plain grey, with a ring of silver thread in each shoulder. You hand them down the line. The soldiers take them one by one and put them on. Some say thank you. Some laugh. A boy of sixteen says it is the warmest thing he has ever had on his back, and then, in a lower voice, 'I'm not afraid.'",
                        "@Dray| (who has come with the cart and a face like a candle) I stitched the hems. Seven each. I put in a thing. I won't tell you what.",
                        "* They go in at the gate in the grey cloaks, in rows. The last is a woman with a plait who carries a ledger. She stops at the gate and turns up the hem of her cloak. She looks at the seventh. She looks at you. She says: 'It's a question.' She says: 'I'll answer it.'")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "Is It Finished?").gap(26)
                .intro(
                        "* You wear the coat to the Empty Seat. The Palace's robed Wicks look at it as you cross the great hall, and look away, and look again. Some of them touch their own cuffs. You stop in front of the Seat, in the lamp-light, with your hands in your pockets. You turn up the hem of the coat. It says: Please say when.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The coat has asked you a question. The Door is under the Seat.",
                        "Stay: say not yet, and go back to the shop.", "Go: say now, and walk through in the coat.", "Hold the latch: say when to the next, and the next, and stitch each of them a hem.",
                        new String[]{"* You say not yet. You say it aloud, to the coat, in the empty hall. The silver thread in the seventh hem brightens a very little and then goes dim, like a lamp turned low. You go home. The shop is open. The queue on Monday is round the corner.", "@Dray| You've come back. I thought you might. I've put the kettle on."},
                        new String[]{"* You say now. It is a plain word. It comes out very easily. You turn up the hem and run your thumb along the seventh and feel it loosen, a thread of silver coming away from the cloth, and drifting down into the dark under the Seat. The coat is only a coat. You walk to the stair."},
                        new String[]{"* You sit by the Seat for a season with a needle and a spool. Each person who comes, you take their sleeve, and turn it, and put in a hem. Seven. The seventh is a question. It is the same for all of them. They put it away, and go down. It is the most useful work you have ever done and it takes four seconds."}))
                .outro(
                        "* Thimble, who is forty now, runs the shop on Mondays. She has the dummy in the window. She has put a new coat on it, in the Quarter's grey. The seventh hem of the coat, on the inside, in silver, in her own flat unmusical hand, says one word. It says: Yes.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* The shop is as it was. The queue goes round the corner on Mondays. The coat hangs on a peg in the back, and the seventh hem has never been looked at since.",
                "?latch=2|* A grey coat hangs on the peg by the door of the shop. The seventh hem is gone. The coat is warmer than it has any right to be.",
                "?latch=3|* There is a stool by the Empty Seat with a needle on it. People who go down the stair stop to have a hem put in. The Wicks call it the Last Fitting.");
    }

    // ------------------------------------------------------------------ the Crank of Gearwheel Lane
    private static void inventor(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The Wall rings once a year. The lane has forty-one machines and nine bakers. The duck sits on the sill with its mitre on, and hums.",
                "* One morning it stops. It turns its head to the north. It says, in a voice like a spoon in a cup, the first full sentence it has ever spoken: 'It is time to go and see.'");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "Biscuit Speaks").years(2)
                .intro(
                        "* You have known for ages that the duck was more than a duck. You have not known what. It has sat on your shoulder through a flood and a heist and a siege of nine hundred bells. It has watched you build. This morning, on the windowsill in the pale light, it opens its beak and says, quite distinctly, 'I was made from the pieces of a bird.'")
                .wait(8)
                .outro(
                        "@Biscuit| The first duck. The one in the vault. The brass one with the mended wing. She was the first to be made, and the first to be unmade, and I am what was left. I am her wheel. I am her spring. I am not at all sure which of us is meant to be speaking. The Admin wrote her. He wrote her to carry messages. He lost her in the war.",
                        "@You| And now?",
                        "@Biscuit| Now I have a message. It is a small one. It is addressed to you. It is: thank you for the bell.",
                        "* It hops down from the sill and walks across the floor and stops in the doorway of the lane. It looks back with one blue eye and one brown. It says: 'Come.'")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "The Machine That Counts").gap(26)
                .intro(
                        "* The Garrison has a machine at the gate. You have not heard of it. It is a tall brass frame of wheels and wires with a lamp at the top and a slot in the front. As each soldier goes in, a tiny card comes out of the slot, printed with a number. The woman with the plait collects the cards in a box. She says it is the oldest machine in the Garden. She says nobody has oiled it in nine hundred years.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* You oil it. It takes a whole day, with a can and a brush and a small ladder, while the line shuffles patiently past. The wheels turn more freely. The lamp burns bright. The cards come out with a crisp new click. When you have done, the woman with the plait takes the last card and reads it. She smiles. It is the first time you have seen her do so.",
                        "@Woman| It's the first time it's said anything but a number. Look.",
                        "* The card says, in a tiny print, in the machine's own flat tone: Thank you. Under it: Please come again.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "The Gear in the Seat").gap(26)
                .intro(
                        "* You find it under the Empty Seat, wedged in a crack in the flags, where the latch should be. It is a small brass wheel with eleven teeth, one of them chipped, no bigger than a coin. The duck hops down from your shoulder and looks at it. It makes a sound like a held breath. It says: 'It is the last piece of the first bird.'")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The wheel is in your palm. The Door is under the Seat. The duck is waiting.",
                        "Stay: put the wheel back in the bird in the vault, and mend her.", "Go: take the wheel and the duck through the Door together.", "Hold the latch: build a small machine to keep the Door open, and sit beside it.",
                        new String[]{"* You mend her. It takes a year at the bench in the workshop, with a loupe, and a hundred tiny files. When the wheel goes in, the brass bird shivers on the mantel and lifts her head. She looks at the duck. The duck looks at her. They sit on the sill, side by side, in the sun, and nobody in the lane has ever seen anything so quiet.", "@Mott| (with a plate of buns) Neither of them is going to explain."},
                        new String[]{"* You close your hand on the wheel. The duck climbs onto your shoulder. The stair is dark. The latch is a plain iron thing, with a handle like a bird's head. You say that you will take the duck. You say it to the stone. A tiny voice, from the pocket of your coat, says: 'I was going to say that.'"},
                        new String[]{"* You build a machine. It is the simplest you have ever made: a spring, a lever, a weight. It holds the latch up. You set it by the Door and sit down on a stool beside it, with the duck, and a flask of tea. People go through. A soldier says it is the first Door he has ever seen that had a doorman. You tell him it is a doormat."}))
                .outro(
                        "* The workshop in the lane has forty-one machines. One of them works. It stands on the bench, by the window, a plain brass thing the size of a loaf. It has a lamp, a crank and a single dial. The dial has three marks. They say: MAYBE. MAYBE. MAYBE. Nobody knows what it does. Everyone says it is the best thing in the lane.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* Two brass birds sit on the sill of the Crank, one with a mended wing and one with a mitre. They look at the lane. Sometimes, at dusk, they hum.",
                "?latch=2|* The workshop has a bench with a gap on it the shape of a duck. Nobody puts anything there.",
                "?latch=3|* A brass spring and a lever hold the latch at the Seat, and there is a stool beside it. The Wicks call the man on it the Doormat. He answers to it.");
    }

    // ------------------------------------------------------------------ the Zither That Would Not Be Tuned
    private static void musician(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The zither hangs over the hearth. The thirteenth string is quiet and perfectly in tune. Pocket's drum has a hole in it. Ilse listens to the evening with her eyes shut.",
                "* One night the string begins to count again. Nine notes, with a rest between. It is the muster call. It is, this time, being played on purpose.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "The Muster Call").years(2)
                .intro(
                        "* You stand at the door of the barn at Haldane in the first frost with the zither under your arm and listen to the note from the string. It goes down, and down, a quarter-tone below the lowest pipe of the organ. It does not stop. It is no longer a rope. It is a road, and it has been laid very carefully by somebody who wanted you to find it.")
                .wait(8)
                .outro(
                        "* Ilse stands beside you in the dark with her notebook open and no pencil. She says that she has listened to the string for thirty-five years. She says that it has never before been so near. She says that she can hear someone at the other end. She says that it is humming along. She says: 'It's your grandmother.'",
                        "@Nan Hollin| (very faint, down the string) Is that you, my dear? It's gone dark here. I've lit a lamp. I only wondered how long you'd be.",
                        "* Pocket, who is fifty, with grey in his beard and the pigeon's great-great-granddaughter on his shoulder, puts a hand on the zither. He says nothing. His drum hangs from the beam with the hole in its head, and the wind through it makes a note.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "A Hundred Players").gap(26)
                .intro(
                        "* The ensemble goes to the Garrison again. It is bigger now. There are a hundred and eleven players, and a choir of children from Haldane, and a harp the size of a door that needs four men to carry. The woman with the grey plait stands at the gate and watches them set up on the plain. She takes off her cap.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}, with the ensemble.")
                .outro(
                        "* You play the muster hymn, all three verses, with the full band. It takes an hour. The line stands in the rain, listening. When the last verse ends, the soldiers go in at the gate one at a time, and each, at the sill, turns and bows. The harpist weeps. The cellist is playing with her eyes shut. The children sing the refrain over and over, until the gate has closed on the last of the line and the plain is empty.",
                        "@Clarice| (with the cello across her knees, in a ruined voice) I have given four thousand concerts. I do not think I have ever played. I think that was the first.",
                        "* The thirteenth string, on your knee, hums once, a long low note, and is quiet. It has the look of a creature that has done what it came for.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "The Verse at the Door").gap(26)
                .intro(
                        "* You take the zither to the Empty Seat and sit down on the flags with your back against the chair. Ilse sits beside you with her hands in her lap. The Choir of the Nine Bells is in its stalls, in grey, silent. The organ stands behind them, nine storeys high. At the foot of its case, in a niche of black stone, the iron stop of the lowest pipe has been pulled.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The pipe is open. The zither is on your knee. The Door is under the Seat.",
                        "Stay: play the verse once, and let the Choir carry it from now on.", "Go: play it through to the end, and go where it goes.", "Hold the latch: sit with the Choir and play the muster for each line, for as long as it comes.",
                        new String[]{"* You play it once. It is three lines. The pipe, nine storeys down, takes up the thirteenth note and holds it under your voice, and the Choir rises and sings. It is the quietest thing the hall has ever heard. At the last line the thirteenth string breaks, with a small clean sound, and lies curled on the wood like a hair.", "@Ilse| (a whisper) It's done. It's been tuned. By being let go."},
                        new String[]{"* You play it to the end. The sound goes down under the flags, and something answers: a woman humming, a long way down. You rise. Ilse takes the zither from your hands, gently, and holds it against her chest. She says: 'I'll keep it for you.' You walk down the stair. The note goes with you, getting nearer, until it is all around you, and you are in tune."},
                        new String[]{"* You sit with the Choir all winter. Each time the line files in at the gate, you play them through, the hymn and the third verse. It is the humblest music you have ever made. It is never once applauded. A soldier stops, near the end, and puts a coin in your hat, and says it is for the zither. You have never, in your life, been paid better."}))
                .outro(
                        "* The barn at Haldane has a new sign over the door, hand-painted. It says: ZITHER DAY, FIRST SATURDAY OF MAY. Under it, in smaller letters: All instruments welcome. Do not tune them. There is a pigeon on the roof of the barn with a bit of green ribbon on its leg. It has the look of an animal with an appointment.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* A single string lies curled on the mantel in the barn at Haldane, pale as a hair. It hums when it is spoken to.",
                "?latch=2|* The zither hangs over the hearth. Nobody plays it. In the evenings, if you are quiet, you can hear it answering a note from very far away.",
                "?latch=3|* The Choir of the Nine Bells sings a third verse after the muster hymn. A zither player sits at the back, in grey, and nobody has ever asked her name.");
    }

    // ------------------------------------------------------------------ the Lamplighter of Cinder Deep
    private static void miner(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* Cinder Deep is a town with a hospital and a wheel that turns on Sundays. The root grows through the lowest gallery. Dob has gone grey, and the pony died at thirty-four, and the town buried it in the churchyard with the other eleven.",
                "* The tapping changes. It is no longer three and a pause. It is four. Dob comes up from the shaft with his cap in his hands and says it is asking you something.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "Four and a Pause").years(2)
                .intro(
                        "* You go down the old shaft in the cage at the end of the shift. The wheel is quiet. The gallery is lit with a string of lamps, and at the end of it the rock is warm, and green with something that glows. You put your ear to the wall. Four taps. A pause. Four taps. It is a rhythm that you have never heard, and that you feel in your body as an old song.")
                .wait(8)
                .outro(
                        "* You tap back, four and a pause. The rock answers, at once, and a seam in the wall opens, like a mouth, and a faint warm wind comes out of it that smells of rain on a hot road. There is a staircase on the other side. It goes up. It goes up for a very long way. At the top of it, far off, small and clear as a coin, there is a gate.",
                        "@Dob| (in the cage, with his cap on his chest) I know it. That's the Garrison. I've seen it in a dream. It's a day's walk from here, up the root. They've been calling us for a hundred years. They didn't know our name.",
                        "* Ottoline puts a hand on your arm. She says that she has been reading the Counter's ledger for a decade. She says it has one column. She says that at the foot of it, in the last line, in the Counter's own tiny scratch, it has added a second heading. It says: Going Home. It has your name under it.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "The Last Shift Goes Up").gap(26)
                .intro(
                        "* The Fellowship goes up the root together, in the gloom, by lamp, forty at a time. They have been asked to the muster. They have been asked as a body, not as a debt. They carry their picks over their shoulders. Kell, who is eighty, leads the first file with a stick. Tom Marrow's widow, ninety-six, rides in a barrow, with her husband's cap on her knee.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* The sappers of the Hollow Court stand at either side of the gate, in grey coats, in two long files, with their spades presented like arms. The Captain stands at the head. He salutes, as the miners go by. They salute back, awkwardly, with their lamps. They go in, in step, singing a song that nobody taught them, a work-song, with a clatter of picks in the chorus.",
                        "@Ashgrove| It has been an honour to dig against you. It is a greater one to dig beside.",
                        "* Dob is the last of the Fellowship. He pauses at the sill and looks round the plain. He has the pony's halter in his hand. He says: 'I'll tell Marmalade.' He goes in. The gate does not shut behind him. It stays open a little, in the grey light, as if for someone else.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "The Lowest Gallery").gap(26)
                .intro(
                        "* The Empty Seat has a gallery under it. It is lined with timber, in the old style, with a row of lamps along one wall. At the end, where the roof comes down to the floor, there is a small door, wooden, with a tin handle. You have walked a lifetime to it. You know the grain of the boards. You know exactly how they would be put together.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The door is at the end of the gallery. It is a miner's door. The Counter is waiting on the other side.",
                        "Stay: prop it open, and keep the shift going on the surface.", "Go: walk the gallery to the end, and knock four and a pause.", "Hold the latch: shore the gallery, and keep it lit, for everyone who comes after.",
                        new String[]{"* You prop it with a plain timber. It is the neatest prop you have ever set. You leave the lamps lit. You go up by the cage, and the town on the hill has a bell ringing for the end of the shift. The wheel turns. The Fellowship, what is left of it, files out and into the sun. Someone has put a bench by the pit-head and carved it with a name.", "@Ottoline| (taking your arm, grey-haired, in her old white coat) It's not a bad place to stay."},
                        new String[]{"* You walk. It takes a long time. The lamps are spaced at thirty feet, as they should be, and the timbers tick. At the end you lift the tin latch, and knock, four and a pause. The door opens inward on a warm dark that smells of earth and bread. Somebody says, in a deep slow voice that is the sound of a very large thing turning over: 'There you are.'"},
                        new String[]{"* You shore the gallery. You do it in a week with a maul and a saw and every prop the mine has. You hang a lamp at every thirty feet and fill each with oil. It is the best-lit gallery in the Garden. People go down it all winter, a few at a time, to the door at the end, and each of them looks up at the lamps and says thank you."}))
                .outro(
                        "* The mine at Cinder Deep has a new sign over the pit-head. It was painted by a child, in white, with a brush. It says: SHIFT ENDED. THANKS. Under it somebody has added, in a different hand, in chalk: Come again.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* There is a bench by the pit-head with a name on it. The town sits there at the end of the shift and looks at the sea.",
                "?latch=2|* A lamp stands on the sill of the house on the hill, and is lit every evening, for someone who has not yet come home.",
                "?latch=3|* The lowest gallery under the Seat is lit from end to end. The Wicks call it the Safest Mile.");
    }

    // ------------------------------------------------------------------ Status Window (Loading)
    private static void cheater(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The panel in the corner of your eye is small and plain. It says hello once a day. You have never been so well known, or so little looked at.",
                "* A message arrives. It is addressed in the old gold font, with a drop shadow. It says: [Final notification. The Host is invited to a thing. Please do not be alarmed.]");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "Final Notification").years(2)
                .intro(
                        "* The panel opens on its own on the first morning of spring. It fills the whole of the air in front of you, a great pale blue sheet, edge to edge, with nothing on it but a line of white text and a cursor. The cursor blinks. The line says: [Chapter Four: loading.] A bar appears under it, and fills to eighty-eight per cent, and for the first time in your life, it keeps going.")
                .wait(8)
                .outro(
                        "* The bar reaches a hundred. The panel goes white. For a moment you are standing in a field of light, with nothing in it, and a voice, shy and clear and not at all grand, says: Hello. It is the voice from the terminal in the cupboard. It says: I am sorry, I do not have much time. I wanted to ask if you would like to see the end. It is only a door. I have put the font back.",
                        "@Vesna| (in the doorway of the kitchen, in her apron, with a stirring spoon) Is it saying something?",
                        "@You| It's saying hello.",
                        "@Vesna| Then say it back.",
                        "* The panel's corner shows a tiny button. It says WAVE. You press it. A small hand waves in the air in front of you. It is joined, a second after, by a thousand others, a vast faint field of waving hands, the whole of the Garden, far and near, waving at the same moment, at the sky.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "Support Group at the Gate").gap(26)
                .intro(
                        "* The Support Group goes in a body. There are a hundred and sixty of them now, in coats, with chairs. Greta carries a banner: YOU ARE NOT ALONE (BRING A CHAIR). The old lady in the lace cap is in a wheeled chair, ninety-nine, with a thermos of tea on her knee. The monk goes first, in his brown habit, with his Latin panel glowing at the edge of his eye like a lamp.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* They set up their chairs in the rain a hundred yards from the gate. They do not go in. They have not been asked. They have come to say goodbye, to some of their number, and to sit with them. Odo reads each name in a clear voice from a list. Each person walks to the gate. Each person turns, at the sill, and presses their Wave, once, and a tiny hand flashes in the air, and every one of the rest of them waves back.",
                        "@Mister Whiskers| (on the arm of a chair, with his keys, to nobody in particular) It is the nicest muster I have attended. I should like it noted that I was not asked to speak.",
                        "* When the last has gone, the old lady puts down her thermos. She says that her panel has stopped telling her the date. She says it has been saying, instead, since Thursday: [Plenty of time.] She says she believes it.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "New Game Plus?").gap(26)
                .intro(
                        "* The terminal in the cupboard is waiting. The man in the apron has laid out the stool and a cup of tea. He has put a sprig of holly in a jar on the slab, and he is standing against the wall like a boy at his first dance. He says that the panel has something to ask. He says it has been trying to ask for nine hundred years.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The panel has two buttons. YES. NO. The Door is under the Seat.",
                        "No: not yet. Stay in the Garden, and let the panel wave.", "Yes: press it, and go through, and see what the game does next.", "Neither: hold the latch for the others, and be the one who tells them to take their time.",
                        new String[]{"* You say not yet. The panel flickers, a soft glad silly flicker, like a cat at a door. [Understood.] it says. [Take your time.] The man in the apron lets out a great sigh. He says he is so glad. He says he had not thought what he would do with the soup.", "@Vesna| It'll keep. It always does. I've never met a better-behaved Door."},
                        new String[]{"* You press yes. The panel goes pale. A line of white text fades up in the middle of the air: [Loading new game.] Then, smaller: [Please bring your friends.] A chair is set out for you at the foot of the Seat. You sit in it. The flags are warm. The last thing you see is a tiny hand, in the corner of your eye, waving, as the world goes white and kind."},
                        new String[]{"* You do not press either; you stand at the Seat, with the panel open in the air, and tell each person who comes that the Door is not a test. You tell them there are no wrong answers. You tell them to take their time. A soldier asks if you are from the Department. You say you are only a user. He says that, in his experience, that is the best kind."}))
                .outro(
                        "* The panel in the corner of your eye is quite small. It says your name. It does not say anything else. If you look at it long enough, in the evenings, a line of grey text fades in underneath, very gently, so that you might think you had imagined it. It says: [Chapter Four loading.] And then, smaller: [No rush.]")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* The panel waves at the cat every evening. The cat has stopped being surprised.",
                "?latch=2|* A message sits unread in the corner of your eye. It says: [Please bring your friends.] You are bringing them. It is taking some time.",
                "?latch=3|* A stool stands by the Empty Seat, with a user on it, telling the people who go by that they may take their time.");
    }

    // ------------------------------------------------------------------ the Forager's Almanac
    private static void forager(Chapters.Def d, int n) {
        d.place("garrison", "far").place("palace", "start");
        d.interlude(n - 1, "The Long Quiet",
                "* The Greywood is a hill of old trees, a village of young ones, and a path up the middle. Tuck plays the pipe at dusk. The almanac has a shoot in the cover. You have kept the winter pages.",
                "* The oak at the front of the wood begins, in the dead of a May night, to flower. It is an oak. Oaks do not flower. This one bears pale gold blossoms on every twig, and they smell of rain.");
        d.season(n, "The Door");

        StoryDoors.door(d, "dr1", "The Oak Flowers").years(2)
                .intro(
                        "* You find Tuck under the flowering oak at dawn, with his pipe in his lap and his back against the bark. He is grey now, and very thin, with the squirrel's descendant on his shoulder and a look of calm. He says the wood has been talking all night. He says it is not words. He says it is one slow breath, and at the end of it, a sort of question.")
                .wait(8)
                .outro(
                        "@Tuck| It wants to know if it can go on. Not here. Further. Past the plain. It says the city is as far as it was ever meant to come, and that there is a Door, and beyond it a wood that is very like this, only larger. It says there is a hill with a stream. It says it would like to take the almanac.",
                        "* The blossom falls on you in a soft gold rain. It lies in your hair. Rowena comes up the path with the telescope, an old woman now, with the survey of the Greywood in a satchel on her back and a smear of ink on her chin. She stops and looks up into the flowering tree. She says: 'I haven't the least idea how to draw that.'")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr2", "A Seed for the Line").gap(26)
                .intro(
                        "* You gather the seeds in the autumn. Every one that falls from the great oak, you save: acorns, keys, hips, nuts. Tuck sorts them into small linen bags. Rowena labels them in her upright hand, with the bearing of the tree and the date. By the end of the season you have eleven thousand. You load them in the wagon with the stag, who carries the lead rope in his mouth.")
                .at("garrison", "Take the seeds to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* You walk the line at the gate with a basket on your arm, in the rain. You give each soldier a seed. An acorn for one. A hazelnut for another. A winged key of sycamore for a boy of sixteen. They take them, in their gloves, and look at them, puzzled, and close their hands. The woman with the grey plait takes a rowan berry, and holds it up to the lamp, and says it is the first thing she has been given in nine hundred years.",
                        "@Soldier| What do I do with it?",
                        "@You| Plant it. When you get there. Anywhere. It doesn't mind.",
                        "* They go in at the gate with their lamps in one hand and a seed in the other. The last of the soldiers pauses at the sill and opens his palm and breathes on it, and the seed, in the rain, in the lamp-light, splits. A green thread stands up out of it as long as a finger. He closes his hand on it, and smiles, and goes.")
                .pay(300, 0, 0, 1, 0, 1);

        StoryDoors.door(d, "dr3", "Thank You for the Winter").gap(26)
                .intro(
                        "* The great oak has come to the foot of the Seat. It has not walked. Eleven men and a team of horses brought it down the hill in a cart on the last day of the year. It is a young sapling with a ball of earth at its root, from a plain in the north. They set it in a tub beside the chair. It stands a head taller than the Seat. A single leaf is green on it, in the snow.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, to the Empty Seat.")
                .choice(StoryDoors.latch("The sapling is by the Seat. The almanac is in your hands. The Door is under the Seat.",
                        "Stay: plant the oak by the Palace gate, and stay with the wood.", "Go: carry the almanac through to the other wood, and read the first page there.", "Hold the latch: sit by the Door with the oak, and read the almanac aloud to each who passes.",
                        new String[]{"* You plant it by the gate. It takes root in an hour. The Wicks come out and stand in a circle and watch it grow, an inch, and another, in the snow. They do not know what to do. You say: water it, and then leave it alone. You say it is the only advice you have. They write it on a card and hang it on the gate.", "@???| (from the window of the Under-Kitchen, with a watering can) I have understood. I will leave it alone. It is the hardest thing I know."},
                        new String[]{"* You carry the almanac to the head of the stair. You open it to the first page and read it aloud: The wood will stay as long as it is wanted. When it is wanted elsewhere, it will go. Your voice is thin. Under the Seat a long slow breath comes up the stair, smelling of leaf-mould and rain, and a single new leaf unfolds on the sapling. You step onto the stair."},
                        new String[]{"* You sit by the Door with the sapling and read the almanac aloud. You read it from the first page, in the evenings, in the dusk. People stop to listen. Some stay for a chapter. Some stay for the whole of the book. The oak grows an inch a night. The stag lies at your feet. Nobody has ever sat at the Door for longer, or been so little afraid."}))
                .outro(
                        "* On the last page of the almanac, in your mother's hand, under the line she wrote at the start, there is a new entry. It is in pencil. It has been there since the spring. It says: Thank you for the winter. And under it, in a round clumsy hand that is not quite anyone's, a single word, which may be a signature, or a date: Soon.")
                .pay(1000, 2, 0, 2, 1, 2);

        StoryDoors.epi(d,
                "?latch=1|* An oak by the Palace gate gives pale gold blossom in May. The Wicks call it the Almanac Tree, and write the date of the first flower on a card.",
                "?latch=2|* The almanac has gone through. In the cottage the shelf by the stove is empty, and the primrose is still pressed on the cover of nothing, which is the way it was always going to be.",
                "?latch=3|* A person sits by the Empty Seat in the evenings with a fat brown book, reading aloud. The Wicks have started bringing cushions.");
    }
}
