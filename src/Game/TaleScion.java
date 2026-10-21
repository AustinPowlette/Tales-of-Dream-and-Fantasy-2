package Game;

/** The Rich Scion (see "Story Plan.md", revised 2026-10-19 after the author's notes: more mystery, a better false road, the sealed envelope opened when the traitor is known, a child of House Kessel to love).
 *  Somebody in the household is selling the house's caravan routes to House Kessel. Who it is is rolled when the life begins (flag culprit: 1 the steward Doric Penn, 2 the cousin Ysolde Rook, 3 the bodyguard Gareth Ironwall),
 *  and all three have a secret that looks like guilt until the night of the false roads. The envelope the father left holds the real secret: he and Odile Kessel began in one market stall, and he broke the partnership.
 *  Choices: the answer to the first ambush (probe), House Kessel's offer (kessel), trusting each suspect (trustD, trustY, trustG), what happens to the traitor (fate). Season 1 is "The Salt Road".
 *  The city and the trade war with Odile moved to seasons 2 and 3 (TaleScionSeasons). */
final class TaleScion {
    private TaleScion() { }

    static void register() {
        Chapters.Def d = Chapters.def("scion");
        d.season(1, "The Salt Road");
        d.place("kessel", "far").place("crossroads", "wild").place("cellar", "near").flags("culprit");
        d.cast("doric", "Doric", "Penn").he().keep().trait(Character.Traits.Scribe).side("Neutral").age(61).start().rel(2);
        d.cast("ysolde", "Ysolde", "Rook").she().keep().trait(Character.Traits.Friendly).side("Neutral").age(26).start().rel(1);
        d.cast("gareth", "Gareth", "Ironwall").he().keep().trait(Character.Traits.Blacksmith).side("Good").lvl(4).age(38).start().rel(3);
        d.cast("odile", "Odile", "Kessel").she().keep().trait(Character.Traits.Merchant).side("Neutral").lvl(4).age(58).at("place:kessel").rel(0);
        d.cast("linden", "Linden", "Kessel").opposite().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(2).age(27).at("place:kessel").rel(1);
        d.cast("clerk", "Hobb", "Tuck").he().trait(Character.Traits.Merchant).side("Neutral").lvl(0).age(50).at("place:cellar").free();
        d.begin((c, st) -> Chapters.set(st, "culprit", 1 + GameDriver.rand.nextInt(3)));

        d.chapter("will", "The Will")
                .intro(
                        "* Your father's study smells of ink, old tea, and the sandalwood he burned when he was thinking. He is gone now. The rain has fallen for three days, as if the sky had been told.",
                        "* The family lawyer is not here. The family steward is. Doric Penn is sixty-one and thin as a quill, and he has kept the books of House {last} since before you could read. He holds the will in both hands, like something that might spill.",
                        "@{doric.first}| The house, the shops, the caravans, and every coin in the vault go to you. Your father wrote it himself a month ago. I watched him sign.",
                        "* Your cousin {ysolde.first} leans in the doorway with a cup of wine she has not touched. She smiles at you. It is a good smile. It always has been.",
                        "@{ysolde.first}| Congratulations, cousin. Try not to lose it all before spring.",
                        "* By the window stands {gareth.first}, who has guarded your family since you were small enough to ride on his shoulders. He says nothing. He puts one big hand on your shoulder and leaves it there.",
                        "@{doric.first}| There is one more thing. Your father left this for you, sealed. He said you should open it only when you are ready.",
                        "* It is a plain envelope, heavier than it looks. On the front, in his sharp hand: 'Numbers before people. People before gold.'",
                        "* You tuck it into your sleeve. You have more spirit stones than sense, as your father liked to say. Time to go and find some sense.")
                .wealth(100000, "Show the house is in good hands: hold 100,000 spirit stones.")
                .outro(
                        "* The vault door closes on a hundred thousand and a little more. You stand with your hand on the cold iron, and it feels like the first thing you have ever done by yourself.",
                        "@{doric.first}| Your father would have been proud.",
                        "* His voice cracks on the last word. He pretends to cough.",
                        "* The Salt Road caravan leaves next week. Only four people know its schedule: Doric, Ysolde, Gareth, and you. That is how your father always did it. Small circles, he said, keep big secrets.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("salt", "The Salt Road").gap(20)
                .intro(
                        "* The Salt Road caravan was due on Tuesday. It arrives on Friday with half its wagons and none of its guards.",
                        "* The driver, a big man with his arm in a sling, tells it twice. 'They knew the hour. They knew the ford. They knew which wagons held the good stock. Two of our lads didn't come back.'",
                        "* Four people knew the schedule. You count them again. You don't like the count.")
                .choice(new Chapters.Choice("probe", "Someone gave the robbers your schedule. What do you do?",
                        "Look for the traitor quietly.", "Offer a public reward for the bandits.", "Change every route and tell no one.")
                        .after(
                                new String[]{"* You say nothing to anyone. You watch. You find that watching is lonelier than shouting."},
                                new String[]{"* The notice goes up in every square. Within a week you have eleven liars, four honest men, and no bandits."},
                                new String[]{"* You redraw the whole map with a pencil that you keep in your sleeve. Not even Gareth sees it."}))
                .shops(2, "Rebuild your trade: open two shops of your own (the Profession button in a city).")
                .outro(
                        "* By midsummer there are two shops with your name over the door. A bell hangs above each, and when it rings you feel it in your chest.",
                        "* That evening a messenger brings a jar of fine plum wine, sealed with wax. The card inside is written in a neat, sharp hand.",
                        "* 'For the new head of House {last}, from a friend in the trade. I admired your father very much. Odile Kessel.'")
                .pay(2000, 0, 0, 0, 0, 0);

        d.chapter("kessel", "House Kessel").gap(26)
                .setup((c, st) -> { Chapters.spawn(c, st, "odile"); Chapters.spawn(c, st, "linden"); })
                .intro(
                        "* House Kessel is the other great merchant house of the province. Your father fought it for thirty years, in prices and in courtrooms, and never said a bad word about it at home. This is the only thing about the feud that has ever troubled you.",
                        "* It is polite to answer a gift in person.")
                .meet("odile", "Visit Madam Odile Kessel in {@kessel}.")
                .outro(
                        "* Odile Kessel is silver-haired and sharp-eyed, and she pours her own tea. Her office has no gold in it at all, which is how you know how much she has. At the far end a young person in a plain coat is copying figures into a book, and has the look of someone doing it very slowly so as not to have to do anything else.",
                        "@{odile.first}| My child, {linden.first}. Pay no attention. They are shy, and good with numbers, and I am told I make both worse. Your father was the best opponent I ever had. When one has a good opponent, one grows careless about being lonely.",
                        "@{odile.first}| I heard about your Salt Road. A terrible thing. The roads are not safe these days. Mine are, as it happens.",
                        "* She lets that sit in the air for a moment, like a coin on a table.",
                        "@{odile.first}| Let us share them. My River Road, your Salt Road, one set of guards. Five thousand spirit stones from me to start, and a promise to leave you alone in the south.")
                .outChoice(new Chapters.Choice("kessel", "Odile Kessel offers a partnership and five thousand spirit stones.",
                        "Accept the partnership.", "Decline politely.")
                        .after(
                                new String[]{"@{odile.first}| Splendid. We shall do very well together.", "* You shake her hand. It is dry and warm. You notice that she did not ask a single question about your guards. At the door, {linden.first} looks up from the book, and looks away again, a little too quickly."},
                                new String[]{"@{odile.first}| A pity. The offer will remain open, but not for ever.", "* She smiles, and the smile does not move any other part of her face. At the door {linden.first} looks up from the book, and gives you something very close to an apology."}))
                .done((c, st) -> { if (Chapters.flag(st, "kessel") == 1) { c.ss += 5000; c.convertCurrency(); } });

        d.chapter("books", "The Steward's Books").gap(30)
                .intro(
                        "* Another caravan goes missing. A third is delayed on a road that nobody should have known. You stop pretending it is bad luck.",
                        "* You begin with the books, because your father said to. Numbers before people. The oldest hands are the ones to ask first, so you go to find Doric.")
                .meet("doric", "Go over the books with Doric Penn.")
                .outro(
                        "* The vault ledger spans twenty years. It is beautifully kept. It has also been touched, here and there, by a tired hand: small sums, taken and put back, taken and put back.",
                        "?culprit=1|* On Doric's desk, half empty, sits a jar of plum wine with a wax seal you know.",
                        "@{doric.first}| I borrowed from the petty cash. Eleven times in four years. I paid it all back, with interest. It was for my granddaughter's lungs. The healer would not wait.",
                        "* He is looking at the floor. He looks very old.",
                        "@{doric.first}| I should have asked your father. I was ashamed. I am more ashamed now.")
                .outChoice(new Chapters.Choice("trustD", "Doric took money, and put it back.",
                        "Tell him it is forgiven, and trust him.", "Say nothing, and keep watching.")
                        .after(
                                new String[]{"@{doric.first}| Thank you. You are more like him than you know.", "* He blows his nose on a handkerchief the size of a flag."},
                                new String[]{"* He nods once. He understands. You hate that he understands."}));

        d.chapter("party", "The Cousin's Party").gap(26)
                .intro(
                        "* Ysolde sends you an invitation on pale green paper. A spring party, she writes, at a house of her own. You did not know she had a house of her own.")
                .meet("ysolde", "Go to Ysolde Rook's party.")
                .outro(
                        "* The house is small, bright, and full of laughter. Lanterns hang in the plum trees. The wine is excellent.",
                        "?culprit=2|* It is plum wine, in bottles with a wax seal you know.",
                        "* She finds you by the pond, and for a moment neither of you says a word.",
                        "@{ysolde.first}| I borrowed the money from a lender in the south. I'm opening a shop. A small one. I wanted something that wasn't yours.",
                        "@{ysolde.first}| Your father never once asked what I wanted. He asked how I was. It isn't the same thing.",
                        "* She isn't crying. She would never let you see.")
                .outChoice(new Chapters.Choice("trustY", "Ysolde has been building a life of her own, and told no one.",
                        "Tell her you are proud, and trust her.", "Smile, drink, and keep watching.")
                        .after(
                                new String[]{"@{ysolde.first}| Don't be kind. I can't stand it when you're kind.", "* She hugs you anyway, hard and quick, like a door slamming."},
                                new String[]{"* You drink your wine. You watch her laugh across the lawn. You wish you were someone who did not have to do this."}));

        d.chapter("brother", "The Guard's Brother").gap(26)
                .intro(
                        "* Gareth has been with your family for thirty years. He has never once been late. Last week he was three hours late, and he would not say why.",
                        "* You have known him all your life. You still follow him, a street behind, like a thief.")
                .meet("gareth", "Follow Gareth Ironwall and learn where he goes.")
                .outro(
                        "* He goes to a poor tavern at the edge of the city. Inside is a thin, sick-looking man with Gareth's nose.",
                        "?culprit=3|* In Gareth's belt is a flask. The wax on it is a seal you know.",
                        "@{gareth.first}| My brother Hal. He's a gambler. He owes money to some bad people, and I pay what I can every month. I never told your father. I never told anyone.",
                        "@{gareth.first}| I should have trusted you with it. I didn't think you'd understand.",
                        "* He is the biggest man you know, and right now he looks like a boy.")
                .outChoice(new Chapters.Choice("trustG", "Gareth has been paying his brother's debts in secret.",
                        "Tell him to come to you next time, and trust him.", "Say you understand, and keep watching.")
                        .after(
                                new String[]{"@{gareth.first}| Next time. Yes. There won't be a next time. I swear it.", "* He puts his big hand on your shoulder, as he did on the day of the will."},
                                new String[]{"* He thanks you. He means it. You are glad he cannot see what you are thinking."}));

        d.chapter("wax", "The Wax").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "clerk"))
                .intro(
                        "* Three people you love have a bottle of plum wine with a wax seal you know, and not one of them thought it was odd. You think it is the oddest thing in the world.",
                        "* The wine merchant is a few streets from the vault. He has a cellar that smells of cork and cold stone, a ledger as fat as a loaf, and the fixed, helpful cheerfulness of a man who has been asked difficult questions before and enjoyed them. His name is Hobb Tuck.")
                .meet("clerk", "Ask the wine merchant, Hobb Tuck, who buys the Kessel plum wine in your house.")
                .outro(
                        "@Tuck| Kessel's wine? Oh, I don't sell it. I only store it. Madam Kessel's agent buys by the crate and sends it out as gifts, to the right sort of people. Merchants. Magistrates. Stewards. That sort. I hold it in the cellar till the day. It's a hobby of hers. She says a bottle is the cheapest way to say you've noticed someone.",
                        "@You| Three crates came to my house.",
                        "@Tuck| Three, yes. I remember the day. Same afternoon, spring, the third week. A good vintage. And one of them, I do remember it, because a man notices when a crate goes out heavy, went with a second parcel. Small, flat, wrapped in plain paper. Like a letter. I would not say. It is not my business.",
                        "@You| Which one?",
                        "@Tuck| That I could not tell you. They all went out together on one cart. I'm very sorry. I never look at the labels. It is a point of professional honour.",
                        "* He sees your face and says, with real regret, that he does wish he had. The cellar is cold. You stand among all the bottles until your feet ache, with three names in your head and a fourth, in a neat sharp hand, at the back of them.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("road", "Three Roads").gap(12)
                .intro(
                        "* There is a way to catch a liar that your father taught you when you were ten, over a game of draughts. You never knew you remembered it. You tell each of them a different truth.",
                        "* You write three schedules. Doric hears the North Ford. Ysolde hears the Old Bridge. Gareth hears the Red Pass. No one hears the true route, which you tell to the driver alone, and then forget. Then you ride to an inn at the crossroads, {@crossroads}, with the three of them in tow, on the pretext of a family council, and the cold feeling of a person who has set a trap for people they love.")
                .at("crossroads", "Wait at the crossroads inn, {@crossroads}, for news of the three roads.")
                .outro(
                        "* The night is long, and nobody is quite themselves. Doric turns the pages of a book he cannot read in the light. Ysolde plays patience with a deck that is one card short and does not seem to mind. Gareth stands by the door with his arms folded, an old habit, and when the innkeeper asks him to sit he says that he would rather not.",
                        "* The messengers return at dawn. Two of them say there was nothing on their road but wind and rooks. The third comes in at a run, mud to the knees.",
                        "@Messenger| Riders at the ford! Twenty of them, all in grey. They were waiting. They waited the whole night, in the rain.",
                        "?culprit=1|* The North Ford. Doric.",
                        "?culprit=2|* The Old Bridge. Ysolde.",
                        "?culprit=3|* The Red Pass. Gareth.",
                        "* The innkeeper sets down a bowl of soup in front of you. You do not touch it. Nobody in the room looks at anyone else, and one of them, you see, is not looking at you in a way that is different from the others, and has been all night.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("confess", "What the Wax Was For").gap(1)
                .intro(
                        "?culprit=1|* You find him in the counting room at dawn, where he has been all night. He does not ask how you knew. He folds his hands.",
                        "?culprit=1|@{doric.first}| I thought the house was failing. Your father was ill, the books were thin, and I was afraid. Odile Kessel sent me a bottle, and a letter, and in the letter a promise of a pension and a house by the sea for my granddaughter. I told myself it was only dates. Only dates. Then two boys died on the Salt Road.",
                        "?culprit=2|* You find her in the green house with the lanterns, packing a bag. She does not pretend.",
                        "?culprit=2|@{ysolde.first}| Odile promised me the Salt Road. My own road, my own name. I thought the guards would run, not fight. I thought nobody would be hurt. Then I read the names of the two boys, and I couldn't stop reading them.",
                        "?culprit=3|* You find him at the tavern with his brother, and you do not need to say anything at all.",
                        "?culprit=3|@{gareth.first}| Kessel bought Hal's debt. She said, one name and the debt is gone. Then it was dates. Then it was routes. I never gave her a guard's name, I swear it. And two of them died anyway. You can do what you like. I won't lift a hand.")
                .choice(new Chapters.Choice("fate", "What happens to the one who sold you?",
                        "Forgive them, and keep them close.", "Send them away with a purse.", "Give them to the magistrate.")
                        .after(
                                new String[]{
                                        "?culprit=1|@You| You will fix every book you ever touched, and then you will teach someone else to keep them honestly.",
                                        "?culprit=1|* He does, every night for the rest of his life, and he never once asks for a raise.",
                                        "?culprit=2|@You| You will run the Salt Road under my eye, for nothing, until the families of those two boys say we are square.",
                                        "?culprit=2|* She does, and she is better at it than anyone you have ever hired.",
                                        "?culprit=3|@You| We will pay Hal's debt. Not to Odile, to whoever she sold it to. And you will tell me everything you told her.",
                                        "?culprit=3|* He does. It takes all night. He weeps through half of it, and does not stop talking."},
                                new String[]{
                                        "* You count out a fair purse. It is also the end of something.",
                                        "?culprit=1|* Doric takes it in shaking hands and goes to live by a sea, after all.",
                                        "?culprit=2|* Ysolde takes it without looking at it. She leaves on the evening boat.",
                                        "?culprit=3|* Gareth will not take the purse. He walks out in only the clothes he stands in."},
                                new String[]{
                                        "* The magistrate is a tired man who has seen this many times. The law is slow, and cold, and mostly fair.",
                                        "?culprit=1|* Doric is given six years. He thanks the court. He asks that someone water the plants in the counting room.",
                                        "?culprit=2|* Ysolde is given three years, and the shame of it, which is longer.",
                                        "?culprit=3|* Gareth tells the court everything. His sentence is light. It is not light enough to matter to the families of the two boys."}))
                .done((c, st) -> {
                    String[] keys = {"", "doric", "ysolde", "gareth"};
                    Character t = Chapters.person(st, keys[Math.max(1, Math.min(3, Chapters.flag(st, "culprit")))]);
                    if (t != null && Chapters.flag(st, "fate") != 1) t.fatedTo = 0; // sent away or tried: they are an ordinary person now
                });

        d.chapter("envelope", "Sealed Until Ready").gap(8)
                .intro(
                        "* You carry the envelope for another month before you open it. It has been in your sleeve through all of it, through the vault, the road, the inn, and the long walk home from the counting room. The seal is a single drop of dark wax with your father's thumb pressed into it.",
                        "* You open it in his study, at his desk, with the rain on the window, because it seems the thing to do. The seal cracks like a knuckle.")
                .wait(1)
                .outro(
                        "* 'My child. I have been dreading this letter for thirty years, and I find that I have nothing to say that is not an excuse. So I will say the plain thing. Odile Kessel and I began in one stall. Salt and string. Two years we shared a barrow and a quarrel and a bed of straw. She did the sums. I did the talking. We were going to build a house.'",
                        "* 'When the first good contract came, I took it. I put my name on it and not hers. I told her it was the safe way. It was a lie. She was left with the debts of the barrow and I was left with the house. I have called what came after a rivalry for thirty years, because that is a cleaner word. It was a theft, and I have paid for it every day in a currency I can't count.'",
                        "* 'Whatever she does to you, ask what I did to her first. Numbers before people, so you do not lie to yourself. People before gold, so you do not become the thing I became. I do not ask you to forgive me. I ask you not to be me. Father.'",
                        "* You read it three times. Outside, the rain has stopped, and the lamps along the street are lit, and across the province in a plain office with no gold in it, an old woman has just put down a pen.")
                .pay(2000, 0, 0, 1, 0, 0);

        d.interlude(1, "Plum Wine",
                "* The house settles. The caravans run. The two wells in the courtyard are named for two boys, and nobody has asked you to. People say the new head of House {last} is a quiet sort, who keeps the books of the dead.",
                "?fate=1|?culprit=1|* Doric Penn fixes books by candle, with a small boy on his knee, and does not once say he is sorry. He doesn't have to.",
                "?fate=1|?culprit=2|* Ysolde runs the Salt Road. She is the best guide it ever had.",
                "?fate=1|?culprit=3|* Gareth guards the vault, and Hal sweeps the stable. Neither has touched a card in a year.",
                "?fate=2|* There is a letter now and then, from a town with a sea. You leave it a day before you open it.",
                "?fate=3|* The sentence ends in the spring. You meet them at the gate with a coat, and say nothing for the first mile.",
                "?kessel=1|* The Kessel partnership is, on paper, still in force. A bottle of plum wine arrives every month with no card.",
                "?kessel=2|* You declined the Kessel offer, and the Kessel offer, as promised, has stayed open.",
                "* The envelope is in a drawer of the desk, and sometimes you take it out and do not read it. Odile Kessel is old. She will not be forgotten. She has just not yet decided what she wants.");
        TaleScionSeasons.add(d);
    }
}
