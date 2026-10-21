package Game;

/** The Lucky Fool (see "Story Plan.md"). The player has no talent and enormous luck; every chapter is a perilous mess the player walks out of by accident, and a misunderstanding grows
 *  (the Society of the Fortunate Hat thinks the player is a Hidden Master). Underneath is a small Luck Spirit, assigned to the player by mistake, who retires; the last peril has no luck
 *  in it, and the player's friends are what saves them. Choices: playing along with the cult (wronghat) and meeting Master Gu (gu). */
final class TaleLucky {
    private TaleLucky() { }

    /** Whether this human has read this place of the Palace's story (so a story may notice what they know). */
    static int read(Character c, String site) { Lore.PState p = Lore.pstate(c); return p != null && p.found.contains(site) ? 1 : 0; }

    static void register() {
        Chapters.Def d = Chapters.def("lucky");
        d.season(1, "The Wrong Hat");
        d.flags("sawshed");
        d.cast("pip", "Pip", "Tallow").he().keep().trait(Character.Traits.Friendly).side("Good").lvl(4).age(19).start().rel(3);
        d.cast("gu", "Master", "Gu").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(12).pot(8).age(60).at("far").rel(0);

        d.chapter("pip", "Everyone Agrees")
                .intro(
                        "* Everyone agrees that you have no talent. The sect elders said so, politely, when you were tested. The tester said so, less politely, when he had finished laughing. Your own mother said so, with great affection, while knitting you a scarf.",
                        "* Everyone also agrees that things keep going your way.",
                        "* You found a purse on the road, twice. You missed a boat that sank. You were mistaken for somebody else at an inn, and the somebody else was owed a free supper. The roof fell in the day after you moved out of the room under it.",
                        "* There is exactly one person who explains this properly. He is Pip Tallow, your best friend, and he is waiting at the gate of Originis with a basket of food and an expression of enormous good cheer.",
                        "@Pip| It's the luck, you see. I've worked it out. It follows you around like a puppy. You never have to do anything. You just have to be there.",
                        "@You| That isn't a skill.",
                        "@Pip| It's the best skill. I've known you for eleven years and you have never had so much as a cold.",
                        "@Pip| But even luck needs friends. Luck on its own is a bit sad. Let's get you some people.")
                .friends(3, "Collect friends the way you collect luck: three of them.")
                .outro(
                        "* You are not sure how it happened. You went to buy a loaf of bread and came back with a retired sailor, a nervous apprentice, and a woman who had mistaken you for her cousin and was now too embarrassed to say so.",
                        "* The five of you sit on a wall and eat the bread. It is, you think, the nicest day you can remember.",
                        "@Pip| See? I told you. It's easy.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("hat", "The Wrong Hat").gap(20)
                .intro(
                        "* You find a hat on the road. It is a rather good hat, tall, black, with a silver cloud on the brim. You put it on, because it would be rude not to.",
                        "* An hour later you are surrounded by thirty people in identical hats, all of them kneeling.",
                        "@Leader| Master! At last! The Hat of the Hidden Heavens has come to us! We have waited for you for nine hundred years!",
                        "@You| I'm just a person who found a hat.",
                        "@Leader| Exactly what a Hidden Master would say.",
                        "* You have been mistaken for something. It is very big. It has banners.")
                .choice(new Chapters.Choice("wronghat", "Thirty people in hats are waiting for your first command.",
                        "Play along. It seems less risky.", "Confess everything, immediately.")
                        .after(
                                new String[]{"@You| ... Arise.", "* They arise. They cheer. It is, you realise, the most thrilling thing that has ever happened to you, and also very possibly a mistake."},
                                new String[]{"@You| I'm not a master. I'm a fool who found a hat.", "@Leader| A master who claims to be a fool! The wisdom!", "* You try to explain it three more times. By the end they have made it a hymn."}))
                .task("KILLS", 5, "", "", "Keep them off your back: five kills (by accident, probably).")
                .outro(
                        "* The Hat people have enemies. This was not in the leaflet. A rival sect called the Order of the Bare Head shows up on the third night, with sharp things and a lot of opinions about hats.",
                        "* What happens next is described by Pip, in three different tellings, as 'a masterclass in indirect combat', 'the greatest thing I have ever seen', and 'a series of coincidences so improbable that they have to be deliberate'.",
                        "* Here is what you remember. You tripped over a tent peg. A man fell on his own sword. A loose beam came down on two more. The last two ran into each other chasing the same lantern. Five of them, by the end of the week, were no longer in a position to follow you.",
                        "@Pip| You see?",
                        "@You| I tripped.",
                        "@Pip| That's what a Hidden Master would say.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("walkover", "The Walkover").gap(20)
                .intro(
                        "* You enter the Spring Tournament on a dare. It is a small tournament, in a small town. Sixteen fighters. You are the sixteenth, and everyone knows you are there because someone forgot to take your name off the list.",
                        "* The night before the first round, you bring a cheap pot of soup to the contenders' dormitory. It seems polite. You do not know the soup has gone off.",
                        "* The next morning, fifteen people are very poorly, and you are standing at the arena gate wondering where everyone is.")
                .realm(1, "Pass the first gate against all odds: reach Major Lvl 1.")
                .outro(
                        "* The referee reads out the results. You win by default. You also win the second round by default. The final is against a man who is currently being sick into a bucket, and he forfeits with enormous dignity.",
                        "* You are handed a trophy. It is gold. It is shaped like a dragon. It is far too heavy for you to lift, and three people have to carry it back to the inn.",
                        "@Pip| The papers are saying you're a tactical genius. That you poisoned them on purpose.",
                        "@You| It was just soup.",
                        "@Pip| That's the genius of it.")
                .pay(0, 0, 0, 2, 0, 0);

        d.chapter("gu", "The Challenger").gap(52).major(1)
                .setup((c, st) -> Chapters.spawn(c, st, "gu"))
                .intro(
                        "* The Society of the Fortunate Hat has a newsletter now. It has eight hundred readers, and a regular column in which Pip, who is its editor, describes your latest miracle. You have never read it. You are afraid of it.",
                        "* One of the readers is a real master. Master Gu of the Iron Wind school has read the whole thing. He has come to see if it's true.",
                        "@Gu| I have heard you defeated the Order of the Bare Head with a tent peg. I have heard you won a tournament by diplomacy. I have heard you are the last Hidden Master of the Hat.",
                        "@Gu| I do not believe it. I am coming to test you.")
                .meet("gu", "Master Gu of the Iron Wind school wants to meet the Hidden Master.")
                .outro(
                        "* Master Gu is sixty, hard as a hammer, and cold as a well. He has a staff and no sense of humour. He circles you. You smile nervously. You back into a washing line.",
                        "* What happens next takes less than a minute. Master Gu steps on a bar of soap that a child left on the path. He goes up like a rocket, comes down in the fish pond, and sits there, blinking.",
                        "* The Society of the Fortunate Hat applauds. Pip, in the front row, wipes his eyes.",
                        "@Gu| ...I have not been so thoroughly beaten since I was eleven.",
                        "* He climbs out, dripping, and bows. It is a real bow. It is deep and slow.")
                .outChoice(new Chapters.Choice("gu", "Master Gu is standing wet and bowing to you.",
                        "Confess it was an accident.", "Accept the bow with great dignity.")
                        .after(
                                new String[]{"@Gu| You are honest, too. Even better. I shall come and visit you.", "* He does. Every year. He brings fish."},
                                new String[]{"@Gu| Hidden Master. I shall tell no one.", "* He tells everyone. The newsletter has to print a second edition."}))
                .pay(500, 0, 0, 0, 0, 1);

        d.chapter("spirit", "Mrs. Fortune").gap(52)
                .setup((c, st) -> Chapters.set(st, "sawshed", read(c, "shed")))
                .intro(
                        "* You have a small purse. It is old, and a bit worn, and you have had it for eleven years. You have never opened the inner pocket. It is sewn shut, and it has always been warm.",
                        "* On a rainy night in an empty inn, something clears its throat in your sleeve.",
                        "* It is a very small woman, about the size of a teaspoon, in a neat grey dress and an immense pair of spectacles. She is sitting on the edge of your purse, tapping a clipboard.",
                        "@Mrs. Fortune| I am Mrs. Fortune. I am a Fortune Attendant, Grade Two. I have been assigned to you for eleven years.",
                        "@You| Assigned?",
                        "@Mrs. Fortune| There was a clerical error. I was meant for a great general named Zhu Bao. He was in the next village. You have the same birthday. A senior clerk transposed the digits, and I was never able to correct it.",
                        "@Mrs. Fortune| I work for the Heavens, you understand. The Department of Seeds and Weather. Very junior. Somebody up there keeps books on every one of you, and they are surprisingly good books, except for mine.",
                        "?sawshed|* You think of a long low shed in a wood, and of shears worn at the tips. You decide not to ask what else is kept in the Department.",
                        "@Mrs. Fortune| I tried to leave several times. You kept being so frightfully endearing.")
                .when((c, st) -> c.majorLvl >= 1)
                .outro(
                        "@Mrs. Fortune| I am retiring, you see. Today. The paperwork has finally come through. It was meant to happen ten years ago, but nobody could find the right form.",
                        "@Mrs. Fortune| I've had a lovely time. You are the least talented person I've ever followed, and the happiest. I used to think that was my doing. I've changed my mind. I think it's you.",
                        "@Mrs. Fortune| I do hope you'll be all right. You will, I think. You've got Pip.",
                        "* She tucks the clipboard under her arm. She straightens her spectacles. Then she goes out like a candle, in a small and surprisingly dignified puff of gold.",
                        "* The purse is cold. It has not been cold in eleven years.")
                .task("WAIT", 2, "", "", "")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("nofortune", "No Luck at All").years(1)
                .intro(
                        "* The first week without Mrs. Fortune is odd. You drop your bowl. You step in a puddle. You miss a boat, and the boat does not sink.",
                        "* The second week, a landslide closes the road and you have to take the long way around. You are chased by a swarm of wasps. You get a splinter.",
                        "* You are beginning to understand that this is what life is like for everyone else.",
                        "@Pip| It's all right. I've got you. We've all got you.")
                .realm(2, "Keep going without luck: reach Major Lvl 2.")
                .outro(
                        "* The road to Major 2 goes through a mountain pass in the autumn. It is not a lucky pass. A rockslide closes the way behind you. A storm closes it ahead.",
                        "* You are halfway up a cliff, with a bad ankle and a worse feeling, when the rope you are holding frays. You close your eyes. There is no puff of gold. There is no tiny voice. There is only the wind, and the long way down.",
                        "* A hand grabs your wrist.",
                        "* It is Pip. Behind him is the retired sailor, with a coil of rope. Behind him are the nervous apprentice, the woman who thought you were her cousin, and, further down, twenty people in tall black hats, passing a lantern from hand to hand up the mountain.",
                        "@Pip| You didn't think we'd let you do this alone? What kind of Society would we be?",
                        "* They pull you up. It takes six of them. There is a lot of swearing, and a great deal of laughter.",
                        "* On the ledge, with your face in the snow and a cup of hot tea in your hand, you realise something very simple. It was never the luck. It was always them. Luck was just the thing that put you in the same place as the people who would keep you alive.")
                .pay(2000, 0, 0, 2, 2, 1);

        d.interlude(1, "Warm Again",
                "* Everyone agrees you have no talent. Everyone also agrees that you are the happiest person they know.",
                "?wronghat=1|* The Society of the Fortunate Hat has two thousand members now. They still think you are a Hidden Master. You have stopped correcting them. It is easier, and there is a lovely tea service.",
                "?wronghat=2|* The Society knows you are not a Hidden Master. They have kept the hats anyway. They say the hat is the point. They may be right.",
                "?gu=1|* Master Gu visits every year with fish. He sits by the pond where he fell in, and tells you about his students. He is, at heart, a lonely man who is good with a stick.",
                "?gu=2|* Master Gu has told everyone you are a Hidden Master. You have given up trying to stop him. He is in the third edition of the newsletter.",
                "* Every so often, in a quiet moment, you open the inner pocket of your purse. It is empty. It is warm again, just a little.",
                "* You have stopped believing in luck. You believe in a lantern passed from hand to hand, up a mountain, in the dark.",
                "* Far above you, at a desk in a long low shed, someone puts a form in the wrong tray. It is a very small mistake. It will take a very long time to find.");
        TaleLuckySeasons.add(d);
    }
}
