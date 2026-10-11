package Game;

/** "The Nine Lamps": a second song for the capital and the Palace. B-flat major, a stately 3/4 (66 bpm), like a procession climbing steps: a horn and
 *  cello carry the tune in long notes (D F, G F D, E-flat G, F), an erhu answers above it, strings and a hushed choir hold the chords, a taiko steps on the
 *  first beat, a bell tolls. The middle turns through C minor and A-flat; the last time the tune is doubled an octave up. Plays with the usual layers. */
final class SongLamps {
    private SongLamps() { }

    // chords Bb | Gm | Eb | F | Bb | Gm | Eb F | Bb
    private static final String MEL_A =
        "D5:2 F5:1 | G5:1.5 F5:.5 D5:1 | Eb5:2 G5:1 | F5:2 C5:1"
      + " | D5:2 F5:1 | Bb5:2 G5:1 | G5:1.5 A5:1.5 | Bb5:2 r:1";
    // the answer above (chords Bb | Gm | Eb | F | Bb | Gm | Eb F | Bb)
    private static final String MEL_A2 =
        "F5:2 Bb5:1 | D6:2 Bb5:1 | G5:2 Bb5:1 | A5:2 F5:1"
      + " | F5:2 Bb5:1 | D6:2 Bb5:1 | G5:1.5 A5:1.5 | Bb5:2 r:1";
    // chords Cm | Ab | Eb | Bb | Cm | Gm | F | F7
    private static final String MEL_B =
        "C5:2 Eb5:1 | Ab5:2 Eb5:1 | G5:2 Eb5:1 | F5:2 D5:1"
      + " | Eb5:2 G5:1 | D5:2 Bb4:1 | C5:2 A4:1 | Eb5:2 A4:1";

    static final String[] LINES = {
        "title: The Nine Lamps",
        "bpm: 66",
        "beats: 3",
        "shift: -2 2",
        "moods: 6:5 5:1",
        "form: intro?:p A:mp A2:mf B:mp A3:f outro:p",

        "pattern step: @Bb1:1 r:2",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern sway: 1:1 5:1 3':1",
        "pattern toll: @F2:3",

        "track lead   role=lead    inst=horn|cello        vel=1.24 pan=-0.10",
        "track lead2  role=lead    inst=erhu|flute        vel=0.76 pan=0.15",
        "track harp   role=rhythm  inst=harp              vel=1.06 pan=0.30  ctr=Bb2 pat=sway",
        "track pad    role=harmony inst=strings|pad       vel=0.67 pan=0     ctr=D4 pat=padN",
        "track choir  role=harmony inst=choir             vel=0.78 pan=0     ctr=D4 pat=padN p=80",
        "track bass   role=bass    inst=cello             vel=1.05 pan=-0.15 ctr=Bb1 pat=root",
        "track taiko  role=perc    inst=taiko             vel=1.12 pan=0     ctr=Bb1 pat=step",
        "track bell   role=color   inst=bell              vel=0.87 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.68 pan=-0.3",

        "[intro]",
        "chords: Bb | Gm | Eb | F",
        "pad: padN x.6",
        "bass: root x.6",
        "bell: [Bb2 F3]:3 | r:3 | [Eb3 Bb3]:3 | r:3",
        "taiko: step x.6 @3-4",

        "[A]",
        "chords: Bb | Gm | Eb | F | Bb | Gm | Eb F | Bb",
        "lead: " + MEL_A,
        "harp: sway x.8",
        "pad: padN x.7",
        "bass: root x.8",
        "taiko: step",
        "bell: [Bb2 F3]:3 | r:3 | r:3 | r:3 | [Bb2 F3]:3 | r:3 | r:3 | r:3",
        "chime: r:3 | r:3 | r:3 | r:3 | r:3 | r:3 | r:3 | F6:3",

        "[A2 < A]",
        "lead: r:3",
        "lead2: " + MEL_A2,
        "choir: padN x.8",
        "pad: padN x.8",

        "[B]",
        "chords: Cm | Ab | Eb | Bb | Cm | Gm | F | F7",
        "lead: " + MEL_B,
        "lead2: -",
        "harp: sway x.9",
        "choir: padN x.8",
        "pad: padN x.8",
        "bass: root x.8",
        "taiko: step x.7",
        "bell: [C3 G3]:3 | r:3 | r:3 | r:3 | [C3 G3]:3 | r:3 | r:3 | r:3",
        "chime: -",

        "[A3 < A]",
        "lead: " + MEL_A,
        "lead2: " + SongTale.octave(MEL_A, 1),
        "choir: padN",
        "pad: padN x.9",
        "bass: root",
        "taiko: step",

        "[outro]",
        "chords: Bb | Gm | F | Bb",
        "lead: p D5:2 F5:1 | G5:1.5 F5:.5 D5:1 | C5:3 | Bb4:3",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: r:3 | r:3 | r:3 | [Bb2 F3 Bb3]:3",
        "chime: r:3 | r:3 | r:3 | Bb5:3",
    };
}
