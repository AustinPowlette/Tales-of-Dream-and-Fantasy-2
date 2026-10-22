package Game;

/** The Orphan of the Ash Road, seasons 2 and 3 (see "Start Stories Beat Sheets.md"). The author asked for a longer story with a different mode in each tale, and for the mother to be a person in it.
 *  Season 2, "The Six Other Stars" (a mystery of a person told in six modes): Ilan (the mother) and the player look for the six other holders of the star tokens, the children of the seven houses.
 *  Each tale has its own genre and voice: comedy (Perpetua Dill), noir (Corvo Rook), horror (Wynn), slow-burn romance (Teodor / Teodora, of the other gender than the human), war (Captain Fell), fable (Mother Hesper).
 *  Season 3, "The Road Home" (cosmic): the Ash Road is the ash of the House of the Seventh Star, which the Hollow Court put out; the end of the road is the Road of Unreturned Lanterns.
 *  Flags: stone (Perpetua's cake), rook (what was done about the Grey Man), wynn (Wynn's wish), lovet (the lighthouse), ford (the camp), tale (the fable's last word), home (what became of the burned hall), lantern (Brannoch's). */
final class TaleOrphanSeasons {
    private TaleOrphanSeasons() { }

    static void add(Chapters.Def d) {
        d.flags("stone", "rook", "wynn", "lovet", "ford", "tale", "home", "lantern");
        d.place("pellmoor", "far").place("lowstreet", "near").place("manor", "wild").place("light", "wild").place("fordcamp", "wild").place("hall", "wild").place("wastes", "wild");
        d.cast("ilan", "Ilan", "Rennick").she().keep().trait(Character.Traits.Friendly).side("Good").lvl(2).age(66).at("place:shrine").rel(3);
        d.cast("perpetua", "Perpetua", "Dill").she().keep().trait(Character.Traits.Alchemist).side("Good").lvl(1).age(31).at("place:pellmoor").rel(1);
        d.cast("rook", "Corvo", "Rook").he().keep().trait(Character.Traits.Scribe).side("Neutral").lvl(2).age(44).at("place:lowstreet").rel(0);
        d.cast("teodor", "Teodora", "Vane").opposite("Teodor").keep().trait(Character.Traits.Friendly).side("Good").lvl(2).age(29).at("place:light").rel(1);
        d.cast("fell", "Captain", "Fell").he().keep().trait(Character.Traits.Leader).side("Good").lvl(4).age(52).at("place:fordcamp").rel(0);
        d.cast("hesper", "Mother", "Hesper").she().keep().trait(Character.Traits.Scribe).side("Good").lvl(1).age(81).at("here").rel(2);
        d.cast("hush", "Mister", "Hush").he().trait(Character.Traits.Leader).side("Evil").lvl(8).pot(10).age(60).at("far").rel(-3);

        // ================================================================== SEASON 2
        d.season(2, "The Six Other Stars");

        d.chapter("table", "A Table for Two").years(1)
                .setup((c, st) -> Chapters.spawn(c, st, "ilan"))
                .intro(
                        "* The house at the shrine has a roof that leaks in exactly the way she predicted and a table that wobbles in exactly the way she did not. At the head of the table sits a woman who will not be told that the second is a fault. She calls it 'balance'. She has fixed the leg three times by sticking a folded letter under it. She says that the table prefers it.",
                        "* Her name is Ilan. She is sixty-six, spare and straight and sharp as a cut loaf. She says very little, and almost all of it is an instruction. Eat. Sit. Wear the scarf. In the first month you hear the word 'love' from her exactly once, and it is about a pie.",
                        "* She has your token out on the table now, with its seven-pointed star in a ring, and beside it a sheet of paper on which she has drawn seven circles in a ring. Six of them are empty. In the seventh she has written, in her square hand, a single word: YOU.",
                        "@Ilan| There were seven houses under the Star. Seven oldest families, sworn to keep the border peace. Each had a child, in the last year of the war, and each child was given a token with a number scratched on the back. Yours is the seventh. The others I have not seen in twenty years.",
                        "@Ilan| The men who came that night wanted the tokens. I have wondered every year why. I think I am too old to wonder much longer. You will find them with me, and we will ask.")
                .wait(2)
                .outro(
                        "* She has a list. It is written in a square hand on the back of an old bill, and it has six lines. Beside each is a place and a question mark. The first says PELLMOOR, CAKES?, and the last says only: THE BLIND ONE, WHEREVER SHE'S GOT TO.",
                        "@Ilan| I'm coming with you where I can. My hip is bad. In the places where I cannot, I shall write. You will find that I am a great deal more talkative on paper.",
                        "* You do not believe this, and you are wrong. Her letters, in the months to come, will be the longest things you have ever read, and the funniest. Each of them ends with the same three words: eat something. Home.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("packing", "Ilan Packs").gap(4)
                .intro(
                        "* She packs the way she does everything, which is in a straight line, and at length, and without discussion. By the evening before you leave there is a pile on the table that has the shape of a very small hill. She goes through it for the third time with a pencil, and a list, and an expression of mild personal betrayal.",
                        "@Ilan| Scarf. Second scarf. A third in case of the second. Salt fish. A needle and thread; you will lose the needle. A second needle. A copper pan. A stone for the pan to sit on, because you will not find one. Rope, lamp oil, a small hammer. Do not argue. A small hammer has solved more problems than a large sword.")
                .wait(1)
                .outro(
                        "* You argue about the scarf. It goes on for a quarter of an hour, and it is the first real argument you have ever had with another person, and you find it odd that it should be so restful. She says that the road is cold. You say that you grew up on it. She says that this is exactly her point. At the end she tucks the scarf into your collar with two fierce tugs and says, 'It is not for you. It is so that I need not think about your neck.'",
                        "* At the door she hands you a package in brown paper. It is heavy and round and warm, and for a moment you think it is a stone. It is a loaf, still hot, wrapped in a cloth, with a note stuck in the crust with a pin.",
                        "* The note says: I HAVE NEVER DONE THIS BEFORE. I AM NOT SURE HOW. EAT SOMETHING.",
                        "@Ilan| Go on. The road is a very long way and I should like you to be out of it before the dark. I shall be perfectly all right. I have been perfectly all right for twenty years. It is a skill. I could teach it. It is not one that I recommend.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("cake", "The Wedding Cake Affair").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "perpetua"))
                .intro(
                        "* The first line on the list takes you to Pellmoor, a city of eleven bridges and a great many pastries. You have eaten nothing for two days but road. You find the shop by the smell.",
                        "* It is called PERPETUA'S, and under the name a smaller sign says WEDDINGS, WAKES, AND THINGS IN BETWEEN. In the window stands a cake the height of a man. It is white. It has seven tiers, a sugar bridge, sixty-one marzipan swans, and, at the top, a very small couple in the attitude of people who have just been told something.",
                        "* The woman behind the counter is round and flushed and floury to the elbow. She has a tape measure round her neck like a stole. She looks up as you come in and says, in a voice of great despair, 'It's the cake. Isn't it. It's always the cake.'")
                .meet("perpetua", "Speak to Perpetua Dill, the cake maker of Pellmoor.")
                .outro(
                        "@Perpetua| Dill. Perpetua Dill. I'm sorry. I thought you were from the Fenwicks. I've a wedding on Saturday, the Fenwick-Marlowe nuptials, four hundred guests, and the groom's mother has sent seventeen letters saying the swans are the wrong shade of white.",
                        "@You| Are they?",
                        "@Perpetua| They're marzipan. They're the shade of marzipan. I've told her. I've said, madam, a swan is not a colour, it is a commitment.",
                        "* The token is in your hand before you have thought about it. It is out of your sleeve and on the counter, between the icing bags and the sugar. Perpetua stops with a swan halfway to her mouth. She puts the swan down. She goes to the back of the shop, and a great many drawers are opened and shut, and she returns with a tin. In the tin, on a bed of baking paper, lies a token.",
                        "@Perpetua| Number three. My mother left it. She said it was a good luck charm. I use it to weigh down the cloth. I've been using it for twenty years as a paperweight. Oh, I'm so sorry. Is it important?",
                        "* You describe it to Ilan in a letter that night. She writes back by return of post. The letter has one line, and it says: A PAPERWEIGHT. TWENTY YEARS. I AM LAUGHING TOO MUCH TO WRITE MORE.",
                        "@Perpetua| Stay for the wedding. There's a place. I need someone to hold the sugar bridge.")
                .outChoice(new Chapters.Choice("stone", "The groom's mother has a view about the swans, and the sugar bridge is wobbling.",
                        "Hold the bridge, and keep your face straight.", "Tell the groom's mother, to her face, that a swan is a commitment.", "Eat a swan, and say nothing.")
                        .after(
                                new String[]{"* You hold it for six hours. At the climax of the toast, the bridge gives out. It is a spectacular failure, a long, slow, sugary collapse into the cream. Four hundred guests applaud. They think it is an effect."},
                                new String[]{"@Mrs. Fenwick| A what?", "@You| A commitment, madam.", "* She stares at you through the length of a held breath. She sits down. The swans, from that day, are never mentioned again."},
                                new String[]{"* It is delicious. You eat another. By the end of the evening you are the only person at the wedding who has been wholly honest about the cake."}))
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("rook", "Rook and Daughter").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "rook"))
                .intro(
                        "* It rained the whole of that week, the way it rains in the kind of city that has something to hide. It came down on the Low Street like it owed somebody money. I had a name on a page, and a page in a pocket, and the pocket was wet.",
                        "* The name was Rook, and the sign on the door said PRIVATE INQUIRIES, and under it, in a smaller hand, AND DAUGHTER, though I never saw a daughter. The room was small and smelled of damp coats and old tobacco. He sat behind the desk with his feet on it and a cigar he was not smoking. He had a face like a closed ledger.",
                        "@Rook| You're the one with the star. I heard about you from a woman in a cake shop. She sent a pigeon.",
                        "* He turned his hat over in his hands. It was a good hat, the kind that has seen things.")
                .meet("rook", "Find the detective Corvo Rook in Low Street.")
                .outro(
                        "@Rook| I had the token. Number five. My old man left it to me. He'd been a lamp-bearer for a family that was gone before I could ask. I kept it in a drawer. Three weeks ago a gentleman came in. Grey coat. Grey gloves. Very polite. He said he was collecting for an estate. He gave me a hundred crowns for it, and I took it. I need to tell you that I took it, because you'll find out, and I'd rather you heard it from me.",
                        "@You| You sold it.",
                        "@Rook| I sold it. Then he left, and I sat in this chair and the rain came down, and I thought: why a hundred crowns for a piece of tin? Nobody pays that. Not for tin. I followed him. I'm a detective. It's a vice.",
                        "* He lit the cigar at last. It took a while. His hand was not as steady as the rest of him.",
                        "@Rook| He went down to the river and gave it to a thin man in a boat. The thin man said one thing before he rowed away. He said, 'One down. Six to go.' I didn't understand it. I do now.",
                        "* Rain on the window. The long dry sound of a man putting his hat back on. A pause, in which the whole of the city seemed to listen.",
                        "@Rook| You'll want to know what I'm going to do. I'm going to find him. Not for you. For the hundred crowns. I find I don't care to be bought.")
                .outChoice(new Chapters.Choice("rook", "Rook sold his token, and is sorry, and knows where the buyer went.",
                        "Let him chase the grey man. He's earned it.", "Go with him: two hunters are better than one.", "Tell him to stay out of it. 'One down' means somebody is counting.")
                        .after(
                                new String[]{"@Rook| That's the right answer. I'll write.", "* He does. The letters come every month in the same blunt hand, and they have the weather in them and little else, which is, you reflect, a kind of faith."},
                                new String[]{"* It takes eight days and a good deal of rain, and you never catch him. You find a boat tied up under a bridge, with a lantern in the bow, burned down to nothing, and in the bottom of the boat a single grey glove."},
                                new String[]{"@Rook| I'll think about it.", "* He lights the cigar again. He is, you can tell, not going to think about it at all."}))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("manor", "The House on Marrow Hill").gap(10)
                .intro(
                        "* The third token is in a house where nobody lives, and the house is not empty.",
                        "* The letter that comes from Ilan is short, for her. It says: 'The one at Marrow Hill is a girl. She was twelve. She died of a fever the winter we ran. The family kept the house. The grey man has been there. I do not like it. Take a lamp. Eat something.'",
                        "* The manor stands on a hill above a village that has stopped looking at it. A gate that does not shut. A path that is swept every morning by no one. The windows are all lit, and there is nobody inside, and the lamps are the wrong colour: they are a faint cold green, like water seen from beneath.")
                .at("manor", "Go to the house on Marrow Hill, at {@manor}.")
                .outro(
                        "* You are expected. A table is laid in the hall for one. There is a note on the plate in a child's hand: I KNEW YOU WOULD COME. THE GREY MAN SAID YOU WOULDN'T. DON'T LOOK BEHIND YOU.",
                        "* You keep your eyes in front of you. You hear the soft sound of a chair being pulled out.",
                        "@Wynn| I'm not a ghost. I want to say so at once. I'm a girl who stayed. It isn't the same. A ghost is someone who can't go. I can go. I'm only waiting.",
                        "* She is sitting on the stairs, and she is eleven, perhaps twelve, in a nightdress that is not wet and a pair of shoes that are. She has the pale clear voice of a child who has been alone with her thoughts for years. She has put the token on the step beside her, with great care, in a straight line with the tread.",
                        "@Wynn| He came in the autumn. He stood in the hall and he said that he was very sorry for my loss. I said, which one? He did not know what to say to that. He asked for the token. I said it's mine. He said it's his, in a way. I said then he should come and get it. He didn't. He looked at the lamps. Then he went away.",
                        "@Wynn| I think he's afraid of them. The green ones. I make them. They are the only thing I'm good at. I can make a light that is only for the living.",
                        "* You pick up the token. It is warm, and not from your hand. The house gives a long sigh, like a sleeper turning over, and the green lamps along the hall dim and glow and dim.")
                .outChoice(new Chapters.Choice("wynn", "Wynn has waited a long time for someone to ask what she wants.",
                        "Ask her what she wants.", "Offer to take her with you, in the token.", "Promise to come back every year.")
                        .after(
                                new String[]{"@Wynn| I'd like to see the sea. I never did. I'd like it to be loud.", "* You say you will see what can be done. It is the sort of promise you intend to keep."},
                                new String[]{"@Wynn| Oh. Can I? I'd be no trouble. I only take up the room of a candle.", "* The token is warm for a week. In the evenings, if you listen, there is a small sound in it, like someone reading under the covers."},
                                new String[]{"@Wynn| Every year? Promise. Cross your heart. Say it properly.", "* You say it properly. She nods, and the green lamps go brighter, as if somebody had finally wound them."}))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("sea", "Wynn Sees the Sea").gap(3)
                .skipIf((c, st) -> Chapters.flag(st, "wynn") != 1)
                .intro(
                        "* You said you would see what could be done. It has weighed on you ever since: a promise to a girl who has been eleven for sixty years. She wants to see the sea, and she wants it to be loud. She lives in a brass token that you carry on a cord against your breastbone, humming faintly in the nights like a person reading under the covers.",
                        "* The nearest water is three days' walk to the east. You take it slowly. You take it on foot, because it feels wrong to take her any other way.")
                .wait(2)
                .outro(
                        "* The beach is long and grey and empty. The tide is out. There is a wind from the west that smells of salt and rotting weed, and a line of gulls standing in the shallows with their backs to it, and a sky the colour of the inside of a shell. You stand on the shingle with your hand on the token, and say, aloud, to nobody: 'Here.'",
                        "* A green light comes out of the metal and goes down the beach. It is small, and low, and glimmering, and it moves in little rushes, like a child let off a lead. It stops at the edge of the water, where the foam comes in. It stands there for a time. Then it takes off its shoes. You cannot see this. You are quite sure that it does.",
                        "@Wynn| It's so big.",
                        "@You| It is.",
                        "@Wynn| Nobody said how big. They said it was big, and I thought, like a lake. It's not like a lake at all. It goes on. It's just all the way, and it doesn't care. I thought it would be like a story. It's like an opinion.",
                        "* She wades in. The green glimmer goes out over the foam, and the waves come up round her, and she laughs, high and clear, a girl's laugh, the first you have heard from her. It is a sound that has not been used in sixty years. It breaks, and rises, and breaks again, and the gulls, one by one, turn round.",
                        "@Wynn| Louder! Can it be louder?",
                        "* The wind rises. A big wave comes in, white, with a roar like a train, and goes over the place where she is standing, and she shrieks, and the whole of the beach is full of green fire.",
                        "* You sit down on the shingle with your back against a post and stay there all afternoon. You do not take out the token to say that it is time to go. When the light fails, she comes back up the beach by herself, dripping, a thing of green and gold, and goes in, and the metal is warm, and wet, and very faintly salt.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("lamp", "Lamp Island").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "teodor"))
                .intro(
                        "* Number two is a lighthouse keeper on an island so small that the gulls need the permission of the wind to land. It is a day's boat and a night's wait from the nearest harbour, and the boatman, who has the weather-beaten gravity of a man who has refused three marriages, charges extra to pretend not to know the way.",
                        "* The light is lit when you arrive. It is lit every evening. It is the oldest rule of the island and the only one anyone obeys. The keeper is on the gallery, tiny against the sky, with a cloth in one hand and a lamp in the other. There is a look on {teodor.his} face that you have seen only in paintings, of people at the moment before they speak.")
                .at("light", "Go to the lighthouse island at {@light}.")
                .outro(
                        "@{teodor.first}| You're the one with the seventh star. I knew you'd be. We were taught the seven, in the nursery, by a nurse with a stammer. She said the seventh was lost. I always thought she'd been unkind to say it.",
                        "* {teodor.He} gives you tea in a cup with no handle, because {teodor.he} has broken the handles on all of them and has not noticed. It is the best tea you have had in a year. The lamp turns above you, and the beam goes out and over the water and comes back, and the whole room is lit, and dark, and lit.",
                        "* {teodor.He} has the second token on a cord round {teodor.his} neck, and does not take it off. You ask. {teodor.He} says: I had it since I was a child. I have worn it so long it feels like a part of me, and I do not like to part with parts.",
                        "@{teodor.first}| Stay a night. The boat doesn't come till the morning. I've not talked to anyone since the spring. I've been practising on the gulls. They're good listeners, but they have strong opinions.",
                        "* You stay three. On the second night you sit on the gallery in two blankets and watch the light turn, saying nothing, and {teodor.first} says, to the dark and to the water, in a low voice: 'It's a funny thing. I never minded being on my own. I only minded when I wasn't minding it.'")
                .outChoice(new Chapters.Choice("lovet", "The beam goes out across the water and comes back, and {teodor.first} is not looking at it.",
                        "Say what you are feeling, plainly.", "Say nothing, and move a little nearer under the blanket.", "Say you have to go, and mean it.")
                        .after(
                                new String[]{"@{teodor.first}| Plainly. Yes. That's the best way. I'm not good at the other kind.", "* It is not the most eloquent thing that has ever been said on the gallery of a lighthouse. It is, you think, the only one that has ever been heard."},
                                new String[]{"* A gull lands on the rail and regards you both with the pained expression of a chaperone. Nobody says anything at all for a good while. It is a very full silence."},
                                new String[]{"@{teodor.first}| I know. The boat comes at dawn.", "* {teodor.He} does not ask you to stay. You see {teodor.his} hand tighten on the rail, once, and then ease. The light turns. It will turn tomorrow, whatever happens."}))
                .done((c, st) -> { if (Chapters.flag(st, "lovet") == 1) Chapters.bond(c, st, "teodor", 3); })
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("letters", "The Letters of Ilan").gap(4)
                .intro(
                        "* They come by the post-rider, in a bundle tied with a red cord, one a week, and they are the longest letters you have ever received. They are written on both sides of a page, in a square clear hand without a blot. They are, for the first time in your life, an education.")
                .wait(1)
                .outro(
                        "* FIRST LETTER. 'I trust you are eating. I have been making a list of what I know about you, for there is very little, and I should like it in a place where I can read it. You prefer your tea black. You count the steps to the nearest door when you enter a room. You hum when you think nobody is listening, and you stop when you notice. You have the fastest hands I have ever seen on anyone. I did not teach you any of it. I find that I am very proud, and very cross, and the two feelings sit together in the kitchen and will not be separated.'",
                        "* SECOND LETTER. 'The roof leaked on Tuesday. I put a bucket under it. I put a second bucket under the first, for the overflow. The cat has taken the third. I know I do not have a cat. A cat has come. It is yellow and has opinions. I have named it Brannoch, for the pipe. I do not think he would mind. He would say: any cat that eats my supper is a cat of the house.'",
                        "* THIRD LETTER. 'I woke at three this morning and lit the lantern and sat in the window and could not remember what I used to do at night. All those years. I had thought it was a duty. I think now it was only a habit, and the lantern a kind of excuse for staying awake. I am not going to stop. But I have begun to leave a little oil in it for you, in case you come home and it is dark.'",
                        "* FOURTH LETTER. 'I have a dream, now and then. It is of a long table with seven chairs and I am in the one at the end. The others are empty, but not cold. They are warm, as if someone has just got up. I do not think it is a prophecy. I think it is only that I am old. All the same I have laid a place.'",
                        "* At the bottom of the fourth letter, under her signature, in a very small, careful hand, there is a postscript. It says: 'P.S. I am glad that it was you.'")
                .pay(100, 0, 0, 1, 0, 0);

        d.chapter("ford", "The Camp at the Ford").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "fell"))
                .intro(
                        "* The fifth line says CAPT. FELL, and under it, in Ilan's hand, something that has been scratched out three times and rewritten: 'He was the boy with the stutter. He made a sword out of a spoon. I hope he is alive.'",
                        "* He is alive, and he is a captain, and he is holding a ford against a force that is not on any map. The camp is a mud-coloured village of tents on the near bank, with a muddy rampart and a muddier flag. It holds four hundred men and women with the grey, patient, dangerous look of those who have not been paid for a season and have decided to be honourable about it.")
                .meet("fell", "Find Captain Fell at the camp on the ford at {@fordcamp}.")
                .outro(
                        "* He is a lean man with a bad leg and a stammer that comes and goes. He has a spoon in his belt, which he has kept for forty years for no reason that he will give. He gives you stew. It is not bad.",
                        "@Fell| I d-didn't know there were s-seven. I thought mine was a medal. I w-was given it for holding a bridge when I was nine. I've been holding things ever since. It's the only t-trade I'm good at.",
                        "@Fell| The grey men are on the far bank. They don't attack. They s-sit. They've been sitting for a month, with lanterns, and every night they put one out. Just one, by the water. Then they look at us. I think they want us to count.",
                        "* He lays the token on the map table among the pebbles. It is the sixth. That makes four you have seen with your own eyes, and one that a stranger bought for a hundred crowns, and one in your pocket. It is, you realise, no longer a hunt. It is a gathering, and someone else has begun to gather faster.")
                .outChoice(new Chapters.Choice("ford", "The grey men put out a lantern a night, and Captain Fell wants to know what to do.",
                        "Cross the ford, and ask what they want.", "Hold the line, and answer their lantern with one of your own.", "Break camp, and take the soldiers away from the ford.")
                        .after(
                                new String[]{"* You cross at dawn, alone, with your hands out of your sleeves. The water is cold. The far bank is silent, and there is a single stool on it, and nobody on the stool. A lantern burns in front of it. It is a green one. It is, you will think later, the loneliest chair you have ever seen."},
                                new String[]{"* That night, when the grey lantern goes out, Fell's soldiers light four hundred in answer, up and down the bank, in a long bright line. In the morning the far bank is empty. You suspect that they have not given up. You suspect that they have counted."},
                                new String[]{"@Fell| I d-did think of it. I never had the nerve. Nobody ever told me I could.", "* He strikes the camp in one day. It is the finest piece of soldiering you ever saw, and the quietest. When it is over, he looks back, once, at the ford, and puts the spoon away."}))
                .pay(500, 0, 0, 1, 0, 0);

