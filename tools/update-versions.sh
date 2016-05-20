#!/usr/bin/env bash

excludes=javax.servlet:javax.servlet-api # we're using some kind of legacy version here

mvn versions:update-parent versions:update-properties versions:use-latest-releases -Dexcludes=${excludes} -DgenerateBackupPoms=false -Dmaven.version.rules=file://\${project.basedir}/config/version-rules.xml