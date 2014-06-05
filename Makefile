skipTests = false
mvn_exec = mvn -Dmaven.test.skip=${skipTests}
current_dir = $(shell pwd)
agate_home = ${current_dir}/agate-webapp/target/agate_home
agate_log = ${agate_home}/logs

help:
	@echo
	@echo "Mica Server"
	@echo
	@echo "Available make targets:"
	@echo "  all         : Clean & install all modules"
	@echo "  clean       : Clean all modules"
	@echo "  install     : Install all modules"
	@echo "  core        : Install core module"
	@echo "  search      : Install search module"
	@echo "  rest        : Install rest module"
	@echo
	@echo "  run         : Run webapp module"
	@echo "  debug       : Debug webapp module on port 8000"
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
	cd agate-core && ${mvn_exec} install

rest:
	cd agate-rest && ${mvn_exec} install

run:
	cd agate-webapp && \
	${mvn_exec} spring-boot:run -Pdev -DAGATE_HOME="${agate_home}" -DAGATE_LOG="${agate_log}"

run-prod:
	cd agate-webapp && \
	mvn package -Pci-build && \
	java -Dloader.path=src/main/conf,target/agate-webapp-0.1-SNAPSHOT.jar -DAGATE_HOME="${agate_home}" -DAGATE_LOG="${agate_log}" -jar target/agate-webapp-0.1-SNAPSHOT.jar

debug:
	export MAVEN_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n && \
	cd agate-webapp && \
	${mvn_exec} spring-boot:run -Pdev -Dspring.profiles.active=dev -DAGATE_HOME="${agate_home}" -DAGATE_LOG="${agate_log}"

grunt:
	cd agate-webapp && \
	grunt server

npm-install:
	cd agate-webapp && \
	npm install

clear-log:
	rm -rf ${agate_log}

drop-mongo:
	mongo agate --eval "db.dropDatabase()"

dependencies-tree:
	mvn dependency:tree

dependencies-update:
	mvn versions:display-dependency-updates

plugins-update:
	mvn versions:display-plugin-updates

keystore:
	rm -f keystore.p12
	keytool -genkey -alias tomcat -keystore keystore.p12 -storepass changeit -validity 365 -keyalg RSA -keysize 2048 -storetype pkcs12 -dname "CN=Agate, O=Maelstrom, OU=OBiBa, L=Montreal, ST=Quebec, C=CA"
	@echo "Generated keystore file:" `pwd`/keystore.p12