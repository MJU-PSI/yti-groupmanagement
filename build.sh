#!/bin/bash

./gradlew -PbuildOption="buildProduction" assemble
docker build -t yti-groupmanagement .
