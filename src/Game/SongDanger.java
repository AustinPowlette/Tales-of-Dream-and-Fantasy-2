package Game;

/** "Where the Beasts Wait": the music of danger (dungeons, hotspots, monster lairs). C minor, 4/4, slow and heavy: a galloping low cello, taiko, strings
 *  trembling on the spot, a horn that cannot finish its phrase, an erhu keening high above, and a choir that swells at the climax. Plays whatever its layers. */
final class SongDanger {
    private SongDanger() { }

    private static String rep(String s, int n) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(i == 0 ? "" : " ").append(s); return b.toString(); }

    // chords Cm | Cm | Abmaj7 | G | Cm | Fm7 | Abmaj7 | G
    private static final String HORN_A =
        "G3:2 Eb4:2 | C4:4 | Ab3:2 Eb4:2 | D4:3 r:1 | G3:2 Eb4:1 G4:1 | F4:2 Eb4:1 C4:1 | Ab3:2 C4:2 | B3:2 D4:1 G3:1";
    // chords Cm | Abmaj7 | Fm7 | G | Cm | Abmaj7 | G | G
    private static final String ERHU_B =
        "G5:3 Eb5:1 | Eb5:2 C5:2 | F5:2 Ab5:2 | G5:2 D5:2 | G5:2 C6:2 | Ab5:3 G5:1 | B5:2 D6:2 | G5:4";

    static final String[] LINES = {
        "title: Where the Beasts Wait",
        "bpm: 76",
        "beats: 4",
        "shift: -2 2",
        "moods: 2:5",
        "layers: fixed",
        "form: intro?:mp A:mf B:mf A2:f outro:mp",

        "pattern gallop: b:.5 b:.5 5:.5 b:.5 b:.5 5:.5 b:.5 5:.5",
        "pattern trem: " + rep("=1+=5:.5", 8),
        "pattern swell: =1+=3+=5:*",
        "pattern boom: @C2:1 r:1 @C2:.5 @C2:.5 r:1",
        "pattern boom2: @C2:.5 @C2:.5 r:1 @C2:1 r:1",

        "track horn  role=lead    inst=horn|cello      vel=1.33  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.78  pan=0.20",
        "track ost   role=rhythm  inst=cello           vel=1.07  pan=-0.15 ctr=E2 pat=gallop",
        "track trem  role=harmony inst=strings         vel=0.62  pan=0.15  ctr=G3 pat=trem",
        "track choir role=harmony inst=choir           vel=0.75  pan=0     ctr=C4 pat=swell p=85",
        "track taiko role=perc    inst=taiko           vel=0.97  pan=0     pat=boom",
        "track bell  role=color   inst=bell            vel=1.02  pan=0.2",
        "track chime role=color   inst=chime           vel=0.67  pan=-0.3",

        "[intro]",
        "chords: Cm | Cm | Abmaj7 | G",
        "ost: gallop x.7",
        "bell: C3:4",
        "horn: r:4 | r:4 | Ab3:4 | G3:4",

        "[A]",
        "chords: Cm | Cm | Abmaj7 | G | Cm | Fm7 | Abmaj7 | G",
        "horn: " + HORN_A,
        "ost: gallop",
        "taiko: boom",
        "bell: C3:4 | r:4 | r:4 | r:4 | C3:4",

        "[B]",
        "chords: Cm | Abmaj7 | Fm7 | G | Cm | Abmaj7 | G | G",
        "horn: -",
        "erhu: " + ERHU_B,
        "ost: gallop x.8",
        "trem: trem x.8",
        "choir: swell x.8",
        "taiko: boom x.7",
        "chime: r:4 | r:4 | r:4 | r:3 B5:1 | r:4 | r:4 | r:4 | D6:4",
        "bell: C3:4 | r:4 | r:4 | r:4 | C3:4",

        "[A2 < A]",
        "chords: Cm | Cm | Abmaj7 | G | Cm | Fm7 | Abmaj7 | G",
        "erhu: r:4 | r:4 | r:4 | r:4 | G5:4 | Ab5:4 | C6:4 | B5:4",
        "trem: trem",
        "choir: swell",
        "taiko: boom2 x1.1",
        "chime: r:4 | r:4 | r:4 | r:3 B5:1 | r:4 | r:4 | r:4 | r:3 B5:1",

        "[outro]",
        "chords: Cm | Abmaj7 | G | Cm",
        "horn: G3:4 | Ab3:4 | G3:4 | C3:4",
        "erhu: -",
        "ost: gallop x.6",
        "trem: trem x.5",
        "choir: -",
        "taiko: -",
        "bell: r:4 | r:4 | r:4 | C3:4",
    };
}
