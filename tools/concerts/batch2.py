from cw import *

PIECES = []

PIECES.append(P("Banners at the Eastern Pass", "war", 108, 4, "E4", 'minor', "arp4",
    "Em | C | D | Em | Em | C | D | Em", "Am | Em | C | D | Am | Em | D | Em", "Em | C | D | Em | Em | C | Bm | Em",
    "B4:1.5 B4:.5 E5:1 G5:1 | G5:1.5 G5:.5 E5:1 C5:1 | A5:1.5 F#5:.5 D5:1 F#5:1 | G5:2 E5:1 B4:1 | B4:1.5 B4:.5 E5:1 G5:1 | C6:1.5 B5:.5 G5:1 E5:1 | A5:1 F#5:1 D5:1 F#5:1 | E5:3 r:1",
    "E5:1.5 E5:.5 A5:1 C6:1 | B5:1.5 B5:.5 G5:1 E5:1 | E5:1.5 E5:.5 G5:1 C6:1 | D6:1.5 C6:.5 A5:1 F#5:1 | C6:1.5 B5:.5 A5:1 E5:1 | G5:1.5 G5:.5 B5:1 E6:1 | D6:1 A5:1 F#5:1 D5:1 | E5:3 r:1",
    "F#5:1 B5:1 D6:1 B5:1 | E5:3 r:1"))

PIECES.append(P("Spear Drum Answer", "war", 92, 2, "A4", 'minor', "arp68",
    "Am | G | Am | Em | Am | G | Em | Am", "C | G | F | Am | C | G | Em | Am", "Am | G | Am | Em | Am | G | Em | Am",
    "A4:1 C5:1/3 E5:1/3 A5:1/3 | G5:1 D5:1/3 B4:1/3 D5:1/3 | E5:1 C5:1/3 E5:1/3 A5:1/3 | G5:1 E5:1/3 B4:1/3 E5:1/3 | A5:1 E5:1/3 C5:1/3 E5:1/3 | B5:1 G5:1/3 D5:1/3 G5:1/3 | E5:1/3 G5:1/3 B5:1/3 E6:1 | A5:2",
    "G5:1 C6:1/3 G5:1/3 E5:1/3 | B5:1 D6:1/3 B5:1/3 G5:1/3 | A5:1 C6:1/3 A5:1/3 F5:1/3 | C6:1 A5:1/3 E5:1/3 A5:1/3 | G5:1 E5:1/3 G5:1/3 C6:1/3 | B5:1 D6:1/3 G5:1/3 B5:1/3 | G5:1 B5:1/3 E6:1/3 B5:1/3 | A5:1 C6:1/3 A5:1/3 E5:1/3",
    "E5:1 G5:1/3 B5:1/3 G5:1/3 | A5:1 E5:1"))

PIECES.append(P("Retreat Across the River", "war", 76, 4, "D4", 'harm', "arp4",
    "Dm | Gm | A | Dm | Bb | Gm | A | Dm", "F | C | Gm | A | Bb | Gm | A | A", "Dm | Gm | A | Dm | Bb | Gm | A7 | Dm",
    "A4:2 D5:1 F5:1 | G5:1.5 F5:.5 D5:1 Bb4:1 | E5:2 C#5:1 A4:1 | D5:3 r:1 | D5:2 F5:1 Bb5:1 | G5:1.5 F5:.5 D5:1 Bb4:1 | C#5:1 E5:1 A5:1 G5:.5 E5:.5 | F5:2 D5:2",
    "A5:2 F5:1 A5:1 | G5:2 E5:1 G5:1 | D5:2 G5:1 Bb5:1 | A5:2 E5:1 C#5:1 | D5:2 F5:1 D5:1 | Bb5:2 G5:1 D5:1 | E5:2 A5:1 C#6:1 | A5:3 r:1",
    "C#5:1 E5:1 G5:1 E5:1 | D5:3 r:1"))

