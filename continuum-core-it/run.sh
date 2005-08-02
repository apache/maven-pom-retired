#!/bin/bash

set -e
set -x

(
case "`uname`" in
  CYGWIN*) 
  # required for the integration tests - would be better for them to download a distro, extract and run against it
  [ -n "$ANT_HOME" ] && ANT_HOME=`cygpath -w $ANT_HOME`
  [ -n "$MAVEN_HOME" ] && MAVEN_HOME=`cygpath -w $MAVEN_HOME`
  [ -n "$M2_HOME" ] && M2_HOME=`cygpath -w $M2_HOME`
  ;;
esac

  m2 -Denv=test clean:clean install "$@"
) 2>&1 | tee result.log
