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


import importlib
import datetime
import time
import re

try:
    import log
except ImportError:
    import logging as log


convertTbl = {'`': '0',
              'a': '1',
              'b': '2',
              'c': '3',
              'd': '4',
              'e': '5',
              'f': '6',
              'g': '7',
              'h': '8',
              'i': '9',
              'j': ',',
              'k': '.'}


def filtering(logType, data, dropFutureLog=True):
    logType = str(logType).lower()

    # Backup raw data id of MongoDB
    try:
        id = data.pop('_id')
    except Exception, e:
        id = None

    # Check alphabet time (for ar-AE ?)
    try:
        if not re.match('^([0-9,.]+)$', data.get('tTM', '0')):
            tTM = ''.join([convertTbl.get(v, v) for v in data['tTM']])
            sr = ''.join([convertTbl.get(v, v) for v in data['sr']])

            data['tTM'] = tTM
            data['sr'] = sr
    except Exception, e:
        log.error(e)

    # Import log filter and filtering
    try:
        filter = importlib.import_module("logfilters.%s" % logType, __package__)
    except ImportError:
        filter = importlib.import_module("logfilters.misc", __package__)

    result = filter.process(data)
    if len(data) > 0 and not result:
        log.error("Filtering error - %s (%s). use default filter" % (logType, data.get('deviceID')))
        filter = importlib.import_module("logfilters.misc", __package__)
        result = filter.process(data)

    if result:
        # check timestamp
        timestamp = result.get('tTM', 0)
        current = time.time()
        if timestamp >= (current + 3600):
            log.warn("Invalid Timestamp. future log, drop data - deviceID : %s, sID : %s, tTM : %s (current : %s)" % (result.get('deviceID'), result.get('sID'), datetime.datetime.fromtimestamp(timestamp), datetime.datetime.fromtimestamp(current)))
            return None

        if id:
            result['_id'] = id

    return result
