package Game;

/** The Beast-Tamer's Daughter (see "Story Plan.md"). The player's mother, Isha Wildmere, walked into the mountains when the player was seven, with her three great companions: Moss the
 *  lake tortoise, Quill the storm crane and Old Ember the cinder boar. The three are found one by one (each is a place and a scene, and each becomes a real companion of the player,
 *  see Taming.legend). Ember was burned by hunters and fears fire; Hunter-captain Brodin is still after him. Choices: how to win Ember (ember) and what to be afterwards (calling). */
final class TaleTamer {
    private TaleTamer() { }

    static void register() {
        Chapters.Def d = Chapters.def("tamer");
        d.place("lake", "wild").place("cliff", "wild").place("wood", "wild");
        d.cast("brodin", "Captain", "Brodin").he().trait(Character.Traits.Leader).side("Evil").lvl(5).pot(6).age(44).at("place:wood").rel(-3);

        d.chapter("voice", "The Tune in the Throat")
                .intro(
                        "* You were seven when your mother walked into the mountains. Your father told you she had gone to look for something. He said it kindly, the way you tell a child that the sun will come up.",
                        "* He raised you on a small farm at the edge of the wild. Every animal on that farm was calm around you. The goats followed you. The geese did not hiss. Once, a wolf stood at the treeline for an hour, watching you hang the washing, and then went away without a sound.",
                        "* Beasts hesitate before they attack you. You have never known why.",
                        "* Your father gave you nothing of your mother's except a tune. He hummed it when he cooked, three slow notes, and then the same three notes higher. You asked once where it was from. He said he did not remember. He was lying. You could tell, because he stopped humming.",
                        "* Now you are eighteen, and the farm is quiet, and you have decided to find out what the wild wants with you.",
                        "* The first thing is to find a beast and ask it. In the old books it is called taming. You use the word 'tame' where beasts roam.")
                .task("TAME", 1, "", "", "Win a beast's trust: tame one (use 'tame' where beasts roam).")
                .outro(
                        "* It is a young thing, a little grey fox-like creature, with ears too big for its head. It stands in front of you in the grass, wary, ready to bolt.",
                        "* You do not move. You do what comes natural. You hum.",
                        "* Three slow notes. The same three, higher.",
                        "* The fox sits down. It looks at you with an expression you cannot read. Then, from deep in its throat, very softly, it answers: three slow notes, and the same three, higher.",
                        "* You have not told it the tune. Nobody has taught it the tune.",
                        "* You put your hand out, and it presses its forehead into your palm, and you realise you are crying.")
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("legacy", "Isha Wildmere").years(1).major(1)
                .intro(
                        "* A letter comes in your father's careful hand. He does not write much. He is not a man for letters.",
                        "* 'My child. I have been a coward. I should have told you the truth when you were small, and I could not stand to say it aloud. Your mother was Isha Wildmere. In her day she was called the Voice of the Wild. She spoke to the great beasts and they listened.'",
                        "* 'She had three: Moss, who sleeps in a lake in the north. Quill, who nests on a cliff where the storms break. And Old Ember, who was her oldest friend, and the most dangerous. When you were seven she went into the mountains to find Ember, who had gone missing. She did not come back.'",
                        "* 'I did not follow her. I had you. I made that choice, and I do not regret it, but I regret everything else. Enclosed is her bone whistle. She said to give it to you when you were old enough to be afraid of nothing. You were never afraid of anything. I should have given it to you years ago.'")
                .realm(1, "Reach Major Lvl 1, and learn what your mother was.")
                .outro(
                        "* The whistle is yellow with age, no longer than your finger, with a tiny hole at each end. You put it to your lips. You do not blow. You are not quite ready.",
                        "* You say the three names aloud. Moss. Quill. Ember. They sound like a prayer.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("moss", "Moss, the Lake That Sleeps").years(2)
                .intro(
                        "* The north is cold and full of water. The lake is called Still Water on the maps, and Nobody's Lake by the people who live near it. A hunter once tried to wade across, and came back an hour later with a gentle, confused expression.",
                        "* People say the lake is asleep. People say if you listen very carefully you can hear it breathe.")
                .at("lake", "Find the sleeping lake at {@lake}.")
                .outro(
                        "* The lake is flat and wide and dark green. A small island sits in the middle. It is only when you are standing on the shore that you notice the island has a head.",
                        "* You do not speak. You sit on a flat stone and wait. You wait all day. The island does not move. The light changes. A heron lands, thinks better of it, and leaves.",
                        "* At dusk a great slow voice speaks in your mind, in no language you know, but you understand every word.",
                        "@Moss| You smell of her. You have her way of sitting.",
                        "@Moss| She came here, a long time ago, when she was younger than you. She said she would come back. I have been very patient. I am good at it.",
                        "@Moss| You have waited a whole day without being asked. That is the answer. She was never in a hurry either.",
                        "* The island rises. Water pours from a shell the size of a house. A huge, soft, ancient face turns toward you, and blinks once, like a door opening on a warm room.",
                        "@Moss| I would like to come with you. If it is no trouble. I shall be slow.")
                .done((c, st) -> Taming.legend(c, "Moss", "Lake Tortoise", Math.max(10, c.lvl + 3)))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("quill", "Quill, Who Rides the Storm").years(2).major(2)
                .intro(
                        "* Moss told you where the second one lives, in the long slow way he tells everything. 'Quill has a nest where the storms break,' he said. 'She is fond of drama.'",
                        "* You have heard of the cliff. Sailors call it the Last Step, because it is the last thing they see before the clouds close over. Nobody climbs it. A great white shape circles over it in bad weather. People say it is a bird. People say it is a bad omen.")
                .at("cliff", "Climb the cliff called the Last Step at {@cliff}.")
                .outro(
                        "* You climb for six hours. The wind tries to remove you twice. You hold on with your fingers and a lot of stubbornness.",
                        "* At the top there is a nest the size of a barn, built of driftwood, rope, and sailors' hats. In the middle of it stands a crane taller than a horse, white as paper, with a bill like a sword. She looks at you with one cold gold eye.",
                        "@Quill| You are slower than your mother. But you have her feet.",
                        "@Quill| I will not take you up unless you trust me. The cliff is high, the wind is wild, and I am rather proud of how I catch things. Will you step off?")
                .outChoice(new Chapters.Choice("quill", "The edge is very near, and very far down.",
                        "Step off the cliff.", "Climb back down and ask her to meet you at the bottom.")
                        .after(
                                new String[]{"* The wind takes you. For one sweet endless instant you fall, and then something enormous and white closes beneath you like a cup. You are laughing before you know it. She is, too, in her crane way, with a sound like a rusty gate.", "@Quill| Well. That was easier than your mother. She screamed."},
                                new String[]{"* You climb down. It takes seven hours. At the bottom, she is waiting, perched on a rock, looking smug.", "@Quill| Sensible. Your mother would have jumped. I will have to teach you to jump."}))
                .done((c, st) -> Taming.legend(c, "Quill", "Storm Crane", Math.max(20, c.lvl + 3)))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("hunters", "The Hunters of the Burned Wood").years(2)
                .setup((c, st) -> Chapters.spawn(c, st, "brodin"))
                .intro(
                        "* 'Don't bring a lantern,' Quill said. 'He remembers fire and nothing else.'",
                        "* She told you the rest on the way down. Ember was the oldest and strongest of the three. Hunters came for him years ago, with torches, and burned his wood. He has not been right since. He lives in the cinders now, and he kills whatever comes close. The villages call him the Cinder Boar. They have put a price on his head.",
                        "* One man has taken the price. His name is Captain Brodin. He is in the Burned Wood now, with twenty hired spears and a great many torches.",
                        "* You are not sure what you will do when you find Ember. You are sure what you will do when you find Brodin.")
                .defeat("brodin", "Stop Captain Brodin, who is hunting Old Ember in the Burned Wood.")
                .outro(
                        "?dead.brodin|* It is quick and ugly. The hired spears run when their captain falls, and the torches go out one by one in the wet ash.",
                        "?!dead.brodin|* He yields on his knees, and his men stand back with their torches lowered. You take the torches and throw them in the river.",
                        "?!dead.brodin|@Brodin| The price was fair. He killed eleven people.",
                        "?!dead.brodin|@You| He was afraid. So were they.",
                        "?!dead.brodin|* He says nothing. He and his men leave in the morning.",
                        "* The wood is quiet. It is a quiet that goes on a long time. Then, from deep inside the burned trees, a single enormous sound, low as an earthquake, rolls toward you across the ash.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("ember", "Old Ember")
                .intro(
                        "* The Burned Wood, {@wood}, is a place where the trees stand like black bones. The ground is soft with cinders. Every footstep sends up a small cloud.",
                        "* You feel him before you see him: a heat on the back of your neck, and a sense of being watched by something that has been watching a long time.")
                .at("wood", "Enter the Burned Wood at {@wood}, alone and without fire.")
                .outro(
                        "* He steps out of the ash like a mountain standing up. He is a boar as large as a house, black as the wood, with tusks like curved swords and a hide cracked with old burns. Little fires flicker deep in the cracks.",
                        "* He lowers his head. His eyes are red, and wild, and very, very tired.",
                        "@Ember| FIRE. YOU. SMELL. OF. FIRE.",
                        "* You do not. You smell of ash and mud. But it is clear that to him everything does.")
                .outChoice(new Chapters.Choice("ember", "Old Ember stands between you and the burned trees, and does not know you.",
                        "Hum your mother's tune, the three slow notes.", "Stand still with your hands open.", "Draw your weapon.")
                        .after(
                                new String[]{"* You hum. Your voice shakes. The three slow notes, and the same three, higher. The boar goes quite still. Then, very slowly, he lowers his enormous head until the tip of one tusk touches the ground.", "@Ember| ...Isha?", "@You| Her daughter.", "* He makes a sound like a mountain sighing. It goes on for a long time."},
                                new String[]{"* You do not move. You do not speak. You hold out your empty hands. The boar circles you once, twice, close enough that his heat blisters the leaves behind you. On the third pass he stops, and presses his huge snout into your open palm.", "@Ember| You smell of her, under the smoke."},
                                new String[]{"* He charges. You meet him with your weapon, and you are thrown into a tree, and the world goes black.", "* When you wake, he is lying beside you in the ash, huge and warm, licking the cut on your arm with a tongue like sandpaper. He was never trying to kill you. He was trying to find out if you would run.", "@Ember| You stood. Isha stood too."}))
                .done((c, st) -> Taming.legend(c, "Old Ember", "Cinder Boar", Math.max(30, c.lvl + 3)))
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("sanctuary", "The Place for the Wild Ones").years(3).major(3)
                .intro(
                        "* That night, in the burned wood, you sleep between a boar and a tortoise and a crane. You dream.",
                        "* You dream of a woman with your chin, in a wood that is on fire. She is not running. She is walking between the flames, humming, with a bone whistle in her hand, and a great black shape at her side. The shape is crying.",
                        "* She turns, and she looks straight at you, though you are not there.",
                        "@Isha| Tell my child I never left because I wanted to. Tell my child it was the only way to get him out. Tell my child the farm was the best thing I ever did.",
                        "* You wake with ash on your face. Ember is looking at you. His eyes are no longer red.",
                        "@Ember| She is not coming back. She was burned. She got me out, and she did not get out. I carried her. I put her in Moss's lake. I did not know how to tell you.",
                        "* Moss, behind you, rolls over in the cinders, with a long sigh.",
                        "@Moss| It is a good lake. She is not cold.")
                .wealth(5000, "Build a sanctuary for the wild ones: gather 5,000 spirit stones.")
                .outro(
                        "* The sanctuary goes up at the north end of Nobody's Lake, a small green place with a fence made of fallen trees. There is a hut for you, a shed for the injured, and a big flat rock where a tortoise can sleep.",
                        "* Hunters have heard of it. A few come out of curiosity. Most go away astonished. A cinder boar is eating apples from a child's hand, and a crane the size of a horse is giving flying lessons to a goose.",
                        "* At dusk you take the bone whistle out of your pocket for the first time. You blow it, just once. Three slow notes, and the same three, higher.",
                        "* From the lake, very faintly, something answers.")
                .outChoice(new Chapters.Choice("calling", "The wild ones are safe. What will you be to them?",
                        "A tamer, with all three always at your side.", "A guardian, who lets them come and go as they please.", "A voice between the wild and the towns, with students of your own.")
                        .after(
                                new String[]{"* They go everywhere with you. It is loud, and slow, and wonderful, and no inn will take you."},
                                new String[]{"* You open the gate each morning, and each evening, they come back. That is the thing no one tells you about letting them go."},
                                new String[]{"* Your first student is a girl who sleeps in the barn. Your second is a man who has been bitten by every animal he's tried to feed. You are very patient with both."}))
                .pay(5000, 0, 0, 2, 0, 1);

        d.epilogue(
                "* Your mother was the Voice of the Wild. You are, you are told, something else. The wild does not have a word for it yet.",
                "?calling=1|* You and the three walk the roads, and the children of every village run out to look. They are not afraid. A cinder boar who loves apples will do that to a village.",
                "?calling=2|* The gate stays open. The sanctuary has forty residents now, and a waiting list. Nobody has ever been turned away.",
                "?calling=3|* The School of the Open Hand teaches forty students every year how to listen. They say the first rule is to sit still and wait. They say it works better than anything.",
                "?ember=1|* Ember still hums, sometimes, when he thinks nobody can hear. Three slow notes, and the same three, higher.",
                "?ember=3|* Ember has a scar on his tusk, from the night he threw you into a tree. He shows it to visitors with great pride.",
                "?!dead.brodin|* Captain Brodin sends a package every spring: a sack of apples and a note that says nothing. You think it is an apology.",
                "* On the night of the autumn storms, you sit by Nobody's Lake with a bone whistle in your hand, and you tell the water about your day. The lake keeps its own counsel. But it is not cold.");
    }
}
