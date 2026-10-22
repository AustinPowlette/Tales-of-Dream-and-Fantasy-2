package Game;

/** The Rich Scion, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Fall of House Kessel" (court intrigue and trade farce, the dry court voice): the father's letter makes Odile's war a debt, not only an enemy. Odile's family comes apart: Cressida wants the house, Linden
 *  (always of the other gender than the human) wants out and may be loved. The suspects of season 1 are the crew of the burglary: all three are there unless the traitor was sent away or tried.
 *  Flags: stance (at the Guild), grudge (what was done with the proof of the father's theft), cressida (her offer), dinner (who was backed at Odile's table), fall (what became of House Kessel), love (Linden).
 *  Season 3, "Ten Thousand Vaults" (court intrigue and trade farce): founding the city. Flags: permit (how the permits were got), winter (what the city did in the hungry winter), vaults (the last choice).
 *  The flags has.doric / has.ysolde / has.gareth say who of the old household is still at the human's side. */
final class TaleScionSeasons {
    private TaleScionSeasons() { }

    private static void crew(Character c, Starts.State st) {
        int cul = Chapters.flag(st, "culprit"), fate = Chapters.flag(st, "fate");
        Chapters.set(st, "has.doric", cul == 1 && fate != 1 ? 0 : 1);
        Chapters.set(st, "has.ysolde", cul == 2 && fate != 1 ? 0 : 1);
        Chapters.set(st, "has.gareth", cul == 3 && fate != 1 ? 0 : 1);
    }

