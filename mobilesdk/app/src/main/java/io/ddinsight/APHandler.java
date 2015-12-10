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

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;


public class APHandler {
    private static final String TAG = APHandler.class.getName();
    private String name;
    private Callback callback;
    private Handler handler;
    private HandlerThread handlerThread;
    private volatile boolean active;
    private Callback handlerCallback = new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (active) {
                try {
                    boolean handle = true;
                    if (callback != null) {
                        handle = callback.handleMessage(msg) == false;
                    }
                    if (handle) APHandler.this.handleMessage(msg);
                } catch (Throwable e) {
                    Log.e(TAG, "handleMessage failed");
                }
            }

            return true;
        }
    };
    public APHandler() {
        this(Type.MAIN, TAG, null);
    }

    public APHandler(Type type, String name) {
        this(type, name, null);
    }

    public APHandler(Type type, String name, Callback callback) {
        this.callback = callback;

        switch (type) {
            case MAIN:
                this.name = name + "@" + Thread.currentThread().getName();
                break;

            case WORK:
                handlerThread = new HandlerThread(name);
                this.name = name + "-" + handlerThread.getId();
                handlerThread.setName(this.name);
                break;
        }
    }

    public static long getWhen(Message msg) {
        return msg.getWhen() - SystemClock.uptimeMillis();
    }

    public synchronized void start() {
        if (handlerThread == null) {
            handler = new Handler(Looper.getMainLooper(), handlerCallback);
        } else {
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper(), handlerCallback);
        }
        active = true;
    }

    public synchronized void stop() {
        active = false;
        if (handlerThread != null) {
            if (handlerThread.quit() == false) {
                Log.w(TAG, name + " failed to quit");
            }
            handlerThread = null;
        }
    }

    public synchronized void pause() {
        active = false;
    }

    public synchronized void resume() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Handler getHandler() {
        return handler;
    }

    public String getName() {
        return name;
    }

    protected void handleMessage(Message msg) {

    }

    public void sendMessage(int what, int arg1, int arg2, Object obj, long delayMillis) {
        if (active) {
            Message msg = handler.obtainMessage(what, arg1, arg2, obj);

            if (0 < delayMillis) {
                handler.sendMessageDelayed(msg, delayMillis);
            } else {
                handler.sendMessage(msg);
            }
        }
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        sendMessage(what, arg1, arg2, obj, 0);
    }

    public void sendMessage(int what, int arg1, int arg2) {
        sendMessage(what, arg1, arg2, null, 0);
    }

    public void sendMessage(int what, Object obj) {
        sendMessage(what, 0, 0, obj, 0);
    }

    public void sendMessage(int what) {
        sendMessage(what, 0, 0, null, 0);
    }

    public void sendMessageDelayed(int what, long delayMillis) {
        sendMessage(what, 0, 0, null, delayMillis);
    }

    public void sendMessageDelayed(int what, Object obj, long delayMillis) {
        sendMessage(what, 0, 0, obj, delayMillis);
    }

    public void sendMessage(Message msg) {
        sendMessage(msg.what, msg.arg1, msg.arg2, msg.obj, 0);
    }

    public void sendMessageDelayed(Message msg, long delayMillis) {
        sendMessage(msg.what, msg.arg1, msg.arg2, msg.obj, delayMillis);
    }

    private boolean post(Runnable runnable, long delayMillis) {
        if (active) {
            if (0 < delayMillis) {
                return handler.postDelayed(runnable, delayMillis);
            } else {
                return handler.post(runnable);
            }
        }
        return false;
    }

    public boolean post(Runnable runnable) {
        return post(runnable, 0);
    }

    public boolean postDelayed(Runnable runnable, long delayMillis) {
        return post(runnable, delayMillis);
    }

    public boolean post(APRunnable runnable) {
        return post(runnable, 0);
    }

    public boolean postDelayed(APRunnable runnable, long delayMillis) {
        return post(runnable, delayMillis);
    }

    public void removeCallbacks(Runnable runnable) {
        if (handler != null) handler.removeCallbacks(runnable);
    }

    public void removeMessages(int what) {
        if (handler != null) handler.removeMessages(what);
    }

    public boolean hasMessages(int what) {
        return handler != null ? handler.hasMessages(what) : false;
    }

    public enum Type {
        MAIN,
        WORK
    }

    public enum WhatType {
        // command
        CMD_START,
        CMD_STOP,

        // track
        TRACK_NORMAL,
        TRACK_NORMAL_NO_EDATA,
        TRACK_EVENT_KV,
        TRACK_EVENT_KV_BIND,
        TRACK_EVENT_KV_EDATA,
        TRACK_EVENT_KV_BIND_EDATA,
        TRACK_EVENT_FAULT,
    }
}

