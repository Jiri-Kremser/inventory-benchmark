#!/bin/bash

mkRegex () {
  ( IFS="|" && echo "$*" );
}

filterOutput() {
  while read line; do
    if ! [[ $(echo $line) =~ $excludeRegex ]] ; then
      echo $line
    fi
  done
}

buildArtificer() {
  FOO=$PWD
  git clone --quiet --depth=1 https://github.com/ArtificerRepo/artificer.git /tmp/artificer
  pushd /tmp/artificer
  mvn -s $FOO/.travis.maven.settings.xml -DskipTests install |& filterOutput
  popd
}

buildThis() {
  mvn -s .travis.maven.settings.xml -DskipTests install |& filterOutput
}

main() {
  local excludeRegex=$(mkRegex \
    ' KB' \
    '\[INFO\]' \
    'Downloading\: http' \
    'Downloaded\: http'
  )
#    '(extracting\:|creating\:|inflating\:|Classpath\:)' \
#    'Compressing objects' \
#    'Resolving deltas' \

  set -f
  jdk_switcher use oraclejdk7
  buildArtificer

  jdk_switcher use oraclejdk8
  buildThis
}

main $@
