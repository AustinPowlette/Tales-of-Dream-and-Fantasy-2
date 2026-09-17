# Development Log — Tales of Dream and Fantasy 2

## 2026-09-16
- Setup: Added `## Development Workflow` section to `CLAUDE.md` — establishes dev branch strategy, compile/smoke-test steps, and doc-update rules
- Setup: Created `DEVLOG.md`
- Fixed: `changeRelationship()` NPE (`Character.java:1366,1369`) — null-checked `currentQuest` before access and corrected boolean OR logic (stream result was being overwritten instead of combined)
- Cleaned: Removed 5 unguarded `System.out.println` debug calls from `changeRelationship()` (`Character.java`)
- Fixed: `spotQuests()` always returned 0 (`Character.java:944`) — changed `return 0` to `return questSuccess`; spot quests now correctly re-enter the priority queue on success (non-oneTime) or get removed (oneTime)
- Fixed: `event()` case 1 fall-through (`Character.java:1024`) — added `break` so beast battle no longer also triggers interact
- Fixed: `professionPicker()` fall-throughs (`Character.java:1073,1075`) — added `break` after Friendly and Tailor so each trait runs only its own profession
- Fixed: `forbiddenCheck()` zombie characters (`Character.java:2000,2011,2034,2042`) — added dead-checks in `battle()` after each forbiddenCheck call; if overextension killed the user, call `dead(opponent)` and return immediately rather than leaving the character as a zombie
- Fixed: `checkRelationship()` string comparison (`Character.java:1473,1474`) — `q.name == "Find Person"` → `"Find Person".equals(q.name)`; removeIf was never firing
- Fixed: `checkRelationship()` NPE duplicate (`Character.java:1487,1490`) — same currentQuest null-check pattern as changeRelationship(); merged into stream OR with null guard
- Cleaned: Removed 10 unguarded `System.out.println` debug calls from `checkRelationship()`
- Fixed: Technique slot duplication in `equip()` (`Character.java:1789,1792`) — added `!techniqueList.contains()` guard before adding technique2 slots; repeated equip() calls were duplicating techniques into the list
- Fixed: Stale quest target area (`Character.java:832`) — refresh `currentQuest.targetArea` from live character location before each pathfinder() call for Character-category quests
