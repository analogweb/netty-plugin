package org.analogweb.netty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.analogweb.Server;
import org.analogweb.ServerFactoryImpl;
import org.analogweb.annotation.Get;
import org.analogweb.annotation.Route;
import org.analogweb.core.DefaultApplicationContext;
import org.analogweb.core.DefaultApplicationProperties;
import org.analogweb.core.WebApplication;
import org.analogweb.util.Maps;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/**
 * @author snowgooseyk
 */
@Route("/")
public class IntegrationTest {

	private static Server server;

	@Route
	@Get
	public String hello() {
		return "Hello, World.";
	}

	@Route
	@Get
	public String exception() {
		throw new IllegalStateException();
	}

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		final ExecutorService es = Executors.newFixedThreadPool(1);
		es.submit(new Runnable() {

			@Override
			public void run() {
				server = new ServerFactoryImpl().create(URI
						.create("http://localhost:58765/"),
						DefaultApplicationProperties.defaultProperties(),
						DefaultApplicationContext.context(Maps
								.<String, Object> newEmptyHashMap()),
						new WebApplication());
				server.run();
			}
		});
		Thread.sleep(2000);
	}

	@AfterClass
	public static void oneTimeTearDown() {
		server.shutdown(0);
	}

	@Test
	public void testOk() throws Exception {
		final AsyncHttpClient cli = new AsyncHttpClient();
		try {
			final Response r = cli.prepareGet("http://localhost:58765/hello")
					.execute().get();
			assertThat(r.getStatusCode(), is(200));
			assertThat(r.getResponseBody(), is("Hello, World."));
		} finally {
			cli.close();
		}
	}

	@Test
	public void testNotFound() throws Exception {
		final AsyncHttpClient cli = new AsyncHttpClient();
		try {
			final Response r = cli
					.prepareGet("http://localhost:58765/notfound").execute()
					.get();
			assertThat(r.getStatusCode(), is(404));
		} finally {
			cli.close();
		}
	}

	@Test
	public void testServerError() throws Exception {
		final AsyncHttpClient cli = new AsyncHttpClient();
		try {
			final Response r = cli
					.prepareGet("http://localhost:58765/exception").execute()
					.get();
			assertThat(r.getStatusCode(), is(500));
		} finally {
			cli.close();
		}
	}

}
