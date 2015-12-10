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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.telephony.TelephonyManager;

import java.util.Random;

public class AirplugAnalyticTracker {

    public final static String PRODUCT = "APAT_P";
    public final static String VERSION = "2.0.3";
    public final static int RUNNABLE_STOP_DELAY = 1000;
    private final static String TAG = AirplugAnalyticTracker.class.getName();
    private static AirplugAnalyticTracker instance = new AirplugAnalyticTracker();
    public static String ANDROID_ID;
    public static ConnectivityManager connectivityManager;
    public static TelephonyManager telephonyManager;
    public static WifiManager wifiManager;
    public static Context context;
    private BatteryReceiver batteryReceiver;

    private boolean enc = true;
    private boolean dispatcherIsBusy = false;
    private volatile boolean bStart = false;
    private Dispatcher dispatcher;
    private int dispatchPeriod;
    private Handler handler;
    private LogStore logStore;
    private String accountID;
    private String pkgID;
    private PackageInfo pkgInfo;
    private APHandler mAPHandler;
    private CellInfoTask mCellInfoTask;
    private Runnable dispatchRunner = new APRunnable("dispatcher runnable") {
        public void runs() {
            AirplugAnalyticTracker.this.dispatch();
        }
    };
    private Runnable runnableStop = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                synchronized (this) {
                    bStart = false;

                    if (dispatcher != null) {
                        dispatcher.stop();
                        dispatcher = null;
                    }

                    cancelPendingDispatchs();
                    mAPHandler.stop();

                    Log.d(TAG, "runnableStop complete");
                }
            } catch (Exception ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            }
        }
    };
    private Callback apatHandlerCallback = new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                TrackMsgObj obj = (TrackMsgObj) msg.obj;

                Log.i(TAG, "handle message. trackType=" + obj.trackType);

                if (obj.trackType == APHandler.WhatType.TRACK_NORMAL) {
                    APEvent event = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, null, null, obj.eData);
                    event.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    long logID = logStore.putEvent(event);
                } else if (obj.trackType == APHandler.WhatType.TRACK_NORMAL_NO_EDATA) {
                    APEvent event = createEvent(pkgInfo.packageName, obj.eventType, obj.logType, pkgInfo.versionCode, null, null, null);
                    event.setUrl(pkgInfo.packageName, obj.eventType, obj.logType, pkgInfo.versionCode);
                    long logID = logStore.putEvent(event);
                } else if (obj.trackType == APHandler.WhatType.TRACK_EVENT_KV) {
                    ExtData bindPkgInfo = new ExtData("");

                    APEvent event = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, obj.evtKey, obj.evtVal, bindPkgInfo);
                    event.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    long logID = logStore.putEvent(event);
                } else if (obj.trackType == APHandler.WhatType.TRACK_EVENT_KV_BIND) {
                    ExtData bindPkgInfo = new ExtData(obj.bindPkgName);

                    APEvent event = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, obj.evtKey, obj.evtVal, bindPkgInfo);
                    event.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    long logID = logStore.putEvent(event);
                } else if (obj.trackType == APHandler.WhatType.TRACK_EVENT_KV_EDATA) {
                    Random random = new Random();
                    int syncId = random.nextInt(2147483647);

                    ExtData bindPkgInfo = new ExtData("");

                    // track event
                    APEvent logEvent = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, obj.evtKey, obj.evtVal, bindPkgInfo);
                    logEvent.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    logEvent.setSyncId(syncId);
                    long logId = logStore.putEvent(logEvent);

                    // track log
                    APEvent analEvent = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, null, null, obj.eData);
                    analEvent.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    analEvent.setSyncId(syncId);
                    logId = logStore.putEvent(analEvent);
                } else if (obj.trackType == APHandler.WhatType.TRACK_EVENT_KV_BIND_EDATA) {
                    Random random = new Random();
                    int syncId = random.nextInt(2147483647);

                    // track event
                    ExtData bindPkgInfo = new ExtData(obj.bindPkgName);

                    APEvent logEvent = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, obj.evtKey, obj.evtVal, bindPkgInfo);
                    logEvent.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    logEvent.setSyncId(syncId);
                    long logId = logStore.putEvent(logEvent);

                    // track log
                    APEvent analEvent = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, null, null, obj.eData);
                    analEvent.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    analEvent.setSyncId(syncId);
                    logId = logStore.putEvent(analEvent);
                } else if (obj.trackType == APHandler.WhatType.TRACK_EVENT_FAULT) {
                    APEvent event = createEvent(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode, null, null, obj.eData);
                    long tm = System.currentTimeMillis();
                    event.setFaultKey("ftkey");
                    event.setFaultVal(Long.toString(tm));
                    event.setUrl(pkgInfo.packageName, EventType.LOGGING, obj.logType, pkgInfo.versionCode);
                    long logID = logStore.putEvent(event);

                    if (mCellInfoTask != null) {
                        mCellInfoTask.trackCellInfo(tm);
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            }

            return true;
        }
    };

    /**
     *
     * @return 안드로이드 로컬 sqlite db에 저장된 로그들을 제어할 수 있는 LogStore 인스턴스 객체 획득
     */
    public LogStore getLogStore() {
        return this.logStore;
    }

    /**
     *
     * @return APAT 라이브러리를 사용하기 위한 Singleton Instance 객체 획득
     */
    public static AirplugAnalyticTracker getInstance() {
        return instance;
    }

    /**
     * 현재 접속한 serving cell과 함께 neighbor cell 정보를 가져오기 위한 max cell list 사이즈를 설정
     * @param size cell list size
     */
    public void setCellInfolistSize(int size) {
        CellInfoTask.DEFAULT_LIST_SIZE = size;
    }

    /**
     * cell recording task의 녹화주기(ms)를 설정할 수 있다. 설정은 실시간으로 변경 가능하다
     * @param interval cell recording interval
     */
    public void setCellInfoTaskInterval(long interval) {
        CellInfoTask.DEFAULT_INTERVAL = interval;
    }

    /**
     * cell recording을 위한 thread를 실행한다. <p/>
     * cell recording task는 오직 한개만 실행 가능하고, 일정한 주기로 cell 정보를 수집하여 저장한다. <p/>
     * @return cell recording task 실행결과.
     */
    public boolean startRecordingCellInfo() {
        if (mCellInfoTask == null) {
            mCellInfoTask = new CellInfoTask();
        }

        mCellInfoTask.clearCellInfo();

        Status status = mCellInfoTask.getStatus();
        if (status == Status.PENDING) {
            mCellInfoTask.execute();
            return true;
        } else if (status == Status.RUNNING) {
            return true;
        } else if (status == Status.FINISHED) {
            mCellInfoTask = new CellInfoTask();
            mCellInfoTask.execute();
            return true;
        }

        return false;
    }

    /**
     * cell recording task 중지시킨다
     * @return cell recording 중지 실행결과.
     */
    public boolean stopRecordingCellInfo() {
        boolean result = false;
        if (mCellInfoTask != null) {
            result = mCellInfoTask.cancel(true);
        }
        return result;
    }

    /**
     *
     * @param accountID 사용자 email 계정정보 (ex: xxxx@airplug.ccom) 참고로 계정정보를 활용하지는 않기 때문에 어떤 정보든 입력 가능.
     * @param pkgVer    package version (ex: 1000)
     * @param period    APAT dispatch period.  >0 일 경우 local sqlite DB가 비워질때까지 설정한 주기값으로 dispatch 실행, <0 경우 period 모드로 동작하지 않음
     * @param enc       이벤트 암호화 여부
     * @param context   안드로이드 컨텍스트
     * @param host      이벤트를 전송할 서버 호스트 정보 (ex: dfront.airplug.com)
     * @param port      이벤트를 전송할 서버 포트 정보 (ex: 80)
     */
    public void start(String accountID, String pkgVer, int period, boolean enc, Context context, String host, int port) {
        start(accountID, pkgVer, period, enc, context, host, port, true);
    }

    /**
     * start() 함수에서 설정한 서버 호스트, 포트 정보를 수정하여 이벤트 정보를 전송하길 원할 경우 서버 정보 변경이 가능하다 <p/>
     * APAT 실행이후, 실시간으로 변경 가능하며 이후 모든 이벤트 정보는 변경된 서버로 전송한다. <p/>
     *
     * @param host 변경할 서버 호스트 정보
     * @param port 변경할 서버 포트 정보
     */
    public void changeHost(String host, int port) {
        if (dispatcher != null) {
            dispatcher.stop();
            dispatcher = new NetworkDispatcher(
                    AirplugAnalyticTracker.PRODUCT,
                    AirplugAnalyticTracker.VERSION,
                    host,
                    port
            );
            dispatcher.init(new DispatcherCallbacks(), logStore);

            Log.d(TAG, "host changed. host=" + host + ":" + port);
        }
    }

    public void start(String accountID, String pkgVer, int period, boolean enc, Context context, String host, int port, boolean bRecord) {
        try {
            synchronized (this) {
                if (bStart) {
                    mAPHandler.removeCallbacks(runnableStop);
                } else {
                    this.context = context.getApplicationContext();

                    if (logStore == null)
                        logStore = new PersistentLogStore(context);

                    logStore.startNewVisit();

                    if (dispatcher == null)
                        dispatcher = new NetworkDispatcher(PRODUCT, VERSION, host, port);
                    dispatcher.init(new DispatcherCallbacks(), logStore);

                    if (connectivityManager == null) {
                        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    }
                    if (handler == null)
                        handler = new Handler(context.getMainLooper());
                    else
                        cancelPendingDispatchs();

                    // encryption
                    this.enc = enc;
                    setDispatchPeriod(period);

                    // APHandler handlerThread
                    if (mAPHandler == null) {
                        mAPHandler = new APHandler(APHandler.Type.WORK, "APATHandler", apatHandlerCallback);
                    }
                    mAPHandler.start();

                    // start CellInfoTask
                    if (bRecord) {
                        if (startRecordingCellInfo()) {
                            Log.d(TAG, "start cellinfo recording is succeesed");
                        } else {
                            Log.d(TAG, "start cellinfo recording is failed");
                        }
                    }

                    // packageInfo
                    PackageManager packageManager = context.getPackageManager();
                    if (packageManager != null)
                        this.pkgInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

                    // androidID
                    AirplugAnalyticTracker.ANDROID_ID = android.provider.Settings.Secure.getString(
                            AirplugAnalyticTracker.context.getContentResolver(),
                            android.provider.Settings.Secure.ANDROID_ID
                    );

                    // add battery broadcast receiver
                    batteryReceiver = new BatteryReceiver();
                    context.getApplicationContext().registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    bStart = true;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            if (logStore != null) {
                trackEvent(LogType.DEBUG, ex);
            }
        }
    }

    /**
     *
     * @param flag 이벤트 암호화 여부 설정
     */
    public void setEnc(boolean flag) {
        enc = flag;
    }

    /**
     *
     * @return 이벤트 암호화 여부 설정값 획득
     */
    public boolean getEnc() {
        return enc;
    }

    /**
     * dispatch period 모드로 동작시키기 위한 주기값 설정. <p/>
     * period > 0 일 경우, dispatch 실행시 local sqlite DB가 <p/>
     * 모두 비워질때까지 설정한 주기값으로 주기적으로 dispatch 실행. <p/>
     * period < 0 일 경우, dispatch 한번만 실행하고 종료. <p/>
     * @param period
     */
    public void setDispatchPeriod(int period) {
        int i = this.dispatchPeriod;
        this.dispatchPeriod = period;

        if (i <= 0) {
            maybeScheduleNextDispatch();
        } else if (i > 0) {
            cancelPendingDispatchs();
            maybeScheduleNextDispatch();
        }
    }

    private void maybeScheduleNextDispatch() {
        if (this.dispatchPeriod <= 0)
            return;
        if (this.handler.postDelayed(this.dispatchRunner, this.dispatchPeriod * 1000)) {
            Log.w(TAG, "try next dispatch schedule!!");
        }
    }

    /**
     * job queue에 들어가있는 dispatch handler를 취소하여 이후 dispatch 동작하지 않도록 함.
     */
    public void cancelPendingDispatchs() {
        if (handler != null)
            handler.removeCallbacks(dispatchRunner);
    }

    /**
     * DISPATCHER_STATUS_SUCCESSED(1) <p/>
     * DISPATCHER_STATUS_NETWORK_UNAVAILABLE(-1) <p/>
     * DISPATCHER_STATUS_BUSY(-2) <p/>
     * DISPATCHER_STATUS_NO_LOGED(-3) <p/>
     * DISPATCHER_STATUS_NOT_STARTED(-4) <p/>
     * DISPATCHER_STATUS_INVALID_IP_ADDRESS(-5) <p/>
     * DISPATCHER_STATUS_INIT(-6) <p/>
     *
     * @return DispatchStatusCode, dispatch 실행 후, 상태 결과값을 리턴한다
     */
    public DispatchStatusCode dispatch() {
        DispatchStatusCode status = DispatchStatusCode.DISPATCHER_STATUS_INIT;

        try {
            NetworkInfo localNetworkInfo = connectivityManager.getActiveNetworkInfo();

            // dispatcher thread null check
            if (dispatcher == null) {
                status = DispatchStatusCode.DISPATCHER_STATUS_NOT_STARTED;
                Log.d(TAG, "APAT is not started");
            } else if (this.dispatcherIsBusy) { // check busy
                Log.d(TAG, "dispatcher is busy");
                maybeScheduleNextDispatch();
                status = DispatchStatusCode.DISPATCHER_STATUS_BUSY;
            } else if ((localNetworkInfo == null) || (!localNetworkInfo.isAvailable())) {
                Log.d(TAG, "local network is not available");
                maybeScheduleNextDispatch();
                status = DispatchStatusCode.DISPATCHER_STATUS_NETWORK_UNAVAILABLE;
            } else {
                if (this.logStore.getNumStoredLogs() != 0) {
                    EventLog[] eventLogs = logStore.peekLogs();
                    dispatcherIsBusy = true;
                    dispatcher.dispatchLogs(eventLogs);
                    maybeScheduleNextDispatch();
                    status = DispatchStatusCode.DISPATCHER_STATUS_SUCCESSED;
                } else {
                    status = DispatchStatusCode.DISPATCHER_STATUS_NO_LOGED;
                }
            }

            EventLogNetworkInfo info = getLastDispatchedLogNetInfo();
            if (info != null) {
                Log.d(TAG, "Last Dispatched Info \n" +
                        "agv latency=" + info.getAvglatency() +
                        "total log=" + info.getTotalLog() +
                        "dispatched cnt=" + info.getDispatchedLogCnt() +
                        "last timestamp=" + info.getLastTimestamp());
            }

            Log.i(TAG, "dispatch called. dispatch status=" + status);

        } catch (Exception ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }

        return status;
    }

    /**
     * dispatch 실행후, 완료되었을 경우 호출되는 콜백함수
     */
    void dispatcheFinished() {
        dispatcherIsBusy = false;

        LogStatus[] logStatus = PersistentLogStore.tableLogStatus.values().toArray(new LogStatus[PersistentLogStore.tableLogStatus.size()]);
        this.logStore.setLastLogsNetInfo(logStatus);

        Log.d(TAG, "dispatch finished");
    }

    /**
     * APAT stop <p/>
     * APAT 시작하면서 할당한 모든 자원을 해제한다. <p/>
     * 종료되기 직전 남아있는 로그 전송을 위해 dispatch 실행하고, <p/>
     * cell recording task가 동작중이라면 정지시키고, <p/>
     * 배터리 상태정보를 수신하기 위한 battery receiver 등록을 해제한다 <p/>
     */
    public void stop() {
        dispatch();
        mAPHandler.postDelayed(runnableStop, RUNNABLE_STOP_DELAY);
        stopRecordingCellInfo();
        if(batteryReceiver != null){
            context.getApplicationContext().unregisterReceiver(batteryReceiver);
        }
    }

    /**
     * 참고로 getLogStore() 함수를 통해 획득한 LogStore 인스턴스로 직접 제어가 가능하다. <p/>
     * local sqlite DB에서 한번에 가져올 로그의 수를 설정한다. <p/>
     * dispatch 실행시, http pipeline을 통해 한번에 전송할 로그의 수를 설정하기 위해 사용한다. <p/>
     * @param parmaInt local sqlite DB 에서 이벤트 로그를 가져오기 위한 peek count
     */
    public void setPeekLogCnt(int parmaInt) {
        this.logStore.setPeekLogCnt(parmaInt);
    }

    /**
     * 서버로 이벤트 정보를 전송하지 못하고 local sqlite DB에 쌓인 이벤트의 수를 리턴한다
     * @return local sqlite DB에 쌓인 이벤트 로그의 수
     */
    public int getNumStoredLogs() {
        return this.logStore.getNumStoredLogs();
    }

    /**
     * @return APAT 실행여부
     */
    public boolean isStarted() {
        return this.bStart;
    }

    /**
     * 저장된 이벤트 로그의 id를 이용하여 DB에 저장된 특정 로그 삭제
     * @param id 삭제된 이벤트 로그의 id
     */
    void deleteLog(long id) {
        this.logStore.deleteLog(id);
    }

    /**
     * local sqlite DB에 저장된 모든 이벤트 로그 삭제
     */
    public void deleteLogAll() {
        this.logStore.deleteLogAll();
    }


    /**
     *
     * @return APAT를 최초 설치한 시간정보 (timestamp (ms))
     */
    public String getUUID() {
        return this.logStore.getUUID();
    }

    /**
     * APAT start 이후 새로운 세션이 시작된 시간 (timestamp (ms))
     * @return APAT Session ID (timestamp)
     */
    public String getSessionID() {
        return this.logStore.getSessionId();
    }

    /**
     * cellular deadzone 식별을 위해 셀신호 세기 관련정보를 수집하여 분석하기 위한 목적으로 만든 함수.
     *
     * @param logType 수집할 로그 타입
     * @param eData 수집할 로그 정보
     * @return track 성공여부
     */
    public long trackEventFault(String logType, Object eData) {
        TrackMsgObj obj = new TrackMsgObj(APHandler.WhatType.TRACK_EVENT_FAULT, logType, null, eData, null, null, null);
        mAPHandler.sendMessage(APHandler.WhatType.TRACK_EVENT_FAULT.ordinal(), obj);
        return 1;
    }

    /**
     * AAT("AT") <p/>
     * ANS("AN") <p/>
     * DEBUG("DB") <p/>
     * TEST("TE") <p/>
     * SETUP("ST") <p/>
     * GE("GE") <p/>
     * AM("AM") <p/>
     * BM("BM") <p/>
     * UB("UB") <p/>
     * CN("CN") <p/>
     * CI("CI") <p/>
     * CD("CD") <p/>
     *
     * @param logType enum 형태의 LogType
     * @param eData 이벤트 로그 정보를 포함한 Java Object
     * @return local sqlite DB에 저장된 후, 이벤트 로그 식별을 위한 unique track id 값
     */
    public long trackEvent(LogType logType, Object eData) {
        long trackId = trackEvent(logType.type, eData);
        return trackId;
    }

    public long trackEvent(String logType, Object eData) {
        TrackMsgObj obj = new TrackMsgObj(APHandler.WhatType.TRACK_NORMAL, logType, null, eData, null, null, null);
        mAPHandler.sendMessage(APHandler.WhatType.TRACK_NORMAL.ordinal(), obj);

        return 1;
    }

    /**
     * LOGGING("Logging") <p/>
     * INSTALL("Install") <p/>
     * UPDATE("Update") <p/>
     *
     * @param logType LogType(enum)
     * @param eventType EventType(enum)
     * @return event track id
     */
    public long trackEvent(LogType logType, EventType eventType) {
        long trackId = trackEvent(logType.type, eventType);
        return trackId;
    }

    public long trackEvent(String logType, EventType eventType) {
        TrackMsgObj obj = new TrackMsgObj(APHandler.WhatType.TRACK_NORMAL_NO_EDATA, logType, null, null, eventType, null, null);
        mAPHandler.sendMessage(APHandler.WhatType.TRACK_NORMAL_NO_EDATA.ordinal(), obj);

        return 1;
    }

    /**
     * key/value 형태의 pair 값을 이벤트 로그 정보르 전송하기 위한 track 함수
     *
     * @param logType LogType(enum)
     * @param evtKey event key
     * @param evtVal event value
     * @return event track id
     */
    public long trackEventKV(LogType logType, String evtKey, String evtVal) {
        long trackId = trackEventKV(logType.type, evtKey, evtVal);
        return trackId;
    }

    public long trackEventKV(String logType, String evtKey, String evtVal) {
        TrackMsgObj obj = new TrackMsgObj(APHandler.WhatType.TRACK_EVENT_KV, logType, null, null, null, evtKey, evtVal);
        mAPHandler.sendMessage(APHandler.WhatType.TRACK_EVENT_KV.ordinal(), obj);

        return 1;
    }

    /**
     * APAT가 멀티 앱 패키지와 연동할 경우 특정 바인딩된 패캐지에서 발생한 이벤트 로그 수집을 위한 trackXXX 함수 제공
     *
     * @param bindPkgName APAT가 바인딩된 패키지 이름 (ex: com.airplug.hoppin)
     * @param logType LogType(enum)
     * @param evtKey event key
     * @param evtVal event value
     * @return event track id
     */
    public long trackEventKV(String bindPkgName, LogType logType, String evtKey, String evtVal) {
        long trackId = trackEventKV(bindPkgName, logType.type, evtKey, evtVal);
        return trackId;
    }

    public long trackEventKV(String bindPkgName, String logType, String evtKey, String evtVal) {
        TrackMsgObj obj = new TrackMsgObj(APHandler.WhatType.TRACK_EVENT_KV_BIND, logType, bindPkgName, null, null, evtKey, evtVal);
        mAPHandler.sendMessage(APHandler.WhatType.TRACK_EVENT_KV_BIND.ordinal(), obj);

        return 1;
    }

    /**
     * trackEventKV 함수에 이벤트 로그 정보를 담기 위한 eData(Java Object)를 포함하여 track 하기 위한 함수 제공
     * @param logType LogType(enum)
     * @param eData 이벤트 로그 정보
     * @param key event key
     * @param val event value
     * @return event track id
     */
    public long trackLogEventKV(LogType logType, Object eData, String key, String val) {
        long trackId = trackLogEventKV(logType.type, eData, key, val);
        return trackId;
    }

    public long trackLogEventKV(String logType, Object eData, String key, String val) {
        TrackMsgObj obj = new TrackMsgObj(APHandler.WhatType.TRACK_EVENT_KV_EDATA, logType, null, null, null, key, val);
        mAPHandler.sendMessage(APHandler.WhatType.TRACK_EVENT_KV_EDATA.ordinal(), obj);

        return 1;
    }

    /**
     *
     * @param bindPkgName APAT가 바인딩된 패키지 이름 (ex: com.airplug.hoppin)
     * @param logType LogType(enum)
     * @param eData 이벤트 로그 정보
     * @param key event key
     * @param val event value
     * @return event track id
     */
    public long trackLogEventKV(String bindPkgName, LogType logType, Object eData, String key, String val) {
        long trackId = trackLogEventKV(bindPkgName, logType.type, eData, key, val);
        return trackId;
    }

    public long trackLogEventKV(String bindPkgName, String logType, Object eData, String key, String val) {
        TrackMsgObj obj = new TrackMsgObj(APHandler.WhatType.TRACK_EVENT_KV_BIND_EDATA, logType, bindPkgName, eData, null, key, val);
        mAPHandler.sendMessage(APHandler.WhatType.TRACK_EVENT_KV_BIND_EDATA.ordinal(), obj);

        return 1;
    }

    private APEvent createEvent(String pkgName, EventType eventType, String logType, int versionCode, String key, String value, Object eData) {
        APEvent event = new APEvent(1, pkgName, eventType.eventType, logType, versionCode, key, value, eData,
                this.context.getResources().getDisplayMetrics().widthPixels,
                this.context.getResources().getDisplayMetrics().heightPixels);

        return event;
    }

    /**
     * 마지막 Dispatch 상태 결과값을 보여준다. <p/>
     *
     * + EventLogNetworkInfo <p/>
     * LogStatus[] logStatus <p/>
     * totalLog <p/>
     * dispatchedLogCnt <p/>
     * avgLatency <p/>
     * lastTimestamp <p/>
     * isDispatched <p/><p/>
     *
     * + LogStatus <p/>
     * DISPATCH_READY(0) <p/>
     * DISPATCH_TRYING(1) <p/>
     * DISPATCH_COMPLETED(2) <p/>
     * DISPATCH_FAILED(3) <p/>
     *
     * @return EventLogNetworkInfo
     */
    public EventLogNetworkInfo getLastDispatchedLogNetInfo() {
        return logStore.getLastLogsNetInfo();
    }


    /**
     * DISPATCHER_STATUS_SUCCESSED: dispatch 성공 <p/>
     * DISPATCHER_STATUS_NETWORK_UNAVAILABLE: 네트워크 사용이 불가능한 상태 <p/>
     * DISPATCHER_STATUS_BUSY: 이미 dispatch task가 실행중이고, Busy 상태임 <p/>
     * DISPATCHER_STATUS_NO_LOGED: 저장된 Event 로그가 없음 <p/>
     * DISPATCHER_STATUS_NOT_STARTED: APAT가 실행된 상태가 아님 <p/>
     * DISPATCHER_STATUS_INVALID_IP_ADDRESS: 안드로이드 단말이 할당받은 IP가 유효하지 않음 <p/>
     * DISPATCHER_STATUS_INIT: dispatch 초기화중인 상태 <p/>
     */
    public enum DispatchStatusCode {
        DISPATCHER_STATUS_SUCCESSED(1),
        DISPATCHER_STATUS_NETWORK_UNAVAILABLE(-1),
        DISPATCHER_STATUS_BUSY(-2),
        DISPATCHER_STATUS_NO_LOGED(-3),
        DISPATCHER_STATUS_NOT_STARTED(-4),
        DISPATCHER_STATUS_INVALID_IP_ADDRESS(-5),
        DISPATCHER_STATUS_INIT(-6);

        public int code;

        private DispatchStatusCode(int errorNo) {
            this.code = code;
        }
    }

    /**
     * dispatch callback class
     */
    final class DispatcherCallbacks implements Dispatcher.Callbacks {
        public DispatcherCallbacks() {
            // TODO Auto-generated constructor stub
        }

        /**
         * dispatch가 성공적으로 끝난 경우 해당 event track id 를 local sqlite DB에서 삭제
         * @param id event track id
         */
        @Override
        public void logDispatched(long id) {
            // TODO Auto-generated method stub
            AirplugAnalyticTracker.this.deleteLog(id);
        }

        /**
         * dispatch가 성공적으로 끝난 경우 dispatch 결과값을 로깅하기 위한 콜백함수 호출
         */
        @Override
        public void dispatchFinished() {
            // TODO Auto-generated method stub
            AirplugAnalyticTracker.this.dispatcheFinished();
        }
    }
}

/**
 * 바인딩된 패키지 정보를 전솣하기 위한 클래스 객체
 */
class ExtData {
    public String playAppPackageName;

    public ExtData(String pkgName) {
        this.playAppPackageName = pkgName;
    }
}

/**
 * trackEventXXX 호출시 ApHandler와 통신하기 위한 메시지 구조
 */
class TrackMsgObj {
    APHandler.WhatType trackType;
    String logType;
    String bindPkgName;
    Object eData;
    EventType eventType;
    String evtKey;
    String evtVal;
    String faultKey;
    String faultVal;

    public TrackMsgObj(APHandler.WhatType trackType,
                       String logType,
                       String bindPkgName,
                       Object eData,
                       EventType eventType,
                       String key,
                       String value) {
        this.trackType = trackType;
        this.logType = logType;
        this.bindPkgName = bindPkgName;
        this.eData = eData;
        this.eventType = eventType;
        this.evtKey = key;
        this.evtVal = value;
    }
}
