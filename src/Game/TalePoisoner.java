package Game;

/** The Apothecary's Second Cup (2026-10-20; new start, three seasons). The human is the apprentice of Madam Verrel, an apothecary in the harbour town of Brinewick who cures by day and fills the other orders by night,
 *  and the inspector Calla Ferrow (the other gender than the human's) has begun to drop in. Season 1, "The Dose Makes the Poison" (dark comedy, deadpan): the three questions before any sale, the honest widow, the Dove,
 *  the death in the fish market, the ledger, Verrel's last order, and a second cup of tea with the inspector. Choices: widow (1 counsel, 2 a sleeping draught, 3 the poison), ledger (1 burn, 2 keep, 3 sell),
 *  dove (1 expose, 2 partner), cup (1 antidote at hand, 2 trust, 3 refuse), love. Seasons 2 and 3 are in TalePoisonerSeasons. */
final class TalePoisoner {
    private TalePoisoner() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void register() {
        Chapters.Def d = Chapters.def("poisoner");
        d.season(1, "The Dose Makes the Poison");
        d.flags("craft0", "widow", "ledger", "dove", "cup", "love");
        d.place("harbour", "wildnear").place("tower", "far");
        d.cast("verrel", "Madam", "Verrel").she().keep().trait(Character.Traits.Alchemist).side("Neutral").lvl(9).pot(6).age(66).at("place:harbour").rel(3);
        d.cast("pim", "Pim", "Totterdown").he().keep().trait(Character.Traits.Friendly).side("Good").lvl(1).age(20).at("place:harbour").rel(3);
        d.cast("calla", "Calla", "Ferrow").opposite("Cael").keep().trait(Character.Traits.Scribe).side("Good").lvl(5).age(31).at("place:harbour").rel(0);
        d.cast("dove", "Mister", "Dove").he().keep().trait(Character.Traits.Poisoner).side("Evil").lvl(8).pot(6).age(47).at("far").rel(-1);

        d.chapter("shop", "Shop Hours")
                .setup((c, st) -> Chapters.spawn(c, st, "verrel"))
                .intro(
                        "* Madam Verrel's apothecary stands at the bottom of Salt Lane in Brinewick, between a net-mender and a shop that sells nothing but string. It has three signs. The one over the door says VERREL, APOTHECARY. The one in the window says CURES. The third has no sign. It is a ledger, kept in the back room, in a green leather binding, under a loose board.",
                        "* You were apprenticed to her at fourteen, for a fee your mother never explained. You are old enough now to know what the fee was for. Verrel has never said it aloud. She has trained you for six years, in the front shop, to cure, and in the back shop, to the other thing, with the same patient hands.")
                .at("harbour", "Go to Verrel's Apothecary in Salt Lane, {@harbour}.")
                .outro(
                        "* The shop smells of camphor, honey and cold tea. A cat sleeps on the scales. Madam Verrel is sixty-six, plump, pink and white, in a lace cap, with the face of a woman who knits for orphans. She is shelling peas into a bowl when you come in, and she does not look up.",
                        "@Verrel| You're late. There's a gentleman in the back with a cough, and a lady at the door with a rat. Take the cough. I'll take the rat.",
                        "@You| Which of them is the rat?",
                        "@Verrel| That's the lesson, dear. Three questions before every sale. Who is it for. What do they think it does. What happens if they are wrong. A customer who answers all three honestly is a customer. A customer who answers one is a client. A customer who answers none of them is a policeman, or a fool, and it's a coin toss which is worse.",
                        "* She pushes the bowl of peas toward you. They are fresh and sweet and entirely innocent. You eat three, because she is watching, and because you are fairly sure that is also a test.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("questions", "Three Questions").gap(8)
                .setup(TalePoisoner::mark)
                .intro(
                        "* The bell over the door goes at nine. It is a thin nervous man in a good coat who says he wants something for the rats. He has one hand in his pocket. He stands too straight. In the shop there are eleven kinds of rat poison, three of which are honest.",
                        "@Verrel| He's yours. I've a cold coming. Ask him nicely.")
                .made(5, "Mind the shop: mix five items.")
                .outro(
                        "* You ask the three questions. Who is it for? The rats. What do they think it does? Kills the rats. What happens if they are wrong? He blinks. He says there will be more rats. He says it with a straight face, and you thank him, and sell him the honest kind, in a paper twist, with a lecture on ventilation.",
                        "* He pays and goes. At the door he stops, and half turns, and says in a low voice that his wife's mother has been staying with them since Easter. Verrel, at the back, sneezes, and looks at the ceiling. You put the honest rat poison in a different paper, and then, because it seems the least you can do, you add a free packet of lavender.",
                        "@Verrel| Good. You asked. You didn't judge. You can always judge later, with a better view. Do you know what the trouble is with a murderer, dear?",
                        "@You| They're hard to catch?",
                        "@Verrel| They're so seldom the ones who ask for poison. The ones who ask for poison are nearly always the ones who haven't thought of anything else. Come and have some tea.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("widow", "The Honest Widow").gap(24)
                .intro(
                        "* The widow comes on a wet afternoon in the fourth week of Lent. She is perhaps fifty, in black that does not suit her, with an umbrella she has forgotten to put down. She stands in the middle of the shop dripping on the floorboards and says that she has come to buy a poison. She says it very clearly. She says that she has never in her life done anything like it. She says her husband is not dead, but he is going to be.")
                .wait(8)
                .outro(
                        "* You put the three questions to her. She answers all of them. She does not weep. It is for her husband, she says. She thinks it will kill him painlessly in about a week. She understands that if she is wrong he may linger, and she has made her peace with that. He has beaten her for twenty-two years and last Tuesday he broke the arm of her youngest son.",
                        "* Verrel, at the back, is standing quite still, with a pestle in her hand. She does not move. She looks at you across the length of the shop, as she looked at you the first day, as at a pea she has not yet decided whether to eat.",
                        "@Widow| I know what I'm asking. I'd like you to say it's wrong, if you think so. I'd rather hear it from someone who knows how.")
                .outChoice(new Chapters.Choice("widow", "A woman with a broken son wants a poison for her husband, and has answered all three questions honestly.",
                        "Refuse. Take her to the magistrate and the harbour doctor, and keep her out of it.", "Sell her a sleeping draught: a week of peace, and a lie, and time.", "Sell her the poison. It is hers to decide.")
                        .after(
                                new String[]{"* You take her to the harbour doctor. It costs you three days and a favour you owe from the spring. It does not end well, quite, and it does not end badly. The husband is sent away to a sect cure house, and the widow comes by on Sundays with a loaf and her son's report from school. She never mentions it. She sits at the back of the shop and shells peas.", "@Verrel| That was very well done. And not remotely what I would have done. I think I'll keep you."},
                                new String[]{"* You give her a sleeping draught, strong and clean, and tell her it will do exactly what she wants. It will give her a week. In the week the son's arm is set, and the neighbour who owes the husband money pays him a visit, and the husband wakes up, on the Wednesday, to find that the house is somebody else's. He leaves town. Nobody knows quite how it happened.", "@Verrel| I shall note that you've the instinct of a diplomat. It is a dreadful quality. Never lose it."},
                                new String[]{"* You sell her the poison. It is three drops in a cup of tea, and she takes it away in a paper twist, with her umbrella, and goes out into the rain. You tell yourself that it was her right, and that it was the kind of decision a person ought to be allowed to make. You wash the pestle three times.", "@Verrel| Mmm. Well. You asked. You didn't judge. Sit down and have a cup of tea. And then, dear, I think you'll want to go for a very long walk."}))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("inspector", "The Inspector Browses").gap(32)
                .setup((c, st) -> Chapters.spawn(c, st, "calla"))
                .intro(
                        "* The bell goes at noon, and a tall person in a plain grey coat steps in out of the sun. They carry a notebook, a pencil, and a polite expression that does not for one moment reach the eyes. They say they are Inspector Ferrow of the Brinewick Magistracy. They say it is a courtesy call.",
                        "@Verrel| A courtesy! How nice. We've a little tea. Pim, the good cups.")
                .meet("calla", "Receive Inspector Calla Ferrow in the shop.")
                .outro(
                        "* {calla.He} stays for an hour. {calla.He} asks very nicely about the stock. {calla.He} inquires what you keep for the hiccups, and for a bad tooth, and for the ordinary kind of grief. {calla.He} writes the answers in a small neat hand. {calla.He} is about thirty, with clever tired eyes and a way of looking at the labels on the jars as if they were a letter in a language {calla.he} had learned in secret.",
                        "@Calla| You have an excellent supply of foxglove. It's one of my favourites. It does the most remarkable things for the heart.",
                        "@Verrel| Oh, it's a wonderful plant. We grow it ourselves.",
                        "@Calla| In a pot by the back door. Yes, I noticed. It looks very healthy. It's half a pot, I think. I wonder what happened to the other half.",
                        "* There is a short pause. The cat opens one eye. Verrel pours more tea. The Inspector takes the cup, and holds it, and looks into it, and says in a pleasant voice that the tea is a delicious colour. {calla.He} does not drink it. {calla.He} thanks you for it, very warmly, and goes out into the sun.",
                        "@Verrel| That is the cleverest person I have met in thirty years. We shall have to be extremely nice to them.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("pim", "Pim Totterdown").gap(32)
                .intro(
                        "* A boy comes to the back door on a Sunday with a basket of eels and a cough. He is twenty and looks fourteen. He has a round cheerful face, a patched coat, and the shoulders of someone who has been carrying things for people since he could stand. He wants to know if the shop needs a hand.",
                        "@Verrel| We do not. We do not need anything at all. We are quite full. Take him in, dear, and give him a bun.")
                .friends(2, "Take on a shop boy and find a second friend.")
                .outro(
                        "* Pim learns the front shop in a week. He cannot learn the back, because the door to it is locked and he does not ask. He sweeps. He fills jars. He remembers the name of every customer's dog. He is the only person who has ever used the word 'apothecary' as though it were a compliment.",
                        "@Pim| It's so clean. It's the cleanest shop in Salt Lane. Everyone says Madam Verrel's the kindest soul in the harbour.",
                        "@You| She is.",
                        "@Pim| I know! That's what I said!",
                        "* You put a second lock on the back-room door that evening. You do not tell Verrel. She notices it the next morning, and tests it with a finger, and gives you a look of such complete and gentle approval that you go red to the ears.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("dove", "Mister Dove").gap(48)
                .setup((c, st) -> Chapters.spawn(c, st, "dove"))
                .intro(
                        "* There is another shop in Brinewick, at the top of the town, called Dove's. It has a bright blue door and a window of pretty bottles. Its owner, a neat sandy man with a very soft voice, sells to anyone with the coin. He does not ask any questions. He does not need to. In the last two months, nine people in the harbour have died of an illness the doctor cannot name.",
                        "@Verrel| That is not my work. I should like that known. I do not poison the innocent, and I do not poison in numbers. It is untidy.")
                .meet("dove", "Call on Mister Dove at the top of the town.")
                .outro(
                        "* The shop is full of morning light. Dove receives you in an apron the colour of a robin's egg. He offers tea, and cake, and a free sample. He says he has long admired Verrel, and that it is such a pity she is so old-fashioned. He says business is booming. He says people don't want to be asked questions any more.",
                        "@Dove| It's the questions that kill a trade, you know. I used to ask. I found the sales dropped. And what is a question, after all, but an accusation with a hat on?",
                        "* He lifts the lid of a cake stand. There is a plum tart under it, glossy and perfect. You look at the tart. Dove looks at you. 'Go on,' he says. 'It's only a tart.' You decline. He smiles as at a good joke, and wraps it up anyway, and presses it into your hands at the door.",
                        "* You carry it home and put it on the shelf. The cat will not go near it.")
                .outChoice(new Chapters.Choice("dove", "Mister Dove sells to anyone, asks nothing, and nine people have died.",
                        "Tell Inspector Ferrow what you know.", "Say nothing, and keep a careful eye on his customers.", "Go back, and take him up on the partnership he is about to offer.")
                        .after(
                                new String[]{"* You send a note to the Inspector, unsigned, in a hand that is not yours. Three days later a plain-clothes constable is seen buying cake at Dove's blue door. You are not sure how you feel about it. You tell yourself it is a civic duty, and then, a little later, that it is a kind of competition.", "@Verrel| Reporting a rival to the law! How very modern of you."},
                                new String[]{"* You say nothing. You do, however, start a second small ledger, in your own hand, of everyone who goes in at the blue door. It is a short list, at first. By the end of the month it has twenty names. You keep it under the loose board, beside the green one, where it feels at home.", "@Verrel| Mm. Keeping your own counsel. That is the beginning of every ledger there ever was."},
                                new String[]{"* You go back on the Thursday. Dove is expecting you. He has a second cup ready, and a contract. It is generous. It is neatly drawn. It says nothing at all about what is to be sold, only to whom, and how much, and what is not to be asked. You leave the contract unsigned, though you take the cake.", "@Dove| You'll think about it. Everyone does. It's so very comfortable."}))
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("fish", "A Death in the Fish Market").gap(32)
                .intro(
                        "* The man is found on a Tuesday at six in the morning, face down in a barrel of herring. He is a dealer in nets, well liked, forty years old. The doctor puts it down to a seizure. A porter says, in a flat voice, to nobody, that he smelled almonds on the man's breath.",
                        "* By ten the whole of Salt Lane knows that the man had an argument with Madam Verrel last Friday over the price of a rope. By noon the story has grown a bottle with a label. By two a small crowd is standing outside the shop with their hands in their pockets, saying nothing, which in Brinewick is considered a demonstration.")
                .wait(8)
                .outro(
                        "* Inspector Ferrow arrives at three, alone, in a plain coat. {calla.He} goes through the front shop with a lamp. {calla.He} goes through the back, which you have locked, and then unlocked, and then, with great reluctance, left open. {calla.He} finds nothing. The jars are labelled, and the labels are true, and every poison in the room is the kind a good apothecary might keep for the rats.",
                        "@Calla| This was not your work. I know it was not. The man was killed with something sweet, which you do not stock. The dose was too large and the delivery was careless. The one who did it wanted him dead, and also wanted it to look as if it had been done by a person like you.",
                        "@Verrel| How very thoughtful of them.",
                        "@Calla| I would like to say, Madam, that I am not certain that you did not do it. I would like to say that I am certain you did not do this. I am not sure what a person ought to do with that.",
                        "* {calla.He} leaves a card. On the back, in pencil, in a neat small hand: If you remember anything, I would rather you told me than that I found it out. It is the first sentence of a sort of friendship, or of a very long game, and you cannot tell which.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("ledger", "The Green Ledger").gap(24)
                .intro(
                        "* Verrel takes the ledger out from under the board on a Sunday evening and lays it on the counter between you. It is a fat green book with a brass clasp. She puts her glasses on and turns the pages one by one, with a face like a woman reading the last letters of a long correspondence. There are eleven years of names in it. There are councillors, a sect elder, two doctors, a bishop's niece, the daughter of the harbour master, and one that she turns past quickly.",
                        "@Verrel| This is what we are, dear. Not the shop. The shop is a way to afford the ledger. These are the people who owe me. Each of them came with a reason, and each of them was right, in his way, and each of them knew that I knew. Do you understand what that is worth?")
                .wait(4)
                .outro(
                        "@Verrel| I am sixty-six. The Inspector is a clever person, and a patient one, and I am afraid I am going to have to be arrested. Dove was the third such, you see. There was a Mister Sprott before him, and a woman called Gently. They always try the same thing. They try to put the death on me, because I'm the one who is easy to put it on.",
                        "@You| What do you want me to do?",
                        "@Verrel| I want you to take the ledger, and decide. It's your shop. If I am taken, you will have it all: the names, the habits, the debts. Everything people were afraid to say. I would like you to use it better than I did. I'm not sure I did.",
                        "* She closes it, and pushes it across the counter, and takes off her glasses, and rubs the bridge of her nose with two fingers. For the first time in six years, she looks her age.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("last", "Verrel's Last Order").gap(16)
                .intro(
                        "* They come for her on a Thursday, at the end of the afternoon, two constables and a clerk. She has been expecting them since eleven. She has changed her cap. She has made tea. She sets a plate of shortbread on the counter, and says, in the voice she uses for rats and widows, that she would like to take a cup with the Inspector before they go. She would like it said that she was no trouble.")
                .meet("calla", "Stand with Madam Verrel when the Inspector comes.")
                .outChoice(new Chapters.Choice("ledger", "Verrel has put the green ledger in your hands.",
                        "Burn it. Let the names go, and let the people go with them.", "Keep it. A shop with a ledger is a shop that is safe.", "Sell it. The Dove's money would pay for a great deal.")
                        .after(
                                new String[]{"* You burn it in the shop stove that night, page by page, while the cat watches. It takes two hours. The smoke is green, and smells of old paper and a very faint burned almond. In the morning the stove is cold, and you feel lighter and poorer and quite sure that you will regret it.", "@Verrel| (from the Tower, in a note) Good. Now they will have to learn to be good with no help from me. I always suspected it might be possible."},
                                new String[]{"* You put the ledger under the board, and the board under the counter, and the counter against the wall. Then you open the shop on Friday as if nothing had happened. It is the same shop. It is not quite, you realise, the same. A bell rings, and a customer enters, and you notice you are counting how much each of them owes.", "@Verrel| (from the Tower, in a note) Don't use it more than you must. It gets heavier."},
                                new String[]{"* The Dove pays four thousand, in a plain bag, delivered in a pie. You send the ledger back in the same pie dish. It is only afterwards that you realise it was a favour he did not need to ask, and a trap you did not need to walk into. The shop is full by Saturday. The bell does not stop.", "@Pim| Is the bank very pleased with us? There are so many envelopes."}))
                .outro(
                        "* Verrel takes the cup and sips it. She smiles at the Inspector and says that it needs sugar. The Inspector looks at {calla.his} own cup, and at hers, and at the shortbread. {calla.He} does not take any. The old woman puts her cap straight, takes her coat from the hook, and walks out between the constables as if she were going to church.",
                        "* At the door she stops. She looks at the shop: the shelves, the scales, the cat, the green jars. She says in a clear dry voice, not to you, not to the Inspector, to the room: 'It was a very good shop.' Then she goes.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("cup", "The Second Cup").gap(48)
                .intro(
                        "* The Inspector comes on a Tuesday evening after the shop has shut. {calla.He} knocks at the front, which {calla.he} has never done before. {calla.He} is out of uniform, in a dark coat with the collar up, and {calla.he} carries a small brown paper bag. You let {calla.him} in. {calla.He} puts the bag on the counter and says, in a voice with a very slight tremor, that it is shortbread.",
                        "@Calla| I wanted to say that I do not think you were involved. I wanted to say that I believe you are a better person than your trade. I would like to say several other things which I cannot say in my official capacity. Could we have some tea?")
                .wait(8)
                .outro(
                        "* You put the kettle on. The shop is dark except for one lamp. {calla.He} sits on the stool by the scales, and the cat comes and sits on {calla.his} lap, which it does for nobody. You make two cups. You bring them over. There is a moment, with the cups in your hands, when you realise with perfect clarity what the tea is and what it could be, and that {calla.he} is realising it too.",
                        "@Calla| I am going to drink mine. I would like you to know that. And I would like you to know that I know exactly what is on the shelf behind you. And that I came anyway.")
                .outChoice(new Chapters.Choice("cup", "The Inspector is going to drink the tea, whatever is in it.",
                        "Pour from the safe pot, and keep the antidote ready in your sleeve.", "Pour from the safe pot, and keep nothing in your sleeve: it is a matter of trust.", "Put the cup down. You cannot do this, even as a test.")
                        .after(
                                new String[]{"* You pour from the safe pot. It is plain Ceylon, a good one, with a little honey. The antidote is in your sleeve, in a vial, not because you are afraid, but because you cannot be the one who didn't. {calla.He} drinks. {calla.He} watches your face the whole time. When {calla.he} puts the cup down {calla.he} says, 'It's lovely,' and takes your hand, and says nothing at all about the vial."},
                                new String[]{"* You pour from the safe pot. You hold nothing back. You trust {calla.him}, and, which is stranger, you trust yourself, and the whole of the shop seems to stand up a little taller around you, as if the shelves had been told they could stop holding their breath. {calla.He} drinks. 'It's the best tea I have ever had,' {calla.he} says, and you both laugh, too hard, for too long."},
                                new String[]{"* You put the cup down. You say that you cannot. {calla.He} looks at you, a long level look, and then {calla.he} nods, and puts {calla.his} own cup down beside yours, undrunk. 'Thank you,' {calla.he} says. It is a very small word. It weighs rather more than the kettle."}))
                .done((c, st) -> { int cup = Chapters.flag(st, "cup"); Chapters.set(st, "love", cup); Chapters.bond(c, st, "calla", cup == 3 ? 1 : 2); })
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("sign", "The New Sign").gap(64)
                .intro(
                        "* The Tower lets visitors in on the second Sunday of the month. The cell is a clean whitewashed room with a window the size of a prayer book. Madam Verrel sits on the bed with her knitting. She is getting on well. She has taught the warders to make shortbread. She has been awarded, by popular vote, the library.",
                        "@Verrel| I am going to be here for a few years, dear. I find I do not mind. It is very restful. There is nothing to be ashamed of when everyone already knows.")
                .realm(1, "Make the shop yours: reach Major Lvl 1.")
                .outro(
                        "* You paint the new sign yourself. It says VERREL AND ----, in white on dark green, with your name in the gap. Under it, smaller, in the lettering of the old shop: Cures. Third sign, in the window, in a hand you do not recognise as your own: Three questions before every sale.",
                        "?love=1|@Calla| (at the door, off duty, with a loaf) I have come to inspect the new paint. I find it passes.",
                        "?love=2|@Calla| (at the door, off duty, with a loaf) I have been told there is a spare chair. I would like to see whether it is a good one.",
                        "?love=3|@Calla| (at the door, in uniform, with a notebook) I have been instructed to keep an eye on the premises. I shall be doing it from this side of the counter, at a respectful distance, with a friendly manner.",
                        "* In the back room, under the loose board, there is a notebook with a very short list of names. In the front room, on the counter, there is a cat. Nobody has told you which of them is in charge.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "Antidotes Extra",
                "* Brinewick has two apothecaries and one inspector. The inspector eats her lunch in the front shop on Wednesdays, and has been seen to leave with a small paper bag of lavender.",
                "?widow=1|* The widow comes by on Sundays with a loaf and her son's school report. She sits at the back and shells peas, and nobody has said a word about it, then or since.",
                "?widow=2|* The husband left town in the spring and nobody has asked where. The widow runs the net-loft now, with a firm hand and her son at her elbow.",
                "?widow=3|* You do not talk about the widow. She does not talk about it either. She nods to you on the quay, once a week, a very small nod. You return it. It is a fair trade.",
                "?ledger=1|* The green ledger is ash. People nod to you on the street and you do not know what they owe, and you find, a little to your surprise, that you like it better.",
                "?ledger=2|* The green ledger is under the board, under the counter, against the wall. You open it twice a year. Both times you feel it gets heavier.",
                "?ledger=3|* The green ledger is in the Dove's cellar. Dove's has a second blue door now, and a doorman. The bell in your shop rings more often than it used to.",
                "* Madam Verrel writes from the Tower on the first of every month. The letters are about knitting, and the library, and a warder called Hilda who has a weak chest and no taste in tea. At the bottom, always, in smaller writing, a line: Mind the foxglove. It is the same line each time. It does not mean the same thing twice.");
        TalePoisonerSeasons.add(d);
    }
}
