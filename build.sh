#!/bin/bash

set -e
set -x

(
case "`uname`" in
  CYGWIN*) 
  # required for the integration tests - would be better for them to download a distro, extract and run against it
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath -ws $JAVA_HOME | sed 's#\\\\$##'`
  [ -n "$ANT_HOME" ] && ANT_HOME=`cygpath -w $ANT_HOME`
  [ -n "$MAVEN_HOME" ] && MAVEN_HOME=`cygpath -ws $MAVEN_HOME | sed 's#\\\\$##'`
  [ -n "$M2_HOME" ] && M2_HOME=`cygpath -w $M2_HOME`
  ;;
esac

  mvn -Denv=test clean:clean install "$@"
  ret=$?; if [ $ret != 0 ]; then exit $ret; fi
) 2>&1 | tee result.log
ret=$?; if [ $ret != 0 ]; then exit $ret; fi