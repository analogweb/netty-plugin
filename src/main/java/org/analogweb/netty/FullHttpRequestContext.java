package org.analogweb.netty;

import io.netty.handler.codec.http.FullHttpRequest;

import java.io.IOException;
import java.util.Locale;

import org.analogweb.Headers;
import org.analogweb.ReadableBuffer;
import org.analogweb.RequestPath;
import org.analogweb.core.AbstractRequestContext;

/**
 * @author snowgooseyk
 */
public class FullHttpRequestContext extends AbstractRequestContext {

	private final FullHttpRequest ex;

	public FullHttpRequestContext(FullHttpRequest ex, RequestPath requestPath,
			Locale defaultLocale) {
		super(requestPath, defaultLocale);
		this.ex = ex;
	}

	protected FullHttpRequest getFullHttpRequest() {
		return this.ex;
	}

	@Override
	public ReadableBuffer getRequestBody() throws IOException {
		return ByteBufReadableBuffer.readBuffer(getFullHttpRequest().content());
	}

	@Override
	public Headers getRequestHeaders() {
		return new FullHttpHeaders(getFullHttpRequest().headers());
	}

	@Override
	public String getRequestMethod() {
		return getFullHttpRequest().method().name();
	}
}
