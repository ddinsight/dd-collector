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

public class ShipdockDebugReport {
    private final static String TAG = ShipdockDebugReport.class.getName();

    private long date;
    private Object msg;
    private String logger;
    private String level;
    private String thread;
    private String file;
    private String classname;
    private String method;
    private int line;

    public ShipdockDebugReport(Exception apex, Object obj) {
        try {
            this.date = System.currentTimeMillis();
            this.msg = obj;
            this.logger = "apat_logger";
            this.level = "ERROR";
            this.thread = Thread.currentThread().getName();

            StackTraceElement[] elements = apex.getStackTrace();
            if (elements.length > 0) {
                this.file = elements[0].getFileName();
                this.classname = elements[0].getClassName();
                this.method = elements[0].getMethodName();
                this.line = elements[0].getLineNumber();
            }
        } catch (Exception ex) {
            Log.d(TAG, "ShipdockDebugReport failed. ex=" + ex.toString());
        }
    }

    public ShipdockDebugReport(Object obj) {
        try {
            this.date = System.currentTimeMillis();
            this.msg = obj;
            this.logger = "apat_logger";
            this.level = "ERROR";
            this.thread = Thread.currentThread().getName();
            this.file = "";
            this.classname = "";
            this.method = "";
            this.line = 0;
        } catch (Exception ex) {
            Log.d(TAG, "ShipdockDebugReport error=" + ex.toString());
        }
    }
}
