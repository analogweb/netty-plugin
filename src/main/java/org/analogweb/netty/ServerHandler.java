package org.analogweb.netty;

import java.net.URI;

import org.analogweb.Application;
import org.analogweb.ApplicationProperties;
import org.analogweb.RequestContext;
import org.analogweb.RequestPath;
import org.analogweb.ResponseContext;
import org.analogweb.core.DefaultRequestPath;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import io.netty.util.CharsetUtil;

/**
 * @author snowgooseyk
 */
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

	private final Application application;
	private final ApplicationProperties properties;

	public ServerHandler(Application application,
			ApplicationProperties properties) {
		this.application = application;
		this.properties = properties;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		handleHttpRequest(ctx, (FullHttpRequest) msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx,
			FullHttpRequest req) {
		// Handle a bad request.
		if (!req.getDecoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
					BAD_REQUEST));
			return;
		}
		RequestContext rcontext = createRequestContext(req);
		ResponseContext response = createResponseContext(req, ctx);
		try {
			int proceed = getApplication().processRequest(
					rcontext.getRequestPath(), rcontext, response);
			if (proceed == Application.NOT_FOUND) {
				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1,
						NOT_FOUND);
				sendHttpResponse(ctx, req, res);
				return;
			}
			response.commmit(rcontext);
		} catch (Exception e) {
			e.printStackTrace();
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
		return new DefaultRequestPath(URI.create("/"),
				URI.create(req.getUri()), req.getMethod().name());
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx,
			FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(),
					CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			setContentLength(res, res.content().readableBytes());
		}
		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
