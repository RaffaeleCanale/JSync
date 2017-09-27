#!/bin/sh
destination="local-mvn-repo"

test -d "$destination" || mkdir "$destination"

mvn install:install-file -Dfile=lib/WXLibraries.jar \
                         -DgroupId=wxlibraries \
                         -DartifactId=com.wx.fx \
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath="$destination"

mvn dependency:purge-local-repository