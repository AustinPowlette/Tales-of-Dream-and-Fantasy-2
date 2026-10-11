package Game;

/** "Rain on the Tile Roofs": a quiet city song for a wet afternoon. A minor, a slow 4/4 at 72 bpm: a piano drips a pentatonic tune (A C D E G) note by note
 *  while a harp keeps eighths underneath like water in a gutter, a flute sings an octave above in the middle, single chimes fall at random, a cello holds the floor.
 *  No drums at all. Plays with the usual layers. */
final class SongRain {
    private SongRain() { }

    // chords Am | F | C | G | Am | Dm | E | Am
    private static final String MEL_A =
        "E5:1 C5:.5 A4:.5 E5:2 | C5:1 A4:.5 F4:.5 C5:2 | G4:1 E5:.5 G5:.5 E5:2 | D5:1.5 B4:.5 G4:2"
      + " | E5:1 A5:1 G5:.5 E5:.5 C5:1 | D5:1 F5:1 A5:2 | G#5:1 E5:1 B4:1 E5:1 | A5:3 r:1";
    // chords F | C | Dm | Am | F | G | Em | Am
    private static final String MEL_B =
        "A5:2 C6:2 | G5:1.5 E5:.5 C5:2 | F5:2 A5:2 | E5:2 A5:1 E5:1 | C6:2 A5:2 | B5:2 D6:2 | G5:2 B5:2 | A5:3 r:1";

    static final String[] LINES = {
        "title: Rain on the Tile Roofs",
        "bpm: 72",
        "beats: 4",
        "shift: -2 2",
        "moods: 1:3 3:2 0:1",
        "form: intro?:p A:mp B:mp A2:mp outro:p",

        "pattern gutter: 1:.5 5:.5 3':.5 5:.5 1':.5 5:.5 3':.5 5:.5",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",

        "track lead  role=lead    inst=piano|pluck     vel=1.89  pan=-0.10",
        "track lead2 role=lead    inst=flute|erhu      vel=0.85  pan=0.20",
        "track harp  role=rhythm  inst=harp            vel=1.24  pan=0.35  ctr=A3 pat=gutter",
        "track pad   role=harmony inst=strings|pad     vel=0.95  pan=0     ctr=C4 pat=padN",
        "track bass  role=bass    inst=cello           vel=1.41  pan=-0.15 ctr=A2 pat=root",
        "track bell  role=color   inst=bell            vel=0.97  pan=0.2",
        "track chime role=color   inst=chime           vel=0.85  pan=-0.3",

        "[intro]",
        "chords: Am | F | Am | E",
        "harp: gutter x.7",
        "pad: padN x.5",
        "chime: r:4 | E6:.5 r:3.5 | r:4 | A6:.5 r:3.5",

        "[A]",
        "chords: Am | F | C | G | Am | Dm | E | Am",
        "lead: " + MEL_A,
        "harp: gutter x.7",
        "pad: padN x.6",
        "bass: root x.7",
        "bell: [A2 E3]:4 | r:4 | r:4 | r:4 | [A2 E3]:4",
        "chime: r:4 | r:2 G6:.5 r:1.5 | r:4 | r:4 | r:4 | r:3 C7:.5 r:.5 | r:4 | r:4",

        "[B]",
        "chords: F | C | Dm | Am | F | G | Em | Am",
        "lead: r:4",
        "lead2: " + MEL_B,
        "harp: gutter x.8",
        "pad: padN x.8",
        "bass: root x.8",
        "bell: [F3 C4]:4",
        "chime: r:1 E6:.5 r:2.5 | r:4 | r:4 | r:3 A6:.5 r:.5 | r:4 | r:4 | r:2 D6:.5 r:1.5 | r:4",

        "[A2 < A]",
        "lead: " + MEL_A,
        "lead2: " + SongTale.octave(MEL_A, 1),
        "harp: gutter x.8",

        "[outro]",
        "chords: Am | F | E | Am",
        "lead: p E5:1 C5:.5 A4:.5 E5:2 | C5:1 A4:.5 F4:.5 C5:2 | B4:2 G#4:2 | A4:4",
        "harp: gutter x.5",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: r:4 | r:4 | r:4 | [A2 E3 A3]:4",
        "chime: r:4 | r:4 | r:4 | A6:4",
    };
}
