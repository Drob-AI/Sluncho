#!/usr/bin/env sh
cd sluncho
echo ${GATE_DIR:=/home/mihail/GATE_Developer_8.1}
GATE_APP_HOME=/home/mihail/Projects/cinusuidi/sluncho/resources/gate
java -Dsluncho.source.ipToHostname="http://localhost:4567/ip-to-username?ip=" -Dserver.port=4242 -Dgate.home="$GATE_DIR" -Dgate.astea.app.home=$GATE_APP_HOME -jar target/sluncho-alpha.jar
