#!/bin/bash

_TITAN_VERSION="titan-all-0.4.4"
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


installTitan() {
  if [[ ! -d ./target/$_TITAN_VERSION ]]; then
    echo "Titan is not installed, installing Titan + C*..."
    wget --quiet http://s3.thinkaurelius.com/downloads/titan/$_TITAN_VERSION.zip -P /tmp/ && unzip -q -d ./target/ /tmp/$_TITAN_VERSION.zip # |& filterOutput
  fi
}

startCassandra() {
  ./target/$_TITAN_VERSION/bin/cassandra # |& filterOutput
  # wait for C* to start up, because the call above is an async call
  sleep 60
}

runBenchmarks() {
#  for demo in SimpleGraphDemo RandomGraphDemo RandomTreeDemo
#  do
#    echo -e "\n\n\nrunning demo $demo ...\n"
echo "Running benchmark..."
    JARS=`find $PWD/target/lib/ | tr '\n' ':'`
    echo $JARS
    java -cp "$PWD/target/hawkular-inventory-titan-poc-$_THIS_VERSION.jar:$JARS" org.openjdk.jmh.Main $@ |& filterOutput
#  done
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
  installTitan
  startCassandra
  runBenchmarks $@
}

main $@
