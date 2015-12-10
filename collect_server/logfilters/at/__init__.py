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


import format
import logfilters.header as header

try:
    import log
except ImportError:
    import logging as log

__all__ = ['format']


def process(data):
    apatHdr = header.convert(data)
    if not apatHdr:
        log.error("APAT Header error")
        return None

    msgBody = format.convert(data)
    if not msgBody:
        log.warn("Cannot find message body")
        return None

    hdrReport = apatHdr.get("__validityReport", {})
    bodyReport = msgBody.get("__validityReport", {})
    bodyReport.update(hdrReport)

    result = dict(apatHdr)
    result.update(msgBody)

    tmp = data.copy()
    for key in result.keys():
        try:
            tmp.pop(key)
        except Exception, e:
            pass

    if len(tmp) > 0:
        log.warn("AT : not filtered data - (%s) %s" % (apatHdr.get('deviceID'), tmp))
        for k, v in tmp.iteritems():
            bodyReport[k] = [(v, "UNKNOWN_FIELD", "unknown field")]

    if len(bodyReport) > 0:
        result["__validityReport"] = bodyReport
    elif result.has_key("__validityReport"):
        result.pop("__validityReport")

    return result

