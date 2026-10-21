# Prompt for the long story runs and the full test sweep

Paste this to start the long runs again (the full regression sweep, test T3, the quest matrix, and test T4, the ten Story starts for 60 game years each, of `Control Audit and Test Plan.md`). The first round was done on 2026-10-18: its tables, repro steps and causes are in `GameInfo/Long Run Results.md`; this prompt asks for a comparison against it.

```
Run the full test sweep and the long runs. Read first: GameInfo/Control Audit and Test Plan.md (sections T0, T3, T4, 6.34, 6.35), GameInfo/Long Run Results.md (the last round, 2026-10-18), tools/probes/README.md and the memory files project_control_audit_2026_10_13.md and project_seat_cheats_realm_stories_2026_10_18.md.

Rules:
- Work in the scratchpad with a 3,000-person copy of the sources, built the way many3.sh does it (copy src/Game/*.java and tools/probes/*.java, set startingPopulation = 3000 in Map.java, javac --release 8). Use a separate compile folder for each group of jobs that run at the same time (a recompile under a running JVM breaks it). Don't touch my saves, settings or dist/. Delete saves/, settings.properties and crash.log when you're done.
- No sub-agents. Don't edit src/*.java without proposing the change first. You may add or change probes in tools/probes without asking.
- Send short status updates as the runs finish, not one big report at the end.
- Kill java AND the shell loops (bash / xargs) when you stop a batch: a killed java makes the loop start the next seed.

1. T0, the sweep: every headless probe in tools/probes (list in the README; the ones that need no display) and every GUI probe (needs a display; QolGui needs QolSeed first and fails 0-3 timing checks about half of the runs; PcProbe S2/S5/S11, SimProbeX and NewProbe are flaky: rerun a failure before calling it a bug). Report each as passed/failed with the count.

2. T3, the quest matrix: tools/probes/QuestMatrixProbe (82 rows: the 72 quest types of PlayerControl.QUEST_TYPES, plus the seek quests and the five dungeon kinds), each through the MANUAL path (qadd, goquest, qdo). Record whether it starts, whether a person can finish it, whether its result is shown, and its turn cost. Compare with the table in Long Run Results.md: which rows changed, and are the old failures still there? Add a row for every new quest type or new way to start one.

3. T4, the story journeys: tools/probes/StoryJourney, one run per Story start (all ten), MANUAL mode, 60 game years, up to three seeds each until the story is complete or the 60 years are lived (a bot that only meditates dies in about half of the runs). Same commands and windows a person has; no cheats, no state edits. For each start record: chapters opened and closed (year, Major level, counters), the ending if any, where it got stuck (a task that cannot be finished, a scene that never appears, a counter that does not move, a chapter open more than 12 years), silent results, every prompt or window that appeared and any decision that could not be answered, death or retirement and the cause, exceptions and crash.log. Run three of them again with -Djourney.extras=1 so the little stories take part, and say whether a lesson, a little story or a favor ever got in the way of a chapter.

What is new since the first round (check it, don't assume): a spectator must take a seat (the Spectate Tournament quest) before the bouts and bets of a tournament are shown; 27 more cheats in CheatsMore (breakthrough, pass, epoch, treasure, dungeon, tournament, jump, freecraft, reveal ...) and the Cheats window's tabs Growth / This tile / Time (ManagerGuiProbe, CheatsProbe); little stories for every realm, five or more each (BatchBProbe V6, TextDump); six more lessons (21 in all); the fight window everywhere, the Music Manager and the scroll speed.

At the end:
  1. A table: one row per start and per quest type, with PASS, FAIL or STUCK and a one-line reason.
  2. For each failure, the repro steps and the file and line of the cause.
  3. Update the plan, the DEVLOG, Long Run Results.md and the probes README.
  4. Tell me what's next, and give me all your questions at once.
```
