package Game;

/** "Thunder in the Ravine": danger as a chase. C-sharp minor in a hard, fast 3/4 (130 bpm): a horn hammering the triad, a taiko on every beat, a cello
 *  pounding eighths, a pipa trembling, strings stabbing on the first beat, an erhu screaming high over the middle, a bell on every bar line. The i VI VII V
 *  turn (C#m A B G#) never settles. Plays whatever its layers (`layers: fixed`). */
final class SongRavine {
    private SongRavine() { }

    private static String rep(String s, int n) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(i == 0 ? "" : " ").append(s); return b.toString(); }

    // chords C#m | A | B | G# | C#m | A | F#m | G#
    private static final String HORN_A =
        "C#4:1 E4:1 G#4:1 | A4:2 E4:1 | B4:1 D#5:1 F#5:1 | D#5:1 C5:1 G#4:1"
      + " | C#5:1 E5:1 G#5:1 | A5:2 E5:1 | F#5:1 A5:1 C#6:1 | G#5:1 D#5:1 C5:1";
    // chords E | B | C#m | A | E | B | A | G#
    private static final String ERHU_B =
        "G#5:1 B5:1 E6:1 | D#6:2 B5:1 | C#6:1 E6:1 G#6:1 | E6:2 C#6:1 | B5:1 G#5:1 E5:1 | F#5:1 B5:1 D#6:1 | C#6:1 A5:1 E5:1 | G#5:2 D#5:1";

    static final String[] LINES = {
        "title: Thunder in the Ravine",
        "bpm: 130",
        "beats: 3",
        "shift: -2 2",
        "moods: 2:5",
        "layers: fixed",
        "form: intro?:mf A:f B:f A2:ff B2:f A3:ff outro:mf",

        "pattern pound: " + rep("b:.5", 6),
        "pattern trem: " + rep("=1+=5:.5", 6),
        "pattern stab: =1+=3+=5:1 r:2",
        "pattern boom: @C#2:1 @C#2:1 @C#2:1",
        "pattern boom2: @C#2:1 @C#2:.5 @C#2:.5 @C#2:1",
        "pattern swell: =1+=3+=5:*",
        "pattern kickR: @kick>:1 @kick^:.5 @kick:.5 @kick:1",
        "pattern snareR: r:2 @snare>:1",
        "pattern hatR: @hat>:.25 @hat^:.25 @hat:.25 @hat^:.25 @hat>:.25 @hat^:.25 @hat:.25 @hat^:.25 @hat>:.25 @hat^:.25 @hat:.25 @hat^:.25",
        "pattern tomR: r:2 @tom3>:.5 @tom5>:.5",

        "track horn  role=lead    inst=horn|cello      vel=1.51  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.70  pan=0.20",
        "track ost   role=rhythm  inst=cello           vel=0.88  pan=-0.15 ctr=E2 pat=pound",
        "track pipa  role=rhythm  inst=pipa            vel=0.58  pan=0.35  ctr=G#3 pat=trem",
        "track stab  role=harmony inst=strings         vel=0.55  pan=0.10  ctr=G#3 pat=stab",
        "track choir role=harmony inst=choir           vel=0.60  pan=0     ctr=E4 pat=swell p=80",
        "track taiko role=perc    inst=taiko           vel=0.62  pan=0     pat=boom",
        "track bell  role=color   inst=bell            vel=0.66  pan=0.2",
        "track kick  role=perc    inst=kit      vel=0.75 pan=0    ctr=C2 pat=kickR",
        "track snare role=perc    inst=kit      vel=0.45 pan=0.1  ctr=C2 pat=snareR",
        "track hat   role=perc    inst=kit      vel=0.45 pan=-0.2 ctr=C2 pat=hatR",
        "track tom   role=perc    inst=kit      vel=0.45 pan=0.3  ctr=C2 pat=tomR",

        "[intro]",
        "chords: C#m | A | B | G#",
        "kick: kickR x.8",
        "ost: pound x.7",
        "taiko: boom x.8",
        "horn: r:3 | r:3 | B3:3 | G#3:3",

        "[A]",
        "chords: C#m | A | B | G# | C#m | A | F#m | G#",
        "kick: kickR",
        "snare: snareR",
        "hat: hatR x.8",
        "horn: " + HORN_A,
        "ost: pound",
        "pipa: trem",
        "stab: stab",
        "taiko: boom",
        "bell: [C#3 G#3]:3 | r:3 | r:3 | r:3 | [C#3 G#3]:3 | r:3 | r:3 | r:3",

        "[B]",
        "chords: E | B | C#m | A | E | B | A | G#",
        "kick: kickR",
        "snare: snareR",
        "hat: hatR",
        "tom: tomR",
        "horn: -",
        "erhu: " + ERHU_B,
        "ost: pound x.9",
        "pipa: trem",
        "stab: stab x.9",
        "choir: swell x.8",
        "taiko: boom x1.1",
        "bell: [E3 B3]:3",

        "[A2 < A]",
        "tom: tomR",
        "kick: kickR x1.1",
        "erhu: r:3 | r:3 | r:3 | r:3 | G#5:3 | A5:3 | C#6:3 | D#6:3",
        "choir: swell",
        "taiko: boom2 x1.1",

        "[B2 < B]",
        "horn: E4:3 | B3:3 | C#4:3 | A3:3 | E4:3 | B3:3 | A3:3 | G#3:3",
        "taiko: boom2 x1.1",

        "[A3 < A2]",
        "erhu: " + SongTale.octave(HORN_A, 1),
        "taiko: boom2 x1.2",

        "[outro]",
        "chords: C#m | A | G# | C#m",
        "kick: kickR x.6",
        "horn: C#5:3 | A4:3 | G#4:3 | C#4:3",
        "erhu: -",
        "ost: pound x.6",
        "pipa: -",
        "stab: -",
        "choir: -",
        "taiko: boom x.7",
        "bell: r:3 | r:3 | r:3 | [C#3 G#3 C#4]:3",
    };
}
