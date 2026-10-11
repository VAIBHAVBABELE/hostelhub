#!/usr/bin/env sh
set -eu
mkdir -p out
javac -encoding UTF-8 -d out src/hostelhub/*.java
java -cp out hostelhub.HostelManagementSystem
