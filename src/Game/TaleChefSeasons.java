package Game;

/** The Cook Who Fed the Mountain, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Truce Dinner" (a court whodunit at a table, card D with comic relief): the Saltglass Pavilion and Ironwood Hall have fought over the ford for nine years. Both ask for one neutral table,
 *  seven courses, forty guests, and the human cooks it. Somebody means to poison the dinner. Rolled at the start: culprit 1 the Matron's taster Dill Harrow (a taster who tastes with the wrong hand), 2 the Elder's nephew
 *  Pascoe Bray (a list that forgot his uncle's one taboo), 3 Auberon Pell of the Jade Table (a garnish that is harmless cold). Every culprit leaves two fair clues. Choices: pellhelp, trap, justice.
 *  Season 3, "The Hungry Year" (cosmic and plain): food begins to taste of nothing, the Beast Tide comes to the crossing, the Palace's Under-Kitchen sends a cart, ??? learns to cook, the dead are given a meal,
 *  Rue comes home, a Captain of the Hollow Court asks for dinner, and the human cooks for the line at the Garrison. Flags: tide, captain, table. */
final class TaleChefSeasons {
    private TaleChefSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "culprit", "pellhelp", "trap", "justice", "tide", "captain", "table");
        d.place("palace", "start").place("garrison", "far");
        d.cast("bray", "Quillon", "Bray").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(8).pot(6).age(63).at("far").rel(0);
        d.cast("teak", "Teak", "Orme").she().keep().trait(Character.Traits.Leader).side("Neutral").lvl(8).pot(6).age(58).at("far").rel(0);
        d.cast("dill", "Dill", "Harrow").he().keep().trait(Character.Traits.Chef).side("Neutral").lvl(3).age(41).at("far").rel(0);
        d.cast("pascoe", "Pascoe", "Bray").he().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(3).age(29).at("far").rel(0);
        d.cast("auberon", "Auberon", "Pell").he().keep().trait(Character.Traits.Merchant).side("Neutral").lvl(3).age(26).at("far").rel(0);
        d.cast("verge", "Captain", "Verge").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(12).pot(8).age(45).at("far").rel(0);

        // ================================================================== SEASON 2
        d.season(2, "The Truce Dinner");

        d.chapter("invite", "Seven Courses for Forty").years(2)
                .setup((c, st) -> { Chapters.set(st, "culprit", 1 + GameDriver.rand.nextInt(3)); Chapters.spawn(c, st, "bray"); Chapters.spawn(c, st, "teak"); Chapters.spawn(c, st, "dill"); Chapters.spawn(c, st, "pascoe"); Chapters.spawn(c, st, "auberon"); })
                .intro(
                        "* Two letters arrive by the same carrier on the same morning, with the same request, in handwriting that could hardly be more different. One is in a bold black copperplate and comes from the Saltglass Pavilion. The other is in a small stiff script and comes from Ironwood Hall. Each says that the writer has been at war with the other for nine years over a ford that is four feet deep.",
                        "* Each says that the writer would now be glad to make a peace, on neutral ground, over a dinner, and that the only neutral ground in the province is the Long Table. Each says, in nearly the same words, that the writer would not for the world embarrass a cook. You read them twice. It is the nicest threat you have had.")
                .wait(12)
                .outro(
                        "* Cassia reads both letters over your shoulder at the board, eating a bun. {cassia.He} has been promoted to First Class, and made no mention of it, and has been seen to wear the badge inside {cassia.his} coat.",
                        "@Cassia| The Hall would like me to say that it takes no position. Privately I would like to say that these two people have not been in a room together for nine years, and the last time they were, someone lost an ear. The dinner is in six weeks. I shall be the arbiter. Forty guests. Seven courses. No steel in the hall. Nothing in the sects' colours.",
                        "@Rue| (in a letter, three weeks later, from the coast) Seven courses is a promise. Count the bowls first. A war is nine years of people sitting very straight in chairs. The way to end it is to make them lean over.",
                        "* You find a clean page at the front of the cookbook and write, in pencil: A TABLE FOR FORTY. Under it, because it seems to be necessary: Nobody leaves hungry. Nobody leaves dead.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("seating", "A Table for Forty").gap(16)
                .setup(TaleChefSeasons::mark)
                .intro(
                        "* The seating plan takes a week. It is harder than the cooking and less honest. The Elder cannot sit beside the Matron, or opposite her, or within earshot of her cough. The Matron cannot sit with her back to a window, and the Elder cannot sit with his back to a door. Each has a champion, a cousin, a steward and a taster who must all be in the line of sight. Pell, who is not invited, writes with advice.",
                        "* You make a rehearsal dinner for twelve, to find the problems before the sects do. Ox is the Elder. Brannock the sergeant, who owns a very good scowl, is the Matron. They are not good at it, and they quarrel over the bread.")
                .made(12, "Rehearse the dinner: cook twelve dishes.")
                .outro(
                        "* By the twelfth dish you have found six problems. The long table wobbles when forty people lean. The stove is on the wrong side for the second course. The wine will go off in a day in that heat. The soup bowls hold a third less than the plan says. The chimney smokes in an east wind. The kitchen is a hundred feet from the nearest well, which, as Cassia observes, matters when somebody has to put out a fire or wash a bowl.",
                        "@Cassia| I shall note them as issues. I would prefer to call them conditions.",
                        "* You fix the wobble with a folded menu, which Ox says will not hold. It does, and for the rest of the book the menu holds up the table, and it is a great joke in the crossing that a treaty is balanced on a piece of paper.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("tasting", "The Tasters").gap(24)
                .intro(
                        "* The custom of the table is that each sect sends a taster, to sample every dish before it is served. They come on the Sunday. They are, by their trade, men and women who trust nothing, who have the lean watchful look of carriers of other people's fears. They are shown the kitchen, and they look into every pot, and they say nothing at all.",
                        "* Dill Harrow is the Matron's taster: a thin worn man of forty with a clean apron and a moustache like an apology. He sniffs every ladle. He holds a spoon in his left hand, which is the hand tasters use for the Matron, and he tastes with his right.")
                .meet("dill", "Walk Dill Harrow, the Matron's taster, through the kitchen.")
                .outro(
                        "* He tastes each pot in turn and writes on a card. When he reaches the soup he tastes it twice, and makes a small sound in his throat. You ask if something is wrong. He says no. He says it is a wonderful soup. He says that it is a little dull, for a wonderful soup, and that you might consider something with more salt.",
                        "?culprit=1|* It is a curious thing to say. The soup is correctly salted. Rue would have thrown her ladle at him. You note, as you let him out the back, that he asks a good many questions about which dishes will be carried by whom, and which of the footmen will have the left side of the table.",
                        "?culprit=2|* The nephew, Pascoe Bray, has come with the Elder's taster, to be shown the kitchen. He is a big cheerful man with an excellent coat and his uncle's jaw. He admires the stove at some length. He asks where the wine will be stored, and whether it will be sealed, and who will have the key.",
                        "?culprit=3|* Auberon Pell has come with a message from his father, which is an offer of green-aproned footmen and a set of the Jade Table's best porcelain. He is polite. He looks at the plates on the shelf with the interest of a man doing sums. 'Hand-glazed,' he says, as though it were a sin. 'In this country they glaze with lead.'",
                        "@Cassia| One does notice the dull ones. There's nobody more dangerous than a person who has just had a good idea.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("allergy", "What the Guests Cannot Eat").gap(16)
                .intro(
                        "* The lists arrive in the post, each in an envelope with the wax of its owner. The Elder's cannot take quince, which makes him sneeze like a cannon. The Matron cannot take lamb. The Elder's champion is a vegetarian by vow. The Matron's cousin eats nothing that has lived within a mile of a river. Half the guests do not eat anything yellow.",
                        "* You pin them to the wall in the kitchen and stand back. It looks like a battle plan. It is, Ox says, a menu with a grudge.")
                .wait(4)
                .outro(
                        "?culprit=2|* There is a discrepancy. The Elder's list, in his nephew's writing, lists every dish he cannot eat, in careful order. It says nothing about nutmeg. You remember a story, from the old stove-hand at the Cobalt Hall, that the Elder Bray had once been very ill from nutmeg and would never touch it. You check the old letters. He has written about it twice.",
                        "?culprit=1|* Dill's card for the Matron is thorough. He has written that she cannot take lamb, which is true. He has also written that she cannot take salt, which is not. You notice, in a pencil note by the card, in a hand that has pressed hard, a question mark.",
                        "?culprit=3|* A courier from the Jade Table brings samples of the porcelain, for the dinner, in a padded crate, free of charge. Each plate has a faint green rim. They are very beautiful. Ox, who handles everything as though it might be alive, picks one up and says that it is heavier than it ought to be.",
                        "* You write the dishes out again, one by one, so that nothing the Elder cannot eat sits beside anything the Matron cannot touch. It takes until three in the morning. When you have done, the menu is a work of art, and it contains no dish with more than two ingredients in common with another. Cassia reads it and says it is the most beautiful thing the Hall has ever received, and that it is probably illegal.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("wine", "The Wine Merchant").gap(16)
                .intro(
                        "* The wine has been ordered from the south, a cask of eleven-year red for the first toast and a case of white for the fish. It is delivered on a Tuesday by a cheerful man in a wide hat who will not be hurried and does not wait for a receipt.",
                        "* You open the case that evening. The corks are fine. The labels are fine. The wine is a very good, pale, honest white. Cassia smells it and says that it is perfectly sound, and then says nothing for a moment, and then asks if you have the invoice.")
                .wait(8)
                .outro(
                        "* There is no invoice. There is a delivery slip. It says Southern Cellars, in a hand you cannot place. You write to the vintner. You receive, by return, a letter of astonishment. They have not shipped to the Long Table in a year. The cheerful man in the wide hat was not theirs. The wine in the cellar, bright and fragrant and honest, has come from nowhere, and it is not yet clear who is meant to drink it.",
                        "@Ox| I've put it under the bench, where nobody will sit on it. Should I taste it?",
                        "@You| No.",
                        "@Ox| I wasn't going to. I'm only saying that I would have been brave.",
                        "* You pour a glass for a stray cat, which sniffs it, and sits down beside it, and does not drink. You take the wine to the cellar and lock it, and put the key in your boot, and sleep with the boot under your pillow.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("helper", "Pell Offers a Hand").gap(16)
                .intro(
                        "* The Jade Table's coach comes up the road on the Monday with three green-aproned footmen on the back and Gaspard Pell inside, in a coat so green it hurts. He gets out slowly. He has put on weight since the cook-off, and lost something else.",
                        "@Pell| I have come to be useful. I am told you are short of hands. I have forty. They are trained, silent and clean.")
                .meet("auberon", "Hear Pell's offer, with his son Auberon.")
                .choice(new Chapters.Choice("pellhelp", "Pell offers forty trained servers for the dinner.",
                        "Accept: you need the hands, and he owes you a debt.", "Refuse: the Long Table serves its own dinners.", "Accept half, and put Ox at the door of their kitchen.")
                        .after(
                                new String[]{"@Pell| You will not regret it. I am a man of my word, and I am keenly aware of what I owe your family.", "* His smile is as wide as ever. This time, for one moment, it is also the smile of a man who is trying to do a good thing in a crooked way. Nobody has ever given him the chance to try it any other."},
                                new String[]{"@Pell| (after a pause) Of course. A kitchen's a kitchen.", "* He says it very nicely, and goes away. Ox watches the coach down the road, then turns and says that he is, on reflection, glad. He says it with the face of a man who would have enjoyed having the forty."},
                                new String[]{"* Pell sends twenty. Ox stands in the door of their station, arms folded, saying nothing, which is more unnerving than any speech. Auberon spends the week looking at the floor.", "@Pell| I like your door. I'd like to hire him."}))
                .outro(
                        "?culprit=3|* Auberon stays a moment after his father has gone. He says nothing, but he handles the porcelain as though he had not meant to. He asks which plates will be given to the Elder and which to the Matron. He is, you notice, rather sorry about something, and rather determined.",
                        "?culprit=1|* Dill Harrow watches the green footmen carry a tray across the yard with the particular attention of a man counting the spoons.",
                        "?culprit=2|* Pascoe Bray, passing through on an errand for his uncle, is amused by the green coats and says that Pell has always been the sort of man who sends his own sheep to be shorn.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("dog", "The Dog and the Plate").gap(16)
                .intro(
                        "* On the Thursday before the dinner the widow's dog falls over in the yard. It is a big shaggy animal called Admiral, and it has never in its life been known to rest. It lies on its side with its tongue out. Ox weeps. The widow, who has no sentiment, goes quite white and has a sudden need to sit down.")
                .wait(8)
                .outro(
                        "* Admiral is not dead. He is asleep, very deeply, and snoring. He has eaten something. You search the yard on your knees, with Cassia holding the lantern. In the corner by the back door you find a plate of scraps from the rehearsal dinner, half finished, with the glaze worn off in a pattern like a bite.",
                        "?culprit=1|* The scraps include a dish of the soup. You taste a tiny drop on your finger. It is the soup, and it is also something very mild and green that was not in the pot. You remember a spoon in the wrong hand.",
                        "?culprit=2|* The scraps include a piece of the cake. You taste a crumb. It is cake, and under the cake there is the faintest warm bitterness. Nutmeg, in strength. A great deal of it, ground very fine, and added with a heavy hand by someone who did not know how little it takes.",
                        "?culprit=3|* The plate itself is faintly green at the rim, and the glaze has dissolved a little where the dog licked it. It is a Jade Table plate. You hold it to the lantern. The surface has a bloom on it, like frost, that rubs away under the thumb.",
                        "@Cassia| It is a very clumsy attempt. I think that is the thing I find most frightening. Somebody has done this who isn't good at it, and who doesn't mind.",
                        "* Admiral wakes at dawn, and eats a whole ham, and is entirely himself. Ox sits with him all night. He says it was only that he did not want him to be alone.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("backdoor", "Questions at the Back Door").gap(16)
                .intro(
                        "* There are eleven people who have been in the kitchen in the last week. Cassia lines them up in the yard in the grey light, as a magistrate would, with a clipboard, and does not look like a magistrate at all. {cassia.He} looks like a very tired person who is asking a favour of strangers.",
                        "@Cassia| I am not accusing anybody. I should like to be told where everybody was on Thursday afternoon, and what each of you saw. The Hall has given me no powers whatsoever. You may of course decline.")
                .friends(8, "Have the crossing's people stand with you: make eight friends.")
                .outro(
                        "* Nobody declines. The whole village comes, at last, to sit on the bench, and tell what it saw. The carter saw a green-aproned boy at the back door at dusk, with a basket. The smith saw a thin man with a moustache cross the yard twice. The widow saw nobody, and did not like it. A small boy saw a cat sit down beside a glass of wine, and not drink.",
                        "?culprit=1|* You find that all three stories have a hand in the same place: the soup cauldron, between four and five on the Thursday. And Dill Harrow, on his own account, was in the yard, on the left of the door, tasting.",
                        "?culprit=2|* You find that Pascoe Bray was not, as he said, on the road to the Pavilion on Thursday. A groom at the Saltglass saw his horse in the stable at three. The groom is troubled. He says it was not a thing a nephew does, to say he was somewhere he was not.",
                        "?culprit=3|* You find that Auberon Pell, who says he was in the Jade Table's cellar on Thursday, was seen on the Long Table's bench with a hamper. It is the sort of coincidence that is not.",
                        "* The yard at dusk is crowded with lanterns and quiet. The village has put its mind to it. It is a good deal easier to be afraid of one thing than of ten, and everybody has begun, gratefully, to be afraid of the same one.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("eve", "The Eve of the Dinner").gap(16)
                .intro(
                        "* The long table is laid with forty places and the Jade Table's porcelain, or without it, depending on what you chose. The candles are trimmed. The kitchen is spotless, which in a kitchen is a kind of alarm. At midnight, when the village has gone to bed, you sit in the dark by the stove with a loaf, a knife and Cassia, and decide how to catch a poisoner who does not know that anybody is looking.")
                .wait(4)
                .choice(new Chapters.Choice("trap", "A poisoner is planning something tomorrow, and you know three things about how.",
                        "Watch the kitchen: sit up all night with Ox at the door.", "Change the menu at the last moment, so that no plan can fit.", "Tell the guests the truth at the start of the dinner, and let the culprit show.")
                        .after(
                                new String[]{"* You sit up. At three in the morning there is a scratching at the back, and a sound like a tray set down, and then silence, and then Ox says in a voice of terrible hope, 'Should I go?' You go. There is nobody. There is a basket on the step, covered with a cloth, with a note in a small neat hand: Sorry. It was meant to be a warning.", "@Cassia| That's a clean confession by omission. I'm not sure I like it."},
                                new String[]{"* You change the menu, dish by dish, at five in the morning, in a bad temper. The sequence is odd, the courses are unbalanced, and the pastry has been moved to the middle. It is the sort of dinner a drunk would plan. It is also, you are fairly sure, impossible to poison on the schedule that somebody has so carefully made.", "@Rue| (in the margin, in pencil, in a hand that has been added since the last time you looked) The cook who changes the menu changes the war."},
                                new String[]{"* You do not sleep. At dawn you sit down with Cassia and write a short speech, which says, in nine lines, that the Long Table has been warned of an attempt on its guests. That it will serve nothing that has not first been tasted by the cook. You underline the last line twice. It is the most dangerous thing you have ever written.", "@Cassia| It is either brave or stupid. In this house I find I can't distinguish them."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("courses", "Seven Courses").gap(8)
                .setup(TaleChefSeasons::mark)
                .intro(
                        "* At dawn the first coaches come up the road in a long grey file, with their pennants furled and their outriders silent. The Saltglass comes first, in black. The Ironwood comes after, in green. They stop at either end of the long table, thirty feet apart, and nobody gets down for a very long minute.",
                        "@Ox| Should I ring the bell?",
                        "@You| Ring the bell.",
                        "* The first course is a clear soup, and it is silent. Forty people lift their spoons together, like soldiers, and look at no one. The second is bread. It is dark and sour and baked on the crossing's own stone, and the Elder, who has not eaten bread in front of the Matron in nine years, breaks it with both hands.")
                .made(7, "Serve the dinner: cook seven courses.")
                .outro(
                        "* Over the third course (a clear fish, in a green sauce) someone coughs. Over the fourth (a roast fowl, with a jelly of plums) the Matron, without being asked, passes the Elder the salt. He looks at it for a long moment, as at a snake. He takes it. It is the first thing either of them has handed the other in nine years, and everyone at the table has stopped breathing.",
                        "* The fifth is the poisoned one. You know it by the way the footman carries it, and you know it by the way Cassia, at the end of the table, sets down {cassia.his} chopsticks. A bowl is put before the Elder. A bowl is put before the Matron. Both are the same, and both smell of rosemary.",
                        "?culprit=1|* You are across the room before you know it. You take the Matron's bowl out of her hands and put it down, and look at Dill Harrow. He is standing at her left. He has the chopsticks in his right hand. He says, in a thin high voice, that he did not taste it, and that he was told not to. His face is the colour of tallow.",
                        "?culprit=2|* You are across the room before you know it. You put your hand over the Elder's bowl, and say, quite loudly, that there is nutmeg in it. A silence. Pascoe Bray, at his uncle's elbow, has gone white. 'There isn't,' he says. 'I mean, I didn't,' and stops, because he has heard what he said.",
                        "?culprit=3|* You are across the room before you know it. You lift the bowl and show them all the rim. It is faintly green. Auberon Pell, standing at the door with the footmen, has the face of a man who has been watching his own clock run down. 'It was only to be a sleeping thing,' he says. 'It was only to be enough to stop it.'",
                        "* Nobody leaves the room. Nobody speaks. It is the last thing in the world anyone expected: forty people, at a table, in the quiet, watching a cook hold a bowl.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("seventh", "The Seventh Course").gap(8)
                .intro(
                        "* The truce is not made in the speeches. It is made in the half hour after, when the guilty has been taken to the back kitchen and the guests have stayed. Nobody has gone home. They sit and eat the sixth course, a bitter salad, in a silence that has changed in texture. The Matron says, to the table, that she supposes the poison was meant for them both. The Elder says he supposes it was. For the first time in nine years they have something in common, which is that someone tried to kill them in front of everyone.")
                .wait(4)
                .choice(new Chapters.Choice("justice", "The guilty one sits in the back kitchen with Ox at the door, and the sects wait for you.",
                        "Hand them over to the sects for judgement.", "Let them go, and take the blame for the dinner.", "Make them wash up. Then ask what they wanted.")
                        .after(
                                new String[]{"* You hand them over. The sects take them away, together, in a plain coach. You never learn what was done. You tell yourself that is the law, and you know that you did not choose it, only failed to choose anything else.", "@Cassia| You were within your rights. It's a fine thing, being within your rights. It doesn't keep one warm."},
                                new String[]{"* You say it was your mistake: a stray herb, a careless hand. It is not believed by anybody. It is accepted by everybody, which is better, and the guilty one leaves in the dark with a loaf and a purse. They send you a letter a year later, with no name, which says only that the bread was good.", "@Cassia| I will have to write that up as an accident. I shall do it extremely badly."},
                                new String[]{"* They wash up. It takes three hours. They cry through the first. They talk through the second. By the third they have told you the whole of it, in a low voice, over a bowl of dishwater, with Ox drying plates and pretending not to hear. What they wanted. Who had asked them. How small it had all been at the start.", "@Ox| That's the first time I've seen a person wash a plate to confess. I think it's the soap."}))
                .outro(
                        "* The seventh course is a dessert, a plum tart, and it is served just before dawn. The Elder and the Matron eat it side by side at the end of the long table, with their elbows touching, like children on a bench. The Matron says she has not had a plum tart since her mother's funeral. The Elder says he hasn't had one since his own wedding. Neither of them says anything about the ford.",
                        "* A treaty is signed on the back of the menu. It is eleven words long. Under the signatures, in Ox's enormous hand, is the word WITNESSED. Under that, in pencil, in the cookbook, is a note: A war is people sitting very straight. The way to end it is to make them lean over.")
                .pay(500, 0, 0, 1, 0, 0);

        d.chapter("report", "Cassia's Report").gap(80)
                .intro(
                        "* The Cobalt Fan Hall summons you to answer for the dinner. They have received the Examiner's report. It runs to ninety pages. It has been read in three sessions, by a committee of seven, in a room with no windows. You are not told what is in it. You are told that it has been 'noted', in a voice that would freeze a duck pond.")
                .realm(2, "Stand your ground: reach Major Lvl 2.")
                .outro(
                        "* They offer you a choice that is not a choice. The Hall will license the Long Table in perpetuity, with no herb restriction, on the single condition that the Hall may call it, whenever it wishes, a neutral ground for peace. They will send you their quarrels. You will feed them. They call it an honour.",
                        "?love=3|@Cassia| (afterwards, on the bench) They've made it official. I wrote that bit myself. I thought: if they have to have a use for you, at least let it be a good one.",
                        "?love=1|@Cassia| (afterwards, at the stove) I wrote that part myself. I shall have to be here often to see that they keep to it. A long inspection. Many forms.",
                        "?love=2|@Cassia| (afterwards, at the stove) I wrote that part myself. I shall be here often to see that they keep to it. I seem to be here often anyway.",
                        "* You put the licence in a frame and hang it by the door, next to the board. It looks very official. Under it, in pencil, in the new margin that has grown on the wall, someone has already written: Nobody means nobody.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "Plum Wine",
                "* The ford is open. A bridge has been talked of. It has been talked of, in fact, for three years, which in the country is as good as built.",
                "?justice=1|* The one who tried to poison the dinner was judged in a sect court, behind a green door, without witnesses. You don't know how it ended. You light a candle on the first of every month at the end of the long table. Ox sets it straight.",
                "?justice=2|* The one who tried to poison the dinner sends a loaf at midwinter, by a different carrier each year. It is always very good bread.",
                "?justice=3|* The one who tried to poison the dinner washes up on Tuesdays. Nobody has asked them to. Nobody has asked them to stop.",
                "* The Long Table has forty places now. It had thirty-two before. Rue wrote from the coast to say that the point of a table was that it should always be a little too small for the people who needed it. A little too big for the people who could afford it.",
                "* You finish a good plum wine in the autumn, from a tree in the north meadow that was a seedling when Rue left. It is dark and sharp and sweet and smells of rain. You send a bottle down the coast, with a card in your own hand: It tastes of the room.",
                "* A long way off, in the Greywood, a grower in a green coat bends to a row of dried pepper and finds, on the third plant from the end, one leaf that has come back the right colour. She does not tell anyone. She sits in the dirt and looks at it until the light goes.");

        // ================================================================== SEASON 3
        d.season(3, "The Hungry Year");



        d.chapter("thin", "The Thin Harvest").years(2)
                .intro(
                        "* It starts with the pepper and spreads to the wheat. The first loaves of the year come out of the oven golden and high and smell of bread, and taste of a sort of pleasant absence, like a word that has been said too often. The butter is correct. The honey is correct. Nobody can find anything wrong with any of it. People eat, and push the plate away, and say they are not hungry, and are surprised to find that they are.",
                        "* Hazel comes down from the Sunroot Valley on a mule, with three sacks of the dry pepper and no wish to sell it. She sets one down on the long table and says that she has had it analysed. It has everything in it that pepper should have. It has no bite. The valley is in the same state. So, she hears, is the coast.")
                .wait(12)
                .outro(
                        "* You taste the pepper and it is pepper, in the way a painted door is a door. You taste the barley and it is barley. You stand in the middle of the kitchen with a spoon and have the oddest feeling that the world has been dimmed by a hair. That you are the only one in the room who is old enough to remember the light.",
                        "@Cassia| The Hall has had reports from nine provinces. They are calling it the Thin Year. They think it is a blight. They have sent a delegation of botanists. I went to meet them and I am sorry to say they were quite useless.",
                        "@Ox| It's only that nothing tastes of anything.",
                        "@You| That's the trouble.",
                        "* You write on the next clean page of the cookbook, in pencil: Hunger is not about food. And underneath, because you are a cook: or not only.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("line", "The Soup Line").gap(80)
                .setup(TaleChefSeasons::mark)
                .intro(
                        "* By the autumn the queue starts at the dawn bell. It is three hundred strong by noon. There are farmers whose crops have gone flat in the field, and pilgrims who have run out of road. There are whole families from the valley who came for a meal and have stopped, uncertainly, at the edge of the yard. They stand in the cold with their bowls in their hands and wait to be fed. They do not complain. That is the part that frightens you.",
                        "* There is a rule over the door, and you have never in your life been so aware of it.")
                .made(30, "Feed the line: cook thirty dishes.")
                .outro(
                        "* You do not stop. Ox does not stop. The widow, who has no sentiment, brings her whole flock of hens and does not ask for them back. Brannock the sergeant, who has slept in the stable since the cook-off, stands at the door and counts heads in a firm voice. Three hundred and four. Three hundred and five. He counts like a man taking the pulse of a patient.",
                        "* By evening you have cooked for three hundred and eleven, and you have used every pot, and every pot's bottom, and half the sacks of the winter store. Rue's sharp voice comes back to you from the other side of the country, sixty years of kitchens in it: Never throw out the bottom of the pot. You have not thrown it out, but you are, in fact, feeding it, with a little of everything, so that the soup of the oldest pot in the crossing can go round again.",
                        "@Cassia| I've counted the stores. We have a month. Perhaps five weeks if you stop making pies.",
                        "@You| I'm not going to stop making pies.",
                        "@Cassia| No. I didn't think you were.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("tide", "The Tide at the Crossing").gap(64)
                .intro(
                        "* The beasts of the Greywood have been going hungry too. For three weeks now there has been a sound at night from the northern hills, a low, enormous murmur, like a crowd trying not to cry. On the morning of the first snow, it stops. In its place there is a silence and a shaking in the ground, and then Ox, at the door of the kitchen, in a voice you have never heard from him, says, 'There's a lot of them.'",
                        "* They come down out of the wood in a long grey mass. Boars the size of carts, a herd of the great woolly deer, wolves in packs of forty, and over them, wheeling in the thin cold light, a flight of hawks with wings like barn doors. They are not attacking. They are walking, with their heads low, toward the smell of the kitchen.")
                .wait(4)
                .choice(new Chapters.Choice("tide", "A Beast Tide comes down the road to the Long Table, hungry and slow.",
                        "Fight: bar the gate, and defend the village.", "Feed them: put the pots in the yard and ladle.", "Lead them: take the pots up the road, and walk them away.")
                        .after(
                                new String[]{"* You hold the gate. It is a short ugly fight, with a great deal of noise and not a great deal of heart on either side. When it is done a dozen beasts are dead in the road and the rest have turned and gone, sullen, back into the wood. The village is safe. You do not sleep. In the morning you find that someone has covered the dead with sacking, and put a bowl of water beside each, as though it might be a comfort.", "@Brannock| It was well done. I don't want to do it again."},
                                new String[]{"* You put the pots in the yard and ladle. You feed the boars on the steps, and the deer in the lane, and a wolf the size of a pony from the end of a spoon. Nobody tells you how much; you are too busy. Ox walks among them with a bucket, murmuring, and once, to your horror, lies down in the straw with a she-wolf's head on his knee.", "* By dawn they have eaten the stores of a week. They lift their heads, one by one, and look at the lit windows of the kitchen, and go. It is the only time in your life you have been thanked in several languages at once, none of them human."},
                                new String[]{"* You carry the pots up the road in a procession, with Ox ladling and the sergeant ringing a handbell. You walk the Tide away from the crossing, a mile, and another mile, to a clearing in the high wood where there is a spring. The beasts follow the smell. You leave a dozen cauldrons there, steaming, in the snow, and walk home through a hundred and fifty pairs of watchful eyes.", "@Cassia| I would like it written down that I did not see this. I would also like it written down that you are the bravest person I know."}))
                .outro(
                        "* The Tide goes, at last, as it came, in a long file into the dusk. You stand in the road with a ladle in your hand and the smell of onions on your clothes. A last boar stops in the road and looks at you. It is old, its muzzle is white, and it has the expression of a creature who has done a long day's work and does not intend to discuss it.",
                        "* It sniffs the air. It makes a small sound, almost conversational. Then it turns and trots after the rest, and you realise it is the first thing in a month that has looked as if it had enjoyed its dinner.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("cellars", "Pell Opens the Cellars").gap(48)
                .intro(
                        "* The coach has no green on it. That is the first thing you notice. It is plain brown and mud to the axles, and the man who gets down from it is plainly dressed and plainly tired. Gaspard Pell has lost two stone and his smile. He stands in the yard of the Long Table with his hat in his hands and says that he has come to speak to the cook.",
                        "?culprit=3|* His son Auberon is not with him. The sect courts took him, after the dinner. Pell does not mention it. His face does, all by itself.")
                .meet("pell", "Hear what Gaspard Pell has come to say.")
                .outro(
                        "@Pell| I have eleven cellars. They are filled with food. I stocked them for a year of fine dining, and I find I have a hundred thousand people about to starve in a season. My accountants say I may hold on. My advisers say I should hold on. My wife has told me, in the plainest language, what I may do with my advisers.",
                        "@Pell| I do not know how to cook. I have never in my life prepared a meal. I know how to buy, and how to sell, and how to be flattered. I have come to ask if you would be so good as to tell me where to put it.",
                        "* You take him to the long table and sit him down. You give him a bowl of the old soup, the pot-bottom, the one that tastes of everyone. He eats. He eats all of it. When he is finished he sits for a minute looking into the bowl, and says in a voice that is no one's but his own: 'I remember this. My mother made this. She had a pot in the back that she never washed.' He is silent. 'I thought I'd made it up.'",
                        "* He gives away the cellars on the same day, to the crossing, to the valley, to the coast road, and he does not ask for a price. He asks that they be fed from the Long Table, and that the sign on the Jade Table be taken down. It is taken down by Ox. It is hung in the kitchen, upside down, as a shelf.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("cart", "A Cart from the Palace").gap(32)
                .intro(
                        "* A plain brown cart with a patient horse comes up the road on a morning of frost. It has no insignia. The driver is a Wick of the Outer Court, a young man in a very thin coat. He has the manner of someone who was told what to say and has rehearsed it, over and over, the whole way.",
                        "@Wick| I'm from the Palace of the Oldest Order. I have a message. It is for the cook of the Long Table. It is from the Under-Kitchen. I don't know what that is. I was told to say please.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and down to the Under-Kitchen.")
                .outro(
                        "* The message is a note on a bit of brown paper, in a careful, round, unpractised hand, like a child's. It says: We have a pot that has never been stirred. We think you might know how. It is signed with three question marks. Under the question marks, in smaller letters, someone has added: He asked me not to say who he is. I think you will know.",
                        "* The Palace is vast and silent. The Wick leads you through the great hall, past the long table where the Nine sit in their lamps, and down a stair that is on no plan, and at the bottom there is a kitchen. It is the warmest room you have ever entered. A great black range runs the length of one wall, cold. Pots hang from hooks, polished and untouched, and on a table in the middle there is one bowl, and one spoon, and an empty chair.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("kitchen", "The Man in the Apron").gap(8)
                .intro(
                        "* He is sitting at the end of the table in a plain apron, a man of no particular age, with a bowl in front of him and a spoon in his hand. He looks up. He has the face of someone who has only lately learned that hunger can be for something other than food. He stands. He sits again. He stands again.",
                        "@???| I am sorry. I am not good at hosts. You are the cook. I am a sort of manager. I can't say what of. I'm told it is in the nature of the position.")
                .wait(4)
                .outro(
                        "@???| I have eaten here every day for ages. I've only lately learned how. I find that it is difficult, and that it is the only thing I am sure of. Something is the matter with the food. I can feel it going. It isn't the pots. It's the, I am not allowed to say. I am allowed to say that I have watched a very great many meals go cold, and I thought you might know why.",
                        "* You taste the soup. It is a soup of the Palace, rich, correct and flavourless. You put the spoon down, and ask him, as gently as you can, how long it has been since he was hungry. He thinks about it for ages.",
                        "@???| Taste is attention. It is what the world pays to a thing when it is looking at it. It is going out of the Garden because the one who is looking has other matters. I can't say more than that. I can say it is not your fault, and not mine, and that it will not be mended by an extra spoon of salt.",
                        "* He pushes the bowl across the table. His hands are shaking a little. He says: 'Will you teach me?' And you, who have not in your life refused a person who asked to be fed, roll up your sleeves and take down the biggest pot from its hook.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("dead", "The Dead Are Hungry Too").gap(32)
                .setup(TaleChefSeasons::mark)
                .intro(
                        "* The Under-Kitchen has a second door. It is small and low and at the back, behind a rack of ladles, and it opens on a stair that goes down. ??? shows it to you with a certain reluctance. He says that the pipes come up here, from the Cistern, and the Cistern is where the Garden puts everyone it has finished with. He says the pill kitchen is on the next floor down. He says he cannot cook there. He does not, for some reason, say why.",
                        "@???| The dead are brought up from below. They come as water, and are made into pills for the living. It is the oldest custom in the Palace. I would like to say it is kind. I find it is no worse than other things I know.")
                .made(10, "Cook a meal for the Cistern's returning: make ten dishes.")
                .outro(
                        "* You do not ask what the pills are for, but you think of Rue, in the voice she used on the first morning: A cook who hasn't eaten can't be trusted with salt. You make ten small dishes, in the plainest clay bowls from the Under-Kitchen shelves: a barley broth, a dumpling, a slice of bread with honey, a boiled egg, an apple baked with cloves. You carry them down the stair on a tray.",
                        "* The Cistern is a high grey hall with a ceiling you cannot see. Round its walls are basins of black water, and in the basins, at the very edge of sight, a number of pale shapes are turning slowly. You set a bowl by each basin. You do not know what you are doing, but you say, to the water, in a low voice, 'It isn't much. It was made by someone who thought of you.'",
                        "* The water stirs, once, like a sleeper turning over. Then, one by one, the bowls are empty. You cannot say where it went. You stand there, holding the tray, and find that you are crying, and that you are not sad, and that there is nobody to explain it to.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("rue", "Rue Comes Home").gap(24)
                .intro(
                        "* The letter arrives with a pot. It is a battered black iron one with a bent handle, and a note: Bring it back clean. R. You turn the pot over in your hands, and see that the date of its making is stamped into the bottom, and it is older than the Long Table.",
                        "* A week later the carrier stops at the crossing and Rue gets down. She is eighty-five. She leans on a stick and a boy. She looks at the kitchen, and at the door, and at the board over it. She sniffs.")
                .meet("rue", "Welcome Auntie Rue home to the Long Table.")
                .outro(
                        "@Rue| Door's on. Good. It'll want oiling. Is that the bottom of the pot? Give it here.",
                        "* She tastes it. She stands for ages with the spoon at her lips, and her face changes, and you see that you have never in your life seen her face change before. She says it does not taste of enough. She says it tastes of us, and the room, and the last year, and a great many other things that she approves of, and that it is a perfectly good pot. She says there is something missing, and that she can name it, and that it is not salt.",
                        "@Rue| It's me. I'm what's missing. I left a bit of myself in this pot sixty years ago, and a pot that's lost a bit won't be whole. You'll want me in the room. I've a dish I never wrote down.",
                        "* That evening she makes it again. It is the plain brown soup with a single dumpling. She makes it with her own hands, in the kitchen, in front of the whole village, with Ox holding the bowl and the widow holding the lamp and Brannock holding his hat. It takes an hour. Nobody speaks. You find, as the pot comes to the boil, that the smell in the kitchen is the smell of everyone who has ever been in it.",
                        "* It is a soup that tastes of the room. And for the first time in a year, in the Long Table, every person who tastes it says the same thing, aloud, one after another, as though giving evidence: I can taste it.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("verge", "Captain Verge Wants Dinner").gap(48)
                .setup((c, st) -> Chapters.spawn(c, st, "verge"))
                .intro(
                        "* He arrives at dusk on foot, a lean grey man in a plain coat that has been mended a good many times. He carries a pack and a short sword. He has the look, you realise, of an officer who has been on a road for a very long time and has forgotten the reason for it. He stops at the door. He looks at the board.",
                        "@Verge| NOBODY IS TURNED AWAY. Is that true?")
                .meet("verge", "Meet the stranger at the door.")
                .outro(
                        "* You say that it is. He nods. He sits down at the end of the long table, in the corner, with his back to the wall. He does not eat for a minute. Then he says, in a voice as dry as paper, that his name is Verge, that he is a captain of the Hollow Court, and that he has been sent for the bottom of your pot.",
                        "@Verge| You have fed a whole country a flavour. It is a great deal of attention. My masters would like it put out. They are not cruel. They are tired, and they think the Garden is a long fever. I have been told that if I carry the pot back to the Court, the taste goes out of the rest of it, and everyone may rest. I do not know that I believe it. I have not eaten in four days.",
                        "* The room is quite silent. Ox has put down his dish. Brannock has a hand on his sword. Rue, at the end of the table, in her chair, has not moved, and has not stopped looking at the captain as if he were a pie that had come out wrong.")
                .outChoice(new Chapters.Choice("captain", "A captain of the Hollow Court asks for the bottom of the pot, and for dinner.",
                        "Feed him a bowl of the old soup, and let him decide.", "Refuse: the pot stays, and so does the rule, and he must go.", "Ask him to cook: put the knife in his hand.")
                        .after(
                                new String[]{"* You ladle him a bowl of the bottom of the pot. He eats, slowly, with a soldier's economy, and halfway down he stops, and sets the spoon down. He says, in a hoarse small voice: 'There's someone in this who knew my mother.' He sits there for ages. He does not take the pot. He leaves in the first light, and does not look back, and he leaves his sword on the bench.", "@Rue| That's a man who's been hungry for something other than soup. Put the sword on the wall."},
                                new String[]{"* You tell him no. It is not easy. He nods, as though he had expected it, and stands, and goes. In the morning the pot is gone from its hook. It has not been stolen, and nobody saw it taken, and where it stood there is a faint grey ring on the stone, like frost. The taste of ninety years is out of the Long Table, and you will have to begin the bottom of the pot again, from nothing.", "@Rue| (quietly) Well. It was a good pot. We'll make another. We begin where we are."},
                                new String[]{"* You put the knife in his hand and an onion on the board. He looks at it as at a puzzle. He has not cooked in thirty years, he says. He cuts the onion, badly. He cuts it again. He stands there in the firelight, a hard man in a plain coat with tears streaming down his face from the onion, and for a minute he is no more than a man cutting an onion. The whole kitchen watches in a delicate silence. He makes a soup. It is dreadful. He eats all of it.", "@Verge| I will say that I could not find the pot. I looked everywhere. I am afraid it must have been taken in a previous raid."}))
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("muster", "Dinner at the Garrison").gap(48)
                .setup(TaleChefSeasons::mark)
                .intro(
                        "* The Palace sends again. This time it is a note, in the round careful hand of the man in the apron, and it says only: They are called. There is a line at the Garrison of the Crossed. It is a long line and it will not be fed. I cannot go. Will you?")
                .made(40, "Feed the line at the Garrison: cook forty dishes.")
                .outro(
                        "* You go with a wagon of pots and a company of the crossing's people, and Rue in the back of the wagon with her stick across her knees. The Garrison stands on a bare grey plain at the edge of the world, empty and whole, with its gates open. In front of it, in a long patient file, in the rain, a crowd of men and women in good boots stand with their blades at their sides. They are quiet. They are very young and very old. There are about two hundred of them, and each has a lamp.",
                        "* You set the long table in the open, on the plain, with planks and trestles and every cloth in the crossing. You cook for two hundred, in the rain, on three stoves, with a captain's sword on a hook over the fire. Ox carries everything. Brannock ladles. The widow brings eggs and does not say why. Cassia, in the grey coat, goes along the line with a basket of bread and says nothing to anyone, and stops at each, and puts a hand on a shoulder.",
                        "* They eat, standing, in the rain. A boy of sixteen asks Rue what is in the soup. She says it is only what is in the room. He nods, as though he had understood. A woman of seventy puts her bowl down on the plank, very gently, and asks if there is any more, and Rue fills it, and then fills it again. It is the quietest meal you have ever served, and the longest, and when the last is done and the line has gone in at the gate, the plain is bare and the rain has stopped.",
                        "@Rue| That's done. Wash up. They'll want us to have done it properly.")
                .pay(600, 0, 0, 2, 0, 1);

        d.chapter("spring", "The Taste of Spring").gap(80)
                .intro(
                        "* The thaw comes in March and the pepper comes with it. Hazel arrives at the crossing on the first dry day with her mule and one sack and a face that has not slept. She puts the sack on the long table. She opens it. She says nothing at all.")
                .wait(8)
                .outro(
                        "* It is green and smells of rain. You put a pinch on your tongue and for a moment you cannot speak. It bites. It bites the way a winter bites, all the way through, and then it opens, like a window in a kitchen, to a flavour of dark earth and sun and something sweet and far off. The whole room is full of people crying and laughing and trying to describe it, and not one of them can.",
                        "@Hazel| It came back on the third day of the thaw. All of it. The wheat, the plums, the whole valley. I don't know why.",
                        "@Rue| Somebody paid attention.",
                        "* Cassia has put a hand on your arm. You have not noticed it until now. {cassia.He} does not say anything. {cassia.He} is looking at the board over the door, and at the lintel under it, where somebody, in the last year, has carved a new line, tiny, in the corner. It says: THE TABLE REMEMBERS.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("margin3", "The Margin").gap(32)
                .intro(
                        "* The apron is stiff with two cooks. You take it down from its hook on a quiet evening in late spring, after the last customer has gone, and sit with it across your knees in the empty kitchen. The cookbook is open on the table. There is one page left in the back, clean. You have been looking at it for a month.")
                .wait(8)
                .choice(new Chapters.Choice("table", "There is one page left, and a Long Table that has fed a mountain.",
                        "Keep the Long Table as it is: a kitchen at a crossroads, for anyone.", "Make it a school: teach cooks from every province, and send them out.", "Take the pot to the Palace: become the cook of the Under-Kitchen.")
                        .after(
                                new String[]{"* You decide to leave it exactly as it is. A door, a board, a long table a little too small for the people who need it. You write in the margin, in pencil: Do nothing clever. Feed whoever is here. You close the book. You are surprised how much it weighs.", "@Rue| That's the whole of it. I thought it would take you longer."},
                                new String[]{"* You build a second hall beside the first, and a third behind it, and put out a notice. In a year you have sixty students, and in five years three hundred, and every one of them has a pot-bottom in a jar to carry home. They call themselves the Table. They write to you. The margins fill up with pencil.", "@Cassia| I will have to license it. I am going to do it very slowly."},
                                new String[]{"* You carry the pot to the Palace, on a cart, with Ox. You set it on the great black range in the Under-Kitchen, and light the fire, and teach a lonely man in an apron to stir. The Long Table goes on without you, in Ox's hands and Rue's, with a door and a board and a very large dog. You come home on the first of every month, with fresh bread, and tell them what the Palace tastes of.", "@???| I find I am learning to be hungry. I did not know it was a thing a person could be taught. It is the best thing that has happened to me."}))
                .outro(
                        "* You put the pencil down. Through the open door, you can hear the crossing in the dusk: the click of a gate, a late customer calling, Ox's slow voice, a dog barking at nothing. The first lamps are lit in the windows along the road. A hundred miles away, in the Under-Kitchen, a man in an apron puts a spoon to his lips, and stops, and stands very still, the way you have seen a good cook stand when something finally tastes right.")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* The Long Table has a door. It is never shut. Over it, on a board black with smoke, it says NOBODY IS TURNED AWAY.",
                "?table=1|* It is still a kitchen at a crossroads. It seats forty and is always a little too small. On any night of the year there is a stranger at the end of it who has walked a long way, and has not yet said why.",
                "?table=2|* It has become a school, and the school has become a custom: a cook who has been through the Table carries a jar of pot-bottom to the next kitchen. That a little of one soup goes everywhere.",
                "?table=3|* The Under-Kitchen of the Palace has a pot that is stirred every day. On the first of the month there is a loaf from the crossing on the long table. The man who eats it says it tastes of the room.",
                "?captain=1|* A grey soldier's sword hangs on the wall of the Long Table, over the fire. Sometimes in winter someone leaves a bowl of soup on the step for no one.",
                "?captain=2|* The bottom of the pot is nine years old now, and growing. It is not the same as the old one. Rue says it is better. Rue says that about everything that is going to live.",
                "?captain=3|* A man in a plain coat visits in the autumn, once a year, to cut the onions. He is still bad at it. He has never been asked to leave.",
                "?love=1|* Cassia keeps the register of the crossing and has never in all these years sent the Hall a report that was entirely true.",
                "?love=2|* Cassia eats breakfast standing up on the porch, with the post, and has not been to the Hall in a decade.",
                "?love=3|* Cassia sits on the bench every Sunday with a book and passes the salt without being asked.",
                "* Sometimes at dusk you take the cookbook down and read the margins. There are three hands in them now: Rue's, yours, and a small round unpractised one that must belong to someone in a plain apron far away. You do not remember when that third hand got in, but you decide not to ask.");

        // @@END
        TaleChefMore.add(d);
    }
}
