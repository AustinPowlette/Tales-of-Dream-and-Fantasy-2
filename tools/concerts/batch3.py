from cw import *

PIECES = []

PIECES.append(P("Lament for a River Town", "lament", 54, 4, "A4", 'minor', "arp4",
    "Am | F | C | G | Am | F | Dm | Am", "Dm | Am | F | C | Dm | Am | G | Am", "Am | F | C | G | Am | F | Em | Am",
    "E5:2 C5:1 A4:1 | C5:1.5 D5:.5 A4:2 | E5:2 G5:1 E5:1 | D5:2 B4:1 G4:1 | A4:1 C5:1 E5:2 | F5:2 A5:1 C6:1 | A5:2 F5:1 D5:1 | E5:3 r:1",
    "F5:2 A5:2 | C6:2 A5:1 E5:1 | A5:2 F5:2 | G5:2 E5:1 C5:1 | D5:1 F5:1 A5:2 | C6:1.5 B5:.5 A5:2 | B5:2 G5:1 D5:1 | A5:3 r:1",
    "G5:2 E5:2 | A4:3 r:1"))

PIECES.append(P("The Last Letter Home", "lament", 60, 3, "E4", 'harm', "arp3",
    "Em | Am | B | Em | C | Am | B | Em", "C | Am | Em | B | C | Am | B | B", "Em | Am | B | Em | C | Am | B7 | Em",
    "B4:2 G4:1 | E5:2 C5:1 | D#5:2 F#5:1 | G5:2 E5:1 | E5:1 G5:1 C6:1 | A5:2 E5:1 | F#5:1 D#5:1 B4:1 | E5:3",
    "E5:1 G5:1 C6:1 | A5:2 E5:1 | G5:1 E5:1 B4:1 | D#5:1 F#5:1 B5:1 | G5:2 E5:1 | A5:2 C6:1 | B5:1 D#6:1 B5:1 | F#5:3",
    "F#5:1 A5:1 D#5:1 | E5:3"))

PIECES.append(P("Lamps in the Quiet Street", "night", 56, 4, "F4", 'lydian', "arp4",
    "F | C | Dm | C | F | G | C | F", "Dm | G | F | C | Dm | G | C | C", "F | C | Dm | C | F | G | C | F",
    "A5:2 C6:1 A5:1 | G5:2 E5:1 G5:1 | F5:2 D5:1 F5:1 | E5:2 G5:1 C6:1 | A5:1.5 G5:.5 F5:1 A5:1 | B5:2 G5:1 D5:1 | E5:2 G5:2 | A5:3 r:1",
    "A5:1 D6:1 A5:1 F5:1 | B5:2 D6:1 B5:1 | C6:2 A5:1 F5:1 | G5:1 E5:1 C5:2 | D5:1 F5:1 A5:2 | G5:1 B5:1 D6:2 | C6:2 G5:2 | E5:3 r:1",
    "G5:2 E5:2 | F5:3 r:1"))

PIECES.append(P("Owl on the Wall", "night", 66, 3, "D4", 'dorian', "arp3",
    "Dm | Am | C | Am | Dm | G | Am | Dm", "G | Dm | C | Am | G | Dm | Am | Am", "Dm | Am | C | Am | Dm | G | C | Dm",
    "D5:1 D5:1 A5:1 | C6:1 C6:1 A5:1 | G5:1 E5:1 G5:1 | E5:2 A4:1 | F5:1 A5:1 D6:1 | B5:2 G5:1 | E5:1 A5:1 C6:1 | A5:2 D5:1",
    "D5:1 G5:1 B5:1 | A5:2 F5:1 | E5:1 G5:1 C6:1 | C6:2 A5:1 | D6:1 B5:1 G5:1 | F5:1 A5:1 D6:1 | C6:1 E6:1 A5:1 | A5:3",
    "G5:2 E5:1 | D5:3"))

PIECES.append(P("Skimming Stones on the Lake", "water", 88, 2, "A4", 'pent', "arp68",
    "A | F#m | D | E | A | F#m | D | E", "D | A | F#m | E | D | A | E | A", "A | F#m | D | E | A | F#m | E | A",
    "E5:1/3 A5:1/3 C#6:1/3 B5:1/3 A5:1/3 E5:1/3 | C#5:1/3 F#5:1/3 A5:1/3 F#5:1/3 C#5:1/3 A4:1/3 | F#5:1/3 A5:1/3 B5:1/3 A5:1/3 F#5:1/3 E5:1/3 | E5:1/3 B5:1/3 E6:1/3 B5:2/3 E5:1/3 | A5:1 C#6:1/3 B5:1/3 A5:1/3 | F#5:1 A5:1/3 C#6:1/3 A5:1/3 | F#5:1/3 A5:1/3 B5:1/3 A5:1/3 F#5:1/3 A5:1/3 | E5:1 B5:1",
    "A5:1/3 B5:1/3 A5:1/3 F#5:1/3 A5:1/3 B5:1/3 | C#6:1/3 B5:1/3 A5:1/3 E5:1/3 A5:1/3 C#6:1/3 | F#5:1/3 A5:1/3 C#6:1/3 A5:2/3 F#5:1/3 | B5:1/3 E6:1/3 B5:1/3 E5:2/3 B4:1/3 | F#5:1/3 B5:1/3 A5:1/3 F#5:1/3 A5:1/3 F#5:1/3 | A5:1/3 C#6:1/3 E6:1/3 C#6:1/3 A5:1/3 E5:1/3 | E5:1/3 B5:1/3 E6:2/3 B5:1/3 E5:1/3 | A5:1 E5:1",
    "B5:1/3 E5:1/3 B5:1/3 E6:2/3 B5:1/3 | A5:2"))

PIECES.append(P("First Blossom Morning", "season", 88, 3, "E4", 'major', "arp3",
    "E | B | C#m | A | E | B | A | B", "A | E | F#m | B | A | E | B | E", "E | B | C#m | A | E | B | B7 | E",
    "G#5:1 B5:1 E6:1 | B5:1 F#5:1 B5:1 | E5:2 C#5:1 | A5:2 E5:1 | G#5:1 B5:1 E6:1 | D#6:1 B5:1 F#5:1 | C#6:2 A5:1 | B5:3",
    "C#6:1 A5:1 E5:1 | G#5:2 B5:1 | A5:1 C#6:1 A5:1 | F#5:1 B5:1 D#6:1 | E6:1 C#6:1 A5:1 | B5:1 G#5:1 E5:1 | F#5:1 B5:1 D#6:1 | B5:3",
    "F#5:1 A5:1 D#6:1 | E6:3", season=0))

if __name__ == '__main__':
    report(PIECES)
    if '--java' in sys.argv:
        for p in PIECES: print(java(p))
