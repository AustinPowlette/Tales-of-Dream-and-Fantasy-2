package Game;

/** The Disgraced Disciple, seasons 2 and 3 (see "Start Stories Beat Sheets.md").
 *  Season 2, "The Pillar Fund" (a whodunit with fair clues and court intrigue): someone outside the Pavilion still pays the Pillar Fund, and the money still vanishes. The Concord of Healers (a cartel that sells cures)
 *  funds the school so that it teaches helplessness; the scroll teaches the opposite. One of three people inside the Pavilion is the Concord's hand (flag hand: 1 the Registrar Ibbot, 2 Captain Renn of the Gate,
 *  3 the Librarian Sunny, rolled when the season begins). Mei (always of the other gender than the human) is in it from the first page. Flags: door (season 1), hand, verdict, venn, love2.
 *  Season 3, "The Last Page" (metaphysical): whoever left the scroll was a guest, Madam Gloam, one of the Hollow Court's gentle hands; she taught Lan and Zhao and the human the same thing from three sides, and the scroll's
 *  last line ("breathing out, you set it down") is how a person sets the whole Garden down. The Wandering Drunken Immortal passes by with a red gourd. Flags: guest (the offer), lastpage (what was written on it). */
final class TaleDisgracedSeasons {
    private TaleDisgracedSeasons() { }

