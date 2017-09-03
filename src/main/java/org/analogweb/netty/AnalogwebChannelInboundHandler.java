package org.analogweb.netty;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.analogweb.Application;
import org.analogweb.ApplicationProperties;
import org.analogweb.RequestContext;
import org.analogweb.RequestPath;
import org.analogweb.Response;
import org.analogweb.ResponseContext;
import org.analogweb.ServerFactoryImpl;
import org.analogweb.core.DefaultRequestPath;
import org.analogweb.util.StringUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

/**
 * @author y2k2mt
 */
public class AnalogwebChannelInboundHandler
        extends
        SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Log log = Logs
            .getLog(AnalogwebChannelInboundHandler.class);
    private final Application application;
    private final ApplicationProperties properties;

    public AnalogwebChannelInboundHandler(Application application,
                                          ApplicationProperties properties) {
        this.application = application;
        this.properties = properties;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg)
            throws Exception {
        handleHttpRequest(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx,
                                   FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
                    BAD_REQUEST));
            return;
        }
        final RequestContext rcontext = createRequestContext(req);
        final ResponseContext response = createResponseContext(req, ctx);
        try {
            final Response proceed = getApplication().processRequest(
                    rcontext.getRequestPath(), rcontext, response);
            if (proceed == Application.NOT_FOUND) {
                final FullHttpResponse res = new DefaultFullHttpResponse(
                        HTTP_1_1, NOT_FOUND);
                sendHttpResponse(ctx, req, res);
                return;
            }
            ctx.executor().schedule(new Runnable() {
                @Override
                public void run() {
                    proceed.commit(rcontext, response);
                }
            }, Properties.getScheduleTimeoutLimit(), TimeUnit.MILLISECONDS);
        } catch (final Exception e) {
            log.log(ServerFactoryImpl.PLUGIN_MESSAGE_RESOURCE, "ENT000001", e);
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
                    INTERNAL_SERVER_ERROR));
            ctx.close();
        }
    }

    protected ResponseContext createResponseContext(FullHttpRequest req,
                                                    ChannelHandlerContext ctx) {
        return new FullHttpResponseContext(req, ctx);
    }

    protected RequestContext createRequestContext(FullHttpRequest req) {
        return new FullHttpRequestContext(req, resolveRequestPath(req),
                getApplicationProperties().getDefaultClientLocale());
    }

    protected Application getApplication() {
        return this.application;
    }

    protected ApplicationProperties getApplicationProperties() {
        return this.properties;
    }

    protected RequestPath resolveRequestPath(FullHttpRequest req) {
        return new DefaultRequestPath(URI.create("/"), URI.create(req.uri()),
                req.method().name());
    }

    private void sendHttpResponse(ChannelHandlerContext ctx,
                                  FullHttpRequest req, FullHttpResponse res) {
        HttpUtil.setContentLength(res, res.content().readableBytes());
        String streamId =
                req.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
        if (StringUtils.isNotEmpty(streamId)) {
            res.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), streamId);
        }
        // Generate an error page if response getStatus code is not OK (200).
        ctx.executor().schedule(new Runnable() {
            @Override
            public void run() {
                ctx.writeAndFlush(res);
            }
        }, Properties.getScheduleTimeoutLimit(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
