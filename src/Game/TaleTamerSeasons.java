package Game;

/** The Beast-Tamer's Daughter, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Storm Crane" (adventure and a trial, dry wit): Quill, who is fond of drama, and the sailors' hats in her nest; the shipping consortium that blames her for the wrecks; Captain Brodin changes sides
 *  in a courtroom. Flags: quill (the step off the cliff), trial (how Quill was defended: 1 the truth, 2 the captain and the hats, 3 Quill's own voice).
 *  Season 3, "The Burned Wood" (elegy): Old Ember, the salve, the orchard, Isha's last message, the sanctuary and the seventh note. Flags: ember (how he was won), calling (what the human is to the wild ones).
 *  Season 4, "The Long Sleep" (the fox grows old; what becomes of the beasts at the Door) is planned in the beat sheets, not written. */
final class TaleTamerSeasons {
    private TaleTamerSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "quill", "trial", "ember", "calling");
        d.place("wreck", "wild").place("court", "far");
        d.cast("dray", "Captain", "Dray").he().keep().trait(Character.Traits.Merchant).side("Good").lvl(1).age(52).at("place:town").rel(1);
        d.cast("teague", "Wilmot", "Teague").he().trait(Character.Traits.Scribe).side("Good").lvl(0).age(22).at("far").rel(1).free();
        d.cast("prewitt", "Magistrate", "Prewitt").she().trait(Character.Traits.Leader).side("Neutral").lvl(2).age(57).at("place:court").rel(0).free();

        // ================================================================== SEASON 2
        d.season(2, "The Storm Crane");

        d.chapter("weather", "Weather").years(2).major(1)
                .intro(
                        "* The sea has been unkind to the coast this year. In the harbour towns they say it began with the autumn gales. Four ships went down inside a month between the headland and the Reef of Teeth, and by midwinter nobody would speak a ship's name aloud in a tavern.",
                        "* The stories vary in the details and agree on the culprit. A great white bird, they say, taller than a horse, that circles the Last Step in bad weather and is seen where the ships go down. She brings the storms. She eats the sailors. She is, depending on the pub, a witch, a god, an omen, or a clerical error on the part of the weather.",
                        "@Moss| Ah.",
                        "* He says it as he says most things, after a pause long enough to have a small weather of its own. {fox.first} has gone very still on his shell.",
                        "@Moss| She is fond of drama. I did say. I am afraid she is also very fond of sailors.")
                .wait(4)
                .outro(
                        "* The road north runs along the coast for three days, and every harbour has the same smell: tar, salt, and a particular kind of fear that has been mended with rope. Women in black stand at the end of piers with their hands in their sleeves. In one village a boy of ten leads you to the beach and shows you what the tide brought in that week: a barrel, a cap, a boot, a small wooden horse.",
                        "@Boy| Mother says the bird eats them. I don't think so. I think she'd have kept the horse.",
                        "* It is a child's argument and you can find nothing wrong with it. Ahead of you the cliff comes up out of the sea haze, black and straight and ridiculous, like a door that somebody has left open on the end of the world.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("cliff", "The Last Step").gap(1)
                .intro(
                        "* Sailors call it the Last Step, because it is the last thing they see before the clouds close over, and there is a sort of hush in the harbour when someone says it. Nobody climbs it. A great white shape circles over it in bad weather. Some say it is a bird. Some say it is a bird's ghost. All of them have the same wary look when they say it, as if the thing might be listening.",
                        "* You stand at the foot of the cliff with a whistle round your neck that you have not yet blown. The wind has already tried to take your hat twice.")
                .at("cliff", "Climb the cliff called the Last Step at {@cliff}.")
                .outro(
                        "* It takes six hours, and for much of it you cling to the rock by your fingertips and a great deal of stubbornness. The wind tries to remove you twice. {fox.first} is tied to your back in a sling of sailcloth, with her head out, regarding the drop with the interested disgust of an animal who has not been consulted.",
                        "* At the top is a nest the size of a barn. It is built of driftwood, rope, spars, old netting and what looks like several hundred hats. In the centre stands a crane taller than a horse, white as paper, with a bill like a long sword, and one cold gold eye turned on you with a look you would call disappointed if you knew her better.",
                        "@Quill| You are slower than your mother. But you have her feet.",
                        "@Quill| I do not take anyone up unless they trust me. The wind is wild, the cliff is high, and I am proud of how I catch things. Step off the edge, and I will tell you what the weather is for.")
                .outChoice(new Chapters.Choice("quill", "The edge is very near, and very far down.",
                        "Step off the cliff.", "Climb back down, and ask her to meet you at the bottom.")
                        .after(
                                new String[]{"* The wind takes you. For one sweet endless instant you fall, and then something enormous and white closes underneath you like a cup. You are laughing before you know it. She is laughing too, in her crane way, with a sound like a rusty gate.", "@Quill| Well. That was easier than your mother. She screamed."},
                                new String[]{"* You climb down. It takes seven hours. At the bottom she is waiting, perched on a rock at the tideline with one leg tucked up, looking smug.", "@Quill| Sensible. Your mother would have jumped. I shall have to teach you to jump."}))
                .done((c, st) -> Chapters.companion(c, st, "quill", "Quill", "Storm Crane", Math.max(20, c.lvl + 3), false))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("hats", "Hats").gap(3)
                .setup((c, st) -> Chapters.spawn(c, st, "dray"))
                .intro(
                        "* The nest is, you discover, a museum. There are hats on every ledge. Oilskin hats, knitted caps, a magistrate's wig, one top hat, a green felt thing with a feather, a hundred and twelve sou'westers. They hang in rows on the spars with a care that would shame a hat shop. Each of them has a label tied to it in Quill's crooked beak-writing: a name, a date, the name of a ship.",
                        "@Quill| I keep what they leave. When a ship is lost the sea brings back the hats. It brings back very little else. Somebody should say their names.",
                        "* {fox.first} has found a bowler hat and is sitting in it with the air of someone about to be photographed.",
                        "@Quill| Some of them are alive. That is the part that nobody wants to believe. I take them off the rocks and carry them to the beach below the village of Salt Landing and put them down where they will be found. They never look up. They do not think of birds. They think of rescue.")
                .meet("dray", "Find Captain Dray in the harbour town, who is looking for a hat.")
                .outro(
                        "* He is sitting on an upturned crate by the quay: a thin man in his fifties with a face like a mended sail. He drinks tea from a tin cup in the careful manner of someone who has swallowed a great deal of salt water. He is looking at the ground. There is a stripe of paler skin on his forehead where a hat used to be.",
                        "@Dray| I was on the Marigold when she went. Twelve years. I woke on the shingle with a broken arm and a headache, and a white shape going away over the cliff. I thought it was the Lord. I told people. They gave me a cap with a chin strap and said it was shock.",
                        "@You| She's got your hat.",
                        "* He looks up. Then he looks at {fox.first}, who is wearing a sou'wester in a fashion that does not suggest a good conscience. He starts to laugh, helplessly, the way a person laughs who has been told something very large in a very small voice.",
                        "@Dray| I knew it. I knew it. Oh, she's a lovely thing. Eleven of us walked off that beach. The company said it was a miracle. It was a bird with a very fine sense of timing.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("lessons", "Flying Lessons").gap(4)
                .intro(
                        "* {fox.first} wants to fly. She has made this clear by climbing to the highest ledge of the nest every morning and standing on it, looking at the sky, with the rapt, stupid hope of an animal who has been told that anything is possible.",
                        "@Quill| It is not possible. She has no wings. She is a fox. I have said so several times.",
                        "@Moss| You said so with one eye on the ledge.",
                        "@Quill| I have a reputation.",
                        "* It becomes the work of the month. Quill instructs. Moss supervises from below with the weary patience of a headmaster. And {fox.first} leaps from the ledge into a sling of netting that the crane has spread across two spars, falls, bounces, and climbs back up, with fresh enthusiasm and a sharply declining dignity.")
                .wait(4)
                .outro(
                        "* On the last day she jumps from the high spar without the net. Nobody has asked her to. You take a step forward, and Quill is faster: a long white blur, a sound like a sheet being shaken out. Then {fox.first} is lying across the crane's back with her paws out and her fur on end, her eyes wide as a pair of plates. She is making a thin sound that you identify, after a moment, as joy.",
                        "@Quill| I have never caught one of them before. My mother used to say it was the thing to do. I did not understand. I had thought it was a duty. It is rather lovely.",
                        "* She sets the fox down and looks at her wing, and then at you, with the expression of someone who has noticed her own hands for the first time.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("fair", "Salt Landing").gap(4)
                .intro(
                        "* Salt Landing holds its autumn fair on the shingle below the village, when the tide is out and the boats are drawn up in rows with their keels in the air like a congregation at prayer. There is a man selling herring in paper cones and a man selling the paper cones. There is a stall of hats, because the hats are why you have come.",
                        "* Captain Dray has brought his family to meet the crane. It is a larger family than you expected. There is a wife with a face like a plate of warm buns, and a mother-in-law who has views. There are seven children, from a boy of fifteen to a baby on the hip of a girl of nine. They stand in a row on the shingle with their hats off, and Quill, on the headland above, lowers her great head and regards them with her one cold gold eye.",
                        "@Dray| She doesn't like crowds. I'm sorry. I did tell them. They wanted to see her.",
                        "@Quill| I do not mind being seen. I mind being regarded. There is a difference, and it lies in the quantity of children.")
                .wait(2)
                .outro(
                        "* It is the baby who settles it. Nobody could say how it happens. A girl of nine, who is holding it, trips on a net-float and sits down hard on the shingle. The baby rolls out of her arms and comes to rest, unharmed and extremely pleased, against the very toes of the crane.",
                        "* Quill goes rigid. Seven children and a mother-in-law hold their breath. The baby looks up the whole fifteen feet of white feathers, to the bill, and the eye, and the crest, and laughs. It is the unselfconscious crowing laugh of a creature who has just found the best thing in the world, and puts out both hands to be picked up.",
                        "* Quill picks it up. She does it with her bill, with a delicacy that you would have said was beyond her, and sets it on her back between the wings. It sits there, delighted, holding two fistfuls of down. And the mother-in-law, to the amazement of the entire village, says in a loud clear voice: 'Well, now. That is the first sensible thing that bird has done.'",
                        "* By evening, Quill has given rides to nine children, a goat, and the minister. She says it is purely a matter of respect. She says she will not do it again. She is, you think, the happiest you have seen her.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("wreck", "The Wreck").gap(8)
                .intro(
                        "* The storm comes at midnight, out of a clear sky, with no warning but the fox's fur standing on end. You wake on the cliff to a sound like the sea clearing its throat. Quill is already on the ledge, a white shape against the dark, with her wings half open. She does not look round.",
                        "@Quill| A ship. The Marigold's sister. I know the sound of her mast.",
                        "* The next hour is the longest of your life. The wind comes straight up the face of the cliff and tries to take the nest apart. Moss, forty feet below, has gone into his shell like a house with the shutters closed. You cling to a spar and shout something that is lost.")
                .at("wreck", "Go with Quill to the wreck at {@wreck}.")
                .outro(
                        "* There are fourteen on the rocks. You count them twice. Quill takes them one at a time, in her beak or on her back, from a ledge that is awash to a beach that is dry, and each time she goes she is slower. By the end her wings are trembling and her eyes are shut, and she lands badly and lies on the sand like a spilled sack of snow.",
                        "* The fourteenth is a girl of nine, whose mother has been, up to that moment, her only parent, and who is clutching a hat. She presses it into the crane's feathers with both hands.",
                        "@Girl| I thought it was lost. I wanted her to have it. I thought she'd want to know whose it was.",
                        "* The crane opens one gold eye. 'Marta,' she says, in a voice so faint it would be lost in a teacup, and then she is asleep. {fox.first} climbs on to her flank and lies down against her ribs, and the two of them breathe together.")
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("writ", "A Price on a White Bird").gap(6)
                .intro(
                        "* The consortium that owns four of the lost ships has decided what it thinks of the white bird. It is a hard thing to say in print, so it has said it in a bounty. Three thousand crowns, payable on the production of the creature, alive or otherwise, to any party able to demonstrate its part in the sinking of vessels. It is a very well-drafted bounty. It names no one it could not afford to name.",
                        "* You hear of it on the quay at {@town}. Then you hear of the man who has taken it up, and you do not need to be told.")
                .meet("brodin", "Speak to Captain Brodin, who has taken the writ.")
                .outro(
                        "* He is on the harbour wall, mending a net he does not own, in the cold, with his collar turned up. He looks, you think, much older than he did at the Cracked Egg. He does not stop when you come.",
                        "@Brodin| I took it for the money. I should say that at once, so you needn't wait for it. I've a daughter, and a mortgage on a house I can't sleep in. Three thousand crowns would be everything. I had never heard of the bird.",
                        "@Brodin| And then I started to ask questions, which is a bad habit for a man in my line. Every ship that went down had a clerk aboard who was the last to be seen. Every ship that went down had a policy on the cargo with the same firm. I don't say what I think. I say the numbers are very tidy.",
                        "@You| You think the consortium sank its own ships.",
                        "@Brodin| I think a bird with a crooked beak is a very convenient reason. And I think a court will be happier to hang a bird than to read a ledger.",
                        "* {fox.first} has jumped up on the wall beside him and put her chin on his knee. He looks at her for a time as if she were a letter he had been expecting. He does not push her off.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("clerk", "The Junior Clerk").gap(3)
                .setup((c, st) -> Chapters.spawn(c, st, "teague"))
                .intro(
                        "* Brodin finds the clerk in a pub three streets from the consortium's counting-house, because he has an instinct for pubs. He is a small pale young man in a thin black coat, with ink on his cuffs, and a pint he has not touched. His name is Wilmot Teague, and he has been junior clerk to the Shipping Board for nine months. He has the look of a person who has found a dead mouse in his soup and is deciding whether to mention it.",
                        "@Brodin| I told him we weren't the law. I told him we're a bird and a fox and a tortoise and a man with a grudge. He seems to have found that reassuring.")
                .meet("teague", "Speak to Wilmot Teague, the junior clerk who has the policies.")
                .outro(
                        "@Teague| I didn't know what it was. I want to say that first. I copy policies. I'm very quick. I copied four, in the spring, for the Marigold and her sisters, and I noticed the premiums. They were high. Higher than a ship's worth. I thought it was a mistake in the ledger, so I went through the ledger.",
                        "@Teague| Every ship that went down was insured for twice its value. Every cargo for three times. And the same man signed every policy. And the same man, six days later, signed the order for the ship to sail in November.",
                        "* He stops. He picks up the glass and puts it down. He says, in a flat unsteady voice, that he has not slept since the end of April. He says he knew a boy on the Marigold. A cabin boy, called Eddie, who used to bring him sweets at the quay.",
                        "@Teague| I have the copies. I took them. I'd like it to be known that I'm a coward. I thought about burning them. I thought about it for two weeks. Then I saw the bird on the cliff one evening, in the light, and she didn't look like a witch. She looked like someone who was waiting for something. I went home and got them out of the stove.",
                        "* The policies are in a flour-sack, in the pocket of his coat. He holds them out in both hands. They are damp and creased and scorched at one corner, and they are the most beautiful papers you have ever seen.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("hearing", "The Hearing").gap(2)
                .setup((c, st) -> Chapters.spawn(c, st, "prewitt"))
                .intro(
                        "* The hearing is in a city a week inland, in a hall with a plaster ceiling and a great many benches. The consortium has hired three advocates and a man to keep the doors. It has not hired anyone to look at the ledgers. Magistrate Prewitt, a grey, tired woman with a pen behind her ear, takes the chair at ten and says, in the voice of someone who has said it before, that she intends to be home by dinner.",
                        "* The advocate for the consortium is a smooth man in a green coat. He puts four shipwrecks on a table, and a bird made of pasteboard, and a sailor who weeps in exactly the right places, and a petition with eleven hundred names. It is very good. It is the kind of case that you could, if you were not on the other side of it, find rather beautiful.",
                        "@Prewitt| The defence?",
                        "* There is no defence. There is Quill, on a roof across the street with her head tucked under her wing, because she would not be left behind. There is Moss, in a field. There is {fox.first}, under the bench, eating a clerk's sandwich. There is you.")
                .at("court", "Go to the hearing at {@court}.")
                .outChoice(new Chapters.Choice("trial", "The Magistrate is waiting.",
                        "Tell it straight: the bird saves sailors, and the consortium is lying.", "Bring Captain Dray, and a hundred and twelve hats.", "Let Quill speak for herself.")
                        .after(
                                new String[]{"* You say it plainly. It takes four minutes. Nobody laughs and nobody gasps, and the Magistrate puts the pen behind her other ear. 'It is a very good story,' she says, 'and I have nothing to set against it but that you say it with a straight face.' The advocate smiles. It is not enough.", "@Brodin| May I?", "* He stands. His voice shakes. He says he has read the ledgers."},
                                new String[]{"* The hats come in on a cart: a hundred and twelve of them, each with its label, spread over the floor of the court in rows. There is a silence in which you can hear the plaster ceiling think about falling. Captain Dray walks out into the middle of them in his mended coat. He picks up the Marigold's cap, and turns it over, and reads his own name.", "@Dray| She took me off the rocks. There were eleven of us. We never saw her. We were told it was shock."},
                                new String[]{"* She does not come in. She does not need to. She lifts her head on the roof across the street, and she speaks, and every person in the room hears her at once, at the back of the mind, in whatever language their mother used. Each hears, in turn, a name and a date and a ship, and the sound of a place where the sea has closed over someone they loved.", "* The Magistrate puts down her pen. The advocate in the green coat is sitting very still, with his hand over his eyes. It is not exactly legal. It is, you reflect afterwards, extremely persuasive."}))
                .outro(
                        "?trial=1|* Brodin's testimony takes an hour and ends the consortium's case. He lays his own ledger on the table, in his own hand, and reads from it, and by the third page the man in the green coat has stopped smiling. The bounty is struck. Brodin does not look at you as he goes. At the door he stops and lifts one hand, a very little way, like a man testing the weather.",
                        "?trial=2|* The case is over before dinner, and in the way these things go, nobody can say exactly when. The consortium's man withdraws. The Magistrate adjourns. Captain Dray sits on the floor among the hats and cries, and a girl of nine, in the gallery, waves.",
                        "?trial=3|* The court is cleared. The consortium's advocates are, for a week, unable to eat. The case is dismissed, and no one is quite able to say on what grounds, and a rumour starts in the city that there is something on the roof that sees into people. It is a story that will follow Quill for the rest of her life, and in a way, she says, she prefers it to the old one.",
                        "* On the steps, the Magistrate stops beside you, with her pen behind her ear, and says in a low voice that she is going home to read some ledgers.")
                .pay(3000, 0, 0, 1, 0, 1);

        d.chapter("nest2", "A Quiet Week in the Nest").gap(2)
                .intro(
                        "* The week after the hearing is the first quiet one for a year. The consortium's carriages have gone. The streets are full of strangers who stop you to shake your hand. A woman gives you a fish. A boy gives you a stone with a hole in it, for luck.",
                        "* Brodin comes up the cliff on the third day. Nobody told him he could. He climbs it in the old way, hand over hand, with a bundle on his back and a child's wooden horse tied to the top of it. At the end he sits down at the edge of the nest with the sweat on his face and says that he has brought his daughter's regards.")
                .wait(1)
                .outro(
                        "* The daughter is eleven. Her name is Ines. She has been ill a long time, with a lung complaint, and she has not been out of the house in the winter. The doctors in town have a name for it, and a price. Brodin has been putting off saying the price. He says it now, flatly, to the sea.",
                        "@Brodin| It was the whole reason, you know. The bounty. I did tell you I took it for the money. I did not say what it was for. You can have a long face, if you like. It is a very ordinary story.",
                        "@Quill| Bring her here.",
                        "@Brodin| I beg your pardon?",
                        "@Quill| The air at the top is clean. It has not been breathed by anyone. I have always thought it a waste. If the girl wishes to come, and is wrapped, I shall carry her up. I do not carry people. I have made an exception of the minister.",
                        "* He cannot answer. He turns his face to the wall of the nest and stays there, among the hats, with his back to you, while the sea works at the foot of the cliff. After a time {fox.first} climbs into his lap, and he puts one hand on her back, and leaves it there.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("hatsgo", "The Hats Go Home").gap(4)
                .intro(
                        "* The consortium's money has to go somewhere, and Magistrate Prewitt has decided where. It is to go to the hats. Quill's hundred and twelve, each with its label, are to be carried home to the families of the ships. The sum is paid in the hall of the Shipping Board, by a clerk with a very red face, and the hats are loaded on a cart.",
                        "* It takes the cart eleven days. You walk beside it. {fox.first} rides on top, in a bowler. Quill goes overhead, in wide slow circles, and makes a point of not looking at the cart. At each town the cart stops and you call out a name, from a label, and a door opens.")
                .wait(3)
                .outro(
                       "* The first is a woman in a doorway, with flour to her wrists. You give her a sou'wester. She holds it against her chest, as if it were a person, and says nothing. Then she says, 'He hated that hat. He said it made him look like a pudding.' She laughs, and puts her face in it, and you stand in the road with your own hat in your hands.",
                        "* The second is a boy of six who does not know what he is being given. He holds the cap, and turns it over, and finds a name written inside the band in an unsteady hand, his father's, as if the man had written it for fun. He reads it aloud, slowly, a letter at a time. When he has finished he looks at you with the stern solemn face of a child who has understood a joke at last.",
                        "* The eleventh day brings you to the last town, which is Salt Landing, and the last hat, which is Captain Dray's own, the one from the Marigold. He has kept it back. He has asked that it be left. It is to hang on a nail in the nest, he says, on the highest ledge, where the weather gets at it, and Quill is to say its name every morning.",
                        "@Quill| I do. I have always done. It is the first thing I say.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("ines", "Ines Rides the Wind").gap(3)
                .intro(
                        "* She arrives at the cliff on the back of her father, in a blanket, with a cap on, and her thin wrists sticking out of the sleeves. She is eleven. She has never been out of the house in the winter. She has the luminous, cautious stare of a child who has been told a great deal about the world and expects to be disappointed in it.",
                        "@Ines| Is that the bird? She's bigger than I thought. She's whiter. Does she mind if I say so?",
                        "@Quill| I do not mind. It is true.")
                .wait(1)
                .outro(
                        "* The first flight is short. She is lifted into the nest, among the hats, wrapped in a quilt, and the crane puts one wing under her, like a sort of stair. They go up in a slow spiral round the head of the cliff. She does not cry out. She hangs on with both fists and her eyes screwed up, and for the length of three turns she is entirely silent. Then she opens one eye.",
                        "@Ines| Oh!",
                        "* It is the whole sea. It lies in a great flat silver sheet under the sun, from the cliff to the end of the world. The little brown boats on it are like crumbs. There is the white line of the surf, and the smoke going straight up from Salt Landing, and the sky above it so wide that it hurts. She says it again, softly, and again. Her father, on the ledge, with his hat in his hand, is not able to speak.",
                        "* She comes down an hour later, flushed to the ears, with her hair on end and the cap gone, and breathing in great clean gulps. She coughs. It is a small, short, ordinary cough. It does not tear. She looks surprised, and tries again, and then simply stands on the ledge in the wind with her mouth open, inhaling the weather.",
                        "@Ines| It doesn't hurt.",
                        "@Quill| It will take many mornings. I have thought it out. We shall begin with the sea air and proceed to the high.",
                        "* Brodin turns his face to the cliff wall. He stands there with his forehead against the cold stone until the gulls have gone round twice. {fox.first} goes and sits on his foot. He doesn't push her off.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("step", "The Last Step, Again").gap(10)
                .intro(
                        "* Autumn comes round again, and the cliff is not so ridiculous any more. The nest has been repaired with new rope and a few of the consortium's sails. The harbour towns have taken to leaving offerings at the foot of the Last Step: a loaf, a ribbon, a hat. Quill pretends she does not notice, and eats the loaves.",
                        "?quill=1|* She calls it, now, the day you stepped off. She says she has caught a great many things in her life, but only one person who was laughing.",
                        "?quill=2|* She calls it, now, the day you climbed down. She says that was the day she began to like you.")
                .wait(2)
                .outro(
                        "* On the last night of the year you all sit on the ledge: a tortoise at the foot, a crane at the top, a fox in the middle, and you. Below, the sea is flat and silver. Quill clears her throat, with a rusty-gate sound, and says she would like to try.",
                        "* You hum the six notes. Moss gives you the low ones, a long way down. And then Quill, a little shyly, with her bill pointed at the sky, sings the high end, a note so far up and so sharp that it hurts, like a pin of light. She sings it where your mother always stopped.",
                        "* It is not the seventh. You know that at once. It is only a piece of it, a bright edge, like the corner of a cloth that has been folded away. But {fox.first} puts her head back and joins in, off-key and a little behind, and there is a moment, for the length of one breath, when four voices make a shape you can almost see.",
                        "@Quill| Ember has the dark one. I know where. I should tell you now, so that you cannot say I did not warn you. Do not bring a lantern.")
                .pay(5000, 0, 0, 2, 0, 1);

        d.interlude(2, "Don't Bring a Lantern",
                "?trial=1|* The consortium folded within the year. The ledgers were read aloud in three cities. A small blue book with Brodin's name on the spine has become a kind of text for people who want to know how to ask a question.",
                "?trial=2|* A hundred and twelve hats hang in the town hall at Salt Landing now, each with its label. People come in on Sundays to look. Children are told: this one was kept for him by a bird.",
                "?trial=3|* Nobody asks Quill to explain herself any more. She is simply allowed to fly where she likes, and the sailors lift their hats, and she lifts a feather back.",
                "?dead.fox|* There is a hat on the nest that has no label. Quill will not say whose it is, and you do not ask her to.",
                "?!dead.fox|* {fox.first} has learned to hang from Quill's back by her teeth, which the crane says is not flying and which {fox.first} says is. They have given up the argument by agreeing to disagree about it at altitude.",
                "* The third one is still out there, in a wood that burned, in the ash, with a name that nobody says without lowering their voice.");

        // ================================================================== SEASON 3
        d.season(3, "The Burned Wood");

        d.chapter("elevenstones", "Eleven Stones").years(1)
                .intro(
                        "* The village at the edge of the Burned Wood is called Cinderfold, and it has a churchyard with a field of small stones. Eleven of them stand together at the north wall, in a row, with a rope in front. Fresh flowers are laid at the foot of each. They are changed on Sundays.",
                        "* You read the names, as you would read a list of the dead in any village. Marten Hale, a carter. Joan Pike, nineteen. Three brothers of the same family, who went up to cut wood on a Tuesday and did not come down. A priest. A woman called Isabel Greave, whose stone has a pair of boots carved on it, small, as for a child. And at the end, in newer stone, GARRICK BRODIN, 19.")
                .wait(1)
                .outro(
                        "* A woman is kneeling at the last stone. She is sixty, in a black shawl, with a bunch of heather in her lap and a knife for the stems. She does not look up as you come. She says, in the voice of someone speaking to a child she has known for many years: 'That's the Captain's boy. They put him last. He was the last to come down.'",
                        "@Woman| They tell me you're going to talk to it. The thing in the wood. I'll say this, since nobody else will. I've lost a son. I'm not going to ask you to let it live. I'm not going to ask you anything. I only want you to have seen the stones.",
                        "* She trims the stems. The knife is very sharp, and her hands are steady, and she lays the heather on the stone in a neat, flat bundle. She says that her own boy is the third stone from the end.",
                        "@Woman| He had a hand like a spade. He'd have been a good man to have by you in a flood. I tell myself he'd have understood the boar. They say it screamed all the way down the hill.",
                        "* You do not say anything. The wind comes down off the wood, and with it, from far off, the smell of old ash, and from the middle of the stones, quite clearly, as you stand there, the sound of {fox.first} sitting down.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("lantern", "Ash").years(2)
                .intro(
                        "* The Burned Wood is two days beyond the last village, past a field of black stones that used to be a farm. The trees stand in rows like the bones of something that was once very large. The air smells of cold fires. The ground is soft with cinders, and every footstep sends up a little cloud that settles on you like a thought.",
                        "@Quill| Nothing alive will go in. I would not blame you if you waited at the edge.",
                        "@Moss| I will wait. I am old, and I have a bad feeling about my legs.",
                        "* It is {fox.first} who goes first. You have not asked her to. She walks into the ash with her head up, her ears forward, and her tail low, like a very small soldier, and does not look back, and you follow her, because you cannot think of anything else to do.")
                .at("wood", "Enter the Burned Wood at {@wood}, alone and without fire.")
                .outro(
                        "* You feel him before you see him, a heat on the back of your neck, a sense of being watched by something that has been watching a very long time. Then the ash in front of you heaves, and he stands up out of it, like a mountain standing.",
                        "* He is a boar as large as a house, black as the wood, with tusks like curved swords and a hide cracked with old burns. Small fires still glow in the cracks. His eyes are red and wild and very tired.",
                        "@Ember| FIRE. YOU. SMELL. OF. FIRE.",
                        "* You do not. You smell of ash and mud and fox. But it is clear that to him everything does, and that this is not a quarrel with you. It is a kind of scream. {fox.first}, at your feet, has planted all four paws in the cinders and is looking up at him without moving, and you realise she is humming.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("captain", "The Captain's Torches").gap(2)
                .intro(
                        "* You retreat to the edge of the wood, because there is nothing else to do, and find Captain Brodin waiting in the trees with twenty spears and a long row of lit torches. He has not slept. He has the plain, careful courtesy of a man who has made up his mind and does not like it.",
                        "@Brodin| I'm sorry. I've not forgotten the hearing. I haven't forgotten any of it. But there was a boy of nineteen, and I counted. A man can only be talked out of one thing in a year.",
                        "@You| You'll burn him again.",
                        "@Brodin| I'll do what I came to do. Stand aside, or don't, but I'd sooner it was aside.")
                .defeat("brodin", "Stop Captain Brodin, who has come with torches for Old Ember.")
                .outro(
                        "?dead.brodin|* It is quick and ugly. The hired spears run when their captain falls, and the torches go out one by one in the wet ash. You stand with your hand over your mouth until the last torch is out, with a fox sitting at your boots and a sound in your ears like the sea.",
                        "?!dead.brodin|* He yields on his knees, and his men stand back with their torches lowered. You take the torches from them one at a time and throw them in the river.",
                        "?!dead.brodin|@Brodin| The price was fair. He killed eleven.",
                        "?!dead.brodin|@You| He was afraid. So was Garrick. So are you.",
                        "?!dead.brodin|* He says nothing. He turns his face away. In the morning he and his men are gone, and the belt buckle has been left on a stump, where you will find it. You put it in your pocket, because it is the kind of thing that ought not to be left.",
                        "* In the wood, behind you, a single huge sound rolls across the ash, low as an earthquake. It is not a roar. It is closer to a question.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("ember", "Old Ember").gap(1)
                .intro(
                        "* This time you go in without anyone. You have left Quill and Moss and {fox.first} at the edge, because he has had enough of watchers. You carry nothing but a pack and a whistle, and a sense that you are about to be asked an enormous question in a language made entirely of heat.",
                        "* He is waiting. He is standing in the ash with his head down, with his great black flanks heaving, and he is looking at you with an expression that you can only call grief.")
                .at("wood", "Go back into the Burned Wood and face Old Ember.")
                .outChoice(new Chapters.Choice("ember", "Old Ember stands between you and the burned trees, and does not know you.",
                        "Hum your mother's tune, the six notes.", "Stand still with your hands open.", "Draw your weapon.")
                        .after(
                                new String[]{"* You hum. Your voice shakes. Three slow notes, and the same three, higher. The boar goes quite still. Then, very slowly, he lowers his enormous head until the tip of one tusk touches the ground.", "@Ember| ...Isha?", "@You| Her child.", "* He makes a sound like a mountain sighing. It goes on until the ash has settled."},
                                new String[]{"* You don't move. You don't speak. You hold out your empty hands. The boar circles you once, twice, close enough that his heat blisters the leaves behind you. On the third pass he stops, and presses his great snout into your open palm.", "@Ember| You smell of her. Under the smoke."},
                                new String[]{"* He charges. You meet him with your weapon, and you are thrown into a tree, and the world goes black.", "* When you wake, he is lying beside you in the ash, huge and warm, licking the cut on your arm with a tongue like sandpaper. He was never trying to kill you. He was trying to find out if you would run.", "@Ember| You stood. Isha stood too."}))
                .outro(
                        "* After that there is not much to say, and neither of you says it. He lies down. You sit with your back against his flank, and the heat comes through your coat like a stove. At the edge of the wood three small shapes are waiting: a crane, a tortoise, a fox. They have been there the whole time.")
                .done((c, st) -> Chapters.companion(c, st, "ember", "Old Ember", "Cinder Boar", Math.max(30, c.lvl + 3), false))
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("salve", "A Salve for Old Ember").gap(2)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The burns are older than the story. They run in long cracked seams across his back, some as wide as a hand, some as long as an arm, glowing a dull red in the dark like coals in a stove that has been banked all night. They have never been tended. Nobody has been near enough.",
                        "@Moss| It will take weeks. It is a task for someone patient.",
                        "@Quill| It is a task for someone with hands. I have a bill.",
                        "* So it is you who does it, with the pots from the pack and the herbs you can find, and a long wooden spoon. {fox.first} supervises. She sits on his head and gives instructions in the form of sneezes. Ember lies still as a hill and says nothing at all, and every so often one of the seams cools and goes grey, and his breath catches.")
                .made(6, "Tend the old burns: make six salves, one at a time.")
                .outro(
                        "* On the last night he speaks. His voice is lower than before, slower, and the words come out the way a man gets out of a bath who has not stood in a long time.",
                        "@Ember| She did this. The first week. Before the hunters. Before the fire. She put her hands on me where I was hurt and said nothing. Nobody had touched me before. I did not know it was a thing a person could do.",
                        "@You| She was a good person.",
                        "@Ember| She was a loud person. She sang off-key. She thought a pig could be taught to dance. She was the best thing that ever happened to me.")
                .pay(0, 0, 1, 0, 1, 0);

        d.chapter("moss3", "Moss Wants an Apple").gap(4)
                .intro(
                        "* Moss has heard about the apples. He has not been told. He has simply put two and two together, which in his case takes about a week, and has arrived at the conclusion that he has been left out.",
                        "@Moss| I am told that Old Ember is to have an apple from a tree that has never burned. I am told it is a great occasion. I find I do not like to be a person who has to be told these things.",
                        "@Ember| You have had an apple. You had one in the first year. I was there.",
                        "@Moss| It was a very small apple. It was a market apple. I do not think it counts.")
                .wait(2)
                .outro(
                        "* They bicker for the whole of a spring. It is one of the great quarrels of the age. Two animals as old as the hills lie twenty feet apart in the grass, communicating by long sighs and meaningful silences, which {fox.first}, from her seat on a stump, translates for you into plain terms. 'He says it is a matter of principle.' 'He says it is not about the apple.' 'He says he has been very patient for two hundred years.'",
                        "* On the night before the tree is to bloom you find them side by side in the dark, with their shells and their tusks touching, asleep. A single leaf has fallen on Moss's nose. In the morning there is a ripe apple, early, a small gold one from the lowest branch, laid carefully on the grass between them. Nobody will say who put it there. Ember says it was the wind. Moss says it was very fortunate that it was the wind.",
                        "@Quill| It was me. I have been a very long time in the company of the old. I know what is needed.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("orchard", "An Orchard of One Tree").gap(1)
                .intro(
                        "* He has one wish, and he is embarrassed by it. It comes out on a still evening, after a long silence, as if he had been working up to it for a decade. He wants to taste an apple from a tree that has never burned.",
                        "@Ember| Isha used to bring me apples. She said I didn't have to like them. I did not know what I liked. I know now.",
                        "* You buy a sapling from a village three days away. It is a thin, spindly thing with four leaves. You plant it by the edge of the wood where the ash gives way to grass, and the fox buries a bone beside it, for luck, and Moss waters it with a bucket held in his mouth, taking some time.")
                .wait(52)
                .outro(
                        "* It blossoms on the tenth day of spring, a small cloud of white and pink on a tree that is not quite as tall as you are. The fox has stopped being able to leave it alone. Quill leaves hats in its branches. Moss has opinions about the soil.",
                        "* The first apple is the size of a plum. It is green and hard. Ember eats it very slowly, with his eyes shut, in the middle of a field of grass, in the sun, and he is silent for the rest of the evening, and when he speaks it is a small voice.",
                        "@Ember| It's sour.",
                        "@Moss| It is a great deal sourer than mine.",
                        "@Ember| It's the best thing I've ever eaten.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("dream", "What Ember Carried").gap(1)
                .intro(
                        "* That night, in the wood that burned, you sleep between a boar and a tortoise and a crane. You dream.",
                        "* You dream of a woman with your chin, in a wood that is on fire. She is not running. She is walking between the flames, humming, with a bone whistle in her hand, and a great black shape at her side, and the shape is crying. She turns, and she looks straight at you, though you are not there.",
                        "@Isha| Tell my child I never left because I wanted to. Tell my child it was the only way to get him out. Tell my child the farm was the best thing I ever did.",
                        "* You wake with ash on your face. Ember is looking at you. His eyes are no longer red.")
                .wait(1)
                .outro(
                        "@Ember| She is not coming back. She was burned. She got me out, and she did not get out. I carried her. I put her in Moss's lake. I did not know how to tell you.",
                        "* Moss, behind you, rolls over in the ash with a long sigh.",
                        "@Moss| It is a good lake. She is not cold.",
                        "* You do not say anything, and nobody asks you to. {fox.first} climbs into your lap, a thing she does not do, and puts her face under your arm. After a while Quill spreads a wing over the lot of you, as one spreads a sheet on a bed, and the wood is quiet.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("farmer", "The Farmer Comes North").gap(3)
                .intro(
                        "* You write to your father in the autumn. It is a short letter. It says that you have found Ember, and the other two, and that your mother is in a lake, and that you have a fox, and an orchard, and that you are all right. It does not say the rest of it. It says: come if you like.",
                        "* He is on the doorstep eleven days later, with a bag of onions and a bad knee, looking at a boar the size of a house. Ember is looking back. Nobody moves. {fox.first} sits down between them, with the air of a diplomat, and begins to wash her ear.")
                .wait(1)
                .outro(
                        "@Ember| You are the one she went home to.",
                        "@Father| I suppose I am. I did not know she talked about me.",
                        "@Ember| She did not. She went home. It is a different thing. She never talked about the places she was going to. Only the places she had left.",
                        "* Your father looks at him until the fire cracks. He has never stood in front of a creature like this, and his hands, folded over the bag of onions, are as white as the vegetables. He says at last, in a small voice, that he has been angry for eleven years at somebody who isn't there, and it seems he has been angry at the wrong one.",
                        "@Father| I should like to say I'm sorry. I don't know for what.",
                        "@Ember| For not coming after her. I know. I heard you decide it, on the porch. You were afraid. It was the best thing you ever did.",
                        "* That night he sits by the fire with the beasts, and tells them what it was to be left. He does it badly. He loses the thread. He asks Moss whether tortoises feel the cold, and is told, at length, that it depends on the tortoise. It is the longest conversation of his life, and when he goes to bed he is humming, flat and unsteady, six notes, and then a seventh, which is not quite the right one, and which Ember hums back.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("grey", "Grey at the Muzzle").gap(3)
                .intro(
                        "* You notice it in the spring, on the stone by the orchard, in the light. It has been there a while. A fine line of silver, along the muzzle, and round the eyes, as though someone had gone over her with a sharp white pencil in the night. She is lying on the warm flagstone with her chin on her paws, in the sun, with the apple blossom coming down on her, and she does not get up when you call.",
                        "* She is twelve, or thirteen, or something. You have never known. A fox is not a creature that tells you its age. She has always been a thing in motion. She has been a flash and a tail and a rumour in the bushes. She has got slow in the way of someone who has decided to, not been made to. She walks a lap of the orchard in the evening, and sits down in the middle of it, and watches the beasts as a retired captain watches the sea.")
                .wait(1)
                .outro(
                        "@Moss| She is not ill. I want to say that at once. I have known many animals who were ill, and she is not one. She has simply understood that there is a great deal of time, and that she does not have to use it all at once.",
                        "@Ember| She sleeps on my back, you know. In the afternoons. I do not mind. It is the one weight I do not mind.",
                        "* You sit down beside her on the warm stone. She lifts her head, and looks at you, and her eyes are the same eyes, large and amber and ironical, and she puts her nose against your wrist and breathes out, once, through it. It is the longest, most contented breath you have ever seen an animal take.",
                        "* You hum the six notes, very low, because you can think of nothing else to do. She does not answer. She does not need to. She closes her eyes. The blossom comes down. The beasts in the orchard go on grazing in the long grass, in the sun, in no hurry at all, and you sit there with your hand on her back until the stone has gone cold.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("whistle", "The Bone Whistle").gap(2)
                .intro(
                        "* You have carried it since the letter. It is yellow with age and no longer than your finger, with a tiny hole at each end. It has hung on a cord against your chest through a cliff and a wreck and a courtroom without once being blown. You have told yourself it was not time. You have told yourself it was a thing for a particular night. You suspect now that you have been afraid.",
                        "* You take it out one evening on the edge of the orchard, with the sun going and the blossom coming down, and you hold it between your finger and thumb, and turn it over. There is something scratched on the side, so worn that you have never seen it. You tilt it to the light. It is a row of tiny marks, like a bird's footprints, and under them a name in your mother's hand. Yours.")
                .wait(1)
                .outro(
                        "* You put it to your lips. The sound is very small. It is not the six notes, or any notes. It is the sound a breath makes going through a hollow bone, a thin, dry, human sound, like a word said under the covers. It goes out across the orchard and nothing happens.",
                        "* Then, a long way off, from the line of the hills, a wolf howls. And from the lake, faintly, a deep note. From the cliff, a cry like a rusty gate, so far that it might be a trick of the air. From the burned wood a long low rumble that you feel in the ground. And from the stump at your feet, from a very small, very ordinary animal who has been dozing in the sun, a scratchy flat little answer, once, in the shape of a question.",
                        "* They are all answering the same thing. It is not a call. You understand that now. Your mother did not use it to summon them. She used it to say where she was.",
                        "@Quill| (from a great way off, faintly, across the sky) Ah. You have blown it. I wondered how long. She used to do it every dusk, on the roof of the farm, to see who was awake.",
                        "@Moss| (from the lake) It is a very small noise.",
                        "@Ember| (from the wood) It's enough.",
                        "* You sit on the grass with the whistle in your hand. It is warm. Soon you put it to your lips again, and this time you play something: three slow notes, and the same three higher. Then, with your heart going, you play a seventh that you have only ever heard in a dream. It is not the right one. It is close. Across five miles of air, the whole of your family answers, and corrects you.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("lake3", "Where Isha Lies").gap(2)
                .intro(
                        "* You go to the lake in the autumn, alone, on foot, with a flask of tea and a stone from the orchard in your pocket. It is the first time you have been since Ember told you. The water is flat and dark and very still. There is a heron on the far shore. A thread of mist lies along the surface, and the reeds at the edge are the colour of old brass.",
                        "* Moss is waiting. He has risen, a little, from the middle. His shell is a rounded island of green in the grey, and his old face looks out of it with its usual enormous patience. He has not been asked. He knew you were coming.")
                .wait(1)
                .outro(
                        "@Moss| She is not in the deep part. I have kept her where the sun reaches. I thought she would like to be warm. I put her by the reeds. There is a stone.",
                        "* You walk along the shore. The stone is flat and plain and very smooth, half under the water. It has no name. It has no carving. There is a small bright pebble on it, and a dead leaf, and a feather: a long grey feather with a white tip. Somebody has been here before you, and often.",
                        "@Moss| The wolf comes. Once a month. I do not know why. He sits on the bank and does not say anything. The first time I thought he was a hunter, and I nearly rose, and then he put down the feather, and sat, and I understood he was doing what I do.",
                        "* You kneel at the edge. The water is cold on your knuckles. You put the apple stone beside the feather, and say nothing, because there is no word in the language that fits. After a while you find that you are humming the tune. Six notes. Slowly. Then, from under the surface, a long way down, a tone like a bell that is very deep and very sweet. Moss is singing the low one.",
                        "@Moss| She sang that one to me when I was young. I did not know what it was for. I think it is for this: so that one may sing it back.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("sanctuary", "The Place for the Wild Ones").gap(10)
                .intro(
                        "* The wild ones need somewhere to go. You have known it for a year: the burned, the hunted, the old, the ones nobody asked. You begin to hear of them. A bear with one eye. A pair of owls that have forgotten how to be quiet. A swan with a bad wing, who has been waiting by a pond for someone to notice.",
                        "@Brodin| I'll build the fence. I've nothing else to do with my hands.")
                .wealth(5000, "Build a sanctuary for the wild ones: gather 5,000 spirit stones.")
                .outro(
                        "* It goes up at the north end of Nobody's Lake, a green place with a fence made of fallen trees. There is a hut for you, a shed for the hurt, and a big flat rock where a tortoise can sleep. Hunters come out of curiosity, and mostly go away astonished: a cinder boar is eating apples from a child's hand, and a crane the size of a horse is giving flying lessons to a goose.",
                        "* At dusk you take the bone whistle from your pocket for the first time. You put it to your lips. You blow it, once: three slow notes, and the same three, higher.",
                        "* And then it comes. Moss gives the low one. Quill the high. Ember, from the field, a note so deep and so warm that the lake shivers. {fox.first} on the rock beside you, small and flat and certain, sings the last of all, the one your mother could not find: a quick, ordinary, wonderful sound, like a door opening on a lit room.",
                        "* It is the seventh note. Everyone knows it at once. It was not in the great ones. It was in the small one all along.")
                .outChoice(new Chapters.Choice("calling", "The wild ones are safe. What will you be to them?",
                        "A tamer, with all of them always at your side.", "A guardian, who lets them come and go as they please.", "A voice between the wild and the towns, with students of your own.")
                        .after(
                                new String[]{"* They go everywhere with you. It is loud, and slow, and no inn will take you, and you would not trade it."},
                                new String[]{"* You open the gate each morning, and each evening they come back. That is the thing no one tells you about letting them go."},
                                new String[]{"* Your first student is a girl who sleeps in the barn. Your second is a man who has been bitten by every animal he has tried to feed. You are very patient with both."}))
                .pay(5000, 0, 0, 2, 0, 1);

        d.epilogue(
                "* Your mother was the Voice of the Wild. You are, you are told, something else. The wild does not have a word for it yet.",
                "?calling=1|* You and the four walk the roads, and the children of every village run out to look. They are not afraid. A cinder boar who loves apples will do that to a village.",
                "?calling=2|* The gate stays open. The sanctuary has forty residents now and a waiting list. Nobody has ever been turned away.",
                "?calling=3|* The School of the Open Hand teaches forty students a year to listen. The first rule is to sit still and wait. They say it works better than anything.",
                "?ember=1|* Ember hums sometimes, when he thinks nobody can hear. Six notes, and then the seventh, which is not his.",
                "?ember=3|* Ember has a scar on his tusk from the night he threw you into a tree. He shows it to visitors with great pride.",
                "?dead.fox|* The tree by the lake has a stone under it, and a bone, and a smell of apples in the autumn that nobody can explain.",
                "?!dead.fox|* {fox.first} is old, and sleeps in the sun by the lake, and does not leave. If you hum the six notes she lifts her head at the end, and sings the seventh without opening her eyes.",
                "?!dead.brodin|* Captain Brodin keeps the fence. He has told people it is a hobby. A sack of apples comes to the sanctuary every spring, with a note that says nothing, and a belt buckle hangs on the post by the gate.",
                "* On the night of the autumn storms you sit by Nobody's Lake with a bone whistle in your hand and tell the water about your day. The lake keeps its own counsel. But it is not cold.");
        TaleTamerMore.add(d);
    }
}
