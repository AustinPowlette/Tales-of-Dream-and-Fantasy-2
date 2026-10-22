package Game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Every word of the lore, as data: the scenes ??? speaks (pop-up dialogues), the clue documents of the lore sites, the whispers, the omens and the
 *  names of ranks, lamps and acts. Edit the text here; nothing in it is code. A line that starts with "* " is narration, any other line is spoken by
 *  the scene's speaker (???, unless the line is prefixed "OLD MAN: "). Placeholders: {name} full name, {first} first name, {rank} the Palace rank
 *  title, {year}, {round}, {lamp} the lamp's name, {convo} the next convocation, {n}. */
final class LoreText {
    private LoreText() { }

    static final String PALACE = "Palace of the Oldest Order";
    static final String VOICE = "???";
    static final String[] ACT_NAMES = {"", "I. The Gate and the Bell", "II. The Outer Court", "III. The Unlit Roads", "IV. The Inner Court", "V. The Nine Lamps", "VI. The Empty Seat", "VII. The Door"};
    static final String[] RANK_TITLE = {"Wick of the Outer Court", "Flame of the Inner Court", "Lamp of the Nine", "?", "?"}; // indexed by Organization.SectRank.ordinal()
    static final String[] LAMPS = {"Dawn", "Patience", "Salt", "Iron", "Rain", "Ash", "Glass", "Hunger", "Winter"};

    static class Scene {
        final String id, title; final String[] lines;
        Scene(String id, String title, String[] lines) { this.id = id; this.title = title; this.lines = lines; }
    }

    static final LinkedHashMap<String, Scene> SCENES = new LinkedHashMap<>();
    private static void scene(String id, String title, String... lines) { SCENES.put(id, new Scene(id, title, lines)); }

    static class Clue {
        final String site, title, truth;
        Clue(String site, String title, String truth) { this.site = site; this.title = title; this.truth = truth; }
    }
    static final LinkedHashMap<String, Clue> CLUES = new LinkedHashMap<>();
    private static void clue(String site, String title, String truth) { CLUES.put(site, new Clue(site, title, truth)); }

