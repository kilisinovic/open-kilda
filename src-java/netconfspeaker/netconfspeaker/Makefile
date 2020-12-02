
PROJECT_NAME=netconfspeaker
# Docker tag comes from the user
DOCKER_TAG=$(shell whoami | sed 's/[^[:alnum:]._-]\+/_/g')

all:
	$(MAKE) compile
	$(MAKE) build
	$(MAKE) clean
	
compile:
	@echo "Generating the fat jar"
	 mvn compile package

build:
	@echo "Generating a docker image"
	docker build -t $(PROJECT_NAME):$(DOCKER_TAG) .
	docker run --network host --add-host kafka.pendev:127.0.0.1 --name $(PROJECT_NAME) $(PROJECT_NAME):$(DOCKER_TAG)

clean:
	@echo "Removing the docker container"
	docker ps -aq --filter name=$(PROJECT_NAME) | xargs --no-run-if-empty docker rm -f

.PHONY: all compile build clean
