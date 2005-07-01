#!/usr/bin/env bash

set -e

m2_repo="http://svn.apache.org/repos/asf/maven/components/trunk"
continuum_repo="http://svn.apache.org/repos/asf/maven/continuum/trunk"

clean=0
force_build=0
self_update=0

usage()
{
  echo "Usage: $0 [--clean] [--force-build] [--self-update]"
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

if [ -r ./settings ]
then
  source ./settings
fi

if [ -z "$NIGHTLY_ROOT" ]
then
  echo "NIGHTLY_ROOT must be set"
  exit 1
fi

root="$NIGHTLY_ROOT"
M2_HOME="$NIGHTLY_ROOT/m2-nightly"

##############################################################################
# Self update
##############################################################################

if [ $self_update -eq "1" ]
then
  trunk="https://svn.apache.org/repos/asf/maven/continuum/trunk"
  script="build-world.sh"
  echo "Saving $script to $script.bak"
  cp $script $script.bak
  echo "Downloading $trunk/$script to $script.tmp from Subversion"
  svn cat $trunk/$script > $script.tmp
  mv $script.tmp $script
  chmod +x $script
  exit 0
fi

##############################################################################
# Clean up
##############################################################################

if [ $clean -eq "1" ]
then
  rm -rf $root/maven
  rm -rf $root/continuum
  rm -rf $root/repository
  rm -rf $M2_HOME
fi

mkdir -p $root
mkdir -p $root/repository

##############################################################################
# Do some checks of the enviroment
##############################################################################

if [ -z "$JAVA_HOME" ]
then
  echo "JAVA_HOME must be set."
fi

if [ -z "`which java`" ]
then
  echo "Could not find 'java' in the path."
  echo "PATH: $PATH"
  exit 1
fi

if [ ! -d sun-repo ]
then
  echo "WARN: Missing ./sun-repo
If the build fails with missing Sun related dependencies please make this
directory and put any relevant jars there. The repository will be copied over
to the real Maven 2 repository before each build to make sure the Maven 2
repository can be cleaned before a build and still not miss any dependencies."
else
  cp -r sun-repo/* $root/repository
fi

##############################################################################
# Check out the sources
##############################################################################

# Maven 2
first_build=0
if [ ! -d maven ]
then
  first_build=1
fi

cd $root
svn co $m2_repo maven > m2_update

tmp=`grep -v revision m2_update | wc -l`
if [ "$tmp" -eq 0 -a $first_build -eq 0 ]
then
 build_m2=0
else
 build_m2=1
fi

# Continuum
first_build=0
if [ ! -d continuum ]
then
  first_build=1
fi

svn co $continuum_repo continuum > continuum_update

tmp=`grep -v revision continuum_update | wc -l`
if [ "$tmp" -eq 0 -a $first_build -eq 0 ]
then
 build_continuum=0
else
 build_continuum=1
fi

##############################################################################
# Build
##############################################################################

PATH=$M2_HOME/bin:$PATH

echo PATH: $PATH
echo M2_HOME: $M2_HOME

if [ $build_m2 -eq 1 -o $clean -eq 1 -o $force_build -eq 1 ]
then
  cd maven
  bash -x m2-bootstrap-all.sh
  cd ..
fi

if [ ! -x m2 ]
then
  echo "WARN: Could not find m2 in PATH. For the build scripts for Continuum to
work m2 has to be in the PATH. 

If this is the first time you are running this script please this message 
can be ignored.

PATH: $PATH
M2_HOME: $M2_HOME"
  exit 1
fi

if [ $build_continuum -eq 1 -o $clean -eq 1 -o $force_build -eq 1 ]
then
  cd continuum
  bash -x build.sh -X
  cd ..
fi
