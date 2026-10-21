#!/usr/bin/env python3
"""Story lint: the mechanical half of the writing framework (GameInfo/Story Writing Framework.md, section 4).

Reads the story text out of the Tale*.java files (every string literal that is a scene line: "* narration", "@Speaker| words",
optionally behind ?flag| conditions), then
  - FAILS (exit 1) on the hard bans: AI-isms, stock endings, the em dash, "It is not X. It is Y.", "You do not X. You Y.", "for a long time"...
  - REPORTS the watchlist as densities per 1,000 words against the limits (small/tiny/little, very/really/quite, quiet/soft/gentle, warm/cozy,
    named emotions, sentences under 8 words, three-item lists, similes of domestic objects),
  - prints the lines at fault with file:line so they can be fixed.

Usage:   python tools/story_lint.py [--chapters] [--watch] [files...]      (default: src/Game/Tale*.java, hard bans only + a one-line summary per file)
         --watch     also print the watchlist lines over their limits
         --chapters  also print the numbers per chapter
Exit code 1 when a hard ban is found (so it can run as a hook)."""
import io, re, sys, glob, os, statistics

HARD_PHRASES = [
    'tapestry', 'testament to', 'a testament', 'beacon of', 'little did', 'a symphony of', 'symphony of', 'dance of', 'the air was thick', 'the air is thick',
    'wave of', 'sent shivers', 'shiver down', 'shivers down', "couldn't help but", 'could not help but', "didn't know they were holding", 'did not know they were holding',
    'ministrations', 'orbs', 'and in that moment', 'in that moment', 'nothing would ever be the same', 'nothing would be the same', 'delve', 'unspoken',
    'ghost of a smile', 'the ghost of a', 'heart skipped', 'heart hammered', 'heart pounded', 'chest tightened', 'breath caught', 'let out a breath', 'breath she', 'breath he',
    'eyes sparkled', 'eyes twinkled', 'a mix of', 'mix of emotions', 'whirlwind of', 'a flicker of', 'flicker of', 'a pang of', 'pang of', 'a surge of', 'surge of',
    'palpable', 'visceral', 'intricate', 'myriad', 'amidst', 'embark', 'unwavering', 'steadfast', 'resolve hardened', 'weight of the world', 'the weight of',
    'it was as if', 'something shifted', 'something stirred', 'something in his', 'something in her', 'something in you', 'sense of purpose', 'newfound',
    'bittersweet', 'in the end', 'and just like that', 'just like that', 'against all odds', 'time stood still', 'every fiber', 'a shadow of a',
    'whispered promise', 'silent promise', 'soft smile', 'small smile', 'knowing smile', 'tiny smile', 'gentle smile', 'warm smile',
    'a long breath', 'deep breath', 'steeling', 'steeled', 'resolute', 'determined look', 'shared a look', 'exchanged a look',
]
# regex bans: (label, pattern, flags)
HARD_RX = [
    ('"It is not X. It is Y" (binary reframe)', r"(it|this|that) (is|was) not [^.!?]{3,70}[.!?] (it|this|that) (is|was)", 0),
    ('"Not X, but Y"', r"\bnot (just|only|merely|simply) [^.,;!?]{2,50}, but\b", 0),
    ('"You do not X. You Y." (paired negation)', r"\byou (do|did|have|will) not [^.!?]{3,60}[.!?] you\b", 0),
    ('"for a long time / for some time / a long while"', r"\bfor (a long time|some time|a long while|a while)\b", 0),
    ('adverb before a speech verb ("said softly")', r"\b(said|says|asked|asks|replied|replies|whispered|whispers|murmured|murmurs|muttered) (softly|quietly|gently|calmly|warmly|sadly|slowly)\b", 0),
    ('em dash', '—', 0),
    ('en dash used as a dash', ' – ', 0),
    ('"a [adjective] sort of / kind of" hedging', r"\b(sort of|kind of) (sad|tired|warm|odd|strange)\b", 0),
    ('"as if the world itself"', r"as if the (world|universe|garden|sky) (itself )?(held|was|were|knew|waited)", 0),
]
WATCH_WORDS = [
    ('small / tiny / little', r"\b(small|tiny|little)\b", 2.0),
    ('very / really / quite', r"\b(very|really|quite)\b", 1.0),
    ('quiet / soft / gentle', r"\b(quiet|quietly|soft|softly|gentle|gently|hush|hushed)\b", 0.5),
    ('warm / cozy', r"\b(warm|warmth|warmly|cozy|snug)\b", 0.3),
    ('named emotions', r"\b(sad|sadness|afraid|fear|scared|happy|happiness|angry|anger|grief|joy|sorrow|lonely|loneliness|guilt)\b", 2.5),
    ('"a long time"', r"\ba long time\b", 0.3),
    ('kettle / tea / soup / lantern comfort props', r"\b(kettle|soup|lantern|lamp|blanket|porridge|bowl of)\b", 3.0),
    ('"seems to / seemed to"', r"\bseem(s|ed)? to\b", 1.5),
    ('"almost / nearly / barely"', r"\b(almost|nearly|barely|hardly)\b", 2.5),
    ('"suddenly / softly / slowly" -ly crutches', r"\b(suddenly|slowly|carefully|gently|softly|quietly)\b", 2.0),
    ('"eyes" (stock reaction shots)', r"\b(eyes widen|eyes widened|eyes narrow|eyes narrowed|eyes met|met your eyes|met his eyes|met her eyes)\b", 0.3),
]
TRI = re.compile(r"\b[\w'-]+(?: [\w'-]+){0,2}, [\w'-]+(?: [\w'-]+){0,2},? (?:and|or) [\w'-]+(?: [\w'-]+){0,2}\b", re.I)
LIT = re.compile(r'"((?:[^"\\]|\\.)*)"')