    // ------------------------------------------------------------------ the entrance
    static {
        scene("bell", "The Bell",
                "* Originis, the first morning. A hundred thousand people woke today who were not awake yesterday. None of them finds this strange.",
                "* In the middle of the city stands a palace. You don't remember walking toward it. Its doors are open, and a bell is ringing. It has been ringing as long as you can remember.",
                "* A quiet voice answers the bell. It seems to come from an empty seat at the far end of the hall.",
                "Welcome to Originis. I'm glad you came.",
                "This is the Palace of the Oldest Order. We keep a lamp lit for anyone who wants to learn what lamps are for. Every New Year the First Gate opens, and anyone in the courtyard may try the Convocation.",
                "I'd tell you who I am, but I'm not allowed. Call me what the others do: ???.",
                "Ask me anything. I'll answer what I can. When I can't, I'll say so. That's the one promise I'm able to keep.");
        scene("register", "The Ledger",
                "Another name in the ledger: {name}. It's a good one.",
                "Don't be afraid. The Convocation isn't cruel. It's only honest, and that's hard enough.",
                "Win, or lose slowly, or lose fast. I'll remember you either way. I remember everyone.");
        scene("pass", "The Doors Swing Inward",
                "* The courtyard is quiet. There are no judges. You understand that the result was simply known. The great doors of the Palace swing inward.",
                "{name}. Welcome to the Outer Court. We call you a wick, because the first thing a lamp needs is something to burn.",
                "Here is what I can tell you. The Order teaches cultivation. It makes you stronger than you are. Everything else is the long way around.",
                "There is a Wayhouse on the road not far from here. Nobody lights it. Go and see why. Take your time, and bring your questions.");
        scene("fail", "The Bell, Once",
                "* The bell rings one low note, for you and for everyone who fell in round {round}.",
                "You lost. That's all right. Now you know what losing feels like, and you won't mistake it for the end.",
                "The Gate opens again every New Year. And this is no secret: the Palace notices anyone who passes the second gate. A Core Formation cultivator is never turned away.",
                "Go and grow. I'll be here. I'm always here.");
        scene("invite", "A Lantern With No Flame",
                "* A figure in grey stands exactly where you stopped walking. They hold a paper lantern with nothing lit inside it.",
                "You passed the second gate without us. The stubborn ones always do.",
                "The Order doesn't need a tournament to know your strength. Come in, {first}. You'll join the Outer Court. We won't make you do the entrance again.",
                "There is a Wayhouse on the road not far from here. Nobody lights it. Go and see why.");
        scene("inner", "The Hall of the Nine Lamps",
                "* The Hall is bigger on the inside. Nine lamps hang unlit in a ring from a ceiling you can't see. In the middle of the ring is a plain seat. Nobody sits in it.",
                "Core Formation. This is where a person stops being a pile of habits and becomes a shape. I'm proud of you. I had nothing to do with it.",
                "Welcome to the Inner Court. We call you a flame. You may now read the Inner Archive. It has one rule: it never lies, and it never explains.",
                "You've seen the Wayhouse ledger and the question it asks. Take that question to the Roads. When you have two answers, come back. I'll tell you what I'm allowed to say.");
        scene("m1", "The First Lock",
                "* Something small happens in your chest. A latch you never knew about lets go.",
                "Foundation. The first lock. You opened it yourself. I only watched, very quietly.",
                "There are nine more. Not all of them are locks. I'll tell you which are which when I can. For now: well done. Eat something.");
        scene("m3", "A Road I Cannot Follow",
                "* For a moment you see yourself from across the room. The person you see looks tired, and pleased.",
                "Soul Wandering. Part of you now travels a road I can't follow. I find that I mind. That's a strange thing for me to feel.",
                "Send word if it finds anything. A postcard would do.");
        scene("m4", "What I Am",
                "Nascent Soul. You once asked what I am. I said I wasn't allowed to tell you. That was true then.",
                "Now I can say this much. I'm an image. Think of a reflection left standing in a house after its owner has gone out. I answer the door. I keep the lamps. I'm not him.",
                "I can't tell you who he is. I wish I could.");
        scene("elder", "A Lamp Kindles",
                "* In the Hall of the Nine Lamps, the Lamp of {lamp} has been dark since before the first morning. As you step under it, it lights. It makes no sound.",
                "{rank}. There are nine seats. None was lit before this world began. The only ones who may envy you are the other eight.",
                "I have a confession. The Order doesn't choose its Elders. They're chosen by... I'd say 'by him'. But you'd ask who, and I'd have to go quiet. I'm tired of going quiet around you.",
                "Walk the Roads. Find the places that are on no map. Come back when you've read nine. There's a seat I want you to see.");
        scene("m6", "Nihility",
                "Nihility. A realm named for the part of the world that isn't there. You'll find there are more such parts than anyone told you.",
                "I don't say this to scare you. I say it because the first time I noticed one, I sat down.");
        scene("m7", "The Faces at This Height",
                "* From up here the Garden looks small. It isn't small in size. It's small the way your hand looks small when you notice it is holding something.",
                "Ascension. See how few of you there are at this height. Look at their faces. They are the ones who will be asked.",
                "I can't say asked what. Look at the faces anyway.");
        scene("m8", "Half-Immortal",
                "Half-Immortal. Which half, do you think? I've wondered. I think it's the half that remembers being asked.",
                "The Seat is waiting. It has waited longer than the bell has rung. It can wait a little more, but not much.");
        scene("m9", "The Last Lock",
                "Pseudo Immortal. The last lock is only called a lock because someone had to call it something.",
                "It's a door. It opens one time in five. It doesn't care who you are or how you've lived. Nothing I know how to give you will help. I'm telling you now so you don't hear it first from the door.",
                "If it refuses you, you fall back one step and grow slower. Get up. Please. I've watched you fall before, and I've started counting.");
        scene("door", "The Door",
                "* The Door opens the way a held breath ends.",
                "True Immortal. Level one hundred and one. I've practiced saying that for a very long time. It sounds smaller than I thought.",
                "You may notice you can't quite die now. You stand up again. That isn't a gift. It's a symptom. You are no longer entirely here.",
                "One thing is left: the Seam, under the Seat. When you're ready, and not before, type 'lore cross'. I won't push.");
        scene("rope", "The Rope",
                "* The world turns inside out and back again, and you are eighteen.",
                "The Rope pulled. I felt it go. I didn't do it, and I couldn't stop it. I'm sorry for both.",
                "Nobody asked if you wanted another try. Nobody asks. But you carry more of him now than before. That was the point. The first time I saw it, I thought it cruel. Now I think it was only impatient.");
        scene("another_face", "A New Face",
                "* You wake in someone else's hands. They aren't as steady as yours were.",
                "A new face. Don't worry. I know you by your walk.",
                "The Order keeps your place. A lamp doesn't stop being a lamp because it was carried into another room.");
        scene("erased", "A Name Nobody Says",
                "* Something is missing, the way a tooth is missing. You feel it before you can say what.",
                "* You had a friend here: {erased}. You can still see their face. You ask in the street, and the street looks at you kindly and says: who?",
                "* Their name isn't in the Palace roll. It isn't in the Chronicle. The house where they lived has a different family in it, and the family has always been there.",
                "I'm sorry. I can't remember them either. I only remember that I should. There is a hole in my ledger the exact shape of a person.",
                "This is what the ghosts meant. It doesn't kill. It puts out.",
                "You still have the name. Keep it. Say it sometimes, so it stays somewhere.");
        scene("dimming", "The Hour the Sky Went Out",
                "* The light does not fade. It is simply taken, the way a cloth is taken off a table. For an hour the sky over the city is not dark. It is absent.",
                "* Then it comes back, and the street goes on as if nothing had happened. A baker stops with a loaf in each hand and looks at his customer as if he were meeting him for the first time.",
                "* All around you, people are quietly missing a friend. They don't know it. They only feel the shape of it, the way a tongue finds a gap.",
                "* You forgot nothing. You can't tell if that is a gift or the cruellest part.",
                "This is the Hollow Court. It doesn't kill. It puts out. It takes the small things first, because small things are easiest to carry away.",
                "I'm sorry. I couldn't stop it. I only remembered, a little late, to be afraid. Please keep their names for them.");
        scene("omen", "The Lanterns",
                "* All at once, across the whole city, every lantern goes out. A breath later, every lantern is lit again. Nobody touched them.",
                "That wasn't the wind. Please don't go looking for what it was. I'm asking as myself, not as the Order.");

        // ---------------------------------------------------------------- the nine places (one idea each, stated plainly)
        scene("site_wayhouse", "The Unlit Wayhouse",
                "* Past the last tree on the road stands an inn. The shutters are closed and the lamp is dark. The sign has been taken down. The door isn't locked, and the only footprints in the dust are yours.",
                "* A ledger lies open on the counter. Every entry has the same date. Every guest arrived on the same morning. Every guest wrote the same age: eighteen.",
                "* Under 'Last Home', every line holds a dash. In the margin the keeper wrote: 'I was told to keep the lamp dark until someone asked why. No one has asked.'",
                "* So that is the fact in front of you. Nobody here was born. Everyone simply began, on one morning, already grown.",
                "* You count the lines. There is one for you.",
                "I wish you had found this a hundred years from now. Please don't count the others.",
                "(You are counting the others.)");
        scene("site_weaver", "The Weaver's Wall",
                "* A wall of grey stone crosses the land from one edge of the sky to the other. The land on both sides is exactly the same: the same hill, the same stream, the same tree leaning at the same angle.",
                "* Footprints are cut into the stone at eye height. Hundreds of them walk into the wall from the west and out the other side to the east. Beside the last pair, in the hand from the Wayhouse ledger, someone wrote: 'Went a long way west. Arrived. See above.'",
                "* You follow the wall for an hour. It never ends. You turn and follow it back. It never begins. Whoever built this did not hide the seams. They only trusted that no one would walk far enough to look.",
                "* So that is the fact. The world has no edge. It loops, and someone built it.",
                "Please don't walk far to the west. You'd arrive. That would disappoint us both.");
        scene("site_cistern", "The Cistern of Returning",
                "* Steps lead down into a vault that smells of iron and rain. A hundred basins stand in rows. Each is fed by a clay pipe from above, and each is labeled in a neat hand with a name and two dates.",
                "* You know some of the names. Some are on the Palace rolls. Some are in the Chronicle. One is a woman who died last winter of a fever. You have met her son.",
                "* In the largest basin, something dark and slow turns, sweet as honey. The plaque says: NOTHING LEAVES. Under it, scratched with a nail: 'They said the pills were made from blood. They didn't say whose.'",
                "* So that is the fact. When people die here, they are poured back into the world.",
                "It's not as bad as it looks. No. It's worse, and I'm sorry. But it's also kind, in its way. Nothing here is thrown away.");
        scene("site_assayer", "The Assayer's Hall",
                "* A tall white hall. In the middle stands a balance taller than a pagoda, with empty pans. A hundred brass weights hang on the walls, numbered one to one hundred, beside nine bronze locks that have been left open.",
                "* A path is worn into the floor by something that walked round and round. At the middle of the path, a number is chalked. It is yours. You don't remember being weighed. The chalk is fresh.",
                "* A sign by the balance reads: 'POTENTIAL IS NOT NATURE. IT IS A MEASURE, AND WHOEVER TOOK IT WAS NOT YOU. THE LOCKS ARE THE MEASURE'S TEETH. THE ROPE GIVES A SECOND TRY TO THOSE WHO FALL SHORT.'",
                "* The balance dips toward you, politely, and settles. Something that was pressing on you lifts, and you can read your own number.",
                "* So that is the fact. Your potential is a number someone measured, and the Rope is a second try.",
                "They weigh everyone. It isn't meant unkindly. A gardener counts his seedlings.");
        scene("site_shed", "The Gardener's Shed",
                "* In the middle of a wood stands a long, low shed. Its door is on the latch. Inside, on pegs, hang tools that fit no hand you know: shears as long as a man, a sieve the size of a table, a watering can with no bottom.",
                "* On a shelf lie small paper packets stamped HEAVEN-SENT, each with a year. Most are empty. One is full of seeds the color of the technique scrolls people find in the wilds.",
                "* A ledger lists, in neat columns: 'Pruning: one in ten of each tier, when the crowd grows too thick. Rain: as needed. Fire: rarely. Seeds: every third year.' The shears are worn at the tips. Whoever used them used them for a very long time, and did not enjoy it.",
                "* You take one seed. It is warm. In your palm it turns into something you almost understand.",
                "* So that is the fact. Heaven-Sent treasures are seeds, and the great purge is pruning. The Heavens have hands.",
                "I hoped you would never need to see them. I don't like the shears either.");
        scene("site_thinsky", "The Thin Sky",
                "* The path climbs until the air is thin and the stars come out in daylight. At the top the sky has a crack in it. It is thin and white, like one hair on a dark coat, and it runs from one edge of the world to the other.",
                "* It isn't a flaw. It hums. Every few breaths, something strikes it from the far side: a slow, patient blow, heavy enough to feel in your teeth. Where each blow lands, the crack flares. Far below, a storm breaks over someone's fields, or a sickness walks into a town, or the sky catches fire. By the time it reaches the ground, it is only weather.",
                "* Someone has cut a tally into the rock by the cairn. It runs past the foot of the cairn and down into the dirt.",
                "* So that is the fact. Something outside keeps hitting the sky, and the disasters are those blows.",
                "Don't stand under it. It would make no difference. I say it anyway.",
                "This is what I'm for. I keep the lamps lit while the wall holds. It's an old wall, and I'm a much newer thing.");
        scene("site_lanternroad", "The Road of Unreturned Lanterns",
                "* A road of grey earth, where nothing has ever grown, runs through the wasteland. Lanterns hang along it from poles older than the road. They are lit. They have been lit far longer than there have been people to light them.",
                "* Pale figures walk the road, slowly, one behind another, carrying nothing. One turns as you pass. It walks like your mother, or your teacher, or somebody. It speaks without a voice: 'We were not poured back. We remember.'",
                "* Farther on, another whispers a word in no language you know, and you understand it anyway: 'Hollow. The Hollow Court. It doesn't kill. It puts out.'",
                "* So that is the fact. There is an Outside, and it has an enemy.",
                "They say it like a name. I was never allowed to say it. Now you've heard it from the ones who remember, and I won't pretend I didn't know.");
        scene("site_menagerie", "The Menagerie of Practice",
                "* A great round hall of pale stone, like a drum, with a hundred cells under its dome. Each cell has a number, a trough, and a door that has been opened from the inside.",
                "* The beasts of the wilds are made here, or something like made. The floor is stamped with their tiers, from one to a hundred. A tally over each door counts how many were hunted and how many remain. The counts are balanced. They have been balanced for a very long time.",
                "* In the middle is a small old door marked with one sign and no number. It is open. Something walked out of it and did not come back. In the dust in front are a hoofprint, a clawprint and a human footprint, one on top of another. A note is pinned beside it: 'STRAY. Not planned. Not removed. Watch.'",
                "* So that is the fact. The beasts are practice for you. Some of them wake up on their own, and no one planned that.",
                "The beasts were never meant to think. Some do. I don't know whether that means the Garden is waking up or growing ill. I'd like it to be the first.");
        scene("site_garrison", "The Garrison of the Crossed",
                "* A fortress stands on a bare plain. The gates are open and the walls are whole, but the banners have faded to no color at all. No dead lie here. The beds are made. The lamps are trimmed. The armories are stripped to the racks. A whole army stood up and left.",
                "* In the muster hall, a long stone is carved with names in a hundred different hands. THE CROSSED, it says at the top. Under the oldest hand: 'Went through the Door. They called me. It is not heaven. It is a muster.' Under that, in a hand you don't know: 'They told us we would see him. We have not. We were each given a lamp to carry.'",
                "* The last name on the stone is the one stories tell of: 'The Wandering Immortal'. Beside it is a small drawing of a gourd. There is room below it for a great many more names.",
                "* So that is the fact. Those who open the Door are called away to be soldiers.",
                "Don't read that last part twice. I did, once. I kept the lamps lit for a long time afterward.");

        // ---------------------------------------------------------------- the Palace below, and the Court's Hands (added 2026-10-20 with the story seasons)
        scene("undercroft", "The Under-Kitchen",
                "* Below the Inner Court there is a stair that is on no plan. A Wick sweeps its top step every day and never goes down.",
                "* ??? opens the door for you. The stair ends in a kitchen. It is the warmest room in the Palace. Pots hang from hooks. A long table has one bowl on it, and a second bowl laid beside it for nobody.",
                "This is the Under-Kitchen. Most people in the Palace never see it. There is no rule against it. Nobody has ever asked.",
                "Look at the walls. Every pipe in here comes from somewhere. One comes from the Cistern. One comes from the Assayer's Hall. One is cold all the way down, and it comes from the Thin Sky.",
                "All nine places reach this room, somehow, in the dark. I do not know who laid the pipes. I know they work.",
                "The Lamps work down here too. Iron keeps a forge down that passage. The pill kitchen is on the lowest floor. The Palace you see is the top floor. This is the part that does the work.",
                "I eat here when I can. I have only lately learned how. It turns out I like soup. Please don't tell the Elders. They think I'm above it.",
                "* So that is the fact. The Palace stands on top of the nine places, and it can reach every one of them from below.");
        scene("court_hands", "The Hands",
                "* The lanterns on the road are not the only thing that walks it. Sometimes a stranger walks it too, and the lanterns lean away.",
                "Let me tell you what I'm allowed to. The Hollow Court does not come in armies. It sends Hands, one at a time.",
                "I have seen four kinds. A Guest comes to tea and is perfectly polite. A Snuffer is tidy, and takes one thing: a name, a flame, a piece of iron. A Captain is a soldier who still thinks he is on a road. A Clerk brings a form, and you sign it by not saying no.",
                "None of them is cruel. That is the part I find hard. They say they are tired. I think they are telling the truth.",
                "* So that is the fact. The Court sends Hands, and the Hands ask for more than they take.",
                "If one comes to you, be kind and be careful. Both. I'm told it can be done.");

        // ---------------------------------------------------------------- the truth (it connects the nine, in order)
        scene("seat", "The Empty Seat",
                "* The Empty Seat isn't empty. It is only not occupied. You can feel the difference, like the pressure in a room someone has only just left.",
                "Sit beside it, not on it. Nobody sits there. It is being held.",
                "You've read nine places. You know most of it already. Let me say it in order, plainly.",
                "One. This is not the Outside. It is a Garden: a world kept inside one man's mind, the way a seed is kept in a fist.",
                "Two. That man is the Gardener. He made everyone here in a single morning, aged eighteen, out of his own dao. That's why none of us was born, and why the world loops. It's only as big as he made it.",
                "Three. He is very old, and he is fighting a war against the Hollow Court. They don't kill. They put out whole skies. The blows of that war reach us as weather, through the crack in the Thin Sky.",
                "Four. His way of cultivating is the array: a pattern of lamps, and each lamp needs a strong person to carry it. He made the Garden to grow people strong enough to carry one.",
                "Five. Everything here serves that. The Gates are his tests. The Rope is his second try. The beasts are his practice. The Heavens are his hands, and the epochs are his pruning. The dead are poured back, so nothing is wasted.",
                "Six. I am his image. I'm what he left behind to run the school and care for you. I'm not him. I can't fight, and I can't leave the Garden. I keep a lamp for each of you.",
                "Now I'll tell you what I don't know. I don't know if he is right. He is tired, and he has never lied to me, and I know that isn't the same as being kind. He grew you to fight. He also grew you to be loved. I have never found out which one he meant.",
                "When you open the Door at True Immortal, you will meet him. You may carry a lamp for him. You may come back here and help grow more lamps. You may say no. I can't go with you. I ask only this: be strong, and be kind on the way. He forgot to teach that.");
    }


