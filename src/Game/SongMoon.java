package Game;

/** "Moon on the Frozen River": a second song for night and winter. D minor, very slow 4/4 (46 bpm), almost no motion: a harp rocking in quarters, a cello
 *  pedal, long falling phrases on an erhu (A D F, then down to the dominant's third), a piano answering high and sparse, a turn to F major in the middle,
 *  one bell. Written to leave room, like the other night song; it plays whatever its layers (`layers: fixed`). */
final class SongMoon {
    private SongMoon() { }

    // chords Dm | Bbmaj7 | Gm7 | A | Dm | Bbmaj7 | Gm7 | A
    private static final String MEL_A =
        "A4:2 D5:1 F5:1 | D5:3 C5:1 | Bb4:2 D5:2 | C#5:3 r:1"
      + " | A4:2 D5:1 F5:1 | F5:2 A5:1 F5:1 | G5:2 F5:1 D5:1 | E5:2 C#5:1 A4:1";
    // chords F | C/E | Dm | Bb | Gm7 | C | A | Dm
    private static final String MEL_B =
        "C5:2 F5:2 | E5:1.5 D5:.5 C5:2 | D5:2 F5:1 A5:1 | F5:2 D5:1 Bb4:1"
      + " | G5:2 D5:1 Bb4:1 | E5:2 G5:1 E5:1 | C#5:2 E5:1 A5:1 | D5:3 r:1";
    // the piano's answer to the first phrase (same chords as MEL_A, high and sparse)
    private static final String ANS_A =
        "r:2 A5:1 F5:1 | F5:3 r:1 | D5:2 G5:2 | E5:3 r:1"
      + " | r:2 A5:1 F5:1 | A5:3 r:1 | Bb5:2 G5:2 | A5:3 r:1";

    static final String[] LINES = {
        "title: Moon on the Frozen River",
        "bpm: 46",
        "beats: 4",
        "shift: -2 2",
        "moods: 3:5",
        "layers: fixed",
        "form: intro?:pp A:p B:mp A2:p outro:pp",

        "pattern rock: 1:1 5:1 3':1 5:1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",

        "track lead  role=lead    inst=erhu|flute      vel=1.10  pan=0.10",
        "track lead2 role=lead    inst=piano|pluck     vel=1.79  pan=-0.20",
        "track harp  role=rhythm  inst=harp            vel=1.66  pan=0.35  ctr=D3 pat=rock",
        "track pad   role=harmony inst=pad|strings     vel=0.99  pan=0     ctr=A3 pat=padN",
        "track choir role=harmony inst=choir           vel=1.25  pan=0     ctr=C4 pat=padN p=60",
        "track bass  role=bass    inst=cello           vel=1.40  pan=-0.15 ctr=G2 pat=root",
        "track bell  role=color   inst=bell            vel=1.23  pan=0.2",
        "track chime role=color   inst=chime           vel=1.14  pan=-0.3",

        "[intro]",
        "chords: Dm | Bbmaj7 | Gm7 | A",
        "harp: rock x.7",
        "pad: padN x.6",
        "bell: [D3 A3]:4",
        "chime: r:4 | r:4 | r:4 | E6:4",

        "[A]",
        "chords: Dm | Bbmaj7 | Gm7 | A | Dm | Bbmaj7 | Gm7 | A",
        "lead: " + MEL_A,
        "harp: rock x.7",
        "pad: padN x.6",
        "bass: root x.7",
        "bell: [D3 A3]:4 | r:4 | r:4 | r:4 | [D3 A3]:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | A6:4",

        "[B]",
        "chords: F | C/E | Dm | Bb | Gm7 | C | A | Dm",
        "lead: " + MEL_B,
        "harp: rock x.8",
        "pad: padN x.8",
        "bass: root x.8",
        "bell: [F3 C4]:4",
        "chime: -",

        "[A2 < A]",
        "chords: Dm | Bbmaj7 | Gm7 | A | Dm | Bbmaj7 | Gm7 | A",
        "lead: r:4",
        "lead2: " + ANS_A,
        "harp: rock x.7",
        "choir: padN",
        "pad: padN x.7",

        "[outro]",
        "chords: Dm | Bbmaj7 | A | Dm",
        "lead: p A4:2 D5:1 F5:1 | D5:3 C5:1 | C#5:3 r:1 | D5:4",
        "harp: rock x.6 @1-3",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: r:4 | r:4 | r:4 | [D3 A3 D4]:4",
        "chime: r:4 | r:4 | r:4 | D6:4",
    };
}
