package org.analogweb.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.File;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

import org.analogweb.Application;
import org.analogweb.ApplicationContext;
import org.analogweb.ApplicationProperties;
import org.analogweb.ServerFactoryImpl;
import org.analogweb.core.ApplicationRuntimeException;
import org.analogweb.util.Assertion;
import org.analogweb.util.ClassCollector;
import org.analogweb.util.FileClassCollector;
import org.analogweb.util.JarClassCollector;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

/**
 * @author y2k2mt
 */
public class AnalogwebChannelInitializer
        extends
        ChannelInitializer<SocketChannel> {

    private static final Log log = Logs
            .getLog(AnalogwebChannelInitializer.class);
    private final SslContext sslCtx;
    private final Application app;
    private final ApplicationProperties properties;
    private final EventExecutorGroup handlerSpecificExecutorGroup = new DefaultEventExecutorGroup(
            Properties.getExecutorParallelism());

    public AnalogwebChannelInitializer(SslContext ssl, Application app,
                                       ApplicationContext contextResolver, ApplicationProperties props) {
        Assertion.notNull(app, Application.class.getName());
        this.sslCtx = ssl == null ? resolveSslContext() : ssl;
        this.properties = props;
        this.app = app;
        getApplication().run(contextResolver, getApplicationProperties(),
                getClassCollectors(), getClassLoader());
    }

    protected SslContext resolveSslContext() {
        if (Properties.isSSL()) {
            try {
                File privateKey = Properties.getSSLPrivateKey(getApplicationProperties());
                File certificate = Properties.getSSLCertificate(getApplicationProperties());
                String passPhrase = Properties.getSSLKeyPassPhrase(getApplicationProperties());
                if (privateKey == null || certificate == null) {
                    log.log(ServerFactoryImpl.PLUGIN_MESSAGE_RESOURCE, "WNT000002");
                    final SelfSignedCertificate ssc = new SelfSignedCertificate();
                    privateKey = ssc.privateKey();
                    certificate = ssc.certificate();
                    passPhrase = null;
                }
                SslContextBuilder building = SslContextBuilder
                        .forServer(certificate, privateKey, passPhrase)
                        .sslProvider(Properties.isOpenSSL() ? SslProvider.OPENSSL : SslProvider.JDK)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE);
                if (Properties.isHTTP2()) {
                    building.applicationProtocolConfig(new ApplicationProtocolConfig(
                            Protocol.ALPN,
                            SelectorFailureBehavior.NO_ADVERTISE,
                            SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1
                    ));
                }
                return building.build();
            } catch (final SSLException e) {
                throw new ApplicationRuntimeException(e) {
                    private static final long serialVersionUID = 1L;
                };
            } catch (final CertificateException e) {
                throw new ApplicationRuntimeException(e) {
                    private static final long serialVersionUID = 1L;
                };
            }
        } else {
            return null;
        }
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        final SslContext ssl = getSslContext();
        if (ssl == null) {
            initChannelWithClearText(ch);
        } else {
            initChannelWithSsl(ssl, ch);
        }
    }

    protected void initChannelWithSsl(SslContext sslContext, SocketChannel ch)
            throws Exception {
        ch.pipeline().addLast(
                sslContext.newHandler(ch.alloc()),
                new AnalogwebApplicationProtocolNegotiationHandler(
                        getApplication(), getApplicationProperties()));
    }

    protected void initChannelWithClearText(SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(
                Properties.getMaxAggregationSize(getApplicationProperties())));
        pipeline.addLast(getHandlerSpecificExecutorGroup(),
                createServerHandler());
    }

    protected ChannelHandler createServerHandler() {
        return new AnalogwebChannelInboundHandler(getApplication(),
                getApplicationProperties());
    }

    protected SslContext getSslContext() {
        return this.sslCtx;
    }

    protected Application getApplication() {
        return this.app;
    }

    protected ApplicationProperties getApplicationProperties() {
        return this.properties;
    }

    protected ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    protected EventExecutorGroup getHandlerSpecificExecutorGroup() {
        return this.handlerSpecificExecutorGroup;
    }

    protected List<ClassCollector> getClassCollectors() {
        final List<ClassCollector> list = new ArrayList<ClassCollector>();
        list.add(new JarClassCollector());
        list.add(new FileClassCollector());
        return Collections.unmodifiableList(list);
    }
}
