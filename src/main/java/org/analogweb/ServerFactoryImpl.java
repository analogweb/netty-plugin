package org.analogweb;

import io.netty.handler.ssl.SslContext;
import io.netty.util.Version;

import java.net.URI;

import org.analogweb.Application;
import org.analogweb.ApplicationContext;
import org.analogweb.ApplicationProperties;
import org.analogweb.Server;
import org.analogweb.ServerFactory;
import org.analogweb.netty.AnalogwebChannelInitializer;
import org.analogweb.netty.HttpServers;
import org.analogweb.util.MessageResource;
import org.analogweb.util.PropertyResourceBundleMessageResource;

/**
 * @author y2k2mt
 */
public class ServerFactoryImpl implements ServerFactory {

	public static final MessageResource PLUGIN_MESSAGE_RESOURCE = new PropertyResourceBundleMessageResource(
			"org.analogweb.netty.analog-messages");
	@Override
	public Server create(URI uri, ApplicationProperties properties,
			ApplicationContext context, Application application) {
		Object obj = properties.getProperties().get(
				"org.analogweb.netty.ssl.context");
		SslContext ssl = (obj instanceof SslContext) ? (SslContext) obj : null;
		return HttpServers.create(uri, new AnalogwebChannelInitializer(ssl,
				application, context, properties));
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Analogweb Netty Plugin : Netty[");
		s.append(
				Version.identify().values().iterator().next().artifactVersion())
				.append("]");
		return s.toString();
	}
}
