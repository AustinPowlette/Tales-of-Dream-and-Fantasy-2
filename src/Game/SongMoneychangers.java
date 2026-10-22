package Game;

/** "The Moneychangers' Waltz": a city song in three. B minor with a sudden turn to D major in the middle, a lilting 3/4 at 100 bpm: a harp or cello on the
 *  first beat, a pipa answering on two and three, an erhu singing the tune (up the triad, down to the leading note) and a flute taking it in thirds the
 *  second time, a wood tick for coins. Plays with the usual layers. */
final class SongMoneychangers {
    private SongMoneychangers() { }

    // chords Bm | Bm | G | F# | Bm | Em | F# | Bm
    private static final String MEL_A =
        "B4:1 D5:1 F#5:1 | B5:2 F#5:1 | G5:1 B5:1 D6:1 | A#5:1 C#6:1 F#5:1"
      + " | B5:1 D6:1 F#6:1 | E6:1 B5:1 G5:1 | C#6:1 A#5:1 F#5:1 | B5:1 F#5:1 B4:1";
    // chords D | A | Bm | F#m | G | D/F# | Em A | D
    private static final String MEL_B =
        "D5:1 F#5:1 A5:1 | C#6:2 A5:1 | D6:1 B5:1 F#5:1 | A5:2 C#6:1"
      + " | B5:1 D6:1 G6:1 | F#6:2 D6:1 | G5:.5 B5:1 C#6:.5 E6:1 | D6:1 A5:1 D5:1";

    static final String[] LINES = {
        "title: The Moneychangers' Waltz",
        "bpm: 100",
        "beats: 3",
        "shift: -2 2",
        "moods: 1:4 6:1",
        "form: intro?:mp A:mf A2:mf B:mf A3:f outro:mp",

        "pattern oom: b:1 r:2",
        "pattern pah: r:1 =1+=3+=5:1 =1+=3+=5:1",
        "pattern arp3: 1:1 3:.5 5:.5 1':1",
        "pattern coin: @C6:.5 r:.5 r:1 @C6:.5 r:.5",
        "pattern padN: =1+=3+=5:*",

        "track lead   role=lead    inst=erhu|flute        vel=0.85 pan=-0.10",
        "track lead2  role=lead    inst=flute|pluck       vel=0.63 pan=0.20",
        "track pah    role=rhythm  inst=pipa|pluck        vel=0.75 pan=0.35  ctr=B3 pat=pah",
        "track oom    role=bass    inst=cello|harp        vel=1.07 pan=-0.20 ctr=B1 pat=oom",
        "track harp   role=rhythm  inst=harp              vel=0.65 pan=-0.35 ctr=B3 pat=arp3 p=70",
        "track pad    role=harmony inst=strings           vel=0.70 pan=0     ctr=D4 pat=padN",
        "track coin   role=perc    inst=wood              vel=0.70 pan=0.3   ctr=C6 pat=coin",
        "track bell   role=color   inst=bell              vel=0.85 pan=0.2",
        "track chime  role=color   inst=chime             vel=0.63 pan=-0.3",

        "[intro]",
        "chords: Bm | G | F# | Bm",
        "oom: oom x.8",
        "pah: pah x.7",
        "pad: padN x.5",
        "bell: [B2 F#3]:3",

        "[A]",
        "chords: Bm | Bm | G | F# | Bm | Em | F# | Bm",
        "lead: " + MEL_A,
        "oom: oom",
        "pah: pah",
        "pad: padN x.6",
        "coin: coin",
        "chime: r:3 | r:3 | r:3 | r:3 | r:3 | r:3 | r:3 | B6:3",

        "[A2 < A]",
        "lead2: " + SongTale.octave(MEL_A, 0),
        "harp: arp3",

        "[B]",
        "chords: D | A | Bm | F#m | G | D/F# | Em A | D",
        "lead: " + MEL_B,
        "lead2: -",
        "harp: -",
        "pad: padN x.8",
        "coin: -",
        "bell: [D3 A3]:3 | r:3 | r:3 | r:3 | [G2 D3]:3 | r:3 | r:3 | r:3",
        "chime: -",

        "[A3 < A]",
        "lead: " + MEL_A,
        "lead2: " + SongTale.octave(MEL_A, 0),
        "harp: arp3",
        "pad: padN x.8",

        "[outro]",
        "chords: Bm | G | F# | Bm",
        "lead: p B4:1 D5:1 F#5:1 | B5:2 G5:1 | C#6:1 A#5:1 F#5:1 | B4:3",
        "oom: oom x.6",
        "pah: pah x.5",
        "pad: padN x.5",
        "bell: r:3 | r:3 | r:3 | [B2 F#3 B3]:3",
        "chime: r:3 | r:3 | r:3 | B6:3",
    };
}
