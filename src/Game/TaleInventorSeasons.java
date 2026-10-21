package Game;

/** The Crank of Gearwheel Lane, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Patent War" (a heist comedy, card A): the Conservatory patents the human's Lifting Engine first, by a trick of dates. The human cannot win in a court, so the crew (Fitch, Old Cobb the locksmith, the duck,
 *  Lysandra Mott on the inside) robs the Conservatory's vault on the night of its gala. What is in the vault is the Conservatory's real secret: a drawer of other people's inventions, bought, buried and never built.
 *  Choices: plan (loud, quiet or legal), trust (what Mott is asked), drawer (expose, take your own, take them all). Flags: plan, trust, drawer.
 *  Season 3, "The Engine of the World" (comic and cosmic): ideas arrive from the Gardener's Shed on loan, and a Clerk of the Hollow Court, Mister Quire, comes with Form 9. The Dimming of Tallowmere is held off by the Ringing Wall.
 *  Flags: wall, loan, form. */
final class TaleInventorSeasons {
    private TaleInventorSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "plan", "trust", "drawer", "wall", "loan", "form");
        d.place("palace", "start").place("shed", "wild");
        d.cast("cobb", "Old", "Cobb").he().keep().trait(Character.Traits.Friendly).side("Neutral").lvl(3).age(66).at("place:lane").rel(2);
        d.cast("quire", "Mister", "Quire").he().keep().trait(Character.Traits.Scribe).side("Neutral").lvl(14).pot(8).age(60).at("far").rel(0);

        // ================================================================== SEASON 2
        d.season(2, "The Patent War");

        d.chapter("notice", "Notice of Infringement").years(2)
                .setup((c, st) -> { Chapters.spawn(c, st, "cobb"); Chapters.spawn(c, st, "fitch"); })
                .intro(
                        "* The letter comes in a cream envelope by a footman in a powdered wig, who hands it over at arm's length, as though it were hot. It is from the Conservatory of Right Arrays. It says that the Lifting Engine, Patent No. 1, is an infringement of Conservatory Patent No. 40,011, filed on the fourth of March. It says that you are required to cease. It says that the penalty is the seizure of all machines.",
                        "* You read it three times. Patent No. 1 was granted on the ninth of June. The Conservatory's was filed on the fourth of March, three months before you built the thing, by a clerk who had never seen it. You show it to Fitch. He says it is the most beautiful lie he has ever read.")
                .wait(12)
                .outro(
                        "* The duck is on the bench looking at the envelope with an expression of grave interest. It pecks it once, very hard, in the middle of the seal. Fitch says that there is nothing to be done. He says the Conservatory has an army of lawyers, a hall full of precedents, and a master who has not lost a case in thirty years.",
                        "@Mrs. Orlop| (leaning in the doorway with a plate of buns) It sounds to me like a case of someone stealing your pie.",
                        "@You| It's more complicated than that.",
                        "@Mrs. Orlop| It's never more complicated than that, dear. It's only longer.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("priorart", "Prior Art").gap(16)
                .intro(
                        "* The first thing to do, Mott says, is prove prior art. It must be shown that the Lifting Engine existed before the fourth of March. Since it did not, it must be shown that something very like it did. Mott comes to the workshop in the evening, off duty, in an overcoat, with a satchel of old catalogues, and spreads them across the long bench.")
                .friends(4, "Find witnesses: make four friends.")
                .outro(
                        "* You spend a fortnight asking. You ask in the lane, and the market, and the old men's club at the canal. You find a retired bridge-builder who remembers a lifting rig from his father's day. You find a widow who has a diary with a drawing. You find a ship's carpenter, a clockmaker, and a woman who sells chestnuts and has, for some reason, a very large collection of old patents in a trunk.",
                        "* By the end of the second week the workshop is full of witnesses, sitting in rows among the half-finished machines, drinking tea. Mott takes down their statements in a neat shorthand. {mott.He} does it with the air of a person at last doing the thing {mott.he} trained for, which is to be on the side of the truth.",
                        "@Mott| I should say I'm not meant to be doing this. It is a conflict of interest. I'm employed by the Conservatory, and I'm helping to prove it wrong. I have never felt so well.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("hearing", "The Hearing").gap(16)
                .intro(
                        "* The Court of Patents sits in a hall with a roof like the inside of a drum. There are forty benches, a gallery, and a high judge's chair on a platform, under a banner of a golden gear. Master Dimmock sits at the table on the left with six lawyers. You sit at the table on the right with Fitch, a duck, and a satchel of statements. The judge is a very old man who falls asleep during the oath.")
                .wait(8)
                .outro(
                        "* You lose. It takes forty minutes. The lawyers speak in a long calm cadence, like water in a drain. They show the filing date. They show the clerk's stamp. They show that the Lifting Engine, as described in the Conservatory's patent, uses a crossed pulley, and that yours uses a crossed pulley, and that crossed pulleys are the property of the Conservatory by prior grant. The judge wakes up and says, 'Quite.'",
                        "* On the steps afterwards Dimmock stops beside you in a long coat, with his gloves in his hand. He says that he bears no ill will. He says that the Engine will be put to excellent use. He says the Conservatory will build a hundred. He says it is a pity to be on the wrong side of progress, and that you will recover. People generally do.",
                        "@Dimmock| Give my regards to your duck.",
                        "* The duck on your shoulder turns slowly, and looks at him. It makes the click that means: noted.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("plan", "A Plan on a Napkin").gap(16)
                .intro(
                        "* The plan is made in Mrs. Orlop's bakery at midnight, on the back of a napkin, over a plate of leftover buns. There are five of you: Fitch, Mott, Old Cobb the locksmith, Mrs. Orlop, who has insisted on attending, and the duck. Between you, you have a skeleton key, forty yards of rope, a map of the Conservatory drawn from memory, and a profound lack of experience.")
                .wait(4)
                .choice(new Chapters.Choice("plan", "The Conservatory's vault holds the original plans and, presumably, the proof. It has a gala in three weeks.",
                        "Go in loud: a diversion with the lane's fireworks, and a dash for the vault.", "Go in quiet: Cobb on the lock, a disguise, and out before anyone notices.", "Go in legal: apply for a warrant of discovery, with Mott's help, and march in with the Guard.")
                        .after(
                                new String[]{"* The plan is loud. Fitch is delighted. He spends the week building fireworks in the cellar, and a rocket the length of a man, which Mrs. Orlop insists on calling Gladys. The napkin is stuck on the wall with a bun. Old Cobb says he has never been so frightened in his life, and takes a second helping.", "@Cobb| I picked my first lock at nine. I've been waiting fifty-seven years for a decent reason."},
                                new String[]{"* The plan is quiet. It is a very careful plan, with eleven steps, and a timetable. Cobb has a key for every door but one. Mott knows the guards' rota. The disguises are in the cupboard: a waiter, a scullery maid, a bishop. The duck, you decide, will be the bishop's hat. Mrs. Orlop is to bring the buns.", "@Mott| It is the most orderly crime I have ever heard of. I find I do not entirely disapprove."},
                                new String[]{"* The plan is legal. You and Mott spend a week in the Archive, with a lamp, drawing up an application of seventeen pages. It is signed by nine witnesses. It is served on a sleepy magistrate, who reads it, and says that it would be a great deal quicker to rob the place. He signs it anyway. It is, he says, the best-drafted request he has seen in thirty years.", "@Mott| I should like it noted that the burglary is the backup."}))
                .outro(
                        "* You pin the napkin to the wall over the bench. It has a sketch of the Conservatory, a row of arrows, and in the corner, in Mrs. Orlop's hand, a bun. Fitch writes a title across the top in capital letters. He says every plan needs a title. After a long argument the title is: OPERATION BUN.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("lock", "The Locksmith").gap(16)
                .intro(
                        "* Old Cobb has a shop on Tinker's Row, a cupboard of a place, hung with ten thousand keys on ten thousand nails. He is sixty-six and small and bent, with a square white beard and hands like a pair of old gloves. He sits at a bench in the window with a lens in his eye and a lock the size of a plum in his fingers. He will not look up.")
                .meet("cobb", "Visit Old Cobb the locksmith in Tinker's Row.")
                .outro(
                        "@Cobb| The Conservatory vault. I've had my eye on it for forty years. It's a Bramah, with a double-bolt and a seven-lever tumbler. I've never seen it opened. They had it put in by a foreigner, who died. It's said to be perfect.",
                        "@You| Can you open it?",
                        "@Cobb| It's not that. I can open anything. It's that I've promised not to. I've promised a lady. It's a long story.",
                        "* He puts down the lock and looks at you for the first time. His eyes are small and bright and very blue. He asks what it is you want out of the vault. You tell him. He listens, nodding, picking at a splinter in the bench. When you have finished he says: 'It's a funny thing. The lady I promised was my wife. She died in the spring. I've been wondering what the promise was for.'",
                        "* He gets up and takes a long black case from a shelf. It is lined with red baize and holds seventeen picks of various sizes. He closes it. He puts on his hat. 'You'd better tell me about this duck,' he says.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("disguise", "Costumes").gap(8)
                .setup(TaleInventorSeasons::mark)
                .intro(
                        "* The gala is a masked ball, which is both a help and a disaster. It means that nobody will recognise anybody. It also means that everyone will be in a costume, and you will be expected to have one. You spend a week building them. You build, as always, rather more than you need.")
                .made(10, "Build the costumes: make ten contraptions.")
                .outro(
                        "* Fitch's is a suit of armour with a hidden compartment. Cobb's is a waiter's jacket with seventeen pockets. Mott's is a grey gown with a tray that holds the guard's keys. Mrs. Orlop has refused a disguise on principle, and is going as a baker, since nobody ever looks at one. Yours is a bishop's mitre with a duck-shaped hole in the top, and a cope that unfolds into a net.",
                        "* The duck tries on its hat. It is a small gold mitre, no bigger than a thimble. It sits at a slight angle. Fitch says it is the most dignified thing he has ever seen. The duck, with a click, turns on the bench and surveys itself in a polished kettle. It appears to approve.",
                        "@Mrs. Orlop| It's very good. It's very, very good. I'd just like to say that I think we are all quite mad.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("inside", "The Inside Woman").gap(8)
                .intro(
                        "* The night before the gala, Mott does not come to the workshop. You wait until eleven. At midnight you go out into the lane and look up at the dark windows of the Conservatory's staff lodgings across the canal. One light is burning, on the fourth floor. A shadow moves behind the curtain, backward and forward, like a person who has not been able to sit down for a day.")
                .wait(4)
                .outChoice(new Chapters.Choice("trust", "Lysandra Mott is going to rob the place that employs {mott.him}, and has not slept.",
                        "Go across and tell {mott.him} that {mott.he} need not do it.", "Go across and tell {mott.him} that you could not do it without {mott.him}.", "Stay in the lane. Some decisions have to be made alone.")
                        .after(
                                new String[]{"* You cross the canal and climb four flights. The door is open. {mott.He} is sitting on the bed in {mott.his} overcoat with {mott.his} hands between {mott.his} knees. You tell {mott.him} that nobody will think any worse. {mott.He} looks up at you for a long moment, and then {mott.he} says, 'I know. That is exactly why I shall.'", "@Mott| I have spent eleven years being scrupulously correct. I should like to do one wrong thing for the right reason."},
                                new String[]{"* You cross the canal and climb four flights. You tell {mott.him} the truth: that you cannot do it without {mott.him}, that the plan will fail, that you have never had a friend who knew the rota. {mott.He} laughs, an odd, relieved bark. 'That is the worst argument I have ever heard,' {mott.he} says. 'I shall be there at nine.'", "@Mott| I do hope you will not make a habit of being so honest."},
                                new String[]{"* You stay in the lane. You stand under the lamp with the duck on your shoulder, looking up at the window. After a long time the light goes out. In the morning Mott is at the door at eight in a grey gown, with a tray, saying nothing. {mott.He} takes your hand for a moment, and lets go, and goes to the kitchen to see about the buns.", "@Mott| Thank you for not coming up. I needed to be sure."}))
                .outro(
                        "* The moon is up over Tallowmere. It is the colour of a bun. A cat walks along the roof ridge of the Conservatory with an air of having an appointment, and in the lane behind you, softly, someone begins to hum.")
                .done((c, st) -> Chapters.bond(c, st, "mott", 1))
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("casing", "Casing the Conservatory").gap(8)
                .intro(
                        "* You spend the day before the gala in the Conservatory in a false moustache. It is not much of a moustache. It is made of brown wool and is attached with gum, and every few minutes one end comes loose and has to be pressed back. You go as a visiting inspector of drains. Nobody asks to see credentials. Everybody wants to talk about the weather.")
                .wait(4)
                .outro(
                        "* You count the guards, which takes an hour: eleven on the ground floor, six on the stair, two at the vault. You count the doors. You count the windows. You count the chandeliers in the great hall, and the number of waiters, and the number of exits, and the number of steps to the nearest cloakroom. The numbers fill a notebook.",
                        "* At four in the afternoon you are caught. A stout footman in a powdered wig steps from behind a pillar and says, 'Excuse me, sir.' You freeze. He asks, in a low voice, whether you are by any chance the gentleman who built the walking kettle. You say you are. He looks round, and whispers that his wife has one, that it brews a lovely pot, and that he has been hoping for a chance to say thank you.",
                        "@Footman| I shall be on duty at the east door tomorrow night. I have a bad memory for faces. I'm told it's the wigs.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("gala", "The Night of the Gala").gap(8)
                .setup(TaleInventorSeasons::mark)
                .intro(
                        "* The Conservatory's great hall blazes with eleven hundred candles. The orchestra is playing a waltz. There are four hundred guests in masks and gowns, dancing under the golden gear that hangs from the ceiling, and a table of food a hundred feet long. At the head of it, in a robe of purple velvet and a mask of silver, stands Master Dimmock, receiving.")
                .made(6, "Work the plan: build six devices on the night.")
                .outro(
                        "?plan=1|* The first firework goes up at nine. The second at ten past. By the third, the whole east wing is in an uproar, the guests are running for the cloakroom. Gladys, the rocket the length of a man, has taken off through the roof of the orangery with Fitch hanging on to the tail and shouting something about physics. In the confusion, you and Cobb are down the back stair with the duck and a bishop's mitre.",
                        "?plan=2|* The waltz plays. You move through the room as a bishop, a waiter, a maid and a baker, bowing, smiling, carrying trays. At nine the guard at the vault stair is called away by a message in Mott's hand. At ten past, Old Cobb is on his knees at the first door with the lens in his eye. At twenty past you are in the cellar, in the dark, by the light of a bun.",
                        "?plan=3|* The warrant is served at nine. The Captain of the Guard reads it aloud in the middle of the dance floor, in a clear, carrying voice, to a hall of astonished masks. Dimmock goes the colour of tallow. The orchestra falters, and stops. Quietly, with a very official face, you and Mott walk across the room and down the back stair, with a Guardsman, to the cellar.",
                        "* The vault door is a huge round slab of grey steel with a wheel in the middle and a keyhole no bigger than a pea. Old Cobb takes off his hat. He kneels. He puts in his first pick, with a sigh, and bends his head. The duck sits on his shoulder and watches him with both its eyes. The only sound in the cellar is a very faint ticking, like a heart.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("vault", "What Was in the Vault").gap(8)
                .intro(
                        "* It takes eleven minutes. When the door swings open it makes no sound at all. Cobb sits back on his heels with a face like an old child's and says, in a whisper, that he can die now. You go in with a lantern. You expect gold, or plans, or a safe full of patents. You find drawers.")
                .wait(4)
                .choice(new Chapters.Choice("drawer", "The vault holds nine thousand drawers, each with a name, a date and a bundle of plans.",
                        "Take the proof of your own engine and expose the vault to the whole city.", "Take only your own plans, and leave the rest untouched.", "Take them all: every drawer, every name, every buried idea, and give them back.")
                        .after(
                                new String[]{"* You take your own papers, and the key to the register, and send a messenger to the newspapers. By morning the whole of Tallowmere knows. The Conservatory buys inventions, you learn, and buries them. It has done so for two hundred years, to keep the Right Way right. Nobody is quite sure how to feel about it. Everyone feels something.", "@Mott| It's going to be the end of a great many careers. I find I'm not sorry."},
                                new String[]{"* You take your own papers. You pause at the last drawer. It is labelled, in a faded copper-plate: Barnaby Crank, Lifting Engine, withdrawn. Underneath it is a drawing in a hand you know, with the word MAYBE written beside it three times. You put it in your coat. You leave the rest. You are not sure it is right. It is, at least, quick.", "@Fitch| That was your uncle's. They had it all along."},
                                new String[]{"* You take them all. It requires forty trips with Mrs. Orlop's bread-baskets, a hired cart, and the entire lane. By dawn there are nine thousand sets of plans in the back of the bakery, and a queue outside the door. The names are on the labels. Within a week the owners are found, one by one, in cottages and attics and the back rooms of shops, and the plans are put into their hands.", "@Orlop| I haven't baked so many buns in forty years. I think it's wonderful."}))
                .outro(
                        "* In the middle of the vault, on a stone plinth, under a glass bell, stands a small thing that looks like a brass bird. It is made of gear-wheels and silver wire, and has one wing missing. A card at its foot says, in a cold neat hand: UNIQUE. DO NOT BUILD. The duck hops onto the plinth. It looks at the bird for ages. Then it clicks, once, and the sound is, you could swear, an enormous sadness.",
                        "@You| What is it?",
                        "@Fitch| It's another duck.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("again", "Prior Art, Again").gap(16)
                .intro(
                        "* The Court of Patents sits again, in the hall like a drum. The gallery is full. The judge is awake. Dimmock does not appear. In his place there is a pale young lawyer who stands up, and sits down again, and mumbles. The old judge asks, in a mild voice, whether he proposes to contest.")
                .wait(4)
                .outro(
                        "* He does not. The register is produced. The papers are read. The old judge listens with his chin on his hand. When it is done he takes off his spectacles and wipes them on his sleeve, and says that in sixty years on the bench he has never been so ashamed of an institution.",
                        "* The Conservatory is not abolished. It is not that sort of country. But it is stripped of the licensing power, and the patent system is handed to a new body, with a panel of inventors and a dean who has never owned a lawyer. The first act of the new Board is to restore Patent No. 1. The second is to ask whether you would be willing to serve as a member.",
                        "@Mott| I shall resign the Conservatory. I should like to say it was a very good job. I should also like to say I hated it.",
                        "* Dimmock is seen later that week on the canal path, in a plain brown coat, feeding the ducks. He looks smaller. He lifts his hat to you as you pass. His hands are shaking a little. You find, to your annoyance, that you are sorry for him.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "Patent Granted",
                "* Gearwheel Lane has a new sign at the top: THE LANE OF INVENTIONS. It has been hand-painted by Mrs. Orlop, with a bun in the corner. She says it is not a pun. She says it is a mark of respect.",
                "?plan=1|* The rocket that went through the roof of the orangery was recovered in a field outside the town, intact. It is on display in the bakery window, with a label: GLADYS.",
                "?plan=2|* The bishop's mitre is on a peg in the workshop, with a duck-shaped hole in the top. The duck likes to sit in it on Sundays.",
                "?plan=3|* The seventeen-page application is framed over the bench. It is considered by lawyers to be the best-drafted request in the history of the city.",
                "?drawer=1|* The register of the vault is on display in the Hall of Records. Visitors may read it. They do, in large numbers, and sometimes weep.",
                "?drawer=2|* Your uncle's drawing hangs over the stove with a card: MAYBE, MAYBE, MAYBE. It is generally agreed to be the best-looking piece of paper in the lane.",
                "?drawer=3|* There are nine thousand inventions out in the world that were not there before. Some of them work. A small number of them are dreadful. A few of them are changing everything.",
                "* The small brass bird from the vault sits on the workshop mantel. Fitch has mended the wing. It does not move. It does not sing. But on certain evenings, at dusk, when the light comes in sideways, it turns its head, and Biscuit, on the sill, turns its head to match. The two of them look at each other, in a long grave silence, like cousins at a funeral.");

        // ================================================================== SEASON 3
        d.season(3, "The Engine of the World");



        d.chapter("idea", "An Idea Arrives").years(2)
                .intro(
                        "* It comes at ten past three in the morning, the way the best ones do, and it is not yours. You know this at once. Your own ideas arrive with a limp and a cough, after a long walk, apologising. This one walks in through the roof, sits on the end of the bed, and says: Wall. A wall of bells, round the whole of a city. Each bell tuned to the next. All of them ringing together, very low, below hearing. And the note keeps the dark out.",
                        "* You are at the bench before you are properly awake. The duck is already on it. It watches you draw. It does not say 'Interesting.' It clicks, very softly, over and over, in a rhythm you have heard before and cannot place. At dawn the sheet is covered, edge to edge, in a hand that is plainly your own and equally plainly not.")
                .wait(8)
                .outro(
                        "@Fitch| (looking at the drawing, in the grey light, with a mug of tea) That's an enormous bell.",
                        "@You| It's a lot of bells.",
                        "@Fitch| It's an array. It's an array the size of a city. It's got nine hundred points. Who showed you this?",
                        "@You| Nobody. It just arrived.",
                        "@Fitch| Ideas don't just arrive. They have to come from somewhere. They come from a postmark. This one's the first I've ever seen that didn't come with a stamp.",
                        "* Mott is in the doorway. {mott.He} has been awake all night, too, on the other side of town, with a feeling {mott.he} could not name, and has come across at dawn in {mott.his} coat over a nightgown. {mott.He} looks at the sheet. {mott.He} goes white. {mott.He} says, 'Where did you get that?' in the voice of someone who has seen a ghost in a library.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("shed", "The Shed in the Wood").gap(16)
                .intro(
                        "* Mott knows where it comes from. {mott.He} has read it, in the Conservatory's archive, in a bound book nobody opens, in a chapter called Provenance. Ideas, the chapter says, are not made. They are lent. They are issued from a store, to those who can use them, on loan, for a term. The store is in a shed, in a deep wood, in the north. Nobody has seen it for three hundred years.")
                .at("shed", "Find the shed in the wood, {@shed}.")
                .outro(
                        "* It is a plain, low, weathered wooden hut at the end of a path that was not there yesterday. It has a door with a latch and a window with a cracked pane. Inside, on racks and pegs and hooks, in long dusty rows, hang tools. They are the strangest tools you have ever seen. There is a plane that planes time. There is a pair of tongs for lifting light. There is a hammer, the size of a thimble, whose head is a small closed fist.",
                        "* You walk along the racks, with Mott behind you, holding a lantern. The duck is on your shoulder. It has gone completely silent. Every tool has a label, tied on with string. The labels have names, and dates, and a line in small neat capitals: ON LOAN TO.",
                        "@Mott| (in a whisper) These are the ideas. These are all of them. The wheel. The lever. The clock. The array. They are tools. They are tools for building a world, and they have been left here, on loan, for anyone who needs them.",
                        "* You find your own name three-quarters of the way down the long rack. It is on a small brass drill. The label says: ON LOAN TO: CRANK, B. And under it, in a different ink, in a newer hand, so fresh that it has not yet dried: AND TO HIS HEIR.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("loan", "On Loan To").gap(8)
                .intro(
                        "* There is a ledger in the shed, on a high desk at the end of the room. It is bound in black cloth and as big as a church Bible. Its pages are ruled in columns: Tool, Borrower, Date, Term, Returned. Mott opens it at the beginning. The first entry is in no language either of you can read. The last is in yours, and says that the Wall has been issued, on a term of nine years, for use in the city of Tallowmere.")
                .wait(4)
                .outro(
                        "* You read down the page. Every entry has a column marked Purpose. The wheel was lent for carrying. The lever was lent for lifting. The clock was lent for waiting. The array, you read, was lent for protecting, and for no other thing. At the very bottom of the page is a line in a hand like a row of nails: These tools are lent to be used. If they are not returned they are not lost. They are owed.",
                        "@Mott| I have spent eleven years of my life guarding patents for ideas that were never ours. I find I am not as angry as I should be.",
                        "@You| What's it owed to?",
                        "@Mott| To whoever lent them. And to anybody who might need them next.",
                        "* The duck, on the ledger, walks slowly down the page with its head cocked, reading. At the last entry it stops. It taps the line with its beak, three times. Then it looks at you. You have the unsettling sense that it has read something you have not, and is deciding whether to tell you.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("build", "Fitch Builds a Prototype").gap(16)
                .setup(TaleInventorSeasons::mark)
                .intro(
                        "* The prototype takes a month. It is a single bell the size of a hat, with nine silver rings round the rim, and a clapper made from a spoon. You and Fitch build it in the yard behind the bakery, under a canvas, with a great many wooden pegs and a pot of glue. Mrs. Orlop brings buns. The lane watches over the wall. Nobody makes a joke.")
                .made(12, "Build the prototype: make twelve parts of the bell.")
                .outro(
                        "* You set the bell on a trestle in the yard on a cold bright evening, and Fitch strikes it, softly, with a wooden mallet. It makes no sound at all. Then the air round it begins to hum. It is a low, steady note, so deep that it seems to come out of the ground. Mrs. Orlop sits down very suddenly on a barrel. The lane's forty-one cats come out of the cellars, one by one, and sit in a circle round the trestle with their eyes shut.",
                        "* A pane in the bakery window shivers and goes to dust. The note does not rise. It does not fall. It rings on and on, in the ground and in your teeth, and where it reaches the evening light stands very clear, as though a lamp had been lit in the air.",
                        "@Fitch| (in a hoarse whisper) That's it. That's the one.",
                        "* The duck, on your shoulder, begins to hum, very faintly, a single note. It is the same note. It has a brass throat, and no reed, and has never made a noise but a click, and it is humming.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("dimming", "The Dimming of Tallowmere").gap(24)
                .intro(
                        "* It happens on a Thursday, at noon, without warning. The sun is high. The market is full. A woman is arguing with a fishmonger about a mackerel. Then the light goes out. It does not fade. It is simply switched off, as a lamp is, from some great distance, and the whole of Tallowmere stands in a cold blue dark with no stars in it, for the length of an hour.")
                .wait(4)
                .outro(
                        "* You are in the yard, with the prototype bell, and it is ringing. You did not strike it. It has begun, of its own accord, to give out the deep low note, and the note holds a little ball of light about the size of the yard, in which you stand with Fitch, Mrs. Orlop, the duck and the cats. All round you in the dark, you can hear the city weeping, and shouting, and asking, in thin lost voices, the names of the people they love.",
                        "* The light comes back at one. The sun is where it was. The mackerel is still on the slab. All over the city, people stand blinking in the street and look at each other with polite bewilderment. A woman asks her husband, politely, if they have been introduced. A baker does not know his own apprentice. Down the lane, a man sits on a kerb and cries, because he has just remembered that there was a person in his life and cannot think who.",
                        "* Mott arrives at a run, hatless, wild-eyed. {mott.He} stops in front of you. {mott.He} looks at your face, as if reading something small and difficult. Then {mott.he} says your name. {mott.He} says it three times, and takes you by both arms. 'I remember,' {mott.he} says. 'Everything. How is that possible? I thought it was supposed to take everyone.'")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("quire", "Mister Quire").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "quire"))
                .intro(
                        "* The man who calls the next morning is dressed in a grey suit with a high collar and carries a leather document case. He has a face as smooth as a stone. He wears a pair of spectacles with very thin gold rims, and a badge that says CLERK. He enters the workshop with his hat in his hand, and stands by the door, and waits to be offered a chair.")
                .meet("quire", "Receive the Clerk at the workshop.")
                .outro(
                        "@Quire| Mister Quire. I am here as a Clerk of the Court. I represent a party you may have heard of. I should explain at once that I am not a hostile presence. I am an administrator. There has been a regrettable irregularity in the matter of an invention.",
                        "* He opens the case. He takes out a printed form. It is the colour of a pale cheese. It has nine boxes and a small neat title in capitals: FORM 9. DECLARATION OF ORIGIN. He lays it on the bench and smooths it with the flat of his hand, the way a man smooths the sheet on a sickbed.",
                        "@Quire| The Court has noticed that a device of considerable power has been proposed within this city. It has no recorded origin. It is not registered with any approved source. The Court is concerned that it may have been acquired without authority. I am obliged to ask you to complete the form.",
                        "@You| And if I don't?",
                        "@Quire| Then the item is deemed to be unclaimed, and is returned. To the place it came from. It is quite painless. It simply stops having been.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("form", "Form Nine").gap(8)
                .intro(
                        "* You read the form for a day and a half. It is the most thoroughly baffling document you have ever seen. It asks for your name, your trade, your place of birth, the place of birth of your invention, the place of birth of its parts, and the place of birth of the idea of its parts. It asks you to state whether the work is wholly your own. It asks, in the last box, in a tiny italic, whether you have at any time been Lent To.")
                .wait(4)
                .choice(new Chapters.Choice("form", "The Clerk waits at the door with a pen, and a pot of ink, and a patient face.",
                        "Fill it honestly: write that the Wall was lent, by the Shed, for protecting.", "Fill it falsely: write that the work is wholly your own, and sign.", "Refuse to sign, and say that an idea cannot be filed.")
                        .after(
                                new String[]{"* You write the truth. It takes forty minutes. You put Origin: the Shed in the north. You put Term: nine years. You put Purpose: to protect. Quire reads it with faint surprise, and, to your astonishment, nods. 'That is perfectly in order,' he says. 'It is not often that a form is filled in so correctly.' He stamps it with a small iron stamp, and sighs. 'I had rather hoped you would lie.'", "@Quire| It would have been so much simpler."},
                                new String[]{"* You write: wholly my own. You sign it with a flourish. Quire takes it, and reads it, and puts it carefully in the case. He says nothing for a moment. Then he says, in an almost gentle voice, that he is obliged to inform you that, in the opinion of the Court, a falsehood on Form 9 is a very serious matter. It forfeits the term. It does not forfeit the tool. It forfeits you, a little.", "@Quire| I shall be back. I shall bring a longer form."},
                                new String[]{"* You refuse. You tell him that an idea cannot be filed. It cannot be owned, or borrowed, or declared. It can only be had. Quire looks at you for ages over the thin gold rims. Then he picks up the form, and tears it neatly in half, and in half again, and puts the pieces in his pocket. 'Nobody has ever said that to me,' he says. 'I shall have to go away and think about whether it is true.'", "@Mott| I have the strangest feeling you have just won a point."}))
                .outro(
                        "* He stays to tea. It is a stiff, formal, curious tea. He sits on a stool in the workshop with a cup in one hand and the document case on his knee, looking round at the forty-one half-finished machines with an expression of mild distress. He admires the walking kettle. He asks if the duck is registered. He says, in a low voice, that he used to have a hobby once.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("palace3", "The Workshop Under the Palace").gap(16)
                .intro(
                        "* The brown cart is at the door by the end of the week, with a Wick in a thin coat. The note he carries is in a careful round hand. It says: There is a bench here, and a good lamp, and a nine-point pattern that is older than the Wall. Please come. I do not understand a thing I have been asked to do, and I think you might.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and down to the Under-Kitchen.")
                .outro(
                        "* The Under-Kitchen is warm, and a long passage runs off it, with a forge at the end and a bench in the corner. A man in an apron is sitting on a stool with a screwdriver, looking at a clock, with the doomed concentration of someone who has just opened it. He looks up. He stands. He sits. He says that his name is ???, and that he has taken it apart and cannot put it together.",
                        "@???| I am allowed to tell you what the Wall is for. It is a little of the old work, from before the Garden. It is a lamp, for a city, that does not go out. The one who made the Garden had a hundred of them, and he put them at the corners, and when he left to fight he took most of them with him. This is one of the few he forgot.",
                        "* The Lamp of Iron is at the forge. She is a broad grey woman with her sleeves rolled to the elbow and an enormous apron. She does not look round. She holds out a hand for the drawing, and reads it in one glance, and grunts, and says it needs nine more points, and a sleeve of bronze, and one more thing she will not explain. It takes her four minutes. It would have taken you a year.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("wallbuild", "The Ringing Wall").gap(16)
                .setup(TaleInventorSeasons::mark)
                .intro(
                        "* It is the largest thing you have ever built. It is a ring of nine hundred bells, each as tall as a man, set in the ground round the walls of Tallowmere, at a hundred paces apart. It takes a year, and the whole of the city. Every guild lends a hand. Every school sends its boys. The bakers bring food. The Conservatory, in its new shape, sends its apprentices, who learn, in a single morning, more than they learned in seven years.")
                .made(30, "Raise the Wall: build thirty bells.")
                .outro(
                        "* On the day it is finished, the whole city turns out on the walls. They stand in rows, in their best, with their children on their shoulders, looking down on the long ring of silver bells in the winter grass. Master Dimmock is there in his brown coat, with a flask. Old Cobb has brought his wife's picture. Mrs. Orlop has baked nine hundred buns, one for each bell, with a sugar note on top.",
                        "* Fitch, who has built every one of the nine hundred clappers by hand, takes the mallet. He is trembling. He turns to you. He says, 'Do you want to do it?' You say no. You say it is his. He swallows and nods and walks out onto the grass, alone, to the first bell, and lifts the mallet in both hands.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("night", "The Night It Rang").gap(8)
                .intro(
                        "* They come on the first night of spring. The air gets cold first, in a straight line, from the north. Then the stars go out, one after another, like a row of lamps put out in a corridor. Then there is a sound that is not a sound, a long slow pressure, like a hand closing, and the whole sky over Tallowmere begins, gently and without malice, to go dark.")
                .wait(4)
                .choice(new Chapters.Choice("wall", "The Wall is ringing. The Court is leaning on the sky, and the city is holding its breath.",
                        "Hold the line: ring the Wall at full strength, for the whole of Tallowmere.", "Widen it: send the note out past the walls, to the villages and the roads.", "Offer it up: let the note go to the Palace, and the Garden, for all.")
                        .after(
                                new String[]{"* You ring it at full strength. The note rises from the ground like the sea. Where it meets the dark there is a long gold line, a wall of light, and the dark presses on it from the other side and cannot pass. The whole night it holds. The city is awake on its roofs, singing. At dawn the pressure lifts, as if a hand were withdrawn, and the sun comes up on a city that has not forgotten anyone.", "@Fitch| (hoarse, grinning) We kept them all."},
                                new String[]{"* You widen it. It takes every bell at once, and every hand on the ropes, and a thing in your chest that you did not know you had. The note goes out past the walls, over the fields, down the roads, into the villages. Where it touches, lamps come on in cottage windows. Old men sit up in bed and say a daughter's name. By morning the Wall has held for a hundred miles, and you cannot stand.", "@Mott| (holding you up) You great fool. That was too far. That was the finest thing I have ever seen."},
                                new String[]{"* You give it up. You do not ring the Wall for Tallowmere; you turn the note upward, through the bells, through the clouds, to the Palace on the hill. It goes up like a pillar. In the Under-Kitchen a man in an apron is standing at the range with a spoon in his hand, and he hears it, and he sits down on the floor, and listens, with his head in his hands. The Wall carries it into the Garden itself.", "@???| (very faint, a long way off) I did not know there was a thing like this. I did not know it could be a gift."}))
                .outro(
                        "* Mister Quire is at the end of the lane when it is over. He stands in the grey dawn in his thin suit, with the document case at his side. He is looking at the Wall. His mouth is slightly open. A single bell is still ringing, very low. He puts out a hand, and lays it on the nearest bell, and holds it there. He stays that way for ages.",
                        "@Quire| I shall have to file a report. I should like to say that I have never seen one work.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("return", "Returning the Tools").gap(24)
                .intro(
                        "* The Shed writes. It sends no letter: it sends a feeling, in the workshop, one morning, like the click of a latch. The duck lifts its head. Fitch looks at the bench and the brass drill, and says without being asked that the term is ending. Nine years. The tools are to come back. They are owed.")
                .wait(8)
                .choice(new Chapters.Choice("loan", "The Shed wants its tools back, and the ledger is open at your name.",
                        "Return every tool: take the plans to the wood, and set them on the racks.", "Keep them: you have earned them, and the city has need.", "Re-lend them: copy every plan and send it to anyone who asks, so that nothing can be called back.")
                        .after(
                                new String[]{"* You carry the tools north in a handcart. You set them back on the racks, one by one, in the dust, under their labels. You write Returned beside your own name in the ledger. The shed is very quiet. Your head is quiet, too, for the first time since the night the Wall arrived. You walk home through the wood without an idea in your head, and are happy.", "@Fitch| I think I'm going to miss them. I think it's the right thing."},
                                new String[]{"* You keep them. You tell the Shed so, in a letter, to a post office that does not exist, in a very polite hand. You do not receive an answer. The tools stay on the bench. The drill is warm to the touch on certain nights. You have a sense of being owed, in some corner of the universe, a debt that you will have to pay one day, in a coin you have not yet seen.", "@Mott| It's a dangerous arrangement. I find I am not at all afraid."},
                                new String[]{"* You copy them all. It takes a hundred and eleven days. You send the plans to every town and every village and every school, free, in handwritten sheets, on the lane's bread-wrappers. When the Shed comes for its tools, it finds nothing to take. Every invention has been lent a thousand times over. The ledger, when you look at it, has a new line at the bottom, in a very small neat hand: Lent to: everyone.", "@Orlop| There. I told you. It's never more complicated than that. It's only longer."}))
                .outro(
                        "* The spring comes. The Wall hums quietly in its ring, nine hundred bells in the grass, and the lane goes about its business. The kettle walks in circles round the stove. Biscuit sits on the sill in the sun with a mitre on its head. In the quiet of the afternoon, you take a clean sheet of paper and a pencil, and write across the top, in your small cramped hand: MAYBE.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("lane3", "Gearwheel Lane").gap(32)
                .intro(
                        "* The sign at the top of the lane has been repainted. The bakery has a second oven. The lamp-maker next door has a shop full of lamps that need no oil, and a queue. Old Cobb has opened a school for locksmiths in the cellar of the Conservatory, now the Board of Inventions. Fitch has a workshop of his own across the lane, and a sign: LARKIN, MECHANICAL. He has already blown up the porch.")
                .wait(12)
                .outro(
                        "* You sit on the step of the Crank in the evening sun, with the duck on your knee and a bun in your hand. The lane is full of noise: hammering, shouting, the clink of tea-cups, a faint continual ticking. It is the noise of forty-one machines, all running at once, none of them quite as they were meant. Somewhere a bell is ringing, very low. It will go on ringing for years.",
                        "@Mott| (arriving, with a bag of oranges) I have decided to retire the Clerkship. I have decided to take up something less suited to my temperament. I thought I might help with the workshop. In the evenings. In an advisory capacity.",
                        "@You| Advisory?",
                        "@Mott| Very advisory.",
                        "* The duck looks up at you, and then at {mott.him}, and then at the sky. It gives the click that means: well, then. This time you know exactly what it means.")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* Gearwheel Lane smells of hot oil, new bread and mild disappointment. It has forty-one half-finished machines in the Crank alone. One of them works.",
                "?wall=1|* The Ringing Wall rings once a year, on the first night of spring. Nobody forgets anyone in Tallowmere. They say it is a feature of the climate.",
                "?wall=2|* The note of the Wall carries a hundred miles, and the villages leave a lamp in every window. Travellers say they can hear it from the road, at dusk, a low steady hum like a cat.",
                "?wall=3|* The Wall rings in the Palace as well. In the Under-Kitchen, on certain evenings, a man in an apron stops stirring the pot, and listens, and smiles.",
                "?form=1|* A grey Clerk calls every year with a form. He fills it in himself. He has begun to leave a sprig of lavender on the bench.",
                "?form=2|* A grey Clerk calls every year with a longer form. You fill it in wrongly. He thanks you.",
                "?form=3|* A grey Clerk comes to tea on Thursdays, and has taken up the hobby of repairing clocks. He is not good. He is happy.",
                "?loan=1|* The tools are back in the Shed, on their racks, in the dust. The ledger says Returned. Sometimes in the evening an idea comes up the lane on foot, sits on the step, and waits to be asked in.",
                "?loan=2|* The brass drill is warm on certain nights. You do not mention it.",
                "?loan=3|* Every child in Tallowmere knows how to make a lever. Some of them know how to make an array. A few of them are already working on a thing called a duck.",
                "?love=1|* Lysandra Mott keeps the accounts of the Crank and has never once filed them correctly.",
                "?love=2|* Mott has a key to the workshop, and an opinion on every machine in it.",
                "?love=3|* Mott sits by the stove on Thursdays, at a respectful distance, and corrects the spelling on the labels.",
                "* At night, when the lane is quiet, Biscuit sits on the windowsill and looks at the stars with one blue eye and one brown. From time to time, very faintly, it hums the note of the Wall. On the mantel behind it a brass bird with a mended wing turns its head to listen.");

        // @@END
    }
}
