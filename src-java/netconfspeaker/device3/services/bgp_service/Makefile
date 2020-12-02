pyang = pyang
svcname = $(notdir $(realpath .))
pkgname = $(svcname).pkg

build: tree pack
test: 
	@$(pyang) --path ./yang:../../yang \
		../../yang/service-tree.yang yang/$(svcname).yang
tree:
	@echo -e "This is extended service-tree:\n------------------------------" && \
        $(pyang) --format tree --path ./yang:../../yang \
		../../yang/service-tree.yang yang/$(svcname).yang && echo
pack:
	@tar cf $(pkgname) --transform 's,^,services/,' \
		callbacks/ yang/ templates/ && \
		echo wrote `stat -c %s $(pkgname)` bytes to $(pkgname)
	
install:
	docker cp - netmeister:/ < $(pkgname)
	docker exec netmeister chown -Rv root: /services/$(svcname)
uninstall:
clean:
	rm -f $(pkgname)
