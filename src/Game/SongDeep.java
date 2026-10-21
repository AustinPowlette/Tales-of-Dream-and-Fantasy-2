package Game;

/** "What the Lantern Does Not Reach": a second song for danger. F-sharp minor in an uneasy 5/4 (84 bpm, felt 3 + 2): a low cello figure that stumbles
 *  instead of galloping, a taiko heartbeat with a hole in it, strings trembling, a horn that leans on the flat second (G over F-sharp) and never settles,
 *  an erhu climbing above in the middle, and a choir under the last statement. Plays whatever its layers (`layers: fixed`). */
final class SongDeep {
    private SongDeep() { }

    private static String rep(String s, int n) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(i == 0 ? "" : " ").append(s); return b.toString(); }

    // chords F#m | F#m | Gmaj7 | F#m | F#m | Dmaj7 | Gmaj7 | C#
    private static final String HORN_A =
        "F#3:3 A3:2 | C#4:3 A3:1 F#3:1 | G3:2 B3:2 D4:1 | A3:3 F#3:2"
      + " | F#3:3 A3:2 | D4:3 A3:2 | B3:3 D4:2 | C#4:3 G#3:2";
    // chords Bm | Gmaj7 | F#m | C# | Bm | Gmaj7 | C# | F#m
    private static final String ERHU_B =
        "F#5:3 D5:2 | B5:3 G5:2 | A5:3 F#5:2 | G#5:3 F5:2 | D6:3 B5:2 | G5:3 D5:2 | G#5:3 C#6:2 | F#5:5";

    static final String[] LINES = {
        "title: What the Lantern Does Not Reach",
        "bpm: 84",
        "beats: 5",
        "shift: -2 2",
        "moods: 2:5",
        "layers: fixed",
        "form: intro?:mp A:mf B:mf A2:f outro:mp",

        "pattern stumble: b:1 5:.5 b:.5 b:1 5:1 b:1",
        "pattern trem: " + rep("=1+=5:.5", 10),
        "pattern swell: =1+=3+=5:*",
        "pattern beat: @F#2:1 r:1 @F#2:.5 @F#2:.5 r:2",
        "pattern beat2: @F#2:.5 @F#2:.5 r:1 @F#2:1 r:1 @F#2:1",

        "track horn  role=lead    inst=horn|cello      vel=1.60  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.74  pan=0.20",
        "track ost   role=rhythm  inst=cello           vel=0.92  pan=-0.15 ctr=E2 pat=stumble",
        "track trem  role=harmony inst=strings         vel=0.59  pan=0.15  ctr=A3 pat=trem",
        "track choir role=harmony inst=choir           vel=0.71  pan=0     ctr=C#4 pat=swell p=85",
        "track taiko role=perc    inst=taiko           vel=0.90  pan=0     pat=beat",
        "track bell  role=color   inst=bell            vel=0.91  pan=0.2",
        "track chime role=color   inst=chime           vel=0.62  pan=-0.3",

        "[intro]",
        "chords: F#m | F#m | Gmaj7 | C#",
        "ost: stumble x.7",
        "bell: F#3:5",
        "horn: r:5 | r:5 | G3:5 | G#3:5",

        "[A]",
        "chords: F#m | F#m | Gmaj7 | F#m | F#m | Dmaj7 | Gmaj7 | C#",
        "horn: " + HORN_A,
        "ost: stumble",
        "taiko: beat",
        "bell: F#3:5 | r:5 | r:5 | r:5 | F#3:5",

        "[B]",
        "chords: Bm | Gmaj7 | F#m | C# | Bm | Gmaj7 | C# | F#m",
        "horn: -",
        "erhu: " + ERHU_B,
        "ost: stumble x.8",
        "trem: trem x.8",
        "choir: swell x.8",
        "taiko: beat x.7",
        "chime: r:5 | r:5 | r:5 | r:4 F6:1 | r:5 | r:5 | r:5 | A6:5",
        "bell: B2:5 | r:5 | r:5 | r:5 | B2:5",

        "[A2 < A]",
        "chords: F#m | F#m | Gmaj7 | F#m | F#m | Dmaj7 | Gmaj7 | C#",
        "erhu: r:5 | r:5 | r:5 | r:5 | A5:5 | A5:3 B5:2 | D6:5 | C#6:5",
        "trem: trem",
        "choir: swell",
        "taiko: beat2 x1.1",
        "chime: r:5 | r:5 | r:5 | r:4 F6:1 | r:5 | r:5 | r:5 | r:4 F6:1",

        "[outro]",
        "chords: F#m | Gmaj7 | C# | F#m",
        "horn: A3:5 | G3:5 | G#3:5 | F#3:5",
        "erhu: -",
        "ost: stumble x.6",
        "trem: trem x.5",
        "choir: -",
        "taiko: -",
        "bell: r:5 | r:5 | r:5 | F#3:5",
    };
}
