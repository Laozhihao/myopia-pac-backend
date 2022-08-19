#!/bin/sh -l

DOCKER_BUILDKIT=1 docker build -t 'wupol/epic-jdk:1.0' .
