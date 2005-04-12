import continuum
import sys

baseurl = "scm:svn:http://svn.apache.org/repos/asf/jakarta/commons/proper"
configuration = { "executable" : "ant", "targets" : "all" }

langId = continuum.addProjectFromScm( baseurl + "/lang/trunk", "ant", "Jakarta Commons Lang", "nobody@localhost", "1.0", configuration )
loggingId = continuum.addProjectFromScm( baseurl + "/logging/trunk", "ant", "Jakarta Commons Logging", "nobody@localhost", "1.0", configuration )

continuum.buildProject( langId )
continuum.buildProject( loggingId )
