from continuum import Continuum, MavenTwoProject, MavenOneProject, AntProject, ShellProject, makeMailNotifier
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

c = Continuum( "http://localhost:8000" )

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

if 1:
    progress( "Initializing Maven 1 CVS project" )
    initMaven1Project( maven1Project, "cvs", cvsroot, "maven-1" )
    progress( "Adding Maven 1 project" )
    maven1Id = getProjectId( c.addMavenOneProject( "file:" + maven1Project + "/project.xml" ) )
    waitForSuccessfulCheckOut( c, maven1Id );
    maven1 = c.getProject( maven1Id )
    assertProject( maven1Id, "Maven 1 Project", email, "1.0", "", "maven-1", maven1 )
    assertCheckedOutFiles( maven1, [ "/project.xml", "/src/main/java/Foo.java" ] )

    progress( "Building Maven 1 project" )
    buildId = buildProject( c, maven1.id ).id
    assertSuccessfulMaven1Build( c, buildId )

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

    #c.updateProjectFromScm( maven1.id )
    #maven1 = c.getProject( maven1.id )
    #assertEquals( "The project name wasn't changed.", "Maven 1 Project - Changed", maven1.name )
    #assertEquals( "The project version wasn't changed.", "1.1", maven1.version )

    removeProject( c, maven1.id );

if 1:
    progress( "Initializing Maven 2 CVS project" )
    initMaven2Project( maven2Project, cvsroot, "maven-2" )
    progress( "Adding Maven 2 project" )
    maven2Id = getProjectId( c.addMavenTwoProject( "file:" + maven2Project + "/pom.xml" ) )
    waitForSuccessfulCheckOut( c, maven2Id );
    maven2 = c.getProject( maven2Id )
    assertProject( maven2Id, "Maven 2 Project", email, "2.0-SNAPSHOT", "-N -B", "maven2", maven2 )

    progress( "Building Maven 2 project" )
    buildId = buildProject( c, maven2.id ).id
    assertSuccessfulMaven2Build( c, buildId )

    progress( "Test that a build without any files changed won't execute the executor" )
    expectedSize = len( c.getBuildsForProject( maven2.id ) )
    c.buildProject( maven2.id, False )
    time.sleep( 3.0 )
    actualSize = len( c.getBuildsForProject( maven2.id ) )
    assertEquals( "A build has unexpectedly been executed.", expectedSize, actualSize )

    progress( "Test that a forced build without any files changed executes the executor" )
    buildId = buildProject( c, maven2.id, True ).id
    build = assertSuccessfulMaven2Build( c, buildId )
    assertTrue( "The 'build forced' flag wasn't true", build.forced );
    build = c.getBuild( buildId )

    removeProject( c, maven2Id )

if 1:
    progress( "Initializing Ant SVN project" )
    initAntProject( antProject )
    svnImport( antProject, svnroot, "ant-svn" )

    progress( "Adding Ant SVN project" )
    p = AntProject()
    p.scmUrl = "scm:svn:file://" + svnroot + "/ant-svn"
    p.name = "Ant SVN Project"
    p.notifiers.append( makeMailNotifier( email ) )
    p.version = "3.0"
    p.commandLineArguments = "-v"
    p.executable = "ant"
    p.targets = "clean build"
    antSvnId = getProjectId( c.addAntProject( p ) )
    waitForSuccessfulCheckOut( c, antSvnId );
    antSvn = c.getProject( antSvnId )
    assertProject( antSvnId, "Ant SVN Project", None, "3.0", "-v", "ant", antSvn )
    progress( "Building SVN Ant project" )
    buildId = buildProject( c, antSvn.id ).id
    assertSuccessfulAntBuild( c, buildId )

    removeProject( c, antSvnId )

if 1:
    progress( "Initializing Ant CVS project" )
    initAntProject( antProject )
    cvsImport( antProject, cvsroot, "ant-cvs" )

    p = AntProject()
    p.scmUrl = "scm:cvs:local:" + basedir + "/cvsroot:ant-cvs"
    p.name = "Ant CVS Project"
    p.notifiers.append( makeMailNotifier( email ) )
    p.version = "3.0"
    p.commandLineArguments = "-d"
    p.executable = "ant"
    p.targets = "clean build"
    antCvsId = getProjectId( c.addAntProject( p ) )
    waitForSuccessfulCheckOut( c, antCvsId );

    antCvs = c.getProject( antCvsId )
    assertProject( antCvsId, "Ant CVS Project", None, "3.0", "-d", "ant", antCvs )
    progress( "Building CVS Ant project" )
    buildId = buildProject( c, antCvs.id ).id
    assertSuccessfulAntBuild( c, buildId )
    removeProject( c, antCvsId )

if 1:
    progress( "Initializing Shell CVS project" )
    initShellProject( shellProject )
    cvsImport( shellProject, cvsroot, "shell" )

    progress( "Adding CVS Shell project" )
    p = ShellProject()
    p.scmUrl = "scm:cvs:local:" + basedir + "/cvsroot:shell"
    p.name = "Shell Project"
    p.notifiers.append( makeMailNotifier( email ) )
    p.version = "3.0"
    p.commandLineArguments = ""
    p.executable = "script.sh"
    shellId = getProjectId( c.addShellProject( p ) )
    waitForSuccessfulCheckOut( c, shellId );
    shell = c.getProject( shellId )
    assertProject( shellId, "Shell Project", None, "3.0", "", "shell", shell )

    progress( "Building Shell project" )
    buildId = buildProject( c, shell.id ).id
    assertSuccessfulShellBuild( c, buildId, "" )

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

    #c.updateProject( shell.id, shell.name, shell.scmUrl, shell.nagEmailAddress, shell.version, "a b" )
    shell = c.getProject( shell.id )
    shell.commandLineArguments = "a b";
    c.updateShellProject( shell )
    shell = c.getProject( shell.id )
    assertEquals( "Updated command line arguments doesn't match", "a b", shell.commandLineArguments );

    buildId = buildProject( c, shell.id ).id
    assertSuccessfulShellBuild( c, buildId, """a
b
""" )
    removeProject( c, shellId )

# TODO: Add project failure tests

endTime = int( time.time() )

print ""
print "##############################################"
print "ALL TESTS PASSED"
print "##############################################"
print "Time elapsed: " + str( endTime - startTime ) + "s."
print "##############################################"
print ""
