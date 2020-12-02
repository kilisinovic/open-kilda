import sysrepo as sr
import logging

# callback
def callback(session, module_name, xpath, event, request_id, private_data):
    logging.debug(f"call for {module_name}: {xpath}")
    # BEGIN service logic
    # END service logic
    return sr.SR_ERR_OK

module_name = "service-tree"
xpath = "/service-tree:services/bgp-service:bgp-service"
private_data = None
priority = 0
options = sr.SR_SUBSCR_UPDATE
xputil = sr.Xpath_Ctx()
logging.debug(f"initialized")
