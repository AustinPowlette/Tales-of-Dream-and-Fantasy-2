from cw import *

PIECES = []

PIECES.append(P("Cicadas at Noon", "season", 108, 4, "A4", 'mixo', "arp4",
    "A | G | D | A | A | G | D | Em", "D | A | G | A | D | A | G | A", "A | G | D | A | A | G | D | A",
    "E5:.5 E5:.5 E5:.5 E5:.5 A5:1 E5:1 | D5:.5 D5:.5 D5:.5 D5:.5 G5:1 D5:1 | F#5:.5 F#5:.5 F#5:.5 F#5:.5 A5:1 D6:1 | C#6:2 A5:1 E5:1 | E5:.5 E5:.5 A5:.5 A5:.5 C#6:1 A5:1 | D6:.5 D6:.5 B5:.5 B5:.5 G5:1 D5:1 | A5:1 F#5:1 D5:1 F#5:1 | G5:1 B5:1 E6:2",
    "D6:1 A5:1 F#5:1 A5:1 | C#6:1 E6:1 C#6:1 A5:1 | D6:.5 D6:.5 B5:.5 B5:.5 G5:1 B5:1 | A5:1 C#6:1 E6:2 | A5:.5 A5:.5 D6:.5 D6:.5 F#5:1 A5:1 | E5:1 A5:1 C#6:2 | B5:1 G5:1 D5:1 G5:1 | A5:3 r:1",
    "F#5:1 A5:1 D6:1 A5:1 | A5:3 r:1", season=1))

PIECES.append(P("Dragon Dance in the Square", "festival", 126, 4, "G4", 'pent', "arp4",
    "G | C | G | D | G | C | D | G", "C | G | Em | D | C | G | D | G", "G | C | G | D | G | C | D | G",
    "G5:.5 B5:.5 D6:1 B5:.5 G5:.5 B5:1 | E5:.5 G5:.5 E6:1 G5:.5 E5:.5 G5:1 | D6:.5 B5:.5 G5:1 B5:.5 D6:.5 B5:1 | A5:1 D6:1 A5:.5 B5:.5 A5:1 | G5:.5 B5:.5 D6:1 G5:1 B5:1 | E6:1 D6:.5 B5:.5 G5:1 E5:1 | A5:.5 D6:.5 A5:1 B5:.5 A5:.5 D6:1 | D6:1 B5:1 G5:2",
    "G5:.5 E5:.5 G5:1 E6:1 G5:1 | B5:.5 D6:.5 B5:1 G5:.5 B5:.5 D6:1 | E6:1 B5:.5 G5:.5 E5:1 G5:1 | A5:1 D6:1 A5:.5 B5:.5 A5:1 | E5:.5 G5:.5 E6:1 G5:1 E5:1 | B5:1 D6:.5 B5:.5 G5:1 D6:1 | A5:1 D6:1 A5:1 D6:1 | B5:1 G5:1 D5:2",
    "A5:1 D6:1 A5:.5 B5:.5 A5:1 | G5:3 r:1"))

PIECES.append(P("Over the Salt Road", "road", 84, 4, "D4", 'dorian', "arp4",
    "Dm | C | Dm | Am | Dm | C | Am | Dm", "F | C | Dm | Am | F | C | Am | Dm", "Dm | C | Dm | Am | Dm | C | C | Dm",
    "D5:1 F5:1 A5:1 F5:1 | E5:1 G5:1 C6:1 G5:1 | A5:1 F5:1 D5:1 F5:1 | C6:1 A5:1 E5:1 A5:1 | D5:1.5 F5:.5 A5:1 D6:1 | C6:1.5 G5:.5 E5:1 G5:1 | A5:1 E5:1 C5:1 E5:1 | D5:3 r:1",
    "A5:1 C6:1 A5:1 F5:1 | G5:1 E5:1 G5:1 C6:1 | F5:1 A5:1 D6:1 A5:1 | C6:1 E6:1 C6:1 A5:1 | A5:1.5 G5:.5 F5:1 A5:1 | E5:1 G5:1 C6:2 | A5:1 C6:1 E6:1 C6:1 | A5:2 D5:2",
    "G5:1 E5:1 C5:2 | D5:3 r:1"))

