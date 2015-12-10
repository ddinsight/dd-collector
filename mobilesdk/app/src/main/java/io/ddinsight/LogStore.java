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

import io.ddinsight.LogStatus.LogStatusCode;

public abstract interface LogStore {
    public abstract long putEvent(APEvent paramEvent);

    public abstract EventLog[] peekLogs();

    public abstract EventLog[] peekLogs(int paramInt);

    public abstract void setPeekLogCnt(int peekLog);

    public abstract void deleteLog(long paramLong);

    public abstract void deleteLogAll();

    public abstract void deleteDB(String packageName);

    public abstract String getVisitorId();

    public abstract String getUUID();

    public abstract String getSessionId();

    public abstract void loadExistingSession();

    public abstract void startNewVisit();

    public abstract int getNumStoredLogs();

    public abstract int getTotalLogNumFromDb();

    public abstract LogStatus getLogStatusByID(long id);

    public abstract EventLogNetworkInfo getLastLogsNetInfo();

    public abstract void setLastLogsNetInfo(LogStatus[] logStatus);

    public abstract int getStoreId();

    public abstract void updateLogStatus(long logID, LogStatusCode statusCode);

    public abstract void updateEventForMarkNetworkError(long logID);

}