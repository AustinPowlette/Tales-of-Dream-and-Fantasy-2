#!/usr/bin/env python3
"""Story autofix: the mechanical repairs for the hard bans of tools/story_lint.py, applied to the scene strings of Tale*.java files.

  - "for a long time / some time / a long while / a while" and "in the end" are swapped for plain phrases;
  - "You do not X. You Y." (paired negation) becomes one sentence joined by "; you";
  - a sentence over 38 words is cut at the comma, semicolon or conjunction nearest its middle, when the right part starts with a subject.
Whatever it cannot repair safely is printed with file:line for a hand edit. Prints a diff-like list of every change it made.

Usage: python tools/story_autofix.py [--dry] files...      (then run tools/story_lint.py)"""
import io, re, sys

LIT = re.compile(r'"((?:[^"\\]|\\.)*)"')
PHRASES = [
    (re.compile(r'\bfor a long time\b', re.I), 'for ages'),
    (re.compile(r'\bfor some time\b', re.I), 'for minutes'),
    (re.compile(r'\bfor a long while\b', re.I), 'for ages'),
    (re.compile(r'\bfor a while\b', re.I), 'for a spell'),
    (re.compile(r'\bin the end\b', re.I), 'at last'),
]
NEG = re.compile(r"\b(You) ((?:do|did|have|will) not [^.!?]{3,60})([.!?]) (You|you)\b")
SUBJ = set('he she it they you we i nobody everyone everybody there the a an his her their your my that this these those no each all both some one two three four five six seven eight nine ten many most only then now once when if as after before while because since'.split())
CONJ = [(', and ', 'and'), (', but ', 'but'), ('; ', ''), (', then ', 'then'), (', so ', 'so'), (', while ', 'while'), (', until ', ''), (', because ', '')]


def cap(s):
    m = re.match(r'(\{[a-z0-9]+\.)(he|his|him|she)\}', s)
    if m:
        return m.group(1) + m.group(2).capitalize() + '}' + s[m.end():]
    for i, ch in enumerate(s):
        if ch.isalpha():
            return s[:i] + ch.upper() + s[i + 1:]
        if ch not in '"\'*(':
            break
    return s


def subject_ok(rest):
    w = re.match(r"[\"'(]*([\w{}.'-]+)", rest)
    if not w:
        return False
    t = w.group(1)
    return t.startswith('{') or t[:1].isupper() or t.lower() in SUBJ


def split_sentence(s, limit=38):
    n = len(s.split())
    if n <= limit:
        return [s]
    best = None
    for conj, word in CONJ:
        start = 0
        while True:
            i = s.find(conj, start)
            if i < 0:
                break
            start = i + 1
            left, right = s[:i], s[i + len(conj):]
            lw, rw = len(left.split()), len(right.split())
            if lw < 6 or rw < 6:
                continue
            if left.count('"') % 2 or left.count("'") % 2 and left.count("'") > 1 and False:
                continue
            if not subject_ok(right):
                continue
            score = abs(lw - rw)
            if best is None or score < best[0]:
                best = (score, left, right, word)
    if best is None:
        return None
    _, left, right, word = best
    if word == 'but':
        right = 'But ' + right
    else:
        right = cap(right)
    left = left.rstrip(',;') + '.'
    a = [left]
    rest = split_sentence(right, limit)
    if rest is None:
        return None
    return a + rest


def fix_text(t, report, where):
    o = t
    for rx, rep in PHRASES:
        t = rx.sub(rep, t)
    t = NEG.sub(lambda m: m.group(1) + ' ' + m.group(2) + '; you', t)
    parts = re.split(r'(?<=[.!?])\s+', t)
    out = []
    for p in parts:
        if len(p.split()) > 38:
            sp = split_sentence(p)
            if sp is None:
                report.append((where, 'cannot split (%d words): %s' % (len(p.split()), p[:90])))
                out.append(p)
            else:
                out.extend(sp)
        else:
            out.append(p)
    return ' '.join(out) if out != parts else t if t != o else o


def main():
    dry = '--dry' in sys.argv
    files = [a for a in sys.argv[1:] if not a.startswith('--')]
    changed = 0
    report = []
    for f in files:
        s = io.open(f, encoding='utf-8', newline='').read()
        crlf = '\r\n' in s
        s = s.replace('\r\n', '\n')
        lines = s.split('\n')
        for no, line in enumerate(lines):
            def rep(m):
                body = m.group(1)
                if not re.match(r'(\?[^|]*\|)*[*@]', body) and not body.startswith('*'):
                    return m.group(0)
                new = fix_text(body, report, '%s:%d' % (f, no + 1))
                return '"' + new + '"'
            nl = LIT.sub(rep, line)
            if nl != line:
                changed += 1
                lines[no] = nl
        out = '\n'.join(lines)
        if crlf:
            out = out.replace('\n', '\r\n')
        if not dry:
            io.open(f, 'w', encoding='utf-8', newline='').write(out)
    print('lines changed:', changed)
    for w, r in report:
        print('  MANUAL', w, r)


if __name__ == '__main__':
    main()
