package Game;

/** "Sand and Drums": the music of a tournament bout (not part of the music the places choose from; `EventMusic` pins it while a bout is on screen). A minor, 4/4 at 116 bpm: a taiko gallop under a horn that states the challenge,
 *  an erhu that answers high, strings stabbing the beat, a wooden clapper like a crowd, a bell on the first bar of each phrase. It plays whatever its layers (`layers: fixed`). */
final class SongArena {
    private SongArena() { }

    // chords Am | F | C | G | Am | F | E | Am
    private static final String HORN_A =
        "A4:1 C5:1 E5:2 | F5:1.5 E5:.5 C5:2 | G5:1 E5:1 C5:2 | D5:2 G4:2"
      + " | A4:1 C5:1 E5:2 | F5:1.5 A5:.5 G5:2 | E5:1 G5:1 E5:1 D5:1 | A4:4";
    // chords Dm | G | C | Am | Dm | E | Am | Am
    private static final String ERHU_B =
        "D5:2 F5:2 | B4:2 D5:2 | E5:1 G5:1 C6:2 | A5:2 E5:2 | D5:1 F5:1 A5:2 | G5:2 B5:2 | C6:1 B5:1 A5:2 | A5:4";

    static final String[] LINES = {
        "title: Sand and Drums",
        "bpm: 116",
        "beats: 4",
        "shift: 0 0",
        "moods: 4:1",
        "layers: fixed",
        "form: intro:mf A:f B:f A2:ff outro:mf",

        "pattern bow: b:*",
        "pattern stab: =1+=3+=5:1 r:.5 =1+=3+=5:.5 r:1 =1+=3+=5:1",
        "pattern gallop: @A2:1 r:.5 @A2:.5 @A2:1 r:1",
        "pattern clap: r:1 @C6:.5 r:.5 r:1 @C6:.5 r:.5",
        "pattern climb: 1:1 3:1 5:1 1':1",

        "track horn  role=lead    inst=horn|cello      vel=1.30  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.80  pan=0.20",
        "track bow   role=bass    inst=cello           vel=0.90  pan=-0.15 ctr=A2 pat=bow",
        "track stab  role=harmony inst=strings         vel=0.55  pan=0.10  ctr=A3 pat=stab",
        "track climb role=rhythm  inst=harp            vel=0.60  pan=0.30  ctr=A3 pat=climb",
        "track taiko role=perc    inst=taiko           vel=0.90  pan=0     pat=gallop",
        "track wood  role=perc    inst=wood            vel=0.65  pan=0.3   pat=clap",
        "track bell  role=color   inst=bell            vel=0.80  pan=0.2",

        "[intro]",
        "chords: Am | Am | E | E",
        "bow: bow x.6",
        "taiko: gallop x.7",
        "bell: [A2 E3]:4 | r:4 | r:4 | r:4",

        "[A]",
        "chords: Am | F | C | G | Am | F | E | Am",
        "horn: " + HORN_A,
        "bow: bow",
        "stab: stab",
        "taiko: gallop",
        "wood: clap",
        "bell: [A2 E3]:4 | r:4 | r:4 | r:4 | [A2 E3]:4 | r:4 | r:4 | r:4",

        "[B]",
        "chords: Dm | G | C | Am | Dm | E | Am | Am",
        "horn: -",
        "erhu: " + ERHU_B,
        "climb: climb",
        "bow: bow x.9",
        "stab: stab x.9",
        "taiko: gallop x1.1",
        "wood: clap",

        "[A2 < A]",
        "chords: Am | F | C | G | Am | F | E | Am",
        "erhu: " + SongTale.octave(HORN_A, 1),
        "climb: climb",
        "taiko: gallop x1.2",

        "[outro]",
        "chords: Am | F | E | Am",
        "horn: A4:1 C5:1 E5:2 | F5:1.5 E5:.5 C5:2 | E5:1 G5:1 E5:2 | A4:4",
        "erhu: -",
        "climb: -",
        "bow: bow x.7",
        "stab: -",
        "taiko: gallop x.8",
        "wood: -",
        "bell: r:4 | r:4 | r:4 | [A2 E3 A3]:4",
    };
}
