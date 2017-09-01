package jmeter.plugins.http2.sampler;

import org.apache.jmeter.samplers.SampleResult;

public class NettyHttp2ClientTest {

    public static void main(String[] args) {
        NettyHttp2Client client = new NettyHttp2Client("GET", "cache.video.iqiyi.com", 443, "/vps?tvid=758286800&vid=38f9ebdd245724266df2403c404ffe45&uid=1177558396&v=1&qypid=758286800_unknown&src=02032001010000000000&t=1504086370000&k_tag=1&k_uid=52741478-7821-446E-9437-ABA160B015A9&bid=4&pt=53000&d=1&s=0&rs=1&1&k_ft1=54528175&k_err_retries=0&vf=ce21de1e48a0a5338e11d936da337a1a", null, "https");
        SampleResult res = client.request();
        System.out.println(res);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
