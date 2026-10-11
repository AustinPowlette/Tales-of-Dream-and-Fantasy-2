package Game;

/** "Cranes Over the Marsh": open country, a song in seven. E Dorian (the raised sixth, C-sharp, gives the A chord its lift), 7/8 felt as 2+2+3 at 120 bpm: a
 *  plucked figure that limps and lands on the long beat, a flute singing in short rising phrases, an erhu answering a sixth higher in the middle, a low
 *  heartbeat, a wood block on the three accents. Plays with the usual layers. */
final class SongCranes {
    private SongCranes() { }

    // chords Em | D | A | Em | Em | G | A | Em   (a bar is 3.5 beats: 1 + 1 + 1.5)
    private static final String MEL_A =
        "B4:1 E5:1 G5:1.5 | F#5:1 A5:1 F#5:1.5 | E5:1 C#5:1 A4:1.5 | B4:1 G4:1 E5:1.5"
      + " | B4:1 E5:1 G5:1.5 | B5:1 D6:1 B5:1.5 | C#6:1 A5:1 E5:1.5 | E5:1 G5:1 E5:1.5";
    // chords C | G | D | Em | C | Am | D | Em
    private static final String MEL_B =
        "G5:1 E5:1 C6:1.5 | D6:1 B5:1 G5:1.5 | A5:1 F#5:1 D6:1.5 | B5:1 G5:1 E5:1.5"
      + " | G5:1 C6:1 E6:1.5 | C6:1 A5:1 E5:1.5 | F#5:1 A5:1 D6:1.5 | B5:1 G5:1 E5:1.5";

    static final String[] LINES = {
        "title: Cranes Over the Marsh",
        "bpm: 120",
        "beats: 3.5",
        "shift: -2 2",
        "moods: 0:4 3:1",
        "form: intro?:p A:mp A2:mp B:mf A3:mf outro:p",

        "pattern limp: 1:.5 5:.5 1':.5 5:.5 3':.5 5:.5 1':.5",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern three: b:1 5:1 b:1.5",
        "pattern heart: @E2:1 r:1 @E2:.5 r:1",
        "pattern tick: @C6:.5 r:.5 @C6:.5 r:.5 @C6:.5 r:1",

        "track lead   role=lead    inst=flute|pluck       vel=0.78 pan=0.10",
        "track lead2  role=lead    inst=erhu|flute        vel=0.91 pan=-0.10",
        "track pluck  role=rhythm  inst=pluck             vel=0.61 pan=0.30  ctr=E3 pat=limp",
        "track harp   role=rhythm  inst=harp              vel=0.67 pan=-0.35 ctr=E3 pat=limp p=60",
        "track pad    role=harmony inst=pad|strings       vel=0.81 pan=0     ctr=A3 pat=padN",
        "track bass   role=bass    inst=cello             vel=1.03 pan=-0.15 ctr=G2 pat=three",
        "track bell   role=color   inst=bell              vel=0.94 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.82 pan=-0.3",
        "track taiko  role=perc    inst=taiko             vel=1.01 pan=0     ctr=E2 pat=heart",
        "track wood   role=perc    inst=wood              vel=0.75 pan=0.3   ctr=C6 pat=tick",

        "[intro]",
        "chords: Em | D | A | Em",
        "pluck: limp x.8",
        "pad: padN x.6",
        "bass: root x.5 @3-4",
        "bell: [E3 B3]:3.5",
        "lead: r:3.5 | r:3.5 | B4:2 A4:1.5 | G4:3.5",

        "[A]",
        "chords: Em | D | A | Em | Em | G | A | Em",
        "lead: " + MEL_A,
        "pluck: limp",
        "pad: padN x.65",
        "bass: three x.8",
        "bell: [E3 B3]:3.5 | r:3.5 | r:3.5 | r:3.5 | [E3 B3]:3.5 | r:3.5 | r:3.5 | r:3.5",
        "chime: r:3.5 | r:3.5 | r:3.5 | r:3.5 | r:3.5 | r:3.5 | r:3.5 | B5:3.5",

        "[A2 < A]",
        "lead: r:3.5",
        "lead2: " + SongTale.octave(MEL_A, 0),
        "harp: limp",
        "pad: padN x.8",
        "wood: tick x.8",

        "[B]",
        "chords: C | G | D | Em | C | Am | D | Em",
        "lead: r:3.5",
        "lead2: " + MEL_B,
        "harp: -",
        "pluck: limp x.9",
        "pad: padN x.85",
        "bass: three",
        "wood: tick",
        "bell: [C3 G3]:3.5",
        "chime: -",

        "[A3 < A]",
        "lead: " + MEL_A,
        "lead2: -",
        "harp: limp",
        "pad: padN x.8",
        "taiko: on",
        "wood: tick",

        "[outro]",
        "chords: Em | D | A | Em",
        "lead: p B4:1 E5:1 G5:1.5 | F#5:1 A5:1 F#5:1.5 | E5:1 C#5:1 A4:1.5 | E5:3.5",
        "pluck: limp x.6",
        "pad: padN x.6",
        "bass: root x.5",
        "bell: r:3.5 | r:3.5 | r:3.5 | [E3 B3 E4]:3.5",
        "chime: r:3.5 | r:3.5 | r:3.5 | E6:3.5",
    };
}
