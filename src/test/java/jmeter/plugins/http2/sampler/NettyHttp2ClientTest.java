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

import org.apache.jmeter.samplers.SampleResult;

public class NettyHttp2ClientTest {

    public static void main(String[] args) {
        NettyHttp2Client client = new NettyHttp2Client("GET", "cache.video.iqiyi.com", 443, "", null, "https");
        SampleResult res = client.request();
        System.out.println(res);

        NettyHttp2Client client2 = new NettyHttp2Client("GET", "cache.video.iqiyi.com", 443, "/test.png", null, "https");
        SampleResult res2 = client2.request();
        System.out.println(res2);

        NettyHttp2Client.close();
    }
}
