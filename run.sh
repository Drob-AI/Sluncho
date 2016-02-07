#!/usr/bin/env sh
cd resources/gate
GATE_APP_HOME=`pwd`
cd ../../sluncho
echo your gate is : $GATE_DIR
echo your wordnet db is : $WORDNET_DB
echo gate app home is : $GATE_APP_HOME
java -Dgate.home="$GATE_DIR" -Dgate.astea.app.home=$GATE_APP_HOME -Dwordnet.database.dir="$WORDNET_DB" -jar target/sluncho-alpha.jar
