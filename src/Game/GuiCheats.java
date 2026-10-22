package Game;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/** The Cheats window (only a Cheater sees it work): one tab for the quick number cheats, then People, Items, Companions, Policies and World. Every button sends a `cheat ...` command through the command line, so the
 *  log shows what happened and nothing here writes the world. `cheat help` lists the same cheats in words. */
final class GuiCheats {
    private GuiCheats() { }

    private static JTextField field(String text, int cols) { JTextField f = new JTextField(text, cols); return f; }

    private static JPanel page() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(GuiWindows.BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return p;
    }

    private static void note(JPanel p, String text) {
        JLabel l = GuiWindows.label("<html><div style='width:660px'>" + text + "</div></html>");
        l.setForeground(Theme.DIM);
        p.add(l);
    }

    /** One line: the parts, then a button that sends the command the supplier builds. */
    private static void line(final MapViewer mv, JPanel p, String button, String tip, final Supplier<String> cmd, Component... parts) {
        JPanel row = GuiWindows.flow(FlowLayout.LEFT);
        for (Component c : parts) row.add(c);
        row.add(mv.makeBtn(button, tip, () -> { String s = cmd.get(); if (s != null && !s.trim().isEmpty()) mv.queueInput(s.trim()); }));
        p.add(row);
    }

    private static JLabel l(String s) { return GuiWindows.label(s); }

