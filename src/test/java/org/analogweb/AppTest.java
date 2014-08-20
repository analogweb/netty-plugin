package org.analogweb;

import org.analogweb.netty.HttpServers;
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
		HttpServers.create("http://localhost:8080/").run();
	}
}
