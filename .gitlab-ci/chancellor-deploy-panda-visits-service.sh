#!/bin/sh
set -u # or set -o nounset
echo "$APP_ID"
#mvn deploy -pl spring-petclinic-visits-service | tee ./infra/service_mvn_deploy_out.log
#sed -n "s/^.*\[INFO\] Uploaded to .*: \(https:\/\/.*.jar\) \(.*\)$/\1/p" < ./infra/service_mvn_deploy_out.log > ./infra/service_nexus_artifact.url
export DTAP=dev
export BUSINESS_ID=atm
export ALIAS=use1
export PANDA_PROPERTIES_FILE="./infra/${APP_ID}_panda.json"
chancellor configuration panda deploy -d "$DTAP" -b "$BUSINESS_ID" -a "$APP_ID" -A "$ALIAS" -t "$CHANCELLOR_TOKEN" -c "$PANDA_PROPERTIES_FILE"
