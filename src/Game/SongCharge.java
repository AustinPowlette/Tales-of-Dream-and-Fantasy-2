package Game;

/** "The Last Charge": war as a sacrifice. F minor with a turn to D-flat and A-flat, 4/4 at 88 bpm in a heavy half-time: a horn singing the tune above a bowed
 *  cello, a taiko that hits only on the third beat, long choir chords, strings climbing in the middle, a bell tolling the first bar of each phrase; the last
 *  time the tune is doubled an octave up by the erhu. It is the sound of people who know. Plays whatever its layers (`layers: fixed`). */
final class SongCharge {
    private SongCharge() { }

    // chords Fm | Db | Ab | Eb | Fm | Db | Bbm | C
    private static final String HORN_A =
        "C5:2 F5:1.5 Ab5:.5 | Db5:2 F5:1 Ab5:1 | C6:2 Ab5:1 Eb5:1 | Eb5:3 G5:1"
      + " | C5:2 F5:1.5 Ab5:.5 | Db6:2 Ab5:1 F5:1 | Bb5:2 Db6:1 Bb5:1 | C6:2 G5:2";
    // chords Db | Ab | Eb | Fm | Db | Ab | C | Fm
    private static final String ERHU_B =
        "F5:2 Ab5:2 | C6:2 Eb6:2 | Bb5:2 G5:2 | F5:2 C6:2 | Db6:2 F6:2 | Eb6:2 C6:2 | E6:2 C6:2 | F5:4";

    static final String[] LINES = {
        "title: The Last Charge",
        "bpm: 88",
        "beats: 4",
        "shift: -2 2",
        "moods: 4:5",
        "layers: fixed",
        "form: intro:mf A:mf B:f A2:ff outro:mf",

        "pattern bow: b:*",
        "pattern swell: =1+=3+=5:*",
        "pattern stab: =1+=3+=5:2 r:2",
        "pattern climb: 1:1 3:1 5:1 1':1",
        "pattern three: r:2 @F2:1 r:1",
        "pattern snap: r:3 @C6:.5 r:.5",

        "track horn  role=lead    inst=horn|cello      vel=1.38  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.65  pan=0.20",
        "track bow   role=bass    inst=cello           vel=0.82  pan=-0.15 ctr=F2 pat=bow",
        "track stab  role=harmony inst=strings         vel=0.48  pan=0.10  ctr=C4 pat=stab",
        "track choir role=harmony inst=choir           vel=0.72  pan=0     ctr=C4 pat=swell",
        "track climb role=rhythm  inst=harp            vel=0.64  pan=0.30  ctr=F3 pat=climb",
        "track taiko role=perc    inst=taiko           vel=0.80  pan=0     pat=three",
        "track wood  role=perc    inst=wood            vel=0.60  pan=0.3   pat=snap",
        "track bell  role=color   inst=bell            vel=0.80  pan=0.2",
        "track chime role=color   inst=chime           vel=0.60  pan=-0.3",

        "[intro]",
        "chords: Fm | Db | Ab | C",
        "bow: bow x.6",
        "choir: swell x.5",
        "bell: [F2 C3]:4 | r:4 | r:4 | r:4",

        "[A]",
        "chords: Fm | Db | Ab | Eb | Fm | Db | Bbm | C",
        "horn: " + HORN_A,
        "bow: bow",
        "stab: stab",
        "choir: swell x.7",
        "taiko: three",
        "bell: [F2 C3]:4 | r:4 | r:4 | r:4 | [F2 C3]:4 | r:4 | r:4 | r:4",

        "[B]",
        "chords: Db | Ab | Eb | Fm | Db | Ab | C | Fm",
        "horn: -",
        "erhu: " + ERHU_B,
        "climb: climb",
        "bow: bow x.9",
        "stab: stab x.9",
        "choir: swell x.9",
        "taiko: three x1.1",
        "wood: snap",
        "chime: r:4 | r:4 | r:4 | r:3 C6:1 | r:4 | r:4 | r:4 | F6:4",

        "[A2 < A]",
        "chords: Fm | Db | Ab | Eb | Fm | Db | Bbm | C",
        "erhu: " + SongTale.octave(HORN_A, 1),
        "climb: climb",
        "choir: swell",
        "wood: snap",
        "taiko: three x1.2",

        "[outro]",
        "chords: Fm | Db | C | Fm",
        "horn: C5:2 F5:1.5 Ab5:.5 | Db5:2 F5:1 Ab5:1 | G5:4 | F5:4",
        "erhu: -",
        "climb: -",
        "bow: bow x.6",
        "stab: -",
        "choir: swell x.5",
        "taiko: three x.7",
        "bell: r:4 | r:4 | r:4 | [F2 C3 F3]:4",
    };
}