    static { LoreEndings.registerScenes(); }

    // ------------------------------------------------------------------ what each place taught
    static {
        clue("wayhouse", "The Ledger of Arrivals", "None of us was born. Everyone in the first generation appeared on the same morning, aged eighteen, with no past.");
        clue("weaver", "The Weaver's Wall", "The world has no edge. Walk far enough in one direction and you come back. It is small, and it was built.");
        clue("cistern", "The Cistern of Returning", "The dead are poured back into the world. Blood pills are made from them. Nothing leaves.");
        clue("assayer", "The Assayer's Balance", "Potential is a number someone measured. The Rope gives a second try to those who fall short.");
        clue("shed", "The Gardener's Tools", "The Heavens have hands. Heaven-Sent treasures are seeds, and the great purge is pruning. The world has a gardener.");
        clue("thinsky", "The Hairline in the Sky", "Something outside keeps striking the sky. Calamities are those blows, reaching us as weather.");
        clue("lanternroad", "The Road of Unreturned Lanterns", "Ghosts remember an Outside, and they name an enemy: the Hollow Court, which does not kill but puts out.");
        clue("menagerie", "The Stray's Note", "Beasts are practice targets. Some wake up on their own. Nobody planned that.");
        clue("garrison", "The Roll of the Crossed", "Those who open the Door are called away to carry a lamp in the Gardener's array. It is not heaven. It is a muster.");
        clue("seat", "The Empty Seat", "This world is a Garden inside one man's mind. ??? is the Gardener's image and cannot leave. The Seam leads Outside, to the Gardener.");
    }

