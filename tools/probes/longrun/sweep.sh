#!/bin/bash
# usage: sweep.sh <classes-dir> <run-dir> <headless|gui> <label> Probe1 Probe2 ...
# Runs each probe from <run-dir> (saves/settings/crash.log removed first), prints one block per probe: summary line, every FAIL line, exceptions, crash.log presence, seconds.
R="${REPO:-$(pwd -W 2>/dev/null || pwd)}"
CL="$1"; RD="$2"; MODE="$3"; LABEL="$4"; shift 4
mkdir -p "$RD"
for f in assets soundfonts songs images; do [ -d "$R/$f" ] && [ ! -e "$RD/$f" ] && cp -r "$R/$f" "$RD/$f" 2>/dev/null; done
cd "$RD"
H=""; [ "$MODE" = "headless" ] && H="-Djava.awt.headless=true"
for P in "$@"; do
  rm -rf saves settings.properties crash.log
  t0=$(date +%s)
  timeout 1500 java $H -Xss512m -Xmx2g -cp "$CL;$R/lib/flatlaf-3.7.2.jar" Game.$P > "$RD/_$P.out" 2>&1
  rc=$?
  t1=$(date +%s)
  echo "### $LABEL $P rc=$rc secs=$((t1-t0))"
  grep -E "=====|PASS: |FAIL" "$RD/_$P.out" | grep -v "^\[.\] PASS" | cut -c1-260 | head -14
  ex=$(grep -c -E "Exception|\sat Game\." "$RD/_$P.out")
  [ "$ex" != "0" ] && { echo "   exception-ish lines: $ex"; grep -E "Exception" "$RD/_$P.out" | cut -c1-200 | sort | uniq -c | sort -rn | head -4; }
  [ -f crash.log ] && { echo "   CRASH.LOG:"; head -6 crash.log | cut -c1-200; }
done
echo "### $LABEL DONE"
