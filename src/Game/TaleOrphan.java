package Game;

/** The Orphan of the Ash Road (see "Story Plan.md"): a short line of chapters (Old Brannoch, his death, the Seventh Star, the lantern at the crossroads) and a pool of road tales that
 *  come up by chance while the orphan travels, about one in two years. Five of them are about the brass token with the seven-pointed star (flags f_ferry, f_seam, f_soldier, f_archive,
 *  f_maid; `trail` counts them); when three are found the last chapter opens. A love story (Sol) runs through three of the tales. */
final class TaleOrphan {
    private TaleOrphan() { }

    private static boolean after(Starts.State st, int chapter) { return st.chapter >= chapter; }
    private static boolean wild(Character c) { return c.location != null && !c.location.city && c.location.sect == null; }
    private static boolean town(Character c) { return c.location != null && c.location.city; }
    private static void mark(Starts.State st, String f) { Chapters.set(st, f, 1); Chapters.set(st, "trail", Chapters.flag(st, "trail") + 1); }

    /** Whether this human has read this place of the Palace's story (so a story may notice what they know). */
    static int read(Character c, String site) { Lore.PState p = Lore.pstate(c); return p != null && p.found.contains(site) ? 1 : 0; }

    static void register() {
        Chapters.Def d = Chapters.def("orphan");
        d.season(1, "The Ash Road");
        d.place("home", "start").place("shrine", "wild").place("deliver", "far").place("town", "near");
        d.flags("trail", "f_ferry", "f_seam", "f_soldier", "f_archive", "f_maid", "solw", "sol", "delivered", "sawlanterns");
        d.cast("sol", "Sol", "Marsh").opposite().keep().trait(Character.Traits.Friendly).side("Good").lvl(1).age(26).at("here").rel(2);
        d.cast("brannoch", "Old", "Brannoch").he().trait(Character.Traits.Friendly).side("Good").age(71).start().rel(3);
        d.cast("kasumi", "Kasumi", "Aoi").she().trait(Character.Traits.Innovative).side("Neutral").lvl(2).age(31).at("here").rel(0);
        d.episodes(2, 14);

        // ------------------------------------------------------------------ the line
        d.chapter("brannoch", "Old Brannoch")
                .intro(
                        "* Nobody ever asks where you are from. You are from the road.",
                        "* The road is the Ash Road, a long grey ribbon that runs past a thousand cooking fires. Everyone who walks it leaves a little soot behind. You were raised on it, in the back of a tinker's wagon, among kettles and rope, by a man everyone calls Old Brannoch.",
                        "* You are eighteen now. The wagon is parked in Originis, and so is he. His knees stopped working last winter. He sits in the wagon door with his pipe and watches the crowds go by. He has a habit you have never been able to cure him of. Before he says a thing that matters, he taps the pipe twice on the doorframe, as if knocking.",
                        "@{brannoch.first}| There you are. Sit. Don't look at me like that. I'm old, not dead.",
                        "* Tap, tap.",
                        "@{brannoch.first}| I want to tell you something I should have said ten years ago. You never stay anywhere long enough to get hurt. I taught you that. I was wrong.",
                        "@You| It worked, didn't it?",
                        "@{brannoch.first}| It worked fine. It's a clever way to live, and a lonely one.",
                        "* He presses something into your hand. It is a small brass token, worn smooth, stamped with a seven-pointed star inside a ring.",
                        "@{brannoch.first}| That came with you, the night you came to me. I never told you, because I didn't know what to tell. Keep it.",
                        "@{brannoch.first}| Do an old man a favor. Make two friends who don't have wheels. Then go wherever you like.")
                .friends(2, "Make two friends who are not travelers (people who stay put).")
                .outro(
                        "* The wagon door is open when you come back. Brannoch looks at you over his pipe.",
                        "@{brannoch.first}| Two. Well. Two is a start.",
                        "* He is smiling, and one of his eyes is wet, and he is going to blame the smoke.",
                        "@{brannoch.first}| Now. I've a favour of my own. One more town. Just the one. I was born in a place called {@town}, and I haven't been back in fifty years, and there's a fair there at midsummer. I'd like to sit on the box one more time and watch you drive.")
                .pay(300, 0, 2, 0, 0, 0);

        d.chapter("ride", "One More Town").gap(2)
                .intro(
                        "* It takes eleven days to drive to {@town}. In the old days it would have taken six. The old days did not have a man of seventy-one beside them on the box, who has to be stopped at every second milestone to have a look at it.",
                        "* He tells you the road. Not the roads you know, the great ones. The small ones, between, the ones with names like Widow's Elbow and The Stupid Hill, where the grass grows up the middle and a mule can find his own way. He tells you which inns water the beer, and which of them put a little something in it, and which one has a cat that reads faces. He names every kettle in the wagon, behind you, as you pass them: Gran, Prudence, the Duke, Little Misery.",
                        "@Brannoch| You'll sell the Duke last. Everyone wants the Duke. The Duke is a good kettle, but he's a drama. If I'd had a son I'd have wanted him to be like Prudence. She does the job and she doesn't talk about it.")
                .at("town", "Drive the wagon to the midsummer fair at {@town}.")
                .outro(
                        "* The fair is a long field of striped tents and loud people. You set up the wagon between a man who sells hats for dogs and a woman who tells fortunes by looking at your shoes. By noon there is a queue. Brannoch sits on the box with his pipe, in the sun, and does not do anything at all, and it is the most effective thing he could possibly do. People come up to him. They take his hand. Half of them seem to know him.",
                        "@Woman| Old Brannoch! You sold me a kettle in the year of the Flood. I've still got it. It does the cooking and my mother's temper.",
                        "@Brannoch| Prudence's cousin. She was always a worker.",
                        "* He sells the Duke for far too much, to a man who wants him as a lamp. He gives Little Misery to a girl of nine who has been staring at it for an hour. He tells you, in a low voice, that you must never sell the last kettle, because the last kettle is for the one who needs it.",
                        "* That night he sits on the step of the wagon with the pipe, in the dark, with the fair noise dying round him, and for the first time you hear him sing. It is not a good voice. It is cracked and low and takes the high notes by surprise. It is a song about a road, with seven verses, and he forgets the fifth, and invents it, and then the seventh, and you realise that he has been inventing the lot.",
                        "@Brannoch| I never learned the end. I always thought someone would tell me.",
                        "* Tap, tap.",
                        "@Brannoch| You're a good driver, you know. You hold the reins like you're asking, not telling. That's the whole of it. I should have said so before.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("wagon", "The Empty Wagon").gap(10)
                .setup((c, st) -> {
                    Character b = Chapters.person(st, "brannoch");
                    if (b != null && !b.dead) { b.fatedTo = 0; b.deathCause = "old age (lived " + b.age + " years)"; b.dead(); }
                })
                .intro(
                        "* You have been three days up the road when a boy from the wagon-yard finds you. He has run the whole way, and he is not sure how to say it. He has his cap in both hands and looks at the ground.",
                        "@Boy| The old tinker. He's gone. He just didn't wake up. He had his pipe in his hand and the window open.",
                        "* You thank him. You give him a coin, because you cannot think of anything else to do. Then you turn round and walk back the way you came, faster than you have ever walked. For a mile you tell yourself that it is not possible, because he was fine, because he sang, because he had a fifth verse to find.")
                .at("home", "Go back to the wagon in Originis.")
                .outro(
                        "* The wagon looks smaller. Someone has covered the kettles with a cloth, and someone else, a neighbour, has put a bowl of rice on the step, which is what people do. The wagon door is open. The pipe is on the sill. It is cold.",
                        "* You do the things that there are to do. You wash the cups. You fold the blanket. You find that you have put out his boots by the door, as if he would be needing them, and you leave them there.",
                        "* Under the driver's seat is a battered tin box. Inside are three things: a ledger of every town he ever took you through, a spare pipe, and a letter in his square, slow handwriting. There is a kettle beside the box, the last one, the one you were told never to sell. It is an ordinary kettle, slightly dented. It has a label tied to the handle. The label says: FOR THE ONE WHO NEEDS IT.",
                        "* 'Kid. I don't know how to start this, so I'll just say it. I didn't find you. You were given to me. A woman stopped my wagon on the Ash Road in a winter storm. She was young. She was crying, and she had a cut on her arm. She put you in my lap and said she would be back.'",
                        "* 'She wasn't. I waited two seasons. The token was in your blanket. I never learned whose it was. I should have asked more questions. I was afraid of the answers.'",
                        "* 'You never stay anywhere long enough to get hurt. I think you got that from me. But I think you also stayed on the road because you were waiting for someone. I'm sorry I couldn't be them. I tried. I'd have liked to hear the end of that song. B.'",
                        "* You put the kettle on the stove. It takes four tries to light. When it boils, you pour the water into two cups, because it is what you do, and you sit with the second cup until it is cold.")
                .pay(500, 0, 0, 1, 0, 0);

        d.chapter("star", "The Seventh Star").years(1)
                .setup((c, st) -> Chapters.set(st, "sawlanterns", read(c, "lanternroad")))
                .soft((c, st) -> Chapters.flag(st, "trail") >= 3, "three of the road's tales about the star")
                .intro(
                        "* You lay the pieces on a table in a cheap inn, one by one, like a hand of cards.",
                        "?f_ferry|* A ferryman who had carried a frightened woman with the same mark across the Grey River, twenty years ago.",
                        "?f_seam|* A seamstress who knew the star as the badge of an old house, and whose mother had stitched it onto a hundred uniforms.",
                        "?f_soldier|* A dying soldier who whispered of a winter coup, a burned hall, and a lady who ran with a baby.",
                        "?f_archive|* An archivist who gave the house a name, the House of the Seventh Star, and a lady called Ilan.",
                        "?f_maid|* An old maid who said that a lady lit a lantern at the crossroads shrine every night, for a child who would one day come home.",
                        "* The pieces fit. They fit the way a key fits a lock you have been carrying all your life.",
                        "* There is a shrine on the Ash Road where a lantern burns every night. People have always said it is lit by nobody. You have walked past it a hundred times.",
                        "* You have never stopped.")
                .at("shrine", "Go to the lantern shrine at {@shrine}.")
                .outro(
                        "* It is a small shrine, no bigger than a bread oven, at the edge of the road. Moss grows on its roof. A single paper lantern burns in the window. It has been lit by someone, and recently.",
                        "* An old woman sits on a stool beside it. Her hair is white, and she is wrapping a cut on her own arm, an old scar, the way people touch a thing they have touched ten thousand times.",
                        "* She looks up. She sees the token in your hand. She says nothing for as long as it takes the lantern to gutter and steady.",
                        "@Ilan| You have my husband's chin.",
                        "@You| You left me with a tinker.",
                        "@Ilan| I did. The men who burned my house were an hour behind me. If I had kept you, they would have found you. If I had gone back for you, they would have found us both. So I did the only thing I could. I gave you to the first honest face on the road.",
                        "?sawlanterns|@Ilan| They say there is a road in the wastes where the lanterns burn and nobody lights them. Mine is lit by me. I think that is the whole difference.",
                        "@Ilan| I lit the lantern that night. I have lit it every night since. I told myself that if I stopped, you would not know where to come.",
                        "* You find that you are crying, and have been since the lantern first steadied. It is a very odd feeling to be given something you did not know you had been missing.",
                        "@Ilan| You do not have to stay. I do not ask it. You have a whole life I know nothing about. But there is a kettle on, and there is room by the fire.",
                        "* You take out the last kettle. You had not meant to. You set it on the flat stone beside the lantern, and she reads the label twice, and her face does something that you will not forget.",
                        "@Ilan| FOR THE ONE WHO NEEDS IT. Oh, you foolish, kind old man.")
                .outChoice(new Chapters.Choice("stay", "She is a stranger, and she is your mother, and the fire is warm.",
                        "Sit down by the fire.", "Tell her you cannot stay, but that you will come back.")
                        .after(
                                new String[]{"* You sit. She pours the tea with hands that shake. For the first time in your life, you stay somewhere long enough to be hurt, and it is the best thing you have ever done."},
                                new String[]{"@Ilan| That is enough. That is more than enough.", "* She hands you a small paper lantern, unlit. You do not know what to say, so you light it from hers, and carry it away down the road."}))
                .pay(0, 5, 0, 1, 0, 0);

        d.interlude(1, "The Lantern at the Crossroads",
                "* The Ash Road is still the Ash Road. You still walk it. But now there is a shrine on it where a lantern burns, and someone inside who knows your step.",
                "?stay=1|* You came to live near the shrine, in a house with a roof that leaks. Ilan says it needs a man to fix it. You say it needs a better roof. You argue about it for eleven years, and fix it in the twelfth.",
                "?stay=2|* You come back every spring, and every autumn. Ilan always has the kettle on. You never stay longer than a week, and you never leave without promising the next time.",
                "?boy=1|* The boy you once fed at the roadside is a ferryman's apprentice now. He still waves.",
                "?boy=2|* The boy you took in has grown, and tinkers, and has a wagon. He says you taught him what a road is for.",
                "?solend=1|* Three Pines has a small house, a loud dog called Soup, and two people who argue about the roof. Some evenings you walk to the shrine together, and Ilan pretends she did not set a third cup.",
                "?solend=2|* Sol never stopped writing. The letters are in a box next to the lantern. Ilan has read all of them, and has opinions.",
                "?solend=3|* Sol's letters stopped, years ago. You still leave a candle in the window on the night you met. Some roads simply end.",
                "?!solend|?sol|* Sol is a name that comes up now and then in a guest book somewhere, in the same untidy handwriting. Still here, it says.",
                "* On a quiet evening you take the brass token off its cord and hold it up to the lantern. The seven-pointed star throws a small, wobbly shadow on the wall. Ilan holds out her hand, and you give it to her, and she studies it until the lantern gutters, and then she turns it over and shows you the back. There is a tiny scratch there. It is a number. It is a seven.",
                "@Ilan| There were seven of us. Seven houses, seven children, seven tokens. I had thought you were the only one left.");
        TaleOrphanSeasons.add(d);

        // ------------------------------------------------------------------ the road tales
        d.episode("bride", "The Ghost Bride").odds(25)
                .when((c, st) -> after(st, 2) && town(c))
                .intro(
                        "* The inn has one room left, and the innkeeper looks embarrassed when she says it.",
                        "@Innkeeper| It's the one at the end. It's perfectly good. It's just that, well, there's a bride in it.",
                        "* There is. She sits at the window in a red dress, very still, looking down at the road. She is also see-through. She does not seem to notice you at all until you drop your bag.",
                        "@Bride| Is he here? He said he would come before the lamps were lit.",
                        "@You| Who?",
                        "@Bride| My husband. He is only a little late. He is always a little late.",
                        "* The innkeeper, in the hall, whispers that she has been waiting for thirty years.")
                .outChoice(new Chapters.Choice("bride", "She waits for someone who is not coming.",
                        "Sit with her until dawn, and tell her he was held up.", "Leave her to her window and sleep in the stable.")
                        .after(
                                new String[]{"* You sit. You talk about roads, and weather, and a husband who has the worst timing in the kingdom. At first light she smiles at you, and the red dress is the color of the sky for a moment, and then the room is empty.", "* The innkeeper does not charge you for the night. She leaves a lantern at your door."},
                                new String[]{"* You sleep badly. In the morning the window is empty, and the innkeeper says she has not seen the bride go. She does not blame you. She has left the lamp lit anyway."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.episode("boy", "The Boy Who Followed").odds(25)
                .when((c, st) -> after(st, 2) && wild(c))
                .intro(
                        "* There is a small shadow behind you on the road. It has been there since the last bend.",
                        "* When you stop, it stops. When you look back, it is a boy of about nine, thin as a rake, with a bundle on his back and the look of someone who has decided not to be noticed.",
                        "@Boy| I'm not following you. I'm just going the same way.",
                        "@You| Where are you going?",
                        "@Boy| Wherever you are.",
                        "* You remember a tinker's wagon, and a man who looked at you in a storm and did not ask a single question.")
                .outChoice(new Chapters.Choice("boy", "A hungry child is walking behind you.",
                        "Feed him, and take him to the next town where someone will look after him.", "Let him come along and teach him the road.", "Give him your food and walk on alone.")
                        .after(
                                new String[]{"* You find him a family at the next village: a baker's wife with five children of her own and room in her heart for a sixth. He waves until you cannot see him."},
                                new String[]{"* He talks the whole way. He has opinions about birds. You didn't know you could be so tired and so glad at once."},
                                new String[]{"* He looks at the bread and at you, and then he nods, once, as if that was the answer he expected. You feel it for days."}))
;

        d.episode("sword", "The Swordsman at the Ford").odds(30)
                .when((c, st) -> after(st, 2) && wild(c) && c.age >= 19)
                .setup((c, st) -> Chapters.spawn(c, st, "kasumi"))
                .intro(
                        "* There is a woman standing in the ford with water up to her shins and a sword across her shoulders like a yoke.",
                        "@{kasumi.first}| I am {kasumi.first}. I have crossed this river forty times, and on every crossing I have fought whoever was on the other bank. It is a stupid habit. I do not wish to stop.",
                        "@You| I am only passing through.",
                        "@{kasumi.first}| Everyone is only passing through. That is why I like the ford.",
                        "* She does not draw. She waits. If you want to cross, you will have to take up her challenge: type 'attack' while you stand beside her.")
                .defeat("kasumi", "Cross the ford, and win the fight {kasumi} wants.")
                .outro(
                        "?dead.kasumi|* She is on the bank. The river goes on. It is not a good victory, and you know it.",
                        "?!dead.kasumi|* She sits on a rock, wringing out her sleeve, grinning like a child.",
                        "?!dead.kasumi|@{kasumi.first}| Forty-one. Now that was a fight. You stand like someone who learned on a wagon.",
                        "?!dead.kasumi|@You| I learned on a road.",
                        "?!dead.kasumi|@{kasumi.first}| Same thing. Here. Take this. It's the one thing I own that's worth more than my sword.",
                        "?!dead.kasumi|* She hands you a rolled slip of paper. It is a diagram, drawn in a quick hand, of how she holds her wrists when she cuts. You will use it for years.")
                .pay(0, 0, 0, 0, 0, 1);

        d.episode("caravan", "The Last Cargo").odds(30)
                .when((c, st) -> after(st, 2) && wild(c))
                .intro(
                        "* A caravan master sits propped against a wagon wheel by the roadside, alone, with a blanket over his knees and a clay pipe gone out in his hand. His wagons stand empty. His men have gone to town for help, and will not be back in time.",
                        "@Caravan master| You're a road-kid. I can tell by your walk. Come here. I haven't much breath.",
                        "@Caravan master| I've one cargo left, in that blue chest. A tea set. It's for a woman in {@deliver}. Thirty years I've promised it to her. I never did. Will you?",
                        "* He presses the chest into your arms. It is heavier than it looks. He is lighter than he should be.",
                        "@Caravan master| Tell her I'm sorry about the delay.",
                        "* He is gone before his men are back.")
                .at("deliver", "Deliver the blue chest to a woman in {@deliver}.")
                .outro(
                        "* The woman is old, and small, and sharp-tongued. She opens the chest and sees the tea set. She is quiet for a very long time.",
                        "@Old woman| He said he'd bring it the spring after we parted. He had a good reason, I'm sure. A war. A flood. A fool who couldn't read a map. Thirty years.",
                        "* She lifts one cup to the light. It is thin as an eggshell and as white as milk.",
                        "@Old woman| Stay and have tea with me. I have a good reason to use the cups, finally.",
                        "* You stay for the afternoon. She tells you everything about him, and you listen. It is a fine tea.")
                .pay(500, 0, 0, 0, 0, 0)
                .done((c, st) -> Chapters.set(st, "delivered", 1));

        d.episode("sol1", "A Stranger at the Well").odds(20)
                .when((c, st) -> after(st, 2) && c.age >= 19 && c.age <= 60)
                .setup((c, st) -> Chapters.spawn(c, st, "sol"))
                .intro(
                        "* The well in the village square has a long queue, and somebody at the front is arguing with the bucket.",
                        "* The somebody is a traveler about your age, with a patched coat and a laugh that carries. {sol.He} has dropped {sol.his} rope. {sol.He} has also dropped {sol.his} bucket. {sol.He} is now negotiating with the well.",
                        "@Sol| I know you can hear me. I only want a drink of water. I'm not asking for your hand in marriage.",
                        "* You lend them your rope. It is not a grand gesture. You will think about it for years.",
                        "@Sol| Sol. And you are?",
                        "* You tell them. They nod, solemnly, as if you had told them the name of a minor god.",
                        "@Sol| Well, {first}. We seem to be going the same direction. Shall we be going the same direction together, as far as the next village?")
                .done((c, st) -> { Chapters.set(st, "solw", GameDriver.time); Chapters.set(st, "sol", 1); })
                .pay(0, 0, 0, 0, 0, 0);

        d.episode("sol2", "The Second Autumn").odds(20)
                .when((c, st) -> st.seen.contains("sol1") && GameDriver.time - Chapters.flag(st, "solw") >= 52 * 3)
                .intro(
                        "* Four autumns have gone by since the well. You have seen Sol three times. Once in a flooded town, once at a funeral, once in a market where you both reached for the last peach.",
                        "* This time you meet on a hill above a river, at sunset, without having planned to. Sol is thinner. There is a scar on {sol.his} chin you do not remember.",
                        "@Sol| I thought about you. I won't pretend I didn't. I told myself it was silly. Two people who can't stay in one place, trying to stay near each other.",
                        "@You| It is a bit silly.",
                        "@Sol| It is very silly. I'd do it again.")
                .choice(new Chapters.Choice("solk", "Sol is standing very close, and is not joking now.",
                        "Kiss them.", "Take a step back, and say you aren't ready.")
                        .after(
                                new String[]{"* It is a short kiss, clumsy and warm. When it is over, Sol laughs, and then you do, and the whole river seems to laugh with you."},
                                new String[]{"@Sol| That's all right. I can wait. I'm very good at waiting. I've been practising on you.", "* {sol.He} squeezes your hand, once, and lets go. The hill smells of woodsmoke and dry grass."}))
                .done((c, st) -> Chapters.set(st, "solw", GameDriver.time));

        d.episode("sol3", "The Third Road").odds(20)
                .when((c, st) -> st.seen.contains("sol2") && GameDriver.time - Chapters.flag(st, "solw") >= 52 * 4)
                .intro(
                        "* Years later, a letter finds you through three inns and a post-rider. It is short, in Sol's untidy hand.",
                        "* 'I'm at the crossroads at Three Pines. I've got a small house and a smaller dog. There's room for a second person, if the second person ever got tired of the road. If you aren't ready, that's all right. I'll write again. Sol.'",
                        "* You read it a second time. You are not sure whether you are tired of the road. You are sure that you are tired of wondering.")
                .choice(new Chapters.Choice("solend", "A house with a dog is waiting at Three Pines.",
                        "Go to Sol, and stay.", "Write back that you love the road, and always will, and hope {sol.he} understands.", "Do not answer.")
                        .after(
                                new String[]{"* The dog is called Soup. It has one ear up, one ear down, and an opinion about you from the first moment. Sol is on the step with a mug of tea, pretending to be calm.", "@Sol| You took your time.", "@You| So did you."},
                                new String[]{"* The reply comes in a month. 'I knew you'd say that. I'll keep the lamp on anyway. Sol.'"},
                                new String[]{"* You keep the letter in your coat for years. You never answer, and you never throw it away."}))
;

        d.episode("ferry", "The Ferryman").odds(12)
                .when((c, st) -> after(st, 2) && wild(c))
                .intro(
                        "* The ferry across the Grey River is a flat boat with a creaky rope. The ferryman is a short, broad man who talks to the river more than to his passengers.",
                        "* You pay your coin. He glances at your wrist, where the cord with the token has slipped out of your sleeve, and stops with the rope in his hands.",
                        "@Ferryman| That mark. Where did you get that mark?",
                        "@You| It's been with me all my life.",
                        "@Ferryman| Twenty years ago, in a winter storm, I carried a woman across this river at midnight. Nobody else would. She had that mark on a cord at her neck. She had a cut on her arm, and she was running from something.",
                        "@Ferryman| I asked her name. She wouldn't give it. She gave me a coin as big as my palm, and said that if anyone ever came asking, I should say nothing. I said nothing. But you're not asking, are you? You're just a traveler with a token. I think it's all right.",
                        "* You thank him. Your hands are not steady on the rope.")
                .done((c, st) -> mark(st, "f_ferry"))
                .pay(0, 0, 0, 0, 0, 0);

        d.episode("seam", "The Seamstress").odds(12)
                .when((c, st) -> after(st, 2) && town(c))
                .intro(
                        "* The shop has a bolt of blue wool in the window and a woman behind the counter with pins in her mouth.",
                        "* You came for a patch. You get a stare.",
                        "@Seamstress| Where did you get that?",
                        "* She is looking at the token, which has slipped out again. You are going to have to find a better cord.",
                        "@Seamstress| My mother stitched that star onto uniforms when I was a girl. A hundred of them, one for every guard at the House. The House of the Seventh Star. It was a good house. It's gone now.",
                        "@You| Gone how?",
                        "@Seamstress| Fire, they say. Winter. I was too small to remember properly. But I remember the star. Seven points, one for each of the seven oldest families who swore to keep the peace. It's an old, old mark.",
                        "* She sews on your patch for free. She does not let you pay.")
                .done((c, st) -> mark(st, "f_seam"));

        d.episode("soldier", "The Soldier Under the Pine").odds(12)
                .when((c, st) -> after(st, 2) && wild(c))
                .intro(
                        "* There is a man lying under a pine tree at the edge of the road. He is very still. He is still breathing.",
                        "* His coat is torn, and underneath it, on a cord, is a mark you know.",
                        "@Soldier| Where did you...? Never mind. Sit. Please. I'd like to talk to someone before I stop.",
                        "@Soldier| I was a guard at the House of the Seventh Star. We were told it was a peaceful winter. It wasn't. There was a coup in the dark of the night. Torches in every window. My lord was dead before the second bell.",
                        "@Soldier| But the lady got out. Lady Ilan. She had a baby in her arms and a cut on her arm, and she ran into the storm. I held the gate for her. I held it as long as I could.",
                        "@Soldier| I've wondered every year what became of the child.",
                        "* He asks no questions. He looks at your face until he has finished, and he smiles, and then he closes his eyes.",
                        "* You bury him under the pine. It takes all afternoon.")
                .done((c, st) -> mark(st, "f_soldier"));

        d.episode("archive", "The Archivist").odds(12)
                .when((c, st) -> after(st, 2) && town(c))
                .intro(
                        "* The library of the city is quiet and smells of dust and tea. The archivist is a skinny man with spectacles, and a habit of talking to books.",
                        "* You ask about the star. He puts down his pen.",
                        "@Archivist| The House of the Seventh Star! Oh, you've picked a sad one. Seven oldest families, sworn to keep the border peace. A noble lord, Rennick. A lady, Ilan, from the south. One child, born in the last month of the last year of the war, lost in the winter coup.",
                        "@Archivist| The records say the child died. But the records are written by the people who won.",
                        "@Archivist| There's an old story that the lady lit a lantern at every crossroads she passed, to tell a lost child which way to come. Most people think it's only a story. But there's a shrine on the Ash Road, you see, where a lantern never goes out.",
                        "* You walk out of the library without remembering to say thank you.")
                .done((c, st) -> mark(st, "f_archive"));

        d.episode("maid", "The Old Maid").odds(12)
                .when((c, st) -> after(st, 2) && wild(c))
                .intro(
                        "* There is a very old woman selling pickled plums from a basket on the roadside. She has no teeth and a great deal to say.",
                        "@Old maid| Now, a plum like this takes eleven days in the jar...",
                        "* She stops. She is looking at your token.",
                        "@Old maid| I was a maid in that house. I was twelve years old. I carried the lady's slippers.",
                        "@Old maid| She isn't dead, you know. People say so, but she's alive. I see her light, some nights, the shrine at the crossroads on the Ash Road, a day's walk from here. She sits at the window and she watches the road.",
                        "* She gives you a handful of plums, and a look you will remember for the rest of your life.")
                .done((c, st) -> mark(st, "f_maid"));

        d.episode("boats", "Paper Boats").odds(30)
                .when((c, st) -> after(st, 2) && town(c))
                .intro(
                        "* On the last night of autumn the whole town goes down to the river with paper boats, and a candle in each boat, and a name in each candle.",
                        "* A child hands you a boat. It is a poor one, folded crooked. There is a stub of candle in it.",
                        "@Child| You have to think of someone you lost. It doesn't matter who. They'll find it.")
                .choice(new Chapters.Choice("boats", "You hold the little boat, and the river is dark.",
                        "Think of Old Brannoch.", "Think of the woman who gave you away.", "Think of nobody, and just watch the lights.")
                        .after(
                                new String[]{"* You say his name under your breath. The boat tips, rights itself, and goes bobbing away, ridiculous and brave."},
                                new String[]{"* You have neither her name nor her face. You think of a storm, and a cut arm, and a coat. The boat floats a long way before it goes out."},
                                new String[]{"* You watch two hundred small lights drift away down the river, and for a little while you are part of something."}));

        d.episode("thief", "The Thief Who Wasn't").odds(30)
                .when((c, st) -> after(st, 2) && town(c))
                .intro(
                        "* You feel the tug at your belt. You have had your pocket picked by professionals. This is not one.",
                        "* The hand is thick and slow, and belongs to a bald man of seventy with a medal pinned to his coat. You catch his wrist. He looks mortified.",
                        "@Old man| I'm sorry. I'm so sorry. I haven't done this since the war. It's only that my pension was late, and I haven't eaten since Tuesday.",
                        "@You| You were a soldier?",
                        "@Old man| General, actually. Third Army. I'm not ashamed. Well. I'm a little ashamed.",
                        "* You buy him a bowl of noodles. Two. He tells you about the Battle of the Reed Pass for the whole of both bowls.")
                .pay(0, 0, 0, 0, 0, 0);

        d.episode("rain", "Three Days of Rain").odds(30)
                .when((c, st) -> after(st, 2) && town(c))
                .intro(
                        "* It rains for three days, and you are stuck in an inn with eleven strangers, a leaking roof, and a fire.",
                        "* On the second night, a thin woman with a ladle for a walking stick offers to tell a story. Nobody argues.",
                        "@Storyteller| Once there was a road that wanted to be a river...",
                        "* It is a long story, with a talking carp in it, and a king who has lost his left sandal, and a girl who walks to the end of the world to find out whether it has an edge.",
                        "* When it ends, nobody speaks. Then somebody says it was too short. The storyteller smiles, and begins again.")
                .pay(0, 0, 0, 0, 0, 0);
    }
}
