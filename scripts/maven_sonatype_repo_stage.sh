#!/bin/bash

if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    exit 1
fi

mvn versions:set -DnewVersion=$1-SNAPSHOT
mvn clean deploy

echo -e "<dependency>"
echo -e "\t<groupdId>com.github.yin.flags</groupId>"
echo -e "\t<artifactId>java-flags</artifactId>"
echo -e "\t<version>$1-SNAPSHOT</version>"
echo -e "</dependency>"
