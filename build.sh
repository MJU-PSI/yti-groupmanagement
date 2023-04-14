#!/bin/bash

./gradlew assemble
docker build -f Dockerfile -t yti-groupmanagement .
