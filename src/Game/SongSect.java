package Game;

/** "Morning Bell, Mountain Hall": the music of the sects. F major, slow 4/4: a bell that tolls the hour, a dizi singing long notes, a guqin (the koto sound)
 *  plucking slow arpeggios, a cello drone, a hushed choir, a wood block keeping the hour, an erhu answering high in the middle. Plays whatever its layers. */
final class SongSect {
    private SongSect() { }

    // chords Fadd9 | Bbmaj7 | Dm7 | C | Fadd9 | Gm7 | C | Fadd9
    private static final String MEL_A =
        "C5:2 A4:1 G4:1 | D5:3 C5:1 | A4:2 C5:1 D5:1 | E5:3 r:1 | C5:2 A4:1 F5:1 | Bb4:2 D5:1 F5:1 | E5:2 G5:2 | F5:3 r:1";
    // chords Dm | Bb | F | C/E | Dm | Gm7 | C | C
    private static final String MEL_B =
        "A5:2 F5:1 D5:1 | D5:2 F5:2 | A5:2 C6:2 | G5:2 E5:2 | F5:1 A5:1 D6:2 | Bb5:2 G5:2 | E5:2 G5:2 | C5:3 r:1";

    static final String[] LINES = {
        "title: Morning Bell, Mountain Hall",
        "bpm: 58",
        "beats: 4",
        "shift: -2 2",
        "moods: 5:5",
        "layers: fixed",
        "form: intro?:p A:mp B:mp A2:mf outro:p",

        "pattern arpH: 1:1 5:1 1':1 3':1",
        "pattern padN: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern knock: @A5:.5 r:1.5 @A5:.5 r:1.5",

        "track lead  role=lead    inst=flute|pluck     vel=0.83  pan=0.10",
        "track lead2 role=lead    inst=erhu|flute      vel=1.00  pan=-0.10",
        "track guqin role=rhythm  inst=pluck           vel=0.88  pan=0.30  ctr=F3 pat=arpH",
        "track pad   role=harmony inst=strings|pad     vel=0.84  pan=0     ctr=A3 pat=padN",
        "track choir role=harmony inst=choir           vel=1.11 pan=0     ctr=C4 pat=padN p=85",
        "track drone role=bass    inst=cello|horn      vel=1.35  pan=-0.15 ctr=G2 pat=root",
        "track wood  role=perc    inst=wood            vel=0.74  pan=0.3   pat=knock",
        "track bell  role=color   inst=bell            vel=1.04  pan=0.2",
        "track chime role=color   inst=chime           vel=0.82  pan=-0.3",

        "[intro]",
        "chords: Fadd9 | Bbmaj7 | Fadd9 | C",
        "bell: [F2 C3]:4 | r:4 | [F2 C3]:4",
        "pad: padN x.6",
        "choir: padN x.5 @2-4",
        "drone: root x.6",

        "[A]",
        "chords: Fadd9 | Bbmaj7 | Dm7 | C | Fadd9 | Gm7 | C | Fadd9",
        "lead: " + MEL_A,
        "guqin: arpH x.8",
        "pad: padN x.6",
        "drone: root x.7",
        "wood: knock x.5",
        "bell: [F2 C3]:4 | r:4 | r:4 | r:4 | [F2 C3]:4",
        "chime: r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | A6:4",

        "[B]",
        "chords: Dm | Bb | F | C/E | Dm | Gm7 | C | C",
        "lead: r:4",
        "lead2: " + MEL_B,
        "guqin: arpH",
        "choir: padN x.8",
        "pad: padN x.8",
        "bell: [D3 A3]:4 | r:4 | r:4 | r:4 | [D3 A3]:4",
        "chime: -",

        "[A2 < A]",
        "chords: Fadd9 | Bbmaj7 | Dm7 | C | Fadd9 | Gm7 | C | Fadd9",
        "lead: " + MEL_A,
        "lead2: " + SongTale.octave(MEL_A, -1),
        "choir: padN x.8",
        "pad: padN x.8",
        "wood: knock",

        "[outro]",
        "chords: Fadd9 | Bbmaj7 | C | Fadd9",
        "lead: p C5:2 A4:1 G4:1 | D5:4 | E5:4 | F5:4",
        "lead2: -",
        "guqin: arpH x.6 @1-3",
        "pad: padN x.5",
        "choir: -",
        "drone: root x.5",
        "wood: -",
        "bell: [F2 C3 F3]:4 | r:4 | r:4 | [F2 C3 F3]:4",
        "chime: r:4 | r:4 | r:4 | C6:4",
    };
}
