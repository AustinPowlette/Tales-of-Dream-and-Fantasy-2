package Game;

/** "Lanterns on the Reed River": a second song for the open country. D major, an unhurried 4/4 (64 bpm): a guzheng-like pluck flowing in eighths, a dizi hook that
 *  rises a step and settles (F# E D, A), an erhu answering with a climb to the dominant, a cello holding the bottom, a bright turn through G and E minor in the
 *  middle. Plays with the usual layers: at night it thins to the pluck, the pad and a soft dizi; in danger and war the `drive` tracks take over. */
final class SongReeds {
    private SongReeds() { }

    // chords D | Bm7 | Gmaj7 | A | D | Bm7 | Em7 | A
    private static final String MEL_A =
        "F#5:1.5 E5:.5 D5:1 A4:1 | B4:2 D5:1 F#5:1 | G5:1.5 F#5:.5 E5:1 D5:1 | C#5:3 r:1"
      + " | F#5:1.5 E5:.5 D5:1 A4:1 | B4:1 D5:1 F#5:1 A5:1 | G5:2 E5:1 B4:1 | C#5:2 E5:2";
    // chords D | Bm7 | Gmaj7 | A | D | Bm7 | Em7 A | D   (the erhu answers and comes home)
    private static final String MEL_A2 =
        "A4:1 D5:1.5 F#5:.5 A5:1 | F#5:1.5 E5:.5 D5:2 | B4:1 D5:1 G5:2 | E5:2 C#5:2"
      + " | A4:1 D5:1.5 F#5:.5 A5:1 | F#5:1.5 E5:.5 D5:2 | G5:1 F#5:1 E5:1 C#5:1 | D5:3 r:1";
    // chords G | D/F# | Em7 | A | G | D/F# | Em7 | A   (the middle: the river bends)
    private static final String MEL_B =
        "B4:1 D5:1 G5:2 | F#5:2 A5:2 | G5:1.5 F#5:.5 E5:2 | E5:1 C#5:1 A4:2"
      + " | B4:1 D5:1 B5:2 | A5:2 F#5:2 | E5:1 G5:1 B5:2 | A5:2 E5:2";
    // the last time: the hook an octave higher over a lifted bass
    private static final String MEL_A3 =
        "F#5:1.5 E5:.5 D5:1 A4:1 | B4:2 D5:1 F#5:1 | G5:1.5 F#5:.5 E5:1 D5:1 | C#5:3 r:1"
      + " | F#5:1.5 E5:.5 D5:1 A4:1 | B4:1 D5:1 F#5:1 A5:1 | G5:1 F#5:1 E5:1 C#5:1 | D5:3 r:1";

    static final String[] LINES = {
        "title: Lanterns on the Reed River",
        "bpm: 64",
        "beats: 4",
        "shift: -2 2",
        "moods: 0:4 3:1 1:1",
        "form: intro?:p A:mp A2:mp B:mf A3:mf outro:p",

        "pattern flow: 1:.5 5:.5 3':.5 5:.5 1':.5 5:.5 3':.5 5:.5",
        "pattern drift: 1:1 5:1 3':1 5:1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern lilt: b:2 5:2",
        "pattern drive4: @D2:.5 r:.5 @D2:.5 @D2:.5 r:.5 @D2:.5 @D2:.5 r:.5",
        "pattern ost4: b:1 5:1 b:1 5:1",
        "pattern heart: @D2:1 r:3",

        "track lead   role=lead    inst=flute|pluck       vel=0.83 pan=0.10",
        "track lead2  role=lead    inst=erhu|flute        vel=0.90 pan=-0.10",
        "track pluck  role=rhythm  inst=pluck             vel=0.70 pan=0.30  ctr=D3 pat=flow",
        "track harp   role=rhythm  inst=harp              vel=0.91 pan=-0.35 ctr=D3 pat=drift p=60",
        "track pad    role=harmony inst=pad|strings       vel=0.75 pan=0     ctr=A3 pat=padN",
        "track bass   role=bass    inst=cello             vel=0.91 pan=-0.15 ctr=G2 pat=root",
        "track cello  role=counter inst=cello|horn        vel=0.94 pan=-0.20",
        "track bell   role=color   inst=bell              vel=0.91 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.82 pan=-0.3",
        "track taiko  role=perc    inst=taiko             vel=1.01 pan=0     ctr=D2 pat=heart",
        "track dtaiko role=drive   inst=taiko             vel=1.27 pan=0     ctr=D2 pat=drive4 all",
        "track dcello role=drive   inst=cello             vel=0.94 pan=-0.1  ctr=D2 pat=ost4 all",

        "[intro]",
        "chords: D | Bm7 | Gmaj7 | A",
        "pluck: flow x.8",
        "pad: padN x.6",
        "bass: root x.5 @3-4",
        "bell: [D3 A3]:4",
        "lead: r:4 | r:4 | B4:2 A4:2 | E5:4",

        "[A]",
        "chords: D | Bm7 | Gmaj7 | A | D | Bm7 | Em7 | A",
        "lead: " + MEL_A,
        "pluck: flow",
        "pad: padN x.65",
        "bass: root x.8",
        "bell: [D3 A3]:4 | r:4 | r:4 | r:4 | [D3 A3]:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | A5:4",

        "[A2 < A]",
        "chords: D | Bm7 | Gmaj7 | A | D | Bm7 | Em7 A | D",
        "lead: r:4",
        "lead2: " + MEL_A2,
        "harp: drift",
        "cello: A3:4 | F#3:4 | G3:4 | E3:4 | A3:4 | F#3:4 | E3:2 C#4:2 | D4:4",
        "pad: padN x.8",
        "bass: lilt",

        "[B]",
        "chords: G | D/F# | Em7 | A | G | D/F# | Em7 | A",
        "lead: r:4",
        "lead2: " + MEL_B,
        "harp: -",
        "pluck: flow x.9",
        "cello: B3:4 | A3:4 | G3:4 | E3:4 | B3:4 | A3:4 | G3:4 | E3:4",
        "pad: padN x.85",
        "bass: lilt",
        "bell: [G2 D3]:4",
        "chime: -",

        "[A3 < A]",
        "chords: D | Bm7 | Gmaj7 | A | D | Bm7 | Em7 A | D",
        "lead: " + MEL_A3,
        "lead2: -",
        "harp: drift",
        "cello: A3:4 | F#3:4 | G3:4 | E3:4 | A3:4 | F#3:4 | E3:2 C#4:2 | D4:4",
        "pad: padN x.8",
        "bass: lilt",
        "taiko: on",

        "[outro]",
        "chords: D | Bm7 | A | D",
        "lead: p F#5:1.5 E5:.5 D5:1 A4:1 | B4:2 D5:2 | C#5:3 r:1 | D5:4",
        "pluck: flow x.6",
        "pad: padN x.6",
        "bass: root x.5",
        "bell: r:4 | r:4 | r:4 | [D3 A3 D4]:4",
        "chime: r:4 | r:4 | r:4 | D6:4",
    };
}
