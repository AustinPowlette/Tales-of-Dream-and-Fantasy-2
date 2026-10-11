package Game;

/** The Rich Scion (see "Story Plan.md"). Somebody in the household is selling the house's caravan routes to House Kessel. Who it is is rolled when the life begins (flag culprit:
 *  1 the steward Doric Penn, 2 the cousin Ysolde Rook, 3 the bodyguard Gareth Ironwall), and all three have a secret that looks like guilt until the night of the false road.
 *  Choices: the answer to the first ambush (probe), House Kessel's offer (kessel), trusting each suspect (trustD, trustY, trustG), what happens to the traitor (fate), how the trade war ends (trade). */
final class TaleScion {
    private TaleScion() { }

    static void register() {
        Chapters.Def d = Chapters.def("scion");
        d.place("kessel", "far").place("crossroads", "wild").flags("culprit");
        d.cast("doric", "Doric", "Penn").he().trait(Character.Traits.Scribe).side("Neutral").age(61).start().rel(2);
        d.cast("ysolde", "Ysolde", "Rook").she().trait(Character.Traits.Friendly).side("Neutral").age(26).start().rel(1);
        d.cast("gareth", "Gareth", "Ironwall").he().trait(Character.Traits.Blacksmith).side("Good").lvl(4).age(38).start().rel(3);
        d.cast("odile", "Odile", "Kessel").she().trait(Character.Traits.Merchant).side("Neutral").lvl(4).age(58).at("place:kessel").rel(0);
        d.begin((c, st) -> Chapters.set(st, "culprit", 1 + GameDriver.rand.nextInt(3)));

        d.chapter("will", "The Will")
                .intro(
                        "* Your father's study smells of ink, old tea, and the sandalwood he burned when he was thinking.",
                        "* He is gone now. The rain has fallen for three days, as if the sky had been told.",
                        "* The family lawyer is not here. The family steward is. Doric Penn is sixty-one and thin as a quill, and he has kept the books of House {last} since before you could read. He holds the will in both hands, like something that might spill.",
                        "@{doric.first}| The house, the shops, the caravans, and every coin in the vault go to you. Your father wrote it himself a month ago. I watched him sign.",
                        "* Your cousin {ysolde} leans in the doorway with a cup of wine she has not touched. She smiles at you. It is a good smile. It always has been.",
                        "@{ysolde.first}| Congratulations, cousin. Try not to lose it all before spring.",
                        "* By the window stands {gareth}, who has guarded your family since you were small enough to ride on his shoulders. He says nothing. He puts one big hand on your shoulder and leaves it there.",
                        "@{doric.first}| There is one more thing. Your father left this for you, sealed. He said you should open it only when you are ready. He did not say when that would be.",
                        "* It is a plain envelope, heavier than it looks. On the front, in his sharp hand: 'Numbers before people. People before gold.'",
                        "* You tuck it into your sleeve. You have more spirit stones than sense, as your father liked to say. Time to go and find some sense.")
                .wealth(100000, "Show the house is in good hands: hold 100,000 spirit stones.")
                .outro(
                        "* The vault door closes on a hundred thousand and a little more. You stand with your hand on the cold iron, and it feels like the first thing you have ever done by yourself.",
                        "@{doric.first}| Your father would have been proud.",
                        "* His voice cracks on the last word. He pretends to cough.",
                        "* The Salt Road caravan leaves next week. Only four people know its schedule: Doric, Ysolde, Gareth, and you. That is how your father always did it. Small circles, he said, keep big secrets.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("salt", "The Salt Road").years(1)
                .intro(
                        "* The Salt Road caravan was due on Tuesday. It arrives on Friday, with half its wagons and none of its guards.",
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
                        "* By midsummer there are two shops with your name over the door. A small bell hangs above each one. When it rings, you feel it in your chest.",
                        "* That evening a messenger brings a jar of fine plum wine, sealed with wax. The card inside is written in a neat, sharp hand.",
                        "* 'For the new head of House {last}, from a friend in the trade. I admired your father very much. Odile Kessel.'")
                .pay(2000, 0, 0, 0, 0, 0);

        d.chapter("kessel", "House Kessel").years(1)
                .setup((c, st) -> Chapters.spawn(c, st, "odile"))
                .intro(
                        "* House Kessel is the other great merchant house of the province. Your father fought it for thirty years, in prices and in courtrooms, and never said a bad word about it at home.",
                        "* It is polite to answer a gift in person.")
                .meet("odile", "Visit Madam Odile Kessel in {@kessel}.")
                .outro(
                        "* Odile Kessel is silver-haired and sharp-eyed, and she pours her own tea. Her office has no gold in it at all, which is how you know how much she has.",
                        "@{odile.first}| Your father was the best opponent I ever had. When one has a good opponent, one grows careless about being lonely.",
                        "@{odile.first}| I heard about your Salt Road. A terrible thing. The roads are not safe these days. Mine are, as it happens.",
                        "* She lets that sit in the air for a moment, like a coin on a table.",
                        "@{odile.first}| Let us share them. My River Road, your Salt Road, one set of guards. Five thousand spirit stones from me to start, and a promise to leave you alone in the south.")
                .outChoice(new Chapters.Choice("kessel", "Odile Kessel offers a partnership and five thousand spirit stones.",
                        "Accept the partnership.", "Decline politely.")
                        .after(
                                new String[]{"@{odile.first}| Splendid. We shall do very well together.", "* You shake her hand. It is dry and warm. You notice that she did not ask a single question about your guards."},
                                new String[]{"@{odile.first}| A pity. The offer will remain open, but not forever.", "* She smiles, and the smile does not move any other part of her face."}))
                .done((c, st) -> { if (Chapters.flag(st, "kessel") == 1) { c.ss += 5000; c.convertCurrency(); } });

        d.chapter("books", "The Steward's Books").years(2)
                .intro(
                        "* Another caravan goes missing. A third is delayed on a road that nobody should have known. You stop pretending it is bad luck.",
                        "* You begin with the books, because your father said to. Numbers before people.",
                        "* The oldest hands are the ones to ask first. You go to find Doric.")
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

        d.chapter("party", "The Cousin's Party").years(1)
                .intro(
                        "* Ysolde sends you an invitation on pale green paper. A spring party, she writes, at a house of her own.",
                        "* You did not know she had a house of her own.")
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

        d.chapter("brother", "The Guard's Brother").years(1)
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

        d.chapter("road", "The False Road").years(1)
                .intro(
                        "* Three suspects. Three secrets, each of them human, each of them sad. You love all three. One of them is lying to you.",
                        "* So you write three different schedules. Doric hears the North Ford. Ysolde hears the Old Bridge. Gareth hears the Red Pass. No one hears the true route, which you tell to the driver alone.",
                        "* Then you ride to an inn at the crossroads, {@crossroads}, with three messengers, a map, and a cold feeling in your stomach.")
                .at("crossroads", "Wait at the crossroads inn, {@crossroads}, for news of the false roads.")
                .outro(
                        "* The night is long. At dawn, two messengers return and say there was nothing on their road but wind and rooks.",
                        "* The third comes in at a run, mud to the knees.",
                        "@Messenger| Riders at the ford! Twenty of them, all in grey. They were waiting. They waited a long time.",
                        "?culprit=1|* The North Ford. Doric.",
                        "?culprit=2|* The Old Bridge. Ysolde.",
                        "?culprit=3|* The Red Pass. Gareth.",
                        "* You do not move for a long while. The innkeeper puts a bowl of soup in front of you, and you do not touch it.",
                        "?culprit=1|* You find him in the counting room at dawn, where he has been all night. He does not ask how you knew. He folds his hands.",
                        "?culprit=1|@{doric.first}| I thought the house was failing. Your father was ill, the books were thin, and I was afraid. Odile Kessel promised me a pension and a house by the sea for my granddaughter. I told myself it was only dates. Only dates. Then two boys died on the Salt Road.",
                        "?culprit=2|* You find her in the green house with the lanterns, packing a bag. She does not pretend.",
                        "?culprit=2|@{ysolde.first}| Odile promised me the Salt Road. My own road, my own name. I thought the guards would run, not fight. I thought nobody would be hurt. Then I read the names of the two boys, and I couldn't stop reading them.",
                        "?culprit=3|* You find him at the tavern with his brother, and you do not need to say anything at all.",
                        "?culprit=3|@{gareth.first}| Kessel bought Hal's debt. She said, one name and the debt is gone. Then it was dates. Then it was routes. I never gave her a guard's name, I swear it. And two of them died anyway. You can do what you like. I won't lift a hand.")
                .outChoice(new Chapters.Choice("fate", "What happens to the one who sold you?",
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

        d.chapter("trade", "The Last Contract").years(2)
                .intro(
                        "* Odile Kessel does not stop because one of her people has been caught. She does not stop for anything. Within a season she has bought two of your suppliers and made offers to a third.",
                        "* You could sue. You could shout. Your father fought her for thirty years, and the only thing that ever worked was having more.",
                        "* So you get more.")
                .wealth(500000, "Out-trade House Kessel: hold 500,000 spirit stones.")
                .outro(
                        "* When the count is done, you are richer than she is. It is a strange thing to say, and a stranger thing to feel.",
                        "* You find her in the empty office with no gold in it. She is looking out of the window, and she does not turn around.",
                        "@{odile.first}| Your father would have done this ten years ago. You have a good deal of him in you. I hope that is not an insult.",
                        "@{odile.first}| Name your terms, child. I am too old to argue.")
                .outChoice(new Chapters.Choice("trade", "House Kessel is yours to decide.",
                        "Ruin her. Buy every debt she owes.", "Buy a share, and make her a partner on your terms.", "Let her keep her house, and make peace.")
                        .after(
                                new String[]{"* It takes four months. When it is done, the Kessel sign comes down from the great hall and no one speaks of it again. You sleep badly for a year."},
                                new String[]{"* She signs, with a hand that does not shake. 'Fair,' she says. 'I would have asked for less.' You both pretend that is a compliment."},
                                new String[]{"@{odile.first}| That is not what your father would have done.", "@You| No. I'm not him.", "* She laughs, short and surprised. It is the first honest sound you have heard from her."}))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("city", "Ten Thousand Vaults").years(3).major(2)
                .intro(
                        "* You have money now. More than you need, more than anyone needs. It sits in vaults and makes nothing, and you begin to hear your father's voice: 'Money that doesn't move is a rock.'",
                        "* A city is the largest thing a person can make that is not a war.")
                .task("FOUND_CITY", 0, "", "", "Found a city of your own.")
                .outro(
                        "* The first stone is laid on a cold bright morning. You carry it yourself, badly.",
                        "* That night you finally open the plain envelope. The seal cracks like a knuckle.",
                        "* 'My child. If you are reading this, I am gone, and you have done something I never did. Do not look for what I would have done. I made mistakes you will not understand until you make your own.'",
                        "* 'Someone you love will let you down. It may be soon, or it may be when you are old. Decide now what you will do when it happens, because on that day you will not be able to think. I did not decide, and it cost me a friend I should not have lost.'",
                        "* 'Numbers before people, so you do not lie to yourself. People before gold, so you do not become a rock. Be better than I was. I think you will be. Father.'",
                        "* You read it three times. Then you go and stand in the rain until the lamps along the new wall are lit.")
                .pay(20000, 0, 0, 2, 0, 0);

        d.epilogue(
                "* The city grows. It has a market, a school, and a well that was dug on your order and named for a boy who died on the Salt Road. There is a second well named for the other one.",
                "?culprit=1|?fate=1|* Doric Penn kept the books until his eyes failed, and then he taught a clever girl to keep them. He was buried with a ledger under his arm.",
                "?culprit=1|?fate=2|* A postcard comes once a year from a village by the sea. It says only: the sea is as large as I was told. I think of you.",
                "?culprit=1|?fate=3|* Doric Penn came out of prison thin as a quill, and took a job at a school for orphans. He teaches them arithmetic. They like him.",
                "?culprit=2|?fate=1|* Ysolde runs the Salt Road. She is the best guide it ever had. On the day the families said they were square, she cried for an hour in a hay barn.",
                "?culprit=2|?fate=2|* Ysolde opened a small shop across the sea. Sometimes a letter comes, written on pale green paper. You have not yet answered. You mean to.",
                "?culprit=2|?fate=3|* Ysolde came back after three years, quieter, and asked if there was any honest work. You gave her the Salt Road, in the end.",
                "?culprit=3|?fate=1|* Gareth guards the city gate. Hal sweeps the stables, sober, and tells anyone who will listen that his brother is the best man alive.",
                "?culprit=3|?fate=2|* Gareth is somewhere, with a hammer, in a town you have never seen. A message comes sometimes, three words long: Still here. Sorry.",
                "?culprit=3|?fate=3|* Gareth serves out his sentence mending the prison's iron gates. The wardens say they have never had a prisoner who worked so hard.",
                "?trade=1|* House Kessel is a memory. Odile lived out her years in a small house, and sent you a jar of plum wine every year, without a card. You never opened one.",
                "?trade=2|* House Kessel and House {last} do business as one. Odile and you argue every week. It is the best part of the week.",
                "?trade=3|* Odile Kessel died in her sleep, holding the last letter you wrote her. She left you her office, with no gold in it, as a joke only you would get.",
                "* On quiet nights you take your father's letter from its drawer and read the last line. Be better than I was. You are trying.")
                ;
    }
}
