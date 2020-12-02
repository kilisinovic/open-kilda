yangfiles = $(wildcard */*.yang)
datafiles = $(wildcard examples/xml/edit-*.xml)
.PHONY: $(yangfiles) $(datafiles)

all: kill novol build vol run
volreset: novol vol
purge: kill novol
rebirth: kill novol vol run

# vars
image = netmeister
name = netmeister
host = $(name)
vol1 = $(name):/var/lib/sysrepo
fwd = 2022:830/tcp
runopts = --name $(name) --hostname $(host) \
	--rm -it --init -v $(vol1) -p $(fwd) -e LOGLEVEL=DEBUG
execopts = -it -w /srv

# JSON DATA
examples/json/%.json:
	yanglint -t edit -f json -p yang -o $@ yang/* examples/xml/$*.xml
jsondata: $(datafiles:examples/xml/%.xml=examples/json/%.json)

# JSON SCHEMA
json/%.json:
	yanglint -p yang -f jsons -o $@ yang/$*.yang
jsonschema: $(yangfiles:yang/%.yang=json/%.json)
#	yanglint -p yang -f jsons -o json/device-tree-aug.json yang/{device-tree,router-config}.yang

### VALIDATE
$(yangfiles): fmt=tree
$(yangfiles): jtox=jtox
$(yangfiles): tree=tree
$(yangfiles): xml=sample-xml-skeleton
$(yangfiles):
	pyang -p yang -f $($(fmt)) $@

### COPY
docker-copy:
	docker cp $(from) $(name):$(temp)
### GET
get: ds=running
get: xarg=$(if $(xpath),--xpath $(xpath),)
get:
	docker exec $(name) \
		sysrepocfg --export -d $(ds) $(xarg)
### EDIT
edit: ds=running
edit: temp=/tmp/data.xml
edit: docker-copy
	docker exec $(name) \
		sysrepocfg --edit=$(temp) -d $(ds)
### IMPORT
import: ds=running temp=/tmp/data.xml
import: docker-copy
	docker exec $(name) \
		sysrepocfg --import=$(temp) -d $(ds)
### INSTALL
install: temp=/tmp/$(notdir $(from))
install: docker-copy
	docker exec $(name) \
		sysrepoctl --install $(temp)
### UPDATE
update: temp=/tmp/$(notdir $(from))
update: docker-copy
	docker exec $(name) \
		sysrepoctl --update $(temp)
### RUN
run:
	docker run $(runopts) $(image) $(cmd)
start:
	docker run $(runopts) -d $(image)
devshell:
	docker run $(runopts) \
		--volume ${PWD}:/srv:rw \
		--workdir /srv \
		-e LOGLEVEL=DEBUG -e PAGER=less \
		--entrypoint ash $(image) scripts/install-dev-packages.sh
### EXEC
shell:
	docker exec $(execopts) $(name) ash
cli:
	docker exec $(execopts) $(name) netopeer2-cli
stop:
	docker stop $(name)
kill:
	docker kill $(name) &>/dev/null || true
build:
	docker build -t $(name)  .

### VOLUME
vol:
	docker volume create $(name)
novol:
	docker volume rm $(name) || true
