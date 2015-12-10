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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class PersistentLogStore implements LogStore {
    public static final int MAX_LOGS = 10000;
    public static final int MAX_PEEK_LOGS = 30;
    private static final String DATABASE_NAME = "airplug_analytics.db";
    private static final int DATABASE_VERSION = 7;
    private static final String CREATE_LOGS_TABLE = "CREATE TABLE IF NOT EXISTS logs (" +
            String.format(" '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,", new Object[]{"log_id"}) +
            String.format(" '%s' TEXT NOT NULL,", new Object[]{"log_string"}) +
            String.format(" '%s' INTEGER NOT NULL);", new Object[]{"log_time"});
    private static final String CREATE_SESSION_TABLE = "CREATE TABLE IF NOT EXISTS session (" +
            String.format(" '%s' INTEGER PRIMARY KEY,", new Object[]{"tm_first"}) +
            String.format(" '%s' INTEGER NOT NULL,", new Object[]{"tm_previous"}) +
            String.format(" '%s' INTEGER NOT NULL,", new Object[]{"tm_current"}) +
            String.format(" '%s' INTEGER NOT NULL,", new Object[]{"visits"}) +
            String.format(" '%s' INTEGER NOT NULL);", new Object[]{"log_id"});
    public static Gson gson = new Gson();
    public static Hashtable<Long, LogStatus> tableLogStatus = new Hashtable<Long, LogStatus>(MAX_PEEK_LOGS * 2);
    private final String TAG = this.getClass().getName();
    private DataBaseHelper databaseHelper;
    private int storeId;
    private long tmFirst;
    private long tmPrevious;
    private long tmCurrent;
    private int visits;
    private volatile int nStoredLogEvent;
    private int nTotalLogEvents;
    private boolean sessionStarted;
    private Random random = new Random();
    private int peekLogCnt;
    private EventLogNetworkInfo lastLogNetInfo;

    PersistentLogStore(Context paramContext) {
        this(paramContext, DATABASE_NAME, DATABASE_VERSION);
        this.gson = new Gson();
    }

    PersistentLogStore(Context paramContext, String paramString, int paramInt) {
        this.databaseHelper = new DataBaseHelper(paramContext, paramString, paramInt, this);
        loadExistingSession();
        peekLogCnt = MAX_PEEK_LOGS;
    }

    PersistentLogStore(DataBaseHelper paramDataBaseHelper) {
        this.databaseHelper = paramDataBaseHelper;
        loadExistingSession();
    }

    private static boolean endTransaction(SQLiteDatabase paramSQLiteDatabase) {
        try {
            paramSQLiteDatabase.endTransaction();
            return true;
        } catch (SQLiteException localSQLiteException) {
            Log.e("PersistentLogStore", "exception ending transaction:" + localSQLiteException.toString());
        }
        return false;
    }

    DataBaseHelper getDatabaseHelper() {
        return this.databaseHelper;
    }

    long getTimestampFirst() {
        return this.tmFirst;
    }

    long getTimestampPrevious() {
        return this.tmPrevious;
    }

    long getTimestampCurrent() {
        return this.tmCurrent;
    }

    int getVisits() {
        return this.visits;
    }

    public void updateLogStatus(long logID, LogStatus.LogStatusCode statusCode) {
        if (tableLogStatus.containsKey(logID)) {
            LogStatus logStatus = tableLogStatus.get(logID);

            if (statusCode == LogStatus.LogStatusCode.DISPATCH_TRYING) {
                logStatus.status = LogStatus.LogStatusCode.DISPATCH_TRYING;
                logStatus.sendTime = System.currentTimeMillis();
            } else if (statusCode == LogStatus.LogStatusCode.DISPATCH_COMPLETED) {
                logStatus.status = LogStatus.LogStatusCode.DISPATCH_COMPLETED;
                logStatus.recvTime = System.currentTimeMillis();
                logStatus.latency = logStatus.recvTime - logStatus.sendTime;
                if (logStatus.latency < 0) {
                    logStatus.latency = 0;
                }
            } else if (statusCode == LogStatus.LogStatusCode.DISPATCH_FAILED) {
                logStatus.status = LogStatus.LogStatusCode.DISPATCH_FAILED;
                logStatus.recvTime = System.currentTimeMillis();
            }
        }
    }

    public LogStatus getLogStatusByID(long id) {
        if (tableLogStatus.containsKey(id)) {
            LogStatus logStatus = (LogStatus) tableLogStatus.get(id);
            return logStatus;
        }

        return null;
    }

    public EventLogNetworkInfo getLastLogsNetInfo() {
        return this.lastLogNetInfo;
    }

    public void setLastLogsNetInfo(LogStatus[] logStatus) {
        this.lastLogNetInfo = new EventLogNetworkInfo();
        this.lastLogNetInfo.analyzeNetInfo(logStatus);
    }

    public synchronized void deleteLog(long paramLong) {
        try {
            this.nStoredLogEvent -= this.databaseHelper.getWritableDatabase().delete("logs", "log_id = ?", new String[]{Long.toString(paramLong)});
        } catch (SQLiteException localSQLiteException) {
            Log.e(TAG, localSQLiteException.toString());
        }
    }

    public synchronized void deleteLogAll() {
        try {
            this.databaseHelper.getWritableDatabase().execSQL("delete from logs");
            this.nStoredLogEvent = 0;
        } catch (SQLiteException localSQLiteException) {
            Log.e(TAG, localSQLiteException.toString());
        }
    }

    public synchronized void deleteDB(String packageName) {
        String dbPath = "/data/data/" + packageName + "/databases/airplug_analytics.db";
        File file = new File(dbPath);
        if (file.delete())
            Log.d(TAG, "path=" + dbPath + " db file deleted");
        else
            Log.d(TAG, "delete DB failed");
    }

    public EventLog[] peekLogs() {
        return peekLogs(this.peekLogCnt);
    }

    public EventLog[] peekLogs(int paramInt) {
        ArrayList<EventLog> localArrayList = new ArrayList<EventLog>();
        tableLogStatus.clear();
        Cursor localCursor = null;

        try {
            SQLiteDatabase localSQLiteDatabase = this.databaseHelper.getReadableDatabase();
            localCursor = localSQLiteDatabase.query("logs", null, null, null, null, null, "log_id", Integer.toString(paramInt));
            while (localCursor.moveToNext()) {
                EventLog localObject1 = new EventLog(localCursor.getLong(0), localCursor.getString(1), localCursor.getLong(2));
                localArrayList.add(localObject1);

                LogStatus localObject2 = new LogStatus(localObject1.id);
                tableLogStatus.put(localObject2.id, localObject2);
            }
        } catch (SQLiteException localSQLiteException) {
            Log.e(TAG, localSQLiteException.toString());
            Object localObject1 = new EventLog[0];
            return (EventLog[]) localObject1;
        } finally {
            if (localCursor != null)
                localCursor.close();
        }
        return (EventLog[]) localArrayList.toArray(new EventLog[localArrayList.size()]);
    }

    public long putEvent(APEvent paramEvent) {
        long logID = -1;

        if (this.nStoredLogEvent >= MAX_LOGS) {
            Log.w(TAG, "Store full. Not storing last event.");
            return -1;
        }

        synchronized (this) {
            SQLiteDatabase localSQLiteDatabase = null;
            try {
                localSQLiteDatabase = this.databaseHelper.getWritableDatabase();
            } catch (SQLiteException localSQLiteException1) {
                Log.e(TAG, "Can't get db: " + localSQLiteException1.toString());
                return -1;
            }
            try {
                localSQLiteDatabase.beginTransaction();
                if (!this.sessionStarted)
                    storeUpdatedSession(localSQLiteDatabase);

                logID = putEvent(paramEvent, localSQLiteDatabase, true);

                localSQLiteDatabase.setTransactionSuccessful();
            } catch (SQLiteException localSQLiteException2) {
                Log.e(TAG, "putEventOuter:" + localSQLiteException2.toString());
                return -1;
            } finally {
                if (localSQLiteDatabase.inTransaction())
                    endTransaction(localSQLiteDatabase);
            }
        }

        return logID;
    }

    public long putEvent(APEvent paramEvent, SQLiteDatabase paramSQLiteDatabase, boolean paramBoolean) {
        if (!paramEvent.isSessionInitialized()) {
            paramEvent.setRandomVal(this.random.nextInt(2147483647));
            paramEvent.setTimestampFirst((int) this.tmFirst);
            paramEvent.setTimestampPrevious((int) this.tmPrevious);
            paramEvent.setTimestampCurrent((int) this.tmCurrent);
            paramEvent.setVisits(this.visits);
            paramEvent.setNumTotalLogs(this.nTotalLogEvents);
            paramEvent.setNumStoredLogs(this.nStoredLogEvent);
        }

        if (paramEvent.getUserId() == -1)
            paramEvent.setUserId(this.storeId);

        long rowID = writeEventToDatabase(paramEvent, paramSQLiteDatabase, paramBoolean);

        return rowID;
    }

    long writeEventToDatabase(APEvent paramEvent, SQLiteDatabase paramSQLiteDatabase, boolean paramBoolean) throws SQLiteException {

        ContentValues localContentValues = new ContentValues();
        String str = EventLogBuilder.constructLogRequestPath(paramEvent);
        localContentValues.put("log_string", str);
        localContentValues.put("log_time", Long.valueOf(paramBoolean ? System.currentTimeMillis() : 0L));
        long rowID = paramSQLiteDatabase.insert("logs", null, localContentValues);

        if (rowID >= 0) {
            this.nStoredLogEvent += 1;
            this.nTotalLogEvents += 1;
        }

        return rowID;
    }

    public void updateEventForMarkNetworkError(long logId) {
        if (logId < 0) {
            return;
        }

        synchronized (this) {
            SQLiteDatabase localSQLiteDatabase = null;

            try {
                localSQLiteDatabase = this.databaseHelper.getWritableDatabase();
                localSQLiteDatabase.beginTransaction();

                String sql = "select * from logs where log_id=" + logId;
                Cursor result = localSQLiteDatabase.rawQuery(sql, null);

                if (result.moveToFirst()) {
                    long logid = result.getLong(0);
                    String log_string = result.getString(1);

                    // add flag back of .apg?
                    int stidx = log_string.indexOf("bFailOver");
                    if (stidx > 0) {
                        log_string = log_string.replace("bFailOver=false", "bFailOver=true");
                        String strFilter = "log_id=" + logId;
                        ContentValues args = new ContentValues();
                        args.put("log_string", log_string);
                        int updateResult = localSQLiteDatabase.update("logs", args, strFilter, null);
                        localSQLiteDatabase.setTransactionSuccessful();
                    }
                }
            } catch (SQLiteException ex) {
                Log.d(TAG, "updateEventForMarkNetworkError error=" + ex.toString());
            } finally {
                localSQLiteDatabase.endTransaction();
            }
        }
    }

    public int getNumStoredLogs() {
        return this.nStoredLogEvent;
    }

    public int getNumStoredLogsFromDb() {
        int i = 0;
        SQLiteDatabase localSQLiteDatabase = null;
        Cursor localCursor = null;
        try {
            localSQLiteDatabase = this.databaseHelper.getReadableDatabase();
            localCursor = localSQLiteDatabase.rawQuery("SELECT COUNT(*) from logs", null);
            if (localCursor.moveToFirst())
                i = (int) localCursor.getLong(0);
        } catch (SQLiteException localSQLiteException) {
            Log.e(TAG, localSQLiteException.toString());
        } finally {
            if (localCursor != null)
                localCursor.close();
        }
        return i;
    }

    public int getTotalLogNumFromDb() {
        int totalLogs = -1;

        SQLiteDatabase localSQLiteDatabase = null;
        Cursor localCursor = null;
        try {
            localSQLiteDatabase = this.databaseHelper.getReadableDatabase();
            localCursor = localSQLiteDatabase.rawQuery("SELECT * from sqlite_sequence where name=\'logs\'", null);
            if (localCursor.moveToFirst())
                totalLogs = (int) localCursor.getLong(1);
        } catch (SQLiteException localSQLiteException) {
            Log.e(TAG, localSQLiteException.toString());
        } finally {
            if (localCursor != null)
                localCursor.close();
        }

        return totalLogs;
    }

    public int getStoreId() {
        return this.storeId;
    }

    public String getVisitorId() {
        if (!this.sessionStarted)
            return null;

        return String.format("%d%d", new Object[]{Long.valueOf(this.tmFirst), Integer.valueOf(this.storeId)});
    }

    public String getUUID() {
        if (tmFirst <= 0) {
            return null;
        }

        return String.format("%d%d", new Object[]{Long.valueOf(this.tmFirst), Integer.valueOf(this.storeId)});
    }

    public String getSessionId() {
        if (!this.sessionStarted)
            return null;
        return Integer.toString((int) this.tmCurrent);
    }

    public void loadExistingSession() {
        try {
            SQLiteDatabase localSQLiteDatabase = this.databaseHelper.getWritableDatabase();
            loadExistingSession(localSQLiteDatabase);
        } catch (SQLiteException localSQLiteException) {
            Log.e(TAG, localSQLiteException.toString());
        }
    }

    public void loadExistingSession(SQLiteDatabase paramSQLiteDatabase) {
        Cursor localCursor = null;
        try {
            localCursor = paramSQLiteDatabase.query("session", null, null, null, null, null, null);
            Object localObject1;
            if (localCursor.moveToFirst()) {
                this.tmFirst = localCursor.getLong(0);
                this.tmPrevious = localCursor.getLong(1);
                this.tmCurrent = localCursor.getLong(2);
                this.visits = localCursor.getInt(3);
                this.storeId = localCursor.getInt(4);
                if (this.tmFirst != 0L)
                    this.sessionStarted = true;
                else
                    this.sessionStarted = false;
            } else {
                this.sessionStarted = false;
                this.storeId = (new SecureRandom().nextInt() & 0x7FFFFFFF);
                localCursor.close();
                localCursor = null;
                localObject1 = new ContentValues();
                ((ContentValues) localObject1).put("tm_first", Long.valueOf(0L));
                ((ContentValues) localObject1).put("tm_previous", Long.valueOf(0L));
                ((ContentValues) localObject1).put("tm_current", Long.valueOf(0L));
                ((ContentValues) localObject1).put("visits", Integer.valueOf(0));
                ((ContentValues) localObject1).put("log_id", Integer.valueOf(this.storeId));
                paramSQLiteDatabase.insert("session", null, (ContentValues) localObject1);
            }
        } catch (SQLiteException ex) {
            Log.e(TAG, ex.toString());
        } finally {
            if (localCursor != null)
                localCursor.close();
        }
    }

    public synchronized void startNewVisit() {
        this.sessionStarted = false;
        this.nStoredLogEvent = getNumStoredLogsFromDb();
        this.nTotalLogEvents = getTotalLogNumFromDb() + 1;
    }

    void storeUpdatedSession(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase = this.databaseHelper.getWritableDatabase();
        paramSQLiteDatabase.delete("session", null, null);
        if (this.tmFirst == 0L) {
            long curTimestamp = System.currentTimeMillis() / 1000L;
            this.tmFirst = curTimestamp;
            this.tmPrevious = curTimestamp;
            this.tmCurrent = curTimestamp;
            this.visits = 1;
        } else {
            this.tmPrevious = this.tmCurrent;
            this.tmCurrent = (System.currentTimeMillis() / 1000L);
            if (this.tmCurrent == this.tmPrevious)
                this.tmCurrent += 1L;
            this.visits += 1;
        }
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("tm_first", Long.valueOf(this.tmFirst));
        localContentValues.put("tm_previous", Long.valueOf(this.tmPrevious));
        localContentValues.put("tm_current", Long.valueOf(this.tmCurrent));
        localContentValues.put("visits", Integer.valueOf(this.visits));
        localContentValues.put("log_id", Integer.valueOf(this.storeId));
        paramSQLiteDatabase.insert("session", null, localContentValues);
        this.sessionStarted = true;
    }

    public void setPeekLogCnt(int peekLogCnt) {
        if (peekLogCnt > 0 && peekLogCnt <= MAX_PEEK_LOGS) {
            this.peekLogCnt = peekLogCnt;
        } else {
            this.peekLogCnt = MAX_PEEK_LOGS;
        }
    }

    static class DataBaseHelper extends SQLiteOpenHelper {
        private final int databaseVersion;
        private final PersistentLogStore store;

        public DataBaseHelper(Context paramContext, PersistentLogStore paramPersistentLogStore) {
            this(paramContext, "airplug_analytics.db", 5, paramPersistentLogStore);
        }

        public DataBaseHelper(Context paramContext, String paramString, PersistentLogStore paramPersistentLogStore) {
            this(paramContext, paramString, 5, paramPersistentLogStore);
        }

        DataBaseHelper(Context paramContext, String paramString, int paramInt, PersistentLogStore paramPersistentLogStore) {
            super(paramContext, paramString, null, paramInt);
            this.databaseVersion = paramInt;
            this.store = paramPersistentLogStore;
        }

        public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
            createLogTable(paramSQLiteDatabase);
            createSessionTable(paramSQLiteDatabase);
        }

        public void onOpen(SQLiteDatabase paramSQLiteDatabase) {
            if (paramSQLiteDatabase.isReadOnly()) {
                return;
            }
        }

        private void createLogTable(SQLiteDatabase paramSQLiteDatabase) {
            paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS logs;");
            paramSQLiteDatabase.execSQL(PersistentLogStore.CREATE_LOGS_TABLE);
        }

        private void createSessionTable(SQLiteDatabase paramSQLiteDatabase) {
            paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS session;");
            paramSQLiteDatabase.execSQL(PersistentLogStore.CREATE_SESSION_TABLE);
        }

        public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int oldVersion, int newVersion) {
            if (oldVersion > newVersion) {
                return;
            }

            if (oldVersion < 2 && newVersion > 1) {
                createLogTable(paramSQLiteDatabase);
                createSessionTable(paramSQLiteDatabase);
            }
        }

        public void onDowngrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
            paramSQLiteDatabase.execSQL(PersistentLogStore.CREATE_LOGS_TABLE);
            paramSQLiteDatabase.execSQL(PersistentLogStore.CREATE_SESSION_TABLE);
        }
    }
}