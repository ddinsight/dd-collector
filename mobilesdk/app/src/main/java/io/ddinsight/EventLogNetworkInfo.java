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

public class EventLogNetworkInfo {
    private LogStatus[] logStatus;
    private int totalLog;
    private int dispatchedLogCnt;
    private double avgLatency;
    private long lastTimestamp;
    private boolean isDispatched;

    public void analyzeNetInfo(LogStatus[] newLogStatus) {
        this.totalLog = 0;
        this.dispatchedLogCnt = 0;
        this.avgLatency = 0.0D;
        this.lastTimestamp = 0L;
        this.isDispatched = false;

        int length = newLogStatus.length;
        this.logStatus = new LogStatus[length];
        System.arraycopy(newLogStatus, 0, this.logStatus, 0, length);

        if (this.logStatus != null) {
            int legnth = this.logStatus.length;
            this.totalLog = legnth;
            long sumLatency = 0L;

            for (int i = 0; i < legnth; i++) {
                LogStatus.LogStatusCode statusCode = this.logStatus[i].status;
                if (statusCode == LogStatus.LogStatusCode.DISPATCH_COMPLETED) {
                    this.dispatchedLogCnt += 1;
                    sumLatency += this.logStatus[i].getLatency();
                }

                this.lastTimestamp = this.logStatus[i].recvTime;
            }

            if (this.dispatchedLogCnt > 0) {
                this.avgLatency = (sumLatency / this.dispatchedLogCnt);
            }
        }
    }

    public long getLastTimestamp() {
        return this.lastTimestamp;
    }

    public int getTotalLog() {
        return this.totalLog;
    }

    public int getDispatchedLogCnt() {
        return this.dispatchedLogCnt;
    }

    public boolean isDispatched() {
        if ((this.totalLog > 0) && (this.totalLog == this.dispatchedLogCnt))
            this.isDispatched = true;
        else {
            this.isDispatched = false;
        }
        return this.isDispatched;
    }

    public double getAvglatency() {
        return this.avgLatency;
    }

    public LogStatus[] getLogStatus() {
        return this.logStatus;
    }
}
