#!/bin/bash

set -e
set -x

includes=\
continuum-api/pom.xml,\
continuum-cc/pom.xml,\
continuum-core/pom.xml,\
continuum-model/pom.xml,\
continuum-web/pom.xml,\
continuum-xmlrpc/pom.xml

m2 -N install "$@"
m2 -r -Dmaven.reactor.includes=*/pom.xml clean:clean "$@"
m2 -r -Dmaven.reactor.includes="$includes" install "$@"

( cd continuum-plexus-application && sh build.sh "$@" )
