package Game;

/** The Cook Who Fed the Mountain (2026-10-20; new start, three seasons; see "Start Stories Beat Sheets.md"). The human inherits the Long Table, a roadside kitchen where the Salt Road meets the Pilgrim Road. Great-aunt Rue's
 *  cookbook has margin notes that only make sense after the dish is cooked. Season 1, "The Long Table" (plain pastoral with comic touches): the first customers, the Truth Broth, the examiner Cassia Venn (the other gender
 *  than the human's) of the Cobalt Fan Hall, the rival Jade Table, a cook-off, Rue's retirement, and the rule on the lintel. Choices: broth (what the first quarrel was fed), price (how to answer the Jade Table), door (the girl
 *  with a bounty), apron (what the human is). Flags: broth, price, cookoff (1 won, 2 lost, 3 drawn), door, love. Seasons 2 and 3 are in TaleChefSeasons. */
final class TaleChef {
    private TaleChef() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void register() {
        Chapters.Def d = Chapters.def("chef");
        d.season(1, "The Long Table");
        d.flags("craft0", "broth", "price", "cookoff", "door", "love");
        d.place("crossing", "wildnear").place("valley", "far");
        d.cast("rue", "Rue", "Alderwick").she().keep().trait(Character.Traits.Chef).side("Good").lvl(8).age(74).at("place:crossing").rel(3);
        d.cast("ox", "Ox", "Tamberlin").he().keep().trait(Character.Traits.Friendly).side("Good").lvl(2).age(20).at("place:crossing").rel(3);
        d.cast("cassia", "Cassia", "Venn").opposite("Cassian").keep().trait(Character.Traits.Chef).side("Neutral").lvl(5).age(28).at("far").rel(1);
        d.cast("pell", "Gaspard", "Pell").he().keep().trait(Character.Traits.Merchant).side("Neutral").lvl(7).pot(6).age(52).at("far").rel(-1);

        d.chapter("crossing", "A Table at the Crossing")
                .setup((c, st) -> Chapters.spawn(c, st, "rue"))
                .intro(
                        "* Your great-aunt's letter is three lines long and smells of onions. 'The roof wants mending. The stove wants you. Come before the snow. R.'",
                        "* The Long Table stands where the Salt Road meets the Pilgrim Road, two days from anywhere with a name. It has been open for ninety years. It has had no door for eighty of them, because someone took it off to make a bench and nobody ever put it back.",
                        "* You are on the road at dusk, in a thin cold rain, with a pack and a knife roll and the feeling of having been summoned to something. Everybody in your family cooks. Nobody in your family has ever been summoned.")
                .at("crossing", "Go to the Long Table at {@crossing}.")
                .outro(
                        "* The kitchen is a long low room with a chimney at each end and a table down the middle that could seat forty. The table is scarred, scrubbed and slightly tilted. There are eleven pots on the hooks. The rain comes in through the missing door and goes out through the missing window, and between the two of them the room is warm.",
                        "* Your great-aunt Rue is seventy-four years old and about as tall as the stove. She has flour to the elbows and arms like rope. She looks up from the dough, looks at the clock, looks at you, and goes back to the dough.",
                        "@Rue| You're late.",
                        "@You| It's a two-day walk.",
                        "@Rue| I said before the snow. It's raining. Rain is snow that hasn't made up its mind. Wash your hands.",
                        "* Over the empty doorway is a board, black with smoke. It says NOBODY IS TURNED AWAY. Under it, in smaller and different letters, someone has added: (Nobody means nobody. See Auntie.)",
                        "@Rue| That's the whole of the law here. The rest is soup. Eat first, then ask. A cook who hasn't eaten can't be trusted with salt.",
                        "* She puts a bowl in front of you. It is plain barley and onion and something green. You have eaten at three sect kitchens and a prince's table, and it is the best thing you have ever put in your mouth. You tell her so. She sniffs.",
                        "@Rue| That's the pot that's been going since Tuesday. Never throw out the bottom of the pot. The bottom is where the years are.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("ten", "Ten Plates").gap(8)
                .setup(TaleChef::mark)
                .intro(
                        "* The first customers come at dawn: eight caravan guards, wet to the knees, who have walked all night and are prepared to be rude about it. Rue pours them tea, tells you to cook, and sits down on a bucket to watch.",
                        "* There is a rule about the Long Table, you learn, and it is that the new cook cooks. Rue does not help. Rue does not advise. Rue peels one potato, extremely slowly, and says nothing, which is worse than advice.")
                .made(10, "Feed the table: make ten dishes.")
                .outro(
                        "* The first plate is oversalted. The second is undersalted, which you notice only because the guard who eats it says 'Ah,' very gently, in the voice of a man at a graveside. The third is perfect and nobody notices. The fourth is burned. The fifth, which you cook in a rage, is the best thing you have made in your life.",
                        "* By the tenth the guards have stopped being rude. The sergeant, a square grey man with a scar like a seam, wipes his plate with a heel of bread and says the road has not fed him this well since the war. He leaves two silver coins under the plate and tells his men to say nothing.",
                        "@Rue| Two silver. He's a sentimental man. They're the worst payers and the best customers. Put one in the jar and the other in your pocket.",
                        "@You| What's the jar for?",
                        "@Rue| For the ones who come after, with nothing. Mind you, you'll learn that nobody has nothing. They have a coat, or a story, or a good pair of hands. You take what they've got. It's the taking that matters, not the price.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("margin", "The Margin Notes").gap(16)
                .intro(
                        "* The cookbook lives in the pocket of Rue's apron. It is a fat greasy book with no title. Rue lends it to you on a wet Wednesday, with the air of a woman handing over a sleeping baby.",
                        "* The recipes are ordinary. The margin notes are not. Beside a barley soup, in a pencil hand that has been rubbed nearly away: Stir until you think of the worst thing you ever said. Beside a plum dumpling: Do not serve to anyone who is lying. You cannot tell whether she is joking.")
                .wait(8)
                .outro(
                        "* On the Thursday a couple come in out of the rain, quarrelling in low furious voices, as couples do who have been walking all day and have run out of things to say but not the will to say them. They sit at opposite ends of the long table. They do not look at each other. They order two bowls of anything.",
                        "* You look at the book. There is a page for exactly this. The Truth Broth. Beside it, in Rue's hand: Gives people what they cannot say. Use only with a good conscience. Rue is standing at the stove with her back to you. She has not said a word. She has never been more obviously not saying one.",
                        "@Rue| It's your kitchen on Thursdays.")
                .outChoice(new Chapters.Choice("broth", "A couple who are not speaking sit at opposite ends of the table.",
                        "Make the Truth Broth, and trust them with it.", "Make the plainest sweet soup you know, and let them be.", "Put it to them: say what is in the pot, and let them choose.")
                        .after(
                                new String[]{"* The broth is brown and smells of nothing in particular. They eat it in silence. About halfway down the bowl she puts down her spoon and says, 'I never wanted to move to your mother's.' He says, 'I know. I never wanted to ask.' It is the most useful sentence either of them has said in a year.", "@Rue| Hm. You stirred it the right way, then. That's not bad for a first go."},
                                new String[]{"* You make the sweet soup, with a little honey and a lot of care. They eat it and relax, the way people do over honey. By the end they are talking about the weather. It is not a cure. It is a pause, and the pause is real, and you are surprised how much you are proud of it.", "@Rue| Kind of you. It will keep till the morning. Not many cooks will take a pause over a victory."},
                                new String[]{"* You go to their table and tell them straight what is in the pot and what it does. They stare at you. Then, after a very long silence, the man laughs, a short bark, and the woman says, 'Give it here.' They drink the Truth Broth like a toast. It is a good evening, and you will hear of it for years.", "@Rue| Asking first. Well, I'll be blowed. I never did that once in sixty years."}))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("examiner", "The Examiner").gap(64)
                .setup((c, st) -> Chapters.spawn(c, st, "cassia"))
                .intro(
                        "* The letter has a blue fan sealed into the wax. The Cobalt Fan Hall is the sect that holds the licence for every kitchen between the mountains and the sea, and it does not write to roadside cooks unless somebody has complained.",
                        "* It says that the Long Table is in breach of the Banquet Code, in that it uses spirit herbs of Grade Two without a licence, and that an Examiner will call. It says that a refusal to receive the Examiner will be taken as an admission. It is signed with a flourish, and under the flourish, in another hand, someone has added: Sorry. I did not write this part.")
                .meet("cassia", "Receive the Examiner of the Cobalt Fan Hall.")
                .outro(
                        "* {cassia.He} comes up the road at noon on a very tidy horse: {cassia.he} is about twenty-eight, thin, in a blue coat with a fan on the breast, with a satchel of forms, a pair of silver chopsticks in a case. The stunned look of a person who has eaten badly in forty inns in a row and has begun to wonder whether it is {cassia.his} own palate.",
                        "@Cassia| Cassia Venn, Examiner, Second Class. I am here to inspect your herbs. I would like to say at the outset that I have nothing against you, and that I am extremely hungry.",
                        "@Rue| Sit down.",
                        "@Cassia| I'm on duty. I can't accept hospitality from the subject of an inspection.",
                        "@Rue| Then inspect it. Sit down and inspect it.",
                        "* {cassia.He} sits. You bring a bowl. {cassia.He} takes the silver chopsticks out of their case with a ritual air, tastes the barley soup, and stops. The chopsticks come to rest across the bowl. {cassia.He} sits very still for a moment, like somebody who has heard {cassia.his} name called in an empty street.",
                        "@Cassia| There are three herbs in this that are Grade Two. I can taste all three. I should write that down.",
                        "@You| Yes.",
                        "@Cassia| I'm not going to write it down yet. I'd like to have another bowl first. For the purposes of the report.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("hands", "Hands").gap(96)
                .intro(
                        "* The Long Table seats forty. for ages it has been feeding thirty on a good day with two cooks, one of whom is seventy-four. On Saturday, a wagon of pilgrims arrives, fifty strong, in a heavy rain. Rue sits down on the bucket and says, in a calm voice, that she has done this for sixty years and would now like to see how you do it.",
                        "* You do it badly. You do it with great speed and a certain flair. There is a point at about the fortieth bowl when the stove goes out. There is a point at the fiftieth when the bread does. The sergeant from the caravan, passing through with a new load of nervous recruits, takes off his coat and starts washing dishes without being asked.")
                .friends(3, "A table needs hands: make three friends.")
                .outro(
                        "* They arrive the way things arrive at a crossroads, one at a time and from every direction. The sergeant, who has turned out to be called Brannock and to be the finest dishwasher in the province. A boy called Ox, who is twenty and the size of a door, and who came for a bowl of soup and has been at the sink ever since, unpaid. Nobody has had the heart to tell him he can stop. A widow from the next farm who brings eggs and an opinion about the cheese.",
                        "* By the end of the week the kitchen has a rota. It is chalked on the wall in four different hands. Rue reads it with her glasses on the end of her nose and says that a cook should never be afraid of being needed. It is the nearest she has come to praise.",
                        "@Ox| I can carry two pots at once. I tried three. It didn't go well.",
                        "@Rue| He can also carry a whole conversation on his own, if you let him. Sit down, lad. Eat.")
                .pay(100, 0, 0, 1, 0, 0);

        d.chapter("jade", "The Jade Table").gap(80)
                .setup((c, st) -> Chapters.spawn(c, st, "pell"))
                .intro(
                        "* The sign goes up across the road at the end of the month, in green and gilt, on a building that was a stable on Monday. THE JADE TABLE: FINE DINING FOR THE DISCERNING TRAVELLER. Beneath, smaller: Franchise of Pell & Sons. Licensed by the Cobalt Fan Hall.",
                        "* The Jade Table has a door. It has a doorman. It has a menu with seventeen kinds of soup, none of which is called soup. By the second day half the pilgrims on the Pilgrim Road have gone to look at it, and the other half are looking at the half who have gone.")
                .meet("pell", "Call on Gaspard Pell at the Jade Table.")
                .outro(
                        "* Gaspard Pell is a round, silver-haired man in a coat of the exact green of his sign. He receives you on the porch like a head of state. He shakes your hand with both of his. He says he has long admired your great-aunt, that he regards the Long Table as an institution, and that he would take it as a personal favour if you would consider selling.",
                        "@Pell| I'd keep the name. Heritage, you understand. The table, the board, the pots. We would put in a door.",
                        "@You| It has a door. It's on the bench.",
                        "@Pell| Then we'd put it back. Think about it. We are none of us getting younger, and the road is changing. Fine dining wants a roof, a licence, and a shared vision.",
                        "* He is smiling all the way through. The smile has a great deal of teeth in it. On the way out you see a tidy boy in a green apron carrying a tray of seventeen tiny bowls. You see that every bowl has a little sprig of garnish on it, and every sprig is the same.")
                .outChoice(new Chapters.Choice("price", "The Jade Table is a stone's throw away and cheaper than you by a third.",
                        "Undercut him: charge less than it costs, and take the loss.", "Ignore him and cook the way you always do.", "Invite him to dinner, and let him taste.")
                        .after(
                                new String[]{"* You lower your prices. It works for ten days. On the eleventh Rue looks into the jar and says nothing, then looks at you and says nothing louder. You raise them again, and learn that a price is a sort of promise.", "@Rue| Never cook cheaper than yourself. It teaches the customer you're worth less than the soup."},
                                new String[]{"* You do nothing. The Jade Table is full all week, and the Long Table is half empty, and on Sunday the pilgrims come back. They say the green place had a lot of towels, and not a great deal else.", "@Rue| People only leave a place that feeds them once. They come back for the same reason."},
                                new String[]{"* Pell comes to dinner in a green coat, with two cousins. He eats three bowls of barley soup. When he is done he sits for a minute, looking at the bowl as if it had spoken to him. Then he says, in a different voice, 'Who taught her?' It is the most honest thing he says all year.", "@Rue| He'll hate you for it. A man hates the one who reminds him what he's forgotten."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("pepper", "The Wrong Pepper").gap(96)
                .intro(
                        "* A bad week. The crossing's supplier of herbs has stopped coming, the Cobalt Fan Hall's licence fee has been raised by a fifth, and the pepper, which is the one thing Rue never runs short of, has run out. It has been coming from the same grower in the Sunroot Valley for forty years.",
                        "* Rue will not close the kitchen to go herself. She is, she says, the kitchen. She gives you a list on the back of a bill. At the bottom, in her handwriting, in pencil: And do not buy from Pell's man.")
                .at("valley", "Fetch pepper from the grower in the Sunroot Valley, {@valley}.")
                .outro(
                        "* The valley is a long green fold in the hills with a river in the bottom. The grower, a lean, silent woman called Hazel, takes you down the rows without a word, and shows you where the pepper has failed. Half of it has dried on the stem. The other half is perfectly fine, but it tastes of nothing.",
                        "@Hazel| It's been like this since the spring. It's not blight. I'd know blight. It's as if the plants have been told something, and have decided they don't want to bother.",
                        "* She hands you a pinch of the good pepper. You roll it on your tongue. It is dry, fragrant, empty. It is pepper, in the way a painted door is a door.",
                        "* You buy all of it, because there is no more to be had, and on the way home you reflect that it is the first time you have ever met a plant that is out of sorts. You mention it to Rue. She says nothing, for rather a long minute, and then she says that her mother once said the same thing about the whole country, the year before the last bad winter.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("cookoff", "The Cook-Off").gap(80)
                .setup((c, st) -> { mark(c, st); int roll = GameDriver.rand.nextInt(100) + c.lvl * 2 + (Chapters.flag(st, "price") == 3 ? 15 : 0) - (Chapters.flag(st, "price") == 1 ? 5 : 0); Chapters.set(st, "cookoff", roll > 70 ? 1 : roll < 45 ? 2 : 3); })
                .intro(
                        "* The challenge arrives on green paper, with a gilt edge. Pell & Sons propose a contest of cooks, in the open, at the crossing, on the first of the month, with a judge appointed by the Cobalt Fan Hall. The loser withdraws. The winner may purchase the premises of the loser at a fair price. It is very reasonable. It is signed in a hand like a row of well-trained ducks.",
                        "@Cassia| I'm the judge. I want it noted that I did not ask to be, and that I tried to decline. They said a conflict of interest was a matter of opinion. I can't tell if they know. They keep sending me small gifts.")
                .made(8, "Win the day: cook eight dishes for the judge.")
                .outro(
                        "* The contest is set out in the road like a fair. Both kitchens have a stove on a platform, and a crowd has come from three days' walk. The Jade Table's cooks wear green and arrive in a line. Yours wear aprons and arrive in the wrong order. Ox carries both stoves.",
                        "* You cook eight dishes. Pell's people cook eight dishes. His are small and exquisite and utterly without fault, like a row of jewels in a case. Yours are large and hot and slightly crooked. The judge tastes them all with the silver chopsticks, in turn, a spoonful at a time, with the stricken face of a person being asked to choose between two parents.",
                        "?cookoff=1|@Cassia| The Long Table. I have to say it. By one point. The Jade Table was flawless and I could not tell you a single thing I remember about it.",
                        "?cookoff=2|@Cassia| The Jade Table, by one point. I am sorry. It is a technical decision. I want you to know that I ate the rest of your dumplings in the cart, and I wept a little.",
                        "?cookoff=3|@Cassia| A draw. I cannot split them. I have written it twice and the pen won't let me do it. I shall be dismissed, and I find that I don't mind.",
                        "* Pell shakes your hand again with both of his. He is not smiling. He looks, for once, like a man who has tried something, and it did not turn out to be a sum.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("last", "Rue's Last Dinner").gap(64)
                .intro(
                        "* On the first frost Rue calls you into the kitchen, shuts the door that is not there, and tells you that her sister on the coast has written. The sister is ninety. The sister has a cottage with a view of the sea and a spare bed and a standing offer, going back thirty years, that Rue has turned down every spring.",
                        "@Rue| I'll be going on the carrier. Friday week. You'll have the kitchen. Don't say anything, I can't stand being thanked. I want to cook one more dinner, with you, and I want you to eat it sitting down.")
                .realm(1, "Before she goes, step over the first gate: reach Major Lvl 1.")
                .outro(
                        "* The dinner is for nine. She has invited the sergeant, the widow, Ox, the grower from the valley who came three days on a donkey, and the doorman of the Jade Table, who came in uniform and left the uniform on the step. Cassia is there too, off duty, in an ordinary grey coat that does not suit {cassia.him}.",
                        "* The dish she makes is not in the book. She has not written it down in sixty years, and she does not write it down now. It is a plain brown soup with a single clear dumpling floating in it, and a spoon of something that you cannot name. It tastes of everyone you have ever sat next to at a meal.",
                        "@Rue| That's it. That's the whole secret. It's only what's in the room.",
                        "* She goes on the carrier on Friday week, with one small bag and a pot. She does not look back. At the bend in the road she raises one arm without turning her head, in the way of someone who has done a good deal of leaving and has found the best method is not to look. Ox is crying into the sink. The widow, who has no sentiment, gives him the egg basket to hold.",
                        "@Rue| (from the carrier, not turning) The bottom of the pot!",
                        "* You find the apron on its hook that evening. It is stiff with sixty years of soup. In the pocket is the cookbook, and a stub of pencil, and a note in her upright, impatient hand: Write something in the margins. They're no use till you do.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("gate", "The Gate Tax").gap(96)
                .intro(
                        "* The Crossing has grown, in a year, from a kitchen to a village. There is a smith now, and a stable that is a stable, and a carter who has set up a yard. There are, you notice, eleven families. The caravan road bends toward the Long Table. The Cobalt Fan Hall, which notices these things, sends a surveyor.",
                        "* The surveyor puts up stakes. He is polite. The stakes are for a gate, and the gate is for a toll, and the toll, as he explains with a regretful cough, is for the benefit of the Hall.")
                .friends(6, "Make the crossing a place: have six friends.")
                .outro(
                        "* You do not fight the gate, but do something stranger: you cook for the surveyor and his four assistants, every day for a fortnight, and give them the best of what you have. On the fifteenth day they are in a state of settled melancholy and the surveyor cannot find his stakes. They have been put to use, by Ox, as the frame of a long, handsome bench.",
                        "@Cassia| I filed a report. I said the crossing was a neutral table, in the old sense, a place where nobody's quarrel is settled but everybody's is heard. I said the Hall would do well to leave it exactly as it was. They were furious. And then they read the rest of it, and I think they were frightened. It is an ugly feeling to realise a rule is older than the people who enforce it.",
                        "* The tollgate is never built. In the spring, somebody carves a new motto under the old one on the board, and nobody will say who: NOBODY IS TURNED AWAY. (Nobody means nobody. See the Cook.)")
                .pay(400, 0, 0, 0, 0, 0);

        d.chapter("girl", "The Girl with a Price on Her").gap(112)
                .intro(
                        "* She comes in on a night of sleet, a girl of sixteen or so in a man's coat, with a younger boy asleep on her back. She has no money. She has a bruise down one side of her face, and the sort of watchfulness that is learned, not born. She asks for a corner and a crust, and for nothing else, and she watches the door.",
                        "* The bounty notice is on the board in the Jade Table by morning. It has her picture, rather well drawn. Eleven thousand spirit stones. The hand that signed it is a magistrate's, and the seal belongs to a sect that does not like to be refused.")
                .wait(8)
                .outro(
                        "* You know, as you look at the board, exactly what the rule says. You know that Rue is a hundred and forty miles away, painting the shutters of a house by the sea, and has no idea. You know that you will have to decide this one, in your kitchen, with your hands. The girl has stopped eating. She is holding the spoon like a knife.",
                        "@Girl| I'll go. I'm no use to you. I'll take him and go before the light.",
                        "@Ox| You can't. It's sleeting.")
                .outChoice(new Chapters.Choice("door", "A girl with a price on her head sits at the end of the long table.",
                        "Feed her, hide her, and say nothing.", "Feed her, and tell her to be gone by morning.", "Feed her and take her story to the Examiner: let the law hear it.")
                        .after(
                                new String[]{"* You put her in the loft over the second chimney, and the boy in the flour bin, and you say nothing to anyone, for six days. A man in a magistrate's coat comes and has a bowl of barley soup and goes away again. Ox stands at the door the whole time, doing the dishes with the door-bar in one hand, in the manner of a man who wishes to be found only moderately large.", "* It is not a clever answer. It is the one the kitchen gives."},
                                new String[]{"* You feed her. You tell her. She nods, as though she had expected it, and in the morning she is gone, with the boy and a cheese. You never learn whether she was caught. It is the first time the board over the door has felt like a lie, and you take it down and clean it that same afternoon, as though it could be washed.", "@Rue| (in a letter, weeks later) A cook can only do what a cook can do. Don't make a saint of it. Don't make a villain of it. Just notice it. It is a good thing to notice."},
                                new String[]{"* Cassia listens without a word. Then {cassia.he} takes down the notice with {cassia.his} own hand, reads it aloud, and says it is badly drafted and cannot be enforced in any province that holds the Banquet Code. It is not true. It is a plausible kind of untrue, and the magistrate, who has a long dinner coming, finds it a good moment to remember he has another engagement.", "@Cassia| I think I've just committed a fraud. I'd like another bowl, please. For my nerves."}))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("apron", "The Apron").gap(80)
                .intro(
                        "* The apron fits you now. It took two years. It has been taken in at the sides and let out at the waist and it has your own stains over Rue's. Cassia has been over to dinner nineteen times, officially for the Hall, and, it has begun to be felt in the village, not entirely for the Hall.",
                        "* You find the cookbook one night with your own handwriting in the margins. You do not remember writing most of it.")
                .wait(8)
                .outro(
                        "* There is a pencil note beside the Truth Broth, in your hand. 'Rue was wrong. It is not for people who can't say it. It is for people who can, and are afraid to hear themselves.'",
                        "* Cassia is sitting at the end of the long table with {cassia.his} chin on {cassia.his} fist, eating the last of the plum dumplings. {cassia.He} has the look of a person who has watched one thing too long to look away. {cassia.He} has not asked for anything, ever. It is possible that {cassia.he} never will.",
                        "@Cassia| You've never asked me what I'm here for.",
                        "@You| I assumed it was the herbs.",
                        "@Cassia| It was, at first. I'd like you to know I stopped checking the herbs about a year ago.")
                .outChoice(new Chapters.Choice("love", "Cassia has stopped checking the herbs.",
                        "Say it plainly: stay.", "Say nothing, and put a second bowl down.", "Say that you are not ready, and mean it kindly.")
                        .after(
                                new String[]{"@You| Stay.", "@Cassia| For the report?", "@You| For the soup. Also the report.", "* {cassia.He} puts down the spoon. It is quite a short silence. You are both sure of what has been decided, and neither of you says it, and the cook who was seventy-four and is now on a coast somewhere would have said that was exactly the way."},
                                new String[]{"* You put the second bowl down without a word. Cassia looks at it for ages. Then {cassia.he} picks up the spoon, and eats, and does not leave that night, or the next night, or any night after that which anyone can recall.", "* In the morning there is a new line in the register of the Cobalt Fan Hall. Examiner, Second Class: Resident at the Crossing. Cause: Soup."},
                                new String[]{"@You| I'm not ready. I'd like to be. I'd like you to be here when I am.", "@Cassia| That's the kindest no I have ever had, and I have had a good many. I'll keep the bench.", "* {cassia.He} does. {cassia.He} sits on it every Sunday, and passes the salt without being asked, and is entirely at ease. It is the kind of friendship that is waiting, in good faith, for a different word."}))
                .done((c, st) -> { Chapters.bond(c, st, "cassia", Chapters.flag(st, "love") == 3 ? 1 : 3); if (Chapters.flag(st, "love") == 1 || Chapters.flag(st, "love") == 2) Chapters.wed(c, st, "cassia"); })
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "The Bottom of the Pot",
                "* The Long Table has a door again. Ox put it back on the first of the year, with a ceremony, a speech he forgot, and a great deal of hammering. The board over it says NOBODY IS TURNED AWAY. The door is never shut.",
                "?love=1|* Cassia keeps the register of the crossing now, and the accounts, and a small vegetable bed. {cassia.He} has never once sent a report to the Hall that was entirely true.",
                "?love=2|* Cassia sleeps in the loft over the second chimney, which is warm, and eats breakfast standing up, and goes out to the porch to read the post.",
                "?love=3|* Cassia is on the bench every Sunday, on the dot, with a book, and passes the salt without being asked.",
                "?cookoff=1|* The Jade Table still stands across the road. It is quieter than it was. Pell dines with you on the first of the month, in the green coat, and has never said why.",
                "?cookoff=2|* The Jade Table won the day and has not won since. Pell comes across on Sundays, in the green coat, and sits at the end of the long table and says nothing at all.",
                "?cookoff=3|* The Jade Table and the Long Table are still, officially, in competition. Pell's cooks eat in your kitchen on Tuesdays. Nobody has mentioned it.",
                "* The bottom of the pot has never been thrown out. It is ninety-one years old this winter. You taste it every Tuesday, with a spoon, standing, in the dark, before the others wake. It tastes of everyone who has been here, and of something just a little further down.",
                "* Across the hills, in the Sunroot Valley, a grower walks the rows at dawn, and picks a leaf of pepper from a plant that has stopped bothering. She holds it up to the light. It is very faintly, and for no reason, a different green.");
        TaleChefSeasons.add(d);
    }
}
