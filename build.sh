should_sign=$1

if [ $should_sign = "--sign" ]; then
    cd instrumentation/ &&
        mvn clean package &&
        cd .. &&
        cp instrumentation/target/instrumentation-1.0-SNAPSHOT.jar epsagon/src/main/resources/agent.jar &&
        sync &&
        cd epsagon/ &&
        mvn clean package &&
        cd ..
else
    cd instrumentation/ &&
        mvn clean install &&
        cd .. &&
        cp instrumentation/target/instrumentation-1.0-SNAPSHOT.jar epsagon/src/main/resources/agent.jar &&
        sync &&
        cd epsagon/ &&
        mvn clean install -P local-build &&
        cd ..
fi
