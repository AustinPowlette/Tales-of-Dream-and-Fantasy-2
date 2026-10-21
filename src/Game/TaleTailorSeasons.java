package Game;

/** The Tailor of Seven Hems, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "Labelled in Your Hand" (a clue-fair mystery with a noir turn): three people in Brass Ford walk to the Mill Weir wearing new coats, each with the shop's label in the human's own hand, and each with a seventh hem
 *  that gave an order instead of a promise. Rolled at the start: culprit 1 Master Bolt the cloth merchant (a selvedge mark and the name "ghost silk"), 2 Dame Thrale of the Guild (a chalk mark and a thumbnail groove),
 *  3 Master Crewel, Thimble's old employer (a double-back stitch and a flat hummed tune). Choices: trust (how Kestra is defended), mercy. Flags: culprit, trust, mercy.
 *  Season 3, "A Coat for the Sky" (a siege, then a cosmic errand): Warlord Grist closes Brass Ford's gates; the human makes cloaks with arrays in the lining; afterwards the Palace asks for someone to thread a very long needle,
 *  because the Thin Sky has a seam and the seam has opened. A Snuffer of the Hollow Court comes to unpick one thread. Flags: siege, stitch, hem. */
final class TaleTailorSeasons {
    private TaleTailorSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "culprit", "trust", "mercy", "siege", "stitch", "hem");
        d.place("palace", "start").place("sky", "far").place("weir", "wild");
        d.cast("pyke", "Sergeant", "Pyke").he().keep().trait(Character.Traits.Scribe).side("Good").lvl(5).age(44).at("place:quarter").rel(1);
        d.cast("bolt", "Master", "Bolt").he().keep().trait(Character.Traits.Merchant).side("Neutral").lvl(5).age(55).at("place:quarter").rel(0);
        d.cast("crewel", "Master", "Crewel").he().keep().trait(Character.Traits.Tailor).side("Evil").lvl(6).age(51).at("place:quarter").rel(0);
        d.cast("snip", "Mister", "Snip").he().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(15).pot(8).age(56).at("far").rel(0);
        d.cast("grist", "Warlord", "Grist").he().keep().trait(Character.Traits.Leader).side("Evil").lvl(13).pot(8).age(48).at("far").rel(-3);

        // ================================================================== SEASON 2
        d.season(2, "Labelled in Your Hand");

        d.chapter("courier", "The Courier Walks to the River").years(2)
                .setup((c, st) -> { Chapters.set(st, "culprit", 1 + GameDriver.rand.nextInt(3)); Chapters.spawn(c, st, "pyke"); Chapters.spawn(c, st, "bolt"); Chapters.spawn(c, st, "crewel"); })
                .intro(
                        "* The courier is a lanky youth of nineteen called Dunstan, who carries letters for the Guild of Hems. On a Monday morning he sets out with a satchel and a new brown overcoat, a gift from an uncle, and instead of turning left to the Guild he turns right. He walks through the Lantern Quarter at a steady pace. He walks down Mill Street. He walks into the weir, in his overcoat, with his satchel on, and the current takes him.",
                        "* He is found two days later. The coat is intact. The satchel is intact. A boatman, who has seen a good many drowned men and is not easily shaken, says the lad was smiling. Inside the collar of the overcoat is a neat green label. It has a gold needle on it. It says: SEVEN HEMS.")
                .wait(12)
                .outro(
                        "* You go to the weir yourself. A rope has been stretched across the bank and a constable stands at it, stamping, with his hands in his sleeves. Inside the rope, on a trestle, the overcoat is spread to dry. You are let through because the constable says the Sergeant expects you.",
                        "* You turn the collar back. The label is green, with a gold needle. The lettering is perfect. The stitches in the border run clockwise, four to the inch, with a very slight lean to the right. It is your hand. You stand there with the collar in your fingers and a sort of cold taking hold of your wrists.",
                        "@Constable| Is it yours, then?",
                        "@You| I didn't make this coat.",
                        "@Constable| That's what I told the Sergeant you'd say.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("pyke", "Sergeant Pyke Asks Nicely").gap(8)
                .intro(
                        "* The Sergeant is a stout, tired man of forty-four in a brown uniform too small for him, with a pencil behind each ear and the expression of someone who has had a hard week and expects a harder one. He sits you down in the back room of the watch-house, on a hard chair, in front of a cup of cold tea. He asks, very nicely, if he might have a few minutes of your time.")
                .meet("pyke", "Answer Sergeant Pyke's questions at the watch-house.")
                .outro(
                        "@Pyke| I've no wish to arrest you. You'll be pleased to hear that. I've been in the watch twenty years and I know the look of a tailor who's done a murder. They get a kind of glassy cheerful look. You've the look of a person who's lost a button. But the label is yours.",
                        "@You| The label is a forgery.",
                        "@Pyke| That's what I've told the magistrate. He says the label is perfect. He says there's only one person in Brass Ford who could do that border, and that's the person who taught the border to everyone else, and she's in the country. So that leaves you.",
                        "* You tell him about the seven hems. You tell him about the promise in the seventh. He listens, with the pencil in one hand and the tea in the other, looking at the wall. When you have done he says, 'I don't suppose a promise can be a command?' and you find, with a sudden chill, that you have no idea.",
                        "@Pyke| I'd be obliged if you would look at the coat. Properly. In your own time. And not run away to the country.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("hem", "Reading a Hem").gap(8)
                .intro(
                        "* You take the overcoat home, in a bag, in the dark. You lay it on the long table. Thimble lights every lamp in the shop. Neither of you speaks. You turn the coat inside out, and unpick the hem with the finest needle you own, thread by thread, a stitch at a time, down to the seventh. It is silver. It is a hair's breadth wide. It is crammed with writing.")
                .wait(4)
                .outro(
                        "* You read it through the glass. The script is so small that a whole sentence takes up the width of a finger. It is not a wish. It is not a promise. It says, in a clear fair hand, in forty-one tiny words: Go to the river. Do not be afraid. It is warm. You will be so much happier. Go now. Go now. Go now.",
                        "* You put the glass down. Thimble has stopped humming. She is sitting very straight on her stool with her hands in her lap and her face quite white. She says, in a voice you have never heard from her, 'That's a stitch.' She says, 'That's a stitch I've seen.'",
                        "@You| Where?",
                        "@Thimble| I don't remember. I only know it. It's like a tune. I've seen it done by hand, once, a long time ago, and I couldn't sleep afterwards.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("guild", "The Guild Opens Its Books").gap(16)
                .intro(
                        "* The Guild of Hems keeps a register of every label struck in the city since the founding. It is held in a vault under the Guildhall in thirty great books chained to a table. Dame Thrale lets you in herself, with a candle and a bunch of keys. She is as dry and upright as ever, and slightly more careful.")
                .wait(8)
                .outro(
                        "@Thrale| I do not like being asked. I do not like any of this. But I like less the idea of a tailor's label used for such a purpose. Look at what you wish. You will find the Seven Hems border on page nine hundred and four. It has been unchanged for forty years.",
                        "* You find it. There are two entries, side by side: Vane's border, in her own hand, and yours, which has been added in the margin. Both match the label on the courier's coat exactly. The border has been copied perfectly, by someone who has had it in front of them for ages.",
                        "?culprit=2|* In the margin of the page, in blue chalk, is a small cross. It is a Guild mark: a check, a seal of examination. It has been made very recently. The chalk is still faintly damp. You say nothing about it, and neither does Dame Thrale, who is watching your face with a candle held absolutely steady.",
                        "?culprit=1|* On the facing page, where the cloth weights are entered, a name has been added to the list of suppliers in the last fortnight: Bolt & Sons, Wool and Silk. There is nothing strange about it. You note, all the same, that the entry is for a grade of silk called ghost silk, and that it is priced at nine times the usual.",
                        "?culprit=3|* A younger clerk, passing with a ledger, stops to say that it was a great shame about Master Crewel's shop, which was struck off the Guild register for an irregularity. He says Crewel took it very badly. He says he has been seen standing across the lane from your window with a coat over his arm.",
                        "@Thrale| I should tell you, since you will find out, that the Guild has had a number of complaints about originality. I have always been against it. I am beginning to wonder whether I was right.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("tune", "Thimble's Tune").gap(16)
                .intro(
                        "* Thimble has not slept in three days. She sits at the long table every night after the shop is shut, with a needle and a spool and a scrap of linen, practising the stitch. She says it is coming back. She says it is a kind of back-stitch, with a double turn, and that if she shuts her eyes she can almost feel the hand that taught it. She hums while she does it. She hums flat.")
                .friends(4, "Find the hand that taught the stitch: make four friends.")
                .outro(
                        "* On the fourth night she stops in the middle of a bar. She puts down the needle. She says very quietly, 'It was a man in a brown apron. He sat at the end of a long room with a lamp, and the lamp was too low, and there were eight of us sewing. He had a way of singing. He sang one tune. He sang it every day.'",
                        "@You| Which tune?",
                        "* She hums it. It is a lilting dull little tune in three flat notes, with a twist at the end. She hums it twice. Then she puts her head down on the table and weeps, without making any sound. You sit beside her. After a time she lifts her head and says: 'It's the one I got thrown out for singing.'",
                        "?culprit=3|* You know the tune. You have heard it, not an hour ago, from a man across the lane, standing in a doorway with a coat over his arm, who was whistling it under his breath and did not know that he was.",
                        "?culprit=1|* You know the tune. Everyone does. It is a tavern song, a drinking song, a hundred years old. You tell yourself that it can be nothing.",
                        "?culprit=2|* You know the tune. Everyone does. It is the Guild's marching song. You tell yourself that it can be nothing.",
                        "* Thimble goes to bed. You sit up for an hour, alone with the lamp, looking at a very small stitch in a scrap of linen.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("cloth", "Bolt and Sons").gap(24)
                .intro(
                        "* The warehouse of Bolt & Sons stands at the bottom of Mill Street, close by the weir. It is tall and brick and full of the sweet dry smell of wool. Master Bolt receives you in his counting-house, in a waistcoat of buttercup silk, behind a desk piled with samples. He is stout, jolly and bald. He offers you a bun.")
                .wait(8)
                .outro(
                        "@Bolt| Terrible business. Terrible. I've known Madam Vane since I was a lad. She bought her first bolt of grey from my father. I said to my wife, I said, this is a slander on the whole trade. A tailor to be accused? A tailor? It's like accusing a baker of making dough.",
                        "* You thank him. You ask about his silk. He talks for twenty minutes about silk, with the passion of a lover, with the fervour of a priest, with the practical interest of a man who knows precisely what each yard is worth. He mentions a new line, a grade he calls ghost silk. He says it takes dye like nothing else and holds a stitch like a vow.",
                        "?culprit=1|* He shows you a sample. It is pearl-grey and soft as breath. The selvedge has a stamp: a small brown bolt, with two lines through it. You have seen that stamp before, inside the collar of a brown overcoat. You say nothing. You buy a yard.",
                        "?culprit=2|* He shows you a sample. It is pearl-grey and soft as breath. He tells you, in confidence, that the Guild has been buying it by the roll this month, under Dame Thrale's own signature, and that he cannot think what for.",
                        "?culprit=3|* He shows you a sample. It is pearl-grey and soft as breath. He tells you a poor old tailor named Crewel came in last month and bought nine yards, paying in small coins from a purse. He says it was a pity to see.",
                        "* You walk back along the weir. The water is brown and fast. A heron stands in the shallows. You stop on the bridge and look down, for a spell, at the place where three people stepped off the bank, one after another, in new coats.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("second", "The Second Coat").gap(16)
                .intro(
                        "* The second is a magistrate's clerk, a stooped grey woman of fifty named Perpetua Swale. She sets out on a Thursday morning with her lunch in a handkerchief and her umbrella under her arm. She is wearing a plum-coloured coat that arrived as a present. She does not turn left or right. She walks straight down Mill Street, nodding to the neighbours, to the weir. The constable at the bridge sees her go. He thinks she is going to feed the ducks.")
                .wait(8)
                .outro(
                        "* This time the Sergeant is there. He stands on the bank with his hands behind his back and his hat in his fist. When the body has been brought up he does not say anything for ages. Then he turns to you and says in a voice with no expression in it at all: 'There'll be a third.'",
                        "@You| You can't know that.",
                        "@Pyke| I can. I've been in the watch twenty years. Three is the number they like. Two is a habit. Three is a plan.",
                        "* The label in the clerk's collar is perfect. The seventh hem, when you open it that night, says the same forty-one words, and one more at the end. It is a name. It is not hers. It is not yours. It is the name of the shop across the lane.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("kestra2", "Kestra Is Questioned").gap(16)
                .intro(
                        "* The name in the hem is Dray & Co., and it takes the town about a day and a half to draw the obvious conclusion. By Saturday the windows of the shop across the lane have been chalked. By Sunday a stone goes through the glass. On Monday morning two constables take Kestra to the watch-house in a closed carriage, and the queue outside the shop melts like butter.")
                .wait(4)
                .choice(new Chapters.Choice("trust", "Kestra has been taken to the watch-house on the strength of a name in a hem.",
                        "Go to the Sergeant and vouch for Kestra, at once and in public.", "Go to the Sergeant and say you are not sure, and ask him to hold Kestra while you look.", "Say nothing, and look into it quietly yourself.")
                        .after(
                                new String[]{"* You walk to the watch-house in your apron, with Thimble, and ask to speak to the Sergeant. You say, in front of the whole room, that Kestra Dray could no more kill a man than hem a handkerchief in silk. The constables stare. The Sergeant raises an eyebrow, and then, slowly, the other. 'That's a bold sort of testimony,' he says. 'I'll bear it in mind.'", "@Kestra| (afterwards, shakily, in the street) I heard that. Every word of it. I want you to know that I heard it."},
                                new String[]{"* You tell the Sergeant that you do not think Kestra did it, but that you cannot be certain. It is the truest thing you can say. He nods. He holds Kestra for two nights, in comfort, with tea. When Kestra is let out, you find that the look in {kestra.his} face has changed. It is not anger. It is a polite and terrible sort of understanding.", "@Kestra| I would rather have been told you believed in me. I should like you to know I understand why you didn't."},
                                new String[]{"* You say nothing. You go home and look at the hems. It takes you two days. On the third morning you present the Sergeant with a list of three reasons why Kestra could not have stitched them, and a drawing of a single stitch that Kestra does not use. He lets {kestra.him} go. It is done well, and quietly, and it takes you a fortnight to realise that you never actually told {kestra.him}.", "@Kestra| (stiffly) I understand that I was cleared. I am obliged to you. I should have liked to know sooner."}))
                .outro(
                        "* Kestra comes across the lane that evening with a bundle of shirts, a bottle of wine and a face like thunder. {kestra.He} sets the shirts on the counter, and the wine beside them, and says that {kestra.he} has decided to be a great deal of trouble. {kestra.He} says it is now a matter of professional pride. {kestra.He} says whoever has been using a good name for this, whether yours or {kestra.his}, is going to be sorry.",
                        "@Kestra| I think we should have a drink and be furious. I don't think it needs to be in that order.")
                .done((c, st) -> Chapters.bond(c, st, "kestra", Chapters.flag(st, "trust") == 1 ? 1 : 0))
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("orlaletter", "Madam Vane Remembers").gap(8)
                .intro(
                        "* The letter from the country comes by the evening coach, on thin blue paper, in an upright hand. It is two pages long. Orla has heard the news. She has heard it, she writes, from a milkman, a vicar, and the dog, who is a surprisingly good source. She begins with an apology, which is unlike her, and goes on to the point, which is not.")
                .wait(4)
                .outro(
                        "@Orla| (in the letter) There is such a stitch. I have seen it once, in my master's workroom in the north, forty-six years ago. It is called the Last Errand. It was used to send soldiers home from the front, with a hem that told them where the road was. It is not a promise. A promise has to be accepted. This is a hem that gives orders, and the wearer takes them as his own thoughts. My master burned the book. He said it should die with him.",
                        "@Orla| (in the letter) I know of four people who ever learned it. One is dead. One is myself, and I did not teach it. One I trained and sent away. And one was a student of my master's, a clever, quiet man, who left the Guild in disgrace and has never been seen since. I leave you to count them. I am an old woman, and I do not like the sum.",
                        "* You read it twice. You fold it, and put it in your apron, and sit for a minute with your hands in your lap. Across the lane the lights of Dray & Co. are on, and a shadow moves behind the glass. Whoever it is has put up the shutters, and then, for some reason, taken them down again.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("trap", "A Coat That Answers Back").gap(8)
                .setup(TaleTailorSeasons::mark)
                .intro(
                        "* There is one way to catch a person who forges a hem. You must give them a coat to forge. You and Thimble and Kestra sit up for three nights in the back room, with a pot of strong tea and a single lamp, making the most beautiful overcoat in the history of Brass Ford. It is cut from ghost silk. It has a green label with a gold needle. In the seventh hem, in tiny silver letters, you stitch nothing at all.")
                .made(6, "Set the trap: sew six parts of the bait coat.")
                .outro(
                        "* On the fourth night you hang the coat in the window of Seven Hems, with the lamp on, and the shutters open. You sit in the dark of the back room, with Thimble beside you holding a lantern with a cloth over it, and Kestra at the door with a kitchen knife and an air of enormous calm. Sergeant Pyke is in the lane, in a doorway, pretending to be a barrel.",
                        "* Nothing happens for two hours. The street is quiet. A cat crosses it. The church clock strikes one. At twenty past, a figure comes along the pavement in a long dark cloak. It stops at the window. It stands for minutes. Then it puts out a hand and lays it, very gently, on the glass, and you see that there is a coat over the arm: a brown one, new, with a green label.",
                        "* Thimble's hand tightens on yours. She whispers a single word: 'Tune.' And in the dark of the lane, so faint that you might be imagining it, a man begins to whistle three flat notes, with a twist at the end.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("last", "The Seventh Hem").gap(8)
                .intro(
                        "* The figure at the window turns. It is not who you expected. It is exactly who you expected. The lantern comes off, and the cloth, and the whole shop is full of light, and the Sergeant steps out of the barrel with a pair of handcuffs and a look of weary vindication. The person under the cloak puts down the coat on the pavement. They do not run. They look at you, and at Thimble, and at the window, and smile.")
                .wait(4)
                .outro(
                        "?culprit=1|@Bolt| You were quicker than I thought. I did try to be considerate. I only wanted the Seven Hems label and the Guild's silk contract, you understand. It was the quiet way. The Last Errand does it so gently. They smile all the way.",
                        "?culprit=1|* He is smiling too, the jolly smile of a man at a counter. 'They were nobody, you know,' he says. 'A courier. A clerk. They were only ever going to be a footnote.' Thimble is across the shop before you can stop her. She does not hit him. She takes the coat from the pavement, folds it neatly, and says in a high clear voice: 'They were people.'",
                        "?culprit=2|@Thrale| I wished to prove a point. The Guild has lost its way. It has let in too many experiments. I wished to show that a tailor's hem is a serious matter, a matter of life and death, which cannot be left to children and shopkeepers. I did not wish anyone to die. I wished only to be listened to.",
                        "?culprit=2|* Dame Thrale takes off her hood. She is perfectly composed. She holds out her wrists for the cuffs. 'I have run a very orderly Guild for thirty years,' she says. 'It is possible that I was wrong about what order is for.' It is the first time you have seen her look old.",
                        "?culprit=3|@Crewel| I taught her to sing, you know. I taught Thimble. I taught all eight of them. I was a good master. Orla Vane sent me away because I knew the Last Errand and she did not. She called it a stain on the craft. I have made coats for forty years, and they have never once sold.",
                        "?culprit=3|* The man in the brown apron lowers his head. 'I only wanted her shop to be what mine was,' he says. 'Empty.' Thimble, in the doorway, has her hands over her mouth. She does not speak. She walks across the floor, and puts one hand on his sleeve, and he flinches as if she had struck him.",
                        "* The Sergeant takes the cloak, and the coat, and the culprit, and a copy of the register. He does not hurry. In the doorway he turns and tips his hat to you. 'It was well done,' he says. 'I'd not have found it.'")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("mercy", "What Was Promised").gap(16)
                .intro(
                        "* The trial is short, and very quiet. The magistrate is a plump grey man who has never in his life been out of breath. He asks you what the Last Errand is. He asks what a hem can do. He asks whether the prisoner can be said to have intended the deaths, when the coats gave the orders. You find that you are not sure of any answer, and you give them all.")
                .wait(4)
                .choice(new Chapters.Choice("mercy", "The court asks the tailor who understood the stitch what ought to be done.",
                        "Ask for the full penalty: three people died.", "Ask that the prisoner be set to unpick every Last Errand ever sewn.", "Ask that the prisoner be let go, with a plain coat, and no hem.")
                        .after(
                                new String[]{"* You speak for the penalty. You speak plainly, without heat, in the dry level voice of a person who has measured something. The magistrate nods. When it is over you walk out into the afternoon sun and stand in the street with your hands at your sides. Nothing has come out even. It never does.", "@Pyke| It's the right thing, I think. I'd not like to say it feels it."},
                                new String[]{"* You ask that the prisoner be set to the work of unpicking. It takes a year. There are nine hundred and forty coats in the city with a Last Errand in the seventh hem, and every one has to be found and opened and taken down with a needle as fine as a hair. You help. So does Thimble. So does Kestra. At the end you find that you have learned the stitch, and burned the book.", "@Thimble| I think it's a very good punishment. It's the sort of thing that makes you wonder who the punishment is for."},
                                new String[]{"* You ask for a plain coat and no hem. The magistrate, who has never in his life been asked for such a thing, takes a long time to understand. at last he nods. The prisoner goes out into the rain in a grey overcoat with a plain seam, and nothing in the seventh, and stands on the steps for a spell. It is a gift and a sentence, and you will never know which of the two it was.", "@Kestra| That was either very kind or very cruel. I'm fairly sure it was both."}))
                .outro(
                        "?trust=1|* Kestra waits for you on the steps, in the sun, with a tape measure round {kestra.his} neck and a pin in {kestra.his} mouth. {kestra.He} takes the pin out. {kestra.He} says, 'You said it in front of everyone. I haven't thanked you.' {kestra.He} does so, rather awkwardly, with {kestra.his} whole face.",
                        "?trust=2|* Kestra waits for you on the steps, in the sun, with a tape measure round {kestra.his} neck. {kestra.He} says, 'I think we are going to have to learn to trust each other in advance. I think it's the only way it works.' It is the most practical declaration you have ever heard, and you take it at once.",
                        "?trust=3|* Kestra waits for you on the steps, in the sun. {kestra.He} says, 'You did all that and never told me.' {kestra.He} is shaking. {kestra.He} says, 'You must never do that again.' You say that you will not. {kestra.He} says, 'Good,' and takes your arm, and holds it, tightly, all the way home.",
                        "* The shop across the lane reopens on Monday. The glass has been mended. The sign says DRAY & VANE, and under it, smaller, in Orla's own upright hand, by post: A tailor with two hands is a tailor with two promises.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "The Last Errand",
                "* The Last Errand is no longer sewn. Nine hundred and forty coats in Brass Ford have been taken down, and their hems are plain. The border of the Seven Hems label has been changed by one stitch, on the Guild's advice, so that a forger can be told at once.",
                "?culprit=1|* Master Bolt's warehouse is closed. The silk sells at auction. Ghost silk has gone out of fashion, which is a kindness.",
                "?culprit=2|* Dame Thrale resigned from the Guild in the spring. She keeps a small shop of her own, in a back street, and makes ordinary coats. She is said to be quite good.",
                "?culprit=3|* Master Crewel works in the shop at the end of Mill Street, in a brown apron, and sings. He is not good. Thimble visits him on Sundays with a basket.",
                "* Thimble has a room of her own over the shop now, with a window. She keeps it full of curtains. She says they are hems with a view.",
                "* Far to the north, in the country, a very old woman puts down her needle at the end of a day. She looks out of the window at the river. She says to the dog, quite quietly, 'It was my fault, you know'. The dog, who does not agree, puts its head on her knee.");

        // ================================================================== SEASON 3
        d.season(3, "A Coat for the Sky");



        d.chapter("gates", "Brass Ford Closes Its Gates").years(2)
                .setup((c, st) -> Chapters.spawn(c, st, "grist"))
                .intro(
                        "* The Ninth Banner comes over the hills on a bright cold morning in March, ten thousand strong, under a flag of a black wheel on a white field. Its commander is Warlord Grist, who was once a quartermaster and is now a good deal more. He has a reputation for paying his soldiers on time. It is, the Guild says, the most frightening thing about him.",
                        "* The city council meets in the Guildhall for four hours and decides to shut the gates. They are shut at dusk with a sound like a great sigh. The next morning, from the top of the Lantern Quarter, you can see the camp: ten thousand small fires, in rows, as neat as a ledger.")
                .wait(8)
                .outro(
                        "* The shop is full of people by noon. Soldiers of the City Guard, with torn sleeves. Merchants who want banners. Women who want warm things for their sons on the wall. A lamp-maker who has brought a tray of tea, since he cannot think what else to do. Thimble has arranged the customers in a queue by the colour of their anxiety, and is moving among them with a tape measure and a calming word.",
                        "@Kestra| Right. I've closed the shop across the road and put the dummies in the cellar. I've sixty yards of grey wool, forty of red flannel and a roll of oilcloth. Where do you want me?",
                        "@You| Cutting table.",
                        "@Kestra| Brilliant. I've always wanted to be useful in a catastrophe.",
                        "* It is the first time since the courier that the two shops have worked as one. It is a strange, quick, frightening pleasure. The lamps burn late. At midnight Orla's old dog, who has come in the post-cart from the country with a note round its neck, lies down on the cutting-room floor and goes to sleep.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("cloaks", "Cloaks by the Hundred").gap(8)
                .setup(TaleTailorSeasons::mark)
                .intro(
                        "* The Captain of the Guard comes to the shop on the fourth day, with a torn ear and a bandaged arm. He says the wall is thin on the north side. He says the men are cold, and the wind off the camp is bitter, and half of them have no cloaks. He says the Master of the Hall has authorised the purchase of two hundred, and he would like to know whether the shop can supply them by Friday.")
                .made(40, "Clothe the wall: sew forty cloaks.")
                .outro(
                        "* You set the cutting table in the street. You set the pressing table beside it. You borrow the lamp-maker's cart, and the net-mender's boys. The whole of the neighbourhood's sewing circle, sixty old women in black who have been stitching shrouds for forty years and are delighted at last to make something warm. They sit in rows in the lane. They sing. They sing flat.",
                        "* On the third night, in the light of a hundred lanterns, you realise what you are doing. You are not stitching a cloak. You are stitching a cloak with something in the lining: a small array, a pattern of silver thread no larger than a thumbnail, sewn into the shoulder where the wearer will not feel it. It is a thing you have never been taught. Your hands have simply known how.",
                        "@Thimble| That's a seventh hem.",
                        "@You| It's a lining.",
                        "@Thimble| It's a hem that's moved.",
                        "* The first cloaks go up to the wall at dawn on Friday. The soldiers put them on. A big sergeant with a squint says, to nobody, that it's the warmest thing he has ever had on his back. Then he says, a minute later, in a voice of surprise, 'I'm not afraid.' He looks at his hands. He says: 'I'm not afraid at all.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("wall", "Kestra on the Wall").gap(16)
                .intro(
                        "* The assault comes on the ninth night. It is a grey bitter night with a thin cold rain, and the camp is alive with torches. Warlord Grist's men come up the slope in long files with ladders and hooks and a great rolling tower of oak. The City Guard stands to. You are on the wall at the north gate with a basket of thread and a lantern, because someone has to mend the cloaks, and because Kestra has refused to leave your side.")
                .wait(8)
                .outro(
                        "* It lasts until dawn. You stitch, and run, and stitch. A cloak torn at the shoulder, a sleeve slashed, a hood on fire. Kestra is beside you with a needle between her teeth, and a pair of shears, and a face as bright as a coin, hemming soldiers where they stand. At two in the morning a ladder strikes the wall six feet away and a man in a black wheel surcoat swings a leg over the parapet and sees the two of you, kneeling in the rain, repairing a boot.",
                        "* He stops. He looks very puzzled. Kestra looks up with a mouthful of pins and says, quite calmly, 'Do you mind? We're in the middle of something.' The man opens his mouth. Then a sergeant of the Guard cuts him down with a billhook, and the moment is gone, and you are both laughing in a hysterical, helpless way, and you have to stop to thread the needle again.",
                        "@Kestra| I think I've been waiting for a crisis to say something. I think I'd rather say it in a minute, when we're not being shot at.",
                        "@You| That seems wise.",
                        "@Kestra| I'm often wise. It's one of my faults.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("flag", "The White Flag").gap(8)
                .intro(
                        "* On the twelfth day Warlord Grist sends a herald under a white flag with a message. The city has food for ten more days. It has water for twenty. It has an army of ten thousand at its gates, and no hope of relief. He proposes terms: the gates are to be opened, the city is to be spared, and every tailor, weaver and dyer within its walls is to be conscripted to the Ninth Banner as an uniform-maker, on pay.")
                .wait(4)
                .choice(new Chapters.Choice("siege", "The Council has asked the Lantern Quarter, as the city's tailors, for its answer.",
                        "Accept the terms: open the gates, save the city, and go to the Banner.", "Refuse, and sew the garrison a set of cloaks no sword can cut.", "Forge the Warlord's own banner, and send a false message in his name.")
                        .after(
                                new String[]{"* You say yes. It is the hardest word you have ever said in a council. The gates open on the morning of the fourteenth day. Grist rides in on a grey horse, a short square tired man with a ledger under his arm, and the first thing he does is tour the granaries. The city is spared. You go with the Banner for a season, as a tailor with ten thousand soldiers to clothe, and learn to hate and respect him in about equal measure.", "@Grist| You have a good needle. I'd rather not waste it on a wall."},
                                new String[]{"* You say no. You say it in the name of the Quarter, with Kestra and Thimble standing behind you, and nine hundred tailors, weavers and dyers in the Guildhall, on their feet. The Council sends the answer. That night you cut the last of the oilcloth into a hundred and twenty cloaks, each with a small silver array in the hem. The wall holds. It holds for six more days. On the seventh, the Banner breaks camp, and goes.", "@Captain| I don't know what you put in those. I'd like to know. I'm also not sure I do."},
                                new String[]{"* You forge a banner. It takes two nights. It is a white wheel on black, an exact reverse of the real one, with Grist's own border. Kestra writes the letter in his hand, which she learned from a captured quartermaster's note, and a boy carries it over the wall on a pig. It says that the city has surrendered to an advance party and the siege is lifted. The Banner stops, and argues, and splits. It is the dirtiest thing you have ever done with a needle.", "@Kestra| I wanted it noted that it was my idea. I also want it noted that I'm very sorry. It worked rather well, though."}))
                .outro(
                        "* The siege ends the way many things end, by being too expensive. The Banner leaves a trail of cold fires on the hillside. In the city the bells ring for an hour, and the lamp-maker sits down in the road and weeps into a teapot. Brass Ford has been spared, not by a victory, but by about two hundred and sixty thousand stitches, put in by sixty old women in black who have just discovered that they can sing.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("after", "After the Siege").gap(32)
                .intro(
                        "* The Lantern Quarter is a mess. Half the windows are boarded, the street is trodden to a grey porridge, and in front of the shop the pressing-table has frozen to the paving. For a week nobody does anything but sleep. Then the neighbours begin to emerge, one by one, in odd garments, blinking, to find out what remains.")
                .wait(8)
                .outro(
                        "* You open the shop on the Monday. The first customer is the big sergeant with the squint. He has brought his cloak. He wants to know what it is. He says that for a fortnight he has been unable to feel anything but a sort of calm, and that his wife says he is insufferable, and he would like it taken out.",
                        "* You hold the cloak. You turn up the hem, and the seventh, and the eighth that you did not know was there, and you look at it. The array is a small neat thing in silver thread, a ring with nine points. It has been worn thin. It is a pattern you have never seen in your life. It is, you realise slowly, a pattern that you could not have made without help.",
                        "@Kestra| I think we'd better ask Orla.",
                        "@You| She's asleep.",
                        "@Kestra| She sent a note. Look.",
                        "* On the back of the oilcloth label, in Orla's upright hand, in pencil, someone has written: It is time you were asked. Go and see the Master of the Palace. He has been sending for you for years.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("note", "A Note on Brown Paper").gap(16)
                .intro(
                        "* The note comes by a plain brown cart, with a Wick of the Outer Court on the box, in a thin coat. It is a single sheet of brown paper, written in a round careful unpractised hand. It says: There is a tear in the sky. We need a person who can thread a very long needle. We think you might know a stitch. Please come. There is a kitchen.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and ask for the Under-Kitchen.")
                .outro(
                        "* The Under-Kitchen is warm. A man in a plain apron stands by the range and stirs a pot with an expression of the deepest concentration. He looks up when you come in. He says he is called ???, and that he is sorry about it, and that he has been told to ask whether you would like some soup.",
                        "@???| I can tell you a few things. I am allowed to. The sky above the Garden is not a roof. It is a coat, and it has a seam. In the Thin Sky, in the far country, the seam has begun to open. The weather comes in through it. The Hollow Court leans on it. It is a very old seam, and a very patient one, and somebody has to mend it.",
                        "@You| Why me?",
                        "@???| The last person who did it was a tailor. A long time ago. I do not remember her, but I remember a needle. It is in a drawer in the room next door. It has been waiting. It is about as long as a bridge.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("needle", "The Wardrobe Under the Palace").gap(8)
                .intro(
                        "* The room next door is the Wardrobe. It is a long vaulted gallery under the Palace, lined with cedar, lit by a single lamp. Along the walls hang garments of every age and colour, hundreds of them, in rows, each in a muslin bag. A cold pale light seeps from the end of the room. The Wick who showed you in has stopped at the door and gone quite white.")
                .wait(4)
                .outro(
                        "* The needle lies in a drawer in a long box of black velvet. It is silver, and nine feet long. The eye is as wide as a hand. The thread is a pale blue light, coiled in a bowl, and it hums when you touch it. Kestra, behind you, makes a small noise in her throat. Thimble says nothing. She takes your elbow.",
                        "@???| I am allowed to say what the needle is. It was left here by the one who made the Garden. It is not for fighting. It is for keeping. He gave it to a woman who made his clothes, and she mended his sky whenever it frayed, and when she died nobody could pick it up.",
                        "* You close your hand round it. It is weightless. It is warm, and alive, like a bird. You feel it turning slightly in your fingers, seeking a direction, as a compass does. It points up. It points at a place very far away, in a cold country, on the highest ground in the world.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("thread", "Thread").gap(16)
                .setup(TaleTailorSeasons::mark)
                .intro(
                        "* The needle wants thread, and the Palace's blue light is not enough. Mending a sky requires seven kinds, one for each hem, and each must be made by hand. You take the bowl of blue light to the long table in the Wardrobe and spend a week making the other six. Dawn thread, spun from the first light of a winter morning. Salt thread, drawn from the sea. Iron thread, twisted from the filings of a forge. Rain, glass, hunger, and the seventh, which you make last.")
                .made(14, "Spin the sky-thread: make fourteen skeins.")
                .outro(
                        "* The seventh thread is the hardest. It is not spun from anything you can name. You work at it for three nights, and on the third you give up, and put down your tools, and look at your hands. They are shaking. They are very tired. You have made, in your life, four thousand and some coats, and in every one you have put something into the seventh hem, and you have never once asked yourself what it was.",
                        "* You take the silver thread that Orla sent you, the very small last scrap of it. You wind it round the card. You look at it. You put it, finally, in the bowl. It spreads, and glows, and settles, and in a moment the seventh thread is a pale clear silver, as bright as a tear. It is the most beautiful thing you have ever made, and you did not make it.",
                        "@Thimble| That's the promise, isn't it? That's what's in the hem. It's every promise you've ever kept.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("snip", "Mister Snip").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "snip"))
                .intro(
                        "* He is waiting for you at the foot of the Palace stair, on the morning you set out, in a tidy brown suit with a great many pockets. He is a small bald man with a neat moustache, a pair of scissors on a silver chain, and an expression of perfect mild reasonableness. He lifts his hat.",
                        "@Snip| Mister Snip. I represent a party. I should like a word.")
                .meet("snip", "Hear what Mister Snip has to say.")
                .outro(
                        "* He walks beside you for a mile, without hurry, talking. He says he is a Snuffer, a Hand of the Hollow Court. He says it is a perfectly respectable trade. He says that the Court has no objection to your needle, or your thread, or your quest. It has a single small request. It would like one thread, that is all, a single filament of the seventh, to be left out of the mending.",
                        "@Snip| It is such a little thing. A hair. The seam will close. The weather will be quite shut out. But one strand of the seventh, one promise, will be left loose, and the Court will be able to pull it. Gently. In its own time. A single stitch undone is an unmaking so very gradual that nobody could feel it.",
                        "@You| And if I say no?",
                        "@Snip| Then I shall walk with you the rest of the way and ask again. I am very patient. I take one thing, you see. I only ever take one thing. Do think about it.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("sky", "The Thin Sky").gap(24)
                .intro(
                        "* The Thin Sky is a mountain peak in the far country under a cracked blue dome. The air is clear and cold and tastes of iron. The crack is a long thin line across the heavens, from one horizon to another, and through it comes a dim far light, and a wind that smells of nothing you can name. Kestra and Thimble climb the last of the ledge behind you. Mister Snip walks beside them with his hands behind his back.")
                .at("sky", "Climb to the Thin Sky, {@sky}, with the needle.")
                .outro(
                        "* You thread the needle at the summit with all seven threads, one after the other, in order. It takes an hour. You stand on the very top of the world with the silver needle lifted above your head. It is nine feet long and weightless. You reach up. You put it into the sky.",
                        "* It goes in like a pin into silk. The crack parts, and closes, and parts again. The wind drops. The thread hums, a huge slow note, like a bell heard from underwater. You draw it through. You turn the needle. You sew the sky, stitch after stitch, with long strokes of your arm, and wherever the thread goes, the dome is whole.",
                        "* The seventh thread is last. It is the brightest of all. It hangs in the air before you, pulsing, and you see, quite clearly, that there is one filament in it that is slightly different from the rest. It is thin as a hair. It is yours. Snip is standing at your elbow, with the silver scissors in his hand, and an expression of kindly inquiry.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("stitch", "The Last Stitch").gap(8)
                .intro(
                        "* There is a moment, with the needle in your hand and the sky half mended and Snip's scissors poised, when you cannot think of anything at all. You can hear the wind, and Thimble's breath, and your own pulse. You think of the seventh hem, and Orla, and the promise that every coat carries and nobody remembers. You think of a door that opens from the inside.")
                .wait(4)
                .choice(new Chapters.Choice("stitch", "The seam is almost closed. The Snuffer is waiting for his thread.",
                        "Close it completely, and keep every thread.", "Leave the thread for Snip: a little loosening is the price of a sky that holds.", "Close it, but put a buttonhole in the seam: a door that opens only from the inside.")
                        .after(
                                new String[]{"* You close it. You pull the last stitch tight and tie it off with the old knot Orla taught you on the first day. The sky is whole. Snip sighs, and puts the scissors away, and bows. He says he quite understands. He says there will be another opportunity. He walks down the mountain in a pleasant grey dusk, whistling, and you do not see him go.", "@Thimble| That's the best sky I've ever seen."},
                                new String[]{"* You pull the strand out, and give it to him. It is the smallest thing in the world. He holds it between finger and thumb, and bows, and it is gone. The sky holds. You are not sure it will hold for ever. You are not sure what has been taken. You are sure that a single hair will, in time, be pulled.", "@Snip| I am extremely grateful. I shall be very careful with it. It is such a delicate work."},
                                new String[]{"* You close the seam, and, in the very last stitch, you put in a buttonhole: a small neat slit with a bound edge, in the seventh thread, which opens only from the inside. It is the best work you have ever done. Snip stares at it. He says it is not regular. He says it is a door. He says the Court will not be at all pleased, and that he finds he is.", "@Snip| I think I shall write a report. It will be a very long one."}))
                .outro(
                        "* The needle comes out of the sky. The last of the light fades. The wind is a wind. The dome is the colour of an ordinary evening, and a single star has come out over the shoulder of the world. Nobody says anything. Kestra takes your hand. Thimble, on a rock, begins to hum, and for the first time in her life, hits a note.",
                        "* You carry the needle down the mountain on your shoulder. It is warm. It is humming a faint, happy, tangled tune. When you reach the foot of the peak it has shrunk to the length of your forearm. When you reach the Palace it is the size of an ordinary darning needle, and when you put it in the drawer it falls asleep.")
                .pay(1000, 0, 0, 2, 1, 2);

        d.chapter("seven", "Seven Hems").gap(24)
                .intro(
                        "* The shop is as you left it. The grey dummy stands in the window. The sign swings in the wind. The bell over the door goes as you push it, and Orla's old dog wakes and lifts its head. A coat is spread on the long table, pinned and chalked, with a note: Wanted for Friday. It is a perfectly ordinary job. You pick up the scissors.")
                .wait(8)
                .choice(new Chapters.Choice("hem", "Seven Hems is yours, and the seventh hem of every coat is yours to fill.",
                        "Keep it as it is: a tailor's shop, with a promise in every hem.", "Open the Wardrobe to the city: teach the Quarter to stitch promises.", "Take the needle's drawer at the Palace as your workshop, and mend what needs mending.")
                        .after(
                                new String[]{"* You keep the shop. The sign is repainted every spring, and the grey dummy in the window gets a new coat every autumn. The queue on Monday is the longest in Brass Ford. You fill the seventh hem of every coat with something small and true, and you never tell anyone what it is. People say the coats are lucky.", "@Orla| (by post, in pencil) Good."},
                                new String[]{"* You open the Wardrobe to the Quarter. In ten years you have four hundred students and a school in the old lamp-maker's yard. They learn the stitch, and the code, and the one rule: nothing goes in the seventh hem that you would not say aloud. The Guild of Hems makes you its President. You accept the post on condition that the meetings are short.", "@Kestra| I think we'd better start a second school. I think the first will be too good."},
                                new String[]{"* You take the drawer. It is a quiet, cedar-scented workroom under the Palace, with a lamp and a stove and a long table, and a window that looks on nothing at all. You mend what comes: a banner, a cloak, a flag torn in a battle, a sky. Kestra visits on Saturdays. The soup is excellent. A man in an apron watches you work, from a stool, and learns to thread a needle.", "@???| I find that I like it. I did not know it could be so quiet."}))
                .outro(
                        "* You sit that evening at the long table, in the lamp's yellow circle, with the dog on your feet and a coat across your knees. You turn up the hem. You turn up the seventh, a hair's breadth wide, and take a length of silver thread, and think, for a long moment, of what to put in it. You know it will be true. You know it will come true, in its own time, and the coat will be worn out. Then you begin.")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* Seven Hems has a gold needle on a green sign, and a grey dummy in the window, and a dog on the doorstep that nobody remembers acquiring.",
                "?hem=1|* The queue on Monday goes round the corner. The coats are lucky. Nobody knows why, and everybody has stopped asking.",
                "?hem=2|* The school in the lamp-maker's yard turns out two hundred tailors a year. Each one learns the stitch for the seventh hem, and the rule that goes with it.",
                "?hem=3|* In the cedar room under the Palace a lamp burns late. A man in a plain apron has learned to thread a needle with one hand.",
                "?stitch=1|* The sky above Brass Ford is whole. Children say it is the best-kept sky in the country. They do not know why they say it.",
                "?stitch=2|* One thread in the seventh hem is loose. You can feel it sometimes, on the highest ground, on a still evening, like a hair across the face. You do not mind. It is gentle work.",
                "?stitch=3|* Somewhere above the highest mountain there is a small neat buttonhole in the sky. It opens only from the inside. Once a year, in the spring, a single feather comes through it, from nowhere, and lands on the sill of the shop.",
                "?siege=1|* A short square man visits in the autumn with a ledger. He is still a very careful tailor's client. He pays on time.",
                "?siege=2|* The north wall of Brass Ford has a row of hooks along it, on which hang a hundred and twenty cloaks, in oilcloth, each with a small ring of silver in the hem. Nobody wears them. Nobody takes them down.",
                "?siege=3|* The black wheel flag still hangs in the Guildhall, reversed, in a glass case. A small card beneath it says: Not authorised.",
                "?love=1|* Kestra keeps the books of the shop across the lane, and sleeps on the right-hand side.",
                "?love=2|* Kestra has a key to both doors.",
                "?love=3|* Kestra and you share a cutting table, a kettle and a joke which nobody else has been told. It has taken a great deal of thread.",
                "* The last coat you made this year had a plain grey outside and a plain grey lining. In the seventh hem, in silver, in very small letters, you stitched: Come back. It is the only promise you have ever made that was not meant for the wearer.");

        // @@END
    }
}
