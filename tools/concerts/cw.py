"""Helper for writing the Musicians' concert pieces (Concerts.Piece): tunes are written in note names over chords; this checks them and emits the Java.
   P(title, occasion, bpm, beats, tonic, scale, style, chordsA, chordsB, chordsA2, melA, melB, endA2, season=-1, master=False, chordsC=None, melC=None, flats=None)
   tonic: a note name such as "D4" (the MIDI note of degree 1). scale: 'major','pent','minor','minorpent','dorian','mixo','lydian','harm'.
   Melodies: bars separated by '|', notes 'F#5:1.5', rests 'r:1', durations in beats ('.5', '1/3'). Chords: one per bar, separated by '|'."""
import re, sys, math

SCALES = {'major': [0, 2, 4, 5, 7, 9, 11], 'pent': [0, 2, 4, 7, 9], 'minor': [0, 2, 3, 5, 7, 8, 10], 'minorpent': [0, 3, 5, 7, 10],
          'dorian': [0, 2, 3, 5, 7, 9, 10], 'mixo': [0, 2, 4, 5, 7, 9, 10], 'lydian': [0, 2, 4, 6, 7, 9, 11], 'harm': [0, 2, 3, 5, 7, 8, 11]}
JAVA_SCALE = {'major': 'MAJOR', 'pent': 'MAJOR_PENT', 'minor': 'MINOR', 'minorpent': 'MINOR_PENT', 'dorian': 'DORIAN', 'mixo': 'MIXO', 'lydian': 'LYDIAN', 'harm': 'HARM_MINOR'}
PC = {'C': 0, 'D': 2, 'E': 4, 'F': 5, 'G': 7, 'A': 9, 'B': 11}
SHARP = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']
FLAT = ['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B']
warnings = []


def midi(name):
    m = re.match(r'^([A-G])([#b]?)(-?\d)$', name)
    if not m: raise ValueError('bad note ' + name)
    return 12 * (int(m.group(3)) + 1) + PC[m.group(1)] + (1 if m.group(2) == '#' else -1 if m.group(2) == 'b' else 0)


def dur(s):
    if '/' in s:
        a, b = s.split('/'); return float(a) / float(b)
    return float(s)


def chord_mask(sym):
    m = re.match(r'^([A-G][#b]?)(m7b5|maj7|maj9|m7|7sus4|sus4|sus2|add9|madd9|dim|m|7|6)?(?:/([A-G][#b]?))?$', sym)
    if not m: raise ValueError('bad chord ' + sym)
    r = PC[m.group(1)[0]] + (1 if m.group(1).endswith('#') else -1 if m.group(1).endswith('b') else 0)
    q = m.group(2) or ''
    third, fifth, seventh, ninth = 4, 7, None, False
    if q in ('m', 'm7', 'madd9', 'dim', 'm7b5'): third = 3
    if q in ('dim', 'm7b5'): fifth = 6
    if q == 'sus4' or q == '7sus4': third = 5
    if q == 'sus2': third = 2
    if q in ('7', 'm7', 'm7b5', '7sus4'): seventh = 10
    if q in ('maj7', 'maj9'): seventh = 11
    if q == '6': seventh = 9
    if q in ('add9', 'madd9', 'maj9'): ninth = True
    mask = {r % 12, (r + third) % 12, (r + fifth) % 12}
    if seventh is not None: mask.add((r + seventh) % 12)
    if ninth: mask.add((r + 2) % 12)
    return mask, r % 12, third


class Piece:
    pass


def parse_mel(text, beats, title, label):
    bars = []
    for bi, bar in enumerate(text.split('|')):
        toks = bar.split()
        notes = []
        tot = 0.0
        for t in toks:
            h, d = t.rsplit(':', 1)
            dd = dur(d)
            tot += dd
            notes.append((h, dd))
        if abs(tot - beats) > 1e-6:
            warnings.append('%s %s bar %d lasts %.3f beats, not %s' % (title, label, bi + 1, tot, beats))
        bars.append(notes)
    return bars


def to_degrees(bars, tonic, scale, title, label):
    out = []
    for bi, notes in enumerate(bars):
        toks = []
        for h, d in notes:
            ds = ('%g' % d) if abs(d - round(d, 4)) < 1e-9 else None
            dstr = None
            for f in (1, 2, 3):
                pass
            # express durations as the author does: .5, 1, 1.5, 2 ... or a third
            if abs(d * 3 - round(d * 3)) < 1e-9 and abs(d - round(d * 2) / 2) > 1e-9:
                n = int(round(d * 3)); dstr = '1/3' if n == 1 else '2/3' if n == 2 else '%d/3' % n
            else:
                dstr = ('%g' % d).lstrip('0') if d < 1 and ('%g' % d).startswith('0') else ('%g' % d)
            if h == 'r':
                toks.append('r:' + dstr); continue
            semis = midi(h) - tonic
            octv = semis // 12
            pc = semis % 12
            if pc not in scale:
                warnings.append('%s %s bar %d: %s is not in the scale' % (title, label, bi + 1, h)); toks.append('1:' + dstr); continue
            deg = scale.index(pc) + 1
            marks = ("'" * octv) if octv > 0 else (',' * (-octv))
            toks.append('%d%s:%s' % (deg, marks, dstr))
        out.append(' '.join(toks))
    return ' | '.join(out)


