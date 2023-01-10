#!/bin/sh
#mvn deploy -pl spring-petclinic-visits-service | tee ./infra/service_mvn_deploy_out.log
#sed -n "s/^.*\[INFO\] Uploaded to .*: \(https:\/\/.*.jar\) \(.*\)$/\1/p" < ./infra/service_mvn_deploy_out.log > ./infra/service_nexus_artifact.url
export DTAP=dev
export BUSINESS_ID=atm
export APP_ID=petclinicvisitsservice
export ALIAS=use1
export RELEASE_VERSION=2.7.6-SNAPSHOT-$(date +%s)
export ARTIFACT_URL=https://nexus.atm.osp.tech/repository/maven-snapshots/org/springframework/samples/petclinic/visits/spring-petclinic-visits-service/2.7.6-SNAPSHOT/spring-petclinic-visits-service-2.7.6-20230110.150255-16.jar
chancellor service deploy --descriptor=./infra/chancellor-cli-templates/default-deployment.json -d $DTAP -b $BUSINESS_ID -a $APP_ID -A $ALIAS -v TIMESTAMP=$(date +%s)
