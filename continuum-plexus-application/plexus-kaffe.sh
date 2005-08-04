#!/bin/bash

set -x

JAVA=/usr/lib/j2sdk1.4-sun/bin/java 
PWD=`pwd`

#JAVA="$JAVA -verbose"
JAVA=kaffe 
JAVA=$HOME/opt/kaffe/bin/kaffe
JAVA=$HOME/opt/Linux-i686/kaffe-cvs/bin/kaffe

#    -fullversion \
#    -Xbootclasspath/p:$HOME/opt/classpath-HEAD/share/classpath/glibj.zip \
#    -verbose \
$JAVA \
    -Xmx128m \
    -classpath $PWD/target/plexus-test-runtime/core/boot/classworlds-1.1-alpha-2.jar \
    -Dclassworlds.conf=$PWD/target/plexus-test-runtime/conf/classworlds.conf \
    -Dplexus.core=$PWD/target/plexus-test-runtime/core \
    -Djava.io.tmpdir=$PWD/target/plexus-test-runtime/temp \
    -Dtools.jar=/usr/lib/j2sdk1.4-sun/lib/tools.jar \
    -Dplexus.home=$PWD/target/plexus-test-runtime \
    org.codehaus.classworlds.Launcher $PWD/target/plexus-test-runtime/conf/plexus.xml
