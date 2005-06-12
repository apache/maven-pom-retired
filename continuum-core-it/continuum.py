import os
from time import strftime, gmtime
import xmlrpclib

class XmlRpcException:
    def __init__( self, method, message, stackTrace ):
        self.method = method
        self.message = message
        self.stackTrace = stackTrace

    def __str__( self ):
        return "Error while executing method." + os.linesep + \
               "Method: " + self.method + os.linesep + \
               "Message: " + self.message + os.linesep + \
               "Stack trace: " + self.stackTrace + os.linesep

def checkResult( map ):
    if ( map[ "result" ] == "ok" ):
        return map

    ex = XmlRpcException( map[ "method" ], 
                          map[ "message" ], 
                          map[ "stackTrace" ] )

    if 1:
        print str( ex )

    raise ex

def decodeState( state ):
    if ( state == 1 ):
        return Continuum.STATE_NEW
    elif ( state == 2 ):
        return Continuum.STATE_OK
    elif ( state == 3 ):
        return Continuum.STATE_FAILED
    elif ( state == 4 ):
        return Continuum.STATE_ERROR
#    elif ( state == 5 ):
#        return Continuum.STATE_BUILD_SIGNALED
    elif ( state == 6 ):
        return Continuum.STATE_BUILDING
    elif ( state == 7 ):
        return Continuum.STATE_CHECKING_OUT
    elif ( state == 8 ):
        return Continuum.STATE_UPDATING
    else:
        return "UNKNOWN STATE (" + str( state ) + ")."

class Continuum:
    STATE_NEW = "new"
    STATE_OK = "ok"
    STATE_FAILED = "failed"
    STATE_ERROR = "error"
    #STATE_BUILD_SIGNALED = "build signaled"
    STATE_BUILDING = "building"
    STATE_CHECKING_OUT = "checking out"
    STATE_UPDATING = "updating"

    def __init__( self, url ):
        self.server = xmlrpclib.Server(url, allow_none=True)

        # This will make sure the server is working
        self.server.continuum.getProjects()

    ####################################################################
    # These methods correspods 1<=>1 with the ContinuumXmlRpc interface
    ####################################################################

    ####################################################################
    # Projects
    ####################################################################

    def removeProject( self, projectId ):
        checkResult( self.server.continuum.removeProject( projectId ) )

    #def updateProject( projectId, name, scmUrl, nagEmailAddress, version, arguments ):
    #    checkResult( server.continuum.updateProject( projectId, name, scmUrl, nagEmailAddress, version, arguments ) )

    #def updateProjectFromScm( projectId ):
    #    checkResult( server.continuum.updateProjectFromScm( projectId ) )

    def updateProjectConfiguration( self, projectId, configuration ):
        checkResult( self.server.continuum.updateProjectConfiguration( projectId, configuration ) )

    def getProject( self, projectId ):
        result = checkResult( self.server.continuum.getProject( projectId ) )

        return Project( result[ "project" ] )

    def getProjects( self ):
        result = checkResult( self.server.continuum.getAllProjects() )

        projects = []
        for project in result[ "projects" ]:
            projects.append( Project( project ) )

        return projects

    ####################################################################
    # Builds
    ####################################################################

    def buildProject( self, projectId, force ):
        checkResult( self.server.continuum.buildProject( projectId, force ) )

    def getBuild( self, buildId ):
        result = checkResult( self.server.continuum.getBuild( buildId ) )

        return Build( result[ "build" ] )

    def getBuildsForProject( self, projectId, start=0, end=0 ):
        result = checkResult( self.server.continuum.getBuildsForProject( projectId, start, end ) )

        builds = []
        for build in result[ "builds" ]:
            builds.append( Build( build ) )

        return builds

    def getBuildResultForBuild( self, buildId ):
        result = checkResult( self.server.continuum.getBuildResultForBuild( buildId ) )

        buildResult = result[ "buildResult" ]

        if ( len( buildResult ) == 0 ):
            return None

        return BuildResult( buildResult )

    def getChangedFilesForBuild( self, buildId ):
        result = checkResult( self.server.continuum.getBuildResultForBuild( buildId ) )

        changedFiles = []
        for changedFile in result[ "changedFiles" ]:
            changedFiles.append( ScmFile( changedFile ) )

        return changedFiles

    ####################################################################
    # Maven 2.x projects
    ####################################################################

    def addMavenTwoProject( self, argument ):
        result = checkResult( self.server.continuum.addMavenTwoProject( argument ) )

        return result[ "projectIds" ]

    def updateMavenTwoProject( self, mavenTwoProject ):
        checkResult( self.server.continuum.updateaddMavenTwoProject( mavenTwoProject ) )

    ####################################################################
    # Maven 1.x projects
    ####################################################################

    def addMavenOneProject( self, argument ):
        result = checkResult( self.server.continuum.addMavenOneProject( argument ) )

        return result[ "projectIds" ]

    def updateMavenOneProject( self, mavenOneProject ):
        checkResult( self.server.continuum.updateMavenOneProject( mavenOneProject ) )

    ####################################################################
    # Ant projects
    ####################################################################

    def addAntProject( self, antProject ):
        result = checkResult( self.server.continuum.addAntProject( antProject ) )

        return result[ "projectId" ]

    ####################################################################
    # Shell projects
    ####################################################################

    def addShellProject( self, shellProject ):
        result = checkResult( self.server.continuum.addShellProject( shellProject ) )

        return result[ "projectId" ]

