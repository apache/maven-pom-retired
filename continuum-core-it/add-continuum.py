import continuum
import sys

baseurl = "http://cvs.continuum.codehaus.org/*checkout*/continuum-apache/"

continuumId = continuum.addProjectFromUrl( baseurl + "pom.xml", "maven2" )
continuumModelId = continuum.addProjectFromUrl( baseurl + "continuum-model/pom.xml", "maven2" )
continuumCoreId = continuum.addProjectFromUrl( baseurl + "continuum-core/pom.xml", "maven2" )
continuumXmlRpcId = continuum.addProjectFromUrl( baseurl + "continuum-xmlrpc/pom.xml", "maven2" )
continuumWebId = continuum.addProjectFromUrl( baseurl + "continuum-web/pom.xml", "maven2" )
