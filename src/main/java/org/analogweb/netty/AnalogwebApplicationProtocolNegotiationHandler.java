package org.analogweb.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
  import io.netty.handler.codec.http.HttpObjectAggregator;
  import io.netty.handler.codec.http.HttpServerCodec;
import org.analogweb.Application;
import org.analogweb.ApplicationProperties;

/**
 * @author y2k2mt
 */
public class AnalogwebApplicationProtocolNegotiationHandler extends ApplicationProtocolNegotiationHandler {
    private final Application app;
    private final ApplicationProperties properties;
    // TODO Wrap ApplicationProperties.
    protected static final int DEFAULT_AGGREGATION_SIZE = 10485760;

    protected AnalogwebApplicationProtocolNegotiationHandler(Application app,ApplicationProperties props) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.app = app;
        this.properties = props;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
                  if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                          ctx.pipeline().addLast(new AnalogwebHttp2ConnectionHandlerBuilder(getApplication(),getApplicationProperties()).build());
                          return;
                      }

                  if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                      ctx.pipeline().addLast(new HttpServerCodec(),
                              new HttpObjectAggregator(DEFAULT_AGGREGATION_SIZE),
                              new AnalogwebChannelInboundHandler(getApplication(),getApplicationProperties()));
                          return;
                      }

                  throw new IllegalStateException("unknown protocol: " + protocol);
    }
    protected Application getApplication() {
        return this.app;
    }

    protected ApplicationProperties getApplicationProperties() {
        return this.properties;
    }
}
