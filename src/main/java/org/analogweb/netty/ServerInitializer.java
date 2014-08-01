package org.analogweb.netty;

import static org.analogweb.core.DefaultApplicationProperties.defaultProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.analogweb.Application;
import org.analogweb.ApplicationContext;
import org.analogweb.ApplicationProperties;
import org.analogweb.core.WebApplication;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.analogweb.util.Assertion;
import org.analogweb.util.ClassCollector;
import org.analogweb.util.FileClassCollector;
import org.analogweb.util.JarClassCollector;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 * @author snowgooseyk
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;
	private final Application app;
	private final ApplicationProperties properties;

	public ServerInitializer(SslContext ssl) {
		this(ssl, new WebApplication());
	}

	public ServerInitializer(SslContext ssl, Application app) {
		this(ssl, app, (ApplicationContext) null);
	}

	public ServerInitializer(SslContext ssl, Application app,
			ApplicationContext contextResolver) {
		this(ssl, app, contextResolver, ApplicationPropertiesHolder.configure(
				app, defaultProperties()));
	}

	public ServerInitializer(SslContext ssl, Application app,
			ApplicationProperties props) {
		this(ssl, app, null, props);
	}

	public ServerInitializer(SslContext ssl, Application app,
			ApplicationContext contextResolver, ApplicationProperties props) {
		Assertion.notNull(app, Application.class.getName());
		this.sslCtx = ssl;
		this.properties = props;
		this.app = app;
		app.run(contextResolver, props, getClassCollectors(), Thread
				.currentThread().getContextClassLoader());
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new ServerHandler(app,properties));
	}

	protected List<ClassCollector> getClassCollectors() {
		List<ClassCollector> list = new ArrayList<ClassCollector>();
		list.add(new JarClassCollector());
		list.add(new FileClassCollector());
		return Collections.unmodifiableList(list);
	}
}
