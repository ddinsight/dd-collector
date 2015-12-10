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

import org.apache.http.Header;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.params.BasicHttpParams;

import java.io.IOException;
import java.net.Socket;

public class PipelinedRequester {
    DefaultHttpClientConnection connection = new DefaultHttpClientConnection();
    Callbacks callbacks;
    int lastStatusCode;
    boolean canPipeline = true;
    HttpHost host;
    SocketFactory socketFactory;

    public PipelinedRequester(HttpHost paramHttpHost) {
        this(paramHttpHost, new PlainSocketFactory());
    }

    public PipelinedRequester(HttpHost paramHttpHost, SocketFactory paramSocketFactory) {
        this.host = paramHttpHost;
        this.socketFactory = paramSocketFactory;
    }

    public void installCallbacks(Callbacks paramCallbacks) {
        this.callbacks = paramCallbacks;
    }

    public void addRequest(HttpEntityEnclosingRequest paramHttpEntityEnclosingRequest) throws HttpException, IOException {
        maybeOpenConnection();
        this.connection.sendRequestHeader(paramHttpEntityEnclosingRequest);
        this.connection.sendRequestEntity(paramHttpEntityEnclosingRequest);
    }

    public void sendRequests() throws IOException, HttpException {
        this.connection.flush();
        HttpConnectionMetrics localHttpConnectionMetrics = this.connection.getMetrics();
        while (localHttpConnectionMetrics.getResponseCount() < localHttpConnectionMetrics.getRequestCount()) {
            HttpResponse localHttpResponse = this.connection.receiveResponseHeader();
            if (!localHttpResponse.getStatusLine().getProtocolVersion().greaterEquals(HttpVersion.HTTP_1_1)) {
                this.callbacks.pipelineModeChanged(false);
                this.canPipeline = false;
            }
            Header[] arrayOfHeader1 = localHttpResponse.getHeaders("Connection");
            if (arrayOfHeader1 != null)
                for (Header localHeader : arrayOfHeader1) {
                    if (!"close".equalsIgnoreCase(localHeader.getValue()))
                        continue;
                    this.callbacks.pipelineModeChanged(false);
                    this.canPipeline = false;
                }
            this.lastStatusCode = localHttpResponse.getStatusLine().getStatusCode();
            if (this.lastStatusCode != 200) {
                this.callbacks.serverError(this.lastStatusCode);
                closeConnection();
                return;
            }
            this.connection.receiveResponseEntity(localHttpResponse);
            localHttpResponse.getEntity().consumeContent();
            this.callbacks.requestSent();

            if (!this.canPipeline) {
                closeConnection();
                return;
            }
        }
    }

    public void finishedCurrentRequests() {
        closeConnection();
    }

    private void maybeOpenConnection() throws IOException {
        if ((this.connection == null) || (!this.connection.isOpen())) {
            BasicHttpParams localBasicHttpParams = new BasicHttpParams();
            Socket localSocket = this.socketFactory.createSocket();
            localSocket = this.socketFactory.connectSocket(localSocket, this.host.getHostName(), this.host.getPort(), null, 0, localBasicHttpParams);
            localSocket.setReceiveBufferSize(8192);
            this.connection.bind(localSocket, localBasicHttpParams);
        }
    }

    private void closeConnection() {
        if ((this.connection != null) && (this.connection.isOpen()))
            try {
                this.connection.close();
            } catch (IOException localIOException) {

            }
    }

    static abstract interface Callbacks {
        public abstract void pipelineModeChanged(boolean paramBoolean);

        public abstract void serverError(int paramInt);

        public abstract void requestSent();

        public void serverConnectionFailed();
    }
}