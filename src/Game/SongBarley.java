package Game;

/** "Wind Across the Barley": open country, a brisk walking song. A major with the flat seventh left in (a folk sound), 4/4 at 112 bpm: a dizi hook that skips up
 *  to the tonic and falls back, a pipa chopping on the off-beats, a harp bass that bounces, an erhu answer that climbs through the middle, and a wooden tick
 *  like boots on a dry road. Plays with the usual layers. */
final class SongBarley {
    private SongBarley() { }

    // chords A | D | A | E | A | F#m | D | E
    private static final String MEL_A =
        "E5:.5 A5:.5 A5:1 G#5:.5 A5:.5 B5:1 | A5:1 F#5:1 D5:2 | C#5:.5 E5:.5 A5:1 E5:1 C#5:1 | B4:2 G#4:1 E5:1"
      + " | E5:.5 A5:.5 A5:1 G#5:.5 A5:.5 C#6:1 | A5:1.5 F#5:.5 C#5:2 | D5:1 F#5:1 A5:1 F#5:1 | B5:1.5 G#5:.5 E5:2";
    // the same tune, ending at home (chords ... D E | A)
    private static final String MEL_A2 =
        "E5:.5 A5:.5 A5:1 G#5:.5 A5:.5 B5:1 | A5:1 F#5:1 D5:2 | C#5:.5 E5:.5 A5:1 E5:1 C#5:1 | B4:2 G#4:1 E5:1"
      + " | E5:.5 A5:.5 A5:1 G#5:.5 A5:.5 C#6:1 | A5:1.5 F#5:.5 C#5:2 | F#5:1 A5:1 G#5:1 B5:1 | A5:3 r:1";
    // chords D | A/C# | Bm | E | D | A/C# | E | A
    private static final String MEL_B =
        "F#5:1 A5:1 D6:2 | C#6:1.5 B5:.5 A5:2 | B5:1 D6:1 B5:1 F#5:1 | G#5:2 E5:2"
      + " | F#5:.5 A5:.5 D6:1 C#6:.5 D6:.5 F#6:1 | E6:1 C#6:1 A5:2 | B5:1 G#5:1 E5:1 G#5:1 | A5:2 E5:2";

    static final String[] LINES = {
        "title: Wind Across the Barley",
        "bpm: 112",
        "beats: 4",
        "swing: 0.06",
        "shift: -2 2",
        "moods: 0:4 1:1",
        "form: intro?:mp A:mf A2:mf B:mf A3:f outro:mp",

        "pattern chop: r:.5 =1+=3+=5:.5 r:.5 =1+=3+=5:.5 r:.5 =1+=3+=5:.5 r:.5 =1+=3+=5:.5",
        "pattern bounce: b:1 5:1 b:1 5:1",
        "pattern padN: =1+=3+=5:*",
        "pattern run: 1:.5 3:.5 5:.5 3':.5 5':.5 3':.5 5:.5 3:.5",
        "pattern frameB: @tom4>:1 r:.5 @tom4^:.5 @tom4:1 @tom4^:.5 @tom4^:.5",
        "pattern stompB: @kick>:1 r:1 @kick:1 r:1",
        "pattern tambB: r:.5 @tamb:.5 r:.5 @tamb^:.5 r:.5 @tamb:.5 r:.5 @tamb^:.5",
        "pattern clapB: r:1 @clap>:1 r:1 @clap>:1",
        "pattern bongoB: @bongo2:.5 @bongo:.5 @bongo2^:.5 @bongo:.5 @bongo2:.5 @bongo:.5 @bongo2^:.5 @bongo:.5",

        "track lead   role=lead    inst=flute|pluck       vel=0.68 pan=0.15",
        "track lead2  role=lead    inst=erhu|flute        vel=0.90 pan=-0.15",
        "track pipa   role=rhythm  inst=pipa|pluck        vel=0.80 pan=0.35  ctr=A3 pat=chop",
        "track bass   role=bass    inst=harp              vel=0.74 pan=-0.20 ctr=A2 pat=bounce",
        "track pad    role=harmony inst=strings           vel=0.72 pan=0     ctr=A3 pat=padN",
        "track harp   role=rhythm  inst=harp              vel=0.65 pan=-0.35 ctr=A3 pat=run p=70",
        "track bell   role=color   inst=bell              vel=0.85 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.64 pan=-0.3",
        "track frame role=perc    inst=kit      vel=0.45 pan=-0.15 ctr=C2 pat=frameB",
        "track stomp role=perc    inst=kit      vel=0.85 pan=0     ctr=C2 pat=stompB",
        "track tamb  role=perc    inst=kit      vel=1.11 pan=0.3   ctr=C2 pat=tambB",
        "track clap  role=perc    inst=kit      vel=0.64 pan=0.15  ctr=C2 pat=clapB",
        "track bongo role=perc    inst=kit      vel=0.76 pan=0.35  ctr=C2 pat=bongoB",

        "[intro]",
        "chords: A | D | A | E",
        "frame: frameB x.7",
        "pipa: chop",
        "bass: bounce x.7",
        "pad: padN x.6",
        "bell: [A2 E3]:4",

        "[A]",
        "chords: A | D | A | E | A | F#m | D | E",
        "frame: frameB x.6",
        "stomp: stompB",
        "tamb: tambB x.8",
        "lead: " + MEL_A,
        "pipa: chop",
        "bass: bounce",
        "pad: padN x.7",
        "chime: r:4 | r:4 | r:4 | r:3 E6:1 | r:4 | r:4 | r:4 | r:3 B5:1",

        "[A2 < A]",
        "chords: A | D | A | E | A | F#m | D E | A",
        "clap: clapB",
        "bongo: bongoB",
        "lead: r:4",
        "lead2: " + MEL_A2,
        "harp: run",

        "[B]",
        "chords: D | A/C# | Bm | E | D | A/C# | E | A",
        "stomp: -",
        "frame: frameB x.6",
        "tamb: tambB",
        "bongo: bongoB",
        "lead: " + MEL_B,
        "lead2: -",
        "harp: -",
        "pipa: chop x.8",
        "pad: padN x.9",
        "bell: [D3 A3]:4 | r:4 | r:4 | r:4 | [D3 A3]:4",
        "chime: -",

        "[A3 < A2]",
        "stomp: stompB",
        "clap: clapB",
        "lead: " + MEL_A2,
        "lead2: -",
        "harp: run",

        "[outro]",
        "chords: A | D | E | A",
        "frame: frameB x.5",
        "lead: p E5:.5 A5:.5 A5:1 G#5:.5 A5:.5 B5:1 | A5:1 F#5:1 D5:2 | B4:2 G#4:2 | A4:4",
        "pipa: chop x.6",
        "bass: bounce x.5",
        "pad: padN x.6",
        "bell: r:4 | r:4 | r:4 | [A2 E3 A3]:4",
        "chime: r:4 | r:4 | r:4 | A6:4",
    };
}