####################################################################
# Domain classes
####################################################################

class Project:
    def __init__( self, map ):
        self.map = map;

        if ( map == None ):
            return

        map[ "state" ] = decodeState( int( map[ "state" ] ) )
        self.id = map[ "id" ]
        self.name = map[ "name" ]
        self.scmUrl = map[ "scmUrl" ]
        self.nagEmailAddress = map[ "nagEmailAddress" ]
        self.version = map[ "version" ]
        self.workingDirectory = map[ "workingDirectory" ]
        self.state = map[ "state" ]
        self.executorId = map[ "executorId" ]

        if ( map.has_key( "commandLineArguments" ) ):
            self.commandLineArguments = map[ "commandLineArguments" ]
        else:
            self.commandLineArguments = ""

        self.configuration = map[ "configuration" ]

        if ( map.has_key( "checkOutScmResult" ) ):
            self.checkOutScmResult = CheckOutScmResult( map[ "checkOutScmResult" ] )
        else:
            self.checkOutScmResult = None
        if ( map.has_key( "checkOutErrorMessage" ) ):
            self.checkOutErrorMessage = map[ "checkOutErrorMessage" ]
        else:
            self.checkOutErrorMessage = None
        if ( map.has_key( "checkOutErrorException" ) ):
            self.checkOutErrorException = map[ "checkOutErrorException" ]
        else:
            self.checkOutErrorException = None

    def __str__( self ):
        s = "id: " + self.id + os.linesep +\
            "name: " + self.name + os.linesep +\
            "nagEmailAddress: " + self.nagEmailAddress + os.linesep +\
            "state: " + self.state + os.linesep +\
            "version: " + self.version + os.linesep +\
            "executor id: " + self.executorId + os.linesep

        if ( len( self.configuration.keys() ) > 0 ):
            conf = ""
            for key in self.configuration.keys():
                conf += os.linesep + key + "=" + self.configuration[ key ]
            s += conf

        return s

class AntProject( Project ):
    def __init__( self, map=None ):
        Project.__init__( self, map )

        if ( map == None ):
            return
    
        self.executable = map[ "executable" ]
        self.targets = map[ "targets" ]

    def __str__( self ):
        return Project.__str__( self ) + os.linesep +\
               "executable: " + self.executable + os.linesep +\
               "targets: " + self.targets + os.linesep

class Build:
    def __init__( self, map ):
        map[ "state" ] = decodeState( int( map[ "state" ] ) )
        map[ "forced" ] = bool( map[ "forced" ] )
        map[ "totalTime" ] = int( map[ "endTime" ] )/ 1000 - int( map[ "startTime" ] ) / 1000
        map[ "startTime" ] = strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime( int( map[ "startTime" ] ) / 1000 ) )
        map[ "endTime" ] = strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime( int( map[ "endTime" ] ) / 1000 ) )

        self.id = map[ "id" ]
        self.state = map[ "state" ]
        self.forced = map[ "forced" ]
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
        value = """Id: %(id)s
State: %(state)s
Start time: %(startTime)s
End time: %(endTime)s
Build time: %(totalTime)ss
""" % self.map

        if ( self.error != "" ):
            value += "Error: %(error)s" % self.map

        return value

class BuildResult:
    def __init__( self, map ):
        # This is the common stuff between all ContinuumBuildResult objects
        self.success = map[ "success" ] == "true"

        self.exitCode = int( map[ "exitCode" ] )
        self.standardOutput = map[ "standardOutput" ]
        self.standardError = map[ "standardError" ]

    def __str__( self ):
        value = "Success: " + str( self.success )

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

        for f in map[ "checkedOutFiles" ]:
            self.checkedOutFiles.append( ScmFile( f ) )

    def __str__( self ):
        value = ScmResult.__str__( self ) + os.linesep

        value += "Checked out files: " + os.linesep
        for f in self.checkedOutFiles:
            value += " " + f.path + os.linesep

        return value

class UpdateScmResult( ScmResult ):
    def __init__( self, map ):
        self.map = map
        ScmResult.__init__( self, map )
        self.updatedFiles = list()

        for f in map[ "updatedFiles" ]:
            self.updatedFiles.append( ScmFile( f ) )

    def __str__( self ):
        value = ScmResult.__str__( self ) + os.linesep

        value += "Updated files: " + os.linesep
        if ( len( self.updatedFiles ) > 0):
            for f in self.updatedFiles:
                value += " " + f.path + os.linesep
        else:
            value += " No files updated"

        return value

class ScmFile:
    def __init__( self, map ):
        self.map = map
        self.path = map[ "path" ]
