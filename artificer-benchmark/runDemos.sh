#!/bin/bash

_WILDFLY_VERSION="8.2.0.Final"
_WILDFLY_NAME="wildfly-$_WILDFLY_VERSION"
_THIS_VERSION="0.1-SNAPSHOT"



stripTerminalEscapeCodes () {
  sed -r "s/\x1B\[([0-9]{1,2}(;[0-9]{1,2})?)?[mGKM]//g"
}

mkRegex () { 
  ( IFS="|" && echo "$*" ); 
}

filterOutput() {
  while read line; do
#    if ! [[ $(echo $line | stripTerminalEscapeCodes) =~ $excludeRegex ]] ; then
    if ! [[ $(echo $line) =~ $excludeRegex ]] ; then
      echo $line
    fi
  done
}


installWildfly() {
  if [[ ! -d ./target/$_WILDFLY_VERSION ]]; then
    echo "Titan is not installed, installing Titan + C*..."
    wget --quiet http://download.jboss.org/wildfly/$_WILDFLY_VERSION/$_WILDFLY_NAME.zip -P /tmp/ && unzip -q -d ./target/ /tmp/$_WILDFLY_NAME.zip # |& filterOutput
  fi
}

buildArtificer() {
  git clone --depth=1 https://github.com/ArtificerRepo/artificer.git /tmp
  pushd /tmp/artificer
  mvn clean -DskipTests install
  cd s-ramp-distro/assembly/target/
  unzip s-ramp-0.8.0-SNAPSHOT.zip
  cd s-ramp-0.8.0-SNAPSHOT
  ant -Dejb-jms.password=artificer1! -Ds-ramp-distro.choices.platform.jboss-wildfly-8=true -Ds-ramp-distro.choices.platform.jboss-wildfly-8.path=/tmp/$_WILDFLY_NAME install-jboss-wildfly-8-wrapper
  popd
}

startWildfly() {
  ./target/$__WILDFLY_NAME/bin/standalone.sh -c standalone-full.xml -Dkeycloak.import=/tmp/artificer/s-ramp-distro/assembly/src/main/resources/ROOT/artificer-realm.json & # |& filterOutput
  sleep 30
}

runBenchmarks() {
  echo "Running benchmark..."
    JARS=`find $PWD/target/lib/ | tr '\n' ':'`
    echo $JARS
    java -cp "$PWD/target/hawkular-inventory-titan-poc-$_THIS_VERSION.jar:$JARS" org.openjdk.jmh.Main $@ |& filterOutput
  echo -e "'\n\n*******\n\e[92mSUCCESS\e[0m\n*******\n"
}

main() {
  local excludeRegex=$(mkRegex \
    'ored because v'
  )
#    '(extracting\:|creating\:|inflating\:|Classpath\:)' \
#    'Downloading\: http' \
#    'Downloaded\: http' \

  set -f
  installWildfly
  buildArtificer
  startWildfly
  runBenchmarks $@
}

main $@