    static void add(Chapters.Def d) {
        d.flags("stance", "grudge", "cressida", "dinner", "fall", "love", "permit", "winter", "vaults", "has.doric", "has.ysolde", "has.gareth", "craft0");
        d.place("guild", "far").place("fair", "near").place("site", "wild").place("ostrow", "far");
        d.cast("pettifer", "Guildmaster", "Pettifer").he().trait(Character.Traits.Leader).side("Neutral").lvl(1).age(63).at("place:guild").free();
        d.cast("cressida", "Cressida", "Kessel").she().trait(Character.Traits.Merchant).side("Evil").lvl(3).pot(5).age(33).at("place:kessel").rel(-1);

        // ================================================================== SEASON 2
        d.season(2, "The Fall of House Kessel");

        d.chapter("guild", "A Seat at the Table").years(2)
                .setup((c, st) -> { crew(c, st); Chapters.spawn(c, st, "pettifer"); })
                .intro(
                        "* The Merchants' Guild meets four times a year in a hall designed by an architect who disliked chairs. It has a long table, a short gallery, and a system of motions so elaborate that, in the memory of the oldest member, only one has ever been passed, and that one by accident, during a faint.",
                        "* You have a seat now. It is the one your father had, at the end, by the draught. The man who runs the meetings is Guildmaster Pettifer, a very small, very round person in a very tall hat, who calls every vote a 'sense of the room' and every defeat a 'deferral'.",
                        "* On the far side sits Odile Kessel. She has not looked at you once. The envelope in your breast pocket is warm, like something that wants to be taken out.")
                .meet("pettifer", "Speak to Guildmaster Pettifer about the Kessel motions.")
                .outChoice(new Chapters.Choice("stance", "Odile has put three motions against your house, and the Guild is waiting.",
                        "Fight her in the open: demand a vote on all three.", "Offer terms before the vote, in private.", "Say nothing, and work against her from inside her own house.")
                        .after(
                                new String[]{"@Pettifer| A vote. In the open. How refreshing. It has not happened since the Year of the Flood, and then only because the doors were stuck.", "* The motions fail by one voice. The voice, you learn later, belonged to a man who thought he was voting on the soup."},
                                new String[]{"@Odile| You wish to talk. How civil. Your father never did.", "* She will see you. She says it as one gives a coin to a child, and you take it as one takes a coin from a woman who is counting."},
                                new String[]{"* You smile at the table. You nod at the right moments. You are, by general agreement, the dullest person present, and nobody notices that you are writing everything down."}))
                .outro(
                        "* The sitting lasts nine hours. At the sixth, a motion to table a motion to table a motion is lost on a technicality involving a missing comma. At the seventh, the Guild sings. At the eighth, Pettifer announces that the matter before the room is 'the matter before the room', and that he has no more to add.",
                        "@Pettifer| You will find we do not like to be hurried. It is a handsome feature. In three hundred years the Guild has never made a decision it has regretted, and for the very good reason that it has never made one.",
                        "* On the steps afterwards, in the cold, the Kessel carriage sits at the kerb. The door opens. Nobody gets out. Someone inside, in a plain coat, with a ledger on their knee, lifts a hand to you. It is not a wave. It is the smallest gesture a person can make and still be said to have made one.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("fair", "Linden Counts").gap(10)
                .intro(
                        "* The note says only: HORSE FAIR, THE THIRD AFTERNOON, BY THE BAD CHESTNUT. It is unsigned. It is written in a hand you have seen once, in a ledger, copying figures slowly so as not to have to do anything else.",
                        "* The horse fair is held in a field beside a river, in a smell of dung and lemonade and money. You find the bad chestnut. It is a large, soulful, ruined animal with a rolling eye and the air of an actor who has been waiting for a better part. Next to it, in the plain coat, stands {linden.first} Kessel, with a halter in one hand and a very worried look.")
                .meet("linden", "Meet {linden.first} Kessel at the horse fair.")
                .outro(
                        "@{linden.first}| I'm told I should not speak to you. I'm told this at every meal. I want to buy this horse. The man says it is sound, and I do not believe him, and I would like to see whether you will say so.",
                        "@You| It's lame in the off hind. The hock is thick. It's been fed to look sleek, and it has an old cough.",
                        "@{linden.first}| Thank you. I was hoping you would be rude about it. I'll buy it anyway. Someone must.",
                        "* The horse is bought. {linden.first} pays by counting out the price in coppers, one at a time, into the dealer's hat, in a steady and rather beautiful rhythm that makes the dealer look at his hat as though it were a stranger. You find you are watching the hands more than the money.",
                        "@{linden.first}| My mother talks about your father every night at dinner. Every night. As if he were in the room. I used to think it was hatred. I'm no longer sure. I don't know what to call it when a person has been thinking of someone for thirty years.",
                        "* The horse chooses this moment to lean its whole warm weight on your shoulder. {linden.first} laughs, and then looks, for a second, as though the laugh were not entirely permitted.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("burglary", "The Quiet Burglary").gap(8)
                .setup((c, st) -> crew(c, st))
                .intro(
                        "* The records of House Kessel are kept in a strongroom under the floor of the room where she does her sums. The house has eleven locks on the street door and a night porter who is paid not to have a favourite biscuit. You plan it in your own study with the shutters closed.",
                        "?has.gareth|@{gareth.first}| I can do the porter. I can do the porter with a plate of dumplings and a long story about my brother. He's soft. They're always soft.",
                        "?has.ysolde|@{ysolde.first}| I'll take the street door. The locks are Dunmarsh pattern, six pins. I've known their maker's wife since I was fifteen. It would be an insult to her husband if I couldn't.",
                        "?has.doric|@{doric.first}| I will do the paper. I can make any hand in the province. I would like it noted that I never thought I'd say that sentence with a clean conscience.",
                        "* It is, you reflect, an extraordinary group. Three people who once failed you, or nearly did, or were thought to, sit round a table with a tea-tray and a floor plan. They discuss a felony with the calm of a church committee on the matter of the roof.")
                .at("kessel", "Go to House Kessel at {@kessel} and read her records.")
                .outro(
                        "* The burglary takes four hours and goes almost exactly as planned, which is to say that it goes wrong in the first twenty minutes. It is rescued by the one thing nobody anticipated. The night porter is asleep with an open book on his chest, and the book is Doric's own treatise on double-entry bookkeeping, of which the porter has been fond for years.",
                        "?has.doric|@{doric.first}| I had no idea. I'm quite overcome. I'll sign it for him on the way out.",
                        "* The strongroom is not large. It is dry and orderly and smells of lavender. Along the back wall, in a long wooden chest, tied in a faded ribbon, lie the papers you came for. A thin bundle, much handled, with a title in a girl's hand, forty years old: PARTNERSHIP, SALT AND STRING.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("owed", "What Odile Owed").gap(2)
                .intro(
                        "* You read it in the strongroom, sitting on the floor, by the dark lantern, with your back against a chest. The partnership agreement is two pages long. It is signed in two hands: one sharp, one round. Below the signatures a third hand has added, in a small neat script, a clause that reads: 'And should either of us profit unfairly, let the other be told, and let them decide.'",
                        "* There are letters too. Forty years of them, never sent. Odile wrote to your father on the first of every month, in the same neat script, and put the letter in the chest. The early ones are furious. The middle ones are dry and funny. The last, dated a week before his death, begins: 'Dear Tobias, I find I have run out of things to be angry about, and I do not know what to do with the hands.'")
                .wait(2)
                .outChoice(new Chapters.Choice("grudge", "The proof that your father took her name, her contracts, and her half of the house is in your lap.",
                        "Burn the partnership and the letters: let the old man's name be clean.", "Keep them, and decide later.", "Return the stolen contracts to Odile, and everything with them.")
                        .after(
                                new String[]{"* The fire is small and orderly. You watch it go. It is the first time you have understood why your father wrote the letter the way he did, and why he could not post it himself."},
                                new String[]{"* You tie the ribbon again, exactly as she had it, and put the bundle in your coat. You will find, in the weeks to come, that you cannot sit with your back to a door."},
                                new String[]{"@Gareth| You're going to tell her. You are actually going to tell her.", "@You| I'm going to give her back what he took. Telling her is the part I can't help."}))
                .outro(
                        "* On the way out there is a small thing that is easy to miss. A chair in the corner, an old one, with a worn seat, set out of the way. A woman's shawl hangs on its back, grey and moth-eaten. Someone has put a cup beside it, with a dried ring of tea in the bottom, and on the cup, in the girl's hand, is a single initial: T.",
                        "* She has laid a place for him. She has laid it, you guess, every night for thirty years, in the one room she is certain nobody will go.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("hettie", "Hettie's Lungs").gap(6)
                .intro(
                        "* There is a girl at the vault door on a grey afternoon, small for twelve, wrapped in a shawl several sizes too big, with a cough that comes in a series, like a stair. She carries a satchel of papers. She says her name is Hettie, and that she is the granddaughter of Doric Penn, and that she would like, if it is not too much trouble, to see where her grandfather worked.",
                        "?has.doric|@{doric.first}| I couldn't stop her. She read the whole of my treatise at nine. She has views on the second schedule. She is a terror.",
                        "?!has.doric|* Her letter, which she holds out in one small damp fist, is two pages long and perfectly spelt. It says that her grandfather has written to her every month, from wherever he is, and that he has told her about you, and that she would like to see the place.")
                .wait(2)
                .outro(
                        "* She goes through the vault in a quarter of an hour, with her nose in the air. She reads the plate on every door. She wants to know why the green ledgers are on the left, and the red on the right, and who decided, and whether it might be done the other way. When she comes to the counting room, she stops dead on the threshold.",
                        "@Hettie| It's exactly the same. Grandfather said it was. He said the air smells of ink and lemon oil and a little of cheese. He said it was the best smell in the world and that I would know it. I do.",
                        "* She walks to the high desk by the window, and lays one hand on the polished wood, and the cough takes her, long and tearing, so that she bends over and holds the desk with both hands. You are beside her in a moment. She waves you off, and straightens, and wipes her mouth, and says in a thin steady voice: 'It was for this that he took the money. The healer wouldn't wait. I know.'",
                        "@You| He put it back.",
                        "@Hettie| With interest. I did the figures. He's a very good man who did a bad thing for a good reason. I've been trying to decide, for two years, which of them to write to.",
                        "* She looks up at you with the face of a clever child who has been carrying a heavy parcel a long way and is not sure that it has been right to put it down.",
                        "@Hettie| I think I'll write to the one who put it back.")
                .pay(300, 0, 1, 1, 0, 0);

        d.chapter("garden", "Odile's Beans").gap(4)
                .intro(
                        "* You go to see her on a Thursday, without a letter, which is not done. The office with no gold in it is shut. A clerk tells you, with a certain wild look, that Madam is at the back, and that she would prefer not to be disturbed, and that she has not been in the building since Monday.",
                        "* She is in a small walled yard behind the counting-house. It is a yard you have never seen. It has a pump, and a rain-barrel, and a patch of black earth about the size of a door, and in the patch, in neat rows, tied to sticks, a great many bean plants. Odile Kessel is kneeling in the middle of them in a black dress and a pair of men's gloves.")
                .wait(2)
                .outro(
                        "@Odile| They're not doing well. I want you to see that. I have never failed at anything. I have run the trade of a province for forty years. And I have planted a row of beans eleven times, and I can't get them to climb.",
                        "* She sits back on her heels. She pulls off a glove, and holds a tendril between her fingers, a limp green curl, as she would hold a wrist. 'It's a matter of the pole,' she says. 'They tell me it needs to be taller. I have put in taller poles. They sulk.'",
                        "@You| Could I look?",
                        "@Odile| You are a merchant's child. You know nothing of beans.",
                        "@You| No. But I'm a merchant's child. I know what happens when you tie something up too tight.",
                        "* She looks at you. She looks at the plants. She looks, slowly, at the strings that run from the poles to the stalks, tied in beautiful neat tight bows, each one precisely the same. She says nothing for a time. Then she reaches out, and unties one, and the little plant leans against the pole as if it had been let out of a corset.",
                        "@Odile| Thirty years. And it was the knots.",
                        "* You spend the afternoon on your knees in the black earth, the two of you, untying them one by one. It is the most peaceful afternoon you have spent in a year. She talks. She does not mention your father, or the Table, or the Guild. She talks about the weather in the year that she was nine, and a goat called Philippa, and the day she first saw a ledger and thought it the most beautiful thing in the world.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("cressida", "The Other Heir").gap(6)
                .setup((c, st) -> Chapters.spawn(c, st, "cressida"))
                .intro(
                        "* She finds you before you find her, which is how you know she is good. Cressida Kessel is a handsome, quick, restless woman of thirty-three, in an excellent coat, with her mother's eyes and none of her mother's patience. She waits in your outer office with a folder, looking at the paintings as if pricing them.",
                        "@Cressida| I do hope I am not intruding. I thought we might save each other some trouble. You want my mother's house broken. I want her house. The two are less far apart than the Guild would like to think.")
                .meet("cressida", "Hear Cressida Kessel's offer.")
                .outChoice(new Chapters.Choice("cressida", "Cressida would like to help you overthrow her mother, and take the chair afterwards.",
                        "Refuse. You will not make a house by tearing one down from inside.", "Agree. It is the quickest road, and you have been patient long enough.", "Pretend to agree, and tell Linden.")
                        .after(
                                new String[]{"@Cressida| A pity. I thought you were practical. Never mind. I shall do it without you, and then you will be dealing with me, instead of a very tired old woman.", "* She goes. You feel, for the first time, how large the house is that you are fighting, and how many doors it has."},
                                new String[]{"@Cressida| Splendid. We shall do very well together.", "* You have heard those words before, in an office with no gold in it. You are aware of that. You say yes anyway."},
                                new String[]{"* She smiles, and it is her mother's smile, the one that moves no other part of the face. You smile back, and you are surprised how easily it comes."}))
                .outro(
                        "* Cressida leaves a card, and a rosewater scent, and the distinct impression of a person who has already won an argument that has not yet begun.",
                        "* {linden.first} comes to your door that evening, uninvited, with rain on the plain coat and a ledger under one arm. They do not wait to be asked in.",
                        "@{linden.first}| I know she's been here. She has told everyone she will be running the house by spring. I want to be clear. I do not want it. I've never wanted it. I want to count things in a quiet room and go home.",
                        "@{linden.first}| But I will not watch my sister make my mother into a ledger entry. Whatever you do, please do it to her, and not to me.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("dinner", "A Dinner for Six").gap(4)
                .intro(
                        "* The invitation is engraved, delivered by hand, and signed in the neat sharp script: 'Madam Odile Kessel requests the pleasure.' It does not say of what. There is a footnote, in what looks like a different ink. It says: 'There will be soup.'",
                        "* The table at House Kessel is long enough to land a boat on. There are six places laid, and a seventh, at the foot, with a worn chair and a shawl on the back, and a cup. Nobody mentions it. Everybody sits round it as water sits round a stone.",
                        "@Odile| You are very welcome. I am told that you went through my strongroom last month. I think it was a clever plan. I think the dumplings were an unusual touch. Please eat. It would be a waste otherwise, and I do not like a waste.")
                .at("kessel", "Go to dinner at House Kessel, at {@kessel}.")
                .outChoice(new Chapters.Choice("dinner", "Odile, Cressida and Linden are at the table. A choice must be made before the soup is cold.",
                        "Side with Odile: tell her what you found, and what you mean to do about it.", "Side with Cressida: she will end the war faster.", "Side with Linden: there is an honest person at this table.")
                        .after(
                                new String[]{"@Odile| Tobias's letter. I wondered when it would be read. I suppose I should have burned mine, but the habit of thirty years does not die easily.", "* She puts her spoon down. It is the only sign. Across the table, Cressida's face does something small and private and terrible."},
                                new String[]{"@Cressida| Mother, I'm afraid I must. The house cannot be kept in a drawer.", "* Odile looks at her daughter. She does not say anything for a time that is measured in spoons. Then she says, quietly: 'Of course. I should have known it would be you.'"},
                                new String[]{"@{linden.first}| I did not ask for this.", "@You| I know. That's why.", "* {linden.first} looks at you across the candlelight for a long moment. Nobody at the table looks at anyone else."}))
                .outro(
                        "?dinner=1|* The rest of the meal is conducted in whispers, and for the first time in forty years someone laughs at Odile Kessel's table, a short, shocked sound that surprises everybody, including the person who made it.",
                        "?dinner=2|* The soup is cold by the time anyone eats it. It is the best soup you have ever tasted, and you swallow it like a verdict.",
                        "?dinner=3|* In the corridor afterwards, {linden.first} catches your sleeve. 'Thank you,' {linden.he} says. 'Nobody has ever picked me before. It's a bit like being the last person into a lifeboat.' Then {linden.he} lets go, and the corridor seems a good deal longer than it did.",
                        "* On the way out, you glance back. The old woman is sitting at the head of the table, alone, with her hand lying on the empty chair. She does not appear to have noticed.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("motion", "A Motion to Table the Motion").gap(2)
                .intro(
                        "* Guildmaster Pettifer calls an extraordinary session in the seventh week. This is unprecedented. The last extraordinary session was held in the Year of the Fire, and was adjourned because of it. The room is full, and the gallery is full, and the door is full, and a small man at the back is selling fried dough in a paper bag.",
                        "@Pettifer| The matter before the room is the matter of Madam Odile Kessel and her conduct. The question is whether a motion may be put that she be removed from the Guild without having been, in the traditional sense, notified. I should like to hear the sense of the room.",
                        "* The sense of the room is a low murmur, like a pot coming to the boil.")
                .wait(1)
                .outro(
                        "* It takes five hours. There is a motion to remove Odile Kessel. There is an amendment to remove her sister, who is not a member. There is a motion to table the amendment, which is lost on a point of order concerning who was entitled to table what. There is a motion that the Guild now sing, which is carried by acclamation, and the Guild sings, and Pettifer weeps.",
                        "* At the sixth hour, when the dust has settled and the gallery has gone home, there is a vote. It is a vote on a motion no one can quite remember. It carries, by a single voice. It is the voice of the man who sells the dough, who has come in from the back to say that it has gone cold, and has been counted as a member because he was standing in the aisle.",
                        "@Pettifer| The Guild has spoken. I am as surprised as anyone. I shall see that the minutes are circulated.",
                        "* Odile Kessel, in her black coat, in the front row, rises as the room empties, and picks up her hat from the floor. She puts it on. She looks at the Guildmaster with her head on one side, as one looks at a clock that has done something unexpected. She says: 'In thirty years, I have never been so thoroughly beaten by a procedure.' Then she smiles. It is the first time you have seen her do it with her mouth.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("auction", "The Salt Road Auction").gap(6)
                .intro(
                        "* There is a way to fight a merchant that is neither a duel nor a lawsuit, and that is to buy what she owes. Odile Kessel has seventeen outstanding notes, held by nine different lenders, and the Guild is about to put them to auction in the great hall by order of a magistrate who has not read them.",
                        "* The auction is conducted by a rotund auctioneer called Mr. Chaffinch, who has the voice of an organ and the speed of a glacier, and who is known to take a bid from anyone who raises a hand, a hat, a cane, or an eyebrow. A donkey has been brought in by mistake. Nobody has the heart to take it out.")
                .wealth(500000, "Buy up House Kessel's notes: hold 500,000 spirit stones.")
                .outro(
                        "* The bidding lasts four hours. By the fifth lot, Mr. Chaffinch has taken three bids from the donkey, one from a sleeping man, and one from a bluebottle, and has declared the bluebottle to be acting for a syndicate. You buy every note. It takes everything, and it takes, towards the end, a quantity of bluff that you would not have thought you possessed.",
                        "* When the gavel falls on the last lot, you hold seventeen pieces of paper, and across the hall, very still, in a plain black coat, sits the woman whose name is on all of them. She is not looking at the notes. She is looking at you, and in her face is something you have never seen on a merchant, and cannot at first name. It is relief.",
                        "@Mr. Chaffinch| Sold, to the lady in the hat. And the donkey.")
                .pay(5000, 0, 0, 1, 0, 0);

        d.chapter("tobias", "A Letter to Tobias").gap(2)
                .intro(
                        "* You write it in the evening, in your father's study, at his desk. It is not meant to be sent. There is nowhere to send it. You write it on a plain sheet in the plain hand he taught you, and you find that the first line is the hardest, and that it is only 'Dear Father'.",
                        "* It takes the night. You write about the stall, and the string, and the salt. You write about a girl with a round hand and a sharp mind who did the sums while he did the talking. You write that you have read her letters, all of them, and that the early ones are not what you expected, and that the late ones are unbearable. You write that you do not forgive him, and that you do not not forgive him, and that you are sitting in his chair.")
                .wait(1)
                .outro(
                        "* You never send it. You fold it in four and put it in the drawer where the envelope lay. Then you do something else. You take a clean sheet and write, in the same hand: 'Dear Madam Kessel.' You write four lines. They are very plain. You say that you have read the chest. You say that you are sorry for the thirty years. You say that you do not ask for an answer.",
                        "* The reply comes in three days. It is on a single card, in the neat sharp script. It says: 'I have been waiting for this letter for forty years. It was not addressed to me. It was addressed to him. I have decided to accept it on his behalf. Come to tea. I have the best chair for you. It is the one he used to take. I have been keeping it warm.'",
                        "* You read it standing in the hall with your coat on. You put it in your pocket. You take it out and read it again. Then you go and tell the cook that you will be out for tea, and the cook, who has been with the family for twenty years and has never been told anything in advance, drops a plate.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("last", "The Last Contract").gap(2)
                .intro(
                        "* She receives you in the office with no gold in it. She has not lit the lamp, though it is dusk. She has cleared the desk. On it lies a single sheet and a pen, uncapped, and a cup of tea gone cold, and beside the cup a pair of spectacles she does not usually wear.",
                        "@Odile| You hold my notes. You hold my house. You hold, I understand, the letters I did not send. I find that I am not the least afraid. I thought I should be. Thirty years I have spent being afraid of exactly this room, with exactly you in it, and now it has come, and I find it rather restful.",
                        "@Odile| Name your terms, child. I am too old to argue.")
                .wait(1)
                .outChoice(new Chapters.Choice("fall", "House Kessel is yours to decide.",
                        "Ruin her. Call every note, and let the house be sold.", "Make her a partner on your terms, and give her back the half your father took.", "Tear up the notes, and let her keep her house.")
                        .after(
                                new String[]{"* It takes four months. When it is done, the Kessel sign comes down from the great hall and nobody speaks of it again. You sleep badly for a year.", "?grudge=3|@Odile| And the contracts?", "?grudge=3|@You| I returned them last winter. They're in your chest, in the ribbon."},
                                new String[]{"* She signs, with a hand that does not shake. 'Fair,' she says. 'I would have asked for less.' You both pretend that it is a compliment.", "?grudge=3|@Odile| You gave back the contracts first. I noticed. It made a difference."},
                                new String[]{"@Odile| That is not what your father would have done.", "@You| No. I'm not him.", "* She laughs, short and surprised. It is the first honest sound you have heard from her."}))
                .outro(
                        "?fall=1|* In the empty hall, long after, a figure stands in the doorway of the office, with a lamp. It is {linden.first}, and it is not clear for whom {linden.he} came. {linden.He} sets the lamp on the desk. 'I was afraid I would feel nothing,' {linden.he} says. 'It turns out I feel everything. It's a surprise.'",
                        "?fall=2|* The partnership is drawn up in a week. In the margin of the final page, in a small neat script, Odile has added a clause: 'And should either of us profit unfairly, let the other be told.' She has underlined it. She does not say who it is for.",
                        "?fall=3|* The notes go on the fire, seventeen of them, in a neat pile, and the flame is tall and brief. Cressida comes in through the door as the last one goes, white to the lips, and stands very still. 'I'd have used them,' she says. Odile looks at her over the spectacles. 'I know,' she says. 'That's why I'm giving them up.'")
                .pay(10000, 0, 0, 2, 0, 1);

        d.chapter("after", "Ash on the Ledger").gap(6)
                .intro(
                        "* A year passes, and it is the odd, loose year that follows a war. Cressida has gone to the east, where a branch of the family is establishing a trade in glass. She writes. Her letters are polite and furious and extremely well-organised.",
                        "* Odile has taken up gardening, which no one expected. She keeps a plot of beans behind the office, and she is a bad hand at it. She writes to you once a week about the failures, in the neat sharp script, with a postscript that says: 'The sums are lovely this year.'")
                .wait(4)
                .outChoice(new Chapters.Choice("love", "{linden.first} has come to the door every week for a year, with a ledger, and a question.",
                        "Say it: you would like {linden.him} to stay.", "Say you would like to wait, and be sure.", "Say it is kinder not to start.")
                        .after(
                                new String[]{"@{linden.first}| I was going to ask the same thing. I had it written out. It had footnotes.", "* There are, it turns out, footnotes. You read them all. They are surprisingly good."},
                                new String[]{"@{linden.first}| Good. I'll count the days. Don't make it too many. I am no good at waiting, I only look it.", "* The ledger stays on the shelf. You do not move it."},
                                new String[]{"@{linden.first}| I thought you might. It's all right. I'd rather know. I shall go on buying bad horses.", "* {linden.He} does. You hear about every one."}))
                .outro(
                        "?love=1|* Linden Kessel moves into the house on the first of the month with one trunk, eleven ledgers, and a lame chestnut horse that is delighted with its new stable. Odile sends a bottle of plum wine with a card. The card says: 'Do not let them mind the soup.'",
                        "?love=2|* The year goes by. Neither of you says anything about it, and neither of you has changed anything, and the ledger on the shelf grows a thin layer of dust, which {linden.first} wipes off, without comment, every Sunday.",
                        "?love=3|* You hear of the horse, and the other horses. You do not hear of anything else. It is a long time before you stop looking for the plain coat in a crowd.")
                .done((c, st) -> { if (Chapters.flag(st, "love") == 1) { Chapters.bond(c, st, "linden", 3); Chapters.wed(c, st, "linden"); } })
                .pay(5000, 0, 0, 1, 0, 0);

        d.interlude(2, "Salt and String",
                "?fall=1|* House Kessel is a name on a gravestone now, and in the title of a street. The vaults were emptied into the common fund of the province. It was the most good that was ever done by a ruined merchant.",
                "?fall=2|* House Kessel and House {last} do business as one. In the first year you argue every week. It is the best part of the week.",
                "?fall=3|* House Kessel trades on, a small, careful house with a good name. Odile has stopped counting. It suits her.",
                "?love=1|* {linden.first} keeps the books at the foot of your desk and tells visitors, with a perfectly straight face, that you are the second-best accountant in the province.",
                "?dinner=2|* Cressida's glassworks are the finest in the east. She sends a crystal every year. You keep them in a cabinet and do not use them.",
                "* The old stall, a plank on two barrels with a string of salt hanging from it, still stands behind the vault, where your father kept it. Nobody ever knew why. You sit on it sometimes, in the evenings, and look at the roofs, and think about what it would have been to build a house from nothing, with someone you loved, and not break it.");

        // ================================================================== SEASON 3
        d.season(3, "Ten Thousand Vaults");
        d.cast("brindle", "Brindle", "Stonecutter").he().keep().trait(Character.Traits.Blacksmith).side("Good").lvl(1).age(45).at("here").rel(2);
        d.cast("vask", "Lord Chamberlain", "Vask").he().trait(Character.Traits.Leader).side("Neutral").lvl(2).age(68).at("place:guild").free();
        d.cast("ostrow", "Baron", "Ostrow").he().trait(Character.Traits.Leader).side("Neutral").lvl(3).age(55).at("place:ostrow").free();

        d.chapter("ground", "A Piece of Ground").years(3)
                .setup((c, st) -> Chapters.spawn(c, st, "brindle"))
                .intro(
                        "* You have more money than you need, and it sits in vaults and does nothing. You hear your father's voice, which is not a thing you expected: 'Money that does not move is a rock.' A city is the largest thing that a person can make that is not a war.",
                        "* Brindle the stonecutter has been told to find you a site. He is a stout man with an enormous moustache and arms like bridge piers. He has taken the matter with the solemnity of a man who has been waiting thirty years for someone to ask him to build something that is not a wall.",
                        "@Brindle| I've three. One's on a hill, and it's dry, and the wind will take your hat. One's by a river, and it's wet, and the river will take your hat. And one's a field in the middle of nowhere, where there is nothing at all, and it will take whatever you put there. I like that one best. It has the most room for mistakes.")
                .at("site", "Go to the field at {@site}, where the city might stand.")
                .outro(
                        "* The field is large and green and entirely empty. A line of hills at the north, a stream at the south, and above it a sky so big that you lose your sense of how small you are. Brindle walks it with a string and a bag of pegs, muttering to himself like a priest at a service in a foreign tongue.",
                        "@Brindle| Here's the market. Here's the gate. Here's where the temple would be, if you wanted one, which you don't, because nobody ever does till the plague comes. And here, in the middle, the vault. You can't have a city without a vault. It's the first thing the thieves look for.",
                        "* He drives a peg into the grass at the exact centre of the field. It makes a small flat sound. It is the first thing that has ever been put there on purpose.",
                        "@You| What do we call it?",
                        "@Brindle| Oh, that's not for me. I only lay the stones. You'll have to ask the people who live in it.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("permits", "Forms in Every Colour").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "vask"))
                .intro(
                        "* A city, you learn, requires seventeen permits, of which nine are issued by the Lord Chamberlain, and of those nine, six cannot be applied for until the others have been granted. The Lord Chamberlain is a very old man in a very new office, with a desk the size of a bedroom, who greets every petition with the words, 'I shall put it in the pile.'",
                        "@Vask| It is a question of order. I do not say it is a reasonable order. I say it is mine. You will want the Permit of Foundation. That requires the Permit of Survey, which requires the Permit of Standing, which can only be had by a person who has already founded a city. I do not make the rules. I merely enforce them in the order in which I find them.")
                .meet("vask", "Petition Lord Chamberlain Vask for the permits.")
                .outChoice(new Chapters.Choice("permit", "The Lord Chamberlain has a circle for you to get round.",
                        "Be patient: spend a season filling in every form in the correct order.", "Find the clause in the original charter that makes the whole thing unnecessary.", "Quietly make it worth his while.")
                        .after(
                                new String[]{"* It takes four months, eleven ink bottles, and a clerk with a stoop. At the end Vask stamps the last page, looks at it for a time, and says, with something like regret: 'Nobody has ever done it correctly before.'"},
                                new String[]{"* Doric's treatise on charter law has been in your study for years. You read it again, from the back. In the third schedule, in a footnote that has been there four hundred years, you find the clause. Vask reads it three times. He sits very still. 'Well,' he says. 'I never.'"},
                                new String[]{"@Vask| I have not been offered such a thing since my wedding. I shall put it in the pile.", "* The permits come through on Thursday. The pile, it appears, has a trapdoor."}))
                .outro(
                        "?permit=1|* You leave with a satchel of permits and the grim satisfaction of a person who has done it by the book. Vask sees you to the door. 'You will be a very bad city,' he says. 'You will have rules.'",
                        "?permit=2|* You leave with the charter, and the clause, and a firm instruction to keep your mouth shut. Within the week the clause is cited in eleven lawsuits across the province, and Vask is promoted.",
                        "?permit=3|* You leave with a satchel of permits and a vague sense of having stepped in something. Vask waves from the window. He seems happier than when you arrived.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("wren", "The Surveyor's Daughter").gap(3)
                .intro(
                        "* Brindle has a daughter. You would not have guessed it. He has mentioned her four times in three years, always sideways, as a man might mention a weather. She is called Wren, and she is twelve, and she arrives on the field one morning with a theodolite on her shoulder that is larger than her head.",
                        "@Brindle| I told her not to. She read my books. She read them in the night, under the blanket. She's a head for angles. I have said she's to be a seamstress, and she has said she won't, and I've said it's a fine trade, and she has said that it is, and she won't.")
                .wait(2)
                .outro(
                        "* She is better than he is. It is a terrible thing to see, for a father, and a lovely one. She walks the line of the main street in a morning with a chain, a sighting-rod, and a boy of nine to hold the end. She calls out numbers in a clear flat treble, and she is right. Her father, at the far end, with his moustache twitching, checks each one with a plumb-line and finds that it is.",
                        "@Wren| You've put the market in the wrong place. It's in the lee of the hill. It'll get all the wind off the river and none of the sun. You should move it two hundred yards east.",
                        "@Brindle| It's been on the plan since March.",
                        "@Wren| Then the plan's wrong.",
                        "* There is a very long pause. Then Brindle takes off his cap, and scratches his head with the corner of the brim, and says in a muffled voice: 'Well. That's the best news I've had all year.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("stone", "The First Stone").gap(6)
                .intro(
                        "* The first stone is laid on a cold bright morning, with a small crowd of workmen, a goat, and a man who has come to sell cakes. Brindle has painted a line on the grass in white lime. The sky is high and hard, like glass. You carry the stone yourself. It is heavier than it looks, and you carry it badly, and nobody laughs, which is how you know they like you.",
                        "?has.gareth|@{gareth.first}| Left a bit. No, your left. The other left.",
                        "@Brindle| It's the right stone in the wrong place, but the wrong place is where we're going to put the city, so it all works out.")
                .task("FOUND_CITY", 0, "", "", "Found a city of your own.")
                .outro(
                        "* By dusk there are forty tents on the field, a cookfire the size of a haystack, and an argument about whether the main street should run east to west or the other way. Someone has started a song. It has no words yet, only a tune, and you realise you have heard it before, in a different place, from the mouth of a woman you did not know until it was almost too late.",
                        "?love=1|* {linden.first} comes up the field at dusk with a ledger and a lantern, and counts the people, one by one, under {linden.his} breath, as they pass the fire. 'Forty-one,' {linden.he} says. 'With the goat. It's a start.'",
                        "* The cakeman has sold out. The goat has a name.")
                .pay(20000, 0, 0, 1, 0, 0);

        d.chapter("cityname", "What to Call It").gap(3)
                .intro(
                        "* The camp has a population of eleven hundred, a gate with no gate in it, and no name. This has started to matter. Letters addressed to it arrive marked THE CITY, or THE FIELD, or in one instance, THE PLACE WITH THE MAN WITH THE MOUSTACHE. Brindle takes the last as a compliment.",
                        "@Brindle| It wants a name. You can't have a town and not know what to put on the cart.",
                        "@You| We could ask the people.",
                        "@Brindle| God help us.")
                .wait(2)
                .outro(
                        "* The vote is held in the square, which is a patch of beaten earth between the market and the pump. A table is carried out. A box is put on the table. Slips of paper are given out by a girl of ten, and a stub of pencil is passed down the line, and a hush falls such as comes on a crowd when it is asked to be wise.",
                        "* The counting takes until midnight. There are four hundred and nine slips, and a hundred and twelve different names. There is Newfield, and Saltgate, and Hope. There is a name that is a joke, and a name that is a rude word in the old dialect, and a name that appears to be a curse. There are eleven votes for the donkey. There are nineteen for Brindle, which he reads aloud, in a voice of dread, to the end.",
                        "* The winner is none of them. It is a name that appears on seventy-three slips, in seventy-three different hands, none of them practised, all of them a little crooked. It is a plain word, which means, in the old tongue, 'the place where the road stopped, and we did not'. There is a silence when it is read. Then someone at the back begins, tentatively, to clap, and in a moment the whole square is a roar.",
                        "@Brindle| It's too long for the cart.",
                        "* It is put on the cart anyway, in white paint, by Wren, in letters a foot high. It runs off the end of the board. She has to finish it round the side.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("water", "A Winter of Wells").gap(4)
                .intro(
                        "* The city needs water. This is a thing you did not think about until the first week, when the stream ran brown from the upstream work and half the tents went down with something that looked like a cough and behaved like a rumour.",
                        "* You dig. You dig for a season, with Brindle's men and with whoever else will lift a spade, in three places at once, on a plan drawn on the back of a bill of lading. The first well hits clay. The second hits a nest of frogs, an enormous number, a chorus, a congregation, which pour out of the hole in a flood and take over the camp for a fortnight.")
                .wait(26)
                .outro(
                        "* The third well hits water. It is cold and clear and sweet, and it is nineteen feet down, and when the bucket comes up there is a silence all round the hole that is the opposite of the silence at a funeral. A boy of nine, who has been told since birth that water is something that happens to other people, drinks the first mouthful from a tin cup and bursts into tears.",
                        "* You name it for the first boy who died on the Salt Road, and the second, which was already the name of a well in your yard. There are two wells, now, in two places. Nobody asked you to. You think your father would have called it sentiment. You think he would have been quietly pleased.")
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("donkey", "The Honorary Donkey").gap(3)
                .intro(
                        "* The donkey has been in the city for a year, and there has never been a question as to what to do with it. It lives in the stable behind the town hall. It is fed by a boy of nine. It has a name, Councillor, bestowed on it by Brindle's men, and it answers, in a manner of speaking, to nothing at all.",
                        "* The matter arises over a drain. There is a vote to be taken on it in the new council, a body of eleven, and one of the eleven is ill. A clerk says, in a voice of the purest academic innocence, that the founding charter has not defined the word 'member'.")
                .wait(2)
                .outro(
                        "* The debate goes on for nine days. Counsel is retained on both sides. The motion is that the Councillor, as a resident for the requisite period, a taxpayer through the boy, and a creature of unquestionable character, be entered on the roll. A minority points out that it is a donkey. A majority replies that the Guild of Merchants once admitted a goat.",
                        "* On the tenth day the vote is taken. The donkey is led in with a rosette on its head and stands in the middle of the floor. It looks at the councillors with its long, grave, weary face, the face of an animal that has attended a great many meetings. When the clerk reads the motion, it lifts its tail and does something unseemly on the carpet.",
                        "@Brindle| I'd call that a yes.",
                        "* It is entered as a yes. By a margin of one the Councillor is admitted. It is the best attended session in the history of the city, and nobody thereafter can say that the council has never made a sound decision. The drain, on the donkey's vote, is mended in the spring. It is a very good drain.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("rival", "Baron Ostrow's Complaint").gap(6)
                .setup((c, st) -> Chapters.spawn(c, st, "ostrow"))
                .intro(
                        "* The Baron's letter is four pages long and has been rewritten at least twice. It begins in the language of a petition, grows into a protest, and ends in a kind of howl. It appears that Ostrow Town, ten miles to the west, has lost nine hundred people since the spring. They have gone to the city on the field. The Baron wants to know what you are going to do about it.")
                .meet("ostrow", "Visit Baron Ostrow, whose town is emptying into yours.")
                .outro(
                        "* He is a pink, plump, sleepless man in a velvet coat that has been slept in, and he meets you in a hall that is much too large for the number of people in it. He does not offer a seat. He paces. He is not, you perceive, a bad man. He is a frightened one.",
                        "@Ostrow| It's not your fault. I do understand that. They're better off. They're warmer. I've been asking myself why they went. They said it was the wells. They said the water here tasted of nothing, and they wanted a town where it tasted of something.",
                        "@You| Come and see the wells.",
                        "@Ostrow| I shall not. It would be too humiliating. Though I admit I am curious. Does the donkey really have a seat on the council?",
                        "@You| It's an honorary seat.",
                        "* He puts his hands over his face. He is laughing, or something like it. When he looks up he has the damp, comic, defeated look of a man who has just found out that his enemy is not very frightening after all.",
                        "@Ostrow| Perhaps we could share. A road. A market. A tax. I have a very good tax collector. He is going spare.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("bell", "The Bell of the New City").gap(3)
                .intro(
                        "* The city has a bell. It has taken a year to decide. There was a committee, then a sub-committee, then a quarrel about whether it should be a bell at all or a gong. The matter was settled by the goat, which ate the draft. It was necessary to start from scratch with a different draft and a lower opinion of the goat.",
                        "* It is to be cast in the square, in the open air, by anyone who can lift a ladle. There is a pit with a clay mould in it. There is a furnace the size of a house. There are two hundred people standing in the cold, in their good coats, in a ring, waiting for the metal.")
                .wait(4)
                .outro(
                        "* The first casting cracks. It cracks along its whole length, from the lip to the crown, with a sound like a very large plate dropped on a very hard floor. Nobody says anything. Brindle sits down on a barrel. A child begins to cry, quietly, in the way of a child who does not yet know whether it is allowed.",
                        "* The second casting takes a week, and the city does it all again, with the same people, in the same cold, with a few more who have heard. This time it holds. They lift the mould in the dawn, with ropes, and there it stands in the pit, dark and wet and ringing faintly with its own heat, and no one in the square speaks for a quarter of an hour.",
                        "* Hettie rings it, if she is there, because she is the smallest, and the bell will take the smallest hand. She stands on a box and pulls the rope with both fists. The note goes out over the roofs and the field and the hills, a deep gold sound with a wobble at the end. The city, which has been a camp, and a field, and a rumour, discovers that it is somewhere.",
                        "?has.gareth|@{gareth.first}| It's a B flat. Or near enough. I'd have gone a quarter higher. But it's honest.",
                        "* At the far side of the square an old woman with her hand on her heart says that she has not heard a bell like that since the Year of the Flood. Another says that she has never heard a bell like that in her life. They look at each other. They agree to be right, together, and go home to tell it.")
                .pay(500, 0, 0, 1, 0, 0);

        d.chapter("winter", "The Hungry Winter").gap(4)
                .intro(
                        "* The harvest fails in the third year. Not the whole of it. The grain in the east goes to blight, and the grain in the west goes to rain, and the middle goes to a plague of something small and quick that the farmers call 'the thieves'. By the autumn the price of bread has doubled. By midwinter it has trebled.",
                        "* The city is big enough now that a famine is a political event. A delegation waits in your hall in the grey light of the morning: nine men and three women in the good coats of people who have decided to be polite. Someone who has never had to bake has told them what the polite thing is.",
                        "@Delegate| We should like to know what you propose to do. We do not ask for charity. We ask for an understanding.")
                .wait(13)
                .outChoice(new Chapters.Choice("winter", "There is grain in your vault, and the city is hungry.",
                        "Open the vault and feed everyone, at cost, and take the loss.", "Sell at the price the market will bear, and use the profit to buy more from the south.", "Share it out by lot, free, until it runs out, and let the chips fall.")
                        .after(
                                new String[]{"* The grain goes out in a long, orderly line. By the new year you have lost a third of your fortune. You have not lost a single person. The ledger, which is Doric's old ledger, has a new column, headed Mouths, and the sum at the bottom of it is steadily and astonishingly large."},
                                new String[]{"* It works, in the way these things work, which is to say that it saves most of the city and leaves a hard feeling in the other part. A few of the poorest go to Ostrow Town. The Baron, who has no grain at all, feeds them anyway and sends you a note: 'You owe me one donkey.'"},
                                new String[]{"* It is the most disorderly week of your life. There are queues. There are fights. A man wins three sacks and gives two to a stranger. A woman draws the same ticket four times and declares it fate. When the grain runs out, on the ninth day, the city is still there, sitting in the street in the sun with its hat in its lap, looking sheepish."}))
                .outro(
                        "?winter=1|* On the last night of the famine, someone leaves a loaf on the steps of the vault, with a note, in a clumsy hand: 'THANKS FOR NOT BEING A ROCK.'",
                        "?winter=2|* The grain from the south comes through in March. You are not proud of the winter. You are not ashamed of it. You will return to it in your mind, a good deal, in the years to come, as to a tooth.",
                        "?winter=3|* There is a small stone in the market, now, with a ticket carved on it. Children play a game of lots, there, in the spring. The one who draws the short straw has to give away their bread.")
                .pay(0, 0, 0, 2, 0, 1);

        d.chapter("odilegate", "Odile at the Gate").gap(4)
                .intro(
                        "?fall=1|* There is a letter on the gate-post on the first day of spring. It is on a single sheet of good plain paper, written in a hand that has not wavered since the day it learned to write. It has been left by someone who did not knock. It says: 'I have heard about your well. I have not seen it. I am sorry I did not come to the opening. I am no longer a person who is received. I should like to see it before I die, from the road. Will you be at the bridge at four? O.K.'",
                        "?!fall=1|* She comes by the main road, on a hired cart, in her black coat, with a bag of beans on her knee. The sentry at the gate does not know her. He lets her through with the incurious politeness of a man who has been told to admit anyone who looks harmless. She is seventy. She is very small. She sits on the wall by the market and looks at the city that was a field.")
                .wait(1)
                .outro(
                        "* You walk her round it yourself. She has her stick and a hat and an opinion on everything, and she is good company. She takes the whole of it in as she goes: the market, the school, the bell, the two wells in the courtyard that bear two names, the half-built vault. She stops often. She looks at the stones. She says, of the drains, that she would have gone a foot deeper. She says, of the wall, that it is a very handsome wall for a town of this size and a very poor wall for a city, and she is correct.",
                        "@Odile| Your father would have built it twice as big and half as sound. I am glad that you did not. It is a good thing to see a child improve on a parent. It is rare.",
                        "* At the vault she asks if she may go in, and you say that it is hers as much as anyone's. She stands in the middle of the great empty round room, under the dome, with its smell of new stone and lamp-oil. In the very centre, on the flags, where the foundation peg went in, is a small brass plate. She reads it. It says, in letters an inch high: SALT AND STRING.",
                        "@Odile| You put it on the floor.",
                        "@You| Where the money would have been.",
                        "* She does not say anything for a time. She takes off her glove, and lays her hand flat on the plate, and leaves it there. She says, to the stone: 'It was a good barrow. It had a wobbly wheel. I used to push it. Tobias did the talking.' And she smiles, quite slowly, with her whole face, the first smile you have ever seen from her that needs no teeth.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("returns", "Back to the Books").gap(8)
                .setup((c, st) -> crew(c, st))
                .intro(
                        "* There is a woman at the gate with a bag. There is a man at the gate with a bag. There is, one way or another, a person at the gate of the new city whom you did not expect to see, standing in the rain with an old face and a new coat, looking at the walls.",
                        "?culprit=1|?fate=2|@{doric.first}| I was in a seaside town. It was very pleasant. The sea was as large as I was told. I could not stand it. I have not balanced a column in ten years. I am dying of leisure.",
                        "?culprit=1|?fate=3|@{doric.first}| They let me out in the spring. I have been teaching at a school for orphans. They are very good at sums. I have come to ask whether the city needs a treasurer. I am extremely cheap. I have a criminal record.",
                        "?culprit=1|?fate=1|@{doric.first}| I took the liberty of going over the first year's accounts. There are eleven errors. I have fixed nine and I am afraid of the other two.",
                        "?culprit=2|?fate=2|@{ysolde.first}| I have a shop across the sea. It does not do well. I am not here to ask for anything. I wanted to see what you had made out of the road.",
                        "?culprit=2|?fate=3|@{ysolde.first}| They released me in the spring. I have been three years in a very clean cell, learning to read maps upside down. I wondered if there was any honest work.",
                        "?culprit=2|?fate=1|@{ysolde.first}| The Salt Road runs to the gate of the city, now. I have been walking it for a month. It is a good road. I thought you would like to know I have not once been tempted to sell it.",
                        "?culprit=3|?fate=2|@{gareth.first}| I have a hammer and a town you've never seen. They say a smith is wanted. I did not like to ask. I said to myself, if the gate is open, I shall go in.",
                        "?culprit=3|?fate=3|@{gareth.first}| I mended the prison's iron gates for three years. It was the first honest work I'd done since I was a boy. The warden gave me a reference. I thought I would try yours.",
                        "?culprit=3|?fate=1|@{gareth.first}| The city wall needs a gate. It needs a proper gate, with a hinge. I have been thinking about it for a week. I have not slept. I have drawn forty-seven gates.")
                .wait(1)
                .outro(
                        "* You think about it for as long as it takes the rain to stop. Then you open the gate. You say nothing fine. You say, 'The kettle is on,' and stand aside, and the old face goes through, wet and shaking, and the door of the new city closes quietly behind.",
                        "?has.doric|* Doric Penn becomes the first treasurer of the city. He keeps its books for the rest of his life. They are, by common consent, the most beautiful accounts in the province, and the only ones in which a donkey is listed as an asset.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("vaults", "Ten Thousand Vaults").gap(10)
                .intro(
                        "* The city has a name now, and it is not a name you would have chosen. It was chosen by a vote of the market, on a wet Tuesday, after a debate of four hours. Brindle says it is a bad name. The goat has a statue. The population is eleven thousand, one hundred and nine.",
                        "?love=1|* {linden.first} presents the census with a flourish. 'Ten thousand one hundred and nine,' {linden.he} says. 'And a goat.' {linden.He} has been counting them since the first fire.",
                        "* On the day of the first Foundation Fair you walk out to the middle of the field and find a barrow standing there. A plank on two barrels, with a string of salt hanging from it. Nobody put it there. Nobody will say. It stands in the sun at the exact centre of the city, where Brindle put the peg.")
                .wait(2)
                .outChoice(new Chapters.Choice("vaults", "A plank on two barrels stands in the middle of the city, where the vault was meant to go.",
                        "Build the vault around it: let it be the first thing in the treasury.", "Put it at the gate, so that everyone passes it coming in.", "Leave it alone. Let it be whatever it is.")
                        .after(
                                new String[]{"* They build the vault around the barrow, stone by stone, with great care. It takes a year. When it is finished, the thing inside it is the only object in the entire treasury that is not worth anything at all, and everyone in the city knows it, and it is the thing that visitors are shown first."},
                                new String[]{"* It stands at the gate with a string of salt, and everyone who comes in puts out a hand and touches the plank. It is nobody's tradition. It is everybody's, now."},
                                new String[]{"* You leave it where it is. The grass grows round it. By the second spring, a bird has built a nest in the salt."}))
                .outro(
                        "?vaults=1|* The vault holds nothing but a barrow. The treasure of the city lies, if you like, in the thing it can never spend.",
                        "?vaults=2|* Visitors say it is a curious custom. Nobody can say who thought of it, and everyone says that they were there.",
                        "?vaults=3|* The bird raises three chicks. The city is not sure whose they are.",
                        "* On the evening of the Fair, the lamps go up along the wall, one at a time, with a cheer for each. The sound carries a long way over the field. Somewhere in the dark, an old woman in a plain black coat, who is sitting on a bench by a plot of beans, looks up from her ledger and listens to it, and then goes back to her sums.")
                .pay(50000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* The city grows. It has a market, a school, a council with an honorary donkey, and two wells named for two boys who died on the Salt Road. House {last} is the name on the gate, and nobody remembers why.",
                "?fall=1|* House Kessel is a gravestone, a street name, and a line in a schoolbook. Odile Kessel is buried next to a chair that was never sat in. You put the cup on the grave. It is the only thing you can think of.",
                "?fall=2|* House Kessel and House {last} are a single name on a single sign, and it is the oldest argument in the province. You would not swap it for the world.",
                "?fall=3|* Odile Kessel lived to be ninety-one. She took up bees at the last. She said they were better company than the Guild, and very much better with money.",
                "?love=1|* {linden.first} keeps the city's books, and yours, and the key to the vault, and a drawer of footnotes that no one is allowed to read. The horse died at twenty-six, of a surfeit of apples, and is buried in the orchard.",
                "?culprit=1|?fate=1|* Doric Penn's ledger is in a glass case in the town hall. The last page has a smudge. It is a tear.",
                "?culprit=2|?fate=1|* Ysolde's Salt Road is a paved highway now. The families of the two boys walk it every spring and say that they are square.",
                "?culprit=3|?fate=1|* Gareth hung the city gate himself. It does not creak. It has never creaked. The children believe that this is magic.",
                "* On the first of every month, a note is posted at the gate in a neat sharp hand that is not yours. It says: 'The sums are lovely this year.' Nobody has ever found out who writes it. Nobody has ever tried very hard.");
    }
}
