CP=
CP=$CP:$HOME/.m2/repository/org/apache/incubator/derby/derby/10.0.2.1/derby-10.0.2.1.jar
CP=$CP:$HOME/.m2/repository/org/apache/incubator/derby/derbynet/10.0.2.1/derbynet-10.0.2.1.jar

mkdir -p target/derby
java -cp $CP \
         -Dderby.system.home=target/derby \
         org.apache.derby.drda.NetworkServerControl start
