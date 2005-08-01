#!/bin/sh

dir=`pwd`

(
  cd /home/jvanzyl/js/sf.net/slimdog

  java -jar webtester.jar -d $dir/src/test/slimdog
)
