package Game;

/** "Gates of Originis": the capital as a fanfare. E major, 4/4 at 84 bpm: horns announcing the city with the open fifth and the octave (E B E), strings
 *  answering, a choir swelling under the second phrase, a taiko on the downbeat like a gate closing, a bell on the first beat, and a last statement with the erhu
 *  an octave above. The grandest of the capital's songs. Plays with the usual layers. */
final class SongGates {
    private SongGates() { }

    // chords E | B | C#m | A | E | B | A | B
    private static final String HORN_A =
        "E4:1 B4:1 E5:2 | D#5:1.5 B4:.5 F#4:2 | C#5:1 E5:1 G#5:2 | A5:1 E5:1 C#5:2"
      + " | E5:1 G#5:1 B5:2 | D#6:1 B5:1 F#5:2 | C#6:1 A5:1 E5:2 | F#5:2 D#5:2";
    // the end turns home (chords ... A | B E)
    private static final String HORN_A2 =
        "E4:1 B4:1 E5:2 | D#5:1.5 B4:.5 F#4:2 | C#5:1 E5:1 G#5:2 | A5:1 E5:1 C#5:2"
      + " | E5:1 G#5:1 B5:2 | D#6:1 B5:1 F#5:2 | C#6:1 A5:1 E5:2 | D#5:1 F#5:1 B4:1 E5:1";
    // chords A | E | F#m | B | A | E/G# | F#m | B
    private static final String STR_B =
        "C#6:2 E6:2 | B5:2 G#5:2 | A5:2 C#6:2 | D#6:2 B5:2 | E6:2 C#6:2 | G#5:2 B5:2 | C#6:2 A5:2 | F#5:4";

    static final String[] LINES = {
        "title: Gates of Originis",
        "bpm: 84",
        "beats: 4",
        "shift: -2 2",
        "moods: 6:5 5:1",
        "form: intro?:mp A:mf B:mf A2:f outro:mp",

        "pattern pulse: @E1:1 r:3",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern climb: 1:1 3:1 5:1 1':1",
        "pattern timpG: b:1 r:1 5:1 r:1",
        "pattern snareG: r:1 @snare:1 r:1 @snare^:.5 @snare:.5",
        "pattern tambG: r:.5 @tamb:.5 r:.5 @tamb^:.5 r:.5 @tamb:.5 r:.5 @tamb^:.5",

        "track horn   role=lead    inst=horn|cello        vel=1.34 pan=-0.10",
        "track erhu   role=lead    inst=erhu|flute        vel=0.75 pan=0.20",
        "track strings role=counter inst=strings|pad      vel=0.75 pan=0.10",
        "track harp   role=rhythm  inst=harp              vel=0.78 pan=0.30  ctr=E3 pat=climb",
        "track pad    role=harmony inst=pad|strings       vel=0.61 pan=0     ctr=G#3 pat=padN",
        "track choir  role=harmony inst=choir             vel=0.71 pan=0     ctr=B3 pat=padN p=85",
        "track bass   role=bass    inst=cello             vel=0.92 pan=-0.15 ctr=E2 pat=root",
        "track taiko  role=perc    inst=taiko             vel=1.00 pan=0     ctr=E1 pat=pulse",
        "track bell   role=color   inst=bell              vel=0.83 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.64 pan=-0.3",
        "track timp  role=perc    inst=timpani  vel=0.88 pan=0    ctr=E2 pat=timpG",
        "track snare role=perc    inst=kit      vel=0.69 pan=0.1  ctr=C2 pat=snareG",
        "track tamb  role=perc    inst=kit      vel=0.88 pan=0.3  ctr=C2 pat=tambG",
        "track gong  role=perc    inst=kit      vel=0.67 pan=0",

        "[intro]",
        "chords: E | B | E | B",
        "timp: timpG x.7",
        "gong: gong>:4 | r:4 | r:4 | r:4",
        "pad: padN x.6",
        "bass: root x.6",
        "taiko: pulse x.7",
        "bell: [E2 B2]:4 | r:4 | [E2 B2]:4 | r:4",
        "horn: r:4 | r:4 | r:4 | F#4:4",

        "[A]",
        "chords: E | B | C#m | A | E | B | A | B",
        "timp: timpG",
        "gong: gong>:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4",
        "horn: " + HORN_A,
        "harp: climb x.8",
        "pad: padN x.65",
        "bass: root x.8",
        "taiko: pulse",
        "bell: [E2 B2]:4 | r:4 | r:4 | r:4 | [E2 B2]:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | r:3 E6:1 | r:4 | r:4 | r:4 | r:3 B6:1",

        "[B]",
        "chords: A | E | F#m | B | A | E/G# | F#m | B",
        "timp: timpG x.8",
        "snare: snareG",
        "tamb: tambG",
        "gong: r:4 | r:4 | r:4 | r:4 | crash>:4 | r:4 | r:4 | r:4",
        "horn: r:4",
        "erhu: " + STR_B,
        "strings: " + SongTale.octave(STR_B, -1),
        "harp: climb",
        "pad: padN x.85",
        "choir: padN x.8",
        "bass: root",
        "taiko: pulse x1.1",
        "bell: [A2 E3]:4 | r:4 | r:4 | r:4 | [A2 E3]:4 | r:4 | r:4 | r:4",
        "chime: -",

        "[A2 < A]",
        "chords: E | B | C#m | A | E | B | A | B E",
        "timp: timpG x1.1",
        "snare: snareG",
        "tamb: tambG",
        "gong: crash>:4 | r:4 | r:4 | r:4 | crash>:4 | r:4 | r:4 | tom3:.5 tom3:.5 tom4:.5 tom4:.5 tom5:1 tom5:1",
        "horn: " + HORN_A2,
        "erhu: " + SongTale.octave(HORN_A2, 1),
        "strings: -",
        "harp: climb",
        "choir: padN",
        "pad: padN x.9",
        "taiko: pulse x1.2",

        "[outro]",
        "chords: E | B | A | E",
        "timp: timpG x.6",
        "gong: gong>:4 | r:4 | r:4 | r:4",
        "horn: E5:1 B4:1 E5:2 | D#5:1.5 B4:.5 F#4:2 | C#5:4 | E4:4",
        "erhu: -",
        "harp: climb x.5",
        "pad: padN x.6",
        "choir: -",
        "bass: root x.6",
        "taiko: pulse x.6",
        "bell: r:4 | r:4 | r:4 | [E2 B2 E3]:4",
        "chime: r:4 | r:4 | r:4 | E6:4",
    };
}