PIECES.append(P("Spilled Wine Reel", "tavern", 92, 2, "G4", 'major', "arp68",
    "G | G | C | D | G | Em | D | G", "C | G | D | G | C | G | D | G", "G | G | C | D | G | Em | D | G",
    "B4:1/3 D5:1/3 G5:1/3 B5:1/3 G5:1/3 D5:1/3 | G5:1/3 A5:1/3 B5:1/3 D6:2/3 B5:1/3 | C6:1/3 B5:1/3 A5:1/3 G5:1/3 E5:1/3 C5:1/3 | D5:1/3 F#5:1/3 A5:1/3 D6:1 | B5:1/3 G5:1/3 D5:1/3 G5:1/3 B5:1/3 D6:1/3 | E6:1/3 D6:1/3 B5:1/3 G5:1/3 B5:1/3 E5:1/3 | F#5:1/3 A5:1/3 D6:1/3 B5:1/3 A5:1/3 F#5:1/3 | G5:1 D5:1",
    "E5:1/3 G5:1/3 C6:1/3 E6:2/3 C6:1/3 | D6:1/3 B5:1/3 G5:1/3 D5:1/3 G5:1/3 B5:1/3 | A5:1/3 F#5:1/3 D5:1/3 F#5:1/3 A5:1/3 D6:1/3 | D6:1/3 B5:1/3 G5:1/3 B5:2/3 G5:1/3 | C6:1/3 G5:1/3 E5:1/3 G5:1/3 C6:1/3 E6:1/3 | D6:1/3 B5:1/3 G5:1/3 B5:1/3 D6:1/3 B5:1/3 | A5:1/3 D6:1/3 A5:1/3 F#5:1/3 A5:1/3 D5:1/3 | G5:1 D5:1",
    "A5:1/3 F#5:1/3 D5:1/3 A5:2/3 F#5:1/3 | G5:2"))

PIECES.append(P("The Landlord's Complaint", "tavern", 100, 4, "C4", 'major', "arp4",
    "C | Am | F | C | Dm | G | C | G", "F | C | G | C | F | C | G | C", "C | Am | F | C | Dm | G | G7 | C",
    "E5:.5 E5:.5 E5:.5 E5:.5 G5:1 E5:1 | E5:.5 E5:.5 E5:.5 E5:.5 A5:1 E5:1 | F5:.5 F5:.5 F5:.5 F5:.5 A5:1 C6:1 | C6:2 G5:1 E5:1 | D5:.5 D5:.5 F5:.5 F5:.5 A5:1 F5:1 | G5:.5 G5:.5 B5:.5 B5:.5 D6:1 B5:1 | C6:1.5 B5:.5 G5:1 E5:1 | D5:2 B4:2",
    "F5:1 A5:1 C6:1 A5:1 | G5:.5 G5:.5 E5:.5 E5:.5 C5:1 E5:1 | D5:.5 D5:.5 G5:.5 G5:.5 B5:1 G5:1 | C6:2 G5:2 | F5:.5 F5:.5 A5:.5 A5:.5 C6:1 A5:1 | G5:1 E5:1 C5:2 | D5:1 G5:1 B5:1 D6:1 | C6:2 G5:1 E5:1",
    "D5:1 F5:1 B5:1 G5:1 | C5:2 E5:1 C5:1"))

PIECES.append(P("Haggling at the Spice Row", "market", 120, 4, "D4", 'mixo', "arp4",
    "D | C | G | D | D | C | G | Am", "G | D | C | D | G | D | C | D", "D | C | G | D | D | C | Am | D",
    "F#5:.5 A5:.5 D6:1 A5:.5 F#5:.5 A5:1 | G5:.5 E5:.5 C6:1 G5:.5 E5:.5 G5:1 | B5:.5 D6:.5 G5:1 B5:.5 D6:.5 B5:1 | A5:.5 F#5:.5 D5:1 F#5:.5 A5:.5 D6:1 | D6:1.5 C6:.5 A5:1 F#5:1 | E5:.5 G5:.5 C6:1 G5:1 E5:1 | G5:1 B5:1 D6:1 B5:1 | E5:1 A5:1 C6:1 A5:1",
    "D6:1 B5:.5 G5:.5 B5:1 D6:1 | A5:1 F#5:.5 A5:.5 D6:1 A5:1 | G5:1 E5:.5 G5:.5 C6:1 E6:1 | D6:1 A5:.5 F#5:.5 A5:1 D6:1 | B5:1 G5:.5 B5:.5 D6:1 B5:1 | A5:.5 D6:.5 F#5:1 A5:.5 D6:.5 A5:1 | E5:1 G5:1 C6:1 G5:1 | D6:2 A5:1 F#5:1",
    "E5:1 A5:1 C6:1 E6:1 | D6:2 A5:2"))

if __name__ == '__main__':
    report(PIECES)
    if '--java' in sys.argv:
        for p in PIECES: print(java(p))
