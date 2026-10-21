#!/bin/bash
# usage: lr_setup.sh <name> <population>   -> builds $SP/lr/<name>/{src,out,run*}
SP="${WORK:?set WORK to a scratch folder}"
R="${REPO:-$(pwd -W 2>/dev/null || pwd)}"
N="$1"; POP="$2"
D="$SP/lr/$N"
rm -rf "$D/src" "$D/out"; mkdir -p "$D/src/Game" "$D/out"
cp "$R"/src/Game/*.java "$D/src/Game/"
cp "$R"/tools/probes/*.java "$D/src/Game/"
cp "$SP"/extra/*.java "$D/src/Game/" 2>/dev/null
cp "$R"/tools/SongTool.java "$R"/tools/PlaytestProbe.java "$R"/tools/HelpShots.java "$D/src/Game/" 2>/dev/null
sed -i "s/startingPopulation = 100000;/startingPopulation = $POP;/" "$D/src/Game/Map.java"
grep -n "startingPopulation = " "$D/src/Game/Map.java" | head -2
(cd "$D" && javac -encoding UTF-8 -nowarn -cp "src;$R/lib/flatlaf-3.7.2.jar" -d out src/Game/*.java 2>&1 | grep -v "^Note" | head -30)
mkdir -p "$D/run"
# copy the assets the probes need (help images, soundfonts folder may be absent)
for f in assets soundfonts songs images; do [ -d "$R/$f" ] && [ ! -e "$D/run/$f" ] && cp -r "$R/$f" "$D/run/$f" 2>/dev/null; done
echo "built $N ($POP)"
