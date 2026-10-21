from cw import *

PIECES = []

# 1. temple masterpiece
PIECES.append(P("Hymn of the Nine Lamps", "temple", 60, 4, "D4", 'dorian', "arp4",
    "Dm | G | Dm | C | F | C | Am | Dm", "F | C | G | Dm | F | C | Am | Am", "Dm | G | Dm | C | F | C | G | Dm",
    "D5:2 F5:1 A5:1 | B5:2 A5:1 G5:1 | F5:1.5 E5:.5 D5:2 | E5:2 G5:1 C6:1 | C6:2 A5:1 F5:1 | G5:1.5 E5:.5 C5:2 | E5:1 A5:1 C6:2 | A5:2 F5:1 D5:1",
    "A5:2 C6:2 | E6:2 C6:1 G5:1 | B5:1.5 D6:.5 B5:1 G5:1 | A5:2 F5:1 D5:1 | F5:1 A5:1 C6:1 A5:1 | G5:1 C6:1 E6:2 | C6:1.5 B5:.5 A5:2 | E5:2 A5:2",
    "G5:1 B5:1 D6:2 | A5:1 F5:1 D5:2",
    master=True,
    chordsC="Dm | C | F | G | Dm | F | G | G",
    melC="D6:2 A5:1 F5:1 | G5:1 C6:1 E6:2 | A5:1 C6:1 F6:2 | D6:2 B5:1 G5:1 | F5:1 A5:1 D6:2 | C6:2 A5:1 F5:1 | D6:2 B5:1 G5:1 | G5:1 B5:1 A5:1 G5:1"))

# 2. war masterpiece
PIECES.append(P("The Pass That Held", "war", 108, 4, "E4", 'minor', "arp4",
    "Em | Em | C | D | Em | C | D | Em", "G | D | Am | Em | C | G | D | D", "Em | Em | C | D | Em | C | Bm | Em",
    "E5:1 E5:.5 E5:.5 B5:1 G5:1 | E6:2 B5:1 G5:1 | E5:1 E5:.5 E5:.5 G5:1 C6:1 | A5:2 F#5:1 D5:1 | E5:1 E5:.5 E5:.5 B5:1 G5:1 | G5:1 C6:1 E6:2 | D6:1.5 C6:.5 A5:1 F#5:1 | E5:2 B4:2",
    "D5:1 G5:1 B5:2 | A5:1 F#5:1 D5:1 F#5:1 | A5:1 C6:1 E6:2 | B5:1 G5:1 E5:2 | E5:1 G5:1 C6:2 | B5:1 D6:1 B5:1 G5:1 | A5:2 D6:1 A5:1 | F#5:2 A5:2",
    "D6:1.5 A5:.5 B5:1 F#5:1 | E5:3 r:1",
    master=True,
    chordsC="Am | Em | C | D | Am | C | D | Em",
    melC="A5:.5 A5:.5 C6:1 E6:2 | E6:1 D6:.5 B5:.5 G5:1 B5:1 | C6:1 E6:1 G5:1 C6:1 | D6:1.5 C6:.5 A5:1 F#5:1 | A5:.5 A5:.5 C6:1 E6:1 C6:1 | G5:.5 G5:.5 C6:1 E6:2 | A5:1 D6:1 F#6:1 D6:1 | E6:2 B5:1 G5:1"))

# 3. wedding masterpiece
PIECES.append(P("The Phoenix Crown Wedding", "wedding", 76, 4, "F4", 'major', "arp4",
    "F | C | Dm | Bb | F | C | Bb | F", "Bb | F | Gm | C | Bb | F | C | C", "F | C | Dm | Bb | F | C | C | F",
    "A5:2 C6:1 A5:1 | G5:2 E5:1 G5:1 | F5:1.5 A5:.5 D6:2 | D6:2 Bb5:1 F5:1 | A5:1 C6:1 F6:2 | E6:2 C6:1 G5:1 | D6:1.5 C6:.5 Bb5:1 D6:1 | C6:2 A5:1 F5:1",
    "F5:2 Bb5:1 D6:1 | C6:2 A5:1 F5:1 | G5:1 Bb5:1 D6:2 | E6:1 D6:.5 C6:.5 G5:2 | Bb5:1 D6:1 F6:2 | C6:1.5 A5:.5 F5:2 | G5:1 C6:1 E6:2 | D6:1 C6:1 G5:2",
    "G5:1 E5:1 G5:2 | A5:2 F5:2",
    master=True,
    chordsC="Dm | Gm | Bb | C | Dm | Bb | C | C",
    melC="A5:2 D6:1 F6:1 | D6:1.5 Bb5:.5 G5:2 | Bb5:1 D6:1 F6:2 | E6:2 C6:2 | D6:1.5 F6:.5 A5:2 | F5:1 Bb5:1 D6:2 | E6:1 D6:1 C6:1 G5:1 | G5:2 E5:1 G5:1"))

