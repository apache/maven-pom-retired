import continuum
import os
import shutil
import sys
import time
import traceback
from it_utils import *

print "############################################################"
print "Running integration tests"
print ""
print "NOTE:"
print "When running these integration tests you will get some"
print "stacktraces. This is normal and expected."
print "############################################################"
print ""

continuum = continuum.Continuum( "http://localhost:8000" )

progress( "Initializing SCM repositories." )

basedir = os.getcwd() + "/target"
cvsroot = basedir + "/cvsroot"
svnroot = basedir + "/svnroot"
maven1Project = basedir + "/maven-1"
maven2Project = basedir + "/maven-2"
antProject = basedir + "/ant"
shellProject = basedir + "/shell"
coDir = basedir + "/tmp-co"

cleanDirectory( basedir )
os.makedirs( basedir )
os.makedirs( cvsroot )
os.makedirs( svnroot )

execute( os.getcwd(), "cvs -d " + cvsroot + " init" )
execute( os.getcwd(), "svnadmin create " + svnroot )

startTime = int( time.time() )

#####################################################################
#
#####################################################################

if 0:
    progress( "Adding a project with a URL pointing to a nonexisting host" )

    try:
        continuum.addMavenTwoProject( "http://thishostnameshoulddefinitelyeverberesolvedorillfreakout/bar" )

        fail( "Expected XmlRpcException" )
    except continuum.XmlRpcException:
        pass

#####################################################################
#
#####################################################################

if 0:
    progress( "Adding a project with a malformed URL" )

    try:
        continuum.addMavenTwoProject( "blah://foo/bar" )

        fail( "Expected XmlRpcException" )
    except continuum.XmlRpcException:
        pass

#####################################################################
#
#####################################################################

if 1:
    progress( "Adding a project with a malformed SCM URL (invalid SCM type)" )

    writeMavenOnePom( basedir + "/project.xml", "foo", "scm:crap:", "foo@bar" )

    projectIds = continuum.addMavenOneProject( "file://" + basedir + "/project.xml" )

    project = waitForCheckOut( continuum, projectIds[ 0 ] )

#    assertEquals( "The project state should be error.", continuum.STATE_ERROR, project.state )
    assertEquals( "The error message wasn't as expected.", "No such provider: 'crap'.", project.checkOutErrorMessage )

#####################################################################
#
#####################################################################

endTime = int( time.time() )

print ""
print "##############################################"
print "ALL TESTS PASSED"
print "##############################################"
print "Time elapsed: " + str( endTime - startTime ) + "s."
print "##############################################"
print ""