    static void add(Chapters.Def d) {
        d.flags("hand", "verdict", "venn", "love2", "guest", "lastpage");
        d.place("concord", "far").place("inn", "near");
        d.cast("ibbot", "Registrar", "Ibbot").he().keep().trait(Character.Traits.Scribe).side("Neutral").lvl(2).age(58).at("place:pavilion").rel(0);
        d.cast("renn", "Captain", "Renn").she().keep().trait(Character.Traits.Leader).side("Neutral").lvl(4).age(41).at("place:pavilion").rel(0);
        d.cast("sunny", "Sunny", "Aldous").he().keep().trait(Character.Traits.Scribe).side("Neutral").lvl(1).age(33).at("place:pavilion").rel(1);
        d.cast("venn", "Matron", "Venn").she().trait(Character.Traits.Merchant).side("Evil").lvl(5).pot(7).age(62).at("place:concord").rel(-1);
        d.cast("calder", "Lord Auditor", "Calder").he().trait(Character.Traits.Leader).side("Evil").lvl(8).pot(9).age(49).at("place:concord").rel(-3);

        // ================================================================== SEASON 2
        d.season(2, "The Pillar Fund");

        d.chapter("invite", "A Letter About Money").years(3)
                .setup((c, st) -> { if (Chapters.flag(st, "hand") == 0) Chapters.set(st, "hand", 1 + GameDriver.rand.nextInt(3)); Chapters.spawn(c, st, "ibbot"); Chapters.spawn(c, st, "renn"); Chapters.spawn(c, st, "sunny"); })
                .intro(
                        "?door=1|* The Pavilion goes on without you, as you meant it to. For three years it is a comfort to know that scrolls are being copied in a hundred villages, and that people who have never met you are mending their own wrists. Then a letter arrives, in the cramped hand of the Registrar, and it is about money.",
                        "?door=2|* The Elder's chair is as hard as ever. You have learned to sit in it with your feet tucked up like a child on a bench. You are passing the third winter in the Stillwater Pavilion when the Registrar brings you the quarterly figures, and puts them down carefully, as if they were a dish of something that might spill.",
                        "?door=3|* The Closed Stacks are ash and the school that grew from the ash is housed in a barn. It has a great many students, a terrible roof, and a library of eleven books, of which three are cookery. The Registrar finds you there in the autumn with a ledger under his arm and a face like a man who has been up all night with a headache.",
                        "@Ibbot| It is the Pillar Fund. I do apologise. I had hoped the matter was closed. For forty years the Fund has paid ten thousand spirit stones a year to the school's poor students, and for twenty of those years Elder Zhao took four tenths of it, as you know. When he was gone, the Fund went on. The money went on arriving. And it went on going.",
                        "@Ibbot| I have checked it six times. I am not a person who makes mistakes with figures. Somebody is still taking it. I should be grateful if you would come and look. You are the only person I could think of who is good at being angry in a useful direction.")
                .at("pavilion", "Go to the Stillwater Pavilion, at {@pavilion}, and audit the Pillar Fund.")
                .outro(
                        "* The Pavilion is smaller than you remember. The courtyard where you were expelled is a vegetable garden. The reading hall has a new roof, and a cat on it. The gate where Mei once stood with a bundle of rice cakes is the same gate, with the same worn flagstone, and you stop on the flagstone for a moment before you go through.",
                        "* {mei.first} is in the corridor. {mei.He} is carrying a stack of scrolls up to {mei.his} chin. You know the walk before you see the face: the long stride that is trying to be a short one, the habit of looking at the floor three paces ahead. {mei.He} puts the whole stack down on the floor.",
                        "@Mei| You took your time.",
                        "@You| So did you. There were sweets in the satchel. I never thanked you.",
                        "@Mei| You thanked me for eleven minutes at the inn. I counted. Come here.",
                        "* The embrace is clumsy and somewhat damaged by the scrolls. Somewhere behind you, in the corridor, the Registrar clears his throat in a way that is almost, but not quite, a cough.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("ledger", "The Ledger of Poor Students").gap(2)
                .intro(
                        "* The Fund's books are kept in a room behind the Registrar's office, in a cabinet of dark wood that smells of camphor and lemon oil. They are very good books. They are written in a fine, even, upright hand, in black ink, with every entry ruled off, and every column carried forward.",
                        "* You spend three days with them. You eat there, on a tray, with a pencil behind your ear. You find that you are happy, in the way of a person who has been given a good puzzle and a quiet room, and you feel faintly guilty about it, and carry on.")
                .wait(2)
                .outro(
                        "* On the fourth day you find the first thing. Student number forty-one. A boy called Pell Winterbourne, admitted on the Fund at the age of nine, who has received a stipend every quarter for fourteen years. His name is in the ledger, and his signature, in a careful child's hand, and his receipt for every quarter's money.",
                        "@Mei| I'll fetch the roll.",
                        "* {mei.He} is back in ten minutes with the school roll. Pell Winterbourne appears on it for one year only, aged nine. Beside the entry, in red, is a note: Died of fever, third winter. The stipend goes on. The signature goes on. Somebody has been signing it as the boy grew up, in a hand that gets steadily firmer, and rounder, and more adult, like a child learning to grow.",
                        "@Mei| He'd be twenty-three.",
                        "@You| Yes.",
                        "@Mei| Somebody has been giving him birthdays.",
                        "* You find three more before the week is out. A girl who left to marry. A boy who ran off to sea. A third who has never, as far as the roll is concerned, existed at all. A small, quiet fraud, tended with great skill and love, like the garden of a person who has more time than they know what to do with. You cannot tell whom to hate. You want to find out before you decide.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("desks", "Three Desks").gap(2)
                .intro(
                        "* There are three people in the Pavilion who could have kept those books. You have been through the numbers, and the numbers are clear. The hand is a clerk's, which means Ibbot, or a librarian's, which means Sunny. Or it is a very good forger's, which could be anyone who has spent twenty years reading the Registrar's accounts, including a captain of the gate with time on her hands and a neat way with a brush.",
                        "* You accuse no one. You ask for tea. It turns out that people will tell a good deal to someone who has asked politely for tea.")
                .meet("ibbot", "Take tea with Registrar Ibbot, Captain Renn, and the Librarian Sunny, one after another.")
                .outro(
                        "* The Registrar's office is the cleanest room in the Pavilion. He pours with the care of a man handling a live animal. He tells you he has not taken a coin from the Fund in his life. This is, so far as you can tell, true. After a very long pause he tells you that he has a sister in the east who is ill. For six years he has sent her sixty crowns a quarter, from a source he declines to name, and which he does not believe to be the Fund.",
                        "@Ibbot| I do not like the look on your face. I wish to say that I know exactly how it sounds.",
                        "* Captain Renn gives you tea on the wall above the gate, because she does not care for rooms. She is lean and brown and scarred along the jaw, and she talks about the guard rotas for twenty minutes with such pleasure that you almost forget the question. Then she says, flatly, that she took a bribe last year to look the other way for a night. She does not know what was carried through the gate. She does not want to know. She would like to be sacked.",
                        "* Sunny Aldous gives you tea in the library, among the stacks, on a stool. He is a round-faced young man with an engaging smile and a habit of sitting on his own hands. He says he has never been so happy anywhere. He says the Pavilion is the first place that ever let him read what he wanted. Then, when you ask about the ledgers, he says without any change in his tone that he does not understand why anyone should object to a gift, and that he is sorry if that is a stupid thing to say.",
                        "* Three cups, three stories, three kinds of honest. You go back to the Fund room and sit with the ledger open on your knee until the lamp wants trimming, trying to decide which of them is lying with their whole face. The answer has been in front of you the whole time, in something all three of them said, in almost the same words, as you were leaving. 'It's not for me. It's for someone else.'")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("gilt", "A Gilt-Edged Card").gap(6)
                .intro(
                        "* The card comes on the sixth day, hand-delivered by a footman in a green coat, on a salver. It is heavy and cream-coloured, with a gold edge and an engraved crest of a hand holding a flask. 'The Matron of the Concord of Healers requests the pleasure.' Under this, in a small neat hand: 'We have been wondering when you would come.'",
                        "@Mei| The Concord. The apothecaries' cartel. They sell every cure in the province, at a price. They own half the physicians and most of the magistrates.",
                        "@You| And they fund the Pillar Fund?",
                        "@Mei| Under another name. The Lantern Trust. I've seen it in the donors' book, in the margin. I didn't know whose it was till now. It all begins to look like a very polite war.")
                .at("concord", "Go to the Concord of Healers' hall at {@concord}.")
                .outro(
                        "* The hall of the Concord is a handsome building of white stone and green glass, set in a garden of medicinal herbs, with a clinic on one side and a counting-house on the other. There are two queues at the clinic door: a short one for those who can pay, and a long one for those who cannot. The second has no one in charge of it, and moves by itself.",
                        "* The footman takes you through the garden. He is a pleasant, indifferent man with a bad knee. He tells you, without being asked, that the roses at the south wall are for coughs, the lavender for sleep, and the valerian for anxiety. The Matron likes to have each of the three on her table, in a small glass, every day, 'for balance'.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("dinner", "Dinner at the Concord").gap(1)
                .setup((c, st) -> { Chapters.spawn(c, st, "venn"); Chapters.spawn(c, st, "calder"); })
                .intro(
                        "* The Matron sits at the head of a table laid for six, in a room that has a ceiling like the inside of a seashell. She is sixty-two, silver-haired, in a plain grey gown with a high collar, and she gets up when you come in, which is a courtesy she does not extend to everyone. Her hands, you notice, are the hands of a woman who has done a great deal of work with them, and has chosen not to stop.",
                        "@Venn| You are the Disgraced Disciple. I have wanted to meet you for twelve years. I had a very great deal of money riding on you not learning to read. I am, I confess, rather delighted that you did.",
                        "* On her right is a tall, stiff man in a black coat, with a face like a closed door, who is introduced as the Lord Auditor Calder. He does not speak. He cuts his meat into nine equal pieces, and eats them in order.")
                .meet("venn", "Dine with Matron Venn of the Concord of Healers.")
                .outChoice(new Chapters.Choice("venn", "Matron Venn has put her hands flat on the table. Dessert is on its way.",
                        "Refuse everything: the Fund is a theft, and you will say so in public.", "Hear her out, and bargain: you will keep quiet if the Fund pays the right people.", "Accept: take the Concord's money for the school, and teach the scroll in your own time.")
                        .after(
                                new String[]{"@Venn| Quite right. I should have thought less of you if you had not. But I'd like you to know that this is not personal. It is accounts.", "* She says it with a gentle sort of regret, like a woman closing a book she is fond of."},
                                new String[]{"@Venn| Now we are being sensible. You will find that I am very good at being bargained with.", "* She slides a paper across the cloth. It is a draft, in clean ink, and it has sixty-three clauses, and you read them all, slowly, while the cheese gets warm."},
                                new String[]{"@Venn| I did hope you would be realistic.", "* She smiles, showing very good teeth, and it makes you feel as if you have walked into a room where the temperature has been carefully arranged."}))
                .outro(
                        "@Venn| I'll tell you what I believe, since you have come all this way. I believe in medicine. I believe that the sick are better off with a good cure at a fair price than with a miracle and a tragedy. I also believe that a person who can mend themselves is a person who has no use for me. You understand that. You are not a stupid man. The scroll you read is going to put my apothecaries out of work.",
                        "@You| It's going to put them out of work, and it's going to save a great many people.",
                        "@Venn| Yes. That is the trouble. I have never yet been able to decide which I mind more. I only know that I have employed a great many people, and that they have children.",
                        "* You find, to your own discomfort, that you like her. She is not a villain. She is an economy in a gown. On your way out, in the corridor, the Lord Auditor Calder falls into step beside you and says, without turning his head, in a voice like a dry hinge: 'When she says it is accounts, it is accounts. When I say it, it is something else.'")
                .pay(2000, 0, 0, 1, 0, 0);

        d.chapter("mclass", "Mei Teaches the First Breath").gap(2)
                .intro(
                        "* The junior class meets in the old gymnasium behind the reading hall. It has a floor of sprung boards and a roof with a hole in it, through which the sun comes down in a hard bright pillar and the pigeons come in for lunch. There are thirty children. They are lined up in four rows, according to height, in grey shirts, with their hands behind their backs and their eyes on the ceiling.",
                        "* Mei stands in front of them. {mei.He} has been a teacher for six months and looks, you think, a good deal more nervous than the pupils. {mei.He} has a bell in one hand and a wooden tablet in the other, and {mei.his} hair is tied back with what appears to be a piece of string. You stand at the back, behind a pillar, and do not breathe.")
                .wait(2)
                .outro(
                        "@Mei| The first breath is the easiest thing you will ever learn and the hardest to do. I want you to breathe in. Not as much as you can. As much as you need. Now let it out. That's it. That is the first breath. The rest of your life is a kind of commentary.",
                        "* A small boy in the second row puts up his hand. He wants to know what happens if you get it wrong. {mei.He} considers this seriously, for a moment, and says: 'You try again.' The boy thinks about it, and says that it doesn't seem very much of a lesson. Mei says, evenly, that it is the entire lesson, and that the rest of it is only practice.",
                        "* They breathe. For ten minutes thirty children stand with their eyes shut and their stomachs going out and in, in a hall full of pigeons and light. The ones at the back start to giggle. The ones at the front hold their faces tight, like people trying to remember something. A girl in the third row, who has been crying since she came in, stops.",
                        "* Afterwards, in the empty hall, {mei.he} comes to you with the string in {mei.his} hand and the look of someone who has just got off a ship. 'I thought I'd die,' {mei.he} says. 'I counted. It was two hundred and eleven times I nearly ran for the door.' And you say that you counted too, from behind the pillar, and that it was only a hundred and seven. 'Show-off,' says {mei.he}, and throws the string at you.")
                .pay(200, 0, 0, 0, 0, 0);

        d.chapter("second", "The Second Ledger").gap(2)
                .intro(
                        "* You come back to the Pavilion with the taste of the Concord's cheese in your mouth and a draft contract in your sleeve. The Fund room is as you left it. The light slants through the high window. Mei is sitting on the floor with her back to the cabinet and a bowl of rice on her knee.",
                        "@Mei| I found it. Behind the camphor drawer. There's a false back. I didn't want to open it without you. I'm learning to be careful.",
                        "* It is a plain notebook, grey, with a cloth cover. There are no entries in it for the Fund. There is only a list of names, each with a sum and a date, and beside each name, in a small tidy hand, a single word: faithful, or wavering, or doubtful, or new. At the top of the first page is a title: THOSE WHO ARE KIND.")
                .wait(1)
                .outro(
                        "?hand=1|* Registrar Ibbot is on the list. Beside his name is 'faithful', and a sum, and the words 'for the sister'. He had not known the money came from the Concord. He had thought it was a pension from his old school.",
                        "?hand=2|* Captain Renn is on the list. Beside her name is 'wavering', and a small sum, and the words 'one night'. Beside it, in another hand, is a note: she may be made useful. She does not know she has been written down.",
                        "?hand=3|* Sunny Aldous is on the list. Beside his name is 'new', and a large sum, and the words 'for the library'. He has been paid, regularly and generously, for three years, to be happy. He had thought it was a gift.",
                        "* You close the notebook. Three names, and two others you do not know. One of the three is the hand that has been keeping the books honest, and the other two are only the hands that have been used. It takes you most of the night to be sure which, and the answer, when it comes, is the one that you were afraid it would be.",
                        "@Mei| It's the one who writes the cats, isn't it? The small neat hand.",
                        "@You| Mei draws the cats.",
                        "@Mei| I know. I wanted to see your face.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("zhaoletter", "A Letter from Zhao").gap(1)
                .intro(
                        "?dead.zhao|* It is found in the sealed drawer of the Elder's chair, by the boy who sweeps the study. It has been there for three years. It is written on a sheet of plain paper in a firm, narrow, upright hand, and it has no name on the outside. It is for you. You know it by the way it is folded, in four, with the fold pressed twice.",
                        "?!dead.zhao|* It comes by the hand of a boy from the school, who brings it from the hut at the edge of the grounds where the old Elder lives. It is written on a sheet of plain paper in a firm, narrow, upright hand. It has your name on the outside. The boy says that the Elder wrote it with the shutters closed and the lamp lit at noon.")
                .wait(1)
                .outro(
                        "* 'I have been trying for some weeks to write a letter and have written eleven, and burned them. This is the twelfth. I will not apologise. It would be a lie of a new kind. I will tell you only what I know. I did not take the money for myself. I took it because the Pavilion was failing, and the Concord offered, and when I asked what the price was, they said: only that the school teach no one to mend themselves. It sounded small.'",
                        "* 'I want you to understand that I did believe it. I thought: a school that cannot heal is a school that needs its masters. I had been a poor boy once, and needed a master, and I thought what I was doing was keeping a door open. I did not know that a door which is kept open by a lack is a trap. You taught me that. You, with your question in the third year, in front of the whole class, in that terrible calm voice.'",
                        "* 'When you came to me in the courtyard I was relieved. I want you to know. It was the first time in thirty years that someone had looked at me and seen the whole of it. I could not say so. I was too proud. But I remember I thought: at last.'",
                        "* 'I have one request, and it is not for me. There is a boy in the third form whose name is Pell, and he cannot read. He is a bright child. Nobody has bothered. I should be obliged if you would see to it. That is all. Zhao.'",
                        "* You fold it in four. You press the fold twice, with your thumbnail, the way he did. You find Pell in the kitchen, on a stool, peeling an onion. He is nine. He has the stubborn silent face of a child who has been told he is slow. You sit down beside him and say, 'Would you like to learn something?' And he looks at you, and the onion, and says: 'Is it hard?'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("hand", "The Hand").gap(1)
                .intro(
                        "?hand=1|* You find him in the Fund room at dawn, as you found Zhao. He is not hiding. He has the ledger open and a clean pen. He does not look up. 'I wondered which of us you would think of first,' he says. 'I had hoped it would be the Captain.'",
                        "?hand=1|@Ibbot| I did not take a coin for myself. I took it for the dead. I made them birthdays. Pell Winterbourne turns twenty-four in the spring. I have bought him a coat. It is in my drawer, folded, and I do not know what to do with it. I could not bear that he should be struck off a list because he was dead. It seemed a very great waste of a name.",
                        "?hand=2|* You find her on the wall above the gate, in the first light, with her sword across her knees. She does not turn. 'I'm glad it's you,' she says. 'I couldn't have stood a stranger.'",
                        "?hand=2|@Renn| I did look the other way, the one night. And then it was every month. They never asked me for anything wicked. They asked me to look. I kept the books honest for them because I'm the only one who could. I told myself it was keeping the Pavilion safe. The Concord said it was keeping the Pavilion quiet. I never saw the difference.",
                        "?hand=3|* You find him in the library, on the stool, among the stacks. He has a book open on his knee, and it is upside down, and he is not reading it. He stands when you come in. He is smiling, in a way that is entirely wrong for the face.",
                        "?hand=3|@Sunny| I was going to tell you. I really was. I wanted you to find out for yourself, and then I thought you wouldn't, and then I thought you would. It's very odd, being found out. It's rather like being read.",
                        "?hand=3|@Sunny| They said it was a patron. That a lady who loved books wanted to leave a gift to the Pavilion, quietly. I have never in my life been given a gift. I did not know that one could be paid to be a certain kind of person.")
                .choice(new Chapters.Choice("verdict", "The Concord's hand is standing in front of you, and has told you everything.",
                        "Expose them: put the whole thing before the school.", "Forgive them, and keep them, and let them run the Fund honestly from now on.", "Use them: let them stay the Concord's friend, and feed the Concord what you choose.")
                        .after(
                                new String[]{"* You tell the whole school in the courtyard, at dusk. It takes forty minutes. At the end there is a silence, and in the silence the old one's voice, steady, saying: 'I am sorry.' Nobody answers. After a while someone brings a lamp."},
                                new String[]{"* You say it plainly: they will not be thrown out. They will work. They will sign the Fund's books in front of the school every quarter and read out the sums. They agree, and it is the hardest thing you have ever asked of anyone."},
                                new String[]{"* It is the least comfortable plan you have ever made. It requires you to be, for a year, a good liar to a bad cause. You find that you are better at it than you would like, and you tell Mei so, and she tells you not to enjoy it."}))
                .wait(1)
                .outro(
                        "* There is a moment, afterwards, in the corridor, in the grey light, when you find yourself standing beside Mei at the window, looking at the garden. Neither of you says anything. The garden is full of the Concord's herbs, which the Pavilion has been growing for three years without knowing why, and they are in flower.")
                .pay(0, 0, 0, 1, 0, 0);

        d.chapter("calder", "The Lord Auditor").gap(2)
                .intro(
                        "* The Lord Auditor Calder comes to the Pavilion on a clear morning, alone, on foot, in the black coat. He walks in at the gate without being announced. He stops in the middle of the courtyard, which is a vegetable garden, and looks at the cabbages as though they had offended him personally.",
                        "@Calder| The Matron has withdrawn her kindness. She asked me to inform you. I do not wish to be a bad guest, but I have also been asked to collect, on behalf of the Concord, a number of things that are not yours. The books. The Fund. The name. Your friend with the cat on her wrist.",
                        "@Mei| I'm not your friend, I'm his. I mean, yours. I mean, you know what I mean.",
                        "@Calder| I do. It is the clumsiest piece of defiance I have ever heard. I want you to know I admire it. It makes this harder.")
                .defeat("calder", "Face the Lord Auditor Calder in the Pavilion's courtyard.")
                .outro(
                        "?dead.calder|* He falls among the cabbages, with a clean, tidy, dry sound, and lies there. The students, who have been watching from the windows, do not cheer. One of them, a small boy, crosses the garden with a watering can and sets it down beside the body, for no reason that he can explain.",
                        "?!dead.calder|* He goes down on one knee in the cabbages and stays there. 'That was extremely irregular,' he says, in his dry hinge voice. Then he gets up, and brushes his coat, and bows to the whole of the courtyard in turn. He leaves by the gate, and does not look back. A cabbage has attached itself to his sleeve. Nobody tells him.",
                        "* On the gate, a few minutes later, you find a sealed note. It is from the Matron. It says: 'I withdraw the matter of the Fund. I would be glad to dine again. I am sorry about the cheese.'")
                .pay(5000, 0, 0, 1, 1, 1);

        d.chapter("rice", "Rice Cakes, Again").gap(6)
                .intro(
                        "* There is a bag of rice cakes on the table in the Fund room, tied with a blue string, with a note on it that says in neat capitals: STALE, ON PURPOSE. You eat none of them. You put the whole bag in the drawer of the Registrar's desk, where it will be found and misunderstood, and you go and find Mei.",
                        "* {mei.He} is up on the roof of the reading hall, where the cat lives, with a book that {mei.he} is not reading. It is a clear evening. The whole valley lies below, in the amber light, with the smoke going up straight from the village chimneys, and the river lying in a loop like a dropped thread.")
                .wait(4)
                .outChoice(new Chapters.Choice("love2", "The cat is on {mei.his} lap. The light is going. You have been asked nothing, and you have something to say.",
                        "Tell {mei.him} how you feel, in as few words as you can.", "Say nothing, and sit down next to {mei.him}, and let the evening do it.", "Say you are not ready, but you are not going anywhere.")
                        .after(
                                new String[]{"@Mei| That took you a lot less time than I thought. I had a speech for the other thing, and now I've nowhere to put it.", "@You| Put it here.", "* {mei.He} does. It is a long speech, and none of it is the kind that you would hear at a school."},
                                new String[]{"* The cat moves from {mei.his} lap to yours. {mei.He} laughs. The sun goes down. Somewhere below, a bell is rung for supper, and neither of you moves, and the bell rings a second time, and a third."},
                                new String[]{"@Mei| That's a good answer. I'll hold you to the second part.", "* {mei.He} puts {mei.his} head on your shoulder for a minute, and then, brusquely, takes it away. It is, you gather, a promissory note."}))
                .outro(
                        "?love2=1|* The school is told in the course of the following week, in the way that schools are told, which is that everyone knew already. The Registrar, with enormous dignity, pretends to be surprised.",
                        "?love2=2|* You stay on the roof until it is dark. The cat falls asleep across both of you. It is a lovely, wasteful, undeserved evening.",
                        "?love2=3|* The year passes. It is not a hard year. It has a promise in it, and a sleeve with a cat drawn on the cuff.")
                .done((c, st) -> { if (Chapters.flag(st, "love2") == 1) { Chapters.bond(c, st, "mei", 3); Chapters.wed(c, st, "mei"); } })
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("clean", "A Clean Ledger").gap(6)
                .intro(
                        "* The Fund is audited again in the spring, by the school, in the open, with Pell Winterbourne's coat laid on the table in the middle, folded. The roll is corrected. The dead are crossed off, with a candle and a few words. The sums add up. They have never been so plain.",
                        "?venn=1|* The Concord withdraws from the Pavilion. In its place, the school's own alumni raise a Fund of their own, which is smaller and more honest and is paid in rice.",
                        "?venn=2|* The bargain holds. The Concord pays, and the Pavilion teaches, and both sides watch the other like cats across a wall. It is not peace. It is a very tidy kind of cold war.",
                        "?venn=3|* The Concord's money goes on arriving. The scroll goes on being taught, in the evenings, in the open air. Nobody quite knows whether to call that a victory. Mei calls it 'the long way round'.")
                .wait(2)
                .outro(
                        "?verdict=1|* The one who was exposed leaves in the autumn, by the east road, and is not seen for years. A letter arrives every spring, in the same small tidy hand. It does not ask forgiveness. It reports the sums.",
                        "?verdict=2|* The one who was forgiven reads out the Fund's books each quarter to the whole school, standing on a box. The students make a game of heckling. It is the gentlest humiliation you have ever seen, and it works.",
                        "?verdict=3|* The one who was used writes to the Concord each month, in the usual hand, with the news that you have chosen. You read every letter before it goes. Some of them are very funny. One is, you think, the best-written lie you ever read.",
                        "* You close the ledger. Beside it, on the table, a candle burns down for Pell Winterbourne, who is twenty-four, and who has been alive for fourteen years longer than anyone should have been.")
                .pay(5000, 0, 0, 2, 0, 1);

        d.interlude(2, "The Grey-Green Hand",
                "* The Pillar Fund pays ten thousand spirit stones a year to the poor students of the school, and nine thousand nine hundred of them now arrive in the hands of the poor students. The rest is set aside for a coat.",
                "?love2=1|* Mei teaches the junior class and does the accounts, and has been heard to say that the school needs more cats.",
                "?love2=2|* Mei teaches the junior class, and is kind about the question that is always on the roof between the two of you. It sits there, and is fed, and is not hurried.",
                "?love2=3|* Mei teaches the junior class, and writes to you every month, and the cat on the cuff of the letter has begun, lately, to wear a very small hat.",
                "* One evening, going through the post, you find the third envelope in two years that is addressed in a grey-green hand. It contains a single leaf of paper, folded in four. On it, in pale ink, are the words: AND NOW THE LAST PAGE. The rest is blank.");

        // ================================================================== SEASON 3
        d.season(3, "The Last Page");
        d.cast("gloam", "Madam", "Gloam").she().trait(Character.Traits.Scribe).side("Evil").lvl(9).pot(10).age(70).at("here").rel(0);

        d.chapter("gate3", "Tea at the Gate").years(3)
                .setup((c, st) -> Chapters.spawn(c, st, "gloam"))
                .intro(
                        "* She is standing at the Pavilion gate when you come back from market, in a gown the colour of the underside of a leaf. She is tall, and thin, and pale, with the long still hands of a woman who has never in her life been in a hurry, and her hair is the grey-green of a pond in autumn. She has a tea set on the wall beside her, on a tray, with two cups. She has been, you realise, there a little while.",
                        "@Gloam| Madam Gloam. I do hope this is not an intrusion. I was passing, and it occurred to me that I had been rather rude. One should thank the people one has been writing to. Will you have a cup?")
                .meet("gloam", "Take tea with the woman at the gate.")
                .outro(
                        "* The tea is excellent. It is, you think, the best tea you have tasted, though you cannot recall drinking it. You have the odd sense of having been given back something that was always yours.",
                        "@Gloam| I left you the scroll. I wanted to say so in person. I left it in a number of hands, you see, over a number of years. I did hope it would find someone patient. Nobody else got further than the third line. You got to the end.",
                        "* She says it with a kind of pride, like a woman who has set a hard puzzle and watched someone solve it. You do not like how much it pleases you.",
                        "@Gloam| I should like to tell you the story. It is not a long one. May I?")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("page", "The Blank Page").gap(1)
                .intro(
                        "* She takes the folded leaf from her sleeve. It is the one that came in the post. She flattens it on the wall, beside the teapot, and the pale ink writes itself across the paper as you watch: not words you have seen, but a column of small spiralling marks, like the track of an insect.",
                        "@Gloam| This is the last page of the scroll. It has never been written, because it cannot be. It is for the reader to write. The scroll is not a technique. It is a door that is left ajar. I left it where a person might find it: a dying boy's desk, a cruel man's study, a place where it would be welcome.")
                .wait(1)
                .outro(
                        "?how=1|@Gloam| I whispered to Elder Zhao that a boy who asked questions would be a danger. Not a lie: a boy who asks questions is. He put the scroll in your desk, to ruin you, and thereby gave it to you. I had not even to ask him. A man who is afraid will do the work for you.",
                        "?how=2|@Gloam| I whispered to Lan, on the fourth night, that there was a desk by the stove where it was warm. He was dying, and he believed it was his own idea. He loved you, in his way. He put the scroll where it would be safe from the cold. I did not even have to ask him. A boy who is dying will do the kindest thing he can think of.",
                        "?how=3|@Gloam| I left it in your desk myself. I have not told that to a living soul. The night was wet. It was not a hard thing to do. I walked through the Closed Stacks in the rain with the scroll under my sleeve, and the doors opened. They always do. It is a talent I have.",
                        "* She sets her cup down. You see that her fingers, as she does it, are very slightly blue.",
                        "@Gloam| I should tell you what I am. It will save a great deal of tea. I belong to those who put out lamps. Not the loud ones. There are loud ones, and they have no taste. I am one of the quiet. I teach people, one at a time, in the kindest way I know, to set down the world.")
                .pay(0, 0, 0, 1, 0, 1);

        d.chapter("origin", "Three Places").gap(2)
                .intro(
                        "* You go back to where it began, because the Guest has told you the story, and you have never in your life believed a story without walking the ground. There are three places, depending on how you tell it. You find that you have to go to all of them.")
                .wait(2)
                .outro(
                        "?how=1|* The first is the Elder's study, in the corridor off the great hall, which has been shut since the Elder was removed. It is clean. Someone has dusted it. A window is open. On the desk there is a little brass key, and a sheet of paper with a single line in his upright hand: IT SEEMED A SMALL THING. You sit in his chair. It fits you better than you would like.",
                        "?how=2|* The first is the pear tree behind the infirmary, where there is a small grave with a plain wooden marker. Lan. Someone has kept it weeded. There are fresh pears in a bowl at the foot of it, small, hard, and green, and a jar with a flower. You kneel. You tell him how it came out, in a low voice, in the order that it happened. You do not leave anything out. At the end you find that you are saying thank you.",
                        "?how=3|* The first is the Closed Stacks, or what is left of them: a square of blackened flagstones under the sky, where the doors stood. Nobody planted it. Grass has come up between the cracks, long and fine, in the shape of a hall. You walk its length, as you must have walked once, in the dark, with a scroll under your sleeve you did not know you carried.",
                        "* The second is your own desk, in the old reading hall, which the school has kept exactly as it was. The lid still has your initials cut into the corner. You lift it. Inside, at the bottom, on the old ink-stain, there is nothing at all. But the wood is warm, the way a seat is warm where somebody has just got up, and you stand with your hand on the lid and feel, very clearly, the shape of an absence that is also a gift.",
                        "* The third is the gate, where Mei stood, on the worn flagstone, on the day of the expulsion. You stand on it and look down the road. It is a grey road, going round a hill. A boy of eleven comes along it with a bundle. He is expelled, or leaving, or on an errand. He stops before you and looks up with enormous grave eyes and asks if this is the Stillwater Pavilion. You say that it is. He asks if they have a library. You say they have a very good one.",
                        "@Boy| Is there a part you're not allowed in?",
                        "@You| There used to be.",
                        "@Boy| Good. I'll start with that.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("others", "Who Else Got a Page?").gap(2)
                .intro(
                        "* It is Mei who thinks of it. {mei.He} comes into the reading hall at dawn with a map under {mei.his} arm and the fixed pale look of someone who has been counting. 'You are not the only one,' {mei.he} says. 'She said so herself. She left it in a number of hands. Where are the others?'",
                        "* It takes a month, and it is more pleasant than it should be. You find them in ones and twos, in villages the map has no names for, by asking in inns about a grey-green gown, and a cup of tea that one is not sure one drank.")
                .wait(2)
                .outro(
                        "* A baker, in a river town, who found a leaf in a loaf. A road-mender who found one in the lining of his coat. A girl of fifteen who found it pinned to her pillow. None of them kept the leaf. None of them could say what it said. All of them, when you ask, smile at once, a wide slow smile that arrives before the question has finished.",
                        "* They are, without exception, the happiest people you have met. They are quiet and kind and extremely easy company. The baker's bread is the best you have eaten. The road-mender's road has no ruts in it. The girl has been, her mother says, a joy since the day it came: never cross, never sad, never frightened.",
                        "@Mei| Look at them. Look at where they're standing.",
                        "* You look. The baker is standing a hand's breadth to one side of his own shadow. The road-mender, who has not stopped smiling, has not moved his eyes from a point somewhere beyond your shoulder for the whole of the conversation. The girl is half a pace to the left of herself.",
                        "@Mei| She's not harming them. That's what I can't get past. They're better than they were. They're all better. And they're all just a little not here.",
                        "* You walk home in the dusk, side by side, without speaking. About a mile from the Pavilion, {mei.he} takes your hand and holds it, quite hard, and does not seem to notice that {mei.he} has.")
                .pay(300, 0, 0, 0, 0, 0);

        d.chapter("inn3", "A Gourd by the Road").gap(2)
                .intro(
                        "* You walk to the inn at {@inn} because you need to walk. The road is dry. The trees are green. You have nothing particular in mind except a bowl of something warm and a corner away from the question.",
                        "* The old man is already in the corner. He is a small, shabby, cheerful person in a coat with too many patches, and a battered straw hat pushed back on his head. He sits on the floor with his back against the wall, a red gourd on his knee and a cup beside it. He raises the cup to you gravely. He has been drinking since morning. It does not seem to have slowed him down.")
                .at("inn", "Go to the inn at {@inn} to think.")
                .outro(
                        "@Old man| Ah. You've met the Guest. It's the tea. She does a very fine tea. I would stay away from it. I'm told it's the best in the Garden, and I say that as a man who has made a study of the field.",
                        "@You| Who are you?",
                        "@Old man| Oh, nobody. A man who walks about. I sing a bit. I have a gourd with a hole in it, which is a great problem for a gourd.",
                        "* He pours you a cup out of the gourd, and it is plain water, cold from a well, and tastes of nothing and everything. You drink it. The room settles. The question, which has been pressing on the back of your head for a week, lightens, a little, like a coat taken off.",
                        "@Old man| She isn't wrong, you know. That's the trouble. Everything she says is true. It's an awful thing to be set down. It's also, I'm told, restful. I've known some who went that road and were grateful. I only don't care for the way she does the asking.",
                        "* He lets his head fall back against the wall. His hat tips over his eyes.",
                        "@Old man| If you can't think of a thing, find someone who'll tell you what they were like when they didn't know. That's a very good question to ask a person. Go and ask your friend with the cat.")
                .pay(0, 0, 0, 0, 0, 1);

        d.chapter("meiq", "Mei's Question").gap(1)
                .intro(
                        "* She asks it in the kitchen, at night, over a bowl of cold noodles, with a candle burning between you and the whole school asleep. She has her feet up on the rungs of your chair. She is eating with her fingers. It is the kind of hour at which the Pavilion is a house like any other, and the two of you are only the people who are awake.")
                .wait(1)
                .outro(
                        "@Mei| What is it like? Setting it down. I want you to tell me properly. Not the way you do in the lectures. What does it feel like?",
                        "@You| It's like standing at the top of a flight of stairs with a heavy pot, and being told you can put it on the step. And you put it on the step. And you can see the whole of the hall, suddenly. You didn't know there was so much hall.",
                        "@Mei| And then?",
                        "@You| And then you don't want to pick it up. That's the thing. It's not that you can't. It's that the hall's so big.",
                        "* She is quiet. The candle gutters. She turns her chopsticks over, and over, and looks at them. Then, in a smaller voice than she has ever used with you, a voice you remember from a roadside inn, she says: 'I don't have a lot to put down. Did you know? I thought about it. I thought, I'm happy. I really am. It's just a house and a cat and you. It's very small.'",
                        "@You| It's a good size.",
                        "@Mei| It's a good size. That's what I'm saying. I don't need the hall.",
                        "* She picks up her bowl and goes round the table and sits down beside you on the bench. She leans her whole weight against your shoulder. After a minute she says, into your sleeve: 'I'd like to be the thing you pick up.' And you say that you were thinking exactly that, and neither of you moves until the candle is out.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("catop", "The Cat's Opinion").gap(1)
                .intro(
                        "* The Pavilion cat has formed a view. It is a large, stout, torn-eared tabby called, for no reason anyone can discover, Magistrate, and it has lived on the roof of the reading hall for nine years. It has a reputation. It does not like anyone. It tolerates Mei. It has, on occasion, been seen to stand on a desk and look at a student in the way that judges look at people who have been late with a fine.",
                        "* On the afternoon that Madam Gloam takes tea at the gate, it comes down from the roof.")
                .wait(1)
                .outro(
                        "* You have never seen it move so fast. It comes across the courtyard like a thrown hat, flat out, tail like a bottle brush, and stops six feet from the wall where the tea tray stands. It sits. It raises its ragged head. It looks at the guest with an expression of perfect, serene, concentrated hatred, and begins very slowly to growl: a long, low, rising note, like a kettle thinking about it.",
                        "@Gloam| What a handsome animal.",
                        "* She puts out a gloved hand, palm up, in the manner of one used to the approval of beasts. The cat stares at the glove. Then it turns its back on her, with the most extreme deliberateness, lifts its tail, and sprays the leg of the tea table.",
                        "* There is a silence. Madam Gloam looks at the stream of cat running down the leg. She smiles, very slightly, with one corner of her mouth, and takes her hand back, and folds it with the other in her lap. 'I see,' she says. 'It is always the small ones who are best informed.'",
                        "* She does not take the tea. She leaves it where it is, steaming, on the wall, and rises, and bows to the cat. The cat does not bow back. It is washing its face.")
                .pay(100, 0, 0, 0, 0, 0);

        d.chapter("offer", "The Offer").gap(1)
                .intro(
                        "* She is waiting at the Pavilion gate, as before, with the tray, but this time there is only one cup. She does not ask you to sit. She looks at you with the patient kindness of a person who has never told a lie and has never had to.",
                        "@Gloam| I will say it plainly. I should like to teach you the last line. The technique of setting the world down. It is not death. It is not even leaving. It is the opposite of grasping. A person who sets it down, for an hour or a year or a lifetime, is for that time wholly at ease. The Garden is a heavy thing, you know. You have carried it from the day you woke.",
                        "@Gloam| I should like to teach it to the whole Pavilion. To the whole province. It will be very gentle. It will be the quietest revolution in history. And when enough people have set it down, the Garden will be a light enough thing to be carried away, and nobody will mind at all.",
                        "@You| Carried away by whom?",
                        "@Gloam| By the ones I serve. The ones who put out lamps. You have heard them called the Hollow Court. It is not a pretty name. It was given by those who were afraid. We are only very tired, and very patient, and we should like to rest.")
                .wait(1)
                .outChoice(new Chapters.Choice("guest", "Madam Gloam has made her offer, and the tea is going cold.",
                        "Refuse: you will not teach the world to put itself down.", "Bargain: you will learn it, but teach it with its other half, to pick up again.", "Accept: it is gentle, and you are tired.")
                        .after(
                                new String[]{"@Gloam| I thought you would. You are a person with an excellent grip.", "* She smiles. It is the saddest smile you have seen. She sets the empty cup on the wall, and turns it a little, as if to make sure it is straight."},
                                new String[]{"@Gloam| Ah. That is the clever one. The other half. The picking up.", "@You| You never wrote it.", "@Gloam| No. I never could. It is the only part of the scroll that I did not understand. I should very much like to be shown."},
                                new String[]{"@Gloam| Thank you. You will not regret it. Nobody does.", "* The air in the courtyard seems to go very still. Somewhere a long way off a dog barks, and then stops, as though it had been told something."}))
                .outro(
                        "?guest=1|* She leaves by the east road, on foot, with her tray. At the bend she turns and bows to the Pavilion. It is a perfectly courteous bow. The tea set is left on the wall, with the cup turned straight.",
                        "?guest=2|* She stays the night. You sit on the floor of the reading hall until the lamps burn down, with a stranger and a scroll, working out the part she never wrote. It takes seven hours, and you do not finish, and you do not stop.",
                        "?guest=3|* The first lesson takes an afternoon. It is the most peaceful afternoon of your life. When it ends, you find that Mei is standing in the doorway with {mei.his} arms crossed, and a look on {mei.his} face that you would give the world not to have seen.")
                .pay(0, 0, 0, 0, 0, 0);

        d.chapter("breathout", "Breathing Out").gap(2)
                .intro(
                        "* The technique, when it comes, is very simple. It takes one breath. You draw it in, as the first line says, and you hold the world. You let it out, as the last line says, and you set it down. For the space of the breath there is nothing. Not darkness, not silence, not peace. Simply no weight. You have not known how much you carry until it is gone.",
                        "?love2=1|* Mei finds you in the reading hall, at four in the morning, sitting on the floor with your hands open on your knees and the look of a person who is trying to remember where they left something. 'You're not here,' {mei.he} says. 'You're about half a pace to the left of yourself.'",
                        "?!love2=1|* Mei finds you in the reading hall, at four in the morning, sitting on the floor with your hands open on your knees. 'You're not here,' {mei.he} says. 'You're about half a pace to the left of yourself.'")
                .wait(2)
                .outro(
                        "* It takes four days to bring you back, and the cat does most of it. It sits in your lap and purrs, and the sound goes in at the ears and arrives at the part of you that has not yet come home. Mei sits on the floor and does not say anything, and every so often puts a rice cake in your hand.",
                        "@Mei| I want to ask you something and I want you to answer it with the part of you that's here. Do you want to put it down?",
                        "@You| No.",
                        "@Mei| Why not?",
                        "@You| Because you're in it.",
                        "* It is not an answer that would satisfy a philosopher. It is quite sufficient for a cat, and for a woman on a floor, and for the thing in your chest that has just, with a creak like a rusty hinge, come home.")
                .pay(0, 0, 1, 0, 0, 1);

        d.chapter("questions", "The Students' Questions").gap(2)
                .intro(
                        "* The school has been asking questions, because that is what it does. They arrive at the door of your study in a trickle, then a stream, then a flood, in the evenings, in ones and twos, carrying their slates. They have heard that there is a guest. They have heard that there is an offer. The Pavilion has no secrets, and a school with no secrets runs on conversation like a mill on water.",
                        "* The youngest is a boy of nine. He comes last, after the rest have gone, and stands in the doorway with his hands behind his back and an expression of fierce, practical, horrified concentration.")
                .wait(1)
                .outro(
                        "@Boy| Does someone who puts the world down still love people?",
                        "* It is the best question you have been asked. You look at him for as long as you decently can. Then you admit that you don't know. You have met people who put it down and were gentle, you tell him, and you cannot tell if they were also kind. The difference might be that kindness costs you something and gentleness doesn't.",
                        "@Boy| So you have to pick it up again. To love.",
                        "@You| I think so. I think you have to hold it. You can't love a thing you've put on the floor.",
                        "* He thinks about this. He turns it over, visibly, like a coin. Then he says, with a judicial air, that it seems an awful lot of trouble, and that he will probably do it anyway, and goes away to the kitchen to see about his supper.",
                        "* You sit in the empty room until the lamp wants oil. The page is on the desk, and not a word of it is written. You dip the brush, and put it down, and pick it up. Then you go out for a walk, and come back at dawn with the answer, which is shorter than you thought.")
                .pay(200, 0, 0, 1, 0, 0);

        d.chapter("last", "The Last Page").gap(2)
                .intro(
                        "* You write on the last page at last, because it is yours to write. You sit at Zhao's desk in the Closed Stacks, or where they used to be, or the barn that stands where they were, with a brush and the scroll unrolled to its blank end. The students are waiting in the courtyard. The cat is on the sill.",
                        "* There is a moment, as the brush comes down, when you can feel her listening. Madam Gloam is a long way off, on the east road, and she is listening, with her whole attention, with her cup in her hand. It is a little like being read by someone who is hoping for a good ending.")
                .wait(1)
                .outChoice(new Chapters.Choice("lastpage", "The page is blank. The brush is wet.",
                        "Write the other half: breathing in, you pick it up again.", "Write nothing, and leave the page open for the next reader.", "Write a name: Lan, or Mei, or the boy who never was.")
                        .after(
                                new String[]{"* It is one line. It takes you an hour. It reads: Breathing out, you set it down, and you pick it up when you are ready, and you are allowed to be ready later. You read it back. It is not a very beautiful sentence. It is true.", "* The students file past the desk to read it. Nobody says anything. A small boy touches the paper with one finger, as if to see if it is wet."},
                                new String[]{"* You leave it blank. You lay down the brush. It is the hardest thing you have done in a year, and the best.", "@Mei| You could write something.", "@You| I'm going to leave room for someone better."},
                                new String[]{"* You write a name. You write it very small, in the centre of the page, and look at it, and add the date. It is not a technique. It is not a line of poetry. It is the only thing you have ever written that has had no purpose at all, and you leave it there."}))
                .outro(
                        "?lastpage=1|* The whole school learns the line in a week. It goes down the valley like a rumour, in the mouths of children, to villages that have never seen the Pavilion. In a month it is on the lintels of three inns. In a year it has the dignity of a proverb, and no one remembers who said it first.",
                        "?lastpage=2|* The scroll hangs in the reading hall under glass, open at the empty page. Students come to look at it and say it is a waste of a good page. A few of them, in the evenings, stand in front of it until the lamps are lit, with their lips moving.",
                        "?lastpage=3|* A name appears on the last page of every copy of the Scroll of Unbroken Breath after that, in a small hand: the name of someone the copyist lost. Nobody ordered it. It simply began.",
                        "* On the east road, a long way off, a woman in a grey-green gown puts down a cup, and looks at the sky for a moment, and goes on her way. She does not look back. She does not look displeased.")
                .pay(30000, 3, 0, 4, 2, 3);

        d.epilogue(
                "* You were expelled at eighteen for owning half a scroll. You finish the story as the person who wrote the last line of it, which is a different thing, and rather heavier.",
                "?door=1|* The Scroll of Unbroken Breath is taught in a thousand villages. People who have never seen the Pavilion mend their own cuts. Nobody remembers who first wrote it down. You like that best.",
                "?door=2|* The Stillwater Pavilion has a new motto over its door: Nobody is turned away who asks a good question. It has a very large number of students and a very loud library.",
                "?door=3|* The barn is a school now, with a proper roof. The shelves still hold three books on cookery, which the students regard with a kind of reverence, and which no one is allowed to lend.",
                "?love2=1|* Mei keeps the school's accounts and the school's cat, and has written a short treatise on the breathing, which contains a single illustration: a very bad drawing of a cat, asleep.",
                "?love2=2|* Mei sits on the roof of the reading hall on clear evenings. There is a place beside her. It has been kept.",
                "?love2=3|* Mei writes every month. The cat on the cuff has a hat, and a small sword, and, lately, a minute and extremely serious pair of spectacles.",
                "?guest=1|* A tea set stands on the wall by the gate, with the cup turned straight. Nobody has ever moved it. In the spring, a bird nests in the pot.",
                "?guest=2|* The part she never wrote is a lesson in the school now. It is the shortest lesson in the curriculum. It is the one students ask to hear twice.",
                "?guest=3|* You teach the first line to no one, and the last to no one. You are not sure what that makes you. The cat, when asked, has no opinion.",
                "?dead.calder|* Lord Auditor Calder has a small stone in the cabbage patch. It says only: IRREGULAR. The students leave him a cabbage every spring.",
                "* Breathing in, you hold the world. Breathing out, you set it down. You understand, now, that you were allowed to pick it up again. It was the whole of the matter.");
    }
}
