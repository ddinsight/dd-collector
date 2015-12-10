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

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CellInfoTask extends AsyncTask<Void, Void, ConcurrentLinkedQueue<JSONObject>> {
    private final static ConcurrentLinkedQueue<JSONObject> mCellList = new ConcurrentLinkedQueue<JSONObject>();
    public static int DEFAULT_LIST_SIZE = 12;
    public static long DEFAULT_INTERVAL = 5000;
    private final String TAG = this.getClass().getName();
    private String threadName;
    private AirplugAnalyticTracker mTracker;

    public CellInfoTask() {
        this.mTracker = AirplugAnalyticTracker.getInstance();
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public int trackCellInfo(long keyVal) {
        JSONArray jsonArray = new JSONArray();

        if (mCellList.size() > 0) {
            for (JSONObject jsonObject : mCellList) {
                try {
                    JSONObject copyJsonObj = new JSONObject(jsonObject.toString());

                    jsonArray.put(copyJsonObj);
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                }
            }

            if (jsonArray.length() > 0) {
                try {
                    JSONObject lastJsonObj = jsonArray.getJSONObject(jsonArray.length() - 1);
                    JSONArray nbCellList = ApCellInfo.getCellInfo(1);
                    lastJsonObj.put("nbcelllist", nbCellList);
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                }
            }

            JSONObject edataJsonObj = new JSONObject();
            try {
                edataJsonObj.put("ftkey", keyVal);
                edataJsonObj.put("cinfolist", jsonArray);
            } catch (Exception ex) {
                Log.d(TAG, ex.toString());
            }

            mTracker.trackEvent("CI", edataJsonObj);
            Log.d(TAG, "edataJsonObj=" + edataJsonObj.toString());
        } else {
            Log.d(TAG, "cellist is empty. nothing to track.");
        }

        return jsonArray.length();
    }

    public void clearCellInfo() {
        if (mCellList != null) {
            mCellList.clear();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ConcurrentLinkedQueue<JSONObject> result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled(ConcurrentLinkedQueue<JSONObject> jsonObjects) {
        super.onCancelled(jsonObjects);
    }

    @Override
    protected ConcurrentLinkedQueue<JSONObject> doInBackground(Void... arg0) {
        setThreadName(Thread.currentThread().getName());

        while (true) {
            try {
                if (isCancelled()) {
                    Log.d(TAG, "thread-" + getThreadName() + " is cancelled. thread will be exited");
                    break;
                }

                JSONObject cellInfo = new JSONObject();
                JSONArray celllist = ApCellInfo.getCellInfo(0);
                cellInfo.put("celllist", celllist);
                HashMap<String, Long> hashMap = Utils.getTraffic();
                for (Map.Entry<String, Long> entry : hashMap.entrySet()) {
                    String key = entry.getKey();
                    Long value = entry.getValue();
                    cellInfo.put(key, value);
                }

                mCellList.add(cellInfo);

                while (mCellList.size() > DEFAULT_LIST_SIZE) {
                    JSONObject polledCellInfo = mCellList.poll();
                }

                Thread.sleep(DEFAULT_INTERVAL);
            } catch (Exception ex) {
                Log.d(TAG, ex.toString());
            }
        }
        return mCellList;
    }
}