#!/bin/bash

./gradlew -PbuildOption="buildProduction" assemble
docker build -f Dockerfile -t yti-groupmanagement .
