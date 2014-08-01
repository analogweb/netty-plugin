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
import org.analogweb.ResponseContext;
import org.analogweb.core.DefaultResponseWriter;
import org.analogweb.core.MapHeaders;

/**
 * @author snowgooseyk
 */
public class FullHttpResponseContext implements ResponseContext {

	protected static long NO_CONTENT = -1;
	protected static long CHUNKED = 0;
	private final FullHttpRequest exc;
	private int status = HttpURLConnection.HTTP_OK;
	private final ResponseWriter writer;
	private final Headers headers;
	private final ChannelHandlerContext context;

	public FullHttpResponseContext(FullHttpRequest request,
			ChannelHandlerContext context) {
		this.exc = request;
		this.writer = new DefaultResponseWriter();
		this.headers = new MapHeaders();
		this.context = context;
	}

	@Override
	public void commmit(RequestContext req) {
		try {
			ResponseEntity entity = getResponseWriter().getEntity();
			ByteBuf buffer = Unpooled.buffer();
			ByteBufOutputStream out = new ByteBufOutputStream(buffer);
			if (entity != null) {
				entity.writeInto(out);
			}
			out.flush();
			FullHttpResponse response = new DefaultFullHttpResponse(
					HttpVersion.HTTP_1_1,
					HttpResponseStatus.valueOf(getStatus()), buffer);
			FullHttpRequest request = getFullHttpRequest();
			boolean close = request.headers().contains("Connection",
					HttpHeaders.Values.CLOSE, true)
					|| request.getProtocolVersion()
							.equals(HttpVersion.HTTP_1_0)
					&& !request.headers().contains("Connection",
							HttpHeaders.Values.KEEP_ALIVE, true);
			if (!close) {
				response.headers()
						.set("Content-Length", buffer.readableBytes());
			}
			HttpHeaders headers = response.headers();
			Headers analogHeaders = getResponseHeaders();
			for (String headerName : analogHeaders.getNames()) {
				headers.set(headerName, analogHeaders.getValues(headerName));
			}
			Channel channel = getChannelHandlerContext().channel();
			ChannelFuture future = channel.writeAndFlush(response);
			if (close) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Headers getResponseHeaders() {
		return this.headers;
	}

	@Override
	public ResponseWriter getResponseWriter() {
		return this.writer;
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
