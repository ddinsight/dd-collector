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

"""
field = {"fmt" : <field formatter>
        "cond" : [<type>, <condition>] --> dep 참고
        "opt" : 0 or 1 # Optional field?
        "dep" : {<field name>:[<type>, <condition>], }
                # opt == 1일때, 이 필드의 부모 필드. 없으면 생략
                # type -> type, val, regex, range, present
                    "type" : "cond":["type", IntegerType]
                    or "cond":["val", 10]
                    or "cond":["regex", "^WIFI|^mobile"]
                    or "cond":["range", 0, 100] --> min/max
                    or "cond":["present", boolean] --> True/False
                    or "cond":["in", [val1, val2, val3...]] --> 값이 리스트에 포함되는지

}

순서
1. fmt() 함수 호출하여 값 변환
2. cond 조건에 따라 값 체크
3. opt 필드 체크
4. dep 필드에 명시된 필드들의 값을 체크하여 체크

결과값 저장
_validityResult = {<fieldName> : [<val>, <result code>, <msg>]}

result code :
    - 0 : ok
    - 10 : field value error
    - 20 : mandatory field error (필수 필드인데 없을때)
    - 30 : Dependency error
    - 40 : unknown field

"""


@Container(report=True)
class LogFormatter(object):
    seqNum = {"fmt":IntegerType}
    ansLogVer = {"fmt":StringType, "cond":["regex", "^(\d+)\.(\d+)$|^((\d+)\.){2}(\d+)$"]}
    ansVer = {"fmt":StringType}
    agentAllowAns = {"fmt":BooleanType}
    initWifi = {"fmt":BooleanType}
    eventTime = {"fmt":IntegerType}
    playAppPackageName = {"fmt":StringType}
    parentAppName = {"fmt":StringType, "opt":1}
    netType = {"fmt":StringType}
    tryTime = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    startTime = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    endTime = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    startCode = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    exitCode = {"fmt":IntegerType}
    NWcharac = {"fmt":StringType, "opt":1, "dep":{"estTP":["present", False]}}
    myfiOn = {"fmt":IntegerType, "opt":1, "cond":["in", [0, 1]]}
    myfiAP = {"fmt":IntegerType, "opt":1, "cond":["in", [0, 1]]}
    cellId = {"fmt":StringType, "cond":["regex", "^((\d+)_){2}(\d)+$"]}
    mobileIP = {"fmt":StringType, "opt":1, "cond":["regex", "^((\d+).){3}(\d+)$"]}
    ssid = {"fmt":UnicodeType, "opt":1, "dep":{"netType":["val", "WIFI"]}}
    bssid = {"fmt":StringType, "cond":["regex", "^([a-fA-F0-9]{2}:){5}[a-fA-F0-9]{2}$"], "opt":1, "dep":{"netType":["val", "WIFI"]}}
    wifiIP = {"fmt":StringType, "cond":["regex", "^((\d+).){3}(\d+)$"], "opt":1, "dep":{"netType":["val", "WIFI"]}}
    enteringRssi = {"fmt":IntegerType, "cond":["range", -200, 0], "opt":1, "dep":{"netType":["val", "WIFI"]}}
    exitingRssi = {"fmt":IntegerType, "cond":["range", -200, 0], "opt":1, "dep":{"netType":["val", "WIFI"]}}
    lnkspd = {'fmt':IntegerType, "opt":1, "dep":{"netType":["val", "WIFI"]}}
    apfreq = {'fmt':IntegerType, "opt":1, "dep":{"netType":["val", "WIFI"]}}
    minGoodRssi = {"fmt":IntegerType, "cond":["range", -200, 0], "opt":1, "dep":{"netType":["val", "WIFI"]}}
    maxBadRssi = {"fmt":IntegerType, "cond":["range", -200, 0], "opt":1, "dep":{"netType":["val", "WIFI"]}}
    activeDownload = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    totalDownload = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    avgTP = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    avgLatency = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    maxTP = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    duration = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    activeTime = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    playTime = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    avgReqTP = {"fmt":IntegerType, "opt":1}
    chargeTime = {"fmt":IntegerType, "opt":1, "dep":{"estTP":["present", False]}}
    chargeLevel = {"fmt":IntegerType, "cond":["range", 0, 100], "opt":1, "dep":{"estTP":["present", False]}}
    estTP = {"fmt":IntegerType, "opt":1}
    estLatency = {"fmt":IntegerType, "opt":1}
    phoneNumber = {"fmt":StringType, "cond":["regex", "^\+?([0-9]{12})$|^([0-9]{11})$|^([0-9]{10})$"], "opt":1}
    phoneAlias = {"fmt":StringType, "opt":1}
    lat = {"fmt":FloatType, "cond":["range", -90, 90], "opt":1}
    lng = {"fmt":FloatType, "cond":["range", -180, 180], "opt":1}
    deviceLog = {"fmt":UnicodeType, "opt":1}


def convert(data):
    result = LogFormatter(data)
    return result
