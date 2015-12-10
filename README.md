
dd-collector
===

dd-collector is the logging part of **DD Insight**, which consists of two sub projects; [**mobile SDK**]() to deliver information from mobile devices to a server and [**collect server**]() to receive/save the logs securely in server-side.

----------

Mobile SDK
---

**Features**
> - Persistent logging
>  * A mechanism is provided to prevent logs from being lost due to network problems. In such mechanism, when logs are generated to be sent, they are first saved on local dB on the device first and later transmitted when network is available. 
> - Burst sending
>  * HTTP1.1/Pipelining is used to transmit huge quantity of saved log messages to the server effectively and fast without too much overhead
> - Permission-scalable collecting
>  * Provides data structure that can be automatically modified and collects only the data that are permitted under permission of parent app
> - Thread-safe
>  * Can track(save) all the event logs from multiple thread source that run on mobile multi-thread environment simultaneously
> - Macro-gathering of cellular information
> - Macro-gathering of Wi-Fi information
> - Macro-gathering of system information

### 
**Developer Guide**

> 1. Git Clone
> git clone http://address
> 2. Set your destination
> modify the value *AP_ANALYTICS_HOST_NAME* in *NetworkDispatcher.java* file in *src/com/airplug/android/apps/analytics* directory to your collector server address
> 3. Build a library
> the following will  generate a library for app (apat.jar)
> ```
> ./gradlew makeJar
> ```
> 4. Import the library to your app (use Android Studio or Eclipse)
> 5. Add the following Android permissions into your app if allowed
> - android.permission.INTERNET
    - android.permission.ACCESS_NETWORK_STATE
    - android.permission.WRITE_EXTERNAL_STORAGE
    - android.permission.ACCESS_FINE_LOCATION
    - android.permission.ACCESS_WIFI_STATE
> 6.  Insert logging codes upon your needs in your app by referring to the developer guide document included in the source or the [Simple Usage Snippet]() below
> 7. Build your app

### 

**Simple Usage Snippet**

*1. Init*
```
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    
    	// get tracker singleton instance
    	tracker = AirplugAnalyticTracker.getInstance();
    
    	// start
        tracker.start("demo@example.com",  // account
    		"1001",        // version code
    		-1,            // dispatch period (sec), -1=off
    		false,         // encryption
    		this,          // android context
    		"demo.example.com",  // collect server hostname
    		80,            // server port
    		false);        // continuous cell-info recording 
    }
```

*2. Track*
```
    public void catchEvent(){
        // get tracker
    	tracker = AirplugAnalyticTracker.getInstance();
    	
    	// track 
    	Object customLog = new Object();
    	tracker.trackEvent(LogType, customLog)
    }
```

*3. Dispatch*
```
    public void sendTrackedEvent(){
    	// get tracker
    	tracker = AirplugAnalyticTracker.getInstance();
    	
    	// dispatch tracked event
    	tracker.dispatch();
    }
```

*4. Stop*
```
    public void onDestroy(){
        // get tracker
    	tracker = AirplugAnalyticTracker.getInstance();
    	
    	// stop
    	tracker.stop();
    }
```

 

----------

Collect Server
---



 **Features**

> - fast and massive logging server implemented on a lightweight web server (flask)
> - scalability and stability by introducing a flexible web gateway (uwsgi)
> - flexible validation of log messages
> - configurable structure for message filtering
> - ability to process concurrent multiple versions of message

### 
**Getting Started:**
> 1. git clone
> 2. run a docker script with the files in *'collect_server/docker'* directory

### 
**Sample Mobile Log Format**
> The minimum log fields that are required to execute netview demonstration in the DD Insight open-source project are listed in the  [sample mobile log format]() page

### 

---------- 
**Authors & Maintainers**
> - Daniel Moon (mobile SDK)
> - Spring Choi (collect server)

### 
**Contributors**
> [See contributors on Github](http://)

### 
**License**
> dd-collector is released under [Apache v2 License](http://)

 
Copyright 2015 - AirPlug Inc.
