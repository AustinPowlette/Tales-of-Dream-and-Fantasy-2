package Game;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** The endings of the story (see "Lore Design.md"): what happens when a human (or an NPC seeker) opens the Door and crosses the Seam, and everything that follows.
 *
 *  Beat 1: the Seam, then the Old Man (the Gardener, who lives Outside; ??? is only his image and cannot leave the Garden) explains the war and his array of nine great lamps.
 *  Beat 2: Stay (Good: the Steward; Evil: the Usurper; Neutral: the Idler; can be undone later with `lore leave`) or Leave (final): become a lamp-bearer (Good, closed once nine
 *  lamps are carried), strike at the Gardener (Evil: 50/50, always death once the array is whole; the blood of the dead gives everyone +5 potential, stacking) or walk out alone (Neutral).
 *  Beat 3: the aftermath, a story stub per path and a random branch of the Outside. A hidden ending (potential 200) kills the Gardener, which destroys the Garden; so does the
 *  Gardener's own death in the last week of the world (those who can cross choose the Outside or the dark). Every ending has funny credits, a Hall of Fame line with a score and a
 *  line in the Roll of the Crossed. NPC seekers walk the same road in the background (`seekersYearly`, `npcCross`). The Giant Footprints are optional places near the Thin Sky
 *  and the Garrison: a short clue and a rare relic each. */
final class LoreEndings {
    private LoreEndings() { }

    static final List<Character> releases = Collections.synchronizedList(new ArrayList<Character>());
    /** Tests: the ids of the endings reached, in order (not saved). */
    static final List<String> reached = Collections.synchronizedList(new ArrayList<String>());

    /** Called once a tick on the game thread: a human who left as a soldier chooses what to play next (the same dialog as after a death). */
    static void drainReleases() {
        while (!releases.isEmpty()) {
            Character c = releases.remove(0);
            if (c != null) GameDriver.handleReincarnation(c);
        }
    }

    // ------------------------------------------------------------------ the scenes (story stubs)
    private static void scene(String id, String title, String... lines) { LoreText.SCENES.put(id, new LoreText.Scene(id, title, lines)); }

    static final String[] OUT_IDS = {"out_road", "out_throne", "out_teahouse", "out_mirror", "out_hollow", "out_deserter", "out_garden", "out_ferry", "out_farm", "out_peak", "out_fall", "out_dust"};
    static final String[] OUT_TITLES = {"The Free Road", "The Small Throne", "The Teahouse at the Edge of the War", "The City of Mirrors", "The Hollow Edge", "The Deserters' Camp", "The Other Garden", "The Ferryman",
            "The Quiet Farm", "The Long Climb", "The Cold Pass", "The Dust of the Road"};
    static final String[] OUT_TAG = {"good", "evil", "neutral", "mystery", "bad", "neutral", "mystery", "mystery", "neutral", "neutral", "bad", "neutral"};
    static final int[] OUT_BONUS = {1700, 1200, 1500, 1900, 600, 1400, 2000, 1800, 1300, 2100, 400, 1100};

