#!/usr/bin/python

import continuum
import sys

baseurl = "http://cvs.plexus.codehaus.org/*checkout*/plexus/"
pomAsText = "/pom.xml?content-type=text%2Fplain"

projects = [ 
"plexus-components/plexus-action/pom.xml",
"plexus-components/plexus-archiver/pom.xml",
"plexus-components/plexus-bayesian/pom.xml",
"plexus-components/plexus-command/pom.xml",
"plexus-components/plexus-compiler/pom.xml",
"plexus-components/plexus-compiler/plexus-compiler-api/pom.xml",
"plexus-components/plexus-compiler/plexus-compilers/pom.xml",
"plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-aspectj/pom.xml",
"plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-eclipse/pom.xml",
"plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-javac/pom.xml",
"plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-jikes/pom.xml",
"plexus-components/plexus-drools/pom.xml",
"plexus-components/plexus-formica-web/pom.xml",
"plexus-components/plexus-formica/pom.xml",
"plexus-components/plexus-hibernate/pom.xml",
"plexus-components/plexus-i18n/pom.xml",
"plexus-components/plexus-jetty-httpd/pom.xml",
"plexus-components/plexus-jetty/pom.xml",
"plexus-components/plexus-mimetyper/pom.xml",
"plexus-components/plexus-summit/pom.xml",
"plexus-components/plexus-velocity/pom.xml",
"plexus-components/plexus-werkflow/pom.xml",
"plexus-components/plexus-xmlrpc/pom.xml",
"plexus-containers/pom.xml",
"plexus-containers/plexus-container-artifact/pom.xml",
"plexus-containers/plexus-container-default/pom.xml",
"plexus-utils/pom.xml",
"plexus-servlet/pom.xml",
"plexus-tools/pom.xml",
"plexus-tools/plexus-cdc/pom.xml",
"plexus-tools/plexus-runtime-builder/pom.xml" 
]

continuum = continuum.Continuum( "http://localhost:8000" )

for project in projects:
    url = baseurl + project + pomAsText
    print url
    mavenProject = continuum.addMavenTwoProject( url )
