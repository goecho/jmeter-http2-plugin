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
