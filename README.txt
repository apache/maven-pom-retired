Getting Sun Jars
----------------
Some jar files can not distributed via http://www.ibiblio.org/maven2 
(see http://maven.apache.org/guides/mini/guide-coping-with-sun-jars.html for further information).
 
As this project depends on them, following procedure might be a prerequisite to starting the 
build (It has to be done only for the local repository and only once):

make an empty directory and download the jars from following urls:
http://java.sun.com/j2ee/connector/download.html                         (Version 1.0)
http://java.sun.com/products/jta                                         (Version 1.0.1B)
http://java.sun.com/products/jaas/index-10.html                          (Version 1.0.01)
http://java.sun.com/products/javamail/downloads/index.html               (Version 1.3.2)
http://java.sun.com/products/javabeans/glasgow/jaf.html                  (Version 1.0.2)
http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html (Some new and shiny version, then select ojdbc14.jar)

Change into the directiory and execute following commands:

mvn install:install-file -Dfile=connector.jar -DgroupId=javax.resource -DartifactId=connector -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=jta-1_0_1B-classes.zip -DgroupId=javax.transaction -DartifactId=jta -Dversion=1.0.1B -Dpackaging=jar
mvn install:install-file -Dfile=jaas-1_0_01.zip -DgroupId=javax.security -DartifactId=jaas -Dversion=1.0.01 -Dpackaging=jar
mvn install:install-file -Dfile=mail.jar -DgroupId=javax.mail -DartifactId=mail -Dversion=1.3.2 -Dpackaging=jar
mvn install:install-file -Dfile=activation.jar -DgroupId=javax.activation -DartifactId=activation -Dversion=1.0.2 -Dpackaging=jar
mvn install:install-file -Dfile=ojdbc14.jar -DgroupId=ojdbc -DartifactId=ojdbc -Dversion=14 -Dpackaging=jar

This fills the jars into your local repository (the poms are comming from ibiblio. Above urls are copied from the pom files,
if you get stuck have a look at them, there might be a hint in there).

Building on Unix
----------------

Run :

 $ sh build.sh

The output of the build will be in build.log.

To start continuum:

 $ cd continuum-plexus-application/target/plexus-test-runtime
 $ ./bin/plexus.sh

Building on Windows
-------------------

NOTE: You need to have the source tree at the root of your disk because continuum build use very long path
and Windows doesn't support path length greater than 250 characters.

Run :

 $ build.bat

The output of the build will be in build.log.

To start continuum:

 $ cd continuum-plexus-application\target\plexus-test-runtime
 $ .\bin\plexus.bat


Access continuum's web site
---------------------------

Go to:

  http://localhost:8080/continuum/servlet/continuum
