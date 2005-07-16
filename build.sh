#!/bin/bash

set -e
set -x

(
  m2 clean:clean install "$@"
) 2>&1 | tee result.log
