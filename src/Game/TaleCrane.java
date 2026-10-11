package Game;

/** The Last of the Silver Crane Clan (see "Story Plan.md"). The clan guarded Stillspring, a spirit vein under its hall. Baron Hexley burned the hall, but the player's uncle Aldous, the
 *  clan head, had signed the vein away three years earlier and tried to take it back. One of the three surviving cousins knew and kept quiet (rolled at the start, flag keeper: 1 Tamsin,
 *  2 Joren, 3 Isla). The tone swings between warm comedy (Joren digs holes) and grief. Choices: how to take Hexley (believe), how to treat the one who knew (keeper_), and what the
 *  clan is for (clan). */
final class TaleCrane {
    private TaleCrane() { }

    static void register() {
        Chapters.Def d = Chapters.def("clan");
        d.place("hall", "wildnear").place("castle", "far").flags("keeper");
        d.cast("tamsin", "Tamsin", "Crane").she().trait(Character.Traits.Gatherer).side("Good").lvl(4).age(27).start().rel(3);
        d.cast("joren", "Joren", "Crane").he().trait(Character.Traits.Miner).side("Good").lvl(5).age(24).start().rel(3);
        d.cast("isla", "Isla", "Crane").she().trait(Character.Traits.Tailor).side("Good").lvl(3).age(19).start().rel(3);
        d.cast("hexley", "Baron", "Hexley").he().trait(Character.Traits.Leader).side("Evil").lvl(9).pot(8).age(55).at("place:castle").rel(-3);
        d.begin((c, st) -> Chapters.set(st, "keeper", 1 + GameDriver.rand.nextInt(3)));

        d.chapter("hall", "The Last of the Cranes")
                .intro(
                        "* The Silver Crane Clan lived in a valley hall for four hundred years. They were not famous. They were not rich. They were, according to their neighbours, a bit too fond of tea.",
                        "* Nine days ago, Baron Hexley's riders came in the night. By morning, the hall was a heap of black beams. Eighty-three members of the clan were gone. The valley smelled of smoke for a week.",
                        "* You are what is left of the main line. The survivors look to you, and you do not feel in the least like a leader.",
                        "* There are three of them. Tamsin Crane, your cousin, six years older than you, broad-shouldered and practical, who tends the herbs and will not be told anything twice. Joren Crane, another cousin, a miner, who carries a shovel everywhere and is cheerful in a way that borders on alarming. And Isla Crane, the youngest, quiet as a mouse, a tailor, with a needle in her sleeve.",
                        "@Tamsin| We have a ruined hall, forty silver coins, and a goat. What do we do?",
                        "@Joren| Dig?",
                        "@Tamsin| We are not digging.",
                        "@Joren| A small dig.",
                        "@Isla| I saved the banner.",
                        "* She unrolls it on a stump. A silver crane on blue, standing on one leg, scorched at one corner. Under it, in faded thread, the clan's motto: Stand on one leg, see far.",
                        "* It is the first time any of you has smiled in nine days.")
                .friends(5, "A clan needs allies: make five friends.")
                .outro(
                        "* They come slowly: a ferryman who owed your grandmother a favour, a widow who made the best dumplings in the province, two brothers from the next valley who had never liked Hexley, a retired schoolteacher with an enormous cat.",
                        "* On the day the fifth one arrives, Isla hangs the banner on a pole above the tent. It flaps in the wind like a bird trying to take off.",
                        "@Tamsin| Well. That is a clan, I suppose.",
                        "@Joren| Can I dig now?")
                .pay(300, 0, 0, 1, 2, 0);

        d.chapter("dig", "A Small Dig").years(1).major(1)
                .intro(
                        "* The letter arrives by courier. It is very short.",
                        "* 'I have dug a hole. It is a very good hole. Please come at once. Do not tell Tamsin. J.'",
                        "* Of course Tamsin finds out.")
                .at("hall", "Join Joren at the burned hall, {@hall}.")
                .outro(
                        "* Joren has been busy. There are eleven holes in what used to be the garden. Tamsin has fallen into two of them. She sits on the edge of the largest, with her arms folded, and lets him show you the twelfth.",
                        "* It goes down through the cellar floor, down through the clay, down into rock. At the bottom is a cave, and in the cave there is a light.",
                        "* It is pale, and silver, and slow, and it comes out of the rock itself. It hums. It feels like standing at the edge of a very large, very quiet lake.",
                        "@Joren| I didn't know. Uncle Aldous said the cellar was flooded. He said never to dig. I think this is what he meant.",
                        "@Tamsin| It's a spirit vein. A big one. This is what Hexley wanted.",
                        "* The light moves over your face, and you feel, for the first time in nine weeks, rested.",
                        "@Isla| It's called Stillspring. Uncle Aldous used to say the name when he thought nobody was listening. I thought it was a prayer.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("banners", "Isla's Banners").years(1)
                .intro(
                        "* Isla has been sewing again. She sews whenever she is afraid. By now there are forty banners hanging in the tent, and each of them is slightly different, and one of them is wrong.",
                        "* You do not notice at first. Then you do. On the edge of every banner, in tiny stitches, is a little coin with an animal's head. It is not part of the clan crest. You have never seen it on anything.")
                .meet("isla", "Ask Isla about the coin on the banners.")
                .outro(
                        "@Isla| I don't know where it comes from. My hands just do it. I think it's a wolf. Or a boar. I can't see the whole thing.",
                        "* She turns pale as she says it. You do not push. You write the coin on a scrap of paper, and put it in your pocket, next to the forty silver coins.",
                        "* Later, in a market in the next valley, you see the same wolf-headed coin carved above a doorway. A passing trader says, without being asked: 'Hexley's mark. Don't stand under it.'")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("baron", "Baron Hexley").years(2).major(2)
                .setup((c, st) -> Chapters.spawn(c, st, "hexley"))
                .intro(
                        "* There comes a day when you cannot go on guessing. You write to Baron Hexley. You expect no answer. He answers within two days, in a clear, formal hand: 'Of course. Come to my castle. I shall be glad to explain.'",
                        "* It is the politest thing anyone has ever said to you. It frightens you more than a threat.")
                .meet("hexley", "Meet Baron Hexley at his castle in {@castle}.")
                .outro(
                        "* The Baron is a grey, handsome man of fifty-five, very upright, with a trace of weariness about the eyes. His study is plain. He pours you tea, and the tea is excellent.",
                        "@Hexley| You think I am a villain. I do not blame you. Let me show you something.",
                        "* He slides a paper across the table. It is a contract. It is three pages long and it is signed, at the bottom of the third, by Aldous Crane, Head of the Silver Crane Clan.",
                        "@Hexley| Three years ago your uncle came to me. The southern raiders were closing in on your valley. He could not hold it. He offered me guardianship of the valley and the spring beneath it, in exchange for the protection of my men.",
                        "@Hexley| I sent them. They held the border for two years. Then your uncle changed his mind. He barred my steward from the gate. He said he had made a mistake.",
                        "@Hexley| I am a patient man, but I paid for what he sold me. I sent my captain to demand it. My captain went further than I told him. He burned your hall. I hanged him for it, the next week. It did not bring anyone back.",
                        "* He is quiet for a time. The teacup in his hand does not shake.",
                        "@Hexley| I do not ask your forgiveness. I only wish you to know that I am not a monster. I am a man who made a bargain and could not bear to see it broken.")
                .outChoice(new Chapters.Choice("believe", "Baron Hexley says your uncle sold your home.",
                        "Believe him. The paper is real.", "Call him a liar to his face.")
                        .after(
                                new String[]{"@You| The signature is my uncle's.", "@Hexley| It is. I am sorry.", "* You walk out of the castle in a daze. It is a terrible thing to find that the man you hate is telling the truth."},
                                new String[]{"@You| My uncle would never have done it.", "@Hexley| Your uncle did what frightened men do. I would not blame him, if I were you. I would blame the raiders.", "* You leave without finishing the tea. You go on hating him, and you know a little more now about why."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("keeper", "The One Who Knew").years(1)
                .intro(
                        "* You cannot let the contract go. Your uncle's signature, under the Baron's calm words, sits in your mind like a stone in a shoe.",
                        "* You gather the three cousins in the tent, and put the paper on the table, and say nothing at all.",
                        "?keeper=1|* Tamsin looks at it a long moment. Then she sits down very slowly. She does not take her eyes off the page.",
                        "?keeper=2|* Joren looks at it. The shovel he has carried for nine years slides out of his hand and clatters on the floor. It is the first time you have seen him without a joke.",
                        "?keeper=3|* Isla looks at it. She does not breathe. The needle in her sleeve falls out and rolls under the table, and nobody picks it up.")
                .task("WAIT", 1, "", "", "")
                .outro(
                        "?keeper=1|@Tamsin| I knew. I was taking tea to his study two years ago and I saw it on the desk. I asked him. He said it was temporary. That the Baron would be gone by spring. I should have burned it. I should have gone straight to you.",
                        "?keeper=1|@Tamsin| I didn't. I decided to be practical. I put the kettle on and I told myself it was none of my business. I have been angry for two years, and I have been angry at the wrong person.",
                        "?keeper=2|@Joren| I surveyed the vein for Aldous, four years ago. He said it was a drainage project. I dug the first tunnel. Hexley's engineers came and looked and wrote things down, and I thought they were helping.",
                        "?keeper=2|@Joren| That was how he knew where it was. That was me. Eleven holes, and I thought I was looking for something. I was trying to bury it.",
                        "?keeper=3|@Isla| I was ten. I was sewing his robe. I saw him sign. He looked at me and said, 'You never saw this, little crane.' I didn't know what it was. I just knew he was frightened. I never told anyone. I embroidered the coin instead, because I couldn't say it out loud.",
                        "?keeper=3|@Isla| I'm so sorry. I'm so sorry.",
                        "* The tent is very quiet. You can hear the wind in the banners, and, somewhere, a goat.")
                .outChoice(new Chapters.Choice("keeper_", "One of your cousins has carried this alone for years.",
                        "Forgive them, and say so.", "Tell them it hurts, and that you will get through it together.", "Hold them, and say nothing.")
                        .after(
                                new String[]{"@You| There is nothing to forgive. We were all children, and he was the one who should have known better.", "* They look at you for a moment as though you have said something in another language. Then they nod, slowly, and some of the weight goes out of them."},
                                new String[]{"@You| It hurts. I won't pretend it doesn't. But there are four of us now, and a hole in the ground, and a goat. We'll manage.", "* You are not sure it is the right thing to say. It is, you think, true."},
                                new String[]{"* You hold them. It is a long time before either of you speaks. Behind you, Tamsin puts the kettle on, the way she always does, because tea is the only way she knows to say I love you."}))
                .pay(0, 3, 0, 0, 0, 0);

        d.chapter("sect", "Stand on One Leg").years(2).major(2)
                .intro(
                        "* You have a vein, a banner, three cousins and fifty followers. It is time to be something more than a camp.",
                        "* Joren says it is time to dig. Tamsin says it is time to build. Isla says nothing, but her banners now have a new design on the edge: a crane, standing on one leg, with a tiny house on its back.")
                .task("FOUND_SECT", 0, "", "", "Raise the clan again: found the Silver Crane sect.")
                .outro(
                        "* The hall is rebuilt in a single summer. It is plain and strong and it has a very large well. The well is Joren's hole, number twelve, with a stone rim.",
                        "* On the day it is finished, the three cousins stand beside you at the gate. Isla raises the banner on the new pole. It catches the wind, and for a moment it really does look like a bird about to take off.",
                        "?keeper=1|@Tamsin| I planted a herb garden where the old one was. I put the chamomile by the steps. It's the one thing he used to like.",
                        "?keeper=2|@Joren| I made the well very deep. I hope that's all right. I wanted it to be somewhere I couldn't lose anything.",
                        "?keeper=3|@Isla| I sewed a new crest for the gate. There's no coin on it. Just the crane.",
                        "* The Silver Crane Clan is back. It is smaller than it was. It is, you think, a bit better.")
                .pay(1000, 0, 0, 1, 0, 1);

        d.chapter("fall", "The Baron's Answer").years(2)
                .intro(
                        "* A letter comes from the castle. It is very short. 'The spring is mine by contract. I am sorry. I have no wish for a war. I will come in the spring, and I will take what I am owed. - H.'",
                        "* You read it twice. You are, you find, not frightened any more.",
                        "* You write back one line. 'Come, then.'")
                .defeat("hexley", "Face Baron Hexley, who is coming for the vein (find him, then 'attack').")
                .outro(
                        "?dead.hexley|* He falls on the road below the gate, and his men lower their spears. They do not fight on. They stand around him, silent, as if they had been waiting for this for a long time.",
                        "?dead.hexley|* His son Edric, a boy of fifteen, takes the Baron's sword from the dust and hands it to you, hilt first. His hands do not shake. 'It was a fair fight,' he says. You do not know whether to hate him or hold him.",
                        "?!dead.hexley|* He is on his knees on the road below the gate. His men have lowered their spears. You take the contract from his belt, and tear it in half, and the halves fall into the mud.",
                        "?!dead.hexley|@Hexley| ...You are not your uncle.",
                        "?!dead.hexley|@You| No. I'm the one who stayed.",
                        "?!dead.hexley|* He bows his head. He rides home that same evening, and nobody sees him again for a long time.")
                .pay(5000, 0, 0, 1, 0, 0);

        d.chapter("crane", "What the Clan Is For").years(1)
                .intro(
                        "* The Silver Crane Clan has a vein, a hall, a well, and no more enemies than a clan should have.",
                        "* On the quiet evening after the Baron's fall, the three cousins find you on the roof of the new hall, watching the stars.",
                        "@Tamsin| So. Now that we have it. What is it for?",
                        "@Joren| Dig?",
                        "@Tamsin| Not now.",
                        "@Isla| I think we should decide it together.")
                .choice(new Chapters.Choice("clan", "Stillspring is the reason for all of it. What do you do with it?",
                        "Open it to all: a school where anyone may learn.", "Keep it for the clan, and share it only with those who earn it.", "Seal it. It has caused enough grief.")
                        .after(
                                new String[]{"@Tamsin| Aldous would have hated it.", "@You| Yes.", "@Tamsin| Good."},
                                new String[]{"@Isla| That is what Uncle Aldous wanted, too.", "@You| Yes. But he forgot the part where you do it honestly.", "* You make a rule: nothing in the clan is a secret that someone else has to pay for."},
                                new String[]{"@Joren| ...All that digging.", "@You| I know.", "@Joren| It's a good hole, though.", "* It is. You leave a flat stone over it, with a crane carved on top, standing on one leg."}))
                .pay(0, 0, 0, 1, 0, 1);

        d.epilogue(
                "* The Silver Crane Clan is not famous. It is not rich. According to its neighbours it is a bit too fond of tea. It has a spring, a well, and four hundred years of reasons to be stubborn.",
                "?clan=1|* The school at Stillspring has two hundred students. They arrive muddy, and leave able to see at night. Nobody is turned away. Nobody is told what to believe.",
                "?clan=2|* The clan keeps the vein honest. Each year it chooses a dozen guests, and teaches them what it knows. It is a small circle, and a true one.",
                "?clan=3|* The stone over the cavern has a crane on it, standing on one leg. Children sit on it to eat their lunch. Nobody tells them what lies below. Some secrets are better being a bench.",
                "?believe=1|* You sent Baron Hexley's family a Midwinter card, once. It said only: we are both still here.",
                "?dead.hexley|* Edric Hexley visits once a year, quietly, and brings flowers to a grave that is not yours. You do not talk about it.",
                "?keeper=1|* Tamsin's garden has thirty kinds of chamomile now. Her tea is terrible, and everyone drinks it.",
                "?keeper=2|* Joren dug a second well, and a third. He found a fourth hole in a field. It was just a hole. He was delighted.",
                "?keeper=3|* Isla's banners hang in the halls of four clans now. Nobody knows who makes them. They have a crane on the edge, and nothing else.",
                "* On the roof of the new hall there is a flat place where you sit on summer nights. Below you, the banner turns in the wind.",
                "* Stand on one leg, the motto says. See far. You are, you think, finally beginning to.");
    }
}
