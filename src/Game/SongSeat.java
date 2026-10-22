package Game;

/** "The Voice in the Empty Seat": the capital as a question. F minor and A-flat major leaning on each other (the chords never decide which one this is), a slow 4/4
 *  at 60 bpm: a piano saying a few notes and waiting, a hushed choir, an erhu answering a sixth above in the middle, a cello pedal, a sparkling chime at the
 *  end of a phrase and no drums. It is the song of a voice that is not allowed to say who it is. Plays with the usual layers. */
final class SongSeat {
    private SongSeat() { }

    // chords Fm | Dbmaj7 | Ab | Ebsus4 | Fm | Dbmaj7 | Bbm | C
    private static final String MEL_A =
        "C5:3 Ab4:1 | Db5:2 F5:2 | Eb5:2 C5:2 | Bb4:2 Ab4:2"
      + " | F5:3 Ab5:1 | Ab5:2 F5:2 | Db6:2 Bb5:2 | G5:2 E5:2";
    // chords Ab | Eb | Fm | Db | Ab | Eb | Bbm | C
    private static final String MEL_B =
        "C6:3 Eb6:1 | Bb5:2 G5:2 | F5:2 Ab5:2 | F5:2 Ab5:2 | Eb6:2 C6:2 | G5:2 Bb5:2 | F5:2 Db5:2 | E5:2 G5:2";

    static final String[] LINES = {
        "title: The Voice in the Empty Seat",
        "bpm: 60",
        "beats: 4",
        "shift: -2 2",
        "moods: 6:4 5:1 3:1",
        "form: intro?:pp A:p B:mp A2:mp outro:pp",

        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern slow: 1:2 5:1 3':1",

        "track lead  role=lead    inst=piano|pluck     vel=1.89  pan=-0.10",
        "track lead2 role=lead    inst=erhu|flute      vel=1.04  pan=0.20",
        "track harp  role=rhythm  inst=harp            vel=1.55  pan=0.35  ctr=F3 pat=slow p=70",
        "track pad   role=harmony inst=strings|pad     vel=0.95  pan=0     ctr=C4 pat=padN",
        "track choir role=harmony inst=choir           vel=1.35  pan=0     ctr=Ab3 pat=padN",
        "track bass  role=bass    inst=cello           vel=1.47  pan=-0.15 ctr=F2 pat=root",
        "track bell  role=color   inst=bell            vel=1.22  pan=0.2",
        "track chime role=color   inst=chime           vel=0.96  pan=-0.3",

        "[intro]",
        "chords: Fm | Ab | Fm | Ebsus4",
        "pad: padN x.5",
        "choir: padN x.4",
        "bell: [F2 C3]:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | Ab6:4",

        "[A]",
        "chords: Fm | Dbmaj7 | Ab | Ebsus4 | Fm | Dbmaj7 | Bbm | C",
        "lead: " + MEL_A,
        "harp: slow x.6",
        "pad: padN x.6",
        "choir: padN x.5",
        "bass: root x.7",
        "bell: [F2 C3]:4 | r:4 | r:4 | r:4 | [F2 C3]:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | E6:4",

        "[B]",
        "chords: Ab | Eb | Fm | Db | Ab | Eb | Bbm | C",
        "lead: r:4",
        "lead2: " + MEL_B,
        "harp: slow x.8",
        "pad: padN x.8",
        "choir: padN x.8",
        "bass: root x.8",
        "bell: [Ab2 Eb3]:4",
        "chime: -",

        "[A2 < A]",
        "chords: Fm | Dbmaj7 | Ab | Ebsus4 | Fm | Dbmaj7 | Bbm | Fm",
        "lead: " + MEL_A.replace("| G5:2 E5:2", "| C6:2 F5:2"),
        "lead2: " + SongTale.octave(MEL_A.replace("| G5:2 E5:2", "| C6:2 F5:2"), 0),
        "harp: slow x.7",
        "choir: padN x.9",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | F6:4",

        "[outro]",
        "chords: Fm | Ab | Ebsus4 | Fm",
        "lead: p C5:3 Ab4:1 | Eb5:2 C5:2 | Bb4:4 | F4:4",
        "harp: slow x.5",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: r:4 | r:4 | r:4 | [F2 C3 F3]:4",
        "chime: r:4 | r:4 | r:4 | F6:4",
    };
}
