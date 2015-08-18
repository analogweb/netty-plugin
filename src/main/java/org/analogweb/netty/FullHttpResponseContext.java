package org.analogweb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.analogweb.Headers;
import org.analogweb.RequestContext;
import org.analogweb.Response;
import org.analogweb.ResponseEntity;
import org.analogweb.core.AbstractResponseContext;
import org.analogweb.core.ApplicationRuntimeException;
import org.analogweb.core.MapHeaders;

/**
 * @author snowgooseyk
 */
public class FullHttpResponseContext extends AbstractResponseContext {

    protected static long NO_CONTENT = -1;
    protected static long CHUNKED = 0;
    private final FullHttpRequest exc;
    private int status = HttpURLConnection.HTTP_OK;
    private final Headers headers;
    private final ChannelHandlerContext context;

    public FullHttpResponseContext(FullHttpRequest request, ChannelHandlerContext context) {
        this.exc = request;
        this.headers = new MapHeaders();
        this.context = context;
    }

    @Override
    public void commit(RequestContext context, Response r){
        try {
            final ResponseEntity entity = r.getEntity();
            final ByteBuf buffer = Unpooled.buffer();
            final ByteBufOutputStream out = new ByteBufOutputStream(buffer);
            if (entity != null) {
                entity.writeInto(out);
            }
            out.flush();
            final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(getStatus()), buffer);
            final FullHttpRequest request = getFullHttpRequest();
            final boolean close = request.headers()
                    .contains("Connection", HttpHeaders.Values.CLOSE, true)
                    || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                    && !request.headers().contains("Connection", HttpHeaders.Values.KEEP_ALIVE,
                            true);
            if (!close) {
                response.headers().set("Content-Length", buffer.readableBytes());
            }
            final HttpHeaders headers = response.headers();
            final Headers analogHeaders = getResponseHeaders();
            for (final String headerName : analogHeaders.getNames()) {
                headers.set(headerName, analogHeaders.getValues(headerName));
            }
            final Channel channel = getChannelHandlerContext().channel();
            final ChannelFuture future = channel.writeAndFlush(response);
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (final IOException e) {
            throw new ApplicationRuntimeException(e) {

                private static final long serialVersionUID = 1L;
            };
        }
    }

    @Override
    public Headers getResponseHeaders() {
        return this.headers;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    protected int getStatus() {
        return this.status;
    }

    protected FullHttpRequest getFullHttpRequest() {
        return this.exc;
    }

    protected ChannelHandlerContext getChannelHandlerContext() {
        return this.context;
    }
}
