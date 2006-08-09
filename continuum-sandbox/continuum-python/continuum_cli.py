#!/usr/bin/env python

import cli
import continuum
import time

##########################################################
# Build your commands in this class.  Each method that
# starts with "do_" is exposed as a shell command, and
# its doc string is used when the user types 'help' or
# 'man'.  A command that does not return None, will
# cause the shell to terminate.
#
# The first line of the docstring is used when help is
# typed by itself to give a summary, and then if the
# user requests specific help on a command, the full
# text is supplied.
#
# If your system has the GNU readline stuff on it, then
# pressing tab will do tab completion of the commands.
# You will also get much nicer command line editing
# just like using your shell, as well as command
# history.
##########################################################

def isEmpty( str ):
    return str == None or str == ""

class ContinuumXmlRpcCli(cli.cli):
    def __init__(self):
        cli.cli.__init__(self)

    def do_quit(self, args):
        """Exit the command interpreter.
        Use this command to quit the demo shell."""

        return 1

    def do_version(self, args):
        """Display the version of the shell.
        Prints the version of this software on the command line."""

        print "Version 1.0"

    def do_addMavenTwoProject(self, args):
        """Add a Maven 2.x project."""

        projectIds = c.addMavenTwoProject( args[0] )

        print "Added " + str( len( projectIds ) ) + " projects."
        for id in projectIds:
            print " id: " + id

    def do_addMavenOneProject(self, args):
        """Add a Maven 1.x project."""

        projectId = c.addMavenOneProject( args[0] )

        print "Added project, id: " + projectId

    # TODO: addAntProject
    # TODO: addShellProject

    def do_showProject(self, args):
        """Shows Continuum project.
        Use this command to show the details of a Continuum project."""

        project = c.getProject( int( args[0])  )

        print "Project details:"
        print "Id: " + project.id
        print "Name:               " + project.name
        print "Version:            " + project.version
        print "Working directory:  " + project.workingDirectory
        print "State:              " + continuum.decodeState( project.state )
        print "Executor type:      " + project.executorId
        print "SCM URL:            " + project.scmUrl

        builds = c.getBuildsForProject( int( project.id ) )
        print ""
        print "Project Builds:"
        print "|  Id  |  State |           Start time            |             End time            | Build time |"
        for build in builds:
            build.state = continuum.decodeState( build.state )
            print "| %(id)4s | %(state)6s | %(startTime)s | %(endTime)s | %(totalTime)10s |" % { 'id': build.id, 'state': build.state, 'startTime' : time.strftime( "%a, %d %b %Y %H:%M:%S +0000", build.startTime ), 'endTime': time.strftime( "%a, %d %b %Y %H:%M:%S +0000", build.endTime ), 'totalTime': build.totalTime }

        print ""
        print "Notifiers:"
        for notifier in project.notifiers:
            print " type: " + notifier.type
            print " configuration: " + str( notifier.configuration )

    def do_showProjects(self, args):
        """Shows all Continuum projects registeret.
        Use this command to list all Continuum projects."""

        projects = c.getAllProjects()

        print ""
        print "Projects:"
        print "  Id |    State     | Executor | Name"
        for project in projects:
#            project.state = continuum.decodeState( project.state ) 
# | %(state)12s
            print "%(id)4s  | %(executorId)s | %(name)s" % project.map

    def do_removeProject(self,args):
        """Removes a project."""

        c.removeProject( args[0] )

    def do_buildProject(self, args):
        """Build a Continuum project.
        Use this command to signal a build for a Continuum project."""

        c.buildProject( args[ 0 ] )

        print "Enqueued project"

    def do_showBuild( self, args ):
        """Shows the result of a build."""

        build = c.getBuild( int( args[ 0 ] ) )

        print build

        if ( build.updateScmResult != None and len( build.updateScmResult.updatedFiles ) > 0 ):
            print "Updated files:"
            print build.updateScmResult
        print ""

        buildResult = c.getBuildResultForBuild( args[ 0 ] );

        print "Build result:"
        print buildResult

    def do_run(self, args):
        """Run a script of commands.
        Use this command to run a script of commands."""
        
        commands = open( args[0], "r" ).readlines()
        
        for command in commands:
            cli.cli.onecmd( self, command )
        
##########################################################
# Main loop
##########################################################

c = continuum.Continuum( "http://localhost:8000" )

try:
    ContinuumXmlRpcCli().cmdloop()

except Exception, e:
    print "Error:", e

