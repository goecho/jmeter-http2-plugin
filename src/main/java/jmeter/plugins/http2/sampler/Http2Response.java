/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package jmeter.plugins.http2.sampler;

import io.netty.handler.codec.http.FullHttpResponse;

public class Http2Response {

    private FullHttpResponse fullHttpResponse;

    private String context;

    public Http2Response(FullHttpResponse fullHttpResponse, String context) {
        this.fullHttpResponse = fullHttpResponse;
        this.context = context;
    }

    public FullHttpResponse getFullHttpResponse() {
        return fullHttpResponse;
    }

    public void setFullHttpResponse(FullHttpResponse fullHttpResponse) {
        this.fullHttpResponse = fullHttpResponse;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