PIECES.append(P("Mule Bells", "road", 94, 2, "C4", 'major', "arp68",
    "C | F | C | G | C | F | G | C", "Am | Dm | G | C | Am | F | G | C", "C | F | C | G | C | F | G | C",
    "E5:1 G5:1/3 E5:1/3 C5:1/3 | F5:1 A5:1/3 F5:1/3 C5:1/3 | E5:1 G5:2/3 E5:1/3 | D5:1 G5:1/3 B5:1/3 D6:1/3 | C6:1 G5:1/3 E5:1/3 G5:1/3 | A5:1 C6:2/3 A5:1/3 | B5:1 D6:1/3 B5:1/3 G5:1/3 | C6:1 G5:1",
    "E5:1 A5:1/3 C6:1/3 A5:1/3 | D5:1 F5:1/3 A5:1/3 F5:1/3 | G5:1 B5:1/3 D6:1/3 B5:1/3 | E6:1 C6:1/3 G5:1/3 E5:1/3 | C6:1 A5:1/3 E5:1/3 A5:1/3 | A5:1 C6:1/3 A5:1/3 F5:1/3 | D6:1 B5:1/3 G5:1/3 B5:1/3 | C6:1 E5:1",
    "B5:1 D6:1/3 B5:1/3 G5:1/3 | C6:2"))

PIECES.append(P("Copying the Third Scroll", "study", 72, 4, "G4", 'major', "arp4",
    "G | D | Em | Bm | C | G | Am | D", "C | G | Am | D | G | D | C | D", "G | D | Em | Bm | C | G | D7 | G",
    "G5:.5 A5:.5 B5:.5 D6:.5 B5:1 G5:1 | F#5:.5 G5:.5 A5:.5 D6:.5 A5:1 F#5:1 | E5:.5 F#5:.5 G5:.5 B5:.5 G5:1 E5:1 | D5:.5 E5:.5 F#5:.5 B5:.5 F#5:1 D5:1 | E5:.5 G5:.5 C6:.5 E6:.5 C6:1 G5:1 | B5:.5 C6:.5 D6:.5 B5:.5 G5:1 B5:1 | A5:.5 B5:.5 C6:.5 A5:.5 E5:1 A5:1 | D6:1 A5:1 F#5:2",
    "G5:.5 E5:.5 G5:.5 C6:.5 E6:1 C6:1 | B5:.5 A5:.5 G5:.5 B5:.5 D6:1 B5:1 | A5:.5 B5:.5 C6:.5 E6:.5 C6:1 A5:1 | F#5:.5 G5:.5 A5:.5 D6:.5 A5:1 F#5:1 | G5:.5 B5:.5 D6:.5 B5:.5 G5:1 D5:1 | F#5:.5 A5:.5 D6:.5 A5:.5 F#5:1 A5:1 | E5:.5 G5:.5 C6:.5 G5:.5 E5:1 G5:1 | A5:2 F#5:1 D5:1",
    "A5:1 C6:1 A5:1 F#5:1 | G5:3 r:1"))

PIECES.append(P("A Question in the Margin", "study", 80, 3, "A4", 'dorian', "arp3",
    "Am | D | Am | Em | Am | D | Em | Am", "C | G | D | Am | C | G | D | Am", "Am | D | Am | Em | Am | D | D | Am",
    "E5:1 A5:1 C6:1 | A5:2 F#5:1 | E5:1 C6:1 A5:1 | G5:2 B5:1 | C6:1 E6:1 C6:1 | D6:1 A5:1 F#5:1 | E5:1 G5:1 B5:1 | A5:3",
    "C6:1 G5:1 E5:1 | D5:1 G5:1 B5:1 | A5:2 D6:1 | C6:2 A5:1 | E5:1 G5:1 C6:1 | B5:1 D6:1 G5:1 | F#5:1 A5:1 D6:1 | C6:3",
    "F#5:2 A5:1 | A5:3"))

PIECES.append(P("Lantern Sellers' Round", "market", 112, 4, "F4", 'major', "arp4",
    "F | C | Dm | Bb | F | C | Bb | C", "Bb | F | Gm | C | Bb | F | C | C", "F | C | Dm | Bb | F | C | C | F",
    "A5:.5 A5:.5 C6:1 A5:1 F5:1 | G5:.5 G5:.5 E5:1 G5:1 C6:1 | F5:.5 F5:.5 A5:1 D6:1 A5:1 | D6:.5 D6:.5 Bb5:1 F5:1 Bb5:1 | A5:.5 A5:.5 C6:1 F6:1 C6:1 | E6:1 C6:.5 G5:.5 E5:1 G5:1 | D6:1 Bb5:1 F5:1 Bb5:1 | G5:1 C6:1 E6:2",
    "F5:1 Bb5:1 D6:1 Bb5:1 | A5:.5 A5:.5 C6:1 D6:2 | G5:1 Bb5:1 D6:2 | E6:1 G5:1 C6:2 | D6:.5 D6:.5 Bb5:1 F5:1 D6:1 | C6:1 A5:1 F5:2 | G5:1 C6:1 E6:1 C6:1 | D6:1 C6:1 G5:2",
    "G5:1 E5:1 G5:2 | A5:3 r:1"))

if __name__ == '__main__':
    report(PIECES)
    if '--java' in sys.argv:
        for p in PIECES: print(java(p))
