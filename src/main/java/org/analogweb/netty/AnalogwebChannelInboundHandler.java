package org.analogweb.netty;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import java.net.URI;

import io.netty.util.internal.ThrowableUtil;
import org.analogweb.*;
import org.analogweb.core.DefaultRequestPath;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

/**
 * @author y2k2mt
 */
public class AnalogwebChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Log log = Logs.getLog(AnalogwebChannelInboundHandler.class);
    private final Application application;
    private final ApplicationProperties properties;

    public AnalogwebChannelInboundHandler(Application application, ApplicationProperties properties) {
        this.application = application;
        this.properties = properties;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        handleHttpRequest(ctx, (FullHttpRequest) msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        final RequestContext rcontext = createRequestContext(req);
        final ResponseContext response = createResponseContext(req, ctx);
        try {
            final Response proceed = getApplication().processRequest(rcontext.getRequestPath(), rcontext,
                    response);
            if (proceed == Application.NOT_FOUND) {
                final FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
                sendHttpResponse(ctx, req, res);
                return;
            }
            proceed.commit(rcontext,response);
        } catch (final Exception e) {
            log.log(ServerFactoryImpl.PLUGIN_MESSAGE_RESOURCE,"ENT000001",e);
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
            ctx.close();
        }
    }

    protected ResponseContext createResponseContext(FullHttpRequest req, ChannelHandlerContext ctx) {
        return new FullHttpResponseContext(req, ctx);
    }

    protected RequestContext createRequestContext(FullHttpRequest req) {
        return new FullHttpRequestContext(req, resolveRequestPath(req), getApplicationProperties()
                .getDefaultClientLocale());
    }

    protected Application getApplication() {
        return this.application;
    }

    protected ApplicationProperties getApplicationProperties() {
        return this.properties;
    }

    protected RequestPath resolveRequestPath(FullHttpRequest req) {
        return new DefaultRequestPath(URI.create("/"), URI.create(req.uri()), req.method()
                .name());
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
            FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            final ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // Send the response and close the connection if necessary.
        final ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
