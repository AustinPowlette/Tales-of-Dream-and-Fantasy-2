package Game;

/** "The Red Tide Rises": war as a rush. E minor, 4/4 at 124 bpm, relentless: a horn riff that repeats its three short notes and leaps up (E E G E, then B), a cello
 *  chugging in eighths, a taiko on every beat with a double hit before the barline, a pipa raking the chord, string stabs, an erhu shrieking high over the middle,
 *  a bell on the first beat of each phrase. The B-major bar at the end of each phrase pulls the whole thing round again. Plays whatever its layers (`layers: fixed`). */
final class SongTide {
    private SongTide() { }

    private static String rep(String s, int n) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(i == 0 ? "" : " ").append(s); return b.toString(); }

    // chords Em | Em | C | D | Em | Am | C | B
    private static final String HORN_A =
        "E4:.5 E4:.5 G4:.5 E4:.5 B4:2 | E4:.5 E4:.5 G4:.5 E4:.5 B4:1 G4:1 | C5:.5 C5:.5 E5:.5 C5:.5 G5:2 | D5:.5 D5:.5 F#5:.5 D5:.5 A5:2"
      + " | E5:.5 E5:.5 G5:.5 E5:.5 B4:2 | A4:.5 A4:.5 C5:.5 A4:.5 E5:2 | C5:.5 E5:.5 G5:1 E5:1 C5:1 | B4:1 D#5:1 F#5:2";
    // chords Am | Em | C | D | Am | Em | B | B
    private static final String ERHU_B =
        "A5:2 C6:2 | B5:2 G5:2 | E6:2 C6:2 | F#6:2 D6:2 | C6:2 E6:2 | G5:2 B5:2 | D#6:2 B5:2 | F#5:4";

    static final String[] LINES = {
        "title: The Red Tide Rises",
        "bpm: 124",
        "beats: 4",
        "shift: -2 2",
        "moods: 4:5",
        "layers: fixed",
        "form: intro:mf A:f B:f A2:ff outro:mf",

        "pattern chug: " + "1:.5 1:.5 1:.5 1:.5 1:.5 1:.5 1:.5 1:.5",
        "pattern rake: =1:.25 =3:.25 =5:.25 =3':.25 =5':.25 =3':.25 =5:.25 =3:.25 =1:.25 =3:.25 =5:.25 =3':.25 =5':.25 =3':.25 =5:.25 =3:.25",
        "pattern stab: =1+=3+=5:1 r:3",
        "pattern swell: =1+=3+=5:*",
        "pattern beat: @E2:1 @E2:1 @E2:1 @E2:.5 @E2:.5",
        "pattern kickW: @kick>:1 @kick:.5 @kick:.5 @kick>:1 @kick:.5 @kick^:.5",
        "pattern snareW: r:1 @snare>:1 r:1 @snare>:1",
        "pattern hatW: @hat>:.5 @hat^:.5 @hat:.5 @hat^:.5 @hat>:.5 @hat^:.5 @hat:.5 @hat^:.5",
        "pattern crashW: @crash>:4",

        "track horn  role=lead    inst=horn|erhu       vel=1.29  pan=-0.10",
        "track erhu  role=lead    inst=erhu|flute      vel=0.70  pan=0.20",
        "track chug  role=rhythm  inst=cello           vel=0.95  pan=-0.20 ctr=E2 pat=chug",
        "track rake  role=rhythm  inst=pipa            vel=0.70  pan=0.35  ctr=E3 pat=rake",
        "track stab  role=harmony inst=strings         vel=0.50  pan=0.10  ctr=B3 pat=stab",
        "track choir role=harmony inst=choir           vel=0.60  pan=0     ctr=E4 pat=swell p=85",
        "track taiko role=perc    inst=taiko           vel=0.72  pan=0     pat=beat",
        "track bell  role=color   inst=bell            vel=0.65  pan=0.2",
        "track kick  role=perc    inst=kit      vel=0.64 pan=0    ctr=C2 pat=kickW",
        "track snare role=perc    inst=kit      vel=0.45 pan=0.1  ctr=C2 pat=snareW",
        "track hat   role=perc    inst=kit      vel=0.45 pan=-0.2 ctr=C2 pat=hatW",
        "track fill  role=perc    inst=kit      vel=0.45 pan=0.3",

        "[intro]",
        "chords: Em | Em | C | B",
        "kick: kickW x.8",
        "fill: crash>:4 | r:4 | r:4 | tom3:.5 tom3:.5 tom4:.5 tom4:.5 tom5:1 tom5:1",
        "chug: chug x.7",
        "taiko: beat x.8",
        "horn: r:4 | r:4 | C5:.5 C5:.5 E5:.5 C5:.5 G5:2 | B4:4",

        "[A]",
        "chords: Em | Em | C | D | Em | Am | C | B",
        "kick: kickW",
        "snare: snareW",
        "hat: hatW x.8",
        "fill: crash>:4 | r:4 | r:4 | r:4 | crash>:4 | r:4 | r:4 | snare:.25 snare:.25 snare:.25 snare:.25 tom3:.5 tom4:.5 tom5:2",
        "horn: " + HORN_A,
        "chug: chug",
        "rake: rake x.8",
        "stab: stab",
        "taiko: beat",
        "bell: [E2 B2]:4 | r:4 | r:4 | r:4 | [E2 B2]:4 | r:4 | r:4 | r:4",

        "[B]",
        "chords: Am | Em | C | D | Am | Em | B | B",
        "kick: kickW",
        "snare: snareW",
        "hat: hatW",
        "fill: crash>:4 | r:4 | r:4 | r:4 | r:4 | r:4 | r:4 | tom1:.5 tom1:.5 tom3:.5 tom3:.5 tom4:.5 tom4:.5 tom5:.5 tom5:.5",
        "horn: -",
        "erhu: " + ERHU_B,
        "chug: chug x.9",
        "rake: rake",
        "stab: stab x.9",
        "choir: swell x.8",
        "taiko: beat x1.1",

        "[A2 < A]",
        "chords: Em | Em | C | D | Em | Am | C | B",
        "kick: kickW x1.1",
        "snare: snareW x1.1",
        "hat: hatW",
        "erhu: r:4 | r:4 | r:4 | r:4 | B5:4 | E6:4 | G6:4 | F#6:4",
        "choir: swell",
        "taiko: beat x1.2",

        "[outro]",
        "chords: Em | C | B | Em",
        "kick: kickW x.6",
        "snare: -",
        "hat: -",
        "fill: tom5:1 tom5:1 r:2 | r:4 | r:4 | crash>:4",
        "horn: E5:.5 E5:.5 G5:.5 E5:.5 B4:2 | C5:.5 C5:.5 E5:.5 C5:.5 G5:2 | B4:4 | E4:4",
        "erhu: -",
        "chug: chug x.6",
        "rake: -",
        "stab: -",
        "choir: -",
        "taiko: beat x.7",
        "bell: r:4 | r:4 | r:4 | [E2 B2 E3]:4",
    };
}
