package Game;

/** Status Window (Loading), seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "Patch Notes" (comic bureaucracy, card A with the System parody): the panel goes down for a week, and the human finds that five other people carry panels of their own. A Clerk of the Hollow Court, Mister Patch,
 *  arrives to apply a correction. The panel is the Assayer's number made readable: potential is a measure someone took, and the human's reads wrong. Choices: report (what is filed), hotfix (what Patch is allowed to fix),
 *  rollback (an undo is offered). Flags: report, hotfix, rollback.
 *  Season 3, "End User" (cosmic and gentle): the panel is a scrap of the Gardener's own view of the Garden, written by accident when the Image of the Gardener tried to say hello. The Court wants it uninstalled. Flags: panel, meeting. */
final class TaleCheaterSeasons {
    private TaleCheaterSeasons() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "report", "hotfix", "rollback", "panel", "meeting");
        d.place("palace", "start").place("archive", "far");
        d.cast("patch", "Mister", "Patch").he().keep().trait(Character.Traits.Scribe).side("Neutral").lvl(14).pot(8).age(40).at("far").rel(0);
        d.cast("greta", "Greta", "Thorn").she().keep().trait(Character.Traits.Gatherer).side("Good").lvl(3).age(33).at("far").rel(2);
        d.cast("odo", "Odo", "Plimsoll").he().keep().trait(Character.Traits.Merchant).side("Neutral").lvl(3).age(41).at("far").rel(2);

        // ================================================================== SEASON 2
        d.season(2, "Patch Notes");

        d.chapter("maintenance", "Scheduled Maintenance").years(2)
                .setup((c, st) -> { Chapters.spawn(c, st, "nib"); Chapters.spawn(c, st, "vesna"); })
                .intro(
                        "* The announcement comes on a Sunday morning, in a very polite banner. [SCHEDULED MAINTENANCE. The Status Window will be unavailable for approximately seven days. We apologise for the inconvenience. Thank you for your continued patience.] Then the panel goes grey. Then it goes black. Then it is gone, with a faint electronic sigh, like a candle snuffed in a very small room.")
                .wait(12)
                .outro(
                        "* The first day is wonderful. You wake and do not look at the corner of your eye. You make breakfast and no one gives you a medal. You say good morning to a stranger and nothing pings. By noon you are walking down the high street with your hands in your pockets feeling like a man who has put down a very heavy box.",
                        "* By the third day it has started to feel like a limp. You go to cross the road and wait for the arrow. You meet an old friend and hold back, waiting to be told how much she likes you. You lose a button and stand in the street looking at the air where the Inventory ought to be. Nib asks what is wrong. You say you cannot remember whether you are strong.",
                        "@Nib| You're strong enough. You carried the sack of flour.",
                        "@You| But what's my number?",
                        "@Nib| (with real alarm) I don't think there ought to be a number. I think you just carried it.",
                        "* On the seventh day, at dawn, the panel comes back. [MAINTENANCE COMPLETE. Welcome back, Host.] You feel a rush of relief so ridiculous that you have to sit down on the kerb.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("support", "Support Group").gap(16)
                .intro(
                        "* The notice is pinned to the board in the tavern. It is written in a spidery hand on a page torn from an account book. It says: DO YOU SEE A BOX? YOU ARE NOT ALONE. FIRST AND THIRD THURSDAYS, BACK ROOM OF THE LAMB, EIGHT O'CLOCK. BRING A CHAIR. You read it four times. You tear it down and put it in your pocket. You are at the Lamb at five to eight with a chair under your arm.")
                .friends(5, "Find the others: make five friends.")
                .outro(
                        "* There are six of you. There is a stout cheerful woman in a green coat called Greta, who sees a countdown over everybody's head and has not yet worked out to what. There is a man named Odo Plimsoll, who has a shop menu in the corner of his eye and cannot stop appraising things. There is a monk whose panel is entirely in Latin, a girl whose panel only shows the weather. A very old lady in a lace cap whose panel has an Accessibility Mode, which, as she says, is the only sensible part.",
                        "@Greta| I thought I was losing my mind. I thought there was something in the cheese.",
                        "@Odo| I've been unable to look at a sofa without reading its price. It's ruined furniture for me.",
                        "@Old Lady| Mine tells me I've got eleven years. I'm ninety-four. I've been taking it in good part.",
                        "* You tell them about your own. They listen with great attention. When you describe the Status Window, and the Lie Detection, and the Rat King, the monk puts up a hand and says in careful English: 'It is a very specific problem, to be told you are special. It is not the same as being told you are loved.' The whole room is quiet. Greta passes the tea.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("notes", "Compare Notes").gap(8)
                .intro(
                        "* You compare the panels on the third Thursday with the tea and the biscuits and a large sheet of brown paper on the wall. It is the most extraordinary evening of your life. Every panel has something in common. Every panel has something odd. And every panel, when you hold them up side by side, has a flaw in precisely the same place.")
                .wait(4)
                .outro(
                        "@Odo| The numbers. Look. It's the same thing. Mine says I'm Level Three. Greta's says Level Three. Yours says Level One, but has a footnote.",
                        "@You| It says: See Appendix.",
                        "@Greta| What's in the Appendix?",
                        "@You| It's greyed out.",
                        "* The monk draws a diagram on the brown paper in charcoal. It is a spiral, with a number at each turn. He says that every one of them has a small mistake in the way the panel shows their potential. He says it is like a thermometer that is off by a degree. He says that in some of them it shows too high, and in some too low, and that in yours it shows a figure that does not belong on the scale.",
                        "@Monk| Yours says: Cheater. This is not a number. This is an accusation.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("mrpatch", "Mister Patch").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "patch"))
                .intro(
                        "* The man who calls at the Lamb on the fourth Thursday is slim, grey and tidy, in a charcoal suit with narrow lapels. He carries a briefcase and a clipboard. His shoes shine. He has a face so mild and neutral that it seems to have been produced by a committee. He says good evening to the room. Every one of the six panels flares in alarm.")
                .meet("patch", "Hear what the visitor has to say.")
                .outro(
                        "@Patch| Mister Patch. Clerk, Department of Corrections. I represent a party, as one does. I have been assigned to the matter of a number of irregularities in the Interface. I am sorry to intrude on your tea. I should say at once that nobody here has done anything wrong. The Interface has. It is a very old piece of work, written in a hurry, with nobody to review it.",
                        "@Greta| Is it going to hurt?",
                        "@Patch| Not in the least. A patch is simply a small adjustment. A tidying. It sets the numbers right. It removes the irregular elements. Some of you will not notice. One or two will feel a certain, ah, narrowing.",
                        "* He looks at you. His eyes are the grey of a wet slate. He says that yours is the case that concerns him. He says the Cheater flag is an error that should never have been written. He says it gives you five points of strength you have not earned, and a licence to alter rules you do not understand. He says he is sure you will see the sense in withdrawing it.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("bug", "Filing a Bug Report").gap(8)
                .intro(
                        "* There is a procedure. Mister Patch explains it in a pleasant, rather weary voice, with the air of a man reciting the rules of a game he has never enjoyed. A bug report must be filed in triplicate, on a green form, at a desk, in the presence of a witness. It must state the nature of the bug, the date it was first observed, the steps by which it may be reproduced and the expected behaviour. It must be signed.")
                .wait(4)
                .choice(new Chapters.Choice("report", "You may file a bug report against the Cheater flag, and a stay of correction while it is heard.",
                        "File it as a bug in the system: the flag is an error, and should be fixed.", "File it as a feature: the flag is a gift, and should be kept.", "File it as a question: what is the flag for?")
                        .after(
                                new String[]{"* You write it carefully. Nine lines. Steps to reproduce: Be me. Expected behaviour: Be treated as a person. Actual behaviour: Be treated as a cheat. Mister Patch reads it twice. He looks up. He says: 'That is not a bug report.' You say you know. He says: 'That is the most accurate one I have ever received.' He stamps it.", "@Patch| It will be heard. I shall be obliged to rule against you. But it will be heard."},
                                new String[]{"* You file it as a feature. You write: Works as intended. The Clerk is silent for ages. Then he says, in a tone of the faintest wonder, that nobody has ever claimed that. He says the department has a form for features, but it has never been used. He goes away to look for it and returns half an hour later, dusty, carrying a single blank sheet.", "@Patch| I am sorry to say it exists. I do not know what it is for."},
                                new String[]{"* You file it as a question. You write: What is it for? The Clerk looks at the page for a very long time. It is the only form he has ever been handed that had a single sentence on it. He turns it over, to see if there is more on the back. There is not. He folds it in four and puts it in his waistcoat, and says: 'I shall have to ask someone.'", "@Greta| That's the first time I've seen anyone make him blink."}))
                .outro(
                        "* The hearing is set for the first of the month. In the meantime the correction is stayed. Mister Patch bows to the room and goes out into the dusk. The six of you look at each other in the glow of your six panels. Not one of them is saying anything. It is, the old lady observes, rather like being at a wake for someone who has not yet died.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("exploit", "The Exploit").gap(16)
                .setup(TaleCheaterSeasons::mark)
                .intro(
                        "* The Clerk's visits are regular and unhurried. He calls every Tuesday at four with the clipboard and a flask. He sits in the kitchen. He asks questions. How many times a week do you use the Cheater flag? In what circumstances? What does it do, in your experience, that an ordinary Host could not? You answer as truthfully as you can. It is a short list.")
                .made(8, "Prove the flag's worth: make eight things.")
                .outro(
                        "* You begin to realise it is not an exploit. It is not the five points of strength, or the Lie Detection, or the cheats the panel will, if you press the right place, grudgingly allow. It is the small habit of looking at a rule, and wondering whether it has to be so. You have been doing it all your life. The panel has only put a label on it.",
                        "* The Clerk takes notes. At the end of the month he puts down his pen and says: 'I find that I am obliged to report that the Cheater flag does what it says on the tin. It lets you change the rules.' He is silent. 'It does not do much. It is a small, local, personal sort of change. It lets you, on occasion, step to one side.'",
                        "@You| Is that a crime?",
                        "@Patch| It is not a crime. It is a deviation. I am bound to correct deviations. I wonder whether I might, just once, be bound a little less.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("depart", "The Department of Interfaces").gap(16)
                .intro(
                        "* He brings you himself, on a Tuesday, in a hired cab. It is a long drive. He sits opposite you with the briefcase on his knee and looks out of the window with a stern tight face. The cab stops at the gate of the Palace of the Oldest Order. He leads you through the great hall, past the Lamps, down a stair that is on no plan, and along a corridor lined with filing cabinets.")
                .at("palace", "Go with Mister Patch to the Palace of the Oldest Order, at Originis, and down to the Department of Interfaces.")
                .outro(
                        "* The Department is a desk. It is a single plain oak desk at the end of the corridor, under a lamp, with a pen, a rubber stamp and a pot of green ink. Behind it, in a plain apron, sits a man of no age with a bowl of soup. He has a pair of spectacles on his nose and a pile of forms in front of him. He rises when you enter. He sits again. He rises. He says his name is ???, and that he is the Department.",
                        "@???| I am allowed to explain. It is the one thing the Department is for. The Interface is a translation. The people of the Garden were made with a number in them, a thing someone measured, and the number cannot be read by the people themselves. A long time ago someone wrote a way to show it. A small window, in the corner of the eye, that tells a person what they are. It was meant for one person. It leaked.",
                        "@You| Whose?",
                        "@???| That is the part I am not allowed to say. I am allowed to say that the first line of the Interface was written by accident. A tired man was looking at the Garden and trying to say hello. He did not know he was writing code. He wrote: Welcome, Host. And it stuck.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("source", "Vesna Reads the Source").gap(8)
                .intro(
                        "* Vesna has the afternoon off. She spends it in the Department's archive at a long table with a stack of forms and a pot of coffee. She says that she cannot make sense of the numbers but that she has always been good with recipes, and a source is much the same as a recipe, with fewer onions. She is still there at midnight. She comes out into the corridor with ink on her nose and a very strange look.")
                .wait(4)
                .outro(
                        "@Vesna| It's a poem. I swear to you it's a poem. It's got a rhythm. There's a refrain. It goes: Host, Host, who are you? and then there's a line that says Please be well, and then another that says I hope the soup is warm. Everything else is the numbers. But those lines are in the margin of every record in the archive. I think he's been saying them to everybody. I think the panel was never meant to rank you. It was meant to wave.",
                        "* You take the page from her. It is a long thin strip of green-lined paper, written in a plain round script. In the margin, in a different ink, some past reader has added in pencil: Do not let them fix it. It is signed with an initial. It is an initial you know.",
                        "@???| (very softly, from the end of the corridor) I did not know anyone had read that far.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("tooltip", "Tooltip").gap(16)
                .intro(
                        "* You find out what the panel is for on a rainy Wednesday in the Lamb. You are looking at a stranger across the room, a tired man with a bag of tools, and out of idle habit you let your eye rest on him. The panel gives a faint chirp. A small grey box appears beside his head, like a note pinned to a coat. [Tooltip: Has not slept in two days. Is afraid of the foreman. Loves his daughter. Hates the weather.]")
                .wait(8)
                .outro(
                        "* You have seen the tooltips before. You have always found them irritating. They have been your little jokes, about hungry goblins and flustered tax-collectors. You have never read them properly. You read them now, down the length of the room, one after another. The barmaid: Wants to be a singer. The farmer: Is dying and has told nobody. The boy under the table: Is not meant to be here. Is very happy.",
                        "* It is the most intimate thing you have ever seen. You put down your drink. Greta, beside you, has gone quite white. She says: 'I can see them too. Mine used to say the date they'd die. I never wanted it. But look at this. Look what it's saying now.' She points at the barmaid. Her countdown reads, in tiny letters: Not for ages yet. Be kind.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("hotfix", "Hotfix").gap(16)
                .intro(
                        "* The hearing is held on the first of the month in the back room of the Lamb. Six chairs are set in a semicircle. A seventh, at the table, is for the Clerk. He has a large brown file and a small brass bell. He rings it. He announces that the matter of the Cheater flag is to be decided. He asks if there is anything further to say. There is a silence in which a clock can be heard.")
                .wait(4)
                .choice(new Chapters.Choice("hotfix", "Mister Patch has the authority to remove your flag, to leave it, or to fix everything but you.",
                        "Let him fix the others: take the Interface's errors out of the five, and leave yours.", "Ask him to leave all six as they are, errors and all.", "Ask him to remove yours, and keep the others, and take your chances without it.")
                        .after(
                                new String[]{"* He fixes the others. It takes a minute. A light goes round the room, a faint pale gold, and the five panels flicker and steady. Greta's countdown vanishes. Odo's shop menu folds away. The monk's Latin turns, for a moment, to English: it says, 'Thank you,' and then, 'Be careful,' and then goes out. Yours does not change. You are alone in the room with a flag that nobody else has. Patch looks at you, and bows his head.", "@Patch| It is the first fix I have ever made that I regretted."},
                                new String[]{"* He puts down the file. He looks at the room, and at the six panels, and at the little brass bell. He says: 'I am not at all sure that I am permitted.' Then he says: 'Very well.' He rings the bell, once, and says that the Department of Corrections has found no deviation. He stamps the form. The stamp is a very loud, final noise, and he flinches at it, as at a dropped plate.", "@Old Lady| That's the nicest bit of bureaucracy I've seen in ninety-four years."},
                                new String[]{"* You ask him to take yours. It is the hardest thing you have said. He stares. He says that nobody has ever asked. You say you would like to find out what you are without it. The room is silent. He raises his hand. A pale gold light goes round you like a hand turning a page. The flag goes out. The panel dims. You feel a little lighter, and a little emptier, and quite ready to carry the flour.", "@Nib| You look the same. You look exactly the same. I thought you'd look different."}))
                .outro(
                        "* The Clerk gathers up his file. He stands in the doorway of the Lamb for minutes, looking at the rain. He says that he has been in the Department for thirty-one years, and is beginning to suspect he has been in the wrong one.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("rollback", "Rollback").gap(16)
                .intro(
                        "* The offer comes from the panel itself, in a quiet voice, at four in the morning. It is not a notification. It is a small dim rectangle at the edge of your vision, with a single line of text. [ROLLBACK AVAILABLE. Restore to: Last Stable Version. Cost: One Life.] A cursor blinks after it. You know exactly what it means. It is the Rope. It is the second try. It offers to undo the whole of your life, back to a point before things went wrong.")
                .wait(4)
                .choice(new Chapters.Choice("rollback", "The panel offers to undo everything since the day it arrived.",
                        "Accept the rollback: go back, start again, and do it better.", "Decline: you would rather have the life you have, mistakes and all.", "Ask the panel what exactly it would restore, and who would remember.")
                        .after(
                                new String[]{"* You accept. The panel flashes. [Restoring...] A bar appears. It fills to eighty-eight per cent and stalls. You wait. Nothing happens. The bar does not move. After a long minute a line scrolls up. [Rollback failed: Host is no longer compatible with previous versions.] Then, gently: [You have been changed. I am sorry. I do not think it can be undone. I am not sure I would want to.]", "@Vesna| (in the doorway, in her nightgown, with a candle) Did you do something stupid?"},
                                new String[]{"* You decline. You say it with your lips, aloud, into the dark. You say that you have been here, and done these things, and been a fool, and would rather keep the lot. The panel is silent for ages. Then it says: [Understood.] And then, as if it had never been quite so tender: [Thank you for staying.] The rectangle fades, and the first light of morning comes in at the window.", "@Nib| (at breakfast) You look as if you've decided something."},
                                new String[]{"* You ask. The panel considers, with a dim flicker, for several minutes. At last it answers. [Restore: Everything. Remember: Nothing. The Host would begin again with no knowledge. The people they had loved would remember, and the Host would not.] You sit very still. You say: no. The panel says: [I thought so. I only wanted to be asked.]", "@Odo| What did it say? You've gone quite pale."}))
                .outro(
                        "* You sit on the edge of the bed in the dark until the light is grey. The village is waking. A cock crows, and a pail clanks, and somewhere Nib is singing, badly. You do not open the panel; you sit, and listen, and let the ordinary morning go through you like a long clear note.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("v2", "Version 2.0").gap(24)
                .intro(
                        "* The new version arrives on a Monday with no fanfare. The panel simply changes. It becomes smaller, and plainer, and softer, and loses its gold. The corners are less round. The font is gentle. It shows your name, and under it, quietly, the words: Host. Under that is a single line in small italics that you have never seen. [Version 2.0. Release notes: We tried to listen.]")
                .realm(2, "Level up: reach Major Lvl 2.")
                .outro(
                        "* The release notes run to six lines. [Fixed: The Host may now see other Hosts without a licence.] [Fixed: Tooltips now contain a reasonable amount of kindness.] [Removed: The word Special.] [Added: A button labelled Wave.] You press it. A small hand appears in the corner of your sight and waves. It is the funniest and most unexpected thing you have ever seen. Nib asks what you are laughing at. You tell him. He asks if he can try. You say he does not have a panel. He says he has a hand.",
                        "@Vesna| It's become a better thing. It's not telling you what to be. It's just saying hello.")
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(2, "Release Notes",
                "* The Support Group has moved from the back room of the Lamb to the front. It has sixty members now. Nobody is quite sure how many of them have panels. They have tea. They have biscuits. Greta has put up a banner: YOU ARE NOT ALONE. Under it, in smaller letters: (Bring a chair.)",
                "?report=1|* Your bug report hangs in a frame on the wall of the Department of Interfaces. It is the only one in the building. The Clerk has been seen standing in front of it, reading it, on his lunch break.",
                "?report=2|* The form for features has been used twice. The Department has ordered more.",
                "?report=3|* The question hangs unanswered on a nail in the corner of the corridor. People pass it and nod. Nobody takes it down.",
                "?hotfix=1|* The five panels are mended. Their owners say they feel as though a window has been opened. Yours is untouched. You do not mind.",
                "?hotfix=2|* All six panels are as they were. The Clerk checks on them on Tuesdays. He has stopped bringing the clipboard.",
                "?hotfix=3|* Your flag is gone. You carry the flour as you always did. People say you walk a little more upright.",
                "* Mister Patch has been seen on the bench outside the Palace of the Oldest Order at dusk, eating a sandwich and watching the sky. He is not wearing his hat. He has a look about him of a man who has put down a form for the first time in his life, and does not yet know what to do with his hands.");

        // ================================================================== SEASON 3
        d.season(3, "End User");



        d.chapter("notify", "One New Message").years(2)
                .intro(
                        "* The panel has been very quiet since Version 2.0. It sits in the corner of your sight, small and soft, and shows your name. Some days you forget it is there. Some days you catch yourself pressing Wave, for no reason, at a cat. Then on a grey Tuesday in October it chimes. It is a plain chime, rather shy. A small envelope icon blinks. [You have 1 new message. From: ADMIN.]")
                .wait(8)
                .outro(
                        "* You read it at the kitchen table. It is written in plain white text, with no formatting. It says: Hello. I am sorry to bother you. I have been meaning to write for minutes. I am not very good at it. I have never been asked for my address. If you are free, I should like to ask for your help, with something quite small. There is soup. Yours, with apologies, and a lot of hope. And a line below that: P.S. I am sorry about the font.",
                        "@Vesna| (reading over your shoulder, with a cup of coffee) Admin. Does it mean the Admin?",
                        "@You| I think it means the person who wrote the first line.",
                        "@Vesna| (after a pause) Well. I hope he's all right. It's a long time to write a letter.",
                        "* Mister Whiskers is on the windowsill with his keys. He opens one eye. He says, in a tone of kindly resignation, that he supposes it was bound to happen. He says it with the air of a person who has been expecting a visitor for nine hundred years, and has put out the best china.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("admin", "Admin").gap(16)
                .intro(
                        "* The brown cart is in the yard of the inn at dawn. The Wick on the box is thin and nervous in a thin coat. He says that the message is from a person in the kitchen of the Palace. He says that he was told to say please. He says he has been saying it to himself all the way from Originis, and is no longer sure he can say it as a word.")
                .at("palace", "Go to the Palace of the Oldest Order, at Originis, and down to the Under-Kitchen.")
                .outro(
                        "* The Under-Kitchen is as warm as a loaf. A man in a plain apron stands by the long table with a spoon in his hand. He has put out two bowls. He turns, and sees you, and his face does a great many things at once. He says: 'Oh. You came.' He says: 'I wrote the letter. I did not think you would.'",
                        "@???| I am the Admin. That is the name on the door. I do not know that it is a good name. I have been called a great many things. I was left to run the school, and the sky, and the soup, and I find that I am not equipped to run any of them. The window in your eye is mine. It was an accident. I would like to apologise for the font.",
                        "@You| It's a nice font.",
                        "@???| (visibly moved) It is Garamond. It was the only one I knew.",
                        "* He takes you to a small side room off the kitchen, a cupboard, really, with a stool and a lamp. On the stool is a glass slab the size of a plate. On the slab is a faint blue light that moves. He says it is the terminal. He says it is how he watches the Garden. He says it is the only way he knows to be with them.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("terminal", "The Terminal").gap(8)
                .intro(
                        "* You sit on the stool. ??? stands behind you, with his hands in his apron. The slab is warm. It is full of light, a vast dim river of tiny rows, scrolling, one after another, like rain on a window. Each row is a name. Beside each name is a number. Beside each number is a word. You watch the names go by: Hannah. Wilfred. Ismay. Brother Cael. Pip. Nib. Vesna. Yours.")
                .wait(4)
                .outro(
                        "@???| That is everybody. That is all of them. A hundred thousand and some. It is the only way I know to see them. I was not made to see them any other way. I was made to keep a list. I have kept it for a very long time. I do not like what it has done to me.",
                        "@You| What has it done?",
                        "@???| It has made me think that a number is a kind of knowing. I have lived with their numbers for longer than they have lived. I know exactly how strong each of them is. I know their potential. I know how long they have left. And I cannot tell you a single thing about what they like to eat.",
                        "* He is crying a little. He does not seem to notice. He says that he has been trying to unlearn it. He says he looked at the Wave button, when it was written, and thought it was the best idea he had ever had. He says that he wants to show every one of them that he is looking. He says that he does not know how.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("online", "All Players Online").gap(16)
                .intro(
                        "* He shows you. It is not hard. There is a button in the corner of the terminal that says Show. You press it, once, with a trembling finger. And the whole of the Garden lights up. For a single second, every person in it, at the same time, can see a small bright name floating over the head of every other person. The village in the morning. The market in the noon. The Palace at dusk. They all stop. They all look up.")
                .wait(8)
                .outro(
                        "* It lasts a second. It is the longest second of your life. In that second you see all of them looking at each other, and you see their faces, and they are not afraid. A woman at a well drops her bucket. A man on a roof stops hammering. A pair of children playing in the road look at each other, and one says: 'Oh. You're Tilly.' The other says: 'You're Jem. I thought you were Jem.'",
                        "* Then it goes. The names fade. The streets go on as before. But all over the Garden, in every village and every street, you can see people standing with their heads a little on one side, as if they had heard something just beyond hearing. After a minute they go back to their business. Some of them are smiling. Some are wiping their eyes.",
                        "@???| (very faint, behind you) I did not know it would be like that. I thought they would be frightened.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("quests", "The Quest Log of the World").gap(16)
                .setup(TaleCheaterSeasons::mark)
                .intro(
                        "* There is another tab on the terminal. It is greyed out. You press it anyway. It opens with a creak, like a stiff door, and shows a list. It is the Garden's Quest Log. It is long. It begins with small things: Feed the Hungry (ongoing). Heal the Sick (ongoing). Defend the Border (ongoing). It ends with one entry that is not like the others. It is in red. It says: [Put Out: 0/1.]")
                .friends(6, "Gather your own party: make six friends.")
                .outro(
                        "@???| That is the Court's. It is the only quest in the Log that does not belong to me. It has been there since the beginning. I cannot delete it. I have tried. I have been very patient. I think it is a kind of promise someone made to someone, a very long time ago, and nobody can remember who. It is not a bad quest. It is a tired one.",
                        "* You look at the zero. It is a very small zero. You look at the slash. You look at the one. You think of Mister Patch, in his charcoal suit, on a bench with his sandwich. You think of a Guest with a pot of tea, and a Captain with a spade, and a man with scissors on a silver chain. You say: 'What happens when it fills?'",
                        "@???| (after a very long time) I do not know. I have never allowed myself to look at the end of the list.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("keys", "Mister Whiskers' Keys").gap(16)
                .intro(
                        "* Mister Whiskers has been waiting nine hundred years for someone to ask. When you do, he does not answer at once. He sits on the windowsill of the inn in the last of the sun and washes behind his ear, with great deliberation, with the keys making a small bright chinking at his neck. Then he says that he was the Gardener's cat.")
                .wait(8)
                .outro(
                        "@Mister Whiskers| (in your head, in the voice of a retired colonel) He had a dog once, and a cat. The dog went with the army. The cat stayed to keep the keys. There are nine. Each opens one of the places. You know the ones. The Wayhouse, the Wall, the Cistern, the Hall, the Shed, the Sky, the Road, the Menagerie and the Garrison. He locked them all, at the end, so no one would come to harm.",
                        "@You| And the Door?",
                        "@Mister Whiskers| The Door has no key. It never did. It has a latch on the inside, and a very patient hand on the other side to hold it open. He told me to keep the keys in case anyone came who could use them. I rather doubted it. I have been mistaken before.",
                        "* He jumps down from the sill. He walks across the floor with a measured gait, and sits at your feet, and bows his head. The ribbon slides over his ears. The nine keys fall into your lap, bright and heavy and warm. He says: 'I should like to retire. I have had a very long watch.'")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("patch3", "Mister Patch Returns").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "patch"))
                .intro(
                        "* He is on the step of the inn when you come out. He has lost the clipboard. He wears the charcoal suit, but the tie is crooked, and there is a stain on the cuff. He looks as a man looks who has had a long hard night and has come to the conclusion that he has not enjoyed it. He lifts his hat. He asks if he might have a word.")
                .meet("patch", "Hear what Mister Patch has to say.")
                .outro(
                        "@Patch| I have been reassigned. It is, I am told, a promotion. I have been made Clerk to the Uninstalling Committee. They are a body of the Court. They have a single function, which is to remove the Interface. They have been trying for nine hundred years. I am to deliver the form.",
                        "* He takes it out of his breast pocket. It is a single page, cream-coloured, with a gold border. It says at the top, in a clear black type: FORM 1. REQUEST FOR UNINSTALLATION. It has one box, and one line. The box is for a tick. The line is for a name. He puts it in your hand and does not let go.",
                        "@Patch| I should like to say that I do not want you to sign it. I have never said that to a subject. It is quite irregular. But I find I have been in the Department a long time, and I have seen what the Interface does, and I have come to think it was the first kind thing anybody wrote in this place.",
                        "* He releases the form. His hand is shaking. He says that the Committee will want an answer by the end of the month. He puts his hat on, and straightens his tie, and says: 'I have prepared nothing. I have no argument. I only wanted to say it to someone.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("coop", "Co-op").gap(16)
                .setup(TaleCheaterSeasons::mark)
                .intro(
                        "* The six of you meet in the back room of the Lamb. You lay out the panels on the brown paper on the wall, with the tea. There is Greta, with her fixed countdown. There is Odo with his shop. There is the monk, and the girl with the weather, and the old lady in her lace cap, and yourself. You hold the form in the middle of the table. Nobody speaks.")
                .made(10, "Build the Co-op: make ten things.")
                .outro(
                        "* It takes a week. It is not a battle. It is a piece of joinery. You work with Odo and the monk on the wall of the Lamb, with chalk and ink and a plumb line, drawing out the six panels as a single design. The weather in the corner. The shop beside it. The countdown in the middle, no longer counting. The monk's Latin round the edge, in a fine gilt border. At the centre, in a space the others have left, is a blank square.",
                        "@Old Lady| That's yours. We've all agreed. It's where the last bit goes.",
                        "@You| What is the last bit?",
                        "@Old Lady| (with a twinkle) My dear, we haven't the faintest idea. That's rather the point.",
                        "* The design takes shape. It is a single great panel, six in one, bright and strange, with room in the middle. When it is finished you stand back and look. It is, you think, the first thing you have ever made that was intended to be looked at by people other than yourself.")
                .pay(400, 0, 0, 1, 0, 1);

        d.chapter("meeting", "The Final Boss Is a Meeting").gap(16)
                .intro(
                        "* The Uninstalling Committee meets in a long pale room in the Palace, at the end of a corridor that smells of beeswax. It has a table of grey wood, with eleven chairs. In each chair sits a Hand of the Court. There is a Guest with a teapot, a Snuffer with scissors, a Captain with a spade, a Clerk with a stamp, and seven others in plain suits. They look up when you come in. They are not hostile. They are, every one of them, extremely tired.")
                .wait(4)
                .choice(new Chapters.Choice("meeting", "Eleven Hands of the Court sit round a table, waiting for an answer about the Interface.",
                        "Attend in person, with your own panel, and speak.", "Send Mister Whiskers, with the nine keys, to speak for the Gardener.", "Send the minutes of the Support Group, all sixty pages, and let them read.")
                        .after(
                                new String[]{"* You attend. You sit in the twelfth chair, with the panel open in the corner of your eye. You speak for six minutes. It is not a good speech. It has a great many ums in it. You say that the panel is a hello, and that a hello is a small thing, and that the Court's quarrel is with the Garden and not with a greeting. You sit. The Guest pours a cup of tea. The Snuffer looks at his scissors. For a minute nobody says anything.", "@Guest| (softly) I must say, that was rather good."},
                                new String[]{"* Mister Whiskers walks in. He walks the length of the table with his tail up and the keys chinking at his neck. He leaps onto the middle of it. He sits, among the cups, and looks along the row of faces. He says in a voice that fills the room: 'I am the Keeper of the Keys. I speak for the one who is not here. He says: please.' Eleven pairs of eyes follow him. The Snuffer's scissors fall quietly to the floor.", "@Captain| (low) I haven't seen that cat in nine hundred years."},
                                new String[]{"* You send the minutes. It is sixty pages, in forty different hands, with a stain of tea on page eleven. The Clerk reads them aloud to the Committee, one after another, in a flat voice, for four hours. By the third hour the Guest has stopped pouring. By the fourth the Captain has taken off his cap. When the last page is read there is a long silence. Then the Clerk lays down his stamp and says: 'I move that the matter be left on the table.'", "@Patch| (in a small voice) Seconded."}))
                .outro(
                        "* The vote is taken by hands. It is not quite unanimous. Of the eleven, four raise their hands to remove, and seven do not. The Chair, a thin old Guest in a grey frock coat, declares the motion not carried. She says it with a certain relief. She says it is the first time in nine hundred years the Committee has not done what it came to do. Mister Patch puts his head in his hands, quite openly, on the table.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("tree", "Skill Tree").gap(16)
                .intro(
                        "* The final tab opens on its own, on a still morning in spring. It is the Skill Tree. It has been empty for years. It is full now. Branches have grown on it, of their own accord, in the night, and each one carries a small bright leaf with a name: Wave. Listen. Ask First. Say Thank You. Keep a Secret. Stay. Each leaf is a skill the panel has learned, from you, from the Support Group, from the barmaid, from the cat.")
                .wait(8)
                .choice(new Chapters.Choice("panel", "The panel is yours, and so is the choice about what it becomes.",
                        "Keep it: the panel stays small and quiet, at the corner of your eye, as it is.", "Delete it: let it go, with thanks, and see the world without a window.", "Give it away: make a copy for every person in the Garden, with the Wave button and nothing else.")
                        .after(
                                new String[]{"* You keep it. It sits where it has always sat, small and soft, showing your name. It no longer pings. It no longer ranks. It has become a kind of friend, at the edge of your sight. Some days you forget it. Some days you press Wave, and a little hand waves back. You feel that you have been very lucky in your company.", "@Vesna| (in the evenings, at the table, to the air) Say hello to it for me."},
                                new String[]{"* You delete it. You do it at dawn, on the hill, with the whole of the Support Group watching. You say thank you aloud. The panel fades, slowly, from the edges, until only a single dot is left, which blinks twice, in the manner of a small animal looking back from a hedge. Then it is gone. The sky is very wide. You stand there for ages with your hands open, feeling the wind.", "@Nib| Do you feel different?"},
                                new String[]{"* You copy it. It takes the Support Group a month. You make a hundred thousand copies, one for each name, each with a single button. It is a Wave. That is all. The next day, all over the Garden, people stop in the street and look at the corner of their eye, and press it. A hundred thousand small hands wave. A hundred thousand small hands wave back.", "@???| (very faint, from a great distance) I had not thought of that. I had not thought that it could be given."}))
                .outro(
                        "* On the branch of the Skill Tree, at the very top, there is a single bud that has not yet opened. It does not have a name. It sits quite still, like a seed in a fist. A line of text under it says: [Unlocked when ready.] The panel does not say what it is. It does not say when. You have the unmistakable impression that it is smiling.")
                .pay(600, 0, 0, 2, 0, 2);

        d.chapter("credits", "Credits").gap(24)
                .intro(
                        "* They roll on a fine evening in June, in the sky over the village, in white letters on dark blue. Nobody has asked for them. They begin quietly, one name at a time. [Starring: You.] [With: Nib Fennimore as Himself.] [Vesna Hartley as the Alchemist.] [Mister Whiskers as Mister Whiskers.] The villagers stand in the road, in a row, with their heads back, reading. Children read out the names, slowly, one syllable at a time.")
                .wait(8)
                .outro(
                        "* The credits go on for twenty minutes. They list everyone. They list the Rat King, and the Rat King's court. They list the tax-collector, the priest, the barmaid, the monk, the old lady, the girl with the weather. They list a Clerk named Patch. They list the Admin, in a smaller font, at the bottom, with the words: And a great many thanks to those who were kind to the font. There is a last line, in white. [No Hosts were harmed in the making of this Garden.]",
                        "@Nib| (staring up, with his hands in his pockets, awed) That's the best thing I've ever read. That's got my name in it. I've never been in a book.",
                        "@Vesna| (beside him, drily) It's not a book. It's a sky. Don't get above yourself.",
                        "* You stand with the whole of the village in the road, under the long slow letters, and feel what you would call, if the panel were to ask, a perfectly ordinary happiness.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("ngplus", "New Game Plus").gap(24)
                .intro(
                        "* The last message arrives in the first week of autumn. It is a plain white envelope in the corner of your sight. It says: [You have 1 new message. From: ADMIN.] You open it. It says: Hello. I wanted to tell you that I found a way to unlearn the numbers. It is very simple. I eat my soup and I look at the people. I do not know what it will do to the Garden. I think it will be all right. Thank you for the font. Yours, with a great deal of hope.")
                .wait(8)
                .outro(
                        "* You read it standing at the window of the inn in the early light. Outside the village is waking. A cock crows. A cart rolls by. Nib calls from the street that the soup is ready. The panel in the corner of your eye is quite still. Then it moves. A very small line of text appears, in plain white, without any chime. [NEW GAME PLUS?] it says.",
                        "* There are two buttons. Yes. No. You look at them for minutes. You look out of the window at the village, and the street, and the grey hill beyond it, with its two small green trees. You think of the Rat King and the Chosen One and the cat. You smile, and you reach out with your mind, and the panel waits with enormous patience.",
                        "@You| Not yet.",
                        "* The panel flickers. It is a soft, glad, silly flicker, like a cat at a door. It dims. The buttons fade. [Understood.] it says. And, in a smaller font, as an afterthought: [Take your time.]")
                .pay(2000, 3, 0, 4, 2, 2);

        d.epilogue(
                "* A blue rectangle sits at the corner of your eye. It says your name. It does not say anything else.",
                "?panel=1|* The panel stays. On quiet evenings it waves at the cat, who has taken to waving back.",
                "?panel=2|* The panel is gone. You have never been so sure of your own strength. You carry the flour as you always did.",
                "?panel=3|* A hundred thousand people have a small hand in the corner of their sight. They press it at dusk. All over the Garden, in the half-light, the villages wave to each other.",
                "?meeting=1|* You attended the meeting in person. A thin old Guest sends a card every winter. It has a cup of tea on it, and a very small tick.",
                "?meeting=2|* A cat with nine keys sits at the head of the Committee's table on the first of the month. Nobody has asked him to leave.",
                "?meeting=3|* The sixty pages of the Support Group's minutes are bound in leather in the Court's library. They have been borrowed more often than any other volume.",
                "?class=1|* The sword over the fireplace is a very good sword. A great many people have stopped asking you to save them.",
                "?class=2|* The picks are used for opening jars. Odo has begun to value them.",
                "?class=3|* Your class is Human (Level 1). It is the only one that can never be nerfed.",
                "?love=1|* Vesna Hartley says yes to the tag, to the date, to the plain gold ring, and to the sofa, in that order.",
                "?love=2|* Vesna Hartley uses the green-barrelled pen to write prescriptions and, on Thursdays, notes.",
                "?love=3|* Vesna Hartley likes a long slow build, and says so, and is generally right.",
                "* At night, when the village is asleep, the panel makes a small noise, like a very polite cough. It says: [Chapter Four loading.] And then, in a smaller font: [No rush.] The bar fills to eighty-eight per cent, and sticks, and stays there, quite contentedly, under the stars.");

        // @@END
    }
}
