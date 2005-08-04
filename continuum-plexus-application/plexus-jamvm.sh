#!/bin/bash

set -x

PWD=`pwd`

GNU_CLASSPATH=$HOME/opt/classpath-HEAD/share/classpath/glibj.zip
JAVA=$HOME/opt/jamvm-1.3.2/bin/jamvm

#    -Xbootclasspath:$GNU_CLASSPATH \
#    -verbose:{class,gc,jni} \

$JAVA \
    -Xnoasyncgc \
    -classpath $PWD/target/plexus-test-runtime/core/boot/classworlds-1.1-alpha-2.jar \
    -Dclassworlds.conf=$PWD/target/plexus-test-runtime/conf/classworlds.conf \
    -Dplexus.core=$PWD/target/plexus-test-runtime/core \
    -Djava.io.tmpdir=$PWD/target/plexus-test-runtime/temp \
    -Dtools.jar=/usr/lib/j2sdk1.4-sun/lib/tools.jar \
    -Dplexus.home=$PWD/target/plexus-test-runtime \
    org.codehaus.classworlds.Launcher $PWD/target/plexus-test-runtime/conf/plexus.xml
