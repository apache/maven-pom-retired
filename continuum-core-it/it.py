from continuum import Continuum, AntProject
import os
import shutil
import sys
import time
import traceback

print "############################################################"
print "Running integration tests"
print ""
print "NOTE:"
print "When running these integration tests you will get some"
print "stacktraces. This is normal and expected."
print "############################################################"
print ""

continuum = Continuum( "http://localhost:8000" )

from it_utils import *

progress( "Initializing SCM repositories." )

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

if 0:
    progress( "Initializing Maven 1 CVS project" )
    initMaven1Project( maven1Project, "cvs", cvsroot, "maven-1" )
    progress( "Adding Maven 1 project" )
    maven1Id = getProjectId( continuum.addMavenOneProject( "file:" + maven1Project + "/project.xml" ) )
    waitForSuccessfulCheckOut( continuum, maven1Id );
    maven1 = continuum.getProject( maven1Id )
    assertProject( maven1Id, "Maven 1 Project", email, continuum.STATE_NEW, "1.0", "", "maven-1", maven1 )
    assertCheckedOutFiles( maven1, [ "/project.xml", "/src/main/java/Foo.java" ] )

    progress( "Building Maven 1 project" )
    buildId = buildProject( continuum, maven1.id ).id
    assertSuccessfulMaven1Build( continuum, buildId )

    progress( "Testing that the POM is updated before each build." )
    cleanDirectory( coDir )
    cvsCheckout( cvsroot, "maven-1", coDir )
    pom = file( coDir + "/project.xml", "r" )
    value = pom.read()
    pom.close()

    value = value.replace( "Maven 1 Project", "Maven 1 Project - Changed" )
    value = value.replace( "1.0", "1.1" )

    pom = file( coDir + "/project.xml", "w+" )
    pom.write( value )
    pom.close()

    cvsCommit( coDir )

    #continuum.updateProjectFromScm( maven1.id )
    #maven1 = continuum.getProject( maven1.id )
    #assertEquals( "The project name wasn't changed.", "Maven 1 Project - Changed", maven1.name )
    #assertEquals( "The project version wasn't changed.", "1.1", maven1.version )

    removeProject( continuum, maven1.id );

if 0:
    progress( "Initializing Maven 2 CVS project" )
    initMaven2Project( maven2Project, cvsroot, "maven-2" )
    progress( "Adding Maven 2 project" )
    maven2Id = getProjectId( continuum.addMavenTwoProject( "file:" + maven2Project + "/pom.xml" ) )
    waitForSuccessfulCheckOut( continuum, maven2Id );
    maven2 = continuum.getProject( maven2Id )
    assertProject( maven2Id, "Maven 2 Project", email, continuum.STATE_NEW, "2.0-SNAPSHOT", "-N", "maven2", maven2 )

    progress( "Building Maven 2 project" )
    buildId = buildProject( continuum, maven2.id ).id
    assertSuccessfulMaven2Build( continuum, buildId )

    progress( "Test that a build without any files changed won't execute the executor" )
    expectedSize = len( continuum.getBuildsForProject( maven2.id ) )
    print( "waiting" )
    continuum.buildProject( maven2.id, False )
    time.sleep( 3.0 )
    print( "waited" )
    actualSize = len( continuum.getBuildsForProject( maven2.id ) )
    assertEquals( "A build has unexpectedly been executed.", expectedSize, actualSize )

    progress( "Test that a forced build without any files changed executes the executor" )
    buildId = buildProject( continuum, maven2.id, True ).id
    build = assertSuccessfulMaven2Build( continuum, buildId )
    assertTrue( "The 'build forced' flag wasn't true", build.forced );
    build = continuum.getBuild( buildId )

    removeProject( continuum, maven2Id )

if 1:
    progress( "Initializing Ant SVN project" )
    initAntProject( antProject )
    svnImport( antProject, svnroot, "ant-svn" )

    progress( "Adding Ant SVN project" )
    antProject = AntProject()
    antProject.scmUrl = "scm:svn:file://" + svnroot + "/ant-svn"
    antProject.name = "Ant SVN Project"
    antProject.nagEmailAddress = email
    antProject.version = "3.0"
    antProject.commandLineArguments = "-v"
    antProject.executable = "ant"
    antProject.targets = "clean build"
    antSvnId = continuum.addAntProject( antProject )
    waitForSuccessfulCheckOut( continuum, antSvnId );
    antSvn = continuum.getProject( antSvnId )
    assertProject( antSvnId, "Ant SVN Project", email, continuum.STATE_NEW, "3.0", "-v", "ant", antSvn )
    progress( "Building SVN Ant project" )
    buildId = buildProject( continuum, antSvn.id ).id
    assertSuccessfulAntBuild( continuum, buildId )

    removeProject( continuum, antSvnId )

if 0:
    progress( "Initializing Ant CVS project" )
    initAntProject( antProject )
    cvsImport( antProject, cvsroot, "ant-cvs" )
    antCvsId = continuum.addAntProject( "scm:cvs:local:" + basedir + "/cvsroot:ant-cvs", "Ant CVS Project", email, "3.0", "-d",
                                      { "executable": "ant", "targets" : "clean build"} )
    waitForSuccessfulCheckOut( continuum, antCvsId );
    antCvs = continuum.getProject( antCvsId )
    assertProject( antCvsId, "Ant CVS Project", email, continuum.STATE_NEW, "3.0", "-d", "ant", antCvs )
    progress( "Building CVS Ant project" )
    buildId = buildProject( continuum, antCvs.id ).id
    assertSuccessfulAntBuild( continuum, buildId )
    removeProject( continuum, antCvsId )

if 0:
    progress( "Initializing Shell CVS project" )
    initShellProject( shellProject )
    cvsImport( shellProject, cvsroot, "shell" )

    progress( "Adding CVS Shell project" )
    shellId = continuum.addShellProject( "scm:cvs:local:" + basedir + "/cvsroot:shell", "Shell Project", email, "3.0", "",
                                         { 
                                            "executable": "script.sh", 
                                         } )
    waitForSuccessfulCheckOut( continuum, shellId );
    shell = continuum.getProject( shellId )
    assertProject( shellId, "Shell Project", email, continuum.STATE_NEW, "3.0", "", "shell", shell )

    progress( "Building Shell project" )
    buildId = buildProject( continuum, shell.id ).id
    assertSuccessfulShellBuild( continuum, buildId, "" )

    # Test project reconfiguration
    # Test that a project will be built after a changed file is committed
    progress( "Building Shell project with alternative configuration" )

    cleanDirectory( coDir )
    cvsCheckout( cvsroot, "shell", coDir )
    script = file( coDir + "/script.sh", "r" )
    value = script.read() + "# Extra line" + os.linesep
    script.close()

    script = file( coDir + "/script.sh", "w+" )
    script.write( value )
    script.close()

    output = cvsCommit( coDir )

    continuum.updateProject( shell.id, shell.name, shell.scmUrl, shell.nagEmailAddress, shell.version, "a b" )
    shell = continuum.getProject( shell.id )
    buildId = buildProject( continuum, shell.id ).id
    assertSuccessfulShellBuild( continuum, buildId, """a
b
""" )
    removeProject( continuum, shellId )

# TODO: Add project failure tests

endTime = int( time.time() )

print ""
print "##############################################"
print "ALL TESTS PASSED"
print "##############################################"
print "Time elapsed: " + str( endTime - startTime ) + "s."
print "##############################################"
print ""
