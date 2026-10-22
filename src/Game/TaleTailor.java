package Game;

/** The Tailor of Seven Hems (2026-10-20; new start, three seasons). The human inherits a tailor's shop in the Lantern Quarter of Brass Ford from Madam Orla Vane, who has gone to the country. Every garment has six hems
 *  you can see and a seventh that holds a promise. Season 1, "The Dress Code" (a comedy of manners): sect dress codes and their loopholes, the apprentice Thimble, the rival across the lane, Kestra Dray (the other gender than
 *  the human's), the gala robe, the Guild examination. Choices: promise (what is stitched into the seventh hem), loophole (how three codes are reconciled), love. Flags: promise, loophole, love.
 *  Seasons 2 and 3 are in TaleTailorSeasons. */
final class TaleTailor {
    private TaleTailor() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void register() {
        Chapters.Def d = Chapters.def("tailor");
        d.season(1, "The Dress Code");
        d.flags("craft0", "promise", "loophole", "love");
        d.place("quarter", "wildnear").place("country", "far");
        d.cast("orla", "Madam", "Vane").she().keep().trait(Character.Traits.Tailor).side("Neutral").lvl(9).pot(6).age(68).at("place:quarter").rel(3);
        d.cast("thimble", "Thimble", "Penhallow").she().keep().trait(Character.Traits.Friendly).side("Good").lvl(1).age(20).at("place:quarter").rel(3);
        d.cast("kestra", "Kestra", "Dray").opposite("Kestrel").keep().trait(Character.Traits.Tailor).side("Neutral").lvl(5).age(29).at("place:quarter").rel(-1);
        d.cast("thrale", "Dame", "Thrale").she().keep().trait(Character.Traits.Leader).side("Neutral").lvl(9).pot(6).age(70).at("far").rel(0);

        d.chapter("hems", "Seven Hems")
                .setup((c, st) -> Chapters.spawn(c, st, "orla"))
                .intro(
                        "* The shop is called Seven Hems. It sits in the Lantern Quarter of Brass Ford, between a lamp-maker and a tea merchant who sells nothing but one very dull tea. The sign is a gold needle on green. The window holds a headless dummy in a good grey coat, which has stood there so long that the neighbours greet it.",
                        "* Madam Orla Vane has run the shop for forty years. She wrote to you in the spring, on a card, with a pin stuck through the corner. It said: I am too old to bite the thread. Come and learn to do it properly. Bring your own scissors.")
                .at("quarter", "Go to Seven Hems in the Lantern Quarter, {@quarter}.")
                .outro(
                        "* Madam Vane is sixty-eight and as upright as a ruler. She wears a black dress with a pincushion strapped to the wrist like a bracelet. Her hair is white and pinned in the shape of a small neat cabbage. She looks you up and down once, from the hat to the boots, as a surgeon looks at a patient she has already decided to save.",
                        "@Orla| Stand there. Don't move. You've a shoulder an inch higher than the other, and you've been told not to slouch, and you do it anyway. Hold out your arm. Mm. You'll do.",
                        "@You| I only came to see the shop.",
                        "@Orla| The shop is the least of it. Look at this.",
                        "* She turns up the hem of her skirt. It is neat and flat and very dull. Then she turns it up again, and again, and you see that it has not one hem but seven, folded inside each other, each a little narrower than the last, each stitched in a different thread. The seventh is a hair's breadth wide. It is stitched in silver.",
                        "@Orla| Six you can see. The seventh holds a promise. Every garment I have ever made has had one. Nobody knows. That's the point of a promise, dear. It does its work whether or not you remember it.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("sleeve", "A Sleeve for Every Sect").gap(8)
                .setup(TaleTailor::mark)
                .intro(
                        "* Brass Ford has nine sects, and every one of them has a code of dress. The Cobalt Order wears blue only, and never below the knee. The Vermilion Hall wears red, in a cut that was fashionable at the time of the flood. The Grey Brotherhood wears anything, so long as it is grey, and they take this very seriously. The shop makes their sleeves.",
                        "@Orla| You'll learn the codes by heart. You'll learn them before you learn the stitches. A sleeve that offends a sect is a sleeve that gets burned, and a tailor who has had two sleeves burned is a tailor who is not asked back.")
                .made(8, "Learn the codes: cut and sew eight garments.")
                .outro(
                        "* By the eighth garment you have made three mistakes. A Cobalt robe, three inches too long. A Vermilion cuff, with a yellow thread by accident. A Grey tunic that, when held to the light, turns out to be fractionally green. Orla finds all three in a single glance and says nothing. She picks up the green tunic, and unpicks it, and hands you the thread.",
                        "@Orla| Again. Slowly. A stitch is a small decision. Don't make it twice.",
                        "* You make the Grey tunic again. It is correct. It is grey as a pigeon's back. When you hold it up, Orla runs her thumb along the cuff and stops at the seam, and for a moment her mouth twitches at one corner. It is the most extravagant praise you have had in your life.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("seventh", "The Seventh Hem").gap(16)
                .intro(
                        "* It is a Tuesday, and you have been sewing for nine hours, when Orla puts down her scissors and says that she thinks you are ready for the seventh. She says it as one might say that the tide is turning. She takes a bolt of undyed linen off the shelf, cuts a length, and lays it on the long table between you.")
                .wait(8)
                .outro(
                        "@Orla| The seventh hem is yours to fill. It can be a word, a wish, a name. It must be true when you stitch it. It has to be small enough that nobody feels it and honest enough that the garment can carry it. I have stitched four thousand and eleven. I have never told anyone what any of them said.",
                        "@You| What happens when it comes true?",
                        "@Orla| The garment is worn out. Not before. That's why one must be so careful what one asks of a coat.")
                .outChoice(new Chapters.Choice("promise", "The seventh hem of your first real garment is empty, and the silver thread is in your hand.",
                        "Stitch a kindness: may the wearer be forgiven something.", "Stitch a warning: may the wearer be told in time.", "Leave the seventh hem plain. Some promises should not be made on another's behalf.")
                        .after(
                                new String[]{"* You stitch a kindness. It takes an hour, and three false starts. When it is done it looks like nothing: a line of silver no wider than a hair. You press it flat with the iron. Orla holds the coat up to the lamp, and runs a thumbnail down the inside of the hem, and says, 'Ah,' very softly, as a person says it who has had a thing confirmed.", "@Orla| Good. It will be forgiven. I hope it is something small."},
                                new String[]{"* You stitch a warning. It takes forty minutes. You are quite sure it is crooked. Orla holds it up, and looks at it for minutes, and says that it is rather a stern little hem, but that the world needs a few. She hangs it on the rail in the window. It sells within the week to a sergeant of the City Guard.", "@Orla| He has a long face. He'll need it."},
                                new String[]{"* You turn up the seventh hem, and press it, and leave it plain. Orla looks at it. She looks at you. She says nothing at all for a minute, and then she says that, in forty years, she has seen perhaps six apprentices do this, and that four of them became very good tailors.", "@Orla| And the other two went into politics. I wouldn't worry."}))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("thimble", "Thimble").gap(24)
                .setup((c, st) -> Chapters.spawn(c, st, "thimble"))
                .intro(
                        "* The girl on the doorstep is twenty and wears a coat made of four other coats. She has a basket, a tin whistle and a placard saying, in careful capitals, HAND TO HOLD. She has no hands to hold of her own, she explains, since the last tailor she worked for threw her out for singing. She would like to be taken on. She would like it explained that she does not need paying. She needs a bed, and a dictionary.")
                .friends(2, "Take on an apprentice and make a second friend.")
                .outro(
                        "* Her name is Thimble, which she says is a nickname, and which she will not explain. She learns the pins in a morning and the pressing in an afternoon. By the end of the week she has reorganised the button drawer by mood. Orla finds the new arrangement baffling and quite right.",
                        "@Thimble| I don't talk much, you know. People think I do. It's only that I'm in a good mood. Do you want me to hem that?",
                        "@You| It's a curtain.",
                        "@Thimble| I like curtains. They're hems with a view.",
                        "* She sings, too. You had been warned. She sings when she presses, and when she sorts thread, and in her sleep, and she is flat on every note. It is one of the most cheerful sounds you have ever heard. Next door, the lamp-maker has stopped complaining. He says it makes the lamps burn higher.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("kestra", "The Shop Across the Lane").gap(32)
                .setup((c, st) -> Chapters.spawn(c, st, "kestra"))
                .intro(
                        "* The shop that opens across the lane at Michaelmas is called Dray & Co. It has plate glass, brass fittings, a doorbell like a cathedral and a window of dummies in the latest cut. They are all faceless. They are all in the colours of the nine sects, tastefully blended into a fashion nobody has asked for. By ten in the morning there is a queue.",
                        "@Orla| That's a young person with money and ideas. I do hope they are going to be tiresome.")
                .meet("kestra", "Call on Kestra Dray, across the lane.")
                .outro(
                        "* {kestra.He} receives you at the door with a tape measure round {kestra.his} neck and a pin in {kestra.his} mouth. {kestra.He} is about twenty-nine, narrow and bright and impatient, with ink on {kestra.his} cuff and a way of standing that makes the room look slightly out of date. {kestra.He} takes the pin out and says that you must be the old shop.",
                        "@Kestra| Don't take it personally. I've nothing against tradition. I only think it ought to be cut on the bias. Do come in. Do you want to see my patterns? I've a way of sewing three codes into one garment that no one's thought of.",
                        "@You| That sounds like a loophole.",
                        "@Kestra| It's not a loophole. It's an interpretation. I've a licence from the Guild.",
                        "* {kestra.He} shows you a sleeve. It is blue on the outside, red inside, and grey in the lining, and it can be turned in the hand to present any of the three. It is the cleverest garment you have ever seen. It is also, you realise, exactly the sort of thing that gets a tailor burned. You tell {kestra.him} so. {kestra.He} laughs, and says that is what they said about the buttonhole.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("gala", "The Gala Robe").gap(32)
                .setup(TaleTailor::mark)
                .intro(
                        "* The letter comes from the Guild of Hems in a cream envelope with a gold seal. Dame Thrale, President of the Guild, requests the honour of a robe for the Convocation Gala at the Palace of the Oldest Order. It is to be worn by the Master of Ceremonies. It is to honour all nine sects. It is to be ready in six weeks. The letter ends with a postscript: Do not attempt anything original.")
                .made(6, "Cut the gala robe: sew six pieces of it.")
                .outro(
                        "* The robe takes six weeks and eleven yards of silk. It has nine panels, one for each sect, and each panel is perfect. You sew them together in the long room at the back of the shop with Orla standing over you and Thimble holding the lamp. By the third week you can hear your own pulse in the needle.",
                        "* Dame Thrale arrives on the day with two clerks and a trunk. She is seventy and as dry as paper, with a voice like a drawer being slid shut. She walks round the robe twice, without a word. She lifts a cuff. She turns up the hem. She runs a thumbnail along the inside of the seventh, which is bare, and stops.",
                        "@Thrale| You have left it plain.",
                        "@You| I thought it was for the wearer to fill.",
                        "@Thrale| Interesting. It is also against the Guild's rules. A seventh hem must be stitched, or it is not a hem. I shall let it pass. I shall mention it to the committee.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("stain", "The Stain").gap(24)
                .intro(
                        "* A man comes into the shop every Thursday with a white linen shirt and a tragic expression. He lays it on the counter. It has a stain on the cuff, a wine stain, a deep plum purple. He asks if you can get it out. You can. You do. He pays, thanks you, and goes. The following Thursday he returns with the same shirt, and the same stain, and the same expression.")
                .wait(12)
                .outro(
                        "* This goes on for five weeks. Thimble starts to keep a tally on the back of the door. Orla says nothing, with a gleam. On the sixth Thursday you take the shirt, and look at the stain, and say, 'I can get this out, but I'd like to know how it keeps getting back in.'",
                        "* He stands there for a moment. He has a long grey face, like a patient horse. He says that his wife died in the spring. He says she was a clumsy woman and spilled wine on his cuff at their last dinner. He says he cannot get himself to wash it. He says he comes here to have it cleaned, and then he goes home and puts it back.",
                        "@You| Would you like it to stay?",
                        "@Man| I would like it to be cleaned, and I would like it to be there. I know it makes no sense.",
                        "* You take the shirt. You sew a small purple flower into the cuff, in silk, exactly where the stain was, and press it flat. It looks like nothing. It looks like the cuff of a shirt. He holds it up to the window for ages. He says thank you in a voice that is much younger. He never comes back, and he never needs to.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("code", "Three Codes, One Coat").gap(24)
                .intro(
                        "* Three sects send commissions on the same morning for the same occasion. The Cobalt Order requires a coat in blue, to the knee, with no ornament. The Vermilion Hall requires red, with a high collar and a gilt button. The Grey Brotherhood requires grey, in any style, but with a mourning band. They are to be worn at the same funeral. They are to be worn by the same man. He has been adopted by all three.")
                .wait(8)
                .outro(
                        "* You spend a night with the three letters spread on the table, and a pot of tea, and Thimble asleep in the corner with a pin in her mouth. In the morning Orla comes down at six and reads them in her dressing gown, with her glasses on the end of her nose. She says that she has seen this before. She says in 1873 she dressed the Duke of Marsh for his own wedding and his own divorce on the same afternoon.",
                        "@Orla| It can be done. It can be done honestly, if you've a great deal of patience. It can be done cleverly. It can be done in a way that will make three tailors across the city furious. You'll have to decide how to make a man look like three things.")
                .outChoice(new Chapters.Choice("loophole", "Three sects want one man in three colours.",
                        "Make it honestly: three coats, one over the other, so that he wears all three.", "Make it cleverly: Kestra's turning sleeve, in your own cloth.", "Refuse the commission and tell the three sects to come to an agreement themselves.")
                        .after(
                                new String[]{"* You make three coats. The blue goes over the red, which goes over the grey, each a little shorter than the last, so that he wears them in order, like layers of a pastry. He looks ridiculous. He looks, also, absolutely sincere. The funeral is a great success. The Cobalt Order says it was tasteful. The Vermilion Hall says it was bold. The Brotherhood says it was grey.", "@Orla| It is the only time I have seen three sects agree, and it was because they couldn't see each other's faces."},
                                new String[]{"* You go across the lane and ask Kestra for the sleeve. {kestra.He} is so pleased that {kestra.he} drops a pincushion. Together you make a coat that turns, blue to red to grey, and fits three codes. It is the cleverest thing either of you has ever made. It is also, as Orla says afterwards, the beginning of a long and complicated friendship.", "@Kestra| I think we ought to work together more often. I think we are annoying each other in a good way."},
                                new String[]{"* You send a polite note to each sect, saying that the Shop would be honoured to dress the man in whatever they agree upon. You do not hear for a fortnight. Then three clerks arrive at the same time, glaring at each other, with a joint document of seven pages. They have agreed on grey, with a blue lining and a red button. It is, you think, the first time in the history of the city.", "@Thimble| Did you do that on purpose?"}))
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("fitting", "The Fitting").gap(24)
                .intro(
                        "* Kestra comes across the lane on a Sunday evening with a bundle under {kestra.his} arm and an air of elaborate unconcern. {kestra.He} says that {kestra.he} has a problem with a sleeve. {kestra.He} says that it has been bothering {kestra.him} for a week, and that Orla has always said you are good with sleeves. {kestra.He} puts the bundle on the counter and unwraps it. It is a coat for {kestra.his} own back. It is the best thing {kestra.he} has ever made. It has one very small error in the left shoulder.")
                .wait(4)
                .outro(
                        "* You pin it on {kestra.him}. It is a long business. You walk round {kestra.him} three times, with pins in your teeth and chalk in your hand, saying 'Turn' and 'Arm up' and 'Don't breathe, please'. {kestra.He} stands very still, as people do, with the faintly stunned look of a person being fitted by someone they dislike a little less than they expected.",
                        "@Kestra| I should say that I am not good at this. Being measured. I am all right at measuring. I have never been quite certain how to stand.",
                        "@You| Like that. You're fine.",
                        "@Kestra| Mmm.",
                        "* There is a short silence. The pins are very small. Thimble, from the corner, with a needle in her mouth, makes a noise that might be a cough. Orla, who is supposedly asleep in the armchair, opens one eye, and closes it again. You mend the shoulder. It is done in five minutes. Kestra says, 'I'm not in any hurry,' and does not leave until nearly eleven.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("exam", "The Examination").gap(40)
                .intro(
                        "* The Guild of Hems holds its Mastership examination on the first Friday of May, in a long bare hall with seven tables, seven chairs, seven candles and seven examiners in grey. It is said to take three days, three nights, and three kinds of patience. Orla drives you there herself in the shop cart, and sits in the front row with her knitting, and does not say a single word.")
                .realm(1, "Qualify for the Mastership: reach Major Lvl 1.")
                .outro(
                        "* The first day is the cutting. You are given a length of striped silk and told to make a coat without a pattern. The second is the sewing. You are given a needle with no eye and told to thread it. The third is the fitting. You are given a stranger from the street, an old man with a hump and a dislike of tailors, and told to make him comfortable.",
                        "* You do not pass at once. The examiners confer. They confer for an hour, in a corner of the hall, in murmurs. At the end of it the eldest comes over and says that the coat was correct, the sewing was correct, and the fitting was admirable. He says the seventh hem of the coat was blank. He says that is irregular. He says they have decided to overlook it, in view of the fact that the old man has asked to keep the coat.",
                        "@Examiner| He says it is the first thing in forty years that did not itch.")
                .pay(500, 0, 0, 1, 1, 1);

        d.chapter("convocation", "The Gala").gap(24)
                .intro(
                        "* The gala is held in the great hall of the Palace of the Oldest Order. There are nine hundred guests, in nine hundred colours. The Master of Ceremonies, a vast mild man in the robe of nine panels, stands on the dais with a staff in one hand and a look of bewilderment. The robe fits him like a church fits a bell.")
                .wait(8)
                .outro(
                        "* It goes wrong at the third toast. It goes wrong in a small way: a button. The Master raises his staff, and the gilt button on the Vermilion panel flies off, strikes a candle, and sets the Cobalt panel smouldering. A footman runs for water. A lady screams. The robe, from the left cuff to the right, begins gently to smoke.",
                        "* Thimble is on the dais before you have understood. She has a bucket. She has a damp towel. She has, for no reason, a tin whistle. She tears off the Cobalt panel with a single pull, and stamps on it, and the whole hall watches a girl of twenty in a coat of four coats put out the Master of Ceremonies. There is a silence.",
                        "* Then Kestra, from the back, calls out, 'Hems to the rescue!' and starts to clap. The hall laughs. The Master, who is unburned, looks down at his robe: eight panels, and a hole where the ninth was, and a broad stripe of white lining showing through. He begins, slowly, to smile. He says it is the best robe he has ever worn.",
                        "@Thrale| (at your elbow, in a very dry voice) I shall be mentioning this to the committee. I shall be doing it in the warmest terms.")
                .pay(600, 0, 0, 1, 0, 1);

        d.chapter("letter", "Madam Vane Writes").gap(40)
                .intro(
                        "* The letter is brought by the carrier. Orla has gone to the country. She has taken a cottage with a view of a river and a small dog she acquired by mistake. The envelope is thick, and smells of lavender and camphor. Inside is a single sheet, a lock of silver thread, and a few lines in an upright hand.")
                .wait(8)
                .outro(
                        "@Orla| (in the letter) I hear you passed. I hear you set the Master of Ceremonies on fire. I hear nobody minded. I enclose a thread. It is the silver one I used for the seventh. There is a very little left. I wanted you to have it. I have stitched four thousand and twelve now. The last was for you. I shall not tell you what it said.",
                        "* You hold the thread up to the window. It catches the light and turns the colour of a moth's wing. Across the lane, in a window of plate glass, someone is standing with a cup of tea, looking at the shop. They raise the cup. You, absurdly, raise a pair of scissors.")
                .outChoice(new Chapters.Choice("love", "Kestra has been waiting across the lane, with a cup of tea, for most of a year.",
                        "Cross the lane with the thread in your hand and say what you mean.", "Send an invitation to the shop, on a pin, through Thimble.", "Hold back. Be friends. Let the next hem be a plain one.")
                        .after(
                                new String[]{"* You cross the lane. The bell on Dray & Co. goes like a cathedral. Kestra is behind the counter, with the cup and a bolt of silk, and looks up with a face that has been ready for this for minutes. You say what you mean. It takes a minute. It is a very short speech and a very long silence, and when it is over the customer in the corner says, 'Oh, well done.'", "@Kestra| I had a coat made for it. I had it made in the spring. I thought it might be a nuisance if I never had to wear it."},
                                new String[]{"* You pin a note to Thimble's collar and send her across the lane. She returns in four minutes with an answer written on a scrap of silk: Yes. I shall bring the tape measure. Thimble hums the whole afternoon, flat, and takes a long time to find the right button.", "@Thimble| I'm not saying anything. I'm only observing that it's a warm day."},
                                new String[]{"* You decide against it. Not for ever. You say that you would like to stay as you are for the present, and Kestra says that is a perfectly reasonable thing to say, and a perfectly dreadful thing to hear. You go on making coats across the lane from each other, and sometimes at the same table, and the thread between you gets longer and stronger and a little more tangled.", "@Kestra| I have all the time in the world. It's very good thread."}))
                .done((c, st) -> { int l = Chapters.flag(st, "love"); Chapters.bond(c, st, "kestra", l == 3 ? 1 : 3); if (l == 1 || l == 2) Chapters.wed(c, st, "kestra"); })
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "The Seventh",
                "* Seven Hems has two windows now. In the old one stands the headless dummy in the good grey coat. In the new one, which looks across the lane, stands another in a coat of nine panels, with a stripe of white where the ninth was.",
                "?promise=1|* Every garment you make has a kindness in its seventh hem. You have stitched so many that you can tell, from the heft of a coat in your hand, who it will forgive before you open it.",
                "?promise=2|* Every garment you make has a warning in its seventh hem. People say your coats are stern. They say nobody ever gets into trouble in one.",
                "?promise=3|* The seventh hem of your coats is plain. You tell customers it is a house rule. A good many of them take it home and fill it themselves.",
                "?loophole=1|* The man with three coats has a fourth now. It is a good deal lighter.",
                "?loophole=2|* The turning sleeve is on sale in Dray & Co. as the Kestra Reversible. It sells, in all three colours.",
                "?loophole=3|* The three sects have agreed on grey. It has been unlucky for the dyers, and good for everyone else.",
                "* On the shelf above the long table there is a silver thread no longer than a finger, wound round a card. Nobody touches it. On the card, in a hand you know, are three words: Use it sparingly. Somewhere in the country, a small dog barks at a river, and a very old woman threads a needle without looking.");
        TaleTailorSeasons.add(d);
    }
}
