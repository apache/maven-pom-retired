#!/bin/bash

set -x

PWD=`pwd`

GNU_CLASSPATH=$HOME/opt/classpath-HEAD/share/classpath/glibj.zip
JAVA=$HOME/opt/sablevm-trunk/bin/sablevm

$JAVA \
    -classpath $PWD/target/plexus-test-runtime/core/boot/classworlds-1.1-alpha-1.jar \
    -Dclassworlds.conf=$PWD/target/plexus-test-runtime/conf/classworlds.conf \
    -Dplexus.core=$PWD/target/plexus-test-runtime/core \
    -Djava.io.tmpdir=$PWD/target/plexus-test-runtime/temp \
    -Dplexus.home=$PWD/target/plexus-test-runtime \
    org.codehaus.classworlds.Launcher $PWD/target/plexus-test-runtime/conf/plexus.xml
