#! /bin/sh
#
# chkconfig_install.sh - install Continuum on a chkconfig-bases system
# 
# Author: Felipe Leme <felipeal@apache.org>
#

# figure out what's Continuum's directory
CONTINUUM_HOME=`dirname $0`
cd ${CONTINUUM_HOME}
CONTINUUM_HOME=`pwd`

INITD_SCRIPT=/etc/rc.d/init.d/continuum

if [ -f ${INITD_SCRIPT} ]
then
  echo "File ${INITD_SCRIPT} already exists. Please remove it and try again."
  exit 1
fi

echo "Creating file ${INITD_SCRIPT}"  
cat >> ${INITD_SCRIPT} <<EOF
#! /bin/sh
# chkconfig: 345 90 10
# description: Maven Continuum server

# uncoment to set JAVA_HOME as the value present when Continuum installed
#export JAVA_HOME=${JAVA_HOME}

if [ -z "\${JAVA_HOME}" ]
then
  echo "Cannot manage Continuum without variable JAVA_HOME set"
  echo "  (try to set it on file ${INITD_SCRIPT})"
  exit 1
fi
# run Continuum as root
cd ${CONTINUUM_HOME}
./run.sh \$*
# run Continuum as user _continuum_user_
#su - _continuum_user_ -c "cd ${CONTINUUM_HOME}; ./run.sh \$*"
EOF
chmod +x ${INITD_SCRIPT}

echo "Adding Continuum to chkconfig"  
chkconfig --add continuum

echo "Enabling Continuum on chkconfig"  
chkconfig continuum on
echo "Continuum set to start on run levels 3, 4 and 5."
echo "To start continuum now, run 'service continuum start'"

