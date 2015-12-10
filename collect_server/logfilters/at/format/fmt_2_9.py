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


@Container(report=True)
class LogFormatter(object):
    #Configuration
    confReportVersion = {"fmt":StringType, "cond":["regex", "^v(\d+)\.(\d+)$|^v((\d+)\.){2}(\d+)$"]}
    confAgentVersion = {"fmt":UnicodeType}
    confUnixTime = {"fmt":IntegerType}
    confOperator = {"fmt":StringType, "cond":["regex", "^(\d+)$"]}

    #Agent
    agentLogType = {"fmt":IntegerType, "cond":["range", 0, 12]}
    agentLogCount = {"fmt":IntegerType}
    agentLogStartTime = {"fmt":IntegerType}
    agentLogEndTime = {"fmt":IntegerType}
    agentAatOnOff = {"fmt":BooleanType}
    agentMode = {"fmt":StringType, "cond":["regex", "^[A-C]$"]}
    agentAnsMode = {"fmt":StringType, "cond":["regex", "^HP$|^SAVE$"]}
    agentAllowMobile = {"fmt":BooleanType}
    agentAllowAns = {"fmt":BooleanType}

    limitCell = {"fmt":BooleanType}
    limitCellSetBytes = {"fmt":IntegerType}
    limitCellUsedBytes = {"fmt":IntegerType}
    limitCellApplied = {"fmt":BooleanType}

    # Traffic from System
    trafficSystemMoTxBytes = {"fmt":IntegerType}
    trafficSystemMoRxBytes = {"fmt":IntegerType}
    trafficSystemWFTxBytes = {"fmt":IntegerType}
    trafficSystemWFRxBytes = {"fmt":IntegerType}

    # Traffic from Agent
    trafficAgentMoAveLatency = {"fmt":IntegerType}
    trafficAgentMoAveBW = {"fmt":FloatType}
    trafficAgentMoBytes = {"fmt":IntegerType}
    trafficAgentWFAveLatency = {"fmt":IntegerType}
    trafficAgentWFAveBW = {"fmt":FloatType}
    trafficAgentWFBytes = {"fmt":IntegerType}
    trafficAgentBWchange = {"fmt":StringType}

    # Network
    netPhoneType = {"fmt":IntegerType, "cond":["range", 0, 2]}
    netCID = {"fmt":IntegerType}
    netLAC = {"fmt":IntegerType}
    netActiveNetwork = {"fmt":RawType}
    netConnectedNetworkCount = {"fmt":IntegerType}
    netCellState = {"fmt":IntegerType}
    bbCount = {"fmt":IntegerType}
    bbMode = {"fmt":IntegerType, "cond":["range", 0, 2]}
    bbList = {"fmt":RawType}

    # Battery
    batteryInfo = {"fmt":RawType}

    # Play
    playServiceMode = {"fmt":IntegerType, "cond":["range", 1, 5]}
    playApiVersion = {"fmt":StringType}
    playAppPackageName = {"fmt":UnicodeType}
    playPlayerPackageName = {"fmt":UnicodeType}
    playSessionId = {"fmt":StringType}
    playHost = {"fmt":StringType}
    playOrigin = {"fmt":StringType}
    playContentId = {"fmt":UnicodeType}
    playTitle = {"fmt":UnicodeType}
    playPreparingTime = {"fmt":FloatType}
    playBufferState = {"fmt":IntegerType, "cond":["range", 0, 2]}
    playPlayingTime = {"fmt":IntegerType}
    playEndState = {"fmt":StringType}


    # Common Field
    playSeekCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [1, 3, 4]]}}
    playSeekForwardCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [1, 3, 4]]}}
    playSeekRewindCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [1, 3, 4]]}}
    playBufferingCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [1, 3, 4]]}}
    playResumeCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [1, 3, 4]]}}
    playAccBufferingTime = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [1, 2, 3, 4]]}}
    playMaxBufferingTime = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [1, 3, 4]]}}

    # VOD Only
    vodContentSize = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 1]}}
    vodContentDuration = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 1]}}
    vodDownloadedTime = {"fmt":StringType, "opt":1, "dep":{"playServiceMode":["val", 1]}}

    # Common Field - HLS Live and HLS VOD
    liveReceivedTSCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    liveSkippedTSCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 2]}}
    liveUserSkippedTSCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 2]}}
    liveTimeoutTSCount = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    liveTargetDuration = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    liveCurrentTSBitrate = {"fmt":FloatType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    liveCurrentThroughput = {"fmt":FloatType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    liveThroughput = {"fmt":FloatType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    liveInitThroughput = {"fmt":FloatType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    abrMode = {"fmt":StringType, "cond":["regex", "^[ABCX]$"], "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    abrAuxData = {"fmt":StringType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    abrMeasureT = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    playBitrateList = {"fmt":StringType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    reqBitrate = {"fmt":FloatType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    requestBR = {"fmt":FloatType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}
    liveContentTitle = {"fmt":RawType, "opt":1}
    userSelectBitrate = {"fmt":StringType, "opt":1, "dep":{"playServiceMode":["in", [2, 3]]}}

    # Audio
    audContentSize = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 4]}}
    audContentDuration = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 4]}}
    audDownloadedTime = {"fmt":StringType, "opt":1, "dep":{"playServiceMode":["val", 4]}}

    # Download
    adnMode = {"fmt":StringType, "cond":["regex", "^NR$|^BB$|^WF$"], "opt":1, "dep":{"playServiceMode":["val", 5]}}
    adnContentSize = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 5]}}
    adnContentRangeStart = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 5]}}
    adnDownloadSize = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 5]}}
    adnDownloadTime = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 5]}}
    adnContentID = {"fmt":IntegerType, "opt":1, "dep":{"playServiceMode":["val", 5]}}
    adnStartCode = {"fmt":IntegerType, "cond":["in", [0, 10, 20]], "opt":1, "dep":{"playServiceMode":["val", 5]}}

    # Optional
    agentUserSetup = {"fmt":StringType, "opt":1}

    # Debug
    confPhoneNumber = {"fmt":StringType, "opt":1}
    agentAnsOnOff = {"fmt":BooleanType, "opt":1}
    netConnectivityCount = {"fmt":IntegerType, "opt":1}
    netConnectivityList = {"fmt":RawType, "opt":1}
    playUserInput = {"fmt":StringType, "opt":1}
    playBufferingEventCount = {"fmt":IntegerType, "opt":1}
    playBufferingEventList = {"fmt":RawType, "opt":1}
    abrMeasureP = {"fmt":IntegerType, "opt":1}
    abrMeasureQ = {"fmt":IntegerType, "opt":1}
    abrMeasureC = {"fmt":IntegerType, "opt":1}
    vodBufferAtPrepared = {"fmt":IntegerType, "opt":1}
    vodBufferAtEnd = {"fmt":IntegerType, "opt":1}
    trafficSystemMoRxBytesChange = {"fmt":StringType, "opt":1}
    trafficSystemWFRxBytesChange = {"fmt":StringType, "opt":1}


def convert(data):
    result = LogFormatter(data)
    return result
