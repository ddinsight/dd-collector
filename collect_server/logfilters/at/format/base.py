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
    playApiVersion = StringType
    playPlayingTime = IntegerType
    trafficSystemMoTxBytes = IntegerType
    agentMode = StringType
    playTitle = StringType
    playOrigin = StringType
    confOperatorName = StringType
    testIndex = IntegerType
    playHost = StringType
    trafficSystemMoRxBytesChange = IntegerType
    netCID = IntegerType
    trafficAgentMoAveLatency = IntegerType
    netConnectivityList = RawType
    trafficSystemMoRxBytes = IntegerType
    confReportVersion = StringType
    netLAC = IntegerType
    trafficSystemWFRxBytesChange = StringType
    playResumeCount = IntegerType
    playBufferingEventCount = IntegerType
    agentLogSendTime = StringType
    trafficAgentBWchange = StringType
    testUserScore = IntegerType
    playSeekCount = IntegerType
    playAppPackageName = UnicodeType
    netActiveNetwork = StringType
    playPreparingTime = IntegerType
    playEndState = StringType
    playServiceMode = IntegerType
    agentLogEndTime = StringType
    agentLogStartTime = StringType
    trafficAgentWFAveLatency = IntegerType
    agentLogCount = IntegerType
    agentAllowMobile = BooleanType
    agentAatOnOff = BooleanType
    confUnixTime = IntegerType
    confAgentVersion = UnicodeType
    playSeekForwardCount = IntegerType
    playSessionId = StringType
    netAPCount = IntegerType
    playBufferingEventList = RawType
    playBufferingCount = IntegerType
    batteryInfo = StringType
    testCalcScore = IntegerType
    agentLogType = IntegerType
    trafficAgentMoBytes = IntegerType
    trafficSystemWFTxBytes = IntegerType
    agentMaoOnOff = BooleanType
    netPhoneType = IntegerType
    playContentId = StringType
    agentAnsMode = StringType
    vodBufferAtEnd = IntegerType
    confOperator = StringType
    playUserInput = StringType
    agentAnsEstimation = StringType
    vodContentSize = IntegerType
    confDate = StringType
    agentAllowAns = BooleanType
    netConnectedNetworkCount = IntegerType
    confOSVersion = StringType
    trafficAgentMoAveBW = FloatType
    vodContentDuration = IntegerType
    confProductModel = StringType
    netConnectivityCount = IntegerType
    playAccBufferingTime = IntegerType
    playSeekRewindCount = IntegerType
    vodDownloadedTime = IntegerType
    confPhoneNumber = StringType
    testComment = StringType
    playPlayerPackageName = UnicodeType
    playMaxBufferingTime = IntegerType
    vodBufferAtPrepared = IntegerType
    trafficAgentWFAveBW = IntegerType
    trafficAgentWFBytes = IntegerType
    agentAnsOnOff = BooleanType
    trafficSystemWFRxBytes = IntegerType


def convert(data):
    @Container
    class Dummy(BaseFormatter):
        pass

    result = Dummy(data)
    return result
