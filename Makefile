skipTests = false
mvn_exec = mvn -Dmaven.test.skip=${skipTests}
current_dir = $(shell pwd)
agate_home = ${current_dir}/agate-server/target/agate_home
agate_log = ${agate_home}/logs

help:
	@echo
	@echo "Agate"
	@echo
	@echo "Available make targets:"
	@echo "  all         : Clean & install all modules"
	@echo "  clean       : Clean all modules"
	@echo "  do-install  : Install all modules"
	@echo "  core        : Install core module"
	@echo "  rest        : Install rest module"
	@echo
	@echo "  run         : Run webapp module"
	@echo "  debug       : Debug webapp module on port 8001"
	@echo "  grunt       : Start grunt on port 9000"
	@echo "  npm-install : Download all NodeJS dependencies"
	@echo
	@echo "  clear-log   : Delete agate.log from agate-server/target"
	@echo "  drop-mongo  : Drop MongoDB agate database"
	@echo
	@echo "  dependencies-tree   : Displays the dependency tree"
	@echo "  dependencies-update : Check for new dependency updates"
	@echo "  plugins-update      : Check for new plugin updates"
	@echo

all: clean do-install

clean:
	${mvn_exec} clean

do-install:
	${mvn_exec} install

core:
	cd agate-core && ${mvn_exec} install

rest:
	cd agate-rest && ${mvn_exec} install

angular:
	cd agate-angularjs-client && ${mvn_exec} install

run:
	cd agate-server && ${mvn_exec} spring-boot:run -DAGATE_HOME="${agate_home}" -DAGATE_LOG="${agate_log}"

debug:
	export MAVEN_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8001,suspend=n && \
	cd agate-server && ${mvn_exec} spring-boot:run -DAGATE_HOME="${agate_home}" -DAGATE_LOG="${agate_log}"

grunt:
	cd agate-angularjs-client && grunt server

npm-install:
	cd agate-angularjs-client && npm install

clear-log:
	rm -f agate-server/target/agate.log*

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