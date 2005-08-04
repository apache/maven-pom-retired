#!/bin/bash


echo "to connect to the database write: connect 'jdbc:derby:target/plexus-test-runtime/apps/continuum/database';"
exec java -cp ~/repository/org/apache/incubator/derby/derby/10.0.2.1/derby-10.0.2.1.jar:/home/trygvis/repository/org/apache/incubator/derby/derbytools/10.0.2.1/derbytools-10.0.2.1.jar org.apache.derby.impl.tools.ij.Main


