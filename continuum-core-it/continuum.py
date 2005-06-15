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
    if ( state == Continuum.STATE_NEW ):
        return "new"
    elif ( state == Continuum.STATE_OK ):
        return "ok"
    elif ( state == Continuum.STATE_FAILED ):
        return "failed"
    elif ( state == Continuum.STATE_ERROR ):
        return "error"
#    elif ( state == 5 ):
#        return Continuum.STATE_BUILD_SIGNALED
    elif ( state == Continuum.STATE_BUILDING ):
        return "building"
    elif ( state == Continuum.STATE_CHECKING_OUT ):
        return "checking out"
    elif ( state == Continuum.STATE_UPDATING ):
        return "updating"
    else:
        return "UNKNOWN STATE (" + str( state ) + ")."

def makeMailNotifier( address ):
    notifier = ContinuumNotifier()

    notifier.type = "mail"
    notifier.configuration = { "address" : address }

class Continuum:
    STATE_NEW = 1
    STATE_OK = 2
    STATE_FAILED = 3
    STATE_ERROR = 4
    #STATE_BUILD_SIGNALED = "build signaled"
    STATE_BUILDING = 6
    STATE_CHECKING_OUT = 7
    STATE_UPDATING = 8

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

    #def updateProjectConfiguration( self, projectId, configuration ):
    #    checkResult( self.server.continuum.updateProjectConfiguration( projectId, configuration ) )

    def getProject( self, projectId ):
        result = checkResult( self.server.continuum.getProject( projectId ) )

        return self.makeProject( result[ "project" ] )

    def getProjects( self ):
        result = checkResult( self.server.continuum.getAllProjects() )

        projects = []
        for project in result[ "projects" ]:
            projects.append( self.makeProject( project ) )

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

#    def getBuildResultForBuild( self, buildId ):
#        result = checkResult( self.server.continuum.getBuildResultForBuild( buildId ) )
#
#        buildResult = result[ "buildResult" ]
#
#        if ( len( buildResult ) == 0 ):
#            return None
#
#        return BuildResult( buildResult )

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

        return result[ "projectIds" ]

    def updateAntProject( self, antProject ):
        checkResult( self.server.continuum.updateAntProject( antProject ) )

    ####################################################################
    # Shell projects
    ####################################################################

    def addShellProject( self, shellProject ):
        result = checkResult( self.server.continuum.addShellProject( shellProject ) )

        return result[ "projectIds" ]

    def updateShellProject( self, shellProject ):
        checkResult( self.server.continuum.updateShellProject( shellProject ) )

    ####################################################################
    #
    ####################################################################

    def makeProject( self, map ):
        executorId = map[ "executorId" ]
        if ( executorId == "maven2" ):
            return MavenTwoProject( map )
        elif ( executorId == "maven-1" ):
            return MavenOneProject( map )
        elif ( executorId == "ant" ):
            return AntProject( map )
        elif ( executorId == "shell" ):
            return ShellProject( map )
        else:
            raise Exception( "Unknown executor id '" + executorId + "'." );


####################################################################
# Domain classes
####################################################################

class Project:
    def __init__( self, map ):
        self.map = map;
        self.developers = []
        self.notifiers = []

        if ( map == None ):
            return

#        map[ "state" ] = decodeState( int( map[ "state" ] ) )
        self.id = map[ "id" ]
        self.name = map[ "name" ]
        self.scmUrl = map[ "scmUrl" ]
        self.version = map[ "version" ]
        self.workingDirectory = map[ "workingDirectory" ]
        self.state = int( map[ "state" ] )
        self.executorId = map[ "executorId" ]

        if ( map.has_key( "commandLineArguments" ) ):
            self.commandLineArguments = map[ "commandLineArguments" ]
        else:
            self.commandLineArguments = ""

#        self.configuration = map[ "configuration" ]

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

        self.developers = list()
        if ( map.has_key( "developers" ) ):
            for f in map[ "developers" ]:
                self.developers.append( ContinuumDeveloper( f ) )

        self.notifiers = list()
        if ( map.has_key( "notifiers" ) ):
            for f in map[ "notifiers" ]:
                self.notifiers.append( ContinuumNotifier( f ) )

    def __str__( self ):
        s = "id: " + self.id + os.linesep +\
            "name: " + self.name + os.linesep +\
            "state: " + decodeState( self.state ) + os.linesep +\
            "version: " + self.version + os.linesep +\
            "executor id: " + self.executorId + os.linesep

        return s

class MavenTwoProject( Project ):
    def __init__( self, map=None ):
        Project.__init__( self, map )

        if ( map == None ):
            return
    
        self.goals = map[ "goals" ]

    def __str__( self ):
        return Project.__str__( self ) + os.linesep +\
               "goals: " + self.goals + os.linesep

class MavenOneProject( Project ):
    def __init__( self, map=None ):
        Project.__init__( self, map )

        if ( map == None ):
            return
    
        self.goals = map[ "goals" ]

    def __str__( self ):
        return Project.__str__( self ) + os.linesep +\
               "goals: " + self.goals + os.linesep

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

class ShellProject( Project ):
    def __init__( self, map=None ):
        Project.__init__( self, map )

        if ( map == None ):
            return
    
        self.executable = map[ "executable" ]

    def __str__( self ):
        return Project.__str__( self ) + os.linesep +\
               "executable: " + self.executable + os.linesep

class Build:
    def __init__( self, map ):
        #map[ "state" ] = decodeState( int( map[ "state" ] ) )
        map[ "forced" ] = bool( map[ "forced" ] )
        map[ "totalTime" ] = int( map[ "endTime" ] )/ 1000 - int( map[ "startTime" ] ) / 1000
        map[ "startTime" ] = strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime( int( map[ "startTime" ] ) / 1000 ) )
        map[ "endTime" ] = strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime( int( map[ "endTime" ] ) / 1000 ) )

        self.id = map[ "id" ]
        self.state = int( map[ "state" ] )
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

        self.success = map[ "success" ] == "true"
        self.exitCode = int( map[ "exitCode" ] )
        if ( map.has_key( "standardOutput" ) ):
            self.standardOutput = map[ "standardOutput" ]
        else:
            self.standardOutput = None
        if ( map.has_key( "standardError" ) ):
            self.standardError = map[ "standardError" ]
        else:
            self.standardError = None

    def __str__( self ):
        s = "Id: " + self.id + os.linesep +\
            "State: " + decodeState( self.state ) + os.linesep +\
            "End time: " + self.endTime + os.linesep +\
            "Build time: " + self.totalTime + os.linesep

        if ( self.error != "" ):
            s += "Error: %(error)s" % self.map
        value = "Success: " + str( self.success )
        value += os.linesep + "Exit code: " + str( self.exitCode )
        if ( len( self.standardOutput ) > 0 ):
              value += os.linesep + "Standard output: " + self.standardOutput
        if ( len( self.standardError ) > 0 ):
               value += os.linesep + "Standard error: " + self.standardError

        return s

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

class ContinuumDeveloper:
    def __init__( self, map ):
        self.id = map[ "id" ]
        self.name = map[ "name" ]
        self.email = map[ "email" ]

    def __str__( self ):
        value = "id: " + self.id + os.linesep +\
                "name: " + self.name + os.linesep +\
                "email: " + self.email

        return value

class ContinuumNotifier:
    def __init__( self, map=None ):
        self.type = None
        self.configuration = {}

        if ( map == None ):
            return

        self.type = map[ "type" ]

        if ( map.has_key( "configuration" ) ):
            self.configuration = map[ "configuration" ]

    def __str__( self ):
        value = "type: " + self.type

        return value
