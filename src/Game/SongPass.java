package Game;

/** "Beyond the Last Pass": open country, vast and alone. G Mixolydian (the flat seventh, F natural, never fixed) in a slow 4/4 at 58 bpm: a horn calls an open
 *  fifth and a flat seventh (G D F), a flute answers an octave higher, a harp drones, a cello holds the floor, a wide hushed choir comes in with the erhu
 *  in the middle, and a single slow taiko heartbeat on the last time. Plays with the usual layers. */
final class SongPass {
    private SongPass() { }

    // chords G | F | C | G | Am7 | F | C | G
    private static final String CALL =
        "G4:2 D5:2 | F5:3 C5:1 | E5:2 G5:2 | D5:3 r:1"
      + " | C5:2 E5:1 G5:1 | A5:2 C6:2 | G5:2 E5:1 C5:1 | D5:2 B4:2";
    // chords Em | Bm | C | D | Em | C | D | G
    private static final String ANSWER =
        "G5:2 B5:2 | F#5:2 D5:2 | E5:2 G5:2 | F#5:3 A5:1 | B5:2 G5:2 | E5:2 C5:2 | A5:2 F#5:2 | G5:4";

    static final String[] LINES = {
        "title: Beyond the Last Pass",
        "bpm: 58",
        "beats: 4",
        "shift: -2 2",
        "moods: 0:3 3:1 5:1",
        "form: intro?:pp A:p B:mp A2:mp outro:pp",

        "pattern drone: 1:2 5:2",
        "pattern arp: 1:1 5:1 1':1 5:1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern heart: @G2:1 r:3",

        "track lead   role=lead    inst=horn|cello        vel=1.37 pan=-0.10",
        "track lead2  role=lead    inst=flute|erhu        vel=0.82 pan=0.20",
        "track erhu   role=counter inst=erhu|flute        vel=0.88 pan=0.10",
        "track harp   role=rhythm  inst=harp              vel=1.28 pan=0.30  ctr=G3 pat=arp",
        "track pad    role=harmony inst=strings|pad       vel=0.90 pan=0     ctr=D4 pat=padN",
        "track choir  role=harmony inst=choir             vel=1.11 pan=0     ctr=D4 pat=padN p=75",
        "track bass   role=bass    inst=cello             vel=1.35 pan=-0.15 ctr=G2 pat=root",
        "track bell   role=color   inst=bell              vel=1.12 pan=0.2",
        "track chime  role=color   inst=chime             vel=1.01 pan=-0.3",
        "track taiko  role=perc    inst=taiko             vel=1.16 pan=0     ctr=G2 pat=heart",

        "[intro]",
        "chords: G | F | G | F",
        "harp: arp x.6",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: [G2 D3]:4",
        "lead: r:4 | r:4 | G4:4 | F4:4",

        "[A]",
        "chords: G | F | C | G | Am7 | F | C | G",
        "lead: " + CALL,
        "lead2: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4",
        "harp: arp x.7",
        "pad: padN x.55",
        "bass: root x.7",
        "bell: [G2 D3]:4 | r:4 | r:4 | r:4 | [A2 E3]:4",
        "chime: r:4 | r:4 | r:4 | r:3 D6:1 | r:4 | r:4 | r:4 | B5:4",

        "[B]",
        "chords: Em | Bm | C | D | Em | C | D | G",
        "lead: r:4",
        "erhu: " + ANSWER,
        "harp: arp x.8",
        "pad: padN x.8",
        "choir: padN x.8",
        "bass: root x.8",
        "bell: [E3 B3]:4",
        "chime: -",

        "[A2 < A]",
        "lead: " + CALL,
        "lead2: " + SongTale.octave(CALL, 1),
        "erhu: -",
        "choir: padN x.9",
        "pad: padN x.7",
        "taiko: heart",

        "[outro]",
        "chords: G | F | C | G",
        "lead: p G4:2 D5:2 | F5:3 C5:1 | E5:4 | D5:4",
        "harp: arp x.5",
        "pad: padN x.4",
        "bass: root x.5",
        "bell: r:4 | r:4 | r:4 | [G2 D3 G3]:4",
        "chime: r:4 | r:4 | r:4 | G6:4",
    };
}
