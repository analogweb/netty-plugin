package org.analogweb;

import java.net.URI;

import org.analogweb.netty.HttpServer;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
	/**
	 * Rigourous Test :-)
	 */
	@Test
	@Ignore
	public void testRun() throws Exception {
		HttpServer.run(URI.create("http://localhost:8080/"));
	}
}
