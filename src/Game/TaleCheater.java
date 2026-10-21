package Game;

/** Status Window (Loading) (2026-10-20; new start, three seasons; a parody of the System novels the author loves). A blue panel appears at the edge of the human's sight and says they are special. It is wrong about nearly everything
 *  else. Season 1, "Level One Human" (comic, card A): absurd daily quests, stats that do not add up, a faulty skill, the alchemist's apprentice Vesna Hartley (the other gender than the human's), a rival Chosen One with a better
 *  panel, patch notes, a cat that is a boss. Choices: class (what the Hidden Class is), love. Flags: class, love. Seasons 2 and 3 are in TaleCheaterSeasons. */
final class TaleCheater {
    private TaleCheater() { }

    static void register() {
        Chapters.Def d = Chapters.def("cheater");
        d.season(1, "Level One Human");
        d.flags("class", "love");
        d.place("village", "wildnear").place("cellar", "wild").place("inn", "far");
        d.cast("nib", "Nib", "Fennimore").he().keep().trait(Character.Traits.Friendly).side("Good").lvl(2).age(24).at("place:village").rel(3);
        d.cast("vesna", "Vesna", "Hartley").opposite("Vesper").keep().trait(Character.Traits.Alchemist).side("Good").lvl(4).age(26).at("place:village").rel(1);
        d.cast("aldwin", "Sir", "Aldwin").he().keep().trait(Character.Traits.Leader).side("Good").lvl(9).pot(8).age(24).at("far").rel(0);

        d.chapter("loading", "Loading")
                .intro(
                        "* You are shelling peas on the back step of your mother's house when the world goes blue at the edges. A rectangle appears in the air in front of your face. It is semi-transparent. It has rounded corners and a drop shadow. It says, in a friendly sans-serif font: [Status Window (Loading...)]. A small bar underneath fills slowly from left to right, and stalls at eighty-eight per cent.",
                        "* You put down the peas. You look behind you. Nobody is there. You look in front of you, and the rectangle is still there, and you have the very clear sense that it has arrived, in the manner of an unexpected relative.")
                .at("village", "Go to the village, {@village}, and find out whether anyone else can see it.")
                .outro(
                        "* The bar reaches a hundred per cent. A chime sounds, inside your head, a bright little ping like a tea-spoon on a glass. The panel clears and shows a column of text. [NAME: ______] [CLASS: Human (Level One)] [HP: 11/11] [MP: --] [TRAIT: Special]. The word Special is in gold. It has a small animated sparkle. It is the most self-satisfied sparkle you have ever seen.",
                        "* A line of text scrolls up the bottom of the panel in bold. [Welcome, Host! You have been selected. You are one of a kind. Please enjoy your stay.] Under it, in smaller letters, the way an apology is typed: [Terms and conditions apply.]",
                        "@Nib| (the innkeeper's lad, two doors down, watching you stare at nothing) You all right? You've been looking at that bit of air for a good while.",
                        "@You| Can you see anything?",
                        "@Nib| Air. A bit of dust. Is it a ghost? You can tell me. I'm very good with ghosts.")
                .pay(100, 0, 1, 0, 0, 0);

        d.chapter("daily", "Daily Quest: Eat Breakfast").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "nib"))
                .intro(
                        "* The first quest arrives at seven in the morning, with a fanfare. [QUEST RECEIVED: Daily Routine (0/1). Objective: Eat breakfast. Reward: +1 Vitality (Hidden). Penalty for failure: Mild Hunger.] You read it twice. You are, at the time, holding a piece of toast. You eat it. A trumpet sounds. [QUEST COMPLETE! +1 Vitality (Hidden). You feel a little more alive. Not noticeably.]")
                .friends(2, "Make friends with the neighbours: make two friends.")
                .outro(
                        "* They come every morning after that. [Daily Quest: Brush Teeth (0/1).] [Daily Quest: Say Good Morning to a Stranger (0/3).] [Daily Quest: Do Not Step on a Crack (0/1). Warning: This quest is a trap.] You do them all, at first, in a spirit of cautious obedience. Then you begin to suspect that the panel is not so much guiding you as keeping you busy.",
                        "@Nib| So the thing says to say good morning three times, and then you get a thing?",
                        "@You| A hidden stat.",
                        "@Nib| What does it do?",
                        "@You| It's hidden.",
                        "@Nib| (after due consideration) That's the best thing I've ever heard. Can I have one?",
                        "* The panel pings. [HOST HAS ACQUIRED AN ALLY: Nib Fennimore (Level 2). Relationship: Loyal. Special Ability: Believes You.] Under it, in italics: [This is a rare trait. Please handle with care.] Nib reads it over your shoulder, which he cannot do, and goes pink to the ears.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("stats", "Stats").gap(16)
                .intro(
                        "* The Stats page unlocks on a Thursday. It arrives with a cheerful riff and the words [Congratulations! You have unlocked: Character Sheet!] in a spinning gold font. You open it with a thought. It lists STRENGTH, DEXTERITY, INTELLIGENCE, VITALITY, LUCK and CHARISMA, each with a number. The numbers are: 4, 5, 3, 6, 98 and -2.")
                .wait(8)
                .outro(
                        "* You study the page for an afternoon. You sit on the well with a mug of tea and move your eyes about, trying to find an Accept button. There is not one. There is a small gear icon in the corner that opens a menu called Settings, which contains only one option: Font Size. You make it bigger. The panel makes it smaller again, with a faint air of reproach.",
                        "@Nib| What's Luck?",
                        "@You| It says ninety-eight.",
                        "@Nib| That's very good. I think. Is it very good?",
                        "@You| I think so. I've been struck by lightning twice this month.",
                        "@Nib| That's not luck.",
                        "@You| It says it is. [LUCK: 98 (Excellent). Hint: High Luck increases the frequency of Fortunate Events.] I've also dropped a cart on my foot, fallen in a ditch, and lost my hat in a river. Five fortunate events in a month.",
                        "* The panel flashes. [Fortunate Events: 5. Misfortunate Events: Not Applicable. Please contact Support if this seems wrong.] There is a link at the bottom. It is greyed out.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("skill", "Skill Acquired: Lie Detection (Faulty)").gap(16)
                .intro(
                        "* The skill comes in the middle of a conversation with the tax-collector. He is explaining, with great warmth, that the new levy is entirely for your benefit. The panel pings in the corner of your eye. [SKILL ACQUIRED: Lie Detection (Level 1). Description: Detects statements that are untrue. Success rate: 50%. Hint: Do not rely on this.] You glance at the tax-collector. A small red icon shows above his head. [LIE.]")
                .wait(8)
                .outro(
                        "* It works on everyone. It works on the baker, who says her bread is fresh. It works on the priest, who says he is at peace. It works on your mother, who says she is not upset. It works, in a rather ghastly series of encounters over the next week, on every person you love, and in each case the red icon shows up with the sound of a small buzzer.",
                        "* On the Sunday you realise it is also working on you. You say 'I'm fine' to Nib, who has asked how you are, and a red [LIE] flashes above your own head in the reflection of the well. You say, 'I'm fine,' again, with more conviction. The icon flickers. It turns yellow. It says: [LIE (Probably).]",
                        "@Nib| Is it doing something?",
                        "@You| It says I'm not fine.",
                        "@Nib| Oh. Well, I could've told you that. I just didn't have a skill.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("vesna", "A Possible Love Interest").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "vesna"))
                .intro(
                        "* The alchemist's shop on the hill has a new apprentice. The panel notices before you do. [NEW CHARACTER DETECTED: Vesna Hartley (Level 4). Occupation: Alchemist's Apprentice. Disposition: Neutral. Special Tag: POSSIBLE LOVE INTEREST (Unconfirmed).] You stare at the tag. It pulses gently. It sparkles. It is, you feel, the most embarrassing thing that has ever been done to you in a public place.")
                .meet("vesna", "Visit the alchemist's apprentice on the hill.")
                .outro(
                        "* {vesna.He} is behind the counter in a stained apron, grinding something green in a mortar, with a pencil in {vesna.his} hair and a look of wary, humorous intelligence. {vesna.He} looks up. {vesna.He} sees you looking at the air above {vesna.his} head. {vesna.He} looks at it too, with real interest, and says: 'Is there something there?'",
                        "@You| No.",
                        "@Vesna| You're lying. You went a very odd colour.",
                        "* The panel says: [LIE.] You close it with a thought. {vesna.He} puts down the pestle and leans on the counter. {vesna.He} says that {vesna.he} has been in the village for three weeks and has already heard about the person who stares at things. {vesna.He} says that in {vesna.his} experience the people who see what nobody else sees are either liars or poets, and that {vesna.he} cannot decide which is worse.",
                        "@Vesna| I'd like to buy you a drink. Not for the reason in your tag. I'm curious. Whatever you can see, I would like to know what it says about me.",
                        "* The panel flashes. [Warning: Do not reveal the Tag. The Tag is private. Revealing the Tag may lower Relationship by up to 40.] Underneath, in small grey type, as an afterthought: [Good luck.]")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("rat", "The Boss Encounter").gap(16)
                .intro(
                        "* The cellar of the inn has been invaded. The landlord hires you to clear it. There is a pay of ten silver and a pie. You go down at dusk with a candle, a broom and Nib, who has brought a saucepan. The panel is silent on the stairs. At the bottom, as the candle lights the dark, there is a chime and a cry of trumpets. [BOSS ENCOUNTER! The RAT KING (Level 3). HP: 40. Weakness: Cheese.]")
                .wait(8)
                .outro(
                        "* He is the size of a large dog. He wears a crown made of a bottle cap. He is surrounded by his court, an assembly of twenty-odd rats, who stand on their hind legs in a respectful ring. The King regards you from a barrel with small shrewd red eyes and the faintly pained expression of a monarch whose subjects have been kept waiting.",
                        "* The panel is jubilant. [BATTLE START! Select a command: ATTACK | DEFEND | ITEM | RUN.] You select nothing. You are a person with a broom. The Rat King squeaks, and his army advances, and the next ten minutes are a confusion of brooms and saucepans and shrieking in the dark.",
                        "* It ends when Nib throws a wheel of cheese. It is a good throw, and it lands on the barrel, and the Rat King takes it in both hands and sits down. The court sits with him. They eat. The panel is baffled. [BOSS DEFEATED?] it says. [Experience gained: ???.] Then: [Error: Experience source not recognised.]",
                        "@Nib| I don't think we won.",
                        "@You| I think we made a treaty.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("rival", "The Chosen One").gap(16)
                .setup((c, st) -> Chapters.spawn(c, st, "aldwin"))
                .intro(
                        "* The Chosen One arrives in the village on the market day with a horse and a sword and a sunbeam that follows him about. He is tall, golden-haired, square-jawed and twenty-four. He is called Sir Aldwin of the Bright Lance. People turn to look. Small children follow him in a line. A dog sits up and begs. The panel, in the corner of your eye, pings in alarm. [WARNING: PROTAGONIST DETECTED. YOUR STATUS HAS BEEN DOWNGRADED TO: SUPPORTING CHARACTER.]")
                .meet("aldwin", "Meet the stranger on the market square.")
                .outro(
                        "@Aldwin| Good day. I am told you are the one who sees things. I have a quest. It has been given to me by a voice in the sky. I am to find the nine keys and open the Door. I wonder whether you might assist.",
                        "* He holds up his hand. In the air above it, clear and brilliant, hangs a panel like yours. It is bigger. It has gold edges. Its font is a graceful serif. It lists his STRENGTH as 87, his DEXTERITY as 91, his LUCK as 100, and his CHARISMA as an upward-pointing arrow. Under his name there is a small icon of a crown. Under yours there is a small icon of a potato.",
                        "@You| I see you've got a better one.",
                        "@Aldwin| Do I? I never compare. I'm told it is rude. Yours is perfectly adequate, I'm sure.",
                        "* The panel flashes. [NOTE: THIS PERSON IS NOT A THREAT. THIS PERSON IS A PROBLEM.] You have the odd sensation that the panel is, for the first time, on your side. It adds, in a smaller font: [Please do not tell him this.]")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("patch", "Patch Notes").gap(16)
                .intro(
                        "* You wake one morning to find that everything has moved. The panel is a different colour. The font has changed. A fanfare sounds and a banner scrolls across your eyes, in letters of gold. [PATCH 1.0.2 HAS BEEN APPLIED! Please read the notes below.] The notes are eleven pages long. You read them at breakfast. They contain no information whatsoever.")
                .wait(8)
                .outro(
                        "* [Fixed: A bug whereby the Host could not see the Host]. [Fixed: A bug where the colour blue was not blue enough]. [Added: Daily Quest: Be Kind to a Cat]. [Changed: The meaning of the word Special]. [Known issues: The Host may experience occasional feelings] You read it again, and a third time. At the end there is a line in a different font, which looks, you think, a little like handwriting. [We are sorry for the inconvenience. We are doing our best.]",
                        "@Nib| Does it say sorry?",
                        "@You| It says it's doing its best.",
                        "@Nib| (with great sympathy) Poor thing. It sounds like it's had a hard week.",
                        "* The panel is silent for minutes. Then a small line appears, in the corner, for no reason, like a hand held up from the back of a room. [Thank you.]")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("cat", "Side Quest: The Lost Cat").gap(16)
                .intro(
                        "* The old woman at the end of the lane has lost her cat. The panel announces it. [SIDE QUEST: The Lost Cat. Objective: Find Mister Whiskers. Reward: ???. Difficulty: Easy. Hint: He is not what he seems.] The old woman is called Mrs. Pargeter. She has a house full of clocks and a voice like a creaking gate. She is sure he has gone to the churchyard. He goes to the churchyard on the first of every month.")
                .wait(8)
                .outro(
                        "* You find him on a tombstone at midnight, a large black cat with a white bib and a ring of keys on a ribbon round his neck. He looks at you without hurry. The panel goes off like a firework. [WARNING! HIDDEN BOSS! MISTER WHISKERS (Level 99). HP: ???. Defeat him to unlock: THE TRUE ENDING.] A chime, and then, anxiously: [Please do not attack him.]",
                        "* He yawns. He stretches. He walks across the grass and leans against your shin, and begins to purr, in a note so low that the gravestones hum. The keys on his ribbon jingle. There are nine of them. They are made of old brass, and each is cut with a different pattern.",
                        "@Mister Whiskers| (in your head, mildly, with the voice of a retired colonel) Don't be alarmed. I was only passing through. I was told someone here could see me. I hadn't hoped for it. Is there any chance of a sardine?",
                        "* You give him your supper, which is a pasty. He eats half. He says it will do. He says that he has been looking for the Host for nine hundred years, and has had a great deal of trouble getting an appointment. He pads away across the graves with his tail up, and the panel, after a long baffled silence, says only: [Side quest complete?]")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("tutorial", "The Tutorial Ends").gap(24)
                .intro(
                        "* On the morning of the first of May the panel plays a long stirring chord and displays, in letters three feet high, a banner. [CONGRATULATIONS! YOU HAVE COMPLETED THE TUTORIAL! Please wait while the real game loads.] There is a spinning wheel. It spins for ages. You make your breakfast and eat it and wash up, and the wheel is still spinning.")
                .realm(1, "Finish the tutorial: reach Major Lvl 1.")
                .outro(
                        "* The wheel stops at noon. The panel says: [LOADING COMPLETE. Welcome to the Real Game!] There is a fanfare. A great many new tabs open. There is a Map, an Inventory, a Quest Log, a Skill Tree and a Friends List. Each one is empty. The Friends List has three names in it: Nib, Vesna and, with a small crown, Mister Whiskers. Under the names a line in grey says: [More friends coming soon.]",
                        "@Vesna| (looking over your shoulder, in the shop, with a tray of vials) Is it showing me again?",
                        "@You| It's showing you as a friend.",
                        "@Vesna| Not as a tag?",
                        "@You| The tag is gone.",
                        "@Vesna| Was it a good tag?",
                        "@You| It was a deeply embarrassing tag.",
                        "@Vesna| Then I'll be having that drink after all.")
                .pay(500, 0, 0, 1, 1, 1);

        d.chapter("class", "Hidden Class Unlocked").gap(24)
                .intro(
                        "* It happens at the top of the stairs in the inn. You trip. The tray you are carrying falls. It breaks seven plates and a jug, and the panel goes off like a siege. [HIDDEN CLASS UNLOCKED!] A golden light fills the corridor. [Please choose your destiny.] There are three options, laid out in three elegant frames like cards in a game.")
                .wait(4)
                .choice(new Chapters.Choice("class", "A golden panel offers a Hidden Class. The frames are labelled: HERO, ROGUE and a third whose label is a small blue question mark.",
                        "HERO: sword, shield, and a prophecy. A very large number of people will depend on you.", "ROGUE: lock-picks, shadows and good teeth. Nobody will depend on you at all.", "?: the third option, whose description says only: Trust me.")
                        .after(
                                new String[]{"* You choose Hero. A sword appears on your hip. It is rather a good sword. The panel plays a rousing march. [CLASS: HERO (Level 1). New skill: Rally. New duty: Save the World. Please note that duty cannot be unequipped.] Nib applauds. Vesna looks rather alarmed. You feel, at once, the faint, cold, gathering weight of other people's hopes, and realise it is much heavier than the sword.", "@Vesna| I liked you better when you stared at the wall."},
                                new String[]{"* You choose Rogue. A set of picks and a dark cloak appear. The panel plays a quiet jaunty tune. [CLASS: ROGUE (Level 1). New skill: Light Fingers. New duty: None.] You test the picks on the inn door and it opens before you touch it. Nib is delighted. Vesna remarks that she has never seen anybody less suited to the profession.", "@Vesna| You've got an honest face. It's frankly a handicap."},
                                new String[]{"* You choose the question mark. A small silence. Then the panel displays, in plain text, without a sound, the word [Thanks]. A new class appears in the list, in a font you have not seen. [CLASS: HUMAN (Level 1). New skill: None. New duty: None. Description: You are exactly who you were. This is the hidden class. It is the only one that can never be nerfed.] Nib says that it looks like the same one you started with. You say that it is.", "@Vesna| I think that's the first honest thing the panel's ever said."}))
                .outro(
                        "* The golden light dies. The corridor is dark. The jug is still in pieces on the boards. In the silence, the panel makes a faint, thoughtful noise, like a throat being cleared in a very small room. It says nothing else for the rest of the day.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("endchapter", "End of Chapter One").gap(24)
                .intro(
                        "* The panel sends a notification at the end of the month. [CHAPTER ONE COMPLETE! Please rate your experience.] Beneath it are five stars. You look at them. You think about the Rat King, and the cat, and the cellar, and the possible love interest. You look for ages. At last you tap the fifth, with your mind, and a small cartoon heart floats up and bursts.")
                .wait(8)
                .choice(new Chapters.Choice("love", "Vesna Hartley is leaning on the counter of the alchemist's shop, waiting for an answer to a question neither of you has asked.",
                        "Ask {vesna.him} out, plainly, without the tag.", "Give {vesna.him} a small gift, a good pen, with no explanation.", "Do nothing. Be friends. Let the game load.")
                        .after(
                                new String[]{"* You ask. It takes forty seconds and comes out in the wrong order. {vesna.He} stares at you. The panel, in the corner, is utterly still. Then {vesna.he} says, 'Yes,' in a flat clear voice, and picks up the mortar again, and you realise {vesna.he} has been waiting for three months to be asked. The panel rings softly. [ACHIEVEMENT UNLOCKED: Asking.] It is the only one you have ever been proud of.", "@Vesna| I'd have said yes to the tag. I want you to know that."},
                                new String[]{"* You leave the pen on the counter, wrapped in brown paper, without a word. It is a good pen, steel-nibbed, with a green barrel. {vesna.He} finds it in the morning. {vesna.He} uses it for a week without comment. At the end of the week there is a note on your doorstep, in a spiky hand, on the back of a prescription. It says: Thursday. I'm told there's a dull play.", "@Nib| I didn't tell her. I want that clear. She worked it out."},
                                new String[]{"* You do nothing. It is a small, awkward, friendly silence, over a counter, in the smell of herbs and sulphur. {vesna.He} says that {vesna.he} is in no hurry. {vesna.He} says the game will wait. The panel, in your eye, sends a polite line of text. [RELATIONSHIP: Friend (Very High). Next level: ???.] It does not say how to reach it.", "@Vesna| I like a long level. I like a good slow build."}))
                .outro(
                        "* The panel dims. A line of text appears in the centre, in a white sans-serif, very plain. [CHAPTER TWO LOADING.] A bar appears under it. It fills to eighty-eight per cent, and sticks. You watch it for minutes. The village goes about its business behind it. Nib calls from across the road that the soup is ready. You do not close the panel; you are, you realise, a little curious.")
                .done((c, st) -> { int l = Chapters.flag(st, "love"); Chapters.bond(c, st, "vesna", l == 3 ? 1 : 3); if (l == 1 || l == 2) Chapters.wed(c, st, "vesna"); })
                .pay(1000, 0, 0, 1, 1, 1);

        d.interlude(1, "Loading...",
                "* The village has a name now on the panel's map. It is called: Starting Village. It is the only entry on the map. The panel is very proud of it.",
                "?class=1|* The sword is hung over the fireplace. It is a very good sword. A great many people have come to ask you to save them. You have been getting rather good at saying that you will think about it.",
                "?class=2|* The picks live in a drawer in the kitchen. They are used, on the whole, for opening jars. It is a use the panel has chosen not to mention.",
                "?class=3|* The panel lists your class as Human (Level 1), and has done so since the day it was offered. Nobody has been able to improve on it.",
                "* The panel says that Chapter Two is loading. It has said so for three months. You have stopped checking it. Mister Whiskers calls on Thursdays, for a sardine, and sits on the windowsill with his keys. He says that the panel is a very young piece of work, and doing quite well. He says it will be a fine thing when it is grown.");
        TaleCheaterSeasons.add(d);
    }
}
