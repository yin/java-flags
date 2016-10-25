#!/bin/bash

if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    exit 1
fi

if git branch | grep '* master' &> /dev/null; then
    echo "checkout master first"
    exit 1
fi

mvn versions:set -DnewVersion=$1
mvn clean deploy -P release
mvn nexus-staging:release
git commit -am "Release $1"
git tag "v$1" -m "Release $1"
git push "v$1"

echo -e "<dependency>"
echo -e "\t<groupId>com.github.yin.flags</groupId>"
echo -e "\t<artifactId>java-flags</artifactId>"
echo -e "\t<version>$1-SNAPSHOT</version>"
echo -e "</dependency>"
