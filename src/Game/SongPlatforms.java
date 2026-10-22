package Game;

/** "The Platforms Fall": the music of the Garden's Variety Tournament (not part of the music the places choose from; `EventMusic` pins it while the tournament is on screen). D minor, 4/4 at 92 bpm: bells and a choir
 *  open it as the sky does, a horn sings the theme over timpani and a bowed cello, an erhu lifts the second strain, a harp climbs, the chimes fall like the platforms. It plays whatever its layers (`layers: fixed`). */
final class SongPlatforms {
    private SongPlatforms() { }

    // chords Dm | Bb | F | C | Dm | Bb | Gm | A
    private static final String HORN_A =
        "D5:2 F5:1.5 A5:.5 | Bb5:2 A5:1 F5:1 | F5:2 A5:1 C6:1 | C6:3 G5:1"
      + " | D5:2 F5:1.5 A5:.5 | Bb5:2 D6:1 Bb5:1 | G5:2 Bb5:1 D6:1 | E6:2 Db6:2";
    // chords F | C | Dm | Bb | F | C | A | Dm
    private static final String ERHU_B =
        "A5:2 C6:2 | G5:2 E6:2 | F6:2 D6:2 | D6:1 F6:1 Bb5:2 | C6:2 A5:2 | E6:2 G6:2 | A6:2 Db6:2 | D6:4";

    static final String[] LINES = {
        "title: The Platforms Fall",
        "bpm: 92",
        "beats: 4",
        "shift: 0 0",
        "moods: 6:1",
        "layers: fixed",
        "form: intro:mp A:mf B:f A2:ff outro:mf",

        "pattern bow: b:*",
        "pattern swell: =1+=3+=5:*",
        "pattern stab: =1+=3+=5:2 r:2",
        "pattern climb: 1:1 3:1 5:1 1':1",
        "pattern roll: @D2:.5 @D2:.5 @D2:.5 @D2:.5 @D2:.5 @D2:.5 @D2:1",
        "pattern three: r:2 @D2:1 r:1",
        "pattern fall: r:3 @D6:.5 r:.5",

        "track horn  role=lead    inst=horn|cello      vel=1.35  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.75  pan=0.20",
        "track bow   role=bass    inst=cello           vel=0.85  pan=-0.15 ctr=D2 pat=bow",
        "track stab  role=harmony inst=strings         vel=0.50  pan=0.10  ctr=D3 pat=stab",
        "track choir role=harmony inst=choir           vel=0.80  pan=0     ctr=D4 pat=swell",
        "track climb role=rhythm  inst=harp            vel=0.62  pan=0.30  ctr=D3 pat=climb",
        "track timp  role=perc    inst=taiko           vel=0.80  pan=0     pat=three",
        "track fall  role=perc    inst=wood            vel=0.50  pan=0.3   pat=fall",
        "track bell  role=color   inst=bell            vel=0.85  pan=0.2",
        "track chime role=color   inst=chime           vel=0.65  pan=-0.3",

        "[intro]",
        "chords: Dm | Dm | A | A",
        "choir: swell x.5",
        "timp: roll x.6",
        "bell: [D3 A3]:4 | r:4 | [E3 A3]:4 | r:4",
        "chime: r:4 | r:4 | r:3 D6:1 | r:2 A6:1 r:1",

        "[A]",
        "chords: Dm | Bb | F | C | Dm | Bb | Gm | A",
        "horn: " + HORN_A,
        "bow: bow",
        "stab: stab",
        "choir: swell x.7",
        "timp: three",
        "bell: [D3 A3]:4 | r:4 | r:4 | r:4 | [D3 A3]:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | r:3 C6:1 | r:4 | r:4 | r:4 | A6:4",

        "[B]",
        "chords: F | C | Dm | Bb | F | C | A | Dm",
        "horn: -",
        "erhu: " + ERHU_B,
        "climb: climb",
        "bow: bow x.9",
        "stab: stab x.9",
        "choir: swell x.9",
        "timp: three x1.1",
        "fall: fall",
        "chime: r:4 | r:4 | r:4 | r:3 F6:1 | r:4 | r:4 | r:4 | D6:4",

        "[A2 < A]",
        "chords: Dm | Bb | F | C | Dm | Bb | Gm | A",
        "erhu: " + SongTale.octave(HORN_A, 1),
        "climb: climb",
        "choir: swell",
        "fall: fall",
        "timp: three x1.2",

        "[outro]",
        "chords: Dm | Bb | A | Dm",
        "horn: D5:2 F5:1.5 A5:.5 | Bb5:2 A5:1 F5:1 | E5:4 | D5:4",
        "erhu: -",
        "climb: -",
        "bow: bow x.6",
        "stab: -",
        "choir: swell x.5",
        "timp: roll x.5",
        "fall: -",
        "bell: r:4 | r:4 | r:4 | [D3 A3 D4]:4",
    };
}
