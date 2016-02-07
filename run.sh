#!/usr/bin/env sh
cd sluncho
echo your gate is here: $GATE_DIR
GATE_APP_HOME=/home/mihail/Projects/Sluncho/resources/gate
java -Dgate.plugins.home="$GATE_DIR/plugins" -Dgate.home="$GATE_DIR" -Dgate.astea.app.home=$GATE_APP_HOME -jar target/sluncho-alpha-jar-with-dependencies.jar
