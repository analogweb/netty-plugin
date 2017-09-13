package org.analogweb.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.analogweb.Application;
import org.analogweb.ApplicationProperties;

/**
 * @author y2k2mt
 */
public class AnalogwebApplicationProtocolNegotiationHandler
        extends
        ApplicationProtocolNegotiationHandler {
    private final Application app;
    private final ApplicationProperties properties;

    protected AnalogwebApplicationProtocolNegotiationHandler(Application app,
                                                             ApplicationProperties props) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.app = app;
        this.properties = props;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol)
            throws Exception {
        if (Properties.isHTTP2() && ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            DefaultHttp2Connection connection = new DefaultHttp2Connection(true);
            InboundHttp2ToHttpAdapter listener = new InboundHttp2ToHttpAdapterBuilder(connection)
                    .propagateSettings(true).validateHttpHeaders(false)
                    .maxContentLength(Properties.getMaxContentLength()).build();

            ctx.pipeline().addLast(
                    new HttpToHttp2ConnectionHandlerBuilder()
                            .frameListener(listener)
                            .connection(connection).build()
            );

            ctx.pipeline().addLast(new AnalogwebChannelInboundHandler(app, properties));
            return;
        }

        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            ctx.pipeline().addLast(
                    new HttpServerCodec(),
                    new HttpObjectAggregator(Properties.getMaxAggregationSize(properties)),
                    new AnalogwebChannelInboundHandler(getApplication(),
                            getApplicationProperties()));
            return;
        }

        throw new IllegalStateException("Unknown protocol: " + protocol);
    }

    protected Application getApplication() {
        return this.app;
    }

    protected ApplicationProperties getApplicationProperties() {
        return this.properties;
    }
}
