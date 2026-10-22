package Game;

/** The Blacksmith's Child, season 4 (2026-10-20): "The Mending Post" (the working voice, an anthology). Blades come back from the Outside by a carrier nobody has seen, to be mended, each with a note from whoever carried it.
 *  The forge learns what its work was for. Pia comes into her own. Choices: broken (what is done with a blade that cannot be mended), forge (what becomes of the forge). Flags: broken, forge, post.
 *  The Door season (StoryDoors) comes after. */
final class TaleSmithMore {
    private TaleSmithMore() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "broken", "forge", "post");

        d.interlude(3, "The Mending Post",
                "* The forge runs on. The sign has four names on it and a fifth, in a child's hand, that nobody will paint over. Pia has her own hammer now, with her own mark on the head. The blades go out into the Outside, a hundred of them, and a hundred and one.",
                "* A parcel arrives on a Tuesday in brown paper, tied with a bit of green cord. There is no stamp. There is no carrier. It is on the doorstep when you open up. It smells of smoke and rain and somewhere far off.");
        d.season(4, "The Mending Post");

        d.chapter("post", "The Post Comes").years(3)
                .major(5)
                .intro(
                        "* There is a blade in the parcel. It is a plain broad-bladed sword, heavy at the hilt, with a nick in the edge as long as a thumb. You unwrap it on the bench. A note is tied to the guard with the green cord. The handwriting is nervous and clean. It says: This is one of yours. It has done well. It has a chip. We have no smith. Would you?")
                .wait(4)
                .outro(
                        "* You take it to the light. The nick is clean, a bite in the steel, as if something had set its teeth. You run a thumb along the edge. There is a faint warm tingle in the metal, and under it, very far off, like a voice heard through a wall, the clash of other blades. Pia stands at your elbow in her leather apron, with soot on her nose. She says she knows the work. She says it is yours, from the second batch, the one you made in the winter you had the cough.",
                        "@Pia| It came back. That's the first. Do you know how many have gone through? A hundred and one. We never thought any would come back.",
                        "* You put it on the anvil. You fire the forge. The heat rolls out into the shop and the walls glow orange. You take up the small hammer. For the first time since you were a child, you are afraid of a piece of work.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("patience", "A Sword Called Patience").gap(8)
                .setup(TaleSmithMore::mark)
                .intro(
                        "* The sword is called Patience. You learn this from the second note, which arrives with the sharpened edge. It is signed by a woman called Orsolya, who carried it for nineteen years in the Gardener's line. She writes that it is a good name. She writes that it was given by a boy of sixteen, on his first night, who said it was the thing he would need. He did not live to need it. She has carried it for him.")
                .made(6, "Mend the first blade: make six pieces of the work.")
                .outro(
                        "* It takes a week to draw out the nick. You work it with the fine file and the stone, and at the end you quench it in a bucket of snowmelt from the hills. It sings as it goes in, a single high note, like a nail on a glass. When it comes out the edge is as bright as a mirror and there is not a mark on it.",
                        "* You pack it in straw. You write a note of your own, on the back of a bill, in pencil, and tie it to the guard with the green cord: It is mended. It was a good name. You put the parcel on the doorstep at dusk, as the first had come. In the morning it is gone. There is a single feather on the step, a long grey pinion with a white tip. Nobody has an idea where it came from.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("small", "The Girl Who Wanted a Smaller Sword").gap(8)
                .intro(
                        "* The third parcel holds a very small sword, no longer than a forearm. It has a bright worn grip wrapped in blue cloth. The note is on a page torn from a child's copybook, in a round hand with many blots. It says: Please make it fit. It is too heavy. I am ten. I was told I could not be a soldier. I said I would carry it for my brother, who is dead. They let me.")
                .wait(4)
                .outro(
                        "* You read the note three times. You put the little sword on the bench and look at it for ages. It is the best thing you have ever made, and you did not make it. It is an old blade, a short sword from the early batches, which somebody has cut down. The edge is dull. The tang is bent. It has been used, for a year, by a child, and it has not once failed her.",
                        "@Pia| You can't make it lighter. It's a sword. It has to weigh what it weighs.",
                        "* You can, though. You think about it all night. In the morning you heat the blade and draw it out thin along the flat, and hollow it, a groove down the middle, the way you would for a bird's wing. It loses a third of its weight and none of its strength. You rewrap the grip in the blue cloth. You write on the note: It will fit. Grow into it slowly.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("pia4", "Pia's Hands").gap(8)
                .intro(
                        "* Pia has been at the forge for fourteen years. She was a girl at the door in a coat of four coats, who said she had been Number Three at Voss's and that she could draw out a nail from a rod in forty strokes. She is thirty now, tall, broad in the shoulder and dark with the soot of her trade, and she holds a hammer like a woman holding a baby. She has never once asked you for anything.")
                .friends(5, "Gather the forge's people: make five friends.")
                .outro(
                        "* She asks you today. She stands in the door of the shop at noon with her hands behind her back and says that she would like a day. She says she has an idea. She says she would like, if it is not an imposition, to make a thing from the beginning, by herself, with nobody watching, from the iron to the quench.",
                        "@You| What kind of thing?",
                        "@Pia| (flushing under the soot) A blade for nothing. Like Evenfall. Only mine.",
                        "* You give her the forge. You take a stool and go out into the yard with a flask of tea and sit in the sun with your back against the wall. You do not listen; you hear it anyway: the roar of the bellows, the ring of the hammer, a long silence, a roar again. At sunset the sound stops. After a while the door opens and she comes out with her face grey and her eyes enormous and sits down beside you in the dust and says nothing at all.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("coin", "Strange Coin").gap(8)
                .intro(
                        "* Marl keeps the accounts, and Marl has opinions. {marl.He} lays the books on the table at the end of the month with a face like a closed gate. There are four columns, and the fourth is full of entries that {marl.he} cannot make sense of. It is headed PAYMENTS. It lists, among other things, a bird's feather, a button, a smear of red clay in a twist of paper, three coins of a metal that does not exist and a small blue stone.")
                .wait(4)
                .outro(
                        "@Marl| They're paying us. I think. Every parcel that comes back has something with it. I've entered them all as miscellaneous. I'd like to know what to enter them as. The bank is very polite about it, but I could tell the clerk was thinking of calling a doctor.",
                        "* You turn over one of the coins in your fingers. It is heavier than lead and lighter than air. It is warm. It has a stamp on it that you can almost read, a bird, or a hand, or a flame. Pia holds the blue stone to the window and it throws a small clean patch of colour on the whitewash, a colour you have never seen, somewhere between a bruise and a morning.",
                        "@Pia| I don't think it's money. I think it's what they had.",
                        "* You put the coin on the shelf above the anvil with the others. It does not look out of place.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("voss4", "Quartermaster Voss Writes").gap(8)
                .skipIf((c, st) -> { Character v = Chapters.person(st, "voss"); return v != null && v.dead; })
                .intro(
                        "* The letter is from a retired man in a small town on the coast, in a hand like a row of bricks. It says: Voss. Late Quartermaster, Greywater. I am told that blades are coming back through the post. I am told that you are mending them. I should like to say, if I may, that I was wrong about a great many things, and that I should be glad to be of use.")
                .wait(4)
                .outro(
                        "* He comes on the Thursday coach with a bag and a limp. He is old and heavy and grey, with a face like a clenched fist and eyes that have lost the habit of command. He stands in the door of the forge for minutes before he enters. He looks at the benches, the anvil, the tools. He looks at the three names on the sign. He says he did not think it would still be here.",
                        "@Voss| I came to say a thing. I ran a forge for forty years where the strongest were paid and the rest were numbered. I numbered a girl called Pia. Number Three. I should like to tell her that I knew she was better than the first two. I did not say so. I let her go.",
                        "* Pia comes out from behind the bellows with her arms folded and soot on her chin. She looks at him for ages. She says that she knew. She says it was why she left. She says that she has a cot in the loft and that he may sleep in it, and that if he is going to stay he can take a hammer.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("broken", "The Blade That Would Not Mend").gap(8)
                .intro(
                        "* It comes in two pieces. The break is clean across the middle, as if the blade had been snapped over a knee. The note is short and has no signature. It says: I used it to hold a door. The door gave way. I am sorry. It was the only thing I had.")
                .wait(2)
                .choice(new Chapters.Choice("broken", "The blade is in two halves, and each half is a good steel, and a friend of yours made it.",
                        "Weld it, and mend it, and send it back, whole, with a seam.", "Forge it anew into a ploughshare, and send that.", "Send it back broken, with a note, and let the wielder choose.")
                        .after(
                                new String[]{"* You weld it. It takes three days and a pound of borax. The seam is a thin bright line, like a river on a map, and when you quench it, it rings. The note you send with it says: It holds. It won't be the same. It will be better. Whatever holds a door should be. It comes back a month later, empty, with a pressed flower between the pages of a paper. There is no note.", "@Pia| That's the first time someone has said thank you with a flower."},
                                new String[]{"* You turn it into a ploughshare. It is a curious hour's work, to take a fine thing and make it humble. The steel is good, and takes the new shape willingly, and when you quench it, it gives a long gentle sigh. You send it back with a note: Plough a field. Plant barley. That is what a blade is for, when it has done. A reply comes in the spring. It is a handful of grain tied in a cloth.", "@Voss| (gruffly, wiping his hands) I never would have done it. I'd have kept it as a trophy."},
                                new String[]{"* You send both halves back as they came, with a note: I can mend it, or turn it to another use, or leave it as it is. It is yours to choose. It is a long time before the answer. It is a small tin box, which comes on the doorstep on a Sunday. Inside, wrapped in a rag, there are two pieces of steel. They have been polished. They have been given to the fire. They have been fitted together with a hinge. It is a pair of shears.", "@Marl| (laughing, in disbelief, holding them up) They cut."}))
                .outro(
                        "* The shears hang on a hook on the wall of the shop with the other things that have come back. There are a ploughshare, a spoon, a trowel and a small brass bell. Each has a tag on it in your handwriting. They are the strangest collection of tools in the country, and they all, in their way, are swords.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("ketter", "Marshal Ketter's Peace").gap(8)
                .intro(
                        "* Marshal Ketter comes to the forge in the middle of a cold snap on a tall bay horse with no escort. He is a thin hard-faced man of fifty in a leather coat, and he has not been seen in the district since the salt flats. He is polite. He stands in the shop with his hat in his hand and says that he has an order.")
                .wait(2)
                .outro(
                        "?dead.ketter|@Ketter's steward| (a young man, nervous, in the Marshal's place) The Marshal asked me to say it. He said it in the last week. He said he wanted blades for the village at the edge of the flats, to be put on the wall of the church. Not swords. A hundred blades, to be left in a pile, under a roof, with a notice: These Were Made for War. They Are Not Being Used.",
                        "?!dead.ketter|@Ketter| I should like a hundred blades. Not to use. To pile under a roof, in the village at the edge of the flats, with a notice over them: These Were Made for War. They Are Not Being Used. I have been thinking about it for ages. I should like it to be said by someone who made them.",
                        "* You take the order. It is the strangest you have ever had. You make them over the winter, a hundred plain short swords of the cheapest iron, none of them to be sharpened. Pia stamps each with the forge's mark and, on the other side, a small clear hand. They go on a cart in the spring, in straw, with Voss at the reins. The village puts them in the church. The notice is cut in stone.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("tamletter", "Tam's Letter").gap(8)
                .intro(
                        "* Tam writes from the Outside, through the Seam. His letters come with the parcels, tied with the green cord, in a hand that has got no steadier. He has been given a company of forty. He has been given the care of a gate. He writes that the Outside is wider than he thought, and a great deal colder, and that there is a tree in the yard of the barracks where the soldiers hang their helmets at night. He writes that it is a very good tree.")
                .wait(4)
                .outro(
                        "?tamgo=1|@Tam| (in the letter) I am in the Outside. I came back. I find I can't be anywhere else. The blade you made is on my wall. I have not used it since the second week. I keep it for the nights.",
                        "?tamgo=3|@Tam| (in the letter) I am in the Outside again. I went and came back and went. The forge was the same when I left, and I find it is the same in my sleep. I have a gate to keep. I keep it. It is an honest job.",
                        "?!tamgo=1|?!tamgo=3|@Tam| (in the letter) I am well. I am in the Outside. I want you to know that I use the bird you drew on the tang as my signature. The other soldiers think it is a rank.",
                        "* The last paragraph is a different ink, and a smaller hand. It says: I have learned to temper. I have been teaching the line. I tell them what you told me: you don't make a thing for a person you haven't met. They look at me as if I were mad. Then, a week later, they bring me a nail and ask for a blade.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("hammer", "Who Holds the Hammer").gap(8)
                .intro(
                        "* You are tired. You have been tired for a spell. You notice it first in the morning, at the quench, when your wrist does not answer as it used to. You notice it again in the evening when the hammer feels a pound too heavy. Pia sees. She says nothing. She brings a stool and sets it by the anvil and, without comment, stands in your place.")
                .wait(2)
                .choice(new Chapters.Choice("forge", "The forge is yours, and Pia stands at the anvil.",
                        "Give it to her: the sign, the hammer, the name, and keep a stool by the bellows.", "Share it: both names on the sign, and the work divided as it falls.", "Keep it, and let her build her own beside it.")
                        .after(
                                new String[]{"* You hand her the hammer. You do it in the yard at sunset in front of the whole forge, Marl and Voss and the boys and the girl with the freckles. You give her the sign, the keys and the account books. She takes the hammer in both hands and says nothing at all. Then she goes into the shop, and strikes. It is the first stroke of a different smith.", "@Voss| (to himself, wiping his eye) Number Three."},
                                new String[]{"* You share it. The sign is repainted, in black, in a new hand. It says: PIA AND YOU, SMITHS. The work divides itself. She takes the long blades and you take the small. She takes the loud days and you take the quiet. It is the happiest arrangement either of you has ever had, and it lasts for the rest of your working life.", "@Marl| (counting) It adds up. I'm so surprised."},
                                new String[]{"* You keep it, and you help her build her own. It takes a year. She has the old stable, across the yard, with a new chimney and a new bellows and an anvil from the scrapyard. It is called PIA'S. For a season there are two forges in one yard and a good deal of friendly noise. Then the blades start to come in addressed to either, and then to neither, and one winter morning you wake to find the two have a single door.", "@Pia| I did not mean to do that."}))
                .outro(
                        "* The hammer rings in the shop. You listen from the bench by the window with a mug of tea. It is not your stroke. It is a little quicker, a little lighter, a little more sure. It is the sound of the forge getting on with its work. You find you are smiling. You find you have not wished, for a moment, to be at the anvil.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("quiet", "Quiet Fires").gap(8)
                .intro(
                        "* The forge is quiet at night. The fire is banked and the bellows are still, and the air in the shop smells of hot iron and linseed and tea. The mending parcels come twice a week now, and Pia and Voss and Marl and the boys work through them in a long unhurried line. You sit on the stool by the anvil, in the glow, with a blanket round your knees. You have taken to watching.")
                .wait(2)
                .outro(
                        "* Tonight a parcel has come that no one has opened. It is brown, and tied with the green cord, and has your name on it in a strange careful hand. You undo the knot. Inside, wrapped in a soft grey cloth, there is a small iron nail. It is bent, and rusted, and has been through a fire. There is a card with it. It says: Do you remember? You made it for a boy called Tam. He kept it.",
                        "* You turn it in your fingers. It is the first thing you ever forged, at the age of eleven, in your father's shop, with your father's hand on your wrist. It was a nail. It was the worst nail in the world. You gave it away, for luck. You had not thought of it in forty years.",
                        "@Pia| (from the bellows, quietly, after a while) What is it?",
                        "@You| It's a nail.",
                        "@Pia| (coming to look, with a face grown suddenly young) It's a good nail. It's the best one I ever saw.")
                .pay(2000, 3, 0, 4, 2, 2);
    }
}
