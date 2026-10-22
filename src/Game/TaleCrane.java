package Game;

/** The Last of the Silver Crane Clan (see "Story Plan.md"; the dialogue was rewritten on 2026-10-19 after the author's note that it sounded like a machine). The clan guarded Stillspring, a spirit vein under its hall.
 *  Baron Hexley burned the hall, but the player's uncle Aldous, the clan head, had signed the vein away three years earlier and tried to take it back. One of the three surviving cousins knew and kept quiet
 *  (rolled at the start, flag keeper: 1 Tamsin, 2 Joren, 3 Isla). The vein hums: it is heard first in the tent, then in the banners, and it is a motif of the three seasons. The clan is now people, not names:
 *  the ferryman Poll, Marta of the dumplings, the schoolmaster Pilcher and his cat. Choices: how to take Hexley (believe), how to treat the one who knew (keeper_), and what the clan is for (clan).
 *  Season 1 is "The Crane's Clan"; season 2 is "Stillspring", season 3 "The Crane's Flight" (TaleCraneSeasons). */
final class TaleCrane {
    private TaleCrane() { }

    static void register() {
        Chapters.Def d = Chapters.def("clan");
        d.season(1, "The Crane's Clan");
        d.place("hall", "wildnear").place("castle", "far").flags("keeper");
        d.cast("tamsin", "Tamsin", "Crane").she().keep().trait(Character.Traits.Gatherer).side("Good").lvl(4).age(27).start().rel(3);
        d.cast("joren", "Joren", "Crane").he().keep().trait(Character.Traits.Miner).side("Good").lvl(5).age(24).start().rel(3);
        d.cast("isla", "Isla", "Crane").she().keep().trait(Character.Traits.Tailor).side("Good").lvl(3).age(19).start().rel(3);
        d.cast("hexley", "Baron", "Hexley").he().keep().trait(Character.Traits.Leader).side("Evil").lvl(9).pot(8).age(55).at("place:castle").rel(-3);
        d.begin((c, st) -> Chapters.set(st, "keeper", 1 + GameDriver.rand.nextInt(3)));

        d.chapter("hall", "The Last of the Cranes")
                .intro(
                        "* The Silver Crane Clan lived in a valley hall for four hundred years. They were not famous. They were not rich. According to their neighbours, they were a bit too fond of tea.",
                        "* Nine days ago Baron Hexley's riders came in the night. By morning the hall was a heap of black beams and eighty-three members of the clan were gone. The valley smelled of smoke for a week, and the tea, when you tried to make it, tasted of it for a month.",
                        "* You are what is left of the main line, which means that three people are looking at you as though you knew what to do.",
                        "@Tamsin| We have a ruined hall, forty silver coins, and a goat. Go on, then.",
                        "* Tamsin is your cousin, six years older, broad in the shoulder, and has never in her life been told anything twice. She tends the herbs. She has made a list of everything the fire left and it is a short list. The goat is item four.",
                        "@Joren| Dig?",
                        "@Tamsin| We're not digging.",
                        "@Joren| A small one.",
                        "* Joren carries a shovel the way other men carry a sword, and for the same reason, which is that it makes him feel prepared. He is a miner, and cheerful in a way that borders on alarming. He sleeps well. He has slept well through the whole of it, and you have not decided if you envy him or are afraid.",
                        "@Isla| I saved the banner.",
                        "* The youngest says it as if it were an apology. She unrolls it on a stump: a silver crane on blue, standing on one leg, scorched at one corner. Under the bird, in faded thread, the clan's motto. Stand on one leg, see far. Isla is a tailor, and eighteen, and has a needle in her sleeve at all times, in case.",
                        "* It is the first time any of you has laughed in nine days. It is not a good laugh. Nobody minds.")
                .friends(5, "A clan needs people: make five friends.")
                .outro(
                        "* They come slowly. A ferryman called Poll, who owed your grandmother a favour and has come to pay it in the only coin he has, which is unlimited complaining about the weather. A widow called Marta, who makes dumplings in a pan the size of a cartwheel and refuses to be thanked. Two brothers from the next valley, Dunn and Dunnock, who never liked Hexley and are not sure why. A retired schoolmaster named Pilcher, who arrives with a cat called Verdict and a trunk of books and sets up a school in the shade of the burned wall for the six children who have no school.",
                        "* On the day the fifth one comes, Isla hangs the banner on a pole above the tent. It flaps in the wind like a bird trying to take off. Poll says that it looks as if it wants to be somewhere else, and Marta tells him it does not, it only wants a bit of encouragement, and gives him a dumpling.",
                        "@Tamsin| Well. That's a clan, I suppose.",
                        "@Joren| Can I dig now?")
                .pay(300, 0, 0, 1, 2, 0);

        d.chapter("dig", "A Small Dig").gap(30).major(1)
                .intro(
                        "* The letter comes by courier on a Thursday, and it is very short. 'I have dug a hole. It is a good hole. Come at once. Do not tell Tamsin. J.'",
                        "* Of course Tamsin finds out. She reads it over your shoulder, and her only comment is a long, heavy, ironic silence, which is the form of speech she uses for her cousin.")
                .at("hall", "Join Joren at the burned hall, {@hall}.")
                .outro(
                        "* There are eleven holes in what used to be the kitchen garden. Tamsin has fallen into two of them. She sits on the edge of the largest with her arms folded, saying nothing, while he shows you the twelfth.",
                        "* It goes down through the cellar floor and the clay, and down into rock. At the bottom is a cave, and in the cave there is a light. It is pale, and silver, and slow, and it comes out of the rock itself, like breath on a cold morning. It hums. It is a note so low that you hear it in your teeth before your ears.",
                        "@Joren| Uncle Aldous told me never to dig in the cellar. He said it was flooded. I thought it was a funny thing to say about a cellar. I asked what it was flooded with. He said tea.",
                        "@Tamsin| It's a spirit vein. A big one. Hexley wanted this.",
                        "* The light moves over your face, and for the first time in nine weeks you feel rested, in the middle of the afternoon, standing up.",
                        "@Isla| Stillspring. That's the name. Uncle Aldous said it when he thought nobody was listening. I thought it was a prayer.",
                        "@Joren| It might be. It's a nice hole for one.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("banners", "Isla's Banners").gap(30)
                .intro(
                        "* Isla sews when she is afraid. By now there are forty banners hanging in the tent, and each is slightly different from the others, and one of them is wrong.",
                        "* You do not see it at first. Then you do. On the edge of every banner, in tiny stitches, is a coin with an animal's head. It is not part of the clan crest. You have never seen it on anything.",
                        "* At night, in the tent, the banners hum. It is very faint. If you put your ear to the cloth it is the same note as the cave.")
                .meet("isla", "Ask Isla about the coin on the banners.")
                .outro(
                        "@Isla| I don't know. My hands just make it. A wolf, I think. Or a boar. I can't see the whole thing, only the nose. It's a bit like trying to remember a face.",
                        "@You| How long have you been doing it?",
                        "@Isla| Since the fire. Before that, a little. I thought I'd got it from the pattern book.",
                        "* You write the coin on a scrap of paper and put it in your pocket next to the forty silver coins. Later, in a market in the next valley, you see the same wolf-headed coin carved above a doorway. A passing trader says, without being asked: 'Hexley's mark. Don't stand under it.'")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("baron", "Baron Hexley").gap(52).major(2)
                .setup((c, st) -> Chapters.spawn(c, st, "hexley"))
                .intro(
                        "* There comes a day when you can't go on guessing, and you write to Baron Hexley. You expect no answer. He answers in two days, in a clear, formal hand: 'Of course. Come to my castle. I shall be glad to explain.'",
                        "* It is the politest thing anyone has said to you in a year, and it frightens you more than a threat would.")
                .meet("hexley", "Meet Baron Hexley at his castle in {@castle}.")
                .outro(
                        "* The Baron is a grey, handsome man of fifty-five, very upright, with a trace of weariness about the eyes. His study has one picture in it, of a boy on a pony. He pours the tea himself, and the tea is excellent, which you resent.",
                        "@Hexley| You think I am a villain. I don't blame you. Allow me one document.",
                        "* The contract is three pages long. At the bottom of the third, in a hand you know from a hundred birthday cards, is the signature of Aldous Crane, Head of the Silver Crane Clan.",
                        "@Hexley| Three years ago your uncle rode to me. The southern raiders were burning the border villages and he could not hold the valley. He offered me the guardianship of the vein beneath it in exchange for two hundred spears. I sent them. They held your border two winters. Then he changed his mind. He barred my steward from the gate and wrote that he had made a mistake.",
                        "@You| And you burned us for it.",
                        "@Hexley| I sent my captain to demand what I had paid for. My captain went further than I told him. I hanged him the following week. It did not bring anybody back, and I did not expect it to.",
                        "* He turns the cup in its saucer. His hand is perfectly steady.",
                        "@Hexley| I do not ask to be forgiven. I would like to be understood a little. I made a bargain, and I could not bear to see it broken, and the second of those is the only thing I am sorry for.")
                .outChoice(new Chapters.Choice("believe", "Baron Hexley says your uncle sold your home.",
                        "Believe him. The paper is real.", "Call him a liar to his face.")
                        .after(
                                new String[]{"@You| That's his signature.", "@Hexley| It is. I'm sorry.", "* You walk out of the castle in a daze. Nobody warns you that the worst part of hating a man is finding that he is telling the truth."},
                                new String[]{"@You| My uncle would never have done it.", "@Hexley| Your uncle did what frightened men do. I would not blame him, were I you. I would blame the raiders.", "* You leave without finishing the tea. You go on hating him, and you know a little more now about why."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("keeper", "The One Who Knew").gap(20)
                .intro(
                        "* You can't let the contract go. Your uncle's signature, under the Baron's calm words, sits in your mind like a stone in a shoe. At last you gather the three cousins in the tent, put the paper on the table, and say nothing at all.",
                        "?keeper=1|* Tamsin reads it. She sits down slowly, the way a person sits who has been told the chair is not there.",
                        "?keeper=2|* Joren reads it. The shovel he has carried for nine years slides out of his hand and clangs on the ground, and it is the first time you have seen him without a joke.",
                        "?keeper=3|* Isla reads it. Her needle drops from her sleeve and rolls under the table, and nobody picks it up.")
                .task("WAIT", 1, "", "", "")
                .outro(
                        "?keeper=1|@Tamsin| I saw it two years ago. I was taking him tea. It was on the desk, face up, and I read it standing there with the pot getting cold. I asked him. He said it was temporary. The Baron would be gone by spring.",
                        "?keeper=1|@Tamsin| I should have burned it. I should have come to you. I decided to be practical. I put the kettle on. I told myself it was none of my business, and I have been angry for two years at the wrong man.",
                        "?keeper=2|@Joren| I surveyed the vein for him four years ago. He called it a drainage job. I dug the first tunnel myself. Hexley's engineers came with their little notebooks and I showed them where everything was, because I thought they were helping.",
                        "?keeper=2|@Joren| That's how they found it. Eleven holes, and I thought I was looking for something. I think I was trying to bury it.",
                        "?keeper=3|@Isla| I was ten. I was hemming his good robe. He signed it at the desk behind me and when he'd done he looked at me and said, 'You never saw that, little crane.' I didn't know what it was. I knew he was frightened. So I stitched the coin instead, because I couldn't say it.",
                        "* The tent is very quiet. You can hear the wind in the banners, and the note under the ground, and, somewhere outside, a goat.")
                .outChoice(new Chapters.Choice("keeper_", "One of your cousins has carried this alone for years.",
                        "Forgive them, and say so.", "Tell them it hurts, and that you will get through it together.", "Hold them, and say nothing.")
                        .after(
                                new String[]{"@You| There's nothing to forgive. We were children, and he was the one who should have known better.", "* They look at you as if you had said it in another language. Then they nod, slowly, and some of the weight goes out of the shoulders."},
                                new String[]{"@You| It hurts. I won't pretend it doesn't. But there are four of us and a hole in the ground and a goat. We'll manage.", "* You are not sure it was the right thing to say. It was, at least, true."},
                                new String[]{"* You hold them. It is a long while before either of you speaks. Behind you Tamsin puts the kettle on, as she always does, because it is the only way she knows to say anything."}))
                .pay(0, 3, 0, 0, 0, 0);

        d.chapter("sect", "Stand on One Leg").gap(40).major(2)
                .intro(
                        "* You have a vein, a banner, three cousins and fifty followers. It is time to be more than a camp. Joren says it is time to dig. Tamsin says it is time to build. Isla says nothing, but her banners have a new design on the edge now: a crane standing on one leg, with a tiny house on its back.")
                .task("FOUND_SECT", 0, "", "", "Raise the clan again: found the Silver Crane sect.")
                .outro(
                        "* The hall goes up in a single summer. It is plain and strong, with a very large well. The well is Joren's hole, number twelve, with a stone rim round it and a small crane carved on the stone. Pilcher's school has a room now, with a stove, and Verdict has been given the whole of the windowsill as a mark of rank.",
                        "* On the day it is finished the three cousins stand beside you at the gate. Isla raises the banner on the new pole. It catches the wind, and for a moment it really does look like a bird about to leave the ground.",
                        "?keeper=1|@Tamsin| I planted chamomile by the steps. It was the one thing he liked. Don't say anything. It's only a plant.",
                        "?keeper=2|@Joren| I made the well extra deep. I wanted somewhere I couldn't lose anything.",
                        "?keeper=3|@Isla| I sewed a new crest for the gate. There's no coin on it. Only the crane.",
                        "* The hum is in the stones now, in the well, in the soles of your feet. It is steady and slow and glad, like a cat in a sunny window. Nobody remarks on it. The Silver Crane Clan is back, and it is smaller than it was, and a good deal louder.")
                .pay(1000, 0, 0, 1, 0, 1);

        d.chapter("fall", "The Baron's Answer").gap(52)
                .intro(
                        "* A letter comes from the castle. 'The spring is mine by contract. I am sorry. I have no wish for a war. I shall come in the spring, and I shall take what I am owed. H.'",
                        "* You read it twice. You find, rather to your surprise, that you aren't frightened any more. You write back a single line. 'Come, then.'")
                .defeat("hexley", "Face Baron Hexley, who is coming for the vein (find him, then 'attack').")
                .outro(
                        "?dead.hexley|* He goes down in the road below the gate, and his men lower their spears. They don't fight on. They stand round him in silence, as if they had been waiting for this for years.",
                        "?dead.hexley|* His son Edric, fifteen years old, picks the Baron's sword out of the dust and hands it to you, hilt first. His hands are steady. 'It was a fair fight,' he says. You cannot tell whether to hate him or to hold him.",
                        "?!dead.hexley|* He is on his knees in the road below the gate, and his men have lowered their spears. You take the contract from his belt and tear it in half, and the halves go into the mud.",
                        "?!dead.hexley|@Hexley| You are not your uncle.",
                        "?!dead.hexley|@You| No. I'm the one who stayed.",
                        "?!dead.hexley|* He bows his head. He rides home that evening, and for years nobody sees him.")
                .pay(5000, 0, 0, 1, 0, 0);

        d.chapter("crane", "What the Clan Is For").gap(30)
                .intro(
                        "* The Silver Crane Clan has a vein, a hall, a well, and no more enemies than a clan should have. On the quiet evening after the Baron's fall, the three cousins find you on the roof of the new hall, watching the stars.",
                        "@Tamsin| So. We've got it. What's it for?",
                        "@Joren| Dig?",
                        "@Tamsin| Not now.",
                        "@Isla| We ought to decide together.")
                .choice(new Chapters.Choice("clan", "Stillspring is the reason for all of it. What do you do with it?",
                        "Open it to all: a school where anyone may learn.", "Keep it for the clan, and share it only with those who earn it.", "Seal it. It has caused enough grief.")
                        .after(
                                new String[]{"@Tamsin| Aldous would have hated it.", "@You| Yes.", "@Tamsin| Good."},
                                new String[]{"@Isla| That's what Uncle Aldous wanted too.", "@You| He forgot the part where you do it honestly.", "* You make a rule: nothing in the clan is a secret that someone else has to pay for."},
                                new String[]{"@Joren| All that digging.", "@You| I know.", "@Joren| It's a good hole, though.", "* It is. You leave a flat stone over it, with a crane carved on top, standing on one leg."}))
                .pay(0, 0, 0, 1, 0, 1);

        d.interlude(1, "Too Fond of Tea",
                "* The Silver Crane Clan is not famous. It is not rich. According to its neighbours it is a bit too fond of tea. It has a spring, a well, and four hundred years of reasons to be stubborn.",
                "?clan=1|* The school at Stillspring has two hundred students. They arrive muddy and leave able to see in the dark. Nobody is turned away. Nobody is told what to believe.",
                "?clan=2|* The clan keeps the vein honest. Each year it chooses a dozen guests and teaches them what it knows. It is a small circle, and a true one.",
                "?clan=3|* The stone over the cavern has a crane on it, standing on one leg. Children sit on it to eat their lunch. Nobody tells them what lies below. Some secrets are better as benches.",
                "?believe=1|* You sent Baron Hexley's family a Midwinter card, once. It said only: we are both still here.",
                "?dead.hexley|* Edric Hexley visits once a year, quietly, and brings flowers to a grave that is not yours. You do not talk about it.",
                "?keeper=1|* Tamsin's garden has thirty kinds of chamomile now. Her tea is terrible, and everyone drinks it.",
                "?keeper=2|* Joren dug a second well, and a third. He found a fourth hole in a field. It was only a hole. He was delighted.",
                "?keeper=3|* Isla's banners hang in the halls of four clans now. Nobody knows who makes them. They have a crane on the edge, and nothing else.",
                "* On summer nights you sit on the flat place on the roof of the hall, with the banner turning below you, and listen. The hum has not stopped. Lately it has started to rise, a very little, at the end of the note, as if it were about to ask something.");
        TaleCraneSeasons.add(d);
    }
}