    static void open(final MapViewer mv) {
        GuiWindows.window(mv, "cheats", "Cheats", 820, 640, 2000, (d, refreshers) -> {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(GuiWindows.MONO_B);
            final JTextField target = field("self", 8);

            // ---- quick: the first set
            JPanel q = page();
            JPanel tp = GuiWindows.flow(FlowLayout.LEFT);
            tp.add(l("Target:")); tp.add(target); tp.add(l("(self, or #id of anyone: Search anyone / People here show the ids)"));
            q.add(tp);
            String[][] rows = {{"Add spirit stones", "ss", "10000"}, {"Set major level (0-10)", "level", "3"}, {"Set potential (1-100)", "potential", "100"}, {"Set kill count", "kills", "100"}, {"Give a weapon of level (1-10)", "weapon", "5"}};
            for (final String[] r : rows) {
                final JTextField val = field(r[2], 8);
                line(mv, q, r[0], "Sends: cheat <target> " + r[1] + " <number>", () -> "cheat " + target.getText().trim() + " " + r[1] + " " + val.getText().trim(), val);
            }
            JPanel simple = GuiWindows.flow(FlowLayout.LEFT);
            simple.add(mv.makeBtn("Restore cultivation", () -> mv.queueInput("cheat " + target.getText().trim() + " heal")));
            simple.add(mv.makeBtn("Clear quests", () -> mv.queueInput("cheat " + target.getText().trim() + " reset")));
            simple.add(mv.makeBtn("Track", () -> mv.queueInput("cheat " + target.getText().trim() + " track")));
            q.add(simple);
            note(q, "Cheats change the world for good. They run as the 'cheat' command, so the log shows what happened. 'cheat help' lists every cheat in words.");
            tabs.addTab("Quick", q);

            // ---- people
            JPanel pe = page();
            line(mv, pe, "Strike down", "Divine lightning: the target dies, whatever protects them (a child, a story's person, a True Immortal, the No Death mark).", () -> "cheat kill " + target.getText().trim(), l("Target (the Quick tab's field):"), target);
            final JTextField ra = field("#", 6), rb = field("#", 6);
            final JSpinner rv = new JSpinner(new SpinnerNumberModel(3, -4, 3, 1));
            line(mv, pe, "Set the bond", "The bond between any two people, both ways: -4 wanted, -3 arch-enemies, -2 rivals, -1 dislike, 0 strangers, 1 friends, 2 close friends, 3 best friends.", () -> "cheat relation " + ra.getText().trim() + " " + rb.getText().trim() + " " + rv.getValue(), l("Between"), ra, l("and"), rb, l("value"), rv);
            final JTextField tx = field("", 5), ty = field("", 5);
            line(mv, pe, "Teleport", "Tile numbers, or the coordinates shown (x100). The target is the Quick tab's field.", () -> "cheat teleport " + tx.getText().trim() + " " + ty.getText().trim() + " " + target.getText().trim(), l("To x"), tx, l("y"), ty);
            final JComboBox<String> slot = new JComboBox<>(new String[]{"martial", "martial2", "movement", "movement2"});
            final JComboBox<String> what = new JComboBox<>(new String[]{"level", "potential"});
            final JSpinner tv = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
            line(mv, pe, "Set the technique", "Set the level or the potential of an equipped technique of the target.", () -> "cheat technique " + slot.getSelectedItem() + " " + what.getSelectedItem() + " " + tv.getValue() + " " + target.getText().trim(), slot, what, tv);
            final JTextField res = field("", 14);
            line(mv, pe, "Resurrect", "Call back one of the recently dead (an #id or part of a name). Leave the box empty to list the recent dead in the log.", () -> "cheat resurrect " + res.getText().trim(), l("Who:"), res);
            final JComboBox<String> unb = new JComboBox<>(new String[]{"both", "weapon", "armor", "off"});
            line(mv, pe, "Unbreakable gear", "The target's weapon and / or armor never breaks.", () -> "cheat unbreakable " + unb.getSelectedItem() + " " + target.getText().trim(), unb);
            JPanel mk = GuiWindows.flow(FlowLayout.LEFT);
            mk.add(mv.makeBtn("No Death mark: on", "Only a person who plays can kill the target: nothing else can, the epoch included.", () -> mv.queueInput("cheat nodeath on " + target.getText().trim())));
            mk.add(mv.makeBtn("off", "Mortal again.", () -> mv.queueInput("cheat nodeath off " + target.getText().trim())));
            pe.add(mk);
            tabs.addTab("People", pe);

            // ---- growth: the person's own numbers
            JPanel gr = page();
            line(mv, gr, "Break through now", "At once to the next realm: the last level of this one is filled and the gate always opens. The target is the Quick tab's field.", () -> "cheat breakthrough " + target.getText().trim(), l("Target (the Quick tab's field):"), target);
            JPanel pass = GuiWindows.flow(FlowLayout.LEFT);
            pass.add(mv.makeBtn("Always pass: on", "Every breakthrough and every tribulation of the target succeeds.", () -> mv.queueInput("cheat pass on " + target.getText().trim())));
            pass.add(mv.makeBtn("off", "Breakthroughs are fair again.", () -> mv.queueInput("cheat pass off " + target.getText().trim())));
            pass.add(mv.makeBtn("Free crafting: on", "Crafting never runs out of herbs, metal or Blood (the bag is filled every turn).", () -> mv.queueInput("cheat freecraft on " + target.getText().trim())));
            pass.add(mv.makeBtn("off", "Crafting costs materials again.", () -> mv.queueInput("cheat freecraft off " + target.getText().trim())));
            gr.add(pass);
            final JSpinner ageV = new JSpinner(new SpinnerNumberModel(30, 0, 100000, 1));
            line(mv, gr, "Set the age", "The target's age in years.", () -> "cheat age " + ageV.getValue() + " " + target.getText().trim(), ageV);
            final JTextField lifeV = field("500", 8);
            line(mv, gr, "Set the lifespan", "The age at which the target dies of old age.", () -> "cheat lifespan " + lifeV.getText().trim() + " " + target.getText().trim(), lifeV);
            final JSpinner inhV = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
            line(mv, gr, "Set the inhibitor", "The slowing left by failed breakthroughs: 1 is none, each failure adds one, 0 is twice as fast as normal.", () -> "cheat inhibitor " + inhV.getValue() + " " + target.getText().trim(), inhV);
            line(mv, gr, "Master every technique", "Every technique the target knows is at its full level (its potential).", () -> "cheat mastery " + target.getText().trim(), l("every technique known"));
            final JComboBox<String> elV = new JComboBox<>(new String[]{"Wood", "Fire", "Earth", "Metal", "Water"});
            final JCheckBox elT = new JCheckBox("and the techniques");
            elT.setBackground(GuiWindows.BG); elT.setForeground(GuiWindows.FG);
            line(mv, gr, "Change the element", "The element of the target's path (and, ticked, of every technique they know).", () -> "cheat element " + elV.getSelectedItem() + " " + target.getText().trim() + (elT.isSelected() ? " techniques" : ""), elV, elT);
            final JTextField wV = field("1000000", 10);
            line(mv, gr, "Set the wealth", "The target's spirit stones, set (not added).", () -> "cheat wealth " + wV.getText().trim() + " " + target.getText().trim(), wV);
            final JTextField ma = field("#", 6), mb = field("#", 6);
            line(mv, gr, "Marry", "Marries any two people (the old spouses are left).", () -> "cheat marry " + ma.getText().trim() + " " + mb.getText().trim(), l("Between"), ma, l("and"), mb);
            final JTextField pw = field("#", 6);
            line(mv, gr, "Pregnant", "A woman with a living husband is with child.", () -> "cheat pregnant " + pw.getText().trim(), l("Who"), pw);
            line(mv, gr, "Reveal this person", "Prints the target in full: path, element, techniques, potential, inhibitor, sect, spouse.", () -> "cheat reveal " + (target.getText().trim().equalsIgnoreCase("self") ? "self" : target.getText().trim()), l("the target"));
            line(mv, gr, "Reveal the tile", "Prints everyone in this tile with their path and element.", () -> "cheat reveal here", l("everyone here"));
            tabs.addTab("Growth", gr);

            // ---- items
            JPanel it = page();
            final JTextField gl = field("5", 4), gn = field("1000", 7);
            final JComboBox<String> gk = new JComboBox<>(new String[]{"weapon", "armor", "array", "instrument"});
            line(mv, it, "Give gear", "Gives an item of the level to the target (the Quick tab's field).", () -> "cheat give " + gk.getSelectedItem() + " " + gl.getText().trim() + " " + target.getText().trim(), gk, l("level 1-10"), gl);
            final JComboBox<String> pk = new JComboBox<>(new String[]{"cultivation", "potential"});
            line(mv, it, "Give pills", "Cultivation pills of a level, or Potential Pills.", () -> "cheat give pill " + pk.getSelectedItem() + " " + gl.getText().trim() + " " + gn.getText().trim() + " " + target.getText().trim(), pk, l("level"), gl, l("how many"), gn);
            final JComboBox<String> mk2 = new JComboBox<>(new String[]{"herb", "metal"});
            line(mv, it, "Give materials", "Herbs or metal of a quality.", () -> "cheat give " + mk2.getSelectedItem() + " " + gl.getText().trim() + " " + gn.getText().trim() + " " + target.getText().trim(), mk2, l("quality"), gl, l("how many"), gn);
            line(mv, it, "Give Blood", "Blood (1,000 make a Potential Pill).", () -> "cheat give blood " + gn.getText().trim() + " " + target.getText().trim(), l("how many"), gn);
            final JComboBox<String> tk = new JComboBox<>(new String[]{"martial", "movement"});
            final JComboBox<String> te = new JComboBox<>(new String[]{"any element", "Wood", "Fire", "Earth", "Metal", "Water"});
            final JSpinner tpot = new JSpinner(new SpinnerNumberModel(50, 1, 100, 1));
            line(mv, it, "Give a technique", "A new technique the target knows at level 1, with the potential and element chosen.", () -> "cheat give technique " + tk.getSelectedItem() + " " + tpot.getValue() + " " + (te.getSelectedIndex() == 0 ? "" : te.getSelectedItem()) + " " + target.getText().trim(), tk, l("potential"), tpot, te);
            tabs.addTab("Items", it);

            // ---- companions
            JPanel co = page();
            final JTextField cl = field("30", 4), cname = field("", 12);
            line(mv, co, "Tame a companion", "A companion beast of any level (the cap does not count).", () -> "cheat tame " + cl.getText().trim() + " " + cname.getText().trim(), l("level"), cl, l("name (optional)"), cname);
            final JTextField cn = field("1", 3), cv = field("40", 4);
            final JComboBox<String> cw = new JComboBox<>(new String[]{"level", "potential"});
            line(mv, co, "Change a companion", "Sets the level or the potential of companion number n (the Companions list).", () -> "cheat companion " + cn.getText().trim() + " " + cw.getSelectedItem() + " " + cv.getText().trim(), l("companion"), cn, cw, cv);
            note(co, "Companions are the beasts that follow you; the Backpack's Companions tab lists them with their numbers.");
            tabs.addTab("Companions", co);

            // ---- policies
            JPanel po = page();
            final JCheckBox evade = new JCheckBox("Evade death: when I die I simply go on living (also while AUTO)");
            final JCheckBox win = new JCheckBox("100% fight win (every fight I am in is won)");
            final JCheckBox noesc = new JCheckBox("100% no escape (nobody can flee from me)");
            final boolean[] set = {false};
            for (JCheckBox b : new JCheckBox[]{evade, win, noesc}) { b.setBackground(GuiWindows.BG); b.setForeground(GuiWindows.FG); po.add(b); }
            evade.addActionListener(e -> { if (!set[0]) mv.queueInput("cheat evade " + (evade.isSelected() ? "on" : "off") + " self"); });
            win.addActionListener(e -> { if (!set[0]) mv.queueInput("cheat win " + (win.isSelected() ? "on" : "off") + " self"); });
            noesc.addActionListener(e -> { if (!set[0]) mv.queueInput("cheat noescape " + (noesc.isSelected() ? "on" : "off") + " self"); });
            note(po, "These are policies of your own character. 100% win and no escape can also be set on anyone with `cheat win on #id`; the No Death mark is in the People tab.");
            refreshers.add(() -> {
                Character me = GuiWindows.player(mv);
                if (me == null) return;
                set[0] = true;
                try { evade.setSelected(me.pc != null && me.pc.evadeDeath); win.setSelected(me.cheatWin); noesc.setSelected(me.cheatNoEscape); } finally { set[0] = false; }
            });
            tabs.addTab("Policies", po);

            // ---- world
            JPanel wo = page();
            final JTextField nm = field("3", 3), nt = field("", 10);
            line(mv, wo, "Create a person", "A new person at your tile, with a realm (0-10) and a trait (optional: Alchemist, Blacksmith, Leader...). They join the world at the end of the week.", () -> "cheat create npc " + nm.getText().trim() + " " + nt.getText().trim(), l("major realm"), nm, l("trait"), nt);
            final JTextField ml = field("40", 4), mn = field("", 12);
            line(mv, wo, "Create a Monster", "A Monster wakes at your tile (never above the 20th strongest cultivator).", () -> "cheat create monster " + ml.getText().trim() + " " + mn.getText().trim(), l("level"), ml, l("name"), mn);
            final JTextField bn = field("10", 4);
            line(mv, wo, "Add wild beasts", "Wild beasts for your tile.", () -> "cheat create beast " + bn.getText().trim(), l("how many"), bn);
            final JComboBox<String> cc = new JComboBox<>(new String[]{"population", "treasury"});
            final JTextField cv2 = field("100000", 10);
            line(mv, wo, "Change the city", "The city you stand in: its people or its treasury.", () -> "cheat city " + cc.getSelectedItem() + " " + cv2.getText().trim(), cc, cv2);
            final JTextField cown = field("self", 6), crn = field("", 14);
            line(mv, wo, "Make the city's lord", "Sets the owner of the city you stand in (self or #id).", () -> "cheat city owner " + cown.getText().trim(), l("lord"), cown);
            line(mv, wo, "Rename the city", "", () -> "cheat city rename " + crn.getText().trim(), l("name"), crn);
            final JTextField sid = field("self", 6), srn = field("", 14);
            final JComboBox<String> sk = new JComboBox<>(new String[]{"master", "join", "expel"});
            line(mv, wo, "Change the sect", "The sect of your tile (or yours): make someone its master, force someone in, or cast them out.", () -> "cheat sect " + sk.getSelectedItem() + " " + sid.getText().trim(), sk, sid);
            line(mv, wo, "Rename the sect", "", () -> "cheat sect rename " + srn.getText().trim(), l("name"), srn);
            tabs.addTab("World", wo);

            // ---- this tile
            JPanel ti = page();
            note(ti, "These change the tile you stand in.");
            final JComboBox<String> trK = new JComboBox<>(new String[]{"(any)", "technique", "instrument", "poison", "potential", "pill", "clothes", "weapon", "array"});
            line(mv, ti, "Heaven-Sent treasure", "A Heaven-Sent treasure descends here; take it with the usual quest.", () -> "cheat treasure " + (trK.getSelectedIndex() == 0 ? "" : trK.getSelectedItem()), trK);
            JPanel vein = GuiWindows.flow(FlowLayout.LEFT);
            vein.add(mv.makeBtn("Spirit vein: on", "A spirit vein under this tile (retreats, dual cultivation and cities gather round it).", () -> mv.queueInput("cheat vein on")));
            vein.add(mv.makeBtn("off", "No spirit vein here.", () -> mv.queueInput("cheat vein off")));
            ti.add(vein);
            final JComboBox<String> dgK = new JComboBox<>(new String[]{"ruins", "lair", "trial", "haunted", "off"});
            final JTextField dgL = field("30", 4);
            line(mv, ti, "Dungeon", "Makes this tile a dungeon of the kind and level (or takes the dungeon away). Not in cities, sects, Originis or the story's places.", () -> "cheat dungeon " + dgK.getSelectedItem() + " " + dgL.getText().trim(), dgK, l("level"), dgL);
            final JTextField arL = field("20", 4);
            line(mv, ti, "Install an array", "An array of the level guards this tile; attackers must break it.", () -> "cheat array install " + arL.getText().trim(), l("level 1-100"), arL);
            line(mv, ti, "Break the arrays", "Every array of this tile is broken.", () -> "cheat array break", l("this tile"));
            line(mv, ti, "Wipe the beasts", "Every wild beast and Monster of this tile falls.", () -> "cheat wipe", l("this tile"));
            line(mv, ti, "Wake a beast", "One wild beast of this tile wakes as a Monster (never above the 20th strongest cultivator).", () -> "cheat awaken", l("a wild beast"));
            final JTextField tlo = field("", 3), thi = field("", 3);
            line(mv, ti, "Open a tournament", "A tournament is announced here (leave the realms empty for yours).", () -> "cheat tournament open " + tlo.getText().trim() + " " + thi.getText().trim(), l("realms"), tlo, l("to"), thi);
            JPanel tr = GuiWindows.flow(FlowLayout.LEFT);
            tr.add(mv.makeBtn("Hold it next week", "Everyone here who may enter is signed up and the bouts are fought next week.", () -> mv.queueInput("cheat tournament hold")));
            tr.add(mv.makeBtn("Call it off", "Closes the tournament of this tile (the prize goes back).", () -> mv.queueInput("cheat tournament close")));
            ti.add(tr);
            line(mv, ti, "Reveal the whole map", "Every place made so far goes on your map.", () -> "cheat reveal map", l("all places"));
            tabs.addTab("This tile", ti);

            // ---- time and the Heavens
            JPanel tm = page();
            final JSpinner jw = new JSpinner(new SpinnerNumberModel(10, 1, 520, 1));
            line(mv, tm, "Jump ahead", "The weeks pass at once (an alert or an attack ends it early).", () -> "cheat jump " + jw.getValue(), jw, l("weeks"));
            final JComboBox<String> sn = new JComboBox<>(new String[]{"spring", "summer", "autumn", "winter"});
            line(mv, tm, "Go to a season", "Waits until the season begins.", () -> "cheat season " + sn.getSelectedItem(), sn);
            line(mv, tm, "Bring the epoch", "The Heavens grow weary at the next tick: the great purge begins (the ranked, the children and the Palace's road are spared).", () -> "cheat epoch", l("the great purge"));
            JPanel tsq = GuiWindows.flow(FlowLayout.LEFT);
            final JTextField sx = field("", 5), sy = field("", 5);
            tsq.add(l("Teleport with the squad to x")); tsq.add(sx); tsq.add(l("y")); tsq.add(sy);
            tsq.add(mv.makeBtn("Go", "You and your squad move to the tile.", () -> mv.queueInput("cheat teleport " + sx.getText().trim() + " " + sy.getText().trim() + " self squad")));
            tm.add(tsq);
            tabs.addTab("Time", tm);
            return tabs;
        });
    }
}
