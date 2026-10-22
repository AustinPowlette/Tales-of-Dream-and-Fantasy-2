package Game;

/** The Last of the Silver Crane Clan, season 4 (2026-10-20): "The View from the Wing" (the clan's working voice, a travel comedy). The hum of Stillspring and a banner of silver thread lift the hall's roof, and the clan, its goat and its
 *  dumpling pan sail over the Garden and see its odd places from above: the Weaver's Wall, the Cistern, the Assayer's balance, the Shed, the Thin Sky and the Garrison. Choices: flight (what becomes of the flying hall).
 *  Flags: flight, poll. The Door season (StoryDoors) comes after. */
final class TaleCraneMore {
    private TaleCraneMore() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "flight", "poll");
        d.place("sky", "far");

        d.interlude(3, "The Roof Lifts",
                "* The Silver Crane Clan is not famous. It is a house of two hundred and a very large well, a school with a cat, a pan the size of a cartwheel and a banner on a pole. The tea is poured at midnight in the cellar. The hum is in the stones.",
                "* One morning in March the roof of the hall is a foot higher than it was. Joren measures it with a rule. He measures it again. He goes to find Tamsin, and when they come back it is two feet.");
        d.season(4, "The View from the Wing");

        d.chapter("lift", "The Roof Lifts").years(3)
                .major(5)
                .intro(
                        "* The roof of the great hall is a hundred and twenty feet of beam and slate, and it weighs, by Joren's calculation, about forty tons. It is rising at a rate of an inch an hour. It has a faint hum in it, a long low note, and it is lifting, very gently, off the walls like the lid of a pot. Nothing else moves. The walls stay put. The floor stays put. The table in the middle, with its forty cups, rattles but does not fall.")
                .wait(4)
                .outro(
                        "@Joren| (from the top of a ladder, with a rule, in tones of great calm) I'd say it's the vein. I'd say it's the hum, and the banner on the pole, and the slate, which is from the north meadow, and has silver in it. I'd say it's all in tune with itself. I've never seen anything so efficient.",
                        "@Tamsin| (at the foot of the ladder, with her arms folded) It's a roof. It's meant to be on the house.",
                        "@Isla| (looking up, with her needle in her mouth) It's a very good roof. I think it wants to go somewhere.",
                        "* By evening the rafters stand a yard clear of the walls, and the clouds can be seen through the gap. Marta puts the dumpling pan out on the grass in case anything falls. Old Poll comes up from the ferry with his hat in his hand, takes one look, and says he's never in his life seen the like, and that he's not going up in it.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("banner", "Isla's Great Banner").gap(8)
                .setup(TaleCraneMore::mark)
                .intro(
                        "* The roof needs steering. That is the finding of a week's committee in the cellar, over the midnight tea, with a diagram on the back of a menu. It will go where the hum goes, and the hum goes where the vein goes, and the vein is under the hall. It will rise, and rise, and drift. Isla says there is only one thing to be done. She says she needs a lot of silver thread.")
                .made(10, "Help Isla: make ten lengths of banner.")
                .outro(
                        "* It takes a month and the whole clan. They sew in the cellar, at the long table, under lamps, in a clatter of needles. Marta feeds them. Pilcher reads aloud. Edric Hexley, the Baron's son, who has become a regular, sits at the end with a ball of thread and a face like a boy at his first dinner. The banner grows. It is silver on blue, a crane standing on one leg, forty feet from beak to tail, with the hum of the vein running through its stitches like a vein of its own.",
                        "@Isla| It's got to be on the beam. It's got to catch the note. I think it's a kind of sail. I think it's going to work, and I'd like to apologise to everyone in advance.",
                        "* You carry it up the ladder in your arms like a sleeping child, and hang it from the ridge. The wind takes it. It fills. It cracks, once, like a shot, and the whole hall, with a groan and a long sigh, rises another yard.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("hole", "Joren Digs Up").gap(8)
                .intro(
                        "* A flying thing needs ballast. That is what Joren says, and Joren is the authority. The roof will not stay level without a counterweight, and the counterweight must be earth, and the earth must come from the well. He has a plan. It is a big hole. It is a hole in the sky, he says, only upside down. He asks if he may dig it in the north meadow, with a team, and send the spoil up in buckets.")
                .wait(4)
                .outro(
                        "* He digs it in eleven days. It is the deepest, cleanest, most beautiful hole he has ever made. It is forty feet across and round as a plate, with walls like polished stone, and a spiral stair cut down the side. The earth he takes up in four hundred buckets, with the help of the whole school, and packs in a great net of rope, slung from the beam. It hangs there like a bell, a heavy brown pendulum, and the hall steadies in the wind.",
                        "@Joren| (leaning on his shovel, with a face of pure joy) I've been saying for twenty years it was useful. Nobody ever listened.",
                        "@Tamsin| (grudgingly, with a hand on his shoulder) You were right. I'll put it in the minutes.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("poll", "Old Poll Is Not Going").gap(8)
                .intro(
                        "* Old Poll has a boat. It is a flat black punt with a sagging cushion, tied to a post at the foot of the garden, and he has poled it up and down the same quarter-mile of river for fifty-three years. He does not hold with heights. He does not hold with flying. He stands on the bank with his arms folded and his hat pulled down. The rest of the clan carry their bags up the ladder. Poll says that if God had meant a ferryman to go up he'd have made a ferry for it.")
                .wait(2)
                .choice(new Chapters.Choice("poll", "The whole clan is going up, and the ferryman is not.",
                        "Leave him on the bank with his boat, and tell him you will be back by supper.", "Carry the boat up, with him in it: a ferry of the air.", "Stay down with him, and let the rest go on without you.")
                        .after(
                                new String[]{"* You leave him there. He sits down in the punt with his pipe, and puts his hat over his eyes, and says he will see you at six. The hall rises behind you, with a long creak, and a cheer from the school, and a bark from the goat. As the ground falls away you look back. A small black punt, a small black figure, a puff of blue smoke. He lifts a hand without taking the hat off his face.", "@Poll| (small, distant) Mind the weather."},
                                new String[]{"* You carry the boat. It takes six, with ropes, up the ladder and across the beams. You set it on its keel in the loft among the apple crates, with water in it from a bucket. Poll gets in with the stiffness of a man stepping into a coffin. He sits on the cushion with his pole across his knees and his eyes shut. When the hall lifts he utters one short word, and keeps his eyes shut for an hour.", "@Marta| (passing with tea) Take a biscuit. It'll settle you."},
                                new String[]{"* You stay. You sit on the bank with him, in the long grass, with a flask and a loaf, and watch the roof go up. It is a marvellous sight. It goes straight up, in the morning sun, with the banner flying and the goat bleating from the rafters. Poll sits with his chin in his hand. He says nothing for minutes. Then he says: 'Go on. I'll mind the boat.'"}))
                .outro(
                        "* The hall hangs in the sky above the valley, quite still, like a sleeping bird. The sun strikes the banner. In the quiet you can hear a lark. Edric Hexley is leaning over the rail at the edge of the floor, holding on with both hands, white as the slate. He says that he is not afraid. He says it in a high voice, to the lark.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("firstflight", "First Flight").gap(4)
                .intro(
                        "* The valley falls away under you. It is the strangest feeling you have ever had: a floor, a table, a chair, a cup of tea, and nothing at all beneath your feet but a mile of blue air. The river is a thread. The hall's old burned ruins, in the north meadow, are a charred smudge, like a thumbprint on a page. The Baron's castle, on its crag, is a tooth.")
                .wait(2)
                .outro(
                        "* You circle the valley once. It takes an hour. You see every field you ever walked, the winding of the road, the shape of the lake, the little square of the school, the little round of the well. You see it all with a kind of bewilderment, as one sees a house one has lived in all one's life, from the roof. Marta cries. The goat is sick. Pilcher's cat sits on the table, with its tail wound round its feet, and regards the whole of creation with the perfect disdain of a creature who has always known it was above it.",
                        "@Edric| (leaning on the rail, a little green, in a thin voice) My father always said the clan was a joke. I should like to say that I think my father was an ass.",
                        "@Isla| (steering, with the cord of the banner in both hands and her hair blowing back) Don't say it too loudly. He's below us.",
                        "* The hall tilts, very gently, into the wind. The banner fills. The hum under your boots rises a note, sweet and strong. Ahead, over the line of the hills, a blue distance opens, a great map of country that none of you has ever seen.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("wall", "The Weaver's Wall").gap(4)
                .intro(
                        "* You come to the edge of the world in the afternoon of the third day. It is a grey band on the horizon, ruled straight as a tailor's chalk. As you draw near it grows. It is a wall of woven cloud or cloth, a hundred miles high, running from one side of the sky to the other, and where the sun strikes it, you can see the threads.")
                .wait(2)
                .outro(
                        "* You sail along it for an hour. It is the loveliest and most frightening thing you have seen. The weave is fine as lace. There are squares in it, each as large as a county, stitched with a tiny precise stitch. At the bottom, a long way down, you can see a door, brown, low, with a latch of horn. It is open a crack. Through the crack you can see a plain with wheat on it, and a road, and a river, and a hall with a banner. It is the valley you left.",
                        "@Tamsin| (very quietly, at the rail) It loops. I've read about it. I never believed it.",
                        "@Joren| (leaning out, with a plumb-line) If I dropped a stone through that door it would land on my own head.",
                        "* He drops a stone. Nobody breathes. Four minutes later, from far away, a small distant cry floats up from the valley, a woman's, indignant. It is Marta's sister. She says, in the tones of a person who has been struck by a pebble, that she would like to know who is throwing things.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("cistern", "The Mouth of the Cistern").gap(4)
                .intro(
                        "* The mist is the first thing you notice. It lies in the low wet ground of a long valley, white and thick, and from the middle of it rises a tall grey funnel, like a chimney, with a smell of old rain. The hall drifts slowly over the top. Joren peers down, with his hand shading his eyes. He says there is a hole, and a stair, and a long way down a flat black gleam of water.")
                .wait(2)
                .outro(
                        "* You hang in the air over the Cistern for most of the evening. Marta lowers a bucket on a rope. It comes up empty, and cold, and clean, and she holds it a moment, and does not say what she saw in the bottom. Pilcher reads, aloud, from a book, a long dry description of the way the dead are recycled, and by the end the whole clan are sitting silent round the table, with their tea untouched.",
                        "@Pilcher| It is a custom. It is as old as the Garden. It says here that no one is wasted. It is not unkind. It is not kind either. It is a system.",
                        "@Marta| (stoutly, after a pause) Well. It could do with a bit of seasoning.",
                        "* You pour the midnight tea over the side. It is a nonsense, a small gesture. You do it all the same. The cup falls, turning, glittering, a long way down into the white, and you do not hear it land.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("balance", "The Balance").gap(4)
                .intro(
                        "* You find it on a high bare plateau, in a hall with no roof: a balance as big as a church. It stands on a plinth of black stone, with two vast pans of bronze hung from chains as thick as a man's waist. In one of the pans sits a small stone. The other is empty. It is level.")
                .wait(2)
                .outro(
                        "@Tamsin| (at the rail, in a low voice) It's the Assayer's Hall. It weighs us. It measures what a person is. It has a number for it.",
                        "* Edric Hexley, who is nineteen and a person of enormous curiosity, begs to be lowered in a basket. You lower him. He steps out onto the empty pan, with his hat in his hand, and the balance does not move. He stands there with an expression of dawning wonder, a small tidy young man on an acre of bronze. Nothing happens. After a minute, very gently, the pan with the stone begins to rise. The empty one sinks. It sinks an inch, and stops.",
                        "@Edric| (calling up, delighted, with his arms spread) It says I weigh an inch! Isn't that wonderful? An inch! I always thought I was nothing!",
                        "* You haul him up. He is laughing so hard he can hardly stand. Isla says, drily, that it is the first time in the history of the Garden anyone has been pleased by their measure. Tamsin says that she is not going down.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("shed", "The Shed in the Wood").gap(4)
                .intro(
                        "* The forest is so thick that you can see no ground. It rolls out beneath you like a green sea, dark and deep, with here and there a tall tree standing clear. In the middle there is a clearing, no bigger than a handkerchief, and in the clearing a small weathered hut with a crooked chimney. A thread of smoke goes up from it, straight, into the still air. Nobody lives there. Nobody has lived there in three hundred years.")
                .wait(2)
                .outro(
                        "* You hang over it in the dusk. Joren, in the basket, goes down to the door on a rope. He lifts the latch. He stays for a quarter of an hour. When he is hauled up he is empty-handed and white to the lips, and he says nothing for minutes. At last he says that the shed is full of tools. He says that every one has a label. He says that his own name is on a spade.",
                        "@Joren| ON LOAN TO: CRANE, J. It says it in a hand like a row of nails. It says: Dig well. It's the nicest thing anyone ever said about my shovel.",
                        "* You do not go in. It is not yours to go in. But on the way out of the clearing, as the hall rises again, a small bright thing falls from the beam, with a ping, onto the grass below. It is a brass screw. Joren looks over the rail and says: 'We've left something.' Tamsin says: 'We've left it where it belongs.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("thinsky", "The Thin Sky").gap(4)
                .intro(
                        "* The air goes thin and cold. The hum under your boots begins to falter. At the edge of the far country there is a mountain peak, high and bare, with a bright pale sky behind it. In the sky, from one horizon to the other, there is a crack. It is a long thin line of darkness, ruler-straight, and through it a faint wind blows that smells of something that is not weather.")
                .at("sky", "Sail the hall to the Thin Sky, {@sky}.")
                .outro(
                        "* The hall shudders. The banner flaps. The hum under the floor slows, and slows, and stops. For an instant, over the highest ground in the Garden, the whole house of the Silver Crane Clan hangs motionless in the cold. It is forty tons of beam and slate, with a crack in the sky above it and a crack below. Nobody moves. The tea in the cups is a still brown ring.",
                        "@Isla| (a whisper, with the cord white in her fists) Something's leaning on it.",
                        "* You feel it too. It comes through the floor, through the soles of your boots: a vast gentle patient pressure, like a hand laid flat on the other side of a door. It is not hostile. It is not anything. It is only very large, and very tired, and it has been resting its weight against the sky for a long, long time. Then the hum comes back, a long clear note, and the hall tips away, shaking, and begins to fall.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("garrison", "The Line, from Above").gap(4)
                .intro(
                        "* You come down the long slope of the sky toward evening, at the edge of a bare plain. A fortress is there, with its gates open, a small grey square. In front of it is a thread of tiny figures in a line. The hall sinks, with a quiet sigh, to within a hundred feet. You can see their faces.")
                .wait(2)
                .outro(
                        "* They look up. They stand in the rain, a hundred of them, with their lamps, and watch a house descend from the clouds with a goat on its roof. Nobody cries out. A woman near the front, with a grey plait and a ledger, lifts one hand to shade her eyes. She smiles. It is a small, tired, startled smile. She lifts the hand, and waves.",
                        "@Marta| (leaning far over the rail, with the dumpling pan in both hands) Have they eaten?",
                        "* She tips it. A hundred and forty dumplings fall in a long fragrant shower on the plain, bouncing, rolling, steaming, among the boots of the line. Nobody moves for a moment. Then a boy of sixteen stoops, and picks one up, and bites. His face changes. He says something. A rumble goes down the line like a wave, and the whole file, to the last man, is laughing.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("land", "Landing").gap(4)
                .intro(
                        "* You bring the hall down on the third of April in a thin cold drizzle, on the walls of its own foundations, with a bump like a wooden spoon dropped on a table. The beams settle. The slate sighs. The hum fades to a low murmur, and then to a whisper, and then to nothing. In the silence you can hear the river, and the wind in the poplars, and the goat.")
                .wait(2)
                .choice(new Chapters.Choice("flight", "The hall has flown, and come home, and can fly again.",
                        "Keep it: a hall that can lift whenever the clan wishes, kept on a short rope.", "Ground it: pin the roof to the walls with iron, and make the flight a story.", "Give it away: offer the hall and its banner to the Palace of the Oldest Order.")
                        .after(
                                new String[]{"* You keep it. You add a chain to the corner posts, thick as a man's arm, and a set of brass rings. The hall rises a few yards, on summer afternoons, to catch the breeze, and the school takes its lessons on the roof. The Baron's son has a hammock slung from the beam. It becomes the best-known house in the province, and nobody has ever been able to find the Silver Crane Clan's address.", "@Joren| (with a spanner) It's all right. It's very well balanced."},
                                new String[]{"* You ground it. You drive twelve iron bolts through the beams, into the walls, with Joren's own hammer. The hum sinks. The banner hangs. The hall sits down on its foundations like a hen settling on eggs. The school children draw it, in chalk, on the flagstones of the yard: a house with wings. It becomes a tale. In fifty years nobody believes it.", "@Pilcher| It is the best-attested incident in the clan's history. I have it in four hands, two of which are drunk."},
                                new String[]{"* You send word to the Palace. A grave, polite reply comes in a week from a man in an apron. It says that the Palace has a great many halls, and none that fly, and he has long thought one would be useful. It says that the Silver Crane Clan would be most welcome to bring it. It says there is a field. It says the soup is ready. You do, in the spring. It is the single most extraordinary arrival the Wicks of the Outer Court have ever seen.", "@???| (in the doorway, delighted, wiping his hands on his apron) It has a goat on it."}))
                .outro(
                        "* The clan sits down to supper in the great hall that evening, at the long table, under a roof that is, at the moment, exactly where it ought to be. The tea is poured at midnight in the cellar. The hum is in the stones. Nobody says anything about it. On the beam over the table, in silver thread, in Isla's hand, a new stitch has been added to the motto. Stand on one leg, see far. And underneath, smaller: Or fly.")
                .pay(2000, 3, 0, 4, 2, 2);
    }
}
