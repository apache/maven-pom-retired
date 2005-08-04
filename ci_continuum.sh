#!/bin/sh

# ----------------------------------------------------------------------------------

. $HOME/.profile

CMD=$1

[ "$1" = "" ] && echo && echo "You must specify a checkout or update!" && echo && exit 1

FROM=continuum@maven.zones.apache.org
TO=continuum-dev@maven.apache.org
DATE=`date`

PID=$$
RUNNING=`ps -ef | grep ci_continuum.sh | grep -v 'sh -c' | grep -v grep | grep -v $PID`
if [ ! -z "$RUNNING" ]; then
  if [ "$CMD" = "checkout" ]; then
    echo "From: $FROM" > running_log
    echo "To: $TO" >> running_log
    echo "Subject: [continuum build - SKIPPED - $CMD] $DATE" >>running_log
    echo "" >> running_log
    echo "ci_continuum.sh already running... exiting" >>running_log
    echo "$RUNNING" >>running_log
    /usr/sbin/sendmail -t < running_log
  fi
  exit 1
fi

HOME_DIR=`pwd`
DIR=$HOME_DIR/checkouts
SUNREPO=$HOME_DIR/sunrepo
REPO=$HOME_DIR/maven-repo-local
SCM_LOG=scm.log
TIMESTAMP=`date +%Y%m%d.%H%M%S`
WWW=$HOME/public_html
DEPLOY_DIR=$WWW/builds
DEPLOY_SITE=http://maven.zones.apache.org/~continuum/builds
DIST=m2-${TIMESTAMP}.tar.gz
SVN=svn

M2_HOME=/export/home/maven/m2
export M2_HOME
PATH=$PATH:$JAVA_HOME/bin:$M2_HOME/bin
export PATH

MESSAGE_DIR=$WWW/logs
MESSAGE_NAME=continuum-build-log-${TIMESTAMP}.txt
MESSAGE=${MESSAGE_DIR}/${MESSAGE_NAME}

mkdir -p $DEPLOY_DIR
mkdir -p $MESSAGE_DIR

# ----------------------------------------------------------------------------------

# Wipe out the working directory and the repository and start entirely
# from scratch.

# ----------------------------------------------------------------------------------

if [ ! -d $DIR/continuum ]; then
  CMD="checkout"
fi

(
  if [ "$CMD" = "checkout" ]
  then

    rm -rf $DIR > /dev/null 2>&1

    mkdir $DIR

    rm -rf $REPO > /dev/null 2>&1

    mkdir $REPO

    cp -R $SUNREPO/* $REPO/

    echo
    echo "Performing a clean check out of continuum ..."
    echo

    (
      cd $DIR

      $SVN co http://svn.apache.org/repos/asf/maven/continuum/trunk continuum > $HOME_DIR/$SCM_LOG 2>&1

      build_continuum=1
    )

  else

    echo
    echo "Performing an update of continuum ..."
    echo

    (
      cd $DIR/continuum

      $SVN update > $HOME_DIR/$SCM_LOG 2>&1

      grep "^[PUAD] " $HOME_DIR/$SCM_LOG > /dev/null 2>&1

      if [ "$?" = "1" ]
      then
        build_continuum=0
      else
        build_continuum=1
      fi

    )

  fi

  if [ build_continuum != 0 ]
  then
      
    echo "Updates occured, build required ..."
    echo

    (
      cd $DIR/continuum

      sh build.sh --settings $HOME_DIR/settings.xml
      ret=$?; if [ $ret != 0 ]; then exit $ret; fi
    )    
    ret=$?; if [ $ret != 0 ]; then exit $ret; fi

  else
    echo "No updates occured, no build required. Done."
  fi

) >> $MESSAGE 2>&1
ret=$?

# Only send mail to the list if a build was required.

host=`hostname`

if [ build_m2 != 0 -o build_continuum != 0 ]
then
  echo "From: $FROM" > log
  echo "To: $TO" >> log
  if [ $ret != 0 ]; then
    echo "Subject: [continuum build - FAILED - $CMD] $DATE" >> log
  else
    echo "Subject: [continuum build - SUCCESS - $CMD] $DATE" >> log
  fi
  echo "" >> log
  echo "Log:" >> log
  echo "http://maven.zones.apache.org/~continuum/logs/${MESSAGE_NAME}" >> log

  /usr/sbin/sendmail -t < log
fi
