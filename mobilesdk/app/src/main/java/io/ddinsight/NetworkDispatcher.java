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
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import io.ddinsight.LogStatus.LogStatusCode;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

public class NetworkDispatcher implements Dispatcher
{
  public static final int MAX_GET_LENGTH = 4096;
  public static final int MAX_POST_LENGTH = 8192;
  private final static String TAG = NetworkDispatcher.class.getName();
  private final static String AP_ANALYTICS_HOST_NAME = "logserver.example.com";
  private final static int AP_ANALYTICS_HOST_PORT = 80;
  private static final String USER_AGENT_TEMPLATE = "%s/%s (Linux; U; Android %s; %s-%s; %s Build/%s)";
  private static final int MAX_EVENTS_PER_PIPELINE = 30;
  private static final int MAX_SEQUENTIAL_REQUESTS = 5;
  private static final int FAIL_OVER_GAP = 2000;

  private final String userAgent;
  private final HttpHost airplugAnalyticsHost;
  private DispatcherThread dispatcherThread;
  private ConnectivityManager connectivityManager;

  public NetworkDispatcher()
  {
    this(AirplugAnalyticTracker.PRODUCT, AirplugAnalyticTracker.VERSION, AP_ANALYTICS_HOST_NAME, AP_ANALYTICS_HOST_PORT);
  }

