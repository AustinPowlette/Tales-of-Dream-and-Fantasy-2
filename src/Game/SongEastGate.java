package Game;

/** "Drums of the Eastern Gate": war as a chant. A minor in a heavy twelve-eight (a bar is four beats of three eighths, 80 bpm): a taiko triplet that never lets up, a
 *  low horn rising through the triad (A C E, A), a choir chanting the chord under it, an erhu calling above in the middle, a pipa plucking in triplets,
 *  strings stabbing on one. It sounds more like a ritual than a charge. Plays whatever its layers (`layers: fixed`). */
final class SongEastGate {
    private SongEastGate() { }

    // chords Am | Am | F | G | Am | Dm | E | Am
    private static final String HORN_A =
        "A3:1 C4:2/3 E4:1/3 A4:2 | A4:1 E4:2/3 C4:1/3 A3:2 | F4:1 A4:2/3 C5:1/3 F5:2 | D5:1 B4:2/3 G4:1/3 D5:2"
      + " | A4:1 C5:2/3 E5:1/3 A5:2 | D5:1 F5:2/3 A5:1/3 F5:2 | E5:1 G#5:2/3 B5:1/3 E5:2 | A4:4";
    // chords C | G | Am | F | C | G | E | E
    private static final String ERHU_B =
        "E5:2 G5:2 | D6:2 B5:2 | C6:2 A5:2 | A5:2 C6:2 | E6:2 C6:2 | B5:2 D6:2 | G#5:2 B5:2 | B5:4";

    static final String[] LINES = {
        "title: Drums of the Eastern Gate",
        "bpm: 80",
        "beats: 4",
        "shift: -2 2",
        "moods: 4:5",
        "layers: fixed",
        "form: intro:mf A:mf B:f A2:ff outro:mf",

        "pattern trip: 1:1/3 5:1/3 3':1/3 1':1/3 3':1/3 5:1/3 1:1/3 5:1/3 3':1/3 1':1/3 3':1/3 5:1/3",
        "pattern stab: =1+=3+=5:1 r:3",
        "pattern swell: =1+=3+=5:*",
        "pattern root: b:*",
        "pattern drum: @A1:1 r:1/3 @A1:1/3 @A1:1/3 @A1:1 r:1/3 @A1:1/3 @A1:1/3",
        "pattern bellE: @cow>:2/3 @cow:2/3 @cow^:1/3 @cow:2/3 @cow:2/3 @cow:2/3 @cow^:1/3",
        "pattern kickE: @kick>:1 r:1 @kick:1 r:1",
        "pattern congaE: @conga>:1 @mconga^:1/3 @mconga:1/3 @mconga:1/3 @conga2:1 @conga:2/3 @mconga^:1/3",
        "pattern shkE: @shaker>:1/3 @shaker^:1/3 @shaker^:1/3 @shaker>:1/3 @shaker^:1/3 @shaker^:1/3 @shaker>:1/3 @shaker^:1/3 @shaker^:1/3 @shaker>:1/3 @shaker^:1/3 @shaker^:1/3",
        "pattern floorE: r:3 @tom5>:1",

        "track horn  role=lead    inst=horn|cello      vel=1.43  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.65  pan=0.20",
        "track pipa  role=rhythm  inst=pipa            vel=0.81  pan=0.35  ctr=A3 pat=trip",
        "track stab  role=harmony inst=strings         vel=0.50  pan=0.10  ctr=C4 pat=stab",
        "track choir role=harmony inst=choir           vel=0.68  pan=0     ctr=C4 pat=swell",
        "track bass  role=bass    inst=cello           vel=0.88  pan=-0.15 ctr=A1 pat=root",
        "track taiko role=perc    inst=taiko           vel=0.84  pan=0     pat=drum",
        "track bell  role=color   inst=bell            vel=0.71  pan=0.2",
        "track cowb  role=perc    inst=kit      vel=1.40 pan=0.3   ctr=C2 pat=bellE",
        "track kick  role=perc    inst=kit      vel=0.83 pan=0     ctr=C2 pat=kickE",
        "track cong  role=perc    inst=kit      vel=0.81 pan=-0.3  ctr=C2 pat=congaE",
        "track shk   role=perc    inst=kit      vel=0.75 pan=0.2   ctr=C2 pat=shkE",
        "track floor role=perc    inst=kit      vel=0.69 pan=0     ctr=C2 pat=floorE",

        "[intro]",
        "chords: Am | Am | F | E",
        "kick: kickE x.8",
        "cowb: bellE x.6",
        "taiko: drum x.8",
        "bass: root x.6",
        "choir: swell x.5",

        "[A]",
        "chords: Am | Am | F | G | Am | Dm | E | Am",
        "cowb: bellE",
        "kick: kickE",
        "shk: shkE x.8",
        "horn: " + HORN_A,
        "pipa: trip x.8",
        "stab: stab",
        "choir: swell x.7",
        "bass: root",
        "taiko: drum",
        "bell: [A2 E3]:4 | r:4 | r:4 | r:4 | [A2 E3]:4 | r:4 | r:4 | r:4",

        "[B]",
        "chords: C | G | Am | F | C | G | E | E",
        "cong: congaE",
        "cowb: bellE",
        "kick: kickE",
        "shk: shkE",
        "floor: floorE",
        "horn: -",
        "erhu: " + ERHU_B,
        "pipa: trip",
        "stab: stab x.9",
        "choir: swell x.9",
        "bass: root x.9",
        "taiko: drum x1.1",

        "[A2 < A]",
        "chords: Am | Am | F | G | Am | Dm | E | Am",
        "cong: congaE",
        "floor: floorE",
        "kick: kickE x1.1",
        "erhu: r:4 | r:4 | r:4 | r:4 | A5:4 | D6:4 | E6:4 | A5:4",
        "choir: swell",
        "taiko: drum x1.2",

        "[outro]",
        "chords: Am | F | E | Am",
        "kick: kickE x.6",
        "cowb: bellE x.5",
        "horn: A4:1 E4:2/3 C4:1/3 A3:2 | F4:1 A4:2/3 C5:1/3 F5:2 | E5:4 | A4:4",
        "erhu: -",
        "pipa: trip x.5",
        "stab: -",
        "choir: swell x.5",
        "taiko: drum x.7",
        "bell: r:4 | r:4 | r:4 | [A2 E3 A3]:4",
    };
}
