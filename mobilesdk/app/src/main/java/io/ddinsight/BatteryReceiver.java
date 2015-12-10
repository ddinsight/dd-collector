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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * Created by sysmoon on 15. 11. 17..
 */
public class BatteryReceiver extends BroadcastReceiver{

    public static int health = -1;
    public static int plugged = -1;
    public static int scale = -1;
    public static int status = -1;
    public static int temperature = -1;
    public static int voltage = -1;
    public static int level = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

        this.health = health;
        this.level = level;
        this.plugged = plugged;
        this.scale = scale;
        this.status = status;
        this.voltage = voltage;
        this.temperature = temperature;
    }
}
