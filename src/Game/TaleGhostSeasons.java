package Game;

/** The Ghost-Touched, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "Dead Letters" (hard-boiled casework with a wink; the author's comic voice): the dead come one at a time with something unfinished, and the living they need are REAL people of the world: a rival who owes three coppers,
 *  a friend who has been sorry for thirty years (Chapters.adopt), and the human's own dead relatives (Chapters.deadKin: the family tree outlives the dead; with none, a great-aunt nobody remembers). Mrs. Pang and the goose are
 *  the running gags. Mister Tally of the Office of Returning wants the deliveries stopped. Flags: door (from season 1), cheese (what the human told Dorrit), tally (the paper duel), pang (how the Pangs left).
 *  Season 3, "The Door" (cosmic, tender): the Cistern of Returning, the queue, Meng, and what a door does with itself. Flag: lastdoor. */
final class TaleGhostSeasons {
    private TaleGhostSeasons() { }

    static void add(Chapters.Def d) {
        d.flags("cheese", "tally", "pang", "lastdoor");
        d.place("field", "wild").place("grave", "wild").place("palace", "start");
        d.cast("rival", "Hobart", "Blunt").he().trait(Character.Traits.Merchant).side("Neutral").lvl(1).age(48).at("far").rel(-2).free();
        d.cast("friend", "Marguerite", "Dale").she().trait(Character.Traits.Friendly).side("Good").lvl(1).age(40).at("near").rel(3).free();
        d.cast("tally", "Mister", "Tally").he().trait(Character.Traits.Scribe).side("Neutral").lvl(5).pot(6).age(64).at("here").rel(0);

        // ================================================================== SEASON 2
        d.season(2, "Dead Letters");

        d.chapter("coppers", "Three Coppers").years(3)
                .setup((c, st) -> Chapters.adopt(c, st, "rival", "rival"))
                .intro(
                        "* It is a wet Tuesday, which is the day the dead do their paperwork. You know this because there is a line of them outside the door and nobody has taken an umbrella, which is the only polite thing about being dead.",
                        "?door=1|* The shop at the bridge has a lantern in the window and a sign that says LOST PROPERTY (SPIRITUAL), and below it, in a smaller hand, NO LIVING PERSON IS REQUIRED TO ENTER. A good many come in anyway, for the tea.",
                        "?door=2|* You keep a ledger, and it has got fat. There are forty-three names in it now. You write each of them down in the evening, with the date, and the thing they wanted, and how it went.",
                        "?door=3|* You closed the door, once. A closed door, you have learned, is only a louder kind of knock.",
                        "* The first client of the morning is a thin, damp, fussy man in a coat that may have been good, with a leather satchel and the expression of someone who has been told that a thing will take five minutes.",
                        "@Quibble| Quibble. Tax collector, retired, unexpectedly. I was told this was the place for outstanding business. I have a debt. Three coppers. I have been trying to collect it from a person called {rival.name} since the spring of the Long Frost, and I keep missing {rival.him}. First {rival.he} moved. Then I died. I do not propose to let a technicality come between a man and his money.",
                        "@You| Three coppers.",
                        "@Quibble| It is the principle of the thing. The principle is three coppers.")
                .meet("rival", "Find {rival.name} and collect three coppers for a ghost.")
                .outro(
                        "* {rival.name} turns out to be a large, tired, defensive person who answers the door as though you had come to arrest {rival.him}. {rival.He} listens, and goes red, and then goes pale, and says that it is the stupidest thing {rival.he} has ever heard in {rival.his} life.",
                        "@{rival.first}| He lent me three coppers for a pie. In the year of the Long Frost. I ate the pie. I never paid him the coppers.",
                        "@You| He says it is the principle.",
                        "@{rival.first}| Of course it's the principle. He chased me through four towns. He sent me a letter every spring for eleven years, and every one of them started 'Dear Sir, I regret to remind you'. It was the only post I got. I used to look forward to it.",
                        "* The coppers are in a jar on the dresser. {rival.He} counts them out onto your palm with a strange slowness, like someone handing over the keys to something. Then {rival.he} stops, and stands in the doorway, and says, in a different voice: 'Is he there? Is he listening?'",
                        "@Quibble| Tell him I forgive the interest.",
                        "* You tell him. {rival.first} laughs, helplessly, and takes off {rival.his} hat, and holds it against {rival.his} chest in the rain, for as long as it takes a debt to go out of the world.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("ring", "The Weight of a Ring").gap(10)
                .setup((c, st) -> Chapters.adopt(c, st, "friend", "friend"))
                .intro(
                        "* The second case arrives in a good dress. She is tall and rather cross, with a veil she has pushed back over her hair like a woman who has decided that dignity can wait. Her name is Ilse, and she was to be married on a Thursday, thirty-one years ago, and there was, she explains, a pond.",
                        "@Ilse| I did not die romantically. I would like that on the record. I died because I went to look at the ducks in my wedding shoes. But the person I was marrying is still alive, and has been sorry for thirty-one years. It is the way people are sorry when they have nothing else to do with their hands, and I am tired of watching it.",
                        "@Ilse| It is your friend. {friend.first}. They never married anyone else. They have a ring in a drawer. Tell them it was the ducks. Tell them I wasn't the kind of person who needed protecting, and I'd be furious if they thought so. And tell them to eat something.")
                .meet("friend", "Find your friend {friend.first} and tell them about the ducks.")
                .outro(
                        "* {friend.first} receives the news with the careful expression of someone who has been told the roof is coming down and is checking whether it matters. You tell the story plainly. The ducks. The shoes. The veil pushed back. The part about being furious.",
                        "* When you finish, there is a very long silence. Then {friend.first} gets up, and goes to a drawer, and returns with a small ring in a cloth, and sets it on the table between you. They look at it as though it were a verdict that someone else had finally read aloud.",
                        "@{friend.first}| She'd have hated the wedding. All those people. She said so, you know. She wanted to elope to a barn.",
                        "@Ilse| A barn. At last somebody remembered.",
                        "* You are not sure whether {friend.first} can hear her. You are quite sure that {friend.he} does not need to. {friend.He} puts the ring back in the drawer, and shuts it, and asks you whether you have eaten. It is the first time in thirty-one years that anyone has seen {friend.him} offer food to a guest.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("cheese", "How Did I Die?").gap(10)
                .intro(
                        "* The third client does not know how she died, and she minds. She is a broad, brisk, weathered woman in her fifties, with the forearms of someone who has hauled things. She stands in the middle of the office with her hands on her hips, like a ship's captain about to complain to the harbourmaster.",
                        "@Dorrit| Dorrit Hale. I've been dead for six weeks and nobody will tell me what of. I've asked the dogs. I've asked the old men on the benches. I've asked Mrs. Pang, who was very nice about it and then talked for an hour about her husband. It's a matter of professional pride. I was never ill. I was never reckless. I'd like to know what I did wrong.",
                        "@Mrs. Pang| I was very nice about it.",
                        "* Mrs. Pang is in the corner, translucent and satisfied, folding a bit of paper into a smaller bit of paper. She has been with you since the first spring. Nobody knows what she does, exactly. The filing, perhaps. She has, in any case, never been on time, which is an odd quality in a person who has all of eternity.",
                        "@Dorrit| There was a field. That's all I remember. A big green field, and a hill, and a lot of people shouting. After that it's very bright.")
                .at("field", "Go to the field at {@field} where Dorrit Hale was last seen alive.")
                .outro(
                        "* The field is on a hill above a village, and on the first Sunday in spring the village is the sort of place that people drive in from three counties to be hurt in. There is a banner across the gate. THE GREAT GORGE CHEESE ROLL, it says. Below this, in a newer paint, THE DORRIT HALE CUP.",
                        "* It takes ten minutes to find the man in the pub who saw it. He tells the story in the flat, careful voice of someone who has told it many times and still has not got it to stop being true.",
                        "@Publican| She won it eleven years running. Eleven. Biggest cheese on the hill, and the best line, and nobody had ever got near her. Last Sunday she took the twelfth. Crossed the line a good ten yards ahead. Turned round to wave. And the second-place cheese, forty pounds of it, which had got a bit above itself, caught her on the ankle.",
                        "@Publican| She went over like a sack. Never woke. The village has been in mourning for six weeks. We've named the cup after her.",
                        "* Standing in the pub's yard, between a pile of barrels and a goose who is looking at you with a faint, pitying, familiar air, you find you need to sit down. There are many ways to die. You had half hoped for a better one on her behalf.",
                        "* The goose sniffs. It has seen it all before. It is possible, you think, that it was here on the day. It is possible that it has been everywhere.")
                .outChoice(new Chapters.Choice("cheese", "Dorrit is waiting at the office to hear how she died.",
                        "Tell her the truth, all of it, including the cheese.", "Tell her she won, and was struck down in her moment of glory.", "Tell her it was nothing: she didn't feel a thing.")
                        .after(
                                new String[]{"@Dorrit| A cheese.", "@You| It was a very big cheese.", "@Dorrit| I was killed by a cheese. After eleven years. I have been in the dairy business. There is a certain justice in it, if you stand far enough back. Oh, I'm furious. And it's rather funny. Give me a minute."},
                                new String[]{"@Dorrit| I won?", "@You| By ten yards. They named the cup after you.", "@Dorrit| The Dorrit Hale Cup. Well. I never in my life expected it to be a cup.", "* She says it quietly, and when you look up she is smiling in the way people do when they have decided to go on a long time with something."},
                                new String[]{"@Dorrit| Nothing?", "@You| It was very quick.", "@Dorrit| Hm. I'm not sure that's a comfort. But I'll take it.", "* You are aware that this is the cowardly answer. You are aware she knows."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("boundary", "The Boundary Stone").gap(6)
                .intro(
                        "* The fourth case of the season comes with a living client, which is unusual, and a goose, which is not. The client is a stout farmer called Arbuthnot Gripe, in a coat the colour of a wet field. He sits down on the visitor's chair and says that he has been told this is the place for 'matters of an unfinished character'. He has the look of a man who has tried everything else.",
                        "@Gripe| The south field. It was my uncle Ferris's, and Ferris was the surveyor, and Ferris is dead, and the will says the south field goes 'to him that the goose approves'. I've a neighbour, Mrs. Ploverwick, who says that means her. I say it means me. We've been to the magistrate. The magistrate said, quote, 'What goose?'",
                        "@Mrs. Pang| I do hope it's the goose.")
                .wait(1)
                .outro(
                        "* The ghost of Uncle Ferris is already in the office when you come back from lunch. He is a thin, upright man in a surveyor's coat, with a chain over one shoulder and a theodolite that he does not seem to have put down since the funeral. He looks at you down the long length of a nose that has measured a great many fields.",
                        "@Ferris| I want it noted that I was perfectly clear. I said the goose. I did not say a goose, or any goose, or the sort of goose that is available. I said the one that saw. I was at the north hedge the day I died, and I stayed there till the light went, and there was exactly one witness to what I did.",
                        "@You| What did you do?",
                        "@Ferris| I moved the boundary stone. Four feet. In Gripe's favour. I felt it was fair. I did it for a reason that I took with me.",
                        "* The goose, when you find it, is standing on the boundary. It is a large, grey, pitying goose, and it is standing with one foot in each field, exactly on the line, where the stone used to be, and it will not move. It looks at Gripe. It looks at Mrs. Ploverwick. It looks at you with an expression that you have come to know very well.",
                        "* You walk it down to the stone, which is lying in the hedge. You put the stone back where Ferris said. The goose watches. When the stone has been set in the hole, with four feet of Gripe's field now on Mrs. Ploverwick's side of it, the goose takes one step to the left, plants itself squarely in Gripe's territory, and honks. It is not a happy honk. It is the honk of an animal who has been asked a question at the end of a long day and has decided to answer it honestly.",
                        "@Mrs. Ploverwick| It's me?",
                        "@Gripe| It's her?",
                        "@Ferris| It is neither. It is both. I wished to see them in the same field. They have been quarrelling for thirty years over a hedge. I knew they would have to sort it out between them or sit down with the goose.",
                        "* They sit down with the goose. It takes the afternoon. By evening there is a pot of tea between them, a drawing of the field, and an arrangement under which each will cut the other's hay. The goose, in the corner, has the expression of a creature who is quite prepared to be paid in grain.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("kin", "The Visitor").gap(14)
                .setup((c, st) -> {
                    if (!Chapters.deadKin(c, st)) { Chapters.word(st, "kin", "Perpetua Quill"); Chapters.word(st, "kinrel", "your great-aunt, on your father's side"); Chapters.word(st, "kinfirst", "Perpetua"); }
                })
                .intro(
                        "* The fourth visitor does not come in the usual way. There is no queue, no knock, no damp umbrella. You wake at three in the morning to find a figure sitting on the end of the bed, with its hands folded and its head a little on one side, like a bird that has remembered something.",
                        "@Ghost| Hello, dear. Don't be frightened. It's only me.",
                        "* It is {=kinrel}, whom you have not seen in a very long time. It is {=kin}. The face is the one you remember, and slightly out of focus, the way a face is when you look at it from the other end of a long hall.",
                        "@{=kinfirst}| I've been standing at the back of the queue since spring. I didn't like to push in. I thought you'd be busy with the people who needed you. I don't need anything. I only wanted to say it, and I'm afraid it's not much.")
                .at("grave", "Go to the place at {@grave} where {=kin} is buried, to hear it said.")
                .outro(
                        "* The grave is a plain one, on a slope above a stream, with a stone that someone has kept free of moss. You sit on the grass beside it. The sun comes out, which is considerate.",
                        "@{=kinfirst}| I never asked what you liked to eat. Do you know, I couldn't tell you? All those years. I could tell you the colour of your shoes on the day you were three, and I couldn't tell you what you'd have for supper if I gave you the choice. It's a stupid thing to regret. I've regretted it for longer than most.",
                        "@You| Stew. I like stew.",
                        "@{=kinfirst}| Stew. Oh, that's good. That's very good. I'll write it down.",
                        "* There is, of course, nothing to write with, and nothing to write on. {=kinfirst} makes the gesture anyway, with a finger, in the air above the stone, slowly and with great care, and for a moment you could swear that the grass bent where it went.")
                .pay(200, 0, 1, 0, 0, 0);

        d.chapter("tally", "Forms in Triplicate").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "tally"))
                .intro(
                        "* The man from the Office of Returning arrives on the stroke of nine with a clipboard, a briefcase, an umbrella that he is still wearing as though it were a hat, and a smile that has been ironed. He is polite in the way of a person who has never had to be anything else. His name is Mister Tally.",
                        "@Tally| I'll be brief. I'm sorry to say that my department has noticed an irregularity. Over the last two years a number of persons who ought to have been returned by the usual channel have, I regret to say, been routed through these premises. They have been, if I can put it delicately, completing their affairs. This has an effect on the schedule.",
                        "@You| You're upset that people are saying goodbye.",
                        "@Tally| Upset is a strong word. We are concerned. A person who finishes their affairs does not come back on time, and a person who does not come back on time is a hole in the ledger. I am not a monster. I have a wife, and a cat called Appendix. I am only a man who files things.")
                .meet("tally", "Hear out Mister Tally of the Office of Returning.")
                .outro(
                        "* He lays three forms on the desk. They are for different things: a notice to cease, a notice of intent to cease, and a form requesting that the first two be treated as equivalent. He asks you to sign them in the places marked with a small cross. He has marked them himself, with a ruler.",
                        "@You| And if I don't?",
                        "@Tally| Then I am to inform you that under the Rules of Returning, article nine, a dispute of this kind may be resolved by formal contest. It is a very old rule. I have never seen it used. I did not think anyone would be so unreasonable.",
                        "* He says it with great regret. He picks up the umbrella. He seems, for a moment, rather like a man who is hoping against hope that you will sign.",
                        "@Mrs. Pang| He's very sweet, really. He sent flowers when I died.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("appendix", "Appendix").gap(2)
                .intro(
                        "* You go to see Mister Tally. It is not a thing you planned. You have a notice on your desk with three forms attached. You also have a cat on your doorstep, a small grey cat with a white bib, who sits there every morning at nine and stares at the door with an air of waiting for something to come out. After the fourth morning you pick it up, and read the tag on its collar, and the tag says APPENDIX, and under it an address.",
                        "* It is a narrow house in a narrow street, in a quarter of the city that smells of ink. The door is opened by Tally in his shirtsleeves. He is holding a ladle. He looks at the cat, and at you, and at the cat, and the ironed smile slips off his face as if it had never been secured.")
                .wait(1)
                .outro(
                        "@Tally| She does that. She goes out in the mornings. I'd thought she went to the baker's. Please come in. I can't stand on the step with you. My wife will think I'm in trouble.",
                        "* His wife is a small round lively woman with a laugh that fills a kitchen. She puts you in the chair by the stove, and a plate of something in front of you, and asks nothing for a quarter of an hour. When she does, it is only whether you take sugar. The kitchen is warm and cluttered and smells of onions, and over the mantel is a framed form, in a beautiful hand, with a gilt border. It is his appointment to the Office. He is plainly very proud of it.",
                        "@Tally| I don't enjoy it. I'd like you to know that. It's a post. It's a very small post, in a department nobody has heard of, and I have it because I can add. Forty years of ledgers. Every night I take them home and check the sums, because if a number's wrong, then a person's wrong, and I couldn't sleep.",
                        "@You| You came to my office with three forms.",
                        "@Tally| I was told to. It is the kind of thing I am told. I'm sorry about Article Nine. I didn't know what else to do. I thought if I said it politely enough it would turn out to be a game.",
                        "* His wife puts a hand on his shoulder. He puts a hand on hers. It is the most natural movement in the world, and he does not notice he has done it. The cat jumps up on the dresser and begins, thoughtfully, to knock a spoon off.",
                        "@Tally| I will fight you if I have to. I'll lose. But I'll do it properly, with the forms. I'd rather that than be the sort of man who doesn't.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("quibble2", "Quibble's Receipt").gap(2)
                .intro(
                        "* He comes back on a Tuesday, in a tidy damp coat, with the leather satchel clasped to his chest. You had thought him gone. He stands on the step with the rain going through him, politely, as it goes through the dead, and he holds up a piece of paper between a finger and thumb.",
                        "@Quibble| I have been remiss. I beg your pardon. I was a tax collector. In forty years I never failed to give a receipt, and I have lately realised that I did not give one for the coppers. It would have been a breach of form.")
                .wait(1)
                .outro(
                        "* The receipt is written on the back of a hymn sheet in a small cramped copperplate. It reads: RECEIVED OF {rival.name}, THREE COPPERS, IN SETTLEMENT OF A DEBT OF ELEVEN YEARS AND ONE PIE. SIGNED, A. QUIBBLE, COLLECTOR, DECEASED. At the bottom there is a space for a witness. He holds out a pencil.",
                        "@Quibble| I should like you to sign. It is the principle.",
                        "@You| I'll sign it.",
                        "@Quibble| And I should like it delivered. Would you? He will not have kept the letters. I asked, in the spring, in my fashion, and he said that he had kept every one. He had them in a shoebox. He read them out to a cat. I cannot say how that made me feel.",
                        "* You deliver it. {rival.name} reads the receipt at the door with his reading glasses on his forehead, and then on his nose, and then in his hand. {rival.He} folds it small. {rival.He} puts it in the pocket of {rival.his} waistcoat, over the heart, in the place where a man keeps things. {rival.He} says: 'Tell him I never wanted to be paid back.'",
                        "* You tell him. Quibble nods, with the grave small dignity of a creature to whom a great thing has been said. 'No,' he says. 'But it was never the debt, you see. It was the receipt.' He puts his hat on his head. It stays there. And he goes out into the rain, a little straighter than he came, and shuts the door behind him very gently, like a man who has finished the books.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("duel", "Article Nine").gap(2)
                .intro(
                        "* The contest is at midnight, on the Mill Bridge, which Tally says is customary and you suspect is merely convenient. He has brought a second, a pale clerk with an ink-stained thumb, and a book in which the proceedings are to be recorded. The goose is there. Nobody invited the goose.",
                        "@Tally| The contest is to be fought to the satisfaction of both parties, under the supervision of the Rules. Neither party may call on the dead. Neither may bring a weapon not described in Schedule C. I must tell you that I am not a fighter. I am, however, extremely well prepared.",
                        "* He takes off the umbrella-hat, and folds it, and puts it to one side. He rolls up his sleeves. He has the forearms of a man who has lifted a great many ledgers, which is to say that they are rather alarming.",
                        "@Mrs. Pang| I put five coppers on you, dear.")
                .defeat("tally", "Fight Mister Tally on the Mill Bridge, under Article Nine.")
                .outro(
                        "?dead.tally|* It is over quickly, and it is not satisfying. He goes down on the bridge with the clipboard under his arm, looking mildly surprised, and the pale clerk closes the book, and writes a line in it, and goes. The goose watches him go. Mrs. Pang is silent for the first time since she arrived.",
                        "?!dead.tally|* He yields at the third round, sitting down on the bridge with his glasses crooked and the clipboard across his knees. 'Satisfactory,' he says, with a dignity that surprises you both. 'Entirely in order. I shall note that I was bested.' He takes out a pen, and writes it down, and looks very happy.",
                        "@Tally| I'll be honest, I would have felt worse to be right. There's a sort of relief in losing to a rule.",
                        "* He leaves a card. It has the name of a department on it, and an address that appears to be under the Palace of the Oldest Order. In a hand like a row of fence posts, he has written: 'You are invited. The Cistern asks for you by name.'")
                .pay(2000, 0, 0, 1, 0, 1);

        d.chapter("muse", "The Poet's Muse").gap(3)
                .intro(
                        "* Lin Zhaoyu has come to the office. He lowers himself into the client's chair as if it were a divan and lays one hand on his brow. He has never in his life, he says, asked a favour of anyone, and he would be grateful if that were noted.",
                        "@Lin| I have been dictated to. For three weeks. In the small hours, in my own handwriting, by a person I cannot see. It is the most appalling presumption. The thing is writing my Ode.",
                        "@You| Your Ode to a Plum.",
                        "@Lin| In Seventeen Cantos. I have written nine. The remaining eight have been composed overnight, in my hand, in a better metre than mine. I do not say they are bad. I say it is a question of authorship.")
                .wait(1)
                .outro(
                        "* The muse is in the study when you arrive, at the desk, with a brush behind her ear. She is a lean, stooped, ink-stained woman in a robe of a fashion four centuries old, and she is not at all pleased to be seen. She has the harassed face of someone who has been left with the washing-up of a very long party.",
                        "@Muse| I want it understood that I did not ask for this. I was a perfectly respectable poet in the reign of the Third Lantern. I wrote twelve poems and a reasonably good elegy. I died. And I was sent, without any notice, to be the muse of a man who writes odes to fruit.",
                        "@Lin| It is an extremely good plum.",
                        "@Muse| It is a plum. He has spent nine cantos on its skin. He has gone on for eleven pages about its blush. There are people who are dead and are not allowed to rest because they have been given a muse's work, and I am one of them, and I should like to know when it stops.",
                        "* You put the matter, as gently as you can, to the poet. He is silent for a moment. He rises, and turns his back, and looks out of the window at the roofs until the tea goes cold. His shoulders, in the fine silk, are very straight.",
                        "@Lin| I thought the verses were mine. I thought I had improved. I was so pleased. It was the first time in years that I had been truly proud. I do not wish to be told that I was merely being helped.",
                        "@Muse| You were helped. You were also there. Every word I wrote was a word you had half thought. I merely sharpened it. It is, I will say, a rather good plum.",
                        "* He turns round. His face is very red. He sits down at the desk, and takes the brush from behind her ear, and, to the amazement of the room, hands her the other end of it. They write the last eight cantos together, in two hands, on the same page, over three nights. They are the best he has done. The plum, in the last of them, is eaten.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("missing", "The Case of the Missing Ghost").gap(4)
                .intro(
                        "* It rained the day she came in. It was the kind of rain that makes a person look at their shoes. She was small and neat and wore a hat with a bird on it. She sat down in the chair in front of the desk with her handbag on her knees, as if it might make a run for the door.",
                        "@Client| Mrs. Aldous Tench. I've lost my husband. Not the usual way. He's gone, you understand, but he didn't go. I've heard him about the house. The kettle moves. The post is rearranged. On Tuesday the pie was gone from the windowsill, and I'd made it for the vicar. I know it's him. He was always taking the pie. But he won't speak to me. He won't be found.")
                .wait(1)
                .outro(
                        "* The house had the quiet that houses have when there is something in them that wants not to be noticed. I walked through it with a lamp. In the kitchen the kettle was warm. On the table was a plate with a single crust of pie, set down neatly, with the fork laid across it. It was the way a man does it who has been told for forty years to put his fork across his plate.",
                        "* He was in the cupboard under the stairs. He had gone in there on the day of the funeral, and he had been in there since. He was a tall man, with a good moustache, and he was sitting on an upturned bucket among the brooms, with his arms round his knees, trying to look like a coat.",
                        "@Ghost| Don't tell her.",
                        "@You| She knows you're here.",
                        "@Ghost| She knows there's something. She doesn't know I'm ashamed. I owe her a pie. I owe her about three thousand pies. She used to make one every Thursday and I used to say it was nice, and I never once said it was the best thing in my life. Forty years. I thought she knew. And then I was dead, and she was crying in the kitchen, and I stood there with my hat in my hand, and it was too late to start.",
                        "* I sat down on the stairs. It was cold and the rain was loud. I told him that there was a thing a person could say, even late. That it might not be enough. That it would be better than the cupboard.",
                        "* It took two hours. He came out at last. He stood at the kitchen door with his hat in his hand, looking at the back of his wife's head as she did the washing-up. I held the door of the world open behind him. He said, in a low, stiff, unpractised voice: 'Maud. It was the best pie.'",
                        "* She did not turn round. Her hands went on in the water. After a moment she said, to the window, in a perfectly level voice: 'It was a bit dry.' And then she put her wet hands over her face. I let myself out.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("pangs", "The Last Word").gap(8)
                .intro(
                        "* The spring that Mister Pang dies is a warm one, and he goes in his sleep on a porch in the sun, with a hat over his face. You hear it from a neighbour who sends a note. You close the shop that day, and put a lantern in the window, and sit up.",
                        "* He comes in just after midnight, small and neat and cheerful, with his hands behind his back, looking at the walls with great interest. He is ninety-six years old, and he looks about fifty, and he is very polite.",
                        "@Mister Pang| Excuse me. I'm looking for my wife. She said there was a place she'd been working.",
                        "* Mrs. Pang is in the corner, with a piece of paper folded to the size of a coin. She doesn't turn round. She has gone, you notice, entirely still, the way a person goes still who has rehearsed a speech for fifty years and has just been handed the stage.")
                .wait(1)
                .outChoice(new Chapters.Choice("pang", "Mister and Mrs. Pang are in the same room for the first time in half a century.",
                        "Leave them alone, and shut the office door.", "Offer them tea.", "Open the door to the other side, and see them off.")
                        .after(
                                new String[]{"* You close the door and sit on the step outside. You cannot hear what they say. There is an hour of it, rising and falling, and at one point a laugh so loud the lantern shakes. When you go in again the paper in the corner has been unfolded, and it is a very long letter, in a hand you recognise."},
                                new String[]{"* You put the kettle on. It is the only thing you can think of. The Pangs sit at the table and drink their tea, which has no taste, and say nothing, and look at each other over the rims of their cups with the pleased astonishment of two people at their own wedding.", "@Mrs. Pang| Oh, you did always make a dreadful cup."},
                                new String[]{"* You open the door to the other side. You have never been so sure of anything. They go together, arm in arm, a little unsteady, into a light the colour of the end of a long day, and at the last moment Mrs. Pang turns round and gives you a look of such complete and unembarrassed happiness that you have to sit down."}))
                .outro(
                        "?pang=1|@Mrs. Pang| He says the chicken was always too dry. Fifty years, and that's what he leads with. We shall be a while.",
                        "?pang=2|@Mister Pang| I shall come back to see the shop, dear. I shouldn't like to miss the goose.",
                        "?pang=3|@Mister Pang| Well, dear. You did always have the last word.",
                        "* The goose, which has been sitting on the doorstep all night, honks once, softly. It is not clear whether it is a farewell. It is clear that it has been listening.")
                .pay(0, 0, 0, 0, 0, 0);

        d.interlude(2, "The Queue",
                "* The office gets busier. You hire a clerk, who is dead. You hire a second, who is not, and who leaves after a week. The shop is mentioned in a guidebook, under 'odd places to have lunch'.",
                "?cheese=1|* The Dorrit Hale Cup is awarded at the Great Gorge Cheese Roll every spring. Last year a cheese was killed. The village was very quiet about it.",
                "?cheese=2|* The Dorrit Hale Cup was won, this year, by a girl of eleven who is said to have the same line as her.",
                "?cheese=3|* You try not to think about the cheese. It does not help.",
                "?dead.tally|* Mister Tally's cat, Appendix, turned up on the doorstep a month after the contest. It has no note.",
                "* Somewhere beneath a Palace there is a Cistern with your name on a card. It is a patient place. It has all the time in the world, and it is beginning to ask.");

        // ================================================================== SEASON 3
        d.season(3, "The Door");
        d.place("palace3", "start");

        d.chapter("invite", "A Cordial Invitation").years(3)
                .intro(
                        "* The card has been on the mantel for three years. You have not thrown it away, and you have not answered it. It has a way of catching the light that you do not like. This spring, in the third week, you wake and find that the ink has changed: 'You are invited' has become 'You are expected'.",
                        "@Mrs. Pang| I'd go. I've heard the soup is very good.",
                        "@You| Mrs. Pang, you left.",
                        "@Mrs. Pang| I came back to see the shop, dear. I told him I would. The soup is very good, though.")
                .wait(2)
                .outro(
                        "?!dead.tally|* Nothing else happens, and then the door opens by itself and Mister Tally stands on the step with his clipboard and a cat in a basket. He has a tired, clean face. He has been, you realise, a good deal worried.",
                        "?!dead.tally|@Tally| I'm here to apologise for the note. It was my superior's doing. She has a way with notes. If you'd be so good as to come, I'll walk you down myself.",
                        "?dead.tally|* Nothing else happens, and then the door opens by itself and a woman in a grey coat stands on the step with a clipboard. She has a tired, clean face, and she has his hands.",
                        "?dead.tally|@Tally| Miss Tally. His sister. I'm the replacement, which I'm told is a terrible thing to be called. I'm to take you down.",
                        "* He says it as if you had already agreed. You find, to your surprise, that you have.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("cistern", "The Cistern of Returning").gap(4)
                .intro(
                        "* Past the Under-Kitchen there is a second stair, and it is very clean. It goes down for a long way, and it is not a cold place, as you had somehow imagined. It is a little damp. There is a smell of old rain and wet stone, and from the dark ahead a sound like a very large kettle just before it boils.",
                        "* At the bottom is a hall with a ceiling you cannot see. It is full of pipes. They run in rows, floor to ceiling, in a great grid of dull iron, and each pipe has a small brass tag on it, with a number. Tally walks along them with his clipboard and his cat, ticking things off, talking in a low voice, as though he were showing you round a library.")
                .at("palace3", "Go to the Palace of the Oldest Order, at Originis, and down to the Cistern.")
                .outro(
                        "@Tally| This is the Returning. The dead come down the pipes, and the water carries them. It is not an unkind arrangement. Nobody is lost, nobody is left behind, and everything is used. There are, after all, only so many people to go round.",
                        "@You| Used for what?",
                        "@Tally| For pills, mostly. For the climbing. A pill of blood is a very small thing, taken by someone who is going up. It goes up with them. It has the memory of what it was made of. Some people find that a comfort.",
                        "* You look at the pipes. You look at the tags. Along one of them, very faintly, you read a name that you know. It is Quibble's. It has a line through it, in pencil, and the pencil has been rubbed out, and drawn in again.",
                        "@Tally| Yes. Well. That is the irregularity. It's what I wanted to show you. Some of them don't come down. Some of them go through you, and out the other side, and they keep their names. I've never been able to explain it. I find I don't wish to.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("kitchen3", "The Soup").gap(2)
                .intro(
                        "* You are shown to the kitchen, where there is soup. The Palace kitchen is bright and warm and smells of onions. A man sits at the long table with a bowl, in plain clothes, with the air of someone who has been told that this is how people eat and is giving it his best.",
                        "@???| Hello. You are the door. I'm not allowed to say who I am. That one I can keep. I do like the soup. It is very good, isn't it? I've had it every day for longer than I can say.")
                .wait(1)
                .outro(
                        "@???| I did not make the Cistern. I want you to know that. I found it when I came. It is not cruel, but I cannot say it is kind. There is a difference, and I have spent a long time standing on the line between the two.",
                        "@???| Doors are rare. Most of the dead go down the pipes, and are used, and do not remember. A few go through someone like you, and keep their names. It costs the door something. Mrs. Pang's husband knew. He said the whole of the past year felt like standing in a draught.",
                        "@You| Meng said I could keep it open or shut.",
                        "@???| Yes. She did. She was a door, too, I think. She asked me once whether it hurt less if you closed it. I did not know what to say. I have had a lot of practice at not knowing what to say.",
                        "* He puts down the spoon. The room shifts, a little, like a sleeper turning over.",
                        "@???| I'm very glad you came, though. I've found I don't have many people to eat with.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("article", "Article One").gap(2)
                .intro(
                        "* The Rules of Returning are kept in a book the size of a door. It has a leather cover the colour of a bad tooth, and a brass clasp, and a ribbon, and Tally has had it brought down from the shelf by two clerks and a trolley. It is to be consulted. He says so with his face turned away, as one announces an operation.",
                        "@Tally| I have not looked at Article One in thirty years. It is one of those articles. It says what the Office is for, and one gets out of the habit of asking.")
                .wait(1)
                .outro(
                        "* He reads it aloud in a thin dry voice, with his finger under the line. 'The Office of Returning exists to ensure that no one who is dead shall be lost, and no one who is lost shall be forgotten. The passage of the one into the other shall be made without waste.' He stops. 'That is the whole of it. Without waste. I had always thought it meant the pills.'",
                        "@You| What do you think it means?",
                        "@Tally| I think it means that the Founder, whoever she was, did not want a single person to be thrown away. I think it is a rule against loss. I think we have been applying it to the wrong end.",
                        "* He turns the page. There is a note in the margin, in a different ink, in a hand that has been put there much later by someone in a hurry. It reads: 'Where a person may be returned to the world by their own door, that is the better passage. See Door.'",
                        "@Tally| See Door. I have never seen that. In thirty years. It has been there all the time. It was only that nobody ever looked up the cross-reference.",
                        "* He is sitting very still. A drop of ink falls from his pen on to the page and runs down the word DOOR in a long black tear. 'Oh dear,' he says. 'Oh, dear. I am afraid, my friend, that you are lawful.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("pipes", "The Pipes Have Names").gap(2)
                .intro(
                        "* Tally takes you along the pipes on a slow afternoon, reading them aloud. It is, he says, a hobby. He likes the sound. There are eleven thousand and forty of them in the hall, and each has a tag, and each tag a name, in a hand that changes along the row as the clerks do. Along the early pipes the writing is a florid copperplate. Along the later ones it is a nervous slant. In the middle there is a run of tags written, by the look, by a child.",
                        "@Tally| Aldous Pym. Ferris. Hesper Wane. Dorrit Hale, only just. She hasn't gone down yet. I thought I'd leave her a pipe. Here is Quibble. You will recall that he was put in pencil and rubbed out. I've drawn him in again. Forgive me.")
                .wait(1)
                .outro(
                        "* The tags along the last wall are blank. They are new, he says, a recent order, ready for whoever is next. Row upon row, bright brass, unmarked. And at the very end of the hall, at the corner where the pipes turn, there is a tag that is not blank, though it has not been used.",
                        "* It is your name. It is written in pencil, very neatly, in the hand of a clerk who has the habit of making each letter twice: once to see how it looks, once to be sure. It has been there, to judge by the dust, a long time. It is the only tag in the hall written in advance.",
                        "@Tally| I'm so sorry. I wanted you to see it from me. It's standard. We reserve a pipe for everyone who has been, ah, through the Office. In your case, as you will observe, it is a good one. It is on the end. It has the best drainage.",
                        "* You stand and look at your own name until your feet go cold. It is not a threatening thing. It is polite. It is, you realise, simply there, like a seat kept at a table.",
                        "@You| Take it down.",
                        "@Tally| I couldn't. I shouldn't. Well. I'll put a line through. It's irregular. It's very irregular. I shall enjoy it.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("manners", "Etiquette for the Dead").gap(2)
                .intro(
                        "* Mrs. Pang takes it upon herself to prepare you. She has been waiting, she says, fifty years for something to organise. She appears in the office at dawn with a roll of paper, which turns out to be a diagram, which turns out to be a floor plan of the street, marked in several colours, with arrows.",
                        "@Mrs. Pang| You will want to know the order. The old go first, because they are used to waiting. The very young go second, because they have no idea how long it takes. The soldiers go after, in rows. The suicides go last, with a tea, and nobody is to say a word about it. If you see a man with a dog, say nothing about the dog.")
                .wait(1)
                .outro(
                        "* She drills you for a week. You learn how to hold a door so that it does not feel like being held. You learn what to say to a person who is about to cross over and wants to say something important and has forgotten what it is, which is 'It will come to you'. You learn what to say to someone who knows, which is nothing. You learn to tell by the shape of their hands whether they will want to hold yours.",
                        "@Mrs. Pang| Don't smile. They hate it. Don't look sad. They hate that more. Look as if you are pleased to see them and have all the time in the world. It is the only face that works.",
                        "@You| How do you know?",
                        "@Mrs. Pang| I wore it for fifty years. I wore it to wait for my husband. By the end I was very good at it. I was so good at it I forgot it was a face.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("queue3", "The Long Night").gap(6)
                .intro(
                        "* On the last night of the year, the queue comes. Not the usual queue. The whole of it. They have been waiting, it appears, for the door to be properly ready. The office is not large enough, so they line up along the bridge and down the street and out of sight, in the dark, each with a lantern that is not a lantern.",
                        "* You have never seen so many. You have never seen anything so quiet. They are not frightening. They are old, and young, and tidy, and untidy, and some of them are in wedding clothes, and some in armour, and some are children. One of them is carrying a cheese.",
                        "@Mrs. Pang| I'll do the introductions, dear. You do the doors.")
                .wait(1)
                .outro(
                        "* It takes the whole night. You open the door to each, one at a time, and each says what they wanted to say, and goes through. A butcher says sorry to a boy. A girl says she never learned to swim. A very tall man says he would like it to be on record that he was right about the weather. An old woman gives you a loaf of bread, and it is warm, and she goes, and you eat it.",
                        "* Near dawn a small girl with a tower of stones in her hands comes to the front of the line, and puts one stone on the sill of your door, and looks at you with a small, serious face.",
                        "@Girl| It's the last one. I kept it for you. I wanted you to have the top.",
                        "* The tower stands on the windowsill. It is very straight. It does not fall.")
                .pay(0, 3, 0, 0, 0, 0);

        d.chapter("mengagain", "What Meng Never Said (Again)").gap(2)
                .intro(
                        "* When the dawn comes, there is one more in the line. She is a tiny old woman with a lot of cats round her feet, in the shape of cats, which is to say no more than a sort of warmth at the ankles. She is sitting on the doorstep, with a pot of tea, as if she had been there all night. She has a thin look, and a very clear one.",
                        "@Meng| Hello, dear. Sit. Don't fuss. I'm sorry I was so long. I wanted to be the last. I thought you ought to have a rest first.")
                .wait(1)
                .outro(
                        "@Meng| I lied to you, you know. I said I couldn't decide whether to keep the door open or shut. I did decide. I shut it. For eleven years. I thought it would hurt less. It did, a bit. And then it was very quiet, and there was nobody to tell me the sort of thing that people tell you at three in the morning, and I found I would rather have the draught.",
                        "@Meng| I never said, because it's not the sort of thing a person can be told. It's the sort of thing you have to do wrong first. I wanted you to be allowed to do it wrong, and find your own way back.",
                        "@You| You could have said.",
                        "@Meng| Yes. And would you have listened?",
                        "* You think about it. You are, in your honest opinion, a person who would have listened. You have, in fact, never listened to anyone in your life, until the dead were in front of you, which is a different sort of listening.",
                        "@Meng| Pour me some of that tea, dear. It's gone cold. It was always going to.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("cats", "Meng's Cats").gap(2)
                .intro(
                        "* There were nine cats at the house of Old Meng in the Street of the Seers when she died, and they all took it differently. The grey one went into the loft and would not come out. The black one went to sit on the grave. A big ginger tom, who had never been heard to purr, purred for eleven days. They were given to the neighbours, in ones and twos, with a note, and the neighbours did the best they could.",
                        "* You learn of it by accident. A girl at the baker's has a white cat, with one blue eye, who sits on the counter and looks at the door. She says it came with the house next to hers. She says it is a bad-tempered cat and will only sit on the window ledge facing south. She says it is waiting for someone.")
                .wait(1)
                .outro(
                        "* You find them over the course of a month, one at a time, in the houses where they were left. They are fat, and sleek, and stiff-necked, and every one of them is sitting somewhere that faces the same direction. They watch you come. They do not move. They have the look of animals who have been waiting a long time for an appointment and are prepared to be gracious.",
                        "* You go through the lot and write it up in the book. Eight, with the grey one in the loft. The ninth is the ginger, at the harbour, who has gone to sea on a fishing boat and is, they say, the best mouser in the fleet. You pay your respects to each. You do not ask them anything. It would not be right.",
                        "* On the last evening you go to the empty house in the Street of the Seers. The door is on the latch. The kitchen is exactly as it was, with the pot on the hob and the cups on the dresser and a cold grey ash in the grate. And on the table, in the dusk, in a ring, as if they had been sitting there for years, are eight cats. There is a ninth, a ghost of a ginger one, translucent as smoke, with its tail round its paws.",
                        "@Meng| (from somewhere, mildly) You found them. I did wonder. I did say they would sort themselves out.",
                        "* The cats look at you. Eight pairs of eyes, one pair of ghost. They blink, all together, slowly, with an extremely deliberate courtesy. Then they get down off the table, one after another, and go out into the evening by different doors, to the houses where they live.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("goose3", "The Goose, Again").gap(2)
                .intro(
                        "* The goose has been on the doorstep since the Long Night. It has not moved. It sits with its feet tucked under and its neck on its back, and watches the street with half-closed eyes, and at night it makes a faint sound in its sleep like someone counting.",
                        "* You have given it bread. It has declined. You have given it a peach, once, in a spirit of experiment, and it has eaten the peach, with great ceremony, in three bites, and gone back to its post.")
                .wait(1)
                .outro(
                        "@Mrs. Pang| It's the oldest one here. Did you not know? It was here before the bridge. It was here before the town. There was a pond on that spot, in the beginning, and a goose on the pond, and it has been seeing people over the water ever since.",
                        "@You| It's a ghost?",
                        "@Mrs. Pang| It's a door. A small one. For things that don't have a Cistern to go to. The old dogs, the sparrows, the fish. It sits on the bridge and watches them cross. It has never once said a word about it. It only bites people who try to take the credit.",
                        "* The goose opens one eye. It is round and dark and ancient. It looks at you, and you have the sudden, giddy conviction that you are being weighed, and found a reasonable size.",
                        "* It lowers its head. It says something, in goose, short and low, and does not look at you again. Mrs. Pang translates, with a straight face, as: 'You will do.'")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("dorrit", "Dorrit's Cup").gap(2)
                .intro(
                        "* Dorrit Hale comes to the front of the line on the Long Night with a cheese under her arm. It is a small cheese. It has been polished. She has carried it, she says, through six weeks of a stranger's ghost-hood and nine of a better-organised one, and she is damned if she'll leave it behind.",
                        "@Dorrit| I won't go down a pipe. I don't mind saying. I have heard what's at the bottom. I'd like to go the other way. I'd like to go out through you, if it's all the same.")
                .wait(1)
                .outro(
                        "* You open the door. She stands on the threshold with the cheese under her arm and her shoulders squared, like a woman about to take a hill. The street behind her is full of lanterns. She looks back at them, and then at the bridge, and then at the little cottage window where a goose sits on the sill.",
                        "@Dorrit| Eleven years I won the roll. Twelve, if you count the last. I want to say something and I'm not good at it, so I'll say it plain. I was not a kind woman. I was good at one thing, and I did it better than anyone. I didn't stop to ask what it was for. I think it was for this. I think it was so that someone would name a cup after me, and a stranger would come and tell me.",
                        "@You| The cup is still run for.",
                        "@Dorrit| Every spring?",
                        "@You| Every spring.",
                        "* She nods slowly. She takes the cheese out from under her arm, and holds it, and weighs it in her two hands, a good steady weight, as a woman weighs a child. She puts it on the sill. The goose regards it with a grave eye. 'For the roll,' she says. 'Somebody ought to have a go.'",
                        "* Then she walks out through the door, into the light, with her hands empty, and her head up, and the walk of a woman crossing the finish line ten yards ahead. She does not turn round to wave. She doesn't need to. The cheese on the windowsill is, you notice, rolling very slightly, and then a little more.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("choice3", "A Door's Choice").gap(4)
                .intro(
                        "* There is a small silence, in the morning after the Long Night. Tally has sent word. ??? has sent soup. The office is quiet. You sit by the lantern, which has stopped burning and does not need to, and you consider what a door is for.",
                        "@Mrs. Pang| I'd not tell you what to do, dear. But there are three things a door can do, and you've done two of them. It's only fair that you get to pick the third.")
                .wait(1)
                .outChoice(new Chapters.Choice("lastdoor", "What does a door do, when it has opened enough?",
                        "Stay open, for as long as you live and after: the dead can always find you.", "Take a post with the Office of Returning, and make the Cistern kinder from the inside.", "Walk through yourself, and shut it behind you, and let the next door open when it must.")
                        .after(
                                new String[]{"@Tally| It will be loud.", "@You| It always was.", "@Tally| It will cost you the quiet. You do understand.", "@You| I never had any. I'm told it's overrated."},
                                new String[]{"@Tally| I can offer you a desk. It is not a grand desk. It has a drawer. The drawer sticks, but it can be mended. You would be the first of us who had ever been on the other side of the counter.", "@You| Does it come with a cat?", "@Tally| It comes with a great many forms."},
                                new String[]{"* You take the letter you wrote in your head the night before, and you tear it, though it is only in your head, and you walk to the door, and put your hand on it. It is warm. It is a little like touching a sleeping animal. Behind you, on the sill, the tower of stones is standing in the light."}))
                .outro(
                        "?lastdoor=1|* The door stays open. It is a warm door, and a heavy one, and sometimes at three in the morning you can hear the whole of the street breathing on the other side of it. You find that you are not afraid of anything any more, except of becoming used to it.",
                        "?lastdoor=2|* You take the desk. The drawer sticks. You mend it, and the Office of Returning, which has not been mended in some time, notices. Within a year the pipes are labelled in a clear hand, and there is a lamp at the end of each, and a card that says, in small letters, 'we are sorry for the delay'.",
                        "?lastdoor=3|* It opens inward. The light on the other side is the colour of the end of a good day. Meng takes your arm. Mrs. Pang takes the other. Behind you, the office is quiet, and the goose is on the step, and as you pass it lowers its neck, very gravely, and gives you the only bow you have ever seen it make.")
                .pay(20000, 2, 0, 5, 2, 2);

        d.epilogue(
                "* You died once and did not stay dead. It was, you decided long ago, the best thing that ever happened to you. Just don't tell the goose.",
                "?lastdoor=1|* There is a little shop now at {@bridge}, with a lantern in the window and a queue that has never, in all the years, grown shorter. The sign says Lost Property (Spiritual). Underneath, in a smaller hand, someone has added: Open.",
                "?lastdoor=2|* The Office of Returning has a new head of department. It is not you, but it takes your advice. The pipes are warm now, in winter, and nobody has been quite sure why.",
                "?lastdoor=3|* The shop at {@bridge} has a sign that says CLOSED FOR THE SEASON. It has said so for years. A tower of stones stands on the windowsill, perfectly straight. Nobody knows how it stays up.",
                "?goose=1|* The goose has retired to a pond. It is believed to be a hundred and four years old. It has never apologised.",
                "?goose=2|* The goose bites you every time you cross the bridge. It is the only constant in an uncertain world.",
                "?goose=3|* A peach appears on the railing each summer. Nobody knows who leaves it. The goose is not saying.",
                "?dead.tally|* A small grey cat sits on the step of the Office of Returning each evening, facing the pipes. It does not appear to be waiting for anything. It has simply decided to be there.",
                "* On the night of the Festival of Hungry Ghosts, the whole city lights lanterns on the river. You stand on the Mill Bridge, in your scarf, with the whole long line of them going past. You are not cold at all.");
        TaleGhostMore.add(d);
    }
}
