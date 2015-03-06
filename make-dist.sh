#!/usr/bin/env bash
find . -name .DS_Store -exec rm -rf {} \;
sbt clean test universal:packageBin universal:packageZipTarball
