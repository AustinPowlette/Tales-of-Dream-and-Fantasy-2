package Game;

/** "Incense at the Ninth Hour": a second song for the sects. C major, slow and open 4/4 (52 bpm): a dizi on long pentatonic notes (E G, G E D, A), a guqin
 *  plucking a rising figure under it, a cello drone with a long pedal, an erhu entering in the middle on the relative minor, a hushed choir, a wood block
 *  keeping the hour and a bell tolled three times at the end. Plays whatever its layers (`layers: fixed`). */
final class SongIncense {
    private SongIncense() { }

    // chords Cmaj7 | Em7 | Fmaj7 | G | Cmaj7 | Am7 | Dm7 | G
    private static final String MEL_A =
        "E5:3 G5:1 | G5:2 E5:1 D5:1 | A5:3 G5:1 | D5:3 B4:1"
      + " | E5:2 G5:2 | C6:2 A5:1 E5:1 | F5:2 D5:1 A4:1 | D5:4";
    // chords Am7 | Fmaj7 | Cmaj7 | G | Am7 | Dm7 | G | Cmaj7
    private static final String MEL_B =
        "E5:2 A5:2 | C6:3 A5:1 | G5:2 E5:2 | D5:2 B4:2"
      + " | C5:2 E5:1 A5:1 | F5:3 D5:1 | B5:2 G5:2 | C6:3 r:1";
    // the last time, chords ... Dm7 G | Cmaj7: the tune comes home
    private static final String MEL_A2 =
        "E5:3 G5:1 | G5:2 E5:1 D5:1 | A5:3 G5:1 | D5:3 B4:1"
      + " | E5:2 G5:2 | C6:2 A5:1 E5:1 | F5:1 D5:1 B4:1 D5:1 | E5:3 r:1";

    static final String[] LINES = {
        "title: Incense at the Ninth Hour",
        "bpm: 52",
        "beats: 4",
        "shift: -2 2",
        "moods: 5:5",
        "layers: fixed",
        "form: intro?:p A:mp B:mp A2:mf outro:p",

        "pattern arpH: 1:1 5:1 1':1 3':1",
        "pattern climb: 1:.5 3:.5 5:.5 1':.5 3':.5 5:.5 3':.5 1':.5",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern knock: @A5:.5 r:1.5 @A5:.5 r:1.5",

        "track lead  role=lead    inst=flute|pluck     vel=0.78  pan=0.10",
        "track lead2 role=lead    inst=erhu|flute      vel=1.00  pan=-0.10",
        "track guqin role=rhythm  inst=pluck           vel=0.78  pan=0.30  ctr=C3 pat=arpH",
        "track harp  role=rhythm  inst=harp            vel=1.20  pan=-0.35 ctr=C3 pat=climb p=70",
        "track pad   role=harmony inst=strings|pad     vel=0.88  pan=0     ctr=G3 pat=padN",
        "track choir role=harmony inst=choir           vel=0.98  pan=0     ctr=E4 pat=padN p=85",
        "track drone role=bass    inst=cello|horn      vel=1.35  pan=-0.15 ctr=G2 pat=root",
        "track wood  role=perc    inst=wood            vel=1.10  pan=0.3   pat=knock",
        "track bell  role=color   inst=bell            vel=1.04  pan=0.2",
        "track chime role=color   inst=chime           vel=0.77  pan=-0.3",

        "[intro]",
        "chords: Cmaj7 | Fmaj7 | Cmaj7 | G",
        "bell: [C3 G3]:4 | r:4 | [C3 G3]:4",
        "pad: padN x.6",
        "choir: padN x.5 @2-4",
        "drone: root x.6",

        "[A]",
        "chords: Cmaj7 | Em7 | Fmaj7 | G | Cmaj7 | Am7 | Dm7 | G",
        "lead: " + MEL_A,
        "guqin: arpH x.8",
        "pad: padN x.6",
        "drone: root x.7",
        "wood: knock x.5",
        "bell: [C3 G3]:4 | r:4 | r:4 | r:4 | [C3 G3]:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | E6:4",

        "[B]",
        "chords: Am7 | Fmaj7 | Cmaj7 | G | Am7 | Dm7 | G | Cmaj7",
        "lead: r:4",
        "lead2: " + MEL_B,
        "guqin: arpH",
        "harp: climb x.8",
        "choir: padN x.8",
        "pad: padN x.8",
        "bell: [A2 E3]:4 | r:4 | r:4 | r:4 | [A2 E3]:4",
        "chime: -",

        "[A2 < A]",
        "chords: Cmaj7 | Em7 | Fmaj7 | G | Cmaj7 | Am7 | Dm7 G | Cmaj7",
        "lead: " + MEL_A2,
        "lead2: " + SongTale.octave(MEL_A2, -1),
        "guqin: arpH",
        "choir: padN x.8",
        "pad: padN x.8",

        "[outro]",
        "chords: Cmaj7 | Fmaj7 | G | Cmaj7",
        "lead: p E5:3 G5:1 | A5:3 G5:1 | D5:4 | C5:4",
        "guqin: arpH x.6",
        "pad: padN x.5",
        "drone: root x.5",
        "bell: r:4 | r:4 | [G2 D3]:4 | [C3 G3 C4]:4",
        "chime: r:4 | r:4 | r:4 | C6:4",
    };
}
