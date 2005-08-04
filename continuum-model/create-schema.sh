#!/bin/bash

source db-settings

java \
  -Djavax.jdo.option.ConnectionDriverName=$driver \
  -Djavax.jdo.option.ConnectionURL=$url \
  -Djavax.jdo.option.ConnectionUserName=$user \
  -Djavax.jdo.option.ConnectionPassword=$pass \
  -cp $HOME/.m2/repository/log4j/log4j/1.2.8/log4j-1.2.8.jar:\
$HOME/.m2/repository/jdo/jdo/2.0-beta/jdo-2.0-beta.jar:\
$HOME/.m2/repository/jpox/jpox/1.1.0-beta-4-c1/jpox-1.1.0-beta-4-c1.jar:\
$HOME/.m2/repository/jpox/jpox-enhancer/1.1.0-beta-4-c1/jpox-enhancer-1.1.0-beta-4-c1.jar:\
$HOME/.m2/repository/hsqldb/hsqldb/1.7.3.3/hsqldb-1.7.3.3.jar:\
$HOME/.m2/repository/org/apache/incubator/derby/derby/10.0.2.1/derby-10.0.2.1.jar:\
src/main/resources/:\
target/classes \
  org.jpox.SchemaTool \
  -create \
  src/main/resources/META-INF/package.jdo
