package Game;

/** The Wandering Immortal's Disciple, seasons 2 and 3 (see "Start Stories Beat Sheets."). The author asked that the vision, direction and premise be left alone; these seasons only walk further down the same road.
 *  Season 2, "The Fourth Move" (pastoral and fable, small and tender): the old man said there was no fourth move. The human has a student of their own, Nettle, and learns what a first pair of hands is for.
 *  People remember Wen Jiu under other names; each tale is a fable with a moral that is slightly wrong. Flags: nettle (how the human answered her), name (what Nettle's own answer to the old question was), song (the lullaby).
 *  Season 3, "Tell the Quiet Man" (quiet and cosmic): deliver the message ("I found the crack, and I'm sorry I'm late") to the Master of the Palace. The Master, who is only the image of the Gardener, was once a boy who could not sleep.
 *  Flag: lastcup. The human never has to reach Ascension: the message goes through the Empty Seat's hairline crack, and a cup of wine goes with it. */
final class TaleDiscipleSeasons {
    private TaleDiscipleSeasons() { }

    static void add(Chapters.Def d) {
        d.flags("nettle", "name", "song", "lastcup");
        d.place("village1", "wild").place("village2", "wild").place("ferry", "wild").place("granny", "far").place("palace", "start");
        d.cast("nettle", "Nettle", "Brook").she().keep().trait(Character.Traits.Friendly).side("Good").lvl(0).age(13).at("here").rel(3);
        d.cast("tobiah", "Tobiah", "Lantern").he().trait(Character.Traits.Merchant).side("Neutral").lvl(2).age(58).at("place:granny").free();
        d.cast("ottoline", "Granny", "Ottoline").she().trait(Character.Traits.Friendly).side("Good").lvl(0).age(88).at("place:granny").free();

        // ================================================================== SEASON 2
        d.season(2, "The Fourth Move");

        d.chapter("student", "A Student, Against Advice").years(3)
                .setup((c, st) -> Chapters.spawn(c, st, "nettle"))
                .intro(
                        "* She is thirteen and she has been sleeping in the corner seat. Nobody has told her she may not. The innkeeper, who is old now and has opinions about everything but this, says that the girl came in out of the rain in the spring and sat down in the one chair nobody sits in. When the innkeeper went to tell her off, the words did not come.",
                        "* She is called Nettle. She has a face like a closed fist and a pair of very large, very steady eyes. She watched you practise the first move in the yard last night, from the kitchen window, with a heel of bread in her hand. She has come to say that you are doing it wrong.",
                        "@Nettle| You tip your head back too soon. The sky doesn't come in until you've finished the wrist. You do the wrist after. I've seen it done properly, I think. I mean, I haven't. But I know where it goes.",
                        "@You| I'm not anyone's teacher.",
                        "@Nettle| I haven't asked you to teach me. I've told you what's wrong with your wrist.")
                .wait(4)
                .outro(
                        "* You teach her nothing. You do the moves in the yard every morning at dawn, in the cold, and she sits on the step with her chin on her knees and never blinks. After a month she has stopped telling you what is wrong. After two, she has started, very slightly, to move her own wrist.",
                        "* One evening you find her in the stable, by herself, with the lantern on the wall, trying to fall toward a bale of straw. She falls. She gets up. She falls again. She is not graceful. She is a thirteen-year-old with skinned knees and a chin like a spade, falling toward straw with a look of the most profound concentration.",
                        "@Nettle| I'm doing it wrong, aren't I?",
                        "@You| Yes.",
                        "@Nettle| Good. Then I'll know when I've got it.",
                        "* You hear yourself say, in a voice that is not entirely yours: 'Don't aim. Fall toward the one you want to stop.' She looks round at you, and you are both rather startled.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("funeral", "The Beggar Prince's Song").gap(14)
                .intro(
                        "* They tell it in a village called Lower Teng, at the foot of a hill, in the evening, to anyone who will sit still for it. A man came through in the year of the long drought. He was old, and drunk, and wore a coat that had more patches than coat. The village was burying its headman that day, and the village did not know how to cry.",
                        "* It was a hard people. They had buried a great many in a great many droughts, and had got into the habit of burying quickly and without noise. The old man stood at the grave with his gourd. He listened to the silence. Then he sang a song.")
                .at("village1", "Go to the village of Lower Teng, at {@village1}, and hear their story of the singing drunk.")
                .outro(
                        "@Village elder| It was a terrible song. I want that known. He had no voice, and he had no tune, and he forgot the words in the second verse and made up the rest. It was about a goat. A goat that wanted to be a bird. And the more he sang, the more we laughed. And the more we laughed, the more we wept. And by the end the whole village was in the churchyard on its knees, howling, and he went away.",
                        "@Village elder| We've sung that song at every funeral since. It is a very bad song. It's the only song we have. You'll find that when we do it right, the dead sit up for a bit and listen.",
                        "* Nettle is beside you on the wall, swinging her heels. She has been given a cup of cider. She says, in her solemn way: 'He was teaching them the second move.'",
                        "@You| What?",
                        "@Nettle| The stumble that isn't a fall. He made them fall toward the thing they were afraid of. That's all that was. He just did it with a goat.",
                        "* The elder tells you the moral of the tale, which is that a village which weeps together will not starve. It is a plain little moral, and slightly wrong. The village starved the next year. But it did it, they say, remarkably well.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("rain", "Rain Follows Laughter").gap(10)
                .intro(
                        "* The second village is high and dry and unlucky. It has not rained there in four years. The wells are down to their last yard of mud. The people have the careful, polite, frayed look of a household that has stopped quarrelling because there is no water in it to quarrel with.",
                        "* They say a man came through. They say he sat down in the dry square beside the dry fountain with his red gourd, and poured the wine out on the ground. All of it. A whole gourdful, glug by glug, on to the cracked clay. And he said, so they remember, in a loud cheerful voice: 'There. Now that's one of us that's drunk.'")
                .at("village2", "Go to the village at {@village2}, and hear what the drunk did to the dry square.")
                .outro(
                        "* The headwoman is a spare, brown, humorous person with a face like a pressed leaf. She leads you to the square and shows you the spot. It is a flat paving stone, a little darker than the rest. Moss grows in a perfect circle where the wine was poured, the only moss for forty miles.",
                        "@Headwoman| It rained the next night. I want to be fair. It might have rained anyway. But it rained the way it rains in a story. It rained for three days, quietly, in a straight line, and not a drop of it fell on the stone. Everything else in the valley was drowned. We had to bail out the church. But the stone was dry.",
                        "@Headwoman| And the old man was sitting on it with his coat over his head, laughing, and the water running off in a ring.",
                        "* She tells you the moral, which is that the sky is fond of a joke. It is a poor moral. The next drought came in nine years and the stone made no difference at all. But on certain evenings, the headwoman says, the moss glows. You do not see it. Nettle, who has an unusual gift for seeing things that are not there, says that she does.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("hatfable", "The Beggar's Hat").gap(6)
                .intro(
                        "* The sixth tale is the shortest. A beggar tells it to you in the street, at the corner of a bridge, with his hat held out. He tells it without being asked, in a rapid sing-song, as if it were a charm. You had not meant to stop. You give him a coin. He said: 'That's the second one today. The first was a man with a red gourd.'")
                .wait(1)
                .outro(
                        "@Beggar| He sat down beside me. He had no hat. He said he'd come out without. He said, Lend us yours, there's a good fellow, I'll be a moment. And I lent it, because he had the face for it, and he held it out to the people going by, and they put in coins, a great many coins, and he thanked each one. He said thank you for the loan, thank you for the loan, thank you for the loan, to every one.",
                        "@Beggar| When it was full, he looked into it, and counted, and turned it over, and poured the lot into my lap. He said: I never take what I've borrowed. It's bad manners. He gave me back the hat. It was mine again, and it was full. That was how I came to be a rich man.",
                        "* He tells you the moral, which is that a beggar who is generous has a hat that cannot be emptied. You look at the hat. It is a ragged, greasy, nameless object, with a hole in the crown, and not a coin in it but yours. 'It's a story,' the beggar says, with a wide gap-toothed smile. 'I never said it was true. It's the best thing I own.'",
                        "* Nettle drops a copper in the hat. She says she has just been paid. She says it is for the loan.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("coin", "The Coin That Came Back").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "tobiah"))
                .intro(
                        "* The merchant is called Tobiah Lantern, and his shop is a long stately building near the market of a city that you have walked through six times. He sells silks, and clocks, and a great many items that do not fit either category. He has a sign over the door that says WE DO NOT LOOK THE OTHER WAY.",
                        "@Tobiah| I was told that you collect stories about a man with a gourd. I have one. I've been waiting thirty years for someone to tell it to who would not laugh. I am slightly afraid that you will laugh.")
                .meet("tobiah", "Hear Tobiah Lantern's story of a coin.")
                .outro(
                        "* He opens a drawer and takes out a velvet cloth, and lays it on the counter, and on the cloth there is a small gold coin. One face is a laughing old man. On the other, in a ring of fine writing, stands a crane on one leg.",
                        "@Tobiah| He paid me with it. Thirty years ago. A cup of tea and a bowl of noodles. I put it in the till. In the morning it was in my pocket. I put it in the till again. In the morning it was on the pillow. I have spent it, I should think, four hundred times, in four hundred different places. It comes back. It has never once been late.",
                        "@Tobiah| I took it to a goldsmith. He said it was the purest gold he had ever seen, and that it weighed exactly the same each time, to the grain. I took it to a priest. He said it was a debt. I asked whose. He said, 'The sort that you pay by owing.'",
                        "* He pushes it across the counter. You do not take it. Nettle takes it, and weighs it on her palm, and gives it back. She says: 'It's not a coin. It's a promise. He said he'd come and settle up.'",
                        "* Tobiah tells you the moral, which is that a debt is a way of keeping a friend. It is quite wrong, and you think of it for weeks.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("ferry", "The Ferry Without a Fare").gap(10)
                .intro(
                        "* The fourth tale is the shortest, and it is told by a ferryman who does not say a word of it. He points. He takes you across the river in a flat boat, and at the far landing, on the bank, there is a little shrine of piled stones, and in the shrine a battered clay cup.",
                        "* On the near side, as you left, a man was waiting for the ferry with a pack and a stick. He was in the queue behind you. He is not there now.")
                .at("ferry", "Cross the river at {@ferry}, where the ferryman does not take a fare.")
                .outro(
                        "* The ferryman says something at last, on the way back, in the middle of the river, with the oars dripping. He says: 'He tried to pay me.' That is all. You wait for more. He rows. Nettle trails her hand in the water.",
                        "@Ferryman| I said no. I never take a fare from a man who looks as tired as that. He sat down in the bow and said, 'Then I'll row.' And he took the oars out of my hands and rowed me across, and back, and across again, until the moon was up. He wouldn't stop. He said it was the only fair way. I sat in the stern with my arms folded, like a passenger, and the strangest thing was how good it felt.",
                        "* He rows on in silence. Nettle looks at you.",
                        "@Nettle| That's the fourth thing. Isn't it? That's what it looks like.",
                        "@You| What does?",
                        "@Nettle| You take the cup. You take the oars. You make them sit. You don't make a speech about it. You just do the next thing, so that they don't have to.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("needle", "The Tailor Who Mended the Sky").gap(8)
                .intro(
                        "* It is told to you at the inn, on a night of heavy rain, by a tailor on his way through. He is a small bald man with a measuring tape round his neck, who says he is not a storyteller but cannot help it, since it is the only way he has found to be believed.",
                        "@Tailor| There was a rip in the sky once. Not a big one. A rip about so. It came from a very hard winter, you see, and the heavens had worn thin at the seam. It let the rain in. People looked up and said it's a pity, and went in, and nobody did anything about it.")
                .wait(1)
                .outro(
                        "@Tailor| Then a drunk man came through the village. He had a gourd, and he had a needle, which he'd got from somewhere, and he sat down under the rip with his back to a stone. The villagers said: what are you doing? He said: it's an awful draught. And he threaded the needle with a long hair of his own beard, and he stood on a barrel, and he stitched the sky.",
                        "@Tailor| It took him three days. It was an ugly stitch. You can see it still, on a clear night, a sort of wobbly line of stars across the north, where it doesn't match. The villagers call it the Drunkard's Seam. They say if you look at it long enough you can feel the wine.",
                        "* He tells you the moral, which is that a thing doesn't have to be done well to be done. It is a poor moral. It is, as Nettle points out afterwards, with her chin on her fists, exactly the opposite of what a tailor ought to believe. 'He does not believe it,' she says. 'He told it as a joke against himself. He mends things properly.' She says it with a faint, critical, loving air, as of someone who has understood a family.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("judge", "The Judge and the Gourd").gap(6)
                .intro(
                        "* The fifth fable is told by a judge. He is retired now, and lives in the country with a large dog, and tells it with a good deal of throat-clearing, as a man unused to public speech. He says that it happened in his father's court, in the year of a bad harvest.",
                        "@Judge| A drunk was brought up for stealing a loaf. It was a perfectly clear case. He was found with the bread in his hand and the baker's wife in the doorway, shouting. My father asked him how he pleaded.")
                .wait(1)
                .outro(
                        "@Judge| He said: guilty, my lord, but I'd like to give evidence. My father, who was a fair man, said that he might. The drunk took the gourd from his belt and set it on the table. He said: this is my evidence. It's never empty. I have drunk from it every day for a hundred years, and it has never been empty, and that is because someone, at some point, always puts something in.",
                        "@Judge| He said: the baker's wife is the one who put the bread in my hand. I was hungry. I asked. She said no. I turned to go, and she ran after me with a loaf and said: here, you daft old fool. And then she shouted. She does both. It is, I'm told, her manner.",
                        "* The judge pauses. His dog sighs. A log falls in the grate.",
                        "@Judge| My father looked at the baker's wife. She went crimson. She said that it was true. She said she could not help it: she had never learned to give anything without being furious about it. The case was dismissed. The drunk bowed to the court, and to the baker's wife, and to the loaf. He left the gourd on the table. My father kept it in the court for thirty years. It was never empty.",
                        "* He tells you the moral, which is that a gift is a theft that has been forgiven. You say it seems the wrong way round. He nods, and says that it has always bothered him, but that the bread was real.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("thirdcup", "The Third Cup").gap(8)
                .intro(
                        "* This one is told by a pair of old women on a bench in a market square, sharing a flask. They tell it in turns, each correcting the other, in the manner of people who have told it a hundred times and hold different views about the ending. The first is large and tolerant. The second is small and sharp and shuffles the cards of a game she is not playing.",
                        "@First| It was at a wedding. Our mother's. He turned up at the gate, the old drunk, without an invitation, and said he'd heard there was wine.",
                        "@Second| He had not heard. He'd smelt it.")
                .wait(1)
                .outro(
                        "@First| They sat him at the bottom of the table, by the door, where the draught was. And my father stood up to make the toast, and filled three cups: one for the groom, one for the bride, and one for the one who comes uninvited, which was an old custom. The third cup is always left. Nobody drinks it.",
                        "@Second| He drank it.",
                        "@First| He did. And he said: it's a very poor wine. It was. My father had made it himself. It was the worst vintage in a century. He said: it's a poor wine, but it's the best-meant I've had in years. And my father, who'd been shaking, laughed so hard that he knocked the groom into the cake.",
                        "@Second| And the cup stayed full. All night. We kept looking. Every time we looked it was full. And in the morning it was gone, and the gate was open, and in the third cup there was a coin.",
                        "* You hold out your hand. The first old woman takes something from her sleeve and puts it into your palm. It is small and gold and warm. On one face is a laughing old man. On the other, in a ring of fine writing, stands a crane on one leg. 'We kept it back,' she says. 'It's for the one who comes asking. We don't know who you are. We know you're him.'",
                        "* The moral, she says, is that an uninvited guest is the best kind. The second old woman says that this is not the moral. She says the moral is that the wine was terrible. They quarrel about it all the way across the square, with the flask going between them, and you stand in the sun and turn the coin over, and it is warm as a hand.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("lullaby", "The Lullaby").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "ottoline"))
                .intro(
                        "* The last tale is a song, and the person who has it is an old woman, in a city far from the road, in a cottage with a cat in every window. Granny Ottoline is eighty-eight, deaf, and delighted. She has sent word, by a boy, that she has heard there is a person about with the tune, and that she would like to hear it.",
                        "* You hum it for her on the doorstep. Slow, a bit sad, with four notes in a row. She listens with her head on one side. When you finish, she puts her hand on your arm, and says in a loud, kind voice, 'No, dear. You've got the end wrong.'")
                .meet("ottoline", "Hum the tune for Granny Ottoline.")
                .outro(
                        "* She hums it herself, in a thin sweet cracked voice, and the cats in the windows stop what they are doing. It goes the same way, and then, at the very end, it does something you have never heard. It goes up. Only a step. It rises, and stops there, unresolved, like a question at the end of a sentence.",
                        "@Ottoline| My grandmother had it. She had it from her grandmother, who was told it by a drunk. Not a nasty one. A tired one. He came to the house in the winter. There was a child in it who couldn't sleep, a very quiet child, from the big house on the hill, and the drunk said that he knew a song for that.",
                        "@Ottoline| He sang it every night for a month. When it was over the child was asleep, and the drunk was gone. My grandmother said the child grew up to be someone important. She said they had a lot of rooms in the big house, and none of them was warm.",
                        "* She pats your arm. She is satisfied. The cats go back to what they were doing.",
                        "@Ottoline| It's a lullaby. For someone who can't sleep. It isn't for you, I don't think. Isn't it queer, how some songs are meant for somebody else?")
                .outChoice(new Chapters.Choice("song", "Granny Ottoline has finished, and Nettle is humming under her breath.",
                        "Hum the end the new way, up a step, and keep it.", "Keep the old end: the tune is his, and you will not change it.", "Teach Nettle the rising end, and let her carry it.")
                        .after(
                                new String[]{"* You hum it, up a step. It is a very small change. It makes the whole tune open like a door. You find that you are, for no reason, close to tears."},
                                new String[]{"* You keep it as it was. It has a finished sound, like a bell. Granny Ottoline says that is the proper way, too, if you do not mind the dead end. She has never minded."},
                                new String[]{"@Nettle| Up a step. Oh, that's lovely. It sounds like asking.", "* She hums it, up to the end, six times, until the cats begin to look put-upon."}))
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("nfall", "Nettle's Fall").gap(4)
                .intro(
                        "* She falls from the roof of the stable on a bright morning in September, and for a moment you stand in the yard with a bucket and think that it is a move. She has been practising the second move, the Stumble That Is Not a Fall, and it is just the kind of thing she does. She goes over the edge with her arms out, head first, toward the haystack, with a look of the most extreme concentration.",
                        "* The haystack has been moved. Nobody knows who moved it. She lands on the cobbles with a sound that you will remember all your life, a flat dull small sound, and does not get up.")
                .wait(1)
                .outro(
                        "* She is not dead. She is not very hurt. The arm is broken and the collarbone is cracked, and she has a gash over one eye that bleeds in the dramatic way such things do. You carry her to the kitchen, and the innkeeper sets the arm with a cloth and a spoon. Then she opens her eyes and says, in a dry thin voice, to the ceiling: 'I did the wrist too soon.'",
                        "* You sit with her through the night. It is the first time you have done it for anyone, and you find that you have no idea how. You hold a cup of water, and put it down, and pick it up again. You sing the tune, because you can think of nothing else, and she goes to sleep in the second verse. You sit on in the lamp-light with your hand on the quilt where her hand is, and think of a barrel in an inn courtyard, a long time ago, and a clay cup with a note.",
                        "@Nettle| (half asleep) Is it you who moved the hay?",
                        "@You| No.",
                        "@Nettle| Funny. Somebody did.",
                        "* The innkeeper, at the door with a bowl of broth, says that it was a mercy. She says it with a perfectly straight face. The red gourd, on its peg by the kitchen window, is warm to the touch. It has been warm all night.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("ask", "What Nettle Asked").gap(6)
                .intro(
                        "* She asks it on a night in autumn, in the courtyard of the inn, with the lanterns lit and the corner chair empty. She is fifteen. She has a scab on her chin from a bad fall. She has been practising the three moves for two years, and she can do them better than you.",
                        "@Nettle| What was his name?",
                        "@You| Wen Jiu. Warm Wine.",
                        "@Nettle| That's what he said. But what did he say when you asked him the first time? When you were ten. You told me once. The moon, and the road, and the gourd.",
                        "@You| He said, ask the moon.",
                        "@Nettle| I've asked. I've been asking for a year. She never says anything. Do you want to know what I think?")
                .wait(1)
                .outChoice(new Chapters.Choice("nettle", "Nettle has asked the question that the old man left you.",
                        "Yes: tell me what you think.", "Say that she is not to ask, and that he is not hers to find.", "Say nothing, and put a cup in her hand.")
                        .after(
                                new String[]{"@Nettle| I think the moon doesn't answer because he wanted you to ask her. I think he's not asking us to find him. I think he's asking someone to find us.", "* It is the most sensible thing anyone has said about the matter."},
                                new String[]{"@Nettle| He was yours, then. I see.", "* She says it quite gently. She doesn't push. It is, you realise, the thing you were afraid of: that she would be easier to hurt than you are."},
                                new String[]{"* It is a small cup, the one from the shelf. You fill it from the gourd. She holds it in both hands, and the wine goes up and down with her breath, and she looks into it as though it were the moon."}))
                .outro(
                        "@Nettle| My name isn't Nettle. It was my mother's idea. She said I was a weed and a stinger. I'd like to ask the moon, and the road, and the gourd, what my name is. I'd like to be called something I've picked.",
                        "* She looks up at the moon until the lantern gutters. Then down the road. Then at the gourd on your hip. And after the longest pause you have ever heard from a child, she says it, quite clearly.",
                        "@Nettle| Brook. Because it goes.")
                .done((c, st) -> Chapters.set(st, "name", 1))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("four", "The Fourth Move").gap(4)
                .intro(
                        "* You do it at midnight in the courtyard, with the two of you, and a lantern, and the empty chair. The three moves, one after another, in the cold: Cup Raised to the Moon, The Stumble That Is Not a Fall, Empty Gourd, Full Sky. You have done them a thousand times. They have never felt like this.",
                        "* On the last of them, when you tip the gourd over and let your arms fall wide, and do not close, she does something you did not teach her. She takes a step. She puts out her hand, palm up, level with your wrist, as if to catch a thing as it fell.")
                .wait(1)
                .outro(
                        "* She does not catch anything. There is nothing to catch. But the movement is the whole of it. It is not a throw, or a fall, or an emptying. It is the hand held out underneath. It is what comes after you have let the world in: someone to put it down in front of.",
                        "@Nettle| I don't know what it's called.",
                        "@You| Cup Passed.",
                        "* You hear yourself say it, in the dark, with a sort of gentle surprise, like a man saying the name of a child he has not seen in years. The old chair, in the corner of the inn, creaks. It is not the wind. A faint warm smell of spilled wine comes through the kitchen.",
                        "@Nettle| Is that it? Is that the fourth?",
                        "@You| He said there wasn't one. He said a teacher is only the first pair of hands.",
                        "@Nettle| Then I'm the second pair.",
                        "* You say nothing. You pour a cup from the gourd, and hold it out, and for the first time, in all those years, someone takes it from you without being asked.")
                .pay(5000, 0, 0, 2, 0, 2);

        d.interlude(2, "The First Pair of Hands",
                "* There is a new plate on the corner table at the inn. It says, in a childish hand, Reserved. Beneath it, in a better one: Free to anyone who will pass it on.",
                "?song=1|* The tune goes up a step at the end now, in every tavern from here to the river. Nobody knows who changed it.",
                "?song=2|* The tune has its old finish. Granny Ottoline, who is ninety, hums it for her cats. They have never complained.",
                "?song=3|* Nettle teaches the rising end to the children at the inn. It spreads, in a season, to the next three villages. They think it is a new song.",
                "?nettle=1|* Brook sits in the corner seat on cold evenings with a book that she does not read. She is working on a fifth move. She says it will be a surprise.",
                "?nettle=2|* Brook visits twice a year. She writes between times. Her hand is firmer than yours. She has begun to sign her letters with a small drawing of a gourd.",
                "?nettle=3|* Brook has gone down the road with a pack. She sends a leaf, pressed, with no words, at the turn of each season. You keep them in the book you do not read.",
                "* In the winter, a man you do not know leaves a cup at the inn, an old clay cup, mended with gold. He says someone asked him to pass it on. He does not say who. The cup is warm.");

        // ================================================================== SEASON 3
        d.season(3, "Tell the Quiet Man");

        d.chapter("barrel", "The Barrel").years(3)
                .intro(
                        "* You go back, on the way, to the courtyard where it began. It is a day's detour. You tell Brook it is nothing, a thing you want to look at, and she says 'Of course', in a voice that makes it plain she has understood the whole of it.",
                        "* The inn has changed. It has a new roof, and a new sign, and a bell on the gate, and the paper lanterns in the yard are red where they were white. The barrel is still there. It is under the eaves, on its side, with the hoops rusted and a sparrow's nest in the bung. Nobody has moved it. Nobody would.")
                .wait(1)
                .outro(
                        "* You sit on it. It is lower than you remembered. Your knees come up. It has been, by your reckoning, thirty years, and the courtyard is much smaller, and the sky above it very much the same, with a first star in it, and a grey ribbon of cloud.",
                        "@Brook| This is where?",
                        "@You| Here. I was on that step. I was ten. I had a rice cake. He stood on that side, with his gourd, and he did the first one.",
                        "* She looks at the place on the cobbles as if she could see a ten-year-old with a rice cake and a face full of attention. She says nothing. She goes to the middle of the yard, and stands with her feet apart and her arms down, and tips her head back, and lifts one hand as if it held a cup.",
                        "* She does the first move, there, in the dusk, in her travelling clothes. She does it perfectly. The sky tips in. You feel it go down your neck. A sparrow, in the bung, wakes and stares. And from the kitchen window a woman who has never seen you before in her life, an old woman, with a floury apron, leans out and looks at you both and says, quite calmly: 'Your tea's ready. He always said you'd come.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("message", "A Message Is a Kind of Debt").gap(2)
                .intro(
                        "* The message has been in your pocket for twenty years. It is five words long and a half. 'If you ever meet a quiet man in a very big garden, tell him I found the crack. And that I am sorry I'm late.' You have never met a quiet man. You have met a great many people, in a great many gardens, and not one of them has been the right one.",
                        "* You have never forgotten it. You have turned it over, on certain nights, in the way one worries at a tooth. It sits in the middle of your life like a stone in a stream, and the water goes round.",
                        "* It is Brook who says it, at last. She comes in one evening from the stable with straw in her hair and the stub of a pencil behind her ear, and she says it flatly, as if reading from a list.",
                        "@Brook| You should go. You've been meaning to for twenty years. There's one place in the world that has a quiet man in a very big garden, and you know it. You have been very careful not to go.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, with the old man's message.")
                .outro(
                        "* You go on foot. It is a long walk, and you take it slowly, and you find that you want to be a little late. The road is the road. The inn lies behind you, a small brown shape with a lantern in the window, and then it does not.",
                        "* At the gate of the Palace there is a young Wick sweeping the steps. He looks up at your gourd. He stops sweeping. He says, with the unsteady courtesy of a person who has been trained for this and never expected to use it: 'You are expected.'",
                        "@You| By whom?",
                        "@Wick| By him. He said you might come. He has said so every year. We didn't believe him. He keeps a chair.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("gatehouse", "Brook Inspects the Palace").gap(1)
                .intro(
                        "* Brook has never seen a palace. She has seen a lot of inns and a few barns and the inside of a mill. She stands in the great hall of the Oldest Order with her pack on her back and her hands in her pockets, and turns very slowly on her heel, once all the way round, like a person doing a survey.",
                        "@Brook| It's very clean.",
                        "@Wick| We do a great deal of sweeping.",
                        "@Brook| It's got no corners. I've never seen a place without a corner where a person could sit. Where do they sit?")
                .wait(1)
                .outro(
                        "* The young Wick, who is about her age, has never been asked. He looks round at the hall as if for the first time. He says that they sit in the kitchen. He says that the kitchen is the only room with a corner. She nods, as at a diagnosis.",
                        "@Brook| That's the one, then. That's where he'll be.",
                        "@Wick| Who?",
                        "@Brook| Whoever's the lonely one. In a place like this it's always the cook.",
                        "* She walks off down the long marble corridor with her pack bouncing on her back, following a smell of onions, as sure as a dog on a scent. The Wick looks after her. He looks at you. He says, in a low, awed voice: 'Is she always like that?' You say that she has been, ever since you met her, a good deal worse.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("soup", "The Man in the Kitchen").gap(1)
                .intro(
                        "* The kitchen is the only warm room in the Palace. You find him at the long table with a bowl of soup, an ordinary man of no age in a plain apron, holding the spoon as a person holds a thing he has only lately learned to use. He looks up. There is a second bowl across from him, laid, with a spoon beside it.",
                        "@???| I'm not allowed to tell you who I am. That one I can keep. I've been told you were coming for a very long time, by a person who is never right about the date. Please sit. The soup is very good. I have it every day.",
                        "* You sit. The red gourd, on your hip, is so warm that you can feel it through your coat.")
                .wait(1)
                .outro(
                        "@You| I have a message for you.",
                        "@???| I know. I have known for twenty years. I knew it before you were born. I had a feeling, that is all. It is one of the few I am allowed. Please say it. I would very much like to hear it said aloud.",
                        "* You say it. You have rehearsed it for twenty years, and when it comes it is not at all as you meant. 'He says he found the crack. And that he's sorry he's late.'",
                        "* The man does not move. The spoon, in his hand, rests against the rim of the bowl. A long silence settles on the kitchen. Then he puts the spoon down, with extreme care, as though it were a thing that might wake, and lays both his hands flat on the table, and begins, very quietly, to laugh.",
                        "@???| He's late. He's always late. He has never in the whole of his life been on time for a single thing. Oh, I could weep. I am weeping. It isn't a thing I am supposed to do. Do you know, he said he would be here by spring. I was nine. It has been, by my reckoning, a rather long spring.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("boy", "A Boy Who Could Not Sleep").gap(1)
                .intro(
                        "* He tells it to you over the second bowl, slowly, with long pauses for the soup. He tells it as though reading from a letter he has by heart. There was a house with many rooms, none of them warm. There was a boy in it who could not sleep. His father was always away at the war. His mother had died. The servants were afraid of him, and so he was afraid of them, and between the two a great silence had grown, like moss on a stone.",
                        "* One winter a drunk man came to the house. He had no business there. He had come in at the kitchen door, looking for a fire, and the cook, who was frightened of everything, gave him bread. And that night the boy heard a sound in the corridor: a man humming, slow and sad, with four notes in a row.")
                .wait(2)
                .outro(
                        "@???| He stayed a month. He taught me three things. He said they were not for fighting. They were for being a person in a place. He said the first was to look up, and the second to fall toward what frightens you, and the third to hold your arms wide and not close. I didn't understand. I was nine. But I did them every night, and in a month I could sleep.",
                        "@???| And then he went. He said he had somewhere to be. He said it was very far. He said, 'I'll come back, lad, when I've found the way.'",
                        "* He is silent until the spoon stops ringing. The soup is cold. Somewhere above you, in the great dark Palace, a bell is rung for a duty that has not been done in a long age.",
                        "@???| I was not allowed to say this to anyone. The message changes that. I think he knew it would. I am the image of that boy. That is what I am. He grew up and made a garden, and in the garden he put a reflection of himself to keep it. I do not remember the boy. I remember only that I could not sleep, and that there was a man, and a tune. I was left behind with it. I have been in this kitchen for a very long time, and I have never once been able to nap.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("crack", "The Hairline").gap(2)
                .intro(
                        "* He takes you down to the Empty Seat himself, which he has never done for anyone. It is a plain stone chair at the end of a plain hall, with a dust-sheet over it. He folds the sheet back with the air of a man opening a shutter on a view.",
                        "* Beneath the chair, in the stone floor, there is a crack. You would not notice it unless you were told. It is a hair's width. It runs from the foot of the Seat to the wall, and it is not dark, as a crack ought to be. It is the colour of a morning in another place.")
                .wait(1)
                .outro(
                        "* It hums. It has always hummed, he says, very faintly, at night, in the small hours, with a tune nobody could place. He had thought it was a draught. He had thought, for a year, that it was his own imagination. It is only lately that he has begun to hear, in the pauses, someone clearing their throat.",
                        "* You kneel down. The stone is warm. You put your lips to the crack, and you hum. The four notes, in a row, slow and a bit sad, and then you go up the step, at the end, the way Granny Ottoline did.",
                        "* The hairline brightens. The hum stops, as a person stops who has been interrupted in the middle of a story.",
                        "@Voice| (very faint, as if from the far end of a long hall) ...Lad?")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("through", "Cup Passed").gap(1)
                .intro(
                        "* It is not a conversation. A crack the width of a hair does not allow one. But it carries a voice, and a voice carries a good deal: pauses and breaths, the small sounds that make up a person. The old man is a long way away. He is, by the sound of it, sitting down. There is the cluck of a cork, and a soft tilt of liquid.",
                        "@Voice| I've been trying to get through this wall for two hundred years. Do you know how long that is? It's a very long time. Not that I've been counting. I had a gourd with a hole in it, and a quantity of free time.",
                        "@???| You're late.",
                        "@Voice| I know. I did say.",
                        "@???| You said spring.",
                        "@Voice| It's been a long spring. Is that the little lad? You've grown. I can hear you've grown. How is the garden?")
                .wait(1)
                .outro(
                        "* You step back. There are two voices at the hairline now, and you are not needed. The Master of the Palace is on his knees on the stone with his ear against the crack and his plain apron spread on the floor. He talks in a quick, low, eager voice, like a child, with a tremor in it. You catch fragments. The soup. The roof. The wall. 'I did the moves every night.' 'Did you? Good lad. Even the third?' 'Especially the third.'",
                        "* You go and sit on the steps, with the gourd on your knee. You are aware that you are crying, in the quiet way of a person who is happy. After a while a young Wick comes and sits beside you, with a bowl of the soup, and says nothing, and you eat it.")
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("letter3", "Brook Writes Home").gap(1)
                .intro(
                        "* She writes it on the floor of the guest room, in the Palace, by lamp, with her tongue between her teeth and her knees up round her ears. It takes her four hours and three sheets of paper and the better part of an ink-stick. She will not let you read it. She says it is not that kind of letter.",
                        "* You read it. You do not mean to. It lies on the boards beside her, open, when she has gone to the kitchen for something to put on her bread. The first line is in her round fierce hand, and you are reading the second before you know.")
                .wait(1)
                .outro(
                        "* 'Dear Mrs. Hallowell. I hope you are well and the cat is not dead. I am in the Palace of the Oldest Order. It is very big and has no corners. I do not like it. There is a man who is the Master of it, but he is not what you would think. He is a sort of cook. He is very nice. He asks a lot of questions about soup. I have decided to look after him.'",
                        "* 'The Master (that is what you call you) is well. I think he is sad. He has gone very quiet. I think it is because he is nearly there. I know what that is like. It is like coming to the end of the road and finding the inn is shut and there is a light on inside. You do not know whether to knock.'",
                        "* 'I am to say that I miss the stable, and the hay, and the mouse called Aldous that is the size of a boot. I miss the corner chair. I do not miss the yard. If anything has happened to the pear tree, do not tell me. I will find out. I am going to be all right. I think I have been all right for years. It is only that nobody has said.'",
                        "* 'Eat something. Brook.'",
                        "* She comes back with a plate and sees you with the paper. She does not take it from you. She sits down on the floor, and sets the plate between you, and says, with the dignity of a person caught red-handed: 'It's a first draft.'")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("debt", "A Debt of Wine").gap(1)
                .intro(
                        "* The Master of the Palace has been busy. He has been, you find, on an errand. He comes into the kitchen at dusk, flushed and out of breath and carrying a large jug, which he sets on the table with the air of a man laying down a new law. He has been to the cellar. He has not been to the cellar in a thousand years.",
                        "@???| I found it behind the barrels. It has a label. The label says 'For the Quiet One, when he is ready'. I think it is mine. I think he left it. I have been sitting on a keg of wine for ages and I did not know.")
                .wait(1)
                .outro(
                        "* You drink it at the long table, the three of you, out of cups from the kitchen shelf. It is not the wine of the red gourd. It is darker, plainer, and stronger, and tastes of a particular hill. Brook sips hers, with an expression of shrewd suspicion, and puts it down, and picks it up again.",
                        "@???| It is the first time I have tasted wine. I did not know it was supposed to be so disagreeable. Is it supposed to be so disagreeable?",
                        "@Brook| It's meant to make you feel as if you're about to say something. And then you say it.",
                        "@???| I see. Then I shall say it. I should like you to know that I am afraid. I am afraid that when he comes through the crack, he will not know me. I am only a picture of a boy, you see. I am made of what he left behind. I am afraid I shall be a disappointment.",
                        "* Nobody answers at once. The kitchen is warm. The cook, who has been standing in the corner this whole time with her arms folded, steps forward, takes the jug, and fills the Master's cup to the brim.",
                        "@Cook| He'll know you. A person always knows the one who waited.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("kitchen3b", "Three Moves in a Kitchen").gap(1)
                .intro(
                        "* He asks you on the fifth night, shyly, with his sleeves rolled and a tea towel over his shoulder. He has been standing at the end of the table since the plates were cleared. He says that he would like, if it is not a trouble, to see the moves. All three. He says he knows them, he thinks, in some fashion, and would like to know whether he has them right.")
                .wait(1)
                .outro(
                        "* The kitchen is large enough. You push the table back against the wall, and the cook, who is not a fool, takes the soup off the heat and sits down on a flour sack to watch. The Master of the Palace stands in the middle of the floor in his apron, with his feet apart. He lifts one hand, as though to hold a cup, and tips his head back. It is not elegant. It is, you realise, the first time he has looked up at a sky in a very long time, and he has to find where it is.",
                        "* The second is harder. He has to fall toward something. He does it toward the cook's cat, which is asleep on the settle. He wobbles. He falls the wrong way. He lands against the dresser with a clatter of crockery, and from the flour sack the cook says, without taking her pipe out of her mouth: 'It's not a thing a man does for the first time at his age. Do it again.'",
                        "* He does it again. He does it eleven times. On the twelfth, he stumbles toward the cat, and falls into it, and the cat opens one eye and lets him. He is on the floor of the Palace kitchen with a cat on his chest and a hand pressed to his ribs, laughing in a high astonished boy's voice, and the cook, in the corner, has taken the pipe out.",
                        "@???| That was good. Wasn't that good? I did not know I could. Oh, I did not know I could do that.",
                        "* The third he does last. He lifts the gourd, which you have lent him, and tips it over, and there is nothing in it, and he lets his arms fall wide, and does not close. He stands there in the middle of the kitchen like a man at the top of a hill. His face is wet. He says, very softly: 'It is the first time I have felt that nothing is going to hit me.'")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("cup3", "A Cup, Through a Crack").gap(1)
                .intro(
                        "* It is nearly dawn when the Master comes to the steps. He has stopped crying, and he looks ten years younger and a hundred years more tired. He has a very small clay cup in his hand, with a seam of gold across it.",
                        "@???| He says to give you this. It's yours. He said you'd know. And he says there is one more thing that he forgot to say, and it has to be said by you, to someone, when the moment comes. I've no idea what it is. He says it is the fourth move.")
                .wait(1)
                .outChoice(new Chapters.Choice("lastcup", "The cup is in your hands, and the crack has not closed.",
                        "Pour wine through the crack to him, from the red gourd.", "Say the fourth move aloud, whatever it is, to the man on the steps.", "Keep the cup, and leave the crack open, for Brook to find.")
                        .after(
                                new String[]{"* You kneel at the hairline and tip the gourd. The wine does not run along the floor. It goes down, as through a funnel, into the crack, and is gone. There is a long silence. Then from the far end of a hall, small, clear, surprised, a laugh."},
                                new String[]{"@You| Would you like a cup?", "* You say it quite simply, to the plain man in the apron, and it is the strangest sentence that you have ever spoken, for it is also the whole of the matter. He looks at you. He takes the cup from your hands. His fingers are shaking. He says, in a small voice: 'Yes. I think I should.'"},
                                new String[]{"* You put the cup on the dusty floor in front of the Seat, and lay the red gourd beside it, and stand. The Master looks at you, and at the cup, and at the crack, and at the gourd. 'You're sure,' he says. 'I'm not at all sure,' you say. 'It's the fourth move.'"}))
                .outro(
                        "?lastcup=1|@Voice| (faint, delighted) Oh, that is a very good vintage. That is a Warm Wine, that is. Oh, I'd forgotten. Do you know, I'd forgotten what it tasted like when it came from somewhere?",
                        "?lastcup=2|* You share a cup, the two of you, a Master and a disciple, at the foot of a stone chair, by the light of a crack. It is a very small cup. There is enough for each of you to have one swallow. It is warm, and sweet, and it tastes like a place.",
                        "?lastcup=3|* You walk out of the Palace in the first light, with the gate open behind you and no cup in your hands. You never look back. You will come again, when the girl is grown. You have a feeling she will be the next to be late.",
                        "* The hum in the crack goes up a step, and stays.")
                .pay(30000, 3, 0, 4, 1, 3);

        d.epilogue(
                "* You are not a great master. You are a person who passes a cup. It has taken you the best part of your life to understand that it is a rather larger thing than it sounds.",
                "?lastcup=1|* A thin line of wine runs from the Empty Seat to the wall, every year, on the night of the first frost. Nobody mops it. It smells of somewhere else.",
                "?lastcup=2|* The Master of the Palace has begun to nap. It is a small thing, and nobody notices, except the cook, who leaves him a blanket, and says nothing.",
                "?lastcup=3|* The cup stands on the floor in front of the Seat, with the gourd beside it. Nobody dusts them. The Wicks walk round on tiptoe.",
                "?nettle=1|* Brook has opened a school at the inn, with three pupils and a cat. The sign over the door says Cup Passed. Below it, in a smaller hand: Bring your own wine.",
                "?nettle=2|* Brook sends you a leaf at the turn of each season. You have a book of them now. It has begun to look like a map.",
                "?nettle=3|* Brook has gone down a road you have not walked. She writes, rarely, in a firmer hand than yours. She says she has met a quiet man in a very small garden, and that he sends his regards.",
                "?song=1|* The tune goes up a step at the end. Everyone who hums it now smiles by the last note, and nobody can say why.",
                "* Sometimes, in the evening, you take out the red gourd and look at the name scratched under the stopper. It still does not say Wen Jiu. The word belongs to a country you have never seen. You hum a tune. Slow, a bit sad, with four notes in a row, and then a step up. You find that, at the end, you are not alone.");
        TaleDiscipleMore.add(d);
    }
}
