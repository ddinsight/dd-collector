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
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * 현재 단말이 접속한 Serving Cell과 주위 Neighbor Cell 정보를 수집하는 클래스
 */
public class ApCellInfo {
    final static TelephonyManager telephonyManager = (TelephonyManager) AirplugAnalyticTracker.context.getSystemService(Context.TELEPHONY_SERVICE);
    final static ConnectivityManager connectivityManager = (ConnectivityManager) AirplugAnalyticTracker.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    private final static String TAG = ApCellInfo.class.getName();
    private final static int MAX_NEIGHBOR_CELL_SIZE = 3;

    /**
     *
     * @param mode 0:
     * @return
     */
    public static JSONArray getCellInfo(int mode) {
        JSONArray jsonCellList = new JSONArray();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.d(TAG, "build version is not support cellinfo api");
            return null;
        }

        try {
            List<CellInfo> cellList = AirplugAnalyticTracker.telephonyManager.getAllCellInfo();

            long scantime = System.currentTimeMillis();
            for (CellInfo cellInfo : cellList) {
                JSONObject jsonSuppCellInfo = new JSONObject();
                jsonSuppCellInfo.put("scantime", scantime);
                jsonSuppCellInfo.put("cistat", 0);

                // MCC/MNC
                try {
                    String mcc = "";
                    String mnc = "";
                    String plmnid = AirplugAnalyticTracker.telephonyManager.getSimOperator();
                    if (plmnid != null) {
                        mcc = plmnid.substring(0, 3);
                        mnc = plmnid.substring(3);
                    }
                    jsonSuppCellInfo.put("mcc", Integer.parseInt(mcc));
                    jsonSuppCellInfo.put("mnc", Integer.parseInt(mnc));
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                }

                if (cellInfo != null) {
                    if (cellInfo instanceof CellInfoGsm) {
                        jsonSuppCellInfo.put("ctype", 0);
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
                        CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();

                        int crssi = cellSignalStrengthGsm.getDbm();
                        jsonSuppCellInfo.put("crssi", crssi);
                        int cid = cellIdentityGsm.getCid();
                        if (cid == 2147483647) {
                            cid = 0;
                        }
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
                        if (ci == 2147483647) {
                            ci = 0;
                        }
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
                                if (cid == 2147483647) {
                                    cid = 0;
                                }
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

                if (cellInfo.isRegistered() && mode == 0) {
                    jsonCellList.put(jsonSuppCellInfo);
                } else if (!cellInfo.isRegistered() && mode == 1) {
                    if (jsonCellList.length() < MAX_NEIGHBOR_CELL_SIZE) {
                        jsonCellList.put(jsonSuppCellInfo);
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "get cell signal strength failed. ex=" + ex.toString());
        }

        return jsonCellList;
    }
}