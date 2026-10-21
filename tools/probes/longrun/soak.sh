#!/bin/bash
# usage: soak.sh <label> <mode> <seed> <years> [minutes]
SP="${WORK:?set WORK to a scratch folder}"
SP="${WORK:?set WORK to a scratch folder}"
R="${REPO:-$(pwd -W 2>/dev/null || pwd)}"
L="$1"; MODE="$2"; SEED="$3"; YEARS="$4"; MIN="${5:-150}"
D="$SP/lr/soakrun_$L"; rm -rf "$D"; mkdir -p "$D"; cd "$D"
for f in assets soundfonts songs images; do [ -d "$R/$f" ] && cp -r "$R/$f" "$D/$f" 2>/dev/null; done
timeout $((MIN*60+600)) java -Djava.awt.headless=true -Xss512m -Xmx3g -Dtdf.verify=true -Dsoak.mode=$MODE -Dsoak.years=$YEARS -Dsoak.seed=$SEED -Dsoak.minutes=$MIN -cp "${CLASSES:-$SP/lr/soak/out};$R/lib/flatlaf-3.7.2.jar" Game.SoakProbe > "$SP/lr/soak_$L.txt" 2>&1
echo "exit $?" >> "$SP/lr/soak_$L.txt"
