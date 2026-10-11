package Game;

/** "The Watchman's Lamp": night as a vigil. G Dorian (the raised sixth, E natural, over the C chord is the sound of it), 4/4 at 54 bpm: one flute alone with a
 *  guqin that plucks only now and then, a cello drone, a wood block that strikes the hour once a bar, a bell at the end of the phrase, an erhu entering
 *  low in the middle and climbing to the lamp's own note. Very little happens, deliberately. Plays whatever its layers (`layers: fixed`). */
final class SongWatchman {
    private SongWatchman() { }

    // chords Gm | C | Gm | Dm | Gm | C | F | Gm
    private static final String MEL_A =
        "D5:3 G5:1 | E5:2 G5:2 | Bb5:2 G5:1 D5:1 | A5:3 F5:1 | G5:2 Bb5:2 | C6:2 G5:2 | A5:2 F5:2 | D5:4";
    // chords Eb | Bb | F | Gm | Eb | Bb | Dm | D
    private static final String MEL_B =
        "Bb5:2 G5:2 | D6:3 Bb5:1 | C6:2 A5:2 | D6:2 G6:2 | Eb6:2 Bb5:2 | F6:2 D6:2 | A5:2 F5:2 | F#5:2 A5:2";

    static final String[] LINES = {
        "title: The Watchman's Lamp",
        "bpm: 54",
        "beats: 4",
        "shift: -2 2",
        "moods: 3:5",
        "layers: fixed",
        "form: intro?:pp A:p B:mp A2:p outro:pp",

        "pattern pluckN: 1:2 5:1 3':1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern hour: @A5:.5 r:3.5",

        "track lead  role=lead    inst=flute|pluck     vel=1.07  pan=0.10",
        "track lead2 role=lead    inst=erhu|flute      vel=1.00  pan=-0.10",
        "track guqin role=rhythm  inst=pluck           vel=1.22  pan=0.30  ctr=G3 pat=pluckN p=85",
        "track pad   role=harmony inst=strings|pad     vel=1.15  pan=0     ctr=D4 pat=padN",
        "track drone role=bass    inst=cello|horn      vel=1.55  pan=-0.15 ctr=G2 pat=root",
        "track wood  role=perc    inst=wood            vel=1.06  pan=0.3   pat=hour",
        "track bell  role=color   inst=bell            vel=1.29  pan=0.2",
        "track chime role=color   inst=chime           vel=1.08  pan=-0.3",

        "[intro]",
        "chords: Gm | C | Gm | Dm",
        "drone: root x.5",
        "pad: padN x.4",
        "bell: [G2 D3]:4 | r:4 | r:4 | r:4",

        "[A]",
        "chords: Gm | C | Gm | Dm | Gm | C | F | Gm",
        "lead: " + MEL_A,
        "guqin: pluckN x.8",
        "pad: padN x.5",
        "drone: root x.7",
        "wood: hour",
        "bell: [G2 D3]:4 | r:4 | r:4 | r:4 | [G2 D3]:4 | r:4 | r:4 | r:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | D6:4",

        "[B]",
        "chords: Eb | Bb | F | Gm | Eb | Bb | Dm | D",
        "lead: r:4",
        "lead2: " + MEL_B,
        "guqin: pluckN",
        "pad: padN x.7",
        "drone: root x.8",
        "wood: hour",
        "bell: [Eb3 Bb3]:4",
        "chime: -",

        "[A2 < A]",
        "lead: " + MEL_A,
        "lead2: " + SongTale.octave(MEL_A, 0),
        "guqin: pluckN x.7",
        "pad: padN x.6",

        "[outro]",
        "chords: Gm | C | Dm | Gm",
        "lead: p D5:3 G5:1 | E5:2 G5:2 | A5:4 | G5:4",
        "guqin: pluckN x.5",
        "pad: padN x.4",
        "drone: root x.5",
        "bell: r:4 | r:4 | r:4 | [G2 D3 G3]:4",
        "chime: r:4 | r:4 | r:4 | G6:4",
    };
}
