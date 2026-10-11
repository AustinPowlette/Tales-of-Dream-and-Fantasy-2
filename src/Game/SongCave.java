package Game;

/** "Eyes in the Cave": danger as something that waits. B-flat minor, 4/4 at 60 bpm and very sparse: a cello creeping on the spot with a gap where the step should be,
 *  strings trembling, a heartbeat taiko that doubles up and stops, a single bell dripping at odd places, a low horn that never rises above a fifth, a lone erhu
 *  high above in the middle. Nothing gallops; the danger is that nothing happens. Plays whatever its layers (`layers: fixed`). */
final class SongCave {
    private SongCave() { }

    private static String rep(String s, int n) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(i == 0 ? "" : " ").append(s); return b.toString(); }

    // chords Bbm | Bbm | Gbmaj7 | F | Bbm | Dbmaj7 | Gb | F
    private static final String HORN_A =
        "Bb3:3 Db4:1 | F4:2 Db4:1 Bb3:1 | Gb3:2 Bb3:2 | A3:3 F3:1"
      + " | Bb3:3 Db4:1 | Db4:2 F4:2 | Gb4:2 Db4:2 | C4:2 A3:2";
    // chords Gb | F | Bbm | Dbmaj7 | Gb | F | Bbm | F
    private static final String ERHU_B =
        "Bb5:3 Gb5:1 | A5:3 F5:1 | Db6:2 F6:2 | C6:3 Ab5:1 | Bb5:2 Db6:2 | C6:3 A5:1 | F5:2 Bb5:2 | A5:4";

    static final String[] LINES = {
        "title: Eyes in the Cave",
        "bpm: 60",
        "beats: 4",
        "shift: -2 2",
        "moods: 2:5",
        "layers: fixed",
        "form: intro?:p A:mp B:mp A2:mf outro:p",

        "pattern creep: b:1 r:1 b:.5 5:.5 r:1",
        "pattern trem: " + rep("=1+=5:.5", 8),
        "pattern swell: =1+=3+=5:*",
        "pattern beat: @Bb1:1 r:.5 @Bb1:1 r:1.5",
        "pattern beat2: @Bb1:1 r:.5 @Bb1:1 r:.5 @Bb1:1",

        "track horn  role=lead    inst=horn|cello      vel=2.12  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.86  pan=0.20",
        "track ost   role=rhythm  inst=cello           vel=1.16  pan=-0.15 ctr=E2 pat=creep",
        "track trem  role=harmony inst=strings         vel=0.74  pan=0.15  ctr=F3 pat=trem",
        "track choir role=harmony inst=choir           vel=0.89  pan=0     ctr=Db4 pat=swell p=80",
        "track taiko role=perc    inst=taiko           vel=1.22  pan=0     pat=beat",
        "track bell  role=color   inst=bell            vel=1.16  pan=0.2",
        "track chime role=color   inst=chime           vel=0.78  pan=-0.3",

        "[intro]",
        "chords: Bbm | Bbm | Gb | F",
        "ost: creep x.6",
        "bell: r:4 | Bb3:4 | r:4 | r:4",

        "[A]",
        "chords: Bbm | Bbm | Gbmaj7 | F | Bbm | Dbmaj7 | Gb | F",
        "horn: " + HORN_A,
        "ost: creep",
        "taiko: beat x.8",
        "bell: r:2 Bb3:2 | r:4 | r:4 | r:4 | r:3 Bb3:1 | r:4 | r:4 | r:4",

        "[B]",
        "chords: Gb | F | Bbm | Dbmaj7 | Gb | F | Bbm | F",
        "horn: -",
        "erhu: " + ERHU_B,
        "ost: creep x.8",
        "trem: trem x.8",
        "choir: swell x.8",
        "taiko: beat x.7",
        "chime: r:4 | r:4 | r:4 | r:3 F6:1 | r:4 | r:4 | r:4 | F6:4",
        "bell: Gb3:4 | r:4 | r:4 | r:4 | Gb3:4",

        "[A2 < A]",
        "chords: Bbm | Bbm | Gbmaj7 | F | Bbm | Dbmaj7 | Gb | F",
        "erhu: r:4 | r:4 | r:4 | r:4 | F5:4 | Ab5:4 | Bb5:4 | C6:4",
        "trem: trem",
        "choir: swell",
        "taiko: beat2",
        "chime: r:4 | r:4 | r:4 | r:3 F6:1 | r:4 | r:4 | r:4 | r:3 F6:1",

        "[outro]",
        "chords: Bbm | Gb | F | Bbm",
        "horn: Bb3:4 | Gb3:4 | F3:4 | Bb2:4",
        "erhu: -",
        "ost: creep x.5",
        "trem: trem x.4",
        "choir: -",
        "taiko: -",
        "bell: r:4 | r:4 | r:4 | Bb3:4",
    };
}
