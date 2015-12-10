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

public class Log {
	final static boolean LOG = true;

	// info
	public static void i(String tag, String string) {
		if (LOG)
			android.util.Log.i(tag, string);
	}

	// error
	public static void e(String tag, String string) {
		if (LOG)
			android.util.Log.e(tag, string);
	}

	// error (exception)
	public static void e(String tag, Exception ex) {
		if (LOG)
			android.util.Log.getStackTraceString(ex);
	}

	// debug
	public static void d(String tag, String string) {
		if (LOG)
			android.util.Log.d(tag, string);
	}

	// verbose
	public static void v(String tag, String string) {
		if (LOG)
			android.util.Log.v(tag, string);
	}

	// warning
	public static void w(String tag, String string) {
		if (LOG)
			android.util.Log.w(tag, string);
	}

	// warning (exception)
	public static void w(String tag, String msg, Exception ex) {
		if (LOG)
			android.util.Log.getStackTraceString(ex);
	}

	// warning (throwable)
	public static void w(String tag, Throwable ex) {
		if (LOG)
			android.util.Log.getStackTraceString(ex);
	}

	public static String getStackTraceString(Exception ex) {
		String ret = null;
		if (LOG) {
			ret = android.util.Log.getStackTraceString(ex);
		}
		return ret;
	}
}