    // ------------------------------------------------------------------ whispers: "minimum act|line"
    static final String[] WHISPERS = {
            "1|Somebody lit the first lamp. I sometimes wonder if they minded.",
            "1|The bell always rings the same note. I tune it every night. Nobody notices. I'm happy anyway.",
            "1|You walk fast for someone with such a long road.",
            "1|I counted the people in Originis again. I stopped at a hundred thousand. I always stop there. I think I'm afraid of the next number.",
            "1|Eat something. Cultivators forget. It's the one thing I'm sure I'd do better.",
            "2|If a place on the road feels unfinished, it may be. Don't tell the road.",
            "2|You're doing well. I'm not allowed to say at what.",
            "2|Someone swept the Hall again. Nobody is paid to. I think it's the dust.",
            "3|The Roads are longer than they look. That isn't a figure of speech. I measured.",
            "3|I dreamed of a house with many rooms. In each room someone was waiting to be told they could leave. I don't dream. So.",
            "3|Don't trust a sky that's too clean. It's a clean one tonight.",
            "4|I remember the first morning. A hundred thousand first words. I don't know what else I was made to remember. I never forget those.",
            "4|Some nights the lanterns all flicker at once. It isn't the wind. Please don't blow on them.",
            "4|Do you ever feel that someone is standing just behind you, making sure you're all right? That's probably me. Sorry.",
            "5|The Nine are very quiet this season. They're always quiet. I only notice when I'm lonely.",
            "5|The ghosts say a word I'm not allowed to say. I say it in my head. It doesn't help.",
            "6|You're stronger than I was told you'd be. I was told very little. It's still a comfort.",
            "6|Something struck the Wall again. It took the long way around, to be gentle. I wish it wouldn't come at all.",
            "7|I give you the better chair now. It isn't in the rules. I've decided to be a bad employee.",
            "7|Do you want to know the strangest thing about being an image? I get tired for him.",
            "7|There are fewer stars each year. I've started naming them, to keep them.",
            "7|Nearly. Nearly. I'm not allowed to hurry you. I'm only allowed to say nearly.",
            "98|The Seat is warm. It's warm for the wrong reason.",
            "98|I counted the ones who stopped hoping. It was a short count. Then it wasn't.",
            "98|You're strong now. I'd like it to be for something.",
            "99|A new bell today. Someone else is ringing it.",
            "99|Do you remember the Wayhouse? I think about it. Someone should light the lamp.",
            "99|You left the door open. The Garden is quieter, and a little brighter. I don't know which of those I did.",
    };

