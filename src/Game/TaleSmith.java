package Game;

/** The Blacksmith's Child (see "Story Plan.md", revamped 2026-10-19 after "Start Stories Seasons Plan.md" section 3.2). Season 1, "Harl and Child": the player's father, Harl, died at his anvil and the player does
 *  not know how. His lessons come back as the player works the forge. The story no longer counts 215 items: it asks for a handful of named pieces at a time (hinges for Marl's gate, a knife, Pia's tongs), each with
 *  a small scene when it is handed over, and the whole season can be finished in about eight years. Pia (who is family, not a romance; she is fifteen when she comes) was once Number Three in Quartermaster Voss's
 *  forge, where the apprentices had no names; Voss comes into the shop as a polite customer long before the player knows him; Tam is the brother of the boy who carried the spearhead at Greywater.
 *  A war breaks out between the Northern League and the Marsh Dukes and both order arms (flag side: 1 the League, 2 the Dukes, 3 both in secret, 4 neither). Behind the old forge wall is Harl's unfinished sword, Evenfall, and
 *  a letter: he refused to make 'weeping blades' for Voss, and Voss's men killed him. Choices: the side, the stand taken after the battle (stand), and what to do with Voss (decided in the fight). */
final class TaleSmith {
    private TaleSmith() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void register() {
        Chapters.Def d = Chapters.def("smith");
        d.season(1, "Harl and Child");
        d.flags("craft0");
        d.cast("pia", "Pia", "Fenn").she().keep().trait(Character.Traits.Blacksmith).side("Good").lvl(0).age(15).at("here").rel(3);
        d.cast("marl", "Marl", "Quenby").opposite().keep().trait(Character.Traits.Gatherer).side("Good").lvl(0).age(34).at("here").rel(1).free();
        d.cast("voss", "Quartermaster", "Voss").he().trait(Character.Traits.Merchant).side("Evil").lvl(7).pot(10).age(49).at("here").rel(-3);
        d.cast("tam", "Tam", "Orrel").he().keep().trait(Character.Traits.Blacksmith).side("Good").lvl(0).age(14).at("here").rel(2);

        d.chapter("cold", "A Cold Forge")
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The forge has been cold for eleven days, which is the longest it has been cold since before you were born. The coals have gone grey and tidy. A grey, tidy forge is a wrong thing to look at: it is a sentence somebody stopped in the middle.",
                        "* Your father's hammer hangs on its hook by the door. The handle is worn bright in one place, in the exact shape of a hand twice the size of yours. You have walked past it eleven times a day and have never once touched it. As far as you can tell, you have not touched anything else either.",
                        "* On the step sits a farmer with a broken hoe across his knees. He arrived at sunrise. He has not knocked or coughed or spoken, in the manner of a man who has a great deal of time and a hoe.",
                        "@Farmer| Harl would have had that done in ten minutes.",
                        "@You| Harl isn't here.",
                        "@Farmer| No. But the hoe is. And so's the sun.",
                        "* You take the hoe, because there is no good way not to. The fire needs three tries and a lot of bad language. When you finish, the repair is not a good one: it is the work of someone who watched it done a thousand times from the corner of the room and never once looked.",
                        "@Farmer| That'll do. That'll do fine. Come by for your coin when you've got the knack.",
                        "* He goes off down the street whistling, the hoe over his shoulder, as though nothing of consequence has happened. In the forge, the fire you made ticks and settles. It is the first sound the place has made in eleven days.",
                        "* By evening you have two more jobs on the bench, because in a town this size a lit forge is a kind of announcement. Marl Quenby wants a pair of hinges for a gate that has been screaming since the autumn. The miller's boy wants a knife and will not say what for.")
                .made(3, "Take in the work: make three things, hinges for Marl Quenby's gate and a knife for the miller's boy among them.")
                .outro(
                        "* Marl inspects the hinges with the gravity of a surgeon, swings the gate twice, listens, and announces that it does not squeak. This is the highest praise a gate can get. Marl pays in eggs, and stays a minute longer than the eggs require.",
                        "* The miller's boy is eleven or so, and he holds out his coin as if it were someone else's. You make him a good knife with a plain handle, not the fancy one he kept looking at. You do not ask what it is for. Some questions you can feel the answer to from across the room.",
                        "* On the third night you are drawing out a bar of iron and it goes dull under the hammer. You frown, and strike again, and it rings wrong. And you remember a small yellow room that smelled of oil, and a very large hand folded round yours.",
                        "@Father| Don't hit the iron. Listen to it. It's already telling you where it wants to go. You only have to be quiet enough to hear.",
                        "* You are nine. Your hands are so small that his thumb covers both of them, and the hammer is too heavy, and he takes the whole of it without a word, which is how he took everything.",
                        "* You open your eyes. The bar glows. You listen, and this time you hear it: a tiny, steady ticking, like a clock in a very small house. You strike where it points, and the iron sings.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("pia", "The Girl at the Door").gap(14)
                .setup((c, st) -> Chapters.spawn(c, st, "pia"))
                .intro(
                        "* The knock comes after midnight, which is not when anyone knocks at a smithy unless something is on fire. Nothing is on fire. On the step stands a girl of fifteen with a bundle and a split lip. She has the look of someone who rehearsed a speech all the way up the hill, and decided on the way to say only the last line.",
                        "@Pia| My name's Pia. I can sweep, and I can carry anything, and I don't eat much. I heard the smith's child was on their own. I'm on my own. I thought we might as well be on our own together.",
                        "* You open your mouth to say no. You are twenty. You have repaired one hoe. You have a recurring dream in which you stand naked in front of a bellows. You are in no position to look after anybody.",
                        "@Pia| I'll sleep in the coal shed. I don't mind the dark. I count things when it's dark, so it isn't really.",
                        "@You| What do you count?",
                        "@Pia| Everything. Last night it was a hundred and forty-one steps from the bridge to your door. Seven of them are loose.")
                .meet("pia", "Find Pia, who went to sleep in the coal shed.")
                .outro(
                        "* She is in the coal shed, black to the elbows, sweeping soot into a pile larger than the soot she started with. There is something odd about the way she works, and it takes you a minute to see what: she does all of it as though someone were watching, and might at any moment take it away.",
                        "@Pia| I'm doing it wrong, aren't I.",
                        "@You| You are. But you're doing it on purpose, which is something.",
                        "* She laughs. It is a short, bright, startled laugh that surprises you both. It is the first the forge has heard since your father died, and it goes up the chimney and out into the night as though it had been waiting there.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("trade", "A Smith's Name").gap(12)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* Pia learns faster than you did, and does not mind saying so. By the second month she has opinions about your bellows and a hammer of her own. The town has worked out that when the forge is lit there will be two people in it, and one of them will be talking.",
                        "* A man in a grey coat comes in on a Tuesday and buys a hinge. He is polite in the way that costs nothing. He pays, weighs the hinge in his palm, and asks, as if it had just occurred to him, whether the old smith is in.",
                        "@You| He died in the spring.",
                        "@Grey coat| I'm sorry to hear it. He was an honest man. They're expensive, in my line of work. Good day.",
                        "* Pia watches him to the corner. She has gone still, the broom held crosswise in front of her like something that might help. When he is out of sight she goes on sweeping, and she sweeps the same board for a quarter of an hour.",
                        "* That evening she asks for something, for the first time. She wants a pair of tongs. Not to borrow: to make. 'Everything in the shop has somebody else's mark on it,' she says. 'I'd like one thing that hasn't.'")
                .made(4, "Take the work that comes: make four things, one of them the tongs Pia asked for.")
                .outro(
                        "* The tongs are ugly. Pia says so first, before you can. The jaws do not quite meet and one handle is longer than the other. She has hammered three dots into the shoulder with a nail and a great deal of concentration, and she holds them up to the light as though they were a crown.",
                        "@Pia| Three. That's my mark. It isn't a letter. I haven't got a letter yet.",
                        "@You| Why three?",
                        "@Pia| It's a number I had.",
                        "* She hangs the tongs on the rack at the far end, where nobody will use them, and goes back to the broom. You know better than to ask.",
                        "* That night you are about to quench a knife and your hand stops by itself. You hear him again. This time he is tired, and twelve years back, standing at the quench barrel with his sleeves rolled to the elbow.",
                        "@Father| A blade is made twice. Once in the fire, once in the water. If you rush the water, you lose the fire.",
                        "* You were a child, impatient, with somewhere to be. There was a knock at the front door. He looked up. A man in a grey coat stood in the street with a clerk behind him and a covered cart.",
                        "@Father| No.",
                        "* He said it three times on three different days, and each time the man went away, politely, and the third time you saw your father's hands shake. 'Never forge what you can't carry,' he told the barrel afterwards. 'Remember that.' You forgot, for twelve years. You remember now.",
                        "* You think of a man who bought a hinge on Tuesday and asked so pleasantly after the old smith. You lower the blade into the water, slowly.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("orders", "Two Letters").gap(16).major(1)
                .intro(
                        "* The war arrives by post. Two letters on the same morning, from opposite ends of the province, in the same fat, confident hand of men who have never had to wait for an answer.",
                        "* The first is from the Northern League: two hundred spearheads, a hundred knives, a good price, and a promise of protection. The second is from the Marsh Dukes: the same, in different handwriting. Each says the other side started it. You suspect they are both right.",
                        "@Pia| We could do both. Nobody would know. Do you know how much that is?",
                        "@You| Yes.",
                        "@Pia| Or neither. I'd understand. Or the one we like better. You decide. It's your name on the door.")
                .choice(new Chapters.Choice("side", "Two armies want your spears.",
                        "Arm the Northern League.", "Arm the Marsh Dukes.", "Arm both, in secret.", "Arm neither.")
                        .after(
                                new String[]{"* You write back: yes. You feel good about it for about a week."},
                                new String[]{"* You write back: yes. The Dukes' clerk is courteous. It is a small comfort."},
                                new String[]{"* You write back to both. You mark the spearheads with a tiny number scratched into the socket, so you can tell whose is whose. It is a dishonest little system, and you are proud of how well it works."},
                                new String[]{"* You write back no, twice. Pia nods and says nothing. That night she leaves a bowl of soup on your anvil, which is the nicest thing anyone has done for you all year."}))
                .setup((c, st) -> mark(c, st))
                .made(6, "Fill what you can: make six pieces, for the war or for the town.")
                .outro(
                        "?side=4|* There is not much to fill. The orders go to other smiths. You make hinges and horseshoes, the same as ever, and watch the carts leave town heavy and come back light.",
                        "?!side=4|* It takes a month. Pia works the bellows. The spearheads come out of the quench with the same sound each time, a hiss like an argument being lost, and you stack them against the wall in rows of ten, like a thing you are trying not to count.",
                        "* On the last evening Pia sits on the end of the anvil with her chin on her fists. 'There was a forge I was at,' she says, out of nowhere, 'where we never saw what happened to anything.' Then she gets up and fetches the water.")
                .pay(2000, 0, 0, 0, 0, 0);

        d.chapter("wall", "The Wall Behind the Forge").gap(14)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The forge is too small. You have known it for years. In the spring you decide to knock out the back wall and build a second hearth.",
                        "* Pia swings the sledge. The plaster comes down in a white cloud, and behind it, in a gap between the stones that nobody has opened in a long time, lies a long shape wrapped in oilcloth.",
                        "* You say nothing. You carry it out into the light.")
                .made(3, "Study your father's notes: make three pieces to his designs.")
                .outro(
                        "* The oilcloth is dry. Inside is an unfinished sword, the blade rough-ground, no guard, no grip. A notebook. A folded letter with your name on the front. You read the letter first, standing up.",
                        "* 'My child. If you are reading this, I am dead, and I am sorry. I could not tell you while I lived. A man named Voss, quartermaster to the army, has asked me three times for something called a weeping blade: an edge that carries a slow poison, made for besieged towns, for wells and water. I will not make it.'",
                        "* 'I have started a sword he cannot use. I call it Evenfall. I never found the last of it. I think the answer is in the quench and the rhythm. If you finish it, it will not cut anyone who has not already raised a hand. Never forge what you can't carry. I could not carry it. Be better than me. I think you can. Father.'",
                        "* A memory comes, not of a lesson this time but of a man in a doorway, big hands open, tired, a little embarrassed. 'A blade remembers who hammered it,' he says. 'So be careful what you hammer into it.'",
                        "* When you look up, Pia is standing very straight with the sledge still in her hands, and she is the colour of the plaster. She puts the sledge down. She says the words one at a time, as though each of them had to be carried across a river.",
                        "@Pia| There were nine of us in the yard. We weren't given names. I was Three. We made them for a man in a grey coat and we were told they were for hunting wolves. A well came back to us one winter as a bill. Seven stopped coming to work. They said he'd gone home. I took a loaf and I ran, and a carter told me there was one smith in the north who had told the Quartermaster no.",
                        "@Pia| I didn't come here by luck. I came to say thank you to someone who was dead. I didn't know how to start.",
                        "* You sit down on the floor of the half-demolished forge. After a minute she sits beside you, and puts her shoulder against yours, and the two of you stay like that until the lamp burns out.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("greywater", "The Field at Greywater").gap(30)
                .intro(
                        "* The war comes to a ford called Greywater, thirty miles from your forge. Both armies meet there on a morning of fog. You do not go. But if the wind is right you can hear it from the forge roof, a long, low sound, like surf.")
                .task("WAIT", 4, "", "", "")
                .outro(
                        "?side=1|* The League holds the ford with the spears you made, and it is called a victory. Three hundred men are dead. A boy of sixteen is carried home with one of your spearheads in his hands. They say he held it all day.",
                        "?side=2|* The Dukes hold the ford with the pikes you made, and it is called a victory. Three hundred men are dead. An old man carries home a pike with your mark on the socket and sits in front of his house with it across his knees.",
                        "?side=3|* In the third hour a pikeman of the League and a pikeman of the Dukes meet spear to spear. Both bear your tiny number on the socket. They strike, and both heads snap at the same place, because you made them on the same day from the same ingot. You will never know which man fell.",
                        "?side=4|* You armed no one. Others did. Three hundred men are dead. You tell yourself it would have been worse, and Pia tells you it would not, and she is probably right.",
                        "* The news comes with the first frost. You read the casualty list twice, as if the second reading might change it.",
                        "* Nothing you have ever made has been used to kill. Now you do not know that any more.")
                .outChoice(new Chapters.Choice("stand", "The war is going to go on, and the orders are going to keep coming.",
                        "Stop making weapons for the war.", "Keep making them, but make them honestly and well.", "Make arms only for the town's own defenders.")
                        .after(
                                new String[]{"@Pia| Are you sure?", "@You| No.", "@Pia| Good. Neither am I. Let's do it."},
                                new String[]{"@Pia| You know that's what every smith says.", "@You| I know. I'll try to deserve it.", "* It is not an answer. It is a promise to keep asking."},
                                new String[]{"* A town needs walls and spears. A town does not need to start a war. You paint it on a board over the door, and the board is a little crooked, and you leave it crooked."}))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("tam", "The Boy With the Spearhead").gap(20)
                .setup((c, st) -> { Chapters.spawn(c, st, "tam"); mark(c, st); })
                .intro(
                        "* He is fourteen and he stands on the step with the stubbornness of someone who has been sent and refuses to be turned back. His clothes are good and have been mended by three different hands. He is holding something against his chest, wrapped in a sack.",
                        "?side=1|@Tam| My brother carried one of yours at Greywater. A League spear. He's the one they brought home. He held it all day. They said the socket never moved. I came to see who made a thing that doesn't move when everything else does.",
                        "?side=2|@Tam| My grandfather carried one of yours at Greywater. A Duke's pike. He sits with it across his knees and won't give it up. He says it never moved. I came to see who made a thing that doesn't move when everything else does.",
                        "?side=3|@Tam| My father had one of yours at Greywater, and so did the man who killed him. I've got both heads. They broke at exactly the same place. Nobody told me what to think about that, so I brought them.",
                        "?side=4|@Tam| I need a hinge. For my mother's gate. They said you're the only smith in the north who wouldn't take the order, and I wanted to see if that was true, and if it was, I wanted a hinge from you.",
                        "* He unwraps the sack on the anvil. It is exactly what he said it was, and it is worse, because it is so ordinary.",
                        "@Pia| What's your name?",
                        "@Tam| Tam.",
                        "@Pia| You can stay for the night, Tam. Nobody here has a number.")
                .made(3, "Show Tam what the forge is for: make three pieces with him at the bellows.")
                .outro(
                        "* He turns out to be good at the bellows and bad at everything else, and he asks the kind of questions that make Pia put her hammer down and think. 'Why does the iron change colour?' 'Because it's hot.' 'Why does hot change the colour?' You catch each other's eye across the fire and neither of you has an answer.",
                        "* At the end of the third day he lays the spearhead you made for his family on the bench beside the three new pieces and looks at them until the lamp gutters, and then he asks if he can stay. Pia says that it is not her forge. You say that he should ask the person whose forge it is. He says that he did.",
                        "* You hear it again that night, the ticking, steadier now. For the first time, it has a second sound under it, like someone answering from the next room.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("voss", "The Quartermaster").gap(20).major(2)
                .setup((c, st) -> Chapters.spawn(c, st, "voss"))
                .intro(
                        "* The man in the grey coat is waiting in your shop when you come back from the market. Pia stands behind the counter, white as flour. Tam is in the corner with a hammer held in both hands like a priest's staff. Nobody has asked anyone to put anything down.",
                        "@Voss| You must be Harl's child. I thought I recognised the chin. I am Voss, quartermaster to the armies of the League and, when it suits me, of the Dukes. I came last spring to ask after your father. You were kind enough to tell me.",
                        "@Voss| Number Three. I did wonder where you went. You've done well.",
                        "@Pia| My name is Pia.",
                        "* She says it as if setting something heavy on a table. He looks at her for a moment with a slight, interested frown, like a man who has found an error in a column he thought he had balanced.",
                        "@Voss| Your father and I had a disagreement. A matter of an order. I never held it against him. I've come, I think, to settle accounts.",
                        "* He puts a ring on the counter. It is a seal ring, grey, with a small design you know from the foot of a letter you have read forty times.",
                        "@Voss| Now. I wonder whether the child is as stubborn as the father.",
                        "* He does not draw a weapon. He does not need to. If you want to answer him, type 'attack' while you stand in the same place.")
                .defeat("voss", "Face Quartermaster Voss, and win.")
                .outro(
                        "?dead.voss|* He falls across your counter, and the ring rolls across the floor and stops against Pia's foot. She picks it up and puts it into your hand. For once she is not counting anything.",
                        "?dead.voss|@Pia| That's the last of him.",
                        "?dead.voss|* There is nothing in the world that feels like winning. There is only the quiet afterwards, and the smell of the forge, and in the corner a boy of fourteen who has just learned how little a hammer is for.",
                        "?!dead.voss|* He is on the floor with a hand over his ribs and your blade at his throat. You leave it where it is. Instead you take the ring from the counter and put it in the fire, and the three of you watch it go soft and dull and drop.",
                        "?!dead.voss|@You| You'll leave this province. You'll never order another blade. If I hear of one, I will find you.",
                        "?!dead.voss|* He goes without looking back. Pia watches from the door until he is out of sight, and then her shoulders come down, for the first time since she was twelve.",
                        "* Somewhere in the middle of it, you realise, the ticking stopped. It starts again now, uncertainly, like a sleeper's breath.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("evenfall", "Evenfall").gap(14)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The sword is on your bench. You have looked at it every day for years. You have read his notebook until it came apart at the folds. You have burned yourself thirty different ways trying to finish the blade, and it has not wanted to be finished by cleverness.",
                        "* It wants, you think, to be finished by someone who has done the work. So you do the work. You make what the forge asks for, a plough-share, a ladle, a latch, a pair of shears, and you make it as though your life depended on the shape of the handle. You let Pia and Tam argue about how.")
                .made(5, "Finish your father's sword: make five pieces to master the work, and then the sword itself.")
                .outro(
                        "* On the fifth night you take the sword out of its oilcloth. It is the first time you have really looked at it. The blade is long and plain and lovely. You know now where the quench goes. You know the rhythm: slow, slow, quick, slow. It is the rhythm of a heart.",
                        "* You grind it. You temper it. You fit the guard and the grip. Tam holds the lantern; Pia stands at the barrel with her hands cupped round a bucket. When it is time, you put the blade into the water very gently, and the water does not hiss. It simply goes quiet.",
                        "* You lift it out. It shines a little, in a way no blade should in lamplight: not the shine of polish but of a thing that has been waited for.",
                        "@Pia| Try it.",
                        "* You test it on a loaf of bread, and it cuts the bread. You hold the point to your own palm, and nothing happens. The edge does not know you. You are no enemy of it. You turn it on a practice dummy with a face painted on, and the edge goes dull as a spoon, as if it will not cut a thing that cannot raise a hand.",
                        "@Pia| Wait. There's a place under the guard. Where nobody looks. Can I?",
                        "* You let her. She takes the nail and the smallest hammer, and puts her three dots into the steel, where they will be covered by the grip. Then she takes the lantern from Tam and holds it up, and the three of you stand in the middle of the forge with a sword on the bench and nothing to say.",
                        "* The ticking has stopped. It does not need to go on. It has said what it was for.")
                .pay(10000, 0, 0, 2, 3, 2);

        d.interlude(1, "The Sign Over the Door",
                "* The sign over the forge still says {last} and Child. Your father painted it the week you were born. You have repainted it eleven times and never changed a letter.",
                "?stand=1|* You never made another weapon for the war. The war ended, as wars do, and the ones who fought it said it was a draw. You did not argue.",
                "?stand=2|* You kept making blades, and made them well. There are people alive today who would not be if the spears had been worse, and people dead who might not be. You carry both.",
                "?stand=3|* The town's walls are your best work. The spears stand in a rack by the gate. Nobody has ever needed them, which was the whole idea.",
                "?side=3|* You still keep the little numbered socket you scratched on the spearheads. It hangs on a nail by the door.",
                "?dead.voss|* The grey ring is in a drawer under the bellows. You never take it out.",
                "?!dead.voss|* Voss was never seen again. Every year, on the day of the fight, you check the road. You don't expect him. You check anyway.",
                "* Evenfall hangs on the wall over the forge. Nobody is allowed to touch it. Children are allowed to look, and they always ask what it is for. You tell them that it is for nothing, and they look disappointed, and Tam, who is twenty now and has to be told twice to stop quoting you, adds that this is the best thing about it.",
                "* The sword has, however, begun to get a reputation. A sword that will not cut the innocent is the kind of thing that travels. This spring three separate people have come to the door to ask if it is true, and one of them was carrying a very good coat.");
        TaleSmithSeasons.add(d);
    }
}
