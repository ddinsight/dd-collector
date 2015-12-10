# -*- coding: utf-8 -*-
#
#  Copyright 2015 AirPlug Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


import os
import logging
import logging.handlers
import time
from settings import ConfigHandler


__logger = None
__debuglogger = None

INFO = logging.INFO
DEBUG = logging.DEBUG
WARN = logging.WARNING
ERROR = logging.ERROR
FATAL = logging.FATAL

# init. default logger
info = logging.info
debug = logging.debug
warn = logging.warn
error = logging.error
fatal = logging.fatal

conf = ConfigHandler()


def __initLogger(provider, filename, level):
    '''
    Initial logger
    '''
    logger = logging.getLogger(provider)

    logger.setLevel(level)
    while len(logger.handlers) > 0:
        logger.removeHandler(logger.handlers[0])

    # Set formatter
    try:
        format = '%(asctime)s %(module)s %(lineno)d %(levelname)s %(message)s'
        formatter = logging.Formatter(format)
    except Exception, e:
        print e

    # Set file logger
    try:
        (path, tmp, name) = filename.rpartition('/')
        try:
            os.makedirs(path, 0755)
        except Exception, e:
            pass

        # use logrotate
        fh = logging.FileHandler(filename, delay=False)
        fh.setLevel(level)
    except Exception, e:
        fh = None
        print e

    if fh:
        fh.setFormatter(formatter)
        logger.addHandler(fh)

    # Set System logger
    try:
        # sys.stdout, sys.stderr..
        ch = logging.StreamHandler()
        ch.setLevel(level)
    except Exception, e:
        ch = None
        print e

    if ch:
        ch.setFormatter(formatter)
        logger.addHandler(ch)

    global info, debug, warn, error, fatal
    # Register log function
    info = logger.info
    debug = logger.debug
    warn = logger.warn
    error = logger.error
    fatal = logger.fatal

    return logger


def __init__():
    global __logger, __debuglogger

    if __logger == None:
        try:
            appname = conf.get('log', 'name')
            filename = conf.get('log', 'file')
            loglevel = conf.get('log', 'level')
            __logger = __initLogger(appname, filename, loglevel)
        except Exception, e:
            __logger = __initLogger("APATLogServer", "apatlog.log", INFO)

__init__()