def unescape(s):
    return s.replace('\\"', '"').replace("\\'", "'").replace('\\\\', '\\').replace('\\n', ' ').replace('\\t', ' ')


def body_of(l):
    """The spoken or narrated words of a scene line, or None when the literal is not a scene line."""
    while l.startswith('?'):
        bar = l.find('|')
        if bar < 0: return None
        l = l[bar + 1:]
    if l.startswith('* '): return l[2:]
    m = re.match(r'@[^|]{1,40}\|\s*(.*)$', l)
    if m: return m.group(1)
    return None


def load(path):
    """-> list of (line_no, chapter_id, kind, text) for every scene line in a Tale file."""
    out = []
    chap = '-'
    for no, line in enumerate(io.open(path, encoding='utf-8').read().split('\n'), 1):
        m = re.search(r'\.(chapter|episode)\("([^"]+)"', line)
        if m: chap = m.group(2)
        if re.search(r'\.epilogue\(|\.interlude\(', line): chap = 'epilogue' if 'epilogue' in line else 'interlude'
        if 'Chapters.def(' in line: chap = '-'
        for lit in LIT.findall(line):
            t = unescape(lit)
            b = body_of(t)
            if b is not None and len(b.split()) >= 2:
                out.append((no, chap, 'say' if t.lstrip('?').split('|', 1)[-1].startswith('@') or re.match(r'(\?[^|]*\|)*@', t) else 'nar', b))
    return out


def sentences(text):
    parts = re.split(r'(?<=[.!?])["”\']?\s+', text.strip())
    return [p for p in parts if len(p.split()) >= 1]


def words(text): return re.findall(r"[A-Za-z']+", text)


def allowed():
    p = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'story_lint_allow.txt')
    if not os.path.isfile(p): return []
    return [l.strip() for l in io.open(p, encoding='utf-8') if l.strip() and not l.startswith('#')]


def lint(path, show_watch, show_chapters):
    items = load(path)
    allow = allowed()
    hard = []
    allw = 0
    sents = []
    counts = {lab: [] for lab, _, _ in WATCH_WORDS}
    tri = 0
    per_chap = {}
    for no, chap, kind, b in items:
        low = b.lower()
        w = len(words(b)); allw += w
        skip = any(a in b for a in allow)
        for ph in ([] if skip else HARD_PHRASES):
            if re.search(r'\b' + re.escape(ph) + r'\b', low): hard.append((no, chap, 'phrase "%s"' % ph, b))
        for lab, rx, fl in ([] if skip else HARD_RX):
            if re.search(rx, b if rx == '—' or rx == ' – ' else low, fl): hard.append((no, chap, lab, b))
        for lab, rx, lim in WATCH_WORDS:
            for mm in re.finditer(rx, low): counts[lab].append((no, chap, mm.group(0), b))
        tri += len(TRI.findall(b))
        for s in re.split(r'(?<=[.!?])\s+', b):   # (the same split as TalesProbe, so a quoted run-on is found too)
            if len(s.split()) > 40: hard.append((no, chap, 'sentence over 40 words (%d)' % len(s.split()), s))
        for s in sentences(b): sents.append(len(words(s)))
        pc = per_chap.setdefault(chap, [0, 0, []])
        pc[0] += w; pc[1] += len(TRI.findall(b)); pc[2].extend(len(words(s)) for s in sentences(b))
    name = os.path.basename(path)
    if not allw:
        print('%-22s (no scene lines)' % name); return 0
    short = 100.0 * sum(1 for x in sents if x < 8) / max(1, len(sents))
    over = [lab for lab, rx, lim in WATCH_WORDS if 1000.0 * len(counts[lab]) / allw > lim]
    print('%-22s %6d words  sentence mean %4.1f (sd %4.1f)  under-8 %3.0f%%  tri-lists %4.1f/1000  watch over: %s' % (
        name, allw, statistics.mean(sents) if sents else 0, statistics.pstdev(sents) if len(sents) > 1 else 0, short, 1000.0 * tri / allw, ', '.join(over) or 'none'))
    for no, chap, lab, b in hard:
        print('  HARD  %s:%d [%s] %s: %s' % (name, no, chap, lab, b if '--full' in sys.argv else b[:110]))
    if show_watch:
        for lab, rx, lim in WATCH_WORDS:
            d = 1000.0 * len(counts[lab]) / allw
            if d > lim:
                print('  watch %-44s %.1f per 1000 (limit %.1f)' % (lab, d, lim))
                for no, chap, w, b in counts[lab][:6]: print('        %s:%d [%s] "%s" in: %s' % (name, no, chap, w, b[:90]))
        if short > 35: print('  watch sentences under 8 words: %.0f%% (limit 35%%)' % short)
    if show_chapters:
        for chap, (w, t, ss) in per_chap.items():
            print('      %-14s %5d words  mean %4.1f  under-8 %3.0f%%  tri %d' % (chap, w, statistics.mean(ss) if ss else 0, 100.0 * sum(1 for x in ss if x < 8) / max(1, len(ss)), t))
    return len(hard)


def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    show_watch = '--watch' in sys.argv
    show_chapters = '--chapters' in sys.argv
    files = args or sorted(glob.glob(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'Game', 'Tale*.java')) + glob.glob(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'Game', 'StoryDoors*.java')))
    bad = 0
    for f in files: bad += lint(f, show_watch, show_chapters)
    print('hard bans: %d' % bad)
    sys.exit(1 if bad else 0)

main()
