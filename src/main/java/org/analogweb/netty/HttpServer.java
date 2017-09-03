package org.analogweb.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;

import java.net.URI;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLException;

import org.analogweb.Server;

/**
 * @author snowgooseyk
 */
public class HttpServer implements Server {

	private final URI uri;
	private final AnalogwebChannelInitializer initializer;
	private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	// Default thread count depends on -Dio.netty.eventLoopThreads
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();

	public HttpServer(URI uri, AnalogwebChannelInitializer initializer) {
		this.uri = uri;
		this.initializer = initializer;
	}

	protected void start() throws SSLException, GeneralSecurityException,
			InterruptedException {
		try {
			final ServerBootstrap boot = new ServerBootstrap();
			boot.option(ChannelOption.SO_BACKLOG, 1024);
			boot.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(initializer);
			final Channel ch = boot.bind(uri.getHost(), uri.getPort()).sync()
					.channel();
			ch.closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	@Override
	public void run() {
		try {
			start();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown(int mode) {
		final Future<?> fw = workerGroup.shutdownGracefully();
		final Future<?> fb = bossGroup.shutdownGracefully();
		try {
			fw.await();
			fb.await();
		} catch (final InterruptedException e) {
		}
	}
}
