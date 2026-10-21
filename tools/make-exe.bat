@echo off
rem Builds a Windows app folder with an .exe launcher (icon = the game's emblem) using the JDK's jpackage.
rem Needs a JDK 14+ (jpackage); the game itself still targets Java 8 (--release 8) and the bundled runtime is a trimmed JDK.
rem Usage (from the repo root):  tools\make-exe.bat  [path-to-JDK]      e.g. tools\make-exe.bat "C:\Program Files\Java\jdk-21.0.10"
rem The heap is capped at 3 GB: the game brings its epoch (thins the world) when 70% of that is alive, so the world settles near 2 GB however long it runs (change -Xmx here for a bigger or smaller cap).
rem Result: dist\TalesOfDreamAndFantasy2\TalesOfDreamAndFantasy2.exe  (a console window opens for the first Y/n question, then the map viewer); soundfonts, assets, images and songs next to it
setlocal
set JDK=%~1
rem The output folder is "dist" unless the DIST variable names another one (set DIST=dist-new before running it when the game in dist\ is still open: Windows will not let the folder be replaced while it runs)
if "%DIST%"=="" set DIST=dist
if "%JDK%"=="" set JDK=%JAVA_HOME%
if "%JDK%"=="" set JDK=C:\Program Files\Java\jdk-21.0.10
if not exist "%JDK%\bin\jpackage.exe" (echo jpackage not found in "%JDK%"  - pass the path of a JDK 14+ as the first argument & exit /b 1)

if exist build rmdir /s /q build
mkdir build\classes build\input
"%JDK%\bin\javac" --release 8 -encoding UTF-8 -nowarn -cp "src;lib\flatlaf-3.7.2.jar" -d build\classes src\Game\*.java tools\MakeLogo.java
if errorlevel 1 (echo compile failed & exit /b 1)
"%JDK%\bin\java" -cp build\classes Game.MakeLogo assets\logo
"%JDK%\bin\jar" --create --file build\input\tdf.jar --main-class Game.GameDriver -C build\classes .
copy lib\flatlaf-3.7.2.jar build\input\ >nul
rem Files the game reads from its working folder (soundfonts, assets = help pictures and logo, images = place pictures, songs = kept songs). The launcher does NOT change the working folder
rem (a double-click starts in the folder of the .exe), so they must sit NEXT TO the .exe: jpackage --app-content does that (JDK 18+). Before 2026-10-13 they were copied into app\ where the game never looked:
rem the packaged game then played the built-in synthesiser instead of the SoundFont and had no help pictures.
set CONTENT=
if exist soundfonts set CONTENT=%CONTENT%,soundfonts
if exist assets set CONTENT=%CONTENT%,assets
if exist images set CONTENT=%CONTENT%,images
if exist songs set CONTENT=%CONTENT%,songs
rem The players' README (how to start, what is needed, where the saves go) sits in the root of the app folder
if exist tools\dist-files\README.txt set CONTENT=%CONTENT%,tools\dist-files\README.txt
if not "%CONTENT%"=="" set CONTENT=%CONTENT:~1%

if exist "%DIST%" rmdir /s /q "%DIST%"
if exist "%DIST%" (echo cannot replace "%DIST%": a game from that folder is probably still running. Close it, or set DIST=another-folder first. & exit /b 1)
"%JDK%\bin\jpackage" --type app-image --name TalesOfDreamAndFantasy2 --input build\input --main-jar tdf.jar --main-class Game.GameDriver ^
  --icon assets\logo\logo.ico --win-console --app-content "%CONTENT%" --dest "%DIST%" --java-options "-Xss512m" --java-options "-Xmx3g" --java-options "--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED" --java-options "-Dfile.encoding=UTF-8" --add-modules java.base,java.desktop,java.management,jdk.management,java.logging,java.prefs,jdk.unsupported --app-version 1.0
if errorlevel 1 (echo jpackage failed & exit /b 1)
rem A zip of the folder to hand to other people (saves and settings of this machine are never inside: the folder is built fresh)
powershell -NoProfile -Command "Compress-Archive -Path '%DIST%\TalesOfDreamAndFantasy2' -DestinationPath '%DIST%\TalesOfDreamAndFantasy2.zip' -Force"
echo.
echo Done: %DIST%\TalesOfDreamAndFantasy2\TalesOfDreamAndFantasy2.exe
echo To give it to other people: %DIST%\TalesOfDreamAndFantasy2.zip
echo Saves, settings and exports are written next to the .exe (the working folder). Copy the whole folder to move the game.
endlocal
