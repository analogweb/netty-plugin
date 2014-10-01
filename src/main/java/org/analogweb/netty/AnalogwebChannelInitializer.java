package org.analogweb.netty;

import static org.analogweb.core.DefaultApplicationProperties.defaultProperties;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

import org.analogweb.Application;
import org.analogweb.ApplicationContext;
import org.analogweb.ApplicationProperties;
import org.analogweb.core.ApplicationRuntimeException;
import org.analogweb.core.WebApplication;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.analogweb.util.Assertion;
import org.analogweb.util.ClassCollector;
import org.analogweb.util.FileClassCollector;
import org.analogweb.util.JarClassCollector;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author snowgooseyk
 */
public class AnalogwebChannelInitializer extends ChannelInitializer<SocketChannel> {

	static final boolean SSL = System.getProperty("ssl") != null;
	private final SslContext sslCtx;
	private final Application app;
	private final ApplicationProperties properties;
	private EventExecutorGroup handlerSpecificExecutorGroup = new DefaultEventExecutorGroup(
			8);

	public AnalogwebChannelInitializer() {
		this(null);
	}

	public AnalogwebChannelInitializer(SslContext ssl) {
		this(ssl, new WebApplication());
	}

	public AnalogwebChannelInitializer(SslContext ssl, Application app) {
		this(ssl, app, (ApplicationContext) null);
	}

	public AnalogwebChannelInitializer(SslContext ssl, Application app,
			ApplicationContext contextResolver) {
		this(ssl, app, contextResolver, ApplicationPropertiesHolder.configure(
				app, defaultProperties()));
	}

	public AnalogwebChannelInitializer(SslContext ssl, Application app,
			ApplicationProperties props) {
		this(ssl, app, null, props);
	}

	public AnalogwebChannelInitializer(SslContext ssl, Application app,
			ApplicationContext contextResolver, ApplicationProperties props) {
		Assertion.notNull(app, Application.class.getName());
		this.sslCtx = ssl == null ? resolveSslContext() : ssl;
		this.properties = props;
		this.app = app;
		getApplication().run(contextResolver, getApplicationProperties(),
				getClassCollectors(), getClassLoader());
	}

	protected SslContext resolveSslContext() {
		final SslContext sslCtx;
		if (SSL) {
			try {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx = SslContext.newServerContext(ssc.certificate(),
						ssc.privateKey());
			} catch (SSLException e) {
				throw new ApplicationRuntimeException(e) {
					private static final long serialVersionUID = 1L;
				};
			} catch (CertificateException e) {
				throw new ApplicationRuntimeException(e) {
					private static final long serialVersionUID = 1L;
				};
			}
		} else {
			sslCtx = null;
		}
		return sslCtx;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		SslContext ssl = getSslContext();
		if (ssl != null) {
			pipeline.addLast(ssl.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(getHandlerSpecificExecutorGroup(), createServerHandler());
	}

	protected ChannelHandler createServerHandler() {
		return new AnalogwebChannelInboundHandler(getApplication(), getApplicationProperties());
	}

	protected SslContext getSslContext() {
		return this.sslCtx;
	}

	protected Application getApplication() {
		return this.app;
	}

	protected ApplicationProperties getApplicationProperties() {
		return this.properties;
	}

	protected ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	
	protected EventExecutorGroup getHandlerSpecificExecutorGroup(){
		return this.handlerSpecificExecutorGroup;
	}

	protected List<ClassCollector> getClassCollectors() {
		List<ClassCollector> list = new ArrayList<ClassCollector>();
		list.add(new JarClassCollector());
		list.add(new FileClassCollector());
		return Collections.unmodifiableList(list);
	}
}