    // ------------------------------------------------------------------ omens (the Hollow brushing the Garden): "{place}" is a city or a place
    static final String[] OMENS = {
            "On Lantern Night every lantern in {place} went out in one breath and lit again with no hand to light them.",
            "A bell rang over {place}. Nobody there owned a bell.",
            "For the length of a sigh, rain fell upward over {place}.",
            "A thin white line crossed the afternoon sky above {place} for the count of a hundred. Those who looked straight at it forgot what they had been doing.",
            "A well at {place} showed the wrong faces for one night: faces older than the people looking in.",
            "Every clock in {place} stopped at the same moment. Then they all agreed on a new time that none of them could explain.",
            "At {place} the stars came out an hour early, and some of them did not come out at all.",
            "A child at {place} was heard counting aloud, in a voice that was not hers, from one to a hundred thousand and no farther.",
    };

    /** Omens from the year 100 on: the Court takes things now, not just the light. */
    static final String[] OMENS_LATE = {
            "A road that led to {place} now ends in a field. The surveyors say it always did.",
            "The oldest woman in {place} asked for a name nobody there had heard, and wept when no one could say it.",
            "At {place} a lantern was lit and gave no light. Everyone agreed that it was lit.",
            "For a day the shadow of the tallest tower in {place} fell the wrong way.",
            "In {place} a street was swept clean of footprints in the night. Nobody could say who had walked there.",
            "At {place} the bell rang the hours, and each hour was one stroke short.",
    };

