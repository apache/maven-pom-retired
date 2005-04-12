import continuum
import sys

baseurl = "http://cvs.apache.org/viewcvs.cgi/*checkout*/maven-scm"
baseurl = "file:/home/trygvis/tmp/continuum/co/maven-scm"

#scmId = continuum.addProjectFromUrl( baseurl + "/pom.xml", "maven2" )
#continuum.buildProject( scmId )
#sys.exit( -1 )

scmId = continuum.addProjectFromUrl( baseurl + "/pom.xml", "maven2" )
scmApiId = continuum.addProjectFromUrl( baseurl + "/maven-scm-api/pom.xml", "maven2" )
scmTestId = continuum.addProjectFromUrl( baseurl + "/maven-scm-test/pom.xml", "maven2" )
scmProvidersId = continuum.addProjectFromUrl( baseurl + "/maven-scm-providers/pom.xml", "maven2" )
scmLocalId = continuum.addProjectFromUrl( baseurl + "/maven-scm-providers/maven-scm-provider-local/pom.xml", "maven2" )
scmCvsId = continuum.addProjectFromUrl( baseurl + "/maven-scm-providers/maven-scm-provider-cvs/pom.xml", "maven2" )

continuum.buildProject( scmId )
continuum.buildProject( scmApiId )
continuum.buildProject( scmTestId )
continuum.buildProject( scmProvidersId )
continuum.buildProject( scmLocalId )
continuum.buildProject( scmCvsId )
