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

import android.annotation.TargetApi;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

class EventLogBuilder {
    private final static String TAG = EventLogBuilder.class.getName();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static String constructEventRequestPath(APEvent paramEvent) {
        AirplugAnalyticTracker tracker = AirplugAnalyticTracker.getInstance();

        Locale localLocale = Locale.getDefault();
        StringBuilder reqURL = new StringBuilder();
        StringBuilder URLParam = new StringBuilder();

        Object eData = paramEvent.getExtData();
        String json = null;
        try {
            if (eData != null) {
                if (paramEvent.logType == LogType.CI.type) {
                    json = eData.toString();
                } else {
                    json = PersistentLogStore.gson.toJson(eData);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        URLParam.append("ver=").append(AirplugAnalyticTracker.VERSION);
        URLParam.append("&verCode=").append(paramEvent.getVersionCode());
        URLParam.append("&pkgName=").append(paramEvent.getPkgName());
        URLParam.append("&deviceID=").append(AirplugAnalyticTracker.ANDROID_ID);
        URLParam.append("&vID=").append(paramEvent.getTimestampFirst()).append(paramEvent.getUserId());
        URLParam.append("&osType=").append("A");
        URLParam.append("&osVer=").append(Build.VERSION.RELEASE);
        URLParam.append("&sdkVer=").append(Build.VERSION.SDK_INT);
        URLParam.append("&model=").append(encode(Build.MODEL));
        URLParam.append("&brand=").append(encode(Build.BRAND));
        URLParam.append("&sID=").append(paramEvent.getTimestampCurrent());
        URLParam.append("&sCnt=").append(paramEvent.getVisits());
        URLParam.append("&bFailOver=").append(false);
        URLParam.append("&numTotalHits=").append(
                paramEvent.getNumTotalLogs());
        URLParam.append("&nStoredLogEvent=").append(
                paramEvent.getNumStoredLogs());
        URLParam.append("&tkcode=").append(paramEvent.getRandomVal()); // tracking code

        double timestamp = System.currentTimeMillis() / 1000.0;
        String pattern = "##########.#####";
        DecimalFormat dformat = new DecimalFormat(pattern);
        URLParam.append("&tTM=").append(dformat.format(timestamp));
        URLParam.append(String.format(
                "&sr=%dx%d",
                new Object[]{Integer.valueOf(paramEvent.screenWidth),
                        Integer.valueOf(paramEvent.screenHeight)}));
        URLParam.append(String.format(
                "&locale=%s-%s",
                new Object[]{localLocale.getLanguage(),
                        localLocale.getCountry()}));

        String faultKey = paramEvent.getFaultKey();
        if (faultKey != null) {
            if (faultKey.equals("ftkey")) {
                URLParam.append("&ftkey=" + paramEvent.getFaultVal());
            }
        }

        JSONObject pData = new JSONObject();
        JSONObject jsonCellInfo = new JSONObject();
        JSONArray jsonCellList = new JSONArray();
        JSONObject jsonWifiInfo = new JSONObject();
        JSONObject jsonBatteryInfo = new JSONObject();
        JSONObject jsonNetInfo = new JSONObject();

        try {
            NetworkInfo netInfo = AirplugAnalyticTracker.connectivityManager.getActiveNetworkInfo();
            if (netInfo != null) {
                int type = netInfo.getType();
                jsonNetInfo.put("ntype", type);
                boolean isConnected = netInfo.isConnected();
                jsonNetInfo.put("bcon", isConnected);
                boolean isAvailable = netInfo.isAvailable();
                jsonNetInfo.put("bavail", isAvailable);
                int state = netInfo.getState().ordinal();
                jsonNetInfo.put("stat", state);
                int detailedState = netInfo.getDetailedState().ordinal();
                jsonNetInfo.put("dstat", detailedState);
                String reason = netInfo.getReason();
                jsonNetInfo.put("rsn", reason);
                boolean isFailover = netInfo.isFailover();
                jsonNetInfo.put("bfo", isFailover);
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            boolean bSim = false;
            if (AirplugAnalyticTracker.telephonyManager.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
                bSim = true;
            }
            jsonCellInfo.put("bsim", bSim);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            boolean mobileDataEnabled = false;
            Class cmClass = Class.forName(AirplugAnalyticTracker.connectivityManager.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            mobileDataEnabled = (Boolean) method.invoke(AirplugAnalyticTracker.connectivityManager);
            jsonCellInfo.put("bon", mobileDataEnabled);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            int airplaneMode = 0;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                airplaneMode = Settings.System.getInt(AirplugAnalyticTracker.context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
            } else {
                airplaneMode = Settings.Global.getInt(AirplugAnalyticTracker.context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
            }
            jsonCellInfo.put("armd", airplaneMode);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            jsonCellInfo.put("ptype", AirplugAnalyticTracker.telephonyManager.getPhoneType());
            jsonCellInfo.put("dtstat", AirplugAnalyticTracker.telephonyManager.getDataState());
            jsonCellInfo.put("dtact", AirplugAnalyticTracker.telephonyManager.getDataActivity());
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            String mcc = "";
            String mnc = "";
            String plmnid = AirplugAnalyticTracker.telephonyManager.getSimOperator();
            if (plmnid != null) {
                mcc = plmnid.substring(0, 3);
                mnc = plmnid.substring(3);
            }
            jsonCellInfo.put("mcc", mcc);
            jsonCellInfo.put("mnc", mnc);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            String ipAddress = "";

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipAddress = inetAddress.getHostAddress().toString();
                        jsonCellInfo.put(intf.getName(), ipAddress);
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // >= 17
                List<CellInfo> cellList = AirplugAnalyticTracker.telephonyManager.getAllCellInfo();

                for (CellInfo cellInfo : cellList) {
                    if (cellInfo.isRegistered()) {
                        JSONObject jsonSuppCellInfo = new JSONObject();
                        jsonSuppCellInfo.put("cistat", 0);

                        if (cellInfo != null) {
                            if (cellInfo instanceof CellInfoGsm) {
                                jsonSuppCellInfo.put("ctype", 0);
                                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
                                CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();

                                int crssi = cellSignalStrengthGsm.getDbm();
                                jsonSuppCellInfo.put("crssi", crssi);
                                int cid = cellIdentityGsm.getCid();
                                jsonSuppCellInfo.put("cid", cid);
                                int lac = cellIdentityGsm.getLac();
                                jsonSuppCellInfo.put("lac", lac);
                                int psc = cellIdentityGsm.getPsc();
                                jsonSuppCellInfo.put("psc", psc);

                            } else if (cellInfo instanceof CellInfoCdma) {
                                jsonSuppCellInfo.put("ctype", 1);
                                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                                CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
                                int crssi = cellSignalStrengthCdma.getDbm();
                                jsonSuppCellInfo.put("crssi", crssi);
                                int crssi_lvl = cellSignalStrengthCdma.getLevel();
                                jsonSuppCellInfo.put("csslvl", crssi_lvl);

                                int cddbm = cellSignalStrengthCdma.getCdmaDbm();
                                jsonSuppCellInfo.put("cddbm", cddbm);
                                int cdecio = cellSignalStrengthCdma.getCdmaEcio();
                                jsonSuppCellInfo.put("cdecio", cdecio);
                                int evdbm = cellSignalStrengthCdma.getEvdoDbm();
                                jsonSuppCellInfo.put("evdbm", evdbm);
                                int evsnr = cellSignalStrengthCdma.getEvdoSnr();
                                jsonSuppCellInfo.put("evsnr", evsnr);
                                int evecio = cellSignalStrengthCdma.getEvdoEcio();
                                jsonSuppCellInfo.put("evecio", evecio);

                                int bsid = cellIdentityCdma.getBasestationId();
                                jsonSuppCellInfo.put("bsid", bsid);
                                double lat = (double) cellIdentityCdma.getLatitude() / 14400;
                                jsonSuppCellInfo.put("lat", lat);
                                double lng = (double) cellIdentityCdma.getLongitude() / 14400;
                                jsonSuppCellInfo.put("lng", lng);
                                int netid = cellIdentityCdma.getNetworkId();
                                jsonSuppCellInfo.put("netid", netid);
                                int sysid = cellIdentityCdma.getSystemId();
                                jsonSuppCellInfo.put("sysid", sysid);
                            } else if (cellInfo instanceof CellInfoLte) {
                                jsonSuppCellInfo.put("ctype", 2);
                                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                                CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();

                                String tmp = cellSignalStrengthLte.toString();
                                int idxStart = tmp.indexOf(":");
                                String parsedInfo = tmp.substring(idxStart + 2);
                                String[] keyVals = parsedInfo.split(" ");
                                for (String keyVal : keyVals) {
                                    String[] tmpKeyVal = keyVal.split("=");
                                    String key = tmpKeyVal[0];
                                    String value = tmpKeyVal[1];
                                    if (key.equals("rsrp") || key.equals("rsrq") || key.equals("ss") || key.equals("ta") || key.equals("cqi") || key.equals("rssnr")) {
                                        jsonSuppCellInfo.put(key, Integer.parseInt(value));
                                    } else {
                                        jsonSuppCellInfo.put(key, value);
                                    }
                                }
                                jsonSuppCellInfo.put("asulvl", cellSignalStrengthLte.getAsuLevel());

                                int crssi = cellSignalStrengthLte.getDbm();
                                jsonSuppCellInfo.put("crssi", crssi);
                                int crssi_lvl = cellSignalStrengthLte.getLevel();
                                jsonSuppCellInfo.put("csslvl", crssi_lvl);
                                int ci = cellIdentityLte.getCi();
                                jsonSuppCellInfo.put("cid", ci);
                                int pci = cellIdentityLte.getPci();
                                jsonSuppCellInfo.put("pci", pci);
                                int tac = cellIdentityLte.getTac();
                                jsonSuppCellInfo.put("tac", tac);
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // >= 18
                                    if (cellInfo instanceof CellInfoWcdma) {
                                        jsonSuppCellInfo.put("ctype", 3);
                                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

                                        try {
                                            String strSignalInfo = cellSignalStrengthWcdma.toString();
                                            int idxStart = strSignalInfo.indexOf(":");
                                            String parsedInfo = strSignalInfo.substring(idxStart + 2);
                                            String[] keyVals = parsedInfo.split(" ");
                                            for (String keyVal : keyVals) {
                                                String[] tmpKeyVal = keyVal.split("=");
                                                String key = tmpKeyVal[0];
                                                String value = tmpKeyVal[1];
                                                jsonSuppCellInfo.put(key, value);
                                            }
                                        } catch (Exception ex) {
                                            Log.d(TAG, ex.toString());
                                        }

                                        CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                                        int crssi = cellSignalStrengthWcdma.getDbm();
                                        jsonSuppCellInfo.put("crssi", crssi);
                                        int crssi_lvl = cellSignalStrengthWcdma.getLevel();
                                        jsonSuppCellInfo.put("csslvl", crssi_lvl);
                                        int asulvl = cellSignalStrengthWcdma.getAsuLevel();
                                        jsonSuppCellInfo.put("asulvl", asulvl);

                                        String strCellIdentity = cellIdentityWcdma.toString();
                                        int cid = cellIdentityWcdma.getCid();
                                        jsonSuppCellInfo.put("cid", cid);
                                        int lac = cellIdentityWcdma.getLac();
                                        jsonSuppCellInfo.put("lac", lac);
                                        int psc = cellIdentityWcdma.getPsc();
                                        jsonSuppCellInfo.put("psc", psc);
                                    } else {
                                        jsonSuppCellInfo.put("cistat", 3);
                                    }
                                }
                            }
                        } else {
                            jsonSuppCellInfo.put("cistat", 2);
                        }

                        jsonCellList.put(jsonSuppCellInfo);
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "get cell signal strength failed. ex=" + ex.toString());
        }

        try {
            WifiInfo wifiInfo = AirplugAnalyticTracker.wifiManager.getConnectionInfo();
            String strWifiInfo = wifiInfo.toString();
            int wifiStatus = AirplugAnalyticTracker.wifiManager.getWifiState();
            jsonWifiInfo.put("wfstat", wifiStatus);

            String bssid = wifiInfo.getBSSID();
            jsonWifiInfo.put("bssid", bssid);

            String ssid = wifiInfo.getSSID();
            if (ssid != null) {
                if (ssid.contains("\"")) {
                    ssid = ssid.replace("\"", "");
                }
            }
            jsonWifiInfo.put("ssid", ssid);

            int linkSpeed = wifiInfo.getLinkSpeed();

            int rssi = wifiInfo.getRssi();
            jsonWifiInfo.put("rssi", rssi);

            SupplicantState supplicantState = wifiInfo.getSupplicantState();
            jsonWifiInfo.put("suppst", supplicantState.ordinal());

            List<ScanResult> scanResultList = AirplugAnalyticTracker.wifiManager.getScanResults();
            for (ScanResult scan : scanResultList) {
                if (bssid.equals(scan.BSSID)) {
                    jsonWifiInfo.put("freq", scan.frequency);
                    if (scan.capabilities.contains("VHT")) {
                        jsonWifiInfo.put("bgga", 1);
                    } else {
                        jsonWifiInfo.put("bgga", 0);
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "get wifiInfo failed. ex=" + ex.toString());
        }

        try {
            jsonBatteryInfo.put("hlth", BatteryReceiver.health);
            jsonBatteryInfo.put("plgd", BatteryReceiver.plugged);
            jsonBatteryInfo.put("scale", BatteryReceiver.scale);
            jsonBatteryInfo.put("stat", BatteryReceiver.status);
            jsonBatteryInfo.put("tmp", BatteryReceiver.temperature);
            jsonBatteryInfo.put("vlt", BatteryReceiver.voltage);
            jsonBatteryInfo.put("bttrlvl", BatteryReceiver.level);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        try {
            pData.put("pnet", jsonNetInfo);
            pData.put("pcell", jsonCellInfo);
            pData.put("pcell_list", jsonCellList);
            pData.put("pwf", jsonWifiInfo);
            pData.put("pbttr", jsonBatteryInfo);

            String pdata_str = pData.toString();
            URLParam.append("&pdata=").append(encode(pdata_str));
            Log.d(TAG, "pdata=" + pdata_str);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }

        if (paramEvent.getKey() != null) {
            URLParam.append("&evtKey=").append(encode(paramEvent.getKey()));
        }

        if (paramEvent.getValue() != null)
            URLParam.append("&evtValue=").append(encode(paramEvent.getValue()));


        int syncId = paramEvent.getSyncId();
        if (syncId > 0)
            URLParam.append("&evtSyncID=").append(syncId);

        reqURL.append(paramEvent.getEventUrl());

        if (json != null) {
            int msgLen = URLParam.length() + json.length();
            if (msgLen >= (NetworkDispatcher.MAX_POST_LENGTH * 0.9)) {
                Log.d(TAG, "edata length(" + msgLen + ") is so long. eData wiil be truncated");
                json = "";
            }
        }

        if (tracker.getEnc()) {
            try {
                if (json != null) {
                    if (paramEvent.logType.equals("DB")) {
                        URLParam.append("&eData=").append(encode(json));
                        reqURL.append("/raw.apg?");
                    } else {
                        URLParam.append("&eData=");
                        URLParam.append(SimpleCrypto.ENC_HEADER_MAGIC_NUM);
                        URLParam.append(SimpleCrypto.ENC_HEADER_FORMAT_VER);
                        URLParam.append(SimpleCrypto.ENC_HEADER_KEY);
                        URLParam.append(SimpleCrypto.encrypt(json));

                        reqURL.append("/enc.apg?");
                    }
                } else {
                    reqURL.append("/enc.apg?");
                }

                reqURL.append(URLParam.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (json != null)
                URLParam.append("&eData=").append(encode(json));
            reqURL.append("/raw.apg?");
            reqURL.append(URLParam.toString());
        }

        return reqURL.toString();
    }

    public static String constructLogRequestPath(APEvent paramEvent) {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append(constructEventRequestPath(paramEvent));
        return localStringBuilder.toString();
    }

    public static String getEscapedCookieString(APEvent paramEvent) {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("__utma=");
        localStringBuilder.append("1").append(".");
        localStringBuilder.append(paramEvent.getUserId()).append(".");
        localStringBuilder.append(paramEvent.getTimestampFirst()).append(".");
        localStringBuilder.append(paramEvent.getTimestampPrevious()).append(".");
        localStringBuilder.append(paramEvent.getTimestampCurrent()).append(".");
        localStringBuilder.append(paramEvent.getVisits()).append(";");

        return encode(localStringBuilder.toString());
    }

    private static String encode(String paramString) {
        return AnalyticsParameterEncoder.encode(paramString);
    }

    public static final String getCellInfo() {
        String cellInfo = "";
        String cellId = "";
        String lac = "";

        try {
            CellLocation location = getCellLocation();
            if (location == null) return "";

            if (CdmaCellLocation.class.isInstance(location)) {
                CdmaCellLocation loc = (CdmaCellLocation) location;

                cellId += loc.getNetworkId();

            } else if (GsmCellLocation.class.isInstance(location)) {
                GsmCellLocation loc = (GsmCellLocation) location;

                int cid = loc.getCid();
                int lo = loc.getLac();
                if (lo <= Integer.valueOf("ffff", 16) && lo > 0) {
                    cellId += cid;
                    lac += lo;
                } else {
                    return "";
                }
            }
        } catch (NoClassDefFoundError e) {
            return "";
        }


        return cellInfo;
    }

    public static final CellLocation getCellLocation() {
        try {
            return AirplugAnalyticTracker.telephonyManager.getCellLocation();
        } catch (SecurityException e) {
            return null;
        }
    }
}