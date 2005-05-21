#!/bin/bash

set -e

# If the application haven't ever been run and exploded, return silently
if [ ! -d "target/plexus-test-runtime/apps/continuum/lib/" ]
then
  exit 0
fi

copy()
{
  dir=$1

  if [ ! -d $dir ]
  then
    return 0
  fi

  file=`find $dir -name \*.jar -printf %f`

  if [ "$dir/$file" -nt "target/plexus-test-runtime/apps/continuum/lib/$file" ]
  then
    echo "Updating $file"
    cp "$dir/$file" "target/plexus-test-runtime/apps/continuum/lib/$file"
  fi
}

copy ../continuum-api/target/
copy ../continuum-core/target/
copy ../continuum-model/target/
copy ../continuum-web/target/
copy ../continuum-xmlrpc/target/
