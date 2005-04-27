#!/usr/bin/env python

import cli
import continuum

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

class ContinuumXmlRpcClient(cli.cli):
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

    def do_addProject(self, args):
        """Add a Continuum project.
        Use this command to add a project to Continuum."""

        projectId = continuum.addProjectFromUrl( args[0], args[1] )

        print "Added project, id: " + projectId

    def do_showProject(self, args):
        """Shows Continuum project.
        Use this command to show the details of a Continuum project."""

        project = continuum.getProject( args[0] )

        print "Project details:"
        print """Id: %(id)s
Name: %(name)s
Version: %(version)s
Working directory: %(workingDirectory)s
Builder type: %(builderId)s""" % project.map

        print ""
        print "Checked out files:"
        print project.checkOutScmResult

        print "Project Configuration:"
        for key in project.configuration.keys():
            print key + "=" + project.configuration[ key ]

        print ""
        print "Project Builds:"
        print "|  Id  |  State |           Start time            |             End time            | Build time |"
        builds = continuum.getBuildsForProject( project.id, 0, 0 )
        for build in builds:
            build.state = continuum.decodeState( build.state )
            print "| %(id)4s | %(state)6s | %(startTime)s | %(endTime)s | %(totalTime)10s |" % build.map

    def do_showProjects(self, args):
        """Shows all Continuum projects registeret.
        Use this command to list all Continuum projects."""

        projects = continuum.getAllProjects()

        for project in projects:
            print "Id %(id)s, name: '%(name)s'" % project

    def do_buildProject(self, args):
        """Build a Continuum project.
        Use this command to signal a build for a Continuum project."""

        buildId = continuum.buildProject( args[ 0 ] )

        print "Enqueued project, build id: " + buildId

    def do_showBuild( self, args ):
        """Shows the result of a build."""

        build = continuum.getBuild( args[ 0 ] );

        print build

        if ( build.updateScmResult != None and len( build.updateScmResult.updatedFiles ) > 0 ):
            print "Updated files:"
            print build.updateScmResult
        print ""

        buildResult = continuum.getBuildResult( args[ 0 ] );

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

try:
    ContinuumXmlRpcClient().cmdloop()

except Exception, e:
    print "Error:", e

