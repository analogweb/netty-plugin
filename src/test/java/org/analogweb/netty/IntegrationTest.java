package org.analogweb.netty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.analogweb.Server;
import org.analogweb.annotation.Get;
import org.analogweb.annotation.Route;
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
                server = HttpServers.create("http://localhost:18765/");
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
            final Response r = cli.prepareGet("http://localhost:18765/hello").execute().get();
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
            final Response r = cli.prepareGet("http://localhost:18765/notfound").execute().get();
            assertThat(r.getStatusCode(), is(404));
        } finally {
            cli.close();
        }
    }

    @Test
    public void testServerError() throws Exception {
        final AsyncHttpClient cli = new AsyncHttpClient();
        try {
            final Response r = cli.prepareGet("http://localhost:18765/exception").execute().get();
            assertThat(r.getStatusCode(), is(500));
        } finally {
            cli.close();
        }
    }
}
