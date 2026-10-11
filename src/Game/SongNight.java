package Game;

/** "Snow on the Lantern Road": the music of night and winter. A slow waltz (3/4) in A minor: a harp rocking underneath, a soft piano singing a falling
 *  line (E D C: the third, second and tonic of C, over A minor), a flute taking it over later, a warm pad, sparkling chimes, and one turn to E major
 *  at the end of each phrase. Written to be quiet and to leave room. Plays whatever its layers (`layers: fixed`). */
final class SongNight {
    private SongNight() { }

    // chords Am | Fmaj7 | C | G/B | Am | Fmaj7 | Dm | E
    private static final String MEL_A =
        "E5:1.5 D5:.5 C5:1 | A4:2 C5:1 | E5:1.5 G5:.5 E5:1 | D5:2 B4:1"
      + " | C5:1.5 D5:.5 E5:1 | F5:1 C5:1 A4:1 | D5:2 A4:1 | G#4:1.5 B4:.5 E5:1";
    // chords Dm | Am | F | C | Dm | Am | E | Am
    private static final String MEL_B =
        "F5:3 | E5:2 C5:1 | A5:1.5 G5:.5 F5:1 | E5:2 G5:1 | A5:1.5 F5:.5 D5:1 | C5:1 E5:1 A5:1 | G#5:2 E5:1 | A5:2 r:1";

    static final String[] LINES = {
        "title: Snow on the Lantern Road",
        "bpm: 56",
        "beats: 3",
        "shift: -2 2",
        "moods: 3:5",
        "layers: fixed",
        "form: intro?:pp A:p B:mp A2:mp outro:pp",

        "pattern arp3: 1:.5 5:.5 1':.5 3':.5 1':.5 5:.5",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",

        "track lead  role=lead    inst=piano|pluck     vel=1.41  pan=-0.10",
        "track lead2 role=lead    inst=flute|erhu      vel=0.92  pan=0.20",
        "track harp  role=rhythm  inst=harp            vel=1.45  pan=0.35  ctr=C3 pat=arp3",
        "track comp  role=rhythm  inst=piano           vel=1.45  pan=-0.30 ctr=C3 pat=arp3",
        "track pad   role=harmony inst=pad|strings     vel=1.04 pan=0     ctr=A3 pat=padN",
        "track choir role=harmony inst=choir           vel=0.99 pan=0     ctr=C4 pat=padN p=70",
        "track bass  role=bass    inst=cello           vel=1.40  pan=-0.15 ctr=G2 pat=root",
        "track bell  role=color   inst=bell            vel=1.09  pan=0.2",
        "track chime role=color   inst=chime           vel=1.10  pan=-0.3",

        "[intro]",
        "chords: Am | Fmaj7 | C | G",
        "harp: arp3 x.8",
        "pad: padN x.7",
        "bell: [A2 E3]:3",
        "lead2: r:3 | r:3 | E5:3 | D5:3",
        "chime: r:3 | r:3 | r:3 | B6:1.5 r:1.5",

        "[A]",
        "chords: Am | Fmaj7 | C | G/B | Am | Fmaj7 | Dm | E",
        "lead: " + MEL_A,
        "harp: arp3 x.7",
        "pad: padN x.6",
        "bass: root x.7",
        "bell: [A2 E3]:3",
        "chime: r:3 | r:3 | r:3 | r:2 E6:1 | r:3 | r:3 | r:3 | G#6:3",

        "[B]",
        "chords: Dm | Am | F | C | Dm | Am | E | Am",
        "lead: r:3",
        "lead2: " + MEL_B,
        "harp: -",
        "comp: arp3",
        "pad: padN x.8",
        "bass: root x.8",
        "bell: [D3 A3]:3",
        "chime: -",

        "[A2 < A]",
        "lead: r:3",
        "lead2: " + MEL_A,
        "harp: -",
        "comp: arp3 x.8",
        "choir: padN",
        "pad: padN x.7",

        "[outro]",
        "chords: Am | Fmaj7 | C | Am",
        "lead: p E5:1.5 D5:.5 C5:1 | A4:2 C5:1 | E5:3 | A4:3",
        "harp: arp3 x.6 @1-3",
        "pad: padN x.5",
        "bass: root x.5",
        "bell: r:3 | r:3 | r:3 | [A2 E3 A3]:3",
        "chime: r:3 | r:3 | r:3 | E6:3",
    };
}
