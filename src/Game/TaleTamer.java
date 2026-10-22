package Game;

/** The Beast-Tamer's Daughter (see "Story Plan.md", revamped 2026-10-19 after "Start Stories Seasons Plan.md" section 3.1 and the author's notes: each great beast gets a season of its own, and the fox is the heart).
 *  Season 1, "The Fox": the player's mother, Isha Wildmere, walked into the mountains when the player was seven, with her three great companions: Moss the lake tortoise, Quill the storm crane and Old Ember the cinder boar.
 *  The player is eighteen. A grey fox answers the three-note tune nobody taught it; the player names her (flag fox: 1 Burdock, 2 Hum, 3 Tuppence) and she is a real companion for the whole story (Taming.legend, kept among the story's people
 *  as "fox", so the text can name her and notice if she dies). A stone is left on the path each morning by someone who has been watching; Captain Brodin is a man, not a name; Moss joins at the end of the season.
 *  Season 2 is Quill, season 3 is Ember and the sanctuary. The tune is six notes: Isha always stopped there and said a seventh was somewhere in it. */
final class TaleTamer {
    private TaleTamer() { }

    static final String[] FOX_NAMES = {"Burdock", "Hum", "Tuppence"};

    static void register() {
        Chapters.Def d = Chapters.def("tamer");
        d.season(1, "The Fox");
        d.flags("tune");
        d.place("meadow", "wildnear").place("lake", "wild").place("cliff", "wild").place("wood", "wild").place("town", "near");
        d.cast("fox", "Fox", "Companion"); d.cast("moss", "Moss", "Lake Tortoise"); d.cast("quill", "Quill", "Storm Crane"); d.cast("ember", "Old Ember", "Cinder Boar");
        d.cast("brodin", "Captain", "Brodin").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(4).pot(6).age(44).at("place:town").rel(-1);

        d.chapter("voice", "The Tune in the Throat")
                .intro(
                        "* You were seven when your mother walked into the mountains. Your father told you she had gone to look for something, and he said it the way you tell a child the sun will come up, as though it were a matter that did not need his help.",
                        "* He raised you on a farm at the edge of the wild: forty goats, a pond, a kitchen that smelled permanently of onions. The animals were calm around you. The geese did not hiss. Once a wolf stood at the treeline for an hour watching you hang the washing, and then left without doing anything about it, which the neighbours, when you told them, found more upsetting than if he had.",
                        "* Your father gave you nothing of your mother's except a tune. He hummed it when he cooked: three slow notes, and then the same three, higher. You asked him once where it came from, and he said he couldn't remember. He was lying. You could tell, because he stopped humming, and a man who has forgotten a tune does not stop in the middle of it.",
                        "* You are eighteen now. The farm runs itself, more or less, the goats having formed views. You have decided to find out what the wild wants with you. You will begin with the meadow past the north fence, where the grass is tall enough to hide in, and where for a week something has been watching the house.")
                .at("meadow", "Go to the meadow at {@meadow} and wait for whatever has been watching the house.")
                .outro(
                        "* It is a young thing, grey, fox-shaped, with ears too big for its head, as if somebody had cut them for a larger animal and used the offcuts. It stands in front of you in the long grass with its weight on its back legs and one paw lifted, ready to bolt.",
                        "* You don't move. You do the only thing you can think of, which is to hum. Three slow notes. The same three, higher. It feels absurd, like singing to a locked door.",
                        "* The fox sits down. She tilts her head one way and then the other, with the expression of someone who has had a word on the tip of her tongue for years. Then, from deep in her throat, scratchy and a little flat, she answers: three slow notes, and the same three, higher.",
                        "* Nobody has taught her the tune. As far as you know, nobody has taught anyone the tune but you.",
                        "* She comes forward through the grass in a series of small hops and pushes her forehead into your palm, hard, the way a cat does. You find you are crying, that she does not mind, and that she is also, with great seriousness, trying to eat your sleeve.")
                .outChoice(new Chapters.Choice("fox", "She will need a name.",
                        "Burdock, because she sticks to you.", "Hum.", "Tuppence, which is what she is worth and what you would pay to keep her.")
                        .after(
                                new String[]{"* Burdock. She looks at you with the polite incomprehension of someone being called after a weed, and decides to allow it."},
                                new String[]{"* Hum. She makes the sound at once, and with obvious approval, and then does it again, louder, in case you missed it."},
                                new String[]{"* Tuppence. She sneezes, which you take as a yes."}))
                .done((c, st) -> Chapters.companion(c, st, "fox", FOX_NAMES[Math.max(0, Chapters.flag(st, "fox") - 1)], "Grey Fox", Math.max(6, c.lvl), true))
                .pay(0, 0, 1, 0, 0, 0);

        d.chapter("crimes", "What the Fox Ate").gap(8)
                .intro(
                        "* In three weeks, {fox.first} eats a boot, most of a cheese, the church's harvest offering of turnips, one of your father's reading glasses, and a bar of soap, which she regrets at once and for some hours. She does not eat the goats. She appears to regard them as a committee.",
                        "* She also follows you. Not in the way a dog follows, which is a form of worship, but in the way a very small, very vain landlord follows a tenant: always a little behind, keeping an eye on the premises. When you stop, she sits. When you laugh, she checks the faces of everyone near you to see if the joke was on her.",
                        "@Father| She's the colour of the one your mother had. Not the same one. Her mother's mother's, maybe. They keep to the same hill.",
                        "* You turn round. He is standing in the kitchen door with a tea towel, looking at the fox in a way that you have never seen him look at an animal. She looks back at him, and does not eat anything.")
                .wait(3)
                .outro(
                        "* That night he cooks. You are sitting on the step, with {fox.first} asleep across your boots, when the humming starts. It is the tune. He does not stop in the middle of it this time. He hums the six notes, and hums them again, and when he gets to the end he stops, and holds the wooden spoon in the air as if it might add something.",
                        "@Father| She always stopped there. Your mother. Six notes, and then she'd look at the ceiling and say there was another one, a seventh, somewhere in the tune, and she couldn't find it. It drove her half mad. She said all the great beasts had a piece of it, if she could only get them to sing at the same time.",
                        "@You| You told me you couldn't remember it.",
                        "@Father| I remembered it perfectly. That was the trouble.",
                        "* {fox.first} wakes at the spoon, looks at the two of you, and begins very softly to snore the tune.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("goats", "Goats in Committee").gap(8)
                .intro(
                        "* The goats have a government. It took you eighteen years to see it, and it took {fox.first} nine days. There is a nanny called Dowager who sits at the top of the hill and decides, by means of a long yellow look, where everyone will graze. There is a billy who is thought to be in charge and is not. There are three kids who go everywhere together and are, as far as anyone can tell, the opposition.",
                        "* {fox.first} has not been accepted. She has made this her project. For a week she has crept up the hill every morning to sit as near the Dowager as she dares, ears flat, her whole body saying that she is only here to look. Every morning the Dowager looks at her for a moment and then deliberately turns her back.",
                        "@Father| She'll be there till Midsummer. Dowager held out against your mother for a year. Isha went up that hill with a bucket of oats and a lot of nerve, and she came down with a goat who wouldn't go anywhere else. I never saw a woman lose an argument so politely.")
                .wait(2)
                .outro(
                        "* It is a fox that settles it, though not the one you expected. A real one, a stranger, a rangy red dog fox, comes down off the moor one night to look at the kids. The Dowager is on her feet before he has cleared the wall. She puts down her head and goes at him like a falling cart. {fox.first}, who has been asleep on the roof of the shed, drops on his back from above with a noise like a kettle boiling over.",
                        "* It is over in about ten seconds. The red fox goes back over the wall and does not stop until the moor. {fox.first} stands in the yard, quite out of breath, with a mouthful of somebody's tail and a faint look of surprise.",
                        "* The Dowager comes and stands in front of her. Nobody breathes. She puts out her long yellow face and examines {fox.first} from nose to tail, and then, with great deliberation, as if conferring a title, turns round and lets her pass.",
                        "@Father| Well. That's you in, then.",
                        "* From the next morning {fox.first} grazes on the hill, which is to say that she lies on it. Nobody has ever seen a fox look so pleased with herself.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("quarrel", "The First Quarrel").gap(6)
                .intro(
                        "* It is the first time you have shouted at her. She has been into the dresser. She has found, behind the cups, in a biscuit tin, a bundle of old letters in a blue ribbon. She has eaten the ribbon and a corner of three of the letters. Now she has settled on the hearth rug with the remains of a fourth and an expression of deep, bright, sticky satisfaction.",
                        "* They are not yours. They are your father's. He has kept them in a tin, in a dresser, for twenty years, and has never once taken them out. You see the writing and you stop. It is a woman's hand, quick and slanted, with the tails of the letters going off the line like birds. It is a hand you have seen on the back of a flour sack, once, under a drawing.")
                .wait(2)
                .outro(
                        "* You say a number of things. They are loud and not fair. {fox.first} listens to all of them with her ears flat and her whole body gone small. When you stop to draw air she gets up and goes away, very slowly, out of the door and into the yard, and does not come back that night.",
                        "* You find her in the morning in the loft over the barn, in a nest of hay, with the scrap of letter between her paws. It has a word on it. She has been guarding it. It says: ...and tell the child that the wild is not a thing you go into. It is a thing you let come to you.",
                        "* You sit down in the hay beside her. You say no sorry, because she would not know the word. You put out your hand, flat, on the hay, and after a long while she puts her chin on it. She is trembling a little. You say: 'You can have the ribbon.' She sneezes. It is, you gather, forgiveness.",
                        "@Father| (from the foot of the ladder) I heard. I'll tell you something. I read those letters every night for the first year. Then I put them in the tin. I couldn't bear to look at them, and I couldn't bear to burn them. I think she did you a favour.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("legacy", "Isha Wildmere").gap(26).major(1)
                .intro(
                        "* The letter comes by a boy on a mule, and it is in your father's careful hand, because he is not a man who writes letters, and so he writes them as if each one were the only one.",
                        "* 'My child. I have been a coward, and I would like to stop. Your mother was Isha Wildmere. In her day they called her the Voice of the Wild. She spoke to the great beasts and they listened. She had three: Moss, who sleeps in a lake in the north; Quill, who nests where the storms break; and Old Ember, who was her oldest friend and the most dangerous.'",
                        "* 'When you were seven she went into the mountains to find Ember, who had gone missing. She did not come back. I did not follow her. I had you. I made that choice and I do not regret it, but I regret everything round it. Enclosed is her bone whistle. She told me to give it to you when you were old enough to be afraid of nothing. You were never afraid of anything. I should have sent it years ago.'",
                        "* {fox.first} has found the folded paper before you have finished it and is trying to take it away to somewhere private. You let her.")
                .realm(1, "Reach Major Lvl 1, and learn what your mother was.")
                .outro(
                        "* The whistle is yellow with age, no longer than your finger, with a tiny hole at each end. You put it to your lips and stop short of blowing. You are not ready, and you suspect it knows.",
                        "* You say the three names aloud, to see what it is like. Moss. Quill. Ember. {fox.first} stops what she is doing, and turns, and looks at the north.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("trunk", "What the Father Kept").gap(6)
                .intro(
                        "* You are packing for the road north when your father comes into the room with a trunk. It is a small one, black, with brass corners and a broken lock. He sets it on the bed and stands looking at it the way he looks at a pot he is not sure is done.",
                        "@Father| I have been keeping this for eleven years. It is not a secret. I did not know how to hand it to you. You have to hand a thing like this over at the right time, or it turns into a speech.")
                .wait(1)
                .outro(
                        "* There is not much in it. A hairbrush, with a long grey feather caught in the bristles. A bone comb. A pair of men's gloves, much too small for a man. A page of music, ruled by hand, with a tune on it that you know, six notes in two lines, and below the sixth, in a big impatient scrawl, a seventh bar, empty, with a question mark.",
                        "* And under the music, folded small, a child's drawing. It is done in charcoal on the back of a flour bag. It shows three animals in a field. One is big and round, with a head like a stone. One is long and white, with her neck up. The third is black and enormous, with tusks, drawn so large that he goes off the edge of the paper. Under them, in the unsteady capitals of a five-year-old, is written: MOSS. QUILL. EMBER.",
                        "@You| I drew this.",
                        "@Father| You did. The summer you were five. They came to the farm.",
                        "* He sits on the edge of the bed. His hands, on his knees, are not steady. He tells it without looking at you.",
                        "@Father| She brought them, all three, for three nights. In the north field. She said you ought to know them while you were small enough not to be frightened. The tortoise ate the cabbages. The crane perched on the barn and told the weathercock what it was doing wrong. And the big one, the black one, wouldn't come in the gate. He lay down outside it with his chin on the road. You went out in your nightgown and sat on his head.",
                        "@Father| You don't remember. I know you don't. I think she made you forget, a little, for your own sake. She said you'd remember in your own time. I think this is your own time.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("tavern", "The Cracked Egg").gap(12)
                .setup((c, st) -> Chapters.spawn(c, st, "brodin"))
                .intro(
                        "* The inn at {@town} is called the Cracked Egg, for reasons the landlord will tell you at length. {fox.first} has been refused entry three times and has succeeded on the fourth by way of the kitchen. You find her under a table in the back room, in the lap of a stranger, who is looking down at her with the tense, shifting expression of a man holding a bomb.",
                        "* He is forty-odd, big in the shoulder, with a scar through one eyebrow, a clean shirt, and a table of other men who have the fixed, cheerful look of people who are being bought drinks. He is talking, and you catch only the tail of it: twenty spears, and a price, and the Cinder Boar.",
                        "@Brodin| Forgive me. I seem to have been adopted. Is she yours? She's very forward. I don't mind. I'd just like to know if I'm about to be bitten.")
                .meet("brodin", "Speak to the man who is holding your fox.")
                .outro(
                        "@You| You're the one who's going after Ember.",
                        "@Brodin| Captain Brodin. And yes. You've the look of someone who has opinions. Say them. I'd rather have them over a table than hear them later from behind a tree.",
                        "@You| He's one of three. He belonged to someone. You can't collect a price on a creature that belonged to somebody.",
                        "@Brodin| He killed eleven people. I counted them. I went and counted. The last of them was nineteen years old and he was told to stay at the edge of the wood with the water. He didn't. They never do. I've his belt buckle here. I'm not saying that to be unpleasant. I'm saying it because you asked what the price was for.",
                        "* He puts it on the table between you. It is a plain iron buckle, worn bright on one side. {fox.first} puts her nose to it, and then, in the manner of a creature who has understood something she was not meant to, leans against the man's arm and leaves it there.",
                        "@Brodin| I'll be in the Burned Wood by autumn. I would ask you not to come. I would be surprised if you listened.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("stones", "A Stone on the Path").gap(10)
                .intro(
                        "* The first stone is on the doorstep in the morning, and you think nothing of it. It is round and smooth and the green-black of deep water, and it is wet. It has not rained. {fox.first} sniffs it for most of a minute, sneezes, and goes to sit as far from it as the step will allow.",
                        "* The second is on the path to the well. The third is in your boot. Each one is a different shape, and each is wet, and each, if you put your ear to it, has the faintest sound in it, very low and very slow, like a heartbeat heard through a wall. By the fourth, you understand that someone is being polite.",
                        "* On the fifth morning there are tracks. They are in the dust of the lane, wide and shallow and slightly wet at the edges, and each one is the size of a table. They come from the north. They stop at your gate, and they turn round.")
                .wait(6)
                .outro(
                        "* The last stone is the biggest, and it is warm. You hold it in both hands. It hums: one note, enormously low, so far down that you feel it first in your knees. It is the tune. It is not the tune. It is six notes sung by a creature that has all the time there is, and it ends where your mother always stopped.",
                        "* {fox.first} has got up. She stands in the lane with her ears forward, looking north, with her whole small body pointed like a compass. She looks back at you once, to check you are coming. Then she sets off, and you pick up your pack.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("night", "The Night the Fox Went Out").gap(2)
                .intro(
                        "* You wake in the small hours with the sense of a draught, and find the door ajar and the place by the hearth empty. {fox.first} has gone out. She has gone out before, for a mouse, or a moon, but never with the door left open behind her. That is a message. You pull on your boots and go after her, across the yard and the fence and over the pasture, in the dark.",
                        "* You find her at the edge of the north field, sitting very upright in the frost, with her tail round her feet, looking at the wood. She does not turn her head. In front of her, a few yards off, sits the great grey wolf with the torn ear.")
                .wait(1)
                .outro(
                        "* They are not fighting. They are not doing anything. They are sitting on opposite sides of a patch of silver grass, a fox and a wolf, in the dead of a winter night, and between them is a feather. It is long and grey, with a white tip. It is not a goose's. It is not a hawk's. It is lying on the frost where one of them put it down.",
                        "* You do not move. After a time the wolf stands, slowly, and turns, and goes away along the edge of the wood, without a sound. {fox.first} gets up. She walks to the feather, and picks it up in her teeth, very carefully, by the quill. She carries it back across the field to you, and puts it down on your boot, and sits.",
                        "* It is the feather from your mother's hairbrush. You are sure of it, though you have no reason. It is the same grey, the same white tip. Either someone brought it here, or it has been brought, over a great many years, by a series of creatures who each took it a little further and put it down for the next.",
                        "* {fox.first} licks your hand once, gravely, as one signs a receipt. Then she walks back to the house in front of you, with her head up, and does not look round, and when you come in she is asleep before you have the bolt across.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("north", "The Road North").gap(3)
                .intro(
                        "* You walk, because that is how it is done. You have never been farther than the market town. The road goes north past a quarry, a chapel, and a farm with a loud dog. By the second day you have learned a number of things about travelling with a fox, the chief of which is that everyone is her friend and she is nobody's.",
                        "* She knows the inns by their smell. She knows which kitchens have a door that sticks. She spends the evenings in the hearth of whatever place has one, chin on the warm stone and one eye on the room. She spends the mornings in the rafters, where she has collected a number of items she cannot account for: a candle, a glove, the landlord's spectacles.")
                .wait(2)
                .outro(
                        "* On the fifth morning you are crossing a long open moor when {fox.first} stops. Her ears go up. She stands in the heather with one forepaw lifted, looking at the edge of a wood a quarter of a mile away, where something grey is standing at the treeline.",
                        "* It is a wolf. It is large, old, and rangy, with a torn ear and a stripe of white across the muzzle, and it is watching you. You know it. You have known it for years. It stood at the edge of the north pasture for an hour once, watching you hang washing, and went away without doing anything about it, and the neighbours were never the same.",
                        "* It does not come closer. It turns, slowly, and starts to trot ahead of you, along the edge of the trees, northward, at exactly the pace you walk. When you stop, it stops. When you sit to eat, it lies down in the heather a long stone's throw off and licks a paw. {fox.first} regards it with an expression of the most complete disapproval, and at the second day's end, gives it half her sausage.",
                        "* It sees you to the foot of the hills. At the last ridge it sits, and watches, and lifts its muzzle to the sky, and howls: one long, low, steady note, held a very long time. You have heard it before. It is the first tone of the tune.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("moss", "Nobody's Lake").gap(4)
                .intro(
                        "* The north is cold and full of water. The lake is called Still Water on the maps and Nobody's Lake by the people who live beside it. A hunter once tried to wade across and came back an hour later with a mild, confused expression and no memory of the middle.",
                        "* People say the lake is asleep. People say that if you listen carefully you can hear it breathe. {fox.first} has stolen an apple from a market stall at the last village, and carries it all the way in her mouth, which has not made her any more popular.")
                .at("lake", "Find the sleeping lake at {@lake}.")
                .outro(
                        "* The lake is flat and wide and green as bottle glass. There is a small island in the middle, and it is only when you are standing on the shore that you notice the island has a head. You sit on a flat stone. You wait all day. The light moves. A heron lands, thinks better of it, and goes. At dusk a great slow voice speaks in your mind, in no language you know, and you understand every word.",
                        "@Moss| You smell of her. You have her way of sitting. She came here when she was younger than you, and said she would be back, and I have been patient. I am good at it. You waited a whole day without being asked. That is the answer. She never hurried either.",
                        "* The island rises. Water pours off a shell the size of a house, and a vast, soft, ancient face turns towards you and blinks once. {fox.first} walks to the edge of the water with the apple, and puts it down at his chin, and sits back with the air of someone who has delivered a package.",
                        "@Moss| What is that?",
                        "@You| It's an apple.",
                        "@Moss| I have heard of them. I have never tasted one. It is very small.",
                        "* He eats it. It takes a long time, and nobody speaks. When it is over he says it was sweet, and sour. It was, he says, the first new thing to happen to him in two hundred years, and he would like to come with you, if it is no trouble. He says he will be slow.",
                        "@Moss| Sing it for me. The one she sang. I want to hear if you stop in the same place.",
                        "* You hum the six notes, and he answers them, low as the floor of the lake. You stop. He waits for you to go on, and you cannot.",
                        "@Moss| Yes. That is where she stopped, too. There is one more, you know. I have been waiting a hundred years to hear what it is.")
                .done((c, st) -> Chapters.companion(c, st, "moss", "Moss", "Lake Tortoise", Math.max(10, c.lvl + 3), false))
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("slow", "Slow, and Then Slower").gap(2)
                .intro(
                        "* There turns out to be a great deal to learn about travelling with a tortoise the size of a barn. He cannot go by the road, which would damage it. He cannot go by the river, which would damage him. He goes, therefore, through the fields, at about the speed that a man might walk if he were thinking about something else.",
                        "* {fox.first} rides on his shell with her chin on her paws, and regards the world from there with the cool, proprietary look of a ship's captain. The children of the first village follow for a mile. The old men of the second take off their hats. In the third, a farmer comes out and says, with enormous dignity, that someone ought to have asked before putting a lake in his turnips.")
                .wait(4)
                .outro(
                        "* On the last evening of the trip he falls asleep in the middle of the road, between one step and the next, with one foot raised, and does not wake up until morning. A cart is stopped behind him. The carter climbs down, looks, climbs back up, and unharnesses his horse, and the two of them sit by the side of the road with a flask until daylight.",
                        "@Moss| I'm so sorry. I do that.",
                        "@Carter| Don't mention it. My wife says I do the same in the pew.",
                        "* You make your camp beside a tortoise, in the dust, at the top of a hill, with a fox asleep in a hole in his shell. Above you, in the north-west, there is weather coming, a long bruised line of it, with lightning walking inside like someone going from room to room.",
                        "@Moss| That would be Quill. She has a nest where the storms break. She is fond of drama.")
                .pay(300, 0, 0, 0, 0, 0);

        d.interlude(1, "Three Slow Notes",
                "* You have a fox, who sleeps across your boots, and a tortoise, who sleeps across the road. You have a whistle that has never been blown and a tune that will not finish. You have, at last, a mother, in the form of other people's accounts of her: she hummed, she stopped, she looked at the ceiling.",
                "?dead.fox|* There is a small hole in the grass beside the farm, under the apple tree, and you go there on the days when the weather turns.",
                "?!dead.fox|* {fox.first} has stopped, by general agreement, being a stranger. She has opinions on the goats, the church, the cheese. She sings in her sleep. It is a whole six notes, and when she stops she looks at the ceiling.",
                "?!dead.brodin|* Captain Brodin sits in an inn somewhere with a belt buckle on the table, counting.",
                "* In the north-west, the weather is getting closer.");
        TaleTamerSeasons.add(d);
    }
}
