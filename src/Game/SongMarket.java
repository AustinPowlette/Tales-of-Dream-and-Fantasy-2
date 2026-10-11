package Game;

/** "Market of a Thousand Lamps": the music of the cities. G major, 4/4, quick (108 bpm). A bouncing harp bass and off-beat chords on the pipa give the street
 *  its walk; a dizi sings a short, singable hook (up the scale, back down), an erhu answers it, a wood block ticks. */
final class SongMarket {
    private SongMarket() { }

    // chords G | Em | C | D | G | Em | C | D G
    private static final String MEL_1 =
        "G4:.5 A4:.5 B4:1 D5:1 B4:.5 A4:.5 | G4:.5 A4:.5 B4:1 E5:1.5 D5:.5 | E5:.5 D5:.5 C5:1 D5:1 E5:1 | D5:1.5 B4:.5 A4:1 r:1"
      + " | G4:.5 A4:.5 B4:1 D5:1 B4:.5 A4:.5 | G4:.5 A4:.5 B4:1 E5:1.5 D5:.5 | E5:.5 G5:.5 E5:1 D5:1 C5:1 | B4:1 A4:1 G4:2";
    // the answer, same chords: down first, then up
    private static final String MEL_2 =
        "D5:1 B4:.5 G4:.5 B4:1 A4:.5 G4:.5 | E5:1.5 D5:.5 B4:1 G4:1 | E5:1 G5:.5 E5:.5 C5:1 D5:1 | D5:1.5 E5:.5 F#5:1 D5:1"
      + " | B4:.5 D5:.5 G5:1 D5:1 B4:1 | G5:1.5 F#5:.5 E5:1 B4:1 | E5:.5 G5:.5 C6:1 G5:1 E5:1 | A5:1 F#5:1 G5:2";
    // the middle (chords Em | Bm | C | D | Em | Am | D | G): the street turns a corner
    private static final String MEL_B =
        "B4:2 E5:1 G5:1 | F#5:2 D5:1 B4:1 | C5:1.5 E5:.5 G5:2 | F#5:2 A5:2"
      + " | G5:1.5 F#5:.5 E5:1 B4:1 | A4:1 C5:1 E5:2 | D5:1.5 F#5:.5 A5:2 | G5:3 r:1";

    static final String[] LINES = {
        "title: Market of a Thousand Lamps",
        "bpm: 108",
        "beats: 4",
        "swing: 0.1",
        "shift: -2 2",
        "moods: 1:5 6:1",
        "form: intro?:mp A:mf A2:mf B:mf A3:f outro:mp",

        "pattern chuck: r:.5 =1+=3+=5:.5 r:.5 =1+=3+=5:.5 r:.5 =1+=3+=5:.5 r:.5 =1+=3+=5:.5",
        "pattern bounce: b:1 5:1 b:1 5:1",
        "pattern padN: =1+=3+=5:*",
        "pattern run: 1:.5 3:.5 5:.5 3':.5 5':.5 3':.5 5:.5 3:.5",
        "pattern kickM: @kick>:1 r:.5 @kick^:.5 @kick:1 r:1",
        "pattern rimM: r:1 @rim>:1 r:1 @rim>:.5 @rim^:.5",
        "pattern hatM: @hat>:.5 @hat^:.5 @hat:.5 @hat^:.5 @hat>:.5 @hat^:.5 @hat:.5 @hat^:.5",
        "pattern tambM: r:.5 @tamb:.5 r:.5 @tamb^:.5 r:.5 @tamb:.5 r:.5 @tamb^:.5",
        "pattern congaM: r:.5 @mconga:.5 @mconga^:.5 @conga>:.5 r:.5 @mconga:.5 @conga:.5 @conga2:.5",

        "track lead   role=lead    inst=flute|pluck       vel=0.75 pan=0.15",
        "track lead2  role=lead    inst=erhu|flute        vel=0.89  pan=-0.15",
        "track pipa   role=rhythm  inst=pipa|pluck        vel=0.79  pan=0.35  ctr=G3 pat=chuck",
        "track bass   role=bass    inst=harp              vel=0.79  pan=-0.20 ctr=G2 pat=bounce",
        "track pad    role=harmony inst=strings           vel=0.70 pan=0     ctr=G3 pat=padN",
        "track harp   role=rhythm  inst=harp              vel=0.61  pan=-0.35 ctr=G3 pat=run p=70",
        "track bell   role=color   inst=bell              vel=0.78  pan=0.2",
        "track chime  role=color   inst=chime             vel=0.63  pan=-0.3",
        "track kick  role=perc    inst=kit      vel=0.86 pan=0    ctr=C2 pat=kickM",
        "track rim   role=perc    inst=kit      vel=0.46 pan=0.2  ctr=C2 pat=rimM",
        "track hat   role=perc    inst=kit      vel=0.49 pan=-0.2 ctr=C2 pat=hatM",
        "track tamb  role=perc    inst=kit      vel=0.88 pan=0.3  ctr=C2 pat=tambM",
        "track conga role=perc    inst=kit      vel=0.90 pan=-0.3 ctr=C2 pat=congaM",

        "[intro]",
        "chords: G | D | G | D",
        "hat: hatM x.7",
        "tamb: tambM x.7",
        "pipa: chuck",
        "bass: bounce x.7",
        "pad: padN x.6",
        "bell: [G3 D4]:4",

        "[A]",
        "chords: G | Em | C | D | G | Em | C | D G",
        "kick: kickM",
        "rim: rimM",
        "hat: hatM",
        "tamb: tambM",
        "lead: " + MEL_1,
        "pipa: chuck",
        "bass: bounce",
        "pad: padN x.7",
        "chime: r:4 | r:4 | r:4 | r:3 D6:1 | r:4 | r:4 | r:4 | r:3 G6:1",

        "[A2 < A]",
        "conga: congaM",
        "lead: r:4",
        "lead2: " + MEL_2,
        "harp: run",

        "[B]",
        "chords: Em | Bm | C | D | Em | Am | D | G",
        "kick: -",
        "rim: -",
        "hat: hatM x.8",
        "tamb: tambM",
        "conga: congaM",
        "lead: r:4",
        "lead2: " + MEL_B,
        "harp: -",
        "pad: padN x.9",
        "bell: [E3 B3]:4",
        "chime: -",

        "[A3 < A]",
        "kick: kickM x1.1",
        "conga: congaM",
        "tamb: tambM",
        "lead: " + MEL_1,
        "lead2: " + SongTale.octave(MEL_1, -1),
        "harp: run x1.1",
        "pad: padN x.9",
        "bell: [G3 D4 G4]:4",

        "[outro]",
        "chords: C D7 | G",
        "hat: hatM x.5",
        "lead: p E5:1 D5:1 C5:1 B4:1 | G4:4",
        "pipa: chuck @1",
        "bass: bounce @1",
        "pad: padN x.6",
        "bell: r:4 | [G3 D4 G4]:4",
        "chime: r:4 | G6:4",
    };
}