    static void registerScenes() {
        // ---- beat 1: the Seam, then the Old Man explains
        scene("end_threshold", "The Seam Asks",
                "* The Empty Seat is warm. Under it, the Seam stands open. It looks like a crack of pale light in the floor.",
                "I've told you everything I'm allowed to tell. What you do now is your choice. I won't push you.",
                "Past the Seam is the Outside. The one who made this Garden lives there. He's waiting for you.",
                "I can't go with you. I'm only his image. I belong to the Garden, the way a painting belongs to a room.",
                "Please be careful. And eat something first.");
        scene("end_oldman", "The Outside",
                "* You step into the Seam. The light is cold. Then it is gone, and you are standing on a plain of grey ash, under a sky with no edge.",
                "* A camp stretches across the plain. The soldiers in it are not young. Each one is at least a Half-Immortal, and many are True Immortals. They are hundreds of years old, or thousands. Their armor is dented, mended, and dented again.",
                "* At the center of the camp, on a plain wooden seat, sits an old man. His robe is grey. His hands are scarred. You know his face. It is the face of ???, but older and far more tired.",
                "OLD MAN: Welcome. I am the Gardener. The Garden you came from lives inside my mind. I made it. ??? is my image there. He isn't a person, but he's the part of me that cares about you, and he has told you the truth, a little at a time.",
                "OLD MAN: There is a war. The enemy is the Hollow Court. They don't kill people. They put out whole skies, one after another, until nothing is left.",
                "OLD MAN: My way is the array. I build great patterns of light. Each lamp in the pattern is carried by one person, and the more lamps burn, the stronger the pattern grows. With every lamp lit, the array can stand against an army of True Immortals.",
                "OLD MAN: That still isn't enough. There are levels above True Immortal, and I've only touched them. So I need more lamps, and strong people to carry them. That is why I made the Garden. It grows lamp-bearers.",
                "OLD MAN: The array holds nine great lamps, and no more. {lamps} of the nine are carried now.",
                "OLD MAN: I won't ask you to forgive me. I made you to fight. I'm no longer sure that was mine to do. I did it anyway, because the other choice was to watch the skies go out.",
                "OLD MAN: Now you choose. You can stay in the Garden, and I'll send you back to grow more soldiers, faster. Or you can leave the Garden and walk the Outside.");
        scene("end_oldman_full", "The Array Is Whole",
                "OLD MAN: I have to tell you something unkind. All nine great lamps are carried. The array is whole, and I can't give you a lamp.",
                "OLD MAN: You can still stay. You can still go. But don't raise your hand against me. The array is set now, and nothing in this world can break it.");
        scene("end_oldman_stay", "Back to the Garden",
                "OLD MAN: Then go back, and take a task with you. Grow more soldiers, and grow them faster than I could. Everyone in the Garden is a lamp that has not been lit yet.",
                "OLD MAN: How you do it is up to you. I won't watch over your shoulder. I have a war to fight.",
                "OLD MAN: The Seam will open again whenever you ask. Remember that. Staying is never a lock. Leaving is.",
                "* He lifts one scarred hand. The light of the Seam wraps around you, and you are carried home.");

        // ---- beat 3, stay: the Garden goes on, as the player
        scene("end_steward", "The Steward",
                "* You are back in the Garden. The Seam closes behind you, quietly, like a hand around a seed.",
                "You came back. I didn't think you would. Thank you. Somebody has to look after them.",
                "* Within the year, the Palace has a master with a face. The Nine Lamps are lit together for the first time. The Elders find you fair.",
                "* You hunt down the cruel so the weak can grow. You hold tournaments and give the prizes away. You learn the name of every Wick in the Outer Court.",
                "* The Garden doesn't become a paradise. It becomes a place where growing stronger is worth it, because somebody is watching who will be proud.",
                "I keep a lamp for each of them. Now there is one more person keeping watch. I'm glad it's you.");
        scene("end_usurper", "The Usurper",
                "* You are back in the Garden. The Gardener told you to grow soldiers. You decide to grow them your way.",
                "{how}",
                "I couldn't tell you no. I wasn't allowed. I want you to know that I mind.",
                "* The Garden changes, the way a field changes when a storm walks over it. People grow stronger because they are afraid, and they call the result peace.",
                "* The Good go first, because they are the ones who would stop you.",
                "* In the Palace, the Empty Seat, warm for ten thousand years, goes a little cold.");
        scene("end_idler", "The Idler",
                "* You are back in the Garden. You do nothing for a very long time, and it is wonderful.",
                "You're lying down. In the Hall of the Nine Lamps. I'd like it noted that this is not what the Hall is for.",
                "* The Elders learn to step around you. The Wicks tell the new ones: that is the person who met the Gardener, and came home to take a nap.",
                "* You grow tea. You sleep through three Convocations. A beast curls up on your feet. When it is time, you politely wake it and ask it to leave.",
                "* The weeks go by. Far away, the war goes on without you, and the lamps stay lit. The Gardener lives, and as long as he does, so does the Garden. Someday that will end. Not yet.",
                "I should disapprove. But I've watched many people try very hard for a very long time. You're the first who looks happy. I'll keep the lamps lit as long as I can. Take your time.");

        // ---- the end of the Garden (the Gardener dies: the last week of the world, or the secret ending)
        scene("end_fade", "The Last Lamp",
                "* The weeks have run out. There is no week after this one. Far away in the Outside, the Gardener's long fight is over, and the Garden, which lives only inside him, begins to go out.",
                "It's time. I'm only the part of him that stayed behind, and when he goes, I go. I wanted to say thank you. I kept a lamp for each of you. It was never a duty. I know all your names, and I'll say them until I can't.");
        scene("end_fade_slain", "The Last Lamp",
                "* A great shock runs through the sky. Far away in the Outside, someone has done what nobody thought could be done. The Gardener is dead, and the Garden, which lives only inside him, begins to go out.",
                "It's time. I'm only the part of him that stayed behind, and when he goes, I go. I wanted to say thank you. I kept a lamp for each of you. It was never a duty. I know all your names, and I'll say them until I can't.");
        scene("end_fade_door", "The Seam Is Open",
                "* The Seam stands open. Beyond it, a road of pale light runs out into the dark. Behind you, one by one, the lamps of the Garden are going out.",
                "I never minded you. I want you to hear that. Of everyone, I didn't mind you at all. Go, if you want to. Or stay with me until the end. Either way, thank you.");
        scene("end_dark", "The Light Fades",
                "* It doesn't hurt. That is the first surprise. The light only grows thin, like the last of the tea in a cup.",
                "* The rivers stop first. They don't dry up. They only grow still, so that you can see the sky in them one last time. The sky is full of the faces of everyone you have known.",
                "* The mountains forget their names. The cities forget their streets. Somewhere a bell begins to ring, and it doesn't finish the note.",
                "* You think of the first morning. You were eighteen. The road was long and the whole world was new. You think of the people you met, the ones you loved, and the ones you fought. You remember them better than you ever did, and you are not afraid.",
                "* The lamps go out, one at a time. Dawn. Patience. Salt. Iron. Rain. Ash. Glass. Hunger. Winter. You say each name, and each one is a little quieter than the last.",
                "* At the end there is only one light, small as a seed. You hold it in your cupped hands. It is warm. It is the only thing left in the world, and it is enough.",
                "* Then it too goes gently into the dark. The Garden isn't destroyed. It is finished, like a story at the end of its book. Somewhere in the dark, a very long way off, someone closes it with great care.");

        // ---- beat 3, leave: Soldier (Good). The Old Man's fight
        scene("end_soldier", "The True Ending: The Lamp-Bearer",
                "OLD MAN: Good. Take this.",
                "* He puts a small lamp in your hands. It is warm. It flickers once, like a glad eye. Something in your chest answers it, as if a lamp had been lit there years ago and you had only now noticed.",
                "OLD MAN: Nine great lamps hold the array. Yours is number {lampNo}. Behind you are the small lamps: every cultivator from the Garden who came before you. They've been waiting.",
                "* You turn. Down the slope of the camp, soldiers from the Garden are lining up. There are hundreds. Next to the old veterans they look very young, and they are afraid. They hold their lamps in both hands. When they see you, they stand a little straighter.",
                "* Then you notice what is missing. No one in the line is old. You ask where the ones who came before them are. The Old Man looks at the lamp in your hands.",
                "OLD MAN: I never lied to you. I never said it would be kind.");
        scene("end_soldier_front", "At the Front",
                "* You take the place at the front. The old veterans make room for you without a word. It is the most dangerous place on the plain, and they know it.");
        scene("end_soldier_guard", "Beside the Young",
                "* You stand among the young ones. You show them how to hold a lamp when their hands shake, and how not to run. You tell them: stay close, and you'll see morning.");
        scene("end_soldier_keep", "At the Back",
                "* You take the place at the back, with the wounded. A lamp that goes out in a fight can't be lit again, so someone has to keep them steady. You keep them steady.");
        scene("end_soldier_war", "The First Dawn",
                "* The Hollow Court comes at dawn. It makes no sound. The sky goes dark, patch by patch, and the dark moves toward the camp.",
                "* The Gardener lifts his hands. Nine great lamps flare, and thousands of small ones with them. The array rises over the camp like a second sun.",
                "* It is a bitter fight. The dark doesn't die. It only goes quiet, and comes back. Many fall. People you knew in the Garden fall. Veterans who have fought for a thousand years fall beside them.",
                "* Somehow, the line holds. When night comes, the dark pulls back. You are alive. Fewer of the others are than you hoped.",
                "OLD MAN: That is one fight.",
                "* He says it plainly. He isn't celebrating. There will be another at dawn. This is not the end of the war. It is only another struggle in the Outside, and you are in it now, and so is everyone you brought.",
                "* Years later, in the Garden, on a road nobody remembers building, a traveller with your walk goes by. They leave a gift on the doorstep of whoever needs it most. The Gardener marked your soul, so as long as he thinks well of you, you can always find your way home.");

        // ---- beat 3, leave: Evil. Attack the Old Man
        scene("end_attack", "The Blow",
                "* You don't take the lamp. You turn to the old man, and with everything you have, you strike at him.",
                "* The camp goes quiet. The veterans don't even draw their weapons. They look at you the way you might look at a child who has picked up a sword.",
                "OLD MAN: I know why. I made you strong and I never asked whether you wanted it. That is my fault.",
                "* The old man raises one hand.");
        scene("end_attack_whole", "The Array Is Whole",
                "* The array rises before you can move. All nine great lamps burn, and it doesn't hesitate. It doesn't need to.");
        scene("end_attack_dead", "Struck Down by the Array",
                "* The array answers. It doesn't feel like pain. It feels like being counted, and found to be too much.",
                "OLD MAN: I'm sorry. I didn't want this.",
                "* Your body comes apart into light. The light doesn't go out. It runs back down the Seam like water down a drain, all the way back to the Garden.",
                "* That night, in the Garden, a rain falls that tastes of iron. A great many people wake up a little stronger than they were. They don't know why. Nobody tells them it was you. Your blood has lifted the potential of the whole world, for ever.");
        scene("end_attack_spared", "Spared",
                "* The light of the array stops a finger's width from your face. It hovers. It trembles. Then it goes out.",
                "OLD MAN: No. I need every lamp I can find, even a dark one. Go. Don't come back until you want to carry something.",
                "* The Seam closes behind you. Ahead, the sky has no walls.");

        // ---- beat 3, leave: Neutral. Walk out alone
        scene("end_leave", "Alone",
                "* You turn away from the lamp and from the army. The old man watches you go. He doesn't stop you.",
                "OLD MAN: Walk well. If you ever want a lamp, you know where to find me.",
                "* The Seam folds shut behind you, small as a lit window. Ahead, the sky has no walls.");

        // ---- the Hidden Ending: the Gardener killed
        scene("end_ascendant", "The Hidden Ending: Past the Ceiling",
                "* Two hundred. The number is no longer a measure. It is a statement.",
                "* The Garden has a ceiling: level 101, True Immortal. It was the last rung of the ladder the Gardener built. You put your hand against it. It is thin as paper, and you push.",
                "Stop. Please. I'm not allowed to ask that, so I'm asking as myself. There is no level after the last one. I would know. I helped write the tables.",
                "* There is one. The tables have no name for it. The Garden was built to hold a hundred thousand people, none stronger than the man who dreamed them. It makes a sound like a lamp being lifted off its hook.",
                "* You walk into the Seam the way a flood walks into a field. You are not called. You are not carried. You are coming.",
                "* In the Outside, the camp goes quiet. The veterans stand. At the center, on a plain wooden seat, the Gardener looks up.",
                "OLD MAN: You are not on the roll of lamps.",
                "* He lifts his hands. The array rises: nine great lamps and a thousand small ones, bright enough to hold back the Hollow Court. It meets you, and you walk through it, because no lamp in it is brighter than what you have become.",
                "* You kill him. It isn't hard. That is the part nobody warned you about. He is a very old, very tired man. The array that could hold a thousand True Immortals does not know what to do with someone who has gone past them.",
                "* Far behind you, the Garden begins to go dark. It lived inside him. Now there is no inside. The bell stops. The lamps lean toward you, and one by one they go out.",
                "* The war goes on. The army looks at you for a long moment, and then turns back to the dark. For the Hollow Court, it is one lamp fewer. For you, it is the biggest thing you have ever done, and you are the only person who knows how small it was.");

        // ---- the Outside: the random branches (Neutral, and everything after a spared attack or the Garden's end)
        scene("out_road", "The Free Road",
                "* You walk out of the Seam into rain, and the rain is real, and it smells of an ordinary hill.",
                "* There is a village below. It is on the wrong side of a war it has never heard of. You carry a woman's grain cart over a ford because it is on your way, and then because it isn't.",
                "* Within the year you have taught forty farmers to hold a spear, and forty more not to need one.",
                "* They call you the Free Road, because wherever you stood, the soldiers of every side found they could walk through without being asked for anything.");
        scene("out_throne", "The Small Throne",
                "* The Outside has few people who can do what you can do, and many who will pay to be protected from them.",
                "* You take a river valley, then the next, then a mountain pass with a good view. Nobody tells you no, and you find you have stopped asking whether anybody should.",
                "* You aren't a monster. You are a very good lord, the way a storm is good for rivers. The valley prospers. The valley is afraid. The valley prospers.",
                "* Sometimes, very late, you hear a bell that isn't there. You ring for more wine, and the bell goes away.");
        scene("out_teahouse", "The Teahouse at the Edge of the War",
                "* The war is a line on the horizon that moves a little every season. You build a teahouse on the side of it that isn't on fire.",
                "* Soldiers of both armies come in, and they are polite. The tea is very good. The owner is said to have come from nowhere, and is clearly not to be argued with.",
                "* Nobody wins the war. It simply stops one spring, because there is nobody left who remembers what they were fighting about, and they are all in your teahouse.");
        scene("out_mirror", "The City of Mirrors",
                "* Past the third ridge there is a city built entirely of glass. In every pane there is a person who is you, walking a different road.",
                "* In one pane you are the Steward. In another you wear a crown. In a third you are small and nobody is looking at you, and you are happy.",
                "* A woman made of reflections tells you that the city is a place where a Garden goes to be remembered. 'There are others,' she says. 'Have you never wondered why the Seam was already open?'",
                "* You step through a pane. It is cold. It isn't your Garden. There is a bell, and it is ringing.");
        scene("out_hollow", "The Hollow Edge",
                "* The sky beyond the third ridge isn't dark. It is absent. It is a place where the lamps have been put out, and the putting-out is still going on, quietly, like frost.",
                "* You raise your hands. It doesn't matter. It doesn't kill you. It only goes on subtracting. Your name, first. Then your face. Then the reason you came.",
                "* You are pulled out by a hand you don't see, and set down in a field with no memory of your own name. A tired soldier wipes your face with a cloth, and says: 'You were gone for a while. Welcome back. What do you remember?'",
                "* You remember a bell. It is the only thing.");
        scene("out_deserter", "The Deserters' Camp",
                "* They are all veterans of the war, hundreds of years old, in the dented armor of people who stood a long time in the line. They have put their spears down.",
                "* 'We aren't afraid,' says their sergeant, a Half-Immortal with three scars. 'We're only tired. He never taught us to rest. Will you teach us?'",
                "* You don't know how to teach rest. You sit with them, and show them, and by the end of the season nobody has deserted any further, and nobody has gone back either.",
                "* Somewhere over the hill, an old man with scarred hands stops giving orders for a moment, and listens.");
        scene("out_garden", "The Other Garden",
                "* The Outside isn't empty. It is full of gardens: pockets in other minds, each with its own bell, each with its own image keeping its lamps.",
                "* You find one with a small dry orchard and an image that wears the face of a girl. She doesn't know she is an image. She is trying very hard to run a school.",
                "* You don't tell her. You become her first student, quietly, and stay the winter.",
                "* In spring you ask who her Gardener is. She says: 'I'm not permitted to say.' And you realise how very many of these there must be.");
        scene("out_ferry", "The Ferryman",
                "* At the edge of the Outside there is a river the color of nothing, and a boat, and a ferryman who knows you.",
                "* 'You're early,' he says. 'Or late. I never get it right. Pay the fare.' You hold out your hand. In it is a very small lamp. You don't remember being given it.",
                "* He rows. The far shore is a bell, and a hall, and an empty seat, very like the one you left.",
                "* 'That is where it all began,' he says. 'Did he never tell you? He built the Garden to have somewhere to come back to.'");
        scene("out_farm", "The Quiet Farm",
                "* You walk until the road gets quiet. Then you walk a little more. There is a farm with a broken fence.",
                "* You mend the fence. You learn to plow. You are not the strongest person for a hundred miles, or the weakest. You are just the neighbor.",
                "* Once, a soldier in dented armor stops for water. He mentions an old man with a great array of lamps, far to the north. You nod and pass the bucket. It is a good bucket.",
                "* At night you light one lamp on the porch, for no reason you can name, and you sleep well.");
        scene("out_peak", "The Long Climb",
                "* You don't stop at the edge of the Garden. You keep climbing.",
                "* The Outside has levels the Garden never named. You find a teacher in a cave who hardly speaks. You find a mountain that tries to crush you, and you don't let it.",
                "* Eighty years later, you have gone past anything you thought possible. On a clear night you can see a great light, far away: the Gardener's array, shining against the dark.",
                "* You could go to it. You could even join. You wave. Then you turn back to the mountain. There is still more to climb.");
        scene("out_fall", "The Cold Pass",
                "* You cross the Outside with no map. For a while it goes well.",
                "* Then, at a mountain pass, you meet a stranger who wants what you carry. He is stronger than you. You fight him anyway.",
                "* It is a quick death and a plain one. Nobody in the Outside knows your name. Snow covers you.",
                "* The last thing you see is a very small star, shaped like a lamp.");
        scene("out_dust", "The Dust of the Road",
                "* The Outside is larger than the Garden, and you are not the strongest in it. You are not even close.",
                "* You carry loads for a trader. Then you keep his books. Then you keep his daughter's books. You never rise past the first realm again.",
                "* It turns out to be a long, small, happy life. At the end, a young cultivator asks whether you ever wanted more. You think for a long time.",
                "* 'Once,' you say. 'It was enough to have wanted it.'");
    }

