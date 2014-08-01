package org.analogweb.netty;

import java.net.URI;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * @author snowgooseyk
 */
public class HttpServer {

	static final boolean SSL = System.getProperty("ssl") != null;
	static final int PORT = Integer.parseInt(System.getProperty("port",
			SSL ? "8443" : "8080"));

	public static void run(String uri) throws SSLException,
			GeneralSecurityException, InterruptedException {
		run(URI.create(uri));
	}

	public static void run(URI uri) throws SSLException,
			GeneralSecurityException, InterruptedException {
		// Configure SSL.
		final SslContext sslCtx;
		if (SSL) {
			// TODO handle certificate.
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContext.newServerContext(ssc.certificate(),
					ssc.privateKey());
		} else {
			sslCtx = null;
		}
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ServerInitializer(sslCtx));
			Channel ch = b.bind(PORT).sync().channel();
			System.err.println("Open your web browser and navigate to "
					+ (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
