package Game;

/** The Last of the Silver Crane Clan, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "Stillspring" (labour dread and folk fear, the working voice with its routines and rules: no whistling below, count the lamps): the clan digs the Deep. The people of the clan feel the vein before the human does.
 *  Edric Hexley (the Baron's son) comes back. The vein answers. Flags: shaft (what the clan did after the cave-in), edric (what became of him), hum (whether the human answered the vein).
 *  Season 3, "The Crane's Flight" (cosmic and mythic): Stillspring is a sleeping crane, the First, kept asleep for four hundred years by a nightly cup of tea (the clan's oldest joke is its oldest rite).
 *  A wanderer pays with a coin that has a crane on it. Captain Nought of the Hollow Court comes for the sleeper. Flags: wake (what the human chose), sleeper (who rode). */
final class TaleCraneSeasons {
    private TaleCraneSeasons() { }

    static void add(Chapters.Def d) {
        d.flags("shaft", "edric", "hum", "wake", "sleeper");
        d.place("deep", "wildnear");
        d.cast("marta", "Marta", "Dumpling").she().keep().trait(Character.Traits.Alchemist).side("Good").lvl(1).age(52).at("here").rel(3);
        d.cast("poll", "Old", "Poll").he().keep().trait(Character.Traits.Gatherer).side("Good").lvl(1).age(61).at("here").rel(3);
        d.cast("pilcher", "Pilcher", "Gant").he().keep().trait(Character.Traits.Scribe).side("Good").lvl(1).age(64).at("here").rel(3);
        d.cast("edric", "Edric", "Hexley").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(6).pot(8).age(23).at("far").rel(-1);
        d.cast("nought", "Captain", "Nought").he().trait(Character.Traits.Leader).side("Evil").lvl(10).pot(10).age(60).at("far").rel(-3);

        // ================================================================== SEASON 2
        d.season(2, "Stillspring");

        d.chapter("bell", "The Shift Bell").years(2)
                .setup((c, st) -> { Chapters.spawn(c, st, "marta"); Chapters.spawn(c, st, "poll"); Chapters.spawn(c, st, "pilcher"); })
                .intro(
                        "* There are rules in the Deep, and they were not written down. They were passed along the way rules are, from one tired mouth to another, with a nod and a nudge on the ladder. Don't whistle. Don't count the dead. Don't say the word 'collapse'; say 'the shift'. Count your lamps going down and count them coming up, and if the numbers differ, say nothing, and go up anyway.",
                        "* It is called the Deep because no one in the clan has been able to agree on another name. It is the old cavern, widened by two summers of work into a shaft with a timber frame and a wheel. A hundred and thirty people of the clan go down it, in three shifts, to take the pale stone of the vein: the dust of it, the grit, the cold bright crumbs that people grind for their cultivation.",
                        "* The bell goes at five. Clang, clang, clang. Boots on the boards. Lamps on the hooks. Marta's dumplings steam on the lid of the stove, and Poll, who has retired from the river and taken up complaining as a trade, says that it will rain, and it does not.",
                        "@Joren| Count your lamps.",
                        "@All| Counted.",
                        "* It is the shift's whole liturgy. You have said it a hundred times. This morning, for the first time, you notice that there are always one more voices than there are men.")
                .wait(8)
                .outro(
                        "* You do the sums on the stairs, going up, with the dust on your lips. A hundred and thirty-one lamps went down. A hundred and thirty came up. You count again in the yard. It is the same.",
                        "@Poll| Don't. Don't go counting it twice. That's how it gets in your head.",
                        "@You| How what gets in?",
                        "@Poll| Nothing. I'm a ferryman. We say that about water.",
                        "* He stumps off. Clang goes the bell for the end of the shift, and the noise goes out across the valley, and the hum underneath it, steady and slow, goes on as if nothing had interrupted.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("rules", "Rules of the Deep").gap(6)
                .intro(
                        "* The children have a rhyme for it. They sing it skipping, on the flat stones by the wheel-house, with two girls turning the rope and a third in the middle. The words go like this: Lamp up, lamp down, count the ones that came to town. Say it low, say it true, if there's one that's not for you. Bring your boots and keep your head, and say good night before you're dead.",
                        "* Pilcher collects them. He has a book. It has eighty rhymes in it already, and a section on omens, and one on what to do about the smell. He writes down each rule as it is told him, with the name of the teller and the date. Beside it he draws a small square, to show whether the rule has ever, so far as he can tell, been broken without consequence.")
                .wait(2)
                .outro(
                        "* The squares are almost all blank. There are three that are filled. The first is a rule about the left boot, and the filled square beside it is for a man named Dunn, who put on the left first, and lost his supper. The second is a rule about not saying the word 'snake', and the filled square is for the cat, which does not know the word and fell into a bucket.",
                        "* The third square is next to a rule that has no name beside it. Nobody can say who told it. It is the first rule in the book, in Pilcher's neatest hand, and it says: Say good night to the dark. And under it, in a handwriting that is not his, small, in pale ink, there is a single word, added later, and it is Good night.",
                        "@Pilcher| I did not write that. I have asked everybody. I assure you I do not forget my own sentences. I have looked at it every day for a week. It is a little warm, if you hold your hand over it.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("sleepers", "The Sleepers").gap(8)
                .intro(
                        "* It begins with Dunnock. He is found at four in the morning at the head of the shaft, in his nightshirt, with his arms at his sides, looking down. His eyes are open. He is asleep. He is humming, very softly, the same note as the Deep, and he does not stop when Tamsin slaps him, nor when she pours a bucket on his head.",
                        "* The next night it is two. Then six. They come out of the huts barefoot, in ones and twos, in the dark, as quiet as cats, and they go down the paths to the shaft head and stand there. They do not fall. They do not try to climb. They only stand and hum and sway a little, and when the sun comes up they wake with no memory at all and a grave, apologetic hunger.",
                        "@Marta| It's not a sickness. I've nursed sickness. It's more like being called.",
                        "@Pilcher| I took the liberty of keeping a register. It is, I regret to say, in alphabetical order. I do not know how to record a thing like this without being orderly.")
                .at("hall", "Keep watch at the shaft head of the Deep, at {@hall}, through the night.")
                .outro(
                        "* You sit on a barrel by the wheel with a lantern and a flask. Joren is beside you, whittling a stick to a point. By midnight there are fourteen of them at the head of the shaft, in their nightshirts, in a loose ring, humming. The moon is up. Under their feet, the boards of the shaft cover are warm.",
                        "* Isla comes out of the tent with her sewing. She sits beside the sleepers, cross-legged, in the light of the lantern, and begins to stitch. She does not look up. After a while the humming slows, and rises, and finds her note, the way a choir finds its pitch.",
                        "@Joren| She's doing it right, isn't she. Look. She isn't fighting it. She's going along with it.",
                        "* The ring of sleepers sways. The needle goes in and out. The note rises one tone and holds. And from below the boards, a very long way down, something answers it, in a voice that has no words, with a sound like a great lazy breath.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("lamps", "Count Your Lamps").gap(6)
                .intro(
                        "* You go down the next morning with the first shift. You have been down a hundred times, and it has never taken so long. The ladder is the same ladder. The rungs are the same rungs. But the numbers on the lamp hooks, which are painted in white, have changed. The hooks that said 41, 42, 43 now say 41, 42, 43, 43, 44.",
                        "@Marta| Don't look at them. Look at your feet.",
                        "* There is a crowd of you at the bottom, the whole shift, with lanterns, on a ledge of grey rock, looking at a wall. The wall is the vein. It is not like stone. It is the silver of a cold sky, and it moves very gently, in and out, in and out, as the wall of a sleeping animal moves.")
                .wait(2)
                .outro(
                        "* Poll counts the lamps. Twice. Out loud, which is against the rules. He gets to the end of the line, and stops, and wipes his mouth with the back of his hand.",
                        "@Poll| Hundred and thirty-two.",
                        "@Joren| We're a hundred and thirty-one.",
                        "@Poll| I know what we are, lad.",
                        "* The extra lamp is a small one, at the very end of the ledge, hanging from a hook that was not there yesterday. It is not one of yours. It burns with a clear, still, unfamiliar flame. It has been lit by someone who knew exactly how much oil to put in, and did not need a wick.",
                        "* None of you touch it. Nobody says it is a good sign. Nobody says anything at all. You go up the ladder, one by one, and count yourselves on the stairs, and the number is right, and nobody feels any better.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("edric", "The Baron's Heir").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "edric"))
                .intro(
                        "* The rider who comes to the gate on the fourth morning is young, in a plain cloak, with a satchel and a bad cough. He has the Baron's long jaw and his father's way of sitting a horse, as if he were at a funeral and the horse were the deceased. He will not give his name to the sentry. He asks for the head of the clan, and when told, he gets down and walks the last half mile on foot.",
                        "@Edric| I'm Edric Hexley. I'll say it plainly, since there's no good way to say it. I know what my father did. I know what the contract said. I've come to see the spring. I've heard it all my life.")
                .meet("edric", "Hear what Edric Hexley wants.")
                .outro(
                        "* He tells it in the kitchen, over Marta's soup, with his cloak off and his sleeves rolled, like a man giving evidence. He grew up in the Baron's castle fifty miles away. On the east side, in a cold room, he heard a note. It came up through the floor on still nights, and no one else heard it. His father said it was the wind. His nurse said it was a draught. His mother, who died, said nothing.",
                        "@Edric| I heard it from the age of four. I think I'm the one who told him where to look. I mentioned it to him once, at supper, as a child does. He went very quiet. He asked where in the room it was loudest. I said the north wall, by the window. He put down his spoon and looked at the map.",
                        "* The kitchen is silent. Marta stops stirring. Pilcher, in the corner, takes off his spectacles.",
                        "@Edric| It's not a story I'm proud of. I think I'm the thing that began it. I didn't want to hide that. I wanted you to hear it from me, and then decide whether I'm to be allowed to hear the note again.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("rota", "The Dumpling Rota").gap(4)
                .intro(
                        "* The trouble starts, as such things do, in the kitchen. A hundred and thirty people work three shifts, which means a hundred and thirty people want to eat at seven different times, and there is one pan. It is a very large pan. It has been in Marta's family for five generations, and it can hold forty dumplings at once, and she has not yet been persuaded to buy a second.",
                        "@Marta| It's not the pan. It's the principle. If I buy a second pan I'll have two pans to keep clean, and the day after that someone will want a third. It's how it begins. First it's the pan, then it's the cook, then it's a committee.",
                        "@Poll| Aye, and then it's bureaucracy.",
                        "@Marta| It's a dumpling.")
                .wait(2)
                .outro(
                        "* By the second week there is a rota on the kitchen door. By the third, there is a sub-rota, for the people who have been excluded from the rota on grounds of quarrel. The night shift says the day shift gets the fresh ones. The day shift says that the night shift gets the hot ones. A small, curly-haired boy of seven writes a petition and delivers it to the pan. It says, in a careful hand: MORE.",
                        "* On the fourth Thursday, with the dumplings an hour late, the entire night shift lays down its tools and sits on the floor of the kitchen with its arms folded. It is the first strike in the history of the clan. There is a long silence. Marta looks at them over the rim of the pan with an expression like a dam being considered.",
                        "@Marta| Right. Well. If it's to be a dispute, it'll be done properly. Pilcher! Fetch the register.",
                        "* The dispute is settled by Tuesday. It takes eleven resolutions, a motion of thanks, and the purchase of a second pan. Marta announces that she has not changed her mind. She says she has merely been outvoted by a pan. She cooks a double batch that night, in both, with her sleeves up, and her face in the steam, and her mouth set in the line of a woman who is, quietly, very happy.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("pollside", "Old Poll Goes Down").gap(3)
                .intro(
                        "* Poll has not been down the Deep. In all the years of the shifts he has stayed at the head, with the wheel, and the bell, and the cart-horse. He is a ferryman, he says. He knows water, and air, and the thing that lies between a boat and the bottom. He does not hold with holes. The clan has stopped asking. He has gone grey in the shaft house, and his boots have never been wet by anything but the rain.",
                        "* On the day the pit boy falls ill with a fever at the end of a shift, the bell for the last cage goes unrung. There is nobody on the ladder but the old man himself. He puts down his pipe, and takes off his coat, and says that somebody has to bring the lad up.")
                .wait(1)
                .outro(
                        "* He goes down the ladder like a man walking into cold water, one rung at a time, with his hands tight and his face white, counting. You go behind him. You count with him. At the sixtieth rung he stops, with his forehead against the rail, and says in a flat small voice: 'I was four. I was in the river, in the spring flood. They fished me out. I was under for what they said was a minute. It was the best minute of my life.'",
                        "@You| What do you mean?",
                        "@Poll| I mean I wasn't afraid. I should have been. I was under the ice, with my eyes open, and the whole river was singing. It was a low note. A long one. I've been listening for it ever since. Why do you think I took up the ferry?",
                        "* You reach the bottom. The lamps of the ledge are burning in a row, and at the far end, hung from the hook that was not there, is the extra lamp. It burns clear and still. Under it, on a bench, wrapped in a blanket, is the pit boy, asleep, with his mouth open. And the wall behind him is breathing, a slow silver surge and sigh, and the note is in the air, the note, as clear as a bell in a well.",
                        "* Poll stands in front of it with his hat in his hands. His face is wet. He is not ashamed. He says: 'Well, there you are.' And the vein, in a voice that every person on the ledge hears at once, says, with great gentleness: 'Hello.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("speak", "What the Vein Said").gap(6)
                .intro(
                        "* On the fortieth night of the sleepers, the vein speaks. It does not speak to the clan. It speaks to everyone in the valley at once, in the small hours, in the voice of whoever they have lost. A widow hears her husband. A boy hears a goat that died. Isla hears a woman humming over a fire, whom she has never met, in a language of no words.",
                        "* You hear, as you lie in the dark of the tent with your eyes open, a man's voice, dry and tired and sure. It is your uncle's. It says one word. It says it kindly. It says: 'Home?'")
                .outChoice(new Chapters.Choice("hum", "The vein has asked a question, and the whole valley is listening.",
                        "Answer it: say 'here' aloud into the dark.", "Hold your tongue. Do not feed it.", "Ask it a question back: 'Who are you?'")
                        .after(
                                new String[]{"* You say it. 'Here.' It is the smallest word in the language, and it goes down through the floor of the tent, into the earth, like a stone into a well, and you wait. A long time later, from very far below, comes a sound like someone setting down a heavy load."},
                                new String[]{"* You bite your tongue. You lie rigid, with your hand over your own mouth, and listen to the valley breathing. At dawn you find that nobody else answered either. The silence of a hundred and thirty people, all holding their breath, is a tremendous noise."},
                                new String[]{"@Vein| Cold.", "* It is not an answer. It is, you will think later, the first honest thing anyone in that family ever said."}))
                .wait(1)
                .outro(
                        "?hum=1|* In the morning the sleepers wake, and they remember. For the first time, every one of them remembers the dream, and each says the same thing, over the porridge: that someone had been waiting, and was glad.",
                        "?hum=2|* In the morning the sleepers wake, and they remember nothing. They are hungry and polite and grateful for the porridge. It is a mercy. You are aware that the question will be asked again.",
                        "?hum=3|* In the morning there is a word written in the dust of the shaft head, in a cold silver script that has the look of frost: COLD. Nobody knows who wrote it. By noon, it is gone, and the dust is warm.",
                        "* The bell goes at five.")
                .pay(0, 0, 1, 1, 0, 0);

        d.chapter("islanight", "Isla's Banner").gap(3)
                .intro(
                        "* The new banner is up on the pole by the wheel-house, a long, plain blue one, and for a week it is the only thing in the yard that does not hum. It does the opposite. It hangs in the wind in a dead quiet, as though it were holding its breath. The Deep hums, and the huts hum, and the cat hums; the banner is a patch of stillness on the pole, and anyone who stands under it can hear, quite clearly, the sound of their own heart.",
                        "@Isla| I didn't sew anything into it. I want you to know. It's only the blue. I didn't dream. I was very careful.")
                .wait(2)
                .outro(
                        "* On the Sunday, at dawn, Isla comes to the head of the shaft with a needle in her fingers. She is white to the lips. She has not slept. She is sewing, as she walks, without looking at her hands, a long line of silver thread on the hem of her sleeve, and when you ask what it is she holds out her arm. It is a drawing. It is done in stitches, quick and fine and quite unlike her usual work: a gallery, a roof, a line of rock coming down, and a figure with a shovel under it.",
                        "@Isla| It's the second gallery. It's Joren. It's Tuesday. I don't know how I know. I woke at four with the needle in my hand and the thread was already through.",
                        "* You look at the little silver picture. The roof comes down in it in a slow slant. The figure with the shovel has his arm up, as a man does who is about to say something amusing.",
                        "@You| Can you stop it?",
                        "@Isla| I tried to unpick it. It won't. It sews itself back.",
                        "* You go to find Joren. He is in the yard with his boots on his knees, making a sandwich. He says he has a shift on Tuesday. He says it is the second gallery. He is very happy about it, because the seam there is nice. It is a good seam, he says, with a fine grain, and he has been looking forward to the end of it all week.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("cave", "The Shift").gap(8)
                .intro(
                        "* It happens on a Tuesday, at nine in the morning, with fifty-one down. The wheel stops. The hum goes up a tone, and holds, and the whole of the Deep gives a single long groan, like a barn full of cattle shifting their weight.",
                        "* Then the roof comes in. Not all at once. A slow, grinding, deliberate settling at the far end of the second gallery, dust like flour in the lamplight. And a sound in the dark that you will remember all your life: a man shouting a name, and nobody answering.",
                        "@Tamsin| Joren. He's in the second gallery. He took the end of the line.")
                .wait(1)
                .outChoice(new Chapters.Choice("shaft", "Joren is under the fall, and the vein is humming louder than you have ever heard it.",
                        "Dig him out by hand, with every shovel in the camp.", "Call the vein: hum back, and ask it to open.", "Break the timbers and bring the whole gallery down, to reach him from above.")
                        .after(
                                new String[]{"* You dig. Fifty-one of you, in the dust, passing stone hand to hand along a line, for eleven hours. Poll sings. Pilcher reads aloud from a book. Marta feeds each of you a dumpling at the ladder, one at a time, like a priest giving a sacrament. At the twelfth hour, Tamsin's shovel strikes cloth."},
                                new String[]{"* You hum. It is not a good hum. It goes up and cracks. But it is the note, and the cave hears it. The rubble in front of you slides, grain by grain, like sugar in water, and a man's hand comes out of it, black with dust, giving a thumbs-up."},
                                new String[]{"* It is a terrible plan, and it works. The gallery falls with a noise like a hill sitting down, and in the new light from above there is a gap the width of a door, and Joren, grey to the hair, with his shovel across his chest."}))
                .outro(
                        "@Joren| I was fine. I want that said. The ceiling held up the whole time. It was like being held by a very large cat.",
                        "@Tamsin| It was a ceiling. It fell on you.",
                        "@Joren| It fell the other way. Around me. It made a sort of hole to sit in. It was quite comfortable. I think it was keeping me warm.",
                        "* Tamsin puts her arms round him, hard, and the shovel falls on her foot, and neither of them cares.",
                        "* The extra lamp, at the end of the ledge, is out. There is a little warm oil on the hook, as if someone had spilt it, going off in a hurry.")
                .pay(2000, 0, 0, 1, 0, 1);

        d.chapter("edricdig", "Edric Learns to Dig").gap(3)
                .intro(
                        "* Joren has taken Edric Hexley in hand, which is to say that he has given him a shovel. The young Baron is a poor digger. He has a scholar's hands, and a scholar's wrist, and the way he holds the handle suggests that he thinks it may go off. He digs the first hole for most of a morning, with his tongue between his teeth. It is a small, shallow, dispirited hole.",
                        "@Joren| Good. That's good. You've got the shape. Now deeper.",
                        "@Edric| It is deep.",
                        "@Joren| It's a bowl. I need a well.")
                .wait(2)
                .outro(
                        "* By the third day he is better. By the end of the week, he has a callus and a language. He says 'spoil' and 'tailings' and 'the face'. He wears a canvas cap. He has learned, from Joren, to put his ear to the wall of a trench and listen, and to know from the sound whether it will hold. He is good at it. He has an ear. He hears the note in the ground the way a bell-ringer hears a flat.",
                        "@Joren| It's the bit you can't teach. I've been doing this fifteen years. I can feel a seam. You can hear it. I think you're better than me. I'm furious.",
                        "* They sit on the lip of the new trench, in the evening, with a flask between them and their boots hanging over the drop. Edric looks at his hands. He says, in a quiet voice, that his father taught him to ride, and to fence, and to read a map, and to keep a ledger, and that in all those years nobody ever put a spade in his hand.",
                        "@Edric| He'd have hated this.",
                        "@Joren| Maybe. Pass the flask. I'll have a go at hating it on his behalf.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("duel", "Edric's Claim").gap(6)
                .intro(
                        "* The young Baron has been in the hall for six weeks. He eats in the kitchen. He sleeps in the loft over the school, with a stack of Pilcher's books for a pillow. He has been, by any measure, an excellent guest. He has also, on two nights, been found at the head of the shaft among the sleepers, humming, with his eyes open, in his shirt.",
                        "* The third time, he is standing at the edge, a pace from the drop. He wakes as you take his arm. He looks at you, and at the shaft, and at his own bare feet, and says in a perfectly ordinary voice: 'I have to go down.'",
                        "@Edric| It's asking for me. I've heard it for nineteen years. It's not asking for you. I'm sorry. I don't want a fight. But if you stop me, I'll have to fight you, and I'd like to apologise in advance.")
                .defeat("edric", "Stop Edric Hexley, who must go down.")
                .outro(
                        "?dead.edric|* It is quick and it is clean and afterwards you stand a long time with the sword in your hand. The shaft is silent. The hum has stopped. For the first time in a year, you hear the wind.",
                        "?!dead.edric|* He sits on the edge of the shaft with his head in his hands. He is not hurt. He is shaking. 'Thank you,' he says. 'I couldn't have stopped myself. I'm very glad that someone could.'",
                        "?!dead.edric|@Edric| Will you teach me to listen to it without going down?",
                        "?!dead.edric|@You| I don't know how.",
                        "?!dead.edric|@Edric| No. I thought not. Perhaps we'll find out together.",
                        "* The bell goes at five. Nobody has told it to stop.")
                .pay(3000, 0, 0, 1, 1, 0);

        d.chapter("quiet", "The Quiet Shift").gap(6)
                .intro(
                        "* The shaft is closed for a month. It is the longest month the clan has known. The pit-boys play knucklebones on the wheel-house floor. Marta makes bread. The hum goes on underneath all of it, steady, faint, and every night, a little after midnight, it rises at the end, as if it were about to ask something, and then thinks better of it.",
                        "* Isla keeps sewing. She has made, you notice, a very large banner: ten feet of blue, with a silver crane in the middle. She has not stitched in its eye.")
                .wait(4)
                .outro(
                        "* On the last night of the month, the whole clan comes up the path to the head of the shaft with lanterns, unasked. They stand in a ring. Nobody gives a speech. The vein, below them, hums, and a hundred and thirty-one voices hum back, ragged and shy and out of tune, the way a congregation sings a hymn it has half forgotten.",
                        "* And Isla climbs the wheel-house roof with the great banner and spreads it out on the slates, and bends over the crane, and puts in the eye with a single stitch. It is a gold eye. It is, as far as you can see from below, open.",
                        "* The hum stops. The silence is so large that Verdict the cat, on the school windowsill, sits up. And then from a great way below there comes one clear, soft, unmistakable sound, like a wing being unfolded in a very small room.")
                .pay(5000, 0, 1, 2, 0, 1);

        d.interlude(2, "The Eye of the Crane",
                "* The Deep is worked again, in the spring, on a new set of rules. The old ones are still observed. No one whistles. Nobody counts the dead. Every shift, a lamp is hung at the end of the ledge on the hook that was not there, and in the morning it is out, and there is a little warm oil.",
                "?shaft=1|* The clan's pit-boys still play a game called Digging Out Joren. It is exactly as rough as it sounds, and there are rules.",
                "?shaft=2|* Children now hum before they speak. Pilcher says it is a phase. Marta says it is a language.",
                "?shaft=3|* The second gallery is a bright hole in the hillside, with a rope across it and a sign. DO NOT FALL IN. Nobody has ever fallen in. It is tremendous.",
                "?dead.edric|* A grave in the burying ground has no name. Isla embroiders the cover every spring with a crane, in blue, and a tiny gold eye.",
                "?!dead.edric|* Edric Hexley lives in the loft over the school, and teaches the older children to read maps. He wakes some nights in his shirt at the head of the shaft. Someone always comes out with a blanket.",
                "* On a clear night in autumn, a traveller stops at the gate, a thin, brown man with a sack and a staff, who asks for tea. He pays at the end with a small bright coin. One face has an old man on it, laughing. On the other, in a ring of fine writing, stands a crane on one leg.");

        // ================================================================== SEASON 3
        d.season(3, "The Crane's Flight");

        d.chapter("coin", "A Coin With a Crane").years(2)
                .intro(
                        "* You take the coin to Pilcher, because it is a thing he would want to hold. He holds it for half an hour under a glass, in the schoolroom, in the late sun, saying nothing, with the cat on his knee. Then he puts it on the table and takes off his spectacles. In a voice you have not heard from him, he says that it is the first thing in his whole life that he has not got a book about.",
                        "@Pilcher| It's not a coin of any state. It isn't a token or a medal. The writing round the edge is a language I don't know. It looks like the footprints of a crane on a muddy bank. And the crane on the back is yours. It's yours, on one leg, with the eye open. It's exactly the crest.",
                        "@Pilcher| And the old man is laughing. I've never seen a coin with a man laughing on it. Who spends their money on a joke?")
                .wait(2)
                .outro(
                        "* The man who paid with it comes back in the evening. He leans on the gate with his staff and his sack, and he tells you, with a certain sheepishness, that he did not mean to part with it. It was the only coin he had. He had meant to give it to someone who deserved it, and then he'd been so thirsty.",
                        "@Traveller| It's an old coin. A friend gave it to me. He said it was good for a drink anywhere, as long as the barman had a sense of humour. I had it from a man who told jokes at the end of the world. I had the crane before that. She was a very fine bird.",
                        "@You| You knew her?",
                        "@Traveller| I knew the whole family. They were not what you would call a chatty family. They were fond of tea. I wonder, do you know what the tea is for?",
                        "* He smiles, and tips his hat, and goes off into the dusk. At the bend he turns round and says, quite kindly: 'You might ask the cup.'")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("oldjoke", "The Hall's Oldest Joke").gap(2)
                .intro(
                        "* There is a joke the clan tells, at funerals, to the new bride, to the child who has lost a tooth. It goes: Why do cranes stand on one leg? And the answer, which every member of the clan has known from the age of four, and which is delivered in a special voice, with a pause, is: To keep the other one for the kettle.",
                        "* You have heard it a thousand times. You have never once thought it was a joke about anything. This evening, as the light goes, you hear Pilcher, over the washing up, tell it to the cat.")
                .wait(1)
                .outro(
                        "@Pilcher| I looked it up. Naturally. I cannot help myself. It's a very old joke. It is, as far as I can find, the oldest thing the clan has. Older than the motto. Older than the banner.",
                        "@You| What does it mean?",
                        "@Pilcher| It's a note on a lullaby. 'Keep the other for the kettle.' It is a stage direction. The person doing the tea stands on one leg, in the cellar, so that the other is free to tap out the beat. It's a rhythm. Slow, slow, quick. The whole joke is a set of instructions for keeping a very large animal asleep.",
                        "* He stops, with a plate in his hands. He looks at it as if he had never seen a plate. Then he laughs, a high thin delighted laugh, and sits down on an upturned bucket in the middle of the scullery.",
                        "@Pilcher| Four hundred years. It's the best joke in the history of the world and nobody in it knew the punchline.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("children", "The Children's Question").gap(1)
                .intro(
                        "* The delegation comes on a Sunday afternoon: eleven children between six and twelve, in a row, with their hands behind them, in their best clothes, which have been put on in a hurry. At the front is a girl of ten called Bryony with a plait, a determined face, and a sheet of paper that has been folded and unfolded so many times that it has gone soft as cloth.",
                        "@Bryony| We have a question. We've been told you'd answer. We've voted. It was a close vote. It was seven to four.")
                .wait(1)
                .outro(
                        "* She reads it out in a high clear carrying voice, like a child at a prize-giving. 'The first question. If there is a bird under the hall, and it is very big, and it has been asleep for a very long time, does it know about us? The second question. If it wakes up, will it be cross? The third question. Is it true that the tea is for the bird? Because if it is, it ought to have milk. We have had a meeting about that, and it is the one point on which we were unanimous.'",
                        "* The room is very quiet. You are aware of Tamsin behind you, in the doorway, with her arms folded, making a sound in her nose that might be a laugh. You get down on one knee so that your face is at the level of Bryony's, and you give each of them in turn the answer that you have, which is that you do not know.",
                        "@Bryony| That's not an answer.",
                        "@You| It's the true one.",
                        "@Bryony| (after consideration) It'll do. We'll put it on the minutes.",
                        "* She folds the paper. She turns, and the line turns with her, and files out of the door in order of height. At the threshold the smallest, a boy of six who has not said a word, looks back at you with a face like a tiny owl, and says, loudly, to the room: 'Tell her thank you for the tea.'")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("cup", "Ask the Cup").gap(4)
                .intro(
                        "* The tea ceremony is the oldest custom of the clan, older than the hall, older than the motto, older than the name. Every night, at midnight, the head of the clan takes a cup of tea down to the cellar, and sets it on the flat stone by the well, and says three words: 'Sleep, grandmother. Sleep.' Then he drinks half and pours half out on the floor. Nobody remembers why. It is just done.",
                        "* You have done it a hundred times. This night you leave it undrunk. You sit on the flat stone with the cup between your hands and ask it. It is an ordinary cup. It is chipped at the rim. It has a pattern of small blue cranes.",
                        "* The cup is warm. It is warmer than the tea. And the hum comes up through the stone, up through the soles of your feet, up into the china, and the cup begins, very quietly, to speak.")
                .at("deep", "Go down into the Deep, at {@deep}, with the cup.")
                .outro(
                        "* It is not a voice. It is a pressure on the mind, like a hand laid very gently on the top of the head. It says: grandmother. It says: sleep. It says: four hundred years. It shows you, one after another, in the cup, the faces of the heads of the clan who have gone down these steps with a cup in their hands.",
                        "* The first is a woman in a cloak, in the dark, with a torch, kneeling beside an opening in the rock, weeping. She is not afraid. She is sad. She pours the tea from a cup into the pale light and says three words. And in the vein, a great way down, something very large, turning in its sleep, settles.",
                        "* It is not a spring. It is not a vein. It is a bird. It is lying on its side in the earth, folded, with its neck curled round and its head tucked under its wing, as cranes do, and it is as long as a valley. It is breathing. The silver light is the shine of its feathers. The hum is its sleep.",
                        "@Joren| Oh. Oh, I see. That's why the hole was so good.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("whystill", "Four Hundred Years of Tea").gap(4)
                .intro(
                        "* There is a book about it. There is always a book. Pilcher finds it in the clan's cellar, behind the wine, in an oilcloth, in a hand that is not a clan's: a brown, spidery, flowing hand that has been laid over the page like a veil. It is called THE LULLABY OF THE FIRST.",
                        "@Pilcher| It's not a history. It's an instruction. It's a set of instructions to a household, from a person whose name I can't read, in a tone of enormous patience, about the care and keeping of an extremely large animal.",
                        "* He reads it aloud in the schoolroom with the cat on his knee. It takes three nights. It is, in places, extremely funny.")
                .wait(3)
                .outro(
                        "* The First Crane is older than the Garden's cities. She came down out of the Outside, when the world was new, in a great fall of silver, a bird the size of a hill. She was hurt. She was looking for somewhere to sleep. Some of her family stayed to keep her. They built a house on her back, and kept her asleep with tea and with quiet, and called themselves cranes, and forgot why.",
                        "* The tea is a lullaby. Not for the bird. For the people. It is the thing they do every night so that they do not go down the steps, wake the bird, and ask where she came from. Because she is a way out. Wherever the Crane came from, she can fly back. And if she does, the world's roof will open, and the Quiet will see in.",
                        "@Tamsin| So that's what the motto is. Stand on one leg. See far.",
                        "@Isla| It's what she does. When she sleeps. It's what she looks like asleep. On one leg. With her eye shut. She's looking at where she came from.",
                        "* Nobody says anything until the lamp burns low. The cat gets down off Pilcher's knee and goes to the door and sits there, facing the cellar, with her tail round her feet.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("verdict", "Verdict").gap(2)
                .intro(
                        "* The cat has been odd for a month. It has stopped sleeping on the schoolroom windowsill. It sits instead on the flat stone by the well, in the cellar, facing the dark, with its tail wound neatly round its feet, and does not move for hours. When anyone comes near it opens one eye and closes it. It has the look of a small, stout, orange, furious priest.",
                        "@Pilcher| He is on duty. I think he has been on duty a very long time. I think it's his job.")
                .wait(1)
                .outro(
                        "* You take your turn at the vigil. It is not a thing you were asked. You go down at midnight with a lamp and sit with the cat on the stone, and the long silver breath of the Deep going in and out behind the wall, and neither of you speaks. At two the cat gets up, stretches, and puts a paw, quite deliberately, on your knee.",
                        "* It looks at you. Its eyes, in the lamplight, are an extraordinary green, and not at all those of an animal. It says nothing. It is a cat. But there comes into your mind, quite clear, as if it had been put there with a finger, a picture. It is a long grey hall. It is full of tall figures in coats the colour of snuffed candles, standing in rows with their hands folded, waiting.",
                        "* You look away. When you look back, it is a cat again, asleep on your knee, with its nose in its tail. You sit very still till dawn. You are not at all sure that you have not been warned.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("nought", "Captain Nought").gap(3)
                .setup((c, st) -> Chapters.spawn(c, st, "nought"))
                .intro(
                        "* The last lamp in the Deep goes out at four in the morning. All hundred and thirty-two of them, one after another down the ledge, as though one great breath had been drawn in. The hum falters. The wheel stops. There is a smell of cold snuffed wicks.",
                        "* And in the grey light on the road, in front of the gate, a rider sits his horse and looks at the hall. He is tall, and thin, and old, in a grey coat the colour of a candle that has gone out, and he carries nothing at all. The horse is grey. His hair is grey. His eyes, when he turns them on you, are the exact colour of the inside of a snuffer.",
                        "@Nought| Captain Nought. I do hope I find you well. I won't keep you. You will have a great deal to do. I have come about the bird.")
                .defeat("nought", "Face Captain Nought of the Hollow Court at the gate.")
                .outro(
                        "?dead.nought|* He falls from the saddle without a sound. The horse stays where it is for an hour, then wanders off down the valley, and the lamps in the Deep, one by one, begin to come back on. It is the most frightening thing you have ever seen, and it is only people lighting candles.",
                        "?!dead.nought|* He gets up from his knees in the road and looks at the hall for a long, thoughtful moment. 'It is not a very good house,' he says, 'but it is a good family.' Then he mounts his grey horse, and rides away down the valley, slowly, and does not look back. The lamps begin to come on.",
                        "* You find a thing in the dust where he fell. It is a small brass snuffer, the length of your finger. On the handle is engraved a single word: NOW.")
                .pay(5000, 0, 0, 1, 1, 1);

        d.chapter("washing", "Four Hundred Years of Washing Up").gap(2)
                .intro(
                        "* The clan decides in the way the clan decides everything, which is at length, in the kitchen, with food. It is the largest meeting in its history. A hundred and thirty-one people sit on the floor and on the tables and on one another. Pilcher takes the minutes. Marta keeps the pan going. Poll says that in his experience no good has ever come of a meeting with more than three people in it.",
                        "@Tamsin| We'll have a show of hands. Wake, sleep, or I don't know.",
                        "@Joren| What if I want to wake her and sleep her?",
                        "@Tamsin| Then you're an 'I don't know'.")
                .wait(2)
                .outro(
                        "* The debate lasts nine hours. Every point is made. A man called Dunnock argues that waking her would be a kind of theft, since she has been asleep for four hundred years and cannot be asked. A girl of twelve replies that sleeping her is a kind of theft too, since she cannot be asked that either. A grandmother says that in her day a person did what they were told. A grandfather says that in her day they were told by a bird.",
                        "* At the eighth hour, someone asks Isla. She has not spoken once. She sits at the end of the long table with the great banner in her lap, sewing the border, with her head down. She says, without looking up, in her small clear voice: 'I think it's not about her. I think it's about whether we're afraid. And I think we've been afraid for four hundred years, and it was a very good reason, and I should like to stop.'",
                        "* The room is quiet. Poll clears his throat. He says that he has changed his mind, for the first time since the Flood. He says a clan which has had a good reason for four hundred years has earned the right to try another.",
                        "* The vote is carried by a hundred and nineteen to twelve. The twelve are asked to say why. They say that they would simply like it noted that they were against it, so that if it goes wrong they may say so. Marta writes it on the door of the pan.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("lastrule", "Pilcher's Last Rule").gap(1)
                .intro(
                        "* The book of rules is full. He tells you so with a sort of dry astonishment, as if it had happened without his consent. Two hundred and eleven rhymes, ninety-three omens, nine recipes for the dumplings, and the entire history of the clan's quarrels, set out in chronological order. There is one page left. It is blank. He has put a ribbon in it, and has been looking at it for three days.",
                        "@Pilcher| I cannot think what to write. It seems wrong to leave it empty. I have been a schoolmaster for forty years. A blank page is the one thing I cannot abide.")
                .wait(1)
                .outro(
                        "* He tries a number of things. He tries 'Always say good night to the dark', and crosses it out. He tries a rhyme, which he writes and reads aloud and tears into small pieces. He tries to put down an entire account of the history of the clan, in a paragraph, and gets as far as the second line. He sits with the pencil in his teeth until the cat gets onto the desk, and onto the page, and sits down in the middle of it.",
                        "@Pilcher| Verdict. I beg of you. This is a very serious book.",
                        "* The cat does not move. It looks at him. It is the yellowish, steady, tolerant look of a creature that has watched a man try to get something right for a very long time. It puts out one paw, slowly, and sets it on the page, and leaves the faint round print of a pad in a little smudge of ink.",
                        "* Pilcher looks at the paw-print until the candle burns down a finger. He takes off his spectacles. He wipes them, and puts them on again, and looks again. Then he picks up the pencil, and under the print, in his small, precise hand, he writes the last rule of the book. It is four words long. It says: Let the cat decide.",
                        "@Pilcher| It has the great merit of being true.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("wake", "Wake Her").gap(2)
                .intro(
                        "* The decision, when it comes, is not a hard one to understand. It is a hard one to make. You sit on the roof of the hall in the dark with the three cousins, and the cup, and the hum going round and round under your feet, and the whole valley below you with its small lights.",
                        "@Tamsin| If we wake her, the Quiet knows where she is.",
                        "@Joren| If we don't, we pour tea for another four hundred years.",
                        "@Isla| She's been asleep so long. I'd like to ask what she'd like.")
                .wait(1)
                .outChoice(new Chapters.Choice("wake", "The First Crane has slept for four hundred years in the clan's cellar.",
                        "Wake her, and let her fly home.", "Let her sleep: the clan has kept the watch for four hundred years and can keep it for four hundred more.", "Wake her, and ask her to stay.")
                        .after(
                                new String[]{"@Tamsin| Right. Well. The kettle's on.", "* She pours the cup out, in the dust of the roof, and says the three words backwards. 'Wake, grandmother. Wake.'"},
                                new String[]{"* You pour the cup out, for the four hundredth year, and say the three words. 'Sleep, grandmother. Sleep.' The hum settles. Below, in the dark, a great white shape turns over in the earth and goes back into its dream."},
                                new String[]{"@Isla| We could ask. She might like it here.", "@Joren| It's a good hole.", "* You pour the cup out and say, instead of the old words, a new one: 'Wake, grandmother. Wake. We would like to talk.'"}))
                .outro(
                        "?wake=1|* The mountain moves. It is a slow, vast, orderly movement, like a cathedral choosing to stand up. The hall rises on a swell of silver, and the roof comes off in one piece, and all the lamps in the valley go up at once into a sky that has, for a moment, no ceiling.",
                        "?wake=2|* The Deep goes quiet. In the morning there is frost on the stones, and the cat sleeps in the cellar. It is the last time anyone will pour the tea out of fear. Hereafter it will be poured for love.",
                        "?wake=3|* The voice, when it comes, is the voice of a very old woman who has been dreaming of a long time ago. It says: 'Child, there is tea.' It is the oddest thing, and the most natural, to be thanked for a cup you did not know you were bringing.")
                .pay(10000, 0, 0, 2, 0, 1);

        d.chapter("lasttea", "The Last Night of Tea").gap(1)
                .intro(
                        "* If it is to be done it will be done at midnight, as it has always been, in the cellar, with a cup. The clan comes down the steps in a long line with lanterns, a hundred and thirty-one of them, one after the other. They line the walls of the old cave until the whole of the Deep seems to be wearing a necklace of lights.",
                        "* Tamsin has made the tea. She has made it in the old pot, the blue one with the cracked lid, as her mother did, and her mother's mother, and so on back through the generations to a woman in a cloak with a torch. It is a plain black tea. There is nothing in it. There is nothing in it that has not been in it for four hundred years.")
                .wait(1)
                .outro(
                        "* You take the cup. It is warm. You stand on the flat stone by the well with the cup between your hands. The long soft silver breath of the sleeper goes in and out behind the rock. You find that you have nothing prepared to say. You had thought of a speech. You had thought of something about duty. All you have is a cup of tea.",
                        "@Isla| You don't have to say anything. She knows who's there.",
                        "* So you say the old words, one more time, the three of them, as they were said. 'Sleep, grandmother. Sleep.' It is a simple thing. It is the plainest thing in the world. A hundred and thirty-one people breathe out. And you add, at the end, because there is room, three more words that you had not known you were going to say. 'Thank you. Wake.'",
                        "* You pour the tea out on the stone. It does not run away. It stands in a small round pool of dark gold, quite still, in the lantern light. Then it begins, very slowly, to shine.",
                        "* The hum stops. It stops the way a held note stops in a church, not with a cut but with a settling. The silence that follows is so wide and so clean that nobody moves. Then you hear above you, through the rock, from a very long way up, a sound you have never heard in your life. It is the sound of a wing being unfolded in the dark, one feather at a time.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("flight", "The Crane's Flight").gap(1)
                .intro(
                        "* She comes up out of the earth at dawn. There is no other way to describe it. The whole hillside heaves and unfolds. A bird the size of a valley raises her neck out of the cleft of the Deep, white as a sheet, with a crest like a frozen wave and an eye as bright and gold as a coin. Her wings go out, one after the other, and the clouds go through them.",
                        "* The clan stands on the roof of the hall, with the banner. Isla, who has made them for every day of her life, has made one more: the largest she has ever done, a crane with an open gold eye on blue. She holds it up in both hands, and the Crane looks at it, and at the clan, and at the three cousins, and lowers her head.")
                .wait(1)
                .outChoice(new Chapters.Choice("sleeper", "The First Crane has lowered her neck to the roof, and it is wide enough for a family.",
                        "Ride: go with her into the Outside, with whoever will come.", "Stay: wave her off, and keep the hall.", "Send the children, the sick, and the old: all who would be safer in the Outside.")
                        .after(
                                new String[]{"@Tamsin| I'm not going. Someone has to look after the goat.", "@Joren| I'll go. I'd like to dig somewhere else.", "* The Crane waits. She is, you notice, remarkably patient. It is, you suppose, the other half of being asleep for four hundred years."},
                                new String[]{"@Isla| You'll be all right.", "@You| Will I?", "@Isla| I think the hall's more of a person than it was. Go on. I'll keep the tea warm.", "* You do not know, until you have said it, that you are going to stay."},
                                new String[]{"* It takes the day. The old go first, in blankets. Pilcher carries the cat. Marta carries the soup. Poll, with the face of a man who has been asked to cross the last river, steps on to the wing and says he'll have a look at the weather."}))
                .outro(
                        "?sleeper=1|* You go up. The wing is warm, and soft, and wider than a street. The whole valley falls away beneath you, small and green and lit, with the stones of the hall like a handful of dropped sugar. Joren is whooping. Marta is holding on to her pan. And ahead, where the cloud-roof of the Garden lies, there is a crack of white light, thin as a hair, growing.",
                        "?sleeper=2|* You wave from the roof. The Crane climbs out of the valley, wide-winged, in a rush of silver. The whole clan stands on the grass and watches until she is a speck, and then a rumour, and then a thing you may have imagined. Somebody blows their nose. Somebody puts the kettle on.",
                        "?sleeper=3|* The old and the young go up in rows, with their blankets and their dumplings, and they wave. It is a long wave. It goes on until the sky shuts. The hall is very quiet. Isla sits on the doorstep and sews a small banner, and when it is done she hangs it from the window, for them to see.",
                        "* The hum is gone. For the first time in your life, you hear the valley as it is, with the birds in it, and the wind, and the creak of a gate. It is a lovely sound. It is, you find, rather lonely. It will take a while to get used to.")
                .pay(30000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* The Silver Crane Clan is not famous. It is not rich. It is a bit too fond of tea, and nobody now remembers why, and everybody says it is because of the bird.",
                "?wake=1|* The First Crane is somewhere in the Outside, in a sky that has no roof. Sometimes a silver feather comes down in the spring, in the north meadow, as long as a spear. It is kept in the school.",
                "?wake=2|* The tea is still poured at midnight, in the cellar, by the head of the clan. The three words have changed. They are now: 'Sleep well, grandmother. Thank you.'",
                "?wake=3|* The First Crane lives in the valley. She sleeps most of the day in a field with her head under her wing, and the cows walk round her with tact. The tea is poured at midnight. She drinks it.",
                "?sleeper=1|* Letters come, in long loops, from a place where nothing is written in the same hand twice. Joren has dug a very good hole.",
                "?sleeper=3|* The old and the young are somewhere warm. A banner hangs in the window of the hall.",
                "?dead.nought|* A small brass snuffer hangs on a nail above the hall door. It says NOW. Isla keeps it polished, as a reminder.",
                "?!dead.nought|* A grey rider is sometimes seen on the road at dusk. He never comes closer than the gate. Marta leaves a dumpling on the wall.",
                "* Stand on one leg, the motto says. See far. You can see a long way from the roof, these days. You can see the whole of the valley, and the hall, and the place where a bird once sat, and the road that goes on past it, and the sky.");
        TaleCraneMore.add(d);
    }
}
