#!/bin/bash

copy()
{
  dir=$1

  file=`find $dir -name \*.jar -printf %f`

  if [ "$dir/$file" -nt "target/plexus-test-runtime/apps/continuum/lib/$file" ]
  then
    echo "Updating $file"
    cp "$dir/$file" "target/plexus-test-runtime/apps/continuum/lib/$file"
  fi
}

copy ../continuum-core/target/
copy ../continuum-web/target/
copy ../continuum-model/target/
copy ../continuum-xmlrpc/target/
