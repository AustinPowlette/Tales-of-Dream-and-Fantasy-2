package Game;

/** "The Sleeper Stirs": danger as something enormous turning over. A Phrygian (the flat second, B-flat over A, is the sound of it), 4/4 at 44 bpm and almost
 *  without motion: a low cello drone, a wide choir breathing chords, one erhu note held until it frays, a horn walking down in fifths in the middle, a single
 *  far-off taiko hit, a bell once a phrase. Plays whatever its layers (`layers: fixed`). */
final class SongSleeper {
    private SongSleeper() { }

    // chords Am | Bb | Am | E | Am | Bb | Gm | Am
    private static final String ERHU_A =
        "E5:4 | F5:3 D5:1 | C5:2 E5:2 | B4:3 G#4:1 | A4:2 C5:2 | D5:2 F5:2 | G5:3 D5:1 | A4:4";
    // chords Bb | Am | Gm | E | Bb | Am | Gm | Am   (the horn walks down in fifths)
    private static final String HORN_B =
        "F3:4 | E3:4 | D3:4 | E3:4 | F3:4 | E3:4 | D3:4 | A3:4";

    static final String[] LINES = {
        "title: The Sleeper Stirs",
        "bpm: 44",
        "beats: 4",
        "shift: -2 2",
        "moods: 2:5",
        "layers: fixed",
        "form: intro?:pp A:p B:mp A2:mf outro:pp",

        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern slow: 1:2 5:2",
        "pattern far: @A1:1 r:3",

        "track erhu  role=lead    inst=erhu|flute      vel=0.90  pan=0.20",
        "track horn  role=lead    inst=horn|cello      vel=1.87  pan=-0.10",
        "track drone role=bass    inst=cello           vel=1.12  pan=-0.15 ctr=A2 pat=root",
        "track pad   role=harmony inst=strings|pad     vel=0.76  pan=0     ctr=C4 pat=padN",
        "track choir role=harmony inst=choir           vel=0.89  pan=0     ctr=C4 pat=padN",
        "track harp  role=rhythm  inst=harp            vel=1.20  pan=0.30  ctr=A3 pat=slow p=70",
        "track taiko role=perc    inst=taiko           vel=1.00  pan=0     pat=far",
        "track bell  role=color   inst=bell            vel=1.22  pan=0.2",
        "track chime role=color   inst=chime           vel=0.61  pan=-0.3",

        "[intro]",
        "chords: Am | Bb | Am | E",
        "drone: root x.6",
        "choir: padN x.5",
        "bell: A3:4 | r:4 | r:4 | r:4",

        "[A]",
        "chords: Am | Bb | Am | E | Am | Bb | Gm | Am",
        "erhu: " + ERHU_A,
        "drone: root x.8",
        "choir: padN x.7",
        "pad: padN x.5",
        "taiko: far",
        "bell: A3:4 | r:4 | r:4 | r:4 | A3:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | E6:4",

        "[B]",
        "chords: Bb | Am | Gm | E | Bb | Am | Gm | Am",
        "erhu: -",
        "horn: " + HORN_B,
        "drone: root",
        "choir: padN x.9",
        "pad: padN x.7",
        "harp: slow",
        "taiko: far x1.1",
        "bell: F3:4 | r:4 | r:4 | r:4 | F3:4",
        "chime: -",

        "[A2 < A]",
        "horn: -",
        "choir: padN",
        "pad: padN x.8",
        "harp: slow x.8",
        "taiko: far x1.3",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | Bb6:4",

        "[outro]",
        "chords: Am | Bb | E | Am",
        "erhu: p E5:4 | F5:4 | G#5:4 | A5:4",
        "horn: -",
        "drone: root x.5",
        "choir: padN x.4",
        "taiko: -",
        "bell: r:4 | r:4 | r:4 | A3:4",
    };
}
