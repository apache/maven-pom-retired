import continuum
import os
import shutil
import sys
import time
import traceback

def progress( message ):
    print "* " + message

def fail( message ):
    print "FAILURE: " + message
    sys.exit( -1 )

def assertEquals( message, expected, actual ):
    if ( expected == None and actual != None ):
        print "Expected None but the actual value was: '" + str( actual ) + "'."
        sys.exit( -1 )

    if ( expected != None and actual == None ):
        assert 0, "Expected '" + str( expected ) + "' but the actual value None."
        sys.exit( -1 )

    if( expected == actual ):
        return
    
    print
    print "##############################################"
    print "ASSERTION FAILURE!"
    print "##############################################"
    print "Message: " + message
    print "Expected: " + str( expected )
    print "Actual: " + str( actual )
    print "##############################################"
    print "Traceback"
    print "##############################################"
    traceback.print_stack()
    print "##############################################"
    print

    sys.exit( -1 )

def assertTrue( message, condition ):
    assertEquals( message, True, condition )

def assertFalse( message, condition ):
    assertEquals( message, False, condition )

def assertNotNull( message, condition ):
    if( condition != None ):
        return

    print message

    sys.exit( -1 )

def assertProject( projectId, name, nagEmailAddress, state, version, builderId, project ):
    assertNotNull( "project.id", projectId )
    assertEquals( "project.name", name, project.name )
    assertEquals( "project.nagEmailAddress", nagEmailAddress, project.nagEmailAddress )
    assertEquals( "project.state", state, project.state )
    assertEquals( "project.version", version, project.version )
    assertEquals( "project.builderId", builderId, project.builderId )

