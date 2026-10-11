package Game;

/** The Last Heir of the Azure Cloud Sect (see "Story Plan.md").
 *  The truth, in order of how it is found: the hand that burned the sect was Cinder Wraith, a runner raised in Marrow Reach. The man who paid him was Orrin Vale, the kind
 *  Magistrate who offers to help. The reason: the sect's Silent Tonic was made of bitterbell root, which Orrin's agents dug out of Marrow Reach and washed in the creek until the
 *  water killed the village; Master Hui learned it, refused another batch, and wrote that he would tell the Provincial Court. Orrin burned the sect a week before. Cinder, the boy who
 *  lived, was taken in by Orrin and told the sect had done it. Both halves of that are true. Choices: how you feel about Marrow Reach (mercy), whether you take Orrin's money (patron),
 *  Cinder's fate (kill or spare), what to do with Orrin (expose, duel, or take his offer) and what the cauldron is for (cauldron). */
final class TaleHeir {
    private TaleHeir() { }

    static void register() {
        Chapters.Def d = Chapters.def("heir");
        d.place("wen", "near").place("vale", "far").place("marrow", "wild").place("cinder", "far").flags("exposed");
        d.cast("wen", "Wen", "Sorrel").she().trait(Character.Traits.Gatherer).side("Good").lvl(0).age(19).at("place:wen").rel(3);
        d.cast("orrin", "Orrin", "Vale").he().trait(Character.Traits.Leader).side("Good").lvl(5).age(52).at("place:vale").rel(1);
        d.cast("cinder", "Cinder", "Wraith").he().trait(Character.Traits.Runner).side("Evil").lvl(10).pot(8).at("place:cinder").rel(-3);
        d.cast("pell", "Pell", "Wraith").she().trait(Character.Traits.Runner).side("Neutral").lvl(3).pot(5).at("near").rel(-3);

        // ------------------------------------------------------------------ I. the fire
        d.chapter("ash", "Ash")
                .intro(
                        "* The fire is a week old. You can still smell it in your hair.",
                        "* You are eighteen. You have a cracked cauldron, a few coins, and no home.",
                        "* Azure Cloud was a small sect. It made cheap pills for fevers and farm cough. It had forty people, a herb garden, and one fat kitchen cat. Now it is a black field with one chimney standing in the middle.",
                        "* You were at the market when it burned. You saw the smoke from the road and ran the whole way. By the gate there was nothing left to save. Only Master Hui, pinned under a kitchen beam, still breathing.",
                        "@Hui| Take the cauldron. It is cracked, but it is all I could carry out.",
                        "@Hui| Do not chase the man with the torch. He is only a hand. Find the one who gave him the match.",
                        "@You| Who? Who gave it to him?",
                        "* Master Hui tried to say a name. It sounded like 'Vale,' or maybe 'valley.' Then his hand went slack.",
                        "* You buried him in the herb garden, where he liked to sit. You did not cry until the grave was finished.",
                        "* Now the cauldron sits heavy in your arms. Someone burned your home. Someone paid for it. You mean to find out who.",
                        "* But you are a pill-maker who has barely begun to cultivate. First you must become someone who can ask questions and stay alive.")
                .realm(1, "Awaken your cultivation: reach Major Lvl 1.")
                .outro(
                        "* It is a small thing, the first gate. Something inside you clicks, like a latch lifting.",
                        "* You think of Master Hui, who used to say that strength is only patience wearing armor.",
                        "* Then you think of the torch, and the patience has to be held down with both hands.")
                .pay(0, 0, 3, 0, 0, 0);

        d.chapter("wen", "The Blue Cloud").years(1).major(1)
                .setup((c, st) -> Chapters.spawn(c, st, "wen"))
                .intro(
                        "* A year goes by. You brew pills, you sell pills, and you ask careful questions in a careless voice.",
                        "* Then a traveling herbalist tells you something odd over a cup of cheap tea.",
                        "@Herbalist| There's a woman in {@wen} who sells herbs with a blue cloud stitched on her sleeve. She won't say why.",
                        "* A blue cloud. Your heart does something clumsy in your chest.")
                .meet("wen", "Find the herb seller in {@wen}.")
                .outro(
                        "* She drops her basket. Mint and sage roll across the cobbles. She does not pick them up.",
                        "@Wen| You're dead. I counted the bodies, and you weren't there, so I decided you were under the roof.",
                        "* She hugs you hard enough to hurt. She smells of crushed leaves and cheap soap, and for a moment you are twelve years old again.",
                        "@Wen| I was in the next valley picking nightbloom. I saw the smoke from the hill. I ran down, but it was too late.",
                        "@Wen| I saw one thing. A rider on the north road, going fast. Grey cloak. He ran like a runner even on a horse, if you can believe that.",
                        "@Wen| In the south they call him Cinder Wraith. He's a hired blade. Everyone knows the name, and nobody asks who hires him.",
                        "* She presses a scrap of paper into your palm. It is a page from the sect ledger, burned black at the edges.",
                        "@Wen| I took it from the gatehouse. I can't make sense of it. Maybe you will, someday.",
                        "* You do not let go of her sleeve for a long time.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("lid", "The Cracked Cauldron").years(1)
                .setup((c, st) -> Chapters.set(st, "craft0", c.itemsCrafted))
                .intro(
                        "* You and Wen share a small room above a noodle shop. The cauldron sits in the corner, cracked from rim to belly. Neither of you can look at it for long.",
                        "@Wen| Master Hui used to say a cauldron learns the hand that stirs it.",
                        "@You| It's cracked. It leaks.",
                        "@Wen| So patch it with clay and cook slowly. Make something. Anything. I'm tired of looking at it like a gravestone.",
                        "* So you do. Ten brews, one after another. Ten small, honest things.")
                .check((c, st) -> c.itemsCrafted - Chapters.flag(st, "craft0") >= 10, "Brew in the old cauldron: make ten items.")
                .outro(
                        "* On the tenth night the clay patch loosens in the heat. Under it, scratched into the iron, are words you never saw in all your years at Azure Cloud.",
                        "* 'Marrow Reach. Silent Tonic. Forgive me.'",
                        "@Wen| I never heard of a Silent Tonic.",
                        "* Neither had you. It is Master Hui's handwriting. He pressed hard, as if he wanted the iron to remember what he could not say.",
                        "@Wen| Marrow Reach is a swamp village. Nobody goes there. Why would Master Hui be sorry about a swamp?")
                .pay(0, 0, 0, 0, 1, 0);

        // ------------------------------------------------------------------ II. the kind magistrate
        d.chapter("letter", "A Kind Letter").years(2).major(2)
                .setup((c, st) -> Chapters.spawn(c, st, "orrin"))
                .intro(
                        "* A letter arrives, sealed in green wax with a leaf crest. Wen reads it over your shoulder and whistles.",
                        "* 'I knew your master well, and I have grieved for him these many years. I hear you are rising. Let me help you rebuild. My door is open, and so is my purse. Orrin Vale, Magistrate of {@vale}.'",
                        "@Wen| A magistrate wants to help us. That is either very good or very strange.",
                        "@You| Master Hui never mentioned him.",
                        "@Wen| Master Hui never mentioned a lot of things.")
                .meet("orrin", "Visit Magistrate Orrin Vale in {@vale}.")
                .outro(
                        "* Orrin Vale is nothing like a magistrate in a story. He is round and warm, with ink on his fingers and a robe that smells of cedar. He pours the tea himself.",
                        "@Orrin| Hui and I talked for hours, in the old days. He never stopped talking about his students. You, mostly. You burned the sugar pills, he said, and cried about it.",
                        "* You laugh before you can stop yourself. It is true.",
                        "@Orrin| I can't give back what was taken. But I can give you a start. Three thousand spirit stones, and a seat at my table whenever you are in the province.")
                .outChoice(new Chapters.Choice("patron", "Orrin offers you three thousand spirit stones. What do you do?",
                        "Accept, and thank him.", "Thank him, but refuse the money.")
                        .after(
                                new String[]{"@Orrin| Good. Hui would have wanted it. Don't be a stranger.", "* The purse is heavy and the tea is good, and you feel a little less alone."},
                                new String[]{"@Orrin| Proud, like your master. Fine. The invitation stands, and it costs you nothing.", "* You walk out with empty hands. Oddly, you feel lighter."}))
                .done((c, st) -> { if (Chapters.flag(st, "patron") == 1) { c.ss += 3000; c.convertCurrency(); } });

        d.chapter("marrow", "Marrow Reach").years(3).major(3)
                .intro(
                        "* The words in the cauldron will not leave you alone. You ask in taverns. You ask in libraries. A mapmaker finally answers, and she looks uneasy doing it.",
                        "@Mapmaker| Marrow Reach? It's a swamp hamlet at {@marrow}. Poor soil, bad water. The roads have grown over. People say there was a sickness there, years back. Nobody asks.",
                        "* You fold the map slowly. Forgive me, the iron said.")
                .at("marrow", "Go to Marrow Reach, at {@marrow}.")
                .outro(
                        "* There is no village left. There are reeds, and low mounds under the reeds, and a ring of leaning posts that might once have been a fence.",
                        "* An old woman sits on a stump with a pole across her knees. She watches you the way you watch weather.",
                        "@Old woman| Azure Cloud. I know that sleeve. The old man wore it.",
                        "@You| Master Hui came here?",
                        "@Old woman| Three winters after. He brought a cart of grain and he knelt in the mud. We did not forgive him. He came back every spring until the spring he didn't.",
                        "@You| Forgive him for what?",
                        "@Old woman| For the root. Bitterbell. It grows here and nowhere else. Men came with silver and a contract, and they dug it by the cartload and washed it in the creek. Within a year the water turned. Sixty of us died. My daughter. Her little brother lived, because she carried him out.",
                        "@Old woman| Your sect made the tonic. The magistrate's agents bought the root. Everyone ate, and nobody asked where the meal came from.",
                        "* She spits into the reeds. Not at you. Near you.",
                        "@Old woman| Your old man asked. Too late. That is more than most did.")
                .outChoice(new Chapters.Choice("mercy", "What do you make of what you heard?",
                        "They had every right to be angry.", "Whatever was done, the fire was murder.", "I don't know what to think.")
                        .after(
                                new String[]{"* You bow to the old woman. She does not bow back, but she does not look away either. 'Sit,' she says, 'before you fall down.'"},
                                new String[]{"* The old woman nods slowly. 'Both things are true,' she says. 'It is heavy, carrying both.'"},
                                new String[]{"* 'Honest,' says the old woman. 'Keep it that way. Sit.'"}))
                .pay(0, 0, 0, 0, 0, 1);

        // ------------------------------------------------------------------ III. the hand
        d.chapter("runner", "The Runner").years(4).major(3)
                .setup((c, st) -> Chapters.spawn(c, st, "cinder"))
                .intro(
                        "* The old woman gave you one more thing before you left, a description. 'The boy who lived. His sister carried him out and died of the water two years later. He ran off, and he never came back.'",
                        "* A runner, Wen said. A grey cloak. Cinder Wraith.",
                        "* You have been looking at your own life through a keyhole. Now you want the door.",
                        "* Word has it he guards caravans near {@cinder}.")
                .meet("cinder", "Find Cinder Wraith near {@cinder}.")
                .outro(
                        "* He is not what you pictured. He is lean and tired, with a runner's legs and a face like a closed door. He sits on a low wall, eating a cold dumpling, and he watches you come the whole way.",
                        "@Cinder| You've got herb smoke on you. Azure Cloud. So one of you lived.",
                        "@You| You burned it.",
                        "@Cinder| I did.",
                        "?mercy=1|@You| I've been to Marrow Reach.",
                        "?mercy=1|@Cinder| Then you know why.",
                        "?mercy=2|@You| Whatever they did, the children in that kitchen did nothing.",
                        "?mercy=2|@Cinder| I never checked. That is the truth. I did not check.",
                        "?mercy=3|@You| I went to Marrow Reach. I still don't know what to think.",
                        "?mercy=3|@Cinder| That makes two of us.",
                        "@Cinder| For years I told myself I would do it again. Lately I am less sure.",
                        "@Cinder| But I won't talk to someone I can put down with one hand. I'm tired of running, so I won't run from you. If you want answers, come and take them.",
                        "* He goes back to his dumpling. It is the most frightening thing anyone has ever done to you.");

        d.chapter("torch", "The Torch")
                .intro(
                        "* He is a whole Major above you. You can feel it from here, like heat off a forge.",
                        "* Train first, if you need to. He said he would wait, and he does not look like a man who lies about small things.",
                        "* When you are ready, face him. Type 'attack' while you stand in the same place.")
                .defeat("cinder", "Face Cinder Wraith and win.")
                .outro(
                        "?dead.cinder|* The fight is over. He slides down the wall and does not get up.",
                        "?dead.cinder|* You go through his coat, because you have to. A child's wooden bird. A flask. A folded paper, stiff with old sweat, sealed in green wax with a leaf crest.",
                        "?dead.cinder|* It is the same leaf as the letter on your table. 'Cinder, my boy. Make it complete. O.V.'",
                        "?dead.cinder|* You stand over him a long time. The wind smells of rain. It does not feel like winning.",
                        "?!dead.cinder|* He drops to one knee with a hand over his ribs. You hold your blade at his throat. Then you take it away.",
                        "?!dead.cinder|@Cinder| Why?",
                        "?!dead.cinder|@You| Because the one who paid you is still out there.",
                        "?!dead.cinder|* He is quiet so long that you think he will not answer.",
                        "?!dead.cinder|@Cinder| A man with a clean voice. He smelled of cedar and lamp oil. He gave me a torch and a green seal and told me Azure Cloud had poisoned my home. It was true. I believed him because it was true.",
                        "?!dead.cinder|@Cinder| He took me in when I was nine. He fed me. He taught me to run. I called him uncle.",
                        "?!dead.cinder|* His face changes slowly as he hears himself say it.",
                        "?!dead.cinder|@Cinder| He was the one who bought the root. He was there the whole time.",
                        "?!dead.cinder|* He looks like a man who has just seen his own house from the outside. 'Go,' he says. 'I need to go and sit with some graves.'",
                        "* Cedar. Lamp oil. Green wax and a leaf crest. You think of a round, warm man pouring tea.",
                        "?patron=1|* Three thousand of his spirit stones are still in your pocket. They have never felt so heavy.")
                .pay(0, 0, 0, 1, 0, 0);

        // ------------------------------------------------------------------ IV. the hand that paid
        d.chapter("ledger", "The Ledger").years(2)
                .intro(
                        "* Wen sets the green seal beside the burned ledger page, and for a long time neither of you speaks.",
                        "@Wen| Orrin Vale.",
                        "?patron=1|@Wen| You took his money.",
                        "?patron=1|@You| I know.",
                        "?patron=1|@Wen| So did Hui, I think, for years. That is what we will have to live with.",
                        "?patron=2|@Wen| And you turned down his gold. I didn't understand why at the time. I do now. Good instinct.",
                        "@Wen| We have a seal, and a story from a man who burned us. A magistrate will laugh at that. We need paper.",
                        "@Wen| A man like that keeps copies. He keeps them in his own city, in a hall he walks past every night. Go and read them.")
                .at("vale", "Find proof in the records hall of {@vale}.")
                .outro(
                        "* The records hall of {@vale} smells of dust and cedar oil. A tired clerk looks at your sect seal, looks at your face, and finds he suddenly needs to be somewhere else for an hour.",
                        "* The Marrow Reach accounts fill a whole shelf. Silver for root, silver for wagons, silver for the men who 'managed the water.' Every page is stamped with the leaf.",
                        "* Below it is a smaller book. Monthly payments to 'C.W.' for services. The last entry is a big one, dated the week of the fire.",
                        "* At the bottom of the shelf lies a single folded letter. The ink is brown. You know the hand at once.",
                        "* 'Orrin. I have seen the water at Marrow Reach. I will not make another batch. On the first of the month I will write to the Provincial Court and tell them everything. I am sorry, old friend. I should have asked sooner. Hui.'",
                        "* He had written the truth. He was one week from telling it. One week.",
                        "* You sit down on the cold floor with the letter on your knees. You cry a little, quietly, the way you could not at the grave.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("cup", "The Last Cup")
                .intro(
                        "* You are barely out of the hall when a servant finds you in the street. He is polite and out of breath.",
                        "@Servant| The Magistrate asks if you would take tea. He says he has been expecting you.",
                        "* The garden is the same garden. The tea is the same tea. Orrin Vale pours with the same careful hands, and he does not look surprised.",
                        "@Orrin| You read my records. I wondered when you would. I kept the letters because I am a vain man. Hui's was the only one I could not bring myself to burn.",
                        "@You| You killed forty people to hide a letter.",
                        "@Orrin| I killed forty people to save a province's trade, and my own neck, and thousands of jobs that depend on that root. I am not asking you to like it.",
                        "@Orrin| I am asking you to be practical. Marrow Reach is sixty graves. The fire is forty. Nothing will undo them. What is left is you and me, and a province that needs a steady hand. Sit beside me. I will make you very strong.")
                .choice(new Chapters.Choice("orrin", "What do you do?",
                        "Tell everyone. Send the records to the Court and to the people of Marrow Reach.",
                        "Draw your weapon. This ends here.",
                        "Take his offer.")
                        .after(
                                new String[]{"@Orrin| They may hang me. Or they may bribe someone, and I will live in a smaller house. You cannot know which.", "@You| No. But they will know the truth.", "* You leave the cup untouched. Behind you, Orrin Vale begins, very softly, to hum."},
                                new String[]{"@Orrin| Hui's student. Of course.", "* He sets down the cup with great care, the way you set down a thing you will never lift again.", "@Orrin| I would rather not. But I will not make it easy."},
                                new String[]{"* The tea is still warm. The records are still in your sleeve. You tell yourself it is only for now.", "* It is not only for now. In the years ahead you will grow very strong, and very comfortable, and you will stop looking at graves.", "?!dead.wen|* Wen leaves you at the city gate without a word. She does not turn around."}))
                .done((c, st) -> {
                    Character o = Chapters.person(st, "orrin");
                    if (Chapters.flag(st, "orrin") == 1 && o != null) { o.fatedTo = 0; o.ss = 0; Chapters.set(st, "exposed", 1); }
                });

        d.chapter("reckoning", "The Reckoning")
                .skipIf((c, st) -> Chapters.flag(st, "orrin") != 2)
                .intro(
                        "* Orrin Vale does not hide. He sits in the same garden with the same cup, and he waits for you with a sword across his knees.",
                        "* He is stronger than you, and he knows every stone of this city. Train first, if you need to. He will not run.",
                        "* When you are ready, face him. Type 'attack' while you stand in the same place.")
                .defeat("orrin", "Face Orrin Vale and win.")
                .outro(
                        "?dead.orrin|* He falls across the tea table and the cup breaks. For a moment he looks like what he is, a tired old man who made a mistake and then a worse one.",
                        "?dead.orrin|@Orrin| I never wanted the fire. I wanted one more year.",
                        "?dead.orrin|* There is no one to answer him. He is gone before you finish listening.",
                        "?!dead.orrin|* He kneels with his sword on the stones. You do not take his life. You take his seal.",
                        "?!dead.orrin|* By evening, the records are on their way to the Provincial Court, with his confession signed below them in shaking ink.",
                        "* Marrow Reach gets its truth. It is not enough. But it is something to bury beside the dead.")
                .done((c, st) -> {
                    Character o = Chapters.person(st, "orrin");
                    if (o != null && !o.dead) { o.fatedTo = 0; o.ss = 0; Chapters.set(st, "exposed", 1); }
                })
                .pay(0, 0, 0, 1, 0, 0);

        // ------------------------------------------------------------------ V. those who are left
        d.chapter("avenger", "Those Who Loved Him").years(25)
                .skipIf((c, st) -> Chapters.flag(st, "dead.cinder") == 0)
                .setup((c, st) -> {
                    Character pell = Chapters.spawn(c, st, "pell");
                    Character w = Chapters.person(st, "wen");
                    if (pell != null && w != null && !w.dead && Chapters.flag(st, "orrin") != 3 && GameDriver.rand.nextInt(2) == 0) { // half of the time she finds Wen first
                        w.fatedTo = 0; w.deathCause = "killed in her stall by Pell Wraith"; w.dead(pell);
                    }
                })
                .intro(
                        "* Twenty years pass. You grow stronger. You do not forget, but you stop counting.",
                        "* Then, one morning, a note is nailed to your door.",
                        "* 'You killed the man who raised me when no one else would. I am coming. Pell Wraith.'",
                        "?dead.wen|* The note is not the worst of it. A courier arrives from {@wen} at noon with his hat in his hands. Wen was found in her herb stall at dawn. Someone had been there before the stall opened. She did not have time to draw her knife.",
                        "?dead.wen|* You do not remember sitting down. You remember the smell of mint.",
                        "?!dead.wen|* A second note comes with the first, from {@wen}. 'Someone came to the stall last night. I hit her with the scale. Don't worry. Wen.' You are not sure whether to laugh or be sick.",
                        "* She has your name and a head start. Make her come to you, or go and meet her. Either way, it ends.")
                .defeat("pell", "Face Pell Wraith, who hunts you.")
                .outro(
                        "?dead.pell|* She was fast, and angry, and very young, and you were not. When it is done, you find a small wooden bird in her pocket, the twin of the one you took from her uncle.",
                        "?dead.pell|* You wonder who will carve the third.",
                        "?!dead.pell|* She lies on her back in the dust, glaring at the sky. You sit down beside her. After a while you hand her Hui's letter and the green seal.",
                        "?!dead.pell|@Pell| Then who did we kill?",
                        "?!dead.pell|@You| Nobody who deserved it. Your uncle was told a half-truth by the man who made the other half. It was a good lie. It nearly worked twice.",
                        "?!dead.pell|* She does not thank you. She does not forgive you. But she takes the letter, and in the morning she is gone, and the wooden bird is on your doorstep.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("debt", "A Letter From the South").years(15)
                .skipIf((c, st) -> Chapters.flag(st, "dead.cinder") != 0)
                .intro(
                        "* A letter comes in a rough hand, from a village you visited once and have not stopped thinking about.",
                        "* 'I did what you said. I sat with the graves. Then I started clearing the creek. The bitterbell is gone and the water is mending, slowly. Last spring a child drank from it and did not get sick. I thought you should know. I don't ask for forgiveness. I only want you to know somebody is keeping the place. C.'",
                        "* A small wooden bird is folded in the paper. Someone carved it long ago, and someone has kept it dry ever since.")
                .pay(0, 3, 0, 0, 0, 0);

        // ------------------------------------------------------------------ VI. the cauldron
        d.chapter("sect", "What a Sect Is For").years(8).major(4)
                .intro(
                        "* Decades have passed since the fire. You are no longer the frightened pill-maker who stood in the ashes.",
                        "* The old field is overgrown now. Green things grow up through the black. You stand at the gate where the chimney still stands, and you think about what a sect is for.",
                        "?!dead.wen|@Wen| You'd better build something. I'm too old to be homeless again.",
                        "?dead.wen|* You think of Wen, who would have complained about the roof.",
                        "?orrin=3|* You have everything Orrin promised: a seat, a name, a city. It is not a bad life. It is only a life with a locked door in it that you walk past every day. Tonight you will unlock it.")
                .task("FOUND_SECT", 0, "", "", "Raise the sect again: found Azure Cloud.")
                .outro(
                        "* The sign goes up on a bright morning. It is plain wood with a blue cloud painted on it, a little crooked, because you let a child hold the brush.",
                        "* The cracked cauldron stands in the middle of the new courtyard. You look at it for a long time. It has been with you through every part of this.")
                .outChoice(new Chapters.Choice("cauldron", "What will you do with the cracked cauldron, and Master Hui's secret?",
                        "Mend it and make the Silent Tonic again, safely, and send the profit to Marrow Reach.",
                        "Bury it in the herb garden beside Master Hui.",
                        "Hang it in the front hall, crack and all, so every new student sees it.")
                        .after(
                                new String[]{"* The smith says it can be done. It takes a month, and the new seam shines like a scar that has healed well."},
                                new String[]{"* You dig the hole yourself. It takes all afternoon. When it is done, the garden looks as if nothing happened, which is how Master Hui would have liked it."},
                                new String[]{"* It takes four men and a rope. When it is up, it hangs a little crooked, and the light comes through the crack in a thin blue line."}))
                .pay(5000, 0, 0, 2, 0, 1);

        d.epilogue(
                "* Azure Cloud is not a great sect. It never will be. It has forty-one people, a herb garden, and an old chimney that no one is allowed to take down.",
                "?cauldron=1|* The mended cauldron brews a pill that quiets the mind and hurts no one. Every spring a cart of coin goes south to Marrow Reach, and every spring someone sends back a jar of clean creek water with a ribbon on it.",
                "?cauldron=2|* The cauldron lies in the herb garden under a plain stone, next to a master who liked to sit there. Nobody tells new students why. They find out when they are ready.",
                "?cauldron=3|* The cracked cauldron hangs in the front hall. New students ask about it on their first day. You always tell them the whole story, even the parts that make Azure Cloud look bad.",
                "?dead.cinder|* Cinder Wraith has a stone of his own in the sect yard, set a little apart. You are still not sure it was right. Every year you put a wooden bird on it, and every year the bird is gone by morning.",
                "?!dead.cinder|* Cinder never joined the sect. But every spring a thin man in a grey cloak walks in at the gate, eats a bowl of noodles in the kitchen, and walks out again without a word. No one stops him.",
                "?exposed=1|* Orrin Vale lived out his years in a smaller house, as he said he would, and wrote to you twice a year. You answered once.",
                "?dead.orrin|* Orrin Vale's seat in {@vale} stayed empty for a long time. When it was filled, the new magistrate's first act was to pay for a well at Marrow Reach.",
                "?orrin=3|* Orrin Vale never again said the word Marrow in your hearing. Neither did you.",
                "?!dead.wen|* Wen runs the herb garden and complains about everyone else's pruning.",
                "?dead.wen|* Wen's stall in {@wen} was never sold. A girl from the noodle shop runs it now, and has started to stitch a small blue cloud on her sleeve.",
                "* On quiet nights you sit in the herb garden where Master Hui liked to sit. You tell him how it went. You do not expect an answer.",
                "* The kitchen cat, a descendant of a descendant, sits down on your feet.");
    }
}
