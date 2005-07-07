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
