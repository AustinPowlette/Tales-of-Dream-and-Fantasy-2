package Game;

/** "Disciples at Dawn": the sects in the first light. G major in a gentle six-eight (a bar is two dotted-quarter beats, 54 bpm): a harp rolling the chord in sixes,
 *  a flute singing a tune that climbs the triad and comes back (D G B, then A F# D), a bell for each bar of the second phrase, an erhu answering in
 *  the middle, a warm pad and a cello that walks the bass down the scale. Hopeful, not grand. Plays whatever its layers (`layers: fixed`). */
final class SongDawn {
    private SongDawn() { }

    // chords G | D/F# | Em | C | G | Am7 | D | G
    private static final String MEL_A =
        "D5:1 G5:2/3 B5:1/3 | A5:1 F#5:2/3 D5:1/3 | B4:1 E5:2/3 G5:1/3 | C6:1 G5:2/3 E5:1/3"
      + " | D5:1 G5:2/3 B5:1/3 | C6:1 A5:2/3 E5:1/3 | F#5:1 A5:2/3 D6:1/3 | B5:2";
    // chords C | G/B | Am | D | C | G/B | Am D | G
    private static final String MEL_B =
        "E5:1 G5:1 | B4:1 G5:1 | C6:1 A5:1 | F#5:1 A5:1 | G5:1 C6:1 | B5:1 D6:1 | C6:1 A5:1 | G5:2";

    static final String[] LINES = {
        "title: Disciples at Dawn",
        "bpm: 54",
        "beats: 2",
        "shift: -2 2",
        "moods: 5:5",
        "layers: fixed",
        "form: intro?:p A:mp B:mp A2:mf outro:p",

        "pattern roll6: 1:1/3 5:1/3 3':1/3 1':1/3 3':1/3 5:1/3",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern walk: b:1 5:1",

        "track lead  role=lead    inst=flute|pluck     vel=0.74  pan=0.10",
        "track lead2 role=lead    inst=erhu|flute      vel=0.91  pan=-0.10",
        "track harp  role=rhythm  inst=harp            vel=1.05  pan=0.30  ctr=G3 pat=roll6",
        "track pad   role=harmony inst=strings|pad     vel=0.91  pan=0     ctr=B3 pat=padN",
        "track choir role=harmony inst=choir           vel=0.85  pan=0     ctr=D4 pat=padN p=70",
        "track bass  role=bass    inst=cello|horn      vel=1.32  pan=-0.15 ctr=G2 pat=walk",
        "track bell  role=color   inst=bell            vel=0.95  pan=0.2",
        "track chime role=color   inst=chime           vel=0.79  pan=-0.3",

        "[intro]",
        "chords: G | D/F# | Em | D",
        "harp: roll6 x.7",
        "pad: padN x.5",
        "bell: [G2 D3]:2 | r:2 | [E3 B3]:2 | r:2",

        "[A]",
        "chords: G | D/F# | Em | C | G | Am7 | D | G",
        "lead: " + MEL_A,
        "harp: roll6 x.8",
        "pad: padN x.6",
        "bass: walk x.7",
        "bell: [G2 D3]:2 | r:2 | r:2 | r:2 | [G2 D3]:2 | r:2 | r:2 | r:2",
        "chime: r:2 | r:2 | r:2 | r:2 | r:2 | r:2 | r:2 | D6:2",

        "[B]",
        "chords: C | G/B | Am | D | C | G/B | Am D | G",
        "lead: r:2",
        "lead2: " + MEL_B,
        "harp: roll6",
        "pad: padN x.8",
        "choir: padN x.8",
        "bass: walk x.8",
        "bell: [C3 G3]:2",
        "chime: -",

        "[A2 < A]",
        "lead2: " + SongTale.octave(MEL_A, 0),
        "choir: padN x.9",
        "pad: padN x.7",

        "[outro]",
        "chords: G | D/F# | C | G",
        "lead: p D5:1 G5:2/3 B5:1/3 | A5:1 F#5:2/3 D5:1/3 | E5:2 | D5:2",
        "harp: roll6 x.5",
        "pad: padN x.4",
        "bass: walk x.5",
        "bell: r:2 | r:2 | r:2 | [G2 D3 G3]:2",
        "chime: r:2 | r:2 | r:2 | G6:2",
    };
}
