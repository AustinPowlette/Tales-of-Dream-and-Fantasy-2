package Game;

/** "Ashes Before the Gate": a second song for war. D minor in a driving 6/8 (84 bpm, one beat = a dotted quarter): a horn that calls long-short-long over a
 *  relentless cello chug, a marching taiko, string stabs, an erhu answering with the climb to the dominant, and the full band for the last statement.
 *  Where the first war song marches in fours, this one runs in two long beats a bar. Plays whatever its layers (`layers: fixed`). */
final class SongGate {
    private SongGate() { }

    private static String rep(String s, int n) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(i == 0 ? "" : " ").append(s); return b.toString(); }

    // chords Dm | Bb | C | Dm | Dm | Bb | A | Dm
    private static final String HORN_A =
        "D5:1 D5:2/3 F5:1/3 | D5:1 F5:2/3 D5:1/3 | E5:1 G5:2/3 E5:1/3 | A5:1 F5:1"
      + " | D5:1 D5:2/3 F5:1/3 | F5:1 D5:1 | E5:1 C#5:2/3 E5:1/3 | D5:2";
    // chords Gm | Dm | Bb | A | Gm | Dm | A | A
    private static final String ERHU_B =
        "G5:1 Bb5:1 | A5:1 F5:1 | D6:1 Bb5:1 | C#6:1 E6:1 | Bb5:1 G5:2/3 Bb5:1/3 | A5:1 D6:1 | C#6:1 A5:1 | E5:2";

    static final String[] LINES = {
        "title: Ashes Before the Gate",
        "bpm: 84",
        "beats: 2",
        "shift: -2 2",
        "moods: 4:5",
        "layers: fixed",
        "form: intro:mf A:mf B:f A2:ff B2:f A3:ff outro:mf",

        "pattern chug: " + rep("1:1/3", 6),
        "pattern stab: =1+=3+=5:1 r:1/3 =1+=3+=5:2/3",
        "pattern ping: =1:1/3 =5:1/3 =3:1/3 =5:1/3 =3:1/3 =5:1/3",
        "pattern swell: =1+=3+=5:*",
        "pattern march: @D2:1 @D2:1/3 @D2:1/3 @D2:1/3",
        "pattern snap: r:1 @C6:1/3 r:2/3",

        "track horn  role=lead    inst=horn|erhu       vel=1.29  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.67  pan=0.20",
        "track chug  role=rhythm  inst=cello           vel=1.01  pan=-0.20 ctr=D2 pat=chug",
        "track stab  role=rhythm  inst=strings         vel=0.51  pan=0.10  ctr=A3 pat=stab",
        "track pipa  role=rhythm  inst=pipa            vel=0.72  pan=0.35  ctr=D4 pat=ping",
        "track choir role=harmony inst=choir           vel=0.62  pan=0     ctr=C4 pat=swell p=85",
        "track taiko role=perc    inst=taiko           vel=0.65  pan=0     pat=march",
        "track wood  role=perc    inst=wood            vel=0.61  pan=0.3   pat=snap",
        "track bell  role=color   inst=bell            vel=0.72  pan=0.2",

        "[intro]",
        "chords: Dm | Dm | Bb | A",
        "chug: chug x.7",
        "taiko: march x.8",
        "horn: r:2 | r:2 | F5:1 D5:2/3 F5:1/3 | E5:2",

        "[A]",
        "chords: Dm | Bb | C | Dm | Dm | Bb | A | Dm",
        "horn: " + HORN_A,
        "chug: chug",
        "stab: stab",
        "pipa: ping",
        "taiko: march",
        "bell: [D2 A2]:2 | r:2 | r:2 | r:2 | [D2 A2]:2",

        "[B]",
        "chords: Gm | Dm | Bb | A | Gm | Dm | A | A",
        "horn: -",
        "erhu: " + ERHU_B,
        "chug: chug x.9",
        "stab: stab x.9",
        "pipa: ping",
        "choir: swell x.8",
        "taiko: march x1.1",
        "wood: snap",

        "[A2 < A]",
        "chords: Dm | Bb | C | Dm | Dm | Bb | A | Dm",
        "erhu: r:2 | r:2 | r:2 | r:2 | A5:2 | D6:2 | C#6:2 | D6:2",
        "choir: swell",
        "wood: snap",
        "taiko: march x1.2",

        "[B2 < B]",
        "horn: G4:2 | A4:2 | Bb4:2 | C#5:2 | G4:2 | A4:2 | A4:2 | A4:2",
        "taiko: march x1.2",

        "[A3 < A2]",
        "erhu: " + SongTale.octave(HORN_A, 1),
        "taiko: march x1.3",

        "[outro]",
        "chords: Dm | Bb | A | Dm",
        "horn: D5:1 D5:2/3 F5:1/3 | F5:1 D5:1 | E5:2 | D5:2",
        "erhu: -",
        "chug: chug x.6",
        "taiko: march x.7",
        "bell: r:2 | r:2 | r:2 | [D2 A2 D3]:2",
    };
}
