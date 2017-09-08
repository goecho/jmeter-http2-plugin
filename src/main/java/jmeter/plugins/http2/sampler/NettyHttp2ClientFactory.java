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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyHttp2ClientFactory {

    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static SslContext sslCtx = getSslContext();

    public NettyHttp2ClientFactory() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> workerGroup.shutdownGracefully()));
    }

    public void close() {
        workerGroup.shutdownGracefully();
    }

    public SampleResult request(String method, String host, int port, String path, HeaderManager headerManager, String scheme) {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        Http2ClientInitializer initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE);
        SampleResult sampleResult = new SampleResult();
        b.remoteAddress(host, port);
        b.handler(initializer);
        // Start sampling
        sampleResult.sampleStart();

        // Start the client.
        Channel channel = b.connect().syncUninterruptibly().channel();

        // Wait for the HTTP/2 upgrade to occur.
        Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
        try {
            http2SettingsHandler.awaitSettings(200, TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            sampleResult.setSuccessful(false);
            return sampleResult;
        }

        HttpResponseHandler responseHandler = initializer.responseHandler();
        final int streamId = 3;
        final URI hostName = URI.create(scheme + "://" + host + ':' + port);

        // Set attributes to SampleResult
        try {
            sampleResult.setURL(hostName.toURL());
        } catch (MalformedURLException exception) {
            sampleResult.setSuccessful(false);
            return sampleResult;
        }

        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, new HttpMethod(method), path);
        request.headers().add(HttpHeaderNames.HOST, hostName.getHost());
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme);

        // Add request headers set by HeaderManager
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                PropertyIterator i = headers.iterator();
                while (i.hasNext()) {
                    org.apache.jmeter.protocol.http.control.Header header
                            = (org.apache.jmeter.protocol.http.control.Header) i.next().getObjectValue();
                    request.headers().add(header.getName(), header.getValue());
                }
            }
        }

        responseHandler.put(streamId, channel.write(request), channel.newPromise());

        final SortedMap<Integer, Http2Response> responseMap;
        try {
            channel.flush();
            responseMap = responseHandler.awaitResponses(5, TimeUnit.SECONDS);
            // Currently pick up only one response of a stream
            Http2Response http2Response = responseMap.get(streamId);
            final FullHttpResponse response = http2Response.getFullHttpResponse();
            final AsciiString responseCode = response.status().codeAsText();
            final String reasonPhrase = response.status().reasonPhrase();
            sampleResult.setResponseCode(new StringBuilder(responseCode.length()).append(responseCode).toString());
            sampleResult.setResponseMessage(new StringBuilder(reasonPhrase.length()).append(reasonPhrase).toString());
            sampleResult.setResponseHeaders(getResponseHeaders(response));
            sampleResult.setResponseData(http2Response.getContext(), "utf-8");
        } catch (Exception exception) {
            sampleResult.setSuccessful(false);
            return sampleResult;
        }

        // Wait until the connection is closed.
        channel.close().syncUninterruptibly();

        // End sampling
        sampleResult.sampleEnd();
        sampleResult.setSuccessful(true);

        return sampleResult;
    }

    private static SslContext getSslContext() {
        SslContext sslCtx = null;

        final SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;

        try {
            sslCtx = SslContextBuilder.forClient()
                    .sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            Protocol.ALPN,
                            // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                            SelectorFailureBehavior.NO_ADVERTISE,
                            // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                            SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1))
                    .build();
        } catch (SSLException exception) {
            return null;
        }

        return sslCtx;
    }

    /**
     * Convert Response headers set by Netty stack to one String instance
     */
    private String getResponseHeaders(FullHttpResponse response) {
        StringBuilder headerBuf = new StringBuilder();

        Iterator<Entry<String, String>> iterator = response.headers().iteratorAsString();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            headerBuf.append(entry.getKey());
            headerBuf.append(": ");
            headerBuf.append(entry.getValue());
            headerBuf.append("\n");
        }

        return headerBuf.toString();
    }
}
