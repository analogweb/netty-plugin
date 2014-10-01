package org.analogweb.netty;

import java.net.URI;

import org.analogweb.Server;

public class HttpServers {
	
	public static Server create(String uri){
		return create(URI.create(uri));
	}

	public static Server create(String uri,AnalogwebChannelInitializer serverInitializer){
		return create(URI.create(uri),serverInitializer);
	}

	public static Server create(URI uri){
		return new HttpServer(uri);
	}

	public static Server create(URI uri,AnalogwebChannelInitializer serverInitializer){
		return new HttpServer(uri,serverInitializer);
	}

}
