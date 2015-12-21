## Sample Mobile Log Format

#### **Basic data format**
> Client-side data needed for analytics are sent in the URL query string format
>- to be sent by HTTP/GET URL or HTTP/POST body
>- mobileHeader1=value&mobileHeader2=value&...&eData=*JSON-formatted-text*


#### **Part I of eData - required for analytics with  live video**

Field name | Value | Remarks
---------- | ----- | ----------
confOperator | 45005 | MCC (3-digits) & MNC (2-digits)
liveCurrentTSBitrate | 2233 | bitrate in kbps of the live video
netActiveNetwork | 1440392367\|C\|Mobile | timestamp \| network-status \| network-type (\| ssid \| bssid \| ip added for Wi-Fi)
netCID | 5782075 | cellular ID (from android API)
netCellState | 0 | decimal converted value of 3-digit binary flag for cellular availability of the device (data-not-allowed(1<<2) \| airplane-mode-on(1<<1) \| no-SIM-card(1) etc)
netLAC | 5633 | location area code (from android API)
playAccBufferingTime | 24 | accumulated stalled time for the video session reported (sec)
playAppPackageName | com.example.tv/01.02.34/10234 | the package name of the app which the mobileSDK is integrated into
playPlayingTime | 100 | playing time of the video session reported (sec)
playPreparingTime | 10.355 | initial start-up time of the video session reported (sec)
playServiceMode | 2 | the type of the application session (2=live video)
playSessionId | 16fc8d0e09644732_1440392276384 | unique session ID of the video session reported (androidID_timestamp) 


#### **Part II of eData - filled with fixed or arbitrary values for tests**
- *The following fields are not necessary for analytics but recommended to set to the fixed values for smooth tests (for now)*
- *These fields shall be modified or deleted in the future upgrade*

Field name | Value | Remarks
---------- | ----- | ----------------
agentLogEndTime	| 1440392367 | set the UNIX timestamp at log-ending (or set to an arbitrary value for tests)
agentLogStartTime	| 1440392367 | set the UNIX timestamp at log-starting (or set to an arbitrary value for tests)
agentAatOnOff | TRUE | set to TRUE for tests
agentLogType	 | 1  | set to 1 for tests
bbCount    | 0 | set to 0 for tests
bbList      |[]| set to null for tests
playContentId | video_0.0.0.0@testvideo.m3u8 |  set to an ID strings for tests
playHost | DD | set to an ID strings (for DD-agent type) for tests
playOrigin | 0.0.0.0 | set to an IP address (of the video origin) for tests
playTitle | TestVideo | set to strings (for the video title) for tests
tkcode | 432526523 | set to an ID number (for tracking code) for tests



#### **Mobile Headers** 

- *These fields are filled automatically by the function call*

### 
Field name (Headers) | Value | Remarks
---------- | ----- | ----------
brand  | google | the name of the device brand
deviceID   | 16fc8d0e09644732 | AndroidID for android 
log_type | xxxlog | eData log type identifier
model | NexusX | android device model
ntype   | 0   | network type at logging (0=mobile)
numTotalHits | 1000 | the number of total logging hits since the initialization of the library 
osType | A | OS type and A = Android
osVer | 6.0.1 | android os version
mcc | 450 | MCC (mobile country code)
mnc | 05 | MNC (mobile network code)
cid | 5782075 | cell id
lac | 5633 | LAC (location area code)
pkgName | com.example.app | the name of the pakage which the collector library integrated into
pwf.rssi | -100 | Wi-Fi singal strength if available
pwf.ssid | APNET | Wi-Fi SSID if available
pwf.bssid | 00:00:00:00:00 | Wi-Fi BSSID if available
sID | 1447816502 | session id of mobile tracking
tTM | 1447817103.453 | log tracked time (timestamp)
vID | 14478165022109379694 | unique visitor id (timestamp + random)
ver | 2.0.3 | version of the mobileSDK
verCode | 1 | version code from the parent app package
