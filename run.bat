@echo off
if not exist out mkdir out
javac -encoding UTF-8 -d out src\hostelhub\*.java
if errorlevel 1 pause & exit /b 1
java -cp out hostelhub.HostelManagementSystem
pause
