import continuum
import sys

baseurl = "http://cvs.plexus.codehaus.org/*checkout*/plexus/"

plexusComponentsId = continuum.addProjectFromUrl( baseurl + "plexus-components/pom.xml", "maven2" );
plexusActionId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-action/pom.xml", "maven2" );
plexusArchiverId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-archiver/pom.xml", "maven2" );
plexusBayesianId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-bayesian/pom.xml", "maven2" );
plexusCommandId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-command/pom.xml", "maven2" );
plexusCompilerId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-compiler/pom.xml", "maven2" );
plexusCompilerApiId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-compiler/plexus-compiler-api/pom.xml", "maven2" );
plexusCompilersId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-compiler/plexus-compilers/pom.xml", "maven2" );
plexusCompilerAspectjId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-aspectj/pom.xml", "maven2" );
plexusCompilerEclipseId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-eclipse/pom.xml", "maven2" );
plexusCompilerJavacId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-javac/pom.xml", "maven2" );
plexusCompilerKikesId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-compiler/plexus-compilers/plexus-compiler-jikes/pom.xml", "maven2" );
plexusDroolsId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-drools/pom.xml", "maven2" );
plexusFormicaId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-formica-web/pom.xml", "maven2" );
plexusFormicaId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-formica/pom.xml", "maven2" );
plexusHibernateId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-hibernate/pom.xml", "maven2" );
plexusI18nId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-i18n/pom.xml", "maven2" );
plexusJettyId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-jetty-httpd/pom.xml", "maven2" );
plexusJettyId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-jetty/pom.xml", "maven2" );
plexusMimetyperId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-mimetyper/pom.xml", "maven2" );
plexusSummitId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-summit/pom.xml", "maven2" );
plexusVelocityId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-velocity/pom.xml", "maven2" );
plexusWerkflowId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-werkflow/pom.xml", "maven2" );
plexusXmlrpcId = continuum.addProjectFromUrl( baseurl + "plexus-components/plexus-xmlrpc/pom.xml", "maven2" );
plexusContainersId = continuum.addProjectFromUrl( baseurl + "plexus-containers/pom.xml", "maven2" );
plexusContainerArtifactId = continuum.addProjectFromUrl( baseurl + "plexus-containers/plexus-container-artifact/pom.xml", "maven2" );
plexusContainerDefaultId = continuum.addProjectFromUrl( baseurl + "plexus-containers/plexus-container-default/pom.xml", "maven2" );
plexusUtilsId = continuum.addProjectFromUrl( baseurl + "plexus-utils/pom.xml", "maven2" );
plexusServletId = continuum.addProjectFromUrl( baseurl + "plexus-servlet/pom.xml", "maven2" );
plexusToolsId = continuum.addProjectFromUrl( baseurl + "plexus-tools/pom.xml", "maven2" );
plexusCdcId = continuum.addProjectFromUrl( baseurl + "plexus-tools/plexus-cdc/pom.xml", "maven2" );
plexusRuntimeBuilderId = continuum.addProjectFromUrl( baseurl + "plexus-tools/plexus-runtime-builder/pom.xml", "maven2" );

