import io, re, sys, collections, statistics

def load(p):
    return io.open(p, encoding='utf-8-sig').read().replace('\r\n', '\n')

def sentences(t):
    t = re.sub(r'\s+', ' ', t)
    parts = re.split(r'(?<=[.!?])["”]?\s+(?=["“‘A-Z(])', t)
    return [s.strip() for s in parts if len(s.split()) >= 2]

def words(t): return re.findall(r"[A-Za-z']+", t)

def quoted_share(t):
    q = re.findall(r'[“"]([^”"]+)[”"]', t)
    return sum(len(x) for x in q) / max(1, len(t))

def stats(name, t, extra=None):
    w = words(t); n = len(w)
    ss = sentences(t)
    lens = [len(words(s)) for s in ss]
    per = lambda c: round(1000.0 * c / max(1, n), 1)
    low = t.lower()
    say = collections.Counter(re.findall(r'\b(says|said|responds|replies|continues|answers|asks|resumes|proceeds|mutters|whispers|shouts|yells|exclaims)\b', low))
    ly = re.findall(r"\b[a-z]{4,}ly\b", low)
    out = []
    out.append('=== %s ===' % name)
    out.append('words %d, sentences %d, mean sentence %.1f words, median %d, longest %d, share over 30 words %.0f%%, share under 8 words %.0f%%' % (n, len(ss), statistics.mean(lens), statistics.median(lens), max(lens), 100.0 * sum(1 for x in lens if x > 30) / len(lens), 100.0 * sum(1 for x in lens if x < 8) / len(lens)))
    out.append('inside quotation marks: %.0f%% of characters' % (100 * quoted_share(t)))
    out.append('speech tags per 1000 words: %s  (total %d -> %.1f)' % (dict(say.most_common(6)), sum(say.values()), per(sum(say.values()))))
    out.append('-ly adverbs per 1000 words: %.1f' % per(len(ly)))
    out.append('similes (like a / as if / as though) per 1000: %.1f' % per(len(re.findall(r'\blike (a|an|the|someone|something)\b|\bas if\b|\bas though\b', low))))
    out.append('ellipses per 1000: %.1f   exclamations per 1000: %.1f   question marks per 1000: %.1f   em dashes per 1000: %.1f' % (per(t.count('...') + t.count('…')), per(t.count('!')), per(t.count('?')), per(t.count('—'))))
    out.append('first person (I / my / me) per 1000: %.1f   second person (you / your) per 1000: %.1f' % (per(len(re.findall(r"\b(i|i'm|i've|i'd|my|me)\b", low))), per(len(re.findall(r"\b(you|your|you're)\b", low)))))
    if extra: out.extend(extra(t, low, n, per))
    return '\n'.join(out)

def tics(t, low, n, per):
    o = []
    pats = [
        ('"It is not X. It is Y" / "Not X. Y" turns', r'\b(it|this|that|he|she|they) (is|was|are|were) not [^.!?]{3,60}[.!?] (it|this|that|he|she|they) (is|was|are|were)\b'),
        ('"for a long time / for some time / for a while"', r'\bfor (a long time|some time|a while|a long while)\b'),
        ('"a long time" anywhere', r'\ba long time\b'),
        ('quiet / quietly / quietness', r'\bquiet(ly|ness)?\b'),
        ('small / tiny', r'\b(small|tiny|little)\b'),
        ('warm / warmth', r'\bwarm(th)?\b'),
        ('"something in your/his/her ..." (vague inner feeling)', r'\bsomething (in|inside) (you|him|her|them|your|his|her)\b'),
        ('"the way X does Y" similes', r'\bthe way (you|he|she|they|a|an|one)\b'),
        ('"You do not X. You Y." paired negations', r'\byou (do|did|have) not [^.!?]{3,50}[.!?] you\b'),
        ('three-item lists "A, B, and C"', r'\b\w+(?: \w+){0,2}, \w+(?: \w+){0,2}, and \w+(?: \w+){0,2}\b'),
        ('"It is [adjective]. It is..." repeats', r'\bit is [a-z]+\. it is\b'),
        ('"definitely/probably/possibly" jokes', r'\b(definitely|probably|possibly|entirely|exactly)\b'),
        ('"very" ', r'\bvery\b'),
        ('"a little" ', r'\ba little\b'),
        ('"that is the point / whole point"', r'\b(that is|that was) (the|a) (whole )?point\b'),
    ]
    for label, rx in pats:
        c = len(re.findall(rx, low))
        o.append('  %-60s %4d  (%.1f per 1000)' % (label, c, per(c)))
    # scenes ending on a very short sentence
    return o

def main():
    base = sys.argv[1]
    for nm in ['Postulate.txt', 'Offworld Side Stories.txt']:
        print(stats(nm, load(base + '/GameInfo/' + nm), tics))
        print()
    # the game text: narrative lines only
    raw = load(base + '/GameInfo/Start Stories Script.md')
    keep = []
    for line in raw.split('\n'):
        l = line.strip()
        if not l or l.startswith('#') or l.startswith('|') or l.startswith('- **') or l.startswith('>') or l.startswith('**Opening') or l.startswith('**Closing') or l.startswith('**Question') or l.startswith('---') or l.startswith('*[only') and False:
            continue
        l = re.sub(r'^\*\[only if [^\]]*\]\* ', '', l)
        l = re.sub(r'^\d+\. ', '', l)
        l = re.sub(r'^\s*- ', '', l)
        l = re.sub(r'\*\*([^*]+):\*\* ', lambda m: '“', l)  # speaker lines become quote starts
        keep.append(l)
    game = '\n'.join(keep)
    print(stats('The ten start stories (game text, all branches)', game, tics))
    # per story
    parts = re.split(r'\n# \d+\. ', raw)
    print()
    for p in parts[1:]:
        title = p.split('  (`')[0]
        ls = []
        for line in p.split('\n'):
            l = line.strip()
            if not l or l[0] in '#|>' or l.startswith('- **') or l.startswith('**Opening') or l.startswith('**Closing') or l.startswith('**Question') or l.startswith('---'): continue
            ls.append(re.sub(r'^\*\[only if [^\]]*\]\* ', '', l))
        tt = ' '.join(ls)
        w = len(words(tt)); ss = sentences(tt)
        ll = [len(words(s)) for s in ss]
        c1 = len(re.findall(r'\b(it|this|that|he|she|they) (is|was|are|were) not [^.!?]{3,60}[.!?] (it|this|that|he|she|they) (is|was|are|were)\b', tt.lower()))
        c2 = len(re.findall(r'\b\w+(?: \w+){0,2}, \w+(?: \w+){0,2}, and \w+(?: \w+){0,2}\b', tt.lower()))
        print('%-45s words %5d  mean sentence %4.1f  "not X. it is Y" %2d  tri-lists %3d (%.1f per 1000)' % (title[:45], w, statistics.mean(ll), c1, c2, 1000.0 * c2 / max(1, w)))

main()
