#!/bin/bash
# Robustness checks: corrupt settings files and damaged saves. Uses the 3,000-person build; each case runs in its own empty folder.
SP="${WORK:?set WORK to a scratch folder}"
R="${REPO:-$(pwd -W 2>/dev/null || pwd)}"
CP="$SP/lr/small/out;$R/lib/flatlaf-3.7.2.jar"
base="$SP/lr/robust"; mkdir -p "$base"

runcase() { # name, setup-function-name, input, seconds
  local d="$base/$1"; mkdir -p "$d"; cd "$d"
  $2
  (printf "$3"; sleep $4) | timeout $(($4+25)) java -Djava.awt.headless=true -Xss512m -Xmx1500m -cp "$CP" Game.GameDriver > out.txt 2>&1
  local rc=$?
  local ex=$(grep -c -E "Exception|\sat Game\." out.txt)
  echo "### $1 rc=$rc exceptions=$ex crash.log=$([ -f crash.log ] && echo YES || echo no) lastlines: $(tail -n 2 out.txt | tr '\n' '|' | cut -c1-200)"
  [ "$ex" != "0" ] && grep -E "Exception" out.txt | sort | uniq -c | sort -rn | head -3 | cut -c1-200
}
s_empty() { : > settings.properties; }
s_garbage() { head -c 3000 /dev/urandom > settings.properties; }
s_badnum() { printf 'musicVolume=abc\nautosaveYears=-5\nstartPopulation=999999999\nleavePct=a,b,c\nscrollSpeed=0\ntickDelayMs=-1\nautosaveKeep=0\ntextScale=-3\nepochpop=-1\nmaxRandomChallenges=zzz\nsoundFont=C:/no/such/file.sf2\nexportDir=?:*<>|\nlore=maybe\n' > settings.properties; }
s_huge() { printf 'startPopulation=2147483647\nautosaveKeep=100000\nlistLimit=99999999999\nscrollSpeed=99999\nepochseconds=-9\ncrowd=0\ncrowd=-2\ntilecap=0\n' > settings.properties; }
s_none() { :; }

echo "=== corrupt settings (a new game for 14 s each)"
for c in empty garbage badnum huge; do runcase "set_$c" s_$c "n\nx\nw\n" 14; done

echo "=== damaged saves"
# make a real small save first with the seed probe, then damage it
mkdir -p "$base/seed" && cd "$base/seed" && rm -rf saves && timeout 200 java -Djava.awt.headless=true -Xss512m -cp "$CP" Game.QolSeed > seed.txt 2>&1; ls saves 2>/dev/null | head -4
mk() { rm -rf saves; cp -r "$base/seed/saves" saves; }
d_truncate() { mk; for f in saves/*.dat; do sz=$(stat -c %s "$f"); head -c $((sz/2)) "$f" > "$f.t" && mv "$f.t" "$f"; done; }
d_zero() { mk; for f in saves/*.dat; do : > "$f"; done; }
d_garbage() { mk; for f in saves/*.dat; do head -c 5000 /dev/urandom > "$f"; done; }
d_nometa() { mk; rm -f saves/*.meta; }
d_badmeta() { mk; for f in saves/*.meta; do printf 'crc=notanumber\nyear=-5\npopulation=abc\nbytes=\n' > "$f"; done; }
d_flipbyte() { mk; for f in saves/*.dat; do python -I -c "
import sys
p=sys.argv[1]; b=bytearray(open(p,'rb').read()); i=len(b)//2; b[i]^=0xFF; open(p,'wb').write(b)" "$(cygpath -m "$PWD/$f")"; done; }
d_baktoo() { mk; for f in saves/*.dat; do cp "$f" "${f%.dat}.bak"; head -c 4000 /dev/urandom > "$f"; done; }
for c in truncate zero garbage nometa badmeta flipbyte baktoo; do runcase "save_$c" d_$c "n\n1\nw\n" 25; done
echo "### ROBUST DONE"
