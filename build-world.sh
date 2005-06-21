#!/bin/bash

set -x
set -e

m2_repo="http://svn.apache.org/repos/asf/maven/components/trunk maven"
continuum_repo="https://svn.apache.org/repos/asf/maven/continuum/trunk continuum"

clean=0
force_build=0
self_update=0

function usage
{
  echo "Usage: $0 [--clean] [--force-build]"
  exit 1
}

while [ "$1" ];
do
  case $1 in 
    --clean) clean=1 ;;
    --force-build) force_build=1 ;;
    --self-update) self_update=1 ;;
    *) usage ;;
  esac
  shift
done

##############################################################################
# Self update
##############################################################################

if [ $self_update == "1" ]
then
  trunk="https://svn.apache.org/repos/asf/maven/continuum/trunk"
  script="build-world.sh"
  echo "Saving $script to $script.bak"
  cp $script $script.bak
  echo "Downloading $trunk/$script to $script.tmp from Subversion"
  svn cat $trunk/$script > $script.tmp
  mv $script.tmp $script
  exit 0
fi

##############################################################################
# Clean up
##############################################################################

if [ $clean == "1" ]
then
  rm -rf maven
  rm -rf continuum
  rm -rf $HOME/repository
  rm -rf $HOME/m2
  mkdir -p $HOME/repository
fi

cp -r sun-repo/* $HOME/repository

##############################################################################
# Check out the sources
##############################################################################

svn co $m2_repo > m2_update

tmp=`grep -v revision m2_update | wc -l`
echo "Updated"
if [ "$tmp" -eq 0 ]
then
 m2_updated=0
else
 m2_updated=1
fi

svn co $continuum_repo > continuum_update
tmp=`grep -v revision continuum_update | wc -l`
if [ "$tmp" -eq 0 ]
then
 continuum_updated=0
else
 continuum_updated=1
fi

##############################################################################
# Build
##############################################################################

PATH=$HOME/m2/bin:$PATH
M2_HOME=$HOME/m2
unset M2_HOME

echo M2_HOME: $M2_HOME

if [ $m2_updated -eq 1 -o $clean -eq 1 -o $force_build -eq 1 ]
then
  cd maven
  M2_HOME=$HOME/m2 bash -x m2-bootstrap-all.sh
  cd ..
fi

if [ $continuum_updated -eq 1 -o $clean -eq 1 -o $force_build -eq 1 ]
then
  cd continuum
  bash -x build.sh -X
  cd ..
fi
