package Game;

/** "Noon at the Palace of a Thousand Windows": the capital in full sun. A major in a lilting six-eight (a bar is two dotted-quarter beats, 70 bpm): a flute tune that
 *  climbs the chord and falls back, harps cascading under it in sixes, an erhu answering a bar later, a pipa chopping on the second beat, a bright bell, and a final
 *  statement with the tune doubled by the erhu an octave lower. Plays with the usual layers. */
final class SongNoon {
    private SongNoon() { }

    // chords A | E/G# | F#m | D | A | E | D | E
    private static final String MEL_A =
        "C#6:1 E6:2/3 C#6:1/3 | B5:1 G#5:2/3 B5:1/3 | A5:1 C#6:2/3 A5:1/3 | F#5:1 A5:2/3 D6:1/3"
      + " | C#6:1 E6:2/3 C#6:1/3 | B5:1 G#5:2/3 E5:1/3 | F#5:1 A5:2/3 F#5:1/3 | G#5:2";
    // the end turns home (chords ... D | E A)
    private static final String MEL_A2 =
        "C#6:1 E6:2/3 C#6:1/3 | B5:1 G#5:2/3 B5:1/3 | A5:1 C#6:2/3 A5:1/3 | F#5:1 A5:2/3 D6:1/3"
      + " | C#6:1 E6:2/3 C#6:1/3 | B5:1 G#5:2/3 E5:1/3 | F#5:1 A5:1 | G#5:1 A5:1";
    // chords D | A/C# | Bm | E | D | A/C# | E | A
    private static final String MEL_B =
        "F#5:1 A5:1 | C#6:1 E6:1 | D6:1 B5:1 | G#5:1 B5:1 | A5:1 D6:1 | E6:1 C#6:1 | B5:1 G#5:1 | A5:2";

    static final String[] LINES = {
        "title: Noon at the Palace of a Thousand Windows",
        "bpm: 70",
        "beats: 2",
        "shift: -2 2",
        "moods: 6:4 1:1",
        "form: intro?:mp A:mf A2:mf B:mf A3:f outro:mp",

        "pattern roll6: 1:1/3 5:1/3 3':1/3 1':1/3 3':1/3 5:1/3",
        "pattern chop: r:1 =1+=3+=5:1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern lilt: b:1 5:1",
        "pattern pulse: @A1:1 r:1",

        "track lead   role=lead    inst=flute|pluck       vel=0.66 pan=0.10",
        "track lead2  role=lead    inst=erhu|flute        vel=0.71 pan=-0.10",
        "track harp   role=rhythm  inst=harp              vel=0.78 pan=-0.35 ctr=A3 pat=roll6",
        "track pipa   role=rhythm  inst=pipa|pluck        vel=0.86 pan=0.35  ctr=A3 pat=chop",
        "track pad    role=harmony inst=strings           vel=0.69 pan=0     ctr=C#4 pat=padN",
        "track bass   role=bass    inst=cello|harp        vel=0.81 pan=-0.15 ctr=A2 pat=lilt",
        "track drum   role=perc    inst=taiko             vel=0.90 pan=0     ctr=A1 pat=pulse",
        "track bell   role=color   inst=bell              vel=0.80 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.70 pan=-0.3",

        "[intro]",
        "chords: A | E | F#m | E",
        "harp: roll6 x.7",
        "pad: padN x.6",
        "bell: [A2 E3]:2 | r:2 | [F#3 C#4]:2 | r:2",

        "[A]",
        "chords: A | E/G# | F#m | D | A | E | D | E",
        "lead: " + MEL_A,
        "harp: roll6 x.8",
        "pipa: chop x.8",
        "pad: padN x.7",
        "bass: lilt",
        "bell: [A2 E3]:2 | r:2 | r:2 | r:2 | [A2 E3]:2 | r:2 | r:2 | r:2",
        "chime: r:2 | r:2 | r:2 | r:2 | r:2 | r:2 | r:2 | B6:2",

        "[A2 < A]",
        "chords: A | E/G# | F#m | D | A | E | D | E A",
        "lead: " + MEL_A2,
        "lead2: -",
        "drum: pulse x.8",

        "[B]",
        "chords: D | A/C# | Bm | E | D | A/C# | E | A",
        "lead: r:2",
        "lead2: " + MEL_B,
        "harp: roll6",
        "pipa: chop",
        "pad: padN x.9",
        "bass: lilt",
        "drum: pulse",
        "bell: [D3 A3]:2 | r:2 | r:2 | r:2 | [D3 A3]:2 | r:2 | r:2 | r:2",
        "chime: -",

        "[A3 < A2]",
        "lead2: " + SongTale.octave(MEL_A2, -1),
        "drum: pulse x1.2",
        "bell: [A2 E3]:2 | r:2 | r:2 | r:2 | [A2 E3]:2 | r:2 | r:2 | r:2",

        "[outro]",
        "chords: A | E | D | A",
        "lead: p C#6:1 E6:2/3 C#6:1/3 | B5:1 G#5:2/3 B5:1/3 | F#5:2 | A5:2",
        "harp: roll6 x.6",
        "pipa: chop x.5",
        "pad: padN x.6",
        "bass: lilt x.6",
        "bell: r:2 | r:2 | r:2 | [A2 E3 A3]:2",
        "chime: r:2 | r:2 | r:2 | A6:2",
    };
}
