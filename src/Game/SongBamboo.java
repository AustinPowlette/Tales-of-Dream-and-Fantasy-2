package Game;

/** "Bamboo and Temple Rain": the sects in a slow waltz. A major on its pentatonic scale (A B C# E F#, never a half step), 3/4 at 56 bpm: a flute on long notes
 *  that always lands on a chord tone, a guqin arpeggio rocking in threes, a temple bell on the first beat of every phrase, a cello and a wide pad, an erhu
 *  joining in the middle on a rising line, rain-drop chimes. Plays whatever its layers (`layers: fixed`). */
final class SongBamboo {
    private SongBamboo() { }

    // chords A | F#m | D | E | A | F#m | Bm7 | E
    private static final String MEL_A =
        "C#5:2 E5:1 | F#5:2 A5:1 | A5:1 F#5:1 D5:1 | B5:2 G#5:1"
      + " | E6:2 C#6:1 | A5:2 F#5:1 | D6:1 B5:1 F#5:1 | G#5:3";
    // chords D | A | Bm7 | E | D | A | E | A
    private static final String MEL_B =
        "F#5:2 A5:1 | E5:1 A5:1 C#6:1 | B5:2 D6:1 | B5:1 G#5:1 E5:1"
      + " | A5:2 F#5:1 | C#6:2 E6:1 | B5:2 G#5:1 | A5:3";

    static final String[] LINES = {
        "title: Bamboo and Temple Rain",
        "bpm: 56",
        "beats: 3",
        "shift: -2 2",
        "moods: 5:5",
        "layers: fixed",
        "form: intro?:p A:mp B:mp A2:mf outro:p",

        "pattern arp3: 1:1 5:1 3':1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",

        "track lead  role=lead    inst=flute|pluck     vel=0.75  pan=0.10",
        "track lead2 role=lead    inst=erhu|flute      vel=0.86  pan=-0.10",
        "track guqin role=rhythm  inst=pluck           vel=0.81  pan=0.30  ctr=A3 pat=arp3",
        "track pad   role=harmony inst=strings|pad     vel=0.89  pan=0     ctr=C#4 pat=padN",
        "track choir role=harmony inst=choir           vel=0.85  pan=0     ctr=E4 pat=padN p=70",
        "track drone role=bass    inst=cello|horn      vel=1.30  pan=-0.15 ctr=A2 pat=root",
        "track bell  role=color   inst=bell            vel=0.95  pan=0.2",
        "track chime role=color   inst=chime           vel=1.04  pan=-0.3",

        "[intro]",
        "chords: A | F#m | A | E",
        "bell: [A2 E3]:3 | r:3 | r:3 | r:3",
        "pad: padN x.5",
        "drone: root x.5",
        "chime: r:3 | r:2 E6:1 | r:3 | B6:3",

        "[A]",
        "chords: A | F#m | D | E | A | F#m | Bm7 | E",
        "lead: " + MEL_A,
        "guqin: arp3 x.8",
        "pad: padN x.6",
        "drone: root x.7",
        "bell: [A2 E3]:3 | r:3 | r:3 | r:3 | [A2 E3]:3 | r:3 | r:3 | r:3",
        "chime: r:3 | r:3 | r:3 | r:3 | r:3 | r:3 | r:2 B5:1 | r:3",

        "[B]",
        "chords: D | A | Bm7 | E | D | A | E | A",
        "lead: r:3",
        "lead2: " + MEL_B,
        "guqin: arp3",
        "pad: padN x.8",
        "choir: padN x.8",
        "drone: root x.8",
        "bell: [D3 A3]:3 | r:3 | r:3 | r:3 | [D3 A3]:3 | r:3 | r:3 | r:3",
        "chime: -",

        "[A2 < A]",
        "chords: A | F#m | D | E | A | F#m | Bm7 E | A",
        "lead: " + MEL_A.replace("D6:1 B5:1 F#5:1 | G#5:3", "D6:1.5 B5:1 G#5:.5 | A5:3"),
        "lead2: " + SongTale.octave(MEL_A.replace("D6:1 B5:1 F#5:1 | G#5:3", "D6:1.5 B5:1 G#5:.5 | A5:3"), 0),
        "choir: padN x.9",
        "pad: padN x.7",

        "[outro]",
        "chords: A | F#m | E | A",
        "lead: p C#5:2 E5:1 | F#5:2 A5:1 | G#5:3 | A5:3",
        "guqin: arp3 x.5",
        "pad: padN x.4",
        "drone: root x.5",
        "bell: r:3 | r:3 | r:3 | [A2 E3 A3]:3",
        "chime: r:3 | r:3 | r:3 | A6:3",
    };
}
