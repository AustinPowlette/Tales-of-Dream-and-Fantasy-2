@echo off
rem The real script is tools\make-exe.bat (this file used to be an out-of-date copy of it). It must run from the repo root, so go there first. Start it as .\make-exe.bat (a bare make-exe.bat is not found when Windows is set not to search the current folder).
cd /d "%~dp0"
call tools\make-exe.bat %*
