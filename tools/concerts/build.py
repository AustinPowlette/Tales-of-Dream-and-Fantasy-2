import io, sys, importlib, os
import cw
ALL = []
for name in ['batch1', 'batch2', 'batch3', 'batch4', 'batch5']:
    if os.path.exists(name + '.py'):
        m = importlib.import_module(name)
        ALL.extend(m.PIECES)
cw.report(ALL)
head = '''package Game;

/** The concert pieces added to the first twelve of `Concerts`: twenty-five for the occasions of the world (market, road, water, season, tavern, lament, festival, night, study, wedding, temple, war) and six masterpieces
 *  (played by a musician of grade 8 or more, or with a Heaven-Sent instrument, one concert in five). Written as tunes in note names over chords (the scale-degree text below is made from them by a small script),
 *  checked for the length of every bar, a chord tone on every long strong beat and no leap over a sixth; the accompaniment is made by `Concerts`. */
final class ConcertsMore {
    private ConcertsMore() { }

    static final Concerts.Piece[] MORE = {
'''
body = '\n'.join(cw.java(p) for p in ALL)
tail = '''
    };
}
'''
out = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'src', 'Game', 'ConcertsMore.java')
io.open(out, 'w', encoding='utf-8', newline='').write(head + body + tail)
print('wrote', len(ALL), 'pieces')
