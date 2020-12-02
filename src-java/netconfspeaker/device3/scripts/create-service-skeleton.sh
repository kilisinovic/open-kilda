#!/bin/bash
# service-name: yang module name
# entry-point: json xpath notation
#
# For example: following arugments:
#   create-service-skeleton.sh dns-service config/nameservers 
#
# Would create a subscriber here:
#   /dns-service:config/nameservers
#
set -e 
[[ -n $2 ]] || { echo "usage: $0 <service-name> <entry-point>" && exit 0; }

name=${1}
prefix=${name/-/}

sleep 0.5
mkdir -p ${name}/{yang,callbacks,templates}

cat > ${name}/yang/${name}.yang <<EOF
module ${name} {

  namespace "http://example.org/yang/${name}";
  prefix ${prefix};

  import service-tree {
    prefix svc3;
  }

  organization
    "ACME Inc.";
  contact
    "acme-developers@acme.org";
  description
    "YANG datamodel for ACME Service";
  revision $(date +%F){
    description
        "Initial revision.";
    reference
        "http://example.org/yang/${name}/revisions";
  }

  augment /svc3:services {
    description
      "Extends the /services tree.";
    list ${name} {
      key "name";
      description
        "Service model for ${name}.";
      leaf name {
        type string;
        description
          "Service Instance Name";
      }
    }
  }
}
EOF

cat > ${name}/callbacks/${prefix}.py <<EOF
import sysrepo as sr
import logging

# callback
def callback(session, module_name, xpath, event, request_id, private_data):
    logging.debug("call for {module_name}: {xpath}")
    # BEGIN service logic
    # END service logic
    return sr.SR_ERR_OK

module_name = "service-tree"
xpath = "/service-tree:services/${name}:${entry}"
private_data = None
priority = 0
options = sr.SR_SUBSCR_UPDATE
xputil = sr.Xpath_Ctx()
logging.info("initialized")
EOF

cat > ${name}/Makefile <<EOF
pyang = pyang
svcname = \$(notdir \$(realpath .))
pkgname = \$(svcname).pkg

build: tree pack
test: 
	@\$(pyang) --path ./yang:../../yang \\
		../../yang/service-tree.yang yang/\$(svcname).yang
tree:
	@echo -e "This is extended service-tree:\n------------------------------" && \\
        \$(pyang) --format tree --path ./yang:../../yang \\
		../../yang/service-tree.yang yang/\$(svcname).yang && echo
pack:
	@tar cf \$(pkgname) --transform 's,^,services/,' \\
		callbacks/ yang/ templates/ && \\
		echo wrote \`stat -c %s \$(pkgname)\` bytes to \$(pkgname)
	
install:
	docker cp - netmeister:/ < \$(pkgname)
	docker exec netmeister chown -Rv root: /services/\$(svcname)
uninstall:
clean:
	rm -f \$(pkgname)
EOF

echo
echo NEW service created!
echo --------------------
echo "  service model:      ${name}/yang/${name}.yang"
echo "  service logic:      ${name}/callbacks/${prefix}.py"
echo "  service templates:  ${name}/templates/${name}.xml"
echo "  service makefile:   ${name}/Makefile"
echo
