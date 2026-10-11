package Game;

import java.util.ArrayList;
import java.util.HashMap;

/** Everything people say in Dialogue, as data. A line is looked up by its kind and the speaker's voice (a kind with no line for that voice falls back to the "*" lines).
 *  Slots: {you} how the speaker names the listener (a name, "friend", "Senior"...), {You} the same capitalised, {Name} the listener's first name, {me} the speaker's first name,
 *  {place} the tile, {season}, {sect}, {thing} and the others each scene fills in. */
final class DialogueText {
    private DialogueText() { }

    enum Voice { WARM, CHEERFUL, BLUNT, FORMAL, SLY, WEARY, SCHOLAR, HOSTILE }

    private static final HashMap<String, String[]> LINES = new HashMap<>();

    private static void add(String kind, String voice, String... lines) { LINES.put(kind + "|" + voice, lines); }

    /** The lines for this kind in this voice; the "*" lines when the voice has none of its own; never null. */
    static String[] lines(String kind, Voice v) {
        String[] r = LINES.get(kind + "|" + v.name());
        if (r == null) r = LINES.get(kind + "|*");
        return r == null ? new String[0] : r;
    }

    static boolean has(String kind) {
        for (Voice v : Voice.values()) if (LINES.containsKey(kind + "|" + v.name())) return true;
        return LINES.containsKey(kind + "|*");
    }

    static ArrayList<String> kinds() {
        ArrayList<String> k = new ArrayList<>();
        for (String key : LINES.keySet()) { String kind = key.substring(0, key.indexOf('|')); if (!k.contains(kind)) k.add(kind); }
        return k;
    }

