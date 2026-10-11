package Game;

/** "Lantern Festival Night": a city song for a holiday. E-flat major, a bright 4/4 at 120 bpm: a pipa running up and down the chord in eighths, a dizi
 *  holding the long notes of the middle section, bells and chimes everywhere, a harp keeping a skipping bass and a taiko on the beat like feet on cobbles.
 *  Plays with the usual layers. */
final class SongFestival {
    private SongFestival() { }

    // chords Eb | Bb | Cm | Ab | Eb | Bb/D | Ab | Bb
    private static final String MEL_A =
        "G5:.5 Bb5:.5 Eb6:1 D6:.5 Bb5:.5 G5:1 | F5:.5 Bb5:.5 D6:1 C6:.5 Bb5:.5 F5:1 | Eb5:.5 G5:.5 C6:1 Bb5:.5 G5:.5 Eb5:1 | C5:.5 Eb5:.5 Ab5:1 G5:.5 Eb5:.5 C5:1"
      + " | G5:.5 Bb5:.5 Eb6:1 F6:.5 G6:.5 Eb6:1 | D6:1 C6:.5 Bb5:.5 F5:2 | C6:1 Eb6:1 C6:1 Ab5:1 | Bb5:2 D6:2";
    // the same tune ending at home (... Ab Bb | Eb)
    private static final String MEL_A2 =
        "G5:.5 Bb5:.5 Eb6:1 D6:.5 Bb5:.5 G5:1 | F5:.5 Bb5:.5 D6:1 C6:.5 Bb5:.5 F5:1 | Eb5:.5 G5:.5 C6:1 Bb5:.5 G5:.5 Eb5:1 | C5:.5 Eb5:.5 Ab5:1 G5:.5 Eb5:.5 C5:1"
      + " | G5:.5 Bb5:.5 Eb6:1 F6:.5 G6:.5 Eb6:1 | D6:1 C6:.5 Bb5:.5 F5:2 | C6:1 Eb6:1 D6:1 F6:1 | Eb6:3 r:1";
    // chords Cm | Ab | Eb | Bb | Cm | Ab | Bb | Eb
    private static final String MEL_B =
        "G5:2 C6:2 | Eb6:3 C6:1 | Bb5:2 G5:2 | D6:3 F6:1 | Eb6:2 G6:2 | Eb6:2 C6:2 | D6:2 Bb5:2 | G5:4";

