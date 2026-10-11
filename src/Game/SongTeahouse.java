package Game;

/** "Tea at Dusk": a second song for the cities. C major, an easy 4/4 (92 bpm), slower and warmer than the market: a pipa plays the tune itself (a little skip,
 *  then a falling line), a dizi doubles it in the answer, a harp walks the bass, a wood block keeps a light two-beat, and the middle leans into A minor like a
 *  lamp being turned down. Plays with the usual layers. */
final class SongTeahouse {
    private SongTeahouse() { }

    // chords C | Am7 | Dm7 | G7 | C | Em7 | F | G
    private static final String MEL_A =
        "E5:.5 G5:.5 E5:1 D5:.5 C5:.5 D5:1 | E5:1.5 C5:.5 A4:2 | F5:.5 A5:.5 F5:1 E5:.5 D5:.5 C5:1 | D5:2 B4:1 G4:1"
      + " | E5:.5 G5:.5 E5:1 G5:.5 A5:.5 G5:1 | B5:1.5 G5:.5 E5:2 | A5:1 C6:1 A5:1 F5:1 | B5:1 G5:1 D5:2";
    // the same tune, ending at home (chords ... Em7 | Dm7 G7 | C)
    private static final String MEL_A2 =
        "E5:.5 G5:.5 E5:1 D5:.5 C5:.5 D5:1 | E5:1.5 C5:.5 A4:2 | F5:.5 A5:.5 F5:1 E5:.5 D5:.5 C5:1 | D5:2 B4:1 G4:1"
      + " | E5:.5 G5:.5 E5:1 G5:.5 A5:.5 G5:1 | B5:1.5 G5:.5 E5:2 | F5:1 A5:1 G5:.5 F5:.5 D5:1 | C5:3 r:1";
    // the middle (chords Am | F | C | G | Am | F | G | G7): the lamp turned down
    private static final String MEL_B =
        "A5:1.5 G5:.5 E5:2 | F5:1.5 E5:.5 C5:2 | G5:1 E5:1 C5:2 | D5:1.5 G5:.5 B5:2"
      + " | C6:1.5 B5:.5 A5:2 | A5:1 G5:1 F5:2 | D5:2 B4:2 | F5:1.5 D5:.5 B4:2";

    static final String[] LINES = {
        "title: Tea at Dusk",
        "bpm: 92",
        "beats: 4",
        "swing: 0.18",
        "shift: -2 2",
        "moods: 1:5 6:1 0:1",
        "form: intro?:mp A:mf A2:mf B:mp A3:f outro:mp",

        "pattern chuck: r:1 =1+=3+=5:.5 r:.5 r:1 =1+=3+=5:.5 r:.5",
        "pattern walk: b:1 5:1 3':1 5:1",
        "pattern padN: =1+=3+=5:*",
        "pattern run: 1:.5 3:.5 5:.5 3':.5 5':.5 3':.5 5:.5 3:.5",
        "pattern kickT: @kick:1 r:1.5 @kick^:.5 r:1",
        "pattern rideT: @ride:1 @ride:.5 @ride^:.5 @ride:1 @ride:.5 @ride^:.5",
        "pattern rimT: r:1 @rim:1 r:1 @rim^:1",
        "pattern shkT: r:.5 @shaker:.5 r:.5 @shaker^:.5 r:.5 @shaker:.5 r:.5 @shaker^:.5",
        "pattern claveT: @clave>:1.5 @clave:1.5 @clave:1",

        "track lead   role=lead    inst=pipa|pluck        vel=1.13 pan=0.15",
        "track lead2  role=lead    inst=flute|erhu        vel=0.76 pan=-0.15",
        "track strum  role=rhythm  inst=pluck             vel=0.47 pan=0.35  ctr=C3 pat=chuck",
        "track bass   role=bass    inst=harp              vel=0.83 pan=-0.20 ctr=C2 pat=walk",
        "track pad    role=harmony inst=strings           vel=0.85 pan=0     ctr=G3 pat=padN",
        "track harp   role=rhythm  inst=harp              vel=0.83 pan=-0.35 ctr=C3 pat=run p=70",
        "track bell   role=color   inst=bell              vel=0.92 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.73 pan=-0.3",
        "track kick  role=perc    inst=kit      vel=0.91 pan=0    ctr=C2 pat=kickT",
        "track ride  role=perc    inst=kit      vel=1.40 pan=0.2  ctr=C2 pat=rideT",
        "track rim   role=perc    inst=kit      vel=0.54 pan=-0.2 ctr=C2 pat=rimT",
        "track shk   role=perc    inst=kit      vel=1.36 pan=0.3  ctr=C2 pat=shkT",
        "track clave role=perc    inst=kit      vel=1.26 pan=-0.3 ctr=C2 pat=claveT",

        "[intro]",
        "chords: C | G | Am7 | G7",
        "shk: shkT x.7",
        "strum: chuck x.8",
        "bass: walk x.7",
        "pad: padN x.6",
        "bell: [C3 G3]:4",

        "[A]",
        "chords: C | Am7 | Dm7 | G7 | C | Em7 | F | G",
        "kick: kickT",
        "ride: rideT",
        "rim: rimT",
        "lead: " + MEL_A,
        "strum: chuck",
        "bass: walk",
        "pad: padN x.7",
        "chime: r:4 | r:4 | r:4 | r:3 D6:1 | r:4 | r:4 | r:4 | r:3 G6:1",

        "[A2 < A]",
        "chords: C | Am7 | Dm7 | G7 | C | Em7 | Dm7 G7 | C",
        "shk: shkT",
        "lead: r:4",
        "lead2: " + MEL_A2,
        "harp: run",

        "[B]",
        "chords: Am | F | C | G | Am | F | G | G7",
        "kick: -",
        "ride: -",
        "rim: -",
        "shk: shkT",
        "clave: claveT",
        "lead: " + SongTale.octave(MEL_B, 0),
        "lead2: -",
        "harp: -",
        "strum: chuck x.8",
        "pad: padN x.9",
        "bell: [A2 E3]:4 | r:4 | r:4 | r:4 | [A2 E3]:4",
        "chime: -",

        "[A3 < A]",
        "chords: C | Am7 | Dm7 | G7 | C | Em7 | Dm7 G7 | C",
        "clave: claveT",
        "shk: shkT",
        "lead: " + MEL_A2,
        "lead2: -",
        "harp: run",

        "[outro]",
        "chords: C | Am7 | G7 | C",
        "shk: shkT x.4",
        "lead: p E5:.5 G5:.5 E5:1 D5:.5 C5:.5 D5:1 | E5:1.5 C5:.5 A4:2 | D5:2 B4:2 | C5:4",
        "strum: chuck x.6",
        "bass: walk x.5",
        "pad: padN x.6",
        "bell: r:4 | r:4 | r:4 | [C3 G3 C4]:4",
        "chime: r:4 | r:4 | r:4 | C6:4",
    };
}
