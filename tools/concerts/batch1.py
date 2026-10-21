from cw import *

PIECES = []

PIECES.append(P("Red Veil Procession", "wedding", 92, 4, "D4", 'major', "arp4",
    "D | A | Bm | G | D | A | Em | A", "G | D | Em | A | G | D | A | D", "D | A | Bm | G | D | A | A7 | D",
    "F#5:1.5 A5:.5 F#5:1 D5:1 | E5:1.5 C#5:.5 A4:1 C#5:1 | D5:1.5 F#5:.5 B5:1 F#5:1 | G5:1.5 F#5:.5 D5:1 B4:1 | F#5:1.5 A5:.5 D6:1 A5:1 | C#6:1.5 B5:.5 A5:1 E5:1 | B5:1 G5:1 E5:1 G5:1 | E5:3 r:1",
    "B5:2 D6:1 B5:1 | A5:1.5 F#5:.5 D5:1 F#5:1 | G5:1 E5:1 B4:1 G5:1 | C#6:2 A5:1 E5:1 | B5:1.5 A5:.5 G5:1 B5:1 | A5:1.5 F#5:.5 A5:1 D6:1 | C#6:1 E6:1 C#6:1 A5:1 | D6:3 r:1",
    "E5:1 A5:1 G5:1 E5:1 | D5:3 r:1"))

PIECES.append(P("Cups of Two Households", "wedding", 90, 2, "F4", 'pent', "arp68",
    "F | Dm | Bb | C | F | Dm | Bb | C", "Bb | C | F | Dm | Bb | C | Dm | F", "F | Dm | Bb | C | F | Dm | C | F",
    "A4:1 C5:2/3 A4:1/3 | D5:1 C5:1/3 A4:1/3 F4:1/3 | D5:1 F5:2/3 D5:1/3 | G4:1 C5:1 | A4:1 C5:1/3 D5:1/3 C5:1/3 | F5:1 D5:2/3 C5:1/3 | D5:1 F5:1/3 G5:1/3 F5:1/3 | C5:1 G4:1",
    "D5:1 F5:1/3 D5:1/3 F5:1/3 | G5:1 C6:2/3 G5:1/3 | A5:1 F5:2/3 A5:1/3 | D6:1 C6:1/3 A5:1/3 F5:1/3 | D6:2/3 C6:1/3 D6:1 | C6:1 G5:1/3 A5:1/3 G5:1/3 | A5:1 F5:1/3 A5:1/3 D6:1/3 | F5:2",
    "G5:1 A5:1/3 G5:1/3 C6:1/3 | F5:2"))

PIECES.append(P("The Bridal Chair Dance", "wedding", 116, 4, "G4", 'major', "arp4",
    "G | D | Em | C | G | D | C | D", "C | G | Am | D | C | G | D | G", "G | D | Em | C | G | D | D7 | G",
    "B4:1 D5:.5 G5:.5 B5:1 G5:1 | A5:1 F#5:.5 D5:.5 F#5:1 A5:1 | G5:1 B5:.5 G5:.5 E5:1 G5:1 | E5:1 G5:.5 E5:.5 C5:1 E5:1 | D5:.5 G5:.5 B5:1 D6:1 B5:1 | A5:1 D6:.5 B5:.5 A5:1 F#5:1 | G5:1 E5:.5 G5:.5 C6:1 G5:1 | A5:2 F#5:1 D5:1",
    "E5:.5 G5:.5 C6:1 E6:1 C6:1 | D6:1 B5:.5 G5:.5 B5:1 D6:1 | C6:1 A5:.5 E5:.5 A5:1 C6:1 | A5:1 D6:.5 A5:.5 F#5:1 A5:1 | G5:1 C6:.5 E6:.5 C6:1 G5:1 | B5:1 D6:.5 B5:.5 G5:1 B5:1 | A5:1 F#5:.5 A5:.5 D6:2 | D6:1 B5:1 G5:2",
    "A5:1 C6:1 A5:1 F#5:1 | G5:3 r:1"))

PIECES.append(P("Incense Before Dawn", "temple", 48, 4, "A4", 'minorpent', "arp4",
    "Am | Dm | Am | Em | Am | Dm | Em | Am", "Dm | Am | F | Em | Dm | Am | Em | Am", "Am | Dm | Am | Em | Am | Dm | Em | Am",
    "E5:2 G5:.5 E5:.5 C5:1 | D5:1.5 E5:.5 A4:2 | C5:2 D5:.5 E5:.5 C5:1 | G5:2 A5:.5 G5:.5 E5:1 | A5:3 G5:.5 E5:.5 | D5:2 A4:1 D5:1 | E5:2 G5:1 E5:1 | A4:3 r:1",
    "A5:2 D6:2 | C6:3 A5:1 | C6:2 A5:2 | G5:2 E5:2 | D5:3 A4:1 | C5:2 E5:2 | G5:3 E5:1 | E5:2 A4:2",
    "G5:2 E5:2 | A4:3 r:1"))

PIECES.append(P("Bell Tower Vespers", "temple", 58, 3, "D4", 'dorian', "arp3",
    "Dm | G | Dm | C | Dm | G | Am | Dm", "F | C | G | Dm | F | C | G | Dm", "Dm | G | Dm | C | Dm | G | C | Dm",
    "A5:2 F5:1 | G5:2 D5:1 | F5:2 D5:1 | E5:2 G5:1 | A5:1 F5:1 D5:1 | G5:1 B5:1 D5:1 | E5:2 A5:1 | D5:3",
    "A5:2 C6:1 | G5:2 E5:1 | D5:2 G5:1 | F5:2 A5:1 | C6:1 A5:1 F5:1 | E5:1 G5:1 C6:1 | B5:2 G5:1 | A5:3",
    "E5:2 G5:1 | D5:3"))

PIECES.append(P("Nine Prostrations", "temple", 52, 4, "E4", 'minorpent', "arp4",
    "Em | Am | Em | Bm | Em | Am | Bm | Em", "G | D | Am | Em | G | D | Bm | Em", "Em | Am | Em | Bm | Em | Am | D | Em",
    "B4:2 G4:1 B4:1 | E5:2 A4:2 | G4:1 B4:1 E5:2 | D5:2 B4:2 | E5:1.5 D5:.5 B4:1 G4:1 | A4:1 E5:1 A5:2 | B5:2 D6:1 B5:1 | E5:3 r:1",
    "D5:2 G5:2 | A5:2 D6:2 | A5:2 E5:2 | G5:2 B5:2 | B5:1.5 A5:.5 G5:1 D5:1 | A4:1 D5:1 A5:2 | D6:2 B5:2 | G5:2 E5:2",
    "A5:2 D5:2 | E5:3 r:1"))

if __name__ == '__main__':
    report(PIECES)
    if '--java' in sys.argv:
        for p in PIECES: print(java(p))
