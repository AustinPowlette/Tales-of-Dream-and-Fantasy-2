package Game;

/** "The Forbidden Scripture": the sects in secret. C Dorian (the natural sixth, A, over the F chord is what keeps it from being plain minor), 4/4 at 50 bpm: a pipa
 *  that plays the tune alone, as if reading it, a whispering choir, a cello drone, one bell, an erhu that enters in the middle as a second reader and climbs
 *  through A-flat and E-flat to the G major chord at the door. Plays whatever its layers (`layers: fixed`). */
final class SongScripture {
    private SongScripture() { }

    // chords Cm | F | Cm | Gm | Cm | F | Bb | G
    private static final String MEL_A =
        "G4:1.5 C5:.5 Eb5:2 | A4:1.5 C5:.5 F5:2 | G5:1.5 Eb5:.5 C5:2 | D5:1.5 Bb4:.5 G4:2"
      + " | Eb5:1.5 G5:.5 C6:2 | A5:1.5 F5:.5 C5:2 | D5:1.5 F5:.5 Bb5:2 | D5:2 B4:2";
    // chords Ab | Eb | Bb | Gm | Ab | Eb | F | G
    private static final String MEL_B =
        "Eb5:2 Ab5:2 | G5:2 Bb5:2 | F5:2 D6:2 | D6:2 G5:2 | C6:2 Eb6:2 | Bb5:2 G5:2 | A5:2 C6:2 | B5:2 D6:2";

    // the second time: the end turns to the C minor chord (... Bb G | Cm)
    private static final String MEL_A2 =
        "G4:1.5 C5:.5 Eb5:2 | A4:1.5 C5:.5 F5:2 | G5:1.5 Eb5:.5 C5:2 | D5:1.5 Bb4:.5 G4:2"
      + " | Eb5:1.5 G5:.5 C6:2 | A5:1.5 F5:.5 C5:2 | D5:1 F5:1 G5:1 B4:1 | C5:3 r:1";

    static final String[] LINES = {
        "title: The Forbidden Scripture",
        "bpm: 50",
        "beats: 4",
        "shift: -2 2",
        "moods: 5:5",
        "layers: fixed",
        "form: intro?:pp A:p B:mp A2:mp outro:pp",

        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern breath: =1+=5:2 r:2",
        "pattern turn: 1:1 5:.5 3':.5 5:1 1':1",

        "track lead  role=lead    inst=pipa|pluck      vel=1.42  pan=0.20",
        "track lead2 role=lead    inst=erhu|flute      vel=0.99  pan=-0.15",
        "track harp  role=rhythm  inst=harp            vel=1.08  pan=0.30  ctr=C3 pat=turn p=60",
        "track pad   role=harmony inst=strings|pad     vel=1.00  pan=0     ctr=Eb4 pat=padN",
        "track choir role=harmony inst=choir           vel=1.18  pan=0     ctr=G3 pat=breath",
        "track drone role=bass    inst=cello|horn      vel=1.68  pan=-0.15 ctr=C2 pat=root",
        "track bell  role=color   inst=bell            vel=1.18  pan=0.2",
        "track chime role=color   inst=chime           vel=1.08  pan=-0.3",

        "[intro]",
        "chords: Cm | F | Cm | G",
        "drone: root x.5",
        "choir: breath x.5",
        "bell: [C3 G3]:4 | r:4 | r:4 | r:4",

        "[A]",
        "chords: Cm | F | Cm | Gm | Cm | F | Bb | G",
        "lead: " + MEL_A,
        "drone: root x.7",
        "choir: breath x.7",
        "pad: padN x.5",
        "bell: [C3 G3]:4 | r:4 | r:4 | r:4 | [C3 G3]:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | D6:4",

        "[B]",
        "chords: Ab | Eb | Bb | Gm | Ab | Eb | F | G",
        "lead: r:4",
        "lead2: " + MEL_B,
        "harp: turn",
        "drone: root x.8",
        "choir: breath x.9",
        "pad: padN x.7",
        "bell: [Ab2 Eb3]:4",
        "chime: -",

        "[A2 < A]",
        "chords: Cm | F | Cm | Gm | Cm | F | Bb G | Cm",
        "lead: " + MEL_A2,
        "lead2: -",
        "harp: turn",
        "choir: breath",

        "[outro]",
        "chords: Cm | F | G | Cm",
        "lead: p G4:1.5 C5:.5 Eb5:2 | A4:1.5 C5:.5 F5:2 | D5:4 | C5:4",
        "drone: root x.5",
        "choir: breath x.4",
        "bell: r:4 | r:4 | r:4 | [C3 G3 C4]:4",
        "chime: r:4 | r:4 | r:4 | C6:4",
    };
}
