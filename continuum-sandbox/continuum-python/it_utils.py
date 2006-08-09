#import continuum
import os
import shutil
import sys
import time
import traceback

#####################################################################
# Configuration
#####################################################################

email = "trygvis@codehaus.org"
basedir = os.getcwd() + "/target"

#####################################################################
#
#####################################################################

def progress( message ):
    print "[" + time.strftime( "%c" ) + "] * " + message

def fail( message ):
    print "FAILURE: " + message
    sys.exit( -1 )

def assertEquals( message, expected, actual ):
    if ( expected == None and actual != None ):
        assertionFailed( message, "None", actual )

    if ( expected != None and actual == None ):
        assertionFailed( message, expected, "None" )

    if( expected == actual ):
        return

    assertionFailed( message, expected, actual )

def assertionFailed( message, expected, actual ):
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
    if ( condition != None ):
        return

    assertionFailed( message, "Not None", condition )

def assertProject( projectId, name, nagEmailAddress, version, commandLineArguments, executorId, project ):
    assertNotNull( "project.id", projectId )
    assertEquals( "project.name", name, project.name )
#    assertEquals( "project.state", state, project.state )
    assertEquals( "project.version", version, project.version )
    assertEquals( "project.commandLineArguments", commandLineArguments, project.commandLineArguments )
    assertEquals( "project.executorId", executorId, project.executorId )

    if ( nagEmailAddress == None ):
        assertEquals( "project.notifiers.size", 0, len( project.notifiers ) )
    else:
        assertEquals( "project.notifiers.size", 1, len( project.notifiers ) )
        assertEquals( "project.notifiers[0].type", "mail", project.notifiers[0].type )
        print str( project.notifiers[0].configuration )
        assertTrue( "project.notifiers[0].configuration.has_key('address')", project.notifiers[0].configuration.has_key('address') )
        assertEquals( "project.notifiers[0].configuration['address']", nagEmailAddress, project.notifiers[0].configuration['address'] )

def assertCheckedOutFiles( project, expectedCheckedOutFiles ):
    actualCheckedOutFiles = project.checkOutScmResult.checkedOutFiles
    if ( len( expectedCheckedOutFiles ) != len( actualCheckedOutFiles ) ):
        print "Expected files: "
        for expectedFile in expectedCheckedOutFiles:
            print " " + expectedFile

        print "Actual files: "
        for actualFile in actualCheckedOutFiles:
            print " " + actualFile.path

    assertEquals( "The expected and actual lists of checked out files doesn't have the same length.", 
                  len( expectedCheckedOutFiles ),
                  len( actualCheckedOutFiles ) )

    i = 0
    for expectedFile in expectedCheckedOutFiles:
        actualFile = actualCheckedOutFiles[ i ]
        i += 1

        assertEquals( "File #" + str( i ) + " doesn't match the expected path.", expectedFile, actualFile.path )