def check_melody(bars, chords, tonic, beats, title, label):
    prev = None
    for bi, notes in enumerate(bars):
        ch = chords[bi] if bi < len(chords) else None
        mask = None
        if ch:
            mask, root, third = chord_mask(ch)
            ext = set(mask)
            ext.add((root + 2) % 12)          # the ninth
            if third == 4: ext.add((root + 9) % 12)  # the sixth of a major chord
        pos = 0.0
        for h, d in notes:
            if h != 'r':
                m = midi(h)
                if prev is not None and abs(m - prev) > 9:
                    warnings.append('%s %s bar %d: leap of %d to %s' % (title, label, bi + 1, abs(m - prev), h))
                prev = m
                if ch and d >= 1.0 - 1e-9 and abs(pos - round(pos)) < 1e-6 and (m % 12) not in ext:
                    warnings.append('%s %s bar %d beat %g: %s is not a tone of %s' % (title, label, bi + 1, pos + 1, h, ch))
                if m < 55 or m > 90:
                    warnings.append('%s %s bar %d: %s is out of the lead range' % (title, label, bi + 1, h))
            pos += d


def P(title, occasion, bpm, beats, tonic, scale, style, chordsA, chordsB, chordsA2, melA, melB, endA2, season=-1, master=False, chordsC=None, melC=None, flats=None):
    p = Piece()
    p.title = title; p.occasion = occasion; p.bpm = bpm; p.beats = beats; p.season = season; p.master = master; p.style = style
    p.tonicName = tonic; p.tonic = midi(tonic); p.scaleName = scale; sc = SCALES[scale]
    # flats: by the key's own spelling
    root_pc = p.tonic % 12
    p.flats = flats if flats is not None else (FLAT[root_pc] != SHARP[root_pc] and FLAT[root_pc].endswith('b') and 'b' in tonic or 'b' in tonic)
    names = FLAT if p.flats else SHARP
    p.harp = names[root_pc] + '3'; p.pad = names[(root_pc + 7) % 12] + '3'; p.bass = names[root_pc] + '2'
    cA, cB, cA2 = [c.strip() for c in chordsA.split('|')], [c.strip() for c in chordsB.split('|')], [c.strip() for c in chordsA2.split('|')]
    for lab, cs in (('A', cA), ('B', cB), ('A2', cA2)):
        if len(cs) != 8: warnings.append('%s chords %s have %d bars' % (title, lab, len(cs)))
        for c in cs: chord_mask(c)
    if cA[:6] != cA2[:6]: warnings.append('%s: the first six chords of A and A2 differ' % title)
    bA = parse_mel(melA, beats, title, 'A'); bB = parse_mel(melB, beats, title, 'B'); bE = parse_mel(endA2, beats, title, 'end')
    if len(bA) != 8 or len(bB) != 8 or len(bE) != 2: warnings.append('%s: bars A %d B %d end %d' % (title, len(bA), len(bB), len(bE)))
    check_melody(bA, cA, p.tonic, beats, title, 'A'); check_melody(bB, cB, p.tonic, beats, title, 'B'); check_melody(bE, cA2[6:], p.tonic, beats, title, 'end')
    p.dA = to_degrees(bA, p.tonic, sc, title, 'A'); p.dB = to_degrees(bB, p.tonic, sc, title, 'B'); p.dE = to_degrees(bE, p.tonic, sc, title, 'end')
    p.cA, p.cB, p.cA2 = chordsA, chordsB, chordsA2
    p.cC = chordsC; p.dC = None
    if master:
        cC = [c.strip() for c in chordsC.split('|')]
        if len(cC) != 8: warnings.append('%s chords C have %d bars' % (title, len(cC)))
        bC = parse_mel(melC, beats, title, 'C'); check_melody(bC, cC, p.tonic, beats, title, 'C')
        p.dC = to_degrees(bC, p.tonic, sc, title, 'C')
    # length
    nb = 24 + (8 if master else 0)
    p.seconds = nb * beats * 60.0 / bpm
    if p.seconds < 30 or p.seconds > 150: warnings.append('%s lasts %.0f s' % (title, p.seconds))
    return p


def java(p, tag=True):
    q = lambda s: '"' + s + '"'
    scale = 'Concerts.' + JAVA_SCALE[p.scaleName]
    tonic = p.tonic
    out = '        new Concerts.Piece(%s, %d, %s, %d, %s, %s, %s, %s, %s, %s,\n' % (q(p.title), p.bpm, ('%g' % p.beats), tonic, scale, 'true' if p.flats else 'false', q(p.style), q(p.harp), q(p.pad), q(p.bass))
    out += '            %s, %s, %s,\n' % (q(p.cA), q(p.cB), q(p.cA2))
    out += '            %s,\n            %s,\n            %s)' % (q(p.dA), q(p.dB), q(p.dE))
    if p.master: out += '.master().third(%s,\n            %s)' % (q(p.cC), q(p.dC))
    elif p.occasion == 'season': out += '.season(%d)' % p.season
    else: out += '.tag(%s)' % q(p.occasion)
    return out + ','


def report(pieces):
    for p in pieces:
        print('%-36s %-9s %3d bpm %s %s  %3.0f s' % (p.title, p.occasion, p.bpm, p.tonicName, p.scaleName, p.seconds))
    print('--- warnings: %d' % len(warnings))
    for w in warnings: print('  ' + w)
