package org.analogweb.netty;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.analogweb.Cookies;
import org.analogweb.Headers;
import org.analogweb.MediaType;
import org.analogweb.Parameters;
import org.analogweb.RequestContext;
import org.analogweb.RequestPath;
import org.analogweb.core.AcceptLanguages;
import org.analogweb.core.ApplicationRuntimeException;
import org.analogweb.core.EmptyCookies;
import org.analogweb.core.FormParameters;
import org.analogweb.core.MatrixParameters;
import org.analogweb.core.MediaTypes;
import org.analogweb.core.QueryParameters;
import org.analogweb.core.RequestCookies;
import org.analogweb.util.CollectionUtils;

/**
 * @author snowgooseyk
 */
public class FullHttpRequestContext implements RequestContext {

	private final FullHttpRequest ex;
	private final RequestPath requestPath;
	private final Parameters params;
	private final Parameters matrixParams;
	private Parameters formParams;
	private final AcceptLanguages langs;
	private final Locale defaultLocale;

	FullHttpRequestContext(FullHttpRequest ex, RequestPath requestPath,
			Locale defaultLocale) {
		this.ex = ex;
		this.requestPath = requestPath;
		this.params = new QueryParameters(getRequestPath().getRequestURI());
		this.matrixParams = new MatrixParameters(getRequestPath()
				.getRequestURI());
		this.langs = new AcceptLanguages(this);
		this.defaultLocale = defaultLocale;
	}

	protected FullHttpRequest getFullHttpRequest() {
		return this.ex;
	}

	@Override
	public MediaType getContentType() {
		List<String> header = getRequestHeaders().getValues("Content-Type");
		if (CollectionUtils.isEmpty(header)) {
			return null;
		}
		return MediaTypes.valueOf(header.get(0));
	}

	@Override
	public Cookies getCookies() {
		List<String> cookieHeader = getRequestHeaders().getValues("Cookie");
		if (CollectionUtils.isEmpty(cookieHeader)) {
			return new EmptyCookies();
		}
		return new RequestCookies(cookieHeader.get(0));
	}

	@Override
	public Parameters getQueryParameters() {
		return this.params;
	}

	@Override
	public Parameters getMatrixParameters() {
		return this.matrixParams;
	}

	@Override
	public Parameters getFormParameters() {
		if (this.formParams == null) {
			try {
				this.formParams = new FormParameters(getRequestPath()
						.getRequestURI(), getRequestBody(), getContentType());
			} catch (IOException e) {
				throw new ApplicationRuntimeException(e) {

					// TODO
					private static final long serialVersionUID = 1L;
				};
			}
		}
		return this.formParams;
	}

	@Override
	public InputStream getRequestBody() throws IOException {
		return new ByteBufInputStream(getFullHttpRequest().content());
	}

	@Override
	public Headers getRequestHeaders() {
		return new FullHttpHeaders(getFullHttpRequest().headers());
	}

	@Override
	public RequestPath getRequestPath() {
		return this.requestPath;
	}

	@Override
	public List<Locale> getLocales() {
		return this.langs.getLocales();
	}

	@Override
	public Locale getLocale() {
		return CollectionUtils.indexOf(getLocales(), 0, this.defaultLocale);
	}
}