    static final String[] LINES = {
        "title: Lantern Festival Night",
        "bpm: 120",
        "beats: 4",
        "swing: 0.08",
        "shift: -2 2",
        "moods: 1:4 6:1",
        "form: intro?:mf A:mf B:mf A2:f A3:ff outro:mf",

        "pattern run: 1:.5 3:.5 5:.5 3':.5 5':.5 3':.5 5:.5 3:.5",
        "pattern skip: b:1.5 5:.5 b:1.5 5:.5",
        "pattern padN: =1+=3+=5:*",
        "pattern ring: =1:.5 =5:.5 =1:.5 =5:.5 =1:.5 =5:.5 =1:.5 =5:.5",
        "pattern kickF: @kick>:1 @kick^:1 @kick:1 @kick^:1",
        "pattern clapF: r:1 @clap>:1 r:1 @clap>:1",
        "pattern hatF: @hat>:.25 @hat^:.25 @hat:.25 @hat^:.25 @hat>:.25 @hat^:.25 @hat:.25 @hat^:.25 @hat>:.25 @hat^:.25 @hat:.25 @hat^:.25 @hat>:.25 @hat^:.25 @hat:.25 @hat^:.25",
        "pattern shkF: r:.5 @shaker:.5 r:.5 @shaker:.5 r:.5 @shaker:.5 r:.5 @shaker:.5",
        "pattern cowF: r:2 @cow>:.5 r:.5 @cow:.5 r:.5",
        "pattern claveF: @clave>:1.5 @clave:1.5 @clave:1",

        "track lead   role=lead    inst=pipa|pluck        vel=1.01 pan=0.15",
        "track lead2  role=lead    inst=flute|erhu        vel=0.66 pan=-0.15",
        "track run    role=rhythm  inst=harp              vel=0.61 pan=-0.35 ctr=Eb3 pat=run",
        "track bass   role=bass    inst=cello|harp        vel=0.85 pan=-0.20 ctr=Eb2 pat=skip",
        "track pad    role=harmony inst=strings           vel=0.75 pan=0     ctr=G3 pat=padN",
        "track ring   role=color   inst=bell              vel=0.53 pan=0.30  ctr=Eb4 pat=ring",
        "track bell   role=color   inst=bell              vel=0.76 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.57 pan=-0.3",
        "track kick  role=perc    inst=kit      vel=0.82 pan=0    ctr=C2 pat=kickF",
        "track clap  role=perc    inst=kit      vel=0.57 pan=0.15 ctr=C2 pat=clapF",
        "track hat   role=perc    inst=kit      vel=0.45 pan=-0.2 ctr=C2 pat=hatF",
        "track shk   role=perc    inst=kit      vel=1.12 pan=0.3  ctr=C2 pat=shkF",
        "track cow   role=perc    inst=kit      vel=1.13 pan=-0.3 ctr=C2 pat=cowF",
        "track clave role=perc    inst=kit      vel=1.21 pan=0.35 ctr=C2 pat=claveF",

        "[intro]",
        "chords: Eb | Bb | Cm | Bb",
        "shk: shkF x.8",
        "clave: claveF x.7",
        "run: run x.7",
        "bass: skip x.7",
        "pad: padN x.6",
        "bell: [Eb3 Bb3]:4 | r:4 | [C3 G3]:4 | r:4",

        "[A]",
        "chords: Eb | Bb | Cm | Ab | Eb | Bb/D | Ab | Bb",
        "kick: kickF",
        "clap: clapF",
        "hat: hatF x.8",
        "shk: shkF",
        "lead: " + MEL_A,
        "run: run x.8",
        "bass: skip",
        "pad: padN x.7",
        "chime: r:4 | r:4 | r:4 | r:3 Eb6:1 | r:4 | r:4 | r:4 | r:3 Bb6:1",

        "[B]",
        "chords: Cm | Ab | Eb | Bb | Cm | Ab | Bb | Eb",
        "kick: -",
        "clap: -",
        "hat: -",
        "shk: shkF",
        "clave: claveF",
        "cow: cowF",
        "lead: r:4",
        "lead2: " + MEL_B,
        "run: -",
        "ring: ring",
        "bass: skip x.9",
        "pad: padN x.9",
        "bell: [C3 G3]:4 | r:4 | r:4 | r:4 | [C3 G3]:4 | r:4 | r:4 | r:4",
        "chime: -",

        "[A2 < A]",
        "chords: Eb | Bb | Cm | Ab | Eb | Bb/D | Ab Bb | Eb",
        "kick: kickF",
        "clap: clapF",
        "hat: hatF",
        "shk: shkF",
        "cow: cowF",
        "clave: -",
        "lead: " + MEL_A2,
        "lead2: -",
        "ring: ring x.8",

        "[A3 < A2]",
        "clave: claveF",
        "lead2: " + SongTale.octave(MEL_A2, -1),
        "run: run",
        "ring: ring",

        "[outro]",
        "chords: Eb | Bb | Ab | Eb",
        "shk: shkF x.5",
        "lead: p G5:.5 Bb5:.5 Eb6:1 D6:.5 Bb5:.5 G5:1 | F5:.5 Bb5:.5 D6:1 C6:.5 Bb5:.5 F5:1 | C6:2 Ab5:2 | Eb6:4",
        "run: run x.6",
        "bass: skip x.6",
        "pad: padN x.6",
        "bell: r:4 | r:4 | r:4 | [Eb3 Bb3 Eb4]:4",
        "chime: r:4 | r:4 | r:4 | Eb6:4",
    };
}
