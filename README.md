# Tales of Dream and Fantasy 2

A cultivation simulation I made that includes stories, music, and art.

A xianxia world of 100,000 people that lives on without you: they are born, train, fight, trade, found cities and sects, go to war and die, one week per turn. You can play one of them (as yourself, a ready-made story start, or a custom character), run them by hand, or hand them over to Auto or Hybrid and watch.

This is the **dev** branch: the game plus its notes and test programs. For just the game, use `main`.

## Play it

Build the Windows app with `.\make-exe.bat` and open `dist\TalesOfDreamAndFantasy2\TalesOfDreamAndFantasy2.exe`. A console window opens first and asks whether you want the map window: answer `Y` and press Enter. No Java is needed to run the built game. Type `help` in the console or press `?` on any window for the illustrated Help.

## Build and run from source

From the repository root (the code targets Java 8; `make-exe.bat` needs JDK 18 or newer):

```
javac --release 8 -encoding UTF-8 -cp "src;lib/flatlaf-3.7.2.jar" -d out src/Game/*.java
java -Xss512m -Xmx3g --add-exports java.desktop/com.sun.media.sound=ALL-UNNAMED -cp "out;lib/flatlaf-3.7.2.jar" Game.GameDriver
```

(`;` is the Windows classpath separator; use `:` elsewhere.) Entry point: `src/Game/GameDriver.java`.

## What is on this branch

| Folder | What it holds |
|---|---|
| `src/Game/` | All the game code (package `Game`). |
| `GameInfo/` | Design documents and notes: `Game Info.md` (technical deep dive), `Game Outline.md` (what is built), `DEVLOG.md` (what changed and when), the lore and story plans, balance and performance reports. |
| `CLAUDE.md` | The project instructions for the coding assistant: workflow, conventions, current state. |
| `tools/probes/` | The regression tests (headless and GUI), see `tools/probes/README.md` for how to run them from a scratch copy. |
| `tools/` | `make-exe.bat`, the song tool (`SongTool`), story lint and other helpers; `tools/concerts/` builds the musicians' concert pieces. |
| `assets/`, `images/`, `songs/`, `soundfonts/`, `lib/` | Pictures, music files and FlatLaf, as on `main`. |

## Branches

- `main`: only what builds and runs the game.
- `dev`: `main` plus the notes and tools above. Work happens here.

## Credits

- Look and feel: [FlatLaf](https://www.formdev.com/flatlaf/) (Apache License 2.0), see `lib/README.txt`.
- Music is played through the GeneralUser GS SoundFont in `soundfonts/` (or the built-in synthesiser if no SoundFont is found).

## Feedback

Which songs you like or dislike (by name, please), which buttons or tabs could sit better, and any quality-of-life ideas are all welcome.