    static {
        // ------------------------------------------------------------------ meeting someone you know (the plain "talked to")
        add("greet", "WARM",
                "Good to see a friendly face out here, {you}.",
                "Well met, {you}. I hope the road has been kind to you.",
                "Oh, hello! I was hoping I'd run into someone today.");
        add("greet", "CHEERFUL",
                "Hey, {you}! Lovely day for it, whatever \"it\" is.",
                "Ha! Fancy meeting you in {place}. Got a minute?",
                "There you are! I was starting to think this place was empty.");
        add("greet", "BLUNT",
                "{You}. Busy?",
                "Hm. You again. There are worse people to meet on these roads.",
                "Nice weather for walking. Bad weather for sitting. Which are you doing?");
        add("greet", "FORMAL",
                "Greetings, {you}. May your path stay clear.",
                "I bid you good day, {you}. The qi in {place} is thick today.",
                "A pleasure, {you}. Might I trouble you for a moment?");
        add("greet", "SLY",
                "Well, well. {You}. Just the person I hoped to see.",
                "Ah, {you}. Funny how paths cross when there's something to be gained.",
                "Don't look so wary. I only want to talk. For now.");
        add("greet", "WEARY",
                "Ah... {you}. My knees say it will rain, and my knees are rarely wrong.",
                "Sit with me a while, {you}. I have fewer years than I did yesterday.",
                "*a slow sigh* Another face. Good. I'm tired of my own.");
        add("greet", "SCHOLAR",
                "Ah, {you}, perfect. I've been turning a question over all morning and I need a second mind.",
                "Forgive the interruption, {you}. I was cataloguing the herbs here and lost track of the hour.",
                "Remarkable. The qi in {place} runs differently from yesterday. Have you noticed?");
        add("greet", "HOSTILE",
                "You. I had hoped not to see you again.",
                "Keep walking, {you}. I'm not in the mood.",
                "Don't stand so close. We both remember how things stand between us.");

        // ------------------------------------------------------------------ meeting a stranger
        add("greet_first", "WARM",
                "I don't believe we've met. I'm {me}.",
                "A new face! I'm {me}. Forgive me, I always say hello.");
        add("greet_first", "CHEERFUL",
                "Oh, hello! I'm {me}. I talk to everyone, so sorry in advance.",
                "New face! I'm {me}. Tell me something interesting.");
        add("greet_first", "BLUNT",
                "I'm {me}. You?",
                "Haven't seen you around. {me}.");
        add("greet_first", "FORMAL",
                "Allow me to introduce myself: {me}.",
                "We have not been introduced. I am {me}. A pleasure.");
        add("greet_first", "SLY",
                "{me}. I know people who know people. You may know someone I'd like to meet.",
                "A stranger. Strangers are the best sort of customer.");
        add("greet_first", "WEARY",
                "I'm {me}. I won't be around long enough for you to forget it.",
                "Another young face. I'm {me}. Don't mind the grumbling, it's the age talking.");
        add("greet_first", "SCHOLAR",
                "I'm {me}. I study how qi settles in different places. And you?",
                "Forgive me, we've not met. {me}. I collect questions.");
        add("greet_first", "HOSTILE",
                "I don't know you, and I don't care to. Move along.");

        // ------------------------------------------------------------------ the question before a decision
        add("friend", "*",
                "We keep running into each other. I'd count it a kindness if you called me a friend.",
                "I don't have many friends out here. Would you be one?",
                "I'd like us to be friends, {you}. Is that so strange to ask?");
        add("friend", "WARM", "You've been good company, {you}. I'd be glad to call you a friend.", "Out here, a friend is worth more than a pill. Will you be mine?");
        add("friend", "CHEERFUL", "Okay, I'm just going to say it: let's be friends! No catch!", "You make the road shorter, you know that? Friends?");
        add("friend", "BLUNT", "I like you. Fewer words than most. Friends?", "Not good at this. Friends, then?");
        add("friend", "FORMAL", "I would be honored to count you among my friends, {you}.", "Permit me to say that I value your company. Might we be friends?");
        add("friend", "SLY", "A friendship can be... useful. To both of us. Interested?", "Between you and me, I'm a better friend than enemy. Shall we find out?");
        add("friend", "WEARY", "At my age one learns to say it plainly: I would like you for a friend.", "I've buried enough friends to know how rare they are. Will you be one?");
        add("friend", "SCHOLAR", "I find your company stimulating. Would you permit the word \"friend\"?", "I keep noting your name in my journal. It seems friendship has crept up on me.");

        add("squad", "*",
                "Mind if I travel with you? Four feet beat two.",
                "I'm headed your way. May I join your party, {you}?",
                "Strength in numbers. Take me along?");
        add("squad", "WARM", "Let me come along, {you}. I'd feel safer, and you'd have company.", "I'd be glad to walk with you. Would you have me?");
        add("squad", "CHEERFUL", "Adventure party! I call dibs on the snacks. Can I join?", "Please say yes, I'm so bored of my own jokes.");
        add("squad", "BLUNT", "I fight. You fight. Together we fight less alone. Squad?", "Room for one more?");
        add("squad", "FORMAL", "I would be honored to lend my blade to your party.", "May I ask to join your company, {you}?");
        add("squad", "SLY", "I could be very useful. And I only stab people who deserve it.", "Every party needs someone who reads the room. Me. Let me in.");
        add("squad", "WEARY", "One more pair of old hands. Maybe I can keep you out of trouble.", "I've seen a lot of roads. Let me show you the quiet ones.");
        add("squad", "SCHOLAR", "Your journeys would make excellent material. May I accompany you?", "I would like to see more of the world, and you seem to know where you're going.");

        add("challenge", "*",
                "Draw your weapon, {you}.",
                "I've waited long enough. Fight me.",
                "One of us is walking away from this. Let's find out which.");
        add("challenge", "WARM", "A friendly bout, {you}? Not to the death, I swear.", "Let's see what you've learned. Easy hands, all right?");
        add("challenge", "CHEERFUL", "Spar with me! It'll be fun, I promise! (Mostly.)", "Come on, one round! Winner buys the next meal!");
        add("challenge", "BLUNT", "I want a fight, {you}. You look like you can give me one.", "Talking is slow. Let's settle this.");
        add("challenge", "FORMAL", "I request a duel, {you}, in good form.", "Honor requires that we cross blades. Will you accept?");
        add("challenge", "SLY", "Nothing personal. But I need what you carry.", "You should have taken the other road, {you}.");
        add("challenge", "WEARY", "I want to see if these old arms still remember how. Humor me.", "One more fight before the years take it from me.");
        add("challenge", "SCHOLAR", "I'd like to test a theory of mine against your technique.", "Purely in the interest of research: defend yourself.");
        add("challenge", "HOSTILE", "I've been dreaming of this, {you}. Today we finish it.", "No more words between us. Fight.");

        add("alliance", "*",
                "My sect proposes an alliance. Our elders believe we are stronger standing together.",
                "We have no quarrel with you. Let us stand side by side instead.");
        add("alliance", "FORMAL", "On behalf of {sect}, I offer your sect the hand of alliance.");
        add("alliance", "SLY", "Think of it as insurance. For both of us, naturally.");
        add("tribute", "*",
                "My sect demands tribute. Pay, and there will be no trouble.",
                "Your sect is weak and mine is not. Do the sensible thing.");
        add("tribute", "FORMAL", "{sect} requires a tribute of your sect. It is a small price for peace.");
        add("tribute", "HOSTILE", "Pay, or we come for what you have.");
        add("defect", "*",
                "My sect has room for someone of your talent. Leave your old banner and join us.",
                "You are wasted where you are. Come to us.");
        add("defect", "WARM", "You'd be among friends with us. Think about it?");
        add("defect", "SLY", "Our offer is generous. Our patience is not.");
        add("invite", "*",
                "You have a gift, {you}. Come to {sect}, and we will train it properly.",
                "I have been watching you. {sect} could use someone like you.");
        add("invite", "WARM", "There is a place for you in {sect}, {you}. Come and see.");
        add("invite", "FORMAL", "{sect} would be honored to count you among its disciples.");

        add("farewell", "WARM", "Take care, {you}. Walk safely.", "Until next time. Come back in one piece.");
        add("farewell", "CHEERFUL", "Bye! Don't do anything exciting without me!", "See you! Say hi to the road for me.");
        add("farewell", "BLUNT", "Right. Go.", "Later.");
        add("farewell", "FORMAL", "Farewell, {you}. May the Heavens look kindly on you.", "I shall not keep you. Good day.");
        add("farewell", "SLY", "Remember who was nice to you.", "Pleasure doing business, {you}.");
        add("farewell", "WEARY", "Go on, then. The young have places to be.", "Mind yourself out there. I'd like to see you again.");
        add("farewell", "SCHOLAR", "I must write this down before I forget it. Farewell, {you}.", "Good talking. I've three new questions now. Thank you.");
        add("farewell", "HOSTILE", "We're done here.", "Leave. Now.");

        // ------------------------------------------------------------------ a scene's opening words (the voice's own way into it)
        add("open", "WARM", "Ah, {you}!", "Oh, {you}, good.");
        add("open", "CHEERFUL", "Hey, {you}!", "Oh! {You}!");
        add("open", "BLUNT", "{You}.", "Listen.");
        add("open", "FORMAL", "Pardon me, {you}.", "If I may, {you}.");
        add("open", "SLY", "Psst. {You}.", "A word, {you}?");
        add("open", "WEARY", "Ah... {you}.", "*clears throat* {You}.");
        add("open", "SCHOLAR", "Ah, {you} --", "Pardon, {you} --");
        add("open", "HOSTILE", "You.", "Hold it.");

        // ------------------------------------------------------------------ replies to how you greeted them
        add("r_warm", "*", "Kind of you. Few bother.", "And you, {you}. It's a good day for it.");
        add("r_warm", "WARM", "That warms an old heart. Thank you.", "And you, {you}. Come, sit a moment.");
        add("r_warm", "CHEERFUL", "Ha! See, that's the spirit!", "You're nice. I like you already.");
        add("r_warm", "BLUNT", "Hm. Good.", "Same to you.");
        add("r_warm", "FORMAL", "You honor me, {you}.", "Your courtesy is noted, and returned.");
        add("r_warm", "SLY", "Friendly. I like that in a person.", "Careful, {you}. Kindness is how I find my best customers.");
        add("r_warm", "WEARY", "You'll make my day, you know. It doesn't take much anymore.", "Kind of you. Few bother with the old.");
        add("r_warm", "SCHOLAR", "How refreshing. Most people merely grunt.", "Splendid. Courtesy is the first thing to go when people get busy.");
        add("r_warm", "HOSTILE", "Save it.");
        add("r_nod", "*", "Of course. Don't let me keep you.", "Right. Go well.");
        add("r_nod", "WARM", "Safe travels, then.", "Another time. Take care.");
        add("r_nod", "CHEERFUL", "Okay! Bye! Come back and tell me everything!", "Sure, sure! See you on the road!");
        add("r_nod", "BLUNT", "Hm.", "Fine.");
        add("r_nod", "FORMAL", "Of course. Good day to you.", "I shall not detain you.");
        add("r_nod", "SLY", "Another time, then. I'm patient.", "Off you go. We'll talk later.");
        add("r_nod", "WEARY", "Go on. The young are always in a hurry.", "Mm. Off you go.");
        add("r_nod", "SCHOLAR", "Another time. I'll be here, probably cataloguing something.", "Of course. Do come back if you notice anything odd.");
        add("r_nod", "HOSTILE", "Good.");
        add("r_cold", "*", "Well. That was rude.", "I see. Forget I spoke.");
        add("r_cold", "WARM", "Oh. All right. I'm sorry I troubled you.", "I didn't mean to bother you. Take care.");
        add("r_cold", "SLY", "I'll remember that.", "Noted. I have a long memory, {you}.");
        add("r_cold", "HOSTILE", "Good. Keep walking.", "Gladly.");
        add("r_cold", "FORMAL", "As you wish.", "Then I will not impose.");
        add("r_spar", "*", "Good bout. You've got something there.", "That was fun. Again sometime.");
        add("r_spar", "WARM", "Thank you. That was exactly what I needed.", "Well done, {you}. I'd say you've grown.");
        add("r_spar", "CHEERFUL", "Whoo! My arm's still ringing. Again soon!", "That was great! Who's buying lunch?");
        add("r_spar", "BLUNT", "You hit harder than you look.", "Good. Same time tomorrow.");
        add("r_spar", "FORMAL", "Well fought. I thank you for the lesson.", "An honorable bout. Thank you.");
        add("r_spar", "SCHOLAR", "Fascinating. I have notes to make. Thank you.", "Hm! Your left foot leads late. Wonderful. Thank you.");
        add("r_spar", "WEARY", "Hah. My bones will complain tomorrow. Worth it.", "It's been a long time since I enjoyed being hit.");
        add("r_spar", "SLY", "Not bad. Not bad at all.", "You fight dirtier than you look. I approve.");
    }
}
