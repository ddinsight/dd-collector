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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import android.net.TrafficStats;
import android.net.Uri;

public class Utils {
    private final static String TAG = Utils.class.getName();

    static Map<String, String> parseURLParameters(String paramString) {
        HashMap localHashMap = new HashMap();
        String[] arrayOfString1 = paramString.split("&");
        for (String str : arrayOfString1) {
            String[] arrayOfString3 = str.split("=");
            if (arrayOfString3.length > 1) {
                localHashMap.put(arrayOfString3[0], arrayOfString3[1]);
            } else {
                if (arrayOfString3.length != 1)
                    continue;
                localHashMap.put(arrayOfString3[0], null);
            }
        }
        return localHashMap;
    }

    static String addQueueTimeParameter(String paramString, long paramLong) {
        String str1 = paramString;
        Uri localUri = Uri.parse(paramString);
        String str2 = localUri.getQueryParameter("utmht");
        if (str2 != null)
            try {
                Long localLong = Long.valueOf(Long.parseLong(str2));
                str1 = str1 + "&utmqt=" + (paramLong - localLong.longValue());
            } catch (NumberFormatException localNumberFormatException) {
                Log.e(TAG, "Error parsing utmht parameter: " + localNumberFormatException.toString());
            }
        return str1;
    }

    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.d(TAG, ex.toString());
        } catch (NullPointerException ex) {
            Log.d(TAG, ex.toString());
        }

        return null;
    }

    public static HashMap<String, Long> getTraffic() {
        HashMap<String, Long> hashMap = new HashMap<>();

        long mobileRxBytes = TrafficStats.getMobileRxBytes();
        long mobileTxBytes = TrafficStats.getMobileTxBytes();
        long totalRxBytes = TrafficStats.getTotalRxBytes();
        long totalTxbytes = TrafficStats.getTotalTxBytes();

        long wifiRxbytes = totalRxBytes - mobileRxBytes;
        long wifiTxbytes = totalTxbytes - mobileTxBytes;

        // total traffic
        hashMap.put("totRxB", totalRxBytes);
        hashMap.put("totTxB", totalTxbytes);

        // mobile traffic
        hashMap.put("mobRxB", mobileRxBytes);
        hashMap.put("mobTxB", mobileTxBytes);

        return hashMap;
    }
}
