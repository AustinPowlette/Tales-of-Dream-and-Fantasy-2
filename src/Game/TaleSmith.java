package Game;

/** The Blacksmith's Child (see "Story Plan.md"). The player's father, Harl, died at his anvil and the player does not know how. His lessons come back as the player works the forge (the
 *  chapters watch the number of items made since the chapter began). A war breaks out between the Northern League and the Marsh Dukes, and both order arms (flag side: 1 the League,
 *  2 the Dukes, 3 both in secret, 4 neither). Behind the old forge wall is Harl's unfinished sword, Evenfall, and a letter: he refused to make 'weeping blades' for Quartermaster Voss,
 *  and Voss's men killed him. Choices: the side, the stand taken after the battle (stand), and what to do with Voss (decided in the fight). */
final class TaleSmith {
    private TaleSmith() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }
    private static boolean made(Character c, Starts.State st, int n) { return c.itemsCrafted - Chapters.flag(st, "craft0") >= n; }

    static void register() {
        Chapters.Def d = Chapters.def("smith");
        d.flags("craft0");
        d.cast("pia", "Pia", "Fenn").she().trait(Character.Traits.Blacksmith).side("Good").lvl(0).age(15).at("here").rel(3);
        d.cast("voss", "Quartermaster", "Voss").he().trait(Character.Traits.Merchant).side("Evil").lvl(7).pot(10).age(49).at("here").rel(-3);

        d.chapter("cold", "A Cold Forge")
                .intro(
                        "* The forge has been cold for eleven days. That is the longest it has been cold since before you were born.",
                        "* Your father's hammer still hangs on its hook by the door. It has a worn place on the handle exactly the shape of his hand. You have not touched it. You have not touched anything.",
                        "* There is a man waiting on the step. He is a farmer with a broken hoe. He has been there since sunrise, and he has not said a word, and he is not leaving.",
                        "@Farmer| Your father would have fixed it in ten minutes.",
                        "* You take the hoe. You light the fire. It takes you an hour. It is not a good repair.",
                        "@Farmer| That'll do. It'll do fine. Come by for your coin when you've got the knack.",
                        "* He walks away whistling. You stand in the warm glow of the forge for the first time in weeks, and your eyes sting from the smoke. It is definitely the smoke.")
                .check((c, st) -> made(c, st, 10), "Light the forge again: make ten items.")
                .outro(
                        "* On the tenth night, you are working a bar of iron, and it goes dull under the hammer. You frown. You hit it again. It rings wrong.",
                        "* You stop. You close your eyes. You remember a small yellow room, and the smell of oil, and a very large hand wrapped around yours.",
                        "@Father| Don't hit the iron. Listen to it. It's already telling you where it wants to go. You only have to be quiet enough to hear.",
                        "* You are nine. Your hands are so small that his thumb covers both of them. The hammer is too heavy for you and he carries the weight.",
                        "* You open your eyes. The iron glows. You listen, and this time you can hear it, a tiny ticking, like a clock in a very small house.",
                        "* You strike where the ticking points. The bar sings.")
                .setup((c, st) -> mark(c, st))
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("pia", "The Girl at the Door").years(1)
                .setup((c, st) -> Chapters.spawn(c, st, "pia"))
                .intro(
                        "* A girl of fifteen knocks at the forge door in the middle of the night. She has a bundle, a split lip, and a fierce look.",
                        "@Pia| My name's Pia. I'm good with a broom, and I can carry anything, and I don't eat much. I heard the smith's child was alone. I'm alone too. I thought we might as well be alone together.",
                        "* You open your mouth to say no. You are twenty. You can barely look after yourself.",
                        "@Pia| I'll sleep in the coal shed. I don't mind the dark.")
                .meet("pia", "Find Pia, who has been sleeping in the coal shed.")
                .outro(
                        "* She is in the coal shed, black to the elbows, sweeping soot into a pile that is bigger than the soot she started with.",
                        "@Pia| I'm doing it wrong, aren't I.",
                        "@You| You are. But you did it on purpose, which is something.",
                        "* She laughs. It is a short, bright, surprised laugh. It is the first laugh the forge has heard in a long time.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("trade", "A Smith's Name")
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* Pia learns fast. She learns faster than you did, and she is not shy about saying so.",
                        "* Together you work through the autumn. Hoes, nails, hinges, knives. A smith's name is made one hinge at a time.")
                .check((c, st) -> made(c, st, 25), "Learn the trade: make twenty-five items.")
                .outro(
                        "* You are about to quench a knife blade when your hand stops. You can hear him again. This time he is tired. This time he is twelve years in the past, and standing at the quench barrel, with his sleeves rolled up.",
                        "@Father| A blade is made twice. Once in the fire, once in the water. If you rush the water, you lose the fire.",
                        "* You were a boy then, or a girl, or a bit of both, in the way children are. You were impatient. You had somewhere to be.",
                        "* There was a knock at the front door. Your father looked up. A man in a grey coat stood in the street, with a clerk behind him, and a cart.",
                        "@Father| No.",
                        "* He said it three times, and the man left each time. The third time, you saw his hands shake.",
                        "@Father| Never forge what you can't carry. Remember that.",
                        "* You didn't remember it. You remembered it now.",
                        "* You lower the blade into the water very slowly.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("orders", "Two Letters").years(1).major(1)
                .intro(
                        "* The war arrives by post. Two letters, on the same morning, from opposite ends of the province.",
                        "* The first is from the Northern League: two hundred spear-heads, a hundred knives, a good price, and a promise of protection. The second is from the Marsh Dukes: the same, in different handwriting.",
                        "* Each says the other side started it. You suspect both are right.",
                        "@Pia| We could do both. Nobody would know. Do you know how much that is?",
                        "@You| Yes.",
                        "@Pia| Or neither. I'd understand that. Or one, the one we like better. You decide. It's your name on the door.")
                .choice(new Chapters.Choice("side", "Two armies want your spears.",
                        "Arm the Northern League.", "Arm the Marsh Dukes.", "Arm both, in secret.", "Arm neither.")
                        .after(
                                new String[]{"* You write back: yes. You feel good for about a week."},
                                new String[]{"* You write back: yes. The Dukes' clerk is courteous. It is a small comfort."},
                                new String[]{"* You write back to both. You mark the spearheads with a tiny number scratched on the socket, so you can tell which are whose. It is a dishonest little system, and you are proud of how well it works."},
                                new String[]{"* You write back no, twice. Pia nods and does not say a word. That night she leaves a bowl of soup on your anvil, which is the nicest thing anyone has done for you all year."}))
                .setup((c, st) -> mark(c, st))
                .check((c, st) -> made(c, st, 50), "Fill the orders: make fifty items.")
                .outro(
                        "?side=4|* There is not much to fill. The orders go to other smiths. You make hinges and horseshoes, the same as always, and watch the carts leave town heavy and come back light.",
                        "?!side=4|* It takes a month. Pia works the bellows. You stay at the anvil until you can no longer feel your hands.")
                .pay(2000, 0, 0, 0, 0, 0);

        d.chapter("wall", "The Wall Behind the Forge").years(1)
                .intro(
                        "* The forge is too small. You have known that for years. In the spring you decide to knock out the back wall and build a second hearth.",
                        "* Pia swings the sledge. The plaster comes down in a cloud. Behind it, in a space between the stones that nobody has opened in years, is a long shape wrapped in oilcloth.",
                        "* You do not say anything. You carry it into the light.")
                .setup((c, st) -> mark(c, st))
                .check((c, st) -> made(c, st, 30), "Study your father's notes: make thirty items to his designs.")
                .outro(
                        "* The oilcloth is dry. Inside is an unfinished sword, the blade rough-ground and unpolished, with no guard and no grip. And a notebook. And a folded letter with your name on the front.",
                        "* You read the letter first. You read it standing up.",
                        "* 'My child. If you are reading this, I am dead, and I am sorry. I could not tell you while I lived. A man named Voss, quartermaster to the army, has been asking me for something called a weeping blade. It is an edge that carries a slow poison. It is made for besieged towns, for wells and for water. I will not make it. He has asked me three times.'",
                        "* 'I have started a sword he cannot use. I call it Evenfall. I never found the last of it. I think the answer is in the quench and the rhythm. If you finish it, it will not cut anyone who has not already raised a hand.'",
                        "* 'Never forge what you can't carry. I could not carry it. I am sorry. Be better than me. I think you can. Father.'",
                        "* A third memory comes, not of a lesson but of a man standing in the doorway with his big hands open, tired, a little embarrassed.",
                        "@Father| A blade remembers who hammered it. So be careful what you hammer into it.",
                        "* You sit on the floor of the half-demolished forge. Pia sits beside you. She does not say anything. She puts her shoulder against yours, and the two of you stay there until the lamps go out.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("greywater", "The Field at Greywater").years(2)
                .intro(
                        "* The war comes to a ford called Greywater, thirty miles from your forge. Both armies meet there on a morning of fog.",
                        "* You do not go. But you can hear it, if the wind is right: a long, low sound, like surf.")
                .task("WAIT", 4, "", "", "")
                .outro(
                        "?side=1|* The League holds the ford with the spears you made. They win. It is called a victory. Three hundred men are dead. A boy of sixteen is carried home with one of your spearheads in his hands. They say he held it all day.",
                        "?side=2|* The Dukes hold the ford with the pikes you made. They win. It is called a victory. Three hundred men are dead. An old man carries home a pike with your mark on the socket, and sits in front of his house with it across his knees.",
                        "?side=3|* In the third hour, a pikeman of the League and a pikeman of the Dukes meet, spear to spear. Both bear your tiny number on the socket. They strike. Both heads snap at the same place, because you made them on the same day from the same ingot. You do not know which of the two fell.",
                        "?side=4|* You armed no one. Others did. Three hundred men are dead. You tell yourself it would have been worse. Pia tells you it would not have been, and she is probably right.",
                        "* The news comes with the first frost. You read the casualty list twice, as if the second reading might change it.",
                        "* Nothing you have ever made has been used to kill. Now you do not know that anymore.")
                .outChoice(new Chapters.Choice("stand", "The war is going to go on, and the orders are going to keep coming.",
                        "Stop making weapons for the war.", "Keep making them, but make them honestly and well.", "Make arms only for the town's own defenders.")
                        .after(
                                new String[]{"@Pia| Are you sure?", "@You| No.", "@Pia| Good. Neither am I. Let's do it."},
                                new String[]{"@Pia| You know that's what every smith says.", "@You| I know. I'll try to deserve it.", "* It is not an answer. It is a promise to keep asking."},
                                new String[]{"* A town needs walls and spears. A town does not need to start a war. You write it on a sign over the door, and the sign is a little crooked, and you leave it that way."}))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("voss", "The Quartermaster").years(2).major(2)
                .setup((c, st) -> Chapters.spawn(c, st, "voss"))
                .intro(
                        "* A man in a grey coat is waiting in your shop when you come back from the market. Pia is behind the counter, white as flour. She tried to throw him out and he did not go.",
                        "@Voss| You must be Harl's child. I thought I recognized the chin. I am Voss, quartermaster to the armies of the League and, when it suits me, of the Dukes.",
                        "@Voss| I was a friend of your father's. We had a disagreement, you may know. A matter of an order. I never held it against him. I came, I think, to settle accounts.",
                        "* He puts a ring on the counter. It is a seal ring, grey, with a small design on it that you know. You have seen it pressed into wax at the bottom of a letter that you have read forty times.",
                        "@Voss| Now. I wonder if the child is as stubborn as the father.",
                        "* He does not draw a weapon. He does not need to. If you want to answer him, type 'attack' while you stand in the same place.")
                .defeat("voss", "Face Quartermaster Voss, and win.")
                .outro(
                        "?dead.voss|* He falls across your counter, and the ring rolls across the floor and stops against Pia's foot. She picks it up. She puts it in your hand.",
                        "?dead.voss|@Pia| That's all of him there is.",
                        "?dead.voss|* There is nothing in the world that feels like winning. There is only the quiet afterwards, and the smell of the forge, and your father's hammer on its hook.",
                        "?!dead.voss|* He is on the floor with his hand over his ribs and your blade at his throat. You do not use it. You take the ring from the counter, and put it in the fire.",
                        "?!dead.voss|@You| You'll leave this province. You'll never order another blade. If I hear of one, I will find you.",
                        "?!dead.voss|* He goes. He does not look back. Pia watches him from the door until he is out of sight, and then she lets out a breath that she has been holding for years.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("evenfall", "Evenfall").years(1)
                .setup((c, st) -> mark(c, st))
                .intro(
                        "* The sword is on your bench. You have looked at it every day for years.",
                        "* You have read his notebook. You have burned yourself thirty different ways, trying to finish the blade. It does not want to be finished by cleverness. It wants to be finished by someone who has done the work.",
                        "* You decide that you will do the work.")
                .check((c, st) -> made(c, st, 100), "Finish your father's sword: make a hundred items to master the work.")
                .outro(
                        "* On the hundredth night you take the sword out of its oilcloth. It is the first time you have really looked at it.",
                        "* The blade is long and plain and lovely. You know now where the quench goes. You know the rhythm: slow, slow, quick, slow. It is the rhythm of a heart.",
                        "* You grind it. You temper it. You fit the guard and the grip. Pia, who is very nearly as good as you now, holds the lantern and says nothing at all.",
                        "* When it is done, you put it in the quench barrel, very gently. The water does not hiss. It simply goes silent.",
                        "* You lift it out. It shines, a little, in a way no blade should shine in lamplight. Not the shine of polish. The shine of a thing that has been waited for.",
                        "@Pia| Try it.",
                        "* You test it on a loaf of bread. It cuts the bread. You hold the point to your own palm, and nothing happens. The edge does not know you. You are not an enemy.",
                        "* You turn it on a practice dummy with a painted face. The edge goes dull as a spoon, as if it will not cut a thing that cannot raise a hand.",
                        "* You put it down on the bench. You cry for some time. Pia puts the kettle on.")
                .pay(10000, 0, 0, 2, 3, 2);

        d.epilogue(
                "* The sign over the forge still says {last} and Child. Your father painted it the week you were born. You have repainted it eleven times and never changed a letter.",
                "?stand=1|* You never made another weapon for the war. The war ended in the end, as wars do, and the ones who fought it said it was a draw. You did not argue.",
                "?stand=2|* You kept making blades, and kept making them well. There are people alive today who would not be if the spears had been worse. There are people dead who might not be. You carry both.",
                "?stand=3|* The town's walls are your best work. The spears stand in a rack by the gate, and nobody has ever needed to use them, and that is the point.",
                "?side=3|* You still keep the little numbered socket you scratched on the spearheads. It hangs on a nail by the door. You look at it every day.",
                "?dead.voss|* The grey ring is in a drawer, under the bellows. You never take it out.",
                "?!dead.voss|* Voss was never seen again. Every year, on the day of the fight, you check the road. You do not think he will come. You check anyway.",
                "* Evenfall hangs on the wall over the forge. Nobody is allowed to touch it. Children are allowed to look. They always ask what it is for.",
                "* You tell them that it is for nothing. That is the point. That is the whole point. It was made by someone who loved you, to be a thing that could not be used to hurt.");
    }
}
