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

/**
 * @author snowgooseyk
 */
public class HttpServer {

	private URI uri;
	private ServerInitializer initializer;
	
	public HttpServer(URI uri){
		this(uri,new ServerInitializer());
	}

	public HttpServer(URI uri,ServerInitializer initializer){
		this.uri = uri;
		this.initializer = initializer;
	}

	public static void run(String uri) throws SSLException,
			GeneralSecurityException, InterruptedException {
		run(URI.create(uri));
	}

	public static void run(URI uri) throws SSLException,
			GeneralSecurityException, InterruptedException {
		new HttpServer(uri, new ServerInitializer()).start();
	}

	public void start()
			throws SSLException, GeneralSecurityException, InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		// Default thread count depends on -Dio.netty.eventLoopThreads
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(initializer);
			Channel ch = boot.bind(uri.getHost(),uri.getPort()).sync().channel();
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
