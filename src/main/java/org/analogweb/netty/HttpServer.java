package org.analogweb.netty;

import java.net.URI;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLException;

import org.analogweb.Server;

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
public class HttpServer implements Server {

	private URI uri;
	private AnalogwebChannelInitializer initializer;
	private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	// Default thread count depends on -Dio.netty.eventLoopThreads
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	public HttpServer(URI uri){
		this(uri,new AnalogwebChannelInitializer());
	}

	public HttpServer(URI uri,AnalogwebChannelInitializer initializer){
		this.uri = uri;
		this.initializer = initializer;
	}

	protected void start()
			throws SSLException, GeneralSecurityException, InterruptedException {
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

	@Override
	public void run() {
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown(int mode) {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
}