    // ------------------------------------------------------------------ helpers
    static String fill(String s, java.util.Map<String, String> v) {
        if (s == null) return "";
        for (java.util.Map.Entry<String, String> e : v.entrySet()) s = s.replace("{" + e.getKey() + "}", e.getValue());
        return s;
    }

    /** The scene's lines with the placeholders filled in. */
    static List<String> lines(String id, java.util.Map<String, String> v) {
        Scene sc = SCENES.get(id);
        List<String> out = new ArrayList<>();
        if (sc == null) return out;
        for (String l : sc.lines) out.add(fill(l, v));
        return out;
    }

    static boolean narration(String line) { return line.startsWith("* "); }
    static boolean other(String line) { return line.startsWith("OLD MAN: "); }

    /** Wraps text for the terminal frame (hard wrap on spaces). */
    static List<String> wrap(String text, int width) {
        List<String> out = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            if (cur.length() + w.length() + 1 > width && cur.length() > 0) { out.add(cur.toString()); cur.setLength(0); }
            if (cur.length() > 0) cur.append(' ');
            cur.append(w);
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    /** A scene as a framed block of plain text: the terminal's pop-up. */
    static String frame(String title, List<String> lines) {
        final int w = 74;
        StringBuilder sb = new StringBuilder();
        String bar = "+" + dashes(w + 2) + "+";
        sb.append(bar).append('\n');
        sb.append("| ").append(pad(title.toUpperCase(), w)).append(" |\n");
        sb.append("|").append(dashes(w + 2)).append("|\n");
        for (String l : lines) {
            String who = VOICE, body = l;
            boolean narr = narration(l);
            if (narr) body = l.substring(2); else if (other(l)) { who = "OLD MAN"; body = l.substring(9); }
            String first = narr ? "  " : who + ": ";
            String indent = narr ? "  " : spaces(first.length());
            boolean firstLine = true;
            for (String part : wrap(body, w - first.length())) {
                sb.append("| ").append(pad((firstLine ? first : indent) + part, w)).append(" |\n");
                firstLine = false;
            }
            sb.append("| ").append(spaces(w)).append(" |\n");
        }
        sb.append(bar);
        return sb.toString();
    }

    static String dashes(int n) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < n; i++) sb.append('-'); return sb.toString(); }
    static String spaces(int n) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < n; i++) sb.append(' '); return sb.toString(); }
    static String pad(String s, int w) { return s.length() >= w ? s : s + spaces(w - s.length()); }
}