        d.chapter("solroad", "Sol on the Road").gap(4)
                .skipIf((c, st) -> Chapters.flag(st, "sol") == 0)
                .intro(
                        "* You are crossing a bridge in the rain when someone calls your name from the other end. It is not shouted. It is the voice of a person who has been saying it quietly, to themselves, for a long way, and has finally got to the place where it can be said out loud.",
                        "* It is Sol. {sol.He} is thinner, and a good deal wetter, with a sack on {sol.his} shoulder and the scar on the chin that you remember. {sol.He} wears a look of half-embarrassed triumph, like a person who has bet on a long shot and had it come in.")
                .wait(1)
                .outro(
                        "@Sol| I've been looking for you since Pellmoor. I heard about the cake. Everyone in the north has heard about the cake. I thought: there's only one person that happens to.",
                        "* You sit under the bridge out of the rain, on a ledge of dry stone, with a loaf between you, and Sol tells you what the last three years have been. A lame mule. A job at a tannery that lasted an afternoon. A bout of fever in a barn. A month of being, as {sol.he} says, extremely idle in a village with a very good ale.",
                        "@Sol| I kept thinking I'd write. Then I'd think: what would I say? That I'm in a barn? That I've got a very good ale? It seemed a poor sort of thing to put on a page.",
                        "@You| You could have said that you were thinking of me.",
                        "@Sol| I know. That was the part that I couldn't write down. I'm better at the other kind of thing.",
                        "* The rain stops. The sun comes out in a long yellow bar under the arch, and a pair of wagtails land on the stones and start up their usual business. Sol looks at them. {sol.He} says, in the quiet voice of someone laying down a hand: 'Is your mother the one with the lantern? The one I've heard of? I'd like to meet her. I've always wondered what a person looks like who lights a lamp for twenty years.'",
                        "* You say that she will be furious. Sol says that this is fine. {sol.He} says {sol.he} has been disapproved of by experts.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("teller", "The Blind Teller").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "hesper"))
                .intro(
                        "* Once there was a woman who had been to the end of the world and found it had no edge. She sat down where she was, and she began to tell.",
                        "* The sixth line is the last. THE BLIND ONE. She is at a crossroads, and the crossroads is in the middle of a market, and the market is in the middle of a town you have walked through eleven times. She is under an awning, on a stool, with a bowl at her feet. She is very old. She has a face like a walnut, and her eyes are white, and she is telling a story to a crowd of children, and the children are quite still.")
                .meet("hesper", "Find Mother Hesper, the blind teller, at the crossroads market.")
                .outro(
                        "* She waits until the children have gone. She waits until the awning has been folded. Then she puts out a hand into the dusk, and finds your sleeve without looking for it, and says: 'Sit, child. I am going to tell you the one I was keeping.'",
                        "@Hesper| Once there was a house with seven lamps, and the lamps kept a fence. It was not a fence of wood, or of stone, but of small lights, and the lights stood in a ring, and outside the ring there was the Quiet. Now the Quiet is a thing with no hands. It does not break. It does not burn. It only waits, and when a light goes out it comes a little nearer.",
                        "@Hesper| Seven children were given the seven lamps to hold. Now the steward of the house was a good man, and the lord was a good lord, and the Quiet found its way in at the weakest place, which is a good man who is afraid. It said: put out one lamp, only one, and I will let the rest alone. The steward did. And then the second. And so on, down the table. When the house was dark, the lord was dead, and the lady ran, and the Quiet sat down in the empty hall and was content.",
                        "@Hesper| That is the tale. You will want to know the end. There is no end. There is only the next lamp.")
                .outChoice(new Chapters.Choice("tale", "Mother Hesper has finished, and the dusk is waiting for your answer.",
                        "Ask her what the Quiet is.", "Ask her how to light the lamps again.", "Sit with her, and say nothing, and let the story be a story.")
                        .after(
                                new String[]{"@Hesper| Ah. That is the one I cannot answer. I am old, child, and blind, and I have been told by the best liars in the world that I am wise. I know only this: it likes a name. It likes to be called a thing. Then it can be afraid of what it is called."},
                                new String[]{"@Hesper| One at a time. That is how they were put out, and that is how they must be lit. But you will need all seven, and in one place, and for one night. I have never known it done.", "* She smiles. It is a terribly young smile."},
                                new String[]{"* You sit. The market empties, and the dark comes up, and a boy goes by with a ladder, lighting the street lamps one by one. She hums a tune you cannot place. You will, years from now, in a different place, from a different mouth, and you will have to sit down."}))
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("seven", "Seven Stars at One Table").gap(8)
                .intro(
                        "* They come in over a month, by road and boat and in one case, Perpetua's, by cart, with a cake. They come to the house at the shrine, with its wobbly table and its leaking roof, and they sit in a row on benches down the wall.",
                        "* Perpetua has brought a cake. Rook has brought a hat, which he keeps on. Fell has brought a spoon. Mother Hesper has brought nothing, and has been led in by the arm of the girl who sells plums. And in the corner, in a faint green glow, where nobody quite sees her, sits a girl of eleven in a damp nightgown, with an open book.",
                        "?lovet=1|* {teodor.first} comes last, from the island, in a coat that smells of salt, and sits down next to you without being asked, and stays.",
                        "@Ilan| I did not think I would live to see this. I am not a person who says that kind of thing. I am saying it now, and you may take it as a mark of how long it has been.")
                .wait(4)
                .outro(
                        "* Five tokens go onto the table beside yours, and they lie in a ring, the stars on their faces catching the lamp. One is a copper, one is a brass, one is a pale grey metal that has never been seen on this road. The sixth, Rook's, is missing, and its place in the ring is a gap. Nobody speaks.",
                        "* Then the lantern in the window goes out.",
                        "* It does not flicker. It simply stops, as though a hand had taken it. In the dark the seven stars give off a faint, steady glow, each of a different colour, and in the glow, outside, you see a long grey figure standing in the road with its hands folded, looking in.",
                        "@Hesper| There he is. I did tell you that the Quiet likes to be named.",
                        "@Ilan| What's his name?",
                        "@Hesper| He says it's Mister Hush.")
                .pay(2000, 0, 0, 2, 0, 1);

        d.interlude(2, "Seven on the Road",
                "* The Quiet does not come in. It stands in the road for a night, and in the morning it is gone, and the lantern in the window is lit again. Nobody had lit it.",
                "?stone=1|* Perpetua writes of the wedding, in a long, smudged letter, and underlines the part about the bridge.",
                "?stone=2|* Perpetua has been banned from the Fenwick house for life, and has never been so well liked in Pellmoor.",
                "?stone=3|* The only honest person at the wedding sends you a small cake, once a year, with a swan on it.",
                "?rook=1|* A letter comes from Rook every month with the weather in it. In the last was a postscript: 'Still looking.'",
                "?lovet=1|* {teodor.first} has taken a post on the coast road, and writes that the gulls have sent their regards.",
                "?lovet=2|* {teodor.first} has written to you twice about the weather, which is how you know it is not about the weather.",
                "?lovet=3|* The island's light still turns. Some nights you see it in your mind, and it is the only thing in the dark.",
                "* The six tokens that have been gathered are in a box on the shelf, with yours, which Ilan has labelled in her square hand: DO NOT TOUCH. WE WILL NEED THESE. She has underlined the second part twice.");

        // ================================================================== SEASON 3
        d.season(3, "The Road Home");

        d.chapter("hum", "The Hum").years(1)
                .intro(
                        "* The box on the shelf begins to hum in the autumn. It is a low sound, a patient one, felt in the shelf and the table and the soles of the feet before it is heard. It sounds like seven cups being run round their rims with a finger, a long way off, all together.",
                        "* The lantern in the window gutters and lights and gutters. Outside, along the Ash Road, you see them for the first time: lights, one at every post and stone, as far as you can see. Nobody lit them. They are small and yellow and steady, and they are going out one by one, from the far end, as if a wind were coming up the road.",
                        "@Ilan| I have seen this once. Not in my life. In a dream. In a dream I had the night of the storm. I did not tell you. I did not think it was a thing that happened to the awake.")
                .wait(4)
                .outro(
                        "* The hum goes on all that month. In the market at the foot of the road, people say that the lamps along the way have started to die. Not all. One in ten, and each of them is a lamp that a person had lit for someone who was lost. A fisherman, a widow, a boy at a window.",
                        "@Ilan| The Quiet is putting out the lanterns of the Unreturned. Brannoch's will be one.",
                        "* You say nothing. You are thinking of a pipe, tapped twice on a doorframe, and a song with a fifth verse that was never found.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("soup7", "Soup for Seven").gap(2)
                .intro(
                        "* The house at the shrine has never held so many people. They are in the kitchen, on the stairs, on the doorstep with their bowls on their knees, and in the yard, under the pear tree, where Perpetua has set out a table made of two doors. It is Ilan's soup. It is a very good soup. She has been making it since four in the morning with an air of grim, glad, unconquerable purpose, in a pot that Fell borrowed from the army.",
                        "* The six of them have come back to the shrine for the winter, which nobody planned. Nobody has been asked. It simply turned out that they could not think of anywhere else to be. Rook has taken the loft and put up a sign that says PRIVATE INQUIRIES, and under it, in smaller letters, AND SOUP.")
                .wait(1)
                .outro(
                        "* They sit down together at the long table. It takes them half an hour, because someone is always getting up for the salt. Perpetua tells the story of the Fenwick wedding and nobody laughs more than Fell. Rook produces a hat, a different one, and puts it on the head of the girl who sells plums. Hesper sits between Ilan and the wall, her sightless eyes turned to the lamp. In her cracked high voice she tells the fable of the fox who sold its tail to buy a coat, and how the coat grew a tail.",
                        "* And in the corner of the room, where nobody sits, there is a glow like water seen from underneath. It is Wynn. She has a bowl of soup on her knee. She is not eating. She is holding it with both hands, for the warmth, and looking at the faces. It is the first time since she was twelve that anyone has laid a place for her.",
                        "@Ilan| Everybody has something to say, I imagine. I don't ask for speeches. I ask only that someone say grace, for I never learned how.",
                        "@Fell| I'll d-do it. I learned one for a funeral. 'For what we are about to receive, may we be a little less afraid.'",
                        "* No one says amen. They simply eat. The lamp burns, and the snow begins outside, and the pot goes round, and for one evening it is the whole of the world.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("kept", "What Ilan Kept").gap(4)
                .intro(
                        "* She gets it out of the bottom of the trunk, wrapped in oiled cloth, the way you wrap a thing that ought not to be looked at in a hurry. It is a lamp. It is small, and brass, and old. It has seven small glass panes round its rim, and in each of them, in a different colour, there is no flame.",
                        "@Ilan| I was the lamp-lighter of the House. That is what a lady of the Star was, in the days I was young: not a lady, a keeper. My husband was the lord because the lord was a man, and the world has rules about those. But the Seven Lamps were kept by the women of the seven houses, in a ring, in a hall, and every night we lit them, and every night we sang.")
                .meet("ilan", "Sit with Ilan, and hear what she kept.")
                .outro(
                        "@Ilan| I knew. That is what I have not said. I knew, the week before. I smelled it in the hall, cold and clean, like a room in which a person has just stopped speaking. I told my husband. He said that I was tired. I told the steward. He looked at me, and he looked away, and he said, quite gently: you are tired, my lady.",
                        "@Ilan| I should have put the lamp in the boy's hand. I should have made a scene. I had been brought up not to. I have been making scenes for twenty years, in my head, in a great many styles.",
                        "* She winds the cloth round the lamp again. It takes a long time. Her hands are not good.",
                        "@Ilan| I did not leave you to be safe. I want you to know that I did not leave you because it was the wiser thing. I left you because the Quiet was in the stairwell, and it was looking at you, and I could not bear to be looked at by it again.",
                        "* You take the lamp out of her hands. You hold her hands instead. There is not a great deal to be said, and neither of you says it, and the tea goes cold in the pot, and the shrine lantern, in the window, burns on.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("kettles", "Brannoch's Kettles").gap(2)
                .intro(
                        "* The wagon has been in a barn for years. It is still where you left it, behind a heap of broken carts, under a cloth, with the shafts on the floor and a family of wrens in the driver's seat. The kettles are in the back. You had thought there would be five. There are seven, all the same size, nested one inside the other like a bad joke.",
                        "* You take the cloth off. You stand and look. Gran. Prudence. The Alderman. The Duke, whom he sold, and who has come back, at some point, in some way, by the same mysterious means by which a good kettle always returns. The others you cannot name, though you can feel that he did. There is a note tucked in the lid of one, in the square slow hand. It says: THESE ARE FOR THE ROAD.")
                .wait(1)
                .outro(
                        "* You take them all. You hang them from the shafts of a new cart, and you go on the road in the spring, and you give them away. It is not planned. A woman with a baby at a crossroads needs something to boil. A man at a bridge, who has lost everything in a flood, needs a vessel. A boy of ten at a ford, whose boat has gone downriver with his dinner in it, needs a pot.",
                        "* On the road to Pellmoor a girl stands up from the verge with a smudge of soot on her nose and a dented kettle at her hip. She is twelve now, and has a mouth full of teeth. You know the kettle. It is Little Misery, which was given away at a fair. She holds it out at arm's length and says: 'I kept it. I've not once let it boil dry.' You tell her she may keep it for the rest of her life. She says she knew that. She says Brannoch told her.",
                        "@You| You knew him?",
                        "@Girl| He was at the fair. He sat on the box. He said I had a good eye for a kettle. He said I would know when I was ready.",
                        "* You have to go and look at something in the road for a moment. When you come back she is sitting on the ground with the kettle in her lap, humming. It has seven verses. She knows the fifth.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("wastes", "The Road of Unreturned Lanterns").gap(6)
                .intro(
                        "* The Ash Road does not end. This is something nobody tells you, because nobody who has been to the end has come back to tell it. It runs past the last village, and the last farm, and the last signpost. It runs on through a country of grey grass, and then it runs on through a country with no grass at all, where the sky comes all the way down.",
                        "* And there are lanterns. They are at every post. They are paper, mostly, and a few are glass, and a very few are horn, and all of them are lit by nobody. Each has a name on it. They stretch away to a vanishing point where the road meets the sky. They are going out.")
                .at("wastes", "Walk to the end of the Ash Road, at {@wastes}, and find Brannoch's lantern.")
                .outro(
                        "* It takes four days. You find it on the fifth, near a fork where the road splits into two grey lines. It is paper, and old, and the glue has gone yellow. On the side, in his square slow hand, someone has written one word: KID.",
                        "* It is still alight. It is the only one near. It burns, small and clear, and the air round it is warm.",
                        "* You sit on a stone beside it, and you do not say anything, and after a time you start to hum. It comes out cracked and low. It is the song. It has seven verses, and you know the first four, and the sixth, because he sang them in the cart, and the seventh, because it is the kind of thing a person invents. You stop at the fifth, as he always did.",
                        "* From the dark at the end of the road, very faint, a voice sings the fifth verse. It is not his voice. It is a young woman's, and a stranger's, and she is singing it with enormous care, as though she were setting down a cup. She sings it right through, to the end of the line. Then she is quiet.",
                        "* You never ask who she is. You think, with a certainty that surprises you, that it does not matter. Somebody had to know the song. Somebody had to have been told.")
                .outChoice(new Chapters.Choice("lantern", "Brannoch's lantern is still lit, and the Quiet is coming down the road.",
                        "Leave it burning where it is, and trust the road.", "Carry it home in your hands, for the shrine.", "Light your own from it, and leave the old one for the next.")
                        .after(
                                new String[]{"* You leave it. The wind, which has been walking up the road for a month, comes to the stone, and goes round it, politely, like a person stepping over a sleeping dog."},
                                new String[]{"* It is lighter than any lantern you have ever carried. You walk the whole way home with it held in front of you in both hands like a bowl of water, and it does not go out, though it passes through three rainstorms, a river, and the town of Pellmoor."},
                                new String[]{"* The new flame takes at once. You hang the old lantern on the post, where it is. It burns a little brighter as you go, as if someone had got up from a chair."}))
                .pay(0, 3, 0, 1, 0, 0);

        d.chapter("lastmile", "The Last Mile").gap(2)
                .intro(
                        "* You walk the last mile of the road at night. It is the best way to do it. There are no lanterns on this stretch, and the stars come out thick and white, and the dark is the dark of an open country, with grass in it, and the sound of insects, and a distant smell of rain.",
                        "* Rook walks with you. He does not say much. He has his hat down and his collar up and a flask in his pocket that he does not take out, for once. He has, he says, been looking for the grey man for six months, and has got to know his methods. He says that they are mostly patience.")
                .wait(1)
                .outro(
                        "@Rook| I'll tell you the truth. I was a poor detective. I never solved anything I wasn't handed. The only case I ever cracked was the one about my own father, and it turned out he'd died of a fever. No villain. No ring. A man in a bed. I think I've been waiting ever since to find out that it was someone's fault.",
                        "@You| And this one?",
                        "@Rook| This one is somebody's fault. It's a relief. It's the only time in my life I've known where to point.",
                        "* The road dips and rises. A long way ahead there is a point of grey, a thread, a cloud where no cloud should be. The grass bends toward it, the way grass bends to a wind, though there is no wind.",
                        "@Rook| I'd like it on the record that I took the token. I took a hundred crowns for a bit of tin and I'd do it again, because it brought me here. It's a poor excuse for a moral. I'll take it.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("bakes", "Perpetua Bakes for the Dead").gap(2)
                .intro(
                        "* She arrives on the eve of the lantern-night with a wheelbarrow and an apron and the stolid look of a person who will not be dissuaded. In the wheelbarrow is a cake, a vast white one, seven tiers, with a sugar bridge and sixty-one marzipan swans. It is the Fenwick cake. She has made another. She says she has not forgotten the shade.",
                        "@Perpetua| They'll want feeding. The ones who walk the road. I thought: what do you give a ghost? I thought: a cake. It's the only thing I'm any good at. Don't say anything. I am going to put it on the wall by the shrine.")
                .wait(1)
                .outro(
                        "* She sets it up on the low stone wall opposite the lantern, with a cloth under it, and a knife, and a stack of small plates. By dusk the first of them have come. You cannot see them, but you can see the cake. A slice lifts, and is gone. A plate rattles. A swan slides along the cloth, a quarter of an inch at a time, and tips off the end, and does not hit the ground.",
                        "* She cuts all night. She works with a serene attention, in the lamplight, her sleeves rolled and a dab of icing on her chin. She says something kind to each slice as it goes. 'There you are, dear.' 'Mind the sugar.' 'No, no, have the corner.' She never looks up. By morning, the cake is gone, and the plates are stacked, and the wall is covered with the most delicate scatter of crumbs in the shape of footprints.",
                        "@Perpetua| That's the best wedding I ever did. Nobody's complained about the swans.",
                        "* She leaves in the grey light, trundling the barrow down the road, with her tape-measure round her neck and her apron full of flour. She is singing. It is the song of seven verses, with the fifth in it, in a loud flat cheerful voice that you could pick out of a choir. You do not remember teaching it to her.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("hesper", "Hesper's Last Fable").gap(2)
                .intro(
                        "* She asks for you at dusk, by a child sent running to the shrine. Mother Hesper is in her bed in the room at the back, under three quilts, with the lamp turned low and the window open to the road. She is eighty-eight. Her white eyes are open. She turns her face toward the door as you come in, with the exact tilt of someone who has listened to every step in the corridor and knows which one you are.",
                        "@Hesper| I have a tale I have not told. I was keeping it. I thought I should know when. I find that I know.")
                .wait(1)
                .outro(
                        "@Hesper| Once there was a woman who told stories. She told them in the market, to children, for their pennies. And one day a man came and asked her to tell him one that was true. She said: they are all true, if you tell them properly. He said: no, one that happened. So she thought. And she told him about a girl who was given to a tinker in a storm, and who grew up on a road, and who was asked by an old man to make two friends who did not have wheels.",
                        "@Hesper| The girl made them. They were not very remarkable. A baker, and a boy who sold plums. But the girl came back to the wagon with them, and the old man looked, and was glad. And that was the tale.",
                        "@Hesper| The man said: that is not much of a story. And the woman said: it is the only one. All the rest are ways of telling it.",
                        "* The lamp gutters. Her thin hand lies on the quilt, palm up, and you take it. She turns her blind face to you and there is, in the deep creases of it, a look of the most perfect and mischievous satisfaction.",
                        "@Hesper| I knew your tinker. He told me. He came by the market, once, in the year before. He said he'd a girl in the wagon who'd never stayed anywhere long enough to be hurt. He asked if I'd a tale that might help. I said I had one, but it wasn't ready. I said the girl would have to tell me first.",
                        "* She dies in the grey hour before the dawn, quite quietly, with the window open to the road, and the sound of the first cart going by. The child who was sent for you is sitting on the floor with her back against the bed. She says, to nobody, in a small cracked voice: 'She said the end was good.'")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("hush", "Mister Hush").gap(2)
                .setup((c, st) -> Chapters.spawn(c, st, "hush"))
                .intro(
                        "* He is waiting for you at the fork. You do not know how long he has been there. He is a tall, narrow, grey person, all one tone from his hat to his gloves, with a face that is easy to forget even as you look at it. He has a long brass snuffer in one hand. In the other he has a very small cup of tea, which he sips, politely, from time to time.",
                        "@Hush| Good evening. I must apologise for the lateness of the hour. I do hate to disturb a person at their devotions. I have come about the tokens. I would ask you to hand them over. I am told you have seven.",
                        "@You| They aren't mine to hand.",
                        "@Hush| Then I must ask you to put them down, and walk away, and not look back. I find that works best. I am not a cruel man. I did not choose my work. I do it very well, and I do it without malice, and I would prefer that we were on good terms.")
                .defeat("hush", "Face Mister Hush on the Ash Road, and win.")
                .outro(
                        "?dead.hush|* When he falls it is without a sound, the way a candle burns down when you are not looking. His snuffer rolls across the grey stones and stops against the lantern. The lantern does not go out. The little cup of tea has not spilled. It is still warm.",
                        "?!dead.hush|* He steps back, and sets down the cup, and straightens his gloves. 'That was very well done,' he says. 'I am not a man who says so often.' He bows, and the grey comes up round him like a tide, and when it clears, the road is empty. He has left the snuffer, standing upright in the dust.",
                        "* On the stones, beside the snuffer, lies a brass token with a chip out of the rim. It is warm. Rook will want it back, and will pretend that he does not mind.",
                        "* The lanterns along the road have stopped going out. They flicker, and settle, and stand in their long line to the edge of the world, small and steady and yellow, like a row of children told to be good.")
                .pay(0, 0, 0, 1, 1, 0);

        d.chapter("spoon", "A Spoon for a Sword").gap(2)
                .intro(
                        "* The children come to the shrine in the evenings now. The lamp in the window is a draw. They sit on the step in a row with their chins on their knees, a dozen of them, from the farms round about, and the person they have come to see is Captain Fell. He stands in the yard with his bad leg set out in front of him and his back to the pear tree, and a spoon in his fist.",
                        "@Child| Tell it again. Tell the one about the bridge.",
                        "@Fell| I've t-told it a hundred t-times.",
                        "@Child| Again!")
                .wait(1)
                .outro(
                        "* He tells it slowly, with the stammer worse at the good parts. There was a war. There was a bridge. He was nine years old, and the boy next to him, a bigger boy, had a real sword, and was sick over the parapet at the first sight of the horsemen. He had no sword. He had a spoon from his mother's kitchen, a big wooden one with a long handle, and he had taken it because it was the nearest thing.",
                        "@Fell| I d-didn't mean to hold it. I was b-behind a wall. I put my head up and there was a m-man on a horse. He had a l-lance. I thought: he'll k-kill me. And I s-stood up. I d-didn't know what else to do. I s-stood up and I held the spoon out, like that, and I s-said, 'You can't come over.'",
                        "* The children are quite still. A smallest one has her thumb in her mouth. The captain demonstrates, with the spoon, in the dusk, with the lamp behind him: arm out, chin up, feet apart. He looks, you think, exactly as he must have looked. Ridiculous. Immovable.",
                        "@Fell| He r-reined in. He l-looked at me for the length of a p-prayer. I think he thought it was a t-trap. Then he t-turned his horse and rode away, and so did all the rest, because they all f-followed him. I held that b-bridge for three days. They s-sent me a medal. It was this s-size.",
                        "* He takes the token from his collar and holds it up to the lamp. It is the sixth. It is warm and dull, and has a chip at the rim where, he says, an arrow hit it.",
                        "@Fell| I th-thought it was the spoon that did it. All m-my life. It's the b-best thing I ever h-heard, that it was this. That I was one of s-seven. That there was a r-reason a nine-year-old should hold a b-bridge.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("burned", "The Burned Hall").gap(4)
                .intro(
                        "* The House of the Seventh Star is a field of nettles on a hill above a grey river. There is a chimney, standing alone, and the footings of a wall, and a flagged floor where the great hall was, laid with moss. A single apple tree has grown in the middle, through a crack, thirty feet high. Nobody has ever picked its fruit.",
                        "* The seven of them come up the hill at dusk. Ilan walks on your arm, because her hip is bad, and because she has said she will not be carried. Perpetua has a cake, because it seemed rude not to. Rook has a flask. Fell has the spoon. Mother Hesper is led, and Wynn goes ahead, a faint green glow in the grass, running, as if she had been told that someone had finally opened a window.",
                        "?lovet=1|* {teodor.first} comes up behind you with a coat over {teodor.his} arm, and puts it round your shoulders without saying anything.")
                .at("hall", "Go to the burned hall, at {@hall}, with the seven tokens.")
                .outro(
                        "* Ilan sets the lamp down on the flagstones in the middle of the hall. It is the first time it has been in that place for twenty years. It has seven panes, and no flame, and it sits there like a bird that has forgotten how to fly.",
                        "* One by one the tokens go into the panes. Teodor's. Perpetua's. Rook's, which was in a grey man's pocket, and is chipped now at the rim. Fell's. Hesper's, which she has carried the longest, in her sleeve, and which she lays in the lamp as though she were handing it to a child. Wynn's, small and green. Yours, last.",
                        "* The lamp lights. It does not go slowly. It goes all at once, and all the lamps, everywhere, go with it: a long clear shock of gold, running down the hill, along the road, out across the river, and over the land. The apple tree is white with blossom, in a single instant, in the wrong season.",
                        "* And in the light the hall stands. The walls are tall. The roof is a roof. There are people in it, going about their evening, with trays, and brooms, and a child in a red shawl chasing a dog. A man at the high table raises his head and says something to a woman at his side, and she laughs. For a moment, a long moment, it is a house. Then the light begins, very gently, to go.")
                .outChoice(new Chapters.Choice("home", "The seven lamps are lit for one night, and the hall is standing.",
                        "Let them rebuild it: raise the House of the Seventh Star again, stone by stone.", "Let it rest: leave the hall as a field of nettles, and keep the light.", "Carry the light down the whole Ash Road, one lantern at every post, for as long as the road goes.")
                        .after(
                                new String[]{"@Ilan| I shall not live to see it.", "@You| You'll see the first stone.", "@Ilan| The first stone is a start. I have been told that a start is the whole of the matter."},
                                new String[]{"@Ilan| Yes. This is better. I should not like it done twice. A thing that is lost is allowed to be lost, if it is lit.", "* The apple tree drops its blossom on the stones, and goes back to being a tree."},
                                new String[]{"@Fell| That's a l-lot of lanterns.", "@Perpetua| I shall make the cakes.", "* Rook says that he will do the logistics. He says it with the air of a man saying that he would rather not. He has already written the first list."}))
                .outro(
                        "?home=1|* The first stone is laid in the spring by a boy of nine, who was told to lay it straight and has done it a quarter-inch crooked. Ilan refuses to have it moved.",
                        "?home=2|* The hall is nettles and apple, and in the evenings the road is full of small lights. You never see who lights them.",
                        "?home=3|* They call it the Lamp Road. It takes eleven years. On the morning the last post is lit, at the very edge of the world, a girl in a red shawl is seen on the far end, with a dog, waving.",
                        "* The tokens go back into the lamp. The lamp goes back into a trunk. The trunk goes back under a bed in a house with a leaking roof and a table that wobbles, where a woman of sixty-nine is making soup, and counting the steps to the stove.")
                .pay(30000, 3, 0, 4, 1, 2);

        d.epilogue(
                "* The Ash Road is still the Ash Road. You still walk it. Now there is a shrine on it where a lantern burns, and a house beside the shrine, and in the house a woman who knows your step. She has opinions about your coat.",
                "?home=1|* The House of the Seventh Star has a roof now. It has a school, and a stable, and a table that does not wobble. It is not what it was. It is not meant to be.",
                "?home=2|* The hall is a field with an apple tree in it. On the eve of the first frost, strangers come from three counties to light a candle under the branches. Nobody tells them to.",
                "?home=3|* The Lamp Road runs to the end of the world. Children on it sing a song of seven verses. They think it was always the same song. It has a fifth verse now. Nobody remembers who found it.",
                "?stone=1|* Perpetua's sugar bridge is a tradition at Pellmoor weddings. They collapse it on purpose.",
                "?lovet=1|* {teodor.first} keeps the road's last light, on a coast where the world ends, and tells visitors that the other lanterns are in the way.",
                "?lantern=2|* A paper lantern hangs in the shrine's window with one word on it. KID. Ilan lights it every night, after her own.",
                "?!dead.hush|* A thin grey person sometimes stands at the edge of the road, at dusk, with a small cup of tea, looking at the lamps. He never comes closer. Perpetua leaves him a slice of cake on the wall. It is always gone by morning.",
                "* You are from the road. You are also, you find, from somewhere. It is a house on a hill, with a leaking roof. Somebody has put the kettle on.");
    }
}
