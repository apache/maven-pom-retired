#!/usr/bin/python

import continuum
import sys

baseurl = "http://svn.apache.org/viewcvs.cgi/*checkout*/maven/continuum/trunk/"
pomAsText = "/pom.xml?content-type=text%2Fplain"

projects = [ 
"continuum-model", 
"continuum-core", 
"continuum-web", 
"continuum-xmlrpc" 
]

continuum = continuum.Continuum( "http://localhost:8000" )

for project in projects:
    url = baseurl + project + pomAsText
    print url
    mavenProject = continuum.addMavenTwoProject( url )
