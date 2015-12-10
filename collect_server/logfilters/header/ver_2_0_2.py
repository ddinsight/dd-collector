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


from logfilters.container import Container
from base import BaseHeader
from datatype import *


@Container(report=True)
class APATHeader(BaseHeader):
    ver = {"fmt":StringType, "cond":["regex", "^(\d+)\.(\d+)$|^((\d+)\.){2}(\d+)$"]}
    verCode = {"fmt":IntegerType}
    pkgName = {"fmt":StringType}
    locale = {"fmt":StringType}
    vID = {"fmt":StringType}
    sr = {"fmt":StringType}
    osType = {"fmt":StringType}
    osVer = {"fmt":StringType}
    sdkVer = {"fmt":StringType, "opt":1, "dep":{"osType":["val", "A"]}}
    sID = {"fmt":IntegerType}
    sCnt = {"fmt":IntegerType}
    tTM = {"fmt":FloatType}
    deviceID = {"fmt":StringType}
    model = {"fmt":StringType}
    brand = {"fmt":StringType}
    numTotalHits = {"fmt":IntegerType}
    numStoredHits = {"fmt":IntegerType}
    evtSyncID = {"fmt":IntegerType, "opt":1}
    nType = {"fmt":IntegerType}
    bFailOver = {"fmt":BooleanType, "opt":1}
    eData = {"fmt":RawType}
    pdata = {"fmt":RawType, "opt":1}
    pnet = {"fmt":RawType, "opt":1}
    pcell = {"fmt":RawType, "opt":1}
    pcell_list = {"fmt":RawType, "opt":1}
    pwf = {"fmt":RawType, "opt":1}
    pbttr = {"fmt":RawType, "opt":1}
    ftkey = {"fmt":LongType, "opt":1}


def convert(data):
    apatHeader = APATHeader(data)
    return apatHeader
