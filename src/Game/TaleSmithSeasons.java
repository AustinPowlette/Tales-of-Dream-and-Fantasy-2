package Game;

/** The Blacksmith's Child, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "Evenfall's Edge" (war drama and moral fable, working voice): Evenfall will not cut anyone who has not raised a hand; in a war that is everyone. A hero wants to borrow it. The wells are poisoned
 *  with weeping blades. Marshal Ketter wears the Salt Oath, a ward that turns aside every blade forged for war; Evenfall was forged for nothing, and passes. Pia has her own forge now; Tam is her apprentice.
 *  Season 3, "The War Forge" (cosmic): the Lamp of Iron asks for a thousand blades for the Gardener's war against the Hollow Court. The player will make none for a person they have not met.
 *  Flags: lend (the sword and the hero), marl (a person to love; Marl is always of the other gender than the player), deal (how the order was answered), tamgo (what became of Tam), edge (what the last blade was for). */
final class TaleSmithSeasons {
    private TaleSmithSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "lend", "marl", "deal", "tamgo", "edge", "ketter_ward", "seven");
        d.place("well", "wild").place("camp", "far").place("palace", "start");
        d.cast("seven", "Seven", "Harrow").he().trait(Character.Traits.Blacksmith).side("Neutral").lvl(2).age(41).at("far").rel(0).free();
        d.cast("aldric", "Aldric", "Vane").he().trait(Character.Traits.Leader).side("Good").lvl(3).pot(8).age(35).at("here").rel(2);
        d.cast("ketter", "Marshal", "Ketter").he().trait(Character.Traits.Leader).side("Evil").lvl(9).pot(10).age(58).at("place:camp").rel(-3);

        // ================================================================== SEASON 2
        d.season(2, "Evenfall's Edge");

        d.chapter("queue", "The Queue").years(3)
                .intro(
                        "* There is a queue outside the forge on the first morning of spring, and it is not for horseshoes. It begins at your door, runs past Marl Quenby's gate, which does not squeak, and ends somewhere around the bridge. You counted forty-one before you stopped counting. Pia would have said forty-three.",
                        "* Pia is not in it. Pia has a forge of her own now, two streets down: a lean-to with a chimney, a sign of three hammered dots, and a boy of seventeen at the bellows who keeps being told to stop quoting you. She comes by every noon with a pie and a complaint.",
                        "@Pia| You know what they want. They want to look at it. They want to ask if it'll cut bread, and you'll say it will, and they'll ask if it'll cut a man, and you'll say it depends, and then they'll want to know on what. It's a very long morning, being a legend.",
                        "@You| I'm not a legend.",
                        "@Pia| No, you're a smith. The sword's the legend. You're just the man who lives with it.",
                        "* The first in line is a woman who wants to know whether it is true that it will not cut the innocent. The second is a merchant who wants to buy it for twenty thousand. The third is a child, who wants to know if it has a name, and when you tell her she says it is a sad name, and when you ask why she says, 'Because it's the end of the day. After that it's dark.'")
                .wait(6)
                .outro(
                        "* By the end of the second week the town has made peace with its own fame and started charging for tea. Marl sells eggs to the queue at a markup that Pia calls disgraceful and has secretly invested in.",
                        "* On the last afternoon a man comes in out of the dust, with a boot full of the road and nothing to sell. He is a refugee from the east, from somewhere the war has been through, and he sits on the bench by the hearth with his hat in his hands.",
                        "@Refugee| I came to see if it was true. We heard in the camp there was a sword that wouldn't cut a man who hadn't earned it. I wanted to say that where I'm from there's a Marshal, and nothing cuts him. Not an axe, not an arrow. We tried everything a village has got. He just walks.",
                        "@You| Nothing at all?",
                        "@Refugee| Nothing that was made to hurt him. That's what the priest said. It's an oath, he said. Salt on the blade, salt on the skin. I don't understand it. I only know I saw a boy hit him with a mattock, and the mattock turned in the boy's hands like a snake.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("borrow", "The Borrower").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "aldric"))
                .intro(
                        "* The man in the good coat turns out to be a knight. He is called Aldric Vane, and he is called the Bright, which he says he did not ask for, in a tone that suggests he has said it before. He is thirty-five and moves like a man who has been tired for years and has not yet been told.",
                        "@Aldric| I'm told it cuts only those who raised a hand. I should like to borrow it. Not to keep. To use, in the east, for as long as it takes. I have a company of two hundred and a war that I cannot lose, because if I do there are eleven villages that nobody will be able to find afterwards.",
                        "@Pia| It's not a loan. It's not a tool. It's a thing he made for his father.",
                        "@Aldric| I understand that. I'm sorry. I do. I'm also asking.")
                .wait(2)
                .outChoice(new Chapters.Choice("lend", "Ser Aldric asks to borrow Evenfall.",
                        "Lend it to him.", "Refuse, and tell him why.", "Go east with him, and carry it yourself.")
                        .after(
                                new String[]{"* He wraps it in his own cloak and carries it out of the forge in both arms like something asleep. At the door he stops. 'I'll bring it back,' he says. 'Whatever else.' It is a strange thing to say, and you turn it over for weeks.", "@Pia| You'll regret that.", "@You| Probably."},
                                new String[]{"@You| It was made so no one could use it as a weapon.", "@Aldric| That is what a weapon is for, in my experience: to be made so that no one can use it.", "* He bows, which you did not expect, and goes. He does not seem angry. That is the worst of it."},
                                new String[]{"@Aldric| I won't ask why.", "@You| I'd like to see what it's for.", "@Aldric| Then it's a long road, and I'm glad of the company. Bring a hat. The east is mostly weather."}))
                .outro(
                        "?lend=1|* He comes back in six weeks. He is walking, which is the first thing you notice, and not riding, and the sword is in his cloak, and his face has the look of a man who has been scraped out and put back wrong.",
                        "?lend=1|@Aldric| It cuts. It does exactly what it says. It cut forty men. Every one of them had raised a hand. Every one of them was seventeen or thereabouts, and they were all very surprised. I do not know what I thought I was asking you for.",
                        "?lend=1|* He sets Evenfall on the bench. The blade is as bright as it was. Of course it is. It has never been dishonest.",
                        "?lend=2|* He comes to the forge once more, a month later, with a different request: a hundred nails, and a good price. You make them. He takes them east. You never find out what they were for, and you know it would be a mistake to ask.",
                        "?lend=3|* The road east takes eleven days. You see three burned farms, a dry well, a child who will not look at you, and a field of men who laugh too loudly in the evenings. The sword stays in its cloth the whole way. You are not sure you have ever been so tired.",
                        "?lend=3|@Aldric| I understand now why you wouldn't lend it. I didn't understand it in the forge. Forgive me. I should have asked what it was like to carry.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("tammark", "Tam's Mark").gap(8)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* On the morning Tam turns eighteen he comes into the forge before the fire is lit, which has never happened, and stands at the bench with his hands behind his back. He has grown a moustache. It is a soft, optimistic, ginger moustache, and everybody has been tactful about it for a month.",
                        "@Tam| I want to make something on my own. Start to finish. Not a piece of yours with my hand in it. I want to put my mark on it.",
                        "@Pia| What mark?",
                        "@Tam| That's the thing. I haven't got one.",
                        "* Pia puts down the tongs. She looks at him for a moment with a face you have rarely seen on her, a kind of terrible gentleness, as of someone being asked to hold a very small bird.",
                        "@Pia| Sit down. I'll tell you how it works. You don't pick one. You wait until you've got something to say that you can't say in a word. Then you hit it into the metal with a nail.")
                .made(2, "Help Tam make his first two pieces on his own: a hinge for his mother's gate, and one other.")
                .outro(
                        "* The hinge takes him a week and is, by any standard, a bad hinge. It is thick at the knuckle and thin at the pin. It will not swing true. He knows it, and holds it up to the light with his jaw set and his ears red, as if daring somebody to say.",
                        "@Pia| It'll work.",
                        "@Tam| It's ugly.",
                        "@Pia| It's yours. That's the first rule of a mark. It doesn't say it's good. It says whose it is.",
                        "* He takes the nail and the smallest hammer and holds the point to the shoulder of the hinge. He looks at it so long that Marl, who has put an egg on the bench and been forgotten, begins to clear a throat. Then he strikes. Three quick taps, and a little downstroke. It is a bird, or the beginning of one, with an eye and a tail. A wren, he says, because it is small and it is loud and nobody ever believes it has got that voice in it.",
                        "@Pia| That's a good mark.",
                        "@Tam| It's crooked.",
                        "@Pia| So was mine. You have to be crooked on purpose. Otherwise it's a hinge pretending to be a person.")
                .pay(100, 0, 0, 1, 0, 0);

        d.chapter("wells", "Weeping Water").gap(10)
                .intro(
                        "* The first well goes in the autumn. It is a village well in the marsh country, ordinary and deep, with a mossed lip and a rope. A week later, nine people are in bed and a child is dead, and the cause is not the water. The cause, the village priest says, is something in the water.",
                        "@Tam| That's my aunt's village. Lower Reedham. She wrote to say she's all right. I don't believe it.",
                        "@Pia| We'll go. All of us. Now.",
                        "* You are, for a moment, a family in a doorway putting on its boots. It is a feeling you have not had in years, and it takes you by surprise: the sense of being a unit, and of the world outside as something to be crossed together.")
                .at("well", "Go to the poisoned well at {@well}.")
                .outro(
                        "* The well is at the centre of a clearing that has gone very quiet. Nobody is drawing water. The villagers stand in a loose ring at a respectful distance, as if the thing in the middle might be hungry.",
                        "* Pia is the one who sees it. She goes down on her knees in the mud beside the stone lip and puts her hand into the shaft to the elbow, and draws out something long and narrow wrapped in rotted cloth. A rod. Black. Dripping.",
                        "@Pia| It's a blade. It's one of ours. Not ours. Theirs. It's a weeping blade, quenched in something. It's still seeping.",
                        "* Tam has gone a bad colour. He takes the rod from her in both hands and puts it down very carefully on the grass, as if it might wake. His aunt, a short broad woman in a clean apron, puts an arm round his shoulders without comment and looks at you over his head.",
                        "@Aunt| They came in a cart. A nice man with a clean coat. He said they were testing the water for the Marshal's men. He lowered it down on a rope. I thought he was lowering a bucket.",
                        "@You| Did he say his name?",
                        "@Aunt| He said the Marshal would be coming through in the spring. He said it would go better for us if we had been cooperative.")
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("ward", "The Salt Oath").gap(6)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* A strip of cloth arrives in the post from the east in a wooden tube. A priest has sent it, with a note. The cloth is grey, ordinary, a handspan wide, and has been cut from the cloak of the Marshal's quartermaster, who was killed by a mob in a village the Marshal had not yet reached. The note says: 'It is still warded. Try what you like.'",
                        "* You spend an afternoon with Tam, setting up a dummy and hanging the cloth from its neck. You try a chisel. A hunting knife. A cavalry sabre from the wall. They go in, and stop, and slide off, as though the cloth were oiled, or the blade were a thought it had decided against.",
                        "@Tam| It's not hard. It's just that nothing's willing.",
                        "@Pia| Make me three. Different. A sabre, an axe, a thing for stabbing. Nothing else in the shop is allowed to be on the table while we do it. I want to see them fail properly.")
                .made(3, "Test the ward: make three different blades to try on the Marshal's cloth.")
                .outro(
                        "* They fail properly. The sabre is a good sabre, a clean piece of work, and it slides off the cloth with a tiny shining sound like a bell. The axe stops dead. The thing for stabbing bends, politely, at the tip.",
                        "* It is very late when Pia says what all three of you have been thinking and not saying. She takes Evenfall down from the wall, still in its oilcloth, and puts it on the bench. She does not unwrap it.",
                        "@Pia| It's the only thing in the shop that was never made to hurt anybody.",
                        "* You hold the cloth up on a pole. You bring the blade down.",
                        "* It goes through like a thought goes through a mind. There is no sound. The cloth simply separates, and the two halves fall to the floor and lie there like a thing that has just been told something. The dummy is untouched. The cloth was warded against blades that wanted to cut. Evenfall did not want to cut. It wanted to be fair.",
                        "@Tam| That's the Marshal's armour.",
                        "@You| That's the Marshal's armour, yes.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("three", "Number Three").gap(8)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* She comes at night, which is her habit, with the door open behind her and the bucket in her hand. She has not slept. She sits on the anvil, which she has not done since she was fifteen, and puts the bucket between her feet.",
                        "@Pia| I want to make the scabbard.",
                        "@You| It's your hand.",
                        "@Pia| It's not about my hand. It's that a sword like that shouldn't hang on a nail. I've been thinking about what you do with a thing that cuts only the guilty, and it seems to me the thing you do is put it in a sheath that knows it. I'd like to make one that won't open for just anybody. A catch. Nothing complicated. A thing that thinks twice.")
                .made(2, "Forge the sheath with Pia: make two pieces under her hand.")
                .outro(
                        "* It takes eleven days, and it is a beautiful thing. Pia works as you have never seen her work: she says nothing, counts nothing, and does not look up when you enter the room. The catch is a little hinged tongue of iron that rises when the sword is drawn by someone who means it, and stays down otherwise. She has stamped the throat of it three times.",
                        "@Pia| It won't fail. I tested it on Tam. It stayed shut for nine minutes while he tried to open it with a spoon.",
                        "@You| I've never seen you make something without a plan.",
                        "@Pia| I had a plan. It was to be better than the thing I used to make. I've been waiting to tell you that I think it worked.",
                        "* She puts the finished sheath in your hands and holds on to the other end for a moment longer than the handing-over requires. You have never been good at this. You have never needed to be.",
                        "@Pia| You were never my master, you know. You were just the one who opened the door.",
                        "@You| You were the one who knocked.",
                        "@Pia| Well. Somebody had to.")
                .pay(0, 0, 0, 1, 1, 0);

        d.chapter("hinge", "A Hinge for Marl").gap(6)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The hinges you made for Marl Quenby's gate in the spring of your first year have given trouble at last. They have not squeaked. They have, if anything, been too good. The gate has been opened and shut a hundred thousand times, and this week one of the pins has begun, very faintly, to sing.",
                        "* You go over with the oil can and a small hammer. Marl is on the step with a mug, and a look of having been waiting. There is something on the step beside the mug that you have not seen before: a pair of boots, man-sized or woman-sized, old, resoled, with the laces tucked neatly inside. They are not Marl's.")
                .made(1, "Mend the song out of Marl's gate: make one new pin.")
                .outro(
                        "?side=1|@Marl| He carried a League spear at Greywater. One of yours, or so the quartermaster said. He was thirty-seven, and a baker. He had never held a spear in his life, and he took it because it was the thing they gave him. He told me when he left, at this gate, that it was a good socket. He said it was the first honest thing anyone had handed him all year.",
                        "?side=2|@Marl| She carried a Dukes' pike at Greywater. One of yours, so they said. She was thirty-seven, and a baker. She had never held a pike in her life, and she took it because it was the thing they gave her. She told me when she left, at this gate, that it was a good socket. She said it was the first honest thing anyone had handed her all year.",
                        "?side=3|@Marl| He was at Greywater. I don't know what he carried. Nobody could say. Two pikes broke in the third hour, they told me, at the same place, and he was between them. I've thought about that a great deal. I've thought about a number scratched on a socket.",
                        "?side=4|@Marl| He was at Greywater. You didn't arm either side, and it made no difference, and he is dead. I tell myself that. I tell myself that it would have been worse. I've never been sure whether that's comfort, or the opposite.",
                        "* You hold the oil can. You say nothing at all. There is a time to be sorry, and it is not on a step with a mug.",
                        "@Marl| I've never said it to you. I didn't want it to be your fault. It isn't. It's a gate and a pair of boots. But it gets harder to say nothing, when you keep coming round with an oil can.",
                        "* You fit the new pin. It slides home. You swing the gate, once, twice, and it does not squeak, and does not sing, and Marl listens to the silence with the face of someone watching a long procession go by.",
                        "@Marl| That's lovely. You do good work.",
                        "@You| I'm trying.",
                        "@Marl| I know. That's the part I can stand.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("eggs", "Eggs").gap(10)
                .setup((c, st) -> Chapters.spawn(c, st, "marl"))
                .intro(
                        "* The war has come as far as the river, and it has made everyone a little more honest than they were. Marl Quenby has been at the forge every evening for a month, with eggs and no reason.",
                        "* You have been aware of it, in the way you are aware of a noise in the next room. You have been aware of the other things too. The gate that does not squeak. The hinges Marl oiled and did not need to. The long stretches of conversation at the anvil about nothing, which always ended in the same place. Pia has said nothing. Pia has been very loud about saying nothing.")
                .meet("marl", "Go and see Marl Quenby, who has not said what the eggs are for.")
                .outChoice(new Chapters.Choice("marl", "Marl has been bringing eggs for a month.",
                        "Say it, plainly, and ask Marl to stay for supper.", "Take the eggs and say thank you, and leave the rest for another year.", "Tell Marl you are sorry: the forge is where your heart is.")
                        .after(
                                new String[]{"* It takes you nine words, which is two more than you planned, and Marl laughs, delighted, and says that it was about time, the neighbours had a bet on, and nobody had given you less than another year. Supper is eggs. It is the best supper you have ever eaten.", "@Pia| I'm not going to say anything.", "@Tam| She's saying something."},
                                new String[]{"* Marl takes the thanks the way one takes weather, and the eggs go on appearing, and so does the long conversation about nothing. It's a kind of patience that neither of you would describe as courage and that is, you suspect, a lot like it.", "@Pia| Oh, for the love of iron."},
                                new String[]{"* Marl is kind about it, in the way of someone who has expected it and hoped otherwise. The eggs stop. The gate goes on not squeaking. For a month or two you feel quite noble, and then you start to feel like a person who has built a gate across your own road and lost the key."}))
                .done((c, st) -> { if (Chapters.flag(st, "marl") == 1) { Chapters.bond(c, st, "marl", 3); Chapters.wed(c, st, "marl"); } })
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("seven", "Number Seven").gap(6)
                .setup((c, st) -> Chapters.spawn(c, st, "seven"))
                .intro(
                        "* It is Pia who hears first. A carter in the market says that the Marshal's army has a new smith, a quiet one, who does the ward-cloth. He draws the wire and knits it, and the cloth takes the oath, and no blade will cut it. A man about forty. Lean. A crooked finger on the left hand, broken and set wrong.",
                        "* She goes white. She puts down her bowl. She says in a voice you have not heard from her since she was fifteen: 'Seven had a finger like that.'",
                        "@Pia| He went first. Before the rest. They said he'd gone home. I thought he was dead. I thought we all were, the nine of us, and I was the one who happened to get out.")
                .meet("seven", "Find the Marshal's cloth-smith, who may be Number Seven.")
                .outro(
                        "* He works in a lean-to behind the quartermaster's tents, over a vat of something that stinks of salt and old metal. He is forty, narrow, stooped, with the ward-wire wound round his forearms like bandages. He does not look up. When Pia says his number, he does not look up either, but his hands stop.",
                        "@Seven| Three.",
                        "@Pia| My name is Pia.",
                        "@Seven| Is it. That's a good one. I used to think I'd choose one, if I ever got out. Then I thought what's the use of a name where there's nobody to say it.",
                        "* He tells it plainly, over the vat, without turning. How he did not go home. How Voss kept him, because he was the best of them at the fine work, and how he told himself he could stop at any time, and how the years went by. How he made the weeping blades at last. All of them. How he has made the oath, since, and what it is: a trick of the wire, a knot that the cloth ties against any edge that was made to hurt. He says it with the flat accuracy of someone who has been over it a thousand times in the dark.",
                        "@Seven| It's a good knot. I'm proud of it. That's the worst part. I'm proud of it.",
                        "@Seven| It has a hole in it, if you want to know. It can't see a blade that wasn't made for anything. It reads the maker. If the maker didn't mean it, the cloth doesn't know what to do. I always meant to tell someone. There was never anyone to tell.")
                .outChoice(new Chapters.Choice("seven", "Number Seven made the weeping blades, and the oath, and has just told you its weakness.",
                        "Forgive him, and tell him the forge has a door.", "Turn him in to the League's magistrate.", "Ask for his help, and take it, and say nothing else.")
                        .after(
                                new String[]{"@Pia| We have a door. It's the front one. It's open.", "* He looks up at last. He has a long, grey, tired face. It does something complicated, and then goes still. 'I don't think I can,' he says. 'I'll think about it.' It is the longest answer of the whole conversation."},
                                new String[]{"* The magistrate is a dry man with a cold. He writes it down. He says that it will be a matter for the courts and that it will take some time. Pia says nothing on the road home. She keeps her hands in her sleeves, and counts under her breath, and loses count, and starts again."},
                                new String[]{"* You take the knot's whole pattern in your head, a long strange diagram of the oath, and you give him your hand. It is a business arrangement. He takes it as though it were a hot coal, and holds it a moment too long."}))
                .outro(
                        "?seven=1|* He comes to the forge a year later with a bundle and a pot of salve for his hands. He stands at the door of the shop until Tam, who is nineteen and has a moustache, says 'Are you coming in or not?'",
                        "?seven=2|* Number Seven does not come to the trial. He is found in a cell in the morning, sitting up with his back to the wall, with a face of enormous calm. Pia goes to see him once. She does not tell anyone what he says.",
                        "?seven=3|* Number Seven dies in the spring, quietly, of a sickness in the lungs that the salt had been working at for years. He leaves Pia a note. It reads: THREE. I'M SORRY ABOUT THE NUMBER. IT WAS A BAD ONE. The note is in the shop, nailed above the quench barrel, where the water is.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("bill", "Aldric's Bill").gap(4)
                .intro(
                        "* The knight arrives at the forge on a day of high wind, on foot, with his coat patched at the elbow and a purse in his fist. He has aged ten years in three. There is a streak of white in his hair that was not there when he came to borrow a sword. He does not take the bench. He stands in the middle of the floor, in front of the anvil, with his hat in his hand.",
                        "?lend=1|@Aldric| I have come to pay for the sword. I know you didn't charge. I know there was no price. That's why I have to. I've been to every village in the east, and I've asked, and I've counted. Forty men. I have the names. I've paid the families what I could. This is the rest.",
                        "?lend=2|@Aldric| I have come to say you were right. I know you didn't ask. I took your answer east, and I thought about it for two years, in the mud, and I have come to say it. I should have been the sort of man who didn't need to be told. I want you to know that I am trying to be.",
                        "?lend=3|@Aldric| I have come to thank you for the road. I know you will say you did nothing. You walked beside a man who was lost, and did not tell him which way, and let him find out. I did not understand it till I'd lost my company. I understand it now.")
                .wait(1)
                .outro(
                        "* He will not be argued with. He has a speech, and he gets to the end of it without a catch, in the manner of a man walking over a plank. When he has finished, he lays the purse on the anvil. It is small. It is clearly all he has. Pia, behind the bellows, looks at it, and at him, and then at you.",
                        "@Pia| Keep it.",
                        "@Aldric| I can't.",
                        "@Pia| Then put it in the box by the door. It's for the next one who wants to borrow it and doesn't know what to say. We keep a box. It has forty pennies in it. Every one of them was somebody else's apology.",
                        "* He puts the purse in the box. It makes a very small sound. He stands there with his hand still on the lid, and Tam, who is at the back, with a hammer on his shoulder, says in his new gruff voice that the soup is on, and there is a place. Aldric looks at the floor. He says that he would like that very much.",
                        "* He stays for supper, and for the night, and for the winter. He is no good at the bellows. He is, to everyone's surprise, a fine hand at the accounts.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("letters", "Two Letters, Again").gap(4)
                .intro(
                        "* The post comes on a Tuesday and brings two letters, one after the other, in the same week, from opposite ends of the province, and for a moment you feel the old sick lurch of the day the orders came. But these are not orders. They are in a hand you do not know. They are both addressed to the smith who made Evenfall, and they are both from women.",
                        "?side=1|* The first is from a League farmwife, a widow, whose husband carried a League spear at Greywater. She writes that she is told it was one of yours. She writes that she did not know whom to write to. She does not ask anything. She says only that he held it all day, and that she would like the smith to know it did not fail.",
                        "?side=2|* The first is from a Dukes' farmwife, a widow, whose husband carried a pike at Greywater. She writes that she is told it was one of yours. She writes that she did not know whom to write to. She does not ask anything. She says only that he held it all day, and that she would like the smith to know it did not fail.",
                        "?side=3|* The first is from a woman in the marsh country. Her son was a pikeman and a stranger's son was a spearman. She has heard, from a clerk, that the heads that broke were made on one day from one ingot. She writes that she would like to know which was which. She knows that it cannot be told.",
                        "?side=4|* The first is from a woman whose husband carried neither side's spear and was killed by a League one anyway. She writes that she has heard you armed no one. She wants to know, plainly, what difference it made. She does not say it as a reproach. She says it as a sum.")
                .wait(1)
                .outro(
                        "* The second letter is from the other side of the war. It is from a woman in the north who has heard that the smith will make nothing for the army. She writes to say thank you. She writes that her boy is nineteen and in the League, and she has been unable to say, for two years, what she wanted to say about it. She says that she is told that there is a forge where a thing is made for nothing. She says she would like to see it, once, before she dies.",
                        "* You read both of them twice. You read them aloud to Pia, who sits on the anvil with her knees up and her chin on her fists. When you have finished she says nothing at all for as long as it takes the fire to settle. Then she gets off the anvil, and finds a pen, and sits down at the bench.",
                        "@Pia| I'm going to answer them. You can write what you like. I'm going to tell them the truth, which is that we don't know. I'm going to tell them it was a good socket, and that the man who made it didn't sleep for a week, and that we think about it.",
                        "* She writes for an hour, in her square big hand, with the tip of her tongue showing. When she has finished she reads it back. She says: 'It's not good.' You say that it is the only kind there is. She folds both letters, one into the other, and puts them in the box by the door where the pennies are.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("marshal", "The Marshal of Salt").gap(12)
                .setup((c, st) -> Chapters.spawn(c, st, "ketter"))
                .intro(
                        "* The Marshal's camp is in the old salt flats east of the river, a city of grey tents and grey faces, and it is quieter than a camp ought to be. The soldiers have the look of people who have stopped expecting to lose and have not noticed what they replaced it with.",
                        "?lend=1|@Aldric| They'll take you in. I told them you might come. I was in the wrong, in the east, and I have not stopped being in the wrong, and I would like to be of use. I will stand outside the tent. I will not be in the way.",
                        "?lend=3|@Aldric| Not a blade between us tonight, you understand. You walk in. I'll stand outside the tent. If he has the sort of ward I've been told, then nothing I do will matter, so I intend to be useful by standing very still.",
                        "* He is sitting in the middle of the camp in a plain chair, and he is rising as you come in. He is sixty and large and very polite, with a grey beard and the clean hands of someone who has been rubbing salt into them for years.",
                        "@Ketter| The smith. I wondered when you would come. I have sent you a letter, and a cloth, and a number of unfriendly men, and I am pleased that none of them took. I have no wish to quarrel. I would like you to make me a thousand blades.",
                        "@You| You know what I'll say.",
                        "@Ketter| I do. I'd like you to say it to my face, so that I may enjoy the sound of someone who thinks they can afford honesty. Then I shall be obliged to see what you have brought.")
                .defeat("ketter", "Face Marshal Ketter, and win.")
                .outro(
                        "?dead.ketter|* The blade goes in without any sound. He looks surprised, and then he looks down, and then, with the air of a man who has just been handed a bill he knew about, he nods. 'That is quite well made,' he says. 'Whoever made it did not mean it.' The salt falls out of his hands like a handful of sand, and he is gone before it has finished falling.",
                        "?!dead.ketter|* He sits down on the ground, quite suddenly, the way an old man does who has lost his chair, and looks at his own hand where the ward used to be. He is not bleeding. There is nothing to bleed. The sword has simply taken away the part of him that made him stand up in the morning. 'I think,' he says, 'I would like to go home. I had a farm, once.' You let him go. He does.",
                        "* Without the Marshal, the camp comes apart like wet paper. Nobody fights. They sit on the ground in the grey, and look at each other, and begin, one at a time, to stand and walk west.",
                        "@Pia| Is it over?",
                        "@You| I think it's just over for him.")
                .pay(5000, 0, 0, 1, 0, 1);

        d.chapter("price", "What It Cost").gap(10)
                .intro(
                        "* The war ends the way wars do: by not being continued. The treaty is dull, and signed, and nobody reads it. The town holds a feast. Marl's eggs are everywhere. Tam, who is twenty, gets very drunk for the first time and tells a story to a roomful of strangers about the day a blade turned a cloak to two halves. By the end of the evening it is a legend and he is the hero of it, and you let him be.",
                        "* It is the kind of night that you only recognise as happiness in memory. On the way home Pia walks beside you and says nothing, and then says, to the dark road: 'Do you ever wonder if it's meant to be this good?'")
                .wait(4)
                .outro(
                        "* The woman arrives in the spring, in a plain coat the colour of iron, with a staff she does not appear to need. She walks through the queue, which is shorter now, and nobody stops her. She does not stand in line. She simply steps into the doorway of the forge and the light changes, as if a cloud had decided to take an interest.",
                        "@Woman| You are the one with the sword. Or perhaps you are the one who made it. I find I have never needed to know the difference. I have a question for you, but I should like to ask it when the war is properly over, and I think that is going to be some years yet.",
                        "@Pia| What war? We've just had one.",
                        "@Woman| Yes. That was practice.")
                .pay(2000, 0, 0, 1, 0, 0);

        d.interlude(2, "Practice",
                "* The sword hangs on the wall over the forge, in a new sheath that thinks twice. Children are allowed to look. They always ask what it is for. By now there is a regular answer, which Tam gives, in the voice of a man who has told it for years and still enjoys it.",
                "?lend=1|* Aldric Vane comes once a year, on the same day, and sits for an hour on the bench by the hearth. He never says why. He never touches the sword. Pia brings him tea, and he drinks it, and goes.",
                "?lend=2|* Aldric Vane died in the east some years later, in a battle that was not a victory. You heard it from a trader. You hung a horseshoe over the door for him, which is the sort of thing he would have found embarrassing.",
                "?lend=3|* Aldric Vane never lost the habit of standing very still in a doorway when something important was happening. You find it comforting. He found it, he says, a kind of cowardice. He is wrong, but it takes him a long time to say so.",
                "?e.spouse|* Marl keeps chickens in the back yard now, and has opinions on the forge's drainage. Some evenings there are eggs for supper.",
                "?!e.spouse|* Marl still brings the eggs. The gate still does not squeak. Neither of you has said anything about it, and neither has changed anything, and neither of you is in a hurry.",
                "* Pia has two apprentices of her own. The younger one is a girl of fourteen who counts things under her breath, and does not know she does it.");

        // ================================================================== SEASON 3
        d.season(3, "The War Forge");
        d.cast("iron", "Elder", "Iron").she().keep().trait(Character.Traits.Blacksmith).side("Good").lvl(5).pot(10).age(88).at("here").rel(2);

        d.chapter("lamp", "The Lamp of Iron").years(4)
                .setup((c, st) -> Chapters.spawn(c, st, "iron"))
                .intro(
                        "* She comes back, as she said she would, in the same plain coat and with the same staff, and she has aged not at all. She takes the stool by the hearth. She asks for water. She drinks it. She says her name is Iron, which is not a name so much as a job, and that she is one of nine, and that you will know the nine if you ever go to the place where they live.",
                        "@Iron| There is a war. Not that one. A larger one. It is not fought here. It is fought outside, and the man who owns this place is fighting it, and he is losing, and the nine of us have been asked to find the means of not losing. I am the means of the iron. I'm told you can make a blade that cannot be used for a lie.",
                        "@Iron| I want a thousand. For soldiers. In the Outside. They will go through the Seam by night, and they will be strong, and they will be afraid, and they will need something in their hands that does not turn on them.")
                .meet("iron", "Hear what Elder Iron has come to ask.")
                .outChoice(new Chapters.Choice("deal", "A thousand blades, for a war outside the world.",
                        "Refuse. Never forge what you can't carry.", "Agree, and ask no questions.", "Agree, on one condition: you will make none for a person you have not met.")
                        .after(
                                new String[]{"@Iron| I thought you might. It is, after all, why I came to you and not to a man who would have said yes. Then let me ask the other thing. Would you meet them? Just meet them. One at a time. And decide, each time, whether you can carry what you are making.", "* You think of your father's hands shaking. You think about it for the length of a very long breath."},
                                new String[]{"@Iron| That is not the answer I wanted. I should have preferred the first. I will ask once more, in a month, and I will be asking a different man.", "* She leaves. It takes you a week to understand what she meant."},
                                new String[]{"@Iron| Good. That is the answer I wanted, and I hoped not to hear it. They will come to you, one by one. They will have names. You may refuse any of them. I will not argue.", "* It is the kind of arrangement that only works if both parties are honest. You find, to your surprise, that you trust her."}))
                .pay(500, 0, 0, 0, 0, 0);

        d.chapter("first", "The First Three").gap(6)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* They come on a Tuesday, in a line, with the particular patience of people who are used to being told to wait. The first is a corporal named Besh, who laughs at everything and does not appear to know that he is doing it. The second is a woman of sixty-five called Dame Orsolya, the last of a family that you are fairly sure used to be a good deal larger. The third is a boy called Fig, who is sixteen and looks twelve, and who has been told to say yes to everything and is therefore terrified.",
                        "@Besh| I'd like it to be a short one. I'm not tall. I don't see the point of a sword that gets in my own way.",
                        "@Orsolya| Heavy. I have a bad wrist, but I intend to use the other one.",
                        "@Fig| Whatever's... easy. If it's no trouble.")
                .made(3, "Make a blade for each of the first three: Besh, Dame Orsolya, and Fig.")
                .outro(
                        "* Besh's is short and plain and has a handle that fits his fist the way a glove fits, and when he takes it he does not laugh. He turns it over. He holds it up to the window. 'Well,' he says, 'that's the first time anybody asked me what I wanted.' He bows to the sword, not to you, and you take it as the compliment it is.",
                        "* Dame Orsolya's is heavy, as she wanted, and she tests it twice against the air, and nods. She presses a coin into your hand, and you try to give it back, and she says that she would like to pay for it, for once in her life, as a person who owes nobody. It is, you think, the most expensive coin you have ever held.",
                        "* Fig does not say anything. He takes the sword in both hands. He looks at it until Besh clears his throat. Then he says, low, as if the sword might hear, 'It's lighter than I thought.' Pia is standing at the door with her arms folded. She looks at the ceiling for a moment, and then at you, and you both know what she is going to say. 'Don't,' you say.",
                        "@Pia| I didn't say anything.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("names", "The Names on the Wall").gap(6)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* Pia chalks the names on the wall of the forge, next to the door, because she cannot stand the sight of a list in a ledger. Each is written out in a big square hand, with a date, and the thing they asked for: BESH, SHORT AND PLAIN. ORSOLYA, HEAVY, BAD WRIST. FIG, WHATEVER'S EASY. She leaves a gap under each so that when they come back there will be a place to say how it went.",
                        "* By the second week the wall is full and she has started on the beams. The soldiers read it when they come in. They run a finger along the chalk, looking for themselves, and when they find themselves, they stop talking. It has an odd effect on a person to see their name on a wall with a thing they asked for beside it. It is like being asked to a dinner.")
                .made(4, "Make four more blades, each for a name already on the wall.")
                .outro(
                        "* Thessaly comes on a Thursday, a lean, brown, nervous woman with a stutter that gets worse the closer she gets to the word she wants. She wants a knife with a hook on the back, for hauling. She asks for it in a quarter of an hour. Nobody speaks for the whole of that time, not even Tam, and when she has finished she looks round at them with an expression of such wild relief that Pia has to turn to the barrel.",
                        "* Lord Pell is not a lord. He tells you at once, in the doorway. 'The name's for luck,' he says. 'My mother thought it was a title.' He wants something that will look good. You make him a plain one, and he hangs it from his belt, and walks out of the shop an inch taller.",
                        "* The twins come together. One wants a sword and the other wants whatever the first one wants. They have been told that only one of them will be allowed to go, and each has privately promised the other to say it was them, and neither has the heart to say so first. Pia looks at them for a long minute. Then she writes both names on the wall, side by side, in a single line, and under it, in smaller letters: ASK THE OTHER ONE.",
                        "@Tam| That's not fair.",
                        "@Pia| No. But they can fight about it somewhere else.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("quench", "The Quench").gap(8)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The orders keep coming, and so do the soldiers, and the forge fills up with the clean, odd smell of a place working at the edge of its ability. Pia has closed her own shop for the season. She walks in one morning with an apron over her arm and sets it on the hook by the door, next to your father's hammer, without any discussion.",
                        "@Pia| I'll do the quenching. I'm better at it than you are, and you know it. But I want one rule. I won't make one for anyone who hasn't asked. Not one. Not for a rank, not for a list, not for the sort of man who says it's an honour. If they ask, I'll do it with my whole heart. If they don't, I'll stand on the road and tell them to go home.",
                        "@You| That's my rule.",
                        "@Pia| Then it's the family rule. Good. I'd hate to have to invent another.")
                .made(4, "Make four more blades with Pia at the barrel, for four who asked.")
                .outro(
                        "* There is a rhythm to it, after the first fortnight. Pia stands at the barrel with her hands cupped round the bucket and listens, and when she says now, you lower the blade, and the water goes quiet. Tam holds the lantern. Marl feeds all three of you at odd hours and complains that nobody eats at a proper time.",
                        "* The ticking has returned. It is faint, and it is steady, and there is a second note under it now that you recognise at last, and cannot believe you did not notice sooner: it is the rhythm of Pia's counting. She has been doing it for years, under her breath, and it has gone into the iron.",
                        "@Pia| What?",
                        "@You| Nothing. Keep counting.",
                        "@Pia| I wasn't counting.",
                        "@You| I know.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("palace", "The Pill Kitchen's Neighbour").gap(10)
                .intro(
                        "* The Lamp of Iron has a workshop, and it is under the Palace of the Oldest Order, in a basement so old that the walls have started to keep their own time. She invites you for a night, to see where the blades go. She does not say it as an order.",
                        "* You arrive by the front gate, which is a disappointment: it looks like any other gate. There is a bell. There is a thin young Wick sweeping the steps, who looks up, gasps, drops the broom, and runs inside. She comes back a minute later with a very senior woman who inclines her head and says nothing, and then you are being led down a stair.",
                        "* The workshop is a long low hall with nine anvils in a row. Eight are empty. At the ninth sits an ordinary man of no age, in a plain apron, with a piece of iron in his tongs and an expression of intense concentration. He is not doing it right. He is doing it with great care.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and see where the blades go.")
                .outro(
                        "@???| I'm not allowed to say who I am. That one I can keep. I'm allowed to say I like this, which is one of the things I'm best at, which is not very good. Would you show me how to hold it? I've been hitting it at the wrong end.",
                        "* You show him. He is a quick pupil, in the sense of someone who has been forced to learn a great deal very fast, and has not had time to be bad at anything for long. The iron, when he strikes, gives off a faint, bright sound that is not like any of the ones you know.",
                        "@???| There are one thousand and four names on the list. I read them all, every night. They are the people who said yes. I'm not allowed to say what it's for. I'm allowed to say it's for them. I find that is not the same thing. It matters more than I thought it would.",
                        "@You| Do you know what it's like to carry one?",
                        "@???| No. I've never carried anything. I am the thing that gets carried. It is rather lonely, I think, though I do not have much to compare it with.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("home", "A Thousand Eggs").gap(6)
                .intro(
                        "* The forge has become a kind of house. There are people sleeping in the loft. There are people who have come to ask about a blade and stayed three days because the weather turned. There is a woman who does the soldiers' mending in the corner by the fire, and a boy who sells pickled onions through the window, and a stray goat that Tam has named Administrator.",
                        "* It is held together, as these things are, by one person. Marl comes at dawn every morning with a basket. They have not moved in. They have not said anything about moving in. They simply arrive, and cook for twenty, and leave at dusk, and the sums of it have long since stopped being a matter of eggs.",
                        "@Marl| I don't know what's wrong with the lot of you. It's a forge, not a hospital. A hospital would have chairs.")
                .wait(2)
                .outro(
                        "* Pia and Tam have a row at the end of the second week. It is about the quench, and then about the loft, and then about whether he ought to be allowed to go with the company at all. By the third quarter of an hour they are both shouting in the yard with the goat looking on. It ends, as it always does, with Pia saying something terrible in a cracked voice. Tam goes very white. Neither of them knows how to take it back.",
                        "* Marl finds the pair of them an hour later on opposite sides of the barrel, not looking. There is a long pause. Then they are given a plate of eggs each, without comment, and told to eat, and when they have eaten, a mug of tea each, and told to drink.",
                        "@Marl| I've buried a husband and a wife and a good dog. I know what it looks like when two people are afraid for each other. It looks exactly like this. Now say sorry in whatever order you like. I'm going to look at the goat.",
                        "* They say it. It comes out as a sort of mutter, from both ends of the barrel at once, in no order at all, and it is the most graceful apology you have ever overheard.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("tamgo", "Tam Asks").gap(6)
                .intro(
                        "* He has been practising it for a week, and you can tell. He comes into the forge at the end of the evening, when Pia has gone and the fire is low. He stands at the end of the bench with his hands behind his back and his feet apart, like a boy giving a recitation.",
                        "@Tam| I want to go. With the next company. I've done the arithmetic, and it's four years since I've been to anywhere, and I think I'm better than the work, and I'd like to carry one of them. Not because I want to kill anyone. I want to be where it is. I want to see what it's for.",
                        "* You say nothing. You look at the boy who arrived with a sack and a spearhead, and who has become a man in the way that boys do: when you are not paying attention.")
                .wait(1)
                .outChoice(new Chapters.Choice("tamgo", "Tam wants to go with the next company.",
                        "Forbid him. You cannot carry that.", "Let him go, and forge him a blade he can carry.", "Say yes, and go with him as far as the Seam.")
                        .after(
                                new String[]{"@Tam| You can't.", "@You| I can. I'm not going to. I'm only saying it's the one time you'll hear it from me. If you go anyway, I will not stop you at the gate.", "* He stays. It takes him a month to forgive you. It takes you the rest of your life to be sure you were right."},
                                new String[]{"* You make it that night, on the cold hearth, with a fire that takes three tries. It is the plainest blade you have ever made. It is exactly the length of his arm. You do not put a mark on it. He will know it, and so will you.", "@Tam| It's lighter than I thought.", "@You| It is. They always are."},
                                new String[]{"* You walk with him to the Garrison of the Crossed, which stands on a bare plain at the edge of the world and does not look like anything. Its gates are open and nobody is at them. There is a line of people outside, patient, in the early light. Tam looks back once and lifts his hand. You are surprised to find yours up too."}))
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("hundred", "The Hundred").gap(10)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* There is a month in which nothing happens but work. The forge never goes cold; the door is never shut. People come in the mornings and leave at night, and between those hours the sound in the place is nothing but iron. Marl brings food at odd hours and puts it where it can be reached from the bench, because nobody will stop. Pia has taken to singing, tunelessly, to the barrel.",
                        "* You make a hundred. Not literally: you make as many as the month will bear, and it is not a thousand, and nobody says so. You learn names the way you learned the bellows: by repetition. Besh, Orsolya, Fig. A woman called Thessaly with a stutter. A man called Lord Pell, who is not a lord. A pair of twins who have each been told that the other will not be allowed to go, and who have each refused to tell.")
                .made(6, "Work through the month: make six more blades for six more names.")
                .outro(
                        "* On the last evening a messenger comes back, mud to the knees, from a place the maps have not got to yet. He has a bundle in his arms, and he puts it down on your bench. It is a blade. It is notched, along the edge, in nine places. It is Besh's.",
                        "@Messenger| He said to give it back. He said it held. He said to tell you he doesn't need it any more. He said to say thank you for asking what he wanted.",
                        "* You hold the sword until the fire burns low. You find the nine notches with your thumb. Each of them, you know, was a thing that happened to a man who might have been somebody else's brother. You find that you can carry it. This is not what you expected. You thought it would be heavier.")
                .pay(5000, 0, 0, 1, 1, 1);

        d.chapter("anvil", "Elder Iron's Anvil").gap(3)
                .intro(
                        "* She takes you down in the evening, by herself, without being asked. Not to the forge. To the Garrison of the Crossed: a plain, flat and grey, in a country you do not know, with an empty fortress at the far edge and its gates standing open. The Door is a long day's walk from here, under the Palace. This is where the called wait for their turn. A line of men and women stands outside the gate, in good boots, with their blades at their sides, quietly in the rain.",
                        "@Iron| This is where they wait before they go through. It is not a grand place. The Gardener does not care for grandeur. It is only a gate in a wall. I have stood here for every company. I have given each one a word. I could not give them anything else.")
                .wait(1)
                .outro(
                        "* She walks along the line. She knows them by name. She stops in front of each, and says something short, and moves on. A boy of sixteen gets a pinch on the cheek. A woman of seventy gets a bow. A big man in a leather jerkin, who is crying without noise, gets a hand put flat on his chest, over the heart, and held there while he breathes.",
                        "* When the last has gone in at the gate, she lowers herself on to a block of stone beside it, slowly, as if every joint were a separate effort. Her hands, in her lap, are scarred in a way you have seen only on the very best smiths, and on soldiers. There are the flat white tracks of old burns and old cuts, and something older that looks like the print of a hot chain.",
                        "@Iron| I forged the first nine blades myself, you know. The first nine, for the first nine of us. I gave every one a weight it had to carry. I have not touched a hammer since. I thought that was a kind of wisdom.",
                        "@Iron| Then I met a smith who would not make a thing for a person he had not met. I thought: oh. It is not wisdom. It is just not looking. I have been not looking for a very long time.",
                        "* She lifts her face to the rain. It is the face of someone who has been very tired for a very long time and has decided, for the length of a breath, not to be.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("needle", "The Hundred and First").gap(3)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The hundred and first soldier is not a soldier. She comes in out of the rain on a Wednesday at dusk with a satchel and a lantern, a small brown woman in a grey coat, with her sleeves pushed up and a smell of vinegar and boiled linen. Her name is Dr. Perin. She is forty. She is a field surgeon, attached to the third company, and she has been told by Elder Iron that she may have a blade, and she has come to say that she doesn't want one.",
                        "@Perin| I'm sorry. I know it's an honour. I know you've been up since four. I'd be a liar if I said I could carry a sword. I'd drop it. I'd cut my own foot off. But I'd like to ask for something, and I've been rehearsing the words for a week, and they sound ridiculous.")
                .made(1, "Make the hundred and first piece, for a woman who did not ask for a blade.")
                .outro(
                        "@Perin| I want a needle. A good one. Curved. With a long eye, strong enough to go through a leg. The needles I've got keep breaking. They're thin, and cheap, and made by people who've never had to put a man's arm back on in the dark.",
                        "* You make it. It takes a whole night. It is the smallest thing you have ever forged, no longer than your finger, curved like a new moon, with an eye the width of a hair. You do it by lantern and by feel, with Pia holding the work in the tongs and Tam holding his breath. It is the hardest piece of work you have done since the sword. You do not know why.",
                        "* She tests it on a scrap of belt leather. It goes through like water through a sieve. She runs her thumb along the curve, and holds it up to the lantern, and the light comes through the eye.",
                        "@Perin| That's it. That's the one. Oh, you beauty.",
                        "* She puts it in a small tin box. She shuts the lid. She does not say thank you. She says that she will count how many it saves, and send you the number. She says it is a thing she does. She says she has a book.",
                        "* She goes out into the rain with the lantern. At the door, Pia murmurs, 'Number one hundred and one,' and writes it on the wall. Beside it, in the space where the thing asked for goes, she chalks one word: NEEDLE.")
                .pay(300, 0, 0, 1, 0, 0);

        d.chapter("wrist", "Orsolya's Wrist").gap(2)
                .intro(
                        "* She comes back. It is the second month of spring and the road is soft. The Dame comes up the hill on her own two feet, her bad wrist strapped to her chest and the heavy blade slung on her back. She has been through the gate. She has been, she says, a long way. She has come, in her own words, to complain.",
                        "@Orsolya| It is too heavy.",
                        "@You| You asked for it heavy.",
                        "@Orsolya| I did. I was a fool. I asked for what I wanted to be, and not for what I was. Sit down. I shall have to say something I do not enjoy.")
                .wait(1)
                .outro(
                        "* She lays the blade on the bench. It is notched. There is a gap in the edge the width of a thumb, and a long scratch across the flat where it turned a spear. She turns it over. There is a dent in the pommel the shape of a skull, and she runs her thumb across it with the tenderness of someone touching a sleeping dog.",
                        "@Orsolya| I used the other hand. Just as I said. For three weeks. And then in the fourth, on a hill, in the rain, with my bad one on my chest, I could not lift it, and I put it down, and I took up a stick off the ground. And I fought with the stick. I want you to know that it was an excellent stick.",
                        "* She tells it plainly. She is a soldier of the old kind: she has no wish to be admired. She says that the stick was ash, and the length of her arm, and had a knot at the top, and that she broke a man's wrist with it, and that she would like another. She says she would like a stick.",
                        "@Pia| We don't make sticks.",
                        "@Orsolya| You make what's asked for. It's your rule. Don't tell me you've no pride in the principle.",
                        "* You make it. It takes an afternoon. It is the strangest thing that has come out of the forge: a plain rod of steel, hand-length and a half, with a knot of folded iron at the top, and a leather grip. It looks like a walking stick for a mule. She takes it in her good hand, and hefts it, and her whole face changes.",
                        "@Orsolya| That is the first thing anyone has made for me, in sixty-five years, that was the right size.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("carry", "What You Can Carry").gap(8)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* There is one blade left to make, and nobody has asked for it. It is on the order in Iron's hand, at the bottom of the list, in the space where a name should be: she left it blank, and she has told you that she will not fill it.",
                        "@Iron| It is for you. If you will take it. The nine of us went through the Seam, one time, long ago, and each of us was given a blade, and each of us regrets what we put into it. I would like to spare you that. I can't. I can only tell you that it is a choice, and that you will be making it in iron.")
                .made(1, "Make the last blade: the one you will carry.")
                .outChoice(new Chapters.Choice("edge", "What will you put into the last blade?",
                        "Mercy: let it dull in the hand of anyone who strikes for pleasure.", "Victory: let it cut whatever stands against the people who asked.", "Nothing: let it be made for nothing, the way Evenfall was.")
                        .after(
                                new String[]{"* You listen to the iron. It tells you where it wants to go. You do not hit it. It is the slowest thing you have ever made.", "@Pia| It's going to take it a while to be a sword.", "@You| It will."},
                                new String[]{"* It comes quicker than the others, which should worry you, and does, a little. The edge is bright and cruel and exactly as sharp as it needs to be.", "@Pia| You're allowed to want it to win.", "@You| I know. That's what's frightening."},
                                new String[]{"* You put nothing into it. You are surprised how hard that is. You hold your hands over the iron, and you think of an empty room, and a clock in a small house, and the blade comes out of the quench so quiet that Pia has to ask if you are finished."}))
                .outro(
                        "?edge=1|* The blade is the colour of a pond on a still morning, and it hums, faintly, when you set it in the sheath.",
                        "?edge=2|* The blade is the colour of a coal in the dark, and the edge makes a small sound against the air, as if it were counting.",
                        "?edge=3|* The blade has no colour you could name. It looks like a piece of the evening.",
                        "* You hang it on the hook by the door, beside your father's hammer. It does not look out of place.")
                .pay(2000, 0, 0, 1, 0, 0);

        d.chapter("even", "Even").gap(4)
                .intro(
                        "* The Gardener comes himself after all, on a morning of first frost, in the shape of an old man in a patched coat with a bag of apples. He does not announce himself. He walks in at the door, nods to Pia, nods to Marl, and sits down on the bench by the hearth and starts peeling an apple with a small, blunt knife. He does it very slowly. He does not seem to be in any hurry.",
                        "@Old Man| I wanted to see the place that made them. I have the numbers, of course. I have all the numbers. It is not the same.",
                        "* He has the kind of face that you think you could describe, and that is gone from your mind when he turns away. His hands are the hands of a man who has held a great many things for a very long time.",
                        "@Old Man| They tell me you wouldn't make one for a person you hadn't met. I want to say that I would not have asked. I would have asked someone else, and the someone else would have said yes, and the blades would have been made, and nobody would have known the names. I want you to know that I know the names.")
                .wait(2)
                .outro(
                        "* He lays the last apple on the bench. He looks at Evenfall on the wall. He looks at the hook by the door, and the blade that hangs on it, the one you made for yourself.",
                        "@Old Man| I am a long way from fair. I have asked a great deal of a great many people, and I have not always known what I was asking. I would like to repay a part of it. I cannot do much. I can do something small.",
                        "* He puts out a hand and touches the blade, and it rings, very softly, like a cup struck with a spoon. Something goes through the forge: a feeling, more than a sound, like the first warm day after a long winter. The fire flares up of its own accord and settles. The ticking, which has not been heard since the Marshal, begins again, and this time it does not stop.",
                        "@Pia| What did you do?",
                        "@Old Man| I gave it a little of what I have. I'm told that is how it is meant to be done.",
                        "* When you look round, he is gone. There is a half-peeled apple on the bench, with a single curl of skin hanging from it, exactly as if he had stepped out for a moment. You eat it, because it seems rude not to.")
                .pay(50000, 3, 0, 6, 3, 3);

        d.epilogue(
                "* The sign over the forge still says {last} and Child. You repainted it again in the spring, and this time you added a small line underneath, in Pia's hand, which says AND PIA, AND TAM, AND EVERYONE ELSE WHO KNOCKED.",
                "?tamgo=1|* Tam stayed. He took over the bellows, then the forge, then the sign, and he has the decency to pretend that he is only keeping it warm.",
                "?tamgo=2|* Tam went. He sends word through the Seam, now and again: a note, a drawing of a bird. The blade you made him is on the wall of his room in the Outside, and it has never once been drawn.",
                "?tamgo=3|* Tam went through the gate, and came back through it after a year, and does not talk about it. He keeps a notched blade by the bed and a box of apples at the foot of it, and he is, Pia says, a better smith than the two of you together.",
                "?edge=1|* The blade by the door dulls itself on a stone whenever you leave it near anyone who is angry. Visitors say it is the strangest thing in the forge.",
                "?edge=2|* The blade by the door is the best you ever made, and you do not take it down. It is a good thing to know that you could.",
                "?edge=3|* The blade by the door has no name, and no mark, and it makes you feel as though the forge itself were listening. Children are allowed to look at it. They never ask what it is for.",
                "?e.spouse|* Marl keeps the accounts and the chickens, and tells people at dinner that the forge was a bad bet but the smith was a good one. You have never been sure which of you is meant to be the joke.",
                "?!e.spouse|* Marl still brings the eggs. The gate still does not squeak. You have both decided that this is a kind of answer.",
                "?dead.ketter|* The salt flats are green now. Somebody planted barley.",
                "* Evenfall hangs on the wall over the forge, in a sheath that thinks twice. Children are allowed to look. When they ask what it is for, somebody says: nothing. They look let down. Then somebody else always adds that this is the best thing about it.");
        TaleSmithMore.add(d);
    }
}
