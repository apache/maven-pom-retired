import cli
import os
import socket
import sys
from time import strftime, gmtime
import xmlrpclib

STATE_NEW = "new"
STATE_OK = "ok"
STATE_FAILED = "failed"
STATE_ERROR = "error"
STATE_BUILD_SIGNALED = "build signaled"
STATE_BUILDING = "building"

server = xmlrpclib.Server("http://localhost:8000")

try:
    server.continuum.getAllProjects()
except socket.error, msg:
    print "Error while connecting to the XML-RPC server"
    print msg
    sys.exit( -1 )

def checkResult( map ):
    if ( map[ "result" ] == "ok" ):
        return map

    print "Error while executing method."
    print "Method: " + map[ "method" ]
    print "Message: " + map[ "message" ]
    print "Stack trace: " + map[ "stackTrace" ]

    raise Exception( "Error while executing method" )

def decodeState( state ):
    if ( state == 1 ):
        return STATE_NEW
    elif ( state == 2 ):
        return STATE_OK
    elif ( state == 3 ):
        return STATE_FAILED
    elif ( state == 4 ):
        return STATE_ERROR
    elif ( state == 5 ):
        return STATE_BUILD_SIGNALED
    elif ( state == 6 ):
        return STATE_BUILDING
    else:
       return "UNKNOWN STATE (" + state + ")."

   
# Maven 2.x project

def addMavenTwoProject( url ):
    result = checkResult( server.continuum.addMavenTwoProject( url ) )

    return result[ "projectId" ]

def addProjectFromUrl( url, builderId ):
    result = checkResult( server.continuum.addProjectFromUrl( url, builderId ) )

    return result[ "projectId" ]

def addProjectFromScm( scmUrl, builderId, name, nagEmailAddress, version, configuration ):
    result = checkResult( server.continuum.addProjectFromScm( scmUrl, builderId, name, nagEmailAddress, version, configuration ) )

    return result[ "projectId" ]

def getProject( projectId ):
    result = checkResult( server.continuum.getProject( projectId ) )

    return Project( result[ "project" ] )

def updateProjectFromScm( projectId ):
    checkResult( server.continuum.updateProjectFromScm( projectId ) )

def updateProjectConfiguration( projectId, configuration ):
    checkResult( server.continuum.updateProjectConfiguration( projectId, configuration ) )

def getAllProjects():
    result = checkResult( server.continuum.getAllProjects() )

    return result[ "projects" ]

def removeProject( projectId ):
    checkResult( server.continuum.removeProject( projectId ) )

def buildProject( projectId ):
    result = checkResult( server.continuum.buildProject( projectId ) )

    return result[ "buildId" ]

def getBuildsForProject( projectId, start, end ):
    result = checkResult( server.continuum.getBuildsForProject( projectId, start, end ) )

    builds = []
    for build in result[ "builds" ]:
        builds.append( Build( build ) )

    return builds

def getBuild( buildId ):
    result = checkResult( server.continuum.getBuild( buildId ) )

    return Build( result[ "build" ] )

def getBuildResult( buildId ):
    result = checkResult( server.continuum.getBuildResult( buildId ) )

    buildResult = result[ "buildResult" ]

    if ( len( buildResult ) == 0 ):
        return None

    return BuildResult( buildResult )

class Project:
    def __init__( self, map ):
        self.map = map
        self.id = map[ "id" ]
        self.name = map[ "name" ]
        self.nagEmailAddress = map[ "nagEmailAddress" ]
        self.state = decodeState( int( map[ "state" ] ) )
        self.version = map[ "version" ]
        self.builderId = map[ "builderId" ]
        self.configuration = map[ "configuration" ]
        self.checkOutScmResult = CheckOutScmResult( map[ "checkOutScmResult" ] )

    def __str__( self ):
        str = "id: " + self.id + os.linesep +\
              "name: " + self.name + os.linesep +\
              "nagEmailAddress: " + self.nagEmailAddress + os.linesep +\
              "state: " + self.state + os.linesep +\
              "version: " + self.version + os.linesep +\
              "builder id: " + self.builderId + os.linesep +\
              "check out ok: " + self.checkOutResult.success + os.linesep

        if ( len( self.configuration.keys() ) > 0 ):
            conf = ""
            for key in self.configuration.keys():
                conf += os.linesep + key + "=" + self.configuration[ key ]
            str += conf

        return str

