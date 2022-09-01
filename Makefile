skipTests = false
version=2.7-SNAPSHOT
mvn_exec = mvn -Dmaven.test.skip=${skipTests}
current_dir = $(shell pwd)
agate_home = ${current_dir}/agate_home
agate_log = ${agate_home}/logs

help:
	@echo
	@echo "Agate"
	@echo
	@echo "Available make targets:"
	@echo "  all         : Clean & install all modules"
	@echo "  clean       : Clean all modules"
	@echo "  install     : Install all modules"
	@echo "  core        : Install core module"
	@echo "  rest        : Install rest module"
	@echo
	@echo "  run         : Run webapp module"
	@echo "  debug       : Debug webapp module on port 8001"
	@echo "  grunt       : Start grunt on port 9000"
	@echo "  npm-install : Download all NodeJS dependencies"
	@echo
	@echo "  clear-log   : Delete logs from ${agate_log}"
	@echo "  drop-mongo  : Drop MongoDB agate database"
	@echo
	@echo "  dependencies-tree   : Displays the dependency tree"
	@echo "  dependencies-update : Check for new dependency updates"
	@echo "  plugins-update      : Check for new plugin updates"
	@echo

all: clean install

clean:
	${mvn_exec} clean

install:
	${mvn_exec} install

core:
	cd agate-core && ${mvn_exec} clean install

rest:
	cd agate-rest && ${mvn_exec} clean install

model: proto

proto:
	cd agate-web-model && ${mvn_exec} clean install

webapp:
	cd agate-webapp && ${mvn_exec} install

dist:
	cd agate-dist && ${mvn_exec} clean install

python:
	cd ../agate-python-client && ${mvn_exec} install

run:
	cd agate-webapp && \
	${mvn_exec} spring-boot:run -Pdev -DAGATE_HOME="${agate_home}" -DAGATE_LOG="${agate_log}"

run-prod:
	cd agate-webapp && \
	mvn install -Pci-build && \
	cd ../agate-dist && \
	mvn clean package && \
	cd target && \
	unzip agate-${version}-dist.zip && \
	mkdir -p ${agate_home} && \
	if [ ! -d ${agate_home}/conf ]; then cp -r agate-${version}/conf ${agate_home}/conf; fi && \
	export AGATE_HOME="${agate_home}" && \
	./agate-${version}/bin/agate

debug:
	cd agate-webapp && \
	${mvn_exec} spring-boot:run -Pdev -Dspring-boot.run.jvmArguments="-Xmx2G -agentlib:jdwp=transport=dt_socket,server=y,address=8001,suspend=n -Dspring.profiles.active=dev -DAGATE_HOME='${agate_home}' -DAGATE_LOG='${agate_log}'"

run-python:
	cd ../agate-python-client/target/agate-python/bin && \
	chmod +x ./scripts/agate && \
	export PYTHONPATH=${current_dir}/../agate-python-client/target/agate-python/bin && \
	./scripts/agate ${args}

grunt:
	cd agate-webapp && \
	grunt server

npm-install:
	cd agate-webapp && \
	npm install

clear-log:
	rm -rf ${agate_log}

log:
	tail -f ${agate_home}/logs/agate.log

restlog:
	tail -f ${agate_home}/logs/rest.log

drop-mongo:
	mongo agate --eval "db.dropDatabase()"

dependencies-tree:
	mvn dependency:tree

dependencies-update:
	mvn versions:display-dependency-updates

plugins-update:
	mvn versions:display-plugin-updates

templates:
	cd agate-webapp && cp -r src/main/resources/_templates/ target/classes/
