#!/bin/sh
set -u # or set -o nounset
echo "$MODULE" "$APP_ID"

export DTAP=dev
export BUSINESS_ID=atm
export ALIAS=use1
mvn deploy -pl ${MODULE} -B | tee ./infra/${MODULE}_mvn_deploy_out.log
sed -n "s/^.*\[INFO\] Uploaded to .*: \(https:\/\/.*.jar\) \(.*\)$/\1/p" < ./infra/${MODULE}_mvn_deploy_out.log > ./infra/${MODULE}_nexus_artifact.url
export TIMESTAMP=$(date +%s)
export RELEASE_VERSION=2.7.6-SNAPSHOT-${TIMESTAMP}
export ARTIFACT_URL=$(cat ./infra/${MODULE}_nexus_artifact.url)
echo $RELEASE_VERSION
echo $ARTIFACT_URL
chancellor service deploy --descriptor=./infra/chancellor-cli-templates/default-deployment.json -d $DTAP -b $BUSINESS_ID -a $APP_ID -A $ALIAS -v TIMESTAMP=${TIMESTAMP}
