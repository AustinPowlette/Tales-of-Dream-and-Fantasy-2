package Game;

/** The Lamplighter of Cinder Deep, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Counting Below" (slow horror, the working voice): the tapping is a count. Below the tenth level of the mine lies an eleventh, sealed by the Company, where eleven dead miners from the old days still work
 *  their shift and ask for their wages. Something there strikes names off a tally. The widow with the short pick is the wife of one of the eleven. Choices: count (what is said back to the Counter), seal (what is done with the shaft).
 *  Flags: count, seal.
 *  Season 3, "The Floor of the Garden" (cosmic): the mine is the root of the Garden. Dig down far enough and you come up at the top of the world, at the Weaver's Wall: the world loops. The Court's sappers, under Captain Ashgrove,
 *  dig up from the other side to put it out. Mister Sable returns for the sap. Flags: root, sable, sappers. */
final class TaleMinerSeasons {
    private TaleMinerSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "count", "seal", "root", "sable", "sappers");
        d.place("palace", "start").place("wall", "far");
        d.cast("agnes", "Agnes", "Marrow").she().keep().trait(Character.Traits.Miner).side("Good").lvl(3).age(58).at("place:mine").rel(2);
        d.cast("ashgrove", "Captain", "Ashgrove").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(14).pot(8).age(52).at("far").rel(0);

        // ================================================================== SEASON 2
        d.season(2, "The Counting Below");

        d.chapter("tally", "The Tally on the Wall").years(2)
                .setup((c, st) -> { Chapters.spawn(c, st, "agnes"); Chapters.spawn(c, st, "dob"); })
                .intro(
                        "* The marks appear in the third gallery in the dry season. They are scratched into the rock at shoulder height in a column, five to a bundle, the way a prisoner scores a wall. Nobody sees who made them. They are fresh every morning, a hand's width lower than the day before, and by the end of the month there are four hundred and eleven.",
                        "* Dob counts them on the Monday. Then he counts again. Then he sits on an ore-cart with his lamp in his lap and says nothing. The workforce of Cinder Deep, children included, is four hundred and eleven. It was four hundred and eight last spring. Three have been born since. The marks have kept pace.")
                .wait(12)
                .outro(
                        "* On the Tuesday one mark is struck through. It is a clean, firm, downward stroke, with a long tail. The same day a man in the sixth gallery steps back to let a cart go by, and a loose stone the size of a loaf falls forty feet and takes him in the neck. He is called Wilf. He had two girls.",
                        "@Dob| (white, in the cage on the way up) It's a tally. It's counting us. And when it strikes you out, you're gone.",
                        "@You| That's superstition.",
                        "@Dob| Is it? Go and look at the wall. Wilf's is struck. It was a hundred and twelve.",
                        "* You go. You hold the lamp to the stone. Number one hundred and twelve has a line through it. Under the line, in very small letters, scratched with a fingernail or a nail, someone has written the name: WILF. Whoever it is, they were polite enough to spell it right.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("eleven", "The Eleventh Level").gap(16)
                .intro(
                        "* The mine has ten levels. Every miner knows it. It is on the Company's maps, in the Company's regulations, painted in white on the lintel of the cage: LEVELS 1-10. The tapping comes from below the tenth. The tally marks are scratched in a hand that is not human. On a Thursday night after the shift, Dob takes you to the very bottom of the shaft and shows you a staircase that is not on any map.")
                .wait(4)
                .outro(
                        "* It starts behind a stack of rotten timbers in the corner of the tenth gallery, in a niche of black stone. It is narrow, and very steep, and cut straight down in the living rock with a tool you have never seen. The steps are worn in the middle by feet. They have been worn very deep. Whoever used them did so for a very long time.",
                        "@Dob| I found it by chance. I fell against the timbers. I was chasing Marmalade. I went down eleven steps and there was a door.",
                        "@You| What was behind it?",
                        "@Dob| I don't know. I didn't open it. I heard someone counting on the other side. I heard it say my name.",
                        "* You hold the lamp over the stairs. A cold draught comes up, smelling of old stone and old dust and, very faintly, of something you cannot name. It is something warm and animal, like a stable. From far below, in the dark, three taps come up the stair. They stop. They begin again, and the fourth tap is yours.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("sealed", "The Company Seals the Shaft").gap(16)
                .intro(
                        "* Sable is back on the morning coach. He stands in the yard of the pit-head, in the rain, in a mackintosh, with two clerks and a man with a crowbar. He announces that, in the interests of safety, the tenth level is closed for inspection. He announces it with a smile. It is the first time in a long time that he has been seen to have a purpose.")
                .wait(8)
                .outro(
                        "* The Fellowship objects. The Fellowship has the right to inspect. Kell, standing in the door of the office with his moustache bristling, points out that the regulations allow a joint inspection by a committee of three. Sable says that the matter is covered by a clause. He produces the clause. It is in a book that is older than the town, and it says that the Company reserves the right to close any gallery that is, in the opinion of the Company, a hazard.",
                        "* They seal it that afternoon. The stairs are bricked up with eleven courses of brick, and the brick plastered over, and the plaster painted the colour of the rock. Sable stands over them, watching. He does not look at the wall. He looks at you. He says: 'It is better for everyone.' It is the only time you have ever believed him.",
                        "@Ottoline| (low, in the shed afterwards) He's afraid. He's not afraid of the men. He's not afraid of the Fellowship. He's afraid of something that's down there.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("book", "Ottoline's Book").gap(8)
                .intro(
                        "* Ottoline puts the long thin book on the table in the shed and opens it to a page near the back. She has drawn lines in red ink across the rows of the dead. She has made notes in the margins. She has been up all night. She says she noticed it when Wilf died, because he was her eleventh that year and it was an odd number. She says she began to look.")
                .wait(4)
                .outro(
                        "@Ottoline| It is not random. I have plotted it. The deaths in the pit fall on the hundredth day, to the day, from the last count. Every hundred days, someone is struck. Not always the same sort of accident. Not always the same gallery. But always, to the day, on the hundredth.",
                        "@You| Since when?",
                        "@Ottoline| Since the Company opened the mine. Ninety-one years. There are three hundred and thirty-two deaths, and they fall on a count of a hundred days, every time, like a clock. And before the first count there is a gap of about a hundred days, in the Company's own records, where the mine did not open. A gap marked: Surveying.",
                        "* She turns the page. On the last leaf she has drawn a diagram. It is a spiral. It goes down, and down, and at the bottom there is a small neat circle, and inside the circle a figure that you cannot make out. She says it is from a map she found in the Company's archive. She says it is a map of the mine as it was in the year before the Company came. She says the figure is a word.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("down", "Down").gap(16)
                .intro(
                        "* You break the wall on a Sunday night, with Dob and Ottoline and the widow, who has insisted. You take a pick, a crowbar, forty feet of rope, a thermos of tea and a lamp apiece. The brick is thick but old. The mortar is soft. It comes away in a dusty sheet, a course at a time, and the stair opens before you: black, steep, silent. The draught that comes up from it smells of a stable.")
                .at("deep", "Go down the sealed stair to the eleventh level, {@deep}.")
                .outro(
                        "* The stair is two hundred and eleven steps. You count them, because there is nothing else to do. At the bottom there is a door, round, of dark wood, bound with bronze. It is not locked. It is not even closed. It stands an inch ajar, with a thin line of yellow light along the edge. And through it, quite clearly, comes the sound of voices: a murmur, a clink, the rattle of tin mugs, the ordinary hum of a shift going off.",
                        "@Agnes Marrow| (the widow, for the first time in a year, in a trembling voice) That's Tom.",
                        "@You| Who?",
                        "@Agnes Marrow| My husband. He went down in 'forty-nine. They never found him. That's his laugh.",
                        "* She puts her hand on the door, and pushes, and it swings open on a long low gallery, lit by lamps. It is full of men.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("gallery", "The Gallery of the Eleven").gap(8)
                .intro(
                        "* They are eleven in number, in old canvas coats and caps of a cut that went out before your grandfather's day. They sit on benches along the wall, eating bread and cheese out of handkerchiefs, drinking tea from tin cans. A pit-pony is asleep in the corner. The lamps burn with a steady yellow flame. It is a perfectly ordinary mine at the end of a shift, except that the shift has been going for seventy years.")
                .wait(4)
                .outro(
                        "* They look up when you come in. They do not look surprised. A broad old man with white whiskers and a face like a friendly ham rises, wiping his hands, and says in a deep voice that they were beginning to wonder. He says the foreman has been late with the wages for minutes. He says it is not like the Company. He asks if you are the new man.",
                        "@Tom Marrow| Evening. I'm Tom. We're the Eleven. We came down in 'forty-nine for the day shift, and the roof came in behind us, and the lads said, never mind, we'll sit it out. We've been waiting for the relief ever since. They've forgotten us, I think. Would you tell the foreman we'd like our money?",
                        "* Agnes makes a sound you will never forget. She is across the gallery before anyone can stop her. She takes his face between her hands. She says his name. He looks at her, politely puzzled, in the manner of a man who has been spoken to by a stranger. Then he says, slowly, in a different voice, 'Aggie?' And all along the bench, the others set down their cans.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("counter", "What Counts").gap(8)
                .intro(
                        "* The Eleven tell it plainly, over the tea. The roof fell in, they say, and they were cut off, and they sat in the dark. After a time they heard a sound in the rock. It was a slow regular tapping, like a heart. It said, in a voice they could feel in their ribs: I am keeping count. They have been counted ever since. They do not grow old. They do not grow tired. They do not die. They have not seen daylight in seventy-two years.")
                .wait(4)
                .outro(
                        "@Tom Marrow| It doesn't mean harm. I'll say that. It's like a clerk. It writes us down. When one of the lads upstairs goes, it marks him off, and we feel it, down here, a sort of cold in the chest. We call it the Counter. It's not a bad sort. It's only that it has nobody to tell.",
                        "@Ottoline| (quietly, to you, with her hand over her notebook) It's a record. It is the world's oldest ledger. Somebody is keeping track of the living, and has been since before the Company.",
                        "* The wall at the end of the gallery is covered with marks. They run from floor to ceiling, in bundles of five, in tens of thousands. Some are struck out. Most are not. At the very bottom, in a corner, in very small tidy scratches, there is a column of names. It begins with Tom Marrow. It continues for eleven lines. It ends, very recently, with a twelfth: yours.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("struck", "A Name Struck Out").gap(8)
                .intro(
                        "* On the hundredth day after Wilf, a Tuesday, Dob does not come up. The cage rises at the end of the shift, and he is not in it. He was at the face of Number Nine with a lamp. He was alone for a moment. A man heard the pick stop. Then there was a sound like a sigh, and then nothing. You are on the stair before anyone has finished speaking.")
                .wait(4)
                .outro(
                        "* You find him in the gallery of the Eleven, sitting on a bench, quite calm, with a cup in his hands. He looks up with a puzzled smile. He says that he came down for a drink. He says he thinks he must have taken a wrong turn. He cannot remember the stair. On the wall behind him, in the column of names, a line has been drawn through the word DOB. It is a long clean downward stroke, very black, and still wet.",
                        "@Tom Marrow| (gravely) It's been struck. I'm sorry, lad. He's with us now. He'll be quite comfortable. We'll show him the ropes.",
                        "@Dob| Oh. Is that what's happened? That's not so bad. I thought it would be worse. I'd like to tell Marmalade. Is there somewhere I could send a note?",
                        "* He is smiling. He is entirely at ease. He has the fixed bright calm of a man who has been given very good news. You take his arm. It is warm, and solid, and quite alive. You say that he is coming with you. He says, kindly, that he does not think he can. You can see the faint mark, on the back of his hand, where the line has been drawn.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("widow", "Agnes Speaks").gap(8)
                .intro(
                        "* Agnes Marrow has not spoken in a year. She speaks now. She sits with you in the gallery, at the end of the bench, with her short pick across her knees and her husband's cap in her lap, and tells you, in a low steady voice, what she knows. She has been listening to the tapping since the day he went. She says she understands it. She says it is not a language. It is a counting, and a counting is a kind of kindness.")
                .wait(4)
                .outro(
                        "@Agnes| It's not to hurt us. It's to keep us. A person who is counted is a person who is not forgotten. Do you understand? That's all it's ever done. It couldn't bear that nobody was keeping track. It's been writing us down since the beginning, in case we're needed. It's only that it writes us down in a ledger that has one column. It doesn't know about the other.",
                        "@You| What other?",
                        "@Agnes| The one that says: going home.",
                        "* She stands. She puts the cap on her husband's head, gently, over the white hair. She looks at the wall of marks. She says that she thinks the way to stop it is not to fight it. She says she thinks the way to stop it is to answer. She says: 'It's been counting alone for ninety years. Somebody ought to count back.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("count", "Counting Back").gap(8)
                .setup(TaleMinerSeasons::mark)
                .intro(
                        "* You do it in the gallery of the Eleven at midnight, by lamplight, with the whole of the Fellowship standing in the stair behind you and every lamp turned up. Ottoline has the book. Kell has the names. Agnes stands at the wall, with her hand flat on the rock, as a woman stands at a bedside. You take a piece of chalk. You write, under the Counter's last scratch, in a clear bold hand, a number.")
                .made(10, "Answer the Counter: make ten marks.")
                .choice(new Chapters.Choice("count", "The Counter has been counting for ninety years. You can answer it with a count of your own.",
                        "Count the dead: read out every name struck, and say that they were here.", "Count the living: read out every name on the town's roll, and say that they are not yours.", "Do not count: put down the chalk, and say plainly that a person is more than a tally.")
                        .after(
                                new String[]{"* You read the names of the dead. All three hundred and thirty-two. It takes an hour and a half. Kell reads half, and Ottoline the rest. At every name the knocking in the rock answers, once, like a hand on a door. When you finish there is a long silence. Then, from every side, the rock begins to hum, very softly, as a church organ does when the last note has been played and the pipe is still sounding.", "@Agnes| (very quietly) There. That's the first time it's been thanked."},
                                new String[]{"* You read the living. Four hundred and eleven names, and the babies born that spring. You say after each, 'Here,' and the Fellowship says it after you, and the rock says nothing. When you reach the end you say, aloud, that none of them belongs to the Counter. You say that they belong to themselves. For a moment the tapping stops. Then it begins, much faster, anxiously, like a clerk who has lost his place.", "@Kell| I'd say you've upset it. I'd say it deserved it."},
                                new String[]{"* You put down the chalk. You say that a person is not a number. You say that you do not mean to be struck out, or kept, or counted at all. The wall is silent. You can hear your own breathing. Somewhere in the rock, a very long way off, a sound begins, a low grinding groan, like a great stone shifting. It is the sound of something that has never been refused.", "@Ottoline| (whispering) I think we've hurt its feelings."}))
                .outro(
                        "* The marks on the wall begin to fade. They go one after another, in order, from the top. The scratches soften, and blur, and dissolve into the rock like breath on glass. When the last has gone, the wall is smooth, and grey, and quite blank. The tapping has stopped. The Eleven stand up from their benches. They look about, in puzzlement, like men who have woken from a long afternoon sleep.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("seal", "The Seal").gap(8)
                .intro(
                        "* They are free to go. That is what it comes to. The Eleven are free to climb the stair, one by one, in their old coats, and walk into daylight, and be old, and die, and be buried. They stand at the foot of the stair for ages. Tom Marrow is the first to speak. He says he would like to see the sun. He says that after seventy-two years he is not at all sure he would know it.")
                .wait(4)
                .choice(new Chapters.Choice("seal", "The stair to the eleventh level stands open, and the Company's bricks lie in a heap.",
                        "Seal it for good: brick the stair up again, with the Eleven's names on the lintel.", "Open it to the town: make the eleventh level a place of memory, with a lamp.", "Take the Eleven up: lead them into the sun, one by one, and let the mine go quiet.")
                        .after(
                                new String[]{"* You seal it. It is done with care, by the Fellowship, in daylight, with eleven courses of good brick. The names are cut on the lintel by a mason from the city: TOM MARROW, AND TEN OTHERS. Beneath, in smaller letters: THEY WERE COUNTED. The Counter does not tap again. Whatever it was, it is at rest, and the town does not speak of it, and the lamp at the pit-head burns a little brighter.", "@Kell| I'd rather it was sealed. I'll sleep better. I don't know that I'll sleep well."},
                                new String[]{"* You open it. The stair is cleaned and lit with a string of lamps. The eleventh level becomes a place of pilgrimage, a gallery with eleven benches and a plain table, where on the first Sunday of the month the people of Cinder Deep go down and sit with their dead. It is quiet, and warm, and not at all afraid. Children learn to count there, on the wall, with chalk.", "@Ottoline| It is the only mine in the world with a visiting day. I rather like it."},
                                new String[]{"* You take them up. It takes a week. They come out one by one into the grey morning, blinking, in their canvas coats, and stand in the yard in front of the whole town. The last is Tom Marrow, on his wife's arm. He lifts his face. The sun is behind a cloud. He says: 'Is it supposed to be so bright?' Then the cloud moves, and he laughs, a great deep laugh, and sits down in the road.", "@Agnes| (holding him up, weeping) It's all right, Tom. It's only the morning."}))
                .outro(
                        "* The Company's flag is taken down from the pit-head the next day by a party of clerks and not replaced. It is a quiet, slightly embarrassed ceremony. The wheel goes on turning. Down in the lowest gallery, where the Counter's last mark was rubbed out, someone has chalked three words on the bare rock, in a large rough hand: SHIFT ENDED. THANKS.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("change", "Shift Change").gap(24)
                .intro(
                        "* Tom Marrow dies in the spring, quietly, in a bed in the infirmary, at the age of ninety-eight by the Company's reckoning and thirty-three by his own. He dies with his wife on one side and Dob on the other, holding the pony's old halter. He asks for a cup of tea. He drinks half of it. He says, 'That's the best I've had in seventy years,' and closes his eyes.")
                .realm(2, "Take the next step: reach Major Lvl 2.")
                .outro(
                        "* They bury him in the churchyard on the hill, with the other ten, in a row. The whole of Cinder Deep comes. They stand in the long grass in their canvas coats with their lamps lit, in the sun, which is a thing nobody has ever seen at a funeral. Ottoline reads the names. Agnes puts the cap on the coffin. Dob rings the pit bell, once, slowly, as Tom would have wished.",
                        "@Kell| (afterwards, low, at the lych-gate, to you) I've been in this mine thirty-seven years. I thought I knew what was down there. I've been wrong about almost everything. I find I don't mind.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "Shift Ended",
                "* The tally is gone from the wall. The tapping in the rock has stopped. For the first time in ninety-one years nobody in Cinder Deep is counted by anything but a clerk.",
                "?count=1|* On certain nights at the turn of the shift you can hear the rock hum a long low chord, softly, and every miner stops, and lifts his head, and nods.",
                "?count=2|* The Counter has gone quiet. Some say it is sulking. Dob leaves a lamp lit at the bottom of the shaft on the first of every month, in case it wants company.",
                "?count=3|* Something in the rock groans on a Thursday evening, once a month, and then is still. Nobody has said what. Everybody knows.",
                "?seal=1|* The lintel at the sealed stair reads THEY WERE COUNTED. On the first Sunday of the month somebody leaves a cup of tea at the foot of it.",
                "?seal=2|* The Gallery of the Eleven has a visiting day. A child sits on Tom's bench with a piece of chalk and writes his own name, and counts to eleven.",
                "?seal=3|* The Eleven lie in a row on the hill. There are twelve stones. The twelfth is blank, and has a lamp on it.",
                "* Far below, at the very bottom of the mine, under the last scratch of the vanished tally, in the dark, the rock is warm. If you put your ear to it, you can hear, very faint, not a tap, not a count, but something much slower. It is the sound of a very large thing, turning over in its sleep.");

        // ================================================================== SEASON 3
        d.season(3, "The Floor of the Garden");



        d.chapter("warm", "The Rock Is Warm").years(2)
                .intro(
                        "* The warmth starts in the autumn. It begins in the lowest gallery, where the last tally was rubbed out, and creeps up through the rock a foot a month. By midwinter the walls of the tenth level are as warm as a loaf. The ice on the pit-head melts. The ponies sweat. Dob takes off his coat in January, and comes to work in his shirt, and says it is like working in a kitchen.",
                        "* Ottoline measures it with a thermometer and a notebook. She goes down every day. She sits on a barrel in the lowest gallery with her ear to the rock, and her instruments laid out on a cloth, and writes. She says it is not the sun. She says the mine is breathing.")
                .wait(12)
                .outro(
                        "@Ottoline| It has a pulse. Fifty-two beats a minute, slow and steady, and very strong. It comes from below. It comes from a long way below. I've taken readings from the sixth level and the tenth, and the pulse arrives at the sixth seven seconds later. I make the depth of the source about eleven thousand feet.",
                        "@You| Eleven thousand? The mine is only four hundred.",
                        "@Ottoline| Quite. It is not our mine. We have been working the top of something else.",
                        "* She turns the notebook round. She has drawn a long thin tree, upside down, with its branches spread under the town and its trunk going down and down off the bottom of the page. At the top, small and neat, is the pit-head. She has labelled it: Twig.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("dig", "Digging Down").gap(16)
                .setup(TaleMinerSeasons::mark)
                .intro(
                        "* The Fellowship votes to dig. It is not a long debate. Kell stands up and says he has worked four hundred feet down for thirty-seven years and would like to know what is under it. Dob says he would like to know what is warm. The widow says nothing, but she has already brought her pick. The vote is carried by every hand in the shed except one, and that one belongs to a man who is asleep.")
                .made(20, "Sink the new shaft: make twenty props.")
                .outro(
                        "* It takes four months. They sink it in the old fashion, with a windlass and a bucket, a foot at a time, every hand in turn. The rock is dense, and dark, and strangely smooth. It does not splinter. It comes away in long curling shavings, like wood, and smells of resin. Where the pick goes in it bleeds a little clear gold sap that runs down the face and sets like amber.",
                        "* On the hundredth day, when the shaft has reached the depth of a church spire, the windlass runs slack. The bucket comes up empty and clean. Dob leans over the edge with a lamp and cannot see the bottom. He says: 'I think it's gone through.' Ottoline says nothing. She has gone completely white. She is holding up a hand, as a person does at a concert when the music is about to begin.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("through", "Up Is Down").gap(8)
                .intro(
                        "* They go down in the cage on the next morning, the five of you, with ropes and lamps and a bag of bread, and at the bottom there is not a gallery but a window. It is a round hole of blue light in the floor of the shaft, four feet across, with a faint breeze coming up it that smells of grass. Kell goes first, on a rope, with a face of grim resolve. He goes through. He does not fall. He stands, upside down, on the air.")
                .at("wall", "Go through the opening at the foot of the shaft, to {@wall}.")
                .outro(
                        "* It is daylight. You stand on a mountain top in a cold clean wind. The sky is wide and pale and the sun is a long way to the left of where it should be. All round you the world falls away in blue folds, and far below, on the plain, tiny and exact, you can see a grey town with a wheel on a tower. It is Cinder Deep. It is a thousand miles below, and it is directly overhead.",
                        "@Dob| (in a whisper, on all fours) We've dug down and come up on the top of the sky.",
                        "@Kell| That is not possible. That is geometrically absurd.",
                        "@Ottoline| (with a little hysterical laugh) No. It's a loop. I've read of it. It's a thing that happens at the edge of a made world. If you go far enough in any direction you come back to where you began. We have dug through the floor of the Garden, and out into its roof.",
                        "* A long way off, at the edge of the plain, there is a wall. It runs from horizon to horizon, grey and high and as fine as a weaver's cloth. You can see, from here, the threads.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("weaver", "The Weaver's Wall").gap(8)
                .intro(
                        "* The Wall is the edge of the world. It is woven, not built. It is a vast flat cloth of grey thread, a hundred feet high, stitched in a pattern of interlocking squares, and when you put your hand to it you can feel it breathe. There is no end to it. It runs away into the distance in both directions. Where you touch it, the squares tremble and close up again, like water.")
                .wait(4)
                .outro(
                        "* There is a door in it, a little way along. It is small and low and brown, with a latch of horn. It has been left ajar. You peer through. On the other side is a plain, with wheat on it, and a road, and a town with a wheel on a tower. It is the same plain as the one you are standing on. It is the same road. If you went through, you would be where you began.",
                        "@Ottoline| The Garden is a loop. That's all it is. It has no edge. You walk to the border and the border puts you back. It is the neatest thing I have ever seen, and I have never been so frightened.",
                        "* The widow Agnes has put down her pick. She is standing with her hand against the weave of the wall, staring at it, with her lips moving. After a time she says: 'It's counting, too. Listen.' You listen. Very faint, from the threads of the Wall, deep in the weave, there is a ticking, regular and slow, three and a pause, three and a pause.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("ashgrove", "Captain Ashgrove").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "ashgrove"))
                .intro(
                        "* The man who comes out of the Wall is square and quiet, with a face like a closed fist. He wears a plain grey coat with a captain's braid, and a sapper's leather apron, and carries a short spade over one shoulder. A line of men and women in similar grey comes after him, silent, in file, each carrying a pick and a coil of fuse. They stop. They stand at ease.")
                .meet("ashgrove", "Speak to the Captain who has come out of the Wall.")
                .outro(
                        "@Ashgrove| Captain Ashgrove. Sappers of the Hollow Court. I should say at once that we are not here for you. We are not here for the town. We have been digging up this floor for eighty years, from the other side, to put out the root. You have been so good as to dig down to meet us. I am rather obliged.",
                        "@You| The root?",
                        "@Ashgrove| The thing you have been calling the vein. The Counter. The warm. It is the Garden's root, the oldest part. It is the one place in the Garden that touches the real earth. If it is cut the Garden will wither from the bottom. We have a good many men down there, and the work is slow. You are three weeks ahead of schedule.",
                        "* He looks at the five of you in your canvas coats with your lamps and your bread. He nods, slowly, as a man nods who has reached a decision he does not like. He says: 'I shall be perfectly honest. I would rather not do it. But I have my orders, and I have never in my life disobeyed one.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("tunnel", "A Tunnel War").gap(8)
                .setup(TaleMinerSeasons::mark)
                .intro(
                        "* It is a war of props. The Court's sappers dig up, and the Fellowship digs across, and every gallery is a race to hold the roof before the other side takes it down. There are no soldiers. There is no shouting. It is a patient scrupulous contest in the dark, fought with timber and rope, and the only casualty is time. The Captain sends a note each morning by a corporal. It says: Good morning. We have advanced a yard.")
                .made(15, "Hold the line underground: make fifteen props.")
                .outro(
                        "* On the ninth day you are working the same gallery from two sides without knowing it. You set a prop and hear a prop set. You set another and the sound answers. at last you reach the middle, a pair of hands on either side of the same beam, and a pair of eyes meet yours through a gap in the rock. They belong to a sapper of eighteen with a dust-grey face and the weary politeness of a boy brought up in a barracks.",
                        "@Sapper| Excuse me. Is this your beam or ours?",
                        "@You| I think it's both.",
                        "@Sapper| That's what I thought. I'm not sure what we do.",
                        "* You pass him a flask of tea through the gap. He takes it with both hands. He drinks. He passes it back. Neither of you says anything for a time. Down the tunnel the sound of picks has stopped, on both sides, and the only noise is the soft crack of the timber settling under the whole world.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("sap", "The Sap").gap(16)
                .intro(
                        "* Ottoline comes up from the lowest gallery with a flask in her hand and a look on her face you have never seen before. It is the look of a person who has found, in a very old ledger, an entry in her own handwriting. She says she has analysed the amber. She says she has tried it on a dying rat. She says the rat is now eleven years old and has taken up with a hamster.")
                .friends(8, "Share the sap's secret: make eight friends.")
                .outro(
                        "@Ottoline| It is life. It's not a cure. It's what cures come from. Every herb, every pill, every medicine in the Garden is a thin dilution of this. It's the Garden's own blood. It's the stuff the world is made of. I've been practising medicine for twelve years and I've never once understood what it was for.",
                        "* She gives a drop to the old stoker, who has had the black lung since he was nineteen. In an hour he is sitting up in bed, breathing like a man at the top of a hill, laughing and asking for tobacco. She gives a drop to a baby with the fever. The baby sneezes, once, and goes to sleep, and in the morning it is pink.",
                        "@Ottoline| It's the thing the Company has been after for ninety years. It's not the silver. It never was the silver. It's this.",
                        "* The Fellowship meets that night in the shed. They agree, without a vote, that the sap shall belong to nobody. It shall be given to anyone who asks. It shall be kept in a plain jug by the door of the infirmary, with a tin cup, and a notice. The notice reads: Take what you need.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("sable", "Sable Returns").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "sable"))
                .intro(
                        "* He comes on the evening coach in a new bowler hat. He has aged ten years. His waistcoat is the same colour, but it hangs on him. He stands in the yard of the pit-head, in the rain, without an umbrella. He does not smile. He says he has come to ask a favour. He says it is the first favour he has asked since his mother died.")
                .meet("sable", "Hear what Mister Sable has come to say.")
                .choice(new Chapters.Choice("sable", "Mister Sable, who has bled the town for thirty years, asks for a flask of the sap.",
                        "Give it to him: a man is entitled to medicine, whoever he is.", "Refuse him: the Company has taken enough.", "Take him down: let him see the thing he spent his life digging for.")
                        .after(
                                new String[]{"* You give him the flask. He takes it in both hands. He does not drink. He stands there in the rain, holding it, with his mouth working. He says that it is for his daughter. He says she has been ill for six years. He says that he has been, on the whole, a vile man, and he knows it, and he would take it as a great kindness if she were not made to pay.", "@Ottoline| (low, behind you) I'll go with him. I'll tell him the dose. I do not forgive him. I am a doctor."},
                                new String[]{"* You refuse. He takes it in silence, with a sort of terrible meekness, and bows his head. He says that he deserves it. He says that he has often wished somebody would say it. He turns and walks away across the yard in the rain, a stout man in a wet bowler hat, and does not look back. In the morning he is gone from the coach. He leaves his hat on the bench.", "@Kell| I'd not have done it. I'd like to think I'm better. I'm not at all sure."},
                                new String[]{"* You take him down. It is a long descent. He holds a rail and says nothing. At the bottom he stands in the shaft and looks at the amber on the walls, shining in the lamplight, and slowly sits down on the floor. He says: 'That is it? That is all it was?' He puts out a finger and touches the sap. He is quiet for a time. He says: 'My word. It's rather beautiful.'", "@Sable| I have been a fool. I have been a very expensive fool. I should like to be put to work."}))
                .outro(
                        "* The rain has stopped. The evening light slants across the yard. The Company's clerks have followed Sable out of the office at a respectful distance. They stand in a little group by the gate, with their eyeshades pushed up and their pens behind their ears, like a congregation waiting for the sermon to begin.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("palace3", "The Brown Cart").gap(16)
                .intro(
                        "* A plain brown cart stands in the yard of the pit-head at dawn with a patient horse and a Wick in a thin coat. He has been waiting since midnight. He has a message in a careful round hand. It says: We have a great many pipes in the Under-Kitchen, and one of them has started to run warm. I think it is yours. Please come and tell me what it is.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and down to the Under-Kitchen.")
                .outro(
                        "* The Under-Kitchen is as warm as a bakery. A man in a plain apron stands at the long table with a pipe in his hands, an old iron pipe, as long as his arm, from which a thin stream of gold sap is dripping into a bowl. He looks up. He says his name is ???, and that he is sorry about it, and that he has put a cup under the drip because he did not know what else to do.",
                        "@???| I am allowed to say what it is. It is the Garden's root, and it comes up in the pipes. Every pipe in this room goes to it. The Cistern is a branch. The Cellars are a branch. The thing you call the Counter is the oldest part, the part that keeps the roll. It is the part that remembers who has lived here. I did not know it was awake.",
                        "@You| The Court is digging it up.",
                        "@???| I know. I am not allowed to stop them. I am allowed to ask you to. I have never asked anyone for anything. I find the form is quite short.",
                        "* He takes a cup of the sap and drinks it. His face changes. He sits down at the long table, slowly, with his hands round the cup, and a look of the most profound surprise. He says: 'That is what soup is for.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("shore", "Shoring the World").gap(16)
                .setup(TaleMinerSeasons::mark)
                .intro(
                        "* The collapse starts in the spring. It begins at the bottom of the new shaft, where the Court's tunnels meet the Fellowship's, and it runs up through the rock in a long slow shiver. A crack opens in the wall of the sixth level. A floor sinks in the infirmary. Up on the surface a house in the High Street leans, quite gently, to one side, like a drunk, and a chimney slides off the roof.")
                .made(30, "Hold up the world: make thirty props.")
                .outro(
                        "* The whole of Cinder Deep turns out. Four hundred miners, and the sappers, and the corporals, and the clerks in their eyeshades, with timber and rope and bread. They work for nine days. They work in shifts, by lamp, in the dark, sinking props and bracing beams, sweating in the warm, while the ground groans under them like a ship in a gale. The Captain works the face with his coat off, a grey man with a spade, and does not say a word.",
                        "* On the ninth night the last beam goes in, with a sound like a church door. The groaning stops. The tremor dies. Somewhere very deep, in the root of the world, a great slow heart beats once, and settles. Dob sits down on a sack of lime and cries without any noise. The Captain wipes his face with a dirty hand, and looks at you through the dust.",
                        "@Ashgrove| I have a report to write. I shall say that the root was found to be sound. I shall say the sappers were of use. I do not believe either of those is a lie.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("root", "The Last Shift").gap(16)
                .intro(
                        "* The sappers camp in the yard of the pit-head, in rows of tents, and eat the Fellowship's stew. They sing in the evenings. They are very good at it. The Captain sits at the fire with a pipe and does not speak, and is not asked to. He has received orders. They are in a plain brown envelope on his knee, unopened, and he has not looked at them for three days.")
                .wait(8)
                .choice(new Chapters.Choice("root", "The root of the Garden is in your hands, and the Captain's orders are in his.",
                        "Seal it: close the new shaft, stop the sap, and let the root sleep.", "Tend it: keep the shaft open, run the sap to anyone who asks, and guard it.", "Let it grow: open the lowest gallery to the root itself, and leave the rest to the world.")
                        .after(
                                new String[]{"* You seal it. It takes a week, and a ton of lime, and the whole of the Fellowship on their knees. When it is done the shaft is a slab of grey concrete with a brass plate. The plate says: HERE THE WORLD IS TOUCHED. DO NOT DIG. The sap stops. The warmth fades from the rock. The Court's orders are not carried out, because there is nothing left to carry them out upon, and the Captain, with a rare expression of relief, burns the envelope in the fire.", "@Ashgrove| I shall tell them it was already sealed. It is nearly true."},
                                new String[]{"* You tend it. The shaft stays open. The jug stands by the infirmary with its tin cup. Within a year there is a hospital on the hill, and within five a school of medicine in the old Company office, and pilgrims from three provinces come to take the cure. The Captain's sappers stay on as the guard, in grey coats, with spades. They guard the root with the same grim scrupulous courtesy with which they came to cut it.", "@Ashgrove| I find that I like having a thing to defend. I had forgotten that."},
                                new String[]{"* You let it grow. You open the lowest gallery and step back, and the root comes up. It is slow, and large, and gentle, and it comes up through the floor of the mine like a tree through a pavement. It spreads. It lifts the town an inch, and then another. Within a decade there is a forest on the hill. The pit-head is hung with green, and the wheel turns only for visitors, and Cinder Deep has become a garden.", "@Ottoline| It's a bit out of hand. I wouldn't change it for the world. I think that's the point."}))
                .outro(
                        "* The sappers strike camp in the morning, quietly, in file. The Captain is the last to leave. He stands at the pit-head with his pack on his back and his spade over his shoulder, a short square man in a grey coat, looking at the wheel. He puts out a hand. You take it. It is hard and dry as a root. He says: 'It has been an honour to dig against you.' He walks out of the gate.")
                .pay(1000, 0, 0, 2, 1, 2);

        d.chapter("daylight3", "Daylight, Again").gap(24)
                .intro(
                        "* They hold the first Sunday service in the open air, on the hill, among the green. The Fellowship is there, and the Eleven's families, and what is left of the Company's clerks in clean collars. Kell has shaved his moustache. Ottoline is in a new hat. Dob has brought the pony, which is thirty-one years old and has learned to wear a ribbon. The preacher reads a long and rather good sermon about work. Nobody listens. They are looking at the sky.")
                .wait(12)
                .outro(
                        "* After the service you walk to the edge of the hill and sit on a stone with the whole of the valley below you: the grey town, the wheel, the sea, the green creeping up from the pit-head. The air is soft. You have a bun in your pocket and a lamp at your belt, unlit. You have owed the Lode Company, in your time, eleven scrip, four pennies and a farthing. You have paid it. You have paid, in a way, a great deal more.",
                        "@Dob| (sitting down beside you, with his cap on his knee) I've been thinking about the tapping.",
                        "@You| What about it?",
                        "@Dob| I think it was never counting us. I think it was only saying hello.")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* Cinder Deep is a town of four hundred houses, built on the lip of a mine. Nobody owes anybody for the cradle.",
                "?root=1|* The shaft is sealed under a plate of brass. Children put their ears to it on the first of March and swear they can hear a heart.",
                "?root=2|* The hospital on the hill treats nine thousand a year. The sappers of the Hollow Court, in grey coats, guard the door. They are very polite.",
                "?root=3|* A forest grows from the pit-head to the sea. The wheel turns on Sundays, for the visitors. The root comes up in the gardens in the spring, and the roses are the size of cabbages.",
                "?sable=1|* Mister Sable keeps a stall in the market, selling medicine at cost. His daughter helps him. She is almost entirely well.",
                "?sable=2|* A hat hangs on a hook in the pit-head office. Nobody wears it. Nobody moves it either.",
                "?sable=3|* Mister Sable works the night shift at the lowest level, with a pick. He is not good at it. He says it is the best job he has ever had.",
                "?count=1|* On the first of the month Agnes Marrow reads out the names of the dead at the pit-head. The rock hums.",
                "?count=2|* Dob leaves a lamp lit at the bottom of the shaft. In the morning it is always out.",
                "?count=3|* Something in the rock groans on a Thursday evening, once a month, and then is still.",
                "?love=1|* Ottoline Harl keeps a laboratory in the old infirmary. She writes a monthly report to the Medical College. The College has stopped being surprised.",
                "?love=2|* Ottoline's letters arrive every Friday from the city. They end in a different line each time. You keep them in a tin under the bed.",
                "?love=3|* Ottoline Harl has a room in the city and a room in Cinder Deep, and has never once been sure which she is going home to.",
                "* The tapping goes on, down at the very bottom, under the sealed stair, in the warm. Three and a pause. Three and a pause. Dob sits on the lip of the shaft in the evenings, with his legs hanging over the dark, and taps back.");

        // @@END
    }
}
