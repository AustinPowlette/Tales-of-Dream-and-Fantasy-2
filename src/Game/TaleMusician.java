package Game;

/** The Zither That Would Not Be Tuned (2026-10-20; new start, three seasons). The human inherits a thirteen-string zither from a grandmother who never explained it. Twelve strings tune; the thirteenth hums at night to someone far away.
 *  Season 1, "The Road Song" (a lyrical road tale, cards C and E): a hat on the ground, the drummer Pocket, a wedding with no band, the Academy of Sweet Sounds and Master Lamberd, a tune for the dead, the Listener of the Convocation,
 *  Ilse Ferrant (the other gender than the human's). Choices: wedding (what is played), duel (how the first duel ends), love. Flags: wedding, duel, love. Seasons 2 and 3 are in TaleMusicianSeasons. */
final class TaleMusician {
    private TaleMusician() { }

    static void register() {
        Chapters.Def d = Chapters.def("musician");
        d.season(1, "The Road Song");
        d.flags("wedding", "duel", "love");
        d.place("inn", "wildnear").place("village", "wild").place("academy", "far");
        d.cast("pocket", "Pocket", "Dunne").he().keep().trait(Character.Traits.Friendly).side("Good").lvl(2).age(20).at("here").rel(3);
        d.cast("ilse", "Ilse", "Ferrant").opposite("Ilias").keep().trait(Character.Traits.Musician).side("Neutral").lvl(6).age(27).at("far").rel(0);
        d.cast("lamberd", "Master", "Lamberd").he().keep().trait(Character.Traits.Leader).side("Neutral").lvl(10).pot(6).age(58).at("place:academy").rel(-1);

        d.chapter("strings", "Thirteen Strings")
                .intro(
                        "* The zither came in a flat box, by the carrier, with a note in shaky capitals: FOR WHEN YOU ARE READY. It is a long narrow instrument of dark wood, as old as a grandmother and as scarred as a doorstep. It has thirteen strings. Twelve are steel. The thirteenth is something else. It is pale, and thin, and when you touch it, it does not sound. It listens.",
                        "* Your grandmother played it at weddings and wakes, in the days when you were small enough to sit under it. She never once let you touch it. She never said why. She died in the winter with the lid shut and her hands folded on it, and the doctor said her last words had been about a tune.")
                .at("inn", "Take the zither to the Fiddler's Rest, the inn at {@inn}.")
                .outro(
                        "* The Fiddler's Rest is a long low inn at a crossroads, with a roof like a hat and a sign of a fiddle with a wooden leg. The landlord is a heavy grey man called Brewster, who looks at the box, and the road, and your boots, and asks if you can play.",
                        "@You| A little.",
                        "@Brewster| A little is plenty. I've had a great deal of nothing. Sit by the fire. If it's good, I'll feed you. If it isn't, I'll feed you anyway, but I'll look sad.",
                        "* You tune it in the firelight with the wind in the chimney. The twelve steel strings come up one by one to a clear bright pitch. The thirteenth does not move. You leave it alone. You begin to play, something simple, a song your grandmother hummed over the washing-up, and the room goes quiet, and halfway through the second verse, very faintly, the pale string begins to hum.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("hat", "A Hat on the Ground").gap(8)
                .intro(
                        "* You have eleven coins and a zither. The road runs three ways. You choose the one with a village at the end, because villages have markets, and markets have crowds, and crowds, in your experience, will give a person a coin to stop. You play at the cross in the square on a Saturday morning with your hat on the ground in front of you and your heart in your mouth.")
                .friends(2, "Play for your supper: make two friends.")
                .outro(
                        "* The first hour is nothing. The second brings a dog. It sits in front of you with its head on one side and its tongue out, and a small boy comes to fetch it, and stays. By noon there is a ring of faces. A woman with a basket of eggs. A man with a ladder. Two girls who have been told to go home and are pretending not to hear.",
                        "* You play every song you know. You play them badly and then less badly. At the end of the afternoon the hat is half full of coins and one boiled egg. The woman with the basket has said that you have a nice touch, and that it is a pity about the thirteenth. You ask what she means. She says she could hear it, all afternoon, under the rest, like somebody humming in another room.",
                        "@Woman| I thought it was the wind. But the wind doesn't know the words.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("pocket", "Pocket").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "pocket"))
                .intro(
                        "* The drummer finds you. He is twenty, thin as a rail, in a coat too big for him, with a pair of sticks in his belt and a snare drum slung on his back with a bootlace. He has been following you for three streets. He says his name is Pocket. He says it is because he keeps everything in them, including, at present, a pigeon.")
                .meet("pocket", "Hear what Pocket has to say.")
                .outro(
                        "@Pocket| I heard you in the square. I couldn't help it. You've got something under the tune. Something low. I tried to hit it on the drum and I couldn't find it. I'd like to come with you. I don't need paying. I eat very little. The pigeon eats more.",
                        "@You| I don't need a drummer.",
                        "@Pocket| Nobody needs a drummer. That's what makes it fun.",
                        "* He taps a rhythm on his knee. It is a plain beat, two slow and one quick, the sort you would hear in a market. The thirteenth string, from the box at your feet, hums to it. It is the first time it has answered anyone but you. Pocket stops. He goes white. He says, 'It's got a pulse,' and sits down on the kerb.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("wedding", "A Wedding Without a Band").gap(16)
                .intro(
                        "* The village of Haldane has a wedding on the first Saturday in May, and the band has got drunk. The fiddler is in the ditch. The piper is in the pond. The bride's father, a vast red man, comes out to the road in his best waistcoat and stops you with a hand like a shovel. He says that his only daughter is being married in an hour. He says that he has been told you can play.")
                .wait(4)
                .choice(new Chapters.Choice("wedding", "A whole village is waiting in a barn with garlands and no music.",
                        "Play the old dances: the ones everyone knows, just as they know them.", "Play something new, written on the spot, for the bride and groom.", "Play the song your grandmother hummed, the one with the thirteenth string.")
                        .after(
                                new String[]{"* You play the old dances. The reels, the jigs, the long slow waltz that goes round the barn like water round a stone. Pocket keeps time on the snare. The whole village is on its feet by the second tune, grandmothers and children and the bride's father, who dances like a bear, and cries, and tells everybody it is the smoke. It is a wedding like every other, and for that reason perfect.", "@Father| That's the way it ought to be. That's how it was in my day."},
                                new String[]{"* You write it as you go. A tune for two people: one slow, one quick, running together, each finishing the other's phrase. You make it up in the corner of the barn with your eyes shut and the string humming under your hand. When you finish there is a silence. Then the groom, a shy young man in his father's coat, walks across the floor and shakes your hand without being able to speak.", "@Bride| Nobody's ever written me anything before."},
                                new String[]{"* You play it. It is the song your grandmother hummed over the washing-up, with nothing added. The thirteenth string joins in on the third line. The sound is not loud. It goes through the barn like a draught through a keyhole, and at its touch every person in the room stops, and looks up, and for a moment sees someone they love who is not there. A man weeps. A child laughs. The bride, who has not wept all day, takes her husband's hand.", "@Pocket| (low, afterwards, behind the drum) What was that?"}))
                .outro(
                        "* They pay you in cheese and cider and a ham. The bride's mother wraps the cheese in a cloth. The father gives you the whole of the wedding cake, in a box, and tells you that if you are ever in Haldane you will never pay for a bed. When you leave, in the grey of the morning, a child runs after you, down the lane, with a bunch of cowslips, and says shyly that he heard the string. He says it sounded like his dog. He says his dog died in the winter.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("academy", "The Academy of Sweet Sounds").gap(24)
                .setup((c, st) -> Chapters.spawn(c, st, "lamberd"))
                .intro(
                        "* The Academy of Sweet Sounds stands in the town of Saltwick, a tall white building with a glass dome and a clock with a different tune for each hour. It teaches the Proper Way to play. It has done so for two hundred years. It has a faculty of thirty masters, a hall of a thousand seats, and a rule on the door: No instrument may be played in this establishment that is not in tune.")
                .meet("lamberd", "Answer Master Lamberd's invitation at the Academy of Sweet Sounds.")
                .outro(
                        "* Master Lamberd is a tall precise man in a black coat with a white stock and a face like a metronome. He receives you in a long panelled room with a grand piano and a cabinet of glass jars, each with a label, each holding a little faint pearly light. He circles the zither without touching it. He says that it is a very old instrument. He says it has one string that is out of tune.",
                        "@Lamberd| I should be glad to retune it for you. It would be a favour to the instrument. It's a matter of principle. A string that cannot be tuned is a string that does not wish to be part of the music.",
                        "@You| It's not for tuning.",
                        "@Lamberd| Everything is for tuning. That is the nature of a string.",
                        "* He smiles. It is a thin, courteous, hungry smile. As you leave, you pass the cabinet of jars again. One of the labels reads: The Lullaby of Fennel Cross. Another: A Marching Song, Anonymous. Another, in a smaller hand: Nan Hollin's Third Verse. You stop. The little light in the last jar flickers, and for a moment hums a note you know.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("funeral", "A Tune for the Dead").gap(16)
                .intro(
                        "* The old miller dies at Fennel Cross in the second week of June, and the village sends for the zither player. There is nobody else. The funeral is held at dusk in the churchyard, under a yew. The miller's widow stands at the graveside in black, a small stout woman with a face like a closed door, and asks you to play the only tune her husband ever whistled. She cannot remember it. She says it will come if you play.")
                .wait(4)
                .outro(
                        "* You play every tune you know. None of them is right. The widow shakes her head. The mourners shuffle. The light is going. You are about to give up when, under your hand, the thirteenth string sounds, quite clear, one note. It is a note you have never played. Then another, and another. It is the tune. It is a plain, slow, cheerful whistle, ten notes long, with a skip in the middle, and a stop at the end where you can hear a man putting on his cap.",
                        "* The widow lifts her head. She sings it. She sings all ten notes, quite steady, in a cracked high voice, with her eyes shut. When she gets to the stop at the end she says, 'There you are, then,' to the open grave, as one speaks to a husband who has come in late. The mourners sing it after her. The yew trembles. The thirteenth string goes quiet, as a person does who has finished what they came to say.",
                        "@Pocket| (very softly) It knew it. How did it know?",
                        "@You| I don't know. I don't think it's mine.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("listener", "The Listener").gap(24)
                .setup((c, st) -> Chapters.spawn(c, st, "ilse"))
                .intro(
                        "* There is a person in the third row at the Fiddler's Rest on a wet Tuesday who is not listening to the music. They are sitting very straight in a grey coat with a notebook on their knee, their head slightly tilted, and their eyes closed. They are not writing. They are doing something else, something a good deal harder, with the whole of their face. When you stop, they are the last to open their eyes.")
                .meet("ilse", "Speak to the stranger in the third row.")
                .outro(
                        "@Ilse| I am sorry. It is bad manners to listen with my eyes shut. I am Ilse Ferrant. I am a Listener of the Convocation Choir. I hear the Convocation. That is my entire function. I have heard every instrument in the Palace of the Oldest Order and I have never heard that.",
                        "* {ilse.He} is about twenty-seven, long and thin, with a high forehead and a quick uncertain smile, like a person who has just remembered someone is watching. {ilse.He} says {ilse.he} has perfect pitch, and no ear whatever for people. {ilse.He} says it has been explained to {ilse.him} that this is a flaw, and that {ilse.he} has never been able to see why.",
                        "@Ilse| That thirteenth string. It is not in any scale. It is a quarter-tone below the lowest note the Palace's organ can reach. And it is in tune. I have never heard anything so exactly in tune with nothing at all.",
                        "* {ilse.He} puts a hand out to the zither, and stops, and draws it back, and says, 'May I?' You nod. {ilse.He} touches the pale string. It hums, once, a long way down. {ilse.He} stands there with {ilse.his} hand on the wood and tears in {ilse.his} eyes, and does not seem to notice.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("songs", "Three Songs for a Hat").gap(16)
                .intro(
                        "* The fair at Reedmarch has a contest, a thing called the Hat, where three players are given three songs, and whoever fills the most hats wins a silver fiddle. It is held in the big marquee on the green, with fifty benches and a crowd of five hundred and a stout man in a striped waistcoat who calls the turns. There are eleven entries. You are the tenth.")
                .wait(8)
                .outro(
                        "* You play a hornpipe, a lament and a ballad. Pocket accompanies on the drum, and on the third song, without warning, on the pigeon, which has learned to flap in time. The hat goes round. It comes back heavy. You do not win; you come second to a girl of twelve with a penny whistle, who plays with her eyes shut and a look of perfect indifference, and wins by a margin of eleven pennies.",
                        "@Pocket| She's very good.",
                        "@You| She is.",
                        "@Pocket| Do you mind?",
                        "@You| Not in the least. I think it's the first time I've lost and enjoyed it.",
                        "* The girl comes over afterwards, to shake your hand, with the silver fiddle in the crook of her arm. She says she liked the thirteenth string. She says her mother plays one. She says her mother is a long way off, and that she can hear it humming when she has her eyes shut, on certain nights. She says, 'Is it the same one?' You say you do not know. She nods, gravely, and goes away to her supper.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("duel", "The Duel of Strings").gap(24)
                .intro(
                        "* The challenge comes on the Academy's good grey paper, in the Master's own upright hand. It proposes a duel. It will be held in the Academy's great hall on the evening of the autumn equinox, before an audience of a thousand, with a champion of the Academy against a player of your choosing. The loser's instrument is to be retuned by the winner. It adds, in a postscript, that the Academy's champion is its best student, a young woman named Clarice Quill. It adds that she is nineteen.")
                .realm(1, "Be ready for the duel: reach Major Lvl 1.")
                .outro(
                        "* The hall is a glittering oval, white and gold, with a thousand velvet seats and a stage the size of a pond. Clarice Quill walks out in a white dress with a cello, bows to the audience with the grace of a swan, sits, and plays a piece of such technical splendour that Pocket, in the wings, forgets to breathe. It is flawless. It is as clean as a cut diamond. It has not one note in it that has ever been a mistake.",
                        "* You walk out with the zither on your knee. You play for ten minutes. You play the funeral tune, and the wedding tune, and a part of the song your grandmother hummed. It is not flawless. It is not clean. But at the third verse the thirteenth string comes in, from nowhere, and the whole hall goes silent, as a room does when a door has opened somewhere behind it. When you stop, nobody claps. A woman in the front row has her hand over her mouth.",
                        "@Lamberd| (from his box, very clearly, into the stillness) Retune it.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("result", "After the Duel").gap(8)
                .intro(
                        "* The judges retire for an hour. They come back in a body, five old men in black, with the faces of men carrying a bag of snakes. The chief judge reads the verdict from a card. He says that the Academy's champion played a perfect performance. He says that the visitor did not. He says that in the opinion of the panel, after careful consideration, the question of which was the better cannot be answered by the rules. It will be left to the audience.")
                .wait(4)
                .choice(new Chapters.Choice("duel", "The hall of a thousand is on its feet, and the audience has been asked to decide.",
                        "Accept the audience's verdict, whichever way it falls.", "Concede to Clarice Quill: she played better, and you know it.", "Offer to play with her: a duet, the two of you, on stage, and let the music decide.")
                        .after(
                                new String[]{"* The audience votes with their hands. It takes ten minutes. The count is close. It is announced as a thousand and four to nine hundred and ninety-six, in your favour, by a clerk who is plainly in pain. Lamberd's face does not change. Clarice, in her white dress, looks at you across the stage, and bows, very low, and straightens. Her face is not the face of a loser.", "@Clarice| (low, as she passes) I'd like to hear that string again. Some day when no one's watching."},
                                new String[]{"* You concede. You say it clearly, into the silence. You say that Miss Quill played the better performance, and that the zither should be retuned, if the Master insists. Lamberd's mouth opens. Clarice stands very still. Then she says, in a high clear furious voice, 'I will not accept that,' and puts down her bow. 'I did not win. I played a recital. She played a duel. I will not be given a thing that I did not earn.' The hall erupts.", "@Lamberd| (in a strangled voice) The result stands."},
                                new String[]{"* You ask Clarice to play with you. She stares. Then she stands, slowly, and carries her cello to the centre of the stage, and sits beside you. You begin together. It is the strangest sound that hall has heard. The cello is precise and dark. The zither is rough and gold. They fight, and agree, and the thirteenth string comes in, a long way down, under both. A thousand people sit without moving. At the end she takes her bow off the string and says, 'Again.'", "@Lamberd| (very quietly, to himself, in the box) No. Not that. Anything but that."}))
                .outro(
                        "* The zither is not retuned. Lamberd does not insist. He does not speak to you again that evening. As you leave by the stage door a clerk presses a note into your hand. It is on Academy paper, in an upright hand, and says: You have something I have been looking for since I was a young man. I shall not stop. Please accept that I mean it kindly.")
                .pay(300, 0, 0, 0, 0, 1);

        d.chapter("tuning", "The Thirteenth").gap(24)
                .intro(
                        "* A letter comes from the country, by a carrier who has been three weeks on the road. It is from a farmer's wife in a village you have never heard of. It says that she has your grandmother's box. It says it was left with her, forty years ago, with a request that it be kept for a grandchild, and that the grandchild be told something when the time came. She is old now. She says she thinks the time has come.")
                .wait(8)
                .outro(
                        "* The farmhouse is small and white, with a pear tree and a dog. The farmer's wife is ninety, as bent as a hook, with a voice like dry leaves. She leads you into a kitchen with a stove and a rocking-chair, and sits you down, and gives you a cup of something hot and dark, and says nothing for so long that you wonder if she has forgotten.",
                        "@Wife| Your grandmother couldn't tune it either. She tried all her life. She said it was waiting to be answered. She said the thirteenth string was a rope with the other end somewhere else, and that you couldn't tune a rope that was tied to a thing so far away. She said you had to play it to someone who could hear.",
                        "@You| Who?",
                        "@Wife| She never said. She only said she'd heard it answer once. In the Palace, at the Convocation, in the year of the great snow. She said it answered for a moment and then went quiet. She said she thought it was the best moment of her life, and the saddest.",
                        "* She gives you a folded paper. It is in your grandmother's hand, a few lines in pencil, a fragment of a song. The last verse. It breaks off in the middle of a line, like a person who has been called away.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("road", "The Road Ahead").gap(24)
                .intro(
                        "* Ilse is waiting at the Fiddler's Rest. {ilse.He} has been there four days. {ilse.He} has paid the landlord for the corner table, and has been sitting at it with a notebook and a pot of tea, as quiet and steady as a heron, waiting for you to come in from the road. {ilse.He} stands. {ilse.He} says that {ilse.he} has put in for a leave of absence, and that it has been granted, and that {ilse.he} has no idea what to do with it.")
                .wait(8)
                .outChoice(new Chapters.Choice("love", "Ilse Ferrant has taken a leave of absence from the Choir and has nowhere to go.",
                        "Ask {ilse.him} to come on the road with you and the string.", "Say that the road is a poor place for a Listener, and ask {ilse.him} to wait.", "Take {ilse.his} hand, and say that you would rather not travel at all without {ilse.him}.")
                        .after(
                                new String[]{"* You ask. {ilse.He} says yes before you have finished the sentence. {ilse.He} says it with such a blaze of happiness that Pocket, at the next table, drops the pigeon. They leave the next morning, three on the road and a pigeon, with the zither on your back and a notebook in {ilse.his} hand. {ilse.He} walks the first mile saying nothing, with {ilse.his} head tilted, listening to the road. It is the loudest thing {ilse.he} has ever heard.", "@Ilse| Everything has a note. I never knew."},
                                new String[]{"* You say the road is no place for a Listener, and she should go home, and you will find her. It is the wrong thing to say. {ilse.He} looks at you with a hurt, level, clever look and says, 'I am twenty-seven years old. I have been a Listener since I was nine. I know what hurts my ears. I assure you it is not the road.' {ilse.He} stays at the inn another week, and then comes after you, on foot, without a word, in a very good coat.", "@Ilse| (catching up, breathless) I decided that the correct response to being told not to was to be in tune with it."},
                                new String[]{"* You take {ilse.his} hand. It is long and cold and ink-stained. {ilse.He} stares at it. You say that you would rather not go anywhere without {ilse.him}. {ilse.He} says nothing, for a very long time. Then, in a small flat voice, as if reading from a card, {ilse.he} says, 'That is a perfect fifth.' It is, you gather later, the highest compliment {ilse.he} knows.", "@Pocket| (to the pigeon) I told you."}))
                .done((c, st) -> { int l = Chapters.flag(st, "love"); Chapters.bond(c, st, "ilse", l == 2 ? 1 : 3); if (l == 1 || l == 3) Chapters.wed(c, st, "ilse"); })
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "The Road Song",
                "* The road is a long one, and it goes everywhere. There are three of you on it, and a pigeon, and a zither with a pale string that hums at night. It is the happiest you have ever been.",
                "?wedding=1|* The village of Haldane still dances the reels exactly as they were, and has declared a holiday on the first Saturday of May. They call it Zither Day. They have not asked your permission.",
                "?wedding=2|* The tune you wrote for the bride and groom is now played at every wedding in the county. Nobody knows where it came from. They say it is a very old one.",
                "?wedding=3|* People in Haldane say that on the first Saturday of May, near midnight, if you stand in the barn, you can hear a child's dog bark.",
                "?duel=1|* Clarice Quill left the Academy in the spring. She sends a postcard, each month, from a different town, with a drawing of a cello and one word: Again.",
                "?duel=2|* Clarice Quill refused the Academy's prize and plays now in a tavern in Saltwick, where she is, by all accounts, appalling and magnificent.",
                "?duel=3|* A cello and a zither duet is played every year at the equinox in the great hall at Saltwick. Master Lamberd has never once come to hear it. He sends flowers.",
                "* At night, in the camp, when Pocket is asleep and the pigeon is on his chest, Ilse sits with {ilse.his} notebook on {ilse.his} knee and writes down the note of the thirteenth string. It is always the same note. It is always, to the fraction of a hair, a little lower than it was the night before. {ilse.He} says that it is not going flat. {ilse.He} says it is getting nearer.");
        TaleMusicianSeasons.add(d);
    }
}
