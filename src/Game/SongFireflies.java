package Game;

/** "Fireflies by the Stone Bridge": a lullaby for the night. E-flat major in a slow twelve-eight (a bar is four dotted-quarter beats of three eighths, 46 bpm): a
 *  piano singing a rocking tune (up the triad and back), a harp rolling under it in sixes, a flute taking over in the middle on long notes, a soft pad, bells
 *  like far-off lamps. The melody never leaves the staff and never jumps. Plays whatever its layers (`layers: fixed`). */
final class SongFireflies {
    private SongFireflies() { }

    // chords Eb | Cm | Ab | Bb | Eb | Gm | Ab | Bb
    private static final String MEL_A =
        "G5:1 Bb5:2/3 G5:1/3 Eb5:2 | Eb5:1 G5:2/3 Eb5:1/3 C5:2 | Ab5:1 C6:2/3 Ab5:1/3 Eb5:2 | D5:1 F5:2/3 D5:1/3 Bb4:2"
      + " | G5:1 Bb5:2/3 G5:1/3 Eb6:2 | D6:1 Bb5:1 G5:2 | C6:1 Eb6:2/3 C6:1/3 Ab5:2 | D6:1 Bb5:1 F5:2";
    // the same tune ending at home (chords ... Ab Bb | Eb)
    private static final String MEL_A2 =
        "G5:1 Bb5:2/3 G5:1/3 Eb5:2 | Eb5:1 G5:2/3 Eb5:1/3 C5:2 | Ab5:1 C6:2/3 Ab5:1/3 Eb5:2 | D5:1 F5:2/3 D5:1/3 Bb4:2"
      + " | G5:1 Bb5:2/3 G5:1/3 Eb6:2 | D6:1 Bb5:1 G5:2 | C6:2 D6:2 | Eb6:4";
    // chords Cm | Fm | Bb | Eb | Cm | Fm | Bb | Bb
    private static final String MEL_B =
        "C6:2 Eb6:2 | Ab5:2 C6:2 | D6:2 Bb5:2 | G5:2 Bb5:2 | Eb6:2 G6:2 | C6:2 Ab5:2 | D6:2 Bb5:2 | F5:4";

    static final String[] LINES = {
        "title: Fireflies by the Stone Bridge",
        "bpm: 46",
        "beats: 4",
        "shift: -2 2",
        "moods: 3:5",
        "layers: fixed",
        "form: intro?:pp A:p B:mp A2:p outro:pp",

        "pattern roll: 1:1/3 5:1/3 3':1/3 1':1/3 3':1/3 5:1/3 1:1/3 5:1/3 3':1/3 1':1/3 3':1/3 5:1/3",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",

        "track lead  role=lead    inst=piano|pluck     vel=1.71  pan=-0.10",
        "track lead2 role=lead    inst=flute|erhu      vel=0.81  pan=0.20",
        "track harp  role=rhythm  inst=harp            vel=1.43  pan=0.35  ctr=Eb3 pat=roll",
        "track pad   role=harmony inst=pad|strings     vel=1.00  pan=0     ctr=G3 pat=padN",
        "track choir role=harmony inst=choir           vel=1.28  pan=0     ctr=Bb3 pat=padN p=60",
        "track bass  role=bass    inst=cello           vel=1.71  pan=-0.15 ctr=Bb1 pat=root",
        "track bell  role=color   inst=bell            vel=1.31  pan=0.2",
        "track chime role=color   inst=chime           vel=1.05  pan=-0.3",

        "[intro]",
        "chords: Eb | Cm | Ab | Bb",
        "harp: roll x.7",
        "pad: padN x.6",
        "bell: [Eb3 Bb3]:4 | r:4 | [Ab2 Eb3]:4 | r:4",

        "[A]",
        "chords: Eb | Cm | Ab | Bb | Eb | Gm | Ab | Bb",
        "lead: " + MEL_A,
        "harp: roll x.7",
        "pad: padN x.6",
        "bass: root x.7",
        "bell: [Eb3 Bb3]:4 | r:4 | r:4 | r:4 | [Eb3 Bb3]:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | F6:4",

        "[B]",
        "chords: Cm | Fm | Bb | Eb | Cm | Fm | Bb | Bb",
        "lead: r:4",
        "lead2: " + MEL_B,
        "harp: roll x.8",
        "pad: padN x.8",
        "bass: root x.8",
        "bell: [C3 G3]:4",
        "chime: -",

        "[A2 < A]",
        "chords: Eb | Cm | Ab | Bb | Eb | Gm | Ab Bb | Eb",
        "lead: " + MEL_A2,
        "harp: roll x.7",
        "choir: padN",
        "pad: padN x.7",

        "[outro]",
        "chords: Eb | Cm | Bb | Eb",
        "lead: p G5:1 Bb5:2/3 G5:1/3 Eb5:2 | Eb5:1 G5:2/3 Eb5:1/3 C5:2 | D5:4 | Eb5:4",
        "harp: roll x.5",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: r:4 | r:4 | r:4 | [Eb3 Bb3 Eb4]:4",
        "chime: r:4 | r:4 | r:4 | Eb6:4",
    };
}
