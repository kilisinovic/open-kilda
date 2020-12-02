import sysrepo as sr
import datetime
import logging

# callback
def callback(session, module_name, xpath, event, request_id, private_data):
    logging.debug(f"callback: running for {module_name}")
    if event != sr.SR_EV_UPDATE:
        return sr.SR_ERR_OK
    changes = session.get_changes_iter(f'/{module_name}:devices/device/config//.')
    while True:
        change = session.get_change_next(changes)
        if not change:
            logging.debug("callback: no more changes")
            break
        if change.oper() == sr.SR_OP_DELETED:
            logging.debug("callback: skipped due to delete")
            break
        change_xpath = change.new_val().xpath()
        logging.debug(f"callback: changes found {change_xpath}")
        device_name = xputil.key_value(change_xpath, 'device', 'name')
        item_xpath = f"/{module_name}:devices/device[name='{device_name}']/last-updated"
        current_time = datetime.datetime.utcnow().replace(tzinfo=datetime.timezone.utc).isoformat()
        session.discard_changes()
        session.set_item_str(item_xpath, current_time)
        logging.debug(f"callback: updated {item_xpath} {current_time}")
    return sr.SR_ERR_OK

module_name = 'device-tree'
xpath = f'/{module_name}:devices'
private_data = None
priority = 0
options = sr.SR_SUBSCR_UPDATE
xputil = sr.Xpath_Ctx()
