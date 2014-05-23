skipTests = false
mvn_exec = mvn -Dmaven.test.skip=${skipTests}
current_dir = $(shell pwd)
agate_home = target/agate_home

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
	@echo "  clear-log   : Delete agate.log from agate-webapp/target"
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

run:
	cd agate-webapp && ${mvn_exec} spring-boot:run -DAGATE_HOME="${agate_home}"

debug:
	export MAVEN_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8001,suspend=n && \
	cd agate-webapp && ${mvn_exec} spring-boot:run -DAGATE_HOME="${agate_home}"

grunt:
	cd agate-webapp && grunt server

npm-install:
	cd agate-webapp && npm install

clear-log:
	rm -f agate-webapp/target/agate.log*

drop-mongo:
	mongo agate --eval "db.dropDatabase()"

dependencies-tree:
	mvn dependency:tree

dependencies-update:
	mvn versions:display-dependency-updates

plugins-update:
	mvn versions:display-plugin-updates