def assertSuccessfulNoBuildPerformed( buildId ):
    build = waitForBuild( buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    buildResult = continuum.getBuildResult( buildId )
    assertNotNull( "Build result was null.", buildResult )
    assertTrue( "The build wasn't successful", buildResult.success )
    assertFalse( "The build was executed", buildResult.buildExecuted )

def assertSuccessfulMaven1Build( buildId ):
    build = waitForBuild( buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    buildResult = continuum.getBuildResult( buildId )
    assertNotNull( "Build result was null.", buildResult )
    assertTrue( "The build wasn't successful", buildResult.success )
    assertTrue( "The build wasn't executed", buildResult.buildExecuted )
    assertTrue( "Standard output didn't contain the 'BUILD SUCCESSFUL' message.", buildResult.standardOutput.find( "BUILD SUCCESSFUL" ) != -1 )
    assertEquals( "Standard error wasn't empty.", 0, len( buildResult.standardError ) )

def assertSuccessfulMaven2Build( buildId ):
    build = waitForBuild( buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    buildResult = continuum.getBuildResult( buildId )
    assertNotNull( "Build result was null.", buildResult )
    assertTrue( "The build wasn't successful", buildResult.success )
    assertTrue( "The build wasn't executed", buildResult.buildExecuted )
    assertTrue( "Standard output didn't contain the 'BUILD SUCCESSFUL' message.", buildResult.standardOutput.find( "BUILD SUCCESSFUL" ) != -1 )
    assertEquals( "Standard error wasn't empty.", 0, len( buildResult.standardError ) )

def assertSuccessfulAntBuild( buildId ):
    build = waitForBuild( buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    buildResult = continuum.getBuildResult( buildId )
    assertNotNull( "Build result was null.", buildResult )
    assertTrue( "The build wasn't successful", buildResult.success )
    assertTrue( "The build wasn't executed", buildResult.buildExecuted )
    assertTrue( "Standard output didn't contain the 'BUILD SUCCESSFUL' message.", buildResult.standardOutput.find( "BUILD SUCCESSFUL" ) != -1 )
    assertEquals( "Standard error wasn't empty.", 0, len( buildResult.standardError ) )

def assertSuccessfulShellBuild( buildId, expectedStandardOutput ):
    build = waitForBuild( buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    buildResult = continuum.getBuildResult( buildId )
    assertNotNull( "Build result was null.", buildResult )
    assertTrue( "The build wasn't successful", buildResult.success )
    assertTrue( "The build wasn't executed", buildResult.buildExecuted )
    assertEquals( "Standard output didn't contain the expected output.", expectedStandardOutput, buildResult.standardOutput )
    assertEquals( "Standard error wasn't empty.", 0, len( buildResult.standardError ) )

def execute( workingDirectory, command ):
    cwd = os.getcwd()
    os.chdir( workingDirectory )
#    print "workingDirectory: " + workingDirectory
#    print "command: " + command
    file = os.popen( command )
    os.chdir( cwd )

    output = file.read()

    ret = file.close()

    if ( ret != None ):
        print output
        print "ret: " + str( ret )
        fail( "The command didn't return 0." )

    return output

def waitForBuild( buildId ):
    timeout = 60
    sleepInterval = 0.1

    build = continuum.getBuild( buildId )

    while( build.state == continuum.STATE_BUILD_SIGNALED or build.state == continuum.STATE_BUILDING ):
        build = continuum.getBuild( buildId )
        time.sleep( sleepInterval )
        timeout -= sleepInterval

        if ( timeout <= 0 ):
            fail( "Timeout while waiting for build (id=%(id)s) to complete" % { "id" : buildId } )

    return build

def cleanDirectory( dir ):
    if ( os.path.isdir( dir ) ):
        shutil.rmtree( dir )

def cvsCommit( basedir ):
    return execute( basedir, "cvs commit -m ''" );

def cvsCheckout( cvsroot, module, coDir ):
    return execute( basedir, "cvs -d " + cvsroot + " checkout -d " + coDir + " " + module );

def cvsImport( basedir, cvsroot, artifactId ):
    return execute( basedir, "cvs -d " + cvsroot + " import -m '' " + artifactId + " continuum_test start" )

def svnImport( basedir, svnroot, artifactId ):
    return execute( basedir, "svn import -m '' . file://" + svnroot + "/" + artifactId )

def makeScmUrl( scm, scmroot, artifactId ):
    if ( scm == "cvs" ):
        return "scm:cvs:local:%(scmroot)s:%(module)s" % { "scmroot" : scmroot , "module" : artifactId }
    elif ( scm == "svn" ):
        return "scm:svn:file:%(scmroot)s/%(artifactId)s" % { "scmroot" : scmroot , "module" : artifactId }

    raise Exception( "Unknown SCM type '" + scm + "'" )

def initMaven1Project( basedir, scm, scmroot, artifactId ):
    cleanDirectory( basedir )
    os.makedirs( basedir )
    pom = file( basedir + "/project.xml", "w+" )
    pom.write( """
<project>
  <pomVersion>3</pomVersion>
  <groupId>continuum</groupId>
  <artifactId>%(artifactId)s</artifactId>
  <currentVersion>1.0</currentVersion>
  <name>Maven 1 Project</name>
  <repository>
    <connection>%(scmUrl)s</connection>
  </repository>
  <build>
    <nagEmailAddress>%(email)s</nagEmailAddress>
  </build>
</project>
""" % { 
        "artifactId" : artifactId, 
        "scm" : scm, 
        "scmUrl" : makeScmUrl( scm, scmroot, artifactId ), 
        "email" : email
      } )
    pom.close()

    os.makedirs( basedir + "/src/main/java" )
    foo = file( basedir + "/src/main/java/Foo.java", "w+" )
    foo.write( "class Foo { }" )
    foo.close()

    cvsImport( basedir, cvsroot, artifactId )

def initMaven2Project( basedir, cvsroot, artifactId ):
    cleanDirectory( basedir )
    os.makedirs( basedir )
    pom = file( basedir + "/pom.xml", "w+" )
    pom.write( """
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>continuum</groupId>
  <artifactId>%(artifactId)s</artifactId>
  <version>2.0-SNAPSHOT</version>
  <name>Maven 2 Project</name>
  <ciManagement>
    <notifiers>
      <notifier>
        <type>mail</type>
        <address>%(email)s</address>
      </notifier>
    </notifiers>
  </ciManagement>
  <scm>
    <connection>scm:cvs:local:%(cvsroot)s:%(artifactId)s</connection>
  </scm>
</project>
""" % { "artifactId" : artifactId, "cvsroot" : cvsroot, "email" : email } )
    pom.close()

    os.makedirs( basedir + "/src/main/java" )
    foo = file( basedir + "/src/main/java/Foo.java", "w+" )
    foo.write( "class Foo { }" )
    foo.close()

    cvsImport( basedir, cvsroot, artifactId )

def initAntProject( basedir ):
    cleanDirectory( basedir )
    os.makedirs( basedir )
    buildXml = file( basedir + "/build.xml", "w+" )
    buildXml.write( """
<project>
  <target name="build">
    <property name="classes" value="target/classes"/>
    <mkdir dir="${classes}"/>
    <javac srcdir="src/main/java" destdir="${classes}"/>
  </target>
  <target name="clean">
    <delete dir="${classes}"/>
  </target>
</project>""" )
    buildXml.close()

    os.makedirs( basedir + "/src/main/java" )
    foo = file( basedir + "/src/main/java/Foo.java", "w+" )
    foo.write( "class Foo { }" )
    foo.close()

def initShellProject( basedir ):
    cleanDirectory( basedir )
    os.makedirs( basedir )
    script = file( basedir + "/script.sh", "w+" )
    script.write( """#!/bin/sh
for arg in "$@"
do
  echo $arg
  done
""" )
    script.close()
    os.system( "chmod +x " + basedir + "/script.sh" )

############################################################
# Start
############################################################

# This is the email that will be used as the nag email address
email = "trygvis@codehaus.org"

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

if 1:
    progress( "Initializing Maven 1 CVS project" )
    initMaven1Project( maven1Project, "cvs", cvsroot, "maven-1" )
    progress( "Adding Maven 1 project" )
    maven1Id = continuum.addProjectFromUrl( "file:" + maven1Project + "/project.xml", "maven-1" )
    maven1 = continuum.getProject( maven1Id )
    assertProject( maven1Id, "Maven 1 Project", email, continuum.STATE_NEW, "1.0", "maven-1", maven1 )

    progress( "Building Maven 1 project" )
    buildId = continuum.buildProject( maven1.id )
    assertSuccessfulMaven1Build( buildId )

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

    continuum.updateProjectFromScm( maven1.id )
    maven1 = continuum.getProject( maven1.id )
    assertEquals( "The project name wasn't changed.", "Maven 1 Project - Changed", maven1.name )
    assertEquals( "The project version wasn't changed.", "1.1", maven1.version )

if 1:
    progress( "Initializing Maven 2 CVS project" )
    initMaven2Project( maven2Project, cvsroot, "maven-2" )
    progress( "Adding Maven 2 project" )
    maven2Id = continuum.addProjectFromUrl( "file:" + maven2Project + "/pom.xml", "maven2" )
    maven2 = continuum.getProject( maven2Id )
    assertProject( maven2Id, "Maven 2 Project", email, continuum.STATE_NEW, "2.0-SNAPSHOT", "maven2", maven2 )

    progress( "Building Maven 2 project" )
    build = continuum.buildProject( maven2.id )
    assertSuccessfulMaven2Build( build )

    progress( "Test that a build without any files changed won't execute the builder" )
    build = continuum.buildProject( maven2.id )
    assertSuccessfulNoBuildPerformed( build )

if 1:
    progress( "Initializing Ant SVN project" )
    initAntProject( antProject )
    svnImport( antProject, svnroot, "ant-svn" )

    progress( "Adding Ant SVN project" )
    antSvnId = continuum.addProjectFromScm( "scm:svn:file://" + svnroot + "/ant-svn", "ant", "Ant SVN Project", email, "3.0", 
                                            { 
                                                "executable": "ant", 
                                                "targets" : "clean, build" 
                                            } )
    antSvn = continuum.getProject( antSvnId )
    assertProject( antSvnId, "Ant SVN Project", email, continuum.STATE_NEW, "3.0", "ant", antSvn )
    progress( "Building SVN Ant project" )
    build = continuum.buildProject( antSvn.id )
    assertSuccessfulAntBuild( build )

if 1:
    progress( "Initializing Ant CVS project" )
    initAntProject( antProject )
    cvsImport( antProject, cvsroot, "ant-cvs" )
    antCvsId = continuum.addProjectFromScm( "scm:cvs:local:" + basedir + "/cvsroot:ant-cvs", "ant", "Ant CVS Project", email, "3.0", 
                                         { "executable": "ant", "targets" : "clean, build"} )
    antCvs = continuum.getProject( antCvsId )
    assertProject( antCvsId, "Ant CVS Project", email, continuum.STATE_NEW, "3.0", "ant", antCvs )
    progress( "Building CVS Ant project" )
    build = continuum.buildProject( antCvs.id )
    assertSuccessfulAntBuild( build )

if 1:
    progress( "Initializing Shell CVS project" )
    initShellProject( shellProject )
    cvsImport( shellProject, cvsroot, "shell" )

    progress( "Adding CVS Shell project" )
    shellId = continuum.addProjectFromScm( "scm:cvs:local:" + basedir + "/cvsroot:shell", "shell", "Shell Project", email, "3.0", 
                                           { "executable": "script.sh", "arguments" : ""} )

    shell = continuum.getProject( shellId )
    assertProject( shellId, "Shell Project", email, continuum.STATE_NEW, "3.0", "shell", shell )

    progress( "Building Shell project" )
    build = continuum.buildProject( shell.id )
    assertSuccessfulShellBuild( build, "" )

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

    configuration = shell.configuration
    configuration[ "arguments" ] = "a b";
    continuum.updateProjectConfiguration( shell.id, configuration );
    shell = continuum.getProject( shell.id )
    build = continuum.buildProject( shell.id )
    assertSuccessfulShellBuild( build, """a
b
""" )

# TODO: Add project failure tests

endTime = int( time.time() )

print ""
print "##############################################"
print "ALL TESTS PASSED"
print "##############################################"
print "Time elapsed: " + str( endTime - startTime ) + "s."
print "##############################################"
print ""
