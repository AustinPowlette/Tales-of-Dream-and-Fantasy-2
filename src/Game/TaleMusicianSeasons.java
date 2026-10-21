package Game;

/** The Zither That Would Not Be Tuned, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Circuit of Nine" (music as combat, a travelling tournament, cards C and D): the loser of a duel of strings forgets the tune they lost with. Master Lamberd collects the lost tunes in glass jars so that none is lost
 *  for good, and has forgotten why. The human and Ilse restore a village's lullaby, and the last duel decides what happens to the jars. Choices: trade, final. Flags: trade, final.
 *  Season 3, "The Song at the Door" (cosmic and lyrical): the thirteenth string is tied to the other side of the Door. The Garrison of the Crossed sings a muster hymn; Mister Rest, a Guest of the Hollow Court, takes sound.
 *  The grandmother's last verse is on the Roll of the Crossed. Flags: tune, rest, verse. */
final class TaleMusicianSeasons {
    private TaleMusicianSeasons() { }

    static void add(Chapters.Def d) {
        d.flags("trade", "final", "tune", "rest", "verse");
        d.place("palace", "start").place("garrison", "far").place("hollow", "wild");
        d.cast("cressida", "Clarice", "Quill").she().keep().trait(Character.Traits.Musician).side("Good").lvl(5).age(20).at("place:academy").rel(1);
        d.cast("rest", "Mister", "Rest").he().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(15).pot(8).age(60).at("far").rel(0);

        // ================================================================== SEASON 2
        d.season(2, "The Circuit of Nine");

        d.chapter("circuit", "The Circuit").years(2)
                .setup((c, st) -> { Chapters.spawn(c, st, "cressida"); Chapters.spawn(c, st, "pocket"); })
                .intro(
                        "* The invitation is printed on pale green paper, with a silver fiddle at the top. It says that the Festival Circuit of Nine Towns will run from the spring to the autumn, and that its tournaments of strings are open to all players of good character, in the old forms. It says the prize is the Silver Fiddle and a seat on the Convocation Choir. It says that the loser of each duel will surrender the tune they were playing.",
                        "@Ilse| That is a very odd rule. It has been in the charter for three hundred years, and nobody has ever asked what it means to surrender a tune.")
                .wait(12)
                .outro(
                        "* You read it at the long table in the Fiddler's Rest with the zither across your knees. Pocket reads over your shoulder, his lips moving. The pigeon has grown old and fat and sleeps on the dresser. Ilse has put down her tea. She says it is the most interesting thing she has seen in ten years.",
                        "@Pocket| Are we going?",
                        "@You| I think so.",
                        "@Ilse| I think we have to. The Silver Fiddle comes with a seat in the Choir. The Choir sings at the Convocation. I have never heard what it sings at the Convocation. They won't tell Listeners. It is considered too loud.",
                        "* The thirteenth string hums, once, from its case on the table, a low long note, and stops.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("drum", "Pocket's Drum").gap(16)
                .intro(
                        "* The snare drum has a hole in it. It is a small neat round one, the size of a coin, in the middle of the head, where for ten years Pocket has hit it in exactly the same place. He holds it up in the morning light and puts a finger through. He says it is fine. He says it is a drum with a window. He says it with a face that is not fine at all.")
                .friends(3, "Find a drummer's friends: make three friends.")
                .outro(
                        "* Mending it takes a week. You go from town to town looking for skin, and for a man who knows how to stretch it. You find him at last in a cellar behind a tannery, a huge hairy old drum-maker called Orlo, who takes one look at the hole and says that it is the finest hole he has ever seen. He says he will not touch it. He says a hole like that is a drum's signature.",
                        "* He gives Pocket a new drum instead, a great round barrel of a thing in ash and goatskin, with a bass like a church bell. He asks no money. He asks only to hear it played. Pocket plays. Orlo sits on a stool with his hands over his face. When it is over he says, in a muffled voice, that he made his first drum in 1843, and that this is the first time he has been sure it was worth it.",
                        "@Pocket| (to you, in a whisper, afterwards, in the street, with the drum in his arms like a baby) I'll never be this good again. I'll never be this happy. I'd like to keep it.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("reedmarch", "Reedmarch").gap(16)
                .intro(
                        "* The first town of the Circuit is Reedmarch, on the river, a pretty place of boats and bridges, with a green that runs down to the water and a bandstand in the middle. Eleven players have entered. They sit on benches under a striped awning, drinking beer, glaring. A herald in a tabard reads out the rules in a voice like a goose.")
                .wait(8)
                .outro(
                        "* You draw a piper, a gaunt young man called Dace, with a bag the colour of old leather and a way of closing his eyes when he plays. He is good. He plays a jig so quick that the dancers on the green fall over each other, and a lament so slow that a woman in the crowd faints. When he has finished the herald turns to you.",
                        "* You play the funeral tune, the ten notes with the skip in the middle. The pipes cannot follow it. They are too high and too bright. By the sixth note Dace has taken his lips from the mouthpiece, and by the tenth he is standing quite still, staring at his hands, as a man stares at a thing he has dropped and cannot find. The herald declares the contest. Dace bows. He walks off the green in silence.",
                        "@Ilse| (afterwards, low) He has lost it. He doesn't know what he was playing. I watched his face. It simply went.",
                        "* At the edge of the green, Dace sits on a bench with his pipes on his knee. A girl in a blue frock goes up to him with a cup of water. He thanks her. He turns the bag over in his hands. He says to nobody, 'I know I used to play this.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("rules", "The Rule of the Duel").gap(8)
                .intro(
                        "* Ilse reads the charter at the inn that night, line by line, with a pencil, by the light of two candles. She does not speak for an hour. She marks passages. She writes in a notebook. When she looks up her face is greenish, and her hands are shaking, and she says she thinks she knows what a surrendered tune is.")
                .wait(4)
                .outro(
                        "@Ilse| The charter is not a metaphor. It says that the losing player shall surrender the tune. It says to the victor. It means it. When a duel of strings is played in the old form, the loser's tune is not defeated. It is taken. It goes into the winning player's ear, and out of the loser's. There is no way to give it back. It is in the charter. It is called the Quiet.",
                        "@You| Then Dace is...",
                        "@Ilse| Dace has lost the jig and the lament. He has not lost his hands. He has not lost the pipes. But he has lost whatever it was that made him play, and he may never get it back. I think the Circuit was meant to be a competition. I think it has been a harvest.",
                        "* Pocket has gone very quiet. He says: 'Where do they go? The tunes. After they've been taken?' Ilse turns over a page. 'To the Academy,' she says. 'To the Master. There is a cabinet.' She says it in the voice of someone naming a disease.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("fennel", "A Village That Forgot").gap(16)
                .intro(
                        "* The letter comes to Ilse by the Circuit's post, forwarded from the Choir. It is from the schoolmistress of Fennel Cross, in a cramped anxious hand. It says that there is something wrong with the village. It says nobody can sing. It says the mothers have forgotten the lullaby, the one their own mothers sang, and the babies will not sleep, and there is a kind of exhaustion in the place. She asks whether the Choir can help.")
                .at("village", "Go to the village of Fennel Cross, {@village}.")
                .outro(
                        "* Fennel Cross is a street of low grey cottages by a stream. It is very quiet. It is quiet in a way you have never met: not the quiet of sleep, but the quiet of a house where a clock has stopped. In every window there is a pale tired face. In every doorway there is a mother with a baby, jiggling, walking, humming something that is not quite anything.",
                        "* The schoolmistress is a brisk worn woman in spectacles who has been up for nine nights. She shows you the churchyard and the well and the old green door. She says the lullaby is called Fennel Cross. She says it has been sung to every child born in the village for four hundred years. She says that on the first of March she woke and could not remember how it began.",
                        "@Ilse| I remember the label on a jar.",
                        "@You| I know.",
                        "@Pocket| Is this what it is? Is this what he does? He takes a village's song, and they just stop?",
                        "@Schoolmistress| (looking at the zither) I don't understand what you are saying. But if you can bring it back, I'll give you anything I have.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("saltwick", "Saltwick Again").gap(16)
                .intro(
                        "* The second town on the Circuit is Saltwick, which is where the Academy stands. The tournament is held in the great hall with the glass dome. The crowd is larger than before. The champions of the nine regions sit in a row on the platform, in white. At the end of the row, in a plain grey dress, with a cello between her knees, sits Clarice Quill.")
                .wait(4)
                .outro(
                        "?duel=1|@Clarice| (stopping by you in the interval) I have been practising. I did not want to lose to you, and I have been careful not to be flattered. But I should tell you I have not slept since the autumn. I keep hearing the thirteenth string in the middle of the night.",
                        "?duel=2|@Clarice| (stopping by you in the interval) I told you I would not accept it. I want to know whether I was right. I do not play for the Academy now. I play for myself, and I have never been so frightened.",
                        "?duel=3|@Clarice| (stopping by you in the interval) We played well, you and I. I have been doing nothing but duets since the autumn, and I find I am bad at everything else.",
                        "* The duel is brief. You draw a bass viol, a man you do not know, a retired concert master of sixty-three with a white moustache. He is gracious. He plays a nocturne of such sweetness that the entire hall sighs. You play a country dance, plain as bread. When you have finished he rises, and bows, and walks off the platform with his instrument under his arm like a man leaving a house he has loved.",
                        "@Clarice| (in a low voice, as he passes) Don't. Don't take it. You don't have to. He doesn't even know what he's giving up.",
                        "* You look at the tune in your head. It is there, small and bright, like a coin in a pocket. It is a beautiful nocturne. It is not yours. You find, for the first time, that you do not want it.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("cabinet", "The Cabinet").gap(8)
                .intro(
                        "* The cabinet stands in Lamberd's study at the top of the Academy, behind a locked glass door, in a pool of lamplight. It is mahogany, with nine shelves, and each shelf holds forty glass jars with a cork and a label. There are three hundred and sixty. They glow faintly in the dusk, each a different colour: pearl, rose, pale gold, grey. You stand before them, with Ilse beside you, and for a moment neither of you can speak.")
                .wait(4)
                .outro(
                        "* Ilse puts her ear to a jar. She listens. She straightens. 'It is a nocturne,' she says. 'It is the nocturne of the old concert master. It is quite complete. It is alive.' She moves along the shelf. 'And this is a drinking song. This is a hymn. This is a march that was played by a regiment that no longer exists.' She stops at one jar, small and pale, with a label in a faded hand. She says: 'This is a verse.'",
                        "* It reads: Nan Hollin's Third Verse. You open the cork. A thread of silver light rises, as thin as a hair, and hums, very faintly, a phrase of three notes. You know it at once. It is the end of the paper your grandmother left. It is the missing line.",
                        "@Lamberd| (from the doorway, in a flat quiet voice) I would rather you did not do that.",
                        "* He stands in the door in his black coat, with a lamp in one hand. His face is in shadow. He is very still. The lamp does not shake. He says: 'I have been in that room every night for forty years. I have never once let anyone else in. I should like to explain, before you decide what to think.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("lamberd", "Master Lamberd's Story").gap(8)
                .intro(
                        "* He tells it at the little table by the window, with a pot of tea gone cold, in a level voice, looking at his hands. He was a boy of eleven in a mining town, he says. He had a whistle. It was a tin one. He could play a hundred tunes on it. He entered the Circuit at twelve, and lost. He gave up a tune that his mother had taught him, a plain slow song she sang at the stove. He woke the next morning and could not remember how it went.")
                .wait(4)
                .outro(
                        "@Lamberd| She died that spring. I could not sing it to her. I have been looking for it ever since. I built the Academy to look for it. I found that the tunes go somewhere, when they are taken. They go into the winner, or they go out of the world. I found I could catch them, in a jar, if I was quick. I have caught three hundred and sixty. I did not find mine.",
                        "@Ilse| (very gently) Why did you keep them?",
                        "@Lamberd| Because nobody else would. I have never been able to bear the idea of a thing being lost. I have forgotten a great deal about why I started. I am told I am very cruel. I have heard that it is true.",
                        "* The thirteenth string hums softly in its case on the floor. He turns his head. For the first time in your acquaintance his face is naked. He says, 'That string is not in my cabinet, because I could not catch it. It is the only one I ever tried to take and could not. I have thought about it every day since the autumn.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("ear", "Ilse's Ear").gap(16)
                .intro(
                        "* There is something that Ilse can do that no one has known. She does it first at Fennel Cross, on the third night, in the churchyard, with a lantern and a mother with a baby on her shoulder. She stands with her eyes shut and her head tilted. She listens to the jar in her mind. Then she opens her mouth, and sings.")
                .friends(5, "Carry the lullaby home: make five friends.")
                .outro(
                        "* It is not a good voice. It is thin, clear, a little flat, with a catch in the third line. But it is the song, all of it, every note, in the right order. The mother with the baby takes a long shaky breath. She lifts her head. She sings the second line, and then the third, and the baby, on her shoulder, takes its fist out of its mouth and looks at her, and shuts its eyes.",
                        "* By morning the whole village has it. Women in doorways. Men at the well. An old man on a bench by the stream. They sing it to one another. They sing it to the children, and the children to the dogs. The schoolmistress cries, in the road, in front of the whole street, without trying to hide it.",
                        "@Ilse| (hoarse, with a half-smile) It turns out I was never listening for the notes. I was listening for the people who needed them.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("seventh", "The Seventh Town").gap(16)
                .intro(
                        "* The Circuit's seventh town is Gravenhall, a grim black market-town with a cathedral. The tournament is held in the nave, under a ceiling of stone ribs, in front of an audience of two thousand. The Duke's Bandmaster is the champion of the region. He is a tall man in a scarlet coat with a beard like a spade and a trumpet the length of an arm. He has never lost a duel.")
                .wait(8)
                .outro(
                        "* He plays a fanfare. It shakes the windows. It brings the dust down from the ribs of the roof in a fine grey rain. When it is done the whole nave is ringing, and the crowd is on its feet. You sit with the zither, small as a sparrow, in the middle of a pool of silence, and do not play a note.",
                        "* The Bandmaster waits. The crowd waits. A baby cries. You close your eyes, and put your hand on the thirteenth string, and let it find its own note. It comes up out of the floor like a spring. It is not loud. It is simply there, a long soft sound, as large as the building, and where it passes the dust stops falling, and the echoes of the trumpet fold themselves up and go to sleep.",
                        "* The Bandmaster lowers his horn. He stands for ages with his mouth slightly open, listening. Then he does a thing no one has ever seen him do. He takes off his hat, and holds it against his chest, and bows his head. The two thousand people in the nave do not move. The sound goes on. After a time, from somewhere very far away, a second sound answers it.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("trade", "Master Lamberd's Offer").gap(8)
                .intro(
                        "* He comes to you on the road to the eighth town, on foot, alone, without his coat. It is raining. He stands in the road in his shirt-sleeves, with water running down his face, and asks if he might speak. He has a new voice, a hoarse humble one, as if he had been practising it. He says he has a proposition, and that he does not expect you to like it.")
                .wait(4)
                .choice(new Chapters.Choice("trade", "Master Lamberd offers every jar in his cabinet, all three hundred and sixty, in return for the thirteenth string.",
                        "Refuse. The string is not for sale, and the jars are not his to trade.", "Accept: three hundred and sixty tunes are worth more than one string.", "Ask for a night to think, and a duel in the morning to settle it.")
                        .after(
                                new String[]{"* You refuse. He takes it quietly, with a slight bow, in the rain. He says he understands. He says it was a wretched thing to ask. He says he would be very grateful if you would do him the honour of considering a duel. He says it is the only way he knows how to ask for what he wants.", "@Lamberd| I have never asked anyone for anything. I find it is much worse than taking."},
                                new String[]{"* You accept. It is the hardest thing you have ever said. You say it with the zither in your arms and the rain in your face. Lamberd closes his eyes. When he opens them he is smiling, a terrible, tender smile, and he says, 'Thank you,' and your stomach turns over. The string, in the case on your back, goes cold. It does not hum again that night.", "@Ilse| (very low, appalled) What have you done?"},
                                new String[]{"* You ask for a night. He nods. He walks away down the road in the rain without his coat, an old man in a white shirt, to spend the night in a ditch rather than risk your changing your mind. In the morning he is still there, soaked, sitting on a stone, quite calm. He says that the duel would be at noon.", "@Pocket| I think I'm starting to like him. I think that's a mistake."}))
                .outro(
                        "* The rain stops at midnight. The sky comes out in a field of stars. By the road, in the wet grass, the thirteenth string begins, faintly, to hum, in a note a little lower than it has ever gone. Ilse lies on her back with her notebook on her chest and her eyes open. She says, 'It is much nearer.' She does not say what.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("final", "The Last Duel of the Circuit").gap(8)
                .intro(
                        "* The last duel is held in the Palace of the Oldest Order, in the great hall, before the Choir of the Nine Bells. It is the oldest custom of the Circuit. The final two players stand on the black-and-white floor with their instruments, in a ring of candles. The Choir sits in its stalls along the wall, nine hundred voices in grey robes, absolutely silent. Above them, in the shadow of the dome, hangs an empty chair.")
                .wait(4)
                .choice(new Chapters.Choice("final", "Master Lamberd stands across the floor with a viol in his hands, and a cabinet of tunes behind him in a cart.",
                        "Win the duel, and take the cabinet: let the jars be broken, and the tunes go free.", "Win the duel, and open the jars quietly: give each tune back to the one it was taken from.", "Play with him: a duet of the viol and the zither, and let the music decide who is beaten.")
                        .after(
                                new String[]{"* You win. It is not a close thing. When Lamberd's viol falters on the third bar you press, and the thirteenth string comes in, and the hall rings like a struck glass. The jars in the cart shatter, all three hundred and sixty, one after another, in a chain of small sweet sounds. The tunes rise like smoke and go out through the windows, in a cloud of pale lights, to wherever they belong.", "@Lamberd| (on his knees, not looking) I did not know they would be so beautiful."},
                                new String[]{"* You win. You say nothing about it. That night, with Ilse and Pocket and a cart, you take the jars one by one to the people from whom they were taken. It takes a year. You find the piper, and the old concert master, and the mother in the mining town. Each time you open a jar they stand a long time in silence. Then they begin, with a stumble, to sing. Lamberd drives the cart. He does not speak.", "@Lamberd| I am only the driver. I should like that to be clear."},
                                new String[]{"* You play with him. You ask him, in front of the whole Choir, to put down his bow and take it up again beside you. He hesitates. He is shaking. Then he lifts the viol. The two instruments find each other in the first four bars. It is a strange, slow, cumbersome thing, like two old men walking together. Halfway through, Lamberd begins to hum, without knowing, a tune. It is a plain slow song a woman sang at a stove.", "@Ilse| (a whisper, in the stalls, with her hands over her mouth) That is the one. That is his mother's."}))
                .outro(
                        "* The silence at the end is the silence of a church. The Choir does not move. Then, from the stalls, from nine hundred throats, in a single great breath, there comes a sound that is not applause. It is a low unanimous hum, a chord, which holds, and swells, and holds. It is the Choir's note. It is the note that it sings at the Convocation, which is not a hymn. It is the thirteenth string.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("tune2", "What the Tune Was For").gap(24)
                .intro(
                        "* The tunes go home. It takes a year, and a thousand miles, and a great deal of walking. Pocket keeps a ledger. He writes down every name in a schoolboy hand, with a drawing of the tune beside it. By the end he has filled four notebooks, and his drum is worn nearly through, and he has learned, to his surprise, that he can read.")
                .realm(2, "Finish the Circuit: reach Major Lvl 2.")
                .outro(
                        "?trade=2|* The thirteenth string is quiet now. It hangs on the zither pale and loose, and hums only when you are asleep. Ilse says it is waiting. She does not say for what. She sits by the fire at night with her ear to the wood, and her face, in the firelight, is calm, and sad.",
                        "?trade=1|* The thirteenth string hums every night, a little lower. Ilse sits with her ear to the wood and writes in her notebook. She says it has stopped being a note. She says it has become a direction.",
                        "?trade=3|* The thirteenth string hums every night, a little lower. Ilse says it is as near as a neighbour's door. She says that if you listened very hard you could hear someone on the other side, putting down a cup.",
                        "* On the last night of the Circuit, at the Fiddler's Rest, there is a small party. Brewster has put out a ham. Orlo the drum-maker has come in a new coat. Clarice plays a duet with Pocket, a cello and a snare, that no one thought could work. At midnight Lamberd, who has been sitting in the corner with a glass of water and a face like a convalescent, stands, and asks if he might sing.",
                        "* It is a plain, slow, wobbly little tune. He sings it in a cracked tenor, with his eyes shut, to the end. When he has finished nobody speaks. Then Brewster says: 'Is that all of it?' And Lamberd says, 'I believe so,' and sits down quite suddenly, and puts his face in his hands.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "The Quiet",
                "* The charter of the Circuit has been amended. A tune surrendered may be given back. It is the first change in three hundred years. It was proposed by a Listener, seconded by a drummer, and carried by an old man with a viol.",
                "?final=1|* The jars were broken and the tunes flew out of the windows. For a week afterwards, all over the country, people woke at night humming something they could not place.",
                "?final=2|* The jars were opened one at a time. It is said that if you stand in Fennel Cross on the first of March, at dusk, you can hear every mother in the village sing the same song, in the same key, to a different baby.",
                "?final=3|* Master Lamberd plays the viol on Sundays in the Fiddler's Rest. He is not good. Brewster says he is the best the place has ever had.",
                "* Clarice Quill has taken the Academy. She has taken the clocks off the tower. She has taken the rule off the door. A new one has been put in its place, in brass, in her own hand: Any instrument may be played in this establishment that can be heard.",
                "* In the cabinet at the top of the Academy there is one jar left. It has not been opened. It is pale, and small, and sealed with black wax. The label says, in a hand like a row of wire: The Last Verse. Under it, in smaller letters: Do not open until the Door.");

        // ================================================================== SEASON 3
        d.season(3, "The Song at the Door");



        d.chapter("wake", "The Note Wakes").years(2)
                .intro(
                        "* It has been humming for thirty years. On a night in October, in the barn at Haldane, with the wind in the thatch and Pocket asleep on a hay-bale with his arm round the drum, the thirteenth string stops. It does not fade. It stops, as a voice stops when a person has been told to wait. Ilse sits up in her blanket. She puts her ear to the wood. She is silent for so long that you think she has gone to sleep with her eyes open.",
                        "@Ilse| It is not humming. It is counting.")
                .wait(8)
                .outro(
                        "* She writes it down by lantern light. It is nine notes, repeated, with a rest between. Low, low, high, low, low, high, high, high, low. She plays it on your spare fiddle, and it is a phrase you have heard a hundred times in your life without knowing. You have heard it in the bell at Originis. You have heard it from the Choir. It is the first line of the muster call.",
                        "@Pocket| (waking, with straw in his hair) What's the muster?",
                        "@Ilse| It is what you play for the people who are about to go through.",
                        "* The thirteenth string takes up the phrase again, very quietly. This time it does not stop. It runs through the nine notes and starts at the beginning. It sounds, you think, rather like someone knocking, from a long way off, politely, on a door.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("organ", "The Lowest Pipe").gap(16)
                .intro(
                        "* The Palace of the Oldest Order has an organ in the great hall, nine storeys high, with five thousand pipes and a console like the bridge of a ship. It is played once a year at the Convocation. It has a pipe that nobody uses, at the very bottom of the case, as wide as a well. Ilse has wanted to hear it since she was nine. She asks the Master of the Choir. The Master of the Choir goes the colour of tallow, and says that it is not played.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and ask for the organ loft.")
                .outro(
                        "* The Master relents. He takes you up through the dark behind the pipes, along a catwalk, past the great gleaming shapes. At the bottom of the case, in a niche of black stone, there is a pipe thirty feet long with a stop of unpolished iron. It has a note painted on it in white. The note is a quarter-tone below the lowest A of the organ. It is the note of the thirteenth string.",
                        "@Master| The Choir does not sing a hymn at the Convocation. It never has. The Choir holds one note. It holds it for the length of the Convocation, nine hundred of us, in relays, without a pause. It is the only thing the Palace does that is older than the Palace. We do not know why. We were told it was a tuning.",
                        "@Ilse| Tuning what?",
                        "@Master| We were told: the Door.",
                        "* He lays his hand on the great iron stop and does not pull it. His fingers are trembling. 'It was last sounded,' he says, 'in the year of the great snow. Someone brought a stringed instrument to the Convocation, and it answered, and the pipe answered it. The Master of the day had it sealed. He said it was an interference.' He looks at the zither on your back. 'He did not say with what.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("roll", "The Roll of the Crossed").gap(24)
                .intro(
                        "* The Garrison of the Crossed stands on a bare plain at the edge of the world. It is a fortress with its gates open, whole and empty, with the beds made and the lamps trimmed. In the muster hall there is a long stone carved with names in a hundred different hands. THE CROSSED, it says at the top. You have not come to read it; you have come because the string has been pointing here since the barn.")
                .at("garrison", "Go to the Garrison of the Crossed, {@garrison}.")
                .outro(
                        "* You walk the length of the stone with a lantern. Ilse follows, reading aloud in a low voice, like a woman reading the lesson in church. Some of the names are only initials. Some are in languages that are no longer spoken. About halfway down, in a small upright pencil hand, scratched rather than cut, there is a line you know. It is a signature you have seen on the flyleaf of a hymn-book: N. Hollin. Under it, in the same hand: Gone to see about a string.",
                        "@Pocket| (reading over your shoulder, with the drum silent for once) That's your gran.",
                        "@You| It can't be.",
                        "@Ilse| It is the same hand. It is the same paper. Look at the date.",
                        "* The date is the year of the great snow. You stand there with the lantern, in the cold, in a long empty room that smells of lamp oil. The thirteenth string, on your back, makes a small, terrible, clear sound, like a person catching their breath. Your grandmother did not die in the winter. She was called. She went through the Door with a zither on her knee, and left the other end of the string behind her, with you.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("hymn", "What the Garrison Sings").gap(16)
                .intro(
                        "* They are about to muster. A line forms in the yard at dusk: a hundred men and women in good boots, with their blades at their sides and a small lamp apiece. They stand in rows. They do not speak. A tall woman with a grey plait walks along the line with a ledger, checking names. When she has finished she nods, once, to nobody. Then from somewhere in the ranks, a single voice begins to sing.")
                .wait(8)
                .outro(
                        "* It is the muster hymn. It is plain and slow and not at all warlike. It has three verses and a refrain. It goes through the yard like a river through a field, rising, until a hundred voices hold it, low and steady and quite without fear. You stand at the back with the zither across your arm, and Ilse puts a hand over her mouth. The refrain has nine notes. The thirteenth string joins it, softly, on the fourth.",
                        "* They stop at the end of the second verse. There is a pause, a gap in the shape of a verse, and then they sing the refrain again and begin to file out through the gate. Ilse's face is wet. She says: 'There is a third. They do not sing it. They have never sung it. Listen to the shape.'",
                        "@You| I have it.",
                        "@Ilse| What?",
                        "@You| Three lines, in pencil, on a piece of paper from a farmhouse. And a last verse, in a jar.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("rest", "Mister Rest").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "rest"))
                .intro(
                        "* He is waiting at the foot of the stair, a tidy bald man in a grey suit with a tuning-fork in his waistcoat pocket and a silence about him like a closed door. He lifts his hat. When he speaks, you notice, there is a faint hush after every word, as if the air round him had been taught to listen.",
                        "@Rest| Mister Rest. I am a Guest of the Court. I have come about the string.")
                .meet("rest", "Hear what Mister Rest has come to say.")
                .outro(
                        "* He walks beside you for a mile under the stars, without hurry. He says that the Court has no quarrel with music. He says it is a perfectly respectable art. He says it has, however, a difficulty with one note, a note that has been sounded in the Garden for as long as the Garden has been, and has been answered, he says, from the other side. It keeps a door ajar. The Court would be extremely grateful if it were allowed to close.",
                        "@Rest| I take one thing. It is a rule of my trade. I only ever take a sound. I should like to take that one. I would leave you every other note you own. I would leave you the zither, and the drum, and the cello. You would play to the end of your days. Only the thirteenth would be gone.",
                        "@Ilse| (very quietly) You would take the Door's ear.",
                        "@Rest| I would take its voice. It is a great kindness to a door to let it stop calling.",
                        "* The thirteenth string, in the case on your back, goes quiet. It is the quiet of an animal in a hedge.")
                .outChoice(new Chapters.Choice("rest", "A Guest of the Court asks for the thirteenth note, and offers the rest of your music.",
                        "Refuse him to his face, and play the note.", "Ask what the Court wants the Door shut for, and listen to the answer.", "Offer him a song instead: one note of your own, freely given.")
                        .after(
                                new String[]{"* You play it. It is as loud as you can make it, in the dark, in the road, with the whole of your body. The note rolls out over the plain and comes back from the stones. Mister Rest does not flinch. He stands with his hat in his hand and his head a little on one side, hearing it out, and when it dies he says in a tone of the deepest respect: 'Ah. That is a good deal harder than it looks.'", "@Rest| I shall be back. I do hope you will keep your tuning."},
                                new String[]{"* You ask. He is delighted to be asked. He tells you, at length, over a mile of road, in a low pleasant voice, that the Court is tired. He says the Garden has been ringing for a very long time, and nobody has had a night's sleep. He says it is not malice. It is exhaustion. He says if the door were shut, and the calling stopped, a great many people on the other side could rest. You listen. When he has finished you find that your eyes are wet.", "@Rest| You see the difficulty. I do not hate it. I only want it quiet."},
                                new String[]{"* You offer him a song. You take the fiddle from the hay-cart, and play, for a single minute, the plainest tune you know. When you finish he is standing very still. He has taken the tuning-fork from his pocket and is holding it in both hands, looking at it. 'I have never been given one,' he says. 'I take, you see. Nobody has ever thought to give.' He puts the fork away with great care.", "@Rest| I shall keep that. I should like to say that it is a great deal more than I deserve."}))
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("ensemble", "Rounding Up the Players").gap(16)
                .intro(
                        "* You need an ensemble. The thirteenth string is a rope, and the Door is a long way off, and a rope can be pulled harder by many hands. Ilse draws up a list, in her upright script, of every player you have met on the Circuit. There are forty names. It begins with Clarice, goes through Orlo and Brewster and the old concert master, and ends, in a smaller hand, with the words: Lamberd (if he will).")
                .friends(10, "Call the players in: make ten friends.")
                .outro(
                        "* You write forty letters. Thirty-one answer. They arrive at Haldane in carts and on foot, a straggle of fiddlers, pipers, fifers, drummers, cellists, one man with a harp the size of a door. They fill the barn. They fill the lane. They sleep in the hay and the stable and the church. The bride's father, now a very large grandfather, feeds them all, and weeps when he is thanked.",
                        "@Clarice| I brought the cello. I brought seven students. I told the Academy I was going on a walking holiday. They were very understanding. I think they assumed I'd gone mad.",
                        "* Lamberd is the last to arrive. He comes at dusk on foot, in his best black coat, with a viol under his arm and a very small hat. He stands in the doorway of the barn. Thirty-one people turn round. Nobody speaks. After a long moment Pocket gets up from the straw, crosses the floor, and says: 'You're late. We've saved you a bench.'")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("rehearse", "Rehearsal in the Rain").gap(16)
                .intro(
                        "* It rains for nine days. The ensemble rehearses in the barn, thirty-two players on straw bales and upturned buckets, in a noise like a barn on fire. Nobody can agree on the key. The harpist, a mild man called Fenwick, plays everything in a mode that has not been heard since the Flood. The pipers refuse to sit with the viols. Pocket, who is conducting because nobody else will, has made a baton from a wooden spoon.")
                .wait(8)
                .outro(
                        "* On the sixth day it all falls apart. Clarice and Lamberd have a row about tempo, in front of everyone, in clipped voices, with the cello and the viol held across their bodies like swords. The pipers go out. The drummers go after them. The harp falls over. A hen walks across the stage. Ilse puts down her notebook, walks to the middle of the barn, and says in a high clear voice, 'Please, will you all be quiet.'",
                        "* They are. It is the first time she has ever raised her voice. She stands there, very straight, in her grey coat, with ink on her cuff and her ears pink. She says that she has listened to every one of them for three weeks. She says that each of them is a very good player. She says that they have not yet noticed that they are all playing the same piece. She says: 'Hear the note under it.'",
                        "* She lifts her hand. She nods to Pocket. He raises the spoon, and counts four. And thirty-two players, who have not once played together in tune, find the thirteenth string under their feet, like a floor, and stand on it.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("jar", "The Last Jar").gap(16)
                .intro(
                        "* Lamberd brings it on the last night of the rehearsal, in a handkerchief, from an inner pocket. It is a small glass jar sealed with black wax. He holds it out on the flat of his palm, like a boy with a bird's egg. He says he has carried it for thirty years and has never once opened it. He says it is not from the Circuit. It was left at the Academy by a woman, on a night of snow, with a note.")
                .wait(4)
                .outro(
                        "@Lamberd| She said: keep this until somebody comes who can hear the string. She said I would know them. I did not. I thought it would be a Master. It is a young person with a hat on the ground. I have been unfair to you, and a great fool. I should like to be allowed to apologise.",
                        "* You break the wax. A thread of silver light rises and loops in the air above your head. It is a voice. It is thin, and old, and very close. It is your grandmother's. She is humming. She is singing a verse, low and slow and a little off the note, as she sang over the washing-up. It is three lines, and it ends the way the hymn ends in the yard of the Garrison, and the pencil fragment, and the song.",
                        "@Nan Hollin| (in the light, faint, from a long way off) If you are hearing this, my dear, it means you have come far enough. I never could tune it, you know. I tried. I thought it was my fault. It was a rope, and I was the other end. I went through to hold it. I am in a dim long room with a great many others, and we are all humming it. It is very restful. Come when you are ready. Play it at the Door.",
                        "* The light folds up, and goes out. The barn is utterly silent. After a time Pocket blows his nose.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("concert", "The Concert at the Door").gap(16)
                .intro(
                        "* The ensemble marches to the Garrison in the first week of spring, thirty-two players in a long file, with carts of instruments and a cook-wagon. They set up on the plain, under the grey sky, in front of the open gates. The line is there, as always, patient, in boots, with lamps. A tall woman with a grey plait stands at its head. She watches the players unpack. She says nothing at all. When Pocket sets up his drum, she turns her head and looks at the horizon.")
                .wait(4)
                .choice(new Chapters.Choice("tune", "The ensemble is in place. The string is a rope, and the Door is at the other end.",
                        "Play it to tune the string: send the full note down the rope and let it settle.", "Play it for the line: a muster hymn with its third verse, for the called, whatever else happens.", "Play it as a message: ask the other end, plainly, to answer.")
                        .after(
                                new String[]{"* You play. The thirteenth string rings, and thirty-one instruments take its note and give it back. For the first time in the world it sounds against something and does not echo. It settles. It is as if a lamp in a window has been trimmed to its proper height. The string is in tune. Ilse takes the zither, afterwards, in her hands, and holds it a long time, and says: 'Quiet. It's so quiet. Is it supposed to be so quiet?' It is. It is a rope with the weight taken off.", "@Clarice| That is the most beautiful noise I've ever made, and I'm no longer sure it belongs to us."},
                                new String[]{"* You play the muster hymn, all three verses. It is the first time the third has been heard on the plain. The line stands, in boots, with lamps, and listens. At the third verse a woman at the head of the file puts her hand to her mouth. By the refrain the whole line is singing. They sing it a hundred strong, in the cold, in the rain that has begun, and then they file in through the gate, one after another, with their lamps held high. Not one of them looks back. Not one of them is afraid.", "@Lamberd| I have conducted four thousand concerts. That is the only one I would call music."},
                                new String[]{"* You play it as a question. It is a single phrase, nine notes, rising. You send it down the string with every instrument you have. for ages there is nothing. Then, from very far away, a single answer comes up the rope. It is a voice, humming, on the same nine notes, a fraction lower. It is a woman's. It is a woman who has hummed the same phrase for forty years, patient and steady, in a long dim room, to anyone who might hear.", "@Ilse| (a whisper) She answered."}))
                .outro(
                        "* The rain falls. The plain is silver. At the edge of the line, the tall woman with the plait turns to you, at last, and inclines her head. It is a small, formal, courteous movement. She says nothing. She puts her hand over her heart. Behind her the gates stand open on a corridor of lamps.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("listens", "Mister Rest Listens").gap(8)
                .intro(
                        "* He stands at the back of the plain after it is over, alone, under his hat, in the rain. He does not come forward. He is holding the tuning-fork in both hands. When you go to him he is looking at the gate. His face is, for the first time, quite without its courtesy.")
                .wait(4)
                .outro(
                        "?rest=1|@Rest| I shall not take it. I should like you to know that. I came to take it, and I found I could not. I have taken the sound of two thousand things. I never once heard one of them. I am not at all sure what to do.",
                        "?rest=2|@Rest| You were right to let me say it. I should like to say it was not an argument. It was a confession. I have been tired for a very long time. I had not noticed that the tiredness was a sound.",
                        "?rest=3|@Rest| I have your tuning-fork. I mean, you have mine. I mean, I have been given a note, and I do not know where to put it.",
                        "* He lifts his hat. He walks away across the plain in the rain, a small grey figure with a tuning-fork, humming. He is a little flat. He stops, and tries again. He gets it, on the third go. He goes on, a tiny tuneful shape against the horizon, and is gone.",
                        "@Pocket| (watching him go) I think he'll be all right. I think he's going to learn the drum.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("verse", "Tuning").gap(24)
                .intro(
                        "* The thirteenth string is still. It hangs pale on the zither, quiet as a thread. It is the first time in your life you have seen it at rest. You sit with it across your knees on the bench in the barn at Haldane in the blue of the evening, with the swallows going over the roof, and look at it for ages. You have one verse left, and one choice.")
                .wait(8)
                .choice(new Chapters.Choice("verse", "The last verse is yours: three lines, in a jar, in a pencil fragment, in your grandmother's hum.",
                        "Keep it. Some tunes are better held than given.", "Teach it to the Choir, so that the Convocation can sing it with the rest.", "Take it to the Door, and sing it yourself, when your turn comes.")
                        .after(
                                new String[]{"* You keep it. You say it aloud once, to the barn, in a low voice, and then never again. It lives in your head like a coin in a pocket. Clarice asks, once, what it was. You say it was a verse. She nods. She says it is the sort of thing that is better asked about than answered.", "@Ilse| It is the first secret you have ever kept. I think it suits you."},
                                new String[]{"* You teach it to the Choir. It takes a winter. Nine hundred voices in grey robes learn it from a Listener and a drummer and a young woman with a cello, in the great hall, in the evenings, by lamplight. At the next Convocation they sing it. It is not loud. It is the quietest thing the hall has ever heard. In the stalls, a man in a plain apron puts down his spoon.", "@???| (afterwards, very faint) I have heard that. I have heard it somewhere. I cannot think where."},
                                new String[]{"* You carry it with you. Years go by. On a clear evening in autumn, at the end of a long life, you stand at the Door with the zither on your back and Ilse's hand in yours. The line has gone in. The gate is open. You put the zither to your knee and play the three lines. It is not a good performance. It is not meant to be. At the end the thirteenth string answers, in a voice that is half an octave lower, and you walk in.", "@Pocket| (gruff, at the gate, his drum on his shoulder) We'll keep the fire lit."}))
                .outro(
                        "* The zither has thirteen strings. Twelve are steel. The thirteenth is pale and thin and does not sound unless it is spoken to. Ilse says it is perfectly in tune. She says she has checked it eleven times. She says that it is in tune with something, and she does not mind what.",
                        "* You hang it on the wall of the barn at Haldane over the hearth. At night, when the fire has burned low, it hums a little, softly, as a cat does, in its sleep.")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* The zither hangs over the hearth in the barn at Haldane. It has thirteen strings, and one of them is in tune.",
                "?tune=1|* The string is quiet. When you play it, the sound goes down the rope and does not come back. Ilse says that is what it is for.",
                "?tune=2|* On the plain at the edge of the world a line forms every dusk. It sings three verses now. Nobody can say who taught it.",
                "?tune=3|* On certain evenings, at dusk, the thirteenth string hums a phrase of nine notes. Some distance away, very faintly, a woman hums it back.",
                "?rest=1|* A bald man in a grey suit has been seen at the back of the Choir's stalls with a tuning-fork. He never sings. He is said to be learning.",
                "?rest=2|* A bald man in a grey suit sits at the end of a bench in the Fiddler's Rest with a glass of water, listening. He tips generously.",
                "?rest=3|* Mister Rest plays the triangle, on Sundays, in a chapel in the north. It is a very small triangle. He plays it a little flat.",
                "?verse=1|* There is a verse that you have never sung. You know it by heart.",
                "?verse=2|* The Choir of the Nine Bells sings a quiet verse after the muster hymn, once a year. Nobody has told them where it came from.",
                "?verse=3|* Your grandmother's third verse was the first thing the line sang, on the day you went in.",
                "?love=1|* Ilse Ferrant listens to you with her eyes shut. She does it still, every evening, as though it were a thing worth the trouble.",
                "?love=2|* Ilse walks beside you on the road, a little behind, with a notebook. She says she is writing a book about listening. It has no first chapter.",
                "?love=3|* Ilse keeps a perfect fifth in a drawer of her desk, on a card, in her upright hand. She says she will frame it. She does not.",
                "* Pocket drums at weddings. His drum has a hole in it, a small neat one, in the middle of the head. He says it is a window. Through it, if you put your eye to it on a clear night, you can see a line of lamps going in at a gate.");

        // @@END
    }
}
