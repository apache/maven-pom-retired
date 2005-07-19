#!/bin/bash

set -e
set -x

(
  m2 -Denv=test clean:clean install "$@"
) 2>&1 | tee result.log
