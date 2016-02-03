#!/usr/bin/env sh
cd sluncho
mvn clean install
mvn eclipse:clean
mvn eclipse:eclipse
cd ..
./run.sh
GATE_DIR=/home/mkraeva/GATE_Developer_8.1
java -Dgate.home="$GATE_DIR" -jar sluncho-alpha-jar-with-dependencies.jar
java -cp target/sluncho-alpha-jar-with-dependencies.jar net.asteasolutions.cinusuidi.sluncho.App -Dgate.home="$GATE_DIR"
