package Game;

/** The Crank of Gearwheel Lane (2026-10-20; new start, three seasons). The human inherits Uncle Barnaby Crank's workshop in Gearwheel Lane, in the city of Tallowmere, where nothing built has ever worked the way it was meant.
 *  Season 1, "Nothing Works as Meant" (a comedy of inventions, card A): the self-kneading dough machine for Mrs. Orlop, the clockwork duck Biscuit, the patent clerk Lysandra Mott (the other gender than the human's),
 *  the Conservatory of Right Arrays, the first thing that works. Choices: duck (what Biscuit is given), offer (what is done with the lifting engine), love. Flags: duck, offer, love.
 *  Seasons 2 and 3 are in TaleInventorSeasons. */
final class TaleInventor {
    private TaleInventor() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void register() {
        Chapters.Def d = Chapters.def("inventor");
        d.season(1, "Nothing Works as Meant");
        d.flags("craft0", "duck", "offer", "love");
        d.place("lane", "wildnear").place("bridge", "wild").place("conserv", "far");
        d.cast("orlop", "Mrs.", "Orlop").she().keep().trait(Character.Traits.Friendly).side("Good").lvl(2).age(60).at("place:lane").rel(3);
        d.cast("fitch", "Fitch", "Larkin").he().keep().trait(Character.Traits.Innovative).side("Good").lvl(2).age(20).at("place:lane").rel(3);
        d.cast("mott", "Lysandra", "Mott").opposite("Lysander").keep().trait(Character.Traits.Scribe).side("Neutral").lvl(5).age(30).at("here").rel(0);
        d.cast("dimmock", "Master", "Dimmock").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(10).pot(6).age(60).at("place:conserv").rel(-1);

        d.chapter("lane", "Gearwheel Lane")
                .setup((c, st) -> Chapters.spawn(c, st, "orlop"))
                .intro(
                        "* Uncle Barnaby Crank left you a workshop, a debt, and a note that said: Sorry about the smell. The workshop is at the bottom of Gearwheel Lane in the city of Tallowmere, next door to a bakery. It has a crooked chimney and a door that opens outward, because the first time it opened inward it took the porch with it.",
                        "* Everyone in the lane calls the workshop the Crank, and everyone in the lane calls you the new crank. They say it with affection. They say it from a safe distance, with their hands over their ears.")
                .at("lane", "Go to the workshop in Gearwheel Lane, {@lane}.")
                .outro(
                        "* The workshop smells of hot oil, singed hair and mild disappointment. There are forty-one half-finished machines on the benches. A brass kettle on legs is walking slowly in circles round the stove. A wind-up umbrella has got itself stuck in the rafters and is opening and closing in a hopeless fashion.",
                        "@Orlop| Oh, you've come. You're the image of him. He was a lovely man. He set fire to my oven three times.",
                        "* Mrs. Orlop is sixty, floury to the elbow, with a bun of grey hair that has a pencil stuck through it and an air of having forgiven everybody in advance. She puts a hot loaf in your hands and tells you the rule of the lane. You may build what you like. You may blow up what you like. But not before seven in the morning or after ten at night, and never on Sundays.",
                        "@Orlop| And if it's going to be loud, put a sign on the door. Your uncle put a sign. It said BRACE. I thought that was very considerate.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("dough", "The Self-Kneading Dough Machine").gap(8)
                .setup(TaleInventor::mark)
                .intro(
                        "* Mrs. Orlop wants a machine to knead dough. She asks politely, with a plate of buns. She says her wrists are not what they were. She says she would be most obliged if it did not talk, explode, or escape, as the last one did, in 1869, and was found three streets away working as a pastry chef.")
                .made(8, "Build the kneader: make eight contraptions.")
                .outro(
                        "* You build eight machines before you build the right one. The first kneads too hard. The second kneads too soft. The third refuses to knead at all, and sits on the bench, humming, in a sulk. The fourth, a pretty thing with brass paddles and a small array in the base, kneads a perfect dough, and then, with a sigh, keeps kneading.",
                        "* By the end of the afternoon the dough is three feet high. It has filled the bakery. It has crept up the stairs. It is gently and patiently kneading itself against the ceiling, and a baker's boy is hanging from a beam, shouting for his mother. Mrs. Orlop surveys it with her hands on her hips.",
                        "@Orlop| It's a very good dough. I've never seen one so well worked. Is there an off switch?",
                        "@You| There was.",
                        "@Orlop| Well. I suppose we'll have to make a great many loaves.",
                        "* The whole lane eats bread for a week. A famous baker from the next district comes to look at the machine, and goes away shaking his head, and orders two. Nobody has ever been so pleased with a catastrophe.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("duck", "Biscuit").gap(16)
                .intro(
                        "* It starts as a spare-parts project, on a wet Thursday, to take your mind off the dough. You have a drawer of gears, a biscuit tin, a pair of tin cymbals and a small array that boils water. You put them together without a plan. At eleven at night there is a duck on the bench. It is brass, the size of a loaf, and it has one blue glass eye and one brown.")
                .wait(8)
                .outro(
                        "* It does not move. You wind it. You tap it. You say, 'Hello,' in a foolish voice. Then, very slowly, as if remembering something it had once been told, it turns its head and looks at you with both its eyes. It makes a small clicking noise, rather like a throat being cleared, and waddles across the bench and settles on the open page of your uncle's notebook.",
                        "@Fitch| Is it alive?",
                        "@You| It's a duck.",
                        "@Fitch| It's looking at the notebook.",
                        "* It is. It is pecking, softly, at a drawing on the page, a plan for a thing called a Lifting Engine, with the word MAYBE written beside it three times in your uncle's hand. You put the duck on your shoulder. It sits there, peering about, as though it had always lived in this room.")
                .outChoice(new Chapters.Choice("duck", "You have built a duck, and it has opinions. You can still change what it is.",
                        "Give it a voice: a small brass reed in the throat.", "Leave it silent. Some things are better with a mind of their own.", "Sell it to the toymaker down the lane for a very good price.")
                        .after(
                                new String[]{"* You fit a brass reed in its throat. It takes the whole night. In the morning the duck opens its beak. It says, in a clear small voice like a spoon in a cup, 'Good morning'. It says it eleven times, with minor variations, and spends the rest of the day saying 'Interesting' to the furniture.", "@Fitch| I think it's the best thing you've ever made. I think it might also be the worst."},
                                new String[]{"* You leave it silent. It takes to the workshop at once, and follows you from bench to bench, and sits on whichever machine is about to go wrong. It has never been wrong yet. Mrs. Orlop says it has the manner of a retired judge.", "@Orlop| It's a kind of companionship. You don't have to say anything."},
                                new String[]{"* You sell it. The toymaker pays twenty silver and takes it away in a box. By the following morning it is back on your bench with a note pinned to its wing: Returned. Does not like the shop. Refund enclosed. The duck settles on your shoulder as if nothing had happened. It is the nicest thing anyone has ever done to you by not leaving.", "@Fitch| It's a good duck."}))
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("noise", "The Petition").gap(16)
                .intro(
                        "* The petition arrives on a Saturday, delivered by a very small boy in a very large hat. It has forty-three signatures. It complains of noise, smoke, sparks, the smell of burnt hair, a continual ticking at night, and a duck. It asks the Crank to desist. It asks very nicely. At the bottom someone has written in a different ink: P.S. Please do not desist. It is the only exciting thing here.")
                .friends(3, "Win the lane: make three friends.")
                .outro(
                        "* You do not desist. Instead you go from door to door with a box of small gifts you have built for the purpose. There is a self-trimming candle, a boot that cleans itself, and a toasting fork that tells you when the bread is done. They do not all work. Some of them work in unexpected ways. A tiny spinning top given to the widow at number nine plays a lullaby so beautiful that her cat is found weeping in the larder.",
                        "* By the evening forty petitioners have been to the workshop. They stand about in a ring, with their hands in their pockets, drinking tea out of whatever cups come to hand. The very small boy in the very large hat tells the duck his name. The duck says nothing. It bows.",
                        "@Orlop| There. That's the lane. It's not much, but it's ours, and we'll put up with a great deal.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("clerk", "The Clerk of Patents").gap(24)
                .setup((c, st) -> { Chapters.spawn(c, st, "mott"); Chapters.spawn(c, st, "dimmock"); Chapters.spawn(c, st, "fitch"); })
                .intro(
                        "* The Clerk of Patents calls on a damp Tuesday in an umbrella and a long grey coat. The umbrella is of the old pattern, black, with a hooked handle. The coat is buttoned to the chin. The clerk has a briefcase, a notebook, a pair of round spectacles, and the sort of narrow, tidy face that has never in its life been late for an appointment.")
                .meet("mott", "Receive the Clerk of Patents at the workshop.")
                .outro(
                        "@Mott| Lysandra Mott, Clerk, Second Grade, the Conservatory of Right Arrays. I should like to say at once that this is a courtesy visit. The Conservatory has had a number of reports about your premises, and I am obliged to inspect. It is a formality. I do not expect to find anything.",
                        "* {mott.He} finds the duck. It is sitting on {mott.his} briefcase. {mott.He} looks at it. It looks at {mott.him}. {mott.He} writes something in the notebook, and crosses it out, and writes something else. {mott.He} asks, in a slightly strangled voice, whether it is registered.",
                        "@You| It's a duck.",
                        "@Mott| That's a category. Is it an Automaton or an Array-Assisted Domestic Animal? I'm afraid the forms are different.",
                        "* {mott.He} stays two hours. {mott.He} inspects forty-one machines, writes eleven citations, and is shown the kettle that walks in circles. {mott.He} laughs at it, unwillingly, one short surprised bark, and then looks appalled at having done so. At the door, {mott.he} turns, and says that {mott.he} has seldom seen so many violations in one room, and that {mott.he} does not expect to find that they are as bad as {mott.he} had been led to fear.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("conserv", "The Conservatory of Right Arrays").gap(24)
                .intro(
                        "* The Conservatory occupies a grey marble building in the middle of Tallowmere, with a portico, nine columns and a clock that is never wrong. It is the guardian of the Right Way of Making: the only licensed method of building arrays, taught in a seven-year course, in a set of forty-one volumes, by a faculty of grey men. It regards inventors as it regards weeds.")
                .meet("dimmock", "Answer the summons of Master Dimmock at the Conservatory.")
                .outro(
                        "* Master Dimmock is sixty, tall, beautifully dressed, with a voice like a church organ and a profile on a medal. He receives you in a long panelled room with a table the length of a boat and a portrait of himself, on the wall behind him, looking slightly younger and slightly more pleased. He invites you to sit. He does not offer tea.",
                        "@Dimmock| I understand that you build. I understand that you build without a licence, a diploma or a mentor. I understand that you have a pet duck. I want to say that I do not regard this as a crime. I regard it as a condition. It is a thing that happens to certain young people. It passes.",
                        "@You| Does it?",
                        "@Dimmock| Almost always. The Conservatory has a very good rehabilitation programme. Seven years. Forty-one volumes. You would be amazed what a young person can learn when there is no choice.",
                        "* You thank him. You decline. He smiles, with every one of his teeth, and rings a small silver bell. A clerk appears with a cup of tea. The cup has a crack in it. You are not sure whether it is an accident.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("teapot", "The Array in the Teapot").gap(24)
                .setup(TaleInventor::mark)
                .intro(
                        "* The idea comes at three in the morning, as ideas do. You are lying awake in the loft with the duck on your feet, thinking about nothing, when it arrives whole. It is a tiny array, no larger than a thumbnail, that heats water by a fraction of a degree for each turn of a wheel. You are out of bed before you have finished the thought. You do not stop till dawn.")
                .made(10, "Chase the idea: build ten prototypes.")
                .outro(
                        "* The first prototype boils a teacup in four seconds, then the table, then the ceiling. The second goes off like a firework. The third makes a high, sweet, ringing note and sets every glass in the workshop vibrating until it shatters. The tenth, which you build at sunrise, with burnt eyebrows and a quivering hand, boils a pot of water in exactly three seconds, quietly, politely, without exploding.",
                        "* You pour a cup of tea. It is perfect. You sit at the bench and drink it, with the duck on your knee, and weep a little, in the way of a person who has been wrong forty-one times and right once. Fitch comes in at nine and finds you asleep with your head on the table and a cup in your hand.",
                        "@Fitch| Did it work?",
                        "@You| Yes.",
                        "@Fitch| Everything?",
                        "@You| Just the one thing.",
                        "@Fitch| That's usually the way, isn't it? One thing. And then you find out what it was for.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("inspect", "Inspection Day").gap(24)
                .intro(
                        "* The Conservatory sends a formal letter of inspection. It will be conducted by a panel of five, led by Clerk Mott, on the first Friday of the month. It will be a full examination of all premises, devices and animals. The letter is signed by Master Dimmock, with a flourish. At the bottom it says: A cheerful demeanour will be noted.")
                .wait(8)
                .outro(
                        "* You spend the week hiding things. By Thursday you have crammed eleven machines into the cellar, six into the loft, and a self-ironing board into the coal scuttle. You put the walking kettle out in the yard under a bucket. You lock the umbrella in a cupboard. You tell Biscuit to be a normal duck.",
                        "* The panel arrives at nine. It is led by Mott, in the long grey coat, with Dimmock's three assistants behind. They examine the benches. They examine the shelves. They examine the loft, where a self-ironing board escapes from the coal scuttle and chases a clerk down the stairs. The kettle gets out from under the bucket and walks into the room, calmly, with a cup on its lid, and comes to rest at Mott's feet.",
                        "@Mott| (after a very long pause, to the panel) It appears to be making tea.",
                        "* It is. It is making a perfect pot of Assam. Mott takes the cup. {mott.He} sips it. {mott.He} sets it down on the saucer without a sound. The three assistants stand there, in a row, like members of a jury who have just been shown the murder weapon and found it is a cake.",
                        "@Mott| I shall record that the premises are in order. I shall record that the kettle is an exemption. I shall record, if I may, that this is the best cup of tea I have had in this city.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("race", "The Great Lane Race").gap(24)
                .intro(
                        "* The race was Fitch's idea. It is to be held on the first Sunday in June, down the whole length of Gearwheel Lane, from the bakery to the canal. The entries are anything with wheels. The rules are that there are no rules. The prize is a golden spanner, made by the lamp-maker, and a barrel of Mrs. Orlop's best cider.")
                .wait(8)
                .outro(
                        "* By the day, there are eleven entries. A coffin on skates. A wheelbarrow with a sail. A tea-trolley with a boiler, driven by the vicar. The biggest crowd the lane has ever seen is packed along the pavements, cheering and eating buns, and Biscuit sits on the bakery roof wearing a tiny flag. You enter a four-wheeled cart with a small boiler array in the back and a handle in the front. You call it the Stately.",
                        "* It does not go well. The start-gun is a firework. The vicar's trolley takes off. The coffin is overtaken by a goat. You are in the lead for the first fifty yards, with the wind in your hair, laughing like a lunatic. Then the Stately's array, which you have set to Cautious, decides it has been Cautious long enough. It switches itself, without asking, to Furious.",
                        "* You cross the finish line at forty miles an hour, backwards, in a cloud of steam. You go through the canal fence. You come to rest, with a splash, in four feet of mud. The crowd is silent. Then, from somewhere, a single voice shouts, 'Again!'",
                        "@Fitch| (hauling you out) That was the best thing I've ever seen. I wanted you to know that. That was incredible.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("bridge", "The Lifting Engine").gap(24)
                .intro(
                        "* The Weaver's Bridge falls in on a Tuesday. It is a grey stone arch, three hundred years old, over the canal at the foot of the lane, and it falls without warning, in the middle of the morning, with a sound like a stack of plates. A cart is on it. A horse is on it. A baker's boy, who has stopped to tie his shoe, is on it. The whole thing goes into the water, and the baker's boy goes with it, and does not come up.")
                .at("bridge", "Run to the fallen bridge, {@bridge}.")
                .outro(
                        "* There is no time. The horse is screaming. The cart lies on its side across the stones. Under it, in the dark water, among broken arches and baulks of timber, there is a space where a small boy might be, alive, if the weight could be taken off. The Guard has sent for a crane. The crane is an hour away. You look at the duck, on your shoulder, and the duck looks at you.",
                        "@You| Fitch! The Lifting Engine!",
                        "@Fitch| It's a plan! It's only a drawing! It says MAYBE!",
                        "* You build it in eleven minutes, on the bank, with a plank, a barrel hoop, a spare wheel, and the little array you made in the teapot. It is ugly, and it works. It takes the cart on its brass arm and lifts it, with a long groan, a foot, two feet, three. Fitch goes into the water. He comes up with the baker's boy over his shoulder, coughing, alive.",
                        "* The crowd gasps, and then roars. The boy's mother runs down the bank with her apron over her face. Mrs. Orlop is crying. The horse is led out, trembling. Biscuit, on your shoulder, makes a very faint, very clear sound that you have never heard before: not a click, but something nearer a laugh.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("offer", "An Offer from Master Dimmock").gap(16)
                .intro(
                        "* He calls the next morning, in person, in a carriage with a coat of arms. He stands in the doorway of the workshop in his beautiful coat, with his gloves in one hand, and he looks at the Lifting Engine on the bench with an expression you cannot read. He says that the Conservatory has been following events with interest. He says it would like to make an offer.")
                .wait(4)
                .outChoice(new Chapters.Choice("offer", "The Conservatory wants the Lifting Engine, and is willing to pay.",
                        "Sell it to the Conservatory for a fortune, and walk away.", "Share it: give the plans to anyone who asks, free, and let the lane build its own.", "Keep it: refuse, and register it in your own name at the Patent Office.")
                        .after(
                                new String[]{"* You sell it. The Conservatory pays four thousand in silver and a seat on the Committee of Mechanical Methods. It is the most money you have ever seen. It is delivered in a box. It is very heavy. You feel, signing the paper, as if you had given away a coat and been paid for the buttons.", "@Fitch| It's a lot of money. It's a lot of money for something that isn't ours any more."},
                                new String[]{"* You give the plans away. You run off a hundred copies on Mrs. Orlop's bread-wrappers and hand them out on the quay. Within a week there are nineteen Lifting Engines in Tallowmere, and a team of boys in the canal with one, rebuilding the bridge. Dimmock is livid. The newspapers are charmed.", "@Orlop| That was a lovely thing to do. It's going to make you awfully unpopular."},
                                new String[]{"* You refuse. You write to the Patent Office the same afternoon, on a sheet of foolscap, in a very formal hand, with the duck's footprint in the corner as a seal. The reply arrives in three days. It says that the application is received, and that the examiner will be a Clerk of the Second Grade named Mott.", "@Dimmock| (in the doorway) You have made a mistake. I shall be sorry to see how it goes."}))
                .outro(
                        "* The duck, on the bench, has turned its head to watch him go. When the carriage has rattled out of the lane it looks round at you and gives, very distinctly, a click. It is the click that means: well, then. It is also, you are fairly sure, the click that means: look out.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("patent", "The Patent").gap(32)
                .intro(
                        "* The Patent Office is a low yellow building at the top of Quill Street with a great many steps. The queue goes round the corner. You wait six hours with a flask and a duck, among inventors of every degree: the mad, the prudent, the obsessed, a man with a bird cage on his head. When your number is called you carry the Lifting Engine up the stairs in your arms, in a blanket, like a baby.")
                .realm(1, "Qualify for the Register: reach Major Lvl 1.")
                .outro(
                        "* Mott receives you in a small bare office with a table, two chairs and a window onto a brick wall. {mott.He} examines the Engine for forty minutes, with a lens, a ruler and a notebook, not speaking. Then {mott.he} sets down the lens and says that, in {mott.his} professional opinion, it is the most original device {mott.he} has examined in eleven years.",
                        "@Mott| It is also, I am sorry to say, entirely illegal. It lacks an approved array. It lacks a certificate of provenance. It has been built by an unlicensed person, using unlicensed methods, from salvage. I have no discretion. I am obliged to reject it.",
                        "@You| I see.",
                        "@Mott| I should like to say that I had to read the regulations three times to be sure. I should also like to say that there is a clause in section nine, which has never been used, which permits a Clerk to waive the licensing requirement in the case of a device that has saved a life.",
                        "* {mott.He} stamps the form. The stamp is a very loud, final noise. It says: GRANTED. Under it, in {mott.his} handwriting, tiny, in the margin, is a note that makes your face go hot. It says: I have wanted to do that for eleven years.")
                .outChoice(new Chapters.Choice("love", "Lysandra Mott has just broken the rules for you, and has gone slightly pink.",
                        "Ask {mott.him} to dinner, and mean it.", "Send a thank-you with a small clockwork bird that sings the date.", "Say thank you, shake hands, and leave it there.")
                        .after(
                                new String[]{"* You ask. It comes out wrong, at the wrong speed, with a sort of squeak in the middle. Mott stares at you for a very long moment. Then the narrow tidy face breaks up into something astonishing, a smile that has clearly been kept in a drawer for minutes, and {mott.he} says, 'Yes. Seven o'clock. I shall wear the other coat.'", "@Mott| It is not a good coat. I want you to know that in advance."},
                                new String[]{"* You send the bird. It is a small brass thing the size of a walnut. It arrives at the Patent Office on Monday and sings the date in a high clear voice at the start of every hour for a week. The whole Office is in uproar. Mott takes it home in a pocket and writes a note, on a card, in a very upright hand, which says: Thursday?", "@Fitch| I think you've been outflanked."},
                                new String[]{"* You say thank you. You shake hands. It is a stiff, brief clasp, over a desk, and both of you let go a little too fast. You go out into the cold afternoon with the Lifting Engine under your arm and the duck on your shoulder, feeling clever and a bit of a fool. From the window above, if you looked up, you would see a narrow face watching you go.", "@Mott| (in a small voice, to the window) Well. That's that."}))
                .done((c, st) -> { int l = Chapters.flag(st, "love"); Chapters.bond(c, st, "mott", l == 3 ? 1 : 3); if (l == 1 || l == 2) Chapters.wed(c, st, "mott"); })
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "Patent Pending",
                "* The Crank has a plate on the door now, in brass, from the Patent Office. It says: MECHANICAL INVENTIONS. It says it in three languages. Under it is a bell that rings if you pull it, which is rare, since people mostly knock and shout.",
                "?duck=1|* Biscuit says 'Interesting' to the furniture, in a high clear voice, and has been known to say it to a bishop.",
                "?duck=2|* Biscuit says nothing. It sits on whichever machine is about to go wrong, and nobody has yet found a way to make it move.",
                "?duck=3|* The toymaker has sent seven more notes. They all say the same: the duck is not wanted, and would you take it back.",
                "?offer=1|* The Lifting Engine is in the Conservatory's courtyard, on a plinth, with a small brass plaque that spells your name wrong.",
                "?offer=2|* There are nineteen Lifting Engines in Tallowmere. Three of them work properly.",
                "?offer=3|* The Lifting Engine is on a shelf in the workshop with a label: PATENT NO. 1. A small crowd of schoolchildren is brought to look at it every Thursday.",
                "* Biscuit sits on the windowsill in the evenings and looks at the stars. It does this with one blue eye and one brown. Nobody has ever asked what it is looking for. The kettle walks in a circle round the stove. In the dusk, if you listen closely, you can hear a very faint sound from somewhere in the roof. It is the sound of an idea, still on its way, getting off its horse.");
        TaleInventorSeasons.add(d);
    }
}