# 4. lament masterpiece
PIECES.append(P("Requiem for a Fallen Sky", "lament", 50, 3, "D4", 'harm', "arp3",
    "Dm | Gm | A | Dm | Bb | Gm | A | Dm", "F | Bb | Gm | A | F | Bb | A | A", "Dm | Gm | A | Dm | Bb | Gm | A7 | Dm",
    "A5:2 F5:1 | G5:2 Bb5:1 | A5:1.5 G5:.5 E5:1 | D5:2 F5:1 | F5:2 D5:1 | Bb5:1.5 A5:.5 G5:1 | A5:2 C#6:1 | D6:3",
    "A5:2 F5:1 | D6:2 Bb5:1 | G5:2 Bb5:1 | A5:1.5 G5:.5 E5:1 | F5:2 A5:1 | D6:1.5 A5:.5 Bb5:1 | C#6:2 A5:1 | E5:2 A5:1",
    "E5:2 G5:1 | D5:3",
    master=True,
    chordsC="Gm | Dm | Bb | A | Gm | Bb | A | A",
    melC="Bb5:2 D6:1 | F6:2 D6:1 | D6:2 Bb5:1 | C#6:2 E6:1 | D6:2 Bb5:1 | F5:2 Bb5:1 | A5:2 E5:1 | E5:2 A5:1"))

# 5. water masterpiece
PIECES.append(P("Song of Ten Thousand Rivers", "water", 66, 4, "D4", 'lydian', "arp4",
    "D | A | Bm | F#m | Em | D | E | A", "Em | A | D | A | Bm | F#m | Em | A", "D | A | Bm | F#m | Em | D | A | D",
    "F#5:.5 A5:.5 D6:1 A5:.5 F#5:.5 A5:1 | E5:.5 A5:.5 C#6:1 A5:.5 E5:.5 A5:1 | D5:.5 F#5:.5 B5:1 F#5:.5 D5:.5 F#5:1 | F#5:.5 A5:.5 C#6:1 A5:.5 F#5:.5 C#6:1 | E5:.5 F#5:.5 B5:1 F#5:.5 E5:.5 B5:1 | D6:1 A5:.5 F#5:.5 A5:1 D6:1 | E6:1 B5:.5 G#5:.5 B5:1 E5:1 | A5:1 C#6:1 E6:2",
    "B5:1 E6:1 B5:2 | A5:1 C#6:1 E6:1 C#6:1 | D6:2 A5:1 F#5:1 | E5:1 A5:1 C#6:2 | B5:1 D6:1 F#6:2 | C#6:2 A5:1 F#5:1 | B4:1 E5:1 B5:2 | A5:2 E5:2",
    "C#6:1 A5:1 E5:2 | F#5:2 D5:2",
    master=True,
    chordsC="Bm | F#m | Em | A | D | E | F#m | A",
    melC="B5:1 D6:1 F#6:2 | C#6:2 A5:1 F#5:1 | E5:1 B5:1 E6:2 | C#6:1 E6:1 A5:2 | F#5:1 A5:1 D6:2 | B5:1 G#5:1 E5:2 | F#5:1 A5:1 C#6:2 | E6:2 C#6:1 A5:1"))

# 6. night masterpiece
PIECES.append(P("Moon Over the Quiet Garden", "night", 54, 3, "G4", 'lydian', "arp3",
    "G | D | Em | Bm | G | D | A | G", "Em | Bm | G | D | Em | Bm | A | A", "G | D | Em | Bm | G | D | D | G",
    "B5:2 D6:1 | A5:2 F#5:1 | G5:1.5 B5:.5 E6:1 | D6:2 B5:1 | D6:1 B5:1 G5:1 | A5:1.5 F#5:.5 D5:1 | E5:1 A5:1 C#6:1 | B5:3",
    "E5:1 G5:1 B5:1 | D6:2 F#6:1 | D6:1.5 B5:.5 G5:1 | A5:2 F#5:1 | G5:1 B5:1 E6:1 | D6:2 B5:1 | C#6:1.5 B5:.5 A5:1 | E5:2 A5:1",
    "F#5:1 A5:1 D6:1 | D6:1 B5:1 G5:1",
    master=True,
    chordsC="G | A | Bm | F#m | Em | D | A | A",
    melC="G5:1 B5:1 D6:1 | E6:2 C#6:1 | B5:1 D6:1 F#6:1 | C#6:2 A5:1 | B5:1 G5:1 E5:1 | F#5:1 A5:1 D6:1 | C#6:2 A5:1 | E5:2 A5:1"))

if __name__ == '__main__':
    report(PIECES)
    if '--java' in sys.argv:
        for p in PIECES: print(java(p))
