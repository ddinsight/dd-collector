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
import time
import re
import logfilters.header as header
from datatype import *

try:
    import log
except ImportError:
    import logging as log

__filterList__ = {}


def process(data):
    apatHdr = header.convert(data)
    if not apatHdr:
        log.error("APAT Header error")
        apatHdr = dict()

    result = dict()

    for k, v in data.iteritems():
        if k in apatHdr:
            continue

        # Check Pattern
        if isinstance(v, (str, unicode)):
            if re.match('^([1-9]\d+|\d)$', v):
                result[k] = IntegerType(v)
            elif re.match('^(\d+)[.,](\d+)$', v):
                result[k] = FloatType(v)
            else:
                result[k] = v
        else:
            result[k] = v

    result.update(apatHdr)

    return result
