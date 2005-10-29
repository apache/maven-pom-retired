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

HOME_DIR=$HOME
DIR=$HOME_DIR/checkouts
SUNREPO=$HOME_DIR/sunrepo
REPO=$HOME_DIR/maven-repo-local
SCM_LOG=$HOME_DIR/scm.log
TIMESTAMP=`date +%Y%m%d.%H%M%S`
WWW=$HOME/public_html
DEPLOY_DIR=$WWW/builds
DEPLOY_SITE=http://maven.zones.apache.org/~continuum/builds
DIST=continuum-${TIMESTAMP}.tar.gz
SVN=svn

M2_HOME=$HOME_DIR/maven-2.0
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

BUILD_REQUIRED=false
if [ -f $HOME_DIR/build_required ]; then
  BUILD_REQUIRED=`cat $HOME_DIR/build_required`
fi

if [ ! -d $DIR/continuum ]; then
  CMD="checkout"
fi

(
  if [ "$CMD" = "checkout" ]
  then

    rm -rf $DIR > /dev/null 2>&1

    mkdir $DIR

    rm -rf $REPO > /dev/null 2>&1

    cp -R $SUNREPO/* $REPO/

    echo
    echo "Performing a clean check out of continuum ..."
    echo

    (
      cd $DIR

      $SVN co http://svn.apache.org/repos/asf/maven/continuum/trunk continuum > $SCM_LOG 2>&1

      echo "true" > $HOME_DIR/build_required
    )

  else

    echo
    echo "Performing an update of continuum ..."
    echo

    (
      cd $DIR/continuum

      $SVN update > $SCM_LOG 2>&1

      grep "^[PUAD] " $SCM_LOG > /dev/null 2>&1

      if [ "$?" = "1" ]
      then

        echo $BUILD_REQUIRED > $HOME_DIR/build_required

          else
        
        echo "true" > $HOME_DIR/build_required

      fi
    )

  fi

  BUILD_REQUIRED=`cat $HOME_DIR/build_required`

  if [ "$BUILD_REQUIRED" = "true" ]
  then
      
    echo "Updates occured, build required ..."
    echo
    grep "^[PUAD] " $SCM_LOG
    echo

    (
      cd $DIR/continuum

      $M2_HOME/bin/mvn -Denv=test --batch-mode --no-plugin-registry --update-snapshots -e clean:clean install
      ret=$?; if [ $ret != 0 ]; then exit $ret; fi
    )
    ret=$?; if [ $ret != 0 ]; then exit $ret; fi

    # Only created on success

    echo
    echo "Creating continuum distribution for public consumption: ${DEPLOY_SITE}/${DIST}"
    echo

    mkdir -p $DEPLOY_DIR > /dev/null 2>&1

    (
      cd $DIR/continuum/continuum-plexus-application

      $M2_HOME/bin/mvn -Denv=production --batch-mode --no-plugin-registry --update-snapshots -e clean:clean assembly:assembly
      ret=$?; if [ $ret != 0 ]; then exit $ret; fi

      mv target/continuum*.tar.gz $DEPLOY_DIR/$DIST
    )
    ret=$?; if [ $ret != 0 ]; then exit $ret; fi

  else
    echo "No updates occured, no build required. Done."
  fi

) >> $MESSAGE 2>&1
ret=$?

grep "FATAL ERROR" $MESSAGE > /dev/null 2>&1
fatal_error=$?

# Only send mail to the list if a build was required.

host=`hostname`

BUILD_REQUIRED=`cat $HOME_DIR/build_required`

if [ "$BUILD_REQUIRED" = "true" ]
then
  echo "From: $FROM" > log
  echo "To: $TO" >> log
  if [ $ret != 0 ]; then
    echo "Subject: [continuum build - FAILED - $CMD] $DATE" >> log
  elif [ $fatal_error != 1 ]; then
    echo "Subject: [continuum build - FAILED - $CMD] $DATE" >> log
  else
    echo "Subject: [continuum build - SUCCESS - $CMD] $DATE" >> log
    echo "" >> log
    echo "Distribution:" >> log
    echo "${DEPLOY_SITE}/${DIST}" >>log
    rm $HOME_DIR/build_required
  fi
  echo "" >> log
  echo "Log:" >> log
  echo "http://maven.zones.apache.org/~continuum/logs/${MESSAGE_NAME}" >> log

  /usr/sbin/sendmail -t < log
fi