def assertSuccessfulNoBuildPerformed( continuum, buildId ):
    build = waitForBuild( continuum, buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    assertTrue( "The build wasn't successful", build.success )

def assertSuccessfulMaven1Build( continuum, buildId ):
    build = waitForBuild( continuum, buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    assertTrue( "The build wasn't successful", build.success )
    assertTrue( "Standard output didn't contain the 'BUILD SUCCESSFUL' message.", build.standardOutput.find( "BUILD SUCCESSFUL" ) != -1 )
    assertEquals( "Standard error wasn't empty.", 0, len( build.standardError ) )

def assertSuccessfulMaven2Build( continuum, buildId ):
    build = waitForBuild( continuum, buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    assertTrue( "The build wasn't successful", build.success )
    assertTrue( "Standard output didn't contain the 'BUILD SUCCESSFUL' message.", build.standardOutput.find( "BUILD SUCCESSFUL" ) != -1 )
    assertEquals( "Standard error wasn't empty.", 0, len( build.standardError ) )

    return build

def assertSuccessfulAntBuild( continuum, buildId ):
    build = waitForBuild( continuum, buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    assertTrue( "The build wasn't successful", build.success )
    assertTrue( "Standard output didn't contain the 'BUILD SUCCESSFUL' message.", build.standardOutput.find( "BUILD SUCCESSFUL" ) != -1 )
    assertEquals( "Standard error wasn't empty.", 0, len( build.standardError ) )

def assertSuccessfulShellBuild( continuum, buildId, expectedStandardOutput ):
    build = waitForBuild( continuum, buildId )
    assertEquals( "The build wasn't successful.", continuum.STATE_OK, build.state )
    assertTrue( "The build wasn't successful", build.success )
    assertEquals( "Standard output didn't contain the expected output.", expectedStandardOutput, build.standardOutput )
    assertEquals( "Standard error wasn't empty.", 0, len( build.standardError ) )

def buildProject( continuum, projectId, force=False ):
    count = 600;

    originalSize = len( continuum.getBuildsForProject( projectId ) )

    continuum.buildProject( projectId, force )

    while( True ):
        builds = continuum.getBuildsForProject( projectId )

        size = len( builds )

        count = count - 1
        if ( count == 0 ):
            fail( "Timeout while waiting for build result." )

        if ( size == originalSize ):
            time.sleep( 0.1 )
            continue

        return builds[ 0 ]

    # This will never happen, it is just to make pychecker shut up
    return None

def removeProject( continuum, projectId ):
    time.sleep( 0.5 )
    continuum.removeProject( projectId )

    result = continuum.server.continuum.getProject( projectId )

    if ( result[ "result" ] != "failure" ):
        print result
        fail( "Expected a failure when removing project." )

def execute( workingDirectory, command ):
    cwd = os.getcwd()
    os.chdir( workingDirectory )
    f = os.popen( command )
    os.chdir( cwd )

    output = f.read()

    ret = f.close()

    if ( ret != None ):
        print output
        print "ret: " + str( ret )
        fail( "The command didn't return 0." )

    return output

def waitForBuild( continuum, buildId ):
    timeout = 120                # seconds
    sleepInterval = 0.1

    build = continuum.getBuild( buildId )

    while( build.state == continuum.STATE_UPDATING or
           build.state == continuum.STATE_BUILDING ):

        if ( timeout <= 0 ):
            fail( "Timeout while waiting for build (id=%(id)s) to complete" % { "id" : buildId } )

        time.sleep( sleepInterval )

        timeout -= sleepInterval

        build = continuum.getBuild( buildId )

    return build

def waitForSuccessfulCheckOut( continuum, projectId ):
    project = waitForCheckOut( continuum, projectId )

#    assertEquals( "The check out was not successful for project #" + project.id, continuum.STATE_NEW, project.state )
    assertEquals( "The check out was not successful for project #" + project.id, None, project.checkOutErrorMessage )
    assertEquals( "The check out was not successful for project #" + project.id, None, project.checkOutErrorException )
    assertTrue( "The check out was not successful for project #" + project.id, project.checkOutScmResult.success )

    return project

def waitForCheckOut( continuum, projectId ):
    timeout = 60
    sleepInterval = 0.1

    project = continuum.getProject( projectId )

    while( project.checkOutScmResult == None and
           project.checkOutErrorMessage == None and
           project.checkOutErrorException == None ):
        project = continuum.getProject( projectId )
        time.sleep( sleepInterval )
        timeout -= sleepInterval

        if ( timeout <= 0 ):
            fail( "Timeout while waiting for checkout (project id=%(id)s) to complete" % { "id" : project.id } )

    return project

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

def getProjectId( projectIds ):
    if ( len( projectIds ) != 1 ):
        fail( "When adding a project only a single project was expected to be added, project ids: " + str( projectIds ) );

    return projectIds[ 0 ]

def writeMavenOnePom( filename, artifactId, scmUrl, email ):
    pom = file( "/" + filename, "w+" )
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
        "scmUrl" : scmUrl,
        "email" : email
      } )
    pom.close()

def initMaven1Project( basedir, scm, cvsroot, artifactId ):
    cleanDirectory( basedir )
    os.makedirs( basedir )
    writeMavenOnePom( basedir + "/project.xml", artifactId, makeScmUrl( scm, cvsroot, artifactId ), email )

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
        <configuration>
          <address>%(email)s</address>
        </configuration>
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
