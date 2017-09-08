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

import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.SampleResult;

public class NettyHttp2Client {
    private final String method;
    private final String scheme;
    private final String host;
    private final int port;
    private final String path;
    private final HeaderManager headerManager;

    private static final NettyHttp2ClientFactory HTTP_2_CLIENT_FACTORY = new NettyHttp2ClientFactory();

    public static void close() {
        HTTP_2_CLIENT_FACTORY.close();
    }

    public NettyHttp2Client(String method, String host, int port, String path, HeaderManager headerManager, String scheme) {
        this.method = method;
        this.host = host;
        this.port = port;
        this.path = path;
        this.headerManager = headerManager;
        this.scheme = scheme;
    }

    public SampleResult request() {
        return HTTP_2_CLIENT_FACTORY.request(method, host, port, path, headerManager, scheme);
    }
}
