package Game;

/** "Stars Over the Salt Flats": night as open space. E Lydian (the raised fourth, A-sharp, in the F-sharp chord is the sound of it), 3/4 at 50 bpm: a few high
 *  piano notes with long silences, a pad and a choir that hold their chords for whole bars, chimes sparkling at the top, a harp rocking in threes, a flute
 *  singing the same shape a sixth lower in the middle. Plays whatever its layers (`layers: fixed`). */
final class SongSalt {
    private SongSalt() { }

    // chords E | F# | E | B | C#m | F# | B | E
    private static final String MEL_A =
        "B5:2 G#5:1 | A#5:2 F#5:1 | G#5:1 B5:1 E6:1 | D#6:2 B5:1"
      + " | E6:2 C#6:1 | A#5:1 C#6:1 F#5:1 | D#6:1 B5:1 F#5:1 | G#5:3";
    // chords C#m | F# | G#m | F# | C#m | B | F# | B
    private static final String MEL_B =
        "E5:2 G#5:1 | F#5:2 A#5:1 | B5:2 D#6:1 | C#6:3"
      + " | E6:2 C#6:1 | D#6:1 B5:1 F#5:1 | A#5:2 C#6:1 | B5:3";

    static final String[] LINES = {
        "title: Stars Over the Salt Flats",
        "bpm: 50",
        "beats: 3",
        "shift: -2 2",
        "moods: 3:5",
        "layers: fixed",
        "form: intro?:pp A:p B:mp A2:p outro:pp",

        "pattern rock: 1:1 5:1 3':1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",

        "track lead  role=lead    inst=piano|pluck     vel=1.89  pan=-0.10",
        "track lead2 role=lead    inst=flute|erhu      vel=0.85  pan=0.20",
        "track harp  role=rhythm  inst=harp            vel=1.59  pan=0.35  ctr=E3 pat=rock p=80",
        "track pad   role=harmony inst=pad|strings     vel=0.98  pan=0     ctr=G#3 pat=padN",
        "track choir role=harmony inst=choir           vel=1.44  pan=0     ctr=B3 pat=padN",
        "track bass  role=bass    inst=cello           vel=1.35  pan=-0.15 ctr=E2 pat=root",
        "track bell  role=color   inst=bell            vel=1.16  pan=0.2",
        "track chime role=color   inst=chime           vel=1.17  pan=-0.3",

        "[intro]",
        "chords: E | F# | E | B",
        "pad: padN x.6",
        "choir: padN x.4",
        "chime: r:3 | E6:1 r:2 | r:3 | B6:1 r:2",

        "[A]",
        "chords: E | F# | E | B | C#m | F# | B | E",
        "lead: " + MEL_A,
        "harp: rock x.7",
        "pad: padN x.6",
        "choir: padN x.5",
        "bass: root x.7",
        "bell: [E3 B3]:3 | r:3 | r:3 | r:3 | [C#3 G#3]:3 | r:3 | r:3 | r:3",
        "chime: r:3 | r:3 | r:2 G#6:1 | r:3 | r:3 | r:3 | r:3 | B6:3",

        "[B]",
        "chords: C#m | F# | G#m | F# | C#m | B | F# | B",
        "lead: r:3",
        "lead2: " + MEL_B,
        "harp: rock x.8",
        "pad: padN x.8",
        "choir: padN x.7",
        "bass: root x.8",
        "bell: [C#3 G#3]:3",
        "chime: -",

        "[A2 < A]",
        "lead: " + MEL_A,
        "lead2: " + SongTale.octave(MEL_A, 0),
        "choir: padN x.9",

        "[outro]",
        "chords: E | F# | B | E",
        "lead: p B5:2 G#5:1 | A#5:2 F#5:1 | D#6:3 | E6:3",
        "harp: rock x.5",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: r:3 | r:3 | r:3 | [E3 B3 E4]:3",
        "chime: r:3 | r:3 | r:3 | E6:3",
    };
}
