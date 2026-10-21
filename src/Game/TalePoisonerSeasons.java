package Game;

/** The Apothecary's Second Cup, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "How It Was Done" (a howcatchem told backward, dry noir): somebody is killing the harbour's great and good with the human's own secret recipe, Number Nine. The inspector asks the poisoner to help find the poisoner.
 *  Rolled at the start: killer 1 Doctor Sprott of the Apothecaries' Hall (the sugar and a glove), 2 the councillor's secretary Lucien Fell (who has the green ledger if the human sold it), 3 Madam Verrel herself, from the Tower,
 *  through the knitting patterns in her letters and the warder Hilda. Each leaves two fair clues. Choices: verdict, rule. Flags: killer, verdict.
 *  Season 3, "The Antidote" (a quiet plague, plain pastoral turning cosmic): a sleeping sickness comes in with the fishing fleet. Mister Lull, a Guest of the Hollow Court, offers rest; the cure needs a willing dose.
 *  Flags: lull, dose, shop. */
final class TalePoisonerSeasons {
    private TalePoisonerSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "killer", "verdict", "lull", "dose", "shop");
        d.place("palace", "start").place("marsh", "wild");
        d.cast("sprott", "Aldous", "Sprott").he().keep().trait(Character.Traits.Alchemist).side("Evil").lvl(7).pot(6).age(58).at("place:harbour").rel(-1);
        d.cast("fell", "Lucien", "Fell").he().keep().trait(Character.Traits.Scribe).side("Evil").lvl(4).age(37).at("place:harbour").rel(0);
        d.cast("hilda", "Hilda", "Marsh").she().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(2).age(49).at("place:tower").rel(1);
        d.cast("lull", "Mister", "Lull").he().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(14).pot(8).age(60).at("far").rel(0);

        // ================================================================== SEASON 2
        d.season(2, "How It Was Done");

        d.chapter("councillor", "A Councillor Dies Politely").years(2)
                .setup((c, st) -> {
                    int k = 1 + GameDriver.rand.nextInt(3);
                    int l = Chapters.flag(st, "ledger");
                    if (l == 3) k = 2;
                    if (l == 1 && k == 2) k = 1 + 2 * GameDriver.rand.nextInt(2);
                    Chapters.set(st, "killer", k);
                    Chapters.spawn(c, st, "sprott"); Chapters.spawn(c, st, "fell"); Chapters.spawn(c, st, "hilda");
                })
                .intro(
                        "* Councillor Hargreave dies at his club on a Wednesday, in a leather chair, between the soup and the fish. He puts down his spoon, remarks that the soup is very good, and dies. The physician says his heart has stopped. The club secretary says it is a great loss. A waiter says that the old gentleman's lips went a pretty pink.",
                        "* You hear of it at the shop, from Pim, who has heard it from the net-mender, who has heard it from the club's boot-boy. By the time you have heard it, you have been sick in the sink, and washed your hands, and stood very still for a minute in the back room with the board lifted, looking at nothing.")
                .wait(12)
                .outro(
                        "* Number Nine is a poison of Verrel's own, and she never sold it. It is a slow heart-stopper in honey, tasteless, painless and undetectable except by a very careful tongue. It takes a fortnight of small doses to work. There are two people who know the recipe, and one of them is in the Tower, knitting.",
                        "* The Inspector is on your step by six. {calla.He} wears no uniform. {calla.He} has a lantern and a bag, and {calla.his} face has the particular stillness of a person who has been awake for a day and a half and has made a decision.",
                        "@Calla| I need to speak to you in an official capacity, which I dislike. I need to say that you are a suspect. I need to say that I do not think you did it. I need you to help me prove that I am right.",
                        "@You| You want me to help you find a poisoner.",
                        "@Calla| I want the best one in Brinewick to tell me how it was done. I will not insult you by pretending there is a better way.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("body", "The Physician's Table").gap(8)
                .intro(
                        "* The mortuary is cold, white and well lit. Councillor Hargreave lies under a sheet with his hands folded. The physician, a harassed young woman with chalk on her sleeve, pulls the sheet back to the waist and stands aside with the look of a person who has been wanting to tell someone a thing for two days.")
                .wait(4)
                .outro(
                        "* You see it at once, and you say nothing for a minute. The signs are there for anyone who knows them. A pinkness at the nail. A dryness of the tongue. A taste, when you lean close, of honey with something bitter far behind it. It is your own recipe, executed in the way your own hands would have executed it, but with one difference.",
                        "@You| The dose was in the food he ate that night. Number Nine takes a fortnight. This was done in a day.",
                        "@Physician| That's what I thought. It was too fast. Someone was in a hurry to prove something.",
                        "@Calla| Or had run out of time.",
                        "* You lay the facts out on the slab, one by one, in order, as Verrel taught you. Number Nine in honey. Dosed once, heavily, in a hot dish. Given at a club where fourteen men sat down to eat, and only one of them died. The killer wanted him specifically, knew his seat, and knew the soup.",
                        "@Calla| You think like a poisoner.",
                        "@You| I am one.",
                        "@Calla| Yes. I find that I'm not as alarmed by that as I ought to be.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("who", "Who Knew Number Nine").gap(16)
                .intro(
                        "* The list is short and uncomfortable. You write it on the back of a prescription pad at the counter, with the cat watching. Verrel, who is in the Tower. Yourself. Pim, who has never been told, but who is observant. Mister Dove, who may have bought it, if you sold him the ledger. And whoever, in eleven years, ever read a page of the green book.",
                        "* The Inspector adds two names in a neat hand. Doctor Aldous Sprott, Master of the Apothecaries' Hall, whose guild has been making trouble since Verrel's arrest. And Lucien Fell, secretary to the late Councillor, who has been seen asking about the ledger.")
                .meet("sprott", "Call on Doctor Sprott at the Apothecaries' Hall.")
                .outro(
                        "* The Hall is a grey building with a brass plate and a staircase that smells of beeswax. Doctor Sprott receives you in a library lined with green bottles. He is a tall pale man in a velvet cap, with a voice like a bell in a damp church, and a way of finishing your sentences for you in a tone of compassionate regret.",
                        "@Sprott| I was so sorry to hear about Madam Verrel. A great loss to the trade. And of course I hear that you've taken the shop. It must be a terrible burden. If you wanted to put it in the Guild's hands, we would see you right.",
                        "@You| That's kind.",
                        "@Sprott| It's only sense. The harbour is changing. It wants rules. It wants licences. It does not want young people with a locked drawer and a clever manner. Do have some tea.",
                        "?killer=1|* You look at the cup. It is plain and white. The tea is a good Darjeeling with a pleasant smell. Beside it is a sugar bowl in the shape of a swan. Sprott does not take sugar. He watches you stir yours as a man watches a clock he has set himself.",
                        "?killer=2|* You look at the cup. It is plain and white. The tea is a good Darjeeling. Sprott says he is quite sure the secretary, Fell, is an honest man, and that the Councillor's death is a tragic accident of age, and that he has never in his life heard of any green ledger. You have not said a word about one.",
                        "?killer=3|* You look at the cup. It is plain and white. Sprott says that all the Hall's records are open to the Inspector, and that Madam Verrel, whom he has always respected, was a clever woman whom nobody ever really understood. He sighs. He says he sends her a basket of fruit each month. She has never once replied.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("tower", "Mind the Foxglove").gap(16)
                .intro(
                        "* The second Sunday. The Tower's visiting room is a clean cell with a table and two chairs. Verrel is waiting, in her lace cap, with a ball of green wool and a pair of needles that click like a very small machine. She does not stop knitting when you come in.")
                .wait(4)
                .outro(
                        "@Verrel| You've come about the Councillor. I'm sorry for it. He was a tiresome man, but I never wished him ill, and he paid on time. Number Nine, I think? It's a pity. It was a lovely recipe.",
                        "@You| Somebody else has it.",
                        "@Verrel| Of course somebody does. Everything gets out at last. I should know. Check the sugar, dear. It's always the sugar. People take care over the salt, and they never look at the sugar.",
                        "?killer=3|* Her needles click. You watch her hands. She is knitting a long strip of green, in a pattern of ribs and bobbles, with a repeat of nine. You count without meaning to: knit nine, purl two, knit nine. A little cold finger runs down your back, and you tell yourself it is only a pattern.",
                        "?killer=1|* Her needles click. She mentions that the Guild sends fruit. She mentions that Aldous Sprott has always had a very sweet tooth, and that he once asked her for a recipe for jam. She laughs, and says you should always notice what a man asks for when he thinks it is not worth hiding.",
                        "?killer=2|* Her needles click. She asks about Fell, the Councillor's boy. She says she remembers him from the ledger, a very clean young man, a collector of other people's secrets. She sighs. 'He always did like to hold the book,' she says.",
                        "* The warder, Hilda, taps on the door. It is time. Verrel kisses you on the cheek, and presses a ball of green wool into your hand. 'For the cold,' she says. 'Mind the foxglove.' It is the same line she writes at the end of every letter. This time, for no reason you can name, you read it twice.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("pim2", "Pim Goes Missing").gap(24)
                .setup(TalePoisonerSeasons::mark)
                .intro(
                        "* Pim does not come in on Monday. He has never been late in two years. By noon you have had the whole of Salt Lane out looking, and by two you have had the net-mender's boy, the fishwife, and a very old man with an ear trumpet. On Tuesday morning, at six, you find him yourself, in the net loft at the end of the quay, wedged behind a pile of floats, awake, and shaking.")
                .made(8, "Keep the shop open while he is gone: mix eight items.")
                .outro(
                        "* He will not come out. He will not speak. You sit on the floor of the net loft with a flask of tea and a bun, and say nothing, and eat the bun yourself. After about an hour he reaches out and takes the flask.",
                        "@Pim| I saw a page. I wasn't looking. It came in the post to the shop on Friday, in an envelope with no stamp, and I took it in because it was addressed to you. And I looked at it. I couldn't help it. It had my name on it. In the green book. I didn't know there was a green book.",
                        "@You| What did it say?",
                        "@Pim| It said I'd been a very good boy. It said Madam V. had checked me out when I came. She'd looked at my mother. She'd looked at my debts. It said I was safe, and that I wasn't to be told. And I thought: that's it. That's the lot of us.",
                        "?killer=2|* He gives you the page. It is in the green book's hand. The paper is torn along the left, and the corner is ink-stained, with a blot of blue ink from a pen with a split nib. You know exactly one man in Brinewick who writes with a split nib, because he says it reminds him of his father.",
                        "?killer=1|* He gives you the page. It is in the green book's hand, and the corner is greasy, with a faint smell of beeswax. The beeswax is the Hall's. They polish everything with it.",
                        "?killer=3|* He gives you the page. It is in the green book's hand. There is a note in the margin in tiny writing that you know: Mind the foxglove. It is a different pen, a different decade, and the same hand.",
                        "* You take him home and put him to bed with the cat. He sleeps eleven hours. In the morning he is himself, mostly, and makes the tea, and does not mention the green book again.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("widow2", "The Widow's Garden").gap(24)
                .intro(
                        "* The widow lives in a tall thin house at the top of Cutler's Row, with a bit of garden and a pear tree. You go to ask her, as the Inspector asks everyone, where she was on Wednesday. The door is opened by the youngest son. He is nine, with an arm that is perfectly straight. He looks at you with a grave kind of recognition and says she is in the garden, among the beans.")
                .wait(4)
                .outro(
                        "?widow=1|* She gives you pears and a glass of cold water. She says she was in the garden, and that the neighbours saw her. She says she hears the Inspector is working very hard. She says it in the tone of someone who has not had to ask for help in a year, and has found out it is possible.",
                        "?widow=2|* She gives you pears. She says she was at church. She says she never did find out what happened to her husband. She looks at the pear tree. She says she is sorry, and that she is not, and that she thinks you will understand the difference.",
                        "?widow=3|* She gives you pears. She says she was at home, and the neighbours saw her. She does not look at you. After a minute she says that she has never told anyone what she did, and that she thinks of it every day, and that she has not been unhappy since. She is very frank. It is horrible, and she means it. 'I thought you should know I'm not the one,' she says. 'I don't want any more.'",
                        "* You go home by the harbour. The sun is out. The fishing fleet is coming in, and the gulls are screaming, and the whole town smells of salt and tar and frying. You find that you are thinking, not for the first time, about the difference between a poison and a medicine. The dose makes the poison. It is the first thing Verrel ever told you. It is the only thing you are certain is true.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("dinner", "A Guild Dinner").gap(24)
                .intro(
                        "* The Salt Guild gives its dinner on the third Saturday of the month, in the Merchants' Hall, with forty places and eleven toasts. The Inspector asks you to come as a consultant. {calla.He} says it as though it were a compliment. You are put at the end of a long table, between a ship-owner's wife and a man who sells rope, and you are given a place card that says: Guest of the Magistracy.",
                        "* You watch the salt, and the sugar, and the soup. You watch the hands of the waiters. You watch Sprott, at the top table, in his velvet cap, drinking nothing. You watch Fell, in a grey coat, near the door. You watch the Inspector, who is watching you.")
                .wait(8)
                .outro(
                        "* The second death comes at the fifth toast. It is Alderman Pike, the shipping magnate, a loud fat man who has been drinking the health of everyone in sight. He rises with the glass in his hand, says 'To Brinewick,' and sits down again very gently. Nobody notices for nearly a minute. Then the ship-owner's wife, beside you, says in a puzzled voice that he has stopped talking.",
                        "* You are out of your seat before anyone else. You put two fingers to his throat. You smell his glass. It is honey and almond, faint and clean. It is in the wine, not in the soup, and the wine was poured at the top table by a waiter you do not know.",
                        "@Calla| (very low, at your shoulder) How was it done?",
                        "@You| The wine. Someone put it in the decanter before the dinner. It was meant for him, only him, and it is far too neat.",
                        "* The room is on its feet, shouting. A lady screams. The waiters stand with their trays. In the confusion, at the far door, a tall man in a velvet cap puts down a napkin, and walks out, and does not look back.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("sugar", "The Marked Sugar").gap(16)
                .intro(
                        "* You think of the sugar all night. By morning you have a plan. It is a small plan, and a bad one, and Verrel would have been proud of it. You will prepare a sugar of your own, indistinguishable to the eye, and mark it with a harmless dye that stains the skin for a week. You will leave it where the killer is likely to take it. And you will wait, and look at hands.")
                .wait(8)
                .outro(
                        "?killer=1|* Three days later Sprott shakes your hand in the corridor of the Magistracy. He smiles, and says he is so glad you have found your feet. His glove is pale grey kid. There is a faint blue line at the wrist, where the cuff has slipped, and it is the exact colour of the dye.",
                        "?killer=2|* Three days later Lucien Fell comes to the shop for a bottle of cough syrup. He pays at the counter with a clean neat hand. When he takes the change, a little blue stain shows at the base of his thumb, and he sees you see it, and for a second his face is perfectly empty. Then he smiles.",
                        "?killer=3|* Three days later the Tower sends a note. The warder, Hilda Marsh, has been seen at the market stall that sells your sugar. She buys a pound of it every Thursday, for a lady who is knitting. Her fingers are stained blue to the second joint. She has no idea why.",
                        "@Calla| It is not proof. It would not convict a cat. But I think I may now say I know who.",
                        "* You walk home along the harbour wall. The tide is out, and the mud smells of old rope, and a boy on the slip is fishing with a bent pin. You find you are neither glad nor sorry. You find that you wish, rather badly, that it were someone you could hate.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("dovefree", "Mister Dove Is Not Guilty").gap(16)
                .intro(
                        "* The Inspector has had Mister Dove in a cell for six days, on the strength of a hunch and a bottle. He has been very polite about it. He has been sending cakes. On the seventh day, with great regret, the Magistracy lets him go, since he has an alibi, a barrister, and a dozen character witnesses, one of whom is the Mayor.")
                .wait(4)
                .outro(
                        "* He stops at your door on his way out, in a new robin's-egg apron and a hat. He is carrying a cake. He says he bears no grudge. He says he has no idea who might wish to frame him. He says business has never been so good.",
                        "@Dove| People are afraid. They are buying everything. I had to hire a second boy. I wanted to thank you for it. For the free advertising.",
                        "@You| I didn't do it on purpose.",
                        "@Dove| That is exactly what makes it so effective. I should like you to have the cake. There is nothing in it. It would be an act of pure friendship, and I like the idea of doing something absolutely pointless to a clever person.",
                        "* He lifts his hat. He goes. You look at the cake for ages, and then, because the cat cannot be trusted, you eat a slice. It is the best cake you have ever eaten. You are annoyed.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("how", "How It Was Done").gap(16)
                .intro(
                        "* You gather them in the shop on a rainy Thursday: the Inspector, Pim, the cat, a constable with a notebook, and one more person, whom you have asked, with great care, to be there. The shutters are closed. The lamp is lit. You have your three questions on a card in your hand. Who is it for. What do they think it does. What happens if they are wrong.")
                .wait(4)
                .outro(
                        "?killer=1|@You| It was done with the sugar. Doctor Sprott, who has no need of it, took the Number Nine from a copy of the ledger he persuaded a clerk to make, and used it on two men who had refused his Guild a licence. He did it with honey, because it was the signature, and because he wanted it to be mine.",
                        "?killer=1|* Sprott stands up. He is smiling the compassionate smile. He says it is a scandalous thing to accuse a Master of the Hall, and that the young person has been under a great strain. He puts his gloved hand on the table. The blue line at the wrist is as bright as a flag.",
                        "?killer=2|@You| It was done by someone who held the book. Lucien Fell, secretary to the Councillor, bought the green ledger from a man who had no business selling it. He has been working his way down the list. Hargreave, Pike, and he has a dozen more. He wants the names themselves, and the debts, and the power of being a man who is owed.",
                        "?killer=2|* Fell does not move. He is standing by the door with his hat in his hands. He says, in a low voice, that it was only supposed to be a warning. He says the first dose was not meant to be fatal. He says that he did not know how much it took.",
                        "?killer=3|@You| It was done by Madam Verrel. She did it from the Tower, with green wool, and a warder who knew nothing, and a pattern that was a recipe. She did it to the ones in the ledger who might one day speak against us. It was not for her. It was for the shop, and for me.",
                        "?killer=3|* The room is very quiet. Pim is crying without any sound. The Inspector is looking at the green wool in your hand, the pattern of ribs and bobbles, the ball you were given on the second Sunday. {calla.He} says very softly: 'Oh, dear. She meant it kindly.'",
                        "@Calla| (to the constable) You may write down that the matter has been explained. I shall want a statement in the morning.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("verdict", "Verdict").gap(8)
                .intro(
                        "* The Magistracy sits at nine in the morning. The room is panelled in oak, with a gallery, and a clock with a very loud tick. You are called as a witness. You say what you know, in order, in your own words. It takes an hour. At the end of it the Inspector rises and says that there is the matter of the shop's licence, and the matter of the ledger, and the matter of what is to be done about the accused.")
                .wait(4)
                .choice(new Chapters.Choice("verdict", "The culprit stands in the dock. The Magistracy turns to you, as the only person who understands how it was done.",
                        "Speak against them: let the law take its course.", "Speak for them: tell the court what made them do it.", "Say nothing. Let the Inspector decide.")
                        .after(
                                new String[]{"* You speak against them. You are precise and unemotional and thorough. The verdict is the one the law requires. You sit down in the gallery afterwards and look at your hands, which are perfectly steady, and wonder why that frightens you.", "@Calla| It was a fair statement. I hope you will forgive me for thinking it was the bravest thing you have done."},
                                new String[]{"* You speak for them. You tell the court about the ledger, the debts, the fear, the habit of a lifetime. You say it was a poison made out of a good intention. It is not a defence. It is, however, heard, and the sentence is lighter than it might have been, and the culprit looks across the court at you with an expression you will remember on bad nights.", "@Calla| You have made my work very much harder. Thank you."},
                                new String[]{"* You say nothing. The Inspector rises and speaks for ten minutes, plainly, in the dry level voice of someone who has thought it through. At the end the verdict is read. You do not agree with it, and you do not disagree, but you find that you would rather not have been asked.", "@Calla| You were right not to. Some things are not yours to carry. I wish I had known that sooner."}))
                .outro(
                        "* The court rises. The clock ticks. The Inspector walks out into the rain, and waits for you at the bottom of the steps, under a black umbrella. It has been a long season. There is a question in {calla.his} face, which {calla.he} has been carrying for minutes, and which {calla.he} is holding as one holds a full cup.",
                        "?love=1|@Calla| I said I would inspect the paint. I should like to inspect the rest of the building. Now, if that is convenient.",
                        "?love=2|@Calla| You have never asked me whether I would stay. I should like to say that I would, if you did.",
                        "?love=3|@Calla| I shall say it plainly, because you will not. I should like us to stop pretending that this is a professional acquaintance.")
                .done((c, st) -> { if (Chapters.flag(st, "love") != 3) { Chapters.bond(c, st, "calla", 3); Chapters.wed(c, st, "calla"); } else Chapters.bond(c, st, "calla", 2); })
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "Honey",
                "* Number Nine is not in any book now. The recipe died with the green ledger, and with the last person who knew it, who was not you.",
                "?killer=1|* Doctor Sprott resigned the Mastership of the Hall and went to live in the country with a great deal of jam. He sends a card every Christmas. It always has a bottle painted on it.",
                "?killer=2|* Lucien Fell was sent away on a ship. He writes from the other side of the sea, once a year, to say that he has found a clean sheet of paper, and a place to put it.",
                "?killer=3|* Verrel serves out her term in the Tower. She has taught eleven warders to knit. She has stopped writing the postscript. In its place there is a new line, always in the same hand: It was a very good shop.",
                "* On the first of every month, Pim hangs a sign in the shop window. He painted it himself. It says: Three questions before every sale. Under it, smaller, because he cannot help himself: And a biscuit.",
                "* Far off, in the Tower, in a cell with a window the size of a prayer book, a lady in a lace cap is knitting a long green strip. It has no pattern. It has no repeat. It goes on, and on, row after row of plain knit, and it will, if she lives, reach the sea.");

        // ================================================================== SEASON 3
        d.season(3, "The Antidote");



        d.chapter("fleet", "The Fleet Comes In Asleep").years(2)
                .intro(
                        "* The Brinewick fleet is forty boats, and on the morning of the first of March it comes in on the tide with every sail set and not one hand at the helm. The boats glide up to the quay one after another and bump against the stones with the gentleness of cattle. Nobody throws a line. Nobody calls. The gulls, which should be screaming, sit on the rigging in a row and watch.",
                        "* You are on the quay in your apron, with a basket, because the herring is late. Pim is beside you. He says, 'Something's wrong with the Marguerite,' and he is right. The skipper is asleep at the tiller with his hat over his eyes. The crew are asleep on the nets. They are breathing. They are pink and warm and smiling, every one of them, with the enormous contentment of people who have been told they may stop.")
                .wait(8)
                .outro(
                        "* The physician arrives at a run, and then three more, and then a clerk from the Magistracy with a list. They shake the sleepers. They shout. They throw water. One of the younger men opens his eyes, says clearly, 'No, thank you, I've had plenty,' and goes back to sleep.",
                        "* By evening there are two hundred and eleven sleepers in the Merchants' Hall, on cots, in rows. By the next morning there are three hundred. By the end of the week a woman in Cutler's Row has gone to bed after supper and not got up, and the baker has been found asleep with his hands in the dough.",
                        "@Pim| Is it catching?",
                        "@You| I don't know.",
                        "@Pim| It looks very peaceful.",
                        "@You| That's what I don't like about it.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("asks", "The Chief Inspector Asks").gap(16)
                .setup(TalePoisonerSeasons::mark)
                .intro(
                        "* Calla is Chief Inspector now. {calla.He} wears the badge on the left side of {calla.his} coat, where it catches the light, and has not yet got used to the weight. {calla.He} arrives at the shop at dawn with dark rings under {calla.his} eyes and a flask of coffee, and sits on the stool by the scales, and says, without preamble, that the physicians have found nothing.",
                        "@Calla| No fever. No wound. No poison that any of them can name. The blood is clean, the heart is steady, the lungs are clear. They are simply asleep, and they will not wake. I have called you because you are the best poisoner in Brinewick. I would prefer not to have to say that twice.")
                .made(10, "Test the sleepers' blood: mix ten test preparations.")
                .outro(
                        "* You work for four days in the back room with the shutters closed. You take samples from a dozen sleepers: a drop of blood, a thread of hair, a breath on a cold glass. You test them against every poison on your shelves and a few that you have never needed to name. You find nothing. It is the most thorough nothing you have ever made.",
                        "* On the fourth night, at three in the morning, with the lamp low and the cat on the scales, you taste a drop of a sleeper's saliva, which is a thing Verrel taught you never to do. It is plain. It is sweet. It tastes, very faintly, of tea.",
                        "@You| It's not a poison.",
                        "@Calla| Then what is it?",
                        "@You| I think it's a kindness. And somebody is doing it on purpose.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("dream", "What the Sleepers Say").gap(16)
                .intro(
                        "* You sit up with the sleepers. The Hall is long and dim and full of the sound of slow breathing. A nurse brings you a cup of something hot and goes away. In the cot beside you a young man in a fisherman's smock turns his head on the pillow and murmurs, quite clearly, in the voice of someone saying grace.")
                .wait(8)
                .outro(
                        "@Sleeper| It's such a nice table. They've laid it so beautifully. There's a cloth with roses on. The host is so kind. He asked if I'd like to put it down, and I said, put what down? And he said, all of it, and I thought: yes, I would.",
                        "* You write it down. You write down the next, and the next, and the one after that. They are all the same. A table with roses. A kind host. A cup of tea, which is not hot and not cold. A voice that says: you may stop. Not one of them is afraid.",
                        "* You take the notebook to the Tower on the second Sunday. Verrel reads it through, slowly, with her glasses on the end of her nose and her lips moving. She turns the page back. She turns it forward. When she is done she takes her glasses off and sits for a minute with her hands folded.",
                        "@Verrel| My master told me of this. She called it the Lull. She said it came once in a lifetime, in a quiet season, with a polite man and a very good tea. She said it was the only poison that did not need a poisoner. She said it was looking for someone to say no to it, and had not found anyone yet.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("lull", "Mister Lull").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "lull"))
                .intro(
                        "* He comes to the shop on a Monday morning, when it is empty. He is a man of sixty or so in a grey frock coat of perfect cut, with a face like a kindly bank manager. He wears gloves. He carries a small black leather bag and a pot of tea, in a tray, with two cups. He sets them on the counter. He bows.",
                        "@Lull| Mister Lull. I am here as a Guest. I hope it is not inconvenient. I have wished for minutes to make the acquaintance of the person who has been so very diligent.")
                .meet("lull", "Receive Mister Lull in the shop.")
                .outro(
                        "* He pours. The tea is a pale gold. It smells of hay and rain and something you cannot name. You do not drink. He does not seem to mind. He sips from his own cup and sets it down with a small delicate click, and talks, in a low even voice, for twenty minutes, without once raising it.",
                        "@Lull| We call it rest. I know it is an ugly word to some people. But the Garden is a long fever, and it has run a very long time. We do not hurt anyone. We offer a table, and a cup, and the chance to put the weight down. They go quite willingly. It is the kindest thing that has ever been done to them.",
                        "@You| They're not waking.",
                        "@Lull| No. Of course not. That is rather the point. I should say that the work would go much faster if you were to stop. I should say that it would be a great courtesy to us, and that the sleepers could be woken, a few at a time, and sent home, as a mark of respect. We are not unreasonable.")
                .outChoice(new Chapters.Choice("lull", "A Guest of the Hollow Court sits at your counter with a pot of tea and an offer.",
                        "Hear him out, and take the time to find his weak point.", "Throw him out. Pour his tea down the sink.", "Bargain: ask for the sleepers back, and offer him something he does not expect.")
                        .after(
                                new String[]{"* You listen. You let him talk. You nod at the right places and ask the three questions in your head: who is it for, what do they think it does, what happens if they are wrong. By the time the pot is empty you know a good deal more than he meant to tell you, and he knows it, and he says with real approval, 'Excellent.'", "@Lull| You will do very well, I think. It is a pity."},
                                new String[]{"* You pick up the tray and put it in the sink. You pour the tea away. It does not smell of anything as it goes. Mister Lull stands there with his gloved hands folded, looking at the drain with the tolerant air of a man who has been refused in a great many countries. 'I shall call again,' he says. 'It is no trouble at all.'", "@Lull| Do give my regards to the cat."},
                                new String[]{"* You say you will take the sleepers back in exchange for a cup of your own tea. He raises an eyebrow. You say that he is a Guest, and that a Guest is entitled to what is offered. 'How very correct,' he says, with delight. 'Oh, I do like a house with rules.' He leaves a card on the counter. It has a teacup on it and nothing else.", "@Lull| Name the day."}))
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("guild", "The Apothecaries' Council").gap(16)
                .intro(
                        "* The Council sits in the Hall on Tuesdays, under a portrait of a stern man in a velvet cap who is no relation. The new Master is a stout gentle woman called Ottilie, who has no gift for politics and a great gift for tea. She admits you to the long table with the look of someone who has been wanting to break the rules for years and has not had a good reason.")
                .friends(8, "Win the Council: be friends with eight people.")
                .outro(
                        "* The Council argues for three hours. The Hall will not release its stock of dreamwort. The Hall will not share its formulary with an apothecary who has been, in the words of one gentleman, convicted by association. A very old doctor makes a long speech about the dignity of the profession and falls asleep in the middle of a sentence. Nobody is sure whether it is the Lull.",
                        "* You have no gift for argument. You wait until the third hour, when the room is tired, and then you say plainly what you know: that the sleepers are not sick, that the cause is a kind of tea. That the only people who can brew its opposite are in this room. You say that you need their stock, and their formulary, and their hands.",
                        "@Ottilie| I have been Master of this Hall for seven weeks. I should like it known that I do not understand a word of this. But I should also like it known that my sister is in the Hall of the Sleepers, and has been for nine days. I will give you the key to the stores if you will give me your word that she wakes up.",
                        "@You| I'll try.",
                        "@Ottilie| Good. Try harder.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("marsh", "Foxglove and Marsh").gap(16)
                .intro(
                        "* The formula needs three things you do not have: the pollen of a marsh orchid that blooms for a single night, the bark of an alder that grows only in standing water, and a particular wild foxglove. The first two can be had at the marsh at the edge of the town. The third, as Verrel's note says in the margin of her letter, is already in your own back garden. You have been looking at it for eleven years.")
                .at("marsh", "Gather the marsh orchid and the alder bark in the marsh, {@marsh}.")
                .outro(
                        "* The marsh is flat and silver and full of sound: frogs, reeds, a distant bell. Pim goes with you. He has brought a lantern, a flask, and a fishing net in case of emergencies. The orchid blooms at midnight on a hummock of black mud. It is the whitest thing you have ever seen. It smells of milk and pepper and a very faint, very distant tea.",
                        "* You take the pollen with a feather. You take the bark with a knife. You do both with a care you have never in your life given a plant. When it is done Pim sits on a stump and eats a cold sausage and says that he has never seen anything so beautiful. He says it to nobody. He says it to the marsh.",
                        "@Pim| I think we're going to be all right, you know. I don't know how I know. But I do.",
                        "* On the way home the lantern goes out. You walk the last mile in the dark by the sound of the sea, and for the first time since the fleet came in, you are not afraid.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("recipe", "Verrel's Last Recipe").gap(16)
                .intro(
                        "* The Tower summons you on a Saturday. Verrel has taken to her bed. The warder, Hilda, meets you at the gate with a tray and a very pale face and says that Madam has asked for you by name. That she is not in pain, and that she is not, the doctor thinks, going to be there much longer. She leads you up the stair. The cell has been scrubbed. There is a bunch of foxglove in a jar on the sill.")
                .wait(4)
                .outro(
                        "@Verrel| You've found the Lull. I thought you would. I'm glad it's you and not Sprott. I have the antidote, dear. It is in my head and not in the book. I never wrote it down.",
                        "* She tells it to you in a low even voice, with her eyes closed. It is simple. It is beautiful. It takes four lines. Three of them are ingredients and the fourth is a method, and the method is the part you do not like.",
                        "@Verrel| It needs a willing dose. You must take the preparation yourself, in the exact amount you mean to give, and go to the place where the sleepers are. It is the only way to know the weight. It is the only honest poison. The kind you take first.",
                        "@You| And if I get the dose wrong?",
                        "@Verrel| Then you will not wake up. I had a master who said that was the third question, dear. Who is it for. What do they think it does. And what happens if you are wrong. She said you must always answer the third one with your own life, or not at all.",
                        "* She sleeps. You sit with her until the lamp burns low. The foxglove in the jar nods once, in a draught from nowhere, and is still.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("palace3", "A Man Who Learns to Eat").gap(24)
                .intro(
                        "* The Palace of the Oldest Order sends a brown cart. The driver is a young Wick with a thin coat and a note in a careful, round, unpractised hand. It says: We have a good many of your sleepers' friends in the Cistern. We should like to help. Please come down to the kitchen. There is soup.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and down to the Under-Kitchen.")
                .outro(
                        "* The Under-Kitchen is the warmest room you have ever entered. A man in a plain apron sits at the long table with a bowl and a spoon, eating with the careful attention of someone who has only lately learned how. He stands when you come in. He sits down again. He stands up. He says his name is ???, and that he is sorry about that, and that he has not been allowed to have another.",
                        "@???| I can tell you what the Lull is. I am allowed that. It is the Hollow Court's gentlest tool. The Court does not kill. It puts out. This is how it does it to a town: a tea, a table, a long sleep. The sleepers are not hurt. They are being emptied, a little at a time, of everything they carry. In a year, they will wake up as no one.",
                        "@You| Can you help?",
                        "@???| I can give you a kitchen, and a pill-maker, and a floor of the Palace to make a great deal of what you need. I cannot give you the dose. It is not mine to give. I am only the reflection of a very tired man.",
                        "* He pushes the bowl toward you. It is the plainest soup in the world. You eat it. It tastes of barley, and onions, and a faint far taste of rain.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("dose", "The Willing Dose").gap(16)
                .intro(
                        "* You make it in the Hall of the Sleepers at night, by lamplight, between the rows of cots, with the Inspector at your shoulder and Pim holding the light. It takes six hours. The preparation is pale gold, like honey, and smells of hay and rain. There is a cup of it at your elbow. There is a second cup, empty, for the antidote you mean to make from the first.",
                        "@Calla| I want it noted that I am against this. I want it noted that I have asked you three times not to. I want it noted that if you do not wake in an hour I shall break every window in the Hall, and I shall also be extremely angry.")
                .wait(4)
                .choice(new Chapters.Choice("dose", "The cup is on the table. You are about to take it.",
                        "Drink it alone, with the Inspector watching, and trust your hands.", "Drink it with Pim holding your wrist and the antidote in his pocket.", "Drink it with the Inspector holding your hand, and tell {calla.him} what you saw.")
                        .after(
                                new String[]{"* You drink it. It tastes of hay and rain and a very faint far tea. Calla watches. {calla.He} does not say a word. After two minutes the lamp seems a long way off, and the room is warm and well arranged, and a pleasant voice says, from the end of a very long hall, that you may put it down.", "@Calla| (very far away) Come back. Please come back."},
                                new String[]{"* You drink it with Pim at your side. He has the little vial in his pocket and his thumb on your pulse. You tell him that he is a better apothecary than he knows. You are gone before he can answer. The last thing you see is the cat on the scales, looking at you with an expression of the most profound disapproval.", "@Pim| (from a long way off) It's all right. I've got you. We've all got you."},
                                new String[]{"* You drink it holding the Inspector's hand. You tell {calla.him} that you are afraid. {calla.He} says that is all right. {calla.He} says that fear is a good sign, in a doctor. The room warms, and fades, and the last thing you feel is a pair of hands closing around yours, steady as a bench.", "@Calla| I'm here. I'm not going anywhere. I'm extremely cross about it, and I'm not going anywhere."}))
                .outro(
                        "* The table has roses on it. The cloth is white. There are twelve places laid and eleven are full: sleepers in their best clothes, smiling, with their hands in their laps. The twelfth is yours. At the head sits Mister Lull, in grey, with a teapot, in a high-backed chair. He rises and bows. He says how kind it is of you to come.",
                        "@Lull| I did say I would call again. I had no idea you would save me the trouble.",
                        "* You do not drink the tea, but you take the second cup, the empty one, out of your sleeve. You put it on the table, and pour into it a few drops from the vial that Verrel taught you to make: the bitter one, the antidote, the honest one. You push it across the white cloth. 'Taste,' you say. 'It's only fair. You've never had one of mine.'",
                        "* He looks at it. He looks at it for ages. Around the table, one by one, the sleepers lift their heads.")
                .pay(600, 0, 0, 2, 1, 2);

        d.chapter("bowls", "Every Bowl in Brinewick").gap(8)
                .setup(TalePoisonerSeasons::mark)
                .intro(
                        "* You wake on the sixth day. You are in your own bed, with the cat on your chest and the Inspector asleep in a chair with a blanket over {calla.his} knees. The shop is silent. Outside the window the street is full of people talking, and the talking is slow, and cheerful, and rather loud, the way people talk who have been somewhere for ages and have come back.",
                        "* Pim is in the doorway with a tray. He says the Hall of the Sleepers is empty. He says the first of them woke on the second morning, a baker, who sat up and said that he had forgotten to light the oven. He says the rest followed in ones and twos. He says there is a great deal of cleaning up.")
                .made(30, "Make the antidote: thirty doses for the town.")
                .outro(
                        "* The Palace sends a cartload of clean bottles and a pill-maker with a very long beard. The Apothecaries' Hall sends dreamwort and apology. The widow of Cutler's Row sends her youngest son with a basket of pears, and he stands in the shop doorway with his straight arm and says gravely that his mother says they are for the cure.",
                        "* The work takes nine days. You give the antidote to every sleeper in Brinewick, and to the sleepers in the next town, and to a woman in the hills who has been asleep since last winter. You stop counting after four hundred. The last bottle goes to the baker's wife, who wakes with a start, and says, 'Where's the bread?' and the whole of Salt Lane laughs.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("morning", "The Morning After").gap(24)
                .intro(
                        "* The quay is crowded. The fleet is going out. Forty boats, with their sails set, and hands at every tiller, and women on the stones with their shawls over their heads, waving. It is a cold, bright, windy morning. The sea is the colour of a knife. The gulls are screaming.")
                .wait(8)
                .outro(
                        "?lull=1|@Lull| (at your elbow, on the quay, in the grey coat) I wanted to say goodbye. I take it as a professional compliment. We very seldom lose a town. We have a great deal to think about.",
                        "?lull=2|@Lull| (at your elbow, on the quay, in the grey coat) I did say I would call again. I am not at all offended. I find I would like to be refused by you for a good many years.",
                        "?lull=3|@Lull| (at your elbow, on the quay, in the grey coat) I have the card. I shall keep it. If the Court ever needs a cup of the honest kind, I will know where to go.",
                        "* He lifts his hat. He walks along the quay among the fishwives, and nobody looks at him. At the end of the pier he stops, and puts down the small black bag, and takes off his gloves, and looks at the sea. Then he is gone, and you cannot say whether he walked away or simply went out.",
                        "@Calla| I will have to file a report. It will say that the cause of the Sleep was a natural phenomenon and that it has passed. It is the biggest lie I have ever written. I find it comes out beautifully.",
                        "* Behind you, in the shop, the cat opens one eye on the scales, and closes it again.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("shop3", "Hemlock and Honey").gap(32)
                .intro(
                        "* Verrel is released in the spring, on a pardon signed by the Mayor, the Inspector, the Master of the Hall, and a petition with two thousand names. She is very frail. She leans on Hilda's arm. She comes up Salt Lane with a ball of green wool in her bag and a smile on her face, stops in front of the shop, and reads the sign.")
                .wait(8)
                .choice(new Chapters.Choice("shop", "Madam Verrel is home, and the shop is yours.",
                        "Keep the apothecary exactly as it is: front shop, back shop, three questions.", "Open a school for antidotes, and teach the harbour to cure as well as it can kill.", "Take the formula to the Palace and work in the pill kitchen, where it can do the most good.")
                        .after(
                                new String[]{"* You keep it as it is. The front shop cures and the back shop does the other thing, and the same pair of hands does both. The sign over the door is repainted every spring. The cat gets fat. There are three questions on a card in the window, and every customer reads them, and some of them even answer.", "@Verrel| That's all it ever was, dear. A shop. I'm so glad you kept it."},
                                new String[]{"* You open a school in the old net-loft. It has forty students by autumn and two hundred in three years, and every one of them can tell you the exact dose that makes the poison, and the exact dose that unmakes it. They call themselves the Third Question. The Inspector attends their graduation, in uniform, with a sprig of foxglove in her buttonhole.", "@Verrel| I think I should like to teach the knitting. I've always wanted a class."},
                                new String[]{"* You take the formula to the Palace in a locked box, with Pim and the cat. The pill kitchen is on the lowest floor, behind a door nobody notices. It is the cleanest place you have ever seen. You put on an apron. A thin man in the next apron says 'Welcome,' and then, after a pause, 'Would you like some soup?' You find that you would.", "@Pim| Is it all right if I make the tea?"}))
                .outro(
                        "* The sign in the shop window changes with the seasons, but the card does not. Three questions before every sale. Who is it for. What do they think it does. What happens if they are wrong. Under it, in Verrel's hand, a fourth line has been added, in very small writing, that you never noticed until the spring: And who will carry the cup?")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* The Apothecary's has three signs. The one over the door says VERREL AND ----. The one in the window says CURES. The third is a card, and it has three questions on it.",
                "?shop=1|* The shop is a shop. It opens at eight and closes at six, and in between it cures a good many things, and sells a few that it does not talk about. The Inspector eats her lunch at the counter on Wednesdays.",
                "?shop=2|* The school in the net-loft turns out four hundred apothecaries a decade, and they are the best in the province, and they know what everything in their hands weighs.",
                "?shop=3|* The pill kitchen on the lowest floor of the Palace has a new cook with a dry manner and an old cat. The soup is better. Nobody has said why.",
                "?lull=1|* A card with a teacup on it lies in the drawer under the scales. It has not been used. You have not thrown it away.",
                "?lull=2|* Once a year, on the first of March, a grey man stands at the end of the quay and watches the fleet go out. He lifts his hat. He does not stay.",
                "?lull=3|* There is a standing arrangement with a polite gentleman, the terms of which are not discussed. A cup of the honest kind goes out on the tide every spring.",
                "?love=1|* The Inspector inspects the new paint every spring. It always passes.",
                "?love=2|* There is a spare chair in the front shop, and it is not spare.",
                "?love=3|* The Chief Inspector has a seat by the stove, at a respectful distance, and a friendly manner, and has long stopped pretending.",
                "* Madam Verrel died at the end of summer, in her own bed, in Salt Lane, with a ball of green wool in her lap and a cat on her feet. The last row of the knitting is plain. It has no repeat. At the very end, in a different green, there are three stitches that you did not at first understand. They spell, if you hold the strip up to the light, one word. Cup.");

        // @@END
    }
}
