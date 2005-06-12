#!/usr/bin/python

import continuum
import sys

baseurl = "http://svn.apache.org/viewcvs.cgi/*checkout*/maven/components/trunk/"
pomAsText = "/pom.xml?content-type=text%2Fplain"

projects = [ 
"maven-model", 
"maven-project", 
"maven-artifact", 
"maven-core" 
]

continuum = continuum.Continuum( "http://localhost:8000" )

for project in projects:
    url = baseurl + project + pomAsText
    print url
    mavenProject = continuum.addMavenTwoProject( url )
