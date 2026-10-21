package Game;

/** The Last Heir of the Azure Cloud Sect, season 4 (2026-10-20): "The Green Seal" (the dry court voice, with the sect's cosy chores). Azure Cloud is no longer small. A green seal turns up on a letter, a rival undersells the sect,
 *  the council wants to buy the farms the way the Green Table once did, and the human finds the Table's habit in their own ledger. Choices: growth (what the sect becomes), charter (the terms with Lisbet's cooperative).
 *  Flags: growth, charter, habit. The Door season (StoryDoors) comes after. */
final class TaleHeirMore {
    private TaleHeirMore() { }

    private static void mark(Character c, Starts.State st) { Chapters.set(st, "craft0", c.itemsCrafted); }

    static void add(Chapters.Def d) {
        d.flags("craft0", "growth", "charter", "habit");
        d.place("table", "far");

        d.interlude(3, "Seven Hundred Beds",
                "* Azure Cloud is not a small sect any more. It has seven hundred beds and a kitchen that feeds three hundred at a sitting. The cauldron by the door has a seam in it that shines. Dunn Tarrow, who arrived with a cow, is the Steward.",
                "* The books balance. The roof has no leaks. Somebody has put a second sign on the gate, in gilt, which says AZURE CLOUD: ESTABLISHED. You did not order it.");
        d.season(4, "The Green Seal");

        d.chapter("seven", "Seven Hundred Beds").years(3)
                .major(5)
                .intro(
                        "* Dunn brings the quarterly books to your study in a leather folder. He is thirty now, broad and quick, with a clean collar and a ledger-clerk's squint. He lays the folder on the desk with a small flourish. He says that he would like you to sit down. He says that he has some figures.")
                .wait(4)
                .outro(
                        "@Dunn| It's a surplus. A very large one. We have made eleven thousand over the cost of the year, which is nine thousand more than we've ever made. I took the liberty of putting it in a plan. It's in the second section. It has a heading. I think you'll like the heading.",
                        "* You turn to the second section. The heading says: EXPANSION. Under it there are eleven pages of tidy handwriting, a map of the district, and a column of farms with prices against them. The prices are fair. The farms are small. You recognise the names. You recognise, with a small cold lurch, the pattern.",
                        "@Dunn| It's only sensible. If we owned the herb farms we could set the price. We could stop the middlemen. We could look after the farmers properly. I've drawn it up so nobody gets hurt.",
                        "@Wen| (from the door, with a cup, mildly) That's what they said, you know. The last time.")
                .pay(100, 0, 0, 0, 0, 1);

        d.chapter("wax", "A Seal in Green Wax").gap(8)
                .setup((c, st) -> Chapters.spawn(c, st, "lisbet"))
                .intro(
                        "* The letter comes by a plain courier on a grey morning. It is addressed in a handsome flowing hand, to the Master of Azure Cloud, and sealed with a blob of green wax. The wax has been stamped with a leaf. The leaf has seven veins. It is not O.V.'s leaf. It is a newer, sharper cut. Wen looks at it for a long moment and then, without a word, goes to the window and shuts it.")
                .meet("lisbet", "Show Lisbet Vale the new seal.")
                .outro(
                        "@Lisbet| I don't know it. I swear I don't. It's a leaf with seven veins. My father's had five. Mister Marr's had nine. This is somebody's attempt to look like both. It's a forger's idea of authority.",
                        "* She turns the letter over. She is older now, and quiet, and fine-boned, with a streak of grey in her dark hair and a habit of holding her cup in both hands. She keeps a modest cooperative of herb growers in the south. She has refused three offers to be made rich. She reads the letter aloud, in a flat even voice, with no inflection at all.",
                        "@Lisbet| It says: The Successor Committee of the Green Table greets the Azure Cloud Sect, and proposes a mutual understanding. It says: we have taken the liberty of buying the debts of eleven of your suppliers. It says: we look forward to your reply at your earliest convenience. It is signed: A Friend.",
                        "* You put the letter down on the table. Nobody speaks. The wax is still faintly warm.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("farms", "Maud Hobb Remembers").gap(8)
                .intro(
                        "* Maud Hobb is seventy-two and still works the herb farm up the valley, with a stick, and a boy to carry. She receives you in the yard in her apron, among drying racks, and does not offer tea. She has the look of a woman who has heard a certain kind of footstep before. She says that she supposes you have come about the debts.")
                .friends(5, "Gather the farms: make five friends.")
                .outro(
                        "@Maud| It began with a good loan. That's what you never tell the young. It always begins with someone being kind. The Table lent my husband two hundred when the roof came down. No interest. No paper. A handshake and a smile. Two years later it lent him four hundred more, and then it was a matter of the harvest, and the price, and a very nice man with a pen.",
                        "@You| And now someone has bought the debts of your neighbours.",
                        "@Maud| (calmly) Then someone will be along with a very nice offer. It'll be fair. It'll be generous. It'll be a handshake. I'd say it was the same man, only he's dead, and his daughter's a cooperative. It's not a man, you see. It's a habit.",
                        "* She walks you to the gate. She says the farms of the valley are meeting on Sunday, in the barn. She says you may come if you sit at the back and keep your hands in your pockets. She says it is not a threat. She says it is advice.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("lamp", "The Gentle Lamp").gap(8)
                .intro(
                        "* The Gentle Lamp Sect opens in the next county in the spring, with a banner, a band, and a very large warehouse. Its pills are the same as yours, to the eye. They are a third cheaper. They come in a pretty tin. The farm carters report that the Lamp has sent buyers into the valley, with cash, to take any herbs that Azure Cloud has not contracted for, at a price that no honest man could refuse.")
                .wait(4)
                .outro(
                        "* By midsummer the pill kitchen is half empty. Orders for the Silent Tonic have fallen by a third. The Steward comes to your study with a face like a closed door. He says that the figures are unprecedented, and unsentimental, and that he has taken the liberty of drawing up a plan. He does not say what it is. He lays the folder on the desk with the second section on top.",
                        "@Dunn| It would only be for a season. It would only be to see us through. We could buy the farms. We could undercut them. We could be the Lamp's equal and then some.",
                        "* The cauldron by the door is humming. It has not hummed in a year. The sound comes up through the floor of the study like the purr of a very large cat. Ambrose, passing the open door with a tray, stops and looks in, and goes away again without a word.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("price", "A Price War in Pills").gap(8)
                .setup(TaleHeirMore::mark)
                .intro(
                        "* You leave the farms alone and do something simpler. You work. You fill every order that comes in, at the old price, with the old herbs, and you make the pills faster than Azure Cloud has ever made them. The kitchen goes round the clock. The whole sect takes a shift at the cauldron, Wicks and elders and cooks and a Steward in an apron. The seam in the iron glows at night like a coal.")
                .made(12, "Fill the orders: make twelve batches of pills.")
                .outro(
                        "* On the ninth night a stranger comes to the kitchen door in a good grey coat. He is a smooth, round-faced man of fifty with ink on his cuff and an affable smile. He does not give his name. He admires the cauldron. He says he understands the sect is under some strain. He says there is a way to ease it. A small adjustment. A pinch of filler in the third stage. Nobody would know.",
                        "@Stranger| The Lamp does it. Everyone does it. It isn't dishonest. It's economy. A pill that is ninety per cent the pill is a perfectly good pill. Think of the savings.",
                        "* You look at him for ages. You think of Master Hui, and the cracked cauldron, and a woman in a farmyard who has heard a certain footstep before. You say that you are obliged to him. You say that it is a very good idea and that you will give it the consideration it deserves. You pour him a cup of tea, and while he drinks it, you show him the door.")
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("quay", "Pettibone Quay Writes Again").gap(8)
                .intro(
                        "* The letter from Pettibone Quay arrives on a wet Monday, in a hand that has got no better. He is the magistrate, or was; he is a very old man now, living on a pension, with a parrot. He writes in violet ink on thin blue paper, with long paragraphs and a great many underlinings, as a person writes who has been afraid to put a word on paper for years.")
                .wait(2)
                .outro(
                        "@Quay| (in the letter) I have been meaning to tell you for minutes. Ysolde Marr is dead. She died in her bed in the south at the age of seventy, of a chill, without a sou. Her Table was dissolved. Her books were burned. But there was a second set. There is always a second set. I know where it is, because I was the one who carried it.",
                        "@Quay| (in the letter) It is in a lead box in the cellar of my sister's house. I should like you to have it. I should like it burned, but I should like you to read it first. You will find that the Table was never a conspiracy. It was a method. The method was written down in a pamphlet. The pamphlet has been reprinted. It has been reprinted nine times in the last six years.",
                        "* You hold the letter in the lamplight. At the bottom, under the signature, in a very small crabbed hand, he has added: Be careful of the kind ones. They are the only ones who can ever take anything.")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("garden", "Orrin's Garden").gap(8)
                .intro(
                        "* The Vale house stands at the end of a quiet lane in the south, behind a high hedge. It has a wide verandah and a view of a river. Lisbet takes you through the long garden to a bench under a pear tree, and pours two cups of tea. She does not say anything for minutes. The tree is in blossom. A bee is working the nearest branch.")
                .wait(2)
                .outro(
                        "?dead.orrin|@Lisbet| He is buried here. Under the bench. I put him where he could see the river. I could never decide whether he deserved it. I think that is what a garden is for.",
                        "?!dead.orrin|@Lisbet| He lives here. In the back room. He makes tea and does the crossword and says nothing about the past. I could never decide whether he deserved it. I think that is what a daughter is for.",
                        "@Lisbet| I want to tell you about the seal. It wasn't his. It was a clerk's. A clerk called Marr kept the first one, in a drawer, in a bank, and it was given to anyone who was kind enough to sign a loan. Father signed. He didn't know what he was signing. And the next man signed, and the next. It isn't a family seal. It's a custom. It passes to the man who has most to lose.",
                        "* She picks up a fallen petal and crushes it between finger and thumb. She says: 'It will come to you. In some form. It came to him through a roof that had fallen in. It will come to you through a very good year.'")
                .pay(200, 0, 0, 0, 0, 1);

        d.chapter("habit", "The Habit").gap(8)
                .intro(
                        "* You find it on a Thursday, in the sect's own ledger. It is a small entry in your own hand. It is a loan, of two hundred, to a farmer named Pike, at no interest, on the strength of a handshake. The sum is a stranger to you, and so is the farmer. You turn the page, and there is another. And another. A column of kindnesses, a dozen of them, each with a date, each small and generous and fair.")
                .wait(2)
                .outro(
                        "* You count them. You sit with the pen in your hand. There are forty-one. They have been made over three years, by you, by Dunn, by an elder, a cook. They are all to farmers. They are all unsecured. Every one has grown a little, in the way that kindness does in a ledger, and none of them has been paid. You realise, with a feeling very like vertigo, that you have been lending, quietly, to the valley, for years.",
                        "@Wen| (in the doorway, with a mug, gently) It's the Table's way. It always begins with kindness. It isn't a crime, you know. Not until someone asks for something back.",
                        "@You| What do I do?",
                        "@Wen| Forgive them. All of them. Burn the paper. And then do the one thing the Table never did. Stop.")
                .done((c, st) -> Chapters.set(st, "habit", 1))
                .pay(300, 0, 0, 1, 0, 1);

        d.chapter("vote4", "The Vote, Again").gap(8)
                .intro(
                        "* The council meets in the great hall on the first of the month, as it has since Hui's day. It has grown. There are thirty elders in the horseshoe now, in blue, with a gavel and a jug. Dunn presents the plan. It takes an hour. It has charts. It has a conclusion. He sits down with a flushed face and a thick folder and looks at you across the hall with a look of painful eagerness.")
                .wait(1)
                .choice(new Chapters.Choice("growth", "Dunn's plan would buy the farms and make Azure Cloud the largest sect in the district.",
                        "Refuse the plan: stay as you are, a sect of seven hundred that does not own its suppliers.", "Accept it, with terms: buy the farms, but give them back to the farmers as shares.", "Split: found a second sect for the valley, owned by those who work it.")
                        .after(
                                new String[]{"* You refuse. You say it quietly, with your hands flat on the table. You say that a sect that cannot grow without owning its neighbours has already stopped being a sect. The hall is silent. Dunn goes white. Then, slowly, he begins to nod. He says: 'I knew. I think I knew. I only wanted to hear someone say it.' He closes the folder. It is the nicest act of obedience you have ever seen.", "@Wen| (softly) Well said."},
                                new String[]{"* You accept, with terms. The farms are bought. Every one is paid for in full. Then, in a short ceremony in the barn, with Maud Hobb presiding, each farmer is handed a share of the sect. It is a little paper with a green leaf on it. They are told that it is theirs to keep or to sell as they please. Nobody sells. The seal is used once more, on the day, and never again.", "@Maud| (leaning on her stick) That's the first time I've seen the thing do anything good."},
                                new String[]{"* You split. You found a second house in the valley, with a roof of thatch and a kitchen and a long table, and give it to the farmers who work the herbs. It is called the Green Bench. It owes nothing to Azure Cloud. It sells to whom it likes. In the first year it undercuts you by a fifth. You are, to your own surprise, delighted.", "@Dunn| (grinning ruefully) I think I've been out-competed by my own idea."}))
                .outro(
                        "* The hall disperses in a murmur. The jug is empty. The gavel is where it was dropped. The Steward is the last to go. He pauses in the door and looks back, and then, to your astonishment, he comes across the floor and takes the folder from the table and tears the top page in half. He says he will start again. He says it is a very good heading.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("charter", "Lisbet's Charter").gap(8)
                .intro(
                        "* Lisbet comes to Azure Cloud at the turn of the year with a bundle of papers and a hat. She sits at the long table in the hall, under the portrait of Master Hui, and lays them out one by one with the air of a woman dealing a hand she has been dreading for years. She says that she has a proposal. She says it will take about an hour. She says there is tea if you want it.")
                .wait(2)
                .choice(new Chapters.Choice("charter", "Lisbet's cooperative and Azure Cloud could write a charter between them: a set of rules that no seal could ever buy.",
                        "A charter of plain trade: fair prices, written down, open to anyone.", "A charter of mutual debt: each owes the other a year of work, and neither can call it in.", "No charter: the only rule is that you eat together once a month.")
                        .after(
                                new String[]{"* You write it in a week. It is eleven clauses. The first says: No debt shall be bought. The second says: No price shall be hidden. The others are about what happens when someone forgets. It is signed by forty farmers and a hundred sect members and a cow. It is hung in the hall of Azure Cloud and the barn of the Green Bench.", "@Lisbet| It is the dullest document I have ever read. I am so proud of it."},
                                new String[]{"* You write it in a week. It is two clauses. Each side owes the other a year of work in a decade, to be asked for only by the debtor. Nobody can call it in. Nobody can sell it. It is the only debt in the district that cannot be bought, and a clerk of the Gentle Lamp, shown a copy, sits down and laughs until he has to be given water.", "@Lisbet| My father would have wept."},
                                new String[]{"* You write one line. It says: Once a month, at the long table, all of us. There is a Sunday. There is a pot. The farmers come, and the Wicks, and Lisbet's growers, and the carters, and the children, and eat. Nobody signs. Nobody is asked for anything. By the third Sunday it has become the strongest thing in the valley, and the Lamp has stopped bothering to compete.", "@Maud| It's what the Table never understood. It's not the paper. It's the pot."}))
                .outro(
                        "* The charter, or the pot, is a success. It does not make anyone rich. It does not make anyone safe. But in the next spring a green seal with seven veins appears on a letter at the sect's gate. The porter, who is a woman of eighty and a former farmer, reads it through, and says that it is nothing to do with them, and puts it on the fire.")
                .pay(500, 0, 0, 1, 0, 1);

        d.chapter("wax4", "Burning the Wax").gap(8)
                .intro(
                        "* They burn the seal at midsummer, on the green behind the kitchen, in a pit dug for the purpose by Dunn. The sect turns out in blue. The farmers turn out in aprons. Lisbet brings a lead box, scoured, and a hammer. Pettibone Quay is carried in a chair by two boys, with the parrot on his shoulder. He is ninety. He is smiling. He has not smiled in public since the year of the Table.")
                .wait(2)
                .outro(
                        "* You open the box. It is full of paper: ledgers, letters, contracts, a pamphlet, a hundred green seals in a drawer. Lisbet reads the pamphlet aloud, standing, in a clear voice, from the first page to the last, to the sect and the farms and the parrot. It takes twenty minutes. When she has done there is a long silence. Then she drops it into the pit.",
                        "* The wax burns with a smell of resin and old summers. The leaves, in the heat, curl up and go, one at a time, like a hand closing. When the last has gone Maud Hobb steps forward with a trowel and sets a hazel sapling in the ashes, and presses the earth round it with her boot. She says that it is the first honest thing she has ever planted on that ground.",
                        "@Wen| (beside you, with a hand in yours) Do you hear it? The cauldron. It's humming.",
                        "* You do. It is a low note, steady, from the hall, deep in the seam. It is not a threat. It is a thank-you, from a very long way off, in a voice you last heard at the foot of a grave in the herb garden.")
                .pay(2000, 3, 0, 4, 2, 2);
    }
}
