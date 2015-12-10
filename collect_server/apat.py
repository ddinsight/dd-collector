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


import sys
import json
from messagehandler import parseData

from flask import Flask

from werkzeug.routing import BaseConverter
from flask import request, make_response
import filter
import time
import httplib
import traceback
from settings import ConfigHandler

conf = ConfigHandler()

reload(sys)
sys.setdefaultencoding('utf-8')

try:
    import log
except ImportError:
    import logging as log


class RegexConverter(BaseConverter):
    def __init__(self, url_map, *items):
        super(RegexConverter, self).__init__(url_map)
        self.regex = items[0]


LOG_TYPE = {
    'TE': 'test',
    'LT': 'livetest',
    'AT': 'aatlog',
    'AN': 'anslog',
}


def sendToFluent(name, data):
    if not data:
        return False

    tag = 'agentlog.%s' % name

    params = json.dumps(data)
    headers = {'Content-type': 'application/json'}
    try:
        host = conf.get('td-agent', 'host')
        port = conf.get('td-agent', 'port')
    except Exception, e:
        host = 'localhost'
        port = 8888
    conn = httplib.HTTPConnection(host, port)
    conn.request('POST', '/%s' % tag, params, headers)
    response = conn.getresponse()
    if response.status != 200:
        return False

    conn.close()

    return True


app = Flask(__name__)
app.url_map.converters['regex'] = RegexConverter

"""
Log Handler for general logs.
"""

@app.route("/<string(length=2):route>/<regex('.*'):param>", methods=['POST', 'GET'])
def defaultHandler(route, param):
    if route not in LOG_TYPE:
        return make_response("URL ERROR", 404)

    if route in ('LT', 'TE'):
        return "APAT LOG SERVER ...WORKING..."

    uri = '/%s/%s' % (route, param)

    logType = LOG_TYPE[route]

    # get URL parameters as raw string
    if request.method == 'POST':
        rawParamData = request.data
    else:
        rawParamData = request.query_string

    try:
        apatLog = parseData(param, request)
    except Exception, e:
        log.error("%s Parse Error: %s [param]%s [request]%s" % (logType, e, param, rawParamData))
        return make_response("PARSE ERROR", 403)

    if apatLog is not None:
        try:
            filteredLog = filter.filtering(route, apatLog, dropFutureLog=False)
            if not filteredLog:
                return make_response("%s 200OK" % route, 200)

            filteredLog['log_type'] = logType
            filteredLog['insTs'] = time.time()
            filteredLog['apat_req_uri'] = uri
            filteredLog['apat_uri_params'] = rawParamData

            if not sendToFluent(logType, filteredLog):
                return make_response("%s : write log fail" % logType, 500)
        except Exception, e:
            log.error("%s write error: %s [method]%s [param]%s [request]%s" % (logType, e, request.method, param, rawParamData))
            log.error(traceback.format_exception(*sys.exc_info()))
            return make_response("%s 500ERROR" % route, 500)

    return make_response("%s 200OK" % route, 200)
