package Game;

/** The Lamplighter of Cinder Deep (2026-10-20; new start, three seasons). The human was born owing the Lode Company for the cradle, in a company town built over a silver mine. Season 1, "The Debt" (the working voice,
 *  card B: routine, onomatopoeia, understatement, superstition against scepticism): scrip and lamp rent, the foreman Kell, the company medic Ottoline Harl (the other gender than the human's), a tapping in the wall, a cave-in,
 *  a union in a shed, a strike. Choices: rescue (how Dob is brought out), strike (how the vote goes), love. Flags: rescue, strike, love. Seasons 2 and 3 are in TaleMinerSeasons. */
final class TaleMiner {
    private TaleMiner() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void register() {
        Chapters.Def d = Chapters.def("miner");
        d.season(1, "The Debt");
        d.flags("craft0", "rescue", "strike", "love");
        d.place("mine", "wildnear").place("deep", "wild").place("town", "far");
        d.cast("kell", "Foreman", "Kell").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(8).pot(6).age(55).at("place:mine").rel(0);
        d.cast("dob", "Dob", "Pennyfeather").he().keep().trait(Character.Traits.Miner).side("Good").lvl(2).age(22).at("place:mine").rel(3);
        d.cast("ottoline", "Ottoline", "Harl").opposite("Otto").keep().trait(Character.Traits.Alchemist).side("Good").lvl(5).age(30).at("place:mine").rel(1);
        d.cast("sable", "Mister", "Sable").he().keep().trait(Character.Traits.Merchant).side("Evil").lvl(9).pot(6).age(50).at("far").rel(-2);

        d.chapter("owing", "Born Owing")
                .setup((c, st) -> Chapters.spawn(c, st, "kell"))
                .intro(
                        "* Cinder Deep is a town of four hundred houses built on the lip of a silver mine, all of them the same grey, all of them the Company's. The Company built the houses. The Company built the chapel. The Company built the shop, the school, the pump and the graveyard, and it charges rent on every one of them, payable in scrip, which is good in the Company's shop and nowhere else.",
                        "* You were born in house 211 on a Tuesday in the wet season. The midwife was a Company midwife. The cradle was a Company cradle. By the time you were a week old you owed the Lode Company eleven scrip, four pennies and a farthing, and the sum was written in the ledger under your name in a clerk's brown ink. It has been growing ever since.")
                .at("mine", "Report to the pit-head at Cinder Deep, {@mine}, for your first shift.")
                .outro(
                        "* The pit-head is a grey wooden tower with a wheel on top and a cage underneath, and round the foot of it, at five in the morning, a hundred people in canvas coats stand in the dark with their lamps. Nobody talks. The wheel turns. The cage goes down with a clank. It comes up empty, and the next forty go in, and you are among them.",
                        "@Kell| Name?",
                        "@You| You know my name.",
                        "@Kell| I know your debt. Name's what I call you when I like you. Lamp.",
                        "* Foreman Kell is fifty-five, as broad as a door and as grey as the stone, with a black moustache and a voice that has never been raised in its life. He hands you a lamp, a pick and a tin of dripping. The lamp is rented. It is the Company's. It is half a scrip a week, and he does not apologise for it, and neither do you.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("shift", "The First Shift").gap(8)
                .intro(
                        "* The cage drops four hundred feet in a minute and a half, with a sound like a wet cough. Then there is the gallery. It runs left and right into the dark, as low as a church pew and as long as a street, lit by a string of lamps every thirty feet. The air is hot and tastes of copper. Somewhere far off a pick is going, clink, clink, clink, steady as a pulse.")
                .friends(2, "Work your first month: make two friends.")
                .outro(
                        "* You learn the work in a week. You learn the rhythm: swing, strike, rest, swing. You learn to read the seam by the colour of the dust, and to hear a bad roof by the way the timber ticks, and to eat with your hands black and not care. You learn that the coal-tits in the tool shed are unlucky and the mice are lucky and that you do not whistle underground.",
                        "* The man on your left is called Dob Pennyfeather, a lanky cheerful lad of twenty-two with a gap in his teeth and a pony he calls Marmalade. The woman on your right is a thin silent widow who works the face with a short pick and has not been seen to speak since the spring. They are both fast. You are slower. They never say so.",
                        "@Dob| You get used to it. It takes three weeks. After that you stop noticing the dark and start noticing the other thing.",
                        "@You| What other thing?",
                        "@Dob| The quiet. It's got a sound. You'll hear it.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("scrip", "Company Scrip").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "dob"))
                .intro(
                        "* Pay day is the last Friday of the month, at the pay-window in the Company shop, through a brass grille. A clerk with a green eyeshade counts out your wages in scrip. He counts it twice, so that you may see him do it. Then he counts out what you owe: rent, lamp, oil, the doctor, the chapel, the school, the pump, and the interest. He counts that twice as well. He pushes the remainder under the grille. It is eleven pennies.")
                .wealth(500, "Save against the debt: hold 500 spirit stones.")
                .outro(
                        "* It takes you a year and a half to get the first five hundred. You do it by working double shifts, by picking up spilled ore after the loaders, by catching rats for the shop at a farthing a head and cutting your own hair. You sleep four hours. You eat at the Company canteen, where the stew is grey and the bread is damp, and you send up a prayer of thanks for the day the cook went on strike.",
                        "* When you have it in your hand you walk to the window and ask for the figure. The clerk looks at the ledger. He turns a page. He turns another. He says that the Company has been pleased to note a rise in expenses in the previous quarter, owing to the price of oil. He says that, with allowance, the debt stands at eight hundred and sixty. It was nine hundred and ten.",
                        "@Dob| (at your elbow, in the queue, softly) Forty-eight less. That's good. That's actually good. My dad took thirty years to get under a hundred.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("medic", "The Company Medic").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "ottoline"))
                .intro(
                        "* The infirmary is a whitewashed hut by the pit-head with six cots and a stove and a smell of carbolic. It is run by the Company's medic, a tall, quick, untidy person in a stained white coat who is paid to keep the workforce at its work and has, as far as anyone can tell, never done so. They sit on the edge of a cot bandaging a boy's hand and singing to him under their breath. They look up. They do not stop singing until the knot is tied.")
                .meet("ottoline", "Visit the Company medic at the infirmary.")
                .outro(
                        "@Ottoline| Harl. Ottoline Harl, M.D. I'm told that makes me a doctor. I've been here two years. I'm not allowed to prescribe rest, which is the only thing the men need, so I prescribe cough linctus and tell them to take it lying down.",
                        "* {ottoline.He} is about thirty, with ink on {ottoline.his} cuffs and straw in {ottoline.his} hair and a wry crooked mouth. {ottoline.He} listens to your chest with a cold steel trumpet. {ottoline.He} says that your lungs are good, your heart is good and your back is a disgrace. {ottoline.He} says that in eight years you will be coughing black. {ottoline.He} says it in the voice of a woman reporting the weather.",
                        "@Ottoline| I keep a book. It's not the Company's. It's mine. Every man and woman in the pit, with the date of every cough. I've been sending copies to the Medical College for two years. They haven't answered. I'd be grateful if you'd come and look at it some evening and tell me whether I'm mad.",
                        "* You say you will. You go on the Wednesday, with a bag of apples. It is a long thin book, in a small crabbed hand, and every page is a roll of the dead.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("tap", "A Tapping in the Wall").gap(16)
                .intro(
                        "* It starts in the fourth gallery in the wet season. A man puts his ear to the rock to hear for water and hears a tapping. It is slow and regular, three and a pause, three and a pause, like someone with a knuckle, a long way off. He thinks it is the next shift. It is not. The next shift is at the top of the shaft, eating.")
                .wait(8)
                .outro(
                        "* By the end of the month it is the talk of the pit. The old hands say it is the Knockers: the little dead men of the mine, who tap to warn of a fall. The young ones say it is water in the pipes. Kell says it is nothing, and puts a lamp on it, and goes away. The widow with the short pick lays her cheek flat against the rock and listens, and straightens, and goes quite white, and crosses herself.",
                        "@You| What does it say?",
                        "@Widow| (the first words she has spoken since the spring) It's counting. It's counting us.",
                        "* You put your ear to the wall that evening, alone, with the lamp turned low. The rock is cold and slightly damp. For a long minute there is only the sound of your own pulse. Then it comes, very faint, deep down, in the dark. Three and a pause. Three and a pause. And then, quite clearly, a fourth. It is a different rhythm, and it is faster, and it has your name in it.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("cavein", "The Fall in Number Six").gap(16)
                .intro(
                        "* It goes at ten past two on a Thursday afternoon, with a sound like a door slammed in the next county. The floor jumps. The lamps swing. A long rolling thunder runs down the gallery from the north, and then there is dust, and in the dust there is a cry. Dob was at the face of Number Six. You were thirty yards back with the cart. You run.")
                .wait(4)
                .choice(new Chapters.Choice("rescue", "Dob Pennyfeather is under a fall in Number Six, and the timbers are groaning.",
                        "Dig him out by hand, now, with the others, and never mind the roof.", "Shore the roof first, even if it takes the hour he may not have.", "Send for the medic and wait: she knows what a body will stand.")
                        .after(
                                new String[]{"* You dig. Forty people, with picks and shovels and their bare hands, tearing at the rock in the dust and the dark. It takes fifty minutes. You get him out with a broken leg and a cracked skull and a smile on his face, and as the last stone comes away the roof behind you drops with a roar. You are nearly under it. Kell hauls you out by the collar and swears for the first time in your acquaintance.", "@Kell| Never again. Do you hear me? Never again. Not even for him."},
                                new String[]{"* You shore. It is the hardest thing you have ever done. You stand in the dust and give orders, quickly and in a flat voice, and cut props with the saw and set them with the maul while a man's voice, somewhere under the stone, calls for his mother. It takes an hour and ten. When you reach him he is grey and cold. He is breathing. The roof holds, over all of you, for the whole time, and you could weep.", "@Ottoline| (afterwards, on the surface, with a bandage in her teeth) That was either the bravest or the coldest thing I have ever seen. I'd say both."},
                                new String[]{"* You send for the medic. It is the longest eight minutes of your life. Ottoline comes down the shaft on the cage with a bag and a lamp and an expression of cold fury. She crawls into the hole, flat on her stomach, with Kell holding her ankles. She says in a calm voice that he has lost a lot of blood and that if you move that rock he dies. She works for forty minutes. You cut the stone away on her word.", "@Dob| (faintly, as they lift him) Is the pony all right?"}))
                .outro(
                        "* They bring him up on a hurdle at dusk, grey with dust, bandaged to the eyes, and lay him on a cot in the infirmary. The whole town stands in the yard in the half-dark, without speaking, in canvas coats, holding lamps. The widow with the short pick is at the front. She has her hand over her mouth. When the doctor comes out, wiping her hands, and says he will live, nobody cheers. A great breath goes out of them, like wind going out of a field.")
                .pay(200, 0, 0, 1, 0, 1);

        d.chapter("shed", "The Meeting in the Shed").gap(16)
                .setup(TaleMiner::mark)
                .intro(
                        "* The meeting is held in the tool shed on a Sunday night, in secret, with the door barred and a lamp in a bucket. Forty people crowd in among the picks and the coils of rope. Ottoline stands on an upturned crate with the long thin book in her hands. She does not make a speech. She reads out names. She reads out the date of every death in the pit in the last two years, and the cause, and the Company's estimate of the cost.")
                .friends(6, "Raise the shed: make six friends.")
                .outro(
                        "* She reads for twenty minutes. By the end there is no sound but the lamp. A man at the back is weeping without any noise at all. The widow with the short pick is holding her lamp in both hands and staring at it. When Ottoline closes the book she says that she is not a union man and does not know how to run a union. She says she knows how to count.",
                        "@Ottoline| It's thirty-one in two years. Thirty-one dead, out of four hundred. And the Company has paid out, in compensation, a total of forty scrip. I put it to you that this is not a mine. It is a slaughterhouse with a chapel.",
                        "* It is a short meeting. At the end of it they take a vote, by hands, and carry a motion which is read out, rather unsteadily, by Dob from his hurdle in the corner, with a bandaged head. It says that the Cinder Deep Miners' Fellowship shall be formed this night. It says that there shall be a committee. It says that nobody shall sign anything, or say anything, to anyone, without the others being told.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("strike", "The Strike").gap(24)
                .intro(
                        "* The Company hears of the Fellowship in a week. It is, after all, a small town. By Monday Mister Sable is on the morning coach from the city, a plump bland man in a bowler hat and a silk waistcoat, with a case of papers and a smile. He receives the committee in the Company office, in a room with a carpet, and tells them that he is delighted to hear of their interest in safety. He says the Company will consider it. He says in the meantime wages are cut by a fifth.")
                .wait(8)
                .choice(new Chapters.Choice("strike", "The Fellowship has called a vote on whether to strike, and you have the casting voice.",
                        "Vote to strike, now, all four hundred, and shut the pit.", "Vote to wait: gather more, and strike in the spring when the stocks are low.", "Vote against, and negotiate: put the case to Sable in writing and bring the Medical College in.")
                        .after(
                                new String[]{"* The pit shuts at noon on the Tuesday. Four hundred people walk out of the cage into the daylight and stand in the yard, blinking, with their lamps in their hands. Nobody says anything. There is a quiet that is not a defeat. The wheel on the pit-head stops turning for the first time in ninety years. Sable comes to the window of the office, and looks down, and for a moment loses his smile.", "@Kell| (low, at your shoulder, with his moustache working) I've been thirty years waiting to see that wheel stop. I'll be damned if I'll start it."},
                                new String[]{"* You vote to wait. The Fellowship grumbles, but the vote carries. For four months you meet in the shed in secret, and keep the cuts, and put by what you can, and write letters. In March, when the Company's stocks of silver are as low as they will ever be, you strike. The pit stops. The Company has no ore to fill its orders. Sable is on the coach by the end of the week, with an offer.", "@Ottoline| Patience is a terrible thing in a cause. It is also, I find, devastatingly effective."},
                                new String[]{"* You write to Sable. It is a long polite letter, in a clear hand, listing the deaths, the figures and the reforms asked. Ottoline writes to the College. The College, to everyone's astonishment, answers. Within a month a Commission of three grave elderly doctors is on the coach to Cinder Deep, with notebooks, and spends a week underground, and goes back to the city saying nothing at all. Sable's smile has gone grey at the edges.", "@Ottoline| It's the first time in two years anybody has answered me. I wanted you to know I had a small cry."}))
                .outro(
                        "* at last it is not one thing but all of them. The pit is closed for six weeks, or the stocks are low, or the Commission has reported, or the press in the city has taken an interest. Whatever it is, on a grey Friday in spring, Sable comes to the pit-head with a paper. It is a short paper, and he reads it aloud in a thin high voice, looking over the heads of the crowd. It says that the Company, in recognition of the loyalty of its workforce, will recognise the Fellowship.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("offer", "Sable's Offer").gap(16)
                .intro(
                        "* He calls on you privately, on a wet evening, at house 211, with his hat in his hand. He sits on the one chair. He accepts a cup of tea. He says, in a pleasant reasonable voice, that he has been very impressed. He says he has had his eye on you for minutes. He says the Company is looking for someone to be the Fellowship's representative to the Board, with a salary and a house on the hill, and that your debt would be struck off at once.")
                .wealth(2000, "Stand on your own feet: hold 2,000 spirit stones.")
                .outro(
                        "* You say no. It takes you a night to say it, and the next morning to mean it. You send him your answer in a note, on a page torn from Ottoline's book: that the debt is a lie. The Company a larger one, and that you will take no house on the hill while there are four hundred in the valley. He does not reply. A week later you receive a second note in a different hand, from a clerk, in brown ink, which says only that your debt has been revised.",
                        "* The revision is an increase. The sum is eleven thousand. It is, as Dob says, with a rueful grin, the first compliment you have ever had from the Company. You laugh until you cry, in the road, with a loaf under your arm. Then you go home and begin again.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("lamps", "The Lamp Count").gap(24)
                .intro(
                        "* The Company has a way of settling disputes. It counts the lamps. Every miner has a rented lamp, numbered on the base. On the first Friday after the recognition the Company calls a muster at the pit-head, in the rain, and requires every lamp to be handed in for inspection. Three hundred and ninety-seven are brought up. There should be four hundred.")
                .wait(8)
                .outro(
                        "* The missing three are Dob's, the widow's, and yours. Sable reads their numbers aloud from a list in a flat voice, and then the numbers of the lamps that were issued to the dead and to the sick and to the departed, and then a sum. It is a large sum. It is the sum of the lamps that have been lost, in ninety years, which the Company proposes to recover from the Fellowship's funds.",
                        "* It is not true. It is perfectly legal. The lamps are in the Company's own ledger as lost, and the ledger is the Company's own. Kell stands at the back with his arms folded, looking at the sky. Ottoline begins to speak, and you put a hand on her arm. You walk forward, in the rain, to the table. You put down the three lamps, one after another. They are warm. They are still lit.",
                        "@You| That's three. And the rest of the sum is a lie, and everybody here knows it, and I think you do.",
                        "* The crowd does not make a sound. But somewhere at the back a lamp is raised, and another, and then a hundred, all lit, a long wet river of small flames, held up in the dark above the heads of the Company's men. Sable looks at them. He says nothing. He folds the list, and goes.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("paid", "Paid Down").gap(24)
                .intro(
                        "* The last payment is made on a Friday in November at the pay-window, through the brass grille. The clerk in the green eyeshade counts it twice, as always. He looks at the ledger, and turns a page, and turns another, and then he sits quite still for a time with his pen above the paper. He looks up. His face is, for the first time in your acquaintance, a face. He says: 'It's nothing.'")
                .realm(1, "Clear your name: reach Major Lvl 1.")
                .outro(
                        "* He writes it in brown ink in the margin. He turns the ledger round on the counter so that you can read it. It says, beside your name: PAID. You have owed the Lode Company something every day of your life. You stand there, at the window, with the pen and the paper, and have no idea what to do with your hands.",
                        "@Dob| (behind you, quiet, with his cap in his fist) Well. That's you out. That's the first one out in our street in thirty years.",
                        "* You walk out into the yard. It is raining, a thin cold drizzle. The wheel on the pit-head is turning. The cage goes down with its clank. You stop in the middle of the yard with your face turned up, and the rain on your eyelids, and stay there for ages. Nobody tells you to move.")
                .pay(500, 0, 0, 1, 1, 1);

        d.chapter("daylight", "Daylight").gap(24)
                .intro(
                        "* Ottoline is packing. There is a trunk on the floor of the infirmary, and a stack of books, and a tin of bandages, and the long thin ledger, wrapped in oilcloth. The Medical College has written at last, offering a post in the city and a laboratory. {ottoline.He} has not yet said yes. {ottoline.He} says that {ottoline.he} has been standing in the doorway for four days, looking at the pit-head, and has not had one useful thought.")
                .wait(8)
                .choice(new Chapters.Choice("love", "Ottoline Harl has been offered a place in the city and has not said yes.",
                        "Ask {ottoline.him} to stay, and mean it.", "Tell {ottoline.him} to go, and that you will write: a laboratory is no small thing.", "Go with {ottoline.him} for a while, and see the city, and decide there.")
                        .after(
                                new String[]{"* You ask. {ottoline.He} stares at you. Then {ottoline.he} sits down on the trunk, rather suddenly, and laughs, and wipes {ottoline.his} eyes with a bandage. {ottoline.He} says: 'I wanted to be asked. I did not want to be the sort of person who needed to be.' {ottoline.He} writes to the College that evening. The reply says that the post may be held for a year.", "@Ottoline| I'll do my research here. There's a mine, after all. It's the best laboratory I've ever had."},
                                new String[]{"* You tell {ottoline.him} to go. It is the hardest thing you have said. {ottoline.He} looks at you for a very long moment. {ottoline.He} says, quietly, that it is the kindest thing and the worst, and that {ottoline.he} will write every week. {ottoline.He} does. The letters are long and dense and full of cheerful scorn. They end in a single line, different each time, and you keep them in a tin under the bed.", "@Ottoline| (in the first letter) The city is too tidy. There is nobody to be rude to."},
                                new String[]{"* You go with {ottoline.him}. You take a month, and a train, and a room in a lodging-house in a city of a million people who have never been underground. It is loud and bright and strange. You see the sea. You eat an ice. On the last night you stand on a bridge at dusk, with the lamps coming on along the river, and agree, in a few short words, that you will go home.", "@Ottoline| I think I like it better when it's ours. I think I'd rather be needed than admired."}))
                .done((c, st) -> { int l = Chapters.flag(st, "love"); Chapters.bond(c, st, "ottoline", l == 2 ? 1 : 3); if (l == 1 || l == 3) Chapters.wed(c, st, "ottoline"); })
                .outro(
                        "* On the first Sunday after, the whole of Cinder Deep goes up the hill. It is a soft clear spring morning. They walk in a long straggling line, in their Sunday best, with children on their shoulders. They stand at the top in the long grass and look out. They can see the sea. Most of them have never seen it before. They stand there in silence for an hour, in the sun, with their lamps in their hands, unlit.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "Paid",
                "* Cinder Deep is a town of four hundred grey houses. Most of them are the same. The Company still owns the shop, and the chapel, and the pump. But the Fellowship owns the rota, and the safety book, and the sound of the wheel.",
                "?rescue=1|* There is a brass plate on the wall of Number Six with a list of names. Dob's is not on it. Dob's is on the hurdle, which hangs over the door of the infirmary, with a pony shoe nailed beside it for luck.",
                "?rescue=2|* The props in Number Six are set in a pattern now called the Dob. It is in the regulations. It is on the wall of every gallery.",
                "?rescue=3|* The doctor's rule is chalked in the cage: the first person to reach a trapped miner shall be the one who knows how to touch them.",
                "?strike=1|* The wheel stopped for six weeks in the spring of that year. Children are born who are told it as a legend.",
                "?strike=2|* The Fellowship has a reserve fund of eleven thousand scrip. It is in a tin in the vestry.",
                "?strike=3|* The Commission's report hangs in the pit-head, in a frame. Nobody has read it. Everybody knows what it says.",
                "* In the lowest gallery, behind the timber props, in the dark, the tapping goes on. Three and a pause. Three and a pause. It has not changed. It has not stopped. In the evenings, when the shift is done and the cage has gone up, Dob sits on the edge of the shaft with his legs hanging over the dark, and taps back.");
        TaleMinerSeasons.add(d);
    }
}
