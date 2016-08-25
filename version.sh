#!/usr/bin/env bash

echo $(git rev-parse HEAD) | cut -c-8
