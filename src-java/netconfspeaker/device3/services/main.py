#!/usr/bin/env python3
import sysrepo as sr
import os
import logging
import glob
import importlib

# log
logging.basicConfig(
    format='%(asctime)s [%(levelname)s] services.%(module)s %(message)s',
    datefmt='%F %T', level=logging.DEBUG)
# debug
if os.environ.get('LOGLEVEL') == "DEBUG":
    log = sr.Logs()
    log.set_stderr(sr.SR_LL_DBG)

# init
connection = sr.Connection()
session = sr.Session(connection)
modinfo = connection.get_module_info()
logging.info('initialized')

# suscribers
subscriber = sr.Subscribe(session)
maindir = os.path.dirname(__file__)
for path in glob.glob(f'{maindir}/*'):
    # skip files
    if os.path.isfile(path):
        continue
    dirname = os.path.basename(path)

    # load models
    for path in glob.glob(f'{maindir}/{dirname}/yang/*.yang'):
        yangmod = os.path.splitext(os.path.basename(path))[0]
        if modinfo.find_path(f'module[name="{yangmod}"]').size() < 1:
            logging.debug(f'installing model {yangmod}')
            connection.install_module(path, os.path.dirname(path), '')
        logging.debug(f'skipping installed {yangmod}')

    # register callbacks
    for path in glob.glob(f'{maindir}/{dirname}/callbacks/*.py'):
        pymod = os.path.splitext(os.path.basename(path))[0]
        logging.debug(f'importing {dirname}.callbacks.{pymod}')
        pylib = importlib.import_module(f'{dirname}.callbacks.{pymod}')
    
        subscriber.module_change_subscribe(
            pylib.module_name, pylib.callback, pylib.xpath,
            pylib.private_data, pylib.priority, pylib.options)
        logging.info(f'monitoring {pylib.module_name} {pylib.xpath}')

sr.global_loop()

# cleanup
session.session_stop()
connection=None
logging.debug(f'program exit')
