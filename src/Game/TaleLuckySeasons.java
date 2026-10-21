package Game;

/** The Lucky Fool, seasons 2 and 3 (see "Start Stories Beat Sheets.md"). Both are comedies about the people who stayed.
 *  Season 2, "The Department of Misfortune" (comedy of errors, the author's comic voice with its puns): Mrs. Fortune is retired, so the Heavens send the human an intern from the other department to even the books
 *  (back luck is due, payable in instalments). Mister Wilbur Hex is dreadful at his job and nice about it. Each chapter is a mishap with one member of the team: Captain Barnacle, Fenn, Marigold, Pip, Master Gu.
 *  The Society of the Fortunate Hat meets the Order of the Bare Head in a contest of hats. Flags: wedding (what Marigold's family was told), lodge (the Society's home), hexgift (what the intern left).
 *  Season 3, "The Clerical Error" (comic and cosmic): Mrs. Fortune returns as an auditor. The human's luck was never hers: it was a gap in the Garden's accounts, a place where nothing was decided. The hearing is before ???.
 *  Flags: ruling (what the human did with the gap). */
final class TaleLuckySeasons {
    private TaleLuckySeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "wedding", "lodge", "hexgift", "ruling");
        d.place("harbour", "wild").place("hall", "far").place("shed", "wild").place("palace", "start");
        d.cast("hex", "Wilbur", "Hex").he().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(2).age(23).at("here").rel(2);
        d.cast("barnacle", "Captain", "Barnacle").he().keep().trait(Character.Traits.Merchant).side("Good").lvl(3).age(66).at("here").rel(3);
        d.cast("fenn", "Fenn", "Applegarth").he().keep().trait(Character.Traits.Alchemist).side("Good").lvl(1).age(19).at("here").rel(3);
        d.cast("marigold", "Marigold", "Dunmore").she().keep().trait(Character.Traits.Friendly).side("Good").lvl(1).age(44).at("here").rel(3);
        d.cast("lucan", "Lucan", "Barrow").he().trait(Character.Traits.Leader).side("Evil").lvl(6).pot(8).age(52).at("place:hall").rel(-2);

        // ================================================================== SEASON 2
        d.season(2, "The Department of Misfortune");

        d.chapter("postcard", "A Postcard from Retirement").years(2)
                .setup((c, st) -> { Chapters.spawn(c, st, "barnacle"); Chapters.spawn(c, st, "fenn"); Chapters.spawn(c, st, "marigold"); Chapters.spawn(c, st, "hex"); })
                .intro(
                        "* The postcard arrives in your sleeve, which is not where postcards usually arrive. It shows a small cloud wearing sunglasses. On the back, in tidy handwriting, it says: 'Having a lovely time. Weather wonderful. Do take care. P.S. A form has gone astray. Don't be alarmed. Mrs. F.'",
                        "* You are alarmed. You are especially alarmed because, as you read it, it begins to rain on the inside of the room, in one very precise column, directly over your head.",
                        "@Pip| That's new.",
                        "* Pip has been editing the Society's newsletter at the kitchen table, in the hat, and has not moved. The rain is not falling on the table. It is falling, with a dedicated look, on you. You move left. It moves left. You move right. It politely follows.",
                        "@Wilbur| Oh, no. Oh, I'm so sorry. You're not meant to be able to see it yet. I had it on a very low setting.",
                        "* There is a young man on the windowsill, about the size of a salt cellar, in a rusty black suit and a very large pair of galoshes, holding a clipboard so tightly that his knuckles have gone white. He has the pale, apologetic face of a person who has never in his life been on time for anything and has always known why.")
                .wait(2)
                .outro(
                        "@Wilbur| Wilbur Hex. Intern, Department of Misfortune, Grade Zero. It's a pleasure. Truly. I've read your file. I loved it. I've never had a file with so many near misses.",
                        "@You| What do you want?",
                        "@Wilbur| It's about the accounts. When Mrs. Fortune retired, the Heavens ran a balance, and it turns out that you're owed, ah, eleven years of ordinary bad luck. Back-dated. With interest. It's perfectly standard. I'm to administer it in instalments. A little at a time. I've been told it's less painful if you don't know when.",
                        "@Pip| You can't just invoice a person for bad luck.",
                        "@Wilbur| Oh, I can, I'm afraid. It's in section nine. I'm very sorry. I'll try to keep the worst of it small.",
                        "* The rain stops. A single drop falls from the ceiling, lands on the end of your nose, and sits there. It is clearly an apology.",
                        "* The Society's tea service, which has stood for two years on the shelf without a chip, picks that moment to slide gracefully off and break into nine pieces. Wilbur looks at it in horror.",
                        "@Wilbur| That wasn't me. I mean it was. But that was the weather.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("friends", "The Hat Gets Crowded").gap(14)
                .intro(
                        "* The Society of the Fortunate Hat has outgrown the kitchen. It has outgrown the back room of the tea house, the loft of the sailor's cousin, and, last Thursday, a field. The newsletter, which Pip insists on calling 'the Chronicle of Wonders', now goes out to eight hundred people and is read aloud at funerals.",
                        "* Meanwhile the intern is working. You can see him at it: a small figure on your shoulder, frowning at a diagram with a pencil behind each ear. Each morning there is a fresh inconvenience. A bootlace snaps. A jar of honey finds a sleeve. A pigeon, with the settled air of a bird that has been paid, takes aim at your hat, and misses, and hits Pip's.",
                        "@Pip| That's the third one today. I'm beginning to take it personally.",
                        "@Wilbur| It's not personal! It's accounts!")
                .friends(8, "Hold the Society together: be friends with eight people.")
                .outro(
                        "* You are not sure how to keep a society from falling apart under a bad-luck audit, so you do the only thing you can think of, which is to make tea for everyone. It takes all afternoon. Eight people drink it. The bad luck, which has to land somewhere, lands on the teapot, which breaks, and on the kettle, which whistles in the wrong key all night.",
                        "* But nobody leaves. They sit on the floor and on the stairs, in their tall black hats, and pass cups. Every so often somebody says 'hat's off to the host' and everybody laughs, and it turns out to be an extremely good evening. Wilbur sits on the mantelpiece with his legs hanging down and a cup the size of a thimble in both hands, and looks at the room with a kind of dazed wonder.",
                        "@Wilbur| It's very different from the Department. In the Department, nobody has ever offered me a cup.",
                        "@Pip| You're welcome to a biscuit as well, you know.",
                        "@Wilbur| I shouldn't. I'd only drop it.",
                        "@Pip| Then I'll hold it for you.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("barehead", "The Order of the Bare Head").gap(12)
                .setup((c, st) -> Chapters.spawn(c, st, "lucan"))
                .intro(
                        "* The Order of the Bare Head has been stewing quietly for two years, and has come to the boil. Its leader is a lean, hungry-looking man called Prior Lucan Barrow, who has not worn a hat since boyhood on principle, and whose scalp, in winter, is a matter of public concern.",
                        "* He sends a letter. It is a challenge, written on his own head, as it were, in letters an inch high. It says that the Society's claims are an insult to common sense, that the Hidden Master is a fraud, and that the Hat is an affectation. It proposes a Contest of the Heads, to be held at {@hall}, in front of witnesses, on the first day of autumn.",
                        "@Pip| He's challenging us to a hat-trick.",
                        "@You| I don't think that's what that means.",
                        "@Pip| Heads up, everyone. This could get cap-sized.")
                .meet("lucan", "Meet Prior Lucan Barrow of the Order of the Bare Head.")
                .outro(
                        "* The Prior receives you on the steps of his lodge, in the cold, with no hat and no cloak. He has a high, bald, shining head, and the pinched look of a man who has been very sincere for a very long time. He does not ask you to sit.",
                        "@Lucan| I do not claim you are a bad person. I claim that you are a coincidence, and that the world is full of people who would rather believe in a miracle than in a coincidence. I am here to speak for the coincidence. Someone must.",
                        "@You| I'd rather be a coincidence than a miracle. It's less to live up to.",
                        "@Lucan| That is a very sensible thing to say, which makes me trust you less.",
                        "* He is not stupid. That is the worst of it. He has read the Chronicle of Wonders in its entirety, with a pencil, and has, in the margins, written the correct explanation for each wonder: a loose beam, a bad soup, a tent peg. He has been right about every single one. It has made him very unhappy.",
                        "@Wilbur| (from your shoulder, very low) I like him. Is that wrong?")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("boat", "Captain Barnacle's Boat").gap(10)
                .intro(
                        "* The retired sailor has not been to sea in fifteen years, and every year of it he has talked about the boat. She is called the Bonny Mistake, and she lies at a mooring at the end of a long grey shingle spit, and she is, he says, as sound as the day he laid her up.",
                        "* She is not. She has a hole in her. She has a barnacle colony in her. She has one mast and what seems to be a family of herons in the rigging. But Captain Barnacle looks at her the way a man looks at a letter from home, and you do not have the heart. The Society will go sailing.",
                        "@Barnacle| She'll float. She floated in '07. And in '09, for most of it.",
                        "@Wilbur| I really think someone should say something about the hole.",
                        "@Barnacle| It's a drainage feature.")
                .at("harbour", "Go with Captain Barnacle to his old boat at {@harbour}.")
                .outro(
                        "* The Bonny Mistake goes out on the morning tide with eleven of the Society aboard, six buckets, a case of pickled herring, and an intern in the binnacle. She leaks, but in a cheerful and well-organised way. The herons stay. The sea is the colour of a slate, and calm, and for an hour you think that nothing can go wrong.",
                        "* Then Wilbur sneezes. It is a very small sneeze. The wind drops, and the sail goes slack, and the Bonny Mistake, with an almost human sigh of resignation, sits down on a sandbar that does not appear on any chart. Everyone looks at the intern.",
                        "@Wilbur| It's not me! It's the instalment! It's due on the fifteenth!",
                        "* It is while you are all in the water, pushing, with the tide coming in, that a pale grey shape rises from the sandbar next to you, with a long serious face. It is a whale. It is about the size of a small hill. It looks at the boat, and at the eleven people, and at the herons, and it puts its shoulder under the keel, with great courtesy, and lifts.",
                        "@Barnacle| Well, would you look at that. I told you she'd float.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("exam", "Fenn's Examination").gap(12)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* Fenn Applegarth has been an apprentice apothecary for six years and has failed his journeyman's examination four times, for reasons that are described on the sheet in language so kind that it is clearly a form of cruelty. He sits at the kitchen table with his hands clasped round a mug, as pale as milk.",
                        "@Fenn| It's the fifth. If I don't pass, they'll put me back to sweeping. The examiner is the Dame Prioress, and she looks at me as though I were a small, interesting stain.",
                        "@You| What does she test?",
                        "@Fenn| Three pills. Cough, fever, and the one for sleep. I'm all right at making them. It's just that every time I make one, something happens. I always think it's me.",
                        "* You look at the intern, who has the grace to look at his shoes.")
                .made(3, "Help Fenn prepare: make three pills with him, cough, fever and sleep.")
                .outro(
                        "* The examination is held in a hall of pale stone, under a clock that is a minute fast. The Dame Prioress is a stately woman with the bearing of a ship under sail. She watches Fenn work. She is not unkind. She is simply thorough. She holds the first pill up to the light.",
                        "* The first pill is fine. The second is fine. For the third, Wilbur, who has been sitting very still in Fenn's pocket in an agony of conscience, loses his nerve and makes a small, decisive gesture. The bottle of sleeping draught in front of Fenn tips, and spills across the bench, and across the Dame Prioress's sleeve, and she looks down at it and says, in a flat voice, 'Oh dear.'",
                        "* There is a long moment. Fenn takes a cloth from his belt, wipes the Prioress's cuff, makes up a second draught from nothing, steadily, without a tremor, and hands her the cup. Then he says the thing he has been told never to say.",
                        "@Fenn| I'm very sorry. It was my fault. I knocked it. I'll do it again.",
                        "@Prioress| You will not. You have done it already. Twice in one morning I have seen a boy mend a catastrophe, and in six years I have never seen him once blame the bottle. Welcome to the profession, Master Applegarth.",
                        "* Outside, on the steps, Fenn is crying and laughing at the same time, with the certificate in both hands. Wilbur sits on his shoulder, patting his ear.",
                        "@Wilbur| I'd like it noted that I didn't mean to be useful.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("miracle", "Fenn Performs a Miracle").gap(6)
                .intro(
                        "* It happens at the market, on a Saturday, by accident. Fenn has set up a little table with a jar of salve and a card that says FREE ADVICE, because he cannot bring himself to charge. A woman has come to him with a child on her hip and a look on her face that is very much worse than the child's cough.",
                        "@Woman| It's not him. It's me. I can't hear. Not since the winter. A fever. I can see people's mouths going, and I can't tell what they say.",
                        "* Fenn is twenty-one. He has a certificate. He has, in his bag, nine pills, two bandages, and a book that is mostly about feet. He looks at the woman, and at the child, and at the sky, where a small man in a rusty suit is lying very flat on a cloud with his hands over his eyes.")
                .wait(1)
                .outro(
                        "* He does the only thing he can. He sits her down, and he looks in her ears, and he finds nothing. He finds nothing at all, and he says so. And then, because he cannot bear it, he leans forward and speaks very slowly and clearly, with his mouth where she can see it, and tells her everything he knows about ears. It takes ten minutes. He explains the drum. He explains the little bones. He explains, finally, in a low voice, that he has no idea what he is doing.",
                        "* On the word 'doing' there is a small, damp, startling pop, like a cork drawn from a bottle. The woman starts. She puts her hand to her head. Her eyes go wide, and wet, and she looks at the child on her hip, who has just said, quite clearly, with its mouth full of fist: 'Mama.'",
                        "@Woman| I heard that. I heard him. Say it again.",
                        "* The child says it again. The whole market turns round. A stall of eggs falls over. Fenn sits very still, his hands in his lap, like a man who has dropped a very expensive plate and watched it land on a cushion.",
                        "@Wilbur| (from the cloud, in a faint, strangled voice) Don't look at me. I didn't. That was the instalment. It had to land somewhere, and the only place it could was a wax plug the size of a pea. It's been there since December. I merely reversed the polarity.",
                        "* The story goes through Salt Landing like a rumour through a nunnery. By evening there is a queue at Fenn's table. He cures nothing else that week. He tells them all about ears. He does it slowly and clearly, and he explains the drum, and nobody asks for their money back, and the pot of salve is empty by Tuesday.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("cousin", "Cousin Marigold").gap(10)
                .intro(
                        "* Marigold Dunmore is the woman who, on a wall, over a loaf, once mistook you for her cousin, and who has never said that she was wrong. She has been with the Society since the beginning. She makes the best dumplings in six counties. She has never had a cousin.",
                        "* You find that out because you find the letters. They are in a shoebox under the stairs, put there by a friend who is cleaning up and does not read them. They are all addressed to a Cousin Ellis, who does not exist, and they are all about her day.",
                        "@Marigold| Oh, you found those. I did wonder. Please don't be upset. I've always been rather a lonely sort of person, and it was a good letter to be writing to. It never wrote back.")
                .meet("marigold", "Talk to Marigold about the letters.")
                .outro(
                        "* You sit with her on the back step with the shoebox between you, and she tells you the whole thing. How she saw a person on a wall, in a hat, laughing, with a crowd around them, and thought: there, that's someone who has a cousin. How she said it before she could stop. How you had said, 'Of course!', and let her sit down, and nobody had ever done that before.",
                        "@Marigold| I should have corrected it. I didn't. I thought: for a minute, I'll have someone. And a minute turned into ten years, and here we are.",
                        "@You| You could have just said hello.",
                        "@Marigold| Dear, I know. I've never been brave enough to say hello. I'm brave enough to say cousin.")
                .outChoice(new Chapters.Choice("wedding", "Marigold has a real family somewhere, who have never been written to.",
                        "Tell her she is your cousin, whatever the family tree says.", "Offer to find her real family and go with her.", "Say nothing: let her write the next letter to you.")
                        .after(
                                new String[]{"@Marigold| Oh, don't. You'll set me off. I've been making dumplings all week for a wedding that isn't mine. Come along. Bring the hat. We'll need the hat.", "* Nobody in the Society ever asks. Nobody in the Society ever needs to. By the end of the month there are sixty cousins in tall black hats, and they all have her nose."},
                                new String[]{"* It takes you a month to find them. They live three towns away, in a house with a blue door, and they are, it turns out, extremely sorry. They had written. The letters had gone to the wrong cousin. It is the kind of mistake that makes you believe in the Department.", "@Marigold| I always thought the clerical error was in my favour. It seems the clerks were just having a bad week."},
                                new String[]{"* The first letter comes on a Tuesday. It is addressed to you, in her tidy hand, and it begins 'Dear Cousin'. You answer it at once. It is the first time in ten years that anybody has written back."}))
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("fish", "Master Gu Comes With Fish").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "gu"))
                .intro(
                        "* Master Gu arrives in the evening with a basket of fish on each arm, as he has every year, and for the first time in your acquaintance he does not look hard. He sets the baskets down in the doorway and stands with his hands folded in the sleeves of his coat. His hair has gone white at the sides.",
                        "@Gu| I have a request. It is a considerable one. I have beaten every man in my school for thirty years, and have never lost. Not once. I lost to you, and it was the finest afternoon of my life. I have been trying to learn how.",
                        "@You| To lose?",
                        "@Gu| To be defeated by a lucky thing. I teach discipline. My students are all very good at it. They are not happy. I would like them to be happy for one afternoon, and to be unprepared, and to fall in a pond.")
                .wait(4)
                .outro(
                        "* The training takes a month. It is the strangest school the Iron Wind has ever seen. Master Gu stands in the yard in his best robe with his arms folded. A line of two hundred of the finest fighters in the province stands in front of him. 'Today,' he says, in a voice of iron, 'we shall practise slipping on soap.'",
                        "* The first day is a disaster. By the second, some of them are laughing. By the end of the week, a boy of fifteen has fallen in the pond three times and come up shining. Wilbur, who has been taking notes on a leaf, is writing a report that begins: 'Subject has been kind to others in a manner not provided for in the schedule.'",
                        "@Gu| I shall not forget this. A school should have a pond in it.")
                .pay(0, 0, 1, 0, 0, 1);

        d.chapter("coins", "A Thousand Tosses").gap(4)
                .intro(
                        "* The Prior of the Bare Head has been doing sums. He announces it at the Society's quarterly tea, by sending a note to the tea, with a footnote. It says that he has devised an experiment. It says that it will settle the question of the Hat for ever, and that he proposes to conduct it on the village green, in front of witnesses, with a coin.",
                        "@Pip| He wants you to toss a coin a thousand times.",
                        "@You| What happens if I get a thousand heads?",
                        "@Pip| Then he's wrong. And if you get five hundred, then he's right. And if you get anything in between, it's what he calls a 'regression to the mean', which I'm told is a kind of weather.")
                .wait(1)
                .outro(
                        "* They do it on a Saturday. Eleven hundred people come. The coin is a plain brass one that Lucan has weighed, measured, and bitten. A clerk with a tally sits at a table on the left, and another on the right. Wilbur is up on the crossbeam of the bandstand with a notebook, sweating. He has made it clear that he will not interfere, that he will not on any account interfere, and has sat on his hands to prove it.",
                        "* The first hundred are even. Fifty-one heads. Lucan looks relieved. The second hundred are even. The third are even. By the fifth hundred the village green is as silent as a church, and a boy selling pies is whispering the price like a prayer. The count is two hundred and fifty-three to two hundred and forty-seven, and the Prior has taken out a handkerchief.",
                        "* At the six hundred and twelfth toss, the coin lands on its edge. It stands there, upright, in the dust, between two blades of grass, humming slightly, and does not fall. Nobody breathes. A bee goes by. It stays there for the whole of a minute, and then, in a voice of the utmost care, Wilbur says from the crossbeam: 'It's the instalment. I'm so sorry. I'd like it entered that I tried.'",
                        "@Lucan| That is not a head.",
                        "@Pip| It's not a tail.",
                        "@Lucan| It is not anything. It is a standing coin. It is a result outside the model.",
                        "* He sits down on the grass, heavily, and takes off his collar. He begins, very slowly, to laugh. It is a wheezing, shaking, helpless laugh that goes on until the clerks have laid down their pens and the crowd has begun, uncertainly, to join in. 'A thousand tosses,' he says. 'A hundred sums. A model. And it stands on its edge.' He wipes his eyes. 'Put it in the Chronicle, Tallow. I will write the footnote.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("contest", "The Contest of the Heads").gap(8)
                .intro(
                        "* The Contest of the Heads is held in the great hall at {@hall}, under a banner that has been hastily relettered. The Society of the Fortunate Hat has come in force, eight hundred strong, in tall black hats, all of them singing a hymn written by Pip, which has eleven verses and no tune. The Order of the Bare Head has come in two hundred, in no hats at all, and with a great deal of dignity.",
                        "* The rules are simple. Each side will put up a champion. The champion of the Bare Head is the Prior. The champion of the Hat, by acclamation, is you, and you spend the first part of the speeches trying to hide under a table.",
                        "@Lucan| I do not wish to hurt you. I wish to prove a point. When I have won, you will say before witnesses that you are a person of ordinary luck, and the Society will go home, and the world will be simpler.",
                        "@Pip| This is a very good speech. I'm so sorry for you.")
                .defeat("lucan", "Face Prior Lucan Barrow in the Contest of the Heads.")
                .outro(
                        "?dead.lucan|* It goes badly for the Prior, in the way that things go badly for people who are entirely right. A loose slate falls from the roof. A pigeon has been waiting its whole life for this. The Prior goes down, and is carried out, and the great hall is silent. Pip's hymn stops on the fourth verse. Nobody at all feels like cheering.",
                        "?!dead.lucan|* It goes badly for the Prior, in the way that things go badly for people who are entirely right. A loose slate falls from the roof. A pigeon has been waiting its whole life for this. The Prior goes down, and sits up, and, to the astonishment of the hall, begins to laugh, a great cracked rusty laugh, as if something had been unlocked in him. 'A coincidence,' he says. 'Of course. What else.'",
                        "?!dead.lucan|@Lucan| I shall write the whole thing down. Properly. With the correct explanation in the margin. And then I shall put a hat on the cover.",
                        "@Wilbur| I'd like it recorded that the pigeon was not mine.",
                        "* In the gallery, a boy of fifteen, who has fallen in a pond three times, is the first to stand and applaud.")
                .pay(3000, 0, 0, 1, 0, 1);

        d.chapter("chronicle", "The Chronicle of Wonders, Volume Nine").gap(4)
                .intro(
                        "* Pip has put out another issue. It arrives, as they all do, on the kitchen table, folded in four and weighted with a boiled egg. The masthead reads THE CHRONICLE OF WONDERS, ORGAN OF THE SOCIETY OF THE FORTUNATE HAT, and under it, in smaller print, NOW WITH CORRECTIONS. The corrections are in pencil. They are in a hand about a quarter of an inch high.",
                        "* You read it standing up. Pip is watching you over the top of his tea, with the face of a man who has put his heart into a thing and is trying to look as if he has not.")
                .wait(1)
                .outro(
                        "* FROM THE EDITOR'S DESK. 'Friends, hats, fellow Fortunate. It has been a season of marvels. I report that the Master, on the fifteenth of the month, did not drown. This is the fourth time. I should like it known that the pond in question was shallow and that the Master was standing in it at the time. I do not say this to diminish the miracle. I say it so that it may be fully appreciated.'",
                        "* [In the margin, in pencil: It was an instalment. I'm sorry. -W.]",
                        "* A NOTE ON THE BOAT. 'Captain Barnacle's vessel, the Bonny Mistake, was lifted off a sandbar by a whale. Eyewitnesses differ as to its size. The Captain says a house. Marigold says a barn. Fenn, who was at the time being sick over the side, says only that it was large and that it had a kind face. The Society wishes to record its thanks to the whale. A fund has been opened. It stands at two buttons and a sprat.'",
                        "* [In the margin: The whale is not within my jurisdiction. I have checked. -W.]",
                        "* THE MASTER'S OPINION. 'We asked the Master to comment on the recent run of events. The Master said, and I quote, I tripped. This is the whole of the statement. It is, as readers will know, what a Hidden Master would say. We have framed it.'",
                        "* CORRESPONDENCE. 'A reader in the north writes to ask whether the Hat can cure gout. We are not qualified to say. We note, however, that the reader enclosed a pound of excellent butter, and that Fenn has asked to be told the address.'",
                        "* A PERSONAL WORD. 'Some of you have asked why I do it. It is a fair question. I was twelve when I met the Master, though the Master did not know I was the one who put the peach in the pocket, and I have been writing it down ever since. I write it all down because it is the only way I know to say that I was there. If I did not, I think it would turn into a story, and stories get told by other people. This one is mine.'",
                        "* [In the margin, very small: I read this one three times. -W.]",
                        "* Pip says nothing while you read it. At the end he hands you the egg. 'It's the fourth printing,' he says. 'I thought you'd want to know it was boiled.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("leave", "Notice of Termination").gap(6)
                .intro(
                        "* The letter comes in a plain grey envelope, with a seal like a closed eye. Wilbur opens it with both hands. He reads it standing on the edge of the mantelpiece. He folds it into eight and puts it inside his coat.",
                        "@Wilbur| It's my review. I've been recalled. The Department says I have been, and I'm quoting, 'unacceptably fond of the subject'. It's the first time I've ever been described as anything. I'm afraid I'm to go back on Thursday.",
                        "@Pip| They can't take him! He's one of us!",
                        "@Wilbur| I'm one of the Department's, really. It's a bit awkward. But I've been thinking. I haven't collected the full eleven years. I've only done four. If I stayed on, I could, in theory, finish.",
                        "@You| And if you go?",
                        "@Wilbur| Then the rest of it is cancelled. The Heavens are very good about forgiving a debt, once the clerk has left. They just don't tell you.")
                .wait(2)
                .outChoice(new Chapters.Choice("hexgift", "Wilbur has three days left.",
                        "Let him go, and write him a very good reference.", "Ask him to stay and finish the debt: you would rather owe the misfortune than lose a friend.", "Give him the Society's hat, and the freedom of the house.")
                        .after(
                                new String[]{"@Wilbur| 'A diligent and thoroughly appalling employee.' That's the nicest thing anyone's ever written. I'll frame it. I'll hang it over my desk in the Department, where nobody will see it.", "* He shakes your hand, with the whole of his, which is a thumb."},
                                new String[]{"@Wilbur| Oh, you daft thing. You're the first person in recorded history to volunteer for an audit.", "* He stays for seven more years. They are, by common consent, the worst seven of your life. You wouldn't trade a day of them."},
                                new String[]{"* It is a tiny hat. Fenn makes it, out of a thimble and some felt, in an afternoon. Wilbur puts it on, and looks at himself in a spoon, and says nothing at all for a good minute. When he leaves, on Thursday, he is wearing it."}))
                .outro(
                        "?hexgift=1|* The grey envelope is back on Thursday, and Wilbur with it. 'Reinstated,' he says. 'With a rise. They said it was an error.' He is crying, a little. He is also, you notice, taller.",
                        "?hexgift=2|* The debt is paid by Tuesday week. The intern stays on. He sleeps in the teapot, when it is mended, and has opinions on the garden.",
                        "?hexgift=3|* He waves, from the top of the lane, with the thimble hat on, until you cannot see him. You do not know if he is crying. It is difficult to tell, at that size.",
                        "* The Society of the Fortunate Hat meets that night in the back room of the tea house, in the usual way, and sings its hymn, all eleven verses, and for once it has a tune.")
                .pay(5000, 0, 0, 2, 1, 1);

        d.interlude(2, "A Quiet Year",
                "* The Society holds the first of its Annual Contests of the Heads in the spring. It is a picnic. Nobody wins. Everybody falls in the pond.",
                "?hexgift=2|* Wilbur Hex is a household fixture, a small figure in a rusty suit who sorts the post and eats the crusts. The bad luck, which he has promised to keep to a minimum, happens mostly to the neighbours.",
                "?!hexgift=2|* Postcards come, at odd intervals. One shows a cloud in a hat. One shows a very small man at a very large desk.",
                "?wedding=1|* Marigold has sixty cousins now and writes to them all. She has not lost the habit of writing to Ellis. It has become the Society's newsletter.",
                "?wedding=2|* Marigold's family has a blue door and a spare bed. She spends the winters with them and the summers with you, and takes dumplings in both directions.",
                "?wedding=3|* Marigold's letters come on a Tuesday. You have kept every one of them, in the shoebox, under the stairs.",
                "* On a clear night in autumn, something very small clears its throat in your sleeve. There is a smell of ink and tea and a faint, sensible perfume. It is a form. A new one.");

        // ================================================================== SEASON 3
        d.season(3, "The Clerical Error");

        d.chapter("audit", "The Audit").years(3)
                .intro(
                        "* The auditor arrives on a Wednesday, in the rain, and the rain, for once, is a perfectly ordinary rain. She is a very small woman in a neat grey dress and an immense pair of spectacles, and she has a clipboard under one arm and an umbrella over the other. She is very much older than when you saw her last, and entirely the same.",
                        "@Mrs. Fortune| Hello, dear. Don't get up. I'm afraid this isn't a social visit. I'm retired, officially, but they've called me back as a consultant, and it's nothing short of a scandal. They'd like me to explain something. I can't. That's the trouble.",
                        "* She sits on the arm of the chair. She puts her clipboard in her lap. She takes off her spectacles, and cleans them on her sleeve, and puts them back on, and looks at you like a doctor with a result.",
                        "@Mrs. Fortune| The books don't balance, you see. They never did. I always assumed it was a clerical error. And I've gone through the ledgers three times, and it isn't. You have luck, dear, that is not mine.")
                .wait(2)
                .outro(
                        "@Mrs. Fortune| I was assigned to you for eleven years. In those eleven years you collected a good deal of good fortune that I did not give you. I had a quota. I did the sums. I did not do enough. I did about a third.",
                        "@You| So where did the rest come from?",
                        "@Mrs. Fortune| That's what I should like to know. I think you were already lucky when I got there. I think you were lucky before the error. I think the error happened because you were lucky, and a senior clerk saw a person with a lot of fortune and assumed I was the cause.",
                        "* She folds her hands. She looks, for the first time since you met her, afraid.",
                        "@Mrs. Fortune| They want a hearing. A proper one. They have told me it will be before the Master of the Palace. I did not know he did hearings.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("pack", "Packing for Heaven").gap(2)
                .intro(
                        "* There is, it emerges, no guidebook. You ask Pip, who writes everything down, and Pip says that he has a folder but it is mostly about hats. You ask Mrs. Fortune. She says that the Department is fifty yards from the sky and a hundred yards from lunch, and that one should bring a coat. You ask Wilbur, in his letter, and his reply, by return, runs to four pages and a map, and is mostly a list of things not to say to the clerks.",
                        "@Marigold| I'm bringing dumplings. Don't argue. Fenn, what have you got in that bag?",
                        "@Fenn| A tourniquet. A splint. Three bandages. A little something for nerves. And a book I'm hoping to be told is wrong.")
                .wait(1)
                .outro(
                        "* The packing takes a day and a half. Captain Barnacle brings a compass, a barometer, a pickled herring, and a tin whistle, and says that you can never tell. Master Gu arrives at dawn with a bucket containing a small carp, and explains that it is for the pond. Nobody knows what pond. He seems to feel that there will be one.",
                        "* The Prior of the Bare Head turns up at the last minute with a satchel and a look of enormous diffidence. He says that he has no reason to come, that it is no concern of his, and that he has put on a clean collar. Pip, who has not spoken a word to him in three years that was not in print, looks at the collar, and at the satchel, and at his own hat, and takes it off. He holds it out.",
                        "@Pip| Go on. It's a day out.",
                        "@Lucan| I couldn't possibly.",
                        "@Pip| Try it on. It's only a hat.",
                        "* The Prior puts it on. It is too large. It comes down over his ears, which are pink. He stands in the kitchen with his hands at his sides and the look of a man who has discovered, at fifty-two, that there was a door in the wall beside his bed.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("shed", "The Department of Seeds and Weather").gap(6)
                .intro(
                        "* The Department of Seeds and Weather is not in the sky. This disappoints you. It is a long low shed at the edge of a wood, with a tin roof and a sign that has been repainted so many times that the lettering stands out in relief. There is a row of boots at the door, and a kettle on a stove inside, and a smell of cut grass and warm iron and old pencils.",
                        "* Mrs. Fortune leads you in with the confidence of a woman who has not been here in years and has not forgotten where the biscuits are. There are rows and rows of desks, each with a lamp, each with a clerk. Some are people. Some are very small. One is a heron. They are all writing, in books as tall as doors.",
                        "@Mrs. Fortune| This is the Fortunes. The Misfortunes are in the next shed. We don't speak. It's very silly.")
                .at("shed", "Go to the long low shed at {@shed}, where the Department keeps its books.")
                .outro(
                        "* The ledger of your life is a slim grey volume, and the clerk who brings it, a girl of about ten with ink up to the elbow, hands it over with a wince. It has your name on the spine. Inside, in a hand that is not a clerk's but something older, there are nine pages.",
                        "* Seven of them are full. Two are blank. And in the middle of the seventh there is a hole, a clean round hole, the size of a thumb, with the paper burnt away at the edge. No ink. No line through it. It is just the place where a fact should be.",
                        "@Mrs. Fortune| That is your luck, dear. That's where it comes from. It's not a gift and it's not a theft. It's a gap in the accounts.",
                        "@Wilbur| (from the other shed, faintly, through the wall) Is it good? I've always wondered what they put in the middle of the pages!")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("misfortune", "The Other Shed").gap(2)
                .intro(
                        "* You are not supposed to go next door. Mrs. Fortune says so twice. It is perfectly all right, she says, as long as nobody touches the kettle. You never ask about the kettle. You follow a corridor so narrow that the walls give off a faint smell of old rain, and at the end of it you push open a door that has a sign: DEPARTMENT OF MISFORTUNE. BRANCH OF ACCIDENTS. SUB-BRANCH OF STUBBED TOES. KNOCK AND WAIT.",
                        "* You knock. You wait. A long time later a very small woman looks over the top of a very tall desk, with glasses on a chain, and a face like a closed umbrella.",
                        "@Mrs. Dread| Yes?")
                .wait(1)
                .outro(
                        "* Mrs. Dread is a Misfortune Attendant, Grade Seven, and has been so for four hundred years. She wears black, and says so, and that nobody has asked her why. She makes you tea. She has a kettle that does not boil. She has made tea in it every morning for four centuries, and every morning it has failed, and every morning she has drunk the result, lukewarm, with an air of calm and total satisfaction.",
                        "@Mrs. Dread| People think we enjoy it. We don't. We like order. A misfortune that is properly filed is a misfortune that stays put. It's the loose ones that do the damage.",
                        "@You| Wilbur said he was assigned to me for an instalment of eleven years.",
                        "@Mrs. Dread| Did he? How very conscientious. Let me look.",
                        "* She goes through a drawer. Then another. Then a filing cabinet of black iron the size of a hearse. She finds the slip in a folder marked MISCELLANEOUS, UNCOLLECTED, SENTIMENTAL, and reads it, and puts it down, and takes off her spectacles, and polishes them for a moment on her sleeve.",
                        "@Mrs. Dread| It was waived. In the first year. The debt was waived on the fourth of March. It says here, 'Subject has shown no ill effects. Case closed. No further action.' He has been administering a debt that did not exist.",
                        "@You| Why?",
                        "@Mrs. Dread| I should think because it was the only thing that gave him a reason to stay. He is a very poor clerk. He is, I have long thought, rather a nice person. It is an unfortunate combination for a department like ours.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("hexback", "Wilbur Returns").gap(4)
                .intro(
                        "* He arrives in a taxi. It is not a taxi. It is a very large snail with a lamp on its back, and Wilbur steps down from it with the dignity of a man who has been promoted. He wears a neat new suit, in a good grey, with a silver pin on the lapel. The galoshes are the same. They have been polished.",
                        "@Wilbur| Grade One! I'm Grade One, now. I've a drawer. I was told to wait in the corridor, and then I was told to come in, and then there was a man with a stamp, and now I have a drawer.",
                        "* He is delighted. He is also pale, and he glances over his shoulder, and lowers his voice.",
                        "@Wilbur| I've been seconded to the hearing. As a witness. I'm not meant to say anything, but I believe there's a view in the Department that your file shouldn't exist. That there is no such thing as luck without an account. They'd like it closed. The hearing is to decide whether it can be.")
                .wait(2)
                .outro(
                        "* He sits on your finger, which he has never done before, and holds on to your knuckle with both arms.",
                        "@Wilbur| I'd like you to know that I will tell the truth. I'm not very good at anything, but I am good at that. I filed the full report. It says: subject is a nuisance, an annoyance, a menace to schedules, and the best-behaved person I've ever been assigned. It's the only thing I ever wrote that I'm proud of.",
                        "* The snail outside lets out a long, low, comfortable sound, like a kettle taken off the heat.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("desk", "Wilbur Gets a Desk").gap(2)
                .intro(
                        "* It is a very small desk. He has been shown it by a very large clerk in a corridor full of other desks. It is made of dark wood, with brass handles, a green leather top, and a lamp with a shade like a mushroom, and it is exactly the size for a person who stands six inches high. It has a drawer. It has a name-plate. The name-plate says W. HEX. GRADE ONE.",
                        "@Wilbur| It's got a drawer! It's got a drawer with a lock! I've put my sandwich in it!",
                        "* He is so happy that he cannot sit still. He climbs on to the chair and off again. He runs his hands along the edge of the desk, and opens and shuts the drawer, and puts his head in it. You are in the corridor to see him settle, and Mrs. Fortune, at your elbow, with her clipboard, watching him with a look of the most complete, exasperated, maternal pride.")
                .wait(1)
                .outro(
                        "* He has a visitor at three. He is a clerk of the Second Misfortune, a hard narrow person with a monocle and a briefcase. He stands before the desk and looks at it, and at the name-plate, and at the boy behind it. In a voice of cold and chiselled disapproval, he says that he has come to deliver the new quotas.",
                        "@Clerk| Seven mishaps a day. One a day of the third degree. A weekly flood, to be assigned at your discretion. This is the standard allocation for a Grade One.",
                        "@Wilbur| I won't.",
                        "* The room goes quiet. The clerk of the Second Misfortune raises an eyebrow, which is a movement of the whole forehead. Wilbur stands up on his chair. He is very pale. He has both hands flat on the green leather. He says, in a high thin clear voice that shakes at the edges:",
                        "@Wilbur| I won't do a flood. I haven't done a flood in my life. I think a flood is a lot. I'll do a stubbed toe. I'll do a spilt cup. I'll do a damp stocking. I'll do as many as you like of the small ones. But the people I'm assigned to are my friends, and I'll not do them a flood.",
                        "* The clerk looks at him for a very long time. Then he takes out a pencil, and writes in the margin of the form: AMENDED. He puts the form in the briefcase and snaps it shut. At the door, with his back turned, he says in the same dry voice: 'The last Grade One to refuse a flood is now Head of Department. I should keep the drawer locked.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("team", "The Team Goes Up").gap(4)
                .intro(
                        "* The Palace of the Oldest Order has never, in its history, admitted a deputation of this kind. It has admitted generals and tricksters and one very large goose, but it has never before had to find chairs for eight hundred people in tall black hats. The Society has come with its newsletter and its hymn. It has not been told it cannot.",
                        "@Pip| It's a matter of solidarity. And of hats. They said no more than thirty at the gate, and we pointed out that it's thirty at a time.",
                        "@Barnacle| I've brought the herring.",
                        "@Marigold| I've brought dumplings. Don't look at me like that. It's a hearing. People get hungry at hearings.",
                        "* Fenn has brought a medical bag. Master Gu has brought a pond, in a bucket. The Prior of the Bare Head has brought nothing at all. He has come, he explains, only to observe. He stands at the back with his head bare and his notebook open, and every so often writes something down and underlines it.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, for the hearing.")
                .outro(
                        "* They are shown to the great hall, which is not a courtroom, because the Palace does not have one. It has a kitchen. In the kitchen, at the long table, sits an ordinary man of no age in a plain apron. He has a bowl of soup in front of him and a very small gavel, which he holds as though it were a live bird.",
                        "@???| Good afternoon. I'm not allowed to tell you who I am. That one I can keep. I've been looking forward to this very much. I have always wanted to say 'order in the court'. Is it a court? I wasn't told.",
                        "@Pip| It is now.",
                        "@???| Oh, good. Order in the court.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("hearing", "The Hearing Before ???").gap(1)
                .intro(
                        "* The hearing is long, absurd, and moving in about equal parts. Mrs. Fortune presents the ledgers. A clerk from the Department reads aloud, with a stammer, from the section on 'irregularities of unassigned origin'. Wilbur reads his report and has to stop twice. Pip reads from the Chronicle of Wonders, which takes an hour, and on which nobody has the heart to interrupt him.",
                        "* ??? listens to it all with the greatest attention, nodding, with his soup going cold. Twice he writes something down. Once he stops the proceedings to ask what a 'cousin Ellis' is. He is told. He is quiet for a moment.",
                        "@???| I find that I am a poor judge of luck. I have none. I was made, you see. There is nothing left to chance in me. I am all quite intended. It is an odd thing to be, in a room full of people like you.")
                .wait(1)
                .outro(
                        "@???| But I can tell you what the gap is. I have looked at the books. I am not allowed to say how I know. I am allowed to say that there are places in the Garden where nobody decided. The one who made it was in a hurry. He was fighting a war. He did not fill in every page. There are blanks. There are a very few people who were made on the blanks, and for them, things are not settled. They can go either way.",
                        "@???| It is not luck. It is not good fortune or bad. It is only the freedom of the thing. You were born in a place where the future had not been written. And you filled it in, as you went, with friends.",
                        "* The hall is silent. Somewhere at the back, a very large goose, who has no business being there, honks, once, in a tone of vindication.",
                        "@Mrs. Fortune| So it was never a clerical error.",
                        "@???| It was a gap. I think that is much better. A clerical error is something wrong. A gap is somewhere to put things.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("recess", "The Recess").gap(1)
                .intro(
                        "* The hearing is adjourned at noon for soup. It is the only adjournment in the Palace's history, and nobody is quite sure of the form. They are shown, by a young Wick, to a long stone corridor with a bench along one wall, and there they sit, the whole Society, in their tall black hats, in a row, eating dumplings out of a basket.",
                        "* It is a kind of hush. For the first time in two hours nobody has anything to say. Marigold passes the dumplings. Captain Barnacle polishes his compass on his sleeve. Master Gu, at the end, has the carp in its bucket on his knees and is looking into the water with a deep and professional attention.")
                .wait(1)
                .outro(
                        "@Fenn| Do you think they'll close it? The gap?",
                        "@Marigold| I think it depends on what they think it's for.",
                        "@Barnacle| I never knew I was lucky. I thought I was just old. Thirty years of sailing and I never once went down. I always put it down to the rum.",
                        "@Gu| I spent my life believing a person was the sum of what he did. I trained for forty years to be a certain weight. Then a bar of soap. I was in the pond for ten minutes. It was the finest ten minutes of my life.",
                        "* They look at one another. Nobody is embarrassed. It is the kind of thing that can only be said in a corridor, on a stone bench, with a mouth full of dumpling. Fenn takes off his hat and turns it over, and looks inside, as if for an answer.",
                        "@Fenn| I used to think I was the unlucky one. Six years of failing. And then it turned out that was where I met all of you.",
                        "@Marigold| That's what a gap is, dear. It's where a person falls through and lands on the others.",
                        "* The young Wick comes to say that they are ready. They file back in, one by one, in their hats. At the door stands the Prior of the Bare Head, who has been outside the whole time with his notebook. He looks along the line of them. In a very small voice he says that he should like a dumpling, if there is one left, and that he will not make a speech about it.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("canteen", "The Canteen").gap(1)
                .intro(
                        "* The Department of Seeds and Weather has a canteen, and it is the only room in the whole of Heaven that smells of boiled cabbage. It is a long low whitewashed hall with benches, and on the benches clerks in their hundreds, large and small, and the hum of them is the hum of any office at noon. A heron queues for a tray.",
                        "* Mrs. Fortune takes you in by the side door. She has a table. It is by the window. It has a cloth on it with a pattern of small, worried umbrellas, and four chairs. She sits. She says that everyone has to eat, even in Heaven, and that she can recommend the pie.")
                .wait(1)
                .outro(
                        "* The clerks come up to the table in ones and twos. They stand at a respectful distance. They are not allowed to speak to the subject, and so they speak to the table, and to the pie, and to the weather. A tall thin clerk in a bow tie murmurs, to the sugar bowl, that he once filed a report on a man who fell into a pond and came out with a purse. 'Very well put,' he says to the sugar. 'Very sensible. I enjoyed it.'",
                        "* A very old clerk, who has been at the Department since the foundation, comes to the next table and sits with her back to you. She says, to the wall, in a voice that carries: 'I have been filing luck for four hundred years. I have never once met anyone it was for.' And she drops a small folded paper over her shoulder on to your pie. It says: THANK YOU FOR THE HAT.",
                        "@Mrs. Fortune| They talk about you, you know. In the corridors. There's a sort of cult. Nobody can find the form that explains why. It's the best thing that has happened to the Department in my lifetime.",
                        "* You eat the pie. It is good. It is very ordinary. It is the best pie you have had in your life, and the heron, at the next table, with its tray, looks at you with a bright knowing eye and says in a flat, polite, entirely human voice: 'Hat's off.'")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("pipwords", "Pip's Statement").gap(1)
                .intro(
                        "* Pip is the last witness. He has been waiting at the back with the others, in his hat, with a roll of paper in his fist that he has been twisting for an hour into a damp baton. When the clerk calls his name he does not hear it. He has to be nudged by Marigold. He gets up like a man in a dream.",
                        "@???| You are Pip Tallow, editor of a newsletter.",
                        "@Pip| I am, sir. Your Mastership. I mean, Master. I don't know what to call you.",
                        "@???| I am not sure either. Please go on. I've been looking forward to this.")
                .wait(1)
                .outro(
                        "* He does not use his notes. He stands at the foot of the table with the roll hanging from his hand and he tells it in the plain, rapid, stumbling voice of a person with no stage manner at all. He says that he met the Master eleven years ago, when they were boys. The Master was sitting on a wall in the rain, eating a peach. The peach was not his, and the Master did not seem to mind.",
                        "@Pip| I thought: there's someone who can't be hurt. Everything lands on him and bounces. I was so jealous I couldn't see straight. And then I thought: I'll stay close. Maybe it'll rub off.",
                        "@Pip| It didn't. It never rubs off. I wanted to say that. I've never been lucky. Not once. I've never found a purse. I get every cold that goes round. I have the worst feet in the province.",
                        "@Pip| But I've been to more places than anyone I know. I've seen a whale lift a boat. I've seen a man fall in a pond so well that two hundred people clapped. I've been at the middle of the most absurd and wonderful life I've ever heard of, and the only qualification I had was that I'd sat down next to someone on a wall.",
                        "* He looks at the paper in his hand. He looks at the Master of the Palace, at the little gavel, at the cold soup. He puts the paper on the table.",
                        "@Pip| So if it's a gap in the books, I'd like to say I think it's a very good one. And if you're going to close it, I'd ask you to leave the wall. Just the wall. There's a bit of the world that's always been there. I like to think it's the same one.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("ruling", "The Ruling").gap(1)
                .intro(
                        "* The ruling is delivered at the long table, over soup. ??? lays the gavel down very gently, beside the bowl, like a man putting down a thing he has been given to hold for somebody else.",
                        "@???| It is for you to say what happens next. I can close the gap. The books will balance. You will have luck and misfortune in the ordinary proportion, like everyone else. Or I can leave it, and things will go on as they have. Or I can do something I have never done, and I am not sure it will work.")
                .wait(1)
                .outChoice(new Chapters.Choice("ruling", "The gap in the books is yours to decide.",
                        "Close it: be ordinary, and have only what you earn.", "Leave it: stay lucky, and keep your friends.", "Open it: let the gap be a place for anyone who needs one.")
                        .after(
                                new String[]{"@???| That is a very brave thing. Most people would not give back a gift. I think you are a person who would rather owe nothing.", "* There is a feeling, like a door being shut very softly in a distant room. The next day you drop a cup, and step in a puddle, and are delighted."},
                                new String[]{"@???| It will not make things easy. But it will make them yours.", "* You walk out into the afternoon in your hat. A purse lies on the road ahead of you. You walk round it, with great determination, and then, after a pause, you pick it up and look for the owner."},
                                new String[]{"@???| I have never done that. I find I am a little afraid.", "* Something spreads from the table, like a warm light through paper. Wilbur sits down heavily on the floor. Marigold laughs out loud, once, at nothing. Pip says, in a faint, awed voice, 'Did anyone else feel that?'"}))
                .outro(
                        "?ruling=1|@Mrs. Fortune| You've the books to yourself again, dear. I'm so proud of you. I'm terribly sorry about the cups.",
                        "?ruling=2|@Mrs. Fortune| I'll retire again, then. Properly this time. You'll not get another postcard.",
                        "?ruling=3|@Mrs. Fortune| I have not the faintest idea what will happen. It is the most exciting audit of my career.",
                        "* The goose waddles to the head of the table and sits down in the Master's chair. He lets it.")
                .pay(10000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* Everyone agrees you have no talent. Everyone also agrees that you are the happiest person they know.",
                "?ruling=1|* You are, by any measure, an ordinary person now. You drop things. You miss boats, and they sink. You have never been so interesting.",
                "?ruling=2|* Things go your way, as they always did. A purse on the road. A roof that falls the day after you move. A friend at the end of a rope. You have stopped asking why.",
                "?ruling=3|* The gap is open. A good many strange and lucky things happen in the neighbourhood, and nobody can say why. A woman finds the cousin she thought she had. A dying business finds a customer. A hat finds a head.",
                "?wronghat=1|* The Society of the Fortunate Hat has two thousand members now. They still think you are a Hidden Master. You have stopped correcting them. It is easier, and there is a lovely tea service.",
                "?wronghat=2|* The Society knows you are not a Hidden Master. They have kept the hats anyway. They say the hat is the point. They may be right.",
                "?gu=1|* Master Gu visits every year with fish. He sits by the pond where he fell in and tells you about his students. They are very good at falling in ponds.",
                "?gu=2|* Master Gu has told everyone you are a Hidden Master. You have given up trying to stop him. He is in the fourth edition of the Chronicle.",
                "?hexgift=2|* Wilbur sleeps in the teapot, which has been mended, and has opinions on the garden.",
                "?!hexgift=2|* A very small man in a rusty suit comes to tea on the first Tuesday of the month, on the back of a snail. He is always late. He is never refused.",
                "* Every so often, in a quiet moment, you open the inner pocket of your purse. It is empty. It is warm again, just a little. You close it, and you put on your hat, and you go out to see who is at the door.");
    }
}