  NetworkDispatcher(String product, String version, String host, int port)
  {
	if (this.connectivityManager == null) {
      connectivityManager = (ConnectivityManager) AirplugAnalyticTracker.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    this.airplugAnalyticsHost = new HttpHost(host, port);
    Locale localLocale = Locale.getDefault();
    this.userAgent = String.format("%s/%s (airplugAnalytics; U; Android %s; %s-%s; %s Build/%s)",
            new Object[] {
                    product,
                    version,
                    Build.VERSION.RELEASE,
                    localLocale.getLanguage() != null ? localLocale.getLanguage().toLowerCase() : "en", localLocale.getCountry() != null ? localLocale.getCountry().toLowerCase() : "",
                    Build.MODEL,
                    Build.ID });
  }

  public void init(Dispatcher.Callbacks paramCallbacks, LogStore logStore)
  {
    stop();
    this.dispatcherThread = new DispatcherThread(paramCallbacks, this.userAgent, this, logStore);
    this.dispatcherThread.start();
  }

  public void init(Dispatcher.Callbacks paramCallbacks, PipelinedRequester paramPipelinedRequester, LogStore logStore)
  {
    stop();
    this.dispatcherThread = new DispatcherThread(paramCallbacks, paramPipelinedRequester, this.userAgent, this, logStore);
    this.dispatcherThread.start();
  }

  public void dispatchLogs(EventLog[] paramArrayOfEventLog)
  {
    if (this.dispatcherThread == null)
      return;
    this.dispatcherThread.dispatchLogs(paramArrayOfEventLog);
  }

  void waitForThreadLooper()
  {
    this.dispatcherThread.getLooper();
    while (this.dispatcherThread.handlerExecuteOnDispatcherThread == null)
      Thread.yield();
  }

  public void stop()
  {
    if ((this.dispatcherThread != null) && (this.dispatcherThread.getLooper() != null))
    {
      this.dispatcherThread.getLooper().quit();
      this.dispatcherThread = null;
    }
  }

  String getUserAgent()
  {
    return this.userAgent;
  }
  
  public int getActiveNetType(){
		NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
		return netInfo.getType();
  }
  
  private static class DispatcherThread extends HandlerThread
  {
    private final PipelinedRequester pipelinedRequester;
    private final String userAgent;
    private final Dispatcher.Callbacks callbacks;
    private final RequesterCallbacks requesterCallBacks;
    private final NetworkDispatcher parent;
    volatile Handler handlerExecuteOnDispatcherThread;
    private int lastStatusCode;
    private int maxEventsPerRequest = 30;
    private long retryInterval;
    private AsyncDispatchTask currentTask = null;
    private LogStore logStore;
    
    private DispatcherThread(Dispatcher.Callbacks paramCallbacks, String paramString, NetworkDispatcher paramNetworkDispatcher, LogStore logStore)
    {
      this(paramCallbacks, new PipelinedRequester(paramNetworkDispatcher.airplugAnalyticsHost), paramString, paramNetworkDispatcher, logStore);
    }

    private DispatcherThread(Dispatcher.Callbacks paramCallbacks, PipelinedRequester paramPipelinedRequester, String paramString, NetworkDispatcher paramNetworkDispatcher, LogStore logStore)
    {
      super("dispatcher thread");
      this.callbacks = paramCallbacks;
      this.userAgent = paramString;
      this.pipelinedRequester = paramPipelinedRequester;
      this.requesterCallBacks = new RequesterCallbacks();
      this.pipelinedRequester.installCallbacks(this.requesterCallBacks);
      this.parent = paramNetworkDispatcher;
      this.logStore = logStore;
    }

    protected void onLooperPrepared()
    {
      this.handlerExecuteOnDispatcherThread = new Handler();
    }

    public void dispatchLogs(EventLog[] paramArrayOfEventLog)
    {
      if (this.handlerExecuteOnDispatcherThread == null)
        return;
      
      currentTask = new AsyncDispatchTask(paramArrayOfEventLog);
      this.handlerExecuteOnDispatcherThread.post(currentTask);
      //this.handlerExecuteOnDispatcherThread.post(new AsyncDispatchTask(paramArrayOfEventLog));
    }

    private class RequesterCallbacks implements PipelinedRequester.Callbacks
    {
      private RequesterCallbacks()
      {
      }

      @Override
      public void pipelineModeChanged(boolean paramBoolean)
      {
        // do nothing. need to be developed
      }

      @Override
      public void requestSent()
      {
          if (NetworkDispatcher.DispatcherThread.this.currentTask == null){
              return;
          }

          EventLog localEventLog = NetworkDispatcher.DispatcherThread.this.currentTask.removeNextLog();
          if(localEventLog != null)
          {
              logStore.updateLogStatus(localEventLog.id, LogStatusCode.DISPATCH_COMPLETED);
              NetworkDispatcher.DispatcherThread.this.callbacks.logDispatched(localEventLog.id);
          }
      }
        @Override
        public void serverConnectionFailed() {

        }

        @Override
        public void serverError(int paramInt)
        {
            Log.i(TAG, "server error. server status code:" + paramInt);
            if(paramInt == 403){
                EventLog localEventLog = NetworkDispatcher.DispatcherThread.this.currentTask.removeNextLog();
                if(localEventLog != null)
                {
                    Log.d(TAG, "403 forbidden. delete logID=" + localEventLog.id);
                    logStore.updateLogStatus(localEventLog.id, LogStatusCode.DISPATCH_FAILED);
                    NetworkDispatcher.DispatcherThread.this.callbacks.logDispatched(localEventLog.id);
                }
            }
        }
    }

    private class AsyncDispatchTask extends APRunnable
    {
      private final LinkedList<EventLog> eventLogs = new LinkedList();

      public AsyncDispatchTask(EventLog[] eventLogs)
      {
        Collections.addAll(this.eventLogs, eventLogs);
        
        for(int i=0; i< eventLogs.length; i++)
        {
        	long key = eventLogs[i].id;
        }
      }

      public void runs()
      {
        for (int i = 0; (i < 1) && (this.eventLogs.size() > 0); i++)
        {
          try
          {       	  
        	  long l = 0L;
        	  if ((NetworkDispatcher.DispatcherThread.this.lastStatusCode == 500) || (NetworkDispatcher.DispatcherThread.this.lastStatusCode == 503))
              {
        		  l = 5L;
        		  Thread.sleep(l * 1000L);
              }
              
        	  dispatchSomePendingLogs(true);
          }
          catch (InterruptedException localInterruptedException)
          {
            Log.w(TAG, "Couldn't sleep.", localInterruptedException);
            break;
          }
          catch (IOException localIOException)
          {
            Log.w(TAG, "Problem with socket or streams.", localIOException);
            break;
          }
          catch (HttpException localHttpException)
          {
            Log.w(TAG, "Problem with http streams.", localHttpException);
            break;
          }
          catch(Throwable e)
          {
        	  Log.w(TAG, e);
        	  break;
          }
        }
        
        NetworkDispatcher.DispatcherThread.this.pipelinedRequester.finishedCurrentRequests();
        NetworkDispatcher.DispatcherThread.this.callbacks.dispatchFinished();
      }

      private void dispatchSomePendingLogs(boolean paramBoolean) throws IOException, ParseException, HttpException
      {
        for (int i = 0; (i < this.eventLogs.size()) && (i < NetworkDispatcher.DispatcherThread.this.maxEventsPerRequest); i++)
        {
          EventLog localEventLog = (EventLog)this.eventLogs.get(i);
          String str1 = localEventLog.eventLogString;
          
          // add nType
          int index = str1.indexOf('?');
          int nType = parent.getActiveNetType();
          String newUrl = str1.substring(0, index+1);
          newUrl += "nType=" + nType;
          newUrl += "&" + str1.substring(index+1);
          
          str1 = newUrl;
          
          int j = str1.indexOf('?');
          
          String str2;
          String str3;
          if (j < 0)
          {
            str2 = str1;
            str3 = "";
          }
          else
          {
            if (j > 0)
              str2 = str1.substring(0, j);
            else
              str2 = "";
            if (j < str1.length() - 2)
              str3 = str1.substring(j + 1);
            else
              str3 = "";
          }
          BasicHttpEntityEnclosingRequest localBasicHttpEntityEnclosingRequest = null;
          int length = str3.length();
       	  if (str3.length() < MAX_GET_LENGTH)
          {
            localBasicHttpEntityEnclosingRequest = new BasicHttpEntityEnclosingRequest("GET", str1);
          }else
          {
        	localBasicHttpEntityEnclosingRequest = new BasicHttpEntityEnclosingRequest("POST", str2);
            localBasicHttpEntityEnclosingRequest.addHeader("Content-Length", Integer.toString(str3.length()));
            localBasicHttpEntityEnclosingRequest.addHeader("Content-Type", "text/plain");
            localBasicHttpEntityEnclosingRequest.setEntity(new StringEntity(str3));
          }
       	  
       	  Log.i(TAG, "request method is " + localBasicHttpEntityEnclosingRequest.getRequestLine().getMethod());

          String str4 = AP_ANALYTICS_HOST_NAME + ":" + AP_ANALYTICS_HOST_PORT;
          localBasicHttpEntityEnclosingRequest.addHeader("Host", str4);
          localBasicHttpEntityEnclosingRequest.addHeader("User-Agent", NetworkDispatcher.DispatcherThread.this.userAgent);
        	  
          if(!paramBoolean)
          {
            StringBuffer localStringBuffer = new StringBuffer();
            for (Header localHeader : localBasicHttpEntityEnclosingRequest.getAllHeaders())
              localStringBuffer.append(localHeader.toString()).append("\n");
            localStringBuffer.append(localBasicHttpEntityEnclosingRequest.getRequestLine().toString()).append("\n");
            Log.i(TAG, localStringBuffer.toString());
          }
          
          if (str3.length() > MAX_POST_LENGTH)
          {
            Log.w(TAG, "EventLog too long (>" + MAX_POST_LENGTH + " bytes)--not sent");
            NetworkDispatcher.DispatcherThread.this.requesterCallBacks.requestSent();
          }
          else
          {
            logStore.updateLogStatus(localEventLog.id, LogStatusCode.DISPATCH_TRYING);
            long dispatchCallTime = System.currentTimeMillis();
            try{
        	    NetworkDispatcher.DispatcherThread.this.pipelinedRequester.addRequest(localBasicHttpEntityEnclosingRequest);
            }catch(IOException ex){
                try{
                    long gap = dispatchCallTime - localEventLog.eventLog_time;
                    if(gap >= 0 && gap <= FAIL_OVER_GAP){
                      logStore.updateEventForMarkNetworkError(localEventLog.id);
                    }
                }catch (Exception internal_ex){
                    Log.d(TAG, internal_ex.toString());
                }
                throw ex;
            }
          }
          
          Log.i(TAG, "dispatch msg. " + localBasicHttpEntityEnclosingRequest.getRequestLine());
        }
        
        if (paramBoolean)
          NetworkDispatcher.DispatcherThread.this.pipelinedRequester.sendRequests();
      }

      public EventLog removeNextLog()
      {
    	  return (EventLog)this.eventLogs.poll();
      }
    }
  }
}
