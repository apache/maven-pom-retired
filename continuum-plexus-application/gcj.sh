#!/bin/bash

rm -rf core
mkdir core
mkdir core/boot
find old_core -name \*.jar -maxdepth 1 -printf " %f\n" | xargs --replace=X gcj -Wall -I core -I core/boot -shared -o core/lib-X.so old_core/X
gcj -Wall -I core -I core/boot --main=org.codehaus.classworlds.Launcher -o core/boot/lib-classworlds-1.1-alpha-1.jar.so old_core/boot/classworlds-1.1-alpha-1.jar
