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
import os
import time
import glob

try:
    import log
except ImportError:
    import logging as log

__filterList__ = {}

if __name__ == __package__:
    # scan filter.
    for name in glob.glob(os.path.dirname(__file__)+"/ver_*.py"):
        filterName = os.path.basename(name)[:-3]
        filterVer = tuple(map(int, filterName[4:].split('_')))
        __filterList__[filterVer] = filterName


def convert(data):
    ver = data.get('ver')

    if not ver:
        return None

    logFmtVer = tuple(map(int, ver.split('.')))
    modName = __filterList__.get(logFmtVer)

    if not modName:
        mappingFilter = max(filter(lambda (k,v): (k,v) if k <= logFmtVer else False, __filterList__.iteritems()), key=lambda x:x[0])
        modName = mappingFilter[1]
        log.info("Header Filter mapping : %s -> %s" % (logFmtVer, mappingFilter[0]))

    try:
        formatter = importlib.import_module(".%s" % modName, __package__)
    except Exception, e:
        log.error("Cannot found APAT Header filter for %s" % logFmtVer)
        return None
    finally:
        result = formatter.convert(data)

    return result
