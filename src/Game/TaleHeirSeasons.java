package Game;

/** The Last Heir of the Azure Cloud Sect, seasons 2 and 3 (see "Start Stories Seasons Plan.md" and "Start Stories Beat Sheets.md").
 *  Season 2, "The Silent Tonic" (court intrigue and caper, dry voice): the sect is being rebuilt, Wen is still there (the gender of Wen follows the player's: a person to love is always of the other gender),
 *  and a counterfeit Silent Tonic is being given to farms. The man above Orrin is Ysolde Marr, Factor of the Green Table, who holds Orrin's daughter Lisbet as surety. "One more year" was for her.
 *  Season 3, "What the Cauldron Is For" (a gentle cosmic story): Master Hui was a Wick of the Palace's pill kitchen. The cracked cauldron has a Seam in its crack. The Hollow Court sends Mister Snuff for it.
 *  Flags: farmplan (what you told the farm), gala (how the ledger was got), love (what you told Wen), thole (spared or not is read from dead.thole), mend (what became of the cauldron: from season 1's cauldron),
 *  seam (what the last brew was for). */
final class TaleHeirSeasons {
    private TaleHeirSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "farmplan", "gala", "love", "seam", "lisbet_free", "hob");
        d.place("farm", "wild").place("hide", "near").place("gala", "far");
        d.cast("dunn", "Dunn", "Tarrow").he().keep().trait(Character.Traits.Friendly).side("Good").lvl(0).age(19).at("here").rel(2);
        d.cast("quay", "Pettibone", "Quay").he().trait(Character.Traits.Merchant).side("Neutral").lvl(2).age(41).at("near").free();
        d.cast("maud", "Maud", "Hobb").she().trait(Character.Traits.Gatherer).side("Good").lvl(0).age(46).at("place:farm").free();
        d.cast("lisbet", "Lisbet", "Vale").she().keep().trait(Character.Traits.Alchemist).side("Good").lvl(2).age(24).at("place:hide").rel(1);
        d.cast("marr", "Ysolde", "Marr").she().trait(Character.Traits.Merchant).side("Evil").lvl(4).pot(6).age(58).at("place:gala").rel(-2);
        d.cast("thole", "Corvin", "Thole").he().trait(Character.Traits.Leader).side("Evil").lvl(8).pot(8).age(37).at("place:gala").rel(-3);

        // ================================================================== SEASON 2
        d.season(2, "The Silent Tonic");

        d.chapter("beds", "Thirty Beds and a Cow").years(3).soft((c, st) -> Chapters.sectSize(c) >= 3, "a sect with a few students in it")
                .setup((c, st) -> { Chapters.spawn(c, st, "dunn"); mark(c, st); })
                .intro(
                        "* Three years of rebuilding have taught you that a sect is mostly plumbing. Azure Cloud now has thirty beds, two of which do not leak, a roof over the kitchen, and a waiting list that begins with a young man named Dunn, who has arrived at the gate with a cow.",
                        "@Dunn| She's called Duchess. She hasn't got any talent, I'll say that straight out, but she's very loyal. I thought she could be, you know. The mascot.",
                        "@Wen| We have a cat.",
                        "@You| The cat will want to see her references.",
                        "* Dunn wants to learn how to make pills the way they do in the stories, where a farm boy swallows one and sparks come out of his ears and a dragon is impressed. You explain that at Azure Cloud the pills are for coughs. He says that is fine. He says there is a lot of coughing at home.",
                        "* Then, a moment later, he frowns, because that is not quite true. He says there used to be a lot of coughing at home. There has been none since the spring.")
                .made(6, "Show Dunn how it is done: make six pills.")
                .outro(
                        "* His first pill comes out the colour of a bruise and smells like a wet dog. His second is better, which is to say it is only a bruise. By the sixth, Duchess has eaten two of the failures with every sign of enjoyment and has not died, and Wen has declared that this is the sect's first clinical trial.",
                        "@Wen| Don't look so pleased. I only said it was a trial. I didn't say it was a good one.",
                        "* Wen is sitting on the end of the bench with a mortar in {wen.his} lap, grinding something that does not need grinding. {wen.He} has been here every evening for three years and has never once said why. You have never once asked. It is the kind of arrangement that two careful people can keep up for decades, if nobody drops a mortar.",
                        "@Dunn| It's the quiet I mind, if I'm honest. The cough stopped, and the shouting stopped, and my uncle's leg is still broken, and he hasn't said a word about it since the spring. A man with a leg like that ought to complain. It's his right.",
                        "* He says it like someone reporting a haunting. Nobody speaks for a moment. Duchess lows, once, at nothing in particular.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("samples", "Free Samples").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "quay"))
                .intro(
                        "* The man with the clean cart is easy to find, because he wants to be found. He has set up under a striped awning in the market at {@hide}, between a woman selling ribbons and a man selling the same ribbons at a different price. A hand-lettered board says SILENT TONIC, NEW FORMULA, SAMPLES FREE.",
                        "* Dunn has gone pale. Wen has gone quiet, which in {wen.him} is worse. You take a sample bottle from the little tray, and for a second nobody is selling anything.",
                        "@Wen| That's Hui's name for it. Nobody else ever called it that. It was never sold. It was never even written down, except in the iron.",
                        "@You| Somebody wrote it down.")
                .meet("quay", "Speak to the salesman at the striped awning.")
                .outro(
                        "@Quay| Pettibone Quay, of the Verdigris Guild, at your service and at a very fair price. You are the Azure Cloud people. Everyone says so. I have a standing instruction to be pleasant to you, which is a thing I rarely get to say about a customer.",
                        "@You| Whose instruction?",
                        "@Quay| My employer values discretion. I value my employer's valuing of it. Please, take a bottle. Take six. It does nothing at all.",
                        "* He means it as a selling point. Wen unstoppers a bottle and tastes it off the tip of a finger and says nothing for a long moment, and then says something you will still remember when the bottle is long gone.",
                        "@Wen| It is not poison. That's the horror of it. It does exactly one thing, and it does it well. It takes the complaining out of a body. The pain is still there, and the hunger is still there. Whoever drinks it just stops mentioning them.",
                        "* Quay is still smiling, and you see that he does not know. He sells it the way a man sells salt. Somewhere further up the road there is somebody who does.")
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("farm", "The Quiet Farm").gap(6)
                .setup((c, st) -> Chapters.spawn(c, st, "maud"))
                .intro(
                        "* Dunn's village is called Lower Hobb, and it is the cleanest place you have ever seen. Every fence is mended. Every field is weeded. The children are in the fields at dawn, and there are no sounds at all but the hoes, which go up and down in time with one another like the oars of a very large, very slow boat.",
                        "* A woman looks up as you come in. She does not wave. She goes on working. She has a bandage on her hand that has been wrapped, you would say, for about a week.",
                        "@Maud| Visitors are welcome. We are well. Everyone is well. Thank you for asking.",
                        "* You had not asked. Dunn has gone to find his uncle, and you stand there, beside a woman who has been well for a week with a bandage the colour of old tea.")
                .at("farm", "Go to Lower Hobb, at {@farm}.")
                .outChoice(new Chapters.Choice("farmplan", "A whole village has stopped complaining. What do you tell them?",
                        "Tell them to stop drinking it.", "Offer to trade: honest pills for the bottles.", "Say nothing yet. Watch, and count what they are not saying.")
                        .after(
                                new String[]{"@Maud| And do what, when the harvest is in and my back is breaking? Sleep? I haven't slept right in six years. It was the first time I slept right, the spring that bottle came.", "* She is not angry. She says it like a fact about the weather. You find that you have nothing to say to it."},
                                new String[]{"@Maud| Pills for bottles. All right. But you'll want to be quick. The man comes on the first of the month, and the last time somebody turned his bottle away, the bottle came back, with a letter.", "* She does not tell you what was in the letter."},
                                new String[]{"* You count. It takes you the afternoon. Fourteen broken things, from a leg to a heart, and not one of them mentioned out loud. By evening you know the village by what it does not say, which is a kind of intimacy you would rather not have had.", "@Wen| You're doing the thing where you carry other people's silence around with you.", "@You| I'm not doing anything.", "@Wen| You are, and it's heavy, and I am going to sit next to you until you put it down."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("honest", "Ten Honest Pills").gap(2)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* There is no cure for a thing that is not a disease, but there is a way to wake a body up again. Wen finds it in the third of Hui's notebooks, the one you have been afraid to open. It is a long, patient recipe for something called a rousing draught, and in the margin, in a different ink, someone has written: 'Slow, or it hurts them.'",
                        "?farmplan=2|@Wen| Pills for bottles. Maud will keep her end. She seems like a woman who keeps her end.",
                        "?farmplan=1|@Wen| You told them to stop. They'll listen better if the thing you offer instead is not nothing.",
                        "?farmplan=3|@Wen| You counted fourteen. Brew fourteen, then, if you like. I'm told it's the reverse of counting sheep.",
                        "* Brewing is slow work, and it is not glamorous. Nobody would put it in a story. It is the same motion, again and again, in a room that smells of crushed leaves, with a boy in the corner who has learned not to talk while the cauldron is turning.")
                .made(10, "Brew the rousing draught: make ten pills for the village.")
                .outro(
                        "* The first one to wake up properly is Dunn's uncle. He sits up on the bed and looks at his broken leg as though someone has brought it in from the street. Then he says, to nobody, 'Oh, hell, that hurts.' Everybody in the room laughs, the kind of laughing that is mostly crying. It is the first complaint Lower Hobb has made in six months.",
                        "* Maud waits until the end of the day. Then she goes to the back of the house and comes back with something wrapped in a cloth, and she sets it down in front of you as if it might bite.",
                        "@Maud| This came with the bottles. They gave us a chit with every crate, because we aren't allowed to carry coin out of the village. It's stamped. You can have it. I don't want to know what it says.",
                        "* It is a little slip of green paper. It says, in a printer's neat type, BY ORDER OF THE GREEN TABLE, and beneath that the name of the Factor, and beneath that a number, which is the village's quota of silence for the year.")
                .pay(300, 0, 0, 0, 1, 0);

        d.chapter("hob", "Old Hob Likes the Quiet").gap(10)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The rousing draught works on twelve of the thirteen people you try it on in Upper Hobb, which has the same name as Lower Hobb, a worse road, and a shop that sells nothing but thread. The thirteenth is a man called Hob.",
                        "* He is seventy, and he sits on a bench in front of his house with his hands on his knees, looking at a view. It is a very good view: a hill, a stand of birches, a long gold slope of barley going down to a stream. He has had it for forty years. His neighbours say that since the spring he has not complained about anything, which is not like him. When Dunn holds out the cup he looks at it, and at Dunn, and says, quite kindly: 'No, thank you, lad.'",
                        "@Hob| It's the first time in nine years that I've sat on this bench and not minded. I'd like to go on.",
                        "@Dunn| But it's only the tonic. It isn't you. It's what it does to you.",
                        "@Hob| I know what it does. Do you know what it does? It lets a man look at a hill.")
                .made(4, "Brew a gentler draught for Upper Hobb: make four pills, slower than the last.")
                .outChoice(new Chapters.Choice("hob", "Old Hob will not take the draught.",
                        "Respect it: leave the cup on the bench, and sit with him.", "Wake him anyway. The tonic is a theft, even when he wants it.", "Ask him what he is not feeling, and let him say it.")
                        .after(
                                new String[]{"* You sit. Neither of you says anything for the whole of the afternoon. Wen comes with a flask. Dunn, who cannot bear a silence, hums, and finds that the hill does not mind."},
                                new String[]{"@Hob| You had no right.", "@You| No. I know.", "* He drinks it anyway, because you do not leave. The colour comes back into his face like a slow tide. He puts his hands over his eyes."},
                                new String[]{"@Hob| My wife painted that hill. Forty summers. Every one a bit different. She died in the spring, and the first bottle came the same week, and it was like being handed a very large, very soft coat.", "@You| And if you took it off?", "@Hob| Then I'd have to know she was dead."}))
                .outro(
                        "?hob=1|* At dusk he takes the cup off the bench by himself, and looks into it, and does not drink. 'Ask me next week,' he says. It is the first thing he has asked of anyone in a year. You write it in the book Wen keeps, under H, and underline it twice.",
                        "?hob=2|* By nightfall he has wept himself out and is eating bread and cheese in the kitchen with the lamp lit. He is very angry with you. He is furious. He will go on being furious for a month, and then he will send you a small, bad painting of a hill.",
                        "?hob=3|* He talks for an hour. It is mostly about the paint: the lead white, the burnt umber, the ground-up lapis she saved for a year to buy and used on the sky of one single picture. When he finishes, he reaches for the cup, and holds it, and says: 'Not today. But I know where it is.'",
                        "* On the road home, Dunn is unusually silent. At the crossroads he says: 'I wanted to be a hero. I thought it was mostly the fighting.'")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("kept", "What Orrin Kept").years(1)
                .setup((c, st) -> Chapters.spawn(c, st, "lisbet"))
                .intro(
                        "* You have heard the name Green Table before, once, in a margin of Orrin Vale's ledger, written very small. Back then it looked like a typing error.",
                        "?dead.orrin|* Orrin is in the ground, and the records are in the Court's hands, and nobody has been able to tell you who the Table was. Then the letter comes, in a hand that is not his, and it begins: 'You do not know me. I am the reason he did it.'",
                        "?!dead.orrin|* Orrin lives in a smaller house now, as he promised. He answers a letter from you after eleven days, which is the fastest he has ever answered anything. 'Come and see my daughter,' it says. 'She will not forgive me, and she is right. But she will talk to you.'",
                        "* It is signed Lisbet Vale. She is waiting at {@hide}.")
                .meet("lisbet", "Meet Lisbet Vale, at {@hide}.")
                .outro(
                        "* She is twenty-four, and she has her father's round cheeks and none of his ease. She keeps her back to the wall. She has a way of looking at a door as if it owes her money.",
                        "@Lisbet| Everyone assumed my father was greedy. He was in debt. The province had a bad harvest, and the Green Table lent him the money to buy grain, and took me as surety. I was fourteen. I was a ward in their counting-house for four years. I learned to keep books. I learned them very well.",
                        "@Lisbet| They said they only wanted one favour. Marrow Reach had a root that made the tonic. They wanted the tonic, a great deal of it, for the farms. A farm that does not complain is a farm that pays its rent. They called it a gentle policy. When your master found out and said he would write to the Court, there was only one more delivery to make, and I would have been free.",
                        "@Lisbet| One more year. That was what he said to himself, I think. He said it on the night of the fire, and he said it as he signed the order. He thought he was buying my life.",
                        "?!dead.orrin|* Behind her, in the corner, an old man you know sits on a stool with his hands between his knees, and does not look up. He has aged a decade in a handful of years.",
                        "@Lisbet| I got out. They'll want me back. They'll want what I took: the Table's second set of books. It lists every farm, every quota, and every magistrate they own. If it were read out in a public court, nobody would be able to pretend any more.",
                        "@You| And you want me to read it out.",
                        "@Lisbet| No. I want you to steal the rest of it. I only took half.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("orrinlast", "What Orrin Wrote").gap(6)
                .intro(
                        "?dead.orrin|* The packet arrives in the spring, by a courier in a green coat who will not say where he has come from. It is tied with a leaf-crest ribbon, and it is addressed to Lisbet. She holds it until the candle gutters before she opens it. Then she reads it without a word and passes it to you. It is Orrin Vale's, written in the last month of his life, and never sent.",
                        "?!dead.orrin|* The old man comes to the sect on his own two feet, with a stick, and a bag, and a coat that was a good one once. He has walked from his smaller house. He is seventy now, and bent, and his face has gone soft in the places where it was round. He stands at the gate for nearly an hour before he knocks.")
                .wait(1)
                .outro(
                        "?dead.orrin|* 'My dear girl. I write this on the nineteenth, in the hope of finding a way to send it. I have thought about the year I asked for. I want you to know I did not ask for it for you. I want to be honest. I asked for it for me. I told myself it was for you, because that was easier to bear. I had borrowed from the Table to cover a loss I could not explain to my wife. When she died, I went on borrowing, because I did not know how to stop. You were the pledge. I was the one in debt.'",
                        "?dead.orrin|* 'There is a name above mine, and I never learned it. A woman in grey, who comes only at the quarter-days, with a ledger. She calls herself the Treasurer. I think she is more than one person. I think the Green Table is only her left hand. Be careful what you ask for, Lisbet. The Marrow root was not the first thing they bought. It will not be the last.'",
                        "?!dead.orrin|* He sits in the kitchen with his stick across his knees. He tells it without being asked, in a low even voice. He tells it as a man reads a charge. He says that he is not here for forgiveness. He says that the Green Table has been to see him, twice, and that he refused, and that the second time they were not polite. He says he has a name.",
                        "?!dead.orrin|@Orrin| She calls herself the Treasurer. She comes at the quarter-days. She does not give a name. She has a ledger, and she writes in it while she talks, without looking down. I think she is more than one person. I think the Table is only her left hand.",
                        "* Lisbet is standing at the door with her arms folded. She has not spoken. She has the straight, white, shut face of someone who has been waiting for a particular sentence, and has just heard something else. She says: 'You could have told me.' He says: 'Yes.' It is the whole of the conversation. It is, Wen says afterwards, quietly, more than either of them had expected.",
                        "* The name goes in the book Wen keeps, under T, next to a drawing of a leaf. There are a great many names in the book now. This one has a box round it.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("pear", "Wen Under the Pear Tree").gap(6)
                .intro(
                        "* There is a pear tree behind the herb garden that has never fruited. It was there when the sect was a field of ash, a thin black stick in the middle of the char, and it was the first thing to put out a leaf. Wen sits under it most evenings, with a bowl of something to peel, and a knife that is a good deal sharper than the work needs.",
                        "* You sit down on the grass beside {wen.him}. You have done this a thousand times. Tonight {wen.he} puts down the bowl.")
                .wait(1)
                .outro(
                        "@Wen| I was never a disciple, you know. People think so. I was the herbalist who brought the mint on Tuesdays. Hui used to give me tea in a cup with a saucer. Nobody had ever done that. I thought there must have been some mistake, and I kept coming back to see if it would be noticed.",
                        "@Wen| After the fire I kept the stall for a year. I left the door unlatched every night. I told the neighbours it was for the cat. I thought, if the cloud-sleeve boy is alive, he'll need to be able to walk in.",
                        "* {wen.He} says it to the peel in {wen.his} hands. The knife goes round, and the long green ribbon comes away in one piece, as unbroken as a sentence.",
                        "@Wen| So when I say I don't mind about the plan, I want you to know that I'm not brave. It's that I've spent a year practising for you not coming back, and you did, and everything since has been a bonus. I'd walk into a counting-house for that. I'd walk into a lot of things.",
                        "* You find that you want to answer, and cannot. What comes out instead is that the pear tree has never fruited. 'It's a male,' says Wen, calmly. 'It needs another one. Someone planted it on its own.'",
                        "* You look at the tree. You look at {wen.him}. You say that it seems a pity. {wen.He} laughs, once, a short bright startled laugh. {wen.He} picks up the bowl again. There is a nursery in the next county, {wen.he} says, that sells a good female pear, and it would be a perfectly reasonable thing for two people to plant.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("rehearse", "The Plan, Version Two").gap(2)
                .intro(
                        "* There is a way to find the holes in a plan, and it is to act it out in a kitchen with the wrong people. You push the table to the wall. Dunn is to play the Factor, because he is the only one who is not afraid of her. Duchess is the ice sculpture. Lisbet, who has actually met the Factor, is to give directions, and has taken one look at the cow and sat down on a stool with her face in her hands.",
                        "@Lisbet| She doesn't walk like that. She walks as if she has never in her life been asked to wait for anything. Dunn, you are walking like a man looking for the privy.",
                        "@Dunn| I am looking for the privy. The scene's been going twenty minutes.")
                .wait(2)
                .outro(
                        "* By the fourth go, Dunn has got it. He has stopped walking like a farm boy and begun to move like something carved out of cream. He pauses in the doorway for exactly the right number of seconds. He looks round the room as if it were a small mistake he was prepared to forgive. When he speaks, it is in a low pleasant voice, and he says: 'A cousin from the provinces. How refreshing.'",
                        "* There is a silence. Lisbet takes her hands from her face. She goes quite white. 'Do that again,' she says, and he does, and she says: 'No, don't. It's exactly her. I can't.'",
                        "@Wen| He's very good.",
                        "@Dunn| I think I might have been a duchess in another life.",
                        "* Duchess, who has been standing in the middle of the kitchen as an ice sculpture for the whole of the evening and has not moved once, lowers her great head, and with enormous gentleness, eats the plan.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("quayturn", "Pettibone Quay Changes His Mind").gap(4)
                .intro(
                        "* The salesman turns up at the sect gate on a wet evening with his cart, which is not clean any more, and a bottle in each hand. He has been walking for four days. He has the look of a man whose coat has been taken away and given back with something missing from the pockets.",
                        "@Quay| I should like to speak to the person who gave Mrs. Maud Hobb of Lower Hobb a bottle of something that wasn't a tonic. Is it you? I have been trying to find her for a month. She sent a letter. It said, quite simply, I can feel my hands. Nobody has said anything like that to me in twenty years.")
                .wait(2)
                .outro(
                        "* He sits in the kitchen with his hat on his knees and tells it badly, out of order, with the care of someone laying a table he has not laid before. He was a seller of hats, then of buttons, then of anything that could be put in a bottle. He took the Verdigris post for the pay. He did not ask what was in the tonic, because it was labelled, and because he had a wife in the south who was ill.",
                        "@Quay| I have not been a good man. I would like to be considered a cheerful one. They are different, I know. But on the road from Lower Hobb to here, I counted. I have sold nine thousand four hundred and twelve bottles. I have a ledger. I wanted to know what I'd done, and I did the sums, and when I had finished I sat in a ditch.",
                        "* Wen puts a bowl of soup in front of him without a word. He looks at it. He picks up the spoon as if it might go off. Dunn, who has said nothing at all, sits down next to him on the bench. The first thing, he says, is to eat. The second is to write it down. The third is to find out which villages. He says it exactly as the sect's old cook used to say it to the apprentices, when they had spilt something.",
                        "@Quay| I have the route. I have every farm on it. I have the order of delivery, and the dates, and the name of every steward who signed. I should like to give it to somebody who can do something about it. I'd go myself, but I'm told I'm not trusted. I can't say I blame them.",
                        "* He hands it over, in a leather book with a strap. It is worn at the corners. It is the most thorough thing you have ever seen: a hundred and eleven villages, in a clerk's cramped ink, each with a number beside it. The number is the quarter's quota of silence. Every one of them has been added up, and the total is underlined in red.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("plan", "A Plan, Told Twice").gap(8)
                .intro(
                        "* The Green Table holds a gala once a year at its counting-house in {@gala}, to which the best-behaved magistrates of the province are invited. There will be music, and an ice sculpture of a plough, and the Factor herself in a gown with a train. And in the strongroom under the dance floor, behind a door that opens to one particular key, the other half of Lisbet's books.",
                        "* The plan is Wen's, and it is a beautiful plan. You sit around the kitchen table, with the cat on the map to keep it flat, and listen to it twice.",
                        "@Wen| One. Dunn comes in as a waiter. He is the one person here who looks harmless. Two. Lisbet is on the guest list under her mother's name, because they've never struck it off. She waits near the stairs. Three. You are the cousin from the provinces, and you are bored. Everyone believes in a bored cousin.",
                        "@Wen| At the sixth bell, the ice plough falls over. That is a thing I will arrange. In the confusion, you go down to the strongroom, and Lisbet has the key, and it takes four minutes. Four. Not five. We are out the garden door before the first guest has been told to calm down.",
                        "@Dunn| What am I doing with Duchess?",
                        "@Wen| Duchess is staying here, Dunn.",
                        "@Dunn| She'll be ever so upset.")
                .at("gala", "Go to the Green Table's gala, at {@gala}.")
                .outro(
                        "* The plan, as it turns out, was a thing that happened in the mind. In the room, the ice plough was delivered a day early and has started to melt across the dance floor. The waiters' uniforms were not cut for Dunn's shoulders. A magistrate who knew Lisbet's mother has recognised the name on the guest list and told everyone near him. And at the sixth bell the Factor herself, Ysolde Marr, in a green gown with a train that follows her like a rumour, walks up to you and offers you her arm.",
                        "@Marr| A cousin from the provinces. How refreshing. Walk with me, cousin. I want to tell you what you are really doing here, and I would like to do it where we cannot be overheard.")
                .outChoice(new Chapters.Choice("gala", "Ysolde Marr has your arm, and the strongroom is a floor below. How do you get the ledger?",
                        "Charm her. Keep her talking while Lisbet works the lock.", "Let Dunn's mistake become the distraction: the cow, and everything the cow implies.", "Skip the cleverness. Go down the stairs and take it.")
                        .after(
                                new String[]{"@Marr| You are a fine liar. It is a pity that I am a better one. Still, it has been a long time since anybody tried flattery on me with such conviction. Let us see how far it goes.", "* It goes four minutes. It is exactly the four you needed, and you will not forget how you earned them."},
                                new String[]{"* Dunn, who is not a waiter, and who has not been told what to do with a cow, has done the single useful thing of his career: he has brought Duchess in through the garden doors. By the time the first magistrate has finished screaming, the gala has become a rodeo, and nobody has noticed the stairs.", "@Wen| I did not plan for the cow.", "@You| Nobody could have."},
                                new String[]{"* The two guards at the head of the stairs are good at their jobs, and quite surprised. It is not a stealthy thing. It is the kind of thing that ends in the third act, with a lot of broken furniture, and the Factor's long, delighted laugh behind you."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("comeback", "Duchess").gap(1)
                .intro(
                        "* There is a complication that the plan did not allow for, and it is in the doorway of the strongroom, eating a bale of the Factor's good cream-coloured velvet.",
                        "* It is Duchess. She was told to stay at home, and did not. She followed the cart for eleven miles in the dark, came in behind the ice sculpture by the garden door, and has made her own arrangements. She turns her large placid head toward you and chews. The velvet is the colour of the Table's crest. It is, as far as you can tell, an heirloom.")
                .wait(1)
                .outro(
                        "@Wen| I'm so sorry. I'm so very sorry. I told them. Dunn, I told you.",
                        "@Dunn| She's got a mind of her own.",
                        "* Thole stops in the corridor. He looks at the cow. He looks at the velvet. He has been a bodyguard for thirty years, and has been shot at, and stabbed, and once set on fire, and there is nothing in his training for this. He opens his mouth. Nothing comes. He closes it. He says, in a voice of immense, slow, bewildered gravity: 'That is a cow.'",
                        "@Lisbet| She's very well behaved.",
                        "@Thole| She is eating the Factor's curtains.",
                        "* It is the one moment in your life you will see a man of that size consider the possibility that he might be in the wrong building. Duchess finishes the velvet, burps gently, and lies down across the doorway to the stair. She is not moving. There is nothing in the world that will move her. Thole, after a pause, takes off his hat.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("vault", "The Counting-House").gap(1)
                .setup((c, st) -> Chapters.spawn(c, st, "thole"))
                .intro(
                        "* The strongroom has a door as thick as a table and a lock that opens to a particular key, and Lisbet has it, on a chain around her neck, where she has kept it for six years. The door swings. Inside there are shelves, and on the shelves the other half of the books.",
                        "* Between you and the shelves stands the Factor's bodyguard, Corvin Thole, a quiet man the size of a barn door, who has the manner of someone who has never been surprised in his life and is looking forward to it.",
                        "@Thole| The Factor says nobody comes out. I don't ask why. I am paid to count the people who go in.",
                        "@Lisbet| He taught me how to hold a pen.",
                        "@Thole| That I did. Walk away from it, miss. I will make it quick.")
                .defeat("thole", "Face Corvin Thole in the strongroom, and win.")
                .outro(
                        "?dead.thole|* He goes down like a wall. The silence afterwards is the loudest thing you have ever heard. Lisbet kneels by him for a moment, with her hand over her mouth, and then gets up and starts to fill a sack with books.",
                        "?!dead.thole|* He goes down on one knee, and you do not finish it. He looks up at you with something that might, in a more expressive man, have been gratitude. 'She was a good bookkeeper,' he says. 'The Factor will have my hide for this. Tell Lisbet to lock the door behind her. I won't follow.'",
                        "* The books are heavy, and they are lovely, in a terrible way. Every farm, every quota, every magistrate with a price beside his name. Wen reads for a minute in the lamplight, and then closes the cover very carefully, the way you shut a door on a sleeping child.",
                        "@Wen| There are four hundred villages in here.")
                .pay(2000, 0, 0, 0, 0, 1);

        d.chapter("quiet", "Quiet, Please").gap(8)
                .intro(
                        "* The books go to the Provincial Court by three separate hands, and one of them is a magistrate who has just discovered how much of his income was owed to an upright citizen named Marr. It takes a month. Then it takes the whole province by surprise, which is a hard thing to do to a province.",
                        "* The Green Table does not collapse. Institutions of that kind never do. But it becomes very quiet about itself. Ysolde Marr is seen in a coach at the border, and then she is not seen at all. Bottles of the Silent Tonic go on sale in a hundred markets for a week, at half price, and then they stop, because no one wants to be seen buying them.")
                .wait(4)
                .outro(
                        "?lisbet_free|* Lisbet comes to stay for a season, and stays for six. She takes over the sect's accounts. In the first month she finds four mistakes. In the second she finds a fraud, which turns out to be Dunn's, and which turns out to be a cow he has been paying for out of the sect's treasury. She cannot stop laughing. It is the first time you have heard it.",
                        "* On the evening the last of the news comes in, you and Wen sit on the roof of the kitchen with a bottle of Maud's cider. You can see the herb garden from here, and Hui's stone, and the lamp in the window of the room above the noodle shop that you shared once, when you had nothing, and thought that was all you would have.",
                        "@Wen| I've been meaning to say something for three years. I've been meaning to say it every evening. I've rehearsed it on the cat.",
                        "@You| What did the cat think?",
                        "@Wen| The cat didn't think anything of it, which I took as encouragement.")
                .outChoice(new Chapters.Choice("love", "Wen has been waiting three years to say something.",
                        "Say it first: that you would like to stay, for good.", "Let Wen say it, and answer honestly.", "Say you are not ready, and that you would like to be.")
                        .after(
                                new String[]{"* It comes out of you in about nine words, none of them good, and it turns out to be exactly enough. {wen.He} laughs, and then {wen.he} stops laughing, and then neither of you says anything at all for a little while, which turns out to be the best conversation you have ever had.", "* Down in the yard, the cat takes up its post on the wall. It has been waiting for this."},
                                new String[]{"* {wen.He} says it. It is not eloquent. It begins with a word about mortars and ends with a word about the roof. You tell {wen.him} what you would say, which is the same thing in a different order, and the cider goes warm in the bottle while you talk.", "* Down in the yard, the cat takes up its post on the wall. It has been waiting for this."},
                                new String[]{"@Wen| That's all right. I've waited three years. I can wait a little more. I just wanted you to know there's a door, and it's open, and I'm the sort of person who can stand in a doorway through a whole winter.", "* You believe {wen.him}. That is the thing about Wen. You always do."}))
                .done((c, st) -> {
                    Character l = Chapters.person(st, "lisbet"); if (l != null && !l.dead) Chapters.set(st, "lisbet_free", 1);
                    if (Chapters.flag(st, "love") == 1 || Chapters.flag(st, "love") == 2) { Chapters.bond(c, st, "wen", 3); Chapters.wed(c, st, "wen"); }
                })
                .pay(5000, 0, 0, 2, 0, 1);

        d.interlude(2, "Five Years Later",
                "* Azure Cloud has sixty beds, a proper roof, and a reputation. The reputation is for being the sect whose pills cure what the guild's pills only quiet.",
                "?e.spouse|* Wen is with you in the mornings now, and in the evenings, and there is a second toothbrush on the shelf over the basin that nobody thought to count, and nobody has complained.",
                "?!e.spouse|* Wen is still the person you tell things to, last thing at night, through a door that stays open.",
                "?dead.thole|* There is a small stone for Corvin Thole beside the kitchen steps. Lisbet put it there. She says it is the only stone in the sect that doesn't have a story attached, and she wants to keep it that way.",
                "?!dead.thole|* Corvin Thole came to the gate one winter, alone, and asked if the sect needed a man who could lift things. It did. He teaches the children how to fall.",
                "* The cat is a very old cat. Duchess is a very old cow. The cauldron sits in its new place of honour, and the people who live around it do not talk much about how long it has been since anyone used it.",
                "* One morning, a young man comes up the road with a basket on his arm, and a letter in his sleeve, and the nervous manner of someone who has been told to be polite. He stops at the gate. He looks up at the crooked sign. The letter is sealed with a candle, put out.");

        // ================================================================== SEASON 3
        d.season(3, "What the Cauldron Is For");
        d.place("kitchen", "start");
        d.cast("ambrose", "Ambrose", "Pell").he().keep().trait(Character.Traits.Alchemist).side("Good").lvl(0).age(20).at("here").rel(1);
        d.cast("snuff", "Mister", "Snuff").he().trait(Character.Traits.Leader).side("Evil").lvl(7).pot(9).age(60).at("far").rel(-3);

        d.chapter("wick", "A Wick at the Gate").years(5)
                .setup((c, st) -> Chapters.spawn(c, st, "ambrose"))
                .intro(
                        "* The young man is called Ambrose, and he is a Wick of the Outer Court, which means that at the Palace of the Oldest Order he is a candle somebody has not got round to lighting. He has walked eleven days with a lunch his mother packed, and he is, in a way he is clearly trying to hide, terrified of you.",
                        "@Ambrose| I have a message. It's from the, well, from the Master of the Palace. I know he doesn't have a name. He didn't give one. He wrote this himself, which the Elders say has never happened, and I was told to say please.",
                        "* The letter is short. It is in a hand that looks like somebody learning to write with their off hand. 'I am not allowed to say why. I am allowed to ask. Please keep the cauldron safe. Please do not let anyone take it. I am sorry about the please. I do not have many people to ask. ???'",
                        "@Wen| He signed it with three question marks.",
                        "@Ambrose| He does that. We all just call him Master. It's easier.",
                        "* The cauldron is where you left it, in the front hall, by the door. It has sat there for years, and the crack in its belly has never grown, but this morning the light coming through it is a slightly different colour, as though the crack were looking out at something.")
                .meet("ambrose", "Hear what Ambrose has to ask of you.")
                .outro(
                        "* Ambrose will not say much more. He does say that Master Hui was a Wick once, long ago, in the pill kitchen of the Palace, and one of the best. He left on a night nobody talks about. The cauldron went with him, which is not the sort of thing that is meant to happen to a Palace's cauldron.",
                        "@Ambrose| They say he left because he found out what the kitchen cooked. I'm not supposed to know that. I don't know what it was. I only know the pills in the Outer Court all taste a bit like pennies, and nobody asks.",
                        "* You think about a small, pale man in a herb garden, patient as a stone, who said that a cauldron learns the hand that stirs it. You think about what he might have learned.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("unpack", "Ambrose Unpacks").gap(6)
                .intro(
                        "* Ambrose has never had a room of his own. At the Palace the Wicks of the Outer Court sleep in long rows, thirty to a hall, with their boots under their beds, in a smell of old straw and boiled cabbage. The room the sect gives him is the one over the stable. It has a window, a bed, a peg, and a mouse. He stands in the doorway with his bundle in his arms and looks at it as though it might be taken back.",
                        "@Ambrose| Is it all mine? The whole of it?",
                        "@Dunn| It's got a mouse. You'll want to talk to the mouse. He's reasonable.")
                .wait(2)
                .outro(
                        "* He learns the sect in the way a person learns a language, by mistakes. He calls Wen 'honoured elder' and is told to stop. He bows to the cat. He eats the whole of a dumpling in two bites, at his first meal, and looks up in alarm, and finds that the whole table has been waiting for him to do it.",
                        "* Dunn teaches him to milk Duchess. It takes an afternoon and goes as these things do. At the end, Ambrose is covered in milk to the ears and Duchess wears a fixed look of long-suffering dignity. Dunn, doubled up on an upturned bucket, says it is the best thing he has seen since the cow came to the gate.",
                        "* That night Ambrose writes to his mother on a page torn from a recipe book. You see it later, left open on the peg. 'Dear Mother. I am well. I have been given a bed. There is a mouse in it. The people here eat at the same table as their Master. I do not know what to make of it. I have decided to make a great deal of it. Your loving son, A.'")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("cellar", "The Cellar Under the Grave").gap(10)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The herb garden has a loose flagstone, you remember it now, by Hui's stone, where you have knelt a hundred times. This time you lift it. Underneath there are eleven steps going down and a cellar that smells of dry paper and old root.",
                        "* He has left you a library. There are thirty notebooks, a lamp that lights itself when you step on the sixth stair, and a long table with a place laid at it, for one, with a cup and a cloth. It is the neatest thing you have ever seen. It is so much like him that you have to sit down on the stairs.",
                        "* The first notebook is a recipe book. It is called, on the spine, in his handwriting, THINGS THAT HURT NOBODY. Each recipe has the same note at the bottom: 'Nothing here came from the dead.' He has underlined it twice.",
                        "@Wen| He must have been trying to make up for something. A whole life of it.",
                        "@Ambrose| Do you think he would mind if I read?",
                        "@You| I think he would mind if you didn't.")
                .made(12, "Cook from Hui's book: make twelve pills with nothing in them that hurt anybody.")
                .outro(
                        "* The twelfth is the first that works. You feel it before you see it: a faint warmth in the iron, a sound in the crack like someone breathing out. The pill that comes out is the colour of the sky at the end of an afternoon. It tastes of absolutely nothing, which you gradually realise is the point.",
                        "* In the last notebook, tucked in the back cover like a pressed flower, there is a single sheet. 'The Palace kitchen cooks the dead. Not cruelly. They are brought up from a place under the floor, and a pill is made, and the pill is given to the living, to help them climb. I was a cook there for nine years before I understood. I took the cauldron because it is the one that does it. I broke it so that it could not. If I am wrong, forgive me. If I am right, then the crack is not damage. It is a door.'",
                        "@Ambrose| I didn't know. I swear I didn't.",
                        "@Wen| None of us did. That's the sort of knowing that doesn't let you sit down for a bit.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("huiletter", "What Hui Cooked").gap(6)
                .intro(
                        "* The last notebook has a letter in the back that is still unread. You found it on the second night and put it away, the way you put away a thing you are not yet strong enough to hold. Now you take it out and read it at the long table in the cellar, with Wen on one side and Ambrose on the other and the lamp that lights itself turned low.",
                        "* It begins: 'To whoever stirs next.'")
                .wait(1)
                .outro(
                        "* 'I do not know your name. I hope you are kind, and I hope you are stubborn, and I suspect you will be the second more than the first. I was a cook in the Palace kitchen for nine years. I was good. I never asked what the pills were for. A cook does not. A cook asks whether the salt is right.'",
                        "* 'On the night I understood, I was alone in the kitchen with the big pot on and the pipes humming, and a man came down the corridor with a lamp. A plain man in an apron, who was not supposed to be there, who is never supposed to be anywhere. I knew who he was. Everyone does, and nobody says. He stood at the door. He looked at the cauldron, and at me, and at the hammer in my hand. I thought: he will stop me.'",
                        "* 'He put out his lamp. That is all. He stood in the dark in the corridor and he let me go by with the pot on my shoulder, and he did not say a word. I have thought about it every day for forty years. I do not know what it cost him. I know that he is the loneliest person who ever lived, and that he did this one thing on his own.'",
                        "* 'If you are reading this, he may have asked you for the cauldron. If he asks, say yes. If he does not, say yes anyway, and tell him I never forgot the lamp. Hui.'",
                        "@Ambrose| He put the lamp out.",
                        "@Wen| He did.",
                        "@Ambrose| I thought it was just a thing they said. I thought it was a story, about the Master being kind.",
                        "* You close the notebook. The lamp on the table, which lights itself, burns a little brighter, as if somebody had come in from the corridor.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("snuff", "Mister Snuff").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "snuff"))
                .intro(
                        "* He comes to the gate on a wet evening, a tall and very tidy man in a grey coat and a hat, carrying a long brass instrument that looks like a candle-snuffer, and which is not. He has the face of somebody who has been very polite his whole life, because it saved him effort. He knocks, three times, with the knuckle of one finger.",
                        "@Snuff| I do hope I'm not intruding. I have been asked to collect an object. I've been told you have it. A cauldron, cracked, with a certain sort of seam. It isn't yours, you understand. It was never anyone's. It was lent, once, to a person who should have been more careful with it.",
                        "@You| Who asked you?",
                        "@Snuff| Oh, I couldn't say. They don't have a name. They have a talent for making rooms go quiet, and for not asking twice.",
                        "* The air has the feeling of the end of a candle. When he smiles, it is like watching a window go dark one pane at a time.")
                .meet("snuff", "Speak with the man at the gate.")
                .outro(
                        "* He is very civil. He declines tea, and accepts a stool. He explains that the thing in your hall is dangerous to everyone, including its owners, because it has a hole in it, and the hole goes somewhere, and the place it goes is looking back. He says it as a kindness. He says it as if you were a child with a lit match.",
                        "@Snuff| I am not here to harm you. Please believe it. I am here to put the lamp out before it burns the house. You may keep your sect, your friends, and every pill you like. I will leave you the garden. I only want the iron.",
                        "* You look at the cauldron. You look at the crack, and the light coming through it, which has gone from the colour of the end of an afternoon to something pale and a little afraid. It seems, you think, to be listening.",
                        "@You| I'll think about it.",
                        "@Snuff| Of course. I shall wait. I am extremely good at waiting. It is, in fact, rather the whole of my profession.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("kitchen", "The Pill Kitchen").gap(6)
                .intro(
                        "* If you want to know what a cauldron is for, you can ask the cauldron's makers. The Palace's pill kitchen is on the lowest floor of the Outer Court, behind a door nobody notices, and it is the cleanest place you have ever seen. The floor is stone. The pans are polished. Along the north wall, a row of iron pipes goes down into the dark.",
                        "* Ambrose has got you in on a delivery, in a sack of onions. It is the first time you have laughed since the gate.",
                        "* The Palace does not ask your name. The cook on duty, a thin woman with her sleeves rolled up, takes one look at you and the onions and says, 'The soup's in the pot,' and walks away. It is a very good soup.")
                .at("kitchen", "Go to the Palace of the Oldest Order, at Originis, and see its kitchen.")
                .outro(
                        "* He is sitting at the cook's table when you look up, as if he had always been there. He is an ordinary man, of no age, in plain clothes, with a bowl in front of him. He holds the spoon the way a person holds a thing he has just learned to use. You know him at once. You have never seen him in your life.",
                        "@???| I am not allowed to tell you who I am. That one I can keep. I can tell you that I like the soup. It is very good, isn't it? I have it every day. It is one of the things I like most about being here.",
                        "@???| I can show you the pipes. I am allowed to show. They go down to the place where the Garden puts everyone it has finished with. People ask, sometimes, what happens to the dead. The answer is that they are used. It is not unkind. It is not kind either. It is a system. I am the system's reflection. I did not choose it.",
                        "@You| Master Hui took the cauldron.",
                        "@???| He was my best cook. I did not stop him. I want you to know that. I could have, and I stood in the corridor with the lamp out, and I let him go past. I have never told anyone. I think it may have been the first thing I ever did on my own.",
                        "* He puts down the spoon. For a moment there is a shift in the room, a quiet slippage, like a sleeper turning over.",
                        "@???| Mister Snuff is not mine. Mister Snuff is from the other side of the sky. If you give him the iron, I do not know what he will do with it. If you do not, I do not know what he will do to you. I am sorry. I have a great many things to be sorry about, and I seem to have collected a new one.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("water", "Cistern Water").gap(6)
                .intro(
                        "* On the way back you carry a jar of something ??? gave you, a little water drawn from the place at the bottom of the pipes, and you are too frightened to look at it. It is not red or black. It is clear and still, and it has a faint, familiar taste, if you could bring yourself to taste it, of bone and cold and something like regret.",
                        "@Wen| He said we should decide what it's for. As if there were a decision.",
                        "@Ambrose| There's always a decision, isn't there? My mother used to say that every pot is a decision about dinner.",
                        "* The cauldron sits in the hall with the crack looking up at you. The jar sits on the table. It is the kind of choice that makes the room feel too small to hold it.")
                .choice(new Chapters.Choice("seam", "What is the last brew for?",
                        "Open the seam and let the dead out: whoever is down there, whoever wants to go.",
                        "Close the seam for good, and keep the Garden's dead where they are, safe from the other side of the sky.",
                        "Send a message through the seam: tell whoever is on the other side that the Garden is not empty.")
                        .after(
                                new String[]{"@Wen| That's the one he'd have wanted, isn't it? Open the door, and let them all walk out?", "@You| I think so. I think he'd have hated himself for it, too."},
                                new String[]{"@Ambrose| It would be safe. Nobody could get at them. But they'd never leave.", "@You| Some of them never wanted to."},
                                new String[]{"@Wen| That's the brave one. I don't know whether it's the clever one.", "@You| Hui would have said they are the same, when it comes to it. He would have been wrong, but he'd have said it."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("parley", "A Cup of Cold Tea").gap(2)
                .intro(
                        "* He sends a note first. It comes on a salver, by a small grey boy, who sets it down on the kitchen table and goes away without being tipped. It is written on very good paper in a very upright hand. It says: 'I should like to explain myself before I am obliged to be rude. I shall be at the old stone bridge at dusk. I shall bring my own tea. Snuff.'",
                        "@Wen| You're not going.",
                        "@You| I am going.",
                        "@Wen| Then I am coming and standing behind a tree.")
                .wait(1)
                .outro(
                        "* He is sitting on the parapet with a flask and two tin cups, in his grey coat, with his hat beside him. The light is going. The stream under the bridge is making a small quick noise. He pours the tea without a word, and holds out a cup, and you take it, because it would be graceless not to.",
                        "@Snuff| It is cold. I apologise. I find it keeps better. I am told that the dead prefer it.",
                        "@Snuff| I should like to tell you what I am. It will save a good deal of unpleasantness. I do not belong to a country, or a creed. I belong to a very large and very old and extremely tired company, which has asked, over the course of a long time, a single question: whether the Garden is worth the trouble.",
                        "@You| And you decided that it isn't.",
                        "@Snuff| I did not decide. I was given a lamp-snuffer. I was not given the wish to be cruel. I put out the ones that are about to be put out in any case. That is all I do. I would be glad if you would believe it.",
                        "* You drink the tea. It is very bad. Behind the willow, forty yards off, there is a small noise like someone stepping on a twig and trying to look as if they hadn't.",
                        "@Snuff| What I cannot tell you is why the cauldron matters. I am not told. I am told only that it is a door, and that doors are the thing we are paid to close. It is not malice. It is, if you will believe me, a sort of housekeeping.",
                        "@You| Housekeeping.",
                        "@Snuff| It is a very large house. It has been open a very long time. Someone must go round at night, and see to the windows.",
                        "* He finishes his tea. He shakes out the cup. He puts it back in the flask, and the flask in his coat, and stands, and bows. 'I shall come on Thursday,' he says. 'I shall try to be brief.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("tailor", "The Last Visitor").gap(2)
                .intro(
                        "* Mister Snuff does not wait for an answer after all. He comes at midnight, in the rain, with the brass snuffer in his hand, and he is no longer being polite. The lamps in the sect go out one after another down the corridor, as if one enormous breath were being let out.",
                        "?!dead.wen|@Wen| Behind me. Both of you. I'll hold the door for as long as it takes to think of something else.",
                        "@Ambrose| I don't want to be brave. I'd like it noted.",
                        "* The cauldron is humming. The crack has begun to glow, white at the edge. You understand, in a flash, what Hui understood: it has been listening all this time, and it wants to be used.",
                        "@Snuff| You have one chance to hand it over. It is a generous chance. I have not extended it to a living soul in a century.")
                .defeat("snuff", "Face Mister Snuff and win.")
                .outro(
                        "?dead.snuff|* When he falls, the lamps come back on, one by one, all down the hall, as though a hand had gone along and lit them. His snuffer rolls across the flagstones and stops against the cauldron, which rings once like a struck bell.",
                        "?!dead.snuff|* He steps back at last and puts away the snuffer, and for the first time there is something almost human in the line of his mouth. 'You are very like him,' he says, to the iron. 'Another time.' Then the lamps come back on, one by one, down the hall.",
                        "* Nobody speaks. Wen's hand finds yours in the dark, and stays. Ambrose sits down on the floor with his back against the wall, and says, 'I'd like it noted that I did not run,' and nobody notes it, because everyone knows.")
                .pay(0, 0, 0, 1, 1, 0);

        d.chapter("eve", "The Night Before").gap(2)
                .intro(
                        "* Nobody sleeps. It is the kind of night when the sect, which is usually a noisy place, goes quiet in patches, like a house where someone is ill. Dunn has written his will on the back of a menu and left it to the cow. Lisbet has done the accounts for the next fifty years. Ambrose has lit a lamp in his window and is not looking at it.",
                        "* You find yourself on the roof of the kitchen with a bottle of Maud's cider, as you did once before. This time the stars are out, and there is no news to wait for. Wen comes up the ladder at midnight and sits down beside you without a word.")
                .wait(1)
                .outro(
                        "@Wen| I used to think a sect was a building. Then I thought it was a person. I've been looking at the garden for an hour and I've decided it's neither. It's the sound of everybody being in the same house and nobody being sure what the noise is.",
                        "* You pass the bottle. Below you, in the yard, the cat is sitting on the cauldron's rim with its tail hanging inside. The crack in the iron is lit from within, faint and steady, a thin white line like the edge of a door left ajar in a dark house.",
                        "@Wen| He was a quiet man. Hui. He wasn't at all what you'd call brave.",
                        "@You| He put a crack in a cauldron and walked out of the Palace.",
                        "@Wen| That's what I mean. Brave people do it loudly. He did it with a hammer, in the dark, and then he went to bed.",
                        "* The cider is warm, and the roof is cold, and a long way off, in the hills, an owl says its one note. You sit until the sky begins to go grey. When the first bird starts, Wen gets up, and brushes the roof-grit from {wen.his} coat, and says: 'Well. We had better make something.'")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("vote", "A Show of Hands").gap(2)
                .intro(
                        "* The cauldron is not yours. This is the first thing that is said at the meeting, and it is said by Dunn, who has never in his life said anything first. He stands up on the bench in the sect hall with his hands in his sleeves. It seems to him, he says, that if it is going to be used for what it is going to be used for, then everybody ought to have a say.",
                        "* So the sect sits down together. All seventy-one of them, on the benches and the floor and the window ledges, with Ambrose at the back looking as if he might be sent away at any moment, and Lisbet taking minutes, and Wen at the stove. There is one item on the sheet. It says: WHAT WE SHOULD DO WITH A DOOR.")
                .wait(2)
                .outro(
                        "* The debate takes four hours. A girl of twelve, who has been at the sect for a month, points out that the cauldron has been in the hall for years and no one has ever asked it what it wanted. A grey old pill-grinder says that in his experience a door is a thing that you either shut or open, and that a person who stands in it is a draught. A boy of nine asks if the dead have to go anywhere, and is told by an old woman at the back, quite gently, that some of them would rather not.",
                        "@Wen| Hui would have hated the meeting. He'd have gone to the garden. And then he'd have come back at the end and said something short, and we'd all have felt rather silly.",
                        "* The vote, when it comes, is not on the question that was set. Dunn proposes it. He proposes that the sect decide not what to do with the door, but how to decide. Whoever stirs the cauldron should be allowed to choose, he says, and the rest of them should stand behind that person and hold the lamp.",
                        "* It is carried by a show of hands, seventy to one. The one is Ambrose, who has not voted for anything in his life, and has put up his hand by mistake, and cannot work out how to put it down. Duchess, at the back, who has been allowed into the hall for the occasion, abstains.",
                        "@Lisbet| I shall record it as unanimous.",
                        "@Ambrose| I didn't mean to!",
                        "@Lisbet| That's what unanimous means.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("brew", "The Last Brew").gap(2)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* You cook it that night, in the cracked cauldron, from Hui's last page and a jar of water from under the floor. It takes until the sky turns grey. Ambrose stirs when you cannot, and Wen feeds the fire, and nobody says a word. There is only the sound of the spoon against the iron, going round, like a very small clock.",
                        "?seam=1|* The brew is the colour of the road at dusk. It smells of rain on a floor, and old clothes, and something sweet underneath, like a name remembered. The crack opens along its length like an eye.",
                        "?seam=2|* The brew is the colour of a closed shutter. It smells of dust and honey. The crack draws shut along its length with a sound like a sigh the size of a house, and the iron, for the first time in a century, is whole.",
                        "?seam=3|* The brew is the colour of a bell. It smells of ink. The crack opens a hand's width, and out of it, as faint as a voice across a lake, comes a sound you have not heard before and recognise at once: someone, very far away, answering.")
                .made(1, "Finish the last brew: make one pill.")
                .outro(
                        "?seam=1|* They come out the way smoke goes up a chimney. You cannot see them, only the room getting a little larger and a little lighter, again and again, until the last one has gone. At the very end you think you hear Hui say, mildly, 'Well, that took long enough,' and then there is only the fire.",
                        "?seam=2|* The Garden's dead go on sleeping. You feel it, afterwards, in the way the whole sect sleeps better: not drugged, just lighter. Hui would not have called it a victory. He would have said that a cauldron should hold a thing, and that this one finally does.",
                        "?seam=3|* The message goes out. It is nine words long, and it was Ambrose's idea: 'Someone is cooking here. Come and eat.' It takes three days to be answered, and the answer, in a hand you do not know, is a single mark on the iron, like a bird's foot, or a name.",
                        "* The cauldron is still cracked. Ambrose polishes it for an hour, and nobody tells him to stop.")
                .pay(10000, 0, 0, 3, 1, 1);

        d.epilogue(
                "* Azure Cloud is still not a great sect. It has seventy beds, a roof that leaks in exactly one corner, and a hall in which an old iron cauldron sits by the door with its crack in the light.",
                "?seam=1|* On certain evenings you can hear them leaving: a murmur in the iron like a crowd leaving a theatre. Nobody has ever found a way to count them.",
                "?seam=2|* The cauldron is whole. The pills it makes taste of nothing at all, which the children think is a flaw and the old people think is a miracle.",
                "?seam=3|* On the fifth day of every month a small mark appears on the iron overnight. Ambrose copies it into a book. He says he's almost sure it is a language. Nobody has been brave enough to answer it twice.",
                "?cauldron=1|* The seam in the old repair shines like a scar that healed well, and lets the light through anyway.",
                "?dead.snuff|* Mister Snuff's snuffer hangs in the kitchen, on a nail, where it is mostly used for candles.",
                "?!dead.snuff|* Once a year a tall man in a grey coat is seen at the end of the road, looking at the sect from a distance. He never comes in. You leave a bowl of soup on the wall. It is always empty by morning.",
                "?e.spouse|* Wen is the person you eat with, and argue with, and wake up beside. When people ask how it began, you tell them: with a cat.",
                "?!e.spouse|* Wen is the person at the end of the table. The door between you stays open. You have both stopped being in a hurry.",
                "* Master Hui's stone sits in the herb garden, and every spring a blue flower grows through the crack in it that nobody planted. You tell him how it went. This time, you think, he is listening.");
        TaleHeirMore.add(d);
    }
}
