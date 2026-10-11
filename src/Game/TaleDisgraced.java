package Game;

/** The Disgraced Disciple (see "Story Plan.md"). You were expelled from the Stillwater Pavilion for owning half of a forbidden scripture, the Scroll of Unbroken Breath. The other half was
 *  cut into four seals by Elder Zhao and hidden. How the scroll came to be in your desk is rolled at the start (flag how: 1 Zhao put it there to silence a question about the Pavilion's
 *  fund, 2 a dying friend, Lan, hid it there for safety, 3 the scroll found you), and is learned at the end of the seals. The technique grows with each seal (the name of your martial
 *  technique changes). Choices: the vault (steal, buy or ask), Mei (tell her or spare her), and what to do with the Pavilion. */
final class TaleDisgraced {
    private TaleDisgraced() { }

    private static void technique(Character c, String name) { if (c.martialTechnique != null) c.martialTechnique.name = name; }

    static void register() {
        Chapters.Def d = Chapters.def("disgraced");
        d.place("mei", "near").place("library", "wild").place("pavilion", "far").place("vault", "far").place("grave", "wild").flags("how");
        d.cast("mei", "Mei", "Lingfeng").she().trait(Character.Traits.Friendly).side("Evil").lvl(1).age(19).at("place:mei").rel(2);
        d.cast("tse", "Brother", "Tse").he().trait(Character.Traits.Blacksmith).side("Neutral").lvl(6).age(44).at("wild").rel(0);
        d.cast("zhao", "Elder", "Zhao").he().trait(Character.Traits.Leader).side("Good").lvl(8).pot(6).age(63).at("place:pavilion").rel(-3);
        d.begin((c, st) -> Chapters.set(st, "how", 1 + GameDriver.rand.nextInt(3)));

        d.chapter("gate", "The Gate of Stillwater")
                .intro(
                        "* The reading hall of the Stillwater Pavilion has a rule that is older than the building. Nobody enters the Closed Stacks. Not students, not teachers, not the cat.",
                        "* You did not enter the Closed Stacks. You woke one morning, and half of a scroll was in the bottom of your desk, wrapped in black silk, and its title was written in a hand you did not know. The Scroll of Unbroken Breath.",
                        "* You read the first line. That was the mistake. You read the second. That was the crime.",
                        "* They found you at noon. The whole school stood in the courtyard in lines. Elder Zhao read the sentence in a calm voice, like a man reading a menu.",
                        "@Zhao| Expelled, for owning a forbidden text. The text is to be burned.",
                        "* You were not meant to keep the scroll. But there is something people forget about reading. Once a thing is in your head, burning the paper does not take it out.",
                        "* The guards walked you to the gate. Mei Lingfeng, who sat beside you in every lesson, was waiting there with a bundle of rice cakes and a face that did not move.",
                        "@Mei| I know you didn't take it. I don't know who put it there, but I'll find out. Stay alive.",
                        "* You are eighteen. You are hated by the only home you have. And you know, with perfect clarity, half of a technique that nobody else alive can use.",
                        "* The guards were told to make sure you left the province. They were not told what to do if you came back.")
                .task("AGE", 21, "", "", "Stay alive until your 21st birthday.")
                .outro(
                        "* On your twenty-first birthday you eat a bowl of noodles in a roadside inn with no name. You pay with the last of your coins.",
                        "* It is not much of a celebration. But you are alive, and nobody has found you, and a rice cake is still wrapped in a cloth at the bottom of your bag.",
                        "* It is stale. You eat it anyway.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("mei", "The One Who Did Not Turn").years(1)
                .setup((c, st) -> Chapters.spawn(c, st, "mei"))
                .intro(
                        "* A letter reaches you through three hands and a goat. It has no name on it, only a drawing of a small cat.",
                        "* 'The teahouse by the wall in {@mei}. Third table. Come alone. M.'",
                        "* You do not know whether to trust it. You go anyway. You are tired of being a person who doesn't.")
                .meet("mei", "Meet Mei Lingfeng at the teahouse in {@mei}.")
                .outro(
                        "* She has grown taller. Her sleeves are the grey of an outer disciple who has not been promoted, which is a sign of someone who asked too many questions.",
                        "@Mei| I looked into it. I wasn't supposed to. The Closed Stacks key is kept in three places, and the lock log says nobody opened it that night.",
                        "?how=1|@Mei| But there is one more entry. Elder Zhao's hand, under the date. Just a number. Eleven. It is the number of his private study. I think he took it out himself.",
                        "?how=2|@Mei| But there is one more name in the infirmary book, the same night. Lan. You remember Lan, who coughed so much. He died four days later. I don't know what he did. I know he asked for your desk to be moved to the warm end of the room.",
                        "?how=3|@Mei| But nothing else. Not a key, not a candle, not a footstep. The scroll was not taken out. It simply was in your desk. I've looked at that book a hundred times. I don't understand it.",
                        "@Mei| There is something else. Elder Zhao cut the other half of the scroll into four pieces. He has hidden them. I copied the list from the study when he was sleeping. I wrote it in lemon juice on a bread wrapper. It's the only thing I've ever stolen.",
                        "* She slides the wrapper across the table. It smells like a breakfast.",
                        "@Mei| Four seals, four keepers. You hold the first half of the breath. If you gather the second, the technique is whole. And if you're whole, he can't call you nothing again.",
                        "* Her hand is shaking. You put yours over it, and neither of you says anything.")
                .pay(300, 0, 0, 0, 0, 0);

        // ------------------------------------------------------------------ the four seals
        d.chapter("seal1", "The Drowned Library").years(1).major(1)
                .intro(
                        "* The first seal is held by a place, not a person. On Mei's list it is called the Drowned Library: an old scholars' retreat that a river took long ago.",
                        "* People say the water went down one year and left the library standing, with the books inside, dry as bone. Nobody goes there. The stacks are said to move.",
                        "* You will have to go and see.")
                .at("library", "Find the Drowned Library at {@library}.")
                .outro(
                        "* It is not a ruin. It is a hall of grey stone with mud halfway up the walls and a roof of green moss. Inside, the air is cold, and every sound you make comes back a moment late.",
                        "* The stacks do not move. But the books are sorted by a system you do not understand, and each shelf has a small brass plate with a number. It takes you three days to see that the numbers spell something.",
                        "* The seal is a flat clay tablet at the end of the last aisle, set into the wall. When you lift it out, the whole room exhales, like a held breath let go.",
                        "* The first line reads: Breathing in, you hold the world. Breathing out, you set it down.",
                        "* You sit on the cold floor and read until the light changes.")
                .done((c, st) -> technique(c, "Unbroken Breath, Third Part"))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("seal2", "The Monk Who Does Not Speak").years(2)
                .setup((c, st) -> Chapters.spawn(c, st, "tse"))
                .intro(
                        "* The second seal is carried by a man. Brother Tse took a vow of silence before you were born, and has lived by it since. He has a mountain hut, an iron staff, and a very long list of people he has politely thrown off his path.",
                        "* Mei's note says: He will not speak to you. He will fight you, and when it is over he will either let you pass or he won't.",
                        "* You are not looking forward to this.")
                .defeat("tse", "Face Brother Tse, the silent keeper of the second seal.")
                .outro(
                        "?dead.tse|* It is quick, in the end. He does not cry out. He puts his hand on his chest, where the seal hangs, and gives it to you with a nod, as if to say: I knew somebody would come.",
                        "?dead.tse|* You bury him with his staff. You say a few words over the grave, though you do not know what he believed.",
                        "?!dead.tse|* He is on one knee, and you are standing over him with your blade, and you do not use it. He looks at you for a long moment.",
                        "?!dead.tse|@Tse| Elder Zhao told me it was an evil text. That it would hollow out anyone who read it. That is why I took the vow. To be quiet when I wanted to ask him what he was afraid of.",
                        "?!dead.tse|* He breaks his silence for the first time in thirty years. His voice is rusty, as if from disuse. He sounds, you think, relieved.",
                        "?!dead.tse|@Tse| Take it. It was never mine to keep.",
                        "* The second seal is a wooden bead on a cord. Inside it, rolled very small, is a scrap of paper in tiny writing. You need a glass to read it. You read it three times.",
                        "* The second line reads: Breathing in, you are small. Breathing out, you are the world.")
                .done((c, st) -> technique(c, "Unbroken Breath, Half"))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("seal3", "The Rival's Vault").years(2).major(2)
                .intro(
                        "* The third seal sits in the vault of the Ironleaf Hall, the Pavilion's oldest rival, in {@vault}. How Elder Zhao got it in there is a question you would like to ask him.",
                        "* The Ironleaf Hall is famous for two things: its iron gate and its master, who is said to be a reasonable man if you catch him before lunch.")
                .at("vault", "Reach the Ironleaf Hall in {@vault}.")
                .outro(
                        "* The vault is a plain steel room at the end of a plain corridor. The master of the hall, a stout man with ink on his cuffs, meets you at the door with a cup of tea.",
                        "@Master Hu| I know why you're here. Zhao asked me to keep it safe, ten years ago. He said it was a family heirloom. I never believed him. I read the label.",
                        "@Master Hu| I'm not a thief and I'm not a saint. What I am is a man who dislikes being used. Take it, buy it, ask me nicely, or sneak in. I'll make it easy either way. Zhao has hurt my hall more than once.")
                .outChoice(new Chapters.Choice("vault", "Master Hu will let you have the third seal, one way or another.",
                        "Slip in at night and take it quietly.", "Pay him a fair price, in coin.", "Ask him, honestly, for it.")
                        .after(
                                new String[]{"* You go at night, and it is easy, and that is the worst part. You take the seal. The master is awake in the corridor when you leave, and he does not say a word.", "* On the road, you feel faintly sick. It was an easy theft. You did not need to do it."},
                                new String[]{"@Master Hu| Three thousand.", "* You pay. He weighs the coins with great care and slides them into a drawer. 'Pleasure,' he says. It may even have been."},
                                new String[]{"@Master Hu| Well, now. That's rare. Most people who come here are either rich or sneaky.", "* He gives you the seal and a second cup of tea. You talk for an hour about scrolls, and he tells you three things about Zhao you will use later."}))
                .done((c, st) -> {
                    technique(c, "Unbroken Breath, Three Parts of Four");
                    if (Chapters.flag(st, "vault") == 2) { c.ss = Math.max(0, c.ss - 3000); c.convertCurrency(); }
                })
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("seal4", "The Last Seal").years(1)
                .intro(
                        "* The last seal is not on Mei's list. She knew it, though. She wrote it on the back of the wrapper in smaller letters, where you did not look until now.",
                        "* 'The fourth keeper is me. I am sorry. He gave it to me when I was thirteen and told me it was a prize. I only worked out what it was last year. I did not want to give it up until I knew you were ready. Come and ask me. M.'")
                .meet("mei", "Go to Mei Lingfeng, who has kept the last seal.")
                .outro(
                        "* She is waiting at the same teahouse, third table. She looks like she hasn't slept.",
                        "@Mei| I'm sorry. I should have said. I thought if I did, you would stop trusting me, and I couldn't stand it. I wanted you to have the whole thing, but I wanted you to need me a little first. That's an awful thing to say.",
                        "* She pulls a small iron key from her sleeve. The seal is a lock-box about the size of a snuffbox.")
                .outChoice(new Chapters.Choice("meiend", "Mei has kept the last seal for years, and not told you.",
                        "Tell her you understand, and you are still her friend.", "Take the seal in silence, and leave it unsaid.")
                        .after(
                                new String[]{"@Mei| You're a better person than I am.", "@You| Everybody is. It's how we know we're in the same school.", "* She laughs, and then she is crying a little, and then you are both laughing again."},
                                new String[]{"* She does not argue. She watches you go. You do not look back, because you know you would stay, and you cannot stay. Not yet."}))
                .done((c, st) -> technique(c, "Unbroken Breath"))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("breath", "Unbroken Breath").years(1).major(2)
                .intro(
                        "* You lay the four seals side by side on a table. Together with the half that has lived in your head for years, they make a whole.",
                        "* It takes you eleven nights to read it through. You cannot sleep, so you sit by the lamp, and you read.",
                        "* At the end there is a line you have been waiting for. It explains why Elder Zhao hid it.")
                .realm(2, "Become strong enough to hold the whole technique: reach Major Lvl 2.")
                .outro(
                        "* The Scroll of Unbroken Breath is not evil. That is what is so unbearable. It is a plain, good, patient technique, which lets a cultivator mend themselves from nearly any wound, and teaches them to stand alone.",
                        "* 'A person who can mend themselves,' says the last line, 'has no need of a healer. A person who can stand alone has no need of a master. Teach it widely.'",
                        "* So that is what he was afraid of. Not that it would hurt you, but that it would free you. Elder Zhao's power over his students was the fact that they needed him.",
                        "* You close the scroll. You are still in the room, but it feels different, as if the walls have moved a little further back.")
                .pay(0, 0, 0, 1, 0, 2);

        d.chapter("how", "Why Your Desk").years(1)
                .intro(
                        "* There is one question left. Why your desk? Why you?",
                        "* The answer, you find, was in the scroll all along, if you knew where to look.")
                .at("grave", "Follow the last clue to {@grave}.")
                .outro(
                        "?how=1|* It is a stone hut outside a village the Pavilion once owned. Inside is a clerk who used to work for Zhao, now old and drunk and glad to talk. He sets a ledger in front of you.",
                        "?how=1|@Old clerk| The Pillar Fund. Ten thousand spirit stones a year, for the Pavilion's poor students. He took four of every ten. You were the only student who asked where it went. In the third year, you asked it out loud, in class.",
                        "?how=1|* So that was all it was. A question in a lesson. He needed a thing to ruin you with, and he had the thing in his private study. The Closed Stacks key was just a prop.",
                        "?how=2|* It is a little grave under a pear tree behind the infirmary, with a plain wooden marker. Lan. He coughed so much in class, and he was always a bit pale, and you never talked to him as much as you should have.",
                        "?how=2|* Under the marker is a note, folded small, in a jar. It reads: 'I took this from the Closed Stacks because I was dying and wanted to read one forbidden thing. I got scared and put it in the warmest desk. I am sorry. I think you will do something good with it.'",
                        "?how=2|* You sit under the pear tree for a very long time. You wish you had asked how he was.",
                        "?how=3|* It is a hill with one dead tree and a view of nothing. You have followed the scroll's own margin note here, in handwriting that is not Zhao's, not Lan's, and not any hand you know.",
                        "?how=3|* On the last page, in pale ink, the note reads: 'The reader found this because the reader needed it. Do not ask who left it. Ask what you will do with it. - a guest.'",
                        "?how=3|* You read it five times. You find you are not frightened at all. Someone, somewhere, wanted you to have this. You will try to deserve it.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("zhao", "Elder Zhao").years(2).major(3)
                .setup((c, st) -> Chapters.spawn(c, st, "zhao"))
                .intro(
                        "* Everything leads back to one man. He is sixty-three, strong as an oak, and he sits in the Elder's chair of the Stillwater Pavilion in {@pavilion}, where he has sat for thirty years.",
                        "* You write one letter before you go. It is short.",
                        "* 'I know about the Pillar Fund, the scroll, and the seals. I am coming. - your old student.'",
                        "* You have thought about this for years, and now you are afraid. It is a very small kind of fear, like the kind you felt in the courtyard that noon.")
                .defeat("zhao", "Face Elder Zhao, and win.")
                .outro(
                        "?dead.zhao|* He falls on the courtyard stones, and the students stand in their lines, silent, as they stood on your day.",
                        "?dead.zhao|* The silence goes on for a long time. Then someone at the back begins, very quietly, to clap. Then somebody else. It is not a happy sound. It is relief.",
                        "?!dead.zhao|* He is on his knees. The students are watching. You hold up the Pillar Fund ledger so that every one of them can see it.",
                        "?!dead.zhao|@You| I could kill you. I won't. I want them to see what you were, and I want you to live and watch them learn.",
                        "?!dead.zhao|* The courtyard is utterly silent. Zhao does not raise his head.")
                .pay(3000, 0, 0, 0, 2, 0);

        d.chapter("door", "The Pavilion's Door").years(1)
                .intro(
                        "* The Stillwater Pavilion has no master now. The Elders look at you, and the students look at you, and the great front door stands open.",
                        "* You are not the child who was walked out of it. You are a different person.",
                        "* You find yourself wondering what Lan would have wanted. What Mei would want. What Master Hu, the paper-stained man, would have said.")
                .choice(new Chapters.Choice("door", "The Pavilion is in your hands.",
                        "Teach the Scroll of Unbroken Breath to everyone, and leave.", "Take the Elder's chair, and rebuild the Pavilion as a school for those who are turned away.", "Burn the Closed Stacks, and let the Pavilion die.")
                        .after(
                                new String[]{"* You spend a year copying it, line by line, in ten thousand hands. When you leave, the scroll is on every desk."},
                                new String[]{"* The chair is hard. You hate it for a year. Then Mei comes and sits beside you, and then it is merely uncomfortable."},
                                new String[]{"* It burns all night. The students stand and watch it, in no particular order. When the sun comes up, nobody says anything. Then someone passes round a bag of rice cakes."}))
                .realm(3, "Walk back in as an equal: reach Major Lvl 3.")
                .pay(0, 0, 0, 2, 0, 0);

        d.epilogue(
                "* You were expelled at eighteen for owning half a scroll. You finish the story as the only person alive who knows the whole of it.",
                "?door=1|* The Scroll of Unbroken Breath is taught in a thousand villages now. People who have never seen the Pavilion can mend their own cuts. Nobody remembers who first wrote it down. You like that best.",
                "?door=2|* The Stillwater Pavilion has a new motto over its door: Nobody is turned away who asks a good question. It has a very large number of students and a very loud library.",
                "?door=3|* The Closed Stacks are ash. The students who lived through that night became the most curious people in the province. They ask questions about everything. They are tremendous nuisances. You are proud of them.",
                "?dead.zhao|* Elder Zhao has a stone in the Pavilion's yard. It says only his name and the years. You decided not to add a verdict.",
                "?!dead.zhao|* Elder Zhao lives in a small hut at the edge of the school grounds. The students bring him soup. He never says thank you. He never stops watching them learn.",
                "?meiend=1|* Mei teaches the youngest class. She still keeps a drawing of a small cat on her desk.",
                "?meiend=2|* Mei sends a short letter once a year. It says: I am well. I think of you. You never answer, and you never throw one out.",
                "?how=2|* Every spring you visit a pear tree behind the infirmary, and ask how he is.",
                "* There is a line in the scroll you did not quite understand at first. 'Breathing in, you hold the world. Breathing out, you set it down.' You understand it now.");
    }
}
