@echo off
rem Builds a Windows app folder with an .exe launcher (icon = the game's emblem) using the JDK's jpackage.
rem Needs a JDK 14+ (jpackage); the game itself still targets Java 8 (--release 8) and the bundled runtime is a trimmed JDK.
rem Usage (from the repo root):  tools\make-exe.bat  [path-to-JDK]      e.g. tools\make-exe.bat "C:\Program Files\Java\jdk-21.0.10"
rem The heap is capped at 3 GB: the game brings its epoch (thins the world) when 70% of that is alive, so the world settles near 2 GB however long it runs (change -Xmx here for a bigger or smaller cap).
rem Result: dist\TalesOfDreamAndFantasy2\TalesOfDreamAndFantasy2.exe  (a console window opens for the first Y/n question, then the map viewer)
setlocal
set JDK=%~1
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
rem files the game reads from its working folder (the launcher starts in the app folder): soundfonts, assets (help pictures, logo), images (place pictures), songs (kept songs)
if exist soundfonts xcopy /e /i /y soundfonts build\input\soundfonts >nul
if exist assets xcopy /e /i /y assets build\input\assets >nul
if exist images xcopy /e /i /y images build\input\images >nul
if exist songs xcopy /e /i /y songs build\input\songs >nul

if exist dist rmdir /s /q dist
"%JDK%\bin\jpackage" --type app-image --name TalesOfDreamAndFantasy2 --input build\input --main-jar tdf.jar --main-class Game.GameDriver ^
  --icon assets\logo\logo.ico --win-console --dest dist --java-options "-Xss512m" --java-options "-Xmx3g" --java-options "-Dfile.encoding=UTF-8" --add-modules java.base,java.desktop,java.management,jdk.management,java.logging,java.prefs,jdk.unsupported --app-version 1.0
if errorlevel 1 (echo jpackage failed & exit /b 1)
echo.
echo Done: dist\TalesOfDreamAndFantasy2\TalesOfDreamAndFantasy2.exe
echo Saves, settings and exports are written next to the .exe (the working folder). Copy the whole folder to move the game.
endlocal
