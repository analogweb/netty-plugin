package org.analogweb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.netty.handler.codec.http2.HttpConversionUtil;
import org.analogweb.*;
import org.analogweb.core.AbstractResponseContext;
import org.analogweb.core.ApplicationRuntimeException;
import org.analogweb.core.MapHeaders;
import org.analogweb.util.StringUtils;

/**
 * @author y2k2mt
 */
public class FullHttpResponseContext extends AbstractResponseContext {

    private final FullHttpRequest exc;
    private int status = HttpURLConnection.HTTP_OK;
    private final Headers headers;
    private final ChannelHandlerContext context;

    public FullHttpResponseContext(FullHttpRequest request,
                                   ChannelHandlerContext context) {
        this.exc = request;
        this.headers = new MapHeaders();
        this.context = context;
    }

    @Override
    public void commit(RequestContext context, Response r) {
        try {
            final ResponseEntity entity = r.getEntity();
            final ByteBuf buffer = Unpooled.buffer();
            final WritableBuffer out = ByteBufWritableBuffer.writeBuffer(buffer);
            if (entity != null) {
                entity.writeInto(out);
            }
            final FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(getStatus()), buffer);
            HttpUtil.setContentLength(response, response.content().readableBytes());
            final FullHttpRequest request = getFullHttpRequest();
            String streamId =
                    request.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
            if (StringUtils.isNotEmpty(streamId)) {
                response.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), streamId);
            }
            final boolean close = request.headers().contains("Connection",
                    HttpHeaderValues.CLOSE, true)
                    || request.protocolVersion().equals(HttpVersion.HTTP_1_0)
                    && !request.headers().contains("Connection",
                    HttpHeaderValues.KEEP_ALIVE, true);
            if (!close) {
                response.headers()
                        .set("Content-Length", buffer.readableBytes());
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
