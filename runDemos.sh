#!/bin/bash

_TITAN_VERSION="titan-all-0.4.4"
_THIS_VERSION="1.0-SNAPSHOT"

if [[ ! -d ./target/$_TITAN_VERSION ]]; then
    echo "Titan is not installed, installing Titan + C*..."
    wget http://s3.thinkaurelius.com/downloads/titan/$_TITAN_VERSION.zip -P /tmp/ && unzip -d ./target/ /tmp/$_TITAN_VERSION.zip
fi

./target/$_TITAN_VERSION/bin/cassandra
# wait for C* to start up, because the call above is an async call
sleep 60 

for demo in SimpleGraphDemo RandomGraphDemo RandomTreeDemo
do
    java -cp ./target/titan-poc-$_THIS_VERSION-jar-with-dependencies.jar org.rhq.$demo
done
