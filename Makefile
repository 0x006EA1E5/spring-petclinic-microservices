DTAP=dev
BUSINESS_ID=atm
ALIAS=use1

.PHONY: mvn_deploy_module
mvn_deploy_module:
	@mvn deploy -pl $(MODULE) -B -am | tee ./infra/$(MODULE)_mvn_deploy_out.log \
	&& sed -n "s/^.*\[INFO\] Uploaded to .*: \(https:\/\/.*$(MODULE).*.jar\) \(.*\)$$/\1/p" < ./infra/$(MODULE)_mvn_deploy_out.log > ./infra/$(MODULE)_nexus_artifact.url

.PHONY: chancellor_deploy_module
chancellor_deploy_module:
	@chancellor service deploy --descriptor=./infra/chancellor-cli-templates/default-deployment.json -d $(DTAP) -b $(BUSINESS_ID) -a $$APP_ID -A $(ALIAS) -v RELEASE_VERSION=2.7.6-SNAPSHOT-$$(date +%s) -v ARTIFACT_URL=$$(cat ./infra/${MODULE}_nexus_artifact.url)



.PHONY: mvn_and_chancellor
mvn_and_chancellor:
	@make mvn_deploy_module && make chancellor_deploy_module

.PHONY: mvn_deploy_visits-service
mvn_deploy_visits-service:
	@make mvn_deploy_module MODULE=spring-petclinic-visits-service APP_ID=petclinicvisitsservice

.PHONY: chancellor_deploy_visits-service
chancellor_deploy_visits-service:
	@make chancellor_deploy_module MODULE=spring-petclinic-visits-service APP_ID=petclinicvisitsservice

.PHONY: deploy_visits-service
deploy_visits-service:
	@export MODULE=spring-petclinic-visits-service && export APP_ID=petclinicvisitsservice \
	&& make mvn_and_chancellor

.PHONY: deploy_vets-service
deploy_vets-service:
	@export MODULE=spring-petclinic-vets-service && export APP_ID=petclinicvetsservice \
	&& make mvn_and_chancellor

.PHONY: deploy_customers-service
deploy_customers-service:
	@export MODULE=spring-petclinic-customers-service && export APP_ID=petcliniccustomersservice \
	&& make mvn_and_chancellor

.PHONY: chancellor_deploy_frontend
chancellor_deploy_frontend:
	@make chancellor_deploy_module MODULE=spring-petclinic-api-gateway APP_ID=petclinic

.PHONY: deploy_frontend
deploy_frontend:
	@export MODULE=spring-petclinic-api-gateway && export APP_ID=petclinic \
	&& make mvn_and_chancellor
