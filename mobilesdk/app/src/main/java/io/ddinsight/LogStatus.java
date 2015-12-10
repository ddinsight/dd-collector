/*
* Copyright 2015 AirPlug Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package io.ddinsight;

public class LogStatus {

    public long id;
    public long sendTime;
    public long recvTime;
    public long latency;
    public LogStatusCode status;
    public LogStatus(long id) {
        this.id = id;
        status = LogStatusCode.DISPATCH_READY;
        latency = 0;
    }

    public long getLatency() {
        return latency;
    }

    public enum LogStatusCode {

        DISPATCH_READY(0),
        DISPATCH_TRYING(1),
        DISPATCH_COMPLETED(2),
        DISPATCH_FAILED(3);

        public int status;

        private LogStatusCode(int status) {
            this.status = status;
        }
    }
}
