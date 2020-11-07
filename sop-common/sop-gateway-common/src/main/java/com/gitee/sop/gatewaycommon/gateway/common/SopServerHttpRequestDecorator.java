package com.gitee.sop.gatewaycommon.gateway.common;

import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;

/**
 * @author tanghc
 */
public class SopServerHttpRequestDecorator extends ServerHttpRequestDecorator {

    private Flux<DataBuffer> bodyDataBuffer;
    private HttpHeaders httpHeaders;
    private URI uri;

    /**
     * ServerHttpRequest包装，作用类似于HttpServletRequestWrapper
     *
     * @param delegate    老的request
     * @param queryString get请求后面的参数
     */
    public SopServerHttpRequestDecorator(ServerHttpRequest delegate, String queryString) {
        super(delegate);
        if (delegate.getMethod() != HttpMethod.GET) {
            throw new IllegalArgumentException("this constructor must be used by GET request.");
        }
        if (queryString == null) {
            throw new IllegalArgumentException("queryString can not be null.");
        }
        // 默认header是只读的，把它改成可写，方便后面的过滤器使用
        this.httpHeaders = HttpHeaders.writableHttpHeaders(delegate.getHeaders());
        this.uri = UriComponentsBuilder.fromUri(delegate.getURI())
                .replaceQuery(queryString)
                .build(true)
                .toUri();
    }


    /**
     * ServerHttpRequest包装，作用类似于HttpServletRequestWrapper
     *
     * @param delegate 老的request
     * @param bodyData 请求体内容
     */
    public SopServerHttpRequestDecorator(ServerHttpRequest delegate, byte[] bodyData) {
        super(delegate);
        if (bodyData == null) {
            throw new IllegalArgumentException("bodyData can not be null.");
        }
        // 默认header是只读的，把它改成可写，方便后面的过滤器使用
        this.httpHeaders = HttpHeaders.writableHttpHeaders(delegate.getHeaders());
        // 由于请求体已改变，这里要重新设置contentLength
        int contentLength = bodyData.length;
        httpHeaders.setContentLength(contentLength);
        if (contentLength <= 0) {
            // TODO: this causes a 'HTTP/1.1 411 Length Required' on httpbin.org
            httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
        }
        this.bodyDataBuffer = stringBuffer(bodyData);
    }

    /**
     * 字符串转DataBuffer
     *
     * @param bytes 请求体
     * @return 返回buffer
     */
    private static Flux<DataBuffer> stringBuffer(byte[] bytes) {
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return Flux.just(buffer);
    }

    @Override
    public URI getURI() {
        return uri == null ? super.getURI() : uri;
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return bodyDataBuffer == null ? super.getBody() : bodyDataBuffer;
    }
}
