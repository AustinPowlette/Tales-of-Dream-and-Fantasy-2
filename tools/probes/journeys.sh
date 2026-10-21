#!/bin/bash
# The story journeys (test T4): every Story start for N game years, several seeds, a few at a time.
# usage: CLASSES=<compiled 3,000-person copy> JAR=<flatlaf jar> OUT=<result folder> journeys.sh <years> <parallel> <maxSeeds> [extras]
#   CLASSES  the folder the sources and probes were compiled into (see README.md)
#   ONLY     optional: the story numbers, e.g. ONLY="3 4 5" (default: all ten; with extras 1 3 5 7)
#   extras   lets the lessons, the little stories and the favors take part (-Djourney.extras=1)
# A story's seeds run one after the other and stop at the first run that completes the story or lives the whole time.
# Each run writes <OUT>/run<story>_s<seed>/journey-<id>.log and <OUT>/t4_<story>_s<seed>.out (the report). Summaries: python sum_t4.py <OUT>.
YEARS=${1:-60}; PAR=${2:-4}; SEEDS=${3:-3}; EXTRAS=$4
: "${CLASSES:?set CLASSES}"; : "${JAR:?set JAR}"; : "${OUT:?set OUT}"
mkdir -p "$OUT"
run1() {
  i=$1
  for seed in $(seq 1 $SEEDS); do
    d="$OUT/run${i}_s${seed}"; rm -rf "$d"; mkdir -p "$d"; cd "$d"
    X=""; [ -n "$EXTRAS" ] && X="-Djourney.extras=1"
    timeout 14400 java -Djava.awt.headless=true $X -Xss512m -Xmx1500m -cp "$CLASSES;$JAR" Game.StoryJourney $i $YEARS $seed > "$OUT/t4_${i}_s${seed}.out" 2>&1
    if grep -q "STORY IS COMPLETE" "$OUT/t4_${i}_s${seed}.out" || grep -q "finished at Y${YEARS}\." "$OUT/t4_${i}_s${seed}.out"; then break; fi
  done
}
export -f run1; export CLASSES JAR OUT YEARS SEEDS EXTRAS
STORIES="${ONLY:-1 2 3 4 5 6 7 8 9 10}"; [ -n "$EXTRAS" ] && STORIES="${ONLY:-1 3 5 7}"
echo $STORIES | tr ' ' '\n' | xargs -P $PAR -I{} bash -c 'run1 {}'
echo ALLDONE > "$OUT/done.flag"
