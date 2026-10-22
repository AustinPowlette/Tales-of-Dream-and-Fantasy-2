package Game;

/** "March of the Broken Banners": the music of war (crowd hotspots). G minor, 4/4, 108 bpm: a horn fanfare of short-short-long calls over a chugging
 *  cello and a marching taiko, string stabs, a high erhu lament in the middle, and the full band for the last statement. Plays whatever its layers. */
final class SongWar {
    private SongWar() { }

    private static String rep(String s, int n) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(i == 0 ? "" : " ").append(s); return b.toString(); }

    // chords Gm | Ebmaj7 | F | Gm | Gm | Ebmaj7 | D | Gm
    private static final String HORN_A =
        "G4:1.5 G4:.5 D5:1 G5:1 | Bb4:1.5 Bb4:.5 G5:1 F5:1 | A4:1.5 A4:.5 C5:1 F5:1 | D5:3 r:1"
      + " | Bb4:1.5 Bb4:.5 D5:1 G5:1 | Eb5:1.5 Eb5:.5 G5:1 Bb5:1 | A5:1.5 A5:.5 F#5:1 D5:1 | G5:3 r:1";
    // chords Cm | Gm/Bb | Ab | D | Cm | Eb/G | F | D
    private static final String ERHU_B =
        "G5:2 C6:1.5 Bb5:.5 | D6:2 Bb5:2 | C6:2 Eb6:2 | F#5:2 A5:2 | G5:2 C6:2 | Bb5:2 G5:2 | A5:2 C6:2 | D6:3 r:1";

    static final String[] LINES = {
        "title: March of the Broken Banners",
        "bpm: 108",
        "beats: 4",
        "shift: -2 2",
        "moods: 4:5",
        "layers: fixed",
        "form: intro:mf A:mf B:f A2:ff outro:mf",

        "pattern chug: " + rep("1:.5", 8),
        "pattern stab: =1+=3+=5:1.5 r:.5 =1+=3+=5:1.5 r:.5",
        "pattern ping: " + rep("=1:.5 =5:.5 =3:.5 =5:.5", 2),
        "pattern swell: =1+=3+=5:*",
        "pattern march: @G2:1 @G2:.5 @G2:.5 @G2:1 @G2:.5 @G2:.5",
        "pattern snap: r:1 @C6:1 r:1 @C6:1",

        "track horn  role=lead    inst=horn|erhu       vel=1.15  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.59  pan=0.20",
        "track chug  role=rhythm  inst=cello           vel=1.03  pan=-0.20 ctr=G2 pat=chug",
        "track stab  role=rhythm  inst=strings         vel=0.49  pan=0.10  ctr=A3 pat=stab",
        "track pipa  role=rhythm  inst=pipa            vel=0.74  pan=0.35  ctr=D4 pat=ping",
        "track choir role=harmony inst=choir           vel=0.56  pan=0     ctr=C4 pat=swell p=85",
        "track taiko role=perc    inst=taiko           vel=0.70  pan=0     pat=march",
        "track wood  role=perc    inst=wood            vel=0.58  pan=0.3   pat=snap",
        "track bell  role=color   inst=bell            vel=0.72  pan=0.2",

        "[intro]",
        "chords: Gm | Gm | Ebmaj7 | D",
        "chug: chug x.7",
        "taiko: march x.8",
        "horn: r:4 | r:4 | Bb4:1.5 Bb4:.5 G5:2 | D5:4",

        "[A]",
        "chords: Gm | Ebmaj7 | F | Gm | Gm | Ebmaj7 | D | Gm",
        "horn: " + HORN_A,
        "chug: chug",
        "stab: stab",
        "pipa: ping",
        "taiko: march",
        "bell: [G2 D3]:4",

        "[B]",
        "chords: Cm | Gm/Bb | Ab | D | Cm | Eb/G | F | D",
        "horn: -",
        "erhu: " + ERHU_B,
        "chug: chug x.8",
        "taiko: march x.7",
        "wood: snap",
        "bell: [C3 G3]:4",

        "[A2 < A]",
        "chords: Gm | Ebmaj7 | F | Gm | Gm | Ebmaj7 | D | Gm",
        "erhu: D6:4 | Bb5:4 | C6:4 | D6:4 | Bb5:4 | Bb5:4 | A5:4 | G5:4",
        "choir: swell",
        "wood: snap",
        "taiko: march x1.1",
        "bell: [G2 D3]:4",

        "[outro]",
        "chords: Gm | Eb | D | Gm",
        "horn: G4:1.5 G4:.5 D5:2 | Bb4:4 | A4:4 | G4:4",
        "stab: -",
        "pipa: -",
        "chug: chug x.6 @1-3",
        "taiko: march x.7 @1-2",
        "bell: r:4 | r:4 | r:4 | [G2 D3 G3]:4",
    };
}
