package Game;

/** The Ghost-Touched (see "Story Plan.md"). The player died once and did not stay dead. Old Meng, a seer in Originis, says three people saw it happen; each tells a different, absurd story.
 *  The truth (a peach pit, a goose, a bridge) is found where it happened. Then the dead start to come to the player, one at a time, with small unfinished things, and the comedy
 *  turns kind. Choices: how to settle with the goose (goose) and what to be to the dead (door). */
final class TaleGhost {
    private TaleGhost() { }

    /** Whether this human has read this place of the Palace's story (so a story may notice what they know). */
    static int read(Character c, String site) { Lore.PState p = Lore.pstate(c); return p != null && p.found.contains(site) ? 1 : 0; }

    static void register() {
        Chapters.Def d = Chapters.def("ghost");
        d.place("bridge", "near").place("hamlet", "far").flags("lanterns");
        d.cast("meng", "Old", "Meng").she().trait(Character.Traits.Scribe).side("Neutral").lvl(10).age(77).start().rel(2);
        d.cast("lu", "Dumpling", "Lu").she().trait(Character.Traits.Friendly).side("Good").lvl(0).age(41).at("place:bridge").rel(1);
        d.cast("foo", "Captain", "Foo").he().trait(Character.Traits.Leader).side("Good").lvl(2).age(50).at("place:bridge").rel(0);
        d.cast("lin", "Lin", "Zhaoyu").he().trait(Character.Traits.Scribe).side("Neutral").lvl(0).age(29).at("place:bridge").rel(1);
        d.cast("bao", "Bao", "Tian").he().trait(Character.Traits.Friendly).side("Good").lvl(1).age(33).at("place:hamlet").rel(0);

        d.chapter("meng", "The Seer of Originis")
                .intro(
                        "* People step out of your way in the street. They do not mean to. They do it the way you step around a puddle. A baby cries when you pass. A dog sits down and refuses to move until you are gone.",
                        "* You have a terrible feeling that you are cold. Not cold like winter. Cold like a cellar.",
                        "* You also have a gap in your memory eleven days wide. The last thing you remember is a bright morning, a bridge, and the smell of a peach.",
                        "* So you go to see the one person in Originis who is said to know about such things.",
                        "* Old Meng is seventy-seven, tiny, and sits behind a counter with a pot of tea and a lot of cats. She looks you over twice.",
                        "@Meng| Oh, you're dead, dear.",
                        "@You| I'm what?",
                        "@Meng| Dead. A little. Eleven days ago. You came back, which is unusual, and you didn't bring anybody with you, which is lucky. I'm afraid you've been put out of sorts by it. You'll find you have a bit of a chill, and people will shy away. Wear a scarf. It helps.",
                        "@You| How did I die?",
                        "@Meng| Ah. I can't see that. The dead are very unreliable about their own deaths. But three people saw you fall. Find them. Ask them what they saw. Then come back and compare.",
                        "@Meng| First, though, you need a friend. A living one. It will keep you from forgetting the difference.")
                .friends(1, "Be known by the living: make one real friend.")
                .outro(
                        "* You are not sure what you expected a friend to be like. This one is a thin, stooped man with a basket of onions, who stops to ask whether you are all right.",
                        "* He does not shy away. He holds out an onion. He says you look as if you could use it.",
                        "* It is the worst thing anyone has ever given you. It is also the best. You put it in your pocket, and you cry a little, and he pretends not to notice.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("lu", "Witness One: The Baker").years(1)
                .setup((c, st) -> { Chapters.spawn(c, st, "lu"); Chapters.spawn(c, st, "foo"); Chapters.spawn(c, st, "lin"); })
                .intro(
                        "* Meng gives you three names on a bit of paper, with a small drawing of a cat next to each. It does not mean anything. She just likes drawing cats.",
                        "* The first is Dumpling Lu, who runs a steamed bun stall at {@bridge}. She was, Meng says, nearest.")
                .meet("lu", "Find Dumpling Lu, who runs a bun stall in {@bridge}.")
                .outro(
                        "* The stall is wonderful. It smells of ginger and steam. Dumpling Lu is a round woman with flour to the elbows and a loud, happy voice that carries across the square.",
                        "@Lu| Oh, I remember you! I'll never forget it! You came to my stall at noon and bought a pork bun, and you bit into it, and you choked!",
                        "@Lu| Went purple! Dropped like a sack! I tried to help, I really did, I hit you on the back with my rolling pin, and I think that was where I went wrong. You went limp. I was sure you were gone.",
                        "@Lu| Then you got up two minutes later and said you needed a nap. I've felt bad about it ever since. Have a bun. It's free. I'll feel better.",
                        "* You take the bun. It is excellent. You are almost certain you were not choking. You decide not to say so.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("foo", "Witness Two: The Captain").years(1)
                .intro("* The second name on the list is Captain Foo of the city watch. He was on duty at the bridge that day, Meng says. Everybody is always on duty at the bridge.")
                .meet("foo", "Find Captain Foo of the city watch.")
                .outro(
                        "* Captain Foo is a very straight man with a very straight moustache. He tells it standing at attention.",
                        "@Foo| Sir. Ma'am. Citizen. I saw it clearly. A giant crab came out of the creek at the Mill Bridge. The size of a cart. Eight feet across.",
                        "@Foo| You stood your ground. You drew your ladle.",
                        "@You| My ladle?",
                        "@Foo| It was the nearest weapon. You fought bravely. The crab got you in the end. A terrible loss. The watch will remember it.",
                        "@Foo| The crab escaped. I would be grateful if you did not mention it to my superiors. There has been paperwork.",
                        "* He salutes. You return it. Neither of you knows why.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("lin", "Witness Three: The Poet").years(1)
                .intro("* The third name is Lin Zhaoyu, a poet. He lives above a tea house and, according to Meng, is the only witness who has never been wrong. About anything. Ever. He will tell you himself.")
                .meet("lin", "Find Lin Zhaoyu, the poet.")
                .outro(
                        "* The poet reclines on a divan with a brush behind his ear and a look of immense, wounded importance.",
                        "@Lin| Ah. The corpse. I wondered when you'd come.",
                        "@Lin| I will be brief. I was on the bridge reciting my new work, 'Ode to a Plum, in Seventeen Cantos.' You were listening. You were riveted. In the ninth canto, I saw your eyes glaze, I saw your knees buckle, and I saw you fall.",
                        "@Lin| It was, I believe, the beauty. I have always feared that the beauty would be fatal to a delicate soul.",
                        "@You| It was boredom.",
                        "@Lin| It was the beauty.",
                        "* You do not argue with him. It would be like arguing with the weather. You write the three stories down in order. A bun. A crab. A plum.",
                        "* None of them can be right. All of them are certain.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("bridge", "The Truth About the Bridge").years(1)
                .intro(
                        "* You take the three stories back to Meng. She reads them with her tea and says 'Ha.' Then she says 'Hmm.' Then she says 'Well, that is perfectly useless, isn't it.'",
                        "@Meng| Go to the bridge. Stand where you fell. The dead remember the places where they died better than the people who watched. Take a deep breath. Try not to be sensible.",
                        "* The Mill Bridge, at {@bridge}, is stone, old, and very ordinary. There is a goose on it.")
                .at("bridge", "Stand where you fell, on the bridge at {@bridge}.")
                .outro(
                        "* You stand in the middle of the bridge. Nothing happens. The goose looks at you. You look at the goose.",
                        "* Then it comes back all at once, like a window thrown open.",
                        "* A warm morning. A peach, which you were eating, juice running down your wrist. A crab in the creek below, which had caught your eye. A man declaiming something about plums in the distance. A steaming bun, on the stall, not yet bought.",
                        "* And a goose. This goose. Standing in the middle of the bridge, exactly where it is now, with its neck out and its wings half open and the unshakeable confidence of an animal that has never been told no.",
                        "* You said you were crossing. It said it was crossing. Neither of you was prepared to give way. There was an argument, which you lost. You stepped back to find a better angle, and put your foot on a peach pit.",
                        "* Your leg went out. The railing was low. You remember a surprising amount of air, and a very small splash, and the goose, peering over the edge, looking pleased.",
                        "* A peach pit, a goose, and a bridge. The others just happened to be nearby.",
                        "@Meng| (in your head, from far away) Oh, you've remembered. How awful. I'm so sorry. It's very undignified, isn't it. Come home and have some tea.")
                .outChoice(new Chapters.Choice("goose", "The goose is still on the bridge, and still looks pleased.",
                        "Forgive the goose.", "Take a firm stand. Cross anyway.", "Leave a peach on the railing as a peace offering.")
                        .after(
                                new String[]{"* You bow to the goose. It honks, once, and moves aside. You are quite sure it has no idea what you were apologising for."},
                                new String[]{"* You cross. The goose bites you on the ankle, hard. It is, to be fair, a very clean bite. You are not even a little surprised."},
                                new String[]{"* You put the peach on the railing. The goose looks at it, then at you, then at the peach. It eats the peach in three bites and, to your surprise, honks something that sounds very much like thank you."}))
                .pay(200, 0, 0, 0, 0, 0);

        // ------------------------------------------------------------------ the dead come
        d.chapter("pang", "A Letter for Mister Pang").years(2).major(1)
                .intro(
                        "* The first ghost comes on a rainy evening. You are sitting in your room with a bowl of rice, and suddenly the rice is cold, and there is a small woman at the foot of your bed.",
                        "* She is old and tidy and see-through. She has her hands folded in front of her, like someone waiting for a bus.",
                        "@Ghost| Excuse me. You're one of the ones who can hear. I've been trying for forty years. Everyone else just gets goosebumps and leaves.",
                        "@Ghost| My name is Mrs. Pang. My husband never got my last letter. I wrote it on the night I died, and it's in the lining of my sewing box, which is in a hamlet at {@hamlet}. He's still alive. He's ninety-one. I'd like him to read it before it's too late.")
                .at("hamlet", "Take Mrs. Pang's last letter to her husband at {@hamlet}.")
                .outro(
                        "* The sewing box is exactly where she said. The letter is yellow and fragile. You carry it as if it were a bird.",
                        "* Mister Pang is a tiny, deaf, cheerful man, sitting on a porch in the sun. You hand him the letter. He reads it for a long time, with his lips moving.",
                        "* When he finishes, he looks up. He does not look at you. He looks at a spot a little to your left.",
                        "@Mister Pang| Well, dear. You always did have the last word.",
                        "* The air next to you goes warm. It is the first warm thing you have felt in months.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("soldier", "The Soldier Who Wanted to Say Sorry").years(2)
                .setup((c, st) -> Chapters.spawn(c, st, "bao"))
                .intro(
                        "* The second ghost has been waiting on a bench in the park for eleven years. He is young, tall, in an old uniform, and he looks very ashamed.",
                        "@Ghost| I was a soldier. I fell out with my brother before the war. Over something stupid. A chicken. I didn't get to say sorry before I went. I'd like him to know. His name is Bao. He lives in {@hamlet}, now, I think.",
                        "@You| I've just been there.",
                        "@Ghost| Have you? Small world. Smaller dead.")
                .meet("bao", "Find Bao Tian and give him a message from his brother.")
                .outro(
                        "* Bao is a thick, gruff farmer, and he does not believe you for a full minute. Then you say the word chicken.",
                        "* He sits down very suddenly on an upturned bucket. He covers his face. He says nothing for a long time.",
                        "@Bao| It was a good chicken, too.",
                        "* The air by your shoulder gets warm again. It is easier, the second time. You almost enjoy it.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("child", "A Game of Stones").years(2)
                .setup((c, st) -> Chapters.set(st, "lanterns", read(c, "lanternroad")))
                .intro(
                        "* The third ghost is the smallest. She is about five. She is sitting in the corner of an inn's courtyard, building a tower of stones that falls over, and building it again.",
                        "@Ghost| Do you want to play? Nobody ever does. They look at me and go away.",
                        "?lanterns|@Ghost| The old ones on the grey road say somebody puts lights out. Not people. Lights. Will they put mine out?",
                        "?lanterns|@You| Not yours. Nobody gets yours.",
                        "?lanterns|* You are not sure that is true. You said it anyway.",
                        "* She is so small and so cheerful that it hurts. You are not sure what she wants. It does not seem to be anything. She wants someone to play with.")
                .task("WAIT", 3, "", "", "")
                .outro(
                        "* You stack stones with her for three afternoons. Her tower is better than yours. She is very patient with you.",
                        "* On the third afternoon, she looks up at you with a small serious face.",
                        "@Ghost| I think I can go now. I only wanted somebody to finish one.",
                        "* The tower is standing. You are almost certain it was your last stone that did it. She waves, and she is gone, and the stones are only stones.",
                        "* You sit in the courtyard until it is dark.")
                .pay(0, 3, 0, 0, 0, 0);

        d.chapter("tea", "What Meng Never Said").years(3).major(2)
                .intro(
                        "* You have not seen Old Meng for a year. When you do, she is thinner, and she has stopped drawing cats.",
                        "@Meng| Sit. Don't fuss. I'm seventy-nine and I've had a good innings. I wanted to tell you something before I go.",
                        "@Meng| You think you're ghost-touched. That you were touched by something. Wrong, dear. I looked at your tea leaves every day for a year. You're not touched. You're a door.",
                        "@Meng| The dead go through. They don't know where else to go. You were a door before you died, and you've been a door ever since. That's all it is. You can keep it open or shut. I never could decide. It's your choice.")
                .realm(3, "Outlive the whispers: reach Major Lvl 3.")
                .outro(
                        "* Old Meng dies on a warm spring day, with a cat on her lap and a pot of tea going cold at her elbow. She is in no pain.",
                        "* You are at her side. You hold her hand. She does not see you; she looks at the ceiling, and says in a surprised voice: 'Oh. Look at all of you.'",
                        "* A moment later the room is filled with a quiet, shifting warmth, and for the first time in your life you are not cold.")
                .outChoice(new Chapters.Choice("door", "You are a door, and the dead are standing on the other side.",
                        "Keep the door open. Let them come.", "Open it only for those who need you.", "Close it. Rest.")
                        .after(
                                new String[]{"* They come all the time. It's noisy and sad and funny. You have never been so busy, and you have never been so well."},
                                new String[]{"* You become choosy. A woman with a letter. A man with a grudge. A child with a tower. Each of them is a small fortune."},
                                new String[]{"* The door shuts quietly, like a kindness. For a long time the house is very silent. In the end you put the kettle on, and you are not alone."}))
                .done((c, st) -> { Character m = Chapters.person(st, "meng"); if (m != null && !m.dead) { m.fatedTo = 0; m.deathCause = "old age (lived " + m.age + " years)"; m.dead(); } })
                .pay(0, 0, 0, 2, 0, 1);

        d.epilogue(
                "* You died once and did not stay dead. It was, you decided long ago, the best thing that ever happened to you. Just don't tell the goose.",
                "?door=1|* There is a little shop now at {@bridge}, with a lantern in the window. Its sign says Lost Property (Spiritual). The dead leave things there, and the living pick them up. You run it, in your scarf.",
                "?door=2|* You keep a small ledger of the ones who came. Mrs. Pang is in it, and the soldier, and a girl with a tower of stones. There are eleven now. You have a tune for each.",
                "?door=3|* A pale thing sits by the window sometimes, on bright mornings, and watches the street. You leave a cup of tea beside it. It never drinks. It likes to be asked.",
                "?goose=1|* The goose has retired to a pond. It is believed to be a hundred and four years old. It has never apologised.",
                "?goose=2|* The goose bites you every time you cross the bridge. You have come to look forward to it. It is the only constant in an uncertain world.",
                "?goose=3|* A peach appears on the railing each summer. Nobody knows who leaves it. The goose is not saying.",
                "* On the night of the Festival of Hungry Ghosts, the whole city lights lanterns on the river. You stand on the Mill Bridge in your scarf. The lanterns drift past.",
                "* You do not feel cold at all.");
    }
}
