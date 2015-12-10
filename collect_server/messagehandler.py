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


import json
import urllib
import urlparse
import traceback
import sys
import log
import re


ESCAPE_PATTERN = re.compile('%|{')


def prepareParams(request):
    if request.method == 'POST':
        params = dict(urlparse.parse_qsl(request.data))
    elif request.method == 'GET':
        params = dict(request.args.items())
    else:
        return None

    return params if len(params) > 0 else None


def loadJson(data):
    try:
        result = json.loads(urllib.unquote_plus(data))
    except Exception, e:
        result = json.loads(data)

    return result


def parseData(reqUrl, request):

    if reqUrl.strip() == '':
        return None

    # check ecrypt mode
    if reqUrl.find('enc.apg') < 0:
        encMode = False
    else:
        encMode = True

    parsedData = prepareParams(request)

    try:
        if parsedData:
            if 'eData' in parsedData.keys():
                if encMode and not re.search(ESCAPE_PATTERN, parsedData['eData']):
                    log.error("Encryption mode is not yet supported!")
                    extData = {}
                else:
                    extData = parsedData['eData']

                if len(extData) == 0:
                    parsedData['eData'] = "truncated"
                else:
                    extDataDict = loadJson(extData)
                    parsedData['eData'] = True
                    parsedData.update(extDataDict)
            else:
                parsedData['eData'] = False

            if 'pdata' in parsedData.keys():
                log.debug("pdata %s" % parsedData['pdata'])
                extPdata = loadJson(parsedData['pdata'])
                parsedData['pdata'] = True
                parsedData.update(extPdata)
    except Exception, e:
        log.error("POST parsing ERROR: %s" % e)
        log.error(parsedData)
        log.error('\n'.join(traceback.format_exception(*sys.exc_info())))

    if parsedData != None and parsedData.has_key('rip') == False and request.headers.has_key('X-Forwarded-For'):
        parsedData['rip'] = request.headers['X-Forwarded-For']

    log.debug("last parsedData \n")
    log.debug(parsedData)

    return parsedData

