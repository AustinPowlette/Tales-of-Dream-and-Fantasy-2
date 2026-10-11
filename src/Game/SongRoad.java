package Game;

/** "The Long Road": the music of the open country. E minor in a lilting 6/8 (one beat = a dotted quarter, so a bar is six eighths): a rolling koto, a dizi
 *  singing the hook (up a fourth, then down a step), an erhu answering with the road's harmonic-minor cadence, a cello line underneath, a soft heartbeat drum
 *  late on. Layers matter here: at night only the koto, pad and a soft dizi are left; in danger and war a taiko and a low cello ostinato (the `drive` tracks)
 *  come in under it and the melody goes away. */
final class SongRoad {
    private SongRoad() { }

    // chords Em | Cmaj7 | G | D | Em | Cmaj7 | D | Em
    private static final String MEL_A =
        "B4:1 E5:2/3 D5:1/3 | C5:1 B4:2/3 G4:1/3 | D5:1 B4:2/3 D5:1/3 | A4:2"
      + " | B4:1 E5:2/3 G5:1/3 | E5:1 D5:2/3 C5:1/3 | A4:1 D5:2/3 F#5:1/3 | E5:2";
    // chords Em | Cmaj7 | G | D | Em | Am | B7 | Em   (the erhu answers and climbs, the cadence leans on B7)
    private static final String MEL_A2 =
        "B4:1 E5:2/3 F#5:1/3 | G5:1 E5:2/3 C5:1/3 | B4:1 D5:2/3 G5:1/3 | F#5:1 A5:2/3 F#5:1/3"
      + " | G5:1 E5:2/3 B4:1/3 | A4:1 C5:2/3 E5:1/3 | D#5:1 F#5:2/3 E5:1/3 | E5:2";
    // chords G | D/F# | Em7 | D | C | G/B | Am7 | D   (the middle: a bright major turn)
    private static final String MEL_B =
        "B4:1 D5:2/3 E5:1/3 | F#5:2 | G5:1 F#5:2/3 E5:1/3 | D5:2 | E5:1 G5:2/3 E5:1/3 | D5:1 B4:1 | C5:1 E5:1 | F#5:1 A5:1";
    // the last time: the same, with the hook leaping higher at the end
    private static final String MEL_A3 =
        "B4:1 E5:2/3 D5:1/3 | C5:1 B4:2/3 G4:1/3 | D5:1 B4:2/3 D5:1/3 | A4:2"
      + " | B4:1 E5:2/3 G5:1/3 | G5:1 E5:2/3 C5:1/3 | F#5:1 A5:2/3 F#5:1/3 | E5:2";

    static final String[] LINES = {
        "title: The Long Road",
        "bpm: 52",
        "beats: 2",
        "shift: -2 2",
        "moods: 0:4 3:1 1:1",
        "form: intro?:p A:mp A2:mp B:mf A3:mf outro:p",

        "pattern rollE: 1:1/3 5:1/3 1':1/3 3':1/3 1':1/3 5:1/3",
        "pattern rollB: 1:2/3 5:1/3 3':2/3 5:1/3",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern lilt: b:1 5:1",
        "pattern drive6: @E2:1/3 r:1/3 @E2:1/3 @E2:1/3 r:1/3 @E2:1/3",
        "pattern ost6: b:2/3 5:1/3 b:2/3 5:1/3",
        "pattern heart: @E2:1/3 r:5/3",

        "track lead   role=lead    inst=flute|pluck       vel=0.86 pan=0.10",
        "track lead2  role=lead    inst=erhu|flute        vel=0.91  pan=-0.10",
        "track koto   role=rhythm  inst=pluck             vel=0.75  pan=0.30  ctr=E3 pat=rollE",
        "track harp   role=rhythm  inst=harp              vel=0.90 pan=-0.35 ctr=E3 pat=rollB p=60",
        "track pad    role=harmony inst=pad|strings       vel=0.77  pan=0     ctr=A3 pat=padN",
        "track bass   role=bass    inst=cello             vel=0.94  pan=-0.15 ctr=G2 pat=root",
        "track cello  role=counter inst=cello|horn        vel=1.02 pan=-0.20",
        "track bell   role=color   inst=bell              vel=0.94  pan=0.2",
        "track chime  role=color   inst=chime             vel=0.82  pan=-0.3",
        "track taiko  role=perc    inst=taiko             vel=1.01 pan=0     ctr=E2 pat=heart",
        "track dtaiko role=drive   inst=taiko             vel=1.27  pan=0     ctr=E2 pat=drive6 all",
        "track dcello role=drive   inst=cello             vel=0.94  pan=-0.1  ctr=E2 pat=ost6 all",

        "[intro]",
        "chords: Em | Cmaj7 | G | D",
        "koto: rollB",
        "pad: padN x.6",
        "bass: root x.5 @3-4",
        "bell: [E3 B3]:2",
        "lead: r:2 | r:2 | B4:2 | A4:2",

        "[A]",
        "chords: Em | Cmaj7 | G | D | Em | Cmaj7 | D | Em",
        "lead: " + MEL_A,
        "koto: rollE",
        "pad: padN x.65",
        "bass: root x.8",
        "bell: [E3 B3]:2",
        "chime: r:2 | r:2 | r:2 | r:2 | r:2 | r:2 | r:2 | B5:2",

        "[A2 < A]",
        "chords: Em | Cmaj7 | G | D | Em | Am | B7 | Em",
        "lead: r:2",
        "lead2: " + MEL_A2,
        "harp: rollB",
        "cello: G3:2 | E3:2 | D3:2 | F#3:2 | G3:2 | C4:2 | D#4:2 | E4:2",
        "pad: padN x.8",
        "bass: lilt",

        "[B]",
        "chords: G | D/F# | Em7 | D | C | G/B | Am7 | D",
        "lead: r:2",
        "lead2: " + MEL_B,
        "harp: -",
        "koto: rollE x.9",
        "cello: B3:2 | A3:2 | G3:2 | F#3:2 | E3:2 | D3:2 | C3:2 | D3:2",
        "pad: padN x.85",
        "bass: lilt",
        "bell: [G2 D3]:2",
        "chime: -",

        "[A3 < A]",
        "chords: Em | Cmaj7 | G | D | Em | Cmaj7 | D | Em",
        "lead: " + MEL_A3,
        "lead2: -",
        "cello: G3:2 | E3:2 | D3:2 | F#3:2 | G3:2 | E3:2 | F#3:2 | E3:2",
        "harp: rollB",
        "pad: padN x.8",
        "bass: lilt",
        "taiko: on",

        "[outro]",
        "chords: Em | Cmaj7 | D | Em",
        "lead: p B4:1 E5:2/3 D5:1/3 | C5:1 B4:2/3 G4:1/3 | A4:2 | E5:2",
        "koto: rollB",
        "pad: padN x.6",
        "bass: root x.5",
        "bell: r:2 | r:2 | r:2 | [E3 B3 E4]:2",
        "chime: r:2 | r:2 | r:2 | E6:2",
    };
}