class Build:
    def __init__( self, map ):
        map[ "totalTime" ] = int( map[ "endTime" ] )/ 1000 - int( map[ "startTime" ] ) / 1000
        map[ "startTime" ] = strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime( int( map[ "startTime" ] ) / 1000 ) )
        map[ "endTime" ] = strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime( int( map[ "endTime" ] ) / 1000 ) )
        map[ "state" ] = decodeState( int( map[ "state" ] ) )

        self.id = map[ "id" ]
        self.state = map[ "state" ]
        self.startTime = map[ "startTime" ]
        self.endTime = map[ "endTime" ]
        self.totalTime = map[ "totalTime" ]
        self.error = map.get( "error" )
        self.map = map
        if ( map.has_key( "updateScmResult" ) ):
            self.updateScmResult = UpdateScmResult( map[ "updateScmResult" ] )
        else:
            self.updateScmResult = None

        if ( self.error == None ):
            self.error = ""
            map[ "error" ] = ""

    def __str__( self ):
        map = self.map

        value = """Id: %(id)s
State: %(state)s
Start time: %(startTime)s
End time: %(endTime)s
Build time: %(totalTime)ss
""" % map

        if ( self.error != "" ):
            value += "Error: %(error)s" % map

        return value

class BuildResult:
    def __init__( self, map ):
        # This is the common stuff between all ContinuumBuildResult objects
        self.success = map[ "success" ] == "true"
        self.buildExecuted = map[ "buildExecuted" ] == "true"
        #self.changedFiles = map[ "changedFiles" ]

        # These fields just happen to be the same for all the build results
        if ( self.buildExecuted ):
            self.exitCode = int( map[ "exitCode" ] )
            self.standardOutput = map[ "standardOutput" ]
            self.standardError = map[ "standardError" ]

    def __str__( self ):
        value = "Success: " + str( self.success ) + os.linesep +\
                "Build executed: " + str( self.buildExecuted )

        if ( self.buildExecuted ):
            value += os.linesep + "Exit code: " + str( self.exitCode )

            if ( len( self.standardOutput ) > 0 ):
                  value += os.linesep + "Standard output: " + self.standardOutput

            if ( len( self.standardError ) > 0 ):
                   value += os.linesep + "Standard error: " + self.standardError

        return value

class ScmResult:
    def __init__( self, map ):
        self.map = map
        self.success = map[ "success" ] == "true"

        if ( map.has_key( "providerMessage" ) ):
            self.providerMessage = map[ "providerMessage" ]
        else:
            self.providerMessage = ""

        if ( map.has_key( "commandOutput" ) ):
            self.commandOutput = map[ "commandOutput" ]
        else:
            self.commandOutput = ""

    def __str__( self ):
        value = "Success: " + str( self.success ) + os.linesep +\
                 "Provider Message: " + self.providerMessage + os.linesep +\
                 "Command output: " + self.commandOutput

        return value

class CheckOutScmResult( ScmResult ):
    def __init__( self, map ):
        self.map = map
        ScmResult.__init__( self, map )
        self.checkedOutFiles = list()

        for file in map[ "checkedOutFiles" ]:
            self.checkedOutFiles.append( ScmFile( file ) )

    def __str__( self ):
        value = ScmResult.__str__( self ) + os.linesep

        value += "Checked out files: " + os.linesep
        for file in self.checkedOutFiles:
            value += " " + file.path + os.linesep

        return value

class UpdateScmResult( ScmResult ):
    def __init__( self, map ):
        self.map = map
        ScmResult.__init__( self, map )
        self.updatedFiles = list()

        for file in map[ "updatedFiles" ]:
            self.updatedFiles.append( ScmFile( file ) )

    def __str__( self ):
        value = ScmResult.__str__( self ) + os.linesep

        value += "Updated files: " + os.linesep
        if ( len( self.updatedFiles ) > 0):
            for file in self.updatedFiles:
                value += " " + file.path + os.linesep
        else:
            value += " No files updated"

        return value

class ScmFile:
    def __init__( self, map ):
        self.map = map
        self.path = map[ "path" ]
