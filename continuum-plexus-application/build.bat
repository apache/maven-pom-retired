@echo off

call m2 -DapplicationConfiguration=src/conf/application.xml -DconfigurationsDirectory=src/conf -DconfigurationProperties=app.properties -DapplicationName=continuum -DruntimeConfiguration=src/test/conf/test-runtime-configuration.xml -DruntimeConfigurationProperties=app.properties clean:clean plexus:app plexus:bundle-application plexus:test-runtime
