# Tales of Dream and Fantasy 2

A cultivation simulation I made that includes stories, music, and art.

A xianxia world of 100,000 people that lives on without you: they are born, train, fight, trade, found cities and sects, go to war and die, one week per turn. You can play one of them (as yourself, a ready-made story start, or a custom character), run them by hand, or hand them over to Auto or Hybrid and watch.

## Play it

The easiest way is the Windows build: run `make-exe.bat` (see below) and open `dist\TalesOfDreamAndFantasy2\TalesOfDreamAndFantasy2.exe`. A console window opens first and asks whether you want the map window: answer `Y` and press Enter, and the game window opens. No Java needs to be installed to run the built game.

Tips that save time:
- Click through the menus before you start; you can resize every window. If something does not trigger, look at your policies and duties.
- The game is a simulation and cultivation takes years. Learn Hybrid and Auto instead of doing everything by hand.
- Type `help` in the console or press `?` on any window for the illustrated Help.

## What you need

- Windows 10 or 11, 64 bit. About 4 GB of free memory for a world of 100,000 people (the game caps itself at 3 GB). The game asks how big a world to make (10,000 to 150,000); pick a smaller one on a weaker machine.
- To build from source: a JDK. The code targets Java 8 (`--release 8`); `make-exe.bat` needs JDK 18 or newer (`jpackage`), JDK 21 is what it was made with.

## Build and run from source

From the repository root:

```
javac --release 8 -encoding UTF-8 -cp "src;lib/flatlaf-3.7.2.jar" -d out src/Game/*.java
java -Xss512m -Xmx3g --add-exports java.desktop/com.sun.media.sound=ALL-UNNAMED -cp "out;lib/flatlaf-3.7.2.jar" Game.GameDriver
```

(`;` is the Windows classpath separator; use `:` on Linux or macOS.) Run it from the repository root so the game finds `soundfonts/`, `assets/`, `images/` and `songs/`. `--add-exports` is only needed on Java 9 or newer and gives the music more voices.

To make the Windows app folder and a zip: `.\make-exe.bat` (optionally `.\make-exe.bat "C:\Path\To\JDK"`). The result is `dist\TalesOfDreamAndFantasy2\`.

## What is in this branch

`main` holds only what is needed to build and run the game: `src/` (all the Java, package `Game`), `lib/` (FlatLaf), `assets/` (Help pictures and the logo), `images/`, `songs/`, `soundfonts/` and the packaging scripts. The notes, design documents and test programs are on the `dev` branch.

## Credits

- Look and feel: [FlatLaf](https://www.formdev.com/flatlaf/) (Apache License 2.0), see `lib/README.txt`.
- Music is played through the GeneralUser GS SoundFont in `soundfonts/` (or the built-in synthesiser if no SoundFont is found).

## Feedback

Which songs you like or dislike (by name, please), which buttons or tabs could sit better, and any quality-of-life ideas are all welcome.
