package Game;

import java.util.regex.Matcher;

/** "A Tale Begins": the main theme. D major, 72 bpm. One hook (A4 B4 D5: up a step and a third) heard in a plain piano statement, an answer on a
 *  singing voice, a yearning B section and a full climax with the melody in octaves, then home. It plays at the capital and in the sects, and now and then in
 *  the open country and at night (thinner). */
final class SongTale {
    private SongTale() { }

    /** A melody string moved down (or up) by whole octaves. */
    static String octave(String mel, int octaves) {
        Matcher m = java.util.regex.Pattern.compile("([A-G][#b]?)(\\d)").matcher(mel);
        StringBuffer sb = new StringBuffer();
        while (m.find()) m.appendReplacement(sb, m.group(1) + (Integer.parseInt(m.group(2)) + octaves));
        m.appendTail(sb);
        return sb.toString();
    }

    // the first statement (piano)
    private static final String MEL_A =
        "A4:1.5 B4:.5 D5:2 | D5:1.5 C#5:.5 B4:1 A4:1 | G4:1.5 A4:.5 B4:1 D5:1 | E5:2.5 D5:.5 C#5:1"
      + " | A4:1.5 B4:.5 D5:1 F#5:1 | E5:1.5 D5:.5 C#5:1 A4:1 | B4:1 D5:1 G5:2 | F#5:1 E5:1 C#5:1 A4:1";
    // the answer (a singing voice): same hook, a different road
    private static final String MEL_A2 =
        "A4:1.5 B4:.5 D5:1 E5:1 | F#5:1.5 E5:.5 D5:1 B4:1 | B4:1.5 A4:.5 G4:1 B4:1 | E5:1.5 F#5:.5 E5:1 C#5:1"
      + " | D5:1.5 E5:.5 F#5:1 A5:1 | A5:1.5 F#5:.5 E5:1 C#5:1 | D5:1 B4:1 G4:1 B4:1 | G4:1 B4:1 C#5:2";
    // the yearning middle
    private static final String MEL_B =
        "D5:2 F#5:1.5 E5:.5 | C#5:2 E5:1 F#5:1 | G5:1.5 F#5:.5 D5:1 B4:1 | A4:2 D5:1 F#5:1"
      + " | G5:2 E5:1 B4:1 | D5:1.5 F#5:.5 A5~:2 | A5:1.5 G5:.5 F#5:1 E5:1 | E5:2.5 r:1.5";
    // the climax: the hook, then the road rises in arpeggios
    private static final String MEL_A3 =
        "A4:1.5 B4:.5 D5:2 | D5:1.5 C#5:.5 B4:1 A4:1 | G4:1.5 A4:.5 B4:1 D5:1 | E5:2.5 D5:.5 C#5:1"
      + " | A4:1 D5:1 F#5:1 A5:1 | A5:1.5 F#5:.5 E5:1 C#5:1 | B4:1 D5:1 G5:1 B5:1 | G5:2 E5:1 C#5:1";

    static final String[] LINES = {
        "title: A Tale Begins",
        "bpm: 72",
        "beats: 4",
        "shift: -2 2",
        "moods: 6:5 5:1 0:1 3:1",
        "form: intro?:p A:mp A2:mf B:mf A3:f outro:p",

        "pattern arpL: 1:.5 5:.5 1':.5 3':.5 5':.5 3':.5 1':.5 5:.5",
        "pattern arpS: 1:1 5:.5 3':.5 5':1 3':1",
        "pattern ring: 1:.5 5:.5 1':.5 3':.5 5':.5 9':*",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern rootFifth: b:2 5:2",
        "pattern thud: @D2:.5 r:3.5",

        "track lead   role=lead    inst=piano|pluck       vel=1.45  pan=-0.10",
        "track low    role=lead    inst=piano             vel=0.82  pan=-0.10",
        "track lead2  role=lead    inst=erhu|flute|cello  vel=0.88 pan=0.15",
        "track lead3  role=lead    inst=strings|horn      vel=0.78  pan=0",
        "track harpEnd role=rhythm inst=harp            vel=1.27  pan=0.35  ctr=D3 pat=ring",
        "track harp   role=rhythm  inst=harp              vel=0.72  pan=0.35  ctr=D3 pat=arpL",
        "track comp   role=rhythm  inst=piano             vel=0.76 pan=-0.30 ctr=D3 pat=arpS",
        "track pad    role=harmony inst=strings           vel=0.63  pan=0     ctr=A3 pat=padN",
        "track choir  role=harmony inst=choir             vel=0.72  pan=0     ctr=D4 pat=padN p=80",
        "track bass   role=bass    inst=cello             vel=0.77 pan=-0.15 ctr=G2 pat=root",
        "track flute  role=counter inst=flute             vel=0.59  pan=0.4",
        "track bell   role=color   inst=bell              vel=0.81 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.67  pan=-0.3",
        "track taiko  role=perc    inst=taiko             vel=0.94  pan=0     ctr=D2 pat=thud",

        "[intro]",
        "chords: D | Bm7 | Gmaj7 | Asus4 A",
        "harp: arpL",
        "pad: padN x.7",
        "bass: root x.6 @3-4",
        "bell: [D3 A3]:4",
        "flute: r:4 | r:4 | B4:2 D5:2 | E5:3 r:1",

        "[A]",
        "chords: D | Bm7 | Gmaj7 | Asus4 A | D | F#m7 | Gmaj7 | Em7 A",
        "lead: " + MEL_A,
        "harp: arpL x.6",
        "pad: padN x.55",
        "bass: root x.7",
        "bell: [D3 A3]:4",
        "chime: r:4 | r:4 | r:4 | r:3 A6:1",

        "[A2 < A]",
        "lead: r:4",
        "lead2: " + MEL_A2,
        "pad: padN x.85",
        "bass: rootFifth",
        "taiko: on x.8",
        "flute: r:4 | r:4 | r:4 | r:4 | A5:4 | A5:2 F#5:2 | B5:4 | G5:2 A5:2",

        "[B]",
        "chords: Bm | F#m7 | Gmaj7 | D/F# | Em7 | Bm7 | Asus4 | A",
        "lead: r:4",
        "lead2: " + MEL_B,
        "harp: -",
        "comp: arpS",
        "pad: padN x.8",
        "bass: rootFifth",
        "taiko: -",
        "flute: -",
        "bell: [B2 F#3]:4",

        "[A3 < A2]",
        "chords: D | Bm7 | Gmaj7 | Asus4 A | D | F#m7 | Gmaj7 | Em7 A",
        "lead: " + MEL_A3,
        "low: " + octave(MEL_A3, -1),
        "lead2: r:4",
        "lead3: " + MEL_A3,
        "harp: arpL x1.1",
        "comp: -",
        "pad: padN x1.0",
        "choir: padN x.9",
        "bass: rootFifth x1.1",
        "taiko: on x1.1",
        "bell: [D3 A3 D4]:4",
        "flute: -",

        "[outro]",
        "chords: D | Bm7 | Gmaj7 Asus4 | Dadd9",
        "lead: p A4:1.5 B4:.5 D5:2 | D5:1.5 C#5:.5 B4:2 | G4:2 A4:2 | D5:4",
        "harp: arpL @1-3",
        "harpEnd: ring @4",
        "pad: padN x.7",
        "bass: root x.6",
        "bell: r:4 | r:4 | r:4 | [D3 A3 D4]:4",
        "chime: r:4 | r:4 | r:4 | r:2 D6:2",
    };
}
