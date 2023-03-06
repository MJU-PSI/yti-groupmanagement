#!/bin/bash

./gradlew -PbuildOption="buildProductionTraefik" assemble
docker build -t yti-groupmanagement .
