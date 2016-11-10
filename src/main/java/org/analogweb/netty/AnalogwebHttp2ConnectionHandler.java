package org.analogweb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;
import org.analogweb.*;
import org.analogweb.core.DefaultRequestPath;

import java.net.URI;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author y2k2mt
 */
public class AnalogwebHttp2ConnectionHandler extends Http2ConnectionHandler
		implements
			Http2FrameListener {

	private Application application;
	private ApplicationProperties properties;

	protected AnalogwebHttp2ConnectionHandler(Http2ConnectionDecoder decoder,
			Http2ConnectionEncoder encoder, Http2Settings initialSettings,
			Application app, ApplicationProperties props) {
		super(decoder, encoder, initialSettings);
		this.application = app;
		this.properties = props;
	}

	// TODO implement.
	protected void handleRequest(ChannelHandlerContext ctx,
			Http2Headers headers, int streamId, ByteBuf payload) {
		try {
			FullHttpRequest req = HttpConversionUtil.toFullHttpRequest(
					streamId, headers, payload.alloc(), false);
			RequestContext rcontext = createRequestContext(req);
			final ResponseContext response = createResponseContext(req, ctx);
			final Response proceed = getApplication().processRequest(
					rcontext.getRequestPath(), rcontext, response);
			if (proceed == Application.NOT_FOUND) {
				final FullHttpResponse res = new DefaultFullHttpResponse(
						HTTP_1_1, NOT_FOUND);
				sendHttpResponse(ctx, req, res);
				return;
			}
			proceed.commit(rcontext, response);
		} catch (Exception e) {
			// TODO implement.
			e.printStackTrace();
		}
	}

	private void sendHttpResponse(ChannelHandlerContext ctx,
			FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.status().code() != 200) {
			final ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
					CharsetUtil.UTF_8);
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

	protected RequestContext createRequestContext(FullHttpRequest req) {
		return new FullHttpRequestContext(req, resolveRequestPath(req),
				getApplicationProperties().getDefaultClientLocale());
	}

	protected ResponseContext createResponseContext(FullHttpRequest req,
			ChannelHandlerContext ctx) {
		return new FullHttpResponseContext(req, ctx);
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

	@Override
	public int onDataRead(ChannelHandlerContext channelHandlerContext, int i,
			ByteBuf byteBuf, int i1, boolean b) throws Http2Exception {
		return 0;
	}

	@Override
	public void onHeadersRead(ChannelHandlerContext channelHandlerContext,
			int i, Http2Headers http2Headers, int i1, boolean b)
			throws Http2Exception {

	}

	@Override
	public void onHeadersRead(ChannelHandlerContext channelHandlerContext,
			int i, Http2Headers http2Headers, int i1, short i2, boolean b,
			int i3, boolean b1) throws Http2Exception {

	}

	@Override
	public void onPriorityRead(ChannelHandlerContext channelHandlerContext,
			int i, int i1, short i2, boolean b) throws Http2Exception {

	}

	@Override
	public void onRstStreamRead(ChannelHandlerContext channelHandlerContext,
			int i, long l) throws Http2Exception {

	}

	@Override
	public void onSettingsAckRead(ChannelHandlerContext channelHandlerContext)
			throws Http2Exception {

	}

	@Override
	public void onSettingsRead(ChannelHandlerContext channelHandlerContext,
			Http2Settings http2Settings) throws Http2Exception {

	}

	@Override
	public void onPingRead(ChannelHandlerContext channelHandlerContext,
			ByteBuf byteBuf) throws Http2Exception {

	}

	@Override
	public void onPingAckRead(ChannelHandlerContext channelHandlerContext,
			ByteBuf byteBuf) throws Http2Exception {

	}

	@Override
	public void onPushPromiseRead(ChannelHandlerContext channelHandlerContext,
			int i, int i1, Http2Headers http2Headers, int i2)
			throws Http2Exception {

	}

	@Override
	public void onGoAwayRead(ChannelHandlerContext channelHandlerContext,
			int i, long l, ByteBuf byteBuf) throws Http2Exception {

	}

	@Override
	public void onWindowUpdateRead(ChannelHandlerContext channelHandlerContext,
			int i, int i1) throws Http2Exception {

	}

	@Override
	public void onUnknownFrame(ChannelHandlerContext channelHandlerContext,
			byte b, int i, Http2Flags http2Flags, ByteBuf byteBuf)
			throws Http2Exception {

	}
	// TODO implement.
}