    // ------------------------------------------------------------------ the blessing of a soldier's line
    static int childBlessing(Character p) { return p == null ? 0 : p.blessing + (p.wanderingImmortal ? 5 : 0); }
    static int inheritedBlessing(Character mother, Character father) { return Math.max(childBlessing(mother), childBlessing(father)); }

    /** `soldier.wanderingImmortal` is already set: every descendant, living or not, is lifted to the blessing of its parent line (+5 strength, +5 potential per soldier). */
    static int bless(Character soldier) {
        ArrayDeque<Character> q = new ArrayDeque<>();
        Set<Character> seen = Collections.newSetFromMap(new IdentityHashMap<Character, Boolean>());
        q.add(soldier); seen.add(soldier);
        int n = 0;
        while (!q.isEmpty()) {
            Character x = q.poll();
            int b = childBlessing(x);
            if (x.children == null) continue;
            for (Character ch : new ArrayList<>(x.children)) {
                if (ch == null || !seen.add(ch)) continue;
                if (ch.blessing < b) { int d = b - ch.blessing; ch.blessing = b; ch.potential = Math.min(100, ch.potential + d); n++; }
                q.add(ch);
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ the lamps and the blood
    /** The Gardener's array holds nine great lamps. Every soldier (human or NPC) carries one; once all are carried the soldier ending is closed, the hidden ending is locked,
     *  and an attack on the Gardener always ends in death. */
    static final int LAMP_MAX = 9;
    static int lampsCarried(Lore.State s) { return s == null || s.wanderers == null ? 0 : s.wanderers.size(); }
    static boolean lampsFull(Lore.State s) { return lampsCarried(s) >= LAMP_MAX; }

    /** The potential every cultivator born from now on gets for the blood that went back to the Garden (5 per sacrifice, stacking, never past 100). */
    static int bloodBonus() { Lore.State s = Lore.state; return s == null ? 0 : s.blood; }

    /** Somebody attacked the Gardener and died: their blood returns to the Garden, +5 potential for everyone alive (and everyone born later; it stacks). */
    static void bloodSacrifice(String who) {
        Lore.State s = Lore.state;
        if (s == null) return;
        s.blood += 5;
        for (Character x : new ArrayList<>(Map.characters)) if (x != null && !x.dead) x.potential = Math.max(x.potential, Math.min(100, x.potential + 5));
        for (Character x : new ArrayList<>(Map.tempCharacters)) if (x != null && !x.dead) x.potential = Math.max(x.potential, Math.min(100, x.potential + 5));
        Chronicle.add("legend", who + " fell on the Gardener's array. Their blood ran back into the Garden: every cultivator, now and to come, has +5 potential (" + s.blood + " in all).");
    }

    // ------------------------------------------------------------------ the choice: the Seam, the Old Man, Stay or Leave, and what follows
    private static String[] opt(String... s) { return s; }

    /** `lore cross` at the open Seam (all checked by Lore.whyNotCross): the Seam, the Old Man explains, then Stay or Leave. Returns true when an ending was reached. */
    static boolean choose(Character c, Lore.PState p) {
        Lore.State s = Lore.state;
        if (!p.metOldMan) {
            if (!p.scenes.contains("end_threshold")) Lore.show(c, p, "end_threshold");
            int go = DecisionBroker.ask(c, "The Seam is open. Step through and meet the one who made the Garden?", opt("Step through", "Not yet"), 1);
            if (go != 0) { PlayerCommandsSay("The Seam waits. It has waited this long."); return false; }
            p.metOldMan = true;
            Lore.show(c, p, "end_oldman");
            if (lampsFull(s)) Lore.show(c, p, "end_oldman_full");
        }
        return decide(c, p);
    }

    /** Stay or Leave (beat 2), after the Old Man has spoken. Stay can be undone later (`lore leave`); Leave cannot. */
    private static boolean decide(Character c, Lore.PState p) {
        while (true) {
            int top = DecisionBroker.ask(c, "The Gardener waits. What do you do?", opt("Stay in the Garden and grow its soldiers", "Leave the Garden", "Not yet"), 2);
            if (top == 2) { PlayerCommandsSay("The Seam waits. It has waited this long."); return false; }
            if (top == 0) { if (stayMenu(c, p)) return true; }
            else if (leaveMenu(c, p)) return true;
        }
    }

    /** Stay: the Old Man's task, then Good, Evil or Neutral. Returns true when an ending was reached, false for Back. */
    private static boolean stayMenu(Character c, Lore.PState p) {
        if (!p.scenes.contains("end_oldman_stay")) Lore.show(c, p, "end_oldman_stay");
        while (true) {
            int al = DecisionBroker.ask(c, "How will you grow the soldiers?", opt("As the Steward: make everyone stronger (Good)", "By taking what the world has (Evil)", "By living as you please (Neutral)", "Back"), 3);
            if (al == 3) return false;
            if (al == 0) return stayGood(c, p);
            if (al == 1) {
                int how = DecisionBroker.ask(c, "What becomes of the Palace?", opt("Take it and make it yours", "Destroy it", "Neither: go your own way (a sect of your own, or alone)", "Back"), 3);
                if (how == 3) continue;
                return stayEvil(c, p, how);
            }
            int how = DecisionBroker.ask(c, "How do you live?", opt("Take the Palace and nap on its throne", "Leave the Palace be and go your own way", "Back"), 2);
            if (how == 2) continue;
            return stayNeutral(c, p, how);
        }
    }

    /** Leave: Soldier (Good), attack the Gardener (Evil) or walk out alone (Neutral). Returns true when an ending was reached, false for Back. */
    private static boolean leaveMenu(Character c, Lore.PState p) {
        Lore.State s = Lore.state;
        boolean full = lampsFull(s);
        while (true) {
            int lv = DecisionBroker.ask(c, "How do you leave the Garden?", opt(full ? "Become a lamp-bearer (closed: all nine lamps are carried)" : "Become a lamp-bearer in the Gardener's array (Good)",
                    "Strike at the Gardener (Evil)", "Walk out alone and find your own road (Neutral)", "Back"), 3);
            if (lv == 3) return false;
            if (lv == 0) {
                if (full) { PlayerCommandsSay("The Gardener shakes his head. All nine great lamps are carried; there is no lamp left for you."); continue; }
                return soldier(c, p);
            }
            if (lv == 1) return attack(c, p);
            return leaveAlone(c, p);
        }
    }

    /** `lore leave` (or `lore cross` again): a human who stayed goes back through the Seam. Leaving is final. */
    static boolean leave(Character c, Lore.PState p) {
        if (!stayedOf(p)) { PlayerCommandsSay("You have not stayed."); return false; }
        PlayerCommandsSay("The Seam opens for you, as the Gardener promised.");
        return leaveMenu(c, p);
    }

    /** A human whose ending was a stay (the Steward, the Usurper, the Idler): they can still leave, and the world's last week still finds them here. */
    static boolean stayedOf(Lore.PState p) {
        return p != null && p.ended && (p.stayed || p.endingId.matches("steward|usurper\\d|idler\\d"));
    }

    /** Leaving after a stay: the chores of a Steward or an Idler stop, and a master gives the Palace back to its image. */
    private static void undoStay(Character c, Lore.PState p) {
        if (!stayedOf(p)) return;
        p.stayed = false; p.stewardActive = false; p.idleUntil = 0;
        Organization pal = Lore.palace();
        if (pal != null && pal.master == c) { pal.virtual = true; pal.master = null; pal.memberRanks.remove(c.id); }
    }

    private static void PlayerCommandsSay(String t) { PlayerCommands.say(t); }

    static void setAlign(Character c, int a) { c.affiliationNum = a; c.affiliation = a > 0 ? "Good" : a < 0 ? "Evil" : "Neutral"; }

    // ------------------------------------------------------------------ the Palace under a human master
    static void becomeMaster(Character c, Organization pal) {
        if (c.sect != null && c.sect != pal) c.sect.resign(c);
        if (c.sect != pal) { pal.members.add(c); c.sect = pal; c.joinedASect(); }
        pal.virtual = false; pal.master = c; pal.founder = c; pal.crisisTimer = 0;
        pal.memberRanks.put(c.id, Organization.SectRank.MASTER);
    }

    static void leavePalace(Character c) { if (c.sect != null && (c.sect.palace || c.sect.virtual)) c.sect.resign(c); }

    static void dissolvePalace(Organization pal) {
        for (Character m : new ArrayList<>(pal.members)) if (m != null) pal.resign(m);
        pal.members.clear(); pal.memberRanks.clear(); pal.master = null;
        if (pal.location != null && pal.location.sect == pal) pal.location.sect = null;
        if (Lore.state != null) Lore.state.palaceGone = true;
    }

    // ------------------------------------------------------------------ the endings
    private static boolean finish(Character c, Lore.PState p, String id, String title, int bonus, boolean endsRun, List<String> lines, String note, List<String> creditsTail) {
        Lore.State s = Lore.state;
        p.ended = true; p.ending = title; p.endingId = id; p.endedWeek = GameDriver.time; p.stayed = !endsRun;
        p.score = score(c, p, bonus);
        Lore.dropQuest(c, p);
        if (!p.counted) { s.crossed++; p.counted = true; }
        String who = c.name + " " + c.lastName;
        s.roll.add(who + " (year " + GameDriver.time / 52 + ", " + title + ", score " + p.score + ")");
        Chronicle.add("legend", who + " crossed the Seam of the Palace of the Oldest Order: " + title + ".", c.id);
        RunStats.hallEnding(c, title, p.score);
        if (endsRun) RunStats.finishQuiet(c, title);
        reached.add(id);
        Notifications.post("breakthrough", "Ending: " + title + " (score " + p.score + ").", -1, c.id);
        List<String> shownLines = new ArrayList<>(lines);
        shownLines.addAll(StoryCodas.lines(c, id));   // the last scene of a Story start, in that start's own words
        List<String> tail = new ArrayList<>(creditsTail);
        String own = StoryCodas.credit(c);
        if (own != null) tail.add(own);
        present(c, p, title, shownLines, credits(c, p, title, p.score, tail));
        return true;
    }

    private static boolean stayGood(Character c, Lore.PState p) {
        Organization pal = Lore.palace();
        if (pal != null) becomeMaster(c, pal);
        setAlign(c, 1);
        p.stewardActive = true;
        return finish(c, p, "steward", "The Steward (stay, Good)", 1500, false, LoreText.lines("end_steward", Lore.vars(c, p)), null,
                Arrays.asList("The Palace is under new management. The management says the lamps are fine.", "Epilogue: a grateful wick has named a noodle dish after you. It is mostly water."));
    }

    private static boolean stayEvil(Character c, Lore.PState p, int how) {
        Organization pal = Lore.palace();
        String line, title;
        p.darkPath = true;
        setAlign(c, -1);
        if (how == 0) {
            if (pal != null) { becomeMaster(c, pal); for (Character m : new ArrayList<>(pal.members)) if (m != null && m != c && !m.dead && m.player == 0 && m.affiliationNum != null && m.affiliationNum > 0) { m.deathCause = "purged by the Usurper"; m.dead(c); } }
            title = "The Usurper: the Palace taken (stay, Evil)";
            line = "* You take the Palace the way a tide takes a beach. The Elders who object are removed. Those who stay learn to smile. The Nine Lamps burn red.";
        } else if (how == 1) {
            if (pal != null) { Chronicle.add("legend", c.name + " " + c.lastName + " razed the Palace of the Oldest Order.", c.id); dissolvePalace(pal); }
            title = "The Usurper: the Palace razed (stay, Evil)";
            line = "* You tear down the Palace. It takes eleven days, because the walls keep trying to be kind to you. At the end, the bell lies on its side in the rubble. It rings once when you kick it.";
        } else {
            leavePalace(c);
            title = "The Usurper: your own way (stay, Evil)";
            line = "* You walk out of the Palace like a man leaving a coat behind. Your sect, or your solitude, will have your name on the door and nobody's lamp in it.";
        }
        java.util.Map<String, String> v = Lore.vars(c, p); v.put("how", line.startsWith("* ") ? line.substring(2) : line);
        List<String> lines = LoreText.lines("end_usurper", v);
        if (!lines.isEmpty()) lines.set(1, line);
        return finish(c, p, "usurper" + how, title, 1200, false, lines, null,
                Arrays.asList("No good cultivators were harmed in the making of this credit. A great many were harmed afterwards.", "Spoiler: the Seat is cold."));
    }

    private static boolean stayNeutral(Character c, Lore.PState p, int how) {
        Organization pal = Lore.palace();
        String title;
        setAlign(c, 0);
        if (how == 0) {
            if (pal != null) becomeMaster(c, pal);
            PlayerControl pc = c.pc();
            if (pc != null) for (int i = 0; i < pc.duties.length; i++) pc.duties[i] = true; // a lazy master leaves the chores to the chores
            title = "The Idler: master of a very quiet Palace (stay, Neutral)";
        } else { leavePalace(c); title = "The Idler: left alone (stay, Neutral)"; }
        p.idleUntil = Integer.MAX_VALUE; // waits for the end of the world (the Gardener's death)
        return finish(c, p, "idler" + how, title, 800, false, LoreText.lines("end_idler", Lore.vars(c, p)), null,
                Arrays.asList("The protagonist is currently napping. Please leave a message.", "Fun fact: this ending has the lowest body count and the highest tea count."));
    }

    /** Leave, Good: the soldier (a lamp-bearer). The body is released and comes back to the Garden as a Wandering Immortal. */
    private static boolean soldier(Character c, Lore.PState p) {
        Lore.State s = Lore.state;
        undoStay(c, p);
        Lore.show(c, p, "end_soldier"); // the lamp number is the next free one: shown before this soldier is counted
        int role = DecisionBroker.ask(c, "How do you carry your lamp?", opt("At the front, where the fighting is worst", "Beside the young ones, to keep them alive", "At the back, to keep the light steady"), 0);
        String roleScene = role == 1 ? "end_soldier_guard" : role == 2 ? "end_soldier_keep" : "end_soldier_front";
        c.wanderingImmortal = true;
        leavePalace(c);
        c.targetQuests.clear(); c.spotQuests.clear(); c.currentQuest = null;
        c.repTitle = "Wandering Immortal"; c.maxAge = 1_000_000;
        if (s.wanderers == null) s.wanderers = new ArrayList<>();
        s.wanderers.add(c.id);
        int blessed = bless(c);
        c.deathCause = "became a lamp-bearer of the Outside";
        List<String> lines = new ArrayList<>(LoreText.lines(roleScene, Lore.vars(c, p)));
        lines.addAll(LoreText.lines("end_soldier_war", Lore.vars(c, p)));
        List<String> tail = new ArrayList<>(Arrays.asList(
                "TALES OF DREAM AND FANTASY 3: THE OUTSIDE",
                c.name + " and the Gardener's army fight on. Coming soon.",
                "(The Gardener has said 'soon' for ten thousand years. Do not hold your breath. Do hold your lamp.)",
                "Lamps carried: " + lampsCarried(s) + " of " + LAMP_MAX + (lampsFull(s) ? " (the array is whole)." : "."),
                blessed + " descendants were blessed: +5 strength and +5 potential for every soldier in the line.",
                "A Wandering Immortal now walks the Garden. They cannot die. They leave gifts. They are not sorry about the gifts."));
        boolean ok = finish(c, p, "soldier", "The True Ending: a lamp-bearer of the Outside", 3000, true, lines, null, tail);
        releases.add(c); // the player chooses another character (or ends the game)
        return ok;
    }

    /** Leave, Neutral: walk out alone into a random branch of the Outside. */
    private static boolean leaveAlone(Character c, Lore.PState p) {
        undoStay(c, p);
        return outside(c, p, "leave_neutral", LoreText.lines("end_leave", Lore.vars(c, p)), "Alone, then ", null, false);
    }

    /** One of the Outside's branches: `lead` is the story so far, `forced` the alignment the player ends with (null = the branch decides), `worldOver` = the Garden is ending (no succession dialog). */
    static boolean outside(Character c, Lore.PState p, String id, List<String> lead, String titlePrefix, Integer forced, boolean worldOver) {
        int k = pickOutside(GameDriver.rand);
        List<String> lines = new ArrayList<>(lead);
        lines.addAll(LoreText.lines(OUT_IDS[k], Lore.vars(c, p)));
        int a = forced != null ? forced : OUT_TAG[k].equals("good") ? 1 : OUT_TAG[k].equals("evil") ? -1 : 0;
        setAlign(c, a);
        String word = a > 0 ? "Good" : a < 0 ? "Evil" : "Neutral";
        String title = titlePrefix + OUT_TITLES[k] + " (leave, " + word + ")";
        c.deathCause = "left the Garden";
        boolean ok = finish(c, p, id + "_" + OUT_IDS[k], title, OUT_BONUS[k] + (forced != null && forced < 0 ? 100 : 0), true, lines, null,
                Arrays.asList("Branch of the Outside: " + OUT_TITLES[k] + " (" + OUT_TAG[k] + ").", "There are more branches. The Outside is large, and nobody mapped it."));
        if (!worldOver) leaveBody(c);
        return ok;
    }

    /** Leave, Evil: strike at the Gardener. 50/50 the array kills you (always, once every lamp is carried); if spared, you walk the Outside as an Evil wanderer. */
    private static boolean attack(Character c, Lore.PState p) {
        Lore.State s = Lore.state;
        undoStay(c, p);
        setAlign(c, -1);
        boolean full = lampsFull(s);
        PlayerCommandsSay("You raise your hand against the Gardener...");
        boolean dies = full || GameDriver.rand.nextInt(100) < 50;
        List<String> lines = new ArrayList<>(LoreText.lines("end_attack", Lore.vars(c, p)));
        if (!dies) {
            lines.addAll(LoreText.lines("end_attack_spared", Lore.vars(c, p)));
            return outside(c, p, "leave_evil_spared", lines, "Spared, then ", -1, false);
        }
        if (full) lines.addAll(LoreText.lines("end_attack_whole", Lore.vars(c, p)));
        lines.addAll(LoreText.lines("end_attack_dead", Lore.vars(c, p)));
        c.deathCause = "struck down by the Gardener's array";
        bloodSacrifice(c.name + " " + c.lastName);
        boolean ok = finish(c, p, "leave_evil_dead", "Struck down by the array (leave, Evil)", 500, true, lines, null,
                Arrays.asList("The attempt on the Gardener was rated 'brave'. The result was rated 'a short credit'.", "Your blood went home: +5 potential for everyone in the Garden, for good (" + bloodBonus() + " so far).", "Memorial lamp: lit. Memorial lamp: out."));
        c.dead(); // a true death: the chronicle and the succession dialog follow
        return ok;
    }

    /** The body leaves the Garden: gone without a death notice (the succession dialog still follows). */
    private static void leaveBody(Character c) {
        c.reincarnating = true;
        c.dead();
        c.reincarnating = false;
        c.needsReincarnation = true; Map.needReincarnation(c);
    }

    static int pickOutside(Random r) { return r.nextInt(OUT_IDS.length); }

    /** Called from Lore.tick for every human body that has finished the story. */
    static void tick(Character c, Lore.PState p) {
        Lore.State s = Lore.state;
        if (s == null || p == null) return;
        footStep(c, p);
    }

    /** `lore usurp` (hidden): potential 200 at the Empty Seat. The player goes through the Seam and kills the Gardener, and the Garden, which lives inside him, goes out. */
    static boolean usurp(Character c) {
        Lore.State s = Lore.state;
        Lore.PState p = Lore.pstate(c);
        if (s == null || p == null || !p.darkPath || p.secretDone) { PlayerCommandsSay("Nothing answers."); return false; }
        if (c.location != Lore.palaceArea()) { PlayerCommandsSay("The Seat is at Originis."); return false; }
        if (c.potential < 200 || c.lvl < 101) { PlayerCommandsSay("The Seat is warm. It is not warm enough. (Potential " + c.potential + ", level " + c.lvl + ".)"); return false; }
        if (lampsFull(s)) { PlayerCommandsSay("The Seat is cold. All nine great lamps are carried now, and the Gardener's array is whole. Nothing you are can break it."); return false; }
        p.secretDone = true;
        Organization pal = Lore.palace();
        if (pal != null && pal.master != c) becomeMaster(c, pal);
        Chronicle.add("legend", c.name + " " + c.lastName + " went through the Seam and killed the Gardener. The Garden went out.", c.id);
        List<String> tail = Arrays.asList("SECRET ENDING UNLOCKED: you were warned. Several times. In several languages. One of them was a sneeze.",
                "Outside: nobody noticed. The war continues. Please do not tell the Hollow Court; they would only be sympathetic.", "Special thanks: ??? (lamp, formerly image, pending paperwork).");
        int k = pickOutside(GameDriver.rand);
        List<String> lines = new ArrayList<>(LoreText.lines("end_ascendant", Lore.vars(c, p)));
        lines.addAll(LoreText.lines(OUT_IDS[k], Lore.vars(c, p)));
        setAlign(c, OUT_TAG[k].equals("good") ? 1 : OUT_TAG[k].equals("evil") ? -1 : 0);
        c.deathCause = "left the Garden";
        boolean ok = finish(c, p, "ascendant_" + OUT_IDS[k], "The Hidden Ending: past the ceiling, then " + OUT_TITLES[k] + " (potential 200)", 6000 + OUT_BONUS[k] / 2, true, lines, null, tail);
        gardenEnds(c);
        return ok;
    }

    static void onKill(Character killer, Character victim) {
        if (killer == null || victim == null || killer.player != 1 || Lore.state == null) return;
        Lore.PState p = Lore.pstate(killer);
        if (p == null || !p.darkPath || p.secretDone) return;
        if (victim.affiliationNum == null || victim.affiliationNum <= 0 || victim.trait == Character.Traits.Beast || victim.trait == Character.Traits.Monster) return;
        if (killer.potential >= 200) return;
        killer.potential++;
        if (killer.potential % 25 == 0 && killer.skipping <= 0) PlayerCommandsSay("[Lore] The hunger takes what the good leave behind: potential " + killer.potential + ".");
    }

    // ------------------------------------------------------------------ the score and the credits
    static int score(Character c, Lore.PState p, int bonus) {
        long wealth = 0;
        try { wealth = PlayerLists.wealth(c); } catch (RuntimeException ignored) { }
        int w = (int) (Math.log10(Math.max(10, wealth)) * 50);
        int places = p.found == null ? 0 : p.found.size(), prints = p.prints == null ? 0 : p.prints.size();
        return c.lvl * 10 + c.majorLvl * 50 + c.kills * 2 + places * 40 + prints * 25 + RunStats.years() * 3 + w + Math.min(200, c.potential) * 4 + p.lives * 100 + bonus;
    }

    static final String[] FUNNY = {
            "Directed by: ??? (not permitted to say)", "Executive Producer: The Gardener (shears not included)", "Written by: A hundred thousand people, all aged eighteen",
            "Stunt Double: the Rope", "Cinematography: the Thin Sky, which has a crack in it and is not afraid to use it",
            "Catering: the Unlit Wayhouse (the lamp remained unlit)", "Music: the Bell (same note, tuned nightly, nobody noticed)", "Casting: the Assayer's Balance",
            "Dialect Coach: none of the beasts were consulted", "Beast Wrangler: every beast who was tamed was asked first. The answer was always 'raaar'",
            "Props: the Gardener's Tools, one (1) bottomless watering can", "Costumes: Wicks of the Outer Court, one grey robe, many sizes",
            "Key Grip: the Weaver's Wall, which is still arriving", "Gaffer: whoever lit the lanterns on Lantern Night. Nobody. Nobody lit them.",
            "Legal: the Cistern of Returning (nothing leaves)", "Spirit stones harmed in the making of this game: all of them",
            "No Hollow Court members were hired. They were uncredited because they put out the credits.", "Continuity: the Menagerie of Practice (cells 1 to 100, all of them open)",
            "Medical Advisor: a Potential Pill of dubious origin", "Weather: the Garden's, as rendered by someone's very large mind",
            "Pets: three thousand and seven, all of them somebody's, none of them dogs", "Understudy for the Hero: the Hero's reincarnation",
            "The Elders were played by the Elders. They did not enjoy it.", "Safety Officer: the Door ('it opens one time in five and does not care who you are')",
            "Translations: ???", "Chief Gardener: the Gardener, regrettably absent", "Sound Mixing: wind, fire, and a Garrison's worth of unreturned lanterns",
            "Quality Assurance: nine places, one Seat, zero complaints", "Sword Fights: choreographed by the Menagerie's strays, with their consent",
            "Special Effects: the Heavens (hands)", "Accounting: the market, whose goods spoil at a very fair rate", "The Epoch was not available for comment.",
            "Marketing: a very persuasive bell", "Human Resources: the Convocation of the First Gate (the bell is not a person, but it is also in HR)"};

    static List<String> credits(Character c, Lore.PState p, String title, int score, List<String> tail) {
        List<String> out = new ArrayList<>();
        out.add("TALES OF DREAM AND FANTASY 2");
        out.add("A Garden Production");
        out.add("");
        out.add("Starring"); out.add(c.name + " " + c.lastName + " as Themselves, Mostly");
        out.add("and ??? as ???");
        out.add("");
        out.add("Ending: " + title);
        out.add("Score: " + score);
        out.add("");
        Random r = new Random((c.name + c.lastName).hashCode() * 31L + title.hashCode());
        List<String> pool = new ArrayList<>(Arrays.asList(FUNNY));
        Collections.shuffle(pool, r);
        for (int i = 0; i < 18 && i < pool.size(); i++) out.add(pool.get(i));
        out.add("");
        out.add(String.format("Kills: %d   Potential: %d   Level: %d   Lives: %d   Places read: %d of %d   Giant Footprints: %d of %d",
                c.kills, c.potential, c.lvl, p.lives + 1, p.found == null ? 0 : p.found.size(), LoreSites.ALL.length + 1, p.prints == null ? 0 : p.prints.size(), LoreSites.FOOT_NAMES.length));
        Lore.State s = Lore.state;
        if (s != null && !s.roll.isEmpty()) {
            out.add("");
            out.add("The Roll of the Crossed");
            for (int i = Math.max(0, s.roll.size() - 6); i < s.roll.size(); i++) out.add(s.roll.get(i));
        }
        out.add("");
        if (tail != null) out.addAll(tail);
        out.add("");
        out.add("No spirit stones were harmed in the making of this credit. All of them were harmed before.");
        out.add("THE END (or is it?)");
        return out;
    }

    /** A scene, then the credits (a window in the GUI, framed text in the terminal). */
    static void present(Character c, Lore.PState p, String title, List<String> lines, List<String> credits) {
        Lore.shown.add("ending:" + title);
        if (GameDriver.guiEnabled && GameDriver.mapViewer != null) {
            PlayerCommandsSay("[Lore] Ending: " + title + " (shown in its own window).");
            GuiLore.ending(title, lines, null, credits); // the run statistics are in the Run stats window (a wide table here made the scene scroll sideways)
        } else {
            PlayerCommandsSay(LoreText.frame(title, lines));
            StringBuilder sb = new StringBuilder("\n~~~~~~~~~~~~~~~~ CREDITS ~~~~~~~~~~~~~~~~\n");
            for (String l : credits) sb.append(l).append('\n');
            sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n").append(RunStats.text());
            PlayerCommandsSay(sb.toString());
        }
    }

    // ------------------------------------------------------------------ the Wandering Immortals
    static final String[] HINTS = {
            "The Cistern does not like being asked. Ask anyway.", "A weapon that meets a stronger fighter breaks. Carry two.", "The Thin Sky is cracked. Do not stand under it. (Do stand near it.)",
            "Spoiled food is not a tragedy. Spoiled food is a market signal.", "The Convocation notices anyone who crosses the second gate. It sends a lantern to find them.",
            "Evil elders murder for gear. Wear the plain one.", "A road with lit lanterns and no travellers is not a road. It is a memory.", "Every sect master is replaceable. Every Wick is somebody's child.",
            "Potential is a measure someone took of you. It is not a measure of you.", "There is an Outside. It is large. Nobody mapped it. Bring tea.",
            "The Giant Footprints are not natural. Something stood there and was pushed back.", "The bell is tuned every night by somebody who is not permitted to say so.",
            "Not every beast is a whetstone. Some are strays. Be kind to the strays.", "If a stranger leaves a gift on your doorstep, do not look for them. They are shy.",
            "The Door opens one time in five. Fall back one step, get up, try again."};

    /** Every turn of a soldier who came back: they wander, and they give. */
    static void wanderTurn(Character c) {
        if (c == null || c.dead) return;
        Random r = GameDriver.rand;
        c.age = Math.min(c.age, 60);
        if (c.location == null) return;
        for (Character h : new ArrayList<>(c.location.characters)) {
            if (h == null || h.dead || h.player != 1 || h == c) continue;
            Starts.givePills(h, Item.PillType.Cultivation, Math.max(1, Math.min(10, h.majorLvl + 1)), 1);
            if (r.nextInt(6) == 0) Starts.givePills(h, Item.PillType.Potential, 1, 1);
            String craftGift = r.nextInt(4) == 0 ? professionGift(c, h, r) : null; // and now and then something from their old trade
            String hint = hintFor(h, r);
            Notifications.post("lore", "A Wandering Immortal (" + c.name + " " + c.lastName + ") leaves a gift and a word: \"" + hint + "\"", c.id, h.id);
            if (h.skipping <= 0) PlayerCommands.say("[Lore] " + c.name + " " + c.lastName + ", a Wandering Immortal, was here. A pill on the ground" + (craftGift != null ? " and " + craftGift : "") + "; a word in your ear: \"" + hint + "\"");
        }
        java.util.ArrayList<Character> here = c.location.characters;
        int n = here.size();
        for (int tries = 0; tries < 3 && n > 1; tries++) {
            Character t;
            try { t = here.get(r.nextInt(n)); } catch (RuntimeException e) { break; }
            if (t == null || t == c || t.dead || t.player != 0 || t.trait == Character.Traits.Beast || t.trait == Character.Traits.Monster || t.age < 8) continue;
            Starts.givePills(t, Item.PillType.Cultivation, Math.max(1, Math.min(10, t.majorLvl + 1)), 1);
            if (r.nextInt(8) == 0) professionGift(c, t, r);
            break;
        }
        c.movement(); // gifts first, then on to the next place
    }

    /** What a Wandering Immortal leaves by the trade they had in the Garden, fitted to the receiver's realm: a smith a weapon, a tailor clothes, a scribe an array, an alchemist pills, a chef food, a brewer wine, a poisoner a vial, a musician an instrument,
     *  an inventor a technique, a merchant spirit stones, a gatherer herbs, a miner metal. Returns a short phrase for the message, or null (no craft). */
    static String professionGift(Character wi, Character to, Random r) {
        int lvl = Math.min(100, (to.majorLvl + 1) * 10), small = Math.max(1, Math.min(10, to.majorLvl + 1));
        switch (wi.trait) {
            case Blacksmith: { Item w = new Item(wi, Item.WeaponType.values()[r.nextInt(Item.WeaponType.values().length)], lvl); to.backpack.add(w); to.equip(); return "a weapon (" + w.name + ")"; }
            case Tailor: { Item cl = new Item(wi, lvl, Item.ItemType.Clothing); to.backpack.add(cl); to.equip(); return "clothes (" + cl.name + ")"; }
            case Scribe: { Item ar = new Item(wi, lvl, Item.ItemType.Array); if (to.arrayBag == null) to.arrayBag = new ArrayList<>(); to.arrayBag.add(ar); return "an array (" + ar.name + ")"; }
            case Alchemist: Starts.givePills(to, Item.PillType.Cultivation, small, 2); if (r.nextInt(3) == 0) Starts.givePills(to, Item.PillType.Potential, 1, 1); return "a bundle of pills";
            case Chef: { Item f = new Item(Item.ItemType.Food, small, 3, Naming.dish(small, wi)); Character.addToBag(Life.goods(to), f); return "a warm meal"; }
            case Drunken: { Item w = new Item(Item.ItemType.Wine, small, 2, Naming.wine(small, wi)); Character.addToBag(Life.goods(to), w); return "a jug of wine"; }
            case Poisoner: { Item p = new Item(Item.ItemType.Poison, small, 2, Naming.poison(small, wi)); Character.addToBag(Life.goods(to), p); return "a small vial"; }
            case Musician: { Item in = new Item(wi, small, Item.ItemType.Instrument); to.backpack.add(in); return "an instrument"; }
            case Innovative: { Technique t = new Technique(r.nextBoolean() ? Technique.techniqueType.Martial : Technique.techniqueType.Movement, wi, Math.min(100, Math.max(5, (to.majorLvl + 1) * 8 + r.nextInt(10)))); to.techniqueList.add(t); to.equip(); return "a technique (" + t.name + ")"; }
            case Merchant: { long ss = 1000L * (to.majorLvl + 1) * (to.majorLvl + 1); to.ss += ss; to.convertCurrency(); return ss + " spirit stones"; }
            case Gatherer: Character.addToBag(to.materialBag, new Item(Item.ItemType.Herb, small, 600)); return "a sack of herbs";
            case Miner: Character.addToBag(to.materialBag, new Item(Item.ItemType.Metal, small, 600)); return "a lump of metal";
            default: return null;
        }
    }

    static String hintFor(Character h, Random r) {
        Lore.State s = Lore.state;
        Lore.PState p = s == null ? null : s.humans.get(h.id);
        if (s != null && p != null && s.siteX != null && r.nextInt(3) == 0) {
            for (int i = 0; i < LoreSites.ALL.length && i < s.siteX.length; i++)
                if (!p.found.contains(LoreSites.ALL[i].id) && h.majorLvl >= LoreSites.ALL[i].minMajor && p.revealed.contains(LoreSites.ALL[i].id))
                    return LoreSites.ALL[i].title + " lies at [" + s.siteX[i] * 100 + ", " + s.siteY[i] * 100 + "].";
        }
        if (s != null && s.footX != null && r.nextInt(3) == 0) {
            int k = r.nextInt(s.footX.length);
            if (p == null || p.prints == null || !p.prints.contains("foot" + k)) return "A giant's footprint lies at [" + s.footX[k] * 100 + ", " + s.footY[k] * 100 + "]. Something stood there and was pushed back.";
        }
        return HINTS[r.nextInt(HINTS.length)];
    }

    // ------------------------------------------------------------------ the end of the Garden
    /** The last week the game's counter can hold (with 200 years to spare): the Gardener falls and the game saves and ends instead of overflowing. Tests lower it. */
    static volatile int worldEndWeek = Integer.MAX_VALUE - 52 * 200;
    static volatile boolean worldEnded = false;

    /** The world's last week: the Gardener's long fight ends, and the Garden, which lives inside him, goes with him. */
    static void worldEnd() { gardenEnds(null); }

    /** The Gardener is gone (the last week, or `killer` killed him): every human who can cross may step through to the Outside, the rest go out with the world; then the game saves and ends. */
    static void gardenEnds(Character killer) {
        worldEnded = true;
        Lore.State s = Lore.state;
        PlayerCommandsSay(killer == null ? "=== The world has reached its last week. The Gardener is gone, and the Garden goes with him. ===" : "=== The Gardener is dead. The Garden, which lived inside him, goes out. ===");
        if (s != null) {
            s.silenced = true; s.gardenDestroyed = true;
            Chronicle.add("legend", killer == null ? "The world reached its last week. The Gardener fell, and the bell stopped." : "The Gardener was killed. The bell stopped, and the lamps went out.");
            for (java.util.Map.Entry<Integer, Lore.PState> e : new ArrayList<>(s.humans.entrySet())) {
                Character c = Character.charById(e.getKey()); Lore.PState p = e.getValue();
                if (c == null || c.dead || c.player != 1 || c == killer || c.wanderingImmortal) continue;
                lastLamp(c, p, killer != null);
            }
        }
        GameDriver.saveRequested = true;
        GameDriver.quitRequested = true;
    }

    /** Whether a human has done what the Seam asks (level 101, Major Lvl 10, all ten places read): wherever they stand, the Door would open for them. */
    static boolean readyToCross(Character c, Lore.PState p) { return c.majorLvl >= 10 && c.lvl >= 101 && p != null && p.found.size() >= LoreSites.ALL.length + 1; }

    /** One human at the Garden's end: if the Door would open for them (or they have already stood at it) they choose the Outside or the dark; otherwise they go out with the world. */
    private static void lastLamp(Character c, Lore.PState p, boolean slain) {
        boolean can = stayedOf(p) || (!p.ended && readyToCross(c, p));
        Lore.show(c, p, slain ? "end_fade_slain" : "end_fade");
        if (can) {
            Lore.show(c, p, "end_fade_door");
            int a = DecisionBroker.ask(c, "The Seam stands open and the lamps are going out. Step through?", opt("Step through and see the Outside", "Stay with the Garden until the light is gone"), 1);
            if (a == 0) {
                undoStay(c, p);
                outside(c, p, "last_out", new ArrayList<String>(), "After the Last Lamp: ", 0, true);
                return;
            }
        }
        finish(c, p, "last_lamp", "The Light Fades (the Garden's end)", can ? 2500 : 500, true, LoreText.lines("end_dark", Lore.vars(c, p)), null,
                Arrays.asList(can ? "The world did not end. It was simply finished. You are still in it. Please remain seated." : "The Garden went out with all its lamps. You were one of them.", "No lamps were harmed. All of them are out."));
    }

    // ------------------------------------------------------------------ bounties on the enemies of the Steward and the Usurper
    /** Each year a Usurper (the dark path) puts a price on Good cultivators and a Steward on Evil ones: Bounty Hunt quests on city boards that any cultivator, sect members included, may take. */
    static void huntYearly(Lore.State s) {
        boolean hunterEvil = false, hunterGood = false;
        for (java.util.Map.Entry<Integer, Lore.PState> e : s.humans.entrySet()) {
            Character c = Character.charById(e.getKey());
            if (c == null || c.dead || c.player != 1 || !e.getValue().ended) continue;
            if (e.getValue().darkPath) hunterEvil = true;
            if (e.getValue().stewardActive) hunterGood = true;
        }
        if (s.stayers != null) for (Integer side : s.stayers.values()) { if (side > 0) hunterGood = true; else if (side < 0) hunterEvil = true; }
        if (!hunterEvil && !hunterGood) return;
        Random r = GameDriver.rand;
        int total = Map.characters.size(), budget = Math.max(5, Math.min(40, Map.population / 2500)), posted = 0;
        for (int tries = 0; total > 0 && tries < 6000 && posted < budget; tries++) {
            Character t = Map.characters.get(r.nextInt(total));
            if (t == null || t.dead || !t.born || t.player != 0 || t.wanderingImmortal || t.lvl < 5 || t.affiliationNum == null || t.trait == Character.Traits.Beast || t.trait == Character.Traits.Monster) continue;
            boolean good = t.affiliationNum > 0, evil = t.affiliationNum < 0;
            if (!((hunterEvil && good) || (hunterGood && evil))) continue;
            if (t.location == null) continue;
            Area city = t.location.city ? t.location : Life.nearestCity(t.location);
            if (city == null) continue;
            final int id = t.id;
            if (city.bounties.stream().anyMatch(b -> b.targetCharacter == id)) continue;
            city.bounties.add(new Quest("Bounty Hunt", true, 55, 4, 80, t));
            posted++;
        }
        if (posted > 0) Chronicle.add("world", (hunterEvil ? "A Usurper's bounties on the Good" : "") + (hunterEvil && hunterGood ? " and " : "") + (hunterGood ? "a Steward's bounties on the cruel" : "") + ": " + posted + " new prices were posted on the boards this year.");
    }

    // ------------------------------------------------------------------ the Giant Footprints
    static void ensureFeet(Lore.State s) {
        if (s == null || s.footX != null || s.siteX == null) return;
        int[][] f = LoreSites.placeFeet(s.seed, s.siteX, s.siteY);
        s.footX = new int[f.length]; s.footY = new int[f.length];
        for (int i = 0; i < f.length; i++) { s.footX[i] = f[i][0]; s.footY[i] = f[i][1]; }
        for (Area a : new ArrayList<>(Map.createdAreas)) LoreSites.applyFoot(s, a);
    }

    static void footStep(Character c, Lore.PState p) {
        if (c.location == null || c.location.loreSite == null || !c.location.loreSite.startsWith("foot")) return;
        int k;
        try { k = Integer.parseInt(c.location.loreSite.substring(4)); } catch (NumberFormatException e) { return; }
        if (k < 0 || k >= LoreSites.FOOT_NAMES.length) return;
        if (p.prints == null) p.prints = new HashSet<>();
        String id = "foot" + k;
        if (p.prints.contains(id)) return;
        if (c.majorLvl < 3) {
            Integer last = p.glimpsed.get(id);
            if (last == null || GameDriver.time - last >= 26) { p.glimpsed.put(id, GameDriver.time); Lore.say(c, "A footprint, far too large. You cannot read it yet: come back when you have crossed Major Lvl 3."); }
            return;
        }
        p.prints.add(id);
        Random r = GameDriver.rand;
        int lvl = Math.max(1, Math.min(10, c.majorLvl + 2));
        Item relic = new Item(c, Item.WeaponType.values()[r.nextInt(Item.WeaponType.values().length)], lvl);
        relic.enchantment = "the Giant's Echo"; relic.enchantBonus = 3; relic.name = "Relic of the Footprint: " + relic.name;
        c.backpack.add(relic);
        String clue = LoreSites.FOOT_CLUES[k];
        Lore.say(c, LoreSites.FOOT_NAMES[k] + ": " + clue + " (You find " + relic.name + ".)");
        Notifications.post("lore", LoreSites.FOOT_NAMES[k] + ": " + clue, -1, c.id);
        Chronicle.add("world", c.name + " " + c.lastName + " read " + LoreSites.FOOT_NAMES[k] + ".", c.id);
    }

    // ------------------------------------------------------------------ yearly: seekers, the Steward, the world's side
    static void yearly(Lore.State s, Organization pal) {
        ensureFeet(s);
        seekersYearly(s, pal);
        stewardYearly(s, pal);
        stayersYearly(s);
        huntYearly(s);
    }

    /** The NPCs the Convocation let in walk the road in the background: they read the places their realm allows, and the strongest cross. */
    static void seekersYearly(Lore.State s, Organization pal) {
        if (s.seekers == null || s.seekers.isEmpty()) return;
        Random r = GameDriver.rand;
        for (Integer id : new ArrayList<>(s.seekers.keySet())) {
            Character c = Character.charById(id);
            if (c == null || c.dead || c.player != 0 || c.wanderingImmortal) { s.seekers.remove(id); continue; }
            if (c.sect == null || !(c.sect.palace || c.sect.virtual)) { if (r.nextInt(4) == 0) s.seekers.remove(id); continue; } // those who left the Palace give up the road
            int mask = s.seekers.get(id);
            int count = Integer.bitCount(mask);
            if (count < LoreSites.ALL.length && r.nextInt(100) < 35) {
                for (int i = 0; i < LoreSites.ALL.length; i++) {
                    if ((mask & (1 << i)) != 0) continue;
                    LoreSites.Site site = LoreSites.ALL[i];
                    if (c.majorLvl < site.minMajor) break; // the places come in order
                    int prev = 0; for (int j = 0; j < LoreSites.ALL.length; j++) if (LoreSites.ALL[j].tier == site.tier - 1 && (mask & (1 << j)) != 0) prev++;
                    if (site.tier > 1 && prev < site.needN) break;
                    mask |= 1 << i;
                    if (s.firstRead == null) s.firstRead = new HashSet<>();
                    if (s.firstRead.add(site.id)) Chronicle.add("world", c.name + " " + c.lastName + ", a seeker of the Oldest Order, was the first to read " + site.title + ".", c.id);
                    break;
                }
                s.seekers.put(id, mask);
            }
            if (Integer.bitCount(mask) >= LoreSites.ALL.length && c.majorLvl >= 10 && c.lvl >= 101) npcCross(s, c);
        }
    }

    /** An NPC seeker opens the Door and makes the same choices a human does, by their nature: stay (Good, Evil or Neutral), become a lamp-bearer, strike at the Gardener, or walk out alone. */
    static void npcCross(Lore.State s, Character c) {
        Random r = GameDriver.rand;
        String who = c.name + " " + c.lastName;
        int al = c.affiliationNum == null ? 0 : c.affiliationNum;
        int roll = r.nextInt(100);
        String kind;   // soldier, stay, attack, outside
        int allies = SeekerArcs.defectorAllies(s, c); // seekers who swore with this one to strike at the Gardener
        if (al > 0) kind = roll < 45 ? "soldier" : roll < 75 ? "stay" : "outside";
        else if (al < 0) kind = roll < 40 ? "attack" : roll < 65 ? "stay" : roll < 85 ? "outside" : "soldier";
        else kind = roll < 25 ? "soldier" : roll < 55 ? "stay" : "outside";
        if (allies >= 2) kind = "attack";
        if (kind.equals("soldier") && lampsFull(s)) kind = roll % 2 == 0 ? "stay" : "outside";
        String title;
        s.seekers.remove(c.id);
        int score = c.lvl * 10 + c.majorLvl * 50 + c.kills * 2 + 800;
        if (kind.equals("soldier")) {
            title = "a lamp-bearer of the Outside";
            c.wanderingImmortal = true; c.repTitle = "Wandering Immortal"; c.maxAge = 1_000_000;
            leavePalace(c);
            c.targetQuests.clear(); c.spotQuests.clear(); c.currentQuest = null;
            if (s.wanderers == null) s.wanderers = new ArrayList<>();
            s.wanderers.add(c.id);
            bless(c);
            score += 2000;
            Chronicle.add("legend", who + " crossed the Seam and took up a lamp in the Gardener's array (" + lampsCarried(s) + " of " + LAMP_MAX + "). A Wandering Immortal now walks the Garden.", c.id);
        } else if (kind.equals("stay")) {
            int side = al > 0 ? 1 : al < 0 ? -1 : 0;
            title = side > 0 ? "stayed to grow the Garden's soldiers (Good)" : side < 0 ? "stayed and took what the Garden has (Evil)" : "stayed, and lives as they please (Neutral)";
            if (s.stayers == null) s.stayers = new HashMap<>();
            s.stayers.put(c.id, side);
            score += 500;
            Chronicle.add("legend", who + " crossed the Seam, met the Gardener, and came back to " + (side > 0 ? "make the Garden stronger." : side < 0 ? "take what the Garden has." : "live as they please."), c.id);
        } else if (kind.equals("outside")) {
            int k = pickOutside(r);
            title = "left for the Outside: " + OUT_TITLES[k];
            score += OUT_BONUS[k] / 2;
            Chronicle.add("legend", who + " crossed the Seam and went out alone (" + OUT_TITLES[k] + ").", c.id);
            c.deathCause = "left the Garden"; c.reincarnating = true; c.dead();
        } else {
            title = "struck at the Gardener";
            boolean dies = lampsFull(s) || r.nextInt(100) < (allies >= 2 ? 20 : 50); // they went through together: the array has more to count
            Chronicle.add("legend", who + " crossed the Seam and struck at the Gardener" + (allies >= 2 ? " with " + allies + " others who had sworn to" : "") + ". " + (dies ? "The array killed them." : "The array spared them, and they went out."), c.id);
            c.deathCause = dies ? "struck down by the Gardener's array" : "left the Garden";
            if (dies) bloodSacrifice(who); else c.reincarnating = true;
            c.dead();
            score = score / 2;
        }
        s.crossed++;
        s.roll.add(who + " (year " + GameDriver.time / 52 + ", " + title + ", score " + score + ")");
        System.out.println("[Lore] " + who + " has crossed the Seam: " + title + ".");
    }

    /** NPCs who stayed keep the promise their way: a Good one gives gifts, an Evil one culls the Good (and both put prices on their enemies, see huntYearly). */
    static void stayersYearly(Lore.State s) {
        if (s.stayers == null || s.stayers.isEmpty()) return;
        Random r = GameDriver.rand;
        int total = Map.characters.size();
        for (java.util.Map.Entry<Integer, Integer> e : new ArrayList<>(s.stayers.entrySet())) {
            Character c = Character.charById(e.getKey());
            if (c == null || c.dead) { s.stayers.remove(e.getKey()); continue; }
            int side = e.getValue(), done = 0, budget = side > 0 ? 20 : 2;
            if (side == 0 || total == 0) continue;
            for (int tries = 0; tries < 1500 && done < budget; tries++) {
                Character t = Map.characters.get(r.nextInt(total));
                if (t == null || t.dead || !t.born || t.player != 0 || t.wanderingImmortal || t.id == c.id || t.age < 8 || t.trait == Character.Traits.Beast || t.trait == Character.Traits.Monster) continue;
                if (side > 0 && t.majorLvl <= 6) { Starts.givePills(t, Item.PillType.Cultivation, Math.max(1, Math.min(10, t.majorLvl + 1)), 1); done++; }
                else if (side < 0 && t.affiliationNum != null && t.affiliationNum > 0 && t.lvl >= 5) { t.deathCause = "culled by " + c.name + " " + c.lastName + ", who took what the Garden has"; t.dead(c); done++; }
            }
        }
    }

    /** The Steward keeps the promise: gifts for the weak, a cull of the cruel, bigger prizes (only while a human Steward is the Palace's master). */
    static void stewardYearly(Lore.State s, Organization pal) {
        Character m = pal.master;
        if (m == null || m.dead || m.player != 1) return;
        Lore.PState p = s.humans.get(m.id);
        if (p == null || !p.stewardActive) return;
        Random r = GameDriver.rand;
        int total = Map.characters.size(), gifts = 0, culled = 0, budgetGifts = 200, budgetCull = Math.max(5, Math.min(40, Map.population / 3000));
        for (int tries = 0; total > 0 && tries < 4000 && (gifts < budgetGifts || culled < budgetCull); tries++) {
            Character c = Map.characters.get(r.nextInt(total));
            if (c == null || c.dead || !c.born || c.player != 0 || c.trait == Character.Traits.Beast || c.trait == Character.Traits.Monster || c.wanderingImmortal || c.age < 8) continue;
            if (c.affiliationNum != null && c.affiliationNum < 0 && c.kills >= 3 && culled < budgetCull) {
                c.deathCause = "culled by the Steward's Elders"; c.dead(); culled++;
            } else if (gifts < budgetGifts && c.majorLvl <= 6 && c.sect != pal) {
                Starts.givePills(c, Item.PillType.Cultivation, Math.max(1, Math.min(10, c.majorLvl + 1)), 1);
                if (gifts % 5 == 0) Starts.givePills(c, Item.PillType.Potential, 1, 1);
                gifts++;
            }
        }
        for (Character mem : new ArrayList<>(pal.members)) if (mem != null && !mem.dead && mem.player == 0) { for (int i = 0; i < 3; i++) mem.cultivate(); } // the Palace trains harder under a master who cares
        Chronicle.add("world", "The Steward " + m.name + " " + m.lastName + " of the Palace of the Oldest Order gave " + gifts + " gifts and had " + culled + " cruel cultivators culled this year.", m.id);
    }
}
