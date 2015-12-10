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


from container import Container
from datatype import *


class BaseFormatter(object):
    seqnum = IntegerType
    nwLogVersion = StringType
    version = StringType
    ansOn = BooleanType
    initWifi = BooleanType
    type = IntegerType
    startTime = IntegerType
    connectTime = IntegerType
    disconnectTime = IntegerType
    reason = IntegerType
    cell_id = StringType
    mip = StringType
    ssid = UnicodeType
    bssid = StringType
    ipAddress = StringType
    enteringRssi = IntegerType
    exitingRssi = IntegerType
    minGoodRssi = IntegerType
    maxBadRssi = IntegerType
    activeDownload = IntegerType
    totalDownload = IntegerType
    avgTP = IntegerType
    avgLatency = IntegerType
    maxTP = IntegerType
    duration = IntegerType
    activeTime = IntegerType
    playTime = IntegerType
    refresh = BooleanType
    phoneModel = StringType
    phoneAlias = StringType
    ts = IntegerType
    phoneNumber = StringType
    uuid = StringType
    longitude = FloatType
    latitude = FloatType


def convert(data):
    @Container
    class Dummy(BaseFormatter):
        pass

    result = Dummy(data)
    return result
