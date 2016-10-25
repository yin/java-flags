#!/bin/bash

if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    echo 1
fi

mvn versions:set -DnewVersion=$1
mvn clean deploy -P release
mvn nexus-staging:release

echo -e "<dependency>"
echo -e "\t<groupdId>com.github.yin.flags</groupId>"
echo -e "\t<artifactId>java-flags</artifactId>"
echo -e "\t<version>$1-SNAPSHOT</version>"
echo -e "</dependency>"
