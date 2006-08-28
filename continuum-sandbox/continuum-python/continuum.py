import os
from time import strftime, localtime
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
    elif ( state == Continuum.STATE_WARNED ):
        return "warned"
    else:
        return "UNKNOWN STATE (" + str( state ) + ")."

def decodeTrigger( trigger ):
    if (trigger == Continuum.TRIGGER_FORCED):
        return "forced"
    elif (trigger == Continuum.TRIGGER_SCHEDULED):
        return "scheduled"
    else:
        return "UNKNOWN TRIGGER (" + str( trigger ) + ")."
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
    STATE_WARNED = 9

    TRIGGER_SCHEDULED = 0
    TRIGGER_FORCED = 1

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
        result = checkResult( self.server.continuum.getProjects() )

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
        result = checkResult( self.server.continuum.getBuildResult( buildId ) )

        return Build( result[ "build" ] )

    def getBuildsForProject( self, projectId ):
        result = checkResult( self.server.continuum.getBuildResultsForProject( projectId ) )

        builds = []
        for build in result[ "builds" ]:
            builds.append( Build( build ) )

        return builds

    def getBuildOutput( self, projectId, buildId ):
        return checkResult( self.server.continuum.getBuildOutput( projectId, buildId ) )[ "buildOutput" ]

    def getChangedFilesForBuild( self, buildId ):
        result = checkResult( self.server.continuum.getBuild( buildId ) )
        scmResult = ScmResult( result[ "scmResult" ] )

        return scmResult.changes

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
        self.dependencies = []
        self.developers = []
        self.notifiers = []

        if ( map == None ):
            return

        self.id = map[ "id" ]
        self.name = map[ "name" ]
        self.scmUrl = map[ "scmUrl" ]
        self.version = map[ "version" ]
        self.build = map[ "buildNumber" ]
        if ( map.has_key( "workingDirectory" ) ):
            self.workingDirectory = map[ "workingDirectory" ]
        else:
            self.workingDirectory = ""
        self.state = int( map[ "state" ] )
        self.executorId = map[ "executorId" ]

        if ( map.has_key( "commandLineArguments" ) ):
            self.commandLineArguments = map[ "commandLineArguments" ]
        else:
            self.commandLineArguments = ""

        self.dependencies = list()
        if ( map.has_key( "dependencies" ) ):
            for f in map[ "dependencies" ]:
                self.dependencies.append( ContinuumDependency( f ) )

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
            "version: " + self.version + os.linesep +\
            "executor id: " + self.executorId + os.linesep +\
            "state: " + decodeState( self.state ) + os.linesep

        return s

class MavenTwoProject( Project ):
    def __init__( self, map=None ):
        Project.__init__( self, map )

        if ( map == None ):
            return
    
        self.goals = '' #map[ "goals" ]
        self.group = map[ "groupId" ]

    def __str__( self ):
        return Project.__str__( self ) + os.linesep +\
               "goals: " + self.goals + os.linesep

class MavenOneProject( Project ):
    def __init__( self, map=None ):
        Project.__init__( self, map )

        if ( map == None ):
            return
    
        self.goals = '' #map[ "goals" ]
        self.group = map[ "groupId" ]

    def __str__( self ):
        return Project.__str__( self ) + os.linesep +\
               "goals: " + self.goals + os.linesep

class AntProject( Project ):
    def __init__( self, map=None ):
        Project.__init__( self, map )

        if ( map == None ):
            return
#        self.executable = map[ "executable" ]
#        self.targets = map[ "goals" ]

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
        map[ "totalTime" ] = int( map[ "endTime" ] )/ 1000 - int( map[ "startTime" ] ) / 1000

        self.id = map[ "id" ]
        self.state = int( map[ "state" ] )
        self.buildNumber = map[ "buildNumber" ]
        if ( self.state != Continuum.STATE_OK ):
            self.buildNumber = ''
        if ( map.has_key( "trigger" ) ):
            self.forced = map[ "trigger" ] == Continuum.TRIGGER_FORCED
            self.trigger = int( map[ "trigger" ] )
        else:
            self.forced = False
            self.trigger = 0

        self.startTime = localtime( int( map[ "startTime" ] ) / 1000 )
        self.endTime = localtime( int( map[ "endTime" ] ) / 1000 )
        self.totalTime = map[ "totalTime" ]
        self.error = map.get( "error" )
        self.map = map
        if ( map.has_key( "scmResult" ) ):
            self.scmResult = ScmResult( map[ "scmResult" ] )
        else:
            self.scmResult = None

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
            "End time: " + strftime( "%a, %d %b %Y %H:%M:%S +0000", self.endTime ) + os.linesep +\
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

        self.changes = list()
        if ( map.has_key( "changes" ) ):
            for f in map[ "changes" ]:
                self.changes.append( ChangeSet( f ) )

    def __str__( self ):
        value = "Success: " + str( self.success ) + os.linesep +\
                "Provider Message: " + self.providerMessage + os.linesep +\
                "Command output: " + self.commandOutput

        return value

class ChangeSet:
    def __init__( self, map ):
        self.map = map
        self.author = map[ "author" ]
        self.comment = map[ "comment" ]
        self.date = localtime( int( map[ "date" ] ) / 1000 )

        self.files = list()
        for f in map[ "files" ]:
            self.files.append( ChangeFile( f ) )

    def __str__( self ):
        value = "Author: " + self.author + os.linesep +\
                "Comment: " + self.comment

        return value

class ChangeFile:
    def __init__( self, map ):
        self.map = map
        self.name = map[ "name" ]
        self.revision = map[ "revision" ]

    def __str__( self ):
        value = "File: " + self.name + " (" + self.revision + ")"

        return value

class ContinuumDependency:
    def __init__( self, map ):
        self.group = map[ "groupId" ]
        self.artifact = map[ "artifactId" ]
        self.version = map[ "version" ]

    def __str__( self ):
        value = self.group + ":" + self.artifact + ":" + self.version
        return value

class ContinuumDeveloper:
    def __init__( self, map ):
        self.id = map[ "continuumId" ]
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
