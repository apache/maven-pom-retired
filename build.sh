#!/bin/bash

set -e
set -x

(
  m2 -N install "$@"
  (cd continuum-notifiers && m2 -N install "$@" )
  m2 -r -Dmaven.reactor.includes=*/pom.xml clean:clean "$@"
  m2 -r install "$@"
) 2>&1 | tee result.log
